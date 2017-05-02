package mega.privacy.android.app.lollipop.megachat.chatAdapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.WrapTextView;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.lollipop.megachat.AndroidMegaChatMessage;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.TimeChatUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRoom;

public class MegaChatLollipopAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    public static final int ITEM_VIEW_TYPE_NORMAL = 0;
    public static final int ITEM_VIEW_TYPE_FILE= 1;
    public static final int ITEM_VIEW_TYPE_CONTACT= 2;

    public static int LEFT_MARGIN_CONTACT_MSG_MANAGEMENT = 40;
    public static int RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT = 68;

    public static int MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT = 73;

    public static int LEFT_MARGIN_CONTACT_MSG_NORMAL = 73;

    Context context;
    int positionClicked;
    ArrayList<AndroidMegaChatMessage> messages;
    RecyclerView listFragment;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    boolean multipleSelect;
    private SparseBooleanArray selectedItems;

    ChatController cC;

    long myUserHandle = -1;

    DisplayMetrics outMetrics;
    DatabaseHandler dbH = null;
    MegaChatRoom chatRoom;

    public MegaChatLollipopAdapter(Context _context, MegaChatRoom chatRoom, ArrayList<AndroidMegaChatMessage> _messages, RecyclerView _listView) {
        log("new adapter");
        this.context = _context;
        this.messages = _messages;
        this.positionClicked = -1;
        this.chatRoom = chatRoom;

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if (megaChatApi == null) {
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        listFragment = _listView;

        if(messages!=null)
        {
            log("Number of messages: "+messages.size());
        }
        else{
            log("Number of messages: NULL");
        }

        myUserHandle = megaChatApi.getMyUserHandle();
        log("MegaChatLollipopAdapter: MyUserHandle: "+myUserHandle);
    }

    public static class ViewHolderImageMessageChat extends RecyclerView.ViewHolder {
        public ViewHolderImageMessageChat(View view) {
            super(view);
        }

        int currentPosition;
        long userHandle;

        public long getUserHandle() {
            return userHandle;
        }

        public int getCurrentPosition() {
            return currentPosition;
        }
    }

    ViewHolderImageMessageChat holderImage;

    public static class ViewHolderTextMessageChat extends RecyclerView.ViewHolder {
        public ViewHolderTextMessageChat(View view) {
            super(view);
        }

        int currentPosition;
        long userHandle;
        String fullNameTitle;
        boolean nameRequested = false;
        boolean nameRequestedAction = false;

        RelativeLayout itemLayout;

        RelativeLayout dateLayout;
        TextView dateText;
        RelativeLayout ownMessageLayout;
        RelativeLayout titleOwnMessage;
//        TextView meText;
        TextView timeOwnText;
        RelativeLayout contentOwnMessageLayout;
        TextView contentOwnMessageText;

        TextView retryAlert;
        ImageView triangleIcon;

        RelativeLayout ownMultiselectionLayout;
        ImageView ownMultiselectionImageView;
        ImageView ownMultiselectionTickIcon;

        RelativeLayout contactMessageLayout;
        RelativeLayout titleContactMessage;
//        TextView contactText;
        TextView timeContactText;
        RelativeLayout contentContactMessageLayout;
        TextView contentContactMessageText;

        RelativeLayout ownManagementMessageLayout;
        TextView ownManagementMessageText;

        TextView contactManagementMessageText;
        RelativeLayout contactManagementMessageLayout;

        RelativeLayout contactMultiselectionLayout;
        ImageView contactMultiselectionImageView;
        ImageView contactMultiselectionTickIcon;

        RelativeLayout contactManagementMultiselectionLayout;
        ImageView contactManagementMultiselectionImageView;
        ImageView contactManagementMultiselectionTickIcon;

        RelativeLayout ownManagementMultiselectionLayout;
        ImageView ownManagementMultiselectionImageView;
        ImageView ownManagementMultiselectionTickIcon;

        public long getUserHandle (){
            return userHandle;
        }

        public int getCurrentPosition (){
            return currentPosition;
        }
    }
    ViewHolderTextMessageChat holder;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        log("onCreateViewHolder");

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        float density  = context.getResources().getDisplayMetrics().density;

        dbH = DatabaseHandler.getDbHandler(context);

        cC = new ChatController(context);

        View v = null;
        if(viewType == ITEM_VIEW_TYPE_NORMAL) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_message_chat, parent, false);
            holder = new ViewHolderTextMessageChat(v);
            holder.itemLayout = (RelativeLayout) v.findViewById(R.id.message_chat_item_layout);
            holder.dateLayout = (RelativeLayout) v.findViewById(R.id.message_chat_date_layout);
            //Margins
            RelativeLayout.LayoutParams dateLayoutParams = (RelativeLayout.LayoutParams)holder.dateLayout.getLayoutParams();
            dateLayoutParams.setMargins(0, Util.scaleHeightPx(8, outMetrics), 0, Util.scaleHeightPx(8, outMetrics));
            holder.dateLayout.setLayoutParams(dateLayoutParams);

            holder.dateText = (TextView) v.findViewById(R.id.message_chat_date_text);

            //Own messages
            holder.ownMessageLayout = (RelativeLayout) v.findViewById(R.id.message_chat_own_message_layout);
            holder.titleOwnMessage = (RelativeLayout) v.findViewById(R.id.title_own_message_layout);
