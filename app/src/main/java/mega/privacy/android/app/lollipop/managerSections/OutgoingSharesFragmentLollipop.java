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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import mega.privacy.android.app.components.scrollBar.FastScroller;
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop;
import mega.privacy.android.app.lollipop.FullScreenImageViewerLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop.DrawerItem;
import mega.privacy.android.app.lollipop.MyAccountInfo;
import mega.privacy.android.app.lollipop.adapters.MegaBrowserLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.NodeController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;


public class OutgoingSharesFragmentLollipop extends Fragment{

	Context context;
	ActionBar aB;
	RecyclerView recyclerView;
	LinearLayoutManager mLayoutManager;
	CustomizedGridLayoutManager gridLayoutManager;
	FastScroller fastScroller;

	ImageView emptyImageView;
	LinearLayout emptyLinearLayout;
	TextView emptyTextViewFirst;
	TextView emptyTextViewSecond;

	MegaBrowserLollipopAdapter adapter;
	OutgoingSharesFragmentLollipop outgoingSharesFragment = this;
	RelativeLayout transfersOverViewLayout;

	TextView contentText;	
	RelativeLayout contentTextLayout;

	Stack<Integer> lastPositionStack;
	
	float density;
	DisplayMetrics outMetrics;
	Display display;

	MegaApiAndroid megaApi;
		
	ArrayList<MegaNode> nodes;

	public ActionMode actionMode;

	DatabaseHandler dbH;
	MegaPreferences prefs;
	String downloadLocationDefaultPath = Util.downloadDIR;

	public void activateActionMode(){
		log("activateActionMode");
		if (!adapter.isMultipleSelect()){
			adapter.setMultipleSelect(true);
			actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
		}
	}
	
	private class ActionBarCallBack implements ActionMode.Callback {
		
		boolean showRename = false;
		boolean showMove = false;
		boolean showCopy = false;
		boolean showLink = false;
		boolean showRemoveLink = false;
		boolean showTrash = false;
		boolean showShare = false;
		boolean showEditLink = false;


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
				case R.id.cab_menu_share:{
					//Check that all the selected options are folders
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						if(documents.get(i).isFolder()){
							handleList.add(documents.get(i).getHandle());
						}
					}

					NodeController nC = new NodeController(context);
					nC.selectContactToShareFolders(handleList);
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
				case R.id.cab_menu_share_link:{

					if(documents.get(0)==null){
						log("The selected node is NULL");
						break;
					}
					((ManagerActivityLollipop) context).showGetLinkActivity(documents.get(0).getHandle());
					break;
				}
				case R.id.cab_menu_share_link_remove:{

					log("Remove public link option");
					if(documents.get(0)==null){
						log("The selected node is NULL");
						break;
					}
					((ManagerActivityLollipop) context).showConfirmationRemovePublicLink(documents.get(0));

					break;
				}
				case R.id.cab_menu_edit_link:{

					log("Edit link option");
					if(documents.get(0)==null){
						log("The selected node is NULL");
						break;
					}
					((ManagerActivityLollipop) context).showGetLinkActivity(documents.get(0).getHandle());
					break;
				}
				case R.id.cab_menu_trash:{
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						handleList.add(documents.get(i).getHandle());
					}

					((ManagerActivityLollipop) context).askConfirmationMoveToRubbish(handleList);
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
			((ManagerActivityLollipop)context).hideFabButton();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			log("onDestroyActionMode");
			clearSelections();
			adapter.setMultipleSelect(false);
			((ManagerActivityLollipop)context).showFabButton();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			List<MegaNode> selected = adapter.getSelectedNodes();
			MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);

