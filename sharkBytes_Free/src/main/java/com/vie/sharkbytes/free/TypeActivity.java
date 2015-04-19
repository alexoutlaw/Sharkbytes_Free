package com.vie.sharkbytes.free;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class TypeActivity extends Activity {
	LinearLayout typeLayout;
	AdView adView;
	ProgressDialog spinner;
	ArrayList<GetInfoTask> jsonTasks = new ArrayList<GetInfoTask>();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);
        this.setTitle("Shark Types");
        
        typeLayout = (LinearLayout) findViewById(R.id.typeLayout);

        // GoogleAnalytics, log screen and view
        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext()) == ConnectionResult.SUCCESS) {
            Tracker t = ((Global) getApplication()).getTracker(Global.TrackerName.APP_TRACKER);
            t.setScreenName("Shark Types - Menu");
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        //AdMob
        adView = (AdView) findViewById(R.id.adView);
        //adView.setAdListener(new ToastAdListener(this));
        AdRequest adRequest = new AdRequest.Builder()
	    	.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	    	.build();
	    adView.loadAd(adRequest);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//AdMob
		if (adView != null) {
			adView.resume();
	    }
		

		//Show Spinner
        spinner = new ProgressDialog(this);
		spinner.setMessage("Loading Types...");
		spinner.show();
		
		//Get json data from server
		typeLayout.removeAllViews();
        jsonTasks.add((GetInfoTask) new GetInfoTask(this).execute("getTypes", ""));
	}
	
	@Override
	protected void onPause() {
		//AdMob
		if (adView != null) {
			adView.pause();
	    }
		
		super.onPause();
	}
	
	@Override
	public void onStop() {
		//Cancel threads while reference is valid
		for(GetInfoTask t: jsonTasks) {t.cancel(true);}
		jsonTasks.clear();
				
		super.onDestroy();
	}
	
	@Override
	public void onDestroy() {
		//AdMob
		if (adView != null) {
			adView.destroy();
		}
		
		super.onDestroy();
	}
	
	public void onTaskFinish(GetInfoTask task, String data) {
		jsonTasks.remove(task);
		
		//Log.d("test", "onTaskFinish data=" + data);
		
		try {
			//Format JSON
			ArrayList<String> formatted_data = new ArrayList<String>();
			JSONArray json = new JSONArray(data);
			for(int i = 0; i < json.length(); i++) {
				formatted_data.add(json.getJSONObject(i).getString("types"));
			}
			
			Collections.sort(formatted_data);
			
			//Add tile
			for(int i = 0; i < formatted_data.size(); i++) {
				final String currentType = formatted_data.get(i);
				
				LayoutInflater inflater = LayoutInflater.from(this);
				View view = inflater.inflate(R.layout.type_tile, null);
				
				TextView textType = (TextView) view.findViewById(R.id.textType);
				textType.setText(currentType);
				//OnClick, get details
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(TypeActivity.this, TypeDetailActivity.class);
						intent.putExtra("type", currentType);
						startActivity(intent);
					}
				});
				
				typeLayout.addView(view);
				typeLayout.refreshDrawableState();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Problem retrieving data", Toast.LENGTH_LONG).show();
		}
		
		//Dismiss Spinner
		if(jsonTasks.isEmpty() && spinner != null && spinner.isShowing()) {
			spinner.dismiss();
		}
	}
}