package nz.mega.android.lollipop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nz.mega.android.CameraSyncService;
import nz.mega.android.CameraUploadFragment;
import nz.mega.android.ChangePasswordActivity;
import nz.mega.android.ContactsExplorerActivity;
import nz.mega.android.CreditCardFragment;
import nz.mega.android.DatabaseHandler;
import nz.mega.android.DownloadService;
import nz.mega.android.FileLinkActivity;
import nz.mega.android.FileStorageActivity;
import nz.mega.android.FileStorageActivity.Mode;
import nz.mega.android.FolderLinkActivity;
import nz.mega.android.FortumoFragment;
import nz.mega.android.MegaApplication;
import nz.mega.android.MegaAttributes;
import nz.mega.android.MegaOffline;
import nz.mega.android.MegaPreferences;
import nz.mega.android.MimeTypeList;
import nz.mega.android.MyAccountFragment;
import nz.mega.android.OfflineActivity;
import nz.mega.android.OldPreferences;
import nz.mega.android.PaymentFragment;
import nz.mega.android.PinActivity;
import nz.mega.android.Product;
import nz.mega.android.R;
import nz.mega.android.ReceivedRequestsFragment;
import nz.mega.android.SearchFragment;
import nz.mega.android.SecureSelfiePreviewActivity;
import nz.mega.android.SettingsActivity;
import nz.mega.android.ShareInfo;
import nz.mega.android.SortByDialogActivity;
import nz.mega.android.TabsAdapter;
import nz.mega.android.TourActivity;
import nz.mega.android.TransfersFragment;
import nz.mega.android.TransfersHolder;
import nz.mega.android.UpgradeAccountFragment;
import nz.mega.android.UploadHereDialog;
import nz.mega.android.UploadService;
import nz.mega.android.ZipBrowserActivity;
import nz.mega.android.utils.PreviewUtils;
import nz.mega.android.utils.ThumbnailUtils;
import nz.mega.android.utils.Util;
import nz.mega.android.utils.billing.IabHelper;
import nz.mega.android.utils.billing.IabResult;
import nz.mega.android.utils.billing.Inventory;
import nz.mega.android.utils.billing.Purchase;
import nz.mega.components.EditTextCursorWatcher;
import nz.mega.components.RoundedImageView;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferListenerInterface;
import nz.mega.sdk.MegaUser;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ManagerActivityLollipop extends PinActivity implements OnItemClickListener, OnClickListener, MegaRequestListenerInterface, MegaGlobalListenerInterface, MegaTransferListenerInterface {
	
	public enum DrawerItem {
		CLOUD_DRIVE, SAVED_FOR_OFFLINE, CAMERA_UPLOADS, INBOX, SHARED_WITH_ME, CONTACTS, SETTINGS, ACCOUNT, SEARCH;

		public String getTitle(Context context) {
			switch(this)
			{
				case CLOUD_DRIVE: return context.getString(R.string.section_cloud_drive);				
				case SAVED_FOR_OFFLINE: return context.getString(R.string.section_saved_for_offline);
				case CAMERA_UPLOADS: return context.getString(R.string.section_photo_sync);
				case INBOX: return context.getString(R.string.section_inbox);
				case SHARED_WITH_ME: return context.getString(R.string.section_shared_items);
				case CONTACTS: return context.getString(R.string.section_contacts);
				case SETTINGS: return context.getString(R.string.action_settings);
				case ACCOUNT: return context.getString(R.string.section_account);				
				case SEARCH: return context.getString(R.string.action_search);
			}
			return null;			
		}
	}
	
	public static int POS_CAMERA_UPLOADS = 2;
	
	public static int DEFAULT_AVATAR_WIDTH_HEIGHT = 250; //in pixels
	
	final public static int MY_ACCOUNT_FRAGMENT = 5000;
	final public static int UPGRADE_ACCOUNT_FRAGMENT = 5001;
	final public static int PAYMENT_FRAGMENT = 5002;
	final public static int OVERQUOTA_ALERT = 5003;
	final public static int CC_FRAGMENT = 5004;
	final public static int FORTUMO_FRAGMENT = 5005;
	
	public static int REQUEST_CODE_GET = 1000;
	public static int REQUEST_CODE_SELECT_MOVE_FOLDER = 1001;
	public static int REQUEST_CODE_SELECT_COPY_FOLDER = 1002;
	public static int REQUEST_CODE_GET_LOCAL = 1003;
	public static int REQUEST_CODE_SELECT_LOCAL_FOLDER = 1004;
	public static int REQUEST_CODE_REFRESH = 1005;
	public static int REQUEST_CODE_SORT_BY = 1006;
	public static int REQUEST_CODE_SELECT_IMPORT_FOLDER = 1007;
	public static int REQUEST_CODE_SELECT_FOLDER = 1008;
	public static int REQUEST_CODE_SELECT_CONTACT = 1009;
	public static int TAKE_PHOTO_CODE = 1010;
	private static int WRITE_SD_CARD_REQUEST_CODE = 1011;
	private static int REQUEST_CODE_SELECT_FILE = 1012;
	
	public static String ACTION_TAKE_SELFIE = "TAKE_SELFIE";
	public static String ACTION_CANCEL_DOWNLOAD = "CANCEL_DOWNLOAD";
	public static String ACTION_CANCEL_UPLOAD = "CANCEL_UPLOAD";
	public static String ACTION_CANCEL_CAM_SYNC = "CANCEL_CAM_SYNC";
	public static String ACTION_OPEN_MEGA_LINK = "OPEN_MEGA_LINK";
	public static String ACTION_OPEN_MEGA_FOLDER_LINK = "OPEN_MEGA_FOLDER_LINK";
	public static String ACTION_IMPORT_LINK_FETCH_NODES = "IMPORT_LINK_FETCH_NODES";
	public static String ACTION_FILE_EXPLORER_UPLOAD = "FILE_EXPLORER_UPLOAD";
	public static String ACTION_REFRESH_PARENTHANDLE_BROWSER = "REFRESH_PARENTHANDLE_BROWSER";
	public static String ACTION_OPEN_PDF = "OPEN_PDF";
	public static String EXTRA_PATH_PDF = "PATH_PDF";
	public static String EXTRA_OPEN_FOLDER = "EXTRA_OPEN_FOLER";
	public static String ACTION_EXPLORE_ZIP = "EXPLORE_ZIP";
	public static String EXTRA_PATH_ZIP = "PATH_ZIP";
	public static String EXTRA_HANDLE_ZIP = "HANDLE_ZIP";
	public static String ACTION_OVERQUOTA_ALERT = "OVERQUOTA_ALERT";
	public static String ACTION_FILE_PROVIDER = "ACTION_FILE_PROVIDER";
	
	final public static int FILE_BROWSER_ADAPTER = 2000;
	final public static int CONTACT_FILE_ADAPTER = 2001;
	final public static int RUBBISH_BIN_ADAPTER = 2002;
	final public static int SHARED_WITH_ME_ADAPTER = 2003;
	final public static int OFFLINE_ADAPTER = 2004;
	final public static int FOLDER_LINK_ADAPTER = 2005;
	final public static int SEARCH_ADAPTER = 2006;
	final public static int PHOTO_SYNC_ADAPTER = 2007;
	final public static int ZIP_ADAPTER = 2008;
	final public static int OUTGOING_SHARES_ADAPTER = 2009;
	final public static int INCOMING_SHARES_ADAPTER = 2010;
	final public static int INBOX_ADAPTER = 2011;
	final public static int INCOMING_REQUEST_ADAPTER = 2012;
	final public static int OUTGOING_REQUEST_ADAPTER = 2013;
	
	public static int MODE_IN = 0;
	public static int MODE_OUT = 1;
	
	boolean megaContacts = true;
	ArrayList<String> contactsData;
	
	String accessToken;
	String feedback;
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
   	private SearchView mSearchView;    
	private MenuItem searchMenuItem;
	
	private MenuItem createFolderMenuItem;
	private MenuItem importLinkMenuItem;
	private MenuItem addMenuItem;
	private MenuItem pauseRestartTransfersItem;
	private MenuItem refreshMenuItem;
	private MenuItem sortByMenuItem;
	private MenuItem helpMenuItem;
	private MenuItem upgradeAccountMenuItem;
	private MenuItem settingsMenuItem;
	private MenuItem selectMenuItem;
	private MenuItem unSelectMenuItem;
	private MenuItem thumbViewMenuItem;
	private MenuItem addContactMenuItem;
	private MenuItem rubbishBinMenuItem;
	private MenuItem clearRubbishBinMenuitem;
	private MenuItem changePass;
	private MenuItem exportMK;
	private MenuItem removeMK;
	private MenuItem takePicture;
	private MenuItem cancelSubscription;
	private MenuItem killAllSessions;
	
	public int accountFragment;
	
	private static DrawerItem drawerItem;
	private static DrawerItem lastDrawerItem;
	
	private TableLayout topControlBar;
	private TableLayout bottomControlBar;
	private RoundedImageView imageProfile;
	private TextView textViewProfile;
	private TextView userName;
	private TextView userEmail;
	private TextView usedSpaceText;
	private TextView usedSpace;
	private ImageView usedSpaceWarning;
	private int usedPerc=0;
	
	ProgressBar usedSpaceBar;
	
	MegaUser contact = null;
	
	//ImageButton customListGrid;
	LinearLayout customSearch;
	AlertDialog permissionsDialog;
	private boolean firstTime = true;
	
	long handleToDownload=0;
	
	long parentHandleBrowser;
	long parentHandleRubbish;
	long parentHandleIncoming;
	long parentHandleOutgoing;
	long parentHandleSearch;
	long parentHandleInbox;
	private boolean isListCloudDrive = true;
	private boolean isListContacts = true;
	private boolean isListRubbishBin = true;
	private boolean isListSharedWithMe = true;
	private boolean isListOffline = true;
	private boolean isListCameraUpload = false;
	private boolean isListInbox = true;
	private ReceivedRequestsFragment rRF;
    private TransfersFragment tF; 
    private MyAccountFragment maF;
    private SearchFragment sF;
    private CameraUploadFragment psF;
    private UpgradeAccountFragment upAF;
    private PaymentFragment pF;
    private CreditCardFragment ccF;
    private FortumoFragment fF;    
    
    /////LOLLIPOP FRAGMENTS
    private FileBrowserFragmentLollipop fbFLol;  
    private RubbishBinFragmentLollipop rbFLol;
    private InboxFragmentLollipop iFLol;
	private IncomingSharesFragmentLollipop inSFLol;
	private OutgoingSharesFragmentLollipop outSFLol;
    private OfflineFragmentLollipop oFLol;
	private ContactsFragmentLollipop cFLol;
	private SentRequestsFragmentLollipop sRFLol;
    //////
    
    TextView textViewBrowser; 
	TextView textViewRubbish;
    TextView textViewIncoming; 
	TextView textViewOutgoing;
    
    //Tabs in Contacts
    private TabHost mTabHostContacts;
    //private Fragment contactTabFragment;	
	TabsAdapter mTabsAdapterContacts;
    ViewPager viewPagerContacts;  
    //Tabs in Shares
    private TabHost mTabHostShares;
	TabsAdapter mTabsAdapterShares;
    ViewPager viewPagerShares;     
    //Tabs in Cloud
    private TabHost mTabHostCDrive;
	TabsAdapter mTabsAdapterCDrive;
    ViewPager viewPagerCDrive; 
    
    static ManagerActivityLollipop managerActivity;
    private MegaApiAndroid megaApi;
    
    private static int EDIT_TEXT_ID = 1;  
    private AlertDialog renameDialog;
    private AlertDialog openLinkDialog;
    private AlertDialog newFolderDialog;
    private AlertDialog addContactDialog;
    private AlertDialog clearRubbishBinDialog;
    private AlertDialog alertNotPermissionsUpload;
    private AlertDialog alertPermissionWiFi;
    private Handler handler;    
    private boolean moveToRubbish = false;
    private boolean sendToInbox = false;
    private boolean isClearRubbishBin = false;
    
    ProgressDialog statusDialog;
    
	public UploadHereDialog uploadDialog;	
	private List<ShareInfo> filePreparedInfos;	
	private int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;
	private int orderContacts = MegaApiJava.ORDER_DEFAULT_ASC;
	private int orderOffline = MegaApiJava.ORDER_DEFAULT_ASC;
	private int orderOutgoing = MegaApiJava.ORDER_DEFAULT_ASC;
	private int orderIncoming = MegaApiJava.ORDER_DEFAULT_ASC;
	
	ActionBar aB;	
	String urlLink = "";		
	SparseArray<TransfersHolder> transfersListArray = null;	
	boolean downloadPlay = true;	
	boolean pauseIconVisible = false;	
	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;
	MegaAttributes attr = null;	
	ArrayList<MegaTransfer> tL;	
	String searchQuery = null;
	ArrayList<MegaNode> searchNodes;
	int levelsSearch = -1;
	private boolean openLink = false;	
	MegaApplication app;	
	NavigationDrawerLollipopAdapter nDALol;
	String pathNavigation = "/";	
	long lastTimeOnTransferUpdate = -1;	
	boolean firstTimeCam = false;
	int accountType = -1;
	MegaAccountDetails accountInfo = null;
	long usedGbStorage = -1;
	AlertDialog overquotaDialog;
	
	String titleAB = "";
	
	private boolean isGetLink = false;
	
	
	//Billing
	IabHelper mHelper;
	// SKU for our subscription PRO_I monthly
    static final String SKU_PRO_I_MONTH = "mega.android.pro1.onemonth";
    // SKU for our subscription PRO_I yearly
    static final String SKU_PRO_I_YEAR = "mega.android.pro1.oneyear";
    // SKU for our subscription PRO_II monthly
    static final String SKU_PRO_II_MONTH = "mega.android.pro2.onemonth";
    // SKU for our subscription PRO_III monthly
    static final String SKU_PRO_III_MONTH = "mega.android.pro3.onemonth";
    // SKU for our subscription PRO_LITE monthly
    static final String SKU_PRO_LITE_MONTH = "mega.android.prolite.onemonth";
    // SKU for our subscription PRO_LITE yearly
    static final String SKU_PRO_LITE_YEAR = "mega.android.prolite.oneyear";
    
    Purchase proLiteMonthly;
    Purchase proLiteYearly;
    Purchase proIMonthly;
    Purchase proIYearly;
    Purchase proIIMonthly;
    Purchase proIIIMonthly;
    
    Purchase maxP;
    
    boolean inventoryFinished = false;
    boolean accountDetailsFinished = false;
    
    int levelAccountDetails = -1;
    int levelInventory = -1;
    
    long numberOfSubscriptions = -1;
    
    BitSet paymentBitSet = null;
    
    /*
	 * Background task to emptying the Rubbish Bin
	 */
	private class ClearRubbisBinTask extends AsyncTask<String, Void, Void> {
		Context context;
		
		ClearRubbisBinTask(Context context){
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(String... params) {
			log("doInBackground-Async Task ClearRubbisBinTask");
			
			if (rbFLol != null){
				ArrayList<MegaNode> rubbishNodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
				
				isClearRubbishBin = true;
				for (int i=0; i<rubbishNodes.size(); i++){
					megaApi.remove(rubbishNodes.get(i), managerActivity);
				}
			}					
			return null;
		}		
	}	
	
    // (arbitrary) request code for the purchase flow
    public static final int RC_REQUEST = 10001;
    String orderId = "";
    
 // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            log("Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                log("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                log("Error purchasing. Authenticity verification failed.");
                return;
            }

            log("Purchase successful.");
            log("ORIGINAL JSON: ****_____" + purchase.getOriginalJson() + "____****");
            
            orderId = purchase.getOrderId();
//            Toast.makeText(getApplicationContext(), "ORDERID WHEN FINISHED: ****_____" + purchase.getOrderId() + "____*****", Toast.LENGTH_LONG).show();
            log("ORDERID WHEN FINISHED: ***____" + purchase.getOrderId() + "___***");
            if (purchase.getSku().equals(SKU_PRO_I_MONTH)) {
                log("PRO I Monthly subscription purchased.");
                alert("Thank you for subscribing to PRO I Monthly!");
            }
            else if (purchase.getSku().equals(SKU_PRO_I_YEAR)) {
                log("PRO I Yearly subscription purchased.");
                alert("Thank you for subscribing to PRO I Yearly!");
            }
            else if (purchase.getSku().equals(SKU_PRO_II_MONTH)) {
                log("PRO II Monthly subscription purchased.");
                alert("Thank you for subscribing to PRO II Monthly!");
            }
            else if (purchase.getSku().equals(SKU_PRO_III_MONTH)) {
                log("PRO III Monthly subscription purchased.");
                alert("Thank you for subscribing to PRO III Monthly!");
            }
            else if (purchase.getSku().equals(SKU_PRO_LITE_MONTH)) {
                log("PRO LITE Monthly subscription purchased.");
                alert("Thank you for subscribing to PRO LITE Monthly!");
            }
            else if (purchase.getSku().equals(SKU_PRO_LITE_YEAR)) {
                log("PRO LITE Yearly subscription purchased.");
                alert("Thank you for subscribing to PRO LITE Yearly!");
            }
            
            if (managerActivity != null){
            	megaApi.submitPurchaseReceipt(purchase.getOriginalJson(), managerActivity);
            }
            else{
            	megaApi.submitPurchaseReceipt(purchase.getOriginalJson());
            }
        }
    };
    
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
    
    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        log("Showing alert dialog: " + message);
        bld.create().show();
    }
	
	// Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            log("Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                log("Failed to query inventory: " + result);
                return;
            }

            log("Query inventory was successful.");
            
            proLiteMonthly = inventory.getPurchase(SKU_PRO_LITE_MONTH);
            proLiteYearly = inventory.getPurchase(SKU_PRO_LITE_YEAR);
            proIMonthly = inventory.getPurchase(SKU_PRO_I_MONTH);
            proIYearly = inventory.getPurchase(SKU_PRO_I_YEAR);
            proIIMonthly = inventory.getPurchase(SKU_PRO_II_MONTH);
            proIIIMonthly = inventory.getPurchase(SKU_PRO_III_MONTH);
           
            if (proLiteMonthly != null){
            	if (megaApi.getMyEmail() != null){
	        		if (proLiteMonthly.getDeveloperPayload().compareTo(megaApi.getMyEmail()) == 0){
	        			levelInventory = 0;	
	        			maxP = proLiteMonthly;
	        		}
            	}
        	}
            
            if (proLiteYearly != null){
            	if (megaApi.getMyEmail() != null){
	            	if (proLiteYearly.getDeveloperPayload().compareTo(megaApi.getMyEmail()) == 0){
	        			levelInventory = 0;
	        			maxP = proLiteYearly;
	        		}
            	}
        	}
            
            if (proIMonthly != null){
            	if (megaApi.getMyEmail() != null){
	            	if (proIMonthly.getDeveloperPayload().compareTo(megaApi.getMyEmail()) == 0){
	        			levelInventory = 1;	
	        			maxP = proIMonthly;
	        		}
            	}
        	}
            
            if (proIYearly!= null){
            	if (megaApi.getMyEmail() != null){
	            	if (proIYearly.getDeveloperPayload().compareTo(megaApi.getMyEmail()) == 0){
	        			levelInventory = 1;
	        			maxP = proIYearly;
	        		}
            	}
        	}
            
            if (proIIMonthly != null){
            	if (megaApi.getMyEmail() != null){
	            	if (proIIMonthly.getDeveloperPayload().compareTo(megaApi.getMyEmail()) == 0){
	        			levelInventory = 2;
	        			maxP = proIIMonthly;
	        		}
            	}
            }
            
            if (proIIIMonthly != null){
            	if (megaApi.getMyEmail() != null){
	            	if (proIIIMonthly.getDeveloperPayload().compareTo(megaApi.getMyEmail()) == 0){
	        			levelInventory = 3;	
	        			maxP = proIIIMonthly;
	        		}
            	}
            }
            
            inventoryFinished = true;
            
            if (accountDetailsFinished){
            	if (levelInventory > levelAccountDetails){
            		if (maxP != null){
            			megaApi.submitPurchaseReceipt(maxP.getOriginalJson(), managerActivity);
            		}
            	}
            }
            
            
            boolean isProIMonthly = false;
            if (proIMonthly != null){
            	isProIMonthly = true;
            }
            if (isProIMonthly){
            	log("PRO I IS SUBSCRIPTED: ORDERID: ***____" + proIMonthly.getOrderId() + "____*****");
            }
            else{
            	log("PRO I IS NOT SUBSCRIPTED");
            }
            
            if (!mHelper.subscriptionsSupported()) {
            	log("SUBSCRIPTIONS NOT SUPPORTED");
            }
            else{
            	log("SUBSCRIPTIONS SUPPORTED");
            	
//            	launchPayment();
            }            
            
            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