			// Link
			if ((selected.size() == 1) && (megaApi.checkAccess(selected.get(0), MegaShare.ACCESS_OWNER).getErrorCode() == MegaError.API_OK)) {

				if(selected.get(0).isExported()){
					//Node has public link
					showRemoveLink=true;
					showLink=false;
					showEditLink = true;

				}
				else{
					showRemoveLink=false;
					showLink=true;
					showEditLink = false;
				}

			}
			if (selected.size() != 0) {
				showTrash = true;
				showMove = true;
				showShare = true;
				showCopy = true;

				for(int i=0; i<selected.size();i++)	{
					if(megaApi.checkMove(selected.get(i), megaApi.getRubbishNode()).getErrorCode() != MegaError.API_OK)	{
						showTrash = false;
						showMove = false;
						break;
					}
				}

				if(selected.size()==adapter.getItemCount()){
					menu.findItem(R.id.cab_menu_select_all).setVisible(false);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
					showLink=false;
					showRemoveLink=false;
					showEditLink=false;
					if(selected.size()==1){
						showRename=true;
					}else{
						showRename=false;
					}
				}
				else if(selected.size()==1){
					//showLink=true;
					showRename=true;
					menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
				}
				else{
					menu.findItem(R.id.cab_menu_select_all).setVisible(true);
					unselect.setTitle(getString(R.string.action_unselect_all));
					unselect.setVisible(true);
					showLink=false;
					showRemoveLink=false;
					showRename=false;
					showEditLink=false;
				}
			}
			else{
				menu.findItem(R.id.cab_menu_select_all).setVisible(true);
				menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
			}
			
			menu.findItem(R.id.cab_menu_download).setVisible(true);
			menu.findItem(R.id.cab_menu_download).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.findItem(R.id.cab_menu_rename).setVisible(showRename);
			menu.findItem(R.id.cab_menu_copy).setVisible(showCopy);

			if(showCopy){
				if(selected.size()>1){
					menu.findItem(R.id.cab_menu_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

				}else{
					menu.findItem(R.id.cab_menu_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

				}
			}
			menu.findItem(R.id.cab_menu_move).setVisible(showMove);
			menu.findItem(R.id.cab_menu_share).setVisible(showShare);
			menu.findItem(R.id.cab_menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.cab_menu_edit_link).setVisible(showEditLink);




			menu.findItem(R.id.cab_menu_share_link).setVisible(showLink);
			if(showLink){
				menu.findItem(R.id.cab_menu_share_link).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}else{
				menu.findItem(R.id.cab_menu_share_link).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

			}

			menu.findItem(R.id.cab_menu_share_link_remove).setVisible(showRemoveLink);
			if(showRemoveLink){
				menu.findItem(R.id.cab_menu_share_link_remove).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}else{
				menu.findItem(R.id.cab_menu_share_link_remove).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

			}

			menu.findItem(R.id.cab_menu_trash).setVisible(showTrash);
			menu.findItem(R.id.cab_menu_leave_multiple_share).setVisible(false);
			return false;
		}
		
	}

	public static OutgoingSharesFragmentLollipop newInstance() {
		log("newInstance");
		OutgoingSharesFragmentLollipop fragment = new OutgoingSharesFragmentLollipop();
		return fragment;
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
		nodes = new ArrayList<MegaNode>();
		super.onCreate(savedInstanceState);
		log("onCreate");		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		log("onCreateView");
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		
		if (aB == null){
			aB = ((AppCompatActivity)context).getSupportActionBar();
		}
		
		if (megaApi.getRootNode() == null){
			return null;
		}
		
		display = ((Activity)context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;		

		((ManagerActivityLollipop)context).showFabButton();
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		if (((ManagerActivityLollipop)context).isList){
			View v = inflater.inflate(R.layout.fragment_filebrowserlist, container, false);
			
			recyclerView = (RecyclerView) v.findViewById(R.id.file_list_view_browser);
			fastScroller = (FastScroller) v.findViewById(R.id.fastscroll);

			recyclerView.setPadding(0, 0, 0, Util.scaleHeightPx(85, outMetrics));
			recyclerView.setClipToPadding(false);
			recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, outMetrics));
			mLayoutManager = new LinearLayoutManager(context);
			recyclerView.setLayoutManager(mLayoutManager);
			recyclerView.setItemAnimator(new DefaultItemAnimator());

			emptyImageView = (ImageView) v.findViewById(R.id.file_list_empty_image);
			emptyLinearLayout = (LinearLayout) v.findViewById(R.id.file_list_empty_text);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.file_list_empty_text_first);
			emptyTextViewSecond = (TextView) v.findViewById(R.id.file_list_empty_text_second);

			contentTextLayout = (RelativeLayout) v.findViewById(R.id.content_text_layout);
			contentText = (TextView) v.findViewById(R.id.content_text);

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
			params.addRule(RelativeLayout.BELOW, contentTextLayout.getId());
			recyclerView.setLayoutParams(params);

