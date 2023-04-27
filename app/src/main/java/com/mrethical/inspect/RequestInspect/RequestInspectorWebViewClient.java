package com.mrethical.inspect.RequestInsect;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.mrethical.inspect.RequestInsect.RequestInspectorJavaScriptInterface;

public class RequestInspectorWebViewClient extends WebViewClient {

    private static final String LOG_TAG = "RequestInspectorWebView";
    private RequestInspectorJavaScriptInterface interceptionJavascriptInterface;
    private RequestInspectOptions options;

    public RequestInspectorWebViewClient(WebView webView) {
        this(webView, new RequestInspectOptions());
    }
    public RequestInspectorWebViewClient(WebView webView, RequestInspectOptions options) {
        this.options = options;
        interceptionJavascriptInterface = new RequestInspectorJavaScriptInterface(webView);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i(LOG_TAG, "Page started loading, enabling request inspection. URL: " + url);
        RequestInspectorJavaScriptInterface.enabledRequestInspection(view);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
		
        RecordedRequest recordedRequest = interceptionJavascriptInterface.findRecordedRequestForUrl(request.getUrl().toString());
        WebViewRequest webViewRequest = WebViewRequest.create(request, recordedRequest);
        return shouldInterceptRequest(view, webViewRequest);
    }

    public WebResourceResponse shouldInterceptRequest(WebView view, WebViewRequest webViewRequest) {
		
        logWebViewRequest(webViewRequest);
        return null;
    }
    protected void logWebViewRequest(WebViewRequest webViewRequest) {
        Log.i(LOG_TAG, "Sending request from WebView: " + webViewRequest.toString());
    }
}
