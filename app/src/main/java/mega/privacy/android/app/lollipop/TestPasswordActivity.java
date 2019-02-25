package mega.privacy.android.app.lollipop;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.controllers.AccountController;
import mega.privacy.android.app.modalbottomsheet.RecoveryKeyBottomSheetDialogFragment;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;

/**
 * Created by mega on 3/04/18.
 */

public class TestPasswordActivity extends PinActivityLollipop implements View.OnClickListener, MegaRequestListenerInterface {

    LinearLayout passwordReminderLayout;
    ImageView passwordReminderCloseButton;
    CheckBox blockCheckBox;
    TextView dialogTest;
    Button testPasswordButton;
    Button passwordReminderBackupRecoveryKeyButton;
    Button passwordReminderDismissButton;

    LinearLayout testPasswordLayout;
    Toolbar tB;
    ActionBar aB;
    private EditText passwordEditText;
    private ImageView passwordToggle;
    private TextView passwordErrorText;
    private ImageView passwordErrorImage;
    private Button confirmPasswordButton;
    private Button testPasswordbackupRecoveryKeyButton;
    private Button testPasswordDismissButton;
    private RelativeLayout containerPasswordError;
    private TextView enterPwdHint;
    private Button procedToLogout;

    private Drawable password_background;

    private boolean passwdVisibility = false;
    private boolean passwordCorrect = false;
    private boolean logout = false;

    boolean isDismissButon = true;
    MegaApiAndroid megaApi;
    DatabaseHandler dbH;

