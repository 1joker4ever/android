package mega.privacy.android.app.lollipop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.Util;


public class MegaOfflineLollipopAdapter extends RecyclerView.Adapter<MegaOfflineLollipopAdapter.ViewHolderOffline> implements OnClickListener {
	
	public static final int ITEM_VIEW_TYPE_LIST = 0;
	public static final int ITEM_VIEW_TYPE_GRID = 1;
	
	Context context;
 
	int positionClicked;
	public static String DB_FILE = "0";
	public static String DB_FOLDER = "1";
	public DatabaseHandler dbH;

	ArrayList<MegaOffline> mOffList = new ArrayList<MegaOffline>();	
	
	int adapterType;
	
	RecyclerView listFragment;
	ImageView emptyImageViewFragment;
	TextView emptyTextViewFragment;
	ActionBar aB;
	SparseBooleanArray selectedItems;
	OfflineFragmentLollipop fragment;
	//ArrayList<MegaOffline> mOffList;
	
	boolean multipleSelect;
	
	/*public static view holder class*/
    public class ViewHolderOffline extends RecyclerView.ViewHolder{
        public ViewHolderOffline(View v) {
			super(v);
			// TODO Auto-generated constructor stub
		}
		ImageView imageView;
        TextView textViewFileName;
        TextView textViewFileSize;
        ImageButton imageButtonThreeDots;
        RelativeLayout itemLayout;
        
        int currentPosition;
        String currentPath;
        String currentHandle;
    }
    
    public class ViewHolderOfflineList extends ViewHolderOffline{
    	public ViewHolderOfflineList (View v){
    		super(v);
    	}
    }
    
    public class ViewHolderOfflineGrid extends ViewHolderOffline{
    	public ViewHolderOfflineGrid (View v){
    		super(v);
    	}
    	
    	public View separator;
    }
    
    private class OfflineThumbnailAsyncTask extends AsyncTask<String, Void, Bitmap>{

    	ViewHolderOffline holder;
    	String currentPath;
    	
    	public OfflineThumbnailAsyncTask(ViewHolderOffline holder) {
    		log("OfflineThumbnailAsyncTask::OfflineThumbnailAsyncTask");
			this.holder = holder;
		}
    	
		@Override
		protected Bitmap doInBackground(String... params) {
			log("OfflineThumbnailAsyncTask::doInBackground");
			currentPath = params[0];
			File currentFile = new File(currentPath);
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap thumb = BitmapFactory.decodeFile(currentFile.getAbsolutePath(), options);
			
			ExifInterface exif;
			int orientation = ExifInterface.ORIENTATION_NORMAL;
			try {
				exif = new ExifInterface(currentFile.getAbsolutePath());
				orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
			} catch (IOException e) {}  
			
			// Calculate inSampleSize
		    options.inSampleSize = Util.calculateInSampleSize(options, 270, 270);
		    
		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    
		    thumb = BitmapFactory.decodeFile(currentFile.getAbsolutePath(), options);
			if (thumb != null){
				thumb = Util.rotateBitmap(thumb, orientation);
				long handle = Long.parseLong(holder.currentHandle);
				ThumbnailUtils.setThumbnailCache(handle, thumb);
				return thumb;
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap thumb){
			log("OfflineThumbnailAsyncTask::onPostExecute");
			if (thumb != null){
				if (holder.currentPath.compareTo(currentPath) == 0){
					RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
					params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
					params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
					params1.setMargins(48, 0, 12, 0);
					holder.imageView.setLayoutParams(params1);
					holder.imageView.setImageBitmap(thumb);
					Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
					holder.imageView.startAnimation(fadeInAnimation);
				}
			}
		}    	
    }
    
    public void toggleSelection(int pos) {
		log("toggleSelection");
		
		//Check if it's the Master Key file
		MegaOffline currentNode = (MegaOffline) getItem(pos);
        if(currentNode.getHandle().equals("0")){
        	String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
			File file= new File(path);
			if(file.exists()){
				notifyItemChanged(pos);
				return;
			}
        }
		
		if (selectedItems.get(pos, false)) {
			log("delete pos: "+pos);
			selectedItems.delete(pos);
		}
		else {
			log("PUT pos: "+pos);
			selectedItems.put(pos, true);
		}
		notifyItemChanged(pos);
	}
	
