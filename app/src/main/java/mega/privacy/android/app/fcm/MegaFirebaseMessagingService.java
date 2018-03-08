package mega.privacy.android.app.fcm;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.R;
import mega.privacy.android.app.UserCredentials;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatItemPreferences;
import mega.privacy.android.app.lollipop.megachat.ChatSettings;
import mega.privacy.android.app.lollipop.megachat.NotificationBuilder;
import mega.privacy.android.app.lollipop.megachat.calls.ChatCallActivity;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatCallListenerInterface;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatListenerInterface;
import nz.mega.sdk.MegaChatPresenceConfig;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaEvent;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaHandleList;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaUser;

public class MegaFirebaseMessagingService extends FirebaseMessagingService implements MegaGlobalListenerInterface, MegaRequestListenerInterface, MegaChatRequestListenerInterface, MegaChatListenerInterface, MegaChatCallListenerInterface {

    MegaApplication app;
    MegaApiAndroid megaApi;
    DatabaseHandler dbH;

    MegaChatApiAndroid megaChatApi;
    ChatSettings chatSettings;

    boolean isLoggingIn = false;

    String remoteMessageType = "";

    boolean shown = false;

    private NotificationBuilder notificationBuilder;

    CountDownTimer countDownTimer;

    Handler h;

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreateFCM");

        app = (MegaApplication) getApplication();
        megaApi = app.getMegaApi();
        megaChatApi = app.getMegaChatApi();
        megaChatApi.addChatListener(this);
        megaChatApi.addChatCallListener(this);
        megaApi.addGlobalListener(this);
        dbH = DatabaseHandler.getDbHandler(getApplicationContext());

        shown = false;
    }

    @Override
    public void onDestroy() {
        log("onDestroyFCM");
        super.onDestroy();
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        log("onMessageReceived");
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        log("From: " + remoteMessage.getFrom());

        notificationBuilder =  NotificationBuilder.newInstance(this, megaApi, megaChatApi);
        remoteMessageType = remoteMessage.getData().get("type");

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            log("Message data payload: " + remoteMessage.getData());
            UserCredentials credentials = dbH.getCredentials();
            if (credentials == null) {
                log("There are not user credentials");
                return;
            }
            else{

                if(remoteMessageType.equals("1")){
                    log("show SharedFolder Notification");
                    String gSession = credentials.getSession();
                    if (megaApi.getRootNode() == null) {
                        log("RootNode = null");
                        performLoginProccess(gSession);
                    }
                }
                else if(remoteMessageType.equals("3")){
                    log("show ContactRequest Notification");
                    String gSession = credentials.getSession();
                    if (megaApi.getRootNode() == null) {
                        log("RootNode = null");
                        performLoginProccess(gSession);
                    }
                }
                else if(remoteMessageType.equals("4")) {
                    String gSession = credentials.getSession();

                    if (megaApi.getRootNode() == null) {
                        log("RootNode = null");
                        performLoginProccess(gSession);
                    }
                    else{
                        log("RootNode is NOT null");
//                        String gSession = credentials.getSession();
                        int ret = megaChatApi.getInitState();
                        log("result of init ---> " + ret);
                        int status = megaChatApi.getOnlineStatus();
                        log("online status ---> "+status);
                        int connectionState = megaChatApi.getConnectionState();
                        log("connection state ---> "+connectionState);

                        MegaHandleList handleList = megaChatApi.getChatCalls();
                        if(handleList!=null){
                            if(handleList.size()==0){
                                log("NO calls in progress");
                            }
                            else if(handleList.size()==1){
                                long chatId = handleList.get(0);

                                MegaChatCall call = megaChatApi.getChatCall(chatId);
                                if(call!=null){
                                    launchCallActivity(call);
                                }
                            }
                            else{
                                log("MORE than one call in progress - not supported yet");
                            }
                        }
                    }

                }else if(remoteMessageType.equals("2")){
                    String gSession = credentials.getSession();
                    if (megaApi.getRootNode() == null){
                        log("RootNode = null");
                        performLoginProccess(gSession);

                        h = new Handler(Looper.getMainLooper());
                        h.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!shown){
                                            log("Show simple notification - no connection finished");
                                            shown=true;
                                            notificationBuilder.showSimpleNotification();
                                        }
                                        else{
                                            log("Notification already shown");
                                        }
                                    }
                                },
                                12000
                        );
                    }
                    else{
                        log("Other notification");
//                        if(Util.isChatEnabled()){
//                            if(!shown) {
//                                showChatNotification();
//                            }
//                        }
                    }
                }

            }
        }

