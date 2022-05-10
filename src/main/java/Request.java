import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.util.*;
import java.util.stream.Collectors;

public class Request {

    private String method;

    private Map<String, String> headers;
    private final StringBuilder body;
    private List<NameValuePair> paramsForGetMethod;
    private List<NameValuePair> paramsForPostMethod;

    public Request(String method, Map<String, String> headers, List<NameValuePair> paramsForGetMethod, List<NameValuePair> paramsForPostMethod, StringBuilder body) {
        this.method = method;
        this.headers = headers;
        this.paramsForGetMethod = paramsForGetMethod;
        this.paramsForPostMethod = paramsForPostMethod;
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

    public void setParamsForGetMethod(List<NameValuePair> paramsForGetMethod) {
        this.paramsForGetMethod = paramsForGetMethod;
    }

    public void setParamsForPostMethod(List<NameValuePair> paramsForPostMethod) {
        this.paramsForPostMethod = paramsForPostMethod;
    }

    public void addPostParam(String k, String v) {
        paramsForPostMethod.add(new BasicNameValuePair(k, v));
    }

    public String getHeaderValueByKey(String k) {
        var value = headers.entrySet().stream()
                .filter(s -> s.getKey().equals(k))
                .findFirst()
                .orElse(null);
        return value != null ? value.getValue() : null;
    }

    public List<NameValuePair> getQueryParams() {
        return paramsForGetMethod;
    }

    public List<NameValuePair> getPostParam(String name) {
        return paramsForPostMethod.stream()
                .filter(s -> s.getName().equals(name))
                .collect(Collectors.toList());
    }

    public List<NameValuePair> getPostParams() {
        return paramsForPostMethod;
    }

    public List<NameValuePair> getQueryParam(String name) {
        return paramsForGetMethod.stream()
                .filter(s -> s.getName().equals(name))
                .collect(Collectors.toList());
    }

    public boolean hasGetParams() {
        return paramsForGetMethod.size() > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Headers: {\r\n");
        headers.forEach((key, value) -> sb.append(key).append(": ").append(value).append("\r\n"));

        sb.append("} \r\nBody: {\r\n");
        sb.append(body.toString());

        sb.append("\r\n} \r\nGET Params: {\r\n");
        paramsForGetMethod.forEach((s) -> sb.append(s.getName()).append(": ").append(s.getValue()).append("\r\n"));

        sb.append("} \r\nPOST Params: {\r\n");
        paramsForPostMethod.forEach((s) -> sb.append(s.getName()).append(": ").append(s.getValue()).append("\r\n"));

        sb.append("}\r\n");

        return sb.toString();
    }

    public boolean doQualityCheck() {
        return method != null && headers != null && paramsForGetMethod != null && paramsForPostMethod != null && body != null;
    }
}
