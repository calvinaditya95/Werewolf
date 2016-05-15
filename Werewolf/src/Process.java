import java.net.InetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Process extends Thread {
    private JSONObject input;
    private InetAddress addr;
    private int port;
    
    public Process(String response) {
        try {
            this.input = new JSONObject(response);
        }
        catch (JSONException e) {
            System.out.println(e);
        }
        this.addr = null;
        this.port = -1;
    }
    
    public Process(String response, InetAddress addr, int port) {
        try {
            this.input = new JSONObject(response);
        }
        catch (JSONException e) {
            System.out.println(e);
        }
        this.addr = addr;
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            if (input.has("method")) {
                String method = input.getString("method");
                
                if (method.equals("start")) {
                    Client.time = input.getString("time");
                    Client.role = input.getString("role");
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    Client.sendTCP(response);
                    
                    Client.requestClients();
                }
                else if (method.equals("change_phase")) {
                    Client.time = input.getString("time");
                    Client.hasVoted = false;
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    Client.sendTCP(response);
                    
                    Client.requestClients();
                }
                else if (method.equals("vote_now")) {
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    Client.sendTCP(response);
                    
                    Client.startVote();
                }
                else if (method.equals("kpu_selected")) {
                    Client.kpuSelected = input.getInt("kpu_id");
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    Client.sendTCP(response);
                    
                    Client.startVote();
                }
                else if (method.equals("game_over")) {
                    System.out.println("Game Over");
                    System.out.println("Winner: " + input.getString("winner"));
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    Client.sendTCP(response);
                }
                else if (method.equals("prepare_proposal")) {
                    JSONArray proposal_id = input.getJSONArray("proposal_id");
                    int proposal_number = proposal_id.getInt(0);
                    int player_id = proposal_id.getInt(1);
                    
                    if (Client.previousProposalNumber < proposal_number) {
                        JSONObject response = new JSONObject();
                        response.put("status", "ok");
                        response.put("description", "accepted");
                        if (Client.previousProposalNumber != -1)
                            response.put("previous_accepted", Client.previousProposalID);
                        
                        Client.sendUDP(response, addr, port);
                        
                        Client.previousProposalNumber = proposal_number;
                        Client.previousProposalID = player_id;
                    }
                    else if (Client.previousProposalNumber == proposal_number) {
                        if (Client.previousProposalID < player_id) {
                            JSONObject response = new JSONObject();
                            response.put("status", "ok");
                            response.put("description", "accepted");
                            response.put("previous_accepted", Client.previousProposalID);
                            Client.sendUDP(response, addr, port);
                            
                            Client.previousProposalNumber = proposal_number;
                            Client.previousProposalID = player_id;
                        }
                        else {
                            JSONObject response = new JSONObject();
                            response.put("status", "fail");
                            response.put("description", "rejected");
                            Client.sendUDP(response, addr, port);
                        }
                    }
                    else {
                        JSONObject response = new JSONObject();
                        response.put("status", "fail");
                        response.put("description", "rejected");
                        Client.sendUDP(response, addr, port);
                    }
                }
                else if (method.equals("accept_proposal")) {
                    JSONArray proposal_id = input.getJSONArray("proposal_id");
                    int proposal_number = proposal_id.getInt(0);
                    int player_id = proposal_id.getInt(1);
                    
                    if (Client.previousProposalNumber < proposal_number) {
                        JSONObject response = new JSONObject();
                        response.put("status", "ok");
                        response.put("description", "accepted");
                        if (Client.previousProposalNumber != -1)
                            response.put("previous_accepted", Client.previousProposalID);
                        Client.sendUDP(response, addr, port);
                        
                        Client.previousProposalNumber = proposal_number;
                        Client.previousProposalID = player_id;
                        Client.kpuID = input.getInt("kpu_id");
                        Client.proposalsAccepted++;
                        
                        int localProposalsAccepted = Client.proposalsAccepted;
                        long startTime = System.nanoTime();
                        
                        while(true) {
                            if (Client.proposalsAccepted > localProposalsAccepted)
                                startTime = System.nanoTime();
                            
                            long elapsedTime = (System.nanoTime() - startTime) / 1000000;
                            
                            if (elapsedTime > 3000)
                                break;
                        }
                        Client.acceptedProposal();
                    }
                    else if (Client.previousProposalNumber == proposal_number) {
                        if (Client.previousProposalID < player_id) {
                            JSONObject response = new JSONObject();
                            response.put("status", "ok");
                            response.put("description", "accepted");
                            response.put("previous_accepted", Client.previousProposalID);
                            Client.sendUDP(response, addr, port);
                            
                            Client.previousProposalNumber = proposal_number;
                            Client.previousProposalID = player_id;
                            Client.kpuID = input.getInt("kpu_id");
                            Client.proposalsAccepted++;
                            
                            int localProposalsAccepted = Client.proposalsAccepted;
                            long startTime = System.nanoTime();

                            while(true) {
                                if (Client.proposalsAccepted > localProposalsAccepted)
                                    startTime = System.nanoTime();

                                long elapsedTime = (System.nanoTime() - startTime) / 1000000;

                                if (elapsedTime > 3000)
                                    break;
                            }
                            Client.acceptedProposal();
                        }
                        else {
                            JSONObject response = new JSONObject();
                            response.put("status", "fail");
                            response.put("description", "rejected");
                            Client.sendUDP(response, addr, port);
                        }
                    }
                    else {
                        JSONObject response = new JSONObject();
                        response.put("status", "fail");
                        response.put("description", "rejected");
                        Client.sendUDP(response, addr, port);
                    }
                }
                else if (method.equals("vote_werewolf")) {
                    int player_id = input.getInt("player_id");
                    
                    Client.votes.add(player_id);
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    response.put("description", "");
                    Client.sendUDP(response, addr, port);
                    
                    int werewolfsDead = 0;
                    for (Clients client : Client.clients) {
                        if (client.role.equals("werewolf")) {
                            werewolfsDead++;
                        }
                    }
                        
                    if (Client.votes.size() >= (2-werewolfsDead)) {
                        Client.sendVoteResultWerewolf();
                        Client.votes.clear();
                    }
                }
                else if (method.equals("vote_civilian")) {
                    int player_id = input.getInt("player_id");
                    
                    Client.votes.add(player_id);
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    response.put("description", "");
                    Client.sendUDP(response, addr, port);
                    
                    int clientsAlive = 0;
                    for (int i = 0; i < Client.clients.size(); i++) {
                        if (Client.clients.get(i).is_alive == 1)
                            clientsAlive++;
                    }
                        
                    if (Client.votes.size() >= clientsAlive) {
                        Client.sendVoteResultCivilian();
                        Client.votes.clear();
                    }
                }
            }
            else if (input.has("status")) {
                String status = input.getString("status");
                
                if (status.equals("ok")) {
                    if (input.has("player_id")) {
                        Client.playerID = input.getInt("player_id");
                        System.out.println("Joined game, your player id is " + Client.playerID);
                    }
                    if (input.has("description")) {
                        if (input.getString("description").equals("accepted")) {
                            Client.proposalNumber++;
                            if (Client.lastSent.has("method")) {
                                if (Client.lastSent.getString("method").equals("prepare_proposal"))
                                    Client.acceptProposal();
                            }
                        }
                    }
                    if (input.has("clients")) {
                        JSONArray clients = input.getJSONArray("clients");
                        System.out.println(clients.toString());
                        Client.clients.clear();
                        for (int i = 0; i < clients.length(); i++) {
                            JSONObject client = clients.getJSONObject(i);
                            Client.clients.add(new Clients(client));
                            
                            if (client.getInt("is_alive") == 0 && client.getInt("player_id") == Client.playerID) {
                                Client.isAlive = 0;
                                System.out.println("You are dead!");
                            }
                        }
                        
                        Client.proposalNumber = 1;
                        Client.previousProposalID = -1;
                        Client.previousProposalNumber = -1;
                        
                        switch (Client.time) {
                            case "day":
                                Client.prepareProposal();
                                break;
                            case "night":
                                Client.startVote();
                                break;
                        }
                    }
                }
                else if (status.equals("error")) {
                    //System.out.println("Error " + input.getString("description"));
                }
                else if (status.equals("fail")) {
                    //System.out.println("Fail: " + input.getString("description"));
                    if (input.has("description")) {
                        if (input.getString("description").equals("tidak kuorum")) {
                            Client.prepareProposal();
                        }
                    }
                }
            }
            join();
        }
        catch (JSONException | InterruptedException e) {
            System.out.println(e);
        }
    }
}
