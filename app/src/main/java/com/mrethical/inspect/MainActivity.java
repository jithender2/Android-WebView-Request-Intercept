package com.mrethical.inspect;

import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.mrethical.inspect.RequestInsect.RequestInspectorWebViewClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		WebView web=findViewById(R.id.webView);
		web.loadUrl("https://google.com");
		web.setWebViewClient(new RequestInspectorWebViewClient(web));
		
    }
}