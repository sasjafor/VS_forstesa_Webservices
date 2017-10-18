package ch.ethz.inf.vs.a2.webservices;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static java.net.NetworkInterface.getByName;
import static java.net.NetworkInterface.getNetworkInterfaces;

public class RestServer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);

        NetworkInterface ni = null;
        try {
            Enumeration<NetworkInterface> list = getNetworkInterfaces();
            System.out.println("DEBUG: network interfaces");
            while(list.hasMoreElements()){
                NetworkInterface h = list.nextElement();
                System.out.println("\n"+h);
                Enumeration<InetAddress> h1 = h.getInetAddresses();
                if(h1.hasMoreElements()) {
                    System.out.println("\n"+h1.nextElement());
                }
            }
            //ni = list.nextElement();
            ni = getByName("rmnet0");
        } catch (SocketException se){
            Toast toast = new Toast(this);
            toast.setText(R.string.socket_exception_text);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }

        intent_service = new Intent(this, RestServerService.class);
        InetAddress addr = ni.getInetAddresses().nextElement();
        InetSocketAddress sock_addr = new InetSocketAddress(addr, PORT);
        intent_service.putExtra("sock_addr",sock_addr);
    }

    public void onClickToggle(View v) {
        ToggleButton tb = (ToggleButton) v;
        if(tb.isChecked()){
            System.out.println("DEBUG: Start server");
            //bindService(intent_service,conn,0);
            startService(intent_service);
        } else {
            stopService(intent_service);
        }
    }

    /*
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
        }

        @Override
        public void onServiceDisconnected (ComponentName className) {
            ToggleButton tb = (ToggleButton) findViewById(R.id.btn_toggle_server);
            tb.setEnabled(false);
        }
    };*/

    private Intent intent_service;
    private Thread server_thread;

    private int PORT = 8088;
}
