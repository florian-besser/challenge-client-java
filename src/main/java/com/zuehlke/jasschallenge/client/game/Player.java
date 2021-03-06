package com.zuehlke.jasschallenge.client.game;

import com.zuehlke.jasschallenge.game.cards.Card;
import com.zuehlke.jasschallenge.game.mode.Mode;
import com.zuehlke.jasschallenge.client.game.strategy.JassStrategy;
import com.zuehlke.jasschallenge.client.game.strategy.FloJassStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;

public class Player {

    private static final Logger logger = LoggerFactory.getLogger(Player.class);

    private String id;
    private final String name;
    private int seatId;
    private final Set<Card> cards;
    private final JassStrategy currentJassStrategy;

    public Player(String id, String name, int seatId) {
        this(name);
        this.id = id;
        this.seatId = seatId;
    }

    public Player(String name) {
        this(name, new FloJassStrategy());
    }

    public Player(String name, JassStrategy strategy) {
        this.name = name;
        this.cards = EnumSet.noneOf(Card.class);
        this.currentJassStrategy = strategy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getName() {
        return name;
    }

    public Set<Card> getCards() {
        return cards;
    }

    public void setCards(Set<Card> cards) {
        this.cards.clear();
        this.cards.addAll(cards);
    }

    public Move makeMove(GameSession session) {
        if(cards.size() == 0) throw new RuntimeException("Cannot play a card without cards in deck");
        final Card cardToPlay = chooseCardWithFallback(session);
        cards.remove(cardToPlay);
        return new Move(this, cardToPlay);
    }

    private Card chooseCardWithFallback(GameSession session) {
        final Card cardToPlay = currentJassStrategy.chooseCard(cards, session);
        final boolean cardIsInvalid = !session.getCurrentRound().getMode().canPlayCard(
                cardToPlay,
                session.getCurrentRound().getPlayedCards(),
                session.getCurrentRound().getRoundColor(),
                cards);
        if(cardIsInvalid) {
            logger.error("Your strategy tried to play an invalid card. Playing random card instead!");
            return new FloJassStrategy().chooseCard(cards, session);
        }
        return cardToPlay;
    }

    public Mode chooseTrumpf(GameSession session, boolean shifted) {
        return currentJassStrategy.chooseTrumpf(cards, session, shifted);
    }

    public void onMoveMade(Move move, GameSession session) {
        currentJassStrategy.onMoveMade(move, session);
    }

    public void onSessionFinished() {
        currentJassStrategy.onSessionFinished();
    }

    public void onGameFinished() {
        currentJassStrategy.onGameFinished();
    }

    public void onGameStarted(GameSession session) {
        currentJassStrategy.onGameStarted(session);
    }

    public void onSessionStarted(GameSession session) {
        currentJassStrategy.onSessionStarted(session);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (id != null ? !id.equals(player.id) : player.id != null) return false;
        return name != null ? name.equals(player.name) : player.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cards=" + cards +
                ", currentJassStrategy=" + currentJassStrategy +
                '}';
    }
}
