package pt.ul.fc.mas.aggregator.finders;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;
import pt.ul.fc.mas.aggregator.model.SearchQuery;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RssUtils {

    public static NewsSearchResult searchFilterAndResult(SearchQuery query, List<URL> feeds) throws FeedException, IOException {
        List<SyndEntry> entries = new ArrayList<>();
        String keyword = query.getKeyword().toLowerCase();
        for (URL feedUrl : feeds) {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            entries.addAll(feed.getEntries().stream().filter(e -> {
                if (query.getType() == SearchQuery.SearchType.TITLE) {
                    return e.getTitle().toLowerCase().contains(keyword);
                } else if (query.getType() == SearchQuery.SearchType.CONTENT) {
                    return e.getDescription().getValue().toLowerCase().contains(keyword);
                }
                return true;
            }).collect(Collectors.toList()));
        }
        return new NewsSearchResult(entries);
    }
}
