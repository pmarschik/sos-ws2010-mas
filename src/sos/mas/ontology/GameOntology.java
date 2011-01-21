package sos.mas.ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;
import jade.content.schema.TermSchema;

public class GameOntology extends Ontology {
    private static Ontology theInstance = new GameOntology();

    public static Ontology getInstance() {
        return theInstance;
    }

    public static final String NAME = "game-ontology";

    // Concepts
    public static final String GAME = "GAME";
    public static final String GAME_ITERATIONS = "iterations";

    public static final String PRISONER = "PRISONER";
    public static final String PRISONER_AGENT = "agent";

    // Predicates
    public static final String PLAYS_IN_GAME = "PLAYS-IN-GAME";
    public static final String PLAYS_IN_GAME_GAME = "game";
    public static final String PLAYS_IN_GAME_PRISONER = "prisoner";

    public static final String GUILTY = "GUILTY";
    public static final String GUILTY_CONFESSION = "confession";
    public static final String GUILTY_PRISONER = "prisoner";

    public static final String ROUND = "ROUND";
    public static final String ROUND_ID = "id";
    public static final String ROUND_GAME = "game";
    public static final String ROUND_CONFESSION1 = "confession1";
    public static final String ROUND_CONFESSION2 = "confession2";

    private GameOntology() {
        super(NAME, BasicOntology.getInstance());

        try {
            add(new ConceptSchema(GAME), Game.class);
            add(new ConceptSchema(PRISONER), Prisoner.class);
            add(new PredicateSchema(PLAYS_IN_GAME), PlaysInGame.class);
            add(new PredicateSchema(GUILTY), Guilty.class);
            add(new PredicateSchema(ROUND), Round.class);

            ConceptSchema cs = (ConceptSchema) getSchema(GAME);
            cs.add(GAME_ITERATIONS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

            cs = (ConceptSchema) getSchema(PRISONER);
            cs.add(PRISONER_AGENT, (TermSchema) getSchema(BasicOntology.AID));

            PredicateSchema ps = (PredicateSchema) getSchema(PLAYS_IN_GAME);
            ps.add(PLAYS_IN_GAME_GAME, getSchema(GAME));
            ps.add(PLAYS_IN_GAME_PRISONER, getSchema(PRISONER));

            ps = (PredicateSchema) getSchema(GUILTY);
            ps.add(GUILTY_CONFESSION, getSchema(BasicOntology.BOOLEAN), ConceptSchema.OPTIONAL);
            ps.add(GUILTY_PRISONER, getSchema(PRISONER), ConceptSchema.OPTIONAL);

            ps = (PredicateSchema) getSchema(ROUND);
            ps.add(ROUND_ID, getSchema(BasicOntology.STRING));
            ps.add(ROUND_GAME, getSchema(GAME));
            ps.add(ROUND_CONFESSION1, getSchema(BasicOntology.BOOLEAN));
            ps.add(ROUND_CONFESSION2, getSchema(BasicOntology.BOOLEAN));
        } catch (OntologyException e) {
            e.printStackTrace(System.out);
        }
    }
}
