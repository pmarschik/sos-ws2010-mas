package sos.mas;
import jade.content.onto.BeanOntology;
import jade.content.onto.*;


public class GameOntology extends BeanOntology{
	private static Ontology theInstance = new GameOntology("GAME_ONTOLOGY");
	public static Ontology getInstance() {
		return theInstance;
	}
	private GameOntology(String name){
		super(name);
		try {
			add(GameResult.class);
			add(ResultsIn.class);
			add(Answers.class);
		}
		catch (Exception e) {
		e.printStackTrace();
		}
	}
}
