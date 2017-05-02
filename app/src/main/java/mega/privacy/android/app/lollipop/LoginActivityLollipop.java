package mega.privacy.android.app.lollipop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mega.privacy.android.app.CameraSyncService;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.UploadService;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaTransfer;


public class LoginActivityLollipop extends AppCompatActivity {

	float scaleH, scaleW;
	float density;
	DisplayMetrics outMetrics;
	Display display;

	RelativeLayout relativeContainer;

	//Fragments
	TourFragmentLollipop tourFragment;
	LoginFragmentLollipop loginFragment;
	ChooseAccountFragmentLollipop chooseAccountFragment;
	CreateAccountFragmentLollipop createAccountFragment;
	ConfirmEmailFragmentLollipop confirmEmailFragment;

	ActionBar aB;
	int visibleFragment;

	static LoginActivityLollipop loginActivity;

	Intent intentReceived = null;

	DatabaseHandler dbH;

    Handler handler = new Handler();
	private MegaApiAndroid megaApi;

	private android.support.v7.app.AlertDialog alertDialogTransferOverquota;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate");
		super.onCreate(savedInstanceState);

		loginActivity = this;

		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
		display.getMetrics(outMetrics);
		density  = getResources().getDisplayMetrics().density;

		aB = getSupportActionBar();
		if(aB!=null){
			aB.hide();
		}

		scaleW = Util.getScaleW(outMetrics, density);
		scaleH = Util.getScaleH(outMetrics, density);

	    dbH = DatabaseHandler.getDbHandler(getApplicationContext());
		if (megaApi == null){
			megaApi = ((MegaApplication) getApplication()).getMegaApi();
		}

		setContentView(R.layout.activity_login);
		relativeContainer = (RelativeLayout) findViewById(R.id.relative_container_login);

		intentReceived = getIntent();
		if (intentReceived != null){
			visibleFragment = intentReceived.getIntExtra("visibleFragment", Constants.LOGIN_FRAGMENT);
			log("There is an intent! VisibleFragment: "+visibleFragment);
		}
		else{
			visibleFragment = Constants.LOGIN_FRAGMENT;
		}

//		visibleFragment = Constants.CHOOSE_ACCOUNT_FRAGMENT;
		showFragment(visibleFragment);
	}

	public void showSnackbar(String message){
		Snackbar snackbar = Snackbar.make(relativeContainer,message,Snackbar.LENGTH_LONG);
		TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		snackbarTextView.setMaxLines(5);
		snackbar.show();
	}

	public void showFragment(int visibleFragment){
		log("showFragment: "+visibleFragment);
		this.visibleFragment = visibleFragment;
		switch (visibleFragment){
			case Constants.LOGIN_FRAGMENT:{
				log("showLoginFragment");
				if(loginFragment==null){
					loginFragment = new LoginFragmentLollipop();
				}
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container_login, loginFragment);
				ft.commitNow();

//
//				getFragmentManager()
//						.beginTransaction()
//						.attach(loginFragment)
//						.commit();
				break;
			}
			case Constants.CHOOSE_ACCOUNT_FRAGMENT:{
				log("Show CHOOSE_ACCOUNT_FRAGMENT");

				if(chooseAccountFragment==null){
					chooseAccountFragment = new ChooseAccountFragmentLollipop();
				}

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container_login, chooseAccountFragment);
				ft.commitNow();
				break;
			}
			case Constants.CREATE_ACCOUNT_FRAGMENT:{

				if(createAccountFragment==null){
					createAccountFragment = new CreateAccountFragmentLollipop();
				}

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container_login, createAccountFragment);
				ft.commitNow();
				break;
			}
			case Constants.TOUR_FRAGMENT:{
				log("Show TOUR_FRAGMENT");

				if(tourFragment==null){
					tourFragment = new TourFragmentLollipop();
				}

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container_login, tourFragment);
				ft.commitNow();
				break;
			}
			case Constants.CONFIRM_EMAIL_FRAGMENT:{

				if(confirmEmailFragment==null){
					confirmEmailFragment = new ConfirmEmailFragmentLollipop();
				}

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container_login, confirmEmailFragment);
				ft.commitNow();
				FragmentManager fragmentManager = getSupportFragmentManager();
				fragmentManager.executePendingTransactions();
				break;
			}
		}
	}

	public void showTransferOverquotaDialog(){
		log("showTransferOverquotaDialog");

		android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);

		LayoutInflater inflater = this.getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.transfer_overquota_layout, null);
		dialogBuilder.setView(dialogView);

		TextView title = (TextView) dialogView.findViewById(R.id.transfer_overquota_title);
		title.setText(getString(R.string.title_depleted_transfer_overquota));

		ImageView icon = (ImageView) dialogView.findViewById(R.id.image_transfer_overquota);
		icon.setImageDrawable(getDrawable(R.drawable.transfer_quota_empty));

		TextView text = (TextView) dialogView.findViewById(R.id.text_transfer_overquota);
		text.setText(getString(R.string.text_depleted_transfer_overquota));

		Button continueButton = (Button) dialogView.findViewById(R.id.transfer_overquota_button_dissmiss);
		continueButton.setText(getString(R.string.login_text));

		Button paymentButton = (Button) dialogView.findViewById(R.id.transfer_overquota_button_payment);
		paymentButton.setText(getString(R.string.continue_without_account_transfer_overquota));


		alertDialogTransferOverquota = dialogBuilder.create();

		continueButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				alertDialogTransferOverquota.dismiss();
			}

		});

		paymentButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				alertDialogTransferOverquota.dismiss();
			}

		});

		alertDialogTransferOverquota.setCancelable(false);
		alertDialogTransferOverquota.setCanceledOnTouchOutside(false);
		alertDialogTransferOverquota.show();
	}

	public void stopCameraSyncService(){
		log("stopCameraSyncService");
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

	public void startCameraSyncService(boolean firstTimeCam, int time){
		log("startCameraSyncService");
		Intent intent = null;
		if(firstTimeCam){
			intent = new Intent(this,ManagerActivityLollipop.class);
			intent.putExtra("firstTimeCam", true);
			startActivity(intent);
			finish();
		}
		else{
			log("Enciendo el servicio de la camara");
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					log("Now I start the service");
					startService(new Intent(getApplicationContext(), CameraSyncService.class));
				}
			}, time);
		}
	}

	public void showConfirmationCancelAllTransfers (){
		log("showConfirmationCancelAllTransfers");

		setIntent(null);
		//Show confirmation message
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						log("Pressed button positive to cancel transfer");
						if (megaApi != null){
							megaApi.cancelTransfers(MegaTransfer.TYPE_DOWNLOAD);
						}
						else{
							log("megaAPI is null");
						}

						break;

					case DialogInterface.BUTTON_NEGATIVE:
						break;
				}
			}
		};

		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
