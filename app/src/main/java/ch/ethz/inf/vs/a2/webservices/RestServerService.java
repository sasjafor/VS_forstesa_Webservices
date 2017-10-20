package ch.ethz.inf.vs.a2.webservices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.interrupted;

public class RestServerService extends Service {

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId){
        t = new Thread("REST_SERVER_SERVICE(" + startId + ")") {
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

            while(!t.isInterrupted()) {
                final Socket conn_sock = sock.accept();
                System.out.println("DEBUG: accepted connection");

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
            InputStreamReader isr = new InputStreamReader(conn_sock.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            //InputStream request = conn_sock.getInputStream();
            //byte b[] = new byte[request.available()];
            //request.read(b);
            String request = "";
            String line;
            while ((line = reader.readLine()) != null) {
                request = request + line + "\n";
            }

            System.out.println("DEBUG: request=" + request);

            String response_version = "HTTP/1.1";

            String[] h = handleRequest(request);
            String response_body = h[0];
            String response_code = h[1];

            OutputStream out = conn_sock.getOutputStream();
            PrintWriter response = new PrintWriter(out);

            response.write(response_version + response_code + "\r\n"
                    + "\r\n"
                    + response_body);

            response.flush();
            conn_sock.close();
        } catch (IOException ie){

        }
    }

    private String[] handleRequest(String request){
        String[] res = new String[2];
        res[0] = "";
        res[1] = "200 OK";
        if(request.isEmpty()) {
            res[1] = "400 Bad Request";
            return res;
        }
        HttpPayload payload_obj = new HttpPayload(request);
        Map<String, String> headers = payload_obj.getHeaderMap();

        String method = payload_obj.getMethod();
        String uri = payload_obj.getUri();

        if (uri.matches("^http://") || uri.matches("^//")) {
            uri = uri.substring(uri.indexOf('/') + 2);
            int index = uri.indexOf('/');
            if (index != -1) {
                uri = uri.substring(index);
            } else {
                uri = "/";
            }
        } else if (uri.matches("^/")) {

        } else {
            res[1] = "400 Bad Request";
            return res;
        }

        switch (method) {
            case "GET":
                res = handleGET(uri);
                break;
            case "POST":
                handlePOST();
                break;
            case "PUT":
            case "DELETE":
            case "HEAD":
            case "OPTIONS":
                res[1] = "501 Not Implemented";
                break;
            default:
                res[1] = "400 Bad Request";
        }

        System.out.println("DEBUG: method=" + method);
        System.out.println("DEBUG: uri=" + uri);

        return res;
    }

    //returns the response_body and the response_code
    private String[] handleGET(String uri) {
        String[] res = new String[2];
        try {
            res[0] = getStringFromFile("file:///android_asset/www" + uri);
        } catch (IOException ie) {
            res[0] = "";
            res[1] = "404 Not Found";
            return res;
        }
        SensorManager sens_man = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorHelper sens_helper = new SensorHelper(sens_man);
        double val = 0.0;
        switch (uri) {
            case "/sensor1.html":
                val = sens_helper.getSensorValue(Sensor.TYPE_PRESSURE);
            case "/sensor2.html":
                val = sens_helper.getSensorValue(Sensor.TYPE_LIGHT);
        }
        res[0].replaceFirst("@@@value1@@@", Double.toString(val));
        return res;
    }

    private void handlePOST(){

    }

    private String getStringFromFile (String filePath) throws IOException{
        File f = new File(filePath);
        FileInputStream fi = new FileInputStream(f);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fi));
        String res = "";
        String line;
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
        t.interrupt();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Thread t;
    private ServerSocket sock;
    private InetSocketAddress sock_addr;
}
