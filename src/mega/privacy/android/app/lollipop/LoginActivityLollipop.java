package mega.privacy.android.app.lollipop;

import java.util.Locale;

import mega.privacy.android.app.CameraSyncService;
import mega.privacy.android.app.ChooseAccountActivity;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.OldPreferences;
import mega.privacy.android.app.OldUserCredentials;
import mega.privacy.android.app.UserCredentials;
import mega.privacy.android.app.providers.FileProviderActivity;
import mega.privacy.android.app.utils.Util;
import mega.privacy.android.app.R;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

 
public class LoginActivityLollipop extends Activity implements OnClickListener, MegaRequestListenerInterface{
	
	public static String ACTION_REFRESH = "ACTION_REFRESH";
	public static String ACTION_CREATE_ACCOUNT_EXISTS = "ACTION_CREATE_ACCOUNT_EXISTS";
	public static String ACTION_CONFIRM = "MEGA_ACTION_CONFIRM";
	public static String EXTRA_CONFIRMATION = "MEGA_EXTRA_CONFIRMATION";
	
	TextView loginTitle;
	TextView newToMega;
	EditText et_user;
	EditText et_password;
	TextView bRegister;
	TextView registerText;
	TextView bLogin;
	ImageView loginThreeDots;
	Switch loginSwitch;
	TextView loginABC;
	LinearLayout loginLogin;
	LinearLayout loginLoggingIn;
	LinearLayout loginCreateAccount;
	View loginDelimiter;
	ProgressBar loginProgressBar;
	ProgressBar loginFetchNodesProgressBar;
	TextView generatingKeysText;
	TextView queryingSignupLinkText;
	TextView confirmingAccountText;
	TextView loggingInText;
	TextView fetchingNodesText;
	TextView prepareNodesText;
	ScrollView scrollView;

	float scaleH, scaleW;
	float density;
	DisplayMetrics outMetrics;
	Display display;
	
//	private ProgressDialog progress;
	
	private String lastEmail;
	private String lastPassword;
	private String gPublicKey;
	private String gPrivateKey;
	private String gSession;
	
	private String confirmLink;
	
	static LoginActivityLollipop loginActivity;
    private MegaApiAndroid megaApi;
    UserCredentials credentials;
    private boolean backWhileLogin;
    private boolean loginClicked = false;
    private long parentHandle = -1;
    
    String action = null;
    String url = null;
    
    boolean firstRequestUpdate = true;
    boolean firstTime = true;
        
    Handler handler = new Handler();
    
    Bundle extras = null;
    Uri uriData = null;
	
	/*
	 * Task to process email and password
	 */
	private class HashTask extends AsyncTask<String, Void, String[]> {

		@Override
		protected String[] doInBackground(String... args) {
			String privateKey = megaApi.getBase64PwKey(args[1]);
			String publicKey = megaApi.getStringHash(privateKey, args[0]);
			return new String[]{new String(privateKey), new String(publicKey)}; 
		}

		
		@Override
		protected void onPostExecute(String[] key) {
			onKeysGenerated(key[0], key[1]);
		}

	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loginClicked = false;
		
		loginActivity = this;
		MegaApplication app = (MegaApplication)getApplication();
		megaApi = app.getMegaApi();
		
		backWhileLogin = false;
		
		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;
		
	    scaleW = Util.getScaleW(outMetrics, density);
	    scaleH = Util.getScaleH(outMetrics, density);
	    float scaleText;
	    if (scaleH < scaleW){
	    	scaleText = scaleH;
	    }
	    else{
	    	scaleText = scaleW;
	    }

	    DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
	    
	    MegaPreferences prefs = dbH.getPreferences();

	    setContentView(R.layout.activity_login);	    
	    firstTime = true;

		scrollView = (ScrollView) findViewById(R.id.scroll_view_login);		
				
	    loginTitle = (TextView) findViewById(R.id.login_text_view);
		//Left margin
		LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams)loginTitle.getLayoutParams();
		textParams.setMargins(Util.scaleWidthPx(30, outMetrics), Util.scaleHeightPx(40, outMetrics), 0, Util.scaleHeightPx(20, outMetrics)); 
		loginTitle.setLayoutParams(textParams);
		
