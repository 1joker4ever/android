package mega.privacy.android.app.lollipop.adapters;

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
import mega.privacy.android.app.MegaContact;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.WrapTextView;
import mega.privacy.android.app.lollipop.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.lollipop.megachat.AndroidMegaChatMessage;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.NonContactInfo;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.TimeChatUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRoom;

public class MegaChatLollipopAdapter extends RecyclerView.Adapter<MegaChatLollipopAdapter.ViewHolderMessageChatList> {

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
    int type;
    private SparseBooleanArray selectedItems;

    DisplayMetrics outMetrics;
    DatabaseHandler dbH = null;

    public MegaChatLollipopAdapter(Context _context, ArrayList<AndroidMegaChatMessage> _messages, RecyclerView _listView) {
        log("new adapter");
        this.context = _context;
        this.messages = _messages;
        this.positionClicked = -1;

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
    }

    /*private view holder class*/
    public static class ViewHolderMessageChatList extends RecyclerView.ViewHolder {
        public ViewHolderMessageChatList(View view) {
            super(view);
        }

        int currentPosition;
        long userHandle;
        String fullNameTitle;

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
    ViewHolderMessageChatList holder;

    @Override
    public ViewHolderMessageChatList onCreateViewHolder(ViewGroup parent, int viewType) {
        log("onCreateViewHolder");

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        float density  = context.getResources().getDisplayMetrics().density;

        dbH = DatabaseHandler.getDbHandler(context);

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_chat, parent, false);
        holder = new ViewHolderMessageChatList(v);
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

