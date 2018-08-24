package mega.privacy.android.app.lollipop.listeners;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.ContactInfoActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaUser;

public class CreateChatToPerformActionListener implements MegaChatRequestListenerInterface {

    public static int SEND_FILE=1;
    public static int START_AUDIO_CALL=2;
    public static int START_VIDEO_CALL=3;

    Context context;
    int counter = 0;
    int error = 0;
    String message;
    ArrayList<MegaChatRoom> chats = null;
    ArrayList<MegaUser> usersNoChat = null;
    long fileHandle;
    int action = -1;

    MegaChatApiAndroid megaChatApi;

    public CreateChatToPerformActionListener(ArrayList<MegaChatRoom> chats, ArrayList<MegaUser> usersNoChat, long fileHandle, Context context, int action) {
        super();
        this.context = context;
        this.counter = usersNoChat.size();
        this.chats = chats;
        this.usersNoChat = usersNoChat;
        this.fileHandle = fileHandle;
        this.action = action;

        if (megaChatApi == null) {
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }
    }

    private static void log(String log) {
        Util.log("CreateChatToPerformActionListener", log);
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish: "+e.getErrorCode());

        if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM){
            if(action==SEND_FILE){
                log("Action: SEND_FILE");
                counter--;
                log("Counter after decrease: "+counter);
                if (e.getErrorCode() != MegaError.API_OK){
                    error++;
                    log("ERROR creating chat");
                }
                else{
                    if(chats==null){
                        chats = new ArrayList<MegaChatRoom>();
                    }
                    MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                    if(chat!=null){
                        chats.add(chat);
                    }
                }

                if(counter==0){
                    log("Counter is 0 - all requests processed");
                    if((usersNoChat.size() == error) && (chats==null || chats.isEmpty())){
                        //All send files fail
                        message = context.getResources().getString(R.string.number_no_sent, error);
                        if(context instanceof ManagerActivityLollipop){
                            ((ManagerActivityLollipop) context).showSnackbar(message);
                        }
                        else if(context instanceof ContactInfoActivityLollipop){
                            ((ContactInfoActivityLollipop) context).showSnackbar(message);
                        }
                    }
                    else {
                        if(context instanceof ManagerActivityLollipop){
                            ((ManagerActivityLollipop) context).sendFileToChatsFromContacts(chats, fileHandle);
                        }
                        else if(context instanceof ContactInfoActivityLollipop){
                            ((ContactInfoActivityLollipop) context).sendFileToChatsFromContacts(chats, fileHandle);
                        }
                    }
                }
            }
            else if(action==START_AUDIO_CALL){
                log("Action: START_AUDIO_CALL");
                if(context instanceof ContactInfoActivityLollipop){
                    if (e.getErrorCode() != MegaError.API_OK){
                        ((ContactInfoActivityLollipop) context).showSnackbar(context.getString(R.string.create_chat_error));
                    }
                    else{
                        MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                        if(chat!=null){
                            megaChatApi.startChatCall(chat.getChatId(), false, (ContactInfoActivityLollipop) context);
                        }
                        else{
                            log("Chatroom not recovered");
                        }
                    }
                }
            }
            else if(action==START_VIDEO_CALL){
                log("Action: START_VIDEO_CALL");
                if(context instanceof ContactInfoActivityLollipop){
                    if (e.getErrorCode() != MegaError.API_OK){
                        ((ContactInfoActivityLollipop) context).showSnackbar(context.getString(R.string.create_chat_error));
                    }
                    else{
                        MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                        if(chat!=null){
                            megaChatApi.startChatCall(chat.getChatId(), true, (ContactInfoActivityLollipop) context);
                        }
                        else{
                            log("Chatroom not recovered");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }
}