	public void selectAll(){
		log("selectAll");
		for (int i= 0; i<this.getItemCount();i++){
			if(!isItemChecked(i)){
				toggleSelection(i);
			}
		}
	}

	public void clearSelections() {
		log("clearSelections");
		if(selectedItems!=null){
			selectedItems.clear();
		}
		notifyDataSetChanged();
	}
	
	private boolean isItemChecked(int position) {
        return selectedItems.get(position);
    }

	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	public List<Integer> getSelectedItems() {
		List<Integer> items = new ArrayList<Integer>(selectedItems.size());
		for (int i = 0; i < selectedItems.size(); i++) {
			items.add(selectedItems.keyAt(i));
		}
		return items;
	}	
	
	/*
	 * Get list of all selected nodes
	 */
	public List<MegaOffline> getSelectedOfflineNodes() {
		log("getSelectedOfflineNodes");
		ArrayList<MegaOffline> nodes = new ArrayList<MegaOffline>();
		
		for (int i = 0; i < selectedItems.size(); i++) {
			if (selectedItems.valueAt(i) == true) {
				MegaOffline document = getNodeAt(selectedItems.keyAt(i));
				if (document != null){
					nodes.add(document);
				}
			}
		}
		return nodes;
	}
	
	public MegaOfflineLollipopAdapter(OfflineFragmentLollipop _fragment, Context _context, ArrayList<MegaOffline> _mOffList, RecyclerView listView, ImageView emptyImageView, TextView emptyTextView, ActionBar aB, int adapterType) {
		log("MegaOfflineListAdapter");
		this.fragment = _fragment;
		this.context = _context;
		this.mOffList = _mOffList;
		this.adapterType =  adapterType;

		this.listFragment = listView;
		this.emptyImageViewFragment = emptyImageView;
		this.emptyTextViewFragment = emptyTextView;
		this.aB = aB;
		
		this.positionClicked = -1;
	}
	
	public void setNodes(ArrayList<MegaOffline> mOffList){
		log("setNodes");
		String pathNav = fragment.getPathNavigation();
		if(pathNav!=null){		
			if (pathNav.equals("/")){
				if (mOffList != null){
					if(!mOffList.isEmpty()) {
						MegaOffline lastItem = mOffList.get(mOffList.size()-1);
						if(!(lastItem.getHandle().equals("0"))){
							String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
							log("Export in: "+path);
							File file= new File(path);
							if(file.exists()){
								MegaOffline masterKeyFile = new MegaOffline("0", path, "MEGARecoveryKey.txt", 0, "0", false, "0");
								mOffList.add(masterKeyFile);
							}
						}	
					}
					else{
						String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
						log("Export in: "+path);
						File file= new File(path);
						if(file.exists()){
							MegaOffline masterKeyFile = new MegaOffline("0", path, "MEGARecoveryKey.txt", 0, "0", false, "0");
							mOffList.add(masterKeyFile);
						}
					}
				}						
			}
		}		

		this.mOffList = mOffList;
		
		positionClicked = -1;	
		notifyDataSetChanged();
	}
	
