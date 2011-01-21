package sos.mas.strategies;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import sos.mas.GameHistory;
import sos.mas.ontology.Guilty;
import sos.mas.ontology.Prisoner;
import sos.mas.ontology.Round;

/**
 * @author patrick
 */
public class TiTForTatStrategy extends AbstractStrategyBehaviour {
    GameHistory game;

    public TiTForTatStrategy(Codec codec, Ontology ontology, GameHistory game) {
        super(codec, ontology);

        this.game = game;
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
}
