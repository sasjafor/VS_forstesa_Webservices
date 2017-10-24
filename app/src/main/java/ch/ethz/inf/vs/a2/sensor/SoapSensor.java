package ch.ethz.inf.vs.a2.sensor;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class SoapSensor extends AbstractSensor {

    public String executeRequest() throws Exception {
        // throws IOException, XmlPullParserException

        String response;

        final String URL = "http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice?WSDL";
        final String NameSpace = "http://webservices.vslecture.vs.inf.ethz.ch/";
        final String SOAPAction = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getSpotRequest";
        //final String SOAPAction = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getDiscoveredSpotsRequest";
        final String MethodName = "getSpot";
        //final String MethodName = "getDiscoveredSpots";

        SoapObject soapObject = new SoapObject(NameSpace, MethodName);

        // argument passed to getSpot method, as in soap request body (see tester), could also be "Spot4" instead
        soapObject.addProperty("id", "Spot3");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(soapObject);

        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        httpTransport.call(SOAPAction, envelope);

        SoapObject result = (SoapObject) envelope.bodyIn; // result.getPropertyCount() == 1
        // System.out.println(result.toString());

        SoapObject step = (SoapObject) result.getProperty(0);
        response = step.getPrimitivePropertyAsString("temperature");

        return response;
    }

    public double parseResponse(String response) {
        double temp = 0;
        try {
            temp = Double.parseDouble(response);
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
        return temp;
    }
}
