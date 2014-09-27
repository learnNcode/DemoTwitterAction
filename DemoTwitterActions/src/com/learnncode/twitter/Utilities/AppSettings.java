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


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppSettings {

	public static void clearPrefs(Context context) {
		SharedPreferences prefs = Pref.get(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
	}


	public static boolean isTwitterAuthenticationDone(Context context) {
		SharedPreferences sharedPrefs = Pref.get(context);
		return sharedPrefs.getString(AppConstants.SHARED_PREF_KEY_TOKEN, null) != null;
	}

	public static void logOutTwitter(Context context){
		SharedPreferences sharedPrefs = Pref.get(context);
		Editor e = sharedPrefs.edit();
		e.putString(AppConstants.SHARED_PREF_KEY_TOKEN, null); 
		e.putString(AppConstants.SHARED_PREF_KEY_SECRET, null); 
		e.commit();
	}

	public static String getTwitterAccessToken(Context context){
		SharedPreferences sharedPrefs = Pref.get(context);
		return sharedPrefs.getString(AppConstants.SHARED_PREF_KEY_TOKEN, null);
	}

	public static String getTwitterAccessTokenSecret(Context context){
		SharedPreferences sharedPrefs = Pref.get(context);
		return sharedPrefs.getString(AppConstants.SHARED_PREF_KEY_SECRET, null);
	}

	public static long getTwitterUserId(Context context){
		SharedPreferences sharedPrefs = Pref.get(context);
		return sharedPrefs.getLong(AppConstants.SHARED_PREF_KEY_USER_ID, 0);
	}

	public static void setTwitterUserId(Context context, long userID){
		SharedPreferences sharedPrefs = Pref.get(context);
		Editor editor = sharedPrefs.edit();
		editor.putLong(AppConstants.SHARED_PREF_KEY_USER_ID, userID); 
		editor.commit();
	}


	public static void setTwitterAccessTokenAndSecret(Context context, String accessToken, String tokenSecret){
		SharedPreferences sharedPrefs = Pref.get(context);
		Editor editor = sharedPrefs.edit();
		editor.putString(AppConstants.SHARED_PREF_KEY_TOKEN, accessToken); 
		editor.putString(AppConstants.SHARED_PREF_KEY_SECRET, tokenSecret); 
		editor.commit();
	}



}
