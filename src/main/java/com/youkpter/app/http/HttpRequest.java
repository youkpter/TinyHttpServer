package com.youkpter.app.http;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by youkpter on 17-7-20.
 */
public class HttpRequest {
    private static Logger log = LoggerFactory.getLogger(HttpRequest.class);

    public enum Method { GET, HEAD, POST, DELETE }

    private Method method;
    private String uri;
    private String version;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters;
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
        log.info(startLine);
        String[] items = startLine.split(" ");
        assert (items.length == 3);
        this.method = Method.valueOf(items[0]);

        String uri = items[1];
        int idx = uri.indexOf('?');
        if (idx == -1) {
            this.uri = uri;
        } else { // parse parameters
            this.uri = uri.substring(0, idx);
            String params = uri.substring(idx + 1);
            String[] pairs = params.split("&");
            Map<String, String> paramMap = new HashMap<>(pairs.length * 4 / 3 + 1);
            for (String pair : pairs) {
                String[] tokens = pair.split("=");
                paramMap.put(tokens[0], tokens[1]);
            }
            this.parameters = paramMap;
        }

        this.version = items[2];
    }

    private void parseHeaders() {
        String header;
        String[] items;

        //TODO each header line ends with CRLF, maybe we need make it clear
        while (in.hasNextLine()) {
            header = in.nextLine();
            if (header.length() == 0) { // a empty line is the boundary of headers
                // and optional body
                break;
            }

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
        if (lengthStr == null) {
            return;
        }
        int len = Integer.parseInt(lengthStr);

        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = in.nextByte();
        }

        // at now, let's consider it as plain text
        String charset = getCharset();
        if (charset != null) {
            try {
                body = new String(bytes, charset);
            } catch (UnsupportedEncodingException e) {
                body = new String(bytes);
                e.printStackTrace();
            }
        } else {
            body = new String(bytes);
        }
    }

    private String getCharset() {
        String contentType = headers.get("content-type");
        String[] items = contentType.split(";");
        if (items.length == 1) {
            return null;
        }

        for (int i = 1; i < items.length; i++) {
            String[] paramter = items[i].split("=");
            if (paramter[0].equalsIgnoreCase("charset")) {
                return paramter[1];
            }
        }
        return null;

    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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
