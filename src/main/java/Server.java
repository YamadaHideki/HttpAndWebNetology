import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.util.Streams;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.*;
import java.net.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Server {
    private final ExecutorService pool = Executors.newFixedThreadPool(64);
    private final Map<String, Map<String, MyHandler>> handlers = new HashMap<>();
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

            Request request = new Request();
            request.setMethod(method);

            // URLEncodedUtils was Deprecated
            try {
                var params = URLEncodedUtils.parse(new URI(pathQuery), StandardCharsets.UTF_8);
                request.setParamsForGetMethod(params);
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

            var valueContentType = request.getHeaderValueByKey("Content-Type");
            System.out.println("VALUE_CONTENT_TYPE: " + valueContentType);

            if (valueContentType != null) {
                switch (valueContentType.trim().split(Pattern.quote(";"))[0]) {
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
                        for (String s : parseBody) {
                            if (!s.equals("") && s.length() > 1) {
                                String[] split = URLDecoder.decode(s, StandardCharsets.UTF_8).split("=");
                                if (split.length == 2) {
                                    request.addPostParam(split[0], split[1]);
                                } else if (split.length == 1) {
                                    request.addPostParam(split[0], "");
                                } else {
                                    badRequest(out);
                                    return;
                                }
                            }
                        }
                        break;
                    case "multipart/form-data":
                        sb.setLength(0);

                        while (in.ready()) {
                            request.addBody(in.readLine());
                        }

                        FileUpload upload = new FileUpload(new DiskFileItemFactory());

                        /* Итератор возвращается пустой, не могу понять причину. Через debug смотрел, вроде все данные забирает и кодировку,
                        * и длину файла и байтовый массив body, while (iter.hasNext()) - неотрабатывает */
                        FileItemIterator iter = upload.getItemIterator(request);
                        while (iter.hasNext()) {
                            FileItemStream item = iter.next();
                            String name = item.getFieldName();
                            System.out.println(name);
                            InputStream stream = item.openStream();
                            if (item.isFormField()) {
                                System.out.println("Form field " + name + " with value "
                                        + Streams.asString(stream) + " detected.");
                            } else {
                                System.out.println("File field " + name + " with file name "
                                        + item.getName() + " detected.");
                                // Process the input stream
                            }
                        }
                        break;
                    default:
                        while (in.ready()) {
                            request.addBody(in.readLine());
                        }
                        break;
                }
            } else {
                while (in.ready()) {
                    request.addBody(in.readLine());
                }
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

        } catch (IOException | FileUploadException e) {
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


