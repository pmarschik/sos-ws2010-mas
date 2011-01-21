package sos.mas.ontology;

import jade.content.Concept;
import jade.core.AID;

import java.io.Serializable;

public class Prisoner implements Serializable, Concept {
    private AID agent;

    public Prisoner() {}

    public Prisoner(AID agent) {
        this.agent = agent;
    }

    public AID getAgent() {
        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }
}
