import java.io.*;
import java.net.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Player extends Thread {
    public String username;
    public String udp_address;
    public int udp_port;
    public int is_alive;
    public String role;
    public boolean ready;
    
    public Socket socket;
    public PrintWriter out;

    public Player(Socket s) {
        socket = s;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {
            System.out.println("Player error: " + e);
        }
    }

    public void run() {
        try {
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            while (true) {
                line = in.readLine();
                process(line);
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void sendToAll(String command) {
        for (int i = 0; i < Server.playerList.size(); i++) {
            Server.playerList.get(i).out.println(command);
        }
    }
    
    public String isGameOver() {
        int n_werewolf = 0;
        int n_civilian = 0;
        
        for (int i = 0; i < Server.playerList.size(); i++) {
            if (Server.playerList.get(i).is_alive == 1) {
                if (Server.playerList.get(i).role.equals("werewolf"))
                    n_werewolf++;
                else
                    n_civilian++;
            }
        }
        
        if (n_civilian <= n_werewolf)
            return "werewolf";
        else if (n_werewolf == 0)
            return "civilian";
        else
            return "no";
    }
    
    public void gameOver(String winner) {
        try {
            JSONObject response = new JSONObject();
            
            response.put("method", "game_over");
            response.put("winner", winner);
            response.put("description", "");
            sendToAll(response.toString());
            
        }
        catch(JSONException e) {
            System.out.println(e);
        }
    }
    
    public void killPlayer(int id) {
        Server.playerList.get(id).is_alive = 0;
    }
    
    public void changePhase() {
        try {
            if (isGameOver().equals("werewolf")) {
                gameOver("werewolf");
            }
            else if (isGameOver().equals("civilian")) {
                gameOver("civilian");
            }
            else {
                JSONObject response = new JSONObject();
                response.put("method", "change_phase");

                if (Server.time.equals("day"))
                    Server.time = "night";
                else if (Server.time.equals("night"))
                    Server.time = "day";

                Server.days++;

                response.put("time", Server.time);
                response.put("days", Server.days);
                response.put("description", "");

                sendToAll(response.toString());
                Server.civilianVoteCount = 0;
            }
        }
        catch (JSONException e) {
            System.out.println(e);
        }
    }
    
    public void voteNow() {
        try {
            JSONObject response = new JSONObject();
            response.put("method", "vote_now");
            response.put("phase", Server.time);
            sendToAll(response.toString());
        }
        catch (JSONException e) {
            System.out.println(e);
        }
    }

    public void process(String input) {
        try {
            JSONObject request = new JSONObject(input);
            String method = request.getString("method");
            System.out.println(input);
            
            if (method.equals("join")) {
                username = request.getString("username");
                Server.nextId++;
                
                udp_address = request.getString("udp_address");
                udp_port = request.getInt("udp_port");
                is_alive = 1;
                ready = false;
                
                Server.playerList.add(this);
                
                if (Server.playerList.indexOf(this) == 2 || Server.playerList.indexOf(this) == 3)
                    role = "werewolf";
                else
                    role = "civilian";                
                
                JSONObject response = new JSONObject();
                response.put("status", "ok");
                response.put("player_id", Server.playerList.indexOf(this));
                out.println(response.toString());
            }
            else if (method.equals("ready")) {
                ready = true;
                JSONObject response = new JSONObject();
                response.put("status", "ok");
                response.put("description", "waiting for other player to start");
                out.println(response.toString());

                int readyPlayers = 0;
                for (int i = 0; i < Server.playerList.size(); i++) {
                    if (Server.playerList.get(i).ready) {
                        readyPlayers++;
                    }                        
                }
                if (readyPlayers == Server.playerList.size() && readyPlayers >= 6) {
                    Server.startGame();
                }
            }
            else if (method.equals("leave")) {
                if (!Server.gameStarted) {
                    ready = false;
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    out.println(response.toString());
                    Server.deletePlayer(this);
                    
                }
                else {
                    JSONObject response = new JSONObject();
                    response.put("status", "fail");
                    response.put("description", "Failed to leave game");
                    out.println(response.toString());
                }
            }
            else if (method.equals("client_address")) {
                JSONObject response = new JSONObject();
                response.put("status", "ok");
                
                JSONArray arr = new JSONArray();
                for (int i = 0; i < Server.playerList.size(); i++) {
                    Player p = Server.playerList.get(i);
                    
                    JSONObject temp = new JSONObject();
                    temp.put("player_id", i);
                    temp.put("is_alive", p.is_alive);
                    temp.put("address", p.udp_address);
                    temp.put("port", p.udp_port);
                    temp.put("username", p.username);
                    if (p.is_alive == 0) {
                        temp.put("role", p.role);
                    }
                    
                    arr.put(temp);
                }
                
                response.put("clients", arr);
                response.put("description", "list of clients retrieved");
                out.println(response.toString());
            }
            else if (method.equals("accepted_proposal")) {
                int kpuId = request.getInt("kpu_id");
                Server.acceptedProposal.add(kpuId);
                
                JSONObject response = new JSONObject();
                response.put("status", "ok");
                response.put("description", "");
                out.println(response.toString());
                
                if (Server.acceptedProposal.size() >= Server.playerList.size()-2) {
                    int mode = Server.acceptedProposal.get(0);
                    int modeCount = 0;
                    
                    for(int i = 0; i < Server.acceptedProposal.size(); i++) {
                        int curInt = Server.acceptedProposal.get(i);
                        int count = 0;
                        
                        for (int j = 0; j < Server.acceptedProposal.size(); j++) {
                            if (Server.acceptedProposal.get(j) == curInt)
                                count++;
                        }
                        
                        if (count > modeCount) {
                            modeCount = count;
                            mode = curInt;
                        }
                        else if (count == modeCount && curInt > mode) {
                            mode = curInt;
                        }
                    }
                    
                    response = new JSONObject();
                    
                    if (Server.playerList.size() % 2 == 0) {
                        if (modeCount > Server.playerList.size()/2) {
                            response.put("method", "kpu_selected");
                            response.put("kpu_id", mode);
                        }
                        else {
                            response.put("status", "fail");
                            response.put("description", "tidak kuorum");
                        }
                    }
                    else {
                        if (modeCount >= (Server.playerList.size()+1)/2) {
                            response.put("method", "kpu_selected");
                            response.put("kpu_id", mode);
                        }
                        else {
                            response.put("status", "fail");
                            response.put("description", "tidak kuorum");
                        }
                    }                    
                    out.println(response.toString());
                }
            }
            else if (method.equals("vote_result_werewolf")) {
                int vote_status = request.getInt("vote_status");
                if (vote_status == 1) {
                    int player_killed = request.getInt("player_killed");
                    killPlayer(player_killed);
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    response.put("description", "");
                    out.println(response.toString());
                    
                    changePhase();
                }
                else if (vote_status == -1) {
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    response.put("description", "");
                    out.println(response.toString());
                    
                    try {
                        sleep(1000);
                    }
                    catch (InterruptedException e) {
                        System.out.println(e);
                    }
                    
                    voteNow();
                }
            }
            else if (method.equals("vote_result_civilian")) {
                Server.civilianVoteCount++;
                int vote_status = request.getInt("vote_status");
                
                if (vote_status == 1) {
                    int player_killed = request.getInt("player_killed");
                    killPlayer(player_killed);
                    
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    response.put("description", "");
                    out.println(response.toString());
                    
                    changePhase();
                }
                else if (vote_status == -1) {
                    JSONObject response = new JSONObject();
                    response.put("status", "ok");
                    response.put("description", "");
                    out.println(response.toString());
                    
                    try {
                        sleep(1000);
                    }
                    catch (InterruptedException e) {
                        System.out.println(e);
                    }
                    
                    if (Server.civilianVoteCount < 2) {
                        voteNow();
                    }
                    else {
                        changePhase();
                    }
                }
            }
        }
        catch (JSONException e) {
            System.out.println(e);
        }
    }
}