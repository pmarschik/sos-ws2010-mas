package sos.mas.strategies;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREResponder;
import sos.mas.GameHistory;
import sos.mas.ontology.Guilty;

public abstract class AbstractStrategyBehaviour extends OneShotBehaviour {
    protected Codec codec;
    protected Ontology ontology;
    protected GameHistory game;

    public AbstractStrategyBehaviour(Codec codec, Ontology ontology, GameHistory game) {
        this.codec = codec;
        this.ontology = ontology;
        this.game = game;
    }

    protected void out(String text, Object... args) {
        System.out.print(String.format("[%s] ", myAgent.getAID().getLocalName()));
        System.out.println(String.format(text, args));
    }

    @Override
    public void action() {
        AchieveREResponder p = (AchieveREResponder) parent;

        ACLMessage query = (ACLMessage) getDataStore().get(p.REQUEST_KEY);
        ACLMessage response = (ACLMessage) getDataStore().get(p.RESPONSE_KEY);

        try {
            Guilty guilty = (Guilty) myAgent.getContentManager().extractContent(query);

            ACLMessage inform = query.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            inform.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
            inform.setOntology(ontology.getName());

            myAgent.getContentManager().fillContent(inform, prepareResultNotification(guilty));

            out("replying with %s", inform.getContent());

            getDataStore().put(p.RESULT_NOTIFICATION_KEY, inform);
        } catch (Exception e) {
            e.printStackTrace();

            ACLMessage notUnderstood = query.createReply();
            notUnderstood.setPerformative(ACLMessage.NOT_UNDERSTOOD);

            getDataStore().put(p.RESULT_NOTIFICATION_KEY, notUnderstood);
        }
    }

    protected abstract Guilty prepareResultNotification(Guilty areYouGuilty);
}
