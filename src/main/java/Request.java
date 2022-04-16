import org.apache.commons.fileupload.RequestContext;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request implements RequestContext {

    private String method;
    private Map<String, String> headers = new HashMap<>();
    private final StringBuilder body = new StringBuilder();
    private List<NameValuePair> paramsForGetMethod = new ArrayList<>();
    private List<NameValuePair> paramsForPostMethod = new ArrayList<>();

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

    public String getQueryParam(String name) {
        return getParam(name, paramsForGetMethod);
    }

    public List<NameValuePair> getQueryParams() {
        return paramsForGetMethod;
    }

    public String getPostParam(String name) {
        return getParam(name, paramsForPostMethod);
    }

    public List<NameValuePair> getPostParams() {
        return paramsForPostMethod;
    }

    private String getParam(String name, List<NameValuePair> params) {
        var value = params.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElse(null);
        return value != null ? value.getValue() : null;
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

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.displayName();
    }

    @Override
    public String getContentType() {
        return getHeaderValueByKey("Content-Type").trim();
    }

    @Override
    public int getContentLength() {
        return Integer.parseInt(getHeaderValueByKey("Content-Length").trim());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(body.toString().getBytes());
    }
}
