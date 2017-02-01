package com.zuehlke.jasschallenge.client.game.strategy;

import com.zuehlke.jasschallenge.Application;
import com.zuehlke.jasschallenge.client.game.*;
import com.zuehlke.jasschallenge.game.Trumpf;
import com.zuehlke.jasschallenge.game.cards.Card;
import com.zuehlke.jasschallenge.game.cards.CardValue;
import com.zuehlke.jasschallenge.game.cards.Color;
import com.zuehlke.jasschallenge.game.mode.Mode;

import java.util.*;
import java.util.stream.Collectors;

public class FloJassStrategy implements JassStrategy {
    private List<Card> playedCardsInGame;
    private static final List<Card> ALL_CARDS = Arrays.asList(Card.values());

    @Override
    public Mode chooseTrumpf(Set<Card> availableCards, GameSession session, boolean isGschobe) {
        if (topDown(availableCards, isGschobe)) {
            return Mode.topDown();
        }
        if (bottomUp(availableCards, isGschobe)) {
            return Mode.bottomUp();
        }
        return trump(availableCards, isGschobe);
    }

    private Mode trump(Set<Card> availableCards, boolean isGschobe) {
        Map<Color, List<Card>> cardsByColor = new HashMap<>();
        Map<Color, Boolean> jackNineByColor= new HashMap<>();
        Map<Color, Boolean> nineAceByColor = new HashMap<>();
        for (Color color : Color.values()) {
            List<Card> cardsByThisColor = availableCards.stream().filter(c -> c.getColor() == color).collect(Collectors.toList());
            cardsByColor.put(color, cardsByThisColor);
            jackNineByColor.put(color, cardsByThisColor.stream().filter(c -> c.getValue() == CardValue.JACK || c.getValue() == CardValue.NINE).collect(Collectors.toList()).size() == 2);
            nineAceByColor.put(color, cardsByThisColor.stream().filter(c -> c.getValue() == CardValue.NINE || c.getValue() == CardValue.ACE).collect(Collectors.toList()).size() == 2);
        }


        //Check multiple JackNineThird:
        if (jackNineByColor.values().stream().findAny().isPresent()) {
            Mode result = Mode.shift();
            //Must at least have three of the color!
            int max = 2;
            for (Color color : Color.values()) {
                if (jackNineByColor.get(color) && cardsByColor.get(color).size() > max) {
                    result = Mode.from(Trumpf.TRUMPF, color);
                    max = cardsByColor.get(color).size();
                }
            }
            if (result.getTrumpfName() == Trumpf.TRUMPF) {
                return result;
            }
        }
        if (nineAceByColor.values().stream().findAny().isPresent()) {
            Mode result = Mode.shift();
            //Must at least have four of the color!
            int max = 3;
            for (Color color : Color.values()) {
                if (nineAceByColor.get(color) && cardsByColor.get(color).size() > max) {
                    result = Mode.from(Trumpf.TRUMPF, color);
                    max = cardsByColor.get(color).size();
                }
            }
            if (result.getTrumpfName() == Trumpf.TRUMPF) {
                return result;
            }
        }
        //Must at least have five of the color!
        Mode result = Mode.shift();
        int max = 4;
        for (Color color : Color.values()) {
            if (cardsByColor.get(color).size() > max) {
                result = Mode.from(Trumpf.TRUMPF, color);
                max = cardsByColor.get(color).size();
            }
        }
        if (result.getTrumpfName() == Trumpf.TRUMPF) {
            return result;
        }
        long colorsWithCards = cardsByColor.values().stream().filter(list -> !list.isEmpty()).count();
        if (!isGschobe && colorsWithCards == 4) {
            //Shift if all colors present
            return Mode.shift();
        }
        //Hail mary!
        max = 0;
        for (Color color : Color.values()) {
            if (cardsByColor.get(color).size() > max) {
                result = Mode.from(Trumpf.TRUMPF, color);
                max = cardsByColor.get(color).size();
            }
        }
        return result;
    }

