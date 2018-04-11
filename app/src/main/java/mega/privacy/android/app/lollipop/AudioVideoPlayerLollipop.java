package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.MimeTypeMime;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.megachat.ChatExplorerActivity;
import mega.privacy.android.app.snackbarListeners.SnackbarNavigateOption;
import mega.privacy.android.app.utils.Constants;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferListenerInterface;

public class AudioVideoPlayerLollipop extends PinActivityLollipop implements VideoRendererEventListener, MegaChatRequestListenerInterface, MegaTransferListenerInterface, AudioRendererEventListener{

    public static int REQUEST_CODE_SELECT_CHAT = 1005;
    public static int REQUEST_CODE_SELECT_LOCAL_FOLDER = 1004;
    private final String offLineDIR = mega.privacy.android.app.utils.Util.offlineDIR;
    private final String oldMKFile = mega.privacy.android.app.utils.Util.oldMKFile;
    
    private MegaApiAndroid megaApi;
    private MegaChatApiAndroid megaChatApi;
    DatabaseHandler dbH = null;
    MegaPreferences prefs = null;

    private AlertDialog alertDialogTransferOverquota;

    Handler handler;
    boolean isFolderLink = false;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private Uri uri;

    private AppBarLayout appBarLayout;
    private Toolbar tB;
    private ActionBar aB;

    private MenuItem shareIcon;
    private MenuItem propertiesIcon;
    private MenuItem chatIcon;
    private MenuItem downloadIcon;

    private RelativeLayout audioVideoPlayerContainer;
    
    private RelativeLayout audioContainer;
    private long handle = -1;
    int countChat = 0;
    int successSent = 0;
    int errorSent = 0;
    boolean transferOverquota = false;

    private boolean video = false;
    private boolean loading = true;
    private ProgressDialog statusDialog = null;
    private String fileName = null;
    private long currentTime;

    private RelativeLayout containerAudioVideoPlayer;

    private Notification.Builder mBuilder;
    private NotificationManager mNotificationManager;

    private boolean isUrl;

    ArrayList<Long> handleListM = new ArrayList<Long>();
    
    private int currentPosition = 0;
    private int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;
    private long parentNodeHandle = -1;
    private int adapterType = 0;
    private ArrayList<Long> mediaHandles;
    private ArrayList<MegaOffline> offList;
    private ArrayList<MegaOffline> mediaOffList;
    private ArrayList<Uri> mediaUris;
    private boolean isOffLine = false;
    private boolean isPlayList;
    private int size = 0;

    private String downloadLocationDefaultPath = "";
    private boolean renamed = false;
    private boolean isOffline;
    private String path;
    private String pathNavigation;

    NodeController nC;
    private android.support.v7.app.AlertDialog downloadConfirmationDialog;
    private DisplayMetrics outMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");

