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

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ProgressBar;

import com.androidquery.AQuery;

public class UIImagePreview extends FragmentActivity{

	private Context context;
	private AQuery aQuery;
	private PhotoView previewImageView;
	private ProgressBar progress;


	public UIImagePreview(){
		this.context = this;
		aQuery = new AQuery(context);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_image_preview);

		init();

		if(getIntent().getExtras() != null){
			String url = getIntent().getStringExtra("url");
			previewImageView.setImageUrl(url);
			aQuery.id(previewImageView).progress(progress.getId()).image(url);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	private void init() {  
		previewImageView  = (PhotoView) findViewById(R.id.previewImageView);
		progress          = (ProgressBar) findViewById(R.id.progressBar);
	}

}
