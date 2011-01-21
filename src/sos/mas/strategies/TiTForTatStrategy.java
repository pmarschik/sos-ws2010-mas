package sos.mas.strategies;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import sos.mas.GameHistory;
import sos.mas.ontology.Guilty;
import sos.mas.ontology.Prisoner;
import sos.mas.ontology.Round;

public class TitForTatStrategy extends AbstractStrategyBehaviour {
    public TitForTatStrategy(Codec codec, Ontology ontology, GameHistory game) {
        super(codec, ontology, game);
    }

    @Override
    protected Guilty prepareResultNotification(Guilty areYouGuilty) {
        boolean comply = true;

        Round previousRound = game.getPreviousRound();

        if (previousRound != null) {
            if (game.getPrisoner1().getAgent().equals(myAgent.getAID()))
                comply = previousRound.getConfession2();
            else
                comply = previousRound.getConfession1();
        }

        areYouGuilty.setPrisoner(new Prisoner(myAgent.getAID()));
        areYouGuilty.setConfession(comply);

        return areYouGuilty;
    }

    @Override
    public String toString() {
        return "TitForTatStrategy";
    }
}
