package app;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.javalin.Handler;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.gateway.JadeGateway;
import pt.ul.fc.mas.aggregator.model.News;
import pt.ul.fc.mas.aggregator.util.AgentUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexController {
    public static Handler serveIndexPage = ctx -> {
        Map<String, Object> model = new HashMap<>();
        model.put("news", Main.feed);
        ctx.render("pages/layout.vm", model);
    };

    public static Handler updateIndexPage = ctx -> {
        Map<String, Object> model = new HashMap<>();
        final Gson gson = new Gson();
        final String body = ctx.body();
        if (!body.isEmpty()) {
            final List<News> news = gson.fromJson(body, new TypeToken<List<News>>(){}.getType());
            Main.feed = news;
            model.put("news", news);
        } else {
            model.put("news", new ArrayList<News>());
        }
        ctx.render("pages/layout.vm", model);
    };

    public static Handler handleSearchRequest = ctx -> {
        String query = ctx.formParam("type") + ":" + ctx.formParam("keyword");

        JadeGateway.execute(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.setConversationId("GET_NEWS");
                req.setContent(query);
                try {
                    List<AID> aggregators = AgentUtils.getAgents(myAgent, "Aggregator");
                    if (aggregators.size() > 0) {
                        req.addReceiver(aggregators.get(0));
                        myAgent.send(req);
                    } else {
                        System.err.println("No Aggregators found!");
                    }
                } catch (FIPAException e) {
                    System.err.println("Error while sending a request to the Aggregator.");
                }
            }
        });
        // Reset the news list
        Main.feed = new ArrayList<>();
        ctx.redirect("/");
    };
}