//            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
//            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
//            log("User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
//
//            // Do we have the infinite gas plan?
//            Purchase infiniteGasPurchase = inventory.getPurchase(SKU_INFINITE_GAS);
//            mSubscribedToInfiniteGas = (infiniteGasPurchase != null &&
//                    verifyDeveloperPayload(infiniteGasPurchase));
//            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
//                        + " infinite gas subscription.");
//            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;
//
//            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
//            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
//            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
//                log("We have gas. Consuming it.");
//                mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
//                return;
//            }
//
//            updateUi();
//            setWaitScreen(false);
            log("Initial inventory query finished.");
        }
    };
    
    void launchPayment(String productId){
    	/* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
    	String payload = megaApi.getMyEmail();
    	
    	if (mHelper == null){
    		initGooglePlayPayments();
    	}
    	
    	if (productId.compareTo(SKU_PRO_I_MONTH) == 0){
    		mHelper.launchPurchaseFlow(this,
        			SKU_PRO_I_MONTH, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);	
    	}
    	else if (productId.compareTo(SKU_PRO_I_YEAR) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_I_YEAR, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);	
    	}
    	else if (productId.compareTo(SKU_PRO_II_MONTH) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_II_MONTH, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);	
    	}
    	else if (productId.compareTo(SKU_PRO_III_MONTH) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_III_MONTH, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);	
    	}
    	else if (productId.compareTo(SKU_PRO_LITE_MONTH) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_LITE_MONTH, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);	
    	}
    	else if (productId.compareTo(SKU_PRO_LITE_YEAR) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_LITE_YEAR, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);	
    	}
    	
    }    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate");
		
//		Toast.makeText(this, "(): " + GooglePlayServicesUtil.isGooglePlayServicesAvailable(this), Toast.LENGTH_LONG).show();
		
		File thumbDir;
		if (getExternalCacheDir() != null){
			thumbDir = new File (getExternalCacheDir(), "thumbnailsMEGA");
			thumbDir.mkdirs();
		}
		else{
			thumbDir = getDir("thumbnailsMEGA", 0);
		}
		File previewDir;
		if (getExternalCacheDir() != null){
			previewDir = new File (getExternalCacheDir(), "previewsMEGA");
			previewDir.mkdirs();
		}
		else{
			previewDir = getDir("previewsMEGA", 0);
		}
		
//	    dbH = new DatabaseHandler(getApplicationContext());
		dbH = DatabaseHandler.getDbHandler(getApplicationContext());

		if (Util.isOnline(this)){
			dbH.setAttrOnline(true);
		}
		else{
			dbH.setAttrOnline(false);
		}		
    	
		super.onCreate(savedInstanceState);
		managerActivity = this;
		if (aB == null){
			aB = getSupportActionBar();
		}

		app = (MegaApplication)getApplication();
		
		initGooglePlayPayments();
		
//		// Get tracker.
//		Tracker t = app.getTracker(TrackerName.APP_TRACKER);
//		// Enable Advertising Features.
//		t.enableAdvertisingIdCollection(true);
//		// Set screen name.
//		t.setScreenName("Prueba");
//		// Send a screen view.
//		t.send(new HitBuilders.AppViewBuilder().build());
		
		
		megaApi = app.getMegaApi();
		
		log("retryPendingConnections()");
		if (megaApi != null){
			megaApi.retryPendingConnections();
		}
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);	    
   
	    if (dbH.getCredentials() == null){
			
			if (OldPreferences.getOldCredentials(this) != null){
	    		Intent loginWithOldCredentials = new Intent(this, LoginActivityLollipop.class);
	    		startActivity(loginWithOldCredentials);
	    		finish();
	    		return;
		    }
			
			Intent newIntent = getIntent();
		    
		    if (newIntent != null){
		    	if (newIntent.getAction() != null){
		    		if (newIntent.getAction().equals(ManagerActivityLollipop.ACTION_OPEN_MEGA_LINK) || newIntent.getAction().equals(ManagerActivityLollipop.ACTION_OPEN_MEGA_FOLDER_LINK)){
		    			openLink = true;
		    		}
		    		else if (newIntent.getAction().equals(ACTION_CANCEL_UPLOAD) || newIntent.getAction().equals(ACTION_CANCEL_DOWNLOAD) || newIntent.getAction().equals(ACTION_CANCEL_CAM_SYNC)){
		    			Intent cancelTourIntent = new Intent(this, TourActivity.class);
		    			cancelTourIntent.setAction(newIntent.getAction());
		    			startActivity(cancelTourIntent);
		    			finish();
		    			return;		    			
		    		}
		    	}
		    }
		    
		    if (!openLink){
		    	logout(this, megaApi, false);
		    }
		    
	    	return;
		}
				
		prefs = dbH.getPreferences();
		if (prefs == null){
			firstTime = true;
		}
		else{
			if (prefs.getFirstTime() == null){
				firstTime = true;
			}
			else{
				firstTime = Boolean.parseBoolean(prefs.getFirstTime());
			}
		}
				
		getOverflowMenu();
		
		handler = new Handler();
		
		setContentView(R.layout.activity_manager);

		imageProfile = (RoundedImageView) findViewById(R.id.profile_photo);
		textViewProfile = (TextView) findViewById(R.id.profile_textview);
		userEmail = (TextView) findViewById(R.id.profile_user_email);
		userEmail.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		userEmail.getLayoutParams().width = Util.px2dp((235*scaleW), outMetrics);
		userEmail.setSingleLine();
		userEmail.setEllipsize(TruncateAt.END);
		userName = (TextView) findViewById(R.id.profile_user_name);
		userName.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		userName.getLayoutParams().width = Util.px2dp((235*scaleW), outMetrics);
		userName.setSingleLine();
		userName.setEllipsize(TruncateAt.END);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        topControlBar = (TableLayout) findViewById(R.id.top_control_bar);
        topControlBar.setOnClickListener(this);
        bottomControlBar = (TableLayout) findViewById(R.id.bottom_control_bar);
        bottomControlBar.setOnClickListener(this);
        usedSpace = (TextView) findViewById(R.id.used_space);
        usedSpaceText = (TextView) findViewById(R.id.used_space_text);
        usedSpaceWarning = (ImageView) findViewById(R.id.used_space_warning);
        usedSpaceWarning.setVisibility(View.INVISIBLE);
        usedSpaceBar = (ProgressBar) findViewById(R.id.manager_used_space_bar);
        
        usedSpaceBar.setProgress(0);
        
        mTabHostCDrive = (TabHost)findViewById(R.id.tabhost_cloud_drive);
        mTabHostCDrive.setup();
                      
        mTabHostContacts = (TabHost)findViewById(R.id.tabhost_contacts);
        mTabHostContacts.setup();
        
        mTabHostShares = (TabHost)findViewById(R.id.tabhost_shares);
        mTabHostShares.setup();
        
        viewPagerContacts = (ViewPager) findViewById(R.id.contact_tabs_pager);  
        viewPagerShares = (ViewPager) findViewById(R.id.shares_tabs_pager);  
        viewPagerCDrive = (ViewPager) findViewById(R.id.cloud_drive_tabs_pager);          
        
        if (!Util.isOnline(this)){
        	
        	Intent offlineIntent = new Intent(this, OfflineActivity.class);
			startActivity(offlineIntent);
			finish();
        	return;
        }
        
        dbH.setAttrOnline(true);
        this.setPathNavigationOffline(pathNavigation);
        
        MegaNode rootNode = megaApi.getRootNode();
		if (rootNode == null){
			 if (getIntent() != null){
				if (getIntent().getAction() != null){
					if (getIntent().getAction().equals(ManagerActivityLollipop.ACTION_IMPORT_LINK_FETCH_NODES)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ManagerActivityLollipop.ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();	
						return;
					}
					else if (getIntent().getAction().equals(ManagerActivityLollipop.ACTION_OPEN_MEGA_LINK)){
						Intent intent = new Intent(managerActivity, FileLinkActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ManagerActivityLollipop.ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();	
						return;
					}
					else if (getIntent().getAction().equals(ManagerActivityLollipop.ACTION_OPEN_MEGA_FOLDER_LINK)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ManagerActivityLollipop.ACTION_OPEN_MEGA_FOLDER_LINK);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();	
						return;
					}
					else if (getIntent().getAction().equals(ACTION_CANCEL_UPLOAD) || getIntent().getAction().equals(ACTION_CANCEL_DOWNLOAD) || getIntent().getAction().equals(ACTION_CANCEL_CAM_SYNC)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(getIntent().getAction());
						startActivity(intent);
						finish();
						return;
					}
				}
			}
			Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		}
		else{
			log("rootNode != null");		
			megaApi.addGlobalListener(this);
			megaApi.addTransferListener(this);
//			ArrayList<MegaUser> contacts = megaApi.getContacts();
			
//			for (int i=0; i < contacts.size(); i++){
//				if (contacts.get(i).getVisibility() == MegaUser.VISIBILITY_ME){
//					contact = contacts.get(i);
//				}
//			}
			
			contact = megaApi.getContact(megaApi.getMyEmail());
			
			if (contact != null){
				userEmail.setVisibility(View.VISIBLE);
				userEmail.setText(contact.getEmail());
//				String userNameString = contact.getEmail();
//				String [] sp = userNameString.split("@");
//				if (sp.length != 0){
//					userNameString = sp[0];
//					userName.setVisibility(View.VISIBLE);
//					userName.setText(userNameString);
//				}
				megaApi.getUserData(this);
				
				Bitmap defaultAvatar = Bitmap.createBitmap(DEFAULT_AVATAR_WIDTH_HEIGHT,DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(defaultAvatar);
				Paint p = new Paint();
				p.setAntiAlias(true);
				p.setColor(getResources().getColor(R.color.color_default_avatar_mega));
				
				int radius; 
		        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
		        	radius = defaultAvatar.getWidth()/2;
		        else
		        	radius = defaultAvatar.getHeight()/2;
		        
				c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
				imageProfile.setImageBitmap(defaultAvatar);
				
			    int avatarTextSize = getAvatarTextSize(density);
			    log("DENSITY: " + density + ":::: " + avatarTextSize);
			    if (contact.getEmail() != null){
				    if (contact.getEmail().length() > 0){
				    	log("TEXT: " + contact.getEmail());
				    	log("TEXT AT 0: " + contact.getEmail().charAt(0));
				    	String firstLetter = contact.getEmail().charAt(0) + "";
				    	firstLetter = firstLetter.toUpperCase(Locale.getDefault());
				    	textViewProfile.setText(firstLetter);
				    	textViewProfile.setTextSize(32);
				    	textViewProfile.setTextColor(Color.WHITE);
				    	textViewProfile.setVisibility(View.VISIBLE);
				    }
			    }
			    
				File avatar = null;
				if (getExternalCacheDir() != null){
					avatar = new File(getExternalCacheDir().getAbsolutePath(), contact.getEmail() + ".jpg");
				}
				else{
					avatar = new File(getCacheDir().getAbsolutePath(), contact.getEmail() + ".jpg");
				}
				Bitmap imBitmap = null;
				if (avatar.exists()){
					if (avatar.length() > 0){
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);
						int imageHeight = options.outHeight;
						int imageWidth = options.outWidth;
						String imageType = options.outMimeType;
						
						// Calculate inSampleSize
					    options.inSampleSize = calculateInSampleSize(options, 250, 250);
					    
					    // Decode bitmap with inSampleSize set
					    options.inJustDecodeBounds = false;

						imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);
						if (imBitmap == null) {
							avatar.delete();
							if (getExternalCacheDir() != null){
								megaApi.getUserAvatar(contact, getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", this);
							}
							else{
								megaApi.getUserAvatar(contact, getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", this);
							}
						}
						else{
							Bitmap circleBitmap = Bitmap.createBitmap(imBitmap.getWidth(), imBitmap.getHeight(), Bitmap.Config.ARGB_8888);
							
							BitmapShader shader = new BitmapShader (imBitmap,  TileMode.CLAMP, TileMode.CLAMP);
					        Paint paint = new Paint();
					        paint.setShader(shader);
					
					        c = new Canvas(circleBitmap);
					        if (imBitmap.getWidth() < imBitmap.getHeight())
					        	radius = imBitmap.getWidth()/2;
					        else
					        	radius = imBitmap.getHeight()/2;
					        
						    c.drawCircle(imBitmap.getWidth()/2, imBitmap.getHeight()/2, radius, paint);
					        imageProfile.setImageBitmap(circleBitmap);
					        textViewProfile.setVisibility(View.GONE);
						}
					}
					else{
						if (getExternalCacheDir() != null){
							megaApi.getUserAvatar(contact, getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", this);
						}
						else{
							megaApi.getUserAvatar(contact, getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", this);
						}
					}
				}
				else{
					if (getExternalCacheDir() != null){
						megaApi.getUserAvatar(contact, getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", this);
					}
					else{
						megaApi.getUserAvatar(contact, getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", this);
					}
				}
			}
			
			bottomControlBar.setVisibility(View.GONE);
	        
			megaApi.getPaymentMethods(this);
	        megaApi.getAccountDetails(this);
	        megaApi.creditCardQuerySubscriptions(this);
	        
	        List<String> items = new ArrayList<String>();
			for (DrawerItem item : DrawerItem.values()) {
				if (!(item.equals(DrawerItem.SEARCH)||(item.equals(DrawerItem.ACCOUNT)))){					
					items.add(item.getTitle(this));
				}
			}      
			
			nDALol = new NavigationDrawerLollipopAdapter(getApplicationContext(), items);
			mDrawerList.setDividerHeight(0);
			mDrawerList.setDivider(null);
			mDrawerList.setAdapter(nDALol);			
       
	        mDrawerList.setOnItemClickListener(this);
	        
	        getSupportActionBar().setIcon(R.drawable.ic_launcher);
	        getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        
	        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  /* host Activity */
	                mDrawerLayout,         /* DrawerLayout object */
	                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
	                R.string.app_name,  /* "open drawer" description for accessibility */
	                R.string.app_name  /* "close drawer" description for accessibility */
	                ) {
	            public void onDrawerClosed(View view) {
	            	
	            	if (getSupportActionBar() != null){
	            		if (titleAB.compareTo("") != 0){
	            			getSupportActionBar().setTitle(titleAB);
	            		}
	            	}
	            	supportInvalidateOptionsMenu();	// creates call to onPrepareOptionsMenu()
	            }
	
	            public void onDrawerOpened(View drawerView) {
	            	
	            	if (getSupportActionBar() != null){
	            		titleAB = getSupportActionBar().getTitle().toString();
	            		getSupportActionBar().setTitle(getString(R.string.general_menu));
	            	}
	            	supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        };
	        mDrawerToggle.setDrawerIndicatorEnabled(true);
	        mDrawerLayout.setDrawerListener(mDrawerToggle);
	        if (savedInstanceState == null){
	        	mDrawerLayout.openDrawer(Gravity.LEFT);
	        }
	        else{
				mDrawerLayout.closeDrawer(Gravity.LEFT);
	        }
	        
	        mDrawerLayout.setVisibility(View.VISIBLE);
	        
	        //Create the actionBar Menu
	        getSupportActionBar().setDisplayShowCustomEnabled(true);
	        getSupportActionBar().setCustomView(R.layout.custom_action_bar_top);
	        
	        customSearch = (LinearLayout) getSupportActionBar().getCustomView().findViewById(R.id.custom_search);
	        customSearch.setOnClickListener(this);

//			customListGrid = (ImageButton) getSupportActionBar().getCustomView().findViewById(R.id.menu_action_bar_grid);
//			customListGrid.setOnClickListener(this);
			
			parentHandleBrowser = -1;
			parentHandleRubbish = -1;
			parentHandleIncoming = -1;
			parentHandleIncoming = -1;
			parentHandleSearch = -1;
			parentHandleInbox = -1;
			orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;
			if (savedInstanceState != null){
				firstTime = false;
				int visibleFragment = savedInstanceState.getInt("visibleFragment");
				orderGetChildren = savedInstanceState.getInt("orderGetChildren");
				parentHandleBrowser = savedInstanceState.getLong("parentHandleBrowser");
				parentHandleRubbish = savedInstanceState.getLong("parentHandleRubbish");
				parentHandleIncoming = savedInstanceState.getLong("parentHandleIncoming");
				parentHandleOutgoing = savedInstanceState.getLong("parentHandleIncoming");
				parentHandleSearch = savedInstanceState.getLong("parentHandleSearch");
				parentHandleInbox = savedInstanceState.getLong("parentHandleInbox");
				switch (visibleFragment){
					case 1:{
						drawerItem = DrawerItem.CLOUD_DRIVE;
						isListCloudDrive = true;
						break;
					}
					case 2:{
						drawerItem = DrawerItem.INBOX;
						isListCloudDrive = false;
						break;
					}
					case 3:{
						drawerItem = DrawerItem.CONTACTS;
						isListContacts = true;
						break;
					}
					case 4:{
						drawerItem = DrawerItem.CONTACTS;
						isListContacts = false;
						break;
					}
					case 8:{
						drawerItem = DrawerItem.SHARED_WITH_ME;
						isListSharedWithMe = true;
						break;
					}
					case 9:{
						drawerItem = DrawerItem.SHARED_WITH_ME;
						isListSharedWithMe = false;
						break;
					}
					case 10:{
						drawerItem = DrawerItem.ACCOUNT;
						break;
					}
					case 11:{
						drawerItem = DrawerItem.SEARCH;
						searchQuery = savedInstanceState.getString("searchQuery");
						levelsSearch = savedInstanceState.getInt("levels");
						break;
					}
					case 12:{
						drawerItem = DrawerItem.CAMERA_UPLOADS;
						isListCameraUpload = true;
						break;
					}
					case 13:{
						drawerItem = DrawerItem.CAMERA_UPLOADS;
						isListCameraUpload = false;
						break;
					}
					case 14:{
						drawerItem = DrawerItem.INBOX;
						isListInbox = true;
						break;
					}
					case 15:{
						drawerItem = DrawerItem.INBOX;
						isListInbox = false;
						break;
					}
					
				}
			}

			if (drawerItem == null) {
				drawerItem = DrawerItem.CLOUD_DRIVE;
				Intent intent = getIntent();
				if (intent != null){
					boolean upgradeAccount = getIntent().getBooleanExtra("upgradeAccount", false);
					if(upgradeAccount){
						log("upgradeAccount true");
						mDrawerLayout.closeDrawer(Gravity.LEFT);
						int accountType = getIntent().getIntExtra("accountType", 0);
						log("accountType: "+accountType);
						long paymentBitSetLong = getIntent().getLongExtra("paymentBitSetLong", 0);
						BitSet paymentBitSet = Util.convertToBitSet(paymentBitSetLong);;
						switch (accountType){
							case 0:{
								log("intent firstTime==true");
								firstTimeCam = true;
								drawerItem = DrawerItem.CAMERA_UPLOADS;
								setIntent(null);
								return;
							}
							case 1:{	
								drawerItem = DrawerItem.ACCOUNT;
								selectDrawerItemLollipop(drawerItem);
								showpF(1, null, paymentBitSet);
								return;
							}
							case 2:{
								drawerItem = DrawerItem.ACCOUNT;
								selectDrawerItemLollipop(drawerItem);
								showpF(2, null, paymentBitSet);
								return;
							}
							case 3:{
								drawerItem = DrawerItem.ACCOUNT;
								selectDrawerItemLollipop(drawerItem);
								showpF(3, null, paymentBitSet);
								return;
							}	
							case 4:{
								drawerItem = DrawerItem.ACCOUNT;
								selectDrawerItemLollipop(drawerItem);
								showpF(4, null, paymentBitSet);
								return;
							}	
						}							
					}
					else{
						log("upgradeAccount false");
						firstTimeCam = getIntent().getBooleanExtra("firstTimeCam", false);
						if (firstTimeCam){
							log("intent firstTime==true");
							firstTimeCam = true;
							drawerItem = DrawerItem.CAMERA_UPLOADS;
							setIntent(null);
						}
					}					
				}
			}
			else{
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			}
	
			//INITIAL FRAGMENT
			selectDrawerItemLollipop(drawerItem);
		}
	}	
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	log("onSaveInstaceState");
    	if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}
		
		log("retryPendingConnections()");
		if (megaApi != null){
			megaApi.retryPendingConnections();
		}
    	super.onSaveInstanceState(outState);
    	
    	long pHBrowser = -1;
    	long pHRubbish = -1;
    	long pHSharedWithMe = -1;
    	long pHSearch = -1;
    	long pHInbox = -1;
    	int visibleFragment = -1;
    	String pathOffline = this.pathNavigation;
    	
    	int order = this.orderGetChildren;
    	if (drawerItem == DrawerItem.CLOUD_DRIVE){
    		
    		int index = viewPagerCDrive.getCurrentItem();
			log("----------------------------------------INDEX: "+index);
			if(index==1){
				//Rubbish bin
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (rbFLol != null){	
					pHRubbish = rbFLol.getParentHandle();
	    			if (isListRubbishBin){
	    				visibleFragment = 5;
	    			}
	    			else{
	    				visibleFragment = 6;
	    			}
				}
			}
			else{
				//Cloud Drive
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (fbFLol != null)
				{	
	    			pHBrowser = fbFLol.getParentHandle();    		
	    			if (isListCloudDrive){
	    				visibleFragment = 1;
	    			}
	    			else{
	    				visibleFragment = 2;
		    		}
				}
			}    		
    	}
    	
    	String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
    	if (cFLol != null){
    		if (drawerItem == DrawerItem.CONTACTS){
    			if (isListContacts){
    				visibleFragment = 3;
    			}
    			else{
    				visibleFragment = 4;
    			}
    		}
    	}
	
    	if (inSFLol != null){
    		pHSharedWithMe = inSFLol.getParentHandle();
    		if (drawerItem == DrawerItem.SHARED_WITH_ME){
    			if (isListSharedWithMe){
    				visibleFragment = 8;
    			}
    			else{
    				visibleFragment = 9;
    			}
    		}
    	}
    	
    	if (maF != null){
    		if (drawerItem == DrawerItem.ACCOUNT){
    			visibleFragment = 10;
    		}
    	}    	
    	if (sF != null){
    		if (drawerItem == DrawerItem.SEARCH){
    			pHSearch = sF.getParentHandle();
    			visibleFragment = 11;
    			outState.putString("searchQuery", searchQuery);
    			outState.putInt("levels", sF.getLevels());
    		}
    	}    	
    	if (psF != null){
    		if (drawerItem == DrawerItem.CAMERA_UPLOADS){
    			if (isListCameraUpload){
    				visibleFragment = 12;
    			}
    			else{
    				visibleFragment = 13;
    			}
    			
    		}
    	}
    	
    	if (drawerItem == DrawerItem.INBOX)
    	{
    		pHInbox = iFLol.getParentHandle();
    		if (iFLol != null){
    			if (isListInbox){
    				visibleFragment = 14;
    			}
    			else{
    				visibleFragment = 15;
    			}
    		}
    	}
    	
    	outState.putInt("orderGetChildren", order);
    	outState.putInt("visibleFragment", visibleFragment);
    	outState.putLong("parentHandleBrowser", pHBrowser);
    	outState.putLong("parentHandleRubbish", pHRubbish);
    	outState.putLong("parentHandleSharedWithMe", pHSharedWithMe);
    	outState.putLong("parentHandleSearch", pHSearch);
    	outState.putLong("parentHandleInbox", pHInbox);
    }

    @Override
    protected void onDestroy(){
    	log("onDestroy()");

    	super.onDestroy();
    	    	
    	if (megaApi.getRootNode() != null){
    		megaApi.removeGlobalListener(this);
    	
//    		startService(new Intent(getApplicationContext(), CameraSyncService.class));
    		megaApi.removeTransferListener(this);
    		megaApi.removeRequestListener(this);
    	} 
    }
    
    boolean isSearching = false;
    
    @Override
	protected void onNewIntent(Intent intent){
    	log("onNewIntent");

    	if ((intent != null) && Intent.ACTION_SEARCH.equals(intent.getAction())){
    		searchQuery = intent.getStringExtra(SearchManager.QUERY);
    		parentHandleSearch = -1;
    		aB.setTitle(getString(R.string.action_search)+": "+searchQuery);
    		
    		isSearching = true;
    		
    		if (searchMenuItem != null) {
    			MenuItemCompat.collapseActionView(searchMenuItem);
			}
    		return;
    	}
     	super.onNewIntent(intent);
    	setIntent(intent); 
    	return;
	}
    
    @Override
	protected void onPause() {
    	log("onPause");
    	managerActivity = null;
    	super.onPause();
    }
    
    @Override
	protected void onResume() {
    	log("onResume ");
    	super.onResume();
    	managerActivity = this;
    	
    	Intent intent = getIntent(); 
    	
//    	dbH = new DatabaseHandler(getApplicationContext());
    	dbH = DatabaseHandler.getDbHandler(getApplicationContext());
    	if(dbH.getCredentials() == null){	
    		if (!openLink){
    			logout(this, megaApi, false);
    			return;
    		}			
		}
    	   	
    	if (intent != null) {  
    		log("intent not null! "+intent.getAction());
    		// Open folder from the intent
			if (intent.hasExtra(EXTRA_OPEN_FOLDER)) {
				log("INTENT: EXTRA_OPEN_FOLDER");
				parentHandleBrowser = intent.getLongExtra(EXTRA_OPEN_FOLDER, -1);
				intent.removeExtra(EXTRA_OPEN_FOLDER);
				setIntent(null);
			}
    					
    		if (intent.getAction() != null){ 
    			log("intent action");
    			
    			if(getIntent().getAction().equals(ManagerActivityLollipop.ACTION_EXPLORE_ZIP)){  

    				String pathZip=intent.getExtras().getString(EXTRA_PATH_ZIP);    				
    				
    				Intent intentZip = new Intent(managerActivity, ZipBrowserActivity.class);    				
    				intentZip.putExtra(ZipBrowserActivity.EXTRA_PATH_ZIP, pathZip);
    			    startActivity(intentZip);   				
    				
    			}
//    			else if(getIntent().getAction().equals(ManagerActivityLollipop.ACTION_OPEN_PDF)){    				
//
//    				String pathPdf=intent.getExtras().getString(EXTRA_PATH_PDF);
//    			    
//    			    File pdfFile = new File(pathPdf);
//    			    
//    			    Intent intentPdf = new Intent();
//    			    intentPdf.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
//    			    intentPdf.setClass(this, OpenPDFActivity.class);
//    			    intentPdf.setAction("android.intent.action.VIEW");
//    				this.startActivity(intentPdf);	
//    				
//    			}    			
    			else if (getIntent().getAction().equals(ManagerActivityLollipop.ACTION_IMPORT_LINK_FETCH_NODES)){
					Intent loginIntent = new Intent(managerActivity, LoginActivityLollipop.class);
					loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					loginIntent.setAction(ManagerActivityLollipop.ACTION_IMPORT_LINK_FETCH_NODES);
					loginIntent.setData(Uri.parse(getIntent().getDataString()));
					startActivity(loginIntent);
					finish();	
					return;
				}
				else if (getIntent().getAction().equals(ManagerActivityLollipop.ACTION_OPEN_MEGA_LINK)){
					Intent fileLinkIntent = new Intent(managerActivity, FileLinkActivity.class);
					fileLinkIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					fileLinkIntent.setAction(ManagerActivityLollipop.ACTION_IMPORT_LINK_FETCH_NODES);
					fileLinkIntent.setData(Uri.parse(getIntent().getDataString()));
					startActivity(fileLinkIntent);
					finish();	
					return;
				}
    			else if (intent.getAction().equals(ACTION_OPEN_MEGA_FOLDER_LINK)){
    				Intent intentFolderLink = new Intent(managerActivity, FolderLinkActivity.class);
    				intentFolderLink.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				intentFolderLink.setAction(ManagerActivityLollipop.ACTION_OPEN_MEGA_FOLDER_LINK);
    				intentFolderLink.setData(Uri.parse(getIntent().getDataString()));
					startActivity(intentFolderLink);
					finish();
    			}
    			else if (intent.getAction().equals(ACTION_REFRESH_PARENTHANDLE_BROWSER)){
    				
    				parentHandleBrowser = intent.getLongExtra("parentHandle", -1);    				
    				intent.removeExtra("parentHandle");
    				setParentHandleBrowser(parentHandleBrowser);
    				
    				if (fbFLol != null){
						fbFLol.setParentHandle(parentHandleBrowser);
    					fbFLol.setIsList(isListCloudDrive);
    					fbFLol.setOrder(orderGetChildren);
    					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
    					fbFLol.setNodes(nodes);
    					if (!fbFLol.isVisible()){
    						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbFLol, "fbFLol").commit();
    					}
    				}	
    				else{
    					fbFLol = new FileBrowserFragmentLollipop();
    					fbFLol.setParentHandle(parentHandleBrowser);
    					fbFLol.setIsList(isListCloudDrive);
    					fbFLol.setOrder(orderGetChildren);
    					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
    					fbFLol.setNodes(nodes);
    					if (!fbFLol.isVisible()){
    						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbFLol, "fbFLol").commit();
    					}
    				}
    			}
    			else if(intent.getAction().equals(ACTION_OVERQUOTA_ALERT)){
	    			showOverquotaAlert();
	    		}
    			else if (intent.getAction().equals(ACTION_CANCEL_UPLOAD) || intent.getAction().equals(ACTION_CANCEL_DOWNLOAD) || intent.getAction().equals(ACTION_CANCEL_CAM_SYNC)){
    				log("ACTION_CANCEL_UPLOAD or ACTION_CANCEL_DOWNLOAD or ACTION_CANCEL_CAM_SYNC");
					Intent tempIntent = null;
					String title = null;
					String text = null;
					if(intent.getAction().equals(ACTION_CANCEL_UPLOAD)){
						tempIntent = new Intent(this, UploadService.class);
						tempIntent.setAction(UploadService.ACTION_CANCEL);
						title = getString(R.string.upload_uploading);
						text = getString(R.string.upload_cancel_uploading);
					} 
					else if (intent.getAction().equals(ACTION_CANCEL_DOWNLOAD)){
						tempIntent = new Intent(this, DownloadService.class);
						tempIntent.setAction(DownloadService.ACTION_CANCEL);
						title = getString(R.string.download_downloading);
						text = getString(R.string.download_cancel_downloading);
					}
					else if (intent.getAction().equals(ACTION_CANCEL_CAM_SYNC)){
						tempIntent = new Intent(this, CameraSyncService.class);
						tempIntent.setAction(CameraSyncService.ACTION_CANCEL);
						title = getString(R.string.cam_sync_syncing);
						text = getString(R.string.cam_sync_cancel_sync);
					}
					
					final Intent cancelIntent = tempIntent;
					AlertDialog.Builder builder = Util.getCustomAlertBuilder(this,
							title, text, null);
					builder.setPositiveButton(getString(R.string.general_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									if (tF != null){
										if (tF.isVisible()){
											tF.setNoActiveTransfers();
											downloadPlay = true;
										}
									}	
									startService(cancelIntent);						
								}
							});
					builder.setNegativeButton(getString(R.string.general_no), null);
					final AlertDialog dialog = builder.create();
					try {
						dialog.show(); 
					}
					catch(Exception ex)	{ 
						startService(cancelIntent); 
					}
				}    			
    			else if (intent.getAction().equals(ACTION_TAKE_SELFIE)){
    				log("Intent take selfie");
    				takePicture();
    			}
    			intent.setAction(null);
				setIntent(null);
    		}
    	}
    	
    }
    
	/*
	 * Show Import Dialog
	 */
	private void importLink(String url) {
		
		try {
			url = URLDecoder.decode(url, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) {}
		url.replace(' ', '+');
		if(url.startsWith("mega://")){
			url = url.replace("mega://", "https://mega.co.nz/");
		}
		
		log("url " + url);
		
		// Download link
		if (url != null && (url.matches("^https://mega.co.nz/#!.*!.*$") || url.matches("^https://mega.nz/#!.*!.*$"))) {
			log("open link url");
			
//			Intent openIntent = new Intent(this, ManagerActivityLollipop.class);
			Intent openFileIntent = new Intent(this, FileLinkActivity.class);
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			openFileIntent.setAction(ManagerActivityLollipop.ACTION_OPEN_MEGA_LINK);
			openFileIntent.setData(Uri.parse(url));
			startActivity(openFileIntent);
//			finish();
			return;
		}
		
		// Folder Download link
		else if (url != null && (url.matches("^https://mega.co.nz/#F!.+$") || url.matches("^https://mega.nz/#F!.+$"))) {
			log("folder link url");
			Intent openFolderIntent = new Intent(this, FolderLinkActivity.class);
			openFolderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			openFolderIntent.setAction(ManagerActivityLollipop.ACTION_OPEN_MEGA_FOLDER_LINK);
			openFolderIntent.setData(Uri.parse(url));
			startActivity(openFolderIntent);
//			finish();
			return;
		}
		else{
			log("wrong url");
			Intent errorIntent = new Intent(this, ManagerActivityLollipop.class);
			errorIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(errorIntent);
		}
	}
	
	
	/*
	 * Check MEGA url and parse if valid
	 */
	private String[] parseDownloadUrl(String url) {
		log("parseDownloadUrl");
		if (url == null) {
			return null;
		}
		if (!url.matches("^https://mega.co.nz/#!.*!.*$")) {
			return null;
		}
		String[] parts = url.split("!");
		if(parts.length != 3) return null;
		return new String[] { parts[1], parts[2] };
	}
	
	public void cameraUploadsClicked(){
		log("cameraUplaodsClicked");
		drawerItem = DrawerItem.CAMERA_UPLOADS;		
		selectDrawerItemLollipop(drawerItem);		
	}
    
	private View getTabIndicator(Context context, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);

        TextView tv = (TextView) view.findViewById(R.id.textView);
        tv.setText(title);
        return view;
    }
	
	public void setInitialCloudDrive (){
		drawerItem = DrawerItem.CLOUD_DRIVE;		

		nDALol.setPositionClicked(0);
		selectDrawerItemLollipop(drawerItem);

		mDrawerLayout.openDrawer(Gravity.LEFT);
		firstTime = true;

//		if (fbFLol == null){
//			fbFLol = new FileBrowserFragment();
//			if (parentHandleBrowser == -1){
//				fbFLol.setParentHandle(megaApi.getRootNode().getHandle());
//				parentHandleBrowser = megaApi.getRootNode().getHandle();
//			}
//			else{
//				fbFLol.setParentHandle(parentHandleBrowser);
//			}
//			fbFLol.setIsList(isListCloudDrive);
//			fbFLol.setOrder(orderGetChildren);
//			ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
//			fbFLol.setNodes(nodes);
//		}
//		else{
//								
//			fbFLol.setIsList(isListCloudDrive);
//			fbFLol.setParentHandle(parentHandleBrowser);
//			fbFLol.setOrder(orderGetChildren);
//			ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
//			fbFLol.setNodes(nodes);
//		}
//		
//		mTabHostContacts.setVisibility(View.GONE);    			
//		viewPagerContacts.setVisibility(View.GONE); 
//		mTabHostShares.setVisibility(View.GONE);    			
//		viewPagerShares.setVisibility(View.GONE);
//		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//		ft.replace(R.id.fragment_container, fbFLol, "fbFLol");
//		ft.commit();
//		
//		mDrawerLayout.openDrawer(Gravity.LEFT);
//		firstTime = false;
//		
//		customSearch.setVisibility(View.VISIBLE);
//		viewPagerShares.setVisibility(View.GONE);
//		viewPagerContacts.setVisibility(View.GONE);
//
//		if (createFolderMenuItem != null){
//			createFolderMenuItem.setVisible(true);
//			addContactMenuItem.setVisible(false);
//			addMenuItem.setVisible(true);
//			refreshMenuItem.setVisible(true);
//			sortByMenuItem.setVisible(true);
//			helpMenuItem.setVisible(true);
//			upgradeAccountMenuItem.setVisible(false);
//			settingsMenuItem.setVisible(true);
//			selectMenuItem.setVisible(true);
//			unSelectMenuItem.setVisible(false);
//			thumbViewMenuItem.setVisible(true);
//			addMenuItem.setEnabled(true);	  
// 			
//			if (isListCloudDrive){	
//				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
//			}
//			else{
//				thumbViewMenuItem.setTitle(getString(R.string.action_list));
//			}
//			rubbishBinMenuItem.setVisible(true);
//			rubbishBinMenuItem.setTitle(getString(R.string.section_rubbish_bin));
//			clearRubbishBinMenuitem.setVisible(false);
//		}
	}
	
	private Fragment recreateFragment(Fragment f)
    {
        try {
            Fragment.SavedState savedState = getSupportFragmentManager().saveFragmentInstanceState(f);

            Fragment newInstance = f.getClass().newInstance();
            newInstance.setInitialSavedState(savedState);

            return newInstance;
        }
        catch (Exception e) // InstantiationException, IllegalAccessException
        {
            throw new RuntimeException("Cannot reinstantiate fragment " + f.getClass().getName(), e);
        }
    }
	
	public void refreshCameraUpload(){
		drawerItem = DrawerItem.CAMERA_UPLOADS;
		nDALol.setPositionClicked(POS_CAMERA_UPLOADS);
		
		Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("psF");
		FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
		fragTransaction.detach(currentFragment);
		fragTransaction.commit();

		fragTransaction = getSupportFragmentManager().beginTransaction();
		fragTransaction.attach(currentFragment);
		fragTransaction.commit();
	}
	
    public void selectDrawerItemLollipop(DrawerItem item){
    	log("selectDrawerItemLollipop");
    	switch (item){
    		case CLOUD_DRIVE:{
//    			
//    			megaApi.getPricing(this);
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
//    			if (fbFLol == null){
//    				fbFLol = new FileBrowserFragmentLollipop();
//					if (parentHandleBrowser == -1){
//						fbFLol.setParentHandle(megaApi.getRootNode().getHandle());
//						parentHandleBrowser = megaApi.getRootNode().getHandle();
//					}
//					else{
//						fbFLol.setParentHandle(parentHandleBrowser);
//					}
//					fbFLol.setIsList(isListCloudDrive);
//					fbFLol.setOrder(orderGetChildren);
//					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
//					fbFLol.setNodes(nodes);
//				}
//				else{
//										
//					fbFLol.setIsList(isListCloudDrive);
//					fbFLol.setParentHandle(parentHandleBrowser);
//					fbFLol.setOrder(orderGetChildren);
//					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
//					fbFLol.setNodes(nodes);
//				}
								
				mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
//				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//				ft.replace(R.id.fragment_container, fbFLol, "fbFLol");
//    			ft.commit();
    			
    			//////
    			Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    			if (currentFragment != null){
    				getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
    			}
    			
//    			mTabHostCDrive.getTabWidget().setBackgroundColor(Color.RED);
    			
    			mTabHostCDrive.setVisibility(View.VISIBLE);    	
    			
    			if (mTabsAdapterCDrive == null){
    				log("mTabsAdapterCloudDrive == null");
    				
    				mTabsAdapterCDrive= new TabsAdapter(this, mTabHostCDrive, viewPagerCDrive);   	
    				
        			TabHost.TabSpec tabSpec5 = mTabHostCDrive.newTabSpec("fbFLol");
        			String titleTab5 = getString(R.string.section_cloud_drive);
        			tabSpec5.setIndicator(getTabIndicator(mTabHostCDrive.getContext(), titleTab5.toUpperCase(Locale.getDefault()))); // new function to inject our own tab layout
        	        TabHost.TabSpec tabSpec6 = mTabHostCDrive.newTabSpec("rBFLol");
        	        String titleTab6 = getString(R.string.section_rubbish_bin);
        	        tabSpec6.setIndicator(getTabIndicator(mTabHostCDrive.getContext(), titleTab6.toUpperCase(Locale.getDefault()))); // new function to inject our own tab layout   	                      	   
        	        
        	        mTabsAdapterCDrive.addTab(tabSpec5, FileBrowserFragmentLollipop.class, null);
        	        mTabsAdapterCDrive.addTab(tabSpec6, RubbishBinFragmentLollipop.class, null);
        	        
        	        viewPagerCDrive.setCurrentItem(0);
        	        aB.setTitle(getResources().getString(R.string.section_cloud_drive));
        	        
        			textViewBrowser = (TextView) mTabHostCDrive.getTabWidget().getChildAt(0).findViewById(R.id.textView); 
        			textViewRubbish = (TextView) mTabHostCDrive.getTabWidget().getChildAt(1).findViewById(R.id.textView); 
        			textViewBrowser.setTypeface(null, Typeface.BOLD);
    				textViewRubbish.setTypeface(null, Typeface.NORMAL); 
    				
    			}
    			else{
    				log("mTabsAdapterCloudDrive NOT null");
        			mTabHostCDrive.setVisibility(View.VISIBLE);    			
        			viewPagerCDrive.setVisibility(View.VISIBLE);
    				
    				fbFLol.setIsList(isListCloudDrive);
    				fbFLol.setParentHandle(parentHandleBrowser);
    				fbFLol.setOrder(orderGetChildren);
					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
					fbFLol.setNodes(nodes);
    			}   			
    			
    			mTabHostCDrive.setOnTabChangedListener(new OnTabChangeListener(){
                    @Override
                    public void onTabChanged(String tabId) {
                    	log("TabId :"+ tabId);
                    	supportInvalidateOptionsMenu();
                        if(tabId.equals("fbFLol")){                         	
                        	String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
            				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
                			if (fbFLol != null){ 
                				textViewBrowser.setTypeface(null, Typeface.BOLD);
                				textViewRubbish.setTypeface(null, Typeface.NORMAL);
                				log("parentHandleCloud: "+ parentHandleBrowser);
                				if(parentHandleBrowser==megaApi.getRootNode().getHandle()||parentHandleBrowser==-1){
                					aB.setTitle(getResources().getString(R.string.section_cloud_drive));
                				}
                				else {
	                				MegaNode node = megaApi.getNodeByHandle(parentHandleBrowser);
	            					aB.setTitle(node.getName());
            					}          					   				
                			}
                        }
                        else if(tabId.equals("rBFLol")){                        	
                        	String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
                        	rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);                			
                        	if (rbFLol != null){  
                        		textViewBrowser.setTypeface(null, Typeface.NORMAL);
                				textViewRubbish.setTypeface(null, Typeface.BOLD);
                        		log("parentHandleRubbish: "+ parentHandleRubbish);
                        		if(parentHandleRubbish==megaApi.getRubbishNode().getHandle()||parentHandleRubbish==-1){
                        			aB.setTitle(getResources().getString(R.string.section_rubbish_bin));
                        		}
                        		else{                        			
                        			MegaNode node = megaApi.getNodeByHandle(parentHandleRubbish);
                					aB.setTitle(node.getName());	
            					}		
                			}                           	
                                          	
                        }
                     }
    			});
    			
				for (int i=0;i<mTabsAdapterCDrive.getCount();i++){
					final int index = i;
					mTabHostCDrive.getTabWidget().getChildAt(i).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							viewPagerCDrive.setCurrentItem(index);	
						}
					});
				}
				/////		
    			
     			
    			if (!firstTime){
    				mDrawerLayout.closeDrawer(Gravity.LEFT);
    			}
    			else{
    				firstTime = false;
    			}
    			
    			customSearch.setVisibility(View.VISIBLE);
    			viewPagerContacts.setVisibility(View.GONE);
    			
    			//OncreateOptionsMenu
    			int index = viewPagerCDrive.getCurrentItem();
    			log("----------------------------------------INDEX: "+index);
    			if(index==1){
    				if (rbFLol != null){	
    					if (createFolderMenuItem != null){
	    					//Show				
	    	    			sortByMenuItem.setVisible(true);
	    	    			selectMenuItem.setVisible(true);
	    	    			thumbViewMenuItem.setVisible(true);
	    	    			clearRubbishBinMenuitem.setVisible(true);
	    	    			
	    					//Hide
	    	    			refreshMenuItem.setVisible(false);
	    					pauseRestartTransfersItem.setVisible(false);
	    					createFolderMenuItem.setVisible(false);
	    	    			addMenuItem.setVisible(false);
	    	    			addContactMenuItem.setVisible(false);
	    	    			upgradeAccountMenuItem.setVisible(false);
	    	    			unSelectMenuItem.setVisible(false);
	    	    			addMenuItem.setEnabled(false);
	    	    			changePass.setVisible(false); 
	    	    			exportMK.setVisible(false); 
	    	    			removeMK.setVisible(false); 
	    	    			importLinkMenuItem.setVisible(false);
	    	    			takePicture.setVisible(false);
	    	    			refreshMenuItem.setVisible(false);
	    					helpMenuItem.setVisible(false);
	    					settingsMenuItem.setVisible(false);
	    	    			
	    	    			if (isListRubbishBin){	
	    	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
	    					}
	    					else{
	    						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    	    			}
	
	    					rbFLol.setIsList(isListRubbishBin);	        			
	    					rbFLol.setParentHandle(parentHandleRubbish);
	    					
	    					if(rbFLol.getItemCount()>0){
	    						selectMenuItem.setVisible(true);
	    						clearRubbishBinMenuitem.setVisible(true);
	    					}
	    					else{
	    						selectMenuItem.setVisible(false);
	    						clearRubbishBinMenuitem.setVisible(false);
	    					}        			
	    	   			
	    	    			rubbishBinMenuItem.setVisible(false);
	    	    			rubbishBinMenuItem.setTitle(getString(R.string.section_cloud_drive));    			
	    				}
    				}
    			}			
    			else{
    				if (fbFLol!=null){
    					if (createFolderMenuItem != null){
    					//Cloud Drive
    					//Show
	    					addMenuItem.setEnabled(true);
	    					addMenuItem.setVisible(true);
	    					createFolderMenuItem.setVisible(true);				
	    					sortByMenuItem.setVisible(true);
	    					thumbViewMenuItem.setVisible(true);
	    					rubbishBinMenuItem.setVisible(false);				
	    	    			upgradeAccountMenuItem.setVisible(false);    			
	    	    			importLinkMenuItem.setVisible(true);
	    	    			takePicture.setVisible(true);
	    	    			selectMenuItem.setVisible(true);
	    	    			
	    					//Hide
	    	    			pauseRestartTransfersItem.setVisible(false);
	    	    			addContactMenuItem.setVisible(false);    			
	    	    			unSelectMenuItem.setVisible(false); 
	    	    			clearRubbishBinMenuitem.setVisible(false); 
	    	    			changePass.setVisible(false); 
	    	    			exportMK.setVisible(false); 
	    	    			removeMK.setVisible(false); 
	    	    			refreshMenuItem.setVisible(false);
	    					helpMenuItem.setVisible(false);
	    					settingsMenuItem.setVisible(false);
	    					killAllSessions.setVisible(false);					
	
	    					if(fbFLol.getItemCount()>0){
	    						selectMenuItem.setVisible(true);
	    					}
	    					else{
	    						selectMenuItem.setVisible(true);
	    					}
	    	    			
	    	    			if (isListCloudDrive){	
	    	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
	    					}
	    					else{
	    						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    	    			}   	    			
	
	    				}
    				}
    			}    			    			
    			
    			//////    			
    			
    			break;
    		}
    		case INBOX:{
   			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (iFLol == null){
    				iFLol = new InboxFragmentLollipop();
    				iFLol.setParentHandle(megaApi.getInboxNode().getHandle());
    				parentHandleInbox = megaApi.getInboxNode().getHandle();
    				iFLol.setIsList(isListInbox);
    				iFLol.setOrder(orderGetChildren);
    				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
    				iFLol.setNodes(nodes);
    			}
    			else{
    				iFLol.setIsList(isListInbox);
    				iFLol.setParentHandle(parentHandleInbox);
    				iFLol.setOrder(orderGetChildren);
    				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleInbox), orderGetChildren);
    				iFLol.setNodes(nodes);
    			}
    			    
    			mTabHostCDrive.setVisibility(View.GONE);    			
    			viewPagerCDrive.setVisibility(View.GONE);
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			viewPagerShares.setVisibility(View.GONE);
    			
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, iFLol, "iFLol");
    			ft.commit();
    			
    			customSearch.setVisibility(View.VISIBLE);
    			viewPagerContacts.setVisibility(View.GONE);
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			if (createFolderMenuItem != null){
    				//Show				
        			sortByMenuItem.setVisible(true);
        			if(iFLol.getItemCount()>0){
						selectMenuItem.setVisible(true);
					}
					else{
						selectMenuItem.setVisible(true);
					}
        			
    				//Hide
        			refreshMenuItem.setVisible(false);
        			thumbViewMenuItem.setVisible(false);
    				pauseRestartTransfersItem.setVisible(false);
    				createFolderMenuItem.setVisible(false);
        			addMenuItem.setVisible(false);
        			addContactMenuItem.setVisible(false);        			
        			unSelectMenuItem.setVisible(false);
        			addMenuItem.setEnabled(false);
        			changePass.setVisible(false); 
        			exportMK.setVisible(false); 
        			removeMK.setVisible(false); 
        			importLinkMenuItem.setVisible(false);
        			takePicture.setVisible(false);
        			refreshMenuItem.setVisible(false);
    				helpMenuItem.setVisible(false);
    				settingsMenuItem.setVisible(false);
        			thumbViewMenuItem.setVisible(false);
        			clearRubbishBinMenuitem.setVisible(false);
        			rubbishBinMenuItem.setVisible(false);
        			upgradeAccountMenuItem.setVisible(false);
	    		}

    			break;
    		}
    		case CONTACTS:{
  			
//    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (aB == null){
    				aB = getSupportActionBar();
    			}
    			aB.setTitle(getString(R.string.section_contacts));
    			
    			if (getmDrawerToggle() != null){
    				getmDrawerToggle().setDrawerIndicatorEnabled(true);
    				supportInvalidateOptionsMenu();
    			}
    			
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
    			mTabHostCDrive.setVisibility(View.GONE);    			
    			viewPagerCDrive.setVisibility(View.GONE);
    			
    			Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    			if (currentFragment != null){
    				getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
    			}
    			mTabHostContacts.setVisibility(View.VISIBLE);    			
    			viewPagerContacts.setVisibility(View.VISIBLE);
    			
    			mTabHostContacts.getTabWidget().setBackgroundColor(Color.BLACK);
    			//mTabHostContacts.getTabWidget().setTextAlignment(textAlignment)
    			
