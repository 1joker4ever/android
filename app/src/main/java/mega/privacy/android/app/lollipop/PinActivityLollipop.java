package mega.privacy.android.app.lollipop;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.PinUtil;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;


public class PinActivityLollipop extends AppCompatActivity{
	
	private MegaApiAndroid megaApi;
	private MegaChatApiAndroid megaChatApi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		if(Util.isChatEnabled()){
			if (megaChatApi == null){
				megaChatApi = ((MegaApplication)getApplication()).getMegaChatApi();
			}
		}
	}

	@Override
	protected void onPause() {
		log("onPause");
		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		if(Util.isChatEnabled()){
			if (megaChatApi == null){
				megaChatApi = ((MegaApplication)getApplication()).getMegaChatApi();
			}
		}

		PinUtil.pause(this);

		MegaApplication.activityPaused();

		super.onPause();
	}
	
	@Override
	protected void onResume() {
		log("onResume");

		super.onResume();
        Util.setAppFontSize(this);
		MegaApplication.activityResumed();

		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}
		
		log("retryPendingConnections()");
		megaApi.retryPendingConnections();


		if(Util.isChatEnabled()){
			if (megaChatApi == null){
				megaChatApi = ((MegaApplication)getApplication()).getMegaChatApi();
			}
		}

		if (megaChatApi != null){
			megaChatApi.retryPendingConnections(false, null);
		}

		if(MegaApplication.isShowPinScreen()){
			PinUtil.resume(this);
		}

		((MegaApplication) getApplication()).sendSignalPresenceActivity();
	}

	public static void log(String message) {
		Util.log("PinActivityLollipop", message);
	}
}
