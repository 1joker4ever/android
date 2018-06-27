package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.util.SizeF;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.MimeTypeThumbnail;
import mega.privacy.android.app.R;
import mega.privacy.android.app.ShareInfo;
import mega.privacy.android.app.UploadService;
import mega.privacy.android.app.UserCredentials;
import mega.privacy.android.app.components.EditTextCursorWatcher;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.managerSections.FileBrowserFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.InboxFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.IncomingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.OfflineFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.OutgoingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.RubbishBinFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.SearchFragmentLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatSettings;
import mega.privacy.android.app.snackbarListeners.SnackbarNavigateOption;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaEvent;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferListenerInterface;
import nz.mega.sdk.MegaUser;

import static mega.privacy.android.app.lollipop.FileInfoActivityLollipop.TYPE_EXPORT_REMOVE;

public class PdfViewerActivityLollipop extends PinActivityLollipop implements MegaGlobalListenerInterface, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener, MegaRequestListenerInterface, MegaChatRequestListenerInterface, MegaTransferListenerInterface{

    int[] screenPosition;
    int mLeftDelta;
    int mTopDelta;
    float mWidthScale;
    float mHeightScale;
    int screenWidth;
    int screenHeight;

    static PdfViewerActivityLollipop pdfViewerActivityLollipop;

    MegaApplication app = null;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    MegaPreferences prefs = null;

    private AlertDialog alertDialogTransferOverquota;
    public ProgressBar progressBar;

    public static boolean loading = true;
    boolean transferOverquota = false;

    PDFView pdfView;

    Toolbar tB;
    public ActionBar aB;
    private String gSession;
    UserCredentials credentials;
    private String lastEmail;
    DatabaseHandler dbH = null;
    ChatSettings chatSettings;
    boolean isUrl;
    DefaultScrollHandle defaultScrollHandle;

    Uri uri;
    String pdfFileName;
    boolean inside = false;
    long handle = -1;
    boolean isFolderLink = false;
    private int currentPage;
    private int type;
    private boolean isOffLine = false;
    int countChat = 0;
    int errorSent = 0;
    int successSent = 0;

    public RelativeLayout uploadContainer;
    RelativeLayout pdfviewerContainer;

    ProgressDialog statusDialog;

    private MenuItem shareMenuItem;
    private MenuItem downloadMenuItem;
    private MenuItem propertiesMenuItem;
    private MenuItem chatMenuItem;
    private MenuItem getlinkMenuItem;
    private MenuItem renameMenuItem;
    private MenuItem moveMenuItem;
    private MenuItem copyMenuItem;
    private MenuItem moveToTrashMenuItem;
    private MenuItem removeMenuItem;
    private MenuItem removelinkMenuItem;
    private MenuItem importMenuItem;
    private MenuItem saveForOfflineMenuItem;
    private MenuItem chatRemoveMenuItem;

    private List<ShareInfo> filePreparedInfos;
    ArrayList<Long> handleListM = new ArrayList<Long>();

    static int TYPE_UPLOAD = 0;
    static int TYPE_DOWNLOAD = 1;

    private String downloadLocationDefaultPath = "";
    private boolean renamed = false;
    private String path;
    private String pathNavigation;

    NodeController nC;
    private android.support.v7.app.AlertDialog downloadConfirmationDialog;
    private DisplayMetrics outMetrics;

    private RelativeLayout bottomLayout;
    private TextView fileNameTextView;
    int accountType;
    int typeExport = -1;
    private Handler handler;
    private AlertDialog renameDialog;
    String regex = "[*|\\?:\"<>\\\\\\\\/]";
    boolean moveToRubbish = false;
    ProgressDialog moveToTrashStatusDialog;
    private boolean fromShared = false;

    private TextView actualPage;
    private TextView totalPages;
    private RelativeLayout pageNumber;

    boolean toolbarVisible = true;
    boolean fromChat = false;
    boolean isDeleteDialogShow = false;
    boolean fromDownload = false;

    ChatController chatC;
    private long msgId = -1;
    private long chatId = -1;
    MegaNode nodeChat;
    MegaChatMessage msgChat;

    boolean notChangePage = false;
    MegaNode currentDocument;

