package sos.mas;

import jade.core.AID;
import jade.core.Agent;
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

    @Override
    protected void setup() {
        try {
            System.out.println("Starting prisoner agent " + getLocalName());

            DFAgentDescription gamemasterServiceTemplate = new DFAgentDescription();
            ServiceDescription gamemasterServiceTemplateSD = new ServiceDescription();
            gamemasterServiceTemplateSD.setType("prisoners-dilemma-gamemaster"); // TODO refactor into constant
            gamemasterServiceTemplate.addServices(gamemasterServiceTemplateSD);

            SearchConstraints sc = new SearchConstraints();
            sc.setMaxResults(1L);

            DFAgentDescription[] results = DFService.search(this, gamemasterServiceTemplate, sc);

            DFAgentDescription dfd = results[0];
            AID gamemasterAID = dfd.getName();

            // do we need this?
            Iterator it = dfd.getAllServices();
            while (it.hasNext()) {
                ServiceDescription sd = (ServiceDescription) it.next();
                if (sd.getType().equals("prisoners-dilemma-gamemaster")) {
                    System.out.println(
                            "Agent " + getLocalName() + " found the following prisoners-dilemma-gamemaster services:");
                    System.out.println(
                            "- Service \"" + sd.getName() + "\" provided by agent " + gamemasterAID.getName());
                }
            }

            MessageTemplate queryMessageTemplate = MessageTemplate.and(MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
                    MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF)), MessageTemplate.MatchContent("(guilty)"));

            ACLMessage subscribeMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
            subscribeMsg.addReceiver(gamemasterAID);
            subscribeMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);

            addBehaviour(new SubscriptionInitiator(this, subscribeMsg) {

            });

            addBehaviour(new AchieveREResponder(this, queryMessageTemplate) {
                @Override
                protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
                    ACLMessage agree = request.createReply();
                    agree.setPerformative(ACLMessage.AGREE);
                    return agree;
                }

                @Override
                protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
                        throws FailureException {
                    boolean comply = Math.random() > .8;

                    System.out.println("Prisoner " + getLocalName() + " " + (comply ? "complies" : "defects"));

                    ACLMessage inform = request.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setContent(comply ? "(true)" : "(false)");

                    return inform;
                }
            });

        } catch (FIPAException e) {
            e.printStackTrace();

            takeDown();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Stopping prisoner agent \"" + getLocalName());
    }
}
