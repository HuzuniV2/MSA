package pt.ul.fc.mas.aggregator.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class NewsSearchResult {

    private List<News> results;

    public NewsSearchResult(List<News> results) {
        this.results = ImmutableList.copyOf(results);
    }

    public List<News> getResults() {
        return results;
    }
}
