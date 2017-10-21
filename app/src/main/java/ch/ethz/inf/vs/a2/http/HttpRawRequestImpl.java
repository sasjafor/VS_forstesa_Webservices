package ch.ethz.inf.vs.a2.http;

/**
 * Created by Christian on 14.10.17.
 */

public class HttpRawRequestImpl implements HttpRawRequest {

    // Method to generate a GET request which can be sent to the server.
    public String generateRequest(String host, int port, String path) {
        String GET_line = "GET " + path +  " HTTP/1.1\r\n";
        String host_line = "Host: " + host + ":" + port + "\r\n";
        String accept_line = "Accept: text/html\r\n";
        String connection_line = "Connection: close\r\n";
        String separator = "\r\n";
        System.out.println(GET_line + host_line + accept_line + connection_line + separator);
        return (GET_line + host_line + accept_line + connection_line + separator);
    }
}
