package pt.ul.fc.mas.aggregator.finders;

import com.google.common.collect.ImmutableList;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;

public class CNNFinder extends NewsFinderAgent {

    public CNNFinder(String category) {
        super(category);
    }

    @Override
    protected void setup() {
        super.setupAgent("Finder", getLocalName() + "-finder");

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
