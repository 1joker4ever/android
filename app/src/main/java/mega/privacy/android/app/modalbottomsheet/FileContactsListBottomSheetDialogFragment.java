package mega.privacy.android.app.modalbottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContact;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.lollipop.FileContactListActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaUser;

public class FileContactsListBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    Context context;
    MegaUser contact = null;
    ContactController cC;

    private BottomSheetBehavior mBehavior;

    public LinearLayout mainLinearLayout;
    public TextView titleNameContactPanel;
    public TextView titleMailContactPanel;
    public RoundedImageView contactImageView;
    public TextView avatarInitialLetter;
    public LinearLayout optionChangePermissions;
    public LinearLayout optionDelete;

    String fullName="";

    DisplayMetrics outMetrics;

    MegaApiAndroid megaApi;
    DatabaseHandler dbH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if(savedInstanceState!=null) {
            log("Bundle is NOT NULL");
            String email = savedInstanceState.getString("email");
            log("Email of the contact: "+email);
            if(email!=null){
                contact = megaApi.getContact(email);
            }
        }
        else{
            log("Bundle NULL");
            if(context instanceof FileContactListActivityLollipop){
                contact = ((FileContactListActivityLollipop) context).getSelectedContact();
            }
        }
        dbH = DatabaseHandler.getDbHandler(getActivity());
    }
    @Override
    public void setupDialog(final Dialog dialog, int style) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_file_contact_list, null);

        mainLinearLayout = (LinearLayout) contentView.findViewById(R.id.file_contact_list_bottom_sheet);

        titleNameContactPanel = (TextView) contentView.findViewById(R.id.file_contact_list_contact_name_text);
        titleMailContactPanel = (TextView) contentView.findViewById(R.id.file_contact_list_contact_mail_text);
        contactImageView = (RoundedImageView) contentView.findViewById(R.id.sliding_file_contact_list_thumbnail);
        avatarInitialLetter = (TextView) contentView.findViewById(R.id.sliding_file_contact_list_initial_letter);

        optionChangePermissions = (LinearLayout) contentView.findViewById(R.id.file_contact_list_option_permissions_layout);
        optionDelete = (LinearLayout) contentView.findViewById(R.id.file_contact_list_option_delete_layout);

        titleNameContactPanel.setMaxWidth(Util.scaleWidthPx(200, outMetrics));
        titleMailContactPanel.setMaxWidth(Util.scaleWidthPx(200, outMetrics));

        optionChangePermissions.setOnClickListener(this);
        optionDelete.setOnClickListener(this);

        if(contact!=null){
            fullName = getFullName(contact);
            titleNameContactPanel.setText(fullName);
            titleMailContactPanel.setText(contact.getEmail());

            addAvatarContactPanel(contact);

            dialog.setContentView(contentView);
            mBehavior = BottomSheetBehavior.from((View) mainLinearLayout.getParent());
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else{
            log("Contact NULL");
        }
    }

    public String getFullName(MegaUser contact){
        String firstNameText ="";
        String lastNameText ="";
        MegaContact contactDB = dbH.findContactByHandle(String.valueOf(contact.getHandle()));
        if(contactDB!=null){
            firstNameText = contactDB.getName();
            lastNameText = contactDB.getLastName();

            String fullName;

            if (firstNameText.trim().length() <= 0){
                fullName = lastNameText;
            }
            else{
                fullName = firstNameText + " " + lastNameText;
            }

            if (fullName.trim().length() <= 0){
                log("Put email as fullname");
                String email = contact.getEmail();
                String[] splitEmail = email.split("[@._]");
                fullName = splitEmail[0];
            }

            return fullName;
        }
        else{
            String email = contact.getEmail();
            String[] splitEmail = email.split("[@._]");
            String fullName = splitEmail[0];
            return fullName;
        }
    }

    public void addAvatarContactPanel(MegaUser contact){

        String contactMail = contact.getEmail();
        File avatar = null;
        if (getActivity().getExternalCacheDir() != null){
            avatar = new File(getActivity().getExternalCacheDir().getAbsolutePath(), contactMail + ".jpg");
        }
        else{
            avatar = new File(getActivity().getCacheDir().getAbsolutePath(), contactMail + ".jpg");
        }
        Bitmap bitmap = null;
        if (avatar.exists()){
            if (avatar.length() > 0){
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                if (bitmap == null) {
                    avatar.delete();
                }
                else{
                    avatarInitialLetter.setVisibility(View.GONE);
                    contactImageView.setImageBitmap(bitmap);
                    return;
                }
            }
        }

        ////DEfault AVATAR
        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);

        if (contact != null) {
            String color = megaApi.getUserAvatarColor(contact);
            if (color != null) {
                log("The color to set the avatar is " + color);
                p.setColor(Color.parseColor(color));
            } else {
                log("Default color to the avatar");
                p.setColor(getResources().getColor(R.color.lollipop_primary_color));
            }
        } else {
            log("Contact is NULL");
            p.setColor(getResources().getColor(R.color.lollipop_primary_color));
        }

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth() / 2;
        else
            radius = defaultAvatar.getHeight() / 2;

        c.drawCircle(defaultAvatar.getWidth() / 2, defaultAvatar.getHeight() / 2, radius, p);
        contactImageView.setImageBitmap(defaultAvatar);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        if (fullName != null) {
            if (fullName.length() > 0) {
                if (fullName.trim().length() > 0) {
                    String firstLetter = fullName.charAt(0) + "";
                    firstLetter = firstLetter.toUpperCase(Locale.getDefault());
                    avatarInitialLetter.setText(firstLetter);
                    avatarInitialLetter.setTextColor(Color.WHITE);
                    avatarInitialLetter.setVisibility(View.VISIBLE);
                    avatarInitialLetter.setTextSize(22);
                } else {
                    avatarInitialLetter.setVisibility(View.INVISIBLE);
                }
            }
            else{
                avatarInitialLetter.setVisibility(View.INVISIBLE);
            }

        } else {
            avatarInitialLetter.setVisibility(View.INVISIBLE);
        }

        ////
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.file_contact_list_option_permissions_layout:{
                log("permissions layout");
                if(contact==null){
                    log("Selected contact NULL");
                    return;
                }
                ((FileContactListActivityLollipop)context).changePermissions();
                break;
            }
            case R.id.file_contact_list_option_delete_layout:{
                log("optionSendFile");
                if(contact==null){
                    log("Selected contact NULL");
                    return;
                }
                ((FileContactListActivityLollipop)context).removeFileContactShare();
                break;
            }
        }

//        dismiss();
        mBehavior = BottomSheetBehavior.from((View) mainLinearLayout.getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);
        String email = contact.getEmail();
        log("Email of the contact: "+email);
        outState.putString("email", email);
    }

    private static void log(String log) {
        Util.log("FileContactsListBottomSheetDialogFragment", log);
    }
}