	@Override
	public ViewHolderOffline onCreateViewHolder(ViewGroup parent, int viewType) {
		log("onCreateViewHolder");
		
		listFragment = (RecyclerView) parent;
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
		
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
		if (viewType == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST){
		
			ViewHolderOfflineList holder = null;
			
			View v = inflater.inflate(R.layout.item_offline_list, parent, false);	
				
			holder = new ViewHolderOfflineList(v);
			holder.itemLayout = (RelativeLayout) v.findViewById(R.id.offline_list_item_layout);
			holder.imageView = (ImageView) v.findViewById(R.id.offline_list_thumbnail);
			holder.textViewFileName = (TextView) v.findViewById(R.id.offline_list_filename);
			holder.textViewFileName.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			holder.textViewFileName.getLayoutParams().width = Util.px2dp(210, outMetrics);
			holder.textViewFileSize = (TextView) v.findViewById(R.id.offline_list_filesize);
			holder.imageButtonThreeDots = (ImageButton) v.findViewById(R.id.offline_list_three_dots);
			
			//Right margin
			RelativeLayout.LayoutParams actionButtonParams = (RelativeLayout.LayoutParams)holder.imageButtonThreeDots.getLayoutParams();
			actionButtonParams.setMargins(0, 0, Util.scaleWidthPx(10, outMetrics), 0); 
			holder.imageButtonThreeDots.setLayoutParams(actionButtonParams);
		
			holder.itemLayout.setOnClickListener(this);
			holder.itemLayout.setTag(holder);
			
			v.setTag(holder);
			
			return holder;
		}
		else if (viewType == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID){
			ViewHolderOfflineGrid holder = null;
			
			View v = inflater.inflate(R.layout.item_offline_grid, parent, false);	
			
			holder = new ViewHolderOfflineGrid(v);
			holder.itemLayout = (RelativeLayout) v.findViewById(R.id.offline_grid_item_layout);
			holder.imageView = (ImageView) v.findViewById(R.id.offline_grid_thumbnail);
			holder.textViewFileName = (TextView) v.findViewById(R.id.offline_grid_filename);
			holder.textViewFileSize = (TextView) v.findViewById(R.id.offline_grid_filesize);
			holder.imageButtonThreeDots = (ImageButton) v.findViewById(R.id.offline_grid_three_dots);
			holder.separator = (View) v.findViewById(R.id.offline_grid_separator);
		
			holder.itemLayout.setOnClickListener(this);
			holder.itemLayout.setTag(holder);
			
			v.setTag(holder);
			
			return holder;
		}
		else{
			return null;
		}
	}

	@Override
	public void onBindViewHolder(ViewHolderOffline holder, int position) {
		log("onBindViewHolder");
		if (adapterType == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST){
			ViewHolderOfflineList holderList = (ViewHolderOfflineList) holder;
			onBindViewHolderList(holderList, position);
		}
		else if (adapterType == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID){
			ViewHolderOfflineGrid holderGrid = (ViewHolderOfflineGrid) holder;
			onBindViewHolderGrid(holderGrid, position);
		}
	}
	