		loginTitle.setText(R.string.login_text);
		loginTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (22*scaleText));
		
		et_user = (EditText) findViewById(R.id.login_email_text);
		android.view.ViewGroup.LayoutParams paramsb1 = et_user.getLayoutParams();		
		paramsb1.width = Util.scaleWidthPx(280, outMetrics);		
		et_user.setLayoutParams(paramsb1);
		//Left margin
		textParams = (LinearLayout.LayoutParams)et_user.getLayoutParams();
		textParams.setMargins(Util.scaleWidthPx(30, outMetrics), 0, 0, Util.scaleHeightPx(10, outMetrics)); 
		et_user.setLayoutParams(textParams);	
		
		et_user.setCursorVisible(true);
		
		et_password = (EditText) findViewById(R.id.login_password_text);	
		et_password.setLayoutParams(paramsb1);
		et_password.setLayoutParams(textParams);	
		
		et_password.setCursorVisible(true);
		
		et_password.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					submitForm();
					return true;
				}
				return false;
			}
		});		
		loginThreeDots = (ImageView) findViewById(R.id.login_three_dots);
		LinearLayout.LayoutParams textThreeDots = (LinearLayout.LayoutParams)loginThreeDots.getLayoutParams();
		textThreeDots.setMargins(Util.scaleWidthPx(0, outMetrics), 0, Util.scaleWidthPx(10, outMetrics), 0); 
		loginThreeDots.setLayoutParams(textThreeDots);
		
		loginABC = (TextView) findViewById(R.id.ABC);
		
		loginSwitch = (Switch) findViewById(R.id.switch_login);
		LinearLayout.LayoutParams switchParams = (LinearLayout.LayoutParams)loginSwitch.getLayoutParams();
		switchParams.setMargins(0, 0, Util.scaleWidthPx(10, outMetrics), 0); 
		loginSwitch.setLayoutParams(switchParams);
		loginSwitch.setChecked(false);
		
		loginSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked){
						et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						et_password.setSelection(et_password.getText().length());
				}else{
						et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
						et_password.setSelection(et_password.getText().length());
			    }				
			}
		});
		
		bLogin = (TextView) findViewById(R.id.button_login_login);
		bLogin.setText(getString(R.string.login_text).toUpperCase(Locale.getDefault()));
		android.view.ViewGroup.LayoutParams paramsbLogin = bLogin.getLayoutParams();		
		paramsbLogin.height = Util.scaleHeightPx(48, outMetrics);
		paramsbLogin.width = Util.scaleWidthPx(63, outMetrics);
		bLogin.setLayoutParams(paramsbLogin);
		//Margin
		LinearLayout.LayoutParams textParamsLogin = (LinearLayout.LayoutParams)bLogin.getLayoutParams();
		textParamsLogin.setMargins(Util.scaleWidthPx(35, outMetrics), Util.scaleHeightPx(40, outMetrics), 0, Util.scaleHeightPx(80, outMetrics)); 
		bLogin.setLayoutParams(textParamsLogin);
		
		bLogin.setOnClickListener(this);
		
		loginDelimiter = (View) findViewById(R.id.login_delimiter);
		loginCreateAccount = (LinearLayout) findViewById(R.id.login_create_account_layout);
		
	    newToMega = (TextView) findViewById(R.id.text_newToMega);
		//Margins (left, top, right, bottom)
		LinearLayout.LayoutParams textnewToMega = (LinearLayout.LayoutParams)newToMega.getLayoutParams();
		textnewToMega.setMargins(Util.scaleHeightPx(30, outMetrics), Util.scaleHeightPx(20, outMetrics), 0, Util.scaleHeightPx(30, outMetrics)); 
		newToMega.setLayoutParams(textnewToMega);	
		newToMega.setTextSize(TypedValue.COMPLEX_UNIT_SP, (22*scaleText));
		
	    bRegister = (TextView) findViewById(R.id.button_create_account_login);
	    
	    bRegister.setText(getString(R.string.create_account).toUpperCase(Locale.getDefault()));
		android.view.ViewGroup.LayoutParams paramsb2 = bRegister.getLayoutParams();		
		paramsb2.height = Util.scaleHeightPx(48, outMetrics);
		paramsb2.width = Util.scaleWidthPx(144, outMetrics);
		bRegister.setLayoutParams(paramsb2);
		//Margin
		LinearLayout.LayoutParams textParamsRegister = (LinearLayout.LayoutParams)bRegister.getLayoutParams();
		textParamsRegister.setMargins(Util.scaleWidthPx(35, outMetrics), 0, 0, 0); 
		bRegister.setLayoutParams(textParamsRegister);
	    
	    bRegister.setOnClickListener(this);
		
		loginLogin = (LinearLayout) findViewById(R.id.login_login_layout);
		loginLoggingIn = (LinearLayout) findViewById(R.id.login_logging_in_layout);
		loginProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
		loginFetchNodesProgressBar = (ProgressBar) findViewById(R.id.login_fetching_nodes_bar);
		generatingKeysText = (TextView) findViewById(R.id.login_generating_keys_text);
		queryingSignupLinkText = (TextView) findViewById(R.id.login_query_signup_link_text);
		confirmingAccountText = (TextView) findViewById(R.id.login_confirm_account_text);
		loggingInText = (TextView) findViewById(R.id.login_logging_in_text);
		fetchingNodesText = (TextView) findViewById(R.id.login_fetch_nodes_text);
		prepareNodesText = (TextView) findViewById(R.id.login_prepare_nodes_text);
		
		loginLogin.setVisibility(View.VISIBLE);
		loginCreateAccount.setVisibility(View.VISIBLE);
		loginDelimiter.setVisibility(View.VISIBLE);
		loginLoggingIn.setVisibility(View.GONE);
		generatingKeysText.setVisibility(View.GONE);
		loggingInText.setVisibility(View.GONE);
		fetchingNodesText.setVisibility(View.GONE);
		prepareNodesText.setVisibility(View.GONE);
		loginProgressBar.setVisibility(View.GONE);
		queryingSignupLinkText.setVisibility(View.GONE);
		confirmingAccountText.setVisibility(View.GONE);		
		
