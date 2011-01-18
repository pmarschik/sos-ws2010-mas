package sos.mas;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class PrisonerAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Starting prisoner agent \"" + getLocalName());

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF));

        addBehaviour(new AchieveREResponder(this, template) {
            @Override
            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                System.out.println("Prisoner " + getLocalName() + ": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response)
                    throws FailureException {
                boolean comply = Math.random() > .8;

                System.out.println("Prisoner " + getLocalName() + " " + (comply ? "complies" : "defects"));

                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent(comply ? "inform-t" : "inform-f");

                return inform;
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("Stopping prisoner agent \"" + getLocalName());
    }
}
