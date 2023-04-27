package com.mrethical.inspect.RequestInsect;

import android.webkit.WebView;
import java.util.ArrayList;
import java.util.Map;
import org.json.JSONArray;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import android.webkit.JavascriptInterface;
import com.mrethical.inspect.RequestInsect.RequestInspectOptions;
public class RequestInspectorJavaScriptInterface {
    private WebView mWebView;
    public RequestInspectorJavaScriptInterface(WebView webView) {
		mWebView = webView;
		mWebView.addJavascriptInterface(this,RequestInspectorJs.INTERFACE_NAME);
        
    }
	private ArrayList<RecordedRequest> recordedRequests = new ArrayList<>();
	public RecordedRequest findRecordedRequestForUrl(String url) {
		synchronized (recordedRequests) {
			for (RecordedRequest recordedRequest : recordedRequests) {
				if (url.contains(recordedRequest.getUrl())) {
					return recordedRequest;
				}
			}
		}
		return null;
	}
	public void recordFormSubmission(String url, String method, String formParameterList, String headers, String trace, String enctype) throws JSONException, UnsupportedEncodingException {
	JSONArray formParameterJsonArray = new JSONArray(formParameterList);
		Map<String, String> headerMap = getHeadersAsMap(headers);
		String body = "";
		switch (enctype) {
			case "application/x-www-form-urlencoded":
				headerMap.put("content-type", enctype);
				body = getUrlEncodedFormBody(formParameterJsonArray);
				break;
			case "multipart/form-data":
				
				headerMap.put("content-type", "multipart/form-data; boundary=" + RequestInspectorJs.MULTIPART_FORM_BOUNDARY);
				body = getMultiPartFormBody(formParameterJsonArray);
				break;
			case "text/plain":
				headerMap.put("content-type", enctype);
				body = getPlainTextFormBody(formParameterJsonArray);
				break;
			default:
				Log.e(RequestInspectorJs.LOG_TAG, "Incorrect encoding received from JavaScript: " + enctype);
				break;
		}

		Log.i(RequestInspectorJs.LOG_TAG, "Recorded form submission from JavaScript");
		addRecordedRequest(new RecordedRequest(WebViewRequestType.FORM,method,url,body,trace,headerMap,enctype));
	}

	private void addRecordedRequest(RecordedRequest recordedRequest) {
		synchronized(recordedRequests) {
			recordedRequests.add(recordedRequest);
		}
	}
	

	private String getPlainTextFormBody(JSONArray formParameterJsonArray) throws JSONException {
		StringBuilder resultStringBuilder = new StringBuilder();
		for (int i = 0; i < formParameterJsonArray.length(); i++) {
			JSONObject formParameter = formParameterJsonArray.getJSONObject(i);
			String name = formParameter.getString("name");
			String value = formParameter.getString("value");
			if (i != 0) {
				resultStringBuilder.append("\n");
			}
			resultStringBuilder.append(name);
			resultStringBuilder.append("=");
			resultStringBuilder.append(value);
		}
		return resultStringBuilder.toString();
	}
	

	private String getMultiPartFormBody(JSONArray formParameterJsonArray) throws JSONException {
		StringBuilder resultStringBuilder = new StringBuilder();
		for (int i = 0; i < formParameterJsonArray.length(); i++) {
			JSONObject formParameter = formParameterJsonArray.getJSONObject(i);
			String name = formParameter.getString("name");
			String value = formParameter.getString("value");
			resultStringBuilder.append("--");
			resultStringBuilder.append(RequestInspectorJs.MULTIPART_FORM_BOUNDARY);
			resultStringBuilder.append("\n");
			resultStringBuilder.append("Content-Disposition: form-data; name=\"");
			resultStringBuilder.append(name);
			resultStringBuilder.append("\"\n\n");
			resultStringBuilder.append(value);
			resultStringBuilder.append("\n");
		}
		resultStringBuilder.append("--");
		resultStringBuilder.append(RequestInspectorJs.MULTIPART_FORM_BOUNDARY);
		resultStringBuilder.append("--");
		return resultStringBuilder.toString();
	}
	
	private String getUrlEncodedFormBody(JSONArray formParameterJsonArray) throws UnsupportedEncodingException, JSONException, UnsupportedEncodingException {
		StringBuilder resultStringBuilder = new StringBuilder();
		for (int i = 0; i < formParameterJsonArray.length(); i++) {
			JSONObject formParameter = formParameterJsonArray.getJSONObject(i);
			String name = formParameter.getString("name");
			String value = formParameter.getString("value");
			String encodedValue = URLEncoder.encode(value, "UTF-8");
			if (i != 0) {
				resultStringBuilder.append("&");
			}
			resultStringBuilder.append(name);
			resultStringBuilder.append("=");
			resultStringBuilder.append(encodedValue);
		}
		return resultStringBuilder.toString();
	}
	

