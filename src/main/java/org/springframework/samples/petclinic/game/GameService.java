package org.springframework.samples.petclinic.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.board.Board;
import org.springframework.samples.petclinic.playerState.PlayerState;
import org.springframework.samples.petclinic.statistics.Statistics;
import org.springframework.samples.petclinic.statistics.StatisticsRepository;
import org.springframework.samples.petclinic.statistics.StatisticsService;
import org.springframework.samples.petclinic.userDwarf.UserDwarf;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.*;

@Service
public class GameService {
    
    private static Integer num = 0;
    
    @Autowired
    private static GameStorage gameStorage;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private StatisticsRepository statisticsRepository;
    
    @Autowired
    private StatisticsService statisticsService;
    
    public Game createGame(UserDwarf player0) {
        Game game = new Game();
        game.setId(num);
        num++;
        //Player creating the game
        game.setPlayer0(player0);
        game.setBoard(new Board(game.getId()));
        List<Integer> order = new ArrayList<>(List.of(0, 1, 2));
        Collections.shuffle(order);
        game.setOrder(order);
        game.setPhase(Phase.INICIO);
        game.setGameStatus(GameStatus.NEW);
        game.setNumberOfPlayers(1);
        GameStorage.getInstance().setGame(game);
        return game;
    }
    
    public Game connectToGame(UserDwarf additionalPlayer, Integer gameId) {
        if (!GameStorage.getInstance().getGames().containsKey(gameId)) {
            throw new InvalidParameterException("Game with provided id does not exist");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);
        if (game.getPlayer2() != null) {
            throw new InvalidParameterException("Game already full");
        } else if (game.getPlayer1() == null) {
            game.setPlayer1(additionalPlayer);
            game.setGameStatus(GameStatus.IN_PROGRESS);
            game.setNumberOfPlayers(game.getNumberOfPlayers()+1);
            GameStorage.getInstance().setGame(game);
        } else if (game.getPlayer2() == null) {
            game.setPlayer2(additionalPlayer);
            game.setGameStatus(GameStatus.IN_PROGRESS);
            game.setNumberOfPlayers(game.getNumberOfPlayers()+1);
            GameStorage.getInstance().setGame(game);
        }
        return game;
    }
    
    public Iterable<Game> findAll() {
        Map<Integer, Game> map = GameStorage.getInstance().getGames();
        return map.values();
    }
    
    public Board getBoard(Integer gameId) {
        Game game = GameStorage.getInstance().getGame(gameId);
        return game.getBoard();
    }
    
    // Guarda la partida en la bbdd y la elimina de la memoria de java
    public void finishGame(Integer gameId) {
        Game game = GameStorage.getInstance().getGame(gameId);

        List<UserDwarf> allPlayersInGame = game.getAllPlayersInGame();
        List<PlayerState> allPlayerStates= game.getAllPlayerStates();
    
        for (int i=0; i< allPlayersInGame.size(); i++) {
            String playerUsername = allPlayersInGame.get(i).getUsername();
            Statistics statisticPlayer = statisticsService.findStatisticsByUsername(playerUsername);
            statisticsService.updateStatistics(allPlayerStates.get(i), statisticPlayer, playerUsername.equals(game.getWinner()));
        }
        
        GameStorage.getInstance().getGames().remove(gameId);
    }
    
    // Método que saca al user de la partida (se activa con un botón) y si no quedan jugadores cierra la partida
    public void surrender(Integer gameId, UserDwarf player) {
        Game game = GameStorage.getInstance().getGame(gameId);
        
        if (player.equals(game.getPlayer0())) {
            game.setPlayer0(null);
        } else if (player.equals(game.getPlayer1())) {
            game.setPlayer1(null);
            Statistics statisticPlayer = this.statisticsRepository.findByUsername(player.getUsername());
            statisticPlayer.setGamesPlayed(statisticPlayer.getGamesPlayed() + 1);
            
        } else if (player.equals(game.getPlayer2())) {
            game.setPlayer2(null);
            Statistics statisticPlayer = this.statisticsRepository.findByUsername(player.getUsername());
            statisticPlayer.setGamesPlayed(statisticPlayer.getGamesPlayed() + 1);
        }
        if (Optional.ofNullable(game.getPlayer0()).isEmpty() && Optional.ofNullable(game.getPlayer1()).isEmpty() && Optional.ofNullable(game.getPlayer2()).isEmpty()) {
            finishGame(gameId);
        }
    }
    
}
