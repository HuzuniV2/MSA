package app;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.javalin.Handler;
import pt.ul.fc.mas.aggregator.model.News;

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
}