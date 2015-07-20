package nz.mega.android.lollipop;

import java.io.File;
import java.util.ArrayList;

import nz.mega.android.DatabaseHandler;
import nz.mega.android.MegaApplication;
import nz.mega.android.MegaPreferences;
import nz.mega.android.R;
import nz.mega.android.utils.Util;
import nz.mega.components.SimpleDividerItemDecoration;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaNode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class CloudDriveExplorerFragmentLollipop extends Fragment implements OnClickListener{

	Context context;
	MegaApiAndroid megaApi;
	ArrayList<MegaNode> nodes;
	long parentHandle = -1;
	
	MegaExplorerLollipopAdapter adapter;
	
	int modeCloud;
	boolean selectFile=false;
	MegaPreferences prefs;
	DatabaseHandler dbH;
	
	public String name;
	
//	boolean first = false;
//	private boolean folderSelected = false;
	private Button uploadButton;
	RecyclerView listView;
	RecyclerView.LayoutManager mLayoutManager;
	ImageView emptyImageView;
	TextView emptyTextView;
	TextView contentText;
	LinearLayout outSpaceLayout=null;

	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		log("onCreate");
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		if (megaApi.getRootNode() == null){
			return;
		}
		
		parentHandle = -1;
		dbH = DatabaseHandler.getDbHandler(context);
		
		Bundle bundle = this.getArguments();
		if (bundle != null) {
		    modeCloud = bundle.getInt("MODE", FileExplorerActivityLollipop.COPY);	
		    selectFile = bundle.getBoolean("SELECTFILE", false);
		}
		log("onCreate mode: "+modeCloud);
//		first=true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		log("onCreateView");
				
		View v = inflater.inflate(R.layout.fragment_fileexplorerlist, container, false);		
		
		listView = (RecyclerView) v.findViewById(R.id.file_list_view_browser);
		listView.addItemDecoration(new SimpleDividerItemDecoration(context));
		mLayoutManager = new LinearLayoutManager(context);
		listView.setLayoutManager(mLayoutManager);
		
		contentText = (TextView) v.findViewById(R.id.content_text);
		contentText.setVisibility(View.GONE);
		
		outSpaceLayout = (LinearLayout) v.findViewById(R.id.out_space);
		outSpaceLayout.setVisibility(View.GONE);
	
		uploadButton = (Button) v.findViewById(R.id.file_explorer_button);
		uploadButton.setOnClickListener(this);
		uploadButton.setVisibility(View.VISIBLE);
		
		emptyImageView = (ImageView) v.findViewById(R.id.file_list_empty_image);
		emptyTextView = (TextView) v.findViewById(R.id.file_list_empty_text);
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) listView.getLayoutParams();
		params.addRule(RelativeLayout.ABOVE, R.id.file_explorer_button);
		
		if(modeCloud==FileExplorerActivityLollipop.SELECT_CAMERA_FOLDER){
			parentHandle = -1;
			changeButtonTitle(context.getString(R.string.section_cloud_drive));
		}
		else{
			if (parentHandle == -1)
			{			
				//Find in the database the last parentHandle
				if (dbH == null){
					dbH = DatabaseHandler.getDbHandler(context);
				}
				if (dbH != null){
					prefs = dbH.getPreferences();
					if (prefs != null) {
						String lastFolder = prefs.getLastFolderCloud();
						if(lastFolder != null) {
							if (lastFolder.compareTo("") != 0){
								parentHandle = Long.parseLong(lastFolder);
							}
						}
					}
				}
			}
		}		
		
		MegaNode chosenNode = megaApi.getNodeByHandle(parentHandle);
		if(chosenNode == null)
		{
			parentHandle = megaApi.getRootNode().getHandle();
			nodes = megaApi.getChildren(megaApi.getRootNode());
			changeButtonTitle(context.getString(R.string.section_cloud_drive));
			changeActionBarTitle(context.getString(R.string.section_cloud_drive));
			changeBackVisibility(false);
		}
		else
		{
			nodes = megaApi.getChildren(chosenNode);
			if(chosenNode.getType() != MegaNode.TYPE_ROOT)
			{
				changeButtonTitle(chosenNode.getName());
				changeActionBarTitle(chosenNode.getName());	
				changeBackVisibility(true);
			}
			else
			{
				changeButtonTitle(context.getString(R.string.section_cloud_drive));
				changeActionBarTitle(context.getString(R.string.section_cloud_drive));
				changeBackVisibility(false);
			}
		}
		
		if (context instanceof FileExplorerActivityLollipop){
			((FileExplorerActivityLollipop)context).setParentHandle(parentHandle);
		}
		
