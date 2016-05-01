import java.io.*;
import java.net.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Player extends Thread {
    public int id;
    public String username;
    public String udp_address;
    public int udp_port;
    public int is_alive;
    public String role;
    
    public Socket socket;
    public PrintWriter out;
    private BufferedReader in;

    public Player(Socket s) {
        socket = s;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (IOException e) {
            System.out.println("Player error: " + e);
        }
    }

    public void run() {
        String line;

        try {
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
        for (int i = 0; i < Server.playerList.size(); i++) {
            if (Server.playerList.get(i).id == id) {
                Server.playerList.get(i).is_alive = 0;
            }
        }
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
            
            JSONObject response = new JSONObject();
            
            if (method.equals("join")) {
                System.out.println(input);
                username = request.getString("username");
                id = Server.playerList.indexOf(this);
                udp_address = request.getString("udp_address");
                udp_port = request.getInt("udp_port");
                is_alive = 1;
                if (id == 2 || id == 3)
                    role = "werewolf";
                else
                    role = "civilian";
                
                response.put("status", "ok");
                response.put("player_id", id);
                out.println(response.toString());
            }
            else if (method.equals("leave")) {
                response.put("status", "ok");
                out.println(response.toString());
            }
            else if (method.equals("ready")) {
                response.put("status", "ok");
                response.put("description", "waiting for other player to start");
                out.println(response.toString());
            }
            else if (method.equals("client_address")) {
                response.put("status", "ok");
                
                JSONArray arr = new JSONArray();
                for (int i = 0; i < Server.playerList.size(); i++) {
                    Player p = Server.playerList.get(i);
                    
                    JSONObject temp = new JSONObject();
                    temp.put("player_id", p.id);
                    temp.put("is_alive", p.is_alive);
                    temp.put("address", p.udp_address);
                    temp.put("port", p.udp_port);
                    temp.put("username", p.username);
                    if (p.role.equals("werewolf")) {
                        temp.put("role", "werewolf");
                    }
                    
                    arr.put(temp.toString());
                }
                
                response.put("clients", arr);
                response.put("description", "list of clients retrieved");
                out.println(response.toString());
            }
            else if (method.equals("vote_result_werewolf")) {
                int vote_status = request.getInt("vote_status");
                if (vote_status == 1) {
                    int player_killed = request.getInt("player_killed");
                    killPlayer(player_killed);
                    
                    response.put("status", "ok");
                    response.put("description", "");
                    out.println(response.toString());
                    
                    changePhase();
                }
                else if (vote_status == -1) {
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
                    
                    response.put("status", "ok");
                    response.put("description", "");
                    out.println(response.toString());
                    
                    changePhase();
                }
                else if (vote_status == -1) {
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