package com.mrethical.inspector.RequestInspect;
import java.util.Map;
public final class RecordedRequest {
	WebViewRequestType type;
	String method;
	String url;
	String body;
	String trace;
	Map<String,String>headers;
	String encType;
	public RecordedRequest(WebViewRequestType type, String method, String url, String body, String trace, Map<String,String>headers, String encType) {
		this.type = type;
		this.method = method;
		this.url = url;
		this.body = body;
		this.trace = trace;
		this.headers = headers;
		this.encType = encType;
	}
	public void setType(WebViewRequestType type) {
		this.type = type;
	}

	public WebViewRequestType getType() {
		return type;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

	public String getTrace() {
		return trace;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setEncType(String encType) {
		this.encType = encType;
	}

	public String getEncType() {
		return encType;
	}}
