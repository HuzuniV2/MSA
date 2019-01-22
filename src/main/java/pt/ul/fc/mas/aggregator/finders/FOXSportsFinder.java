package pt.ul.fc.mas.aggregator.finders;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import pt.ul.fc.mas.aggregator.util.AgentUtils;

@SuppressWarnings("serial")
public class FOXSportsFinder extends Agent implements NewsFinderAgent {
	
	private Map<String, String> themes = new HashMap<String, String>();
	
	/**
	 * Builds a Map containing key-theme and value-url from the cnn table
	 * @param themes
	 * @throws IOException
	 */
	private void getThemes(Map<String, String> themes) throws IOException{
			themes.put("MLB", "https://api.foxsports.com/v1/rss?=mlb");
			themes.put("NFL", "https://api.foxsports.com/v1/rss?=nfl");
			themes.put("NCAAFB", "https://api.foxsports.com/v1/rss?=cfb");
			themes.put("NBA", "https://api.foxsports.com/v1/rss?=nba");
			themes.put("NHL", "https://api.foxsports.com/v1/rss?=nhl");
			themes.put("NCAABK", "https://api.foxsports.com/v1/rss?=cbk");
			themes.put("NASCAR", "https://api.foxsports.com/v1/rss?=nascar");
			themes.put("UFC", "https://api.foxsports.com/v1/rss?=ufc");
			themes.put("Motor", "https://api.foxsports.com/v1/rss?=motor");
			themes.put("Golf", "https://api.foxsports.com/v1/rss?=golf");
			themes.put("Soccer", "https://api.foxsports.com/v1/rss?=soccer");
			themes.put("Olympics", "https://api.foxsports.com/v1/rss?=olympics");
			themes.put("Tennis", "https://api.foxsports.com/v1/rss?=tennis");
			themes.put("Horseracing", "https://api.foxsports.com/v1/rss?=horse-racing");
			themes.put("WNBA", "https://api.foxsports.com/v1/rss?=wnba");
			themes.put("WCBK", "https://api.foxsports.com/v1/rss?=wcbk");		
	}
	
	
	
	

    private String category;
    private List<SyndEntry> feed = new ArrayList<SyndEntry>();

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
					e.printStackTrace();
				}
				if (query.getType() == SearchQuery.SearchType.CATEGORY) {
					if (!category.isEmpty()) {
						// Specialized finder - respond only if asked for the specialized category.
						return category.equalsIgnoreCase(query.getKeyword());
	                } 
					else {
						// General finder, without specified category - respond to any of handled categories.
	                    return themes.containsKey(query.getKeyword().toLowerCase());
	                   }
	                }
	                return true;
            }

            @Override
            public NewsSearchResult performSearch(SearchQuery query) {
            	
            	
            	switch(query.getType()) {
            	
            	case TITLE:
            		//for each rss url in the table
            		themes.forEach((key, value) -> {
            			try {
							URL feedUrl = new URL(value);
							SyndFeedInput input = new SyndFeedInput();
							try {
								//copy the all entries
								List<SyndEntry> temp = input.build(new XmlReader(feedUrl)).getEntries();
								//for each entry containing the keyword in the title, that entry gets copied
								for(SyndEntry entry : temp) {
									if (entry.getTitle().toLowerCase().trim().contains(query.getKeyword().toLowerCase())) {
										feed.add(entry);
									}
								}
							} catch (IllegalArgumentException | FeedException | IOException e) {
								e.printStackTrace();
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
            		});			
            		break;
            		
            	case CONTENT:
            		//for each rss url in the table
            		themes.forEach((key, value) -> {
            			try {
							URL feedUrl = new URL(value);
							SyndFeedInput input = new SyndFeedInput();
							try {
								//copy the all entries
								List<SyndEntry> temp = input.build(new XmlReader(feedUrl)).getEntries();
								//for each entry containing the keyword in the description, that entry gets copied
								for(SyndEntry entry : temp) {
									if (entry.getDescription().toString().toLowerCase().trim().contains(query.getKeyword().toLowerCase())) {
										feed.add(entry);
									}
								}
							} catch (IllegalArgumentException | FeedException | IOException e) {
								e.printStackTrace();
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
            		});	
            		break;
            		
            	case CATEGORY:
            		try {
						URL feedUrl = new URL(themes.get(query.getKeyword()));
						SyndFeedInput input = new SyndFeedInput();
		                try {
		                	// Gets all entries
							feed = input.build(new XmlReader(feedUrl)).getEntries();
						} catch (IllegalArgumentException | FeedException | IOException e) {
							e.printStackTrace();
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
            		
            		break;
				default:
					throw new IllegalArgumentException("Unknown type of keyword: " + query.getType());
            	}
				
               
            	NewsSearchResult news = new NewsSearchResult(feed);
            	return news;
            	
            }
        });
    }
}
