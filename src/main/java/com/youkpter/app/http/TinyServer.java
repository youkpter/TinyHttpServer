package com.youkpter.app.http;

import static com.youkpter.app.http.HttpRequest.METHOD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by youkpter on 17-7-16.
 */
public class TinyServer {

    private static Logger log = LoggerFactory.getLogger(TinyServer.class);
    private static Set<METHOD> supportedMethods;
    private static final String BASE_PATH = TinyServer.class.getClassLoader()
            .getResource("").getPath();
    private int port;

    static {
        supportedMethods = EnumSet.of(METHOD.GET, METHOD.HEAD);
    }

    public static void main(String[] args) throws IOException {
        TinyServer server;
        if (args.length < 1) {
            log.info("using default port:8080");
            server = new TinyServer(8080);
        } else {
            int port = Integer.parseInt(args[0]);
            server = new TinyServer(port);
        }
        server.start();
    }

    public TinyServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket s = new ServerSocket(this.port);
        log.info("start listening port {}", port);
        while (true) {
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

        HttpResponse response = generateResponse(request);
        response.setOutputStream(socket.getOutputStream());

        printResponse(response);
        socket.close();
    }

    private static HttpResponse generateResponse(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        log.info("starting generateResponse");

        response.setVersion(request.getVersion());

        if (!supportedMethods.contains(request.getMethod())) {
            response.setStatus(501);
            response.setReason("Not Implemented");
            return response;
        }


        String url = request.getUrl();
        File file = new File(TinyServer.BASE_PATH + url);
        log.info("try to access file: {}", file.toString());

        if (file.exists() && file.canRead()) {
            response.setStatus(200);
            response.setReason("OK");

            if (url.endsWith(".html"))
                response.getHeaders().put("Content-Type", "text/html");
            else //TODO: detect the file's real content-type
                response.getHeaders().put("Content-Type", "text/plain");
            response.getHeaders().put("Content-Length",
                    String.valueOf(file.length()));
            if (request.getMethod() == METHOD.GET)
                response.setSource(file);
        } else {
            log.info("{} is not existed or cannot read", file.toString());
            response.setStatus(404);
            response.setReason("Not Found");
        }

        return response;
    }

    private static void printResponse(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append(response.getVersion()).append(" ")
                .append(response.getStatus()).append(" ")
                .append(response.getReason()).append("\r\n");

        for (Map.Entry<String, String> entry : response.getHeaders().entrySet())
            sb.append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append("\r\n");

        sb.append("\r\n");
        log.info(sb.toString());
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.print(sb.toString());

        if (response.getSource() != null) {
            File file = response.getSource();
            try {
                Scanner in = new Scanner(file);
                while (in.hasNextLine()) {
                    out.println(in.nextLine());
                }
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
                e.printStackTrace();
            }
        } else if (response.getBody() != null)
            out.print(response.getBody());

        out.flush();

        log.info("printResponse over");

    }
}
