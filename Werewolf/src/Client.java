import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    private ObjectInputStream is = null;
    private Socket socket = null;  
    private ObjectOutputStream os = null;
    private Vector<Client> clients = new Vector();
    private int playerID;
    private String username;
    private String myAddress;
    private int myPort;
    private int status;
    private int type = 0;
    private int prevProposalID = 0;
    private int proposalNumber = 0;
    
    /**
     * Create client with server socket
     * @param addr server IP Address
     * @param port server port
     */
    public Client(String addr, int port, String username) {
        try {
            socket = new Socket(addr, port);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
            
            this.username = username;
            this.status = 1;
            this.myAddress = InetAddress.getLocalHost().getHostAddress();
            this.myPort = 9876;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
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

                if (status == "ok") {
                    this.playerID = inData.getInt("player_id");
                    fail = !fail;
                }
                else if (status == "fail") {
                    System.out.println(inData.getString("description"));
                }
                else {
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
        try {
            os.writeObject(data);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            obj = (JSONObject)is.readObject();
        }
        catch (ClassNotFoundException ex) {
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

                if (status == "ok") {
                    fail = !fail;
                }
                else if (status == "fail") {
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
                
                if (status == "ok") {
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
                
                if (status == "ok") {
                    fail = !fail;
                    clientJSON = response.getJSONArray("clients");
                    
                    for (int i=0; i<clientJSON.length(); i++) {
                        JSONObject temp = clientJSON.getJSONObject(i);
                        Client tempClient = new Client("192.168.1.17", 9876, temp.getString("username"));
                        tempClient.setMyAddress(temp.getString("address"));
                        tempClient.setMyPort(temp.getInt("port"));
                        tempClient.setStatus(temp.getInt("is_alive"));
                        tempClient.setID(temp.getInt("player_id"));
                        clients.add(tempClient);
                    }
                }
                else if (status == "fail") {
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
        try {
            data.put("method", "prepare_proposal");
            data.put("proposal_id", "("+proposalNumber+","+playerID+")");
            
            for (int i=0; i<clients.size(); i++) {
                InetAddress targetAddr = InetAddress.getByName(clients.get(i).getMyAddress());
                sendUDP(data, targetAddr, clients.get(i).getMyPort());
            }
            
            while (count != clients.size()) {
                response = receiveUDP();
                String sentence = new String(response.getData(), 0, response.getLength());
                responseJSON = new JSONObject(sentence);
                
                String status = responseJSON.getString("status");
                if (status == "ok") {
                    count++;
                }
                else if (status == "fail") {
                    System.out.println(responseJSON.getString("description"));
                    InetAddress targetAddr = response.getAddress();
                    sendUDP(data, targetAddr, 9876);
                }
                else {
                    System.out.println(responseJSON.getString("description"));
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
        try {
            data.put("method", "vote_werewolf");
            data.put("player_id", id);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendTCP(data);
    }
    
    private void killCivilian(int id){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "vote_civilian");
            data.put("player_id", id);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendTCP(data);
    }
    
    private void infoWerewolf(){
        JSONObject data = new JSONObject();
        try {
            data.put("method", "vote_result_werewolf");
            data.put("vote_status", "1");
            data.put("player_killed", "1");
            data.put("vote_result", "1");
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        sendTCP(data);
    }
    
/**
 * Contoh kode program untuk node yang mengirimkan paket. Paket dikirim
 * menggunakan UnreliableSender untuk mensimulasikan paket yang hilang.
 */
    public static void main(String args[]) throws Exception
    {
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
    }
}
