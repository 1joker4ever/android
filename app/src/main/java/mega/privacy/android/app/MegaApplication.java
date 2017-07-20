package mega.privacy.android.app;

//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.Logger.LogLevel;
//import com.google.android.gms.analytics.Tracker;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.AccountController;
import mega.privacy.android.app.lollipop.megachat.ChatItemPreferences;
import mega.privacy.android.app.lollipop.megachat.ChatSettings;
import mega.privacy.android.app.lollipop.megachat.RecentChatsFragmentLollipop;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaChatLollipopAdapter;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaListChatLollipopAdapter;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatListenerInterface;
import nz.mega.sdk.MegaChatPresenceConfig;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaUser;


public class MegaApplication extends Application implements MegaListenerInterface, MegaChatListenerInterface{
	final String TAG = "MegaApplication";
	static final String USER_AGENT = "MEGAAndroid/3.2.1_143";

	DatabaseHandler dbH;
	MegaApiAndroid megaApi;
	MegaApiAndroid megaApiFolder;
	String localIpAddress = "";
	BackgroundRequestListener requestListener;
	final static private String APP_KEY = "6tioyn8ka5l6hty";
	final static private String APP_SECRET = "hfzgdtrma231qdm";


	MegaChatApiAndroid megaChatApi = null;

//	static final String GA_PROPERTY_ID = "UA-59254318-1";
//	
//	/**
//	 * Enum used to identify the tracker that needs to be used for tracking.
//	 *
//	 * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
//	 * storing them all in Application object helps ensure that they are created only once per
//	 * application instance.
//	 */
//	public enum TrackerName {
//	  APP_TRACKER/*, // Tracker used only in this app.
//	  GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
//	  ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
//	  */
//	}
//
//	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
	
	class BackgroundRequestListener implements MegaRequestListenerInterface
	{

		@Override
		public void onRequestStart(MegaApiJava api, MegaRequest request) {
			log("BackgroundRequestListener:onRequestStart: " + request.getRequestString());
		}

		@Override
		public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
			log("BackgroundRequestListener:onRequestUpdate: " + request.getRequestString());
		}

