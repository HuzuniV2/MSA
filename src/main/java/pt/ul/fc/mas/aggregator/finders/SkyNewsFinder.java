package pt.ul.fc.mas.aggregator.finders;

import com.google.common.collect.ImmutableMap;
import com.rometools.rome.io.FeedException;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;
import pt.ul.fc.mas.aggregator.util.AgentUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkyNewsFinder extends Agent implements NewsFinderAgent {

    private static final String RSS_PREFIX = "http://feeds.skynews.com/feeds/rss/";

    public static final Map<String, String> HANDLED_CATEGORIES = ImmutableMap.<String, String>builder()
        .put("uk", RSS_PREFIX + "uk.xml")
        .put("world", RSS_PREFIX + "world.xml")
        .put("us", RSS_PREFIX + "us.xml")
        .put("business", RSS_PREFIX + "business.xml")
        .put("politics", RSS_PREFIX + "politics.xml")
        .put("technology", RSS_PREFIX + "technology.xml")
        .put("entertainment", RSS_PREFIX + "entertainment.xml")
        .put("strangenews", RSS_PREFIX + "strange.xml")
        .build();

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
            if (!RssUtils.isValidCategoryArg(this.category, HANDLED_CATEGORIES.keySet())) {
                System.err.println("WARNING: Invalid category for agent " + getLocalName() + ": " + this.category
                    + " - agent will be assigned the general category.");
                this.category = "";
            } else {
                System.out.println("Agent " + getLocalName() + " assigned category: " + this.category);
            }
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
                if (query.getType() == SearchQuery.SearchType.CATEGORY) {
                    if (!category.isEmpty()) {
                        // Specialised finder - respond only if asked for the specialised category.
                        return category.equalsIgnoreCase(query.getKeyword());
                    } else {
                        // General finder, without specified category - respond to any of handled categories.
                        return HANDLED_CATEGORIES.keySet().contains(query.getKeyword().toLowerCase());
                    }
                }
                return true;
            }

            /**
             * Checks whether {@code categoryKeyword} is valid, i.e. is one of {@code HANDLED_CATEGORIES},
             * and in case the finder is specialised, whether it matches the finder's specialisation.
             */
            private boolean isValidCategory(String categoryKeyword) {
                return HANDLED_CATEGORIES.containsKey(categoryKeyword.toLowerCase()) &&
                    (category.isEmpty() || category.equalsIgnoreCase(categoryKeyword));
            }

            @Override
            public NewsSearchResult performSearch(SearchQuery query) throws FailureException {
                try {
                    List<URL> matchingFeeds = new ArrayList<>();
                    if (query.getType() == SearchQuery.SearchType.CATEGORY) {
                        if (!isValidCategory(query.getKeyword())) {
                            throw new FailureException("Non-handled category requested.");
                        }
                        matchingFeeds.add(new URL(HANDLED_CATEGORIES.get(query.getKeyword().toLowerCase())));
                    } else if (!category.isEmpty() && HANDLED_CATEGORIES.containsKey(category)) {
                        // Specialised finder for <category>
                        matchingFeeds.add(new URL(HANDLED_CATEGORIES.get(category)));
                    } else {
                        for (String url : HANDLED_CATEGORIES.values()) {
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
