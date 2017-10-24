package ch.ethz.inf.vs.a2.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.sensor.SensorFactory;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.sensor.SoapSensor;
import ch.ethz.inf.vs.a2.sensor.XmlSensor;

public class SoapClient extends AppCompatActivity implements SensorListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_client);

        temperature = (TextView) findViewById(R.id.soapTemperature);
        temperature.setText(getString(R.string.temperature,0.0));

        xmlSensor = (XmlSensor) SensorFactory.getInstance(SensorFactory.Type.XML);
        xmlSensor.registerListener(this);

        soapSensor = (SoapSensor) SensorFactory.getInstance(SensorFactory.Type.SOAP);
        soapSensor.registerListener(this);
    }

    @Override
    public void onReceiveSensorValue(double value) {
        temperature.setText(getString(R.string.temperature,value));
    }

    @Override
    public void onReceiveMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void onPause() {
        super.onPause();

        xmlSensor.unregisterListener(this);
        soapSensor.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        xmlSensor.registerListener(this);
        soapSensor.registerListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        xmlSensor.unregisterListener(this);
        soapSensor.unregisterListener(this);
    }

    public void onClickXml(View v) {
        xmlSensor.getTemperature();
    }

    public void onClickSoap(View v) {
        soapSensor.getTemperature();
    }

    TextView temperature;
    XmlSensor xmlSensor;
    SoapSensor soapSensor;
}
