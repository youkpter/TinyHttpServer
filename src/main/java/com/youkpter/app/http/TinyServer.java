package com.youkpter.app.http;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * Created by youkpter on 17-7-16.
 */
public class TinyServer {

    private static Logger log = LoggerFactory.getLogger(TinyServer.class);
    private int port;

    public static void main(String[] args) throws IOException {
        TinyServer server = new TinyServer(8080);
        server.start();
    }

    public TinyServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket s = new ServerSocket(this.port);
        while(true) {
            try {
                Socket incoming = s.accept();
                handleRequest(incoming);
            } catch (IOException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private static void handleRequest(Socket socket) throws IOException {
        HttpRequest request = new HttpRequest(socket.getInputStream());
        request.parseRequest();

        log.info("request: {}", request.getUrl());

        HttpResponse response = generateResponse(request);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        printResponse(out, response);
        socket.close();
    }

    private static HttpResponse generateResponse(HttpRequest request) {
        log.info("starting generaResponse");

        HttpResponse response = new HttpResponse();

        response.setVersion(request.getVersion());
        response.setStatus(200);
        response.setReason("OK");
        response.getHeaders().put("Content-Type","text/plain");

        String sb = "You requested " + request.getUrl() + ".\n" +
                "The request body is " + request.getBody() + "\n";
        response.setBody(sb);

        response.getHeaders().put("Content-Length",
                String.valueOf(response.getBody().length()));

        return response;
    }

    private static void printResponse(PrintWriter out, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append(response.getVersion()).append(" ")
                .append(response.getStatus()).append(" ")
                .append(response.getReason()).append("\r\n");

        for(Map.Entry<String, String> entry : response.getHeaders().entrySet())
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");

        sb.append("\r\n");
        sb.append(response.getBody());

        out.print(sb.toString());
        out.flush();

        log.info("printResponse over");

    }
}
