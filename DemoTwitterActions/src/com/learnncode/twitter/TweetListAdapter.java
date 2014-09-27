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

import java.util.List;

import twitter4j.Status;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learnncode.twitter.Utilities.AppSettings;
import com.learnncode.twitter.async.TweetActionAsync;
import com.learnncode.twitter.async.TweetActionAsync.ActionType;
import com.learnncode.twitter.interfaces.IGetTweetStatusObject;

public class TweetListAdapter extends ArrayAdapter<Status> implements IGetTweetStatusObject{

	List<Status> list;
	LayoutInflater inflater;
	Context mContext;
	int count;
	private AlertDialog mAlertBuilder;



	public TweetListAdapter(Context context, int resource, List<Status> objects) {

		super(context, resource, objects);
		System.out.println("TweetAdapter.TweetAdapter()");
		mContext = context;
		list = objects;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public Status getItem(int position) {
		return list.get(position);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	public void addAll(List<Status> collection) {
		list.addAll(collection);
	}

	@Override
	public int getPosition(Status item) {
		return super.getPosition(item);
	}

	public void twitterAction(ActionType actionType, int position) {

		if(actionType == ActionType.reply){
			replyAction(position);
		}else if(actionType == ActionType.retweet){
			retweetAction(position);
		}else {
			favoriteAction(position);
		}
	}


	@Override
	public View getView( final int position, View convertView, ViewGroup parent) {
		TweetViewHolder holder;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.list_row_social_twitter, null);
			holder = new TweetViewHolder();
			holder.userNameTextView = (TextView) convertView.findViewById(R.id.socialTwitter_name);
			holder.userProfileImageView = (ImageView) convertView.findViewById(R.id.socialTwitter_profilePic);
			holder.twitterHandlerTextView = (TextView) convertView.findViewById(R.id.socialTwitter_nameHandle);
			holder.tweetTextView = (TextView) convertView.findViewById(R.id.socialTwitter_tweet);
			holder.timeTextView = (TextView) convertView.findViewById(R.id.socialTwitter_date);
			holder.replyImageView = (ImageView) convertView.findViewById(R.id.socialTwitter_action_reply);
			holder.retweetTextView = (TextView) convertView.findViewById(R.id.socialTwitter_action_retweet);
			holder.favoriteTextView = (TextView) convertView.findViewById(R.id.socialTwitter_action_favorite);
			holder.imageWithTweet = (ImageView) convertView.findViewById(R.id.socialTwitter_tweet_image);
			holder.imageContainer = (LinearLayout) convertView.findViewById(R.id.socialTwitter_tweet_image_container);
			convertView.setTag(holder);
		}else{
			holder = (TweetViewHolder) convertView.getTag();
		}

		holder.populateView(mContext,list.get(position));

		holder.favoriteTextView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				favoriteAction(position);
				return false;
			}
		});

		holder.retweetTextView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(list.get(position).isRetweetedByMe()){
					list.get(position).getRetweetCount();
					Toast.makeText(mContext, mContext.getString(R.string.cannot_retweet_status_text), Toast.LENGTH_SHORT).show();
				}else{

					retweetAction(position);
				}

				return false;
			}
		});


		holder.replyImageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				replyAction(position);
				return false;
			}
		});

		return convertView;
	}


	@Override
	public void setTweetStatusObject(Status result, int position, TweetActionAsync.ActionType type) {
		if(result != null && position < list.size()){
			if(type == ActionType.favorite){
				list.remove(position);
				list.add(position, result);

			}else if(type == ActionType.reply){
				list.add(0, result);

			} else if(type == ActionType.retweet){
				list.remove(position);
				list.add(position, result);

			}
			handler.sendEmptyMessage(1);

		}else{
			if(type == ActionType.retweet){
				Message message = new Message();
				message.obj = mContext.getString(R.string.cannot_retweet_status_text);
				message.what = 2;
				handler.sendMessage(message);
			}else{
				handler.sendEmptyMessage(3);
			}
		}
	}

	Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if(mAlertBuilder != null){
				mAlertBuilder.dismiss();
			}
			if(msg.what == 1){
				Toast.makeText(mContext, mContext.getString(R.string.success_text), Toast.LENGTH_SHORT).show();

			}else if(msg.what == 2){
				Toast.makeText(mContext, (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();

			}else{
				Toast.makeText(mContext, mContext.getString(R.string.fail_text), Toast.LENGTH_SHORT).show();
			}
			notifyDataSetChanged();
			return false;
		}
	});


	private void favoriteAction(final int position) {

		if(AppSettings.isTwitterAuthenticationDone(mContext)){
			try {
				showDialog(mContext);
				if(list.get(position).isFavorited()){
					TweetActionAsync actionAsync = new TweetActionAsync(mContext, TweetActionAsync.ActionType.unfavorite, TweetListAdapter.this, list.get(position).getId(), position);
					actionAsync.execute(new Void[]{});
				}else{
					TweetActionAsync actionAsync = new TweetActionAsync(mContext, TweetActionAsync.ActionType.favorite, TweetListAdapter.this, list.get(position).getId(), position);
					actionAsync.execute(new Void[]{});
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			Intent twitterLoginIntent = new Intent(mContext, TwitterLoginActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("ActionType", ActionType.favorite);
			bundle.putInt("position", position);
			twitterLoginIntent.putExtras(bundle);
			mContext.startActivity(twitterLoginIntent);
		}
	}


	private void retweetAction(final int position) {

		if(AppSettings.isTwitterAuthenticationDone(mContext)){
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

			View promptsView = inflater.inflate(R.layout.view_twitter_reply_alert_box, null);
			alertDialogBuilder.setView(promptsView);

			final TextView title = (TextView) promptsView.findViewById(R.id.socialTwitterReply_title);
			final EditText replyText = (EditText) promptsView.findViewById(R.id.socialTwitterReply_reply);
			final TextView tweetCharCount = (TextView) promptsView.findViewById(R.id.socialTwitterReply_char_count);

			title.setText(mContext.getResources().getString(R.string.retweet_this_to_your_followers_text));


			tweetCharCount.setVisibility(View.GONE);
			replyText.setEnabled(false);
			replyText.setText(list.get(position).getText());

			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton(mContext.getResources().getString(R.string.retweet_text),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {

					try {
						showDialog(mContext);
						TweetActionAsync actionAsync = new TweetActionAsync(mContext, TweetActionAsync.ActionType.retweet, TweetListAdapter.this, list.get(position).getId(), position);
						actionAsync.execute(new Void[]{});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			})
			.setNegativeButton(mContext.getResources().getString(R.string.cancel_text),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();

		}else{
			Intent twitterLoginIntent = new Intent(mContext, TwitterLoginActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("ActionType", ActionType.retweet);
			bundle.putInt("position", position);
			twitterLoginIntent.putExtras(bundle);
			mContext.startActivity(twitterLoginIntent);
		}



	}

	private void replyAction(final int position) {

		if(AppSettings.isTwitterAuthenticationDone(mContext)){
			System.out
			.println("TweetListAdapter.getView(...).new OnClickListener() {...}.onClick()");
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

			View promptsView = inflater.inflate(R.layout.view_twitter_reply_alert_box, null);
			alertDialogBuilder.setView(promptsView);

			final TextView title = (TextView) promptsView.findViewById(R.id.socialTwitterReply_title);
			final EditText replyText = (EditText) promptsView.findViewById(R.id.socialTwitterReply_reply);
			final TextView tweetCharCount = (TextView) promptsView.findViewById(R.id.socialTwitterReply_char_count);

			title.setText(mContext.getResources().getString(R.string.reply_text));


			count = (140 - (list.get(position).getUser().getScreenName().toString().length() + 1));
			tweetCharCount.setText("" + count);
			replyText.setText("@"+list.get(position).getUser().getScreenName());
			replyText.setSelection(replyText.getText().toString().length(), replyText.getText().toString().length());


			alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton(mContext.getResources().getString(R.string.reply_text),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {

					try {
						showDialog(mContext);
						TweetActionAsync actionAsync = new TweetActionAsync(mContext, TweetActionAsync.ActionType.reply, TweetListAdapter.this, list.get(position).getId(), position);
						actionAsync.setreply("@"+list.get(position).getUser().getScreenName() + replyText.getText());
						actionAsync.execute(new Void[]{});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			})
			.setNegativeButton(mContext.getResources().getString(R.string.cancel_text),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});


			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();


			replyText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					if(count > 0){
						tweetCharCount.setEnabled(true);
					}else{
						tweetCharCount.setEnabled(false);
					}
				}

				@Override
				public void afterTextChanged(Editable s) {

					if(s.length() < 140){
						count--;
						tweetCharCount.setText("" + (140 - s.length() ));
					}
				}
			});

		}else{
			Intent twitterLoginIntent = new Intent(mContext, TwitterLoginActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("ActionType", ActionType.reply);
			bundle.putInt("position", position);
			twitterLoginIntent.putExtras(bundle);
			mContext.startActivity(twitterLoginIntent);
		}

	}

	private void showDialog(Context context) {
		mAlertBuilder = new AlertDialog.Builder(context).create();
		mAlertBuilder.setCancelable(false);
		mAlertBuilder.setTitle(R.string.please_wait_title);
		View view = ((Activity) context).getLayoutInflater().inflate(R.layout.view_loading, null);
		((TextView) view.findViewById(R.id.messageTextViewFromLoading)).setText(context.getString(R.string.please_wait_title));
		mAlertBuilder.setView(view);
		mAlertBuilder.show();
	}

}
