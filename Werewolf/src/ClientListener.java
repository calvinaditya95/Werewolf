
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientListener extends Thread {  
    private boolean done = false;
    private int messageNum = 0;
    private int myPort;
    
    public ClientListener(int i) {
        this.myPort = i;
    }
    
    public int getPort(){
        return myPort;
    }
    
    private void listenProposal() throws JSONException {
        DatagramPacket response = null;
        JSONObject responseJSON = new JSONObject();
        
        response = receiveUDP(myPort);
        responseJSON = Client.parseToJSON(response);
        if(responseJSON.has("method")){
            String method = responseJSON.getString("method");
            if (method.equals("prepare_proposal")){
                JSONArray temp = responseJSON.getJSONArray("proposal_id");
                int a = temp.getInt(0);
                int b = temp.getInt(1);
                
                response = receiveUDP(myPort);
                responseJSON = Client.parseToJSON(response);
                JSONArray temp2 = responseJSON.getJSONArray("proposal_id");
                int c = temp2.getInt(0);
                int d = temp2.getInt(1);
                
                int prev = Client.getPrevProposalID();
                int kpu_id;
                if( a > c ){
                    kpu_id = a;
                }else if(a < c){
                    kpu_id = c;
                }else{
                    if(b > d){
                        kpu_id = a;
                    }else if(b < d){
                        kpu_id = c;
                    }
                }
            }
        }
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
    
    @Override
    public void run() {
        while (!done) {
            
        }
    }
}