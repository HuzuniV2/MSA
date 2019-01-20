package pt.ul.fc.mas.aggregator.finders;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;

public abstract class NewsFinderBehaviour extends ContractNetResponder {

    public NewsFinderBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    /**
     * Checks if it is possible to retrieve data for the {@code query}.
     * @return
     */
    public abstract boolean evaluateSearchQuery(SearchQuery query);

    /**
     * Finds the data for requested {@code query}.
     * @return
     */
    public abstract NewsSearchResult performSearch(SearchQuery query);

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        System.out.println("Agent " + myAgent.getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());

        // TODO: parse search query from message content.
        SearchQuery query = new SearchQuery(SearchQuery.SearchType.TITLE, "Błaszczykowski");

        boolean proposal = evaluateSearchQuery(query);
        if (proposal) {
            // We provide a proposal
            System.out.println("Agent " + myAgent.getLocalName() + ": Proposing " + proposal);
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);

            // TODO: Change to some numerical value?
            propose.setContent(String.valueOf(proposal));
            return propose;
        } else {
            // We refuse to provide a proposal
            System.out.println("Agent " + myAgent.getLocalName() + ": Refuse");
            throw new RefuseException("evaluation-failed");
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal accepted");

        // TODO: parse search query from message content.
        SearchQuery query = new SearchQuery(SearchQuery.SearchType.TITLE, "Błaszczykowski");

        NewsSearchResult newsSearchResult = performSearch(query);
        if (newsSearchResult.getResults().size() > 0) {
            System.out.println("Agent " + myAgent.getLocalName() + ": Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);

            //TODO: Send results in the message.
            return inform;
        } else {
            System.out.println("Agent " + myAgent.getLocalName() + ": Action execution failed");
            throw new FailureException("unexpected-error");
        }
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal rejected");
    }
}
