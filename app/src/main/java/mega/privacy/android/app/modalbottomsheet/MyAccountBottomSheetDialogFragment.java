package mega.privacy.android.app.modalbottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.AccountController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaUser;

public class MyAccountBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    Context context;
    MegaUser contact = null;
    AccountController aC;

    private BottomSheetBehavior mBehavior;

    public LinearLayout mainLinearLayout;
    public TextView titleText;
    public LinearLayout optionChoosePicture;
    public LinearLayout optionTakePicture;
    public LinearLayout optionRemovePicture;

    DisplayMetrics outMetrics;

    MegaApiAndroid megaApi;
    DatabaseHandler dbH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if(context instanceof ManagerActivityLollipop){
            contact = ((ManagerActivityLollipop) context).getSelectedUser();
        }

        aC = new AccountController(context);

        dbH = DatabaseHandler.getDbHandler(getActivity());
    }
    @Override
    public void setupDialog(final Dialog dialog, int style) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_my_account, null);

        mainLinearLayout = (LinearLayout) contentView.findViewById(R.id.my_account_bottom_sheet);

        titleText = (TextView) contentView.findViewById(R.id.my_account_title_text);

        optionChoosePicture= (LinearLayout) contentView.findViewById(R.id.my_account_choose_photo_layout);
        optionTakePicture = (LinearLayout) contentView.findViewById(R.id.my_account_take_photo_layout);
        optionRemovePicture = (LinearLayout) contentView.findViewById(R.id.my_account_delete_layout);

        optionChoosePicture.setOnClickListener(this);
        optionTakePicture.setOnClickListener(this);
        optionRemovePicture.setOnClickListener(this);

        if(aC.existsAvatar()){
            optionRemovePicture.setVisibility(View.VISIBLE);
        }
        else{
            optionRemovePicture.setVisibility(View.GONE);
        }

        dialog.setContentView(contentView);
    }


    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.my_account_choose_photo_layout:{
                log("option choose photo avatar");
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                ((ManagerActivityLollipop)context).startActivityForResult(Intent.createChooser(intent, null), Constants.CHOOSE_PICTURE_PROFILE_CODE);

                dismissAllowingStateLoss();
                break;
            }
            case R.id.my_account_take_photo_layout:{
                log("option take photo avatar");
                AccountController aC = new AccountController(context);
                aC.takeProfilePicture();
                dismissAllowingStateLoss();
                break;
            }
            case R.id.my_account_delete_layout:{
                log("option delete avatar");
                ((ManagerActivityLollipop) context).showConfirmationDeleteAvatar();
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
        context = activity;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private static void log(String log) {
        Util.log("MyAccountBottomSheetDialogFragment", log);
    }
}