//        holder.meText = (TextView) v.findViewById(R.id.message_chat_me_text);

            holder.timeOwnText = (TextView) v.findViewById(R.id.message_chat_time_text);
            //Margins
            RelativeLayout.LayoutParams timeOwnTextParams = (RelativeLayout.LayoutParams)holder.timeOwnText.getLayoutParams();
            timeOwnTextParams.setMargins(Util.scaleWidthPx(7, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
            holder.timeOwnText.setLayoutParams(timeOwnTextParams);

            holder.contentOwnMessageLayout = (RelativeLayout) v.findViewById(R.id.content_own_message_layout);
            //Margins
            RelativeLayout.LayoutParams ownLayoutParams = (RelativeLayout.LayoutParams)holder.contentOwnMessageLayout.getLayoutParams();
            ownLayoutParams.setMargins(0, 0, 0, Util.scaleHeightPx(4, outMetrics));
            holder.contentOwnMessageLayout.setLayoutParams(ownLayoutParams);

            holder.contentOwnMessageText = (WrapTextView) v.findViewById(R.id.content_own_message_text);
            //Margins
            RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)holder.contentOwnMessageText.getLayoutParams();
            ownMessageParams.setMargins(Util.scaleWidthPx(43, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
            holder.contentOwnMessageText.setLayoutParams(ownMessageParams);

            holder.retryAlert = (TextView) v.findViewById(R.id.not_sent_own_message_text);
            //Margins
            RelativeLayout.LayoutParams ownRetryAlertParams = (RelativeLayout.LayoutParams)holder.retryAlert.getLayoutParams();
            ownRetryAlertParams.setMargins(Util.scaleWidthPx(43, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
            holder.retryAlert.setLayoutParams(ownRetryAlertParams);

            holder.triangleIcon = (ImageView)  v.findViewById(R.id.own_triangle_icon);
            //Margins
            RelativeLayout.LayoutParams ownTriangleParams = (RelativeLayout.LayoutParams)holder.triangleIcon.getLayoutParams();
            ownTriangleParams.setMargins(Util.scaleWidthPx(43, outMetrics), 0, Util.scaleWidthPx(4, outMetrics), 0);
            holder.triangleIcon.setLayoutParams(ownTriangleParams);

            holder.ownManagementMessageLayout = (RelativeLayout) v.findViewById(R.id.own_management_message_layout);
            //Margins
            RelativeLayout.LayoutParams ownManagementParams = (RelativeLayout.LayoutParams)holder.ownManagementMessageLayout.getLayoutParams();
            ownManagementParams.addRule(RelativeLayout.ALIGN_RIGHT);
            ownManagementParams.setMargins(0, 0, 0, Util.scaleHeightPx(4, outMetrics));
            holder.ownManagementMessageLayout.setLayoutParams(ownManagementParams);

            holder.ownManagementMessageText = (TextView) v.findViewById(R.id.own_management_message_text);
            RelativeLayout.LayoutParams ownManagementTextParams = (RelativeLayout.LayoutParams)holder.ownManagementMessageText.getLayoutParams();
            ownManagementTextParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
            holder.ownManagementMessageText.setLayoutParams(ownManagementTextParams);

            holder.ownMultiselectionLayout = (RelativeLayout) v.findViewById(R.id.own_multiselection_layout);
            //Margins
            RelativeLayout.LayoutParams ownMultiselectionParams = (RelativeLayout.LayoutParams)holder.ownMultiselectionLayout.getLayoutParams();
            ownMultiselectionParams.setMargins(Util.scaleWidthPx(20, outMetrics), 0, 0, 0);
            holder.ownMultiselectionLayout.setLayoutParams(ownMultiselectionParams);

            holder.ownMultiselectionImageView = (ImageView) v.findViewById(R.id.own_multiselection_image_view);
            holder.ownMultiselectionTickIcon = (ImageView) v.findViewById(R.id.own_multiselection_tick_icon);

            holder.ownManagementMultiselectionLayout = (RelativeLayout) v.findViewById(R.id.own_management_multiselection_layout);
            //Margins
            RelativeLayout.LayoutParams ownManagementMultiselectionParams = (RelativeLayout.LayoutParams)holder.ownManagementMultiselectionLayout.getLayoutParams();
            ownManagementMultiselectionParams.setMargins(Util.scaleWidthPx(20, outMetrics), 0, 0, 0);
            holder.ownManagementMultiselectionLayout.setLayoutParams(ownManagementMultiselectionParams);

            holder.ownManagementMultiselectionImageView = (ImageView) v.findViewById(R.id.own_management_multiselection_image_view);
            holder.ownManagementMultiselectionTickIcon = (ImageView) v.findViewById(R.id.own_management_multiselection_tick_icon);

            //Contact messages////////////////////////////////////////
            holder.contactMessageLayout = (RelativeLayout) v.findViewById(R.id.message_chat_contact_message_layout);
            holder.titleContactMessage = (RelativeLayout) v.findViewById(R.id.title_contact_message_layout);
//        holder.contactText = (TextView) v.findViewById(R.id.message_chat_contact_text);

            holder.timeContactText = (TextView) v.findViewById(R.id.contact_message_chat_time_text);
            //Margins
            RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
            timeContactTextParams.setMargins(Util.scaleWidthPx(75, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
            holder.timeContactText.setLayoutParams(timeContactTextParams);

            holder.contentContactMessageLayout = (RelativeLayout) v.findViewById(R.id.content_contact_message_layout);
            //Margins
            RelativeLayout.LayoutParams contactLayoutParams = (RelativeLayout.LayoutParams)holder.contentContactMessageLayout.getLayoutParams();
            contactLayoutParams.setMargins(0, 0, 0, Util.scaleHeightPx(4, outMetrics));
            holder.contentContactMessageLayout.setLayoutParams(contactLayoutParams);

            holder.contentContactMessageText = (WrapTextView) v.findViewById(R.id.content_contact_message_text);
            //Margins
            RelativeLayout.LayoutParams contactMessageParams = (RelativeLayout.LayoutParams)holder.contentContactMessageText.getLayoutParams();
            contactMessageParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
            holder.contentContactMessageText.setLayoutParams(contactMessageParams);

            holder.contactManagementMessageLayout = (RelativeLayout) v.findViewById(R.id.contact_management_message_layout);
            //Margins
            RelativeLayout.LayoutParams contactManagementParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageLayout.getLayoutParams();
            contactManagementParams.addRule(RelativeLayout.ALIGN_LEFT);
            contactManagementParams.setMargins(0, 0, 0, Util.scaleHeightPx(4, outMetrics));
            holder.contactManagementMessageLayout.setLayoutParams(contactManagementParams);

            holder.contactManagementMessageText = (TextView) v.findViewById(R.id.contact_management_message_text);
            //Margins
            RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
            contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
            holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

            holder.contactMultiselectionLayout = (RelativeLayout) v.findViewById(R.id.contact_multiselection_layout);
            //Margins
            RelativeLayout.LayoutParams contactMultiselectionParams = (RelativeLayout.LayoutParams)holder.contactMultiselectionLayout.getLayoutParams();
            contactMultiselectionParams.setMargins(Util.scaleWidthPx(20, outMetrics), 0, 0, 0);
            holder.contactMultiselectionLayout.setLayoutParams(contactMultiselectionParams);

            holder.contactMultiselectionImageView = (ImageView) v.findViewById(R.id.contact_multiselection_image_view);
            holder.contactMultiselectionTickIcon = (ImageView) v.findViewById(R.id.contact_multiselection_tick_icon);

            holder.contactManagementMultiselectionLayout = (RelativeLayout) v.findViewById(R.id.contact_management_multiselection_layout);
            //Margins
            RelativeLayout.LayoutParams contactManagementMultiselectionParams = (RelativeLayout.LayoutParams)holder.contactManagementMultiselectionLayout.getLayoutParams();
            contactManagementMultiselectionParams.setMargins(Util.scaleWidthPx(20, outMetrics), 0, 0, 0);
            holder.contactManagementMultiselectionLayout.setLayoutParams(contactManagementMultiselectionParams);

            holder.contactManagementMultiselectionImageView = (ImageView) v.findViewById(R.id.contact_management_multiselection_image_view);
            holder.contactManagementMultiselectionTickIcon = (ImageView) v.findViewById(R.id.contact_management_multiselection_tick_icon);

            v.setTag(holder);

            return holder;

        }
        else{
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_message_chat, parent, false);
            holderImage = new ViewHolderImageMessageChat(v);

            v.setTag(holderImage);

            return holderImage;
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        log("onBindViewHolder: " + position);

        final int itemType = getItemViewType(position);
        log("itemType: "+itemType);

        if(itemType==ITEM_VIEW_TYPE_NORMAL) {
            onBindText(holder, position);
        }
        else{

        }
    }

    public void onBindImage(RecyclerView.ViewHolder holder, int position) {
        log("onBindImage: " + position);

    }

    public void onBindText(RecyclerView.ViewHolder holder, int position) {
        log("onBindText: "+position);

        ((ViewHolderTextMessageChat)holder).currentPosition = position;

        ((ViewHolderTextMessageChat)holder).triangleIcon.setVisibility(View.GONE);
        ((ViewHolderTextMessageChat)holder).retryAlert.setVisibility(View.GONE);

        MegaChatMessage message = messages.get(position).getMessage();
        ((ViewHolderTextMessageChat)holder).userHandle = message.getUserHandle();

//        String myMail = ((ChatActivityLollipop) context).getMyMail();

        if(message.getType()==MegaChatMessage.TYPE_ALTER_PARTICIPANTS){
            log("ALTER PARTICIPANT MESSAGE!!");

            if(message.getHandleOfAction()==myUserHandle){
                log("me alter participant");

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).dateText.setText(TimeChatUtils.formatDate(message.getTimestamp(), TimeChatUtils.DATE_SHORT_FORMAT));
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }

                ((ViewHolderTextMessageChat)holder).ownMessageLayout.setVisibility(View.VISIBLE);
                ((ViewHolderTextMessageChat)holder).contactMessageLayout.setVisibility(View.GONE);

                int privilege = message.getPrivilege();
                log("Privilege of me: "+privilege);
                String textToShow = "";
                String fullNameAction = cC.getFullName(message.getUserHandle(), chatRoom);

                if(fullNameAction==null){
                    fullNameAction = "";
                }

                if(fullNameAction.trim().length()<=0){

                    log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                    fullNameAction = "Unknown name";
                    if(!(((ViewHolderTextMessageChat)holder).nameRequestedAction)){
                        log("3-Call for nonContactName: "+ message.getUserHandle());
                        ((ViewHolderTextMessageChat)holder).nameRequestedAction=true;
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, message.getUserHandle());
                        megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                        megaChatApi.getUserLastname(message.getUserHandle(), listener);
                    }
                    else{
                        log("4-Name already asked and no name received: "+ message.getUserHandle());
                    }
                }

                if(privilege!=MegaChatRoom.PRIV_RM){
                    log("I was added");
                    textToShow = String.format(context.getString(R.string.message_add_participant), context.getString(R.string.chat_I_text), fullNameAction);
                }
                else{
                    log("I was removed or left");
                    if(message.getUserHandle()==message.getHandleOfAction()){
                        log("I left the chat");
                        textToShow = String.format(context.getString(R.string.message_participant_left_group_chat), context.getString(R.string.chat_I_text));

                    }
                    else{
                        textToShow = String.format(context.getString(R.string.message_remove_participant), context.getString(R.string.chat_I_text), fullNameAction);
                    }
                }

                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }

                ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setText(result);

                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.GONE);
                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);

                if (!multipleSelect) {
                    ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.GONE);
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.VISIBLE);
                    if (positionClicked != -1){
                        if (positionClicked == position){
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);
                    if(this.isItemChecked(position)){
                        log("Selected: "+message.getContent());
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                    }
                    else{
                        log("NOT selected");
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }
            }
            else{
                log("CONTACT Message type ALTER PARTICIPANTS");

                int privilege = message.getPrivilege();
                log("Privilege of the user: "+privilege);

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).dateText.setText(TimeChatUtils.formatDate(message.getTimestamp(), TimeChatUtils.DATE_SHORT_FORMAT));
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }
                ((ViewHolderTextMessageChat)holder).ownMessageLayout.setVisibility(View.GONE);
                ((ViewHolderTextMessageChat)holder).contactMessageLayout.setVisibility(View.VISIBLE);

                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.GONE);
                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);

                if (!multipleSelect) {
                    ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.GONE);

                    //Margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                    if (positionClicked != -1){
                        if (positionClicked == position){
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                    //Margins
                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                    //Set more margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    if (this.isItemChecked(position)) {
                        log("Selected: " + message.getContent());
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                    } else {
                        log("NOT selected");
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }

                ((ViewHolderTextMessageChat)holder).fullNameTitle = cC.getFullName(message.getHandleOfAction(), chatRoom);


                if(((ViewHolderTextMessageChat)holder).fullNameTitle==null){
                    ((ViewHolderTextMessageChat)holder).fullNameTitle = "";
                }

                if(((ViewHolderTextMessageChat)holder).fullNameTitle.trim().length()<=0){

                    log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                    ((ViewHolderTextMessageChat)holder).fullNameTitle = "Unknown name";
                    if(!(((ViewHolderTextMessageChat)holder).nameRequested)){
                        log("3-Call for nonContactName: "+ message.getUserHandle());

                        ((ViewHolderTextMessageChat)holder).nameRequested=true;
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, message.getHandleOfAction());

                        megaChatApi.getUserFirstname(message.getHandleOfAction(), listener);
                        megaChatApi.getUserLastname(message.getHandleOfAction(), listener);
                    }
                    else{
                        log("4-Name already asked and no name received: "+ message.getUserHandle());
                    }
                }

                String textToShow = "";
                if(privilege!=MegaChatRoom.PRIV_RM){
                    log("Participant was added");
                    if(message.getUserHandle()==myUserHandle){
                        log("By me");
                        textToShow = String.format(context.getString(R.string.message_add_participant), ((ViewHolderTextMessageChat)holder).fullNameTitle, context.getString(R.string.chat_me_text));
                    }
                    else{
//                        textToShow = String.format(context.getString(R.string.message_add_participant), message.getHandleOfAction()+"");
                        log("By other");
                        String fullNameAction = cC.getFullName(message.getUserHandle(), chatRoom);

                        if(fullNameAction==null){
                            fullNameAction = "";
                        }

                        if(fullNameAction.trim().length()<=0){

                            log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                            fullNameAction = "Unknown name";
                            if(!(((ViewHolderTextMessageChat)holder).nameRequestedAction)){
                                log("3-Call for nonContactName: "+ message.getUserHandle());
                                ((ViewHolderTextMessageChat)holder).nameRequestedAction=true;
                                ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, message.getUserHandle());
                                megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                                megaChatApi.getUserLastname(message.getUserHandle(), listener);
                            }
                            else{
                                log("4-Name already asked and no name received: "+ message.getUserHandle());
                            }
                        }

                        textToShow = String.format(context.getString(R.string.message_add_participant), ((ViewHolderTextMessageChat)holder).fullNameTitle, fullNameAction);

                    }
                }//END participant was added
                else{
                    log("Participant was removed or left");
                    if(message.getUserHandle()==myUserHandle){
                        textToShow = String.format(context.getString(R.string.message_remove_participant), ((ViewHolderTextMessageChat)holder).fullNameTitle, context.getString(R.string.chat_me_text));
                    }
                    else{

                        if(message.getUserHandle()==message.getHandleOfAction()){
                            log("The participant left the chat");

                            textToShow = String.format(context.getString(R.string.message_participant_left_group_chat), ((ViewHolderTextMessageChat)holder).fullNameTitle);

                        }
                        else{
                            log("The participant was removed");
                            String fullNameAction = cC.getFullName(message.getUserHandle(), chatRoom);

                            if(fullNameAction==null){
                                fullNameAction = "";
                            }

                            if(fullNameAction.trim().length()<=0){

                                log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                                fullNameAction = "Unknown name";
                                if(!(((ViewHolderTextMessageChat)holder).nameRequestedAction)){
                                    log("3-Call for nonContactName: "+ message.getUserHandle());
                                    ((ViewHolderTextMessageChat)holder).nameRequestedAction=true;
                                    ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, message.getUserHandle());
                                    megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                                    megaChatApi.getUserLastname(message.getUserHandle(), listener);
                                }
                                else{
                                    log("4-Name already asked and no name received: "+ message.getUserHandle());
                                }
                            }

                            textToShow = String.format(context.getString(R.string.message_remove_participant), ((ViewHolderTextMessageChat)holder).fullNameTitle, fullNameAction);
                        }
//                        textToShow = String.format(context.getString(R.string.message_remove_participant), message.getHandleOfAction()+"");
                    }
                } //END participant removed

                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }
                ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setText(result);

            } //END CONTACT MANAGEMENT MESSAGE
        }
        else if(message.getType()==MegaChatMessage.TYPE_PRIV_CHANGE){
            log("PRIVILEGE CHANGE message");
            if(message.getHandleOfAction()==myUserHandle){
                log("a moderator change my privilege");
                int privilege = message.getPrivilege();
                log("Privilege of the user: "+privilege);

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).dateText.setText(TimeChatUtils.formatDate(message.getTimestamp(), TimeChatUtils.DATE_SHORT_FORMAT));
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }

                ((ViewHolderTextMessageChat)holder).ownMessageLayout.setVisibility(View.VISIBLE);
                ((ViewHolderTextMessageChat)holder).contactMessageLayout.setVisibility(View.GONE);

                String privilegeString = "";
                if(privilege==MegaChatRoom.PRIV_MODERATOR){
                    privilegeString = context.getString(R.string.administrator_permission_label_participants_panel);
                }
                else if(privilege==MegaChatRoom.PRIV_STANDARD){
                    privilegeString = context.getString(R.string.standard_permission_label_participants_panel);
                }
                else if(privilege==MegaChatRoom.PRIV_RO){
                    privilegeString = context.getString(R.string.observer_permission_label_participants_panel);
                }
                else {
                    log("Change to other");
                    privilegeString = "Unknow";
                }

                String textToShow = "";

                if(message.getUserHandle()==myUserHandle){
                    log("I changed my Own permission");
                    textToShow = String.format(context.getString(R.string.message_permissions_changed), context.getString(R.string.chat_I_text), privilegeString, context.getString(R.string.chat_me_text));
                }
                else{
                    log("I was change by someone");
                    String fullNameAction = cC.getFullName(message.getUserHandle(), chatRoom);

                    if(fullNameAction==null){
                        fullNameAction = "";
                    }

                    if(fullNameAction.trim().length()<=0){

                        log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                        fullNameAction = "Unknown name";
                        if(!(((ViewHolderTextMessageChat)holder).nameRequestedAction)){
                            log("3-Call for nonContactName: "+ message.getUserHandle());
                            ((ViewHolderTextMessageChat)holder).nameRequestedAction=true;
                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, message.getUserHandle());
                            megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                            megaChatApi.getUserLastname(message.getUserHandle(), listener);
                        }
                        else{
                            log("4-Name already asked and no name received: "+ message.getUserHandle());
                        }
                    }
                    textToShow = String.format(context.getString(R.string.message_permissions_changed), context.getString(R.string.chat_I_text), privilegeString, fullNameAction);
                }

                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }

                ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setText(result);

                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.GONE);
                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);
                log("Visible own management message!");

                if (!multipleSelect) {
                    ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.GONE);

                    if (positionClicked != -1){
                        if (positionClicked == position){
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");

                    ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                    if (this.isItemChecked(position)) {
                        log("Selected: " + message.getContent());
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                    } else {
                        log("NOT selected");
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }
            }
            else{
                log("Participant privilege change!");
                log("Message type PRIVILEGE CHANGE: "+message.getContent());

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).dateText.setText(TimeChatUtils.formatDate(message.getTimestamp(), TimeChatUtils.DATE_SHORT_FORMAT));
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }
                ((ViewHolderTextMessageChat)holder).ownMessageLayout.setVisibility(View.GONE);
                ((ViewHolderTextMessageChat)holder).contactMessageLayout.setVisibility(View.VISIBLE);

                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.GONE);
                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);

                if (!multipleSelect) {
                    ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.GONE);

                    //Margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                    if (positionClicked != -1){
                        if (positionClicked == position){
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                    //Margins
                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                    //Set more margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    if (this.isItemChecked(position)) {
                        log("Selected: " + message.getContent());
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                    } else {
                        log("NOT selected");
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }

                ((ViewHolderTextMessageChat)holder).fullNameTitle = cC.getFullName(message.getHandleOfAction(), chatRoom);


                if(((ViewHolderTextMessageChat)holder).fullNameTitle==null){
                    ((ViewHolderTextMessageChat)holder).fullNameTitle = "";
                }

                if(((ViewHolderTextMessageChat)holder).fullNameTitle.trim().length()<=0){

                    log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                    ((ViewHolderTextMessageChat)holder).fullNameTitle = "Unknown name";
                    if(!(((ViewHolderTextMessageChat)holder).nameRequested)){
                        log("3-Call for nonContactName: "+ message.getUserHandle());

                        ((ViewHolderTextMessageChat)holder).nameRequested=true;
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, message.getHandleOfAction());

                        megaChatApi.getUserFirstname(message.getHandleOfAction(), listener);
                        megaChatApi.getUserLastname(message.getHandleOfAction(), listener);
                    }
                    else{
                        log("4-Name already asked and no name received: "+ message.getUserHandle());
                    }
                }

                int privilege = message.getPrivilege();
                String privilegeString = "";
                if(privilege==MegaChatRoom.PRIV_MODERATOR){
                    privilegeString = context.getString(R.string.administrator_permission_label_participants_panel);
                }
                else if(privilege==MegaChatRoom.PRIV_STANDARD){
                    privilegeString = context.getString(R.string.standard_permission_label_participants_panel);
                }
                else if(privilege==MegaChatRoom.PRIV_RO){
                    privilegeString = context.getString(R.string.observer_permission_label_participants_panel);
                }
                else {
                    log("Change to other");
                    privilegeString = "Unknow";
                }

                String textToShow = "";
                if(message.getUserHandle()==myUserHandle){
                    log("The privilege was change by me");
                    textToShow = String.format(context.getString(R.string.message_permissions_changed), ((ViewHolderTextMessageChat)holder).fullNameTitle, privilegeString, context.getString(R.string.chat_me_text));

                }
                else{
                    log("By other");
                    String fullNameAction = cC.getFullName(message.getUserHandle(), chatRoom);

                    if(fullNameAction==null){
                        fullNameAction = "";
                    }

                    if(fullNameAction.trim().length()<=0){

                        log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                        fullNameAction = "Unknown name";
                        if(!(((ViewHolderTextMessageChat)holder).nameRequestedAction)){
                            log("3-Call for nonContactName: "+ message.getUserHandle());
                            ((ViewHolderTextMessageChat)holder).nameRequestedAction=true;
                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, message.getUserHandle());
                            megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                            megaChatApi.getUserLastname(message.getUserHandle(), listener);
                        }
                        else{
                            log("4-Name already asked and no name received: "+ message.getUserHandle());
                        }
                    }

                    textToShow = String.format(context.getString(R.string.message_permissions_changed), ((ViewHolderTextMessageChat)holder).fullNameTitle, privilegeString, fullNameAction);
                }
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }

                ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setText(result);
            }
        }
        else{
            //OTHER TYPE OF MESSAGES
            if(message.getUserHandle()==myUserHandle) {
                log("MY message!!");
                log("MY message handle!!: "+message.getMsgId());

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).dateText.setText(TimeChatUtils.formatDate(message.getTimestamp(), TimeChatUtils.DATE_SHORT_FORMAT));
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleOwnMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }

                ((ViewHolderTextMessageChat)holder).ownMessageLayout.setVisibility(View.VISIBLE);
                ((ViewHolderTextMessageChat)holder).contactMessageLayout.setVisibility(View.GONE);

                if(message.getType()==MegaChatMessage.TYPE_NORMAL||message.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT||message.getType()==MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT||message.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){

                    log("Message type: "+message.getMsgId());

                    String messageContent = "";
                    if(message.getContent()!=null){
                        messageContent = message.getContent();
                    }

                    if(message.isEdited()){

                        log("Message is edited");
                        Spannable content = new SpannableString(messageContent);
                        int status = message.getStatus();
                        if((status==MegaChatMessage.STATUS_SERVER_REJECTED)||(status==MegaChatMessage.STATUS_SENDING_MANUAL)){
                            log("Show triangle retry!");
                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.mail_my_account));
                            content.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.mail_my_account)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            ((ViewHolderTextMessageChat)holder).triangleIcon.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).retryAlert.setVisibility(View.VISIBLE);
                        }
                        else if((status==MegaChatMessage.STATUS_SENDING)){
                            log("Status not received by server: "+message.getStatus());
                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.mail_my_account));
                            content.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.mail_my_account)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        else{
                            log("Status: "+message.getStatus());
                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.name_my_account));
                            content.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.name_my_account)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setText(content+" ");

                        Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                        edited.setSpan(new RelativeSizeSpan(0.85f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.accentColor)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        ((ViewHolderTextMessageChat)holder).contentOwnMessageText.append(edited);
                        ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.GONE);

                        if (!multipleSelect) {
                            //Margins
                            RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contentOwnMessageText.getLayoutParams();
                            ownMessageParams.setMargins(Util.scaleWidthPx(43, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setLayoutParams(ownMessageParams);

                            ((ViewHolderTextMessageChat)holder).ownMultiselectionLayout.setVisibility(View.GONE);
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.VISIBLE);
                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).ownMultiselectionLayout.setVisibility(View.VISIBLE);
                            //Margins
                            RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contentOwnMessageText.getLayoutParams();
                            ownMessageParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setLayoutParams(ownMessageParams);

                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionTickIcon.setVisibility(View.GONE);
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                    else if(message.isDeleted()){
                        log("Message is deleted");

                        RelativeLayout.LayoutParams ownManagementParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).ownManagementMessageText.getLayoutParams();
                        ownManagementParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setLayoutParams(ownManagementParams);

                        ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setText(context.getString(R.string.text_deleted_message));
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);

                        if (!multipleSelect) {
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.GONE);

                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                            if (this.isItemChecked(position)) {
                                log("Selected: " + message.getContent());
                                ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                            } else {
                                log("NOT selected");
                                ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                    else{

                        if(message.getType()==MegaChatMessage.TYPE_NORMAL){

                            if(message.getContent()!=null){
                                messageContent = message.getContent();
                            }

                            int status = message.getStatus();

                            if((status==MegaChatMessage.STATUS_SERVER_REJECTED)||(status==MegaChatMessage.STATUS_SENDING_MANUAL)){
                                log("Show triangle retry!");
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.mail_my_account));
                                ((ViewHolderTextMessageChat)holder).triangleIcon.setVisibility(View.VISIBLE);
                                ((ViewHolderTextMessageChat)holder).retryAlert.setVisibility(View.VISIBLE);
                            }
                            else if((status==MegaChatMessage.STATUS_SENDING)){
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.mail_my_account));
                            }
                            else{
                                log("Status: "+message.getStatus());
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.name_my_account));
                            }
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){

                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            messageContent = "An attachment was sent";
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT){
                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            messageContent = "Attachment revoked";
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){
                            ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            messageContent = "A contact was sent";
                        }
                        ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setText(messageContent);

                        ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.GONE);

                        //Margins
                        RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contentOwnMessageText.getLayoutParams();
                        ownMessageParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
                        ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setLayoutParams(ownMessageParams);

                        if (!multipleSelect) {

                            ((ViewHolderTextMessageChat)holder).ownMultiselectionLayout.setVisibility(View.GONE);

                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");

                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                ((ViewHolderTextMessageChat)holder).ownMultiselectionTickIcon.setVisibility(View.GONE);
                                ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                }
                else if(message.getType()==MegaChatMessage.TYPE_TRUNCATE){
                    log("Message type TRUNCATE");

                    RelativeLayout.LayoutParams ownManagementParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).ownManagementMessageText.getLayoutParams();
                    ownManagementParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setLayoutParams(ownManagementParams);

                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                    ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setText(context.getString(R.string.history_cleared_message));
                    ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);

                    if (!multipleSelect) {
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.GONE);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                        } else {
                            log("NOT selected");
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }

                }
                else if(message.getType()==MegaChatMessage.TYPE_CHAT_TITLE){
                    log("Message type TITLE CHANGE: "+message.getContent());

                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.GONE);

                    String messageContent = message.getContent();

                    String textToShow = String.format(context.getString(R.string.change_title_messages), context.getString(R.string.chat_I_text), messageContent);

                    Spanned result = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        result = Html.fromHtml(textToShow);
                    }

                    ((ViewHolderTextMessageChat)holder).ownManagementMessageText.setText(result);

                    ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.VISIBLE);

                    if (!multipleSelect) {
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.GONE);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                        } else {
                            log("NOT selected");
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            ((ViewHolderTextMessageChat)holder).ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                }
                else{
                    log("Type message ERROR: "+message.getType());
                    ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.tour_bar_red));

                    ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setText(context.getString(R.string.error_message_unrecognizable));
                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setVisibility(View.VISIBLE);
                    ((ViewHolderTextMessageChat)holder).ownManagementMessageLayout.setVisibility(View.GONE);

                    //Margins
                    RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contentOwnMessageText.getLayoutParams();
                    ownMessageParams.setMargins(Util.scaleWidthPx(43, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
                    ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setLayoutParams(ownMessageParams);
                    ((ViewHolderTextMessageChat)holder).ownMultiselectionLayout.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

//                log("Content: "+message.getContent());
                }
//                ((ViewHolderTextMessageChat)holder).retryAlert.setVisibility(View.VISIBLE);
            }
            else{
                long userHandle = message.getUserHandle();
                log("Contact message!!: "+userHandle);

                if(((ChatActivityLollipop) context).isGroup()){

                    ((ViewHolderTextMessageChat)holder).fullNameTitle = cC.getFullName(userHandle, chatRoom);

                    if(((ViewHolderTextMessageChat)holder).fullNameTitle==null){
                        ((ViewHolderTextMessageChat)holder).fullNameTitle = "";
                    }

                    if(((ViewHolderTextMessageChat)holder).fullNameTitle.trim().length()<=0){

                        log("NOT found in DB - ((ViewHolderTextMessageChat)holder).fullNameTitle");
                        ((ViewHolderTextMessageChat)holder).fullNameTitle = "Unknown name";
                        if(!(((ViewHolderTextMessageChat)holder).nameRequested)){
                            log("3-Call for nonContactName: "+ message.getUserHandle());
                            ((ViewHolderTextMessageChat)holder).nameRequested=true;
                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, ((ViewHolderTextMessageChat)holder), this, userHandle);
                            megaChatApi.getUserFirstname(userHandle, listener);
                            megaChatApi.getUserLastname(userHandle, listener);
                        }
                        else{
                            log("4-Name already asked and no name received: "+ message.getUserHandle());
                        }
                    }
                }
                else{
//                    ((ViewHolderTextMessageChat)holder).fullNameTitle = getContactFullName(((ViewHolderTextMessageChat)holder).userHandle);
                    ((ViewHolderTextMessageChat)holder).fullNameTitle = chatRoom.getTitle();
                }

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).dateText.setText(TimeChatUtils.formatDate(message.getTimestamp(), TimeChatUtils.DATE_SHORT_FORMAT));
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            ((ViewHolderTextMessageChat)holder).dateLayout.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).titleContactMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }
                ((ViewHolderTextMessageChat)holder).ownMessageLayout.setVisibility(View.GONE);
                ((ViewHolderTextMessageChat)holder).contactMessageLayout.setVisibility(View.VISIBLE);

                if(message.getType()==MegaChatMessage.TYPE_NORMAL||message.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT||message.getType()==MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT||message.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){

                    if(((ChatActivityLollipop) context).isGroup()){
                        Spannable name = new SpannableString(((ViewHolderTextMessageChat)holder).fullNameTitle+"\n");
//                    name.setSpan(new RelativeSizeSpan(0.85f), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        String handleString = megaApi.userHandleToBase64(((ViewHolderTextMessageChat)holder).userHandle);
                        String color = megaApi.getUserAvatarColor(handleString);
                        if(color!=null){
                            log("The color to set the avatar is "+color);
                            name.setSpan(new ForegroundColorSpan(Color.parseColor(color)), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        else{
                            log("Default color to the avatar");
                            name.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accentColor)), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

//                    name.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ((ViewHolderTextMessageChat)holder).contentContactMessageText.setText(name);
                    }
                    else{
                        ((ViewHolderTextMessageChat)holder).contentContactMessageText.setText("");
                    }

                    if(message.isEdited()){
                        log("Message is edited");
                        String messageContent = "";
                        if(message.getContent()!=null){
                            messageContent = message.getContent();
                        }

                        Spannable content = new SpannableString(messageContent);
                        content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.name_my_account)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ((ViewHolderTextMessageChat)holder).contentContactMessageText.append(content+" ");
//                    ((ViewHolderTextMessageChat)holder).contentContactMessageText.setText(content);

                        Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                        edited.setSpan(new RelativeSizeSpan(0.85f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accentColor)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ((ViewHolderTextMessageChat)holder).contentContactMessageText.append(edited);
                        ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.GONE);

                        //Margins
                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                        if (!multipleSelect) {
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).contactMultiselectionLayout.setVisibility(View.GONE);
                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).contactMultiselectionLayout.setVisibility(View.VISIBLE);
                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionTickIcon.setVisibility(View.GONE);
                                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                    else if(message.isDeleted()){
                        log("Message is deleted");
                        ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);

                        //Margins
                        RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                        contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                        if (!multipleSelect) {
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.GONE);

                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                            if (this.isItemChecked(position)) {
                                log("Selected: " + message.getContent());
                                ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                            } else {
                                log("NOT selected");
                                ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }

                        if(((ChatActivityLollipop) context).isGroup()){
                            String textToShow = String.format(context.getString(R.string.text_deleted_message_by), ((ViewHolderTextMessageChat)holder).fullNameTitle);
                            Spanned result = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                            } else {
                                result = Html.fromHtml(textToShow);
                            }
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setText(result);
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setText(context.getString(R.string.text_deleted_message));
                        }
                    }
                    else{
                        ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.VISIBLE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.GONE);

                        String messageContent = "";
                        if(message.getType()==MegaChatMessage.TYPE_NORMAL){
                            ((ViewHolderTextMessageChat)holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.name_my_account));
                            if(message.getContent()!=null){
                                messageContent = message.getContent();
                            }
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
                            ((ViewHolderTextMessageChat)holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            messageContent = "An attachment was received";
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT){
                            ((ViewHolderTextMessageChat)holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            messageContent = "Attachment revoked";
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){
                            ((ViewHolderTextMessageChat)holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            messageContent = "A contact was received";
                        }
                        ((ViewHolderTextMessageChat)holder).contentContactMessageText.append(messageContent);

                        //Margins
                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                        if (!multipleSelect) {
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).contactMultiselectionLayout.setVisibility(View.GONE);
                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).contactMultiselectionLayout.setVisibility(View.VISIBLE);
                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                ((ViewHolderTextMessageChat)holder).contactMultiselectionTickIcon.setVisibility(View.GONE);
                                ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                }
                else if(message.getType()==MegaChatMessage.TYPE_TRUNCATE){
                    log("Message type TRUNCATE");
                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);

                    //Margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                    if (!multipleSelect) {
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.GONE);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                        } else {
                            log("NOT selected");
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }

                    if(((ChatActivityLollipop) context).isGroup()){
                        String textToShow = String.format(context.getString(R.string.history_cleared_by), ((ViewHolderTextMessageChat)holder).fullNameTitle);
                        Spanned result = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            result = Html.fromHtml(textToShow);
                        }

                        ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setText(result);

                    }
                    else{
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setText(context.getString(R.string.history_cleared_message));
                    }
                }
                else if(message.getType()==MegaChatMessage.TYPE_CHAT_TITLE){
                    log("Message type CHANGE TITLE "+message.getContent());

                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.GONE);

                    ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.VISIBLE);

                    if (!multipleSelect) {
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.GONE);

                        //Margins
                        RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                        contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            ((ViewHolderTextMessageChat)holder).imageButtonThreeDots.setVisibility(View.GONE);
                        ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        //Margins
                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                        //Set more margins
                        RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contactManagementMessageText.getLayoutParams();
                        contactManagementTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_file_list_selected_row));

                        } else {
                            log("NOT selected");
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            ((ViewHolderTextMessageChat)holder).contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }

                    String messageContent = message.getContent();

                    String textToShow = String.format(context.getString(R.string.change_title_messages), ((ViewHolderTextMessageChat)holder).fullNameTitle, messageContent);

                    Spanned result = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        result = Html.fromHtml(textToShow);
                    }

                    ((ViewHolderTextMessageChat)holder).contactManagementMessageText.setText(result);
                }
                else{
                    log("Type message ERROR: "+message.getType());

                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setVisibility(View.VISIBLE);
                    ((ViewHolderTextMessageChat)holder).contactManagementMessageLayout.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).contentContactMessageText.setTextColor(ContextCompat.getColor(context, R.color.tour_bar_red));
                    ((ViewHolderTextMessageChat)holder).contentContactMessageText.setText(context.getString(R.string.error_message_unrecognizable));

                    //Margins
                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    ((ViewHolderTextMessageChat)holder).timeContactText.setLayoutParams(timeContactTextParams);

                    ((ViewHolderTextMessageChat)holder).contactMultiselectionLayout.setVisibility(View.GONE);
                    ((ViewHolderTextMessageChat)holder).contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                }
            }
        }

        //Check the next message to know the margin bottom the content message
        //        Message nextMessage = messages.get(position+1);
        //Margins
