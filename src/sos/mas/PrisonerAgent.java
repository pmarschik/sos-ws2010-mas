package sos.mas;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
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
import sos.mas.ontology.GameOntology;
import sos.mas.ontology.Round;
import sos.mas.strategies.AbstractStrategyBehaviour;
import sos.mas.strategies.ConstantStrategy;
import sos.mas.strategies.RandomStrategy;
import sos.mas.strategies.TiTForTatStrategy;

import java.util.Iterator;

public class PrisonerAgent extends Agent {

    private Codec codec = new SLCodec();
    private Ontology ontology = GameOntology.getInstance();
    private AbstractStrategyBehaviour usedStrategy = null;
    private GameHistory game;

    private void out(String text, Object... args) {
        System.out.print("[" + getLocalName() + "] ");
        System.out.println(String.format(text, args));
    }

    @Override
    protected void setup() {
        try {
            out("Starting");

            getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);
            getContentManager().registerOntology(ontology);

            game = new GameHistory(getAID(), null, -1);
            handleArguments();

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
        MessageTemplate queryMessageTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF));

        AchieveREResponder arer = new AchieveREResponder(this, queryMessageTemplate) {
            @Override
            protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }
        };

        arer.registerPrepareResultNotification(usedStrategy);

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

                ContentElement msgContent = null;

                try {
                    msgContent = getContentManager().extractContent(inform);
                } catch (Exception e) {
                    e.printStackTrace();

                    return;
                }

                if (!(msgContent instanceof Round)) {
                    out("ERROR: message content not Round");

                    return;
                }

                Round lastRound = (Round) msgContent;
                game.pushRound(lastRound);
            }
        };
    }

    private AID getGamemasterService() throws FIPAException {
        DFAgentDescription gamemasterServiceTemplate = new DFAgentDescription();
        ServiceDescription gamemasterServiceTemplateSD = new ServiceDescription();
        gamemasterServiceTemplateSD.setType("prisoners-dilemma-gamemaster"); // TODO refactor into constant
        gamemasterServiceTemplateSD.addLanguages(FIPANames.ContentLanguage.FIPA_SL0);
        gamemasterServiceTemplateSD.addOntologies(ontology.getName());
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

    private void handleArguments() {
        Object[] args = getArguments();

        if (args == null || args.length > 2) {
            out("Need to supply the strategy which the prisoner should use.");

            doDelete();
        }

        String arg = (String) args[0];

        if (arg.equals("random"))
            usedStrategy = new RandomStrategy(codec, ontology, Double.parseDouble((String) args[1]));
        else if (arg.equals("constant")) {
            if (Integer.parseInt((String) args[1]) == 1)
                usedStrategy = new ConstantStrategy(codec, ontology, true);
            else if (Integer.parseInt((String) args[1]) == 0)
                usedStrategy = new ConstantStrategy(codec, ontology, false);
        } else if (arg.equals("titfortat"))
            usedStrategy = new TiTForTatStrategy(codec, ontology, game);

        if (usedStrategy == null) {
            out("ERROR: invalid strategy parameter supplied");

            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        out("Stopping");
    }
}
