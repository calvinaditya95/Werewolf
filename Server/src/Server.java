import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

public class Server {
    private static int port;
    private static ServerSocket serverSocket;
    private static Scanner consoleInput = new Scanner(System.in);
    public static ArrayList<Player> playerList;
    public static String time;
    public static int days;
    public static int civilianVoteCount;
    public static boolean gameStarted;
    
    public void startGame() {
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
                        if (playerList.get(i).id != playerList.get(j).id && playerList.get(i).role.equals("werewolf")) {
                            arr.put(playerList.get(i).username);
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
        playerList = new ArrayList<>();
        time = "day";
        days = 1;
        civilianVoteCount = 0;
        gameStarted = false;

        System.out.print("Enter port number\t: ");
        port = consoleInput.nextInt();

        try {
            serverSocket = new ServerSocket(port);

            while(true) {
                Socket clientSocket = serverSocket.accept();

                Player p = new Player(clientSocket);
                playerList.add(p);
                p.start();
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}