import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

public class Server {
    private static int port;
    private static ServerSocket serverSocket;
    private static Scanner consoleInput = new Scanner(System.in);
    public static Vector<Player> playerList;
    public static Vector<Integer> acceptedProposal;
    public static String time;
    public static int days;
    public static int civilianVoteCount;
    public static boolean gameStarted;
    public static int nextId;
    
    public static void deletePlayer(Player p) {
        playerList.remove(p);
    }
    
    public static void startGame() {
        try {
            gameStarted = true;
            
            for (int i = 0; i < playerList.size(); i++) {
                JSONObject response = new JSONObject();
                response.put("method", "start");
                response.put("time", Server.time);
                response.put("role", playerList.get(i).role);
                
                JSONArray arr = new JSONArray();
                if (playerList.get(i).role.equals("werewolf")) {
                    for (int j = 0; j < playerList.size(); j++) {
                        if (i != j && playerList.get(j).role.equals("werewolf")) {
                            arr.put(playerList.get(j).username);
                        }
                    }
                }
                
                response.put("friend", arr);
                playerList.get(i).out.println(response.toString());
            }
        }
        catch (JSONException e) {
            System.out.println(e);
        }
    }
    
    public static void main(String args[]) {
        playerList = new Vector<>();
        acceptedProposal = new Vector<>();
        time = "day";
        days = 1;
        civilianVoteCount = 0;
        nextId = 0;
        gameStarted = false;

        /*
        System.out.print("Enter port number\t: ");
        port = consoleInput.nextInt();
        */
        
        port = 9999;

        try {
            serverSocket = new ServerSocket(port);

            while(true) {
                Socket clientSocket = serverSocket.accept();

                Player p = new Player(clientSocket);
                p.start();
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}