    @Override
    public void onCreate (Bundle savedInstanceState){
        log("onCreate");

        super.onCreate(savedInstanceState);

        pdfViewerActivityLollipop = this;

        final Intent intent = getIntent();
        if (intent == null){
            log("intent null");
            finish();
            return;
        }
        handler = new Handler();
        if (savedInstanceState != null) {
            log("saveInstanceState");
            currentPage = savedInstanceState.getInt("currentPage");
            handle = savedInstanceState.getLong("HANDLE");
            pdfFileName = savedInstanceState.getString("pdfFileName");
            uri = Uri.parse(savedInstanceState.getString("uri"));
            renamed = savedInstanceState.getBoolean("renamed");
            accountType = savedInstanceState.getInt("typeAccount");
            isDeleteDialogShow = savedInstanceState.getBoolean("isDeleteDialogShow", false);
            toolbarVisible = savedInstanceState.getBoolean("toolbarVisible", toolbarVisible);
        }
        else {
            currentPage = 1;
            accountType = intent.getIntExtra("typeAccount", MegaAccountDetails.ACCOUNT_TYPE_FREE);
            isDeleteDialogShow = false;
            handle = intent.getLongExtra("HANDLE", -1);
            uri = intent.getData();
            log("URI pdf: "+uri);
            if (uri == null){
                log("uri null");
                finish();
                return;
            }
        }
        fromDownload = intent.getBooleanExtra("fromDownloadService", false);
        fromShared = intent.getBooleanExtra("fromShared", false);
        inside = intent.getBooleanExtra("inside", false);
//        handle = intent.getLongExtra("HANDLE", -1);
        isFolderLink = intent.getBooleanExtra("isFolderLink", false);
        type = intent.getIntExtra("adapterType", 0);
        path = intent.getStringExtra("path");

//        if (!renamed){
//            uri = intent.getData();
//            log("URI pdf: "+uri);
//            if (uri == null){
//                log("uri null");
//                finish();
//                return;
//            }
//        }

        if (type == Constants.OFFLINE_ADAPTER){
            isOffLine = true;
            pathNavigation = intent.getStringExtra("pathNavigation");
        }
        else if (type == Constants.FILE_LINK_ADAPTER) {
            String serialize = intent.getStringExtra(Constants.EXTRA_SERIALIZE_STRING);
            if(serialize!=null) {
                currentDocument = MegaNode.unserialize(serialize);
                if (currentDocument != null) {
                    log("currentDocument NOT NULL");
                }
                else {
                    log("currentDocument is NULL");
                }
            }
            isOffLine = false;
            fromChat = false;
        }
        else {
            isOffLine = false;
            pathNavigation = null;
            if (type == Constants.FROM_CHAT){
                fromChat = true;
                chatC = new ChatController(this);
                msgId = intent.getLongExtra("msgId", -1);
                chatId = intent.getLongExtra("chatId", -1);
            }
            else {
                fromChat = false;
            }
        }

        Display display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        screenHeight = outMetrics.heightPixels;
        screenWidth = outMetrics.widthPixels;

        setContentView(R.layout.activity_pdfviewer);

        if (Build.VERSION.SDK_INT >= 26) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        if (!isOffLine) {
            app = (MegaApplication) getApplication();
            if (isFolderLink){
                megaApi = app.getMegaApiFolder();
            }
            else {
                megaApi = app.getMegaApi();
            }

            if (Util.isChatEnabled()) {
                megaChatApi = app.getMegaChatApi();
                if (megaChatApi != null) {
                    if (msgId != -1 && chatId != -1) {
                        msgChat = megaChatApi.getMessage(chatId, msgId);
                        if (msgChat != null) {
                            nodeChat = msgChat.getMegaNodeList().get(0);
                            if (isDeleteDialogShow) {
                                showConfirmationDeleteNode(chatId, msgChat);
                            }
                        }
                    } else {
                        log("msgId or chatId null");
                    }
                }
            }

            log("Add transfer listener");
            megaApi.addTransferListener(this);
            megaApi.addGlobalListener(this);

            if (savedInstanceState != null && uri.toString().contains("http://")){
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

                if (savedInstanceState != null && uri.toString().contains("http://")){
                    MegaNode node = null;
                    if (fromChat) {
                        node = nodeChat;
                    }
                    else if (type == Constants.FILE_LINK_ADAPTER) {
                        node = currentDocument;
                    }
                    else {
                        node = megaApi.getNodeByHandle(handle);
                    }
                    if (node != null){
                        uri = Uri.parse(megaApi.httpServerGetLocalLink(node));
                    }
                    else {
                        showSnackbar(getString(R.string.error_streaming));
                    }
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
        }

//        if(megaApi==null||megaApi.getRootNode()==null){
//            log("Refresh session - sdk");
//            Intent intentLogin = new Intent(this, LoginActivityLollipop.class);
//            intentLogin.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
//            intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intentLogin);
//            finish();
//            return;
//        }
//
//        if(Util.isChatEnabled()){
//            if (megaChatApi == null){
//                megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
//            }
//
//            if(megaChatApi==null||megaChatApi.getInitState()== MegaChatApi.INIT_ERROR){
//                log("Refresh session - karere");
//                Intent intentLogin = new Intent(this, LoginActivityLollipop.class);
//                intentLogin.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
//                intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intentLogin);
//                finish();
//                return;
//            }
//        }

        tB = (Toolbar) findViewById(R.id.toolbar_pdf_viewer);
        if(tB==null){
            log("Tb is Null");
            return;
        }

        tB.setVisibility(View.VISIBLE);
        setSupportActionBar(tB);
        aB = getSupportActionBar();
        aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);

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

        bottomLayout = (RelativeLayout) findViewById(R.id.pdf_viewer_layout_bottom);
        fileNameTextView = (TextView) findViewById(R.id.pdf_viewer_file_name);
        actualPage = (TextView) findViewById(R.id.pdf_viewer_actual_page_number);
        actualPage.setText(""+currentPage);
        totalPages = (TextView) findViewById(R.id.pdf_viewer_total_page_number);
        pageNumber = (RelativeLayout) findViewById(R.id.pdf_viewer_page_number);
        progressBar = (ProgressBar) findViewById(R.id.pdf_viewer_progress_bar);

        pdfView = (PDFView) findViewById(R.id.pdfView);

        pdfView.setBackgroundColor(Color.LTGRAY);
        pdfFileName = getFileName(uri);
        defaultScrollHandle = new DefaultScrollHandle(PdfViewerActivityLollipop.this);

        loading = true;
        if (uri.toString().contains("http://")){
            isUrl = true;
            loadStreamPDF();
        }
        else {
            isUrl = false;
            loadLocalPDF();
        }

        setTitle(pdfFileName);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            fileNameTextView.setMaxWidth(Util.scaleWidthPx(300, outMetrics));
        }
        else{
            fileNameTextView.setMaxWidth(Util.scaleWidthPx(300, outMetrics));
        }

        fileNameTextView.setText(pdfFileName);

        uploadContainer = (RelativeLayout) findViewById(R.id.upload_container_layout_bottom);
        if (!inside) {
            aB.setTitle(pdfFileName);
            uploadContainer.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.GONE);
            pageNumber.animate().translationY(-150).start();
        }
        else {
            aB.setTitle(" ");
            uploadContainer.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.VISIBLE);
            pageNumber.animate().translationY(-100).start();
        }

        uploadContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("onClick uploadContainer");
                Intent intent1 = new Intent(PdfViewerActivityLollipop.this, FileExplorerActivityLollipop.class);
                intent1.setAction(FileExplorerActivityLollipop.ACTION_UPLOAD_TO_CLOUD);
                intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent1.setDataAndType(uri, "application/pdf");
                startActivity(intent1);
                finish();
            }
        });

        pdfviewerContainer = (RelativeLayout) findViewById(R.id.pdf_viewer_container);

        if (!toolbarVisible) {
            setToolbarVisibilityHide(0L);
        }

        if (savedInstanceState == null){
            ViewTreeObserver observer = pdfView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    pdfView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int[] location = new int[2];
                    pdfView.getLocationOnScreen(location);
                    int[] getlocation = new int[2];
                    getLocationOnScreen(getlocation);
                    screenPosition = getIntent().getIntArrayExtra("screenPosition");
                    if (screenPosition != null){
                        mLeftDelta = getlocation[0] - location[0];
                        mTopDelta = getlocation[1] - location[1];

                        mWidthScale = (float) screenPosition[2] / pdfView.getWidth();
                        mHeightScale = (float) screenPosition[3] / pdfView.getHeight();
                    }
                    else {
                        mLeftDelta = (screenWidth/2) - location[0];
                        mTopDelta = (screenHeight/2) - location[1];

                        mWidthScale = (float) (screenWidth/4) / pdfView.getWidth();
                        mHeightScale = (float) (screenHeight/4) / pdfView.getHeight();
                    }
                    log("mLeftDelta: "+mLeftDelta+" mTopDelta: "+mTopDelta+" mWidthScale: "+mWidthScale+" mHeightScale: "+mHeightScale);
                    runEnterAnimation();

                    return true;
                }
            });
        }
    }

    public void runEnterAnimation() {
        final long duration = 400;
        if (aB != null && aB.isShowing()) {
            if(tB != null) {
                tB.animate().translationY(-220).setDuration(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                aB.hide();
                            }
                        }).start();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                bottomLayout.animate().translationY(220).setDuration(0).start();
                uploadContainer.animate().translationY(220).setDuration(0).start();
                pageNumber.animate().translationY(0).setDuration(0).start();
            } else {
                aB.hide();
            }
        }
        pageNumber.animate().translationY(0).start();

        pdfView.setPivotX(0);
        pdfView.setPivotY(0);
        pdfView.setScaleX(mWidthScale);
        pdfView.setScaleY(mHeightScale);
        pdfView.setTranslationX(mLeftDelta);
        pdfView.setTranslationY(mTopDelta);

        pdfView.animate().setDuration(duration).scaleX(1).scaleY(1).translationX(0).translationY(0).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                setToolbarVisibilityShow();
            }
        });
    }

    void getLocationOnScreen(int[] location){
        if (type == Constants.RUBBISH_BIN_ADAPTER){
            RubbishBinFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.INBOX_ADAPTER){
            InboxFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.INCOMING_SHARES_ADAPTER){
            IncomingSharesFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.OUTGOING_SHARES_ADAPTER){
            OutgoingSharesFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.CONTACT_FILE_ADAPTER){
            ContactFileListFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.FOLDER_LINK_ADAPTER){
            FolderLinkActivityLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.SEARCH_ADAPTER){
            SearchFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.FILE_BROWSER_ADAPTER){
            FileBrowserFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
        else if (type == Constants.OFFLINE_ADAPTER){
            OfflineFragmentLollipop.imageDrag.getLocationOnScreen(location);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log("onNewIntent");

        if (intent == null){
            log("intent null");
            finish();
            return;
        }
        pdfViewerActivityLollipop = this;

        handler = new Handler();
        if (intent.getBooleanExtra("inside", false)){
            setIntent(intent);
            if (!intent.getBooleanExtra("isUrl", true)){
                isUrl = false;
                uri = intent.getData();
                supportInvalidateOptionsMenu();
            }
        }
        else {
            accountType = intent.getIntExtra("typeAccount", MegaAccountDetails.ACCOUNT_TYPE_FREE);
            type = intent.getIntExtra("adapterType", 0);
            path = intent.getStringExtra("path");
            currentPage = 1;
            inside = false;
            if (type == Constants.OFFLINE_ADAPTER){
                isOffLine = true;
                pathNavigation = intent.getStringExtra("pathNavigation");
            }
            else {
                isOffLine = false;
                pathNavigation = null;
                if (type == Constants.FROM_CHAT){
                    fromChat = true;
                    chatC = new ChatController(this);
                    msgId = intent.getLongExtra("msgId", -1);
                    chatId = intent.getLongExtra("chatId", -1);
                }
                else {
                    fromChat = false;
                }
            }
            handle = getIntent().getLongExtra("HANDLE", -1);

            uri = intent.getData();
            if (uri == null){
                log("uri null");
                finish();
                return;
            }
            Intent newIntent = new Intent();
            newIntent.setDataAndType(uri, "application/pdf");
            newIntent.setAction(Constants.ACTION_OPEN_FOLDER);
            setIntent(newIntent);
            Display display = getWindowManager().getDefaultDisplay();
            outMetrics = new DisplayMetrics ();
            display.getMetrics(outMetrics);
            screenHeight = outMetrics.heightPixels;
            screenWidth = outMetrics.widthPixels;

            setContentView(R.layout.activity_pdfviewer);

            if (!isOffLine){
                app = (MegaApplication)getApplication();
                if (isFolderLink){
                    megaApi = app.getMegaApiFolder();
                }
                else {
                    megaApi = app.getMegaApi();
                }

                if(Util.isChatEnabled()){
                    megaChatApi = app.getMegaChatApi();
                    if (megaChatApi != null){
                        if (msgId != -1 && chatId != -1){
                            msgChat = megaChatApi.getMessage(chatId, msgId);
                            if (msgChat != null){
                                nodeChat = msgChat.getMegaNodeList().get(0);
                            }
                        }
                        else {
                            log("msgId or chatId null");
                        }
                    }
                }

                log("Add transfer listener");
                megaApi.addTransferListener(this);
                megaApi.addGlobalListener(this);

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
            }

            tB = (Toolbar) findViewById(R.id.toolbar_pdf_viewer);
            if(tB==null){
                log("Tb is Null");
                return;
            }

            tB.setVisibility(View.VISIBLE);
            setSupportActionBar(tB);
            aB = getSupportActionBar();
            aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
            aB.setHomeButtonEnabled(true);
            aB.setDisplayHomeAsUpEnabled(true);

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

            bottomLayout = (RelativeLayout) findViewById(R.id.pdf_viewer_layout_bottom);
            fileNameTextView = (TextView) findViewById(R.id.pdf_viewer_file_name);
            actualPage = (TextView) findViewById(R.id.pdf_viewer_actual_page_number);
            actualPage.setText(""+currentPage);
            totalPages = (TextView) findViewById(R.id.pdf_viewer_total_page_number);
            pageNumber = (RelativeLayout) findViewById(R.id.pdf_viewer_page_number);
            pageNumber.animate().translationY(-150).start();
            progressBar = (ProgressBar) findViewById(R.id.pdf_viewer_progress_bar);

            pdfView = (PDFView) findViewById(R.id.pdfView);

            pdfView.setBackgroundColor(Color.LTGRAY);
            defaultScrollHandle = new DefaultScrollHandle(PdfViewerActivityLollipop.this);

            isUrl = false;
            loadLocalPDF();
            pdfFileName = getFileName(uri);

            path = uri.getPath();
            setTitle(pdfFileName);
            aB.setTitle(pdfFileName);

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                fileNameTextView.setMaxWidth(Util.scaleWidthPx(300, outMetrics));
            }
            else{
                fileNameTextView.setMaxWidth(Util.scaleWidthPx(300, outMetrics));
            }
            fileNameTextView.setText(pdfFileName);

            uploadContainer = (RelativeLayout) findViewById(R.id.upload_container_layout_bottom);
            uploadContainer.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.GONE);
            uploadContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    log("onClick uploadContainer");
                    Intent intent1 = new Intent(PdfViewerActivityLollipop.this, FileExplorerActivityLollipop.class);
                    intent1.setAction(FileExplorerActivityLollipop.ACTION_UPLOAD_TO_CLOUD);
                    intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent1.setDataAndType(uri, "application/pdf");
                    startActivity(intent1);
                    finish();
                }
            });

            pdfviewerContainer = (RelativeLayout) findViewById(R.id.pdf_viewer_container);

            ViewTreeObserver observer = pdfView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    pdfView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int[] location = new int[2];
                    pdfView.getLocationOnScreen(location);
                    int[] getlocation = new int[2];
                    getLocationOnScreen(getlocation);
                    screenPosition = getIntent().getIntArrayExtra("screenPosition");
                    if (screenPosition != null){
                        mLeftDelta = getlocation[0] - location[0];
                        mTopDelta = getlocation[1] - location[1];

                        mWidthScale = (float) screenPosition[2] / pdfView.getWidth();
                        mHeightScale = (float) screenPosition[3] / pdfView.getHeight();
                    }
                    else {
                        mLeftDelta = (screenWidth/2) - location[0];
                        mTopDelta = (screenHeight/2) - location[1];

                        mWidthScale = (float) (screenWidth/4) / pdfView.getWidth();
                        mHeightScale = (float) (screenHeight/4) / pdfView.getHeight();
                    }
                    log("mLeftDelta: "+mLeftDelta+" mTopDelta: "+mTopDelta+" mWidthScale: "+mWidthScale+" mHeightScale: "+mHeightScale);
                    runEnterAnimation();

                    return true;
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putInt("currentPage", currentPage);
        outState.putLong("HANDLE", handle);
        outState.putString("pdfFileName", pdfFileName);
        outState.putString("uri", uri.toString());
        outState.putBoolean("renamed", renamed);
        outState.putInt("typeAccount", accountType);
        outState.putBoolean("isDeleteDialogShow", isDeleteDialogShow);
        outState.putBoolean("toolbarVisible", toolbarVisible);
    }

    @Override
    public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {

    }

    @Override
    public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> nodeList) {
        log("onNodesUpdate");
        if (megaApi.getNodeByHandle(handle) == null){
            return;
        }
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onReloadNeeded(MegaApiJava api) {

    }

    @Override
    public void onAccountUpdate(MegaApiJava api) {

    }

    @Override
    public void onContactRequestsUpdate(MegaApiJava api, ArrayList<MegaContactRequest> requests) {

    }

    @Override
    public void onEvent(MegaApiJava api, MegaEvent event) {

    }

    class LoadPDFStream extends AsyncTask<String, Void, InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                if (httpURLConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream( (httpURLConnection.getInputStream()));
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            log("onPostExecute");
            try {
                pdfView.fromStream(inputStream)
                        .defaultPage(currentPage-1)
                        .onPageChange(PdfViewerActivityLollipop.this)
                        .enableAnnotationRendering(true)
                        .onLoad(PdfViewerActivityLollipop.this)
                        .scrollHandle(defaultScrollHandle)
                        .spacing(10) // in dp
                        .onPageError(PdfViewerActivityLollipop.this)
                        .load();
            } catch (Exception e) {

            }

            if (loading && !transferOverquota){
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadStreamPDF() {
        log("loadStreamPDF loading: "+loading);
        new LoadPDFStream().execute(uri.toString());
    }

    private void loadLocalPDF() {
        log("loadLocalPDF loading: "+loading);

        progressBar.setVisibility(View.VISIBLE);
        try {
            pdfView.fromUri(uri)
                    .defaultPage(currentPage-1)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(defaultScrollHandle)
                    .spacing(10) // in dp
                    .onPageError(this)
                    .load();
        } catch (Exception e) {

        }
    }

    public void download(){

        if (type == Constants.FILE_LINK_ADAPTER){
            MegaNode node = megaApi.getNodeByHandle(currentDocument.getHandle());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                if (!hasStoragePermission) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Constants.REQUEST_WRITE_STORAGE);

                    handleListM.add(node.getHandle());
                }
            }

            downloadNode();
        }
        else if (fromChat){
            if (chatC == null){
                chatC = new ChatController(this);
            }
            if (nodeChat != null){
                chatC.prepareForChatDownload(nodeChat);
            }
        }
        else {
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
    }

    public void downloadNode(){
        log("downloadNode()");
        if (currentDocument == null){
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasStoragePermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_WRITE_STORAGE);
                return;
            }
        }


        if (dbH == null){
            dbH = DatabaseHandler.getDbHandler(getApplicationContext());
        }

        if (dbH.getCredentials() == null || dbH.getPreferences() == null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                File[] fs = getExternalFilesDirs(null);
                if (fs.length > 1){
                    if (fs[1] == null){

                        Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                        intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                        intent.setClass(this, FileStorageActivityLollipop.class);
                        intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                        intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
                    }else{
                        Dialog downloadLocationDialog;
                        String[] sdCardOptions = getResources().getStringArray(R.array.settings_storage_download_location_array);
                        android.app.AlertDialog.Builder b=new android.app.AlertDialog.Builder(this);

                        b.setTitle(getResources().getString(R.string.settings_storage_download_location));
                        b.setItems(sdCardOptions, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which){
                                    case 0:{
                                        Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                                        intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                                        intent.setClass(getApplicationContext(), FileStorageActivityLollipop.class);
                                        intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                                        intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                                        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
                                        break;
                                    }
                                    case 1:{
                                        File[] fs = getExternalFilesDirs(null);
                                        if (fs.length > 1){
                                            String path = fs[1].getAbsolutePath();
                                            File defaultPathF = new File(path);
                                            defaultPathF.mkdirs();
                                            Toast.makeText(getApplicationContext(), getString(R.string.general_download) + ": "  + defaultPathF.getAbsolutePath() , Toast.LENGTH_LONG).show();
                                            downloadTo(path, uri.toString(), currentDocument.getSize(), currentDocument.getHandle());
                                        }
                                        break;
                                    }
                                }
                            }
                        });
                        b.setNegativeButton(getResources().getString(R.string.general_cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        downloadLocationDialog = b.create();
                        downloadLocationDialog.show();
                    }
                }
                else{
                    Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                    intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                    intent.setClass(this, FileStorageActivityLollipop.class);
                    intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                    intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                    startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
                }
            }
            else{
                Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                intent.setClass(this, FileStorageActivityLollipop.class);
                intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
            }
            return;
        }

        boolean askMe = true;
        String downloadLocationDefaultPath = "";
        prefs = dbH.getPreferences();
        if (prefs != null){
            if (prefs.getStorageAskAlways() != null){
                if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
                    if (prefs.getStorageDownloadLocation() != null){
                        if (prefs.getStorageDownloadLocation().compareTo("") != 0){
                            askMe = false;
                            downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
                            log("downloadLocationDefaultPath = "+downloadLocationDefaultPath);

                        }
                    }
                }
            }
        }

        if (askMe){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                File[] fs = getExternalFilesDirs(null);
                if (fs.length > 1){
                    if (fs[1] == null){
                        Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                        intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                        intent.setClass(this, FileStorageActivityLollipop.class);
                        intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                        intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
                    }
                    else{
                        Dialog downloadLocationDialog;
                        String[] sdCardOptions = getResources().getStringArray(R.array.settings_storage_download_location_array);
                        android.app.AlertDialog.Builder b=new android.app.AlertDialog.Builder(this);

                        b.setTitle(getResources().getString(R.string.settings_storage_download_location));
//						final long sizeFinal = size;
//						final long[] hashesFinal = new long[hashes.length];
//						for (int i=0; i< hashes.length; i++){
//							hashesFinal[i] = hashes[i];
//						}

                        b.setItems(sdCardOptions, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which){
                                    case 0:{
                                        Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                                        intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                                        intent.setClass(getApplicationContext(), FileStorageActivityLollipop.class);
                                        intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                                        intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                                        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
                                        break;
                                    }
                                    case 1:{
                                        File[] fs = getExternalFilesDirs(null);
                                        if (fs.length > 1){
                                            String path = fs[1].getAbsolutePath();
                                            File defaultPathF = new File(path);
                                            defaultPathF.mkdirs();
                                            Toast.makeText(getApplicationContext(), getString(R.string.general_download) + ": "  + defaultPathF.getAbsolutePath() , Toast.LENGTH_LONG).show();
                                            downloadTo(path, uri.toString(), currentDocument.getSize(), currentDocument.getHandle());
                                        }
                                        break;
                                    }
                                }
                            }
                        });
                        b.setNegativeButton(getResources().getString(R.string.general_cancel), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        downloadLocationDialog = b.create();
                        downloadLocationDialog.show();
                    }
                }
                else{
                    Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                    intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                    intent.setClass(this, FileStorageActivityLollipop.class);
                    intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                    intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                    startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
                }
            }
            else{
                Intent intent = new Intent(FileStorageActivityLollipop.Mode.PICK_FOLDER.getAction());
                intent.putExtra(FileStorageActivityLollipop.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
                intent.setClass(this, FileStorageActivityLollipop.class);
                intent.putExtra(FileStorageActivityLollipop.EXTRA_URL, uri.toString());
                intent.putExtra(FileStorageActivityLollipop.EXTRA_SIZE, currentDocument.getSize());
                startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER);
            }
        }
        else{
            downloadTo(downloadLocationDefaultPath, null, currentDocument.getSize(), currentDocument.getHandle());
        }
    }

    public void downloadTo(String parentPath, String url, long size, long hash){
        log("downloadTo");
        double availableFreeSpace = Double.MAX_VALUE;
        try{
            StatFs stat = new StatFs(parentPath);
            availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
        }
        catch(Exception ex){}

        MegaNode tempNode = currentDocument;
        if((tempNode != null) && tempNode.getType() == MegaNode.TYPE_FILE){
            log("is file");
            String localPath = mega.privacy.android.app.utils.Util.getLocalFile(this, tempNode.getName(), tempNode.getSize(), parentPath);
            if(localPath != null){
                try {
                    mega.privacy.android.app.utils.Util.copyFile(new File(localPath), new File(parentPath, tempNode.getName()));
                }catch(Exception e) {}

                showSnackbar(getString(R.string.general_already_downloaded));
            }else{
                log("LocalPath is NULL");
            }

            MegaNode node = currentDocument;
            if(node != null){
                log("Node!=null: "+node.getName());
                Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
                dlFiles.put(node, parentPath);

                for (MegaNode document : dlFiles.keySet()) {
                    String path = dlFiles.get(document);

                    if(availableFreeSpace < document.getSize()){
                        showSnackbarNotSpace();
                        continue;
                    }

                    Intent service = new Intent(this, DownloadService.class);
                    service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
//                    service.putExtra(DownloadService.EXTRA_URL, url);
                    service.putExtra(Constants.EXTRA_SERIALIZE_STRING, currentDocument.serialize());
                    service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
                    service.putExtra(DownloadService.EXTRA_PATH, path);
                    service.putExtra("fromMV", true);
                    log("intent to DownloadService");
                    startService(service);
                }
            }else if(url != null) {
                if(availableFreeSpace < size) {
                    showSnackbarNotSpace();
                }

                Intent service = new Intent(this, DownloadService.class);
                service.putExtra(DownloadService.EXTRA_HASH, hash);
//                service.putExtra(DownloadService.EXTRA_URL, url);
                service.putExtra(Constants.EXTRA_SERIALIZE_STRING, currentDocument.serialize());
                service.putExtra(DownloadService.EXTRA_SIZE, size);
                service.putExtra(DownloadService.EXTRA_PATH, parentPath);
                service.putExtra("fromMV", true);
                startService(service);
            }else {
                log("node not found. Let's try the document");
            }
        }
    }

    public void onIntentProcessed(List<ShareInfo> infos) {

        if (statusDialog != null) {
            try {
                statusDialog.dismiss();
            }
            catch(Exception ex){}
        }

        log("intent processed!");

        if (infos == null) {
            log("Error infos is NULL");
            return;
        }
        else {

            MegaNode parentNode = megaApi.getRootNode();
            for (ShareInfo info : infos) {

                Intent intent = new Intent(this, UploadService.class);
                intent.putExtra(UploadService.EXTRA_FILEPATH, info.getFileAbsolutePath());
                intent.putExtra(UploadService.EXTRA_NAME, info.getTitle());
                intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
                intent.putExtra(UploadService.EXTRA_SIZE, info.getSize());
                startService(intent);
            }
            filePreparedInfos = null;
        }
    }

    public void backToCloud(long handle){
        log("backToCloud: "+handle);
        Intent startIntent = new Intent(this, ManagerActivityLollipop.class);
        if(handle!=-1){
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.setAction(Constants.ACTION_OPEN_FOLDER);
            startIntent.putExtra("PARENT_HANDLE", handle);
        }
        startActivity(startIntent);
    }

    public  void setToolbarVisibilityShow () {
        log("setToolbarVisibilityShow");
        toolbarVisible = true;
        aB.show();
        if(tB != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            tB.animate().translationY(0).setDuration(200L).start();
            bottomLayout.animate().translationY(0).setDuration(200L).start();
            uploadContainer.animate().translationY(0).setDuration(200L).start();
            if (inside){
                pageNumber.animate().translationY(-100).setDuration(200L).start();
            }
            else {
                pageNumber.animate().translationY(-150).setDuration(200L).start();
            }
        }
    }

    public void setToolbarVisibilityHide (long duration) {
        log("setToolbarVisibilityHide");
        toolbarVisible = false;
        if(tB != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            tB.animate().translationY(-220).setDuration(duration).withEndAction(new Runnable() {
                @Override
                public void run() {
                    aB.hide();
                }
            }).start();
            bottomLayout.animate().translationY(220).setDuration(duration).start();
            uploadContainer.animate().translationY(220).setDuration(duration).start();
            pageNumber.animate().translationY(0).setDuration(duration).start();
        }
        else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            aB.hide();
        }
    }

    public boolean isToolbarVisible(){
        return toolbarVisible;
    }

    public void setToolbarVisibility (){

        int page = pdfView.getCurrentPage();

        if (queryIfPdfIsHorizontal(page) &&  getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && !pdfView.isZooming()) {
            notChangePage = true;
            pdfView.jumpTo(page - 1);
        }

        if (aB != null && aB.isShowing()) {
            setToolbarVisibilityHide(200L);
        } else if (aB != null && !aB.isShowing()){
            setToolbarVisibilityShow();
        }
    }

    boolean queryIfPdfIsHorizontal(int page){
        SizeF sizeF = pdfView.getPageSize(page);
        if (sizeF.getWidth() > sizeF.getHeight()){
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_pdfviewer, menu);

        shareMenuItem = menu.findItem(R.id.pdf_viewer_share);
        downloadMenuItem = menu.findItem(R.id.pdf_viewer_download);
        chatMenuItem = menu.findItem(R.id.pdf_viewer_chat);
        propertiesMenuItem = menu.findItem(R.id.pdf_viewer_properties);
        getlinkMenuItem = menu.findItem(R.id.pdf_viewer_get_link);
        renameMenuItem = menu.findItem(R.id.pdf_viewer_rename);
        moveMenuItem = menu.findItem(R.id.pdf_viewer_move);
        copyMenuItem = menu.findItem(R.id.pdf_viewer_copy);
        moveToTrashMenuItem = menu.findItem(R.id.pdf_viewer_move_to_trash);
        removeMenuItem = menu.findItem(R.id.pdf_viewer_remove);
        removelinkMenuItem = menu.findItem(R.id.pdf_viewer_remove_link);
        importMenuItem = menu.findItem(R.id.chat_pdf_viewer_import);
        saveForOfflineMenuItem = menu.findItem(R.id.chat_pdf_viewer_save_for_offline);
        chatRemoveMenuItem = menu.findItem(R.id.chat_pdf_viewer_remove);

        if (!inside){
            propertiesMenuItem.setVisible(false);
            chatMenuItem.setVisible(false);
            shareMenuItem.setVisible(true);
            downloadMenuItem.setVisible(false);
            getlinkMenuItem.setVisible(false);
            renameMenuItem.setVisible(false);
            moveMenuItem.setVisible(false);
            copyMenuItem.setVisible(false);
            moveToTrashMenuItem.setVisible(false);
            removeMenuItem.setVisible(false);
            removelinkMenuItem.setVisible(false);
            importMenuItem.setVisible(false);
            saveForOfflineMenuItem.setVisible(false);
            chatRemoveMenuItem.setVisible(false);
        }
        else {
            if (type == Constants.OFFLINE_ADAPTER){
                getlinkMenuItem.setVisible(false);
                removelinkMenuItem.setVisible(false);
                shareMenuItem.setVisible(true);
                propertiesMenuItem.setVisible(true);
                downloadMenuItem.setVisible(false);
                renameMenuItem.setVisible(false);
                moveMenuItem.setVisible(false);
                copyMenuItem.setVisible(false);
                moveToTrashMenuItem.setVisible(false);
                removeMenuItem.setVisible(false);
                chatMenuItem.setVisible(false);
                importMenuItem.setVisible(false);
                saveForOfflineMenuItem.setVisible(false);
                chatRemoveMenuItem.setVisible(false);
            }
            else if(type == Constants.SEARCH_ADAPTER){
                MegaNode node = megaApi.getNodeByHandle(handle);

                if (isUrl){
                    shareMenuItem.setVisible(false);
                    downloadMenuItem.setVisible(true);
                }
                else {
                    shareMenuItem.setVisible(true);
                    downloadMenuItem.setVisible(false);
                }
                if(node.isExported()){
                    removelinkMenuItem.setVisible(true);
                    getlinkMenuItem.setVisible(false);
                }else{
                    removelinkMenuItem.setVisible(false);
                    getlinkMenuItem.setVisible(true);
                }

                propertiesMenuItem.setVisible(true);
                renameMenuItem.setVisible(true);
                moveMenuItem.setVisible(true);
                copyMenuItem.setVisible(true);

                if(Util.isChatEnabled()){
                    chatMenuItem.setVisible(true);
                }
                else{
                    chatMenuItem.setVisible(false);
                }

                MegaNode parent = megaApi.getNodeByHandle(handle);
                while (megaApi.getParentNode(parent) != null){
                    parent = megaApi.getParentNode(parent);
                }

                if (parent.getHandle() != megaApi.getRubbishNode().getHandle()){
                    moveToTrashMenuItem.setVisible(true);
                    removeMenuItem.setVisible(false);
                }
                else{
                    moveToTrashMenuItem.setVisible(false);
                    removeMenuItem.setVisible(true);
                }
                importMenuItem.setVisible(false);
                saveForOfflineMenuItem.setVisible(false);
                chatRemoveMenuItem.setVisible(false);
            }
            else if (type == Constants.FROM_CHAT){
                getlinkMenuItem.setVisible(false);
                removelinkMenuItem.setVisible(false);
                shareMenuItem.setVisible(false);
                propertiesMenuItem.setVisible(false);
                renameMenuItem.setVisible(false);
                moveMenuItem.setVisible(false);
                copyMenuItem.setVisible(false);
                moveToTrashMenuItem.setVisible(false);
                removeMenuItem.setVisible(false);
                chatMenuItem.setVisible(false);

                if(megaApi==null || !Util.isOnline(this)) {
                    downloadMenuItem.setVisible(false);
                    importMenuItem.setVisible(false);
                    saveForOfflineMenuItem.setVisible(false);

                    if (MegaApiJava.userHandleToBase64(msgChat.getUserHandle()).equals(megaChatApi.getMyUserHandle())) {
                        if (msgChat.isDeletable()){
                            chatRemoveMenuItem.setVisible(true);
                        }
                        else {
                            chatRemoveMenuItem.setVisible(false);
                        }
                    }
                    else {
                        log("The message is not mine");
                        chatRemoveMenuItem.setVisible(false);
                    }
                }
                else {
                    if (nodeChat != null){
                        downloadMenuItem.setVisible(true);
                        importMenuItem.setVisible(true);
                        saveForOfflineMenuItem.setVisible(true);

                        if (msgChat.getUserHandle() == megaChatApi.getMyUserHandle()) {
                            if((megaApi.getNodeByHandle(nodeChat.getHandle()))==null){
                                log("The node is not mine");
                                chatRemoveMenuItem.setVisible(false);
                            }
                            else{
                                if(msgChat.isDeletable()){
                                    chatRemoveMenuItem.setVisible(true);
                                }
                                else{
                                    chatRemoveMenuItem.setVisible(false);
                                }
                            }
                        }
                        else {
                            log("The message is not mine");
                            chatRemoveMenuItem.setVisible(false);
                        }
                    }
                    else {
                        downloadMenuItem.setVisible(false);
                        importMenuItem.setVisible(false);
                        saveForOfflineMenuItem.setVisible(false);
                        chatRemoveMenuItem.setVisible(false);
                    }
                }
            }
            else if (type == Constants.FILE_LINK_ADAPTER) {
                log("onCreateOptionsMenu FILE_LINK_ADAPTER");
                getlinkMenuItem.setVisible(false);
                removelinkMenuItem.setVisible(false);
                shareMenuItem.setVisible(false);
                propertiesMenuItem.setVisible(false);
                downloadMenuItem.setVisible(true);
                renameMenuItem.setVisible(false);
                moveMenuItem.setVisible(false);
                copyMenuItem.setVisible(false);
                moveToTrashMenuItem.setVisible(false);
                removeMenuItem.setVisible(false);
                chatMenuItem.setVisible(false);
                importMenuItem.setVisible(false);
                saveForOfflineMenuItem.setVisible(false);
                chatRemoveMenuItem.setVisible(false);
            }
            else {
                shareMenuItem.setVisible(true);
                boolean shareVisible = true;
                MegaNode node = megaApi.getNodeByHandle(handle);

                if (node == null) {
                    getlinkMenuItem.setVisible(false);
                    removelinkMenuItem.setVisible(false);
                    shareMenuItem.setVisible(false);
                    propertiesMenuItem.setVisible(false);
                    downloadMenuItem.setVisible(false);
                    renameMenuItem.setVisible(false);
                    moveMenuItem.setVisible(false);
                    copyMenuItem.setVisible(false);
                    moveToTrashMenuItem.setVisible(false);
                    removeMenuItem.setVisible(false);
                    chatMenuItem.setVisible(false);
                    importMenuItem.setVisible(false);
                    saveForOfflineMenuItem.setVisible(false);
                    chatRemoveMenuItem.setVisible(false);
                }
                else {
                    if(type==Constants.CONTACT_FILE_ADAPTER){
                        shareMenuItem.setVisible(false);
                        shareVisible = false;
                    }
                    else{
                        if(fromShared){
                            shareMenuItem.setVisible(false);
                            shareVisible = false;
                        }
                        if(isFolderLink){
                            shareMenuItem.setVisible(false);
                            shareVisible = false;
                        }
                    }
                    copyMenuItem.setVisible(true);

                    if(node.isExported()){
                        getlinkMenuItem.setVisible(false);
                        removelinkMenuItem.setVisible(true);
                    }
                    else{
                        if(type==Constants.CONTACT_FILE_ADAPTER){
                            getlinkMenuItem.setVisible(false);
                            removelinkMenuItem.setVisible(false);
                        }
                        else{
                            if(fromShared){
                                removelinkMenuItem.setVisible(false);
                                getlinkMenuItem.setVisible(false);
                            }
                            else{
                                if(isFolderLink){
                                    getlinkMenuItem.setVisible(false);
                                    removelinkMenuItem.setVisible(false);

                                }
                                else{
                                    getlinkMenuItem.setVisible(true);
                                    removelinkMenuItem.setVisible(false);
                                }
                            }
                        }
                    }

                    if(fromShared){
                        removeMenuItem.setVisible(false);
                        chatMenuItem.setVisible(false);

                        node = megaApi.getNodeByHandle(handle);
                        int accessLevel = megaApi.getAccess(node);

                        switch(accessLevel){
                            case MegaShare.ACCESS_OWNER:
                            case MegaShare.ACCESS_FULL:{
                                renameMenuItem.setVisible(true);
                                moveMenuItem.setVisible(true);
                                moveToTrashMenuItem.setVisible(true);
                                break;
                            }
                            case MegaShare.ACCESS_READWRITE:
                            case MegaShare.ACCESS_READ:{
                                renameMenuItem.setVisible(false);
                                moveMenuItem.setVisible(false);
                                moveToTrashMenuItem.setVisible(false);
                                break;
                            }
                        }
                    }
                    else{
                        if(isFolderLink){
                            propertiesMenuItem.setVisible(false);
                            moveToTrashMenuItem.setVisible(false);
                            removeMenuItem.setVisible(false);
                            renameMenuItem.setVisible(false);
                            moveMenuItem.setVisible(false);
                            copyMenuItem.setVisible(false);
                            chatMenuItem.setVisible(false);
                        }
                        else{
                            propertiesMenuItem.setVisible(true);

                            if(type==Constants.CONTACT_FILE_ADAPTER){
                                removeMenuItem.setVisible(false);
                                node = megaApi.getNodeByHandle(handle);
                                int accessLevel = megaApi.getAccess(node);
                                switch(accessLevel){
                                    case MegaShare.ACCESS_OWNER:
                                    case MegaShare.ACCESS_FULL:{
                                        renameMenuItem.setVisible(true);
                                        moveMenuItem.setVisible(true);
                                        moveToTrashMenuItem.setVisible(true);
                                        if(Util.isChatEnabled()){
                                            chatMenuItem.setVisible(true);
                                        }
                                        else{
                                            chatMenuItem.setVisible(false);
                                        }
                                        break;
                                    }
                                    case MegaShare.ACCESS_READWRITE:
                                    case MegaShare.ACCESS_READ:{
                                        renameMenuItem.setVisible(false);
                                        moveMenuItem.setVisible(false);
                                        moveToTrashMenuItem.setVisible(false);
                                        chatMenuItem.setVisible(false);
                                        break;
                                    }
                                }
                            }
                            else{
                                if(Util.isChatEnabled()){
                                    chatMenuItem.setVisible(true);
                                }
                                else{
                                    chatMenuItem.setVisible(false);
                                }
                                renameMenuItem.setVisible(true);
                                moveMenuItem.setVisible(true);

                                node = megaApi.getNodeByHandle(handle);

                                final long handle = node.getHandle();
                                MegaNode parent = megaApi.getNodeByHandle(handle);

                                while (megaApi.getParentNode(parent) != null){
                                    parent = megaApi.getParentNode(parent);
                                }

                                if (parent.getHandle() != megaApi.getRubbishNode().getHandle()){
                                    moveToTrashMenuItem.setVisible(true);
                                    removeMenuItem.setVisible(false);

                                }
                                else{
                                    moveToTrashMenuItem.setVisible(false);
                                    removeMenuItem.setVisible(true);
                                    getlinkMenuItem.setVisible(false);
                                    removelinkMenuItem.setVisible(false);
                                }
                            }
                        }
                    }
                    if (isUrl){
                        downloadMenuItem.setVisible(true);
                        shareMenuItem.setVisible(false);
                    }
                    else {
                        downloadMenuItem.setVisible(false);
                        if (shareVisible){
                            shareMenuItem.setVisible(true);
                        }
                    }
                    importMenuItem.setVisible(false);
                    saveForOfflineMenuItem.setVisible(false);
                    chatRemoveMenuItem.setVisible(false);
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected");
        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        int id = item.getItemId();
        switch(id) {
            case android.R.id.home: {
                onBackPressed();
                finish();
                break;
            }
            case R.id.pdf_viewer_share: {
                intentToSendFile();
                break;
            }
            case R.id.pdf_viewer_download: {
                download();
                break;
            }
            case R.id.pdf_viewer_chat: {
                long[] longArray = new long[1];
                longArray[0] = handle;

                if(nC ==null){
                    nC = new NodeController(this);
                }

                nC.selectChatsToSendNodes(longArray);
                break;
            }
            case R.id.pdf_viewer_properties: {
                showPropertiesActivity();
                break;
            }
            case R.id.pdf_viewer_get_link: {
                showGetLinkActivity();
                break;
            }
            case R.id.pdf_viewer_remove_link: {
                showRemoveLink();
                break;
            }
            case R.id.pdf_viewer_rename: {
                showRenameDialog();
                break;
            }
            case R.id.pdf_viewer_move: {
                showMove();
                break;
            }
            case R.id.pdf_viewer_copy: {
                showCopy();
                break;
            }
            case R.id.pdf_viewer_move_to_trash: {
                moveToTrash();
                break;
            }
            case R.id.pdf_viewer_remove: {
                moveToTrash();
                break;
            }
            case R.id.chat_pdf_viewer_import:{
                if (nodeChat != null){
                    importNode();
                }
                break;
            }
            case R.id.chat_pdf_viewer_save_for_offline:{
                if (chatC == null){
                    chatC = new ChatController(this);
                }
                if (msgChat != null){
                    chatC.saveForOffline(msgChat.getMegaNodeList());
                }
                break;
            }
            case R.id.chat_pdf_viewer_remove:{
                if (msgChat != null && chatId != -1){
                    showConfirmationDeleteNode(chatId, msgChat);
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void importNode(){
        log("importNode");

        Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
        intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_IMPORT_FOLDER);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_IMPORT_FOLDER);
    }

    public void showConfirmationDeleteNode(final long chatId, final MegaChatMessage message){
        log("showConfirmationDeleteNode");
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if (chatC == null){
                            chatC = new ChatController(pdfViewerActivityLollipop);
                        }
                        chatC.deleteMessage(message, chatId);
                        isDeleteDialogShow = false;
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        isDeleteDialogShow = false;
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        }
        else{
            builder = new android.support.v7.app.AlertDialog.Builder(this);
        }
        builder.setMessage(R.string.confirmation_delete_one_attachment);
        builder.setPositiveButton(R.string.context_remove, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();

        isDeleteDialogShow = true;

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDeleteDialogShow = false;
            }
        });
    }

    public void moveToTrash(){
        log("moveToTrash");

        moveToRubbish = false;
        if (!Util.isOnline(this)){
            Snackbar.make(pdfviewerContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
            return;
        }

        if(isFinishing()){
            return;
        }

        final MegaNode rubbishNode = megaApi.getRubbishNode();

        MegaNode parent = megaApi.getNodeByHandle(handle);
        while (megaApi.getParentNode(parent) != null){
            parent = megaApi.getParentNode(parent);
        }

        if (parent.getHandle() != megaApi.getRubbishNode().getHandle()){
            moveToRubbish = true;
        }
        else{
            moveToRubbish = false;
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Check if the node is not yet in the rubbish bin (if so, remove it)

                        if (moveToRubbish){
                            megaApi.moveNode(megaApi.getNodeByHandle(handle), rubbishNode, PdfViewerActivityLollipop.this);
                            ProgressDialog temp = null;
                            try{
                                temp = new ProgressDialog(PdfViewerActivityLollipop.this);
                                temp.setMessage(getString(R.string.context_move_to_trash));
                                temp.show();
                            }
                            catch(Exception e){
                                return;
                            }
                            moveToTrashStatusDialog = temp;
                        }
                        else{
                            megaApi.remove(megaApi.getNodeByHandle(handle), PdfViewerActivityLollipop.this);
                            ProgressDialog temp = null;
                            try{
                                temp = new ProgressDialog(PdfViewerActivityLollipop.this);
                                temp.setMessage(getString(R.string.context_delete_from_mega));
                                temp.show();
                            }
                            catch(Exception e){
                                return;
                            }
                            moveToTrashStatusDialog = temp;
                        }


                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        if (moveToRubbish){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            String message= getResources().getString(R.string.confirmation_move_to_rubbish);
            builder.setMessage(message).setPositiveButton(R.string.general_move, dialogClickListener)
                    .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            String message= getResources().getString(R.string.confirmation_delete_from_mega);
            builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
                    .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
        }
    }


    public void showCopy(){
        log("showCopy");

        ArrayList<Long> handleList = new ArrayList<Long>();
        handleList.add(handle);

        Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
        intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_COPY_FOLDER);
        long[] longArray = new long[handleList.size()];
        for (int i=0; i<handleList.size(); i++){
            longArray[i] = handleList.get(i);
        }
        intent.putExtra("COPY_FROM", longArray);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_COPY_FOLDER);
    }

    public void showMove(){
        log("showMove");

        ArrayList<Long> handleList = new ArrayList<Long>();
        handleList.add(handle);

        Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
        intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_MOVE_FOLDER);
        long[] longArray = new long[handleList.size()];
        for (int i=0; i<handleList.size(); i++){
            longArray[i] = handleList.get(i);
        }
        intent.putExtra("MOVE_FROM", longArray);
        startActivityForResult(intent, Constants.REQUEST_CODE_SELECT_MOVE_FOLDER);
    }

    private void showKeyboardDelayed(final View view) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 50);
    }

    public void showRenameDialog() {
        log("showRenameDialog");
        final MegaNode node = megaApi.getNodeByHandle(handle);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);
        //	    layout.setLayoutParams(params);

        final EditTextCursorWatcher input = new EditTextCursorWatcher(this, node.isFolder());
        input.setSingleLine();
        input.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);

        input.setImeActionLabel(getString(R.string.context_rename), EditorInfo.IME_ACTION_DONE);
        input.setText(node.getName());


        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                if (hasFocus) {
                    if (node.isFolder()) {
                        input.setSelection(0, input.getText().length());
                    } else {
                        String[] s = node.getName().split("\\.");
                        if (s != null) {
                            int numParts = s.length;
                            int lastSelectedPos = 0;
                            if (numParts == 1) {
                                input.setSelection(0, input.getText().length());
                            } else if (numParts > 1) {
                                for (int i = 0; i < (numParts - 1); i++) {
                                    lastSelectedPos += s[i].length();
                                    lastSelectedPos++;
                                }
                                lastSelectedPos--; //The last point should not be selected)
                                input.setSelection(0, lastSelectedPos);
                            }
                        }
                        showKeyboardDelayed(v);
                    }
                }
            }
        });


        layout.addView(input, params);

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(Util.scaleWidthPx(20, outMetrics), 0, Util.scaleWidthPx(17, outMetrics), 0);

        final RelativeLayout error_layout = new RelativeLayout(PdfViewerActivityLollipop.this);
        layout.addView(error_layout, params1);

        final ImageView error_icon = new ImageView(PdfViewerActivityLollipop.this);
        error_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_input_warning));
        error_layout.addView(error_icon);
        RelativeLayout.LayoutParams params_icon = (RelativeLayout.LayoutParams) error_icon.getLayoutParams();

        params_icon.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        error_icon.setLayoutParams(params_icon);

        error_icon.setColorFilter(ContextCompat.getColor(PdfViewerActivityLollipop.this, R.color.login_warning));

        final TextView textError = new TextView(PdfViewerActivityLollipop.this);
        error_layout.addView(textError);
        RelativeLayout.LayoutParams params_text_error = (RelativeLayout.LayoutParams) textError.getLayoutParams();
        params_text_error.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error.addRule(RelativeLayout.CENTER_VERTICAL);
        params_text_error.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params_text_error.setMargins(Util.scaleWidthPx(3, outMetrics), 0, 0, 0);
        textError.setLayoutParams(params_text_error);

        textError.setTextColor(ContextCompat.getColor(PdfViewerActivityLollipop.this, R.color.login_warning));

        error_layout.setVisibility(View.GONE);

        input.getBackground().mutate().clearColorFilter();
        input.getBackground().mutate().setColorFilter(ContextCompat.getColor(this, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (error_layout.getVisibility() == View.VISIBLE) {
                    error_layout.setVisibility(View.GONE);
                    input.getBackground().mutate().clearColorFilter();
                    input.getBackground().mutate().setColorFilter(ContextCompat.getColor(pdfViewerActivityLollipop, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    String value = v.getText().toString().trim();
                    if (value.length() == 0) {
                        input.getBackground().mutate().setColorFilter(ContextCompat.getColor(pdfViewerActivityLollipop, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
                        textError.setText(getString(R.string.invalid_string));
                        error_layout.setVisibility(View.VISIBLE);
                        input.requestFocus();

                    } else {
                        boolean result = matches(regex, value);
                        if (result) {
                            input.getBackground().mutate().setColorFilter(ContextCompat.getColor(pdfViewerActivityLollipop, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
                            textError.setText(getString(R.string.invalid_characters));
                            error_layout.setVisibility(View.VISIBLE);
                            input.requestFocus();

                        } else {
                            //						nC.renameNode(node, value);
                            renameDialog.dismiss();
                            rename(value, node);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.context_rename) + " "	+ new String(node.getName()));
        builder.setPositiveButton(getString(R.string.context_rename),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString().trim();
                        if (value.length() == 0) {
                            return;
                        }
                        rename(value, node);
                    }
                });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                input.getBackground().clearColorFilter();
            }
        });
        builder.setView(layout);
        renameDialog = builder.create();
        renameDialog.show();
        renameDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new   View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String value = input.getText().toString().trim();

                if (value.length() == 0) {
                    input.getBackground().mutate().setColorFilter(ContextCompat.getColor(pdfViewerActivityLollipop, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
                    textError.setText(getString(R.string.invalid_string));
                    error_layout.setVisibility(View.VISIBLE);
                    input.requestFocus();
                }
                else{
                    boolean result=matches(regex, value);
                    if(result){
                        input.getBackground().mutate().setColorFilter(ContextCompat.getColor(pdfViewerActivityLollipop, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
                        textError.setText(getString(R.string.invalid_characters));
                        error_layout.setVisibility(View.VISIBLE);
                        input.requestFocus();

                    }else{
                        //nC.renameNode(node, value);
                        renameDialog.dismiss();
                        rename(value, node);
                    }
                }
            }
        });
    }

    private void rename(String newName, MegaNode node){
        if (newName.equals(node.getName())) {
            return;
        }

        if(!Util.isOnline(this)){
            Snackbar.make(pdfviewerContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
            return;
        }

        if (isFinishing()){
            return;
        }

        ProgressDialog temp = null;
        try{
            temp = new ProgressDialog(this);
            temp.setMessage(getString(R.string.context_renaming));
            temp.show();
        }
        catch(Exception e){
            return;
        }
        statusDialog = temp;

        log("renaming " + node.getName() + " to " + newName);

        megaApi.renameNode(node, newName, this);
    }

    public static boolean matches(String regex, CharSequence input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        return m.find();
    }

    public void showPropertiesActivity(){
        Intent i = new Intent(this, FileInfoActivityLollipop.class);
        if (isOffLine){
            i.putExtra("name", pdfFileName);
            i.putExtra("imageId", MimeTypeThumbnail.typeForName(pdfFileName).getIconResourceId());
            i.putExtra("adapterType", Constants.OFFLINE_ADAPTER);
            i.putExtra("path", path);
            if (pathNavigation != null){
                i.putExtra("pathNavigation", pathNavigation);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.setDataAndType(uri, MimeTypeList.typeForName(pdfFileName).getType());
            }
            else{
                i.setDataAndType(uri, MimeTypeList.typeForName(pdfFileName).getType());
            }
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else {
            MegaNode node = megaApi.getNodeByHandle(handle);
            i.putExtra("handle", node.getHandle());
            i.putExtra("imageId", MimeTypeThumbnail.typeForName(node.getName()).getIconResourceId());
            i.putExtra("name", node.getName());
        }
        startActivity(i);
        renamed = false;
    }

    public void showRemoveLink(){
        android.support.v7.app.AlertDialog removeLinkDialog;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_link, null);
        TextView url = (TextView) dialoglayout.findViewById(R.id.dialog_link_link_url);
        TextView key = (TextView) dialoglayout.findViewById(R.id.dialog_link_link_key);
        TextView symbol = (TextView) dialoglayout.findViewById(R.id.dialog_link_symbol);
        TextView removeText = (TextView) dialoglayout.findViewById(R.id.dialog_link_text_remove);

        ((RelativeLayout.LayoutParams) removeText.getLayoutParams()).setMargins(Util.scaleWidthPx(25, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(10, outMetrics), 0);

        url.setVisibility(View.GONE);
        key.setVisibility(View.GONE);
        symbol.setVisibility(View.GONE);
        removeText.setVisibility(View.VISIBLE);

        removeText.setText(getString(R.string.context_remove_link_warning_text));

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;

        float scaleW = Util.getScaleW(outMetrics, density);
        float scaleH = Util.getScaleH(outMetrics, density);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            removeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, (10*scaleW));
        }else{
            removeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, (15*scaleW));

        }

        builder.setView(dialoglayout);

        builder.setPositiveButton(getString(R.string.context_remove), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                typeExport=TYPE_EXPORT_REMOVE;
                megaApi.disableExport(megaApi.getNodeByHandle(handle), pdfViewerActivityLollipop);
            }
        });

        builder.setNegativeButton(getString(R.string.general_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        removeLinkDialog = builder.create();
        removeLinkDialog.show();
    }

    public void showGetLinkActivity(){
        log("showGetLinkActivity");
        Intent linkIntent = new Intent(this, GetLinkActivityLollipop.class);
        linkIntent.putExtra("handle", handle);
        linkIntent.putExtra("account", accountType);
        startActivity(linkIntent);
    }

    public void intentToSendFile(){
        log("intentToSendFile");

        if(uri!=null){
            if (!isUrl){
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("application/pdf");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    log("Use provider to share");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri.toString()));
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                else{
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                }
                startActivity(Intent.createChooser(share, getString(R.string.context_share)));
            }
            else {
                Snackbar.make(pdfviewerContainer, getString(R.string.not_download), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void updateFile (){
        MegaNode file = null;
        if (pdfFileName != null && handle != -1 ) {
            file = megaApi.getNodeByHandle(handle);
            if (file != null){
                log("Pdf File: "+pdfFileName+" node file: "+file.getName());
                if (!pdfFileName.equals(file.getName())) {
                    log("updateFile");

                    pdfFileName = file.getName();
                    if (aB != null){
                        tB = (Toolbar) findViewById(R.id.toolbar_pdf_viewer);
                        if(tB==null){
                            log("Tb is Null");
                            return;
                        }
                        tB.setVisibility(View.VISIBLE);
                        setSupportActionBar(tB);
                        aB = getSupportActionBar();
                    }
                    aB.setTitle(" ");
                    setTitle(pdfFileName);
                    fileNameTextView.setText(pdfFileName);
                    supportInvalidateOptionsMenu();


                    getDownloadLocation();
                    boolean isOnMegaDownloads = false;
                    String localPath = Util.getLocalFile(this, file.getName(), file.getSize(), downloadLocationDefaultPath);
                    File f = new File(downloadLocationDefaultPath, file.getName());
                    if(f.exists() && (f.length() == file.getSize())){
                        isOnMegaDownloads = true;
                    }
                    if (localPath != null && (isOnMegaDownloads || (megaApi.getFingerprint(file).equals(megaApi.getFingerprint(localPath))))){
                        File mediaFile = new File(localPath);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && prefs.getStorageDownloadLocation().contains(Environment.getExternalStorageDirectory().getPath())) {
                            uri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", mediaFile);
                        }
                        else{
                            uri = Uri.fromFile(mediaFile);
                        }
                    }
                    else {
                        if (megaApi == null){
                            MegaApplication app = (MegaApplication)getApplication();
                            megaApi = app.getMegaApi();
                            megaApi.addTransferListener(this);
                            megaApi.addGlobalListener(this);
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
                        if (url != null){
                            uri = Uri.parse(url);
                        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        log("-------------------onActivityResult " + requestCode + "____" + resultCode);
        if (intent == null) {
            return;
        }

        if (requestCode == Constants.REQUEST_CODE_SELECT_CHAT && resultCode == RESULT_OK){
            long[] chatHandles = intent.getLongArrayExtra("SELECTED_CHATS");
            log("Send to "+chatHandles.length+" chats");

            long[] nodeHandles = intent.getLongArrayExtra("NODE_HANDLES");
            log("Send "+nodeHandles.length+" nodes");

            countChat = chatHandles.length;
            if (megaChatApi != null) {
                if(countChat==1){
                    megaChatApi.attachNode(chatHandles[0], nodeHandles[0], this);
                }
                else if(countChat>1){

                    for(int i=0; i<chatHandles.length; i++){
                        megaChatApi.attachNode(chatHandles[i], nodeHandles[0], this);
                    }
                }
            }
            else{
                log("megaChatApi is Null - cannot attach nodes");
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
            log("local folder selected");
            String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
            if (type == Constants.FILE_LINK_ADAPTER){
                downloadTo(parentPath, uri.toString(), currentDocument.getSize(), currentDocument.getHandle());
            }
            else {
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
        else if (requestCode == Constants.REQUEST_CODE_SELECT_MOVE_FOLDER && resultCode == RESULT_OK) {

            if(!Util.isOnline(this)){
                Snackbar.make(pdfviewerContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
                return;
            }

            final long[] moveHandles = intent.getLongArrayExtra("MOVE_HANDLES");
            final long toHandle = intent.getLongExtra("MOVE_TO", 0);
            final int totalMoves = moveHandles.length;

            MegaNode parent = megaApi.getNodeByHandle(toHandle);
            moveToRubbish = false;

            ProgressDialog temp = null;
            try{
                temp = new ProgressDialog(this);
                temp.setMessage(getString(R.string.context_moving));
                temp.show();
            }
            catch(Exception e){
                return;
            }
            statusDialog = temp;

            for(int i=0; i<moveHandles.length;i++){
                megaApi.moveNode(megaApi.getNodeByHandle(moveHandles[i]), parent, this);
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_SELECT_COPY_FOLDER && resultCode == RESULT_OK){
            if(!Util.isOnline(this)){
                Snackbar.make(pdfviewerContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
                return;
            }

            final long[] copyHandles = intent.getLongArrayExtra("COPY_HANDLES");
            final long toHandle = intent.getLongExtra("COPY_TO", 0);
            final int totalCopy = copyHandles.length;

            ProgressDialog temp = null;
            try{
                temp = new ProgressDialog(this);
                temp.setMessage(getString(R.string.context_copying));
                temp.show();
            }
            catch(Exception e){
                return;
            }
            statusDialog = temp;

            MegaNode parent = megaApi.getNodeByHandle(toHandle);
            for(int i=0; i<copyHandles.length;i++){
                MegaNode cN = megaApi.getNodeByHandle(copyHandles[i]);
                if (cN != null){
                    log("cN != null, i = " + i + " of " + copyHandles.length);
                    megaApi.copyNode(cN, parent, this);
                }
                else{
                    log("cN == null, i = " + i + " of " + copyHandles.length);
                    try {
                        statusDialog.dismiss();
                        Snackbar.make(pdfviewerContainer, getString(R.string.context_no_copied), Snackbar.LENGTH_LONG).show();
                    }
                    catch (Exception ex) {}
                }
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_SELECT_IMPORT_FOLDER && resultCode == RESULT_OK){
            log("onActivityResult REQUEST_CODE_SELECT_IMPORT_FOLDER OK");

            if(!Util.isOnline(this)||megaApi==null) {
                try{
                    statusDialog.dismiss();
                } catch(Exception ex) {};
                Snackbar.make(pdfviewerContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
                return;
            }

            final long toHandle = intent.getLongExtra("IMPORT_TO", 0);
            MegaNode target = null;
            target = megaApi.getNodeByHandle(toHandle);
            if(target == null){
                target = megaApi.getRootNode();
            }
            log("TARGET: " + target.getName() + "and handle: " + target.getHandle());
            if (nodeChat != null) {
                log("DOCUMENT: " + nodeChat.getName() + "_" + nodeChat.getHandle());
                if (target != null) {
                    megaApi.copyNode(nodeChat, target, this);
                } else {
                    log("TARGET: null");
                    Snackbar.make(pdfviewerContainer, getString(R.string.import_success_error), Snackbar.LENGTH_LONG).show();
                }
            }
            else{
                log("DOCUMENT: null");
                Snackbar.make(pdfviewerContainer, getString(R.string.import_success_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        log("page: "+page);
        if (!notChangePage) {
            currentPage = page+1;
            actualPage.setText(String.valueOf(currentPage));
            setTitle(String.format("%s %s / %s", pdfFileName, currentPage, pageCount));
        }
        else {
            notChangePage = false;
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        log("Cannot load page " + page);
    }

    @Override
    public void loadComplete(int nbPages) {
        totalPages.setText(""+nbPages);
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        log("title = " + meta.getTitle());
        log("author = " + meta.getAuthor());
        log("subject = " + meta.getSubject());
        log("keywords = " + meta.getKeywords());
        log("creator = " + meta.getCreator());
        log("producer = " + meta.getProducer());
        log("creationDate = " + meta.getCreationDate());
        log("modDate = " + meta.getModDate());
        printBookmarksTree(pdfView.getTableOfContents(), "-");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (toolbarVisible)
                    setToolbarVisibilityHide(200L);
            }
        }, 2000);
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            log(String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
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

    public static void log(String log) {
        Util.log("PdfViewerActivityLollipop", log);
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestFinish");

        MegaNode node = megaApi.getNodeByHandle(request.getNodeHandle());

        if (request.getType() == MegaRequest.TYPE_LOGIN){

            if (e.getErrorCode() != MegaError.API_OK) {

                MegaApplication.setLoggingIn(false);

                DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
                dbH.clearCredentials();
                if (dbH.getPreferences() != null){
                    dbH.clearPreferences();
                    dbH.setFirstTime(false);
                }
            }
            else{
                //LOGIN OK

                gSession = megaApi.dumpSession();
                credentials = new UserCredentials(lastEmail, gSession, "", "", "");

                DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
                dbH.clearCredentials();

                log("Logged in with session");

                megaApi.fetchNodes(this);
            }
        }
        else if (request.getType() == MegaRequest.TYPE_FETCH_NODES){

            if (e.getErrorCode() == MegaError.API_OK){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = this.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_dark_primary_color));

                }
                DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());

                gSession = megaApi.dumpSession();
                MegaUser myUser = megaApi.getMyUser();
                String myUserHandle = "";
                if(myUser!=null){
                    lastEmail = megaApi.getMyUser().getEmail();
                    myUserHandle = megaApi.getMyUser().getHandle()+"";
                }

                credentials = new UserCredentials(lastEmail, gSession, "", "", myUserHandle);

                dbH.saveCredentials(credentials);

                chatSettings = dbH.getChatSettings();
                if(chatSettings!=null) {

                    boolean chatEnabled = Boolean.parseBoolean(chatSettings.getEnabled());
                    if(chatEnabled){

                        log("Chat enabled-->connect");
                        if((megaChatApi.getInitState()!=MegaChatApi.INIT_ERROR)){
                            log("Connection goes!!!");
                            megaChatApi.connect(this);
                        }
                        else{
                            log("Not launch connect: "+megaChatApi.getInitState());
                        }
                        MegaApplication.setLoggingIn(false);
                        download();
                    }
                    else{

                        log("Chat NOT enabled - readyToManager");
                        MegaApplication.setLoggingIn(false);
                        download();
                    }
                }
                else{
                    log("chatSettings NULL - readyToManager");
                    MegaApplication.setLoggingIn(false);
                    download();
                }
            }
        }
        else if (request.getType() == MegaRequest.TYPE_RENAME){

            try {
                statusDialog.dismiss();
            }
            catch (Exception ex) {}

            if (e.getErrorCode() == MegaError.API_OK){
                Snackbar.make(pdfviewerContainer, getString(R.string.context_correctly_renamed), Snackbar.LENGTH_LONG).show();
                updateFile();
            }
            else{
                Snackbar.make(pdfviewerContainer, getString(R.string.context_no_renamed), Snackbar.LENGTH_LONG).show();
            }
        }
        else if (request.getType() == MegaRequest.TYPE_MOVE){
            try {
                statusDialog.dismiss();
            }
            catch (Exception ex) {}

            if (moveToRubbish){
                if (e.getErrorCode() == MegaError.API_OK){
                    this.finish();
                }
                else{
                    Snackbar.make(pdfviewerContainer, getString(R.string.context_no_moved), Snackbar.LENGTH_LONG).show();
                }
                moveToRubbish = false;
                log("move to rubbish request finished");
            }
            else{
                if (e.getErrorCode() == MegaError.API_OK){
                    Snackbar.make(pdfviewerContainer, getString(R.string.context_correctly_moved), Snackbar.LENGTH_LONG).show();
                    finish();
                }
                else{
                    Snackbar.make(pdfviewerContainer, getString(R.string.context_no_moved), Snackbar.LENGTH_LONG).show();
                }
                log("move nodes request finished");
            }
        }
        else if (request.getType() == MegaRequest.TYPE_REMOVE){


            if (e.getErrorCode() == MegaError.API_OK){
                if (moveToTrashStatusDialog.isShowing()){
                    try {
                        moveToTrashStatusDialog.dismiss();
                    }
                    catch (Exception ex) {}
                    Snackbar.make(pdfviewerContainer, getString(R.string.context_correctly_removed), Snackbar.LENGTH_LONG).show();
                }
                finish();
            }
            else{
                Snackbar.make(pdfviewerContainer, getString(R.string.context_no_removed), Snackbar.LENGTH_LONG).show();
            }
            log("remove request finished");
        }
        else if (request.getType() == MegaRequest.TYPE_COPY){
            try {
                statusDialog.dismiss();
            }
            catch (Exception ex) {}

            if (e.getErrorCode() == MegaError.API_OK){
                Snackbar.make(pdfviewerContainer, getString(R.string.context_correctly_copied), Snackbar.LENGTH_LONG).show();
            }
            else{
                Snackbar.make(pdfviewerContainer, getString(R.string.context_no_copied), Snackbar.LENGTH_LONG).show();
            }
            log("copy nodes request finished");
        }
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestTemporaryError");
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish - MegaChatApi");

        if (request.getType() == MegaChatRequest.TYPE_CONNECT){
            MegaApplication.setLoggingIn(false);
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("Connected to chat!");
            }
            else{
                log("ERROR WHEN CONNECTING " + e.getErrorString());
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_ATTACH_NODE_MESSAGE){

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
        if (!isOffLine && !fromChat && !isFolderLink
                && type != Constants.FILE_LINK_ADAPTER){
            if (megaApi.getNodeByHandle(handle) == null && inside && !fromDownload){
                finish();
            }
            updateFile();
        }
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
            megaApi.removeGlobalListener(this);
            megaApi.httpServerStop();
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        super.onDestroy();
    }

    public void showTransferOverquotaDialog(){
        log("showTransferOverquotaDialog");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PdfViewerActivityLollipop.this);

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
                progressBar.setVisibility(View.GONE);
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                alertDialogTransferOverquota.dismiss();
                transferOverquota = false;
                if (loading && !transferOverquota){
                    progressBar.setVisibility(View.VISIBLE);
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

    public void showSnackbar(String s){
        log("showSnackbar");
        Snackbar snackbar = Snackbar.make(pdfviewerContainer, s, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);
        snackbar.show();
    }

    public void openAdvancedDevices (long handleToDownload){
        log("openAdvancedDevices");
//		handleToDownload = handle;
        String externalPath = Util.getExternalCardPath();

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
        Snackbar mySnackbar = Snackbar.make(pdfviewerContainer, R.string.error_not_enough_free_space, Snackbar.LENGTH_LONG);
        mySnackbar.setAction("Settings", new SnackbarNavigateOption(this));
        mySnackbar.show();
    }

    public void askSizeConfirmationBeforeChatDownload(String parentPath, ArrayList<MegaNode> nodeList, long size){
        log("askSizeConfirmationBeforeChatDownload");

        final String parentPathC = parentPath;
        final ArrayList<MegaNode> nodeListC = nodeList;
        final long sizeC = size;
        final ChatController chatC = new ChatController(this);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        LinearLayout confirmationLayout = new LinearLayout(this);
        confirmationLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(10, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

        final CheckBox dontShowAgain =new CheckBox(this);
        dontShowAgain.setText(getString(R.string.checkbox_not_show_again));
        dontShowAgain.setTextColor(getResources().getColor(R.color.text_secondary));

        confirmationLayout.addView(dontShowAgain, params);

        builder.setView(confirmationLayout);

        builder.setMessage(getString(R.string.alert_larger_file, Util.getSizeString(sizeC)));
        builder.setPositiveButton(getString(R.string.general_download),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(dontShowAgain.isChecked()){
                            dbH.setAttrAskSizeDownload("false");
                        }
                        chatC.download(parentPathC, nodeListC);
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
        params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(10, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

        final CheckBox dontShowAgain =new CheckBox(this);
        dontShowAgain.setText(getString(R.string.checkbox_not_show_again));
        dontShowAgain.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        confirmationLayout.addView(dontShowAgain, params);

        builder.setView(confirmationLayout);

//				builder.setTitle(getString(R.string.confirmation_required));

        builder.setMessage(getString(R.string.alert_larger_file, Util.getSizeString(sizeC)));
        builder.setPositiveButton(getString(R.string.general_download),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(dontShowAgain.isChecked()){
                            dbH.setAttrAskSizeDownload("false");
                        }
                        if(nC==null){
                            nC = new NodeController(PdfViewerActivityLollipop.this);
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
        params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(10, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

        final CheckBox dontShowAgain =new CheckBox(this);
        dontShowAgain.setText(getString(R.string.checkbox_not_show_again));
        dontShowAgain.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

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
                            nC = new NodeController(PdfViewerActivityLollipop.this);
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

    public int getAccountType() {
        return accountType;
    }
}
