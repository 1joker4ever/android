package mega.privacy.android.app.lollipop.megachat.calls;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.OnSwipeTouchListener;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.components.CustomizedGridCallRecyclerView;
import mega.privacy.android.app.lollipop.LoginActivityLollipop;
import mega.privacy.android.app.lollipop.listeners.UserAvatarListener;
import mega.privacy.android.app.lollipop.megachat.ChatItemPreferences;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.GroupCallAdapter;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatCallListenerInterface;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaChatSession;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;
import android.provider.CallLog;

import static android.provider.Settings.System.DEFAULT_RINGTONE_URI;
import static android.view.View.GONE;
import static mega.privacy.android.app.utils.Util.brandAlertDialog;
import static mega.privacy.android.app.utils.Util.context;

public class ChatCallActivity extends AppCompatActivity implements MegaChatRequestListenerInterface, MegaChatCallListenerInterface, MegaRequestListenerInterface, View.OnClickListener, SensorEventListener, KeyEvent.Callback {

    DatabaseHandler dbH = null;
    ChatItemPreferences chatPrefs = null;
    MegaUser myUser;
    Chronometer myChrono;
    int actionBarHeight = 0;

    public static int REMOTE_VIDEO_NOT_INIT = -1;
    public static int REMOTE_VIDEO_ENABLED = 1;
    public static int REMOTE_VIDEO_DISABLED = 0;

    float widthScreenPX, heightScreenPX;

    // flagMyAvatar if true - small avatar circle is contact's avatar
    // flagMyAvatar if false - small avatar circle is my avatar
    boolean flagMyAvatar;

    // flagContactAvatar if true - big avatar circle is contact's avatar
    // flagContactAvatar if false - big avatar circle is my avatar
    boolean flagContactAvatar;

    long chatId;
    MegaChatRoom chat;
    MegaChatCall callChat;
    private MegaApiAndroid megaApi = null;
    MegaChatApiAndroid megaChatApi = null;
    private Handler handlerArrow1, handlerArrow2, handlerArrow3, handlerArrow4, handlerArrow5, handlerArrow6;
    Display display;
    DisplayMetrics outMetrics;
    float density;
    float scaleW;
    float scaleH;
    AppBarLayout appBarLayout;
    Toolbar tB;
    LinearLayout toolbarElements;
    TextView titleToolbar;
    TextView subtitleToobar;
    LinearLayout linearParticipants;
    TextView participantText;
    TextView infoUsersBar;

    ActionBar aB;
    boolean avatarRequested = false;

    ArrayList<InfoPeerGroupCall> peersOnCall = new ArrayList<>();
    ArrayList<InfoPeerGroupCall> peersBeforeCall = new ArrayList<>();

    Timer timer = null;
    Timer ringerTimer = null;
    long milliseconds = 0;

    RelativeLayout smallElementsIndividualCallLayout;
    RelativeLayout bigElementsIndividualCallLayout;
    RelativeLayout bigElementsGroupCallLayout;

    RelativeLayout recyclerViewLayout;
    CustomizedGridCallRecyclerView recyclerView;

    RelativeLayout bigRecyclerViewLayout;
    LinearLayoutManager layoutManager;
    RecyclerView bigRecyclerView;

    GroupCallAdapter adapterGrid;
    GroupCallAdapter adapterList;

    int isRemoteVideo = REMOTE_VIDEO_NOT_INIT;

    Ringtone ringtone = null;
    Vibrator vibrator = null;
    ToneGenerator toneGenerator = null;

    //my avatar
    RelativeLayout myAvatarLayout;
    RoundedImageView myImage;
    TextView myInitialLetter;

    //contact avatar
    RelativeLayout contactAvatarLayout;
    RoundedImageView contactImage;
    TextView contactInitialLetter;

    static ChatCallActivity chatCallActivityActivity = null;

    private MenuItem remoteAudioIcon;

    FloatingActionButton videoFAB;
    FloatingActionButton microFAB;
    FloatingActionButton hangFAB;
    FloatingActionButton answerCallFAB;
    boolean isNecessaryCreateAdapter = true;

    AudioManager audioManager;
    MediaPlayer thePlayer;

    FrameLayout fragmentContainerLocalCamera;
    FrameLayout fragmentContainerLocalCameraFS;
    FrameLayout fragmentContainerRemoteCameraFS;

    ViewGroup parentLocal;
    ViewGroup parentLocalFS;
    ViewGroup parentRemoteFS;

    private LocalCameraCallFragment localCameraFragment = null;
    private LocalCameraCallFullScreenFragment localCameraFragmentFS = null;
    private RemoteCameraCallFullScreenFragment remoteCameraFragmentFS = null;

    //Big elements for group call (more than 6 users)
    FrameLayout fragmentBigCameraGroupCall;
    ImageView microFragmentBigCameraGroupCall;
    ViewGroup parentBigCameraGroupCall;
    private BigCameraGroupCallFragment bigCameraGroupCallFragment = null;
    RelativeLayout avatarBigCameraGroupCallLayout;
    ImageView avatarBigCameraGroupCallMicro;
    RoundedImageView avatarBigCameraGroupCallImage;
    TextView avatarBigCameraGroupCallInitialLetter;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;

    AppRTCAudioManager rtcAudioManager;

    Animation shake;

    long translationAnimationDuration = 500;
    long alphaAnimationDuration = 600;
    long alphaAnimationDurationArrow = 1000;

    LinearLayout linearFAB;
    RelativeLayout relativeCall;
    LinearLayout linearArrowCall;
    ImageView firstArrowCall;
    ImageView secondArrowCall;
    ImageView thirdArrowCall;
    ImageView fourArrowCall;

    RelativeLayout relativeVideo;
    LinearLayout linearArrowVideo;
    ImageView firstArrowVideo;
    ImageView secondArrowVideo;
    ImageView thirdArrowVideo;
    ImageView fourArrowVideo;

    long startTime, timeInMilliseconds = 0;
    Handler customHandler = new Handler();

    InfoPeerGroupCall peerSelected = null;
    boolean isManualMode = false;
    int statusBarHeight = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_calls_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        log("onPrepareOptionsMenu");

        remoteAudioIcon = menu.findItem(R.id.info_remote_audio);
        if(chat.isGroup()){
            remoteAudioIcon.setVisible(false);
        }else{
            MegaChatSession userSession = callChat.getMegaChatSession(chat.getPeerHandle(0));
            if(callChat!=null && userSession!=null){
                if(userSession.hasAudio()){
                    log("Audio remote connected");
                    remoteAudioIcon.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_up_white));
                }else{
                    log("Audio remote NOT connected");
                    remoteAudioIcon.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume_off_white));
                }
            }else{
                log("callChat is Null");
            }
            remoteAudioIcon.setVisible(true);
            remoteAudioIcon.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected");

        ((MegaApplication) getApplication()).sendSignalPresenceActivity();
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                log("home");
                onBackPressed();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateScreenStatusInProgress(){
        log("updateScreenStatusInProgress() chatId: "+chatId);

        if(chat.isGroup()){
            log("updateScreenStatusInProgress() - group chat - chatId: "+chatId);

            if((peersBeforeCall != null)&&(peersBeforeCall.size() != 0)){
                peersBeforeCall.clear();
            }

            if(peersOnCall!=null){
                if(peersOnCall.size() != 0){
                    boolean changes = false;
                    //Get all participant and check it
                    for (int i = 0; i < callChat.getParticipants().size(); i++) {
                        boolean peerContain = false;
                        Long userHandle = callChat.getParticipants().get(i);
                        for (InfoPeerGroupCall peerOnCall : peersOnCall) {
                            if (peerOnCall.getHandle() == userHandle) {
                                peerContain = true;
                                break;
                            }
                        }
                        if (!peerContain) {
                            if (userHandle != megaChatApi.getMyUserHandle()) {
                                InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle,  chat.getPeerFullnameByHandle(userHandle), false, false, false, true,null);
                                log("updateScreenStatusInProgress() "+userPeer.getName()+" added in peersOnCall");
                                peersOnCall.add((peersOnCall.size() == 0 ? 0:(peersOnCall.size()-1)), userPeer);
                                changes = true;
                            } else {
                                InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(),  megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false, true, null);
                                log("updateScreenStatusInProgress() "+myPeer.getName()+" added in peersOnCall");
                                peersOnCall.add(myPeer);
                                changes = true;
                            }
                        }
                    }
                    for (int i = 0; i < peersOnCall.size(); i++) {
                        boolean peerContained = false;
                        for (int j = 0; j < callChat.getParticipants().size(); j++) {
                            long userHandle = callChat.getParticipants().get(j);
                            if (peersOnCall.get(i).getHandle() == userHandle){
                                peerContained = true;
                                break;
                            }
                        }
                        if (!peerContained) {
                            log("updateScreenStatusInProgress() "+peersOnCall.get(i).getName()+" removed of peersOnCall");
                            peersOnCall.remove(i);
                            changes = true;
                        }
                    }
                    if(changes){
                        updatePeers(true);
                    }
                }else{
                    boolean changes = false;
                    for(int i = 0; i < callChat.getParticipants().size(); i++){
                        long userHandle = callChat.getParticipants().get(i);
                        if(userHandle != megaChatApi.getMyUserHandle()){
                            InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle,  chat.getPeerFullnameByHandle(userHandle), false, false, false, true,null);
                            log("updateScreenStatusInProgress() "+userPeer.getName()+" added in peersOnCall");
                            peersOnCall.add((peersOnCall.size() == 0 ? 0:(peersOnCall.size()-1)), userPeer);
                            changes = true;

                        }else{
                            InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(),  megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false,true,null);
                            log("updateScreenStatusInProgress() "+myPeer.getName()+" added in peersOnCall");
                            peersOnCall.add(myPeer);
                            changes = true;
                        }
                    }
                    if(changes){
                        updatePeers(true);
                    }
                }
            }

            for(int i=0; i<peersOnCall.size(); i++){
                if(peersOnCall.get(i).getHandle() == megaChatApi.getMyUserHandle()){
                    updateLocalVideoStatus();
                    updateLocalAudioStatus();
                }else{
                    updateRemoteVideoStatus(peersOnCall.get(i).getHandle());
                    updateRemoteAudioStatus(peersOnCall.get(i).getHandle());
                }
            }

            updateSubTitleClock();
        }else{
            log("updateScreenStatusInProgress() - individual chat - chatId: "+chatId);
            callChat = megaChatApi.getChatCall(chatId);
            if (callChat != null){
                int callStatus = callChat.getStatus();
                if(callStatus == MegaChatCall.CALL_STATUS_RING_IN){
                    log("updateScreenStatusInProgress() - CALL_STATUS_RING_IN");

                    RelativeLayout.LayoutParams layoutExtend = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    layoutExtend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    layoutExtend.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    linearFAB.setLayoutParams(layoutExtend);
                    linearFAB.requestLayout();
                    linearFAB.setOrientation(LinearLayout.HORIZONTAL);


                    myAvatarLayout.setVisibility(View.VISIBLE);
                    contactAvatarLayout.setVisibility(View.VISIBLE);
                    flagMyAvatar = true;
                    setProfileMyAvatar();
                    flagContactAvatar = false;
                    setProfileContactAvatar();

                }else if(callStatus==MegaChatCall.CALL_STATUS_IN_PROGRESS){
                    log("updateScreenStatusInProgress() - CALL_STATUS_IN_PROGRESS");

                    RelativeLayout.LayoutParams layoutExtend = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutExtend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    layoutExtend.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    linearFAB.setLayoutParams(layoutExtend);
                    linearFAB.requestLayout();
                    linearFAB.setOrientation(LinearLayout.HORIZONTAL);

                    myAvatarLayout.setVisibility(View.VISIBLE);
                    contactAvatarLayout.setVisibility(View.VISIBLE);
                    flagMyAvatar = true;
                    setProfileMyAvatar();
                    flagContactAvatar = false;
                    setProfileContactAvatar();

                }else{
                    log("updateScreenStatusInProgress() - OUTGOING");

                    RelativeLayout.LayoutParams layoutExtend = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutExtend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    layoutExtend.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    linearFAB.setLayoutParams(layoutExtend);
                    linearFAB.requestLayout();
                    linearFAB.setOrientation(LinearLayout.HORIZONTAL);


                    flagMyAvatar = false;
                    setProfileMyAvatar();
                    flagContactAvatar = true;
                    setProfileContactAvatar();
                    myAvatarLayout.setVisibility(View.VISIBLE);
                }
            }
            updateLocalVideoStatus();
            updateLocalAudioStatus();
            updateRemoteVideoStatus(-1);
            updateRemoteAudioStatus(-1);

            updateSubTitleClock();

        }

        stopAudioSignals();
        startClock();
    }


    public void updateSubTitleClock(){
        log("updateSubTitleClock()");
        int sessionStatus = -1;

        if(callChat.getStatus()<=MegaChatCall.CALL_STATUS_RING_IN){
            subtitleToobar.setText(getString(R.string.call_starting));

        }else if(callChat.getStatus()==MegaChatCall.CALL_STATUS_IN_PROGRESS){
            if(chat.isGroup()){
                startClock();
                int totalParticipants = callChat.getNumParticipants() + 1;
                log("update subtitle: "+totalParticipants +" of "+chat.getPeerCount());
                updateSubtitleToolbar();
            } else{
                linearParticipants.setVisibility(GONE);
                MegaChatSession userSession = callChat.getMegaChatSession(chat.getPeerHandle(0));
                if(userSession!=null){
                    sessionStatus = userSession.getStatus();
                    log("sessionStatus: "+sessionStatus);
                    if(sessionStatus==MegaChatSession.SESSION_STATUS_IN_PROGRESS){
                        startClock();
                    }else{
                        subtitleToobar.setText(getString(R.string.chat_connecting));
                    }
                }else{
                    log("Error getting the session of the user");
                    subtitleToobar.setText(null);
                }
            }
        }else{
            subtitleToobar.setText(null);

        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log("onNewIntent()");

        Bundle extras = intent.getExtras();
        log(getIntent().getAction());
        if (extras != null) {
            long newChatId = extras.getLong("chatHandle", -1);
            log("New chat id: "+newChatId);
            if(chatId == newChatId){

                if(chat.isGroup()){
                    callChat = megaChatApi.getChatCall(chatId);
                    log("onNewIntent() - group chat - Its the same call -> STATUS: "+callChat.getStatus());
                    if(callChat.getStatus() ==  MegaChatCall.CALL_STATUS_RING_IN){
                        log("onNewIntent() - group chat - CALL_STATUS_RING_IN");

                        RelativeLayout.LayoutParams layoutExtend = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        layoutExtend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        layoutExtend.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                        linearFAB.setLayoutParams(layoutExtend);
                        linearFAB.requestLayout();
                        linearFAB.setOrientation(LinearLayout.HORIZONTAL);


                        boolean isMe = false;
                        for(int i = 0; i < callChat.getParticipants().size(); i++){
                            long userHandle = callChat.getParticipants().get(i);
                            if (userHandle == megaChatApi.getMyUserHandle()) {
                                isMe = true;
                                break;
                            }
                        }

                        if(!isMe){
                            if(peersBeforeCall!=null){
                                if(peersBeforeCall.size()!=0){
                                    boolean changes = false;
                                    for(int i = 0; i < callChat.getParticipants().size(); i++){
                                        boolean peerContain = false;
                                        long userHandle = callChat.getParticipants().get(i);
                                        for(InfoPeerGroupCall peerBeforeCall: peersBeforeCall){
                                            if(peerBeforeCall.getHandle() == userHandle){
                                                peerContain = true;
                                                break;
                                            }
                                        }
                                        if(!peerContain){
                                            InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle,  chat.getPeerFullnameByHandle(userHandle), false, false, false,true,null);
                                            log("onNewIntent() "+userPeer.getName()+" added in peersBeforeCall");
                                            peersBeforeCall.add((peersBeforeCall.size() == 0 ? 0:(peersBeforeCall.size()-1)), userPeer);
                                            changes = true;
                                        }
                                    }
                                    for(int i=0;i<peersBeforeCall.size();i++){
                                        boolean peerContained = false;
                                        for(int j=0; j<callChat.getParticipants().size();j++){
                                            long userHandle = callChat.getParticipants().get(j);
                                            if(peersBeforeCall.get(i).getHandle() == userHandle){
                                                peerContained = true;
                                            }
                                        }
                                        if(!peerContained){
                                            log("onNewIntent() "+peersBeforeCall.get(i).getName()+" removed of peersBeforeCall");
                                            peersBeforeCall.remove(i);
                                            changes = true;
                                        }
                                    }

                                    if(changes){
                                        updatePeers(false);
                                    }

                                }else{
                                    boolean changes = false;
                                    for(int i=0; i<callChat.getParticipants().size(); i++){
                                        long userHandle = callChat.getParticipants().get(i);
                                        InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle,  chat.getPeerFullnameByHandle(userHandle), false, false, false,true,null);
                                        log("onNewIntent() "+userPeer.getName()+" added in peersBeforeCall");
                                        peersBeforeCall.add((peersBeforeCall.size() == 0 ? 0 : (peersBeforeCall.size() - 1)), userPeer);
                                        changes = true;
                                    }
                                    if(changes){
                                        updatePeers(true);
                                    }
                                }
                            }

                        }
                    }else if(callChat.getStatus() ==  MegaChatCall.CALL_STATUS_IN_PROGRESS){
                        log("onNewIntent() CALL_STATUS_IN_PROGRESS -> participants: "+callChat.getParticipants().size());

                        RelativeLayout.LayoutParams layoutExtend = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutExtend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        layoutExtend.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                        linearFAB.setLayoutParams(layoutExtend);
                        linearFAB.requestLayout();
                        linearFAB.setOrientation(LinearLayout.HORIZONTAL);

                        if(peersOnCall != null){
                            if(peersOnCall.size() != 0){
                                boolean changes = false;
                                //Get all participant and check it
                                for (int i = 0; i < callChat.getParticipants().size(); i++) {
                                    boolean peerContain = false;
                                    long userHandle = callChat.getParticipants().get(i);
                                    for (InfoPeerGroupCall peerOnCall : peersOnCall) {
                                        if (peerOnCall.getHandle() == userHandle) {
                                            peerContain = true;
                                            break;
                                        }
                                    }
                                    if (!peerContain) {
                                        if (userHandle == megaChatApi.getMyUserHandle()) {
                                            InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(), megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false, true,null);
                                            log("onNewIntent() "+myPeer.getName()+" added in peersOnCall");
                                            peersOnCall.add(myPeer);
                                            changes = true;
                                        } else {
                                            InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle, chat.getPeerFullnameByHandle(userHandle), false, false, false, true,null);
                                            log("onNewIntent() "+userPeer.getName()+" added in peersOnCall");
                                            peersOnCall.add((peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1)), userPeer);
                                            changes = true;
                                        }
                                    }
                                }
                                for (int i = 0; i < peersOnCall.size(); i++) {
                                    boolean peerContained = false;
                                    for (int j = 0; j < callChat.getParticipants().size(); j++) {
                                        long userHandle = callChat.getParticipants().get(j);
                                        if (peersOnCall.get(i).getHandle() == userHandle){
                                            peerContained = true;
                                            break;
                                        }
                                    }
                                    if (!peerContained) {
                                        log("onNewIntent() "+peersOnCall.get(i).getName()+" removed of peersOnCall");
                                        peersOnCall.remove(i);
                                        changes = true;
                                    }
                                }
                                if(changes){
                                    updatePeers(true);
                                }

                            }else{
                                boolean changes = false;
                                for(int i=0; i<callChat.getParticipants().size(); i++){
                                    Long userHandle = callChat.getParticipants().get(i);
                                    if (userHandle.equals(megaChatApi.getMyUserHandle())){
                                        InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(), megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false, true,null);
                                        log("onNewIntent() "+myPeer.getName()+" added in peersOnCall");
                                        peersOnCall.add(myPeer);
                                        changes = true;
                                    }else{
                                        InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle, chat.getPeerFullnameByHandle(userHandle), false, false, false,true, null);
                                        log("onNewIntent() "+userPeer.getName()+" added in peersOnCall");
                                        peersOnCall.add((peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1)), userPeer);
                                        changes = true;
                                    }
                                }
                                if(changes){
                                    updatePeers(true);
                                }
                            }
                        }
                    }

                }else {
                    log("onNewIntent() - individual chat - Its the same call");

                }

            }else{
                log("It is not the same: "+newChatId);
                //Check the new call if in progress
                chatId = newChatId;
                chat = megaChatApi.getChatRoom(chatId);
                callChat = megaChatApi.getChatCall(chatId);
                titleToolbar.setText(chat.getTitle());

                updateScreenStatusInProgress();

                log("Start call Service");
                Intent intentService = new Intent(this, CallService.class);
                intentService.putExtra("chatHandle", callChat.getChatid());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(intentService);
                }
                else{
                    this.startService(intentService);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls_chat);

        chatCallActivityActivity = this;

        MegaApplication.setShowPinScreen(true);

        display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = getResources().getDisplayMetrics().density;
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        scaleW = Util.getScaleW(outMetrics, density);
        scaleH = Util.getScaleH(outMetrics, density);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        widthScreenPX = outMetrics.widthPixels;
        heightScreenPX = outMetrics.heightPixels - statusBarHeight;


        MegaApplication app = (MegaApplication) getApplication();
        if (megaApi == null) {
            megaApi = app.getMegaApi();
        }

        log("retryPendingConnections()");
        if (megaApi != null) {
            log("---------retryPendingConnections");
            megaApi.retryPendingConnections();
        }

        if (megaChatApi != null){
            megaChatApi.retryPendingConnections(false, null);
        }

        if (megaChatApi == null) {
            megaChatApi = app.getMegaChatApi();
        }

        if(megaApi==null||megaApi.getRootNode()==null){
            log("Refresh session - sdk");
            Intent intent = new Intent(this, LoginActivityLollipop.class);
            intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }
        if(megaChatApi==null||megaChatApi.getInitState()== MegaChatApi.INIT_ERROR){
            log("Refresh session - karere");
            Intent intent = new Intent(this, LoginActivityLollipop.class);
            intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        megaChatApi.addChatCallListener(this);
        myUser = megaApi.getMyUser();

        dbH = DatabaseHandler.getDbHandler(getApplicationContext());
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        try {
            field = PowerManager.class.getClass().getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        } catch (Throwable ignored) {}

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getLocalClassName());

