package netoTestWebForms;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    final char DELIMITER = '?';
    private String method;
    private String path;
    private Map<String, String> headers;
    private String body;
    private List<NameValuePair> queryParams;

    public Request() {
        this.headers = new ConcurrentHashMap<>();
        this.method = "";
        this.path = "";
        this.body = "";
    }

    public Optional<String> extractHeader(String header) { //из списка строк мы вытскивем header, причем его значение
        return headers.entrySet()//преобразовываем его в стрим
                .stream()
                .filter(o -> o.getKey().equals(header))// оставляем только строки которые начинаются с искомого header
                .map(o -> o.getValue())
                .findFirst();
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setQueryParams() {
        int delimiter = path.indexOf(DELIMITER);
        if (delimiter == -1) return;
        queryParams = URLEncodedUtils.parse(path.substring(delimiter + 1), StandardCharsets.UTF_8);
    }

    public List<NameValuePair> getBodyParams() {
        return URLEncodedUtils.parse(body, Charset.forName("UTF-8"));
    }

    public String getBody() {
        return body;
    }

    public String getPathWithoutQueryParams() {
        int queryDelimiter = path.indexOf(DELIMITER);
        if (queryDelimiter != -1) {
            path = path.substring(0, queryDelimiter);
            System.out.println(path);
        }
        return path;
    }

    public Optional<String> getQueryParamValue(String queryParam) {
        return queryParams.stream()
                .filter(o -> o.getName().equals(queryParam))
                .map(o -> o.getValue())
                .findFirst();
    }

    public Optional<String> getHeaderValue(String header) {
        return headers.entrySet()
                .stream()
                .filter(o -> o.getKey().equals(header))
                .map(o -> o.getValue())
                .findFirst();
    }

    public boolean addHeader(String header) {
        String[] headerParts = header.split(":");
        if (headerParts.length == 2) {
            this.headers.put(headerParts[0], headerParts[1].replace(" ",""));
            return true;
        } else {
            return false;
        }
    }
}
