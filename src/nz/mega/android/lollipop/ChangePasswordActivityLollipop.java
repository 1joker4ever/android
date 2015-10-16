package nz.mega.android.lollipop;

import java.util.Locale;

import nz.mega.android.DatabaseHandler;
import nz.mega.android.MegaApplication;
import nz.mega.android.R;
import nz.mega.android.UserCredentials;
import nz.mega.android.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;


@SuppressLint("NewApi")
public class ChangePasswordActivityLollipop extends PinActivityLollipop implements OnClickListener, MegaRequestListenerInterface{
	
	ChangePasswordActivityLollipop changePasswordActivity = this;

	private ProgressDialog progress;
	
	float scaleH, scaleW;
	float density;
	DisplayMetrics outMetrics;
	Display display;
	
	private MegaApiAndroid megaApi;
	
	private EditText oldPasswordView, newPassword1View, newPassword2View;
	private TextView changePasswordButton;
	ImageView loginThreeDots;
	Switch loginSwitch;
	TextView loginABC;
    RelativeLayout fragmentContainer;
	
	private ActionBar aB;
	Toolbar tB;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
		//Set toolbar
		tB = (Toolbar) findViewById(R.id.toolbar_change_pass);
		setSupportActionBar(tB);
		aB = getSupportActionBar();
//				aB.setLogo(R.drawable.ic_arrow_back_black);
		aB.setDisplayHomeAsUpEnabled(true);
		aB.setDisplayShowHomeEnabled(true);
		aB.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		aB.setDisplayShowTitleEnabled(false);
		aB.setTitle(getString(R.string.my_account_change_password_title));
		
        fragmentContainer = (RelativeLayout) findViewById(R.id.fragment_container_change_pass);
		
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
		