//		loginLogin.setVisibility(View.GONE);
//		loginCreateAccount.setVisibility(View.GONE);
//		loginDelimiter.setVisibility(View.GONE);
//		loginLoggingIn.setVisibility(View.VISIBLE);
//		generatingKeysText.setVisibility(View.VISIBLE);
//		loggingInText.setVisibility(View.VISIBLE);
//		fetchingNodesText.setVisibility(View.VISIBLE);
//		prepareNodesText.setVisibility(View.VISIBLE);
//		loginProgressBar.setVisibility(View.VISIBLE);
//		queryingSignupLinkText.setVisibility(View.VISIBLE);
//		confirmingAccountText.setVisibility(View.VISIBLE);
			
		Intent intentReceived = getIntent();
		if (intentReceived != null){
			if (ACTION_CONFIRM.equals(intentReceived.getAction())) {
				handleConfirmationIntent(intentReceived);
				return;
			}
			else if (ACTION_CREATE_ACCOUNT_EXISTS.equals(intentReceived.getAction())){
				String message = getString(R.string.error_email_registered);
				Snackbar.make(scrollView,message,Snackbar.LENGTH_LONG).show();
				return;
			}
		}
		
		credentials = dbH.getCredentials();
		if (credentials != null){
			firstTime = false;
			if ((intentReceived != null) && (intentReceived.getAction() != null)){
				if (intentReceived.getAction().equals(ACTION_REFRESH)){
					parentHandle = intentReceived.getLongExtra("PARENT_HANDLE", -1);
					
					lastEmail = credentials.getEmail();
					gSession = credentials.getSession();
					
					loginLogin.setVisibility(View.GONE);
					loginDelimiter.setVisibility(View.GONE);
					loginCreateAccount.setVisibility(View.GONE);
					queryingSignupLinkText.setVisibility(View.GONE);
					confirmingAccountText.setVisibility(View.GONE);
					loginLoggingIn.setVisibility(View.VISIBLE);
//					generatingKeysText.setVisibility(View.VISIBLE);
//					megaApi.fastLogin(gSession, this);
					
					loginProgressBar.setVisibility(View.VISIBLE);
					loginFetchNodesProgressBar.setVisibility(View.GONE);
					loggingInText.setVisibility(View.VISIBLE);
					fetchingNodesText.setVisibility(View.VISIBLE);
					prepareNodesText.setVisibility(View.GONE);
					megaApi.fetchNodes(loginActivity);
					return;
				}
				else{
					if(intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_OPEN_MEGA_FOLDER_LINK)){
						action = ManagerActivityLollipop.ACTION_OPEN_MEGA_FOLDER_LINK;
						url = intentReceived.getDataString();
					}
					else if(intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_IMPORT_LINK_FETCH_NODES)){
						action = ManagerActivityLollipop.ACTION_OPEN_MEGA_LINK;
						url = intentReceived.getDataString();
					}
					else if (intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_CANCEL_UPLOAD) || intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_CANCEL_DOWNLOAD) || intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_CANCEL_CAM_SYNC)){
						action = intentReceived.getAction();
					}
