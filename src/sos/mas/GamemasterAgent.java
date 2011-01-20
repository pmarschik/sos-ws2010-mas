package sos.mas;

import jade.content.ContentElement;
import jade.content.abs.AbsContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Done;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class GamemasterAgent extends Agent {
	private int RoundsPlayed = 0;
	
	private Codec codec = new SLCodec();
	private Ontology ontology = GameOntology.getInstance();
	
    private void out(String text, Object... args) {
        System.out.print("[" + getLocalName() + "] ");
        System.out.println(String.format(text, args));
    }

    private class SubscriptionResponder extends jade.proto.SubscriptionResponder {
        public SubscriptionResponder(Agent a) {
            super(a, MessageTemplate.and(
                    MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                            MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)));

        }

        @Override
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

        public void notify(ACLMessage inform) {
            // this is the method you invoke ("call-back") for creating a new inform message;
            // it is not part of the SubscriptionResponder API, so rename it as you like         

            // go through every subscription
            Vector subs = getSubscriptions();

            for (int i = 0; i < subs.size(); i++)
                ((jade.proto.SubscriptionResponder.Subscription) subs.elementAt(i)).notify(inform);
        }
    }

    private SubscriptionResponder subscriptionResponder;
    private AID prisoner1;
    private AID prisoner2;
    private int iterations;
    private GameHistory gameHistory = new GameHistory();

    @Override
    protected void setup() {
        try {
            out("Starting");

            getContentManager().registerLanguage(codec);
            getContentManager().registerOntology(ontology);
            
            handleArguments();
            registerService();

            subscriptionResponder = new SubscriptionResponder(this);
            SequentialBehaviour queryProtocol = createQueryProtocol();

            ParallelBehaviour behaviour = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);
            behaviour.addSubBehaviour(subscriptionResponder);
            behaviour.addSubBehaviour(queryProtocol);

            addBehaviour(behaviour);
        } catch (FIPAException fe) {
            fe.printStackTrace();

            doDelete();
        }
    }

    private SequentialBehaviour createQueryProtocol() {
        ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
        msg.addReceiver(prisoner1);
        msg.addReceiver(prisoner2);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);

        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        // TODO replace by FIPA SL
        msg.setContent("(guilty)");

        SequentialBehaviour behaviour = new SequentialBehaviour(this);

        for (int i = 0; i < iterations; i++) {
            behaviour.addSubBehaviour(new AchieveREInitiator(this, msg) {
                

				@Override
                protected void handleFailure(ACLMessage failure) {
                    if (failure.getSender().equals(myAgent.getAMS()))
                        // FAILURE notification from the JADE runtime: the receiver does not exist
                        out("Responder does not exist");
                    else
                        out("Agent %s failed to perform the requested action", failure.getSender().getName());
                }

                @Override
                protected void handleAllResultNotifications(Vector notifications) {
                    out("handling %d result notifications", notifications.size());

                    if (notifications.size() == 0) return;

                    List<GameHistory.Answer> answers = new ArrayList<GameHistory.Answer>(notifications.size());

                    for (Object notification : notifications) {
                        ACLMessage inform = (ACLMessage) notification;

                        
                        ContentElement msgContent= null;
                        try {
        					msgContent = getContentManager().extractContent(inform);
        				} catch (UngroundedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} catch (CodecException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} catch (OntologyException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
                        if (msgContent instanceof Answers)
                        {
                        	Answers answer = (Answers)msgContent;
                        	boolean complied = answer.getAnswer();        	

                            answers.add(new GameHistory.Answer(inform.getSender(), complied));

                            out("Agent %s %s", inform.getSender().getName(), (complied ? "complied" : "defected"));
                        }
                        
                        /*
                        // TODO more comprehensive parsing (i.e. handle errors)
                        // TODO replace with FIPA SL parsing
                        boolean complied = inform.getContent().equals("(true)");

                        // TODO store result

                        answers.add(new GameHistory.Answer(inform.getSender(), complied));

                        out("Agent %s %s", inform.getSender().getName(), (complied ? "complied" : "defected")); 
                        */
                    }

                    String id = ((ACLMessage) notifications.get(0)).getConversationId();
                    gameHistory.addAnswer(id, new GameHistory.AnswersPrisoners(answers.get(0).getPrisonerAID(),
                            answers.get(0).getAnswer(), answers.get(1).getPrisonerAID(), answers.get(1).getAnswer()));                    
                    
                    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                    inform.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
                    inform.setLanguage(codec.getName());
                    inform.setOntology(ontology.getName());
                    
                    GameResult result = new GameResult();
                    result.setId(id);
                    result.setPrisoner1(answers.get(0).getPrisonerAID());
                    result.setPrisoner2(answers.get(1).getPrisonerAID());
                    result.setAnswer1(answers.get(0).getAnswer());
                    result.setAnswer2(answers.get(1).getAnswer());                    
                    
                    ResultsIn resultsIn = new ResultsIn();
                    resultsIn.setResult(result);
                    
                    try {
                    	// Let JADE convert from Java objects to string
                    	getContentManager().fillContent(inform, resultsIn);                    	
                    	send(inform);
                    	}
                    catch (CodecException ce) {
                    	ce.printStackTrace();
                    	}
                    catch (OntologyException oe) {
                    	oe.printStackTrace();
                    	}
                    
                    //inform.setContent(String.format("%s %s %s %s %s", id, answers.get(0).getPrisonerAID().getLocalName(),
                     //       answers.get(0).getAnswer(), answers.get(1).getPrisonerAID().getLocalName(), answers.get(1).getAnswer()));
                    
                    
                    subscriptionResponder.notify(inform);
                    
                    RoundsPlayed++;
                    if(RoundsPlayed == iterations)
                    {
                    	AID winner = gameHistory.calculateWinner();
                    	if(winner != null)
                    		out(winner.getLocalName() + " WON!");
                    	else
                    		out("Result: DRAW!");
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
        sd.addOntologies(ontology.getName());
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
