package pt.ul.fc.mas.aggregator.finders;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import jade.core.Agent;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;
import pt.ul.fc.mas.aggregator.model.SearchQuery.SearchType;
import pt.ul.fc.mas.aggregator.util.AgentUtils;

public class CNNFinder extends Agent implements NewsFinderAgent {
	
	private Map<String, String> themes = new HashMap<String, String>();
	
	/**
	 * Builds a Map containing key-theme and value-url from the cnn table
	 * @param themes
	 * @throws IOException
	 */
	private void getThemes(Map<String, String> themes) throws IOException{
			Document doc = Jsoup.connect("http://edition.cnn.com/services/rss/").get();
			Elements table = doc.select("table");
	        for (Element row : table.select("tr")) {
	            Elements tds = row.select("td");
	            if (tds.size() > 2 && tds.size() < 20 && !tds.text().equals("")) {
	                themes.put(tds.get(0).text(), tds.get(2).text());
	            }
	        }
	}
	
	
	
	

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
            	try {
					getThemes(themes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                // Checks if the query type is a valid one, if its not it refuses the contract
            	Boolean valid = false;
            	if (query.getType() == SearchQuery.SearchType.CATEGORY)
            		valid = true;
                return valid;
            }

            @Override
            public NewsSearchResult performSearch(SearchQuery query) {
            	switch(query.getType()) {
            	case TITLE:
            		break;
            	case CONTENT:
            		break;
            	case CATEGORY:
            		try {
						URL feedUrl = new URL(themes.get(query.getType()));
						SyndFeedInput input = new SyndFeedInput();
		                try {
							List<SyndEntry> feed = input.build(new XmlReader(feedUrl)).getEntries();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		
            		break;
				default:
					throw new IllegalArgumentException("Unknown type of keyword: " + query.getType());
            	}
				
                // if it is in the feed, get it somehow
            	// check what the query type is
            	// switch, depending on the query might need to scrape inside it
                // TODO: Implement.
                return new NewsSearchResult(ImmutableList.of());
            }
        });
    }
}