		@Override
		public void onRequestFinish(MegaApiJava api, MegaRequest request,
				MegaError e) {
			log("BackgroundRequestListener:onRequestFinish: " + request.getRequestString() + "____" + e.getErrorCode() + "___" + request.getParamType());
			if (e.getErrorCode() == MegaError.API_ESID){
				if (request.getType() == MegaRequest.TYPE_LOGOUT){
					log("type_logout");
					AccountController.logout(getApplicationContext(), getMegaApi(), getMegaChatApi(), false);
				}
			}
			else if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
				if (e.getErrorCode() == MegaError.API_OK){
					if (megaApi != null){
						log("BackgroundRequestListener:onRequestFinish: enableTransferResumption ");
						log("BackgroundRequestListener:onRequestFinish: enableTransferResumption - Session: " + megaApi.dumpSession());
//						megaApi.enableTransferResumption();
					}
				}
			}
			else if(request.getType() == MegaRequest.TYPE_GET_ATTR_USER){
				if (e.getErrorCode() == MegaError.API_OK){

					if(request.getParamType()==MegaApiJava.USER_ATTR_FIRSTNAME||request.getParamType()==MegaApiJava.USER_ATTR_LASTNAME){
						log("BackgroundRequestListener:onRequestFinish: Name: "+request.getText());
						if (megaApi != null){
							if(request.getEmail()!=null){
								log("BackgroundRequestListener:onRequestFinish: Email: "+request.getEmail());
								MegaUser user = megaApi.getContact(request.getEmail());
								if (user != null) {
									log("BackgroundRequestListener:onRequestFinish: User handle: "+user.getHandle());
									log("Visibility: "+user.getVisibility()); //If user visibity == MegaUser.VISIBILITY_UNKNOW then, non contact
									if(user.getVisibility()!=MegaUser.VISIBILITY_VISIBLE){
										log("BackgroundRequestListener:onRequestFinish: Non-contact");
										if(request.getParamType()==MegaApiJava.USER_ATTR_FIRSTNAME){
											dbH.setNonContactEmail(request.getEmail(), user.getHandle()+"");
											dbH.setNonContactFirstName(request.getText(), user.getHandle()+"");
										}
										else if(request.getParamType()==MegaApiJava.USER_ATTR_LASTNAME){
											dbH.setNonContactLastName(request.getText(), user.getHandle()+"");
										}
									}
									else{
										log("BackgroundRequestListener:onRequestFinish: The user is or was CONTACT: "+user.getEmail());
									}
								}
								else{
									log("BackgroundRequestListener:onRequestFinish: User is NULL");
								}
							}
						}
					}
				}
			}
		}

		@Override
		public void onRequestTemporaryError(MegaApiJava api,
				MegaRequest request, MegaError e) {
			log("BackgroundRequestListener: onRequestTemporaryError: " + request.getRequestString());
		}
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		MegaApiAndroid.addLoggerObject(new AndroidLogger());
		MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);

		dbH = DatabaseHandler.getDbHandler(getApplicationContext());

		megaApi = getMegaApi();
		megaApiFolder = getMegaApiFolder();

		MegaChatApiAndroid.setLoggerObject(new AndroidChatLogger());
		MegaChatApiAndroid.setLogLevel(MegaChatApiAndroid.LOG_LEVEL_MAX);

		Util.setContext(getApplicationContext());
		boolean fileLoggerSDK = false;
		if (dbH != null) {
			MegaAttributes attrs = dbH.getAttributes();
			if (attrs != null) {
				if (attrs.getFileLoggerSDK() != null) {
					try {
						fileLoggerSDK = Boolean.parseBoolean(attrs.getFileLoggerSDK());
					} catch (Exception e) {
						fileLoggerSDK = false;
					}
				} else {
					fileLoggerSDK = false;
				}
			} else {
				fileLoggerSDK = false;
			}
		}

		if (Util.DEBUG){
			MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);
		}
		else {
			Util.setFileLoggerSDK(fileLoggerSDK);
			if (fileLoggerSDK) {
				MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);
			} else {
				MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_FATAL);
			}
		}

		boolean fileLoggerKarere = false;
		if (dbH != null) {
			MegaAttributes attrs = dbH.getAttributes();
			if (attrs != null) {
				if (attrs.getFileLoggerKarere() != null) {
					try {
						fileLoggerKarere = Boolean.parseBoolean(attrs.getFileLoggerKarere());
					} catch (Exception e) {
						fileLoggerKarere = false;
					}
				} else {
					fileLoggerKarere = false;
				}
			} else {
				fileLoggerKarere = false;
			}
		}

		if (Util.DEBUG){
			MegaChatApiAndroid.setLogLevel(MegaChatApiAndroid.LOG_LEVEL_MAX);
		}
		else {
			Util.setFileLoggerKarere(fileLoggerKarere);
			if (fileLoggerKarere) {
				MegaChatApiAndroid.setLogLevel(MegaChatApiAndroid.LOG_LEVEL_MAX);
			} else {
				MegaChatApiAndroid.setLogLevel(MegaChatApiAndroid.LOG_LEVEL_ERROR);
			}
		}
		
//		initializeGA();
		
//		new MegaTest(getMegaApi()).start();
	}	
	
