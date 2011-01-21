package sos.mas;


import jade.core.AID;
import sos.mas.ontology.Game;
import sos.mas.ontology.PlaysInGame;
import sos.mas.ontology.Prisoner;
import sos.mas.ontology.Round;

import java.util.Stack;

public class GameHistory {
    private Game game;

    private PlaysInGame plays1;
    private PlaysInGame plays2;

    private Stack<Round> rounds = new Stack<Round>();

    public GameHistory(AID pris1, AID pris2, int iterations) {
        Prisoner p1 = new Prisoner(pris1);
        Prisoner p2 = new Prisoner(pris2);
        game = new Game(iterations);
        plays1 = new PlaysInGame(p1, game);
        plays2 = new PlaysInGame(p2, game);
    }

    public int getIterations() {
        return game.getIterations();
    }

    public Prisoner getPrisoner1() {
        return plays1.getPrisoner();
    }

    public Prisoner getPrisoner2() {
        return plays2.getPrisoner();
    }

    public Round newRound(String id) {
        Round round = new Round(id, game, false, false);
        rounds.add(round);

        return round;
    }

    public Round getPreviousRound() {
        if (rounds.size() - 1 == 0) return null;

        return rounds.get(rounds.size() - 1);
    }

    public void pushRound(Round round) {
        rounds.push(round);
    }

    public int[] calculatePoints() {
        int points1 = 0;
        int points2 = 0;

        final int BOTH_CONFESSED = 3;
        final int ONE_CONFESSED = 5;
        final int NONE_CONFESSED = 1;

        for (Round round : rounds) {
            if (round.getConfession1() && round.getConfession2()) {
                points1 += BOTH_CONFESSED;
                points2 += BOTH_CONFESSED;
            } else if (round.getConfession1() && !round.getConfession2()) {
                points1 += ONE_CONFESSED;
            } else if (!round.getConfession1() && round.getConfession2()) {
                points2 += ONE_CONFESSED;
            } else {
                points1 += NONE_CONFESSED;
                points2 += NONE_CONFESSED;
            }
        }

        return new int[]{points1, points2};
    }
}
