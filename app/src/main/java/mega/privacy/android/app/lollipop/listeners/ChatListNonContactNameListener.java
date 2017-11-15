package mega.privacy.android.app.lollipop.listeners;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;

import java.io.File;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaChatExplorerAdapter;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaChatLollipopAdapter;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaListChatLollipopAdapter;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaError;

public class ChatListNonContactNameListener implements MegaChatRequestListenerInterface {

    Context context;
    RecyclerView.ViewHolder holder;
    RecyclerView.Adapter adapter;
    boolean isUserHandle;
    DatabaseHandler dbH;
    String firstName;
    String lastName;
    String fullName;
    long userHandle;
    boolean receivedFirstName = false;
    boolean receivedLastName = false;
    MegaApiAndroid megaApi;

    public ChatListNonContactNameListener(Context context, RecyclerView.ViewHolder holder, RecyclerView.Adapter adapter, long userHandle) {
        this.context = context;
        this.isUserHandle = true;
        this.userHandle = userHandle;
        dbH = DatabaseHandler.getDbHandler(context);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if(adapter instanceof MegaListChatLollipopAdapter){
            this.holder = (MegaListChatLollipopAdapter.ViewHolderChatList) holder;
            this.adapter = (MegaListChatLollipopAdapter) adapter;
        }
        if(adapter instanceof MegaChatExplorerAdapter){
            this.holder = (MegaChatExplorerAdapter.ViewHolderChatList) holder;
            this.adapter = (MegaChatExplorerAdapter) adapter;
        }

    }

    public ChatListNonContactNameListener(Context context) {
        this.context = context;
        dbH = DatabaseHandler.getDbHandler(context);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }
    }

    private static void log(String log) {
        Util.log("ChatListNonContactNameListener", log);
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish()");

        if (e.getErrorCode() == MegaError.API_OK){
            if(request.getType()==MegaChatRequest.TYPE_GET_FIRSTNAME){
                log("ChatListNonContactNameListener->First name received");
                if(this.userHandle==request.getUserHandle()){
                    log("Match!");
                    firstName = request.getText();
                    receivedFirstName = true;
                    dbH.setNonContactFirstName(firstName, request.getUserHandle()+"");
                    log("Update holder ");
                    if(holder instanceof MegaListChatLollipopAdapter.ViewHolderChatList){
                        updateAdapter(((MegaListChatLollipopAdapter.ViewHolderChatList)holder).currentPosition);
                    }
                    else if(holder instanceof MegaChatExplorerAdapter.ViewHolderChatList){
                        updateAdapter(((MegaChatExplorerAdapter.ViewHolderChatList)holder).currentPosition);
                    }

                }
                else{
                    log("userHandles-> "+this.userHandle+"_"+request.getUserHandle());
                }
            }
            else if(request.getType()==MegaChatRequest.TYPE_GET_LASTNAME){
                log("ChatListNonContactNameListener->Update lastname: ");

                if(this.userHandle==request.getUserHandle()) {
                    log("MEgaChatAdapter->Match!");
                    lastName = request.getText();
                    receivedLastName = true;
                    dbH.setNonContactLastName(lastName, request.getUserHandle()+"");
                    log("Update holder: ");
                    if(holder instanceof MegaListChatLollipopAdapter.ViewHolderChatList){
                        updateAdapter(((MegaListChatLollipopAdapter.ViewHolderChatList)holder).currentPosition);
                    }
                    else if(holder instanceof MegaChatExplorerAdapter.ViewHolderChatList){
                        updateAdapter(((MegaChatExplorerAdapter.ViewHolderChatList)holder).currentPosition);
                    }
                }
                else{
                    log("userHAndles-> "+this.userHandle+"_"+request.getUserHandle());
                }
            }
        }
        else{
            log("Error asking for name!");
        }
    }

    public void updateAdapter(int position) {
        log("updateAdapter: "+position);
        if(receivedFirstName&&receivedLastName){

            if(adapter instanceof MegaListChatLollipopAdapter){
                ((MegaListChatLollipopAdapter)adapter).updateNonContactName(position, this.userHandle);
            }
            else if(adapter instanceof MegaChatExplorerAdapter){
                ((MegaChatExplorerAdapter)adapter).updateNonContactName(position, this.userHandle);
            }

            receivedFirstName = false;
            receivedLastName = false;
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }
}