// Wake screen for Xiaomi devices
//        if (!powerManager.isInteractive()){ // if screen is not already on, turn it on (get wake_lock for 10 seconds)
//            PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MH24_SCREENLOCK");
//            wl.acquire(10000);
//            PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
//            wl_cpu.acquire(10000);
//        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tB = (Toolbar) findViewById(R.id.call_toolbar);
        if (tB == null) {
            log("Tb is Null");
            return;
        }
        tB.setVisibility(View.VISIBLE);
        setSupportActionBar(tB);
        aB = getSupportActionBar();
        aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);
        aB.setTitle(null);
        aB.setSubtitle(null);

        toolbarElements = (LinearLayout) tB.findViewById(R.id.toolbar_elements);
        titleToolbar = (TextView) tB.findViewById(R.id.title_toolbar);
        titleToolbar.setText(" ");
        subtitleToobar = (TextView) tB.findViewById(R.id.subtitle_toolbar);
        linearParticipants = (LinearLayout) tB.findViewById(R.id.ll_participants);
        participantText = (TextView) tB.findViewById(R.id.participants_text);
        linearParticipants.setVisibility(View.GONE);



        myChrono = new Chronometer(context);

        smallElementsIndividualCallLayout = (RelativeLayout) findViewById(R.id.small_elements_individual_call);
        smallElementsIndividualCallLayout.setVisibility(GONE);

        bigElementsIndividualCallLayout = (RelativeLayout) findViewById(R.id.big_elements_individual_call);
        bigElementsIndividualCallLayout.setVisibility(GONE);

        linearFAB = (LinearLayout) findViewById(R.id.linear_buttons);
        RelativeLayout.LayoutParams layoutCompress = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutCompress.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutCompress.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        linearFAB.setLayoutParams(layoutCompress);
        linearFAB.requestLayout();
        linearFAB.setOrientation(LinearLayout.HORIZONTAL);

        infoUsersBar = (TextView) findViewById(R.id.info_users_bar);
        infoUsersBar.setVisibility(GONE);

        isManualMode = false;

        relativeCall = (RelativeLayout) findViewById(R.id.relative_answer_call_fab);
        relativeCall.requestLayout();
        relativeCall.setVisibility(GONE);

        linearArrowCall = (LinearLayout) findViewById(R.id.linear_arrow_call);
        linearArrowCall.setVisibility(GONE);
        firstArrowCall = (ImageView) findViewById(R.id.first_arrow_call);
        secondArrowCall = (ImageView) findViewById(R.id.second_arrow_call);
        thirdArrowCall = (ImageView) findViewById(R.id.third_arrow_call);
        fourArrowCall = (ImageView) findViewById(R.id.four_arrow_call);

        relativeVideo = (RelativeLayout) findViewById(R.id.relative_video_fab);
        relativeVideo.requestLayout();
        relativeVideo.setVisibility(GONE);

        linearArrowVideo = (LinearLayout) findViewById(R.id.linear_arrow_video);
        linearArrowVideo.setVisibility(GONE);
        firstArrowVideo = (ImageView) findViewById(R.id.first_arrow_video);
        secondArrowVideo = (ImageView) findViewById(R.id.second_arrow_video);
        thirdArrowVideo = (ImageView) findViewById(R.id.third_arrow_video);
        fourArrowVideo = (ImageView) findViewById(R.id.four_arrow_video);

        answerCallFAB = (FloatingActionButton) findViewById(R.id.answer_call_fab);
        answerCallFAB.setVisibility(GONE);

        videoFAB = (FloatingActionButton) findViewById(R.id.video_fab);
        videoFAB.setOnClickListener(this);
        videoFAB.setVisibility(GONE);

        microFAB = (FloatingActionButton) findViewById(R.id.micro_fab);
        microFAB.setOnClickListener(this);
        microFAB.setVisibility(GONE);

        hangFAB = (FloatingActionButton) findViewById(R.id.hang_fab);
        hangFAB.setOnClickListener(this);
        hangFAB.setVisibility(GONE);

        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        rtcAudioManager = AppRTCAudioManager.create(getApplicationContext());

        //Cameras in Group call
        bigElementsGroupCallLayout = (RelativeLayout) findViewById(R.id.big_elements_group_call);
        bigElementsGroupCallLayout.setVisibility(GONE);

        //Recycler View for 1-6 peers
        recyclerViewLayout = (RelativeLayout) findViewById(R.id.rl_recycler_view);
        recyclerViewLayout.setVisibility(GONE);
        recyclerView = (CustomizedGridCallRecyclerView) findViewById(R.id.recycler_view_cameras);
        recyclerView.setPadding(0, 0, 0, 0);
