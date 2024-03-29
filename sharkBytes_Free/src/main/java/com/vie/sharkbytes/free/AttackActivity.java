package com.vie.sharkbytes.free;

import java.util.ArrayList;

import org.json.JSONArray;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class AttackActivity extends Activity {
	LinearLayout attackLayout;
	AdView adView;
	ProgressDialog spinner;
	ArrayList<GetInfoTask> jsonTasks = new ArrayList<GetInfoTask>();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attack);
        this.setTitle(R.string.incidents_main_text);
        
        attackLayout = (LinearLayout) findViewById(R.id.attackLayout);

        // GoogleAnalytics, log screen and view
        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext()) == ConnectionResult.SUCCESS) {
            Tracker t = ((Global) getApplication()).getTracker(Global.TrackerName.APP_TRACKER);
            t.setScreenName("Recent Shark Incidents");
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
	protected void onStart() {
		super.onStart();
		
		//AdMob
		if (adView != null) {
			adView.resume();
	    }
		
		//Show Spinner
        spinner = new ProgressDialog(this);
		spinner.setMessage("Loading Incidents...");
		spinner.setCancelable(false);
		spinner.show();
		
		//Get json data from server
		attackLayout.removeAllViews();
        jsonTasks.add((GetInfoTask) new GetInfoTask(this).execute("getAttacks", ""));
	}
	
	@Override
	protected void onStop() {
		//AdMob
		if (adView != null) {
			adView.pause();
	    }
		
		//Cancel threads while reference is valid
		for(GetInfoTask t: jsonTasks) {t.cancel(true);}
		jsonTasks.clear();
		
		super.onStop();
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
		
		try {
			JSONArray json = new JSONArray(data);
			for(int i = 0; i < json.length(); i++) {
				//Add tile
				LayoutInflater inflater = LayoutInflater.from(this);
				View view = inflater.inflate(R.layout.attack_tile, null);
				
				TextView titleText = (TextView) view.findViewById(R.id.textTitle);
				titleText.setText(json.getJSONObject(i).getString("title"));
				TextView dateText = (TextView) view.findViewById(R.id.textDate);
				dateText.setText(json.getJSONObject(i).getString("date"));
				TextView bodyText = (TextView) view.findViewById(R.id.textBody);
				bodyText.setText(Html.fromHtml(Html.fromHtml(json.getJSONObject(i).getString("body")).toString()).toString());
				
				attackLayout.addView(view);
				attackLayout.refreshDrawableState();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Problem retrieving data", Toast.LENGTH_LONG).show();
		}
		
		//Dismiss Spinner if done
		if(jsonTasks.isEmpty() && spinner != null && spinner.isShowing()) {
			spinner.dismiss();
		}
	}
}
