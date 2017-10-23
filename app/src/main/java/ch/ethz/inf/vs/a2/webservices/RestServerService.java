package ch.ethz.inf.vs.a2.webservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;

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
        }
        try {
            sock.bind(sock_addr);

            while(!t.isInterrupted()) {
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
            System.out.println("DEBUG: failed to bind socket to address");
        }
    }

    private void startConnectionThread(Socket conn_sock){
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn_sock.getInputStream(), "UTF-8")
            );

            String response_version = "HTTP/1.1";

            String response_body;
            String response_code;
            String response_headers = "";
            try {
                HttpPayload payload_obj = new HttpPayload(in);
                String[] h = handleRequest(payload_obj);
                response_body = h[0];
                response_code = h[1];
            } catch (NullPointerException npe) {
                response_body = "";
                response_code = "400 Bad Request";
            }

            if (response_code.equals("302 Found")) {
                response_headers = "Location: http://" + sock_addr.getAddress().getHostAddress() + ":8088/index.html";
            }

            OutputStream out = conn_sock.getOutputStream();
            PrintWriter response = new PrintWriter(out);

            String resp = response_version + " " + response_code + "\r\n"
                    + response_headers
                    + "\r\n"
                    + response_body;

            response.write(resp);

            response.flush();
            conn_sock.close();
        } catch (IOException ie){
            Toast toast = Toast.makeText(this,R.string.connection_failed,Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private String[] handleRequest(HttpPayload payload_obj){
        String[] res = new String[2];
        res[0] = "";
        res[1] = "400 Bad Request";

        String method = payload_obj.getMethod();
        String uri = payload_obj.getUri();
        String body = payload_obj.getBody();

        if (uri.startsWith("http://") || uri.matches("^//")) {
            uri = uri.substring(uri.indexOf('/') + 2);
            int index = uri.indexOf('/');
            if (index != -1) {
                uri = uri.substring(index);
            } else {
                uri = "/";
            }
        } else if (uri.startsWith("/")) {
            //uri matches '/', do nothing and continue
        } else {
            return res;
        }

        if (uri.equals("/")) {
            res[1] = "302 Found";
            return res;
        }

        switch (method) {
            case "GET":
                res = handleGET(uri);
                break;
            case "POST":
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
            res[0] = getStringFromFile(file_location);
            res[1] = "200 OK";
        } catch (IOException ie) {
            res[0] = "";
            res[1] = "404 Not Found";
            return res;
        }
        SensorManager sens_man = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        SensorHelper sens_helper = new SensorHelper(sens_man);
        double val;
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
                if (body.startsWith("duration=")) {
                    long duration;
                    String dur = body.split("=")[1];
                    duration = Integer.parseInt(dur);
                    Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                    vib.vibrate(duration);

                    res[1] = "200 OK";
                }
                break;
            case "/actuators/actuator2.html":
                if (body.startsWith("title=")){
                    String title = "", text = "";

                    String[] split = body.split("&");
                    String[] t1 = split[0].split("=");
                    String[] t2 = split[1].split("=");

                    if (t1.length == 2) {
                        title = t1[1];
                    }
                    if (t2.length == 2) {
                        text = split[1].split("=")[1];
                    }
                    String colour = split[2].split("=")[1];
                    int col = Color.WHITE;
                    switch (colour) {
                        case "red":
                            col = 0xFFFF0000;
                            break;
                        case "green":
                            col = 0xFF00FF00;
                            break;
                        case "blue":
                            col = 0xFF0000FF;
                            break;
                    }
                    try {
                        title = URLDecoder.decode(title, "UTF-8");
                        text = URLDecoder.decode(text, "UTF-8");
                    } catch (UnsupportedEncodingException uee) {
                        break;
                    }
                    Notification noti = new Notification.Builder(this)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setLights(col,300,100)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setSmallIcon(R.drawable.notification_small)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.notification_small))
                            .build();
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    noti.flags = Notification.FLAG_SHOW_LIGHTS;
                    nm.notify(42,noti);

                    res[1] = "200 OK";
                }
                break;
        }
        return handleGET(uri);
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
