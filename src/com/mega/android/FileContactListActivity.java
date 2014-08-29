package com.mega.android;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StatFs;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.mega.android.FileStorageActivity.Mode;
import com.mega.components.RoundedImageView;
import com.mega.sdk.MegaApiAndroid;
import com.mega.sdk.MegaApiJava;
import com.mega.sdk.MegaError;
import com.mega.sdk.MegaGlobalListenerInterface;
import com.mega.sdk.MegaNode;
import com.mega.sdk.MegaRequest;
import com.mega.sdk.MegaRequestListenerInterface;
import com.mega.sdk.MegaShare;
import com.mega.sdk.MegaUser;
import com.mega.sdk.NodeList;
import com.mega.sdk.ShareList;

public class FileContactListActivity extends PinActivity implements MegaRequestListenerInterface, OnItemClickListener, OnItemLongClickListener, OnClickListener, MegaGlobalListenerInterface {

	MegaApiAndroid megaApi;
	ActionBar aB;
	FileContactListActivity fileContactListActivity = this;
		
	TextView nameView;
	ImageView imageView;
	ImageView statusDot;
	TextView createdView;
	
	RelativeLayout contactLayout;
	RelativeLayout fileContactLayout;
	ListView listView;
	ImageView emptyImage;
	TextView emptyText;
	
	ShareList listContacts;	
	
	ShareFolderContactsDialog shareFolderContactsDialog;
	ArrayList<MegaUser> listContactsArray = new ArrayList<MegaUser>();
	
	long nodeHandle;
	MegaNode node;
	NodeList contactNodes;
	
	MegaSharedFolderAdapter adapter;
	
	long parentHandle = -1;
	
	Stack<Long> parentHandleStack = new Stack<Long>();
	
	private ActionMode actionMode;
	
	boolean removeShare = false;
	boolean changeShare = false;
	
	ProgressDialog statusDialog;
	AlertDialog permissionsDialog;
	
	public static int REQUEST_CODE_GET = 1000;
	public static int REQUEST_CODE_GET_LOCAL = 1003;
	public static int REQUEST_CODE_SELECT_COPY_FOLDER = 1002;
	public static int REQUEST_CODE_SELECT_LOCAL_FOLDER = 1004;
	
	private int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;
	
	public UploadHereDialog uploadDialog;
	
	private List<ShareInfo> filePreparedInfos;
	
	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;
	
	MenuItem permissionButton;
	MenuItem deleteShareButton;
	MenuItem addSharingContact;
	
	private class ActionBarCallBack implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			log("onActionItemClicked");
			final List<MegaShare> contacts = getSelectedContacts();			
						
