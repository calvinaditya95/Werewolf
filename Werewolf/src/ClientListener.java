
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Asus
 */
public class ClientListener extends Thread {  
    private boolean done = false;
    private int senderNum;
    
    public ClientListener(int i) {
        this.senderNum = i;
    }
    
    private void listenProposal() {
        DatagramPacket response = null;
        JSONObject responseJSON = new JSONObject();
        
        response = receiveUDP();
        responseJSON = Client.parseToJSON(response);
        
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
    
    @Override
    public void run() {
        while (!done) {
            listenProposal();
        }
    }
}