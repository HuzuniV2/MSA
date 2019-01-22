package pt.ul.fc.mas.aggregator.model;

import com.google.common.collect.ImmutableList;
import com.rometools.rome.feed.synd.SyndEntry;

import java.util.List;

public class NewsSearchResult {

    private List<SyndEntry> results;

    public NewsSearchResult(List<SyndEntry> results) {
        this.results = ImmutableList.copyOf(results);
    }

    public List<SyndEntry> getResults() {
        return results;
    }
}
