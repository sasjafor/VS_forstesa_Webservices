package ch.ethz.inf.vs.a2.webservices;

import java.util.HashMap;
import java.util.Map;

public class HttpPayload {

    public HttpPayload(String payload) {
        String[] split_string = payload.split("\r\n");
        method = split_string[0].split(" ")[0];
        uri = split_string[0].split(" ")[1];
        version = split_string[0].split(" ")[2];
        header_map = new HashMap<>();
        for(int k = 1; k < split_string.length-1; k++){
            String[] header = split_string[k].split(": ", 2);
            header_map.put(header[0],header[1]);
        }
        body = split_string[split_string.length-1];
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
