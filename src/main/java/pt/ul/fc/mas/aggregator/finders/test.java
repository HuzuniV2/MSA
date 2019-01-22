package pt.ul.fc.mas.aggregator.finders;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class test {
	public static void main(String [] args) throws IOException {
		Document doc = Jsoup.connect("https://www.foxsports.com/rss-feeds").get();
		//System.out.println(doc);
		Elements table = doc.select("table");
		//System.out.println(table);
        for (Element row : table.select("tr")) {
            Elements tds = row.select("p");
            if (tds.size() > 2 && tds.size() < 20 && !tds.text().equals("")) {
                //System.out.println(tds.get(0).text());
            }
        }
        //System.out.println();
        //System.out.println();
        //System.out.println();
        //Document doc2 = Jsoup.parse("http://rss.cnn.com/rss/edition.rss","" , Parser.xmlParser());
        //for (Element e : doc.select("test")) {
        //    System.out.println(e);
        //}
       boolean ok = false;
            try {
                URL feedUrl = new URL("https://api.foxsports.com/v1/rss?=mlb");

                //Map<SyndEntry, SyndEntry> allEntries = new HashMap<SyndEntry, SyndEntry>();
                SyndFeedInput input = new SyndFeedInput();
                List<SyndEntry> feed = input.build(new XmlReader(feedUrl)).getEntries();
                for(SyndEntry entry : feed) {
                	System.out.println(entry.getTitle().trim());
                    System.out.println(entry.getDescription().getValue().trim());
                    System.out.println(entry.getPublishedDate());
                    System.out.println();
                }

                //System.out.println(feed);
                //System.out.println(feed.getTitleEx());
                //System.out.println(feed.getDescriptionEx());
                //System.out.println(feed.getPublishedDate());
                
                ok = true;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("ERROR: "+ex.getMessage());
            }

        if (!ok) {
            System.out.println();
            System.out.println("FeedReader reads and prints any RSS/Atom feed type.");
            System.out.println("The first parameter must be the URL of the feed to read.");
            System.out.println();
        }
	}
}
