package mega.privacy.android.app.lollipop;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.EditTextPIN;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;


@SuppressLint("NewApi")
public class ChangePasswordActivityLollipop extends PinActivityLollipop implements OnClickListener, MegaRequestListenerInterface{
	
	ChangePasswordActivityLollipop changePasswordActivity = this;

	private ProgressDialog progress;
	
	float scaleH, scaleW;
	float density;
	DisplayMetrics outMetrics;
	Display display;
	
	private MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;

	boolean changePassword = true;
	
	private EditText newPassword1View, newPassword2View;
	private RelativeLayout newPassword1ErrorView, newPassword2ErrorView;
	private TextView newPassword1ErrorText, newPassword2ErrorText;
	private Button changePasswordButton;
    private RelativeLayout fragmentContainer;
	private TextView title;
	private String linkToReset;
	private String mk;

	private ActionBar aB;
	Toolbar tB;

	private Drawable newPassword_background;
	private Drawable newPassword2_background;

	private ImageView toggleButtonNewPasswd;
	private ImageView toggleButtonNewPasswd2;
	private boolean passwdVisibility;
	private LinearLayout containerPasswdElements;
	private ImageView firstShape;
	private ImageView secondShape;
	private ImageView tirdShape;
	private ImageView fourthShape;
	private ImageView fifthShape;
	private TextView passwdType;
	private TextView passwdAdvice;
	private boolean passwdValid;

	LinearLayout changePasswordVerificationLayout;
	InputMethodManager imm;
	private EditTextPIN firstPin;
	private EditTextPIN secondPin;
	private EditTextPIN thirdPin;
	private EditTextPIN fourthPin;
	private EditTextPIN fifthPin;
	private EditTextPIN sixthPin;
	private StringBuilder sb = new StringBuilder();
	private String pin = null;
	private TextView pinError;
	private RelativeLayout lostYourDeviceButton;
	private Button verifyButton;

	private boolean isErrorShown = false;
	private boolean is2FAEnabled = false;
	private boolean firstTime = true;
	private boolean isOnVerifyScreen = false;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
        fragmentContainer = (RelativeLayout) findViewById(R.id.fragment_container_change_pass);
		megaApi = ((MegaApplication)getApplication()).getMegaApi();

		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;
		
	    scaleW = Util.getScaleW(outMetrics, density);
	    scaleH = Util.getScaleH(outMetrics, density);

		title = (TextView) findViewById(R.id.title_change_pass);

		toggleButtonNewPasswd = (ImageView) findViewById(R.id.toggle_button_new_passwd);
		toggleButtonNewPasswd.setOnClickListener(this);
		toggleButtonNewPasswd2 = (ImageView) findViewById(R.id.toggle_button_new_passwd2);
		toggleButtonNewPasswd2.setOnClickListener(this);
		passwdVisibility = false;
		passwdValid = false;

		containerPasswdElements = (LinearLayout) findViewById(R.id.container_passwd_elements);
		containerPasswdElements.setVisibility(View.GONE);
		firstShape = (ImageView) findViewById(R.id.shape_passwd_first);
		secondShape = (ImageView) findViewById(R.id.shape_passwd_second);
		tirdShape = (ImageView) findViewById(R.id.shape_passwd_third);
		fourthShape = (ImageView) findViewById(R.id.shape_passwd_fourth);
		fifthShape = (ImageView) findViewById(R.id.shape_passwd_fifth);
		passwdType = (TextView) findViewById(R.id.password_type);
		passwdAdvice = (TextView) findViewById(R.id.password_advice_text);

		newPassword1View = (EditText) findViewById(R.id.change_password_newPassword1);
		newPassword1View.getBackground().clearColorFilter();
		newPassword_background = newPassword1View.getBackground().mutate().getConstantState().newDrawable();
		newPassword1ErrorView = (RelativeLayout) findViewById(R.id.login_newPassword1_text_error);
		newPassword1ErrorText = (TextView) findViewById(R.id.login_newPassword1_text_error_text);

