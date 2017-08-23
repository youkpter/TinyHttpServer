package com.youkpter.app.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by youkpter on 17-7-20.
 */
public class HttpResponse {
    private static Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private final String version = "HTTP/1.1";
    private int status;
    private String reason;
    private Map<String, String> headers = new HashMap<>();

    /*
     * the response body
     */
    private String body;

    /*
     *  the response body is the file content
     */
    private File source;

    private OutputStream outputStream;

    static final String listDirectory =
            "<html>"
        + "<body><ul>{}</ul></body>"
        + "</html>";

    public HttpResponse(HttpStatus status) {
        this.status = status.code();
        this.reason = status.reason();
    }

    public void output() {
        StringBuilder sb = new StringBuilder();

        // response status line
        sb.append(version).append(" ")
            .append(status).append(" ")
            .append(reason).append("\r\n");

        // headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ")
                .append(entry.getValue()).append("\r\n");
        }

        sb.append("\r\n");
        log.info(sb.toString());
        PrintWriter out = new PrintWriter(outputStream);
        out.print(sb.toString());

        if (source != null) {
            try {
                Scanner in = new Scanner(source);
                while (in.hasNextLine()) {
                    out.println(in.nextLine());
                }
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
                e.printStackTrace();
            }
        } else if (body != null) {
            out.print(body);
        }

        out.flush();

        log.info("printResponse over");

    }

    public String getVersion() {
        return version;
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

    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream out) {
        this.outputStream = out;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }
}
