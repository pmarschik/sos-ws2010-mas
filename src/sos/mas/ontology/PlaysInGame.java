package sos.mas.ontology;

import jade.content.Predicate;

import java.io.Serializable;

public class PlaysInGame implements Serializable, Predicate {
    private Prisoner prisoner;
    private Game game;

    public PlaysInGame() {}

    public PlaysInGame(Prisoner prisoner, Game game) {
        this.prisoner = prisoner;
        this.game = game;
    }

    public Prisoner getPrisoner() {
        return prisoner;
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
