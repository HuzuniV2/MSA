package pt.ul.fc.mas.aggregator;

import com.google.gson.Gson;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import pt.ul.fc.mas.aggregator.model.NewsSearchResult;

import java.io.IOException;

public class Presenter extends Agent {

    private static final String SERVER_ADDRESS = "http://localhost:7000";

    public static final String PRESENT_RESULTS_MSG = "PRESENT_RESULTS";
    private static final MessageTemplate MSG_TMPL_REQUEST = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    private static final MessageTemplate MSG_TMPL_PRESENT_RESULTS = MessageTemplate.MatchConversationId(PRESENT_RESULTS_MSG);

    @Override
    protected void setup() {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Presenter");
        sd.setName(getLocalName() + "-presenter");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(MessageTemplate.and(MSG_TMPL_REQUEST, MSG_TMPL_PRESENT_RESULTS));
                if (msg != null) {
                    Gson gson = new Gson();
                    NewsSearchResult result = gson.fromJson(msg.getContent(), NewsSearchResult.class);
                    sendNewResults(result);
                } else {
                    block();
                }
            }
        });
    }

    private void sendNewResults(NewsSearchResult result) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            final String payload = new Gson().toJson(result.getResults());
            StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
            HttpPost request = new HttpPost(SERVER_ADDRESS);
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);
            System.out.println("Response from the server: " + response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            System.err.println("Error while sending results to the server: " + e.getMessage());
        }
    }
}
