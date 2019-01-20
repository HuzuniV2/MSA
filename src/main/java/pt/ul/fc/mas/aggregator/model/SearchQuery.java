package pt.ul.fc.mas.aggregator.model;

public class SearchQuery {

    public enum SearchType {
        TITLE,
        CONTENT,
        CATEGORY
    }

    private SearchType type;
    private String keyword;

    public SearchQuery(SearchType type, String keyword) {
        this.type = type;
        this.keyword = keyword;
    }

    public SearchType getType() {
        return type;
    }

    public String getKeyword() {
        return keyword;
    }
}
