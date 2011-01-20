package sos.mas;

import jade.content.Concept;
import jade.core.AID;

public class GameResult implements java.io.Serializable, Concept {	 
	private static final long serialVersionUID = 1L;
	
    private String id;
    private AID prisoner1;
    private AID prisoner2;
    private boolean answer1;
    private boolean answer2;    
 
    public GameResult() {
    }
 
    public String getId() {
        return this.id;
    }
 
    public void setId(final String id) {
        this.id = id;
    }

	public void setPrisoner1(AID prisoner1) {
		this.prisoner1 = prisoner1;
	}

	public AID getPrisoner1() {
		return prisoner1;
	}

	public void setPrisoner2(AID prisoner2) {
		this.prisoner2 = prisoner2;
	}

	public AID getPrisoner2() {
		return prisoner2;
	}

	public void setAnswer1(boolean answer1) {
		this.answer1 = answer1;
	}

	public boolean isAnswer1() {
		return answer1;
	}

	public void setAnswer2(boolean answer2) {
		this.answer2 = answer2;
	}

	public boolean isAnswer2() {
		return answer2;
	}    
}