//		if (modeCloud == FileExplorerActivityLollipop.MOVE) {
//			uploadButton.setText(getString(R.string.general_move_to) + " " + actionBarTitle );
//		}
//		else if (modeCloud == FileExplorerActivityLollipop.COPY){
//			uploadButton.setText(getString(R.string.general_copy_to) + " " + actionBarTitle );
//		}
//		else if (modeCloud == FileExplorerActivityLollipop.UPLOAD){
//			uploadButton.setText(getString(R.string.action_upload));
//		}
//		else if (modeCloud == FileExplorerActivityLollipop.IMPORT){
//			uploadButton.setText(getString(R.string.general_import_to) + " " + actionBarTitle );
//		}
//		else if (modeCloud == FileExplorerActivityLollipop.SELECT){
//			uploadButton.setText(getString(R.string.general_select) + " " + actionBarTitle );
//		}
//		else if(modeCloud == FileExplorerActivityLollipop.UPLOAD_SELFIE){
//			uploadButton.setText(getString(R.string.action_upload) + " " + actionBarTitle );
//		}	
//				
		if (adapter == null){
			adapter = new MegaExplorerLollipopAdapter(context, nodes, parentHandle, listView, emptyImageView, emptyTextView, selectFile);			
			
			adapter.SetOnItemClickListener(new MegaExplorerLollipopAdapter.OnItemClickListener() {
				
				@Override
				public void onItemClick(View view, int position) {
					itemClick(view, position);
				}
			});
		}
		else{
			adapter.setParentHandle(parentHandle);
			adapter.setNodes(nodes);
			adapter.setSelectFile(selectFile);
		}
		
		if(selectFile)
		{
			uploadButton.setVisibility(View.GONE);
		}
		
		adapter.setPositionClicked(-1);		
		
		listView.setAdapter(adapter);		
		
		return v;
	}
	
