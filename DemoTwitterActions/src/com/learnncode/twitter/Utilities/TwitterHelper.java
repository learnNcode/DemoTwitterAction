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



package com.learnncode.twitter.Utilities;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.learnncode.twitter.R;
import com.learnncode.twitter.TwitterLoginActivity;
import com.learnncode.twitter.async.TweetActionAsync;
import com.learnncode.twitter.interfaces.IGetTweetStatusObject;



public class TwitterHelper {

	public static Twitter getTwitterInstance(Context _Context) {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(_Context.getResources().getString(R.string.twitter_consumer_key));
		configurationBuilder.setOAuthConsumerSecret(_Context.getResources().getString(R.string.twitter_consumer_secret));
		configurationBuilder.setOAuthAccessToken(AppSettings.getTwitterAccessToken((_Context)));
		configurationBuilder.setOAuthAccessTokenSecret(AppSettings.getTwitterAccessTokenSecret(_Context));
		Configuration configuration = configurationBuilder.build();
		return new TwitterFactory(configuration).getInstance();
	}



	public static Twitter getTwitterInstanceWithoutAuthentication(Context _Context) {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(_Context.getResources().getString(R.string.twitter_consumer_key));
		configurationBuilder.setOAuthConsumerSecret(_Context.getResources().getString(R.string.twitter_consumer_secret));
		configurationBuilder.setOAuthAccessToken(_Context.getResources().getString(R.string.twitter_access_token));
		configurationBuilder.setOAuthAccessTokenSecret(_Context.getResources().getString(R.string.twitter_access_token_secret));
		Configuration configuration = configurationBuilder.build();
		return new TwitterFactory(configuration).getInstance();
	}



	public static void postOnTwitter(final Context context, final IGetTweetStatusObject getTweetStatusObject) {
		if(AppSettings.isTwitterAuthenticationDone(context)){
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View promptsView = inflater.inflate(R.layout.view_twitter_reply_alert_box, null);
			alertDialogBuilder.setView(promptsView);

			final TextView title = (TextView) promptsView.findViewById(R.id.socialTwitterReply_title);
			final EditText replyText = (EditText) promptsView.findViewById(R.id.socialTwitterReply_reply);
			final TextView tweetCharCount = (TextView) promptsView.findViewById(R.id.socialTwitterReply_char_count);

			title.setText(context.getResources().getString(R.string.retweet_this_to_your_followers_text));


			tweetCharCount.setVisibility(View.GONE);
			replyText.setEnabled(false);
			replyText.setText(replyText.getText());

			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton(context.getResources().getString(R.string.retweet_text),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {

					try {
						TweetActionAsync actionAsync = new TweetActionAsync(context, TweetActionAsync.ActionType.reply, getTweetStatusObject, 0, 0);
						actionAsync.execute(new Void[]{});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			})
			.setNegativeButton(context.getResources().getString(R.string.cancel_text),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

		}else{
			Intent twitterLoginIntent = new Intent(context, TwitterLoginActivity.class);
			context.startActivity(twitterLoginIntent);
		}
	}


	public static abstract class TwitterCallback{
		public abstract void onFinsihed(Boolean success);
	}
}
