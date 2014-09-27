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

import twitter4j.Status;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Parcel;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.learnncode.twitter.Utilities.AppSettings;
import com.learnncode.twitter.Utilities.AppUtilities;

public class TweetViewHolder {

	public ImageView userProfileImageView;
	public TextView userNameTextView;
	public TextView twitterHandlerTextView;
	public TextView tweetTextView;
	public TextView timeTextView;
	public ImageView replyImageView;
	public TextView retweetTextView;
	public TextView favoriteTextView;
	public ImageView imageWithTweet;
	public LinearLayout imageContainer;
	private Context mContext;
	private AQuery aQuery;
	private String mImageLink;


	public void populateView(final Context context, final Status status) {
		mContext = context;
		if(status.getUser().getId() != AppSettings.getTwitterUserId(mContext)){

			retweetTextView.setEnabled(true);

		}else{
			retweetTextView.setEnabled(false);
		}


		retweetTextView.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.retweet_action_selector, 0);

		aQuery = new AQuery(mContext);
		aQuery.id(userProfileImageView).image(status.getUser().getBiggerProfileImageURL(), true, true, userProfileImageView.getWidth(), 0, null, 0);
		userNameTextView.setText(status.getUser().getName());
		twitterHandlerTextView.setText("@" + status.getUser().getScreenName());
		setMessage(mContext, tweetTextView, status.getText());
		favoriteTextView.setText(" " + status.getFavoriteCount());
		retweetTextView.setText(" " + status.getRetweetCount());


		timeTextView.setText(AppUtilities.getConvertDate(status.getCreatedAt().toString()));

		twitter4j.MediaEntity[] entity = status.getMediaEntities();
		int count = entity.length;
		if(count > 0 ){
			imageContainer.setVisibility(View.VISIBLE);
			imageWithTweet.setVisibility(View.VISIBLE);
			for(int i = 0; i < count; i++){
				mImageLink = entity[i].getMediaURL();
				aQuery.id(imageWithTweet).image(entity[i].getMediaURL(), true, true, userProfileImageView.getWidth(), 0, null, 0);
			}
		}else{
			imageContainer.setVisibility(View.GONE);
			imageWithTweet.setVisibility(View.GONE);
		}

		imageWithTweet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, UIImagePreview.class);
				intent.putExtra("url", mImageLink);
				context.startActivity(intent);

			}
		});
	}

	private void setMessage(final Context context, TextView textView, String message) {
		textView.setTextColor(Color.BLACK);


		if (message != null) {
			final String[] str_array = message.split(" ");
			SpannableStringBuilder ss = new SpannableStringBuilder(message);
			for (int i = 0; i < str_array.length; i++) {
				if (str_array[i].startsWith("@")) {
					ss.setSpan(new ClickableSpan() {

						@Override
						public void onClick(View widget) {

						}
					}, message.indexOf(str_array[i]), message.indexOf(str_array[i]) + (str_array[i].length()), 0);

					ss.setSpan(new ForegroundColorSpan(Color.BLUE), message.indexOf(str_array[i]), message.indexOf(str_array[i]) + (str_array[i].length()),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					NoUnderlineSpan noUnderline = new NoUnderlineSpan();
					ss.setSpan(noUnderline, message.indexOf(str_array[i]), message.indexOf(str_array[i]) + (str_array[i].length()), 0);
				}
				if (str_array[i].startsWith("#")) {
					ss.setSpan(new ForegroundColorSpan(Color.BLACK), message.indexOf(str_array[i]), message.indexOf(str_array[i])

							+ (str_array[i].length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					ss.setSpan(new StyleSpan(Typeface.BOLD), message.indexOf(str_array[i]), message.indexOf(str_array[i]) + (str_array[i].length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

				}
				if (str_array[i].contains("#")) {
					ss.setSpan(new ForegroundColorSpan(Color.BLACK), str_array[i].indexOf("#"), message.indexOf(str_array[i]) + (str_array[i].length()),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					ss.setSpan(new StyleSpan(Typeface.BOLD), message.indexOf(str_array[i]), message.indexOf(str_array[i]) + (str_array[i].length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

				}
				if (Patterns.WEB_URL.matcher(str_array[i]).matches()) {
					final int j;
					j = i;
					ss.setSpan(new ClickableSpan() {

						@Override
						public void onClick(View widget) {

							String url;
							if (!str_array[j].toLowerCase().contains("http") && !str_array[j].toLowerCase().contains("https")) {
								url =  "http://" + str_array[j];
							} else {
								url =  str_array[j];
							}

							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
							context.startActivity(browserIntent);
						}
					}, message.indexOf(str_array[i], i), message.indexOf(str_array[i], i) + (str_array[i].length()), 0);
				}
			}
			textView.setText(ss);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			textView.setVisibility(View.GONE);
		}
	}

	public static class NoUnderlineSpan extends UnderlineSpan {
		public NoUnderlineSpan() {
		}

		public NoUnderlineSpan(Parcel src) {
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false);
		}
	}

}
