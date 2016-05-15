import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Math.random;
import static java.lang.StrictMath.random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class Client extends Thread {
    public static int playerID;
    public static int kpuID;
    public static int kpuSelected;
    public static String role;
    public static int previousProposalNumber;
    public static int previousProposalID;
    public static Vector<Clients> clients;
    public static Vector<Integer> votes;
    public static int proposalNumber;
    public static String time;
    public static int proposalsAccepted;
    public static boolean isVoting;
    public static boolean hasVoted;
    
    public static String serverIP = "127.0.0.1";
    public static int serverPort = 9999;
    public static Socket socket;
    public static String udpAddress = "127.0.0.1";
    public static int udpPort;
    public static DatagramSocket udpSocket;
    public static PrintWriter os;
    public static String username;
    public static int isAlive;
    public static TCPListener tcpListener;
    public static UDPListener udpListener;
    public static Scanner consoleInput = new Scanner(System.in);
    
    public static JSONObject lastSent;
    
    public Client() {
        try {
            socket = new Socket(serverIP, serverPort);
            udpSocket = new DatagramSocket(udpPort);
            os = new PrintWriter(socket.getOutputStream(), true);
            
            isAlive = 1;
            proposalNumber = 1;
            previousProposalNumber = -1;
            previousProposalID = -1;
            proposalsAccepted = 0;
            isVoting = false;
            hasVoted = false;
            
            clients = new Vector<>();
            votes = new Vector<>();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    @Override
    public void run() {
        tcpListener = new TCPListener(socket);
        tcpListener.start();
            
        udpListener = new UDPListener(udpSocket);
        udpListener.start();
    }
    
    public static void joinGame() {
        String input = consoleInput.nextLine();
        while (!input.equalsIgnoreCase("join")) {
            input = consoleInput.nextLine();
        }
        if (input.equalsIgnoreCase("join")) {
            JSONObject outData = new JSONObject();
            try {
                outData.put("method", "join");
                outData.put("username", username);
                outData.put("udp_address", udpAddress);
                outData.put("udp_port", udpPort);

                sendTCP(outData);
            } catch (JSONException e) {
                System.out.println(e);
            }
        }
    }
    
    public static void ready(){
        String input = consoleInput.nextLine();
        while (!input.equalsIgnoreCase("ready")) {
            input = consoleInput.nextLine();
        }
        if (input.equalsIgnoreCase("ready")) {
            JSONObject data = new JSONObject();
            try {
                data.put("method", "ready");
                sendTCP(data);            
            } catch (JSONException e) {
                System.out.println(e);
            }
        }
    }
    
    public static void leave() {
        JSONObject data = new JSONObject();
        try {
            data.put("method", "leave");
            sendTCP(data);
        } catch (JSONException e) {
            System.out.println(e);
        }
    }
    
    public static void requestClients(){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "client_address");
            sendTCP(data);
        } catch (JSONException e) {
            System.out.println(e);
        }        
    }
    
    public static void prepareProposal(){
        if (playerID >= clients.size()-2) {
            JSONObject data = new JSONObject();
            JSONArray arr = new JSONArray();

            try {
                arr.put(proposalNumber);
                arr.put(playerID);
                data.put("method", "prepare_proposal");
                data.put("proposal_id", arr);

                for (Clients client : clients) {
                    InetAddress targetAddr = InetAddress.getByName(client.address);
                    sendUDP(data, targetAddr, client.port);
                }
            } catch (JSONException | UnknownHostException e) {
                System.out.println(e);
            }
        }
    }
    
    public static void acceptProposal(){
        if (playerID >= clients.size() - 2) {
            JSONObject data = new JSONObject();
            JSONArray arr = new JSONArray();
            try {
                arr.put(proposalNumber);
                arr.put(playerID);
                data.put("method", "accept_proposal");
                data.put("proposal_id", arr);
                data.put("kpu_id", playerID);

                for (Clients client : clients) {
                    InetAddress targetAddr = InetAddress.getByName(client.address);
                    sendUDP(data, targetAddr, client.port);
                }
            } catch (JSONException | UnknownHostException e) {
                System.out.println(e);
            }
        }
    }
    
    public static void acceptedProposal(){
        try {
            JSONObject data = new JSONObject();
            data.put("method", "accepted_proposal");
            data.put("kpu_id", kpuID);
            data.put("Description", "Kpu is selected");
            sendTCP(data);
        } catch (JSONException e) {
            System.out.println(e);
        }
    }
    
    public static void voteWerewolf(int id){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "vote_werewolf");
            data.put("player_id", id);
            sendUDPByID(data, kpuSelected);
        } catch (JSONException e) {
            System.out.println(e);
        }
    }
    
    public static void voteCivilian(int id){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "vote_civilian");
            data.put("player_id", id);
            sendUDPByID(data, kpuSelected);
        } catch (JSONException e) {
            System.out.println(e);
        }
    }
    
    public static void startVote() {
        if (isAlive == 1) {
            if (!isVoting) {
                isVoting = true;
                
                int id;
                
                switch (time) {
                    case "day":
                        System.out.println("Time\t: " + time);
                        System.out.println("Choose a civilian to kill\t: ");
                        id = consoleInput.nextInt();
                        consoleInput.nextLine();
                        voteCivilian(id);
                        isVoting = false;
                        break;
                    case "night":
                        if (role.equals("werewolf")) {
                            System.out.println("Time\t: " + time);
                            System.out.println("Choose a civilian to kill\t: ");
                            id = consoleInput.nextInt();
                            consoleInput.nextLine();
                            voteWerewolf(id);
                        }
                        else {
                            System.out.println("The werewolfs are voting...");
                        }
                        isVoting = false;
                        break;
                }
            }
        }
        else {
            System.out.println("You are dead, cannot vote");
        }
    }
    
    public static void sendVoteResultWerewolf() {
        try {
            int vote_status = -1;
            int mode = votes.get(0);
            int modeCount = 0;
            
            for (int i = 0; i < votes.size(); i++) {
                int curInt = votes.get(i);
                int count = 0;
                
                for (int j = 0; j < votes.size(); j++) {
                    if (curInt == votes.get(j))
                        count++;
                }
                
                if (count > modeCount) {
                    modeCount = count;
                    mode = curInt;
                    vote_status = 1;
                }
                else if (count == modeCount  && mode != curInt) {
                    vote_status = -1;
                }
            }
            
            JSONObject data = new JSONObject();
            data.put("method", "vote_result_werewolf");
            data.put("vote_status", vote_status);
            if (vote_status == 1) {
                data.put("player_killed", mode);
            }
            sendTCP(data);
        } catch (JSONException e) {
            System.out.println(e);
        }
    }
    
    public static void sendVoteResultCivilian() {
        try {
            int vote_status = -1;
            int mode = votes.get(0);
            int modeCount = 0;
            
            for (int i = 0; i < votes.size(); i++) {
                int curInt = votes.get(i);
                int count = 0;
                
                for (int j = 0; j < votes.size(); j++) {
                    if (curInt == votes.get(j))
                        count++;
                }
                
                if (count > modeCount) {
                    modeCount = count;
                    mode = curInt;
                    vote_status = 1;
                }
                else if (count == modeCount && mode != curInt) {
                    vote_status = -1;
                }
            }
            
            JSONObject data = new JSONObject();
            data.put("method", "vote_result_civilian");
            data.put("vote_status", vote_status);
            if (vote_status == 1) {
                data.put("player_killed", mode);
            }
            sendTCP(data);
        } catch (JSONException e) {
            System.out.println(e);
        }        
    }
    
    public static void sendTCP(JSONObject data) {
        os.println(data.toString());
        if (!data.has("status"))
            lastSent = data;
    }
    
    public static void sendUDP(JSONObject data, InetAddress addr, int port) {
        Random random = new Random();
        try {
            byte[] sendData = data.toString().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, addr, port);
            double rand = random.nextDouble();
		if (rand < 0.85) {
			udpSocket.send(sendPacket);
                        if (!data.has("status"))
                            lastSent = data;
                }
        }
        catch (SocketException e) {
            System.out.println(e);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public static void sendUDPByID(JSONObject data, int id) {
        Random random = new Random();
        try {
            for (Clients c : clients) {
                if (c.player_id == id) {
                    byte[] sendData = data.toString().getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(c.address), c.port);
                    double rand = random.nextDouble();
                    if (rand < 0.85) {
			udpSocket.send(sendPacket);
                        if (!data.has("status"))
                            lastSent = data;
                    }
                }
            }            
        }
        catch (SocketException e) {
            System.out.println(e);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public static void main(String[] args) {
        System.out.print("Input username\t: ");
        username = consoleInput.nextLine();
        
        /*
        System.out.print("Input server address\t: ");
        serverIP = consoleInput.nextLine();
        
        System.out.print("Input server port\t: ");
        serverPort = consoleInput.nextInt();
        consoleInput.nextLine();
        
        System.out.print("Input udp address\t: ");
        udpAddress = consoleInput.nextLine();
        */
        
        System.out.print("Input udp port\t: ");
        udpPort = consoleInput.nextInt();
        consoleInput.nextLine();
        
        Client c = new Client();
        c.start();
        
        joinGame();
        ready();
        while(true) {
            
        }
    }
}