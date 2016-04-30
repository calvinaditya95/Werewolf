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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    private ObjectInputStream is = null;
    private Socket socket = null;  
    private ObjectOutputStream os = null;
    private int playerID;
    private int type = 0;
    private int prevProposalID = 0;
    private int proposalNumber = 0;
    
    /**
     * Create client with server socket
     * @param addr server IP Address
     * @param port server port
     */
    public Client(String addr, int port) {
        try {
            socket = new Socket(addr, port);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Join Game
     * @param username username of the client
     * @param addr use inetaddress.getlocalhost() to get local IP Address
     * @param port connection port
     */
    private void join(String username, String addr, int port) {
        try {
            JSONObject outData = new JSONObject();
            JSONObject inData = new JSONObject();
            try {
                outData.put("method", "join");
                outData.put("username", username);
                outData.put("udp_address", addr);
                outData.put("udp_port", port);
            } catch (JSONException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            sendTCP(outData);
            inData = receiveTCP();
            
            String status = inData.getString("status");
            
            if (status == "ok") {
                this.playerID = inData.getInt("player_id");
            }
            else if (status == "fail") {
                System.out.println(inData.getString("description"));
            }
            else {
                System.out.println(inData.getString("description"));
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
