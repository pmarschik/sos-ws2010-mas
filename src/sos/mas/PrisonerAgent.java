package sos.mas;

import jade.core.Agent;

/**
 * @author patrick
 */
public class PrisonerAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Starting prisoner agent \"" + getAID().getName());
    }

    @Override
    protected void takeDown() {
        System.out.println("Stopping prisoner agent \"" + getAID().getName());
    }
}