//					else if (intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_FILE_EXPLORER_UPLOAD)){
//						action = ManagerActivityLollipop.ACTION_FILE_EXPLORER_UPLOAD;
//						uriData = intentReceived.getData();
//						log("URI: "+uriData);
//						extras = intentReceived.getExtras();
//						url = null;
//						Snackbar.make(scrollView,getString(R.string.login_before_share),Snackbar.LENGTH_LONG).show();
//					}
					else if (intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_FILE_PROVIDER)){
						action = ManagerActivityLollipop.ACTION_FILE_PROVIDER;
						uriData = intentReceived.getData();
						extras = intentReceived.getExtras();
						url = null;
					}
					
					MegaNode rootNode = megaApi.getRootNode();
					if (rootNode != null){
						Intent intent = new Intent(this, ManagerActivityLollipop.class);
						if (action != null){
//							if (action.equals(ManagerActivityLollipop.ACTION_FILE_EXPLORER_UPLOAD)){
//								intent = new Intent(this, FileExplorerActivityLollipop.class);
//								if(extras != null)
//								{
//									intent.putExtras(extras);
//								}
//								intent.setData(uriData);
//							}
							if (action.equals(ManagerActivityLollipop.ACTION_FILE_PROVIDER)){
								intent = new Intent(this, FileProviderActivity.class);
								if(extras != null)
								{
									intent.putExtras(extras);
								}
								intent.setData(uriData);
							}
							intent.setAction(action);
							if (url != null){
								intent.setData(Uri.parse(url));
							}
						}
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						}
					    
					    handler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								log("Now I start the service");
								startService(new Intent(getApplicationContext(), CameraSyncService.class));		
							}
						}, 5 * 60 * 1000);
						
						this.startActivity(intent);
						this.finish();
						return;
					}
					else{
						lastEmail = credentials.getEmail();
						gSession = credentials.getSession();
						
						loginLogin.setVisibility(View.GONE);
						loginDelimiter.setVisibility(View.GONE);
						loginCreateAccount.setVisibility(View.GONE);
						queryingSignupLinkText.setVisibility(View.GONE);
						confirmingAccountText.setVisibility(View.GONE);
						loginLoggingIn.setVisibility(View.VISIBLE);
//						generatingKeysText.setVisibility(View.VISIBLE);
						loginProgressBar.setVisibility(View.VISIBLE);
						loginFetchNodesProgressBar.setVisibility(View.GONE);
						loggingInText.setVisibility(View.VISIBLE);
						fetchingNodesText.setVisibility(View.GONE);
						prepareNodesText.setVisibility(View.GONE);
						megaApi.fastLogin(gSession, this);
						return;
					}
				}
			}
			else{
				MegaNode rootNode = megaApi.getRootNode();
				if (rootNode != null){
					
					log("rootNode != null");
					Intent intent = new Intent(this, ManagerActivityLollipop.class);
					if (action != null){
//						if (action.equals(ManagerActivityLollipop.ACTION_FILE_EXPLORER_UPLOAD)){
//							intent = new Intent(this, FileExplorerActivityLollipop.class);
//							if(extras != null)
//							{
//								intent.putExtras(extras);
//							}
//							intent.setData(uriData);
//						}
						if (action.equals(ManagerActivityLollipop.ACTION_FILE_PROVIDER)){
							intent = new Intent(this, FileProviderActivity.class);
							if(extras != null)
							{
								intent.putExtras(extras);
							}
							intent.setData(uriData);
						}
						intent.setAction(action);
						if (url != null){
							intent.setData(Uri.parse(url));
						}
					}
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					}
					
					prefs = dbH.getPreferences();
					if(prefs!=null)
					{
						if (prefs.getCamSyncEnabled() != null){
							if (Boolean.parseBoolean(prefs.getCamSyncEnabled())){
								log("Enciendo el servicio de la camara");
								handler.postDelayed(new Runnable() {
									
									@Override
									public void run() {
										log("Now I start the service");
										startService(new Intent(getApplicationContext(), CameraSyncService.class));		
									}
								}, 30 * 1000);
							}
						}
					}
					this.startActivity(intent);
					this.finish();
					return;
				}
				else{
					log("rootNode == null");
					
					lastEmail = credentials.getEmail();
					gSession = credentials.getSession();
					log("session: " + gSession);
					loginLogin.setVisibility(View.GONE);
					loginDelimiter.setVisibility(View.GONE);
					loginCreateAccount.setVisibility(View.GONE);
					queryingSignupLinkText.setVisibility(View.GONE);
					confirmingAccountText.setVisibility(View.GONE);
					loginLoggingIn.setVisibility(View.VISIBLE);
//					generatingKeysText.setVisibility(View.VISIBLE);
					loginProgressBar.setVisibility(View.VISIBLE);
					loginFetchNodesProgressBar.setVisibility(View.GONE);
					loggingInText.setVisibility(View.VISIBLE);
					fetchingNodesText.setVisibility(View.GONE);
					prepareNodesText.setVisibility(View.GONE);
					megaApi.fastLogin(gSession, this);
					return;
				}
			}
		}
		else{
			if ((intentReceived != null) && (intentReceived.getAction() != null)){
				Intent intent;
				if (intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_FILE_PROVIDER)){
					intent = new Intent(this, FileProviderActivity.class);
					if(extras != null)
					{
						intent.putExtras(extras);
					}
					intent.setData(uriData);
				
					intent.setAction(action);
					
					action = ManagerActivityLollipop.ACTION_FILE_PROVIDER;
				}
				else if (intentReceived.getAction().equals(ManagerActivityLollipop.ACTION_FILE_EXPLORER_UPLOAD)){
					action = ManagerActivityLollipop.ACTION_FILE_EXPLORER_UPLOAD;
//					uriData = intentReceived.getData();
//					log("URI: "+uriData);
//					extras = intentReceived.getExtras();
//					url = null;
					Snackbar.make(scrollView,getString(R.string.login_before_share),Snackbar.LENGTH_LONG).show();
				}
			}
			if (OldPreferences.getOldCredentials(this) != null){
				loginLogin.setVisibility(View.GONE);
				loginDelimiter.setVisibility(View.GONE);
				loginCreateAccount.setVisibility(View.GONE);
				queryingSignupLinkText.setVisibility(View.GONE);
				confirmingAccountText.setVisibility(View.GONE);
				loginLoggingIn.setVisibility(View.VISIBLE);
//				generatingKeysText.setVisibility(View.VISIBLE);
				loginProgressBar.setVisibility(View.VISIBLE);
				loginFetchNodesProgressBar.setVisibility(View.GONE);
				loggingInText.setVisibility(View.VISIBLE);
				fetchingNodesText.setVisibility(View.GONE);
				prepareNodesText.setVisibility(View.GONE);
				
				OldUserCredentials oldCredentials = OldPreferences.getOldCredentials(this);
				lastEmail = oldCredentials.getEmail();
				OldPreferences.clearCredentials(this);
				onKeysGeneratedLogin(oldCredentials.getPrivateKey(), oldCredentials.getPublicKey());
			}
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
//		if (firstTime){
//			int diffHeight = heightGrey - loginCreateAccount.getTop();
//		
//			int paddingBottom = Util.px2dp((40*scaleH), outMetrics) + diffHeight;
//			loginLogin.setPadding(0, Util.px2dp((40*scaleH), outMetrics), 0, paddingBottom);
//		}
//		else{
//			int diffHeight = heightGrey - loginCreateAccount.getTop();
//			int paddingBottom = Util.px2dp((10*scaleH), outMetrics) + diffHeight;
//			loginLogin.setPadding(0, Util.px2dp((40*scaleH), outMetrics), 0, paddingBottom);
//		}
//		Toast.makeText(this, "onWindow: HEIGHT: " + loginCreateAccount.getTop() +"____" + heightGrey, Toast.LENGTH_LONG).show();
//		int marginBottom = 37; //related to a 533dp height
//		float dpHeight = outMetrics.heightPixels / density;
//		marginBottom =  marginBottom + (int) ((dpHeight - 533) / 6);
//		loginLogin.setPadding(0, Util.px2dp((40*scaleH), outMetrics), 0, Util.px2dp((marginBottom*scaleH), outMetrics));
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()){
			case R.id.button_login_login:
				loginClicked = true;
				onLoginClick(v);
				break;
			case R.id.button_create_account_login:
			case R.id.login_text_create_account:
				onRegisterClick(v);
				break;
		}
	}
	
	public void onLoginClick(View v){
		submitForm();
	}
	
	public void onRegisterClick(View v){
		Intent intent = new Intent(this, CreateAccountActivityLollipop.class);
		startActivity(intent);
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        // do nothing
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}  
	
	/*
	 * Log in form submit
	 */
	private void submitForm() {
		if (!validateForm()) {
			return;
		}
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et_user.getWindowToken(), 0);
		
		if(!Util.isOnline(this))
		{
			loginLoggingIn.setVisibility(View.GONE);
			loginLogin.setVisibility(View.VISIBLE);
			loginDelimiter.setVisibility(View.VISIBLE);
			loginCreateAccount.setVisibility(View.VISIBLE);
			queryingSignupLinkText.setVisibility(View.GONE);
			confirmingAccountText.setVisibility(View.GONE);
			generatingKeysText.setVisibility(View.GONE);
			loggingInText.setVisibility(View.GONE);
			fetchingNodesText.setVisibility(View.GONE);
			prepareNodesText.setVisibility(View.GONE);

			Snackbar.make(scrollView,getString(R.string.error_server_connection_problem),Snackbar.LENGTH_LONG).show();
			return;
		}
		
		loginLogin.setVisibility(View.GONE);
		loginDelimiter.setVisibility(View.GONE);
		loginCreateAccount.setVisibility(View.GONE);
		loginLoggingIn.setVisibility(View.VISIBLE);
		generatingKeysText.setVisibility(View.VISIBLE);
		loginProgressBar.setVisibility(View.VISIBLE);
		loginFetchNodesProgressBar.setVisibility(View.GONE);
		queryingSignupLinkText.setVisibility(View.GONE);
		confirmingAccountText.setVisibility(View.GONE);
		
		lastEmail = et_user.getText().toString().toLowerCase(Locale.ENGLISH).trim();
		lastPassword = et_password.getText().toString();
		
		log("generating keys");
		
		new HashTask().execute(lastEmail, lastPassword);
	}
	
	private void onKeysGenerated(String privateKey, String publicKey) {
		log("key generation finished");

		this.gPrivateKey = privateKey;
		this.gPublicKey = publicKey;
		
		if (confirmLink == null) {
			onKeysGeneratedLogin(privateKey, publicKey);
		} 
		else{
			if(!Util.isOnline(this)){
				Snackbar.make(scrollView,getString(R.string.error_server_connection_problem),Snackbar.LENGTH_LONG).show();				
				return;
			}
			
			loginLogin.setVisibility(View.GONE);
			loginDelimiter.setVisibility(View.GONE);
			loginCreateAccount.setVisibility(View.GONE);
			loginLoggingIn.setVisibility(View.VISIBLE);
			generatingKeysText.setVisibility(View.VISIBLE);
			loginProgressBar.setVisibility(View.VISIBLE);
			loginFetchNodesProgressBar.setVisibility(View.GONE);
			queryingSignupLinkText.setVisibility(View.GONE);
			confirmingAccountText.setVisibility(View.VISIBLE);
			fetchingNodesText.setVisibility(View.GONE);
			prepareNodesText.setVisibility(View.GONE);
			
			log("fastConfirm");
			megaApi.fastConfirmAccount(confirmLink, privateKey, this);
		}
	}
	
	private void onKeysGeneratedLogin(final String privateKey, final String publicKey) {
		
		if(!Util.isOnline(this)){
			loginLoggingIn.setVisibility(View.GONE);
			loginLogin.setVisibility(View.VISIBLE);
			loginDelimiter.setVisibility(View.VISIBLE);
			loginCreateAccount.setVisibility(View.VISIBLE);
			queryingSignupLinkText.setVisibility(View.GONE);
			confirmingAccountText.setVisibility(View.GONE);
			generatingKeysText.setVisibility(View.GONE);
			loggingInText.setVisibility(View.GONE);
			fetchingNodesText.setVisibility(View.GONE);
			prepareNodesText.setVisibility(View.GONE);
			
			Snackbar.make(scrollView,getString(R.string.error_server_connection_problem),Snackbar.LENGTH_LONG).show();
			return;
		}
		
		loggingInText.setVisibility(View.VISIBLE);
		fetchingNodesText.setVisibility(View.GONE);
		prepareNodesText.setVisibility(View.GONE);
		
		log("fastLogin con publicKey y privateKey");
		megaApi.fastLogin(lastEmail, publicKey, privateKey, this);
	}
	
	/*
	 * Validate email and password
	 */
	private boolean validateForm() {
		String emailError = getEmailError();
		String passwordError = getPasswordError();

		et_user.setError(emailError);
		et_password.setError(passwordError);

		if (emailError != null) {
			et_user.requestFocus();
			return false;
		} else if (passwordError != null) {
			et_password.requestFocus();
			return false;
		}
		return true;
	}
	
	/*
	 * Validate email
	 */
	private String getEmailError() {
		String value = et_user.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_email);
		}
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
			return getString(R.string.error_invalid_email);
		}
		return null;
	}
	
	/*
	 * Validate password
	 */
	private String getPasswordError() {
		String value = et_password.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		return null;
	}
	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request)
	{
		log("onRequestStart: " + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
//			loginProgressBar.setVisibility(View.GONE);
			loginFetchNodesProgressBar.setVisibility(View.VISIBLE);
			loginFetchNodesProgressBar.getLayoutParams().width = Util.px2dp((250*scaleW), outMetrics);
			loginFetchNodesProgressBar.setProgress(0);
		}
	}
	
	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
