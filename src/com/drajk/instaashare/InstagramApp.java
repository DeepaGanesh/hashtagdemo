package com.drajk.instaashare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.drajk.instaashare.InstagramDialog.OAuthDialogListener;

public class InstagramApp {
	
	private InstagramSession mSession;
	private InstagramDialog mDialog;
	private OAuthAuthenticationListener mListener;
	private ProgressDialog mProgress;
	private String mAuthUrl;
	private String mTokenUrl;
	private String mAccessToken;
	private Context mCtx;
	
	private String mClientId;
	private String mClientSecret;
	
//	private ImageView mTagImages;
	private Activity mTagImages;
	

	private static int WHAT_FINALIZE = 0;
	private static int WHAT_ERROR = 1;
	private static int WHAT_FETCH_INFO = 2;
	private static int WHAT_FETCH_TAG_IMAGE = 3;

	/**
	 * Callback url, as set in 'Manage OAuth Costumers' page
	 * (https://developer.github.com/)
	 */

	public static String mCallbackUrl = "https://www.google.com";
	private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
	private static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
	private static final String API_URL = "https://api.instagram.com/v1";

	private static final String TAG = "Instaashare";

	public InstagramApp(Context context, String clientId, String clientSecret,
			String callbackUrl,Activity tagImageView) {
		
		mClientId = clientId;
		mClientSecret = clientSecret;
		mCtx = context;
		mSession = new InstagramSession(context);
		mAccessToken = mSession.getAccessToken();
		mCallbackUrl = callbackUrl;
		mTokenUrl = TOKEN_URL + "?client_id=" + clientId + "&client_secret="
				+ clientSecret + "&redirect_uri=" + mCallbackUrl + "&grant_type=authorization_code";
		mAuthUrl = AUTH_URL + "?client_id=" + clientId + "&redirect_uri="
				+ mCallbackUrl + "&response_type=code&display=touch&scope=likes+comments+relationships";
		mTagImages=tagImageView;
		
		OAuthDialogListener listener = new OAuthDialogListener() {
			@Override
			public void onComplete(String code) {
				getAccessToken(code);
			}

			@Override
			public void onError(String error) {
				mListener.onFail("Authorization failed");
			}
		};

		mDialog = new InstagramDialog(context, mAuthUrl, listener);
		mProgress = new ProgressDialog(context);
		mProgress.setCancelable(false);
	}

