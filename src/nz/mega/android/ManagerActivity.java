package nz.mega.android;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nz.mega.android.FileStorageActivity.Mode;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.util.Log;
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
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;


public class ManagerActivity extends PinActivity implements OnItemClickListener, OnClickListener, MegaRequestListenerInterface, MegaGlobalListenerInterface, MegaTransferListenerInterface {
	
	public enum DrawerItem {
		CLOUD_DRIVE, SAVED_FOR_OFFLINE, CAMERA_UPLOADS, INBOX, SHARED_WITH_ME, CONTACTS, TRANSFERS, RUBBISH_BIN, SETTINGS, ACCOUNT, SEARCH;

		public String getTitle(Context context) {
			switch(this)
			{
				case CLOUD_DRIVE: return context.getString(R.string.section_cloud_drive);				
				case SAVED_FOR_OFFLINE: return context.getString(R.string.section_saved_for_offline);
				case CAMERA_UPLOADS: return context.getString(R.string.section_photo_sync);
				case INBOX: return context.getString(R.string.section_inbox);
				case SHARED_WITH_ME: return context.getString(R.string.section_shared_items);
				case CONTACTS: return context.getString(R.string.section_contacts);
				case TRANSFERS: return context.getString(R.string.section_transfers);
				case RUBBISH_BIN: return context.getString(R.string.section_rubbish_bin);
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
	
	public static int MODE_IN = 0;
	public static int MODE_OUT = 1;
	
	String accessToken;
	
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
	private FileBrowserFragment fbF;
	private ContactsFragment cF;
	private RubbishBinFragment rbF;
	private IncomingSharesFragment inSF;
	private OutgoingSharesFragment outSF;
    private TransfersFragment tF; 
    private MyAccountFragment maF;
    private OfflineFragment oF;
    private SearchFragment sF;
    private CameraUploadFragment psF;
    private UpgradeAccountFragment upAF;
    private PaymentFragment pF;
    private InboxFragment iF;
    private CreditCardFragment ccF;
    private FortumoFragment fF;
    
    //Tabs in Contacts
    private TabHost mTabHostContacts;
    //private Fragment contactTabFragment;	
	TabsAdapter mTabsAdapterContacts;
    ViewPager viewPagerContacts;    
    private TabHost mTabHostShares;
	TabsAdapter mTabsAdapterShares;
    ViewPager viewPagerShares; 
    
    static ManagerActivity managerActivity;
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
	NavigationDrawerAdapter nDA;	
	String pathNavigation = "/";	
	long lastTimeOnTransferUpdate = -1;	
	boolean firstTimeCam = false;
	
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
			
			if (rbF != null){
				ArrayList<MegaNode> rubbishNodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
				
//				ProgressDialog temp = null;
//				try{
//					temp = new ProgressDialog(this);
//					temp.setMessage(getString(R.string.context_delete_from_mega));
//					temp.show();
//				}
//				catch(Exception e){
//					return;
//				}
//				statusDialog = temp;
				
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
	    		Intent loginWithOldCredentials = new Intent(this, LoginActivity.class);
	    		startActivity(loginWithOldCredentials);
	    		finish();
	    		return;
		    }
			
			Intent newIntent = getIntent();
		    
		    if (newIntent != null){
		    	if (newIntent.getAction() != null){
		    		if (newIntent.getAction().equals(ManagerActivity.ACTION_OPEN_MEGA_LINK) || newIntent.getAction().equals(ManagerActivity.ACTION_OPEN_MEGA_FOLDER_LINK)){
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
                      
        mTabHostContacts = (TabHost)findViewById(R.id.tabhost_contacts);
        mTabHostContacts.setup();
        
        mTabHostShares = (TabHost)findViewById(R.id.tabhost_shares);
        mTabHostShares.setup();
        
        viewPagerContacts = (ViewPager) findViewById(R.id.contact_tabs_pager);  
        viewPagerShares = (ViewPager) findViewById(R.id.shares_tabs_pager);   
        
        
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
					if (getIntent().getAction().equals(ManagerActivity.ACTION_IMPORT_LINK_FETCH_NODES)){
						Intent intent = new Intent(managerActivity, LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ManagerActivity.ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();	
						return;
					}
					else if (getIntent().getAction().equals(ManagerActivity.ACTION_OPEN_MEGA_LINK)){
						Intent intent = new Intent(managerActivity, FileLinkActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ManagerActivity.ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();	
						return;
					}
					else if (getIntent().getAction().equals(ManagerActivity.ACTION_OPEN_MEGA_FOLDER_LINK)){
						Intent intent = new Intent(managerActivity, LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(ManagerActivity.ACTION_OPEN_MEGA_FOLDER_LINK);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();	
						return;
					}
					else if (getIntent().getAction().equals(ACTION_CANCEL_UPLOAD) || getIntent().getAction().equals(ACTION_CANCEL_DOWNLOAD) || getIntent().getAction().equals(ACTION_CANCEL_CAM_SYNC)){
						Intent intent = new Intent(managerActivity, LoginActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(getIntent().getAction());
						startActivity(intent);
						finish();
						return;
					}
				}
			}
			Intent intent = new Intent(managerActivity, LoginActivity.class);
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
	        
	        megaApi.getAccountDetails(this);
	        megaApi.creditCardQuerySubscriptions(this);
	        
	        List<String> items = new ArrayList<String>();
			for (DrawerItem item : DrawerItem.values()) {
				if (!(item.equals(DrawerItem.SEARCH)||(item.equals(DrawerItem.ACCOUNT)))){					
					items.add(item.getTitle(this));
				}
			}
	        
			nDA = new NavigationDrawerAdapter(getApplicationContext(), items);
			mDrawerList.setAdapter(nDA);
	        
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
					case 5:{
						drawerItem = DrawerItem.RUBBISH_BIN;
						isListRubbishBin = true;
						break;
					}
					case 6:{
						drawerItem = DrawerItem.RUBBISH_BIN;
						isListRubbishBin = false;
						break;
					}
					case 7:{
						drawerItem = DrawerItem.TRANSFERS;
						downloadPlay = savedInstanceState.getBoolean("downloadPlay", true);
						pauseIconVisible = savedInstanceState.getBoolean("pauseIconVisible", false);
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
								selectDrawerItem(drawerItem);
								showpF(1, null);
								return;
							}
							case 2:{
								drawerItem = DrawerItem.ACCOUNT;
								selectDrawerItem(drawerItem);
								showpF(2, null);
								return;
							}
							case 3:{
								drawerItem = DrawerItem.ACCOUNT;
								selectDrawerItem(drawerItem);
								showpF(3, null);
								return;
							}	
							case 4:{
								drawerItem = DrawerItem.ACCOUNT;
								selectDrawerItem(drawerItem);
								showpF(4, null);
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
			selectDrawerItem(drawerItem);
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
    	if (fbF != null){
    		pHBrowser = fbF.getParentHandle();
    		if (drawerItem == DrawerItem.CLOUD_DRIVE){
    			if (isListCloudDrive){
    				visibleFragment = 1;
    			}
    			else{
    				visibleFragment = 2;
    			}
    		}
    	}
    	
    	String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
    	if (cF != null){
    		if (drawerItem == DrawerItem.CONTACTS){
    			if (isListContacts){
    				visibleFragment = 3;
    			}
    			else{
    				visibleFragment = 4;
    			}
    		}
    	}
    	if (drawerItem == DrawerItem.RUBBISH_BIN)
    	{
    		pHRubbish = rbF.getParentHandle();
    		if (rbF != null){
    			if (isListRubbishBin){
    				visibleFragment = 5;
    			}
    			else{
    				visibleFragment = 6;
    			}
    		}
    	}    	
    	
    	if (inSF != null){
    		pHSharedWithMe = inSF.getParentHandle();
    		if (drawerItem == DrawerItem.SHARED_WITH_ME){
    			if (isListSharedWithMe){
    				visibleFragment = 8;
    			}
    			else{
    				visibleFragment = 9;
    			}
    		}
    	}
    	
    	if (tF != null){
	    	if (drawerItem == DrawerItem.TRANSFERS){
	    		visibleFragment = 7;
	    		outState.putBoolean("pauseIconVisible", pauseIconVisible);
	    		outState.putBoolean("downloadPlay", downloadPlay);
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
    		pHInbox = iF.getParentHandle();
    		if (rbF != null){
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
    			
    			if(getIntent().getAction().equals(ManagerActivity.ACTION_EXPLORE_ZIP)){  

    				String pathZip=intent.getExtras().getString(EXTRA_PATH_ZIP);    				
    				
    				Intent intentZip = new Intent(managerActivity, ZipBrowserActivity.class);    				
    				intentZip.putExtra(ZipBrowserActivity.EXTRA_PATH_ZIP, pathZip);
    			    startActivity(intentZip);   				
    				
    			}
//    			else if(getIntent().getAction().equals(ManagerActivity.ACTION_OPEN_PDF)){    				
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
    			else if (getIntent().getAction().equals(ManagerActivity.ACTION_IMPORT_LINK_FETCH_NODES)){
					Intent loginIntent = new Intent(managerActivity, LoginActivity.class);
					loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					loginIntent.setAction(ManagerActivity.ACTION_IMPORT_LINK_FETCH_NODES);
					loginIntent.setData(Uri.parse(getIntent().getDataString()));
					startActivity(loginIntent);
					finish();	
					return;
				}
				else if (getIntent().getAction().equals(ManagerActivity.ACTION_OPEN_MEGA_LINK)){
					Intent fileLinkIntent = new Intent(managerActivity, FileLinkActivity.class);
					fileLinkIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					fileLinkIntent.setAction(ManagerActivity.ACTION_IMPORT_LINK_FETCH_NODES);
					fileLinkIntent.setData(Uri.parse(getIntent().getDataString()));
					startActivity(fileLinkIntent);
					finish();	
					return;
				}
    			else if (intent.getAction().equals(ACTION_OPEN_MEGA_FOLDER_LINK)){
    				Intent intentFolderLink = new Intent(managerActivity, FolderLinkActivity.class);
    				intentFolderLink.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				intentFolderLink.setAction(ManagerActivity.ACTION_OPEN_MEGA_FOLDER_LINK);
    				intentFolderLink.setData(Uri.parse(getIntent().getDataString()));
					startActivity(intentFolderLink);
					finish();
    			}
    			else if (intent.getAction().equals(ACTION_REFRESH_PARENTHANDLE_BROWSER)){
    				
    				parentHandleBrowser = intent.getLongExtra("parentHandle", -1);    				
    				intent.removeExtra("parentHandle");
    				setParentHandleBrowser(parentHandleBrowser);
    				
    				if (fbF != null){
						fbF.setParentHandle(parentHandleBrowser);
    					fbF.setIsList(isListCloudDrive);
    					fbF.setOrder(orderGetChildren);
    					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
    					fbF.setNodes(nodes);
    					if (!fbF.isVisible()){
    						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbF, "fbF").commit();
    					}
    				}	
    				else{
    					fbF = new FileBrowserFragment();
    					fbF.setParentHandle(parentHandleBrowser);
    					fbF.setIsList(isListCloudDrive);
    					fbF.setOrder(orderGetChildren);
    					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
    					fbF.setNodes(nodes);
    					if (!fbF.isVisible()){
    						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbF, "fbF").commit();
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
			
//			Intent openIntent = new Intent(this, ManagerActivity.class);
			Intent openFileIntent = new Intent(this, FileLinkActivity.class);
			openFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			openFileIntent.setAction(ManagerActivity.ACTION_OPEN_MEGA_LINK);
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
			openFolderIntent.setAction(ManagerActivity.ACTION_OPEN_MEGA_FOLDER_LINK);
			openFolderIntent.setData(Uri.parse(url));
			startActivity(openFolderIntent);
//			finish();
			return;
		}
		else{
			log("wrong url");
			Intent errorIntent = new Intent(this, ManagerActivity.class);
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
		selectDrawerItem(drawerItem);
	}
    
	private View getTabIndicator(Context context, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);

        TextView tv = (TextView) view.findViewById(R.id.textView);
        tv.setText(title);
        return view;
    }
	
	public void setInitialCloudDrive (){
		drawerItem = DrawerItem.CLOUD_DRIVE;
		nDA.setPositionClicked(0);
		mDrawerLayout.openDrawer(Gravity.LEFT);
		firstTime = true;
		selectDrawerItem(drawerItem);

//		if (fbF == null){
//			fbF = new FileBrowserFragment();
//			if (parentHandleBrowser == -1){
//				fbF.setParentHandle(megaApi.getRootNode().getHandle());
//				parentHandleBrowser = megaApi.getRootNode().getHandle();
//			}
//			else{
//				fbF.setParentHandle(parentHandleBrowser);
//			}
//			fbF.setIsList(isListCloudDrive);
//			fbF.setOrder(orderGetChildren);
//			ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
//			fbF.setNodes(nodes);
//		}
//		else{
//								
//			fbF.setIsList(isListCloudDrive);
//			fbF.setParentHandle(parentHandleBrowser);
//			fbF.setOrder(orderGetChildren);
//			ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
//			fbF.setNodes(nodes);
//		}
//		
//		mTabHostContacts.setVisibility(View.GONE);    			
//		viewPagerContacts.setVisibility(View.GONE); 
//		mTabHostShares.setVisibility(View.GONE);    			
//		viewPagerShares.setVisibility(View.GONE);
//		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//		ft.replace(R.id.fragment_container, fbF, "fbF");
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
		nDA.setPositionClicked(POS_CAMERA_UPLOADS);
		
		Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("psF");
		FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
		fragTransaction.detach(currentFragment);
		fragTransaction.commit();

		fragTransaction = getSupportFragmentManager().beginTransaction();
		fragTransaction.attach(currentFragment);
		fragTransaction.commit();
	}
	
    public void selectDrawerItem(DrawerItem item){
    	log("selectDrawerItem");
    	switch (item){
    		case CLOUD_DRIVE:{
//    			
//    			megaApi.getPricing(this);
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			if (fbF == null){
					fbF = new FileBrowserFragment();
					if (parentHandleBrowser == -1){
						fbF.setParentHandle(megaApi.getRootNode().getHandle());
						parentHandleBrowser = megaApi.getRootNode().getHandle();
					}
					else{
						fbF.setParentHandle(parentHandleBrowser);
					}
					fbF.setIsList(isListCloudDrive);
					fbF.setOrder(orderGetChildren);
					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
					fbF.setNodes(nodes);
				}
				else{
										
					fbF.setIsList(isListCloudDrive);
					fbF.setParentHandle(parentHandleBrowser);
					fbF.setOrder(orderGetChildren);
					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderGetChildren);
					fbF.setNodes(nodes);
				}
								
				mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, fbF, "fbF");
    			ft.commit();
    			
    			if (!firstTime){
    				mDrawerLayout.closeDrawer(Gravity.LEFT);
    			}
    			else{
    				firstTime = false;
    			}
    			
    			customSearch.setVisibility(View.VISIBLE);
    			viewPagerContacts.setVisibility(View.GONE);
    			
    			if (createFolderMenuItem != null){
    				changePass.setVisible(false); 
        			exportMK.setVisible(false); 
        			removeMK.setVisible(false); 
	    			createFolderMenuItem.setVisible(true);
	    			addContactMenuItem.setVisible(false);
	    			addMenuItem.setVisible(true);
	    			refreshMenuItem.setVisible(false);
	    			sortByMenuItem.setVisible(true);
	    			helpMenuItem.setVisible(false);
	    			upgradeAccountMenuItem.setVisible(false);
	    			settingsMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(true);
	    			unSelectMenuItem.setVisible(false);
	    			thumbViewMenuItem.setVisible(true);
	    			addMenuItem.setEnabled(true);	  
 	    			
	    			if (isListCloudDrive){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}
	    			rubbishBinMenuItem.setVisible(false);
	    			rubbishBinMenuItem.setTitle(getString(R.string.section_rubbish_bin));
	    			clearRubbishBinMenuitem.setVisible(false);
    			}
    			
    			break;
    		}
    		case INBOX:{
   			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (iF == null){
    				iF = new InboxFragment();
    				iF.setParentHandle(megaApi.getInboxNode().getHandle());
    				parentHandleInbox = megaApi.getInboxNode().getHandle();
    				iF.setIsList(isListInbox);
    				iF.setOrder(orderGetChildren);
    				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
    				iF.setNodes(nodes);
    			}
    			else{
    				iF.setIsList(isListInbox);
    				iF.setParentHandle(parentHandleInbox);
    				iF.setOrder(orderGetChildren);
    				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleInbox), orderGetChildren);
    				iF.setNodes(nodes);
    			}
    			    			
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, iF, "iF");
    			ft.commit();
    			
    			customSearch.setVisibility(View.VISIBLE);
    			viewPagerContacts.setVisibility(View.GONE);
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			if (createFolderMenuItem != null){
    				//Show				
        			sortByMenuItem.setVisible(true);
        			selectMenuItem.setVisible(true);            			
        			
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
        			clearRubbishBinMenuitem.setVisible(false);
        			rubbishBinMenuItem.setVisible(false);
	    		}

    			break;
    		}
    		case CONTACTS:{
  			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
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
        	        
        	        TabHost.TabSpec tabSpec3 = mTabHostContacts.newTabSpec("receivedRequests");
        	        tabSpec3.setIndicator(getTabIndicator(mTabHostContacts.getContext(), getString(R.string.tab_received_requests))); // new function to inject our own tab layout
   				
    				
    				mTabsAdapterContacts.addTab(tabSpec1, ContactsFragment.class, null);
    				mTabsAdapterContacts.addTab(tabSpec2, SentRequestsFragment.class, null);
    				mTabsAdapterContacts.addTab(tabSpec3, ReceivedRequestsFragment.class, null);
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
    		case RUBBISH_BIN:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (rbF == null){
    				rbF = new RubbishBinFragment();
    				rbF.setParentHandle(megaApi.getRubbishNode().getHandle());
    				parentHandleRubbish = megaApi.getRubbishNode().getHandle();
    				rbF.setIsList(isListRubbishBin);
    				rbF.setOrder(orderGetChildren);
    				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
    				rbF.setNodes(nodes);
    			}
    			else{
    				rbF.setIsList(isListRubbishBin);
    				rbF.setParentHandle(parentHandleRubbish);
    				rbF.setOrder(orderGetChildren);
    				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderGetChildren);
    				rbF.setNodes(nodes);
    			}
    			
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, rbF, "rbF");
    			ft.commit();
    			
    			customSearch.setVisibility(View.VISIBLE);
    			viewPagerContacts.setVisibility(View.GONE);
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			if (createFolderMenuItem != null){
    				createFolderMenuItem.setVisible(false);
	    			addMenuItem.setVisible(false);
	    			refreshMenuItem.setVisible(false);
	    			sortByMenuItem.setVisible(true);
	    			helpMenuItem.setVisible(false);
	    			upgradeAccountMenuItem.setVisible(false);
	    			settingsMenuItem.setVisible(false);
	    			selectMenuItem.setVisible(true);
	    			unSelectMenuItem.setVisible(true);
	    			thumbViewMenuItem.setVisible(true);
	    			addMenuItem.setEnabled(false);
	    			changePass.setVisible(false);
	    			exportMK.setVisible(false); 
        			removeMK.setVisible(false); 
    			
        			if (isListRubbishBin){	
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}
        			rbF.setIsList(isListRubbishBin);	        			
        			rbF.setParentHandle(parentHandleRubbish);
        			rubbishBinMenuItem.setVisible(false);
        			rubbishBinMenuItem.setTitle(getString(R.string.section_cloud_drive));
	    			clearRubbishBinMenuitem.setVisible(true);
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
    			
    			Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    			if (currentFragment != null){
    				getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
    			}
    			
    			mTabHostShares.getTabWidget().setBackgroundColor(Color.BLACK);
    			
    			mTabHostShares.setVisibility(View.VISIBLE);    			
    			mTabHostShares.setVisibility(View.VISIBLE);
    			
    			if (mTabsAdapterShares == null){
    				mTabsAdapterShares= new TabsAdapter(this, mTabHostShares, viewPagerShares);   	
    				
        			TabHost.TabSpec tabSpec3 = mTabHostShares.newTabSpec("incomingSharesFragment");
        			tabSpec3.setIndicator(getTabIndicator(mTabHostShares.getContext(), getString(R.string.tab_incoming_shares))); // new function to inject our own tab layout
        	        //tabSpec.setContent(contentID);
        	        //mTabHostContacts.addTab(tabSpec);
        	        TabHost.TabSpec tabSpec4 = mTabHostShares.newTabSpec("outgoingSharesFragment");
        	        tabSpec4.setIndicator(getTabIndicator(mTabHostShares.getContext(), getString(R.string.tab_outgoing_shares))); // new function to inject our own tab layout
        	                	          				
    				mTabsAdapterShares.addTab(tabSpec3, IncomingSharesFragment.class, null);
    				mTabsAdapterShares.addTab(tabSpec4, OutgoingSharesFragment.class, null);
    				
    			}
    			
    			mTabHostShares.setOnTabChangedListener(new OnTabChangeListener(){
                    @Override
                    public void onTabChanged(String tabId) {
                    	log("TabId :"+ tabId);
                    	supportInvalidateOptionsMenu();
                        if(tabId.equals("outgoingSharesFragment")){                         	
                			if (outSF != null){                 				
                				if(parentHandleOutgoing!=-1){
	                				MegaNode node = megaApi.getNodeByHandle(parentHandleOutgoing);
	            					aB.setTitle(node.getName());
            					}
                				else{
                					aB.setTitle(getResources().getString(R.string.section_shared_items));
                					outSF.refresh(); 
                				}            					   				
                			}
                        }
                        else if(tabId.equals("incomingSharesFragment")){                        	
                        	if (inSF != null){                        		
                        		if(parentHandleIncoming!=-1){
                        			
                        			MegaNode node = megaApi.getNodeByHandle(parentHandleIncoming);
                					aB.setTitle(node.getName());	
            					}
                				else{
                					
                					aB.setTitle(getResources().getString(R.string.section_shared_items));
                					inSF.refresh(); 
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
    			
    			if (createFolderMenuItem != null){
    				selectMenuItem.setVisible(true);
    				sortByMenuItem.setVisible(true);
    				thumbViewMenuItem.setVisible(true); 
        			upgradeAccountMenuItem.setVisible(false);

    				
        			//Hide
    				createFolderMenuItem.setVisible(false);
    				addContactMenuItem.setVisible(false);
        			addMenuItem.setVisible(false);   			
        			selectMenuItem.setVisible(false);
        			unSelectMenuItem.setVisible(false);  				
        			rubbishBinMenuItem.setVisible(false);
        			addMenuItem.setVisible(false);
        			createFolderMenuItem.setVisible(false);
        			rubbishBinMenuItem.setVisible(false);
        			clearRubbishBinMenuitem.setVisible(false);
        			changePass.setVisible(false); 
        			exportMK.setVisible(false); 
        			removeMK.setVisible(false); 
        			settingsMenuItem.setVisible(false);
    				refreshMenuItem.setVisible(false);
    				helpMenuItem.setVisible(false);
	    		}
    			
    			if (inSF != null){
    				aB.setTitle(getString(R.string.section_shared_items));	
    				inSF.refresh();			
    				
    			}    			
    			if (outSF != null){    				
					aB.setTitle(getString(R.string.section_shared_items));				
					outSF.refresh();    				
    			}
    			
    			break;
    		}
    		case SETTINGS:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			mDrawerLayout.closeDrawer(Gravity.LEFT);
    			
    			startActivity(new Intent(this, SettingsActivity.class));
    			
    			drawerItem = lastDrawerItem;
    			selectDrawerItem(drawerItem);
    			
    			break;
    		}
    		case ACCOUNT:{
    			
    			if (nDA != null){
					nDA.setPositionClicked(-1);
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
    		}
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
    		}
    		case SAVED_FOR_OFFLINE:{
    			
    			topControlBar.setBackgroundColor(getResources().getColor(R.color.navigation_drawer_background));
    			
    			if (oF == null){
    				oF = new OfflineFragment();
    				oF.setIsList(isListOffline);
    				oF.setPathNavigation("/");
    			}
    			else{
    				oF.setPathNavigation("/");
    				oF.setIsList(isListOffline);
    			}
    			
    			mTabHostContacts.setVisibility(View.GONE);    			
    			viewPagerContacts.setVisibility(View.GONE); 
    			mTabHostShares.setVisibility(View.GONE);    			
    			mTabHostShares.setVisibility(View.GONE);
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, oF, "oF");
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
    			
    			if (nDA != null){
    				nDA.setPositionClicked(POS_CAMERA_UPLOADS);
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
		log("onBackPressed");
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
		
		if (fbF != null){
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				if (fbF.onBackPressed() == 0){
					super.onBackPressed();
					return;
				}
			}
		}	
		
		if (drawerItem == DrawerItem.SEARCH){
			if (sF != null){
    			if (sF.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					selectDrawerItem(drawerItem);
					if(nDA!=null){
						nDA.setPositionClicked(0);
					}
					return;
				}
    		}
    	}
		
		if (drawerItem == DrawerItem.INBOX){
			if (iF != null){			
				if (iF.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					selectDrawerItem(drawerItem);
					if(nDA!=null){
						nDA.setPositionClicked(0);
					}
					return;
				}
			}
		}
		
		if (drawerItem == DrawerItem.CONTACTS){
			String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
			cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
			if (cF != null){			
				if (cF.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					selectDrawerItem(drawerItem);
					if(nDA!=null){
						nDA.setPositionClicked(0);
					}
					return;
				}
			}
		}
		
		if (drawerItem == DrawerItem.SHARED_WITH_ME){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){				
				//OUTGOING				
				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
				log("Tag: "+ cFTag2);
				outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag2);
				if (outSF != null){					
					if (outSF.onBackPressed() == 0){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItem(drawerItem);
						if(nDA!=null){
							nDA.setPositionClicked(0);
						}
						return;
					}					
				}
			}
			else{			
				//InCOMING
				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
				log("Tag: "+ cFTag1);
				inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag1);
				if (inSF != null){					
					if (inSF.onBackPressed() == 0){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItem(drawerItem);
						if(nDA!=null){
							nDA.setPositionClicked(0);
						}
						return;
					}					
				}				
			}	
		}
		if (rbF != null){
			if (drawerItem == DrawerItem.RUBBISH_BIN){
				if (rbF.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					selectDrawerItem(drawerItem);
					if(nDA!=null){
						nDA.setPositionClicked(0);
					}
					return;
				}
			}
		}
		if (tF != null){
			if (drawerItem == DrawerItem.TRANSFERS){
				if (tF.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					selectDrawerItem(drawerItem);
					if(nDA!=null){
						nDA.setPositionClicked(0);
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
						selectDrawerItem(drawerItem);
						if(nDA!=null){
							nDA.setPositionClicked(0);
							
						}					
					}
					return;
				}
				case UPGRADE_ACCOUNT_FRAGMENT:{
					if (upAF != null){						
						drawerItem = DrawerItem.ACCOUNT;
						selectDrawerItem(drawerItem);
						if(nDA!=null){
							nDA.setPositionClicked(-1);
							
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
						showpF(parameterType, accounts);
					}
					else{
						showUpAF();
					}
					return;
				}
				case OVERQUOTA_ALERT:{
					if (upAF != null){						
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItem(drawerItem);
						if(nDA!=null){
							nDA.setPositionClicked(0);
							
						}					
					}
					return;
				}
				default:{
					if (fbF != null){						
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItem(drawerItem);
						if(nDA!=null){
							nDA.setPositionClicked(0);
							
						}					
					}
				}
			}
		}
		
		if (oF != null){
			if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
				if (oF.onBackPressed() == 0){
					attr = dbH.getAttributes();
					if (attr != null){
						if (attr.getOnline() != null){
							if (!Boolean.parseBoolean(attr.getOnline())){
								super.onBackPressed();
								return;
							}
						}
					}
					
					if (fbF != null){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						selectDrawerItem(drawerItem);
						if(nDA!=null){
							nDA.setPositionClicked(0);
						}
					}
					else{
						super.onBackPressed();
					}
					return;
				}
			}
		}
		
//		if (sF != null){
//			if (drawerItem == DrawerItem.SEARCH){
//				if (sF.onBackPressed() == 0){
//					drawerItem = DrawerItem.CLOUD_DRIVE;
//					selectDrawerItem(drawerItem);
//					if(nDA!=null){
//						nDA.setPositionClicked(0);
//					}
//					return;
//				}
//			}
//		}
		
		if (psF != null){
			if (drawerItem == DrawerItem.CAMERA_UPLOADS){
				if (psF.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					selectDrawerItem(drawerItem);
					if(nDA!=null){
						nDA.setPositionClicked(0);
					}
					return;
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
    		selectDrawerItem(DrawerItem.SEARCH);    		
    		isSearching = false;
	    } 
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		log("onCreateOptionsMenu");
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
		
//		if (drawerItem == DrawerItem.CLOUD_DRIVE){
		if (fbF != null){
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				//Show
				addMenuItem.setEnabled(true);
				addMenuItem.setVisible(true);
				createFolderMenuItem.setVisible(true);
				selectMenuItem.setVisible(true);
				sortByMenuItem.setVisible(true);
				thumbViewMenuItem.setVisible(true);
				rubbishBinMenuItem.setVisible(false);				
    			upgradeAccountMenuItem.setVisible(false);    			
    			importLinkMenuItem.setVisible(true);
    			takePicture.setVisible(true);
    			
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
    			
    			if (isListCloudDrive){	
    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
				}
				else{
					thumbViewMenuItem.setTitle(getString(R.string.action_list));
    			}
    			
    			return super.onCreateOptionsMenu(menu);
			}
		}
		
		if (drawerItem == DrawerItem.CONTACTS){
			int index = viewPagerContacts.getCurrentItem();
			if (index == 0){
				String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
				cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (cF != null){
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
		
		
		if (drawerItem == DrawerItem.RUBBISH_BIN){
			if (rbF != null){	
				//Show
				refreshMenuItem.setVisible(true);
    			sortByMenuItem.setVisible(true);
    			selectMenuItem.setVisible(true);
    			thumbViewMenuItem.setVisible(true);
    			
				//Hide
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
    			rbF.setIsList(isListRubbishBin);	        			
    			rbF.setParentHandle(parentHandleRubbish);
    			rubbishBinMenuItem.setVisible(false);
    			rubbishBinMenuItem.setTitle(getString(R.string.section_cloud_drive));
    			clearRubbishBinMenuitem.setVisible(true);
			}
		}
		
		
		if (drawerItem == DrawerItem.INBOX){
			if (iF != null){	
				//Show				
    			sortByMenuItem.setVisible(true);
    			selectMenuItem.setVisible(true);    			
    			    			
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
			int index = viewPagerShares.getCurrentItem();
			if(index==0){	
				String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
				inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(sharesTag);
				if (inSF != null){
					sortByMenuItem.setVisible(true);
					thumbViewMenuItem.setVisible(true); 
					upgradeAccountMenuItem.setVisible(false);

					addMenuItem.setEnabled(true);
					addMenuItem.setVisible(true);

					log("parentHandleIncoming: "+parentHandleIncoming);
					if(parentHandleIncoming==-1){
						addMenuItem.setVisible(false);
					}
					else{
						addMenuItem.setVisible(true);
					}

					//Hide
					pauseRestartTransfersItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					selectMenuItem.setVisible(false);
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
			else if(index==1){
				String sharesTag = getFragmentTag(R.id.shares_tabs_pager, 1);		
				outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(sharesTag);
				if (outSF != null){

					selectMenuItem.setVisible(true);
					sortByMenuItem.setVisible(true);
					thumbViewMenuItem.setVisible(true); 
					upgradeAccountMenuItem.setVisible(false);


					log("parentHandleOutgoing: "+parentHandleOutgoing);
					if(parentHandleOutgoing==-1){
						addMenuItem.setVisible(false);
					}
					else{
						addMenuItem.setVisible(true);
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
		
		if (tF != null){
			if (drawerItem == DrawerItem.TRANSFERS){
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
    			importLinkMenuItem.setVisible(false);
    			takePicture.setVisible(false);					
    			refreshMenuItem.setVisible(false);
				helpMenuItem.setVisible(false);
				settingsMenuItem.setVisible(false);
    			
//    			if (downloadPlay){
//    				addMenuItem.setIcon(R.drawable.ic_pause);
//    			}
//    			else{
//    				addMenuItem.setIcon(R.drawable.ic_play);
//    			}
    			
    			if (megaApi.getTransfers().size() == 0){
    				downloadPlay = true;
    			}
			}
		}
		
		if (oF != null){
			if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
				
				//Show
				refreshMenuItem.setVisible(true);
    			sortByMenuItem.setVisible(true);
    			thumbViewMenuItem.setVisible(false); //TODO
    			selectMenuItem.setVisible(true);
    			upgradeAccountMenuItem.setVisible(false);
    			
				//Hide
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
	public void copyToSDCard (long[] hashes){
		log("copyToSDCard");
		String externalPath = Util.getExternalCardPath();
		File newFile =  new File(externalPath+"/"+"prueba.txt");
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

	    // Filter to only show results that can be "opened", such as
	    // a file (as opposed to a list of contacts or timezones).
	    intent.addCategory(Intent.CATEGORY_OPENABLE);

	    // Create a file with the requested MIME type.
	    String mimeType = MimeTypeList.getMimeType(newFile);
	    intent.setType(mimeType);
	    intent.putExtra(Intent.EXTRA_TITLE, newFile.getAbsolutePath());
	    startActivityForResult(intent, WRITE_SD_CARD_REQUEST_CODE);		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelected");
		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}
		
		log("retryPendingConnections()");
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
		    		if (fbF != null){
		    			if (drawerItem == DrawerItem.CLOUD_DRIVE){
		    				fbF.onBackPressed();
		    				return true;
		    			}
		    		}
		    		if (rbF != null){
		    			if (drawerItem == DrawerItem.RUBBISH_BIN){
		    				rbF.onBackPressed();
		    				return true;
		    			}
		    		}
		    		if (drawerItem == DrawerItem.SHARED_WITH_ME){
		    			int index = viewPagerShares.getCurrentItem();
		    			if(index==1){				
		    				//OUTGOING				
		    				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
		    				log("Tag: "+ cFTag2);
		    				outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag2);
		    				if (outSF != null){					
		    					outSF.onBackPressed();				
		    				}
		    			}
		    			else{			
		    				//InCOMING
		    				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
		    				log("Tag: "+ cFTag1);
		    				inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag1);
		    				if (inSF != null){					
		    					inSF.onBackPressed();					
		    				}				
		    			}	
		    		}
		    		if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
		    			if (oF != null){
		    				oF.onBackPressed();
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
	        case R.id.action_new_folder:{
	        	if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        		showNewFolderDialog(null);
	        	}
	        	else if (drawerItem == DrawerItem.CONTACTS){
	        		showNewContactDialog(null);
	        	}
	        	
	        	else if (drawerItem == DrawerItem.RUBBISH_BIN){
	        		showClearRubbishBinDialog(null);
	        	}
	        	return true;
	        }
	        case R.id.action_add:{
	        	
	        	if (drawerItem == DrawerItem.SHARED_WITH_ME){
	        		String swmTag = getFragmentTag(R.id.shares_tabs_pager, 0);		
	        		inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(swmTag);
	        		if (viewPagerShares.getCurrentItem()==0){		
		        		if (inSF != null){	        		
		        			Long checkHandle = inSF.getParentHandle();		        			
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
	        		outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(swmTag);	
	        		if (viewPagerShares.getCurrentItem()==1){	
		        		if (outSF != null){        			
		        			this.uploadFile();
		        		}
	        		}
	        	}	
	        	else {
	        		this.uploadFile();
	        	}
	        	
	        	return true;     	
	        }
	        case R.id.action_pause_restart_transfers:{
	        	if (drawerItem == DrawerItem.TRANSFERS){	    			
	    			if (downloadPlay){
	    				downloadPlay = false;
	    				pauseRestartTransfersItem.setTitle(getResources().getString(R.string.menu_restart_transfers));
	    			}
	    			else{
	    				downloadPlay = true;
	    				pauseRestartTransfersItem.setTitle(getResources().getString(R.string.menu_pause_transfers));
	    			}
	    			megaApi.pauseTransfers(!downloadPlay, this);
	        	}
	        	
	        	return true;
	        }
	        case R.id.action_select:{
	        	//TODO: multiselect
	        	if (fbF != null){
	        		if (drawerItem == DrawerItem.CLOUD_DRIVE){	        			
	        			fbF.selectAll();
	        			if (fbF.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
	        			}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        			return true;
	        		}
	        	}

	        	String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
	        	cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
	        	if (cF != null){
	        		if (drawerItem == DrawerItem.CONTACTS){
	        			cF.selectAll();
	        			if (cF.showSelectMenuItem()){
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
	        		inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(swmTag);
	        		if (viewPagerShares.getCurrentItem()==0){		
		        		if (inSF != null){	        		
		        			inSF.selectAll();
		        			if (inSF.showSelectMenuItem()){
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
	        		outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(swmTag);	
	        		if (viewPagerShares.getCurrentItem()==1){	
		        		if (outSF != null){        			
		        			outSF.selectAll();
		        			if (outSF.showSelectMenuItem()){
		        				selectMenuItem.setVisible(true);
		        				unSelectMenuItem.setVisible(false);
		        			}
		        			else{
		        				selectMenuItem.setVisible(false);
		        				unSelectMenuItem.setVisible(true);
		        			}
	        			}
	        		}
	        	}
	        	if (oF != null){ 
	        		if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
	    				oF.selectAll();
	    				if (oF.showSelectMenuItem()){
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
	        		if (iF != null){	        		
	    				iF.selectAll();
	    				if (iF.showSelectMenuItem()){
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
	        	
	        	if (rbF != null){
	        		if (drawerItem == DrawerItem.RUBBISH_BIN){	        			
	        			rbF.selectAll();
	        			if (rbF.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
	        			}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        			return true;
	        		}
	        	}
	        	
	        	return true;
	        }
	        case R.id.action_grid:{	    			
	        	//TODO: gridView
	        	if (fbF != null){
	        		if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("fbF");
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
	        			fbF.setIsList(isListCloudDrive);
	        			fbF.setParentHandle(parentHandleBrowser);

	        			fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.attach(currentFragment);
	        			fragTransaction.commit();

	        		}
	        	}

	        	String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
	    		cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
	        	if (cF != null){
	        		if (drawerItem == DrawerItem.CONTACTS){
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
	        			cF.setIsList(isListContacts);

	        			fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.attach(currentFragment);
	        			fragTransaction.commit();	

	        		}
	        	}

	        	if (rbF != null){
	        		if (drawerItem == DrawerItem.RUBBISH_BIN){
	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("rbF");
	        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.detach(currentFragment);
	        			fragTransaction.commit();
	        			
	        			isListRubbishBin = !isListRubbishBin;
	        			if (isListRubbishBin){	
		    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
						}
						else{
							thumbViewMenuItem.setTitle(getString(R.string.action_list));
		    			}
	        			rbF.setIsList(isListRubbishBin);	        			
	        			rbF.setParentHandle(parentHandleRubbish);

	        			fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.attach(currentFragment);
	        			fragTransaction.commit();

	        		}
	        	}

	        	if (drawerItem == DrawerItem.SHARED_WITH_ME){
	        		
	    			Toast toast = Toast.makeText(this, getString(R.string.general_not_yet_implemented), Toast.LENGTH_LONG);
	    			toast.show();
//	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("inSF");
//	        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
//	        			fragTransaction.detach(currentFragment);
//	        			fragTransaction.commit();
//
//	        			isListSharedWithMe = !isListSharedWithMe;
//	        			inSF.setIsList(isListSharedWithMe);
//	        			inSF.setParentHandle(parentHandleSharedWithMe);
//
//	        			fragTransaction = getSupportFragmentManager().beginTransaction();
//	        			fragTransaction.attach(currentFragment);
//	        			fragTransaction.commit();

	        		
	        	}

        		if (oF != null){
        			if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
        				Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("oF");
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
        				oF.setIsList(isListOffline);						
        				oF.setPathNavigation(pathNavigation);
        				//oF.setGridNavigation(false);
        				//oF.setParentHandle(parentHandleSharedWithMe);

        				fragTransaction = getSupportFragmentManager().beginTransaction();
        				fragTransaction.attach(currentFragment);
        				fragTransaction.commit();


        			}
        		}

        		if (psF != null){
        			if (drawerItem == DrawerItem.CAMERA_UPLOADS){
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
	        case R.id.action_rubbish_bin:{
	        	if (drawerItem == DrawerItem.RUBBISH_BIN){
	        		drawerItem = DrawerItem.CLOUD_DRIVE;
	        		selectDrawerItem(drawerItem);
	        	}
	        	else if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        		drawerItem = DrawerItem.RUBBISH_BIN;
	        		selectDrawerItem(drawerItem);
	        	}
	        	return true;
	        }
	        case R.id.action_menu_clear_rubbish_bin:{
	        	if (drawerItem == DrawerItem.RUBBISH_BIN){
	        		showClearRubbishBinDialog(null);
	        	}
	        	return true;
	        }
	        case R.id.action_menu_refresh:{
	        	switch(drawerItem){
		        	case CLOUD_DRIVE:{
		        		Intent intent = new Intent(managerActivity, LoginActivity.class);
			    		intent.setAction(LoginActivity.ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleBrowser);
			    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
		        		break;
		        	}
		        	case CONTACTS:{
		        		Intent intent = new Intent(managerActivity, LoginActivity.class);
			    		intent.setAction(LoginActivity.ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleBrowser);
			    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
			    		break;
		        	}
		        	case RUBBISH_BIN:{
		        		Intent intent = new Intent(managerActivity, LoginActivity.class);
			    		intent.setAction(LoginActivity.ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleRubbish);
			    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
			    		break;
		        	}
		        	case SHARED_WITH_ME:{
		        		
		        		int index = viewPagerShares.getCurrentItem();
		    			if(index==1){				
		    				//OUTGOING				
		    				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
		    				log("Tag: "+ cFTag2);
		    				outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag2);
		    				if (outSF != null){					
		    					Intent intent = new Intent(managerActivity, LoginActivity.class);
					    		intent.setAction(LoginActivity.ACTION_REFRESH);
					    		intent.putExtra("PARENT_HANDLE", parentHandleOutgoing);
					    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
					    		break;
		    				}
		    			}
		    			else{			
		    				//InCOMING
		    				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
		    				log("Tag: "+ cFTag1);
		    				inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag1);
		    				if (inSF != null){					
		    					Intent intent = new Intent(managerActivity, LoginActivity.class);
					    		intent.setAction(LoginActivity.ACTION_REFRESH);
					    		intent.putExtra("PARENT_HANDLE", parentHandleIncoming);
					    		startActivityForResult(intent, REQUEST_CODE_REFRESH);
					    		break;
		    				}				
		    			}	
		        	}
		        	case ACCOUNT:{
		        		Intent intent = new Intent(managerActivity, LoginActivity.class);
			    		intent.setAction(LoginActivity.ACTION_REFRESH);
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
	        	showUpAF();
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
	        		megaApi.creditCardCancelSubscriptions(this);
	        	}
	        	return true;
	        }
            default:{
	            return super.onOptionsItemSelected(item);
            }
	    }
	}
	
	public void selectSortByContacts(int _orderContacts){
		this.orderContacts = _orderContacts;
		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
		cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cF != null){	
			cF.setOrder(orderContacts);
			if (orderContacts == MegaApiJava.ORDER_DEFAULT_ASC){
				cF.sortByNameAscending();
			}
			else{
				cF.sortByNameDescending();
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
		
		if (oF != null){	
			oF.setOrder(orderOffline);
			if (orderOffline == MegaApiJava.ORDER_DEFAULT_ASC){
				oF.sortByNameAscending();
			}
			else{
				oF.sortByNameDescending();
			}
		}
	}
	
	public void selectSortByIncoming(int _orderIncoming){
		log("selectSortByIncoming");
		
		this.orderIncoming = _orderIncoming;
		
		if (inSF != null){	
			inSF.setOrder(orderIncoming);
			if (orderIncoming == MegaApiJava.ORDER_DEFAULT_ASC){
				inSF.sortByNameAscending();
			}
			else{
				inSF.sortByNameDescending();
			}
		}
	}
	
	public void selectSortByOutgoing(int _orderOutgoing){
		log("selectSortByOutgoing");
		
		this.orderOutgoing = _orderOutgoing;
		
		if (outSF != null){	
			outSF.setOrder(orderOutgoing);
			if (orderOutgoing == MegaApiJava.ORDER_DEFAULT_ASC){
				outSF.sortByNameAscending();
			}
			else{
				outSF.sortByNameDescending();
			}
		}
	}
	
	public void selectSortByCloudDrive(int _orderGetChildren){
		this.orderGetChildren = _orderGetChildren;
		MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
		if (parentNode != null){
			if (fbF != null){						
				ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
				fbF.setOrder(orderGetChildren);
				fbF.setNodes(nodes);
				fbF.getListView().invalidateViews();						
			}
		}
		else{
			if (fbF != null){						
				ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
				fbF.setOrder(orderGetChildren);
				fbF.setNodes(nodes);
				fbF.getListView().invalidateViews();					
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
			
		if (nDA != null){
			nDA.setPositionClicked(position);
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
			selectDrawerItem(drawerItem);
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
				if (nDA != null){
					nDA.setPositionClicked(-1);
				}
				drawerItem = DrawerItem.ACCOUNT;
				titleAB = drawerItem.getTitle(this);
				selectDrawerItem(drawerItem);
				break;
			}
			case R.id.bottom_control_bar:{
				if (nDA != null){
					nDA.setPositionClicked(-1);
				}
				drawerItem = DrawerItem.ACCOUNT;
				titleAB = drawerItem.getTitle(this);
				selectDrawerItem(drawerItem);
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
		log("---------onRequestFinish: "  + request.getRequestString());
		
		if (request.getType() == MegaRequest.TYPE_ACCOUNT_DETAILS){
			log ("account_details request");
			if (e.getErrorCode() == MegaError.API_OK){
				
				MegaAccountDetails accountInfo = request.getMegaAccountDetails();
				
				
				int accountType = accountInfo.getProLevel();
				
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
				long usedStorage = accountInfo.getStorageUsed();
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
					
					used = used + usedStorage + " GB";
					
				}
				else{
					if (usedStorage >= 1024){
						usedStorage = usedStorage / 1024;
						used = used + usedStorage + " TB";
					}
					else{
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
						ft.detach(fbF);
						ft.attach(fbF);
						ft.commitAllowingStateLoss();
		        	}
		        }
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
//					log("calling ManagerActivity.logout");
//					MegaApiAndroid megaApi = app.getMegaApi(); 
//					ManagerActivity.logout(managerActivity, app, megaApi, false);
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
					cF.notifyDataSetChanged();
				}	
			}
			else{
				log("Termino con error");
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (moveToRubbish){
				if (e.getErrorCode() == MegaError.API_OK){
					Toast.makeText(this, getString(R.string.context_correctly_moved), Toast.LENGTH_SHORT).show();
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
			
			if (e.getErrorCode() == MegaError.API_OK){
//				Toast.makeText(this, getString(R.string.context_correctly_moved), Toast.LENGTH_SHORT).show();
				if (drawerItem == DrawerItem.CLOUD_DRIVE){
					if (fbF != null){
					
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbF.getParentHandle()), orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();
					}
				}
				if (drawerItem == DrawerItem.INBOX){
					if (iF != null){
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(iF.getParentHandle()), orderGetChildren);
//							rbF.setNodes(nodes);
						iF.refresh();
					}
				}	
				if (drawerItem == DrawerItem.RUBBISH_BIN){
					if (rbF != null){
					
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbF.getParentHandle()), orderGetChildren);
						rbF.setNodes(nodes);
						rbF.getListView().invalidateViews();
					}
				}
				if (drawerItem == DrawerItem.SHARED_WITH_ME){
					if (inSF != null){
						//TODO: ojo con los hijos
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSF.getParentHandle()), orderGetChildren);
//							inSF.setNodes(nodes);
						inSF.getListView().invalidateViews();
					}
					if (outSF != null){
						//TODO: ojo con los hijos
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(outSF.getParentHandle()), orderGetChildren);
//							inSF.setNodes(nodes);
						outSF.getListView().invalidateViews();
					}
				}
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
				
				if (fbF != null){
					if (drawerItem == DrawerItem.CLOUD_DRIVE){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbF.getParentHandle()), orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();
					}
				}
				if (rbF != null){
					if (drawerItem == DrawerItem.RUBBISH_BIN){
						if (isClearRubbishBin){
							isClearRubbishBin = false;
							parentHandleRubbish = megaApi.getRubbishNode().getHandle();
							rbF.setParentHandle(megaApi.getRubbishNode().getHandle());
							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
							rbF.setNodes(nodes);
							rbF.getListView().invalidateViews();
							aB.setTitle(getString(R.string.section_rubbish_bin));	
							getmDrawerToggle().setDrawerIndicatorEnabled(true);
						}
						else{
							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbF.getParentHandle()), orderGetChildren);
							rbF.setNodes(nodes);
							rbF.getListView().invalidateViews();
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
				if (fbF != null){
					if (drawerItem == DrawerItem.CLOUD_DRIVE){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbF.getParentHandle()), orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();
					}
				}
				if (rbF != null){
					if (drawerItem == DrawerItem.RUBBISH_BIN){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbF.getParentHandle()), orderGetChildren);
						rbF.setNodes(nodes);
						rbF.getListView().invalidateViews();
					}
				}
				if (inSF != null){
					if (drawerItem == DrawerItem.SHARED_WITH_ME){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSF.getParentHandle()), orderGetChildren);
						//TODO: ojo con los hijos
//						inSF.setNodes(nodes);
						inSF.getListView().invalidateViews();
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
				if (drawerItem == DrawerItem.INBOX||drawerItem == DrawerItem.CLOUD_DRIVE){
					if(iF!=null){
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
			}
			else{
				try { 
					statusDialog.dismiss();	
				} 
				catch (Exception ex) {}
				
				if (e.getErrorCode() == MegaError.API_OK){
					Toast.makeText(this, getString(R.string.context_correctly_copied), Toast.LENGTH_SHORT).show();
					if (fbF != null){
						if (drawerItem == DrawerItem.CLOUD_DRIVE){
							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbF.getParentHandle()), orderGetChildren);
							fbF.setNodes(nodes);
							fbF.getListView().invalidateViews();
						}
					}
					if (rbF != null){
						if (drawerItem == DrawerItem.RUBBISH_BIN){
							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbF.getParentHandle()), orderGetChildren);
							rbF.setNodes(nodes);
							rbF.getListView().invalidateViews();
						}
					}
					if (inSF != null){
						if (drawerItem == DrawerItem.SHARED_WITH_ME){
							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(inSF.getParentHandle()), orderGetChildren);
							//TODO: ojo con los hijos
//							inSF.setNodes(nodes);
//							inSF.getListView().invalidateViews();
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
				if (fbF != null){
					if (drawerItem == DrawerItem.CLOUD_DRIVE){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbF.getParentHandle()), orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();
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
//				cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
//				if (cF != null){
//					if (drawerItem == DrawerItem.CONTACTS){	
//						ArrayList<MegaUser> contacts = megaApi.getContacts();
//						cF.setContacts(contacts);
//						cF.getListView().invalidateViews();
//					}
//				}
			}
			log("add contact");
		}
		else if (request.getType() == MegaRequest.TYPE_PAUSE_TRANSFERS){
			if (e.getErrorCode() == MegaError.API_OK) {
				if (tF != null){
					if (drawerItem == DrawerItem.TRANSFERS){
						if (!downloadPlay){
		    				pauseRestartTransfersItem.setTitle(getResources().getString(R.string.menu_restart_transfers));
							tF.setPause(true);
						}
						else{
		    				pauseRestartTransfersItem.setTitle(getResources().getString(R.string.menu_pause_transfers));
							tF.setPause(false);
						}		
					}
				}				
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CANCEL_TRANSFER){
			if (e.getErrorCode() == MegaError.API_OK){
				if (tF != null){
					if (drawerItem == DrawerItem.TRANSFERS){
						Intent cancelOneIntent = new Intent(this, DownloadService.class);
						cancelOneIntent.setAction(DownloadService.ACTION_CANCEL_ONE_DOWNLOAD);				
						startService(cancelOneIntent);
						tF.setTransfers(megaApi.getTransfers());
					}
				}
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
				selectDrawerItem(drawerItem);
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
		log("onFileClick");
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
			if (prefs.getStorageAskAlways() != null){
				if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
					//askMe==false
					if (prefs.getStorageDownloadLocation() != null){
						if (prefs.getStorageDownloadLocation().compareTo("") != 0){
							askMe = false;
							downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
						}
					}
				}
				else
				{
					//askMe=true
					if (prefs.getStorageAdvancedDevices() != null){
						advancedDevices = Boolean.parseBoolean(prefs.getStorageAdvancedDevices());						
					}
					
				}
			}
		}		
			
		if (askMe){
			
			if(advancedDevices){
				//Launch Intent to SAF
		    	this.copyToSDCard(hashes);
			}
			else{
				Intent intent = new Intent(Mode.PICK_FOLDER.getAction());
				intent.putExtra(FileStorageActivity.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
				intent.putExtra(FileStorageActivity.EXTRA_SIZE, size);
				intent.setClass(this, FileStorageActivity.class);
				intent.putExtra(FileStorageActivity.EXTRA_DOCUMENT_HASHES, hashes);
				startActivityForResult(intent, REQUEST_CODE_SELECT_LOCAL_FOLDER);
			}				
		}
		else{
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
				if (fbF != null){
					if (!(drawerItem == DrawerItem.CLOUD_DRIVE)){
						return;
					}
				}
				if (rbF != null){
					if (drawerItem == DrawerItem.RUBBISH_BIN){
						return;
					}
				}
				String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);		
				cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
				if (cF != null){
					if (drawerItem == DrawerItem.CONTACTS){
						return;
					}
				}
				if (inSF != null){
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
		if (rbF.isVisible()){
			rbF.setPositionClicked(-1);
			rbF.notifyDataSetChanged();
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
		cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cF != null){
			if (drawerItem == DrawerItem.CONTACTS){
				cF.setPositionClicked(-1);
				cF.notifyDataSetChanged();
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
						addContact(value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		addContactDialog = builder.create();
		addContactDialog.show();
	}
	
	public void pickFolderToShare(List<MegaUser> users){
		
		Intent intent = new Intent(this, FileExplorerActivity.class);
		intent.setAction(FileExplorerActivity.ACTION_SELECT_FOLDER);
		String[] longArray = new String[users.size()];
		for (int i=0; i<users.size(); i++){
			longArray[i] = users.get(i).getEmail();
		}
		intent.putExtra("SELECTED_CONTACTS", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_FOLDER);
		
	}
	
	public void showNewFolderDialog(String editText){
		log("showNewFolderDialog");
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			fbF.setPositionClicked(-1);
			fbF.notifyDataSetChanged();
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
			parentHandle = fbF.getParentHandle();
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
	
	public void showMove(ArrayList<Long> handleList){
		log("showMove");
		Intent intent = new Intent(this, FileExplorerActivity.class);
		intent.setAction(FileExplorerActivity.ACTION_PICK_MOVE_FOLDER);
		long[] longArray = new long[handleList.size()];
		for (int i=0; i<handleList.size(); i++){
			longArray[i] = handleList.get(i);
		}
		intent.putExtra("MOVE_FROM", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_MOVE_FOLDER);
	}
	
	public void showCopy(ArrayList<Long> handleList){
		log("showCopy");
		Intent intent = new Intent(this, FileExplorerActivity.class);
		intent.setAction(FileExplorerActivity.ACTION_PICK_COPY_FOLDER);
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
					uploadServiceIntent = new Intent (ManagerActivity.this, UploadService.class);
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
			log("EOEOEOEOE: " + uri);
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
				titleDivider.setBackgroundColor(resources.getColor(R.color.mega));
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
			
			final ArrayList<String> contactsData = intent.getStringArrayListExtra(ContactsExplorerActivity.EXTRA_CONTACTS);			
			final boolean megaContacts = intent.getBooleanExtra(ContactsExplorerActivity.EXTRA_MEGA_CONTACTS, true);
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
						titleDivider.setBackgroundColor(resources.getColor(R.color.mega));
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
						titleDivider.setBackgroundColor(resources.getColor(R.color.mega));
						
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
				parentHandleUpload = fbF.getParentHandle();
			}
			else if(drawerItem == DrawerItem.SHARED_WITH_ME){
				int index = viewPagerShares.getCurrentItem();
				if(index==0){	
					//INCOMING
					String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
//					log("Tag: "+ cFTag1);
					inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag1);
					if (inSF != null){		
						parentHandleUpload=inSF.getParentHandle();
					}					
				}
				else if(index==1){
					//OUTGOING
					String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 1);	
//					log("Tag: "+ cFTag1);
					outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag1);
					if (outSF != null){		
						parentHandleUpload=outSF.getParentHandle();
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
			
			if (intent == null) {			
				log("Return.....");
				return;						
			}
			
			String parentPath = intent.getStringExtra(FileStorageActivity.EXTRA_PATH);
			String url = intent.getStringExtra(FileStorageActivity.EXTRA_URL);
			long size = intent.getLongExtra(FileStorageActivity.EXTRA_SIZE, 0);
			long[] hashes = intent.getLongArrayExtra(FileStorageActivity.EXTRA_DOCUMENT_HASHES);
			
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
					if (fbF != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();						
					}
				}
				else{
					if (fbF != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();						
					}
				}
			}
			else if (drawerItem == DrawerItem.RUBBISH_BIN){
				parentHandleRubbish = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleRubbish);
				if (parentNode != null){
					if (rbF != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						rbF.setNodes(nodes);
						rbF.getListView().invalidateViews();						
					}
				}
				else{
					if (rbF != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
						rbF.setNodes(nodes);
						rbF.getListView().invalidateViews();						
					}
				}
			}
			else if (drawerItem == DrawerItem.SHARED_WITH_ME){
				parentHandleIncoming = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
				if (parentNode != null){
					if (inSF != null){					
//						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						//TODO: ojo con los hijos
//							inSF.setNodes(nodes);
						inSF.getListView().invalidateViews();						
					}
				}
				else{
					if (inSF != null){						
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
						//TODO: ojo con los hijos
//							inSF.setNodes(nodes);
						inSF.getListView().invalidateViews();						
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
					if (fbF != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						fbF.setOrder(orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();						
					}
				}
				else{
					if (fbF != null){						
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
						fbF.setOrder(orderGetChildren);
						fbF.setNodes(nodes);
						fbF.getListView().invalidateViews();					
					}
				}
			}
			else if (drawerItem == DrawerItem.RUBBISH_BIN){
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleRubbish);
				if (parentNode != null){
					if (rbF != null){
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						rbF.setOrder(orderGetChildren);
						rbF.setNodes(nodes);
						rbF.getListView().invalidateViews();
					}
				}
				else{
					if (rbF != null){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
						rbF.setOrder(orderGetChildren);
						rbF.setNodes(nodes);
						rbF.getListView().invalidateViews();						
					}
				}
			}
			else if (drawerItem == DrawerItem.SHARED_WITH_ME){
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
				if (parentNode != null){
					if (inSF != null){
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						inSF.setOrder(orderGetChildren);
						//TODO: ojo con los hijos
//							inSF.setNodes(nodes);
						inSF.getListView().invalidateViews();
					}
				}
				else{
					if (inSF != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
						inSF.setOrder(orderGetChildren);
						//TODO: ojo con los hijos
//							inSF.setNodes(nodes);
						inSF.getListView().invalidateViews();
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
		log("onIntentProcessed");
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
			parentHandle = fbF.getParentHandle();
			parentNode = megaApi.getNodeByHandle(parentHandle);
		}
		else if (drawerItem == DrawerItem.SHARED_WITH_ME){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){				
				//OUTGOING				
				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
				log("Tag: "+ cFTag2);
				outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag2);
				if (outSF != null){					
					parentHandleOutgoing = outSF.getParentHandle();
					parentNode = megaApi.getNodeByHandle(parentHandleOutgoing);
				}
			}
			else{			
				//InCOMING
				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
				log("Tag: "+ cFTag1);
				inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag1);
				if (inSF != null){					
					parentHandleIncoming = inSF.getParentHandle();	
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
		cF = (ContactsFragment) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cF != null){
			if (drawerItem == DrawerItem.CONTACTS){					
				cF.updateView();
			}
		}
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> updatedNodes) {
		log("onNodesUpdate");
		try { 
			statusDialog.dismiss();	
		} 
		catch (Exception ex) {}
		
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			if (fbF != null){
			
				if (fbF.isVisible()){
					ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(fbF.getParentHandle()), orderGetChildren);
					fbF.setNodes(nodes);
					fbF.setContentText();
					fbF.getListView().invalidateViews();
				}
			}
		}
		if (drawerItem == DrawerItem.INBOX){
			log("INBOX shown");
			if (iF != null){
				iF.refresh();
//				iF.getListView().invalidateViews();
			}
		}
		if (rbF != null){
			if (drawerItem == DrawerItem.RUBBISH_BIN){
				if (isClearRubbishBin){
					isClearRubbishBin = false;
					parentHandleRubbish = megaApi.getRubbishNode().getHandle();
					aB.setTitle(getString(R.string.section_rubbish_bin));	
					getmDrawerToggle().setDrawerIndicatorEnabled(true);

					if(rbF.isVisible())
					{
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
						rbF.setParentHandle(megaApi.getRubbishNode().getHandle());
						rbF.setNodes(nodes);
						rbF.getListView().invalidateViews();
					}
				}
				else{
					if(rbF.isVisible())
					{
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(rbF.getParentHandle()), orderGetChildren);
						rbF.setNodes(nodes);
						rbF.setContentText();
						rbF.getListView().invalidateViews();
					}
				}				
			}
		}
		
		if (drawerItem == DrawerItem.SHARED_WITH_ME){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){				
				//OUTGOING				
				String cFTag2 = getFragmentTag(R.id.shares_tabs_pager, 1);		
				log("Tag: "+ cFTag2);
				outSF = (OutgoingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag2);
				if (outSF != null){					
					aB.setTitle(getString(R.string.section_shared_items));				
					outSF.refresh(this.parentHandleOutgoing);				
				}
			}
			else{			
				//InCOMING
				String cFTag1 = getFragmentTag(R.id.shares_tabs_pager, 0);	
				log("Tag: "+ cFTag1);
				inSF = (IncomingSharesFragment) getSupportFragmentManager().findFragmentByTag(cFTag1);
				if (inSF != null){					
					aB.setTitle(getString(R.string.section_shared_items));	
					inSF.refresh(this.parentHandleIncoming);			
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
		if (cF != null){
			if (drawerItem == DrawerItem.CONTACTS){
				log("Share finish");
				cF.updateView();
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
		if (fbF != null){
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
			
			fbF.setTransfers(mTHash);
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
		if (fbF != null){
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
			
			fbF.setTransfers(mTHash);
		}
		
		if (inSF != null){
			for(int i=0; i<tL.size(); i++){
				
				MegaTransfer tempT = tL.get(i);
				if (tempT.getType() == MegaTransfer.TYPE_DOWNLOAD){
					long handleT = tempT.getNodeHandle();
					
					mTHash.put(handleT,tempT);						
				}
			}
			
			inSF.setTransfers(mTHash);
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
		if (fbF != null){
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
			fbF.setTransfers(mTHash);	
		}
		
		if (inSF != null){
			for(int i=0; i<tL.size(); i++){
				
				MegaTransfer tempT = tL.get(i);
				if (tempT.getType() == MegaTransfer.TYPE_DOWNLOAD){
					long handleT = tempT.getNodeHandle();
	
					mTHash.put(handleT,tempT);						
				}
			}
			
			inSF.setTransfers(mTHash);
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
		
		if (drawerItem == DrawerItem.TRANSFERS){
			Time now = new Time();
			now.setToNow();
			long nowMillis = now.toMillis(false);
			if (lastTimeOnTransferUpdate < 0){
				lastTimeOnTransferUpdate = now.toMillis(false);
				tF.setCurrentTransfer(transfer);
			}
			else if ((nowMillis - lastTimeOnTransferUpdate) > Util.ONTRANSFERUPDATE_REFRESH_MILLIS){
				lastTimeOnTransferUpdate = nowMillis;
				tF.setCurrentTransfer(transfer);
			}
		}

		if (fbF != null){
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				if (transfer.getType() == MegaTransfer.TYPE_DOWNLOAD){
					Time now = new Time();
					now.setToNow();
					long nowMillis = now.toMillis(false);
					if (lastTimeOnTransferUpdate < 0){
						lastTimeOnTransferUpdate = now.toMillis(false);
						fbF.setCurrentTransfer(transfer);
					}
					else if ((nowMillis - lastTimeOnTransferUpdate) > Util.ONTRANSFERUPDATE_REFRESH_MILLIS){
						lastTimeOnTransferUpdate = nowMillis;
						fbF.setCurrentTransfer(transfer);
					}			
				}		
			}
		}
		
		if (inSF != null){
			if (drawerItem == DrawerItem.SHARED_WITH_ME){
				if (transfer.getType() == MegaTransfer.TYPE_DOWNLOAD){
					Time now = new Time();
					now.setToNow();
					long nowMillis = now.toMillis(false);
					if (lastTimeOnTransferUpdate < 0){
						lastTimeOnTransferUpdate = now.toMillis(false);
						inSF.setCurrentTransfer(transfer);
					}
					else if ((nowMillis - lastTimeOnTransferUpdate) > Util.ONTRANSFERUPDATE_REFRESH_MILLIS){
						lastTimeOnTransferUpdate = nowMillis;
						inSF.setCurrentTransfer(transfer);
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
		Util.log("ManagerActivity", message);
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		log("onRequestUpdate: "  + request.getRequestString());		
	}
	
	public void downloadTo(String parentPath, String url, long size, long [] hashes){
		log("downloadTo");
		double availableFreeSpace = Double.MAX_VALUE;
		try{
			StatFs stat = new StatFs(parentPath);
			availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
		}
		catch(Exception ex){}		
		
		if (hashes == null){
			if(url != null) {
				if(availableFreeSpace < size) {
					Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space), false, this);
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
			if(hashes.length == 1){
				MegaNode tempNode = megaApi.getNodeByHandle(hashes[0]);
				if((tempNode != null) && tempNode.getType() == MegaNode.TYPE_FILE){
					log("ISFILE");
					String localPath = Util.getLocalFile(this, tempNode.getName(), tempNode.getSize(), parentPath);
					
					if(localPath != null){	
						try { 
							Util.copyFile(new File(localPath), new File(parentPath, tempNode.getName())); 
						}
						catch(Exception e) {}
												
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
							
		    			    File zipFile = new File(localPath);
		    			    
		    			    Intent intentZip = new Intent();
		    			    intentZip.setClass(this, ZipBrowserActivity.class);
		    			    intentZip.putExtra(ZipBrowserActivity.EXTRA_PATH_ZIP, zipFile.getAbsolutePath());
		    			    intentZip.putExtra(ZipBrowserActivity.EXTRA_HANDLE_ZIP, tempNode.getHandle());

		    				this.startActivity(intentZip);
							
						}
						else{							
							Intent viewIntent = new Intent(Intent.ACTION_VIEW);
							viewIntent.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
							if (isIntentAvailable(this, viewIntent))
								startActivity(viewIntent);
							else{
								Intent intentShare = new Intent(Intent.ACTION_SEND);
								intentShare.setDataAndType(Uri.fromFile(new File(localPath)), MimeTypeList.typeForName(tempNode.getName()).getType());
								if (isIntentAvailable(this, intentShare))
									startActivity(intentShare);
								String toastMessage = getString(R.string.general_already_downloaded) + ": " + localPath;
								Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
							}								
						}
						return;
					}
				}
			}
			
			for (long hash : hashes) {
				MegaNode node = megaApi.getNodeByHandle(hash);
				if(node != null){
					Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
					if (node.getType() == MegaNode.TYPE_FOLDER) {
						getDlList(dlFiles, node, new File(parentPath, new String(node.getName())));
					} else {
						dlFiles.put(node, parentPath);
					}
					
					for (MegaNode document : dlFiles.keySet()) {
						
						String path = dlFiles.get(document);
						
						if(availableFreeSpace < document.getSize()){
							Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space) + " (" + new String(document.getName()) + ")", false, this);
							continue;
						}
						
						Intent service = new Intent(this, DownloadService.class);
						service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
						service.putExtra(DownloadService.EXTRA_URL, url);
						service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
						service.putExtra(DownloadService.EXTRA_PATH, path);
						startService(service);
					}
				}
				else if(url != null) {
					if(availableFreeSpace < size) {
						Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space), false, this);
						continue;
					}
					
					Intent service = new Intent(this, DownloadService.class);
					service.putExtra(DownloadService.EXTRA_HASH, hash);
					service.putExtra(DownloadService.EXTRA_URL, url);
					service.putExtra(DownloadService.EXTRA_SIZE, size);
					service.putExtra(DownloadService.EXTRA_PATH, parentPath);
					startService(service);
				}
				else {
					log("node not found");
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
	
	public void shareFolder(MegaNode node){
		log("shareFolder MegaNode");
		
		if((drawerItem == DrawerItem.SHARED_WITH_ME) || (drawerItem == DrawerItem.CLOUD_DRIVE) ){
									
			Intent intent = new Intent(ContactsExplorerActivity.ACTION_PICK_CONTACT_SHARE_FOLDER);
	    	intent.setClass(this, ContactsExplorerActivity.class);
	    	//Multiselect=0
	    	intent.putExtra("MULTISELECT", 0);
	    	intent.putExtra("SEND_FILE",0);
	    	intent.putExtra(ContactsExplorerActivity.EXTRA_NODE_HANDLE, node.getHandle());
	    	startActivityForResult(intent, REQUEST_CODE_SELECT_CONTACT);
		}			
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
	
	public void showUpAF(){
		
//		Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("maF");
//        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
//        fragTransaction.detach(currentFragment);
//        fragTransaction.commit();
//
//        fragTransaction = getSupportFragmentManager().beginTransaction();
//        fragTransaction.attach(currentFragment);
//        fragTransaction.commit();
		
		accountFragment=UPGRADE_ACCOUNT_FRAGMENT;
		
		mTabHostContacts.setVisibility(View.GONE);    			
		viewPagerContacts.setVisibility(View.GONE); 
		mTabHostShares.setVisibility(View.GONE);    			
		mTabHostShares.setVisibility(View.GONE);
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(upAF==null){
			upAF = new UpgradeAccountFragment();
			ft.replace(R.id.fragment_container, upAF, "upAF");
			ft.commit();
		}
		else{			
			ft.replace(R.id.fragment_container, upAF, "upAF");
			ft.commit();
		}
	}
	
	public void showpF(int type, ArrayList<Product> accounts){
		showpF(type, accounts, false);
	}
	
	public void showpF(int type, ArrayList<Product> accounts, boolean refresh){
		log("showpF");
		
		accountFragment=PAYMENT_FRAGMENT;
		
		mTabHostContacts.setVisibility(View.GONE);    			
		viewPagerContacts.setVisibility(View.GONE); 
		mTabHostShares.setVisibility(View.GONE);    			
		mTabHostShares.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (!refresh){
			if(pF==null){			
				pF = new PaymentFragment();
				pF.setInfo(type, accounts);
				ft.replace(R.id.fragment_container, pF, "pF");
				ft.commit();
			}
			else{			
				pF.setInfo(type, accounts);			
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
					pF.setInfo(type, accounts);
					ft.replace(R.id.fragment_container, pF, "pF");
					ft.commit();
				}
				else{			
					pF.setInfo(type, accounts);			
					ft.replace(R.id.fragment_container, pF, "pF");
					ft.commit();
				}
			}
		}
	}
	
	public void onUpgrade1Click(View view) {
		if (upAF != null){
			showpF(1, upAF.getAccounts());
		}
	}

	public void onUpgrade2Click(View view) {
		if (upAF != null){
			showpF(2, upAF.getAccounts());
		}
	}

	public void onUpgrade3Click(View view) {
		if (upAF != null){
			showpF(3, upAF.getAccounts());
		}
	}
	
	public void onUpgradeLiteClick(View view){
		if (upAF != null){
			showpF(4, upAF.getAccounts());
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
		selectDrawerItem(drawerItem);
	}
	
	public void showCC(int type, ArrayList<Product> accounts, int payMonth){
		showCC(type, accounts, payMonth, false);
	}
	
	public void showCC(int type, ArrayList<Product> accounts, int payMonth, boolean refresh){
		accountFragment = CC_FRAGMENT;
		mTabHostContacts.setVisibility(View.GONE);    			
		viewPagerContacts.setVisibility(View.GONE); 
		mTabHostShares.setVisibility(View.GONE);    			
		mTabHostShares.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (!refresh){
			if (ccF == null){
				ccF = new CreditCardFragment();
				ccF.setInfo(type, accounts, payMonth);
				ft.replace(R.id.fragment_container, ccF, "ccF");
				ft.commit();
			}
			else{			
				ccF.setInfo(type, accounts, payMonth);			
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
					ccF.setInfo(type, accounts, payMonth);
					ft.replace(R.id.fragment_container, ccF, "ccF");
					ft.commit();
				}
				else{			
					ccF.setInfo(type, accounts, payMonth);			
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
    	showUpAF();
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

	@Override
	public void onAccountUpdate(MegaApiJava api) {
		// TODO Auto-generated method stub
		
	}
}