//		log("onRequestUpdate: " + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
			if (firstRequestUpdate){
				loginProgressBar.setVisibility(View.GONE);
				firstRequestUpdate = false;
			}
			loginFetchNodesProgressBar.setVisibility(View.VISIBLE);
			loginFetchNodesProgressBar.getLayoutParams().width = Util.px2dp((250*scaleW), outMetrics);
			if (request.getTotalBytes() > 0){
				double progressValue = 100.0 * request.getTransferredBytes() / request.getTotalBytes();
				if ((progressValue > 99) || (progressValue < 0)){
					progressValue = 100;
					prepareNodesText.setVisibility(View.VISIBLE);
					loginProgressBar.setVisibility(View.VISIBLE);
				}
//				log("progressValue = " + (int)progressValue);
				loginFetchNodesProgressBar.setProgress((int)progressValue);				
			}
		}
	}	

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError error) {
		
		log("onRequestFinish: " + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_LOGIN){
			if (error.getErrorCode() != MegaError.API_OK) {
				String errorMessage;
				if (error.getErrorCode() == MegaError.API_ENOENT) {
					errorMessage = getString(R.string.error_incorrect_email_or_password);
				}
				else if (error.getErrorCode() == MegaError.API_ENOENT) {
					errorMessage = getString(R.string.error_server_connection_problem);
				}
				else if (error.getErrorCode() == MegaError.API_ESID){
					errorMessage = getString(R.string.error_server_expired_session);
				}
				else{
					errorMessage = error.getErrorString();
				}
				loginLoggingIn.setVisibility(View.GONE);
				loginLogin.setVisibility(View.VISIBLE);
				loginDelimiter.setVisibility(View.VISIBLE);
				loginCreateAccount.setVisibility(View.VISIBLE);
				queryingSignupLinkText.setVisibility(View.GONE);
				confirmingAccountText.setVisibility(View.GONE);
				generatingKeysText.setVisibility(View.GONE);
				loggingInText.setVisibility(View.GONE);
				fetchingNodesText.setVisibility(View.GONE);
				prepareNodesText.setVisibility(View.GONE);

				Snackbar.make(scrollView,errorMessage,Snackbar.LENGTH_LONG).show();
				
//				DatabaseHandler dbH = new DatabaseHandler(this);
				DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
				dbH.clearCredentials();
				if (dbH.getPreferences() != null){
					dbH.clearPreferences();
					dbH.setFirstTime(false);
//					dbH.setPinLockEnabled(false);
//					dbH.setPinLockCode("");
//					dbH.setCamSyncEnabled(false);
					Intent stopIntent = null;
					stopIntent = new Intent(this, CameraSyncService.class);
					stopIntent.setAction(CameraSyncService.ACTION_LOGOUT);
					startService(stopIntent);
				}
			}
			else{

				loginProgressBar.setVisibility(View.VISIBLE);
				loginFetchNodesProgressBar.setVisibility(View.GONE);
				loggingInText.setVisibility(View.VISIBLE);
				fetchingNodesText.setVisibility(View.VISIBLE);
				prepareNodesText.setVisibility(View.GONE);
				
				gSession = megaApi.dumpSession();
				credentials = new UserCredentials(lastEmail, gSession);
				
//				DatabaseHandler dbH = new DatabaseHandler(getApplicationContext());
				DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
				dbH.clearCredentials();
				
				log("Logged in: " + gSession);
				
//				String session = megaApi.dumpSession();
//				Toast.makeText(this, "Session = " + session, Toast.LENGTH_LONG).show();

				//TODO
				//Aqui va el addAccount (email, session)
//				String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
//				if (accountType != null){
//					authTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
//					if (authTokenType == null){
//						authTokenType = LoginActivity.AUTH_TOKEN_TYPE_INSTANTIATE;
//					}
//					Account account = new Account(lastEmail, accountType);
//					accountManager.addAccountExplicitly(account, gSession, null);
//					log("AUTTHO: _" + authTokenType + "_");
//					accountManager.setAuthToken(account, authTokenType, gSession);
//				}
				
				megaApi.fetchNodes(loginActivity);
			}
		}
		else if (request.getType() == MegaRequest.TYPE_FETCH_NODES){
			if (error.getErrorCode() == MegaError.API_OK){
				DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
				
				gSession = megaApi.dumpSession();
				lastEmail = megaApi.getMyEmail();
				credentials = new UserCredentials(lastEmail, gSession);
				
				dbH.saveCredentials(credentials);
			}
			if(confirmLink==null){
				if (error.getErrorCode() != MegaError.API_OK) {
					String errorMessage;
					errorMessage = error.getErrorString();
					loginLoggingIn.setVisibility(View.GONE);
					loginLogin.setVisibility(View.VISIBLE);
					loginDelimiter.setVisibility(View.VISIBLE);
					loginCreateAccount.setVisibility(View.VISIBLE);
					generatingKeysText.setVisibility(View.GONE);
					loggingInText.setVisibility(View.GONE);
					fetchingNodesText.setVisibility(View.GONE);
					prepareNodesText.setVisibility(View.GONE);
					queryingSignupLinkText.setVisibility(View.GONE);
					confirmingAccountText.setVisibility(View.GONE);
					
					Snackbar.make(scrollView,errorMessage,Snackbar.LENGTH_LONG).show();
				}
				else{
					if (!backWhileLogin){
						
						if (parentHandle != -1){
							Intent intent = new Intent();
							intent.putExtra("PARENT_HANDLE", parentHandle);
							setResult(RESULT_OK, intent);
							finish();
						}
						else{
							Intent intent = null;
							if (firstTime){
								intent = new Intent(loginActivity,ManagerActivityLollipop.class);
								intent.putExtra("firstTimeCam", true);
							}
							else{
								boolean initialCam = false;
//								DatabaseHandler dbH = new DatabaseHandler(getApplicationContext());
								DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
								MegaPreferences prefs = dbH.getPreferences();
								prefs = dbH.getPreferences();
								if (prefs != null){
									if (prefs.getCamSyncEnabled() != null){
										if (Boolean.parseBoolean(prefs.getCamSyncEnabled())){
											log("Enciendo el servicio de la camara");
											handler.postDelayed(new Runnable() {
												
												@Override
												public void run() {
													log("Now I start the service");
													startService(new Intent(getApplicationContext(), CameraSyncService.class));		
												}
											}, 30 * 1000);
										}
									}
									else{
										intent = new Intent(loginActivity,ManagerActivityLollipop.class);
										intent.putExtra("firstTimeCam", true);
										initialCam = true;								
									}
								}
								else{
									intent = new Intent(loginActivity,ManagerActivityLollipop.class);
									intent.putExtra("firstTimeCam", true);
									initialCam = true;								
								}

								if (!initialCam){
									intent = new Intent(loginActivity,ManagerActivityLollipop.class);
									if (action != null){
//										if (action.equals(ManagerActivityLollipop.ACTION_FILE_EXPLORER_UPLOAD)){
//											intent = new Intent(this, FileExplorerActivityLollipop.class);
//											if(extras != null)
//											{
//												intent.putExtras(extras);
//											}
//											intent.setData(uriData);
//										}
										if (action.equals(ManagerActivityLollipop.ACTION_FILE_PROVIDER)){
											intent = new Intent(this, FileProviderActivity.class);
											if(extras != null)
											{
												intent.putExtras(extras);
											}
											intent.setData(uriData);
										}
										intent.setAction(action);
										if (url != null){
											intent.setData(Uri.parse(url));
										}
									}
								}
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							}
							
							startActivity(intent);
							finish();
						}
					}
				}
			}
			else{
				Intent intent = new Intent();
				intent = new Intent(this,ChooseAccountActivityLollipop.class);
				startActivity(intent);
				finish();				
			}
	
		}
		else if (request.getType() == MegaRequest.TYPE_QUERY_SIGNUP_LINK){
			String s = "";
			loginLogin.setVisibility(View.VISIBLE);
			loginDelimiter.setVisibility(View.VISIBLE);
			loginCreateAccount.setVisibility(View.VISIBLE);
			loginLoggingIn.setVisibility(View.GONE);
			generatingKeysText.setVisibility(View.GONE);
			queryingSignupLinkText.setVisibility(View.GONE);
			confirmingAccountText.setVisibility(View.GONE);
			fetchingNodesText.setVisibility(View.GONE);
			prepareNodesText.setVisibility(View.GONE);
			
			if(error.getErrorCode() == MegaError.API_OK){
				s = request.getEmail();
				et_user.setText(s);
				et_password.requestFocus();
			}
			else{
				Snackbar.make(scrollView,error.getErrorString(),Snackbar.LENGTH_LONG).show();
				confirmLink = null;
			}
		}
		else if (request.getType() == MegaRequest.TYPE_CONFIRM_ACCOUNT){
			if (error.getErrorCode() == MegaError.API_OK){
				log("fastConfirm finished - OK");
				onKeysGeneratedLogin(gPrivateKey, gPublicKey);
			}
			else{
				loginLogin.setVisibility(View.VISIBLE);
				loginDelimiter.setVisibility(View.VISIBLE);
				loginCreateAccount.setVisibility(View.VISIBLE);
				loginLoggingIn.setVisibility(View.GONE);
				generatingKeysText.setVisibility(View.GONE);
				queryingSignupLinkText.setVisibility(View.GONE);
				confirmingAccountText.setVisibility(View.GONE);
				fetchingNodesText.setVisibility(View.GONE);
				prepareNodesText.setVisibility(View.GONE);
				
				if (error.getErrorCode() == MegaError.API_ENOENT){
					Snackbar.make(scrollView,getString(R.string.error_incorrect_email_or_password),Snackbar.LENGTH_LONG).show();
				}
				else{
					Snackbar.make(scrollView,error.getErrorString(),Snackbar.LENGTH_LONG).show();
				}
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e)
	{
		log("onRequestTemporaryError: " + request.getRequestString());
	}
	
	@Override
	public void onBackPressed() {
		backWhileLogin = true;
		
		if (loginClicked){
			super.onBackPressed();
		}
		else{
			Intent intent = new Intent(this, TourActivityLollipop.class);
			startActivity(intent);
			finish();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	public void onNewIntent(Intent intent){
		if (intent != null && ACTION_CONFIRM.equals(intent.getAction())) {
			handleConfirmationIntent(intent);
		}
	}
	
	/*
	 * Handle intent from confirmation email
	 */
	private void handleConfirmationIntent(Intent intent) {
		confirmLink = intent.getStringExtra(EXTRA_CONFIRMATION);
		loginTitle.setText(R.string.login_confirm_account);
		bLogin.setText(R.string.login_confirm_account);
		updateConfirmEmail(confirmLink);
	}
	
	/*
	 * Get email address from confirmation code and set to emailView
	 */
	private void updateConfirmEmail(String link) {
		if(!Util.isOnline(this)){
			Snackbar.make(scrollView,getString(R.string.error_server_connection_problem),Snackbar.LENGTH_LONG).show();
			return;
		}
		
		loginLogin.setVisibility(View.GONE);
		loginDelimiter.setVisibility(View.GONE);
		loginCreateAccount.setVisibility(View.GONE);
		loginLoggingIn.setVisibility(View.VISIBLE);
		generatingKeysText.setVisibility(View.GONE);
		queryingSignupLinkText.setVisibility(View.VISIBLE);
		confirmingAccountText.setVisibility(View.GONE);
		fetchingNodesText.setVisibility(View.GONE);
		prepareNodesText.setVisibility(View.GONE);
		loginProgressBar.setVisibility(View.VISIBLE);
		log("querySignupLink");
		megaApi.querySignupLink(link, this);
	}
	
	
	public static void log(String message) {
		Util.log("LoginActivityLollipop", message);
	}
	
	@Override
	public void onDestroy(){
		if(megaApi != null)
		{	
			megaApi.removeRequestListener(this);
		}
		
		super.onDestroy();
	}
}
