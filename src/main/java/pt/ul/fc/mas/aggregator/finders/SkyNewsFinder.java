package pt.ul.fc.mas.aggregator.finders;

import com.google.common.collect.ImmutableList;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;

/**
 * @author Nuno Rodrigues fc44825
 */
public class SkyNewsFinder extends Agent {

    protected void setup() {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Presenter");
        sd.setName(getLocalName() + "-presenter");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("Agent " + getLocalName() + " waiting for CFP...");
        MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new NewsFinderBehaviour(this, template) {
            @Override
            public boolean evaluateSearchQuery(SearchQuery query) {
                // Check if the topic is in the Rss feed
                // TODO: Implement.
                return true;
            }

            @Override
            public NewsSearchResult performSearch(SearchQuery query) {
                // if it is in the feed, get it somehow
                // TODO: Implement.
                return new NewsSearchResult(ImmutableList.of());
            }
        });
    }
}
