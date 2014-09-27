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




package com.learnncode.twitter.async;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import android.content.Context;
import android.os.AsyncTask;

import com.learnncode.twitter.Utilities.AppSettings;
import com.learnncode.twitter.Utilities.AppUtilities;
import com.learnncode.twitter.Utilities.TwitterHelper;
import com.learnncode.twitter.interfaces.IHashtagTweetList;

public class HashtagTweetFetchAsyncTask extends AsyncTask<Object, Void, List<twitter4j.Status>>{

	Context mContext;
	String hashtag;
	IHashtagTweetList iHashTagList;
	long lastSeenId = 0;


	public HashtagTweetFetchAsyncTask(Context _context, String hashtag, Long id, IHashtagTweetList iHashTagList){
		mContext = _context;
		this.hashtag = hashtag;	
		this.iHashTagList = iHashTagList;
		lastSeenId = id;
	}

	@Override
	protected List<twitter4j.Status> doInBackground(Object... params) {

		List<twitter4j.Status> list = new ArrayList<twitter4j.Status>();
		if(AppUtilities.IsNetworkAvailable(mContext)){

			try {
				Twitter twitter;
				if(AppSettings.isTwitterAuthenticationDone(mContext)){
					twitter = TwitterHelper.getTwitterInstance(mContext);

				}else{
					twitter = TwitterHelper.getTwitterInstanceWithoutAuthentication(mContext);
				}
				Query query = new Query(hashtag);
				query.count(10);
				if(lastSeenId != 0){
					query.setMaxId(lastSeenId);
				}
				QueryResult qr = twitter.search(query);
				List<twitter4j.Status> qrTweets = qr.getTweets();


				for(twitter4j.Status t : qrTweets) {
					list.add(t);
				}

			} catch (Exception e) {
				System.err.println();
			}
		}
		if(lastSeenId != 0){
			iHashTagList.setTweetList(list, true);
		}else {
			iHashTagList.setTweetList(list, false);
		}

		return list;
	}

}
