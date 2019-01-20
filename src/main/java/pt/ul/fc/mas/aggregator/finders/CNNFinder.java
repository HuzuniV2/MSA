package pt.ul.fc.mas.aggregator.finders;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;
import jade.core.Agent;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;
import pt.ul.fc.mas.aggregator.util.AgentUtils;

public class CNNFinder extends Agent implements NewsFinderAgent {

    private String category;

    @Override
    public String getCategory() {
        return this.category;
    }

    @Override
    protected void setup() {
        try {
            AgentUtils.registerService(this, "Finder", getLocalName() + "-finder");
        } catch (FIPAException e) {
            System.err.println("Error while registering agent " + getLocalName() + ": " + e.getMessage());
        }

        Object[] args = getArguments();
        if (args.length > 0) {
            this.category = (String) args[0];
            System.out.println("Agent " + getLocalName() + " assigned category: " + this.category);
        } else {
            System.out.println("Agent " + getLocalName() + " not assigned a category.");
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
            	Boolean found = false;
            	Document doc;
				try {
					doc = Jsoup.connect("http://edition.cnn.com/services/rss/").get();
					Elements table = doc.select("table");
        	        for (Element row : table.select("tr")) {
        	            Elements tds = row.select("td");
        	            if (tds.size() > 2 && tds.size() < 20) {
        	                if (tds.get(0).text().equals(query.getKeyword())){
        	                	found = true;
        	                }
        	            }
        	        }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	 
                return found;
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
