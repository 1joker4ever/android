package mega.privacy.android.app.lollipop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconGridFragment;
import io.github.rockerhieu.emojicon.EmojiconsFragment;
import io.github.rockerhieu.emojicon.emoji.Emojicon;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContact;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.MegaLinearLayoutManager;
import mega.privacy.android.app.lollipop.adapters.MegaChatLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.tempMegaChatClasses.ChatRoom;
import mega.privacy.android.app.lollipop.tempMegaChatClasses.Message;
import mega.privacy.android.app.lollipop.tempMegaChatClasses.RecentChat;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.TimeChatUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaChatRoomListener;
import nz.mega.sdk.MegaChatRoomListenerInterface;

public class ChatActivityLollipop extends PinActivityLollipop implements MegaChatRequestListenerInterface, MegaChatRoomListenerInterface, RecyclerView.OnItemTouchListener, GestureDetector.OnGestureListener, View.OnClickListener, EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    Handler handler;

    RecentChat recentChat;
    MegaChatRoom chatRoom;
    long idChat;

    long myUserHandle;

    ActionBar aB;
    Toolbar tB;

    float scaleH, scaleW;
    float density;
    DisplayMetrics outMetrics;
    Display display;

    GestureDetectorCompat detector;

    RelativeLayout fragmentContainer;
    RelativeLayout writingContainerLayout;
    RelativeLayout writingLayout;
    RelativeLayout disabledWritingLayout;
    RelativeLayout chatRelativeLayout;