    int counter = 0;
    boolean testingPassword = false;
    int numRequests = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_password);
        if (getIntent() == null){
            log("intent NULL");
            return;
        }

        if (savedInstanceState != null){
            counter = savedInstanceState.getInt("counter", 0);
            testingPassword = savedInstanceState.getBoolean("testingPassword", false);
        }
        else {
            counter = 0;
            testingPassword = false;
        }

        logout = getIntent().getBooleanExtra("logout", false);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_primary_color));

        megaApi = ((MegaApplication)getApplication()).getMegaApi();

        passwordReminderLayout = (LinearLayout) findViewById(R.id.password_reminder_layout);
        passwordReminderCloseButton = (ImageView) findViewById(R.id.password_reminder_close_image_button);
        passwordReminderCloseButton.setOnClickListener(this);
        dialogTest = (TextView) findViewById(R.id.password_reminder_text);
        blockCheckBox = (CheckBox) findViewById(R.id.password_reminder_checkbox);
        blockCheckBox.setOnClickListener(this);
        testPasswordButton = (Button) findViewById(R.id.password_reminder_test_button);
        testPasswordButton.setOnClickListener(this);
        passwordReminderBackupRecoveryKeyButton = (Button) findViewById(R.id.password_reminder_recoverykey_button);
        passwordReminderBackupRecoveryKeyButton.setOnClickListener(this);
        passwordReminderDismissButton = (Button) findViewById(R.id.password_reminder_dismiss_button);
        passwordReminderDismissButton.setOnClickListener(this);

        testPasswordLayout = (LinearLayout) findViewById(R.id.test_password_layout);
        tB = (Toolbar) findViewById(R.id.toolbar);
        enterPwdHint = (TextView) findViewById(R.id.test_password_enter_pwd_hint);
        passwordEditText = (EditText) findViewById(R.id.test_password_edittext);
        passwordToggle = (ImageView) findViewById(R.id.toggle_button);
        passwordToggle.setOnClickListener(this);
        passwordErrorText = (TextView) findViewById(R.id.test_password_text_error_text);
        passwordErrorImage = (ImageView) findViewById(R.id.test_password_text_error_icon);
        confirmPasswordButton = (Button) findViewById(R.id.test_password_confirm_button);
        confirmPasswordButton.setOnClickListener(this);
        testPasswordbackupRecoveryKeyButton = (Button) findViewById(R.id.test_password_backup_button);
        testPasswordbackupRecoveryKeyButton.setOnClickListener(this);
        testPasswordDismissButton = (Button) findViewById(R.id.test_password_dismiss_button);
        testPasswordDismissButton.setOnClickListener(this);
        containerPasswordError = (RelativeLayout) findViewById(R.id.test_password_text_error);
        procedToLogout = (Button) findViewById(R.id.proced_to_logout_button);
        procedToLogout.setOnClickListener(this);

        if (logout) {
            passwordReminderCloseButton.setVisibility(View.VISIBLE);
            dialogTest.setText(R.string.remember_pwd_dialog_text_logout);
            passwordReminderDismissButton.setText(R.string.proced_to_logout);
            passwordReminderDismissButton.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
            testPasswordDismissButton.setVisibility(View.GONE);
            procedToLogout.setVisibility(View.VISIBLE);
        }
        else {
            passwordReminderCloseButton.setVisibility(View.GONE);
            dialogTest.setText(R.string.remember_pwd_dialog_text);
            passwordReminderDismissButton.setText(R.string.general_dismiss);
            passwordReminderDismissButton.setTextColor(ContextCompat.getColor(this, R.color.accentColor));
            testPasswordDismissButton.setVisibility(View.VISIBLE);
            procedToLogout.setVisibility(View.GONE);
        }

        passwordEditText.getBackground().clearColorFilter();

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String password = passwordEditText.getText().toString();
                    passwordCorrect = megaApi.checkPassword(password);
                    showError(passwordCorrect);
                    return true;
                }
                return false;
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!passwordCorrect){
                    quitError();
                }
            }
        });

        enterPwdHint.setVisibility(View.INVISIBLE);
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    enterPwdHint.setVisibility(View.VISIBLE);
                    passwordEditText.setHint(null);
                    passwordToggle.setVisibility(View.VISIBLE);
                    passwordToggle.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_b_shared_read));
                }
                else {
                    passwordToggle.setVisibility(View.GONE);
                    passwdVisibility = false;
                    showHidePassword();
                }
            }
        });
        password_background = passwordEditText.getBackground().mutate().getConstantState().newDrawable();

        if (testingPassword) {
            setTestPasswordLayout();
        }
        else {
            passwordReminderLayout.setVisibility(View.VISIBLE);
            testPasswordLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("counter", counter);
        outState.putBoolean("testingPassword", testingPassword);
    }

    void setTestPasswordLayout () {
        tB.setVisibility(View.VISIBLE);
        setSupportActionBar(tB);
        aB = getSupportActionBar();
        aB.setTitle(getString(R.string.remember_pwd_dialog_button_test).toUpperCase());
        aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);
        passwordReminderLayout.setVisibility(View.GONE);
        testPasswordLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (testingPassword) {
            isDismissButon = true;
            dismissActivity();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    void quitError(){
        if(containerPasswordError.getVisibility() != View.INVISIBLE){
            enterPwdHint.setTextColor(ContextCompat.getColor(this, R.color.accentColor));
            containerPasswordError.setVisibility(View.INVISIBLE);
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                passwordEditText.setBackgroundDrawable(password_background);
            } else{
                passwordEditText.setBackground(password_background);
            }
            testPasswordbackupRecoveryKeyButton.setTextColor(ContextCompat.getColor(this, R.color.accentColor));
            confirmPasswordButton.setEnabled(true);
            confirmPasswordButton.setAlpha(1F);
        }
    }

    void showError (boolean correct) {
        hideKeyboard();
        if(containerPasswordError.getVisibility() == View.INVISIBLE){
            containerPasswordError.setVisibility(View.VISIBLE);
            Drawable background = password_background.mutate().getConstantState().newDrawable();
            PorterDuffColorFilter porterDuffColorFilter;
            if (correct){
                porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.green_unlocked_rewards), PorterDuff.Mode.SRC_ATOP);
                passwordErrorText.setText(getString(R.string.test_pwd_accepted));
                passwordErrorText.setTextColor(ContextCompat.getColor(this, R.color.green_unlocked_rewards));
                passwordErrorImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_accept_test));
                testPasswordbackupRecoveryKeyButton.setTextColor(ContextCompat.getColor(this, R.color.accentColor));
                passwordEditText.setEnabled(false);
                numRequests++;
                megaApi.passwordReminderDialogSucceeded(this);
            }
            else {
                counter++;
                porterDuffColorFilter = new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.login_warning), PorterDuff.Mode.SRC_ATOP);
                passwordErrorText.setText(getString(R.string.test_pwd_wrong));
                enterPwdHint.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
                passwordErrorText.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
                Drawable errorIcon = ContextCompat.getDrawable(this, R.drawable.ic_input_warning);
                errorIcon.setColorFilter(porterDuffColorFilter);
                passwordErrorImage.setImageDrawable(errorIcon);
                testPasswordbackupRecoveryKeyButton.setTextColor(ContextCompat.getColor(this, R.color.login_warning));
                if (counter == 3) {
                    Intent intent = new Intent(this, ChangePasswordActivityLollipop.class);
                    startActivity(intent);
                    finish();
                }
            }
            confirmPasswordButton.setEnabled(false);
            confirmPasswordButton.setAlpha(0.3F);
            background.setColorFilter(porterDuffColorFilter);
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                passwordEditText.setBackgroundDrawable(background);
            } else{
                passwordEditText.setBackground(background);
            }
        }
    }

    void hideKeyboard (){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void showHidePassword (){
        if(!passwdVisibility){
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordEditText.setTypeface(Typeface.SANS_SERIF,Typeface.NORMAL);
            passwordEditText.setSelection(passwordEditText.getText().length());
        }else{
            passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordEditText.setSelection(passwordEditText.getText().length());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.REQUEST_DOWNLOAD_FOLDER && resultCode == RESULT_OK){
            log("REQUEST_DOWNLOAD_FOLDER");
            String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
            if (parentPath != null){
                log("parentPath no NULL");
                String[] split = Util.rKFile.split("/");
                parentPath = parentPath+"/"+split[split.length-1];
                Intent newIntent = new Intent(this, ManagerActivityLollipop.class);
                newIntent.putExtra("parentPath", parentPath);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                newIntent.setAction(Constants.ACTION_REQUEST_DOWNLOAD_FOLDER_LOGOUT);
                startActivity(newIntent);
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.password_reminder_checkbox: {
                if (blockCheckBox.isChecked()) {
                    log("Block CheckBox checked!");
                }
                else {
                    log("Block CheckBox does NOT checked!");
                }
                break;
            }
            case R.id.password_reminder_test_button: {
                shouldBlockPasswordReminder();
                testingPassword = true;
                setTestPasswordLayout();
                break;
            }
            case R.id.toggle_button:{
                if (passwdVisibility) {
                    passwordToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_b_shared_read));
                    passwdVisibility = false;
                    showHidePassword();
                }
                else {
                    passwordToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_b_see));
                    passwdVisibility = true;
                    showHidePassword();
                }
                break;
            }
            case R.id.test_password_confirm_button:{
                String password = passwordEditText.getText().toString();
                passwordCorrect = megaApi.checkPassword(password);
                showError(passwordCorrect);
                break;
            }
            case R.id.password_reminder_recoverykey_button:
            case R.id.test_password_backup_button: {
                RecoveryKeyBottomSheetDialogFragment recoveryKeyBottomSheetDialogFragment = new RecoveryKeyBottomSheetDialogFragment();
                recoveryKeyBottomSheetDialogFragment.show(getSupportFragmentManager(), recoveryKeyBottomSheetDialogFragment.getTag());
                break;
            }
            case R.id.password_reminder_close_image_button: {
                isDismissButon = true;
                dismissActivity();
                break;
            }
            case R.id.password_reminder_dismiss_button:
            case R.id.test_password_dismiss_button:
            case R.id.proced_to_logout_button: {
                isDismissButon = false;
                dismissActivity();
                break;
            }
        }
    }

    public void dismissActivity() {
        numRequests++;
        megaApi.passwordReminderDialogSkipped(this);
        shouldBlockPasswordReminder();
    }

    void shouldBlockPasswordReminder() {
        if (blockCheckBox.isChecked()) {
            numRequests++;
            megaApi.passwordReminderDialogBlocked(this);
        }
    }

    public void showSnackbar(String s){
        log("showSnackbar");
        Snackbar snackbar = Snackbar.make(findViewById(R.id.container_layout), s, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);
        snackbar.show();
    }

    public static void log(String message) {
        Util.log("TestPasswordActivity", message);
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        numRequests--;
        if(request.getType() == MegaRequest.TYPE_SET_ATTR_USER && request.getParamType() == MegaApiJava.USER_ATTR_PWD_REMINDER && e.getErrorCode() == MegaError.API_OK){
            log("New value of attribute USER_ATTR_PWD_REMINDER: " +request.getText());
            if (!isDismissButon && logout && numRequests <= 0) {
                AccountController ac = new AccountController(this);
                ac.logout(this, megaApi);
            }
            else {
                finish();
            }
        }
        else if (request.getType() == MegaRequest.TYPE_LOGOUT){
            log("logout finished");

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
    }

    public boolean isLogout() {
        return logout;
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

    }
}
