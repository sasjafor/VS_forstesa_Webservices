package ch.ethz.inf.vs.a2.sensor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;

/**
 * Created by Christian on 15.10.17.
 */

public class JsonSensor extends AbstractSensor {

    @Override
    public String executeRequest() throws Exception {

        String response = "";
        URL url = new URL("http://vslab.inf.ethz.ch:8081/sunspots/Spot1/sensors/temperature");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Accept", "application/json");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String t;
            while ((t = in.readLine()) != null) {
                response = response + t;
            }
            in.close();

        }
        finally {
            urlConnection.disconnect();
        }
        return response;
    }

    @Override
    public double parseResponse(String response) {

        try {
            JSONObject obj = new JSONObject(response);
            return Double.parseDouble(obj.getString("value"));
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        return Double.NaN;
    }
}