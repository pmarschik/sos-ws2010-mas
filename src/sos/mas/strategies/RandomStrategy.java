package sos.mas.strategies;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import sos.mas.ontology.Guilty;
import sos.mas.ontology.Prisoner;

public class RandomStrategy extends AbstractStrategyBehaviour {

    private double chanceForComply;

    public RandomStrategy(Codec codec, Ontology ontology) { this(codec, ontology, 0.2); }

    public RandomStrategy(Codec codec, Ontology ontology, double chanceForComply) {
        super(codec, ontology);
        this.chanceForComply = chanceForComply;
    }

    @Override
    protected Guilty prepareResultNotification(Guilty areYouGuilty) {
        boolean comply = Math.random() < chanceForComply;

        areYouGuilty.setPrisoner(new Prisoner(myAgent.getAID()));
        areYouGuilty.setConfession(comply);

        return areYouGuilty;
    }
}