			switch(item.getItemId()){
				case R.id.action_file_contact_list_permissions:{
					
					//Change permissions
	
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(fileContactListActivity);
	
					dialogBuilder.setTitle(getString(R.string.file_properties_shared_folder_permissions));
					
					
					final CharSequence[] items = {getString(R.string.file_properties_shared_folder_read_only), getString(R.string.file_properties_shared_folder_read_write), getString(R.string.file_properties_shared_folder_full_access)};
					dialogBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							removeShare = false;
							changeShare = true;
							ProgressDialog temp = null;
							try{
								temp = new ProgressDialog(fileContactListActivity);
								temp.setMessage(getString(R.string.context_sharing_folder));
								temp.show();
							}
							catch(Exception e){
								return;
							}
							statusDialog = temp;
							switch(item) {
								case 0:{
									if(contacts!=null){
		
										if(contacts.size()!=0){
											log("Tamaño array----- "+contacts.size());	
											for(int j=0;j<contacts.size();j++){
												log("Numero: "+j);	
												megaApi.share(node, contacts.get(j).getUser(), MegaShare.ACCESS_READ, fileContactListActivity);							
											}
										}
									}		                        	
									break;
								}
								case 1:{
									if(contacts!=null){
										if(contacts.size()!=0){
											log("Tamaño array----- "+contacts.size());		
											for(int j=0;j<contacts.size();j++){										
												log("Numero: "+j);
												megaApi.share(node, contacts.get(j).getUser(), MegaShare.ACCESS_READWRITE, fileContactListActivity);								
											}
										}
									}	
		
									break;
								}
								case 2:{
									if(contacts!=null){
		
										if(contacts.size()!=0){
											log("Tamaño array----- "+contacts.size());		
											for(int j=0;j<contacts.size();j++){										
												log("Numero: "+j);		
												megaApi.share(node, contacts.get(j).getUser(), MegaShare.ACCESS_FULL, fileContactListActivity);								
											}
										}
									}
									break;
								}
							}
						}
					});
					
					permissionsDialog = dialogBuilder.create();
					permissionsDialog.show();
					Resources resources = permissionsDialog.getContext().getResources();
					int alertTitleId = resources.getIdentifier("alertTitle", "id", "android");
					TextView alertTitle = (TextView) permissionsDialog.getWindow().getDecorView().findViewById(alertTitleId);
					alertTitle.setTextColor(resources.getColor(R.color.mega));
					int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
					View titleDivider = permissionsDialog.getWindow().getDecorView().findViewById(titleDividerId);
					titleDivider.setBackgroundColor(resources.getColor(R.color.mega));
					
					adapter.setMultipleSelect(false);
	
					log("Cambio permisos");
					
					clearSelections();
					hideMultipleSelect();
					
					break;
				}
			case R.id.action_file_contact_list_delete:{
				

				removeShare = true;
				changeShare = false;
				ProgressDialog temp = null;

				try{
					temp = new ProgressDialog(fileContactListActivity);					

					temp.setMessage((getString(R.string.context_sharing_folder))); 
					temp.show();
				}
				catch(Exception e){
					return false;
				}

				statusDialog = temp;

				if(contacts!=null){

					if(contacts.size()!=0){

						for(int j=0;j<contacts.size();j++){									

							megaApi.share(node, contacts.get(j).getUser(), MegaShare.ACCESS_UNKNOWN, fileContactListActivity);								
						}

					}
				}
				adapter.setMultipleSelect(false);
				clearSelections();
				hideMultipleSelect();
				break;
			}

			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			log("onCreateActionMode");
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.shared_contact_browser_action, menu);
			return true;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			log("onDestroyActionMode");
			adapter.setMultipleSelect(false);
			listView.setOnItemLongClickListener(fileContactListActivity);
			clearSelections();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			log("onPrepareActionMode");
			List<MegaShare> selected = getSelectedContacts();
			boolean deleteShare = false;
			boolean permissions = false;
						
			if (selected.size() > 0) {
				permissions = true;
				deleteShare = true;
			}
			
			menu.findItem(R.id.action_file_contact_list_permissions).setVisible(permissions);
			menu.findItem(R.id.action_file_contact_list_delete).setVisible(deleteShare);

			
			return false;
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate");
		super.onCreate(savedInstanceState);
		
		if (megaApi == null){
			megaApi = ((MegaApplication) getApplication()).getMegaApi();
		}		
		
		megaApi.addGlobalListener(this);
		
		aB = getSupportActionBar();
		aB.setHomeButtonEnabled(true);
		aB.setDisplayShowTitleEnabled(true);
		aB.setLogo(R.drawable.ic_action_navigation_accept);
		aB.setTitle(getString(R.string.file_contact_list_activity));
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);	    
	    
	    Bundle extras = getIntent().getExtras();
		if (extras != null){
			nodeHandle = extras.getLong("name");
			node=megaApi.getNodeByHandle(nodeHandle);				
			
			setContentView(R.layout.activity_file_contact_list);
			imageView = (ImageView) findViewById(R.id.file_properties_icon);
			nameView = (TextView) findViewById(R.id.node_name);
			createdView = (TextView) findViewById(R.id.node_last_update);
			contactLayout = (RelativeLayout) findViewById(R.id.file_contact_list_layout);
			contactLayout.setOnClickListener(this);
			fileContactLayout = (RelativeLayout) findViewById(R.id.file_contact_list_browser_layout);
						
			nameView.setText(node.getName());		
			
			imageView.setImageResource(R.drawable.mime_folder_shared);	
			
			listContacts = megaApi.getOutShares(node);							
					
			listView = (ListView) findViewById(R.id.file_contact_list_view_browser);
			listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listView.setItemsCanFocus(false);
			
			emptyImage = (ImageView) findViewById(R.id.file_contact_list_empty_image);
			emptyText = (TextView) findViewById(R.id.file_contact_list_empty_text);
			if (listContacts.size() != 0){
				emptyImage.setVisibility(View.GONE);
				emptyText.setVisibility(View.GONE);
				fileContactLayout.setVisibility(View.VISIBLE);
				listView.setVisibility(View.VISIBLE);
			}			
			else{
				fileContactLayout.setVisibility(View.VISIBLE);
				emptyImage.setVisibility(View.VISIBLE);
				emptyText.setVisibility(View.VISIBLE);
				
				listView.setVisibility(View.GONE);
				emptyImage.setImageResource(R.drawable.ic_empty_folder);
				emptyText.setText(R.string.file_browser_empty_folder);
			}
			
			if (node.getCreationTime() != 0){
				try {createdView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));
				}catch(Exception ex){
					createdView.setText("");
				}				
				
			}
			else{
				createdView.setText("");				
			}
						
			if (adapter == null){
				
				adapter = new MegaSharedFolderAdapter(this, node, listContacts, listView);
				listView.setAdapter(adapter);
				adapter.setShareList(listContacts);		
			}
			else{
				adapter.setShareList(listContacts);
				//adapter.setParentHandle(-1);
			}
						
			adapter.setPositionClicked(-1);
			adapter.setMultipleSelect(false);
			
			listView.setAdapter(adapter);
		}
	}
	
	@Override
    protected void onDestroy(){
    	super.onDestroy();
    	
    	megaApi.removeGlobalListener(this);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_folder_contact_list, menu);
	    