//    		    TextView title1 = (TextView) mIndicator.findViewById(android.R.id.title);    		    
//    		    title1.setText(R.string.tab_contacts); 			
    			
    			if (mTabsAdapterContacts == null){
    				mTabsAdapterContacts = new TabsAdapter(this, mTabHostContacts, viewPagerContacts);   	
    				
        			TabHost.TabSpec tabSpec1 = mTabHostContacts.newTabSpec("contactsFragment");
        	        tabSpec1.setIndicator(getTabIndicator(mTabHostContacts.getContext(), getString(R.string.tab_contacts))); // new function to inject our own tab layout
        	        //tabSpec.setContent(contentID);
        	        //mTabHostContacts.addTab(tabSpec);
        	        TabHost.TabSpec tabSpec2 = mTabHostContacts.newTabSpec("sentRequests");
        	        tabSpec2.setIndicator(getTabIndicator(mTabHostContacts.getContext(), getString(R.string.tab_sent_requests))); // new function to inject our own tab layout
//        	        
//        	        TabHost.TabSpec tabSpec3 = mTabHostContacts.newTabSpec("receivedRequests");
//        	        tabSpec3.setIndicator(getTabIndicator(mTabHostContacts.getContext(), getString(R.string.tab_received_requests))); // new function to inject our own tab layout
    				
    				mTabsAdapterContacts.addTab(tabSpec1, ContactsFragmentLollipop.class, null);
    				mTabsAdapterContacts.addTab(tabSpec2, SentRequestsFragmentLollipop.class, null);
//    				mTabsAdapterContacts.addTab(tabSpec3, ReceivedRequestsFragment.class, null);
    			}		
    			
    			mTabHostContacts.setOnTabChangedListener(new OnTabChangeListener(){
                    @Override
                    public void onTabChanged(String tabId) {
                    	managerActivity.supportInvalidateOptionsMenu();
                    }
    			});
    			
    			for (int i=0;i<mTabsAdapterContacts.getCount();i++){
    				final int index = i;
    				mTabHostContacts.getTabWidget().getChildAt(i).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							viewPagerContacts.setCurrentItem(index);
						}
					});
    			}
    			
    			customSearch.setVisibility(View.VISIBLE);     			
			    			
    			mDrawerLayout.closeDrawer(Gravity.LEFT);

    			if (createFolderMenuItem != null){
    				changePass.setVisible(false); 
        			exportMK.setVisible(false); 
        			removeMK.setVisible(false); 
    				createFolderMenuItem.setVisible(false);
    				addContactMenuItem.setVisible(true);
	    			addMenuItem.setVisible(false);
	    			refreshMenuItem.setVisible(false);
	    			sortByMenuItem.setVisible(true);
	    			helpMenuItem.setVisible(false);
	    			upgradeAccountMenuItem.setVisible(false);
	    			settingsMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(true);
	    			unSelectMenuItem.setVisible(false);
	    			thumbViewMenuItem.setVisible(true);
	    			addMenuItem.setEnabled(false);	
	    			rubbishBinMenuItem.setVisible(false);
	    			clearRubbishBinMenuitem.setVisible(false);
	    			
	    			if (isListContacts){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}	    			
    			}
    			break;
    		}    		
    		case SHARED_WITH_ME:{    			
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (aB == null){
    				aB = getSupportActionBar();
    			}
    			aB.setTitle(getString(R.string.section_shared_items));
    			
    			if (getmDrawerToggle() != null){
    				getmDrawerToggle().setDrawerIndicatorEnabled(true);
    				supportInvalidateOptionsMenu();
    			}
    			
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostCDrive.setVisibility(View.GONE);    			
    			viewPagerCDrive.setVisibility(View.GONE);
    			
    			Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    			if (currentFragment != null){
    				getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
    			}
    			
//    			mTabHostShares.getTabWidget().setBackgroundColor(Color.BLACK);
    			
    			if (mTabsAdapterShares == null){
    				mTabsAdapterShares= new TabsAdapter(this, mTabHostShares, viewPagerShares);   	
    				
        			TabHost.TabSpec tabSpec3 = mTabHostShares.newTabSpec("incomingSharesFragment");
        			String titleTab3 = getString(R.string.tab_incoming_shares);
        			tabSpec3.setIndicator(getTabIndicator(mTabHostShares.getContext(), titleTab3.toUpperCase(Locale.getDefault()))); // new function to inject our own tab layout  			
         			
        	        TabHost.TabSpec tabSpec4 = mTabHostShares.newTabSpec("outgoingSharesFragment");
        	        String titleTab4 = getString(R.string.tab_outgoing_shares);
        	        tabSpec4.setIndicator(getTabIndicator(mTabHostShares.getContext(), titleTab4.toUpperCase(Locale.getDefault()))); // new function to inject our own tab layout
        	                	          				
    				mTabsAdapterShares.addTab(tabSpec3, IncomingSharesFragmentLollipop.class, null);
    				mTabsAdapterShares.addTab(tabSpec4, OutgoingSharesFragmentLollipop.class, null); 
    				
        	        viewPagerCDrive.setCurrentItem(0);
        			textViewIncoming = (TextView) mTabHostShares.getTabWidget().getChildAt(0).findViewById(R.id.textView); 
        			textViewOutgoing = (TextView) mTabHostShares.getTabWidget().getChildAt(1).findViewById(R.id.textView); 
        			textViewIncoming.setTypeface(null, Typeface.BOLD);
        			textViewOutgoing.setTypeface(null, Typeface.NORMAL);
    			}
    			else{
    				log("mTabsAdapterShares NOT null");
        			mTabHostCDrive.setVisibility(View.VISIBLE);    			
        			viewPagerCDrive.setVisibility(View.VISIBLE);
    				
        			inSFLol.setIsList(isListSharedWithMe);
        			inSFLol.setParentHandle(parentHandleIncoming);
        			inSFLol.setOrder(orderGetChildren);
        			inSFLol.refresh(parentHandleIncoming);
    			}
    			
    			mTabHostShares.setVisibility(View.VISIBLE);    			
    			mTabHostShares.setVisibility(View.VISIBLE);
    			
    			mTabHostShares.setOnTabChangedListener(new OnTabChangeListener(){
                    @Override
                    public void onTabChanged(String tabId) {
                    	log("TabId :"+ tabId);
                    	supportInvalidateOptionsMenu();
                        if(tabId.equals("outgoingSharesFragment")){                         	
                			if (outSFLol != null){    
                				textViewOutgoing.setTypeface(null, Typeface.BOLD);
                				textViewIncoming.setTypeface(null, Typeface.NORMAL);
                				if(parentHandleOutgoing!=-1){
	                				MegaNode node = megaApi.getNodeByHandle(parentHandleOutgoing);
	            					aB.setTitle(node.getName());
            					}
                				else{
                					aB.setTitle(getResources().getString(R.string.section_shared_items));
                					outSFLol.refresh(); 
                				}            					   				
                			}
                        }
                        else if(tabId.equals("incomingSharesFragment")){                        	
                        	if (inSFLol != null){    
                        		textViewOutgoing.setTypeface(null, Typeface.NORMAL);
                				textViewIncoming.setTypeface(null, Typeface.BOLD);
                        		if(parentHandleIncoming!=-1){
                        			
                        			MegaNode node = megaApi.getNodeByHandle(parentHandleIncoming);
                					aB.setTitle(node.getName());	
            					}
                				else{
                					
                					aB.setTitle(getResources().getString(R.string.section_shared_items));
                					inSFLol.refresh(); 
                				}   				
                			}                           	
                        }
                     }
    			});
    			
				for (int i=0;i<mTabsAdapterShares.getCount();i++){
					final int index = i;
					mTabHostShares.getTabWidget().getChildAt(i).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							viewPagerShares.setCurrentItem(index);	
						}
					});
				}
   			
    			customSearch.setVisibility(View.VISIBLE);
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			//onCreateOptionsMenu
    			int index = viewPagerShares.getCurrentItem();
    			if(index==0){	
    				String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
    				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
    				if (inSFLol != null){
    					sortByMenuItem.setVisible(true);
    					thumbViewMenuItem.setVisible(true); 

    					addMenuItem.setEnabled(true);
    					addMenuItem.setVisible(true);

    					log("parentHandleIncoming: "+parentHandleIncoming);
    					if(parentHandleIncoming==-1){
    						addMenuItem.setVisible(false);
    					}
    					else{
    						addMenuItem.setVisible(true);
    					}
    					
    					if(inSFLol.getItemCount()>0){
    						selectMenuItem.setVisible(true);
    					}
    					else{
    						selectMenuItem.setVisible(false);
    					}

    					//Hide
    					pauseRestartTransfersItem.setVisible(false);
    					createFolderMenuItem.setVisible(false);
    					addContactMenuItem.setVisible(false);
    					unSelectMenuItem.setVisible(false);  				
    					rubbishBinMenuItem.setVisible(false);
    					createFolderMenuItem.setVisible(false);
    					rubbishBinMenuItem.setVisible(false);
    					clearRubbishBinMenuitem.setVisible(false);
    					changePass.setVisible(false); 
    					exportMK.setVisible(false); 
    					removeMK.setVisible(false); 
    					importLinkMenuItem.setVisible(false);
    					takePicture.setVisible(false);					
    	    			refreshMenuItem.setVisible(false);
    					helpMenuItem.setVisible(false);
    					settingsMenuItem.setVisible(false);
    					upgradeAccountMenuItem.setVisible(false);
    				}
    			}
    			else if(index==1){
    				String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
    				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
    				if (outSFLol != null){

    					sortByMenuItem.setVisible(true);
    					thumbViewMenuItem.setVisible(true); 

    					log("parentHandleOutgoing: "+parentHandleOutgoing);
    					if(parentHandleOutgoing==-1){
    						addMenuItem.setVisible(false);
    					}
    					else{
    						addMenuItem.setVisible(true);
    					}
    					
    					if(outSFLol.getItemCount()>0){
    						selectMenuItem.setVisible(true);
    					}
    					else{
    						selectMenuItem.setVisible(false);
    					}

    					//Hide
    					upgradeAccountMenuItem.setVisible(false);
    					pauseRestartTransfersItem.setVisible(false);
    					createFolderMenuItem.setVisible(false);
    					addContactMenuItem.setVisible(false);
    					unSelectMenuItem.setVisible(false);  				
    					rubbishBinMenuItem.setVisible(false);
    					createFolderMenuItem.setVisible(false);
    					rubbishBinMenuItem.setVisible(false);
    					clearRubbishBinMenuitem.setVisible(false);
    					changePass.setVisible(false); 
    					exportMK.setVisible(false); 
    					removeMK.setVisible(false); 
    					importLinkMenuItem.setVisible(false);
    					takePicture.setVisible(false);					
    	    			refreshMenuItem.setVisible(false);
    					helpMenuItem.setVisible(false);
    					settingsMenuItem.setVisible(false);
    				}
    			}   			
    			String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);    			
    			if (inSFLol != null){
    				aB.setTitle(getString(R.string.section_shared_items));	
    				inSFLol.refresh();			
    				
    			} 
    			sharesTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
        		outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);	
    			if (outSFLol != null){    				
					aB.setTitle(getString(R.string.section_shared_items));				
					outSFLol.refresh();    				
    			}
    			
    			break;
    		}
    		case SETTINGS:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			startActivity(new Intent(this, SettingsActivity.class));
    			
    			drawerItem = lastDrawerItem;
    			selectDrawerItemLollipop(drawerItem);
    			
    			break;
    		}
    		case ACCOUNT:{
    			
    			if (nDALol != null){
					nDALol.setPositionClicked(-1);
				}
    			
    			accountFragment=MY_ACCOUNT_FRAGMENT;
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.color_navigation_drawer_selected));
    			
    			if (maF == null){
    				maF = new MyAccountFragment();
    			}
    			
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, maF, "maF");
    			ft.commit();
    			
    			customSearch.setVisibility(View.GONE);
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			if (createFolderMenuItem != null){
    				createFolderMenuItem.setVisible(false);
//        				rubbishBinMenuItem.setVisible(false);
	    			addMenuItem.setVisible(false);
	    			refreshMenuItem.setVisible(true);
	    			sortByMenuItem.setVisible(false);
	    			helpMenuItem.setVisible(true);
	    			upgradeAccountMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(false);
	    			unSelectMenuItem.setVisible(false);
	    			thumbViewMenuItem.setVisible(false);
	    			changePass.setVisible(true); 
	    			if (numberOfSubscriptions > 0){
	    				cancelSubscription.setVisible(true);
	    			}
	    			killAllSessions.setVisible(true);
	    			
	    			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MEGA/MEGAMasterKey.txt";
	    			log("Export in: "+path);
	    			File file= new File(path);
	    			if(file.exists()){
	    				exportMK.setVisible(false); 
		    			removeMK.setVisible(true); 
	    			}
	    			else{
	    				exportMK.setVisible(true); 
		    			removeMK.setVisible(false); 		
	    			}
	    			
//    	    			logoutMenuItem.setVisible(true);
//    	    			rubbishBinMenuItem.setIcon(R.drawable.ic_action_bar_null);
//    	    			rubbishBinMenuItem.setEnabled(false);
//    	    			addMenuItem.setIcon(R.drawable.ic_action_bar_null);
	    			addMenuItem.setEnabled(false);
//    	    			createFolderMenuItem.setIcon(R.drawable.ic_action_bar_null);
	    			createFolderMenuItem.setEnabled(false);
	    			rubbishBinMenuItem.setVisible(false);
	    			clearRubbishBinMenuitem.setVisible(false);
        			settingsMenuItem.setVisible(false);
	    		}
    			
    			
    			break;
    		}/*
    		case TRANSFERS:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (tF == null){
    				tF = new TransfersFragment();
    			}
    			tF.setTransfers(megaApi.getTransfers());
    			tF.setPause(!downloadPlay);
    			
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, tF, "tF");
    			ft.commit();
    			
    			customSearch.setVisibility(View.GONE);
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			if (createFolderMenuItem != null){
    				//Show
    				pauseRestartTransfersItem.setVisible(true);
        			upgradeAccountMenuItem.setVisible(false);
        			
    				//Hide
    				createFolderMenuItem.setVisible(false);
    				addContactMenuItem.setVisible(false);
        			addMenuItem.setVisible(false);
        			sortByMenuItem.setVisible(false);
        			selectMenuItem.setVisible(false);
        			unSelectMenuItem.setVisible(false);
        			thumbViewMenuItem.setVisible(false);
        			changePass.setVisible(false); 
        			exportMK.setVisible(false); 
        			removeMK.setVisible(false); 
        			rubbishBinMenuItem.setVisible(false);
        			clearRubbishBinMenuitem.setVisible(false);
        			settingsMenuItem.setVisible(false);
    				refreshMenuItem.setVisible(false);
    				helpMenuItem.setVisible(false);
        			
//        			if (downloadPlay){
//        				addMenuItem.setIcon(R.drawable.ic_pause);
//        			}
//        			else{
//        				addMenuItem.setIcon(R.drawable.ic_play);
//        			}
        			
        			if (megaApi.getTransfers().size() == 0){
        				downloadPlay = true;
        			}
    			}
    			
    			break;
    		}*/
    		case SAVED_FOR_OFFLINE:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (oFLol == null){
    				oFLol = new OfflineFragmentLollipop();
    				oFLol.setIsList(isListOffline);
    				oFLol.setPathNavigation("/");
    			}
    			else{
    				oFLol.setPathNavigation("/");
    				oFLol.setIsList(isListOffline);
    			}
    			
    			mTabHostCDrive.setVisibility(View.GONE);    			
    			viewPagerCDrive.setVisibility(View.GONE);
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, oFLol, "oFLol");
    			ft.commit();
    			
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			customSearch.setVisibility(View.VISIBLE);
    			

    			if (createFolderMenuItem != null){
	    			createFolderMenuItem.setVisible(false);
	    			addMenuItem.setVisible(false);
	    			sortByMenuItem.setVisible(false);
	    			upgradeAccountMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(true);
	    			unSelectMenuItem.setVisible(false);
	    			thumbViewMenuItem.setVisible(true);
	    			addMenuItem.setEnabled(false);
	    			createFolderMenuItem.setEnabled(false);
	    			changePass.setVisible(false); 
	    			exportMK.setVisible(false); 
	    			removeMK.setVisible(false); 
	    			if (isListOffline){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}
	    			rubbishBinMenuItem.setVisible(false);
	    			clearRubbishBinMenuitem.setVisible(false);
        			settingsMenuItem.setVisible(false);
    				refreshMenuItem.setVisible(false);
    				helpMenuItem.setVisible(false);
    			}
    			
    			break;
    		}
    		case SEARCH:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (sF == null){
        			sF = new SearchFragment();
        		}
    			
    			searchNodes = megaApi.search(megaApi.getRootNode(), searchQuery, true);
    			
    			drawerItem = DrawerItem.SEARCH;
    			
    			sF.setSearchNodes(searchNodes);
    			sF.setNodes(searchNodes);
    			sF.setSearchQuery(searchQuery);
    			sF.setParentHandle(parentHandleSearch);
    			sF.setLevels(levelsSearch);
    			
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, sF, "sF");
    			ft.commit();
    			
    			customSearch.setVisibility(View.VISIBLE);    			

    			if (createFolderMenuItem != null){
        			createFolderMenuItem.setVisible(false);
        			addMenuItem.setVisible(false);
        			sortByMenuItem.setVisible(false);
        			upgradeAccountMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(true);
	    			unSelectMenuItem.setVisible(false);
	    			thumbViewMenuItem.setVisible(true);
        			addMenuItem.setEnabled(true);
        			rubbishBinMenuItem.setVisible(false); 
        			clearRubbishBinMenuitem.setVisible(false);
        			changePass.setVisible(false); 
        			exportMK.setVisible(false); 
        			removeMK.setVisible(false); 
        			settingsMenuItem.setVisible(false);
    				refreshMenuItem.setVisible(false);
    				helpMenuItem.setVisible(false);
    			}
    			break;
    		}
    		case CAMERA_UPLOADS:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (nDALol != null){
    				nDALol.setPositionClicked(POS_CAMERA_UPLOADS);
    			}
    			
    			if (psF == null){
    				psF = new CameraUploadFragment();
    				psF.setIsList(isListCameraUpload);
   					psF.setFirstTimeCam(firstTimeCam);
				}
				else{
					psF.setIsList(isListCameraUpload);
					psF.setFirstTimeCam(firstTimeCam);
				}
				
				
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, psF, "psF");
    			ft.commit();
    			
    			
    			firstTimeCam = false;
    			
    			
				mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			customSearch.setVisibility(View.VISIBLE);
    			
    			if (createFolderMenuItem != null){
	    			createFolderMenuItem.setVisible(false);
	    			addMenuItem.setVisible(false);
	    			sortByMenuItem.setVisible(false);
	    			upgradeAccountMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(false);
	    			unSelectMenuItem.setVisible(false);
	    			thumbViewMenuItem.setVisible(true);
	    			addMenuItem.setEnabled(false);
	    			createFolderMenuItem.setEnabled(false);
	    			changePass.setVisible(false); 
	    			exportMK.setVisible(false); 
	    			removeMK.setVisible(false); 
        			settingsMenuItem.setVisible(false);
    				refreshMenuItem.setVisible(false);
    				helpMenuItem.setVisible(false);
	    			if (isListCameraUpload){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}
	    			rubbishBinMenuItem.setVisible(false);
	    			clearRubbishBinMenuitem.setVisible(false);
    			}
      			break;
    		}
			default:{
				break;
			}
    	}
    }
   
	@Override
	public void onBackPressed() {
    	log("onBackPressedLollipop");
		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		log("retryPendingConnections()");
		if (megaApi != null){
			megaApi.retryPendingConnections();
		}
		try { 
			statusDialog.dismiss();	
		} 
		catch (Exception ex) {}

		if (drawerItem == DrawerItem.CLOUD_DRIVE){

			int index = viewPagerCDrive.getCurrentItem();
			if(index==1){	
				//Rubbish Bin
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (rbFLol != null){
					if (rbFLol.onBackPressed() == 0){
						super.onBackPressed();
						return;
					}
				}
			}
			else if(index==0){
				//Cloud Drive
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (fbFLol != null){
					if (fbFLol.onBackPressed() == 0){
						super.onBackPressed();
						return;
					}
				}
			}			
		}
		else if (drawerItem == DrawerItem.INBOX){
			if (iFLol != null){			
				if (iFLol.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					selectDrawerItemLollipop(drawerItem);
					if(nDALol!=null){
						nDALol.setPositionClicked(0);
					}
					return;
				}
			}
		}
		else if (drawerItem == DrawerItem.SHARED_WITH_ME){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){				
				//OUTGOING				
				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
				log("Tag: "+ cFTag2);
				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag2);
				if (outSFLol != null){					
					if (outSFLol.onBackPressed() == 0){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItemLollipop(drawerItem);
						if(nDALol!=null){
							nDALol.setPositionClicked(0);
						}
						return;
					}					
				}
			}
			else{			
				//InCOMING
				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
				log("Tag: "+ cFTag1);
				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
				if (inSFLol != null){					
					if (inSFLol.onBackPressed() == 0){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItemLollipop(drawerItem);
						if(nDALol!=null){
							nDALol.setPositionClicked(0);
						}
						return;
					}					
				}				
			}	
		}	
		else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
			if (oFLol != null){				
				if (oFLol.onBackPressed() == 0){
					attr = dbH.getAttributes();
					if (attr != null){
						if (attr.getOnline() != null){
							if (!Boolean.parseBoolean(attr.getOnline())){
								super.onBackPressed();
								return;
							}
						}
					}
					
					if (fbFLol != null){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItemLollipop(drawerItem);
						if(nDALol!=null){
							nDALol.setPositionClicked(0);
						}
					}
					else{
						super.onBackPressed();
					}
					return;
				}
			}
		}
    	if (drawerItem == DrawerItem.CONTACTS){
    		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
    		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
    		if (cFLol != null){			
    			if (cFLol.onBackPressed() == 0){
    				drawerItem = DrawerItem.CLOUD_DRIVE;
    				selectDrawerItemLollipop(drawerItem);
    				if(nDALol!=null){
    					nDALol.setPositionClicked(0);
    				}
    				return;
    			}
    		}
    	}
    	if (drawerItem == DrawerItem.ACCOUNT){

    		switch(accountFragment){

    		case MY_ACCOUNT_FRAGMENT:{
    			if (maF != null){						
    				drawerItem = DrawerItem.CLOUD_DRIVE;
    				selectDrawerItemLollipop(drawerItem);
    				if(nDALol!=null){
    					nDALol.setPositionClicked(0);

    				}					
    			}
    			return;
    		}
    		case UPGRADE_ACCOUNT_FRAGMENT:{
    			if (upAF != null){						
    				drawerItem = DrawerItem.ACCOUNT;
    				selectDrawerItemLollipop(drawerItem);
    				if(nDALol!=null){
    					nDALol.setPositionClicked(-1);

    				}					
    			}
    			return;
    		}
    		case PAYMENT_FRAGMENT:{
    			if (pF != null){
    				pF.onBackPressed();
    			}
    			return;					
    		}
    		case CC_FRAGMENT:{
    			if (ccF != null){
    				int parameterType = ccF.getParameterType();
    				ArrayList<Product> accounts = ccF.getAccounts();
    				BitSet paymentBitSet = ccF.getPaymentBitSet();
    				showpF(parameterType, accounts, paymentBitSet);
    			}
    			else{
    				showUpAF(null);
    			}
    			return;
    		}
    		case OVERQUOTA_ALERT:{
    			if (upAF != null){						
    				drawerItem = DrawerItem.CLOUD_DRIVE;
    				selectDrawerItemLollipop(drawerItem);
    				if(nDALol!=null){
    					nDALol.setPositionClicked(0);

    				}					
    			}
    			return;
    		}
    		default:{
    			if (fbFLol != null){						
    				drawerItem = DrawerItem.CLOUD_DRIVE;
    				selectDrawerItemLollipop(drawerItem);
    				if(nDALol!=null){
    					nDALol.setPositionClicked(0);

    				}					
    			}
    		}
    		}
    	}
	}    
	
	@Override
	public void onPostCreate(Bundle savedInstanceState){
		log("onPostCreate");
		super.onPostCreate(savedInstanceState);
		if (!openLink){
			mDrawerToggle.syncState();
		}
	}
	
	@Override
	protected void onPostResume() {
	    super.onPostResume();
	    if (isSearching){	    	
			selectDrawerItemLollipop(DrawerItem.SEARCH);       		
    		isSearching = false;
	    } 
	}

	public void showOptionsPanel(MegaNode node){
		log("showOptionsPanel");
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			int index = viewPagerCDrive.getCurrentItem();
			if (index == 0){
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (fbFLol != null){
					fbFLol.showOptionsPanel(node);
				}
			}
			else{
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (rbFLol != null){
					rbFLol.showOptionsPanel(node);
				}				
			}
		}
		else if (drawerItem == DrawerItem.INBOX){
			if (iFLol != null){				
				iFLol.showOptionsPanel(node);				
			}
		}	
		else if (drawerItem == DrawerItem.SHARED_WITH_ME){
			int index = viewPagerShares.getCurrentItem();
			if (index == 0){
				String cFTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (inSFLol != null){
					inSFLol.showOptionsPanel(node);
				}
			}
			else{
				String cFTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (outSFLol != null){
					outSFLol.showOptionsPanel(node);
				}				
			}
		}	
	}
	
	public void showOptionsPanel(MegaOffline node){
		log("showOptionsPanel-Offline");
		
		if (oFLol != null){				
			oFLol.showOptionsPanel(node);				
		}			
	}
	
	public void showOptionsPanel(MegaUser user){
		log("showOptionsPanel-Offline");
		
		if (cFLol != null){				
			cFLol.showOptionsPanel(user);				
		}			
	}
	
	public void showOptionsPanel(MegaContactRequest request){
		log("showOptionsPanel-MegaContactRequest");
		String sRFTag1 = getFragmentTag(R.id.contact_tabs_pager, 1);	
		log("Tag: "+ sRFTag1);
		sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sRFTag1);
		if (sRFLol != null){				
			sRFLol.showOptionsPanel(request);				
		}			
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		log("onCreateOptionsMenuLollipop");
	
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_manager, menu);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
	    
	    final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchMenuItem = menu.findItem(R.id.action_search);
		searchMenuItem.setVisible(false);
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
		
		if (searchView != null){
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(true);
		}
		
		addContactMenuItem =menu.findItem(R.id.action_add_contact);
		addMenuItem = menu.findItem(R.id.action_add);
		pauseRestartTransfersItem = menu.findItem(R.id.action_pause_restart_transfers);
		createFolderMenuItem = menu.findItem(R.id.action_new_folder);
		importLinkMenuItem = menu.findItem(R.id.action_import_link);
		selectMenuItem = menu.findItem(R.id.action_select);
		unSelectMenuItem = menu.findItem(R.id.action_unselect);
		thumbViewMenuItem= menu.findItem(R.id.action_grid);
		
		refreshMenuItem = menu.findItem(R.id.action_menu_refresh);
		sortByMenuItem = menu.findItem(R.id.action_menu_sort_by);
		helpMenuItem = menu.findItem(R.id.action_menu_help);
		upgradeAccountMenuItem = menu.findItem(R.id.action_menu_upgrade_account);
		settingsMenuItem = menu.findItem(R.id.action_menu_settings);
		rubbishBinMenuItem = menu.findItem(R.id.action_rubbish_bin);
		clearRubbishBinMenuitem = menu.findItem(R.id.action_menu_clear_rubbish_bin);
		
		changePass = menu.findItem(R.id.action_menu_change_pass);
		exportMK = menu.findItem(R.id.action_menu_export_MK);
		removeMK = menu.findItem(R.id.action_menu_remove_MK);
		
		takePicture = menu.findItem(R.id.action_take_picture);
		
		cancelSubscription = menu.findItem(R.id.action_menu_cancel_subscriptions);
		cancelSubscription.setVisible(false);
		
		killAllSessions = menu.findItem(R.id.action_menu_kill_all_sessions);
		killAllSessions.setVisible(false);
		
