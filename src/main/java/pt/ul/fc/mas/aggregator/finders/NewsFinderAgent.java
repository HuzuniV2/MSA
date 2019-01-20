package pt.ul.fc.mas.aggregator.finders;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public abstract class NewsFinderAgent extends Agent {

    /** Category for which the agent is responsible. */
    private String category;

    public NewsFinderAgent(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    protected void setupAgent(String type, String name) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(name);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            System.err.println("Error while registering agent: " + name);
            System.exit(1);
        }
    }
}