//        recyclerView.setClipToPadding(false);
//        recyclerView.setHasFixedSize(true);
        recyclerView.setColumnWidth((int) widthScreenPX);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVisibility(GONE);

        //Big elements group calls
        parentBigCameraGroupCall = (ViewGroup) findViewById(R.id.parent_layout_big_camera_group_call);
        ViewGroup.LayoutParams paramsBigCameraGroupCall = (ViewGroup.LayoutParams) parentBigCameraGroupCall.getLayoutParams();
        if(widthScreenPX<heightScreenPX){
            paramsBigCameraGroupCall.width = (int)widthScreenPX;
            paramsBigCameraGroupCall.height = (int)widthScreenPX;
        }else{
            paramsBigCameraGroupCall.width = (int)heightScreenPX;
            paramsBigCameraGroupCall.height = (int)heightScreenPX;
        }
        parentBigCameraGroupCall.setLayoutParams(paramsBigCameraGroupCall);
        fragmentBigCameraGroupCall = (FrameLayout) findViewById(R.id.fragment_big_camera_group_call);
        fragmentBigCameraGroupCall.setVisibility(View.GONE);
        microFragmentBigCameraGroupCall = (ImageView) findViewById(R.id.micro_fragment_big_camera_group_call);
        microFragmentBigCameraGroupCall.setVisibility(View.GONE);

        avatarBigCameraGroupCallLayout = (RelativeLayout) findViewById(R.id.rl_avatar_big_camera_group_call);
        avatarBigCameraGroupCallMicro = (ImageView) findViewById(R.id.micro_avatar_big_camera_group_call);
        avatarBigCameraGroupCallImage = (RoundedImageView) findViewById(R.id.image_big_camera_group_call);
        avatarBigCameraGroupCallInitialLetter = (TextView) findViewById(R.id.initial_letter_big_camera_group_call);
        avatarBigCameraGroupCallMicro.setVisibility(GONE);
        avatarBigCameraGroupCallLayout.setVisibility(View.GONE);
        parentBigCameraGroupCall.setVisibility(View.GONE);

        //Recycler View for 7-8 peers (because 9-10 without video)
        bigRecyclerViewLayout = (RelativeLayout) findViewById(R.id.rl_big_recycler_view);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bigRecyclerView = (RecyclerView) findViewById(R.id.big_recycler_view_cameras);
        bigRecyclerView.setLayoutManager(layoutManager);
        RelativeLayout.LayoutParams bigRecyclerViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bigRecyclerViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        bigRecyclerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.parent_layout_big_camera_group_call);
        bigRecyclerViewParams.addRule(RelativeLayout.BELOW, 0);
        bigRecyclerViewLayout.setLayoutParams(bigRecyclerViewParams);
        bigRecyclerViewLayout.requestLayout();
        bigRecyclerView.setVisibility(GONE);
        bigRecyclerViewLayout.setVisibility(GONE);

        //Local camera small
        parentLocal = (ViewGroup) findViewById(R.id.parent_layout_local_camera);
        fragmentContainerLocalCamera = (FrameLayout) findViewById(R.id.fragment_container_local_camera);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)fragmentContainerLocalCamera.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        fragmentContainerLocalCamera.setLayoutParams(params);
        fragmentContainerLocalCamera.setOnTouchListener(new OnDragTouchListener(fragmentContainerLocalCamera,parentLocal));
        parentLocal.setVisibility(View.GONE);
        fragmentContainerLocalCamera.setVisibility(View.GONE);

        //Local camera Full Screen
        parentLocalFS = (ViewGroup) findViewById(R.id.parent_layout_local_camera_FS);
        fragmentContainerLocalCameraFS = (FrameLayout) findViewById(R.id.fragment_container_local_cameraFS);
        RelativeLayout.LayoutParams paramsFS = (RelativeLayout.LayoutParams)fragmentContainerLocalCameraFS.getLayoutParams();
        paramsFS.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        paramsFS.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        fragmentContainerLocalCameraFS.setLayoutParams(paramsFS);
        parentLocalFS.setVisibility(View.GONE);
        fragmentContainerLocalCameraFS.setVisibility(View.GONE);

        //Remote camera Full Screen
        parentRemoteFS = (ViewGroup) findViewById(R.id.parent_layout_remote_camera_FS);
        fragmentContainerRemoteCameraFS = (FrameLayout) findViewById(R.id.fragment_container_remote_cameraFS);
        RelativeLayout.LayoutParams paramsRemoteFS = (RelativeLayout.LayoutParams)fragmentContainerRemoteCameraFS.getLayoutParams();
        paramsRemoteFS.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
        paramsRemoteFS.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
        fragmentContainerRemoteCameraFS.setLayoutParams(paramsRemoteFS);
        fragmentContainerRemoteCameraFS.setOnTouchListener(new OnDragTouchListener(fragmentContainerRemoteCameraFS, parentRemoteFS));
        parentRemoteFS.setVisibility(View.GONE);
        fragmentContainerRemoteCameraFS.setVisibility(View.GONE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD){
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            myAvatarLayout = (RelativeLayout) findViewById(R.id.call_chat_my_image_rl);
            myAvatarLayout.setVisibility(View.GONE);
            myImage = (RoundedImageView) findViewById(R.id.call_chat_my_image);
            myInitialLetter = (TextView) findViewById(R.id.call_chat_my_image_initial_letter);

            contactAvatarLayout = (RelativeLayout) findViewById(R.id.call_chat_contact_image_rl);
            contactAvatarLayout.setVisibility(View.GONE);
            contactImage = (RoundedImageView) findViewById(R.id.call_chat_contact_image);
            contactInitialLetter = (TextView) findViewById(R.id.call_chat_contact_image_initial_letter);

            videoFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disable_fab_chat_call)));

            videoFAB.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_video_off));

            //Contact's avatar
            chatId = extras.getLong("chatHandle", -1);
            log("Chat handle to call: " + chatId);
            if (chatId != -1) {
                chat = megaChatApi.getChatRoom(chatId);
                callChat = megaChatApi.getChatCall(chatId);

                if (callChat == null){
                    megaChatApi.removeChatCallListener(this);
                    MegaApplication.activityPaused();
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        super.finishAndRemoveTask();
                    }
                    else {
                        super.finish();
                    }
                    return;
                }

                log("Start call Service");
                Intent intentService = new Intent(this, CallService.class);
                intentService.putExtra("chatHandle", callChat.getChatid());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(intentService);
                }
                else{
                    this.startService(intentService);
                }

                audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                int callStatus = callChat.getStatus();
                log("The status of the callChat is: " + callStatus);
                titleToolbar.setText(chat.getTitle());
                updateSubTitleClock();

                if(chat.isGroup()){
                    smallElementsIndividualCallLayout.setVisibility(View.GONE);
                    bigElementsIndividualCallLayout.setVisibility(View.GONE);
                    bigElementsGroupCallLayout.setVisibility(View.VISIBLE);


                }else{
                    smallElementsIndividualCallLayout.setVisibility(View.VISIBLE);
                    bigElementsIndividualCallLayout.setVisibility(View.VISIBLE);
                    bigElementsGroupCallLayout.setVisibility(View.GONE);
                    bigRecyclerView.setVisibility(GONE);
                    bigRecyclerViewLayout.setVisibility(GONE);
                }

                if(callStatus==MegaChatCall.CALL_STATUS_RING_IN){
                    ringtone = RingtoneManager.getRingtone(this, DEFAULT_RINGTONE_URI);
                    ringerTimer = new Timer();
                    MyRingerTask myRingerTask = new MyRingerTask();
                    ringerTimer.schedule(myRingerTask, 0, 500);

                    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 1000, 500, 500, 1000};
                    if (vibrator != null){
                        if (vibrator.hasVibrator()){
                            //FOR API>=26
                            //vibrator.vibrate(createWaveform(pattern, 0), USAGE_NOTIFICATION_RINGTONE); ??
                            vibrator.vibrate(pattern, 0);
                        }
                    }
                    if(chat.isGroup()){
                        log("onCreate()-Incoming group call");

                        //Get all the participants, add them to peersBeforeCall array and show only the avatars
                        if((peersBeforeCall != null)&&(peersBeforeCall.size() != 0)){
                            peersBeforeCall.clear();
                        }
                        if((peersOnCall != null)&&(peersOnCall.size() != 0)){
                            peersOnCall.clear();
                        }

                        if(callChat.getParticipants().size()!=0){
                            boolean changes = false;
                            for(int i = 0; i < callChat.getParticipants().size(); i++){
                                long userHandle = callChat.getParticipants().get(i);
                                InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle,  chat.getPeerFullnameByHandle(userHandle), false, false, false,true,null);
                                log("onCreate()- Incoming "+userPeer.getHandle()+" added in peersBeforeCall");
                                peersBeforeCall.add((peersBeforeCall.size() == 0 ? 0:(peersBeforeCall.size()-1)), userPeer);
                                changes = true;
                            }
                            if(changes){
                                updatePeers(false);
                            }
                        }
                    }else{
                        log("onCreate()-Incoming individual call");

                        myAvatarLayout.setVisibility(View.VISIBLE);
                        contactAvatarLayout.setVisibility(View.VISIBLE);
                        flagMyAvatar = true;
                        setProfileMyAvatar();
                        flagContactAvatar = false;
                        setProfileContactAvatar();
                    }

                }else if(callStatus==MegaChatCall.CALL_STATUS_IN_PROGRESS){

                    log("onCreate()- In Progress");
                    updateScreenStatusInProgress();
                }else if(callStatus==MegaChatCall.CALL_STATUS_REQUEST_SENT){
                    int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (volume == 0) {
                        toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
                        toneGenerator.startTone(ToneGenerator.TONE_SUP_RINGTONE, 60000);
                    }else {
                        thePlayer = MediaPlayer.create(getApplicationContext(), R.raw.outgoing_voice_video_call);
                        thePlayer.setLooping(true);
                        thePlayer.start();
                    }

                    if(chat.isGroup()){

                        log("onCreate()-Outgoing group call");

                        if((peersBeforeCall != null)&&(peersBeforeCall.size() != 0)){
                            peersBeforeCall.clear();
                        }
                        if((peersOnCall != null)&&(peersOnCall.size() != 0)){
                            peersOnCall.clear();
                        }

                        InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(),  megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false,true,null);
                        log("onCreate()- Outgoing "+myPeer.getHandle()+" added in peersOnCall");
                        peersOnCall.add(myPeer);
                        updatePeers(true);

                    }else{
                        log("onCreate()-Outgoing individual call");

                        flagMyAvatar = false;
                        setProfileMyAvatar();
                        flagContactAvatar = true;
                        setProfileContactAvatar();
                        myAvatarLayout.setVisibility(View.VISIBLE);
                    }
                    updateLocalVideoStatus();
                    updateLocalAudioStatus();

                }else if(callStatus==MegaChatCall.CALL_STATUS_JOINING){
                    log("onCreate()- Joining");
                    updateScreenStatusInProgress();

                }else{

                }
            }
        }
        checkPermissionsWriteLog();
        if(checkPermissions()){
            showInitialFABConfiguration();
        }
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {
        log("onRequestStart: " + request.getType());
    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
        log("onRequestUpdate");

    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestFinish(): "+request.getEmail());
    }


    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {}

    public void createDefaultAvatar(long userHandle,  String fullName) {
        log("createDefaultAvatar");

        Bitmap defaultAvatar = Bitmap.createBitmap(outMetrics.widthPixels, outMetrics.widthPixels, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.TRANSPARENT);

        String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(userHandle));
        if (color != null) {
            log("The color to set the avatar is " + color);
            p.setColor(Color.parseColor(color));
        }else {
            log("Default color to the avatar");
            p.setColor(ContextCompat.getColor(this, R.color.lollipop_primary_color));
        }
        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight()) {
            radius = defaultAvatar.getWidth() / 2;
        }else {
            radius = defaultAvatar.getHeight() / 2;
        }
        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        if(flagContactAvatar){
            myImage.setImageBitmap(defaultAvatar);
            String contactFirstLetter = fullName.charAt(0) + "";
            contactFirstLetter = contactFirstLetter.toUpperCase(Locale.getDefault());
            myInitialLetter.setText(contactFirstLetter);
            myInitialLetter.setTextSize(40);
            myInitialLetter.setTextColor(Color.WHITE);
            myInitialLetter.setVisibility(View.VISIBLE);
        }else {
            contactImage.setImageBitmap(defaultAvatar);
            String contactFirstLetter = fullName.charAt(0) + "";
            contactFirstLetter = contactFirstLetter.toUpperCase(Locale.getDefault());
            contactInitialLetter.setText(contactFirstLetter);
            contactInitialLetter.setTextSize(60);
            contactInitialLetter.setTextColor(Color.WHITE);
            contactInitialLetter.setVisibility(View.VISIBLE);
        }
    }

    public void setProfileContactAvatar(){
        log("setProfileContactAvatar");
        Bitmap bitmap = null;
        File avatar = null;
        if (context.getExternalCacheDir() != null) {
            avatar = new File(context.getExternalCacheDir().getAbsolutePath(), chat.getPeerEmail(0) + ".jpg");
        } else {
            avatar = new File(context.getCacheDir().getAbsolutePath(), chat.getPeerEmail(0) + ".jpg");
        }
        if (avatar.exists()) {
            if (avatar.length() > 0) {
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                bitmap = ThumbnailUtilsLollipop.getRoundedRectBitmap(context, bitmap, 3);
                if (bitmap != null) {
                    if(flagContactAvatar){
                        myImage.setImageBitmap(bitmap);
                        myInitialLetter.setVisibility(GONE);
                    }else{
                        contactImage.setImageBitmap(bitmap);
                        contactInitialLetter.setVisibility(GONE);
                    }
                }else{
                    UserAvatarListener listener = new UserAvatarListener(this);
                    avatar.delete();
                    if(!avatarRequested){
                        avatarRequested = true;
                        if (context.getExternalCacheDir() != null){
                            megaApi.getUserAvatar(chat.getPeerEmail(0), context.getExternalCacheDir().getAbsolutePath() + "/" + chat.getPeerEmail(0) + ".jpg", listener);
                        }else{
                            megaApi.getUserAvatar(chat.getPeerEmail(0), context.getCacheDir().getAbsolutePath() + "/" + chat.getPeerEmail(0) + ".jpg", listener);
                        }
                    }
                    createDefaultAvatar(chat.getPeerHandle(0), chat.getPeerFullname(0));
                }
            }else{
                UserAvatarListener listener = new UserAvatarListener(this);

                if(!avatarRequested){
                    avatarRequested = true;
                    if (context.getExternalCacheDir() != null){
                        megaApi.getUserAvatar(chat.getPeerEmail(0), context.getExternalCacheDir().getAbsolutePath() + "/" + chat.getPeerEmail(0) + ".jpg", listener);
                    }
                    else{
                        megaApi.getUserAvatar(chat.getPeerEmail(0), context.getCacheDir().getAbsolutePath() + "/" + chat.getPeerEmail(0) + ".jpg", listener);
                    }
                }
                createDefaultAvatar(chat.getPeerHandle(0), chat.getPeerFullname(0));
            }
        }else{
            UserAvatarListener listener = new UserAvatarListener(this);
            if(!avatarRequested){
                avatarRequested = true;
                if (context.getExternalCacheDir() != null){
                    megaApi.getUserAvatar(chat.getPeerEmail(0), context.getExternalCacheDir().getAbsolutePath() + "/" + chat.getPeerEmail(0) + ".jpg", listener);
                }else{
                    megaApi.getUserAvatar(chat.getPeerEmail(0), context.getCacheDir().getAbsolutePath() + "/" + chat.getPeerEmail(0) + ".jpg", listener);
                }
            }
            createDefaultAvatar(chat.getPeerHandle(0), chat.getPeerFullname(0));
        }
    }

    public void createMyDefaultAvatar() {
        log("createMyDefaultAvatar");

        String myFullName = megaChatApi.getMyFullname();
        String myFirstLetter=myFullName.charAt(0) + "";
        myFirstLetter = myFirstLetter.toUpperCase(Locale.getDefault());
        long userHandle = megaChatApi.getMyUserHandle();
        Bitmap defaultAvatar = Bitmap.createBitmap(outMetrics.widthPixels,outMetrics.widthPixels, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.TRANSPARENT);

        String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(userHandle));

        if(color!=null){
            log("The color to set the avatar is "+color);
            p.setColor(Color.parseColor(color));
        }else{
            log("Default color to the avatar");
            p.setColor(ContextCompat.getColor(this, R.color.lollipop_primary_color));
        }
        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight()) {
            radius = defaultAvatar.getWidth() / 2;
        }else {
            radius = defaultAvatar.getHeight() / 2;
        }
        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        if(flagMyAvatar){
            myImage.setImageBitmap(defaultAvatar);
            myInitialLetter.setText(myFirstLetter);
            myInitialLetter.setTextSize(40);
            myInitialLetter.setTextColor(Color.WHITE);
            myInitialLetter.setVisibility(View.VISIBLE);
        }else{
            contactImage.setImageBitmap(defaultAvatar);
            contactInitialLetter.setText(myFirstLetter);
            contactInitialLetter.setTextSize(60);
            contactInitialLetter.setTextColor(Color.WHITE);
            contactInitialLetter.setVisibility(View.VISIBLE);
        }
    }

    public void setProfileMyAvatar() {
        log("setProfileMyAvatar: "+ flagMyAvatar);
        Bitmap myBitmap = null;
        File avatar = null;
        if (context != null) {
            log("context is not null");
            if (context.getExternalCacheDir() != null) {
                avatar = new File(context.getExternalCacheDir().getAbsolutePath(), megaChatApi.getMyEmail() + ".jpg");
            } else {
                avatar = new File(context.getCacheDir().getAbsolutePath(), megaChatApi.getMyEmail() + ".jpg");
            }
        }
        if (avatar.exists()) {
            if (avatar.length() > 0) {
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                myBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                myBitmap = ThumbnailUtilsLollipop.getRoundedRectBitmap(context, myBitmap, 3);
                if (myBitmap != null) {
                    if(flagMyAvatar){
                        myImage.setImageBitmap(myBitmap);
                        myInitialLetter.setVisibility(GONE);
                    }else{
                        contactImage.setImageBitmap(myBitmap);
                        contactInitialLetter.setVisibility(GONE);
                    }
                }else{
                    createMyDefaultAvatar();
                }
            }else {
                createMyDefaultAvatar();
            }
        }else {
            createMyDefaultAvatar();
        }
    }

    protected void hideActionBar(){
        if (aB != null && aB.isShowing()) {
            if(tB != null) {
               tB.animate().translationY(-220).setDuration(800L)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                aB.hide();
                            }
                        }).start();
            } else {
                aB.hide();
            }
        }
    }

    protected void showActionBar(){
        if (aB != null && !aB.isShowing()) {
            aB.show();
            if(tB != null) {
                tB.animate().translationY(0).setDuration(800L).start();
            }
        }
    }

    protected void hideFABs(){
        if(videoFAB.getVisibility() == View.VISIBLE){
            videoFAB.hide();
        }
        if(microFAB.getVisibility() == View.VISIBLE){
            microFAB.hide();
        }
        if(hangFAB.getVisibility() == View.VISIBLE){
            hangFAB.hide();
        }
        if(answerCallFAB.getVisibility() == View.VISIBLE){
            answerCallFAB.hide();
            relativeCall.setVisibility(GONE);
        }

        if((bigRecyclerViewLayout!=null)&&(bigRecyclerView!=null)&&(parentBigCameraGroupCall!=null)){
            RelativeLayout.LayoutParams bigRecyclerViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            bigRecyclerViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            bigRecyclerViewParams.addRule(RelativeLayout.BELOW, R.id.parent_layout_big_camera_group_call);
            bigRecyclerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, 0);
            bigRecyclerViewLayout.setLayoutParams(bigRecyclerViewParams);
            bigRecyclerViewLayout.requestLayout();
        }
    }

    @Override
    public void onPause(){
        log("onPause");
        mSensorManager.unregisterListener(this);

        super.onPause();
    }

    @Override
    protected void onResume() {
        log("onResume()");
        if(peersOnCall!=null){
            if(peersOnCall.size()!=0){
                if(peersOnCall.size()<7){
                    for(InfoPeerGroupCall peer :peersOnCall){
                        if(peer.getListener()!=null){
                            if(peer.getListener().getHeight() != 0){
                                peer.getListener().setHeight(0);
                            }
                            if(peer.getListener().getWidth() != 0){
                                peer.getListener().setWidth(0);
                            }
                        }
                    }
                }else{
                    for(InfoPeerGroupCall peer :peersOnCall){
                        if(peer.getListener()!=null){
                            if(peer.getListener().getHeight() != 0){
                                peer.getListener().setHeight(0);
                            }
                            if(peer.getListener().getWidth() != 0){
                                peer.getListener().setWidth(0);
                            }
                        }
                    }
                }

            }
        }
        super.onResume();
//        adapter.notifyDataSetChanged();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        MegaApplication.activityResumed();
        if((callChat.getStatus()==MegaChatCall.CALL_STATUS_IN_PROGRESS)||(callChat.getStatus()==MegaChatCall.CALL_STATUS_REQUEST_SENT)){
            ((MegaApplication) getApplication()).sendSignalPresenceActivity();
        }
    }

    @Override
    public void onDestroy(){
        log("onDestroy()");

        if (megaChatApi != null) {
            megaChatApi.removeChatCallListener(this);
//            megaChatApi.removeChatVideoListener(chatId, userHandle, this);
        }

        if (customHandler != null){
            customHandler.removeCallbacksAndMessages(null);
        }
        clearHandlers();

        if (localCameraFragment != null) {
            localCameraFragment.removeSurfaceView();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(localCameraFragment);
            localCameraFragment = null;
        }

        if (localCameraFragmentFS != null) {
            localCameraFragmentFS.removeSurfaceView();
            FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
            ftFS.remove(localCameraFragmentFS);
            localCameraFragmentFS = null;
        }
        if (remoteCameraFragmentFS != null) {
            remoteCameraFragmentFS.removeSurfaceView();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(remoteCameraFragmentFS);
            remoteCameraFragmentFS = null;
        }

        if (bigCameraGroupCallFragment != null) {
            bigCameraGroupCallFragment.removeSurfaceView();
            FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
            ftFS.remove(bigCameraGroupCallFragment);
            bigCameraGroupCallFragment = null;
        }

        peerSelected = null;
        if(adapterList!=null){
            adapterList.updateMode(false);
        }
        isManualMode = false;

        if(adapterGrid!=null){
            adapterGrid.onDestroy();
        }
        if(adapterList!=null){
            adapterList.onDestroy();
        }

        peersOnCall.clear();
        peersBeforeCall.clear();

        recyclerView.setAdapter(null);
        bigRecyclerView.setAdapter(null);

        stopAudioSignals();
        mSensorManager.unregisterListener(this);
        MegaApplication.activityPaused();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        log("onBackPressed");
        super.onBackPressed();

        if (megaChatApi != null) {
            megaChatApi.removeChatCallListener(this);
//            megaChatApi.removeChatVideoListener(chatId, userHandle, this);
        }

        if (customHandler != null){
            customHandler.removeCallbacksAndMessages(null);
        }
        clearHandlers();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        }
        else {
            super.finish();
        }
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {
        log("onRequestStart: "+request.getType());
    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {
        log("onRequestUpdate: "+request.getType());
    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish: "+request.getType());

        if(request.getType() == MegaChatRequest.TYPE_HANG_CHAT_CALL){
            log("onRequestFinish: TYPE_HANG_CHAT_CALL");

            MegaApplication.activityPaused();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                super.finishAndRemoveTask();
            }else {
                super.finish();
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_ANSWER_CHAT_CALL){
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                videoFAB.setVisibility(View.VISIBLE);
                relativeVideo.setVisibility(View.VISIBLE);

                microFAB.setVisibility(View.VISIBLE);
                answerCallFAB.setVisibility(GONE);
                relativeCall.setVisibility(GONE);
                linearArrowVideo.setVisibility(GONE);

                if(request.getFlag()==true){
                    log("Ok answer with video");
                }else{
                    log("Ok answer with NO video - ");
                }
                updateLocalVideoStatus();
                updateLocalAudioStatus();
            }
            else{
                log("Error call: "+e.getErrorString());
                MegaApplication.activityPaused();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    super.finishAndRemoveTask();
                }
                else {
                    super.finish();
                }
//                showSnackbar(getString(R.string.clear_history_error));
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_DISABLE_AUDIO_VIDEO_CALL){

            if(e.getErrorCode()==MegaChatError.ERROR_OK){
//                if(request.getParamType()==MegaChatRequest.AUDIO){
//                    if(request.getFlag()==true){
//                        log("Enable audio");
//                        microFAB.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
//                    }
//                    else{
//                        log("Disable audio");
//                        microFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accentColor)));
//                    }
//                }
//                else if(request.getParamType()==MegaChatRequest.VIDEO){
//                    if(request.getFlag()==true){
//                        log("Enable video");
//                        myAvatarLayout.setVisibility(View.VISIBLE);
//                        myImageBorder.setVisibility(GONE);
//                        videoFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accentColor)));
//                    }
//                    else{
//                        log("Disable video");
//                        myAvatarLayout.setVisibility(GONE);
//                        myImageBorder.setVisibility(View.VISIBLE);
//                        videoFAB.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
//                    }
//                }
            }
            else{
                log("Error changing audio or video: "+e.getErrorString());
//                showSnackbar(getString(R.string.clear_history_error));
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
    }

    @Override
    public void onChatCallUpdate(MegaChatApiJava api, MegaChatCall call) {
        log("onChatCallUpdate()");

        if(call.getChatid()==chatId){
            this.callChat = call;
            log("onChatCallUpdate()  call.getChatid() = chatId = "+chatId);

            if(callChat.hasChanged(MegaChatCall.CHANGE_TYPE_STATUS)){

                int callStatus = callChat.getStatus();
                log("onChatCallUpdate()-CHANGE_TYPE_STATUS -> status: "+callStatus);

                switch (callStatus){
                    case MegaChatCall.CALL_STATUS_IN_PROGRESS:{

                        if(chat.isGroup()){
                            log("CALL_STATUS_IN_PROGRESS -Group call");

                            if((peersOnCall != null)&&(peersOnCall.size() != 0)){
                                boolean peerContain = false;
                                for(InfoPeerGroupCall peer : peersOnCall) {
                                    if (peer.getHandle().equals(megaChatApi.getMyUserHandle())) {
                                        peerContain = true;
                                        break;
                                    }
                                }
                                if(!peerContain){
                                    InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(),  megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false,true,null);
                                    log("onChatCallUpdate()- CALL_STATUS_IN_PROGRESS "+myPeer.getHandle()+" added in peersOnCall");
                                    peersOnCall.add(myPeer);
                                    updatePeers(true);
                                }
                            }else{
                                if((peersBeforeCall!=null)&&(peersBeforeCall.size()!=0)){
                                    for(InfoPeerGroupCall peerBefore: peersBeforeCall){
                                        log("onChatCallUpdate()- CALL_STATUS_IN_PROGRESS "+peerBefore.getHandle()+" added in peersOnCall");
                                        peersOnCall.add((peersOnCall.size() == 0 ? 0:(peersOnCall.size()-1)), peerBefore);
                                    }
                                }
                                InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(),  megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false,true,null);
                                log("onChatCallUpdate()- CALL_STATUS_IN_PROGRESS "+myPeer.getHandle()+" added in peersOnCall");
                                peersOnCall.add(myPeer);
                                updatePeers(true);
                            }
                            updateLocalVideoStatus();
                            updateLocalAudioStatus();

                        }else{
                            log("CALL_STATUS_IN_PROGRESS - Individual call");

                            flagMyAvatar = true;
                            setProfileMyAvatar();
                            flagContactAvatar = false;
                            setProfileContactAvatar();
                            if (localCameraFragmentFS != null) {
                                log("remove localCameraFragmentFS");
                                localCameraFragmentFS.removeSurfaceView();
                                FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
                                ftFS.remove(localCameraFragmentFS);
                                localCameraFragmentFS = null;
                                contactAvatarLayout.setVisibility(View.VISIBLE);
                                parentLocalFS.setVisibility(View.GONE);
                                fragmentContainerLocalCameraFS.setVisibility(View.GONE);
                            }

                            updateLocalVideoStatus();
                            updateLocalAudioStatus();

                            updateRemoteVideoStatus(-1);
                            updateRemoteAudioStatus(-1);

                        }

                        videoFAB.setOnClickListener(null);
                        answerCallFAB.setOnTouchListener(null);
                        videoFAB.setOnTouchListener(null);
                        videoFAB.setOnClickListener(this);

                        updateSubTitleClock();
                        stopAudioSignals();
                        rtcAudioManager.start(null);
                        showInitialFABConfiguration();
                        break;

                    }
                    case MegaChatCall.CALL_STATUS_TERMINATING_USER_PARTICIPATION:{
                        log("CALL_STATUS_TERMINATING_USER_PARTICIPATION");
                        //I have finished the group call but I can join again

                        log("Terminating call of chat: "+chatId);
                        if(chat.isGroup()){
                            stopAudioSignals();
                            rtcAudioManager.stop();
                            MegaApplication.activityPaused();
//                            if (bigCameraGroupCallFragment != null) {
//                                bigCameraGroupCallFragment.removeSurfaceView();;
//                                FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
//                                ftFS.remove(bigCameraGroupCallFragment);
//                                bigCameraGroupCallFragment = null;
//                            }

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                super.finishAndRemoveTask();
                            }
                            else {
                                super.finish();
                            }
                        }else{

//                            if (localCameraFragment != null) {//
//                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                                ft.remove(localCameraFragment);
//                                localCameraFragment = null;
//                            }
//                            if (localCameraFragmentFS != null) {
//                                FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
//                                ftFS.remove(localCameraFragmentFS);
//                                localCameraFragmentFS = null;
//                            }
//                            if (remoteCameraFragmentFS != null) {
//                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                                ft.remove(remoteCameraFragmentFS);
//                                remoteCameraFragmentFS = null;
//                            }
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                super.finishAndRemoveTask();
                            }
                            else {
                                super.finish();
                            }
                        }




                        break;
                    }
                    case MegaChatCall.CALL_STATUS_USER_NO_PRESENT:{
                        log("CALL_STATUS_USER_NO_PRESENT");

                        break;
                    }
                    case MegaChatCall.CALL_STATUS_DESTROYED:{
                        log(" CALL_STATUS_DESTROYED");

                        log("CALL_STATUS_DESTROYED:TERM code of the call: "+call.getTermCode());
                        //The group call has finished but I can not join again

                        stopAudioSignals();
                        rtcAudioManager.stop();
                        MegaApplication.activityPaused();

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            super.finishAndRemoveTask();
                        }
                        else {
                            super.finish();
                        }
                        break;
                    }
                }
            }
            else if(call.hasChanged(MegaChatCall.CHANGE_TYPE_SESSION_STATUS)){
                log("CHANGE_TYPE_SESSION_STATUS");
                log("onChatCallUpdate()-CHANGE_TYPE_SESSION_STATUS");

                if(chat.isGroup()){

                    if((peersBeforeCall != null)&&(peersBeforeCall.size() != 0)){
                        peersBeforeCall.clear();
                    }
                    long userHandle = call.getPeerSessionStatusChange();

                    MegaChatSession userSession = callChat.getMegaChatSession(userHandle);
                    if(userSession != null){
                        if(userSession.getStatus()==MegaChatSession.SESSION_STATUS_IN_PROGRESS){
                            log("SESSION_STATUS_IN_PROGRESS");

                            updateRemoteVideoStatus(userHandle);
                            updateRemoteAudioStatus(userHandle);

                            updateSubtitleToolbar();
                            //contact joined the group call
                            log(chat.getPeerFullnameByHandle(userHandle)+" joined in the group call");

                        }else if(userSession.getStatus()==MegaChatSession.SESSION_STATUS_DESTROYED){
                            log("SESSION_STATUS_DESTROYED ");

                            updateSubtitleToolbar();
                            //contact left the group call
                            log(chat.getPeerFullnameByHandle(userHandle)+" left the group call");
                        }
                    }
                }else{
                    log("onChatCallUpdate() - CHANGE_TYPE_SESSION_STATUS");

                    if(call.getPeerSessionStatusChange()==chat.getPeerHandle(0)){
//                        updateSubTitle();
                    }
                    updateRemoteVideoStatus(-1);
                    updateRemoteAudioStatus(-1);

                    updateLocalVideoStatus();
                    updateLocalAudioStatus();
                }
            }
            else if(call.hasChanged(MegaChatCall.CHANGE_TYPE_REMOTE_AVFLAGS)){
                log("onChatCallUpdate()-CHANGE_TYPE_REMOTE_AVFLAGS");
                if(chat.isGroup()){
                    updateRemoteVideoStatus(call.getPeerSessionStatusChange());
                    updateRemoteAudioStatus(call.getPeerSessionStatusChange());
                }else{
                    if(call.getPeerSessionStatusChange()==chat.getPeerHandle(0)){
                        updateRemoteVideoStatus(-1);
                        updateRemoteAudioStatus(-1);
                    }
                }
            }else if(call.hasChanged(MegaChatCall.CHANGE_TYPE_LOCAL_AVFLAGS)){
                log("onChatCallUpdate()-CHANGE_TYPE_LOCAL_AVFLAGS");
                updateLocalVideoStatus();
                updateLocalAudioStatus();


            }else if(call.hasChanged(MegaChatCall.CHANGE_TYPE_RINGING_STATUS)){
                log("onChatCallUpdate()-CHANGE_TYPE_RINGING_STATUS");

            }else if(call.hasChanged(MegaChatCall.CHANGE_TYPE_CALL_COMPOSITION)){
                log("CHANGE_TYPE_CALL_COMPOSITION : call.getStatus(): "+call.getStatus());

                if(call.getStatus() ==  MegaChatCall.CALL_STATUS_RING_IN){
                    log("CHANGE_TYPE_CALL_COMPOSITION -CALL_STATUS_RING_IN -> participants: "+call.getParticipants().size());

                    boolean isMe = false;
                    for(int i = 0; i < call.getParticipants().size(); i++){
                        long userHandle = call.getParticipants().get(i);
                        if (userHandle == megaChatApi.getMyUserHandle()){
                            isMe = true;
                            break;
                        }
                    }

                    if(!isMe){
                        boolean changes = false;
                        //Get all participant and check it
                        for(int i = 0; i < call.getParticipants().size(); i++){
                            boolean peerContain = false;
                            long userHandle = call.getParticipants().get(i);
                            for(InfoPeerGroupCall peerBeforeCall: peersBeforeCall){
                                if(peerBeforeCall.getHandle() == userHandle){
                                    peerContain = true;
                                    break;
                                }
                            }
                            if(!peerContain){
                                InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle,  chat.getPeerFullnameByHandle(userHandle), false, false, false,true,null);
                                log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (ring in) "+userPeer.getName()+" added");
                                peersBeforeCall.add((peersBeforeCall.size() == 0 ? 0:(peersBeforeCall.size()-1)), userPeer);
                                changes = true;
                            }
                        }
                        for(int i=0;i<peersBeforeCall.size();i++){
                            boolean peerContained = false;
                            for(int j=0; j<call.getParticipants().size();j++){
                                long userHandle = call.getParticipants().get(j);
                                if(peersBeforeCall.get(i).getHandle() == userHandle){
                                    peerContained = true;
                                }
                            }
                            if(!peerContained){
                                log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (ring in) "+peersBeforeCall.get(i).getName()+" removed");
                                peersBeforeCall.remove(i);
                                changes = true;
                            }
                        }

                        if(changes){
                            updatePeers(false);
                        }

                    }
                }else if(call.getStatus() ==  MegaChatCall.CALL_STATUS_IN_PROGRESS){
                    log("CHANGE_TYPE_CALL_COMPOSITION -CALL_STATUS_IN_PROGRESS -> participants: "+call.getParticipants().size());
                    if(peersOnCall != null){
                        if(peersOnCall.size()!=0){
                            //Get all participant and check it
                            for (int i = 0; i < call.getParticipants().size(); i++) {
                                boolean peerContain = false;
                                long userHandle = call.getParticipants().get(i);
                                for (InfoPeerGroupCall peerOnCall : peersOnCall) {
                                    if (peerOnCall.getHandle() == userHandle) {
                                        peerContain = true;
                                        break;
                                    }
                                }
                                if (!peerContain) {
                                    if (userHandle == megaChatApi.getMyUserHandle()){
                                        InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(), megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false, true,null);
                                        log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (in progress) "+myPeer.getName()+" added");
                                        peersOnCall.add(myPeer);
                                        updatePeers(true);
                                    } else {
                                        InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle, chat.getPeerFullnameByHandle(userHandle), false, false, false, true,null);
                                        log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (in progress) "+userPeer.getName()+" added");
                                        peersOnCall.add((peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1)), userPeer);
                                        infoUsersBar.setText(userPeer.getName()+" "+getString(R.string.contact_joined_the_call));
                                        infoUsersBar.setBackgroundColor(ContextCompat.getColor(this, R.color.accentColor));
                                        infoUsersBar.setAlpha(1);
                                        infoUsersBar.setVisibility(View.VISIBLE);
                                        infoUsersBar.animate().alpha(0).setDuration(4000);

                                        if(peersOnCall.size() < 7){
                                            if (adapterGrid != null) {
                                                if (peersOnCall.size() < 4) {
                                                    recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                    recyclerView.setColumnWidth((int) widthScreenPX);
                                                    int posInserted = (peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                    adapterGrid.notifyItemInserted(posInserted);
                                                    adapterGrid.notifyDataSetChanged();
                                                    updateSubtitleToolbar();
                                                }else{
                                                    if (peersOnCall.size() == 4) {
                                                        recyclerViewLayout.setPadding(0, Util.scaleWidthPx(136, outMetrics), 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX / 2);
                                                        adapterGrid.notifyItemInserted(peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                        adapterGrid.notifyDataSetChanged();
                                                        updateSubtitleToolbar();
                                                    } else {
                                                        recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX / 2);
                                                        int posInserted = (peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                        adapterGrid.notifyItemInserted(posInserted);
                                                        adapterGrid.notifyItemRangeChanged((posInserted - 1), peersOnCall.size());
                                                        updateSubtitleToolbar();
                                                    }
                                                }
                                            }else{
                                                updatePeers(true);
                                            }
                                        }else{
                                            if (adapterList != null) {
                                                if(peersOnCall.size() == 7){
                                                    updatePeers(true);
                                                }else{
                                                    int posInserted=(peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                    adapterList.notifyItemInserted(posInserted);
                                                    adapterList.notifyItemRangeChanged((posInserted-1), peersOnCall.size());
                                                    updateUserSelected(true);
                                                    updateSubtitleToolbar();
                                                }
                                            } else {
                                                updatePeers(true);
                                            }
                                        }

                                    }
                                }
                            }

                            for (int i = 0; i < peersOnCall.size(); i++) {
                                boolean peerContained = false;
                                for (int j = 0; j < call.getParticipants().size(); j++) {
                                    long userHandle = call.getParticipants().get(j);
                                    if (peersOnCall.get(i).getHandle() == userHandle){
                                        peerContained = true;
                                        break;
                                    }
                                }
                                if (!peerContained) {
                                    log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (in progress) "+peersOnCall.get(i).getName()+" removed");
                                    infoUsersBar.setText(peersOnCall.get(i).getName()+" "+getString(R.string.contact_left_the_call));
                                    infoUsersBar.setBackgroundColor(ContextCompat.getColor(this, R.color.accentColor));
                                    infoUsersBar.setAlpha(1);
                                    infoUsersBar.setVisibility(View.VISIBLE);
                                    infoUsersBar.animate().alpha(0).setDuration(4000);
                                    peersOnCall.remove(i);

                                    if (peersOnCall.size() < 7) {

                                        if (adapterGrid != null) {
                                            if (peersOnCall.size() < 4) {
                                                recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                recyclerView.setColumnWidth((int) widthScreenPX);
                                                adapterGrid.notifyItemRemoved(i);
                                                adapterGrid.notifyDataSetChanged();
                                                updateSubtitleToolbar();
                                            } else {
                                                if(peersOnCall.size() == 6){
                                                    recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                    recyclerView.setColumnWidth((int) widthScreenPX/2);
                                                    adapterGrid.notifyItemRemoved(i);
                                                    adapterGrid.notifyDataSetChanged();
                                                    updateSubtitleToolbar();
                                                }else{
                                                    if(peersOnCall.size() == 4){
                                                        recyclerViewLayout.setPadding(0, Util.scaleWidthPx(136, outMetrics), 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX / 2);
                                                    }else{
                                                        recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX/2);
                                                    }
                                                    adapterGrid.notifyItemRemoved(i);
                                                    adapterGrid.notifyItemRangeChanged(i, peersOnCall.size());
                                                    updateSubtitleToolbar();
                                                }
                                            }

                                        } else {
                                            updatePeers(true);
                                        }
                                    } else {
                                        if (adapterList != null) {
                                            if(peersOnCall.size() >= 7){
                                                adapterList.notifyItemRemoved(i);
                                                adapterList.notifyItemRangeChanged(i, peersOnCall.size());
                                                updateUserSelected(true);
                                                updateSubtitleToolbar();
                                            }else{
                                                updatePeers(true);
                                            }
                                        } else {
                                            updatePeers(true);
                                        }
                                    }
                                }
                            }
                        }else{
                            boolean changes = false;
                            for(int i=0;i<call.getParticipants().size();i++){
                                if (call.getParticipants().get(i) == megaChatApi.getMyUserHandle()){
                                    InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(), megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false, true,null);
                                    log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (in progress) "+myPeer.getName()+" added");
                                    peersOnCall.add(myPeer);
                                    changes = true;
                                }else{
                                    InfoPeerGroupCall userPeer = new InfoPeerGroupCall(call.getParticipants().get(i),  chat.getPeerFullnameByHandle(call.getParticipants().get(i)), false, false, false, true,null);
                                    log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (in progress) "+userPeer.getName()+" added");
                                    peersOnCall.add((peersOnCall.size() == 0 ? 0:(peersOnCall.size()-1)), userPeer);
                                    changes = true;
                                }
                            }
                            if(changes){
                                updatePeers(true);
                            }
                        }

                    }

                }else if(call.getStatus() ==  MegaChatCall.CALL_STATUS_JOINING){
                    log("CHANGE_TYPE_CALL_COMPOSITION - CALL_STATUS_JOINING -> participants: "+call.getParticipants().size());
                    if(peersOnCall != null){
                        if(peersOnCall.size()!=0){
                            //Get all participant and check it
                            for (int i = 0; i < call.getParticipants().size(); i++) {
                                boolean peerContain = false;
                                long userHandle = call.getParticipants().get(i);
                                for (InfoPeerGroupCall peerOnCall : peersOnCall) {
                                    if (peerOnCall.getHandle() == userHandle) {
                                        peerContain = true;
                                        break;
                                    }
                                }
                                if (!peerContain) {
                                    if (userHandle == megaChatApi.getMyUserHandle()){
                                        InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(), megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false, true,null);
                                        log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (joining) "+myPeer.getName()+" added");
                                        peersOnCall.add(myPeer);
                                        updatePeers(true);
                                    } else {
                                        InfoPeerGroupCall userPeer = new InfoPeerGroupCall(userHandle, chat.getPeerFullnameByHandle(userHandle), false, false, false, true,null);
                                        log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (joining) "+userPeer.getName()+" added");
                                        peersOnCall.add((peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1)), userPeer);
                                        infoUsersBar.setText(userPeer.getName()+" "+getString(R.string.contact_joined_the_call));
                                        infoUsersBar.setBackgroundColor(ContextCompat.getColor(this, R.color.accentColor));
                                        infoUsersBar.setAlpha(1);
                                        infoUsersBar.setVisibility(View.VISIBLE);
                                        infoUsersBar.animate().alpha(0).setDuration(4000);

                                        if(peersOnCall.size() < 7){
                                            if (adapterGrid != null) {
                                                if (peersOnCall.size() < 4) {
                                                    recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                    recyclerView.setColumnWidth((int) widthScreenPX);
                                                    int posInserted = (peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                    adapterGrid.notifyItemInserted(posInserted);
                                                    adapterGrid.notifyDataSetChanged();
                                                    updateSubtitleToolbar();
                                                }else{
                                                    if (peersOnCall.size() == 4) {
                                                        recyclerViewLayout.setPadding(0, Util.scaleWidthPx(136, outMetrics), 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX / 2);
                                                        adapterGrid.notifyItemInserted(peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                        adapterGrid.notifyDataSetChanged();
                                                        updateSubtitleToolbar();
                                                    } else {
                                                        recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX / 2);
                                                        int posInserted = (peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                        adapterGrid.notifyItemInserted(posInserted);
                                                        adapterGrid.notifyItemRangeChanged((posInserted - 1), peersOnCall.size());
                                                        updateSubtitleToolbar();
                                                    }
                                                }
                                            }else{
                                                updatePeers(true);
                                            }
                                        }else{
                                            if (adapterList != null) {
                                                if(peersOnCall.size() == 7){
                                                    updatePeers(true);
                                                }else{
                                                    int posInserted=(peersOnCall.size() == 0 ? 0 : (peersOnCall.size() - 1));
                                                    adapterList.notifyItemInserted(posInserted);
                                                    adapterList.notifyItemRangeChanged((posInserted-1), peersOnCall.size());
                                                    updateUserSelected(true);
                                                    updateSubtitleToolbar();
                                                }
                                            } else {
                                                updatePeers(true);
                                            }
                                        }

                                    }
                                }
                            }

                            for (int i = 0; i < peersOnCall.size(); i++) {
                                boolean peerContained = false;
                                for (int j = 0; j < call.getParticipants().size(); j++) {
                                    long userHandle = call.getParticipants().get(j);
                                    if (peersOnCall.get(i).getHandle() == userHandle){
                                        peerContained = true;
                                        break;
                                    }
                                }
                                if (!peerContained) {
                                    log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (joining) "+peersOnCall.get(i).getName()+" removed");
                                    infoUsersBar.setText(peersOnCall.get(i).getName()+" "+getString(R.string.contact_left_the_call));
                                    infoUsersBar.setBackgroundColor(ContextCompat.getColor(this, R.color.accentColor));
                                    infoUsersBar.setAlpha(1);
                                    infoUsersBar.setVisibility(View.VISIBLE);
                                    infoUsersBar.animate().alpha(0).setDuration(4000);
                                    peersOnCall.remove(i);

                                    if (peersOnCall.size() < 7) {

                                        if (adapterGrid != null) {
                                            if (peersOnCall.size() < 4) {
                                                recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                recyclerView.setColumnWidth((int) widthScreenPX);
                                                adapterGrid.notifyItemRemoved(i);
                                                adapterGrid.notifyDataSetChanged();
                                                updateSubtitleToolbar();
                                            } else {
                                                if(peersOnCall.size() == 6){
                                                    recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                    recyclerView.setColumnWidth((int) widthScreenPX/2);
                                                    adapterGrid.notifyItemRemoved(i);
                                                    adapterGrid.notifyDataSetChanged();
                                                    updateSubtitleToolbar();
                                                }else{
                                                    if(peersOnCall.size() == 4){
                                                        recyclerViewLayout.setPadding(0, Util.scaleWidthPx(136, outMetrics), 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX / 2);
                                                    }else{
                                                        recyclerViewLayout.setPadding(0, 0, 0, 0);
                                                        recyclerView.setColumnWidth((int) widthScreenPX/2);
                                                    }
                                                    adapterGrid.notifyItemRemoved(i);
                                                    adapterGrid.notifyItemRangeChanged(i, peersOnCall.size());
                                                    updateSubtitleToolbar();
                                                }
                                            }

                                        } else {
                                            updatePeers(true);
                                        }
                                    } else {
                                        if (adapterList != null) {
                                            if(peersOnCall.size() >= 7){
                                                adapterList.notifyItemRemoved(i);
                                                adapterList.notifyItemRangeChanged(i, peersOnCall.size());
                                                updateUserSelected(true);
                                                updateSubtitleToolbar();
                                            }else{
                                                updatePeers(true);
                                            }
                                        } else {
                                            updatePeers(true);
                                        }
                                    }
                                }
                            }
                        }else{
                            boolean changes = false;
                            for(int i=0;i<call.getParticipants().size();i++){
                                if (call.getParticipants().get(i) == megaChatApi.getMyUserHandle()){
                                    InfoPeerGroupCall myPeer = new InfoPeerGroupCall(megaChatApi.getMyUserHandle(), megaChatApi.getMyFullname(), callChat.hasLocalVideo(), callChat.hasLocalAudio(), false, true,null);
                                    log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (joining) "+myPeer.getName()+" added");
                                    peersOnCall.add(myPeer);
                                    changes = true;
                                }else{
                                    InfoPeerGroupCall userPeer = new InfoPeerGroupCall(call.getParticipants().get(i),  chat.getPeerFullnameByHandle(call.getParticipants().get(i)), false, false, false, true,null);
                                    log("onChatCallUpdate()-CHANGE_TYPE_CALL_COMPOSITION (joining) "+userPeer.getName()+" added");
                                    peersOnCall.add((peersOnCall.size() == 0 ? 0:(peersOnCall.size()-1)), userPeer);
                                    changes = true;
                                }
                            }
                            if(changes){
                                updatePeers(true);
                            }
                        }

                    }


                }
            }else if(call.hasChanged(MegaChatCall.CHANGE_TYPE_SESSION_AUDIO_LEVEL)) {
                log("onChatCallUpdate()-CHANGE_TYPE_SESSION_AUDIO_LEVEL");

                if((peersOnCall != null)&&(peersOnCall.size() != 0)){
                    if(peersOnCall.size()>= 7){
                        if(!isManualMode){
                            long userHandle = call.getPeerSessionStatusChange();
                            MegaChatSession userSession = callChat.getMegaChatSession(userHandle);
                            if(userSession != null){
                                boolean userHasAudio = userSession.getAudioDetected();
                                if(userHasAudio){
                                    //The user is talking
                                    int position = -1;
                                    for(int i=0;i<peersOnCall.size();i++){
                                        if (peersOnCall.get(i).getHandle().equals(userHandle)){
                                            position = i;
                                        }
                                    }
                                    if(position != -1){
                                        peerSelected = adapterList.getNodeAt(position);
                                        log("audio detected: "+peerSelected.getName());
                                        updateUserSelected(true);
                                    }
                                }
                            }
                        }

                    }
                }
            }else if(call.hasChanged(MegaChatCall.CHANGE_TYPE_SESSION_NETWORK_QUALITY)){
                log("onChatCallUpdate()-CHANGE_TYPE_SESSION_NETWORK_QUALITY");

                if(chat.isGroup()){
                    log("onChatCallUpdate()-CHANGE_TYPE_SESSION_NETWORK_QUALITY isGroup");

                    if((peersBeforeCall != null)&&(peersBeforeCall.size() != 0)){
                        peersBeforeCall.clear();
                    }
                    long userHandle = call.getPeerSessionStatusChange();

                    MegaChatSession userSession = callChat.getMegaChatSession(userHandle);
                    if(userSession != null){
                        if(userSession.getStatus()==MegaChatSession.SESSION_STATUS_IN_PROGRESS){
                            log("SESSION_STATUS_IN_PROGRESS");

                            int qualityLevel =  userSession.getNetworkQuality();
                            if((peersOnCall!=null)&&(peersOnCall.size()!=0)){
                                if(qualityLevel < 2){
                                    //bad quality

                                    for(int i=0; i<peersOnCall.size(); i++){
                                        if(peersOnCall.get(i).getHandle() == userHandle){
                                            log(peersOnCall.get(i).getName()+" has bad quality: "+qualityLevel);
                                            if(peersOnCall.get(i).isGoodQuality()){
                                                peersOnCall.get(i).setGoodQuality(false);
                                                if(peersOnCall.size()<7){
                                                    if(adapterGrid!=null){
                                                        adapterGrid.changesInQuality(i, null);
                                                    }else{
                                                        updatePeers(true);
                                                    }
                                                }else{
                                                    if(adapterList!=null){
                                                        adapterList.changesInQuality(i, null);
                                                    }else{
                                                        updatePeers(true);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    //good quality
                                    for(int i=0; i<peersOnCall.size(); i++){
                                        if(peersOnCall.get(i).getHandle() == userHandle){
                                            if(!peersOnCall.get(i).isGoodQuality()){
                                                log(peersOnCall.get(i).getName()+" has good quality: "+qualityLevel);
                                                peersOnCall.get(i).setGoodQuality(true);
                                                if(peersOnCall.size()<7){
                                                    if(adapterGrid!=null){
                                                        adapterGrid.changesInQuality(i, null);
                                                    }else{
                                                        updatePeers(true);
                                                    }
                                                }else{
                                                    if(adapterList!=null){
                                                        adapterList.changesInQuality(i, null);
                                                    }else{
                                                        updatePeers(true);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }else{
                            log("other ");
                        }
                    }
                }else{
                    log("onChatCallUpdate()-CHANGE_TYPE_SESSION_NETWORK_QUALITY individual call");
                }
            }else{
                log("other: "+call.getChanges());
            }
        }
    }

    public void stopAudioSignals(){
        log("stopAudioSignals");

        try{
            if(thePlayer!=null){
                thePlayer.stop();
                thePlayer.release();
            }
        }
        catch(Exception e){
            log("Exception stopping player");
        }

        try{
            if (toneGenerator != null) {
                toneGenerator.stopTone();
                toneGenerator.release();
            }
        }
        catch(Exception e){
            log("Exception stopping tone generator");
        }

        try{
            if(ringtone != null){
                ringtone.stop();
            }
        }
        catch(Exception e){
            log("Exception stopping ring tone");
        }

        try{
            if (timer != null){
                timer.cancel();
            }

            if (ringerTimer != null) {
                log("Cancel ringer timer");
                ringerTimer.cancel();
            }

        }
        catch(Exception e){
            log("Exception stopping ringing timer");
        }

        try{
            if (vibrator != null){
                if (vibrator.hasVibrator()) {
                    vibrator.cancel();
                }
            }
        }
        catch(Exception e){
            log("Exception stopping vibrator");
        }

        thePlayer=null;
        toneGenerator = null;
        timer = null;
        ringerTimer = null;
    }


    int width = 0;
    int height = 0;
    Bitmap bitmap;

    @Override
    public void onClick(View v) {
        log("onClick");

        switch (v.getId()) {
            case R.id.call_chat_contact_image_rl:
            case R.id.parent_layout_big_camera_group_call:{
                remoteCameraClick();
                break;
            }
            case R.id.video_fab:{
                log("onClick video FAB");
                if(callChat.getStatus()==MegaChatCall.CALL_STATUS_RING_IN){
                    linearFAB = (LinearLayout) findViewById(R.id.linear_buttons);
                    RelativeLayout.LayoutParams layoutCompress = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutCompress.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    layoutCompress.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    linearFAB.setLayoutParams(layoutCompress);
                    linearFAB.requestLayout();
                    linearFAB.setOrientation(LinearLayout.HORIZONTAL);

                    megaChatApi.answerChatCall(chatId, true, this);
                    clearHandlers();

                    answerCallFAB.clearAnimation();
                    videoFAB.clearAnimation();
                }else{
                    if(callChat.hasLocalVideo()){
                        log(" disableVideo");
                        megaChatApi.disableVideo(chatId, this);
                    }else{
                        log(" enableVideo");
                        megaChatApi.enableVideo(chatId, this);
                    }
                }

                if((callChat.getStatus()==MegaChatCall.CALL_STATUS_IN_PROGRESS)||(callChat.getStatus()==MegaChatCall.CALL_STATUS_REQUEST_SENT)){
                    ((MegaApplication) getApplication()).sendSignalPresenceActivity();
                }
                break;
            }
            case R.id.micro_fab: {
                if(callChat.hasLocalAudio()){
                    megaChatApi.disableAudio(chatId, this);
                }else{
                    megaChatApi.enableAudio(chatId, this);
                }
                if((callChat.getStatus()==MegaChatCall.CALL_STATUS_IN_PROGRESS)||(callChat.getStatus()==MegaChatCall.CALL_STATUS_REQUEST_SENT)){
                    ((MegaApplication) getApplication()).sendSignalPresenceActivity();
                }
                break;
            }
            case R.id.hang_fab: {
                log("Click on hang fab");
                megaChatApi.hangChatCall(chatId, this);

                if((callChat.getStatus()==MegaChatCall.CALL_STATUS_IN_PROGRESS)||(callChat.getStatus()==MegaChatCall.CALL_STATUS_REQUEST_SENT)){
                    ((MegaApplication) getApplication()).sendSignalPresenceActivity();
                }
                break;
            }
            case R.id.answer_call_fab:{
                log("Click on answer fab");
                if(callChat.getStatus()==MegaChatCall.CALL_STATUS_RING_IN){
                    linearFAB = (LinearLayout) findViewById(R.id.linear_buttons);
                    RelativeLayout.LayoutParams layoutCompress = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutCompress.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                    layoutCompress.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                    linearFAB.setLayoutParams(layoutCompress);
                    linearFAB.requestLayout();
                    linearFAB.setOrientation(LinearLayout.HORIZONTAL);
                    megaChatApi.answerChatCall(chatId, false, this);
                    clearHandlers();

                    answerCallFAB.clearAnimation();
                    videoFAB.clearAnimation();
                }else{

                }

                if((callChat.getStatus()==MegaChatCall.CALL_STATUS_IN_PROGRESS)||(callChat.getStatus()==MegaChatCall.CALL_STATUS_REQUEST_SENT)){
                    ((MegaApplication) getApplication()).sendSignalPresenceActivity();
                }

                break;
            }
        }
    }

    public boolean checkPermissionsWriteLog(){
        log("checkPermissionsWriteLog()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasWriteLogPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED);
            if (!hasWriteLogPermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALL_LOG}, Constants.WRITE_LOG);
                return false;
            }else{
                return true;
            }
        }
        return true;
    }

    public boolean checkPermissions(){
        log("checkPermissions() Camera && Audio");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            if (!hasCameraPermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA);
                return false;
            }

            boolean hasRecordAudioPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
            if (!hasRecordAudioPermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, Constants.RECORD_AUDIO);
                return false;
            }

            return true;
        }
        return true;
    }

    public void showInitialFABConfiguration(){
        log("showInitialFABConfiguration() ");

        if(callChat.getStatus()==MegaChatCall.CALL_STATUS_RING_IN){
            relativeCall.setVisibility(View.VISIBLE);
            linearArrowCall.setVisibility(GONE);
            answerCallFAB.show();
            answerCallFAB.setVisibility(View.VISIBLE);
            hangFAB.show();
            hangFAB.setVisibility(View.VISIBLE);

            relativeVideo.setVisibility(View.VISIBLE);
            linearArrowVideo.setVisibility(GONE);
            videoFAB.setVisibility(View.VISIBLE);

            videoFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.accentColor)));
            videoFAB.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_videocam_white));

            microFAB.setVisibility(GONE);

            if (callChat.hasVideoInitialCall()) {
                RelativeLayout.LayoutParams layoutExtend = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                layoutExtend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutExtend.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                linearFAB.setLayoutParams(layoutExtend);
                linearFAB.requestLayout();
                linearFAB.setOrientation(LinearLayout.HORIZONTAL);

                answerCallFAB.setOnClickListener(this);
                videoFAB.setOnClickListener(null);

                linearArrowVideo.setVisibility(View.VISIBLE);
                videoFAB.startAnimation(shake);

                animationAlphaArrows(fourArrowVideo);
                handlerArrow1 = new Handler();
                handlerArrow1.postDelayed(new Runnable() {
                    public void run() {
                        animationAlphaArrows(thirdArrowVideo);
                        handlerArrow2 = new Handler();
                        handlerArrow2.postDelayed(new Runnable() {
                            public void run() {
                                animationAlphaArrows(secondArrowVideo);
                                handlerArrow3 = new Handler();
                                handlerArrow3.postDelayed(new Runnable() {
                                    public void run() {
                                        animationAlphaArrows(firstArrowVideo);
                                    }
                                }, 250);
                            }
                        }, 250);
                    }
                }, 250);


                videoFAB.setOnTouchListener(new OnSwipeTouchListener(this) {
                    public void onSwipeTop() {
                        log("onSwipeTop");

                        videoFAB.clearAnimation();

                        TranslateAnimation translateAnim = new TranslateAnimation( 0, 0 , 0, -380 );
                        translateAnim.setDuration(translationAnimationDuration);
                        translateAnim.setFillAfter(true);
                        translateAnim.setFillBefore(true);
                        translateAnim.setRepeatCount(0);

                        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
                        alphaAnim.setDuration(alphaAnimationDuration);
                        alphaAnim.setFillAfter(true);
                        alphaAnim.setFillBefore(true);
                        alphaAnim.setRepeatCount(0);

                        AnimationSet s = new AnimationSet(false);//false means don't share interpolators
                        s.addAnimation(translateAnim);
                        s.addAnimation(alphaAnim);

                        videoFAB.startAnimation(s);
                        firstArrowVideo.clearAnimation();
                        secondArrowVideo.clearAnimation();
                        thirdArrowVideo.clearAnimation();
                        fourArrowVideo.clearAnimation();
                        linearArrowVideo.setVisibility(GONE);

                        translateAnim.setAnimationListener(new Animation.AnimationListener(){
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                                linearFAB.setOrientation(LinearLayout.HORIZONTAL);
                                RelativeLayout.LayoutParams layoutCompress = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                layoutCompress.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                                layoutCompress.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                                linearFAB.setLayoutParams(layoutCompress);
                                linearFAB.requestLayout();
                                answerVideoCall();
                            }
                        });
                    }
                    public void onSwipeRight() {}
                    public void onSwipeLeft() {}
                    public void onSwipeBottom() {}

                });


            }else{

                RelativeLayout.LayoutParams layoutExtend = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                layoutExtend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutExtend.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                linearFAB.setLayoutParams(layoutExtend);
                linearFAB.requestLayout();
                linearFAB.setOrientation(LinearLayout.HORIZONTAL);

                answerCallFAB.startAnimation(shake);

                linearArrowCall.setVisibility(View.VISIBLE);
                animationAlphaArrows(fourArrowCall);
                handlerArrow4 = new Handler();
                handlerArrow4.postDelayed(new Runnable() {
                    public void run() {
                        animationAlphaArrows(thirdArrowCall);
                        handlerArrow5 = new Handler();
                        handlerArrow5.postDelayed(new Runnable() {
                            public void run() {
                                animationAlphaArrows(secondArrowCall);
                                handlerArrow6 = new Handler();
                                handlerArrow6.postDelayed(new Runnable() {
                                    public void run() {
                                        animationAlphaArrows(firstArrowCall);
                                    }
                                }, 250);
                            }
                        }, 250);
                    }
                }, 250);

                answerCallFAB.setOnTouchListener(new OnSwipeTouchListener(this) {
                    public void onSwipeTop() {
                        log("onSwipeTop");
                        answerCallFAB.clearAnimation();

                        TranslateAnimation translateAnim = new TranslateAnimation( 0, 0 , 0, -380 );
                        translateAnim.setDuration(translationAnimationDuration);
                        translateAnim.setFillAfter(true);
                        translateAnim.setFillBefore(true);
                        translateAnim.setRepeatCount(0);

                        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
                        alphaAnim.setDuration(alphaAnimationDuration);
                        alphaAnim.setFillAfter(true);
                        alphaAnim.setFillBefore(true);
                        alphaAnim.setRepeatCount(0);

                        AnimationSet s = new AnimationSet(false);//false means don't share interpolators
                        s.addAnimation(translateAnim);
                        s.addAnimation(alphaAnim);

                        answerCallFAB.startAnimation(s);
                        firstArrowCall.clearAnimation();
                        secondArrowCall.clearAnimation();
                        thirdArrowCall.clearAnimation();
                        fourArrowCall.clearAnimation();
                        linearArrowCall.setVisibility(GONE);

                        translateAnim.setAnimationListener(new Animation.AnimationListener(){
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                RelativeLayout.LayoutParams layoutCompress = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                layoutCompress.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                                layoutCompress.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                                linearFAB.setLayoutParams(layoutCompress);
                                linearFAB.requestLayout();
                                linearFAB.setOrientation(LinearLayout.HORIZONTAL);

                                answerAudioCall();
                            }
                        });
                    }
                    public void onSwipeRight() {}
                    public void onSwipeLeft() {}
                    public void onSwipeBottom() {}

                });

            }

        }else{
            relativeVideo.setVisibility(View.VISIBLE);
            videoFAB.show();
            videoFAB.setVisibility(View.VISIBLE);

            microFAB.show();
            microFAB.setVisibility(View.VISIBLE);

            relativeCall.setVisibility(GONE);
            answerCallFAB.setVisibility(GONE);

            hangFAB.show();
            hangFAB.setVisibility(View.VISIBLE);
        }

        if((bigRecyclerViewLayout!=null)&&(bigRecyclerView!=null)&&(parentBigCameraGroupCall!=null)){
            RelativeLayout.LayoutParams bigRecyclerViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            bigRecyclerViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            bigRecyclerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.parent_layout_big_camera_group_call);
            bigRecyclerViewParams.addRule(RelativeLayout.BELOW, 0);
            bigRecyclerViewLayout.setLayoutParams(bigRecyclerViewParams);
            bigRecyclerViewLayout.requestLayout();
        }

    }

    public void updateLocalVideoStatus(){
        log("updateLocalVideoStatus");
        int callStatus = callChat.getStatus();
        if(chat.isGroup()){
            log("is group");
            if(callChat !=null){
                if (callChat.hasLocalVideo()) {
                    videoFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.accentColor)));
                    videoFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_videocam_white));
                    if ((peersOnCall != null)&& (peersOnCall.size()!=0)) {
                        int position = peersOnCall.size()-1;
                        InfoPeerGroupCall item = peersOnCall.get(position);
                        if(!item.isVideoOn()){
                            log("activate Local Video for "+peersOnCall.get(position).getName());
                            item.setVideoOn(true);
                            if(peersOnCall.size()<7){
                                if(adapterGrid!=null){
                                    adapterGrid.notifyItemChanged(position);
                                }else{
                                    updatePeers(true);
                                }
                            }else{
                                if(adapterList!=null){
                                    adapterList.notifyItemChanged(position);
                                }else{
                                    updatePeers(true);
                                }
                            }
                            updateSubtitleToolbar();
                        }

                    }
                }else {
                    log("Video local NOT connected");
                    videoFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disable_fab_chat_call)));
                    videoFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_off));
                    if ((peersOnCall != null) && (peersOnCall.size()!=0)) {
                        int position = peersOnCall.size()-1;
                        InfoPeerGroupCall item = peersOnCall.get(position);
                        if(item.isVideoOn()){
                            log("desactivate Local Video fot "+peersOnCall.get(position).getName());
                            item.setVideoOn(false);
                            if(peersOnCall.size()<7){
                                if(adapterGrid!=null){
                                    adapterGrid.notifyItemChanged(position);
                                }else{
                                    updatePeers(true);
                                }
                            }else{
                                if(adapterList!=null){
                                    adapterList.notifyItemChanged(position);
                                }else{
                                    updatePeers(true);
                                }
                            }
                            updateSubtitleToolbar();
                        }
                    }
                }
            }
        }else{
            log("is individual");
            if (callChat.hasLocalVideo()) {
                log("Video local connected: chatId: "+chatId);
                videoFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.accentColor)));
                videoFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_videocam_white));

                if(callStatus==MegaChatCall.CALL_STATUS_REQUEST_SENT){
                    log("callStatus: CALL_STATUS_REQUEST_SENT");

                    if(localCameraFragmentFS == null){
                        log("create localCameraFragmentFS");
                        localCameraFragmentFS = LocalCameraCallFullScreenFragment.newInstance(chatId);
                        FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
                        ftFS.replace(R.id.fragment_container_local_cameraFS, localCameraFragmentFS, "localCameraFragmentFS");
                        ftFS.commitNowAllowingStateLoss();
                    }
                    contactAvatarLayout.setVisibility(GONE);
                    parentLocalFS.setVisibility(View.VISIBLE);
                    fragmentContainerLocalCameraFS.setVisibility(View.VISIBLE);

                }else if(callStatus==MegaChatCall.CALL_STATUS_IN_PROGRESS){
                    log("callStatus: CALL_STATUS_IN_PROGRESS");

                    if(localCameraFragment == null){
                        log("create localCameraFragment");
                        localCameraFragment = LocalCameraCallFragment.newInstance(chatId);
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container_local_camera, localCameraFragment, "localCameraFragment");
                        ft.commitNowAllowingStateLoss();

                    }
                    myAvatarLayout.setVisibility(GONE);
                    parentLocal.setVisibility(View.VISIBLE);
                    fragmentContainerLocalCamera.setVisibility(View.VISIBLE);
                }

            }else {
                log("Video local NOT connected: chatId: "+chatId);
                videoFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disable_fab_chat_call)));
                videoFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_off));

                if(callStatus==MegaChatCall.CALL_STATUS_REQUEST_SENT){
                    log("callStatus: CALL_STATUS_REQUEST_SENT");

                    if (localCameraFragmentFS != null) {
                        log("remove localCameraFragmentFS");
                        localCameraFragmentFS.removeSurfaceView();
                        FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
                        ftFS.remove(localCameraFragmentFS);
                        localCameraFragmentFS = null;
                    }
                    parentLocalFS.setVisibility(View.GONE);
                    fragmentContainerLocalCameraFS.setVisibility(View.GONE);
                    contactAvatarLayout.setVisibility(View.VISIBLE);

                }else if(callStatus==MegaChatCall.CALL_STATUS_IN_PROGRESS){
                    log("callStatus: CALL_STATUS_IN_PROGRESS ");
                    if (localCameraFragment != null) {
                        log("remove localCameraFragment");
                        localCameraFragment.removeSurfaceView();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.remove(localCameraFragment);
                        localCameraFragment = null;
                    }
                    parentLocal.setVisibility(View.GONE);
                    fragmentContainerLocalCamera.setVisibility(View.GONE);
                    myAvatarLayout.setVisibility(View.VISIBLE);

                }
            }
        }
    }

    public void updateLocalAudioStatus(){
        log("updateLocalAudioStatus");
        if(chat.isGroup()) {
            log("is group");
            if (callChat != null) {
                if(callChat.hasLocalAudio()){
                    log("Audio local connected");
                    microFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accentColor)));
                    microFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_record_audio_w));
                    if (peersOnCall != null && !peersOnCall.isEmpty()) {
                        int position = peersOnCall.size()-1;
                        if(!peersOnCall.get(position).isAudioOn()){
                            peersOnCall.get(position).setAudioOn(true);
                            if(peersOnCall.size()<7){
                                if(adapterGrid!=null){
                                    adapterGrid.changesInAudio(position,null);
                                }else{
                                    updatePeers(true);
                                }
                            }else{
                                if(adapterList!=null){
                                    adapterList.changesInAudio(position,null);
                                }else{
                                    updatePeers(true);
                                }
                            }
                        }
                    }
                }else{
                    log("Audio local NOT connected");
                    microFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disable_fab_chat_call)));
                    microFAB.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_mic_off));
                    if (peersOnCall != null && !peersOnCall.isEmpty()) {
                        int position = peersOnCall.size()-1;
                        if(peersOnCall.get(position).isAudioOn()){
                            peersOnCall.get(position).setAudioOn(false);
                            if(peersOnCall.size()<7){
                                if(adapterGrid!=null){
                                    adapterGrid.changesInAudio(position,null);
                                }else{
                                    updatePeers(true);
                                }
                            }else{
                                if(adapterList!=null){
                                    adapterList.changesInAudio(position,null);
                                }else{
                                    updatePeers(true);
                                }
                            }
                        }
                    }

                }
            }

        }else {
            log("is individual");
            if(callChat.hasLocalAudio()){
                log("Audio local connected");
                microFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accentColor)));
                microFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_record_audio_w));
            }else{
                log("Audio local NOT connected");
                microFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disable_fab_chat_call)));
                microFAB.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_mic_off));
            }
        }

    }

    public void updateRemoteVideoStatus(long userHandle){
        log("updateRemoteVideoStatus(): "+userHandle);
        if(chat.isGroup()){
            log("isGroup()");

            MegaChatSession userSession = callChat.getMegaChatSession(userHandle);
            if(userSession!=null && userSession.hasVideo()) {
                log(userHandle+": Video remote connected");
                if ((peersOnCall != null) && (peersOnCall.size()!=0)) {
                    for(int i=0;i<peersOnCall.size();i++){
                        log("userHandle: "+userHandle+" VS i: "+peersOnCall.get(i).getHandle()+" ("+peersOnCall.get(i).getName()+")");
                        if(peersOnCall.get(i).getHandle() == userHandle){
                            if(!peersOnCall.get(i).isVideoOn()){
                                log("updateRemoteVideo()- "+peersOnCall.get(i).getName()+" Connected video");
                                peersOnCall.get(i).setVideoOn(true);
                                updateSubtitleToolbar();
                                if(peersOnCall.size()<7){
                                    if(adapterGrid!=null){
                                        adapterGrid.notifyItemChanged(i);
                                    }else{
                                        updatePeers(true);
                                    }
                                }else{
                                    if(adapterList != null){
                                        adapterList.notifyItemChanged(i);
                                        if(peerSelected != null){
                                            if(peerSelected.getHandle() == userHandle){
                                                createBigFragment(peerSelected.getHandle());
                                                if(peerSelected.isAudioOn()){
                                                    //Disable audio icon GONE
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }else{
                                                    //Disable audio icon VISIBLE
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }
                                    }else{
                                        updatePeers(true);
                                        if(peerSelected != null){
                                            if(peerSelected.getHandle() == userHandle){
                                                createBigFragment(peerSelected.getHandle());
                                                if(peerSelected.isAudioOn()){
                                                    //Disable audio icon GONE
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }else{
                                                    //Disable audio icon VISIBLE
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }else{
                log(userHandle+": Video remote NOT connected");
                if ((peersOnCall != null) && (peersOnCall.size() != 0)) {
                    for(int i=0;i<peersOnCall.size();i++){
                        if(peersOnCall.get(i).getHandle() == userHandle){
                            if(peersOnCall.get(i).isVideoOn()){
                                peersOnCall.get(i).setVideoOn(false);
                                log("updateRemoteVideo()- "+peersOnCall.get(i).getName()+" Disconnected video");
                                updateSubtitleToolbar();
                                if(peersOnCall.size()<7){
                                    if(adapterGrid!=null){
                                        adapterGrid.notifyItemChanged(i);
                                    }else{
                                        updatePeers(true);
                                    }
                                }else{
                                    if(adapterList!=null){
                                        adapterList.notifyItemChanged(i);
                                        if(peerSelected != null){
                                            if(peerSelected.getHandle() == userHandle){
                                                createBigAvatar(peerSelected.getHandle(),peerSelected.getName());
                                                if(peerSelected.isAudioOn()){
                                                    //Disable audio icon GONE
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }else{
                                                    //Disable audio icon VISIBLE
                                                    avatarBigCameraGroupCallMicro.setVisibility(View.VISIBLE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }
                                            }
                                        }
                                    }else{
                                        updatePeers(true);
                                        if(peerSelected != null){
                                            if(peerSelected.getHandle() == userHandle){
                                                createBigAvatar(peerSelected.getHandle(),peerSelected.getName());
                                                if(peerSelected.isAudioOn()){
                                                    //Disable audio icon GONE
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }else{
                                                    //Disable audio icon VISIBLE
                                                    avatarBigCameraGroupCallMicro.setVisibility(View.VISIBLE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }
                                            }
                                        }
                                    }
                                }

                                break;
                            }
                        }
                    }
                }
            }
        }else{
            log("is individual");
            MegaChatSession userSession = callChat.getMegaChatSession(chat.getPeerHandle(0));

            if(isRemoteVideo == REMOTE_VIDEO_NOT_INIT){

                if(userSession!=null && userSession.hasVideo()){
                    log("Video remote connected");
                    isRemoteVideo = REMOTE_VIDEO_ENABLED;

                    if(remoteCameraFragmentFS == null){
                        log("create remoteCameraFragmentFS");
                        remoteCameraFragmentFS = RemoteCameraCallFullScreenFragment.newInstance(chatId, chat.getPeerHandle(0));
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container_remote_cameraFS, remoteCameraFragmentFS, "remoteCameraFragmentFS");
                        ft.commitNowAllowingStateLoss();
                    }
                    contactAvatarLayout.setOnClickListener(null);
                    contactAvatarLayout.setVisibility(GONE);
                    parentRemoteFS.setVisibility(View.VISIBLE);
                    fragmentContainerRemoteCameraFS.setVisibility(View.VISIBLE);

                }else{
                    log("Video remote NOT connected");
                    isRemoteVideo = REMOTE_VIDEO_DISABLED;
                    if (remoteCameraFragmentFS != null) {
                        log("remove remoteCameraFragmentFS");
                        remoteCameraFragmentFS.removeSurfaceView();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.remove(remoteCameraFragmentFS);
                        remoteCameraFragmentFS = null;
                    }
                    contactAvatarLayout.setVisibility(View.VISIBLE);
                    contactAvatarLayout.setOnClickListener(this);
                    parentRemoteFS.setVisibility(View.GONE);
                    fragmentContainerRemoteCameraFS.setVisibility(View.GONE);
                }
            }else{
                if((isRemoteVideo==REMOTE_VIDEO_ENABLED)&&(userSession!=null)&&(!userSession.hasVideo())){
                    isRemoteVideo = REMOTE_VIDEO_DISABLED;

                    if (remoteCameraFragmentFS != null) {
                        log("remove remoteCameraFragmentFS");
                        remoteCameraFragmentFS.removeSurfaceView();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.remove(remoteCameraFragmentFS);
                        remoteCameraFragmentFS = null;
                    }
                    contactAvatarLayout.setVisibility(View.VISIBLE);
                    contactAvatarLayout.setOnClickListener(this);
                    parentRemoteFS.setVisibility(View.GONE);
                    fragmentContainerRemoteCameraFS.setVisibility(View.GONE);

                }else if((isRemoteVideo==REMOTE_VIDEO_DISABLED)&&(userSession!=null)&&(userSession.hasVideo())){
                    isRemoteVideo = REMOTE_VIDEO_ENABLED;

                    if(remoteCameraFragmentFS == null){
                        log("create remoteCameraFragmentFS");
                        remoteCameraFragmentFS = RemoteCameraCallFullScreenFragment.newInstance(chatId, chat.getPeerHandle(0));
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container_remote_cameraFS, remoteCameraFragmentFS, "remoteCameraFragmentFS");
                        ft.commitNowAllowingStateLoss();
                    }
                    contactAvatarLayout.setOnClickListener(null);
                    contactAvatarLayout.setVisibility(GONE);
                    parentRemoteFS.setVisibility(View.VISIBLE);
                    fragmentContainerRemoteCameraFS.setVisibility(View.VISIBLE);

                }
            }
        }
    }

    public void updateRemoteAudioStatus(long userHandle){
        log("updateRemoteAudioStatus");
        if(chat.isGroup()){
            if(chat.isGroup()){
                MegaChatSession userSession = callChat.getMegaChatSession(userHandle);
                if(userSession!=null && userSession.hasAudio()) {
                    log(userHandle+": Audio remote connected");
                    if (peersOnCall != null && !peersOnCall.isEmpty()) {
                        for(int i=0;i<peersOnCall.size();i++){
                            if(peersOnCall.get(i).getHandle().equals(userHandle)){
                                if(!peersOnCall.get(i).isAudioOn()){
                                    peersOnCall.get(i).setAudioOn(true);
                                    if(peersOnCall.size()<7){
                                        if(adapterGrid!=null) {
                                            adapterGrid.changesInAudio(i, null);
                                        }else{
                                            updatePeers(true);
                                        }
                                    }else{
                                        if(adapterList!=null){
                                            adapterList.changesInAudio(i,null);
                                            if(peerSelected!=null){
                                                if(peerSelected.getHandle().equals(userHandle)){
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }
                                            }

                                        }else{
                                            updatePeers(true);
                                            if(peerSelected!=null){
                                                if(peerSelected.getHandle().equals(userHandle)){
                                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                }
                                            }
                                        }

                                    }
                                    break;
                                }
                            }
                        }
                    }
                }else {
                    log(userHandle+": Audio remote NOT connected");
                    if (peersOnCall != null && !peersOnCall.isEmpty()) {
                        for(int i=0;i<peersOnCall.size();i++){
                            if(peersOnCall.get(i).getHandle().equals(userHandle)){
                                if(peersOnCall.get(i).isAudioOn()){
                                    peersOnCall.get(i).setAudioOn(false);

                                    if(peersOnCall.size()<7){
                                        if(adapterGrid!=null){
                                            adapterGrid.changesInAudio(i,null);
                                        }else{
                                            updatePeers(true);
                                        }
                                    }else{
                                        if(adapterList!=null){
                                            adapterList.changesInAudio(i,null);
                                            if(peerSelected != null){
                                                if(peerSelected.getHandle().equals(userHandle)){
                                                    if(peerSelected.isVideoOn()){
                                                        avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                        microFragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
                                                    }else{
                                                        avatarBigCameraGroupCallMicro.setVisibility(View.VISIBLE);
                                                        microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                    }
                                                }
                                            }
                                        }else{
                                            updatePeers(true);
                                            if(peerSelected != null){
                                                if(peerSelected.getHandle().equals(userHandle)){
                                                    if(peerSelected.isVideoOn()){
                                                        avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                                        microFragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
                                                    }else{
                                                        avatarBigCameraGroupCallMicro.setVisibility(View.VISIBLE);
                                                        microFragmentBigCameraGroupCall.setVisibility(GONE);
                                                    }
                                                }
                                            }
                                        }

                                    }
                                    break;
                                }
                            }
                        }
                    }

                }
            }
        }else{
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        log("onRequestPermissionsResult() ");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CAMERA: {
                log("REQUEST_CAMERA");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissions()){
                       showInitialFABConfiguration();
                    }
                }
                else{
                    hangFAB.setVisibility(View.VISIBLE);
                }
                break;
            }
            case Constants.RECORD_AUDIO: {
                log("RECORD_AUDIO");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissions()){
                        showInitialFABConfiguration();
                    }
                }
                else{
                    hangFAB.setVisibility(View.VISIBLE);
                }
                break;
            }

            case Constants.WRITE_LOG: {
                log("WRITE_LOG");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionsWriteLog()){
                        log("accepted WRITE_LOG permissions");
                    }else{
                        log("rejected WRITE_LOG permissions");
                    }
                }
                break;
            }
        }
    }

    public static void log(String message) {
        Util.log("ChatCallActivity", message);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.values[0] == 0) {
            //Turn off Screen
            if(!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        } else {
            //Turn on Screen
            if(wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    public void remoteCameraClick(){
        log("remoteCameraClick");
        if(callChat.getStatus()==MegaChatCall.CALL_STATUS_IN_PROGRESS) {
            if (aB.isShowing()) {
                hideActionBar();
                hideFABs();
            } else {
                showActionBar();
                showInitialFABConfiguration();
            }
        }
    }

    public void itemClicked(InfoPeerGroupCall peer){
        log("itemClicked->  userSelected: "+peerSelected.getName());
        if(peerSelected.getHandle() == peer.getHandle()){
            //I touched the same user that is now in big fragment:
            if(isManualMode){
                isManualMode = false;
                log("manual mode - False");
            }else{
                isManualMode = true;
                log("manual mode - True");

            }
            if(adapterList!=null){
                adapterList.updateMode(isManualMode);
                for(int i=0;i<peersOnCall.size();i++){
                    if(peersOnCall.get(i).getHandle() == peer.getHandle()){
                        peersOnCall.get(i).setGreenLayer(true);
                        adapterList.changesInGreenLayer(i,null);
                    }else{
                        if(peersOnCall.get(i).hasGreenLayer()){
                            peersOnCall.get(i).setGreenLayer(false);
                            adapterList.changesInGreenLayer(i,null);
                        }
                    }
                }
            }

        }else{
            if(peer.getHandle() != megaChatApi.getMyUserHandle()){
                if(!isManualMode) {
                    isManualMode = true;
                    if(adapterList!=null){
                        adapterList.updateMode(true);
                    }
                    log("manual mode - True");
                }
                peerSelected = peer;
                updateUserSelected(true);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public static String getDateFromMillis(long d) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(d);
    }

    private void startClock(){
        log("startClock");
        long seconds = 0;

        if(callChat!=null){
            seconds = callChat.getDuration();
        }
        long baseTime = SystemClock.uptimeMillis() - (seconds*1000);
        myChrono.setBase(baseTime);
        customHandler.postDelayed(updateTimerThread, 1000);
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            long elapsedTime = SystemClock.uptimeMillis() - myChrono.getBase();
//            aB.setSubtitle(getDateFromMillis(elapsedTime));
            subtitleToobar.setText(getDateFromMillis(elapsedTime));
            customHandler.postDelayed(this, 0);
        }
    };

    private class MyRingerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    if (ringtone != null && !ringtone.isPlaying())
                    {
                        ringtone.play();
                    }
                }});
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_UP: {
                try {
                    if (callChat.getStatus() == MegaChatCall.CALL_STATUS_RING_IN) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    } else if (callChat.getStatus() == MegaChatCall.CALL_STATUS_REQUEST_SENT) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    } else {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                    }
                }
                catch(SecurityException e) {
                    return super.onKeyDown(keyCode, event);
                }

                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                try {
                    if (callChat.getStatus() == MegaChatCall.CALL_STATUS_RING_IN) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    } else if (callChat.getStatus() == MegaChatCall.CALL_STATUS_REQUEST_SENT) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    } else {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                    }
                } catch(SecurityException e) {
                    return super.onKeyDown(keyCode, event);
                }

                return true;
            }
            default: {
                return super.onKeyDown(keyCode, event);
            }
        }
    }

    public void answerAudioCall(){
        log("answerAudioCall");
        clearHandlers();

        if (megaChatApi.isSignalActivityRequired()) {
            megaChatApi.signalPresenceActivity();
        }
        megaChatApi.answerChatCall(chatId, false, this);
    }
    public void answerVideoCall(){
        log("answerVideoCall");
        clearHandlers();
        if (megaChatApi.isSignalActivityRequired()) {
            megaChatApi.signalPresenceActivity();
        }
        megaChatApi.answerChatCall(chatId, true, this);
    }

    public void animationAlphaArrows(final ImageView arrow){
        AlphaAnimation alphaAnimArrows = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimArrows.setDuration(alphaAnimationDurationArrow);
        alphaAnimArrows.setFillAfter(true);
        alphaAnimArrows.setFillBefore(true);
        alphaAnimArrows.setRepeatCount(Animation.INFINITE);
        arrow.startAnimation(alphaAnimArrows);
    }

    public void updateSubtitleToolbar(){
        log("updateSubtitleToolbar() ");
        int cont = 0;
        if(peersOnCall!=null){
            for(int i=0;i<peersOnCall.size();i++){
                if(peersOnCall.get(i).isVideoOn()){
                    cont++;
                }
            }
        }
        participantText.setText(cont+"/6");
        linearParticipants.setVisibility(View.VISIBLE);
    }

    public void updatePeers(boolean flag){
        log("updatePeers()");

        if(flag){
            log("* updatePeers()  in progress call: peersOnCall("+peersOnCall.size()+")");

            if((peersBeforeCall != null)&&(peersBeforeCall.size() != 0)){
                peersBeforeCall.clear();
            }

            //Call IN PROGRESS
            if(peersOnCall.size() < 7){
                //1-6
                if(peersOnCall.size()!=0){
                    if(adapterList!=null){
                        log("updatePeers()  destroy adapterList)");
                        adapterList.onDestroy();
                        adapterList = null;
                    }

                    if (bigCameraGroupCallFragment != null) {
                        log("updatePeers()  destroy bigCameraGroupCallFragment)");
                        bigCameraGroupCallFragment.removeSurfaceView();
                        FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
                        ftFS.remove(bigCameraGroupCallFragment);
                        bigCameraGroupCallFragment = null;
                    }
                    avatarBigCameraGroupCallLayout.setVisibility(View.GONE);
                    bigRecyclerView.setAdapter(null);
                    bigRecyclerView.setVisibility(GONE);
                    bigRecyclerViewLayout.setVisibility(GONE);

                    parentBigCameraGroupCall.setOnClickListener(null);
                    parentBigCameraGroupCall.setVisibility(View.GONE);
                    recyclerViewLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    if(peersOnCall.size()< 4){
                        recyclerViewLayout.setPadding(0,0,0,0);
                        recyclerView.setColumnWidth((int) widthScreenPX);
                    }else{
                        if(peersOnCall.size() == 4){
                            recyclerViewLayout.setPadding(0,Util.scaleWidthPx(136, outMetrics),0,0);
                        }else{
                            recyclerViewLayout.setPadding(0,0,0,0);
                        }
                        recyclerView.setColumnWidth((int) widthScreenPX/2);
                    }
                    if(adapterGrid == null){
                        log("(1-6) in progress call - create adapter");
                        recyclerView.setAdapter(null);
                        adapterGrid = new GroupCallAdapter(this, recyclerView, peersOnCall, chatId, flag, true);
                        recyclerView.setAdapter(adapterGrid);
                    }else{
                        if(isNecessaryCreateAdapter){
                            log("(1-6) in progress call - create adapter");
                            adapterGrid.onDestroy();
                            recyclerView.setAdapter(null);
                            adapterGrid = new GroupCallAdapter(this, recyclerView, peersOnCall, chatId, flag, true);
                            recyclerView.setAdapter(adapterGrid);
                        }else{
                            log("(1-6) in progress call - notifyDataSetChanged");
                            adapterGrid.notifyDataSetChanged();
                        }
                    }

                }else{
                    if(adapterGrid != null){
                        adapterGrid.onDestroy();
                        adapterGrid = null;
                    }
                    if(adapterList!=null){
                        adapterList.onDestroy();
                        adapterList =null;
                    }
                    recyclerView.setAdapter(null);
                    recyclerView.setVisibility(View.GONE);
                    recyclerViewLayout.setVisibility(View.GONE);
                    bigRecyclerView.setAdapter(null);
                    bigRecyclerView.setVisibility(GONE);
                    bigRecyclerViewLayout.setVisibility(GONE);

                    parentBigCameraGroupCall.setOnClickListener(null);
                    parentBigCameraGroupCall.setVisibility(View.GONE);
                }

            }else{

                //7 +
                if(adapterGrid != null){
                    adapterGrid.onDestroy();
                    adapterGrid = null;
                }
                recyclerView.setAdapter(null);
                recyclerView.setVisibility(GONE);
                recyclerViewLayout.setVisibility(View.GONE);
                parentBigCameraGroupCall.setOnClickListener(this);
                parentBigCameraGroupCall.setVisibility(View.VISIBLE);

                bigRecyclerViewLayout.setVisibility(View.VISIBLE);
                bigRecyclerView.setVisibility(View.VISIBLE);

                if(adapterList == null){
                    log("(7 +) in progress call - create adapter");
                    bigRecyclerView.setAdapter(null);
                    adapterList = new GroupCallAdapter(this, bigRecyclerView, peersOnCall, chatId, flag, false);
                    bigRecyclerView.setAdapter(adapterList);
                }else{
                    if(isNecessaryCreateAdapter){
                        log("(7 +) in progress call - create adapter");
                        adapterList.onDestroy();
                        bigRecyclerView.setAdapter(null);
                        adapterList = new GroupCallAdapter(this, bigRecyclerView, peersOnCall, chatId, flag, false);
                        bigRecyclerView.setAdapter(adapterList);
                    }else{
                        log("(7 +) in progress call - notifyDataSetChanged");
                        adapterList.notifyDataSetChanged();
                    }
                }
                updateUserSelected(flag);
            }

            isNecessaryCreateAdapter = false;

        }else{

            isNecessaryCreateAdapter = true;
            //Call INCOMING
            log("updatePeers()  incoming call: peersBeforeCall("+peersBeforeCall.size()+")");

            linearParticipants.setVisibility(View.GONE);

            if(peersBeforeCall.size() < 7) {

                if(peersBeforeCall.size() != 0){
                    //1-6
                    if(adapterList != null){
                        adapterList.onDestroy();
                        adapterList = null;
                    }
                    avatarBigCameraGroupCallLayout.setVisibility(View.GONE);

                    bigRecyclerView.setAdapter(null);
                    bigRecyclerView.setVisibility(GONE);
                    bigRecyclerViewLayout.setVisibility(GONE);

                    parentBigCameraGroupCall.setOnClickListener(null);
                    parentBigCameraGroupCall.setVisibility(View.GONE);

                    recyclerViewLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    if(peersBeforeCall.size()< 4){
                        recyclerViewLayout.setPadding(0,0,0,0);
                        recyclerView.setColumnWidth((int) widthScreenPX);
                    }else{
                        if(peersBeforeCall.size() == 4){
                            recyclerViewLayout.setPadding(0,Util.scaleWidthPx(136, outMetrics),0,0);
                        }else{
                            recyclerViewLayout.setPadding(0,0,0,0);
                        }
                        recyclerView.setColumnWidth((int) widthScreenPX/2);
                    }
                    if(adapterGrid == null){
                        log("(1-6) incoming call - create adapter");
                        recyclerView.setAdapter(null);
                        adapterGrid = new GroupCallAdapter(this, recyclerView, peersBeforeCall, chatId, flag, true);
                        recyclerView.setAdapter(adapterGrid);
                    }else{
                        log("(1-6) incoming call - notifyDataSetChanged");
                        adapterGrid.notifyDataSetChanged();
                    }

                 }else{

                    if(adapterGrid != null){
                        adapterGrid.onDestroy();
                        adapterGrid = null;
                    }
                    if(adapterList != null){
                        adapterList.onDestroy();
                        adapterList = null;
                    }
                    recyclerView.setAdapter(null);
                    recyclerView.setVisibility(GONE);
                    recyclerViewLayout.setVisibility(View.GONE);

                    bigRecyclerView.setAdapter(null);
                    bigRecyclerView.setVisibility(GONE);
                    bigRecyclerViewLayout.setVisibility(GONE);

                    parentBigCameraGroupCall.setOnClickListener(null);
                    parentBigCameraGroupCall.setVisibility(View.GONE);
                }

            }else{

                //7 +
                if(adapterGrid != null){
                    adapterGrid.onDestroy();
                    adapterGrid = null;
                }
                recyclerView.setAdapter(null);
                recyclerView.setVisibility(GONE);
                recyclerViewLayout.setVisibility(View.GONE);

                parentBigCameraGroupCall.setOnClickListener(this);
                parentBigCameraGroupCall.setVisibility(View.VISIBLE);

                bigRecyclerViewLayout.setVisibility(View.VISIBLE);
                bigRecyclerView.setVisibility(View.VISIBLE);
                if(adapterList==null){
                    log("(7 +) incoming call - create adapter");
                    bigRecyclerView.setAdapter(null);
                    adapterList = new GroupCallAdapter(this, bigRecyclerView, peersBeforeCall, chatId, flag, false);
                    bigRecyclerView.setAdapter(adapterList);
                }else{
                    log("(7 +) incoming call - notifyDataSetChanged");
                    adapterList.notifyDataSetChanged();
                }

                updateUserSelected(flag);
            }
        }
    }

    public void updateUserSelected(boolean flag){
        log("updateUserSelected()");
        if(flag){
            //Call IN PROGRESS
            if(peerSelected == null){
                log("updateUserSelected() peerSelected == null");

                //First case:
                if(!isManualMode){

                    if((peersOnCall!=null)&&(peersOnCall.size()!=0)){
                        int position = 0;

                        peerSelected = peersOnCall.get(position);
                        log("updateUserSelected() InProgress - new peerSelected "+peerSelected.getHandle());

                        for(int i=0;i<peersOnCall.size();i++){
                            if(i == position){
                                if(!peersOnCall.get(position).hasGreenLayer()){
                                    peersOnCall.get(position).setGreenLayer(true);
                                    if(adapterList!=null){
                                        adapterList.changesInGreenLayer(position,null);
                                    }
                                }
                            }else{
                                if(peersOnCall.get(i).hasGreenLayer()){
                                    peersOnCall.get(i).setGreenLayer(false);
                                    if(adapterList!=null){
                                        adapterList.changesInGreenLayer(i,null);
                                    }
                                }
                            }
                        }
                        if(peerSelected.isVideoOn()){
                            //Video ON
                            createBigFragment(peerSelected.getHandle());
                            if(peerSelected.isAudioOn()){
                                //Disable audio icon GONE
                                avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                microFragmentBigCameraGroupCall.setVisibility(GONE);
                            }else{
                                //Disable audio icon VISIBLE
                                avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                microFragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
                            }
                        }else{
                            //Video OFF
                            createBigAvatar(peerSelected.getHandle(), peerSelected.getName());
                            if(peerSelected.isAudioOn()){
                                //Disable audio icon GONE
                                avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                microFragmentBigCameraGroupCall.setVisibility(GONE);
                            }else{
                                //Disable audio icon VISIBLE
                                avatarBigCameraGroupCallMicro.setVisibility(View.VISIBLE);
                                microFragmentBigCameraGroupCall.setVisibility(GONE);
                            }
                        }
                    }
                }
            }else{
                log("updateUserSelected() peerSelected != null");

                //find if peerSelected is removed:
                if((peersOnCall!=null)&&(peersOnCall.size()!=0)){
                    boolean peerContained = false;
                    for(int i=0; i<peersOnCall.size(); i++){
                        if(peersOnCall.get(i).getHandle() == peerSelected.getHandle()){
                            peerContained = true;
                            break;
                        }
                    }
                    if(!peerContained){
                        //it was removed
                        if((peersOnCall!=null)&&(peersOnCall.size()!=0)){
                            int position = 0;
                            peerSelected = peersOnCall.get(position);
                            log("updateUserSelected() InProgress - new peerSelected: "+peerSelected.getHandle());

                            for(int i=0;i<peersOnCall.size();i++){
                                if(i == position){
                                    isManualMode = false;
                                    if(adapterList!=null){
                                        adapterList.updateMode(false);
                                    }
                                    if(!peersOnCall.get(position).hasGreenLayer()){
                                        peersOnCall.get(position).setGreenLayer(true);
                                        if(adapterList!=null){
                                            adapterList.changesInGreenLayer(position,null);
                                        }
                                    }
                                }else{
                                    if(peersOnCall.get(i).hasGreenLayer()){
                                        peersOnCall.get(i).setGreenLayer(false);
                                        if(adapterList!=null){
                                            adapterList.changesInGreenLayer(i,null);
                                        }
                                    }
                                }
                            }

                            if(peerSelected.isVideoOn()){
                                //Video ON
                                createBigFragment(peerSelected.getHandle());
                                if(peerSelected.isAudioOn()){
                                    //Disable audio icon GONE
                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                }else{
                                    //Disable audio icon VISIBLE
                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                    microFragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
                                }
                            }else{
                                //Video OFF
                                createBigAvatar(peerSelected.getHandle(), peerSelected.getName());
                                if(peerSelected.isAudioOn()){
                                    //Disable audio icon GONE
                                    avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                }else{
                                    //Disable audio icon VISIBLE
                                    avatarBigCameraGroupCallMicro.setVisibility(View.VISIBLE);
                                    microFragmentBigCameraGroupCall.setVisibility(GONE);
                                }
                            }
                        }

                    }else{
                        log("updateUserSelected() InProgress - peerSelected: "+peerSelected.getHandle());

                        for(int i=0; i<peersOnCall.size(); i++){
                            if(peersOnCall.get(i).getHandle() == peerSelected.getHandle()){
                                    peersOnCall.get(i).setGreenLayer(true);
                                    if(adapterList!=null){
                                        adapterList.changesInGreenLayer(i,null);
                                    }

                            }else{
                                if(peersOnCall.get(i).hasGreenLayer()){
                                    peersOnCall.get(i).setGreenLayer(false);
                                    if(adapterList!=null){
                                        adapterList.changesInGreenLayer(i,null);
                                    }
                                }
                            }
                        }

                        if(peerSelected.isVideoOn()){
                            //Video ON
                            createBigFragment(peerSelected.getHandle());
                            if(peerSelected.isAudioOn()){
                                //Audio on, icon GONE
                                avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                microFragmentBigCameraGroupCall.setVisibility(GONE);
                            }else{
                                //Audio off, icon VISIBLE
                                avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                microFragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
                            }
                        }else{
                            //Video OFF
                            createBigAvatar(peerSelected.getHandle(), peerSelected.getName());
                            if(peerSelected.isAudioOn()){
                                //Audio on, icon GONE
                                avatarBigCameraGroupCallMicro.setVisibility(GONE);
                                microFragmentBigCameraGroupCall.setVisibility(GONE);
                            }else{
                                //Audio off, icon VISIBLE
                                avatarBigCameraGroupCallMicro.setVisibility(View.VISIBLE);
                                microFragmentBigCameraGroupCall.setVisibility(GONE);
                            }
                        }
                    }
                }
            }

        }else{
            log("updateUserSelected() Incoming");
            //Call INCOMING
            parentBigCameraGroupCall.setVisibility(View.VISIBLE);
            if(peerSelected == null){
                //First time:
                //Remove Camera element, because with incoming, avatar is the only showed
                if (bigCameraGroupCallFragment != null) {
                    bigCameraGroupCallFragment.removeSurfaceView();
                    FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
                    ftFS.remove(bigCameraGroupCallFragment);
                    bigCameraGroupCallFragment = null;
                }
                fragmentBigCameraGroupCall.setVisibility(View.GONE);

                //Create Avatar, get the last peer of peersBeforeCall
                if((peersBeforeCall!=null)&& (peersBeforeCall.size()!= 0)){
                    avatarBigCameraGroupCallLayout.setVisibility(View.VISIBLE);
                    InfoPeerGroupCall peerTemp = peersBeforeCall.get((peersBeforeCall.size())-1);
                    log("updateUserSelected() Incoming - peerTemp: "+peerTemp.getHandle());
                    setProfileContactAvatarGroupCall(peerTemp.getHandle(), peerTemp.getName());
                }else{
                    avatarBigCameraGroupCallLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    public void createBigFragment(Long handle){
        log("createBigFragment()");


        //Remove big Camera
        if(bigCameraGroupCallFragment != null){
            log("REMOVE bigCameraGroupCallFragment");
            bigCameraGroupCallFragment.removeSurfaceView();
            FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
            ftFS.remove(bigCameraGroupCallFragment);
            bigCameraGroupCallFragment = null;
        }

        //Create big Camera
        if(bigCameraGroupCallFragment == null){
            log("CREATE bigCameraGroupCallFragment");
            bigCameraGroupCallFragment = BigCameraGroupCallFragment.newInstance(chatId, handle);
            FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
            ftFS.replace(R.id.fragment_big_camera_group_call, bigCameraGroupCallFragment, "bigCameraGroupCallFragment");
            ftFS.commitNowAllowingStateLoss();
        }
        fragmentBigCameraGroupCall.setVisibility(View.VISIBLE);
        parentBigCameraGroupCall.setVisibility(View.VISIBLE);
        //Remove Avatar
        avatarBigCameraGroupCallLayout.setVisibility(View.GONE);
    }

    public void createBigAvatar(Long handle, String fullName){
        log("createBigAvatar()");
        //Remove big Camera
        if (bigCameraGroupCallFragment != null) {
            log("REMOVE bigCameraGroupCallFragment");
            bigCameraGroupCallFragment.removeSurfaceView();
            FragmentTransaction ftFS = getSupportFragmentManager().beginTransaction();
            ftFS.remove(bigCameraGroupCallFragment);
            bigCameraGroupCallFragment = null;
        }

        fragmentBigCameraGroupCall.setVisibility(View.GONE);

        //Create Avatar
        avatarBigCameraGroupCallImage.setImageBitmap(null);
        setProfileContactAvatarGroupCall(handle,fullName);
        parentBigCameraGroupCall.setVisibility(View.VISIBLE);
        avatarBigCameraGroupCallLayout.setVisibility(View.VISIBLE);
    }

    public void setProfileContactAvatarGroupCall(long userHandle,  String fullName){
        log("setProfileContactAvatarGroupCall");
        Bitmap bitmap = null;
        File avatar = null;
        String contactMail = megaChatApi.getContactEmail(userHandle);
        if (context.getExternalCacheDir() != null) {
            avatar = new File(context.getExternalCacheDir().getAbsolutePath(), contactMail + ".jpg");
        } else {
            avatar = new File(context.getCacheDir().getAbsolutePath(), contactMail + ".jpg");
        }

        if (avatar.exists()) {
            if (avatar.length() > 0) {
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                bitmap = ThumbnailUtilsLollipop.getRoundedRectBitmap(context, bitmap, 3);
                if (bitmap != null) {
                    avatarBigCameraGroupCallImage.setVisibility(View.VISIBLE);
                    avatarBigCameraGroupCallImage.setImageBitmap(bitmap);
                    avatarBigCameraGroupCallInitialLetter.setVisibility(GONE);
                }else{
                    UserAvatarListener listener = new UserAvatarListener(context);
                    avatar.delete();
                    if(!avatarRequested){
                        avatarRequested = true;
                        if (context.getExternalCacheDir() != null){
                            megaApi.getUserAvatar(contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                        }
                        else{
                            megaApi.getUserAvatar(contactMail, context.getCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                        }
                    }

                    createDefaultAvatarGroupCall(userHandle, fullName);
                }
            }else{
                UserAvatarListener listener = new UserAvatarListener(context);

                if(!avatarRequested){
                    avatarRequested = true;
                    if (context.getExternalCacheDir() != null){
                        megaApi.getUserAvatar(contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                    }
                    else{
                        megaApi.getUserAvatar(contactMail, context.getCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                    }
                }

                createDefaultAvatarGroupCall(userHandle, fullName);
            }
        }else{
            UserAvatarListener listener = new UserAvatarListener(context);

            if(!avatarRequested){
                avatarRequested = true;
                if (context.getExternalCacheDir() != null){
                    megaApi.getUserAvatar(contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                }
                else{
                    megaApi.getUserAvatar(contactMail, context.getCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                }
            }
            createDefaultAvatarGroupCall(userHandle, fullName);
        }
    }

    public void createDefaultAvatarGroupCall(long userHandle,  String fullName) {
        log("createDefaultAvatarGroupCall");
        Bitmap defaultAvatar = Bitmap.createBitmap(outMetrics.widthPixels, outMetrics.widthPixels, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.TRANSPARENT);

        String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(userHandle));
        if (color != null) {
            p.setColor(Color.parseColor(color));
        }else{
            p.setColor(ContextCompat.getColor(this, R.color.lollipop_primary_color));
        }

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight()) {
            radius = defaultAvatar.getWidth() / 2;
        }else {
            radius = defaultAvatar.getHeight() / 2;
        }
        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        avatarBigCameraGroupCallImage.setImageBitmap(defaultAvatar);
        String contactFirstLetter = fullName.charAt(0) + "";
        contactFirstLetter = contactFirstLetter.toUpperCase(Locale.getDefault());
        avatarBigCameraGroupCallInitialLetter.setText(contactFirstLetter);
        avatarBigCameraGroupCallInitialLetter.setVisibility(View.VISIBLE);
    }

    public void clearHandlers(){
        log("clearHandlers");
        if (handlerArrow1 != null){
            handlerArrow1.removeCallbacksAndMessages(null);
        }
        if (handlerArrow2 != null){
            handlerArrow2.removeCallbacksAndMessages(null);
        }
        if (handlerArrow3 != null){
            handlerArrow3.removeCallbacksAndMessages(null);
        }
        if (handlerArrow4 != null){
            handlerArrow4.removeCallbacksAndMessages(null);
        }
        if (handlerArrow5 != null){
            handlerArrow5.removeCallbacksAndMessages(null);
        }
        if (handlerArrow6 != null){
            handlerArrow6.removeCallbacksAndMessages(null);
        }
    }

}