//		if (drawerItem == DrawerItem.CLOUD_DRIVE){

		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			int index = viewPagerCDrive.getCurrentItem();
			log("----------------------------------------INDEX: "+index);
			if(index==1){
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (rbFLol != null){	
					//Show				
	    			sortByMenuItem.setVisible(true);
	    			selectMenuItem.setVisible(true);
	    			thumbViewMenuItem.setVisible(true);
	    			clearRubbishBinMenuitem.setVisible(true);
	    			
					//Hide
	    			refreshMenuItem.setVisible(false);
					pauseRestartTransfersItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
	    			addMenuItem.setVisible(false);
	    			addContactMenuItem.setVisible(false);
	    			upgradeAccountMenuItem.setVisible(false);
	    			unSelectMenuItem.setVisible(false);
	    			addMenuItem.setEnabled(false);
	    			changePass.setVisible(false); 
	    			exportMK.setVisible(false); 
	    			removeMK.setVisible(false); 
	    			importLinkMenuItem.setVisible(false);
	    			takePicture.setVisible(false);
	    			refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					settingsMenuItem.setVisible(false);
	    			
	    			if (isListRubbishBin){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}

					rbFLol.setIsList(isListRubbishBin);	        			
					rbFLol.setParentHandle(parentHandleRubbish);
					
					if(rbFLol.getItemCount()>0){
						selectMenuItem.setVisible(true);
						clearRubbishBinMenuitem.setVisible(true);
					}
					else{
						selectMenuItem.setVisible(false);
						clearRubbishBinMenuitem.setVisible(false);
					}        			
	   			
	    			rubbishBinMenuItem.setVisible(false);
	    			rubbishBinMenuItem.setTitle(getString(R.string.section_cloud_drive));    			
				}
			}			
			else{
				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (fbFLol!=null){
					//Cloud Drive
					//Show
					addMenuItem.setEnabled(true);
					addMenuItem.setVisible(true);
					createFolderMenuItem.setVisible(true);				
					sortByMenuItem.setVisible(true);
					thumbViewMenuItem.setVisible(true);
					rubbishBinMenuItem.setVisible(false);				
	    			upgradeAccountMenuItem.setVisible(false);    			
	    			importLinkMenuItem.setVisible(true);
	    			takePicture.setVisible(true);
	    			selectMenuItem.setVisible(true);
	    			
					//Hide
	    			pauseRestartTransfersItem.setVisible(false);
	    			addContactMenuItem.setVisible(false);    			
	    			unSelectMenuItem.setVisible(false); 
	    			clearRubbishBinMenuitem.setVisible(false); 
	    			changePass.setVisible(false); 
	    			exportMK.setVisible(false); 
	    			removeMK.setVisible(false); 
	    			refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					settingsMenuItem.setVisible(false);
					killAllSessions.setVisible(false);					

					if(fbFLol.getItemCount()>0){
						selectMenuItem.setVisible(true);
					}
					else{
						selectMenuItem.setVisible(false);
					}
	    			
	    			if (isListCloudDrive){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}
				}
			}
			return super.onCreateOptionsMenu(menu);
		}
		
		if (drawerItem == DrawerItem.CONTACTS){
			int index = viewPagerContacts.getCurrentItem();
			if (index == 0){
				String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
				cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (cFLol != null){
					//Show
					addContactMenuItem.setVisible(true);
					selectMenuItem.setVisible(true);
					sortByMenuItem.setVisible(true);
					thumbViewMenuItem.setVisible(true);
	    			upgradeAccountMenuItem.setVisible(false);
	    			
	    			//Hide	
	    			pauseRestartTransfersItem.setVisible(false);
					createFolderMenuItem.setVisible(false);				
	    			addMenuItem.setVisible(false);
	    			unSelectMenuItem.setVisible(false);    			
	    			addMenuItem.setEnabled(false);
	    			changePass.setVisible(false); 
	    			exportMK.setVisible(false); 
	    			removeMK.setVisible(false); 
	    			rubbishBinMenuItem.setVisible(false);
	    			clearRubbishBinMenuitem.setVisible(false);
	    			importLinkMenuItem.setVisible(false);
	    			takePicture.setVisible(false);
	    			refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					settingsMenuItem.setVisible(false);
	    			
	    			if (isListContacts){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}    
	    			
				}
			}
			else{
				//Show
    			upgradeAccountMenuItem.setVisible(false);
    			
    			//Hide	
    			addContactMenuItem.setVisible(false);
				selectMenuItem.setVisible(false);
				sortByMenuItem.setVisible(false);
				thumbViewMenuItem.setVisible(false);
    			pauseRestartTransfersItem.setVisible(false);
				createFolderMenuItem.setVisible(false);				
    			addMenuItem.setVisible(false);
    			unSelectMenuItem.setVisible(false);    			
    			addMenuItem.setEnabled(false);
    			changePass.setVisible(false); 
    			exportMK.setVisible(false); 
    			removeMK.setVisible(false); 
    			rubbishBinMenuItem.setVisible(false);
    			clearRubbishBinMenuitem.setVisible(false);
    			importLinkMenuItem.setVisible(false);
    			takePicture.setVisible(false);
    			refreshMenuItem.setVisible(false);
				helpMenuItem.setVisible(false);
				settingsMenuItem.setVisible(false);
			}
		}		
		
		if (drawerItem == DrawerItem.INBOX){
			if (iFLol != null){	
				//Show				
    			sortByMenuItem.setVisible(true);
 
    			if(iFLol.getItemCount()>0){
					selectMenuItem.setVisible(true);
				}
				else{
					selectMenuItem.setVisible(false);
				}
    			    			
				//Hide
    			refreshMenuItem.setVisible(false);
    			thumbViewMenuItem.setVisible(false);
				pauseRestartTransfersItem.setVisible(false);
				createFolderMenuItem.setVisible(false);
    			addMenuItem.setVisible(false);
    			addContactMenuItem.setVisible(false);
    			upgradeAccountMenuItem.setVisible(false);
    			unSelectMenuItem.setVisible(false);
    			addMenuItem.setEnabled(false);
    			changePass.setVisible(false); 
    			exportMK.setVisible(false); 
    			removeMK.setVisible(false); 
    			importLinkMenuItem.setVisible(false);
    			takePicture.setVisible(false);
    			refreshMenuItem.setVisible(false);
				helpMenuItem.setVisible(false);
				settingsMenuItem.setVisible(false);
    			thumbViewMenuItem.setVisible(false);
//    			rubbishBinMenuItem.setTitle(getString(R.string.section_cloud_drive));
    			clearRubbishBinMenuitem.setVisible(false);
    			rubbishBinMenuItem.setVisible(false);
			}
		}

		if (drawerItem == DrawerItem.SHARED_WITH_ME){
			//Lollipop
			int index = viewPagerShares.getCurrentItem();
			if(index==0){	
				String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
				if (inSFLol != null){
					sortByMenuItem.setVisible(true);
					thumbViewMenuItem.setVisible(true); 

					addMenuItem.setEnabled(true);
					addMenuItem.setVisible(true);

					log("parentHandleIncoming: "+parentHandleIncoming);
					if(parentHandleIncoming==-1){
						addMenuItem.setVisible(false);
					}
					else{
						addMenuItem.setVisible(true);
					}
					
					if(inSFLol.getItemCount()>0){
						selectMenuItem.setVisible(true);
					}
					else{
						selectMenuItem.setVisible(false);
					}

					//Hide
					pauseRestartTransfersItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);  				
					rubbishBinMenuItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					changePass.setVisible(false); 
					exportMK.setVisible(false); 
					removeMK.setVisible(false); 
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);					
	    			refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					settingsMenuItem.setVisible(false);
					upgradeAccountMenuItem.setVisible(false);
				}
			}
			else if(index==1){
				String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
				if (outSFLol != null){

					sortByMenuItem.setVisible(true);
					thumbViewMenuItem.setVisible(true); 

					log("parentHandleOutgoing: "+parentHandleOutgoing);
					if(parentHandleOutgoing==-1){
						addMenuItem.setVisible(false);
					}
					else{
						addMenuItem.setVisible(true);
					}
					
					if(outSFLol.getItemCount()>0){
						selectMenuItem.setVisible(true);
					}
					else{
						selectMenuItem.setVisible(false);
					}

					//Hide
					upgradeAccountMenuItem.setVisible(false);
					pauseRestartTransfersItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);  				
					rubbishBinMenuItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					changePass.setVisible(false); 
					exportMK.setVisible(false); 
					removeMK.setVisible(false); 
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);					
	    			refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					settingsMenuItem.setVisible(false);
				}
			}
		}
		
		if (drawerItem == DrawerItem.ACCOUNT){
			if (maF != null){
					
				//Show
				refreshMenuItem.setVisible(true);
				helpMenuItem.setVisible(true);
				upgradeAccountMenuItem.setVisible(false);
				changePass.setVisible(true); 
				
				//Hide
				pauseRestartTransfersItem.setVisible(false);
				createFolderMenuItem.setVisible(false);
				addContactMenuItem.setVisible(false);
    			addMenuItem.setVisible(false);
    			sortByMenuItem.setVisible(false);
    			selectMenuItem.setVisible(false);
    			unSelectMenuItem.setVisible(false);
    			thumbViewMenuItem.setVisible(false);
    			addMenuItem.setEnabled(false);
    			createFolderMenuItem.setEnabled(false);
    			rubbishBinMenuItem.setVisible(false);
    			clearRubbishBinMenuitem.setVisible(false);
    			importLinkMenuItem.setVisible(false);
    			takePicture.setVisible(false);
				settingsMenuItem.setVisible(false);
				
				if (numberOfSubscriptions > 0){
					cancelSubscription.setVisible(true);
				}
				
				killAllSessions.setVisible(true);
    			
    			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MEGA/MEGAMasterKey.txt";
    			log("Export in: "+path);
    			File file= new File(path);
    			if(file.exists()){
    				exportMK.setVisible(false); 
	    			removeMK.setVisible(true); 
    			}
    			else{
    				exportMK.setVisible(true); 
	    			removeMK.setVisible(false); 		
    			}
 
			}
		}

		if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
			if (oFLol != null){	
				//Show
    			sortByMenuItem.setVisible(true);
    			thumbViewMenuItem.setVisible(true); //TODO
    			
    			if(oFLol.getItemCount()>0){
					selectMenuItem.setVisible(true);
				}
				else{
					selectMenuItem.setVisible(false);
				}
    			
				//Hide
    			upgradeAccountMenuItem.setVisible(false);
				refreshMenuItem.setVisible(false);
    			pauseRestartTransfersItem.setVisible(false);
				createFolderMenuItem.setVisible(false);
				addContactMenuItem.setVisible(false);
    			addMenuItem.setVisible(false);
    			unSelectMenuItem.setVisible(false);
    			addMenuItem.setEnabled(false);
    			createFolderMenuItem.setEnabled(false);
    			changePass.setVisible(false); 
    			exportMK.setVisible(false); 
    			removeMK.setVisible(false); 
    			rubbishBinMenuItem.setVisible(false);
    			clearRubbishBinMenuitem.setVisible(false);
    			importLinkMenuItem.setVisible(false);
    			takePicture.setVisible(false);					
    			refreshMenuItem.setVisible(false);
				helpMenuItem.setVisible(false);
				settingsMenuItem.setVisible(false);
    			
    			if (isListOffline){	
    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
				}
				else{
					thumbViewMenuItem.setTitle(getString(R.string.action_list));
    			}    			
			}
		}
		
		if (sF != null){
			if (drawerItem == DrawerItem.SEARCH){
				if (createFolderMenuItem != null){
					
					//Show
	    			upgradeAccountMenuItem.setVisible(false);	    			

					//Hide
	    			thumbViewMenuItem.setVisible(false);
					pauseRestartTransfersItem.setVisible(false);
	    			createFolderMenuItem.setVisible(false);
	    			addContactMenuItem.setVisible(false);
	    			addMenuItem.setVisible(false);
	    			refreshMenuItem.setVisible(false);
	    			sortByMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(false);
	    			unSelectMenuItem.setVisible(false);
	    			changePass.setVisible(false); 
	    			exportMK.setVisible(false); 
	    			removeMK.setVisible(false); 
	    			addMenuItem.setEnabled(false);
	    			createFolderMenuItem.setEnabled(false);
	    			rubbishBinMenuItem.setVisible(false);
	    			clearRubbishBinMenuitem.setVisible(false);
	    			importLinkMenuItem.setVisible(false);
	    			takePicture.setVisible(false);					
	    			refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					settingsMenuItem.setVisible(false);
				}
			}
		}
		
		if (psF != null){
			if (drawerItem == DrawerItem.CAMERA_UPLOADS){
				
				//Show
    			upgradeAccountMenuItem.setVisible(false);
    			selectMenuItem.setVisible(true);
    			takePicture.setVisible(true);

				//Hide
				pauseRestartTransfersItem.setVisible(false);
				createFolderMenuItem.setVisible(false);
				addContactMenuItem.setVisible(false);
    			addMenuItem.setVisible(false);
    			refreshMenuItem.setVisible(false);
    			sortByMenuItem.setVisible(false);
    			unSelectMenuItem.setVisible(false);
    			thumbViewMenuItem.setVisible(true);
    			addMenuItem.setEnabled(false);
    			createFolderMenuItem.setEnabled(false);
    			changePass.setVisible(false); 
    			exportMK.setVisible(false); 
    			removeMK.setVisible(false); 
    			rubbishBinMenuItem.setVisible(false);
    			clearRubbishBinMenuitem.setVisible(false);
    			importLinkMenuItem.setVisible(false);					
    			refreshMenuItem.setVisible(false);
				helpMenuItem.setVisible(false);
				settingsMenuItem.setVisible(false);

    			if (isListCameraUpload){	
    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
				}
				else{
					thumbViewMenuItem.setTitle(getString(R.string.action_list));
    			}
			}
		}
	    	    
	    return super.onCreateOptionsMenu(menu);
	}	

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void openAdvancedDevices (long handle){
		log("openAdvancedDevices");
		handleToDownload = handle;
		String externalPath = Util.getExternalCardPath();	
		
		if(externalPath!=null){
			log("ExternalPath for advancedDevices: "+externalPath);
			MegaNode node = megaApi.getNodeByHandle(handle);
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
			    try{
			    	startActivityForResult(intent, WRITE_SD_CARD_REQUEST_CODE);			
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelectedLollipop");
		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}
		
		if (megaApi != null){
			megaApi.retryPendingConnections();
		}
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
		    case android.R.id.home:{
//		    case R.id.home:
//		    case R.id.homeAsUp:
	    	//case 16908332: //Algo pasa con la CyanogenMod
		    	if (mDrawerToggle.isDrawerIndicatorEnabled()) {
					mDrawerToggle.onOptionsItemSelected(item);
				}
		    	else {
		    		if (drawerItem == DrawerItem.CLOUD_DRIVE){
		    			int index = viewPagerCDrive.getCurrentItem();
		    			if(index==1){				
		    				//Rubbish Bin		
		    				String cFTag2 = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
		    				log("Tag: "+ cFTag2);
		    				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag2);
		    				if (rbFLol != null){					
		    					rbFLol.onBackPressed();	
		    					return true;
		    				}
		    			}
		    			else{			
		    				//Cloud Drive
		    				String cFTag1 = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);	
		    				log("Tag: "+ cFTag1);
		    				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
		    				if (fbFLol != null){					
		    					fbFLol.onBackPressed();					
		    				}				
		    			}
		    		}
		    		if (drawerItem == DrawerItem.SHARED_WITH_ME){
		    			int index = viewPagerShares.getCurrentItem();
		    			if(index==1){				
		    				//OUTGOING				
		    				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
		    				log("Tag: "+ cFTag2);
		    				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag2);
		    				if (outSFLol != null){					
		    					outSFLol.onBackPressed();				
		    				}
		    			}
		    			else{			
		    				//InCOMING
		    				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
		    				log("Tag: "+ cFTag1);
		    				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
		    				if (inSFLol != null){					
		    					inSFLol.onBackPressed();					
		    				}				
		    			}	
		    		}
		    		if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
		    			if (oFLol != null){
		    				oFLol.onBackPressed();
		    				return true;
		    			}
		    		}
		    		if (sF != null){
		    			if (drawerItem == DrawerItem.SEARCH){
		    				sF.onBackPressed();
		    				return true;
		    			}
		    		}
				}
		    	return true;
		    }
		    case R.id.action_import_link:{
		    	showImportLinkDialog();
		    	return true;
		    }
		    case R.id.action_take_picture:{
		    	
		    	this.takePicture();
		    	return true;
		    }
	        case R.id.action_search:{
	        	mSearchView.setIconified(false);
	        	return true;
	        }
	        case R.id.action_add_contact:{
	        	if (drawerItem == DrawerItem.CONTACTS){
	        		showNewContactDialog(null);
	        	}
	        	
	        	return true;
	        }
	        case R.id.action_menu_kill_all_sessions:{
	        	megaApi.killSession(-1, this);
	        	return true;
	        }
	        case R.id.action_new_folder:{
	        	if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        		showNewFolderDialog(null);
	        	}
	        	else if (drawerItem == DrawerItem.CONTACTS){
	        		showNewContactDialog(null);
	        	}
	        	return true;
	        }
	        case R.id.action_add:{
	        	
	        	if (drawerItem == DrawerItem.SHARED_WITH_ME){
	        		String swmTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
	        		inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(swmTag);
	        		if (viewPagerShares.getCurrentItem()==0){		
		        		if (inSFLol != null){	        		
		        			Long checkHandle = inSFLol.getParentHandle();		        			
		        			MegaNode checkNode = megaApi.getNodeByHandle(checkHandle);
		        			
		        			if((megaApi.checkAccess(checkNode, MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK)){
		        				this.uploadFile();
							}
							else if(megaApi.checkAccess(checkNode, MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK){
								this.uploadFile();
							}	
							else if(megaApi.checkAccess(checkNode, MegaShare.ACCESS_READ).getErrorCode() == MegaError.API_OK){
								log("Not permissions to upload");
								AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.no_permissions_upload), null, null);
								builder.setTitle(R.string.op_not_allowed);
								builder.setCancelable(false).setPositiveButton(R.string.cam_sync_ok, new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							                //do things
							        	   alertNotPermissionsUpload.dismiss();
							           }
							       });
								
								alertNotPermissionsUpload = builder.create();
								alertNotPermissionsUpload.show();
								Util.brandAlertDialog(alertNotPermissionsUpload);
							}
		        		}
	        		}
	        		swmTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
	        		outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(swmTag);	
	        		if (viewPagerShares.getCurrentItem()==1){	
		        		if (outSFLol != null){        			
		        			this.uploadFile();
		        		}
	        		}
	        	}	
	        	else {
	        		this.uploadFile();
	        	}
	        	
	        	return true;     	
	        }
	        case R.id.action_select:{
	        	//TODO: multiselect
	        	
        		if (drawerItem == DrawerItem.CLOUD_DRIVE){	  
        			int index = viewPagerCDrive.getCurrentItem();
        			log("----------------------------------------INDEX: "+index);
        			if(index==1){
        				//Rubbish bin
        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
        				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
        				if (rbFLol != null){
            				rbFLol.selectAll();
            				if (rbFLol.showSelectMenuItem()){
            					selectMenuItem.setVisible(true);
            					unSelectMenuItem.setVisible(false);
            				}
            				else{
            					selectMenuItem.setVisible(false);
            					unSelectMenuItem.setVisible(true);
            				}
            			}		
        			}
        			else{
        				//Cloud Drive
        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
        				if (fbFLol != null){
        					fbFLol.selectAll();
                			if (fbFLol.showSelectMenuItem()){
                				selectMenuItem.setVisible(true);
                				unSelectMenuItem.setVisible(false);
                			}
                			else{
                				selectMenuItem.setVisible(false);
                				unSelectMenuItem.setVisible(true);
                			}
        				}      
        			}  				
    				  				
        			return true;        		
	        	}
	        	if (drawerItem == DrawerItem.CONTACTS){
		        	String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		        	cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		        	if (cFLol != null){	        		
	        			cFLol.selectAll();
	        			if (cFLol.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
	        			}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}        			
	        		}
	        	}
	        	if (drawerItem == DrawerItem.SHARED_WITH_ME){
	        		String swmTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
	        		inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(swmTag);
	        		if (viewPagerShares.getCurrentItem()==0){		
		        		if (inSFLol != null){	        		
		        			inSFLol.selectAll();
		        			if (inSFLol.showSelectMenuItem()){
		        				selectMenuItem.setVisible(true);
		        				unSelectMenuItem.setVisible(false);
		        			}
		        			else{
		        				selectMenuItem.setVisible(false);
		        				unSelectMenuItem.setVisible(true);
		        			}	  
		        		}
	        		}
	        		swmTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
	        		outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(swmTag);	
	        		if (viewPagerShares.getCurrentItem()==1){	
		        		if (outSFLol != null){        			
		        			outSFLol.selectAll();
		        			if (outSFLol.showSelectMenuItem()){
		        				selectMenuItem.setVisible(true);
		        				unSelectMenuItem.setVisible(false);
		        			}
		        			else{
		        				selectMenuItem.setVisible(false);
		        				unSelectMenuItem.setVisible(true);
		        			}
	        			}
	        		}
        			return true;
	        	}
	        	if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
	        		if (oFLol != null){ 	        		
	    				oFLol.selectAll();
	    				if (oFLol.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
	        			}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
    			}
	        	if (drawerItem == DrawerItem.INBOX){
	        		if (iFLol != null){	        		
	        			iFLol.selectAll();
	    				if (iFLol.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
	        			}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
    			}
	        	if (psF != null){
	        		if (drawerItem == DrawerItem.CAMERA_UPLOADS){
	        			psF.selectAll();
	        			if (psF.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
	        			}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
	        	}  
	        	return true;
	        }
	        case R.id.action_grid:{	    			
	        	//TODO: gridView
	        	if (fbFLol != null){
	        		if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("fbFLol");
	        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.detach(currentFragment);
	        			fragTransaction.commit();

	        			isListCloudDrive = !isListCloudDrive;
	        			if (isListCloudDrive){	
		    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
						}
						else{
							thumbViewMenuItem.setTitle(getString(R.string.action_list));
		    			}
	        			fbFLol.setIsList(isListCloudDrive);
	        			fbFLol.setParentHandle(parentHandleBrowser);

	        			fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.attach(currentFragment);
	        			fragTransaction.commit();

	        		}
	        	}
	        	if (drawerItem == DrawerItem.CONTACTS){
		        	String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		    		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		        	if (cFLol != null){
	        		
	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(cFTag);
	        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.detach(currentFragment);
	        			fragTransaction.commit();

	        			isListContacts = !isListContacts;
	        			if (isListContacts){	
		    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
						}
						else{
							thumbViewMenuItem.setTitle(getString(R.string.action_list));
		    			}
	        			cFLol.setIsList(isListContacts);

	        			fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.attach(currentFragment);
	        			fragTransaction.commit();	

	        		}
	        	}
	        	if (drawerItem == DrawerItem.SHARED_WITH_ME){
	        		
	    			Toast toast = Toast.makeText(this, getString(R.string.general_not_yet_implemented), Toast.LENGTH_LONG);
	    			toast.show();
//	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("inSFLol");
//	        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
//	        			fragTransaction.detach(currentFragment);
//	        			fragTransaction.commit();
//
//	        			isListSharedWithMe = !isListSharedWithMe;
//	        			inSFLol.setIsList(isListSharedWithMe);
//	        			inSFLol.setParentHandle(parentHandleSharedWithMe);
//
//	        			fragTransaction = getSupportFragmentManager().beginTransaction();
//	        			fragTransaction.attach(currentFragment);
//	        			fragTransaction.commit();

	        		
	        	}
	        	if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
	        		if (oFLol != null){        			
        				Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("oFLol");
        				FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        				fragTransaction.detach(currentFragment);
        				fragTransaction.commit();

        				isListOffline = !isListOffline;
        				if (isListOffline){	
    	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
    					}
    					else{
    						thumbViewMenuItem.setTitle(getString(R.string.action_list));
    	    			}
        				oFLol.setIsList(isListOffline);						
        				oFLol.setPathNavigation(pathNavigation);
        				//oFLol.setGridNavigation(false);
        				//oFLol.setParentHandle(parentHandleSharedWithMe);

        				fragTransaction = getSupportFragmentManager().beginTransaction();
        				fragTransaction.attach(currentFragment);
        				fragTransaction.commit();
        				
	        		}
        		}
	        	if (drawerItem == DrawerItem.CAMERA_UPLOADS){
	        		if (psF != null){        			
        				Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("psF");
        				FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        				fragTransaction.detach(currentFragment);
        				fragTransaction.commit();

        				isListCameraUpload = !isListCameraUpload;
        				if (isListCameraUpload){	
    	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
    					}
    					else{
    						thumbViewMenuItem.setTitle(getString(R.string.action_list));
    	    			}
        				psF.setIsList(isListCameraUpload);

        				fragTransaction = getSupportFragmentManager().beginTransaction();
        				fragTransaction.attach(currentFragment);
        				fragTransaction.commit();

        			}
        		}
           	
	        	return true;
	        }	        
