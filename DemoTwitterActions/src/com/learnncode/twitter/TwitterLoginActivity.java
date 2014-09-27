/*
 * Copyright 2014 - learnNcode (learnncode@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.learnncode.twitter;

import java.util.HashMap;
import java.util.Map;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.learnncode.twitter.Utilities.AppConstants;
import com.learnncode.twitter.Utilities.AppSettings;
import com.learnncode.twitter.Utilities.TwitterHelper;

public class TwitterLoginActivity extends Activity {

	public static final int TWITTER_LOGIN_RESULT_CODE_SUCCESS = 1111;
	public static final int TWITTER_LOGIN_RESULT_CODE_FAILURE = 2222;


	private WebView twitterLoginWebView;
	private AlertDialog mAlertBuilder;
	private static String twitterConsumerKey;
	private static String twitterConsumerSecret;

	private static Twitter twitter;
	private static RequestToken requestToken;

	private Bundle twitterActionBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		twitterConsumerKey = getString(R.string.twitter_consumer_key);
		twitterConsumerSecret = getString(R.string.twitter_consumer_secret);

		if (twitterConsumerKey == null || twitterConsumerSecret == null) {
			TwitterLoginActivity.this.setResult(TWITTER_LOGIN_RESULT_CODE_FAILURE);
			TwitterLoginActivity.this.finish();
		}

		if (getIntent() != null) {

			twitterActionBundle = getIntent().getExtras();
		}
		
		 setContentView(R.layout.activity_twitter_login);

		mAlertBuilder = new AlertDialog.Builder(this).create();
		mAlertBuilder.setCancelable(true);
		mAlertBuilder.setTitle(R.string.please_wait_title);
		View view = getLayoutInflater().inflate(R.layout.view_loading, null);
		((TextView) view.findViewById(R.id.messageTextViewFromLoading)).setText(getString(R.string.authenticating_your_app_message));
		mAlertBuilder.setView(view);
		mAlertBuilder.show();


		mAlertBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				mAlertBuilder = null;

				finish();
			}
		});

		twitterLoginWebView = (WebView) findViewById(R.id.twitterLoginWebView);
		twitterLoginWebView.setBackgroundColor(Color.TRANSPARENT);
		twitterLoginWebView.clearCache(true);

		twitterLoginWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(AppConstants.TWITTER_CALLBACK_URL)) {
					Uri uri = Uri.parse(url);
					TwitterLoginActivity.this.saveAccessTokenAndFinish(uri);
					return true;
				}
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				if (mAlertBuilder != null) {
					mAlertBuilder.dismiss();
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);

				if (mAlertBuilder != null) {
					mAlertBuilder.show();
				}
			}
		});

		try {
			askOAuth();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (AppSettings.isTwitterAuthenticationDone(TwitterLoginActivity.this)) {
			Intent broadcastIntent = new Intent(AppConstants.TWITTER_AUTHENTICATION_DONE_BROADCAST);

			if (twitterActionBundle != null) {
				broadcastIntent.putExtras(twitterActionBundle);
			}
			sendBroadcast(broadcastIntent);
		}

		if (mAlertBuilder != null) {
			mAlertBuilder.dismiss();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void saveAccessTokenAndFinish(final Uri uri) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String verifier = uri.getQueryParameter(AppConstants.IEXTRA_OAUTH_VERIFIER);
				try {
					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
					AppSettings.setTwitterAccessTokenAndSecret(TwitterLoginActivity.this, accessToken.getToken(), accessToken.getTokenSecret());
					TwitterLoginActivity.this.setResult(TWITTER_LOGIN_RESULT_CODE_SUCCESS);
					AppSettings.setTwitterUserId(getApplicationContext(), accessToken.getUserId());
				} catch (Exception e) {
					TwitterLoginActivity.this.setResult(TWITTER_LOGIN_RESULT_CODE_FAILURE);
				}
				TwitterLoginActivity.this.finish();
			}
		}).start();
	}


	private void askOAuth() throws Exception {

		twitter = TwitterHelper.getTwitterInstance(TwitterLoginActivity.this);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					requestToken = twitter.getOAuthRequestToken(AppConstants.TWITTER_CALLBACK_URL);
				} catch (Exception e) {
					final String errorString = e.toString();
					TwitterLoginActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mAlertBuilder.cancel();
							Toast.makeText(TwitterLoginActivity.this, errorString.toString(), Toast.LENGTH_SHORT).show();
							finish();
						}
					});
					return;
				}

				TwitterLoginActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Map<String, String> noCacheHeaders = new HashMap<String, String>(2);
						noCacheHeaders.put("Pragma", "no-cache");
						noCacheHeaders.put("Cache-Control", "no-cache");
						twitterLoginWebView.loadUrl(requestToken.getAuthorizationURL(), noCacheHeaders);
					}
				});
			}
		}).start();
	}

}
