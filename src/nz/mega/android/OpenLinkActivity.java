package nz.mega.android;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import nz.mega.android.utils.Util;
import nz.mega.sdk.MegaApiAndroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


public class OpenLinkActivity extends PinActivity {

	MegaApplication app;
	MegaApiAndroid megaApi;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		app = (MegaApplication) getApplication();
		megaApi = app.getMegaApi();
		
		Intent intent = getIntent();
		String url = intent.getDataString();
		
		try {
			url = URLDecoder.decode(url, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) {}
		url.replace(' ', '+');
		if(url.startsWith("mega://")){
			url = url.replace("mega://", "https://mega.co.nz/");
		}
		
		log("url " + url);
		
		// Download link
		if (url != null && (url.matches("^https://mega.co.nz/#!.*!.*$") || url.matches("^https://mega.nz/#!.*!.*$"))) {
			log("open link url");
			
//			Intent openIntent = new Intent(this, ManagerActivity.class);
			Intent openFileIntent = new Intent(this, FileLinkActivity.class);
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			openFileIntent.setAction(ManagerActivity.ACTION_OPEN_MEGA_LINK);
			openFileIntent.setData(Uri.parse(url));
			startActivity(openFileIntent);
			finish();

			return;
		}
		
		// Confirmation link
		if (url != null && (url.matches("^https://mega.co.nz/#confirm.+$") || url.matches("^https://mega.nz/#confirm.+$"))) {
			log("confirmation url");
			ManagerActivity.logout(this, app, megaApi, true);
			Intent confirmIntent = new Intent(this, LoginActivity.class);
			confirmIntent.putExtra(LoginActivity.EXTRA_CONFIRMATION, url);
			confirmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			confirmIntent.setAction(LoginActivity.ACTION_CONFIRM);
			startActivity(confirmIntent);
			finish();
			return;
		}
		
		// Folder Download link
		if (url != null && (url.matches("^https://mega.co.nz/#F!.+$") || url.matches("^https://mega.nz/#F!.+$"))) {
			log("folder link url");
			Intent openFolderIntent = new Intent(this, FolderLinkActivity.class);
			openFolderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			openFolderIntent.setAction(ManagerActivity.ACTION_OPEN_MEGA_FOLDER_LINK);
			openFolderIntent.setData(Uri.parse(url));
			startActivity(openFolderIntent);
			finish();
			return;
		}
		
		log("wrong url");
		Intent errorIntent = new Intent(this, ManagerActivity.class);
		errorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(errorIntent);
		finish();
	}
	
	public static void log(String message) {
		Util.log("OpenLinkActivity", message);
	}
}
