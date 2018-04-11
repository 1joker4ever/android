package mega.privacy.android.app.lollipop.megachat.calls;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;

public class CallNotificationIntentService extends IntentService implements MegaChatRequestListenerInterface {

    public static final String ANSWER = "ANSWER";
    public static final String IGNORE = "IGNORE";

    MegaChatApiAndroid megaChatApi;
    MegaApiAndroid megaApi;
    MegaApplication app;

    long chatHandleToAnswer;
    long chatHandleInProgress;

    public CallNotificationIntentService() {
        super("CallNotificationIntentService");
    }

    public void onCreate() {
        super.onCreate();

        app = (MegaApplication) getApplication();
        megaChatApi = app.getMegaChatApi();
        megaApi = app.getMegaApi();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        log("onHandleIntent");

        chatHandleToAnswer = intent.getExtras().getLong("chatHandleToAnswer", -1);
        chatHandleInProgress = intent.getExtras().getLong("chatHandleInProgress", -1);
        clearIncomingCallNotification(chatHandleToAnswer);

        final String action = intent.getAction();
        if (ANSWER.equals(action)) {
            log("Hang in progress call: "+chatHandleInProgress);
            megaChatApi.hangChatCall(chatHandleInProgress, this);
        } else if (IGNORE.equals(action)) {
            log("onHandleIntent:IGNORE");
            checkQueuedCalls();
            stopSelf();
        } else {
            throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

    public static void log(String log) {
        Util.log("CallNotificationIntentService", log);
    }

    public void clearIncomingCallNotification(long chatHandleToAnswer) {
        log("clearIncomingCallNotification:chatID: "+chatHandleToAnswer);

        try{
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            MegaChatCall call = megaChatApi.getChatCall(chatHandleToAnswer);
            if(call!=null){
                long chatCallId = call.getId();
                String notificationCallId = MegaApiJava.userHandleToBase64(chatCallId);
                int notificationId = (notificationCallId).hashCode();

                notificationManager.cancel(notificationId);
            }
            else{
                log("clearIncomingCallNotification:ERROR:NullCallObject");
            }
        }
        catch(Exception e){
            log("clearIncomingCallNotification:EXCEPTION");
        }
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {
    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

        if(request.getType() == MegaChatRequest.TYPE_HANG_CHAT_CALL){
            log("onRequestFinish:TYPE_HANG_CHAT_CALL");
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                megaChatApi.answerChatCall(chatHandleToAnswer, false, this);
            }
            else{
                log("onRequestFinish: ERROR:HANG_CALL: "+e.getErrorCode());
            }

        }
        else if(request.getType() == MegaChatRequest.TYPE_ANSWER_CHAT_CALL){
            log("onRequestFinish:TYPE_ANSWER_CHAT_CALL");
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                MegaApplication.setShowPinScreen(false);

                Intent i = new Intent(this, ChatCallActivity.class);
                i.putExtra("chatHandle", chatHandleToAnswer);
                i.setAction("SECOND_CALL");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(i);

                checkQueuedCalls();

                stopSelf();
            }
            else{
                log("onRequestFinish: ERROR:ANSWER_CALL: "+e.getErrorCode());
            }
        }
    }

    public void checkQueuedCalls(){
        log("checkQueuedCalls");

//        MegaHandleList handleList = megaChatApi.getChatCalls();
//        if(handleList!=null){
//            long numberOfCalls = handleList.size();
//            log("checkQueuedCalls: Number of calls in progress: "+numberOfCalls);
//            if (numberOfCalls>1){
//                log("checkQueuedCalls: MORE than one call in progress: "+numberOfCalls);
//                MegaChatCall callInProgress = null;
//                MegaChatCall callIncoming = null;
//                for(int j=0; j<handleList.size(); j++){
//                    MegaChatCall call = megaChatApi.getChatCall(handleList.get(j));
//                    if(call!=null){
//                        log("checkQueuedCalls: Call ChatID: "+call.getChatid()+" Status: "+call.getStatus());
//                        if(call.getStatus()>=MegaChatCall.CALL_STATUS_IN_PROGRESS){
//                            callInProgress = call;
//                            log("checkQueuedCalls: Call in progress: "+callInProgress.getChatid());
//                        }
//                        else{
//                            callIncoming = call;
//                            log("checkQueuedCalls: Call incoming: "+callIncoming.getChatid());
//                        }
//                    }
//
//                    if(callInProgress!=null){
//                        if(call.getStatus()<MegaChatCall.CALL_STATUS_IN_PROGRESS){
//                            AdvancedNotificationBuilder notificationBuilder = AdvancedNotificationBuilder.newInstance(this, megaApi, megaChatApi);
//                            notificationBuilder.showIncomingCallNotification(callIncoming, callInProgress);
//                        }
//                    }
//                }
//
//            }
//            else{
//                log("checkQueuedCalls: No calls to launch");
//            }
//        }

    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
    }

}