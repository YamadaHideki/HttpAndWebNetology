import java.util.HashMap;
import java.util.Map;

public class Request {

    private String method;
    private Map<String, String> headers = new HashMap<>();
    private final StringBuilder sb = new StringBuilder();

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addBody(String s) {
        sb.append(s);
    }

}
