package sos.mas;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.SubscriptionResponder;

import java.util.Date;
import java.util.Vector;

public class GamemasterAgent extends Agent {

    private void out(String text, Object... args) {
        System.out.print("[" + getLocalName() + "] ");
        System.out.println(String.format(text, args));
    }

    class GMSubscriptionResponder extends SubscriptionResponder {
        GMSubscriptionResponder(Agent a) {
            super(a, MessageTemplate.and(
                    MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                            MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)));
        }

        protected ACLMessage handleSubscription(ACLMessage subscription) {
            // handle a subscription request
            // if subscription is ok, create it        	
            try {
                createSubscription(subscription);
            } catch (Exception e) {
                ACLMessage refuse = new ACLMessage(ACLMessage.REFUSE);
                refuse.addReceiver(subscription.getSender());
                refuse.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);

                return refuse;
            }
            // if successful, should answer (return) with AGREE; otherwise with REFUSE or NOT_UNDERSTOOD
            ACLMessage agree = new ACLMessage(ACLMessage.AGREE);
            agree.addReceiver(subscription.getSender());
            agree.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);

            return agree;
        }


        protected void notify(ACLMessage inform) {
            // this is the method you invoke ("call-back") for creating a new inform message;
            // it is not part of the SubscriptionResponder API, so rename it as you like         
            // go through every subscription
            Vector subs = getSubscriptions();

            for (int i = 0; i < subs.size(); i++)
                ((SubscriptionResponder.Subscription) subs.elementAt(i)).notify(inform);
        }
    }

    private GMSubscriptionResponder subscriptionResponder;
    private AID prisoner1;
    private AID prisoner2;
    private int iterations;

    @Override
    protected void setup() {
        try {
            out("Starting");

            handleArguments();
            registerService();

            subscriptionResponder = new GMSubscriptionResponder(this);
            SequentialBehaviour queryProtocol = createQueryProtocol();

            ParallelBehaviour behaviour = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);
            behaviour.addSubBehaviour(subscriptionResponder);
            behaviour.addSubBehaviour(queryProtocol);

            addBehaviour(behaviour);
        } catch (FIPAException fe) {
            fe.printStackTrace();

            takeDown();
        }
    }

    private SequentialBehaviour createQueryProtocol() {
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(prisoner1);
        msg.addReceiver(prisoner2);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);

        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        // TODO replace by FIPA SL
        msg.setContent("guilty");

        SequentialBehaviour behaviour = new SequentialBehaviour(this);

        for (int i = 0; i < iterations; i++) {
            behaviour.addSubBehaviour(new AchieveREInitiator(this, msg) {
                @Override
                public void registerHandleOutOfSequence(Behaviour b) {
                    out("out of sequence!");

                    super.registerHandleOutOfSequence(
                            b);    //To change body of overridden methods use File | Settings | File Templates.
                }

                protected void handleFailure(ACLMessage failure) {
                    if (failure.getSender().equals(myAgent.getAMS()))
                        // FAILURE notification from the JADE runtime: the receiver does not exist
                        out("Responder does not exist");
                    else
                        out("Agent %s failed to perform the requested action", failure.getSender().getName());
                }

                protected void handleAllResultNotifications(Vector notifications) {
                    out("handling result notifications");

                    for (Object notification : notifications) {
                        ACLMessage inform = (ACLMessage) notification;

                        // TODO more comprehensive parsing (i.e. handle errors)
                        // TODO replace with FIPA SL parsing
                        boolean complied = inform.getContent().equals("(true)");

                        // TODO store result

                        out("Agent %s %s", inform.getSender().getName(), (complied ? "complied" : "defected"));
                    }
                }
            });
        }

        return behaviour;
    }

    private void handleArguments() {
        Object[] args = getArguments();

        if (args == null || args.length < 3 || args.length > 3) {
            out("Need to supply the names of the two prisoner agents and the number of iterations.");

            takeDown();
        }

        prisoner1 = new AID((String) args[0], AID.ISLOCALNAME);
        prisoner2 = new AID((String) args[1], AID.ISLOCALNAME);
        iterations = Integer.parseInt((String) args[2]);
    }

    private void registerService() throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(getLocalName());
        sd.setType("prisoners-dilemma-gamemaster");
        // Agents that want to use this service need to "know" the prisoners-dilemma-ontology
        sd.addOntologies("prisoners-dilemma-ontology");
        // Agents that want to use this service need to "speak" the FIPA-SL language
        sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
        dfd.addServices(sd);

        DFService.register(this, dfd);
    }

    @Override
    protected void takeDown() {
        out("Stopping");
    }
}
