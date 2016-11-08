package mega.privacy.android.app.lollipop.megachat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.EditTextCursorWatcher;
import mega.privacy.android.app.components.MegaLinearLayoutManager;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.components.SlidingUpPanelLayout;
import mega.privacy.android.app.lollipop.AddContactActivityLollipop;
import mega.privacy.android.app.lollipop.MegaContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.PinActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaListChatLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaParticipantsChatLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatPanelListener;
import mega.privacy.android.app.lollipop.listeners.MultipleGroupChatRequestListener;
import mega.privacy.android.app.lollipop.listeners.MultipleRequestListener;
import mega.privacy.android.app.lollipop.listeners.ParticipantPanelListener;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatPeerList;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaUser;


public class GroupChatInfoActivityLollipop extends PinActivityLollipop implements MegaChatRequestListenerInterface, View.OnClickListener, MegaRequestListenerInterface, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener {

    GroupChatInfoActivityLollipop groupChatInfoActivity;

    long chatHandle;
    MegaChatRoom chat;

    MegaChatParticipant selectedParticipant;

    AlertDialog permissionsDialog;
    AlertDialog changeTitleDialog;

    MenuItem addParticipantItem;
    MenuItem changeTitleItem;

    private MegaApiAndroid megaApi = null;
    MegaChatApiAndroid megaChatApi = null;

    Display display;
    DisplayMetrics outMetrics;
    float density;
    float scaleW;
    float scaleH;
    float scaleText;

    DatabaseHandler dbH = null;
    ChatPreferences chatPrefs = null;

    CoordinatorLayout fragmentContainer;
    TextView initialLetter;
    RoundedImageView avatarImageView;

    ScrollView scrollView;

    LinearLayout infoLayout;
    RelativeLayout avatarLayout;
    TextView infoTitleChatText;
    TextView infoNumParticipantsText;
    RelativeLayout infoTextContainerLayout;
    ImageView editImageView;

    LinearLayout notificationsLayout;
    Switch notificationsSwitch;
    TextView notificationsTitle;
    TextView notificationSelectedText;

    RelativeLayout messageSoundLayout;
    TextView messageSoundText;
    View dividerMessageSoundLayout;

    RelativeLayout ringtoneLayout;
    TextView ringtoneText;
    View dividerRingtoneLayout;

    RelativeLayout clearChatLayout;
    View dividerClearLayout;
    RelativeLayout leaveChatLayout;

    TextView participantsTitle;
    long participantsCount;

    //CHAT PANEL
    private SlidingUpPanelLayout slidingParticipantPanel;
    public TextView titleNameContactChatPanel;
    ImageView stateIcon;
    public TextView titleMailContactChatPanel;
    public FrameLayout contactOutLayout;
    public RoundedImageView contactImageView;
    public TextView contactInitialLetter;
    public LinearLayout contactLayout;
    public LinearLayout optionContactInfoChat;
    public LinearLayout optionStartConversationChat;
    public LinearLayout optionEditProfileChat;
    public LinearLayout optionLeaveChat;
    public LinearLayout optionChangePermissionsChat;
    public LinearLayout optionRemoveParticipantChat;
    private ParticipantPanelListener participantPanelListener;
    ////

    RecyclerView recyclerView;
    MegaParticipantsChatLollipopAdapter adapter;
    ArrayList<MegaChatParticipant> participants;

    Toolbar toolbar;
    ActionBar aB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        log("onCreate");
        groupChatInfoActivity = this;
        if (megaApi == null) {
            MegaApplication app = (MegaApplication) getApplication();
            megaApi = app.getMegaApi();
        }

        if (megaChatApi == null) {
            MegaApplication app = (MegaApplication) getApplication();
            megaChatApi = app.getMegaChatApi();
        }

        display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = getResources().getDisplayMetrics().density;

        scaleW = Util.getScaleW(outMetrics, density);
        scaleH = Util.getScaleH(outMetrics, density);