//	private void initializeGA(){
//		// Set the log level to verbose.
//		GoogleAnalytics.getInstance(this).getLogger().setLogLevel(LogLevel.VERBOSE);
//	}
	
	public MegaApiAndroid getMegaApiFolder(){
		if (megaApiFolder == null){
			PackageManager m = getPackageManager();
			String s = getPackageName();
			PackageInfo p;
			String path = null;
			try
			{
				p = m.getPackageInfo(s, 0);
				path = p.applicationInfo.dataDir + "/";
			}
			catch (NameNotFoundException e)
			{
				e.printStackTrace();
			}
			
			Log.d(TAG, "Database path: " + path);
			megaApiFolder = new MegaApiAndroid(MegaApplication.APP_KEY, 
					MegaApplication.USER_AGENT, path);
			
			megaApiFolder.setDownloadMethod(MegaApiJava.TRANSFER_METHOD_AUTO_ALTERNATIVE);
			megaApiFolder.setUploadMethod(MegaApiJava.TRANSFER_METHOD_AUTO_ALTERNATIVE);
		}
		
		return megaApiFolder;
	}

	public MegaChatApiAndroid getMegaChatApi(){
		if (megaChatApi == null){
			if (megaApi == null){
				getMegaApi();
			}
			else{
				megaChatApi = new MegaChatApiAndroid(megaApi);
			}
		}

		return megaChatApi;
	}

	public void disableMegaChatApi(){
		megaChatApi = null;
	}
	
	public MegaApiAndroid getMegaApi()
	{
		if(megaApi == null)
		{
			log("MEGAAPI = null");
			PackageManager m = getPackageManager();
			String s = getPackageName();
			PackageInfo p;
			String path = null;
			try
			{
				p = m.getPackageInfo(s, 0);
				path = p.applicationInfo.dataDir + "/";
			}
			catch (NameNotFoundException e)
			{
				e.printStackTrace();
			}
			
			Log.d(TAG, "Database path: " + path);
			megaApi = new MegaApiAndroid(MegaApplication.APP_KEY, 
					MegaApplication.USER_AGENT, path);
			
			megaApi.setDownloadMethod(MegaApiJava.TRANSFER_METHOD_AUTO_ALTERNATIVE);
			megaApi.setUploadMethod(MegaApiJava.TRANSFER_METHOD_AUTO_ALTERNATIVE);
			
			requestListener = new BackgroundRequestListener();
			log("ADD REQUESTLISTENER");
			megaApi.addRequestListener(requestListener);
			megaApi.addListener(this);

//			DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
//			if (dbH.getCredentials() != null){
//				megaChatApi = new MegaChatApiAndroid(megaApi, true);
//			}
//			else{
//				megaChatApi = new MegaChatApiAndroid(megaApi, false);
//			}

			if(Util.isChatEnabled()){
				megaChatApi = new MegaChatApiAndroid(megaApi);
				megaChatApi.addChatListener(this);
			}

			String language = Locale.getDefault().toString();
			boolean languageString = megaApi.setLanguage(language);
			log("Result: "+languageString+" Language: "+language);
			if(languageString==false){
				language = Locale.getDefault().getLanguage();
				languageString = megaApi.setLanguage(language);
				log("2--Result: "+languageString+" Language: "+language);
			}

		}
		
		return megaApi;
	}

	public static boolean isActivityVisible() {
		log("isActivityVisible() => " + activityVisible);
		return activityVisible;
	}

	public static void setFirstConnect(boolean firstConnect){
		MegaApplication.firstConnect = firstConnect;
	}

	public static boolean isFirstConnect(){
		return firstConnect;
	}

	public static void activityResumed() {
		log("activityResumed()");
		activityVisible = true;
	}

	public static void activityPaused() {
		log("activityPaused()");
		activityVisible = false;
	}

	private static boolean activityVisible = false;
	private static boolean isLoggingIn = false;
	private static boolean firstConnect = true;
	private static boolean recentChatsFragmentVisible = false;
	public static boolean isFireBaseConnection = false;

	private static long openChatId = -1;

	public static boolean isLoggingIn() {
		return isLoggingIn;
	}

	public static void setLoggingIn(boolean loggingIn) {
		isLoggingIn = loggingIn;
	}

	public static void setRecentChatsFragmentVisible(boolean recentChatsFragmentVisible){
		MegaApplication.recentChatsFragmentVisible = recentChatsFragmentVisible;
	}

	public static void setOpenChatId(long openChatId){
		MegaApplication.openChatId = openChatId;
	}
	
	
