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
     * 
     * @param username username of the client
     * @param addr use inetaddress.getlocalhost() to get local IP Address
     * @param port connection port
     */
    private void join(String username, String addr, int port) {
        JSONObject data = new JSONObject();
        try {
            data.put("method", "join");
            data.put("username", username);
            data.put("udp_address", addr);
            data.put("udp_port", port);
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        send(data);
    }
    
    private void send(JSONObject data) {
        try {
            os.writeObject(data);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
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
