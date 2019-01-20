package pt.ul.fc.mas.aggregator;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import pt.ul.fc.mas.aggregator.model.SearchQuery;
import pt.ul.fc.mas.aggregator.util.AgentUtils;

import java.util.Date;
import java.util.List;

/**
 * This agent aggregates the news gathered from the NewsFinder agents.
 */
public class Aggregator extends Agent {

    protected void setup() {

        Object[] args = getArguments();
        if (args == null || args.length == 0) {
            System.out.println("Aggregator requires a query to search for. Usage:");
            System.out.println("    [title|content|category]:<keyword>");
            System.out.println("Example queries:");
            System.out.println("    title:Błaszczykowski");
            System.out.println("    content:Ronaldo");
            System.out.println("    category:Golf");
            System.exit(0);
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Aggregator");
        sd.setName(getLocalName() + "-aggregator");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        try {
            List<AID> finders = AgentUtils.getAgents(this, "Finder");
            System.out.println("Delegating searching for news to " + finders.size() + " responders.");

            // Fill the CFP message
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            for (AID finderAgent : finders) {
                msg.addReceiver(finderAgent);
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 10 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            msg.setContent("news-search");
            // TODO: Parse search query argument and put in content.
            SearchQuery query = new SearchQuery(SearchQuery.SearchType.TITLE, "Błaszczykowski");

            addBehaviour(new AggregatorBehaviour(this, msg));

        } catch (FIPAException e) {
            System.err.println("Error while searching for news finder agents.");
            System.exit(1);
        }
    }
}
