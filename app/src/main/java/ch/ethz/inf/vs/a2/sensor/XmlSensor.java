package ch.ethz.inf.vs.a2.sensor;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class XmlSensor extends AbstractSensor {

    public String executeRequest() throws Exception {
        // throws IOException

        String response = "";

        // soap request for Spot3
        String soapXmlRequestSpot3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <S:Header/>\n" +
                "    <S:Body>\n" +
                "        <ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\">\n" +
                "            <id>Spot3</id>\n" +
                "        </ns2:getSpot>\n" +
                "    </S:Body>\n" +
                "</S:Envelope>";

        // soap request for Spot4, unused
        String soapXmlRequestSpot4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <S:Header/>\n" +
                "    <S:Body>\n" +
                "        <ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\">\n" +
                "            <id>Spot4</id>\n" +
                "        </ns2:getSpot>\n" +
                "    </S:Body>\n" +
                "</S:Envelope>";

        // setting up the connection
        URL url = new URL("http://vslab.inf.ethz.ch:8080/\n" +
                "SunSPOTWebServices/SunSPOTWebservice");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
        urlConnection.setRequestProperty("SOAPAction", "http://webservices.vslecture.vs.inf.ethz.ch/");
        urlConnection.setDoOutput(true);

        // retrieving values from Spot3
        OutputStream outputStream = urlConnection.getOutputStream();
        outputStream.write(soapXmlRequestSpot3.getBytes());
        outputStream.flush();

        // reading the response
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String text;
        while ((text = bufferedReader.readLine()) != null) {
            response = response + text;
        } bufferedReader.close();

        urlConnection.disconnect();

        return response;
    }

    public double parseResponse(String response) {
        double temp = 0;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(response));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("temperature")) {
                        temp = Double.parseDouble(parser.nextText());
                    }
                }
                eventType = parser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return temp;
    }
}
