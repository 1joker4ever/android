package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mega.privacy.android.app.AndroidCompletedTransfer;
import mega.privacy.android.app.CameraSyncService;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaAttributes;
import mega.privacy.android.app.MegaContactAdapter;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.MimeTypeThumbnail;
import mega.privacy.android.app.R;
import mega.privacy.android.app.ShareInfo;
import mega.privacy.android.app.UploadService;
import mega.privacy.android.app.UserCredentials;
import mega.privacy.android.app.components.EditTextCursorWatcher;
import mega.privacy.android.app.components.EditTextPIN;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.fcm.ChatAdvancedNotificationBuilder;
import mega.privacy.android.app.fcm.ContactsAdvancedNotificationBuilder;
import mega.privacy.android.app.lollipop.adapters.CloudDrivePagerAdapter;
import mega.privacy.android.app.lollipop.adapters.ContactsPageAdapter;
import mega.privacy.android.app.lollipop.adapters.MyAccountPageAdapter;
import mega.privacy.android.app.lollipop.adapters.SharesPageAdapter;
import mega.privacy.android.app.lollipop.adapters.TransfersPageAdapter;
import mega.privacy.android.app.lollipop.controllers.AccountController;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.listeners.ContactNameListener;
import mega.privacy.android.app.lollipop.listeners.CreateChatToPerformActionListener;
import mega.privacy.android.app.lollipop.listeners.CreateGroupChatWithTitle;
import mega.privacy.android.app.lollipop.listeners.FabButtonListener;
import mega.privacy.android.app.lollipop.listeners.MultipleAttachChatListener;
import mega.privacy.android.app.lollipop.managerSections.CameraUploadFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.CentiliFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.CompletedTransfersFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.ContactsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.CreditCardFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.ExportRecoveryKeyFragment;
import mega.privacy.android.app.lollipop.managerSections.FileBrowserFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.FortumoFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.InboxFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.IncomingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.MonthlyAnnualyFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.MyAccountFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.MyStorageFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.OfflineFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.OutgoingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.ReceivedRequestsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.RubbishBinFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.SearchFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.SentRequestsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.SettingsFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.TransfersFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.TurnOnNotificationsFragment;
import mega.privacy.android.app.lollipop.managerSections.UpgradeAccountFragmentLollipop;
import mega.privacy.android.app.lollipop.megaachievements.AchievementsActivity;
import mega.privacy.android.app.lollipop.megachat.BadgeDrawerArrowDrawable;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatSettings;
import mega.privacy.android.app.lollipop.megachat.RecentChatsFragmentLollipop;
import mega.privacy.android.app.lollipop.qrcode.QRCodeActivity;
import mega.privacy.android.app.lollipop.qrcode.ScanCodeFragment;
import mega.privacy.android.app.lollipop.tasks.CheckOfflineNodesTask;
import mega.privacy.android.app.lollipop.tasks.FilePrepareTask;
import mega.privacy.android.app.lollipop.tasks.FillDBContactsTask;
import mega.privacy.android.app.modalbottomsheet.ContactsBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.MyAccountBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.NodeOptionsBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.OfflineOptionsBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.ReceivedRequestBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.RecoveryKeyBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.SentRequestBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.TransfersBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.UploadBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.ChatBottomSheetDialogFragment;
import mega.privacy.android.app.snackbarListeners.SnackbarNavigateOption;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.Util;
import mega.privacy.android.app.utils.billing.IabHelper;
import mega.privacy.android.app.utils.billing.IabResult;
import mega.privacy.android.app.utils.billing.Inventory;
import mega.privacy.android.app.utils.billing.Purchase;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatCallListenerInterface;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatListenerInterface;
import nz.mega.sdk.MegaChatPeerList;
import nz.mega.sdk.MegaChatPresenceConfig;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaEvent;
import nz.mega.sdk.MegaFolderInfo;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaHandleList;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferData;
import nz.mega.sdk.MegaTransferListenerInterface;
import nz.mega.sdk.MegaUser;
import nz.mega.sdk.MegaUtilsAndroid;

public class ManagerActivityLollipop extends PinActivityLollipop implements MegaRequestListenerInterface, MegaChatListenerInterface, MegaChatCallListenerInterface,MegaChatRequestListenerInterface, OnNavigationItemSelectedListener, MegaGlobalListenerInterface, MegaTransferListenerInterface, OnClickListener,
			NodeOptionsBottomSheetDialogFragment.CustomHeight, ContactsBottomSheetDialogFragment.CustomHeight, View.OnFocusChangeListener, View.OnLongClickListener{

	public int accountFragment;

	public long handleInviteContact = 0;

	public ArrayList<Integer> transfersInProgress;
	public MegaTransferData transferData;

	public long transferCallback = 0;

	String regex = "[*|\\?:\"<>\\\\\\\\/]";

	TransfersBottomSheetDialogFragment transfersBottomSheet = null;

	//GET PRO ACCOUNT PANEL
	LinearLayout getProLayout=null;
	TextView getProText;
	TextView leftCancelButton;
	TextView rightUpgradeButton;
	FloatingActionButton fabButton;

	AlertDialog evaluateAppDialog;

	MegaNode inboxNode = null;

	boolean mkLayoutVisible = false;

	MegaNode rootNode = null;

	NodeController nC;
	ContactController cC;
	AccountController aC;

	long[] searchDate = null;

	MegaNode selectedNode;
	MegaOffline selectedOfflineNode;
	MegaContactAdapter selectedUser;
	MegaContactRequest selectedRequest;

	public long selectedChatItemId;
//	String fullNameChat;

	private BadgeDrawerArrowDrawable badgeDrawable;

	//COLLECTION FAB BUTTONS
	CoordinatorLayout fabButtonsLayout;
	FloatingActionButton mainFabButtonChat;
	FloatingActionButton firstFabButtonChat;
	FloatingActionButton secondFabButtonChat;
	FloatingActionButton thirdFabButtonChat;
	private Animation openFabAnim,closeFabAnim,rotateLeftAnim,rotateRightAnim, collectionFABLayoutOut;
	boolean isFabOpen=false;
	//

	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;
	ChatSettings chatSettings = null;
	MegaAttributes attr = null;
	static ManagerActivityLollipop managerActivity = null;
	MegaApplication app = null;
	MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;
	Handler handler;
	DisplayMetrics outMetrics;
    float scaleText;
    FrameLayout fragmentContainer;
//	boolean tranfersPaused = false;
    Toolbar tB;
    ActionBar aB;
    AppBarLayout abL;

	int selectedPaymentMethod;
	int selectedAccountType;
	int displayedAccountType;

	int countUserAttributes=0;
	int errorUserAttibutes=0;

	ShareInfo infoManager;
	MegaNode parentNodeManager;

	boolean firstNavigationLevel = true;
    DrawerLayout drawerLayout;
    ArrayList<MegaUser> contacts = new ArrayList<>();
    ArrayList<MegaUser> visibleContacts = new ArrayList<>();

    public boolean openFolderFromSearch = false;

    public boolean openSettingsStorage = false;
    public boolean openSettingsQR = false;
	boolean newAccount = false;

	boolean showStorageAlmostFullDialog = true;

    int orientationSaved;

	public enum DrawerItem {
		CLOUD_DRIVE, SAVED_FOR_OFFLINE, CAMERA_UPLOADS, INBOX, SHARED_ITEMS, CONTACTS, SETTINGS, ACCOUNT, SEARCH, TRANSFERS, MEDIA_UPLOADS, CHAT;

		public String getTitle(Context context) {
			switch(this)
			{
				case CLOUD_DRIVE: return context.getString(R.string.section_cloud_drive);
				case SAVED_FOR_OFFLINE: return context.getString(R.string.section_saved_for_offline);
				case CAMERA_UPLOADS: return context.getString(R.string.section_photo_sync);
				case INBOX: return context.getString(R.string.section_inbox);
				case SHARED_ITEMS: return context.getString(R.string.section_shared_items);
				case CONTACTS: {
					context.getString(R.string.section_contacts);
				}
				case SETTINGS: return context.getString(R.string.action_settings);
				case ACCOUNT: return context.getString(R.string.section_account);
				case SEARCH: return context.getString(R.string.action_search);
				case TRANSFERS: return context.getString(R.string.section_transfers);
				case MEDIA_UPLOADS: return context.getString(R.string.section_secondary_media_uploads);
				case CHAT: return context.getString(R.string.section_chat);
			}
			return null;
		}
	}

	public boolean turnOnNotifications = false;

	static DrawerItem drawerItem = null;
	static DrawerItem lastDrawerItem = null;
	static MenuItem drawerMenuItem = null;
	NavigationView nV;
	RelativeLayout usedSpaceLayout;
	FrameLayout accountInfoFrame;
	TextView nVDisplayName;
	TextView nVEmail;
	RoundedImageView nVPictureProfile;
	TextView nVPictureProfileTextView;
	TextView usedSpaceTV;
	TextView totalSpaceTV;
	ProgressBar usedSpacePB;

    //Tabs in Shares
	TabLayout tabLayoutShares;
	SharesPageAdapter sharesPageAdapter;
    ViewPager viewPagerShares;

    //Tabs in Cloud
	TabLayout tabLayoutCloud;
	CloudDrivePagerAdapter cloudPageAdapter;
    ViewPager viewPagerCDrive;

	//Tabs in Contacts
	TabLayout tabLayoutContacts;
	ContactsPageAdapter contactsPageAdapter;
	ViewPager viewPagerContacts;

	//Tabs in My Account
	TabLayout tabLayoutMyAccount;
	MyAccountPageAdapter mTabsAdapterMyAccount;
	ViewPager viewPagerMyAccount;

	//Tabs in Transfers
	TabLayout tabLayoutTransfers;
	TransfersPageAdapter mTabsAdapterTransfers;
	ViewPager viewPagerTransfers;

	boolean firstTime = true;
//	String pathNavigation = "/";
	public String searchQuery = null;
	public boolean textSubmitted = false;
	public boolean textsearchQuery = false;
	boolean isSearching = false;
	ArrayList<MegaNode> searchNodes;
	public int levelsSearch = -1;
	boolean openLink = false;

	long lastTimeOnTransferUpdate = Calendar.getInstance().getTimeInMillis();

	public int orderCloud = MegaApiJava.ORDER_DEFAULT_ASC;
	public int orderContacts = MegaApiJava.ORDER_DEFAULT_ASC;
	public int orderOthers = MegaApiJava.ORDER_DEFAULT_ASC;
	public int orderCamera = MegaApiJava.ORDER_MODIFICATION_DESC;
//	private int orderOffline = MegaApiJava.ORDER_DEFAULT_ASC;
//	private int orderOutgoing = MegaApiJava.ORDER_DEFAULT_ASC;
//	private int orderIncoming = MegaApiJava.ORDER_DEFAULT_ASC;

	boolean firstTimeCam = false;
	private boolean isGetLink = false;
	private boolean isClearRubbishBin = false;
	private boolean moveToRubbish = false;
	private boolean restoreFromRubbish = false;

	private List<ShareInfo> filePreparedInfos;
	boolean megaContacts = true;
	String feedback;

//	private boolean isListCloudDrive = true;
//	private boolean isListOffline = true;
//	private boolean isListRubbishBin = true;
	public boolean isListCameraUploads = false;
//	public boolean isLargeGridCameraUploads = true;
	public boolean isSmallGridCameraUploads = false;

	//	private boolean isListInbox = true;
//	private boolean isListContacts = true;
//	private boolean isListIncoming = true;
//	private boolean isListOutgoing = true;
	public boolean passwordReminderFromMyAccount = false;

	public boolean isList = true;

	public long parentHandleBrowser;
	public long parentHandleRubbish;
	public long parentHandleIncoming;
	public boolean isSearchEnabled;
	public long parentHandleOutgoing;
	public long parentHandleSearch;
	public long parentHandleInbox;
	public String pathNavigationOffline;
	public int deepBrowserTreeIncoming = 0;
	public int deepBrowserTreeOutgoing = 0;
	int indexShares = -1;
	int indexCloud = -1;
	int indexContacts = -1;
//	int indexChat = -1;
	int indexAccount = -1;
	int indexTransfers = -1;

	//LOLLIPOP FRAGMENTS
    private FileBrowserFragmentLollipop fbFLol;
    private RubbishBinFragmentLollipop rubbishBinFLol;
    private OfflineFragmentLollipop oFLol;
    private InboxFragmentLollipop iFLol;
    private IncomingSharesFragmentLollipop inSFLol;
	private OutgoingSharesFragmentLollipop outSFLol;
	private ContactsFragmentLollipop cFLol;
	private ReceivedRequestsFragmentLollipop rRFLol;
	private SentRequestsFragmentLollipop sRFLol;
	private MyAccountFragmentLollipop maFLol;
	private MyStorageFragmentLollipop mStorageFLol;
	private TransfersFragmentLollipop tFLol;
	private CompletedTransfersFragmentLollipop completedTFLol;
	private SearchFragmentLollipop sFLol;
	private SettingsFragmentLollipop sttFLol;
	private CameraUploadFragmentLollipop muFLol;
	private UpgradeAccountFragmentLollipop upAFL;
	private MonthlyAnnualyFragmentLollipop myFL;
	private FortumoFragmentLollipop fFL;
	private CentiliFragmentLollipop ctFL;
	private CreditCardFragmentLollipop ccFL;
	private CameraUploadFragmentLollipop cuFL;

	private RecentChatsFragmentLollipop rChatFL;

	private TurnOnNotificationsFragment tonF;
	private ExportRecoveryKeyFragment eRKeyF;

	ProgressDialog statusDialog;

	private AlertDialog renameDialog;
	private AlertDialog newFolderDialog;
	private AlertDialog addContactDialog;
	private AlertDialog overquotaDialog;
	private AlertDialog permissionsDialog;
	private AlertDialog presenceStatusDialog;
	private AlertDialog openLinkDialog;
	private AlertDialog alertNotPermissionsUpload;
	private AlertDialog clearRubbishBinDialog;
	private AlertDialog downloadConfirmationDialog;
	private AlertDialog insertPassDialog;
	private AlertDialog changeUserAttributeDialog;
	private AlertDialog getLinkDialog;
	private AlertDialog setPinDialog;
	private AlertDialog alertDialogTransferOverquota;
	private AlertDialog alertDialogStorageAlmostFull;

	private MenuItem searchMenuItem;
	private MenuItem gridSmallLargeMenuItem;
	private MenuItem addContactMenuItem;
	private MenuItem addMenuItem;
//	private MenuItem pauseRestartTransfersItem;
	private MenuItem createFolderMenuItem;
	private MenuItem importLinkMenuItem;
	private MenuItem selectMenuItem;
	private MenuItem unSelectMenuItem;
	private MenuItem thumbViewMenuItem;
	private MenuItem refreshMenuItem;
	private MenuItem sortByMenuItem;
	private MenuItem helpMenuItem;
	private MenuItem upgradeAccountMenuItem;
	private MenuItem rubbishBinMenuItem;
	private MenuItem clearRubbishBinMenuitem;
	private MenuItem changePass;
	private MenuItem exportMK;
	private MenuItem removeMK;
	private MenuItem takePicture;
	private MenuItem searchByDate;
	private MenuItem cancelSubscription;
	private MenuItem killAllSessions;
	private MenuItem cancelAllTransfersMenuItem;
	private MenuItem playTransfersMenuIcon;
	private MenuItem pauseTransfersMenuIcon;
	private MenuItem logoutMenuItem;
	private MenuItem forgotPassMenuItem;
	private MenuItem newChatMenuItem;
	private MenuItem setStatusMenuItem;
	private MenuItem clearCompletedTransfers;
	private MenuItem scanQRcode;

	int fromTakePicture = -1;

	public static AlertDialog rememberPasswordDialog;
	private TextView rememberPasswordDialogText;
	private boolean passwordReminderDialogSkipped = false;
	private boolean passwordReminderDialogBlocked = false;
	private CheckBox showRememberPaswordCheckBox;
	private Button testPwdButton;
	private Button recoveryKeyButton;
	private Button dismissButton;
	private boolean rememberPasswordLogout = false;

	AlertDialog enable2FADialog;
	boolean isEnable2FADialogShown = false;
	Button enable2FAButton;
	Button skip2FAButton;
	AlertDialog verify2FADialog;
	boolean verify2FADialogIsShown = false;
	int verifyPin2FADialogType;
	private boolean is2FAEnabled = false;
	InputMethodManager imm;
	private EditTextPIN firstPin;
	private EditTextPIN secondPin;
	private EditTextPIN thirdPin;
	private EditTextPIN fourthPin;
	private EditTextPIN fifthPin;
	private EditTextPIN sixthPin;
	private StringBuilder sb = new StringBuilder();
	private String pin = null;
	private String newMail = null;
	private TextView pinError;
	private ProgressBar verify2faProgressBar;

	private boolean isFirstTime = true;
	private boolean isErrorShown = false;
	private boolean pinLongClick = false;

	boolean sendToChat = false;

	private BroadcastReceiver updateMyAccountReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			int actionType;

			if (intent != null){
				actionType = intent.getIntExtra("actionType", -1);

				if(actionType == Constants.UPDATE_GET_PRICING){
					log("BROADCAST TO UPDATE AFTER GET PRICING");
					//UPGRADE_ACCOUNT_FRAGMENT
					if(upAFL!=null && upAFL.isAdded()){
						upAFL.setPricing();
					}

					//MONTHLY_YEARLY_FRAGMENT
					if(myFL!=null && myFL.isAdded()){
						myFL.setPricing();
					}

					//CENTILI_FRAGMENT
					if(ctFL!=null && ctFL.isAdded()){
						ctFL.getPaymentId();
					}

					//FORTUMO_FRAGMENT
					if(fFL!=null && fFL.isAdded()){
						fFL.getPaymentId();
					}
				}
				else if(actionType == Constants.UPDATE_ACCOUNT_DETAILS){
					log("BROADCAST TO UPDATE AFTER UPDATE_ACCOUNT_DETAILS");
					if(!isFinishing()){

						updateAccountDetailsVisibleInfo();

						//Check if myAccount section is visible
						String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
						maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
						if(maFLol!=null && maFLol.isAdded()){
							log("Update the account fragment");
							maFLol.setAccountDetails();
						}

						String myStorageTag = getFragmentTag(R.id.my_account_tabs_pager, 1);
						mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myStorageTag);
						if(mStorageFLol!=null && mStorageFLol.isAdded()){
							log("Update the account fragment");
							mStorageFLol.setAccountDetails();
						}

						if(upAFL!=null && upAFL.isAdded()){
							upAFL.showAvailableAccount();
						}


						if(sttFLol!=null && sttFLol.isAdded()){
							sttFLol.setRubbishInfo();
						}
					}
				}
				else if(actionType == Constants.UPDATE_CREDIT_CARD_SUBSCRIPTION){
					log("BROADCAST TO UPDATE AFTER UPDATE_CREDIT_CARD_SUBSCRIPTION");
					updateCancelSubscriptions();
				}
				else if(actionType == Constants.UPDATE_PAYMENT_METHODS){
					log("BROADCAST TO UPDATE AFTER UPDATE_PAYMENT_METHODS");
				}
			}
		}
	};

	private BroadcastReceiver receiverUpdate2FA = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				boolean enabled = intent.getBooleanExtra("enabled", false);
				is2FAEnabled = enabled;
				if (sttFLol != null && sttFLol.isAdded()) {
					sttFLol.update2FAPreference(enabled);
				}
			}
		}
	};

	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			log("Network broadcast received!");
			int actionType;

			if (intent != null){
				actionType = intent.getIntExtra("actionType", -1);

				if(actionType == Constants.GO_OFFLINE){
					showOfflineMode();
				}
				else if(actionType == Constants.GO_ONLINE){
					showOnlineMode();
				}
			}
		}
	};

	private BroadcastReceiver receiverUpdatePosition = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int position;
			int adapterType;
			int actionType;
			ImageView imageDrag = null;

			if (intent != null){
				actionType = intent.getIntExtra("actionType", -1);


				position = intent.getIntExtra("position", -1);
				adapterType = intent.getIntExtra("adapterType", 0);

				if (position != -1){
					if (adapterType == Constants.RUBBISH_BIN_ADAPTER){
						if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = rubbishBinFLol.getImageDrag(position);
								if (rubbishBinFLol.imageDrag != null){
									rubbishBinFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									rubbishBinFLol.imageDrag = imageDrag;
									rubbishBinFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								rubbishBinFLol.updateScrollPosition(position);
							}
						}
					}
					else if (adapterType == Constants.INBOX_ADAPTER){
						if (iFLol != null && iFLol.isAdded()){
							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = iFLol.getImageDrag(position);
								if (iFLol.imageDrag != null){
									iFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									iFLol.imageDrag = imageDrag;
									iFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								iFLol.updateScrollPosition(position);
							}
						}
					}
					else if (adapterType == Constants.INCOMING_SHARES_ADAPTER){
						if (inSFLol != null && inSFLol.isAdded()){
							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = inSFLol.getImageDrag(position);
								if (inSFLol.imageDrag != null){
									inSFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									inSFLol.imageDrag = imageDrag;
									inSFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								inSFLol.updateScrollPosition(position);
							}
						}
					}
					else if (adapterType == Constants.OUTGOING_SHARES_ADAPTER){
						if (outSFLol != null && outSFLol.isAdded()){
							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = outSFLol.getImageDrag(position);
								if (outSFLol.imageDrag != null){
									outSFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									outSFLol.imageDrag = imageDrag;
									outSFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								outSFLol.updateScrollPosition(position);
							}
						}
					}
					else if (adapterType == Constants.SEARCH_ADAPTER){
						Long handle = intent.getLongExtra("handle", -1);
						if (sFLol != null && sFLol.isAdded()){
							ArrayList<MegaNode> listNodes = sFLol.getNodes();
							for (int i=0; i<listNodes.size(); i++){
								if (listNodes.get(i).getHandle() == handle){
									position = i;
									break;
								}
							}

							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = sFLol.getImageDrag(position);
								if (sFLol.imageDrag != null){
									sFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									sFLol.imageDrag = imageDrag;
									sFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								sFLol.updateScrollPosition(position);
							}
						}
					}
					else if (adapterType == Constants.FILE_BROWSER_ADAPTER){
						if (fbFLol != null && fbFLol.isAdded()){
							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = fbFLol.getImageDrag(position);
								if (fbFLol.imageDrag != null){
									fbFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									fbFLol.imageDrag = imageDrag;
									fbFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								fbFLol.updateScrollPosition(position);
							}
						}
					}
					else if (adapterType == Constants.PHOTO_SYNC_ADAPTER || adapterType == Constants.SEARCH_BY_ADAPTER) {
						Long handle = intent.getLongExtra("handle", -1);
						if (cuFL != null && cuFL.isAdded()){

							if (cuFL.getAdapterList() != null){
								ArrayList<CameraUploadFragmentLollipop.PhotoSyncHolder> listNodes = cuFL.getNodesArray();
								for (int i=0; i<listNodes.size(); i++){
									if (listNodes.get(i).getHandle() == handle){
										position = i;
										break;
									}
								}
							}
							else {
								ArrayList<MegaMonthPicLollipop> listNodes = cuFL.getMonthPics();
								ArrayList<Long> handles;
								int count = 0;
								boolean found = false;
								for (int i=0; i<listNodes.size(); i++){
									handles = listNodes.get(i).getNodeHandles();
									for (int j=0; j<handles.size(); j++){
										count++;
										String h1 = handles.get(j).toString();
										String h2 = handle.toString();
										if (h1.equals(h2)){
											position = count;
											found = true;
											break;
										}
									}
									count++;
									if (found){
										break;
									}
								}
							}

							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = cuFL.getImageDrag(position);
								if (cuFL.imageDrag != null){
									cuFL.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									cuFL.imageDrag = imageDrag;
									cuFL.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								cuFL.updateScrollPosition(position);
							}
						}
						else if (muFLol != null && muFLol.isAdded()){

							if (muFLol.getAdapterList() != null){
								ArrayList<CameraUploadFragmentLollipop.PhotoSyncHolder> listNodes = muFLol.getNodesArray();
								for (int i=0; i<listNodes.size(); i++){
									if (listNodes.get(i).getHandle() == handle){
										position = i;
										break;
									}
								}
							}
							else {
								ArrayList<MegaMonthPicLollipop> listNodes = muFLol.getMonthPics();
								ArrayList<Long> handles;
								int count = 0;
								boolean found = false;
								for (int i=0; i<listNodes.size(); i++){
									handles = listNodes.get(i).getNodeHandles();
									for (int j=0; j<handles.size(); j++){
										count++;
										String h1 = handles.get(j).toString();
										String h2 = String.valueOf(handle);
										if (h1.equals(h2)){
											position = count;
											found = true;
											break;
										}
									}
									count++;
									if (found){
										break;
									}
								}
							}

							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = muFLol.getImageDrag(position);
								if (muFLol.imageDrag != null){
									muFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									muFLol.imageDrag = imageDrag;
									muFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								muFLol.updateScrollPosition(position);
							}
						}
					}
					else if (adapterType == Constants.OFFLINE_ADAPTER){
						if (oFLol != null && oFLol.isAdded()){
							if (actionType == Constants.UPDATE_IMAGE_DRAG) {
								imageDrag = oFLol.getImageDrag(position);
								if (oFLol.imageDrag != null){
									oFLol.imageDrag.setVisibility(View.VISIBLE);
								}
								if (imageDrag != null){
									oFLol.imageDrag = imageDrag;
									oFLol.imageDrag.setVisibility(View.GONE);
								}
							}
							else if (actionType == Constants.SCROLL_TO_POSITION) {
								oFLol.updateScrollPosition(position);
							}
						}
					}

					if (imageDrag != null){
						int[] positionDrag = new int[2];
						int[] screenPosition = new int[4];
						imageDrag.getLocationOnScreen(positionDrag);

						screenPosition[0] = (imageDrag.getWidth() / 2) + positionDrag[0];
						screenPosition[1] = (imageDrag.getHeight() / 2) + positionDrag[1];
						screenPosition[2] = imageDrag.getWidth();
						screenPosition[3] = imageDrag.getHeight();

						Intent intent1 =  new Intent(Constants.BROADCAST_ACTION_INTENT_FILTER_UPDATE_IMAGE_DRAG);
						intent1.putExtra("screenPosition", screenPosition);
						LocalBroadcastManager.getInstance(managerActivity).sendBroadcast(intent1);
					}
				}
			}
		}
	};

	//Billing

	// (arbitrary) request code for the purchase flow
    public static final int RC_REQUEST = 10001;
    String orderId = "";

	IabHelper mHelper;
	// SKU for our subscription PRO_I monthly
    public static final String SKU_PRO_I_MONTH = "mega.android.pro1.onemonth";
    // SKU for our subscription PRO_I yearly
	public static final String SKU_PRO_I_YEAR = "mega.android.pro1.oneyear";
    // SKU for our subscription PRO_II monthly
	public static final String SKU_PRO_II_MONTH = "mega.android.pro2.onemonth";
    // SKU for our subscription PRO_II yearly
	public static final String SKU_PRO_II_YEAR = "mega.android.pro2.oneyear";
    // SKU for our subscription PRO_III monthly
	public static final String SKU_PRO_III_MONTH = "mega.android.pro3.onemonth";
    // SKU for our subscription PRO_III yearly
	public static final String SKU_PRO_III_YEAR = "mega.android.pro3.oneyear";
    // SKU for our subscription PRO_LITE monthly
	public static final String SKU_PRO_LITE_MONTH = "mega.android.prolite.onemonth";
    // SKU for our subscription PRO_LITE yearly
	public static final String SKU_PRO_LITE_YEAR = "mega.android.prolite.oneyear";

    Purchase proLiteMonthly;
    Purchase proLiteYearly;
    Purchase proIMonthly;
    Purchase proIYearly;
    Purchase proIIMonthly;
    Purchase proIIYearly;
    Purchase proIIIMonthly;
    Purchase proIIIYearly;
    Purchase maxP;

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
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO I Monthly!", null);
				}
            }
            else if (purchase.getSku().equals(SKU_PRO_I_YEAR)) {
                log("PRO I Yearly subscription purchased.");
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO I Yearly!", null);
				}
            }
            else if (purchase.getSku().equals(SKU_PRO_II_MONTH)) {
                log("PRO II Monthly subscription purchased.");
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO II Monthly!", null);
				}
            }
            else if (purchase.getSku().equals(SKU_PRO_II_YEAR)) {
                log("PRO II Yearly subscription purchased.");
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO II Yearly!", null);
				}
            }
            else if (purchase.getSku().equals(SKU_PRO_III_MONTH)) {
                log("PRO III Monthly subscription purchased.");
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO III Monthly!", null);
				}
            }
            else if (purchase.getSku().equals(SKU_PRO_III_YEAR)) {
                log("PRO III Yearly subscription purchased.");
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO III Yearly!", null);
				}
            }
            else if (purchase.getSku().equals(SKU_PRO_LITE_MONTH)) {
                log("PRO LITE Monthly subscription purchased.");
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO LITE Monthly!", null);
				}
            }
            else if (purchase.getSku().equals(SKU_PRO_LITE_YEAR)) {
                log("PRO LITE Yearly subscription purchased.");
				if (managerActivity != null){
					Util.showAlert(managerActivity, "Thank you for subscribing to PRO LITE Yearly!", null);
				}
            }

            if (managerActivity != null){
            	log("ORIGINAL JSON3:" + purchase.getOriginalJson() + ":::");
            	megaApi.submitPurchaseReceipt(purchase.getOriginalJson(), managerActivity);
            }
            else{
            	log("ORIGINAL JSON4:" + purchase.getOriginalJson() + ":::");
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
			proIIYearly = inventory.getPurchase(SKU_PRO_II_YEAR);
			proIIIMonthly = inventory.getPurchase(SKU_PRO_III_MONTH);
			proIIIYearly = inventory.getPurchase(SKU_PRO_III_YEAR);

			if (proLiteMonthly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	        		if (proLiteMonthly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(0);
				((MegaApplication) getApplication()).getMyAccountInfo().setProLiteMonthly(proLiteMonthly);
				maxP = proLiteMonthly;
//	        		}
//            	}
				log("PRO LITE MONTHLY (JSON): __*" + proLiteMonthly.getOriginalJson() + "*__");
			}

			if (proLiteYearly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	            	if (proLiteYearly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(0);
				((MegaApplication) getApplication()).getMyAccountInfo().setProLiteYearly(proLiteYearly);
				maxP = proLiteYearly;
//	        		}
//            	}
				log("PRO LITE ANNUALY (JSON): __*" + proLiteYearly.getOriginalJson() + "*__");
			}

			if (proIMonthly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	            	if (proIMonthly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(1);
				((MegaApplication) getApplication()).getMyAccountInfo().setProIMonthly(proIMonthly);
				maxP = proIMonthly;
//	        		}
//            	}
				log("PRO I MONTHLY (JSON): __*" + proIMonthly.getOriginalJson() + "*__");
			}

			if (proIYearly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	            	if (proIYearly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(1);
				((MegaApplication) getApplication()).getMyAccountInfo().setProIYearly(proIYearly);
				maxP = proIYearly;
//	        		}
//            	}
				log("PRO I ANNUALY (JSON): __*" + proIYearly.getOriginalJson() + "*__");
			}

			if (proIIMonthly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	            	if (proIIMonthly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(2);
				((MegaApplication) getApplication()).getMyAccountInfo().setProIIMonthly(proIIMonthly);
				maxP = proIIMonthly;
//	        		}
//            	}
				log("PRO II MONTHLY (JSON): __*" + proIIMonthly.getOriginalJson() + "*__");
			}

			if (proIIYearly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	            	if (proIIYearly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(2);
				((MegaApplication) getApplication()).getMyAccountInfo().setProIIYearly(proIIYearly);
				maxP = proIIYearly;
//	        		}
//            	}
				log("PRO II ANNUALY (JSON): __*" + proIIYearly.getOriginalJson() + "*__");
			}

			if (proIIIMonthly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	            	if (proIIIMonthly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(3);
				maxP = proIIIMonthly;
				((MegaApplication) getApplication()).getMyAccountInfo().setProIIIMonthly(proIIIMonthly);
//	        		}
//            	}
				log("PRO III MONTHLY (JSON): __*" + proIIIMonthly.getOriginalJson() + "*__");
			}

			if (proIIIYearly != null){
//            	if (megaApi.getMyUser().getEmail() != null){
//	            	if (proIIIYearly.getDeveloperPayload().compareTo(megaApi.getMyUser().getEmail()) == 0){
				((MegaApplication) getApplication()).getMyAccountInfo().setLevelInventory(3);
				((MegaApplication) getApplication()).getMyAccountInfo().setProIIIYearly(proIIIYearly);
				maxP = proIIIYearly;
//	        		}
//            	}
				log("PRO III ANNUALY (JSON): __*" + proIIIYearly.getOriginalJson() + "*__");
			}

			((MegaApplication) getApplication()).getMyAccountInfo().setInventoryFinished(true);

			if (upAFL != null && upAFL.isAdded()) {
				upAFL.setPricing();
			}

			if (myFL != null && myFL.isAdded()) {
				myFL.setPricing();
			}

			log("LEVELACCOUNTDETAILS: " + ((MegaApplication) getApplication()).getMyAccountInfo().getLevelAccountDetails() + "; LEVELINVENTORY: " + ((MegaApplication) getApplication()).getMyAccountInfo().getLevelInventory() + "; ACCOUNTDETAILSFINISHED: " + ((MegaApplication) getApplication()).getMyAccountInfo().isAccountDetailsFinished());

			if (((MegaApplication) getApplication()).getMyAccountInfo().isAccountDetailsFinished()){
				if (((MegaApplication) getApplication()).getMyAccountInfo().getLevelInventory() > ((MegaApplication) getApplication()).getMyAccountInfo().getLevelAccountDetails()){
					if (maxP != null){
						log("ORIGINAL JSON1:" + maxP.getOriginalJson() + ":::");
						megaApi.submitPurchaseReceipt(maxP.getOriginalJson(), managerActivity);
					}
				}
			}

			boolean isProLiteMonthly = false;
			if (proLiteMonthly != null){
				isProLiteMonthly = true;
			}
			if (isProLiteMonthly){
				log("PRO LITE IS SUBSCRIPTED: ORDERID: ***____" + proLiteMonthly.getOrderId() + "____*****");
			}else{
				log("PRO LITE IS NOT SUBSCRIPTED");
			}

			if (!mHelper.subscriptionsSupported()) {
				log("SUBSCRIPTIONS NOT SUPPORTED");
			}
			else{
				log("SUBSCRIPTIONS SUPPORTED");
			}


			log("Initial inventory query finished.");
		}
	};

    public void launchPayment(String productId){
    	/* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
    	String payload = megaApi.getMyUser().getEmail();

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
    	else if (productId.compareTo(SKU_PRO_II_YEAR) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_II_YEAR, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
    	}
    	else if (productId.compareTo(SKU_PRO_III_MONTH) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_III_MONTH, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
    	}
    	else if (productId.compareTo(SKU_PRO_III_YEAR) == 0){
    		mHelper.launchPurchaseFlow(this,
    				SKU_PRO_III_YEAR, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
    	}
    	else if (productId.compareTo(SKU_PRO_LITE_MONTH) == 0){
    		log("LAUNCH PURCHASE FLOW!");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		log("onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
			case Constants.REQUEST_READ_CONTACTS:{
				log("REQUEST_READ_CONTACTS");
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					boolean hasReadContactsPermissions = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
					if (hasReadContactsPermissions){
						Intent phoneContactIntent = new Intent(this, PhoneContactsActivityLollipop.class);
						this.startActivity(phoneContactIntent);
					}
				}
				break;
			}
			case Constants.REQUEST_UPLOAD_CONTACT:{
				uploadContactInfo(infoManager, parentNodeManager);
				break;
			}
	        case Constants.REQUEST_CAMERA:{
				log("REQUEST_CAMERA PERMISSIONS");

	        	if (fromTakePicture==Constants.TAKE_PICTURE_OPTION){
					log("TAKE_PICTURE_OPTION");
		        	if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
		        		boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
		        		if (!hasStoragePermission){
		        			ActivityCompat.requestPermissions(this,
					                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
									Constants.REQUEST_WRITE_STORAGE);
		        		}
		        		else{
		        			this.takePicture();
		        			fromTakePicture = -1;
		        		}
		        	}
	        	}
				else if (fromTakePicture==Constants.TAKE_PROFILE_PICTURE){
					log("TAKE_PROFILE_PICTURE");
					if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
						boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
						if (!hasStoragePermission){
							ActivityCompat.requestPermissions(this,
									new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
									Constants.REQUEST_WRITE_STORAGE);
						}
						else{
							this.takeProfilePicture();
							fromTakePicture = -1;
						}
					}
				}
	        	break;
	        }
			case Constants.REQUEST_READ_WRITE_STORAGE:{
				log("REQUEST_READ_WRITE_STORAGE");
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					onGetReadWritePermission();
				}
			}
	        case Constants.REQUEST_WRITE_STORAGE:{
				log("REQUEST_WRITE_STORAGE PERMISSIONS");
	        	if (firstTimeCam){
					log("The first time");
	        		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

//						if (firstTimeCam){
//							firstTimeCam = false;
//						}

						if (fromTakePicture==Constants.TAKE_PICTURE_OPTION){
							log("TAKE_PICTURE_OPTION");
							boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
							if (!hasCameraPermission){
								ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA);
							}
							else{
								this.takePicture();
								fromTakePicture = -1;
							}
						}
						else if (fromTakePicture==Constants.TAKE_PROFILE_PICTURE){
							log("TAKE_PROFILE_PICTURE");
							boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
							if (!hasCameraPermission){
								ActivityCompat.requestPermissions(this,
										new String[]{Manifest.permission.CAMERA},
										Constants.REQUEST_CAMERA);
							}
							else{
								this.takeProfilePicture();
								fromTakePicture = -1;
							}
						}
						else{
							log("No option fromTakePicture: "+fromTakePicture);
						}
		        	}
	        	}
	        	else{
					if (fromTakePicture==Constants.TAKE_PICTURE_OPTION){
						log("TAKE_PICTURE_OPTION");
						boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
						if (!hasCameraPermission){
							ActivityCompat.requestPermissions(this,
									new String[]{Manifest.permission.CAMERA},
									Constants.REQUEST_CAMERA);
						}
						else{
							this.takePicture();
							fromTakePicture = -1;
						}
					}
					else if (fromTakePicture==Constants.TAKE_PROFILE_PICTURE){
						log("TAKE_PROFILE_PICTURE");
						boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
						if (!hasCameraPermission){
							ActivityCompat.requestPermissions(this,
									new String[]{Manifest.permission.CAMERA},
									Constants.REQUEST_CAMERA);
						}
						else{
							this.takeProfilePicture();
							fromTakePicture = -1;
						}
					}
					else{
						log("No option fromTakePicture: "+fromTakePicture);
						if(oFLol != null && oFLol.isAdded()){
							oFLol.notifyDataSetChanged();
						}
					}
				}
	        	break;
	        }
        }
//        switch (requestCode)
//        {
//            case REQUEST_WRITE_STORAGE: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                {
//                    //reload my activity with permission granted or use the features what required the permission
//                } else
//                {
//                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
//                }
//            }
//        }

    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState");
		if (drawerItem != null){
			log("DrawerItem = " + drawerItem);
		}
		else{
			log("DrawerItem is null");
		}
		super.onSaveInstanceState(outState);
		outState.putLong("parentHandleBrowser", parentHandleBrowser);
		outState.putLong("parentHandleRubbish", parentHandleRubbish);
		outState.putLong("parentHandleIncoming", parentHandleIncoming);
		log("IN BUNDLE -> parentHandleOutgoing: "+parentHandleOutgoing);
		outState.putLong("parentHandleOutgoing", parentHandleOutgoing);
		outState.putLong("parentHandleSearch", parentHandleSearch);
		outState.putLong("parentHandleInbox", parentHandleInbox);
		outState.putSerializable("drawerItem", drawerItem);
		outState.putBoolean("firstTimeCam",firstTimeCam);

		outState.putBoolean("isSearchEnabled", isSearchEnabled);
		outState.putLongArray("searchDate",searchDate);

		if(parentHandleIncoming!=-1){
			outState.putInt("deepBrowserTreeIncoming", deepBrowserTreeIncoming);
		}

		if(parentHandleOutgoing!=-1){
			outState.putInt("deepBrowserTreeOutgoing", deepBrowserTreeOutgoing);
		}

		if (viewPagerShares != null) {
			indexShares = viewPagerShares.getCurrentItem();
		}
		outState.putInt("indexShares", indexShares);

		if (viewPagerCDrive != null) {
			indexCloud = viewPagerCDrive.getCurrentItem();
		}
		outState.putInt("indexCloud", indexCloud);

		if (viewPagerContacts != null) {
			indexContacts = viewPagerContacts.getCurrentItem();
		}
		outState.putInt("indexContacts", indexContacts);

		if(oFLol!=null){
			pathNavigationOffline = oFLol.getPathNavigation();
		}
		outState.putString("pathNavigationOffline", pathNavigationOffline);
//		outState.putParcelable("obj", myClass);
		if(drawerItem==DrawerItem.ACCOUNT){
			outState.putInt("accountFragment", accountFragment);
			if(accountFragment==Constants.MONTHLY_YEARLY_FRAGMENT){
				outState.putInt("selectedAccountType", selectedAccountType);
				outState.putInt("selectedPaymentMethod", selectedPaymentMethod);
			}
		}

		if(searchQuery!=null){
			outState.putInt("levelsSearch", levelsSearch);
			outState.putString("searchQuery", searchQuery);
			textsearchQuery = true;
			outState.putBoolean("textsearchQuery", textsearchQuery);
		}else {
			textsearchQuery = false;
		}
		if (passwordReminderDialogBlocked){
			outState.putBoolean("passwordReminderDialogBlocked", true);
		}
		if (rememberPasswordLogout){
			outState.putBoolean("rememberPasswordLogout", true);
		}
		if (passwordReminderFromMyAccount){
			outState.putBoolean("passwordReminderFromMyAccount", true);
		}
		if (turnOnNotifications){
			outState.putBoolean("turnOnNotifications", turnOnNotifications);
		}

		outState.putInt("orientationSaved", orientationSaved);
		outState.putBoolean("verify2FADialogIsShown", verify2FADialogIsShown);
		outState.putInt("verifyPin2FADialogType", verifyPin2FADialogType);
		outState.putBoolean("isEnable2FADialogShown", isEnable2FADialogShown);
	}

	@Override
	public void onStart(){
		log("onStart");
		super.onStart();
	}

	@SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
		log("onCreate");
//		Fragments are restored during the Activity's onCreate().
//		Importantly though, they are restored in the base Activity class's onCreate().
//		Thus if you call super.onCreate() first, all of the rest of your onCreate() method will execute after your Fragments have been restored.
		super.onCreate(savedInstanceState);
		log("onCreate after call super");

		boolean selectDrawerItemPending = true;

		if(savedInstanceState!=null){
			log("Bundle is NOT NULL");
			parentHandleBrowser = savedInstanceState.getLong("parentHandleBrowser", -1);
			log("savedInstanceState -> parentHandleBrowser: "+parentHandleBrowser);
			parentHandleRubbish = savedInstanceState.getLong("parentHandleRubbish", -1);
			parentHandleIncoming = savedInstanceState.getLong("parentHandleIncoming", -1);
            log("savedInstanceState -> parentHandleIncoming: "+parentHandleIncoming);
			parentHandleOutgoing = savedInstanceState.getLong("parentHandleOutgoing", -1);
			log("savedInstanceState -> parentHandleOutgoing: "+parentHandleOutgoing);
			parentHandleSearch = savedInstanceState.getLong("parentHandleSearch", -1);
			parentHandleInbox = savedInstanceState.getLong("parentHandleInbox", -1);
			deepBrowserTreeIncoming = savedInstanceState.getInt("deepBrowserTreeIncoming", 0);
			deepBrowserTreeOutgoing = savedInstanceState.getInt("deepBrowserTreeOutgoing", 0);
			isSearchEnabled = savedInstanceState.getBoolean("isSearchEnabled");
			searchDate = savedInstanceState.getLongArray("searchDate");
			firstTimeCam = savedInstanceState.getBoolean("firstTimeCam");
			drawerItem = (DrawerItem) savedInstanceState.getSerializable("drawerItem");
			indexShares = savedInstanceState.getInt("indexShares", indexShares);
			log("savedInstanceState -> indexShares: "+indexShares);
			indexCloud = savedInstanceState.getInt("indexCloud", indexCloud);
			log("savedInstanceState -> indexCloud: "+indexCloud);
			indexContacts = savedInstanceState.getInt("indexContacts", 0);
			pathNavigationOffline = savedInstanceState.getString("pathNavigationOffline", pathNavigationOffline);
			log("savedInstanceState -> pathNavigationOffline: "+pathNavigationOffline);
			accountFragment = savedInstanceState.getInt("accountFragment", -1);
			selectedAccountType = savedInstanceState.getInt("selectedAccountType", -1);
			selectedPaymentMethod = savedInstanceState.getInt("selectedPaymentMethod", -1);
			searchQuery = savedInstanceState.getString("searchQuery");
			textsearchQuery = savedInstanceState.getBoolean("textsearchQuery");
			levelsSearch = savedInstanceState.getInt("levelsSearch");
			passwordReminderDialogBlocked = savedInstanceState.getBoolean("passwordReminderDialogBlocked", false);
			rememberPasswordLogout = savedInstanceState.getBoolean("rememberPasswordLogout", false);
			passwordReminderFromMyAccount = savedInstanceState.getBoolean("passwordReminderFromaMyAccount", false);
			turnOnNotifications = savedInstanceState.getBoolean("turnOnNotifications", false);
			orientationSaved = savedInstanceState.getInt("orientationSaved");
			verify2FADialogIsShown = savedInstanceState.getBoolean("verify2FADialogIsShown", false);
			verifyPin2FADialogType = savedInstanceState.getInt("verifyPin2FADialogType");
			isEnable2FADialogShown = savedInstanceState.getBoolean("isEnable2FADialogShown", false);
		}
		else{
			log("Bundle is NULL");
			parentHandleBrowser = -1;
			parentHandleRubbish = -1;
			parentHandleIncoming = -1;
			parentHandleOutgoing = -1;
			isSearchEnabled= false;
			parentHandleSearch = -1;
			parentHandleInbox = -1;
			indexContacts = -1;
			deepBrowserTreeIncoming = 0;
			deepBrowserTreeOutgoing = 0;

			this.setPathNavigationOffline("/");
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(receiverUpdatePosition, new IntentFilter(Constants.BROADCAST_ACTION_INTENT_FILTER_UPDATE_POSITION));
		LocalBroadcastManager.getInstance(this).registerReceiver(updateMyAccountReceiver, new IntentFilter(Constants.BROADCAST_ACTION_INTENT_UPDATE_ACCOUNT_DETAILS));
		LocalBroadcastManager.getInstance(this).registerReceiver(receiverUpdate2FA, new IntentFilter(Constants.BROADCAST_ACTION_INTENT_UPDATE_2FA_SETTINGS));
		LocalBroadcastManager.getInstance(this).registerReceiver(networkReceiver, new IntentFilter(Constants.BROADCAST_ACTION_INTENT_CONNECTIVITY_CHANGE));

		nC = new NodeController(this);
		cC = new ContactController(this);
		aC = new AccountController(this);

		File thumbDir;
		if (getExternalCacheDir() != null){
			thumbDir = new File (getExternalCacheDir(), "thumbnailsMEGA");
			thumbDir.mkdirs();
			log("------------------ThumbnailsMEGA folder created: "+thumbDir.getAbsolutePath());
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

		dbH = DatabaseHandler.getDbHandler(getApplicationContext());

		managerActivity = this;
		app = (MegaApplication)getApplication();
		megaApi = app.getMegaApi();
		if(Util.isChatEnabled()){
			megaChatApi = app.getMegaChatApi();
			log("addChatListener");
			megaChatApi.addChatListener(this);
			megaChatApi.addChatCallListener(this);
		}
		else{
			megaChatApi=null;
		}

		log("retryPendingConnections()");
		if (megaApi != null){
			log("---------retryPendingConnections");
			megaApi.retryPendingConnections();
		}

		if (megaChatApi != null){
			megaChatApi.retryPendingConnections(false, null);
		}

		transfersInProgress = new ArrayList<Integer>();

		Display display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = getResources().getDisplayMetrics().density;

	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
	    if (scaleH < scaleW){
	    	scaleText = scaleH;
	    }
	    else{
	    	scaleText = scaleW;
	    }

	    if (dbH.getEphemeral() != null){
            Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
            intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
		}

	    if (dbH.getCredentials() == null){
	    	Intent newIntent = getIntent();

	    	if (newIntent != null){
		    	if (newIntent.getAction() != null){
		    		if (newIntent.getAction().equals(Constants.ACTION_EXPORT_MASTER_KEY) || newIntent.getAction().equals(Constants.ACTION_OPEN_MEGA_LINK) || newIntent.getAction().equals(Constants.ACTION_OPEN_MEGA_FOLDER_LINK)){
		    			openLink = true;
		    		}
		    		else if (newIntent.getAction().equals(Constants.ACTION_CANCEL_CAM_SYNC)){
						Intent cancelTourIntent = new Intent(this, LoginActivityLollipop.class);
						cancelTourIntent.putExtra("visibleFragment", Constants. TOUR_FRAGMENT);
						cancelTourIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    			cancelTourIntent.setAction(newIntent.getAction());
		    			startActivity(cancelTourIntent);
		    			finish();
		    			return;
		    		}
		    	}
		    }

	    	if (!openLink){
//				megaApi.localLogout();
//				AccountController aC = new AccountController(this);
//				aC.logout(this, megaApi, megaChatApi, false);
				Intent intent = new Intent(this, LoginActivityLollipop.class);
				intent.putExtra("visibleFragment", Constants. TOUR_FRAGMENT);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
					finish();
				}

		    }

	    	return;
	    }

	    prefs = dbH.getPreferences();
		if (prefs == null){
			firstTime = true;
			isList=true;
			isListCameraUploads=false;
			isSmallGridCameraUploads = false;
		}
		else{

			if (prefs.getFirstTime() == null){
				firstTime = true;
				isListCameraUploads=false;
			}else{
				firstTime = Boolean.parseBoolean(prefs.getFirstTime());
			}
			if (prefs.getPreferredViewList() == null){
				isList = true;
			}
			else{
				isList = Boolean.parseBoolean(prefs.getPreferredViewList());
			}
			if (prefs.getPreferredViewListCameraUploads() == null){
				isListCameraUploads = false;
			}
			else{
				isListCameraUploads = Boolean.parseBoolean(prefs.getPreferredViewListCameraUploads());
			}

			isSmallGridCameraUploads = dbH.isSmallGridCamera();
		}
		log("Preferred View List: "+isList);
		log("Preferred View List for camera uploads: "+isListCameraUploads);

		if(prefs!=null){
			if(prefs.getPreferredSortCloud()!=null){
				orderCloud = Integer.parseInt(prefs.getPreferredSortCloud());
				log("The orderCloud preference is: "+orderCloud);
			}
			else{
				orderCloud = megaApi.ORDER_DEFAULT_ASC;
				log("Preference orderCloud is NULL -> ORDER_DEFAULT_ASC");
			}
			if(prefs.getPreferredSortContacts()!=null){
				orderContacts = Integer.parseInt(prefs.getPreferredSortContacts());
				log("The orderContacts preference is: "+orderContacts);
			}
			else{
				orderContacts = megaApi.ORDER_DEFAULT_ASC;
				log("Preference orderContacts is NULL -> ORDER_DEFAULT_ASC");
			}
			if(prefs.getPreferredSortOthers()!=null){
				orderOthers = Integer.parseInt(prefs.getPreferredSortOthers());
				log("The orderOthers preference is: "+orderOthers);
			}
			else{
				orderOthers = megaApi.ORDER_DEFAULT_ASC;
				log("Preference orderOthers is NULL -> ORDER_DEFAULT_ASC");
			}
		}
		else {
			log("Prefs is NULL -> ORDER_DEFAULT_ASC");
			orderCloud = megaApi.ORDER_DEFAULT_ASC;
			orderContacts = megaApi.ORDER_DEFAULT_ASC;
			orderOthers = megaApi.ORDER_DEFAULT_ASC;
		}
		getOverflowMenu();

		handler = new Handler();

		log("Set view");
		setContentView(R.layout.activity_manager);
//		long num = 11179220468180L;
//		dbH.setSecondaryFolderHandle(num);
		//Set toolbar
		abL = (AppBarLayout) findViewById(R.id.app_bar_layout);

		tB = (Toolbar) findViewById(R.id.toolbar);
		if(tB==null){
			log("Tb is Null");
			return;
		}

		tB.setVisibility(View.VISIBLE);
		setSupportActionBar(tB);
		aB = getSupportActionBar();

		aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);

        //Set navigation view
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        nV = (NavigationView) findViewById(R.id.navigation_view);
        nV.setNavigationItemSelectedListener(this);

		badgeDrawable = new BadgeDrawerArrowDrawable(getSupportActionBar().getThemedContext());

		usedSpaceLayout = (RelativeLayout) findViewById(R.id.nv_used_space_layout);

		View nVHeader = LayoutInflater.from(this).inflate(R.layout.nav_header, null);
		nV.addHeaderView(nVHeader);

		//FAB buttonaB.
		fabButton = (FloatingActionButton) findViewById(R.id.floating_button);
		fabButton.setOnClickListener(new FabButtonListener(this));

		//Collection of FAB for CHAT
		fabButtonsLayout = (CoordinatorLayout) findViewById(R.id.fab_collection_layout);
		mainFabButtonChat = (FloatingActionButton) findViewById(R.id.main_fab_chat);
		mainFabButtonChat.setOnClickListener(new FabButtonListener(this));
		firstFabButtonChat = (FloatingActionButton) findViewById(R.id.first_fab_chat);
		firstFabButtonChat.setOnClickListener(new FabButtonListener(this));
		secondFabButtonChat = (FloatingActionButton) findViewById(R.id.second_fab_chat);
		secondFabButtonChat.setOnClickListener(new FabButtonListener(this));
		thirdFabButtonChat = (FloatingActionButton) findViewById(R.id.third_fab_chat);
		thirdFabButtonChat.setOnClickListener(new FabButtonListener(this));

		collectionFABLayoutOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.collection_fab_layout_out);
		collectionFABLayoutOut.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				log("onAnimationEnd");
				fabButtonsLayout.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		openFabAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.open_fab);
		closeFabAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.close_fab);
		closeFabAnim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				log("onAnimationEnd");
//				mainFabButtonChat.setVisibility(View.GONE);
//				fabButtonsLayout.startAnimation(collectionFABLayoutOut);
				fabButtonsLayout.setVisibility(View.GONE);
				fabButton.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		rotateRightAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_right);
		rotateLeftAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_left);

		//PRO PANEL
		getProLayout=(LinearLayout) findViewById(R.id.get_pro_account);
		String getProTextString = getString(R.string.get_pro_account);
		try {
			getProTextString = getProTextString.replace("[A]", "\n");
		}
		catch(Exception e){
			log("Formatted string: " + getProTextString);
		}

		getProText= (TextView) findViewById(R.id.get_pro_account_text);
		getProText.setText(getProTextString);
		rightUpgradeButton = (TextView) findViewById(R.id.btnRight_upgrade);
		leftCancelButton = (TextView) findViewById(R.id.btnLeft_cancel);

		accountInfoFrame = (FrameLayout) nVHeader.findViewById(R.id.navigation_drawer_account_view);
        accountInfoFrame.setOnClickListener(this);

        nVDisplayName = (TextView) nVHeader.findViewById(R.id.navigation_drawer_account_information_display_name);
		nVEmail = (TextView) nVHeader.findViewById(R.id.navigation_drawer_account_information_email);
        nVPictureProfile = (RoundedImageView) nVHeader.findViewById(R.id.navigation_drawer_user_account_picture_profile);
        nVPictureProfileTextView = (TextView) nVHeader.findViewById(R.id.navigation_drawer_user_account_picture_profile_textview);

        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);
        usedSpaceTV = (TextView) findViewById(R.id.navigation_drawer_used_space);
        totalSpaceTV = (TextView) findViewById(R.id.navigation_drawer_total_space);
        usedSpacePB = (ProgressBar) findViewById(R.id.manager_used_space_bar);

		//TABS section Cloud Drive
		tabLayoutCloud =  (TabLayout) findViewById(R.id.sliding_tabs_cloud_drive);
		viewPagerCDrive = (ViewPager) findViewById(R.id.cloud_drive_tabs_pager);

		viewPagerCDrive.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			public void onPageScrollStateChanged(int state) {}
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

			public void onPageSelected(int position) {
				log("onTabChanged TabId :"+ position);
				supportInvalidateOptionsMenu();

				if(position == 0){
					if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
						if(rubbishBinFLol.isMultipleselect()){
							rubbishBinFLol.actionMode.finish();
						}
					}
				}
				else if(position == 1){

					fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
					if (fbFLol != null && fbFLol.isAdded()){
						if(fbFLol.isMultipleselect()){
							fbFLol.actionMode.finish();
						}
					}
				}
                setToolbarTitle();
				showFabButton();
			}
		});

		//TABS section Contacts
		tabLayoutContacts =  (TabLayout) findViewById(R.id.sliding_tabs_contacts);
		viewPagerContacts = (ViewPager) findViewById(R.id.contact_tabs_pager);
		viewPagerContacts.setOffscreenPageLimit(3);

		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			tabLayoutContacts.setTabMode(TabLayout.MODE_FIXED);
		}else{
			tabLayoutContacts.setTabMode(TabLayout.MODE_SCROLLABLE);
		}

		//TABS section Shared Items
		tabLayoutShares =  (TabLayout) findViewById(R.id.sliding_tabs_shares);
		viewPagerShares = (ViewPager) findViewById(R.id.shares_tabs_pager);

		viewPagerShares.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				log("selectDrawerItemSharedItems: TabId :"+ position);
				supportInvalidateOptionsMenu();
				if(position == 1){
					if (inSFLol != null && inSFLol.isAdded()){
						if(inSFLol.isMultipleselect()){
							inSFLol.actionMode.finish();
						}
					}
				}
				else if(position == 0){
					if (outSFLol != null && outSFLol.isAdded()){
						if(outSFLol.isMultipleselect()){
							outSFLol.actionMode.finish();
						}
					}
				}
				setToolbarTitle();
				showFabButton();
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		//Tab section MyAccount
		tabLayoutMyAccount =  (TabLayout) findViewById(R.id.sliding_tabs_my_account);
		viewPagerMyAccount = (ViewPager) findViewById(R.id.my_account_tabs_pager);

		//Tab section Transfers
		tabLayoutTransfers =  (TabLayout) findViewById(R.id.sliding_tabs_transfers);
		viewPagerTransfers = (ViewPager) findViewById(R.id.transfers_tabs_pager);


        if (!Util.isOnline(this)){
        	log("No network: SHOW OFFLINE MODE");

			if(drawerItem==null){
				drawerItem = DrawerItem.SAVED_FOR_OFFLINE;
			}

			selectDrawerItemLollipop(drawerItem);

			showOfflineMode();
			if(Util.isChatEnabled()){
				UserCredentials credentials = dbH.getCredentials();
				if(credentials!=null){
					String gSession = credentials.getSession();
					int ret = megaChatApi.getInitState();
					log("In Offline mode: init chat is: "+ret);
					if(ret==0||ret==MegaChatApi.INIT_ERROR){
						ret = megaChatApi.init(gSession);
						log("After init: "+ret);
					}
					else{
						log("Offline mode: Do not init, chat already initialized");
					}
				}
			}
			else{
				log("Offline mode: chat disabled");
			}
			return;
        }

		///Check the MK file
		int versionApp = Util.getVersion(this);
		log("-------------------Version app: "+versionApp);
		final String pathOldMK = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.oldMKFile;
		final File fMKOld = new File(pathOldMK);
		if (fMKOld != null) {
			if (fMKOld.exists()) {
				log("Old MK file need to be renamed!");
				aC.renameMK();
			}
		}

		rootNode = megaApi.getRootNode();
		if (rootNode == null){
			 if (getIntent() != null){
			 	log("Action: "+getIntent().getAction());
				if (getIntent().getAction() != null){
					if (getIntent().getAction().equals(Constants.ACTION_IMPORT_LINK_FETCH_NODES)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OPEN_MEGA_LINK)){
						Intent intent = new Intent(managerActivity, FileLinkActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_IMPORT_LINK_FETCH_NODES);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OPEN_MEGA_FOLDER_LINK)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_OPEN_MEGA_FOLDER_LINK);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_CANCEL_CAM_SYNC)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(getIntent().getAction());
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_EXPORT_MASTER_KEY)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(getIntent().getAction());
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_SHOW_TRANSFERS)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_SHOW_TRANSFERS);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_IPC)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_IPC);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
						startActivity(intent);
						finish();
						return;
					}
                    else if(getIntent().getAction().equals(Constants.ACTION_CHAT_SUMMARY)) {
                        Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
                        intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setAction(Constants.ACTION_CHAT_SUMMARY);
                        startActivity(intent);
                        finish();
                        return;
                    }
					else if (getIntent().getAction().equals(Constants.ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OPEN_HANDLE_NODE)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants.LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_OPEN_HANDLE_NODE);
						intent.setData(Uri.parse(getIntent().getDataString()));
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OVERQUOTA_TRANSFER)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants.LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_OVERQUOTA_TRANSFER);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OVERQUOTA_STORAGE)){
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants.LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_OVERQUOTA_STORAGE);
						startActivity(intent);
						finish();
						return;
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OPEN_CONTACTS_SECTION)){
						log("Login loin");
						handleInviteContact = getIntent().getLongExtra("handle", 0);

						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("handle", handleInviteContact);
						intent.putExtra("visibleFragment", Constants.LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.setAction(Constants.ACTION_OPEN_CONTACTS_SECTION);
						startActivity(intent);
						finish();
						return;
					}
				}
			}
			Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
			intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		}
		else{
			inboxNode = megaApi.getInboxNode();
			attr = dbH.getAttributes();
			if (attr != null){
				if (attr.getInvalidateSdkCache() != null){
					if (attr.getInvalidateSdkCache().compareTo("") != 0) {
						try {
							if (Boolean.parseBoolean(attr.getInvalidateSdkCache())){
								log("megaApi.invalidateCache();");
								megaApi.invalidateCache();
							}
						}
						catch(Exception e){}
					}
				}
			}

			dbH.setInvalidateSdkCache(false);

			String token = FirebaseInstanceId.getInstance().getToken();
			if (token != null) {
				log("FCM TOKEN: " + token);
				megaApi.registerPushNotifications(Constants.DEVICE_ANDROID, token, this);
//				Log.d("TOKEN___", token);

//				Toast.makeText(this, "TOKEN: _" + token + "_", Toast.LENGTH_LONG).show();
			}


			nVEmail.setVisibility(View.VISIBLE);
			nVEmail.setText(megaApi.getMyEmail());
//				megaApi.getUserData(this);
			if(((MegaApplication) getApplication()).getMyAccountInfo()!=null) {
				log("getUserAttribute FirstName");
				((MegaApplication) getApplication()).getMyAccountInfo().setFirstName(false);
				log("getUserAttribute LastName");
				((MegaApplication) getApplication()).getMyAccountInfo().setLastName(false);
			}
			megaApi.getUserAttribute(MegaApiJava.USER_ATTR_FIRSTNAME, this);

			megaApi.getUserAttribute(MegaApiJava.USER_ATTR_LASTNAME, this);

			this.setDefaultAvatar();

			this.setProfileAvatar();

			initGooglePlayPayments();

			megaApi.addGlobalListener(this);

			if(Util.isChatEnabled()){
				megaApi.shouldShowRichLinkWarning(this);
				megaApi.isRichPreviewsEnabled(this);
			}

			transferData = megaApi.getTransferData(this);
			int downloadsInProgress = transferData.getNumDownloads();
			int uploadsInProgress = transferData.getNumUploads();

            for(int i=0;i<downloadsInProgress;i++){
                transfersInProgress.add(transferData.getDownloadTag(i));
            }
            for(int i=0;i<uploadsInProgress;i++){
                transfersInProgress.add(transferData.getUploadTag(i));
            }

			if(savedInstanceState==null) {
				log("Run async task to check offline files");
				//Check the consistency of the offline nodes in the DB
				CheckOfflineNodesTask checkOfflineNodesTask = new CheckOfflineNodesTask(this);
				checkOfflineNodesTask.execute();
			}

	        if (getIntent() != null){
				if (getIntent().getAction() != null){
			        if (getIntent().getAction().equals(Constants.ACTION_EXPORT_MASTER_KEY)){
			        	log("Intent to export Master Key - im logged in!");
						drawerItem=DrawerItem.ACCOUNT;
						showMKLayout();
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
						return;
					}
					else if(getIntent().getAction().equals(Constants.ACTION_CANCEL_ACCOUNT)){
						String link = getIntent().getDataString();
						if(link!=null){
							log("link to cancel: "+link);
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							megaApi.queryCancelLink(link, this);
						}
					}
					else if(getIntent().getAction().equals(Constants.ACTION_CHANGE_MAIL)){
						String link = getIntent().getDataString();
						if(link!=null){
							log("link to change mail: "+link);
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							showDialogInsertPassword(link, false);
						}
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OPEN_FOLDER)) {
						log("Open after LauncherFileExplorerActivityLollipop ");
						boolean locationFileInfo = getIntent().getBooleanExtra("locationFileInfo", false);
						long handleIntent = getIntent().getLongExtra("PARENT_HANDLE", -1);

						if (locationFileInfo){
							boolean offlineAdapter = getIntent().getBooleanExtra("offline_adapter", false);
							if (offlineAdapter){
								drawerItem = DrawerItem.SAVED_FOR_OFFLINE;
								String pathNavigation = getIntent().getStringExtra("pathNavigation");
								setPathNavigationOffline(pathNavigation);
								selectDrawerItemLollipop(drawerItem);
								selectDrawerItemPending=false;
							}
							else {
								long fragmentHandle = getIntent().getLongExtra("fragmentHandle", -1);

								if (fragmentHandle == megaApi.getRootNode().getHandle()){
									drawerItem = DrawerItem.CLOUD_DRIVE;
									indexCloud = 0;
									setParentHandleBrowser(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
								else if (fragmentHandle == megaApi.getRubbishNode().getHandle()){
									drawerItem = DrawerItem.CLOUD_DRIVE;
									indexCloud = 1;
									setParentHandleRubbish(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
								else if (fragmentHandle == megaApi.getInboxNode().getHandle()){
									drawerItem = DrawerItem.INBOX;
									setParentHandleInbox(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
								else {
									//Incoming
									drawerItem = DrawerItem.SHARED_ITEMS;
									indexShares = 0;
									MegaNode parentIntentN = megaApi.getNodeByHandle(handleIntent);
									if (parentIntentN != null){
										deepBrowserTreeIncoming = MegaApiUtils.calculateDeepBrowserTreeIncoming(parentIntentN, this);
									}
									setParentHandleIncoming(handleIntent);
									selectDrawerItemLollipop(drawerItem);
									selectDrawerItemPending=false;
								}
							}
						}
						else {
							int access = -1;
							if (handleIntent != -1) {
								MegaNode parentIntentN = megaApi.getNodeByHandle(handleIntent);
								if (parentIntentN != null) {
									access = megaApi.getAccess(parentIntentN);
									switch (access) {
										case MegaShare.ACCESS_OWNER:
										case MegaShare.ACCESS_UNKNOWN: {
											log("The intent set the parentHandleBrowser to " + handleIntent);
											parentHandleBrowser = handleIntent;
											break;
										}
										case MegaShare.ACCESS_READ:
										case MegaShare.ACCESS_READWRITE:
										case MegaShare.ACCESS_FULL: {
											log("The intent set the parentHandleIncoming to " + handleIntent);
											parentHandleIncoming = handleIntent;
											drawerItem = DrawerItem.SHARED_ITEMS;
											deepBrowserTreeIncoming = MegaApiUtils.calculateDeepBrowserTreeIncoming(parentIntentN, this);
											log("After calculate deepBrowserTreeIncoming: "+deepBrowserTreeIncoming);
											break;
										}
										default: {
											log("DEFAULT: The intent set the parentHandleBrowser to " + handleIntent);
											parentHandleBrowser = handleIntent;
											break;
										}
									}
								}
							}
						}
					}
					else if(getIntent().getAction().equals(Constants.ACTION_PASS_CHANGED)){
						int result = getIntent().getIntExtra("RESULT",-20);
						if(result==0){
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							log("Show success mesage");
							Util.showAlert(this, getString(R.string.pass_changed_alert), null);
						}
						else if(result==MegaError.API_EARGS){
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							log("Error when changing pass - the current password is not correct");
							Util.showAlert(this,getString(R.string.old_password_provided_incorrect), getString(R.string.general_error_word));
						}
						else{
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							log("Error when changing pass - show error message");
							Util.showAlert(this,getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
						}
					}
					else if(getIntent().getAction().equals(Constants.ACTION_RESET_PASS)){
						String link = getIntent().getDataString();
						if(link!=null){
							log("link to resetPass: "+link);
							drawerItem=DrawerItem.ACCOUNT;
							selectDrawerItemLollipop(drawerItem);
							selectDrawerItemPending=false;
							showConfirmationResetPassword(link);
						}
					}
					else if(getIntent().getAction().equals(Constants.ACTION_IPC)){
						log("IPC link - go to received request in Contacts");
						drawerItem=DrawerItem.CONTACTS;
						indexContacts=2;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
					}
					else if(getIntent().getAction().equals(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE)){
						log("Chat notitificacion received");
						drawerItem=DrawerItem.CHAT;
						selectDrawerItemLollipop(drawerItem);
						long chatId = getIntent().getLongExtra("CHAT_ID", -1);
						String text = getIntent().getStringExtra("showSnackbar");
						if(chatId!=-1){
							openChat(chatId, text);
						}
						selectDrawerItemPending=false;
						getIntent().setAction(null);
						setIntent(null);
					}
					else if(getIntent().getAction().equals(Constants.ACTION_CHAT_SUMMARY)) {
						log("Chat notification: ACTION_CHAT_SUMMARY");
						drawerItem=DrawerItem.CHAT;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
						getIntent().setAction(null);
						setIntent(null);
					}
					else if(getIntent().getAction().equals(Constants.ACTION_SHOW_SETTINGS)) {
						log("Chat notification: SHOW_SETTINGS");
						selectDrawerItemPending=false;
						moveToSettingsSection();
						getIntent().setAction(null);
						setIntent(null);
					}
					else if (getIntent().getAction().equals(Constants.ACTION_SHOW_SETTINGS_STORAGE)) {
						log("ACTION_SHOW_SETTINGS_STORAGE");
						selectDrawerItemPending=false;
						moveToSettingsSectionStorage();
						getIntent().setAction(null);
						setIntent(null);
					}
					else if(getIntent().getAction().equals(Constants.ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION)){
						log("IPC link - go to received request in Contacts");
						drawerItem=DrawerItem.SHARED_ITEMS;
						indexShares=0;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
					}
					else if(getIntent().getAction().equals(Constants.ACTION_SHOW_MY_ACCOUNT)){
						log("intent from chat - show my account");
						drawerItem=DrawerItem.ACCOUNT;
						accountFragment=Constants.MY_ACCOUNT_FRAGMENT;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
					}
					else if(getIntent().getAction().equals(Constants.ACTION_SHOW_UPGRADE_ACCOUNT)){
						log("intent from chat - show my account");
						drawerItem=DrawerItem.ACCOUNT;
						accountFragment=Constants.UPGRADE_ACCOUNT_FRAGMENT;
						selectDrawerItemLollipop(drawerItem);
						selectDrawerItemPending=false;
					}
					else if(getIntent().getAction().equals(Constants.ACTION_OVERQUOTA_TRANSFER)){
						log("intent overquota transfer alert!!");
						if(alertDialogTransferOverquota==null){
							showTransferOverquotaDialog();
						}
						else{
							if(!(alertDialogTransferOverquota.isShowing())){
								showTransferOverquotaDialog();
							}
						}
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OPEN_HANDLE_NODE)){
						String link = getIntent().getDataString();
						String [] s = link.split("#");
						if (s.length > 1){
							String nodeHandleLink = s[1];
							String [] sSlash = s[1].split("/");
							if (sSlash.length > 0){
								nodeHandleLink = sSlash[0];
							}
							long nodeHandleLinkLong = MegaApiAndroid.base64ToHandle(nodeHandleLink);
							MegaNode nodeLink = megaApi.getNodeByHandle(nodeHandleLinkLong);
							if (nodeLink == null){
								showSnackbar(getString(R.string.general_error_file_not_found));
							}
							else{
								MegaNode pN = megaApi.getParentNode(nodeLink);
								if (pN == null){
									pN = megaApi.getRootNode();
								}
								parentHandleBrowser = pN.getHandle();
								drawerItem = DrawerItem.CLOUD_DRIVE;
								selectDrawerItemLollipop(drawerItem);
								selectDrawerItemPending = false;

								Intent i = new Intent(this, FileInfoActivityLollipop.class);
								i.putExtra("handle", nodeLink.getHandle());
								if (nodeLink.isFolder()) {
									if (nodeLink.isInShare()){
										i.putExtra("imageId", R.drawable.ic_folder_incoming);
									}
									else if (nodeLink.isOutShare()||megaApi.isPendingShare(nodeLink)){
										i.putExtra("imageId", R.drawable.ic_folder_outgoing);
									}
									else{
										i.putExtra("imageId", R.drawable.ic_folder);
									}
								}
								else {
									i.putExtra("imageId", MimeTypeThumbnail.typeForName(nodeLink.getName()).getIconResourceId());
								}
								i.putExtra("name", nodeLink.getName());
								startActivity(i);
							}
						}
						else{
							drawerItem = DrawerItem.CLOUD_DRIVE;
							selectDrawerItemLollipop(drawerItem);
						}
					}
					else if (getIntent().getAction().equals(Constants.ACTION_IMPORT_LINK_FETCH_NODES)){
						getIntent().setAction(null);
						setIntent(null);
					}
					else if (getIntent().getAction().equals(Constants.ACTION_OPEN_CONTACTS_SECTION)){
						handleInviteContact = getIntent().getLongExtra("handle", 0);

						drawerItem = DrawerItem.CONTACTS;
						indexContacts = 0;
						selectDrawerItemLollipop(drawerItem);
					}
				}
	        }

			log("onCreate - Check if there any unread chat");
			if(Util.isChatEnabled()){
				log("Connect to chat!: "+megaChatApi.getInitState());

				if(megaChatApi!=null){
					if((megaChatApi.getInitState()!=MegaChatApi.INIT_ERROR)){
						log("Connection goes!!!");
						megaChatApi.connect(this);
					}
					else{
						log("Not launch connect: "+megaChatApi.getInitState());
					}
				}
				else{
					log("megaChatApi is NULL");
				}

				setChatTitleSection();
			}

			log("onCreate - Check if there any INCOMING pendingRequest contacts");
			setContactTitleSection();

			if (drawerItem == null) {
	        	drawerItem = DrawerItem.CLOUD_DRIVE;
	        	Intent intent = getIntent();
	        	if (intent != null){
	        		boolean upgradeAccount = getIntent().getBooleanExtra("upgradeAccount", false);
					newAccount = getIntent().getBooleanExtra("newAccount", false);
					
                    //reset flag to fix incorrect view loaded when orientation changes
                    getIntent().removeExtra("newAccount");
                    getIntent().removeExtra("upgradeAccount");
	        		if(upgradeAccount){
	        			drawerLayout.closeDrawer(Gravity.LEFT);
						int accountType = getIntent().getIntExtra("accountType", 0);

						switch (accountType){
							case 0:{
								log("intent firstTime==true");
								firstTimeCam = true;
								drawerItem = DrawerItem.CAMERA_UPLOADS;
								setIntent(null);
								displayedAccountType = -1;
								return;
							}
							case Constants.PRO_I:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								displayedAccountType = Constants.PRO_I;
								selectDrawerItemLollipop(drawerItem);
								selectDrawerItemPending=false;
								return;
							}
							case Constants.PRO_II:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = Constants.PRO_II;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case Constants.PRO_III:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = Constants.PRO_III;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case Constants.PRO_LITE:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = Constants.PRO_LITE;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
						}
	        		}
	        		else{
						firstTimeCam = getIntent().getBooleanExtra("firstTimeCam", firstTimeCam);
						if (firstTimeCam){
							log("intent firstTimeCam==true");
							drawerItem = DrawerItem.CAMERA_UPLOADS;
							setIntent(null);
						}
					}
	        	}
	        }
	        else{
				log("DRAWERITEM NOT NULL1: " + drawerItem);
				Intent intentRec = getIntent();
	        	if (intentRec != null){
					boolean upgradeAccount = getIntent().getBooleanExtra("upgradeAccount", false);
					newAccount = getIntent().getBooleanExtra("newAccount", false);
					
					//reset flag to fix incorrect view loaded when orientation changes
                    getIntent().removeExtra("newAccount");
                    getIntent().removeExtra("upgradeAccount");
					firstTimeCam = intentRec.getBooleanExtra("firstTimeCam", firstTimeCam);
					if(upgradeAccount){
						drawerLayout.closeDrawer(Gravity.LEFT);
						int accountType = getIntent().getIntExtra("accountType", 0);

						switch (accountType){
							case Constants.FREE:{
								log("intent firstTime==true");
								firstTimeCam = true;
								drawerItem = DrawerItem.CAMERA_UPLOADS;
								displayedAccountType = -1;
								setIntent(null);
								return;
							}
							case Constants.PRO_I:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = Constants.PRO_I;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case Constants.PRO_II:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = Constants.PRO_II;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case Constants.PRO_III:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = Constants.PRO_III;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
							case Constants.PRO_LITE:{
								drawerItem = DrawerItem.ACCOUNT;
								accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
								selectDrawerItemPending=false;
								displayedAccountType = Constants.PRO_LITE;
								selectDrawerItemLollipop(drawerItem);
								return;
							}
						}
					}
					else{
						if (firstTimeCam) {
							log("intent firstTimeCam2==true");
							if (prefs != null){
								if (prefs.getCamSyncEnabled() != null){
									firstTimeCam = false;
								}
								else{
									firstTimeCam = true;
									drawerItem = DrawerItem.CAMERA_UPLOADS;
								}
							}
							else{
								firstTimeCam = true;
								drawerItem = DrawerItem.CAMERA_UPLOADS;
							}
							setIntent(null);
						}
					}

	        		if (intentRec.getAction() != null){
	        			if (intentRec.getAction().equals(Constants.ACTION_SHOW_TRANSFERS)){
	        				drawerItem = DrawerItem.TRANSFERS;
							setIntent(null);
	        			}
	        		}
	        	}
				drawerLayout.closeDrawer(Gravity.LEFT);
			}

			showStorageAlmostFullDialog();

	        //INITIAL FRAGMENT
			if(selectDrawerItemPending){
				selectDrawerItemLollipop(drawerItem);
			}
		}
		megaApi.shouldShowPasswordReminderDialog(false, this);
//		showRememberPasswordDialog(false);
//		showTransferOverquotaDialog();
		if (verify2FADialogIsShown){
			showVerifyPin2FA(verifyPin2FADialogType);
		}
		log("END onCreate");
	}

	public void showRememberPasswordDialog(boolean logout){

		if (logout){
			rememberPasswordLogout = true;
		}
		else {
			rememberPasswordLogout = false;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_remember_password, null);
		builder.setView(v);

		rememberPasswordDialogText = (TextView) v.findViewById(R.id.dialog_remember_pwd_text);
		showRememberPaswordCheckBox = (CheckBox) v.findViewById(R.id.dialog_remember_pwd_checkbox);
		testPwdButton = (Button) v.findViewById(R.id.dialog_remember_pwd_test_button);
		recoveryKeyButton = (Button) v.findViewById(R.id.dialog_remember_pwd_backup_recoverykey_button);
		dismissButton = (Button) v.findViewById(R.id.dialog_remember_pwd_dismiss_button);
		if (passwordReminderDialogBlocked){
			showRememberPaswordCheckBox.setChecked(true);
		}

		showRememberPaswordCheckBox.setOnClickListener(this);
		testPwdButton.setOnClickListener(this);
		recoveryKeyButton.setOnClickListener(this);
		dismissButton.setOnClickListener(this);


		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.RIGHT;
		if (rememberPasswordLogout) {
			rememberPasswordDialogText.setText(R.string.recovery_key_exported_dialog_text_logout);
			recoveryKeyButton.setText(R.string.option_export_recovery_key);
			dismissButton.setText(R.string.option_logout_anyway);
		}
		else {
			rememberPasswordDialogText.setText(R.string.remember_pwd_dialog_text);
			recoveryKeyButton.setText(R.string.action_export_master_key);
			dismissButton.setText(R.string.general_dismiss);
		}

		rememberPasswordDialog = builder.create();
		rememberPasswordDialog.setCancelable(false);
		rememberPasswordDialog.setCanceledOnTouchOutside(false);
		rememberPasswordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				passwordReminderDialogBlocked = false;
				if (passwordReminderDialogBlocked){
					log("Do not show me again");
					passwordReminderDialogBlocked();
				}
				else if (passwordReminderDialogSkipped){
					passwordReminderDialogSkiped();
				}
			}
		});
		rememberPasswordDialog.show();
	}

	void passwordReminderDialogBlocked(){
		megaApi.passwordReminderDialogBlocked(this);
	}

	void passwordReminderDialogSkiped(){
		megaApi.passwordReminderDialogSkipped(this);
	}

	@Override
	protected void onResume(){
		log("onResume");
		super.onResume();

//		dbH.setShowNotifOff(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			queryIfNotificationsAreOn();
		}

		if (getResources().getConfiguration().orientation != orientationSaved) {
			orientationSaved = getResources().getConfiguration().orientation;
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}
	}

	void queryIfNotificationsAreOn(){
		log("queryIfNotificationsAreOn");

		if (dbH == null){
			dbH = DatabaseHandler.getDbHandler(getApplicationContext());
		}

		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		if (turnOnNotifications){
			setTurnOnNotificationsFragment();
		}
		else {
			NotificationManagerCompat nf = NotificationManagerCompat.from(this);
			log ("NotificationsEnabled: "+nf.areNotificationsEnabled());
			if (!nf.areNotificationsEnabled()){
				log("off");
				if (dbH.getShowNotifOff() == null || dbH.getShowNotifOff().equals("true")){
					if (Util.isChatEnabled()){
						if (megaChatApi == null){
							megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
						}
						if ((megaApi.getContacts().size() >= 1) || (megaChatApi.getChatListItems().size() >= 1)){
							setTurnOnNotificationsFragment();
						}
					}
					else {
						if ((megaApi.getContacts().size() >= 1)){
							setTurnOnNotificationsFragment();
						}
					}
				}
			}
		}
	}

	public void deleteTurnOnNotificationsFragment(){
		log("deleteTurnOnNotificationsFragment");
		turnOnNotifications = false;

		tB.setVisibility(View.VISIBLE);
		abL.setVisibility(View.VISIBLE);

 		tonF = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
//			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			window.setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_dark_primary_color));
			window.setStatusBarColor(0);
		}
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD){
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		supportInvalidateOptionsMenu();
		selectDrawerItemLollipop(drawerItem);
	}

	void setTurnOnNotificationsFragment(){
		log("setTurnOnNotificationsFragment");
		aB.setSubtitle(null);
		tB.setVisibility(View.GONE);

		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (currentFragment != null){
			getSupportFragmentManager().beginTransaction().remove(currentFragment).commitNow();
		}

		if (tonF == null){
			tonF = new TurnOnNotificationsFragment();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_container, tonF, "tonF");
		ft.commitNowAllowingStateLoss();

		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);
		abL.setVisibility(View.GONE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.turn_on_notifications_statusbar));
		}
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD){
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		fragmentContainer.setVisibility(View.VISIBLE);
		drawerLayout.closeDrawer(Gravity.LEFT);
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		supportInvalidateOptionsMenu();
		hideFabButton();
	}

	@Override
	protected void onPostResume() {
		log("onPostResume");
    	super.onPostResume();

		((MegaApplication) getApplication()).sendSignalPresenceActivity();

		if (isSearching){
			selectDrawerItemLollipop(DrawerItem.SEARCH);
			isSearching = false;
			return;
		}

    	managerActivity = this;

    	Intent intent = getIntent();

//    	dbH = new DatabaseHandler(getApplicationContext());
    	dbH = DatabaseHandler.getDbHandler(getApplicationContext());
    	if(dbH.getCredentials() == null){
    		if (!openLink){
//				megaApi.localLogout();
//				AccountController aC = new AccountController(this);
//				aC.logout(this, megaApi, megaChatApi, false);
    			return;
    		}
    		else{
    			log("onPostResume: not credentials");
    			if (intent != null) {
    				log("onPostResume: not credentials -> INTENT");
    				if (intent.getAction() != null){
    					log("onPostResume: intent with ACTION: "+intent.getAction());
    					if (getIntent().getAction().equals(Constants.ACTION_EXPORT_MASTER_KEY)){
    						Intent exportIntent = new Intent(managerActivity, LoginActivityLollipop.class);
							intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
							exportIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    						exportIntent.setAction(getIntent().getAction());
    						startActivity(exportIntent);
    						finish();
    						return;
    					}
    				}
    			}
    		}
		}

    	if (intent != null) {
    		log("onPostResume: intent not null! "+intent.getAction());
    		// Open folder from the intent
			if (intent.hasExtra(Constants.EXTRA_OPEN_FOLDER)) {
				log("onPostResume: INTENT: EXTRA_OPEN_FOLDER");
				parentHandleBrowser = intent.getLongExtra(Constants.EXTRA_OPEN_FOLDER, -1);
				intent.removeExtra(Constants.EXTRA_OPEN_FOLDER);
				setIntent(null);
			}

    		if (intent.getAction() != null){
    			log("onPostResume: intent action");

    			if(getIntent().getAction().equals(Constants.ACTION_EXPLORE_ZIP)){
					log("onPostResume: open zip browser");
    				String pathZip=intent.getExtras().getString(Constants.EXTRA_PATH_ZIP);

    				Intent intentZip = new Intent(managerActivity, ZipBrowserActivityLollipop.class);
    				intentZip.putExtra(ZipBrowserActivityLollipop.EXTRA_PATH_ZIP, pathZip);
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
    			if (getIntent().getAction().equals(Constants.ACTION_IMPORT_LINK_FETCH_NODES)){
					log("onPostResume: ACTION_IMPORT_LINK_FETCH_NODES");
					Intent loginIntent = new Intent(managerActivity, LoginActivityLollipop.class);
					intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
					loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					loginIntent.setAction(Constants.ACTION_IMPORT_LINK_FETCH_NODES);
					loginIntent.setData(Uri.parse(getIntent().getDataString()));
					startActivity(loginIntent);
					finish();
					return;
				}
				else if (getIntent().getAction().equals(Constants.ACTION_OPEN_MEGA_LINK)){
					log("onPostResume: ACTION_OPEN_MEGA_LINK");
					Intent fileLinkIntent = new Intent(managerActivity, FileLinkActivityLollipop.class);
					fileLinkIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					fileLinkIntent.setAction(Constants.ACTION_IMPORT_LINK_FETCH_NODES);
					String data = getIntent().getDataString();
					if(data!=null){
						fileLinkIntent.setData(Uri.parse(data));
						startActivity(fileLinkIntent);
					}
					else{
						log("onPostResume: getDataString is NULL");
					}
					finish();
					return;
				}
    			else if (intent.getAction().equals(Constants.ACTION_OPEN_MEGA_FOLDER_LINK)){
					log("onPostResume: ACTION_OPEN_MEGA_FOLDER_LINK");
    				Intent intentFolderLink = new Intent(managerActivity, FolderLinkActivityLollipop.class);
    				intentFolderLink.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    				intentFolderLink.setAction(Constants.ACTION_OPEN_MEGA_FOLDER_LINK);

					String data = getIntent().getDataString();
					if(data!=null){
						intentFolderLink.setData(Uri.parse(data));
						startActivity(intentFolderLink);
					}
					else{
						log("onPostResume: getDataString is NULL");
					}
					finish();
    			}
    			else if (intent.getAction().equals(Constants.ACTION_REFRESH_PARENTHANDLE_BROWSER)){

    				parentHandleBrowser = intent.getLongExtra("parentHandle", -1);
    				intent.removeExtra("parentHandle");

					if(cloudPageAdapter!=null){
						cloudPageAdapter.notifyDataSetChanged();
					}
    			}
    			else if(intent.getAction().equals(Constants.ACTION_OVERQUOTA_STORAGE)){
	    			showOverquotaAlert(false);
	    		}
				else if(intent.getAction().equals(Constants.ACTION_PRE_OVERQUOTA_STORAGE)){
					showOverquotaAlert(true);
				}
	    		else if(intent.getAction().equals(Constants.ACTION_OVERQUOTA_TRANSFER)){
					log("onPostResume show overquota transfer alert!!");
					if(alertDialogTransferOverquota==null){
						showTransferOverquotaDialog();
					}
					else{
						if(!(alertDialogTransferOverquota.isShowing())){
							showTransferOverquotaDialog();
						}
					}
				}
				else if (intent.getAction().equals(Constants.ACTION_CHANGE_AVATAR)){
					log("onPostResume: Intent CHANGE AVATAR");
					String path = intent.getStringExtra("IMAGE_PATH");
					log("onPostResume: Path of the avatar: "+path);
					megaApi.setAvatar(path, this);
				}
    			else if (intent.getAction().equals(Constants.ACTION_CANCEL_CAM_SYNC)){
    				log("onPostResume: ACTION_CANCEL_UPLOAD or ACTION_CANCEL_DOWNLOAD or ACTION_CANCEL_CAM_SYNC");
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
						Intent tempIntent = null;
						String title = null;
						String text = null;
						if (intent.getAction().equals(Constants.ACTION_CANCEL_CAM_SYNC)) {
							tempIntent = new Intent(this, CameraSyncService.class);
							tempIntent.setAction(CameraSyncService.ACTION_CANCEL);
							title = getString(R.string.cam_sync_syncing);
							text = getString(R.string.cam_sync_cancel_sync);
						}

						final Intent cancelIntent = tempIntent;
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
//					builder.setTitle(title);
						builder.setMessage(text);

						builder.setPositiveButton(getString(R.string.cam_sync_stop),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										startService(cancelIntent);
									}
								});
						builder.setNegativeButton(getString(R.string.general_cancel), null);
						final AlertDialog dialog = builder.create();
						try {
							dialog.show();
						} catch (Exception ex) {
							startService(cancelIntent);
						}
					}
				}
    			else if (intent.getAction().equals(Constants.ACTION_SHOW_TRANSFERS)){
    				log("onPostResume: intent show transfers");
    				drawerItem = DrawerItem.TRANSFERS;
    				selectDrawerItemLollipop(drawerItem);
    			}
    			else if (intent.getAction().equals(Constants.ACTION_TAKE_SELFIE)){
    				log("onPostResume: Intent take selfie");
    				takePicture();
    			}
				else if (intent.getAction().equals(Constants.SHOW_REPEATED_UPLOAD)){
					log("onPostResume: Intent SHOW_REPEATED_UPLOAD");
					String message = intent.getStringExtra("MESSAGE");
					showSnackbar(message);
				}
				else if(getIntent().getAction().equals(Constants.ACTION_IPC)){
					log("IPC - go to received request in Contacts");
					drawerItem=DrawerItem.CONTACTS;
					indexContacts=2;
					selectDrawerItemLollipop(drawerItem);
				}
				else if(getIntent().getAction().equals(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE)){
					log("onPostResume: ACTION_CHAT_NOTIFICATION_MESSAGE");

					long chatId = getIntent().getLongExtra("CHAT_ID", -1);
					String text = getIntent().getStringExtra("showSnackbar");
					if(chatId!=-1){
						openChat(chatId, text);
					}
				}
				else if(getIntent().getAction().equals(Constants.ACTION_CHAT_SUMMARY)) {
					log("onPostResume: ACTION_CHAT_SUMMARY");
					drawerItem=DrawerItem.CHAT;
					selectDrawerItemLollipop(drawerItem);
				}
				else if(getIntent().getAction().equals(Constants.ACTION_INCOMING_SHARED_FOLDER_NOTIFICATION)){
					log("IPC - go to received request in Contacts");
					drawerItem=DrawerItem.SHARED_ITEMS;
					indexShares = 0;
					selectDrawerItemLollipop(drawerItem);
				}
				else if(getIntent().getAction().equals(Constants.ACTION_OPEN_CONTACTS_SECTION)){
					log("onPostResume: ACTION_OPEN_CONTACTS_SECTION");
					handleInviteContact = getIntent().getLongExtra("handle", 0);

					drawerItem = DrawerItem.CONTACTS;
					indexContacts = 0;
					selectDrawerItemLollipop(drawerItem);
				}
				else if (getIntent().getAction().equals(Constants.ACTION_RECOVERY_KEY_EXPORTED)){
					log("onPostResume: ACTION_RECOVERY_KEY_EXPORTED");
					exportRecoveryKey();
				}
				else if (getIntent().getAction().equals(Constants.ACTION_REQUEST_DOWNLOAD_FOLDER_LOGOUT)){
					String parentPath = intent.getStringExtra("parentPath");
					if (parentPath != null){
						log("path to download: "+parentPath);
						boolean fromOffline = getIntent().getBooleanExtra("fromOffline", false);
						AccountController ac = new AccountController(this);
						ac.exportMK(parentPath, fromOffline);
					}
				}
				else  if (getIntent().getAction().equals(Constants.ACTION_RECOVERY_KEY_COPY_TO_CLIPBOARD)){
					AccountController ac = new AccountController(this);
					if (getIntent().getBooleanExtra("logout", false)) {
						ac.copyMK(true);
					}
					else {
						ac.copyMK(false);
					}
				}

    			intent.setAction(null);
				setIntent(null);
    		}
    	}

    	if (nV != null){
    		switch(drawerItem){
	    		case CLOUD_DRIVE:{
	    			log("onPostResume: case CLOUD DRIVE");
					//Check the tab to shown and the title of the actionBar
					setToolbarTitle();

	    			break;
	    		}
	    		case SHARED_ITEMS:{
	    			log("onPostResume: case SHARED ITEMS");

					try {
						NotificationManager notificationManager =
								(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

						notificationManager.cancel(Constants.NOTIFICATION_PUSH_CLOUD_DRIVE);
					}
					catch (Exception e){
						log("Exception NotificationManager - remove contact notification");
					}
					setToolbarTitle();
					log("onPostResume: shared tabs visible");
					tabLayoutShares.setVisibility(View.VISIBLE);
					tabLayoutShares.setVisibility(View.VISIBLE);
					viewPagerShares.setVisibility(View.VISIBLE);
		    		break;
	    		}
				case SETTINGS:{
					setToolbarTitle();
					break;
				}
				case CONTACTS:{

					try {
						ContactsAdvancedNotificationBuilder notificationBuilder;
						notificationBuilder =  ContactsAdvancedNotificationBuilder.newInstance(this, megaApi);

						notificationBuilder.removeAllIncomingContactNotifications();
						notificationBuilder.removeAllAcceptanceContactNotifications();
					}
					catch (Exception e){
						log("Exception NotificationManager - remove all CONTACT notifications");
					}

					setToolbarTitle();
					break;
				}
				case SEARCH:{
					setToolbarTitle();
					break;
				}
				case CHAT:{
					if (nV != null){
						Menu nVMenu = nV.getMenu();
						resetNavigationViewMenu(nVMenu);
						MenuItem menuItem = nVMenu.findItem(R.id.navigation_item_chat);
						menuItem.setChecked(true);
						menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_chat_red));
					}

					if (rChatFL != null){
						if(rChatFL.isAdded()){
							rChatFL.setChats();
							rChatFL.setStatus();

							try {
								ChatAdvancedNotificationBuilder notificationBuilder;
								notificationBuilder =  ChatAdvancedNotificationBuilder.newInstance(this, megaApi, megaChatApi);

								notificationBuilder.removeAllChatNotifications();
							}
							catch (Exception e){
								log("Exception NotificationManager - remove all notifications");
							}

							MegaApplication.setRecentChatVisible(true);
						}
					}
					break;
				}
				case ACCOUNT:{
					setToolbarTitle();
					try {
						NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						notificationManager.cancel(Constants.NOTIFICATION_STORAGE_OVERQUOTA);
					}
					catch (Exception e){
						log("Exception NotificationManager - remove all notifications");
					}

					break;
				}
    		}
    	}
	}

	public void openChat(long chatId, String text){
		log("openChat: "+chatId);
//		drawerItem=DrawerItem.CHAT;
//		selectDrawerItemLollipop(drawerItem);

		if(chatId!=-1){
			MegaChatRoom chat = megaChatApi.getChatRoom(chatId);
			if(chat!=null){
				log("open chat with id: " + chatId);
				Intent intentToChat = new Intent(this, ChatActivityLollipop.class);
				intentToChat.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
				intentToChat.putExtra("CHAT_ID", chatId);
				if(text!=null){
					intentToChat.putExtra("showSnackbar", text);
				}
				this.startActivity(intentToChat);
			}
			else{
				log("Error, chat is NULL");
			}
		}
		else{
			log("Error, chat id is -1");
		}
	}

	public void showMuteIcon(MegaChatListItem item){
		log("showMuteIcon");
		if (rChatFL != null) {
			if (rChatFL.isAdded()) {
				rChatFL.showMuteIcon(item);
			}
		}
	}

	public void setProfileAvatar(){
		log("setProfileAvatar");
		File avatar = null;
		if (getExternalCacheDir() != null){
			avatar = new File(getExternalCacheDir().getAbsolutePath(), megaApi.getMyEmail() + ".jpg");
		}
		else{
			avatar = new File(getCacheDir().getAbsolutePath(), megaApi.getMyEmail() + ".jpg");
		}
		Bitmap imBitmap = null;
		if (avatar.exists()){
			if (avatar.length() > 0){
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);

				// Calculate inSampleSize
				options.inSampleSize = Util.calculateInSampleSize(options, 250, 250);

				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;

				imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);
				if (imBitmap == null) {
					avatar.delete();
					if (getExternalCacheDir() != null){
						megaApi.getUserAvatar(megaApi.getMyUser(), getExternalCacheDir().getAbsolutePath() + "/" + megaApi.getMyEmail() + ".jpg", this);
					}
					else{
						megaApi.getUserAvatar(megaApi.getMyUser(), getCacheDir().getAbsolutePath() + "/" + megaApi.getMyEmail() + ".jpg", this);
					}
				}
				else{
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
					nVPictureProfile.setImageBitmap(circleBitmap);
					nVPictureProfileTextView.setVisibility(View.GONE);
				}
			}
			else{
				if (getExternalCacheDir() != null){
					megaApi.getUserAvatar(megaApi.getMyUser(), getExternalCacheDir().getAbsolutePath() + "/" + megaApi.getMyUser().getEmail() + ".jpg", this);
				}
				else{
					megaApi.getUserAvatar(megaApi.getMyUser(), getCacheDir().getAbsolutePath() + "/" + megaApi.getMyUser().getEmail() + ".jpg", this);
				}
			}
		}
		else{
			if (getExternalCacheDir() != null){
				megaApi.getUserAvatar(megaApi.getMyUser(), getExternalCacheDir().getAbsolutePath() + "/" + megaApi.getMyUser().getEmail() + ".jpg", this);
			}
			else{
				megaApi.getUserAvatar(megaApi.getMyUser(), getCacheDir().getAbsolutePath() + "/" + megaApi.getMyUser().getEmail() + ".jpg", this);
			}
		}
	}

	public void setDefaultAvatar(){
		log("setDefaultAvatar");

		float density  = getResources().getDisplayMetrics().density;
		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);
		String color = megaApi.getUserAvatarColor(megaApi.getMyUser());
		if(color!=null){
			log("The color to set the avatar is "+color);
			p.setColor(Color.parseColor(color));
		}
		else{
			log("Default color to the avatar");
			p.setColor(ContextCompat.getColor(this, R.color.lollipop_primary_color));
		}

		int radius;
		if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
			radius = defaultAvatar.getWidth()/2;
		else
			radius = defaultAvatar.getHeight()/2;

		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
		nVPictureProfile.setImageBitmap(defaultAvatar);

		int avatarTextSize = Util.getAvatarTextSize(density);
		log("DENSITY: " + density + ":::: " + avatarTextSize);

		String firstLetter = " ";
		if(((MegaApplication) getApplication()).getMyAccountInfo()!=null) {
			firstLetter = ((MegaApplication) getApplication()).getMyAccountInfo().getFirstLetter();
		}

		nVPictureProfileTextView.setText(firstLetter);
		nVPictureProfileTextView.setTextSize(30);
		nVPictureProfileTextView.setTextColor(Color.WHITE);
		nVPictureProfileTextView.setVisibility(View.VISIBLE);
	}

	public void setOfflineAvatar(String email, long myHandle, String firstLetter){
		log("setOfflineAvatar");

		File avatar = null;
		if (getExternalCacheDir() != null){
			avatar = new File(getExternalCacheDir().getAbsolutePath(), email + ".jpg");
		}
		else{
			avatar = new File(getCacheDir().getAbsolutePath(), email + ".jpg");
		}
		Bitmap imBitmap = null;
		if (avatar.exists()) {
			if (avatar.length() > 0) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);

				// Calculate inSampleSize
				options.inSampleSize = Util.calculateInSampleSize(options, 250, 250);

				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;

				imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), options);
				if (imBitmap != null) {
					Bitmap circleBitmap = Bitmap.createBitmap(imBitmap.getWidth(), imBitmap.getHeight(), Bitmap.Config.ARGB_8888);

					BitmapShader shader = new BitmapShader(imBitmap, TileMode.CLAMP, TileMode.CLAMP);
					Paint paint = new Paint();
					paint.setShader(shader);

					Canvas c = new Canvas(circleBitmap);
					int radius;
					if (imBitmap.getWidth() < imBitmap.getHeight())
						radius = imBitmap.getWidth() / 2;
					else
						radius = imBitmap.getHeight() / 2;

					c.drawCircle(imBitmap.getWidth() / 2, imBitmap.getHeight() / 2, radius, paint);
					nVPictureProfile.setImageBitmap(circleBitmap);
					nVPictureProfileTextView.setVisibility(View.GONE);
					return;
				}
			}
		}

		float density  = getResources().getDisplayMetrics().density;
		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);

		String myHandleEncoded = "";
		if(megaApi.getMyUser()!=null){
			myHandle = megaApi.getMyUser().getHandle();
			myHandleEncoded = MegaApiAndroid.userHandleToBase64(myHandle);
		}
		else{
			myHandleEncoded = MegaApiAndroid.userHandleToBase64(myHandle);
		}

		String color = megaApi.getUserAvatarColor(myHandleEncoded);
		if(color!=null){
			log("The color to set the avatar is "+color);
			p.setColor(Color.parseColor(color));
		}
		else{
			log("Default color to the avatar");
			p.setColor(ContextCompat.getColor(this, R.color.lollipop_primary_color));
		}

		int radius;
		if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
			radius = defaultAvatar.getWidth()/2;
		else
			radius = defaultAvatar.getHeight()/2;

		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
		nVPictureProfile.setImageBitmap(defaultAvatar);

		int avatarTextSize = Util.getAvatarTextSize(density);
		log("DENSITY: " + density + ":::: " + avatarTextSize);

		nVPictureProfileTextView.setText(firstLetter);
		nVPictureProfileTextView.setTextSize(32);
		nVPictureProfileTextView.setTextColor(Color.WHITE);
		nVPictureProfileTextView.setVisibility(View.VISIBLE);

	}

	public void showDialogChangeUserAttribute(){
		log("showDialogChangeUserAttribute");

		megaApi.multiFactorAuthCheck(megaApi.getMyEmail(), this);

		ScrollView scrollView = new ScrollView(this);

		LinearLayout layout = new LinearLayout(this);

		scrollView.addView(layout);

		layout.setOrientation(LinearLayout.VERTICAL);
//        layout.setNestedScrollingEnabled(true);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.setMargins(Util.scaleWidthPx(20, outMetrics), 0, Util.scaleWidthPx(17, outMetrics), 0);

		final EditText inputFirstName = new EditText(this);
		inputFirstName.getBackground().mutate().clearColorFilter();
		inputFirstName.getBackground().mutate().setColorFilter(ContextCompat.getColor(this, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
		layout.addView(inputFirstName, params);

		final RelativeLayout error_layout_firtName = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout_firtName, params1);

		final ImageView error_icon_firtName = new ImageView(ManagerActivityLollipop.this);
		error_icon_firtName.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout_firtName.addView(error_icon_firtName);
		RelativeLayout.LayoutParams params_icon_firtName = (RelativeLayout.LayoutParams) error_icon_firtName.getLayoutParams();

		params_icon_firtName.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon_firtName.setLayoutParams(params_icon_firtName);

		error_icon_firtName.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		final TextView textError_firtName = new TextView(ManagerActivityLollipop.this);
		error_layout_firtName.addView(textError_firtName);
		RelativeLayout.LayoutParams params_text_error_firtName = (RelativeLayout.LayoutParams) textError_firtName.getLayoutParams();
		params_text_error_firtName.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error_firtName.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error_firtName.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error_firtName.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error_firtName.setMargins(Util.scaleWidthPx(3, outMetrics), 0,0,0);
		textError_firtName.setLayoutParams(params_text_error_firtName);

		textError_firtName.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		error_layout_firtName.setVisibility(View.GONE);

		final EditText inputLastName = new EditText(this);
		inputLastName.getBackground().mutate().clearColorFilter();
		inputLastName.getBackground().mutate().setColorFilter(ContextCompat.getColor(this, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
		layout.addView(inputLastName, params);

		final RelativeLayout error_layout_lastName = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout_lastName, params1);

		final ImageView error_icon_lastName = new ImageView(ManagerActivityLollipop.this);
		error_icon_lastName.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout_lastName.addView(error_icon_lastName);
		RelativeLayout.LayoutParams params_icon_lastName = (RelativeLayout.LayoutParams) error_icon_lastName.getLayoutParams();


		params_icon_lastName.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon_lastName.setLayoutParams(params_icon_lastName);

		error_icon_lastName.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		final TextView textError_lastName = new TextView(ManagerActivityLollipop.this);
		error_layout_lastName.addView(textError_lastName);
		RelativeLayout.LayoutParams params_text_error_lastName = (RelativeLayout.LayoutParams) textError_lastName.getLayoutParams();
		params_text_error_lastName.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error_lastName.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error_lastName.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error_lastName.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error_lastName.setMargins(Util.scaleWidthPx(3, outMetrics), 0,0,0);
		textError_lastName.setLayoutParams(params_text_error_lastName);

		textError_lastName.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		error_layout_lastName.setVisibility(View.GONE);

		final EditText inputMail = new EditText(this);
		inputMail.getBackground().mutate().clearColorFilter();
		inputMail.getBackground().mutate().setColorFilter(ContextCompat.getColor(this, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
		layout.addView(inputMail, params);

		final RelativeLayout error_layout_email = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout_email, params1);

		final ImageView error_icon_email = new ImageView(ManagerActivityLollipop.this);
		error_icon_email.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout_email.addView(error_icon_email);
		RelativeLayout.LayoutParams params_icon_email = (RelativeLayout.LayoutParams) error_icon_email.getLayoutParams();


		params_icon_email.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon_email.setLayoutParams(params_icon_email);

		error_icon_email.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		final TextView textError_email = new TextView(ManagerActivityLollipop.this);
		error_layout_email.addView(textError_email);
		RelativeLayout.LayoutParams params_text_error_email = (RelativeLayout.LayoutParams) textError_email.getLayoutParams();
		params_text_error_email.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error_email.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error_email.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error_email.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error_email.setMargins(Util.scaleWidthPx(3, outMetrics), 0,0,0);
		textError_email.setLayoutParams(params_text_error_email);

		textError_email.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		error_layout_email.setVisibility(View.GONE);


		inputFirstName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if(error_layout_firtName.getVisibility() == View.VISIBLE){
					error_layout_firtName.setVisibility(View.GONE);
					inputFirstName.getBackground().mutate().clearColorFilter();
					inputFirstName.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});
		inputFirstName.setSingleLine();

		inputFirstName.setText(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText());
		inputFirstName.setTextColor(getResources().getColor(R.color.text_secondary));
		inputFirstName.setImeOptions(EditorInfo.IME_ACTION_DONE);
		inputFirstName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		inputFirstName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String valueFirstName = inputFirstName.getText().toString().trim();
					String valueLastName = inputLastName.getText().toString().trim();
					String value = inputMail.getText().toString().trim();
					String emailError = Util.getEmailError(value, managerActivity);
					if (emailError != null) {
//						inputMail.setError(emailError);
						inputMail.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_email.setText(emailError);
						error_layout_email.setVisibility(View.VISIBLE);
						inputMail.requestFocus();
					}
					else if(valueFirstName.equals("")||valueFirstName.isEmpty()){
						log("input is empty");
//						inputFirstName.setError(getString(R.string.invalid_string));
						inputFirstName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_firtName.setText(getString(R.string.invalid_string));
						error_layout_firtName.setVisibility(View.VISIBLE);
						inputFirstName.requestFocus();
					}
					else if(valueLastName.equals("")||valueLastName.isEmpty()){
						log("input is empty");
//						inputLastName.setError(getString(R.string.invalid_string));
						inputLastName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_lastName.setText(getString(R.string.invalid_string));
						error_layout_lastName.setVisibility(View.VISIBLE);
						inputLastName.requestFocus();
					}
					else {
						log("positive button pressed - change user attribute");
						countUserAttributes = aC.updateUserAttributes(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText(), valueFirstName, ((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText(), valueLastName, megaApi.getMyEmail(), value);
						changeUserAttributeDialog.dismiss();
					}
				}
				else{
					log("other IME" + actionId);
				}
				return false;
			}
		});


		inputFirstName.setImeActionLabel(getString(R.string.title_edit_profile_info),EditorInfo.IME_ACTION_DONE);

		inputLastName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if(error_layout_lastName.getVisibility() == View.VISIBLE){
					error_layout_lastName.setVisibility(View.GONE);
					inputLastName.getBackground().mutate().clearColorFilter();
					inputLastName.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});
		inputLastName.setSingleLine();
		inputLastName.setText(((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText());
		inputLastName.setTextColor(getResources().getColor(R.color.text_secondary));
		inputLastName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		inputLastName.setImeOptions(EditorInfo.IME_ACTION_DONE);
		inputLastName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String valueFirstName = inputFirstName.getText().toString().trim();
					String valueLastName = inputLastName.getText().toString().trim();
					String value = inputMail.getText().toString().trim();
					String emailError = Util.getEmailError(value, managerActivity);
					if (emailError != null) {
//						inputMail.setError(emailError);
						inputMail.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_email.setText(emailError);
						error_layout_email.setVisibility(View.VISIBLE);
						inputMail.requestFocus();
					}
					else if(valueFirstName.equals("")||valueFirstName.isEmpty()){
						log("input is empty");
//						inputFirstName.setError(getString(R.string.invalid_string));
						inputFirstName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_firtName.setText(getString(R.string.invalid_string));
						error_layout_firtName.setVisibility(View.VISIBLE);
						inputFirstName.requestFocus();
					}
					else if(valueLastName.equals("")||valueLastName.isEmpty()){
						log("input is empty");
//						inputLastName.setError(getString(R.string.invalid_string));
						inputLastName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_lastName.setText(getString(R.string.invalid_string));
						error_layout_lastName.setVisibility(View.VISIBLE);
						inputLastName.requestFocus();
					}
					else {
						log("positive button pressed - change user attribute");
						countUserAttributes = aC.updateUserAttributes(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText(), valueFirstName, ((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText(), valueLastName, megaApi.getMyEmail(), value);
						changeUserAttributeDialog.dismiss();
					}
				}
				else{
					log("other IME" + actionId);
				}
				return false;
			}
		});

		inputLastName.setImeActionLabel(getString(R.string.title_edit_profile_info),EditorInfo.IME_ACTION_DONE);

		inputMail.getBackground().mutate().clearColorFilter();
		inputMail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if(error_layout_email.getVisibility() == View.VISIBLE){
					error_layout_email.setVisibility(View.GONE);
					inputMail.getBackground().mutate().clearColorFilter();
					inputMail.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);

				}
			}
		});
		inputMail.setSingleLine();
		inputMail.setText(megaApi.getMyUser().getEmail());
		inputMail.setTextColor(getResources().getColor(R.color.text_secondary));
		inputMail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		inputMail.setImeOptions(EditorInfo.IME_ACTION_DONE);
		inputMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		inputMail.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String valueFirstName = inputFirstName.getText().toString().trim();
					String valueLastName = inputLastName.getText().toString().trim();
					String value = inputMail.getText().toString().trim();
					String emailError = Util.getEmailError(value, managerActivity);
					if (emailError != null) {
//						inputMail.setError(emailError);
						inputMail.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_email.setText(emailError);
						error_layout_email.setVisibility(View.VISIBLE);
						inputMail.requestFocus();
					}
					else if(valueFirstName.equals("")||valueFirstName.isEmpty()){
						log("input is empty");
//						inputFirstName.setError(getString(R.string.invalid_string));
						inputFirstName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_firtName.setText(getString(R.string.invalid_string));
						error_layout_firtName.setVisibility(View.VISIBLE);
						inputFirstName.requestFocus();
					}
					else if(valueLastName.equals("")||valueLastName.isEmpty()){
						log("input is empty");
//						inputLastName.setError(getString(R.string.invalid_string));
						inputLastName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_lastName.setText(getString(R.string.invalid_string));
						error_layout_lastName.setVisibility(View.VISIBLE);
						inputLastName.requestFocus();
					}
					else {
						log("positive button pressed - change user attribute");
						countUserAttributes = aC.updateUserAttributes(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText(), valueFirstName, ((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText(), valueLastName, megaApi.getMyEmail(), value);
						changeUserAttributeDialog.dismiss();
					}
				}
				else{
					log("other IME" + actionId);
				}
				return false;
			}
		});
		inputMail.setImeActionLabel(getString(R.string.title_edit_profile_info),EditorInfo.IME_ACTION_DONE);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_edit_profile_info));

		builder.setPositiveButton(getString(R.string.title_edit_profile_info), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				inputFirstName.getBackground().clearColorFilter();
				inputLastName.getBackground().clearColorFilter();
				inputMail.getBackground().clearColorFilter();
			}
		});
		builder.setView(scrollView);

		changeUserAttributeDialog = builder.create();
		changeUserAttributeDialog.show();
		changeUserAttributeDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				log("OK BTTN PASSWORD");
				String valueFirstName = inputFirstName.getText().toString().trim();
				String valueLastName = inputLastName.getText().toString().trim();
				String value = inputMail.getText().toString().trim();
				String emailError = Util.getEmailError(value, managerActivity);
				if (emailError != null) {
//					inputMail.setError(emailError);
					inputMail.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
					textError_email.setText(emailError);
					error_layout_email.setVisibility(View.VISIBLE);
					inputMail.requestFocus();
				}
				else if(valueFirstName.equals("")||valueFirstName.isEmpty()){
					log("input is empty");
//					inputFirstName.setError(getString(R.string.invalid_string));
					inputFirstName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
					textError_firtName.setText(getString(R.string.invalid_string));
					error_layout_firtName.setVisibility(View.VISIBLE);
					inputFirstName.requestFocus();
				}
				else if(valueLastName.equals("")||valueLastName.isEmpty()){
					log("input is empty");
//					inputLastName.setError(getString(R.string.invalid_string));
					inputLastName.getBackground().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
					textError_lastName.setText(getString(R.string.invalid_string));
					error_layout_lastName.setVisibility(View.VISIBLE);
					inputLastName.requestFocus();
				}
				else {
					log("positive button pressed - change user attribute");
					countUserAttributes = aC.updateUserAttributes(((MegaApplication) getApplication()).getMyAccountInfo().getFirstNameText(), valueFirstName, ((MegaApplication) getApplication()).getMyAccountInfo().getLastNameText(), valueLastName, megaApi.getMyEmail(), value);
					changeUserAttributeDialog.dismiss();
				}
			}
		});
		showKeyboardDelayed(inputFirstName);
	}

	@Override
	protected void onStop(){
		log("onStop");
		super.onStop();
	}

	@Override
	protected void onPause() {
    	log("onPause");
    	managerActivity = null;
    	super.onPause();
    }

	@Override
    protected void onDestroy(){
		log("onDestroy()");

		dbH.removeSentPendingMessages();

    	if (megaApi.getRootNode() != null){
    		megaApi.removeGlobalListener(this);
    		megaApi.removeTransferListener(this);
    		megaApi.removeRequestListener(this);
    	}

		if (megaChatApi != null){
			megaChatApi.removeChatListener(this);
			megaChatApi.removeChatCallListener(this);
		}

		showStorageAlmostFullDialog = true;

		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverUpdatePosition);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(updateMyAccountReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverUpdate2FA);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(networkReceiver);

    	super.onDestroy();
	}
	public void selectDrawerItemCloudDrive(){
		log("selectDrawerItemCloudDrive");

		tB.setVisibility(View.VISIBLE);
		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);

		fragmentContainer.setVisibility(View.GONE);

		if (cloudPageAdapter == null){
			log("mTabsAdapterCloudDrive == null");
			tabLayoutCloud.setVisibility(View.VISIBLE);
			viewPagerCDrive.setVisibility(View.VISIBLE);
			cloudPageAdapter = new CloudDrivePagerAdapter(getSupportFragmentManager(),this);
			viewPagerCDrive.setAdapter(cloudPageAdapter);
			tabLayoutCloud.setupWithViewPager(viewPagerCDrive);

			//Force on CreateView, addTab do not execute onCreateView
			if(indexCloud!=-1){
				log("The index of the TAB CLOUD is: "+indexCloud);
				if (viewPagerCDrive != null){
					if(indexCloud==0){
						log("after creating tab in CLOUD TAB: "+parentHandleBrowser);
						viewPagerCDrive.setCurrentItem(0);
					}
					else{
						log("after creating tab in RUBBISH TAB: "+parentHandleRubbish);
						viewPagerCDrive.setCurrentItem(1);
					}
				}
				indexCloud=-1;
			}
			else{
				//No bundle, no change of orientation
				log("indexCloud is NOT -1");
			}
		}
		else{
			log("mTabsAdapterCloudDrive NOT null");
			tabLayoutCloud.setVisibility(View.VISIBLE);
			viewPagerCDrive.setVisibility(View.VISIBLE);
		}

		if (!firstTime){
			log("Its NOT first time");
			drawerLayout.closeDrawer(Gravity.LEFT);

			int dbContactsSize = dbH.getContactsSize();
			int sdkContactsSize = megaApi.getContacts().size();
			if (dbContactsSize != sdkContactsSize){
				log("Contacts TABLE != CONTACTS SDK "+ dbContactsSize + " vs " +sdkContactsSize);
				dbH.clearContacts();
				FillDBContactsTask fillDBContactsTask = new FillDBContactsTask(this);
				fillDBContactsTask.execute();
			}
		}
		else{
			log("Its first time");

			drawerLayout.openDrawer(Gravity.LEFT);
			//Fill the contacts DB
			FillDBContactsTask fillDBContactsTask = new FillDBContactsTask(this);
			fillDBContactsTask.execute();
			firstTime = false;
            dbH.setFirstTime(false);
		}
	}

	public void setToolbarTitle(){
		log("setToolbarTitle");
		if(drawerItem==null){
			return;
		}

		switch (drawerItem){
			case CLOUD_DRIVE:{
				aB.setSubtitle(null);
				log("setToolbarTitle: Cloud Drive SECTION");
				int indexCloud = getTabItemCloud();
				switch(indexCloud){
					case 0:{
						log("setToolbarTitle: cloud TAB");
						//Cloud Drive TAB
						MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
						if (parentNode != null){
							if(megaApi.getRootNode()!=null){
								if (parentNode.getHandle() == megaApi.getRootNode().getHandle() || parentHandleBrowser == -1){
									aB.setTitle(getString(R.string.section_cloud_drive));
									firstNavigationLevel = true;
								}
								else{
									aB.setTitle(parentNode.getName());
									firstNavigationLevel = false;
								}
							}
							else{
								parentHandleBrowser = -1;
							}
						}
						else{
							if(megaApi.getRootNode()!=null){
								parentHandleBrowser = megaApi.getRootNode().getHandle();
								aB.setTitle(getString(R.string.section_cloud_drive));
								firstNavigationLevel = true;
							}
							else{
								parentHandleBrowser = -1;
								firstNavigationLevel = true;
							}
						}
						break;
					}
					case 1:{
						log("setToolbarTitle: rubbish TAB");
						if(parentHandleRubbish == megaApi.getRubbishNode().getHandle() || parentHandleRubbish == -1){
							aB.setTitle(getResources().getString(R.string.section_rubbish_bin));
							firstNavigationLevel = true;
						}
						else{
							MegaNode node = megaApi.getNodeByHandle(parentHandleRubbish);
							if(node==null){
								log("Node NULL - cannot be recovered");
								aB.setTitle(getResources().getString(R.string.section_rubbish_bin));
							}
							else{
								aB.setTitle(node.getName());
							}

							firstNavigationLevel = false;
						}
						break;
					}
					default: {
						aB.setTitle(getResources().getString(R.string.section_rubbish_bin));
						firstNavigationLevel = true;
						break;
					}
				}

				break;
			}
			case SHARED_ITEMS:{
				log("setToolbarTitle: Shared Items SECTION");
				aB.setSubtitle(null);
				int indexShares = getTabItemShares();
				switch(indexShares){
					case 0:{
						log("setToolbarTitle: INCOMING TAB");
						inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
						if (inSFLol != null){

							if(parentHandleIncoming!=-1){
								MegaNode node = megaApi.getNodeByHandle(parentHandleIncoming);
								if(node==null){
									log("Node NULL - cannot be recovered");
									aB.setTitle(getResources().getString(R.string.section_shared_items));
								}
								else{
									aB.setTitle(node.getName());
								}

								firstNavigationLevel = false;
							}
							else{
								aB.setTitle(getResources().getString(R.string.section_shared_items));
								firstNavigationLevel = true;
							}
						}
						else{
							log("selectDrawerItemSharedItems: inSFLol == null");
						}
						break;
					}
					case 1:{
						log("setToolbarTitle: OUTGOING TAB");
						outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
						if (outSFLol != null) {

							if (parentHandleOutgoing != -1) {
								MegaNode node = megaApi.getNodeByHandle(parentHandleOutgoing);
								aB.setTitle(node.getName());
								firstNavigationLevel = false;
							} else {
								aB.setTitle(getResources().getString(R.string.section_shared_items));
								firstNavigationLevel = true;
							}
						}
						break;
					}
					default: {
						aB.setTitle(getResources().getString(R.string.section_shared_items));
						firstNavigationLevel = true;
						break;
					}
				}
				break;
			}
			case SAVED_FOR_OFFLINE: {
				aB.setSubtitle(null);

				if(pathNavigationOffline!=null){

					log("AFTER PathNavigation is: "+pathNavigationOffline);
					if (pathNavigationOffline.equals("/")){
						aB.setTitle(getString(R.string.section_saved_for_offline));
						firstNavigationLevel=true;
					}
					else{
						log("The pathNavigation is: "+pathNavigationOffline);
						String title = pathNavigationOffline;
						int index=title.lastIndexOf("/");
						title=title.substring(0,index);
						index=title.lastIndexOf("/");
						title=title.substring(index+1,title.length());
						aB.setTitle(title);
						firstNavigationLevel=false;
					}
				}
				else{
					log("PathNavigation is NULL");
					aB.setTitle(getString(R.string.section_saved_for_offline));
					firstNavigationLevel=true;
				}

				break;
			}
			case INBOX:{
				aB.setSubtitle(null);
				if(parentHandleInbox==megaApi.getInboxNode().getHandle()||parentHandleInbox==-1){
					aB.setTitle(getResources().getString(R.string.section_inbox));
					firstNavigationLevel = true;
				}
				else{
					MegaNode node = megaApi.getNodeByHandle(parentHandleInbox);
					aB.setTitle(node.getName());
					firstNavigationLevel = false;
				}
				break;
			}
			case CONTACTS:{
				aB.setSubtitle(null);
				aB.setTitle(getString(R.string.section_contacts));
				firstNavigationLevel = true;
				break;
			}
			case CHAT:{
				tB.setVisibility(View.VISIBLE);
				aB.setTitle(getString(R.string.section_chat));
				firstNavigationLevel = true;
				break;
			}
			case SEARCH:{
				aB.setSubtitle(null);
				if(textsearchQuery){
					sFLol.setAllowedMultiselect(true);
				}
				if(parentHandleSearch==-1){
					firstNavigationLevel = true;
					if(searchQuery!=null){
						if(!searchQuery.isEmpty()){
							aB.setTitle(getString(R.string.action_search)+": "+searchQuery);
						}else{
							aB.setTitle(getString(R.string.action_search)+": "+"");
						}
					}else{
						aB.setTitle(getString(R.string.action_search)+": "+"");
					}

				}else{
					MegaNode parentNode = megaApi.getNodeByHandle(parentHandleSearch);
					if (parentNode != null){
						aB.setTitle(parentNode.getName());
						firstNavigationLevel = false;
					}
				}
				break;
			}
			case SETTINGS:{
				aB.setSubtitle(null);
				aB.setTitle(getString(R.string.action_settings));
				firstNavigationLevel = true;
				break;
			}
			case ACCOUNT:{
				aB.setSubtitle(null);
				if(accountFragment==Constants.MY_ACCOUNT_FRAGMENT){
					aB.setTitle(getString(R.string.section_account));
					setFirstNavigationLevel(true);
				}
				else if(accountFragment==Constants.MONTHLY_YEARLY_FRAGMENT){
					aB.setTitle(R.string.action_upgrade_account);
					setFirstNavigationLevel(false);
				}
				else if(accountFragment==Constants.UPGRADE_ACCOUNT_FRAGMENT){
					aB.setTitle(R.string.action_upgrade_account);
					setFirstNavigationLevel(false);
				}
				else{
					aB.setTitle(getString(R.string.section_account));
					setFirstNavigationLevel(true);
				}
				break;
			}
			case TRANSFERS:{
				aB.setSubtitle(null);
				aB.setTitle(getString(R.string.section_transfers));
				setFirstNavigationLevel(true);
				break;
			}
			case CAMERA_UPLOADS:{
				aB.setSubtitle(null);
				if(isSearchEnabled){
					setFirstNavigationLevel(false);
				}
				else{
					setFirstNavigationLevel(true);
				}
				aB.setTitle(getString(R.string.section_photo_sync));
				break;
			}
			case MEDIA_UPLOADS:{
				aB.setSubtitle(null);
				if(isSearchEnabled){
					setFirstNavigationLevel(false);
				}
				else{
					setFirstNavigationLevel(true);
				}
				aB.setTitle(getString(R.string.section_secondary_media_uploads));
				break;
			}
			default:{
				log("setToolbarTitle: default GONE");

				break;
			}
		}

		updateNavigationToolbarIcon();
	}

	public void updateNavigationToolbarIcon(){
		//Just working on 4.4.+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if(Util.isChatEnabled() && megaChatApi != null){
				int numberUnread = megaChatApi.getUnreadChats();

				if(numberUnread==0){

					if(isFirstNavigationLevel()){
						if (drawerItem == DrawerItem.SEARCH){
							aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
						}
						else {
							aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
						}
					}
					else{
						aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
					}
				}
				else{
					if(isFirstNavigationLevel()){
						if (drawerItem == DrawerItem.SEARCH){
							badgeDrawable.setProgress(1.0f);
						}
						else {
							badgeDrawable.setProgress(0.0f);
						}
					}
					else{
						badgeDrawable.setProgress(1.0f);
					}

					if(numberUnread>9){
						badgeDrawable.setText("9+");
					}
					else{
						badgeDrawable.setText(numberUnread+"");
					}

					aB.setHomeAsUpIndicator(badgeDrawable);
				}
			}
			else{
				if(isFirstNavigationLevel()){
					if (drawerItem == DrawerItem.SEARCH){
						aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
					}
					else {
						aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
					}
				}
				else{
					aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
				}
			}
		} else {
			if(isFirstNavigationLevel()){
				if (drawerItem == DrawerItem.SEARCH){
					aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
				}
				else {
					aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
				}
			}
			else{
				aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
			}
		}
	}

	public void showOnlineMode(){
		log("showOnlineMode");

		if(usedSpaceLayout!=null){

			if(rootNode!=null){
				Menu nVMenu = nV.getMenu();
				if(nVMenu!=null){
					resetNavigationViewMenu(nVMenu);
				}
				clickDrawerItemLollipop(drawerItem);

				if(sttFLol!=null){
					if(sttFLol.isAdded()){
						sttFLol.setOnlineOptions(true);
					}
				}

				supportInvalidateOptionsMenu();
			}
			else{
				log("showOnlineMode - Root is NULL");
				if(getApplicationContext()!=null){
					showConfirmationConnect();
				}
			}

			if (rChatFL != null){
				if(rChatFL.isAdded()){
					log("ONLINE: Update screen RecentChats");
					if(!Util.isChatEnabled()){
						rChatFL.showDisableChatScreen();
					}
				}
			}

			usedSpaceLayout.setVisibility(View.VISIBLE);
		}
	}

	public void showConfirmationConnect(){
		log("showConfirmationConnect");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
						break;

					case DialogInterface.BUTTON_NEGATIVE:

						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		try {
			builder.setMessage(R.string.confirmation_to_reconnect).setPositiveButton(R.string.cam_sync_ok, dialogClickListener)
					.setNegativeButton(R.string.general_cancel, dialogClickListener).show().setCanceledOnTouchOutside(false);
		}
		catch (Exception e){}
	}

	public void showOfflineMode(){
		log("showOfflineMode");

		if(megaApi==null){
			log("megaApi is Null in Offline mode");
		}

		usedSpaceLayout.setVisibility(View.GONE);

		UserCredentials credentials = dbH.getCredentials();
		if(credentials!=null){
			String emailCredentials = credentials.getEmail();
			if(emailCredentials!=null){
				nVEmail.setText(emailCredentials);
			}

			String myHandleCredentials = credentials.getMyHandle();
			long myHandle = -1;
			if(myHandleCredentials!=null){
				if(!myHandleCredentials.isEmpty()){
					myHandle = Long.parseLong(myHandleCredentials);
				}
			}

			String firstNameText = credentials.getFirstName();
			String lastNameText = credentials.getLastName();
			String fullName = "";
			if(firstNameText==null){
				firstNameText="";
			}
			if(lastNameText==null){
				lastNameText="";
			}
			if (firstNameText.trim().length() <= 0){
				fullName = lastNameText;
			}
			else{
				fullName = firstNameText + " " + lastNameText;
			}

			if (fullName.trim().length() <= 0){
				log("Put email as fullname");
				String[] splitEmail = emailCredentials.split("[@._]");
				fullName = splitEmail[0];
			}

			if (fullName.trim().length() <= 0){
				fullName = getString(R.string.name_text)+" "+getString(R.string.lastname_text);
				log("Full name set by default: "+fullName);
			}

			nVDisplayName.setText(fullName);

			String firstLetter = fullName.charAt(0) + "";
			firstLetter = firstLetter.toUpperCase(Locale.getDefault());

			setOfflineAvatar(emailCredentials, myHandle, firstLetter);
		}

		if(sttFLol!=null){
			if(sttFLol.isAdded()){
				sttFLol.setOnlineOptions(false);
			}
		}

		if (rChatFL != null){
			if(rChatFL.isAdded()){
				log("OFFLINE: Update screen RecentChats");
				if(!Util.isChatEnabled()){
					rChatFL.showNoConnectionScreen();
				}
			}
		}

		log("DrawerItem on start offline: "+drawerItem);
		if(drawerItem==null){
			log("On start OFFLINE MODE");
			drawerItem=DrawerItem.SAVED_FOR_OFFLINE;
			Menu nVMenu = nV.getMenu();
			drawerMenuItem = nVMenu.findItem(R.id.navigation_item_saved_for_offline);
			if (drawerMenuItem != null){
				disableNavigationViewMenu(nVMenu);
				drawerMenuItem.setChecked(true);
				drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.saved_for_offline_red));
			}

			selectDrawerItemLollipop(drawerItem);
		}
		else{
			log("Change to OFFLINE MODE");
			Menu nVMenu = nV.getMenu();
			if (nVMenu != null){
				disableNavigationViewMenu(nVMenu);
			}
			if(drawerItem==DrawerItem.SETTINGS||drawerItem==DrawerItem.SAVED_FOR_OFFLINE||drawerItem==DrawerItem.CHAT){
				clickDrawerItemLollipop(drawerItem);
			}
		}

		supportInvalidateOptionsMenu();
	}

	public void clickDrawerItemLollipop(DrawerItem item){
		log("clickDrawerItemLollipop: "+item);
		Menu nVMenu = nV.getMenu();
		if (nVMenu != null){
			if(item==null){
				drawerMenuItem = nVMenu.findItem(R.id.navigation_item_cloud_drive);
				onNavigationItemSelected(drawerMenuItem);
				return;
			}

//			drawerLayout.closeDrawer(Gravity.LEFT);
			switch (item){
				case CLOUD_DRIVE:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_cloud_drive);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
					break;
				}
				case SAVED_FOR_OFFLINE:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_saved_for_offline);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.saved_for_offline_red));
					break;
				}
				case CAMERA_UPLOADS:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_camera_uploads);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_red));
					break;
				}
				case MEDIA_UPLOADS:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_camera_uploads);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_red));
					break;
				}
				case INBOX:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_inbox);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.inbox_red));
					break;
				}
				case SHARED_ITEMS:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_shared_items);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.shared_items_red));
					break;
				}
				case CONTACTS:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_contacts);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.contacts_red));
					break;
				}
				case SETTINGS:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_settings);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.settings_red));
					break;
				}
				case SEARCH:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_hidden);
					drawerMenuItem.setChecked(true);
					break;
				}
				case ACCOUNT:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_hidden);
					drawerMenuItem.setChecked(true);
					break;
				}
				case TRANSFERS:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_hidden);
					drawerMenuItem.setChecked(true);
					break;
				}
				case CHAT:{
					drawerMenuItem = nVMenu.findItem(R.id.navigation_item_chat);
					drawerMenuItem.setChecked(true);
					drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_chat_red));
					break;
				}
			}
		}
	}

	public void selectDrawerItemSharedItems(){
		log("selectDrawerItemSharedItems");
		tB.setVisibility(View.VISIBLE);

		try {
			NotificationManager notificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.cancel(Constants.NOTIFICATION_PUSH_CLOUD_DRIVE);
		}
		catch (Exception e){
			log("Exception NotificationManager - remove contact notification");
		}

		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);

		fragmentContainer.setVisibility(View.GONE);		

		if (sharesPageAdapter == null){
			log("selectDrawerItemSharedItems: sharesPageAdapter is NULL");
			tabLayoutShares.setVisibility(View.VISIBLE);
			viewPagerShares.setVisibility(View.VISIBLE);

			sharesPageAdapter = new SharesPageAdapter(getSupportFragmentManager(),this);
			viewPagerShares.setAdapter(sharesPageAdapter);
			tabLayoutShares.setupWithViewPager(viewPagerShares);

			//Force on CreateView, addTab do not execute onCreateView
			if(indexShares!=-1){
				log("selectDrawerItemSharedItems: The index of the TAB Shares is: "+indexShares);
				if (viewPagerShares != null){
					if(indexShares==0){
						log("selectDrawerItemSharedItems: after creating tab in INCOMING TAB: "+parentHandleIncoming);
						log("selectDrawerItemSharedItems: deepBrowserTreeIncoming: "+deepBrowserTreeIncoming);
						viewPagerShares.setCurrentItem(0);
					}
					else{
						log("selectDrawerItemSharedItems: after creating tab in OUTGOING TAB: "+parentHandleOutgoing);
						viewPagerShares.setCurrentItem(1);
					}
				}
				indexShares=-1;
			}
			else {
				//No bundle, no change of orientation
				log("selectDrawerItemSharedItems: indexShares is NOT -1");
			}

		}
		else{
			log("selectDrawerItemSharedItems: sharesPageAdapter NOT null");
			tabLayoutShares.setVisibility(View.VISIBLE);
			viewPagerShares.setVisibility(View.VISIBLE);
		}

		setToolbarTitle();

		if(Util.isChatEnabled()) {
			MegaNode parentNode = megaApi.getNodeByPath("/" + Constants.CHAT_FOLDER);
			if (parentNode == null) {
				log("Create folder: " + Constants.CHAT_FOLDER);
				megaApi.createFolder(Constants.CHAT_FOLDER, megaApi.getRootNode(), null);
			}
		}
		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void selectDrawerItemContacts (){
		log("selectDrawerItemContacts");
		tB.setVisibility(View.VISIBLE);

		try {
			ContactsAdvancedNotificationBuilder notificationBuilder;
			notificationBuilder =  ContactsAdvancedNotificationBuilder.newInstance(this, megaApi);

			notificationBuilder.removeAllIncomingContactNotifications();
			notificationBuilder.removeAllAcceptanceContactNotifications();
		}
		catch (Exception e){
			log("Exception NotificationManager - remove all CONTACT notifications");
		}

		if (aB == null){
			aB = getSupportActionBar();
		}
		setToolbarTitle();

		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);

		fragmentContainer.setVisibility(View.GONE);

		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (currentFragment != null){
			getSupportFragmentManager().beginTransaction().remove(currentFragment).commitNow();
		}
		tabLayoutContacts.setVisibility(View.VISIBLE);
		viewPagerContacts.setVisibility(View.VISIBLE);

		if (contactsPageAdapter == null){
			log("contactsPageAdapter == null");

			tabLayoutContacts.setVisibility(View.VISIBLE);
			viewPagerContacts.setVisibility(View.VISIBLE);
			contactsPageAdapter = new ContactsPageAdapter(getSupportFragmentManager(),this);
			viewPagerContacts.setAdapter(contactsPageAdapter);
			tabLayoutContacts.setupWithViewPager(viewPagerContacts);

			log("The index of the TAB CONTACTS is: " + indexContacts);
			if(indexContacts==-1) {
				log("The index os contacts is -1");
				ArrayList<MegaContactRequest> requests = megaApi.getIncomingContactRequests();
				if(requests!=null) {
					int pendingRequest = requests.size();
					if (pendingRequest != 0) {
						indexContacts = 2;
					}
				}
			}

			if (viewPagerContacts != null) {
				switch (indexContacts){
					case 1:{
						viewPagerContacts.setCurrentItem(1);
						log("Select Sent Requests TAB");
						break;
					}
					case 2:{
						viewPagerContacts.setCurrentItem(2);
						log("Select Received Request TAB");
						break;
					}
					default:{
						viewPagerContacts.setCurrentItem(0);
						log("Select Contacts TAB");
						break;
					}
				}
			}
		}
		else {
			log("contactsPageAdapter NOT null");
			String sharesTag = getFragmentTag(R.id.contact_tabs_pager, 0);
			cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
			sharesTag = getFragmentTag(R.id.contact_tabs_pager, 1);
			sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);
			sharesTag = getFragmentTag(R.id.contact_tabs_pager, 2);
			rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(sharesTag);

			log("The index of the TAB CONTACTS is: " + indexContacts);
			if (viewPagerContacts != null) {
				switch (indexContacts) {
					case 1: {
						viewPagerContacts.setCurrentItem(1);
						log("Select Sent Requests TAB");
						break;
					}
					case 2: {
						viewPagerContacts.setCurrentItem(2);
						log("Select Received Request TAB");
						break;
					}
					default: {
						viewPagerContacts.setCurrentItem(0);
						log("Select Contacts TAB");
						break;
					}
				}
			}
		}

		viewPagerContacts.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				log("onPageScrolled");
				indexContacts = position;
			}

			@Override
			public void onPageSelected(int position) {
				log("onPageSelected");
				indexContacts = position;
				String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
				cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if(cFLol!=null && cFLol.isAdded()){
					cFLol.hideMultipleSelect();
					cFLol.clearSelectionsNoAnimations();
				}
				cFTag = getFragmentTag(R.id.contact_tabs_pager, 1);
				sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if(sRFLol!=null && sRFLol.isAdded()){
					sRFLol.clearSelections();
					sRFLol.hideMultipleSelect();
				}
				cFTag = getFragmentTag(R.id.contact_tabs_pager, 2);
				rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
				if(rRFLol!=null && rRFLol.isAdded()){
					rRFLol.clearSelections();
					rRFLol.hideMultipleSelect();
				}
				supportInvalidateOptionsMenu();
				showFabButton();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void selectDrawerItemAccount(){
		log("selectDrawerItemAccount");

		if(((MegaApplication) getApplication()).getMyAccountInfo()!=null && ((MegaApplication) getApplication()).getMyAccountInfo().getNumVersions() == -1){
			megaApi.getFolderInfo(megaApi.getRootNode(), this);
		}

		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);

		switch(accountFragment){
			case Constants.UPGRADE_ACCOUNT_FRAGMENT:{
				log("Show upgrade FRAGMENT");
				fragmentContainer.setVisibility(View.VISIBLE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				showUpAF();
				break;
			}
			case Constants.MONTHLY_YEARLY_FRAGMENT:{
				log("Show monthly yearly FRAGMENT");
				fragmentContainer.setVisibility(View.VISIBLE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				showmyF(selectedPaymentMethod, selectedAccountType);
				showFabButton();
				break;
			}
			default:{
				log("Show myAccount Fragment");
				fragmentContainer.setVisibility(View.GONE);
				accountFragment=Constants.MY_ACCOUNT_FRAGMENT;

				tabLayoutMyAccount.setVisibility(View.VISIBLE);
				viewPagerMyAccount.setVisibility(View.VISIBLE);

				if (mTabsAdapterMyAccount == null){
					log("mTabsAdapterMyAccount == null");

					mTabsAdapterMyAccount = new MyAccountPageAdapter(getSupportFragmentManager(),this);
					viewPagerMyAccount.setAdapter(mTabsAdapterMyAccount);
					tabLayoutMyAccount.setupWithViewPager(viewPagerMyAccount);

					log("The index of the TAB ACCOUNT is: " + indexAccount);
					if(indexAccount!=-1) {
						if (viewPagerMyAccount != null) {
							switch (indexAccount){
								case 0:{
									viewPagerMyAccount.setCurrentItem(0);
									log("General TAB");
									break;
								}
								case 1:{
									viewPagerMyAccount.setCurrentItem(1);
									log("Storage TAB");
									break;
								}
								default:{
									viewPagerContacts.setCurrentItem(0);
									log("Default general TAB");
									break;
								}
							}
						}
					}
					else{
						//No bundle, no change of orientation
						log("indexAccount is NOT -1");
					}
				}
				else{
					log("mTabsAdapterMyAccount NOT null");
					String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);

					myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 1);
					mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);

					if(indexAccount!=-1) {
						log("The index of the TAB MyAccount is: " + indexAccount);
						if (viewPagerMyAccount != null) {
							switch (indexAccount) {
								case 1: {
									viewPagerMyAccount.setCurrentItem(1);
									log("Select Storage TAB");
									break;
								}
								default: {
									viewPagerMyAccount.setCurrentItem(0);
									log("Select General TAB");
									break;
								}
							}
						}
					}
				}

				viewPagerMyAccount.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
					@Override
					public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
					}

					@Override
					public void onPageSelected(int position) {
						supportInvalidateOptionsMenu();
					}

					@Override
					public void onPageScrollStateChanged(int state) {
					}
				});

				drawerLayout.closeDrawer(Gravity.LEFT);
				setToolbarTitle();
				supportInvalidateOptionsMenu();
				showFabButton();
				break;
			}
		}
	}

	public void selectDrawerItemTransfers(){
		log("selectDrawerItemTransfers");

		tB.setVisibility(View.VISIBLE);

		drawerItem = DrawerItem.TRANSFERS;

		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
			resetNavigationViewMenu(nVMenu);
			hidden.setChecked(true);
		}

		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);

		fragmentContainer.setVisibility(View.GONE);

		drawerLayout.closeDrawer(Gravity.LEFT);

		tabLayoutTransfers.setVisibility(View.VISIBLE);
		viewPagerTransfers.setVisibility(View.VISIBLE);

		if (mTabsAdapterTransfers == null){
			log("mTabsAdapterTransfers == null");

			mTabsAdapterTransfers = new TransfersPageAdapter(getSupportFragmentManager(),this);
			viewPagerTransfers.setAdapter(mTabsAdapterTransfers);
			tabLayoutTransfers.setupWithViewPager(viewPagerTransfers);

			log("The index of the TAB TRANSFERS is: " + indexTransfers);
			if(indexTransfers!=-1) {
				if (viewPagerMyAccount != null) {
					switch (indexTransfers){
						case 0:{
							viewPagerMyAccount.setCurrentItem(0);
							log("General TAB");
							break;
						}
						case 1:{
							viewPagerMyAccount.setCurrentItem(1);
							log("Storage TAB");
							break;
						}
						default:{
							viewPagerContacts.setCurrentItem(0);
							log("Default general TAB");
							break;
						}
					}
				}
			}
			else{
				//No bundle, no change of orientation
				log("indexTransfers is NOT -1");
			}
		}
		else{
			log("mTabsAdapterTransfers NOT null");
			String transfersTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
			tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(transfersTag);

			transfersTag = getFragmentTag(R.id.transfers_tabs_pager, 1);
			completedTFLol = (CompletedTransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(transfersTag);

			if(indexTransfers!=-1) {
				log("The index of the TAB Transfers is: " + indexTransfers);
				if (viewPagerTransfers != null) {
					switch (indexTransfers) {
						case 1: {
							viewPagerTransfers.setCurrentItem(1);
							log("Select Storage TAB");
							break;
						}
						default: {
							viewPagerTransfers.setCurrentItem(0);
							log("Select General TAB");
							break;
						}
					}
				}
			}
		}

		setToolbarTitle();

		showFabButton();

		drawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void selectDrawerItemChat(){
		log("selectDrawerItemChat");

		((MegaApplication)getApplication()).setRecentChatVisible(true);

		try {
			ChatAdvancedNotificationBuilder notificationBuilder;
			notificationBuilder =  ChatAdvancedNotificationBuilder.newInstance(this, megaApi, megaChatApi);

			notificationBuilder.removeAllChatNotifications();
		}
		catch (Exception e){
			log("Exception NotificationManager - remove all notifications");
		}

		MegaNode parentNode = megaApi.getNodeByPath("/"+Constants.CHAT_FOLDER);
		if(parentNode == null){
			log("Create folder: "+Constants.CHAT_FOLDER);
			megaApi.createFolder(Constants.CHAT_FOLDER, megaApi.getRootNode(), null);
		}

		setToolbarTitle();

		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);

		fragmentContainer.setVisibility(View.VISIBLE);

		if (rChatFL == null){
			log("New REcentChatFragment");
			rChatFL = new RecentChatsFragmentLollipop();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, rChatFL, "rChat");
			ft.commitNow();
		}
		else{
			log("REcentChatFragment is not null");
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, rChatFL, "rChat");
			ft.commitNow();
			rChatFL.setChats();
			rChatFL.setStatus();
		}
		log("show chats");
		drawerLayout.closeDrawer(Gravity.LEFT);
	}
	@SuppressLint("NewApi")
	public void selectDrawerItemLollipop(DrawerItem item){
    	log("selectDrawerItemLollipop: "+item);

		((MegaApplication)getApplication()).setRecentChatVisible(false);

    	switch (item){
			case CLOUD_DRIVE:{
				selectDrawerItemCloudDrive();
				if (openFolderFromSearch){
					onNodesCloudDriveUpdate();
					openFolderFromSearch = false;
				}
    			supportInvalidateOptionsMenu();
				setToolbarTitle();
				showFabButton();
				log("END selectDrawerItem for Cloud Drive");
    			break;
    		}
    		case SAVED_FOR_OFFLINE:{

				tB.setVisibility(View.VISIBLE);

    			if (oFLol == null){
					log("New OfflineFragment");
    				oFLol = new OfflineFragmentLollipop();
    				oFLol.setIsList(isList);
//    				oFLol.setPathNavigation("/");
    			}
    			else{
					log("OfflineFragment exist");
//    				oFLol.setPathNavigation("/");
    				oFLol.setIsList(isList);
					oFLol.findNodes();
    			}

    			tabLayoutCloud.setVisibility(View.GONE);
    			viewPagerCDrive.setVisibility(View.GONE);
				tabLayoutContacts.setVisibility(View.GONE);
    			viewPagerContacts.setVisibility(View.GONE);
    			viewPagerShares.setVisibility(View.GONE);
    			tabLayoutShares.setVisibility(View.GONE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				tabLayoutTransfers.setVisibility(View.GONE);
				viewPagerTransfers.setVisibility(View.GONE);

				fragmentContainer.setVisibility(View.VISIBLE);

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, oFLol, "oFLol");
    			ft.commitNow();

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
					if (!hasStoragePermission) {
						ActivityCompat.requestPermissions(this,
								new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								Constants.REQUEST_WRITE_STORAGE);
					}

//					boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
//					if (!hasCameraPermission) {
//						ActivityCompat.requestPermissions(this,
//				                new String[]{Manifest.permission.CAMERA},
//				                ManagerActivityLollipop.REQUEST_CAMERA);
//					}

//					if (hasStoragePermission && hasCameraPermission){
				}


    			drawerLayout.closeDrawer(Gravity.LEFT);

    			supportInvalidateOptionsMenu();
    			setToolbarTitle();
				showFabButton();
    			break;
    		}
    		case CAMERA_UPLOADS:{
				tB.setVisibility(View.VISIBLE);
    			if (cuFL == null){
                    Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("cuFLol");
                    if(currentFragment != null && currentFragment instanceof CameraUploadFragmentLollipop){
                        cuFL = ((CameraUploadFragmentLollipop) currentFragment);
                    }
                    else{
                        cuFL = new CameraUploadFragmentLollipop();
                    }
				}

    			tabLayoutCloud.setVisibility(View.GONE);
    			viewPagerCDrive.setVisibility(View.GONE);
				tabLayoutContacts.setVisibility(View.GONE);
    			viewPagerContacts.setVisibility(View.GONE);
    			tabLayoutShares.setVisibility(View.GONE);
    			viewPagerShares.setVisibility(View.GONE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				tabLayoutTransfers.setVisibility(View.GONE);
				viewPagerTransfers.setVisibility(View.GONE);

				fragmentContainer.setVisibility(View.VISIBLE);

				FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
				Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("cuFLol");
				if (currentFragment != null) {
					fragTransaction.detach(currentFragment);
					fragTransaction.commitNowAllowingStateLoss();

					fragTransaction = getSupportFragmentManager().beginTransaction();
					fragTransaction.attach(currentFragment);
					fragTransaction.commitNowAllowingStateLoss();
				}
				else{
					fragTransaction.replace(R.id.fragment_container, cuFL, "cuFLol");
					fragTransaction.commitNowAllowingStateLoss();
				}

    			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
					if (!hasStoragePermission) {
						ActivityCompat.requestPermissions(this,
				                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								Constants.REQUEST_WRITE_STORAGE);
					}

//					boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
//					if (!hasCameraPermission) {
//						ActivityCompat.requestPermissions(this,
//				                new String[]{Manifest.permission.CAMERA},
//				                ManagerActivityLollipop.REQUEST_CAMERA);
//					}

				}

				drawerLayout.closeDrawer(Gravity.LEFT);

				setToolbarTitle();
    			supportInvalidateOptionsMenu();
				showFabButton();
      			break;
    		}
    		case MEDIA_UPLOADS:{
				tB.setVisibility(View.VISIBLE);

				if (nV != null){
					Menu nVMenu = nV.getMenu();
					MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
					resetNavigationViewMenu(nVMenu);
					hidden.setChecked(true);
				}

    			if (muFLol == null){
                    Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("muFLol");
                    if(currentFragment != null && currentFragment instanceof CameraUploadFragmentLollipop){
                        muFLol = (CameraUploadFragmentLollipop) currentFragment;
                    }
                    else {
//    					cuF = new CameraUploadFragmentLollipop(CameraUploadFragmentLollipop.TYPE_MEDIA);
                        muFLol = CameraUploadFragmentLollipop.newInstance(CameraUploadFragmentLollipop.TYPE_MEDIA);
                    }
				}

    			tabLayoutCloud.setVisibility(View.GONE);
    			viewPagerCDrive.setVisibility(View.GONE);
				tabLayoutContacts.setVisibility(View.GONE);
    			viewPagerContacts.setVisibility(View.GONE);
    			tabLayoutShares.setVisibility(View.GONE);
    			viewPagerShares.setVisibility(View.GONE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				tabLayoutTransfers.setVisibility(View.GONE);
				viewPagerTransfers.setVisibility(View.GONE);

				fragmentContainer.setVisibility(View.VISIBLE);

				FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
				Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("muFLol");
				if (currentFragment != null) {
					fragTransaction.detach(currentFragment);
					fragTransaction.commitNowAllowingStateLoss();

					fragTransaction = getSupportFragmentManager().beginTransaction();
					fragTransaction.attach(currentFragment);
					fragTransaction.commitNowAllowingStateLoss();
				}
				else{
					fragTransaction.replace(R.id.fragment_container, muFLol, "muFLol");
					fragTransaction.commitNowAllowingStateLoss();
				}

				drawerLayout.closeDrawer(Gravity.LEFT);

    			supportInvalidateOptionsMenu();
    			setToolbarTitle();
				showFabButton();
      			break;
    		}
    		case INBOX:{

				tB.setVisibility(View.VISIBLE);
				iFLol = new InboxFragmentLollipop().newInstance();

    			tabLayoutCloud.setVisibility(View.GONE);
    			viewPagerCDrive.setVisibility(View.GONE);
				tabLayoutContacts.setVisibility(View.GONE);
    			viewPagerContacts.setVisibility(View.GONE);
    			tabLayoutShares.setVisibility(View.GONE);
    			viewPagerShares.setVisibility(View.GONE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				tabLayoutTransfers.setVisibility(View.GONE);
				viewPagerTransfers.setVisibility(View.GONE);

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, iFLol, "iFLol");
    			ft.commitNow();

				fragmentContainer.setVisibility(View.VISIBLE);

    			drawerLayout.closeDrawer(Gravity.LEFT);

				if (openFolderFromSearch){
					onNodesInboxUpdate();
					openFolderFromSearch = false;
				}
    			supportInvalidateOptionsMenu();
				setToolbarTitle();
				showFabButton();
    			break;
    		}
    		case SHARED_ITEMS:{
				selectDrawerItemSharedItems();
				if (openFolderFromSearch){
					onNodesSharedUpdate();
					openFolderFromSearch = false;
				}
    			supportInvalidateOptionsMenu();

				showFabButton();
    			break;
    		}
    		case CONTACTS:{
				selectDrawerItemContacts();
				supportInvalidateOptionsMenu();
				showFabButton();
    			break;
    		}
    		case SETTINGS:{

				if(((MegaApplication) getApplication()).getMyAccountInfo()!=null && ((MegaApplication) getApplication()).getMyAccountInfo().getNumVersions() == -1){
					megaApi.getFolderInfo(megaApi.getRootNode(), this);
				}

				aB.setSubtitle(null);
				tB.setVisibility(View.VISIBLE);

    			drawerLayout.closeDrawer(Gravity.LEFT);

    			supportInvalidateOptionsMenu();

				tabLayoutContacts.setVisibility(View.GONE);
    			viewPagerContacts.setVisibility(View.GONE);
    			tabLayoutShares.setVisibility(View.GONE);
    			viewPagerShares.setVisibility(View.GONE);
    			tabLayoutCloud.setVisibility(View.GONE);
    			viewPagerCDrive.setVisibility(View.GONE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				tabLayoutTransfers.setVisibility(View.GONE);
				viewPagerTransfers.setVisibility(View.GONE);

    			Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    			if (currentFragment != null){
    				getSupportFragmentManager().beginTransaction().remove(currentFragment).commitNow();
    			}

    			if (sttFLol != null && sttFLol.isAdded()){
					if (openSettingsStorage){
						sttFLol.goToCategoryStorage();
					}
					else if (openSettingsQR){
						log ("goToCategoryQR");
						sttFLol.goToCategoryQR();
					}
				}
				else {
					sttFLol = new SettingsFragmentLollipop();
				}

    			android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
    			ft.replace(R.id.fragment_container, sttFLol, "sttF");
    			ft.commit();

				fragmentContainer.setVisibility(View.VISIBLE);

				setToolbarTitle();
				supportInvalidateOptionsMenu();
				showFabButton();
				break;
    		}
    		case SEARCH:{
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
					resetNavigationViewMenu(nVMenu);
					hidden.setChecked(true);
				}

				drawerLayout.closeDrawer(Gravity.LEFT);

    			drawerItem = DrawerItem.SEARCH;
				sFLol = new SearchFragmentLollipop().newInstance();

    			tabLayoutCloud.setVisibility(View.GONE);
    			viewPagerCDrive.setVisibility(View.GONE);
				tabLayoutContacts.setVisibility(View.GONE);
    			viewPagerContacts.setVisibility(View.GONE);
    			tabLayoutShares.setVisibility(View.GONE);
    			viewPagerShares.setVisibility(View.GONE);
				tabLayoutMyAccount.setVisibility(View.GONE);
				viewPagerMyAccount.setVisibility(View.GONE);
				tabLayoutTransfers.setVisibility(View.GONE);
				viewPagerTransfers.setVisibility(View.GONE);

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, sFLol, "sFLol");
    			ft.commitNowAllowingStateLoss();

				fragmentContainer.setVisibility(View.VISIBLE);

				showFabButton();
    			break;
    		}
			case ACCOUNT:{
				log("case ACCOUNT: "+accountFragment);
//    			tB.setVisibility(View.GONE);
				aB.setSubtitle(null);
				selectDrawerItemAccount();
				supportInvalidateOptionsMenu();
				break;
			}
    		case TRANSFERS:{
				aB.setSubtitle(null);
				selectDrawerItemTransfers();
    			supportInvalidateOptionsMenu();
				showFabButton();
    			break;
    		}
			case CHAT:{
				log("chat selected");
				if (megaApi != null) {
					contacts = megaApi.getContacts();
					for (int i=0;i<contacts.size();i++){
						if (contacts.get(i).getVisibility() == MegaUser.VISIBILITY_VISIBLE){

							MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(contacts.get(i).getHandle()+""));
							String fullName = "";
							if(contactDB!=null){
								ContactController cC = new ContactController(this);
								fullName = cC.getFullName(contactDB.getName(), contactDB.getLastName(), contacts.get(i).getEmail());
							}
							else{
								//No name, ask for it and later refresh!!
								log("CONTACT DB is null");
								fullName = contacts.get(i).getEmail();
							}
							visibleContacts.add(contacts.get(i));
						}
					}
				}
				selectDrawerItemChat();
				supportInvalidateOptionsMenu();
				if (visibleContacts.size() == 0 || visibleContacts.isEmpty() || visibleContacts == null){
					hideFabButton();
				}
				else {
					showFabButton();
				}
				break;
			}
    	}

		if (megaApi.multiFactorAuthAvailable()) {
			if (newAccount || isEnable2FADialogShown) {
				showEnable2FADialog();
			}
		}
	}

	void showEnable2FADialog () {
		log ("showEnable2FADialog newaccount: "+newAccount);
		newAccount = false;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_enable_2fa_create_account, null);
		builder.setView(v);

		enable2FAButton = (Button) v.findViewById(R.id.enable_2fa_button);
		enable2FAButton.setOnClickListener(this);
		skip2FAButton = (Button) v.findViewById(R.id.skip_enable_2fa_button);
		skip2FAButton.setOnClickListener(this);

		enable2FADialog = builder.create();
		enable2FADialog.setCanceledOnTouchOutside(false);
		try {
			enable2FADialog.show();
		}catch (Exception e){};
		isEnable2FADialogShown = true;
	}

	public void moveToSettingsSection(){
		drawerItem=DrawerItem.SETTINGS;
		selectDrawerItemLollipop(drawerItem);
	}

	public void moveToSettingsSectionStorage(){
		openSettingsStorage = true;
		drawerItem=DrawerItem.SETTINGS;
		selectDrawerItemLollipop(drawerItem);
	}

	public void moveToSettingsSectionQR(){
		openSettingsQR = true;
		drawerItem=DrawerItem.SETTINGS;
		selectDrawerItemLollipop(drawerItem);
	}

	private String getFragmentTag(int viewPagerId, int fragmentPosition){
	     return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
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

	public void showMyAccount(){
		drawerItem = DrawerItem.ACCOUNT;
		selectDrawerItemLollipop(drawerItem);
	}

	public void showCC(int type, int payMonth, boolean refresh){

		accountFragment = Constants.CC_FRAGMENT;
		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (!refresh){
			if (ccFL == null){
				ccFL = new CreditCardFragmentLollipop();
				ccFL.setInfo(type, payMonth);
				ft.replace(R.id.fragment_container, ccFL, "ccF");
				ft.commitNow();
			}
			else{
				ccFL.setInfo(type, payMonth);
				ft.replace(R.id.fragment_container, ccFL, "ccF");
				ft.commitNow();
			}
		}
		else{
			Fragment tempF = getSupportFragmentManager().findFragmentByTag("ccF");
			if (tempF != null){
				ft.detach(tempF);
				ft.attach(tempF);
				ft.commitNowAllowingStateLoss();
			}
			else{
				if (ccFL == null){
					ccFL = new CreditCardFragmentLollipop();
					ccFL.setInfo(type, payMonth);
					ft.replace(R.id.fragment_container, ccFL, "ccF");
					ft.commitNow();
				}
				else{
					ccFL.setInfo(type, payMonth);
					ft.replace(R.id.fragment_container, ccFL, "ccF");
					ft.commitNow();
				}
			}
		}
		fragmentContainer.setVisibility(View.VISIBLE);
	}

	public void updateInfoNumberOfSubscriptions(){
        if (cancelSubscription != null){
            cancelSubscription.setVisible(false);
        }
        if (((MegaApplication) getApplication()).getMyAccountInfo()!= null && ((MegaApplication) getApplication()).getMyAccountInfo().getNumberOfSubscriptions() > 0){
            if (cancelSubscription != null){
                if (drawerItem == DrawerItem.ACCOUNT){
                    if (maFLol != null){
                        cancelSubscription.setVisible(true);
                    }
                }
            }
        }
    }

	public void showFortumo(){
		accountFragment = Constants.FORTUMO_FRAGMENT;
		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (fFL == null){
			fFL = new FortumoFragmentLollipop();
			ft.replace(R.id.fragment_container,  fFL, "fF");
			ft.commitNow();
		}
		else{
			ft.replace(R.id.fragment_container, fFL, "fF");
			ft.commitNow();
		}
		fragmentContainer.setVisibility(View.VISIBLE);
	}

	public void showCentili(){
		accountFragment = Constants.CENTILI_FRAGMENT;
		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (ctFL == null){
			ctFL = new CentiliFragmentLollipop();
			ft.replace(R.id.fragment_container,  ctFL, "ctF");
			ft.commitNow();
		}
		else{
			ft.replace(R.id.fragment_container, ctFL, "ctF");
			ft.commitNow();
		}
		fragmentContainer.setVisibility(View.VISIBLE);
	}

	public void showmyF(int paymentMethod, int type){
		log("showmyF");

		accountFragment = Constants.MONTHLY_YEARLY_FRAGMENT;
		setToolbarTitle();

		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (myFL == null){
			myFL = new MonthlyAnnualyFragmentLollipop();
			myFL.setInfo(paymentMethod, type);
			ft.replace(R.id.fragment_container, myFL, "myF");
			ft.commitNow();
		}
		else{
			myFL.setInfo(paymentMethod, type);
			ft.replace(R.id.fragment_container, myFL, "myF");
			ft.commitNow();
		}
		fragmentContainer.setVisibility(View.VISIBLE);
	}

	public void showUpAF(){
		log("showUpAF");

		accountFragment=Constants.UPGRADE_ACCOUNT_FRAGMENT;
		setToolbarTitle();

		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if(upAFL==null){
			upAFL = new UpgradeAccountFragmentLollipop();
			ft.replace(R.id.fragment_container, upAFL, "upAFL");
			ft.commitNowAllowingStateLoss();
		}
		else{
			ft.replace(R.id.fragment_container, upAFL, "upAFL");
			ft.commitNowAllowingStateLoss();
		}
		fragmentContainer.setVisibility(View.VISIBLE);

		supportInvalidateOptionsMenu();
		showFabButton();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		log("onCreateOptionsMenuLollipop");

		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_manager, menu);
//	    getSupportActionBar().setDisplayShowCustomEnabled(true);

	    final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchMenuItem = menu.findItem(R.id.action_search);
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

		if (searchView != null){
			searchView.setIconifiedByDefault(true);
		}

		MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				textsearchQuery = false;
				searchQuery = "";
				firstNavigationLevel = true;
				parentHandleSearch = -1;
				levelsSearch = -1;
				drawerItem = DrawerItem.SEARCH;
				selectDrawerItemLollipop(DrawerItem.SEARCH);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				log("onMenuItemActionCollapse()");
				drawerItem = DrawerItem.CLOUD_DRIVE;
				selectDrawerItemLollipop(DrawerItem.CLOUD_DRIVE);
				textSubmitted = true;
				return true;
			}
		});
		searchView.setMaxWidth(Integer.MAX_VALUE);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				log("onQueryTextSubmit: "+query);
				searchQuery = "" + query;
				selectDrawerItemLollipop(DrawerItem.SEARCH);
				setToolbarTitle();
				supportInvalidateOptionsMenu();
				log("Search query: " + query);
				textSubmitted = true;
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				if(textSubmitted){
					sFLol.setAllowedMultiselect(true);

					textSubmitted = false;
				}else if (textsearchQuery) {
					selectDrawerItemLollipop(DrawerItem.SEARCH);
				}else{
					searchQuery = newText;
					selectDrawerItemLollipop(DrawerItem.SEARCH);
				}
				return true;
			}
    	});

		gridSmallLargeMenuItem = menu.findItem(R.id.action_grid_view_large_small);
		addContactMenuItem =menu.findItem(R.id.action_add_contact);
		addMenuItem = menu.findItem(R.id.action_add);
		createFolderMenuItem = menu.findItem(R.id.action_new_folder);
		importLinkMenuItem = menu.findItem(R.id.action_import_link);
		selectMenuItem = menu.findItem(R.id.action_select);
		unSelectMenuItem = menu.findItem(R.id.action_unselect);
		thumbViewMenuItem= menu.findItem(R.id.action_grid);

		refreshMenuItem = menu.findItem(R.id.action_menu_refresh);
		sortByMenuItem = menu.findItem(R.id.action_menu_sort_by);
		helpMenuItem = menu.findItem(R.id.action_menu_help);
		upgradeAccountMenuItem = menu.findItem(R.id.action_menu_upgrade_account);
		rubbishBinMenuItem = menu.findItem(R.id.action_rubbish_bin);
		clearRubbishBinMenuitem = menu.findItem(R.id.action_menu_clear_rubbish_bin);
		cancelAllTransfersMenuItem = menu.findItem(R.id.action_menu_cancel_all_transfers);
		clearCompletedTransfers = menu.findItem(R.id.action_menu_clear_completed_transfers);
		playTransfersMenuIcon = menu.findItem(R.id.action_play);
		pauseTransfersMenuIcon = menu.findItem(R.id.action_pause);
		cancelAllTransfersMenuItem.setVisible(false);
		clearCompletedTransfers.setVisible(false);
		scanQRcode = menu.findItem(R.id.action_scan_qr);
		scanQRcode.setVisible(false);

		changePass = menu.findItem(R.id.action_menu_change_pass);

		takePicture = menu.findItem(R.id.action_take_picture);
		searchByDate = menu.findItem(R.id.action_search_by_date);

		cancelSubscription = menu.findItem(R.id.action_menu_cancel_subscriptions);
		cancelSubscription.setVisible(false);

		exportMK = menu.findItem(R.id.action_menu_export_MK);
		exportMK.setVisible(false);
		removeMK = menu.findItem(R.id.action_menu_remove_MK);
		removeMK.setVisible(false);

		killAllSessions = menu.findItem(R.id.action_menu_kill_all_sessions);
		killAllSessions.setVisible(false);

		logoutMenuItem = menu.findItem(R.id.action_menu_logout);
		logoutMenuItem.setVisible(false);

		forgotPassMenuItem = menu.findItem(R.id.action_menu_forgot_pass);
		forgotPassMenuItem.setVisible(false);

		newChatMenuItem = menu.findItem(R.id.action_menu_new_chat);
		setStatusMenuItem = menu.findItem(R.id.action_menu_set_status);

	    if (drawerItem == null){
	    	if (nV != null){
	    		Menu nVMenu = nV.getMenu();
	    		if (nVMenu != null){
	    			drawerItem = DrawerItem.CLOUD_DRIVE;
	    			resetNavigationViewMenu(nVMenu);
	    			drawerMenuItem = nVMenu.findItem(R.id.navigation_item_cloud_drive);
	    			if (drawerMenuItem != null){
	    				resetNavigationViewMenu(nVMenu);
	    				drawerMenuItem.setChecked(true);
	    				drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
	    				if (drawerLayout != null){
	    					drawerLayout.openDrawer(Gravity.LEFT);
	    				}
	    			}
	    		}

	    	}
	    	else{
				log("onCreateOptionsMenuLollipop: nV is NULL");
	    	}
	    }
	    else{
	    	if (nV != null){
	    		Menu nVMenu = nV.getMenu();
	    		switch(drawerItem){
		    		case CLOUD_DRIVE:{
		    			drawerMenuItem = nVMenu.findItem(R.id.navigation_item_cloud_drive);
		    			if (drawerMenuItem != null){
		    				resetNavigationViewMenu(nVMenu);
		    				drawerMenuItem.setChecked(true);
		    				drawerMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
		    			}
		    			break;
		    		}
	    		}
	    	}
	    }

	    if(Util.isOnline(this)){

			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				log("----------------------------------------INDEX: "+indexCloud);
				if(cloudPageAdapter!=null){
					if(viewPagerCDrive.getCurrentItem()==1){
						rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
						if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
							//Show
							selectMenuItem.setVisible(true);
							if(!firstTimeCam){
								thumbViewMenuItem.setVisible(true);
							}else{
								thumbViewMenuItem.setVisible(false);
							}

							clearRubbishBinMenuitem.setVisible(true);
							searchMenuItem.setVisible(true);

							//Hide
							searchByDate.setVisible(false);
							refreshMenuItem.setVisible(false);
							pauseTransfersMenuIcon.setVisible(false);
							playTransfersMenuIcon.setVisible(false);
							log("createFolderMenuItem.setVisible_13");
							createFolderMenuItem.setVisible(false);
							addMenuItem.setVisible(false);
							addContactMenuItem.setVisible(false);
							upgradeAccountMenuItem.setVisible(true);
							unSelectMenuItem.setVisible(false);
							addMenuItem.setEnabled(false);
							changePass.setVisible(false);
							importLinkMenuItem.setVisible(false);
							takePicture.setVisible(false);
							refreshMenuItem.setVisible(false);
							helpMenuItem.setVisible(false);
							logoutMenuItem.setVisible(false);
							forgotPassMenuItem.setVisible(false);

							if (isList){
								thumbViewMenuItem.setTitle(getString(R.string.action_grid));
							}
							else{
								thumbViewMenuItem.setTitle(getString(R.string.action_list));
							}

							if(rubbishBinFLol.getItemCount()>0){
								sortByMenuItem.setVisible(true);
								selectMenuItem.setVisible(true);
								clearRubbishBinMenuitem.setVisible(true);
							}
							else{
								sortByMenuItem.setVisible(false);
								selectMenuItem.setVisible(false);
								clearRubbishBinMenuitem.setVisible(false);
							}

							rubbishBinMenuItem.setVisible(false);
							gridSmallLargeMenuItem.setVisible(false);
						}
					}
					else{
						fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
						if (fbFLol!=null && fbFLol.isAdded()){
							log("onCreateOptionsMenuLollipop: in Cloud");

							//Show
							addMenuItem.setEnabled(true);
							addMenuItem.setVisible(true);
							log("createFolderMenuItem.setVisible_14");
							createFolderMenuItem.setVisible(true);
							if(!firstTimeCam){
								thumbViewMenuItem.setVisible(true);
							}else{
								thumbViewMenuItem.setVisible(false);
							}
							rubbishBinMenuItem.setVisible(false);
							upgradeAccountMenuItem.setVisible(true);
							importLinkMenuItem.setVisible(true);
							if(!firstTimeCam){
								takePicture.setVisible(true);
							}else{
								takePicture.setVisible(false);
							}
							selectMenuItem.setVisible(true);
							searchMenuItem.setVisible(true);

							//Hide
							searchByDate.setVisible(false);
							pauseTransfersMenuIcon.setVisible(false);
							playTransfersMenuIcon.setVisible(false);
							addContactMenuItem.setVisible(false);
							unSelectMenuItem.setVisible(false);
							clearRubbishBinMenuitem.setVisible(false);
							changePass.setVisible(false);
							refreshMenuItem.setVisible(false);
							helpMenuItem.setVisible(false);
							killAllSessions.setVisible(false);
							logoutMenuItem.setVisible(false);
							forgotPassMenuItem.setVisible(false);

							if(fbFLol.getItemCount()>0){
								selectMenuItem.setVisible(true);
								sortByMenuItem.setVisible(true);
							}
							else{
								selectMenuItem.setVisible(false);
								sortByMenuItem.setVisible(false);
							}

							if (isList){
								thumbViewMenuItem.setTitle(getString(R.string.action_grid));
							}
							else{
								thumbViewMenuItem.setTitle(getString(R.string.action_list));
							}
							gridSmallLargeMenuItem.setVisible(false);
						}
						else{
							log("Fragment NULL");
						}
					}
					newChatMenuItem.setVisible(false);
					setStatusMenuItem.setVisible(false);
				}
			}
			else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
				if (oFLol != null && oFLol.isAdded()){
					//Show
					if(!firstTimeCam){
						thumbViewMenuItem.setVisible(true);
					}else{
						thumbViewMenuItem.setVisible(false);
					}

					if(oFLol.getItemCountWithoutRK()>0){
						sortByMenuItem.setVisible(true);
						selectMenuItem.setVisible(true);
					}
					else{
						sortByMenuItem.setVisible(false);
						selectMenuItem.setVisible(false);
					}
					searchMenuItem.setVisible(true);

					//Hide
					searchByDate.setVisible(false);
					upgradeAccountMenuItem.setVisible(false);
					refreshMenuItem.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_15");
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					changePass.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					forgotPassMenuItem.setVisible(false);

					if (isList){
						thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
					}
					gridSmallLargeMenuItem.setVisible(false);
				}
				newChatMenuItem.setVisible(false);
				setStatusMenuItem.setVisible(false);
			}
			else if (drawerItem == DrawerItem.CAMERA_UPLOADS){
				if (cuFL != null && cuFL.isAdded()){

					//Show
					upgradeAccountMenuItem.setVisible(true);
					takePicture.setVisible(false);

					if(firstNavigationLevel){
						if(!firstTimeCam){
							searchByDate.setVisible(true);
						}else{
							searchByDate.setVisible(false);
						}
					}else{
						searchByDate.setVisible(false);
					}

					//Hide
					sortByMenuItem.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_16");
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					refreshMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					if(!firstTimeCam){
						thumbViewMenuItem.setVisible(true);
					}else{
						thumbViewMenuItem.setVisible(false);
					}					changePass.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					forgotPassMenuItem.setVisible(false);

					if (isListCameraUploads){
						thumbViewMenuItem.setTitle(getString(R.string.action_grid));
						gridSmallLargeMenuItem.setVisible(false);
						searchMenuItem.setVisible(true);

						if(cuFL.getItemCountList()>0){
							selectMenuItem.setVisible(true);
						}
						else{
							selectMenuItem.setVisible(false);
						}
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
						if (isSmallGridCameraUploads){
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
						}else{
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
						}

//						if (isLargeGridCameraUploads){
//							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
//						}
//						else{
//							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
//						}
						if(!firstTimeCam) {
							gridSmallLargeMenuItem.setVisible(true);
						}else{
							gridSmallLargeMenuItem.setVisible(false);
						}
						searchMenuItem.setVisible(false);

						if(cuFL.getItemCountGrid()>0){
							selectMenuItem.setVisible(true);
						}
						else{
							selectMenuItem.setVisible(false);
						}
					}
				}
				newChatMenuItem.setVisible(false);
				setStatusMenuItem.setVisible(false);
			}
			else if (drawerItem == DrawerItem.MEDIA_UPLOADS){
				if (muFLol != null && muFLol.isAdded()){

					//Show
					upgradeAccountMenuItem.setVisible(true);

					takePicture.setVisible(false);

					if(firstNavigationLevel){
						if(!firstTimeCam){
							searchByDate.setVisible(true);
						}else{
							searchByDate.setVisible(false);
						}
					}else{
						searchByDate.setVisible(false);
					}

					//Hide
					sortByMenuItem.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_17");
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					refreshMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					if(!firstTimeCam){
						thumbViewMenuItem.setVisible(true);
					}else{
						thumbViewMenuItem.setVisible(false);
					}					changePass.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					forgotPassMenuItem.setVisible(false);

					if (isListCameraUploads){
						thumbViewMenuItem.setTitle(getString(R.string.action_grid));
						gridSmallLargeMenuItem.setVisible(false);
						searchMenuItem.setVisible(true);

						if(muFLol.getItemCountList()>0){
							selectMenuItem.setVisible(true);
						}
						else{
							selectMenuItem.setVisible(false);
						}
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
						if (isSmallGridCameraUploads){
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
						}else{
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
						}

//						if (isLargeGridCameraUploads){
//							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
//						}
//						else{
//							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
//						}

						if(!firstTimeCam) {
							gridSmallLargeMenuItem.setVisible(true);
						}else{
							gridSmallLargeMenuItem.setVisible(false);
						}
						searchMenuItem.setVisible(false);

						if(muFLol.getItemCountGrid()>0){
							selectMenuItem.setVisible(true);
						}
						else{
							selectMenuItem.setVisible(false);
						}
					}
				}
				newChatMenuItem.setVisible(false);
				setStatusMenuItem.setVisible(false);
			}

			else if (drawerItem == DrawerItem.INBOX){
				if (iFLol != null && iFLol.isAdded()){
					//Show
					if(iFLol.getItemCount()>0){
						selectMenuItem.setVisible(true);
						sortByMenuItem.setVisible(true);

					}
					else{
						selectMenuItem.setVisible(false);
						sortByMenuItem.setVisible(false);

					}

					if (isList){
						thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
					}

					searchMenuItem.setVisible(true);
					if(!firstTimeCam){
						thumbViewMenuItem.setVisible(true);
					}else{
						thumbViewMenuItem.setVisible(false);
					}
					//Hide
					searchByDate.setVisible(false);
					refreshMenuItem.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_18");
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					upgradeAccountMenuItem.setVisible(true);
					unSelectMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					changePass.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					gridSmallLargeMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					newChatMenuItem.setVisible(false);
					setStatusMenuItem.setVisible(false);
				}
			}

			else if (drawerItem == DrawerItem.SHARED_ITEMS){
				//Lollipop
				int index = viewPagerShares.getCurrentItem();
				if(index==0){
					inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
					if (inSFLol != null && inSFLol.isAdded()){
						if(!firstTimeCam){
							thumbViewMenuItem.setVisible(true);
						}else{
							thumbViewMenuItem.setVisible(false);
						}
						addMenuItem.setEnabled(true);

						log("onCreateOptionsMenu parentHandleIncoming: "+parentHandleIncoming);
						if(parentHandleIncoming==-1){
							addMenuItem.setVisible(false);
							createFolderMenuItem.setVisible(false);
						}
						else{
							MegaNode node = megaApi.getNodeByHandle(parentHandleIncoming);
							if(node!=null){
								//Check the folder's permissions
								int accessLevel= megaApi.getAccess(node);
								log("onCreateOptionsMenu Node: "+node.getName());

								switch(accessLevel){
									case MegaShare.ACCESS_OWNER:
									case MegaShare.ACCESS_READWRITE:
									case MegaShare.ACCESS_FULL:{
										addMenuItem.setVisible(true);
										createFolderMenuItem.setVisible(true);
										break;
									}
									case MegaShare.ACCESS_READ:{
										addMenuItem.setVisible(false);
										createFolderMenuItem.setVisible(false);
										break;
									}
								}
							}
							else{
								addMenuItem.setVisible(false);
								createFolderMenuItem.setVisible(false);
							}
						}

						if(inSFLol.getItemCount()>0){
							selectMenuItem.setVisible(true);
							sortByMenuItem.setVisible(true);

						}
						else{
							selectMenuItem.setVisible(false);
							sortByMenuItem.setVisible(false);

						}
						searchMenuItem.setVisible(true);

						//Hide
						searchByDate.setVisible(false);
						pauseTransfersMenuIcon.setVisible(false);
						playTransfersMenuIcon.setVisible(false);
						addContactMenuItem.setVisible(false);
						unSelectMenuItem.setVisible(false);
						rubbishBinMenuItem.setVisible(false);
						clearRubbishBinMenuitem.setVisible(false);
						changePass.setVisible(false);
						importLinkMenuItem.setVisible(false);
						takePicture.setVisible(false);
						refreshMenuItem.setVisible(false);
						helpMenuItem.setVisible(false);
						upgradeAccountMenuItem.setVisible(false);
						gridSmallLargeMenuItem.setVisible(false);
						logoutMenuItem.setVisible(false);
						forgotPassMenuItem.setVisible(false);

						if (isList){
							thumbViewMenuItem.setTitle(getString(R.string.action_grid));
						}
						else{
							thumbViewMenuItem.setTitle(getString(R.string.action_list));
						}
					}
				}
				else if(index==1){
					outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
					if (outSFLol != null && outSFLol.isAdded()){

						if(!firstTimeCam){
							thumbViewMenuItem.setVisible(true);
						}else{
							thumbViewMenuItem.setVisible(false);
						}
						log("parentHandleOutgoing: "+parentHandleOutgoing);
						if(parentHandleOutgoing==-1){
							addMenuItem.setVisible(false);
							createFolderMenuItem.setVisible(false);
						}
						else{
							addMenuItem.setVisible(true);
							createFolderMenuItem.setVisible(true);
						}

						if(outSFLol.getItemCount()>0){
							selectMenuItem.setVisible(true);
							sortByMenuItem.setVisible(true);

						}
						else{
							selectMenuItem.setVisible(false);
							sortByMenuItem.setVisible(false);

						}
						searchMenuItem.setVisible(true);

						//Hide
						searchByDate.setVisible(false);
						upgradeAccountMenuItem.setVisible(false);
						pauseTransfersMenuIcon.setVisible(false);
						playTransfersMenuIcon.setVisible(false);
						addContactMenuItem.setVisible(false);
						unSelectMenuItem.setVisible(false);
						rubbishBinMenuItem.setVisible(false);
						clearRubbishBinMenuitem.setVisible(false);
						changePass.setVisible(false);
						importLinkMenuItem.setVisible(false);
						takePicture.setVisible(false);
						refreshMenuItem.setVisible(false);
						helpMenuItem.setVisible(false);
						gridSmallLargeMenuItem.setVisible(false);
						logoutMenuItem.setVisible(false);
						forgotPassMenuItem.setVisible(false);

						if (isList){
							thumbViewMenuItem.setTitle(getString(R.string.action_grid));
						}
						else{
							thumbViewMenuItem.setTitle(getString(R.string.action_list));
						}
					}
				}
				newChatMenuItem.setVisible(false);
				setStatusMenuItem.setVisible(false);
			}

			else if (drawerItem == DrawerItem.CONTACTS){
				log("createOptions CONTACTS");
				int index = viewPagerContacts.getCurrentItem();
				newChatMenuItem.setVisible(false);
				setStatusMenuItem.setVisible(false);
				if (index == 0){
					log("createOptions TAB CONTACTS");
					String contactsTag = getFragmentTag(R.id.contact_tabs_pager, 0);
					cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(contactsTag);

					//Show
					addContactMenuItem.setVisible(true);
					if(!firstTimeCam){
						thumbViewMenuItem.setVisible(true);
					}else{
						thumbViewMenuItem.setVisible(false);
					}					upgradeAccountMenuItem.setVisible(true);
					searchMenuItem.setVisible(false);
					scanQRcode.setVisible(true);

					if (cFLol != null && cFLol.isAdded()) {
						if(cFLol.getItemCount()>0){
							selectMenuItem.setVisible(true);
							sortByMenuItem.setVisible(true);
						}
						else{
							selectMenuItem.setVisible(false);
							sortByMenuItem.setVisible(false);
						}
						if (handleInviteContact != 0) {
							cFLol.invite(handleInviteContact);
							handleInviteContact = 0;
						}
					}
					else{
						log("The CONTACTS tab is null");
					}

					//Hide
					searchByDate.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_21");
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					changePass.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					changePass.setVisible(false);
					killAllSessions.setVisible(false);
					forgotPassMenuItem.setVisible(false);

					if (isList){
						thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
					}

					gridSmallLargeMenuItem.setVisible(false);
				}
				else if (index == 1){
					log("createOptions TAB SENT requests");

					String contactsTag = getFragmentTag(R.id.contact_tabs_pager, 1);
					sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(contactsTag);

					//Show
					addContactMenuItem.setVisible(true);
					upgradeAccountMenuItem.setVisible(true);
					scanQRcode.setVisible(true);

					if (sRFLol != null && sRFLol.isAdded()) {
						if(sRFLol.getItemCount()>0){
							selectMenuItem.setVisible(true);
						}
						else{
							selectMenuItem.setVisible(false);
						}
					}

					//Hide
					searchByDate.setVisible(false);
					sortByMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					searchMenuItem.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_21");
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					changePass.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					changePass.setVisible(false);
					killAllSessions.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					gridSmallLargeMenuItem.setVisible(false);
					forgotPassMenuItem.setVisible(false);
				}
				else{
					log("createOptions TAB RECEIVED requests");

					String contactsTag = getFragmentTag(R.id.contact_tabs_pager, 2);
					rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(contactsTag);

					//Show
					upgradeAccountMenuItem.setVisible(true);

					if (rRFLol != null && rRFLol.isAdded()) {
						if(rRFLol.getItemCount()>0){
							selectMenuItem.setVisible(true);
						}
						else{
							selectMenuItem.setVisible(false);
						}
					}

					//Hide
					searchByDate.setVisible(false);
					searchMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_22");
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					changePass.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					gridSmallLargeMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					killAllSessions.setVisible(false);
					logoutMenuItem.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					scanQRcode.setVisible(false);
				}
			}
			else if (drawerItem == DrawerItem.SEARCH){
				log("createOptions search");
				if (sFLol != null && sFLol.isAdded()){
					if (createFolderMenuItem != null){

						//Hide
						searchByDate.setVisible(false);
						upgradeAccountMenuItem.setVisible(true);
						cancelAllTransfersMenuItem.setVisible(false);
						clearCompletedTransfers.setVisible(false);
						pauseTransfersMenuIcon.setVisible(false);
						playTransfersMenuIcon.setVisible(false);
						log("createFolderMenuItem.setVisible_23");
						createFolderMenuItem.setVisible(false);
						addContactMenuItem.setVisible(false);
						addMenuItem.setVisible(false);
						refreshMenuItem.setVisible(false);
						sortByMenuItem.setVisible(false);
						unSelectMenuItem.setVisible(false);
						changePass.setVisible(false);
						rubbishBinMenuItem.setVisible(false);
						clearRubbishBinMenuitem.setVisible(false);
						importLinkMenuItem.setVisible(false);
						takePicture.setVisible(false);
						refreshMenuItem.setVisible(false);
						helpMenuItem.setVisible(false);
						gridSmallLargeMenuItem.setVisible(false);
						logoutMenuItem.setVisible(false);
						forgotPassMenuItem.setVisible(false);
						newChatMenuItem.setVisible(false);
						setStatusMenuItem.setVisible(false);

						//Show
						if(sFLol.getNodes()!=null){
							if(sFLol.getNodes().size()!=0){
//							log("size after search: "+sFLol.getNodes().size());
								selectMenuItem.setVisible(true);
								if(!firstTimeCam){
									thumbViewMenuItem.setVisible(true);
								}else{
									thumbViewMenuItem.setVisible(false);
								}
								if (isList){
									thumbViewMenuItem.setTitle(getString(R.string.action_grid));
								}
								else{
									thumbViewMenuItem.setTitle(getString(R.string.action_list));
								}
							}
							else{
								selectMenuItem.setVisible(false);
								thumbViewMenuItem.setVisible(false);
							}
						}
					}
				}
			}
			else if (drawerItem == DrawerItem.ACCOUNT){
				log("createOptions ACCOUNT");

				if (createFolderMenuItem != null) {

					//Hide
					searchByDate.setVisible(false);
					helpMenuItem.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					log("createFolderMenuItem.setVisible_24");
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					selectMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
					clearCompletedTransfers.setVisible(false);
					gridSmallLargeMenuItem.setVisible(false);
					newChatMenuItem.setVisible(false);
					setStatusMenuItem.setVisible(false);

					if(accountFragment==Constants.MY_ACCOUNT_FRAGMENT){
						//Show
						refreshMenuItem.setVisible(true);
						killAllSessions.setVisible(true);
						upgradeAccountMenuItem.setVisible(true);
						changePass.setVisible(true);
						logoutMenuItem.setVisible(true);
						forgotPassMenuItem.setVisible(false);
						searchMenuItem.setVisible(false);

						int index = viewPagerMyAccount.getCurrentItem();
						if(index==0){
							String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
							log("Exists MK in: "+path);
							File file= new File(path);
							if(file.exists()){
								removeMK.setVisible(true);
								exportMK.setVisible(false);
							}
							else{
								removeMK.setVisible(false);
								exportMK.setVisible(true);
							}
						}
						else{
							removeMK.setVisible(false);
							exportMK.setVisible(false);
						}

						if (((MegaApplication) getApplication()).getMyAccountInfo()!= null && ((MegaApplication) getApplication()).getMyAccountInfo().getNumberOfSubscriptions() > 0) {
							cancelSubscription.setVisible(true);
						}
						else{
							cancelSubscription.setVisible(false);
						}
					}
					else{
						refreshMenuItem.setVisible(true);
						killAllSessions.setVisible(false);
						upgradeAccountMenuItem.setVisible(false);
						changePass.setVisible(false);
						logoutMenuItem.setVisible(true);
						forgotPassMenuItem.setVisible(false);

						cancelSubscription.setVisible(false);
						removeMK.setVisible(false);
						exportMK.setVisible(false);
					}
				}
			}

			else if (drawerItem == DrawerItem.TRANSFERS){
				log("in Transfers Section");

				//Hide
				searchByDate.setVisible(false);
				searchMenuItem.setVisible(false);
				createFolderMenuItem.setVisible(false);
				addContactMenuItem.setVisible(false);
				addMenuItem.setVisible(false);
				sortByMenuItem.setVisible(false);
				selectMenuItem.setVisible(false);
				unSelectMenuItem.setVisible(false);
				thumbViewMenuItem.setVisible(false);
				rubbishBinMenuItem.setVisible(false);
				clearRubbishBinMenuitem.setVisible(false);
				importLinkMenuItem.setVisible(false);
				takePicture.setVisible(false);
				refreshMenuItem.setVisible(false);
				helpMenuItem.setVisible(false);
				upgradeAccountMenuItem.setVisible(false);
				changePass.setVisible(false);
				cancelSubscription.setVisible(false);
				killAllSessions.setVisible(false);
				logoutMenuItem.setVisible(false);
				forgotPassMenuItem.setVisible(false);
				newChatMenuItem.setVisible(false);
				setStatusMenuItem.setVisible(false);

				cancelAllTransfersMenuItem.setVisible(true);

				String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 1);
				completedTFLol = (CompletedTransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
				if(completedTFLol!=null){
					if(completedTFLol.isAdded()){
						if(completedTFLol.isAnyTransferCompleted()){
							clearCompletedTransfers.setVisible(true);
						}
						else{
							clearCompletedTransfers.setVisible(false);
						}
					}
				}

				if (transfersInProgress != null) {
					if (transfersInProgress.size() > 0) {

						if (megaApi.areTransfersPaused(MegaTransfer.TYPE_DOWNLOAD) || megaApi.areTransfersPaused(MegaTransfer.TYPE_UPLOAD)) {
							log("Any transfer is paused");
							playTransfersMenuIcon.setVisible(true);
							pauseTransfersMenuIcon.setVisible(false);
							cancelAllTransfersMenuItem.setVisible(true);
						} else {
							log("No transfers paused");
							playTransfersMenuIcon.setVisible(false);
							pauseTransfersMenuIcon.setVisible(true);
							cancelAllTransfersMenuItem.setVisible(true);
						}
					} else {
						playTransfersMenuIcon.setVisible(false);
						pauseTransfersMenuIcon.setVisible(false);
						cancelAllTransfersMenuItem.setVisible(false);
					}
				} else {
					playTransfersMenuIcon.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
				}
			}

			else if (drawerItem == DrawerItem.SETTINGS){
				log("in Settings Section");
				if (sttFLol != null){

					//Hide
					searchByDate.setVisible(false);
					searchMenuItem.setVisible(false);
					log("createFolderMenuItem.setVisible_settings");
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					selectMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					upgradeAccountMenuItem.setVisible(true);
					changePass.setVisible(false);
					cancelSubscription.setVisible(false);
					killAllSessions.setVisible(false);
					logoutMenuItem.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
					clearCompletedTransfers.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					newChatMenuItem.setVisible(false);
					setStatusMenuItem.setVisible(false);
				}
			}
			else if (drawerItem == DrawerItem.CHAT){
				log("in Chat Section");
				ChatController chatController = new ChatController(this);
				if(Util.isChatEnabled()){

					if (rChatFL != null && rChatFL.isAdded()){

						if(Util.isOnline(this)){
							newChatMenuItem.setVisible(true);
							if(rChatFL.getItemCount()>0){
								selectMenuItem.setVisible(true);
							}
							else{
								selectMenuItem.setVisible(false);
							}
							setStatusMenuItem.setVisible(true);
						}
						else{
							newChatMenuItem.setVisible(false);
							selectMenuItem.setVisible(false);
							setStatusMenuItem.setVisible(false);
						}
					}

					//Hide
					searchByDate.setVisible(false);
					searchMenuItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					upgradeAccountMenuItem.setVisible(false);
					changePass.setVisible(false);
					cancelSubscription.setVisible(false);
					killAllSessions.setVisible(false);
					logoutMenuItem.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
					clearCompletedTransfers.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					addContactMenuItem.setVisible(false);
				}
				else{
					//Hide ALL
					searchByDate.setVisible(false);
					newChatMenuItem.setVisible(false);
					setStatusMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					selectMenuItem.setVisible(false);
					searchMenuItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					upgradeAccountMenuItem.setVisible(false);
					changePass.setVisible(false);
					cancelSubscription.setVisible(false);
					killAllSessions.setVisible(false);
					logoutMenuItem.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
					clearCompletedTransfers.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
				}
			}
		}
		else{
			log("Offline options shown");
			if (drawerItem == DrawerItem.CHAT) {
				log("in Chat Section without NET");
				ChatController chatController = new ChatController(this);
				if (Util.isChatEnabled()) {

					newChatMenuItem.setVisible(true);
					if (rChatFL != null) {
						if (Util.isOnline(this)) {
							selectMenuItem.setVisible(true);
							setStatusMenuItem.setVisible(true);
						} else {
							selectMenuItem.setVisible(false);
							setStatusMenuItem.setVisible(false);
						}
					}

					//Hide
					searchByDate.setVisible(false);
					addContactMenuItem.setVisible(false);
					searchMenuItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					upgradeAccountMenuItem.setVisible(false);
					changePass.setVisible(false);
					cancelSubscription.setVisible(false);
					killAllSessions.setVisible(false);
					logoutMenuItem.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
					clearCompletedTransfers.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
				}
				else {
					log("onCreateOptionsMenu: HIDE ALL options chat disabled");
					//Hide ALL
					searchByDate.setVisible(false);
					newChatMenuItem.setVisible(false);
					setStatusMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					selectMenuItem.setVisible(false);
					searchMenuItem.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
					addMenuItem.setEnabled(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					upgradeAccountMenuItem.setVisible(false);
					changePass.setVisible(false);
					cancelSubscription.setVisible(false);
					killAllSessions.setVisible(false);
					logoutMenuItem.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
					clearCompletedTransfers.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
				}
			}
			else{
				log("onCreateOptionsMenu: HIDE ALL options without NET");
				//Hide ALL
				searchByDate.setVisible(false);
				newChatMenuItem.setVisible(false);
				setStatusMenuItem.setVisible(false);
				addContactMenuItem.setVisible(false);
				selectMenuItem.setVisible(false);
				searchMenuItem.setVisible(false);
				createFolderMenuItem.setVisible(false);
				addMenuItem.setVisible(false);
				sortByMenuItem.setVisible(false);
				unSelectMenuItem.setVisible(false);
				thumbViewMenuItem.setVisible(false);
				addMenuItem.setEnabled(false);
				rubbishBinMenuItem.setVisible(false);
				clearRubbishBinMenuitem.setVisible(false);
				importLinkMenuItem.setVisible(false);
				takePicture.setVisible(false);
				refreshMenuItem.setVisible(false);
				helpMenuItem.setVisible(false);
				upgradeAccountMenuItem.setVisible(false);
				changePass.setVisible(false);
				cancelSubscription.setVisible(false);
				killAllSessions.setVisible(false);
				logoutMenuItem.setVisible(false);
				cancelAllTransfersMenuItem.setVisible(false);
				clearCompletedTransfers.setVisible(false);
				forgotPassMenuItem.setVisible(false);
				playTransfersMenuIcon.setVisible(false);
				pauseTransfersMenuIcon.setVisible(false);
			}
		}

		log("Call to super onCreateOptionsMenu");
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelected() ");
		fromTakePicture = -1;
		log("onOptionsItemSelected");
		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		if (megaApi != null){
			log("---------retryPendingConnections");
			megaApi.retryPendingConnections();
		}

		if (megaChatApi != null){
			megaChatApi.retryPendingConnections(false, null);
		}

		((MegaApplication) getApplication()).sendSignalPresenceActivity();

		int id = item.getItemId();
		switch(id){
			case android.R.id.home:{
				if (firstNavigationLevel && drawerItem != DrawerItem.SEARCH){
					drawerLayout.openDrawer(nV);
				}else{
					log("NOT firstNavigationLevel");
		    		if (drawerItem == DrawerItem.CLOUD_DRIVE){
		    			int index = viewPagerCDrive.getCurrentItem();
		    			if(index==1){
		    				//Rubbish Bin
		    				rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
		    				if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
		    					rubbishBinFLol.onBackPressed();
		    					return true;
		    				}
		    			}
		    			else{
		    				//Cloud Drive
		    				fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
		    				if (fbFLol != null && fbFLol.isAdded()){
		    					fbFLol.onBackPressed();
		    				}
		    			}
		    		}
		    		else if (drawerItem == DrawerItem.SHARED_ITEMS){
		    			int index = viewPagerShares.getCurrentItem();
		    			if(index==1){
		    				//OUTGOING
		    				outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
		    				if (outSFLol != null && outSFLol.isAdded()){
		    					outSFLol.onBackPressed();
		    				}
		    			}
		    			else{
		    				//InCOMING
		    				inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
							if (inSFLol != null && inSFLol.isAdded()){
		    					inSFLol.onBackPressed();
		    				}

		    			}
		    		}
					else if (drawerItem == DrawerItem.CAMERA_UPLOADS){
						if (cuFL != null){
							if(cuFL.isAdded()){
								long cameraUploadHandle = cuFL.getPhotoSyncHandle();
								MegaNode nps = megaApi.getNodeByHandle(cameraUploadHandle);
								if (nps != null){
									ArrayList<MegaNode> nodes = megaApi.getChildren(nps, MegaApiJava.ORDER_MODIFICATION_DESC);
									cuFL.setNodes(nodes);
									isSearchEnabled=false;
									setToolbarTitle();
									invalidateOptionsMenu();
								}
								return true;
							}

						}

					}else if (drawerItem == DrawerItem.MEDIA_UPLOADS){
						if (muFLol != null){
							if(muFLol.isAdded()){
								long cameraUploadHandle = muFLol.getPhotoSyncHandle();
								MegaNode nps = megaApi.getNodeByHandle(cameraUploadHandle);
								if (nps != null){
									ArrayList<MegaNode> nodes = megaApi.getChildren(nps, MegaApiJava.ORDER_MODIFICATION_DESC);
									muFLol.setNodes(nodes);
									setToolbarTitle();
									isSearchEnabled=false;
									invalidateOptionsMenu();
								}
								return true;
							}
						}
					}
		    		else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
		    			if (oFLol != null && oFLol.isAdded()){
		    				oFLol.onBackPressed();
		    				return true;
		    			}
		    		}
					else if (drawerItem == DrawerItem.INBOX){
						if (iFLol != null && iFLol.isAdded()){
							iFLol.onBackPressed();
							return true;
						}
					}
		    		else if (drawerItem == DrawerItem.SEARCH){

		    			if (sFLol != null && sFLol.isAdded()){
//		    				sFLol.onBackPressed();
		    				onBackPressed();
		    				return true;
		    			}
		    		}
		    		else if (drawerItem == DrawerItem.TRANSFERS){

						drawerItem = DrawerItem.CLOUD_DRIVE;
						if (nV != null){
							Menu nVMenu = nV.getMenu();
							MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
							cloudDrive.setChecked(true);
							cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
						}
						selectDrawerItemLollipop(drawerItem);
						return true;
		    		}
					else if (drawerItem == DrawerItem.ACCOUNT){

						switch(accountFragment){
							case Constants.UPGRADE_ACCOUNT_FRAGMENT:{
								log("Back to MyAccountFragment");
								setFirstNavigationLevel(true);
								displayedAccountType=-1;
								if (upAFL != null){
									drawerItem = DrawerItem.ACCOUNT;
									accountFragment=Constants.MY_ACCOUNT_FRAGMENT;
									selectDrawerItemLollipop(drawerItem);
									if (nV != null){
										Menu nVMenu = nV.getMenu();
										MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
										resetNavigationViewMenu(nVMenu);
										hidden.setChecked(true);
									}
								}
								return true;
							}
							case Constants.CC_FRAGMENT:{
								if (ccFL != null){
									displayedAccountType = ccFL.getParameterType();
								}
								showUpAF();
								return true;
							}
							case Constants.MONTHLY_YEARLY_FRAGMENT:{
								if (myFL != null && myFL.isAdded()){
									myFL.onBackPressed();
								}
								return true;
							}
						}

//						if (tFLol != null){
//							if (tFLol.onBackPressed() == 0){
//								drawerItem = DrawerItem.CLOUD_DRIVE;
//								if (nV != null){
//									Menu nVMenu = nV.getMenu();
//									MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
//									resetNavigationViewMenu(nVMenu);
//									cloudDrive.setChecked(true);
//									cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
//								}
//								selectDrawerItemLollipop(drawerItem);
//								return true;
//							}
//						}
					}
				}
		    	return true;
		    }
			case R.id.action_search:{
				log("Action search selected");
				textSubmitted = false;
				if (createFolderMenuItem != null){
					upgradeAccountMenuItem.setVisible(false);
					cancelAllTransfersMenuItem.setVisible(false);
					clearCompletedTransfers.setVisible(false);
					pauseTransfersMenuIcon.setVisible(false);
					playTransfersMenuIcon.setVisible(false);
					createFolderMenuItem.setVisible(false);
					addContactMenuItem.setVisible(false);
					addMenuItem.setVisible(false);
					refreshMenuItem.setVisible(false);
					sortByMenuItem.setVisible(false);
					unSelectMenuItem.setVisible(false);
					changePass.setVisible(false);
					rubbishBinMenuItem.setVisible(false);
					clearRubbishBinMenuitem.setVisible(false);
					importLinkMenuItem.setVisible(false);
					takePicture.setVisible(false);
					refreshMenuItem.setVisible(false);
					helpMenuItem.setVisible(false);
					gridSmallLargeMenuItem.setVisible(false);
					logoutMenuItem.setVisible(false);
					forgotPassMenuItem.setVisible(false);
					newChatMenuItem.setVisible(false);
					setStatusMenuItem.setVisible(false);
					selectMenuItem.setVisible(false);
					thumbViewMenuItem.setVisible(false);
				}
				return true;
			}
		    case R.id.action_import_link:{
		    	showImportLinkDialog();
		    	return true;
		    }
		    case R.id.action_take_picture:{
		    	fromTakePicture = Constants.TAKE_PICTURE_OPTION;
		    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
					if (!hasStoragePermission) {
						ActivityCompat.requestPermissions(this,
				                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								Constants.REQUEST_WRITE_STORAGE);
					}

					boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
					if (!hasCameraPermission) {
						ActivityCompat.requestPermissions(this,
				                new String[]{Manifest.permission.CAMERA},
								Constants.REQUEST_CAMERA);
					}

					if (hasStoragePermission && hasCameraPermission){
						this.takePicture();
					}
				}
		    	else{
		    		this.takePicture();
		    	}

		    	return true;
		    }
		    case R.id.action_menu_cancel_all_transfers:{
		    	showConfirmationCancelAllTransfers();
		    	return true;
		    }
			case R.id.action_menu_clear_completed_transfers:{
				showConfirmationClearCompletedTransfers();
				return true;
			}
	        case R.id.action_pause:{
	        	if (drawerItem == DrawerItem.TRANSFERS){
	        		log("Click on action_pause - play visible");
	        		megaApi.pauseTransfers(true, this);
	        		pauseTransfersMenuIcon.setVisible(false);
	        		playTransfersMenuIcon.setVisible(true);
	        	}

	        	return true;
	        }
	        case R.id.action_play:{
	        	log("Click on action_play - pause visible");
				pauseTransfersMenuIcon.setVisible(true);
				playTransfersMenuIcon.setVisible(false);
    			megaApi.pauseTransfers(false, this);

	        	return true;
	        }
	        case R.id.action_add_contact:{
	        	if (drawerItem == DrawerItem.CONTACTS||drawerItem == DrawerItem.CHAT){
					chooseAddContactDialog(false);
	        	}
	        	return true;
	        }
			case R.id.action_menu_new_chat:{
				if (drawerItem == DrawerItem.CHAT){
					log("Create new chat");
					chooseAddContactDialog(true);
				}

				return true;
			}
			case R.id.action_menu_set_status:{
				if (drawerItem == DrawerItem.CHAT){
					log("Action set status");
					showPresenceStatusDialog();
//					drawerItem = DrawerItem.SETTINGS;
//					if (nV != null){
//						Menu nVMenu = nV.getMenu();
////						MenuItem chat = nVMenu.findItem(R.id.navigation_item_chat);
////						chat.setTitle(getString(R.string.section_chat));
//						MenuItem mi = nVMenu.findItem(R.id.navigation_item_chat);
//						if (mi != null){
//							mi.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_chat));
//							mi.setChecked(false);
//						}
//						MenuItem settings = nVMenu.findItem(R.id.navigation_item_settings);
//						settings.setChecked(true);
//						settings.setIcon(ContextCompat.getDrawable(this, R.drawable.settings_red));
//					}
//					scrollToChat = true;
//					selectDrawerItemLollipop(drawerItem);
				}

				return true;
			}
	        case R.id.action_menu_kill_all_sessions:{
				aC.killAllSessions(this);
	        	return true;
	        }
	        case R.id.action_new_folder:{
	        	if (drawerItem == DrawerItem.CLOUD_DRIVE){
	        		showNewFolderDialog();
	        	}
	        	else if(drawerItem == DrawerItem.SHARED_ITEMS){
	        		showNewFolderDialog();
	        	}
	        	else if (drawerItem == DrawerItem.CONTACTS){
					chooseAddContactDialog(false);
	        	}
	        	return true;
	        }
	        case R.id.action_add:{
	        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
	    			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
	    			if (!hasStoragePermission) {
	    				ActivityCompat.requestPermissions(this,
	    		                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
								Constants.REQUEST_WRITE_STORAGE);
	    			}
	    		}

	        	if (drawerItem == DrawerItem.SHARED_ITEMS){
	        		if (viewPagerShares.getCurrentItem()==0){

						MegaNode checkNode = megaApi.getNodeByHandle(parentHandleIncoming);

						if((megaApi.checkAccess(checkNode, MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK)){
							this.showUploadPanel();
						}
						else if(megaApi.checkAccess(checkNode, MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK){
							this.showUploadPanel();
						}
						else if(megaApi.checkAccess(checkNode, MegaShare.ACCESS_READ).getErrorCode() == MegaError.API_OK){
							log("Not permissions to upload");
							AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setMessage(getString(R.string.no_permissions_upload));
//								builder.setTitle(R.string.op_not_allowed);
							builder.setCancelable(false).setPositiveButton(R.string.cam_sync_ok, new DialogInterface.OnClickListener() {
								   public void onClick(DialogInterface dialog, int id) {
										//do things
									   alertNotPermissionsUpload.dismiss();
								   }
							   });

							alertNotPermissionsUpload = builder.create();
							alertNotPermissionsUpload.show();
//								Util.brandAlertDialog(alertNotPermissionsUpload);
						}

	        		}
	        		else if(viewPagerShares.getCurrentItem()==1){
						this.showUploadPanel();
					}
	        	}
	        	else {
        			this.showUploadPanel();
	        	}

	        	return true;
	        }
	        case R.id.action_select:{
        		if (drawerItem == DrawerItem.CLOUD_DRIVE){
        			int index = viewPagerCDrive.getCurrentItem();
        			log("----------------------------------------INDEX: "+index);
        			if(index==1){
        				//Rubbish bin
        				rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
        				if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
            				rubbishBinFLol.selectAll();
            				if (rubbishBinFLol.showSelectMenuItem()){
            					selectMenuItem.setVisible(true);
            					unSelectMenuItem.setVisible(false);
								changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
							}
            				else{
            					selectMenuItem.setVisible(false);
            					unSelectMenuItem.setVisible(true);
            				}
            			}
        			}
        			else{
        				//Cloud Drive
        				fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
        				if (fbFLol != null && fbFLol.isAdded()){
        					fbFLol.selectAll();
                			if (fbFLol.showSelectMenuItem()){
                				selectMenuItem.setVisible(true);
                				unSelectMenuItem.setVisible(false);
								changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
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
					int index = viewPagerContacts.getCurrentItem();
					log("----------------------------------------INDEX: "+index);
					switch(index){
						case 0:{
							String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
							cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
							if (cFLol != null && cFLol.isAdded()){
								cFLol.selectAll();
								if (cFLol.showSelectMenuItem()){
									selectMenuItem.setVisible(true);
									unSelectMenuItem.setVisible(false);
									changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
								}
								else{
									selectMenuItem.setVisible(false);
									unSelectMenuItem.setVisible(true);
								}
							}
							break;
						}
						case 1:{
							String cFTag = getFragmentTag(R.id.contact_tabs_pager, 1);
							sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
							if (sRFLol != null && sRFLol.isAdded()){
								sRFLol.selectAll();
								if (sRFLol.showSelectMenuItem()){
									selectMenuItem.setVisible(true);
									unSelectMenuItem.setVisible(false);
									changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
								}
								else{
									selectMenuItem.setVisible(false);
									unSelectMenuItem.setVisible(true);
								}
							}
							break;
						}
						case 2:{
							String cFTag = getFragmentTag(R.id.contact_tabs_pager, 2);
							rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
							if (rRFLol != null && rRFLol.isAdded()){
								rRFLol.selectAll();
								if (rRFLol.showSelectMenuItem()){
									selectMenuItem.setVisible(true);
									unSelectMenuItem.setVisible(false);
									changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
								}
								else{
									selectMenuItem.setVisible(false);
									unSelectMenuItem.setVisible(true);
								}
							}
							break;
						}

					}
	        	}
	        	if (drawerItem == DrawerItem.SHARED_ITEMS){
	        		if (viewPagerShares.getCurrentItem()==0){
						inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
		        		if (inSFLol != null && inSFLol.isAdded()){
		        			inSFLol.selectAll();
		        			if (inSFLol.showSelectMenuItem()){
		        				selectMenuItem.setVisible(true);
		        				unSelectMenuItem.setVisible(false);
								changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
							}
		        			else{
		        				selectMenuItem.setVisible(false);
		        				unSelectMenuItem.setVisible(true);
		        			}
		        		}
	        		}
	        		else if (viewPagerShares.getCurrentItem()==1){
						outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
		        		if (outSFLol != null && outSFLol.isAdded()){
		        			outSFLol.selectAll();
		        			if (outSFLol.showSelectMenuItem()){
		        				selectMenuItem.setVisible(true);
		        				unSelectMenuItem.setVisible(false);
								changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
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
	        		if (oFLol != null && oFLol.isAdded()){
	    				oFLol.selectAll();
	    				if (oFLol.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
							changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
						}
	        			else{
							selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
    			}
				if (drawerItem == DrawerItem.CHAT){
					if (rChatFL != null && rChatFL.isAdded()){
						rChatFL.selectAll();
						if (rChatFL.showSelectMenuItem()){
							selectMenuItem.setVisible(true);
							unSelectMenuItem.setVisible(false);
							changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
						}
						else{
							selectMenuItem.setVisible(false);
							unSelectMenuItem.setVisible(true);
						}
					}
				}
	        	if (drawerItem == DrawerItem.INBOX){
	        		if (iFLol != null && iFLol.isAdded()){
	        			iFLol.selectAll();
	    				if (iFLol.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
							changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
						}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
    			}
	        	if (drawerItem == DrawerItem.SEARCH){
	        		if (sFLol != null && sFLol.isAdded()){
	        			sFLol.selectAll();
	    				if (sFLol.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
							changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
						}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
    			}
	        	if (drawerItem == DrawerItem.MEDIA_UPLOADS){
	        		if (muFLol != null && muFLol.isAdded()){
	        			muFLol.selectAll();
	        			if (muFLol.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
							changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
						}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
	        	}
	        	if (drawerItem == DrawerItem.CAMERA_UPLOADS){
	        		if (cuFL != null && cuFL.isAdded()){
	        			cuFL.selectAll();
	        			if (cuFL.showSelectMenuItem()){
	        				selectMenuItem.setVisible(true);
	        				unSelectMenuItem.setVisible(false);
							changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
	        			}
	        			else{
	        				selectMenuItem.setVisible(false);
	        				unSelectMenuItem.setVisible(true);
	        			}
	        		}
	        	}
	        	return true;
	        }
	        case R.id.action_grid_view_large_small:{
				if (drawerItem == DrawerItem.CAMERA_UPLOADS){
	        		if (cuFL != null){
	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("cuFLol");
	        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.detach(currentFragment);
	        			fragTransaction.commitNowAllowingStateLoss();

	        			isSmallGridCameraUploads = !isSmallGridCameraUploads;
	        			dbH.setSmallGridCamera(isSmallGridCameraUploads);

						if (isSmallGridCameraUploads){
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
						}else{
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
						}

//						isLargeGridCameraUploads = !isLargeGridCameraUploads;


//						if (isLargeGridCameraUploads){
//	        				gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
//	        			}
//	        			else{
//	        				gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
//	        			}

	        			fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.attach(currentFragment);
	        			fragTransaction.commitNowAllowingStateLoss();
	        		}
	        	}
	        	if (drawerItem == DrawerItem.MEDIA_UPLOADS){
	        		if (muFLol != null){
	        			Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("muFLol");
	        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.detach(currentFragment);
	        			fragTransaction.commitNowAllowingStateLoss();

						isSmallGridCameraUploads = !isSmallGridCameraUploads;
						dbH.setSmallGridCamera(isSmallGridCameraUploads);

						if (isSmallGridCameraUploads){
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
						}else{
							gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
						}

//	        			isLargeGridCameraUploads = !isLargeGridCameraUploads;
//
//						if (isLargeGridCameraUploads){
//	        				gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview_small));
//	        			}
//	        			else{
//	        				gridSmallLargeMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_gridview));
//	        			}

	        			fragTransaction = getSupportFragmentManager().beginTransaction();
	        			fragTransaction.attach(currentFragment);
	        			fragTransaction.commitNow();
	        		}
	        	}
	        	return true;
	        }
	        case R.id.action_grid:{
	        	log("action_grid selected");
	        	//Redraw the section headers.
	        	if(oFLol != null) {
                    oFLol.floatingItemDecoration = null;
                }
	        	if (drawerItem == DrawerItem.CAMERA_UPLOADS){
	        		log("action_grid_list in CameraUploads");
	        		isListCameraUploads = !isListCameraUploads;
	    			dbH.setPreferredViewListCamera(isListCameraUploads);
	    			log("dbH.setPreferredViewListCamera: "+isListCameraUploads);
	    			if (isListCameraUploads){
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}
					selectDrawerItemLollipop(drawerItem);
	        	}
	        	else if (drawerItem == DrawerItem.MEDIA_UPLOADS){
	        		log("action_grid_list in MediaUploads");
	        		isListCameraUploads = !isListCameraUploads;
	    			dbH.setPreferredViewListCamera(isListCameraUploads);
	    			log("dbH.setPreferredViewListCamera: "+isListCameraUploads);
	    			if (isListCameraUploads){
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}
	        		if (muFLol != null){
        				Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("muFLol");
        				FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
        				fragTransaction.detach(currentFragment);
        				fragTransaction.commitNowAllowingStateLoss();

        				if (isListCameraUploads){
    	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
    	    				gridSmallLargeMenuItem.setVisible(false);
    	    				searchMenuItem.setVisible(true);
    					}
    					else{
    						thumbViewMenuItem.setTitle(getString(R.string.action_list));
							if(!firstTimeCam) {
								gridSmallLargeMenuItem.setVisible(true);
							}else{
								gridSmallLargeMenuItem.setVisible(false);
							}
    						searchMenuItem.setVisible(false);

    	    			}

        				fragTransaction = getSupportFragmentManager().beginTransaction();
        				fragTransaction.attach(currentFragment);
        				fragTransaction.commitNowAllowingStateLoss();
        			}
        		}
	        	else{
		        	isList = !isList;
	    			dbH.setPreferredViewList(isList);

	    			if (isList){
	    				thumbViewMenuItem.setTitle(getString(R.string.action_grid));
					}
					else{
						thumbViewMenuItem.setTitle(getString(R.string.action_list));
	    			}

					if(cloudPageAdapter!=null){
						cloudPageAdapter.notifyDataSetChanged();
					}

					if(sharesPageAdapter!=null){
						sharesPageAdapter.notifyDataSetChanged();
					}
					//Refresh OfflineFragmentLollipop layout even current fragment isn't OfflineFragmentLollipop.
                    if (oFLol != null && oFLol.isAdded()){
                        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("oFLol");
                        FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
                        fragTransaction.detach(currentFragment);
                        fragTransaction.commitNowAllowingStateLoss();
                        oFLol.floatingItemDecoration = null;
                        oFLol.setIsList(isList);
                        oFLol.setPathNavigation(pathNavigationOffline);
                        //oFLol.setGridNavigation(false);
                        //oFLol.setParentHandle(parentHandleSharedWithMe);
    
                        fragTransaction = getSupportFragmentManager().beginTransaction();
                        fragTransaction.attach(currentFragment);
                        fragTransaction.commitNowAllowingStateLoss();
                    }
                    
	    			if(drawerItem == DrawerItem.INBOX){
	    				selectDrawerItemLollipop(drawerItem);
	    			}
	    			else if (drawerItem == DrawerItem.CONTACTS){
	    				String cFTagC = getFragmentTag(R.id.contact_tabs_pager, 0);
			    		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTagC);
			        	if (cFLol != null){

		        			FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
		        			fragTransaction.detach(cFLol);
		        			fragTransaction.commitNowAllowingStateLoss();

		        			cFLol.setIsList(isList);

		        			fragTransaction = getSupportFragmentManager().beginTransaction();
		        			fragTransaction.attach(cFLol);
		        			fragTransaction.commitNowAllowingStateLoss();

		        		}
	    			}
//	    			else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
//	    				if (oFLol != null && oFLol.isAdded()){
//	        				Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("oFLol");
//	        				FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
//	        				fragTransaction.detach(currentFragment);
//	        				fragTransaction.commitNowAllowingStateLoss();
//
//	        				oFLol.setIsList(isList);
//	        				oFLol.setPathNavigation(pathNavigationOffline);
//	        				//oFLol.setGridNavigation(false);
//	        				//oFLol.setParentHandle(parentHandleSharedWithMe);
//
//	        				fragTransaction = getSupportFragmentManager().beginTransaction();
//	        				fragTransaction.attach(currentFragment);
//	        				fragTransaction.commitNowAllowingStateLoss();
//
//		        		}
//	    			}
	    			else if (drawerItem == DrawerItem.SEARCH){
						selectDrawerItemLollipop(drawerItem);
	    			}

//		        	if (drawerItem == DrawerItem.CLOUD_DRIVE){
//
//		        	}
//		        	if (drawerItem == DrawerItem.INBOX){
//
//		        	}
//		        	if (drawerItem == DrawerItem.CONTACTS){
//
//		        	}
//		        	if (drawerItem == DrawerItem.SHARED_ITEMS){
//
//
//
//		        	}
//		        	if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
//
//	        		}
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
	        		showClearRubbishBinDialog();
	        	}
	        	return true;
	        }
	        case R.id.action_menu_refresh:{
	        	switch(drawerItem){
		        	case ACCOUNT:{
						//Refresh all the info of My Account
		        		Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
						intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
			    		intent.setAction(Constants.ACTION_REFRESH);
			    		intent.putExtra("PARENT_HANDLE", parentHandleBrowser);
			    		startActivityForResult(intent, Constants.REQUEST_CODE_REFRESH);
			    		break;
		        	}
	        	}
	        	return true;
	        }
	        case R.id.action_menu_sort_by:{

        		AlertDialog sortByDialog;
        		LayoutInflater inflater = getLayoutInflater();
        		View dialoglayout = inflater.inflate(R.layout.sortby_dialog, null);

        		TextView sortByNameTV = (TextView) dialoglayout.findViewById(R.id.sortby_dialog_name_text);
        		sortByNameTV.setText(getString(R.string.sortby_name));
        		ViewGroup.MarginLayoutParams nameMLP = (ViewGroup.MarginLayoutParams) sortByNameTV.getLayoutParams();
        		sortByNameTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		nameMLP.setMargins(Util.scaleWidthPx(25, outMetrics), Util.scaleHeightPx(15, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		TextView sortByDateTV = (TextView) dialoglayout.findViewById(R.id.sortby_dialog_date_text);
        		sortByDateTV.setText(getString(R.string.sortby_modification_date));
        		ViewGroup.MarginLayoutParams dateMLP = (ViewGroup.MarginLayoutParams) sortByDateTV.getLayoutParams();
        		sortByDateTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		dateMLP.setMargins(Util.scaleWidthPx(25, outMetrics), Util.scaleHeightPx(15, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		TextView sortBySizeTV = (TextView) dialoglayout.findViewById(R.id.sortby_dialog_size_text);
        		sortBySizeTV.setText(getString(R.string.sortby_size));
        		ViewGroup.MarginLayoutParams sizeMLP = (ViewGroup.MarginLayoutParams) sortBySizeTV.getLayoutParams();
        		sortBySizeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		sizeMLP.setMargins(Util.scaleWidthPx(25, outMetrics), Util.scaleHeightPx(15, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		final CheckedTextView ascendingCheck = (CheckedTextView) dialoglayout.findViewById(R.id.sortby_dialog_ascending_check);
        		ascendingCheck.setText(getString(R.string.sortby_name_ascending));
        		ascendingCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		ascendingCheck.setCompoundDrawablePadding(Util.scaleWidthPx(34, outMetrics));
        		ViewGroup.MarginLayoutParams ascendingMLP = (ViewGroup.MarginLayoutParams) ascendingCheck.getLayoutParams();
        		ascendingMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		final CheckedTextView descendingCheck = (CheckedTextView) dialoglayout.findViewById(R.id.sortby_dialog_descending_check);
        		descendingCheck.setText(getString(R.string.sortby_name_descending));
        		descendingCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		descendingCheck.setCompoundDrawablePadding(Util.scaleWidthPx(34, outMetrics));
        		ViewGroup.MarginLayoutParams descendingMLP = (ViewGroup.MarginLayoutParams) descendingCheck.getLayoutParams();
        		descendingMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		final CheckedTextView newestCheck = (CheckedTextView) dialoglayout.findViewById(R.id.sortby_dialog_newest_check);
        		newestCheck.setText(getString(R.string.sortby_date_newest));
        		newestCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		newestCheck.setCompoundDrawablePadding(Util.scaleWidthPx(34, outMetrics));
        		ViewGroup.MarginLayoutParams newestMLP = (ViewGroup.MarginLayoutParams) newestCheck.getLayoutParams();
        		newestMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		final CheckedTextView oldestCheck = (CheckedTextView) dialoglayout.findViewById(R.id.sortby_dialog_oldest_check);
        		oldestCheck.setText(getString(R.string.sortby_date_oldest));
        		oldestCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		oldestCheck.setCompoundDrawablePadding(Util.scaleWidthPx(34, outMetrics));
        		ViewGroup.MarginLayoutParams oldestMLP = (ViewGroup.MarginLayoutParams) oldestCheck.getLayoutParams();
        		oldestMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		final CheckedTextView largestCheck = (CheckedTextView) dialoglayout.findViewById(R.id.sortby_dialog_largest_first_check);
        		largestCheck.setText(getString(R.string.sortby_size_largest_first));
        		largestCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		largestCheck.setCompoundDrawablePadding(Util.scaleWidthPx(34, outMetrics));
        		ViewGroup.MarginLayoutParams largestMLP = (ViewGroup.MarginLayoutParams) largestCheck.getLayoutParams();
        		largestMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		final CheckedTextView smallestCheck = (CheckedTextView) dialoglayout.findViewById(R.id.sortby_dialog_smallest_first_check);
        		smallestCheck.setText(getString(R.string.sortby_size_smallest_first));
        		smallestCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
        		smallestCheck.setCompoundDrawablePadding(Util.scaleWidthPx(34, outMetrics));
        		ViewGroup.MarginLayoutParams smallestMLP = (ViewGroup.MarginLayoutParams) smallestCheck.getLayoutParams();
        		smallestMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setView(dialoglayout);
				TextView textViewTitle = new TextView(ManagerActivityLollipop.this);
				textViewTitle.setText(getString(R.string.action_sort_by));
				textViewTitle.setTextSize(20);
				textViewTitle.setTextColor(0xde000000);
				textViewTitle.setPadding(Util.scaleWidthPx(23, outMetrics), Util.scaleHeightPx(20, outMetrics), 0, 0);
        		builder.setCustomTitle(textViewTitle);

        		sortByDialog = builder.create();
        		sortByDialog.show();
        		if(drawerItem==DrawerItem.CONTACTS){
        			switch(orderContacts){
		        		case MegaApiJava.ORDER_DEFAULT_ASC:{
		        			ascendingCheck.setChecked(true);
		        			descendingCheck.setChecked(false);
							newestCheck.setChecked(false);
							oldestCheck.setChecked(false);
		        			break;
		        		}
		        		case MegaApiJava.ORDER_DEFAULT_DESC:{
		        			ascendingCheck.setChecked(false);
		        			descendingCheck.setChecked(true);
							newestCheck.setChecked(false);
							oldestCheck.setChecked(false);
		        			break;
		        		}
						case MegaApiJava.ORDER_CREATION_ASC:{
							ascendingCheck.setChecked(false);
							descendingCheck.setChecked(false);
							newestCheck.setChecked(true);
							oldestCheck.setChecked(false);
							break;
						}
						case MegaApiJava.ORDER_CREATION_DESC:{
							ascendingCheck.setChecked(false);
							descendingCheck.setChecked(false);
							newestCheck.setChecked(false);
							oldestCheck.setChecked(true);
							break;
						}
	        		}
        		}
        		else if(drawerItem==DrawerItem.SAVED_FOR_OFFLINE){
        			log("orderOthers: "+orderOthers);
        			switch(orderOthers){
		        		case MegaApiJava.ORDER_DEFAULT_ASC:{
		        			log("ASCE");
		        			ascendingCheck.setChecked(true);
		        			descendingCheck.setChecked(false);
		        			break;
		        		}
		        		case MegaApiJava.ORDER_DEFAULT_DESC:{
		        			log("DESC");
		        			ascendingCheck.setChecked(false);
		        			descendingCheck.setChecked(true);
		        			break;
		        		}
        			}
        		}
        		else if(drawerItem==DrawerItem.SHARED_ITEMS){
					if(viewPagerShares!=null){
						int index = viewPagerShares.getCurrentItem();
						if(index==1){
							if (parentHandleOutgoing == -1){
								switch(orderOthers){
									case MegaApiJava.ORDER_DEFAULT_ASC:{
										log("ASCE");
										ascendingCheck.setChecked(true);
										descendingCheck.setChecked(false);
										break;
									}
									case MegaApiJava.ORDER_DEFAULT_DESC:{
										log("DESC");
										ascendingCheck.setChecked(false);
										descendingCheck.setChecked(true);
										break;
									}
								}
							}
							else{
								switch(orderCloud){
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
									case MegaApiJava.ORDER_MODIFICATION_ASC:{
										ascendingCheck.setChecked(false);
										descendingCheck.setChecked(false);
										newestCheck.setChecked(false);
										oldestCheck.setChecked(true);
										largestCheck.setChecked(false);
										smallestCheck.setChecked(false);
										break;
									}
									case MegaApiJava.ORDER_MODIFICATION_DESC:{
										ascendingCheck.setChecked(false);
										descendingCheck.setChecked(false);
										newestCheck.setChecked(true);
										oldestCheck.setChecked(false);
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
							}
						}
						else{
							if (parentHandleIncoming == -1){
								switch(orderOthers){
									case MegaApiJava.ORDER_DEFAULT_ASC:{
										log("ASCE");
										ascendingCheck.setChecked(true);
										descendingCheck.setChecked(false);
										break;
									}
									case MegaApiJava.ORDER_DEFAULT_DESC:{
										log("DESC");
										ascendingCheck.setChecked(false);
										descendingCheck.setChecked(true);
										break;
									}
								}
							}
							else{
								switch(orderCloud){
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
									case MegaApiJava.ORDER_MODIFICATION_ASC:{
										ascendingCheck.setChecked(false);
										descendingCheck.setChecked(false);
										newestCheck.setChecked(false);
										oldestCheck.setChecked(true);
										largestCheck.setChecked(false);
										smallestCheck.setChecked(false);
										break;
									}
									case MegaApiJava.ORDER_MODIFICATION_DESC:{
										ascendingCheck.setChecked(false);
										descendingCheck.setChecked(false);
										newestCheck.setChecked(true);
										oldestCheck.setChecked(false);
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
							}
						}
					}
				}
				else if(drawerItem==DrawerItem.CAMERA_UPLOADS||drawerItem==DrawerItem.MEDIA_UPLOADS){
					switch(orderCamera){
						case MegaApiJava.ORDER_MODIFICATION_ASC:{
							log("ASCE");
							newestCheck.setChecked(false);
							oldestCheck.setChecked(true);
							break;
						}
						case MegaApiJava.ORDER_MODIFICATION_DESC:{
							log("DESC");
							newestCheck.setChecked(true);
							oldestCheck.setChecked(false);
							break;
						}
					}
				}
        		else{
					log("orderCloud: "+orderCloud);
	        		switch(orderCloud){
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
//		        		case MegaApiJava.ORDER_CREATION_DESC:{
//		        			ascendingCheck.setChecked(false);
//		        			descendingCheck.setChecked(false);
//		        			newestCheck.setChecked(true);
//		        			oldestCheck.setChecked(false);
//		        			largestCheck.setChecked(false);
//		        			smallestCheck.setChecked(false);
//		        			break;
//		        		}
//		        		case MegaApiJava.ORDER_CREATION_ASC:{
//		        			ascendingCheck.setChecked(false);
//		        			descendingCheck.setChecked(false);
//		        			newestCheck.setChecked(false);
//		        			oldestCheck.setChecked(true);
//		        			largestCheck.setChecked(false);
//		        			smallestCheck.setChecked(false);
//		        			break;
//		        		}
						case MegaApiJava.ORDER_MODIFICATION_ASC:{
							ascendingCheck.setChecked(false);
							descendingCheck.setChecked(false);
							newestCheck.setChecked(false);
							oldestCheck.setChecked(true);
							largestCheck.setChecked(false);
							smallestCheck.setChecked(false);
							break;
						}
						case MegaApiJava.ORDER_MODIFICATION_DESC:{
							ascendingCheck.setChecked(false);
							descendingCheck.setChecked(false);
							newestCheck.setChecked(true);
							oldestCheck.setChecked(false);
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
	        	}

        		final AlertDialog dialog = sortByDialog;
	        	switch(drawerItem){
		        	case CONTACTS:{
						sortByDateTV.setText(getString(R.string.sortby_date));
						sortByDateTV.setVisibility(View.VISIBLE);
		        		newestCheck.setVisibility(View.VISIBLE);
		        		oldestCheck.setVisibility(View.VISIBLE);
		        		sortBySizeTV.setVisibility(View.GONE);
		        		largestCheck.setVisibility(View.GONE);
		        		smallestCheck.setVisibility(View.GONE);

		        		ascendingCheck.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
								newestCheck.setChecked(false);
								oldestCheck.setChecked(false);
								log("order contacts value _ "+orderContacts);
								if(orderContacts!=MegaApiJava.ORDER_DEFAULT_ASC){
									log("call to selectSortByContacts ASC _ "+orderContacts);
									selectSortByContacts(MegaApiJava.ORDER_DEFAULT_ASC);
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
								newestCheck.setChecked(false);
								oldestCheck.setChecked(false);
								log("order contacts value _ "+orderContacts);
								if(orderContacts!=MegaApiJava.ORDER_DEFAULT_DESC) {
									log("call to selectSortByContacts DESC _ "+orderContacts);
									selectSortByContacts(MegaApiJava.ORDER_DEFAULT_DESC);
								}
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
								log("order contacts value _ "+orderContacts);
								if(orderContacts!=MegaApiJava.ORDER_CREATION_ASC){
									log("call to selectSortByContacts ASC _ "+orderContacts);
									selectSortByContacts(MegaApiJava.ORDER_CREATION_ASC);
								}
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
								log("order contacts value _ "+orderContacts);
								if(orderContacts!=MegaApiJava.ORDER_CREATION_DESC) {
									log("call to selectSortByContacts DESC _ "+orderContacts);
									selectSortByContacts(MegaApiJava.ORDER_CREATION_DESC);
								}
								if (dialog != null){
									dialog.dismiss();
								}
							}
						});

		        		break;
		        	}
		        	case SAVED_FOR_OFFLINE: {

		        		sortByDateTV.setVisibility(View.GONE);
		        		newestCheck.setVisibility(View.GONE);
		        		oldestCheck.setVisibility(View.GONE);
		        		sortBySizeTV.setVisibility(View.GONE);
		        		largestCheck.setVisibility(View.GONE);
		        		smallestCheck.setVisibility(View.GONE);

		        		ascendingCheck.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
								if(orderOthers!=MegaApiJava.ORDER_DEFAULT_ASC) {
									selectSortByOffline(MegaApiJava.ORDER_DEFAULT_ASC);
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
								if(orderOthers!=MegaApiJava.ORDER_DEFAULT_DESC) {
									selectSortByOffline(MegaApiJava.ORDER_DEFAULT_DESC);
								}
			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});

		        		break;

		        	}
		        	case SHARED_ITEMS: {

						if(firstNavigationLevel){

							if (viewPagerShares.getCurrentItem()==0){
								//Incoming Shares
								sortByNameTV.setText(getString(R.string.sortby_owner_mail));
							}
							else{
								sortByNameTV.setText(getString(R.string.sortby_name));
							}

							sortByDateTV.setVisibility(View.GONE);
							newestCheck.setVisibility(View.GONE);
							oldestCheck.setVisibility(View.GONE);
							sortBySizeTV.setVisibility(View.GONE);
							largestCheck.setVisibility(View.GONE);
							smallestCheck.setVisibility(View.GONE);

							ascendingCheck.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									ascendingCheck.setChecked(true);
									descendingCheck.setChecked(false);
									if(orderOthers!=MegaApiJava.ORDER_DEFAULT_ASC){
										refreshOthersOrder(MegaApiJava.ORDER_DEFAULT_ASC);
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
									if(orderOthers!=MegaApiJava.ORDER_DEFAULT_DESC){
										refreshOthersOrder(MegaApiJava.ORDER_DEFAULT_DESC);
									}

									if (dialog != null){
										dialog.dismiss();
									}
								}
							});
						}
						else{
							log("No first level navigation on Incoming Shares");
							sortByNameTV.setText(getString(R.string.sortby_name));

							ascendingCheck.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									ascendingCheck.setChecked(true);
									descendingCheck.setChecked(false);
									newestCheck.setChecked(false);
									oldestCheck.setChecked(false);
									largestCheck.setChecked(false);
									smallestCheck.setChecked(false);

									refreshCloudOrder(MegaApiJava.ORDER_DEFAULT_ASC);

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

									refreshCloudOrder(MegaApiJava.ORDER_DEFAULT_DESC);

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

									refreshCloudOrder(MegaApiJava.ORDER_MODIFICATION_DESC);

									if (dialog != null){
										dialog.dismiss();
									}
								}
							});

							oldestCheck.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									ascendingCheck.setChecked(false);
									descendingCheck.setChecked(false);;
									newestCheck.setChecked(false);
									oldestCheck.setChecked(true);
									largestCheck.setChecked(false);
									smallestCheck.setChecked(false);

									refreshCloudOrder(MegaApiJava.ORDER_MODIFICATION_ASC);

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

									refreshCloudOrder(MegaApiJava.ORDER_SIZE_DESC);

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

									refreshCloudOrder(MegaApiJava.ORDER_SIZE_ASC);

									if (dialog != null){
										dialog.dismiss();
									}
								}
							});
						}

		        		break;

		        	}
					case MEDIA_UPLOADS:
					case CAMERA_UPLOADS:{

						sortByNameTV.setVisibility(View.GONE);
						ascendingCheck.setVisibility(View.GONE);
						descendingCheck.setVisibility(View.GONE);
						sortBySizeTV.setVisibility(View.GONE);
						largestCheck.setVisibility(View.GONE);
						smallestCheck.setVisibility(View.GONE);

						oldestCheck.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
								descendingCheck.setChecked(false);
								if(orderCamera!=MegaApiJava.ORDER_MODIFICATION_ASC){
									selectSortUploads(MegaApiJava.ORDER_MODIFICATION_ASC);
								}

								if (dialog != null){
									dialog.dismiss();
								}
							}
						});

						newestCheck.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
								descendingCheck.setChecked(true);
								if(orderCamera!=MegaApiJava.ORDER_MODIFICATION_DESC){
									selectSortUploads(MegaApiJava.ORDER_MODIFICATION_DESC);
								}

								if (dialog != null){
									dialog.dismiss();
								}
							}
						});
						break;
					}
		        	case CLOUD_DRIVE:
		        	case INBOX:{

		        		ascendingCheck.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(true);
			        			descendingCheck.setChecked(false);
			        			newestCheck.setChecked(false);
			        			oldestCheck.setChecked(false);
			        			largestCheck.setChecked(false);
			        			smallestCheck.setChecked(false);

								refreshCloudOrder(MegaApiJava.ORDER_DEFAULT_ASC);

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

								refreshCloudOrder(MegaApiJava.ORDER_DEFAULT_DESC);

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

								refreshCloudOrder(MegaApiJava.ORDER_MODIFICATION_DESC);

								if (dialog != null){
									dialog.dismiss();
								}
							}
						});

						oldestCheck.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ascendingCheck.setChecked(false);
								descendingCheck.setChecked(false);;
								newestCheck.setChecked(false);
								oldestCheck.setChecked(true);
								largestCheck.setChecked(false);
								smallestCheck.setChecked(false);

								refreshCloudOrder(MegaApiJava.ORDER_MODIFICATION_ASC);

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

								refreshCloudOrder(MegaApiJava.ORDER_SIZE_DESC);

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

			        			refreshCloudOrder(MegaApiJava.ORDER_SIZE_ASC);

			        			if (dialog != null){
			        				dialog.dismiss();
			        			}
							}
						});

		        		break;
	        		}
//		        	default:{
//		        		Intent intent = new Intent(managerActivity, SortByDialogActivity.class);
//			    		intent.setAction(SortByDialogActivity.ACTION_SORT_BY);
//			    		startActivityForResult(intent, REQUEST_CODE_SORT_BY);
//			    		break;
//		        	}
	        	}
	        	return true;
	        }
			case R.id.action_search_by_date:{
				Intent intent = new Intent(this, SearchByDateActivityLollipop.class);
				startActivityForResult(intent, Constants.ACTION_SEARCH_BY_DATE);
				return  true;
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
	        	if (nV != null){
					Menu nVMenu = nV.getMenu();
					MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
					resetNavigationViewMenu(nVMenu);
					hidden.setChecked(true);
				}
				drawerItem = DrawerItem.ACCOUNT;
				accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
				displayedAccountType = -1;
				selectDrawerItemLollipop(drawerItem);
				return true;
	        }

	        case R.id.action_menu_change_pass:{
	        	Intent intent = new Intent(this, ChangePasswordActivityLollipop.class);
				startActivity(intent);
				return true;
	        }
	        case R.id.action_menu_remove_MK:{
				log("remove MK option selected");
				showConfirmationRemoveMK();
				return true;
	        }
	        case R.id.action_menu_export_MK:{
	        	log("export MK option selected");

				showMKLayout();
	        	return true;
	        }
	        case R.id.action_menu_logout:{
				log("action menu logout pressed");
				passwordReminderFromMyAccount = true;
				megaApi.shouldShowPasswordReminderDialog(true, this);
//				showRememberPasswordDialog(true);

//				AccountController aC = new AccountController(this);
//				aC.logout(this, megaApi);
	        	return true;
	        }
	        case R.id.action_menu_cancel_subscriptions:{
				log("action menu cancel subscriptions pressed");
	        	if (megaApi != null){
	        		//Show the message
	        		showCancelMessage();
	        	}
	        	return true;
	        }
			case R.id.action_menu_forgot_pass:{
				log("action menu forgot pass pressed");
				String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
				maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
				if(maFLol!=null){
					showConfirmationResetPasswordFromMyAccount();
				}
				return true;
			}
			case R.id.action_scan_qr: {
				log("action menu scan QR code pressed");
				ScanCodeFragment fragment = new ScanCodeFragment();
				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
				Intent intent = new Intent(this, QRCodeActivity.class);
				intent.putExtra("contacts", true);
				startActivity(intent);
				return true;
			}
            default:{
	            return super.onOptionsItemSelected(item);
            }
		}
	}

	public void hideMKLayout(){
		log("hideMKLayout");
		mkLayoutVisible= false;

		tB.setVisibility(View.VISIBLE);
		abL.setVisibility(View.VISIBLE);

		eRKeyF = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
//			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			window.setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_dark_primary_color));
			window.setStatusBarColor(0);
		}
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD){
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		supportInvalidateOptionsMenu();
		selectDrawerItemLollipop(drawerItem);
	}

	public void showMKLayout(){
		log("showMKLayout");

		mkLayoutVisible=true;

		aB.setSubtitle(null);
		tB.setVisibility(View.GONE);

		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (currentFragment != null){
			getSupportFragmentManager().beginTransaction().remove(currentFragment).commitNow();
		}

		if (eRKeyF == null){
			eRKeyF = new ExportRecoveryKeyFragment();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_container, eRKeyF, "eRKeyF");
		ft.commitNowAllowingStateLoss();

		tabLayoutContacts.setVisibility(View.GONE);
		viewPagerContacts.setVisibility(View.GONE);
		tabLayoutShares.setVisibility(View.GONE);
		viewPagerShares.setVisibility(View.GONE);
		tabLayoutCloud.setVisibility(View.GONE);
		viewPagerCDrive.setVisibility(View.GONE);
		tabLayoutMyAccount.setVisibility(View.GONE);
		viewPagerMyAccount.setVisibility(View.GONE);
		tabLayoutTransfers.setVisibility(View.GONE);
		viewPagerTransfers.setVisibility(View.GONE);
		abL.setVisibility(View.GONE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_login));
		}
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD){
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		fragmentContainer.setVisibility(View.VISIBLE);
		drawerLayout.closeDrawer(Gravity.LEFT);
		drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		supportInvalidateOptionsMenu();
		hideFabButton();
	}

	public void refreshAfterMovingToRubbish(){
		log("refreshAfterMovingToRubbish");

		if (drawerItem == DrawerItem.CLOUD_DRIVE) {
			if(cloudPageAdapter!=null){
				cloudPageAdapter.notifyDataSetChanged();
			}
		}
		else if (drawerItem == DrawerItem.INBOX){
			if (iFLol != null && iFLol.isAdded()) {
				iFLol.hideMultipleSelect();
				iFLol.refresh();
			}

			//Refresh Rubbish Fragment
			if(cloudPageAdapter!=null){
				rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
				if (rubbishBinFLol != null){
					if(rubbishBinFLol.isAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderCloud);
						rubbishBinFLol.setNodes(nodes);
						rubbishBinFLol.getRecyclerView().invalidate();
					}
				}
			}
		}
		else if (drawerItem == DrawerItem.SHARED_ITEMS){
			if(sharesPageAdapter!=null){
				outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
				if(outSFLol!=null){
					if(outSFLol.isAdded()){
						outSFLol.hideMultipleSelect();
						outSFLol.refreshContent();
					}
				}

				inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
				if(inSFLol!=null){
					if(inSFLol.isAdded()){
						inSFLol.hideMultipleSelect();
						inSFLol.refresh();
					}
				}
			}

			//Refresh Rubbish Fragment
			if(cloudPageAdapter!=null){
				rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
				if (rubbishBinFLol != null){
					if(rubbishBinFLol.isAdded()){
						ArrayList<MegaNode> nodes;
						if(parentHandleRubbish == -1){
							nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderCloud);
						}
						else{
							nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderCloud);
						}

						rubbishBinFLol.setNodes(nodes);
						rubbishBinFLol.getRecyclerView().invalidate();
					}
				}
			}
		}else if(drawerItem == DrawerItem.SEARCH){
			if(sFLol!=null){
				if(sFLol.isAdded()){
					sFLol.hideMultipleSelect();
					sFLol.refresh();
				}
			}
			//Refresh Rubbish Fragment
			if(cloudPageAdapter!=null){
				rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
				if (rubbishBinFLol != null){
					if(rubbishBinFLol.isAdded()){
						ArrayList<MegaNode> nodes;
						if(parentHandleRubbish == -1){
							nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderCloud);
						}
						else{
							nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderCloud);
						}

						rubbishBinFLol.setNodes(nodes);
						rubbishBinFLol.getRecyclerView().invalidate();
					}
				}
			}

		}
		setToolbarTitle();
	}

	public void refreshAfterMoving() {
		log("refreshAfterMoving");
		if (drawerItem == DrawerItem.CLOUD_DRIVE) {
			if(cloudPageAdapter!=null){
				cloudPageAdapter.notifyDataSetChanged();
			}
		}
		else if (drawerItem == DrawerItem.INBOX) {
			if (iFLol != null && iFLol.isAdded()) {
				iFLol.hideMultipleSelect();
				iFLol.refresh();
			}

			if(cloudPageAdapter!=null){
				fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
				if (fbFLol != null){
					if(fbFLol.isAdded()){
						ArrayList<MegaNode> nodes;
						if(parentHandleBrowser==-1){
							nodes = megaApi.getChildren(megaApi.getNodeByHandle(megaApi.getRootNode().getHandle()), orderCloud);
						}
						else{
							nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderCloud);
						}
						log("nodes: "+nodes.size());
						fbFLol.hideMultipleSelect();
						fbFLol.setNodes(nodes);
						fbFLol.setOverviewLayout();
						fbFLol.getRecyclerView().invalidate();
					}
				}
				else{
					log("FileBrowser is NULL after move");
				}
			}

		}
		else if(drawerItem == DrawerItem.SHARED_ITEMS) {
			inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
			if (inSFLol != null && inSFLol.isAdded()){

				inSFLol.hideMultipleSelect();
				inSFLol.getRecyclerView().invalidate();
			}

			outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
			if (outSFLol != null && outSFLol.isAdded()){
				outSFLol.hideMultipleSelect();
				outSFLol.refresh();
			}

			//Refresh Cloud Drive
			if(cloudPageAdapter!=null){
				cloudPageAdapter.notifyDataSetChanged();
			}
		}else if(drawerItem == DrawerItem.SEARCH){
			if (sFLol != null && sFLol.isAdded()){

				sFLol.hideMultipleSelect();
				sFLol.refresh();
			}
		}

		setToolbarTitle();
	}

	public void refreshAfterRemoving(){
		log("refreshAfterRemoving");
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			int index = viewPagerCDrive.getCurrentItem();
			log("----------------------------------------INDEX: "+index);
			if(index==1){
				//Rubbish bin
				rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
				if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
					rubbishBinFLol.hideMultipleSelect();

					if (isClearRubbishBin){
						isClearRubbishBin = false;
						parentHandleRubbish = megaApi.getRubbishNode().getHandle();
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderCloud);
						rubbishBinFLol.setNodes(nodes);
						rubbishBinFLol.getRecyclerView().invalidate();
					}
					else{
						ArrayList<MegaNode> nodes;
						if(parentHandleRubbish==-1){
							nodes = megaApi.getChildren(megaApi.getNodeByHandle(megaApi.getRubbishNode().getHandle()), orderCloud);
						}
						else{
							nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderCloud);
						}
						rubbishBinFLol.setNodes(nodes);
						rubbishBinFLol.getRecyclerView().invalidate();
					}
				}
			}
			else{
				//Cloud Drive
				log("Why executed?");
			}
		}
		else if (drawerItem == DrawerItem.INBOX){
			if (iFLol != null && iFLol.isAdded()){
//							ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(iFLol.getParentHandle()), orderGetChildren);
//							rubbishBinFLol.setNodes(nodes);
				iFLol.hideMultipleSelect();
				iFLol.refresh();
			}
		}else if(drawerItem == DrawerItem.SEARCH){
			if (sFLol != null && sFLol.isAdded()){
				sFLol.hideMultipleSelect();
				sFLol.refresh();
			}
		}
	}

	@Override
	public void onBackPressed() {
		log("onBackPressedLollipop");

		if (drawerLayout.isDrawerOpen(nV)){
    		drawerLayout.closeDrawer(Gravity.LEFT);
    		return;
    	}

		if (megaApi == null){
			megaApi = ((MegaApplication)getApplication()).getMegaApi();
		}

		if (megaApi != null){
			log("---------retryPendingConnections");
			megaApi.retryPendingConnections();
		}

		if (megaChatApi != null){
			megaChatApi.retryPendingConnections(false, null);
		}

		try {
			statusDialog.dismiss();
		}
		catch (Exception ex) {}

		log("DRAWERITEM: " + drawerItem);

		if (turnOnNotifications){
			deleteTurnOnNotificationsFragment();
			return;
		}

		if (mkLayoutVisible){
			hideMKLayout();
			return;
		}

		if (drawerItem == DrawerItem.CLOUD_DRIVE){

			int index = viewPagerCDrive.getCurrentItem();
			if(index==1){
				//Rubbish Bin
				if (cloudPageAdapter != null) {
					rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
					if (rubbishBinFLol != null && rubbishBinFLol.isAdded()) {
						if (rubbishBinFLol.onBackPressed() == 0) {
							viewPagerCDrive.setCurrentItem(0);
						}
						return;
					}
				}
			}
			else if(index==0){
				//Cloud Drive
				if (cloudPageAdapter != null) {
					fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
					if (fbFLol != null && fbFLol.isAdded()) {
						if (fbFLol.onBackPressed() == 0) {
							super.onBackPressed();
						}
						return;
					}
				}
			}

			super.onBackPressed();
		}
		else if (drawerItem == DrawerItem.TRANSFERS){

			drawerItem = DrawerItem.CLOUD_DRIVE;
			if (nV != null){
				Menu nVMenu = nV.getMenu();
				MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
				resetNavigationViewMenu(nVMenu);
				cloudDrive.setChecked(true);
				cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
			}
			selectDrawerItemLollipop(drawerItem);
			return;

    	}
		else if (drawerItem == DrawerItem.INBOX){
			if (iFLol != null && iFLol.isAdded()){
				if (iFLol.onBackPressed() == 0){
					drawerItem = DrawerItem.CLOUD_DRIVE;
					if (nV != null){
						Menu nVMenu = nV.getMenu();
						MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
						resetNavigationViewMenu(nVMenu);
						cloudDrive.setChecked(true);
						cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
					}
					selectDrawerItemLollipop(drawerItem);
					return;
				}
			}
		}
		else if (drawerItem == DrawerItem.SETTINGS){

			if (!Util.isOnline(this)){
				showOfflineMode();
			}
			else{
				drawerItem = DrawerItem.CLOUD_DRIVE;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
					resetNavigationViewMenu(nVMenu);
					cloudDrive.setChecked(true);
					cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
				}
				selectDrawerItemLollipop(drawerItem);
			}

			return;
		}
		else if (drawerItem == DrawerItem.SHARED_ITEMS){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){
				//OUTGOING
				outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
				if (outSFLol != null && outSFLol.isAdded()){
					if (outSFLol.onBackPressed() == 0){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						if (nV != null){
							Menu nVMenu = nV.getMenu();
							MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
							cloudDrive.setChecked(true);
							cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
						}
						selectDrawerItemLollipop(drawerItem);
						return;
					}
				}
			}
			else{
				//InCOMING
				inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
				if (inSFLol != null && inSFLol.isAdded()){
					if (inSFLol.onBackPressed() == 0){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						if (nV != null){
							Menu nVMenu = nV.getMenu();
							MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
							cloudDrive.setChecked(true);
							cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
						}
						selectDrawerItemLollipop(drawerItem);
						return;
					}
				}
			}
		}
		else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){
			if (oFLol != null && oFLol.isAdded()){
				if (oFLol.onBackPressed() == 0){

					if (!Util.isOnline(this)){
						super.onBackPressed();
						return;
					}

					if (fbFLol != null){
						drawerItem = DrawerItem.CLOUD_DRIVE;
						if (nV != null){
							Menu nVMenu = nV.getMenu();
							MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
							cloudDrive.setChecked(true);
							cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
						}
						selectDrawerItemLollipop(drawerItem);
					}
					else{
						super.onBackPressed();
					}
					return;
				}
			}
		}
		else if (drawerItem == DrawerItem.CHAT){

			if (!Util.isOnline(this)){
				showOfflineMode();
			}
			else{
				drawerItem = DrawerItem.CLOUD_DRIVE;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
					resetNavigationViewMenu(nVMenu);
					cloudDrive.setChecked(true);
					cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
				}
				selectDrawerItemLollipop(drawerItem);
			}
		}
		else if (drawerItem == DrawerItem.CONTACTS){
			int index = viewPagerContacts.getCurrentItem();
			switch (index) {
				case 0:{
					//CONTACTS FRAGMENT
		    		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
		    		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		    		if (cFLol != null && cFLol.isAdded()){
		    			if (cFLol.onBackPressed() == 0){
		    				drawerItem = DrawerItem.CLOUD_DRIVE;
		    				if (nV != null){
								Menu nVMenu = nV.getMenu();
								MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
								resetNavigationViewMenu(nVMenu);
								cloudDrive.setChecked(true);
								cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
							}
							selectDrawerItemLollipop(drawerItem);
		    				return;
		    			}
		    		}
					break;
				}
				case 1:{
					String cFTag = getFragmentTag(R.id.contact_tabs_pager, 1);
					sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		    		if (sRFLol != null){
                        drawerItem = DrawerItem.CLOUD_DRIVE;
                        if (nV != null){
                            Menu nVMenu = nV.getMenu();
                            MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
                            cloudDrive.setChecked(true);
                            cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
                        }
                        selectDrawerItemLollipop(drawerItem);
                        return;
		    		}
					break;
				}
				case 2:{
					String cFTag = getFragmentTag(R.id.contact_tabs_pager, 2);
					rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		    		if (rRFLol != null){

						drawerItem = DrawerItem.CLOUD_DRIVE;
						if (nV != null){
							Menu nVMenu = nV.getMenu();
							MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
							cloudDrive.setChecked(true);
							cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
						}
						selectDrawerItemLollipop(drawerItem);
						return;
		    		}
					break;
				}
			}
		}
		else if (drawerItem == DrawerItem.ACCOUNT){
			log("MyAccountSection");
			log("The accountFragment is: "+accountFragment);
    		switch(accountFragment){

	    		case Constants.MY_ACCOUNT_FRAGMENT:{
	    			if (maFLol != null && maFLol.isAdded()){
	    				if (maFLol.onBackPressed() == 0){
		    				drawerItem = DrawerItem.CLOUD_DRIVE;
		    				if (nV != null){
								Menu nVMenu = nV.getMenu();
								MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
								resetNavigationViewMenu(nVMenu);
								cloudDrive.setChecked(true);
								cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
							}
							selectDrawerItemLollipop(drawerItem);
	    				}
	    			}
	    			return;
	    		}
	    		case Constants.UPGRADE_ACCOUNT_FRAGMENT:{
					log("Back to MyAccountFragment");
					displayedAccountType=-1;
	    			if (upAFL != null){
	    				drawerItem = DrawerItem.ACCOUNT;
						accountFragment=Constants.MY_ACCOUNT_FRAGMENT;
	    				selectDrawerItemLollipop(drawerItem);
	    				if (nV != null){
	    					Menu nVMenu = nV.getMenu();
	    					MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
	    					resetNavigationViewMenu(nVMenu);
	    					hidden.setChecked(true);
	    				}
	    			}
	    			return;
	    		}
	    		case Constants.CC_FRAGMENT:{
	    			if (ccFL != null){
						displayedAccountType = ccFL.getParameterType();
	    			}
					showUpAF();
	    			return;
	    		}
	    		case Constants.OVERQUOTA_ALERT:{
	    			if (upAFL != null){
	    				drawerItem = DrawerItem.CLOUD_DRIVE;
	    				if (nV != null){
							Menu nVMenu = nV.getMenu();
							MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
							cloudDrive.setChecked(true);
							cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
						}
						selectDrawerItemLollipop(drawerItem);
	    			}
	    			return;
	    		}
	    		case Constants.MONTHLY_YEARLY_FRAGMENT:{
	    			if (myFL != null && myFL.isAdded()){
	    				myFL.onBackPressed();
	    			}
	    			return;
	    		}
	    		default:{
	    			if (fbFLol != null){
	    				drawerItem = DrawerItem.CLOUD_DRIVE;
	    				if (nV != null){
							Menu nVMenu = nV.getMenu();
							MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
							resetNavigationViewMenu(nVMenu);
							cloudDrive.setChecked(true);
							cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
						}
						selectDrawerItemLollipop(drawerItem);
	    			}
	    		}
    		}
    	}
		else if (drawerItem == DrawerItem.CAMERA_UPLOADS){
			if (cuFL != null && cuFL.isAdded()){
    			if (cuFL.onBackPressed() == 0){
					visibilitySearch(false);
    				drawerItem = DrawerItem.CLOUD_DRIVE;
    				if (nV != null){
						Menu nVMenu = nV.getMenu();
						MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
						resetNavigationViewMenu(nVMenu);
						cloudDrive.setChecked(true);
						cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
					}
					selectDrawerItemLollipop(drawerItem);
    				return;
    			}
    		}
    	}
		else if (drawerItem == DrawerItem.MEDIA_UPLOADS){
			if (muFLol != null && muFLol.isAdded()){
    			if (muFLol.onBackPressed() == 0){
					visibilitySearch(false);
					drawerItem = DrawerItem.CLOUD_DRIVE;
    				if (nV != null){
						Menu nVMenu = nV.getMenu();
						MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
						resetNavigationViewMenu(nVMenu);
						cloudDrive.setChecked(true);
						cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
					}
					selectDrawerItemLollipop(drawerItem);
    				return;
    			}
    		}
    	}
		else if (drawerItem == DrawerItem.SEARCH){
    		if (sFLol != null && sFLol.isAdded()){
    			if (sFLol.onBackPressed() == 0){
    				drawerItem = DrawerItem.CLOUD_DRIVE;
    				if (nV != null){
						Menu nVMenu = nV.getMenu();
						MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
						resetNavigationViewMenu(nVMenu);
						cloudDrive.setChecked(true);
						cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
					}
    				selectDrawerItemLollipop(drawerItem);
    				return;
    			}
    		}
    	}
		else{
			super.onBackPressed();
			return;
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		log("onNavigationItemSelected");
		if(firstTimeCam){
			firstTimeCam = false;
			dbH.setCamSyncEnabled(false);
		}
		switch (menuItem.getItemId()){
			case R.id.navigation_item_cloud_drive:{
//				Snackbar.make(fragmentContainer, menuItem.getTitle() + " (" + menuItem.getItemId() + ")", Snackbar.LENGTH_LONG).show();
				drawerMenuItem = menuItem;
				drawerItem = DrawerItem.CLOUD_DRIVE;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
			case R.id.navigation_item_saved_for_offline:{
//				Snackbar.make(fragmentContainer, menuItem.getTitle() + " (" + menuItem.getItemId() + ")", Snackbar.LENGTH_LONG).show();
				drawerMenuItem = menuItem;
				drawerItem = DrawerItem.SAVED_FOR_OFFLINE;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.saved_for_offline_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
			case R.id.navigation_item_camera_uploads:{
//				Snackbar.make(fragmentContainer, menuItem.getTitle() + " (" + menuItem.getItemId() + ")", Snackbar.LENGTH_LONG).show();
				drawerMenuItem = menuItem;
				drawerItem = DrawerItem.CAMERA_UPLOADS;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
			case R.id.navigation_item_inbox:{
//				Snackbar.make(fragmentContainer, menuItem.getTitle() + " (" + menuItem.getItemId() + ")", Snackbar.LENGTH_LONG).show();
				drawerMenuItem = menuItem;
				drawerItem = DrawerItem.INBOX;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.inbox_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
			case R.id.navigation_item_shared_items:{
//				Snackbar.make(fragmentContainer, menuItem.getTitle() + " (" + menuItem.getItemId() + ")", Snackbar.LENGTH_LONG).show();
				drawerMenuItem = menuItem;
				drawerItem = DrawerItem.SHARED_ITEMS;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.shared_items_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
			case R.id.navigation_item_chat:{
				drawerMenuItem = menuItem;
				drawerItem = DrawerItem.CHAT;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_chat_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
			case R.id.navigation_item_contacts:{
//				Snackbar.make(fragmentContainer, menuItem.getTitle() + " (" + menuItem.getItemId() + ")", Snackbar.LENGTH_LONG).show();
				drawerMenuItem = menuItem;
				drawerItem = DrawerItem.CONTACTS;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.contacts_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
			case R.id.navigation_item_settings:{
//				Snackbar.make(fragmentContainer, menuItem.getTitle() + " (" + menuItem.getItemId() + ")", Snackbar.LENGTH_LONG).show();
				lastDrawerItem = drawerItem;
				drawerItem = DrawerItem.SETTINGS;
				if (nV != null){
					Menu nVMenu = nV.getMenu();
					resetNavigationViewMenu(nVMenu);
				}
				menuItem.setChecked(true);
				menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.settings_red));
				selectDrawerItemLollipop(drawerItem);
				break;
			}
		}
		drawerLayout.closeDrawer(Gravity.LEFT);

		return true;
	}

	public void showSnackbar(String s){
		log("showSnackbar");
		Snackbar snackbar = Snackbar.make(fragmentContainer, s, Snackbar.LENGTH_LONG);
		TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		snackbarTextView.setMaxLines(5);
		snackbar.show();
	}

	public void showSnackbarNotSpace(){
		log("showSnackbarNotSpace");
		Snackbar mySnackbar = Snackbar.make(fragmentContainer, R.string.error_not_enough_free_space, Snackbar.LENGTH_LONG);
		mySnackbar.setAction("Settings", new SnackbarNavigateOption(this));
		mySnackbar.show();
	}

	public void askConfirmationNoAppInstaledBeforeDownload (String parentPath, String url, long size, long [] hashes, String nodeToDownload){
		log("askConfirmationNoAppInstaledBeforeDownload");

		final String parentPathC = parentPath;
		final String urlC = url;
		final long [] hashesC = hashes;
		final long sizeC=size;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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


	public void askSizeConfirmationBeforeDownload(String parentPath, String url, long size, long [] hashes){
		log("askSizeConfirmationBeforeDownload");

		final String parentPathC = parentPath;
		final String urlC = url;
		final long [] hashesC = hashes;
		final long sizeC=size;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

	public void askSizeConfirmationBeforeChatDownload(String parentPath, ArrayList<MegaNode> nodeList, long size){
		log("askSizeConfirmationBeforeChatDownload");

		final String parentPathC = parentPath;
		final ArrayList<MegaNode> nodeListC = nodeList;
		final long sizeC = size;
		final ChatController chatC = new ChatController(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

	public void restoreFromRubbish(final MegaNode node) {
		log("restoreFromRubbish");

		restoreFromRubbish = true;

		MegaNode newParent = megaApi.getNodeByHandle(node.getRestoreHandle());
		if(newParent !=null){
			megaApi.moveNode(node, newParent, this);
		}
		else{
			log("restoreFromRubbish:The restore folder no longer exists");
		}
	}

	public void showRenameDialog(final MegaNode document, String text){
		log("showRenameDialog");
		LinearLayout layout = new LinearLayout(this);
	    layout.setOrientation(LinearLayout.VERTICAL);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);
//	    layout.setLayoutParams(params);

		final EditTextCursorWatcher input = new EditTextCursorWatcher(this, document.isFolder());
//		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
//		input.setHint(getString(R.string.context_new_folder_name));
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);

		input.setImeActionLabel(getString(R.string.context_rename),EditorInfo.IME_ACTION_DONE);
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

	    layout.addView(input, params);

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.setMargins(Util.scaleWidthPx(20, outMetrics), 0, Util.scaleWidthPx(17, outMetrics), 0);

		final RelativeLayout error_layout = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout, params1);

		final ImageView error_icon = new ImageView(ManagerActivityLollipop.this);
		error_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout.addView(error_icon);
		RelativeLayout.LayoutParams params_icon = (RelativeLayout.LayoutParams) error_icon.getLayoutParams();

		params_icon.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon.setLayoutParams(params_icon);

		error_icon.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		final TextView textError = new TextView(ManagerActivityLollipop.this);
		error_layout.addView(textError);
		RelativeLayout.LayoutParams params_text_error = (RelativeLayout.LayoutParams) textError.getLayoutParams();
		params_text_error.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error.setMargins(Util.scaleWidthPx(3, outMetrics), 0,0,0);
		textError.setLayoutParams(params_text_error);

		textError.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

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
				if(error_layout.getVisibility() == View.VISIBLE){
					error_layout.setVisibility(View.GONE);
					input.getBackground().mutate().clearColorFilter();
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});

		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
										  KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {

					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError.setText(getString(R.string.invalid_string));
						error_layout.setVisibility(View.VISIBLE);
						input.requestFocus();

					}else{
						boolean result=matches(regex, value);
						if(result){
							input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
							textError.setText(getString(R.string.invalid_characters));
							error_layout.setVisibility(View.VISIBLE);
							input.requestFocus();

						}else{
							nC.renameNode(document, value);
							renameDialog.dismiss();
						}
					}
					return true;
				}
				return false;
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.context_rename) + " "	+ new String(document.getName()));
		builder.setPositiveButton(getString(R.string.context_rename),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

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
		renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new   View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String value = input.getText().toString().trim();

				if (value.length() == 0) {
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
					textError.setText(getString(R.string.invalid_string));
					error_layout.setVisibility(View.VISIBLE);
					input.requestFocus();
				}
				else{
					boolean result=matches(regex, value);
					if(result){
						input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError.setText(getString(R.string.invalid_characters));
						error_layout.setVisibility(View.VISIBLE);
						input.requestFocus();

					}else{
						nC.renameNode(document, value);
						renameDialog.dismiss();
					}
				}
			}
		});
	}

	public static boolean matches(String regex, CharSequence input) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		return m.find();
	}

	public void showGetLinkActivity(long handle){
		log("showGetLinkActivity");
		Intent linkIntent = new Intent(this, GetLinkActivityLollipop.class);
		linkIntent.putExtra("handle", handle);
		startActivity(linkIntent);

		refreshAfterMovingToRubbish();
	}

	/*
	 * Display keyboard
	 */
	private void showKeyboardDelayed(final View view) {
		log("showKeyboardDelayed");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 50);
	}

	public void setIsGetLink(boolean value){
		this.isGetLink = value;
	}

	public void setIsClearRubbishBin(boolean value){
		this.isClearRubbishBin = value;
	}

	public void setMoveToRubbish(boolean value){
		this.moveToRubbish = value;
	}

	public void askConfirmationMoveToRubbish(final ArrayList<Long> handleList){
		log("askConfirmationMoveToRubbish");
		isClearRubbishBin=false;

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						//TODO remove the outgoing shares
						nC.moveToTrash(handleList, moveToRubbish);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		if(handleList!=null){

			if (handleList.size() > 0){
				MegaNode p = megaApi.getNodeByHandle(handleList.get(0));
				while (megaApi.getParentNode(p) != null){
					p = megaApi.getParentNode(p);
				}
				if (p.getHandle() != megaApi.getRubbishNode().getHandle()){

					setMoveToRubbish(true);

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(getResources().getString(R.string.confirmation_move_to_rubbish));

					builder.setPositiveButton(R.string.general_move, dialogClickListener);
					builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
					builder.show();
				}
				else{

					setMoveToRubbish(false);

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(getResources().getString(R.string.confirmation_delete_from_mega));

					//builder.setPositiveButton(R.string.context_delete, dialogClickListener);
					builder.setPositiveButton(R.string.context_remove, dialogClickListener);
					builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
					builder.show();
				}
			}
		}
		else{
			log("handleList NULL");
			return;
		}

	}

	public void showDialogInsertPassword(String link, boolean cancelAccount){
		log("showDialogInsertPassword");

		final String confirmationLink = link;
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

		final EditText input = new EditText(this);
		layout.addView(input, params);

//		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setHint(getString(R.string.edit_text_insert_pass));
		input.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
		input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//		input.setSelectAllOnFocus(true);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if(cancelAccount){
			log("cancelAccount action");
			input.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						String pass = input.getText().toString().trim();
						if(pass.equals("")||pass.isEmpty()){
							log("input is empty");
							input.setError(getString(R.string.invalid_string));
							input.requestFocus();
						}
						else {
							log("action DONE ime - cancel account");
							aC.confirmDeleteAccount(confirmationLink, pass);
							insertPassDialog.dismiss();
						}
					}
					else{
						log("other IME" + actionId);
					}
					return false;
				}
			});
			input.setImeActionLabel(getString(R.string.delete_account),EditorInfo.IME_ACTION_DONE);
			builder.setTitle(getString(R.string.delete_account));
			builder.setMessage(getString(R.string.delete_account_text_last_step));
			builder.setNegativeButton(getString(R.string.general_dismiss), null);
			builder.setPositiveButton(getString(R.string.delete_account),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					});
		}
		else{
			log("changeMail action");
			input.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						String pass = input.getText().toString().trim();
						if(pass.equals("")||pass.isEmpty()){
							log("input is empty");
							input.setError(getString(R.string.invalid_string));
							input.requestFocus();
						}
						else {
							log("action DONE ime - change mail");
							aC.confirmChangeMail(confirmationLink, pass);
							insertPassDialog.dismiss();
						}
					}
					else{
						log("other IME" + actionId);
					}
					return false;
				}
			});
			input.setImeActionLabel(getString(R.string.change_pass),EditorInfo.IME_ACTION_DONE);
			builder.setTitle(getString(R.string.change_mail_title_last_step));
			builder.setMessage(getString(R.string.change_mail_text_last_step));
			builder.setNegativeButton(getString(android.R.string.cancel), null);
			builder.setPositiveButton(getString(R.string.change_pass),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					});
		}

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

		builder.setView(layout);
		insertPassDialog = builder.create();
		insertPassDialog.show();
		if(cancelAccount){
			builder.setNegativeButton(getString(R.string.general_dismiss), null);
			insertPassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					log("OK BTTN PASSWORD");
					String pass = input.getText().toString().trim();
					if(pass.equals("")||pass.isEmpty()){
						log("input is empty");
						input.setError(getString(R.string.invalid_string));
						input.requestFocus();
					}
					else {
						log("positive button pressed - cancel account");
						aC.confirmDeleteAccount(confirmationLink, pass);
						insertPassDialog.dismiss();
					}
				}
			});
		}
		else{
			builder.setNegativeButton(getString(android.R.string.cancel), null);
			insertPassDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					log("OK BTTN PASSWORD");
					String pass = input.getText().toString().trim();
					if(pass.equals("")||pass.isEmpty()){
						log("input is empty");
						input.setError(getString(R.string.invalid_string));
						input.requestFocus();
					}
					else {
						log("positive button pressed - change mail");
						aC.confirmChangeMail(confirmationLink, pass);
						insertPassDialog.dismiss();
					}
				}
			});
		}
	}

	public void askConfirmationDeleteAccount(){
		log("askConfirmationDeleteAccount");
		megaApi.multiFactorAuthCheck(megaApi.getMyEmail(), this);

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						aC.deleteAccount();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.delete_account));

		builder.setMessage(getResources().getString(R.string.delete_account_text));

		builder.setPositiveButton(R.string.delete_account, dialogClickListener);
		builder.setNegativeButton(R.string.general_dismiss, dialogClickListener);
		builder.show();
	}

	void verify2FA(int type) {
		if (type == Constants.CANCEL_ACCOUNT_2FA) {
			megaApi.multiFactorAuthCancelAccount(pin, this);
		}
		else if (type == Constants.CHANGE_MAIL_2FA){
			megaApi.multiFactorAuthChangeEmail(newMail, pin, this);
		}
		else if (type ==  Constants.DISABLE_2FA) {
			megaApi.multiFactorAuthDisable(pin, this);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
			case R.id.pin_first_verify:{
				if (hasFocus) {
					firstPin.setText("");
				}
				break;
			}
			case R.id.pin_second_verify:{
				if (hasFocus) {
					secondPin.setText("");
				}
				break;
			}
			case R.id.pin_third_verify:{
				if (hasFocus) {
					thirdPin.setText("");
				}
				break;
			}
			case R.id.pin_fouth_verify:{
				if (hasFocus) {
					fourthPin.setText("");
				}
				break;
			}
			case R.id.pin_fifth_verify:{
				if (hasFocus) {
					fifthPin.setText("");
				}
				break;
			}
			case R.id.pin_sixth_verify:{
				if (hasFocus) {
					sixthPin.setText("");
				}
				break;
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()){
			case R.id.pin_first_verify:
			case R.id.pin_second_verify:
			case R.id.pin_third_verify:
			case R.id.pin_fouth_verify:
			case R.id.pin_fifth_verify:
			case R.id.pin_sixth_verify: {
				pinLongClick = true;
				v.requestFocus();
			}
		}
		return false;
	}

	void pasteClipboard() {
		log("pasteClipboard");
		pinLongClick = false;
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clipData = clipboard.getPrimaryClip();
		if (clipData != null) {
			String code = clipData.getItemAt(0).getText().toString();
			log("code: "+code);
			if (code != null && code.length() == 6) {
				boolean areDigits = true;
				for (int i=0; i<6; i++) {
					if (!Character.isDigit(code.charAt(i))) {
						areDigits = false;
						break;
					}
				}
				if (areDigits) {
					firstPin.setText(""+code.charAt(0));
					secondPin.setText(""+code.charAt(1));
					thirdPin.setText(""+code.charAt(2));
					fourthPin.setText(""+code.charAt(3));
					fifthPin.setText(""+code.charAt(4));
					sixthPin.setText(""+code.charAt(5));
				}
				else {
					firstPin.setText("");
					secondPin.setText("");
					thirdPin.setText("");
					fourthPin.setText("");
					fifthPin.setText("");
					sixthPin.setText("");
				}
			}
		}
	}

	public void showVerifyPin2FA(final int type){
		verifyPin2FADialogType = type;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_verify_2fa, null);
		builder.setView(v);

		TextView titleDialog = (TextView) v.findViewById(R.id.title_dialog_verify);
		if (type == Constants.CANCEL_ACCOUNT_2FA){
			titleDialog.setText(getString(R.string.cancel_account_verification));
		}
		else if (type == Constants.CHANGE_MAIL_2FA){
			titleDialog.setText(getString(R.string.change_mail_verification));
		}
		else if (type == Constants.DISABLE_2FA) {
			titleDialog.setText(getString(R.string.disable_2fa_verification));
		}

		verify2faProgressBar = (ProgressBar) v.findViewById(R.id.progressbar_verify_2fa);

		pinError = (TextView) v.findViewById(R.id.pin_2fa_error_verify);
		pinError.setVisibility(View.GONE);

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		firstPin = (EditTextPIN) v.findViewById(R.id.pin_first_verify);
		firstPin.setOnLongClickListener(this);
		firstPin.setOnFocusChangeListener(this);
		imm.showSoftInput(firstPin, InputMethodManager.SHOW_FORCED);
		firstPin.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if(firstPin.length() != 0){
					secondPin.requestFocus();
					secondPin.setCursorVisible(true);

					if (isFirstTime && !pinLongClick){
						secondPin.setText("");
						thirdPin.setText("");
						fourthPin.setText("");
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else if (pinLongClick) {
						pasteClipboard();
					}
					else  {
						permitVerify(type);
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
				}
			}
		});

		secondPin = (EditTextPIN) v.findViewById(R.id.pin_second_verify);
		secondPin.setOnLongClickListener(this);
		secondPin.setOnFocusChangeListener(this);
		imm.showSoftInput(secondPin, InputMethodManager.SHOW_FORCED);
		secondPin.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (secondPin.length() != 0){
					thirdPin.requestFocus();
					thirdPin.setCursorVisible(true);

					if (isFirstTime && !pinLongClick) {
						thirdPin.setText("");
						fourthPin.setText("");
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else if (pinLongClick) {
						pasteClipboard();
					}
					else  {
						permitVerify(type);
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
				}
			}
		});

		thirdPin = (EditTextPIN) v.findViewById(R.id.pin_third_verify);
		thirdPin.setOnLongClickListener(this);
		thirdPin.setOnFocusChangeListener(this);
		imm.showSoftInput(thirdPin, InputMethodManager.SHOW_FORCED);
		thirdPin.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (thirdPin.length()!= 0){
					fourthPin.requestFocus();
					fourthPin.setCursorVisible(true);

					if (isFirstTime && !pinLongClick) {
						fourthPin.setText("");
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else if (pinLongClick) {
						pasteClipboard();
					}
					else  {
						permitVerify(type);
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
				}
			}
		});

		fourthPin = (EditTextPIN) v.findViewById(R.id.pin_fouth_verify);
		fourthPin.setOnLongClickListener(this);
		fourthPin.setOnFocusChangeListener(this);
		imm.showSoftInput(fourthPin, InputMethodManager.SHOW_FORCED);
		fourthPin.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (fourthPin.length()!=0){
					fifthPin.requestFocus();
					fifthPin.setCursorVisible(true);

					if (isFirstTime && !pinLongClick) {
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else if (pinLongClick) {
						pasteClipboard();
					}
					else  {
						permitVerify(type);
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
				}
			}
		});

		fifthPin = (EditTextPIN) v.findViewById(R.id.pin_fifth_verify);
		fifthPin.setOnLongClickListener(this);
		fifthPin.setOnFocusChangeListener(this);
		imm.showSoftInput(fifthPin, InputMethodManager.SHOW_FORCED);
		fifthPin.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (fifthPin.length()!=0){
					sixthPin.requestFocus();
					sixthPin.setCursorVisible(true);

					if (isFirstTime && !pinLongClick) {
						sixthPin.setText("");
					}
					else if (pinLongClick) {
						pasteClipboard();
					}
					else  {
						permitVerify(type);
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
				}
			}
		});

		sixthPin = (EditTextPIN) v.findViewById(R.id.pin_sixth_verify);
		sixthPin.setOnLongClickListener(this);
		sixthPin.setOnFocusChangeListener(this);
		imm.showSoftInput(sixthPin, InputMethodManager.SHOW_FORCED);
		sixthPin.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (sixthPin.length()!=0){
					sixthPin.setCursorVisible(true);
					hideKeyboard();

					if (pinLongClick) {
						pasteClipboard();
					}
					else {
						permitVerify(type);
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
				}
			}
		});

		firstPin.setGravity(Gravity.CENTER_HORIZONTAL);
		android.view.ViewGroup.LayoutParams paramsb1 = firstPin.getLayoutParams();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			paramsb1.width = Util.scaleWidthPx(42, outMetrics);
		}
		else {
			paramsb1.width = Util.scaleWidthPx(25, outMetrics);
		}
		firstPin.setLayoutParams(paramsb1);
		LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams)firstPin.getLayoutParams();
		textParams.setMargins(0, 0, Util.scaleWidthPx(8, outMetrics), 0);
		firstPin.setLayoutParams(textParams);

		secondPin.setGravity(Gravity.CENTER_HORIZONTAL);
		android.view.ViewGroup.LayoutParams paramsb2 = secondPin.getLayoutParams();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			paramsb2.width = Util.scaleWidthPx(42, outMetrics);
		}
		else {
			paramsb2.width = Util.scaleWidthPx(25, outMetrics);
		}
		secondPin.setLayoutParams(paramsb2);
		textParams = (LinearLayout.LayoutParams)secondPin.getLayoutParams();
		textParams.setMargins(0, 0, Util.scaleWidthPx(8, outMetrics), 0);
		secondPin.setLayoutParams(textParams);
		secondPin.setEt(firstPin);

		thirdPin.setGravity(Gravity.CENTER_HORIZONTAL);
		android.view.ViewGroup.LayoutParams paramsb3 = thirdPin.getLayoutParams();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			paramsb3.width = Util.scaleWidthPx(42, outMetrics);
		}
		else {
			paramsb3.width = Util.scaleWidthPx(25, outMetrics);
		}
		thirdPin.setLayoutParams(paramsb3);
		textParams = (LinearLayout.LayoutParams)thirdPin.getLayoutParams();
		textParams.setMargins(0, 0, Util.scaleWidthPx(25, outMetrics), 0);
		thirdPin.setLayoutParams(textParams);
		thirdPin.setEt(secondPin);

		fourthPin.setGravity(Gravity.CENTER_HORIZONTAL);
		android.view.ViewGroup.LayoutParams paramsb4 = fourthPin.getLayoutParams();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			paramsb4.width = Util.scaleWidthPx(42, outMetrics);
		}
		else {
			paramsb4.width = Util.scaleWidthPx(25, outMetrics);
		}
		fourthPin.setLayoutParams(paramsb4);
		textParams = (LinearLayout.LayoutParams)fourthPin.getLayoutParams();
		textParams.setMargins(0, 0, Util.scaleWidthPx(8, outMetrics), 0);
		fourthPin.setLayoutParams(textParams);
		fourthPin.setEt(thirdPin);

		fifthPin.setGravity(Gravity.CENTER_HORIZONTAL);
		android.view.ViewGroup.LayoutParams paramsb5 = fifthPin.getLayoutParams();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			paramsb5.width = Util.scaleWidthPx(42, outMetrics);
		}
		else {
			paramsb5.width = Util.scaleWidthPx(25, outMetrics);
		}
		fifthPin.setLayoutParams(paramsb5);
		textParams = (LinearLayout.LayoutParams)fifthPin.getLayoutParams();
		textParams.setMargins(0, 0, Util.scaleWidthPx(8, outMetrics), 0);
		fifthPin.setLayoutParams(textParams);
		fifthPin.setEt(fourthPin);

		sixthPin.setGravity(Gravity.CENTER_HORIZONTAL);
		android.view.ViewGroup.LayoutParams paramsb6 = sixthPin.getLayoutParams();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			paramsb6.width = Util.scaleWidthPx(42, outMetrics);
		}
		else {
			paramsb6.width = Util.scaleWidthPx(25, outMetrics);
		}
		sixthPin.setLayoutParams(paramsb6);
		textParams = (LinearLayout.LayoutParams)sixthPin.getLayoutParams();
		textParams.setMargins(0, 0, 0, 0);
		sixthPin.setLayoutParams(textParams);
		sixthPin.setEt(fifthPin);

		verify2FADialog = builder.create();
		verify2FADialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				verify2FADialogIsShown = false;
			}
		});
		verify2FADialog.show();
		verify2FADialogIsShown = true;
	}

	void hideKeyboard(){

		View v = getCurrentFocus();
		if (v != null){
			if (imm == null) {
				imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			}
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

	void verifyQuitError(){
		isErrorShown = false;
		pinError.setVisibility(View.GONE);
		firstPin.setTextColor(ContextCompat.getColor(this, R.color.name_my_account));
		secondPin.setTextColor(ContextCompat.getColor(this, R.color.name_my_account));
		thirdPin.setTextColor(ContextCompat.getColor(this, R.color.name_my_account));
		fourthPin.setTextColor(ContextCompat.getColor(this, R.color.name_my_account));
		fifthPin.setTextColor(ContextCompat.getColor(this, R.color.name_my_account));
		sixthPin.setTextColor(ContextCompat.getColor(this, R.color.name_my_account));
	}

	void verifyShowError(){
		log("Pin not correct verifyShowError");
		isFirstTime = false;
		isErrorShown = true;
		pinError.setVisibility(View.VISIBLE);
		firstPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		secondPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		thirdPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		fourthPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		fifthPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		sixthPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
	}

	void permitVerify(int type){
		log("permitVerify");
		if (firstPin.length() == 1 && secondPin.length() == 1 && thirdPin.length() == 1 && fourthPin.length() == 1 && fifthPin.length() == 1 && sixthPin.length() == 1){
			hideKeyboard();
			if (sb.length()>0) {
				sb.delete(0, sb.length());
			}
			sb.append(firstPin.getText());
			sb.append(secondPin.getText());
			sb.append(thirdPin.getText());
			sb.append(fourthPin.getText());
			sb.append(fifthPin.getText());
			sb.append(sixthPin.getText());
			pin = sb.toString();
			log("PIN: "+pin);
			if (!isErrorShown && pin != null) {
				verify2faProgressBar.setVisibility(View.VISIBLE);
				verify2FA(type);
			}
		}
	}

	public void showImportLinkDialog(){
		log("showImportLinkDialog");
		LinearLayout layout = new LinearLayout(this);
	    layout.setOrientation(LinearLayout.VERTICAL);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleWidthPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

	    final EditText input = new EditText(this);
//		input.setId(EDIT_TEXT_ID);
		input.setSingleLine(false);

		input.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.getBackground().mutate().clearColorFilter();
		input.getBackground().mutate().setColorFilter(ContextCompat.getColor(this, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
		layout.addView(input, params);
		input.setImeActionLabel(getString(R.string.context_open_link_title),EditorInfo.IME_ACTION_DONE);

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.setMargins(Util.scaleWidthPx(20, outMetrics), 0, Util.scaleWidthPx(17, outMetrics), 0);

		final RelativeLayout error_layout = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout, params1);

		final ImageView error_icon = new ImageView(ManagerActivityLollipop.this);
		error_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout.addView(error_icon);
		RelativeLayout.LayoutParams params_icon = (RelativeLayout.LayoutParams) error_icon.getLayoutParams();

//		params_icon.width = Util.scaleWidthPx(24, outMetrics);
//		params_icon.width = 80;
//		params_icon.height = Util.scaleHeightPx(24, outMetrics);
//		params_icon.height = 80;
		params_icon.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon.setLayoutParams(params_icon);

		error_icon.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		final TextView textError = new TextView(ManagerActivityLollipop.this);
		error_layout.addView(textError);
		RelativeLayout.LayoutParams params_text_error = (RelativeLayout.LayoutParams) textError.getLayoutParams();
		params_text_error.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error.width = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error.setMargins(Util.scaleWidthPx(3, outMetrics), 0,0,0);
		textError.setLayoutParams(params_text_error);

		textError.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		error_layout.setVisibility(View.GONE);

		input.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if(error_layout.getVisibility() == View.VISIBLE){
					error_layout.setVisibility(View.GONE);
					input.getBackground().mutate().clearColorFilter();
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.context_open_link_title));
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
						nC.importLink(value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		builder.setView(layout);
		openLinkDialog = builder.create();
		openLinkDialog.show();

		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {

					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError.setText(getString(R.string.invalid_string));
						error_layout.setVisibility(View.VISIBLE);
						input.requestFocus();
						return true;
					}
					nC.importLink(value);
					try{
						openLinkDialog.dismiss();
					}
					catch(Exception e){}
					return true;
				}
				try{
					openLinkDialog.dismiss();
				}
				catch(Exception e){}
				return false;
			}
		});

		openLinkDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new   View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String value = input.getText().toString().trim();
				if (value.length() == 0) {
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
					textError.setText(getString(R.string.invalid_string));
					error_layout.setVisibility(View.VISIBLE);
					input.requestFocus();
					return;
				}

				try{
					openLinkDialog.dismiss();
				}
				catch(Exception e){}
				nC.importLink(value);
			}
		});
	}

	public void takePicture(){
		log("takePicture");
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.temporalPicDIR;
        File newFolder = new File(path);
        newFolder.mkdirs();

        String file = path + "/picture.jpg";
        File newFile = new File(file);
        try {
        	newFile.createNewFile();
        } catch (IOException e) {}

		Uri outputFileUri;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			outputFileUri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", newFile);
		}
		else{
			outputFileUri = Uri.fromFile(newFile);
		}

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent, Constants.TAKE_PHOTO_CODE);
	}

	public void checkPermissions(){
		log("checkPermissionsCall");

		fromTakePicture = Constants.TAKE_PROFILE_PICTURE;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						Constants.REQUEST_WRITE_STORAGE);
			}

			boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
			if (!hasCameraPermission) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.CAMERA},
						Constants.REQUEST_CAMERA);
			}

			if (hasStoragePermission && hasCameraPermission){
				this.takeProfilePicture();
			}
		}
		else{
			this.takeProfilePicture();
		}
	}

	public void takeProfilePicture(){
		log("takeProfilePicture");
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.profilePicDIR;
		File newFolder = new File(path);
		newFolder.mkdirs();

		String file = path + "/picture.jpg";
		File newFile = new File(file);
		try {
			newFile.createNewFile();
		} catch (IOException e) {}

		Uri outputFileUri;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			outputFileUri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", newFile);
		}
		else{
			outputFileUri = Uri.fromFile(newFile);
		}

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		startActivityForResult(cameraIntent, Constants.TAKE_PICTURE_PROFILE_CODE);
	}

	public void showCancelMessage(){
		log("showCancelMessage");
		AlertDialog cancelDialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getString(R.string.title_cancel_subscriptions));

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
					Snackbar.make(fragmentContainer, getString(R.string.reason_cancel_subscriptions), Snackbar.LENGTH_LONG).show();
				}
				else{
					showCancelConfirmation(feedback);
				}
			}
		});

		builder.setNegativeButton(getString(R.string.general_dismiss), new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		cancelDialog = builder.create();
		cancelDialog.show();
//		Util.brandAlertDialog(cancelDialog);
	}

	public void showPresenceStatusDialog(){
		log("showPresenceStatusDialog");

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		final CharSequence[] items = {getString(R.string.online_status), getString(R.string.away_status), getString(R.string.busy_status), getString(R.string.offline_status)};
		int statusToShow = megaChatApi.getOnlineStatus();
		switch(statusToShow){
			case MegaChatApi.STATUS_ONLINE:{
				statusToShow = 0;
				break;
			}
			case MegaChatApi.STATUS_AWAY:{
				statusToShow = 1;
				break;
			}
			case MegaChatApi.STATUS_BUSY:{
				statusToShow = 2;
				break;
			}
			case MegaChatApi.STATUS_OFFLINE:{
				statusToShow = 3;
				break;
			}
		}
		dialogBuilder.setSingleChoiceItems(items, statusToShow, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				presenceStatusDialog.dismiss();
				switch(item) {
					case 0:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_ONLINE, managerActivity);
						break;
					}
					case 1:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_AWAY, managerActivity);
						break;
					}
					case 2:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_BUSY, managerActivity);
						break;
					}
					case 3:{
						megaChatApi.setOnlineStatus(MegaChatApi.STATUS_OFFLINE, managerActivity);
						break;
					}
				}
			}
		});
		dialogBuilder.setTitle(getString(R.string.chat_status_title));
		presenceStatusDialog = dialogBuilder.create();
//		presenceStatusDialog.se
		presenceStatusDialog.show();
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

	public void showNewFolderDialog(){
		log("showNewFolderDialogKitLollipop");

		LinearLayout layout = new LinearLayout(this);
	    layout.setOrientation(LinearLayout.VERTICAL);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleWidthPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

	    final EditText input = new EditText(this);
	    layout.addView(input, params);

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.setMargins(Util.scaleWidthPx(20, outMetrics), 0, Util.scaleWidthPx(17, outMetrics), 0);

		final RelativeLayout error_layout = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout, params1);

		final ImageView error_icon = new ImageView(ManagerActivityLollipop.this);
		error_icon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout.addView(error_icon);
		RelativeLayout.LayoutParams params_icon = (RelativeLayout.LayoutParams) error_icon.getLayoutParams();


		params_icon.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon.setLayoutParams(params_icon);

		error_icon.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		final TextView textError = new TextView(ManagerActivityLollipop.this);
		error_layout.addView(textError);
		RelativeLayout.LayoutParams params_text_error = (RelativeLayout.LayoutParams) textError.getLayoutParams();
		params_text_error.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error.setMargins(Util.scaleWidthPx(3, outMetrics), 0,0,0);
		textError.setLayoutParams(params_text_error);

		textError.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

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
				if(error_layout.getVisibility() == View.VISIBLE){
					error_layout.setVisibility(View.GONE);
					input.getBackground().mutate().clearColorFilter();
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});

//		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
		input.setHint(getString(R.string.context_new_folder_name));
//		input.setSelectAllOnFocus(true);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError.setText(getString(R.string.invalid_string));
						error_layout.setVisibility(View.VISIBLE);
						input.requestFocus();

					}else{
						boolean result=matches(regex, value);
						if(result){
							input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
							textError.setText(getString(R.string.invalid_characters));
							error_layout.setVisibility(View.VISIBLE);
							input.requestFocus();

						}else{
							createFolder(value);
							newFolderDialog.dismiss();
						}
					}
					return true;
				}
				return false;
			}
		});
		input.setImeActionLabel(getString(R.string.general_create),EditorInfo.IME_ACTION_DONE);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showKeyboardDelayed(v);
				}
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.menu_new_folder));
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
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				input.getBackground().clearColorFilter();
			}
		});
		builder.setView(layout);
		newFolderDialog = builder.create();
		newFolderDialog.show();
		newFolderDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new   View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String value = input.getText().toString().trim();
				if (value.length() == 0) {
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
					textError.setText(getString(R.string.invalid_string));
					error_layout.setVisibility(View.VISIBLE);
					input.requestFocus();

				}else{
					boolean result=matches(regex, value);
					if(result){
						input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError.setText(getString(R.string.invalid_characters));
						error_layout.setVisibility(View.VISIBLE);
						input.requestFocus();

					}else{
						createFolder(value);
						newFolderDialog.dismiss();
					}
				}


			}
		});
	}

	public void showAutoAwayValueDialog(){
		log("showAutoAwayValueDialog");

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleWidthPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		layout.addView(input, params);

//		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
		input.setHint(getString(R.string.hint_minutes));
//		input.setSelectAllOnFocus(true);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String value = v.getText().toString().trim();
					if (value.length() != 0) {
						return true;
					}
					setAutoAwayValue(value, false);
					newFolderDialog.dismiss();
					return true;
				}
				return false;
			}
		});
		input.setImeActionLabel(getString(R.string.general_create),EditorInfo.IME_ACTION_DONE);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showKeyboardDelayed(v);
				}
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.title_dialog_set_autoaway_value));
		builder.setPositiveButton(getString(R.string.button_set),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString().trim();
						if (value.length() == 0) {
							return;
						}
						setAutoAwayValue(value, false);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						setAutoAwayValue("-1", true);
					}
				});
		builder.setView(layout);
		newFolderDialog = builder.create();
		newFolderDialog.show();
	}

	public void setAutoAwayValue(String value, boolean cancelled){
		log("setAutoAwayValue: "+ value);
		if(cancelled){
			if(sttFLol!=null){
				if(sttFLol.isAdded()){
					sttFLol.updatePresenceConfigChat(true, null);
				}
			}
		}
		else{
			int timeout = Integer.parseInt(value);
			if(megaChatApi!=null){
				megaChatApi.setPresenceAutoaway(true, timeout*60);
			}
		}
	}


	private void createFolder(String title) {
		log("createFolder");
		if (!Util.isOnline(this)){
			Snackbar.make(fragmentContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
			return;
		}

		if(isFinishing()){
			return;
		}

		long parentHandle=-1;
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			parentHandle = parentHandleBrowser;
			if(parentHandle==-1){
				parentHandle= megaApi.getRootNode().getHandle();
			}
		}
		else if(drawerItem == DrawerItem.SHARED_ITEMS){
			int index = viewPagerShares.getCurrentItem();
			if (index == 0){
				parentHandle = parentHandleIncoming;
			}
			else{
				parentHandle = parentHandleOutgoing;

			}
		}
		else{
			return;
		}

		if(parentHandle!=-1){
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
				Snackbar.make(fragmentContainer, getString(R.string.context_folder_already_exists), Snackbar.LENGTH_LONG).show();
			}
		}
		else{
			log("Incorrect parentHandle");
		}
	}

	public void showClearRubbishBinDialog(){
		log("showClearRubbishBinDialog");

		if (rubbishBinFLol != null) {
			if (rubbishBinFLol.isVisible()) {
				rubbishBinFLol.notifyDataSetChanged();
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.context_clear_rubbish));
		builder.setMessage(getString(R.string.clear_rubbish_confirmation));
		/*builder.setPositiveButton(getString(R.string.context_delete),new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						nC.cleanRubbishBin();
					}
				});*/
		builder.setPositiveButton(getString(R.string.general_clear),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						nC.cleanRubbishBin();
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		clearRubbishBinDialog = builder.create();
		clearRubbishBinDialog.show();
	}

	public void showConfirmationClearAllVersions(){
		log("showConfirmationClearAllVersions");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.settings_file_management_delete_versions));
		builder.setMessage(getString(R.string.text_confirmation_dialog_delete_versions));

		builder.setPositiveButton(getString(R.string.context_delete),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						nC.clearAllVersions();
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		clearRubbishBinDialog = builder.create();
		clearRubbishBinDialog.show();
	}

//	public void upgradeAccountButton(){
//		log("upgradeAccountButton");
//		drawerItem = DrawerItem.ACCOUNT;
//		if (accountInfo != null){
//			if ((accountInfo.getSubscriptionStatus() == MegaAccountDetails.SUBSCRIPTION_STATUS_NONE) || (accountInfo.getSubscriptionStatus() == MegaAccountDetails.SUBSCRIPTION_STATUS_INVALID)){
//				Time now = new Time();
//				now.setToNow();
//				if (accountType != 0){
//					log("accountType != 0");
//					if (now.toMillis(false) >= (accountInfo.getProExpiration()*1000)){
//						if (Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CREDIT_CARD) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_FORTUMO) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CENTILI)){
//							log("SUBSCRIPTION INACTIVE: CHECKBITSET --> CC || FORT || INFO");
//							showUpAF(null);
//						}
//						else{
//							Snackbar.make(fragmentContainer, getString(R.string.not_upgrade_is_possible), Snackbar.LENGTH_LONG).show();
//
//						}
//					}
//					else{
//						log("CURRENTLY ACTIVE SUBSCRIPTION");
//						Snackbar.make(fragmentContainer, getString(R.string.not_upgrade_is_possible), Snackbar.LENGTH_LONG).show();
//					}
//				}
//				else{
//					log("accountType == 0");
//					if (Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CREDIT_CARD) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_FORTUMO) || Util.checkBitSet(paymentBitSet, MegaApiAndroid.PAYMENT_METHOD_CENTILI)){
//						log("CHECKBITSET --> CC || FORT || INFO");
//						showUpAF(null);
//					}
//					else{
//						Snackbar.make(fragmentContainer, getString(R.string.not_upgrade_is_possible), Snackbar.LENGTH_LONG).show();
//					}
//				}
//			}
//			else{
//				Snackbar.make(fragmentContainer, getString(R.string.not_upgrade_is_possible), Snackbar.LENGTH_LONG).show();
//			}
//		}
//		else{
//			Snackbar.make(fragmentContainer, getString(R.string.not_upgrade_is_possible), Snackbar.LENGTH_LONG).show();
//		}
//	}


	public void showPanelSetPinLock(){
		log("showPanelSetPinLock");

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		final CharSequence[] items = {getString(R.string.four_pin_lock), getString(R.string.six_pin_lock), getString(R.string.AN_pin_lock)};

		dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				setPinDialog.dismiss();
				switch(item) {
					case 0:{
						dbH.setPinLockType(Constants.PIN_4);
						if(sttFLol!=null && sttFLol.isAdded()){
							sttFLol.intentToPinLock();
						}
						break;
					}
					case 1:{
						dbH.setPinLockType(Constants.PIN_6);
						if(sttFLol!=null && sttFLol.isAdded()){
							sttFLol.intentToPinLock();
						}
						break;
					}
					case 2:{
						dbH.setPinLockType(Constants.PIN_ALPHANUMERIC);
						if(sttFLol!=null && sttFLol.isAdded()){
							sttFLol.intentToPinLock();
						}
						break;
					}
				}
			}
		});
		dialogBuilder.setTitle(getString(R.string.pin_lock_type));

		dialogBuilder.setOnKeyListener(new Dialog.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
				log("onKeyListener: "+keyCode);
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					log("Cancel set pin action");
					setPinDialog.dismiss();
					if(sttFLol!=null){
						if(sttFLol.isAdded()){
							sttFLol.cancelSetPinLock();
						}
					}
				}
				return true;
			}
		});

		dialogBuilder.setOnCancelListener(
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						log("setOnCancelListener setPin");
						setPinDialog.dismiss();
						if(sttFLol!=null){
							if(sttFLol.isAdded()){
								sttFLol.cancelSetPinLock();
							}
						}
					}
				}
		);

		setPinDialog = dialogBuilder.create();
		setPinDialog.setCanceledOnTouchOutside(true);
		setPinDialog.show();
	}

	public void chooseAddContactDialog(boolean isMegaContact){
		log("chooseAddContactDialog");

		Intent in = new Intent(this, AddContactActivityLollipop.class);
		if(isMegaContact){

			if(megaApi!=null && megaApi.getRootNode()!=null){
				ArrayList<MegaUser> contacts = megaApi.getContacts();
				if(contacts==null){
					showSnackbar("You have no MEGA contacts. Please, invite friends from the Contacts section");
				}
				else {
					if(contacts.isEmpty()){
						showSnackbar("You have no MEGA contacts. Please, invite friends from the Contacts section");
					}
					else{
						in.putExtra("contactType", Constants.CONTACT_TYPE_MEGA);
						startActivityForResult(in, Constants.REQUEST_CREATE_CHAT);
					}
				}
			}
			else{
				log("Online but not megaApi");
				showSnackbar(getString(R.string.error_server_connection_problem));
			}
		}
		else{
			addContactFromPhone();
		}
	}

	public void addContactFromPhone(){

		Intent in = new Intent(this, AddContactActivityLollipop.class);
		in.putExtra("contactType", Constants.CONTACT_TYPE_DEVICE);
		startActivityForResult(in, Constants.REQUEST_INVITE_CONTACT_FROM_DEVICE);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			boolean hasReadContactsPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
//			if (!hasReadContactsPermission) {
//				log("No read contacts permission");
//				ActivityCompat.requestPermissions((ManagerActivityLollipop)this,
//						new String[]{Manifest.permission.READ_CONTACTS},
//						Constants.REQUEST_READ_CONTACTS);
//				return;
//			}
//		}
//
//		Intent phoneContactIntent = new Intent(this, PhoneContactsActivityLollipop.class);
//		this.startActivity(phoneContactIntent);
	}

	public void showNewContactDialog(){
		log("showNewContactDialog");

		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cFLol != null && cFLol.isAdded()){
			if (drawerItem == DrawerItem.CONTACTS){
				cFLol.setPositionClicked(-1);
				cFLol.notifyDataSetChanged();
			}
		}

		LinearLayout layout = new LinearLayout(this);
	    layout.setOrientation(LinearLayout.VERTICAL);
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	    params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

		final EditText input = new EditText(this);
		layout.addView(input, params);

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.setMargins(Util.scaleWidthPx(20, outMetrics), 0, Util.scaleWidthPx(17, outMetrics), 0);

		final RelativeLayout error_layout_email = new RelativeLayout(ManagerActivityLollipop.this);
		layout.addView(error_layout_email, params1);

		final ImageView error_icon_email = new ImageView(ManagerActivityLollipop.this);
		error_icon_email.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_input_warning));
		error_layout_email.addView(error_icon_email);
		RelativeLayout.LayoutParams params_icon = (RelativeLayout.LayoutParams) error_icon_email.getLayoutParams();


		params_icon.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		error_icon_email.setLayoutParams(params_icon);

		error_icon_email.setColorFilter(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		final TextView textError_email = new TextView(ManagerActivityLollipop.this);
		error_layout_email.addView(textError_email);
		RelativeLayout.LayoutParams params_text_error = (RelativeLayout.LayoutParams) textError_email.getLayoutParams();
		params_text_error.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		params_text_error.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params_text_error.addRule(RelativeLayout.CENTER_VERTICAL);
		params_text_error.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params_text_error.addRule(RelativeLayout.LEFT_OF, error_icon_email.getId());
		params_text_error.setMargins(Util.scaleWidthPx(3, outMetrics), 0,0,0);
		textError_email.setLayoutParams(params_text_error);

		textError_email.setTextColor(ContextCompat.getColor(ManagerActivityLollipop.this, R.color.login_warning));

		error_layout_email.setVisibility(View.GONE);

//		input.setId(EDIT_TEXT_ID);
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
				if(error_layout_email.getVisibility() == View.VISIBLE){
					error_layout_email.setVisibility(View.GONE);
					input.getBackground().mutate().clearColorFilter();
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.accentColor), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});
		input.setSingleLine();
		input.setHint(getString(R.string.context_new_contact_name));
		input.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
//		input.setSelectAllOnFocus(true);
		input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String value = input.getText().toString().trim();
					String emailError = Util.getEmailError(value, managerActivity);
					if (emailError != null) {
//                        input.setError(emailError);
						input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
						textError_email.setText(emailError);
						error_layout_email.setVisibility(View.VISIBLE);
					} else {
						cC.inviteContact(value);
						addContactDialog.dismiss();
					}
				}
				else{
					log("other IME" + actionId);
				}
				return false;
			}
		});
		input.setImeActionLabel(getString(R.string.general_add),EditorInfo.IME_ACTION_DONE);
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showKeyboardDelayed(v);
				}
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.menu_add_contact));
		builder.setPositiveButton(getString(R.string.general_add),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				input.getBackground().clearColorFilter();
			}
		});
		builder.setView(layout);
		addContactDialog = builder.create();
		addContactDialog.show();
		addContactDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String value = input.getText().toString().trim();
				String emailError = Util.getEmailError(value, managerActivity);
				if (emailError != null) {
//					input.setError(emailError);
					input.getBackground().mutate().setColorFilter(ContextCompat.getColor(managerActivity, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
					textError_email.setText(emailError);
					error_layout_email.setVisibility(View.VISIBLE);
				} else {
					cC.inviteContact(value);
					addContactDialog.dismiss();
				}
			}
		});
	}

	public void showConfirmationRemoveContact(final MegaUser c){
		log("showConfirmationRemoveContact");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.removeContact(c);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String title = getResources().getQuantityString(R.plurals.title_confirmation_remove_contact, 1);
		builder.setTitle(title);
		String message= getResources().getQuantityString(R.plurals.confirmation_remove_contact, 1);
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveContacts(final ArrayList<MegaUser> c){
		log("showConfirmationRemoveContactssssss");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.removeMultipleContacts(c);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String title = getResources().getQuantityString(R.plurals.title_confirmation_remove_contact, c.size());
		builder.setTitle(title);
		String message= getResources().getQuantityString(R.plurals.confirmation_remove_contact, c.size());
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveContactRequest(final MegaContactRequest r){
		log("showConfirmationRemoveContactRequest");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.removeInvitationContact(r);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message= getResources().getString(R.string.confirmation_delete_contact_request,r.getTargetEmail());
		builder.setMessage(message).setPositiveButton(R.string.context_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationRemoveContactRequests(final List<MegaContactRequest> r){
		log("showConfirmationRemoveContactRequests");
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						cC.deleteMultipleSentRequestContacts(r);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		String message="";
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if(r.size()==1){
			message= getResources().getString(R.string.confirmation_delete_contact_request,r.get(0).getTargetEmail());
		}else{
			message= getResources().getString(R.string.confirmation_remove_multiple_contact_request,r.size());
		}

		builder.setMessage(message).setPositiveButton(R.string.context_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationLeaveMultipleShares (final ArrayList<Long> handleList){
		log("showConfirmationleaveMultipleShares");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	nC.leaveMultipleIncomingShares(handleList);
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getResources().getString(R.string.alert_leave_share));
		String message= getResources().getString(R.string.confirmation_leave_share_folder);
		builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
	    	.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationRemoveAllSharingContacts (final ArrayList<MegaShare> shareList, final MegaNode n){
		log("showConfirmationRemoveAllSharingContacts");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						nC.removeAllSharingContacts(shareList, n);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getResources().getString(R.string.alert_leave_share));
		int size = shareList.size();
		String message = getResources().getQuantityString(R.plurals.confirmation_remove_outgoing_shares, size, size);
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationRemovePublicLink (final MegaNode n){
		log("showConfirmationRemovePublicLink");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						nC.removeLink(n);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getResources().getString(R.string.alert_leave_share));
		String message= getResources().getString(R.string.context_remove_link_warning_text);
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

		refreshAfterMovingToRubbish();
	}

	public void showConfirmationLeaveIncomingShare (final MegaNode n){
		log("showConfirmationLeaveIncomingShare");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE: {
					nC.leaveIncomingShare(n);
					break;
				}
		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getResources().getString(R.string.alert_leave_share));
		String message= getResources().getString(R.string.confirmation_leave_share_folder);
		builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
	    	.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void showConfirmationLeaveChat (final MegaChatRoom c){
		log("showConfirmationLeaveChat");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						ChatController chatC = new ChatController(managerActivity);
						chatC.leaveChat(c);
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.title_confirmation_leave_group_chat));
		String message= getResources().getString(R.string.confirmation_leave_group_chat);
		builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationLeaveChat (final MegaChatListItem c){
		log("showConfirmationLeaveChat");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						ChatController chatC = new ChatController(managerActivity);
						chatC.leaveChat(c.getChatId());
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.title_confirmation_leave_group_chat));
		String message= getResources().getString(R.string.confirmation_leave_group_chat);
		builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}
	public void showConfirmationLeaveChats (final  ArrayList<MegaChatListItem> cs){
		log("showConfirmationLeaveChats");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						ChatController chatC = new ChatController(managerActivity);

						for(int i=0;i<cs.size();i++){
							MegaChatListItem chat = cs.get(i);
							if(chat!=null){
								chatC.leaveChat(chat.getChatId());
							}
						}

						break;
					}
					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						if(rChatFL!=null){
							if(rChatFL.isAdded()){
								rChatFL.clearSelections();
								rChatFL.hideMultipleSelect();
							}
						}
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.title_confirmation_leave_group_chat));
		String message= getResources().getString(R.string.confirmation_leave_group_chat);
		builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationClearChat(final MegaChatListItem c){
		log("showConfirmationClearChat");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						log("Clear chat!");
//						megaChatApi.truncateChat(chatHandle, MegaChatHandle.MEGACHAT_INVALID_HANDLE);
						log("Clear history selected!");
						ChatController chatC = new ChatController(managerActivity);
						chatC.clearHistory(c.getChatId());
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
		String message= getResources().getString(R.string.confirmation_clear_group_chat);
		builder.setTitle(R.string.title_confirmation_clear_group_chat);
		builder.setMessage(message).setPositiveButton(R.string.general_clear, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationResetPasswordFromMyAccount (){
		log("showConfirmationResetPasswordFromMyAccount: ");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
						maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
						if(maFLol!=null && maFLol.isAdded()){
							maFLol.resetPass();
						}
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message= getResources().getString(R.string.email_verification_text_change_pass);
		builder.setMessage(message).setPositiveButton(R.string.cam_sync_ok, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationResetPassword (final String link){
		log("showConfirmationResetPassword: "+link);

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						Intent intent = new Intent(managerActivity, ChangePasswordActivityLollipop.class);
						intent.setAction(Constants.ACTION_RESET_PASS_FROM_LINK);
						intent.setData(Uri.parse(link));
						String key = megaApi.exportMasterKey();
						intent.putExtra("MK", key);
						startActivity(intent);
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.title_dialog_insert_MK));
		String message= getResources().getString(R.string.text_reset_pass_logged_in);
		builder.setMessage(message).setPositiveButton(R.string.pin_lock_enter, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void cameraUploadsClicked(){
		log("cameraUplaodsClicked");
		drawerItem = DrawerItem.CAMERA_UPLOADS;
		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem cameraUploadsItem = nVMenu.findItem(R.id.navigation_item_camera_uploads);
			drawerMenuItem = cameraUploadsItem;
			resetNavigationViewMenu(nVMenu);
			cameraUploadsItem.setChecked(true);
			cameraUploadsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_red));
		}
		selectDrawerItemLollipop(drawerItem);
	}

	public void secondaryMediaUploadsClicked(){
		log("secondaryMediaUploadsClicked");
		drawerItem = DrawerItem.MEDIA_UPLOADS;
//		if (nV != null){
//			Menu nVMenu = nV.getMenu();
//			MenuItem cameraUploadsItem = nVMenu.findItem(R.id.navigation_item_cloud_drive);
//			drawerMenuItem = cameraUploadsItem;
//			resetNavigationViewMenu(nVMenu);
//			cameraUploadsItem.setChecked(true);
//			cameraUploadsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_red));
//		}
		selectDrawerItemLollipop(drawerItem);
	}

	public void setInitialCloudDrive (){
		drawerItem = DrawerItem.CLOUD_DRIVE;

		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem cloudDrive = nVMenu.findItem(R.id.navigation_item_cloud_drive);
			resetNavigationViewMenu(nVMenu);
			cloudDrive.setChecked(true);
			cloudDrive.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_red));
		}
		firstTime = true;
		selectDrawerItemLollipop(drawerItem);
		drawerLayout.openDrawer(Gravity.LEFT);
	}

	public void refreshCameraUpload(){
		drawerItem = DrawerItem.CAMERA_UPLOADS;
		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem cameraUploads = nVMenu.findItem(R.id.navigation_item_camera_uploads);
			resetNavigationViewMenu(nVMenu);
			cameraUploads.setChecked(true);
			cameraUploads.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_red));
		}

		Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("cuFLol");
		FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
		fragTransaction.detach(currentFragment);
		fragTransaction.commitNowAllowingStateLoss();

		fragTransaction = getSupportFragmentManager().beginTransaction();
		fragTransaction.attach(currentFragment);
		fragTransaction.commitNowAllowingStateLoss();
	}

	public void showNodeOptionsPanel(MegaNode node){
		log("showNodeOptionsPanel");
		this.selectedNode=node;
		if(node!=null){
			this.selectedNode = node;
			NodeOptionsBottomSheetDialogFragment bottomSheetDialogFragment = new NodeOptionsBottomSheetDialogFragment();
			bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		}
	}

	public void showOptionsPanel(MegaOffline sNode){
		log("showNodeOptionsPanel-Offline");
		if(sNode!=null){
			this.selectedOfflineNode = sNode;
			OfflineOptionsBottomSheetDialogFragment bottomSheetDialogFragment = new OfflineOptionsBottomSheetDialogFragment();
			bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		}
	}

	public void showContactOptionsPanel(MegaContactAdapter user){
		log("showContactOptionsPanel");

		if(!Util.isOnline(this)){
			showSnackbar(getString(R.string.error_server_connection_problem));
			return;
		}

		if(user!=null){
			this.selectedUser = user;
			ContactsBottomSheetDialogFragment bottomSheetDialogFragment = new ContactsBottomSheetDialogFragment();
			bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		}
	}

	public void showSentRequestOptionsPanel(MegaContactRequest request){
		log("showSentRequestOptionsPanel");
		if(request!=null){
			this.selectedRequest = request;
			SentRequestBottomSheetDialogFragment bottomSheetDialogFragment = new SentRequestBottomSheetDialogFragment();
			bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		}
	}

	public void showReceivedRequestOptionsPanel(MegaContactRequest request){
		log("showReceivedRequestOptionsPanel");
		if(request!=null){
			this.selectedRequest = request;
			ReceivedRequestBottomSheetDialogFragment bottomSheetDialogFragment = new ReceivedRequestBottomSheetDialogFragment();
			bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		}
	}

	public void showMyAccountOptionsPanel() {
		log("showMyAccountOptionsPanel");
		MyAccountBottomSheetDialogFragment bottomSheetDialogFragment = new MyAccountBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public void showUploadPanel(){
		log("showUploadPanel");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions((ManagerActivityLollipop)this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
						Constants.REQUEST_READ_WRITE_STORAGE);
			}else{
				onGetReadWritePermission();
			}
		}else{
			onGetReadWritePermission();
		}
	}

	private void onGetReadWritePermission(){
		UploadBottomSheetDialogFragment bottomSheetDialogFragment = new UploadBottomSheetDialogFragment();
		bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
	}

	public int getHeightToPanel(BottomSheetDialogFragment dialog){
		
		if(dialog instanceof NodeOptionsBottomSheetDialogFragment){
			if(fragmentContainer != null && aB != null && tabLayoutCloud != null && tabLayoutCloud.getHeight() != 0){
				final Rect r = new Rect();
				fragmentContainer.getWindowVisibleDisplayFrame(r);
				return (r.height() - aB.getHeight() - tabLayoutCloud.getHeight());
			}
			else if(fragmentContainer != null && aB != null && tabLayoutShares != null && tabLayoutShares.getHeight() != 0){
				final Rect r = new Rect();
				fragmentContainer.getWindowVisibleDisplayFrame(r);
				return (r.height() - aB.getHeight() - tabLayoutShares.getHeight());
			}
			else if(fragmentContainer != null && aB != null && tabLayoutTransfers != null && tabLayoutTransfers.getHeight() != 0){
				final Rect r = new Rect();
				fragmentContainer.getWindowVisibleDisplayFrame(r);
				return (r.height() - aB.getHeight() - tabLayoutTransfers.getHeight());
			}
		}
		else if(dialog instanceof ContactsBottomSheetDialogFragment){
			if(fragmentContainer != null && aB != null && tabLayoutContacts != null && tabLayoutContacts.getHeight() != 0){
				final Rect r = new Rect();
				fragmentContainer.getWindowVisibleDisplayFrame(r);
				return (r.height() - aB.getHeight() - tabLayoutContacts.getHeight());
			}
		}
		return -1;
	}

	private void showOverquotaAlert(boolean prewarning){
		log("showOverquotaAlert: prewarning: "+prewarning);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.overquota_alert_title));

		if(prewarning){
			builder.setMessage(getString(R.string.pre_overquota_alert_text));
		}
		else{
			builder.setMessage(getString(R.string.overquota_alert_text));
			dbH.setCamSyncEnabled(false);
		}

		if(overquotaDialog==null){

			builder.setPositiveButton(getString(R.string.my_account_upgrade_pro), new android.content.DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					//Show UpgradeAccountActivity
					drawerItem = DrawerItem.ACCOUNT;
					accountFragment=Constants.UPGRADE_ACCOUNT_FRAGMENT;
					selectDrawerItemAccount();
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
			overquotaDialog.setCanceledOnTouchOutside(false);
		}

		overquotaDialog.show();
	}

	public void updateAccountDetailsVisibleInfo(){
		log("updateAccountDetailsVisibleInfo");
		if(isFinishing()){
			return;
		}

		usedSpacePB.setProgress(((MegaApplication) getApplication()).getMyAccountInfo().getUsedPerc());

//				String usedSpaceString = getString(R.string.used_space, used, total);
		usedSpaceTV.setText(((MegaApplication) getApplication()).getMyAccountInfo().getUsedFormatted());
		totalSpaceTV.setText(((MegaApplication) getApplication()).getMyAccountInfo().getTotalFormatted());

		usedSpacePB.setProgress(((MegaApplication) getApplication()).getMyAccountInfo().getUsedPerc());

//				String usedSpaceString = getString(R.string.used_space, used, total);
		usedSpaceTV.setText(((MegaApplication) getApplication()).getMyAccountInfo().getUsedFormatted());
		totalSpaceTV.setText(((MegaApplication) getApplication()).getMyAccountInfo().getTotalFormatted());

		if (((MegaApplication) getApplication()).getMyAccountInfo().isInventoryFinished()){
			if (((MegaApplication) getApplication()).getMyAccountInfo().getLevelAccountDetails() < ((MegaApplication) getApplication()).getMyAccountInfo().getLevelInventory()){
				if (maxP != null){
					log("ORIGINAL JSON2:" + maxP.getOriginalJson() + ":::");
					megaApi.submitPurchaseReceipt(maxP.getOriginalJson(), this);
				}
			}
		}

		showStorageAlmostFullDialog();

		if (((MegaApplication) getApplication()).getMyAccountInfo().getUsedPerc() < 90){
			usedSpacePB.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_ok));
//		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_ok)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		        	usedSpaceWarning.setVisibility(View.INVISIBLE);
		}
		else if ((((MegaApplication) getApplication()).getMyAccountInfo().getUsedPerc() >= 90) && (((MegaApplication) getApplication()).getMyAccountInfo().getUsedPerc() <= 95)){
			usedSpacePB.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_warning));
//		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_warning)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		        	usedSpaceWarning.setVisibility(View.VISIBLE);
		}
		else{
			if (((MegaApplication) getApplication()).getMyAccountInfo().getUsedPerc() > 100){
				((MegaApplication) getApplication()).getMyAccountInfo().setUsedPerc(100);
			}
			usedSpacePB.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.custom_progress_bar_horizontal_exceed));
//		        	wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.used_space_exceed)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		        	usedSpaceWarning.setVisibility(View.VISIBLE);
		}
	}

	public void selectSortByContacts(int _orderContacts){
		log("selectSortByContacts");

		this.setOrderContacts(_orderContacts);
		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cFLol != null && cFLol.isAdded()){
			cFLol.sortBy();
			cFLol.updateOrder();
		}
	}

	public void selectSortByOffline(int _orderOthers){
		log("selectSortByOffline");

		this.orderOthers = _orderOthers;
		this.setOrderOthers(orderOthers);
		if (oFLol != null && oFLol.isAdded()){
			oFLol.setOrder(orderOthers);
			if (orderOthers == MegaApiJava.ORDER_DEFAULT_ASC){
				oFLol.sortByNameAscending();
			}
			else{
				oFLol.sortByNameDescending();
			}
		}
	}

	public void refreshCloudOrder(int newOrderCloud){
		log("refreshCloudOrder: "+newOrderCloud);
		this.setOrderCloud(newOrderCloud);
		if(cloudPageAdapter!=null){
			cloudPageAdapter.notifyDataSetChanged();
		}
		if(sharesPageAdapter!=null){
			sharesPageAdapter.notifyDataSetChanged();
		}
		if (iFLol != null && iFLol.isAdded()){
			MegaNode inboxNode = megaApi.getInboxNode();
			if(inboxNode!=null){
				ArrayList<MegaNode> nodes = megaApi.getChildren(inboxNode, orderCloud);
				iFLol.setNodes(nodes);
				iFLol.getRecyclerView().invalidate();
			}
		}
	}

	public void refreshOthersOrder(int newOrderOthers){
		log("refreshOthersOrder: "+newOrderOthers);
		this.setOrderOthers(newOrderOthers);

		if(sharesPageAdapter!=null){
			sharesPageAdapter.notifyDataSetChanged();
		}
	}

	public void selectSortUploads(int orderCamera){
		log("selectSortUploads");

		this.orderCamera = orderCamera;

		cuFL = (CameraUploadFragmentLollipop) getSupportFragmentManager().findFragmentByTag("cuFLol");
		if (cuFL != null && cuFL.isAdded()){
			ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(cuFL.getPhotoSyncHandle()), MegaApiJava.ORDER_MODIFICATION_DESC);
			cuFL.setNodes(nodes);
			cuFL.getRecyclerView().invalidate();
		}

		muFLol = (CameraUploadFragmentLollipop) getSupportFragmentManager().findFragmentByTag("muFLol");
		if (muFLol != null && muFLol.isAdded()){
			ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(muFLol.getPhotoSyncHandle()), MegaApiJava.ORDER_MODIFICATION_DESC);
			muFLol.setNodes(nodes);
			muFLol.getRecyclerView().invalidate();
		}
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

	public void setFirstNavigationLevel(boolean firstNavigationLevel){
		log("setFirstNavigationLevel: set value to "+firstNavigationLevel);
		this.firstNavigationLevel = firstNavigationLevel;
	}

	public boolean isFirstNavigationLevel() {
		return firstNavigationLevel;
	}

	public void setParentHandleBrowser(long parentHandleBrowser){
		log("setParentHandleBrowser: set value to:"+parentHandleBrowser);

		this.parentHandleBrowser = parentHandleBrowser;
	}

	public void setParentHandleRubbish(long parentHandleRubbish){
		log("setParentHandleRubbish");
		this.parentHandleRubbish = parentHandleRubbish;
	}

	public void setParentHandleSearch(long parentHandleSearch){
		log("setParentHandleSearch");
		this.parentHandleSearch = parentHandleSearch;
	}

	public void setParentHandleIncoming(long parentHandleIncoming){
		log("setParentHandleIncoming: " + parentHandleIncoming);
		this.parentHandleIncoming = parentHandleIncoming;
	}

	public void setParentHandleInbox(long parentHandleInbox){
		log("setParentHandleInbox: " + parentHandleInbox);
		this.parentHandleInbox = parentHandleInbox;
	}

	public void setParentHandleOutgoing(long parentHandleOutgoing){
		log("setParentHandleOutgoing: " + parentHandleOutgoing);
		this.parentHandleOutgoing = parentHandleOutgoing;
	}

	@Override
	protected void onNewIntent(Intent intent){
    	log("onNewIntent");

    	if(intent != null) {
			if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
				searchQuery = intent.getStringExtra(SearchManager.QUERY);
				parentHandleSearch = -1;
				setToolbarTitle();
				isSearching = true;

				if (searchMenuItem != null) {
					MenuItemCompat.collapseActionView(searchMenuItem);
				}
				return;
			}
//			When the user clicks on settings option in QR section, set drawerItem to SETTINGS and scroll to auto-accept setting
			else if (intent.getBooleanExtra("fromQR", false)){
				Bundle bundle = intent.getExtras();
				if (bundle.getSerializable("drawerItemQR") != null){
					if (DrawerItem.SETTINGS.equals(bundle.getSerializable("drawerItemQR"))){
						log ("From QR Settings");
						moveToSettingsSectionQR();
					}
				}
				return;
			}

		}
     	super.onNewIntent(intent);
    	setIntent(intent);
    	return;
	}

	public void navigateToUpgradeAccount(){
		log("navigateToUpgradeAccount");
		drawerItem = DrawerItem.ACCOUNT;
		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
			resetNavigationViewMenu(nVMenu);
			hidden.setChecked(true);
		}

		getProLayout.setVisibility(View.GONE);
		drawerItem = DrawerItem.ACCOUNT;
		accountFragment = Constants.UPGRADE_ACCOUNT_FRAGMENT;
		displayedAccountType = -1;
		selectDrawerItemLollipop(drawerItem);
	}

	public void navigateToAchievements(){
		log("navigateToAchievements");
		drawerItem = DrawerItem.ACCOUNT;
		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
			resetNavigationViewMenu(nVMenu);
			hidden.setChecked(true);
		}

		getProLayout.setVisibility(View.GONE);
		drawerItem = DrawerItem.ACCOUNT;
		accountFragment = Constants.MY_ACCOUNT_FRAGMENT;
		displayedAccountType = -1;
		selectDrawerItemLollipop(drawerItem);

		Intent intent = new Intent(this, AchievementsActivity.class);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		log("onClick");

		((MegaApplication) getApplication()).sendSignalPresenceActivity();

		switch(v.getId()){
//			case R.id.custom_search:{
//				if (searchMenuItem != null) {
//					MenuItemCompat.expandActionView(searchMenuItem);
//				}
//				else{
//					log("searchMenuItem == null");
//				}
//				break;
//			}
			case R.id.btnLeft_cancel:{
				getProLayout.setVisibility(View.GONE);
				break;
			}
			case R.id.btnRight_upgrade:{
				//Add navigation to Upgrade Account
				log("click on Upgrade in pro panel!");
				navigateToUpgradeAccount();
				break;
			}

			case R.id.navigation_drawer_account_view:{
//				Snackbar.make(fragmentContainer, "MyAccount", Snackbar.LENGTH_LONG).show();
				if (Util.isOnline(this)){
					drawerItem = DrawerItem.ACCOUNT;
					accountFragment=Constants.MY_ACCOUNT_FRAGMENT;
					if (nV != null){
						Menu nVMenu = nV.getMenu();
						MenuItem hidden = nVMenu.findItem(R.id.navigation_item_hidden);
						resetNavigationViewMenu(nVMenu);
						hidden.setChecked(true);
					}
					selectDrawerItemLollipop(drawerItem);
				}

				break;
			}
			case R.id.dialog_remember_pwd_checkbox: {
				if (showRememberPaswordCheckBox.isChecked()){
					log("passwordReminderDialogBlocked checked");
					passwordReminderDialogBlocked = true;
				}
				else {
					log("showRememberPaswordCheckBox not checked");
					passwordReminderDialogBlocked = false;
				}
				break;
			}
			case R.id.dialog_remember_pwd_test_button: {
				passwordReminderDialogSkipped = false;
				rememberPasswordDialog.dismiss();
				Intent intent = new Intent(this, TestPasswordActivity.class);
				intent.putExtra("rememberPasswordLogout", rememberPasswordLogout);
				startActivity(intent);
				break;
			}
			case R.id.dialog_remember_pwd_backup_recoverykey_button: {
				passwordReminderDialogSkipped = false;
				if (rememberPasswordLogout){
					showBottomSheetRecoveryKey();
				}
				else{
					rememberPasswordDialog.dismiss();
					exportRecoveryKey();
				}
				break;
			}
			case R.id.dialog_remember_pwd_dismiss_button: {
				passwordReminderDialogSkipped = true;
				rememberPasswordDialog.dismiss();
				if (rememberPasswordLogout){
					AccountController ac = new AccountController(this);
					ac.logout(this, megaApi);
				}
				break;
			}
			case R.id.enable_2fa_button: {
				if (enable2FADialog != null) {
					enable2FADialog.dismiss();
				}
				isEnable2FADialogShown = false;
				Intent intent = new Intent(this, TwoFactorAuthenticationActivity.class);
				intent.putExtra("newAccount", true);
				startActivity(intent);
				break;
			}
			case R.id.skip_enable_2fa_button: {
				isEnable2FADialogShown = false;
				if (enable2FADialog != null) {
					enable2FADialog.dismiss();
				}
				break;
			}
//			case R.id.top_control_bar:{
//				if (nDALol != null){
//					nDALol.setPositionClicked(-1);
//				}
//				drawerItem = DrawerItem.ACCOUNT;
//				titleAB = drawerItem.getTitle(this);
//
//				selectDrawerItemLollipop(drawerItem);
//
//				break;
//			}
//			case R.id.bottom_control_bar:{
//				if (nDALol != null){
//					nDALol.setPositionClicked(-1);
//				}
//				drawerItem = DrawerItem.ACCOUNT;
//				titleAB = drawerItem.getTitle(this);
//
//				selectDrawerItemLollipop(drawerItem);
//
//				break;
//			}
		}
	}

	void showBottomSheetRecoveryKey(){
		RecoveryKeyBottomSheetDialogFragment recoveryKeyBottomSheetDialogFragment = new RecoveryKeyBottomSheetDialogFragment();
		recoveryKeyBottomSheetDialogFragment.show(getSupportFragmentManager(), recoveryKeyBottomSheetDialogFragment.getTag());
	}

	void exportRecoveryKey (){
		AccountController aC = new AccountController(this);
		aC.exportMK(null, false);
	}

	public void showConfirmationRemoveMK(){
		log("showConfirmationRemoveMK");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						Constants.REQUEST_WRITE_STORAGE);
			}
		}

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						AccountController aC = new AccountController(managerActivity);
						aC.removeMK();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.remove_key_confirmation).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationRemoveFromOffline(){
		log("showConfirmationRemoveFromOffline");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
						Constants.REQUEST_WRITE_STORAGE);
			}
		}

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						String pathNavigation = getPathNavigationOffline();
						MegaOffline mOff = getSelectedOfflineNode();

						NodeController nC = new NodeController(managerActivity);
						nC.deleteOffline(mOff, pathNavigation);

						break;
					}
					case DialogInterface.BUTTON_NEGATIVE: {
						//No button clicked
						break;
					}
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.confirmation_delete_from_save_for_offline).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	public void showConfirmationRemoveSomeFromOffline(final List<MegaOffline> documents){
		log("showConfirmationRemoveSomeFromOffline");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
			if (!hasStoragePermission) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_WRITE_STORAGE);
			}
		}

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {

						String pathNavigation = getPathNavigationOffline();
						NodeController nC = new NodeController(managerActivity);
						for (int i=0;i<documents.size();i++){
							nC.deleteOffline(documents.get(i), pathNavigation);
						}
						updateOfflineView(documents.get(0));
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE: {
						break;
					}
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.confirmation_delete_from_save_for_offline).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}



	public void showConfirmationEnableLogsSDK(){
		log("showConfirmationEnableLogsSDK");

		if(sttFLol!=null){
			sttFLol.numberOfClicksSDK = 0;
		}
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						enableLogsSDK();
						break;

					case DialogInterface.BUTTON_NEGATIVE:

						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.enable_log_text_dialog).setPositiveButton(R.string.general_enable, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show().setCanceledOnTouchOutside(false);
	}

	public void showConfirmationEnableLogsKarere(){
		log("showConfirmationEnableLogsKarere");

		if(sttFLol!=null){
			sttFLol.numberOfClicksKarere = 0;
		}
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						enableLogsKarere();
						break;

					case DialogInterface.BUTTON_NEGATIVE:

						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.enable_log_text_dialog).setPositiveButton(R.string.general_enable, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show().setCanceledOnTouchOutside(false);
	}

	public void enableLogsSDK(){
		log("enableLogsSDK");

		dbH.setFileLoggerSDK(true);
		Util.setFileLoggerSDK(true);
		MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);
		showSnackbar(getString(R.string.settings_enable_logs));
		log("App Version: " + Util.getVersion(this));
	}

	public void enableLogsKarere(){
		log("enableLogsKarere");

		dbH.setFileLoggerKarere(true);
		Util.setFileLoggerKarere(true);
		MegaChatApiAndroid.setLogLevel(MegaChatApiAndroid.LOG_LEVEL_MAX);
		showSnackbar(getString(R.string.settings_enable_logs));
		log("App Version: " + Util.getVersion(this));
	}

	public void showConfirmationDeleteAvatar(){
		log("showConfirmationDeleteAvatar");

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						AccountController aC = new AccountController(managerActivity);
						aC.removeAvatar();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.confirmation_delete_avatar).setPositiveButton(R.string.context_delete, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		log("-------------------onActivityResult "+requestCode + "____" + resultCode);

		if (resultCode == RESULT_FIRST_USER){
			Snackbar.make(fragmentContainer, getString(R.string.context_no_destination_folder), Snackbar.LENGTH_LONG).show();
			return;
		}

		if (requestCode == Constants.REQUEST_CODE_TREE && resultCode == RESULT_OK){
			if (intent == null){
				log("intent NULL");
				return;
			}

			Uri treeUri = intent.getData();
	        DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
		}
		else if (requestCode == Constants.REQUEST_CODE_GET && resultCode == RESULT_OK) {
			log("REQUEST_CODE_GET");
			if (intent == null) {
				log("Return.....");
				return;
			}

			log("Intent action: "+intent.getAction());
			log("Intent action: "+intent.getType());

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
		else if (requestCode == Constants.CHOOSE_PICTURE_PROFILE_CODE && resultCode == RESULT_OK) {

			if (resultCode == RESULT_OK) {
				if (intent == null) {
					log("Return.....");
					return;
				}
				
				boolean isImageAvailable = checkProfileImageExistence(intent.getData());
				if(!isImageAvailable){
					log("error when changing avatar: image not exist");
					showSnackbar(getString(R.string.error_changing_user_avatar_image_not_available));
					return;
				}
				
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
			else {
				log("resultCode for CHOOSE_PICTURE_PROFILE_CODE: "+resultCode);
			}
		}
		else if (requestCode == Constants.REQUEST_CODE_SELECT_CHAT && resultCode == RESULT_OK){
			log("Attach nodes to chats: REQUEST_CODE_SELECT_CHAT");

			long[] chatHandles = intent.getLongArrayExtra("SELECTED_CHATS");
			log("Send to "+chatHandles.length+" chats");

			long[] nodeHandles = intent.getLongArrayExtra("NODE_HANDLES");

			long[] userHandles = intent.getLongArrayExtra("USER_HANDLES");

			int countChat = chatHandles.length;
			log("Selected: "+countChat+" chats to send");

			if(nodeHandles!=null){
				log("Send "+nodeHandles.length+" nodes");
				MultipleAttachChatListener listener = null;
				int counter = chatHandles.length*nodeHandles.length;
				if(countChat==1){
					if(nodeHandles.length==1){
						listener = new MultipleAttachChatListener(this, chatHandles[0], false, counter);
					}
					else{
						listener = new MultipleAttachChatListener(this, chatHandles[0], true, counter);
					}
				}
				else{

					if(nodeHandles.length==1){
						listener = new MultipleAttachChatListener(this, -1, false, counter);
					}
					else{
						listener = new MultipleAttachChatListener(this, -1, true, counter);
					}
				}
				if(countChat==1){

					if(nodeHandles.length==1){
						//One chat, one file
						megaChatApi.attachNode(chatHandles[0], nodeHandles[0], listener);
					}
					else{
						//One chat, many files
						for(int i=0;i<nodeHandles.length;i++){
							megaChatApi.attachNode(chatHandles[0], nodeHandles[i], listener);
						}
					}
				}
				else if(countChat>1){

					if(nodeHandles.length==1){
						//Many chats, one file
						for(int i=0;i<chatHandles.length;i++){
							megaChatApi.attachNode(chatHandles[i], nodeHandles[0], listener);
						}

					}
					else{
						//Many chat, many files
						for(int i=0;i<chatHandles.length;i++){
							for(int j=0;j<nodeHandles.length;j++){
								megaChatApi.attachNode(chatHandles[i], nodeHandles[j], listener);
							}
						}
					}
				}
			}
			else if(userHandles!=null){
				log("Send "+userHandles.length+" contacts");

				for(int i=0;i<chatHandles.length;i++){
					for(int j=0;j<userHandles.length;j++){
						MegaHandleList handleList = MegaHandleList.createInstance();
						handleList.addMegaHandle(userHandles[j]);
						megaChatApi.attachContacts(chatHandles[i], handleList);
					}
				}

				if(countChat==1){
					openChat(chatHandles[0], null);
				}
				else{
					String message = getResources().getQuantityString(R.plurals.plural_contact_sent_to_chats, userHandles.length);
					showSnackbar(message);
				}
			}
			else{
				log("Error on sending to chat");
			}

		}
		else if (requestCode == Constants.WRITE_SD_CARD_REQUEST_CODE && resultCode == RESULT_OK) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
				if (!hasStoragePermission) {
					ActivityCompat.requestPermissions(this,
			                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
							Constants.REQUEST_WRITE_STORAGE);
				}
			}

			Uri treeUri = intent.getData();
			log("--------------Create the document : "+treeUri);
			long handleToDownload = intent.getLongExtra("handleToDownload", -1);
			log("The recovered handle is: "+handleToDownload);
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
		else if (requestCode == Constants.REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
			log("requestCode == REQUEST_CODE_SELECT_FILE");
			if (intent == null) {
				log("Return.....");
				return;
			}

			if(cFLol!=null && cFLol.isAdded()){
				if(cFLol.isMultipleselect()){
					cFLol.hideMultipleSelect();
					cFLol.clearSelectionsNoAnimations();
				}
			}

			ArrayList<String> selectedContacts = intent.getStringArrayListExtra("SELECTED_CONTACTS");
			long fileHandle = intent.getLongExtra("SELECT", 0);

			//Send file to contacts
			//Check if all contacts have a chat created

			ArrayList<MegaChatRoom> chats = null;
			ArrayList<MegaUser> usersNoChat = null;

			for(int i=0; i<selectedContacts.size(); i++){

				MegaUser contact = megaApi.getContact(selectedContacts.get(i));

				MegaChatRoom chatRoom = megaChatApi.getChatRoomByUser(contact.getHandle());
				if(chatRoom!=null){
					if(chats==null){
						chats = new ArrayList<MegaChatRoom>();
					}
					chats.add(chatRoom);

				}
				else{
					if(usersNoChat==null){
						usersNoChat = new ArrayList<MegaUser>();
					}
					usersNoChat.add(contact);
				}
			}

			if(usersNoChat==null || usersNoChat.isEmpty()){
				sendFileToChatsFromContacts(chats, fileHandle);

			}
			else{
				//Create first the chats
				CreateChatToPerformActionListener listener = new CreateChatToPerformActionListener(chats, usersNoChat, fileHandle, this, CreateChatToPerformActionListener.SEND_FILE);

				for(int i=0; i<usersNoChat.size(); i++){
					MegaChatPeerList peers = MegaChatPeerList.createInstance();
					peers.addPeer(usersNoChat.get(i).getHandle(), MegaChatPeerList.PRIV_STANDARD);
					megaChatApi.createChat(false, peers, listener);
				}
			}
		}
		else if(requestCode == Constants.ACTION_SEARCH_BY_DATE && resultCode == RESULT_OK){
			if (intent == null) {
				log("Return.....");
				return;
			}
			searchDate = intent.getLongArrayExtra("SELECTED_DATE");
			if (cuFL != null){
				if(cuFL.isAdded()){
					long cameraUploadHandle = cuFL.getPhotoSyncHandle();
					MegaNode nps = megaApi.getNodeByHandle(cameraUploadHandle);
					if (nps != null){
						ArrayList<MegaNode> nodes = megaApi.getChildren(nps, MegaApiJava.ORDER_MODIFICATION_DESC);
                        if((searchByDate) != null && (searchDate!=null)){
							ArrayList<MegaNode> nodesSearch = cuFL.searchDate(searchDate, nodes);
							cuFL.setNodes(nodesSearch);
							isSearchEnabled = true;
						}else{
							cuFL.setNodes(nodes);

						}

					}
				}
			}

			if (muFLol != null){
				if(muFLol.isAdded()){
					long cameraUploadHandle = muFLol.getPhotoSyncHandle();
					MegaNode nps = megaApi.getNodeByHandle(cameraUploadHandle);
					if (nps != null){
						ArrayList<MegaNode> nodes = megaApi.getChildren(nps, MegaApiJava.ORDER_MODIFICATION_DESC);
						if((searchByDate) != null && (searchDate!=null)){
							ArrayList<MegaNode> nodesSearch = muFLol.searchDate(searchDate, nodes);
							muFLol.setNodes(nodesSearch);
							isSearchEnabled = true;
						}else{
							muFLol.setNodes(nodes);

						}
					}
				}
			}

		}
		else if (requestCode == Constants.REQUEST_CODE_SELECT_FOLDER && resultCode == RESULT_OK) {
			log("REQUEST_CODE_SELECT_FOLDER");

			if (intent == null) {
				log("Return.....");
				return;
			}

			final ArrayList<String> selectedContacts = intent.getStringArrayListExtra("SELECTED_CONTACTS");
			final long folderHandle = intent.getLongExtra("SELECT", 0);

			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
			final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
			dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {

					permissionsDialog.dismiss();
					switch(item) {
						case 0:{
							nC.shareFolder(folderHandle, selectedContacts, MegaShare.ACCESS_READ);
							break;
						}
						case 1:{
							nC.shareFolder(folderHandle, selectedContacts, MegaShare.ACCESS_READWRITE);
							break;
						}
						case 2:{
							nC.shareFolder(folderHandle, selectedContacts, MegaShare.ACCESS_FULL);
							break;
						}
					}
				}
			});
			dialogBuilder.setTitle(getString(R.string.dialog_select_permissions));
			permissionsDialog = dialogBuilder.create();
			permissionsDialog.show();

		}
		else if (requestCode == Constants.REQUEST_CODE_SELECT_CONTACT && resultCode == RESULT_OK){
			log("onActivityResult REQUEST_CODE_SELECT_CONTACT OK");

			if (intent == null) {
				log("Return.....");
				return;
			}

			final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);
			megaContacts = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_MEGA_CONTACTS, true);

			final int multiselectIntent = intent.getIntExtra("MULTISELECT", -1);

			//if (megaContacts){

			if(multiselectIntent==0){
				//One file to share
				final long nodeHandle = intent.getLongExtra(AddContactActivityLollipop.EXTRA_NODE_HANDLE, -1);

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
				dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
				final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
				dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

					permissionsDialog.dismiss();

					switch(item) {
						case 0:{
							nC.shareFolder(nodeHandle, contactsData, MegaShare.ACCESS_READ);
							break;
						}
						case 1:{
							nC.shareFolder(nodeHandle, contactsData, MegaShare.ACCESS_READWRITE);
							break;
						}
						case 2:{
							nC.shareFolder(nodeHandle, contactsData, MegaShare.ACCESS_FULL);
							break;
						}
					}
					}
				});
				dialogBuilder.setTitle(getString(R.string.dialog_select_permissions));
				permissionsDialog = dialogBuilder.create();
				permissionsDialog.show();
			}
			else if(multiselectIntent==1){
				//Several folders to share
				final long[] nodeHandles = intent.getLongArrayExtra(AddContactActivityLollipop.EXTRA_NODE_HANDLE);

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
				dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
				final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
				dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						permissionsDialog.dismiss();
						switch(item) {
							case 0:{
								log("ACCESS_READ");
								nC.shareFolders(nodeHandles, contactsData, MegaShare.ACCESS_READ);
								break;
							}
							case 1:{
								log("ACCESS_READWRITE");
								nC.shareFolders(nodeHandles, contactsData, MegaShare.ACCESS_READWRITE);
								break;
							}
							case 2:{
								log("ACCESS_FULL");
								nC.shareFolders(nodeHandles, contactsData, MegaShare.ACCESS_FULL);

								break;
							}
						}
					}
				});
				dialogBuilder.setTitle(getString(R.string.dialog_select_permissions));
				permissionsDialog = dialogBuilder.create();
				permissionsDialog.show();
			}
			//}
			//else{
				//log("no contact");
//				for (int i=0; i < contactsData.size();i++){
//					String type = contactsData.get(i);
//					if (type.compareTo(ContactsExplorerActivityLollipop.EXTRA_EMAIL) == 0){
//						log("other email");
//
//						i++;
//						Snackbar.make(fragmentContainer, getString(R.string.general_not_yet_implemented), Snackbar.LENGTH_LONG).show();
////						Toast.makeText(this, "Sharing a folder: An email will be sent to the email address: " + contactsData.get(i) + ".\n", Toast.LENGTH_LONG).show();
//					}
//					else if (type.compareTo(ContactsExplorerActivityLollipop.EXTRA_PHONE) == 0){
//						log("contact phone email");
//
//						i++;
//						Snackbar.make(fragmentContainer, getString(R.string.general_not_yet_implemented), Snackbar.LENGTH_LONG).show();
////						Toast.makeText(this, "Sharing a folder: A Text Message will be sent to the phone number: " + contactsData.get(i) , Toast.LENGTH_LONG).show();
//					}
//					else{
//						log("else default!");
//
//						i++;
//						Snackbar.make(fragmentContainer, "Probando!!", Snackbar.LENGTH_LONG).show();
////						Toast.makeText(this, "Sharing a folder: A Text Message will be sent to the phone number: " + contactsData.get(i) , Toast.LENGTH_LONG).show();
//					}
//				}




			//}
		}
		else if (requestCode == Constants.REQUEST_CODE_GET_LOCAL && resultCode == RESULT_OK) {

			if (intent == null) {
				log("Return.....");
				return;
			}

			String folderPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			ArrayList<String> paths = intent.getStringArrayListExtra(FileStorageActivityLollipop.EXTRA_FILES);

			int i = 0;
			long parentHandleUpload=-1;
			log("On section: "+drawerItem);
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				parentHandleUpload = parentHandleBrowser;
			}
			else if(drawerItem == DrawerItem.SHARED_ITEMS){
				int index = viewPagerShares.getCurrentItem();
				if(index==0){
						parentHandleUpload=parentHandleIncoming;
				}
				else if(index==1){
					parentHandleUpload=parentHandleOutgoing;
				}
			}
			else if (drawerItem == DrawerItem.SEARCH){
				if(sFLol!=null && sFLol.isAdded())
				{
					parentHandleUpload = sFLol.getParentHandle();
				}
			}
			else{
				log("Return - nothing to be done");
				return;
			}

			UploadServiceTask uploadServiceTask = new UploadServiceTask(folderPath, paths, parentHandleUpload);
			uploadServiceTask.start();
		}
		else if (requestCode == Constants.REQUEST_CODE_SELECT_MOVE_FOLDER && resultCode == RESULT_OK) {

			if (intent == null) {
				log("Return.....");
				return;
			}

			moveToRubbish = false;

			final long[] moveHandles = intent.getLongArrayExtra("MOVE_HANDLES");
			final long toHandle = intent.getLongExtra("MOVE_TO", 0);

			nC.moveNodes(moveHandles, toHandle);

		}
		else if (requestCode ==  Constants.REQUEST_CODE_SELECT_COPY_FOLDER && resultCode == RESULT_OK){
			log("onActivityResult: REQUEST_CODE_SELECT_COPY_FOLDER");
			if (intent == null) {
				log("Return.....");
				return;
			}
			final long[] copyHandles = intent.getLongArrayExtra("COPY_HANDLES");
			final long toHandle = intent.getLongExtra("COPY_TO", 0);

			nC.copyNodes(copyHandles, toHandle);
		}
		else if (requestCode == Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
			log("onActivityResult: REQUEST_CODE_SELECT_LOCAL_FOLDER");
			if (intent == null) {
				log("Return.....");
				return;
			}

			String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			log("parentPath: "+parentPath);
			String url = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_URL);
			log("url: "+url);
			long size = intent.getLongExtra(FileStorageActivityLollipop.EXTRA_SIZE, 0);
			log("size: "+size);
			long[] hashes = intent.getLongArrayExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES);
			log("hashes size: "+hashes.length);

			nC.checkSizeBeforeDownload(parentPath, url, size, hashes);
//			Snackbar.make(fragmentContainer, getString(R.string.download_began), Snackbar.LENGTH_LONG).show();
		}
		else if (requestCode == Constants.REQUEST_CODE_REFRESH && resultCode == RESULT_OK) {
			log("Resfresh DONE onActivityResult");

			if (intent == null) {
				log("Return.....");
				return;
			}

			((MegaApplication) getApplication()).askForFullAccountInfo();
			((MegaApplication) getApplication()).askForExtendedAccountDetails();

			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				parentHandleBrowser = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
				if (parentNode != null){
					if (fbFLol != null && fbFLol.isAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderCloud);
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
				else{
					if (fbFLol != null && fbFLol.isAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderCloud);
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
			}
//			else if (drawerItem == DrawerItem.RUBBISH_BIN){
//				parentHandleRubbish = intent.getLongExtra("PARENT_HANDLE", -1);
//				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleRubbish);
//				if (parentNode != null){
//					if (rubbishBinFLol != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
//						rubbishBinFLol.setNodes(nodes);
//						rubbishBinFLol.getListView().invalidateViews();
//					}
//				}
//				else{
//					if (rubbishBinFLol != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
//						rubbishBinFLol.setNodes(nodes);
//						rubbishBinFLol.getListView().invalidateViews();
//					}
//				}
//			}
			else if (drawerItem == DrawerItem.SHARED_ITEMS){
				parentHandleIncoming = intent.getLongExtra("PARENT_HANDLE", -1);
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
				if (parentNode != null){
					if (inSFLol != null && inSFLol.isAdded()){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						//TODO: ojo con los hijos
//							inSFLol.setNodes(nodes);
						inSFLol.getRecyclerView().invalidate();
					}
				}
				else{
					if (inSFLol != null && inSFLol.isAdded()){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
						//TODO: ojo con los hijos
//							inSFLol.setNodes(nodes);
						inSFLol.getRecyclerView().invalidate();
					}
				}
			}
		}
		else if (requestCode == Constants.TAKE_PHOTO_CODE){
			log("TAKE_PHOTO_CODE");
			if(resultCode == Activity.RESULT_OK){
				String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.temporalPicDIR + "/picture.jpg";
				File imgFile = new File(filePath);

				String name = Util.getPhotoSyncName(imgFile.lastModified(), imgFile.getAbsolutePath());
				log("Taken picture Name: "+name);
				String newPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.temporalPicDIR + "/"+name;
				log("----NEW Name: "+newPath);
				File newFile = new File(newPath);
				imgFile.renameTo(newFile);

				uploadTakePicture(newPath);
			}
			else{
				log("TAKE_PHOTO_CODE--->ERROR!");
			}

	    }
		else if (requestCode == Constants.TAKE_PICTURE_PROFILE_CODE){
			log("TAKE_PICTURE_PROFILE_CODE");
			if(resultCode == Activity.RESULT_OK){

				String myEmail =  megaApi.getMyUser().getEmail();
				String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.profilePicDIR + "/picture.jpg";;
				File imgFile = new File(filePath);

				String newPath = null;
				File qrFile = null;
				if (getExternalCacheDir() != null){
					newPath = getExternalCacheDir().getAbsolutePath() + "/" + myEmail + "Temp.jpg";
					qrFile = new File(getExternalCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
				}else{
					log("getExternalCacheDir() is NULL");
					newPath = getCacheDir().getAbsolutePath() + "/" + myEmail + "Temp.jpg";
					qrFile = new File(getCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
				}
				if (qrFile.exists()) {
					qrFile.delete();
				}

				if(newPath!=null) {
					File newFile = new File(newPath);
					log("NEW - the destination of the avatar is: " + newPath);
					if (newFile != null) {
						MegaUtilsAndroid.createAvatar(imgFile, newFile);
						megaApi.setAvatar(newFile.getAbsolutePath(), this);

					} else {
						log("Error new path avatar!!");
					}

				}else{
					log("ERROR! Destination PATH is NULL");
				}
			}else{
				log("TAKE_PICTURE_PROFILE_CODE--->ERROR!");
			}

		}
		else if (requestCode == Constants.REQUEST_CODE_SORT_BY && resultCode == RESULT_OK){

			if (intent == null) {
				log("Return.....");
				return;
			}

			int orderGetChildren = intent.getIntExtra("ORDER_GET_CHILDREN", 1);
			if (drawerItem == DrawerItem.CLOUD_DRIVE){
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
				if (parentNode != null){
					if (fbFLol != null && fbFLol.isAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
				else{
					if (fbFLol != null && fbFLol.isAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRootNode(), orderGetChildren);
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
			}
//			else if (drawerItem == DrawerItem.RUBBISH_BIN){
//				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleRubbish);
//				if (parentNode != null){
//					if (rubbishBinFLol != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
//						rubbishBinFLol.setOrder(orderGetChildren);
//						rubbishBinFLol.setNodes(nodes);
//						rubbishBinFLol.getListView().invalidateViews();
//					}
//				}
//				else{
//					if (rubbishBinFLol != null){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderGetChildren);
//						rubbishBinFLol.setOrder(orderGetChildren);
//						rubbishBinFLol.setNodes(nodes);
//						rubbishBinFLol.getListView().invalidateViews();
//					}
//				}
//			}
			else if (drawerItem == DrawerItem.SHARED_ITEMS){
				MegaNode parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
				if (parentNode != null){
					if (inSFLol != null && inSFLol.isAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(parentNode, orderGetChildren);
						//TODO: ojo con los hijos
//							inSFLol.setNodes(nodes);
						inSFLol.getRecyclerView().invalidate();
					}
				}
				else{
					if (inSFLol != null && inSFLol.isAdded()){
//						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getInboxNode(), orderGetChildren);
						inSFLol.getRecyclerView().invalidate();
					}
				}
			}
		}
		else if (requestCode == Constants.REQUEST_CREATE_CHAT && resultCode == RESULT_OK) {
			log("onActivityResult REQUEST_CREATE_CHAT OK");

			if (intent == null) {
				log("Return.....");
				return;
			}

			final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);

			final boolean isGroup = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_GROUP_CHAT, false);

			if (contactsData != null){
				if(!isGroup){
					log("Create one to one chat");
					MegaUser user = megaApi.getContact(contactsData.get(0));
					if(user!=null){
						log("Chat with contact: "+contactsData.size());
						startOneToOneChat(user);
					}
				}
				else{
					log("Create GROUP chat");
					MegaChatPeerList peers = MegaChatPeerList.createInstance();
					for (int i=0; i<contactsData.size(); i++){
						MegaUser user = megaApi.getContact(contactsData.get(i));
						if(user!=null){
							peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
						}
					}
					log("create group chat with participants: "+peers.size());

					final String chatTitle = intent.getStringExtra(AddContactActivityLollipop.EXTRA_CHAT_TITLE);
					if(chatTitle!=null){
						CreateGroupChatWithTitle listener = new CreateGroupChatWithTitle(this, chatTitle);
						megaChatApi.createChat(true, peers, listener);
					}
					else{
						megaChatApi.createChat(true, peers, this);
					}
				}
			}
		}
		else if (requestCode == Constants.REQUEST_INVITE_CONTACT_FROM_DEVICE && resultCode == RESULT_OK) {
			log("onActivityResult REQUEST_INVITE_CONTACT_FROM_DEVICE OK");

			if (intent == null) {
				log("Return.....");
				return;
			}

			final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);
			megaContacts = intent.getBooleanExtra(AddContactActivityLollipop.EXTRA_MEGA_CONTACTS, true);

			if (contactsData != null){
				cC.inviteMultipleContacts(contactsData);
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
	            drawerItem = DrawerItem.ACCOUNT;
//	            Toast.makeText(this, "HURRAY!: ORDERID: **__" + orderId + "__**", Toast.LENGTH_LONG).show();
	            log("HURRAY!: ORDERID: **__" + orderId + "__**");
	        }
		}
		else if (requestCode == Constants.REQUEST_DOWNLOAD_FOLDER && resultCode == RESULT_OK){
			String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			if (parentPath != null){
				String[] split = Util.rKFile.split("/");
				String path = parentPath+"/"+split[split.length-1];
				log("REQUEST_DOWNLOAD_FOLDER:path to download: "+path);
				AccountController ac = new AccountController(this);
				ac.exportMK(path, false);
			}
		}
		else if (requestCode == Constants.REQUEST_SAVE_MK_FROM_OFFLINE && resultCode == RESULT_OK){
			String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
			if (parentPath != null){
				String[] split = Util.rKFile.split("/");
				String path = parentPath+"/"+split[split.length-1];
				log("REQUEST_SAVE_MK_FROM_OFFLINE:path to download: "+path);
				AccountController ac = new AccountController(this);
				ac.exportMK(path, true);
			}
		}
		else{
			log("No requestcode");
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}

	public void sendFileToChatsFromContacts(ArrayList<MegaChatRoom> chats, long fileHandle){
		log("sendFileToChatsFromContacts");

		MultipleAttachChatListener listener = null;

		if(chats.size()==1){
			listener = new MultipleAttachChatListener(this, chats.get(0).getChatId(), false, chats.size());
		}
		else{
			listener = new MultipleAttachChatListener(this, -1, false, chats.size());
		}

		if(chats.size()==1){
			//One chat, one file
			megaChatApi.attachNode(chats.get(0).getChatId(), fileHandle, listener);
		}
		else if(chats.size()>1){
			//Many chats, one file
			for(int i=0;i<chats.size();i++){
				megaChatApi.attachNode(chats.get(i).getChatId(), fileHandle, listener);
			}
		}
	}

	public void startOneToOneChat(MegaUser user){
		log("startOneToOneChat");
		MegaChatRoom chat = megaChatApi.getChatRoomByUser(user.getHandle());
		MegaChatPeerList peers = MegaChatPeerList.createInstance();
		if(chat==null){
			log("No chat, create it!");
			peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
			megaChatApi.createChat(false, peers, this);
		}
		else{
			log("There is already a chat, open it!");
			Intent intentOpenChat = new Intent(this, ChatActivityLollipop.class);
			intentOpenChat.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
			intentOpenChat.putExtra("CHAT_ID", chat.getChatId());
			this.startActivity(intentOpenChat);
		}
	}

	public void startGroupConversation(ArrayList<Long> userHandles){
		log("startGroupConversation");
		MegaChatPeerList peers = MegaChatPeerList.createInstance();

		for(int i=0;i<userHandles.size();i++){
			long handle = userHandles.get(i);
			peers.addPeer(handle, MegaChatPeerList.PRIV_STANDARD);
		}

		megaChatApi.createChat(false, peers, this);
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

			log("Run Upload Service Task");

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

	void disableNavigationViewMenu(Menu menu){
		log("disableNavigationViewMenu");
		MenuItem mi = menu.findItem(R.id.navigation_item_cloud_drive);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_grey));
			mi.setChecked(false);
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.navigation_item_saved_for_offline);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.saved_for_offline_grey));
			mi.setChecked(false);
		}
		mi = menu.findItem(R.id.navigation_item_camera_uploads);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_grey));
			mi.setChecked(false);
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.navigation_item_inbox);
		if (mi != null){
			if(inboxNode==null){
				mi.setVisible(false);
			}
			else{
				boolean hasChildren = megaApi.hasChildren(inboxNode);
				if(hasChildren){
					mi.setIcon(ContextCompat.getDrawable(this, R.drawable.inbox_grey));
					mi.setChecked(false);
					mi.setEnabled(false);
					mi.setVisible(true);
				}
				else{
					mi.setVisible(false);
				}
			}
		}
		mi = menu.findItem(R.id.navigation_item_shared_items);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.shared_items_grey));
			mi.setChecked(false);
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.navigation_item_chat);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_chat));
			mi.setChecked(false);
		}
		mi = menu.findItem(R.id.navigation_item_contacts);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.contacts_grey));
			mi.setChecked(false);
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.navigation_item_settings);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.settings_grey));
			mi.setChecked(false);
		}
	}

	void resetNavigationViewMenu(Menu menu){
		log("resetNavigationViewMenu()");

		if(!Util.isOnline(this)){
			disableNavigationViewMenu(menu);
			return;
		}

		MenuItem mi = menu.findItem(R.id.navigation_item_cloud_drive);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.cloud_drive_grey));
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.navigation_item_saved_for_offline);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.saved_for_offline_grey));
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.navigation_item_camera_uploads);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.camera_uploads_grey));
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.navigation_item_inbox);
		if (mi != null){
			if(inboxNode==null){
				mi.setVisible(false);
				log("Inbox Node is NULL");
			}
			else{
				boolean hasChildren = megaApi.hasChildren(inboxNode);
				if(hasChildren){
					mi.setIcon(ContextCompat.getDrawable(this, R.drawable.inbox_grey));
					mi.setChecked(false);
					mi.setEnabled(true);
					mi.setVisible(true);
				}
				else{
					log("Inbox Node NO children");
					mi.setVisible(false);
				}
			}
		}
		mi = menu.findItem(R.id.navigation_item_shared_items);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.shared_items_grey));
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.navigation_item_chat);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_chat));
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.navigation_item_contacts);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.contacts_grey));
			mi.setChecked(false);
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.navigation_item_settings);
		if (mi != null){
			mi.setIcon(ContextCompat.getDrawable(this, R.drawable.settings_grey));
			mi.setChecked(false);
			mi.setEnabled(true);
		}
	}

	public void setInboxNavigationDrawer(){
		log("setInboxNavigationDrawer");
		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem mi = nVMenu.findItem(R.id.navigation_item_inbox);
			if (mi != null){
				if(inboxNode==null){
					mi.setVisible(false);
					log("Inbox Node is NULL");
				}
				else{
					boolean hasChildren = megaApi.hasChildren(inboxNode);
					if(hasChildren){
						if(drawerItem==DrawerItem.INBOX){
							mi.setChecked(true);
							mi.setIcon(ContextCompat.getDrawable(this, R.drawable.inbox_red));
							mi.setEnabled(true);
							mi.setVisible(true);
						}
						else{
							mi.setIcon(ContextCompat.getDrawable(this, R.drawable.inbox_grey));
							mi.setChecked(false);
							mi.setEnabled(true);
							mi.setVisible(true);
						}
					}
					else{
						log("Inbox Node NO children");
						mi.setVisible(false);
					}
				}
			}
		}
	}

	public void showProPanel(){
		log("showProPanel");
		//Left and Right margin
		LinearLayout.LayoutParams proTextParams = (LinearLayout.LayoutParams)getProText.getLayoutParams();
		proTextParams.setMargins(Util.scaleWidthPx(24, outMetrics), Util.scaleHeightPx(23, outMetrics), Util.scaleWidthPx(24, outMetrics), Util.scaleHeightPx(23, outMetrics));
		getProText.setLayoutParams(proTextParams);

		rightUpgradeButton.setOnClickListener(this);
		android.view.ViewGroup.LayoutParams paramsb2 = rightUpgradeButton.getLayoutParams();
		//Left and Right margin
		LinearLayout.LayoutParams optionTextParams = (LinearLayout.LayoutParams)rightUpgradeButton.getLayoutParams();
		optionTextParams.setMargins(Util.scaleWidthPx(6, outMetrics), 0, Util.scaleWidthPx(8, outMetrics), 0);
		rightUpgradeButton.setLayoutParams(optionTextParams);

		leftCancelButton.setOnClickListener(this);
		android.view.ViewGroup.LayoutParams paramsb1 = leftCancelButton.getLayoutParams();
		leftCancelButton.setLayoutParams(paramsb1);
		//Left and Right margin
		LinearLayout.LayoutParams cancelTextParams = (LinearLayout.LayoutParams)leftCancelButton.getLayoutParams();
		cancelTextParams.setMargins(Util.scaleWidthPx(6, outMetrics), 0, Util.scaleWidthPx(6, outMetrics), 0);
		leftCancelButton.setLayoutParams(cancelTextParams);

		getProLayout.setVisibility(View.VISIBLE);
		getProLayout.bringToFront();
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
		if(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()>MegaAccountDetails.ACCOUNT_TYPE_FREE){
			log("USER PRO");
			paymentButton.setText(getString(R.string.action_upgrade_account));
		}
		else{
			log("FREE USER");
			paymentButton.setText(getString(R.string.plans_depleted_transfer_overquota));
		}

		alertDialogTransferOverquota = dialogBuilder.create();

		continueButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				alertDialogTransferOverquota.dismiss();
			}

		});

		paymentButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				alertDialogTransferOverquota.dismiss();
				navigateToUpgradeAccount();
			}

		});

		alertDialogTransferOverquota.setCancelable(false);
		alertDialogTransferOverquota.setCanceledOnTouchOutside(false);
		alertDialogTransferOverquota.show();
	}

	public void showStorageAlmostFullDialog(){
		log("showStorageAlmostFullDialog");

		if(((MegaApplication) getApplication()).getMyAccountInfo()==null || ((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()==-1){
			log("Do not show dialog, not info of the account received yet");
			return;
		}

		if(((MegaApplication) getApplication()).getMyAccountInfo().getUsedPerc()>=95){
			if(showStorageAlmostFullDialog){

				showStorageAlmostFullDialog = false;

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

				LayoutInflater inflater = this.getLayoutInflater();
				View dialogView = inflater.inflate(R.layout.storage_almost_full_layout, null);
				dialogBuilder.setView(dialogView);

				TextView title = (TextView) dialogView.findViewById(R.id.storage_almost_full_title);
				title.setText(getString(R.string.action_upgrade_account));

				TextView text = (TextView) dialogView.findViewById(R.id.text_storage_almost_full);
				text.setText(getString(R.string.text_almost_full_warning));

				LinearLayout horizontalButtonsLayout = (LinearLayout) dialogView.findViewById(R.id.horizontal_buttons_storage_almost_full_layout);
				LinearLayout verticalButtonsLayout = (LinearLayout) dialogView.findViewById(R.id.vertical_buttons_storage_almost_full_layout);
				Button verticalDismissButton = (Button) dialogView.findViewById(R.id.vertical_storage_almost_full_button_dissmiss);
				Button horizontalDismissButton = (Button) dialogView.findViewById(R.id.horizontal_storage_almost_full_button_dissmiss);
				Button verticalActionButton = (Button) dialogView.findViewById(R.id.vertical_storage_almost_full_button_action);
				Button horizontalActionButton = (Button) dialogView.findViewById(R.id.horizontal_storage_almost_full_button_payment);
				Button achievementsButton = (Button) dialogView.findViewById(R.id.vertical_storage_almost_full_button_achievements);

				if(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()==MegaAccountDetails.ACCOUNT_TYPE_FREE){
					log("show StorageAlmostFull Dialog for FREE USER");

					if(megaApi.isAchievementsEnabled()){
						horizontalButtonsLayout.setVisibility(View.GONE);
						verticalButtonsLayout.setVisibility(View.VISIBLE);
						verticalActionButton.setText(getString(R.string.button_plans_almost_full_warning));

						verticalDismissButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								alertDialogStorageAlmostFull.dismiss();
							}

						});

						achievementsButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								alertDialogStorageAlmostFull.dismiss();
								log("Go to achievements section");
								navigateToAchievements();
							}

						});

						verticalActionButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								alertDialogStorageAlmostFull.dismiss();
								navigateToUpgradeAccount();
							}

						});
					}
					else{
						horizontalButtonsLayout.setVisibility(View.VISIBLE);
						verticalButtonsLayout.setVisibility(View.GONE);
						horizontalActionButton.setText(getString(R.string.button_plans_almost_full_warning));

						horizontalDismissButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								alertDialogStorageAlmostFull.dismiss();
							}
						});

						horizontalActionButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								alertDialogStorageAlmostFull.dismiss();
								navigateToUpgradeAccount();
							}

						});
					}

				}
				else{
					horizontalButtonsLayout.setVisibility(View.VISIBLE);
					verticalButtonsLayout.setVisibility(View.GONE);

					if(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()>=MegaAccountDetails.ACCOUNT_TYPE_PROIII){
						log("show StorageAlmostFull Dialog for USER PRO III");
						horizontalActionButton.setText(getString(R.string.button_custom_almost_full_warning));

						horizontalActionButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								alertDialogStorageAlmostFull.dismiss();
								askForCustomizedPlan();
							}

						});
					}
					else{
						log("show StorageAlmostFull Dialog for USER PRO");
						horizontalActionButton.setText(getString(R.string.my_account_upgrade_pro));

						horizontalActionButton.setOnClickListener(new OnClickListener(){
							public void onClick(View v) {
								alertDialogStorageAlmostFull.dismiss();
								navigateToUpgradeAccount();
							}

						});
					}

					horizontalDismissButton.setOnClickListener(new OnClickListener(){
						public void onClick(View v) {
							alertDialogStorageAlmostFull.dismiss();
						}

					});

				}

				alertDialogStorageAlmostFull = dialogBuilder.create();

				alertDialogStorageAlmostFull.setCancelable(false);
				alertDialogStorageAlmostFull.setCanceledOnTouchOutside(false);
				alertDialogStorageAlmostFull.show();
			}
			else{
				log("Storage almost full dialog already shown");
			}
		}
		else{
			if(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()==MegaAccountDetails.ACCOUNT_TYPE_FREE){
				log("usedSpacePerc<95");
				if(Util.showMessageRandom()){
					log("Random: TRUE");
					showProPanel();
				}
			}
		}
	}

	public void askForCustomizedPlan(){
		log("askForCustomizedPlan");

		StringBuilder body = new StringBuilder();
		body.append(getString(R.string.subject_mail_upgrade_plan));
		body.append("\n\n\n\n\n\n\n");
		body.append(getString(R.string.settings_about_app_version)+" v"+getString(R.string.app_version)+"\n");
		body.append(getString(R.string.user_account_feedback)+"  "+megaApi.getMyEmail());

		if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
			if(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()<0||((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()>4){
				body.append(" ("+getString(R.string.my_account_free)+")");
			}
			else{
				switch(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()){
					case 0:{
						body.append(" ("+getString(R.string.my_account_free)+")");
						break;
					}
					case 1:{
						body.append(" ("+getString(R.string.my_account_pro1)+")");
						break;
					}
					case 2:{
						body.append(" ("+getString(R.string.my_account_pro2)+")");
						break;
					}
					case 3:{
						body.append(" ("+getString(R.string.my_account_pro3)+")");
						break;
					}
					case 4:{
						body.append(" (PRO "+getString(R.string.my_account_prolite)+")");
						break;
					}
				}
			}
		}

		String emailAndroid = Constants.MAIL_SUPPORT;
		String subject = getString(R.string.title_mail_upgrade_plan);

		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAndroid));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
		startActivity(Intent.createChooser(emailIntent, " "));

	}

	public void updateCancelSubscriptions(){
		log("updateCancelSubscriptions");
		if (cancelSubscription != null){
			cancelSubscription.setVisible(false);
		}
		if (((MegaApplication) getApplication()).getMyAccountInfo().getNumberOfSubscriptions() > 0){
			if (cancelSubscription != null){
				if (drawerItem == DrawerItem.ACCOUNT){
					if (maFLol != null){
						cancelSubscription.setVisible(true);
					}
				}
			}
		}
	}

	public void updateOfflineView(MegaOffline mOff){
		log("updateOfflineView");
		if(oFLol!=null && oFLol.isAdded()){
			oFLol.hideMultipleSelect();
			if(mOff==null){
				oFLol.refresh();

			}
			else{
				oFLol.refreshPaths(mOff);
			}
			supportInvalidateOptionsMenu();
		}
	}

	public void updateContactsView(boolean contacts, boolean sentRequests, boolean receivedRequests){
		log("updateContactsView");

		if(contacts){
			log("Update Contacts Fragment");
			String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
			cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
			if (cFLol != null && cFLol.isAdded()){
				cFLol.hideMultipleSelect();
				cFLol.updateView();
			}
		}

		if(sentRequests){
			log("Update SentRequests Fragment");
			String cFTagSR = getFragmentTag(R.id.contact_tabs_pager, 1);
			sRFLol = (SentRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTagSR);
			if (sRFLol != null && sRFLol.isAdded()){
				sRFLol.hideMultipleSelect();
				sRFLol.updateView();
			}
		}

		if(receivedRequests){
			log("Update ReceivedRequest Fragment");
			String cFTagRR = getFragmentTag(R.id.contact_tabs_pager, 2);
			rRFLol = (ReceivedRequestsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTagRR);
			if (rRFLol != null && rRFLol.isAdded()){
				rRFLol.hideMultipleSelect();
				rRFLol.updateView();
			}
		}
	}

	/*
	 * Handle processed upload intent
	 */
	public void onIntentProcessed(List<ShareInfo> infos) {
		log("onIntentProcessedLollipop");
//		List<ShareInfo> infos = filePreparedInfos;
		if (statusDialog != null) {
			try {
				statusDialog.dismiss();
			}
			catch(Exception ex){}
		}

		long parentHandle = -1;
		MegaNode parentNode = null;
		if (drawerItem == DrawerItem.CLOUD_DRIVE){
			parentHandle = parentHandleBrowser;
			parentNode = megaApi.getNodeByHandle(parentHandle);
			if (parentNode == null){
				parentNode = megaApi.getRootNode();
			}
		}
		else if (drawerItem == DrawerItem.SHARED_ITEMS){
			int index = viewPagerShares.getCurrentItem();
			if(index==1){
				parentNode = megaApi.getNodeByHandle(parentHandleOutgoing);
			}
			else{
				parentNode = megaApi.getNodeByHandle(parentHandleIncoming);
			}
			if(parentNode==null){
				log("Incorrect folder to upload");
				parentNode = megaApi.getRootNode();
			}
		}
		else if(drawerItem == DrawerItem.ACCOUNT){
			if(infos!=null){
				for (ShareInfo info : infos) {
					String avatarPath = info.getFileAbsolutePath();
					if(avatarPath!=null){
						log("Chosen picture to change the avatar: "+avatarPath);
						File imgFile = new File(avatarPath);
//						String name = Util.getPhotoSyncName(imgFile.lastModified(), imgFile.getAbsolutePath());
						String newPath = null;
						File qrFile = null;
						if (getExternalCacheDir() != null){
							newPath = getExternalCacheDir().getAbsolutePath() + "/" + megaApi.getMyUser().getEmail() + "Temp.jpg";
							qrFile = new File(getExternalCacheDir().getAbsolutePath(), megaApi.getMyUser().getEmail() + "QRcode.jpg");
						}
						else{
							log("getExternalCacheDir() is NULL");
							newPath = getCacheDir().getAbsolutePath() + "/" + megaApi.getMyUser().getEmail() + "Temp.jpg";
							qrFile = new File(getCacheDir().getAbsolutePath(), megaApi.getMyUser().getEmail() + "QRcode.jpg");
						}

						if (qrFile.exists()) {
							qrFile.delete();
						}

						if(newPath!=null){
							File newFile = new File(newPath);
							log("NEW - the destination of the avatar is: "+newPath);
							if(newFile!=null){
								MegaUtilsAndroid.createAvatar(imgFile, newFile);
								String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
								maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
								if(maFLol!=null){
									megaApi.setAvatar(newFile.getAbsolutePath(), this);
								}

							}
							else{
								log("Error new path avatar!!");
							}
						}
						else{
							log("ERROR! Destination PATH is NULL");
						}


//						String newPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.profilePicDIR + "/"+name;
//						log("----NEW Name: "+newPath);
//						File newFile = new File(newPath);
//						MegaUtilsAndroid.createAvatar(imgFile, newFile);

					}
					else{
						log("The chosen avatar path is NULL");
					}
				}
			}
			else{
				log("infos is NULL");
			}
			return;
		}

		if(parentNode == null){
			Snackbar.make(fragmentContainer, getString(R.string.error_temporary_unavaible), Snackbar.LENGTH_LONG).show();
			return;
		}

		if (infos == null) {
			Snackbar.make(fragmentContainer, getString(R.string.upload_can_not_open), Snackbar.LENGTH_LONG).show();
		}
		else {
			for (ShareInfo info : infos) {
				if(info.isContact){
					requestContactsPermissions(info, parentNode);
				}
				else{
					Snackbar.make(fragmentContainer, getString(R.string.upload_began), Snackbar.LENGTH_LONG).show();
					Intent intent = new Intent(this, UploadService.class);
					intent.putExtra(UploadService.EXTRA_FILEPATH, info.getFileAbsolutePath());
					intent.putExtra(UploadService.EXTRA_NAME, info.getTitle());
					intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
					intent.putExtra(UploadService.EXTRA_SIZE, info.getSize());
					startService(intent);
				}
			}
		}
	}

	public void requestContactsPermissions(ShareInfo info, MegaNode parentNode){
		log("requestContactsPermissions");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			boolean hasReadContactsPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
			if (!hasReadContactsPermission) {
				log("No read contacts permission");
				infoManager = info;
				parentNodeManager = parentNode;
				ActivityCompat.requestPermissions(this,	new String[]{Manifest.permission.READ_CONTACTS}, Constants.REQUEST_UPLOAD_CONTACT);
			} else {
				uploadContactInfo(info, parentNode);
			}
		}
		else{
			uploadContactInfo(info, parentNode);
		}
	}

	public void uploadContactInfo(ShareInfo info, MegaNode parentNode){
		log("Upload contact info");

		Cursor cursorID = getContentResolver().query(info.contactUri, null, null, null, null);

		if (cursorID != null) {
			if (cursorID.moveToFirst()) {
				log("It is a contact");

				String id = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				Integer hasPhone = cursorID.getInt(cursorID.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

				// get the user's email address
				String email = null;
				Cursor ce = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
						ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
				if (ce != null && ce.moveToFirst()) {
					email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					ce.close();
				}

				// get the user's phone number
				String phone = null;
				if (hasPhone > 0) {
					Cursor cp = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
					if (cp != null && cp.moveToFirst()) {
						phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						cp.close();
					}
				}

				StringBuilder data = new StringBuilder();
				data.append(name);
				if(phone!=null){
					data.append(", "+phone);
				}

				if(email!=null){
					data.append(", "+email);
				}

				createFile(name, data.toString(), parentNode);
			}
		}
		else{
			showSnackbar(getString(R.string.error_temporary_unavaible));
		}
	}

	private void createFile(String name, String data, MegaNode parentNode){

		File file = Util.createTemporalTextFile(name, data);
		if(file!=null){
			Snackbar.make(fragmentContainer,getString(R.string.upload_began),Snackbar.LENGTH_LONG).show();

			Intent intent = new Intent(this, UploadService.class);
			intent.putExtra(UploadService.EXTRA_FILEPATH, file.getAbsolutePath());
			intent.putExtra(UploadService.EXTRA_NAME, file.getName());
			intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
			intent.putExtra(UploadService.EXTRA_SIZE, file.getTotalSpace());
			startService(intent);
		}
		else{
			Snackbar.make(fragmentContainer,getString(R.string.email_verification_text_error),Snackbar.LENGTH_LONG).show();
		}
	}

	@Override
	public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {
//		if (request.getType() == MegaChatRequest.TYPE_INITIALIZE){
//			MegaApiAndroid.setLoggerObject(new AndroidLogger());
////			MegaChatApiAndroid.setLoggerObject(new AndroidChatLogger());
//		}
	}

	@Override
	public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

	}

	@Override
	public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
		log("onRequestFinish(CHAT): " + request.getRequestString()+"_"+e.getErrorCode());

		if(request.getType() == MegaChatRequest.TYPE_TRUNCATE_HISTORY){
			log("Truncate history request finish.");
			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				showSnackbar(getString(R.string.clear_history_success));
			}
			else{
				showSnackbar(getString(R.string.clear_history_error));
				log("Error clearing history: "+e.getErrorString());
			}
		}
		else if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM){
			log("Create chat request finish");
			onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), false);
		}
		else if(request.getType() == MegaChatRequest.TYPE_REMOVE_FROM_CHATROOM){
			log("remove from chat finish!!!");
			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				//Update chat view
//				if(rChatFL!=null){
//					rChatFL.setChats();
//				}
			}
			else{
				log("EEEERRRRROR WHEN leaving CHAT " + e.getErrorString());
				showSnackbar(getString(R.string.leave_chat_error));
			}
		}
		else if (request.getType() == MegaChatRequest.TYPE_CONNECT){
			log("Connecting chat finished");

			if (MegaApplication.isFirstConnect()){
				log("Set first connect to false");
				MegaApplication.setFirstConnect(false);
			}

			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				log("CONNECT CHAT finished ");
				if(drawerItem == DrawerItem.CHAT){
					if(rChatFL!=null){
						if(rChatFL.isAdded()){
							rChatFL.onlineStatusUpdate(megaChatApi.getOnlineStatus());
						}
					}
				}
			}
			else{
				log("ERROR WHEN CONNECTING " + e.getErrorString());
//				showSnackbar(getString(R.string.chat_connection_error));
			}
		}
		else if (request.getType() == MegaChatRequest.TYPE_DISCONNECT){
			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				log("DISConnected from chat!");
			}
			else{
				log("EEEERRRRROR WHEN DISCONNECTING " + e.getErrorString());
			}
		}
		else if (request.getType() == MegaChatRequest.TYPE_LOGOUT){
			log("onRequestFinish(CHAT): " + MegaChatRequest.TYPE_LOGOUT);

			if (e.getErrorCode() != MegaError.API_OK){
				log("onRequestFinish(CHAT):MegaChatRequest.TYPE_LOGOUT:ERROR");
			}
			if(sttFLol!=null){
				if(sttFLol.isAdded()){
					sttFLol.hidePreferencesChat();
				}
			}

			if (app != null){
				app.disableMegaChatApi();
			}
			Util.resetAndroidLogger();
		}
		else if(request.getType() == MegaChatRequest.TYPE_SET_ONLINE_STATUS){
			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				log("Status changed to: "+request.getNumber());
			}
			else{
				log("EEEERRRRROR WHEN TYPE_SET_ONLINE_STATUS " + e.getErrorString());
				showSnackbar(getString(R.string.changing_status_error));
			}
		}
		else if(request.getType() == MegaChatRequest.TYPE_ARCHIVE_CHATROOM){
			long chatHandle = request.getChatHandle();
			MegaChatRoom chat = megaChatApi.getChatRoom(chatHandle);
			String chatTitle = chat.getTitle();

			if(chatTitle==null){
				chatTitle = "";
			}
			else if(!chatTitle.isEmpty() && chatTitle.length()>60){
				chatTitle = chatTitle.substring(0,59)+"...";
			}

			if(!chatTitle.isEmpty() && chat.isGroup() && !chat.hasCustomTitle()){
				chatTitle = "\""+chatTitle+"\"";
			}

			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				if(request.getFlag()){
					log("Chat archived");
					showSnackbar(getString(R.string.success_archive_chat, chatTitle));
				}
				else{
					log("Chat unarchived");
					showSnackbar(getString(R.string.success_unarchive_chat, chatTitle));
				}
			}
			else{
				if(request.getFlag()){
					log("EEEERRRRROR WHEN ARCHIVING CHAT " + e.getErrorString());
					showSnackbar(getString(R.string.error_archive_chat, chatTitle));
				}
				else{
					log("EEEERRRRROR WHEN UNARCHIVING CHAT " + e.getErrorString());
					showSnackbar(getString(R.string.error_unarchive_chat, chatTitle));
				}
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

	}

	public void onRequestFinishCreateChat(int errorCode, long chatHandle, boolean loadMessages){
		if(errorCode==MegaChatError.ERROR_OK){
			log("Chat CREATED.");

			//Update chat view
			if(rChatFL!=null && rChatFL.isAdded()){

				if(selectMenuItem!=null){
					selectMenuItem.setVisible(true);
				}
			}

			log("open new chat: " + chatHandle);
			Intent intent = new Intent(this, ChatActivityLollipop.class);
			if(loadMessages){
				intent.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
			}
			else{
				intent.setAction(Constants.ACTION_NEW_CHAT);
			}

			intent.putExtra("CHAT_ID", chatHandle);
			this.startActivity(intent);

//				log("open new chat");
//				Intent intent = new Intent(this, ChatActivityLollipop.class);
//				intent.setAction(Constants.ACTION_CHAT_NEW);
//				String myMail = getMyAccountInfo().getMyUser().getEmail();
//				intent.putExtra("CHAT_ID", request.getChatHandle());
//				intent.putExtra("MY_MAIL", myMail);
//
//				boolean isGroup = request.getFlag();
//				if(isGroup){
//					log("GROUP");
//					MegaChatPeerList list = request.getMegaChatPeerList();
//					log("Size: "+list.size());
//
//				}
//				else{
//					log("NOT group");
//				}
//
//				this.startActivity(intent);
		}
		else{
			log("EEEERRRRROR WHEN CREATING CHAT " + errorCode);
			showSnackbar(getString(R.string.create_chat_error));
		}
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getRequestString());
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		log("onRequestUpdate: " + request.getRequestString());
	}

	@SuppressLint("NewApi") @Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		log("onRequestFinish: " + request.getRequestString()+"_"+e.getErrorCode());

		if (request.getType() == MegaRequest.TYPE_CREDIT_CARD_CANCEL_SUBSCRIPTIONS){
			if (e.getErrorCode() == MegaError.API_OK){
				Snackbar.make(fragmentContainer, getString(R.string.cancel_subscription_ok), Snackbar.LENGTH_LONG).show();
			}
			else{
				Snackbar.make(fragmentContainer, getString(R.string.cancel_subscription_error), Snackbar.LENGTH_LONG).show();
			}
			((MegaApplication) getApplication()).askForCCSubscriptions();
		}
		else if (request.getType() == MegaRequest.TYPE_LOGOUT){
			log("onRequestFinish: " + MegaRequest.TYPE_LOGOUT);

			if (e.getErrorCode() == MegaError.API_OK){
				log("onRequestFinish:OK:" + MegaRequest.TYPE_LOGOUT);
				if(Util.isChatEnabled()){
					log("END logout sdk request - wait chat logout");
				}
				else{
					log("END logout sdk request - chat disabled");
					if (dbH == null){
						dbH = DatabaseHandler.getDbHandler(getApplicationContext());
					}
					if (dbH != null){
						dbH.clearEphemeral();
					}

					AccountController aC = new AccountController(this);
					aC.logoutConfirmed(this);

					Intent tourIntent = new Intent(this, LoginActivityLollipop.class);
					tourIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					this.startActivity(tourIntent);

					finish();
				}
			}
			else if (e.getErrorCode() != MegaError.API_ESID){
				showSnackbar(getString(R.string.email_verification_text_error));
			}
		}
		else if(request.getType() == MegaRequest.TYPE_SET_ATTR_USER) {
			log("TYPE_SET_ATTR_USER");
			if(request.getParamType()==MegaApiJava.USER_ATTR_FIRSTNAME){
				log("(1)request.getText(): "+request.getText());
				countUserAttributes--;
				if(((MegaApplication) getApplication()).getMyAccountInfo() == null){
					log("ERROR: MyAccountInfo is NULL");
				}
				((MegaApplication) getApplication()).getMyAccountInfo().setFirstNameText(request.getText());
				if (e.getErrorCode() == MegaError.API_OK){
					log("The first name has changed");
					String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
					if(maFLol!=null){
						if(maFLol.isAdded()){
							maFLol.updateNameView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
						}
					}
					updateUserNameNavigationView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName(), ((MegaApplication) getApplication()).getMyAccountInfo().getFirstLetter());
				}
				else{
					log("Error with first name");
					errorUserAttibutes++;
				}

				if(countUserAttributes==0){
					if(errorUserAttibutes==0){
						log("All user attributes changed!");
						showSnackbar(getString(R.string.success_changing_user_attributes));
					}
					else{
						log("Some error ocurred when changing an attribute: "+errorUserAttibutes);
						showSnackbar(getString(R.string.error_changing_user_attributes));
					}
					AccountController aC = new AccountController(this);
					errorUserAttibutes=0;
					aC.setCount(0);
				}
			}
			else if(request.getParamType()==MegaApiJava.USER_ATTR_LASTNAME){
				log("(2)request.getText(): "+request.getText());
				countUserAttributes--;
				if(((MegaApplication) getApplication()).getMyAccountInfo() == null){
					log("ERROR: MyAccountInfo is NULL");
				}
				((MegaApplication) getApplication()).getMyAccountInfo().setLastNameText(request.getText());
				if (e.getErrorCode() == MegaError.API_OK){
					log("The last name has changed");
					String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
					if(maFLol!=null){
						if(maFLol.isAdded()){
							maFLol.updateNameView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
						}
					}
					updateUserNameNavigationView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName(), ((MegaApplication) getApplication()).getMyAccountInfo().getFirstLetter());
				}
				else{
					log("Error with last name");
					errorUserAttibutes++;
				}

				if(countUserAttributes==0){
					if(errorUserAttibutes==0){
						log("All user attributes changed!");
						showSnackbar(getString(R.string.success_changing_user_attributes));
					}
					else{
						log("Some error ocurred when changing an attribute: "+errorUserAttibutes);
						showSnackbar(getString(R.string.error_changing_user_attributes));
					}
					AccountController aC = new AccountController(this);
					errorUserAttibutes=0;
					aC.setCount(0);
				}
			}
			else if(request.getParamType() == MegaApiJava.USER_ATTR_PWD_REMINDER){
				log("MK exported - USER_ATTR_PWD_REMINDER finished");
				if (e.getErrorCode() == MegaError.API_OK || e.getErrorCode() == MegaError.API_ENOENT) {
					log("New value of attribute USER_ATTR_PWD_REMINDER: " + request.getText());
				}
			}
			else if (request.getParamType() == MegaApiJava.USER_ATTR_AVATAR) {

				if (e.getErrorCode() == MegaError.API_OK){
					log("Avatar changed!!");
					if(request.getFile()!=null){
						log("old path: "+request.getFile());
						File oldFile = new File(request.getFile());
						if(oldFile!=null){
							if(oldFile.exists()){
								String newPath = null;
								if (getExternalCacheDir() != null){
									newPath = getExternalCacheDir().getAbsolutePath() + "/" + megaApi.getMyEmail() + ".jpg";
								}
								else{
									log("getExternalCacheDir() is NULL");
									newPath = getCacheDir().getAbsolutePath() + "/" + megaApi.getMyEmail() + ".jpg";
								}
								File newFile = new File(newPath);
								boolean result = oldFile.renameTo(newFile);
								if(result){
									log("The avatar file was correctly renamed");
								}
							}
						}
						log("User avatar changed!");
						showSnackbar(getString(R.string.success_changing_user_avatar));
					}
					else{

						log("User avatar deleted!");
						showSnackbar(getString(R.string.success_deleting_user_avatar));
					}
					setProfileAvatar();

					String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
					if(maFLol!=null){
						if(maFLol.isAdded()){
							maFLol.updateAvatar(false);
						}
					}
				}
				else{

					if(request.getFile()!=null){

						log("Some error ocurred when changing avatar: "+e.getErrorString()+" "+e.getErrorCode());
						showSnackbar(getString(R.string.error_changing_user_avatar));
					}
					else{

						log("Some error ocurred when deleting avatar: "+e.getErrorString()+" "+e.getErrorCode());
						showSnackbar(getString(R.string.error_deleting_user_avatar));
					}

				}
			}
			else if (request.getParamType() == MegaApiJava.USER_ATTR_RICH_PREVIEWS) {
				log("change isRickLinkEnabled - USER_ATTR_RICH_PREVIEWS finished");
				if (e.getErrorCode() != MegaError.API_OK){
					log("ERROR:USER_ATTR_RICH_PREVIEWS");
					if(sttFLol!=null){
						if(sttFLol.isAdded()){
							sttFLol.updateEnabledRichLinks();
						}
					}
				}
			}
			else if (request.getParamType() == MegaApiJava.USER_ATTR_CONTACT_LINK_VERIFICATION) {
				log("change QR autoaccept - USER_ATTR_CONTACT_LINK_VERIFICATION finished");
				if (e.getErrorCode() == MegaError.API_OK) {
					log("OK setContactLinkOption: " + request.getText());
					if (sttFLol != null && sttFLol.isAdded()) {
						sttFLol.setSetAutoaccept(false);
						if (sttFLol.getAutoacceptSetting()) {
							sttFLol.setAutoacceptSetting(false);
						} else {
							sttFLol.setAutoacceptSetting(true);
						}
						sttFLol.setValueOfAutoaccept(sttFLol.getAutoacceptSetting());
						log("autoacept: " + sttFLol.getAutoacceptSetting());
					}
				} else {
					log("Error setContactLinkOption");
				}
			}
			else if(request.getParamType() == MegaApiJava.USER_ATTR_DISABLE_VERSIONS){
				MegaApplication.setDisableFileVersions(Boolean.valueOf(request.getText()));

				if (e.getErrorCode() != MegaError.API_OK) {
					log("ERROR:USER_ATTR_DISABLE_VERSIONS");
					if(sttFLol!=null && sttFLol.isAdded()){
						sttFLol.updateEnabledFileVersions();
					}
				}
				else{
					log("File versioning attribute changed correctly");
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER){
			if(request.getParamType() == MegaApiJava.USER_ATTR_PWD_REMINDER){
				//Listener from logout menu
				log("TYPE_GET_ATTR_USER. PasswordReminderFromMyAccount: "+getPasswordReminderFromMyAccount());
				if (e.getErrorCode() == MegaError.API_OK || e.getErrorCode() == MegaError.API_ENOENT){
					log("New value of attribute USER_ATTR_PWD_REMINDER: " +request.getText());
					if (request.getFlag()){
						if (getPasswordReminderFromMyAccount()){
							showRememberPasswordDialog(true);
						}
						else {
							showRememberPasswordDialog(false);
						}
					}
					else if (getPasswordReminderFromMyAccount()){
						if (aC == null){
							aC = new AccountController(this);
						}
						aC.logout(this, megaApi);
					}
				}
				setPasswordReminderFromMyAccount(false);
			}
			else if(request.getParamType()==MegaApiJava.USER_ATTR_AVATAR){
				log("(0)request avatar");
				if (e.getErrorCode() == MegaError.API_OK){
					setProfileAvatar();
					//refresh MyAccountFragment if visible
					String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
					if(maFLol!=null && maFLol.isAdded()){
						log("Update the account fragment");
						maFLol.updateAvatar(false);
					}
				}
				else{
					if(e.getErrorCode()==MegaError.API_ENOENT) {
						setDefaultAvatar();
					}

					if(e.getErrorCode()==MegaError.API_EARGS){
						log("Error changing avatar: ");
						if(request.getFile()!=null){
							log("DESTINATION FILE: "+request.getFile());
						}
						if(request.getEmail()!=null){
							log("email: "+request.getEmail());
						}
					}

					//refresh MyAccountFragment if visible
					String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
					maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
					if(maFLol!=null && maFLol.isAdded()){
						log("Update the account fragment");
						maFLol.updateAvatar(false);
					}
				}
			}
			else if(request.getParamType()==MegaApiJava.USER_ATTR_FIRSTNAME){
				if (e.getErrorCode() == MegaError.API_OK){
					log("(1)request.getText(): "+request.getText());
					if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
						((MegaApplication) getApplication()).getMyAccountInfo().setFirstNameText(request.getText());
						((MegaApplication) getApplication()).getMyAccountInfo().setFirstName(true);
					}
					dbH.saveMyFirstName(request.getText());
				}
				else{
					log("ERROR - (1)request.getText(): "+request.getText());
					if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
						((MegaApplication) getApplication()).getMyAccountInfo().setFirstNameText("");
						((MegaApplication) getApplication()).getMyAccountInfo().setFirstName(true);
					}
				}

				if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
					if(((MegaApplication) getApplication()).getMyAccountInfo().isFirstName() && ((MegaApplication) getApplication()).getMyAccountInfo().isLastName()){
						log("Name and First Name received!");

						((MegaApplication) getApplication()).getMyAccountInfo().setFullName();
						updateUserNameNavigationView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName(), ((MegaApplication) getApplication()).getMyAccountInfo().getFirstLetter());

						((MegaApplication) getApplication()).getMyAccountInfo().setFirstName(false);
						((MegaApplication) getApplication()).getMyAccountInfo().setLastName(false);

						//refresh MyAccountFragment if visible
						String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
						maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
						if(maFLol!=null && maFLol.isAdded()){
							log("Update the account fragment");
							maFLol.updateNameView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
						}
					}
				}

			}
			else if(request.getParamType()==MegaApiJava.USER_ATTR_LASTNAME){
				if (e.getErrorCode() == MegaError.API_OK){
					log("(2)request.getText(): "+request.getText());
					if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
						((MegaApplication) getApplication()).getMyAccountInfo().setLastNameText(request.getText());
						((MegaApplication) getApplication()).getMyAccountInfo().setLastName(true);
					}

					dbH.saveMyLastName(request.getText());
				}
				else{
					log("ERROR - (2)request.getText(): "+request.getText());
					if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
						((MegaApplication) getApplication()).getMyAccountInfo().setLastNameText("");
						((MegaApplication) getApplication()).getMyAccountInfo().setLastName(true);
					}
				}

				if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
					if(((MegaApplication) getApplication()).getMyAccountInfo().isFirstName() && ((MegaApplication) getApplication()).getMyAccountInfo().isLastName()){
						log("Name and First Name received!");

						((MegaApplication) getApplication()).getMyAccountInfo().setFullName();
						updateUserNameNavigationView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName(), ((MegaApplication) getApplication()).getMyAccountInfo().getFirstLetter());

						((MegaApplication) getApplication()).getMyAccountInfo().setFirstName(false);
						((MegaApplication) getApplication()).getMyAccountInfo().setLastName(false);

						//refresh MyAccountFragment if visible
						String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
						maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
						if(maFLol!=null && maFLol.isAdded()){
							log("Update the account fragment");
							maFLol.updateNameView(((MegaApplication) getApplication()).getMyAccountInfo().getFullName());
						}
					}
				}
			}
            else if(request.getParamType() == MegaApiJava.USER_ATTR_RICH_PREVIEWS){

				if(e.getErrorCode() == MegaError.API_ENOENT){
					log("Attribute USER_ATTR_RICH_PREVIEWS not set");
				}

				if(request.getNumDetails()==1){
					log("USER_ATTR_RICH_PREVIEWS:shouldShowRichLinkWarning:");

					long counter = request.getNumber();
					boolean flag = request.getFlag();

					MegaApplication.setShowRichLinkWarning(request.getFlag());
					MegaApplication.setCounterNotNowRichLinkWarning((int) request.getNumber());
				}
				else if(request.getNumDetails()==0){

					log("USER_ATTR_RICH_PREVIEWS:isRichPreviewsEnabled:"+request.getFlag());

					MegaApplication.setEnabledRichLinks(request.getFlag());

                    if(sttFLol!=null){
                        if(sttFLol.isAdded()){
                            sttFLol.updateEnabledRichLinks();
                        }
                    }
				}
            }
            else if (request.getParamType() == MegaApiJava.USER_ATTR_CONTACT_LINK_VERIFICATION) {
				log("Type: GET_ATTR_USER ParamType: USER_ATTR_CONTACT_LINK_VERIFICATION --> getContactLinkOption");
				if (e.getErrorCode() == MegaError.API_OK) {
					if (sttFLol != null && sttFLol.isAdded()) {
						sttFLol.setAutoacceptSetting(request.getFlag());
						log("OK getContactLinkOption: " + request.getFlag());
//						If user request to set QR autoaccept
						if (sttFLol.getSetAutoaccept()) {
							if (sttFLol.getAutoacceptSetting()) {
								log("setAutoaccept false");
//								If autoaccept is enabled -> request to disable
								megaApi.setContactLinksOption(true, this);
							} else {
								log("setAutoaccept true");
//								If autoaccept is disabled -> request to enable
								megaApi.setContactLinksOption(false, this);
							}
						} else {
							sttFLol.setValueOfAutoaccept(sttFLol.getAutoacceptSetting());
						}
						log("autoacept: " + sttFLol.getAutoacceptSetting());
					}
				} else if (e.getErrorCode() == MegaError.API_ENOENT) {
					log("Error MegaError.API_ENOENT getContactLinkOption: " + request.getFlag());
					if (sttFLol != null && sttFLol.isAdded()) {
						sttFLol.setAutoacceptSetting(request.getFlag());
					}
					megaApi.setContactLinksOption(false, this);
				} else {
					log("Error getContactLinkOption: " + e.getErrorString());
				}
			}
            else if(request.getParamType() == MegaApiJava.USER_ATTR_DISABLE_VERSIONS){
				MegaApplication.setDisableFileVersions(request.getFlag());
				if(sttFLol!=null && sttFLol.isAdded()){
					sttFLol.updateEnabledFileVersions();
				}
			}
		}
		else if(request.getType() == MegaRequest.TYPE_GET_CHANGE_EMAIL_LINK) {
			log("TYPE_GET_CHANGE_EMAIL_LINK: "+request.getEmail());
			if (verify2faProgressBar != null) {
				verify2faProgressBar.setVisibility(View.GONE);
			}
			if (e.getErrorCode() == MegaError.API_OK){
				log("The change link has been sent");
				hideKeyboard();
				if (verify2FADialog != null && verify2FADialog.isShowing()) {
					verify2FADialog.dismiss();
				}
				Util.showAlert(this, getString(R.string.email_verification_text_change_mail), getString(R.string.email_verification_title));
			}
			else if(e.getErrorCode() == MegaError.API_EEXIST){
				log("The new mail already exists");
				hideKeyboard();
				if (verify2FADialog != null && verify2FADialog.isShowing()) {
					verify2FADialog.dismiss();
				}
				Util.showAlert(this, getString(R.string.mail_already_used), getString(R.string.email_verification_title));
			}
			else if (e.getErrorCode() == MegaError.API_EFAILED || e.getErrorCode() == MegaError.API_EEXPIRED){
				if (is2FAEnabled()){
					verifyShowError();
				}
			}
			else{
				log("Error when asking for change mail link");
				log(e.getErrorString() + "___" + e.getErrorCode());
				hideKeyboard();
				if (verify2FADialog != null && verify2FADialog.isShowing()) {
					verify2FADialog.dismiss();
				}
				Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
			}
		}
		else if(request.getType() == MegaRequest.TYPE_CONFIRM_CHANGE_EMAIL_LINK){
			log("CONFIRM_CHANGE_EMAIL_LINK: "+request.getEmail());
			if(e.getErrorCode() == MegaError.API_OK){
				log("Email changed");
				updateMyEmail(request.getEmail());
			}
			else if(e.getErrorCode() == MegaError.API_EEXIST){
				log("The new mail already exists");
				Util.showAlert(this, getString(R.string.mail_already_used), getString(R.string.general_error_word));
			}
			else if(e.getErrorCode() == MegaError.API_ENOENT){
				log("Email not changed -- API_ENOENT");
				Util.showAlert(this, "Email not changed!" + getString(R.string.old_password_provided_incorrect), getString(R.string.general_error_word));
			}
			else{
				log("Error when asking for change mail link");
				log(e.getErrorString() + "___" + e.getErrorCode());
				Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
			}
		}
		else if(request.getType() == MegaRequest.TYPE_QUERY_RECOVERY_LINK) {
			log("TYPE_GET_RECOVERY_LINK");
			if (e.getErrorCode() == MegaError.API_OK){
				String url = request.getLink();
				log("cancel account url");
				String myEmail = request.getEmail();
				if(myEmail!=null){
					if(myEmail.equals(megaApi.getMyEmail())){
						log("The email matchs!!!");
						showDialogInsertPassword(url, true);
					}
					else{
						log("Not logged with the correct account");
						log(e.getErrorString() + "___" + e.getErrorCode());
						Util.showAlert(this, getString(R.string.error_not_logged_with_correct_account), getString(R.string.general_error_word));
					}
				}
				else{
					log("My email is NULL in the request");
				}
			}
			else if(e.getErrorCode() == MegaError.API_EEXPIRED){
				log("Error expired link");
				log(e.getErrorString() + "___" + e.getErrorCode());
				Util.showAlert(this, getString(R.string.cancel_link_expired), getString(R.string.general_error_word));
			}
			else{
				log("Error when asking for recovery pass link");
				log(e.getErrorString() + "___" + e.getErrorCode());
				Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
			}
		}
		else if(request.getType() == MegaRequest.TYPE_GET_CANCEL_LINK){
            log("TYPE_GET_CANCEL_LINK");
			if (verify2faProgressBar != null) {
				verify2faProgressBar.setVisibility(View.GONE);
			}
			if (e.getErrorCode() == MegaError.API_OK){
				log("cancelation link received!");
				log(e.getErrorString() + "___" + e.getErrorCode());
				hideKeyboard();
				if (verify2FADialog != null && verify2FADialog.isShowing()) {
					verify2FADialog.dismiss();
				}
				Util.showAlert(this, getString(R.string.email_verification_text), getString(R.string.email_verification_title));
			}
			else if (e.getErrorCode() == MegaError.API_EFAILED || e.getErrorCode() == MegaError.API_EEXPIRED){
				if (is2FAEnabled()){
					verifyShowError();
				}
			}
			else{
				log("Error when asking for the cancelation link");
				log(e.getErrorString() + "___" + e.getErrorCode());
				hideKeyboard();
				if (verify2FADialog != null && verify2FADialog.isShowing()){
					verify2FADialog.dismiss();
				}
				Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
			}
        }
		else if(request.getType() == MegaRequest.TYPE_CONFIRM_CANCEL_LINK){
			if (e.getErrorCode() == MegaError.API_OK){
				log("ACCOUNT CANCELED");
			}
			else if (e.getErrorCode() == MegaError.API_ENOENT){
				log("Error cancelling account: API_ENOENT"+e.getErrorCode());
				log(e.getErrorString() + "___" + e.getErrorCode());
				Util.showAlert(this, getString(R.string.old_password_provided_incorrect), getString(R.string.general_error_word));
			}
			else{
				log("Error cancelling account: "+e.getErrorCode());
				log(e.getErrorString() + "___" + e.getErrorCode());
				Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE_CONTACT){

			if (e.getErrorCode() == MegaError.API_OK){
				Snackbar.make(fragmentContainer, getString(R.string.context_contact_removed), Snackbar.LENGTH_LONG).show();
			}
			else{
				log("Error deleting contact");
				Snackbar.make(fragmentContainer, getString(R.string.context_contact_not_removed), Snackbar.LENGTH_LONG).show();
			}
			updateContactsView(true, false, false);
		}
		else if (request.getType() == MegaRequest.TYPE_INVITE_CONTACT){
			log("MegaRequest.TYPE_INVITE_CONTACT finished: "+request.getNumber());

			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}


			if(request.getNumber()==MegaContactRequest.INVITE_ACTION_REMIND){
				showSnackbar(getString(R.string.context_contact_invitation_resent));
			}
			else{
				if (e.getErrorCode() == MegaError.API_OK){
					log("OK INVITE CONTACT: "+request.getEmail());
					if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD)
					{
						showSnackbar(getString(R.string.context_contact_request_sent, request.getEmail()));
					}
					else if(request.getNumber()==MegaContactRequest.INVITE_ACTION_DELETE)
					{
						showSnackbar(getString(R.string.context_contact_invitation_deleted));
					}
				}
				else{
					log("Code: "+e.getErrorString());
					if(e.getErrorCode()==MegaError.API_EEXIST)
					{
						showSnackbar(getString(R.string.context_contact_already_exists, request.getEmail()));
					}
					else if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD && e.getErrorCode()==MegaError.API_EARGS)
					{
						showSnackbar(getString(R.string.error_own_email_as_contact));
					}
					else{
						showSnackbar(getString(R.string.general_error));
					}
					log("ERROR: " + e.getErrorCode() + "___" + e.getErrorString());
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REPLY_CONTACT_REQUEST){
			log("MegaRequest.TYPE_REPLY_CONTACT_REQUEST finished: "+request.getType());

			if (e.getErrorCode() == MegaError.API_OK){

				if(request.getNumber()==MegaContactRequest.REPLY_ACTION_ACCEPT){
					log("I've accepted the invitation");
					showSnackbar(getString(R.string.context_invitacion_reply_accepted));
					MegaContactRequest contactRequest = megaApi.getContactRequestByHandle(request.getNodeHandle());
					log("Handle of the rquest: "+request.getNodeHandle());
					if(contactRequest!=null){
						log("Source: "+contactRequest.getSourceEmail());
						//Get the data of the user (avatar and name)
						MegaContactDB contactDB = dbH.findContactByEmail(contactRequest.getSourceEmail());
						if(contactDB==null){
							log("The contact: "+contactRequest.getSourceEmail()+" not found! Will be added to DB!");
							cC.addContactDB(contactRequest.getSourceEmail());
						}
						//Update view to get avatar
						String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
						cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
						if (cFLol != null && cFLol.isAdded()){
							cFLol.updateView();
						}
					}
					else{
						log("ContactRequest is NULL");
					}
				}
				else if(request.getNumber()==MegaContactRequest.REPLY_ACTION_DENY){
					showSnackbar(getString(R.string.context_invitacion_reply_declined));
				}
				else if(request.getNumber()==MegaContactRequest.REPLY_ACTION_IGNORE){
					showSnackbar(getString(R.string.context_invitacion_reply_ignored));
				}
			}
			else{
				showSnackbar(getString(R.string.general_error));
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}

			if (e.getErrorCode() == MegaError.API_OK){
//				Toast.makeText(this, getString(R.string.context_correctly_moved), Toast.LENGTH_LONG).show();

					if (moveToRubbish){
						log("moveToRubbish ");
						//Update both tabs
        				//Rubbish bin
						log("Move to Rubbish");
						refreshAfterMovingToRubbish();
						showSnackbar(getString(R.string.context_correctly_moved_to_rubbish));

						if (drawerItem == DrawerItem.INBOX){
							setInboxNavigationDrawer();
						}
						moveToRubbish = false;
					}
					else if(restoreFromRubbish){
						log("Not moved to rubbish");
						MegaNode destination = megaApi.getNodeByHandle(request.getParentHandle());
						showSnackbar(getString(R.string.context_correctly_node_restored, destination.getName()));
						restoreFromRubbish = false;
					}
					else{
						log("Not moved to rubbish");
						refreshAfterMoving();
						showSnackbar(getString(R.string.context_correctly_moved));
					}
			}
			else {
				if(restoreFromRubbish){
					showSnackbar(getString(R.string.context_no_restored));
					restoreFromRubbish = false;
				}
				else{
					showSnackbar(getString(R.string.context_no_moved));
					moveToRubbish = false;
				}
			}

			log("SINGLE move nodes request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_PAUSE_TRANSFERS){
			log("MegaRequest.TYPE_PAUSE_TRANSFERS");
			if (e.getErrorCode() == MegaError.API_OK) {

				if(cloudPageAdapter!=null){
					fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
					if(fbFLol!=null){
						if(fbFLol.isAdded()){
							fbFLol.updateTransferButton();
						}
					}
				}

				if(megaApi.areTransfersPaused(MegaTransfer.TYPE_DOWNLOAD)||megaApi.areTransfersPaused(MegaTransfer.TYPE_UPLOAD)){
					log("show PLAY button");

					String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
					tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
					if (tFLol != null){
						if (drawerItem == DrawerItem.TRANSFERS && tFLol.isAdded()) {
							pauseTransfersMenuIcon.setVisible(false);
							playTransfersMenuIcon.setVisible(true);
						}
					}
    			}
    			else{
    				log("show PAUSE button");
					String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
					tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
					if (tFLol != null){
						if (drawerItem == DrawerItem.TRANSFERS && tFLol.isAdded()) {
							pauseTransfersMenuIcon.setVisible(true);
							playTransfersMenuIcon.setVisible(false);
						}
					}
    			}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_PAUSE_TRANSFER) {
			log("one MegaRequest.TYPE_PAUSE_TRANSFER");

			if (e.getErrorCode() == MegaError.API_OK){
				int pendingTransfers = megaApi.getNumPendingDownloads() + megaApi.getNumPendingUploads();

				String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
				tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
				if (tFLol != null){
					if (tFLol.isAdded()){
						tFLol.changeStatusButton(request.getTransferTag());
					}
				}
			}
			else{
				showSnackbar(getString(R.string.error_general_nodes));
			}
		}
		else if(request.getType() == MegaRequest.TYPE_CANCEL_TRANSFERS){
			log("MegaRequest.TYPE_CANCEL_TRANSFERS");
			//After cancelling all the transfers
			if (e.getErrorCode() == MegaError.API_OK){
				if(cloudPageAdapter!=null){
					fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
					if (fbFLol != null){
						if(fbFLol.isAdded()){
							fbFLol.setOverviewLayout();
						}
					}
				}

				String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
				tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
				if (tFLol != null){
					if (drawerItem == DrawerItem.TRANSFERS && tFLol.isAdded()){
						pauseTransfersMenuIcon.setVisible(false);
						playTransfersMenuIcon.setVisible(false);
						cancelAllTransfersMenuItem.setVisible(false);
					}
				}
			}
			else{
				showSnackbar(getString(R.string.error_general_nodes));
			}

		}
		else if (request.getType() == MegaRequest.TYPE_CANCEL_TRANSFER){
			log("one MegaRequest.TYPE_CANCEL_TRANSFER");

			if (e.getErrorCode() == MegaError.API_OK){

				log("REQUEST OK - wait for onTransferFinish()");
				if(cloudPageAdapter!=null){
					fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
					if (fbFLol != null){
						if(fbFLol.isAdded()){
							fbFLol.setOverviewLayout();
						}
					}
				}
				supportInvalidateOptionsMenu();
			}
			else{
				showSnackbar(getString(R.string.error_general_nodes));
			}

		}
		else if (request.getType() == MegaRequest.TYPE_KILL_SESSION){
			log("requestFinish TYPE_KILL_SESSION"+MegaRequest.TYPE_KILL_SESSION);
			if (e.getErrorCode() == MegaError.API_OK){
				log("success kill sessions");
				showSnackbar(getString(R.string.success_kill_all_sessions));
			}
			else
			{
				log("error when killing sessions: "+e.getErrorString());
				showSnackbar(getString(R.string.error_kill_all_sessions));
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
					}
				}
				refreshAfterRemoving();
				showSnackbar(getString(R.string.context_correctly_removed));
			}
			else{
				showSnackbar(getString(R.string.context_no_removed));
			}
			log("remove request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_RENAME){

			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}

			if (e.getErrorCode() == MegaError.API_OK){
				showSnackbar(getString(R.string.context_correctly_renamed));
				if (drawerItem == DrawerItem.CLOUD_DRIVE){

					int index = viewPagerCDrive.getCurrentItem();
        			log("----------------------------------------INDEX: "+index);
        			if(index==0){
        		        //Cloud Drive
        				fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
        				if (fbFLol != null && fbFLol.isAdded()){
							ArrayList<MegaNode> nodes;
							if(parentHandleBrowser==-1){
								nodes = megaApi.getChildren(megaApi.getRootNode(), orderCloud);
							}
							else{
								nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderCloud);
							}
    						fbFLol.setNodes(nodes);
    						fbFLol.getRecyclerView().invalidate();
    					}
        			}
        			else if(index==1){
						//Cloud Drive
						rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
						if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
							ArrayList<MegaNode> nodes;
							if(parentHandleRubbish==-1){
								nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderCloud);
							}
							else{
								nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderCloud);
							}
							rubbishBinFLol.setNodes(nodes);
							rubbishBinFLol.getRecyclerView().invalidate();
						}
					}
				}
				else if (drawerItem == DrawerItem.INBOX){

					if (iFLol != null && iFLol.isAdded()){
						iFLol.getRecyclerView().invalidate();
					}
				}
				else if (drawerItem == DrawerItem.SAVED_FOR_OFFLINE){

					if (oFLol != null && oFLol.isAdded()){
						oFLol.getRecyclerView().invalidate();
					}
				}
				else if (drawerItem == DrawerItem.SHARED_ITEMS){
    				inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
					if (inSFLol != null && inSFLol.isAdded()){
						inSFLol.getRecyclerView().invalidate();
					}
	        		outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
					if (outSFLol != null && outSFLol.isAdded()){
						outSFLol.getRecyclerView().invalidate();
					}
				}
			}
			else{
				showSnackbar(getString(R.string.context_no_renamed));
			}
		}
		else if (request.getType() == MegaRequest.TYPE_COPY){
			log("TYPE_COPY");

			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}

			if (e.getErrorCode() == MegaError.API_OK){
				if (sendToChat && megaApi.getNodeByHandle(request.getParentHandle()).getName().equals(Constants.CHAT_FOLDER)
						&& drawerItem == DrawerItem.SHARED_ITEMS && getTabItemShares() == 0) {
					log("Incoming node copied to Send to chat");
					MegaNode attachNode = megaApi.getNodeByHandle(request.getNodeHandle());
					if (attachNode != null) {
						nC.selectChatsToSendNode(attachNode);
					}
				}
				else {
					log("Show snackbar!!!!!!!!!!!!!!!!!!!");
					showSnackbar(getString(R.string.context_correctly_copied));

					if (drawerItem == DrawerItem.CLOUD_DRIVE){

						int index = viewPagerCDrive.getCurrentItem();
						log("----------------------------------------INDEX: "+index);
						if(index==1){
							//Rubbish bin
							rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
							if (rubbishBinFLol != null && rubbishBinFLol.isAdded()){
								ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderCloud);
								rubbishBinFLol.setNodes(nodes);
								rubbishBinFLol.getRecyclerView().invalidate();
							}
						}
						else{
							//Cloud Drive
							fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
							if (fbFLol != null && fbFLol.isAdded()){
								ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderCloud);
								fbFLol.setNodes(nodes);
								fbFLol.getRecyclerView().invalidate();
							}
						}
					}
					else if (drawerItem == DrawerItem.INBOX){
						if (iFLol != null && iFLol.isAdded()){
							iFLol.getRecyclerView().invalidate();
						}
					}
				}
			}
			else{
				if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
					log("OVERQUOTA ERROR: "+e.getErrorCode());
					showOverquotaAlert(false);
				}
				else if(e.getErrorCode()==MegaError.API_EGOINGOVERQUOTA){
					log("OVERQUOTA ERROR: "+e.getErrorCode());
					showOverquotaAlert(true);
				}
				else
				{
					showSnackbar(getString(R.string.context_no_copied));
				}
			}
			sendToChat = false;
		}
		else if (request.getType() == MegaRequest.TYPE_CREATE_FOLDER){
			try {
				statusDialog.dismiss();
			}
			catch (Exception ex) {}

			if (e.getErrorCode() == MegaError.API_OK){
				showSnackbar(getString(R.string.context_folder_created));
				if (drawerItem == DrawerItem.CLOUD_DRIVE){
					if (fbFLol != null && fbFLol.isAdded()){
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderCloud);
						fbFLol.setNodes(nodes);
						fbFLol.getRecyclerView().invalidate();
					}
				}
				else if (drawerItem == DrawerItem.SHARED_ITEMS){
					inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
					if (inSFLol != null){
						if(inSFLol.isAdded()) {
							inSFLol.refresh();
						}
					}
					outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
					if (outSFLol != null){
						if(outSFLol.isAdded()) {
							outSFLol.refreshContent();
						}
					}
				}
			}
			else{
				log("TYPE_CREATE_FOLDER ERROR: "+e.getErrorCode()+" "+e.getErrorString());
				showSnackbar(getString(R.string.context_folder_no_created));
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
				if(request.getAccess()==MegaShare.ACCESS_UNKNOWN){
					showSnackbar(getString(R.string.context_remove_sharing));
				}
				else{
					showSnackbar(getString(R.string.context_correctly_shared));
				}
			}
			else{
//				log("ERROR MegaRequest.TYPE_SHARE: "+request.getEmail()+" : "+request.getName());
				if(request.getAccess()==MegaShare.ACCESS_UNKNOWN){
					showSnackbar(getString(R.string.context_no_removed_shared));
				}
				else{
					showSnackbar(getString(R.string.context_no_shared));
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_SUBMIT_PURCHASE_RECEIPT){
			if (e.getErrorCode() == MegaError.API_OK){
				log("PURCHASE CORRECT!");
				drawerItem = DrawerItem.CLOUD_DRIVE;
				selectDrawerItemLollipop(drawerItem);
			}
			else{
				log("PURCHASE WRONG: " + e.getErrorString() + " (" + e.getErrorCode() + ")");
//				Snackbar.make(fragmentContainer, "PURCHASE WRONG: " + e.getErrorString() + " (" + e.getErrorCode() + ")", Snackbar.LENGTH_LONG).show();
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CLEAN_RUBBISH_BIN){
			if (e.getErrorCode() == MegaError.API_OK){
				log("OK MegaRequest.TYPE_CLEAN_RUBBISH_BIN");
				showSnackbar(getString(R.string.rubbish_bin_emptied));
				//Get storage figures again
				((MegaApplication) getApplication()).askForAccountDetails();
			}
			else{
				showSnackbar(getString(R.string.rubbish_bin_no_emptied));
			}
		}
		else if(request.getType() == MegaRequest.TYPE_REMOVE_VERSIONS){
			if (e.getErrorCode() == MegaError.API_OK){
				log("OK MegaRequest.TYPE_REMOVE_VERSIONS");
				showSnackbar(getString(R.string.success_delete_versions));

				if(sttFLol!=null && sttFLol.isAdded()) {
					sttFLol.resetVersionsInfo();
				}
				//Get info of the version again (after 10 seconds)
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						updateAccountStorageInfo();
					}
				}, 8000);
			}
			else{
				showSnackbar(getString(R.string.error_delete_versions));
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REGISTER_PUSH_NOTIFICATION){
			if (e.getErrorCode() == MegaError.API_OK){
				log("FCM OK TOKEN MegaRequest.TYPE_REGISTER_PUSH_NOTIFICATION");
			}
			else{
				log("FCM ERROR TOKEN TYPE_REGISTER_PUSH_NOTIFICATION: " + e.getErrorCode() + "__" + e.getErrorString());
			}
		}
		else if (request.getType() == MegaRequest.TYPE_EXPORT) {
			if (e.getErrorCode() == MegaError.API_ENOENT) {
				log("Removing link error");
				showSnackbar(getString(R.string.context_link_removal_error));
			}
			else if (e.getErrorCode() != MegaError.API_OK) {
				showSnackbar(getString(R.string.context_link_action_error));
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MULTI_FACTOR_AUTH_CHECK) {
			if (e.getErrorCode() == MegaError.API_OK) {
				if (request.getFlag()) {
					is2FAEnabled = true;
				} else {
					is2FAEnabled = false;
				}
				if (sttFLol != null && sttFLol.isAdded()) {
					sttFLol.update2FAPreference(is2FAEnabled);
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MULTI_FACTOR_AUTH_SET){
			log("TYPE_MULTI_FACTOR_AUTH_SET: "+e.getErrorCode());
			if (verify2faProgressBar != null) {
				verify2faProgressBar.setVisibility(View.GONE);
			}
			if (!request.getFlag() && e.getErrorCode() == MegaError.API_OK){
				log("Pin correct: Two-Factor Authentication disabled");
				is2FAEnabled = false;
				if (sttFLol != null && sttFLol.isAdded()) {
					sttFLol.update2FAPreference(false);
					showSnackbar(getString(R.string.label_2fa_disabled));
				}
				hideKeyboard();
				if (verify2FADialog != null) {
					verify2FADialog.dismiss();
				}
			}
			else if (e.getErrorCode() == MegaError.API_EFAILED){
				log("Pin not correct");
				verifyShowError();
			}
			else {
				hideKeyboard();
				if (verify2FADialog != null) {
					verify2FADialog.dismiss();
				}
				showSnackbar(getString(R.string.error_disable_2fa));
				log("An error ocurred trying to disable Two-Factor Authentication");
			}

			megaApi.multiFactorAuthCheck(megaApi.getMyEmail(), this);
		}
		else if(request.getType() == MegaRequest.TYPE_FOLDER_INFO) {
			if (e.getErrorCode() == MegaError.API_OK) {
				MegaFolderInfo info = request.getMegaFolderInfo();
				int numVersions = info.getNumVersions();
				log("Num versions: " + numVersions);
				long previousVersions = info.getVersionsSize();
				log("Previous versions: " + previousVersions);

				if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
					((MegaApplication) getApplication()).getMyAccountInfo().setNumVersions(numVersions);
					((MegaApplication) getApplication()).getMyAccountInfo().setPreviousVersionsSize(previousVersions);
				}

			} else {
				log("ERROR requesting version info of the account");
			}

			//Refresh My Storage if it is shown
			String myStorageTag = getFragmentTag(R.id.my_account_tabs_pager, 1);
			mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myStorageTag);
			if(mStorageFLol!=null && mStorageFLol.isAdded()){
				mStorageFLol.refreshVersionsInfo();
			}

			//Refresh Settings if it is shown
			if(sttFLol!=null && sttFLol.isAdded()) {
				sttFLol.setVersionsInfo();
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CONTACT_LINK_CREATE) {
			String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
			maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
			if (maFLol != null && maFLol.isAdded()) {
				maFLol.initCreateQR(request, e);
			}
		}
	}

	public void updateAccountStorageInfo(){
		log("updateAccountStorageInfo");
		megaApi.getFolderInfo(megaApi.getRootNode(), this);
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError: " + request.getRequestString() + "__" + e.getErrorCode() + "__" + e.getErrorString());
	}

	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		log("onUsersUpdateLollipop-----------------------------------------------");

		if (users != null){
			log("users.size(): "+users.size());
			for(int i=0; i<users.size();i++){
				MegaUser user=users.get(i);

				if(user!=null){
					// 0 if the change is external.
					// >0 if the change is the result of an explicit request
					// -1 if the change is the result of an implicit request made by the SDK internally

					if(user.isOwnChange()>0){
						log("isOwnChange!!!: "+user.getEmail());
						if (user.hasChanged(MegaUser.CHANGE_TYPE_RICH_PREVIEWS)){
							log("Change on CHANGE_TYPE_RICH_PREVIEWS");
							if(Util.isChatEnabled()){
								megaApi.shouldShowRichLinkWarning(this);
								megaApi.isRichPreviewsEnabled(this);
							}
						}
					}
					else{
						log("NOT OWN change: "+user.getEmail());

						log("Changes: "+user.getChanges());

						if(megaApi.getMyUser()!=null) {
							if (user.getHandle() == megaApi.getMyUser().getHandle()) {
								log("Change on my account from another client");
								if (user.hasChanged(MegaUser.CHANGE_TYPE_DISABLE_VERSIONS)) {
									log("Change on CHANGE_TYPE_DISABLE_VERSIONS");
									megaApi.getFileVersionsOption(this);
								} else if (user.hasChanged(MegaUser.CHANGE_TYPE_CONTACT_LINK_VERIFICATION)) {
									log("Change on CHANGE_TYPE_CONTACT_LINK_VERIFICATION");
									megaApi.getContactLinksOption(this);
								}
							}
						}

						if (user.hasChanged(MegaUser.CHANGE_TYPE_FIRSTNAME)){
							log("The user: "+user.getEmail()+"changed his first name");
							if(user.getEmail().equals(megaApi.getMyUser().getEmail())){
								log("I change my first name");
								if(((MegaApplication) getApplication()).getMyAccountInfo()!=null)
								{
									((MegaApplication) getApplication()).getMyAccountInfo().setFirstName(false);
								}
								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_FIRSTNAME, this);
							}
							else{
								if(((MegaApplication) getApplication()).getMyAccountInfo()!=null)
								{
									((MegaApplication) getApplication()).getMyAccountInfo().setFirstName(false);
								}

								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_FIRSTNAME, new ContactNameListener(this));
							}
						}
						else if (user.hasChanged(MegaUser.CHANGE_TYPE_LASTNAME)){
							log("The user: "+user.getEmail()+"changed his last name");
							if(user.getEmail().equals(megaApi.getMyUser().getEmail())){
								log("I change my last name");
								if(((MegaApplication) getApplication()).getMyAccountInfo()!=null) {
									((MegaApplication) getApplication()).getMyAccountInfo().setLastName(false);
								}
								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_LASTNAME, this);
							}
							else{
								if(((MegaApplication) getApplication()).getMyAccountInfo()!=null) {
									((MegaApplication) getApplication()).getMyAccountInfo().setLastName(false);
								}
								megaApi.getUserAttribute(user, MegaApiJava.USER_ATTR_LASTNAME, new ContactNameListener(this));
							}
						}
						else if (user.hasChanged(MegaUser.CHANGE_TYPE_AVATAR)){
							log("The user: "+user.getEmail()+"changed his AVATAR");

							File avatar = null;
							if (this.getExternalCacheDir() != null){
								avatar = new File(this.getExternalCacheDir().getAbsolutePath(), user.getEmail() + ".jpg");
							}
							else{
								avatar = new File(this.getCacheDir().getAbsolutePath(), user.getEmail() + ".jpg");
							}
							Bitmap bitmap = null;
							if (avatar.exists()){
								avatar.delete();
							}

							if(user.getEmail().equals(megaApi.getMyUser().getEmail())){
								log("I change my avatar");
								if (getExternalCacheDir() != null){
									String destinationPath = null;
									destinationPath = getExternalCacheDir().getAbsolutePath() + "/" + megaApi.getMyEmail() + ".jpg";
									if(destinationPath!=null){
										log("The destination of the avatar is: "+destinationPath);
										megaApi.getUserAvatar(megaApi.getMyUser(), destinationPath, this);
									}
									else{
										log("ERROR! Destination PATH is NULL");
									}
								}
								else{
									log("getExternalCacheDir() is NULL");
									megaApi.getUserAvatar(megaApi.getMyUser(), getCacheDir().getAbsolutePath() + "/" + megaApi.getMyEmail() + ".jpg", this);
								}
							}
							else {
								log("Update de ContactsFragment");
								String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
								cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
								if (cFLol != null && cFLol.isAdded()) {
									if (drawerItem == DrawerItem.CONTACTS) {
										cFLol.updateView();
									}
								}
							}
						}
						else if (user.hasChanged(MegaUser.CHANGE_TYPE_EMAIL)){
							log("CHANGE_TYPE_EMAIL");
							if(user.getEmail().equals(megaApi.getMyUser().getEmail())){
								log("I change my mail");
								updateMyEmail(user.getEmail());
							}
							else{
								log("The contact: "+user.getHandle()+" changes the mail: "+user.getEmail());
								if(dbH.findContactByHandle(String.valueOf(user.getHandle()))==null){
									log("The contact NOT exists -> DB inconsistency! -> Clear!");
									if (dbH.getContactsSize() != megaApi.getContacts().size()){
										dbH.clearContacts();
										FillDBContactsTask fillDBContactsTask = new FillDBContactsTask(this);
										fillDBContactsTask.execute();
									}
								}
								else{
									log("The contact already exists -> update");
									dbH.setContactMail(user.getHandle(),user.getEmail());
								}
							}
						}

						if(cFLol!=null){
							if(cFLol.isAdded()){
								updateContactsView(true, false, false);
							}
						}
						//When last contact changes avatar, update view.
						if(maFLol != null) {
						    if(maFLol.isAdded()) {
								maFLol.updateContactsCount();
                                maFLol.updateView();
                            }
                        }
					}
				}
				else{
					log("Continue...");
					continue;
				}
			}


		}
	}

	@Override
	public void onEvent(MegaApiJava api, MegaEvent event) {

	}

	public void updateMyEmail(String email){
		log("updateMyEmail:newEmail: "+email);
		nVEmail.setText(email);
		String oldEmail = dbH.getMyEmail();
		if(oldEmail!=null){
			log("updateMyEmail:oldEmail: "+oldEmail);

			try{
				File avatarFile = null;
				if (getExternalCacheDir() != null){
					avatarFile = new File(getExternalCacheDir().getAbsolutePath(), oldEmail + ".jpg");
				}
				else{
					avatarFile = new File(getCacheDir().getAbsolutePath(), oldEmail + ".jpg");
				}

				if(avatarFile!=null){
					if(avatarFile.exists()){
						String newPath = null;
						if (getExternalCacheDir() != null){
							newPath = getExternalCacheDir().getAbsolutePath() + "/" + email + ".jpg";
						}
						else{
							log("getExternalCacheDir() is NULL");
							newPath = getCacheDir().getAbsolutePath() + "/" + email + ".jpg";
						}
						File newFile = new File(newPath);
						boolean result = avatarFile.renameTo(newFile);
						if(result){
							log("The avatar file was correctly renamed");
						}
					}
				}
			}
			catch(Exception e){
				log("EXCEPTION renaming the avatar on changing email");
			}
		}
		else{
			log("ERROR. Old email is NULL");
		}

		dbH.saveMyEmail(email);

		String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
		maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
		if(maFLol!=null){
			if(maFLol.isAdded()){
				maFLol.updateMailView(email);
			}
		}
	}

	public long[] getTypeOfSearch(){
		return  searchDate;
	}

	public boolean getIsSearchEnabled(){
		return  isSearchEnabled;
	}
	public void setIsSearchEnabled(boolean isSearchEnabled){
		this.isSearchEnabled = isSearchEnabled;
	}

	public void onNodesCloudDriveUpdate() {
		log("onNodesCloudDriveUpdate");

		if(cloudPageAdapter!=null){
			//Rubbish bin
			rubbishBinFLol = (RubbishBinFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 1);
			if (rubbishBinFLol != null){
				if(rubbishBinFLol.isAdded()){
					rubbishBinFLol.hideMultipleSelect();

					if (isClearRubbishBin){
						isClearRubbishBin = false;
						parentHandleRubbish = megaApi.getRubbishNode().getHandle();
						ArrayList<MegaNode> nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderCloud);
						rubbishBinFLol.setNodes(nodes);
						rubbishBinFLol.getRecyclerView().invalidate();
					}
					else{
						ArrayList<MegaNode> nodes;
						if(parentHandleRubbish==-1){
							nodes = megaApi.getChildren(megaApi.getRubbishNode(), orderCloud);
						}
						else{
							nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleRubbish), orderCloud);
						}
						rubbishBinFLol.setNodes(nodes);
						rubbishBinFLol.getRecyclerView().invalidate();
					}
				}
			}

			fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
			if (fbFLol != null){
				if(fbFLol.isAdded()){
					ArrayList<MegaNode> nodes;
					if(parentHandleBrowser==-1){
						nodes = megaApi.getChildren(megaApi.getNodeByHandle(megaApi.getRootNode().getHandle()), orderCloud);
					}
					else{
						nodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandleBrowser), orderCloud);
					}
					log("nodes: "+nodes.size());
					fbFLol.hideMultipleSelect();
					fbFLol.setNodes(nodes);
					fbFLol.getRecyclerView().invalidate();
				}
			}
			else{
				log("FileBrowser is NULL after move");
			}
		}
	}

	public void onNodesInboxUpdate() {

		if (iFLol != null){
			if(iFLol.isAdded()){
				iFLol.refresh();
			}
		}
	}

	public void onNodesSearchUpdate() {
		log("onNodesSearchUpdate");
		if (sFLol != null){
			if(sFLol.isAdded()){
				sFLol.refresh();
			}
		}
	}

	public void onNodesSharedUpdate() {
		log("onNodesSharedUpdate");

		if(sharesPageAdapter!=null){
			outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
			if (outSFLol != null){
				if(outSFLol.isAdded()){
					outSFLol.refresh();
				}
			}

			inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
			if (inSFLol != null){
				if(inSFLol.isAdded()){
					inSFLol.refresh();
				}
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

		boolean updateContacts = false;

		if(updatedNodes!=null){
			//Verify is it is a new item to the inbox
			for(int i=0;i<updatedNodes.size(); i++){
				MegaNode updatedNode = updatedNodes.get(i);

				if(!updateContacts){
					if(updatedNode.isInShare()){
						updateContacts = true;
					}
				}

				if(updatedNode.getParentHandle()==inboxNode.getHandle()){
					log("New element to Inbox!!");
					setInboxNavigationDrawer();
				}
			}
		}

		if(updateContacts){
			String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
			cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
			if (cFLol != null){
				if(cFLol.isAdded()){
					log("Incoming update - update contacts section");
					cFLol.updateShares();
				}
			}
		}

		onNodesCloudDriveUpdate();

//		if (sFLol != null && sFLol.isAdded()){
//			sFLol.refresh();
//		}

		onNodesSearchUpdate();

		onNodesSharedUpdate();

		onNodesInboxUpdate();


		if (cuFL != null){
			if(cuFL.isAdded()){
				long cameraUploadHandle = cuFL.getPhotoSyncHandle();
				MegaNode nps = megaApi.getNodeByHandle(cameraUploadHandle);
				log("cameraUploadHandle: " + cameraUploadHandle);
				if (nps != null){
					log("nps != null");
					ArrayList<MegaNode> nodes = megaApi.getChildren(nps, MegaApiJava.ORDER_MODIFICATION_DESC);

					if(firstNavigationLevel){
						cuFL.setNodes(nodes);
					}else{
					    if(getIsSearchEnabled()){
                            if((searchByDate != null)&&(searchDate !=null)){
                                ArrayList<MegaNode> nodesSearch = cuFL.searchDate(searchDate, nodes);
                                cuFL.setNodes(nodesSearch);
                                isSearchEnabled = true;
                            }else{
                                cuFL.setNodes(nodes);

                            }
                        }else{
                            cuFL.setNodes(nodes);

                        }


					}
				}
			}
		}

		if (muFLol != null){
			if(muFLol.isAdded()){
				long cameraUploadHandle = muFLol.getPhotoSyncHandle();
				MegaNode nps = megaApi.getNodeByHandle(cameraUploadHandle);
				log("mediaUploadsHandle: " + cameraUploadHandle);
				if (nps != null){
					log("nps != null");
					ArrayList<MegaNode> nodes = megaApi.getChildren(nps, MegaApiJava.ORDER_MODIFICATION_DESC);
					if(firstNavigationLevel){
						muFLol.setNodes(nodes);
					}else{
                        if(getIsSearchEnabled()){
                            if((searchByDate != null)&&(searchDate !=null)){
                                ArrayList<MegaNode> nodesSearch = muFLol.searchDate(searchDate, nodes);
                                muFLol.setNodes(nodesSearch);
                                isSearchEnabled = true;
                            }else{
                                muFLol.setNodes(nodes);
                            }
                        }else{
                            muFLol.setNodes(nodes);

                        }

					}
				}
			}
		}

		setToolbarTitle();
		supportInvalidateOptionsMenu();
	}

	@Override
	public void onReloadNeeded(MegaApiJava api) {
		log("onReloadNeeded");
	}

	@Override
	public void onAccountUpdate(MegaApiJava api) {
		log("onAccountUpdate");
	}

	@Override
	public void onContactRequestsUpdate(MegaApiJava api,ArrayList<MegaContactRequest> requests) {
		log("---------------------onContactRequestsUpdate");

		if(requests!=null){
			for(int i=0; i<requests.size();i++){
				MegaContactRequest req = requests.get(i);
				if(req.isOutgoing()){
					log("SENT REQUEST");
					log("STATUS: "+req.getStatus()+" targetEmail: "+req.getTargetEmail()+" contactHandle: "+req.getHandle());
					if(req.getStatus()==MegaContactRequest.STATUS_ACCEPTED){
						cC.addContactDB(req.getTargetEmail());
					}
					updateContactsView(true, true, false);
				}
				else{
					log("RECEIVED REQUEST");
					setContactTitleSection();
					log("STATUS: "+req.getStatus()+" sourceEmail: "+req.getSourceEmail()+" contactHandle: "+req.getHandle());
					if(req.getStatus()==MegaContactRequest.STATUS_ACCEPTED){
						cC.addContactDB(req.getSourceEmail());
					}
					updateContactsView(true, false, true);
				}
			}
		}
	}

	////TRANSFERS/////

	public void changeTransfersStatus(){
		log("changeTransfersStatus");
		if(megaApi.areTransfersPaused(MegaTransfer.TYPE_DOWNLOAD)||megaApi.areTransfersPaused(MegaTransfer.TYPE_UPLOAD)){
			log("show PLAY button");
			megaApi.pauseTransfers(false, this);
		}
		else{
			log("Transfers are play -> pause");
			megaApi.pauseTransfers(true, this);
		}
	}

	public void pauseIndividualTransfer(MegaTransfer mT){
		log("pauseIndividualTransfer");
		if(mT.getState()==MegaTransfer.STATE_PAUSED){
			megaApi.pauseTransfer(mT, false, managerActivity);
		}
		else{
			megaApi.pauseTransfer(mT, true, managerActivity);
		}
	}

	public void showConfirmationClearCompletedTransfers (){
		log("showConfirmationClearCompletedTransfers");

		//Show confirmation message
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE: {
						log("Pressed button positive to clear transfers");
						dbH.emptyCompletedTransfers();
						String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 1);
						completedTFLol = (CompletedTransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
						if (completedTFLol != null) {
							if (completedTFLol.isAdded()) {
								completedTFLol.updateCompletedTransfers();
							}
						}
						supportInvalidateOptionsMenu();
						break;
					}
					case DialogInterface.BUTTON_NEGATIVE: {
						break;
					}
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getResources().getString(R.string.cancel_transfer_title));

		builder.setMessage(getResources().getString(R.string.confirmation_to_clear_completed_transfers));
		builder.setPositiveButton(R.string.general_clear, dialogClickListener);
		builder.setNegativeButton(R.string.general_cancel, dialogClickListener);

		builder.show();
	}

	public void showConfirmationCancelTransfer (MegaTransfer t, boolean cancelValue){
		log("showConfirmationCancelTransfer");
		final MegaTransfer mT = t;
		final boolean cancel = cancelValue;

		//Show confirmation message
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						log("Pressed button positive to cancel transfer");
						if(cancel){
							megaApi.cancelTransfer(mT, managerActivity);
						}
						else{
							pauseIndividualTransfer(mT);
						}

						break;

					case DialogInterface.BUTTON_NEGATIVE:
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getResources().getString(R.string.cancel_transfer_title));
		if(cancel){

			builder.setMessage(getResources().getString(R.string.cancel_transfer_confirmation));
			builder.setPositiveButton(R.string.context_delete, dialogClickListener);
			builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
		}
		else {

			if(mT.getState()==MegaTransfer.STATE_PAUSED){
				builder.setMessage(getResources().getString(R.string.menu_resume_individual_transfer));
				builder.setPositiveButton(R.string.button_resume_individual_transfer, dialogClickListener);
				builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
			}
			else{
				builder.setMessage(getResources().getString(R.string.menu_pause_individual_transfer));
				builder.setPositiveButton(R.string.action_pause, dialogClickListener);
				builder.setNegativeButton(R.string.general_cancel, dialogClickListener);
			}

		}
		builder.show();
	}

	public void showConfirmationCancelAllTransfers (){
		log("showConfirmationCancelAllTransfers");

		//Show confirmation message
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						log("Pressed button positive to cancel transfer");
						megaApi.cancelTransfers(MegaTransfer.TYPE_DOWNLOAD);
						megaApi.cancelTransfers(MegaTransfer.TYPE_UPLOAD);

						break;

					case DialogInterface.BUTTON_NEGATIVE:
						break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getResources().getString(R.string.cancel_transfer_title));

		builder.setMessage(getResources().getString(R.string.cancel_all_transfer_confirmation));
		builder.setPositiveButton(R.string.context_delete, dialogClickListener);
		builder.setNegativeButton(R.string.general_cancel, dialogClickListener);

		builder.show();
	}

	public void addCompletedTransfer(MegaTransfer transfer){
		log("addCompletedTransfer: "+transfer.getFileName());

		String size = Util.getSizeString(transfer.getTotalBytes());
		AndroidCompletedTransfer completedTransfer = new AndroidCompletedTransfer(transfer.getFileName(), transfer.getType(), transfer.getState(), size, transfer.getNodeHandle()+"");

		String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 1);
		completedTFLol = (CompletedTransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
		if(completedTFLol!=null){
			if(completedTFLol.isAdded()){
				completedTFLol.transferFinish(completedTransfer);
			}
		}
	}

	@Override
	public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {
		log("-------------------onTransferStart: " + transfer.getNotificationNumber()+ "-" + transfer.getFileName() + " - " + transfer.getTag());

		if(transfer.isStreamingTransfer()){
			return;
		}

		if(transferCallback<transfer.getNotificationNumber()) {

			transferCallback = transfer.getNotificationNumber();

			long now = Calendar.getInstance().getTimeInMillis();
			lastTimeOnTransferUpdate = now;

			if(!transfer.isFolderTransfer()){
				transfersInProgress.add(transfer.getTag());

				if(cloudPageAdapter!=null){
					fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
					if (fbFLol != null){
						if(fbFLol.isAdded()){
							fbFLol.setOverviewLayout();
						}
					}
				}

				String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
				tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
				if (tFLol != null){
					if(tFLol.isAdded()){
						tFLol.transferStart(transfer);
					}
				}
			}
		}
	}

	@Override
	public void onTransferFinish(MegaApiJava api, MegaTransfer transfer, MegaError e) {
		log("--------------onTransferFinish: "+transfer.getFileName() + " - " + transfer.getTag() + "- " +transfer.getNotificationNumber());

		if(transfer.isStreamingTransfer()){
			return;
		}

		if(transferCallback<transfer.getNotificationNumber()) {

			transferCallback = transfer.getNotificationNumber();
			long now = Calendar.getInstance().getTimeInMillis();
			lastTimeOnTransferUpdate = now;

			if(!transfer.isFolderTransfer()){
				ListIterator li = transfersInProgress.listIterator();
				int index = 0;
				while(li.hasNext()) {
					Integer next = (Integer) li.next();
					if(next == transfer.getTag()){
						index=li.previousIndex();
						break;
					}
				}

				if(!transfersInProgress.isEmpty()){
					transfersInProgress.remove(index);
					log("The transfer with index : "+index +"has been removed, left: "+transfersInProgress.size());
				}
				else{
					log("The transferInProgress is EMPTY");
				}

				if(transfer.getState()==MegaTransfer.STATE_COMPLETED){
					addCompletedTransfer(transfer);
				}

				int pendingTransfers = 	megaApi.getNumPendingDownloads() + megaApi.getNumPendingUploads();

				if(pendingTransfers<=0){
					if(transfersBottomSheet!=null){
						if(transfersBottomSheet.isAdded()){
							transfersBottomSheet.dismiss();
						}
					}
					if (pauseTransfersMenuIcon != null) {
						pauseTransfersMenuIcon.setVisible(false);
						playTransfersMenuIcon.setVisible(false);
						cancelAllTransfersMenuItem.setVisible(false);
					}

//					showSnackbar(getString(R.string.message_transfers_completed));
				}

				if(cloudPageAdapter!=null){
					fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
					if (fbFLol != null){
						if(fbFLol.isAdded()){
							fbFLol.setOverviewLayout();
						}
					}
				}

				String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
				tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
				if (tFLol != null){
					if(tFLol.isAdded()){
						tFLol.transferFinish(index);
					}
				}
				else{
					log("tF is null!");
				}
			}
		}
	}

	@Override
	public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {
//		log("onTransferUpdate: " + transfer.getFileName() + " - " + transfer.getTag());

		if(transfer.isStreamingTransfer()){
			return;
		}

		long now = Calendar.getInstance().getTimeInMillis();
		if((now - lastTimeOnTransferUpdate)>Util.ONTRANSFERUPDATE_REFRESH_MILLIS){
			log("Update onTransferUpdate: " + transfer.getFileName() + " - " + transfer.getTag()+ " - "+ transfer.getNotificationNumber());
			lastTimeOnTransferUpdate = now;

			if (!transfer.isFolderTransfer()){
				if(transferCallback<transfer.getNotificationNumber()){
					transferCallback = transfer.getNotificationNumber();

					if(cloudPageAdapter!=null){
						fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
						if (fbFLol != null){
							if(fbFLol.isAdded()){
								fbFLol.setOverviewLayout();
							}
						}
					}

					String tFTag = getFragmentTag(R.id.transfers_tabs_pager, 0);
					tFLol = (TransfersFragmentLollipop) getSupportFragmentManager().findFragmentByTag(tFTag);
					if (tFLol != null){
						if(tFLol.isAdded()){
							tFLol.transferUpdate(transfer);
						}
					}

				}
			}
		}
	}

	@Override
	public void onTransferTemporaryError(MegaApiJava api, MegaTransfer transfer, MegaError e) {
		log("onTransferTemporaryError: " + transfer.getFileName() + " - " + transfer.getTag());

		if(e.getErrorCode() == MegaError.API_EOVERQUOTA){
			log("API_EOVERQUOTA error!!");
			if(cloudPageAdapter!=null){
				fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
				if (fbFLol != null){
					if(fbFLol.isAdded()){
						fbFLol.setOverviewLayout();
					}
				}
			}
		}
	}

	@Override
	public boolean onTransferData(MegaApiJava api, MegaTransfer transfer, byte[] buffer) {
		log("onTransferData");

//		if(Util.isVideoFile(transfer.getPath())){
//		log("Is video!!!");
//		ThumbnailUtilsLollipop.createThumbnailVideo(this, transfer.getPath(), megaApi, transfer.getNodeHandle());
//	}
//	else{
//		log("NOT video!");
//	}

		return true;
	}

	public static void log(String message) {
		Util.log("ManagerActivityLollipop", message);
	}

	public boolean isList() {
		return isList;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

	public boolean isListCameraUploads() {
		return isListCameraUploads;
	}
	public boolean isSmallGridCameraUploads() {
		return isSmallGridCameraUploads;
	}
	public void setSmallGridCameraUploads(boolean isSmallGridCameraUploads) {
		this.isSmallGridCameraUploads = isSmallGridCameraUploads;
	}

	public boolean getFirstTimeCam() {
		return firstTimeCam;
	}
	public void setFirstTimeCam(boolean flag){
		firstTimeCam = flag;
	}

	public void setListCameraUploads(boolean isListCameraUploads) {
		this.isListCameraUploads = isListCameraUploads;
	}

	public void setOrderCloud(int orderCloud) {
		log("setOrderCloud");
		this.orderCloud = orderCloud;
		if(prefs!=null){
			prefs.setPreferredSortCloud(String.valueOf(orderCloud));
		}
		dbH.setPreferredSortCloud(String.valueOf(orderCloud));
	}

	public int getOrderContacts() {
		return orderContacts;
	}

	public void setOrderContacts(int orderContacts) {
		log("setOrderContacts");
		this.orderContacts = orderContacts;
		if(prefs!=null) {
			prefs.setPreferredSortContacts(String.valueOf(orderContacts));
		}
		dbH.setPreferredSortContacts(String.valueOf(orderContacts));
	}

	public int getOrderOthers() {
		return orderOthers;
	}

	public void setOrderOthers(int orderOthers) {
		log("setOrderOthers");
		this.orderOthers = orderOthers;
		if(prefs!=null) {
			prefs.setPreferredSortOthers(String.valueOf(orderOthers));
		}
		dbH.setPreferredSortOthers(String.valueOf(orderOthers));
	}

	public String getPathNavigationOffline() {
		return pathNavigationOffline;
	}

	public void setPathNavigationOffline(String pathNavigationOffline) {
		log("setPathNavigationOffline: "+pathNavigationOffline);
		this.pathNavigationOffline = pathNavigationOffline;
	}

	public int getDeepBrowserTreeIncoming() {
		return deepBrowserTreeIncoming;
	}

	public void setDeepBrowserTreeIncoming(int deep) {
		deepBrowserTreeIncoming=deep;
	}

	public void increaseDeepBrowserTreeIncoming() {
		deepBrowserTreeIncoming++;
	}

	public void decreaseDeepBrowserTreeIncoming() {
		deepBrowserTreeIncoming--;
	}

	public int getDeepBrowserTreeOutgoing() {
		return deepBrowserTreeOutgoing;
	}

	public void setDeepBrowserTreeOutgoing(int deep) {
		this.deepBrowserTreeOutgoing = deep;
	}

	public void increaseDeepBrowserTreeOutgoing() {
		deepBrowserTreeOutgoing++;
	}

	public void decreaseDeepBrowserTreeOutgoing() {
		deepBrowserTreeOutgoing--;
	}

	public static DrawerItem getDrawerItem() {
		return drawerItem;
	}

	public static void setDrawerItem(DrawerItem drawerItem) {
		ManagerActivityLollipop.drawerItem = drawerItem;
	}

	public int getTabItemCloud(){
		if(viewPagerCDrive!=null){
			return viewPagerCDrive.getCurrentItem();
		}
		return -1;
	}

	public int getTabItemShares(){
		if(viewPagerShares!=null){
			return viewPagerShares.getCurrentItem();
		}
		return -1;
	}

	public int getTabItemContacts(){
		if(viewPagerContacts!=null){
			return viewPagerContacts.getCurrentItem();
		}
		return -1;
	}

	public void setTabItemCloud(int index){
		viewPagerCDrive.setCurrentItem(index);
	}

	public void setTabItemShares(int index){
		viewPagerShares.setCurrentItem(index);
	}

	public void setTabItemContacts(int index){
		viewPagerContacts.setCurrentItem(index);
	}

	public void showChatPanel(MegaChatListItem chat){
		log("showChatPanel");

		if(chat!=null){
			this.selectedChatItemId = chat.getChatId();
			ChatBottomSheetDialogFragment bottomSheetDialogFragment = new ChatBottomSheetDialogFragment();
			bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		}
	}

	public void showTransfersPanel(){
		log("showChatPanel");

		int pendingTransfers = megaApi.getNumPendingUploads()+megaApi.getNumPendingDownloads();

		if(pendingTransfers>0){
			transfersBottomSheet = new TransfersBottomSheetDialogFragment();
			transfersBottomSheet.show(getSupportFragmentManager(), transfersBottomSheet.getTag());
		}
	}

	public void updateUserNameNavigationView(String fullName, String firstLetter){
		log("updateUserNameNavigationView");

		nVDisplayName.setText(fullName);

		nVPictureProfileTextView.setText(firstLetter);
		nVPictureProfileTextView.setTextSize(32);
		nVPictureProfileTextView.setTextColor(Color.WHITE);
	}

	public void updateMailNavigationView(String email){
		log("updateMailNavigationView: "+email);
		nVEmail.setText(megaApi.getMyEmail());
	}

	public void animateFABCollection(){
		log("animateFABCollection");

		if(isFabOpen){
			mainFabButtonChat.startAnimation(rotateLeftAnim);
			firstFabButtonChat.startAnimation(closeFabAnim);
			secondFabButtonChat.startAnimation(closeFabAnim);
			thirdFabButtonChat.startAnimation(closeFabAnim);
			firstFabButtonChat.setClickable(false);
			secondFabButtonChat.setClickable(false);
			thirdFabButtonChat.setClickable(false);
			isFabOpen = false;
			log("close COLLECTION FAB");

		} else {
			mainFabButtonChat.startAnimation(rotateRightAnim);
			firstFabButtonChat.startAnimation(openFabAnim);
			secondFabButtonChat.startAnimation(openFabAnim);
			thirdFabButtonChat.startAnimation(openFabAnim);
			firstFabButtonChat.setClickable(true);
			secondFabButtonChat.setClickable(true);
			thirdFabButtonChat.setClickable(true);
			isFabOpen = true;
			fabButton.setVisibility(View.GONE);
			fabButtonsLayout.setVisibility(View.VISIBLE);
			log("open COLLECTION FAB");
		}
	}

	public void hideFabButton(){
		fabButton.setVisibility(View.GONE);
	}

	public void showFabButton(){
		log("showFabButton");
		if(drawerItem==null){
			return;
		}
		fabButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_white));
		switch (drawerItem){
			case CLOUD_DRIVE:{
				log("showFabButton: Cloud Drive SECTION");
				int indexCloud = getTabItemCloud();
				switch(indexCloud){
					case 0:{
						log("showFabButton: cloud TAB");
						fabButton.setVisibility(View.VISIBLE);
						break;
					}
					case 1:{
						log("showFabButton: rubbish TAB");
						fabButton.setVisibility(View.GONE);
						break;
					}
					default: {
						fabButton.setVisibility(View.GONE);
						break;
					}
				}
				break;
			}
			case SHARED_ITEMS:{
				log("showFabButton: Shared Items SECTION");
				int indexShares = getTabItemShares();
				switch(indexShares){
					case 0:{
						log("showFabButton: INCOMING TAB");
						inSFLol = (IncomingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 0);
						if(inSFLol!=null){
							if(deepBrowserTreeIncoming<=0){
								log("showFabButton: fabButton GONE");
								fabButton.setVisibility(View.GONE);
							}
							else {
								//Check the folder's permissions
								MegaNode parentNodeInSF = megaApi.getNodeByHandle(parentHandleIncoming);
								if(parentNodeInSF!=null){
									int accessLevel= megaApi.getAccess(parentNodeInSF);
									log("showFabButton: Node: "+parentNodeInSF.getName());

									switch(accessLevel) {
										case MegaShare.ACCESS_OWNER:
										case MegaShare.ACCESS_READWRITE:
										case MegaShare.ACCESS_FULL: {
											fabButton.setVisibility(View.VISIBLE);
											break;
										}
										case MegaShare.ACCESS_READ: {
											fabButton.setVisibility(View.GONE);
											break;
										}
									}
								}
								else{
									fabButton.setVisibility(View.GONE);
								}
							}
						}
						break;
					}
					case 1:{
						log("showFabButton: OUTGOING TAB");
						outSFLol = (OutgoingSharesFragmentLollipop) sharesPageAdapter.instantiateItem(viewPagerShares, 1);
						if(outSFLol!=null){
							if(deepBrowserTreeOutgoing<=0){
								fabButton.setVisibility(View.GONE);
							}
							else {
								fabButton.setVisibility(View.VISIBLE);
							}
						}
						break;
					}
					default: {
						fabButton.setVisibility(View.GONE);
						break;
					}
				}
				break;
			}
			case CONTACTS:{
				int indexContacts = getTabItemContacts();
				switch(indexContacts){
					case 0:
					case 1:{
						fabButton.setVisibility(View.VISIBLE);
						break;
					}
					default:{
						fabButton.setVisibility(View.GONE);
						break;
					}
				}
				break;
			}
			case CHAT:{
				if(megaChatApi!=null){
					if(megaChatApi.getChatRooms().size()==0){
						fabButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_chat_white));
					}
					fabButton.setVisibility(View.VISIBLE);
				}
				else{
					fabButton.setVisibility(View.GONE);
				}
				break;
			}
			case SEARCH:{
				if(sFLol!=null && sFLol.isAdded()){
					log("parentHandleSearch: "+parentHandleSearch);

					if(levelsSearch<0){
						fabButton.setVisibility(View.GONE);
					}
					else{
						long parentHandleSearch = sFLol.getParentHandle();
						if(parentHandleSearch!=-1){
							MegaNode node = megaApi.getNodeByHandle(parentHandleSearch);
							if(node.isInShare()){
								log("Node is incoming folder");
								int accessLevel = megaApi.getAccess(node);

								if(accessLevel== MegaShare.ACCESS_FULL||accessLevel== MegaShare.ACCESS_OWNER){
									fabButton.setVisibility(View.VISIBLE);
								}
								else if(accessLevel== MegaShare.ACCESS_READWRITE){
									fabButton.setVisibility(View.VISIBLE);
								}
								else{
									fabButton.setVisibility(View.GONE);
								}
							}
							else{
								fabButton.setVisibility(View.VISIBLE);
							}
						}
						else{
							fabButton.setVisibility(View.GONE);
						}

					}
				}
				break;
			}
			default:{
				log("showFabButton: default GONE fabButton");
				fabButton.setVisibility(View.GONE);
				break;
			}
		}
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

	public MegaNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(MegaNode selectedNode) {
		this.selectedNode = selectedNode;
	}


	public ContactsFragmentLollipop getContactsFragment() {
		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		return cFLol;
	}

	public MyAccountFragmentLollipop getMyAccountFragment() {
		String myAccountTag = getFragmentTag(R.id.my_account_tabs_pager, 0);
		maFLol = (MyAccountFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myAccountTag);
		if(maFLol!=null){
			return maFLol;
		}
		return null;
	}

	public MyStorageFragmentLollipop getMyStorageFragment() {
		String myStorageTag = getFragmentTag(R.id.my_account_tabs_pager, 1);
		mStorageFLol = (MyStorageFragmentLollipop) getSupportFragmentManager().findFragmentByTag(myStorageTag);
		if(mStorageFLol!=null){
			return mStorageFLol;
		}
		return null;
	}

	public UpgradeAccountFragmentLollipop getUpgradeAccountFragment() {
		return upAFL;
	}

	public MonthlyAnnualyFragmentLollipop getMonthlyAnnualyFragment() {
		return myFL;
	}

	public CentiliFragmentLollipop getCentiliFragment() {
		return ctFL;
	}

	public FortumoFragmentLollipop getFortumoFragment() {
		return fFL;
	}

	public void setContactsFragment(ContactsFragmentLollipop cFLol) {
		this.cFLol = cFLol;
	}

	public SettingsFragmentLollipop getSettingsFragment() {
		return sttFLol;
	}

	public void setSettingsFragment(SettingsFragmentLollipop sttFLol) {
		this.sttFLol = sttFLol;
	}

	public MegaContactAdapter getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(MegaContactAdapter selectedUser) {
		this.selectedUser = selectedUser;
	}


	public MegaContactRequest getSelectedRequest() {
		return selectedRequest;
	}

	public void setSelectedRequest(MegaContactRequest selectedRequest) {
		this.selectedRequest = selectedRequest;
	}

	public int getAccountFragment() {
		return accountFragment;
	}

	public void setAccountFragment(int accountFragment) {
		this.accountFragment = accountFragment;
	}

	public MegaOffline getSelectedOfflineNode() {
		return selectedOfflineNode;
	}

	public void setSelectedOfflineNode(MegaOffline selectedOfflineNode) {
		this.selectedOfflineNode = selectedOfflineNode;
	}


	public int getSelectedPaymentMethod() {
		return selectedPaymentMethod;
	}

	public void setSelectedPaymentMethod(int selectedPaymentMethod) {
		this.selectedPaymentMethod = selectedPaymentMethod;
	}
	public void visibilitySearch(boolean visibility){
		searchByDate.setVisible(visibility);
	}


	public int getSelectedAccountType() {
		return selectedAccountType;
	}

	public void setSelectedAccountType(int selectedAccountType) {
		this.selectedAccountType = selectedAccountType;
	}


	public int getDisplayedAccountType() {
		return displayedAccountType;
	}

	public void setDisplayedAccountType(int displayedAccountType) {
		this.displayedAccountType = displayedAccountType;
	}

	public void enableChat(){

		((MegaApplication) getApplication()).enableChat();

		Intent intent = new Intent(managerActivity, LoginActivityLollipop.class);
		intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
		intent.setAction(Constants.ACTION_ENABLE_CHAT);
		startActivity(intent);
		finish();
//		UserCredentials credentials = dbH.getCredentials();
//		String gSession = credentials.getSession();
//		int ret = megaChatApi.init(gSession);
//		megaApi.fetchNodes(this);
	}

	public void disableChat(){
		log("disableChat");

		String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
		cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
		if (cFLol != null){
			if(cFLol.isAdded()){
				cFLol.notifyDataSetChanged();
			}
		}

		drawerItem = DrawerItem.SETTINGS;
		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem chat = nVMenu.findItem(R.id.navigation_item_chat);
			chat.setTitle(getString(R.string.section_chat));
			MenuItem settings = nVMenu.findItem(R.id.navigation_item_settings);
			settings.setChecked(true);
			settings.setIcon(ContextCompat.getDrawable(this, R.drawable.settings_red));
		}

		if (megaChatApi != null){
			megaChatApi.removeChatListener(this);
		}

		megaChatApi.logout(this);
		app.disableMegaChatApi();
		megaChatApi=null;

		updateNavigationToolbarIcon();
	}

	@Override
	public void onChatListItemUpdate(MegaChatApiJava api, MegaChatListItem item) {
		if (item != null){
			log("onChatListItemUpdate:" + item.getTitle());
		}
		else{
			log("onChatListItemUpdate");
		}

		if(rChatFL!=null){
			if(rChatFL.isAdded()){
				rChatFL.listItemUpdate(item);
			}
		}

		if(Util.isChatEnabled()){
			if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_UNREAD_COUNT)) {
				log("Change unread count: " + item.getTitle());
				setChatTitleSection();
				updateNavigationToolbarIcon();
			}
		}
	}

	@Override
	public void onChatInitStateUpdate(MegaChatApiJava api, int newState) {
		log("onChatInitStateUpdate: "+newState);
		if (newState == MegaChatApi.INIT_ERROR) {
			// chat cannot initialize, disable chat completely
//			log("newState == MegaChatApi.INIT_ERROR");
//			if (chatSettings == null) {
//				log("1 - onChatInitStateUpdate: ERROR----> Switch OFF chat");
//				chatSettings = new ChatSettings(false + "", true + "", "", true + "");
//				dbH.setChatSettings(chatSettings);
//			} else {
//				log("2 - onChatInitStateUpdate: ERROR----> Switch OFF chat");
//				dbH.setEnabledChat(false + "");
//			}
//			if(megaChatApi!=null){
//				megaChatApi.logout(null);
//			}
		}
	}

	@Override
	public void onChatOnlineStatusUpdate(MegaChatApiJava api, long userHandle, int status, boolean inProgress) {
		log("onChatOnlineStatusUpdate: "+status+"___"+inProgress);
		if(inProgress){
			status = -1;
		}

		if(megaChatApi!=null){
			if(Util.isChatEnabled()){
				if(userHandle == megaChatApi.getMyUserHandle()){
					log("My own status update");
					if(drawerItem == DrawerItem.CHAT){
							if(rChatFL!=null){
								if(rChatFL.isAdded()){
									rChatFL.onlineStatusUpdate(status);

								}
							}
						}
				}
				else{
					log("Status update for the user: "+userHandle);
					if(rChatFL!=null){
						if(rChatFL.isAdded()){
							log("Update Recent chats view");
							rChatFL.contactStatusUpdate(userHandle, status);
						}
					}

					String cFTag = getFragmentTag(R.id.contact_tabs_pager, 0);
					cFLol = (ContactsFragmentLollipop) getSupportFragmentManager().findFragmentByTag(cFTag);
					if(cFLol!=null){
						if(cFLol.isAdded()){
							log("Update Contacts view");
							cFLol.contactStatusUpdate(userHandle, status);
						}
					}
				}
			}
		}
	}

	@Override
	public void onChatPresenceConfigUpdate(MegaChatApiJava api, MegaChatPresenceConfig config) {
		log("onPresenceConfigUpdate");
		if(config!=null){
			log("Config status: "+config.getOnlineStatus());
			log("Config autoway: "+config.isAutoawayEnabled());
			log("Config persist: "+config.isPersist());
			if(sttFLol!=null){
				if(sttFLol.isAdded()){
					if(config!=null){
						sttFLol.updatePresenceConfigChat(false, config);
					}
				}
			}
		}
		else{
			log("Config is null");
		}
	}

	@Override
	public void onChatConnectionStateUpdate(MegaChatApiJava api, long chatid, int newState) {
		log("onChatConnectionStateUpdate: "+chatid+" newState: "+newState);

		if(newState==MegaChatApi.CHAT_CONNECTION_ONLINE && chatid==-1){
			log("Online Connection: "+chatid);
			if (rChatFL != null){
				if(rChatFL.isAdded()){
					rChatFL.setChats();
					if(drawerItem == DrawerItem.CHAT){
						rChatFL.setStatus();
					}
				}
			}
		}
	}

	public boolean isMkLayoutVisible() {
		return mkLayoutVisible;
	}

	public void setMkLayoutVisible(boolean mkLayoutVisible) {
		this.mkLayoutVisible = mkLayoutVisible;
	}

	public void copyError(){
		try {
			statusDialog.dismiss();
			showSnackbar(getString(R.string.context_no_copied));
		}
		catch (Exception ex) {}
	}

	public void uploadTakePicture(String imagePath){
		log("uploadTakePicture");

		MegaNode parentNode = null;

		if(cloudPageAdapter!=null) {
			fbFLol = (FileBrowserFragmentLollipop) cloudPageAdapter.instantiateItem(viewPagerCDrive, 0);
			if (fbFLol != null) {
				if (fbFLol.isAdded()) {
					if (parentHandleBrowser != -1) {
						parentNode = megaApi.getNodeByHandle(parentHandleBrowser);
					}
				}
			} else {
				log("FileBrowser is NULL after move");
			}
		}

		if(parentNode==null){
			parentNode = megaApi.getRootNode();
		}

		Intent intent = new Intent(this, UploadService.class);
		File selfie = new File(imagePath);
		intent.putExtra(UploadService.EXTRA_FILEPATH, selfie.getAbsolutePath());
		intent.putExtra(UploadService.EXTRA_NAME, selfie.getName());
		intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
		intent.putExtra(UploadService.EXTRA_SIZE, selfie.length());
		startService(intent);
	}

//	public void showFileChooser(String imagePath){
//
//		log("showFileChooser: "+imagePath);
//		Intent intent = new Intent(this, FileExplorerActivityLollipop.class);
//		intent.setAction(FileExplorerActivityLollipop.ACTION_UPLOAD_SELFIE);
//		intent.putExtra("IMAGE_PATH", imagePath);
//		startActivity(intent);
//		//finish();
//	}

	public void changeStatusBarColor(int option) {
		log("changeStatusBarColor");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_dark_primary_color));

			if (option == 2){
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						window.setStatusBarColor(0);
					}
				}, 500);
			}
		}
		if (option == 1){
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
		else {
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}

	}

	public long getParentHandleInbox() {
		return parentHandleInbox;
	}

	@Override
	public void onChatCallUpdate(MegaChatApiJava api, MegaChatCall call) {
		log("onChatCallUpdate");
	}

	public void setContactTitleSection(){
		ArrayList<MegaContactRequest> requests = megaApi.getIncomingContactRequests();

		if (nV != null) {
			Menu nVMenu = nV.getMenu();
			MenuItem contacts = nVMenu.findItem(R.id.navigation_item_contacts);
			if(requests!=null){
				int pendingRequest = requests.size();
				if(pendingRequest==0){
					contacts.setTitle(getString(R.string.section_contacts));
				}
				else{
					String textToShow = String.format(getString(R.string.section_contacts_with_notification), pendingRequest);
					try {
						textToShow = textToShow.replace("[A]", "<font color=\'#ff333a\'>");
						textToShow = textToShow.replace("[/A]", "</font>");
					}
					catch(Exception e){
						log("Formatted string: " + textToShow);
					}

					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					contacts.setTitle(result);
				}
			}
		}
	}

	public void setChatTitleSection(){
		log("setChatTitleSection");
		if (nV != null){
			Menu nVMenu = nV.getMenu();
			MenuItem chat = nVMenu.findItem(R.id.navigation_item_chat);
			int numberUnread = megaChatApi.getUnreadChats();
			if(numberUnread==0){
				chat.setTitle(getString(R.string.section_chat));
			}
			else{
				String textToShow = String.format(getString(R.string.section_chat_with_notification), numberUnread);
				try {
					textToShow = textToShow.replace("[A]", "<font color=\'#ff333a\'>");
					textToShow = textToShow.replace("[/A]", "</font>");
				}
				catch(Exception e){
					log("Formatted string: " + textToShow);
				}

				log("TEXTTOSHOW: " + textToShow);
				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}
				chat.setTitle(result);
			}
		}
	}

	public void showEvaluatedAppDialog(){
		if(megaChatApi.isSignalActivityRequired()){
			megaChatApi.signalPresenceActivity();
		}

		LayoutInflater inflater = getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.evaluate_the_app_dialog, null);

		final CheckedTextView rateAppCheck = (CheckedTextView) dialoglayout.findViewById(R.id.rate_the_app);
		rateAppCheck.setText(getString(R.string.rate_the_app_panel));
		rateAppCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
		rateAppCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
		ViewGroup.MarginLayoutParams rateAppMLP = (ViewGroup.MarginLayoutParams) rateAppCheck.getLayoutParams();
		rateAppMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

		final CheckedTextView sendFeedbackCheck = (CheckedTextView) dialoglayout.findViewById(R.id.send_feedback);
		sendFeedbackCheck.setText(getString(R.string.send_feedback_panel));
		sendFeedbackCheck.setTextSize(TypedValue.COMPLEX_UNIT_SP, (16*scaleText));
		sendFeedbackCheck.setCompoundDrawablePadding(Util.scaleWidthPx(10, outMetrics));
		ViewGroup.MarginLayoutParams sendFeedbackMLP = (ViewGroup.MarginLayoutParams) sendFeedbackCheck.getLayoutParams();
		sendFeedbackMLP.setMargins(Util.scaleWidthPx(15, outMetrics), Util.scaleHeightPx(10, outMetrics), 0, Util.scaleHeightPx(10, outMetrics));

		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
		builder.setView(dialoglayout);

		builder.setTitle(getString(R.string.title_evaluate_the_app_panel));
		evaluateAppDialog = builder.create();

		evaluateAppDialog.show();

		rateAppCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				log("Rate the app");
				//Rate the app option:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=mega.privacy.android.app") ) );

				if (evaluateAppDialog!= null){
					evaluateAppDialog.dismiss();
				}
			}
		});

		sendFeedbackCheck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				log("Send Feedback");

				//Send feedback option:
				StringBuilder body = new StringBuilder();
				body.append(getString(R.string.setting_feedback_body));
				body.append("\n\n\n\n\n\n\n\n\n\n\n");
				body.append(getString(R.string.settings_feedback_body_device_model)+"  "+getDeviceName()+"\n");
				body.append(getString(R.string.settings_feedback_body_android_version)+"  "+Build.VERSION.RELEASE+" "+Build.DISPLAY+"\n");
				body.append(getString(R.string.user_account_feedback)+"  "+megaApi.getMyEmail());

				if(((MegaApplication) getApplication()).getMyAccountInfo()!=null){
					if(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()<0||((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()>4){
						body.append(" ("+getString(R.string.my_account_free)+")");
					}
					else{
						switch(((MegaApplication) getApplication()).getMyAccountInfo().getAccountType()){
							case 0:{
								body.append(" ("+getString(R.string.my_account_free)+")");
								break;
							}
							case 1:{
								body.append(" ("+getString(R.string.my_account_pro1)+")");
								break;
							}
							case 2:{
								body.append(" ("+getString(R.string.my_account_pro2)+")");
								break;
							}
							case 3:{
								body.append(" ("+getString(R.string.my_account_pro3)+")");
								break;
							}
							case 4:{
								body.append(" (PRO "+getString(R.string.my_account_prolite)+")");
								break;
							}
						}
					}
				}

				String emailAndroid = Constants.MAIL_ANDROID;
				String versionApp = (getString(R.string.app_version));
				String subject = getString(R.string.setting_feedback_subject)+" v"+versionApp;

				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAndroid));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
				emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
				startActivity(Intent.createChooser(emailIntent, " "));

				if (evaluateAppDialog != null){
					evaluateAppDialog.dismiss();
				}
			}
		});

	}

	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public boolean getPasswordReminderFromMyAccount() {
		return passwordReminderFromMyAccount;
	}

	public void setPasswordReminderFromMyAccount(boolean passwordReminderFromMyAccount) {
		this.passwordReminderFromMyAccount = passwordReminderFromMyAccount;
	}

	public void refreshMenu(){
		log("refreshMenu");
		supportInvalidateOptionsMenu();
	}

	public boolean is2FAEnabled (){
		return is2FAEnabled;
	}

	public void setNewMail (String newMail) {
		this.newMail = newMail;
	}

	public void setSendToChat (boolean sendToChat) {
			this.sendToChat = sendToChat;
	}

	public boolean isCameraUploads(MegaNode n){
		log("isCameraUploads()");
		String cameraSyncHandle = null;

		//Check if the item is the Camera Uploads folder
		if(dbH.getPreferences()!=null){
			prefs = dbH.getPreferences();
			if(prefs.getCamSyncHandle()!=null){
				cameraSyncHandle = prefs.getCamSyncHandle();
			}else{
				cameraSyncHandle = null;
			}
		}else{
			prefs=null;
		}

		if(cameraSyncHandle!=null){
			if(!(cameraSyncHandle.equals(""))){
				if ((n.getHandle()==Long.parseLong(cameraSyncHandle))){
					return true;
				}

			}else{
				if(n.getName().equals("Camera Uploads")){
					if (prefs != null){
						prefs.setCamSyncHandle(String.valueOf(n.getHandle()));
					}
					dbH.setCamSyncHandle(n.getHandle());
					log("FOUND Camera Uploads!!----> "+n.getHandle());
					return true;
				}
			}

		}else{
			if(n.getName().equals("Camera Uploads")){
				if (prefs != null){
					prefs.setCamSyncHandle(String.valueOf(n.getHandle()));
				}
				dbH.setCamSyncHandle(n.getHandle());
				log("FOUND Camera Uploads!!: "+n.getHandle());
				return true;
			}
		}

		//Check if the item is the Media Uploads folder
		String secondaryMediaHandle = null;

		if(prefs!=null){
			if(prefs.getMegaHandleSecondaryFolder()!=null){
				secondaryMediaHandle =prefs.getMegaHandleSecondaryFolder();
			}else{
				secondaryMediaHandle = null;
			}
		}

		if(secondaryMediaHandle!=null){
			if(!(secondaryMediaHandle.equals(""))){
				if ((n.getHandle()==Long.parseLong(secondaryMediaHandle))){
					log("Click on Media Uploads");
					return true;
				}
			}
		}else{
			if(n.getName().equals(CameraSyncService.SECONDARY_UPLOADS)){
				if (prefs != null){
					prefs.setMegaHandleSecondaryFolder(String.valueOf(n.getHandle()));
				}
				dbH.setSecondaryFolderHandle(n.getHandle());
				log("FOUND Media Uploads!!: "+n.getHandle());
				return true;
			}
		}
		return false;
	}
	
	//need to check image existence before use due to android content provider issue.
	//Can not check query count - still get count = 1 even file does not exist
	private boolean checkProfileImageExistence(Uri uri){
		boolean isFileExist = false;
		InputStream inputStream;
		try {
			inputStream = this.getContentResolver().openInputStream(uri);
			if(inputStream != null){
				isFileExist = true;
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return isFileExist;
	}
}
