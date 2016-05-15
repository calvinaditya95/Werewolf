import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPListener extends Thread {
    private final Socket socket;
    
    public TCPListener(Socket s) {
        socket = s;
    }
    
    @Override
    public void run() {
        try {
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            while (true) {
                line = in.readLine();
                Process p = new Process(line);
                p.start();
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
