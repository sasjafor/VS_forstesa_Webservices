package ch.ethz.inf.vs.a2.webservices;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;

public class RestServerService extends Service {

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null){
            sock_addr = (InetSocketAddress) extras.get("sock_addr");
        }
        try {
            sock.bind(sock_addr);
            Socket conn_sock = sock.accept();
            InputStream in = conn_sock.getInputStream();
            OutputStream out = conn_sock.getOutputStream();

            byte b[] = new byte[in.available()];
            in.read(b);
            System.out.println("DEBUG: request="+b);

        } catch (IOException ie){
            Toast toast = new Toast(this);
            toast.setText(R.string.bind_exception_text);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
        return binder;
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
    }

    public class LocalBinder extends Binder {
        RestServerService getService() {
            return RestServerService.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    private ServerSocket sock;
    private InetSocketAddress sock_addr;
}
