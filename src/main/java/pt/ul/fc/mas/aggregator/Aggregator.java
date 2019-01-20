package pt.ul.fc.mas.aggregator;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
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

import java.util.List;
import java.util.Map;

/**
 * This agent aggregates the news gathered from the NewsFinder agents.
 */
public class Aggregator extends Agent {
    private static Map<String, SearchQuery.SearchType> HANDLED_SEARCH_TYPES = ImmutableMap.of(
        "title", SearchQuery.SearchType.TITLE,
        "content", SearchQuery.SearchType.CONTENT,
        "category", SearchQuery.SearchType.CATEGORY);

    @Override
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
            // msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

            SearchQuery query = parseQuery((String) args[0]);
            Gson gson = new Gson();
            msg.setContent(gson.toJson(query));

            addBehaviour(new AggregatorBehaviour(this, msg));

        } catch (FIPAException e) {
            System.err.println("Error while searching for news finder agents.");
            System.exit(1);
        }
    }

    private SearchQuery parseQuery(String query) {
        String[] split = query.split(":");
        if (split.length < 2 || !HANDLED_SEARCH_TYPES.keySet().contains(split[0])) {
            throw new IllegalArgumentException("Search query needs to be in the form: [title|content|category]:<keyword>");
        }
        return new SearchQuery(HANDLED_SEARCH_TYPES.get(split[0]), split[1]);
    }
}
