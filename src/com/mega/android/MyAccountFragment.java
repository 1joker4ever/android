package com.mega.android;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.mega.android.ManagerActivity.DrawerItem;
import com.mega.android.utils.Util;
import com.mega.components.RoundedImageView;
import com.mega.sdk.MegaAccountDetails;
import com.mega.sdk.MegaApiAndroid;
import com.mega.sdk.MegaApiJava;
import com.mega.sdk.MegaError;
import com.mega.sdk.MegaNode;
import com.mega.sdk.MegaRequest;
import com.mega.sdk.MegaRequestListenerInterface;
import com.mega.sdk.MegaUser;

public class MyAccountFragment extends Fragment implements OnClickListener, MegaRequestListenerInterface {

	RoundedImageView imageView;
	RelativeLayout contentLayout;
	TextView userNameTextView;
	TextView infoEmail;
	TableLayout contentTable;
	Button logoutButton;	
	Button upgradeButton;
//	String userEmail;	
	Context context;
	ActionBar aB;
	TableLayout bottomControlBar;
	TextView usedSpace;
	TextView usedSpaceText;
	ProgressBar usedSpaceBar;  
	TextView titleTypeAccount;
	TextView typeAccount;
	TextView titleLastSession;
	TextView lastSession;
	TextView titleConnections;
	TextView connections;
	//	private ListView overflowMenuList;
	private boolean overflowVisible = false; 
	MegaApiAndroid megaApi;
	//MegaUser contact;
	String myEmail;
	MegaUser myUser;

	@Override
	public void onCreate (Bundle savedInstanceState){
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		super.onCreate(savedInstanceState);
		log("onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		if (aB == null){
			aB = ((ActionBarActivity)context).getSupportActionBar();
		}

		aB.setTitle(R.string.section_account);
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = ((Activity) context).getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);

		View v = null;
		v = inflater.inflate(R.layout.fragment_my_account, container, false);
	
		imageView = (RoundedImageView) v.findViewById(R.id.my_avatar_image);
		imageView.getLayoutParams().width = Util.px2dp((200*scaleW), outMetrics);
		imageView.getLayoutParams().height = Util.px2dp((200*scaleW), outMetrics);
		contentTable = (TableLayout) v.findViewById(R.id.my_content_table);
		userNameTextView = (TextView) v.findViewById(R.id.my_name);
		infoEmail = (TextView) v.findViewById(R.id.my_email);
		bottomControlBar = (TableLayout) v.findViewById(R.id.progress_my_account);
		usedSpace = (TextView) v.findViewById(R.id.used_space_my_account);
	    usedSpaceText = (TextView) v.findViewById(R.id.used_space_text_my_account);
	    usedSpaceBar = (ProgressBar) v.findViewById(R.id.my_account_used_space_bar);	      
	    usedSpaceBar.setProgress(0);    
	    
		titleTypeAccount = (TextView) v.findViewById(R.id.my_account_title);	  
		typeAccount = (TextView) v.findViewById(R.id.my_account_type_account);	 
		titleLastSession = (TextView) v.findViewById(R.id.my_last_session_title);	
		lastSession= (TextView) v.findViewById(R.id.my_last_session);	
		titleConnections = (TextView) v.findViewById(R.id.my_connections_title);	
		connections = (TextView) v.findViewById(R.id.my_connections);	
		
	
		upgradeButton = (Button) v.findViewById(R.id.btn_upgrade); 
		upgradeButton.setOnClickListener(this); 
		logoutButton = (Button) v.findViewById(R.id.my_account_logout);
		logoutButton.setOnClickListener(this);

		
		myEmail=megaApi.getMyEmail();
		infoEmail.setText(myEmail);
		userNameTextView.setText(myEmail);
		
		myUser = megaApi.getContact(myEmail);

		logoutButton.setText(R.string.action_logout);
		lastSession.setText("Not implemented yet");
		
		ArrayList<MegaUser> contacts = megaApi.getContacts();
		ArrayList<MegaUser> visibleContacts=new ArrayList<MegaUser>();

		for (int i=0;i<contacts.size();i++){
			log("contact: " + contacts.get(i).getEmail() + "_" + contacts.get(i).getVisibility());
			if ((contacts.get(i).getVisibility() == MegaUser.VISIBILITY_VISIBLE) || (megaApi.getInShares(contacts.get(i)).size() != 0)){
				visibleContacts.add(contacts.get(i));
			}
		}		
		connections.setText(visibleContacts.size()+" Contacts");
		
		megaApi.getAccountDetails(this);
		
		File avatar = null;
		if (context.getExternalCacheDir() != null){
			avatar = new File(context.getExternalCacheDir().getAbsolutePath(), myEmail + ".jpg");
		}
		else{
			avatar = new File(context.getCacheDir().getAbsolutePath(), myEmail + ".jpg");
		}