//	synchronized Tracker getTracker(TrackerName trackerId) {
//		if (!mTrackers.containsKey(trackerId)) {
//
//			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//			Tracker t = null;
//			if (trackerId == TrackerName.APP_TRACKER){
//				t = analytics.newTracker(GA_PROPERTY_ID);
//			}
////			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
////					: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
////							: analytics.newTracker(R.xml.ecommerce_tracker);
//					mTrackers.put(trackerId, t);
//					
//		}
//	
//		return mTrackers.get(trackerId);
//	}


	public static long getOpenChatId() {
		return openChatId;
	}

	public String getLocalIpAddress(){
		return localIpAddress;
	}
	
	public void setLocalIpAddress(String ip){
		localIpAddress = ip;
	}
	
	public static void log(String message) {
		Util.log("MegaApplication", message);
	}



	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestFinish: " + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_LOGOUT){
			log("type_logout: " + e.getErrorCode() + "__" + request.getParamType());
			if (e.getErrorCode() == MegaError.API_ESID){
				log("calling ManagerActivity.logout");
				AccountController.logout(getApplicationContext(), getMegaApi(), getMegaChatApi(), false);
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		log("onUsersUpdate");
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> updatedNodes) {
		if (megaApi == null){
			megaApi = getMegaApi();
		}

		if (updatedNodes != null) {
			log("updatedNodes: " + updatedNodes.size());

			for (int i = 0; i < updatedNodes.size(); i++) {
				MegaNode n = updatedNodes.get(i);
				if (n.isInShare() && n.hasChanged(MegaNode.CHANGE_TYPE_INSHARE)){
					log("updatedNodes name: " + n.getName() + " isInshared: " + n.isInShare() + " getchanges: " + n.getChanges() + " haschanged(TYPE_INSHARE): " + n.hasChanged(MegaNode.CHANGE_TYPE_INSHARE));

					try {
						String email = "";
						if (megaApi.getMyUser() != null) {
							if (megaApi.getMyUser().getEmail() != null) {
								email = megaApi.getMyUser().getEmail();
							}
						}

						String notificationTitle = "Cloud activity (" + email + ")";
						String notificationContent = "A folder has been shared with you";
						int notificationId = Constants.NOTIFICATION_PUSH_CLOUD_DRIVE;

						Intent intent = new Intent(this, ManagerActivityLollipop.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION);
						PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
								PendingIntent.FLAG_ONE_SHOT);

						Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
								.setSmallIcon(R.drawable.ic_stat_notify_download)
								.setContentTitle(notificationTitle)
								.setContentText(notificationContent)
								.setAutoCancel(true)
								.setSound(defaultSoundUri)
								.setContentIntent(pendingIntent);

						NotificationManager notificationManager =
								(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

						notificationManager.notify(notificationId, notificationBuilder.build());
					}
					catch(Exception e){}
				}
			}
		}
	}

	@Override
	public void onReloadNeeded(MegaApiJava api) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onTransferFinish(MegaApiJava api, MegaTransfer transfer,
			MegaError e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTransferTemporaryError(MegaApiJava api,
			MegaTransfer transfer, MegaError e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onTransferData(MegaApiJava api, MegaTransfer transfer,
			byte[] buffer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onAccountUpdate(MegaApiJava api) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onContactRequestsUpdate(MegaApiJava api,
			ArrayList<MegaContactRequest> requests) {
		log("onContactRequestUpdate");

		try {
			if (requests == null) {
				return;
			}

			boolean showNotification = false;
			for (int i = 0; i < requests.size(); i++) {
				MegaContactRequest cr = requests.get(i);
				if (cr != null) {
					if ((cr.getStatus() == MegaContactRequest.STATUS_UNRESOLVED) && (!cr.isOutgoing())) {
						showNotification = true;
						log("onContactRequestUpdate: " + cr.getSourceEmail() + " cr.isOutgoing: " + cr.isOutgoing() + " cr.getStatus: " + cr.getStatus());
					}
				}
			}


			if (showNotification) {
				String email = "";
				if (megaApi != null) {
					if (megaApi.getMyUser() != null) {
						if (megaApi.getMyUser().getEmail() != null) {
							email = megaApi.getMyUser().getEmail();
						}
					}
				}

				String notificationTitle = "Contact activity (" + email + ")";
				String notificationContent = "You have a new contact request";
				int notificationId = Constants.NOTIFICATION_PUSH_CONTACT;

				Intent intent = new Intent(this, ManagerActivityLollipop.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.setAction(Constants.ACTION_IPC);
				PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
						PendingIntent.FLAG_ONE_SHOT);

				Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
						.setSmallIcon(R.drawable.ic_stat_notify_download)
						.setContentTitle(notificationTitle)
						.setContentText(notificationContent)
						.setAutoCancel(true)
						.setSound(defaultSoundUri)
						.setContentIntent(pendingIntent);

				NotificationManager notificationManager =
						(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

				notificationManager.notify(notificationId, notificationBuilder.build());
			}
		}
		catch(Exception e){}
	}

	@Override
	public void onChatListItemUpdate(MegaChatApiJava api, MegaChatListItem item) {
		log("onChatListItemUpdate");
		if (megaApi == null){
			megaApi = getMegaApi();
		}

		if (megaChatApi == null){
			megaChatApi = getMegaChatApi();
		}

		if (item.hasChanged(MegaChatListItem.CHANGE_TYPE_LAST_MSG) && (item.getUnreadCount() > 0)){
			try {
				if(isFireBaseConnection){
					log("Show notification ALWAYS");
					isFireBaseConnection=false;
					showNotification(item);
				}
				else{
					if (!recentChatsFragmentVisible) {
						if (openChatId != item.getChatId()) {
							if (isFirstConnect()) {
								log("onChatListItemUpdateMegaApplication. FIRSTCONNECT " + item.getTitle() + "Unread count: " + item.getUnreadCount() + " hasChanged(CHANGE_TYPE_UNREAD_COUNT): " + item.hasChanged(MegaChatListItem.CHANGE_TYPE_UNREAD_COUNT) + " hasChanged(CHANGE_TYPE_LAST_TS):" + item.hasChanged(MegaChatListItem.CHANGE_TYPE_LAST_TS));
							} else {
								log("onChatListItemUpdateMegaApplication. NOTFIRSTCONNECT " + item.getTitle() + "Unread count: " + item.getUnreadCount() + " hasChanged(CHANGE_TYPE_UNREAD_COUNT): " + item.hasChanged(MegaChatListItem.CHANGE_TYPE_UNREAD_COUNT) + " hasChanged(CHANGE_TYPE_LAST_TS):" + item.hasChanged(MegaChatListItem.CHANGE_TYPE_LAST_TS));
								showNotification(item);
							}

						}
					}
				}

			}
			catch (Exception e){}
		}
	}

	public void showNotification(MegaChatListItem item){
		log("showNotification: "+item.getTitle()+ "message: "+item.getLastMessage());

		ChatSettings chatSettings = dbH.getChatSettings();
		if(chatSettings!=null){
			if(chatSettings.getNotificationsEnabled().equals("true")){
				log("Notifications ON for all chats");

				ChatItemPreferences chatItemPreferences = dbH.findChatPreferencesByHandle(String.valueOf(item.getChatId()));

				if(chatItemPreferences==null){
					log("No preferences for this item");
					String soundString = chatSettings.getNotificationsSound();
					Uri uri = Uri.parse(soundString);
					log("Uri: "+uri);

					if(soundString.equals("true")||soundString.equals("")){

						Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						showCustomizedNotification(defaultSoundUri2, item, chatSettings.getVibrationEnabled());
					}
					else if(soundString.equals("-1")){
						log("Silent notification");
						showCustomizedNotification(null, item, chatSettings.getVibrationEnabled());
					}
					else{
						Ringtone sound = RingtoneManager.getRingtone(this, uri);
						if(sound==null){
							log("Sound is null");
							showCustomizedNotification(null, item, chatSettings.getVibrationEnabled());
						}
						else{
							showCustomizedNotification(uri, item, chatSettings.getVibrationEnabled());
						}
					}
				}
				else{
					log("Preferences FOUND for this item");
					if(chatItemPreferences.getNotificationsEnabled().equals("true")){
						log("Notifications ON for this chat");
						String soundString = chatItemPreferences.getNotificationsSound();
						Uri uri = Uri.parse(soundString);
						log("Uri: "+uri);

						if(soundString.equals("true")){

							Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
							showCustomizedNotification(defaultSoundUri2, item, chatSettings.getVibrationEnabled());
						}
						else if(soundString.equals("-1")){
							log("Silent notification");
							showCustomizedNotification(null, item, chatSettings.getVibrationEnabled());
						}
						else{
							Ringtone sound = RingtoneManager.getRingtone(this, uri);
							if(sound==null){
								log("Sound is null");
								showCustomizedNotification(null, item, chatSettings.getVibrationEnabled());
							}
							else{
								showCustomizedNotification(uri, item, chatSettings.getVibrationEnabled());
							}
						}
					}
					else{
						log("Notifications OFF for this chats");
					}
				}
			}
			else{
				log("Notifications OFF");
			}
		}
		else{
			log("Notifications DEFAULT ON");

			Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			showCustomizedNotification(defaultSoundUri2, item, "true");
		}
	}

	public void showCustomizedNotification(Uri uriParameter, MegaChatListItem item, String vibration){
		log("showCustomizedNotification");
		String notificationTitle = "Chat activity";
		String source = "<b>"+item.getTitle()+": </b>"+item.getLastMessage();

		Spanned notificationContent = Html.fromHtml(source,0);

		int notificationId = Constants.NOTIFICATION_PUSH_CHAT;

		Intent intent = new Intent(this, ManagerActivityLollipop.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
				PendingIntent.FLAG_ONE_SHOT);


		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_stat_notify_download)
				.setContentTitle(notificationTitle)
				.setContentText(notificationContent)
				.setColor(ContextCompat.getColor(getApplicationContext(),R.color.mega))
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

		Bitmap largeIcon = setUserAvatar(item);
		if(largeIcon!=null){
			log("There is avatar!");
			notificationBuilder.setLargeIcon(largeIcon);
		}

		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

//		StringBuilder[] events = {notificationContent, new StringBuilder("y trooo"), new StringBuilder("y moreee"), new StringBuilder("y yaaaa")};
// Sets a title for the Inbox in expanded layout
//		inboxStyle.setBigContentTitle("New messages:");

		String[] events = {"y trooo", "y moreee", "y yaaaa"};
// Moves events into the expanded layout
		inboxStyle.addLine(notificationContent);
		for (int i=0; i < events.length; i++) {
			inboxStyle.addLine(events[i]);

		}
// Moves the expanded layout object into the notification object.
		notificationBuilder.setStyle(inboxStyle);


		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder.setSound(uriParameter);
		if(vibration!=null){
			if(vibration.equals("true")){
				notificationBuilder.setVibrate(new long[] {0, 1000});
			}
		}

		notificationManager.notify(notificationId, notificationBuilder.build());
	}

	public Bitmap setUserAvatar(MegaChatListItem item){
		log("setUserAvatar");

		if(item.isGroup()){
			return createGroupChatAvatar(item);
		}
		else{
			if(megaChatApi==null){
				return createDefaultAvatar(item);
			}
			String contactMail = megaChatApi.getContactEmail(item.getPeerHandle());
			File avatar = null;
			if (getExternalCacheDir() != null){
				avatar = new File(getExternalCacheDir().getAbsolutePath(), contactMail + ".jpg");
			}
			else{
				avatar = new File(getCacheDir().getAbsolutePath(), contactMail + ".jpg");
			}
			Bitmap bitmap = null;
			if (avatar.exists()){
				if (avatar.length() > 0){
					BitmapFactory.Options bOpts = new BitmapFactory.Options();
					bOpts.inPurgeable = true;
					bOpts.inInputShareable = true;
					bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
					if (bitmap == null) {
						return createDefaultAvatar(item);
					}
					else{
						return getCircleBitmap(bitmap);
					}
				}
				else{
					return createDefaultAvatar(item);
				}
			}
			else{
				return createDefaultAvatar(item);
			}

		}
	}

	private Bitmap getCircleBitmap(Bitmap bitmap) {
		final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);

		final int color = Color.RED;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		bitmap.recycle();

		return output;
	}

	public Bitmap createDefaultAvatar(MegaChatListItem item){
		log("createDefaultAvatar()");

		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);

		String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(item.getPeerHandle()));
		if(color!=null){
			log("The color to set the avatar is "+color);
			p.setColor(Color.parseColor(color));
		}
		else{
			log("Default color to the avatar");
			p.setColor(getResources().getColor(R.color.lollipop_primary_color));
		}

		int radius;
		if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
			radius = defaultAvatar.getWidth()/2;
		else
			radius = defaultAvatar.getHeight()/2;

		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
		p.setColor(getResources().getColor(R.color.white));
		c.drawText("P", 10, 10, p);

		return defaultAvatar;
//		holder.imageView.setImageBitmap(defaultAvatar);
//
//		boolean setInitialByMail = false;
//
//		if (holder.fullName != null){
//			if (holder.fullName.trim().length() > 0){
//				String firstLetter = holder.fullName.charAt(0) + "";
//				firstLetter = firstLetter.toUpperCase(Locale.getDefault());
//				holder.contactInitialLetter.setText(firstLetter);
//				holder.contactInitialLetter.setTextColor(Color.WHITE);
//				holder.contactInitialLetter.setVisibility(View.VISIBLE);
//			}else{
//				setInitialByMail=true;
//			}
//		}
//		else{
//			setInitialByMail=true;
//		}
//		if(setInitialByMail){
//			if (holder.contactMail != null){
//				if (holder.contactMail.length() > 0){
//					log("email TEXT: " + holder.contactMail);
//					log("email TEXT AT 0: " + holder.contactMail.charAt(0));
//					String firstLetter = holder.contactMail.charAt(0) + "";
//					firstLetter = firstLetter.toUpperCase(Locale.getDefault());
//					holder.contactInitialLetter.setText(firstLetter);
//					holder.contactInitialLetter.setTextColor(Color.WHITE);
//					holder.contactInitialLetter.setVisibility(View.VISIBLE);
//				}
//			}
//		}
//		holder.contactInitialLetter.setTextSize(24);
	}

	public Bitmap createGroupChatAvatar(MegaChatListItem item){
		log("createGroupChatAvatar()");

		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(ContextCompat.getColor(this,R.color.divider_upgrade_account));

		int radius;
		if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
			radius = defaultAvatar.getWidth()/2;
		else
			radius = defaultAvatar.getHeight()/2;

		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);

		if(item.getTitle()!=null){
			if(!item.getTitle().isEmpty()){
				char title = item.getTitle().charAt(0);
				String firstLetter = new String(title+"");

				if(!firstLetter.equals("(")){

                    log("Draw letter: "+firstLetter);
					Paint text = new Paint();
					Typeface face = Typeface.SANS_SERIF;
					text.setTypeface(face);
					text.setAntiAlias(true);
					text.setSubpixelText(true);
					text.setStyle(Paint.Style.FILL);
					text.setColor(Color.WHITE);
					text.setTextSize(150);
					text.setTextAlign(Paint.Align.CENTER);

					text.setColor(ContextCompat.getColor(this,R.color.black));
                    Rect bounds = new Rect();
					text.getTextBounds(firstLetter, 0, firstLetter.length(), bounds);
                    int x = (defaultAvatar.getWidth() - bounds.width())/2;
                    int y = (defaultAvatar.getHeight() + bounds.height())/2;
                    c.drawText(firstLetter.toUpperCase(Locale.getDefault()), x, y, text);
				}

			}
		}
		return defaultAvatar;
