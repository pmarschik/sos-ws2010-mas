package sos.mas.strategies;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import sos.mas.GameHistory;
import sos.mas.ontology.Guilty;
import sos.mas.ontology.Prisoner;

public class ConstantStrategy extends AbstractStrategyBehaviour {

    private boolean comply;

    public ConstantStrategy(Codec codec, Ontology ontology, GameHistory game, boolean comply) {
        super(codec, ontology, game);
        this.comply = comply;
    }

    public ConstantStrategy(Codec codec, Ontology ontology, GameHistory game, String comply) {
        this(codec, ontology, game, Boolean.parseBoolean(comply));
    }

    @Override
    protected Guilty prepareResultNotification(Guilty areYouGuilty) {
        areYouGuilty.setPrisoner(new Prisoner(myAgent.getAID()));
        areYouGuilty.setConfession(comply);

        return areYouGuilty;
    }

    @Override
    public String toString() {
        return "ConstantStrategy{comply=" + comply + '}';
    }
}
