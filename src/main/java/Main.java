import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            var response = "{method: \"GET\"}";

            try {
                responseStream.write(
                        ("HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + "application/json" + "\r\n" +
                                "Content-Length: " + response.length() + "\r\n" +
                                "Connection: close" + "\r\n" +
                                "\r\n").getBytes()
                );

                responseStream.write(
                        response.getBytes()
                );
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("POST", "/messages", ((request, responseStream) -> {
            var response = "{method: \"POST\"}";

            try {
                responseStream.write(
                        ("HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + "application/json" + "\r\n" +
                                "Content-Length: " + response.length() + "\r\n" +
                                "Connection: close" + "\r\n" +
                                "\r\n").getBytes()
                );

                responseStream.write(
                        response.getBytes()
                );
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        server.listen(9999);
    }
}
