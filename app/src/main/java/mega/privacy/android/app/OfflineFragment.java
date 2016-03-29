package mega.privacy.android.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaNode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class OfflineFragment extends Fragment implements OnClickListener, OnItemClickListener, OnItemLongClickListener{
	
	static int FROM_FILE_BROWSER = 13;
	static int FROM_INCOMING_SHARES= 14;
	static int FROM_OFFLINE= 15;

	Context context;
	ActionBar aB;
	ListView listView;
	ImageView emptyImageView;
	TextView emptyTextView;
	MegaOfflineListAdapter adapterList;
	MegaOfflineGridAdapter adapterGrid;
	OfflineFragment offlineFragment = this;
	DatabaseHandler dbH = null;
	ArrayList<MegaOffline> mOffList= null;
	String pathNavigation = null;
	TextView contentText;
	long parentHandle = -1;
	boolean isList = true;
	boolean gridNavigation=false;
	int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;
	public static String DB_FILE = "0";
	public static String DB_FOLDER = "1";
	MegaApiAndroid megaApi;
	LinearLayout outSpaceLayout=null;
	TextView outSpaceText;
	Button outSpaceButton;
	int usedSpacePerc;
	
	private ActionMode actionMode;
	
	private class ActionBarCallBack implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			log("ActionBarCallBack::onActionItemClicked");
			List<MegaOffline> documents = getSelectedDocuments();
			
			switch(item.getItemId()){
				case R.id.cab_menu_download:{
					
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						String path = documents.get(i).getPath() + documents.get(i).getName();
						MegaNode n = megaApi.getNodeByPath(path);	
						if(n == null)
						{
							continue;
						}
						handleList.add(n.getHandle());
					}
					clearSelections();
					hideMultipleSelect();
					((ManagerActivity) context).onFileClick(handleList);
					
					break;
				}
				case R.id.cab_menu_rename:{
					clearSelections();
					hideMultipleSelect();
					if (documents.size()==1){
						String path = documents.get(0).getPath() + documents.get(0).getName();
						MegaNode n = megaApi.getNodeByPath(path);
						if(n == null)
						{
							break;
						}
						((ManagerActivity) context).showRenameDialog(n, n.getName());
					}
					break;
				}
				case R.id.cab_menu_share_folder:{
				
					clearSelections();
					hideMultipleSelect();
					if (documents.size()==1){
						String path = documents.get(0).getPath() + documents.get(0).getName();
						MegaNode n = megaApi.getNodeByPath(path);	
						if(n == null)
						{
							break;
						}
						((ManagerActivity) context).getPublicLinkAndShareIt(n);
					}
					
					break;
				}
				case R.id.cab_menu_move:{					
					ArrayList<Long> handleList = new ArrayList<Long>();
					for (int i=0;i<documents.size();i++){
						String path = documents.get(i).getPath() + documents.get(i).getName();
						MegaNode n = megaApi.getNodeByPath(path);			
						if(n == null)
						{
							continue;
						}
						handleList.add(n.getHandle());
					}
					clearSelections();
					hideMultipleSelect();
					((ManagerActivity) context).showMove(handleList);
					break;
				}
				case R.id.cab_menu_copy:{
					ArrayList<Long> handleList = new ArrayList<Long>();					
					for (int i=0;i<documents.size();i++){
						String path = documents.get(i).getPath() + documents.get(i).getName();
						MegaNode n = megaApi.getNodeByPath(path);
						if(n == null)
						{
							continue;
						}
						handleList.add(n.getHandle());
					}
					clearSelections();
					hideMultipleSelect();
					((ManagerActivity) context).showCopy(handleList);
					break;
				}
				case R.id.cab_menu_trash:{
					
					for (int i=0;i<documents.size();i++){						
						deleteOffline(context, documents.get(i));
					}					
					clearSelections();
					hideMultipleSelect();
					refreshPaths(documents.get(0));
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
			}
			return false;
		}
		
		private int deleteOffline(Context context,MegaOffline node){
			
			log("deleteOffline");

//			dbH = new DatabaseHandler(context);
			dbH = DatabaseHandler.getDbHandler(context);

			ArrayList<MegaOffline> mOffListParent=new ArrayList<MegaOffline>();
			ArrayList<MegaOffline> mOffListChildren=new ArrayList<MegaOffline>();			
			MegaOffline parentNode = null;	
			
			//Delete children
			mOffListChildren=dbH.findByParentId(node.getId());
			if(mOffListChildren.size()>0){
				//The node have childrens, delete
				deleteChildrenDB(mOffListChildren);			
			}
			
			int parentId = node.getParentId();
			log("Finding parents...");
			//Delete parents
			if(parentId!=-1){
				mOffListParent=dbH.findByParentId(parentId);
				
				log("Same Parent?:" +mOffListParent.size());
				
				if(mOffListParent.size()<1){
					//No more node with the same parent, keep deleting				

					parentNode = dbH.findById(parentId);
					log("Recursive parent: "+parentNode.getName());
					if(parentNode != null){
						deleteOffline(context, parentNode);	
							
					}	
				}			
			}	
			
			log("Remove the node physically");
			//Remove the node physically
			File destination = null;								

			if (Environment.getExternalStorageDirectory() != null){
				destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + node.getPath());
			}
			else{
				destination = context.getFilesDir();
			}	

			try{
				File offlineFile = new File(destination, node.getName());	
				log("Delete in phone: "+node.getName());
				Util.deleteFolderAndSubfolders(context, offlineFile);
			}
			catch(Exception e){
				log("EXCEPTION: deleteOffline - adapter");
			};		
			
			dbH.removeById(node.getId());		
			
			return 1;		
		}
		
		private void deleteChildrenDB(ArrayList<MegaOffline> mOffListChildren){

			log("deleteChildenDB: "+mOffListChildren.size());
			MegaOffline mOffDelete=null;

			for(int i=0; i<mOffListChildren.size(); i++){	

				mOffDelete=mOffListChildren.get(i);

				log("Children "+i+ ": "+ mOffDelete.getName());
				ArrayList<MegaOffline> mOffListChildren2=dbH.findByParentId(mOffDelete.getId());
				if(mOffListChildren2.size()>0){
					//The node have children, delete				
					deleteChildrenDB(mOffListChildren2);				
				}	

				int lines = dbH.removeById(mOffDelete.getId());		
				log("Borradas; "+lines);
			}		
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			log("ActionBarCallBack::onCreateActionMode");
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.file_browser_action, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode arg0) {
			log("ActionBarCallBack::onDestroActionMode");
			adapterList.setMultipleSelect(false);
			listView.setOnItemLongClickListener(offlineFragment);
			clearSelections();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			log("ActionBarCallBack::onPrepareActionMode");
			List<MegaOffline> selected = getSelectedDocuments();
			
			if (Util.isOnline(context)){
				if (selected.size() != 0) {
					
					if(selected.size()==adapterList.getCount()){
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
				
				if (selected.size() == 1) {
					menu.findItem(R.id.cab_menu_share_link).setVisible(true);
					menu.findItem(R.id.cab_menu_rename).setVisible(true);
				}
				else{
					menu.findItem(R.id.cab_menu_share_link).setVisible(false);
					menu.findItem(R.id.cab_menu_rename).setVisible(false);
				}

				menu.findItem(R.id.cab_menu_download).setVisible(true);				
				menu.findItem(R.id.cab_menu_copy).setVisible(true);
				menu.findItem(R.id.cab_menu_move).setVisible(true);				
				menu.findItem(R.id.cab_menu_trash).setVisible(true);
			}
			else{
				if (selected.size() != 0) {
				
					if(selected.size()==adapterList.getCount()){
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
				
				if (selected.size() == 1) {
					menu.findItem(R.id.cab_menu_rename).setVisible(true);
				}
				else{
					menu.findItem(R.id.cab_menu_rename).setVisible(false);
				}

				menu.findItem(R.id.cab_menu_download).setVisible(false);			
				menu.findItem(R.id.cab_menu_copy).setVisible(false);
				menu.findItem(R.id.cab_menu_move).setVisible(false);
				menu.findItem(R.id.cab_menu_share_link).setVisible(false);
				menu.findItem(R.id.cab_menu_trash).setVisible(true);
			}			
			menu.findItem(R.id.cab_menu_leave_multiple_share).setVisible(false);
			return false;
		}
		
	}
	
	public void selectAll(){
		actionMode = ((ActionBarActivity)context).startSupportActionMode(new ActionBarCallBack());

		adapterList.setMultipleSelect(true);
		for ( int i=0; i< adapterList.getCount(); i++ ) {
			listView.setItemChecked(i, true);
		}
		updateActionModeTitle();
		listView.setOnItemLongClickListener(null);
	}
	
	public boolean showSelectMenuItem(){
		if (isList){
			if (adapterList != null){
				return adapterList.isMultipleSelect();
			}
		}
		else{
			if (adapterGrid != null){
				return adapterGrid.isMultipleSelect();
			}
		}
		
		return false;
	}
		
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		log("onCreate");
		
		if (Util.isOnline(context)){
			if (megaApi == null){
				megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
			}
		}
		else{
			megaApi=null;
		}			
		
//		dbH = new DatabaseHandler(context);
		dbH = DatabaseHandler.getDbHandler(context);
		
		mOffList = new ArrayList<MegaOffline>();		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log("onCreateView");
		if (aB == null){
			aB = ((ActionBarActivity)context).getSupportActionBar();
		}
		
		aB.setTitle(getString(R.string.section_saved_for_offline));	
		aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
		if (context instanceof ManagerActivity && ((ManagerActivity)context).getmDrawerToggle() != null){
			((ManagerActivity)context).getmDrawerToggle().setDrawerIndicatorEnabled(true);
			((ManagerActivity)context).supportInvalidateOptionsMenu();
		}
		
		//Check pathNAvigation
		if (isList){
			View v = inflater.inflate(R.layout.fragment_offlinelist, container, false);
	        
	        listView = (ListView) v.findViewById(R.id.offline_view_browser);
			listView.setOnItemClickListener(this);
			listView.setOnItemLongClickListener(this);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listView.setItemsCanFocus(false);
			
			emptyImageView = (ImageView) v.findViewById(R.id.offline_empty_image);
			emptyTextView = (TextView) v.findViewById(R.id.offline_empty_text);		
					
			contentText = (TextView) v.findViewById(R.id.offline_content_text);
			
			outSpaceLayout = (LinearLayout) v.findViewById(R.id.offline_out_space);
			outSpaceText =  (TextView) v.findViewById(R.id.offline_out_space_text);
			outSpaceButton = (Button) v.findViewById(R.id.offline_out_space_btn);
			
			if (context instanceof ManagerActivity){
				
				outSpaceButton.setOnClickListener(this);
				usedSpacePerc=((ManagerActivity)context).getUsedPerc();
				
				if(usedSpacePerc>95){
					//Change below of ListView
					log("usedSpacePerc>95");
//					RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//					p.addRule(RelativeLayout.ABOVE, R.id.out_space);
//					listView.setLayoutParams(p);
					outSpaceLayout.setVisibility(View.VISIBLE);
					outSpaceLayout.bringToFront();
					
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {					
						
						@Override
						public void run() {
							log("BUTTON DISAPPEAR");
							log("altura: "+outSpaceLayout.getHeight());
							
							TranslateAnimation animTop = new TranslateAnimation(0, 0, 0, outSpaceLayout.getHeight());
							animTop.setDuration(2000);
							animTop.setFillAfter(true);
							outSpaceLayout.setAnimation(animTop);
						
							outSpaceLayout.setVisibility(View.GONE);
							outSpaceLayout.invalidate();
//							RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//							p.addRule(RelativeLayout.ABOVE, R.id.buttons_layout);
//							listView.setLayoutParams(p);
						}
					}, 15 * 1000);
					
				}	
				else{
					outSpaceLayout.setVisibility(View.GONE);
				}
			}
			else{
				outSpaceLayout.setVisibility(View.GONE);
			}		
			

			mOffList=dbH.findByPath(pathNavigation);
			
			log("Number of elements: "+mOffList.size());
									
			for(int i=0; i<mOffList.size();i++){
				
				MegaOffline checkOffline = mOffList.get(i);
				
				if(!checkOffline.isIncoming()){				
					log("NOT isIncomingOffline");
					File offlineDirectory = null;
					if (Environment.getExternalStorageDirectory() != null){
						offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + checkOffline.getPath()+checkOffline.getName());
					}
					else{
						offlineDirectory = context.getFilesDir();
					}	
					
					if (!offlineDirectory.exists()){
						log("Path to remove A: "+(mOffList.get(i).getPath()+mOffList.get(i).getName()));
						//dbH.removeById(mOffList.get(i).getId());
						mOffList.remove(i);		
						i--;
					}	
				}
				else{
					log("isIncomingOffline");
					File offlineDirectory = null;
					if (Environment.getExternalStorageDirectory() != null){
						offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" +checkOffline.getHandleIncoming() + "/" + checkOffline.getPath()+checkOffline.getName());
						log("offlineDirectory: "+offlineDirectory);
					}
					else{
						offlineDirectory = context.getFilesDir();
					}	
					
					if (!offlineDirectory.exists()){
						log("Path to remove B: "+(mOffList.get(i).getPath()+mOffList.get(i).getName()));
						//dbH.removeById(mOffList.get(i).getId());
						mOffList.remove(i);
						i--;
					}	
					
				}
			}

			
			if (adapterList == null){
				adapterList = new MegaOfflineListAdapter(this, context, mOffList, listView, emptyImageView, emptyTextView, aB);
			}
			else{
				adapterList.setNodes(mOffList);
			}
			
			if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
				sortByNameDescending();
			}
			else{
				sortByNameAscending();
			}
			
			adapterList.setPositionClicked(-1);
			adapterList.setMultipleSelect(false);
			
			listView.setAdapter(adapterList);
						
			if (adapterList.getCount() == 0){
				listView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				contentText.setVisibility(View.GONE);
				emptyImageView.setImageResource(R.drawable.ic_empty_offline);
				emptyTextView.setText(R.string.file_browser_empty_folder);
			}
			else{
				listView.setVisibility(View.VISIBLE);
				contentText.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
			}
			contentText.setText(getInfoFolder(mOffList));
		
			return v;
		}
		else{
			View v = inflater.inflate(R.layout.fragment_offlinegrid, container, false);
			
			listView = (ListView) v.findViewById(R.id.offline_grid_view_browser);
			listView.setOnItemClickListener(null);
			listView.setItemsCanFocus(false);
	        
	        emptyImageView = (ImageView) v.findViewById(R.id.offline_grid_empty_image);
			emptyTextView = (TextView) v.findViewById(R.id.offline_grid_empty_text);
			
			mOffList=dbH.findByPath(pathNavigation);
			
			for(int i=0; i<mOffList.size();i++){
				
				File offlineDirectory = null;
				if (Environment.getExternalStorageDirectory() != null){
					offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + mOffList.get(i).getPath()+mOffList.get(i).getName());
				}
				else{
					offlineDirectory = context.getFilesDir();
				}	
				
				if (!offlineDirectory.exists()){
					dbH.removeById(mOffList.get(i).getId());
					mOffList.remove(i);
					
				}			
			}			
			
			if (adapterGrid == null){
				adapterGrid = new MegaOfflineGridAdapter(this, context, mOffList, listView, emptyImageView, emptyTextView, aB);
			}
			else{
				adapterGrid.setNodes(mOffList);
			}
			
			if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
				sortByNameDescending();
			}
			else{
				sortByNameAscending();
			}			
						
			if (adapterGrid.getCount() == 0){
				listView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				emptyImageView.setImageResource(R.drawable.ic_empty_offline);
				emptyTextView.setText(R.string.file_browser_empty_folder);
			}
			else{
				listView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);
			}	
			
			contentText.setText(getInfoFolder(mOffList));
			
			adapterGrid.setPositionClicked(-1);
			//adapterGrid.setMultipleSelect(false);
			listView.setAdapter(adapterGrid);
			
			return v;
		}		
	}
	
	public void download (String path){
		
		if (Util.isOnline(context)){
			ArrayList<Long> handleList = new ArrayList<Long>();
			MegaNode node = megaApi.getNodeByPath(path);
			if(node == null)
			{
				return;
			}
			
			handleList.add(node.getHandle());
			log("download "+node.getName());
			((ManagerActivity) context).onFileClick(handleList);
		}
		else{
			//TODO toast no connection
		}
	}
	
	public void sortByNameDescending(){
		
		ArrayList<String> foldersOrder = new ArrayList<String>();
		ArrayList<String> filesOrder = new ArrayList<String>();
		ArrayList<MegaOffline> tempOffline = new ArrayList<MegaOffline>();
		
		
		for(int k = 0; k < mOffList.size() ; k++) {
			MegaOffline node = mOffList.get(k);
			if(node.getType().equals("1")){
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
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = foldersOrder.get(k);
				String nameOffline = mOffList.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(mOffList.get(j));
				}				
			}
			
		}
		
		for(int k = 0; k < filesOrder.size() ; k++) {
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = filesOrder.get(k);
				String nameOffline = mOffList.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(mOffList.get(j));					
				}				
			}
			
		}
		
		mOffList.clear();
		mOffList.addAll(tempOffline);

		if (isList){
			adapterList.setNodes(mOffList);
		}
		else{
			adapterGrid.setNodes(mOffList);
		}
	}

	
	public void sortByNameAscending(){
		log("sortByNameAscending");
		ArrayList<String> foldersOrder = new ArrayList<String>();
		ArrayList<String> filesOrder = new ArrayList<String>();
		ArrayList<MegaOffline> tempOffline = new ArrayList<MegaOffline>();
				
		for(int k = 0; k < mOffList.size() ; k++) {
			MegaOffline node = mOffList.get(k);
			if(node.getType().equals("1")){
				foldersOrder.add(node.getName());
			}
			else{
				filesOrder.add(node.getName());
			}
		}		
	
		Collections.sort(foldersOrder, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(filesOrder, String.CASE_INSENSITIVE_ORDER);

		for(int k = 0; k < foldersOrder.size() ; k++) {
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = foldersOrder.get(k);
				String nameOffline = mOffList.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(mOffList.get(j));
				}				
			}			
		}
		
		for(int k = 0; k < filesOrder.size() ; k++) {
			for(int j = 0; j < mOffList.size() ; j++) {
				String name = filesOrder.get(k);
				String nameOffline = mOffList.get(j).getName();
				if(name.equals(nameOffline)){
					tempOffline.add(mOffList.get(j));
				}				
			}
			
		}
		
		mOffList.clear();
		mOffList.addAll(tempOffline);

		if (isList){
			adapterList.setNodes(mOffList);
		}
		else{
			adapterGrid.setNodes(mOffList);
		}

	}
	
