package mega.privacy.android.app.lollipop;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.utils.DBUtil;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;


public class MyStorageFragmentLollipop extends Fragment implements OnClickListener, MegaRequestListenerInterface{

	Context context;
	MyAccountInfo myAccountInfo;

	MegaUser myUser;

	LinearLayout parentLinearLayout;

	TextView typeAccountText;
	TextView expirationAccountText;
	TextView storageAvailableText;
	TextView transferQuotaUsedText;

	TextView totalUsedSpace;

	TextView cloudDriveUsedText;
	TextView inboxUsedText;
	TextView incomingUsedText;
	TextView rubbishUsedText;
	TextView availableSpaceText;


	
	DisplayMetrics outMetrics;
	float density;

	MegaApiAndroid megaApi;

	@Override
	public void onCreate (Bundle savedInstanceState){
		log("onCreate");
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		super.onCreate(savedInstanceState);
	}
	
	public void onDestroy()
	{
		if(megaApi != null)
		{	
			megaApi.removeRequestListener(this);
		}
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log("onCreateView");
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);

		View v = inflater.inflate(R.layout.fragment_my_storage, container, false);
		
		myUser = megaApi.getMyUser();
		if(myUser == null){
			return null;
		}

		parentLinearLayout = (LinearLayout) v.findViewById(R.id.my_storage_parent_linear_layout);

		typeAccountText = (TextView) v.findViewById(R.id.my_storage_account_plan_text);
		storageAvailableText = (TextView) v.findViewById(R.id.my_storage_account_space_text);
		expirationAccountText = (TextView) v.findViewById(R.id.my_storage_account_expiration_text);
		transferQuotaUsedText = (TextView) v.findViewById(R.id.my_storage_account_transfer_text);


		totalUsedSpace = (TextView) v.findViewById(R.id.my_storage_used_space_result_text);





		if(myAccountInfo==null){
			log("MyAccountInfo is NULL");
			myAccountInfo = ((ManagerActivityLollipop)context).getMyAccountInfo();
		}


		setAccountDetails();
//
		refreshAccountInfo();

