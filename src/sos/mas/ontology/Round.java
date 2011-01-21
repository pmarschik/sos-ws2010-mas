package sos.mas.ontology;

import jade.content.Predicate;

import java.io.Serializable;

public class Round implements Serializable, Predicate {
    private static final long serialVersionUID = 1L;

    private String id;
    private Game game;
    private boolean confession1;
    private boolean confession2;

    public Round() {}

    public Round(String id, Game game, boolean confession1, boolean confession2) {
        this.id = id;
        this.game = game;
        this.confession1 = confession1;
        this.confession2 = confession2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean getConfession1() {
        return confession1;
    }

    public void setConfession1(boolean confession1) {
        this.confession1 = confession1;
    }

    public boolean getConfession2() {
        return confession2;
    }

    public void setConfession2(boolean confession2) {
        this.confession2 = confession2;
    }
}