//    TextView inviteText;
    ImageButton keyboardButton;
    EmojiconEditText textChat;
    ImageButton sendIcon;
    RelativeLayout messagesContainerLayout;
    ScrollView emptyScrollView;

    FloatingActionButton fab;
    FrameLayout emojiKeyboardLayout;

    RecyclerView listView;
    MegaLinearLayoutManager mLayoutManager;

    ChatActivityLollipop chatActivity;
    String myMail;

    RelativeLayout uploadPanel;
    RelativeLayout uploadFromGalleryOption;
    RelativeLayout uploadFromCloudOption;
    RelativeLayout uploadAudioOption;
    RelativeLayout uploadContactOption;
    RelativeLayout uploadFromFilesystemOption;

    MenuItem callMenuItem;
    MenuItem videoMenuItem;
    MenuItem inviteMenuItem;
    MenuItem clearHistoryMenuItem;
    MenuItem contactInfoMenuItem;
    MenuItem leaveMenuItem;

    LinearLayout megaInfoEmptyLayout;
    LinearLayout confidentialityEmptyLayout;
    LinearLayout authenticityEmptyLayout;
    TextView megaInfoTextView;
    TextView confidentialityTextView;
    TextView authenticityTextView;
    TextView megaInfoTitle;
    TextView authenticityTitle;
    TextView confidentialityTitle;

    int diffMeasure;
    boolean focusChanged=false;

    KeyboardListener keyboardListener;

    String intentAction;
    MegaChatLollipopAdapter adapter;

    MegaContact contactChat;
    String fullName;
    String shortContactName;

    int stateHistory;

    DatabaseHandler dbH = null;

    int keyboardSize = -1;
    int firstSize = -1;

    boolean emojiKeyboardShown = false;
    boolean softKeyboardShown = false;

    ArrayList<MegaChatMessage> messages;
    ArrayList<Integer> infoToShow;

    View.OnFocusChangeListener focus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            log("onFocusChange");
            if(!focusChanged){
                focusChanged = true;
            }
        }
    };

    private ActionMode actionMode;

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(textChat, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(textChat);
    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        public void onLongPress(MotionEvent e) {
            log("onLongPress");
            View view = listView.findChildViewUnder(e.getX(), e.getY());
            int position = listView.getChildLayoutPosition(view);

            // handle long press
            if (!adapter.isMultipleSelect()){
                adapter.setMultipleSelect(true);

                actionMode = startSupportActionMode(new ActionBarCallBack());

                itemClick(position);
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            log("onSingleTapUp");

            if (adapter.isMultipleSelect()){
                View view = listView.findChildViewUnder(e.getX(), e.getY());
                int position = listView.getChildLayoutPosition(view);
                itemClick(position);
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        if (megaApi == null) {
            MegaApplication app = (MegaApplication) getApplication();
            megaApi = app.getMegaApi();
        }

        if (megaChatApi == null) {
            MegaApplication app = (MegaApplication) getApplication();
            megaChatApi = app.getMegaChatApi();
        }

        dbH = DatabaseHandler.getDbHandler(this);

        detector = new GestureDetectorCompat(this, new RecyclerViewOnGestureListener());

        recentChat = new RecentChat();
        chatActivity = this;

        setContentView(R.layout.activity_chat);

        //Set toolbar
        tB = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(tB);
        aB = getSupportActionBar();
//		aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        aB.setDisplayHomeAsUpEnabled(true);
        aB.setDisplayShowHomeEnabled(true);

        display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density  = getResources().getDisplayMetrics().density;
        handler = new Handler();

        aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);

        fragmentContainer = (RelativeLayout) findViewById(R.id.fragment_container_chat);

        writingContainerLayout = (RelativeLayout) findViewById(R.id.writing_container_layout_chat_layout);
        writingLayout = (RelativeLayout) findViewById(R.id.writing_linear_layout_chat);
        disabledWritingLayout = (RelativeLayout) findViewById(R.id.writing_disabled_linear_layout_chat);

        ///Empty screen
        emptyScrollView = (ScrollView) findViewById(R.id.layout_empty_scroll_view);
//        emptyScrollView.setVisibility(View.VISIBLE);

        megaInfoEmptyLayout = (LinearLayout) findViewById(R.id.mega_info_layout);
        RelativeLayout.LayoutParams megaInfoParams = (RelativeLayout.LayoutParams)megaInfoEmptyLayout.getLayoutParams();
        megaInfoParams.setMargins(Util.scaleWidthPx(36, outMetrics), Util.scaleHeightPx(40, outMetrics), Util.scaleWidthPx(36, outMetrics), Util.scaleHeightPx(24, outMetrics));
        megaInfoEmptyLayout.setLayoutParams(megaInfoParams);

        confidentialityEmptyLayout = (LinearLayout) findViewById(R.id.confidentiality_info);
        RelativeLayout.LayoutParams megaConfidentialityParams = (RelativeLayout.LayoutParams)confidentialityEmptyLayout.getLayoutParams();
        megaConfidentialityParams.setMargins(Util.scaleWidthPx(36, outMetrics), 0, Util.scaleWidthPx(36, outMetrics), Util.scaleHeightPx(24, outMetrics));
        confidentialityEmptyLayout.setLayoutParams(megaConfidentialityParams);

        authenticityEmptyLayout = (LinearLayout) findViewById(R.id.authenticity_info);
        RelativeLayout.LayoutParams megaAuthenticityParams = (RelativeLayout.LayoutParams)authenticityEmptyLayout.getLayoutParams();
        megaAuthenticityParams.setMargins(Util.scaleWidthPx(36, outMetrics), 0, Util.scaleWidthPx(36, outMetrics), Util.scaleHeightPx(24, outMetrics));
        authenticityEmptyLayout.setLayoutParams(megaAuthenticityParams);

        megaInfoTitle = (TextView) findViewById(R.id.mega_title);
        RelativeLayout.LayoutParams megaTitleParams = (RelativeLayout.LayoutParams)megaInfoTitle.getLayoutParams();
        megaTitleParams.setMargins(Util.scaleWidthPx(24, outMetrics), 0, 0, 0);
        megaInfoTitle.setLayoutParams(megaTitleParams);

        confidentialityTitle = (TextView) findViewById(R.id.confidentiality_title);
        RelativeLayout.LayoutParams confidentialityTitleParams = (RelativeLayout.LayoutParams)confidentialityTitle.getLayoutParams();
        confidentialityTitleParams.setMargins(Util.scaleWidthPx(24, outMetrics), 0, 0, 0);
        confidentialityTitle.setLayoutParams(confidentialityTitleParams);

        authenticityTitle = (TextView) findViewById(R.id.authenticity_title);
        RelativeLayout.LayoutParams authenticityTitleParams = (RelativeLayout.LayoutParams)authenticityTitle.getLayoutParams();
        authenticityTitleParams.setMargins(Util.scaleWidthPx(24, outMetrics), 0, 0, 0);
        authenticityTitle.setLayoutParams(authenticityTitleParams);

        megaInfoTextView = (TextView) findViewById(R.id.mega_info);
        RelativeLayout.LayoutParams megaTextParams = (RelativeLayout.LayoutParams)megaInfoTextView.getLayoutParams();
        megaTextParams.setMargins(Util.scaleWidthPx(24, outMetrics), 0, 0, 0);
        megaInfoTextView.setLayoutParams(megaTextParams);

        confidentialityTextView = (TextView) findViewById(R.id.confidentiality_text);
        RelativeLayout.LayoutParams confidentialityTextParams = (RelativeLayout.LayoutParams)confidentialityTextView.getLayoutParams();
        confidentialityTextParams.setMargins(Util.scaleWidthPx(24, outMetrics), 0, 0, 0);
        confidentialityTextView.setLayoutParams(confidentialityTextParams);

        authenticityTextView = (TextView) findViewById(R.id.authenticity_text);
        RelativeLayout.LayoutParams authenticityTextParams = (RelativeLayout.LayoutParams)authenticityTextView.getLayoutParams();
        authenticityTextParams.setMargins(Util.scaleWidthPx(24, outMetrics), 0, 0, 0);
        authenticityTextView.setLayoutParams(authenticityTextParams);

        keyboardButton = (ImageButton) findViewById(R.id.keyboard_icon_chat);
        textChat = (EmojiconEditText) findViewById(R.id.edit_text_chat);
        keyboardButton.setOnClickListener(this);

        textChat.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(count>0){
                    sendIcon.setVisibility(View.VISIBLE);
                }
                else{
                    sendIcon.setVisibility(View.GONE);
                }
            }
        });

        textChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(uploadPanel.getVisibility()==View.VISIBLE){
                    hideUploadPanel();
                }
                if (emojiKeyboardShown){
//                    int inputType = textChat.getInputType();
//                    textChat.setInputType(InputType.TYPE_NULL);
//                    textChat.onTouchEvent(event);
//                    textChat.setInputType(inputType);
//
//                    float x = event.getX();
//                    float y = event.getY();
//
//                    int touchPosition = textChat.getOffsetForPosition(x, y);
//                    Toast.makeText(ChatActivityLollipop.this, "X: " + x + "__ Y " + y + "__TOUCHPOSITION: " + touchPosition, Toast.LENGTH_SHORT).show();
//                    if (touchPosition  > 0){
//                        textChat.setSelection(touchPosition);
//                    }
////                    InputMethodManager imm = (InputMethodManager) getSystemService(ChatActivityLollipop.this.INPUT_METHOD_SERVICE);
////                    imm.hideSoftInputFromWindow(textChat.getWindowToken(), 0);
//
//                    return true;

                    removeEmojiconFragment();
                }
                return false;
            }
        });

        chatRelativeLayout  = (RelativeLayout) findViewById(R.id.relative_chat_layout);

        sendIcon = (ImageButton) findViewById(R.id.send_message_icon_chat);
        sendIcon.setOnClickListener(this);
        sendIcon.setVisibility(View.GONE);

        listView = (RecyclerView) findViewById(R.id.messages_chat_list_view);
        listView.setClipToPadding(false);;
        listView.setNestedScrollingEnabled(false);

        mLayoutManager = new MegaLinearLayoutManager(this);
        listView.setLayoutManager(mLayoutManager);
        listView.addOnItemTouchListener(this);

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // Get the first visible item
//                            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                int pos = mLayoutManager.findFirstVisibleItemPosition();
//                if(mLayoutManager.findViewByPosition(pos).getTop()==0 && pos==0){
//                    showSnackbar("loadMoreMessages!!!");
//                }

                if(pos==5){
                    showSnackbar("loadMoreMessages!!!");
                }
            }
        });

        messagesContainerLayout = (RelativeLayout) findViewById(R.id.message_container_chat_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab_chat);
        fab.setOnClickListener(this);

        uploadPanel = (RelativeLayout) findViewById(R.id.upload_panel_chat);
        uploadFromGalleryOption = (RelativeLayout) findViewById(R.id.upload_from_gallery_chat);
        uploadFromGalleryOption.setOnClickListener(this);

        uploadFromCloudOption = (RelativeLayout) findViewById(R.id.upload_from_cloud_chat);
        uploadFromCloudOption.setOnClickListener(this);

        uploadFromFilesystemOption = (RelativeLayout) findViewById(R.id.upload_from_filesystem_chat);
        uploadFromFilesystemOption.setOnClickListener(this);

        uploadAudioOption = (RelativeLayout) findViewById(R.id.upload_audio_chat);
        uploadAudioOption.setOnClickListener(this);

        uploadContactOption = (RelativeLayout) findViewById(R.id.upload_contact_chat);
        uploadContactOption.setOnClickListener(this);

        emojiKeyboardLayout = (FrameLayout) findViewById(R.id.chat_emoji_keyboard);

        ViewTreeObserver viewTreeObserver = fragmentContainer.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    log("onGlobalLayout");
                    InputMethodManager imm = (InputMethodManager) ChatActivityLollipop.this.getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (firstSize == -1){
                        if (messagesContainerLayout != null){
                            firstSize = messagesContainerLayout.getHeight();
                        }
                    }
                    else {
                        if (keyboardSize == -1) {
                            if (imm.isAcceptingText()) {
                                if (messagesContainerLayout != null) {
                                    keyboardSize = firstSize - messagesContainerLayout.getHeight();
                                }
                            }
                        }
                    }

                    if ((firstSize - messagesContainerLayout.getHeight()) > 150 ) { //Every keyboard is at least 180 px height
                        if (!emojiKeyboardShown){
                            softKeyboardShown = true;
                        }
                    }
                    else{
                        softKeyboardShown = false;
                    }

                    if (shouldShowEmojiKeyboard){
                        setEmojiconFragment(false);
                        shouldShowEmojiKeyboard = false;
                    }
                }
            });
        }

        Intent newIntent = getIntent();

        if (newIntent != null){
            intentAction = newIntent.getAction();
            if (intentAction != null){
                if (intentAction.equals(Constants.ACTION_CHAT_NEW)){
                    log("ACTION_CHAT_NEW");
                    long chatId = newIntent.getLongExtra("CHAT_ID", -1);
                    if(chatId!=-1){
                        MegaChatRoom chatRoom = megaChatApi.getChatRoom(chatId);
                        aB.setTitle(chatRoom.getTitle());

		/*
  public final static int STATUS_OFFLINE = 0;
  public final static int STATUS_BUSY = 1;
  public final static int STATUS_AWAY = 2;
  public final static int STATUS_ONLINE = 3;
  public final static int STATUS_CHATTY = 4;*/

                        aB.setSubtitle("Status");
                    }
                    else{
                        log("ChatRoom is -1");
                    }

                    log("I have chat!!!: "+chatId);
                    fab.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    chatRelativeLayout.setVisibility(View.GONE);
                    emptyScrollView.setVisibility(View.VISIBLE);
//                    inviteText.setVisibility(View.VISIBLE);
                    textChat.setOnFocusChangeListener(focus);
                }
                else if (intentAction.equals(Constants.ACTION_CHAT_INVITE)){
                    fab.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    chatRelativeLayout.setVisibility(View.GONE);
                    emptyScrollView.setVisibility(View.VISIBLE);
//                    inviteText.setVisibility(View.VISIBLE);
                    textChat.setOnFocusChangeListener(focus);
                    keyboardListener = new KeyboardListener();
                    emptyScrollView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
                    textChat.setText("Hi there!\nLet's chat!");
                }
                else if (intentAction.equals(Constants.ACTION_CHAT_SHOW_MESSAGES)){
                    log("ACTION_CHAT_SHOW_MESSAGES");
                    fab.setVisibility(View.VISIBLE);

                    idChat = newIntent.getLongExtra("CHAT_ID", -1);
//                    idChat=8179160514871859886L;
                    myMail = megaApi.getMyEmail();
                    myUserHandle = megaApi.getMyUser().getHandle();

                    log("Show empty screen");
                    chatRelativeLayout.setVisibility(View.GONE);
                    emptyScrollView.setVisibility(View.VISIBLE);

                    if(idChat!=-1){
                        //REcover chat
                        log("Recover chat with id: "+idChat);

                        chatRoom = megaChatApi.getChatRoom(idChat);
                        aB.setTitle(chatRoom.getTitle());
                        aB.setSubtitle("Status");

                        log("Call to open chat");
                        boolean result = megaChatApi.openChatRoom(idChat, this);

                        messages = new ArrayList<MegaChatMessage>();
                        //Prepare data
                        infoToShow = new ArrayList<Integer>();

                        log("El resultado de abrir chat: "+result);
                        log("Start to get Messages!!!");
                        stateHistory = megaChatApi.loadMessages(idChat, 16);

                        String[] words = chatRoom.getTitle().split(" ");
                        shortContactName=words[0];

                        mLayoutManager.setStackFromEnd(true);
                        listView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

    }

    public int compareTime(MegaChatMessage message, MegaChatMessage previous){

        if(previous!=null){

            Calendar cal = Util.calculateDateFromTimestamp(message.getTimestamp());
            Calendar previousCal =  Util.calculateDateFromTimestamp(previous.getTimestamp());

            TimeChatUtils tc = new TimeChatUtils(TimeChatUtils.TIME);

            int result = tc.compare(cal, previousCal);
            log("RESULTS compareTime: "+result);
            return result;
        }
        else{
            log("return -1");
            return -1;
        }
    }

    public int compareDate(MegaChatMessage message, MegaChatMessage previous){

        if(previous!=null){
            Calendar cal = Util.calculateDateFromTimestamp(message.getTimestamp());
            Calendar previousCal =  Util.calculateDateFromTimestamp(previous.getTimestamp());

            TimeChatUtils tc = new TimeChatUtils(TimeChatUtils.DATE);

            int result = tc.compare(cal, previousCal);
            log("RESULTS compareDate: "+result);
            return result;
        }
        else{
            log("return -1");
            return -1;
        }
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    EmojiconsFragment emojiconsFragment = null;
    boolean firstTimeEmoji = true;
    boolean shouldShowEmojiKeyboard = false;

    private void setEmojiconFragment(boolean useSystemDefault) {
        log("setEmojiconFragment(" + useSystemDefault + ")");
        if (firstTimeEmoji) {
            emojiconsFragment = EmojiconsFragment.newInstance(useSystemDefault);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.chat_emoji_keyboard, emojiconsFragment)
                    .commitNow();
            firstTimeEmoji = false;
        }

        if (keyboardSize != -1) {
            if (keyboardSize == 0){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) emojiKeyboardLayout.getLayoutParams();
                params.height = 660;
                emojiKeyboardLayout.setLayoutParams(params);
            }
            else {
                if (emojiKeyboardLayout != null) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) emojiKeyboardLayout.getLayoutParams();
                    params.height = keyboardSize;
                    emojiKeyboardLayout.setLayoutParams(params);
                }
            }
        }
        else{
            if (emojiKeyboardLayout != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) emojiKeyboardLayout.getLayoutParams();
                params.height = 660;
                emojiKeyboardLayout.setLayoutParams(params);
            }
        }
        emojiKeyboardShown = true;
    }

    private void removeEmojiconFragment(){
        log("removeEmojiconFragment");
        if (emojiconsFragment != null){
//            getSupportFragmentManager().beginTransaction().remove(emojiconsFragment).commitNow();

            if (emojiKeyboardLayout != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) emojiKeyboardLayout.getLayoutParams();
                params.height = 0;
                emojiKeyboardLayout.setLayoutParams(params);
            }
        }
        emojiKeyboardShown = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenuLollipop");

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_action, menu);

        callMenuItem = menu.findItem(R.id.cab_menu_call_chat);
        videoMenuItem = menu.findItem(R.id.cab_menu_video_chat);
        inviteMenuItem = menu.findItem(R.id.cab_menu_invite_chat);
        clearHistoryMenuItem = menu.findItem(R.id.cab_menu_clear_history_chat);
        contactInfoMenuItem = menu.findItem(R.id.cab_menu_contact_info_chat);
        leaveMenuItem = menu.findItem(R.id.cab_menu_leave_chat);

        if(chatRoom!=null){
            if(chatRoom.isGroup()){
                contactInfoMenuItem.setVisible(false);
                clearHistoryMenuItem.setVisible(false);
                leaveMenuItem.setVisible(true);
            }
            else{
                contactInfoMenuItem.setVisible(true);
                clearHistoryMenuItem.setVisible(true);
                leaveMenuItem.setVisible(false);
            }
        }
        else{
            log("Chatroom NULL on create menu");
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.cab_menu_call_chat:{

                break;
            }
            case R.id.cab_menu_video_chat:{

                break;
            }
            case R.id.cab_menu_invite_chat:{

                break;
            }
            case R.id.cab_menu_contact_info_chat:{
                break;
            }
            case R.id.cab_menu_clear_history_chat:{
                log("Clear history selected!");
                ChatController chatC = new ChatController(this);
                chatC.clearHistory(chatRoom);
                break;
            }
            case R.id.cab_menu_leave_chat:{
                log("Leave selected!");
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        log("onBackPressedLollipop");

        if(uploadPanel.getVisibility()==View.VISIBLE){
            hideUploadPanel();
            return;
        }

        if (emojiKeyboardShown) {
            removeEmojiconFragment();
        }
        else{
            finish();
        }
    }

    public static void log(String message) {
        Util.log("ChatActivityLollipop", message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home:{
                emptyScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardListener);
                break;
            }
			case R.id.send_message_icon_chat:{
                log("click on Send message");
                emptyScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(keyboardListener);

                writingLayout.setClickable(false);
                String text = textChat.getText().toString();

                sendMessage(text);
                log("Send message: "+text);
                textChat.getText().clear();

//                textChat.setFocusable(false);
//                textChat.setEnabled(false);
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) disabledWritingLayout.getLayoutParams();
//                params.height = writingLayout.getHeight();
//                disabledWritingLayout.setLayoutParams(params);
//                disabledWritingLayout.setVisibility(View.VISIBLE);

//                inviteText.setVisibility(View.GONE);
                break;
			}
            case R.id.keyboard_icon_chat:{
                log("open emoji keyboard:  " + emojiKeyboardShown);

                if (emojiKeyboardShown){
                    removeEmojiconFragment();
                    textChat.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textChat, InputMethodManager.SHOW_IMPLICIT);
                }
                else{
                    InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);

                    if (softKeyboardShown){
                        log("imm.isAcceptingText()");
                        imm.hideSoftInputFromWindow(textChat.getWindowToken(), 0);
                        shouldShowEmojiKeyboard = true;
                    }
                    else{
                        setEmojiconFragment(false);
                    }
                }


