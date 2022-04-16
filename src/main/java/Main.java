import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{\"method\": \"GET\"");
            if (request.hasGetParams()) {
                jsonResponse.append(", \"params\": [");

                var params = request.getQueryParams();
                for (int i = 0; i < params.size(); i++) {
                    jsonResponse.append("{\"").append(params.get(i).getName()).append("\"").append(": ")
                            .append("\"").append(params.get(i).getValue()).append("\"}");
                    if (i + 1 != params.size()) {
                        jsonResponse.append(",");
                    }
                }

                jsonResponse.append("]");
            }
            jsonResponse.append("}");

            try {
                responseStream.write(
                        ("HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + "application/json" + "\r\n" +
                                "Content-Length: " + jsonResponse.length() + "\r\n" +
                                "Connection: close" + "\r\n" +
                                "\r\n").getBytes()
                );

                responseStream.write(
                        jsonResponse.toString().getBytes()
                );
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("POST", "/messages", ((request, responseStream) -> {
            System.out.println(request);
            var response = "{\"method\": \"POST\"}";

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
