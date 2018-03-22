package mega.privacy.android.app.lollipop;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.lollipop.adapters.MegaBrowserLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.lollipop.listeners.FabButtonListener;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaUser;


public class ContactFileListFragmentLollipop extends Fragment{

	MegaApiAndroid megaApi;
	ActionBar aB;
	Context context;
	Object contactFileListFragment = this;

	String userEmail;

	CoordinatorLayout mainLayout;

	RecyclerView listView;
	LinearLayoutManager mLayoutManager;
	ImageView emptyImageView;
	TextView emptyTextView;

	MegaUser contact;
	ArrayList<MegaNode> contactNodes;

	MegaBrowserLollipopAdapter adapter;

	FloatingActionButton fab;

	long parentHandle = -1;

	Stack<Long> parentHandleStack = new Stack<Long>();
	Stack<Integer> lastPositionStack;

	private ActionMode actionMode;

	ProgressDialog statusDialog;

	public static int REQUEST_CODE_GET = 1000;
	public static int REQUEST_CODE_GET_LOCAL = 1003;
	public static int REQUEST_CODE_SELECT_COPY_FOLDER = 1002;
	public static int REQUEST_CODE_SELECT_LOCAL_FOLDER = 1004;
	public static int REQUEST_CODE_SELECT_MOVE_FOLDER = 1001;

	private int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;

	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;
	String downloadLocationDefaultPath = Util.downloadDIR;

	public void activateActionMode(){
		log("activateActionMode");
		if (!adapter.isMultipleSelect()){
			adapter.setMultipleSelect(true);
			actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
		}
	}

	private class ActionBarCallBack implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			List<MegaNode> documents = adapter.getSelectedNodes();

			switch (item.getItemId()) {
				case R.id.cab_menu_download: {
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i = 0; i < documents.size(); i++) {
						handleList.add(documents.get(i).getHandle());
					}

					((ContactFileListActivityLollipop)context).onFileClick(handleList);
					break;
				}
				case R.id.cab_menu_copy: {
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i = 0; i < documents.size(); i++) {
						handleList.add(documents.get(i).getHandle());
					}

