package org.springframework.samples.petclinic.testBoard;

import org.junit.jupiter.api.BeforeEach;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.board.BoardController;
import org.springframework.samples.petclinic.card.Card;
import org.springframework.samples.petclinic.card.CardService;
import org.springframework.samples.petclinic.card.CardType;
import org.springframework.samples.petclinic.configuration.SecurityConfiguration;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(controllers = BoardController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfigurer.class), excludeAutoConfiguration = SecurityConfiguration.class)
public class testBoardController {

    private static final int TEST_CARD_ID = 1;

	@MockBean
	private CardService cardService;

	@Autowired
	private MockMvc mockMvc;

	private Card mockCard;

    @BeforeEach
    void setup(){
        mockCard = new Card();
        mockCard.setCardImage("resources/cards/Alloy Steel 1-1.png");
        mockCard.setCardType(CardType.EXTRACCION_RECURSOS);
        mockCard.setDescription("Return 3 iron to the supply then take 2 steel.");
        mockCard.setEffect("-3i,+2s");
        mockCard.setPosition(0);
        mockCard.setTitle("Alloy Steel");
        given(this.cardService.findCardById(TEST_CARD_ID)).willReturn(mockCard);
    }

}