        if (scaleH < scaleW){
            scaleText = scaleH;
        }
        else{
            scaleText = scaleW;
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatHandle = extras.getLong("handle", -1);
            if (chatHandle == -1) {
                finish();
                return;
            }

            chat = megaChatApi.getChatRoom(chatHandle);

            dbH = DatabaseHandler.getDbHandler(getApplicationContext());
            chatPrefs = dbH.findChatPreferencesByHandle(String.valueOf(chatHandle));

            setContentView(R.layout.activity_group_chat_properties);

            fragmentContainer = (CoordinatorLayout) findViewById(R.id.fragment_container_group_chat);
            toolbar = (Toolbar) findViewById(R.id.toolbar_group_chat_properties);
            setSupportActionBar(toolbar);
            aB = getSupportActionBar();

            aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
            aB.setHomeButtonEnabled(true);
            aB.setDisplayHomeAsUpEnabled(true);

            aB.setTitle(chat.getTitle());

            scrollView = (ScrollView) findViewById(R.id.scroll_view_group_chat_properties);

            initialLetter = (TextView) findViewById(R.id.chat_contact_properties_toolbar_initial_letter);

            float scaleText;
            if (scaleH < scaleW) {
                scaleText = scaleH;
            } else {
                scaleText = scaleW;
            }

            //Info Layout
            avatarImageView = (RoundedImageView) findViewById(R.id.chat_group_properties_thumbnail);
            initialLetter = (TextView) findViewById(R.id.chat_group_properties_initial_letter);

            if (chat.getTitle().length() > 0){
                String chatTitle = chat.getTitle().trim();
                String firstLetter = chatTitle.charAt(0) + "";
                firstLetter = firstLetter.toUpperCase(Locale.getDefault());
                initialLetter.setText(firstLetter);
            }

            createGroupChatAvatar();

            infoLayout = (LinearLayout) findViewById(R.id.chat_group_contact_properties_info_layout);
            infoLayout.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams paramsInfo = (LinearLayout.LayoutParams) infoLayout.getLayoutParams();
            paramsInfo.leftMargin = Util.scaleWidthPx(16, outMetrics);
            paramsInfo.topMargin = Util.scaleHeightPx(2, outMetrics);
            infoLayout.setLayoutParams(paramsInfo);
            avatarLayout = (RelativeLayout) findViewById(R.id.chat_group_properties_avatar_layout);

            infoTextContainerLayout = (RelativeLayout) findViewById(R.id.chat_group_contact_properties_info_text_container);
            LinearLayout.LayoutParams paramsInfoText = (LinearLayout.LayoutParams) infoTextContainerLayout.getLayoutParams();
            paramsInfoText.leftMargin = Util.scaleWidthPx(16, outMetrics);
            infoTextContainerLayout.setLayoutParams(paramsInfoText);

            infoTitleChatText = (TextView) findViewById(R.id.chat_group_contact_properties_info_title);
            infoTitleChatText.setText(chat.getTitle());
            infoNumParticipantsText = (TextView) findViewById(R.id.chat_group_contact_properties_info_participants);
            participantsCount = chat.getPeerCount();
            log("Participants count: "+participantsCount);
            long participantsLabel = participantsCount+1; //Add one to include me
            infoNumParticipantsText.setText(participantsLabel+ " "+ getString(R.string.participants_chat_label));

            editImageView = (ImageView) findViewById(R.id.chat_group_contact_properties_edit_icon);
            RelativeLayout.LayoutParams paramsEditIcon = (RelativeLayout.LayoutParams) editImageView.getLayoutParams();
            paramsEditIcon.leftMargin = Util.scaleWidthPx(15, outMetrics);
            editImageView.setLayoutParams(paramsEditIcon);
            editImageView.setOnClickListener(this);

            //Notifications Layout

            notificationsLayout = (LinearLayout) findViewById(R.id.chat_group_contact_properties_notifications_layout);
            notificationsLayout.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams paramsNotifications = (LinearLayout.LayoutParams) notificationsLayout.getLayoutParams();
            paramsNotifications.leftMargin = Util.scaleWidthPx(72, outMetrics);
            notificationsLayout.setLayoutParams(paramsNotifications);

            notificationsTitle = (TextView) findViewById(R.id.chat_group_contact_properties_notifications_title);
            notificationSelectedText = (TextView) findViewById(R.id.chat_group_contact_properties_notifications_option);

            notificationsSwitch = (Switch) findViewById(R.id.chat_group_contact_properties_switch);
            notificationsSwitch.setOnCheckedChangeListener(this);
            LinearLayout.LayoutParams paramsSwitch = (LinearLayout.LayoutParams) notificationsSwitch.getLayoutParams();
            paramsSwitch.rightMargin = Util.scaleWidthPx(16, outMetrics);
            notificationsSwitch.setLayoutParams(paramsSwitch);

            //Chat message sound Layout

            messageSoundLayout = (RelativeLayout) findViewById(R.id.chat_group_contact_properties_messages_sound_layout);
            messageSoundLayout.setOnClickListener(this);
            LinearLayout.LayoutParams paramsSound = (LinearLayout.LayoutParams) messageSoundLayout.getLayoutParams();
            paramsSound.leftMargin = Util.scaleWidthPx(72, outMetrics);
            messageSoundLayout.setLayoutParams(paramsSound);

            messageSoundText = (TextView) findViewById(R.id.chat_group_contact_properties_messages_sound);

            dividerMessageSoundLayout = (View) findViewById(R.id.divider_message_sound_layout);
            LinearLayout.LayoutParams paramsDividerSound = (LinearLayout.LayoutParams) dividerMessageSoundLayout.getLayoutParams();
            paramsDividerSound.leftMargin = Util.scaleWidthPx(72, outMetrics);
            dividerMessageSoundLayout.setLayoutParams(paramsDividerSound);

            //Call ringtone Layout

            ringtoneLayout = (RelativeLayout) findViewById(R.id.chat_group_contact_properties_ringtone_layout);
            ringtoneLayout.setOnClickListener(this);
            LinearLayout.LayoutParams paramsRingtone = (LinearLayout.LayoutParams) ringtoneLayout.getLayoutParams();
            paramsRingtone.leftMargin = Util.scaleWidthPx(72, outMetrics);
            ringtoneLayout.setLayoutParams(paramsRingtone);

            ringtoneText = (TextView) findViewById(R.id.chat_group_contact_properties_ringtone);

            dividerRingtoneLayout = (View) findViewById(R.id.divider_ringtone_layout);
            LinearLayout.LayoutParams paramsRingtoneDivider = (LinearLayout.LayoutParams) dividerRingtoneLayout.getLayoutParams();
            paramsRingtoneDivider.leftMargin = Util.scaleWidthPx(72, outMetrics);
            dividerRingtoneLayout.setLayoutParams(paramsRingtoneDivider);

            //Clear chat Layout
            clearChatLayout = (RelativeLayout) findViewById(R.id.chat_group_contact_properties_clear_layout);
            clearChatLayout.setOnClickListener(this);
            LinearLayout.LayoutParams paramsClearChat = (LinearLayout.LayoutParams) clearChatLayout.getLayoutParams();
            paramsClearChat.leftMargin = Util.scaleWidthPx(72, outMetrics);
            clearChatLayout.setLayoutParams(paramsClearChat);

            dividerClearLayout = (View) findViewById(R.id.divider_clear_layout);
            LinearLayout.LayoutParams paramsClearDivider = (LinearLayout.LayoutParams) dividerClearLayout.getLayoutParams();
            paramsClearDivider.leftMargin = Util.scaleWidthPx(72, outMetrics);
            dividerClearLayout.setLayoutParams(paramsClearDivider);

            if(chat.getOwnPrivilege()==MegaChatRoom.PRIV_MODERATOR){
                editImageView.setVisibility(View.VISIBLE);
                dividerClearLayout.setVisibility(View.VISIBLE);
                clearChatLayout.setVisibility(View.VISIBLE);
            }
            else{
                editImageView.setVisibility(View.GONE);
                dividerClearLayout.setVisibility(View.GONE);
                clearChatLayout.setVisibility(View.GONE);
            }

            //Leave chat Layout
            leaveChatLayout = (RelativeLayout) findViewById(R.id.chat_group_contact_properties_leave_layout);
            leaveChatLayout.setOnClickListener(this);
            LinearLayout.LayoutParams paramsLeaveChat = (LinearLayout.LayoutParams) leaveChatLayout.getLayoutParams();
            paramsLeaveChat.leftMargin = Util.scaleWidthPx(72, outMetrics);
            leaveChatLayout.setLayoutParams(paramsLeaveChat);

            participantsTitle = (TextView) findViewById(R.id.chat_group_contact_properties_title_text);
            RelativeLayout.LayoutParams paramsPartTitle = (RelativeLayout.LayoutParams) participantsTitle.getLayoutParams();
            paramsPartTitle.leftMargin = Util.scaleWidthPx(72, outMetrics);
            participantsTitle.setLayoutParams(paramsPartTitle);

            recyclerView = (RecyclerView) findViewById(R.id.chat_group_contact_properties_list);
//            recyclerView.setPadding(0, 0, 0, Util.scaleHeightPx(85, outMetrics));
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, outMetrics));
            recyclerView.setHasFixedSize(true);
            MegaLinearLayoutManager linearLayoutManager = new MegaLinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
