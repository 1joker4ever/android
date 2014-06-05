package com.mega.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.mega.android.CameraSyncService;
import com.mega.android.Util;

public class CameraEventReceiver extends BroadcastReceiver {
	
	public CameraEventReceiver() {}

	@Override
	public void onReceive(Context context, Intent intent){
		Cursor cursor = context.getContentResolver().query(intent.getData(), null,null, null, null);
	    cursor.moveToFirst();
	    String image_path = cursor.getString(cursor.getColumnIndex("_data"));
	    log("CameraEventReceiver_New Photo is Saved as : -" + image_path);
	    
	    context.startService(new Intent(context, CameraSyncService.class));
	}
	
	public static void log(String message) {
		Util.log("CameraEventReceiver", message);
	}
}