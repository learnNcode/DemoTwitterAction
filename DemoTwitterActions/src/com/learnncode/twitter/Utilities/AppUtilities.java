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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


@SuppressLint("SimpleDateFormat")
public class AppUtilities {

	public static String getConvertDate(String dateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

		String dateString = dateStr.replace("Z", "GMT+00:00");
		dateFormat.setTimeZone(TimeZone.getDefault());
		Date date = null;
		try {
			date = dateFormat.parse(dateString);
		} catch (java.text.ParseException e1) {
			e1.printStackTrace();
		}

		dateFormat = new SimpleDateFormat("hh:mm a - dd MMM yy");
		String outputText = dateFormat.format(date);

		return outputText;
	}

	  public static boolean IsNetworkAvailable(Context context) {

	        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
	        if (connectivityManager != null && netInfo != null) {

	            if (netInfo.isConnectedOrConnecting()) {
	                return true;
	            } else {
	                return false;
	            }
	        } else {
	            return false;
	        }
	    }

}
