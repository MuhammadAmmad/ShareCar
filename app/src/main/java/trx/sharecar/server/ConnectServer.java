package trx.sharecar.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;

public class ConnectServer {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static String ip="";

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean connect() {

        try {
            //socket = new Socket("192.168.199.215",9898);
            if(ip.equals(""))
                socket = new Socket("172.20.10.6",9898);
            else
                socket = new Socket(ip,9898);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (ConnectException e){
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void sendMSG(String msg){
        try {
            writer.write(msg+"\n");
            Log.i("ClientSendMSG",msg);
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String receiveMSG(){
        String msg = null;
        try {
            if((msg=reader.readLine()) != null)
                Log.i("ClientReceiveMSG",msg);
            else
                Log.i("ClientReceiveMSG","未收到消息");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    public void close(){
        try {
            writer.close();
            reader.close();
            socket.close();
            Log.i("ClientClosed", "Socket Closed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