		newPassword1View.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				log("onTextChanged: " + s.toString() + "_ " + start + "__" + before + "__" + count);
				if (s != null){
					if (s.length() > 0) {
						String temp = s.toString();
						containerPasswdElements.setVisibility(View.VISIBLE);

						checkPasswordStrenght(temp.trim());
					}
					else{
						passwdValid = false;
						containerPasswdElements.setVisibility(View.GONE);
					}
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				quitError(newPassword1View);
			}
		});

		newPassword1View.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					toggleButtonNewPasswd.setVisibility(View.VISIBLE);
					toggleButtonNewPasswd.setImageDrawable(ContextCompat.getDrawable(changePasswordActivity, R.drawable.ic_b_shared_read));
				}
				else {
					toggleButtonNewPasswd.setVisibility(View.GONE);
					passwdVisibility = false;
					showHidePassword(R.id.toggle_button_new_passwd);
				}
			}
		});
		
		newPassword2View = (EditText) findViewById(R.id.change_password_newPassword2);
		newPassword2View.getBackground().clearColorFilter();
		newPassword2ErrorView = (RelativeLayout) findViewById(R.id.login_newPassword2_text_error);
		newPassword2ErrorText = (TextView) findViewById(R.id.login_newPassword2_text_error_text);

		newPassword2_background = newPassword2View.getBackground().mutate().getConstantState().newDrawable();

		newPassword2View.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				quitError(newPassword2View);
			}
		});

		newPassword2View.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					toggleButtonNewPasswd2.setVisibility(View.VISIBLE);
					toggleButtonNewPasswd2.setImageDrawable(ContextCompat.getDrawable(changePasswordActivity, R.drawable.ic_b_shared_read));
				}
				else {
					toggleButtonNewPasswd2.setVisibility(View.GONE);
					passwdVisibility = false;
					showHidePassword(R.id.toggle_button_new_passwd2);
				}
			}
		});

				
		changePasswordButton = (Button) findViewById(R.id.action_change_password);
		changePasswordButton.setOnClickListener(this);
		
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.my_account_changing_password));
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);

		tB  =(Toolbar) findViewById(R.id.toolbar);
		hideAB();

		changePasswordVerificationLayout = (LinearLayout) findViewById(R.id.change_password_2fa);
		changePasswordVerificationLayout.setVisibility(View.GONE);
		lostYourDeviceButton = (RelativeLayout) findViewById(R.id.lost_authentication_device);
		lostYourDeviceButton.setOnClickListener(this);
		verifyButton = (Button) findViewById(R.id.button_verify_2fa);
		verifyButton.setOnClickListener(this);
		pinError = (TextView) findViewById(R.id.pin_2fa_error_change_password);
		pinError.setVisibility(View.GONE);

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		firstPin = (EditTextPIN) findViewById(R.id.pin_first_change_password);
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

					if (firstTime){
						secondPin.setText("");
						thirdPin.setText("");
						fourthPin.setText("");
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else  {
						permitVerify();
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
					verifyButton.setVisibility(View.GONE);
				}
			}
		});

		secondPin = (EditTextPIN) findViewById(R.id.pin_second_change_password);
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

					if (firstTime) {
						thirdPin.setText("");
						fourthPin.setText("");
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else  {
						permitVerify();
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
					verifyButton.setVisibility(View.GONE);
				}
			}
		});

		thirdPin = (EditTextPIN) findViewById(R.id.pin_third_change_password);
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

					if (firstTime) {
						fourthPin.setText("");
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else  {
						permitVerify();
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
					verifyButton.setVisibility(View.GONE);
				}
			}
		});

		fourthPin = (EditTextPIN) findViewById(R.id.pin_fouth_change_password);
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

					if (firstTime) {
						fifthPin.setText("");
						sixthPin.setText("");
					}
					else  {
						permitVerify();
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
					verifyButton.setVisibility(View.GONE);
				}
			}
		});

		fifthPin = (EditTextPIN) findViewById(R.id.pin_fifth_change_password);
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

					if (firstTime) {
						sixthPin.setText("");
					}
					else  {
						permitVerify();
					}
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
					verifyButton.setVisibility(View.GONE);
				}
			}
		});

		sixthPin = (EditTextPIN) findViewById(R.id.pin_sixth_change_password);
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

					permitVerify();
				}
				else {
					if (isErrorShown){
						verifyQuitError();
					}
					verifyButton.setVisibility(View.GONE);
				}
			}
		});
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		Intent intentReceived = getIntent();
		if (intentReceived != null) {
			log("There is an intent!");
			if(intentReceived.getAction()!=null){
				if (getIntent().getAction().equals(Constants.ACTION_RESET_PASS_FROM_LINK)) {
					log("ACTION_RESET_PASS_FROM_LINK");
					changePassword=false;
					linkToReset = getIntent().getDataString();
					if (linkToReset == null) {
						log("link is NULL - close activity");
						finish();
					}
					mk = getIntent().getStringExtra("MK");
					if(mk==null){
						log("MK is NULL - close activity");
						Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
					}

					title.setText(getString(R.string.title_enter_new_password));
				}
				if (getIntent().getAction().equals(Constants.ACTION_RESET_PASS_FROM_PARK_ACCOUNT)) {
					changePassword=false;
					log("ACTION_RESET_PASS_FROM_PARK_ACCOUNT");
					linkToReset = getIntent().getDataString();
					if (linkToReset == null) {
						log("link is NULL - close activity");
						Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
					}
					mk = null;

					title.setText(getString(R.string.title_enter_new_password));
				}
			}
		}
		((MegaApplication) getApplication()).sendSignalPresenceActivity();
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
		firstTime = false;
		isErrorShown = true;
		verifyButton.setVisibility(View.GONE);
		pinError.setVisibility(View.VISIBLE);
		firstPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		secondPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		thirdPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		fourthPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		fifthPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
		sixthPin.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
	}

	void permitVerify(){
		if (firstPin.length() == 1 && secondPin.length() == 1 && thirdPin.length() == 1 && fourthPin.length() == 1 && fifthPin.length() == 1 && sixthPin.length() == 1){
			if (!isErrorShown) {
				verifyButton.setVisibility(View.VISIBLE);
			}
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
		}
	}

	void hideKeyboard(){

		View v = getCurrentFocus();
		if (v != null){
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		((MegaApplication) getApplication()).sendSignalPresenceActivity();
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
		    case android.R.id.home:{
		    	if (isOnVerifyScreen){
		    		isOnVerifyScreen = false;
		    		fragmentContainer.setVisibility(View.VISIBLE);
		    		hideAB();
		    		changePasswordVerificationLayout.setVisibility(View.GONE);
				}
				else {
					finish();
				}
		    	return true;
		    }
		}	    
	    return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		log("onClick");
		((MegaApplication) getApplication()).sendSignalPresenceActivity();
		switch(v.getId()){
			case R.id.action_change_password: {
				if (changePassword) {
					log("ok proceed to change");
					onChangePasswordClick();
				} else {
					log("reset pass on click");
					if (linkToReset == null) {
						log("link is NULL");
						Util.showAlert(this, getString(R.string.email_verification_text_error), getString(R.string.general_error_word));
					} else {
						if (mk == null) {
							log("proceed to park account");
							onResetPasswordClick(false);
						} else {
							log("ok proceed to reset");
							onResetPasswordClick(true);
						}
					}
				}
				break;
			}
			case R.id.toggle_button_new_passwd: {
				if (passwdVisibility) {
					toggleButtonNewPasswd.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_b_shared_read));
					passwdVisibility = false;
					showHidePassword(R.id.toggle_button_new_passwd);
				}
				else {
					toggleButtonNewPasswd.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_b_see));
					passwdVisibility = true;
					showHidePassword(R.id.toggle_button_new_passwd);
				}
				break;
			}
			case R.id.toggle_button_new_passwd2: {
				if (passwdVisibility) {
					toggleButtonNewPasswd2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_b_shared_read));
					passwdVisibility = false;
					showHidePassword(R.id.toggle_button_new_passwd2);
				}
				else {
					toggleButtonNewPasswd2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_b_see));
					passwdVisibility = true;
					showHidePassword(R.id.toggle_button_new_passwd2);
				}
				break;
			}
			case R.id.button_verify_2fa: {
				changePassword(newPassword1View.getText().toString());
				break;
			}
