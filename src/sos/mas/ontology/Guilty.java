package sos.mas.ontology;

import jade.content.Predicate;

import java.io.Serializable;

public class Guilty implements Serializable, Predicate {
    private static final long serialVersionUID = 1L;

    private Prisoner prisoner;
    private Boolean confession;

    public Guilty() {}

    public Guilty(Prisoner prisoner, boolean confession) {
        this.prisoner = prisoner;
        this.confession = confession;
    }

    public Prisoner getPrisoner() {
        return prisoner;
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
    }

    public void setConfession(Boolean confession) {
        this.confession = confession;
    }

    public Boolean getConfession() {
        return confession;
    }
}
