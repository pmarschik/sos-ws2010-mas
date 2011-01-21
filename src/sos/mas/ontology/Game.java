package sos.mas.ontology;

import jade.content.Concept;

import java.io.Serializable;

public class Game implements Serializable, Concept {
    private Integer iterations;

    public Game() {}

    public Game(Integer iterations) {
        this.iterations = iterations;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }
}