//                editText.requestFocus();
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

//                InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

//                Intent intent = new Intent(this, KeyboardActivityLollipop.class);
//                this.startActivity(intent);

                break;
            }
            case R.id.fab_chat:{
                showUploadPanel();
                break;
            }
            case R.id.upload_from_gallery_chat:{
                hideUploadPanel();
                break;
            }
            case R.id.upload_from_cloud_chat:{
                hideUploadPanel();
                break;
            }
            case R.id.upload_audio_chat:{
                hideUploadPanel();
                break;
            }
            case R.id.upload_contact_chat:{
                hideUploadPanel();
                break;
            }
            case R.id.upload_from_filesystem_chat:{
                hideUploadPanel();
                break;
            }
		}
    }

    public void sendMessage(String text){
        log("sendMessage: "+text);

        MegaChatMessage msgSent = megaChatApi.sendMessage(idChat, text);
        if(msgSent!=null){
            log("Mensaje enviado con id temp: "+msgSent.getTempId());
            messages.add(msgSent);
            int index = messages.size()-1;

            if(index==0){
                //First element
                log("First element!");
                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
            }
            else{
                //Not first element
                MegaChatMessage previousMessage = messages.get(index-1);
                log("previous message: "+previousMessage.getContent());
                if(previousMessage.getUserHandle()==myUserHandle) {
                    //The last two messages are mine
                    if(compareDate(msgSent, previousMessage)==0){
                        //Same date
                        if(compareTime(msgSent, previousMessage)==0){
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_NOTHING);
                        }
                        else{
                            //Different minute
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
                        }
                    }
                    else{
                        //Different date
                        infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                    }
                }
                else{
                    //The last message is mine, the previous not
                    if(compareDate(msgSent, previousMessage)==0){
                        infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
                    }
                    else{
                        //Different date
                        infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                    }
                }
            }

            adapter.setMessages(messages, infoToShow);
        }
        else{
            log("Error al enviar mensaje!");
        }


    }

    public void showUploadPanel(){
        fab.setVisibility(View.GONE);
        uploadPanel.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messagesContainerLayout.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, R.id.upload_panel_chat);
        messagesContainerLayout.setLayoutParams(params);
    }

    public void hideUploadPanel(){
        fab.setVisibility(View.VISIBLE);
        uploadPanel.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messagesContainerLayout.getLayoutParams();
        params.addRule(RelativeLayout.ABOVE, R.id.writing_container_layout_chat_layout);
        messagesContainerLayout.setLayoutParams(params);
    }

    /////Multiselect/////
    private class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            List<MegaChatMessage> chats = adapter.getSelectedMessages();

            switch(item.getItemId()){
                case R.id.cab_menu_select_all:{
                    selectAll();
                    actionMode.invalidate();
                    break;
                }
                case R.id.cab_menu_unselect_all:{
                    clearSelections();
                    hideMultipleSelect();
                    actionMode.invalidate();
                    break;
                }
                case R.id.cab_menu_copy:{
                    clearSelections();
                    hideMultipleSelect();
                    //Archive
                    Toast.makeText(chatActivity, "Copy: "+chats.size()+" chats",Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.cab_menu_delete:{
                    clearSelections();
                    hideMultipleSelect();
                    //Delete
                    Toast.makeText(chatActivity, "Delete: "+chats.size()+" chats",Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.messages_chat_action, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            log("onDEstroyActionMode");
            adapter.setMultipleSelect(false);
            clearSelections();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            List<MegaChatMessage> selected = adapter.getSelectedMessages();

            if (selected.size() != 0) {
                menu.findItem(R.id.cab_menu_mute).setVisible(true);
                menu.findItem(R.id.cab_menu_archive).setVisible(true);
                menu.findItem(R.id.cab_menu_delete).setVisible(true);

                MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);
                if(selected.size()==adapter.getItemCount()){
                    menu.findItem(R.id.cab_menu_select_all).setVisible(false);
                    unselect.setTitle(getString(R.string.action_unselect_all));
                    unselect.setVisible(true);
                }
                else if(selected.size()==1){
                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                    unselect.setTitle(getString(R.string.action_unselect_one));
                    unselect.setVisible(true);
                }
                else{
                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                    unselect.setTitle(getString(R.string.action_unselect_all));
                    unselect.setVisible(true);
                }

            }
            else{
                menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
            }

            return false;
        }

    }

    public boolean showSelectMenuItem(){
        if (adapter != null){
            return adapter.isMultipleSelect();
        }

        return false;
    }

    /*
     * Clear all selected items
     */
    private void clearSelections() {
        if(adapter.isMultipleSelect()){
            adapter.clearSelections();
        }
        updateActionModeTitle();
    }

    private void updateActionModeTitle() {
//        if (actionMode == null || getActivity() == null) {
//            return;
//        }
        List<MegaChatMessage> messages = adapter.getSelectedMessages();

        actionMode.setTitle(getString(R.string.selected_items, messages.size()));

        try {
            actionMode.invalidate();
        } catch (NullPointerException e) {
            e.printStackTrace();
            log("oninvalidate error");
        }
    }

    /*
     * Disable selection
     */
    void hideMultipleSelect() {
        log("hideMultipleSelect");
        adapter.setMultipleSelect(false);
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public void selectAll() {
        if (adapter != null) {
            if (adapter.isMultipleSelect()) {
                adapter.selectAll();
            } else {
                adapter.setMultipleSelect(true);
                adapter.selectAll();

                actionMode = startSupportActionMode(new ActionBarCallBack());
            }

            updateActionModeTitle();
        }
    }

    public void itemClick(int position) {
        log("itemClick: "+position);
        if (adapter.isMultipleSelect()){
            adapter.toggleSelection(position);
            List<MegaChatMessage> messages = adapter.getSelectedMessages();
            if (messages.size() > 0){
                updateActionModeTitle();
//                adapter.notifyDataSetChanged();
            }
            else{
                hideMultipleSelect();
            }
        }
//        else{
//            log("open chat one to one");
//            Intent intent = new Intent(this, ChatActivityLollipop.class);
//            intent.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
//            String myMail = ((ManagerActivityLollipop) context).getMyAccountInfo().getMyUser().getEmail();
//            intent.putExtra("CHAT_ID", position);
//            intent.putExtra("MY_MAIL", myMail);
//            this.startActivity(intent);
//        }
    }
    /////END Multiselect/////

    private class KeyboardListener implements ViewTreeObserver.OnGlobalLayoutListener{
        @Override
        public void onGlobalLayout() {

//            if(!focusChanged){
//                diffMeasure = emptyScrollView.getHeight();
//                log("Store Scroll height: "+emptyScrollView.getHeight());
//                RelativeLayout.LayoutParams inviteTextViewParams = (RelativeLayout.LayoutParams)inviteText.getLayoutParams();
//                inviteTextViewParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(150, outMetrics), Util.scaleWidthPx(56, outMetrics), 0);
//                inviteText.setLayoutParams(inviteTextViewParams);
//            }
//            else{
//                int newMeasure = emptyScrollView.getHeight();
//                log("New Scroll height: "+emptyScrollView.getHeight());
//                if(newMeasure < (diffMeasure-200)){
//                    log("Keyboard shown!!!");
//                    RelativeLayout.LayoutParams inviteTextViewParams = (RelativeLayout.LayoutParams)inviteText.getLayoutParams();
//                    inviteTextViewParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(56, outMetrics), 0);
//                    inviteText.setLayoutParams(inviteTextViewParams);
//                }
//                else{
//                    log("Keyboard hidden!!!");
//                    RelativeLayout.LayoutParams inviteTextViewParams = (RelativeLayout.LayoutParams)inviteText.getLayoutParams();
//                    inviteTextViewParams.setMargins(Util.scaleWidthPx(43, outMetrics), Util.scaleHeightPx(150, outMetrics), Util.scaleWidthPx(56, outMetrics), 0);
//                    inviteText.setLayoutParams(inviteTextViewParams);
//                }
//            }
        }
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        detector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public String getMyMail() {
        return myMail;
    }

    public void setMyMail(String myMail) {
        this.myMail = myMail;
    }

    public String getShortContactName() {
        return shortContactName;
    }

    public void setShortContactName(String shortContactName) {
        this.shortContactName = shortContactName;
    }

    @Override
    public void onChatRoomUpdate(MegaChatApiJava api, MegaChatRoom chat) {
        log("onChatRoomUpdate!");
    }

    @Override
    public void onMessageLoaded(MegaChatApiJava api, MegaChatMessage msg) {
        log("onMessageLoaded!");

        log("Show messages screen");
        chatRelativeLayout.setVisibility(View.VISIBLE);
        emptyScrollView.setVisibility(View.GONE);

        messages.add(msg);

//        Collections.sort(messages,  new Comparator<MegaChatMessage>() {
//            @Override
//            public int compare(MegaChatMessage m1, MegaChatMessage m2) {
//                return m1.getMsgIndex() - m2.getMsgIndex();
//            }
//        });

        infoToShow.clear();
        ListIterator<MegaChatMessage> itr = messages.listIterator();
        while (itr.hasNext()) {
            MegaChatMessage messageToShow = itr.next();
            if(messageToShow.getUserHandle()==myUserHandle) {
                log("MY message!!: "+messageToShow.getContent());
                if(itr.nextIndex()==1){
                    //First element
                    infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                }
                else{
                    //Not first element
                    MegaChatMessage previousMessage = messages.get(itr.previousIndex()-1);
                    log("previous message: "+previousMessage.getContent());
                    if(previousMessage.getUserHandle()==myUserHandle) {
                        log("Last message and previous is mine");
                        //The last two messages are mine
                        if(compareDate(messageToShow, previousMessage)==0){
                            //Same date
                            if(compareTime(messageToShow, previousMessage)==0){
                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_NOTHING);
                            }
                            else{
                                //Different minute
                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
                            }
                        }
                        else{
                            //Different date
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                        }
                    }
                    else{
                        //The last message is mine, the previous not
                        log("Last message is mine, NOT previous");
                        if(compareDate(messageToShow, previousMessage)==0){
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
                        }
                        else{
                            //Different date
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                        }
                    }
                }
            }
            else {
                log("NOT MY message!! - CONTACT");

                if(itr.nextIndex()==1){
                    //First element
                    infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                }
                else{
                    //Not first element
                    MegaChatMessage previousMessage = messages.get(itr.previousIndex()-1);
                    log("previous message: "+previousMessage.getContent());
                    if(previousMessage.getUserHandle()==messageToShow.getUserHandle()) {
                        //The last message is also a contact's message
                        if(compareDate(messageToShow, previousMessage)==0){
                            //Same date
                            if(compareTime(messageToShow, previousMessage)==0){
                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_NOTHING);
                            }
                            else{
                                //Different minute
                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
                            }
                        }
                        else{
                            //Different date
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                        }
                    }
                    else{
                        //The last message is from contact, the previous not
                        if(compareDate(messageToShow, previousMessage)==0){
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
                        }
                        else{
                            //Different date
                            infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
                        }
                    }
                }
            }
//            log("Index: "+ messageToShow.getMsgIndex() + " Message: "+messageToShow.getContent());
        }

        //Create adapter
        if(adapter==null){
            adapter = new MegaChatLollipopAdapter(this, messages, infoToShow, listView);
            listView.setLayoutManager(mLayoutManager);
            listView.setAdapter(adapter);
            adapter.setMessages(messages, infoToShow);
//            adapter.setPositionClicked(-1);
        }
        else{
//            adapter.setPositionClicked(-1);
//            adapter.addMessage(messages, infoToShow);
            adapter.setMessages(messages, infoToShow);
//            adapter.notifyDataSetChanged();
            log("addMesagge: "+messages.size());
        }
    }

    @Override
    public void onMessageReceived(MegaChatApiJava api, MegaChatMessage msg) {
        log("onMessageReceived!");
    }

    @Override
    public void onMessageUpdate(MegaChatApiJava api, MegaChatMessage msg) {
        log("onMessageUpdate!");
//        if(msg!=null){
//            log(msg.getContent()+" "+msg.getStatus());
//        }
//        int indexToReplace = -1;
//
//        ListIterator<MegaChatMessage> itrReplace = messages.listIterator();
//        while (itrReplace.hasNext()) {
//            MegaChatMessage message = itrReplace.next();
//            if(message.getTempId()==msg.getTempId()){
//                indexToReplace = itrReplace.nextIndex()-1;
//            }
//        }
//
//        if(indexToReplace!=-1){
//            messages.set(indexToReplace, msg);
//
//            Collections.sort(messages,  new Comparator<MegaChatMessage>() {
//                @Override
//                public int compare(MegaChatMessage m1, MegaChatMessage m2) {
//                    return m1.getMsgIndex() - m2.getMsgIndex();
//                }
//            });
//
//            infoToShow.clear();
//            ListIterator<MegaChatMessage> itr = messages.listIterator();
//            while (itr.hasNext()) {
//                MegaChatMessage messageToShow = itr.next();
//                if (messageToShow.getUserHandle() == myUserHandle) {
//                    log("MY message!!");
//                    if (itr.nextIndex() == 1) {
//                        //First element
//                        infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
//                    } else {
//                        //Not first element
//                        MegaChatMessage previousMessage = messages.get(itr.previousIndex() - 1);
//                        log("previous message: " + previousMessage.getContent());
//                        if (previousMessage.getUserHandle() == myUserHandle) {
//                            //The last two messages are mine
//                            if (compareDate(messageToShow, previousMessage) == 0) {
//                                //Same date
//                                if (compareTime(messageToShow, previousMessage) == 0) {
//                                    infoToShow.add(Constants.CHAT_ADAPTER_SHOW_NOTHING);
//                                } else {
//                                    //Different minute
//                                    infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
//                                }
//                            } else {
//                                //Different date
//                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
//                            }
//                        } else {
//                            //The last message is mine, the previous not
//                            if (compareDate(messageToShow, previousMessage) == 0) {
//                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
//                            } else {
//                                //Different date
//                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
//                            }
//                        }
//                    }
//                } else {
//                    log("NOT MY message!! - CONTACT");
//
//                    if (itr.nextIndex() == 1) {
//                        //First element
//                        infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
//                    } else {
//                        //Not first element
//                        MegaChatMessage previousMessage = messages.get(itr.previousIndex() - 1);
//                        log("previous message: " + previousMessage.getContent());
//                        if (previousMessage.getUserHandle() == messageToShow.getUserHandle()) {
//                            //The last message is also a contact's message
//                            if (compareDate(messageToShow, previousMessage) == 0) {
//                                //Same date
//                                if (compareTime(messageToShow, previousMessage) == 0) {
//                                    infoToShow.add(Constants.CHAT_ADAPTER_SHOW_NOTHING);
//                                } else {
//                                    //Different minute
//                                    infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
//                                }
//                            } else {
//                                //Different date
//                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
//                            }
//                        } else {
//                            //The last message is from contact, the previous not
//                            if (compareDate(messageToShow, previousMessage) == 0) {
//                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_TIME);
//                            } else {
//                                //Different date
//                                infoToShow.add(Constants.CHAT_ADAPTER_SHOW_ALL);
//                            }
//                        }
//                    }
//                }
//
//                //Create adapter
//                if (adapter == null) {
//                    adapter = new MegaChatLollipopAdapter(this, messages, infoToShow, listView);
//                    listView.setAdapter(adapter);
//                    adapter.setPositionClicked(-1);
//                } else {
//                    adapter.setPositionClicked(-1);
//                    adapter.setMessages(messages, infoToShow);
//                }
//            }

//        }
//        else{
//            log("Error, id temp message not found!!");
//        }

    }

    public void showSnackbar(String s){
        log("showSnackbar: "+s);
        Snackbar snackbar = Snackbar.make(fragmentContainer, s, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);
        snackbar.show();
    }


    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish");

        if(request.getType() == MegaChatRequest.TYPE_TRUNCATE_HISTORY){
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
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestTemporaryError");
    }

    @Override
    protected void onDestroy(){
        log("onDestroy()");

        megaChatApi.closeChatRoom(idChat, this);

        super.onDestroy();
    }
}