		Bitmap imBitmap = null;
		if (avatar.exists()){
			if (avatar.length() > 0){
				BitmapFactory.Options bOpts = new BitmapFactory.Options();
				bOpts.inPurgeable = true;
				bOpts.inInputShareable = true;
				imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
				if (imBitmap == null) {
					avatar.delete();
					if (context.getExternalCacheDir() != null){
						megaApi.getUserAvatar(myUser, context.getExternalCacheDir().getAbsolutePath() + "/" + myEmail, this);
					}
					else{
						megaApi.getUserAvatar(myUser, context.getCacheDir().getAbsolutePath() + "/" + myEmail, this);
					}
				}
				else{
					imageView.setImageBitmap(imBitmap);
				}
			}
		}
		//infoAdded.setText(contact.getTimestamp()+"");
		
		
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		aB = ((ActionBarActivity)activity).getSupportActionBar();
	}	

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.my_account_logout:{
				ManagerActivity.logout(context, (MegaApplication)((ManagerActivity)context).getApplication(), megaApi, false);
				break;
			}
			case R.id.btn_upgrade:{
				Intent intent = new Intent(getActivity(), UpgradeActivity.class);
				startActivity(intent);
				break;
			}
		}
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
			info = numFolders +  " " + getResources().getQuantityString(R.plurals.general_num_shared_folders, numFolders);
			if (numFiles > 0){
				info = info + ", " + numFiles + " " + getResources().getQuantityString(R.plurals.general_num_shared_folders, numFiles);
			}
		}
		else {
			if (numFiles == 0){
				info = numFiles +  " " + getResources().getQuantityString(R.plurals.general_num_shared_folders, numFolders);
			}
			else{
				info = numFiles +  " " + getResources().getQuantityString(R.plurals.general_num_shared_folders, numFiles);
			}
		}

		return info;
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart()");
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestFinish");
		if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER){
			if (e.getErrorCode() == MegaError.API_OK){
				File avatar = null;
				if (context.getExternalCacheDir() != null){
					avatar = new File(context.getExternalCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
				}
				else{
					avatar = new File(context.getCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
				}
				Bitmap imBitmap = null;
				if (avatar.exists()){
					if (avatar.length() > 0){
						BitmapFactory.Options bOpts = new BitmapFactory.Options();
						bOpts.inPurgeable = true;
						bOpts.inInputShareable = true;
						imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
						if (imBitmap == null) {
							avatar.delete();
						}
						else{
							imageView.setImageBitmap(imBitmap);
						}
					}
				}
			}
		}
		if (request.getType() == MegaRequest.TYPE_ACCOUNT_DETAILS){
			log ("account_details request");
			if (e.getErrorCode() == MegaError.API_OK){
				
				MegaAccountDetails accountInfo = request.getMegaAccountDetails();
				
				int accountType = accountInfo.getProLevel();
				switch(accountType){				
				
					case 0:{	  
						typeAccount.setTextColor(getResources().getColor(R.color.green_free_account));
						typeAccount.setText(R.string.free_account);
						break;
					}
						
					case 1:{
						typeAccount.setText(R.string.pro1_account);
						break;
					}
					
					case 2:{
						typeAccount.setText(R.string.pro2_account);
						break;
					}
					
					case 3:{
						typeAccount.setText(R.string.pro3_account);
						break;
					}
					
				}
						
				long totalStorage = accountInfo.getStorageMax();
				long usedStorage = accountInfo.getStorageUsed();				
							
				totalStorage = ((totalStorage / 1024) / 1024) / 1024;
				String total = "";
				if (totalStorage >= 1024){
					totalStorage = totalStorage / 1024;
					total = total + totalStorage + " TB";
				}
				else{
					 total = total + totalStorage + " GB";
				}

				usedStorage = ((usedStorage / 1024) / 1024) / 1024;
				String used = "";
				if (usedStorage >= 1024){
					usedStorage = usedStorage / 1024;
					used = used + usedStorage + " TB";
				}
				else{
					used = used + usedStorage + " GB";
				}
				
		        String usedSpaceString = getString(R.string.used_space, used, total);
		        usedSpace.setText(usedSpaceString);
		        Spannable wordtoSpan = new SpannableString(usedSpaceString);
		        
		        bottomControlBar.setVisibility(View.VISIBLE);
		        int usedPerc = 0;
		        if (totalStorage != 0){
		        	usedPerc = (int)((100 * usedStorage) / totalStorage);
		        }
		        if (usedPerc < 90){
		        	usedSpaceBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_ok));
		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_ok)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        }
		        else if ((usedPerc >= 90) && (usedPerc <= 95)){
		        	usedSpaceBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_warning));
		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_warning)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        }
		        else{
		        	if (usedPerc > 100){
			        	usedPerc = 100;
			        }
		        	usedSpaceBar.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_exceed));    
		        	wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_exceed)), 0, used.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        }
		        usedSpaceBar.setProgress(usedPerc);      
		        
		        wordtoSpan.setSpan(new RelativeSizeSpan(1.5f), 0, used.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.navigation_drawer_mail)), used.length() + 1, used.length() + 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        wordtoSpan.setSpan(new RelativeSizeSpan(1.5f), used.length() + 4, used.length() + 4 + total.length() - 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        usedSpace.setText(wordtoSpan);        
			        
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError");
	}

	public static void log(String log) {
		Util.log("ContactPropertiesFragment", log);
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub

	}
	
	public void setMyInfo(){
		//TODO
		
		
	}
}