	private void getAccessToken(final String code) {
		mProgress.setMessage("Getting access token ...");
		mProgress.show();

		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Getting access token");
				int what = WHAT_FETCH_INFO;
				try {
					URL url = new URL(TOKEN_URL);
					//URL url = new URL(mTokenUrl + "&code=" + code);
					Log.i(TAG, "Opening Token URL " + url.toString());
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);
					//urlConnection.connect();
					OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
					writer.write("client_id="+mClientId+
								"&client_secret="+mClientSecret+
								"&grant_type=authorization_code" +
								"&redirect_uri="+mCallbackUrl+
								"&code=" + code);
				    writer.flush();
					String response = streamToString(urlConnection.getInputStream());
					Log.i(TAG, "response " + response);
					JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
					
					mAccessToken = jsonObj.getString("access_token");
					Log.i(TAG, "Got access token: " + mAccessToken);
					
					String id = jsonObj.getJSONObject("user").getString("id");
					String user = jsonObj.getJSONObject("user").getString("username");
					String name = jsonObj.getJSONObject("user").getString("full_name");					
					
					mSession.storeAccessToken(mAccessToken, id, user, name);
					
				} catch (Exception ex) {
					what = WHAT_ERROR;
					ex.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
			}
		}.start();
	}
	
	private void fetchUserName() {
		mProgress.setMessage("Finalizing ...");
		
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Fetching user info");
				int what = WHAT_FETCH_TAG_IMAGE;
				try {
					URL url = new URL(API_URL + "/users/" + mSession.getId() + "/?access_token=" + mAccessToken);

					Log.d(TAG, "Opening URL fetching user infor " + url.toString());
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("GET");
					urlConnection.setDoInput(true);
					//urlConnection.setDoOutput(true);
					urlConnection.connect();
					System.out.println("getting urlconnections "+urlConnection.getResponseMessage());
					String response = streamToString(urlConnection.getInputStream());
					System.out.println("getting username "+response);
					JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
					String name = jsonObj.getJSONObject("data").getString("full_name");
					String bio = jsonObj.getJSONObject("data").getString("bio");
					Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
				} catch (Exception ex) {
					what = WHAT_ERROR;
					ex.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
			}
		}.start();	
		
	}
	private void fetchPicWithTag() {
		mProgress.setMessage("Fetching tag pics ...");
		
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "Fetching pic info matching tag");
				int what = WHAT_FINALIZE;
				try {
					URL url = new URL(API_URL + "/tags/selfie/media/recent?access_token=" + mAccessToken);
//https://api.instagram.com/v1/tags/selfie/media/recent?access_token=1543176444.fb3b686.0c77fd93775b4581ab4807a2f1806858
					Log.d(TAG, "Opening URL  for getting images " + url.toString());
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("GET");
					urlConnection.setDoInput(true);
					//urlConnection.setDoOutput(true);
					urlConnection.connect();
					System.out.println("getting urlconnections "+urlConnection.getResponseMessage());
					String response = streamToString(urlConnection.getInputStream());
					System.out.println("getting username "+response);
					JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
					JSONArray dataObj = jsonObj.getJSONArray("data");
					String[] tagImages = new String[dataObj.length()];
					for(int i=0;i<dataObj.length();i++){
					tagImages[i]=dataObj.getJSONObject(0).getJSONObject("images").getJSONObject("thumbnail").getString("url");
					}
					//String bio = jsonObj.getJSONObject("data").getString("bio");
					Log.i(TAG, "Got pic: " + tagImages[1]);
					URL newurl = new URL(tagImages[1]); 
					final Bitmap mIcon;
					
					//getImages=(ImageView)mCtx.getResources().getLayout(R.layout.activity_main);
					mIcon = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
					mTagImages.runOnUiThread(new Runnable(){
						
						public void run(){
							final ImageView getImages=(ImageView)mTagImages.findViewById(R.id.setImages);
							getImages.setImageBitmap(mIcon);
						}
					});
					
					
				
				} catch (Exception ex) {
					what = WHAT_ERROR;
					ex.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
			}
		}.start();	
		
	}



	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == WHAT_ERROR) {
				mProgress.dismiss();
				if(msg.arg1 == 1) {
					mListener.onFail("Failed to get access token");
				}
				else if(msg.arg1 == 2) {
					mListener.onFail("Failed to get user information");
				}
			} 
			else if(msg.what == WHAT_FETCH_INFO) {
				fetchUserName();
			}else if(msg.what == WHAT_FETCH_TAG_IMAGE) {
				fetchPicWithTag();
			}
			else {
				mProgress.dismiss();
				//mListener.onSuccess();
			}
		}
		
	};
	
	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}

	public void setListener(OAuthAuthenticationListener listener) {
		mListener = listener;
	}

	public String getUserName() {
		return mSession.getUsername();
	}

	public String getId() {
		return mSession.getId();
	}
	
	public String getName() {
		return mSession.getName();
	}
	
	public void authorize() {
		//Intent webAuthIntent = new Intent(Intent.ACTION_VIEW);
        //webAuthIntent.setData(Uri.parse(AUTH_URL));
        //mCtx.startActivity(webAuthIntent);
		mDialog.show();
	}

	private String streamToString(InputStream is) throws IOException {
		String str = "";

		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
			} finally {
				is.close();
			}

			str = sb.toString();
			System.out.println("strrringggg "+str);
		}

		return str;
	}

	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken();
			mAccessToken = null;
		}
	}

	public interface OAuthAuthenticationListener {
		public abstract void onSuccess();

		public abstract void onFail(String error);
	}
}
