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

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import android.content.Context;
import android.os.AsyncTask;

import com.learnncode.twitter.Utilities.TwitterHelper;
import com.learnncode.twitter.interfaces.IGetTweetStatusObject;

public class TweetActionAsync extends AsyncTask<Void, Void, Void>{

	public enum ActionType{
		favorite, retweet, reply, unfavorite, tweet
	}

	private ActionType mActionType;
	private Context mContext;
	private IGetTweetStatusObject mStatusObject;
	private long mTweetId;
	private int mPosition;
	private String mReplyText;
	private File imageFile;


	public TweetActionAsync(Context context, ActionType type, IGetTweetStatusObject statusObject, long tweetId, int position){

		mContext = context;
		this.mActionType = type;
		this.mStatusObject = statusObject;
		this.mTweetId = tweetId;
		this.mPosition = position;
	}

	public void setreply(String reply){
		mReplyText = reply;
	}

	public void setImageFileToUpload(File imageFile){
		if(imageFile.exists()){
			this.imageFile = imageFile;
		}
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		try {
			if(mActionType == ActionType.favorite){
				mStatusObject.setTweetStatusObject(TwitterHelper.getTwitterInstance(mContext).createFavorite(mTweetId), mPosition, mActionType);

			}else if(mActionType == ActionType.unfavorite){
				mStatusObject.setTweetStatusObject(TwitterHelper.getTwitterInstance(mContext).destroyFavorite(mTweetId), mPosition, mActionType);

			}else if(mActionType == ActionType.retweet){

				mStatusObject.setTweetStatusObject(TwitterHelper.getTwitterInstance(mContext).retweetStatus(mTweetId), mPosition, mActionType);


			}else if(mActionType == ActionType.reply){
				Twitter twitter = TwitterHelper.getTwitterInstance(mContext);
				StatusUpdate statusUpdate = new StatusUpdate(mReplyText);
				statusUpdate.inReplyToStatusId(mTweetId);
				mStatusObject.setTweetStatusObject(twitter.updateStatus(statusUpdate), mPosition, mActionType);

			}else if(mActionType == ActionType.tweet){
				Twitter twitter = TwitterHelper.getTwitterInstance(mContext);
				StatusUpdate statusUpdate = new StatusUpdate(mReplyText);
				if(imageFile != null){
					statusUpdate.setMedia(imageFile);
				}

				mStatusObject.setTweetStatusObject(twitter.updateStatus(statusUpdate), mPosition, mActionType);
			}

		} catch (Exception e) {
			mStatusObject.setTweetStatusObject(null, mPosition, mActionType);
		}
		return null;
	}


}