//	public void updateView (){
//		log("updateView");
//		mOffList=dbH.findByPath(pathNavigation);
//		
//		for(int i=0; i<mOffList.size();i++){
//			
//			File offlineDirectory = null;
//			if (Environment.getExternalStorageDirectory() != null){
//				offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + mOffList.get(i).getPath()+mOffList.get(i).getName());
//			}
//			else{
//				offlineDirectory = context.getFilesDir();
//			}	
//			
//			if (!offlineDirectory.exists()){
//				dbH.removeById(mOffList.get(i).getId());
//				mOffList.remove(i);
//				
//			}			
//		}
//		this.setNodes(mOffList);
//	}
//	
	public void showProperties (String path, String handle){
		log("showProperties: "+path);
		
		MegaNode n = megaApi.getNodeByHandle(Long.valueOf(handle));		
		if(n == null)
		{
			return;
		}
		
		Intent i = new Intent(context, FilePropertiesActivity.class);
		i.putExtra("handle", Long.valueOf(handle));
		i.putExtra("from", FROM_OFFLINE);

		if (n.isFolder()) {
			if (megaApi.isShared(n)){
				i.putExtra("imageId", R.drawable.folder_shared_mime);	
			}
			else{
				i.putExtra("imageId", R.drawable.folder_mime);
			}

		} 
		else {
			i.putExtra("imageId", MimeTypeMime.typeForName(n.getName()).getIconResourceId());
		}
		i.putExtra("name", n.getName());
		context.startActivity(i);
	}
	
	public void getLink (String path){
		MegaNode n = megaApi.getNodeByPath(path);
		if(n == null)
		{
			return;
		}
		
		((ManagerActivity) context).getPublicLinkAndShareIt(n);

	}
	
	public void shareFolder (String path){
		MegaNode n = megaApi.getNodeByPath(path);
		if(n == null)
		{
			return;
		}
		
		((ManagerActivity) context).shareFolder(n);
	}
	
	public void rename (String path){
		MegaNode n = megaApi.getNodeByPath(path);
		if(n == null)
		{
			return;
		}
		
		((ManagerActivity) context).showRenameDialog(n, n.getName());
	}
	
	public void move (String path){
		MegaNode n = megaApi.getNodeByPath(path);
		if(n == null)
		{
			return;
		}
		
		ArrayList<Long> handleList = new ArrayList<Long>();
		handleList.add(n.getHandle());									
		((ManagerActivity) context).showMove(handleList);
	}
	
	public void copy (String path){
		MegaNode n = megaApi.getNodeByPath(path);
		if(n == null)
		{
			return;
		}
		
		ArrayList<Long> handleList = new ArrayList<Long>();
		handleList.add(n.getHandle());									
		((ManagerActivity) context).showCopy(handleList);
	}
	
	public boolean isFolder(String path){
		MegaNode n = megaApi.getNodeByPath(path);
		if(n == null)
		{
			return false;
		}
		
		if(n.isFile()){
			return false;
		}
		else{
			return true;
		}		
	}
	
	private String getInfoFolder(ArrayList<MegaOffline> mOffInfo) {
		log("getInfoFolder");
//		log("primer elemento: "+mOffInfo.get(0).getName());
		
		String info = "";
		int numFolders=0;
		int numFiles=0;
		
		String pathI=null;
		
		if(mOffInfo.size()>0){
			if(mOffInfo.get(0).isIncoming()){
				pathI = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + mOffInfo.get(0).getHandleIncoming() + "/";
			}
			else{
				pathI= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR;
			}	
			
			for(int i=0; i<mOffInfo.size();i++){
				MegaOffline mOff = (MegaOffline) mOffInfo.get(i);
				String path = pathI + mOff.getPath() + mOff.getName();			

				File destination = new File(path);
				if (destination.exists()){
					if(destination.isFile()){
						numFiles++;					
					}
					else{
						numFolders++;					
					}
				}
				else{
					log("File do not exist");
				}		
			}
		}		
		
		if (numFolders > 0) {
			info = numFolders
					+ " "
					+ context.getResources().getQuantityString(
							R.plurals.general_num_folders, numFolders);
			if (numFiles > 0) {
				info = info
						+ ", "
						+ numFiles
						+ " "
						+ context.getResources().getQuantityString(
								R.plurals.general_num_files, numFiles);
			}
		} else {
			info = numFiles
					+ " "
					+ context.getResources().getQuantityString(
							R.plurals.general_num_files, numFiles);
		}

		log(info);
		return info;
	}
		
	@Override
    public void onAttach(Activity activity) {
		log("onAttach");
        super.onAttach(activity);
        context = activity;
        aB = ((ActionBarActivity)activity).getSupportActionBar();
    }
	
	@Override
	public void onClick(View v) {
		log("onClick");
		switch(v.getId()){
		case R.id.offline_out_space_btn:
			log("Click Account Button");
			((ManagerActivity)getActivity()).upgradeAccountButton();
			break;
		}
	}
	
	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		log("onItemClick");
		if (isList){
			log("mode List");
			if (adapterList.isMultipleSelect()){
				log("multiselect");
				SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
				if (checkedItems.get(position, false) == true){
					listView.setItemChecked(position, true);
				}
				else{
					listView.setItemChecked(position, false);
				}				
				updateActionModeTitle();
				adapterList.notifyDataSetChanged();
			}
			else{
				MegaOffline currentNode = mOffList.get(position);
				
				aB.setTitle(currentNode.getName());
				aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
				pathNavigation= currentNode.getPath()+ currentNode.getName()+"/";	
				if (context instanceof ManagerActivity){
					((ManagerActivity)context).getmDrawerToggle().setDrawerIndicatorEnabled(false);
					((ManagerActivity)context).supportInvalidateOptionsMenu();
					((ManagerActivity)context).setPathNavigationOffline(pathNavigation);
				}
				else if (context instanceof OfflineActivity){
					((OfflineActivity)context).setPathNavigationOffline(pathNavigation);
				}
				
				File currentFile=null;
				
				if(currentNode.isIncoming()){
					String handleString = currentNode.getHandleIncoming();
					currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + handleString + "/"+currentNode.getPath() + "/" + currentNode.getName());
				}
				else{
					currentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + currentNode.getPath() + "/" + currentNode.getName());
				}
				
				if(currentFile.exists()&&currentFile.isDirectory()){

					mOffList=dbH.findByPath(currentNode.getPath()+currentNode.getName()+"/");
					if (adapterList.getCount() == 0){
						listView.setVisibility(View.GONE);
						emptyImageView.setVisibility(View.VISIBLE);
						emptyTextView.setVisibility(View.VISIBLE);						
					}
					else{
						File offlineDirectory = null;
						String path;
						
						if(currentNode.isIncoming()){
							path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + currentNode.getHandleIncoming();
						}
						else{							
							path= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR;
							log("Path NOT INCOMING: "+path);							
						}						
												
						for(int i=0; i<mOffList.size();i++){
							
							if (Environment.getExternalStorageDirectory() != null){								
								log("mOffList path: "+path+mOffList.get(i).getPath());
								offlineDirectory = new File(path + mOffList.get(i).getPath()+mOffList.get(i).getName());
							}
							else{
								offlineDirectory = context.getFilesDir();
							}	

							if (!offlineDirectory.exists()){
								//Updating the DB because the file does not exist	
								log("Path to remove C: "+(path + mOffList.get(i).getName()));
								dbH.removeById(mOffList.get(i).getId());
								mOffList.remove(i);
								i--;
							}			
						}
					}

					if (adapterList == null){						
						adapterList = new MegaOfflineListAdapter(this, context, mOffList, listView, emptyImageView, emptyTextView, aB);
					}
					else{						
						adapterList.setNodes(mOffList);
					}
					
					if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
						sortByNameDescending();
					}
					else{
						sortByNameAscending();
					}
					
					contentText.setText(getInfoFolder(mOffList));
					adapterList.setPositionClicked(-1);
					
					notifyDataSetChanged();

				}else{
					if(currentFile.exists()&&currentFile.isFile()){
						//Open it!
						if (MimeTypeList.typeForName(currentFile.getName()).isImage()){
							Intent intent = new Intent(context, FullScreenImageViewer.class);
							intent.putExtra("position", position);
							intent.putExtra("adapterType", ManagerActivity.OFFLINE_ADAPTER);
							intent.putExtra("parentNodeHandle", -1L);
							intent.putExtra("offlinePathDirectory", currentFile.getParent());
							startActivity(intent);
						}
//						else if(currentFile.exists()&&MimeType.typeForName(currentFile.getName()).isPdf()){
//		    			    log("Offline - File PDF");		    			    
//		    			    Intent intentPdf = new Intent();
//		    			    intentPdf.setDataAndType(Uri.fromFile(currentFile), "application/pdf");
//		    			    intentPdf.setClass(context, OpenPDFActivity.class);
//		    			    intentPdf.setAction("android.intent.action.VIEW");
//		    				this.startActivity(intentPdf);						
//							
//						}
						else{
							Intent viewIntent = new Intent(Intent.ACTION_VIEW);
							viewIntent.setDataAndType(Uri.fromFile(currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
							if (ManagerActivity.isIntentAvailable(context, viewIntent)){
								context.startActivity(viewIntent);
							}
							else{
								Intent intentShare = new Intent(Intent.ACTION_SEND);
								intentShare.setDataAndType(Uri.fromFile(currentFile), MimeTypeList.typeForName(currentFile.getName()).getType());
								if (ManagerActivity.isIntentAvailable(context, intentShare)){
									context.startActivity(intentShare);
								}
							}
						}
						
					}					
				}

				
				
				
//				String currentPath = paths.get(position);
//				File currentFile = new File (currentPath);
				
//				if (MimeType.typeForName(currentFile.getName()).isImage()){
//					Intent intent = new Intent(context, FullScreenImageViewer.class);
//					intent.putExtra("position", position);
//					intent.putExtra("adapterType", ManagerActivity.OFFLINE_ADAPTER);
//					intent.putExtra("parentNodeHandle", -1L);
//					startActivity(intent);
//				}
//				else{
//					Intent viewIntent = new Intent(Intent.ACTION_VIEW);
//					viewIntent.setDataAndType(Uri.fromFile(currentFile), MimeType.typeForName(currentFile.getName()).getType());
//					if (ManagerActivity.isIntentAvailable(context, viewIntent)){
//						context.startActivity(viewIntent);
//					}
//					else{
//						Intent intentShare = new Intent(Intent.ACTION_SEND);
//						intentShare.setDataAndType(Uri.fromFile(currentFile), MimeType.typeForName(currentFile.getName()).getType());
//						if (ManagerActivity.isIntentAvailable(context, intentShare)){
//							context.startActivity(intentShare);
//						}
//					}
//				}
			}
		}
    }	
		
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		log("onItemLongClick");
		if (adapterList.getPositionClicked() == -1){
			clearSelections();
			actionMode = ((ActionBarActivity)context).startSupportActionMode(new ActionBarCallBack());
			listView.setItemChecked(position, true);
			adapterList.setMultipleSelect(true);
			updateActionModeTitle();
			listView.setOnItemLongClickListener(null);
		}
		return true;
	}
	
	/*
	 * Clear all selected items
	 */
	private void clearSelections() {
		log("clearSelections");
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
		log("updateActionModeTitle");
		if (actionMode == null || getActivity() == null) {
			return;
		}
		List<MegaOffline> documents = getSelectedDocuments();
		int files = 0;
		int folders = 0;
		for (MegaOffline document : documents) {
			File f = new File(document.getName());
			if (f.isFile()) {
				files++;
			} 
			else {
				folders++;
			}
		}
		Resources res = getActivity().getResources();
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
	private List<MegaOffline> getSelectedDocuments() {
		log("getSelectedDocuments");
		ArrayList<MegaOffline> documents = new ArrayList<MegaOffline>();
		
		SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
		for (int i = 0; i < checkedItems.size(); i++) {
			if (checkedItems.valueAt(i) == true) {
				
				MegaOffline currentNode = mOffList.get(checkedItems.keyAt(i));
				if (currentNode != null){
					documents.add(currentNode);
				}
			}
		}
		return documents;
	}
	
	/*
	 * Disable selection
	 */
	void hideMultipleSelect() {
		log("hideMultipleSelect");
		adapterList.setMultipleSelect(false);
		if (actionMode != null) {
			actionMode.finish();
		}
	}
	
	public int onBackPressed(){
		log("onBackPressed");
		if (isList){
			if (adapterList == null){
				return 0;
			}
			
			if (adapterList.isMultipleSelect()){
				hideMultipleSelect();
				return 2;
			}
			
			if (adapterList.getPositionClicked() != -1){
				adapterList.setPositionClicked(-1);
				adapterList.notifyDataSetChanged();
				return 1;
			}
			else if(pathNavigation != null){
				if(pathNavigation.isEmpty()){
					return 0;
				}
				if (!pathNavigation.equals("/")){
					log("onBackPress: "+pathNavigation);
					pathNavigation=pathNavigation.substring(0,pathNavigation.length()-1);
					log("substring: "+pathNavigation);
					int index=pathNavigation.lastIndexOf("/");				
					pathNavigation=pathNavigation.substring(0,index+1);
					
					if (context instanceof ManagerActivity){
						((ManagerActivity)context).setPathNavigationOffline(pathNavigation);
						
						if (pathNavigation.equals("/")){
							aB.setTitle(getString(R.string.section_saved_for_offline));
							aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
							((ManagerActivity)context).getmDrawerToggle().setDrawerIndicatorEnabled(true);
							((ManagerActivity)context).supportInvalidateOptionsMenu();
						}
						else{
							String title = pathNavigation;
							index=title.lastIndexOf("/");				
							title=title.substring(0,index);
							index=title.lastIndexOf("/");				
							title=title.substring(index+1,title.length());			
							aB.setTitle(title);
							aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
							((ManagerActivity)context).getmDrawerToggle().setDrawerIndicatorEnabled(false);
							((ManagerActivity)context).supportInvalidateOptionsMenu();
						}
					}
					else if (context instanceof OfflineActivity){
						((OfflineActivity)context).setPathNavigationOffline(pathNavigation);
						
						if (pathNavigation.equals("/")){
							aB.setTitle(getString(R.string.section_saved_for_offline));
							aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
							((OfflineActivity)context).supportInvalidateOptionsMenu();
						}
						else{
							
							String title = pathNavigation;
							index=title.lastIndexOf("/");				
							title=title.substring(0,index);
							index=title.lastIndexOf("/");				
							title=title.substring(index+1,title.length());			
							aB.setTitle(title);	
							aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
							((OfflineActivity)context).supportInvalidateOptionsMenu();
						}
					}
					ArrayList<MegaOffline> mOffListNavigation= new ArrayList<MegaOffline>();				
					mOffListNavigation=dbH.findByPath(pathNavigation);
					
					contentText.setText(getInfoFolder(mOffListNavigation));
					this.setNodes(mOffListNavigation);
					
					if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
						sortByNameDescending();
					}
					else{
						sortByNameAscending();
					}
					//adapterList.setNodes(mOffList);
					
					return 2;
				}
				else{
					log("pathNavigation  / ");
					return 0;
				}
			}
			else{
					return 0;
			}
		}		
		else{
		
			if (adapterGrid.getPositionClicked() != -1){
				adapterGrid.setPositionClicked(-1);
				adapterGrid.notifyDataSetChanged();
				return 1;
			}
			else if(pathNavigation != null){
				if (!pathNavigation.equals("/")){
					//TODO En caso de que no esté en el raíz del offline, pues navegar para atrás.
		
					// Esto es, poner el nuevo path y adapterList.setNodes() y adapterList.notifyDataSetChanged();
					((ManagerActivity)context).getmDrawerToggle().setDrawerIndicatorEnabled(true);
					
					pathNavigation=pathNavigation.substring(0,pathNavigation.length()-1);
					int index=pathNavigation.lastIndexOf("/");				
					pathNavigation=pathNavigation.substring(0,index+1);
					if (context instanceof ManagerActivity){
						((ManagerActivity)context).setPathNavigationOffline(pathNavigation);
					}
					else if (context instanceof OfflineActivity){
						((OfflineActivity)context).setPathNavigationOffline(pathNavigation);
					}
					
					ArrayList<MegaOffline> mOffListNavigation= new ArrayList<MegaOffline>();				
					mOffListNavigation=dbH.findByPath(pathNavigation);
					this.setNodes(mOffListNavigation);
					
					if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
						sortByNameDescending();
					}
					else{
						sortByNameAscending();
					}
					//adapterGrid.setNodes(mOffList);
					//adapterGrid.setPathNavigation?
					
					return 2;
				}
				else{
					return 0;
				}
			}
			else{
					return 0;
			}
					
//			parentHandle = adapterGrid.getParentHandle();
//			((ManagerActivity)context).setParentHandleBrowser(parentHandle);
//			
//			if (adapterGrid.getPositionClicked() != -1){
//				adapterGrid.setPositionClicked(-1);
//				adapterGrid.notifyDataSetChanged();
//				return 1;
//			}
//			else{
//				MegaNode parentNode = megaApi.getParentNode(megaApi.getNodeByHandle(parentHandle));
//				if (parentNode != null){
//					listView.setVisibility(View.VISIBLE);
//					emptyImageView.setVisibility(View.GONE);
//					emptyTextView.setVisibility(View.GONE);
//					if (parentNode.getHandle() == megaApi.getRootNode().getHandle()){
//						aB.setTitle(getString(R.string.section_cloud_drive));	
//						((ManagerActivity)context).getmDrawerToggle().setDrawerIndicatorEnabled(true);
//					}
//					else{
//						aB.setTitle(parentNode.getName());					
//						((ManagerActivity)context).getmDrawerToggle().setDrawerIndicatorEnabled(false);
//					}
//					
//					((ManagerActivity)context).supportInvalidateOptionsMenu();
//					
//					parentHandle = parentNode.getHandle();
//					((ManagerActivity)context).setParentHandleBrowser(parentHandle);
//					nodes = megaApi.getChildren(parentNode, orderGetChildren);
//					adapterGrid.setNodes(nodes);
//					listView.setSelection(0);
//					adapterGrid.setParentHandle(parentHandle);
//					return 2;
//				}
//				else{
//					return 0;
//				}
//			}
		}
	}
	
	public ListView getListView(){
		log("getListView");
		return listView;
	}
	
	public void setNodes(ArrayList<MegaOffline> _mOff){
		log("setNodes");
		this.mOffList = _mOff;

		if (isList){
			if (adapterList != null){
				adapterList.setNodes(mOffList);
				if (adapterList.getCount() == 0){
					listView.setVisibility(View.GONE);
					emptyImageView.setVisibility(View.VISIBLE);
					emptyTextView.setVisibility(View.VISIBLE);
					emptyImageView.setImageResource(R.drawable.ic_empty_offline);
					emptyTextView.setText(R.string.file_browser_empty_folder);
				}
				else{
					listView.setVisibility(View.VISIBLE);
					emptyImageView.setVisibility(View.GONE);
					emptyTextView.setVisibility(View.GONE);
				}			
			}	
		}
//		else{
//			if (adapterGrid != null){
//				adapterGrid.setPaths(paths);
//				if (adapterGrid.getCount() == 0){
//					listView.setVisibility(View.GONE);
//					emptyImageView.setVisibility(View.VISIBLE);
//					emptyTextView.setVisibility(View.VISIBLE);
//					emptyImageView.setImageResource(R.drawable.ic_empty_offline);
//					emptyTextView.setText(R.string.file_browser_empty_folder);
//				}
//				else{
//					listView.setVisibility(View.VISIBLE);
//					emptyImageView.setVisibility(View.GONE);
//					emptyTextView.setVisibility(View.GONE);
//				}			
//			}
//		}
	}
	
	public void setPositionClicked(int positionClicked){
		log("setPositionClicked");
		if (isList){
			if (adapterList != null){
				adapterList.setPositionClicked(positionClicked);
			}
		}
		else{
			if (adapterGrid != null){
				adapterGrid.setPositionClicked(positionClicked);
			}	
		}		
	}
	
	public void notifyDataSetChanged(){
		log("notifyDataSetChanged");
		if (isList){
			if (adapterList != null){
				adapterList.notifyDataSetChanged();
			}
		}
		else{
			if (adapterGrid != null){
				adapterGrid.notifyDataSetChanged();
			}
		}
	}
	
	public void refreshPaths(){
		log("refreshPaths()");
		mOffList=dbH.findByPath("/");	
		
		if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}
		