			transfersOverViewLayout = (RelativeLayout) v.findViewById(R.id.transfers_overview_item_layout);
			transfersOverViewLayout.setVisibility(View.GONE);
			
			if (adapter == null){
				log("Creating the adapter: "+((ManagerActivityLollipop)context).parentHandleOutgoing);
				adapter = new MegaBrowserLollipopAdapter(context, this, nodes, ((ManagerActivityLollipop)context).parentHandleOutgoing, recyclerView, aB, Constants.OUTGOING_SHARES_ADAPTER, MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST);
			}
			else{
				adapter.setAdapterType(MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST);
			}

			if (((ManagerActivityLollipop)context).parentHandleOutgoing == -1){
				log("ParentHandle -1");
				findNodes();
			}else{
				MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleOutgoing);
				log("ParentHandle: "+((ManagerActivityLollipop)context).parentHandleOutgoing);
				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderOthers);
			}

			((ManagerActivityLollipop)context).setToolbarTitle();
			adapter.setNodes(nodes);
			((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
			adapter.setMultipleSelect(false);
			
			recyclerView.setAdapter(adapter);
			fastScroller.setRecyclerView(recyclerView);

			if (adapter != null){
				adapter.setNodes(nodes);
				visibilityFastScroller();

				if (adapter.getItemCount() == 0){
					recyclerView.setVisibility(View.GONE);
					contentTextLayout.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyLinearLayout.setVisibility(View.VISIBLE);

					if (megaApi.getRootNode().getHandle()==((ManagerActivityLollipop)context).parentHandleOutgoing||((ManagerActivityLollipop)context).parentHandleOutgoing==-1) {
						if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
							emptyImageView.setImageResource(R.drawable.outgoing_empty_landscape);
						}else{
							emptyImageView.setImageResource(R.drawable.outgoing_shares_empty);
						}
						emptyTextViewFirst.setText(R.string.context_empty_contacts);
						String text = getString(R.string.context_empty_outgoing);
						emptyTextViewSecond.setText(" "+text+".");
						emptyTextViewSecond.setVisibility(View.VISIBLE);

					}else {
						emptyImageView.setImageResource(R.drawable.ic_empty_folder);
						emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
						emptyTextViewSecond.setVisibility(View.GONE);
					}
				}
				else{
					recyclerView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyLinearLayout.setVisibility(View.GONE);
				}			
			}

			contentText.setText(MegaApiUtils.getInfoNodeOnlyFolders(nodes, context));

			return v;
		}
		else{
			log("Grid View");
			
			View v = inflater.inflate(R.layout.fragment_filebrowsergrid, container, false);
			
			recyclerView = (CustomizedGridRecyclerView) v.findViewById(R.id.file_grid_view_browser);
			fastScroller = (FastScroller) v.findViewById(R.id.fastscroll);

			recyclerView.setPadding(0, 0, 0, Util.scaleHeightPx(80, outMetrics));
			recyclerView.setClipToPadding(false);
			recyclerView.setHasFixedSize(true);
			gridLayoutManager = (CustomizedGridLayoutManager) recyclerView.getLayoutManager();

			recyclerView.setItemAnimator(new DefaultItemAnimator());

			emptyImageView = (ImageView) v.findViewById(R.id.file_grid_empty_image);
			emptyLinearLayout = (LinearLayout) v.findViewById(R.id.file_grid_empty_text);
			emptyTextViewFirst = (TextView) v.findViewById(R.id.file_grid_empty_text_first);
			emptyTextViewSecond = (TextView) v.findViewById(R.id.file_grid_empty_text_second);

			contentTextLayout = (RelativeLayout) v.findViewById(R.id.content_grid_text_layout);
			contentText = (TextView) v.findViewById(R.id.content_grid_text);

			if (adapter == null){
				adapter = new MegaBrowserLollipopAdapter(context, this, nodes, ((ManagerActivityLollipop)context).parentHandleOutgoing, recyclerView, aB, Constants.OUTGOING_SHARES_ADAPTER, MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID);
			}
			else{
				adapter.setAdapterType(MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID);
			}

			if (((ManagerActivityLollipop)context).parentHandleOutgoing == -1){
				log("ParentHandle -1");
				findNodes();
			}
			else{
				MegaNode parentNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleOutgoing);
				log("ParentHandle: "+((ManagerActivityLollipop)context).parentHandleOutgoing);
				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderOthers);
			}
			((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
			if (((ManagerActivityLollipop) context).deepBrowserTreeOutgoing == 0){
				contentText.setText(MegaApiUtils.getInfoNodeOnlyFolders(nodes, context));
			}
			else{
				MegaNode infoNode = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleOutgoing);
				contentText.setText(MegaApiUtils.getInfoFolder(infoNode, context));

				aB.setTitle(infoNode.getName());
			}
			adapter.setMultipleSelect(false);
			
			recyclerView.setAdapter(adapter);
			fastScroller.setRecyclerView(recyclerView);
			visibilityFastScroller();
			if (adapter.getItemCount() == 0){
				recyclerView.setVisibility(View.GONE);
				contentTextLayout.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyLinearLayout.setVisibility(View.VISIBLE);

				if (megaApi.getRootNode().getHandle()==((ManagerActivityLollipop)context).parentHandleOutgoing||((ManagerActivityLollipop)context).parentHandleOutgoing==-1) {

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.outgoing_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.outgoing_shares_empty);
					}
					emptyTextViewFirst.setText(R.string.context_empty_contacts);
					String text = getString(R.string.context_empty_outgoing);
					emptyTextViewSecond.setText(" "+text+".");
					emptyTextViewSecond.setVisibility(View.VISIBLE);

				}else {

					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
					emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
					emptyTextViewSecond.setVisibility(View.GONE);
				}
			}
			else{
				recyclerView.setVisibility(View.VISIBLE);
				contentTextLayout.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyLinearLayout.setVisibility(View.GONE);
			}	