//            recyclerView.addOnItemTouchListener(this);
            recyclerView.setFocusable(false);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setNestedScrollingEnabled(false);

            participants = new ArrayList<>();

            setParticipants();

            adapter.setPositionClicked(-1);
            recyclerView.setAdapter(adapter);

            //Sliding CHAT panel
            slidingParticipantPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout_group_participants_chat);
            titleNameContactChatPanel = (TextView) findViewById(R.id.group_participants_chat_name_text);
            stateIcon = (ImageView) findViewById(R.id.group_participants_state_circle);

            stateIcon.setVisibility(View.VISIBLE);

            stateIcon.setMaxWidth(Util.scaleWidthPx(6,outMetrics));
            stateIcon.setMaxHeight(Util.scaleHeightPx(6,outMetrics));

            RelativeLayout.LayoutParams stateIconParams = (RelativeLayout.LayoutParams)stateIcon.getLayoutParams();
            stateIconParams.setMargins(Util.scaleWidthPx(6, outMetrics), Util.scaleHeightPx(4, outMetrics), 0, 0);
            stateIcon.setLayoutParams(stateIconParams);

            titleMailContactChatPanel = (TextView) findViewById(R.id.group_participants_chat_mail_text);
            contactLayout = (LinearLayout) findViewById(R.id.group_participants_chat);
            contactImageView = (RoundedImageView) findViewById(R.id.sliding_group_participants_chat_list_thumbnail);
            contactInitialLetter = (TextView) findViewById(R.id.sliding_group_participants_chat_list_initial_letter);
            contactOutLayout = (FrameLayout) findViewById(R.id.out_group_participants_chat);
            optionContactInfoChat = (LinearLayout) findViewById(R.id.contact_info_group_participants_chat_layout);
            optionStartConversationChat = (LinearLayout) findViewById(R.id.start_chat_group_participants_chat_layout);
            optionEditProfileChat = (LinearLayout) findViewById(R.id.edit_profile_group_participants_chat_layout);
            optionLeaveChat = (LinearLayout) findViewById(R.id.leave_group_participants_chat_layout);
            optionChangePermissionsChat = (LinearLayout) findViewById(R.id.change_permissions_group_participants_chat_layout);
            optionRemoveParticipantChat= (LinearLayout) findViewById(R.id.remove_group_participants_chat_layout);

            participantPanelListener = new ParticipantPanelListener(this);

            optionChangePermissionsChat.setOnClickListener(participantPanelListener);
            optionRemoveParticipantChat.setOnClickListener(participantPanelListener);
            optionContactInfoChat.setOnClickListener(participantPanelListener);
            optionStartConversationChat.setOnClickListener(participantPanelListener);
            optionEditProfileChat.setOnClickListener(participantPanelListener);
            optionLeaveChat.setOnClickListener(participantPanelListener);

            contactOutLayout.setOnClickListener(participantPanelListener);

            slidingParticipantPanel.setVisibility(View.INVISIBLE);
            slidingParticipantPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            //////
        }
    }

    public void setParticipants(){
        log("setParticipants");
        //Set the first element = me
        MegaChatParticipant me = new MegaChatParticipant(megaApi.getMyUser().getHandle(), null, null, getString(R.string.chat_me_text), megaApi.getMyUser().getEmail(), chat.getOwnPrivilege(), MegaChatApi.STATUS_ONLINE);
        participants.add(me);

        participantsCount = chat.getPeerCount();
        for(int i=0;i<participantsCount;i++){
            long peerHandle = chat.getPeerHandle(i);
            int peerPrivilege = chat.getPeerPrivilege(i);
//                String participantFirstName = chat.getPeerFirstname(i);
            String participantFirstName = "Pepe";
            String participantLastName = "Botella";

//                chat.getOnlineStatus()
            log("First of the peer: "+participantFirstName);
            log("Last of the peer: "+participantLastName);

            String fullName;

            if (participantFirstName.trim().length() <= 0){
                fullName = participantLastName;
            }
            else{
                fullName = participantFirstName + " " + participantLastName;
            }
            log("FullName of the peer: "+fullName);

            int status = megaChatApi.getUserOnlineStatus(peerHandle);

//                String tempFullName = "";
//                tempFullName = fullName.substring(1);
//                fullName = tempFullName;

            MegaChatParticipant participant = null;
            String userHandleEncoded = MegaApiAndroid.userHandleToBase64(peerHandle);
            MegaUser participantContact = megaApi.getContact(userHandleEncoded);
            if(participantContact!=null){
                participant = new MegaChatParticipant(peerHandle, participantFirstName, participantLastName, fullName, participantContact.getEmail(), peerPrivilege, status);

            }
            else{
                participant = new MegaChatParticipant(peerHandle, participantFirstName, participantLastName, fullName, null, peerPrivilege, status);
            }

            participants.add(participant);
        }

        log("number of participants: "+participants.size());
        if (adapter == null){
            adapter = new MegaParticipantsChatLollipopAdapter(this, participants, recyclerView);
        }
        else{
            adapter.setParticipants(participants);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenuLollipop");

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_group_chat_info, menu);

        addParticipantItem = menu.findItem(R.id.action_add_participants);
        changeTitleItem =menu.findItem(R.id.action_rename);

        int permission = chat.getOwnPrivilege();

        if(permission==MegaChatRoom.PRIV_MODERATOR) {
            addParticipantItem.setVisible(true);
            changeTitleItem.setVisible(true);
        }
        else {
            addParticipantItem.setVisible(false);
            changeTitleItem.setVisible(false);
        }

        log("Call to super onCreateOptionsMenu");
        return super.onCreateOptionsMenu(menu);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_add_participants:{
                //
                chooseAddParticipantDialog();
                break;
            }
            case R.id.action_rename:{
                showRenameGroupDialog();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void chooseAddParticipantDialog(){
        log("chooseAddContactDialog");

        Intent in = new Intent(this, AddContactActivityLollipop.class);
//		in.putExtra("contactType", Constants.CONTACT_TYPE_MEGA);
        in.putExtra("contactType", Constants.CONTACT_TYPE_MEGA);
        startActivityForResult(in, Constants.REQUEST_ADD_PARTICIPANTS);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        log("-------------------onActivityResult "+requestCode + "____" + resultCode);

        if (requestCode == Constants.REQUEST_ADD_PARTICIPANTS && resultCode == RESULT_OK) {
            log("onActivityResult REQUEST_CREATE_CHAT OK");

            if (intent == null) {
                log("Return.....");
                return;
            }

            final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);
            MultipleGroupChatRequestListener multipleListener = null;

            if (contactsData != null) {

                if (contactsData.size() == 1) {
                    MegaUser user = megaApi.getContact(contactsData.get(0));
                    if (user != null) {
                        megaChatApi.inviteToChat(chatHandle, user.getHandle(), MegaChatPeerList.PRIV_STANDARD, this);
                    }
                } else {
                    log("Add multiple participants "+contactsData.size());
                    multipleListener = new MultipleGroupChatRequestListener(this);
                    for (int i = 0; i < contactsData.size(); i++) {
                        MegaUser user = megaApi.getContact(contactsData.get(i));
                        if (user != null) {
                            megaChatApi.inviteToChat(chatHandle, user.getHandle(), MegaChatPeerList.PRIV_STANDARD, multipleListener);
                        }
                    }
                }
            }
        }
        else{
            log("Error onActivityResult: REQUEST_ADD_PARTICIPANTS");
        }
    }

    public void showRemoveParticipantConfirmation (MegaChatParticipant participant, MegaChatRoom chatToChange){
        log("showRemoveParticipantConfirmation");
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:{
                        removeParticipant();
                        break;
                    }

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(groupChatInfoActivity, R.style.AppCompatAlertDialogStyle);
        String message= getResources().getString(R.string.confirmation_remove_chat_contact,participant.getFullName());
        builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();

    }

    public void removeParticipant(){
        log("removeParticipant: ");
        ChatController cC = new ChatController(groupChatInfoActivity);
        cC.removeParticipant(chatHandle, selectedParticipant.getHandle());
        hideParticipantsOptionsPanel();
    }

    public void changeTitle(String title){
        log("changeTitle: "+title);
        ChatController cC = new ChatController(groupChatInfoActivity);
        cC.changeTitle(chatHandle, title);
    }

    public void showChangePermissionsDialog(MegaChatParticipant participant, MegaChatRoom chatToChange){
        //Change permissions

        final MegaChatParticipant participantToChange = participant;

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.change_permissions_dialog, null);

        final CheckedTextView administratorCheck = (CheckedTextView) dialoglayout.findViewById(R.id.change_permissions_dialog_administrator);
        administratorCheck.setText(getString(R.string.file_properties_shared_folder_full_access));
        administratorCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        administratorCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
        ViewGroup.MarginLayoutParams administratorMLP = (ViewGroup.MarginLayoutParams) administratorCheck.getLayoutParams();
        administratorMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        final CheckedTextView memberCheck = (CheckedTextView) dialoglayout.findViewById(R.id.change_permissions_dialog_member);
        memberCheck.setText(getString(R.string.file_properties_shared_folder_read_write));
        memberCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        memberCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
        ViewGroup.MarginLayoutParams memberMLP = (ViewGroup.MarginLayoutParams) memberCheck.getLayoutParams();
        memberMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        final CheckedTextView observerCheck = (CheckedTextView) dialoglayout.findViewById(R.id.change_permissions_dialog_observer);
        observerCheck.setText(getString(R.string.file_properties_shared_folder_read_only));
        observerCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        observerCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
        ViewGroup.MarginLayoutParams observerMLP = (ViewGroup.MarginLayoutParams) observerCheck.getLayoutParams();
        observerMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setView(dialoglayout);

        builder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
        permissionsDialog = builder.create();

        permissionsDialog.show();

        int permission = chatToChange.getPeerPrivilegeByHandle(participantToChange.getHandle());

        if(permission==MegaChatRoom.PRIV_STANDARD) {
            administratorCheck.setChecked(false);
            memberCheck.setChecked(true);
            observerCheck.setChecked(false);
        }
        else if(permission==MegaChatRoom.PRIV_MODERATOR){
            administratorCheck.setChecked(true);
            memberCheck.setChecked(false);
            observerCheck.setChecked(false);
        }
        else{
            administratorCheck.setChecked(false);
            memberCheck.setChecked(false);
            observerCheck.setChecked(true);
        }

        final AlertDialog dialog = permissionsDialog;
        administratorCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changePermissions(MegaChatRoom.PRIV_MODERATOR);
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        memberCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changePermissions(MegaChatRoom.PRIV_STANDARD);
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        observerCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                changePermissions(MegaChatRoom.PRIV_RO);
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        log("Cambio permisos");
    }

    public void changePermissions(int newPermissions){
        log("changePermissions: "+newPermissions);
        ChatController cC = new ChatController(groupChatInfoActivity);
        cC.alterParticipantsPermissions(chatHandle, selectedParticipant.getHandle(), newPermissions);
        hideParticipantsOptionsPanel();
    }

    public void createGroupChatAvatar(){
        log("createGroupChatAvatar()");

        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(getResources().getColor(R.color.lollipop_primary_color));

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        avatarImageView.setImageBitmap(defaultAvatar);

        Display display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;

        int avatarTextSize = getAvatarTextSize(density);
        log("DENSITY: " + density + ":::: " + avatarTextSize);

        initialLetter.setTextColor(Color.WHITE);
        initialLetter.setVisibility(View.VISIBLE);
        initialLetter.setTextSize(24);
    }

    private int getAvatarTextSize (float density){
        float textSize = 0.0f;

        if (density > 3.0){
            textSize = density * (DisplayMetrics.DENSITY_XXXHIGH / 72.0f);
        }
        else if (density > 2.0){
            textSize = density * (DisplayMetrics.DENSITY_XXHIGH / 72.0f);
        }
        else if (density > 1.5){
            textSize = density * (DisplayMetrics.DENSITY_XHIGH / 72.0f);
        }
        else if (density > 1.0){
            textSize = density * (72.0f / DisplayMetrics.DENSITY_HIGH / 72.0f);
        }
        else if (density > 0.75){
            textSize = density * (72.0f / DisplayMetrics.DENSITY_MEDIUM / 72.0f);
        }
        else{
            textSize = density * (72.0f / DisplayMetrics.DENSITY_LOW / 72.0f);
        }

        return (int)textSize;
    }

    public void showParticipantsOptionsPanel(MegaChatParticipant participant){
        log("showParticipantsOptionsPanel");

        if(participant!=null){
            this.selectedParticipant = participant;
        }
        else{
            log("participant is NULL");
            return;
        }

        titleNameContactChatPanel.setText(participant.getFullName());
        int permission = chat.getPeerPrivilegeByHandle(participant.getHandle());

        if(permission==MegaChatRoom.PRIV_STANDARD) {
            titleMailContactChatPanel.setText(getString(R.string.member_permission_label_participants_panel));
        }
        else if(permission==MegaChatRoom.PRIV_MODERATOR){
            titleMailContactChatPanel.setText(getString(R.string.administrator_permission_label_participants_panel));
        }
        else{
            titleMailContactChatPanel.setText(getString(R.string.observer_permission_label_participants_panel));
        }

        int state = participant.getStatus();
        if(state == MegaChatApi.STATUS_ONLINE){
            log("This user is connected: "+chat.getTitle());
            stateIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_status_contact_connected));
        }
        else{
            log("This user status is: "+state+  " " + chat.getTitle());
            stateIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle_status_contact_not_connected));
        }

        if(participant.getHandle() == megaApi.getMyUser().getHandle()){
            log("Participant selected its me");
            optionEditProfileChat.setVisibility(View.VISIBLE);
            optionLeaveChat.setVisibility(View.VISIBLE);
            optionContactInfoChat.setVisibility(View.GONE);
            optionStartConversationChat.setVisibility(View.GONE);
            optionChangePermissionsChat.setVisibility(View.GONE);
            optionRemoveParticipantChat.setVisibility(View.GONE);
        }
        else{
            optionEditProfileChat.setVisibility(View.GONE);
            optionLeaveChat.setVisibility(View.GONE);

            if(participant.getEmail()!=null){
                optionContactInfoChat.setVisibility(View.VISIBLE);
                optionStartConversationChat.setVisibility(View.VISIBLE);
            }
            else{
                optionContactInfoChat.setVisibility(View.GONE);
                optionStartConversationChat.setVisibility(View.GONE);
            }

            log("Other participant selected its me");
            if(chat.getOwnPrivilege()==MegaChatRoom.PRIV_MODERATOR){
                optionChangePermissionsChat.setVisibility(View.VISIBLE);
                optionRemoveParticipantChat.setVisibility(View.VISIBLE);
            }
            else{
                optionChangePermissionsChat.setVisibility(View.GONE);
                optionRemoveParticipantChat.setVisibility(View.GONE);
            }
        }

        addAvatarParticipantPanel(participant);

        slidingParticipantPanel.setVisibility(View.VISIBLE);
