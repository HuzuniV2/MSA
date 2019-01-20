package pt.ul.fc.mas.aggregator;

import com.google.common.collect.ImmutableList;
import jade.Boot;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import pt.ul.fc.mas.aggregator.finders.CNNFinder;
import pt.ul.fc.mas.aggregator.finders.SkyNewsFinder;

import java.util.List;

public class Runner {
    public static void main(String[] args) {

        String[] opts = {
            "-gui",
            "-local-host 127.0.0.1",
            "-port 1222",
        };
        Boot.main(opts);

        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, "NewsAggregator");
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        AgentContainer container = Runtime.instance().createAgentContainer(profile);

        List<String> topicList = ImmutableList.of("Sport", "Politics", "Science");
        String searchQuery = "category:Money";

        try {
            for (String topic : topicList) {
                container.createNewAgent(
                    "CNNFinder-" + topic, CNNFinder.class.getName(), new Object[]{topic}).start();
                container.createNewAgent(
                    "SkyNewsFinder-" + topic, SkyNewsFinder.class.getName(), new Object[]{topic}).start();
            }
            AgentController aggregator =
                container.createNewAgent(
                    "aggregator", Aggregator.class.getName(), new Object[]{searchQuery});
            aggregator.start();
        } catch (StaleProxyException e) {
            System.err.println("Error while creating agents: " + e.getMessage());
            System.exit(1);
        }

//        try {
//            URL myURL = new URL("https://www.dn.pt/");
//            URLConnection myURLConnection = myURL.openConnection();
//            myURLConnection.connect();
//            System.out.println("ok");
//        } catch (MalformedURLException e) {
//            System.out.println("not ok");
//        } catch (IOException e) {
//            System.out.println("not ok also");
//        }
    }
}