//	        case R.id.action_rubbish_bin:{
//	        	if (drawerItem == DrawerItem.RUBBISH_BIN){
//	        		drawerItem = DrawerItem.CLOUD_DRIVE;
//	        		selectDrawerItem(drawerItem);
//	        	}
//	        	else if (drawerItem == DrawerItem.CLOUD_DRIVE){
//	        		drawerItem = DrawerItem.RUBBISH_BIN;
//	        		selectDrawerItem(drawerItem);
//	        	}
//	        	return true;
//	        }
	        case R.id.action_menu_clear_rubbish_bin:{
	        	if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        		showClearRubbishBinDialog(null);
	        	}
	        	return true;
	        }
	        case R.id.action_menu_refresh:{
	        	switch(drawerItem){
		        	case CLOUD_DRIVE:{
		        		Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
			    		intent.setAction(LoginActivityLollipop.ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleBrowser);
			    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
		        		break;
		        	}
		        	case CONTACTS:{
		        		Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
			    		intent.setAction(LoginActivityLollipop.ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleBrowser);
			    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
			    		break;
		        	}
		        	case SHARED_WITH_ME:{
		        		
		        		int index = viewPagerShares.getCurrentItem();
		    			if(index==1){				
		    				//OUTGOING				
		    				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
		    				log("Tag: "+ cFTag2);
		    				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag2);
		    				if (outSFLol != null){					
		    					Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
					    		intent.setAction(LoginActivityLollipop.ACTION_REFRESH);
					    		intent.putExtra("PARENT_HANDLE", parentHandleOutgoing);
					    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
					    		break;
		    				}
		    			}
		    			else{			
		    				//InCOMING
		    				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
		    				log("Tag: "+ cFTag1);
		    				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
		    				if (inSFLol != null){					
		    					Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
					    		intent.setAction(LoginActivityLollipop.ACTION_REFRESH);
					    		intent.putExtra("PARENT_HANDLE", parentHandleIncoming);
					    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
					    		break;
		    				}				
		    			}	
		        	}
		        	case ACCOUNT:{
		        		Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
			    		intent.setAction(LoginActivityLollipop.ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleBrowser);
			    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
			    		break;
		        	}
	        	}
	        	return true;
	        }
	        case R.id.action_menu_sort_by:{
	        	switch(drawerItem){
		        	case CONTACTS:{
		        		AlertDialog sortByDialog;		        		
		        		LayoutInflater inflater = getLayoutInflater();
		        		View dialoglayout = inflater.inflate(R.layout.sortby_dialog, null);
		        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        		builder.setView(dialoglayout);
		        		builder.setTitle(getString(R.string.action_sort_by));
		        		builder.setPositiveButton(getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		        		
		        		sortByDialog = builder.create();
		        		sortByDialog.show();
		        		Util.brandAlertDialog(sortByDialog);
		        		
		        		TextView byNameTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_name_text);
		        		byNameTextView.setText(getString(R.string.sortby_name));
		        		final CheckedTextView ascendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_ascending_check);
		        		ascendingCheck.setText(getString(R.string.sortby_name_ascending));
		        		final CheckedTextView descendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_descending_check);
		        		descendingCheck.setText(getString(R.string.sortby_name_descending));
		        		
		        		TextView byDateTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_date_text);
		        		byDateTextView.setText(getString(R.string.sortby_date));
		        		final CheckedTextView newestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_newest_check);
		        		newestCheck.setText(getString(R.string.sortby_date_newest));
		        		final CheckedTextView oldestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_oldest_check);
		        		oldestCheck.setText(getString(R.string.sortby_date_oldest));
		        		
		        		TextView bySizeTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_size_text);
		        		bySizeTextView.setText(getString(R.string.sortby_size));
		        		final CheckedTextView largestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_largest_first_check);
		        		largestCheck.setText(getString(R.string.sortby_size_largest_first));
		        		final CheckedTextView smallestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_smallest_first_check);
		        		smallestCheck.setText(getString(R.string.sortby_size_smallest_first));
		        		
		        		View separator4 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator4);
		        		separator4.setVisibility(View.GONE);
		        		View separator5 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator5);
		        		separator5.setVisibility(View.GONE);
		        		View separator6 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator6);
		        		separator6.setVisibility(View.GONE);
		        		View separator7 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator7);
		        		separator7.setVisibility(View.GONE);
		        		View separator8 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator8);
		        		separator8.setVisibility(View.GONE);
		        		View separator9 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator9);
		        		separator9.setVisibility(View.GONE);
		        		
		        		byDateTextView.setVisibility(View.GONE);
		        		newestCheck.setVisibility(View.GONE);
		        		oldestCheck.setVisibility(View.GONE);
		        		bySizeTextView.setVisibility(View.GONE);
		        		largestCheck.setVisibility(View.GONE);
		        		smallestCheck.setVisibility(View.GONE);
		        		
		        		switch(orderContacts){
			        		case MegaApiJava.ORDER_DEFAULT_ASC:{
			        			ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_DEFAULT_DESC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			break;
			        		}
		        		}
		        		
		        		final AlertDialog dialog = sortByDialog;
		        		
		        		ascendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			selectSortByContacts(MegaApiJava.ORDER_DEFAULT_ASC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		descendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			selectSortByContacts(MegaApiJava.ORDER_DEFAULT_DESC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		break;
		        	}
		        	case SAVED_FOR_OFFLINE: {
		        		AlertDialog sortByDialog;		        		
		        		LayoutInflater inflater = getLayoutInflater();
		        		View dialoglayout = inflater.inflate(R.layout.sortby_dialog, null);
		        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        		builder.setView(dialoglayout);
		        		builder.setTitle(getString(R.string.action_sort_by));
		        		builder.setPositiveButton(getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		        		
		        		sortByDialog = builder.create();
		        		sortByDialog.show();
		        		Util.brandAlertDialog(sortByDialog);
		        		
		        		TextView byNameTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_name_text);
		        		byNameTextView.setText(getString(R.string.sortby_name));
		        		final CheckedTextView ascendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_ascending_check);
		        		ascendingCheck.setText(getString(R.string.sortby_name_ascending));
		        		final CheckedTextView descendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_descending_check);
		        		descendingCheck.setText(getString(R.string.sortby_name_descending));
		        		
		        		TextView byDateTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_date_text);
		        		byDateTextView.setText(getString(R.string.sortby_date));
		        		final CheckedTextView newestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_newest_check);
		        		newestCheck.setText(getString(R.string.sortby_date_newest));
		        		final CheckedTextView oldestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_oldest_check);
		        		oldestCheck.setText(getString(R.string.sortby_date_oldest));
		        		
		        		TextView bySizeTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_size_text);
		        		bySizeTextView.setText(getString(R.string.sortby_size));
		        		final CheckedTextView largestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_largest_first_check);
		        		largestCheck.setText(getString(R.string.sortby_size_largest_first));
		        		final CheckedTextView smallestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_smallest_first_check);
		        		smallestCheck.setText(getString(R.string.sortby_size_smallest_first));
		        		
		        		View separator4 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator4);
		        		separator4.setVisibility(View.GONE);
		        		View separator5 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator5);
		        		separator5.setVisibility(View.GONE);
		        		View separator6 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator6);
		        		separator6.setVisibility(View.GONE);
		        		View separator7 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator7);
		        		separator7.setVisibility(View.GONE);
		        		View separator8 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator8);
		        		separator8.setVisibility(View.GONE);
		        		View separator9 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator9);
		        		separator9.setVisibility(View.GONE);
		        		
		        		byDateTextView.setVisibility(View.GONE);
		        		newestCheck.setVisibility(View.GONE);
		        		oldestCheck.setVisibility(View.GONE);
		        		bySizeTextView.setVisibility(View.GONE);
		        		largestCheck.setVisibility(View.GONE);
		        		smallestCheck.setVisibility(View.GONE);
		        		
		        		switch(orderOffline){
			        		case MegaApiJava.ORDER_DEFAULT_ASC:{
			        			ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_DEFAULT_DESC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			break;
			        		}
		        		}
		        		
		        		final AlertDialog dialog = sortByDialog;
		        		
		        		ascendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			selectSortByOffline(MegaApiJava.ORDER_DEFAULT_ASC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		descendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			selectSortByOffline(MegaApiJava.ORDER_DEFAULT_DESC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		break;
		        		
		        	}
		        	case SHARED_WITH_ME: {		        		
 		
		         		AlertDialog sortByDialog;		        		
		        		LayoutInflater inflater = getLayoutInflater();
		        		View dialoglayout = inflater.inflate(R.layout.sortby_dialog, null);
		        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        		builder.setView(dialoglayout);
		        		builder.setTitle(getString(R.string.action_sort_by));
		        		builder.setPositiveButton(getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		        		
		        		sortByDialog = builder.create();
		        		sortByDialog.show();
		        		Util.brandAlertDialog(sortByDialog);
		        		
		        		TextView byNameTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_name_text);
		        		byNameTextView.setText(getString(R.string.sortby_name));
		        		final CheckedTextView ascendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_ascending_check);
		        		ascendingCheck.setText(getString(R.string.sortby_name_ascending));
		        		final CheckedTextView descendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_descending_check);
		        		descendingCheck.setText(getString(R.string.sortby_name_descending));
		        		
		        		TextView byDateTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_date_text);
		        		byDateTextView.setText(getString(R.string.sortby_date));
		        		final CheckedTextView newestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_newest_check);
		        		newestCheck.setText(getString(R.string.sortby_date_newest));
		        		final CheckedTextView oldestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_oldest_check);
		        		oldestCheck.setText(getString(R.string.sortby_date_oldest));
		        		
		        		TextView bySizeTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_size_text);
		        		bySizeTextView.setText(getString(R.string.sortby_size));
		        		final CheckedTextView largestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_largest_first_check);
		        		largestCheck.setText(getString(R.string.sortby_size_largest_first));
		        		final CheckedTextView smallestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_smallest_first_check);
		        		smallestCheck.setText(getString(R.string.sortby_size_smallest_first));
		        		
		        		View separator4 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator4);
		        		separator4.setVisibility(View.GONE);
		        		View separator5 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator5);
		        		separator5.setVisibility(View.GONE);
		        		View separator6 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator6);
		        		separator6.setVisibility(View.GONE);
		        		View separator7 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator7);
		        		separator7.setVisibility(View.GONE);
		        		View separator8 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator8);
		        		separator8.setVisibility(View.GONE);
		        		View separator9 = (View) sortByDialog.findViewById(R.id.sortby_dialog_separator9);
		        		separator9.setVisibility(View.GONE);
		        		
		        		byDateTextView.setVisibility(View.GONE);
		        		newestCheck.setVisibility(View.GONE);
		        		oldestCheck.setVisibility(View.GONE);
		        		bySizeTextView.setVisibility(View.GONE);
		        		largestCheck.setVisibility(View.GONE);
		        		smallestCheck.setVisibility(View.GONE);
		        		
		        		switch(orderOffline){
			        		case MegaApiJava.ORDER_DEFAULT_ASC:{
			        			ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_DEFAULT_DESC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			break;
			        		}
		        		}
		        		
		        		final AlertDialog dialog = sortByDialog;
		        		int tab =-1;
		        				        		
		        		if (viewPagerShares.getCurrentItem()==0){
		        			tab = 0;
		        		}
		        		else{
		        			tab = 1;
		        		}
		        		
		        		final int tabFinal = tab;
		        		
		        		ascendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			if(tabFinal==0){
			        				//INCOMING
			        				log("Incoming tab sort");
			        				selectSortByIncoming(MegaApiJava.ORDER_DEFAULT_ASC);
			        				
			        			}
			        			else{
			        				//OUTGOING
			        				log("Outgoing tab sort");
			        				selectSortByOutgoing(MegaApiJava.ORDER_DEFAULT_ASC);
			        			}	
			        			
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		descendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			if(tabFinal==0){
			        				//INCOMING
			        				log("Incoming tab sort");
			        				selectSortByIncoming(MegaApiJava.ORDER_DEFAULT_DESC);
			        				
			        			}
			        			else{
			        				//OUTGOING
			        				log("Outgoing tab sort");
			        				selectSortByOutgoing(MegaApiJava.ORDER_DEFAULT_DESC);
			        			}	
			        			
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		break;
	        		
		        	}
		        	case CLOUD_DRIVE:{
		        		AlertDialog sortByDialog;		        		
		        		LayoutInflater inflater = getLayoutInflater();
		        		View dialoglayout = inflater.inflate(R.layout.sortby_dialog, null);
		        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        		builder.setView(dialoglayout);
		        		builder.setTitle(getString(R.string.action_sort_by));
		        		builder.setPositiveButton(getString(R.string.general_cancel), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
		        		
		        		sortByDialog = builder.create();
		        		sortByDialog.show();
		        		Util.brandAlertDialog(sortByDialog);
		        		
		        		TextView byNameTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_name_text);
		        		byNameTextView.setText(getString(R.string.sortby_name));
		        		final CheckedTextView ascendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_ascending_check);
		        		ascendingCheck.setText(getString(R.string.sortby_name_ascending));
		        		final CheckedTextView descendingCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_descending_check);
		        		descendingCheck.setText(getString(R.string.sortby_name_descending));
		        		
		        		TextView byDateTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_date_text);
		        		byDateTextView.setText(getString(R.string.sortby_date));
		        		final CheckedTextView newestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_newest_check);
		        		newestCheck.setText(getString(R.string.sortby_date_newest));
		        		final CheckedTextView oldestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_oldest_check);
		        		oldestCheck.setText(getString(R.string.sortby_date_oldest));
		        		
		        		TextView bySizeTextView = (TextView) sortByDialog.findViewById(R.id.sortby_dialog_size_text);
		        		bySizeTextView.setText(getString(R.string.sortby_size));
		        		final CheckedTextView largestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_largest_first_check);
		        		largestCheck.setText(getString(R.string.sortby_size_largest_first));
		        		final CheckedTextView smallestCheck = (CheckedTextView) sortByDialog.findViewById(R.id.sortby_dialog_smallest_first_check);
		        		smallestCheck.setText(getString(R.string.sortby_size_smallest_first));
		        		
		        		switch(orderGetChildren){
			        		case MegaApiJava.ORDER_DEFAULT_ASC:{
			        			ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_DEFAULT_DESC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_CREATION_DESC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(true);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_CREATION_ASC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(true);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_SIZE_ASC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(true);
			        			break;
			        		}
			        		case MegaApiJava.ORDER_SIZE_DESC:{
			        			ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(true);
			        			smallestCheck.setChecked(false);
			        			break;
			        		}
		        		}
		        		
		        		final AlertDialog dialog = sortByDialog;
		        		
		        		ascendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			selectSortByCloudDrive(MegaApiJava.ORDER_DEFAULT_ASC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		descendingCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(true);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			selectSortByCloudDrive(MegaApiJava.ORDER_DEFAULT_DESC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		newestCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(true);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			selectSortByCloudDrive(MegaApiJava.ORDER_CREATION_DESC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		oldestCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(true);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);
			        			selectSortByCloudDrive(MegaApiJava.ORDER_CREATION_ASC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		largestCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(true);
			        			smallestCheck.setChecked(false);
			        			selectSortByCloudDrive(MegaApiJava.ORDER_SIZE_DESC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		smallestCheck.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(true);
			        			selectSortByCloudDrive(MegaApiJava.ORDER_SIZE_ASC);
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});
		        		
		        		break;
	        		}
		        	default:{
		        		Intent intent = new Intent(managerActivity, SortByDialogActivity.class);
			    		intent.setAction(SortByDialogActivity.ACTION_SORT_BY);
			    		startActivityForResult(intent, REQUEST_CODE_SORT_BY);
			    		break;
		        	}
	        	}
	        	return true;
	        }
	        case R.id.action_menu_help:{
	        	Intent intent = new Intent();
	            intent.setAction(Intent.ACTION_VIEW);
	            intent.addCategory(Intent.CATEGORY_BROWSABLE);
	            intent.setData(Uri.parse("https://mega.co.nz/#help/android"));
	            startActivity(intent);

	    		return true;
	    	}
	        case R.id.action_menu_upgrade_account:{
	        	drawerItem = DrawerItem.ACCOUNT;
	        	showUpAF(null);
				return true;
	        }
	        case R.id.action_menu_settings:{
//				if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
				    startActivity(new Intent(this, SettingsActivity.class));
//				}
//				else {
//					startActivity(new Intent(this, SettingsActivityHC.class));
//				}
	        	return true;
	        }
	        
	        case R.id.action_menu_change_pass:{
	        	Intent intent = new Intent(this, ChangePasswordActivity.class);
				startActivity(intent);
				return true;
	        }
	        case R.id.action_menu_remove_MK:{
	        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:

							final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MEGA/MEGAMasterKey.txt";
							final File f = new File(path);
				        	f.delete();	
				        	removeMK.setVisible(false);
				        	exportMK.setVisible(true);
				            break;

				        case DialogInterface.BUTTON_NEGATIVE:
				            //No button clicked
				            break;
				        }
				    }
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.remove_key_confirmation).setPositiveButton(R.string.general_yes, dialogClickListener)
				    .setNegativeButton(R.string.general_no, dialogClickListener).show();
				return true;
	        }
	        case R.id.action_menu_export_MK:{
	        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				        	String key = megaApi.exportMasterKey();
							
							BufferedWriter out;         
							try {						

								final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MEGA/MEGAMasterKey.txt";
								final File f = new File(path);
								log("Export in: "+path);
								FileWriter fileWriter= new FileWriter(path);	
								out = new BufferedWriter(fileWriter);	
								out.write(key);	
								out.close(); 								
								String toastMessage = getString(R.string.toast_master_key) + " " + path;
								Toast.makeText(getBaseContext(), toastMessage, Toast.LENGTH_LONG).show();	
								removeMK.setVisible(true);
					        	exportMK.setVisible(false);

							}catch (FileNotFoundException e) {
							 e.printStackTrace();
							}catch (IOException e) {
							 e.printStackTrace();
							}
				        	
				            break;

				        case DialogInterface.BUTTON_NEGATIVE:
				            //No button clicked
				            break;
				        }
				    }
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.export_key_confirmation).setPositiveButton(R.string.general_yes, dialogClickListener)
				    .setNegativeButton(R.string.general_no, dialogClickListener).show();		
	        	
	        	return true;
	        }
//	        case R.id.action_menu_logout:{
//	        	logout(managerActivity, (MegaApplication)getApplication(), megaApi, false);
//	        	return true;
//	        }
	        case R.id.action_menu_cancel_subscriptions:{
	        	if (megaApi != null){
	        		//Show the message
	        		showCancelMessage();	        		
	        	}
	        	return true;
	        }
            default:{
	            return super.onOptionsItemSelected(item);
            }
	    }
	}	
		
	public void showCancelMessage(){
		AlertDialog cancelDialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_cancel_subscriptions));
		
		LayoutInflater inflater = getLayoutInflater();
		View dialogLayout = inflater.inflate(R.layout.dialog_cancel_subscriptions, null);
		TextView message = (TextView) dialogLayout.findViewById(R.id.dialog_cancel_text);
		final EditText text = (EditText) dialogLayout.findViewById(R.id.dialog_cancel_feedback);
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);
		
		message.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleW));
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleW));
		
		builder.setView(dialogLayout);
		
		builder.setPositiveButton(getString(R.string.send_cancel_subscriptions), new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				feedback = text.getText().toString();
				if(feedback.matches("")||feedback.isEmpty()){
					Toast.makeText(managerActivity, getString(R.string.reason_cancel_subscriptions), Toast.LENGTH_SHORT).show();
				}
				else{
					showCancelConfirmation(feedback);			
				}								
			}
		});
		
		builder.setNegativeButton(getString(R.string.dismiss_cancel_subscriptions), new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		
		cancelDialog = builder.create();
		cancelDialog.show();
		Util.brandAlertDialog(cancelDialog);
	}
	
	public void showCancelConfirmation(final String feedback){
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        {
			        	log("Feedback: "+feedback);
			        	megaApi.creditCardCancelSubscriptions(feedback, managerActivity);
			        	break;
			        }
			        case DialogInterface.BUTTON_NEGATIVE:
			        {
			            //No button clicked
			        	log("Feedback: "+feedback);
			            break;
			        }
		        }
		    }
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.confirmation_cancel_subscriptions).setPositiveButton(R.string.general_yes, dialogClickListener)
		    .setNegativeButton(R.string.general_no, dialogClickListener).show();		
		
	}
	
	public void selectSortByContacts(int _orderContacts){
		this.orderContacts = _orderContacts;
		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cFLol != null){	
			cFLol.setOrder(orderContacts);
			if (orderContacts == MegaApiJava.ORDER_DEFAULT_ASC){
				cFLol.sortByNameAscending();
			}
			else{
				cFLol.sortByNameDescending();
			}
		}
	}
	
	public void takePicture(){
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.temporalPicDIR;		    		
        File newFolder = new File(path);
        newFolder.mkdirs();
        
        String file = path + "/picture.jpg";
        File newFile = new File(file);
        try {
        	newFile.createNewFile();
        } catch (IOException e) {}       

        Uri outputFileUri = Uri.fromFile(newFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
	}
	
	public void selectSortByOffline(int _orderOffline){
		log("selectSortByOffline");
		
		this.orderOffline = _orderOffline;
		
		if (oFLol != null){	
			oFLol.setOrder(orderOffline);
			if (orderOffline == MegaApiJava.ORDER_DEFAULT_ASC){
				oFLol.sortByNameAscending();
			}
			else{
				oFLol.sortByNameDescending();
			}
		}
	}
	
	public void selectSortByIncoming(int _orderIncoming){
		log("selectSortByIncoming");
		
		this.orderIncoming = _orderIncoming;
		
		if (inSFLol != null){	
			inSFLol.setOrder(orderIncoming);
			if (orderIncoming == MegaApiJava.ORDER_DEFAULT_ASC){
				inSFLol.sortByNameAscending();
			}
			else{
				inSFLol.sortByNameDescending();
			}
		}
	}
	
	public void selectSortByOutgoing(int _orderOutgoing){
		log("selectSortByOutgoing");
		
		this.orderOutgoing = _orderOutgoing;
		
		if (outSFLol != null){	
			outSFLol.setOrder(orderOutgoing);
			if (orderOutgoing == MegaApiJava.ORDER_DEFAULT_ASC){
				outSFLol.sortByNameAscending();
			}
			else{
				outSFLol.sortByNameDescending();
			}
		}
	}
	
	public void selectSortByCloudDrive(int _orderGetChildren){
		this.orderGetChildren = _orderGetChildren;
		MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
		if (parentNode != null){
			if (fbFLol != null){						
				ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
				fbFLol.setOrder(orderGetChildren);
				fbFLol.setNodes(nodes);
				fbFLol.getListView().invalidate();						
			}
		}
		else{
			if (fbFLol != null){						
				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
				fbFLol.setOrder(orderGetChildren);
				fbFLol.setNodes(nodes);
				fbFLol.getListView().invalidate();					
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		log("onItemClick");
		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}
		
		log("retryPendingConnections()");
		if (megaApi != null){
			megaApi.retryPendingConnections();
		}
			
		if (nDALol != null){
			nDALol.setPositionClicked(position);
		}
		
//		if (position >= 3){
//			position++;
//		}
		
		lastDrawerItem = drawerItem;
		drawerItem = DrawerItem.values()[position];
		
		if (drawerItem != lastDrawerItem){
			if (drawerItem != DrawerItem.SETTINGS){
				titleAB = "";
			}
			else{
				getSupportActionBar().setTitle(titleAB);
			}

			selectDrawerItemLollipop(drawerItem);			
		}
		else{
			getSupportActionBar().setTitle(titleAB);
			titleAB = "";
			mDrawerLayout.closeDrawer(Gravity.LEFT);
		}
	}

	public void uploadFile(){
		uploadDialog = new UploadHereDialog();
		uploadDialog.show(getSupportFragmentManager(), "fragment_upload");
	}
	
	@Override
	public void onClick(View v) {
		log("onClick");
		switch(v.getId()){
			case R.id.custom_search:{
				if (searchMenuItem != null) {
					MenuItemCompat.expandActionView(searchMenuItem);
				}
				else{
					log("searchMenuItem == null");
				}
				break;
			}
			case R.id.top_control_bar:{
				if (nDALol != null){
					nDALol.setPositionClicked(-1);
				}
				drawerItem = DrawerItem.ACCOUNT;
				titleAB = drawerItem.getTitle(this);
				
				selectDrawerItemLollipop(drawerItem);
				
				break;
			}
			case R.id.bottom_control_bar:{
				if (nDALol != null){
					nDALol.setPositionClicked(-1);
				}
				drawerItem = DrawerItem.ACCOUNT;
				titleAB = drawerItem.getTitle(this);
				
				selectDrawerItemLollipop(drawerItem);
				
				break;
			}
		}
	}
	
	static public void logout(Context context, MegaApiAndroid megaApi, boolean confirmAccount) {
		logout(context, megaApi, confirmAccount, false);
	}
	
	public void initGooglePlayPayments(){
		String base64EncodedPublicKey = Util.base64EncodedPublicKey_1 + Util.base64EncodedPublicKey_2 + Util.base64EncodedPublicKey_3 + Util.base64EncodedPublicKey_4 + Util.base64EncodedPublicKey_5; 
		
		log ("Creating IAB helper.");
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		mHelper.enableDebugLogging(true);
		
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                log("Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    log("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                log("Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
	}
	
	 /*
	 * Logout user
	 */
	static public void logout(Context context, MegaApiAndroid megaApi, boolean confirmAccount, boolean logoutBadSession) {
//		context.stopService(new Intent(context, BackgroundService.class));
		log("logout");
//		context.stopService(new Intent(context, CameraSyncService.class));
		
		File offlineDirectory = null;
		if (Environment.getExternalStorageDirectory() != null){
			offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR);
		}
//		if (context.getExternalFilesDir(null) != null){
//			offlineDirectory = context.getExternalFilesDir(null);
//		}
		else{
			offlineDirectory = context.getFilesDir();
		}
		
		try {
			Util.deleteFolderAndSubfolders(context, offlineDirectory);
		} catch (IOException e) {}
		
		File thumbDir = ThumbnailUtils.getThumbFolder(context);
		File previewDir = PreviewUtils.getPreviewFolder(context);
		
		try {
			Util.deleteFolderAndSubfolders(context, thumbDir);
		} catch (IOException e) {}
		
		try {
			Util.deleteFolderAndSubfolders(context, previewDir);
		} catch (IOException e) {}
		
		File externalCacheDir = context.getExternalCacheDir();
		File cacheDir = context.getCacheDir();
		try {
			Util.deleteFolderAndSubfolders(context, externalCacheDir);
		} catch (IOException e) {}
		
		try {
			Util.deleteFolderAndSubfolders(context, cacheDir);
		} catch (IOException e) {}
		
		PackageManager m = context.getPackageManager();
		String s = context.getPackageName();
		try {
		    PackageInfo p = m.getPackageInfo(s, 0);
		    s = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
		    log("Error Package name not found " + e);
		}
		
		File appDir = new File(s);
		
		for (File c : appDir.listFiles()){
			if (c.isFile()){
				c.delete();
			}
//			deleteFolderAndSubfolders(context, c);
		}
		
		
//		DatabaseHandler dbH = new DatabaseHandler(context);
		Intent cancelTransfersIntent = new Intent(context, DownloadService.class);
		cancelTransfersIntent.setAction(DownloadService.ACTION_CANCEL);
		context.startService(cancelTransfersIntent);
		cancelTransfersIntent = new Intent(context, UploadService.class);
		cancelTransfersIntent.setAction(UploadService.ACTION_CANCEL);
		context.startService(cancelTransfersIntent);
		
		DatabaseHandler dbH = DatabaseHandler.getDbHandler(context);
		dbH.clearCredentials();
		if (dbH.getPreferences() != null){
			dbH.clearPreferences();
			dbH.setFirstTime(false);
//			dbH.setPinLockEnabled(false);
//			dbH.setPinLockCode("");
//			dbH.setCamSyncEnabled(false);
//			dbH.setStorageAskAlways(true);
			Intent stopIntent = null;
			stopIntent = new Intent(context, CameraSyncService.class);
			stopIntent.setAction(CameraSyncService.ACTION_LOGOUT);
			context.startService(stopIntent);
		}
		dbH.clearOffline();
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		if (!logoutBadSession){
			megaApi.logout();
		}
		drawerItem = null;
		
		if (!confirmAccount){		
			if(managerActivity != null)	{
				Intent intent = new Intent(managerActivity, TourActivity.class);
		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				managerActivity.startActivity(intent);
				managerActivity.finish();
				managerActivity = null;
			}
			else{
//				Intent intent = new Intent (context, TourActivity.class);
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//		        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//				context.startActivity(intent);
//				if (context instanceof Activity){
//					((Activity)context).finish();
//				}
//				context = null;
			}
		}
		else{
			if (managerActivity != null){
				managerActivity.finish();
			}
			else{
				((Activity)context).finish();
			}
		}
	}	
	

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: "  + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_ACCOUNT_DETAILS){
			log("account_details request start");
		}
		else if (request.getType() == MegaRequest.TYPE_LOGOUT){
			log("logout request start");
		}	
		else if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
			log("fecthnodes request start");
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			log("move request start");
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE){
			log("remove request start");
		}
		else if (request.getType() == MegaRequest.TYPE_EXPORT){
			log("export request start");
		}
		else if(request.getType() == MegaRequest.TYPE_RENAME){
			log("rename request start");
		}
		else if (request.getType() == MegaRequest.TYPE_COPY){
			log("copy request start");
		}
		else if (request.getType() == MegaRequest.TYPE_CREATE_FOLDER){
			log("create folder start");
		}
		else if (request.getType() == MegaRequest.TYPE_PAUSE_TRANSFERS){
			log("pause transfers start");
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		log("---------onRequestFinishLollipop: "  + request.getRequestString());
		
		if (request.getType() == MegaRequest.TYPE_ACCOUNT_DETAILS){
			log ("account_details request");
			if (e.getErrorCode() == MegaError.API_OK){
				
				accountInfo = request.getMegaAccountDetails();				
				
				accountType = accountInfo.getProLevel();
				
				switch (accountType){
					case 0:{
						levelAccountDetails = -1;
						break;
					}
					case 1:{
						levelAccountDetails = 1;
						break;
					}
					case 2:{
						levelAccountDetails = 2;
						break;
					}
					case 3:{
						levelAccountDetails = 3;
						break;
					}
					case 4:{
						levelAccountDetails = 0;
						break;
					}
				}

				accountDetailsFinished = true;
				
				if (inventoryFinished){
					if (levelAccountDetails < levelInventory){
						if (maxP != null){
							megaApi.submitPurchaseReceipt(maxP.getOriginalJson(), this);
						}
					}
				}
				
				long totalStorage = accountInfo.getStorageMax();
				long usedStorage = accountInfo.getStorageUsed();;
				boolean totalGb = false;				
		        
		        bottomControlBar.setVisibility(View.VISIBLE);
		        usedPerc = 0;
		        if (totalStorage != 0){
		        	usedPerc = (int)((100 * usedStorage) / totalStorage);
		        }
		        usedSpaceBar.setProgress(usedPerc);
				
				totalStorage = ((totalStorage / 1024) / 1024) / 1024;
				String total = "";
				if (totalStorage >= 1024){
					totalStorage = totalStorage / 1024;
					total = total + totalStorage + " TB";
				}
				else{
					 total = total + totalStorage + " GB";
					 totalGb = true;
				}

				usedStorage = ((usedStorage / 1024) / 1024) / 1024;
				String used = "";
				if(totalGb){
					usedGbStorage = usedStorage;
					used = used + usedStorage + " GB";					
				}
				else{
					if (usedStorage >= 1024){
						usedGbStorage = usedStorage;
						usedStorage = usedStorage / 1024;

						used = used + usedStorage + " TB";
					}
					else{
						usedGbStorage = usedStorage;
						used = used + usedStorage + " GB";
					}
				}
		      
//				String usedSpaceString = getString(R.string.used_space, used, total);
				String usedSpaceString = used + " / " + total;
		        usedSpace.setText(usedSpaceString);
		        Spannable wordtoSpan = new SpannableString(usedSpaceString);

		        if (usedPerc < 90){
		        	usedSpaceBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_ok));
		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_ok)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        	usedSpaceWarning.setVisibility(View.INVISIBLE);
		        }
		        else if ((usedPerc >= 90) && (usedPerc <= 95)){
		        	usedSpaceBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_warning));
		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_warning)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        	usedSpaceWarning.setVisibility(View.VISIBLE);
		        }
		        else{
		        	if (usedPerc > 100){
			        	usedPerc = 100;			        	
			        }
		        	usedSpaceWarning.setVisibility(View.VISIBLE);
		        	usedSpaceBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_exceed));    
		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_exceed)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        }      
		        
		        wordtoSpan.setSpan(new RelativeSizeSpan(1.5f), 0, used.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.navigation_drawer_mail)), used.length() + 1, used.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        wordtoSpan.setSpan(new RelativeSizeSpan(1.5f), used.length() + 3, used.length() + 3 + total.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        usedSpace.setText(wordtoSpan);	
		        
		        log("onRequest TYPE_ACCOUNT_DETAILS: "+usedPerc);

		        if(drawerItem==DrawerItem.CLOUD_DRIVE){
		        	if (usedPerc > 95){
		        		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
						ft.detach(fbFLol);
						ft.attach(fbFLol);
						ft.commitAllowingStateLoss();
		        	}
		        }
			}
		}
		else if (request.getType() == MegaRequest.TYPE_GET_PAYMENT_METHODS){
			if (e.getErrorCode() == MegaError.API_OK){
				paymentBitSet = Util.convertToBitSet(request.getNumber());
			}
		}
		else if(request.getType() == MegaRequest.TYPE_CREDIT_CARD_QUERY_SUBSCRIPTIONS){
			if (e.getErrorCode() == MegaError.API_OK){
				numberOfSubscriptions = request.getNumber();
				log("NUMBER OF SUBS: " + numberOfSubscriptions);
				if (cancelSubscription != null){
					cancelSubscription.setVisible(false);
				}
				if (numberOfSubscriptions > 0){
					if (cancelSubscription != null){
						if (drawerItem == DrawerItem.ACCOUNT){
							if (maF != null){
								cancelSubscription.setVisible(true);
							}
						}
					}
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CREDIT_CARD_CANCEL_SUBSCRIPTIONS){
			if (e.getErrorCode() == MegaError.API_OK){
				Toast.makeText(this, getString(R.string.cancel_subscription_ok), Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(this, getString(R.string.cancel_subscription_error), Toast.LENGTH_SHORT).show();
			}
			megaApi.creditCardQuerySubscriptions(this);
		}
		else if (request.getType() == MegaRequest.TYPE_LOGOUT){
			log("logout finished");
//			if (request.getType() == MegaRequest.TYPE_LOGOUT){
//				log("type_logout");
//				if (e.getErrorCode() == MegaError.API_ESID){
//					log("calling ManagerActivityLollipop.logout");
//					MegaApiAndroid megaApi = app.getMegaApi(); 
//					ManagerActivityLollipop.logout(managerActivity, app, megaApi, false);
//				}
//			}
		}
		else if (request.getType() == MegaRequest.TYPE_GET_USER_DATA){
			if (e.getErrorCode() == MegaError.API_OK){
				userName.setText(request.getName());
			}
		}
		else if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
			log("fecthnodes request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE_CONTACT){
			
			if (e.getErrorCode() == MegaError.API_OK){
			
				if(drawerItem==DrawerItem.CONTACTS){
					cFLol.notifyDataSetChanged();
				}	
			}
			else{
				log("Termino con error");
			}
		}
		else if (request.getType() == MegaRequest.TYPE_INVITE_CONTACT){	
			log("MegaRequest.TYPE_INVITE_CONTACT finished: "+request.getNumber());

			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if(request.getNumber()==MegaContactRequest.INVITE_ACTION_REMIND){
				Toast.makeText(this, getString(R.string.context_contact_invitation_resent), Toast.LENGTH_LONG).show();
			}
			else{
				if (e.getErrorCode() == MegaError.API_OK){
					
					if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD)
					{
						Toast.makeText(this, getString(R.string.context_contact_added), Toast.LENGTH_LONG).show();					
					}
					else if(request.getNumber()==MegaContactRequest.INVITE_ACTION_DELETE)
					{
						Toast.makeText(this, getString(R.string.context_contact_invitation_deleted), Toast.LENGTH_LONG).show();					
					}
//					else
//					{
//						Toast.makeText(this, getString(R.string.context_contact_invitation_resent), Toast.LENGTH_LONG).show();					
//					}				
				}
				else{
					if(e.getErrorCode()==MegaError.API_EEXIST)
					{
						Toast.makeText(this, request.getEmail()+" "+getString(R.string.context_contact_already_exists), Toast.LENGTH_LONG).show();
					}
					else{
						Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
					}				
					log("ERROR: " + e.getErrorCode() + "___" + e.getErrorString());
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REPLY_CONTACT_REQUEST){	
			log("MegaRequest.TYPE_REPLY_CONTACT_REQUEST finished: "+request.getType());
			
			if (e.getErrorCode() == MegaError.API_OK){
				
				Toast.makeText(this, getString(R.string.context_invitacion_reply), Toast.LENGTH_LONG).show();
	//			Toast.makeText(this, getString(R.string.context_correctly_moved), Toast.LENGTH_SHORT).show();

			}
			else{
				Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			
			if (e.getErrorCode() == MegaError.API_OK){
//				Toast.makeText(this, getString(R.string.context_correctly_moved), Toast.LENGTH_SHORT).show();
				if (drawerItem == DrawerItem.CLOUD_DRIVE){
					if (moveToRubbish){
						//Update both tabs
        				//Rubbish bin
        				if (rbFLol != null){
        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbFLol.getParentHandle()), orderGetChildren);
    						rbFLol.setNodes(nodes);
    						rbFLol.getListView().invalidate();
            			}	

        				//Cloud Drive
        				if (fbFLol != null){
        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
    						fbFLol.setNodes(nodes);
    						fbFLol.getListView().invalidate();
        				}	        			
					}
					else{
						int index = viewPagerCDrive.getCurrentItem();
	        			log("----------------------------------------INDEX: "+index);
	        			if(index==1){
	        				//Rubbish bin
	        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
	        				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
	        				if (rbFLol != null){
	        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbFLol.getParentHandle()), orderGetChildren);
	    						rbFLol.setNodes(nodes);
	    						rbFLol.getListView().invalidate();
	            			}		
	        			}
	        			else{
	        				//Cloud Drive
	        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
	        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
	        				if (fbFLol != null){
	        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
	    						fbFLol.setNodes(nodes);
	    						fbFLol.getListView().invalidate();
	        				}
	        			}
					}					
				}
				else if (drawerItem == DrawerItem.INBOX){
					if (iFLol != null){
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(iFLol.getParentHandle()), orderGetChildren);
//							rbFLol.setNodes(nodes);
						iFLol.refresh();
						if (moveToRubbish){
							//Refresh Rubbish Fragment
							String cFTagRb = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
	        				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTagRb);
	        				if (rbFLol != null){
	        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbFLol.getParentHandle()), orderGetChildren);
	    						rbFLol.setNodes(nodes);
	    						rbFLol.getListView().invalidate();
	            			}	
						}
						else{
							//Refresh Cloud Drive
							String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
	        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
	        				if (fbFLol != null){
	        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
	    						fbFLol.setNodes(nodes);
	    						fbFLol.getListView().invalidate();
	        				}
						}
					}
				}	
				else if (drawerItem == DrawerItem.SHARED_WITH_ME){
					String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
    				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
					if (inSFLol != null){
						//TODO: ojo con los hijos
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSFLol.getParentHandle()), orderGetChildren);
//							inSFLol.setNodes(nodes);
						inSFLol.getListView().invalidate();
					}
	    			sharesTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
	        		outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
					if (outSFLol != null){
						//TODO: ojo con los hijos
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(outSFLol.getParentHandle()), orderGetChildren);
//							inSFLol.setNodes(nodes);
						outSFLol.getListView().invalidate();
					}
					
					if (moveToRubbish){
						//Refresh Rubbish Fragment
						String cFTagRb = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
        				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTagRb);
        				if (rbFLol != null){
        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbFLol.getParentHandle()), orderGetChildren);
    						rbFLol.setNodes(nodes);
    						rbFLol.getListView().invalidate();
            			}	
					}
					else{
						//Refresh Cloud Drive
						String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
        				if (fbFLol != null){
        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
    						fbFLol.setNodes(nodes);
    						fbFLol.getListView().invalidate();
        				}
					}
				}
				else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
					if (oFLol != null){
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(iFLol.getParentHandle()), orderGetChildren);
//							rbFLol.setNodes(nodes);
						oFLol.refreshPaths();
						//Refresh Cloud Drive
						String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
        				if (fbFLol != null){
        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
    						fbFLol.setNodes(nodes);
    						fbFLol.getListView().invalidate();
        				}						
					}
				}	
			}	
			
			if (moveToRubbish){
				if (e.getErrorCode() == MegaError.API_OK){
					Toast.makeText(this, getString(R.string.context_correctly_moved_to_rubbish), Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(this, getString(R.string.context_no_moved), Toast.LENGTH_LONG).show();
				}
				moveToRubbish = false;
				log("move to rubbish request finished");
			}
			else{
				if (e.getErrorCode() == MegaError.API_OK){
					Toast.makeText(this, getString(R.string.context_correctly_moved), Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(this, getString(R.string.context_no_moved), Toast.LENGTH_LONG).show();
				}
			
				log("move nodes request finished");
			}
			
			
		}
		else if (request.getType() == MegaRequest.TYPE_KILL_SESSION){
			if (e.getErrorCode() == MegaError.API_OK){
				Toast.makeText(this, getString(R.string.success_kill_all_sessions), Toast.LENGTH_SHORT).show();
			}
			else
			{
				log("error when killing sessions: "+e.getErrorString());
				Toast.makeText(this, getString(R.string.error_kill_all_sessions), Toast.LENGTH_SHORT).show();
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE){
			
			log("requestFinish "+MegaRequest.TYPE_REMOVE);
			if (e.getErrorCode() == MegaError.API_OK){
				if (statusDialog != null){
					if (statusDialog.isShowing()){
						try { 
							statusDialog.dismiss();	
						} 
						catch (Exception ex) {}
						Toast.makeText(this, getString(R.string.context_correctly_removed), Toast.LENGTH_SHORT).show();
					}
				}
				if (drawerItem == DrawerItem.CLOUD_DRIVE){
					
					int index = viewPagerCDrive.getCurrentItem();
        			log("----------------------------------------INDEX: "+index);
        			if(index==1){
        				//Rubbish bin
        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
        				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
        				if (rbFLol != null){
        					if (isClearRubbishBin){
    							isClearRubbishBin = false;
    							parentHandleRubbish = megaApi.getRubbishNode().getHandle();
    							rbFLol.setParentHandle(megaApi.getRubbishNode().getHandle());
    							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
    							rbFLol.setNodes(nodes);
    							rbFLol.getListView().invalidate();
    							aB.setTitle(getString(R.string.section_rubbish_bin));	
    							getmDrawerToggle().setDrawerIndicatorEnabled(true);
    						}
    						else{
    							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbFLol.getParentHandle()), orderGetChildren);
    							rbFLol.setNodes(nodes);
    							rbFLol.getListView().invalidate();
    						}
            			}		
        			}
        			else{
        				//Cloud Drive
        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
        				if (fbFLol != null){
        					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
    						fbFLol.setNodes(nodes);
    						fbFLol.getListView().invalidate();
        				}
        			}
				}	
			}
			else{
				Toast.makeText(this, getString(R.string.context_no_removed), Toast.LENGTH_LONG).show();
			}
			log("remove request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_EXPORT){
			MegaNode node = megaApi.getNodeByHandle(request.getNodeHandle());
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				
				if (isGetLink){
					final String link = request.getLink();
					
					AlertDialog getLinkDialog;
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(getString(R.string.context_get_link_menu));
					
					LayoutInflater inflater = getLayoutInflater();
					View dialoglayout = inflater.inflate(R.layout.dialog_link, null);
					ImageView thumb = (ImageView) dialoglayout.findViewById(R.id.dialog_link_thumbnail);
					TextView url = (TextView) dialoglayout.findViewById(R.id.dialog_link_link_url);
					TextView key = (TextView) dialoglayout.findViewById(R.id.dialog_link_link_key);
					
					String urlString = "";
					String keyString = "";
					String [] s = link.split("!");
					if (s.length == 3){
						urlString = s[0] + "!" + s[1];
						keyString = s[2];
					}
					if (node.isFolder()){
						thumb.setImageResource(R.drawable.folder_thumbnail);
					}
					else{
						thumb.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());
					}
					
					Display display = getWindowManager().getDefaultDisplay();
					DisplayMetrics outMetrics = new DisplayMetrics();
					display.getMetrics(outMetrics);
					float density = getResources().getDisplayMetrics().density;
	
					float scaleW = Util.getScaleW(outMetrics, density);
					float scaleH = Util.getScaleH(outMetrics, density);
					
					url.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleW));
					key.setTextSize(TypedValue.COMPLEX_UNIT_SP, (14*scaleW));
					
					url.setText(urlString);
					key.setText(keyString);
					
					
					builder.setView(dialoglayout);
					
					builder.setPositiveButton(getString(R.string.context_send_link), new android.content.DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.setType("text/plain");
							intent.putExtra(Intent.EXTRA_TEXT, link);
							startActivity(Intent.createChooser(intent, getString(R.string.context_get_link)));
						}
					});
					
					builder.setNegativeButton(getString(R.string.context_copy_link), new android.content.DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
							    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							    clipboard.setText(link);
							} else {
							    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", link);
					            clipboard.setPrimaryClip(clip);
							}
							
							Toast.makeText(managerActivity, getString(R.string.file_properties_get_link), Toast.LENGTH_LONG).show();
						}
					});
					
					getLinkDialog = builder.create();
					getLinkDialog.show();
					Util.brandAlertDialog(getLinkDialog);
				}
			}
			else{
				Toast.makeText(this, getString(R.string.context_no_link), Toast.LENGTH_LONG).show();
			}
			log("export request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_RENAME){
			
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				Toast.makeText(this, getString(R.string.context_correctly_renamed), Toast.LENGTH_SHORT).show();
				if (drawerItem == DrawerItem.CLOUD_DRIVE){
					
					int index = viewPagerCDrive.getCurrentItem();
        			log("----------------------------------------INDEX: "+index);
        			if(index==0){
        		        //Cloud Drive
        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
        				if (fbFLol != null){					
    						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
    						fbFLol.setNodes(nodes);
    						fbFLol.getListView().invalidate();
    					}
        			}					
				}
				else if (drawerItem == DrawerItem.INBOX){
					
					if (iFLol != null){					
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSFLol.getParentHandle()), orderGetChildren);
						//TODO: ojo con los hijos
//						inSFLol.setNodes(nodes);
						iFLol.getListView().invalidate();
					}			
				}
				else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
					
					if (oFLol != null){					
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSFLol.getParentHandle()), orderGetChildren);
						//TODO: ojo con los hijos