//		setNodes(mOffList);
		listView.invalidateViews();
	}
	
	public void refreshPaths(MegaOffline mOff){
		log("refreshPaths(MegaOffline mOff");
		int index=0;
		MegaOffline retFindPath = null;
		
		//Find in the tree, the last existing node
		String pNav= mOff.getPath();
		
		if(mOff.getType()==DB_FILE){
			
			index=pNav.lastIndexOf("/");				
			pNav=pNav.substring(0,index+1);
			
		}
		else{
			pNav=pNav.substring(0,pNav.length()-1);
		}	
			
		if(pNav.length()==0){
			mOffList=dbH.findByPath("/");
		}
		else{
			retFindPath=findPath(pNav);	
			mOffList=dbH.findByPath(retFindPath.getPath()+retFindPath.getName()+"/");
		}
				
		if(orderGetChildren == MegaApiJava.ORDER_DEFAULT_DESC){
			sortByNameDescending();
		}
		else{
			sortByNameAscending();
		}
		
//		setNodes(mOffList);
		pathNavigation=pNav;
		listView.invalidateViews();
	}
	
	private MegaOffline findPath (String pNav){
		
		log("Path Navigation" + pNav);
		
		MegaOffline nodeToShow = null;
		int index=pNav.lastIndexOf("/");	
		String pathToShow = pNav.substring(0, index+1);
		log("Path: "+ pathToShow);
		String nameToShow = pNav.substring(index+1, pNav.length());
		log("Name: "+ nameToShow);
		
		nodeToShow = dbH.findbyPathAndName(pathToShow, nameToShow);
		if(nodeToShow!=null){
			//Show the node
			log("NOde: "+ nodeToShow.getName());
			
			return nodeToShow;
		}
		else{
			findPath (pathToShow);
		}
		
		return nodeToShow;		
	}	
	
	public void setPathNavigation(String _pathNavigation){
		log("setPathNavigation");
		this.pathNavigation = _pathNavigation;
		if (isList){
			if (adapterList != null){	
				adapterList.setNodes(dbH.findByPath(_pathNavigation));
			}
		}
		else{
			if (adapterGrid != null){
				adapterGrid.setNodes(dbH.findByPath(_pathNavigation));
			}
		}
	}
	
	public void setIsList(boolean isList){
		log("setIsList");
		this.isList = isList;
	}
	
	public boolean getIsList(){
		log("getIsList");
		return isList;
	}
	
	public void setOrder(int orderGetChildren){
		log("setOrder");
		this.orderGetChildren = orderGetChildren;
	}
	
	private static void log(String log) {
		Util.log("OfflineFragment", log);
	}
}