	public void onBindViewHolderGrid (ViewHolderOfflineGrid holder, int position){
		log("onBindViewHolderGrid");
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
	    
		holder.currentPosition = position;
		
		if (!multipleSelect) {
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			
			if (positionClicked != -1) {
				if (positionClicked == position) {
					holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid));
					holder.separator.setBackgroundColor(context.getResources().getColor(R.color.new_background_fragment));
					listFragment.smoothScrollToPosition(positionClicked);
				}
				else {
					holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid));
					holder.separator.setBackgroundColor(context.getResources().getColor(R.color.new_background_fragment));
				}
			} 
			else {
				holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid));
				holder.separator.setBackgroundColor(context.getResources().getColor(R.color.new_background_fragment));
			}
		} 
		else {
			holder.imageButtonThreeDots.setVisibility(View.GONE);		

			if(this.isItemChecked(position)){
				holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid_long_click_lollipop));
				holder.separator.setBackgroundColor(Color.WHITE);
			}
			else{
				holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid));
				holder.separator.setBackgroundColor(context.getResources().getColor(R.color.new_background_fragment));
			}
		}
				
		MegaOffline currentNode = (MegaOffline) getItem(position);
		
		if(currentNode.getHandle().equals("0")){
			//The node is the MasterKey File
			holder.currentPosition = position;
			holder.textViewFileName.setText(currentNode.getName());
			
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
			File file= new File(path);
			long nodeSize;
			if(file.exists()){
				nodeSize = file.length();
				holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
			}			
			holder.imageView.setImageResource(MimeTypeList.typeForName(currentNode.getName()).getIconResourceId());
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			return;
		}
		
		String path=null;
		
		if(currentNode.isIncoming()){
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + currentNode.getHandleIncoming() + "/";
		}
		else{
			path= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR;
		}	
		
		File currentFile = null;
		if (Environment.getExternalStorageDirectory() != null){
			currentFile = new File(path + currentNode.getPath()+currentNode.getName());
		}
		else{
			currentFile = context.getFilesDir();
		}
		
		holder.currentPath = currentFile.getAbsolutePath();
		holder.currentHandle = currentNode.getHandle();
		holder.currentPosition = position;
		
		holder.textViewFileName.setText(currentNode.getName());
		
		int folders=0;
		int files=0;
		if (currentFile.isDirectory()){
			
			File[] fList = currentFile.listFiles();
			for (File f : fList){
				
				if (f.isDirectory()){
					folders++;						
				}
				else{
					files++;
				}
			}
			
			String info = "";
			if (folders > 0){
				info = folders +  " " + context.getResources().getQuantityString(R.plurals.general_num_folders, folders);
				if (files > 0){
					info = info + ", " + files + " " + context.getResources().getQuantityString(R.plurals.general_num_files, folders);
				}
			}
			else {
				info = files +  " " + context.getResources().getQuantityString(R.plurals.general_num_files, files);
			}			
					
			holder.textViewFileSize.setText(info);			
		}
		else{
			long nodeSize = currentFile.length();
			holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
		}
		
		holder.imageView.setImageResource(MimeTypeList.typeForName(currentNode.getName()).getIconResourceId());
		if (currentFile.isFile()){
			log("...........................Busco Thumb");
			if (MimeTypeList.typeForName(currentNode.getName()).isImage()){
				Bitmap thumb = null;
								
				if (currentFile.exists()){
					thumb = ThumbnailUtils.getThumbnailFromCache(Long.parseLong(currentNode.getHandle()));
					if (thumb != null){
						holder.imageView.setImageBitmap(thumb);
					}
					else{
						try{
							new OfflineThumbnailAsyncTask(holder).execute(currentFile.getAbsolutePath());
						}
						catch(Exception e){
							//Too many AsyncTasks
						}
					}
				}
			}
		}
		else{
			holder.imageView.setImageResource(R.drawable.ic_folder_list);
		}
		
		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);
	}
	
	public void onBindViewHolderList (ViewHolderOfflineList holder, int position){
		log("onBindViewHolderList");
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
	    
		holder.currentPosition = position;
		
		if (!multipleSelect){
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			
			if (positionClicked != -1){
				if (positionClicked == position){
					
					holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
					listFragment.smoothScrollToPosition(positionClicked);				
				}
				else{
					//				
					holder.itemLayout.setBackgroundColor(Color.WHITE);
				}
			}
			else{
				holder.itemLayout.setBackgroundColor(Color.WHITE);
			}			
		}
		else{
			log("multiselect enabled");
			holder.imageButtonThreeDots.setVisibility(View.GONE);		

			if(this.isItemChecked(position)){
				log("checked");
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
			}
			else{
				log("NO checked");
				holder.itemLayout.setBackgroundColor(Color.WHITE);
			}
		}
				
		MegaOffline currentNode = (MegaOffline) getItem(position);
		
		if(currentNode.getHandle().equals("0")){
			//The node is the MasterKey File
			holder.currentPosition = position;
			holder.textViewFileName.setText(currentNode.getName());
			
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
			File file= new File(path);
			long nodeSize;
			if(file.exists()){
				nodeSize = file.length();
				holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
			}			
			holder.imageView.setImageResource(MimeTypeList.typeForName(currentNode.getName()).getIconResourceId());
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			holder.imageButtonThreeDots.setTag(holder);
			holder.imageButtonThreeDots.setOnClickListener(this);
			return;
		}
		
		String path=null;
		
		if(currentNode.isIncoming()){
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + currentNode.getHandleIncoming() + "/";
		}
		else{
			path= Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR;
		}	
		
		File currentFile = null;
		if (Environment.getExternalStorageDirectory() != null){
			currentFile = new File(path + currentNode.getPath()+currentNode.getName());
		}
		else{
			currentFile = context.getFilesDir();
		}
		
		holder.currentPath = currentFile.getAbsolutePath();
		holder.currentHandle = currentNode.getHandle();
		holder.currentPosition = position;
		
		holder.textViewFileName.setText(currentNode.getName());
		
		int folders=0;
		int files=0;
		if (currentFile.isDirectory()){
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
			params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params.setMargins(36, 0, 0, 0);
			holder.imageView.setLayoutParams(params);
			
			File[] fList = currentFile.listFiles();
			for (File f : fList){
				
				if (f.isDirectory()){
					folders++;						
				}
				else{
					files++;
				}
			}
			
			String info = "";
			if (folders > 0){
				info = folders +  " " + context.getResources().getQuantityString(R.plurals.general_num_folders, folders);
				if (files > 0){
					info = info + ", " + files + " " + context.getResources().getQuantityString(R.plurals.general_num_files, folders);
				}
			}
			else {
				info = files +  " " + context.getResources().getQuantityString(R.plurals.general_num_files, files);
			}			
					
			holder.textViewFileSize.setText(info);			
		}
		else{
			long nodeSize = currentFile.length();
			holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
		}
		
		holder.imageView.setImageResource(MimeTypeList.typeForName(currentNode.getName()).getIconResourceId());
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
		params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
		params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
		params.setMargins(36, 0, 0, 0);
		holder.imageView.setLayoutParams(params);		
		
		if (currentFile.isFile()){
			log("...........................Busco Thumb");
			if (MimeTypeList.typeForName(currentNode.getName()).isImage()){
				Bitmap thumb = null;
																
				if (currentFile.exists()){
					thumb = ThumbnailUtils.getThumbnailFromCache(Long.parseLong(currentNode.getHandle()));
					if (thumb != null){
						RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
						params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						params1.setMargins(48, 0, 12, 0);
						holder.imageView.setLayoutParams(params1);
						holder.imageView.setImageBitmap(thumb);
					}
					else{
						try{
							new OfflineThumbnailAsyncTask(holder).execute(currentFile.getAbsolutePath());
						}
						catch(Exception e){
							//Too many AsyncTasks
						}
					}
				}
			}
		}
		else{
			holder.imageView.setImageResource(R.drawable.ic_folder_list);
			RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
			params2.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params2.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params2.setMargins(36, 0, 0, 0);
			holder.imageView.setLayoutParams(params2);
		}
		
		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);
	}
	
	@Override
	public int getItemCount() {
		log("getItemCount");
		return mOffList.size();
	} 
	
	@Override
	public int getItemViewType(int position) {
		return adapterType;
	}
 
	public Object getItem(int position) {
		return mOffList.get(position);
	}
	
    @Override
    public long getItemId(int position) {
    	log("getItemId");
        return position;
    }    
    
    public int getPositionClicked (){
    	log("getPositionClicked");
    	return positionClicked;
    }
    
    public void setPositionClicked(int p){
    	log("setPositionClicked");
    	positionClicked = p;
		notifyDataSetChanged();
    }
    
    public void setAdapterType(int adapterType){
    	this.adapterType = adapterType;
    }

	@Override
	public void onClick(View v) {
		log("onClick");
		ViewHolderOffline holder = (ViewHolderOffline) v.getTag();
		
		int currentPosition = holder.currentPosition;
		MegaOffline mOff = (MegaOffline) getItem(currentPosition);
		
		switch (v.getId()){
			case R.id.offline_list_item_layout:
			case R.id.offline_grid_item_layout:{
				fragment.itemClick(currentPosition);								
				break;
			}			
			case R.id.offline_list_three_dots:
			case R.id.offline_grid_three_dots:{
				if (positionClicked == -1){
					positionClicked = currentPosition;
					notifyDataSetChanged();
				}
				else{
					if (positionClicked == currentPosition){
						positionClicked = -1;
						notifyDataSetChanged();
					}
					else{
						positionClicked = currentPosition;
						notifyDataSetChanged();
					}
				}

				if (Util.isOnline(context)){
					//With connection
					log("Connection! - ManagerActivity instance!");
					if(context instanceof ManagerActivityLollipop){
						((ManagerActivityLollipop) context).showOptionsPanel(mOff);
					}
				}
				else{
					//No connection
					log("No connection!");
					if(context instanceof OfflineActivityLollipop){
						((OfflineActivityLollipop) context).showOptionsPanel(mOff);
					}
				}

				break;
			}
		}		
	}
	
	/*
	 * Get document at specified position
	 */
	public MegaOffline getNodeAt(int position) {
		try {
			if (mOffList != null) {
				return mOffList.get(position);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	public boolean isMultipleSelect() {
		log("isMultipleSelect");
		return multipleSelect;
	}
	
	public void setMultipleSelect(boolean multipleSelect) {
		log("setMultipleSelect: "+multipleSelect);
		if (this.multipleSelect != multipleSelect) {
			this.multipleSelect = multipleSelect;
			notifyDataSetChanged();
		}
		if(this.multipleSelect)
		{
			selectedItems = new SparseBooleanArray();
		}
	}

	private static void log(String log) {
		Util.log("MegaOfflineLollipopAdapter", log);
	}
}