//
//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            log("Message Notification Body: " + remoteMessage.getNotification().getBody());
//            sendNotification(remoteMessage.getNotification().getBody());
//        }
//
//        if (megaApi.getRootNode() != null) {
//            log("nullll!!!!!!!");
//        }
//        else{
//            log("Tengo root node!!!!!");
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    public void performLoginProccess(String gSession){
        isLoggingIn = MegaApplication.isLoggingIn();
        if (!isLoggingIn){
            isLoggingIn  = true;
            MegaApplication.setLoggingIn(isLoggingIn);

            if (Util.isChatEnabled()) {
                if (megaChatApi == null) {
                    megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
                }

                int ret = megaChatApi.getInitState();

                if(ret==0||ret==MegaChatApi.INIT_ERROR){
                    ret = megaChatApi.init(gSession);
                    log("result of init ---> " + ret);
                    chatSettings = dbH.getChatSettings();
                    if (ret == MegaChatApi.INIT_NO_CACHE) {
                        log("condition ret == MegaChatApi.INIT_NO_CACHE");
                    } else if (ret == MegaChatApi.INIT_ERROR) {
                        log("condition ret == MegaChatApi.INIT_ERROR");
                        if (chatSettings == null) {
                            log("ERROR----> Switch OFF chat");
                            chatSettings = new ChatSettings();
                            chatSettings.setEnabled(false+"");
                            dbH.setChatSettings(chatSettings);
                        } else {
                            log("ERROR----> Switch OFF chat");
                            dbH.setEnabledChat(false + "");
                        }
                        megaChatApi.logout(this);
                    } else {
                        log("Chat correctly initialized");
                    }
                }
            }

            megaApi.fastLogin(gSession, this);
        }
    }

    public static void log(String message) {
        Util.log("MegaFirebaseMessagingService", "FCM " + message);
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {
        log("onRequestStart: " + request.getRequestString());
    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
        log("onRequestUpdate: " + request.getRequestString());
    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestFinish: " + request.getRequestString());

        if (request.getType() == MegaRequest.TYPE_LOGIN){
            if (e.getErrorCode() == MegaError.API_OK) {
                log("Fast login OK");
                log("Calling fetchNodes from MegaFireBaseMessagingService");
                megaApi.fetchNodes(this);
            }
            else{
                log("ERROR: " + e.getErrorString());
                isLoggingIn = false;
                MegaApplication.setLoggingIn(isLoggingIn);
                return;
            }
        }
        else if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
            isLoggingIn = false;
            MegaApplication.setLoggingIn(isLoggingIn);
            if (e.getErrorCode() == MegaError.API_OK){
                log("OK fetch nodes");
                if (Util.isChatEnabled()) {
                    log("Chat enabled-->connect");
//                    MegaApplication.isFireBaseConnection=true;
                    megaChatApi.connectInBackground(this);
                }
                else{
                    log("Chat NOT enabled - sendNotification");
                }
            }
            else {
                log("ERROR: " + e.getErrorString());
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestTemporary: " + request.getRequestString());
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish: "+request.getRequestString()+ " result: "+e.getErrorString());

        if(request.getType()==MegaChatRequest.TYPE_CONNECT){
//            MegaApplication.isFireBaseConnection=false;
            log("TYPE CONNECT");
            //megaChatApi.setBackgroundStatus(true, this);
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("Connected to chat!");
            }
            else{
                log("EEEERRRRROR WHEN CONNECTING " + e.getErrorString());
            }
        }
        else if (request.getType() == MegaChatRequest.TYPE_SET_BACKGROUND_STATUS){
            log("TYPE SETBACKGROUNDSTATUS");
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }

    @Override
    public void onChatCallUpdate(MegaChatApiJava api, MegaChatCall call) {
        log("onChatCallUpdate: " + call.getChatid() + " " + call.getStatus());

        if(call.hasChanged(MegaChatCall.CHANGE_TYPE_STATUS)){
            launchCallActivity(call);
        }

        if(call.hasRemoteAudio()){
            log("Remote audio is connected");
        }
        else{
            log("Remote audio is NOT connected");
        }
        if(call.hasRemoteVideo()){
            log("Remote video is connected");
        }
        else{
            log("Remote video is NOT connected");
        }
    }

    public void launchCallActivity(MegaChatCall call){
        log("launchCallActivity");
        MegaApplication.setShowPinScreen(false);

        if(call.getStatus()==MegaChatCall.CALL_STATUS_RING_IN){

            Intent i = new Intent(this, ChatCallActivity.class);
            i.putExtra("chatHandle", call.getChatid());
            i.putExtra("callId", call.getId());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i);

            removeListeners();
        }
        else{
            log("Not in RINGING status");
        }
    }

    public void showSharedFolderNotification(MegaNode n) {
        log("showSharedFolderNotification");

        removeListeners();

        try {
            ArrayList<MegaShare> sharesIncoming = megaApi.getInSharesList();
            String name = "";
            for (int j = 0; j < sharesIncoming.size(); j++) {
                MegaShare mS = sharesIncoming.get(j);
                if (mS.getNodeHandle() == n.getHandle()) {
                    MegaUser user = megaApi.getContact(mS.getUser());
                    if (user != null) {
                        MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(user.getHandle()));

                        if (contactDB != null) {
                            if (!contactDB.getName().equals("")) {
                                name = contactDB.getName() + " " + contactDB.getLastName();

                            } else {
                                name = user.getEmail();

                            }
                        } else {
                            log("The contactDB is null: ");
                            name = user.getEmail();

                        }
                    } else {
                        name = user.getEmail();
                    }
                }
            }

            String source = "<b>" + n.getName() + "</b> " + getString(R.string.incoming_folder_notification) + " " + name;
            Spanned notificationContent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                notificationContent = Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
            } else {
                notificationContent = Html.fromHtml(source);
            }

            int notificationId = Constants.NOTIFICATION_PUSH_CLOUD_DRIVE;

            Intent intent = new Intent(this, ManagerActivityLollipop.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Constants.ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_notify_download)
                    .setContentTitle(getString(R.string.title_incoming_folder_notification))
                    .setContentText(notificationContent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notificationContent))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setColor(ContextCompat.getColor(this, R.color.mega))
                    .setContentIntent(pendingIntent);

            Drawable d;

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                d = getResources().getDrawable(R.drawable.ic_folder_incoming, getTheme());
            } else {
                d = getResources().getDrawable(R.drawable.ic_folder_incoming);
            }

            notificationBuilder.setLargeIcon(((BitmapDrawable) d).getBitmap());

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(notificationId, notificationBuilder.build());
        } catch (Exception e) {
            log("Exception: " + e.toString());
        }

