package ch.ethz.inf.vs.a2.sensor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;
import ch.ethz.inf.vs.a2.http.HttpRawRequestImpl;

/**
 * Created by Christian on 14.10.17.
 */

public class RawHttpSensor extends AbstractSensor {

    public String executeRequest() throws Exception {

        String response = "";
        try {

            // Initialize socket for communication
            Socket s = new Socket("vslab.inf.ethz.ch", 8081);

            // Generate request to send
            HttpRawRequest request = new HttpRawRequestImpl();
            String req = request.generateRequest("vslab.inf.ethz.ch", 8081, "/sunspots/Spot1/sensors/temperature");

            // Send request
            PrintWriter out = new PrintWriter(s.getOutputStream());
            out.print(req);
            out.flush();

            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String t;
            while ((t = in.readLine()) != null) {
                response = response + t;
            }
            in.close();

        } catch (Exception e) {
            System.out.println("Error: " + e);
            return null;
        }

        System.out.println("DEBUG: response " + response);
        return response;
    }

    @Override
    public double parseResponse(String response) {

        // Return NaN when GET request was not successful:
        if (!response.substring(0, 15).contains("200 OK")) {
            return Double.NaN;
        }
        Pattern pattern = Pattern.compile("<span class=\"getterValue\">([0-9.]+)</span>");
        Matcher matcher = pattern.matcher(response);
        matcher.find();
        Double temp = Double.parseDouble(matcher.group(1));
        return temp;

    }
}