package com.share.instatagapp.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Handler;


public class InstaActivity extends ActionBarActivity implements View.OnClickListener {
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
    public static ImageView getSelfie;
    public static Activity activity;
    public static String[] data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insta);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        client_id = getResources().getString(R.string.client_id);
        client_secret = getResources().getString(R.string.client_secret);
        getSelfie = (ImageView) findViewById(R.id.setImages0);
        activity = this;
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
                CALLBACKURL, this);
        mApp.authorize();
    }

    @Override
    public void onClick(View view) {
//TODO perform onclick on the grid imageviews
    }
}
