package mega.privacy.android.app.lollipop.managerSections;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.CustomizedGridLayoutManager;
import mega.privacy.android.app.components.CustomizedGridRecyclerView;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop;
import mega.privacy.android.app.lollipop.FullScreenImageViewerLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop.DrawerItem;
import mega.privacy.android.app.lollipop.MyAccountInfo;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaBrowserLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;


public class InboxFragmentLollipop extends Fragment{

	public static int GRID_WIDTH =400;
	
	Context context;
	ActionBar aB;
	RecyclerView recyclerView;
	LinearLayoutManager mLayoutManager;
	CustomizedGridLayoutManager gridLayoutManager;
	MegaBrowserLollipopAdapter adapter;
	public InboxFragmentLollipop inboxFragment = this;
	MegaNode inboxNode;

	ArrayList<MegaNode> nodes;
	MegaNode selectedNode;
	
	ImageView emptyImageView;
	LinearLayout emptyTextView;
	TextView emptyTextViewFirst;
	TextView emptyTextViewSecond;

	TextView contentText;
	RelativeLayout contentTextLayout;
	Stack<Integer> lastPositionStack;
	
	MegaApiAndroid megaApi;

	String downloadLocationDefaultPath = Util.downloadDIR;
	
	private ActionMode actionMode;
	
	float density;
	DisplayMetrics outMetrics;
	Display display;