//			setNodes(nodes);	

			return v;
		}		
	}
	
	public void refresh (){
		log("refresh with parentHandle: "+((ManagerActivityLollipop)context).parentHandleOutgoing);

        if (((ManagerActivityLollipop)context).parentHandleOutgoing == -1){
            findNodes();
            aB.setTitle(getString(R.string.section_shared_items));
            log("aB.setHomeAsUpIndicator_1122");
            aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            ((ManagerActivityLollipop)context).setFirstNavigationLevel(true);
            if(adapter != null){
                log("adapter != null");
                adapter.setNodes(nodes);
				visibilityFastScroller();

                if (adapter.getItemCount() == 0){
                    log("adapter.getItemCount() = 0");
                    recyclerView.setVisibility(View.GONE);
                    contentTextLayout.setVisibility(View.GONE);
                    emptyImageView.setVisibility(View.VISIBLE);
                    emptyLinearLayout.setVisibility(View.VISIBLE);

					if (megaApi.getRootNode().getHandle()==((ManagerActivityLollipop)context).parentHandleOutgoing||((ManagerActivityLollipop)context).parentHandleOutgoing==-1) {

						if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
							emptyImageView.setImageResource(R.drawable.outgoing_empty_landscape);
						}else{
							emptyImageView.setImageResource(R.drawable.outgoing_shares_empty);
						}
						emptyTextViewFirst.setText(R.string.context_empty_contacts);
						String text = getString(R.string.context_empty_outgoing);
						emptyTextViewSecond.setText(" "+text+".");
						emptyTextViewSecond.setVisibility(View.VISIBLE);

					}else {

						emptyImageView.setImageResource(R.drawable.ic_empty_folder);
						emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
						emptyTextViewSecond.setVisibility(View.GONE);
					}
                }
                else{

					log("adapter.getItemCount() != 0");
                    recyclerView.setVisibility(View.VISIBLE);
                    contentTextLayout.setVisibility(View.VISIBLE);
                    emptyImageView.setVisibility(View.GONE);
                    emptyLinearLayout.setVisibility(View.GONE);
                }
                contentText.setText(MegaApiUtils.getInfoNodeOnlyFolders(nodes, context));
            }
        }
        else{
            MegaNode n = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleOutgoing);

            aB.setTitle(n.getName());
            log("aB.setHomeAsUpIndicator_39");
            aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
            ((ManagerActivityLollipop)context).setFirstNavigationLevel(false);
            ((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

            contentText.setText(MegaApiUtils.getInfoFolder(n, context));

            nodes = megaApi.getChildren(n, ((ManagerActivityLollipop)context).orderOthers);
            adapter.setNodes(nodes);
			visibilityFastScroller();

            //If folder has no files
            if (adapter.getItemCount() == 0){
                recyclerView.setVisibility(View.GONE);
                contentTextLayout.setVisibility(View.GONE);
                emptyImageView.setVisibility(View.VISIBLE);
                emptyLinearLayout.setVisibility(View.VISIBLE);

                if (megaApi.getRootNode().getHandle()==n.getHandle()) {

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.outgoing_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.outgoing_shares_empty);
					}
					emptyTextViewFirst.setText(R.string.context_empty_contacts);
					String text = getString(R.string.context_empty_outgoing);
					emptyTextViewSecond.setText(" "+text+".");
					emptyTextViewSecond.setVisibility(View.VISIBLE);

				} else {

					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
                    emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
					emptyTextViewSecond.setVisibility(View.GONE);
				}
            }
            else{
                recyclerView.setVisibility(View.VISIBLE);
                contentTextLayout.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.GONE);
                emptyLinearLayout.setVisibility(View.GONE);
            }
        }
        ((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
	}

	public void refreshContent (){
		log("refreshContent with parentHandle: "+((ManagerActivityLollipop)context).parentHandleOutgoing);

		if (((ManagerActivityLollipop)context).parentHandleOutgoing == -1){
			findNodes();
			if(adapter != null){
				log("adapter != null");
				adapter.setNodes(nodes);
				visibilityFastScroller();

				if (adapter.getItemCount() == 0){
					log("adapter.getItemCount() = 0");
					recyclerView.setVisibility(View.GONE);
					contentTextLayout.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyLinearLayout.setVisibility(View.VISIBLE);
				}
				else{
					log("adapter.getItemCount() != 0");
					recyclerView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyLinearLayout.setVisibility(View.GONE);
				}
				contentText.setText(MegaApiUtils.getInfoNodeOnlyFolders(nodes, context));
			}
		}
		else{
			MegaNode n = megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleOutgoing);
			contentText.setText(MegaApiUtils.getInfoFolder(n, context));

			nodes = megaApi.getChildren(n, ((ManagerActivityLollipop)context).orderOthers);
			adapter.setNodes(nodes);
			visibilityFastScroller();

			//If folder has no files
			if (adapter.getItemCount() == 0){
				recyclerView.setVisibility(View.GONE);
				contentTextLayout.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyLinearLayout.setVisibility(View.VISIBLE);

				if (megaApi.getRootNode().getHandle()==n.getHandle()) {

					if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
						emptyImageView.setImageResource(R.drawable.outgoing_empty_landscape);
					}else{
						emptyImageView.setImageResource(R.drawable.outgoing_shares_empty);
					}
					emptyTextViewFirst.setText(R.string.context_empty_contacts);
					String text = getString(R.string.context_empty_outgoing);
					emptyTextViewSecond.setText(" "+text+".");
					emptyTextViewSecond.setVisibility(View.VISIBLE);

				} else {
					emptyImageView.setImageResource(R.drawable.ic_empty_folder);
					emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
					emptyTextViewSecond.setVisibility(View.GONE);
				}
			}
			else{
				recyclerView.setVisibility(View.VISIBLE);
				contentTextLayout.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyLinearLayout.setVisibility(View.GONE);
			}
		}
		((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
	}

	public void findNodes(){	
		log("findNodes");
		ArrayList<MegaShare> outNodeList = megaApi.getOutShares();
		nodes.clear();
		long lastFolder=-1;		
		
		for(int k=0;k<outNodeList.size();k++){
			
			if(outNodeList.get(k).getUser()!=null){
				MegaShare mS = outNodeList.get(k);				
				MegaNode node = megaApi.getNodeByHandle(mS.getNodeHandle());
				
				if(lastFolder!=node.getHandle()){
					lastFolder=node.getHandle();
					nodes.add(node);			
				}	
			}
		}

		if(((ManagerActivityLollipop)context).orderOthers == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }

    public void itemClick(int position) {
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

				((ManagerActivityLollipop) context).increaseDeepBrowserTreeOutgoing();
				log("deepBrowserTree after clicking folder"+((ManagerActivityLollipop) context).deepBrowserTreeOutgoing);
				
				MegaNode n = nodes.get(position);

				int lastFirstVisiblePosition = 0;
				if(((ManagerActivityLollipop)context).isList){
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

				((ManagerActivityLollipop)context).setParentHandleOutgoing(n.getHandle());
				aB.setTitle(n.getName());
				log("aB.setHomeAsUpIndicator_40");
				aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
				((ManagerActivityLollipop)context).setFirstNavigationLevel(false);
				((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
				
				MegaNode infoNode = megaApi.getNodeByHandle(n.getHandle());
				contentText.setText(MegaApiUtils.getInfoFolder(infoNode, context));

				nodes = megaApi.getChildren(nodes.get(position), ((ManagerActivityLollipop)context).orderOthers);
				adapter.setNodes(nodes);
				recyclerView.scrollToPosition(0);
				visibilityFastScroller();

				//If folder has no files
				if (adapter.getItemCount() == 0){
					recyclerView.setVisibility(View.GONE);
					contentTextLayout.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyLinearLayout.setVisibility(View.VISIBLE);

					if (megaApi.getRootNode().getHandle()==n.getHandle()) {

						if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
							emptyImageView.setImageResource(R.drawable.outgoing_empty_landscape);
						}else{
							emptyImageView.setImageResource(R.drawable.outgoing_shares_empty);
						}
						emptyTextViewFirst.setText(R.string.context_empty_contacts);
						String text = getString(R.string.context_empty_outgoing);
						emptyTextViewSecond.setText(" "+text+".");
						emptyTextViewSecond.setVisibility(View.VISIBLE);

					}else {
						emptyImageView.setImageResource(R.drawable.ic_empty_folder);
						emptyTextViewFirst.setText(R.string.file_browser_empty_folder);
						emptyTextViewSecond.setVisibility(View.GONE);

					}
				}
				else{
					recyclerView.setVisibility(View.VISIBLE);
					contentTextLayout.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyLinearLayout.setVisibility(View.GONE);
				}

				((ManagerActivityLollipop) context).showFabButton();
			}
			else{
				//Is file
				if (MimeTypeList.typeForName(nodes.get(position).getName()).isImage()){
					Intent intent = new Intent(context, FullScreenImageViewerLollipop.class);
					intent.putExtra("position", position);
					intent.putExtra("adapterType", Constants.FILE_BROWSER_ADAPTER);
					intent.putExtra("isFolderLink", false);
					MyAccountInfo accountInfo = ((ManagerActivityLollipop)context).getMyAccountInfo();
					if(accountInfo!=null){
						intent.putExtra("typeAccount", accountInfo.getAccountType());
					}
					if (megaApi.getParentNode(nodes.get(position)).getType() == MegaNode.TYPE_ROOT){
						intent.putExtra("parentNodeHandle", -1L);
					}
					else{
						intent.putExtra("parentNodeHandle", megaApi.getParentNode(nodes.get(position)).getHandle());
					}
					intent.putExtra("orderGetChildren", ((ManagerActivityLollipop)context).orderOthers);
					startActivity(intent);
							
				}
				else if (MimeTypeList.typeForName(nodes.get(position).getName()).isVideo() || MimeTypeList.typeForName(nodes.get(position).getName()).isAudio() ){
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
					if (file.getName().contains(".avi") || file.getName().contains(".wmv") || file.getName().contains(".mpg")
							|| file.getName().contains(".flv") || file.getName().contains(".vob") || file.getName().contains(".mts")){
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
			  			context.startActivity(mediaIntent);
			  		}
			  		else{
						((ManagerActivityLollipop) context).showSnackbar(context.getResources().getString(R.string.intent_not_available));
			  			adapter.notifyDataSetChanged();
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
	
	public boolean showSelectMenuItem(){
		if (adapter != null){
			return adapter.isMultipleSelect();
		}

		return false;
	}

	public void setNodes(ArrayList<MegaNode> nodes){
		log("setNodes");
		this.nodes = nodes;
		if(((ManagerActivityLollipop)context).orderOthers == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}
	}
	
	public void sortByNameDescending(){
		
		ArrayList<String> foldersOrder = new ArrayList<String>();
		ArrayList<String> filesOrder = new ArrayList<String>();
		ArrayList<MegaNode> tempOffline = new ArrayList<MegaNode>();
		
		
		for(int k = 0; k < nodes.size() ; k++) {
			MegaNode node = nodes.get(k);
			if(node.isFolder()){
				foldersOrder.add(node.getName());
			}
			else{
				filesOrder.add(node.getName());
			}
		}
		
	
		Collections.sort(foldersOrder, String.CASE_INSENSITIVE_ORDER);
		Collections.reverse(foldersOrder);
		Collections.sort(filesOrder, String.CASE_INSENSITIVE_ORDER);
		Collections.reverse(filesOrder);

		for(int k = 0; k < foldersOrder.size() ; k++) {
			for(int j = 0; j < nodes.size() ; j++) {
				String name = foldersOrder.get(k);
				String nameOffline = nodes.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(nodes.get(j));
				}				
			}
			
		}
		
		for(int k = 0; k < filesOrder.size() ; k++) {
			for(int j = 0; j < nodes.size() ; j++) {
				String name = filesOrder.get(k);
				String nameOffline = nodes.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(nodes.get(j));					
				}				
			}
			
		}
		
		nodes.clear();
		nodes.addAll(tempOffline);

		adapter.setNodes(nodes);
	}

	
	public void sortByNameAscending(){
		log("sortByNameAscending");
		ArrayList<String> foldersOrder = new ArrayList<String>();
		ArrayList<String> filesOrder = new ArrayList<String>();
		ArrayList<MegaNode> tempOffline = new ArrayList<MegaNode>();
				
		for(int k = 0; k < nodes.size() ; k++) {
			MegaNode node = nodes.get(k);
			if(node.isFolder()){
				foldersOrder.add(node.getName());
			}
			else{
				filesOrder.add(node.getName());
			}
		}		
	
		Collections.sort(foldersOrder, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(filesOrder, String.CASE_INSENSITIVE_ORDER);

		for(int k = 0; k < foldersOrder.size() ; k++) {
			for(int j = 0; j < nodes.size() ; j++) {
				String name = foldersOrder.get(k);
				String nameOffline = nodes.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(nodes.get(j));
				}				
			}			
		}
		
		for(int k = 0; k < filesOrder.size() ; k++) {
			for(int j = 0; j < nodes.size() ; j++) {
				String name = filesOrder.get(k);
				String nameOffline = nodes.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(nodes.get(j));
				}				
			}
			
		}
		
		nodes.clear();
		nodes.addAll(tempOffline);

		adapter.setNodes(nodes);
	}
			
	/*
	 * Clear all selected items
	 */
	private void clearSelections() {
		if(adapter.isMultipleSelect()){
			adapter.clearSelections();
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
		// actionMode.
	}
	
	/*
	 * Disable selection
	 */
	public void hideMultipleSelect() {
		adapter.setMultipleSelect(false);
		((ManagerActivityLollipop)context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_TRANSPARENT_BLACK);
		if (actionMode != null) {
			actionMode.finish();
		}
	}

	
	public int onBackPressed(){

		log("onBackPressed");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		log("deepBrowserTree "+((ManagerActivityLollipop) context).deepBrowserTreeOutgoing);
					
		if (adapter == null){
			return 0;
		}

		((ManagerActivityLollipop) context).decreaseDeepBrowserTreeOutgoing();
		((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
		if(((ManagerActivityLollipop) context).deepBrowserTreeOutgoing==0){
			log("deepBrowserTree==0");
			//In the beginning of the navigation
			((ManagerActivityLollipop)context).setParentHandleOutgoing(-1);
			log("Shared With Me");
			aB.setTitle(getString(R.string.section_shared_items));
			log("aB.setHomeAsUpIndicator_41");
			aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
			((ManagerActivityLollipop)context).setFirstNavigationLevel(true);
			findNodes();

			adapter.setNodes(nodes);
			visibilityFastScroller();

			int lastVisiblePosition = 0;
			if(!lastPositionStack.empty()){
				lastVisiblePosition = lastPositionStack.pop();
				log("Pop of the stack "+lastVisiblePosition+" position");
			}
			log("Scroll to "+lastVisiblePosition+" position");

			if(lastVisiblePosition>=0){

				if(((ManagerActivityLollipop)context).isList){
					mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
				}
				else{
					gridLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
				}
			}

			contentText.setText(MegaApiUtils.getInfoNodeOnlyFolders(nodes, context));
			recyclerView.setVisibility(View.VISIBLE);
			contentTextLayout.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyLinearLayout.setVisibility(View.GONE);
			((ManagerActivityLollipop) context).showFabButton();
			return 3;
		}
		else if (((ManagerActivityLollipop) context).deepBrowserTreeOutgoing>0){
			log("Keep navigation");

			MegaNode parentNode = megaApi.getParentNode(megaApi.getNodeByHandle(((ManagerActivityLollipop)context).parentHandleOutgoing));
			contentText.setText(MegaApiUtils.getInfoFolder(parentNode, context));
			if (parentNode != null){
				recyclerView.setVisibility(View.VISIBLE);
				contentTextLayout.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyLinearLayout.setVisibility(View.GONE);

				((ManagerActivityLollipop)context).setParentHandleOutgoing(parentNode.getHandle());
				aB.setTitle(parentNode.getName());		
				log("aB.setHomeAsUpIndicator_42");
				aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
				((ManagerActivityLollipop)context).setFirstNavigationLevel(false);
				((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();

				nodes = megaApi.getChildren(parentNode, ((ManagerActivityLollipop)context).orderOthers);
				adapter.setNodes(nodes);
				visibilityFastScroller();

				int lastVisiblePosition = 0;
				if(!lastPositionStack.empty()){
					lastVisiblePosition = lastPositionStack.pop();
					log("Pop of the stack "+lastVisiblePosition+" position");
				}
				log("Scroll to "+lastVisiblePosition+" position");

				if(lastVisiblePosition>=0){

					if(((ManagerActivityLollipop)context).isList){
						mLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
					}
					else{
						gridLayoutManager.scrollToPositionWithOffset(lastVisiblePosition, 0);
					}
				}

			}
			((ManagerActivityLollipop) context).showFabButton();
			return 2;
		}
		else{
			log("Back to Cloud");
			recyclerView.setVisibility(View.VISIBLE);
			contentTextLayout.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyLinearLayout.setVisibility(View.GONE);
//			((ManagerActivityLollipop)context).setParentHandleBrowser(megaApi.getRootNode().getHandle());
			((ManagerActivityLollipop) context).deepBrowserTreeOutgoing=0;
			return 0;
		}
	}
	
	public RecyclerView getRecyclerView(){
		return recyclerView;
	}
	
//	public void setNodes(ArrayList<MegaNode> nodes){
//		this.nodes = nodes;
//		if (isList){
//			if (adapterList != null){
//				adapterList.setNodes(nodes);
//				if (adapterList.getCount() == 0){
//					listView.setVisibility(View.GONE);
//					emptyImageView.setVisibility(View.VISIBLE);
//					emptyLinearLayout.setVisibility(View.VISIBLE);
//					contentText.setVisibility(View.GONE);
//				}
//				else{
//					listView.setVisibility(View.VISIBLE);
//					emptyImageView.setVisibility(View.GONE);
//					emptyLinearLayout.setVisibility(View.GONE);
//					contentText.setText(getInfoNode());
//					contentText.setVisibility(View.VISIBLE);
//				}			
//			}	
//		}
//		else{
//			if (adapterGrid != null){
//				adapterGrid.setNodes(nodes);
//				if (adapterGrid.getCount() == 0){
//					listView.setVisibility(View.GONE);
//					emptyImageView.setVisibility(View.VISIBLE);
//					emptyLinearLayout.setVisibility(View.VISIBLE);
//				}
//				else{
//					listView.setVisibility(View.VISIBLE);
//					emptyImageView.setVisibility(View.GONE);
//					emptyLinearLayout.setVisibility(View.GONE);
//
//				}			
//			}
//		}
//	}

	public void notifyDataSetChanged(){
		if (adapter != null){
			adapter.notifyDataSetChanged();
		}
	}

	public int getItemCount(){
		if(adapter!=null){
			return adapter.getItemCount();
		}
		return 0;
	}

	public boolean isMultipleselect(){
		return adapter.isMultipleSelect();
	}

	private static void log(String log) {
		Util.log("OutgoingSharesFragmentLollipop", log);
	}

	public void visibilityFastScroller(){
		if(adapter == null){
			fastScroller.setVisibility(View.GONE);
		}else{
			if(adapter.getItemCount() < Constants.MIN_ITEMS_SCROLLBAR){
				fastScroller.setVisibility(View.GONE);
			}else{
				fastScroller.setVisibility(View.VISIBLE);
			}
		}

	}
}
