import org.apache.hc.core5.http.NameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private String method;
    private Map<String, String> headers = new HashMap<>();
    private final StringBuilder sb = new StringBuilder();
    private List<NameValuePair> params = new ArrayList<>();

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

    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    public String getQueryParam(String name) {
        var value = params.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElse(null);
        return value != null ? value.getValue() : null;
    }

    public List<NameValuePair> getQueryParams() {
        return params;
    }

    public boolean hasParams() {
        return params.size() > 0;
    }
}
