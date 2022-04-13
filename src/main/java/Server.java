import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Server {
    private final ExecutorService pool = Executors.newFixedThreadPool(64);
    private final Map<String, Map<String, MyHandler>> handlers = new HashMap<>();

    public Server() {
        handlers.put("GET", new HashMap<>());
        handlers.put("POST", new HashMap<>());
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                        pool.submit(() -> {
                            connectHandler(socket);
                            System.out.println("Socket closed: " + socket.isClosed());
                        });
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectHandler(Socket socket) {
        try (socket;
             final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1

            final var requestLine = in.readLine();

            final var parts = requestLine.split(" ");
            final var method = parts[0];
            final var pathQuery = parts[1];
            final var pathWithoutQuery = pathQuery.split(Pattern.quote("?"))[0];

            System.out.println(method + ", " + pathWithoutQuery);

            if (parts.length != 3) {
                // just close socket
                return;
            }

            Request request = new Request();
            request.setMethod(parts[0]);

            try {
                var params = URLEncodedUtils.parse(new URI(pathQuery), StandardCharsets.UTF_8);
                request.setParams(params);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            // обработка headers
            while (in.ready()) {
                String line = in.readLine();
                if (line.equals("")) {
                    break;
                }
                var lineAsKeyValue = line.split(":");
                request.addHeader(lineAsKeyValue[0], lineAsKeyValue[1]);
            }

            // обработка body
            while (in.ready()) {
                request.addBody(in.readLine());
            }

            handlers.entrySet().stream()
                    .filter(s -> s.getKey().equals(method))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .filter(s -> s.containsKey(pathWithoutQuery))
                    .ifPresentOrElse(
                            s -> s.get(pathWithoutQuery).handle(request, out),
                            () -> pageNotFound(out)
                    );

            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, MyHandler handler) {
        Map<String, MyHandler> newMap = new HashMap<>();
        newMap.put(path, handler);

        if (!handlers.containsKey(method)) {
            handlers.put(method, newMap);
        } else {
            handlers.entrySet().stream()
                    .filter(s -> s.getKey().equals(method))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .ifPresent(editMap -> editMap.put(path, handler));
        }
    }

    public void pageNotFound(BufferedOutputStream responseStream) {
        try {
            responseStream.write(
                    ("HTTP/1.1 404 Not Found" + "\r\n" +
                            "Content-Length: 0" + "\r\n" +
                            "Connection: close" + "\r\n" +
                            "\r\n").getBytes()
            );
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


