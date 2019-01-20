package pt.ul.fc.mas.aggregator.util;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AgentUtils {

    public static void registerService(Agent agent, String serviceType, String serviceName) throws FIPAException {
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(serviceName);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());
        dfd.addServices(sd);
        DFService.register(agent, dfd);
    }

    public static List<AID> getAgents(Agent agent, String agentType) throws FIPAException {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agentType);
        template.addServices(sd);
        return Arrays.stream(DFService.search(agent, template))
            .map(DFAgentDescription::getName)
            .collect(Collectors.toList());
    }
}
