package app;

import io.javalin.Javalin;
import pt.ul.fc.mas.aggregator.model.News;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    static List<News> feed = new ArrayList<>();

    public static void main(String[] args) {

        Javalin app = Javalin.create()
            .port(7000)
            .enableStaticFiles("/public")
            .enableRouteOverview("/routes")
            .start();

        app.routes(() -> {
            get("/", IndexController.serveIndexPage);
            post("/", IndexController.updateIndexPage);
            post("/news", IndexController.handleSearchRequest);
        });
    }
}
