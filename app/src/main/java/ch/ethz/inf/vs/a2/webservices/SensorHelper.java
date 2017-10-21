package ch.ethz.inf.vs.a2.webservices;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorHelper implements SensorEventListener{

    public SensorHelper(SensorManager sens_man) {
        this.sens_man = sens_man;
    }

    synchronized public double getSensorValue(int sens_type) {
        Sensor sens = sens_man.getDefaultSensor(sens_type);
        sens_man.registerListener(this,sens,SensorManager.SENSOR_DELAY_NORMAL);
        try {
            wait();
        } catch (InterruptedException ie) {

        }
        return value;
    }

    private SensorManager sens_man;

    @Override
    synchronized public void onSensorChanged(SensorEvent event) {
        value = event.values[0];
        notify();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double value;
}