    @Override
    public void onBindViewHolder(ViewHolderMessageChatList holder, int position) {
        log("onBindViewHolder: "+position);
        holder.currentPosition = position;

        holder.triangleIcon.setVisibility(View.GONE);
        holder.retryAlert.setVisibility(View.GONE);

        MegaChatMessage message = messages.get(position).getMessage();
        holder.userHandle = message.getUserHandle();

//        String myMail = ((ChatActivityLollipop) context).getMyMail();

        if(message.getType()==MegaChatMessage.TYPE_ALTER_PARTICIPANTS){
            log("ALTER PARTICIPANT MESSAGE!!");

            if(message.getUserHandleOfAction()==megaApi.getMyUser().getHandle()){
                log("me alter participant");

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            holder.dateLayout.setVisibility(View.VISIBLE);
                            holder.dateText.setText(TimeChatUtils.formatDate(message, TimeChatUtils.DATE_SHORT_FORMAT));
                            holder.titleOwnMessage.setVisibility(View.VISIBLE);
                            holder.timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleOwnMessage.setVisibility(View.VISIBLE);
                            holder.timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleOwnMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }

                holder.ownMessageLayout.setVisibility(View.VISIBLE);
                holder.contactMessageLayout.setVisibility(View.GONE);

                int privilege = message.getPrivilege();
                log("Privilege of me: "+privilege);
                String textToShow = "";
                String fullNameAction = getParticipantFullName(message.getUserHandle());

                if(fullNameAction.trim().length()<=0){
                    log("No name!");
                    NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle()+"");
                    if(nonContact!=null){
                        fullNameAction = nonContact.getFullName();
                    }
                    else{
                        log("Ask for name non-contact");
                        fullNameAction = "Participant left";
                        log("1-Call for nonContactName: "+ message.getUserHandle());
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
                        megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                        megaChatApi.getUserLastname(message.getUserHandle(), listener);
                    }
                }

                if(privilege!=MegaChatRoom.PRIV_RM){
                    log("I was added");
                    textToShow = String.format(context.getString(R.string.message_add_participant), context.getString(R.string.chat_I_text), fullNameAction);
                }
                else{
                    log("I was removed or left");
                    if(message.getUserHandle()==message.getUserHandleOfAction()){
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

                holder.ownManagementMessageText.setText(result);

                holder.contentOwnMessageLayout.setVisibility(View.GONE);
                holder.ownManagementMessageLayout.setVisibility(View.VISIBLE);

                if (!multipleSelect) {
                    holder.ownManagementMultiselectionLayout.setVisibility(View.GONE);
//            holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
                    if (positionClicked != -1){
                        if (positionClicked == position){
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                    holder.ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);
                    if(this.isItemChecked(position)){
                        log("Selected: "+message.getContent());
                        holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        holder.ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                    }
                    else{
                        log("NOT selected");
                        holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        holder.ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
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
                            holder.dateLayout.setVisibility(View.VISIBLE);
                            holder.dateText.setText(TimeChatUtils.formatDate(message, TimeChatUtils.DATE_SHORT_FORMAT));
                            holder.titleContactMessage.setVisibility(View.VISIBLE);
                            holder.timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleContactMessage.setVisibility(View.VISIBLE);
                            holder.timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleContactMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }
                holder.ownMessageLayout.setVisibility(View.GONE);
                holder.contactMessageLayout.setVisibility(View.VISIBLE);

                holder.contentContactMessageLayout.setVisibility(View.GONE);
                holder.contactManagementMessageLayout.setVisibility(View.VISIBLE);

                if (!multipleSelect) {
                    holder.contactManagementMultiselectionLayout.setVisibility(View.GONE);

                    //Margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    holder.timeContactText.setLayoutParams(timeContactTextParams);

                    if (positionClicked != -1){
                        if (positionClicked == position){
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                    holder.contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                    //Margins
                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    holder.timeContactText.setLayoutParams(timeContactTextParams);

                    //Set more margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    if (this.isItemChecked(position)) {
                        log("Selected: " + message.getContent());
                        holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        holder.contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                    } else {
                        log("NOT selected");
                        holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        holder.contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }

                holder.fullNameTitle = getParticipantFullName(message.getUserHandleOfAction());

                if(holder.fullNameTitle.trim().length()<=0){
                    NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandleOfAction()+"");
                    if(nonContact!=null){
                        holder.fullNameTitle = nonContact.getFullName();
                    }
                    else{
                        holder.fullNameTitle = "Unknown name";
                        log("3-Call for nonContactName: "+ message.getUserHandle());
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandleOfAction());
                        megaChatApi.getUserFirstname(message.getUserHandleOfAction(), listener);
                        megaChatApi.getUserLastname(message.getUserHandleOfAction(), listener);
                    }
                }

                String textToShow = "";
                if(privilege!=MegaChatRoom.PRIV_RM){
                    log("Participant was added");
                    if(message.getUserHandle()==megaApi.getMyUser().getHandle()){
                        log("By me");
                        textToShow = String.format(context.getString(R.string.message_add_participant), holder.fullNameTitle, context.getString(R.string.chat_me_text));
                    }
                    else{
//                        textToShow = String.format(context.getString(R.string.message_add_participant), message.getUserHandleOfAction()+"");
                        log("By other");
                        String fullNameAction = getParticipantFullName(message.getUserHandle());

                        if(fullNameAction.trim().length()<=0){
                            NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle()+"");
                            if(nonContact!=null){
                                fullNameAction = nonContact.getFullName();
                            }
                            else{
                                fullNameAction = "Unknown name";
                                log("2-Call for nonContactName: "+ message.getUserHandle());
                                ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
                                megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                                megaChatApi.getUserLastname(message.getUserHandle(), listener);
                            }

                        }
                        textToShow = String.format(context.getString(R.string.message_add_participant), holder.fullNameTitle, fullNameAction);

                    }
                }//END participant was added
                else{
                    log("Participant was removed or left");
                    if(message.getUserHandle()==megaApi.getMyUser().getHandle()){
                        textToShow = String.format(context.getString(R.string.message_remove_participant), holder.fullNameTitle, context.getString(R.string.chat_me_text));
                    }
                    else{

                        if(message.getUserHandle()==message.getUserHandleOfAction()){
                            log("The participant left the chat");

                            textToShow = String.format(context.getString(R.string.message_participant_left_group_chat), holder.fullNameTitle);

                        }
                        else{
                            log("The participant was removed");
                            String fullNameAction = getParticipantFullName(message.getUserHandle());

                            if(fullNameAction.trim().length()<=0){
                                NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle()+"");
                                if(nonContact!=null){
                                    fullNameAction = nonContact.getFullName();
                                }
                                else{
                                    fullNameAction = "Unknown name";
                                    log("4-Call for nonContactName: "+ message.getUserHandle());
                                    ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
                                    megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                                    megaChatApi.getUserLastname(message.getUserHandle(), listener);
                                }

                            }

                            textToShow = String.format(context.getString(R.string.message_remove_participant), holder.fullNameTitle, fullNameAction);
                        }
//                        textToShow = String.format(context.getString(R.string.message_remove_participant), message.getUserHandleOfAction()+"");
                    }
                } //END participant removed

                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }
                holder.contactManagementMessageText.setText(result);

            } //END CONTACT MANAGEMENT MESSAGE
        }
        else if(message.getType()==MegaChatMessage.TYPE_PRIV_CHANGE){
            log("PRIVILEGE CHANGE message");
            if(message.getUserHandleOfAction()==megaApi.getMyUser().getHandle()){
                log("a moderator change my privilege");
                int privilege = message.getPrivilege();
                log("Privilege of the user: "+privilege);

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            holder.dateLayout.setVisibility(View.VISIBLE);
                            holder.dateText.setText(TimeChatUtils.formatDate(message, TimeChatUtils.DATE_SHORT_FORMAT));
                            holder.titleOwnMessage.setVisibility(View.VISIBLE);
                            holder.timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleOwnMessage.setVisibility(View.VISIBLE);
                            holder.timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleOwnMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }

                holder.ownMessageLayout.setVisibility(View.VISIBLE);
                holder.contactMessageLayout.setVisibility(View.GONE);

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

                if(message.getUserHandle()==megaApi.getMyUser().getHandle()){
                    log("I changed my Own permission");
                    textToShow = String.format(context.getString(R.string.message_permissions_changed), context.getString(R.string.chat_I_text), privilegeString, context.getString(R.string.chat_me_text));
                }
                else{
                    log("I was change by someone");
                    String fullNameAction = getParticipantFullName(message.getUserHandle());

                    if(fullNameAction.trim().length()<=0){
                        NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle()+"");
                        if(nonContact!=null){
                            fullNameAction = nonContact.getFullName();
                        }
                        else{
                            fullNameAction = "Unknown name";
                            log("5-Call for nonContactName: "+ message.getUserHandle());
                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
                            megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                            megaChatApi.getUserLastname(message.getUserHandle(), listener);
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

                holder.ownManagementMessageText.setText(result);

                holder.contentOwnMessageLayout.setVisibility(View.GONE);
                holder.ownManagementMessageLayout.setVisibility(View.VISIBLE);
                log("Visible own management message!");

                if (!multipleSelect) {
                    holder.ownManagementMultiselectionLayout.setVisibility(View.GONE);

                    if (positionClicked != -1){
                        if (positionClicked == position){
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");

                    holder.ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                    if (this.isItemChecked(position)) {
                        log("Selected: " + message.getContent());
                        holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        holder.ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                    } else {
                        log("NOT selected");
                        holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        holder.ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }
            }
            else{
                log("Participant privilege change!");
                log("Message type PRIVILEGE CHANGE: "+message.getContent());

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            holder.dateLayout.setVisibility(View.VISIBLE);
                            holder.dateText.setText(TimeChatUtils.formatDate(message, TimeChatUtils.DATE_SHORT_FORMAT));
                            holder.titleContactMessage.setVisibility(View.VISIBLE);
                            holder.timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleContactMessage.setVisibility(View.VISIBLE);
                            holder.timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleContactMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }
                holder.ownMessageLayout.setVisibility(View.GONE);
                holder.contactMessageLayout.setVisibility(View.VISIBLE);

                holder.contentContactMessageLayout.setVisibility(View.GONE);
                holder.contactManagementMessageLayout.setVisibility(View.VISIBLE);

                if (!multipleSelect) {
                    holder.contactManagementMultiselectionLayout.setVisibility(View.GONE);

                    //Margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    holder.timeContactText.setLayoutParams(timeContactTextParams);

                    if (positionClicked != -1){
                        if (positionClicked == position){
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            listFragment.smoothScrollToPosition(positionClicked);
                        }
                        else{
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }
                    else{
                        holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                } else {
                    log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                    holder.contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                    //Margins
                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    holder.timeContactText.setLayoutParams(timeContactTextParams);

                    //Set more margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    if (this.isItemChecked(position)) {
                        log("Selected: " + message.getContent());
                        holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                        holder.contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                        holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                    } else {
                        log("NOT selected");
                        holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                        holder.contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                        holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }

                holder.fullNameTitle = getParticipantFullName(message.getUserHandleOfAction());

                if(holder.fullNameTitle.trim().length()<=0){
                    NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandleOfAction()+"");
                    if(nonContact!=null){
                        holder.fullNameTitle = nonContact.getFullName();
                    }
                    else{
                        holder.fullNameTitle = "Unknown name";
                        log("6-Call for nonContactName: "+ message.getUserHandle());
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandleOfAction());
                        megaChatApi.getUserFirstname(message.getUserHandleOfAction(), listener);
                        megaChatApi.getUserLastname(message.getUserHandleOfAction(), listener);
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
                if(message.getUserHandle()==megaApi.getMyUser().getHandle()){
                    log("The privilege was change by me");
                    textToShow = String.format(context.getString(R.string.message_permissions_changed), holder.fullNameTitle, privilegeString, context.getString(R.string.chat_me_text));

                }
                else{
                    log("By other");
                    String fullNameAction = getParticipantFullName(message.getUserHandle());

                    if(fullNameAction.trim().length()<=0){
                        NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle()+"");
                        if(nonContact!=null){
                            fullNameAction = nonContact.getFullName();
                        }
                        else{
                            fullNameAction = "Unknown name";
                            log("8-Call for nonContactName: "+ message.getUserHandle());
                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
                            megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                            megaChatApi.getUserLastname(message.getUserHandle(), listener);
                        }
                    }

                    textToShow = String.format(context.getString(R.string.message_permissions_changed), holder.fullNameTitle, privilegeString, fullNameAction);
                }
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }

                holder.contactManagementMessageText.setText(result);
            }
        }
        else{
            //OTHER TYPE OF MESSAGES
            if(message.getUserHandle()==megaApi.getMyUser().getHandle()) {
                log("MY message!!:");
                log("MY message handle!!: "+message.getMsgId());

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            holder.dateLayout.setVisibility(View.VISIBLE);
                            holder.dateText.setText(TimeChatUtils.formatDate(message, TimeChatUtils.DATE_SHORT_FORMAT));
                            holder.titleOwnMessage.setVisibility(View.VISIBLE);
                            holder.timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleOwnMessage.setVisibility(View.VISIBLE);
                            holder.timeOwnText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleOwnMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }

                holder.ownMessageLayout.setVisibility(View.VISIBLE);
                holder.contactMessageLayout.setVisibility(View.GONE);

                if(message.getType()==MegaChatMessage.TYPE_NORMAL){
                    log("Message type NORMAL: "+message.getMsgId());

                    String messageContent = "";
                    if(message.getContent()!=null){
                        messageContent = message.getContent();
                    }

                    if(message.isEdited()){
                        //Margins
                        RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)holder.contentOwnMessageText.getLayoutParams();
                        ownMessageParams.setMargins(Util.scaleWidthPx(43, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
                        holder.contentOwnMessageText.setLayoutParams(ownMessageParams);

                        log("Message is edited");
                        Spannable content = new SpannableString(messageContent);
                        content.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.name_my_account)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.contentOwnMessageText.setText(content+" ");

                        Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                        edited.setSpan(new RelativeSizeSpan(0.85f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.accentColor)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        holder.contentOwnMessageText.append(edited);
                        holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
                        holder.ownManagementMessageLayout.setVisibility(View.GONE);

                        if (!multipleSelect) {
                            holder.ownMultiselectionLayout.setVisibility(View.GONE);
//            holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                            holder.ownMultiselectionLayout.setVisibility(View.VISIBLE);
                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                holder.ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                holder.ownMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                holder.ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                holder.ownMultiselectionTickIcon.setVisibility(View.GONE);
                                holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                    else if(message.isDeleted()){
                        log("Message is deleted");

                        RelativeLayout.LayoutParams ownManagementParams = (RelativeLayout.LayoutParams)holder.ownManagementMessageText.getLayoutParams();
                        ownManagementParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        holder.ownManagementMessageText.setLayoutParams(ownManagementParams);

                        holder.contentOwnMessageLayout.setVisibility(View.GONE);
                        holder.ownManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                        holder.ownManagementMessageText.setText(context.getString(R.string.text_deleted_message));
                        holder.ownManagementMessageLayout.setVisibility(View.VISIBLE);

                        if (!multipleSelect) {
                            holder.ownManagementMultiselectionLayout.setVisibility(View.GONE);

                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                            holder.ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                            if (this.isItemChecked(position)) {
                                log("Selected: " + message.getContent());
                                holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                holder.ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                            } else {
                                log("NOT selected");
                                holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                holder.ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                                holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                    else{
                        //Margins
                        RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)holder.contentOwnMessageText.getLayoutParams();
                        ownMessageParams.setMargins(Util.scaleWidthPx(43, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
                        holder.contentOwnMessageText.setLayoutParams(ownMessageParams);

                        int status = message.getStatus();
                        if((status==MegaChatMessage.STATUS_SERVER_REJECTED)||(status==MegaChatMessage.STATUS_SENDING_MANUAL)){
                            log("Show triangle retry!");
                            holder.contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.mail_my_account));
                            //Margins
                            ownMessageParams = (RelativeLayout.LayoutParams)holder.contentOwnMessageText.getLayoutParams();
                            ownMessageParams.setMargins(Util.scaleWidthPx(0, outMetrics), 0, Util.scaleWidthPx(68, outMetrics), 0);
                            holder.contentOwnMessageText.setLayoutParams(ownMessageParams);

                            holder.triangleIcon.setVisibility(View.VISIBLE);
                            holder.retryAlert.setVisibility(View.VISIBLE);
                        }
                        else if((status==MegaChatMessage.STATUS_SENDING)){
                            holder.contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.mail_my_account));
                        }
                        else{
                            log("Status: "+message.getStatus());
                            holder.contentOwnMessageText.setTextColor(ContextCompat.getColor(context, R.color.name_my_account));
                        }
                        holder.contentOwnMessageText.setText(messageContent);
                        holder.contentOwnMessageLayout.setVisibility(View.VISIBLE);
                        holder.ownManagementMessageLayout.setVisibility(View.GONE);

                        if (!multipleSelect) {
                            holder.ownMultiselectionLayout.setVisibility(View.GONE);
//            holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                            holder.ownMultiselectionLayout.setVisibility(View.VISIBLE);
                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                holder.ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                holder.ownMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                holder.ownMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                holder.ownMultiselectionTickIcon.setVisibility(View.GONE);
                                holder.contentOwnMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                }
                else if(message.getType()==MegaChatMessage.TYPE_TRUNCATE){
                    log("Message type TRUNCATE");

                    RelativeLayout.LayoutParams ownManagementParams = (RelativeLayout.LayoutParams)holder.ownManagementMessageText.getLayoutParams();
                    ownManagementParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    holder.ownManagementMessageText.setLayoutParams(ownManagementParams);

                    holder.contentOwnMessageLayout.setVisibility(View.GONE);
                    holder.ownManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                    holder.ownManagementMessageText.setText(context.getString(R.string.history_cleared_message));
                    holder.ownManagementMessageLayout.setVisibility(View.VISIBLE);

                    if (!multipleSelect) {
                        holder.ownManagementMultiselectionLayout.setVisibility(View.GONE);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                        holder.ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            holder.ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                        } else {
                            log("NOT selected");
                            holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            holder.ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }

                }
                else if(message.getType()==MegaChatMessage.TYPE_CHAT_TITLE){
                    log("Message type TITLE CHANGE: "+message.getContent());

                    holder.contentOwnMessageLayout.setVisibility(View.GONE);

                    String messageContent = message.getContent();

                    String textToShow = String.format(context.getString(R.string.change_title_messages), context.getString(R.string.chat_I_text), messageContent);

                    Spanned result = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        result = Html.fromHtml(textToShow);
                    }

                    holder.ownManagementMessageText.setText(result);

                    holder.ownManagementMessageLayout.setVisibility(View.VISIBLE);

                    if (!multipleSelect) {
                        holder.ownManagementMultiselectionLayout.setVisibility(View.GONE);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                        holder.ownManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            holder.ownManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                        } else {
                            log("NOT selected");
                            holder.ownManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            holder.ownManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            holder.ownManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }

                }
                else{
                    log("Type message: "+message.getType());
//                log("Content: "+message.getContent());
                }
            }
            else{
                log("Contact message!!");
                long userHandle = message.getUserHandle();

                if(((ChatActivityLollipop) context).isGroup()){

                    holder.fullNameTitle = getParticipantFullName(userHandle);

                    if(holder.fullNameTitle.trim().length()<=0){
//                        String userHandleString = megaApi.userHandleToBase64(message.getUserHandle());
                        NonContactInfo nonContact = dbH.findNonContactByHandle(message.getUserHandle()+"");
                        if(nonContact!=null){
                            holder.fullNameTitle = nonContact.getFullName();
                        }
                        else{
                            holder.fullNameTitle = "Unknown name";
//
                            ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, message.getUserHandle());
                            megaChatApi.getUserFirstname(message.getUserHandle(), listener);
                            megaChatApi.getUserLastname(message.getUserHandle(), listener);
                        }
                    }
                }
                else{
                    holder.fullNameTitle = getContactFullName(holder.userHandle);
                }

                if(messages.get(position).getInfoToShow()!=-1){
                    switch (messages.get(position).getInfoToShow()){
                        case Constants.CHAT_ADAPTER_SHOW_ALL:{
                            holder.dateLayout.setVisibility(View.VISIBLE);
                            holder.dateText.setText(TimeChatUtils.formatDate(message, TimeChatUtils.DATE_SHORT_FORMAT));
                            holder.titleContactMessage.setVisibility(View.VISIBLE);
                            holder.timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_TIME:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleContactMessage.setVisibility(View.VISIBLE);
                            holder.timeContactText.setText(TimeChatUtils.formatTime(message));
                            break;
                        }
                        case Constants.CHAT_ADAPTER_SHOW_NOTHING:{
                            holder.dateLayout.setVisibility(View.GONE);
                            holder.titleContactMessage.setVisibility(View.GONE);
                            break;
                        }
                    }
                }
                holder.ownMessageLayout.setVisibility(View.GONE);
                holder.contactMessageLayout.setVisibility(View.VISIBLE);

                if(message.getType()==MegaChatMessage.TYPE_NORMAL){

                    if(((ChatActivityLollipop) context).isGroup()){
                        Spannable name = new SpannableString(holder.fullNameTitle+"\n");
//                    name.setSpan(new RelativeSizeSpan(0.85f), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        String handleString = megaApi.userHandleToBase64(holder.userHandle);
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
                        holder.contentContactMessageText.setText(name);
                    }
                    else{
                        holder.contentContactMessageText.setText("");
                    }

                    if(message.isEdited()){
                        log("Message is edited");
                        String messageContent = "";
                        if(message.getContent()!=null){
                            messageContent = message.getContent();
                        }

                        Spannable content = new SpannableString(messageContent);
                        content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.name_my_account)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.contentContactMessageText.append(content+" ");
//                    holder.contentContactMessageText.setText(content);

                        Spannable edited = new SpannableString(context.getString(R.string.edited_message_text));
                        edited.setSpan(new RelativeSizeSpan(0.85f), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accentColor)), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        edited.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, edited.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.contentContactMessageText.append(edited);
                        holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
                        holder.contactManagementMessageLayout.setVisibility(View.GONE);

                        //Margins
                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        holder.timeContactText.setLayoutParams(timeContactTextParams);

                        if (!multipleSelect) {
//            holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
                            holder.contactMultiselectionLayout.setVisibility(View.GONE);
                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                            holder.contactMultiselectionLayout.setVisibility(View.VISIBLE);
                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                holder.contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                holder.contactMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                holder.contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                holder.contactMultiselectionTickIcon.setVisibility(View.GONE);
                                holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                    else if(message.isDeleted()){
                        log("Message is deleted");
                        holder.contentContactMessageLayout.setVisibility(View.GONE);
                        holder.contactManagementMessageLayout.setVisibility(View.VISIBLE);

                        //Margins
                        RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                        contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        holder.timeContactText.setLayoutParams(timeContactTextParams);

                        if (!multipleSelect) {
                            holder.contactManagementMultiselectionLayout.setVisibility(View.GONE);

                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                            holder.contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                            if (this.isItemChecked(position)) {
                                log("Selected: " + message.getContent());
                                holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                holder.contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                            } else {
                                log("NOT selected");
                                holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                holder.contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                                holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }

                        if(((ChatActivityLollipop) context).isGroup()){
                            String textToShow = String.format(context.getString(R.string.text_deleted_message_by), holder.fullNameTitle);
                            Spanned result = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                            } else {
                                result = Html.fromHtml(textToShow);
                            }
                            holder.contactManagementMessageText.setText(result);
                        }
                        else{
                            holder.contactManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                            holder.contactManagementMessageText.setText(context.getString(R.string.text_deleted_message));
                        }
                    }
                    else{
                        holder.contentContactMessageLayout.setVisibility(View.VISIBLE);
                        holder.contactManagementMessageLayout.setVisibility(View.GONE);
                        holder.contactManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                        String messageContent = "";
                        if(message.getContent()!=null){
                            messageContent = message.getContent();
                        }
                        holder.contentContactMessageText.append(messageContent);

                        //Margins
                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        holder.timeContactText.setLayoutParams(timeContactTextParams);

                        if (!multipleSelect) {
//            holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
                            holder.contactMultiselectionLayout.setVisibility(View.GONE);
                            if (positionClicked != -1){
                                if (positionClicked == position){
                                    holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                    listFragment.smoothScrollToPosition(positionClicked);
                                }
                                else{
                                    holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                                }
                            }
                            else{
                                holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        } else {
                            log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                            holder.contactMultiselectionLayout.setVisibility(View.VISIBLE);
                            if(this.isItemChecked(position)){
                                log("Selected: "+message.getContent());
                                holder.contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                                holder.contactMultiselectionTickIcon.setVisibility(View.VISIBLE);
                                holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                            }
                            else{
                                log("NOT selected");
                                holder.contactMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                                holder.contactMultiselectionTickIcon.setVisibility(View.GONE);
                                holder.contentContactMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                    }
                }
                else if(message.getType()==MegaChatMessage.TYPE_TRUNCATE){
                    log("Message type TRUNCATE");
                    holder.contentContactMessageLayout.setVisibility(View.GONE);
                    holder.contactManagementMessageLayout.setVisibility(View.VISIBLE);

                    //Margins
                    RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                    contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                    holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                    RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                    timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_NORMAL, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                    holder.timeContactText.setLayoutParams(timeContactTextParams);

                    if (!multipleSelect) {
                        holder.contactManagementMultiselectionLayout.setVisibility(View.GONE);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                        holder.contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            holder.contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                        } else {
                            log("NOT selected");
                            holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            holder.contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }

                    if(((ChatActivityLollipop) context).isGroup()){
                        String textToShow = String.format(context.getString(R.string.history_cleared_by), holder.fullNameTitle);
                        Spanned result = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            result = Html.fromHtml(textToShow);
                        }

                        holder.contactManagementMessageText.setText(result);

                    }
                    else{
                        holder.contactManagementMessageText.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
                        holder.contactManagementMessageText.setText(context.getString(R.string.history_cleared_message));
                    }
                }
                else if(message.getType()==MegaChatMessage.TYPE_CHAT_TITLE){
                    log("Message type CHANGE TITLE "+message.getContent());

                    holder.contentContactMessageLayout.setVisibility(View.GONE);

                    holder.contactManagementMessageLayout.setVisibility(View.VISIBLE);

                    if (!multipleSelect) {
                        holder.contactManagementMultiselectionLayout.setVisibility(View.GONE);

                        //Margins
                        RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                        contactManagementTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(RIGHT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        holder.timeContactText.setLayoutParams(timeContactTextParams);

                        if (positionClicked != -1){
                            if (positionClicked == position){
                                holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));
                                listFragment.smoothScrollToPosition(positionClicked);
                            }
                            else{
                                holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                            }
                        }
                        else{
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    } else {
                        log("Multiselect ON");
//            holder.imageButtonThreeDots.setVisibility(View.GONE);
                        holder.contactManagementMultiselectionLayout.setVisibility(View.VISIBLE);

                        //Margins
                        RelativeLayout.LayoutParams timeContactTextParams = (RelativeLayout.LayoutParams)holder.timeContactText.getLayoutParams();
                        timeContactTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), 0, Util.scaleWidthPx(7, outMetrics), 0);
                        holder.timeContactText.setLayoutParams(timeContactTextParams);

                        //Set more margins
                        RelativeLayout.LayoutParams contactManagementTextParams = (RelativeLayout.LayoutParams)holder.contactManagementMessageText.getLayoutParams();
                        contactManagementTextParams.setMargins(Util.scaleWidthPx(MULTISELECTION_LEFT_MARGIN_CONTACT_MSG_MANAGEMENT, outMetrics), Util.scaleHeightPx(5, outMetrics), Util.scaleWidthPx(68, outMetrics), Util.scaleHeightPx(5, outMetrics));
                        holder.contactManagementMessageText.setLayoutParams(contactManagementTextParams);

                        if (this.isItemChecked(position)) {
                            log("Selected: " + message.getContent());
                            holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_filled));
                            holder.contactManagementMultiselectionTickIcon.setVisibility(View.VISIBLE);
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.file_list_selected_row));

                        } else {
                            log("NOT selected");
                            holder.contactManagementMultiselectionImageView.setImageDrawable(context.getDrawable(R.drawable.message_multiselection_empty));
                            holder.contactManagementMultiselectionTickIcon.setVisibility(View.GONE);
                            holder.contactManagementMessageLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                        }
                    }

                    String messageContent = message.getContent();

                    String textToShow = String.format(context.getString(R.string.change_title_messages), holder.fullNameTitle, messageContent);

                    Spanned result = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        result = Html.fromHtml(textToShow);
                    }

                    holder.contactManagementMessageText.setText(result);
                }
                else{
                    log("Type message: "+message.getType());
                    log("Content: "+message.getContent());
                }
            }
        }

