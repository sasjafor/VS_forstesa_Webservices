package ch.ethz.inf.vs.a2.webservices;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;

public class RestServerService extends Service {

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId){
        Thread t = new Thread("REST_SERVER_SERVICE(" + startId + ")") {
            @Override
            public void run() {
                startThis(intent);
            }
        };
        t.start();

        return START_NOT_STICKY;
    }

    private void startThis(final Intent intent){
        Bundle extras = intent.getExtras();
        if (extras != null){
            sock_addr = (InetSocketAddress) extras.get("sock_addr");
            System.out.println("DEBUG: sock_addr"+sock_addr);
        }
        try {
            sock.bind(sock_addr);
            System.out.println("DEBUG : Bound to socket");

            while(true) {
                final Socket conn_sock = sock.accept();

                Thread t = new Thread(){
                    @Override
                    public void run(){
                        startConnectionThread(conn_sock);
                    }
                };
                t.start();
            }

        } catch (IOException ie){
            /*Toast toast = new Toast(this);
            toast.setText(R.string.bind_exception_text);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();*/
        }
    }

    private void startConnectionThread(Socket conn_sock){
        try {
            InputStream request = conn_sock.getInputStream();
            byte b[] = new byte[request.available()];
            request.read(b);
            String payload = new String(b, "UTF-8");
            System.out.println("DEBUG: request=" + payload);

            if(payload.isEmpty()) {
                System.out.println("DEBUG: Empty Request");
                stopSelf();
            }

            HttpPayload payload_obj = new HttpPayload(payload);
            Map<String, String> headers = payload_obj.getHeaderMap();

            String method = payload_obj.getMethod();
            String uri = payload_obj.getUri();

            System.out.println("DEBUG: method=" + method);
            System.out.println("DEBUG: uri=" + uri);


            OutputStream out = conn_sock.getOutputStream();
            PrintWriter response = new PrintWriter(out);

            String response_code = "200 OK";

            String response_body = null;
            try {
                response_body = getStringFromFile("file:///android_asset/www" + uri);
            } catch (FileNotFoundException fnfe) {
                System.out.println("DEBUG: File doesn't exist");
                stopSelf();
            }

            response.write(payload_obj.getVersion() + response_code + "\r\n"
                    + "\r\n"
                    + response_body);

            response.flush();
            conn_sock.close();
        } catch (IOException ie){

        }
    }

    public static String getStringFromFile (String filePath) throws IOException{
        File f = new File(filePath);
        FileInputStream fi = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fi));
        String res = "";
        String line = null;
        while ((line = reader.readLine()) != null) {
            res = res + line + "\n";
        }
        reader.close();
        fi.close();
        return res;
    }

    @Override
    public void onCreate(){
        try {
            sock = new ServerSocket();
        } catch (IOException ie){
            //do nothing for now
        }

    }

    @Override
    public void onDestroy(){
        try {
            sock.close();
        } catch (IOException ie) {
            //do nothing
        }
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ServerSocket sock;
    private InetSocketAddress sock_addr;
}
