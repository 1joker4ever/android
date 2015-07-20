package nz.mega.android.lollipop;

import java.util.ArrayList;

import nz.mega.android.MegaApplication;
import nz.mega.android.R;
import nz.mega.android.utils.Util;
import nz.mega.components.SimpleDividerItemDecoration;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaUser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import android.widget.Toast;


public class IncomingSharesExplorerFragmentLollipop extends Fragment implements OnClickListener{

	Context context;
	MegaApiAndroid megaApi;
	ArrayList<MegaNode> nodes;
	long parentHandle = -1;
	
	MegaExplorerLollipopAdapter adapter;
	
	int modeCloud;
	boolean selectFile;
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
	int deepBrowserTree = 0;

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
		
		nodes = new ArrayList<MegaNode>();
		deepBrowserTree=0;
		parentHandle = -1;
		
		Bundle bundle = this.getArguments();
		if (bundle != null) {
		    modeCloud = bundle.getInt("MODE", FileExplorerActivityLollipop.COPY);
		    selectFile = bundle.getBoolean("SELECTFILE", false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		log("onCreateView");
				
		View v = inflater.inflate(R.layout.fragment_fileexplorerlist, container, false);		
		
		listView = (RecyclerView) v.findViewById(R.id.file_list_view_browser);
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
		
		String actionBarTitle = getString(R.string.title_incoming_shares_explorer);	
		
		if (parentHandle == -1){			
			findNodes();	
			adapter.parentHandle=-1;
			uploadButton.setText(getString(R.string.choose_folder_explorer));
		}
		else{
			adapter.parentHandle=parentHandle;
			MegaNode parentNode = megaApi.getNodeByHandle(parentHandle);
			nodes = megaApi.getChildren(parentNode);
		}	

		if (deepBrowserTree != 0){		
			MegaNode infoNode = megaApi.getNodeByHandle(parentHandle);
		}						
	
		adapter.setPositionClicked(-1);		
		
		listView.setAdapter(adapter);		
		
		return v;
	}
	
	public void findNodes(){
		deepBrowserTree=0;
		ArrayList<MegaUser> contacts = megaApi.getContacts();
		ArrayList<Long> disabledNodes = new ArrayList<Long>();
		nodes.clear();
		for (int i=0;i<contacts.size();i++){			
			ArrayList<MegaNode> nodeContact=megaApi.getInShares(contacts.get(i));
			if(nodeContact!=null){
				if(nodeContact.size()>0){
					nodes.addAll(nodeContact);
				}
			}			
		}
		
		for (int i=0;i<nodes.size();i++){	
			MegaNode folder = nodes.get(i);
			int accessLevel = megaApi.getAccess(folder);
			
			if(selectFile){
				if(accessLevel!=MegaShare.ACCESS_FULL) {
					disabledNodes.add(folder.getHandle());
				}
			}
			else{
				if(accessLevel==MegaShare.ACCESS_READ) {
					disabledNodes.add(folder.getHandle());
				}
			}
		}
		
		this.setDisableNodes(disabledNodes);
		
	}
	
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
		else if (modeCloud == FileExplorerActivityLollipop.SELECT){
			uploadButton.setText(getString(R.string.general_select) + " " + folder);
		}
		else if(modeCloud == FileExplorerActivityLollipop.UPLOAD_SELFIE){
			uploadButton.setText(getString(R.string.action_upload) + " " + folder );
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
				((FileExplorerActivityLollipop) context).buttonClick(parentHandle);
			}
		}
	}

    public void itemClick(View view, int position) {
		log("------------------itemClick: "+deepBrowserTree);
		
		
		if (nodes.get(position).isFolder()){
					
			deepBrowserTree = deepBrowserTree+1;
			
			MegaNode n = nodes.get(position);			
			
			String path=n.getName();	
			String[] temp;
			temp = path.split("/");
			name = temp[temp.length-1];

			changeButtonTitle(name);
			changeActionBarTitle(name);
			changeBackVisibility(true);
			
			parentHandle = nodes.get(position).getHandle();
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
				//Seleccionar el fichero para enviar...
				MegaNode n = nodes.get(position);
				log("Selected node to send: "+n.getName());
				if(nodes.get(position).isFile()){
					MegaNode nFile = nodes.get(position);
					
					MegaNode parentFile = megaApi.getParentNode(nFile);
					if(megaApi.getAccess(parentFile)==MegaShare.ACCESS_FULL)
					{
						((FileExplorerActivityLollipop) context).buttonClick(nFile.getHandle());
					}
					else{
						Toast.makeText(context, getString(R.string.context_send_no_permission), Toast.LENGTH_LONG).show();
					}					
				}		
			}
		}
	}	

	public int onBackPressed(){
		log("deepBrowserTree "+deepBrowserTree);
		deepBrowserTree = deepBrowserTree-1;
		
		if(deepBrowserTree==0){
			parentHandle=-1;
			changeActionBarTitle(getString(R.string.title_incoming_shares_explorer));
			changeBackVisibility(false);
			uploadButton.setText(getString(R.string.choose_folder_explorer));
			findNodes();
			
			adapter.setNodes(nodes);
			listView.scrollToPosition(0);
			adapter.setParentHandle(parentHandle);

//			adapterList.setNodes(nodes);
			listView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);

			return 3;
		}
		else if (deepBrowserTree>0){
			parentHandle = adapter.getParentHandle();
			//((ManagerActivity)context).setParentHandleSharedWithMe(parentHandle);			
			
			MegaNode parentNode = megaApi.getParentNode(megaApi.getNodeByHandle(parentHandle));				

			if (parentNode != null){
				listView.setVisibility(View.VISIBLE);
				emptyImageView.setVisibility(View.GONE);
				emptyTextView.setVisibility(View.GONE);

				changeActionBarTitle(parentNode.getName());	
				changeBackVisibility(true);
				
				parentHandle = parentNode.getHandle();
				nodes = megaApi.getChildren(parentNode);

				adapter.setNodes(nodes);
				listView.scrollToPosition(0);
				adapter.setParentHandle(parentHandle);
				return 2;
			}	
			return 2;
		}
		else{
			listView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
			deepBrowserTree=0;
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
		Util.log("IncomingSharesExplorerFragment", log);
	}
	
	public long getParentHandle(){
		return adapter.getParentHandle();
	}
	
	public void setParentHandle(long parentHandle){
		this.parentHandle = parentHandle;
		if (adapter != null){
			adapter.setParentHandle(parentHandle);
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

	public int getDeepBrowserTree() {
		return deepBrowserTree;
	}

	public void setDeepBrowserTree(int deepBrowserTree) {
		this.deepBrowserTree = deepBrowserTree;
	}
}
