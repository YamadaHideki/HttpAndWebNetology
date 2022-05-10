import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.io.*;
import java.net.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Server {
    private final ExecutorService pool = Executors.newFixedThreadPool(64);
    private final Map<String, Map<String, MyHandler>> handlers = new ConcurrentHashMap<>();
    private final String HTTP_VERSION = "HTTP/1.1";
    private final StringBuilder sb = new StringBuilder();

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
                            System.out.println("Socket closed: " + socket.isClosed() + "\r\n");
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

            //Request request = new Request();
            var requestBuilder = new RequestBuilder();
            requestBuilder.setMethod(method);

            try {
                var queryParams = new URIBuilder(new URI(pathQuery), StandardCharsets.UTF_8).getQueryParams();
                requestBuilder.setParamsForGetMethod(queryParams);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            // обработка headers
            Map<String, String> headers = new HashMap<>();
            while (in.ready()) {
                String line = in.readLine();
                if (line.equals("")) {
                    break;
                }
                var lineAsKeyValue = line.split(":");
                headers.put(lineAsKeyValue[0], lineAsKeyValue[1]);
            }
            requestBuilder.setHeaders(headers);

            //var valueContentType = request.getHeaderValueByKey("Content-Type");
            String valueContentType = headers.get("Content-Type");
            System.out.println("VALUE_CONTENT_TYPE: " + valueContentType);

            if (valueContentType != null) {
                switch (valueContentType.trim()) {
                    case "application/x-www-form-urlencoded":
                        sb.setLength(0);

                        while (in.ready()) {
                            sb.append((char) in.read());
                        }

                        // В этом case зависает на in.readLine()
                        /*while (in.ready()) {
                            String lineDecoding = URLDecoder.decode(in.readLine(), StandardCharsets.UTF_8);
                            sb.append(lineDecoding);
                        }*/

                        String[] parseBody = sb.toString().split(Pattern.quote("&"));
                        List<NameValuePair> postParams = new ArrayList<>();
                        for (String s : parseBody) {
                            String[] split = URLDecoder.decode(s, StandardCharsets.UTF_8).split("=");
                            if (split.length > 1) {
                                postParams.add(new BasicNameValuePair(split[0], split[1]));
                            } else {
                                badRequest(out);
                                return;
                            }
                        }
                        requestBuilder.setParamsForPostMethod(postParams);
                        break;
                    case "multipart/form-data":
                        sb.setLength(0);

                        while (in.ready()) {
                            sb.append(in.readLine());
                        }

                        System.out.println(sb.toString());

                        break;
                    default:
                        while (in.ready()) {
                            requestBuilder.addBody(in.readLine());
                        }
                        break;
                }
            } else {
                while (in.ready()) {
                    requestBuilder.addBody(in.readLine());
                }
            }

            handlers.entrySet().stream()
                    .filter(s -> s.getKey().equals(method))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .filter(s -> s.containsKey(pathWithoutQuery))
                    .ifPresentOrElse(
                            s -> s.get(pathWithoutQuery).handle(requestBuilder.build(), out),
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
                    (HTTP_VERSION + " 404 Not Found" + "\r\n" +
                            "Content-Length: 0" + "\r\n" +
                            "Connection: close" + "\r\n" +
                            "\r\n").getBytes()
            );
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void badRequest(BufferedOutputStream responseStream) {
        try {
            responseStream.write(
                    (HTTP_VERSION + " 400 Bad Request" + "\r\n" +
                            "Content-Length: 0" + "\r\n" +
                            "Connection: close" + "\r\n" +
                            "\r\n").getBytes()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


