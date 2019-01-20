package pt.ul.fc.mas.aggregator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Runner {
    public static void main(String[] args) {

        try {
            URL myURL = new URL("https://www.dn.pt/");
            URLConnection myURLConnection = myURL.openConnection();
            myURLConnection.connect();
            System.out.println("ok");
        } catch (MalformedURLException e) {
            System.out.println("not ok");
        } catch (IOException e) {
            System.out.println("not ok also");
        }
    }
}