        setContentView(R.layout.activity_audiovideoplayer);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            currentTime = savedInstanceState.getLong("currentTime");
            fileName = savedInstanceState.getString("fileName");
            handle = savedInstanceState.getLong("handle");
            uri = Uri.parse(savedInstanceState.getString("uri"));
            renamed = savedInstanceState.getBoolean("renamed");
        }
        else {
            currentTime = 0;
        }

        Intent intent = getIntent();
        if (intent == null){
            log("intent null");
            finish();
            return;
        }
        path = intent.getStringExtra("path");
        adapterType = getIntent().getIntExtra("adapterType", 0);
        if (adapterType == Constants.OFFLINE_ADAPTER){
            isOffline = true;
            pathNavigation = intent.getStringExtra("pathNavigation");
        }
        else {
            isOffline = false;
            pathNavigation = null;
        }
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            handle = bundle.getLong("HANDLE");
            fileName = bundle.getString("FILENAME");
        }
        isFolderLink = intent.getBooleanExtra("isFolderLink", false);
        currentPosition = intent.getIntExtra("position", 0);
        isPlayList = intent.getBooleanExtra("isPlayList", true);
        orderGetChildren = intent.getIntExtra("orderGetChildren", MegaApiJava.ORDER_DEFAULT_ASC);
        parentNodeHandle = intent.getLongExtra("parentNodeHandle", -1);
        adapterType = intent.getIntExtra("adapterType", 0);

        if (!renamed){
            uri = intent.getData();
            if (uri == null){
                log("uri null");
                finish();
                return;
            }
        }
        log("uri: "+uri);

        if (uri.toString().contains("http://")){
            isUrl = true;
        }
        else {
            isUrl = false;
        }

        Display display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

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

        tB = (Toolbar) findViewById(R.id.call_toolbar);
        if (tB == null) {
            log("Tb is Null");
            return;
        }

        tB.setVisibility(View.VISIBLE);
        setSupportActionBar(tB);
        aB = getSupportActionBar();
        log("aB.setHomeAsUpIndicator_1");
        aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);
        if (fileName != null) {
            aB.setTitle(fileName);
        }
        else {
            aB.setTitle(getFileName(uri));
        }

        containerAudioVideoPlayer = (RelativeLayout) findViewById(R.id.audiovideoplayer_container);

        audioContainer = (RelativeLayout) findViewById(R.id.audio_container);
        audioContainer.setVisibility(View.GONE);
        
        audioVideoPlayerContainer = (RelativeLayout) findViewById(R.id.audiovideoplayer_container); 

        handler = new Handler();

        MegaApplication app = (MegaApplication)getApplication();
        if (isFolderLink){
            megaApi = app.getMegaApiFolder();
        }
        else{
            megaApi = app.getMegaApi();
        }

        if(megaApi==null||megaApi.getRootNode()==null){
            log("Refresh session - sdk");
            Intent intentLogin = new Intent(this, LoginActivityLollipop.class);
            intentLogin.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
            intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentLogin);
            finish();
            return;
        }

        if(mega.privacy.android.app.utils.Util.isChatEnabled()){
            if (megaChatApi == null){
                megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
            }

            if(megaChatApi==null||megaChatApi.getInitState()== MegaChatApi.INIT_ERROR){
                log("Refresh session - karere");
                Intent intentLogin = new Intent(this, LoginActivityLollipop.class);
                intentLogin.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
                intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentLogin);
                finish();
                return;
            }
        }

        log("Overquota delay: "+megaApi.getBandwidthOverquotaDelay());
        if(megaApi.getBandwidthOverquotaDelay()>0){
            if(alertDialogTransferOverquota==null){
                showTransferOverquotaDialog();
            }
            else {
                if (!(alertDialogTransferOverquota.isShowing())) {
                    showTransferOverquotaDialog();
                }
            }
        }

        log("Add transfer listener");
        megaApi.addTransferListener(this);

        if (dbH == null){
            dbH = DatabaseHandler.getDbHandler(getApplicationContext());
        }

        if (megaApi.httpServerIsRunning() == 0) {
            megaApi.httpServerStart();
        }

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        if(mi.totalMem>Constants.BUFFER_COMP){
            log("Total mem: "+mi.totalMem+" allocate 32 MB");
            megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
        }
        else{
            log("Total mem: "+mi.totalMem+" allocate 16 MB");
            megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
        }

        MegaNode parentNode;

        if (isPlayList){
            if (adapterType == Constants.OFFLINE_ADAPTER){
                //OFFLINE
                log("OFFLINE_ADAPTER");
                isOffLine = true;
                offList = new ArrayList<>();
                String pathNavigation = intent.getStringExtra("pathNavigation");
                log("PATHNAVIGATION: " + pathNavigation);
                offList=dbH.findByPath(pathNavigation);
                log ("offList.size() = " + offList.size());

                for(int i=0; i<offList.size();i++){
                    MegaOffline checkOffline = offList.get(i);
                    File offlineDirectory = null;
                    if(checkOffline.getOrigin()==MegaOffline.INCOMING){
                        log("isIncomingOffline");

                        if (Environment.getExternalStorageDirectory() != null){
                            offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offLineDIR + "/" +checkOffline.getHandleIncoming() + "/" + checkOffline.getPath()+checkOffline.getName());
                            log("offlineDirectory: "+offlineDirectory);
                        }
                        else{
                            offlineDirectory = getFilesDir();
                        }
                    }
                    else if(checkOffline.getOrigin()==MegaOffline.INBOX){
                        if (Environment.getExternalStorageDirectory() != null){
                            offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offLineDIR + "/in/" + checkOffline.getPath()+checkOffline.getName());
                            log("offlineDirectory: "+offlineDirectory);
                        }
                        else{
                            offlineDirectory = getFilesDir();
                        }
                    }
                    else{
                        log("NOT isIncomingOffline");
                        if (Environment.getExternalStorageDirectory() != null){
                            offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offLineDIR + checkOffline.getPath()+checkOffline.getName());
                        }
                        else{
                            offlineDirectory = getFilesDir();
                        }
                    }

                    if(offlineDirectory!=null){
                        if (!offlineDirectory.exists()){
                            log("Path to remove B: "+(offList.get(i).getPath()+offList.get(i).getName()));
                            //dbH.removeById(offList.get(i).getId());
                            offList.remove(i);
                            i--;
                        }
                    }
                }

                if (offList != null){
                    if(!offList.isEmpty()) {
                        MegaOffline lastItem = offList.get(offList.size()-1);
                        if(!(lastItem.getHandle().equals("0"))){
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+oldMKFile;
                            log("Export in: "+path);
                            File file= new File(path);
                            if(file.exists()){
                                MegaOffline masterKeyFile = new MegaOffline("0", path, "MEGARecoveryKey.txt", 0, "0", 0, "0");
                                offList.add(masterKeyFile);
                            }
                        }
                    }
                    else{
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+oldMKFile;
                        log("Export in: "+path);
                        File file= new File(path);
                        if(file.exists()){
                            MegaOffline masterKeyFile = new MegaOffline("0", path, "MEGARecoveryKey.txt", 0, "0", 0, "0");
                            offList.add(masterKeyFile);
                        }
                    }
                }

                if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
                    sortByNameDescending();
                }
                else{
                    sortByNameAscending();
                }

                if (offList.size() > 0){

                    mediaOffList = new ArrayList<>();
                    int mediaPosition = -1;
                    for (int i=0;i<offList.size();i++){
                        if ((MimeTypeList.typeForName(offList.get(i).getName()).isVideoReproducible() && !MimeTypeList.typeForName(offList.get(i).getName()).isVideoNotSupported())|| MimeTypeList.typeForName(offList.get(i).getName()).isAudio()){
                            mediaOffList.add(offList.get(i));
                            mediaPosition++;
                            if (i == currentPosition){
                                currentPosition = mediaPosition;
                            }
                        }
                    }

                    if (currentPosition >= mediaOffList.size()){
                        currentPosition = 0;
                    }
                }
                size = mediaOffList.size();
            }
            else if(adapterType == Constants.SEARCH_ADAPTER){
                isOffLine = false;
                mediaHandles = new ArrayList<>();

                ArrayList<MegaNode> nodes = null;
                if (parentNodeHandle == -1){
                    String query = intent.getStringExtra("searchQuery");
                    nodes = megaApi.search(query);
                }
                else{
                    parentNode =  megaApi.getNodeByHandle(parentNodeHandle);
                    nodes = megaApi.getChildren(parentNode, orderGetChildren);
                }

                int mediaNumber = 0;
                for (int i=0;i<nodes.size();i++){
                    MegaNode n = nodes.get(i);
                    if ((MimeTypeList.typeForName(n.getName()).isVideoReproducible() && !MimeTypeList.typeForName(n.getName()).isVideoNotSupported()) || MimeTypeList.typeForName(n.getName()).isAudio()){
                        mediaHandles.add(n.getHandle());
                        if (i == currentPosition){
                            currentPosition = mediaNumber;
                        }
                        mediaNumber++;
                    }
                }

                if(mediaHandles.size() == 0){
                    finish();
                    return;
                }

                if(currentPosition >= mediaHandles.size()){
                    currentPosition = 0;
                }

                size = mediaHandles.size();
            }
            else{
                isOffLine = false;
                if (parentNodeHandle == -1){

                    switch(adapterType){
                        case Constants.FILE_BROWSER_ADAPTER:{
                            parentNode = megaApi.getRootNode();
                            break;
                        }
                        case Constants.RUBBISH_BIN_ADAPTER:{
                            parentNode = megaApi.getRubbishNode();
                            break;
                        }
                        case Constants.SHARED_WITH_ME_ADAPTER:{
                            parentNode = megaApi.getInboxNode();
                            break;
                        }
                        case Constants.FOLDER_LINK_ADAPTER:{
                            parentNode = megaApi.getRootNode();
                            break;
                        }
                        default:{
                            parentNode = megaApi.getRootNode();
                            break;
                        }
                    }

                }
                else{
                    parentNode = megaApi.getNodeByHandle(parentNodeHandle);
                }

                mediaHandles = new ArrayList<>();
                ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);

                int mediaNumber = 0;
                for (int i=0;i<nodes.size();i++){
                    MegaNode n = nodes.get(i);
                    if ((MimeTypeList.typeForName(n.getName()).isVideoReproducible() && !MimeTypeList.typeForName(n.getName()).isVideoNotSupported()) || MimeTypeList.typeForName(n.getName()).isAudio()){
                        mediaHandles.add(n.getHandle());
                        if (i == currentPosition){
                            currentPosition = mediaNumber;
                        }
                        mediaNumber++;
                    }
                }

                if(mediaHandles.size() == 0)
                {
                    finish();
                    return;
                }

                if(currentPosition >= mediaHandles.size())
                {
                    currentPosition = 0;
                }

                ((MegaApplication) getApplication()).sendSignalPresenceActivity();
                size = mediaHandles.size();
            }

            if (size > 1) {
                sortBySelected();
            }
        }


        //Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        //Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        //Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        //Set media controller
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();

        //Bind the player to the view
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setControllerAutoShow(false);
        simpleExoPlayerView.setControllerShowTimeoutMs(999999999);
        simpleExoPlayerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (aB.isShowing()) {
                        simpleExoPlayerView.hideController();
                        hideActionStatusBar();
                    }
                    else {
                        showActionStatusBar();
                    }
                }
                return true;
            }
        });

        //Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        //Produces DataSource instances through which meida data is loaded
        //DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "android2"), defaultBandwidthMeter);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "android2"), defaultBandwidthMeter);
        //Produces Extractor instances for parsing the media data
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        MediaSource mediaSource = null;
        LoopingMediaSource loopingMediaSource = null;

        if (isPlayList && size > 1) {
            final List<MediaSource> playlist = new ArrayList<>();
            MediaSource mSource;
            String localPath;
            Uri mediaUri;
            File mediaFile;
            mediaUris = new ArrayList<>();
            if (isOffLine){
                for(int i=0; i<mediaOffList.size(); i++){
                    MegaOffline currentNode = mediaOffList.get(i);
                    if(currentNode.getOrigin()==MegaOffline.INCOMING){
                        String handleString = currentNode.getHandleIncoming();
                        mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offLineDIR + "/" + handleString + "/"+currentNode.getPath() + "/" + currentNode.getName());
                    }
                    else if(currentNode.getOrigin()==MegaOffline.INBOX){
                        String handleString = currentNode.getHandleIncoming();
                        mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offLineDIR + "/in/"+currentNode.getPath() + "/" + currentNode.getName());
                    }
                    else{
                        mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + offLineDIR + currentNode.getPath() + "/" + currentNode.getName());
                    }
                    mediaUri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", mediaFile);
                    mediaUris.add(mediaUri);
                    mSource = new ExtractorMediaSource(mediaUri, dataSourceFactory, extractorsFactory, null, null);
                    playlist.add(mSource);
                }
            }
            else {
                MegaNode n;
                for(int i=0; i<mediaHandles.size(); i++){
                    n = megaApi.getNodeByHandle(mediaHandles.get(i));
                    localPath = mega.privacy.android.app.utils.Util.getLocalFile(this, n.getName(), n.getSize(), downloadLocationDefaultPath);
                    if (localPath != null && megaApi.getFingerprint(n).equals(megaApi.getFingerprint(localPath))){
                        mediaFile = new File(localPath);
                        mediaUri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", mediaFile);
                        mediaUris.add(mediaUri);
                        mSource = new ExtractorMediaSource(mediaUri, dataSourceFactory, extractorsFactory, null, null);
                    }
                    else {
                        mediaUri = Uri.parse(megaApi.httpServerGetLocalLink(n));
                        mediaUris.add(mediaUri);
                        mSource = new ExtractorMediaSource (mediaUri, dataSourceFactory, extractorsFactory, null, null);
                    }
                    playlist.add(mSource);
                }
            }

            final ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(playlist.toArray(new MediaSource[playlist.size()]));
            loopingMediaSource = new LoopingMediaSource(concatenatingMediaSource);
            player.prepare(loopingMediaSource);

        }
        else {
            mediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
            player.prepare(mediaSource);
        }

        final LoopingMediaSource finalLoopingMediaSource = loopingMediaSource;
        final MediaSource finalMediaSource = mediaSource;
        //MediaSource mediaSource = new HlsMediaSource(uri, dataSourceFactory, handler, null);
        //DashMediaSource mediaSource = new DashMediaSource(uri, dataSourceFactory, new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);

        statusDialog = new ProgressDialog(AudioVideoPlayerLollipop.this);
        statusDialog.setMessage(getString(R.string.general_loading));
        statusDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                showActionStatusBar();
            }
        });
        statusDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (loading) {
                    finish();
                }
            }
        });

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                log("onTimelineChanged");
                if (player.getCurrentPosition() > 0){
                    statusDialog.dismiss();
                }
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                log("onTracksChanged");

                loading = true;
                video = false;

                if (loading && !transferOverquota){
                    try {
                        statusDialog.setCanceledOnTouchOutside(false);
                        statusDialog.show();
                    }
                    catch(Exception e){
                        return;
                    }
                }
                else {
                    statusDialog.hide();
                    if (!video) {
                        audioContainer.setVisibility(View.VISIBLE);
                    }
                    else {
                        audioContainer.setVisibility(View.GONE);
                    }
                }
                if (size > 1) {
                    invalidateOptionsMenu();
                    if (isOffLine){
                        MegaOffline n = mediaOffList.get(player.getCurrentWindowIndex());
                        tB.setTitle(n.getName());
                        handle = Long.parseLong(n.getHandle());
                    }
                    else {
                        MegaNode n = megaApi.getNodeByHandle(mediaHandles.get(player.getCurrentWindowIndex()));
                        tB.setTitle(n.getName());
                        handle = n.getHandle();
                    }
                    uri = mediaUris.get(player.getCurrentWindowIndex());
                }
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                log("onLoadingChanged");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                log("onPlayerStateChanged");

                if (playbackState == ExoPlayer.STATE_BUFFERING){
                    audioContainer.setVisibility(View.GONE);

                    if (loading && !transferOverquota && !isOffline){
                        try {
                            statusDialog.setCanceledOnTouchOutside(false);
                            statusDialog.show();
                        }
                        catch(Exception e){
                            return;
                        }
                    }
                }
                else {
                    statusDialog.hide();

                    if (!video) {
                        audioContainer.setVisibility(View.VISIBLE);
                    }
                    else {
                        audioContainer.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                log("onRepeatModeChanged");
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                log("onShuffleModeEnabledChanged");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                log("onPlayerError");
                player.stop();
                if (isPlayList && size > 1){
                    player.prepare(finalLoopingMediaSource);
                }
                else {
                    player.prepare(finalMediaSource);
                }
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                log("onPositionDiscontinuity");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                log("onPlaybackParametersChanged");
            }

            @Override
            public void onSeekProcessed() {
                log("onSeekProcessed");
            }
        });
        player.setPlayWhenReady(true);
        player.seekTo(currentTime);
        player.setVideoDebugListener(this);
        player.setAudioDebugListener(this);
        simpleExoPlayerView.showController();
    }

    public void sortBySelected() {
        boolean found = false;

        if (isOffLine){
            ArrayList<MegaOffline> tempOffLine = new ArrayList<>();
            ArrayList<MegaOffline> orderOffLine = new ArrayList<>();

            for (int i=0; i<mediaOffList.size(); i++){
                if (!found) {
                    if (handle == Long.parseLong(mediaOffList.get(i).getHandle())) {
                        found = true;
                        orderOffLine.add(mediaOffList.get(i));
                    }
                    else {
                        tempOffLine.add(mediaOffList.get(i));
                    }
                }
                else {
                    orderOffLine.add(mediaOffList.get(i));
                }
            }
            if (tempOffLine.size() > 0) {
                for (int i=0; i<tempOffLine.size(); i++) {
                    orderOffLine.add(tempOffLine.get(i));
                }
            }
            mediaOffList.clear();
            mediaOffList = (ArrayList<MegaOffline>) orderOffLine.clone();
        }
        else {
            ArrayList<Long> tempHandles = new ArrayList<>();
            ArrayList<Long> orderHandles = new ArrayList<>();

            for (int i=0; i<mediaHandles.size(); i++){
                if (!found) {
                    if (handle == mediaHandles.get(i)) {
                        found = true;
                        orderHandles.add(mediaHandles.get(i));
                    }
                    else {
                        tempHandles.add(mediaHandles.get(i));
                    }
                }
                else {
                    orderHandles.add(mediaHandles.get(i));
                }
            }
            if (tempHandles.size() > 0) {
                for (int i=0; i<tempHandles.size(); i++) {
                    orderHandles.add(tempHandles.get(i));
                }
            }
            mediaHandles.clear();
            mediaHandles = (ArrayList<Long>) orderHandles.clone();
        }
    }

    public void sortByNameDescending(){

        ArrayList<String> foldersOrder = new ArrayList<String>();
        ArrayList<String> filesOrder = new ArrayList<String>();
        ArrayList<MegaOffline> tempOffline = new ArrayList<MegaOffline>();


        for(int k = 0; k < offList.size() ; k++) {
            MegaOffline node = offList.get(k);
            if(node.getType().equals("1")){
                foldersOrder.add(node.getName());
            }
            else{
                filesOrder.add(node.getName());
            }
        }


        Collections.sort(foldersOrder, String.CASE_INSENSITIVE_ORDER);
        Collections.reverse(foldersOrder);
        Collections.sort(filesOrder, String.CASE_INSENSITIVE_ORDER);
        Collections.reverse(filesOrder);

        for(int k = 0; k < foldersOrder.size() ; k++) {
            for(int j = 0; j < offList.size() ; j++) {
                String name = foldersOrder.get(k);
                String nameOffline = offList.get(j).getName();
                if(name.equals(nameOffline)){
                    tempOffline.add(offList.get(j));
                }
            }

        }

        for(int k = 0; k < filesOrder.size() ; k++) {
            for(int j = 0; j < offList.size() ; j++) {
                String name = filesOrder.get(k);
                String nameOffline = offList.get(j).getName();
                if(name.equals(nameOffline)){
                    tempOffline.add(offList.get(j));
                }
            }

        }

        offList.clear();
        offList.addAll(tempOffline);
    }


    public void sortByNameAscending(){
        log("sortByNameAscending");
        ArrayList<String> foldersOrder = new ArrayList<String>();
        ArrayList<String> filesOrder = new ArrayList<String>();
        ArrayList<MegaOffline> tempOffline = new ArrayList<MegaOffline>();

        for(int k = 0; k < offList.size() ; k++) {
            MegaOffline node = offList.get(k);
            if(node.getType().equals("1")){
                foldersOrder.add(node.getName());
            }
            else{
                filesOrder.add(node.getName());
            }
        }

        Collections.sort(foldersOrder, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(filesOrder, String.CASE_INSENSITIVE_ORDER);

        for(int k = 0; k < foldersOrder.size() ; k++) {
            for(int j = 0; j < offList.size() ; j++) {
                String name = foldersOrder.get(k);
                String nameOffline = offList.get(j).getName();
                if(name.equals(nameOffline)){
                    tempOffline.add(offList.get(j));
                }
            }
        }

        for(int k = 0; k < filesOrder.size() ; k++) {
            for(int j = 0; j < offList.size() ; j++) {
                String name = filesOrder.get(k);
                String nameOffline = offList.get(j).getName();
                if(name.equals(nameOffline)){
                    tempOffline.add(offList.get(j));
                }
            }

        }

        offList.clear();
        offList.addAll(tempOffline);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        currentTime = player.getCurrentPosition();
        outState.putLong("currentTime", currentTime);
        outState.putLong("currentPosition", currentPosition);
        outState.putLong("handle", handle);
        outState.putString("fileName", fileName);
        outState.putString("uri", uri.toString());
        outState.putBoolean("renamed", renamed);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    protected void hideActionStatusBar(){
        if (aB != null && aB.isShowing()) {
            if(tB != null) {
                tB.animate().translationY(-220).setDuration(400L)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                aB.hide();
                            }
                        }).start();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            else {
                aB.hide();
            }
            simpleExoPlayerView.hideController();
        }
    }
    protected void showActionStatusBar(){
        if (aB != null && !aB.isShowing()) {
            aB.show();
            if(tB != null) {
                tB.animate().translationY(0).setDuration(400L).start();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            simpleExoPlayerView.showController();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_audiovideoplayer, menu);

        shareIcon = menu.findItem(R.id.full_video_viewer_share);
        downloadIcon = menu.findItem(R.id.full_video_viewer_download);

        if (isUrl) {
            shareIcon.setVisible(false);
            downloadIcon.setVisible(true);
        }
        else {
            shareIcon.setVisible(true);
            downloadIcon.setVisible(false);
        }

        propertiesIcon = menu.findItem(R.id.full_video_viewer_properties);

        chatIcon = menu.findItem(R.id.full_video_viewer_chat);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        log("onPrepareOptionsMenu");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected");
        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                log("onBackPRess");
                if (player != null) {
                    player.release();
                }
                onBackPressed();
                break;
            }
            case R.id.full_video_viewer_chat:{
                log("Chat option");
                long[] longArray = new long[1];

                longArray[0] = handle;

                Intent i = new Intent(this, ChatExplorerActivity.class);
                i.putExtra("NODE_HANDLES", longArray);
                startActivityForResult(i, REQUEST_CODE_SELECT_CHAT);
                break;
            }
            case R.id.full_video_viewer_share: {
                log("Share option");

                intentToSendFile(uri);

                break;
            }
            case R.id.full_video_viewer_properties: {
                log("Info option");

                Intent i = new Intent(this, FileInfoActivityLollipop.class);
                if (isOffline){
                    i.putExtra("name", fileName);
                    i.putExtra("imageId", MimeTypeMime.typeForName(fileName).getIconResourceId());
                    i.putExtra("adapterType", Constants.OFFLINE_ADAPTER);
                    i.putExtra("path", path);
                    if (pathNavigation != null){
                        i.putExtra("pathNavigation", pathNavigation);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        i.setDataAndType(uri, MimeTypeList.typeForName(fileName).getType());
                    }
                    else{
                        i.setDataAndType(uri, MimeTypeList.typeForName(fileName).getType());
                    }
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                else {
                    MegaNode node = megaApi.getNodeByHandle(handle);
                    i.putExtra("handle", node.getHandle());
                    i.putExtra("imageId", MimeTypeMime.typeForName(node.getName()).getIconResourceId());
                    i.putExtra("name", node.getName());
                }
                startActivity(i);
                renamed = false;
                break;
            }
            case R.id.full_video_viewer_download: {
                log("Download option");
                downloadFile();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void downloadFile() {

        MegaNode node = megaApi.getNodeByHandle(handle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasStoragePermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.REQUEST_WRITE_STORAGE);

                handleListM.add(node.getHandle());
            }
        }
        ArrayList<Long> handleList = new ArrayList<Long>();
        handleList.add(node.getHandle());

        if(nC==null){
            nC = new NodeController(this);
        }
        nC.prepareForDownload(handleList);
    }


    public void intentToSendFile(Uri uri){
        log("intentToSendFile");

        if(uri!=null){
            if (!isUrl) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType(MimeTypeList.typeForName(fileName).getType()+"/*");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    log("Use provider to share");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri.toString()));
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                }
                startActivity(Intent.createChooser(share, getString(R.string.context_share)));
            }
            else{
                Snackbar.make(this.getCurrentFocus(), getString(R.string.not_download), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (intent == null) {
            return;
        }

        if (requestCode == REQUEST_CODE_SELECT_CHAT && resultCode == RESULT_OK){
            long[] chatHandles = intent.getLongArrayExtra("SELECTED_CHATS");
            log("Send to "+chatHandles.length+" chats");

            long[] nodeHandles = intent.getLongArrayExtra("NODE_HANDLES");
            log("Send "+nodeHandles.length+" nodes");

            countChat = chatHandles.length;
            if(countChat==1){
                megaChatApi.attachNode(chatHandles[0], nodeHandles[0], this);
            }
            else if(countChat>1){

                for(int i=0; i<chatHandles.length; i++){
                    megaChatApi.attachNode(chatHandles[i], nodeHandles[0], this);
                }
            }
        }
        else if (requestCode == REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
            log("local folder selected");
            String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
            String url = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_URL);
            long size = intent.getLongExtra(FileStorageActivityLollipop.EXTRA_SIZE, 0);
            long[] hashes = intent.getLongArrayExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES);
            log("URL: " + url + "___SIZE: " + size);

            if(nC==null){
                nC = new NodeController(this);
            }
            nC.checkSizeBeforeDownload(parentPath, url, size, hashes);
        }
    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {
        log("onVideoEnabled");
        video = true;
        loading = false;
        statusDialog.dismiss();
        audioContainer.setVisibility(View.GONE);
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        log("onVideoDecoderInitialized");
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
        log("onVideoInputFormatChanged");
    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {
        log("onDroppedFrames");
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        log("onVideoSizeChanged");
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
        log("onRenderedFirstFrame");
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
        log("onVideoDisabled");
        video = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");

        updateFile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
    }

    @Override
    protected void onDestroy() {
        log("onDestroy()");

        if (megaApi != null) {
            megaApi.removeTransferListener(this);
        }
        if (player != null){
            player.release();
        }

        super.onDestroy();
    }

    public void updateFile (){
        log("updateFile");

        MegaNode file = null;

        if (fileName != null){
            file = megaApi.getNodeByHandle(handle);
            if (file != null){
                if (!fileName.equals(file.getName())) {
                    fileName = file.getName();
                    if (aB != null){
                        tB = (Toolbar) findViewById(R.id.call_toolbar);
                        if(tB==null){
                            log("Tb is Null");
                            return;
                        }
                        tB.setVisibility(View.VISIBLE);
                        setSupportActionBar(tB);
                        aB = getSupportActionBar();
                    }
                    aB.setTitle(fileName);
                    setTitle(fileName);
                    invalidateOptionsMenu();

                    if (megaApi == null){
                        MegaApplication app = (MegaApplication)getApplication();
                        megaApi = app.getMegaApi();
                        megaApi.addTransferListener(this);
                    }
                    if (megaApi.httpServerIsRunning() == 0) {
                        megaApi.httpServerStart();
                    }

                    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    activityManager.getMemoryInfo(mi);

                    if(mi.totalMem>Constants.BUFFER_COMP){
                        log("Total mem: "+mi.totalMem+" allocate 32 MB");
                        megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
                    }
                    else{
                        log("Total mem: "+mi.totalMem+" allocate 16 MB");
                        megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
                    }

                    String url = megaApi.httpServerGetLocalLink(file);
                    getDownloadLocation();
                    String localPath = mega.privacy.android.app.utils.Util.getLocalFile(this, file.getName(), file.getSize(), downloadLocationDefaultPath);

                    if (localPath != null){
                        File mediaFile = new File(localPath);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && prefs.getStorageDownloadLocation().contains(Environment.getExternalStorageDirectory().getPath())) {
                            uri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", mediaFile);
                        }
                        else{
                            uri = Uri.fromFile(mediaFile);
                        }
                    }
                    else {
                        uri = Uri.parse(url);
                    }
                    renamed = true;
                }
            }
        }
    }

    public void getDownloadLocation(){
        if (dbH == null){
            dbH = DatabaseHandler.getDbHandler(getApplicationContext());
        }

        prefs = dbH.getPreferences();
        if (prefs != null){
            if (prefs.getStorageAskAlways() != null){
                if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
                    if (prefs.getStorageDownloadLocation() != null){
                        if (prefs.getStorageDownloadLocation().compareTo("") != 0){
                            downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
                        }
                    }
                }
            }
        }
    }

    public static void log(String message) {
        mega.privacy.android.app.utils.Util.log("AudioVideoPlayerLollipop", message);
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
        if(request.getType() == MegaChatRequest.TYPE_ATTACH_NODE_MESSAGE){

            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("File sent correctly");
                successSent++;

            }
            else{
                log("File NOT sent: "+e.getErrorCode()+"___"+e.getErrorString());
                errorSent++;
            }

            if(countChat==errorSent+successSent){
                if(successSent==countChat){
                    if(countChat==1){
                        long handle = request.getChatHandle();
                        MegaChatListItem chatItem = megaChatApi.getChatListItem(handle);
                        if(chatItem!=null){
                            Intent intent = new Intent(this, ManagerActivityLollipop.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
                            intent.putExtra("CHAT_ID", handle);
                            startActivity(intent);
                            finish();
                        }
                    }
                    else{
                        showSnackbar(getString(R.string.success_attaching_node_from_cloud_chats, countChat));
                    }
                }
                else if(errorSent==countChat){
                    showSnackbar(getString(R.string.error_attaching_node_from_cloud));
                }
                else{
                    showSnackbar(getString(R.string.error_attaching_node_from_cloud_chats));
                }
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }

    public void showSnackbar(String s){
        log("showSnackbar");
        Snackbar snackbar = Snackbar.make(containerAudioVideoPlayer, s, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);
        snackbar.show();
    }

    @Override
    public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {

    }

    @Override
    public void onTransferFinish(MegaApiJava api, MegaTransfer transfer, MegaError e) {

    }

    @Override
    public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {

    }

    @Override
    public void onTransferTemporaryError(MegaApiJava api, MegaTransfer transfer, MegaError e) {
        log("onTransferTemporaryError");

        if(e.getErrorCode() == MegaError.API_EOVERQUOTA){
            log("API_EOVERQUOTA error!!");

            if(alertDialogTransferOverquota==null){
                showTransferOverquotaDialog();
            }
            else {
                if (!(alertDialogTransferOverquota.isShowing())) {
                    showTransferOverquotaDialog();
                }
            }
        }
    }

    @Override
    public boolean onTransferData(MegaApiJava api, MegaTransfer transfer, byte[] buffer) {
        return false;
    }


    public void showTransferOverquotaDialog(){
        log("showTransferOverquotaDialog");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.transfer_overquota_layout, null);
        dialogBuilder.setView(dialogView);

        TextView title = (TextView) dialogView.findViewById(R.id.transfer_overquota_title);
        title.setText(getString(R.string.title_depleted_transfer_overquota));

        ImageView icon = (ImageView) dialogView.findViewById(R.id.image_transfer_overquota);
        icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.transfer_quota_empty));

        TextView text = (TextView) dialogView.findViewById(R.id.text_transfer_overquota);
        text.setText(getString(R.string.text_depleted_transfer_overquota));

        Button continueButton = (Button) dialogView.findViewById(R.id.transfer_overquota_button_dissmiss);

        Button paymentButton = (Button) dialogView.findViewById(R.id.transfer_overquota_button_payment);
        paymentButton.setText(getString(R.string.action_upgrade_account));

        alertDialogTransferOverquota = dialogBuilder.create();

        alertDialogTransferOverquota.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                transferOverquota = true;
                statusDialog.hide();
                showActionStatusBar();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                alertDialogTransferOverquota.dismiss();
                transferOverquota = false;
                if (loading) {
                    statusDialog.setCanceledOnTouchOutside(false);
                    statusDialog.show();
                }
            }

        });

        paymentButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                alertDialogTransferOverquota.dismiss();
                transferOverquota = false;
                showUpgradeAccount();
            }
        });

        alertDialogTransferOverquota.setCancelable(false);
        alertDialogTransferOverquota.setCanceledOnTouchOutside(false);
        alertDialogTransferOverquota.show();
    }

    public void showUpgradeAccount(){
        log("showUpgradeAccount");
        Intent upgradeIntent = new Intent(this, ManagerActivityLollipop.class);
        upgradeIntent.setAction(Constants.ACTION_SHOW_UPGRADE_ACCOUNT);
        startActivity(upgradeIntent);
    }

    @Override
    public void onAudioEnabled(DecoderCounters counters) {
        loading = false;
        statusDialog.dismiss();
    }

    @Override
    public void onAudioSessionId(int audioSessionId) {

    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onAudioInputFormatChanged(Format format) {

    }

    @Override
    public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {

    }

    public void openAdvancedDevices (long handleToDownload){
        log("openAdvancedDevices");
//		handleToDownload = handle;
        String externalPath = mega.privacy.android.app.utils.Util.getExternalCardPath();

        if(externalPath!=null){
            log("ExternalPath for advancedDevices: "+externalPath);
            MegaNode node = megaApi.getNodeByHandle(handleToDownload);
            if(node!=null){

//				File newFile =  new File(externalPath+"/"+node.getName());
                File newFile =  new File(node.getName());
                log("File: "+newFile.getPath());
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                // Filter to only show results that can be "opened", such as
                // a file (as opposed to a list of contacts or timezones).
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Create a file with the requested MIME type.
                String mimeType = MimeTypeList.getMimeType(newFile);
                log("Mimetype: "+mimeType);
                intent.setType(mimeType);
                intent.putExtra(Intent.EXTRA_TITLE, node.getName());
                intent.putExtra("handleToDownload", handleToDownload);
                try{
                    startActivityForResult(intent, Constants.WRITE_SD_CARD_REQUEST_CODE);
                }
                catch(Exception e){
                    log("Exception in External SDCARD");
                    Environment.getExternalStorageDirectory();
                    Toast toast = Toast.makeText(this, getString(R.string.no_external_SD_card_detected), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
        else{
            log("No external SD card");
            Environment.getExternalStorageDirectory();
            Toast toast = Toast.makeText(this, getString(R.string.no_external_SD_card_detected), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void showSnackbarNotSpace(){
        log("showSnackbarNotSpace");
        Snackbar mySnackbar = Snackbar.make(containerAudioVideoPlayer, R.string.error_not_enough_free_space, Snackbar.LENGTH_LONG);
        mySnackbar.setAction("Settings", new SnackbarNavigateOption(this));
        mySnackbar.show();
    }

    public void askSizeConfirmationBeforeDownload(String parentPath, String url, long size, long [] hashes){
        log("askSizeConfirmationBeforeDownload");

        final String parentPathC = parentPath;
        final String urlC = url;
        final long [] hashesC = hashes;
        final long sizeC=size;

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        LinearLayout confirmationLayout = new LinearLayout(this);
        confirmationLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(mega.privacy.android.app.utils.Util.scaleWidthPx(20, outMetrics), mega.privacy.android.app.utils.Util.scaleHeightPx(10, outMetrics), mega.privacy.android.app.utils.Util.scaleWidthPx(17, outMetrics), 0);

        final CheckBox dontShowAgain =new CheckBox(this);
        dontShowAgain.setText(getString(R.string.checkbox_not_show_again));
        dontShowAgain.setTextColor(getResources().getColor(R.color.text_secondary));

        confirmationLayout.addView(dontShowAgain, params);

        builder.setView(confirmationLayout);

//				builder.setTitle(getString(R.string.confirmation_required));

        builder.setMessage(getString(R.string.alert_larger_file, mega.privacy.android.app.utils.Util.getSizeString(sizeC)));
        builder.setPositiveButton(getString(R.string.general_download),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(dontShowAgain.isChecked()){
                            dbH.setAttrAskSizeDownload("false");
                        }
                        if(nC==null){
                            nC = new NodeController(AudioVideoPlayerLollipop.this);
                        }
                        nC.checkInstalledAppBeforeDownload(parentPathC, urlC, sizeC, hashesC);
                    }
                });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(dontShowAgain.isChecked()){
                    dbH.setAttrAskSizeDownload("false");
                }
            }
        });

        downloadConfirmationDialog = builder.create();
        downloadConfirmationDialog.show();
    }

    public void askConfirmationNoAppInstaledBeforeDownload (String parentPath, String url, long size, long [] hashes, String nodeToDownload){
        log("askConfirmationNoAppInstaledBeforeDownload");

        final String parentPathC = parentPath;
        final String urlC = url;
        final long [] hashesC = hashes;
        final long sizeC=size;

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        LinearLayout confirmationLayout = new LinearLayout(this);
        confirmationLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(mega.privacy.android.app.utils.Util.scaleWidthPx(20, outMetrics), mega.privacy.android.app.utils.Util.scaleHeightPx(10, outMetrics), mega.privacy.android.app.utils.Util.scaleWidthPx(17, outMetrics), 0);

        final CheckBox dontShowAgain =new CheckBox(this);
        dontShowAgain.setText(getString(R.string.checkbox_not_show_again));
        dontShowAgain.setTextColor(getResources().getColor(R.color.text_secondary));

        confirmationLayout.addView(dontShowAgain, params);

        builder.setView(confirmationLayout);

//				builder.setTitle(getString(R.string.confirmation_required));
        builder.setMessage(getString(R.string.alert_no_app, nodeToDownload));
        builder.setPositiveButton(getString(R.string.general_download),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(dontShowAgain.isChecked()){
                            dbH.setAttrAskNoAppDownload("false");
                        }
                        if(nC==null){
                            nC = new NodeController(AudioVideoPlayerLollipop.this);
                        }
                        nC.download(parentPathC, urlC, sizeC, hashesC);
                    }
                });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(dontShowAgain.isChecked()){
                    dbH.setAttrAskNoAppDownload("false");
                }
            }
        });
        downloadConfirmationDialog = builder.create();
        downloadConfirmationDialog.show();
    }
}