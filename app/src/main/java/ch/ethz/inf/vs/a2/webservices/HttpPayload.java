package ch.ethz.inf.vs.a2.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpPayload {

    public HttpPayload(BufferedReader in) throws IOException{
        StringBuilder builder = new StringBuilder();
        String line = in.readLine();
        String[] first_line = line.split(" ");

        method = first_line[0];
        uri = first_line[1];
        version = first_line[2];

        header_map = new HashMap<>();

        while (line != null) {
            line = in.readLine();
            if (line.equals("")) break;
            String[] header = line.split(": ", 2);
            header_map.put(header[0],header[1]);
        }

        if (header_map.containsKey("Content-Length")) {
            int length = Integer.parseInt(header_map.get("Content-Length"));
            char[] cbuf = new char[length];
            in.read(cbuf);
            body = String.valueOf(cbuf);
        } else {
            body = "";
        }
    }

    public Map<String, String> getHeaderMap () {
        return header_map;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }

    public String getBody() {
        return body;
    }

    private String method;
    private String uri;
    private String version;
    private String body;
    private Map<String, String> header_map;
}
