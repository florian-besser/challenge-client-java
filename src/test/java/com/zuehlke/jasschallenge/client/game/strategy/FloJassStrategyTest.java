package com.zuehlke.jasschallenge.client.game.strategy;

import com.zuehlke.jasschallenge.Application;
import com.zuehlke.jasschallenge.client.game.*;
import com.zuehlke.jasschallenge.game.Trumpf;
import com.zuehlke.jasschallenge.game.cards.Card;
import com.zuehlke.jasschallenge.game.cards.Color;
import com.zuehlke.jasschallenge.game.mode.Mode;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FloJassStrategyTest {

    private FloJassStrategy sut;

    @Before
    public void setup() {
        sut = new FloJassStrategy();
    }

    @Test
    public void chooseTrumpfObeAbeClub() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_ACE);
        cards.add(Card.CLUB_KING);
        cards.add(Card.CLUB_QUEEN);
        cards.add(Card.CLUB_JACK);
        Mode mode = sut.chooseTrumpf(cards, null, false);
        assertEquals(Trumpf.OBEABE, mode.getTrumpfName());
    }

    @Test
    public void chooseTrumpfObeAbeGschobe() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_ACE);
        cards.add(Card.CLUB_KING);
        cards.add(Card.CLUB_QUEEN);
        cards.add(Card.CLUB_JACK);
        Mode mode = sut.chooseTrumpf(cards, null, true);
        assertEquals(Trumpf.TRUMPF, mode.getTrumpfName());
    }

    @Test
    public void chooseTrumpfObeAbeGschobeWithAces() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_ACE);
        cards.add(Card.CLUB_KING);
        cards.add(Card.CLUB_QUEEN);
        cards.add(Card.CLUB_JACK);
        cards.add(Card.DIAMOND_ACE);
        cards.add(Card.HEART_ACE);
        Mode mode = sut.chooseTrumpf(cards, null, true);
        assertEquals(Trumpf.OBEABE, mode.getTrumpfName());
    }

    @Test
    public void chooseTrumpfUndeUfeClub() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_SIX);
        cards.add(Card.CLUB_SEVEN);
        cards.add(Card.CLUB_EIGHT);
        cards.add(Card.CLUB_NINE);
        Mode mode = sut.chooseTrumpf(cards, null, false);
        assertEquals(Trumpf.UNDEUFE, mode.getTrumpfName());
    }

    @Test
    public void chooseTrumpfUndeUfeGschobe() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_SIX);
        cards.add(Card.CLUB_SEVEN);
        cards.add(Card.CLUB_EIGHT);
        cards.add(Card.CLUB_NINE);
        Mode mode = sut.chooseTrumpf(cards, null, true);
        assertEquals(Trumpf.TRUMPF, mode.getTrumpfName());
    }

    @Test
    public void chooseTrumpfUndeUfeGschobeWithAces() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_SIX);
        cards.add(Card.CLUB_SEVEN);
        cards.add(Card.CLUB_EIGHT);
        cards.add(Card.CLUB_NINE);
        cards.add(Card.DIAMOND_SIX);
        cards.add(Card.HEART_SIX);
        Mode mode = sut.chooseTrumpf(cards, null, true);
        assertEquals(Trumpf.UNDEUFE, mode.getTrumpfName());
    }

    @Test
    public void chooseTrumpfBuurNällDritt() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_JACK);
        cards.add(Card.CLUB_NINE);
        cards.add(Card.CLUB_EIGHT);
        Mode mode = sut.chooseTrumpf(cards, null, true);
        assertEquals(Color.CLUBS, mode.getTrumpfColor());
    }

    @Test
    public void chooseTrumpfNällDritt() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_NINE);
        cards.add(Card.CLUB_EIGHT);
        cards.add(Card.HEART_SIX);
        cards.add(Card.DIAMOND_SIX);
        cards.add(Card.SPADE_JACK);

        Mode mode = sut.chooseTrumpf(cards, null, false);
        assertEquals(Trumpf.SCHIEBE, mode.getTrumpfName());
    }

    @Test
    public void chooseTrumpfNällAssViert() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_NINE);
        cards.add(Card.CLUB_ACE);
        cards.add(Card.CLUB_EIGHT);
        cards.add(Card.CLUB_SEVEN);

        Mode mode = sut.chooseTrumpf(cards, null, false);
        assertEquals(Color.CLUBS, mode.getTrumpfColor());
    }

    @Test
    public void chooseTrumpfFoiv() throws Exception {
        Set<Card> cards = new HashSet<>();
        cards.add(Card.CLUB_TEN);
        cards.add(Card.CLUB_SIX);
        cards.add(Card.CLUB_EIGHT);
        cards.add(Card.CLUB_SEVEN);
        cards.add(Card.CLUB_KING);

        Mode mode = sut.chooseTrumpf(cards, null, false);
        assertEquals(Color.CLUBS, mode.getTrumpfColor());
    }

    @Test
    public void chooseCardFirstTopDownAce() throws Exception {
        GameSession session = mock(GameSession.class);
        Game game = mock(Game.class);
        Mode mode = Mode.topDown();
        Round round = Round.createRound(mode, 1, null);
        when(session.getCurrentGame()).thenReturn(game);
        when(game.getCurrentRound()).thenReturn(round);

        HashSet<Card> availableCards = new HashSet<>();
        availableCards.add(Card.CLUB_ACE);
        availableCards.add(Card.HEART_EIGHT);
        availableCards.add(Card.CLUB_TEN);

        sut.onGameStarted(session);
        Card chosenCard = sut.chooseCard(availableCards, session);

        assertEquals(Card.CLUB_ACE, chosenCard);
    }

    @Test
    public void chooseCardNotFirstMyStich() throws Exception {
        GameSession session = mock(GameSession.class);
        Game game = mock(Game.class);
        Mode mode = Mode.topDown();
        Player firstPlayer = new Player(Application.BOT_NAME);
        Player secondPlayer = new Player("foo");
        ArrayList<Player> playersInInitialPlayingOrder = new ArrayList<>();
        playersInInitialPlayingOrder.add(firstPlayer);
        playersInInitialPlayingOrder.add(secondPlayer);
        Round round = Round.createRound(mode, 1, PlayingOrder.createOrder(playersInInitialPlayingOrder));
        when(session.getCurrentGame()).thenReturn(game);
        when(game.getCurrentRound()).thenReturn(round);
        round.makeMove(new Move(firstPlayer, Card.CLUB_ACE));
        round.makeMove(new Move(secondPlayer, Card.CLUB_EIGHT));

        HashSet<Card> availableCards = new HashSet<>();
        availableCards.add(Card.CLUB_JACK);
        availableCards.add(Card.HEART_EIGHT);
        availableCards.add(Card.CLUB_TEN);

        sut.onGameStarted(session);
        Card chosenCard = sut.chooseCard(availableCards, session);

        assertEquals(Card.CLUB_TEN, chosenCard);
    }

    @Test
    public void chooseCardNotFirstMyStichOnlyTrumpfLeft() throws Exception {
        GameSession session = mock(GameSession.class);
        Game game = mock(Game.class);
        Mode mode = Mode.from(Trumpf.TRUMPF, Color.CLUBS);
        Player firstPlayer = new Player(Application.BOT_NAME);
        Player secondPlayer = new Player("foo");
        ArrayList<Player> playersInInitialPlayingOrder = new ArrayList<>();
        playersInInitialPlayingOrder.add(firstPlayer);
        playersInInitialPlayingOrder.add(secondPlayer);
        Round round = Round.createRound(mode, 1, PlayingOrder.createOrder(playersInInitialPlayingOrder));
        when(session.getCurrentGame()).thenReturn(game);
        when(game.getCurrentRound()).thenReturn(round);
        round.makeMove(new Move(firstPlayer, Card.CLUB_ACE));
        round.makeMove(new Move(secondPlayer, Card.CLUB_EIGHT));

        HashSet<Card> availableCards = new HashSet<>();
        availableCards.add(Card.CLUB_JACK);
        availableCards.add(Card.CLUB_TEN);

        sut.onGameStarted(session);
        Card chosenCard = sut.chooseCard(availableCards, session);

        assertEquals(Card.CLUB_TEN, chosenCard);
    }

    @Test
    public void chooseCardNotFirstNotMyStich() throws Exception {
        GameSession session = mock(GameSession.class);
        Game game = mock(Game.class);
        Mode mode = Mode.topDown();
        Player firstPlayer = new Player(Application.BOT_NAME);
        Player secondPlayer = new Player("foo");
        ArrayList<Player> playersInInitialPlayingOrder = new ArrayList<>();
        playersInInitialPlayingOrder.add(firstPlayer);
        playersInInitialPlayingOrder.add(secondPlayer);
        Round round = Round.createRound(mode, 1, PlayingOrder.createOrder(playersInInitialPlayingOrder));
        when(session.getCurrentGame()).thenReturn(game);
        when(game.getCurrentRound()).thenReturn(round);
        round.makeMove(new Move(firstPlayer, Card.CLUB_EIGHT));
        round.makeMove(new Move(secondPlayer, Card.CLUB_ACE));

        HashSet<Card> availableCards = new HashSet<>();
        availableCards.add(Card.CLUB_JACK);
        availableCards.add(Card.HEART_EIGHT);
        availableCards.add(Card.CLUB_TEN);

        sut.onGameStarted(session);
        Card chosenCard = sut.chooseCard(availableCards, session);

        assertEquals(Card.CLUB_TEN, chosenCard);
    }

    @Test
    public void chooseCardNotFirstNotMyStichOnlyTrumpfLeft() throws Exception {
        GameSession session = mock(GameSession.class);
        Game game = mock(Game.class);
        Mode mode = Mode.from(Trumpf.TRUMPF, Color.CLUBS);
        Player firstPlayer = new Player(Application.BOT_NAME);
        Player secondPlayer = new Player("foo");
        ArrayList<Player> playersInInitialPlayingOrder = new ArrayList<>();
        playersInInitialPlayingOrder.add(firstPlayer);
        playersInInitialPlayingOrder.add(secondPlayer);
        Round round = Round.createRound(mode, 1, PlayingOrder.createOrder(playersInInitialPlayingOrder));
        when(session.getCurrentGame()).thenReturn(game);
        when(game.getCurrentRound()).thenReturn(round);
        round.makeMove(new Move(firstPlayer, Card.DIAMOND_EIGHT));
        round.makeMove(new Move(secondPlayer, Card.DIAMOND_ACE));

        HashSet<Card> availableCards = new HashSet<>();
        availableCards.add(Card.CLUB_JACK);
        availableCards.add(Card.CLUB_TEN);

        sut.onGameStarted(session);
        Card chosenCard = sut.chooseCard(availableCards, session);

        assertEquals(Card.CLUB_TEN, chosenCard);
    }

}