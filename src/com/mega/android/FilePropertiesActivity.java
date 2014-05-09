package com.mega.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mega.android.FileStorageActivity.Mode;
import com.mega.components.MySwitch;
import com.mega.components.RoundedImageView;
import com.mega.sdk.MegaApiAndroid;
import com.mega.sdk.MegaApiJava;
import com.mega.sdk.MegaError;
import com.mega.sdk.MegaNode;
import com.mega.sdk.MegaRequest;
import com.mega.sdk.MegaRequestListenerInterface;
import com.mega.sdk.NodeList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FilePropertiesActivity extends ActionBarActivity implements OnClickListener, MegaRequestListenerInterface, OnCheckedChangeListener{
	
	ImageView iconView;
	TextView nameView;
	TextView availableOfflineView;
	RoundedImageView imageView;
	RelativeLayout availableOfflineLayout;
	MySwitch availableSwitchOnline;
	MySwitch availableSwitchOffline;
	ActionBar aB;
	
	TextView sizeTextView;
	TextView sizeTitleTextView;
	TextView addedTextView;
	TextView modifiedTextView;
	
	MegaNode node;
	long handle;
	
	boolean availableOfflineBoolean = false;
	
	private MegaApiAndroid megaApi = null;
	public FilePropertiesActivity filePropertiesActivity;
	
	ProgressDialog statusDialog;
	
	private static int EDIT_TEXT_ID = 1;
	private Handler handler;
	
	private AlertDialog renameDialog;

	boolean moveToRubbish = false;
	
	public static int REQUEST_CODE_SELECT_MOVE_FOLDER = 1001;
	public static int REQUEST_CODE_SELECT_COPY_FOLDER = 1002;
	public static int REQUEST_CODE_SELECT_LOCAL_FOLDER = 1004;
	
	MenuItem downloadMenuItem; 
	
	boolean shareIt = true;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (megaApi == null){
			MegaApplication app = (MegaApplication)getApplication();
			megaApi = app.getMegaApi();
		}
		filePropertiesActivity = this;
		handler = new Handler();
		
		aB = getSupportActionBar();
		aB.setHomeButtonEnabled(true);
		aB.setDisplayShowTitleEnabled(false);
		aB.setLogo(R.drawable.ic_action_navigation_accept);
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			int imageId = extras.getInt("imageId");
			String name = extras.getString("name");
			handle = extras.getLong("handle", -1);
			node = megaApi.getNodeByHandle(handle);
			if (node == null){
				finish();
			}
			
			name = node.getName();
					
			setContentView(R.layout.activity_file_properties);
			iconView = (ImageView) findViewById(R.id.file_properties_icon);
			nameView = (TextView) findViewById(R.id.file_properties_name);
			imageView = (RoundedImageView) findViewById(R.id.file_properties_image);
			imageView.getLayoutParams().width = Util.px2dp((270*scaleW), outMetrics);
			imageView.getLayoutParams().height = Util.px2dp((270*scaleH), outMetrics);
			((RelativeLayout.LayoutParams) imageView.getLayoutParams()).setMargins(Util.px2dp((9*scaleW), outMetrics), Util.px2dp((9*scaleH), outMetrics), Util.px2dp((9*scaleW), outMetrics), Util.px2dp((9*scaleH), outMetrics));
			availableOfflineLayout = (RelativeLayout) findViewById(R.id.file_properties_available_offline);
			availableOfflineView = (TextView) findViewById(R.id.file_properties_available_offline_text);
			availableSwitchOnline = (MySwitch) findViewById(R.id.file_properties_switch_online);
			availableSwitchOnline.setChecked(true);
			availableSwitchOffline = (MySwitch) findViewById(R.id.file_properties_switch_offline);
			availableSwitchOffline.setChecked(false);
			availableSwitchOnline.setOnCheckedChangeListener(this);			
			availableSwitchOffline.setOnCheckedChangeListener(this);			
			
			sizeTitleTextView  = (TextView) findViewById(R.id.file_properties_info_menu_size);
			sizeTextView = (TextView) findViewById(R.id.file_properties_info_data_size);
			addedTextView = (TextView) findViewById(R.id.file_properties_info_data_added);
			modifiedTextView = (TextView) findViewById(R.id.file_properties_info_data_modified);
						
			imageView.setImageResource(imageId);
			nameView.setText(name);
			nameView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			nameView.setSingleLine();
			nameView.setTypeface(null, Typeface.BOLD);
			
			iconView.getLayoutParams().height = Util.px2dp((20*scaleH), outMetrics);
			iconView.setImageResource(imageId);
			((RelativeLayout.LayoutParams)iconView.getLayoutParams()).setMargins(Util.px2dp((30*scaleW), outMetrics), Util.px2dp((15*scaleH), outMetrics), 0, 0);


			File destination = null;
			File offlineFile = null;
			if (node.isFile()){
				
				sizeTitleTextView.setText(getString(R.string.file_properties_info_size_file));
				
				sizeTextView.setText(Formatter.formatFileSize(this, node.getSize()));
			
				destination = null;
				if (Environment.getExternalStorageDirectory() != null){
					destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/");
				}
//				if (getExternalFilesDir(null) != null){
//					destination = new File (getExternalFilesDir(null), node.getHandle()+"");
//				}
				else{
					destination = new File(getFilesDir(), node.getHandle()+"");
				}
				
				if (destination.exists() && destination.isDirectory()){
					offlineFile = new File(destination, node.getHandle() + "_" + node.getName());
					if (offlineFile.exists() && node.getSize() == offlineFile.length() && offlineFile.getName().equals(node.getHandle() + "_" + node.getName())){ //This means that is already available offline
						availableOfflineBoolean = true;
						availableSwitchOffline.setVisibility(View.VISIBLE);
						availableSwitchOnline.setVisibility(View.GONE);
					}
					else{
						availableOfflineBoolean = false;
						availableSwitchOffline.setVisibility(View.GONE);
						availableSwitchOnline.setVisibility(View.VISIBLE);
						removeOffline();
						supportInvalidateOptionsMenu();
					}
				}
				else{
					availableOfflineBoolean = false;
					availableSwitchOffline.setVisibility(View.GONE);
					availableSwitchOnline.setVisibility(View.VISIBLE);
					removeOffline();
					supportInvalidateOptionsMenu();
				}
				
				
				availableOfflineView.setPadding(Util.px2dp(30*scaleW, outMetrics), 0, Util.px2dp(40*scaleW, outMetrics), 0);
			}
			else{
				sizeTitleTextView.setText(getString(R.string.file_properties_info_size_folder));
				
				sizeTextView.setText(getInfoFolder(node));
				
				availableOfflineLayout.setVisibility(View.INVISIBLE);
			}
			
			if (node.getCreationTime() != 0){
				try {addedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{addedTextView.setText("");}
				
				if (node.getModificationTime() != 0){
					try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getModificationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}
				}
				else{
					try {modifiedTextView.setText(DateUtils.getRelativeTimeSpanString(node.getCreationTime() * 1000));}catch(Exception ex)	{modifiedTextView.setText("");}	
				}
			}
			else{
				addedTextView.setText("");
				modifiedTextView.setText("");
			}
			
			
			Bitmap thumb = null;
			Bitmap preview = null;
			//If image
			if (node.isFile()){
				if (node.hasThumbnail()){
					if (availableOfflineBoolean){
						if (offlineFile != null){
							
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inJustDecodeBounds = true;
							thumb = BitmapFactory.decodeFile(offlineFile.getAbsolutePath(), options);
							
							ExifInterface exif;
							int orientation = ExifInterface.ORIENTATION_NORMAL;
							try {
								exif = new ExifInterface(offlineFile.getAbsolutePath());
								orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
							} catch (IOException e) {}  
							
							// Calculate inSampleSize
						    options.inSampleSize = Util.calculateInSampleSize(options, 270, 270);
						    
						    // Decode bitmap with inSampleSize set
						    options.inJustDecodeBounds = false;
						    
						    thumb = BitmapFactory.decodeFile(offlineFile.getAbsolutePath(), options);
							if (thumb != null){
								thumb = Util.rotateBitmap(thumb, orientation);
								
								imageView.setImageBitmap(thumb);
							}
						}
					}
					else{
						thumb = ThumbnailUtils.getThumbnailFromCache(node);
						if (thumb != null){
							imageView.setImageBitmap(thumb);
						}
						else{
							thumb = ThumbnailUtils.getThumbnailFromFolder(node, this);
							if (thumb != null){
								imageView.setImageBitmap(thumb);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
			
		}
	}
	

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked){
			availableOfflineBoolean = false;
			availableSwitchOffline.setVisibility(View.GONE);
			availableSwitchOnline.setVisibility(View.VISIBLE);
			availableSwitchOffline.setChecked(false);			
			if (node.isFile()){
				removeOffline();
			}
			supportInvalidateOptionsMenu();
		}
		else{
			availableOfflineBoolean = true;
			availableSwitchOffline.setVisibility(View.VISIBLE);
			availableSwitchOnline.setVisibility(View.GONE);
			availableSwitchOnline.setChecked(true);
			if (node.isFile()){
				saveOffline();
			}
			supportInvalidateOptionsMenu();
		}		
	}
	
	public void saveOffline (){
		
		if (node.isFile()){
			File destination = null;
			if (Environment.getExternalStorageDirectory() != null){
				destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/");
			}
//			if (getExternalFilesDir(null) != null){
//				destination = new File (getExternalFilesDir(null), node.getHandle()+"");
//			}
			else{
				destination = new File(getFilesDir(), node.getHandle()+"");
			}
			
			if (destination.exists() && destination.isDirectory()){
				File offlineFile = new File(destination, node.getHandle() + "_" + node.getName());
				if (offlineFile.exists() && node.getSize() == offlineFile.length() && offlineFile.getName().equals(node.getHandle() + "_" + node.getName())){ //This means that is already available offline
					return;
				}
			}
			
			destination.mkdirs();
			
			double availableFreeSpace = Double.MAX_VALUE;
			try{
				StatFs stat = new StatFs(destination.getAbsolutePath());
				availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
			}
			catch(Exception ex){}
			
			Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
			if (node.getType() == MegaNode.TYPE_FOLDER) {
				getDlList(dlFiles, node, new File(destination, new String(node.getName())));
			} else {
				dlFiles.put(node, destination.getAbsolutePath());
			}
			
			for (MegaNode document : dlFiles.keySet()) {
				
				String path = dlFiles.get(document);
				
				if(availableFreeSpace <document.getSize()){
					Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space) + " (" + new String(document.getName()) + ")", false, this);
					continue;
				}
				
				String url = null;
				Intent service = new Intent(this, DownloadService.class);
				service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
				service.putExtra(DownloadService.EXTRA_URL, url);
				service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
				service.putExtra(DownloadService.EXTRA_PATH, path);
				service.putExtra(DownloadService.EXTRA_OFFLINE, true);
				startService(service);
			}
		}
		else if (node.isFolder()){
			Toast.makeText(this, "IS FOLDER (not yet implemented)", Toast.LENGTH_LONG).show();
		}
	}
	
	public void removeOffline(){
		if (node.isFile()){
			File destination = null;
			if (Environment.getExternalStorageDirectory() != null){
				destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/");
			}
//			if (getExternalFilesDir(null) != null){
//				destination = new File (getExternalFilesDir(null), node.getHandle()+"");
//			}
			else{
				destination = new File(getFilesDir(), node.getHandle()+"");
			}
			
			try{
				File offlineFile = new File(destination, node.getHandle() + "_" + node.getName());
				delete(offlineFile);
			}
			catch(Exception e){};
			
			MediaScannerConnection.scanFile(this,
					new String[] { Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR}, null,
			        new MediaScannerConnection.OnScanCompletedListener() {
			      		public void onScanCompleted(String path, Uri uri) {
			      			log("Scanned: " + path);
			      		}
			 		}
			);			
		}
		else if (node.isFolder()){
			Toast.makeText(this, "Folder remove (not yet implemented)", Toast.LENGTH_LONG).show();
		}
		
		
	}
	
	void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles()){
				delete(c);
			}
		}
		
		if (!f.delete()){
			throw new FileNotFoundException("Failed to delete file: " + f);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
		    case android.R.id.home:{
		    	finish();
		    	return true;
		    }
		    case R.id.action_file_properties_download:{
		    	if (!availableOfflineBoolean){
			    	ArrayList<Long> handleList = new ArrayList<Long>();
					handleList.add(node.getHandle());
					downloadNode(handleList);
		    	}
		    	else{
		    		
		    		File destination = null;
					File offlineFile = null;
					if (Environment.getExternalStorageDirectory() != null){
						destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/");
					}
//					if (getExternalFilesDir(null) != null){
//						destination = new File (getExternalFilesDir(null), node.getHandle()+"");
//					}
					else{
						destination = new File(getFilesDir(), node.getHandle()+"");
					}
					
					if (destination.exists() && destination.isDirectory()){
						offlineFile = new File(destination, node.getHandle() + "_" + node.getName());
						if (offlineFile.exists() && node.getSize() == offlineFile.length() && offlineFile.getName().equals(node.getHandle() + "_" + node.getName())){ //This means that is already available offline
							availableOfflineBoolean = true;
							availableSwitchOffline.setVisibility(View.VISIBLE);
							availableSwitchOnline.setVisibility(View.GONE);
						}
						else{
							availableOfflineBoolean = false;
							removeOffline();
							supportInvalidateOptionsMenu();
						}
					}
					else{
						availableOfflineBoolean = false;
						removeOffline();
						supportInvalidateOptionsMenu();
					}
		    		Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(offlineFile), MimeType.typeForName(offlineFile.getName()).getType());
					if (isIntentAvailable(this, intent)){
						startActivity(intent);
					}
					else{
						Toast.makeText(this, "There is not any app installed in the device to open this file", Toast.LENGTH_LONG).show();
					}
		    	}
				return true;
		    }
		    case R.id.action_file_properties_get_link:{
		    	shareIt = false;
		    	getPublicLinkAndShareIt();
		    	return true;
		    }
		    case R.id.action_file_properties_send_link:{
		    	shareIt = true;
		    	getPublicLinkAndShareIt();
		    	return true;
		    }
		    case R.id.action_file_properties_rename:{
		    	showRenameDialog();
		    	return true;
		    }
		    case R.id.action_file_properties_remove:{
		    	moveToTrash();
		    	return true;
		    }
		    case R.id.action_file_properties_move:{
		    	showMove();
		    	return true;
		    }
		    case R.id.action_file_properties_copy:{
		    	showCopy();
		    	return true;
		    }
		}	    
	    return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Display keyboard
	 */
	private void showKeyboardDelayed(final View view) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 50);
	}
	
	public void showCopy(){
		
		ArrayList<Long> handleList = new ArrayList<Long>();
		handleList.add(node.getHandle());
		
		Intent intent = new Intent(this, FileExplorerActivity.class);
		intent.setAction(FileExplorerActivity.ACTION_PICK_COPY_FOLDER);
		long[] longArray = new long[handleList.size()];
		for (int i=0; i<handleList.size(); i++){
			longArray[i] = handleList.get(i);
		}
		intent.putExtra("COPY_FROM", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_COPY_FOLDER);
	}
	
	public void showMove(){
		
		ArrayList<Long> handleList = new ArrayList<Long>();
		handleList.add(node.getHandle());
		
		Intent intent = new Intent(this, FileExplorerActivity.class);
		intent.setAction(FileExplorerActivity.ACTION_PICK_MOVE_FOLDER);
		long[] longArray = new long[handleList.size()];
		for (int i=0; i<handleList.size(); i++){
			longArray[i] = handleList.get(i);
		}
		intent.putExtra("MOVE_FROM", longArray);
		startActivityForResult(intent, REQUEST_CODE_SELECT_MOVE_FOLDER);
	}

	public void downloadNode(ArrayList<Long> handleList){
		
		long[] hashes = new long[handleList.size()];
		for (int i=0;i<handleList.size();i++){
			hashes[i] = handleList.get(i);
		}
		
		Intent intent = new Intent(Mode.PICK_FOLDER.getAction());
		intent.putExtra(FileStorageActivity.EXTRA_BUTTON_PREFIX, getString(R.string.context_download_to));
		intent.setClass(this, FileStorageActivity.class);
		intent.putExtra(FileStorageActivity.EXTRA_DOCUMENT_HASHES, hashes);
		startActivityForResult(intent, REQUEST_CODE_SELECT_LOCAL_FOLDER);
	}
	
	public void moveToTrash(){
		
		long handle = node.getHandle();
		moveToRubbish = false;
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		MegaNode rubbishNode = megaApi.getRubbishNode();

		//Check if the node is not yet in the rubbish bin (if so, remove it)
		MegaNode parent = megaApi.getNodeByHandle(handle);
		while (megaApi.getParentNode(parent) != null){
			parent = megaApi.getParentNode(parent);
		}
			
		if (parent.getHandle() != megaApi.getRubbishNode().getHandle()){
			moveToRubbish = true;
			megaApi.moveNode(megaApi.getNodeByHandle(handle), rubbishNode, this);
		}
		else{
			megaApi.remove(megaApi.getNodeByHandle(handle), this);
		}
		
		if (moveToRubbish){
			ProgressDialog temp = null;
			try{
				temp = new ProgressDialog(this);
				temp.setMessage(getString(R.string.context_move_to_trash));
				temp.show();
			}
			catch(Exception e){
				return;
			}
			statusDialog = temp;
		}
		else{
			ProgressDialog temp = null;
			try{
				temp = new ProgressDialog(this);
				temp.setMessage(getString(R.string.context_delete_from_mega));
				temp.show();
			}
			catch(Exception e){
				return;
			}
			statusDialog = temp;
		}
	}
	
	public void showRenameDialog(){
		
		final EditText input = new EditText(this);
		input.setId(EDIT_TEXT_ID);
		input.setSingleLine();
		input.setText(node.getName());
		input.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				input.performLongClick();
			}
		});

		input.setImeOptions(EditorInfo.IME_ACTION_DONE);

		input.setImeActionLabel(getString(R.string.context_rename),
				KeyEvent.KEYCODE_ENTER);

		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(final View v, boolean hasFocus) {
				if (hasFocus) {
					if (node.isFolder()){
						input.setSelection(0, input.getText().length());
					}
					else{
						String [] s = node.getName().split("\\.");
						if (s != null){
							int numParts = s.length;
							int lastSelectedPos = 0;
							if (numParts == 1){
								input.setSelection(0, input.getText().length());
							}
							else if (numParts > 1){
								for (int i=0; i<(numParts-1);i++){
									lastSelectedPos += s[i].length(); 
									lastSelectedPos++;
								}
								lastSelectedPos--; //The last point should not be selected)
								input.setSelection(0, lastSelectedPos);
							}
						}
						showKeyboardDelayed(v);
					}
				}
			}
		});

		AlertDialog.Builder builder = Util.getCustomAlertBuilder(this, getString(R.string.context_rename) + " "	+ new String(node.getName()), null, input);
		builder.setPositiveButton(getString(R.string.context_rename),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString().trim();
						if (value.length() == 0) {
							return;
						}
						rename(value);
					}
				});
		builder.setNegativeButton(getString(android.R.string.cancel), null);
		renameDialog = builder.create();
		renameDialog.show();

		input.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					renameDialog.dismiss();
					String value = v.getText().toString().trim();
					if (value.length() == 0) {
						return true;
					}
					rename(value);
					return true;
				}
				return false;
			}
		});
	}
	
	private void rename(String newName){
		if (newName.equals(node.getName())) {
			return;
		}
		
		if(!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if (isFinishing()){
			return;
		}
		
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(this);
			temp.setMessage(getString(R.string.context_renaming));
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;
		
		log("renaming " + node.getName() + " to " + newName);
		
		megaApi.renameNode(node, newName, this);
	}
	
	public void getPublicLinkAndShareIt(){
		
		if (!Util.isOnline(this)){
			Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
			return;
		}
		
		if(isFinishing()){
			return;	
		}
		
		ProgressDialog temp = null;
		try{
			temp = new ProgressDialog(this);
			temp.setMessage(getString(R.string.context_creating_link));
			temp.show();
		}
		catch(Exception e){
			return;
		}
		statusDialog = temp;
		
		megaApi.exportNode(node, this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_file_properties, menu);
	   
	    downloadMenuItem = menu.findItem(R.id.action_file_properties_download);
	    
	    if (availableOfflineBoolean){
	    	downloadMenuItem.setIcon(R.drawable.ic_action_collections_collection_dark);
	    }
	    else{
	    	downloadMenuItem.setIcon(R.drawable.ic_menu_download_dark);
	    }
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getName());
	}

	@SuppressLint("NewApi")
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		
		node = megaApi.getNodeByHandle(request.getNodeHandle());
		
		log("onRequestFinish");
		if (request.getType() == MegaRequest.TYPE_EXPORT){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				String link = request.getLink();
				if (filePropertiesActivity != null){
					if (shareIt){
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_TEXT, link);
						startActivity(Intent.createChooser(intent, getString(R.string.context_get_link)));
					}
					else{
						if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
						    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						    clipboard.setText(link);
						} else {
						    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", link);
				            clipboard.setPrimaryClip(clip);
						}
						
						Toast.makeText(this, getString(R.string.file_properties_get_link), Toast.LENGTH_LONG).show();
					}
				}
			}
			else{
				Toast.makeText(this, "Impossible to get the link", Toast.LENGTH_LONG).show();
			}
			log("export request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_RENAME){
			
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				Toast.makeText(this, "Correctly renamed", Toast.LENGTH_SHORT).show();
				nameView.setText(megaApi.getNodeByHandle(request.getNodeHandle()).getName());
			}			
			else{
				Toast.makeText(this, "The file has not been renamed", Toast.LENGTH_LONG).show();
			}
		}
		else if (request.getType() == MegaRequest.TYPE_MOVE){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (moveToRubbish){
				if (e.getErrorCode() == MegaError.API_OK){
					Toast.makeText(this, "Correctly moved to Rubbish bin", Toast.LENGTH_SHORT).show();
					finish();
				}
				else{
					Toast.makeText(this, "The file has not been removed", Toast.LENGTH_LONG).show();
				}
				moveToRubbish = false;
				log("move to rubbish request finished");
			}
			else{
				if (e.getErrorCode() == MegaError.API_OK){
					Toast.makeText(this, "Correctly moved", Toast.LENGTH_SHORT).show();
					finish();
				}
				else{
					Toast.makeText(this, "The file has not been moved", Toast.LENGTH_LONG).show();
				}
				log("move nodes request finished");
			}
		}
		else if (request.getType() == MegaRequest.TYPE_REMOVE){
			
			
			if (e.getErrorCode() == MegaError.API_OK){
				if (statusDialog.isShowing()){
					try { 
						statusDialog.dismiss();	
					} 
					catch (Exception ex) {}
					Toast.makeText(this, "Correctly deleted from MEGA", Toast.LENGTH_SHORT).show();
				}
				finish();
			}
			else{
				Toast.makeText(this, "The file has not been removed", Toast.LENGTH_LONG).show();
			}
			log("remove request finished");
		}
		else if (request.getType() == MegaRequest.TYPE_COPY){
			try { 
				statusDialog.dismiss();	
			} 
			catch (Exception ex) {}
			
			if (e.getErrorCode() == MegaError.API_OK){
				Toast.makeText(this, "Correctly copied", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(this, "The file has not been copied", Toast.LENGTH_LONG).show();
			}
			log("copy nodes request finished");
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError: " + request.getName());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (intent == null) {
			return;
		}
		
		if (requestCode == REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
			log("local folder selected");
			String parentPath = intent.getStringExtra(FileStorageActivity.EXTRA_PATH);
			double availableFreeSpace = Double.MAX_VALUE;
			try{
				StatFs stat = new StatFs(parentPath);
				availableFreeSpace = (double)stat.getAvailableBlocks() * (double)stat.getBlockSize();
			}
			catch(Exception ex){}
			
			String url = intent.getStringExtra(FileStorageActivity.EXTRA_URL);
			long size = intent.getLongExtra(FileStorageActivity.EXTRA_SIZE, 0);
			long[] hashes = intent.getLongArrayExtra(FileStorageActivity.EXTRA_DOCUMENT_HASHES);
			
			if(hashes.length == 1){
				MegaNode tempNode = megaApi.getNodeByHandle(hashes[0]);
				if((tempNode != null) && tempNode.getType() == MegaNode.TYPE_FILE){
					String localPath = Util.getLocalFile(this, tempNode.getName(), tempNode.getSize(), parentPath);
					if(localPath != null){	
						try { 
							Util.copyFile(new File(localPath), new File(parentPath, tempNode.getName())); 
						}
						catch(Exception e) {}
						
						Intent viewIntent = new Intent(Intent.ACTION_VIEW);
						viewIntent.setDataAndType(Uri.fromFile(new File(localPath)), MimeType.typeForName(tempNode.getName()).getType());
						if (isIntentAvailable(this, viewIntent))
							startActivity(viewIntent);
						else{
							Intent intentShare = new Intent(Intent.ACTION_SEND);
							intentShare.setDataAndType(Uri.fromFile(new File(localPath)), MimeType.typeForName(tempNode.getName()).getType());
							if (isIntentAvailable(this, intentShare))
								startActivity(intentShare);
							String toastMessage = getString(R.string.already_downloaded) + ": " + localPath;
							Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
						}								
						return;
					}
				}
			}
			
			for (long hash : hashes) {
				MegaNode node = megaApi.getNodeByHandle(hash);
				if(node != null){
					Map<MegaNode, String> dlFiles = new HashMap<MegaNode, String>();
					if (node.getType() == MegaNode.TYPE_FOLDER) {
						getDlList(dlFiles, node, new File(parentPath, new String(node.getName())));
					} else {
						dlFiles.put(node, parentPath);
					}
					
					for (MegaNode document : dlFiles.keySet()) {
						
						String path = dlFiles.get(document);
						
						if(availableFreeSpace <document.getSize()){
							Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space) + " (" + new String(document.getName()) + ")", false, this);
							continue;
						}
						
						Intent service = new Intent(this, DownloadService.class);
						service.putExtra(DownloadService.EXTRA_HASH, document.getHandle());
						service.putExtra(DownloadService.EXTRA_URL, url);
						service.putExtra(DownloadService.EXTRA_SIZE, document.getSize());
						service.putExtra(DownloadService.EXTRA_PATH, path);
						startService(service);
					}
				}
				else if(url != null) {
					if(availableFreeSpace < size) {
						Util.showErrorAlertDialog(getString(R.string.error_not_enough_free_space), false, this);
						continue;
					}
					
					Intent service = new Intent(this, DownloadService.class);
					service.putExtra(DownloadService.EXTRA_HASH, hash);
					service.putExtra(DownloadService.EXTRA_URL, url);
					service.putExtra(DownloadService.EXTRA_SIZE, size);
					service.putExtra(DownloadService.EXTRA_PATH, parentPath);
					startService(service);
				}
				else {
					log("node not found");
				}
			}
			Util.showToast(this, R.string.download_began);
		}
		else if (requestCode == REQUEST_CODE_SELECT_MOVE_FOLDER && resultCode == RESULT_OK) {
			
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final long[] moveHandles = intent.getLongArrayExtra("MOVE_HANDLES");
			final long toHandle = intent.getLongExtra("MOVE_TO", 0);
			final int totalMoves = moveHandles.length;
			
			MegaNode parent = megaApi.getNodeByHandle(toHandle);
			moveToRubbish = false;
			
			ProgressDialog temp = null;
			try{
				temp = new ProgressDialog(this);
				temp.setMessage(getString(R.string.context_moving));
				temp.show();
			}
			catch(Exception e){
				return;
			}
			statusDialog = temp;
			
			for(int i=0; i<moveHandles.length;i++){
				megaApi.moveNode(megaApi.getNodeByHandle(moveHandles[i]), parent, this);
			}
		}
		else if (requestCode == REQUEST_CODE_SELECT_COPY_FOLDER && resultCode == RESULT_OK){
			if(!Util.isOnline(this)){
				Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
				return;
			}
			
			final long[] copyHandles = intent.getLongArrayExtra("COPY_HANDLES");
			final long toHandle = intent.getLongExtra("COPY_TO", 0);
			final int totalCopy = copyHandles.length;
			
			ProgressDialog temp = null;
			try{
				temp = new ProgressDialog(this);
				temp.setMessage(getString(R.string.context_copying));
				temp.show();
			}
			catch(Exception e){
				return;
			}
			statusDialog = temp;
			
			MegaNode parent = megaApi.getNodeByHandle(toHandle);
			for(int i=0; i<copyHandles.length;i++){
				megaApi.copyNode(megaApi.getNodeByHandle(copyHandles[i]), parent, this);
			}
		}
	}
	
	/*
	 * Get list of all child files
	 */
	private void getDlList(Map<MegaNode, String> dlFiles, MegaNode parent, File folder) {
		
		if (megaApi.getRootNode() == null)
			return;
		
		folder.mkdir();
		NodeList nodeList = megaApi.getChildren(parent);
		for(int i=0; i<nodeList.size(); i++){
			MegaNode document = nodeList.get(i);
			if (document.getType() == MegaNode.TYPE_FOLDER) {
				File subfolder = new File(folder, new String(document.getName()));
				getDlList(dlFiles, document, subfolder);
			} 
			else {
				dlFiles.put(document, folder.getAbsolutePath());
			}
		}
	}
	
	/*
	 * If there is an application that can manage the Intent, returns true. Otherwise, false.
	 */
	public static boolean isIntentAvailable(Context ctx, Intent intent) {

		final PackageManager mgr = ctx.getPackageManager();
		List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	private String getInfoFolder (MegaNode n){
		NodeList nL = megaApi.getChildren(n);
		
		int numFolders = 0;
		int numFiles = 0;
		
		for (int i=0;i<nL.size();i++){
			MegaNode c = nL.get(i);
			if (c.isFolder()){
				numFolders++;
			}
			else{
				numFiles++;
			}
		}
		
		String info = "";
		if (numFolders > 0){
			info = numFolders +  " " + getResources().getQuantityString(R.plurals.general_num_folders, numFolders);
			if (numFiles > 0){
				info = info + ", " + numFiles + " " + getResources().getQuantityString(R.plurals.general_num_files, numFiles);
			}
		}
		else {
			info = numFiles +  " " + getResources().getQuantityString(R.plurals.general_num_files, numFiles);
		}
		
		return info;
	}

	public static void log(String message) {
		Util.log("FilePropertiesActivity", message);
	}
}
