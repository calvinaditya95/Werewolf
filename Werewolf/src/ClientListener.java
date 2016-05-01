
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientListener extends Thread {
    private int messageNum = 0;
    private int myPort;
    
    public ClientListener(int i) {
        this.myPort = i;
    }
    
    public int getPort(){
        return myPort;
    }
    
    private void receiveWerewolfVote(JSONObject obj) {
        int playerCount = 2;
        int messageCount = 0;
        boolean dead = false;
        int idx = 0;
        String vote_result = "[ ";
        Vector<Integer> voteCount = new Vector<Integer>();
        JSONObject responseJSON = obj;
        
        for (int i=0; i<Client.clients.size(); i++) {
            System.out.println(Client.clients.size());
            voteCount.add(0);
            if (Client.clients.get(i).getRace() == 1) {
                playerCount = 1;
            }
        }
        
        try {
            messageCount++;
            System.out.println("1st count");
            System.out.println(responseJSON.getInt("player_id"));
            int a = voteCount.get(responseJSON.getInt("player_id"));
            voteCount.set(responseJSON.getInt("player_id"), a+1);
            
            while (messageCount != playerCount) {
                responseJSON = Client.parseToJSON(Client.receiveUDP());

                String method = responseJSON.getString("method");
                if (method.equals("vote_werewolf")) {
                    messageCount++;
                    System.out.println("2nd count");
                    a = voteCount.get(responseJSON.getInt("player_id"));
                    voteCount.set(responseJSON.getInt("player_id"), a+1);
                }
            }
            
            for (int i=0; i<voteCount.size(); i++) {
                vote_result += "["+ i + ", " + voteCount.get(i) + "] ";
                int x = voteCount.get(i);
                if (x == playerCount) {
                    idx = i;
                    dead = true;
                }
            }
            
            vote_result += "]";
            
            if (!dead) {
                Client.infoWerewolf(-1, -1, vote_result);
            }
            else {
                Client.infoWerewolf(idx, 1, vote_result);
            }
        }
        catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveCivilianVote(JSONObject obj) {
        int playerCount = 6;
        int messageCount = 0;
        boolean dead = false;
        int idx = 0;
        String vote_result = "[ ";
        Vector<Integer> voteCount = new Vector();
        JSONObject responseJSON = obj;
        
        for(int i=0; i<Client.clients.size(); i++) {
            voteCount.add(0);
            if (Client.clients.get(i).getStatus() == 0) {
                playerCount--;
            }
        }
        try {
            messageCount++;
            int a = voteCount.get(responseJSON.getInt("player_id"));
            voteCount.set(responseJSON.getInt("player_id"), a+1);
            
            while (messageCount != playerCount) {
                responseJSON = Client.parseToJSON(Client.receiveUDP());

                String method = responseJSON.getString("method");
                if (method.equals("vote_civilian")) {
                    messageCount++;
                    a = voteCount.get(responseJSON.getInt("player_id"));
                    voteCount.set(responseJSON.getInt("player_id"), a+1);
                }
            }
            
            int max = -1;
            int secondMax = -1;
            for (int i=0; i<voteCount.size(); i++) {
                vote_result += "["+ i + ", " + voteCount.get(i) + "] ";
                int x = voteCount.get(i);
                if (max <= x) {
                    secondMax= max;
                    max = x;
                    idx = i;
                }
            }            
            
            if (secondMax != max) {
                dead = true;
            }
            
            vote_result += "]";
            
            if (!dead) {
                Client.infoCivilian(-1, -1, vote_result);
            }
            else {
                Client.infoCivilian(idx, 1, vote_result);
            }
        }
        catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void listen() throws JSONException {
        DatagramPacket response = null;
        JSONObject responseJSON = new JSONObject();
        int kpu_id = -1;
        
        response = Client.receiveUDP();
        responseJSON = Client.parseToJSON(response);
        if(responseJSON.has("method")){
            String method = responseJSON.getString("method");
            if (method.equals("prepare_proposal")){
                JSONArray temp = responseJSON.getJSONArray("proposal_id");
                int a = temp.getInt(0);
                int b = temp.getInt(1);
                
                response = Client.receiveUDP();
                responseJSON = Client.parseToJSON(response);
                JSONArray temp2 = responseJSON.getJSONArray("proposal_id");
                int c = temp2.getInt(0);
                int d = temp2.getInt(1);
                
                int prev = Client.getPrevProposalID();
                
                JSONObject dataOK = new JSONObject();
                dataOK.put("status", "ok");
                dataOK.put("description", "accepted");
                dataOK.put("previous_accepted", prev);
                
                JSONObject dataFail = new JSONObject();
                dataOK.put("status", "fail");
                dataOK.put("description", "rejected");
                if( a > c ){
                    kpu_id = b;
                    Client.sendToID(b, dataOK);
                    Client.sendToID(d, dataFail);
                }else if(a < c){
                    kpu_id = d;
                    Client.sendToID(d, dataOK);
                    Client.sendToID(b, dataFail);
                }else{
                    if(b > d){
                        kpu_id = b;
                        Client.sendToID(b, dataOK);
                        Client.sendToID(d, dataFail);
                    }else if(b < d){
                        kpu_id = d;
                        Client.sendToID(b, dataFail);
                        Client.sendToID(d, dataOK);
                    }
                }
                Client.setPrevProposalID(kpu_id);
            }
            else if (method.equals("accept_proposal")) {
                JSONArray temp = responseJSON.getJSONArray("proposal_id");
                int b = temp.getInt(1);
                if (b == Client.getPrevProposalID()) {
                    JSONObject dataOK = new JSONObject();
                    dataOK.put("status", "ok");
                    dataOK.put("description", "accepted");
                    Client.sendToID(b, dataOK);
                }
                else {
                    JSONObject dataFail = new JSONObject();
                    dataFail.put("status", "fail");
                    dataFail.put("description", "rejected");
                    Client.sendToID(b, dataFail);
                }
            }
            else if (method.equals("vote_werewolf")) {
                receiveWerewolfVote(responseJSON);
            }
            else if (method.equals("vote_civilian")) {
                receiveCivilianVote(responseJSON);
            }
        }
    }    
    @Override
    public void run() {
        while (true) {
            try {
                listen();
            } catch (JSONException ex) {
                Logger.getLogger(ClientListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}