    private boolean topDown(Set<Card> availableCards, boolean isGschobe) {
        List<Card> aces = availableCards.stream().filter(c -> c.getValue() == CardValue.ACE).collect(Collectors.toList());
        int fixedStich = aces.size();
        List<Card> matchingKings = new ArrayList<>();
        for (Card ace : aces) {
            matchingKings.addAll(availableCards.stream().filter(c -> c.getValue() == CardValue.KING && c.getColor() == ace.getColor()).collect(Collectors.toList()));
        }
        fixedStich += matchingKings.size();

        List<Card> matchingQueens = new ArrayList<>();
        for (Card king : matchingKings) {
            matchingQueens.addAll(availableCards.stream().filter(c -> c.getValue() == CardValue.QUEEN && c.getColor() == king.getColor()).collect(Collectors.toList()));
        }
        fixedStich += matchingQueens.size();

        List<Card> matchingJacks = new ArrayList<>();
        for (Card queen : matchingQueens) {
            matchingJacks.addAll(availableCards.stream().filter(c -> c.getValue() == CardValue.JACK && c.getColor() == queen.getColor()).collect(Collectors.toList()));
        }
        fixedStich += matchingJacks.size();

        return fixedStich >= 4 && (!isGschobe || aces.size() >= 3);
    }

    private boolean bottomUp(Set<Card> availableCards, boolean isGschobe) {
        List<Card> sixes = availableCards.stream().filter(c -> c.getValue() == CardValue.SIX).collect(Collectors.toList());
        int fixedStich = sixes.size();
        List<Card> matchingSevens = new ArrayList<>();
        for (Card six : sixes) {
            matchingSevens.addAll(availableCards.stream().filter(c -> c.getValue() == CardValue.SEVEN && c.getColor() == six.getColor()).collect(Collectors.toList()));
        }
        fixedStich += matchingSevens.size();

        List<Card> matchingEights = new ArrayList<>();
        for (Card seven : matchingSevens) {
            matchingEights.addAll(availableCards.stream().filter(c -> c.getValue() == CardValue.EIGHT && c.getColor() == seven.getColor()).collect(Collectors.toList()));
        }
        fixedStich += matchingEights.size();

        List<Card> matchingNines = new ArrayList<>();
        for (Card eight : matchingEights) {
            matchingNines.addAll(availableCards.stream().filter(c -> c.getValue() == CardValue.NINE && c.getColor() == eight.getColor()).collect(Collectors.toList()));
        }
        fixedStich += matchingNines.size();

        return fixedStich >= 4 && (!isGschobe || sixes.size() >= 3);
    }

    @Override
    public Card chooseCard(Set<Card> availableCards, GameSession session) {
        final Game currentGame = session.getCurrentGame();
        final Round round = currentGame.getCurrentRound();
        final Mode gameMode = round.getMode();

        List<Card> playableCards = availableCards.stream()
                .filter(card -> gameMode.canPlayCard(card, round.getPlayedCards(), round.getRoundColor(), availableCards)).collect(Collectors.toList());
        if (round.getPlayedCards().isEmpty()) {
            //I'm going first!
            return chooseBockOrLowestCardOfColorWithMostCards(playableCards, gameMode);
        }

        Player winner = round.getWinner();
        boolean myStich = winner != null && Application.BOT_NAME.equals(winner.getName());
        if (myStich) {
            return getLowestWithTrumpf(playableCards, gameMode);
        }

        //Check if I can stich w/o trumpf
        Card highestPlayedCard = round.getPlayedCards().stream().sorted(Comparator.comparingInt(o -> -o.getValue().getRank())).findFirst().get();
        Optional<Card> highestNonTrumpCardWhichStichs = playableCards.stream().
                filter(card -> card.getColor() != gameMode.getTrumpfColor() && card.getColor() == highestPlayedCard.getColor() && card.isHigherThan(highestPlayedCard)).
                sorted(Comparator.comparingInt(o -> - o.getValue().getRank())).findFirst();
        if (highestNonTrumpCardWhichStichs.isPresent())
            return highestNonTrumpCardWhichStichs.get();

        //Check if I can check w/ trumpf
        Optional<Card> highestPlayedTrumpfCard = round.getPlayedCards().stream().filter(card -> card.getColor() == gameMode.getTrumpfColor()).sorted(Comparator.comparingInt(o -> -o.getValue().getRank())).findFirst();
        Optional<Card> lowestTrumpfCardWhichStichs = playableCards.stream().
                filter(card -> card.getColor() == gameMode.getTrumpfColor() && (!highestPlayedTrumpfCard.isPresent() || card.isHigherTrumpfThan(highestPlayedTrumpfCard.get()))).
                sorted(Comparator.comparingInt(o -> o.getValue().getRank())).findFirst();
        if (lowestTrumpfCardWhichStichs.isPresent())
            return lowestTrumpfCardWhichStichs.get();

        return getLowestWithTrumpf(playableCards, gameMode);
    }

