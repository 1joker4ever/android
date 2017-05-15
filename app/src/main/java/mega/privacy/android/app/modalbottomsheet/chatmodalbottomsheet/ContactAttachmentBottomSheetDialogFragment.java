package mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.lollipop.ContactInfoActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.megachat.AndroidMegaChatMessage;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatHandleList;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;
import nz.mega.sdk.MegaUser;

public class ContactAttachmentBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    Context context;
    MegaChatHandleList handleList;
    AndroidMegaChatMessage message = null;
    long chatId;
    long messageId;
    NodeController nC;
    ChatController chatC;

    private BottomSheetBehavior mBehavior;

    CoordinatorLayout coordinatorLayout;

    public LinearLayout mainLinearLayout;
    public TextView titleNameContactChatPanel;
    public ImageView stateIcon;
    public TextView titleMailContactChatPanel;
    public RoundedImageView contactImageView;
    public TextView contactInitialLetter;
    LinearLayout optionView;
    LinearLayout optionInfo;
    LinearLayout optionStartConversation;
    LinearLayout optionInvite;
    LinearLayout optionRemove;

    DisplayMetrics outMetrics;

    static ManagerActivityLollipop.DrawerItem drawerItem = null;
    Bitmap thumb = null;

    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    DatabaseHandler dbH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log("onCreate");
        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if (megaChatApi == null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        if(savedInstanceState!=null) {
            log("Bundle is NOT NULL");
            chatId = savedInstanceState.getLong("chatId", -1);
            log("Handle of the chat: "+chatId);
            messageId = savedInstanceState.getLong("messageId", -1);
            log("Handle of the message: "+messageId);
            MegaChatMessage messageMega = megaChatApi.getMessage(chatId, messageId);
            if(messageMega!=null){
                message = new AndroidMegaChatMessage(messageMega);
            }
        }
        else{
            log("Bundle NULL");

            chatId = ((ChatActivityLollipop) context).idChat;
            messageId = ((ChatActivityLollipop) context).selectedMessageId;
            MegaChatMessage messageMega = megaChatApi.getMessage(chatId, messageId);
            if(messageMega!=null){
                message = new AndroidMegaChatMessage(messageMega);
            }
        }

        nC = new NodeController(context);
        chatC = new ChatController(context);

        dbH = DatabaseHandler.getDbHandler(getActivity());
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {

        super.setupDialog(dialog, style);
        log("setupDialog");
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_contact_attachment_item, null);

        mainLinearLayout = (LinearLayout) contentView.findViewById(R.id.contact_attachment_bottom_sheet);

        titleNameContactChatPanel = (TextView) contentView.findViewById(R.id.contact_attachment_chat_name_text);
        stateIcon = (ImageView) contentView.findViewById(R.id.contact_attachment_state_circle);

        stateIcon.setVisibility(View.VISIBLE);

        stateIcon.setMaxWidth(Util.scaleWidthPx(6,outMetrics));
        stateIcon.setMaxHeight(Util.scaleHeightPx(6,outMetrics));

        titleMailContactChatPanel = (TextView) contentView.findViewById(R.id.contact_attachment_chat_mail_text);
        contactImageView = (RoundedImageView) contentView.findViewById(R.id.contact_attachment_thumbnail);
        contactInitialLetter = (TextView) contentView.findViewById(R.id.contact_attachment_initial_letter);

        optionView = (LinearLayout) contentView.findViewById(R.id.option_view_layout);
        optionInfo = (LinearLayout) contentView.findViewById(R.id.option_info_layout);
        optionStartConversation = (LinearLayout) contentView.findViewById(R.id.option_start_conversation_layout);
        optionInvite = (LinearLayout) contentView.findViewById(R.id.option_invite_layout);
        optionRemove = (LinearLayout) contentView.findViewById(R.id.option_remove_layout);

        optionView.setOnClickListener(this);
        optionInfo.setOnClickListener(this);
        optionStartConversation.setOnClickListener(this);
        optionInvite.setOnClickListener(this);

        optionRemove.setVisibility(View.GONE);

        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            log("onCreate: Landscape configuration");
            titleNameContactChatPanel.setMaxWidth(Util.scaleWidthPx(270, outMetrics));
            titleMailContactChatPanel.setMaxWidth(Util.scaleWidthPx(270, outMetrics));
        }
        else{
            titleNameContactChatPanel.setMaxWidth(Util.scaleWidthPx(200, outMetrics));
            titleMailContactChatPanel.setMaxWidth(Util.scaleWidthPx(200, outMetrics));
        }

        if (message != null) {
            if(message.getMessage().getUsersCount()==1){
                log("One contact attached");

                optionView.setVisibility(View.GONE);
                optionInfo.setVisibility(View.VISIBLE);

                long userHandle = message.getMessage().getUserHandle(0);
                int state = megaChatApi.getUserOnlineStatus(userHandle);
                if(state == MegaChatApi.STATUS_ONLINE){
                    log("This user is connected");
                    stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_online));
                }
                else if(state == MegaChatApi.STATUS_AWAY){
                    log("This user is away");
                    stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_away));
                }
                else if(state == MegaChatApi.STATUS_BUSY){
                    log("This user is busy");
                    stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_busy));
                }
                else{
                    log("This user status is: "+state);
                    stateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_offline));
                }

                if(userHandle == megaApi.getMyUser().getHandle()){
//                    addAvatarParticipantPanel(userHandle, megaChatApi.getMyEmail());
                }
                else{
                    String userName = message.getMessage().getUserName(0);
                    titleNameContactChatPanel.setText(userName);

                    String userEmail = message.getMessage().getUserEmail(0);
                    titleMailContactChatPanel.setText(userEmail);
                    MegaUser contact = megaApi.getContact(userEmail);

                    if(contact!=null) {
                        if (contact.getVisibility() == MegaUser.VISIBILITY_VISIBLE) {
                            optionInfo.setVisibility(View.VISIBLE);
                            optionStartConversation.setVisibility(View.VISIBLE);
                            optionInvite.setVisibility(View.GONE);
                        }
                        else{
                            log("Non contact");
                            optionInfo.setVisibility(View.GONE);
                            optionStartConversation.setVisibility(View.GONE);
                            optionInvite.setVisibility(View.VISIBLE);
                        }
                    }
                    else{
                        log("Non contact");
                        optionInfo.setVisibility(View.GONE);
                        optionStartConversation.setVisibility(View.GONE);
                        optionInvite.setVisibility(View.VISIBLE);
                    }

                    addAvatarParticipantPanel(userHandle, userEmail, userName);

                }

            }
            else {
                log("More than one contact attached");
                optionView.setVisibility(View.VISIBLE);
                optionInfo.setVisibility(View.GONE);
            }
            dialog.setContentView(contentView);

            mBehavior = BottomSheetBehavior.from((View) contentView.getParent());
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        }
    }

    public void addAvatarParticipantPanel(long handle, String email, String name){

        File avatar = null;

        //Ask for avatar
        if (getActivity().getExternalCacheDir() != null){
            avatar = new File(getActivity().getExternalCacheDir().getAbsolutePath(), email + ".jpg");
        }
        else{
            avatar = new File(getActivity().getCacheDir().getAbsolutePath(), email + ".jpg");
        }
        Bitmap bitmap = null;
        if (avatar.exists()){
            if (avatar.length() > 0){
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                if (bitmap == null) {
                    avatar.delete();
                }
                else{
                    contactInitialLetter.setVisibility(View.GONE);
                    contactImageView.setImageBitmap(bitmap);
                    return;
                }
            }
        }

        log("Set default avatar");
        ////DEfault AVATAR
        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        String userHandleEncoded = MegaApiAndroid.userHandleToBase64(handle);
        String color = megaApi.getUserAvatarColor(userHandleEncoded);
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
        contactImageView.setImageBitmap(defaultAvatar);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        if(name!=null){
            if(!(name.trim().isEmpty())){
                String firstLetter = name.charAt(0) + "";
                firstLetter = firstLetter.toUpperCase(Locale.getDefault());
                contactInitialLetter.setText(firstLetter);
                contactInitialLetter.setTextColor(Color.WHITE);
                contactInitialLetter.setVisibility(View.VISIBLE);
                contactInitialLetter.setTextSize(24);
            }
            else{
                contactInitialLetter.setVisibility(View.GONE);
            }
        }
        else{
            contactInitialLetter.setVisibility(View.GONE);
        }

        ////
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.option_info_layout:{
                log("Info option");
                Intent i = new Intent(context, ContactInfoActivityLollipop.class);
                i.putExtra("name", message.getMessage().getUserEmail(0));
                context.startActivity(i);
                dismissAllowingStateLoss();
                break;
            }
            case R.id.option_view_layout:{
                log("View option");

                ((ChatActivityLollipop)context).showSnackbar("Coming soon");
//                context.startActivity(i);
//                dismissAllowingStateLoss();
                break;
            }
            case R.id.option_invite_layout:{
                log("Invite option");

                ContactController cC = new ContactController(context);
                cC.inviteContact(message.getMessage().getUserEmail(0));
                break;
            }
            case R.id.option_start_conversation_layout:{
                log("Import option");

                ((ChatActivityLollipop)context).showSnackbar("Coming soon");
                break;
            }
        }

//        dismiss();
        mBehavior = BottomSheetBehavior.from((View) mainLinearLayout.getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    @Override
    public void onAttach(Activity activity) {
        log("onAttach");
        super.onAttach(activity);
        this.context = activity;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putLong("chatId", chatId);
        outState.putLong("messageId", messageId);
    }

    private static void log(String log) {
        Util.log("NodeAttachmentBottomSheetDialogFragment", log);
    }
}
