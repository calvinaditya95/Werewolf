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
    private boolean start = false;
    private String serverIP;
    private int serverPort;
    private BufferedReader is = null;
    private Socket socket = null;  
    private static PrintWriter os = null;
    public static Vector<Client> clients = new Vector();
    public static int kpuID  = -1;
    private int playerID;
    private String username;
    private String myAddress;
    private int myPort;
    private int status;
    private int race = 0; //0-1
    private int role = 0; //0-2
    public static int prevProposalID = 0;
    private int proposalNumber = 0;
    
    /**
     * Create client with server socket
     * @param addr server IP Address
     * @param port server port
     */
    public Client(String username, String addr, int port, int udpPort) {
        try {
            socket = new Socket(addr, port);
            os = new PrintWriter(socket.getOutputStream(), true);
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ClientListener cl = new ClientListener(udpPort);
            cl.start();
            
            this.username = username;
            this.status = 1;
            this.myAddress = InetAddress.getLocalHost().getHostAddress();
            this.myPort = udpPort;
            this.serverIP = addr;
            this.serverPort = port;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isStart() {
        return start;
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
    
    public static void setPrevProposalID(int x){
        prevProposalID = x;
    }
    
    public static void setKpuID(int x) {
        kpuID = x;
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
    
    public static int getPrevProposalID() {
        return prevProposalID;
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
    private static void sendTCP(JSONObject data) {
        os.println(data.toString());
    }
    
    private static void sendUDP(JSONObject data, InetAddress addr, int port) {
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
    
    private DatagramPacket receiveUDP(int listenPort) {
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
            
            response = receiveTCP();
            if (response.getString("method").equals("start")) {
                start = true;
                if (response.getString("role").equals("werewolf")) {
                    this.role = 1;
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
                        Client tempClient = new Client(temp.getString("username"), this.serverIP, this.serverPort, temp.getInt("port"));
                        tempClient.setMyAddress(temp.getString("address"));
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
        JSONArray arr = new JSONArray();
        byte[] receiveData = new byte[1024];
        DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);

        try {
            arr.put(proposalNumber);
            arr.put(playerID);
            data.put("method", "prepare_proposal");
            data.put("proposal_id", arr);
            
            for (int i=0; i<clients.size(); i++) {
                if (clients.get(i).getStatus() == 1) {
                    InetAddress targetAddr = InetAddress.getByName(clients.get(i).getMyAddress());
                    sendUDP(data, targetAddr, clients.get(i).getMyPort());
                }
            }           
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void acceptProposalPaxos(){
        JSONObject data = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            arr.put(proposalNumber);
            arr.put(playerID);
            data.put("method", "accept_proposal");
            data.put("proposal_id", arr);
            data.put("kpu_id", kpuID);
            
            for (int i=0; i<clients.size(); i++) {
                if (clients.get(i).getStatus() == 1) {
                    InetAddress targetAddr = InetAddress.getByName(clients.get(i).getMyAddress());
                    sendUDP(data, targetAddr, clients.get(i).getMyPort());
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                responseJSON = parseToJSON(receiveUDP(getMyPort()));
                System.out.println(responseJSON);
                
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
                Scanner in = new Scanner(System.in);
                int port = in.nextInt();
                responseJSON = parseToJSON(receiveUDP(port));
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
    
    public static void infoWerewolf(int player_id, int vote_status, String vote_result){
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
    
    public static void infoCivilian(int player_id, int vote_status, String vote_result){
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
    
    public static void sendToID(int id, JSONObject data) {
        
        try {
            InetAddress targetAddr = InetAddress.getByName(clients.get(id).getMyAddress());
            sendUDP(data, targetAddr, clients.get(id).getMyPort());
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
/**
 * Contoh kode program untuk node yang mengirimkan paket. Paket dikirim
 * menggunakan UnreliableSender untuk mensimulasikan paket yang hilang.
 */
    public static void main(String args[]) throws Exception
    {
        Scanner in = new Scanner(System.in);
        System.out.print("Username: ");
        String username = in.nextLine();
        //System.out.print("Server IP: ");
        String addr = "localhost";
        //System.out.print("Server Port: ");
        int port = 1000;
        System.out.print("UDP Port: ");
        int udpPort = in.nextInt();
        Client c = new Client(username, addr, port, udpPort);
        c.join();
        c.ready();
        System.out.println("1st phase");
        if (c.isStart()) {
            c.requestClients();
            System.out.println("2nd phase");
            c.prepareProposal();
            System.out.println("3rd phase");
            c.acceptProposalPaxos();
            System.out.println("4th phase");
            c.acceptProposalClient();
            System.out.println("5th phase");
            c.voteWerewolf(0);
            System.out.println("6th phase");
        }
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
