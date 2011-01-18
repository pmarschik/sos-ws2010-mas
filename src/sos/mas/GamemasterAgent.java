import jade.core.Agent;

public class GamemasterAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("Starting gamemaster agent \"" + getAID().getName());
    }

    @Override
    protected void takeDown() {
        System.out.println("Stopping gamemaster agent \"" + getAID().getName());
    }
}
