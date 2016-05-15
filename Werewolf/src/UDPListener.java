import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPListener extends Thread {
    private final DatagramSocket udpSocket;
    private DatagramPacket lastReceived;
    private String lastInput;
    
    public UDPListener(DatagramSocket s) {
        udpSocket = s;
        lastReceived = null;
        lastInput = null;
    }
    
    @Override
    public void run() {
        try {
            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                udpSocket.receive(receivePacket);                
                
                String input = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (lastReceived != null) {
                    if (receivePacket.getAddress().equals(lastReceived.getAddress()) && receivePacket.getPort() == lastReceived.getPort() && lastInput.equals(input)) {
                        //Do nothing, duplicate packet
                    }
                    else {
                        lastReceived = receivePacket;
                        lastInput = input;

                        Process p = new Process(input, receivePacket.getAddress(), receivePacket.getPort());
                        p.start();
                    }
                }
                else {
                    lastReceived = receivePacket;
                    lastInput = input;

                    Process p = new Process(input, receivePacket.getAddress(), receivePacket.getPort());
                    p.start();
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
}