//			case R.id.cancel_change_password:{
//				changePasswordActivity.finish();
//				break;
//			}
		}
	}

	public void checkPasswordStrenght(String s) {

		if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_VERYWEAK || s.length() < 4){
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				firstShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_very_weak));
				secondShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
				tirdShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fourthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fifthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
			} else{
				firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_very_weak));
				secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
				tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			}

			passwdType.setText(getString(R.string.pass_very_weak));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.login_warning));

			passwdAdvice.setText(getString(R.string.passwd_weak));

			passwdValid = false;
		}
		else if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_WEAK){
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				firstShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_weak));
				secondShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_weak));
				tirdShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fourthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fifthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
			} else{
				firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_weak));
				secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_weak));
				tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			}

			passwdType.setText(getString(R.string.pass_weak));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.pass_weak));

			passwdAdvice.setText(getString(R.string.passwd_weak));

			passwdValid = true;
		}
		else if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_MEDIUM){
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				firstShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
				secondShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
				tirdShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
				fourthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fifthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
			} else{
				firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
				secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
				tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_medium));
				fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
				fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			}

			passwdType.setText(getString(R.string.pass_medium));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.green_unlocked_rewards));

			passwdAdvice.setText(getString(R.string.passwd_medium));

			passwdValid = true;
		}
		else if (megaApi.getPasswordStrength(s) == MegaApiJava.PASSWORD_STRENGTH_GOOD){
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				firstShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				secondShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				tirdShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				fourthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				fifthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_password));
			} else{
				firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_good));
				fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_password));
			}

			passwdType.setText(getString(R.string.pass_good));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.pass_good));

			passwdAdvice.setText(getString(R.string.passwd_good));

			passwdValid = true;
		}
		else {
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
				firstShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				secondShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				tirdShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				fourthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				fifthShape.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
			} else{
				firstShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				secondShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				tirdShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				fourthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
				fifthShape.setBackground(ContextCompat.getDrawable(this, R.drawable.passwd_strong));
			}

			passwdType.setText(getString(R.string.pass_strong));
			passwdType.setTextColor(ContextCompat.getColor(this, R.color.blue_unlocked_rewards));

			passwdAdvice.setText(getString(R.string.passwd_strong));

			passwdValid = true;
		}
	}

	public void showHidePassword (int type) {
		if(!passwdVisibility){
			switch (type) {
				case R.id.toggle_button_new_passwd: {
					newPassword1View.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					newPassword1View.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
					newPassword1View.setSelection(newPassword1View.getText().length());
					break;
				}
				case R.id.toggle_button_new_passwd2: {
					newPassword2View.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					newPassword2View.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
					newPassword2View.setSelection(newPassword2View.getText().length());
					break;
				}
			}
		}else{
			switch (type) {
				case R.id.toggle_button_new_passwd: {
					newPassword1View.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					newPassword1View.setSelection(newPassword1View.getText().length());
					break;
				}
				case R.id.toggle_button_new_passwd2: {
					newPassword2View.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					newPassword2View.setSelection(newPassword2View.getText().length());
					break;
				}
			}
		}
	}

	public void onResetPasswordClick(boolean hasMk){
		log("onResetPasswordClick");

		if(!Util.isOnline(this))
		{
			Snackbar.make(fragmentContainer, getString(R.string.error_server_connection_problem), Snackbar.LENGTH_LONG).show();
			return;
		}

		((MegaApplication) getApplication()).sendSignalPresenceActivity();

		if (!validateForm(false)) {
			return;
		}

		imm.hideSoftInputFromWindow(newPassword1View.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(newPassword2View.getWindowToken(), 0);

		String newPassword1 = newPassword1View.getText().toString();
		String newPassword2 = newPassword2View.getText().toString();

//		if (!newPassword1.equals(newPassword2)){
//			log("no new password repeat");
////			newPassword2View.setError(getString(R.string.my_account_change_password_dont_match));
//			setError(newPassword2View, getString(R.string.my_account_change_password_dont_match));
//			return;
//		}

		final String newPassword = newPassword1;

		progress.setMessage(getString(R.string.my_account_changing_password));
		progress.show();

		if(hasMk){
			log("reset with mk");
			megaApi.confirmResetPassword(linkToReset, newPassword, mk, this);
		}
		else{
			megaApi.confirmResetPassword(linkToReset, newPassword, null, this);
		}
	}
	
	public void onChangePasswordClick(){
		log("onChangePasswordClick");
		if(!Util.isOnline(this))
		{
			showSnackbar(getString(R.string.error_server_connection_problem));
			return;
		}

		if (!validateForm(true)) {
			return;
		}

        imm.hideSoftInputFromWindow(newPassword1View.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(newPassword2View.getWindowToken(), 0);
		

		String newPassword1 = newPassword1View.getText().toString();
		String newPassword2 = newPassword2View.getText().toString();

//		if (!newPassword1.equals(newPassword2)){
//			log("no new password repeat");
////			newPassword2View.setError(getString(R.string.my_account_change_password_dont_match));
//			setError(newPassword2View, getString(R.string.my_account_change_password_dont_match));
//			return;
//		}
		
		megaApi.multiFactorAuthCheck(megaApi.getMyEmail(), this);
//		changePassword(newPassword1);
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
	private boolean validateForm(boolean withOldPass) {
		if(withOldPass){
			String newPassword1Error = getNewPassword1Error();
			String newPassword2Error = getNewPassword2Error();

//			newPassword1View.setError(newPassword1Error);
			setError(newPassword1View, newPassword1Error);
//			newPassword2View.setError(newPassword2Error);
			setError(newPassword2View, newPassword2Error);

			if(newPassword1Error != null) {
				newPassword1View.requestFocus();
				return false;
			}
			else if(newPassword2Error != null) {
				newPassword2View.requestFocus();
				return false;
			}
		}
		else{
			String newPassword1Error = getNewPassword1Error();
			String newPassword2Error = getNewPassword2Error();

//			newPassword1View.setError(newPassword1Error);
			setError(newPassword1View, newPassword1Error);
//			newPassword2View.setError(newPassword2Error);
			setError(newPassword2View, newPassword2Error);

			if(newPassword1Error != null) {
				newPassword1View.requestFocus();
				return false;
			}
			else if(newPassword2Error != null) {
				newPassword2View.requestFocus();
				return false;
			}
		}
		return true;
	}

	/*
	 * Validate new password1
	 */
	private String getNewPassword1Error() {
		String value = newPassword1View.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		else if (!passwdValid){
			containerPasswdElements.setVisibility(View.GONE);
			return getString(R.string.error_password);
		}
		return null;
	}
	
	/*
	 * Validate new password2
	 */
	private String getNewPassword2Error() {
		String value = newPassword2View.getText().toString();
		String confirm = newPassword1View.getText().toString();
		if (value.length() == 0) {
			return getString(R.string.error_enter_password);
		}
		else if (!value.equals(confirm)) {
			return getString(R.string.error_passwords_dont_match);
		}
		return null;
	}
	
	private void changePassword (String newPassword){
		log("changePassword");
		progress.setMessage(getString(R.string.my_account_changing_password));
		progress.show();

		if (is2FAEnabled){
			megaApi.multiFactorAuthChangePassword(null, newPassword, pin, this);
		}
		else {
			megaApi.changePassword(null, newPassword, this);
		}
	}
	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getName());
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		log("onRequestFinish");
		
		if (request.getType() == MegaRequest.TYPE_CHANGE_PW){
			log("TYPE_CHANGE_PW");
			if (e.getErrorCode() != MegaError.API_OK){
				log("e.getErrorCode = " + e.getErrorCode() + "__ e.getErrorString = " + e.getErrorString());
				
				try{ 
					progress.dismiss();
				} catch(Exception ex) {};

				//Intent to MyAccount
				Intent resetPassIntent = new Intent(this, ManagerActivityLollipop.class);
				if(e.getErrorCode()!=MegaError.API_OK){
					log("Error, request: "+e.getErrorString());
					if (e.getErrorCode() == MegaError.API_EFAILED || e.getErrorCode() == MegaError.API_EEXPIRED){
						if (is2FAEnabled){
							verifyShowError();
						}
					}
					else {
						showSnackbar(getString(R.string.email_verification_text_error));
					}
				}
				else{
					resetPassIntent.setAction(Constants.ACTION_PASS_CHANGED);
					log("General Error");
					resetPassIntent.putExtra("RESULT", -1);
					startActivity(resetPassIntent);
					finish();
				}
			}
			else{
				log("pass changed OK");
				try{ 
					progress.dismiss();
				} catch(Exception ex) {};
				
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				//Intent to MyAccount
				Intent resetPassIntent = new Intent(this, ManagerActivityLollipop.class);
				resetPassIntent.setAction(Constants.ACTION_PASS_CHANGED);
				resetPassIntent.putExtra("RESULT", 0);
				startActivity(resetPassIntent);
				finish();
			}
		}
		else if(request.getType() == MegaRequest.TYPE_CONFIRM_RECOVERY_LINK){
			log("TYPE_CONFIRM_RECOVERY_LINK");
			if(megaApi.getRootNode()==null) {
				log("Not logged in");
				if (e.getErrorCode() != MegaError.API_OK){
					log("e.getErrorCode = " + e.getErrorCode() + "__ e.getErrorString = " + e.getErrorString());

					try{
						progress.dismiss();
					} catch(Exception ex) {};

					//Intent to Login
					Intent resetPassIntent = new Intent(this, LoginActivityLollipop.class);
					resetPassIntent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
					resetPassIntent.setAction(Constants.ACTION_PASS_CHANGED);

					if(e.getErrorCode()==MegaError.API_EARGS){
						resetPassIntent.putExtra("RESULT", MegaError.API_EARGS);
					}
					else if(e.getErrorCode()==MegaError.API_EKEY){
						resetPassIntent.putExtra("RESULT", MegaError.API_EKEY);
					}
					else{
						resetPassIntent.putExtra("RESULT", -1);
					}

					startActivity(resetPassIntent);
					finish();
				}
				else{
					log("pass changed");
					try{
						progress.dismiss();
					} catch(Exception ex) {};

					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
					//Intent to Login
					Intent resetPassIntent = new Intent(this, LoginActivityLollipop.class);
					resetPassIntent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
					resetPassIntent.setAction(Constants.ACTION_PASS_CHANGED);
					resetPassIntent.putExtra("RESULT", 0);
					startActivity(resetPassIntent);
					finish();
				}
			}
			else {
				log("Logged IN");

				if (e.getErrorCode() != MegaError.API_OK){
					log("e.getErrorCode = " + e.getErrorCode() + "__ e.getErrorString = " + e.getErrorString());

					try{
						progress.dismiss();
					} catch(Exception ex) {};

					//Intent to Login
					Intent resetPassIntent = new Intent(this, ManagerActivityLollipop.class);
					resetPassIntent.setAction(Constants.ACTION_PASS_CHANGED);
					resetPassIntent.putExtra("RESULT", -1);
					startActivity(resetPassIntent);
					finish();
				}
				else{
					log("pass changed");
					try{
						progress.dismiss();
					} catch(Exception ex) {};

					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
					//Intent to Login
					Intent resetPassIntent = new Intent(this, ManagerActivityLollipop.class);
					resetPassIntent.setAction(Constants.ACTION_PASS_CHANGED);
					resetPassIntent.putExtra("RESULT", 0);
					startActivity(resetPassIntent);
					finish();
				}

			}
		}
		else if (request.getType() == MegaRequest.TYPE_MULTI_FACTOR_AUTH_CHECK){
			if (e.getErrorCode() == MegaError.API_OK){
				if (request.getFlag()){
					is2FAEnabled = true;
					fragmentContainer.setVisibility(View.GONE);
					showAB();
					isOnVerifyScreen = true;
					changePasswordVerificationLayout.setVisibility(View.VISIBLE);
				}
				else {
					is2FAEnabled = false;
					changePassword(newPassword1View.getText().toString());
				}
			}
		}
	}

	private void setError(final EditText editText, String error){
		log("setError");
		if(error == null || error.equals("")){
			return;
		}
		switch (editText.getId()){

			case R.id.change_password_newPassword1:{
				newPassword1ErrorView.setVisibility(View.VISIBLE);
				newPassword1ErrorText.setText(error);
				PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
//                et_user.getBackground().mutate().setColorFilter(porterDuffColorFilter);
				Drawable background = newPassword_background.mutate().getConstantState().newDrawable();
				background.setColorFilter(porterDuffColorFilter);
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					newPassword1View.setBackgroundDrawable(background);
				} else{
					newPassword1View.setBackground(background);
				}
				break;
			}
			case R.id.change_password_newPassword2:{
				newPassword2ErrorView.setVisibility(View.VISIBLE);
				newPassword2ErrorText.setText(error);
				PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
//                et_user.getBackground().mutate().setColorFilter(porterDuffColorFilter);
				Drawable background = newPassword2_background.mutate().getConstantState().newDrawable();
				background.setColorFilter(porterDuffColorFilter);
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
					newPassword2View.setBackgroundDrawable(background);
				} else{
					newPassword2View.setBackground(background);
				}
				break;
			}
		}
	}

	private void quitError(EditText editText){
		switch (editText.getId()){
			case R.id.change_password_newPassword1:{
				if(newPassword1ErrorView.getVisibility() != View.GONE){
					newPassword1ErrorView.setVisibility(View.GONE);
					if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
						newPassword1View.setBackgroundDrawable(newPassword_background);
					} else{
						newPassword1View.setBackground(newPassword_background);
					}
				}
				break;
			}
			case R.id.change_password_newPassword2:{
				if(newPassword2ErrorView.getVisibility() != View.GONE){
					newPassword2ErrorView.setVisibility(View.GONE);
					if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
						newPassword2View.setBackgroundDrawable(newPassword2_background);
					} else{
						newPassword2View.setBackground(newPassword2_background);
					}
				}
				break;
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

	public void showSnackbar(String s){
		log("showSnackbar");
		Snackbar snackbar = Snackbar.make(fragmentContainer, s, Snackbar.LENGTH_LONG);
		TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		snackbarTextView.setMaxLines(5);
		snackbar.show();
	}

	void showAB(){
		setSupportActionBar(tB);
		if (aB == null){
			aB = getSupportActionBar();
		}
		aB.show();
		aB.setHomeButtonEnabled(true);
		aB.setDisplayHomeAsUpEnabled(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_dark_primary_color));
		}
	}

	void hideAB(){
		if (aB != null){
			aB.hide();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_login));
		}
	}
}
