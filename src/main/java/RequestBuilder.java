import org.apache.hc.core5.http.NameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestBuilder implements IRequestBuilder{

    private String method;
    private final StringBuilder body = new StringBuilder();
    private Map<String, String> headers = new HashMap<>();
    private List<NameValuePair> paramsForGetMethod = new ArrayList<>();
    private List<NameValuePair> paramsForPostMethod = new ArrayList<>();

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public void setParamsForGetMethod(List<NameValuePair> params) {
        this.paramsForGetMethod = params;
    }

    @Override
    public void setParamsForPostMethod(List<NameValuePair> params) {
        this.paramsForPostMethod = params;
    }

    @Override
    public void addBody(String line) {
        body.append(line);
    }

    @Override
    public Request build() {
        Request request = new Request(method, headers, paramsForGetMethod, paramsForPostMethod, body);
        if (request.doQualityCheck()) {
            return request;
        }
        return null;
    }
}