		oldPasswordView = (EditText) findViewById(R.id.change_password_oldPassword);
		android.view.ViewGroup.LayoutParams paramsb1 = oldPasswordView.getLayoutParams();		
		paramsb1.width = Util.scaleWidthPx(280, outMetrics);		
		oldPasswordView.setLayoutParams(paramsb1);
		//Left margin
		LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams)oldPasswordView.getLayoutParams();
		textParams.setMargins(Util.scaleWidthPx(30, outMetrics), 0, 0, Util.scaleHeightPx(10, outMetrics)); 
		oldPasswordView.setLayoutParams(textParams);		
		
		newPassword1View = (EditText) findViewById(R.id.change_password_newPassword1);
		newPassword1View.setLayoutParams(paramsb1);
		newPassword1View.setLayoutParams(textParams);
		
		newPassword2View = (EditText) findViewById(R.id.change_password_newPassword2);
		newPassword2View.setLayoutParams(paramsb1);
		newPassword2View.setLayoutParams(textParams);
		
		loginThreeDots = (ImageView) findViewById(R.id.change_pass_three_dots);
		LinearLayout.LayoutParams textThreeDots = (LinearLayout.LayoutParams)loginThreeDots.getLayoutParams();
		textThreeDots.setMargins(Util.scaleWidthPx(0, outMetrics), 0, Util.scaleWidthPx(10, outMetrics), 0); 
		loginThreeDots.setLayoutParams(textThreeDots);
		
		loginABC = (TextView) findViewById(R.id.ABC_change_pass);
		
		loginSwitch = (Switch) findViewById(R.id.switch_change_pass);
		LinearLayout.LayoutParams switchParams = (LinearLayout.LayoutParams)loginSwitch.getLayoutParams();
		switchParams.setMargins(0, 0, Util.scaleWidthPx(10, outMetrics), 0); 
		loginSwitch.setLayoutParams(switchParams);
		loginSwitch.setChecked(false);
		
		loginSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked){
					oldPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					oldPasswordView.setSelection(oldPasswordView.getText().length());
					
					newPassword1View.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					newPassword1View.setSelection(newPassword1View.getText().length());
					
					newPassword2View.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					newPassword2View.setSelection(newPassword2View.getText().length());
				}else{
					oldPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					oldPasswordView.setSelection(oldPasswordView.getText().length());
					
					newPassword1View.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					newPassword1View.setSelection(newPassword1View.getText().length());
					
					newPassword2View.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					newPassword2View.setSelection(newPassword2View.getText().length());
			    }				
			}
		});		
				
		changePasswordButton = (TextView) findViewById(R.id.change_password_password);
		
		changePasswordButton.setText(getString(R.string.my_account_change_password).toUpperCase(Locale.getDefault()));
		android.view.ViewGroup.LayoutParams paramsbLogin = changePasswordButton.getLayoutParams();		
		paramsbLogin.height = Util.scaleHeightPx(48, outMetrics);
		paramsbLogin.width = Util.scaleWidthPx(63, outMetrics);
		changePasswordButton.setLayoutParams(paramsbLogin);
		//Margin
		LinearLayout.LayoutParams textParamsLogin = (LinearLayout.LayoutParams)changePasswordButton.getLayoutParams();
		textParamsLogin.setMargins(Util.scaleWidthPx(35, outMetrics), Util.scaleHeightPx(20, outMetrics), 0, Util.scaleHeightPx(80, outMetrics)); 
		changePasswordButton.setLayoutParams(textParamsLogin);		
		
		changePasswordButton.setOnClickListener(this);
		
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.my_account_changing_password));
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		
		megaApi = ((MegaApplication)getApplication()).getMegaApi();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
		    case android.R.id.home:{
		    	finish();
		    	return true;
		    }
		}	    
	    return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.change_password_password:{
				onChangePasswordClick();
				break;
			}
		}
	}
	
	public void onChangePasswordClick(){
		if(!Util.isOnline(this))
		{
			Snackbar.make(fragmentContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
			return;
		}
		
		if (!validateForm()) {
			return;
		}
		
		InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(oldPasswordView.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(newPassword1View.getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(newPassword2View.getWindowToken(), 0);
		
		final String oldPassword = oldPasswordView.getText().toString();
		String newPassword1 = newPassword1View.getText().toString();
		String newPassword2 = newPassword2View.getText().toString();
		
		if (!newPassword1.equals(newPassword2)){
			log("no new password repeat");
			newPassword2View.setError(getString(R.string.my_account_change_password_dont_match));
			return;
		}
		
//		if (!checkPassword(oldPassword, newPassword1, newPassword2)){
//
//		}
		
		final String newPassword = newPassword1;

		
		progress.setMessage(getString(R.string.my_account_changing_password));
		progress.show();
		
		DatabaseHandler dbH = DatabaseHandler.getDbHandler(getApplicationContext());
//		DatabaseHandler dbH = new DatabaseHandler(getApplicationContext());
		final UserCredentials oldCredentials = dbH.getCredentials();
		
		String currentEmail = oldCredentials.getEmail();
		
//		new HashTask().execute(currentEmail, newPassword, oldPassword);
		changePassword(currentEmail, newPassword, oldPassword);
	}
	
//	private boolean checkPassword (String oldPassword, String newPassword1, String newPassword2){
//		log(newPassword1);
//		log(newPassword2);
//		if (!newPassword1.equals(newPassword2)){
//			log("no new password repeat");
//			return false;
//		}
//		DatabaseHandler dbH = new DatabaseHandler(getApplicationContext()); 
//		UserCredentials cred = dbH.getCredentials();
//		String email = cred.getEmail();
//		new CheckTask();
//		String privateKey = megaApi.getBase64PwKey(oldPassword);
//		String publicKey = megaApi.getStringHash(privateKey, cred.getEmail());
//		
//		if (!privateKey.equals(cred.getPrivateKey()) || !publicKey.equals(cred.getPublicKey())){
//			log("no old password");
//			return false;
//		}
//			
//		return true;
//	}
	
	/*
	 * Validate old password and new passwords 
	 */
	private boolean validateForm() {
		String oldPasswordError = getOldPasswordError();
		String newPassword1Error = getNewPassword1Error();
		String newPassword2Error = getNewPassword2Error();

		oldPasswordView.setError(oldPasswordError);
		newPassword1View.setError(newPassword1Error);
		newPassword2View.setError(newPassword2Error);

		if (oldPasswordError != null) {
			oldPasswordView.requestFocus();
			return false;
		}
		else if(newPassword1Error != null) {
			newPassword1View.requestFocus();
			return false;
		}
		else if(newPassword2Error != null) {
			newPassword2View.requestFocus();
			return false;
		}
		return true;
	}
	
	/*
	 * Validate old password
	 */
	private String getOldPasswordError() {
		String value = oldPasswordView.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		return null;
	}
	
	/*
	 * Validate new password1
	 */
	private String getNewPassword1Error() {
		String value = newPassword1View.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		return null;
	}
	
	/*
	 * Validate new password2
	 */
	private String getNewPassword2Error() {
		String value = newPassword2View.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		return null;
	}
	
	private void changePassword (String email, String newPassword, String oldPassword){
		megaApi.changePassword(oldPassword, newPassword, this);
	}
	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getName());
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestFinish");
		
		if (request.getType() == MegaRequest.TYPE_CHANGE_PW){
			if (e.getErrorCode() != MegaError.API_OK){
				log("e.getErrorCode = " + e.getErrorCode() + "__ e.getErrorString = " + e.getErrorString());
				
				try{ 
					progress.dismiss();
				} catch(Exception ex) {};

				Snackbar.make(fragmentContainer, getString(R.string.my_account_change_password_error_2), Snackbar.LENGTH_LONG).show();				
			}
			else{
				log("pass changed");
				try{ 
					progress.dismiss();
				} catch(Exception ex) {};
				
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				finish();				
				Snackbar.make(fragmentContainer, getString(R.string.my_account_change_password_OK), Snackbar.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError: " + request.getName());
	}
	
	public static void log(String message) {
		Util.log("ChangePasswordActivityLollipop", message);
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
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
