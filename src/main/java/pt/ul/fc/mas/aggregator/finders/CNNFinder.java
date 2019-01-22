package pt.ul.fc.mas.aggregator.finders;

import com.rometools.rome.io.FeedException;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;
import pt.ul.fc.mas.aggregator.util.AgentUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CNNFinder extends Agent implements NewsFinderAgent {

    private String category;
    private Map<String, String> themes;

    /**
     * Builds a Map containing key-theme and value-url from the cnn table
     *
     * @throws IOException
     */
    private Map<String, String> getThemes() throws IOException {
        HashMap<String, String> result = new HashMap<>();
        Document doc = Jsoup.connect("http://edition.cnn.com/services/rss/").get();
        Elements table = doc.select("table");
        for (Element row : table.select("tr")) {
            Elements tds = row.select("td");
            if (tds.size() > 2 && tds.size() < 20 && !tds.text().equals("")) {
                result.put(tds.get(0).text(), tds.get(2).text());
            }
        }
        return result;
    }

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
                    themes = getThemes();
                    if (query.getType() == SearchQuery.SearchType.CATEGORY) {
                        if (!category.isEmpty()) {
                            // Specialized finder - respond only if asked for the specialized category.
                            return category.equalsIgnoreCase(query.getKeyword());
                        } else {
                            // General finder, without specified category - respond to any of handled categories.
                            return themes.containsKey(query.getKeyword().toLowerCase());
                        }
                    } else {
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            private boolean isValidCategory(String keyword) {
                return themes.containsKey(keyword.toLowerCase()) &&
                    (category.isEmpty() || category.equalsIgnoreCase(keyword));
            }

            @Override
            public NewsSearchResult performSearch(SearchQuery query) throws FailureException {
                try {
                    List<URL> matchingFeeds = new ArrayList<>();
                    if (query.getType() == SearchQuery.SearchType.CATEGORY) {
                        if (!isValidCategory(query.getKeyword())) {
                            throw new FailureException("Non-handled category requested.");
                        }
                        matchingFeeds.add(new URL(themes.get(query.getKeyword().toLowerCase())));
                    } else if (!category.isEmpty() && themes.containsKey(category)) {
                        // Specialised finder for <category>
                        matchingFeeds.add(new URL(themes.get(category)));
                    } else {
                        for (String url : themes.values()) {
                            matchingFeeds.add(new URL(url));
                        }
                    }

                    // Filter results for each feed based on the query
                    return RssUtils.searchFilterAndResult(query, matchingFeeds);
                } catch (FeedException | IOException e) {
                    throw new FailureException("Unexpected error: " + e.getMessage());
                }
            }
        });
    }
}