//		builder.setTitle(getResources().getString(R.string.cancel_transfer_title));

		builder.setMessage(getResources().getString(R.string.cancel_all_transfer_confirmation));
		builder.setPositiveButton(R.string.general_cancel, dialogClickListener);
		builder.setNegativeButton(R.string.general_dismiss, dialogClickListener);

		builder.show();
	}

	@Override
	public void onBackPressed() {
		log("onBackPressed");

		int valueReturn = -1;

		switch (visibleFragment){
			case Constants.LOGIN_FRAGMENT:{
				if(loginFragment!=null){
					valueReturn = loginFragment.onBackPressed();
				}
				break;
			}
			case Constants.CREATE_ACCOUNT_FRAGMENT:{
				showFragment(Constants.TOUR_FRAGMENT);
				break;
			}
			case Constants.TOUR_FRAGMENT:{
				valueReturn=0;
				break;
			}
			case Constants.CONFIRM_EMAIL_FRAGMENT:{
				valueReturn=0;
				break;
			}
			case Constants.CHOOSE_ACCOUNT_FRAGMENT:{
				//nothing to do
				break;
			}
		}

		if (valueReturn == 0) {
			super.onBackPressed();
		}
	}

	@Override
	public void onResume() {
		log("onResume");
		super.onResume();

		Intent intent = getIntent();

		if (intent != null){
			if (intent.getAction() != null){
				if (intent.getAction().equals(Constants.ACTION_CANCEL_CAM_SYNC)){
					log("ACTION_CANCEL_CAM_SYNC");
					Intent tempIntent = null;
					String title = null;
					String text = null;
					if (intent.getAction().equals(Constants.ACTION_CANCEL_CAM_SYNC)){
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
				else if (intent.getAction().equals(Constants.ACTION_CANCEL_DOWNLOAD)){
					showConfirmationCancelAllTransfers();
				}
				intent.setAction(null);
			}
		}

		setIntent(null);
	}

	public void showConfirmationEnableLogs(){
		log("showConfirmationEnableLogs");

		if(loginFragment!=null){
			loginFragment.numberOfClicks = 0;
		}
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						enableLogs();
						break;

					case DialogInterface.BUTTON_NEGATIVE:

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

		builder.setMessage(R.string.enable_log_text_dialog).setPositiveButton(R.string.general_enable, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show().setCanceledOnTouchOutside(false);
	}

	public void enableLogs(){
		log("enableLogs");

		dbH.setFileLogger(true);
		Util.setFileLogger(true);
		MegaApiAndroid.setLogLevel(MegaApiAndroid.LOG_LEVEL_MAX);
		showSnackbar(getString(R.string.settings_enable_logs));
		log("App Version: " + Util.getVersion(this));
	}


//	public void onNewIntent(Intent intent){
//		if (intent != null && Constants.ACTION_CONFIRM.equals(intent.getAction())) {
//			loginFragment.handleConfirmationIntent(intent);
//		}
//	}

	public static void log(String message) {
		Util.log("LoginActivityLollipop", message);
	}

}
