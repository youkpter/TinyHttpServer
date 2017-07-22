package com.youkpter.app.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by youkpter on 17-7-20.
 */
public class HttpRequest {
    private String method;
    private String url;
    private String version;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    private InputStream inputStream;
    private Scanner in;

    public HttpRequest(InputStream inputStream) {
        this.inputStream = inputStream;
        this.in = new Scanner(inputStream);
    }


    public void parseRequest() {
        parseStartLine();
        parseHeaders();
        parseEntityBody();
    }

    private void parseStartLine() {
        String startLine = in.nextLine();
        String[] items = startLine.split(" ");
        assert(items.length == 3);
        this.method = items[0];
        this.url = items[1];
        this.version = items[2];
    }

    private void parseHeaders() {
        String header = null;
        String[] items;

        while(in.hasNextLine()) {
            header = in.nextLine();
            if(header.length() == 0) // a empty line is the boundary of headers and body
                break;

            items = header.split(":", 2);
            headers.put(items[0].toLowerCase(), items[1].trim());
        }
    }

    /*
     * rfc1945
     * An entity body is included with a request message only when the
     * request method calls for one. The presence of an entity body in a
     * request is signaled by the inclusion of a Content-Length header field
     * in the request message headers. HTTP/1.0 requests containing an
     * entity body must include a valid Content-Length header field.
     *
     * so, the entity exists in request message, if and only if Content-Length
     * header exists.
     */
    private void parseEntityBody() {
        String lengthStr = headers.get("content-length");
        if(lengthStr == null)
            return;
        int len = Integer.parseInt(lengthStr);

        byte[] bytes = new byte[len];
        for(int i = 0; i < len; i++) {
            bytes[i] = in.nextByte();
        }

        // at now, let's consider it as plain text
        body = new String(bytes);

    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
