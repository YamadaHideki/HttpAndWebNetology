import org.apache.hc.core5.http.NameValuePair;

import java.util.List;
import java.util.Map;

public interface IRequestBuilder {
    void setMethod(String method);

    void setHeaders(Map<String, String> headers);

    void setParams(List<NameValuePair> params);

    void addBody(String line);

    Request build();
}
