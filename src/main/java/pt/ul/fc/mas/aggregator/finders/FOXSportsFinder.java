package pt.ul.fc.mas.aggregator.finders;

import com.google.common.collect.ImmutableMap;
import com.rometools.rome.feed.synd.SyndEntry;
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

public class FOXSportsFinder extends Agent implements NewsFinderAgent {

    private static final String RSS_PREFIX = "https://api.foxsports.com/v1/rss?";

    public static final Map<String, String> THEMES = ImmutableMap.<String, String>builder()
        .put("NFL", RSS_PREFIX + "tag=nfl")
        .put("NCAAFB", RSS_PREFIX + "tag=cfb")
        .put("NBA", RSS_PREFIX + "tag=nba")
        .put("NHL", RSS_PREFIX + "tag=nhl")
        .put("NCAABK", RSS_PREFIX + "tag=cbk")
        .put("NASCAR", RSS_PREFIX + "tag=nascar")
        .put("UFC", RSS_PREFIX + "tag=ufc")
        .put("Motor", RSS_PREFIX + "tag=motor")
        .put("Golf", RSS_PREFIX + "=golf")
        .put("Soccer", RSS_PREFIX + "=soccer")
        .put("Olympics", RSS_PREFIX + "=olympics")
        .put("Tennis", RSS_PREFIX + "=tennis")
        .put("Horseracing", RSS_PREFIX + "=horse-racing")
        .put("WNBA", RSS_PREFIX + "=wnba")
        .put("WCBK", RSS_PREFIX + "=wcbk")
        .build();

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
            if (!RssUtils.isValidCategoryArg(this.category, THEMES.keySet())) {
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
                        // Specialized finder - respond only if asked for the specialized category.
                        return category.equalsIgnoreCase(query.getKeyword());
                    } else {
                        // General finder, without specified category - respond to any of handled categories.
                        return THEMES.containsKey(query.getKeyword().toLowerCase());
                    }
                }
                return true;
            }
            /**
             * Checks whether {@code categoryKeyword} is valid, i.e. is one of {@code HANDLED_CATEGORIES},
             * and in case the finder is specialised, whether it matches the finder's specialisation.
             */
            private boolean isValidCategory(String categoryKeyword) {
                return THEMES.containsKey(categoryKeyword.toLowerCase()) &&
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
                        matchingFeeds.add(new URL(THEMES.get(query.getKeyword().toLowerCase())));
                    } else if (!category.isEmpty() && THEMES.containsKey(category)) {
                        // Specialised finder for <category>
                        matchingFeeds.add(new URL(THEMES.get(category)));
                    } else {
                        for (String url : THEMES.values()) {
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