					((ContactFileListActivityLollipop)context).showCopyLollipop(handleList);
					break;
				}
				case R.id.cab_menu_select_all:{
					selectAll();
					break;
				}
				case R.id.cab_menu_unselect_all:{
					clearSelections();
					break;
				}
				case R.id.cab_menu_leave_multiple_share: {
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					((ContactFileListActivityLollipop) context).showConfirmationLeaveIncomingShare(handleList);
                    break;
				}
                case R.id.cab_menu_trash: {
                    ArrayList<Long> handleList = new ArrayList<Long>();
                    for (int i=0;i<documents.size();i++){
                        handleList.add(documents.get(i).getHandle());
                    }
                    ((ContactFileListActivityLollipop)(context)).askConfirmationMoveToRubbish(handleList);
                    break;
                }
				case R.id.cab_menu_rename: {
					MegaNode aux = documents.get(0);
					((ContactFileListActivityLollipop) context).showRenameDialog(aux, aux.getName());
					break;
				}
			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.file_browser_action, menu);
			fab.setVisibility(View.GONE);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			log("onDestroyActionMode");
			clearSelections();
			adapter.setMultipleSelect(false);
			fab.setVisibility(View.VISIBLE);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			List<MegaNode> selected = adapter.getSelectedNodes();
			boolean showRename = false;
			boolean showMove = false;
			boolean showTrash = false;
			
			// Rename
			if(selected.size() == 1){
				if ((megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK) || (megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK)) {
					showRename = true;
				}
			}

			if (selected.size() > 0) {
				if ((megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK) || (megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK)) {
					showMove = true;	
				}
			}
			
			if (selected.size() != 0) {
				showMove = false;
				// Rename
				if(selected.size() == 1) {
					
					if((megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_FULL).getErrorCode() == MegaError.API_OK)){
						showMove = true;
						showRename = true;
					}
					else if(megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_READWRITE).getErrorCode() == MegaError.API_OK){
						showMove = false;
						showRename = false;
					}		
				}
				else{
					showRename = false;
					showMove = false;
				}
				
				for(int i=0; i<selected.size();i++)	{
					if(megaApi.checkMove(selected.get(i), megaApi.getRubbishNode()).getErrorCode() != MegaError.API_OK)	{
						showMove = false;
						break;
					}
				}

				if(!((ContactFileListActivityLollipop)context).isEmptyParentHandleStack()){
					showTrash = true;
				}
				for(int i=0; i<selected.size(); i++){
					if((megaApi.checkAccess(selected.get(i), MegaShare.ACCESS_FULL).getErrorCode() != MegaError.API_OK)){
						showTrash = false;
						break;
					}
				}
				
				if(selected.size()==adapter.getItemCount()){
					menu.findItem(R.id.cab_menu_select_all).setVisible(false);
					menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);			
				}
				else{
					menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					menu.findItem(R.id.cab_menu_unselect_all).setVisible(true);	
				}	
			}
			else{
				menu.findItem(R.id.cab_menu_select_all).setVisible(true);
				menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
			}
			
			menu.findItem(R.id.cab_menu_download).setVisible(true);
			menu.findItem(R.id.cab_menu_download).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.findItem(R.id.cab_menu_leave_multiple_share).setVisible(true);
			menu.findItem(R.id.cab_menu_leave_multiple_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.findItem(R.id.cab_menu_rename).setVisible(showRename);
			menu.findItem(R.id.cab_menu_copy).setVisible(true);

			menu.findItem(R.id.cab_menu_move).setVisible(showMove);
			menu.findItem(R.id.cab_menu_share_link).setVisible(false);
			menu.findItem(R.id.cab_menu_trash).setVisible(showTrash);

			return false;
		}

	}

	@Override
	public void onCreate (Bundle savedInstanceState){
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		dbH = DatabaseHandler.getDbHandler(context);
		prefs = dbH.getPreferences();
		if (prefs != null){
			log("prefs != null");
			if (prefs.getStorageAskAlways() != null){
				if (!Boolean.parseBoolean(prefs.getStorageAskAlways())){
					log("askMe==false");
					if (prefs.getStorageDownloadLocation() != null){
						if (prefs.getStorageDownloadLocation().compareTo("") != 0){
							downloadLocationDefaultPath = prefs.getStorageDownloadLocation();
						}
					}
				}
			}
		}

		lastPositionStack = new Stack<>();
		super.onCreate(savedInstanceState);
		log("onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log("onCreateView");
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		
		if (aB == null){
			aB = ((AppCompatActivity)context).getSupportActionBar();
		}

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);

		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		View v = null;

		if (userEmail != null){
			v = inflater.inflate(R.layout.fragment_contact_file_list, container, false);

			mainLayout = (CoordinatorLayout) v.findViewById(R.id.contact_file_list_coordinator_layout);

			fab = (FloatingActionButton) v.findViewById(R.id.floating_button_contact_file_list);
			fab.setOnClickListener(new FabButtonListener(context));
			fab.setVisibility(View.GONE);

			contact = megaApi.getContact(userEmail);
			if(contact == null)
			{
				return null;
			}

			contactNodes = megaApi.getInShares(contact);
			
			listView = (RecyclerView) v.findViewById(R.id.contact_file_list_view_browser);
			listView.addItemDecoration(new SimpleDividerItemDecoration(context, outMetrics));
			mLayoutManager = new LinearLayoutManager(context);
			listView.setLayoutManager(mLayoutManager);
			listView.setItemAnimator(new DefaultItemAnimator());

			Resources res = getResources();
			int valuePaddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, res.getDisplayMetrics());
			int valuePaddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 88, res.getDisplayMetrics());

			listView.setClipToPadding(false);
			listView.setPadding(0, valuePaddingTop, 0, valuePaddingBottom);

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
//				emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//				emptyTextView.setText(R.string.file_browser_empty_folder);

				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
				}else{
					emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
				}
				String textToShow = String.format(context.getString(R.string.context_empty_contacts), getString(R.string.context_empty_incoming));
				try{
					textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
					textToShow = textToShow.replace("[/A]", "</font>");
					textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
					textToShow = textToShow.replace("[/B]", "</font>");
				}
				catch (Exception e){}
				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}
				emptyTextView.setText(result);
			}

			if (adapter == null) {
				adapter = new MegaBrowserLollipopAdapter(context, this, contactNodes, -1,listView, aB,Constants.CONTACT_FILE_ADAPTER, MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST);

			} else {
				adapter.setNodes(contactNodes);
				adapter.setParentHandle(-1);
			}

			adapter.setMultipleSelect(false);

			listView.setAdapter(adapter);
		}

		return v;
	}
	
	public void showOptionsPanel(MegaNode sNode){
		log("showOptionsPanel");
		((ContactFileListActivityLollipop)context).showOptionsPanel(sNode);
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
			((ContactFileListActivityLollipop)context).setParentHandle(parentHandle);
			adapter.setParentHandle(parentHandle);

			setNodes(megaApi.getInShares(contact));
		}
		else{
			this.parentHandle = parentHandle;
			((ContactFileListActivityLollipop)context).setParentHandle(parentHandle);
			adapter.setParentHandle(parentHandle);
			setNodes(megaApi.getChildren(megaApi.getNodeByHandle(parentHandle), orderGetChildren));
		}
	}

	public void setNodes(ArrayList<MegaNode> nodes){
		this.contactNodes = nodes;
		if (adapter != null){
			adapter.setNodes(contactNodes);
			if (adapter.getItemCount() == 0){
				listView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				if (megaApi.getRootNode().getHandle()==parentHandle) {
					emptyImageView.setImageResource(R.drawable.ic_empty_cloud_drive);
					emptyTextView.setText(R.string.file_browser_empty_cloud_drive);
				} else {
//					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//					emptyTextView.setText(R.string.file_browser_empty_folder);

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
					}
					String textToShow = String.format(context.getString(R.string.context_empty_contacts), getString(R.string.context_empty_incoming));
					try{
						textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
						textToShow = textToShow.replace("[/A]", "</font>");
						textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
						textToShow = textToShow.replace("[/B]", "</font>");
					}
					catch (Exception e){}
					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					emptyTextView.setText(result);
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
		this.context = activity;
		aB = ((AppCompatActivity)activity).getSupportActionBar();
		if (aB != null){
			aB.show();
			((AppCompatActivity) context).invalidateOptionsMenu();
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		this.context = context;
		aB = ((AppCompatActivity)context).getSupportActionBar();
		if (aB != null){
			aB.show();
			((AppCompatActivity) context).invalidateOptionsMenu();
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

	public static void log(String log) {
		Util.log("ContactFileListFragmentLollipop", log);
	}

	@Override
	public void onDestroy(){

		super.onDestroy();
	}
	
	public void itemClick(int position) {
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		if (adapter.isMultipleSelect()){
			log("multiselect ON");
			adapter.toggleSelection(position);

			List<MegaNode> selectedNodes = adapter.getSelectedNodes();
			if (selectedNodes.size() > 0){
				updateActionModeTitle();
			}
		}
		else{
			if (contactNodes.get(position).isFolder()) {
				MegaNode n = contactNodes.get(position);

				int lastFirstVisiblePosition = 0;

				lastFirstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();

				log("Push to stack "+lastFirstVisiblePosition+" position");
				lastPositionStack.push(lastFirstVisiblePosition);

				((ContactFileListActivityLollipop)context).setTitleActionBar(n.getName());
				((ContactFileListActivityLollipop)context).supportInvalidateOptionsMenu();

				parentHandleStack.push(parentHandle);
				parentHandle = contactNodes.get(position).getHandle();
				adapter.setParentHandle(parentHandle);
				((ContactFileListActivityLollipop)context).setParentHandle(parentHandle);

				contactNodes = megaApi.getChildren(contactNodes.get(position));
				adapter.setNodes(contactNodes);
				listView.scrollToPosition(0);
				
				// If folder has no files
				if (adapter.getItemCount() == 0) {
					listView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);
					//******
//					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
//					emptyTextView.setText(R.string.file_browser_empty_folder);

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.incoming_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.incoming_shares_empty);
					}
					String textToShow = String.format(context.getString(R.string.context_empty_contacts), getString(R.string.context_empty_incoming));
					try{
						textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
						textToShow = textToShow.replace("[/A]", "</font>");
						textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
						textToShow = textToShow.replace("[/B]", "</font>");
					}
					catch (Exception e){}
					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}
					emptyTextView.setText(result);


				} else {
					listView.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);
				}
				showFabButton(n);
			} 
			else {
				if (MimeTypeList.typeForName(contactNodes.get(position).getName()).isImage()) {
					Intent intent = new Intent(context, FullScreenImageViewerLollipop.class);
					intent.putExtra("position", position);
					intent.putExtra("adapterType", Constants.CONTACT_FILE_ADAPTER);
					if (megaApi.getParentNode(contactNodes.get(position)).getType() == MegaNode.TYPE_ROOT) {
						intent.putExtra("parentNodeHandle", -1L);
					} else {
						intent.putExtra("parentNodeHandle", megaApi.getParentNode(contactNodes.get(position)).getHandle());
					}
					((ContactFileListActivityLollipop)context).startActivity(intent);
				} 
				else if (MimeTypeList.typeForName(contactNodes.get(position).getName()).isVideoReproducible()	|| MimeTypeList.typeForName(contactNodes.get(position).getName()).isAudio()) {
					MegaNode file = contactNodes.get(position);
					if (megaApi.httpServerIsRunning() == 0) {
						megaApi.httpServerStart();
					}

					ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
					ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
					activityManager.getMemoryInfo(mi);

					if(mi.totalMem>Constants.BUFFER_COMP){
						log("Total mem: "+mi.totalMem+" allocate 32 MB");
						megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
					}
					else{
						log("Total mem: "+mi.totalMem+" allocate 16 MB");
						megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
					}

					String url = megaApi.httpServerGetLocalLink(file);
					String mimeType = MimeTypeList.typeForName(file.getName()).getType();
					log("FILENAME: " + file.getName());

					//Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
					Intent mediaIntent;
					if (MimeTypeList.typeForName(file.getName()).isVideoNotSupported()){
						mediaIntent = new Intent(Intent.ACTION_VIEW);
					}
					else {
						mediaIntent = new Intent(context, AudioVideoPlayerLollipop.class);
					}
					mediaIntent.putExtra("HANDLE", file.getHandle());
					mediaIntent.putExtra("FILENAME", file.getName());
					String localPath = Util.getLocalFile(context, file.getName(), file.getSize(), downloadLocationDefaultPath);
					if (localPath != null){
						File mediaFile = new File(localPath);
						//mediaIntent.setDataAndType(Uri.parse(localPath), mimeType);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && prefs.getStorageDownloadLocation().contains(Environment.getExternalStorageDirectory().getPath())) {
							mediaIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						else{
							mediaIntent.setDataAndType(Uri.fromFile(mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						mediaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					}
					else {
						mediaIntent.setDataAndType(Uri.parse(url), mimeType);
					}
					if (MegaApiUtils.isIntentAvailable(context, mediaIntent)){
			  			startActivity(mediaIntent);
			  		}
			  		else{
						((ContactFileListActivityLollipop) context).showSnackbar(context.getResources().getString(R.string.intent_not_available));
						adapter.notifyDataSetChanged();
						ArrayList<Long> handleList = new ArrayList<Long>();
						handleList.add(contactNodes.get(position).getHandle());
						((ContactFileListActivityLollipop)context).onFileClick(handleList);
			  		}
				}else if (MimeTypeList.typeForName(contactNodes.get(position).getName()).isPdf()){
					MegaNode file = contactNodes.get(position);

					if (megaApi.httpServerIsRunning() == 0) {
						megaApi.httpServerStart();
					}

					ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
					ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
					activityManager.getMemoryInfo(mi);

					if(mi.totalMem>Constants.BUFFER_COMP){
						log("Total mem: "+mi.totalMem+" allocate 32 MB");
						megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
					}
					else{
						log("Total mem: "+mi.totalMem+" allocate 16 MB");
						megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
					}

					String url = megaApi.httpServerGetLocalLink(file);
					String mimeType = MimeTypeList.typeForName(file.getName()).getType();
					log("FILENAME: " + file.getName() + "TYPE: "+mimeType);

					Intent pdfIntent = new Intent(context, PdfViewerActivityLollipop.class);
					pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					pdfIntent.putExtra("APP", true);
					String localPath = Util.getLocalFile(context, file.getName(), file.getSize(), downloadLocationDefaultPath);
					if (localPath != null){
						File mediaFile = new File(localPath);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && prefs.getStorageDownloadLocation().contains(Environment.getExternalStorageDirectory().getPath())) {
							pdfIntent.setDataAndType(FileProvider.getUriForFile(context, "mega.privacy.android.app.providers.fileprovider", mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						else{
							pdfIntent.setDataAndType(Uri.fromFile(mediaFile), MimeTypeList.typeForName(file.getName()).getType());
						}
						pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					}
					else {
						pdfIntent.setDataAndType(Uri.parse(url), mimeType);
					}
					pdfIntent.putExtra("HANDLE", file.getHandle());
					if (MegaApiUtils.isIntentAvailable(context, pdfIntent)){
						startActivity(pdfIntent);
					}
					else{
						Toast.makeText(context, context.getResources().getString(R.string.intent_not_available), Toast.LENGTH_LONG).show();

						ArrayList<Long> handleList = new ArrayList<Long>();
						handleList.add(contactNodes.get(position).getHandle());
						NodeController nC = new NodeController(context);
						nC.prepareForDownload(handleList);
					}
				}
				else {
					adapter.notifyDataSetChanged();
					ArrayList<Long> handleList = new ArrayList<Long>();
					handleList.add(contactNodes.get(position).getHandle());
					((ContactFileListActivityLollipop)context).onFileClick(handleList);
				}
			}
		}
	}

	public int onBackPressed() {
		log("onBackPressed");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		parentHandle = adapter.getParentHandle();
		((ContactFileListActivityLollipop)context).setParentHandle(parentHandle);

		if (parentHandleStack.isEmpty()) {
			log("return 0");
			fab.setVisibility(View.GONE);
			return 0;
		} else {
			parentHandle = parentHandleStack.pop();
			listView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
			if (parentHandle == -1) {
				fab.setVisibility(View.GONE);
				contactNodes = megaApi.getInShares(contact);
				((ContactFileListActivityLollipop)context).setTitleActionBar(null);
				((ContactFileListActivityLollipop)context).supportInvalidateOptionsMenu();
				adapter.setNodes(contactNodes);
				int lastVisiblePosition = 0;
				if(!lastPositionStack.empty()){
					lastVisiblePosition = lastPositionStack.pop();
					log("Pop of the stack "+lastVisiblePosition+" position");
				}
				log("Scroll to "+lastVisiblePosition+" position");

				if(lastVisiblePosition>=0){
					mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
				}
				((ContactFileListActivityLollipop)context).setParentHandle(parentHandle);
				adapter.setParentHandle(parentHandle);
				log("return 2");
				return 2;
			} else {
				contactNodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandle));
				((ContactFileListActivityLollipop)context).setTitleActionBar(megaApi.getNodeByHandle(parentHandle).getName());
				((ContactFileListActivityLollipop)context).supportInvalidateOptionsMenu();
				adapter.setNodes(contactNodes);
				int lastVisiblePosition = 0;
				if(!lastPositionStack.empty()){
					lastVisiblePosition = lastPositionStack.pop();
					log("Pop of the stack "+lastVisiblePosition+" position");
				}
				log("Scroll to "+lastVisiblePosition+" position");

				if(lastVisiblePosition>=0){
					mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
				}
				((ContactFileListActivityLollipop)context).setParentHandle(parentHandle);
				adapter.setParentHandle(parentHandle);
				showFabButton(megaApi.getNodeByHandle(parentHandle));
				log("return 3");
				return 3;
			}
		}
	}

	public void setNodes(){
		contactNodes = megaApi.getChildren(megaApi.getNodeByHandle(parentHandle));
		adapter.setNodes(contactNodes);
		listView.invalidate();
	}

	public void selectAll(){
		if (adapter != null){
			if(adapter.isMultipleSelect()){
				adapter.selectAll();
			}
			else{
				adapter.setMultipleSelect(true);
				adapter.selectAll();
				
				actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
			}
			
			updateActionModeTitle();
		}
	}
	
	/*
	 * Clear all selected items
	 */
	public void clearSelections() {
		if(adapter.isMultipleSelect()){
			adapter.clearSelections();
		}
	}

	private void updateActionModeTitle() {
		if (actionMode == null) {
			return;
		}
		List<MegaNode> documents = adapter.getSelectedNodes();
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
		String title;
		int sum=files+folders;

		if (files == 0 && folders == 0) {
			title = Integer.toString(sum);
		} else if (files == 0) {
			title = Integer.toString(folders);
		} else if (folders == 0) {
			title = Integer.toString(files);
		} else {
			title = Integer.toString(sum);
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
	 * Disable selection
	 */
	public void hideMultipleSelect() {
		log("hideMultipleSelect");
		adapter.setMultipleSelect(false);
		if (actionMode != null) {
			actionMode.finish();
		}
	}

	public void notifyDataSetChanged(){		
		if (adapter != null){
			adapter.notifyDataSetChanged();
		}		
	}

	public void showFabButton(MegaNode node){
		log("showFabButton");
		int accessLevel = megaApi.getAccess(node);

		if(accessLevel== MegaShare.ACCESS_READ){
			fab.setVisibility(View.GONE);
		}
		else{
			fab.setVisibility(View.VISIBLE);
		}
		((ContactFileListActivityLollipop) context).invalidateOptionsMenu();
	}

	public int getFabVisibility(){
		return fab.getVisibility();
	}

	public long getParentHandle() {
		return parentHandle;
	}

	public boolean isEmptyParentHandleStack() {
		return parentHandleStack.isEmpty();
	}

}
