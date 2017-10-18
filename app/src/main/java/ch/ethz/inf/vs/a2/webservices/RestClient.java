package ch.ethz.inf.vs.a2.webservices;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.sensor.JsonSensor;
import ch.ethz.inf.vs.a2.sensor.RawHttpSensor;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.sensor.TextSensor;

public class RestClient extends AppCompatActivity implements SensorListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_client);

        //TODO: replace constructors with factory constructors when project finished

        temp_val = (TextView) findViewById(R.id.temperature);

        RawHttpSensor raw_sensor = new RawHttpSensor();
        TextSensor text_sensor = new TextSensor();
        JsonSensor json_sensor = new JsonSensor();

        raw_sensor.registerListener(this);
        text_sensor.registerListener(this);
        json_sensor.registerListener(this);
    }

    @Override
    public void onReceiveSensorValue(double value) {
        Resources res = getResources();
        temp_val.setText(res.getString(R.string.temperature,value));
    }

    @Override
    public void onReceiveMessage(String message) {
        System.out.println(message);
    }

    public void onClickRaw(View v) {
        raw_sensor.getTemperature();
    }

    public void onClickURL(View v) {
        text_sensor.getTemperature();
    }

    public void onClickJSON(View v) {
        json_sensor.getTemperature();
    }

    RawHttpSensor raw_sensor;
    TextSensor text_sensor;
    JsonSensor json_sensor;
    TextView temp_val;
}
