package sos.mas.strategies;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import sos.mas.GameHistory;
import sos.mas.ontology.Guilty;
import sos.mas.ontology.Prisoner;

public class RandomStrategy extends AbstractStrategyBehaviour {

    private double chanceForComply;

    public RandomStrategy(Codec codec, Ontology ontology, GameHistory game) { this(codec, ontology, game, 0.2); }

    public RandomStrategy(Codec codec, Ontology ontology, GameHistory game, double chanceForComply) {
        super(codec, ontology, game);
        this.chanceForComply = chanceForComply;
    }

    public RandomStrategy(Codec codec, Ontology ontology, GameHistory game, String chanceForComply) {
        this(codec, ontology, game, Double.parseDouble(chanceForComply));
    }

    @Override
    protected Guilty prepareResultNotification(Guilty areYouGuilty) {
        boolean comply = Math.random() < chanceForComply;

        areYouGuilty.setPrisoner(new Prisoner(myAgent.getAID()));
        areYouGuilty.setConfession(comply);

        return areYouGuilty;
    }

    @Override
    public String toString() {
        return "RandomStrategy{chanceForComply=" + chanceForComply + '}';
    }
}
