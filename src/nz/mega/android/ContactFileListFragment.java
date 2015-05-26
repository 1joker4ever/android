package nz.mega.android;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import nz.mega.android.utils.Util;
import nz.mega.components.RoundedImageView;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaUser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ContactFileListFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener, OnClickListener, MegaRequestListenerInterface {

	MegaApiAndroid megaApi;
	ActionBar aB;
	Context context;
	ContactFileListFragment contactFileListFragment = this;

	String userEmail;

	TextView nameView;
	RoundedImageView imageView;
	TextView contactInitialLetter;
	ImageView statusDot;
	TextView textViewContent;

	RelativeLayout contactLayout;
	ListView listView;
	ImageView emptyImageView;
	TextView emptyTextView;

	MegaUser contact;
	ArrayList<MegaNode> contactNodes;

	MegaBrowserListAdapter adapter;

	long parentHandle = -1;

	Stack<Long> parentHandleStack = new Stack<Long>();

	private ActionMode actionMode;
	
	private boolean name = false;
	private boolean firstName = false;
	String nameText;
	String firstNameText;

	ProgressDialog statusDialog;

	public static int REQUEST_CODE_GET = 1000;
	public static int REQUEST_CODE_GET_LOCAL = 1003;
	public static int REQUEST_CODE_SELECT_COPY_FOLDER = 1002;
	public static int REQUEST_CODE_SELECT_LOCAL_FOLDER = 1004;
	public static int REQUEST_CODE_SELECT_MOVE_FOLDER = 1001;

	private int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;

	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;

	ArrayList<MegaTransfer> tL;
	HashMap<Long, MegaTransfer> mTHash = null;
	long lastTimeOnTransferUpdate = -1;

	private class ActionBarCallBack implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			List<MegaNode> documents = getSelectedDocuments();

			switch (item.getItemId()) {
			case R.id.cab_menu_download: {
				ArrayList<Long> handleList = new ArrayList<Long>();
				for (int i = 0; i < documents.size(); i++) {
					handleList.add(documents.get(i).getHandle());
				}
				clearSelections();
				hideMultipleSelect();
				((ContactPropertiesMainActivity)context).onFileClick(handleList);
				break;
			}
			case R.id.cab_menu_rename: {
				clearSelections();
				hideMultipleSelect();
				if (documents.size() == 1) {
					((ContactPropertiesMainActivity)context).showRenameDialog(documents.get(0),
							documents.get(0).getName());
				}
				break;
			}
			case R.id.cab_menu_copy: {
				ArrayList<Long> handleList = new ArrayList<Long>();
				for (int i = 0; i < documents.size(); i++) {
					handleList.add(documents.get(i).getHandle());
				}
				clearSelections();
				hideMultipleSelect();

				((ContactPropertiesMainActivity)context).showCopy(handleList);
				break;
			}
			case R.id.cab_menu_move: {
				ArrayList<Long> handleList = new ArrayList<Long>();
				for (int i = 0; i < documents.size(); i++) {
					handleList.add(documents.get(i).getHandle());
				}
				clearSelections();
				hideMultipleSelect();
				((ContactPropertiesMainActivity)context).showMove(handleList);
				break;
			}
			case R.id.cab_menu_share_link: {
				clearSelections();
				hideMultipleSelect();
				if (documents.size() == 1) {
					// ((ManagerActivity)
					// context).getPublicLinkAndShareIt(documents.get(0));
				}
				break;
			}
			case R.id.cab_menu_trash: {
				ArrayList<Long> handleList = new ArrayList<Long>();
				for (int i = 0; i < documents.size(); i++) {
					handleList.add(documents.get(i).getHandle());
				}
				clearSelections();
				hideMultipleSelect();
				((ContactPropertiesMainActivity)context).moveToTrash(handleList);
				break;
			}

			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.file_browser_action, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			adapter.setMultipleSelect(false);
			listView.setOnItemLongClickListener(contactFileListFragment);
			clearSelections();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			List<MegaNode> selected = getSelectedDocuments();
			boolean showDownload = false;
			boolean showRename = false;
			boolean showCopy = false;
			boolean showMove = false;
			boolean showLink = false;
			boolean showTrash = false;

			// Rename
			if(selected.size() == 1){
				if ((megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK) || (megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK)) {
					showRename = true;
				}
			}

			if (selected.size() > 0) {
				showDownload = true;
				showCopy = true;
				if ((megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK) || (megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK)) {
					showTrash = true;
					showMove = true;	
				}				
			}

			menu.findItem(R.id.cab_menu_download).setVisible(showDownload);
			menu.findItem(R.id.cab_menu_rename).setVisible(showRename);
			menu.findItem(R.id.cab_menu_copy).setVisible(showCopy);
			menu.findItem(R.id.cab_menu_move).setVisible(showMove);
			menu.findItem(R.id.cab_menu_share_link).setVisible(showLink);
			menu.findItem(R.id.cab_menu_trash).setVisible(showTrash);

			return false;
		}

	}

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

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = ((Activity) context).getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);

		View v = null;

		if (userEmail != null){
			v = inflater.inflate(R.layout.fragment_contact_file_list, container, false);

			if (aB != null){
				aB.setTitle(R.string.contact_shared_files);
				aB.setLogo(R.drawable.ic_action_navigation_accept_white);
			}

			nameView = (TextView) v.findViewById(R.id.contact_file_list_name);
			imageView = (RoundedImageView) v.findViewById(R.id.contact_file_list_thumbnail);
			contactInitialLetter = (TextView) v.findViewById(R.id.contact_file_list_initial_letter);
			statusDot = (ImageView) v.findViewById(R.id.contact_file_list_status_dot);
			textViewContent = (TextView) v.findViewById(R.id.contact_file_list_content);
			contactLayout = (RelativeLayout) v.findViewById(R.id.contact_file_list_contact_layout);
			contactLayout.setOnClickListener(this);

			nameView.setText(userEmail);
			contact = megaApi.getContact(userEmail);
			if(contact == null)
			{
				return null;
			}
			name=false;
			firstName=false;
			megaApi.getUserAttribute(contact, 1, this);
			megaApi.getUserAttribute(contact, 2, this);
			
			createDefaultAvatar();

			File avatar = null;
			if (context.getExternalCacheDir() != null) {
				avatar = new File(context.getExternalCacheDir().getAbsolutePath(),
						contact.getEmail() + ".jpg");
			} else {
				avatar = new File(context.getCacheDir().getAbsolutePath(),
						contact.getEmail() + ".jpg");
			}
			Bitmap imBitmap = null;
			if (avatar.exists()) {
				if (avatar.length() > 0) {
					BitmapFactory.Options bOpts = new BitmapFactory.Options();
					bOpts.inPurgeable = true;
					bOpts.inInputShareable = true;
					imBitmap = BitmapFactory.decodeFile(
							avatar.getAbsolutePath(), bOpts);
					if (imBitmap == null) {
						avatar.delete();
						if (context.getExternalCacheDir() != null) {
							megaApi.getUserAvatar(contact,
									context.getExternalCacheDir().getAbsolutePath()
									+ "/" + contact.getEmail(), this);
						} else {
							megaApi.getUserAvatar(contact,
									context.getCacheDir().getAbsolutePath() + "/"
											+ contact.getEmail(), this);
						}
					} else {
						imageView.setImageBitmap(imBitmap);
						contactInitialLetter.setVisibility(View.GONE);
					}
				}
			}

			contactNodes = megaApi.getInShares(contact);
			textViewContent.setText(getDescription(contactNodes));

			listView = (ListView) v.findViewById(R.id.contact_file_list_view_browser);
			listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listView.setItemsCanFocus(false);

			emptyImageView = (ImageView) v.findViewById(R.id.contact_file_list_empty_image);
			emptyTextView = (TextView) v.findViewById(R.id.contact_file_list_empty_text);
			if (contactNodes.size() != 0) {
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			} else {
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				emptyImageView.setImageResource(R.drawable.ic_empty_folder);
				emptyTextView.setText(R.string.file_browser_empty_folder);
			}

			if (adapter == null) {
				adapter = new MegaBrowserListAdapter(context, contactNodes, -1,listView, aB,ManagerActivity.CONTACT_FILE_ADAPTER);
				if (mTHash != null){
					adapter.setTransfers(mTHash);
				}
			} else {
				adapter.setNodes(contactNodes);
				adapter.setParentHandle(-1);
			}

			adapter.setPositionClicked(-1);
			adapter.setMultipleSelect(false);

			listView.setAdapter(adapter);
		}

		return v;
	}
	
	public void createDefaultAvatar(){
		log("createDefaultAvatar()");
		
		Bitmap defaultAvatar = Bitmap.createBitmap(ManagerActivity.DEFAULT_AVATAR_WIDTH_HEIGHT,ManagerActivity.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(context.getResources().getColor(R.color.color_default_avatar_mega));
		
		int radius; 
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
        	radius = defaultAvatar.getWidth()/2;
        else
        	radius = defaultAvatar.getHeight()/2;
        
		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
		imageView.setImageBitmap(defaultAvatar);
		
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = context.getResources().getDisplayMetrics().density;
	    
	    int avatarTextSize = getAvatarTextSize(density);
	    log("DENSITY: " + density + ":::: " + avatarTextSize);
	    if (userEmail != null){
		    if (userEmail.length() > 0){
		    	String firstLetter = userEmail.charAt(0) + "";
		    	firstLetter = firstLetter.toUpperCase(Locale.getDefault());
		    	contactInitialLetter.setVisibility(View.VISIBLE);
		    	contactInitialLetter.setText(firstLetter);
		    	contactInitialLetter.setTextSize(32);
		    	contactInitialLetter.setTextColor(Color.WHITE);
		    }
	    }
	}
		
	private int getAvatarTextSize (float density){
		float textSize = 0.0f;
		
		if (density > 3.0){
			textSize = density * (DisplayMetrics.DENSITY_XXXHIGH / 72.0f);
		}
		else if (density > 2.0){
			textSize = density * (DisplayMetrics.DENSITY_XXHIGH / 72.0f);
		}
		else if (density > 1.5){
			textSize = density * (DisplayMetrics.DENSITY_XHIGH / 72.0f);
		}
		else if (density > 1.0){
			textSize = density * (72.0f / DisplayMetrics.DENSITY_HIGH / 72.0f);
		}
		else if (density > 0.75){
			textSize = density * (72.0f / DisplayMetrics.DENSITY_MEDIUM / 72.0f);
		}
		else{
			textSize = density * (72.0f / DisplayMetrics.DENSITY_LOW / 72.0f); 
		}
		
		return (int)textSize;
	}

	public boolean showUpload(){
		if (!parentHandleStack.isEmpty()){
			if ((megaApi.checkAccess(megaApi.getNodeByHandle(parentHandle), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK) || (megaApi.checkAccess(megaApi.getNodeByHandle(parentHandle), MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK)) {
				return true;
			}
		}

		return false;
	}

	public void setNodes(long parentHandle){
		if (megaApi.getNodeByHandle(parentHandle) == null){
			parentHandle = -1;
			this.parentHandle = -1;
			((ContactPropertiesMainActivity)context).setParentHandle(parentHandle);
			adapter.setParentHandle(parentHandle);
			if (aB != null){
				aB.setTitle(R.string.contact_shared_files);
				aB.setLogo(R.drawable.ic_action_navigation_accept_white);
			}
			setNodes(megaApi.getInShares(contact));
		}
		else{
			this.parentHandle = parentHandle;
			((ContactPropertiesMainActivity)context).setParentHandle(parentHandle);
			adapter.setParentHandle(parentHandle);
			setNodes(megaApi.getChildren(megaApi.getNodeByHandle(parentHandle), orderGetChildren));
		}
	}

	public void setNodes(ArrayList<MegaNode> nodes){
		this.contactNodes = nodes;
		if (adapter != null){
			adapter.setNodes(contactNodes);
			if (adapter.getCount() == 0){
				listView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				if (megaApi.getRootNode().getHandle()==parentHandle) {
					emptyImageView.setImageResource(R.drawable.ic_empty_cloud_drive);
					emptyTextView.setText(R.string.file_browser_empty_cloud_drive);
				} else {
					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
					emptyTextView.setText(R.string.file_browser_empty_folder);
				}
			}
			else{
				listView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
			}
		}
	}

	public void setUserEmail(String userEmail){
		this.userEmail = userEmail;
	}

	public String getUserEmail(){
		return this.userEmail;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		aB = ((ActionBarActivity)activity).getSupportActionBar();
		if (aB != null){
			aB.show();
		}
	}

	public String getDescription(ArrayList<MegaNode> nodes) {
		int numFolders = 0;
		int numFiles = 0;

		for (int i = 0; i < nodes.size(); i++) {
			MegaNode c = nodes.get(i);
			if (c.isFolder()) {
				numFolders++;
			} else {
				numFiles++;
			}
		}

		String info = "";
		if (numFolders > 0) {
			info = numFolders
					+ " "
					+ getResources().getQuantityString(
							R.plurals.general_num_folders, numFolders);
			if (numFiles > 0) {
				info = info
						+ ", "
						+ numFiles
						+ " "
						+ getResources().getQuantityString(
								R.plurals.general_num_files, numFiles);
			}
		} else {
			if (numFiles == 0) {
				info = numFiles
						+ " "
						+ getResources().getQuantityString(
								R.plurals.general_num_folders, numFolders);
			} else {
				info = numFiles
						+ " "
						+ getResources().getQuantityString(
								R.plurals.general_num_files, numFiles);
			}
		}

		return info;
	}

	public void setParentHandle(long parentHandle) {
		this.parentHandle = parentHandle;
		if (adapter != null){
			adapter.setParentHandle(parentHandle);
		}
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		if (request.getType() == MegaRequest.TYPE_MOVE) {
			log("move request start");
		} else if (request.getType() == MegaRequest.TYPE_REMOVE) {
			log("remove request start");
		} else if (request.getType() == MegaRequest.TYPE_EXPORT) {
			log("export request start");
		} else if (request.getType() == MegaRequest.TYPE_RENAME) {
			log("rename request start");
		} else if (request.getType() == MegaRequest.TYPE_COPY) {
			log("copy request start");
		}

	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestFinish: "+request.getType());
		if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER) {
			if (e.getErrorCode() == MegaError.API_OK) {
				File avatar = null;				
				if (context.getExternalCacheDir() != null) {
					avatar = new File(context.getExternalCacheDir().getAbsolutePath(),
							request.getEmail() + ".jpg");
				} else {
					avatar = new File(context.getCacheDir().getAbsolutePath(),
							request.getEmail() + ".jpg");
				}
				Bitmap imBitmap = null;
				if (avatar.exists()) {
					if (avatar.length() > 0) {
						BitmapFactory.Options bOpts = new BitmapFactory.Options();
						bOpts.inPurgeable = true;
						bOpts.inInputShareable = true;
						imBitmap = BitmapFactory.decodeFile(
								avatar.getAbsolutePath(), bOpts);
						if (imBitmap == null) {
							avatar.delete();
						} else {
							imageView.setImageBitmap(imBitmap);
							contactInitialLetter.setVisibility(View.GONE);
						}
					}
				}
				if(request.getParamType()==1){
					log("(1)request.getText(): "+request.getText());
					nameText=request.getText();
					name=true;
				}
				else if(request.getParamType()==2){
					log("(2)request.getText(): "+request.getText());
					firstNameText = request.getText();
					firstName = true;
				}
				if(name&&firstName){
					nameView.setText(nameText+" "+firstNameText);
					name= false;
					firstName = false;
				}				
			}
		} 
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError");
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		log("onRequestUpdate");
	}

	public static void log(String log) {
		Util.log("ContactFileListFragment", log);
	}

	@Override
	public void onDestroy(){
		if(megaApi != null)
		{	
			megaApi.removeRequestListener(this);
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (adapter.isMultipleSelect()) {
			SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
			if (checkedItems.get(position, false) == true) {
				listView.setItemChecked(position, true);
			} else {
				listView.setItemChecked(position, false);
			}
			updateActionModeTitle();
			adapter.notifyDataSetChanged();
		} else {
			if (contactNodes.get(position).isFolder()) {
				MegaNode n = contactNodes.get(position);

				if (aB != null){
					aB.setTitle(n.getName());
					aB.setLogo(R.drawable.ic_action_navigation_previous_item);
				}
				((ContactPropertiesMainActivity)context).supportInvalidateOptionsMenu();

				parentHandleStack.push(parentHandle);
				parentHandle = contactNodes.get(position).getHandle();
				adapter.setParentHandle(parentHandle);
				((ContactPropertiesMainActivity)context).setParentHandle(parentHandle);

				contactNodes = megaApi.getChildren(contactNodes.get(position));
				adapter.setNodes(contactNodes);
				listView.setSelection(0);

				// If folder has no files
				if (adapter.getCount() == 0) {
					listView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);
					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
					emptyTextView.setText(R.string.file_browser_empty_folder);
				} else {
					listView.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);
				}
			} 
			else {
				if (MimeTypeList.typeForName(contactNodes.get(position).getName()).isImage()) {
					Intent intent = new Intent(context, FullScreenImageViewer.class);
					intent.putExtra("position", position);
					if (megaApi.getParentNode(contactNodes.get(position)).getType() == MegaNode.TYPE_ROOT) {
						intent.putExtra("parentNodeHandle", -1L);
					} else {
						intent.putExtra("parentNodeHandle", megaApi.getParentNode(contactNodes.get(position)).getHandle());
					}
					((ContactPropertiesMainActivity)context).startActivity(intent);
				} 
				else if (MimeTypeList.typeForName(contactNodes.get(position).getName()).isVideo()	|| MimeTypeList.typeForName(contactNodes.get(position).getName()).isAudio()) {
					MegaNode file = contactNodes.get(position);
					Intent service = new Intent(context, MegaStreamingService.class);
					((ContactPropertiesMainActivity)context).startService(service);
					String fileName = file.getName();
					try {
						fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					String url = "http://127.0.0.1:4443/" + file.getBase64Handle() + "/" + fileName;
					String mimeType = MimeTypeList.typeForName(file.getName()).getType();
					System.out.println("FILENAME: " + fileName);

					Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
					mediaIntent.setDataAndType(Uri.parse(url), mimeType);
					if (ManagerActivity.isIntentAvailable(context, mediaIntent)){
			  			startActivity(mediaIntent);
			  		}
			  		else{
			  			Toast.makeText(context, context.getResources().getString(R.string.intent_not_available), Toast.LENGTH_LONG).show();
			  			adapter.setPositionClicked(-1);
						adapter.notifyDataSetChanged();
						ArrayList<Long> handleList = new ArrayList<Long>();
						handleList.add(contactNodes.get(position).getHandle());
						((ContactPropertiesMainActivity)context).onFileClick(handleList);
			  		}
				} else {
					adapter.setPositionClicked(-1);
					adapter.notifyDataSetChanged();
					ArrayList<Long> handleList = new ArrayList<Long>();
					handleList.add(contactNodes.get(position).getHandle());
					((ContactPropertiesMainActivity)context).onFileClick(handleList);
				}
			}
		}
	}

	public int onBackPressed() {

		parentHandle = adapter.getParentHandle();
		((ContactPropertiesMainActivity)context).setParentHandle(parentHandle);

		if (adapter.getPositionClicked() != -1) {
			adapter.setPositionClicked(-1);
			adapter.notifyDataSetChanged();
			return 1;
		} else {
			if (parentHandleStack.isEmpty()) {
				return 0;
			} else {
				parentHandle = parentHandleStack.pop();
				listView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
				if (parentHandle == -1) {
					contactNodes = megaApi.getInShares(contact);
					if (aB != null){
						aB.setTitle(R.string.contact_shared_files);
						aB.setLogo(R.drawable.ic_action_navigation_accept_white);
					}
					((ContactPropertiesMainActivity)context).supportInvalidateOptionsMenu();
					adapter.setNodes(contactNodes);
					listView.setSelection(0);
					((ContactPropertiesMainActivity)context).setParentHandle(parentHandle);
					adapter.setParentHandle(parentHandle);
					return 2;
				} else {
					contactNodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandle));
					if (aB != null){
						aB.setTitle(megaApi.getNodeByHandle(parentHandle).getName());
						aB.setLogo(R.drawable.ic_action_navigation_previous_item);
					}
					((ContactPropertiesMainActivity)context).supportInvalidateOptionsMenu();
					adapter.setNodes(contactNodes);
					listView.setSelection(0);
					((ContactPropertiesMainActivity)context).setParentHandle(parentHandle);
					adapter.setParentHandle(parentHandle);
					return 3;
				}
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		if (adapter.getPositionClicked() == -1) {
			clearSelections();
			actionMode = ((ActionBarActivity)context).startSupportActionMode(new ActionBarCallBack());
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
		List<MegaNode> documents = getSelectedDocuments();
		int files = 0;
		int folders = 0;
		for (MegaNode document : documents) {
			if (document.isFile()) {
				files++;
			} else if (document.isFolder()) {
				folders++;
			}
		}
		Resources res = getResources();
		String format = "%d %s";
		String filesStr = String.format(format, files,
				res.getQuantityString(R.plurals.general_num_files, files));
		String foldersStr = String.format(format, folders,
				res.getQuantityString(R.plurals.general_num_folders, folders));
		String title;
		if (files == 0 && folders == 0) {
			title = "";
		} else if (files == 0) {
			title = foldersStr;
		} else if (folders == 0) {
			title = filesStr;
		} else {
			title = foldersStr + ", " + filesStr;
		}
		actionMode.setTitle(title);
		try {
			actionMode.invalidate();
		} catch (NullPointerException e) {
			e.printStackTrace();
			log("oninvalidate error");
		}
		// actionMode.
	}

	/*
	 * Get list of all selected documents
	 */
	private List<MegaNode> getSelectedDocuments() {
		ArrayList<MegaNode> documents = new ArrayList<MegaNode>();
		SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
		for (int i = 0; i < checkedItems.size(); i++) {
			if (checkedItems.valueAt(i) == true) {
				MegaNode document = adapter
						.getDocumentAt(checkedItems.keyAt(i));
				if (document != null) {
					documents.add(document);
				}
			}
		}
		return documents;
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
		//		switch (v.getId()) {
		//		case R.id.contact_file_list_contact_layout: {
		//			Intent i = new Intent(this, ContactPropertiesMainActivity.class);
		//			i.putExtra("name", contact.getEmail());
		//			startActivity(i);
		//			finish();
		//			break;
		//		}
		//		}
	}

	public void setTransfers(HashMap<Long, MegaTransfer> _mTHash){
		this.mTHash = _mTHash;

		if (adapter != null){
			adapter.setTransfers(mTHash);
		}
	}

	public void setCurrentTransfer(MegaTransfer mT){
		if (adapter != null){
			adapter.setCurrentTransfer(mT);
		}
	}
}
