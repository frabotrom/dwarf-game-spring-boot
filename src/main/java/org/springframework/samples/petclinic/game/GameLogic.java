package org.springframework.samples.petclinic.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.board.Board;
import org.springframework.samples.petclinic.board.Cell;
import org.springframework.samples.petclinic.card.Card;
import org.springframework.samples.petclinic.card.CardService;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.playerState.PlayerState;
import org.springframework.samples.petclinic.userDwarf.UserDwarf;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class GameLogic {

    private static final Map<String, Integer> special2normal = Map.of("muster", 59, "hold", 60, "sell", 61,
        "past", 62, "special", 63, "turn", 64,
        "apprentice", 57, "collapse", 65, "run", 58);

    private final CardService cardService;

    private static GameLogic instance = null;

    private GameLogic(@Autowired CardService cardService) {
        this.cardService = cardService;
    }

    public GameLogic getInstance(CardService cardService) {
        if (instance == null) {
            instance = new GameLogic(cardService);
        }
        return instance;
    }

    private static final Class<?> gameClass = Game.class;
    private static final List<Integer> possibleActions = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 19, 110, 111, 29, 210, 211, 39, 310, 311);

    public void initPlayerStates(Game game) throws NoSuchMethodException, InvocationTargetException,
        IllegalAccessException {

        List<Integer> order = game.getOrder();

        game.getTurnsOrder().addAll(order);
        Collections.reverse(order);
        game.getTurnsOrder().addAll(order);
        Collections.reverse(order);

        game.getPlayerState_0().setId(0);
        game.getPlayerState_1().setId(1);
        game.getPlayerState_2().setId(2);

        game.setActivePlayer(game.getTurnsOrder().remove(0));

    }

    public void initBoard(Game game, List<Card> allSpecialCards, List<Card> allNormalCards,
                          List<Card> allInitialCards) {
        Board board = game.getBoard();

        List<Integer> specialIdList = allSpecialCards.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<Integer> normalIdList = allNormalCards.stream().map(BaseEntity::getId).collect(Collectors.toList());

        // Shuffle all normal cards and add them to the draw deck
        Collections.shuffle(normalIdList);
        board.setDeck(normalIdList);

        // Shuffle special cards and get them in the special slots
        Collections.shuffle(specialIdList);
        board.setCartasAccionEspecial_0(specialIdList.subList(0, 3));
        board.setCartasAccionEspecial_0(specialIdList.subList(3, 6));
        board.setCartasAccionEspecial_0(specialIdList.subList(6, 9));

        // Draw the nine initial cards and put them in the mine slots
        List<Integer> allInitialCardsIds = allInitialCards.stream().map(BaseEntity::getId).collect(Collectors.toList());

        board.setCardCells(new ArrayList<>());

        Integer cardId;
        for (int index = 0; index < 9; index++) {
            int finalIndex = index;
            cardId = allInitialCardsIds.get(index);
            board.getCardCells().add(new Cell());
            board.getCell(index).addToTop(cardId);
        }

    }

    public String playerTurn(Game game, ClientData data)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
        IllegalStateException {

        // TODO: Write an ordinal enum for possible return states and change return to
        // an int;

        if (!possibleActions.contains(data.getPlayerAction())) {
            throw new IllegalStateException("Action described is not possible.");
        }

        int playerAction = data.getPlayerAction();
        int playerIndex = getPlayerIndex(game, data);
        PlayerState playerState;

        if (playerIndex == game.getActivePlayer()) {
            playerState = getIndexedPlayerState(game, playerIndex);
            List<Integer> workerList = playerState.getWorkerList();

            // Finding out which worker is available
            int worker = workerList.indexOf(12);
            if (worker < 0) {
                return "no worker available";
            }

            if (List.of(9, 10, 11).contains(playerAction % 100)) {
                long workerCount = workerList.stream().takeWhile(w -> w == 12).count();
                // Check if available worker count is enough and if they aren't special workers
                if ((workerCount == 2) && !(worker == 2 || worker == 3)) {
                    game.setPhase(Phase.ESPECIAL);

                    return specialAction(game, data);
                } else if ((workerCount == 1) && !(worker == 2 || worker == 3)) {
                    List<Integer> resourcesList = playerState.getResourcesList();
                    int resourceIndex = (playerAction / 100) - 1;

                    if (resourcesList.get(resourceIndex) >= 4) {
                        resourcesList.set(resourceIndex, resourcesList.get(resourceIndex) - 4);
                        game.setPhase(Phase.ESPECIAL);
                        data.setPlayerAction(playerAction % 100);

                        return specialAction(game, data);
                    }
                } else {
                    return "special action not possible";
                }
            }

            if (game.getAllPlayerStates().stream().flatMap(pS -> pS.getWorkerList().stream())
                .anyMatch(w -> w == playerAction)) {
                return "mine position occupied";
            }

            workerList.set(worker, playerAction);

            game.setActivePlayer(game.getTurnsOrder().remove(0));

        } else {
            return "player isn't the active player";
        }

        return "player turn finished";
    }

    public String specialAction(Game game, ClientData data)
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        /*
         * Overview of how it works:
         * x Change worker states.
         * o Invoke card effect.
         * x Get normal card.
         * x Check if someone in position of normal card; if true, give player resources
         * of original card. Mark position of card as blocked
         * for more workers (somehow).
         * o Done.
         */

        Board board = game.getBoard();

        Integer playerAction = data.getPlayerAction();
        switch (playerAction / 100) {
            case 4:
                data.setPlayerAction(playerAction % 100);
                String result = playerTurn(game, data);

                board.getCell(5).addToTop(special2normal.get("turn"));

                return result;

            case 5:
                PlayerState playerState = getIndexedPlayerState(game, game.getActivePlayer());
                List<Integer> resourcesList = playerState.getResourcesList();
                int resourceIndex = playerAction % 100 - 1;
                resourcesList.set(resourceIndex, resourcesList.get(resourceIndex) + 5);

                return "done";

        }

        Card card;
        String effect;
        List<Integer> specialCardDeck;
        switch (playerAction) {
            case 9:
                specialCardDeck = board.getCartasAccionEspecial_0();
                if (specialCardDeck.isEmpty())
                    return "special deck empty";

                card = cardService.findCardById(specialCardDeck.remove(0));
                effect = card.getEffect();

                return invokeEffect(game, effect);

            case 10:
                specialCardDeck = board.getCartasAccionEspecial_1();
                if (specialCardDeck.isEmpty())
                    return "special deck empty";
                card = cardService.findCardById(specialCardDeck.remove(0));
                effect = card.getEffect();

                return invokeEffect(game, effect);
            case 11:
                specialCardDeck = board.getCartasAccionEspecial_2();
                if (specialCardDeck.isEmpty())
                    return "special deck empty";
                card = cardService.findCardById(specialCardDeck.remove(0));
                effect = card.getEffect();

                return invokeEffect(game, effect);
        }

        return "something went wrong";
    }

    private String invokeEffect(Game game, String effect)
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Board board = game.getBoard();

        switch (effect) {
            case "muster":
                //Done
                changeCard(board, 0, game, effect, true);
                game.setDoDefend(false);

                return "done";

            case "hold":
                //Done
                changeCard(board, 1, game, effect, false);

                List<Cell> cardCells = board.getCardCells();
                cardCells.forEach(cell -> {
                    if (cell.getCards().size() > 1) {
                        board.getDeck().add(cell.removeCardOnTop());
                    }
                });

                Collections.shuffle(board.getDeck());

                board.getCell(1).addToTop(special2normal.get(effect));

                return "done";

            case "sell":
                if (getIndexedPlayerState(game, game.getActivePlayer()).getObject() == 0) {
                    return "not possible";
                }
                changeCard(board, 2, game, effect, true);

                return effect;

            case "past":
                changeCard(board, 3, game, effect, true);

                // TODO: Offer list of selected cell to player

                return effect;

            case "special":
                changeCard(board, 4, game, effect, true);

                // TODO: Add player action for selecting resource;

                return effect;

            case "turn":
                //Done
                changeCard(board, 5, game, effect, false);

                List<PlayerState> allPlayerStates = game.getAllPlayerStates();

                List<Integer> positions =
                    IntStream.range(0, 9).filter(p -> allPlayerStates.stream().anyMatch(playerState -> playerState.getWorkerList().contains(p)))
                        .boxed().collect(Collectors.toList());

                game.setAvailablePositions(positions);
                game.setDoSpecialEffect(true);

                return "special action";

            case "apprentice":
                changeCard(board, 6, game, effect, true);

                // TODO: Be able to select which location you want to put the worker on that's
                // already occupied.

                return effect;

            case "collapse":
                //Done
                changeCard(board, 7, game, effect, false);

                board.getCardCells().forEach(cell -> Collections.rotate(cell.getCards(), 1));

                board.getCell(7).addToTop(special2normal.get(effect));

                return "done";

            case "run":
                //Done
                changeCard(board, 8, game, effect, false);

                board.getCardCells().forEach(cell -> Collections.shuffle(cell.getCards()));

                board.getCell(8).addToTop(special2normal.get(effect));

                return "done";

        }
        return "something went wrong";
    }

    private void changeCard(Board board, int index, Game game, String effect, boolean doAddCard) {
        Cell cell = board.getCardCells().get(index);
        Card card = cardService.findCardById(cell.getCardOnTop());
        game.getAllPlayerStates().forEach(playerState -> {
            List<Integer> workerList = playerState.getWorkerList();
            if (workerList.contains(index)) {
                forge(game.getForgingPlayers(), playerState, card);
                mine(playerState, card);
                workerList.set(workerList.indexOf(index), -index);
            }
        });

        if (doAddCard) {
            cell.addToTop(special2normal.get(effect));
        }
    }

    public void checkIfHelpAction(Game game, ClientData clientData)
        throws InvocationTargetException, NoSuchMethodException,
        IllegalAccessException {
        Card actionableCard = cardService
            .findCardById(game.getBoard().getCellsTopCard().get(clientData.getPlayerAction()));

        if (actionableCard.getCardType().isHelp()) {
            game.getHelpTurnsOrder().add(getPlayerIndex(game, clientData));
        }
    }

    public List<Integer> processHelpTurnOrder(Game game, ClientData data) {
        // Check if there are actionable help cards on the board
        Board board = game.getBoard();
        List<Card> presentHelpCardsList = board.getCellsTopCard().stream().map(cardService::findCardById)
            .takeWhile(card -> card.getCardType().isHelp()).collect(Collectors.toList());

        if (presentHelpCardsList.size() == 0) {
            return new ArrayList<>();
        }

        List<Integer> helpTurnsOrder = game.getHelpTurnsOrder();
        List<Integer> turnsOrder = game.getTurnsOrder();
        IntStream.range(0, helpTurnsOrder.size()).forEach(i -> turnsOrder.add(i * 2 + 1, helpTurnsOrder.get(i)));

        if (turnsOrder.get(0).equals(game.getOrder().get(0))) {
            Collections.rotate(game.getOrder(), 1);
        }

        game.setActivePlayer(turnsOrder.remove(0));

        return turnsOrder;
    }

    public int defense(Game game) {
        Board board = game.getBoard();
        List<PlayerState> allPlayerStates = game.getAllPlayerStates();

        List<Card> presentDefenseCardsList = board.getCellsTopCard().stream().map(cardService::findCardById)
            .takeWhile(card -> card.getCardType().isDefense()).collect(Collectors.toList());

        if (presentDefenseCardsList.isEmpty()) {
            return 0;
        }

        List<Integer> defenseCardsPositions = presentDefenseCardsList.stream().map(Card::getPosition)
            .collect(Collectors.toList());
        List<Integer> occupiedDefenseCards = allPlayerStates.stream()
            .flatMap(playerState -> playerState.getWorkerList().stream()).distinct()
            .filter(defenseCardsPositions::contains).collect(Collectors.toList());

        presentDefenseCardsList.removeIf(card -> occupiedDefenseCards.contains(card.getPosition()));

        allPlayerStates.stream()
            .filter(playerState -> playerState.getWorkerList().stream().anyMatch(occupiedDefenseCards::contains))
            .forEach(playerState -> playerState.setMedal(playerState.getMedal() + 1));

        if (presentDefenseCardsList.isEmpty()) {
            return 0;
        }

        for (Card card : presentDefenseCardsList) {
            String effect = card.getEffect();

            if (effect.equals("*")) {
                game.setDoMine(false);
            } else if (effect.equals("?")) {
                allPlayerStates.forEach(playerState -> {
                    playerState.setGold(Math.min(playerState.getGold() - 2, 0));
                    playerState.setIron(playerState.getIron() + 2);
                });
            } else if (effect.length() == 3) {
                switch (effect.charAt(2)) {
                    case 'g':
                        allPlayerStates
                            .forEach(playerState -> playerState.setGold(Math.min(playerState.getGold() - 1, 0)));
                        break;
                    case 'i':
                        allPlayerStates
                            .forEach(playerState -> playerState.setIron(Math.min(playerState.getIron() - 1, 0)));
                        break;
                }
            } else {
                allPlayerStates.forEach(playerState -> playerState.setGold(Math.min(playerState.getGold() - 100, 0)));
            }
        }

        return 0;
    }

    public int drawCard(Game game) {
        Board board = game.getBoard();
        List<Integer> deck = board.getDeck();
        int timesDrawn = 0;
        List<Integer> positionsDrawn = new ArrayList<>();
        Integer p;

        while (timesDrawn < 3 && !deck.isEmpty()) {
            timesDrawn++;
            Integer cardId = deck.remove(0);
            Card card = cardService.findCardById(cardId);
            p = card.getPosition();

            if (!positionsDrawn.contains(p)) {
                positionsDrawn.add(p);
                board.getCellContent(p).add(0, card.getId());
                if (timesDrawn == 2)
                    break;
            }
        }

        return 0;
    }

    private int getPlayerIndex(Game game, ClientData data) throws NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {

        for (int i = 0; i < 3; i++) {
            Method getPlayer = gameClass.getMethod("getPlayer" + i);
            UserDwarf player = (UserDwarf) getPlayer.invoke(game);

            if (!(player == null)) {
                if (player.getUsername().equals(data.getCurrentUser())) {
                    return i;
                }
            }
        }

        return 0;
    }

    private PlayerState getIndexedPlayerState(Game game, int playerIndex)
        throws IllegalAccessException, InvocationTargetException,
        NoSuchMethodException {
        return (PlayerState) gameClass.getMethod("getPlayerState_" + playerIndex).invoke(game);
    }

    public void resourceRound(Game game)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalStateException {
        List<PlayerState> allPlayersStates = game.getAllPlayerStates();

        for (PlayerState playerState : allPlayersStates) {
            for (Integer workerPosition : playerState.getWorkerList()) {

                if (workerPosition.toString().matches("^[01245678]")) {

                    List<Integer> cards = game.getBoard().getCellsTopCard();

                    for (int j = 0; j <= cards.size(); j++) {

                        Card card = cardService.findCardById(j);
                        Integer cardPosition = card.getPosition();

                        if (cardPosition.equals(workerPosition)) {
                            mine(playerState, card);
                        }
                    }
                }
            }
        }
    }

    private void mine(PlayerState playerState, Card card) {
        if (card.getCardType().isResource()) {

            String effect = card.getEffect();
            String[] field = effect.split(",");

            for (int z = 0; z <= field.length; z++) {

                String campo = field[z];

                if (campo.contains("i")) {
                    int index = campo.indexOf("i");
                    String resource = campo.substring(0, index);
                    playerState.setIron(playerState.getIron() + Integer.parseInt(resource));

                } else if (campo.contains("g")) {
                    int index = campo.indexOf("g");
                    String resource = campo.substring(0, index);
                    playerState.setGold(playerState.getGold() + Integer.parseInt(resource));
                } else {
                    int index = campo.indexOf("s");
                    String resource = campo.substring(0, index);
                    playerState.setSteel(playerState.getSteel() + Integer.parseInt(resource));
                }
            }
        }
    }

    public List<Integer> timeToForge(Game game)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
        IllegalStateException {
        List<PlayerState> playerStates = game.getAllPlayerStates();
        List<Integer> forgingPlayers = game.getForgingPlayers();

        for (PlayerState playerState : playerStates) {
            List<Integer> listWorkers = playerState.getWorkerList();

            for (Integer workerPosition : listWorkers) {
                if (workerPosition.toString().matches("^[012345678]")) {
                    List<Integer> cards = game.getBoard().getCellsTopCard();

                    for (Integer integer : cards) {
                        Card card = cardService.findCardById(integer);
                        Integer cardPosition = card.getPosition();

                        if (cardPosition.equals(workerPosition)) {
                            forge(forgingPlayers, playerState, card);
                        }
                    }
                }
            }
        }

        return forgingPlayers;
    }

    private void forge(List<Integer> forgingPlayers, PlayerState playerState, Card card) {

        if (card.getCardType().isForging()) {
            String effect = card.getEffect();
            String[] field = effect.split(",");
            int fLength = field.length;

            AtomicReference<Integer> ironRequirement = new AtomicReference<>(0);
            AtomicReference<Integer> goldRequirement = new AtomicReference<>(0);
            AtomicReference<Integer> steelRequirement = new AtomicReference<>(0);
            AtomicReference<Integer> objectReward = new AtomicReference<>(0);

            Arrays.stream(field).forEach(f -> {
                int newValue = Integer.parseInt(f.substring(1, fLength - 1));
                switch (f.charAt(fLength - 1)) {
                    case 'i':
                        ironRequirement.set(newValue);
                        break;
                    case 'g':
                        goldRequirement.set(newValue);
                        break;
                    case 's':
                        steelRequirement.set(newValue);
                        break;
                    case 'o':
                        objectReward.set(newValue);
                        break;
                }
            });

            if ((playerState.getIron() - ironRequirement.get()) >= 0
                && (playerState.getGold() - goldRequirement.get()) >= 0
                && (playerState.getSteel() - steelRequirement.get()) >= 0) {
                playerState.setObject(playerState.getObject() + objectReward.get());
            }

            forgingPlayers.add(playerState.getId());
        }
    }

    public void fin(Game game) {

        game.setRound(game.getRound() + 1);

        List<PlayerState> playerStates = game.getAllPlayerStates();


        for (PlayerState playerState : playerStates) {

            playerState.setWorker0(12);
            playerState.setWorker1(12);
            playerState.setWorker2(12);
            playerState.setWorker3(12);

        }

        Map<String, List<Integer>> playersResources = new HashMap<>();

        List<UserDwarf> allPlayersInGame = game.getAllPlayersInGame();

        for (int i = 0; i < allPlayersInGame.size(); i++) {
            playersResources.put(allPlayersInGame.get(i).getUsername(), playerStates.get(i).getResourcesList());
        }

        String winner = manageScore(playersResources);

        game.setWinner(winner);


    }


    private int compareResources(List<Integer> list1, List<Integer> list2) {

        int compareResult = IntStream.range(0, list1.size()).map(i -> Integer.signum(list1.get(i) - list2.get(i)))
            .reduce(Integer::sum).getAsInt();

        return Integer.compare(compareResult, 0);
    }


    private String manageScore(Map<String, List<Integer>> playersResources) {
        List<String> keys = new ArrayList<>(playersResources.keySet());
        List<List<Integer>> values = keys.stream().map(playersResources::get).collect(Collectors.toList());

        List<List<Integer>> vipResources = values.stream().map(l -> l.subList(1, 4)).collect(Collectors.toList());

        /*
         *   Creating comparison list consisting of players consisting of their score on winning resources
         */
        List<Integer> comparisonList_0 =
            IntStream.range(0, keys.size()).mapToObj(i -> {
                    List<Integer> row = new ArrayList<>();
                    switch (i) {
                        case 0:
                            row.addAll(List.of(0, compareResources(vipResources.get(0), vipResources.get(1)),
                                compareResources(vipResources.get(0), vipResources.get(2))));
                            break;
                        case 1:
                            row.addAll(List.of(compareResources(vipResources.get(1), vipResources.get(0)), 0,
                                compareResources(vipResources.get(1), vipResources.get(2))));
                            break;
                        case 2:
                            row.addAll(List.of(compareResources(vipResources.get(2), vipResources.get(0)),
                                compareResources(vipResources.get(2), vipResources.get(1)), 0));
                            break;
                    }
                    return row;
                }
            ).map(x -> x.stream().reduce(Integer::sum).get()).collect(Collectors.toList());

        Integer winnerIndex = comparisonList_0.stream().anyMatch(x -> x == 2) ? comparisonList_0.indexOf(2) : -1;

        if (winnerIndex != -1) {
            return keys.get(winnerIndex);
        }

        //Draw 1
        List<Integer> comparisonList_1 = values.stream().map(l -> l.get(4)).collect(Collectors.toList());

        Integer maxMedals = comparisonList_1.stream().max(Comparator.naturalOrder()).get();

        if (comparisonList_1.stream().filter(l -> l.equals(maxMedals)).count() == 1) {
            return keys.get(comparisonList_0.indexOf(maxMedals));
        }

        //Draw 2
        List<Integer> comparisonList_2 = values.stream().map(l -> l.get(0)).collect(Collectors.toList());

        Integer maxIron = comparisonList_2.stream().max(Comparator.naturalOrder()).get();

        if (comparisonList_2.stream().filter(l -> l.equals(maxIron)).count() == 1) {
            return keys.get(comparisonList_0.indexOf(maxIron));
        }

        //Draw 3
        List<Integer> comparisonList_3 = values.stream().map(l -> l.get(3)).collect(Collectors.toList());

        Integer maxObjects = comparisonList_3.stream().max(Comparator.naturalOrder()).get();

        if (comparisonList_3.stream().filter(l -> l.equals(maxObjects)).count() == 1) {
            return keys.get(comparisonList_0.indexOf(maxMedals));
        }

        return "complete draw";
    }

}