//	public void setMode(int mode){
//		log("setMode: "+mode);
//		modeCloud=mode;
//		log("setMode: "+modeCloud);
//	}	
	
	public void changeButtonTitle(String folder){
		log("changeButtonTitle "+folder);
//		windowTitle.setText(folder);
		
		if (modeCloud == FileExplorerActivityLollipop.MOVE) {
			uploadButton.setText(getString(R.string.general_move_to) + " " + folder);
		}
		else if (modeCloud == FileExplorerActivityLollipop.COPY){
			uploadButton.setText(getString(R.string.general_copy_to) + " " + folder);
		}
		else if (modeCloud == FileExplorerActivityLollipop.UPLOAD){
			uploadButton.setText(getString(R.string.action_upload));
		}
		else if (modeCloud == FileExplorerActivityLollipop.IMPORT){
			uploadButton.setText(getString(R.string.general_import_to) + " " + folder);
		}
		else if (modeCloud == FileExplorerActivityLollipop.SELECT || modeCloud == FileExplorerActivityLollipop.SELECT_CAMERA_FOLDER){
			uploadButton.setText(getString(R.string.general_select) + " " + folder);
		}
		else if(modeCloud == FileExplorerActivityLollipop.UPLOAD_SELFIE){
			uploadButton.setText(getString(R.string.action_upload) + " " + folder );
		}	
		else {
			uploadButton.setText(getString(R.string.general_select) + " " + folder);
		}
		
		
	}
	
	public void changeActionBarTitle(String folder){
		((FileExplorerActivityLollipop) context).changeTitle(folder);
	}
	
	public void changeBackVisibility(boolean backVisibility){
		((FileExplorerActivityLollipop) context).changeBackVisibility(backVisibility);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.file_explorer_button:{				
				dbH.setLastCloudFolder(Long.toString(parentHandle));
				((FileExplorerActivityLollipop) context).buttonClick(parentHandle);
			}
		}
	}

    public void itemClick(View view, int position) {
		log("itemClick");
		
		if (nodes.get(position).isFolder()){
					
			MegaNode n = nodes.get(position);			
			
			String path=n.getName();	
			String[] temp;
			temp = path.split("/");
			name = temp[temp.length-1];

			if(n.getType() != MegaNode.TYPE_ROOT)
			{
				changeButtonTitle(name);
				changeActionBarTitle(name);
				changeBackVisibility(true);
			}
			else
			{
				changeButtonTitle(context.getString(R.string.section_cloud_drive));
				changeActionBarTitle(context.getString(R.string.section_cloud_drive));
				changeBackVisibility(false);
			}
			
			parentHandle = nodes.get(position).getHandle();
			if (context instanceof FileExplorerActivityLollipop){
				((FileExplorerActivityLollipop)context).setParentHandle(parentHandle);
			}
			adapter.setParentHandle(parentHandle);
			nodes = megaApi.getChildren(nodes.get(position));
			adapter.setNodes(nodes);
			listView.scrollToPosition(0);
			
			//If folder has no files
			if (adapter.getItemCount() == 0){
				listView.setVisibility(View.GONE);
				emptyImageView.setVisibility(View.VISIBLE);
				emptyTextView.setVisibility(View.VISIBLE);
				if (megaApi.getRootNode().getHandle()==n.getHandle()) {
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
		else
		{
			//Is file
			if(selectFile)
			{
				//Send file
				MegaNode n = nodes.get(position);
				log("Selected node to send: "+n.getName());
				if(nodes.get(position).isFile()){
					MegaNode nFile = nodes.get(position);
					((FileExplorerActivityLollipop) context).buttonClick(nFile.getHandle());
				}
				
			}
		}
	}	

	public int onBackPressed(){
		log("onBackPressed");
		
		parentHandle = adapter.getParentHandle();
		
		MegaNode parentNode = megaApi.getParentNode(megaApi.getNodeByHandle(parentHandle));
		if (parentNode != null){
			
			if(parentNode.getType()==MegaNode.TYPE_ROOT){
				parentHandle=-1;
				changeButtonTitle(context.getString(R.string.section_cloud_drive));
				changeActionBarTitle(context.getString(R.string.section_cloud_drive));
				changeBackVisibility(false);
			}
			else{
				String path=parentNode.getName();	
				String[] temp;
				temp = path.split("/");
				name = temp[temp.length-1];

				changeButtonTitle(name);
				changeActionBarTitle(name);
				changeBackVisibility(true);
				
				parentHandle = parentNode.getHandle();
			}
			
			listView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
			
			
			nodes = megaApi.getChildren(parentNode);
			adapter.setNodes(nodes);
			listView.scrollToPosition(0);
			adapter.setParentHandle(parentHandle);
			if (context instanceof FileExplorerActivityLollipop){
				((FileExplorerActivityLollipop)context).setParentHandle(parentHandle);
			}
			
			return 2;
		}
		else{
			return 0;
		}
	}
	
	/*
	 * Disable nodes from the list
	 */
	public void setDisableNodes(ArrayList<Long> disabledNodes) {
		adapter.setDisableNodes(disabledNodes);
	}
	
	private static void log(String log) {
		Util.log("CloudDriveExplorerFragment", log);
	}
	
	public long getParentHandle(){
		return adapter.getParentHandle();
	}
	
	public void setParentHandle(long parentHandle){
		this.parentHandle = parentHandle;
		if (adapter != null){
			adapter.setParentHandle(parentHandle);
		}
		
		if (context instanceof FileExplorerActivityLollipop){
			((FileExplorerActivityLollipop)context).setParentHandle(parentHandle);
		}
	}
	
	public void setNodes(ArrayList<MegaNode> nodes){
		this.nodes = nodes;
		if (adapter != null){
			adapter.setNodes(nodes);
			if (adapter.getItemCount() == 0){
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
	
	public RecyclerView getListView(){
		return listView;
	}
}
