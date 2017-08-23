package com.youkpter.app.http;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by youkpter on 17-7-20.
 */
public class HttpResponse {
    private String version;
    private int status;
    private String reason;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    //source也许表示文件名更好些
    private File source;

    private OutputStream out;

    static final String listDirectory =
            "<html>"
        + "<body><ul>{}</ul></body>"
        + "</html>";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public OutputStream getOutputStream() {
        return out;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }
}