	DatabaseHandler dbH;
	MegaPreferences prefs;

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
			((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

			List<MegaNode> documents = adapter.getSelectedNodes();
			
			switch(item.getItemId()){
				case R.id.cab_menu_download:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.prepareForDownload(handleList);

					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_rename:{

					if (documents.size()==1){
						((ManagerActivityLollipop) context).showRenameDialog(documents.get(0), documents.get(0).getName());
					}

					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_copy:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.chooseLocationToCopyNodes(handleList);

					clearSelections();
					hideMultipleSelect();
					break;
				}	
				case R.id.cab_menu_move:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					NodeController nC = new NodeController(context);
					nC.chooseLocationToMoveNodes(handleList);

					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_trash:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					((ManagerActivityLollipop) context).askConfirmationMoveToRubbish(handleList);

					clearSelections();
					hideMultipleSelect();
					break;
				}
				case R.id.cab_menu_select_all:{
					((ManagerActivityLollipop)context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
					selectAll();
					break;
				}
				case R.id.cab_menu_unselect_all:{
					clearSelections();
					hideMultipleSelect();
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
			log("onDestroyActionMode");
			clearSelections();
			adapter.setMultipleSelect(false);
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			List<MegaNode> selected = adapter.getSelectedNodes();
			boolean showDownload = false;
			boolean showRename = false;
			boolean showCopy = false;
			boolean showMove = false;
			boolean showLink = false;
			boolean showTrash = false;

			MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);

			if (selected.size() != 0) {

				if(selected.size()==adapter.getItemCount()){
					menu.findItem(R.id.cab_menu_select_all).setVisible(false);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
				}
				else if(selected.size()==1){
					menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
				}
				else{
					menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
				}

				if(selected.size()==1){
					showRename = true;
				}
				else{
					showRename = false;
				}

				showDownload = true;
				showTrash = true;
				showMove = true;
				showCopy = true;
				for(int i=0; i<selected.size();i++)	{
					if(megaApi.checkMove(selected.get(i), megaApi.getInboxNode()).getErrorCode() != MegaError.API_OK)	{
						showTrash = false;
						showMove = false;
						break;
					}
				}
			}
			else{
				menu.findItem(R.id.cab_menu_select_all).setVisible(true);
				menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
			}

			menu.findItem(R.id.cab_menu_download).setVisible(showDownload);
			if(showDownload){
				menu.findItem(R.id.cab_menu_download).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			menu.findItem(R.id.cab_menu_rename).setVisible(showRename);

			menu.findItem(R.id.cab_menu_copy).setVisible(showCopy);
			if(showCopy){
				menu.findItem(R.id.cab_menu_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			menu.findItem(R.id.cab_menu_move).setVisible(showMove);
			if(showMove){
				menu.findItem(R.id.cab_menu_move).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			menu.findItem(R.id.cab_menu_share_link).setVisible(showLink);
			if (showTrash){
				menu.findItem(R.id.cab_menu_trash).setTitle(context.getString(R.string.context_move_to_trash));
			}

			menu.findItem(R.id.cab_menu_trash).setVisible(showTrash);
			menu.findItem(R.id.cab_menu_leave_multiple_share).setVisible(false);
			
			return false;
		}		
	}
	
	public boolean showSelectMenuItem(){
		if (adapter != null){
			return adapter.isMultipleSelect();
		}
		
		return false;
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
	
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		log("onCreate");

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
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log("onCreateView");
		
		if (aB == null){
			aB = ((AppCompatActivity)context).getSupportActionBar();
		}

		display = ((Activity)context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;

		if (((ManagerActivityLollipop) context).parentHandleInbox == -1||((ManagerActivityLollipop) context).parentHandleInbox==megaApi.getInboxNode().getHandle()) {
			log("parentHandle -1");

			if (megaApi.getInboxNode() != null){
				log("InboxNode != null");
				inboxNode = megaApi.getInboxNode();
				nodes = megaApi.getChildren(inboxNode, ((ManagerActivityLollipop)context).orderCloud);
			}
		}
		else{
			log("parentHandle: " + ((ManagerActivityLollipop) context).parentHandleInbox);
			MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop) context).parentHandleInbox);

			if(parentNode!=null){
				log("parentNode: "+parentNode.getName());
				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
			}

		}
		((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();
	    
		if (((ManagerActivityLollipop) context).isList){
			View v = inflater.inflate(R.layout.fragment_inboxlist, container, false);

			recyclerView = (RecyclerView) v.findViewById(R.id.inbox_list_view);
			recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, outMetrics));
			mLayoutManager = new LinearLayoutManager(context);
			recyclerView.setLayoutManager(mLayoutManager);
			recyclerView.setItemAnimator(new DefaultItemAnimator());

			emptyImageView = (ImageView) v.findViewById(R.id.inbox_list_empty_image);
			emptyTextView = (LinearLayout) v.findViewById(R.id.inbox_list_empty_text);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.inbox_list_empty_text_first);
			emptyTextViewSecond = (TextView) v.findViewById(R.id.inbox_list_empty_text_second);

			contentTextLayout = (RelativeLayout) v.findViewById(R.id.inbox_list_content_text_layout);
			contentText = (TextView) v.findViewById(R.id.inbox_list_content_text);

			if (adapter == null){
				adapter = new MegaBrowserLollipopAdapter(context, this, nodes, ((ManagerActivityLollipop) context).parentHandleInbox, recyclerView, aB, Constants.INBOX_ADAPTER, MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST);
			}
			else{
				adapter.setParentHandle(((ManagerActivityLollipop) context).parentHandleInbox);
				adapter.setNodes(nodes);
				adapter.setAdapterType(MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST);
			}	

			adapter.setMultipleSelect(false);

			recyclerView.setAdapter(adapter);

			setNodes(nodes);
			return v;
		}
		else{
			log("isGrid View");
			View v = inflater.inflate(R.layout.fragment_inboxgrid, container, false);
			
			recyclerView = (CustomizedGridRecyclerView) v.findViewById(R.id.inbox_grid_view);
			recyclerView.setHasFixedSize(true);
			gridLayoutManager = (CustomizedGridLayoutManager) recyclerView.getLayoutManager();

			recyclerView.setItemAnimator(new DefaultItemAnimator());

			emptyImageView = (ImageView) v.findViewById(R.id.inbox_grid_empty_image);
			emptyTextView = (LinearLayout) v.findViewById(R.id.inbox_grid_empty_text);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.inbox_grid_empty_text_first);
			emptyTextViewSecond = (TextView) v.findViewById(R.id.inbox_grid_empty_text_second);


//			emptyImageView.setImageResource(R.drawable.inbox_empty);
//			emptyTextView.setText(R.string.empty_inbox);

			contentTextLayout = (RelativeLayout) v.findViewById(R.id.inbox_grid_content_text_layout);
			contentText = (TextView) v.findViewById(R.id.inbox_content_grid_text);			

			if (adapter == null){
				adapter = new MegaBrowserLollipopAdapter(context, this, nodes, ((ManagerActivityLollipop) context).parentHandleInbox, recyclerView, aB, Constants.INBOX_ADAPTER, MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID);
			}
			else{
				adapter.setParentHandle(((ManagerActivityLollipop) context).parentHandleInbox);
				adapter.setNodes(nodes);
				adapter.setAdapterType(MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID);
			}

			recyclerView.setAdapter(adapter);

			setNodes(nodes);

			setContentText();

			return v;	
		}
	}
	
	public void refresh(){
		log("refresh");
		if(((ManagerActivityLollipop) context).parentHandleInbox==-1||((ManagerActivityLollipop) context).parentHandleInbox==inboxNode.getHandle()){
			nodes = megaApi.getChildren(inboxNode, ((ManagerActivityLollipop)context).orderCloud);
		}
		else{
			MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop) context).parentHandleInbox);
			if(parentNode!=null){
				log("parentNode: "+parentNode.getName());
				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
			}
		}

		setNodes(nodes);
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }
	
	public void itemClick(int position) {
		log("itemClick");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		if (adapter.isMultipleSelect()){
			log("multiselect ON");
			adapter.toggleSelection(position);

			List<MegaNode> selectedNodes = adapter.getSelectedNodes();
			if (selectedNodes.size() > 0){
				updateActionModeTitle();
				((ManagerActivityLollipop)context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_RED);
			}
		}
		else{

			if (nodes.get(position).isFolder()){
				MegaNode n = nodes.get(position);

				int lastFirstVisiblePosition = 0;
				if(((ManagerActivityLollipop) context).isList){
					lastFirstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
				}
				else{
					lastFirstVisiblePosition = ((CustomizedGridRecyclerView) recyclerView).findFirstCompletelyVisibleItemPosition();
					if(lastFirstVisiblePosition==-1){
						log("Completely -1 then find just visible position");
						lastFirstVisiblePosition = ((CustomizedGridRecyclerView) recyclerView).findFirstVisibleItemPosition();
					}
				}

				log("Push to stack "+lastFirstVisiblePosition+" position");
				lastPositionStack.push(lastFirstVisiblePosition);

				aB.setTitle(n.getName());
				aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
				((ManagerActivityLollipop)context).setFirstNavigationLevel(false);
				((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

				((ManagerActivityLollipop) context).setParentHandleInbox(nodes.get(position).getHandle());
				nodes = megaApi.getChildren(nodes.get(position), ((ManagerActivityLollipop)context).orderCloud);
				adapter.setNodes(nodes);

				setContentText();

				recyclerView.scrollToPosition(0);
			}
			else{
				if (MimeTypeList.typeForName(nodes.get(position).getName()).isImage()){
					Intent intent = new Intent(context, FullScreenImageViewerLollipop.class);
					intent.putExtra("position", position);
					intent.putExtra("adapterType", Constants.RUBBISH_BIN_ADAPTER);
					if (megaApi.getParentNode(nodes.get(position)).getType() == MegaNode.TYPE_RUBBISH){
						intent.putExtra("parentNodeHandle", -1L);
					}
					else{
						intent.putExtra("parentNodeHandle", megaApi.getParentNode(nodes.get(position)).getHandle());
					}
					MyAccountInfo accountInfo = ((ManagerActivityLollipop)context).getMyAccountInfo();
					if(accountInfo!=null){
						intent.putExtra("typeAccount", accountInfo.getAccountType());
					}
					intent.putExtra("orderGetChildren", ((ManagerActivityLollipop)context).orderCloud);
					startActivity(intent);
				}
				else if (MimeTypeList.typeForName(nodes.get(position).getName()).isVideoReproducible() || MimeTypeList.typeForName(nodes.get(position).getName()).isAudio() ){
					MegaNode file = nodes.get(position);

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

					Intent mediaIntent;
					if (MimeTypeList.typeForName(file.getName()).isVideoNotSupported()){
						mediaIntent = new Intent(Intent.ACTION_VIEW);
					}
					else {
						mediaIntent = new Intent(context, AudioVideoPlayerLollipop.class);
					}
					mediaIntent.putExtra("position", position);
					if (megaApi.getParentNode(nodes.get(position)).getType() == MegaNode.TYPE_RUBBISH){
						mediaIntent.putExtra("parentNodeHandle", -1L);
					}
					else{
						mediaIntent.putExtra("parentNodeHandle", megaApi.getParentNode(nodes.get(position)).getHandle());
					}
					mediaIntent.putExtra("orderGetChildren", ((ManagerActivityLollipop)context).orderCloud);
					mediaIntent.putExtra("adapterType", Constants.RUBBISH_BIN_ADAPTER);
					mediaIntent.putExtra("HANDLE", file.getHandle());
					mediaIntent.putExtra("FILENAME", file.getName());
					String localPath = Util.getLocalFile(context, file.getName(), file.getSize(), downloadLocationDefaultPath);
					if (localPath != null && (megaApi.getFingerprint(file).equals(megaApi.getFingerprint(localPath)))){
						File mediaFile = new File(localPath);
						//mediaIntent.setDataAndType(Uri.parse(localPath), mimeType);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
						Toast.makeText(context, context.getResources().getString(R.string.intent_not_available), Toast.LENGTH_LONG).show();
						adapter.notifyDataSetChanged();
						ArrayList<Long> handleList = new ArrayList<Long>();
						handleList.add(nodes.get(position).getHandle());
						NodeController nC = new NodeController(context);
						nC.prepareForDownload(handleList);
					}
				}else if (MimeTypeList.typeForName(nodes.get(position).getName()).isPdf()){
					MegaNode file = nodes.get(position);

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
					pdfIntent.putExtra("APP", true);
					String localPath = Util.getLocalFile(context, file.getName(), file.getSize(), downloadLocationDefaultPath);
					if (localPath != null && (megaApi.getFingerprint(file).equals(megaApi.getFingerprint(localPath)))){
						File mediaFile = new File(localPath);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
						handleList.add(nodes.get(position).getHandle());
						NodeController nC = new NodeController(context);
						nC.prepareForDownload(handleList);
					}
				}
				else{
					adapter.notifyDataSetChanged();
					ArrayList<Long> handleList = new ArrayList<Long>();
					handleList.add(nodes.get(position).getHandle());
					NodeController nC = new NodeController(context);
					nC.prepareForDownload(handleList);
				}
			}
		}
    }
	
	private void updateActionModeTitle() {
		if (actionMode == null || getActivity() == null) {
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
		Resources res = getActivity().getResources();

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
		/*String format = "%d %s";
		String filesStr = String.format(format, files,
				res.getQuantityString(R.plurals.general_num_files, files));
		String foldersStr = String.format(format, folders,
				res.getQuantityString(R.plurals.general_num_folders, folders));
		String title;
		if (files == 0 && folders == 0) {
			title = foldersStr + ", " + filesStr;
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
		}*/

	}

	/*
	 * Clear all selected items
	 */
	private void clearSelections() {
		if(adapter.isMultipleSelect()){
			adapter.clearSelections();
		}
	}
	
	/*
	 * Disable selection
	 */
	public void hideMultipleSelect() {
		log("hideMultipleSelect");
		adapter.setMultipleSelect(false);
		((ManagerActivityLollipop)context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_TRANSPARENT_BLACK);
		if (actionMode != null) {
			actionMode.finish();
		}
	}

	public static InboxFragmentLollipop newInstance() {
		log("newInstance");
		InboxFragmentLollipop fragment = new InboxFragmentLollipop();
		return fragment;
	}
	
	public int onBackPressed(){
		log("onBackPressed");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		if (adapter == null){
			return 0;
		}

		MegaNode parentNode = megaApi.getParentNode(megaApi.getNodeByHandle(((ManagerActivityLollipop) context).parentHandleInbox));
		if (parentNode != null) {
			log("ParentNode: "+parentNode.getName());

			if (parentNode.getHandle() == megaApi.getInboxNode().getHandle()){
				aB.setTitle(getString(R.string.section_inbox));
				aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
				((ManagerActivityLollipop)context).setFirstNavigationLevel(true);
			}
			else{
				aB.setTitle(parentNode.getName());
				aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
				((ManagerActivityLollipop)context).setFirstNavigationLevel(false);
			}

			((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

			((ManagerActivityLollipop) context).setParentHandleInbox(parentNode.getHandle());
			nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderCloud);
			setNodes(nodes);

			int lastVisiblePosition = 0;
			if(!lastPositionStack.empty()){
				lastVisiblePosition = lastPositionStack.pop();
				log("Pop of the stack "+lastVisiblePosition+" position");
			}
			log("Scroll to "+lastVisiblePosition+" position");

			if(lastVisiblePosition>=0){

				if(((ManagerActivityLollipop) context).isList){
					mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
				}
				else{
					gridLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
				}
			}
			return 2;
		}
		else{
			return 0;
		}
	}

	public boolean getIsList(){
		return ((ManagerActivityLollipop) context).isList;
	}
	
	public long getParentHandle(){
		return ((ManagerActivityLollipop) context).parentHandleInbox;
	}

	public RecyclerView getRecyclerView(){
		return recyclerView;
	}
	
	public void setNodes(ArrayList<MegaNode> nodes){
		log("setNodes");
		this.nodes = nodes;
		if (adapter != null){
			adapter.setNodes(nodes);
			setContentText();
		}	
	}

	public void setContentText(){
		log("setContentText");

		if (adapter.getItemCount() == 0){

			recyclerView.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyTextView.setVisibility(View.VISIBLE);
			contentTextLayout.setVisibility(View.GONE);

			if (megaApi.getInboxNode().getHandle()==((ManagerActivityLollipop)context).parentHandleInbox||((ManagerActivityLollipop)context).parentHandleInbox==-1) {
				if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
					emptyImageView.setImageResource(R.drawable.inbox_empty_landscape);
				}else{
					emptyImageView.setImageResource(R.drawable.inbox_empty);
				}
				emptyTextViewFirst.setText(R.string.context_empty_inbox);
				String text = getString(R.string.section_inbox);
				emptyTextViewSecond.setText(" "+text+".");

			} else {
				emptyImageView.setImageResource(R.drawable.ic_empty_folder);
				emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
			}
		}
		else{
			recyclerView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
			contentTextLayout.setVisibility(View.VISIBLE);

			if (megaApi.getInboxNode().getHandle()==((ManagerActivityLollipop) context).parentHandleInbox||((ManagerActivityLollipop) context).parentHandleInbox==-1) {

				contentText.setText(MegaApiUtils.getInfoFolder(inboxNode, context));
			} else {
				MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop) context).parentHandleInbox);

				if(parentNode!=null){
					contentText.setText(MegaApiUtils.getInfoFolder(parentNode, context));
				}
			}
		}
	}

	public void notifyDataSetChanged(){
		if (adapter != null){
			adapter.notifyDataSetChanged();
		}
	}

	private static void log(String log) {
		Util.log("InboxFragmentLollipop", log);
	}

	public int getItemCount(){
		if(adapter != null){
			return adapter.getItemCount();
		}
		return 0;
	}
}
