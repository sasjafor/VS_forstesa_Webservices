package ch.ethz.inf.vs.a2.webservices;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.net.Inet6Address;
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
        } catch (SocketException se){
            Toast toast = Toast.makeText(this,R.string.socket_exception_text,Toast.LENGTH_LONG);
            toast.show();
        }
        while(list != null && list.hasMoreElements()) {
            NetworkInterface el = list.nextElement();
            if (el.getInetAddresses().hasMoreElements()) {
                String h = el.getName();
                interfaces.add(h);
            }
        }

        lv = (ListView) findViewById(R.id.interface_list);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, interfaces);
        lv.setAdapter(adapter);
        lv.setEnabled(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ni = getByName(interfaces.get(position));
                } catch (SocketException se) {
                    Toast toast = Toast.makeText(RestServer.this,R.string.socket_exception_text,Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                intent_service = new Intent(RestServer.this, RestServerService.class);
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                InetAddress addr = addresses.nextElement();
                while (addr instanceof Inet6Address && addresses.hasMoreElements()) {
                    addr = addresses.nextElement();
                }
                if (addr instanceof Inet6Address){
                    address = "["+addr.getHostAddress().substring(0,addr.toString().indexOf('%'))+"]";
                } else {
                    address = addr.getHostAddress();
                }


                Resources res = getResources();
                TextView text = (TextView) findViewById(R.id.network_information);
                server_info = res.getString(R.string.network_information_down,address+":"+PORT);
                text.setText(server_info);

                InetSocketAddress sock_addr = new InetSocketAddress(addr, PORT);
                intent_service.putExtra("sock_addr",sock_addr);
            }
        });
    }

    public void onClickToggle(View v) {
        ToggleButton tb = (ToggleButton) v;
        if(tb.isChecked()){
            if (ni != null) {
                lv.setEnabled(false);
                Resources res = getResources();
                TextView text = (TextView) findViewById(R.id.network_information);
                server_info = res.getString(R.string.network_information_up,address+":"+PORT);
                text.setText(server_info);
                startService(intent_service);
                service_running = true;
            } else {
                Toast toast = Toast.makeText(this,R.string.no_interface_chosen,Toast.LENGTH_LONG);
                toast.show();
                tb.setChecked(false);
            }
        } else {
            stopService(intent_service);
            service_running = false;
            Resources res = getResources();
            TextView text = (TextView) findViewById(R.id.network_information);
            server_info = res.getString(R.string.network_information_down,address+":"+PORT);
            text.setText(server_info);
            lv.setEnabled(true);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        ToggleButton tb = (ToggleButton) findViewById(R.id.btn_toggle_server);
        if (service_running) {
            tb.setChecked(true);
            lv.setEnabled(false);
            TextView text = (TextView) findViewById(R.id.network_information);
            text.setText(server_info);
        }
    }

    private static Intent intent_service;
    private NetworkInterface ni;
    private String address;
    private ListView lv;
    private static boolean service_running;
    private static String server_info;

    private final int PORT = 8088;
}
