package ch.ethz.inf.vs.a2.webservices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import java.util.Set;

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
            /*InputStream in = conn_sock.getInputStream();
            //InputStreamReader isr = new InputStreamReader(in);
            //BufferedReader reader = new BufferedReader(isr);
            byte[] buffer = new byte[4096];
            while(){
                in.read()
            }
            String request = new String(buffer, "UTF-8");*/

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn_sock.getInputStream(), "UTF-8")
            );
            //System.out.println("DEBUG: request=\n" + request);

            String response_version = "HTTP/1.1";

            HttpPayload payload_obj = new HttpPayload(in);


            String[] h = handleRequest(payload_obj);


            String response_body = h[0];
            String response_code = h[1];

            OutputStream out = conn_sock.getOutputStream();
            PrintWriter response = new PrintWriter(out);

            String resp = response_version + " " + response_code + "\r\n"
                    + "\r\n"
                    + response_body;

            System.out.println("DEBUG: response=\n" + resp);

            response.write(resp);

            response.flush();
            //in.close();
            //out.close();
            conn_sock.close();
        } catch (IOException ie){

        }
    }

    private String[] handleRequest(HttpPayload payload_obj){
        String[] res = new String[2];
        res[0] = "";
        res[1] = "400 Bad Request";
        /*if(request.isEmpty()) {
            return res;
        }*/

        Map<String, String> headers = payload_obj.getHeaderMap();

        String method = payload_obj.getMethod();
        String uri = payload_obj.getUri();
        String body = payload_obj.getBody();

        System.out.println("DEBUG: method=" + method);
        System.out.println("DEBUG: uri=" + uri);
        System.out.println("DEBUG: headers");
        Object[] keys = headers.keySet().toArray();
        for (int k = 0; k < headers.size(); k++){
            System.out.println("DEBUG: "+ keys[k] + "=" + headers.get(keys[k]));
        }
        System.out.println("DEBUG: body=" + body);

        if (uri.startsWith("http://") || uri.matches("^//")) {
            uri = uri.substring(uri.indexOf('/') + 2);
            int index = uri.indexOf('/');
            if (index != -1) {
                uri = uri.substring(index);
            } else {
                uri = "/";
            }
        } else if (uri.startsWith("/")) {
            System.out.println("DEBUG: URI matches /");
        } else {
            return res;
        }

        switch (method) {
            case "GET":
                res = handleGET(uri);
                break;
            case "POST":
                //System.out.println("DEBUG: It's a POST request");
                res = handlePOST(uri, body);
                break;
            default:
                res[1] = "501 Not Implemented";
                break;
        }

        return res;
    }

    //returns the response_body and the response_code
    private String[] handleGET(String uri) {
        String[] res = new String[2];
        try {
            String file_location = "www" + uri;
            System.out.println("DEBUG: file_location="+file_location);
            res[0] = getStringFromFile(file_location);
            res[1] = "200 OK";
        } catch (IOException ie) {
            res[0] = "";
            res[1] = "404 Not Found";
            return res;
        }
        SensorManager sens_man = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorHelper sens_helper = new SensorHelper(sens_man);
        double val = 0.0;
        switch (uri) {
            case "/sensors/sensor1.html":
                val = sens_helper.getSensorValue(Sensor.TYPE_PRESSURE);
                res[0] = res[0].replaceFirst("@@@pressure@@@", Double.toString(val));
            case "/sensors/sensor2.html":
                val = sens_helper.getSensorValue(Sensor.TYPE_LIGHT);
                res[0] = res[0].replaceFirst("@@@brightness@@@", Double.toString(val));
        }
        return res;
    }

    private String[] handlePOST(String uri, String body){
        String[] res = new String[2];
        res[0] = "";
        res[1] = "400 Bad Request";
        switch (uri) {
            case "/actuators/actuator1.html":
                //String[] params = body.split("&");
                long duration;
                //if (params.length >= 1) {
                String dur = body.split("=")[1];
                    //String pattern = params[1].split("=")[1];

                    duration = Integer.parseInt(dur);
                //} else {
                //    return res;
               // }
                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                vib.vibrate(duration);
                //vib.vibrate(VibrationEffect.createOneShot(duration,amplitude));

                res[1] = "200 OK";
        }
        return res;
    }

    private String getStringFromFile (String file) throws IOException{
        InputStream in = getAssets().open(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String res = "";
        String line;
        while ((line = reader.readLine()) != null) {
            res = res + line + "\n";
        }
        reader.close();
        in.close();
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
