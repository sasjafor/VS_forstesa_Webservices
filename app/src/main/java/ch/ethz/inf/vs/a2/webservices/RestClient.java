package ch.ethz.inf.vs.a2.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.sensor.JsonSensor;
import ch.ethz.inf.vs.a2.sensor.RawHttpSensor;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.sensor.TextSensor;

public class RestClient extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_client);

        //TODO: replace constructors with factory constructors when project finished
        // For each version show the temperature value
        temp_val_raw = (TextView) findViewById(R.id.temp_val_raw);
        temp_val_url = (TextView) findViewById(R.id.temp_val_url);
        temp_val_json = (TextView) findViewById(R.id.temp_val_json);

        // RawHttpSensor version
        RawHttpSensor raw_sensor = new RawHttpSensor();
        raw_sensor.registerListener(new SensorListener() {

            @Override
            public void onReceiveSensorValue(double value) {
                String temp = Double.toString(value);
                RestClient.this.temp_val_raw.setText(temp);
            }

            @Override
            public void onReceiveMessage(String message) {
                System.out.println(message);
            }
        });
        raw_sensor.getTemperature();

        // HttpURLConnection version
        TextSensor text_sensor = new TextSensor();
        text_sensor.registerListener(new SensorListener() {
            @Override
            public void onReceiveSensorValue(double value) {
                String temp = Double.toString(value);
                RestClient.this.temp_val_url.setText(temp);
            }

            @Override
            public void onReceiveMessage(String message) {
                System.out.println(message);
            }
        });
        text_sensor.getTemperature();

        // JSON version
        JsonSensor json_sensor = new JsonSensor();
        json_sensor.registerListener(new SensorListener() {
            @Override
            public void onReceiveSensorValue(double value) {
                String temp = Double.toString(value);
                RestClient.this.temp_val_json.setText(temp);
            }

            @Override
            public void onReceiveMessage(String message) {
                System.out.println(message);
            }
        });
        json_sensor.getTemperature();
    }

    TextView temp_val_raw;
    TextView temp_val_url;
    TextView temp_val_json;
}
