package com.share.instatagapp.app;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthWebViewClient extends WebViewClient{
	InstagramDialog InstagramLoginDialog;
	private String CALLBACKURL="https://www.google.com";
	private String request_token;
	@Override
   public boolean shouldOverrideUrlLoading(WebView view, String url) 
{
if (url.startsWith(CALLBACKURL)) 
{ 
     System.out.println(url);
     String parts[] = url.split("=");
     request_token = parts[1];  //This is your request token.
     InstagramLoginDialog.dismiss();
return true;
       }
return false;
          }
}