        //Check the next message to know the margin bottom the content message
        //        Message nextMessage = messages.get(position+1);
        //Margins
//        RelativeLayout.LayoutParams ownMessageParams = (RelativeLayout.LayoutParams)holder.contentOwnMessageText.getLayoutParams();
//        ownMessageParams.setMargins(Util.scaleWidthPx(11, outMetrics), Util.scaleHeightPx(-14, outMetrics), Util.scaleWidthPx(62, outMetrics), Util.scaleHeightPx(16, outMetrics));
//        holder.contentOwnMessageText.setLayoutParams(ownMessageParams);
    }

    public String getParticipantFullName(long userHandle){
        String participantFirstName = ((ChatActivityLollipop) context).getParticipantFirstName(userHandle);
        String participantLastName = ((ChatActivityLollipop) context).getParticipantLastName(userHandle);

        if(participantFirstName==null){
            participantFirstName="";
        }
        if(participantLastName == null){
            participantLastName="";
        }

        if (participantFirstName.trim().length() <= 0){
            log("Participant1: "+participantFirstName);
            return participantLastName;
        }
        else{
            log("Participant2: "+participantLastName);
            return participantFirstName + " " + participantLastName;
        }
    }

    public String getContactFullName(long userHandle){
        MegaContact contactDB = dbH.findContactByHandle(String.valueOf(userHandle));
        if(contactDB!=null){

            String name = contactDB.getName();
            String lastName = contactDB.getLastName();

            if(name==null){
                name="";
            }
            if(lastName==null){
                lastName="";
            }
            String fullName = "";

            if (name.trim().length() <= 0){
                    fullName = lastName;
            }
            else{
                fullName = name + " " + lastName;
            }

            if (fullName.trim().length() <= 0){
                log("Put email as fullname");
                String email = contactDB.getMail();
                String[] splitEmail = email.split("[@._]");
                fullName = splitEmail[0];
            }

            return fullName;
        }
        else{
            String email = contactDB.getMail();
            String[] splitEmail = email.split("[@._]");
            String fullName = splitEmail[0];
            return fullName;
        }
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
        ViewHolderMessageChatList view = (ViewHolderMessageChatList) listFragment.findViewHolderForLayoutPosition(pos);

        if (selectedItems.get(pos, false)) {
            log("delete pos: "+pos);
            selectedItems.delete(pos);
            if(view!=null){
                log("Start animation");
                Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
                MegaChatMessage message = messages.get(pos).getMessage();
//                String myMail = ((ChatActivityLollipop) context).getMyMail();
                if(message.getUserHandle()==megaApi.getMyUser().getHandle()) {
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
                if(message.getUserHandle()==megaApi.getMyUser().getHandle()) {
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

    public void loadPreviousMessage(ArrayList<AndroidMegaChatMessage> messages){
        this.messages = messages;
        notifyItemInserted(0);
    }

    public void loadPreviousMessages(ArrayList<AndroidMegaChatMessage> messages, int counter){
        log("loadPreviousMessages: "+counter);
        this.messages = messages;
        notifyItemRangeInserted(0, counter);
    }

    private static void log(String log) {
        Util.log("MegaChatLollipopAdapter", log);
    }
}