//		String firstLetter = holder.contactInitialLetter.getText().toString();
//
//		if(firstLetter.trim().isEmpty()){
//			holder.contactInitialLetter.setVisibility(View.INVISIBLE);
//		}
//		else{
//			log("Group chat initial letter is: "+firstLetter);
//			if(firstLetter.equals("(")){
//				holder.contactInitialLetter.setVisibility(View.INVISIBLE);
//			}
//			else{
//				holder.contactInitialLetter.setText(firstLetter);
//				holder.contactInitialLetter.setTextColor(Color.WHITE);
//				holder.contactInitialLetter.setVisibility(View.VISIBLE);
//				holder.contactInitialLetter.setTextSize(24);
//			}
//		}
	}


	@Override
	public void onChatInitStateUpdate(MegaChatApiJava api, int newState) {
		log("onChatInitStateUpdate");
	}

	@Override
	public void onChatOnlineStatusUpdate(MegaChatApiJava api, long userhandle, int status, boolean inProgress) {
		log("onChatOnlineStatusUpdate");
	}

	@Override
	public void onChatPresenceConfigUpdate(MegaChatApiJava api, MegaChatPresenceConfig config) {
		log("onChatPresenceConfigUpdate");
	}

	public void sendSignalPresenceActivity(){
		log("sendSignalPresenceActivity");
		if(Util.isChatEnabled()){
			if (megaChatApi != null){
				if(megaChatApi.isSignalActivityRequired()){
					megaChatApi.signalPresenceActivity();
				}
			}
		}
	}
}
