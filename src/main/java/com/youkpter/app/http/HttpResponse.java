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
    private int statusCode;
    private String reason;
    private Map<String, String> headers = new HashMap<>();

    private OutputStream outputStream;
    private PrintWriter writer;

    static final String listDirectory =
            "<html>"
        + "<body><ul>{}</ul></body>"
        + "</html>";

    public HttpResponse(HttpStatus status) {
        this.statusCode = status.code();
        this.reason = status.reason();
    }

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.writer = new PrintWriter(outputStream);
    }

    /**
     *  send status line, headers and the empty line.
     */
    public void sendMetaData() {
        StringBuilder sb = new StringBuilder();

        // response status line
        sb.append(version).append(" ")
            .append(statusCode).append(" ")
            .append(reason).append("\r\n");

        // headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ")
                .append(entry.getValue()).append("\r\n");
        }

        sb.append("\r\n");
        writer.print(sb.toString());
        writer.flush();
    }


    public String getVersion() {
        return version;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatus(HttpStatus status) {
        this.statusCode = status.code();
        this.reason = status.reason();
    }

    public String getReason() {
        return reason;
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

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void send(String body) {
        sendMetaData();
        // send http body
        if (body != null) {
            writer.print(body);
            writer.flush();
        }
    }

    public void send(HttpStatus status) {
        setStatus(status);
        sendMetaData();
    }

    public void send(File file) {
        sendMetaData();

        if (file != null) {
            try {
                Scanner in = new Scanner(file);
                while (in.hasNextLine()) {
                    writer.println(in.nextLine());
                }
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
                e.printStackTrace();
            }
            writer.flush();
        }
    }
}
