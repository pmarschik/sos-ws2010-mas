package sos.mas;

import jade.core.AID;

import java.util.HashMap;

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

        public boolean isAnswer() {
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

    private HashMap<Integer, AnswersPrisoners> answers = new HashMap<Integer, AnswersPrisoners>();

    public HashMap<Integer, AnswersPrisoners> getAnswers() {
        return answers;
    }

    public void addAnswer(Integer id, AnswersPrisoners answer) {
        answers.put(id, answer);
    }

    public AnswersPrisoners getAnswer(Integer id) {
        return answers.get(id);
    }
}
