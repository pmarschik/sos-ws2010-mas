package sos.mas;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.SubscriptionInitiator;

import java.util.Iterator;

public class PrisonerAgent extends Agent {

    private abstract class StrategyBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            AchieveREResponder p = (AchieveREResponder) parent;

            ACLMessage query = (ACLMessage) getDataStore().get(p.REQUEST_KEY);
            ACLMessage response = (ACLMessage) getDataStore().get(p.RESPONSE_KEY);

            ACLMessage inform = prepareResultNotification(query, response);

            out("replying with %s", inform.getContent());

            getDataStore().put(p.RESULT_NOTIFICATION_KEY, inform);
        }

        protected abstract ACLMessage prepareResultNotification(ACLMessage query, ACLMessage response);
    }

    public class ConstantStrategy extends StrategyBehaviour {

        private boolean comply;

        public ConstantStrategy(boolean comply) { this.comply = comply; }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage query, ACLMessage response) {
            ACLMessage inform = query.createReply();
            inform.setPerformative(ACLMessage.INFORM);

            // TODO replace with FIPA SL
            inform.setContent(String.format("(%s)", comply));

            return inform;
        }
    }

    public class RandomStrategy extends StrategyBehaviour {

        private double chanceForComply;

        public RandomStrategy() { this(0.2); }

        public RandomStrategy(double chanceForComply) { this.chanceForComply = chanceForComply; }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage query, ACLMessage response) {
            ACLMessage inform = query.createReply();
            inform.setPerformative(ACLMessage.INFORM);

            boolean comply = Math.random() > (1 - chanceForComply);

            // TODO replace with FIPA SL
            inform.setContent(String.format("(%s)", comply));

            return inform;
        }
    }

    private GameHistory history = new GameHistory();

    private void out(String text, Object... args) {
        System.out.print("[" + getLocalName() + "] ");
        System.out.println(String.format(text, args));
    }

    @Override
    protected void setup() {
        try {
            out("Starting");

            AID gamemasterAID = getGamemasterService();

            ParallelBehaviour behaviour = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);
            behaviour.addSubBehaviour(createQueryProtocol());
            behaviour.addSubBehaviour(createSubscriptionProtocol(gamemasterAID));

            addBehaviour(behaviour);
        } catch (FIPAException e) {
            e.printStackTrace();

            doDelete();
        }
    }

    private AchieveREResponder createQueryProtocol() {
        MessageTemplate queryMessageTemplate = MessageTemplate.and(MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF)), MessageTemplate.MatchContent("(guilty)"));

        AchieveREResponder arer = new AchieveREResponder(this, queryMessageTemplate) {
            @Override
            protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }
        };

        arer.registerPrepareResultNotification(new RandomStrategy(0.5));

        return arer;
    }

    private SubscriptionInitiator createSubscriptionProtocol(AID gamemasterAID) {
        ACLMessage subscribeMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
        subscribeMsg.addReceiver(gamemasterAID);
        subscribeMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);

        return new SubscriptionInitiator(this, subscribeMsg) {
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                out("%s failed to subscribe", refuse.getSender().getName());
            }

            @Override
            protected void handleAgree(ACLMessage agree) {
                out("%s agreed to subscribe", agree.getSender().getName());

                super.handleAgree(agree);
            }

            @Override
            protected void handleInform(ACLMessage inform) {
                out("been informed by %s", inform.getSender().getName());

                String content = inform.getContent();

                // TODO replace with FIPA SL
                String contents[] = content.split(" ");
                String id = contents[0];
                AID aid1 = new AID(contents[1], AID.ISLOCALNAME);
                boolean answer1 = Boolean.parseBoolean(contents[2]);
                AID aid2 = new AID(contents[3], AID.ISLOCALNAME);
                boolean answer2 = Boolean.parseBoolean(contents[2]);

                if (aid2.equals(myAgent.getAID())) {
                    AID aidT = aid1;
                    boolean answertT = answer1;

                    aid1 = aid2;
                    answer1 = answer2;

                    aid2 = aidT;
                    answer2 = answertT;
                }

                history.addAnswer(id, new GameHistory.AnswersPrisoners(aid1, answer1, aid2, answer2));
            }
        };
    }

    private AID getGamemasterService() throws FIPAException {
        DFAgentDescription gamemasterServiceTemplate = new DFAgentDescription();
        ServiceDescription gamemasterServiceTemplateSD = new ServiceDescription();
        gamemasterServiceTemplateSD.setType("prisoners-dilemma-gamemaster"); // TODO refactor into constant
        gamemasterServiceTemplate.addServices(gamemasterServiceTemplateSD);

        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(1L);

        DFAgentDescription[] results =
                DFService.searchUntilFound(this, getDefaultDF(), gamemasterServiceTemplate, sc,
                        10000L);

        DFAgentDescription dfd = results[0];
        AID gamemasterAID = dfd.getName();

        // do we need this?
        Iterator it = dfd.getAllServices();
        while (it.hasNext()) {
            ServiceDescription sd = (ServiceDescription) it.next();
            if (sd.getType().equals("prisoners-dilemma-gamemaster"))
                out("found the following service: %s by %s", sd.getName(), gamemasterAID.getName());
        }

        return gamemasterAID;
    }

    @Override
    protected void takeDown() {
        out("Stopping");
    }
}
