package sos.mas;

import jade.core.AID;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

class GameHistory {
    public static final int PointsBothComplied = 3;
    public static final int PointsWinner = 5;
    public static final int PointsLoser = 0;
    public static final int PointsBothDefected = 1;

    public static class Answer {
        private AID prisonerAID;
        private boolean answer;

        public Answer(AID prisonerAID, boolean answer) {
            this.prisonerAID = prisonerAID;
            this.answer = answer;
        }

        public AID getPrisonerAID() {
            return prisonerAID;
        }

        public boolean getAnswer() {
            return answer;
        }
    }

    public static class AnswersPrisoners {
        private Answer answer1;
        private Answer answer2;

        public AnswersPrisoners(AID agent1, boolean answer1, AID agent2, boolean answer2) {
            this.answer1 = new Answer(agent1, answer1);
            this.answer2 = new Answer(agent2, answer2);
        }

        public Answer getAnswer1() {
            return answer1;
        }

        public Answer getAnswer2() {
            return answer2;
        }
    }

    private HashMap<String, AnswersPrisoners> answers = new HashMap<String, AnswersPrisoners>();

    public HashMap<String, AnswersPrisoners> getAnswers() {
        return answers;
    }

    public void addAnswer(String id, AnswersPrisoners answer) {
        answers.put(id, answer);
    }

    public AnswersPrisoners getAnswer(Integer id) {
        return answers.get(id);
    }
    
    public AID calculateWinner()
    {    	
    	Collection c = answers.values();
    	Iterator itr = c.iterator();
    	AID prisoner1 = null;
    	AID prisoner2 = null;
    	int pointsPrisoner1 = 0;
    	int pointsPrisoner2 = 0;
    	boolean aidSet = false;
    	
    	while(itr.hasNext())
    	{
    		AnswersPrisoners answer = (AnswersPrisoners)itr.next();
    		
    		if(aidSet == false)
    		{
	    		prisoner1 = answer.answer1.prisonerAID;
	    		prisoner2 = answer.answer2.prisonerAID; 
	    		aidSet = true;
    		}
    		
    		if(answer.answer1.answer == true &&  answer.answer2.answer == true)
			{
    			pointsPrisoner1 += 3; pointsPrisoner2 += 3;
			} 
    		else if(answer.answer1.answer == false &&  answer.answer2.answer == false)
    		{
    			pointsPrisoner1 += 1; pointsPrisoner2 += 1;
    		}
    		else if(answer.answer1.answer == true &&  answer.answer2.answer == false)
    		{
    			if(answer.answer1.prisonerAID.equals(prisoner1))
    			{
    				pointsPrisoner1 += 0;
    				pointsPrisoner2 += 5;
    			}
    			else
    			{
    				pointsPrisoner1 += 5;
    				pointsPrisoner2 += 0;	
    			}
    		}
    		else if(answer.answer1.answer == false &&  answer.answer2.answer == true)
    		{
    			if(answer.answer1.prisonerAID.equals(prisoner1))
    			{
    				pointsPrisoner1 += 5; 
    				pointsPrisoner2 += 0;
    			}
    			else
    			{
    				pointsPrisoner1 += 0; 
    				pointsPrisoner2 += 5;
    			}
    		}    			
    	}
    		
		if(pointsPrisoner1 > pointsPrisoner2) return prisoner1;
		if(pointsPrisoner1 < pointsPrisoner2) return prisoner2;
		return null;
    }
}