//						inSFLol.setNodes(nodes);
						oFLol.getListView().invalidate();
					}			
				}
				else if (drawerItem == DrawerItem.SHARED_WITH_ME){
					String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
    				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
					if (inSFLol != null){					
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSFLol.getParentHandle()), orderGetChildren);
						//TODO: ojo con los hijos
//						inSFLol.setNodes(nodes);
						inSFLol.getListView().invalidate();
					}
	    			sharesTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
	        		outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
					if (outSFLol != null){					
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSFLol.getParentHandle()), orderGetChildren);
						//TODO: ojo con los hijos
//						inSFLol.setNodes(nodes);
						outSFLol.getListView().invalidate();
					}
				}
			}
			else{
				Toast.makeText(this, getString(R.string.context_no_renamed), Toast.LENGTH_LONG).show();
			}
		} 
		else if (request.getType() == MegaRequest.TYPE_COPY){
			log("TYPE_COPY");
			if(sendToInbox){
				log("sendToInbox");
				if (drawerItem == DrawerItem.INBOX||drawerItem == DrawerItem.CLOUD_DRIVE||drawerItem == DrawerItem.CONTACTS){
					sendToInbox=false;
					if (e.getErrorCode() == MegaError.API_OK){
						Toast.makeText(this, getString(R.string.context_correctly_sent), Toast.LENGTH_SHORT).show();
					}
					else if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
						log("OVERQUOTA ERROR: "+e.getErrorCode());
						showOverquotaAlert();
					}
					else
					{
						Toast.makeText(this, getString(R.string.context_no_sent), Toast.LENGTH_LONG).show();
					}
				}				
			}
			else{
				try { 
					statusDialog.dismiss();	
				} 
				catch (Exception ex) {}
				
				if (e.getErrorCode() == MegaError.API_OK){
					Toast.makeText(this, getString(R.string.context_correctly_copied), Toast.LENGTH_SHORT).show();
					if (drawerItem == DrawerItem.CLOUD_DRIVE){
						
						int index = viewPagerCDrive.getCurrentItem();
	        			log("----------------------------------------INDEX: "+index);
	        			if(index==1){
	        				//Rubbish bin
	        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 1);		
	        				rbFLol = (RubbishBinFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
	        				if (rbFLol != null){						
								ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbFLol.getParentHandle()), orderGetChildren);
								rbFLol.setNodes(nodes);
								rbFLol.getListView().invalidate();
							}
	        			}
	        			else{
	        				//Cloud Drive
	        				String cFTag = getFragmentTag(R.id.cloud_drive_tabs_pager, 0);		
	        				fbFLol = (FileBrowserFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
	        				if (fbFLol != null){						
								ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
								fbFLol.setNodes(nodes);
								fbFLol.getListView().invalidate();
							}
	        			}		
					}										
					else if (drawerItem == DrawerItem.INBOX){
						if (iFLol != null){
							iFLol.getListView().invalidate();
						}
					}
				}
				else{
					if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
						log("OVERQUOTA ERROR: "+e.getErrorCode());
						showOverquotaAlert();
					}
					else
					{
						Toast.makeText(this, getString(R.string.context_no_copied), Toast.LENGTH_LONG).show();
					}
				}			
			}			
		}
		else if (request.getType() == MegaRequest.TYPE_CREATE_FOLDER){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				Toast.makeText(this, getString(R.string.context_folder_created), Toast.LENGTH_LONG).show();
				if (fbFLol != null){
					if (drawerItem == DrawerItem.CLOUD_DRIVE){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getListView().invalidate();
					}
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER){
			boolean avatarExists = false;
			if (e.getErrorCode() == MegaError.API_OK){
				
				File avatar = null;
				if (getExternalCacheDir() != null){
					avatar = new File(getExternalCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
				}
				else{
					avatar = new File(getCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
				}
				Bitmap imBitmap = null;
				if (avatar.exists()){
					if (avatar.length() > 0){
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);
						int imageHeight = options.outHeight;
						int imageWidth = options.outWidth;
						String imageType = options.outMimeType;
						
						// Calculate inSampleSize
					    options.inSampleSize = calculateInSampleSize(options, 250, 250);
					    
					    // Decode bitmap with inSampleSize set
					    options.inJustDecodeBounds = false;

						imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);
						if (imBitmap == null) {
							avatar.delete();
						}
						else{
							avatarExists = true;
							Bitmap circleBitmap = Bitmap.createBitmap(imBitmap.getWidth(), imBitmap.getHeight(), Bitmap.Config.ARGB_8888);
							
							BitmapShader shader = new BitmapShader (imBitmap,  TileMode.CLAMP, TileMode.CLAMP);
					        Paint paint = new Paint();
					        paint.setShader(shader);
					
					        Canvas c = new Canvas(circleBitmap);
					        int radius; 
					        if (imBitmap.getWidth() < imBitmap.getHeight())
					        	radius = imBitmap.getWidth()/2;
					        else
					        	radius = imBitmap.getHeight()/2;
					        
						    c.drawCircle(imBitmap.getWidth()/2, imBitmap.getHeight()/2, radius, paint);
					        imageProfile.setImageBitmap(circleBitmap);
					        textViewProfile.setVisibility(View.GONE);
						}
					}
				}
			}
			
			log("avatar user downloaded");
		}
		else if (request.getType() == MegaRequest.TYPE_ADD_CONTACT){
			
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				Toast.makeText(this, getString(R.string.context_contact_added), Toast.LENGTH_LONG).show();
//				String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
//				cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
//				if (cFLol != null){
//					if (drawerItem == DrawerItem.CONTACTS){	
//						ArrayList<MegaUser> contacts = megaApi.getContacts();
//						cFLol.setContacts(contacts);
//						cFLol.getListView().invalidateViews();
//					}
//				}
			}
			log("add contact");
		}
		else if (request.getType() == MegaRequest.TYPE_PAUSE_TRANSFERS){
			if (e.getErrorCode() == MegaError.API_OK) {
//				if (tF != null){
//					if (drawerItem == DrawerItem.TRANSFERS){
//						if (!downloadPlay){
//		    				pauseRestartTransfersItem.setTitle(getResources().getString(R.string.menu_restart_transfers));
//							tF.setPause(true);
//						}
//						else{
//		    				pauseRestartTransfersItem.setTitle(getResources().getString(R.string.menu_pause_transfers));
//							tF.setPause(false);
//						}		
//					}
//				}				
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CANCEL_TRANSFER){
			if (e.getErrorCode() == MegaError.API_OK){
//				if (tF != null){
//					if (drawerItem == DrawerItem.TRANSFERS){
//						Intent cancelOneIntent = new Intent(this, DownloadService.class);
//						cancelOneIntent.setAction(DownloadService.ACTION_CANCEL_ONE_DOWNLOAD);				
//						startService(cancelOneIntent);
//						tF.setTransfers(megaApi.getTransfers());
//					}
//				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_SHARE){
			try {				
				statusDialog.dismiss();	
				log("Dismiss");
			} 
			catch (Exception ex) {log("Exception");}
			if (e.getErrorCode() == MegaError.API_OK){
				log("OK MegaRequest.TYPE_SHARE");				
			}
			else{
				log("ERROR MegaRequest.TYPE_SHARE");
			}
		}
		else if (request.getType() == MegaRequest.TYPE_SUBMIT_PURCHASE_RECEIPT){
			if (e.getErrorCode() == MegaError.API_OK){
//				Toast.makeText(this, "PURCHASE CORRECT!", Toast.LENGTH_LONG).show();
				drawerItem = DrawerItem.CLOUD_DRIVE;
				selectDrawerItemLollipop(drawerItem);
			}
			else{
				Toast.makeText(this, "PURCHASE WRONG: " + e.getErrorString() + " (" + e.getErrorCode() + ")", Toast.LENGTH_LONG).show();
			}
		}
	}
	

	private void showOverquotaAlert(){
		
		dbH.setCamSyncEnabled(false);
		
		if(overquotaDialog==null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.overquota_alert_title));
			LayoutInflater inflater = getLayoutInflater();
			View dialoglayout = inflater.inflate(R.layout.dialog_overquota_error, null);
			TextView textOverquota = (TextView) dialoglayout.findViewById(R.id.dialog_overquota);
			builder.setView(dialoglayout);
			
			builder.setPositiveButton(getString(R.string.my_account_upgrade_pro), new android.content.DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Show UpgradeAccountActivity
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					if(upAF==null){
						upAF = new UpgradeAccountFragment();
						ft.replace(R.id.fragment_container, upAF, "upAF");
						drawerItem = DrawerItem.ACCOUNT;
						accountFragment=OVERQUOTA_ALERT;
						ft.commit();
					}
					else{			
						ft.replace(R.id.fragment_container, upAF, "upAF");
						drawerItem = DrawerItem.ACCOUNT;
						accountFragment=OVERQUOTA_ALERT;
						ft.commit();
					}
				}
			});
			builder.setNegativeButton(getString(R.string.general_cancel), new android.content.DialogInterface.OnClickListener() {			

				@Override
				public void onClick(DialogInterface dialog, int which) {						
					dialog.dismiss();	
					overquotaDialog=null;
				}
			});
			
			overquotaDialog = builder.create();
			overquotaDialog.show();
			Util.brandAlertDialog(overquotaDialog);
		}	
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
	
	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,MegaError e) {
		log("onRequestTemporaryError: "  + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_LOGOUT){
			log("logout temporary error");
		}	
		else if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
			log("fetchnodes temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			log("move temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE){
			log("remove temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_EXPORT){
			log("export temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_RENAME){
			log("rename temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_COPY){
			log("copy temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_CREATE_FOLDER){
			log("create folder temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER){
			log("get user attribute temporary error");
		}
		else if (request.getType() == MegaRequest.TYPE_ADD_CONTACT){
			log("add contact temporary error");
		}
	}
	
	public ActionBarDrawerToggle getmDrawerToggle() {
		log("getmDrawerToggle");
		return mDrawerToggle;
	}

	public void setmDrawerToggle(ActionBarDrawerToggle mDrawerToggle) {
		log("setmDrawerToggle");
		this.mDrawerToggle = mDrawerToggle;
	}
	
	File destination;
	
	public void onFileClick(ArrayList<Long> handleList){
		log("onFileClick: "+handleList.size()+" files to download");
		long size = 0;
		long[] hashes = new long[handleList.size()];
		for (int i=0;i<handleList.size();i++){
			hashes[i] = handleList.get(i);
			size += megaApi.getNodeByHandle(hashes[i]).getSize();
		}
		
		if (dbH == null){
//			dbH = new DatabaseHandler(getApplicationContext());
			dbH = DatabaseHandler.getDbHandler(getApplicationContext());
		}
		
		boolean askMe = true;
		boolean advancedDevices=false;
		String downloadLocationDefaultPath = Util.downloadDIR;
		prefs = dbH.getPreferences();		
		if (prefs != null){
			log("prefs != null");
			if (prefs.getStorageAskAlways() != null){				
				if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
					log("askMe==false");
					if (prefs.getStorageDownloadLocation() != null){
						if (prefs.getStorageDownloadLocation().compareTo("") != 0){
							askMe = false;
							downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
						}
					}
				}
				else
				{
					log("askMe==true");
					//askMe=true
					if (prefs.getStorageAdvancedDevices() != null){
						advancedDevices = Boolean.parseBoolean(prefs.getStorageAdvancedDevices());						
					}
					
				}
			}
		}		
			
		if (askMe){
			log("askMe");
			if(advancedDevices){
				log("advancedDevices");
				//Launch Intent to SAF
				if(hashes.length==1){
					downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
					this.openAdvancedDevices(hashes[0]);
				}
				else
				{
					//Show error message, just one file
					Toast.makeText(this, getString(R.string.context_select_one_file), Toast.LENGTH_LONG).show();
				}		    	
			}
			else{
				log("NOT advancedDevices");
				Intent intent = new Intent(Mode.PICK_FOLDER.getAction());
				intent.putExtra(FileStorageActivity.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
				intent.putExtra(FileStorageActivity.EXTRA_SIZE, size);
				intent.setClass(this, FileStorageActivity.class);
				intent.putExtra(FileStorageActivity.EXTRA_DOCUMENT_HASHES, hashes);
				startActivityForResult(intent, REQUEST_CODE_SELECT_LOCAL_FOLDER);
			}				
		}
		else{
			log("NOT askMe");
			File defaultPathF = new File(downloadLocationDefaultPath);
			defaultPathF.mkdirs();
			downloadTo(downloadLocationDefaultPath, null, size, hashes);
		}		
	}
	
	public void moveToTrash(final ArrayList<Long> handleList){
		log("moveToTrash");
		isClearRubbishBin = false;
		
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}		
		
		if(isFinishing()){
			return;	
		}
		
		final MegaNode rubbishNode = megaApi.getRubbishNode();
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	//TODO remove the outgoing shares
		        	
		        	for (int i=0;i<handleList.size();i++){
		    			//Check if the node is not yet in the rubbish bin (if so, remove it)
		    			MegaNode parent = megaApi.getNodeByHandle(handleList.get(i));
		    			while (megaApi.getParentNode(parent) != null){
		    				parent = megaApi.getParentNode(parent);
		    			}
		    				
		    			if (parent.getHandle() != megaApi.getRubbishNode().getHandle()){
		    				moveToRubbish = true;
		    				megaApi.moveNode(megaApi.getNodeByHandle(handleList.get(i)), rubbishNode, managerActivity);
		    			}
		    			else{
		    				megaApi.remove(megaApi.getNodeByHandle(handleList.get(i)), managerActivity);
		    			}
		    		}
		    		
		    		if (moveToRubbish){
		    			ProgressDialog temp = null;
		    			try{
		    				temp = new ProgressDialog(managerActivity);
		    				temp.setMessage(getString(R.string.context_move_to_trash));
		    				temp.show();
		    			}
		    			catch(Exception e){
		    				return;
		    			}
		    			statusDialog = temp;
		    		}
		    		else{
		    			ProgressDialog temp = null;
		    			try{
		    				temp = new ProgressDialog(managerActivity);
		    				temp.setMessage(getString(R.string.context_delete_from_mega));
		    				temp.show();
		    			}
		    			catch(Exception e){
		    				return;
		    			}
		    			statusDialog = temp;
		    		}
		        	
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		if (handleList.size() > 0){
			MegaNode p = megaApi.getNodeByHandle(handleList.get(0));
			while (megaApi.getParentNode(p) != null){
				p = megaApi.getParentNode(p);
			}
			if (p.getHandle() != megaApi.getRubbishNode().getHandle()){
				AlertDialog.Builder builder = new AlertDialog.Builder(managerActivity);
				String message= getResources().getString(R.string.confirmation_move_to_rubbish);
				builder.setMessage(message).setPositiveButton(R.string.general_yes, dialogClickListener)
			    	.setNegativeButton(R.string.general_no, dialogClickListener).show();
			}
			else{
				AlertDialog.Builder builder = new AlertDialog.Builder(managerActivity);
				String message= getResources().getString(R.string.confirmation_delete_from_mega);
				builder.setMessage(message).setPositiveButton(R.string.general_yes, dialogClickListener)
			    	.setNegativeButton(R.string.general_no, dialogClickListener).show();
			}
		}
		
	}
	
	public void getPublicLinkAndShareIt(MegaNode document){
		log("getPublicLinkAndShareIt");
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(this);
			temp.setMessage(getString(R.string.context_creating_link));
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;
		
		isGetLink = true;
		megaApi.exportNode(document, this);
	}
	
	/*
	 * Display keyboard
	 */
	private void showKeyboardDelayed(final View view) {
		log("showKeyboardDelayed");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (fbFLol != null){
					if (!(drawerItem == DrawerItem.CLOUD_DRIVE)){
						return;
					}
				}
				String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
				cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (cFLol != null){
					if (drawerItem == DrawerItem.CONTACTS){
						return;
					}
				}
				if (inSFLol != null){
					if (drawerItem == DrawerItem.SHARED_WITH_ME){
						return;
					}
				}
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 50);
	}

	public void showClearRubbishBinDialog(String editText){
		log("showClearRubbishBinDialog");

		if (rbFLol.isVisible()){
			rbFLol.setPositionClicked(-1);
			rbFLol.notifyDataSetChanged();
		}
		
		String text;
		if ((editText == null) || editText.equals("")){
			text = getString(R.string.context_clear_rubbish);
		}
		else{
			text = editText;
		}
		
		AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.context_clear_rubbish), null, null);
		builder.setPositiveButton(getString(R.string.general_empty),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						clearRubbishBin();
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		clearRubbishBinDialog = builder.create();
		clearRubbishBinDialog.show();
		Util.brandAlertDialog(clearRubbishBinDialog);
	}
	
	private String getFragmentTag(int viewPagerId, int fragmentPosition)
	{
	     return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
	}
	
	public void showNewContactDialog(String editText){
		log("showNewContactDialog");
		
		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cFLol != null){
			if (drawerItem == DrawerItem.CONTACTS){
				cFLol.setPositionClicked(-1);
				cFLol.notifyDataSetChanged();
			}
		}
		
		String text;
		if ((editText == null) || editText.equals("")){
			text = getString(R.string.context_new_contact_name);
		}
		else{
			text = editText;
		}
		
		final EditText input = new EditText(this);
		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setSelectAllOnFocus(true);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						return true;
					}
					addContact(value);
					addContactDialog.dismiss();
					return true;
				}
				return false;
			}
		});
		input.setImeActionLabel(getString(R.string.general_add),
				KeyEvent.KEYCODE_ENTER);
		input.setText(text);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showKeyboardDelayed(v);
				}
			}
		});
		AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.menu_add_contact),
				null, input);
		builder.setPositiveButton(getString(R.string.general_add),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString().trim();
						if (value.length() == 0) {
							return;
						}
						inviteContact(value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		addContactDialog = builder.create();
		addContactDialog.show();
	}
	
	public void pickFolderToShare(List<MegaUser> users){
		
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_SELECT_FOLDER);
		String[] longArray = new String[users.size()];
		for (int i=0; i<users.size(); i++){
			longArray[i] = users.get(i).getEmail();
		}
		intent.putExtra("SELECTED_CONTACTS", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_FOLDER);
		
	}
	
	public void showNewFolderDialog(String editText){
		log("showNewFolderDialogKitLollipop");
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			fbFLol.setPositionClicked(-1);
			fbFLol.notifyDataSetChanged();
		}
		
		String text;
		if (editText == null || editText.equals("")){
			text = getString(R.string.context_new_folder_name);
		}
		else{
			text = editText;
		}
		
		final EditText input = new EditText(this);
		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setSelectAllOnFocus(true);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						return true;
					}
					createFolder(value);
					newFolderDialog.dismiss();
					return true;
				}
				return false;
			}
		});
		input.setImeActionLabel(getString(R.string.general_create),
				KeyEvent.KEYCODE_ENTER);
		input.setText(text);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showKeyboardDelayed(v);
				}
			}
		});
		AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.menu_new_folder),
				null, input);
		builder.setPositiveButton(getString(R.string.general_create),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString().trim();
						if (value.length() == 0) {
							return;
						}
						createFolder(value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		newFolderDialog = builder.create();
		newFolderDialog.show();
	}

	private void clearRubbishBin(){
		log("clearRubbishBin");
		ClearRubbisBinTask clearRubbishBinTask = new ClearRubbisBinTask(this);
		clearRubbishBinTask.execute();
	}
	
	public void addContact(String contactEmail){
		log("addContact");
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		statusDialog = null;
		try {
			statusDialog = new ProgressDialog(this);
			statusDialog.setMessage(getString(R.string.context_adding_contact));
			statusDialog.show();
		}
		catch(Exception e){
			return;
		}

		megaApi.addContact(contactEmail, this);
	}
	
	public void inviteContact(String contactEmail){
		log("inviteContact");
		
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		statusDialog = null;
		try {
			statusDialog = new ProgressDialog(this);
			statusDialog.setMessage(getString(R.string.context_adding_contact));
			statusDialog.show();
		}
		catch(Exception e){
			return;
		}		

		megaApi.inviteContact(contactEmail, null, MegaContactRequest.INVITE_ACTION_ADD, this);
	}	
	
	private void createFolder(String title) {
		log("createFolder");
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		long parentHandle;
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			parentHandle = fbFLol.getParentHandle();
		}
		else{
			return;
		}
		
		MegaNode parentNode = megaApi.getNodeByHandle(parentHandle);
		
		if (parentNode == null){
			parentNode = megaApi.getRootNode();
		}
		
		boolean exists = false;
		ArrayList<MegaNode> nL = megaApi.getChildren(parentNode);
		for (int i=0;i<nL.size();i++){
			if (title.compareTo(nL.get(i).getName()) == 0){
				exists = true;
			}
		}
		
		if (!exists){
			statusDialog = null;
			try {
				statusDialog = new ProgressDialog(this);
				statusDialog.setMessage(getString(R.string.context_creating_folder));
				statusDialog.show();
			}
			catch(Exception e){
				return;
			}
			
			megaApi.createFolder(title, parentNode, this);
		}
		else{
			Toast.makeText(this, getString(R.string.context_folder_already_exists), Toast.LENGTH_LONG).show();
		}		
	}
	
	public void showRenameDialog(final MegaNode document, String text){
		log("showRenameDialog");
		final EditTextCursorWatcher input = new EditTextCursorWatcher(this);
		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);

		input.setImeActionLabel(getString(R.string.context_rename),
				KeyEvent.KEYCODE_ENTER);
		input.setText(text);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, boolean hasFocus) {
				if (hasFocus) {
					if (document.isFolder()){
						input.setSelection(0, input.getText().length());
					}
					else{
						String [] s = document.getName().split("\\.");
						if (s != null){
							int numParts = s.length;
							int lastSelectedPos = 0;
							if (numParts == 1){
								input.setSelection(0, input.getText().length());
							}
							else if (numParts > 1){
								for (int i=0; i<(numParts-1);i++){
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

		AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.context_rename) + " "	+ new String(document.getName()), null, input);
		builder.setPositiveButton(getString(R.string.context_rename),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString().trim();
						if (value.length() == 0) {
							return;
						}
						rename(document, value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		renameDialog = builder.create();
		renameDialog.show();

		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					renameDialog.dismiss();
					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						return true;
					}
					rename(document, value);
					return true;
				}
				return false;
			}
		});
	}
	
	public void showImportLinkDialog(){
		log("showRenameDialog");
		final EditText input = new EditText(this);
		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);

		input.setImeActionLabel(getString(R.string.context_open_link_title),KeyEvent.KEYCODE_ENTER);
		AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.context_open_link_title), null, input);
		builder.setPositiveButton(getString(R.string.context_open_link),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString().trim();
						if (value.length() == 0) {
							return;
						}
						
						try{
							openLinkDialog.dismiss();
						}
						catch(Exception e){}
						importLink(value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		openLinkDialog = builder.create();
		openLinkDialog.show();
		Util.brandAlertDialog(openLinkDialog);
		
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					try{
						openLinkDialog.dismiss();
					}
					catch(Exception e){}
					
					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						return true;
					}
					importLink(value);
					return true;
				}
				return false;
			}
		});
	}
	
	private void rename(MegaNode document, String newName){
		log("rename");
		if (newName.equals(document.getName())) {
			return;
		}
		
		if(!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
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
		
		log("renaming " + document.getName() + " to " + newName);
		
		megaApi.renameNode(document, newName, this);
	}
	
	public void leaveMultipleShares (ArrayList<Long> handleList){
	
		for (int i=0; i<handleList.size(); i++){
			MegaNode node = megaApi.getNodeByHandle(handleList.get(i));
			this.leaveIncomingShare(node);
		}
	}
	
	public void showMoveLollipop(ArrayList<Long> handleList){
		log("showMoveLollipop");
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_MOVE_FOLDER);
		long[] longArray = new long[handleList.size()];
		for (int i=0; i<handleList.size(); i++){
			longArray[i] = handleList.get(i);
		}
		intent.putExtra("MOVE_FROM", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_MOVE_FOLDER);
	}
	
	public void showCopyLollipop(ArrayList<Long> handleList){
		log("showCopyLollipop");
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_PICK_COPY_FOLDER);
		long[] longArray = new long[handleList.size()];
		for (int i=0; i<handleList.size(); i++){
			longArray[i] = handleList.get(i);
		}
		intent.putExtra("COPY_FROM", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_COPY_FOLDER);
	}
	
	/*
	 * If there is an application that can manage the Intent, returns true. Otherwise, false.
	 */
	public static boolean isIntentAvailable(Context ctx, Intent intent) {
		log("isIntentAvailable");
		final PackageManager mgr = ctx.getPackageManager();
		List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	/*
	 * Background task to get files on a folder for uploading
	 */
	private class UploadServiceTask extends Thread {

		String folderPath;
		ArrayList<String> paths;
		long parentHandle;
		
		UploadServiceTask(String folderPath, ArrayList<String> paths, long parentHandle){
			this.folderPath = folderPath;
			this.paths = paths;
			this.parentHandle = parentHandle;
		}
		
		@Override
		public void run(){
			
			MegaNode parentNode = megaApi.getNodeByHandle(parentHandle);
			if (parentNode == null){
				parentNode = megaApi.getRootNode();
			}
			
			for (String path : paths) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				Intent uploadServiceIntent;
				if(managerActivity != null)
				{
					uploadServiceIntent = new Intent (managerActivity, UploadService.class);
				}
				else
				{
					uploadServiceIntent = new Intent (ManagerActivityLollipop.this, UploadService.class);
				}
				
				File file = new File (path);
				if (file.isDirectory()){
					uploadServiceIntent.putExtra(UploadService.EXTRA_FILEPATH, file.getAbsolutePath());
					uploadServiceIntent.putExtra(UploadService.EXTRA_NAME, file.getName());
					log("EXTRA_FILE_PATH_dir:" + file.getAbsolutePath());
				}
				else{
					ShareInfo info = ShareInfo.infoFromFile(file);
					if (info == null){
						continue;
					}
					uploadServiceIntent.putExtra(UploadService.EXTRA_FILEPATH, info.getFileAbsolutePath());
					uploadServiceIntent.putExtra(UploadService.EXTRA_NAME, info.getTitle());
					uploadServiceIntent.putExtra(UploadService.EXTRA_SIZE, info.getSize());
					log("EXTRA_FILE_PATH_file:" + info.getFileAbsolutePath());
				}
				
				log("EXTRA_FOLDER_PATH:" + folderPath);
				uploadServiceIntent.putExtra(UploadService.EXTRA_FOLDERPATH, folderPath);
				uploadServiceIntent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
				startService(uploadServiceIntent);				
			}
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		log("onActivityResult "+requestCode + "____" + resultCode);
		
		if (requestCode == REQUEST_CODE_GET){
			log("resultCode = " + resultCode);
			if (intent == null){
				log("INTENT NULL");
			}
			else{
				log("URI: " + intent.getData());
			}
		}
		
		if (requestCode == REQUEST_CODE_GET && resultCode == RESULT_OK) {
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			Uri uri = intent.getData();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			FilePrepareTask filePrepareTask = new FilePrepareTask(this);
			filePrepareTask.execute(intent);
			ProgressDialog temp = null;
			try{
				temp = new ProgressDialog(this);
				temp.setMessage(getString(R.string.upload_prepare));
				temp.show();
			}
			catch(Exception e){
				return;
			}
			statusDialog = temp;
		}
		else if (requestCode == WRITE_SD_CARD_REQUEST_CODE && resultCode == RESULT_OK) {
			
			Uri treeUri = intent.getData();
			log("--------------Create the document : "+treeUri);
			
			//Now, call to the DownloadService 
			
			if(handleToDownload!=0 && handleToDownload!=-1){
				Intent service = new Intent(this, DownloadService.class);
				service.putExtra(DownloadService.EXTRA_HASH, handleToDownload);
				service.putExtra(DownloadService.EXTRA_CONTENT_URI, treeUri.toString());
				String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.advancesDevicesDIR + "/";
				File tempDownDirectory = new File(path);
				if(!tempDownDirectory.exists()){
					tempDownDirectory.mkdirs();
				}				
				service.putExtra(DownloadService.EXTRA_PATH, path);				
				startService(service);				
			}			
		}
		else if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
			log("requestCode == REQUEST_CODE_SELECT_FILE");
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final String[] selectedContacts = intent.getStringArrayExtra("SELECTED_CONTACTS");
			final long fileHandle = intent.getLongExtra("SELECT", 0);	
			
			MegaNode node = megaApi.getNodeByHandle(fileHandle);
			if(node!=null)
			{
				sendToInbox=true;
				log("File to send: "+node.getName());
				for (int i=0;i<selectedContacts.length;i++){
            		MegaUser user= megaApi.getContact(selectedContacts[i]);		                    		
            		megaApi.sendFileToUser(node, user, this);
            	}
			}			
		}	
		else if (requestCode == REQUEST_CODE_SELECT_FOLDER && resultCode == RESULT_OK) {
			
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final String[] selectedContacts = intent.getStringArrayExtra("SELECTED_CONTACTS");
			final long folderHandle = intent.getLongExtra("SELECT", 0);			
			
			final MegaNode parent = megaApi.getNodeByHandle(folderHandle);
			
			if (parent.isFolder()){
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
				dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
				final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
				dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						ProgressDialog temp = null;
						try{
							temp = new ProgressDialog(managerActivity);
							temp.setMessage(getString(R.string.context_sharing_folder));
							temp.show();
						}
						catch(Exception e){
							return;
						}
						statusDialog = temp;
						permissionsDialog.dismiss();						
						
						switch(item) {
						    case 0:{
		                    	for (int i=0;i<selectedContacts.length;i++){
		                    		MegaUser user= megaApi.getContact(selectedContacts[i]);		                    		
		                    		megaApi.share(parent, user, MegaShare.ACCESS_READ,managerActivity);
		                    	}
		                    	break;
		                    }
		                    case 1:{	                    	
		                    	for (int i=0;i<selectedContacts.length;i++){
		                    		MegaUser user= megaApi.getContact(selectedContacts[i]);		                    		
		                    		megaApi.share(parent, user, MegaShare.ACCESS_READWRITE,managerActivity);
		                    	}
		                        break;
		                    }
		                    case 2:{                   	
		                    	for (int i=0;i<selectedContacts.length;i++){
		                    		MegaUser user= megaApi.getContact(selectedContacts[i]);		                    		
		                    		megaApi.share(parent, user, MegaShare.ACCESS_FULL,managerActivity);
		                    	}
		                        break;
		                    }
		                }
					}
				});
				permissionsDialog = dialogBuilder.create();
				permissionsDialog.show();
				Resources resources = permissionsDialog.getContext().getResources();
				int alertTitleId = resources.getIdentifier("alertTitle", "id", "android");
				TextView alertTitle = (TextView) permissionsDialog.getWindow().getDecorView().findViewById(alertTitleId);
		        alertTitle.setTextColor(resources.getColor(R.color.mega));
				int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
				View titleDivider = permissionsDialog.getWindow().getDecorView().findViewById(titleDividerId);
				if(titleDivider!=null){
					titleDivider.setBackgroundColor(resources.getColor(R.color.mega));
				}
			}
		}	
		else if (requestCode == REQUEST_CODE_SELECT_CONTACT && resultCode == RESULT_OK){
			
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}

			contactsData = intent.getStringArrayListExtra(ContactsExplorerActivityLollipop.EXTRA_CONTACTS);			
			megaContacts = intent.getBooleanExtra(ContactsExplorerActivityLollipop.EXTRA_MEGA_CONTACTS, true);
			
			final int multiselectIntent = intent.getIntExtra("MULTISELECT", -1);
			final int sentToInbox = intent.getIntExtra("SEND_FILE", -1);
			
			if (megaContacts){
				
				if(sentToInbox==0){
					//Just one folder to share
					if(multiselectIntent==0){
	
						final long nodeHandle = intent.getLongExtra(ContactsExplorerActivity.EXTRA_NODE_HANDLE, -1);
						final MegaNode node = megaApi.getNodeByHandle(nodeHandle);
						
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
						dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
						final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
						dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								ProgressDialog temp = null;
								try{
									temp = new ProgressDialog(managerActivity);
									temp.setMessage(getString(R.string.context_sharing_folder));
									temp.show();
								}
								catch(Exception e){
									return;
								}
								statusDialog = temp;
								permissionsDialog.dismiss();
								
								switch(item) {
				                    case 0:{
				                    	for (int i=0;i<contactsData.size();i++){
				                    		MegaUser u = megaApi.getContact(contactsData.get(i));		
				                    		log("Node: "+node.getName());
				                    		log("User: "+u.getEmail());
				                    		megaApi.share(node, u, MegaShare.ACCESS_READ, managerActivity);
				                    	}
				                    	break;
				                    }
				                    case 1:{
				                    	for (int i=0;i<contactsData.size();i++){
				                    		MegaUser u = megaApi.getContact(contactsData.get(i));
				                    		megaApi.share(node, u, MegaShare.ACCESS_READWRITE, managerActivity);
				                    	}
				                        break;
				                    }
				                    case 2:{
				                    	for (int i=0;i<contactsData.size();i++){
				                    		MegaUser u = megaApi.getContact(contactsData.get(i));
				                    		megaApi.share(node, u, MegaShare.ACCESS_FULL, managerActivity);
				                    	}		                    	
				                        break;
				                    }
				                }
							}
						});
						permissionsDialog = dialogBuilder.create();
						permissionsDialog.show();
						Resources resources = permissionsDialog.getContext().getResources();
						int alertTitleId = resources.getIdentifier("alertTitle", "id", "android");
						TextView alertTitle = (TextView) permissionsDialog.getWindow().getDecorView().findViewById(alertTitleId);
				        alertTitle.setTextColor(resources.getColor(R.color.mega));
						int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
						View titleDivider = permissionsDialog.getWindow().getDecorView().findViewById(titleDividerId);
						if(titleDivider!=null){
							titleDivider.setBackgroundColor(resources.getColor(R.color.mega));
						}						
					}
					else if(multiselectIntent==1){
						//Several folder to share
						final long[] nodeHandles = intent.getLongArrayExtra(ContactsExplorerActivity.EXTRA_NODE_HANDLE);
						
							
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
						dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
						final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
						dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								ProgressDialog temp = null;
								try{
									temp = new ProgressDialog(managerActivity);
									temp.setMessage(getString(R.string.context_sharing_folder));
									temp.show();
								}
								catch(Exception e){
									return;
								}
								statusDialog = temp;
								permissionsDialog.dismiss();
								
								switch(item) {
				                    case 0:{
				                    	for (int i=0;i<contactsData.size();i++){
				                    		MegaUser u = megaApi.getContact(contactsData.get(i));			                    		
				                    		for(int j=0; j<nodeHandles.length;j++){						
				            					
				        						final MegaNode node = megaApi.getNodeByHandle(nodeHandles[j]);
				        						megaApi.share(node, u, MegaShare.ACCESS_READ, managerActivity);
				                    		}
				                    	}
				                    	break;
				                    }
				                    case 1:{
				                    	for (int i=0;i<contactsData.size();i++){
				                    		MegaUser u = megaApi.getContact(contactsData.get(i));
				                    		for(int j=0; j<nodeHandles.length;j++){						
				            					
				        						final MegaNode node = megaApi.getNodeByHandle(nodeHandles[j]);
				        						megaApi.share(node, u, MegaShare.ACCESS_READWRITE, managerActivity);
				                    		}
	//			                    		megaApi.share(node, u, MegaShare.ACCESS_READWRITE, managerActivity);
				                    	}
				                        break;
				                    }
				                    case 2:{
				                    	for (int i=0;i<contactsData.size();i++){
				                    		MegaUser u = megaApi.getContact(contactsData.get(i));
				                    		for(int j=0; j<nodeHandles.length;j++){						
				            					
				        						final MegaNode node = megaApi.getNodeByHandle(nodeHandles[j]);
				        						megaApi.share(node, u, MegaShare.ACCESS_FULL, managerActivity);
				                    		}
	//			                    		megaApi.share(node, u, MegaShare.ACCESS_FULL, managerActivity);
				                    	}		                    	
				                        break;
				                    }
				                }
							}
						});
						permissionsDialog = dialogBuilder.create();
						permissionsDialog.show();
						Resources resources = permissionsDialog.getContext().getResources();
						int alertTitleId = resources.getIdentifier("alertTitle", "id", "android");
						TextView alertTitle = (TextView) permissionsDialog.getWindow().getDecorView().findViewById(alertTitleId);
				        alertTitle.setTextColor(resources.getColor(R.color.mega));
						int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
						View titleDivider = permissionsDialog.getWindow().getDecorView().findViewById(titleDividerId);
						if(titleDivider!=null){
							titleDivider.setBackgroundColor(resources.getColor(R.color.mega));
						}				
					}
				}
				else if (sentToInbox==1){
					//Send file
					final long nodeHandle = intent.getLongExtra(ContactsExplorerActivity.EXTRA_NODE_HANDLE, -1);
					final MegaNode node = megaApi.getNodeByHandle(nodeHandle);
					MegaUser u = megaApi.getContact(contactsData.get(0));
					megaApi.sendFileToUser(node, u, this);
				}

			}
			else{

				for (int i=0; i < contactsData.size();i++){
					String type = contactsData.get(i);
					if (type.compareTo(ContactsExplorerActivity.EXTRA_EMAIL) == 0){
						i++;
						Toast.makeText(this, getString(R.string.general_not_yet_implemented), Toast.LENGTH_LONG).show();
//						Toast.makeText(this, "Sharing a folder: An email will be sent to the email address: " + contactsData.get(i) + ".\n", Toast.LENGTH_LONG).show();
					}
					else if (type.compareTo(ContactsExplorerActivity.EXTRA_PHONE) == 0){
						i++;
						Toast.makeText(this, getString(R.string.general_not_yet_implemented), Toast.LENGTH_LONG).show();
//						Toast.makeText(this, "Sharing a folder: A Text Message will be sent to the phone number: " + contactsData.get(i) , Toast.LENGTH_LONG).show();
					}
				}

			}			
		}		
		else if (requestCode == REQUEST_CODE_GET_LOCAL && resultCode == RESULT_OK) {
			
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			String folderPath = intent.getStringExtra(FileStorageActivity.EXTRA_PATH);
			ArrayList<String> paths = intent.getStringArrayListExtra(FileStorageActivity.EXTRA_FILES);
			
			int i = 0;
			long parentHandleUpload=-1;
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				if(fbFLol!=null)
				{
					parentHandleUpload = fbFLol.getParentHandle();
				}	
			}
			else if(drawerItem == DrawerItem.SHARED_WITH_ME){
				int index = viewPagerShares.getCurrentItem();
				if(index==0){	
					//INCOMING
					String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
//					log("Tag: "+ cFTag1);
					inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
					if (inSFLol != null){		
						parentHandleUpload=inSFLol.getParentHandle();
					}					
				}
				else if(index==1){
					//OUTGOING
					String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 1);	
//					log("Tag: "+ cFTag1);
					outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
					if (outSFLol != null){		
						parentHandleUpload=outSFLol.getParentHandle();
					}	
				}
			}
			else{
				return;
			}
			
			UploadServiceTask uploadServiceTask = new UploadServiceTask(folderPath, paths, parentHandleUpload);
			uploadServiceTask.start();			
		}
		else if (requestCode == REQUEST_CODE_SELECT_MOVE_FOLDER && resultCode == RESULT_OK) {
		
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final long[] moveHandles = intent.getLongArrayExtra("MOVE_HANDLES");
			final long toHandle = intent.getLongExtra("MOVE_TO", 0);
//			final int totalMoves = moveHandles.length;
			
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
		else if (requestCode == REQUEST_CODE_SELECT_COPY_FOLDER && resultCode == RESULT_OK){
			
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
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
				megaApi.copyNode(megaApi.getNodeByHandle(copyHandles[i]), parent, this);
			}
		}
		else if (requestCode == REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
			log("onActivityResult: REQUEST_CODE_SELECT_LOCAL_FOLDER");
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			String parentPath = intent.getStringExtra(FileStorageActivity.EXTRA_PATH);
			log("parentPath: "+parentPath);
			String url = intent.getStringExtra(FileStorageActivity.EXTRA_URL);
			log("url: "+url);
			long size = intent.getLongExtra(FileStorageActivity.EXTRA_SIZE, 0);
			log("size: "+size);
			long[] hashes = intent.getLongArrayExtra(FileStorageActivity.EXTRA_DOCUMENT_HASHES);
			log("hashes size: "+hashes.length);
			
			downloadTo (parentPath, url, size, hashes);
			Util.showToast(this, R.string.download_began);
		}
		else if (requestCode == REQUEST_CODE_REFRESH && resultCode == RESULT_OK) {
			
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				parentHandleBrowser = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
				if (parentNode != null){
					if (fbFLol != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getListView().invalidate();						
					}
				}
				else{
					if (fbFLol != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getListView().invalidate();						
					}
				}
			}
