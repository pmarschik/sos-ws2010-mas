package sos.mas;

import jade.core.AID;
import jade.core.Agent;
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

            takeDown();
        }
    }

    private AchieveREResponder createQueryProtocol() {
        MessageTemplate queryMessageTemplate = MessageTemplate.and(MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF)), MessageTemplate.MatchContent("(guilty)"));

        return new AchieveREResponder(this, queryMessageTemplate) {
            @Override
            protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
                out("sending agree to %s", request.getSender().getName());

                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
                    throws FailureException {
                boolean comply = Math.random() > .8;

                out((comply ? "complies" : "defects"));

                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);

                // TODO replace with FIPA SL
                inform.setContent(comply ? "(true)" : "(false)");

                return inform;
            }
        };
    }

    private SubscriptionInitiator createSubscriptionProtocol(AID gamemasterAID) {
        ACLMessage subscribeMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
        subscribeMsg.addReceiver(gamemasterAID);
        subscribeMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);

        return new SubscriptionInitiator(this, subscribeMsg) {
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                out("Failed to subscribe to %s", refuse.getSender().getName());
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
                Integer id = Integer.parseInt(contents[0]);
                boolean answerMe = Boolean.parseBoolean(contents[1]);
                boolean answerOther = Boolean.parseBoolean(contents[2]);

                history.addAnswer(id, new GameHistory.AnswersPrisoners(null, answerMe, null, answerOther));
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