	private Map<String, String> getHeadersAsMap(String headersString) throws JSONException {
		JSONObject headersObject = new JSONObject(headersString);
		Map<String, String> map = new HashMap<String, String>();
		Iterator<String> iterator = headersObject.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String lowercaseHeader = key.toLowerCase(Locale.getDefault());
			map.put(lowercaseHeader, headersObject.getString(key));
		}
		return map;
	}
	
	
	@JavascriptInterface
	public void recordXhr(String url, String method, String body, String headers, String trace) throws JSONException {
		Log.i(RequestInspectorJs.LOG_TAG, "Recorded XHR from JavaScript");
		Map<String, String> headerMap = getHeadersAsMap(headers);
		addRecordedRequest(
			new RecordedRequest(WebViewRequestType.FORM,method,url,body,trace,headerMap,null)
		);
	}

	@JavascriptInterface
	public void recordFetch(String url, String method, String body, String headers, String trace) throws JSONException {
		Log.i(RequestInspectorJs.LOG_TAG, "Recorded fetch from JavaScript");
		Map<String, String> headerMap = getHeadersAsMap(headers);
		addRecordedRequest(
			new RecordedRequest(WebViewRequestType.FORM,method,url,body,trace,headerMap,null)
		);
	}
	public class RequestInspectorJs {
		public static final String LOG_TAG = "RequestInspectorJs";
		public static final String MULTIPART_FORM_BOUNDARY = "----WebKitFormBoundaryU7CgQs9WnqlZYKs6";
		public static final String INTERFACE_NAME = "RequestInspection";
		public static final String JAVASCRIPT_INTERCEPTION_CODE="function getFullUrl(url) {\n"+
		"if (url.startsWith(\"/\")) {\n"+
		"return location.protocol + '//' + location.host + url;\n"+
		"} else {\n"+
		"return url;\n"+
		"}\n"+
		"}\n"+
		"function recordFormSubmission(form) {\n"+
		"var jsonArr = [];\n"+
		"for (i = 0; i < form.elements.length; i++) {\n"+
		"var parName = form.elements[i].name;\n"+
		"var parValue = form.elements[i].value;\n"+
		"var parType = form.elements[i].type;\n"+
		"jsonArr.push({\n"+
		"name: parName,\n"+
		"value: parValue,\n"+
		"type: parType\n"+
		"});\n"+
		"}\n"+
		"const path = form.attributes['action'] === undefined ? \"/\" : form.attributes['action'].nodeValue;\n"+
		"const method = form.attributes['method'] === undefined ? \"GET\" : form.attributes['method'].nodeValue;\n"+
		"const url = getFullUrl(path);\n"+
		"const encType = form.attributes['enctype'] === undefined ? \"application/x-www-form-urlencoded\" : form.attributes['enctype'].nodeValue;\n"+
		"const err = new Error();\n"+
		"$INTERFACE_NAME.recordFormSubmission(\n"+
		"url,\n"+
		"method,\n"+
		"JSON.stringify(jsonArr),\n"+
		"\"{}\",\n"+
		"err.stack,\n"+
		"encType\n"+
		");\n"+
		"}\n"+
		"function handleFormSubmission(e) {\n"+
		"const form = e ? e.target : this;\n"+
		"recordFormSubmission(form);\n"+
		"form._submit();\n"+
		"}\n"+
		"HTMLFormElement.prototype._submit = HTMLFormElement.prototype.submit;\n"+
		"HTMLFormElement.prototype.submit = handleFormSubmission;\n"+
		"window.addEventListener('submit', function (submitEvent) {\n"+
		"handleFormSubmission(submitEvent);\n"+
		"}, true);\n"+
		"let lastXmlhttpRequestPrototypeMethod = null;\n"+
		"let xmlhttpRequestHeaders = {};\n"+
		"let xmlhttpRequestUrl = null;\n"+
		"XMLHttpRequest.prototype._open = XMLHttpRequest.prototype.open;\n"+
		"XMLHttpRequest.prototype.open = function (method, url, async, user, password) {\n"+
		"lastXmlhttpRequestPrototypeMethod = method;\n"+
		"xmlhttpRequestUrl = url;\n"+
		"const asyncWithDefault = async === undefined ? true : async;\n"+
		"this._open(method, url, asyncWithDefault, user, password);\n"+
		"};\n"+
		"XMLHttpRequest.prototype._setRequestHeader = XMLHttpRequest.prototype.setRequestHeader;\n"+
		"XMLHttpRequest.prototype.setRequestHeader = function (header, value) {\n"+
		"xmlhttpRequestHeaders[header] = value;\n"+
		"this._setRequestHeader(header, value);\n"+
		"};\n"+
		"XMLHttpRequest.prototype._send = XMLHttpRequest.prototype.send;\n"+
		"XMLHttpRequest.prototype.send = function (body) {\n"+
		"const err = new Error();\n"+
		"const url = getFullUrl(xmlhttpRequestUrl);\n"+
		"$INTERFACE_NAME.recordXhr(\n"+
		"url,\n"+
		"lastXmlhttpRequestPrototypeMethod,\n"+
		"body || \"\",\n"+
		"JSON.stringify(xmlhttpRequestHeaders),\n"+
		"err.stack\n"+
		");\n"+
		"lastXmlhttpRequestPrototypeMethod = null;\n"+
		"xmlhttpRequestUrl = null;\n"+
		"xmlhttpRequestHeaders = {};\n"+
		"this._send(body);\n"+
		"};\n"+
		"window._fetch = window.fetch;\n"+
		"window.fetch = function () {\n"+
		"const url = arguments[1] && 'url' in arguments[1] ? arguments[1]['url'] : \"/\";\n"+
		"const fullUrl = getFullUrl(url);\n"+
		"const method = arguments[1] && 'method' in arguments[1] ? arguments[1]['method'] : \"GET\";\n"+
		"const body = arguments[1] && 'body' in arguments[1] ? arguments[1]['body'] : \"\";\n"+
		"const headers = JSON.stringify(arguments[1] && 'headers' in arguments[1] ? arguments[1]['headers'] : {});\n"+
		"let err = new Error();\n"+
		"$INTERFACE_NAME.recordFetch(fullUrl, method, body, headers, err.stack);\n"+
		"return window._fetch.apply(this, arguments);\n"+
		"}\n";
		
	}
	public static void enabledRequestInspection(WebView webView ) {
		String jsCode = RequestInspectorJs.JAVASCRIPT_INTERCEPTION_CODE + "\n";//extraJavaScriptToInject;
		webView.evaluateJavascript("javascript: " + jsCode, null);
	}
	

    // Methods to inspect network requests using JavaScript
}