//			else if (drawerItem == DrawerItem.RUBBISH_BIN){
//				parentHandleRubbish = intent.getLongExtra("PARENT_HANDLE", -1);
//				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleRubbish);
//				if (parentNode != null){
//					if (rbFLol != null){						
//						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
//						rbFLol.setNodes(nodes);
//						rbFLol.getListView().invalidateViews();						
//					}
//				}
//				else{
//					if (rbFLol != null){						
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
//						rbFLol.setNodes(nodes);
//						rbFLol.getListView().invalidateViews();						
//					}
//				}
//			}
			else if (drawerItem == DrawerItem.SHARED_WITH_ME){
				parentHandleIncoming = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
				if (parentNode != null){
					if (inSFLol != null){					
//						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						//TODO: ojo con los hijos
//							inSFLol.setNodes(nodes);
						inSFLol.getListView().invalidate();						
					}
				}
				else{
					if (inSFLol != null){						
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
						//TODO: ojo con los hijos
//							inSFLol.setNodes(nodes);
						inSFLol.getListView().invalidate();						
					}
				}
			}
		}
		else if (requestCode == TAKE_PHOTO_CODE){
			log("Entrooo en requestCode");
			if(resultCode == Activity.RESULT_OK){
				
				log("REcibo el intent OOOOKK");
				Intent intentPicture = new Intent(this, SecureSelfiePreviewActivity.class);			
				startActivity(intentPicture);
			}
			else{
				log("REcibo el intent con error");
			}			

	    }
		else if (requestCode == REQUEST_CODE_SORT_BY && resultCode == RESULT_OK){
			
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			orderGetChildren = intent.getIntExtra("ORDER_GET_CHILDREN", 1);
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
				if (parentNode != null){
					if (fbFLol != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						fbFLol.setOrder(orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getListView().invalidate();						
					}
				}
				else{
					if (fbFLol != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
						fbFLol.setOrder(orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getListView().invalidate();					
					}
				}
			}
//			else if (drawerItem == DrawerItem.RUBBISH_BIN){
//				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleRubbish);
//				if (parentNode != null){
//					if (rbFLol != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
//						rbFLol.setOrder(orderGetChildren);
//						rbFLol.setNodes(nodes);
//						rbFLol.getListView().invalidateViews();
//					}
//				}
//				else{
//					if (rbFLol != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
//						rbFLol.setOrder(orderGetChildren);
//						rbFLol.setNodes(nodes);
//						rbFLol.getListView().invalidateViews();						
//					}
//				}
//			}
			else if (drawerItem == DrawerItem.SHARED_WITH_ME){
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
				if (parentNode != null){
					if (inSFLol != null){
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						inSFLol.setOrder(orderGetChildren);
						//TODO: ojo con los hijos
//							inSFLol.setNodes(nodes);
						inSFLol.getListView().invalidate();
					}
				}
				else{
					if (inSFLol != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
						inSFLol.setOrder(orderGetChildren);
						//TODO: ojo con los hijos
//							inSFLol.setNodes(nodes);
						inSFLol.getListView().invalidate();
					}
				}
			}			
		}
		else if (requestCode == RC_REQUEST){
			// Pass on the activity result to the helper for handling
	        if (!mHelper.handleActivityResult(requestCode, resultCode, intent)) {
	            // not handled, so handle it ourselves (here's where you'd
	            // perform any handling of activity results not related to in-app
	            // billing...
	        	
	        	super.onActivityResult(requestCode, resultCode, intent);
	        }
	        else {
	            log("onActivityResult handled by IABUtil.");
	            drawerItem = DrawerItem.CLOUD_DRIVE;
//	            Toast.makeText(this, "HURRAY!: ORDERID: **__" + orderId + "__**", Toast.LENGTH_LONG).show();
	            log("HURRAY!: ORDERID: **__" + orderId + "__**");
	        }
		}
	}	
	
	/*
	 * Get list of all child files
	 */
	private void getDlList(Map<MegaNode, String> dlFiles, MegaNode parent, File folder) {
		log("getDlList");
		if (megaApi.getRootNode() == null)
			return;
		
		folder.mkdir();
		ArrayList<MegaNode> nodeList = megaApi.getChildren(parent, orderGetChildren);
		for(int i=0; i<nodeList.size(); i++){
			MegaNode document = nodeList.get(i);
			if (document.getType() == MegaNode.TYPE_FOLDER) {
				File subfolder = new File(folder, new String(document.getName()));
				getDlList(dlFiles, document, subfolder);
			} 
			else {
				dlFiles.put(document, folder.getAbsolutePath());
			}
		}
	}
	
	/*
	 * Background task to process files for uploading
	 */
	private class FilePrepareTask extends AsyncTask<Intent, Void, List<ShareInfo>> {
		Context context;
		
		FilePrepareTask(Context context){
			log("FilePrepareTask::FilePrepareTask");
			this.context = context;
		}
		
		@Override
		protected List<ShareInfo> doInBackground(Intent... params) {
			log("FilePrepareTask::doInBackGround");
			return ShareInfo.processIntent(params[0], context);
		}

		@Override
		protected void onPostExecute(List<ShareInfo> info) {
			log("FilePrepareTask::onPostExecute");
			filePreparedInfos = info;
			onIntentProcessed();
		}
	}
	
	/*
	 * Handle processed upload intent
	 */
	public void onIntentProcessed() {
		log("onIntentProcessedLollipop");
		List<ShareInfo> infos = filePreparedInfos;
		if (statusDialog != null) {
			try { 
				statusDialog.dismiss(); 
			} 
			catch(Exception ex){}
		}
		
		long parentHandle = -1;
		MegaNode parentNode = null;
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			parentHandle = fbFLol.getParentHandle();
			parentNode = megaApi.getNodeByHandle(parentHandle);
		}
		else if (drawerItem == DrawerItem.SHARED_WITH_ME){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){				
				//OUTGOING				
				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
				log("Tag: "+ cFTag2);
				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag2);
				if (outSFLol != null){					
					parentHandleOutgoing = outSFLol.getParentHandle();
					parentNode = megaApi.getNodeByHandle(parentHandleOutgoing);
				}
			}
			else{			
				//InCOMING
				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
				log("Tag: "+ cFTag1);
				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
				if (inSFLol != null){					
					parentHandleIncoming = inSFLol.getParentHandle();	
					parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
				}				
			}	
		}
		
		if(parentNode == null){
			Util.showErrorAlertDialog(getString(R.string.error_temporary_unavaible), false, this);
			return;
		}
			
		if (infos == null) {
			Util.showErrorAlertDialog(getString(R.string.upload_can_not_open),
					false, this);
		} 
		else {
			Toast.makeText(getApplicationContext(), getString(R.string.upload_began),
					Toast.LENGTH_SHORT).show();
			for (ShareInfo info : infos) {
				Intent intent = new Intent(this, UploadService.class);
				intent.putExtra(UploadService.EXTRA_FILEPATH, info.getFileAbsolutePath());
				intent.putExtra(UploadService.EXTRA_NAME, info.getTitle());
				intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
				intent.putExtra(UploadService.EXTRA_SIZE, info.getSize());
				startService(intent);
			}
		}
	}	

	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		log("onUsersUpdate");
		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cFLol != null){
			if (drawerItem == DrawerItem.CONTACTS){					
				cFLol.updateView();
			}
		}
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> updatedNodes) {
		log("onNodesUpdateLollipop");
		try { 
			statusDialog.dismiss();	
		} 
		catch (Exception ex) {}
		
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			if (fbFLol != null){
			
				if (fbFLol.isVisible()){
					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbFLol.getParentHandle()), orderGetChildren);
					fbFLol.setNodes(nodes);
					fbFLol.setContentText();
					fbFLol.getListView().invalidate();
				}
			}
			if (rbFLol != null){
				
				if (isClearRubbishBin){
					isClearRubbishBin = false;
					parentHandleRubbish = megaApi.getRubbishNode().getHandle();
					aB.setTitle(getString(R.string.section_rubbish_bin));	
					getmDrawerToggle().setDrawerIndicatorEnabled(true);

					if(rbFLol.isVisible())
					{
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
						rbFLol.setParentHandle(megaApi.getRubbishNode().getHandle());
						rbFLol.setNodes(nodes);
						rbFLol.getListView().invalidate();
					}
				}
				else{
					if(rbFLol.isVisible())
					{
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbFLol.getParentHandle()), orderGetChildren);
						rbFLol.setNodes(nodes);
						rbFLol.setContentText();
						rbFLol.getListView().invalidate();
					}
				}				
			}
		}
		if (drawerItem == DrawerItem.INBOX){
			log("INBOX shown");
			if (iFLol != null){
				iFLol.refresh();
//				iFLol.getListView().invalidateViews();
			}
		}		
		
		if (drawerItem == DrawerItem.SHARED_WITH_ME){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){				
				//OUTGOING				
				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
				log("Tag: "+ cFTag2);
				outSFLol = (OutgoingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag2);
				if (outSFLol != null){					
					aB.setTitle(getString(R.string.section_shared_items));				
					outSFLol.refresh(this.parentHandleOutgoing);				
				}
			}
			else{			
				//InCOMING
				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
				log("Tag: "+ cFTag1);
				inSFLol = (IncomingSharesFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag1);
				if (inSFLol != null){					
					aB.setTitle(getString(R.string.section_shared_items));	
					inSFLol.refresh(this.parentHandleIncoming);			
				}				
			}	
		}
		if (drawerItem == DrawerItem.CAMERA_UPLOADS){
			if (psF != null){			
				if(psF.isAdded()){
					long cameraUploadHandle = psF.getPhotoSyncHandle();
					MegaNode nps = megaApi.getNodeByHandle(cameraUploadHandle);
					log("cameraUploadHandle: " + cameraUploadHandle);
					if (nps != null){
						log("nps != null");
						ArrayList<MegaNode> nodes = megaApi.getChildren(nps, MegaApiJava.ORDER_MODIFICATION_DESC);
						psF.setNodes(nodes);
					}
				}				
			}
		}
		if (cFLol != null){
			if (drawerItem == DrawerItem.CONTACTS){
				log("Share finish");
				cFLol.updateView();
			}
		}
	}

	@Override
	public void onReloadNeeded(MegaApiJava api) {
		log("onReloadNeeded");
	}	
	
	public void setParentHandleBrowser(long parentHandleBrowser){
		log("setParentHandleBrowser");
		
		this.parentHandleBrowser = parentHandleBrowser;

		HashMap<Long, MegaTransfer> mTHash = new HashMap<Long, MegaTransfer>();

		//Update transfer list
		if (tF == null){
			tF = new TransfersFragment();
		}
		tL = megaApi.getTransfers();
		tF.setTransfers(tL);		

		//Update File Browser Fragment
		if (fbFLol != null){
			for(int i=0; i<tL.size(); i++){
				
				MegaTransfer tempT = tL.get(i);
				if (tempT.getType() == MegaTransfer.TYPE_DOWNLOAD){
					long handleT = tempT.getNodeHandle();
					MegaNode nodeT = megaApi.getNodeByHandle(handleT);
					MegaNode parentT = megaApi.getParentNode(nodeT);
					
					if (parentT != null){
						if(parentT.getHandle() == this.parentHandleBrowser){	
							mTHash.put(handleT,tempT);						
						}
					}
				}
			}
			
			fbFLol.setTransfers(mTHash);
		}
	}
	
	public void setParentHandleInbox(long parentHandleInbox){
		log("setParentHandleInbox");
		this.parentHandleInbox = parentHandleInbox;
	}
	
	public void setParentHandleRubbish(long parentHandleRubbish){
		log("setParentHandleRubbish");
		this.parentHandleRubbish = parentHandleRubbish;
	}
	
	public void setParentHandleIncoming(long parentHandleSharedWithMe){
		log("setParentHandleSharedWithMe");
		this.parentHandleIncoming = parentHandleSharedWithMe;
	}
	
	public void setParentHandleOutgoing(long parentHandleSharedWithMe){
		log("setParentHandleSharedWithMe");
		this.parentHandleOutgoing = parentHandleSharedWithMe;
	}
	
	public void setParentHandleSearch(long parentHandleSearch){
		log("setParentHandleSearch");
		this.parentHandleSearch = parentHandleSearch;
	}
	
	public void setPathNavigationOffline(String pathNavigation){
		this.pathNavigation = pathNavigation;
	}
	
	public void setPauseIconVisible(boolean visible){
		log("setPauseIconVisible");
		pauseIconVisible = visible;
		if (pauseRestartTransfersItem != null){
			pauseRestartTransfersItem.setVisible(visible);
		}
	}
	
	public void setTransfers(ArrayList<MegaTransfer> transfersList){
		log("setTransfers");
		if (tF != null){
			tF.setTransfers(transfersList);
		}
	}
	
	private void getOverflowMenu() {
		log("getOverflowMenu");
	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void setDownloadPlay(boolean downloadPlay){
		log("setDownloadPlay");
		this.downloadPlay = downloadPlay;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		log("onKeyUp");
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {
		log("onTransferStart");

		HashMap<Long, MegaTransfer> mTHash = new HashMap<Long, MegaTransfer>();

		//Update transfer list
		if (tF == null){
			tF = new TransfersFragment();
		}
		tL = megaApi.getTransfers();
		tF.setTransfers(tL);		

		//Update File Browser Fragment
		if (fbFLol != null){
			for(int i=0; i<tL.size(); i++){
				
				MegaTransfer tempT = tL.get(i);
				if (tempT.getType() == MegaTransfer.TYPE_DOWNLOAD){
					long handleT = tempT.getNodeHandle();
					MegaNode nodeT = megaApi.getNodeByHandle(handleT);
					MegaNode parentT = megaApi.getParentNode(nodeT);
					
					if (parentT != null){
						if(parentT.getHandle() == this.parentHandleBrowser){	
							mTHash.put(handleT,tempT);						
						}
					}
				}
			}
			
			fbFLol.setTransfers(mTHash);
		}
		
		if (inSFLol != null){
			for(int i=0; i<tL.size(); i++){
				
				MegaTransfer tempT = tL.get(i);
				if (tempT.getType() == MegaTransfer.TYPE_DOWNLOAD){
					long handleT = tempT.getNodeHandle();
					
					mTHash.put(handleT,tempT);						
				}
			}
			
			inSFLol.setTransfers(mTHash);
		}
		
		log("onTransferStart: " + transfer.getFileName() + " - " + transfer.getTag());

	}

	@Override
	public void onTransferFinish(MegaApiJava api, MegaTransfer transfer, MegaError e) {
		log("onTransferFinish"); 
		
//		ThumbnailUtils.pendingThumbnails.remove(transfer.getNodeHandle());
		
		
		HashMap<Long, MegaTransfer> mTHash = new HashMap<Long, MegaTransfer>();

		//Update transfer list
		if (tF == null){
			tF = new TransfersFragment();
		}		
		tL = megaApi.getTransfers();
		tF.setTransfers(tL);	
		
		//Update File Browser Fragment
		if (fbFLol != null){
			for(int i=0; i<tL.size(); i++){
				
				MegaTransfer tempT = tL.get(i);
				long handleT = tempT.getNodeHandle();
				MegaNode nodeT = megaApi.getNodeByHandle(handleT);
				MegaNode parentT = megaApi.getParentNode(nodeT);
				
				if (parentT != null){
					if(parentT.getHandle() == this.parentHandleBrowser){
						mTHash.put(handleT,tempT);
					}
				}			
			}
			fbFLol.setTransfers(mTHash);	
		}
		
		if (inSFLol != null){
			for(int i=0; i<tL.size(); i++){
				
				MegaTransfer tempT = tL.get(i);
				if (tempT.getType() == MegaTransfer.TYPE_DOWNLOAD){
					long handleT = tempT.getNodeHandle();
	
					mTHash.put(handleT,tempT);						
				}
			}
			
			inSFLol.setTransfers(mTHash);
		}

		log("onTransferFinish: " + transfer.getFileName() + " - " + transfer.getTag());
	}
	
	@Override
	public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {
		log("onTransferUpdate: " + transfer.getFileName() + " - " + transfer.getTag());

		//Update transfer list
		if (tF == null){
			tF = new TransfersFragment();
		}
		
//		if (drawerItem == DrawerItem.TRANSFERS){
//			Time now = new Time();
//			now.setToNow();
//			long nowMillis = now.toMillis(false);
//			if (lastTimeOnTransferUpdate < 0){
//				lastTimeOnTransferUpdate = now.toMillis(false);
//				tF.setCurrentTransfer(transfer);
//			}
//			else if ((nowMillis - lastTimeOnTransferUpdate) > Util.ONTRANSFERUPDATE_REFRESH_MILLIS){
//				lastTimeOnTransferUpdate = nowMillis;
//				tF.setCurrentTransfer(transfer);
//			}
//		}

		if (fbFLol != null){
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				if (transfer.getType() == MegaTransfer.TYPE_DOWNLOAD){
					Time now = new Time();
					now.setToNow();
					long nowMillis = now.toMillis(false);
					if (lastTimeOnTransferUpdate < 0){
						lastTimeOnTransferUpdate = now.toMillis(false);
						fbFLol.setCurrentTransfer(transfer);
					}
					else if ((nowMillis - lastTimeOnTransferUpdate) > Util.ONTRANSFERUPDATE_REFRESH_MILLIS){
						lastTimeOnTransferUpdate = nowMillis;
						fbFLol.setCurrentTransfer(transfer);
					}			
				}		
			}
		}
		
		if (inSFLol != null){
			if (drawerItem == DrawerItem.SHARED_WITH_ME){
				if (transfer.getType() == MegaTransfer.TYPE_DOWNLOAD){
					Time now = new Time();
					now.setToNow();
					long nowMillis = now.toMillis(false);
					if (lastTimeOnTransferUpdate < 0){
						lastTimeOnTransferUpdate = now.toMillis(false);
						inSFLol.setCurrentTransfer(transfer);
					}
					else if ((nowMillis - lastTimeOnTransferUpdate) > Util.ONTRANSFERUPDATE_REFRESH_MILLIS){
						lastTimeOnTransferUpdate = nowMillis;
						inSFLol.setCurrentTransfer(transfer);
					}			
				}		
			}
		}
	}

	@Override
	public void onTransferTemporaryError(MegaApiJava api,
			MegaTransfer transfer, MegaError e) {
		
		log("onTransferTemporaryError: " + transfer.getFileName() + " - " + transfer.getTag());
	}
	
	public static void log(String message) {
		Util.log("ManagerActivityLollipop", message);
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		log("onRequestUpdate: "  + request.getRequestString());		
	}
	
	public void downloadTo(String parentPath, String url, long size, long [] hashes){
		log("downloadTo, parentPath: "+parentPath+ "url: "+url+" size: "+size);
		log("files to download: ");
		if (hashes != null){
			for (long hash : hashes) {
				MegaNode node = megaApi.getNodeByHandle(hash);
				log("Node: "+ node.getName());
			}
		}
		
		double availableFreeSpace = Double.MAX_VALUE;
		try{
			StatFs stat = new StatFs(parentPath);
			availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
		}
		catch(Exception ex){}		
		
		if (hashes == null){
			log("hashes is null");
			if(url != null) {
				log("url NOT null");
				if(availableFreeSpace < size) {
					Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space), false, this);
					log("Not enough space");
					return;
				}
				
				Intent service = new Intent(this, DownloadService.class);
				service.putExtra(DownloadService.EXTRA_URL, url);
				service.putExtra(DownloadService.EXTRA_SIZE, size);
				service.putExtra(DownloadService.EXTRA_PATH, parentPath);
				startService(service);
			}
		}
		else{
			log("hashes is NOT null");
			if(hashes.length == 1){
				log("hashes.length == 1");
				MegaNode tempNode = megaApi.getNodeByHandle(hashes[0]);
				if((tempNode != null) && tempNode.getType() == MegaNode.TYPE_FILE){
					log("ISFILE");
					String localPath = Util.getLocalFile(this, tempNode.getName(), tempNode.getSize(), parentPath);
					
					if(localPath != null){
						log("localPath != null");
						try { 
							log("Call to copyFile");
							Util.copyFile(new File(localPath), new File(parentPath, tempNode.getName())); 
						}
						catch(Exception e) {
							log("Exception!!");
						}
												
//						if(MimeType.typeForName(tempNode.getName()).isPdf()){
//							
//		    			    File pdfFile = new File(localPath);
//		    			    
//		    			    Intent intentPdf = new Intent();
//		    			    intentPdf.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
//		    			    intentPdf.setClass(this, OpenPDFActivity.class);
//		    			    intentPdf.setAction("android.intent.action.VIEW");
//		    				this.startActivity(intentPdf);
//							
//						}
//						else						
						
						if(MimeTypeList.typeForName(tempNode.getName()).isZip()){
							log("MimeTypeList ZIP");
		    			    File zipFile = new File(localPath);
		    			    
		    			    Intent intentZip = new Intent();
		    			    intentZip.setClass(this, ZipBrowserActivity.class);
		    			    intentZip.putExtra(ZipBrowserActivity.EXTRA_PATH_ZIP, zipFile.getAbsolutePath());
		    			    intentZip.putExtra(ZipBrowserActivity.EXTRA_HANDLE_ZIP, tempNode.getHandle());

		    				this.startActivity(intentZip);
							
						}
						else{		
							log("MimeTypeList other file");
							Intent viewIntent = new Intent(Intent.ACTION_VIEW);
							viewIntent.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
							if (isIntentAvailable(this, viewIntent)){
								log("if isIntentAvailable");
								startActivity(viewIntent);
							}								
							else{
								log("ELSE isIntentAvailable");
								Intent intentShare = new Intent(Intent.ACTION_SEND);
								intentShare.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
								if (isIntentAvailable(this, intentShare)){
									log("call to startActivity(intentShare)");
									startActivity(intentShare);
								}									
								String toastMessage = getString(R.string.general_already_downloaded) + ": " + localPath;
								Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
							}								
						}
						return;
					}
				}
			}
			
			for (long hash : hashes) {
				log("hashes.length more than 1");
				MegaNode node = megaApi.getNodeByHandle(hash);
				if(node != null){
					log("node NOT null");
					Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
					if (node.getType() == MegaNode.TYPE_FOLDER) {
						log("MegaNode.TYPE_FOLDER");
						getDlList(dlFiles, node, new File(parentPath, new String(node.getName())));
					} else {
						log("MegaNode.TYPE_FILE");
						dlFiles.put(node, parentPath);
					}
					
					for (MegaNode document : dlFiles.keySet()) {
						
						String path = dlFiles.get(document);
						log("path of the file: "+path);
						
						if(availableFreeSpace < document.getSize()){
							log("Not enough space");
							Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space) + " (" + new String(document.getName()) + ")", false, this);
							continue;
						}
						
						log("start service");
						Intent service = new Intent(this, DownloadService.class);
						service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
						service.putExtra(DownloadService.EXTRA_URL, url);
						service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
						service.putExtra(DownloadService.EXTRA_PATH, path);
						startService(service);
					}
				}
				else if(url != null) {
					log("URL NOT null");
					if(availableFreeSpace < size) {
						log("Not enough space");
						Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space), false, this);
						continue;
					}
					log("start service");
					Intent service = new Intent(this, DownloadService.class);
					service.putExtra(DownloadService.EXTRA_HASH, hash);
					service.putExtra(DownloadService.EXTRA_URL, url);
					service.putExtra(DownloadService.EXTRA_SIZE, size);
					service.putExtra(DownloadService.EXTRA_PATH, parentPath);
					startService(service);
				}
				else {
					log("node NOT fOUND!!!!!");
				}
			}
		}
	}
	
	@Override
	public boolean onTransferData(MegaApiJava api, MegaTransfer transfer, byte[] buffer)
	{
		log("onTransferData");
		return true;
	}
	
	public void removeContact(final MegaUser c){
		
		//TODO (megaApi.getInShares(c).size() != 0) --> Si el contacto que voy a borrar tiene carpetas compartidas, avisar de eso y eliminar las shares (IN and ¿OUT?)
		
		final ArrayList<MegaNode> inShares = megaApi.getInShares(c);
		
		if(inShares.size() != 0)
		{
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	
			        	for(int i=0; i<inShares.size();i++){
			        		MegaNode removeNode = inShares.get(i);
			        		megaApi.remove(removeNode);			        		
			        	}
			        	megaApi.removeContact(c, managerActivity);			        	
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //No button clicked
			            break;
			        }
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(managerActivity);
			builder.setMessage(getResources().getString(R.string.confirmation_remove_contact)+" "+c.getEmail()+"?").setPositiveButton(R.string.general_yes, dialogClickListener)
			    .setNegativeButton(R.string.general_no, dialogClickListener).show();
		}
		else{
			//NO incoming shares
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	//TODO remove the outgoing shares
			        	
			        	megaApi.removeContact(c, managerActivity);
			        	
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //No button clicked
			            break;
			        }
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(managerActivity);
			String message= getResources().getString(R.string.confirmation_remove_contact)+" "+c.getEmail()+"?";
			builder.setMessage(message).setPositiveButton(R.string.general_yes, dialogClickListener)
			    .setNegativeButton(R.string.general_no, dialogClickListener).show();			
			
		}	
		
	}
	
	public void removeMultipleContacts(final List<MegaUser> contacts){
		
		//TODO (megaApi.getInShares(c).size() != 0) --> Si el contacto que voy a borrar tiene carpetas compartidas, avisar de eso y eliminar las shares (IN and ¿OUT?)
		for(int j=0; j<contacts.size();j++){
			
			final MegaUser c= contacts.get(j);
			
			final ArrayList<MegaNode> inShares = megaApi.getInShares(c);
			
			if(inShares.size() != 0)
			{			
			        	
	        	for(int i=0; i<inShares.size();i++){
	        		MegaNode removeNode = inShares.get(i);
	        		megaApi.remove(removeNode);			        		
	        	}
	        	megaApi.removeContact(c, managerActivity);		        	
				
			}
			else{
				//NO incoming shares				
				        	
	        	megaApi.removeContact(c, managerActivity);
			}	
		}		
	}
	
	public void shareFolder(ArrayList<Long> handleList){
		log("shareFolder ArrayListLong");
		//TODO shareMultipleFolders

		if((drawerItem == DrawerItem.SHARED_WITH_ME) || (drawerItem == DrawerItem.CLOUD_DRIVE) ){
			Intent intent = new Intent(ContactsExplorerActivity.ACTION_PICK_CONTACT_SHARE_FOLDER);
	    	intent.setClass(this, ContactsExplorerActivity.class);
	    	
	    	long[] handles=new long[handleList.size()];
	    	int j=0;
	    	for(int i=0; i<handleList.size();i++){
	    		handles[j]=handleList.get(i);
	    		j++;
	    	}	    	
	    	intent.putExtra(ContactsExplorerActivity.EXTRA_NODE_HANDLE, handles);
	    	//Multiselect=1 (multiple folders)
	    	intent.putExtra("MULTISELECT", 1);
	    	intent.putExtra("SEND_FILE",0);
	    	startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
		}			
	}
	
	public void shareFolderLollipop(ArrayList<Long> handleList){
		log("shareFolder ArrayListLong");
		//TODO shareMultipleFolders

		Intent intent = new Intent(ContactsExplorerActivityLollipop.ACTION_PICK_CONTACT_SHARE_FOLDER);
    	intent.setClass(this, ContactsExplorerActivityLollipop.class);
    	
    	long[] handles=new long[handleList.size()];
    	int j=0;
    	for(int i=0; i<handleList.size();i++){
    		handles[j]=handleList.get(i);
    		j++;
    	}	    	
    	intent.putExtra(ContactsExplorerActivityLollipop.EXTRA_NODE_HANDLE, handles);
    	//Multiselect=1 (multiple folders)
    	intent.putExtra("MULTISELECT", 1);
    	intent.putExtra("SEND_FILE",0);
    	startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
			
	}
	
	public void shareFolderLollipop(MegaNode node){
		log("shareFolderLollipop");		
											
		Intent intent = new Intent(ContactsExplorerActivityLollipop.ACTION_PICK_CONTACT_SHARE_FOLDER);
    	intent.setClass(this, ContactsExplorerActivityLollipop.class);
    	//Multiselect=0
    	intent.putExtra("MULTISELECT", 0);
    	intent.putExtra("SEND_FILE",0);
    	intent.putExtra(ContactsExplorerActivityLollipop.EXTRA_NODE_HANDLE, node.getHandle());
    	startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
				
	}
	
	public void sentToInbox(MegaNode node){
		log("sentToInbox MegaNode");
		
		if((drawerItem == DrawerItem.SHARED_WITH_ME) || (drawerItem == DrawerItem.CLOUD_DRIVE) ){
			sendToInbox = true;			
			Intent intent = new Intent(ContactsExplorerActivity.ACTION_PICK_CONTACT_SEND_FILE);
	    	intent.setClass(this, ContactsExplorerActivity.class);
	    	//Multiselect=0
	    	intent.putExtra("MULTISELECT", 0);
	    	intent.putExtra("SEND_FILE",1);
	    	intent.putExtra(ContactsExplorerActivity.EXTRA_NODE_HANDLE, node.getHandle());
	    	startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
		}			
	}
	
	public void pickContacToSendFile(List<MegaUser> users){
		
		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
		intent.setAction(FileExplorerActivityLollipop.ACTION_SELECT_FILE);
		String[] longArray = new String[users.size()];
		for (int i=0; i<users.size(); i++){
			longArray[i] = users.get(i).getEmail();
		}
		intent.putExtra("SELECTED_CONTACTS", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);		
	}
	
	public void leaveIncomingShare (MegaNode n){
		log("leaveIncomingShare");
		//TODO 
//		ProgressDialog temp = null;
//		try{
//			temp = new ProgressDialog(this);
//			temp.setMessage(getString(R.string.leave_incoming_share)); 
//			temp.show();
//		}
//		catch(Exception e){
//			return;
//		}
//		statusDialog = temp;
		megaApi.remove(n);
	}

	public void removeAllSharingContacts (ArrayList<MegaShare> listContacts, MegaNode node)
	{
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(this);
			temp.setMessage(getString(R.string.remove_all_sharing)); 
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;
		
		for(int j=0; j<listContacts.size();j++){
			String cMail = listContacts.get(j).getUser();
			if(cMail!=null){
				MegaUser c = megaApi.getContact(cMail);
				if (c != null){							
					megaApi.share(node, c, MegaShare.ACCESS_UNKNOWN, this);
				}
				else{
					isGetLink = false;
					megaApi.disableExport(node, this);
				}
			}
			else{
				isGetLink = false;
				megaApi.disableExport(node, this);
			}
		}	
		//TODO change the place
		try{
			statusDialog.dismiss();
		}
		catch(Exception e){
			return;
		}		
	}
	
	public void showUpAF(BitSet paymentBitSet){
		
//		Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("maF");
//        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
//        fragTransaction.detach(currentFragment);
//        fragTransaction.commit();
//
//        fragTransaction = getSupportFragmentManager().beginTransaction();
//        fragTransaction.attach(currentFragment);
//        fragTransaction.commit();
		
		if (paymentBitSet == null){
			if (this.paymentBitSet != null){
				paymentBitSet = this.paymentBitSet;
			}
		}
		
		accountFragment=UPGRADE_ACCOUNT_FRAGMENT;
		
		mTabHostContacts.setVisibility(View.GONE);    			
		viewPagerContacts.setVisibility(View.GONE); 
		mTabHostShares.setVisibility(View.GONE);    			
		mTabHostShares.setVisibility(View.GONE);
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(upAF==null){
			upAF = new UpgradeAccountFragment();
			upAF.setInfo(paymentBitSet);
			ft.replace(R.id.fragment_container, upAF, "upAF");
			ft.commit();
		}
		else{
			upAF.setInfo(paymentBitSet);
			ft.replace(R.id.fragment_container, upAF, "upAF");
			ft.commit();
		}
	}
	
	public void showpF(int type, ArrayList<Product> accounts, BitSet paymentBitSet){
		showpF(type, accounts, false, paymentBitSet);
	}
	
	public void showpF(int type, ArrayList<Product> accounts, boolean refresh, BitSet paymentBitSet){
		log("showpF");
		
		if (paymentBitSet == null){
			if (this.paymentBitSet != null){
				paymentBitSet = this.paymentBitSet;
			}
		}
		
		accountFragment=PAYMENT_FRAGMENT;
		
		mTabHostContacts.setVisibility(View.GONE);    			
		viewPagerContacts.setVisibility(View.GONE); 
		mTabHostShares.setVisibility(View.GONE);    			
		mTabHostShares.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (!refresh){
			if(pF==null){			
				pF = new PaymentFragment();
				pF.setInfo(type, accounts, paymentBitSet);
				ft.replace(R.id.fragment_container, pF, "pF");
				ft.commit();
			}
			else{			
				pF.setInfo(type, accounts, paymentBitSet);			
				ft.replace(R.id.fragment_container, pF, "pF");
				ft.commit();
			}
		}
		else{
			Fragment tempF = getSupportFragmentManager().findFragmentByTag("pF");
			if (tempF != null){
				ft.detach(tempF);
				ft.attach(tempF);
				ft.commit();
			}
			else{
				if(pF==null){			
					pF = new PaymentFragment();
					pF.setInfo(type, accounts, paymentBitSet);
					ft.replace(R.id.fragment_container, pF, "pF");
					ft.commit();
				}
				else{			
					pF.setInfo(type, accounts, paymentBitSet);			
					ft.replace(R.id.fragment_container, pF, "pF");
					ft.commit();
				}
			}
		}
	}
	
	public void onUpgrade1Click(View view) {
		if (upAF != null){
			showpF(1, upAF.getAccounts(), upAF.getPaymentBitSet());
		}
	}

	public void onUpgrade2Click(View view) {
		if (upAF != null){
			showpF(2, upAF.getAccounts(), upAF.getPaymentBitSet());
		}
	}

	public void onUpgrade3Click(View view) {
		if (upAF != null){
			showpF(3, upAF.getAccounts(), upAF.getPaymentBitSet());
		}
	}
	
	public void onUpgradeLiteClick(View view){
		if (upAF != null){
			showpF(4, upAF.getAccounts(), upAF.getPaymentBitSet());
		}
	}
	
	public void onYearlyClick(View view) {
		log("yearly");
		pF.payYear();		
	}
	
	public void onMonthlyClick(View view) {
		log("monthly");
		pF.payMonth();	
	}
	
	public void showMyAccount(){
		drawerItem = DrawerItem.ACCOUNT;
		selectDrawerItemLollipop(drawerItem);
	}
	
	public void showCC(int type, ArrayList<Product> accounts, int payMonth, BitSet paymentBitSet){
		showCC(type, accounts, payMonth, false, paymentBitSet);
	}
	
	public void showCC(int type, ArrayList<Product> accounts, int payMonth, boolean refresh, BitSet paymentBitSet){
		
		if (paymentBitSet == null){
			if (this.paymentBitSet != null){
				paymentBitSet = this.paymentBitSet;
			}
		}
		
		accountFragment = CC_FRAGMENT;
		mTabHostContacts.setVisibility(View.GONE);    			
		viewPagerContacts.setVisibility(View.GONE); 
		mTabHostShares.setVisibility(View.GONE);    			
		mTabHostShares.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (!refresh){
			if (ccF == null){
				ccF = new CreditCardFragment();
				ccF.setInfo(type, accounts, payMonth, paymentBitSet);
				ft.replace(R.id.fragment_container, ccF, "ccF");
				ft.commit();
			}
			else{			
				ccF.setInfo(type, accounts, payMonth, paymentBitSet);			
				ft.replace(R.id.fragment_container, ccF, "ccF");
				ft.commit();
			}
		}
		else{
			Fragment tempF = getSupportFragmentManager().findFragmentByTag("ccF");
			if (tempF != null){
				ft.detach(tempF);
				ft.attach(tempF);
				ft.commit();
			}
			else{
				if (ccF == null){
					ccF = new CreditCardFragment();
					ccF.setInfo(type, accounts, payMonth, paymentBitSet);
					ft.replace(R.id.fragment_container, ccF, "ccF");
					ft.commit();
				}
				else{			
					ccF.setInfo(type, accounts, payMonth, paymentBitSet);			
					ft.replace(R.id.fragment_container, ccF, "ccF");
					ft.commit();
				}
			}
		}
	}
	
	public void showFortumo(){
		accountFragment = FORTUMO_FRAGMENT;
		mTabHostContacts.setVisibility(View.GONE);    			
		viewPagerContacts.setVisibility(View.GONE); 
		mTabHostShares.setVisibility(View.GONE);    			
		mTabHostShares.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (fF == null){
			fF = new FortumoFragment();
			ft.replace(R.id.fragment_container,  fF, "fF");
			ft.commit();
		}
		else{
			ft.replace(R.id.fragment_container, fF, "fF");
			ft.commit();
		}
	}
	
	public int getUsedPerc()
	{
		return usedPerc;
	}
	
	public void upgradeAccountButton(){
		log("upgradeAccountButton");
		drawerItem = DrawerItem.ACCOUNT;
		if (accountInfo != null){
			if ((accountInfo.getSubscriptionStatus() == MegaAccountDetails.SUBSCRIPTION_STATUS_NONE) || (accountInfo.getSubscriptionStatus() == MegaAccountDetails.SUBSCRIPTION_STATUS_INVALID)){
				Time now = new Time();
				now.setToNow();
				if (accountType != 0){
					log("accountType != 0");
					if (now.toMillis(false) >= (accountInfo.getProExpiration()*1000)){
						if (Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CREDIT_CARD) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_FORTUMO) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CENTILI)){
							log("SUBSCRIPTION INACTIVE: CHECKBITSET --> CC || FORT || INFO");
							showUpAF(null);
						}
						else{
							Toast.makeText(this, getString(R.string.not_upgrade_is_possible), Toast.LENGTH_LONG).show();
						}
					}
					else{
						log("CURRENTLY ACTIVE SUBSCRIPTION");
						Toast.makeText(this, getString(R.string.not_upgrade_is_possible), Toast.LENGTH_LONG).show();
					}
				}
				else{
					log("accountType == 0");
					if (Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CREDIT_CARD) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_FORTUMO) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CENTILI)){
						log("CHECKBITSET --> CC || FORT || INFO");
						showUpAF(null);
					}
					else{
						Toast.makeText(this, getString(R.string.not_upgrade_is_possible), Toast.LENGTH_LONG).show();
					}
				}
			}
			else{
				Toast.makeText(this, getString(R.string.not_upgrade_is_possible), Toast.LENGTH_LONG).show();
			}
		}
		else{
			Toast.makeText(this, getString(R.string.not_upgrade_is_possible), Toast.LENGTH_LONG).show();
		}
	}

	public long getNumberOfSubscriptions(){
		if (cancelSubscription != null){
			cancelSubscription.setVisible(false);
		}
		if (numberOfSubscriptions > 0){
			if (cancelSubscription != null){
				if (drawerItem == DrawerItem.ACCOUNT){
					if (maF != null){
						cancelSubscription.setVisible(true);
					}
				}
			}
		}
		return numberOfSubscriptions;
	}
	
	public void setNumberOfSubscriptions(long numberOfSubscriptions){
		this.numberOfSubscriptions = numberOfSubscriptions;
	}
	
	public void showStatusDialog(String text){
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(managerActivity);
			temp.setMessage(text);
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;
	}
	
	public void dismissStatusDialog(){
		if (statusDialog != null){
			try{
				statusDialog.dismiss();
			}
			catch(Exception ex){}
		}
	}
	
	public void reinviteContact(MegaContactRequest c)
	{
		log("inviteContact");
		megaApi.inviteContact(c.getTargetEmail(), null, MegaContactRequest.INVITE_ACTION_REMIND, this);
	}
	
	public void removeInvitationContact(MegaContactRequest c)
	{
		log("removeInvitationContact");
		megaApi.inviteContact(c.getTargetEmail(), null, MegaContactRequest.INVITE_ACTION_DELETE, this);
	}
	
	public void acceptInvitationContact(MegaContactRequest c)
	{
		log("acceptInvitationContact");
		megaApi.replyContactRequest(c, MegaContactRequest.REPLY_ACTION_ACCEPT, this);
	}
	
	public void ignoreInvitationContact(MegaContactRequest c)
	{
		log("ignoreInvitationContact");
		megaApi.replyContactRequest(c, MegaContactRequest.REPLY_ACTION_IGNORE, this);
	}
	
	public void declineInvitationContact(MegaContactRequest c)
	{
		log("declineInvitationContact");
		megaApi.replyContactRequest(c, MegaContactRequest.REPLY_ACTION_DENY, this);
	}
	
	public int getAccountType(){
		return accountType;
	}
	
	public long getUsedGbStorage(){
		return usedGbStorage;
	}

	@Override
	public void onAccountUpdate(MegaApiJava api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onContactRequestsUpdate(MegaApiJava api, ArrayList<MegaContactRequest> requests) {
		log("---------------------onContactRequestsUpdate");
		// TODO Auto-generated method stub
		if (drawerItem == DrawerItem.CONTACTS){
//			int index = viewPagerContacts.getCurrentItem();
//			if(index==1||index==0){	
//				log("En SentRequestFragment TAB");
			String sRFTag1 = getFragmentTag(R.id.contact_tabs_pager, 1);	
			log("Tag: "+ sRFTag1);
			sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sRFTag1);
			if (sRFLol != null){	
				log("sRFLol != null");
//					ArrayList<MegaContactRequest> contacts = megaApi.getOutgoingContactRequests();
//			    	if(contacts!=null)
//			    	{
//			    		log("contacts SIZE: "+contacts.size());
//			    	}
				sRFLol.setContactRequests();
			}	
//			}
//			else if(index==2){
//				log("En ReceiveRequestFragment TAB");
			String rRFTag2 = getFragmentTag(R.id.contact_tabs_pager, 2);	
			log("Tag: "+ rRFTag2);
			rRF = (ReceivedRequestsFragment) getSupportFragmentManager().findFragmentByTag(rRFTag2);
			if (rRF != null){					
				rRF.setContactRequests();
			}	
//			}						
		}
		
	}
}
