package pt.ul.fc.mas.aggregator.model;

import java.util.Date;

public class News {

    private final String author;
    private final String title;
    private final String description;
    private final String link;
    private final String source;
    private final String sourceLink;
    private final Date publishedDate;

    public News(String author, String title, String description, String link, String source, String sourceLink, Date publishedDate) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.link = link;
        this.source = source;
        this.sourceLink = sourceLink;
        this.publishedDate = publishedDate;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getSource() {
        return source;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }
}
