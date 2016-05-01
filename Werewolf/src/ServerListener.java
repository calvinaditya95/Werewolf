
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
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
public class ServerListener extends Thread {
    private Socket socket;
    private BufferedReader is;
    
    public ServerListener(Socket sock) {
        try {
            this.socket = sock;
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
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
    
    @Override
    public void run() {
        try {
            JSONObject data = new JSONObject();
            data = receiveTCP();
            if (data.getString("method").equals("kpu_selected")) {
                Client.setKpuID(data.getInt("kpu_id"));
            }
                } catch (JSONException ex) {
            Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