		return v;
	}

	public static MyStorageFragmentLollipop newInstance() {
		log("newInstance");
		MyStorageFragmentLollipop fragment = new MyStorageFragmentLollipop();
		return fragment;
	}

	public void refreshAccountInfo(){
		log("refreshAccountInfo");

		//Check if the call is recently
		log("Check the last call to getAccountDetails");
		if(DBUtil.callToAccountDetails(context)){
			log("megaApi.getAccountDetails SEND");
			megaApi.getAccountDetails(myAccountInfo);
		}
		log("Check the last call to getExtendedAccountDetails");
		if(DBUtil.callToExtendedAccountDetails(context)){
			log("megaApi.getExtendedAccountDetails SEND");
			megaApi.getExtendedAccountDetails(true, false, false, myAccountInfo);
		}
		log("Check the last call to callToPaymentMethods");
		if(DBUtil.callToPaymentMethods(context)){
			log("megaApi.getPaymentMethods SEND");
			megaApi.getPaymentMethods(myAccountInfo);
		}
	}

	public void setAccountDetails(){
		log("setAccountDetails");

		if((getActivity() == null) || (!isAdded())){
			log("Fragment MyAccount NOT Attached!");
			return;
		}
		//Set account details
		if(myAccountInfo.getAccountType()<0||myAccountInfo.getAccountType()>4){
			typeAccountText.setText(getString(R.string.recovering_info));
			expirationAccountText.setText(getString(R.string.recovering_info));
			storageAvailableText.setText(getString(R.string.recovering_info));
		}
		else{
			storageAvailableText.setText(myAccountInfo.getTotalFormatted());
			switch(myAccountInfo.getAccountType()){

				case 0:{
					typeAccountText.setText(R.string.my_account_free);
					expirationAccountText.setText("No billing cycle");
					break;
				}

				case 1:{
					typeAccountText.setText(getString(R.string.my_account_pro1));
					expirationAccountText.setText(Util.getDateString(myAccountInfo.getAccountInfo().getProExpiration()));
					break;
				}

				case 2:{
					typeAccountText.setText(getString(R.string.my_account_pro2));
					expirationAccountText.setText(Util.getDateString(myAccountInfo.getAccountInfo().getProExpiration()));
					break;
				}

				case 3:{
					typeAccountText.setText(getString(R.string.my_account_pro3));
					expirationAccountText.setText(Util.getDateString(myAccountInfo.getAccountInfo().getProExpiration()));
					break;
				}

				case 4:{
					typeAccountText.setText(getString(R.string.my_account_prolite));
					expirationAccountText.setText(Util.getDateString(myAccountInfo.getAccountInfo().getProExpiration()));
					break;
				}

			}
		}


//		if (getPaymentMethodsBoolean == true){
//			if (upgradeButton != null){
//				if ((myAccountInfo.getAccountInfo().getSubscriptionStatus() == MegaAccountDetails.SUBSCRIPTION_STATUS_NONE) || (myAccountInfo.getAccountInfo().getSubscriptionStatus() == MegaAccountDetails.SUBSCRIPTION_STATUS_INVALID)){
//					Time now = new Time();
//					now.setToNow();
//					if (myAccountInfo.getAccountType() != 0){
//						if (now.toMillis(false) >= (myAccountInfo.getAccountInfo().getProExpiration()*1000)){
//							if (Util.checkBitSet(myAccountInfo.getPaymentBitSet(), MegaApiAndroid.PAYMENT_METHOD_CREDIT_CARD) || Util.checkBitSet(myAccountInfo.getPaymentBitSet(), MegaApiAndroid.PAYMENT_METHOD_FORTUMO)){
//								upgradeButton.setVisibility(View.VISIBLE);
//							}
//						}
//					}
//					else{
//						if (Util.checkBitSet(myAccountInfo.getPaymentBitSet(), MegaApiAndroid.PAYMENT_METHOD_CREDIT_CARD) || Util.checkBitSet(myAccountInfo.getPaymentBitSet(), MegaApiAndroid.PAYMENT_METHOD_FORTUMO)){
//							upgradeButton.setVisibility(View.VISIBLE);
//						}
//					}
//				}
//			}
//		}

		if(myAccountInfo.getUsedFormatted().trim().length()<=0){
			totalUsedSpace.setText(getString(R.string.recovering_info));
		}
		else{
			String usedSpaceString = myAccountInfo.getUsedFormatted() + " " + getString(R.string.general_x_of_x) + " " + myAccountInfo.getTotalFormatted();
			totalUsedSpace.setText(usedSpaceString);
		}

//		if(myAccountInfo.getAccountInfo().getTransferOwnUsed()<0){
//			transferQuotaUsedText.setText(getString(R.string.recovering_info));
//		}
//		else{
//			long transferQuotaUsed = myAccountInfo.getAccountInfo().getTransferOwnUsed();
////			String usedSpaceString = myAccountInfo.getUsedFormatted() + " " + getString(R.string.general_x_of_x) + " " + myAccountInfo.getTotalFormatted();
//			transferQuotaUsedText.setText(transferQuotaUsed+"");
//		}

	}

	@Override
	public void onAttach(Activity activity) {
		log("onAttach");
		super.onAttach(activity);
		context = activity;
	}

	@Override
	public void onAttach(Context context) {
		log("onAttach context");
		super.onAttach(context);
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		log("onClick");
		switch (v.getId()) {

		}
	}

	public int onBackPressed(){
		log("onBackPressed");

//		if(exportMKLayout.getVisibility()==View.VISIBLE){
//			log("Master Key layout is VISIBLE");
//			hideMKLayout();
//			return 1;
//		}

		return 0;
	}

	public String getDescription(ArrayList<MegaNode> nodes){
		int numFolders = 0;
		int numFiles = 0;

		for (int i=0;i<nodes.size();i++){
			MegaNode c = nodes.get(i);
			if (c.isFolder()){
				numFolders++;
			}
			else{
				numFiles++;
			}
		}

		String info = "";
		if (numFolders > 0){
			info = numFolders +  " " + context.getResources().getQuantityString(R.plurals.general_num_shared_folders, numFolders);
			if (numFiles > 0){
				info = info + ", " + numFiles + " " + context.getResources().getQuantityString(R.plurals.general_num_shared_folders, numFiles);
			}
		}
		else {
			if (numFiles == 0){
				info = numFiles +  " " + context.getResources().getQuantityString(R.plurals.general_num_shared_folders, numFolders);
			}
			else{
				info = numFiles +  " " + context.getResources().getQuantityString(R.plurals.general_num_shared_folders, numFiles);
			}
		}

		return info;
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getRequestString());
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		log("onRequestFinish");

	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError");
	}

	public static void log(String log) {
		Util.log("MyStorageFragmentLollipop", log);
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

	}

	public MyAccountInfo getMyAccountInfo() {
		return myAccountInfo;
	}

}
