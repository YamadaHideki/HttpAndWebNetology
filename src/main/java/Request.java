import java.util.Map;

public class Request {

    final private String method;
    final private Map<String, String> headers;
    final private String body;

    public Request(String method, Map<String, String> headers, String body) {
        this.method = method;
        this.headers = headers;
        this.body = body;
    }


}
