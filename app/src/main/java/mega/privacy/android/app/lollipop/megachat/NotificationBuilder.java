package mega.privacy.android.app.lollipop.megachat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;

import java.io.File;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatListNonContactNameListener;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaUser;

public final class NotificationBuilder {

    private static final String GROUP_KEY = "Karere";
    private static final int SUMMARY_ID = 0;

    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final SharedPreferences sharedPreferences;
    DatabaseHandler dbH;
    MegaApiAndroid megaApi;

    public static NotificationBuilder newInstance(Context context, MegaApiAndroid megaApi) {
        Context appContext = context.getApplicationContext();
        Context safeContext = ContextCompat.createDeviceProtectedStorageContext(appContext);
        if (safeContext == null) {
            safeContext = appContext;
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(safeContext);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(safeContext);
        return new NotificationBuilder(safeContext, notificationManager, sharedPreferences, megaApi);
    }

    public NotificationBuilder(Context context, NotificationManagerCompat notificationManager, SharedPreferences sharedPreferences, MegaApiAndroid megaApi) {
        this.context = context.getApplicationContext();
        this.notificationManager = notificationManager;
        this.sharedPreferences = sharedPreferences;
        dbH = DatabaseHandler.getDbHandler(context);
        this.megaApi = megaApi;
    }


    public void sendBundledNotification(Uri uriParameter, MegaChatListItem item, String vibration, String email) {
        Notification notification = buildNotification(uriParameter, item, vibration, GROUP_KEY, email);
        log("Notification id: "+getNotificationIdByHandle(item.getChatId()));
        notificationManager.notify(getNotificationIdByHandle(item.getChatId()), notification);
        Notification summary = buildSummary(GROUP_KEY);
        notificationManager.notify(SUMMARY_ID, summary);
    }

    public Notification buildNotification(Uri uriParameter, MegaChatListItem item, String vibration, String groupKey, String email) {
        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String title = "Chat activity";
        int unreadMessages = item.getUnreadCount();
        log("Unread messages: "+unreadMessages);
        if(unreadMessages!=0){

            if(unreadMessages<0){
                unreadMessages = Math.abs(unreadMessages);
                log("unread number: "+unreadMessages);

                if(unreadMessages>1){
                    String numberString = "+"+unreadMessages;
                    title = item.getTitle() + " (" + numberString + " " + context.getString(R.string.messages_chat_notification) + ")";
                }
            }
            else{

                if(unreadMessages>1){
                    String numberString = unreadMessages+"";
                    title = item.getTitle() + " (" + numberString + " " + context.getString(R.string.messages_chat_notification) + ")";
                }
            }
        }
        else{
            title = item.getTitle();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(context,R.color.mega))
                .setAutoCancel(true)
                .setShowWhen(true)
                .setGroup(groupKey)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if(item.isGroup()){

            long lastMsgSender = item.getLastMessageSender();
            String nameAction = getParticipantShortName(lastMsgSender);
//            ChatController cC = new ChatController(context);
//            String fullNameAction = cC.getFullName(item.getLastMessageSender(), item.getChatId());
            if(nameAction.isEmpty()){
                notificationBuilder.setContentText(item.getLastMessage());
            }
            else{
                String source = "<b>"+nameAction+": </b>"+item.getLastMessage();

                Spanned notificationContent = Html.fromHtml(source,0);
                notificationBuilder.setContentText(notificationContent);
            }
        }
        else{
            notificationBuilder.setContentText(item.getLastMessage());
        }

        //		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        //		StringBuilder[] events = {notificationContent, new StringBuilder("y trooo"), new StringBuilder("y moreee"), new StringBuilder("y yaaaa")};
        // Sets a title for the Inbox in expanded layout
        //		inboxStyle.setBigContentTitle("New messages:");

        //		String[] events = {"y trooo", "y moreee", "y yaaaa"};
        //// Moves events into the expanded layout
        //		inboxStyle.addLine(notificationContent);
        //		for (int i=0; i < events.length; i++) {
        //			inboxStyle.addLine(events[i]);
        //
        //		}
        // Moves the expanded layout object into the notification object.
        //		notificationBuilder.setStyle(inboxStyle);

        Bitmap largeIcon = setUserAvatar(item, email);
        if(largeIcon!=null){
            log("There is avatar!");
            notificationBuilder.setLargeIcon(largeIcon);
        }

        notificationBuilder.setSound(uriParameter);
        if(vibration!=null){
            if(vibration.equals("true")){
                notificationBuilder.setVibrate(new long[] {0, 1000});
            }
        }

        return notificationBuilder.build();
    }

    public String getParticipantShortName(long userHandle){
        log("getParticipantShortName");

        MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(userHandle));
        if (contactDB != null) {

            String participantFirstName = contactDB.getName();

            if(participantFirstName==null){
                participantFirstName="";
            }

            if (participantFirstName.trim().length() <= 0){
                String participantLastName = contactDB.getLastName();

                if(participantLastName == null){
                    participantLastName="";
                }

                if (participantLastName.trim().length() <= 0){
                    String stringHandle = MegaApiJava.handleToBase64(userHandle);
                    MegaUser megaContact = megaApi.getContact(stringHandle);
                    if(megaContact!=null){
                        return megaContact.getEmail();
                    }
                    else{
                        return "Unknown name";
                    }
                }
                else{
                    return participantLastName;
                }
            }
            else{
                return participantFirstName;
            }
        } else {
            log("Find non contact!");

            NonContactInfo nonContact = dbH.findNonContactByHandle(userHandle+"");

            if(nonContact!=null){
                String nonContactFirstName = nonContact.getFirstName();

                if(nonContactFirstName==null){
                    nonContactFirstName="";
                }

                if (nonContactFirstName.trim().length() <= 0){
                    String nonContactLastName = nonContact.getLastName();

                    if(nonContactLastName == null){
                        nonContactLastName="";
                    }

                    if (nonContactLastName.trim().length() <= 0){
                        log("Ask for email of a non contact");
                    }
                    else{
                        return nonContactLastName;
                    }
                }
                else{
                    return nonContactFirstName;
                }
            }
            else{
                log("Ask for non contact info");
            }

            return "";
        }
    }

