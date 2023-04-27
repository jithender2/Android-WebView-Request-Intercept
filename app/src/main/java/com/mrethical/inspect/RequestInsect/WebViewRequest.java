package com.mrethical.inspect.RequestInsect;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.Locale;
import android.webkit.WebResourceRequest;
import java.util.HashMap;
import android.webkit.CookieManager;
import android.os.Build;

public class WebViewRequest {
    private final WebViewRequestType type;
    private final String url;
    private final String method;
    private final String body;
    private final Map<String, String> headers;
    private final String trace;
    private final String enctype;
    private final boolean isForMainFrame;
    private final boolean isRedirect;
    private final boolean hasGesture;

    public WebViewRequest(
        WebViewRequestType type,
        String url,
        String method,
        String body,
        Map<String, String> headers,
        String trace,
        String enctype,
        boolean isForMainFrame,
        boolean isRedirect,
        boolean hasGesture
    ) {
        this.type = type;
        this.url = url;
        this.method = method;
        this.body = body;
        this.headers = headers;
        this.trace = trace;
        this.enctype = enctype;
        this.isForMainFrame = isForMainFrame;
        this.isRedirect = isRedirect;
        this.hasGesture = hasGesture;
    }

    public WebViewRequestType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getTrace() {
        return trace;
    }

    public String getEnctype() {
        return enctype;
    }

    public boolean isForMainFrame() {
        return isForMainFrame;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public boolean hasGesture() {
        return hasGesture;
    }

    @Override
    public String toString() {
        StringBuilder traceStringBuilder = new StringBuilder();
		for (String traceLine : trace.split("\n")) {
			traceStringBuilder.append("    ").append(traceLine.trim()).append("\n");
		}
		String traceWithIndent = traceStringBuilder.toString();
		return String.format(Locale.US,
							 "Type: %s\n" +
							 "URL: %s\n" +
							 "Method: %s\n" +
							 "Body: %s\n" +
							 "Headers: %s" +
							 "Trace: %s" +
							 "Encoding type (form submissions only): %s\n" +
							 "Is for main frame? %s\n" +
							 "Is redirect? %s\n" +
							 "Has gesture? %s\n",
							 type,
							 url,
							 method,
							 body,
							 headers,
							 traceWithIndent,
							 enctype != null ? enctype : "",
							 isForMainFrame,
							 isRedirect,
							 hasGesture);
		
    }
	public static WebViewRequest create(WebResourceRequest webResourceRequest,RecordedRequest recordedRequest) {
    WebViewRequestType type = recordedRequest != null ? recordedRequest.getType() : WebViewRequestType.HTML;
    String url = webResourceRequest.getUrl().toString();
    String cookies = CookieManager.getInstance().getCookie(url) != null ? CookieManager.getInstance().getCookie(url) : "";
    HashMap<String, String> headers = new HashMap<>();
    headers.put("cookie", cookies);
    if (recordedRequest != null) {
        Map<String, String> recordedHeadersInLowercase = new HashMap<>();
        for (Map.Entry<String, String> entry : recordedRequest.getHeaders().entrySet()) {
            recordedHeadersInLowercase.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        headers.putAll(recordedHeadersInLowercase);
    }
    Map<String, String> requestHeaders = new HashMap<>();
    for (Map.Entry<String, String> entry : webResourceRequest.getRequestHeaders().entrySet()) {
        requestHeaders.put(entry.getKey().toLowerCase(), entry.getValue());
    }
    headers.putAll(requestHeaders);
    boolean isRedirect = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? webResourceRequest.isRedirect() : false;
    return new WebViewRequest(
            type,
            url,
            webResourceRequest.getMethod(),
            recordedRequest != null ? recordedRequest.getBody() : "",
            headers,
            recordedRequest != null ? recordedRequest.getTrace() : "",
            recordedRequest != null ? recordedRequest.getEncType() : null,
            webResourceRequest.isForMainFrame(),
            isRedirect,
            webResourceRequest.hasGesture()
    );
}

}
