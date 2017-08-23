package com.youkpter.app.http;

import static com.youkpter.app.http.HttpRequest.Method;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by youkpter on 17-7-16.
 */
public class TinyServer {

    private static Logger log = LoggerFactory.getLogger(TinyServer.class);

    private static Set<Method> supportedMethods;
    private static final String BASE_PATH = TinyServer.class.getClassLoader()
            .getResource("").getPath();
    private int port;

    static {
        supportedMethods = EnumSet.of(Method.GET, Method.HEAD);
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

        response.output();
        socket.close();
    }

    private static HttpResponse generateResponse(HttpRequest request) {
        HttpResponse response;
        log.info("starting generateResponse");

        if (!supportedMethods.contains(request.getMethod())) {
            return new HttpResponse(HttpStatus.NOT_IMPLEMENTED);
        }

        String uri = request.getUri();
        File file = new File(TinyServer.BASE_PATH + uri);
        log.info("try to access file: {}", file.toString());

        if (!file.exists() || !file.canRead()) {
            log.info("{} is not existed or cannot read", file.toString());
            return new HttpResponse(HttpStatus.NOT_FOUND);
        } else {
            response = new HttpResponse(HttpStatus.OK);

            setContentType(file, response);

            if (file.isFile()) {
                dealWithFile(request, response, file);
            } else if (file.isDirectory()) {
                log.info("request-uri: {} is a directory.", file.getName());

                File welcomeFile = new File(file + "/index.html");

                if (welcomeFile.exists()) {
                    log.info("welcomeFile({}) exists", welcomeFile.getName());
                    dealWithFile(request, response, welcomeFile);
                } else { // list this directory
                    log.info("try to list directory({})", file.getName());
                    dealWithListDirectory(request, response, file);
                }
            } else {
                log.warn("special file");
                response = new HttpResponse(HttpStatus.NOT_FOUND);
            }
        }

        if (request.getMethod() == Method.HEAD) {
            response.setSource(null);
            response.setBody(null);
        }

        return response;
    }


    private static void setContentType(File file, HttpResponse response) {
        // if request a file which suffix is not html
        if (file.isFile() && !file.getName().endsWith(".html")) {
            //TODO: detect the file's real content-type
            response.addHeader("Content-Type", "text/plain");
        } else {
            response.addHeader("Content-Type", "text/html");
        }
    }

    private static void dealWithFile(HttpRequest req, HttpResponse response, File file) {
        response.addHeader("Content-Length", String.valueOf(file.length()));

        if (req.getMethod() == Method.HEAD) {
            return;
        }
        response.setSource(file);
    }

    private static void dealWithListDirectory(HttpRequest req, HttpResponse response, File file) {
        // default size may be too small
        StringBuilder sb = new StringBuilder(256);
        for (File f : file.listFiles()) {
            sb.append("<li>").append(f.getName()).append("</li>\n");
        }
        String body = HttpResponse.listDirectory.replace("{}", sb.toString());
        response.addHeader("Content-Length", String.valueOf(body.length()));

        // set body, if and only if request method is GET.(HEAD method doesn't need body)
        if (req.getMethod() == Method.GET) {
            response.setBody(body);
        }
    }
}
