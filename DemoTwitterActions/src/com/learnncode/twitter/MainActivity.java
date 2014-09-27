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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import twitter4j.Status;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.learnncode.twitter.Utilities.AppConstants;
import com.learnncode.twitter.async.HashtagTweetFetchAsyncTask;
import com.learnncode.twitter.async.TweetActionAsync.ActionType;
import com.learnncode.twitter.interfaces.IHashtagTweetList;

public class MainActivity extends Activity implements IHashtagTweetList{

	private ListView mTweetListView;
	private TweetListAdapter mAdapter;
	private EditText hashtagsEditText;
	private boolean loadingMore = false;
	private boolean userScrolled = false;
	private String hashtag;
	private AlertDialog mAlertBuilder; 
	private Button mRefreshListButton;
	private Context mContext;


	public MainActivity(){
		mContext = MainActivity.this;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTweetListView = (ListView) findViewById(android.R.id.list);
		mRefreshListButton = (Button) findViewById(R.id.twitterSocial_fetch_list_button);
		hashtagsEditText = (EditText) findViewById(R.id.hashtag_textview);

		mAlertBuilder = new AlertDialog.Builder(MainActivity.this).create();

		mAdapter = new TweetListAdapter(mContext, 0, new ArrayList<Status>());
		mTweetListView.setAdapter(mAdapter);


		mRefreshListButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(hashtagsEditText.getWindowToken(), 0);
				fetchTweets();
			}
		});


		IntentFilter tweeterActionIntentFilter = new IntentFilter(AppConstants.TWITTER_AUTHENTICATION_DONE_BROADCAST);
		registerReceiver(twitterAction, tweeterActionIntentFilter);

		fetchTweets();

		mTweetListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == 1){
					userScrolled = true;
				}  
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				int lastInScreen = firstVisibleItem + visibleItemCount;    
				if(userScrolled){
					if((lastInScreen == totalItemCount) && !(loadingMore)){   
						if(mAdapter != null && mAdapter.getCount() > 0){
							fetchTweetPagination(mAdapter.getItem(mAdapter.getCount() -1 ).getId());
							loadingMore = true;
						}
					}
				}
			}
		});
	}


	@Override
	public void onDestroy() {
		if(twitterAction != null){
			unregisterReceiver(twitterAction);
		}
		super.onDestroy();
	}


	public void fetchTweetPagination(long id) {
		loadingMore = true;
		showDialog();
		HashtagTweetFetchAsyncTask asyncTask = new HashtagTweetFetchAsyncTask(
				mContext, hashtag, id, MainActivity.this);
		asyncTask.execute(new Object[] {});
	}


	private void fetchTweets() {
		String tags = null;
		loadingMore = true;
		if(TextUtils.isEmpty(hashtagsEditText.getText())){
			hashtag = "#google";	
		}else{
			hashtag = hashtagsEditText.getText().toString();
			System.out.println("SocialTwitterFeedsFragment.onCreateView()|" + hashtag);
		}
		StringTokenizer st2 = new StringTokenizer(hashtag, ",");
		while (st2.hasMoreTokens()) {
			String value = st2.nextToken();
			if(tags == null){
				tags =  value ;
			}else{
				//				tags = tags + " AND " + value ; // for AND function
				tags = tags + " OR " + value ;
			}
		}

		showDialog();
		HashtagTweetFetchAsyncTask asyncTask = new HashtagTweetFetchAsyncTask(mContext, tags,(long) 0, MainActivity.this);
		asyncTask.execute(new Object[] {});
	}

	private void showDialog() {

		mAlertBuilder.setCancelable(false);
		mAlertBuilder.setTitle(R.string.please_wait_title);
		View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.view_loading, null);
		((TextView) view.findViewById(R.id.messageTextViewFromLoading)).setText(mContext.getString(R.string.please_wait_title));
		mAlertBuilder.setView(view);
		mAlertBuilder.show();
	}

	BroadcastReceiver twitterAction = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(mAdapter != null){
				if(intent.getExtras() != null){
					mAdapter.twitterAction((ActionType)intent.getExtras().get("ActionType"), (intent.getExtras().getInt("position")));
				}
			}
		}
	};


	Handler handler = new Handler(new Handler.Callback() {
		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message message) {
			if(message.what == 1){
				if(message.arg1 == 2){
					mAdapter.clear();
				}
				mAdapter.addAll((List<Status>)message.obj);

				mAdapter.notifyDataSetChanged();
				loadingMore = false;
				mAlertBuilder.dismiss();
			}
			return false;
		}
	});


	@Override
	public void setTweetList(List<Status> result, boolean isPagination) {
		Message message = new Message();
		message.obj = result;
		if(isPagination){
			message.arg1 = 1;

		}else{
			message.arg1 = 2;
		}
		message.what = 1;
		handler.sendMessage(message);
	}
}
