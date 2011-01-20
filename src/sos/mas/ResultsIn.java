package sos.mas;

import jade.content.Predicate;

public class ResultsIn implements Predicate{
	private static final long serialVersionUID = 1L;
	
	private GameResult result;

	public ResultsIn()
	{
		
	}
	
	public void setResult(GameResult result) {
		this.result = result;
	}

	public GameResult getResult() {
		return result;
	}
	
	
}