//        try{
//            String source = "Tap to get more info";
//            Spanned notificationContent;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                notificationContent = Html.fromHtml(source,Html.FROM_HTML_MODE_LEGACY);
//            } else {
//                notificationContent = Html.fromHtml(source);
//            }
//
//            int notificationId = Constants.NOTIFICATION_PUSH_CLOUD_DRIVE;
//
//            Intent intent = new Intent(this, ManagerActivityLollipop.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.setAction(Constants.ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//
//            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.drawable.ic_stat_notify_download)
//                    .setContentTitle(getString(R.string.title_incoming_folder_notification))
//                    .setContentText(notificationContent)
//                    .setStyle(new NotificationCompat.BigTextStyle()
//                            .bigText(notificationContent))
//                    .setAutoCancel(true)
//                    .setSound(defaultSoundUri)
//                    .setColor(ContextCompat.getColor(this,R.color.mega))
//                    .setContentIntent(pendingIntent);
//
//            Drawable d;
//
//            if(android.os.Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP){
//                d = getResources().getDrawable(R.drawable.ic_folder_incoming, getTheme());
//            } else {
//                d = getResources().getDrawable(R.drawable.ic_folder_incoming);
//            }
//
//            notificationBuilder.setLargeIcon(((BitmapDrawable)d).getBitmap());
//
//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            notificationManager.notify(notificationId, notificationBuilder.build());
//        }
//        catch(Exception e){
//            log("Exception when showing shared folder notification: "+e.getMessage());
//        }
    }

    public void showContactRequestNotification(MegaContactRequest crToShow) {
        log("showContactRequestNotification");

        removeListeners();

        String notificationContent;
        if(crToShow!=null){
            notificationContent = crToShow.getSourceEmail();
        }
        else{
            log("Return because the request is NULL");
            return;
        }

        int notificationId = Constants.NOTIFICATION_PUSH_CONTACT;

        Intent intent = new Intent(this, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_IPC);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setContentTitle(getString(R.string.title_contact_request_notification))
                .setContentText(notificationContent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationContent))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(ContextCompat.getColor(this,R.color.mega))
                .setContentIntent(pendingIntent);

        if(crToShow!=null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                Bitmap largeIcon = createDefaultAvatar(crToShow.getSourceEmail());
                if(largeIcon!=null){
                    notificationBuilder.setLargeIcon(largeIcon);
                }
            }
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());

