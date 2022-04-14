import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.util.*;

public class Request {

    private String method;
    private Map<String, String> headers = new HashMap<>();
    private final StringBuilder body = new StringBuilder();
    private List<NameValuePair> params = new ArrayList<>();
    private List<NameValuePair> postParams = new ArrayList<>();

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

    public void setPostParams(List<NameValuePair> postParams) {
        this.postParams = postParams;
    }

    public void addPostParam(String k, String v) {
        postParams.add(new BasicNameValuePair(k, v));
    }

    public String getHeaderValueByKey(String k) {
        var value = headers.entrySet().stream()
                .filter(s -> s.getKey().equals(k))
                .findFirst()
                .orElse(null);
        return value != null ? value.getValue() : null;
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

    public void getPostParam(String name) {

    }

    public void getPostParams() {

    }

    public boolean hasParams() {
        return params.size() > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Headers: {\r\n");
        headers.forEach((key, value) -> sb.append(key).append(": ").append(value).append("\r\n"));

        sb.append("} \r\nBody: {\r\n");
        sb.append(body.toString());

        sb.append("\r\n} \r\nGET Params: {\r\n");
        params.forEach((s) -> sb.append(s.getName()).append(": ").append(s.getValue()).append("\r\n"));
        sb.append("} \r\nPOST Params: {\r\n");
        postParams.forEach((s) -> sb.append(s.getName()).append(": ").append(s.getValue()).append("\r\n"));
        sb.append("}\r\n");

        return sb.toString();
    }
}
