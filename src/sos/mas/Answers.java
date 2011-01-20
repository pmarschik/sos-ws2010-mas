package sos.mas;

import jade.content.Predicate;

public class Answers implements Predicate{
	private static final long serialVersionUID = 1L;
	
	private boolean answer;

	public Answers()
	{
		
	}
	
	public void setAnswer(boolean answer) {
		this.answer = answer;
	}

	public boolean getAnswer() {
		return answer;
	}
	
	
}
