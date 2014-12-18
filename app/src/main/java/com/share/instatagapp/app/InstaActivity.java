package com.share.instatagapp.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class InstaActivity extends ActionBarActivity {
    private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
    // Used for Authentication.
    private static final String TOKENURL = "https://api.instagram.com/oauth/access_token";
    // Used for getting token and User details.
    public static final String APIURL = "https://api.instagram.com/v1";
    // Used to specify the API version which we are going to use.
//	public static String CALLBACKURL = "https://www.google.com";
    public static String CALLBACKURL = "https://www.google.com";
    // The callback url that we have used while registering the application.

    public String authURLString, tokenURLString, client_id, client_secret;
    public ImageView getSelfie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta);
        client_id = getResources().getString(R.string.client_id);
        client_secret = getResources().getString(R.string.client_secret);
        getSelfie=(ImageView)findViewById(R.id.setImages);
        authURLString = AUTHURL
                + "?client_id="
                + client_id
                + "&redirect_uri="
                + CALLBACKURL
                + "&response_type=code&display=touch&scope=likes+comments+relationships";
        tokenURLString = TOKENURL + "?client_id=" + client_id
                + "&client_secret=" + client_secret + "&redirect_uri="
                + CALLBACKURL + "&grant_type=authorization_code";
        InstagramApp mApp = new InstagramApp(this,
                client_id,
                client_secret,
                CALLBACKURL,this);
        mApp.authorize();

		/*WebView webView = new WebView(getApplicationContext());
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.setWebViewClient(new AuthWebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(authURLString);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.insta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