//		slidingParticipantPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        slidingParticipantPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void hideParticipantsOptionsPanel(){
        log("hideChatPanel");

        slidingParticipantPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        slidingParticipantPanel.setVisibility(View.GONE);

//        adapter.setPositionClicked(-1);
    }

    public void addAvatarParticipantPanel(MegaChatParticipant participant){

        File avatar = null;
        if(participant.getEmail()!=null){
            log("isContact selected!");

            //Ask for avatar
            if (getExternalCacheDir() != null){
                avatar = new File(getExternalCacheDir().getAbsolutePath(), participant.getEmail() + ".jpg");
            }
            else{
                avatar = new File(getCacheDir().getAbsolutePath(), participant.getEmail() + ".jpg");
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
        }

        log("Set default avatar");
        ////DEfault AVATAR
        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        String userHandleEncoded = MegaApiAndroid.userHandleToBase64(participant.getHandle());
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

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;

//		String fullName;
        String firstLetter = participant.getFullName().charAt(0) + "";
        firstLetter = firstLetter.toUpperCase(Locale.getDefault());
        contactInitialLetter.setText(firstLetter);
        contactInitialLetter.setTextColor(Color.WHITE);
        contactInitialLetter.setVisibility(View.VISIBLE);

        contactInitialLetter.setTextSize(24);
        ////
    }

    public MegaChatParticipant getSelectedParticipant() {
        return selectedParticipant;
    }

    public void setSelectedParticipant(MegaChatParticipant selectedParticipant) {
        this.selectedParticipant = selectedParticipant;
    }

    public MegaChatRoom getChat() {
        return chat;
    }

    public void setChat(MegaChatRoom chat) {
        this.chat = chat;
    }

    @Override
    public void onClick(View view) {

        log("onClick");
        switch (view.getId()) {

            case R.id.chat_group_contact_properties_edit_icon: {
                showRenameGroupDialog();
                break;
            }
            case R.id.chat_group_contact_properties_leave_layout: {
                showConfirmationLeaveChat(chat);
                break;
            }
            case R.id.chat_group_contact_properties_clear_layout: {
                log("Clear chat option");
                showConfirmationClearChat();
                break;
            }
        }

    }

    public void showRenameGroupDialog(){
        log("showRenameGroupDialog");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

        final EditText input = new EditText(this);
        layout.addView(input, params);

        input.setSingleLine();
        input.setHint(chat.getTitle());
        input.setTextColor(getResources().getColor(R.color.text_secondary));
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String title = input.getText().toString();
                    if(title.equals("")||title.isEmpty()){
                        log("input is empty");
                        input.setError(getString(R.string.invalid_string));
                        input.requestFocus();
                    }
                    else {
                        log("action DONE ime - change title");
                        changeTitle(title);
                        changeTitleDialog.dismiss();
                    }
                }
                else{
                    log("other IME" + actionId);
                }
                return false;
            }
        });
        input.setImeActionLabel(getString(R.string.context_rename),EditorInfo.IME_ACTION_DONE);
        builder.setTitle(getString(R.string.context_rename));
        builder.setPositiveButton(getString(R.string.context_rename),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String title = input.getText().toString();
                        if(title.equals("")||title.isEmpty()){
                            log("input is empty");
                            input.setError(getString(R.string.invalid_string));
                            input.requestFocus();
                        }
                        else {
                            log("positive button pressed - change title");
                            changeTitle(title);
                            changeTitleDialog.dismiss();
                        }
                    }
                });


        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.setView(layout);
        changeTitleDialog = builder.create();
        changeTitleDialog.show();

        changeTitleDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("OK BTTN CHANGE");
                String title = input.getText().toString();
                if(title.equals("")||title.isEmpty()){
                    log("input is empty");
                    input.setError(getString(R.string.invalid_string));
                    input.requestFocus();
                }
                else {
                    log("positive button pressed - change title");
                    changeTitle(title);
                    changeTitleDialog.dismiss();
                }
            }
        });

    }

    public void showConfirmationClearChat(){
        log("showConfirmationClearChat");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        log("Clear chat!");
//						megaChatApi.truncateChat(chatHandle, MegaChatHandle.MEGACHAT_INVALID_HANDLE);
                        log("Clear history selected!");
                        ChatController chatC = new ChatController(groupChatInfoActivity);
                        chatC.clearHistory(chat);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        String message= getResources().getString(R.string.confirmation_clear_group_chat);
        builder.setTitle(R.string.title_confirmation_clear_group_chat);
        builder.setMessage(message).setPositiveButton(R.string.general_clear, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    public void showConfirmationLeaveChat (final MegaChatRoom c){
        log("showConfirmationLeaveChat");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE: {
                        ChatController chatC = new ChatController(groupChatInfoActivity);
                        chatC.leaveChat(c);
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.title_confirmation_leave_group_chat));
        String message= getResources().getString(R.string.confirmation_leave_group_chat);
        builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    @Override
    public void onBackPressed() {
        log("onBackPressed");

        if(slidingParticipantPanel.getPanelState()!= SlidingUpPanelLayout.PanelState.HIDDEN||slidingParticipantPanel.getVisibility()==View.VISIBLE){
            log("slidingUploadPanel()!=PanelState.HIDDEN");
            hideParticipantsOptionsPanel();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish CHAT: "+request.getType());

        if(request.getType() == MegaChatRequest.TYPE_UPDATE_PEER_PERMISSIONS){
            log("Permissions changed");
            int index=-1;
            MegaChatParticipant participantToUpdate=null;

            log("Participants count: "+participantsCount);
            for(int i=0;i<participantsCount;i++){

                if(request.getUserHandle()==participants.get(i).getHandle()){
                    participantToUpdate = participants.get(i);

                    participantToUpdate.setPrivilege(request.getPrivilege());
                    index=i;
                    break;
                }
            }

            if(index!=-1&&participantToUpdate!=null){
                participants.set(index,participantToUpdate);
//                adapter.setParticipants(participants);
                adapter.updateParticipant(index, participants);
            }

        }
        else if(request.getType() == MegaChatRequest.TYPE_REMOVE_FROM_CHATROOM){
            log("Remove participant");
            int index=-1;
            MegaChatParticipant participantToUpdate=null;

            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                if(megaApi.getMyUser().getHandle()==request.getUserHandle()){
                    log("I left the chatroom");
                        finish();
                }
                else{
                    log("Participants count: "+participantsCount);
                    for(int i=0;i<participantsCount;i++){

                        if(request.getUserHandle()==participants.get(i).getHandle()){
                            participantToUpdate = participants.get(i);

                            participantToUpdate.setPrivilege(request.getPrivilege());
                            index=i;
                            break;
                        }
                    }

                    if(index!=-1&&participantToUpdate!=null){
                        participants.remove(index);
                        participants.clear();
                        setParticipants();
//                        adapter.removeParticipant(index, participants);
                    }
                    scrollView.invalidate();
                    scrollView.setFillViewport(false);
                }
            }
            else{
                log("EEEERRRRROR WHEN TYPE_REMOVE_FROM_CHATROOM " + e.getErrorString());
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_EDIT_CHATROOM_NAME) {
            log("Change title");
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                if(request.getText()!=null) {
                    log("NEW title: "+request.getText());
                    infoTitleChatText.setText(request.getText());
                    aB.setTitle(request.getText());
                }
            }
            else{
                log("EEEERRRRROR WHEN TYPE_EDIT_CHATROOM_NAME " + e.getErrorString());
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_TRUNCATE_HISTORY){
            log("Truncate history request finish!!!");
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("Ok. Clear history done");
                showSnackbar(getString(R.string.clear_history_success));
            }
            else{
                log("Error clearing history: "+e.getErrorString());
                showSnackbar(getString(R.string.clear_history_error));
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_INVITE_TO_CHATROOM) {
            log("Invite to chatroom request finish!!!");
            if (e.getErrorCode() == MegaChatError.ERROR_OK) {
                log("Ok. Invited");
                showSnackbar(getString(R.string.add_participant_success));
                chat = megaChatApi.getChatRoom(chatHandle);
                participants.clear();
                setParticipants();
                scrollView.invalidate();
                scrollView.setFillViewport(false);
            }
            else if (e.getErrorCode() == -12){
                log("Error inviting ARGS: "+e.getErrorString());
                showSnackbar(getString(R.string.add_participant_error_already_exists));
            }
            else{
                log("Error inviting: "+e.getErrorString()+" "+e.getErrorCode());
                showSnackbar(getString(R.string.add_participant_error));
            }
        }
    }

    public void showSnackbar(String s){
        log("showSnackbar: "+s);
        Snackbar snackbar = Snackbar.make(fragmentContainer, s, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);
        snackbar.show();
    }


    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {

    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

    }

    public static void log(String message) {
        Util.log("GroupChatInfoActivity", message);
    }
}