//        try{
//            int notificationId = Constants.NOTIFICATION_PUSH_CONTACT;
//
//            Intent intent = new Intent(this, ManagerActivityLollipop.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.setAction(Constants.ACTION_IPC);
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                    PendingIntent.FLAG_ONE_SHOT);
//
//            String notificationContent = "Tap to get more info";
//            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.drawable.ic_stat_notify_download)
//                    .setContentTitle(getString(R.string.title_contact_request_notification))
//                    .setContentText(notificationContent)
//                    .setStyle(new NotificationCompat.BigTextStyle()
//                            .bigText(notificationContent))
//                    .setAutoCancel(true)
//                    .setSound(defaultSoundUri)
//                    .setColor(ContextCompat.getColor(this,R.color.mega))
//                    .setContentIntent(pendingIntent);
//
//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
//
//            notificationManager.notify(notificationId, notificationBuilder.build());
//        }
//        catch(Exception e){
//            log("Exception when showing IPC request: "+e.getMessage());
//        }
    }

    public Bitmap createDefaultAvatar(String email){
        log("createDefaultAvatar()");

        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint paintText = new Paint();
        Paint paintCircle = new Paint();

        paintCircle.setColor(ContextCompat.getColor(this, R.color.lollipop_primary_color));
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(150);
        paintCircle.setAntiAlias(true);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);
        Typeface face = Typeface.SANS_SERIF;
        paintText.setTypeface(face);
        paintText.setAntiAlias(true);
        paintText.setSubpixelText(true);
        paintText.setStyle(Paint.Style.FILL);

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, paintCircle);

        if(email!=null){
            if(!email.isEmpty()){
                char title = email.charAt(0);
                String firstLetter = new String(title+"");

                if(!firstLetter.equals("(")){

                    log("Draw letter: "+firstLetter);
                    Rect bounds = new Rect();

                    paintText.getTextBounds(firstLetter,0,firstLetter.length(),bounds);
                    int xPos = (c.getWidth()/2);
                    int yPos = (int)((c.getHeight()/2)-((paintText.descent()+paintText.ascent()/2))+20);
                    c.drawText(firstLetter.toUpperCase(Locale.getDefault()), xPos, yPos, paintText);
                }

            }
        }
        return defaultAvatar;
    }

    public void showChatNotification(){
        log("showChatNotification");

        shown = true;
        removeListeners();

        ArrayList<MegaChatListItem> unreadChats = megaChatApi.getUnreadChatListItems();
        if (unreadChats != null) {
            log("Size of unread: " + unreadChats.size());

            for (int i = 0; i < unreadChats.size(); i++) {
                MegaChatListItem itemA = unreadChats.get(i);
                log("Item: " + itemA.getTitle() + " message: " + itemA.getLastMessage());
            }
            Collections.sort(unreadChats, new Comparator<MegaChatListItem>() {

                public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                    long timestamp1 = c1.getLastTimestamp();
                    long timestamp2 = c2.getLastTimestamp();

                    long result = timestamp2 - timestamp1;
                    return (int) result;
                }
            });

            if (unreadChats.size() > 0) {
                MegaChatListItem item = unreadChats.get(0);
                log("showChatNotification last item: " + item.getTitle() + " message: " + item.getLastMessage());

                ChatSettings chatSettings = dbH.getChatSettings();
                String email = megaChatApi.getContactEmail(item.getPeerHandle());

                if (chatSettings != null) {
                    if (chatSettings.getNotificationsEnabled().equals("true")) {
                        log("Notifications ON for all chats");

                        ChatItemPreferences chatItemPreferences = dbH.findChatPreferencesByHandle(String.valueOf(item.getChatId()));

                        if (chatItemPreferences == null) {
                            log("No preferences for this item");
                            String soundString = chatSettings.getNotificationsSound();
                            Uri uri = Uri.parse(soundString);
                            log("Uri: " + uri);

                            if (soundString.equals("true") || soundString.equals("")) {

                                Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                notificationBuilder.sendBundledNotification(defaultSoundUri2, unreadChats, chatSettings.getVibrationEnabled(), email);
                            } else if (soundString.equals("-1")) {
                                log("Silent notification");
                                notificationBuilder.sendBundledNotification(null, unreadChats, chatSettings.getVibrationEnabled(), email);
                            } else {
                                Ringtone sound = RingtoneManager.getRingtone(this, uri);
                                if (sound == null) {
                                    log("Sound is null");
                                    notificationBuilder.sendBundledNotification(null, unreadChats, chatSettings.getVibrationEnabled(), email);
                                } else {
                                    notificationBuilder.sendBundledNotification(uri, unreadChats, chatSettings.getVibrationEnabled(), email);
                                }
                            }
                        } else {
                            log("Preferences FOUND for this item");
                            if (chatItemPreferences.getNotificationsEnabled().equals("true")) {
                                log("Notifications ON for this chat");
                                String soundString = chatItemPreferences.getNotificationsSound();
                                Uri uri = Uri.parse(soundString);
                                log("Uri: " + uri);

                                if (soundString.equals("true")) {

                                    Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    notificationBuilder.sendBundledNotification(defaultSoundUri2, unreadChats, chatSettings.getVibrationEnabled(), email);
                                } else if (soundString.equals("-1")) {
                                    log("Silent notification");
                                    notificationBuilder.sendBundledNotification(null, unreadChats, chatSettings.getVibrationEnabled(), email);
                                } else {
                                    Ringtone sound = RingtoneManager.getRingtone(this, uri);
                                    if (sound == null) {
                                        log("Sound is null");
                                        notificationBuilder.sendBundledNotification(null, unreadChats, chatSettings.getVibrationEnabled(), email);
                                    } else {
                                        notificationBuilder.sendBundledNotification(uri, unreadChats, chatSettings.getVibrationEnabled(), email);

                                    }
                                }
                            } else {
                                log("Notifications OFF for this chats");
                            }
                        }
                    } else {
                        log("Notifications OFF");
                    }
                } else {
                    log("Notifications DEFAULT ON");

                    Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    notificationBuilder.sendBundledNotification(defaultSoundUri2, unreadChats, "true", email);
                }
            }
        }
    }

    @Override
    public void onChatListItemUpdate(MegaChatApiJava api, MegaChatListItem item) {

    }

    @Override
    public void onChatInitStateUpdate(MegaChatApiJava api, int newState) {

    }

    @Override
    public void onChatOnlineStatusUpdate(MegaChatApiJava api, long userhandle, int status, boolean inProgress) {

    }

    @Override
    public void onChatPresenceConfigUpdate(MegaChatApiJava api, MegaChatPresenceConfig config) {

    }

    @Override
    public void onChatConnectionStateUpdate(MegaChatApiJava api, long chatid, int newState) {
        log("onChatConnectionStateUpdate: "+chatid + " "+newState);
        if(newState==MegaChatApi.CHAT_CONNECTION_ONLINE && chatid==-1){
            log("Online Connection: "+chatid);
            if(remoteMessageType.equals("2")){
                if(!shown){
                    showChatNotification();
                }
            }
        }
    }

    @Override
    public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {

    }

    @Override
    public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> updatedNodes) {
        log("onNodesUpdated");

        if(remoteMessageType.equals("1")) {

            if (updatedNodes != null) {
                log("updatedNodes: " + updatedNodes.size());

                for (int i = 0; i < updatedNodes.size(); i++) {
                    MegaNode n = updatedNodes.get(i);
                    if (n.isInShare() && n.hasChanged(MegaNode.CHANGE_TYPE_INSHARE)) {
                        log("updatedNodes name: " + n.getName() + " isInshared: " + n.isInShare() + " getchanges: " + n.getChanges() + " haschanged(TYPE_INSHARE): " + n.hasChanged(MegaNode.CHANGE_TYPE_INSHARE));

                        showSharedFolderNotification(n);
                    }
                }
            }
            else{
                log("Updated nodes is NULL");
            }
        }
    }

    @Override
    public void onReloadNeeded(MegaApiJava api) {

    }

    @Override
    public void onAccountUpdate(MegaApiJava api) {

    }


    @Override
    public void onEvent(MegaApiJava api, MegaEvent event) {

    }

    @Override
    public void onContactRequestsUpdate(MegaApiJava api, ArrayList<MegaContactRequest> requests) {
        log("onContactRequestsUpdate");

        if(remoteMessageType.equals("3")) {
            try {
                if (requests == null) {
                    log("Return REQUESTS are NULL");
                    return;
                }

                for (int i = 0; i < requests.size(); i++) {
                    MegaContactRequest cr = requests.get(i);
                    if (cr != null) {
                        if ((cr.getStatus() == MegaContactRequest.STATUS_UNRESOLVED) && (!cr.isOutgoing())) {
                            showContactRequestNotification(cr);
                            log("onContactRequestUpdate: " + cr.getSourceEmail() + " cr.isOutgoing: " + cr.isOutgoing() + " cr.getStatus: " + cr.getStatus());
                        }
                    }
                }
            } catch (Exception e) {
                log("Exception when showing IPC request: " + e.getMessage());
            }
        }
    }

    public void removeListeners(){
        megaChatApi.removeChatCallListener(this);
        megaChatApi.removeChatListener(this);
        megaApi.removeGlobalListener(this);
    }
}