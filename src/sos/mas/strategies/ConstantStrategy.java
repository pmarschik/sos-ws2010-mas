package sos.mas.strategies;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import sos.mas.ontology.Guilty;
import sos.mas.ontology.Prisoner;

/**
 * @author patrick
 */
public class ConstantStrategy extends AbstractStrategyBehaviour {

    private boolean comply;

    public ConstantStrategy(Codec codec, Ontology ontology, boolean comply) {
        super(codec, ontology);
        this.comply = comply;
    }

    @Override
    protected Guilty prepareResultNotification(Guilty areYouGuilty) {
        areYouGuilty.setPrisoner(new Prisoner(myAgent.getAID()));
        areYouGuilty.setConfession(comply);

        return areYouGuilty;
    }
}