    private Card getLowestWithTrumpf(List<Card> playableCards, Mode gameMode) {
        Optional<Card> lowestNonTrumpCard = playableCards.stream().filter(card -> card.getColor() != gameMode.getTrumpfColor()).sorted(Comparator.comparingInt(o -> o.getValue().getRank())).findFirst();
        if (lowestNonTrumpCard.isPresent())
            return lowestNonTrumpCard.get();
        return playableCards.stream().filter(card -> card.getColor() == gameMode.getTrumpfColor()).sorted(Comparator.comparingInt(o -> o.getValue().getRank())).findFirst().orElseThrow(() -> new RuntimeException("There should always be a card to play"));
    }

    private Card chooseBockOrLowestCardOfColorWithMostCards(List<Card> playableCards, Mode gameMode) {
        List<Card> bocks = playableCards.stream().filter(c -> isBock(gameMode, c)).collect(Collectors.toList());
        //I have a bock
        if (!bocks.isEmpty()) {
            return bocks.get(0);
        }

        //Playing lowest card of color with most cards
        Card result = playableCards.get(0);
        List<Card> clubs = playableCards.stream().filter(c -> c.getColor() == Color.CLUBS).collect(Collectors.toList());
        List<Card> spades = playableCards.stream().filter(c -> c.getColor() == Color.SPADES).collect(Collectors.toList());
        List<Card> hearts = playableCards.stream().filter(c -> c.getColor() == Color.HEARTS).collect(Collectors.toList());
        List<Card> diamonds = playableCards.stream().filter(c -> c.getColor() == Color.DIAMONDS).collect(Collectors.toList());
        int max = 0;
        if (clubs.size() > max) {
            result = getLowest(clubs);
            max = clubs.size();
        }
        if (diamonds.size() > max) {
            result = getLowest(diamonds);
            max = clubs.size();
        }
        if (hearts.size() > max) {
            result = getLowest(hearts);
            max = clubs.size();
        }
        if (spades.size() > max) {
            result = getLowest(spades);
        }
        return result;
    }

    private Card getLowest(List<Card> cards) {
        Card result;
        cards.sort(Comparator.comparingInt(o -> o.getValue().getRank()));
        result = cards.get(0);
        return result;
    }

    private boolean isBock(Mode mode, Card c) {
        Trumpf trumpfName = mode.getTrumpfName();
        List<Card> playedCardsOfSameColor = playedCardsInGame.stream().filter(card -> card.getColor() == c.getColor()).collect(Collectors.toList());
        List<Card> missingCardsOfColor = ALL_CARDS.stream().filter(card -> card.getColor() == c.getColor() && !playedCardsOfSameColor.contains(card)).collect(Collectors.toList());
        if (trumpfName == Trumpf.OBEABE) {
            List<Card> higherMissingCards = missingCardsOfColor.stream().filter(card -> card.isHigherThan(c)).collect(Collectors.toList());
            return higherMissingCards.isEmpty();
        } else if (trumpfName == Trumpf.UNDEUFE) {
            if (c.getValue() == CardValue.SIX) {
                return  true;
            }
            List<Card> lowerMissingCards = missingCardsOfColor.stream().filter(card -> card.isLowerThan(c)).collect(Collectors.toList());
            return lowerMissingCards.isEmpty();
        }
        Color trumpfColor = mode.getTrumpfColor();
        if (c.getColor() == trumpfColor) {
            List<Card> higherMissingCards = missingCardsOfColor.stream().filter(card -> card.isHigherThan(c)).collect(Collectors.toList());
            return higherMissingCards.isEmpty();
        }

        long playedTrumpfs = playedCardsInGame.stream().filter(card -> card.getColor() == trumpfColor).count();
        if (playedTrumpfs < 9) {
            return false;
        }

        List<Card> higherMissingCards = missingCardsOfColor.stream().filter(card -> card.isHigherThan(c)).collect(Collectors.toList());
        return higherMissingCards.isEmpty();
    }

    @Override
    public void onMoveMade(Move move, GameSession session) {
        playedCardsInGame.add(move.getPlayedCard());
    }

    @Override
    public void onGameStarted(GameSession session) {
        playedCardsInGame = new ArrayList<>();
    }
}