    public Bitmap setUserAvatar(MegaChatListItem item, String contactMail){
        log("setUserAvatar");

        if(item.isGroup()){
            return createDefaultAvatar(item);
        }
        else{
            File avatar = null;
            if (context.getExternalCacheDir() != null){
                avatar = new File(context.getExternalCacheDir().getAbsolutePath(), contactMail + ".jpg");
            }
            else{
                avatar = new File(context.getCacheDir().getAbsolutePath(), contactMail + ".jpg");
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

        if(item.isGroup()){
            p.setColor(ContextCompat.getColor(context,R.color.divider_upgrade_account));
        }
        else{
            String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(item.getPeerHandle()));
            if(color!=null){
                log("The color to set the avatar is "+color);
                p.setColor(Color.parseColor(color));
            }
            else{
                log("Default color to the avatar");
                p.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
            }
        }

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

                    Rect r = new Rect();
                    c.getClipBounds(r);
                    int cHeight = r.height();
                    int cWidth = r.width();
                    text.setTextAlign(Paint.Align.LEFT);
                    text.getTextBounds(firstLetter, 0, firstLetter.length(), r);
                    float x = 0;
                    float y = 0;
                    if(firstLetter.toUpperCase(Locale.getDefault()).equals("A")){
                        x = cWidth / 2f - r.width() / 2f - r.left - 10;
                        y = cHeight / 2f + r.height() / 2f - r.bottom + 10;
                    }
                    else{
                        x = cWidth / 2f - r.width() / 2f - r.left;
                        y = cHeight / 2f + r.height() / 2f - r.bottom;
                    }

                    c.drawText(firstLetter.toUpperCase(Locale.getDefault()), x, y, text);
                }

            }
        }
        return defaultAvatar;
    }

    public Notification buildSummary(String groupKey) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_download)
                .setShowWhen(true)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setColor(ContextCompat.getColor(context,R.color.mega))
                .setAutoCancel(true)
                .build();
    }

//    public int getNotificationId() {
//        int id = sharedPreferences.getInt(NOTIFICATION_ID, SUMMARY_ID) + 1;
//        while (id == SUMMARY_ID) {
//            id++;
//        }
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(NOTIFICATION_ID, id);
//        editor.apply();
//        return id;
//    }

    public int getNotificationIdByHandle(long chatHandle) {
        String handleString = MegaApiJava.handleToBase64(chatHandle);

        int id = sharedPreferences.getInt(handleString, -1);
        if (id == -1) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(handleString, (int)chatHandle);
            editor.apply();
            return (int)chatHandle;
        }
        else{
            return id;
        }
    }

    public static void log(String message) {
        Util.log("NotificationBuilder", message);
    }

}