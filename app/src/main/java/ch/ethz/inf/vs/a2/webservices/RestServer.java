package ch.ethz.inf.vs.a2.webservices;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import static java.net.NetworkInterface.getByName;
import static java.net.NetworkInterface.getNetworkInterfaces;

public class RestServer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);

        final ArrayList<String> interfaces = new ArrayList<>();
        Enumeration<NetworkInterface> list = null;
        try {
            list = getNetworkInterfaces();
            //System.out.println("DEBUG: network interfaces");

                //System.out.println("\n"+h);
                //Enumeration<InetAddress> h1 = h.getInetAddresses();
                /*if(h1.hasMoreElements()) {
                    System.out.println("\n"+h1.nextElement());
                }*/

            //ni = list.nextElement();
            //ni = getByName("wlan0");
        } catch (SocketException se){
            Toast toast = Toast.makeText(this,R.string.socket_exception_text,Toast.LENGTH_LONG);
            toast.show();
        }
        while(list.hasMoreElements()) {
            NetworkInterface el = list.nextElement();
            if (el.getInetAddresses().hasMoreElements()) {
                String h = el.getName();
                interfaces.add(h);
            }
        }



        ListView lv = (ListView) findViewById(R.id.interface_list);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, interfaces);
        lv.setAdapter(adapter);
        lv.setEnabled(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //NetworkInterface ni = null;
                try {
                    ni = getByName(interfaces.get(position));
                } catch (SocketException se) {
                    Toast toast = Toast.makeText(RestServer.this,R.string.socket_exception_text,Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                intent_service = new Intent(RestServer.this, RestServerService.class);
                InetAddress addr = ni.getInetAddresses().nextElement();

                Resources res = getResources();
                TextView text = (TextView) findViewById(R.id.network_information);
                text.setText(res.getString(R.string.network_information,addr+":"+PORT));

                InetSocketAddress sock_addr = new InetSocketAddress(addr, PORT);
                intent_service.putExtra("sock_addr",sock_addr);
            }
        });
    }

    public void onClickToggle(View v) {
        ToggleButton tb = (ToggleButton) v;
        if(tb.isChecked()){
            if (ni != null) {
                System.out.println("DEBUG: Start server");
                startService(intent_service);
            } else {
                Toast toast = Toast.makeText(this,R.string.no_interface_chosen,Toast.LENGTH_LONG);
                toast.show();
                tb.setChecked(false);
            }
        } else {
            stopService(intent_service);
        }
    }

    /*private void setNi(NetworkInterface ni){
        this.ni = ni;
    }*/

    private Intent intent_service;
    private NetworkInterface ni;

    private int PORT = 8088;
}