//        RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)((ViewHolderTextMessageChat)holder).contentOwnMessageText.getLayoutParams();
//        ownMessageParams.setMargins(Util.scaleWidthPx(11, outMetrics), Util.scaleHeightPx(-14, outMetrics), Util.scaleWidthPx(62, outMetrics), Util.scaleHeightPx(16, outMetrics));
//        ((ViewHolderTextMessageChat)holder).contentOwnMessageText.setLayoutParams(ownMessageParams);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public boolean isMultipleSelect() {
        log("isMultipleSelect");
        return multipleSelect;
    }

    public void setMultipleSelect(boolean multipleSelect) {
        log("setMultipleSelect");
        if (this.multipleSelect != multipleSelect) {
            this.multipleSelect = multipleSelect;
            notifyDataSetChanged();
        }
        if(this.multipleSelect)
        {
            selectedItems = new SparseBooleanArray();
        }
    }

    public void toggleSelection(int pos) {
        log("toggleSelection");
        ViewHolderTextMessageChat view = (ViewHolderTextMessageChat) listFragment.findViewHolderForLayoutPosition(pos);

        if (selectedItems.get(pos, false)) {
            log("delete pos: "+pos);
            selectedItems.delete(pos);
            if(view!=null){
                log("Start animation");
                Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
                MegaChatMessage message = messages.get(pos).getMessage();
//                String myMail = ((ChatActivityLollipop) context).getMyMail();
                if(message.getUserHandle()==myUserHandle) {
                    view.ownMultiselectionTickIcon.setVisibility(View.GONE);
                    view.contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                    view.ownMultiselectionLayout.startAnimation(flipAnimation);
                }
                else{
                    view.contactMultiselectionTickIcon.setVisibility(View.GONE);
                    view.contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                    view.contactMultiselectionLayout.startAnimation(flipAnimation);
                }
            }
        }
        else {
            log("PUT pos: "+pos);
            selectedItems.put(pos, true);
            if(view!=null){
                log("Start animation");
                Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
                MegaChatMessage message = messages.get(pos).getMessage();
                String myMail = ((ChatActivityLollipop) context).getMyMail();
                if(message.getUserHandle()==myUserHandle) {
                    view.ownMultiselectionLayout.startAnimation(flipAnimation);
                }
                else{
                    view.contactMultiselectionLayout.startAnimation(flipAnimation);
                }
            }
        }
        notifyItemChanged(pos);
    }

    public void selectAll(){
        for (int i= 0; i<this.getItemCount();i++){
            if(!isItemChecked(i)){
                toggleSelection(i);
            }
        }
    }

    public void clearSelections() {
        log("clearSelection");
        if(selectedItems!=null){
            selectedItems.clear();
        }
//        notifyDataSetChanged();
    }

    private boolean isItemChecked(int position) {
        return selectedItems.get(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    /*
     * Get request at specified position
     */
    public AndroidMegaChatMessage getMessageAt(int position) {
        try {
            if (messages != null) {
                return messages.get(position);
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return null;
    }

    /*
     * Get list of all selected chats
     */
    public ArrayList<AndroidMegaChatMessage> getSelectedMessages() {
        ArrayList<AndroidMegaChatMessage> messages = new ArrayList<AndroidMegaChatMessage>();

        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.valueAt(i) == true) {

                AndroidMegaChatMessage m = getMessageAt(selectedItems.keyAt(i));
                if (m != null){
                    messages.add(m);
                }
            }
        }
        return messages;
    }

    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setPositionClicked(int p){
        log("setPositionClicked: "+p);
        positionClicked = p;
        notifyDataSetChanged();
    }

    public void setMessages (ArrayList<AndroidMegaChatMessage> messages){
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void modifyMessage(ArrayList<AndroidMegaChatMessage> messages, int position){
        this.messages = messages;

        if(messages.get(position).getMessage().isDeleted()){
            log("Deleted the position message");
        }
        notifyItemChanged(position);
    }

    public void appendMessage(ArrayList<AndroidMegaChatMessage> messages){
        this.messages = messages;
        notifyItemInserted(messages.size() - 1);
    }

    public void addMessage(ArrayList<AndroidMegaChatMessage> messages, int position){
        this.messages = messages;
        notifyItemInserted(position);
    }

    public void removeMesage(int position, ArrayList<AndroidMegaChatMessage> messages){
        this.messages = messages;
        notifyItemRemoved(position);
    }

    public void loadPreviousMessage(ArrayList<AndroidMegaChatMessage> messages){
        this.messages = messages;
        notifyItemInserted(0);
    }

    public void loadPreviousMessages(ArrayList<AndroidMegaChatMessage> messages, int counter){
        log("loadPreviousMessages: "+counter);
        this.messages = messages;
        notifyItemRangeInserted(0, counter);
    }

    @Override
    public int getItemViewType(int position) {
        log("getItemViewType: position"+position);

        AndroidMegaChatMessage message = messages.get(position);

        int type = message.getMessage().getType();
        switch (type){
            case MegaChatMessage.TYPE_NODE_ATTACHMENT:{
                return ITEM_VIEW_TYPE_FILE;
            }
            case MegaChatMessage.TYPE_CONTACT_ATTACHMENT:{
                return ITEM_VIEW_TYPE_NORMAL;
            }
            case MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT:{
                return ITEM_VIEW_TYPE_NORMAL;
            }
            default:{
                return ITEM_VIEW_TYPE_NORMAL;
            }
        }
    }

    @Override
    public void onClick(View v) {
        log("onCLick");
//        ViewHolderTextMessageChat holder = (ViewHolderTextMessageChat) v.getTag();
//        if(holder==null){
//            log("Holder is null!");
//            return;
//        }
//        int currentPosition = ((ViewHolderTextMessageChat)holder).currentPosition;
//        switch (v.getId()){
//            case R.id.not_sent_own_message_text:{
//                log("not_sent_own_message_text, message_chat_item_layout");
//
//                AndroidMegaChatMessage m = (AndroidMegaChatMessage) getItem(currentPosition);
//                ((ChatActivityLollipop) context).showMsgNotSentPanel(m);
//                break;
//            }
//            case R.id.message_chat_item_layout:{
//                ((ChatActivityLollipop) context).itemClick(currentPosition);
//                break;
//            }
//
//        }
    }

    private static void log(String log) {
        Util.log("MegaChatLollipopAdapter", log);
    }
}
