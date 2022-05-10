import org.apache.hc.core5.http.NameValuePair;

import java.util.*;
import java.util.stream.Collectors;

public class Request {

    private String method;
    private final StringBuilder body;
    private Map<String, String> headers;
    private List<NameValuePair> params;

    public Request(String method, Map<String, String> headers, List<NameValuePair> params, StringBuilder body) {
        this.method = method;
        this.headers = headers;
        this.params = params;
        this.body = body;
    }

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
        body.append(s);
    }

    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return params.stream()
                .filter(s -> s.getName().equals(name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getQueryParams() {
        return params;
    }

    public boolean hasParams() {
        return params.size() > 0;
    }

    public boolean doQualityCheck() {
        return method != null && headers != null && params != null;
    }
}
