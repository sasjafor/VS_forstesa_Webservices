package ch.ethz.inf.vs.a2.webservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickRestClient(View v){
        this.startActivity(new Intent(this, RestClient.class));
    }

    public void onClickSoapClient(View v){
        this.startActivity(new Intent(this, SoapClient.class));
    }

    public void onClickRestServer(View v){
        this.startActivity(new Intent(this, RestServer.class));
    }
}
