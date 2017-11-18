package com.youkpter.app.http;

import static com.youkpter.app.http.HttpRequest.Method;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by youkpter on 17-7-16.
 */
public class TinyServer {

    private static Logger log = LoggerFactory.getLogger(TinyServer.class);

    private static final String BASE_PATH = TinyServer.class.getClassLoader()
        .getResource("").getPath();
    private int port;

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
                incoming.close();
            } catch (IOException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
            log.info("finish request");
        }
    }


    private static void handleRequest(Socket socket) throws IOException {
        HttpRequest request = new HttpRequest(socket.getInputStream());
        request.parseRequest();

        HttpResponse response = new HttpResponse(socket.getOutputStream());
        doInternalRequest(request, response);
    }

    private static void doInternalRequest(HttpRequest request, HttpResponse response) {
        switch (request.getMethod()) {
            case GET:
                doGet(request, response);
                break;
            case HEAD:
                doHead(request, response);
                break;
            default:
                doNotImplemented(request, response);
                break;
        }
    }

    private static void doNotImplemented(HttpRequest request, HttpResponse response) {
        response.send(HttpStatus.NOT_IMPLEMENTED);
    }


    private static void doHead(HttpRequest request, HttpResponse response) {
        // the methods invoked by doGet have already taking head method into account
        doGet(request, response);
    }

    private static void doGet(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        File file = new File(TinyServer.BASE_PATH + uri);

        if (!file.exists() || !file.canRead()) {
            log.info("{} is not existed or cannot read", file.toString());
            response.send(HttpStatus.NOT_FOUND);
        } else {
            response.setStatus(HttpStatus.OK);
            setContentType(file, response);
            if (file.isFile()) {
                dealWithFile(request, response, file);
            } else if (file.isDirectory()) {
                dealWithWelcomeFile(request, response, file);
            } else {
                log.warn("special file");
                response.send(HttpStatus.NOT_FOUND);
            }

        }
    }

    private static void dealWithWelcomeFile(HttpRequest request, HttpResponse response, File file) {
        File welcomeFile = new File(file + "/index.html");

        if (welcomeFile.exists()) {
            dealWithFile(request, response, welcomeFile);
        } else { // list this directory
            listDirectory(request, response, file);
        }
    }

    private static void setContentType(File file, HttpResponse response) {
        // if request a file which suffix is not html
        if (file.isFile() && !file.getName().endsWith(".html")) {
            //TODO: detect the file's real content-type
            response.addHeader("Content-Type", "text/plain; charset=utf-8");
        } else {
            response.addHeader("Content-Type", "text/html; charset=utf-8");
        }
    }


    private static void dealWithFile(HttpRequest req, HttpResponse response, File file) {
        response.addHeader("Content-Length", String.valueOf(file.length()));

        if (req.getMethod() == Method.HEAD) {
            response.send((String) null);
            return;
        }

        response.send(file);

    }

    private static void listDirectory(HttpRequest req, HttpResponse response, File file) {
        // default size may be too small
        StringBuilder sb = new StringBuilder(256);
        for (File f : file.listFiles()) {
            sb.append("<li>").append(f.getName()).append("</li>\n");
        }
        String body = HttpResponse.listDirectory.replace("{}", sb.toString());
        int contentLength = 0;
        try {
            // response's charset is utf-8
            contentLength = body.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.addHeader("Content-Length", String.valueOf(contentLength));

        // if this is HEAD method, we don't need the http body
        if (req.getMethod() == Method.HEAD) {
            body = null;
        }

        response.send(body);
    }
}
