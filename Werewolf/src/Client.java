import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    private String serverIP;
    private int serverPort;
    private BufferedReader is = null;
    private Socket socket = null;  
    private PrintWriter os = null;
    private Vector<Client> clients = new Vector();
    private int kpuID  = 0;
    private int playerID;
    private String username;
    private String myAddress;
    private int myPort;
    private int status;
    private int race = 0; //0-1
    private int role = 0; //0-2
    private int prevProposalID = 0;
    private int proposalNumber = 0;
    
    /**
     * Create client with server socket
     * @param addr server IP Address
     * @param port server port
     */
    public Client(String username, String addr, int port) {
        try {
            socket = new Socket(addr, port);
            os = new PrintWriter(socket.getOutputStream(), true);
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            this.username = username;
            this.status = 1;
            this.myAddress = InetAddress.getLocalHost().getHostAddress();
            this.myPort = 9876;
            this.serverIP = addr;
            this.serverPort = port;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setRace (int x) {
        this.race = x;
    }
    
    public void setRole (int x) {
        this.role = x;
    }
    
    public void setStatus (int x) {
        this.status = x;
    }
    
    public void setMyAddress (String x) {
        this.myAddress = x;
    }
    
    public void setMyPort (int x) {
        this.myPort = x;
    }
    
    public void setID (int x) {
        this.playerID = x;
    }
    
    public int getRace() {
        return this.race;
    }
    
    public int getRole() {
        return this.role;
    }
    
    public int getStatus() {
        return this.status;
    }
    
    public String getMyAddress() {
        return this.myAddress;
    }
    
    public int getMyPort() {
        return this.myPort;
    }
    
    public int getID() {
        return this.playerID;
    }
    
    /**
     * Join Game
     * @param username username of the client
     * @param addr use inetaddress.getlocalhost() to get local IP Address
     * @param port connection port
     */
    private void join() {
        JSONObject outData = new JSONObject();
        JSONObject inData = new JSONObject();
        boolean fail = true;
        try {
            outData.put("method", "join");
            outData.put("username", this.username);
            outData.put("udp_address", this.myAddress);
            outData.put("udp_port", this.myPort);
            
            while (fail) {
                sendTCP(outData);
                inData = receiveTCP();

                String status = inData.getString("status");

                if (status.equals("ok")) {
                    this.playerID = inData.getInt("player_id");
                    fail = !fail;
                }
                else if (status.equals("fail")) {
                    System.out.println(inData.getString("description"));
                }
                else if (status.equals("error")){
                    System.out.println(inData.getString("description"));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Send JSONObject to server
     * @param data JSONObject to be sent
     */
    private void sendTCP(JSONObject data) {
        os.println(data.toString());
    }
    
    private void sendUDP(JSONObject data, InetAddress addr, int port) {
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            byte[] sendData = data.toString().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, addr, port);
            udpSocket.send(sendPacket);
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private JSONObject receiveTCP() {
        JSONObject obj = new JSONObject();
        try {
            obj = new JSONObject(is.readLine());
            System.out.println(obj);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return obj;
    }
    
    private DatagramPacket receiveUDP() {
        int listenPort = 9876;
        DatagramSocket serverSocket;
        try {
            serverSocket = new DatagramSocket(listenPort);
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            return receivePacket;
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    private void leave() {
        
        JSONObject data = new JSONObject();
        JSONObject response = new JSONObject();
        boolean fail = true;
        try {
            data.put("method", "leave");
            
            while (fail) {
                sendTCP(data);
                response = receiveTCP();
                String status = response.getString("status");

                if (status.equals("ok")) {
                    fail = !fail;
                }
                else if (status.equals("fail")) {
                    System.out.println(response.getString("description"));
                }
                else {
                    System.out.println(response.getString("description"));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
    }
    
    private void ready(){
        JSONObject data = new JSONObject();
        JSONObject response = new JSONObject();
        boolean fail = true;
        
        try {
            data.put("method", "ready");
            
            while (fail) {
                sendTCP(data);
                response = receiveTCP();
                String status = response.getString("status");
                
                if (status.equals("ok")) {
                    fail = !fail;
                }
                else {
                    System.out.println(response.getString("description"));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void requestClients(){
        JSONObject data = new JSONObject();
        JSONObject response = new JSONObject();
        JSONArray clientJSON = new JSONArray();
        boolean fail = true;
        try {
            data.put("method", "client_address");
            
            while (fail) {
                sendTCP(data);
                response = receiveTCP();
                String status = response.getString("status");
                
                if (status.equals("ok")) {
                    fail = !fail;
                    clientJSON = response.getJSONArray("clients");
                    
                    for (int i=0; i<clientJSON.length(); i++) {
                        JSONObject temp = clientJSON.getJSONObject(i);
                        Client tempClient = new Client(temp.getString("username"), this.serverIP, this.serverPort);
                        tempClient.setMyAddress(temp.getString("address"));
                        tempClient.setMyPort(temp.getInt("port"));
                        tempClient.setStatus(temp.getInt("is_alive"));
                        tempClient.setID(temp.getInt("player_id"));
                        if (temp.has("role")) {
                            tempClient.setRace(1);
                        }
                        clients.add(tempClient);
                    }
                }
                else if (status.equals("fail")) {
                    System.out.println(response.getString("description"));
                }
                else {
                    System.out.println(response.getString("description"));
                } 
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void prepareProposal(){
        JSONObject data = new JSONObject();
        JSONObject responseJSON;
        
        byte[] receiveData = new byte[1024];
        DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);

        int count = 0;
        int playerCount = 0;
        try {
            data.put("method", "prepare_proposal");
            data.put("proposal_id", "("+proposalNumber+","+playerID+")");
            
            for (int i=0; i<clients.size(); i++) {
                if (clients.get(i).getStatus() == 1) {
                    playerCount++;
                    InetAddress targetAddr = InetAddress.getByName(clients.get(i).getMyAddress());
                    sendUDP(data, targetAddr, clients.get(i).getMyPort());
                }
            }
            
            this.proposalNumber++;            
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void acceptProposalPaxos(){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "accept_proposal");
            data.put("proposal_id", "("+proposalNumber+","+playerID+")");
            data.put("kpu_id", "1");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendTCP(data);
    }
    
    private void acceptProposalClient(){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "accepted_proposal");
            data.put("kpu_id", playerID);
            data.put("Description", "Kpu is selected");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendTCP(data);
    }
    
    private void voteWerewolf(int id){
        JSONObject data = new JSONObject();
        JSONObject responseJSON = new JSONObject();
        boolean fail = true;
        try {
            data.put("method", "vote_werewolf");
            data.put("player_id", id);
            int idx = searchByPlayerID(kpuID);
            InetAddress targetAddr = InetAddress.getByName(clients.get(idx).getMyAddress());
            sendUDP(data, targetAddr, clients.get(idx).getMyPort());
            
            while (fail) {
                responseJSON = parseToJSON(receiveUDP());
                String status = responseJSON.getString("status");
                
                if (status.equals("ok")) {
                    fail = !fail;
                }
                else if (status.equals("fail")) {
                    System.out.println("FAIL");
                }
                else if (status.equals("error")) {
                    System.out.println("ERROR");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveWerewolfVote() {
        int playerCount = 0;
        int messageCount = 0;
        boolean dead = false;
        int idx = 0;
        String vote_result = "[ ";
        Vector<Integer> voteCount = new Vector();
        JSONObject data = new JSONObject();
        JSONObject responseJSON = new JSONObject();
        
        for(int i=0; i<clients.size(); i++) {
            voteCount.add(0);
            if (clients.get(i).getRace() == 1) {
                if (clients.get(i).getStatus() == 1) {
                    playerCount++;
                }
            }
        }
        try {
            while (messageCount != playerCount) {
                responseJSON = parseToJSON(receiveUDP());

                String method = responseJSON.getString("method");
                if (method.equals("vote_werewolf")) {
                    messageCount++;
                    int a = voteCount.get(responseJSON.getInt("player_id"));
                    voteCount.set(responseJSON.getInt("player_id"), a+1);
                }
            }
            
            data.put("status", "ok");
            data.put("description", "");
            
            for (int i=0; i<clients.size(); i++) {
                if (clients.get(i).getRace() == 1) {
                    if (clients.get(i).getStatus() == 1) {
                        InetAddress targetAddr = InetAddress.getByName(clients.get(i).getMyAddress());
                        sendUDP(data, targetAddr, clients.get(i).getMyPort());
                    }
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
                infoWerewolf(-1, -1, vote_result);
            }
            else {
                infoWerewolf(idx, 1, vote_result);
            }
        }
        catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void killCivilian(int id){
        JSONObject data = new JSONObject();
        JSONObject responseJSON = new JSONObject();
        boolean fail = true;
        try {
            data.put("method", "vote_civilian");
            data.put("player_id", id);
            int idx = searchByPlayerID(kpuID);
            InetAddress targetAddr = InetAddress.getByName(clients.get(idx).getMyAddress());
            sendUDP(data, targetAddr, clients.get(idx).getMyPort());
            
            while (fail) {
                responseJSON = parseToJSON(receiveUDP());
                String status = responseJSON.getString("status");
                
                if (status.equals("ok")) {
                    fail = !fail;
                }
                else if (status.equals("fail")) {
                    System.out.println("FAIL");
                }
                else if (status.equals("error")) {
                    System.out.println("ERROR");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void infoWerewolf(int player_id, int vote_status, String vote_result){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "vote_result_werewolf");
            data.put("vote_status", vote_status);
            data.put("vote_result", vote_result);
            if (vote_status == 1) {
                data.put("player_killed", player_id);
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendTCP(data);
    }
    
    private void receiveCivilianVote() {
        int playerCount = 0;
        int messageCount = 0;
        boolean dead = false;
        int idx = 0;
        String vote_result = "[ ";
        Vector<Integer> voteCount = new Vector();
        JSONObject data = new JSONObject();
        JSONObject responseJSON = new JSONObject();
        
        for(int i=0; i<clients.size(); i++) {
            voteCount.add(0);
            if (clients.get(i).getStatus() == 1) {
                playerCount++;
            }
        }
        try {
            while (messageCount != playerCount) {
                responseJSON = parseToJSON(receiveUDP());

                String method = responseJSON.getString("method");
                if (method.equals("vote_civilian")) {
                    messageCount++;
                    int a = voteCount.get(responseJSON.getInt("player_id"));
                    voteCount.set(responseJSON.getInt("player_id"), a+1);
                }
            }
            
            data.put("status", "ok");
            data.put("description", "");
            
            for (int i=0; i<clients.size(); i++) {
                if (clients.get(i).getStatus() == 1) {
                    InetAddress targetAddr = InetAddress.getByName(clients.get(i).getMyAddress());
                    sendUDP(data, targetAddr, clients.get(i).getMyPort());
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
                infoCivilian(-1, -1, vote_result);
            }
            else {
                infoCivilian(idx, 1, vote_result);
            }
        }
        catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void infoCivilian(int player_id, int vote_status, String vote_result){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "vote_result_civilian");
            data.put("vote_status", vote_status);
            data.put("vote_result", vote_result);
            if (vote_status == 1) {
                data.put("player_killed", player_id);
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendTCP(data);
    }
    
    public static JSONObject parseToJSON(DatagramPacket packet) {
        String sentence = new String(packet.getData(), 0, packet.getLength());
        try {
            return new JSONObject(sentence);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    private int searchByPlayerID(int id) {
        for (int i=0; i<clients.size(); i++) {
            if (clients.get(i).getID() == id) {
                return i;
            }
        }
        return -1;
    }
    
/**
 * Contoh kode program untuk node yang mengirimkan paket. Paket dikirim
 * menggunakan UnreliableSender untuk mensimulasikan paket yang hilang.
 */
    public static void main(String args[]) throws Exception
    {
        Scanner in = new Scanner(System.in);
        System.out.print("Server IP: ");
        String addr = in.nextLine();
        System.out.print("Port: ");
        int port = in.nextInt();
        Client c = new Client("client1", addr, port);
        c.join();
        System.out.println("1st phase");
        c.requestClients();
        System.out.println("2nd phase");
        c.voteWerewolf(0);
        System.out.println("3rd phase");
        c.receiveWerewolfVote();
        System.out.println("4th phase");
        while (true) {
            
        }
        /*
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        String targetAddress = "localhost";
        InetAddress IPAddress = InetAddress.getByName(targetAddress);
        int targetPort = 9876;

        DatagramSocket datagramSocket = new DatagramSocket();
        UnreliableSender unreliableSender = new UnreliableSender(datagramSocket);

        while (true)
        {
                String sentence = inFromUser.readLine();
                if (sentence.equals("quit"))
                {
                        break;
                }

                byte[] sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, targetPort);
                unreliableSender.send(sendPacket);
        }
        datagramSocket.close();
        */
    }
}