//	    permissionButton = menu.findItem(R.id.action_file_contact_list_permissions);
//	    deleteShareButton = menu.findItem(R.id.action_file_contact_list_delete);
	    addSharingContact = menu.findItem(R.id.action_folder_contacts_list_share_folder);
	    
//	    permissionButton.setVisible(false);
//	    deleteShareButton.setVisible(false);
	    addSharingContact.setVisible(true);
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
		    case android.R.id.home:{
		    	onBackPressed();
		    	return true;
		    }
		    case R.id.action_folder_contacts_list_share_folder:{
		    	//Option add new contact to share
		    	
		    	shareFolderContactsDialog = new ShareFolderContactsDialog();
		    	shareFolderContactsDialog.setNode(node);
		    	shareFolderContactsDialog.show(getSupportFragmentManager(), "fragment_share_folder_contacts");
		    	
	        	return true;
	        }
		    default:{
	            return super.onOptionsItemSelected(item);
	        }
	    }
	}
	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		if (request.getType() == MegaRequest.TYPE_SHARE) {
			log("onRequestStart - Share");
		}
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestFinish");
		if (request.getType() == MegaRequest.TYPE_SHARE){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}

			if (e.getErrorCode() == MegaError.API_OK){

				if(removeShare){
					Toast.makeText(this, "The contacts have been removed", Toast.LENGTH_SHORT).show();
					removeShare=false;

				}
				else{
					if(changeShare){
						permissionsDialog.dismiss();
						Toast.makeText(this, "The permissions have been changed", Toast.LENGTH_SHORT).show();
						changeShare=false;
					}
				}
				
			}

			adapter.setShareList(listContacts);
			listView.invalidateViews();
		}
		else{
			if(removeShare){
				Toast.makeText(this, "The contacts have not been removed", Toast.LENGTH_SHORT).show();
				removeShare=false;

			}
			if(changeShare)
				Toast.makeText(this, "The permissions have not been changed", Toast.LENGTH_SHORT).show();
		}
		log("Finish onRequestFinish");
	}


	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError");
	}
	
	public static void log(String log) {
		Util.log("FileContactListActivity", log);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		log("onItemClick");
		if(adapter.isMultipleSelect()){
			log("isMultipleSelect");
			SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
			if (checkedItems.get(position, false) == true) {
				listView.setItemChecked(position, true);
			} else {
				listView.setItemChecked(position, false);
			}
			updateActionModeTitle();
			adapter.notifyDataSetChanged();
			
		}
	}

	@Override
	public void onBackPressed() {
					
		if (adapter.getPositionClicked() != -1){
			adapter.setPositionClicked(-1);
			adapter.notifyDataSetChanged();
		}
		else{
			if (parentHandleStack.isEmpty()){
				super.onBackPressed();
			}
			else{
				parentHandle = parentHandleStack.pop();
				listView.setVisibility(View.VISIBLE);
				emptyImage.setVisibility(View.GONE);
				emptyText.setVisibility(View.GONE);
				if (parentHandle == -1){
					aB.setTitle(getString(R.string.file_contact_list_activity));
					aB.setLogo(R.drawable.ic_action_navigation_accept);
					supportInvalidateOptionsMenu();
					adapter.setShareList(listContacts);
					listView.setSelection(0);
				}
				else{
					contactNodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandle));
					aB.setTitle(megaApi.getNodeByHandle(parentHandle).getName());
					aB.setLogo(R.drawable.ic_action_navigation_previous_item);
					supportInvalidateOptionsMenu();
					adapter.setShareList(listContacts);
					listView.setSelection(0);
				}
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		log("onItemLongClick");

		if (adapter.getPositionClicked() == -1){
			clearSelections();
			actionMode = startSupportActionMode(new ActionBarCallBack());
			listView.setItemChecked(position, true);
			adapter.setMultipleSelect(true);
			updateActionModeTitle();
			listView.setOnItemLongClickListener(null);
		}
		return true;
	}
	
	/*
	 * Clear all selected items
	 */
	private void clearSelections() {
		SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
		for (int i = 0; i < checkedItems.size(); i++) {
			if (checkedItems.valueAt(i) == true) {
				int checkedPosition = checkedItems.keyAt(i);
				listView.setItemChecked(checkedPosition, false);
			}
		}
		updateActionModeTitle();
	}
	
	private void updateActionModeTitle() {
		if (actionMode == null) {
			return;
		}
		log("updateActionModeTitle");
		List<MegaShare> contacts = getSelectedContacts();
		
		Resources res = getResources();
		String format = "%d %s";
	
		actionMode.setTitle(String.format(format, contacts.size(),res.getQuantityString(R.plurals.general_num_contacts, contacts.size())));
		try {
			actionMode.invalidate();
		} catch (NullPointerException e) {
			e.printStackTrace();
			log("oninvalidate error");
		}		
	}

	/*
	 * Get list of all selected documents
	 */
	private List<MegaShare> getSelectedContacts() {
		
		ArrayList<MegaShare> contacts = new ArrayList<MegaShare>();
		SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
		for (int i = 0; i < checkedItems.size(); i++) {
			if (checkedItems.valueAt(i) == true) {
				MegaShare contact = adapter.getContactAt(checkedItems.keyAt(i));
				if (contact != null){
					contacts.add(contact);
				}
			}
		}
		log("Contacts Size: "+contacts.size());
		return contacts;		
	}
	
	/*
	 * Disable selection
	 */
	void hideMultipleSelect() {
		adapter.setMultipleSelect(false);
		if (actionMode != null) {
			actionMode.finish();
		}
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()){
		
			case R.id.file_contact_list_layout:{
				Intent i = new Intent(this, ManagerActivity.class);
				i.setAction(ManagerActivity.ACTION_REFRESH_PARENTHANDLE_BROWSER);
				i.putExtra("parentHandle", node.getHandle());
				startActivity(i);
				finish();
				break;
			}
		}
	}

	/*
	 * Handle processed upload intent
	 */
	public void onIntentProcessed() {
		List<ShareInfo> infos = filePreparedInfos;
		if (statusDialog != null) {
			try { 
				statusDialog.dismiss(); 
			} 
			catch(Exception ex){}
		}
		
		MegaNode parentNode = megaApi.getNodeByHandle(parentHandle); 
		if(parentNode == null){
			Util.showErrorAlertDialog(getString(R.string.error_temporary_unavaible), false, this);
			return;
		}
			
		if (infos == null) {
			Util.showErrorAlertDialog(getString(R.string.upload_can_not_open),
					false, this);
		} 
		else {
			Toast.makeText(getApplicationContext(), getString(R.string.upload_began),
					Toast.LENGTH_SHORT).show();
			for (ShareInfo info : infos) {
				Intent intent = new Intent(this, UploadService.class);
				intent.putExtra(UploadService.EXTRA_FILEPATH, info.getFileAbsolutePath());
				intent.putExtra(UploadService.EXTRA_NAME, info.getTitle());
				intent.putExtra(UploadService.EXTRA_PARENT_HASH, parentNode.getHandle());
				intent.putExtra(UploadService.EXTRA_SIZE, info.getSize());
				startService(intent);
			}
		}
	}
	
	@Override
	public void onUsersUpdate(MegaApiJava api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodesUpdate(MegaApiJava api) {

		log("onNodesUpdate");

		if (node.isFolder()){

			listContacts = megaApi.getOutShares(node);
			if (listContacts != null){
				if (listContacts.size() > 0){
					fileContactLayout.setVisibility(View.VISIBLE);

					if (adapter != null){
						adapter.setNode(node);
						adapter.setContext(this);
						adapter.setShareList(listContacts);
						adapter.setListViewActivity(listView);
					}
					else{
						adapter = new MegaSharedFolderAdapter(this, node, listContacts, listView);
					}

				}
				else{
					fileContactLayout.setVisibility(View.GONE);
					//((RelativeLayout.LayoutParams)infoTable.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.file_properties_image);
				}
			}			
		}

		listView.invalidateViews();
	}

	@Override
	public void onReloadNeeded(MegaApiJava api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}
}

