package mega.privacy.android.app.lollipop.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.MimeTypeThumbnail;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.NewGridRecyclerView;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.managerSections.OfflineFragmentLollipop;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;


public class MegaOfflineLollipopAdapter extends RecyclerView.Adapter<MegaOfflineLollipopAdapter.ViewHolderOffline> implements OnClickListener, View.OnLongClickListener {
	
	public static final int ITEM_VIEW_TYPE_LIST = 0;
	public static final int ITEM_VIEW_TYPE_GRID = 1;
	
	Context context;
 
	int positionClicked;
	public static String DB_FILE = "0";
	public static String DB_FOLDER = "1";
	public DatabaseHandler dbH;

	private ArrayList<MegaOffline> mOffList = new ArrayList<MegaOffline>();
	public int folderCount = 0;
    private int placeholderCount;
	
//	int getAdapterType();
	
	RecyclerView listFragment;
	ImageView emptyImageViewFragment;
	LinearLayout emptyTextViewFragment;
	ActionBar aB;
	SparseBooleanArray selectedItems;
	OfflineFragmentLollipop fragment;

	boolean multipleSelect;
	
	/*public static view holder class*/
    public class ViewHolderOffline extends RecyclerView.ViewHolder{
        public ViewHolderOffline(View v) {
			super(v);
		}
		ImageView imageView;
        ImageView iconView;
        TextView textViewFileName;
        TextView textViewFileSize;
        RelativeLayout itemLayout;
        
        int currentPosition;
        String currentPath;
        String currentHandle;
		RelativeLayout thumbLayout;

	}
	
	private int getAdapterType() {
		return ((ManagerActivityLollipop)context).isList ? MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST : MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID;
	}
    
    public class ViewHolderOfflineList extends ViewHolderOffline{
    	public ViewHolderOfflineList (View v){
    		super(v);
    	}
		RelativeLayout threeDotsLayout;

	}
    
    public class ViewHolderOfflineGrid extends ViewHolderOffline{
    	public ViewHolderOfflineGrid (View v){
    		super(v);
    	}
		ImageButton imageButtonThreeDots;
    	public View separator;
    	
		public View folderLayout;
		public View fileLayout;
		public ImageView imageViewIcon;
		public RelativeLayout thumbLayoutForFile;
		public ImageView fileGridIconForFile;
		public ImageButton imageButtonThreeDotsForFile;
		public TextView textViewFileNameForFile;
		public ImageView fileGridSelected;
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
					if (getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST){
						RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
						params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
						params1.setMargins(left, 0, 0, 0);

						holder.imageView.setLayoutParams(params1);

					}else if(getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID){
						holder.iconView.setVisibility(View.GONE);
						holder.imageView.setVisibility(View.VISIBLE);
						thumb = ThumbnailUtilsLollipop.getRoundedRectBitmap(context, thumb, 3);
					}
					holder.imageView.setImageBitmap(thumb);
					Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
					holder.imageView.startAnimation(fadeInAnimation);

				}
			}
		}    	
    }
    
    public void toggleSelection(int pos) {
		log("toggleSelection");
        //Otherwise out of bounds exception happens.
        boolean a = pos >= folderCount;
        boolean b = getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID;
        boolean c = placeholderCount != 0;
        if (a && b && c) {
            pos += placeholderCount;
        }
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

		if (getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST){
			MegaOfflineLollipopAdapter.ViewHolderOfflineList view = (MegaOfflineLollipopAdapter.ViewHolderOfflineList) listFragment.findViewHolderForLayoutPosition(pos);
			if(view!=null){
				log("Start animation: "+pos);
				Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
				flipAnimation.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						if (selectedItems.size() <= 0){
							((OfflineFragmentLollipop) fragment).hideMultipleSelect();
						}
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}
				});
				view.imageView.startAnimation(flipAnimation);
			}
		}
		else{
            log("adapter type is GRID");
            MegaOfflineLollipopAdapter.ViewHolderOfflineGrid view = (MegaOfflineLollipopAdapter.ViewHolderOfflineGrid) listFragment.findViewHolderForLayoutPosition(pos);
			if(view != null) {
                if (view.folderLayout.getVisibility() == View.VISIBLE) {
                    Animation flipAnimation = AnimationUtils.loadAnimation(context,R.anim.multiselect_flip);
                    flipAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
            
                        }
        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (selectedItems.size() <= 0){
                               fragment.hideMultipleSelect();
                            }
                        }
        
                        @Override
                        public void onAnimationRepeat(Animation animation) {
            
                        }
                    });
                    view.imageViewIcon.startAnimation(flipAnimation);
                }else{
                    if (selectedItems.size() <= 0){
                        fragment.hideMultipleSelect();
                    }
                }
            }
		}
	}

	public void toggleAllSelection(int pos) {
        final int positionToflip = pos;
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

		if (getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST){
			MegaOfflineLollipopAdapter.ViewHolderOfflineList view = (MegaOfflineLollipopAdapter.ViewHolderOfflineList) listFragment.findViewHolderForLayoutPosition(pos);
			if(view!=null){
				log("Start animation: "+pos);
				Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
				flipAnimation.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						if (selectedItems.size() <= 0){
							((OfflineFragmentLollipop) fragment).hideMultipleSelect();
						}
						notifyItemChanged(positionToflip);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}
				});
				view.imageView.startAnimation(flipAnimation);
			}
			else{
				log("NULL view pos: "+positionToflip);
				notifyItemChanged(pos);
			}
		}
		else{
			log("adapter type is GRID");
			if (selectedItems.size() <= 0){
				((OfflineFragmentLollipop) fragment).hideMultipleSelect();
			}
			notifyItemChanged(positionToflip);
		}
	}
    
    public void selectAll() {
        for (int i = 0;i < getItemCount();i++) {
            if(getItem(i) == null) {
                continue;
            }
            if (!isItemChecked(i)) {
                toggleAllSelection(i);
            }
        }
    }

//	public boolean isRecoveryKey(MegaOffline currentNode){
//		log("isRecoveryKey");
//
//		//Check if it's the Master Key file
//		if(currentNode.getHandle().equals("0")){
//			return true;
//		}else{
//			return false;
//		}
//	}
    
    public void clearSelections() {
        for (int i = 0;i < this.getItemCount();i++) {
            if(getItem(i) == null) {
                continue;
            }
            if (isItemChecked(i)) {
                toggleAllSelection(i);
            }
        }
    }
	
	private boolean isItemChecked(int position) {
		log("isItemChecked");
		if((selectedItems!=null)){
			return selectedItems.get(position);
		}else{
			return false;
		}
    }

	public int getSelectedItemCount() {
		log("getSelectedItemCount");
		return selectedItems.size();
	}

	public List<Integer> getSelectedItems() {
		log("getSelectedItems");
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
	
	public MegaOfflineLollipopAdapter(OfflineFragmentLollipop _fragment, Context _context, ArrayList<MegaOffline> _mOffList, RecyclerView listView, ImageView emptyImageView, LinearLayout emptyTextView, ActionBar aB, int getAdapterType) {
		log("MegaOfflineListAdapter");
		this.fragment = _fragment;
		this.context = _context;
//		this.getAdapterType() =  getAdapterType();
        
        this.listFragment = listView;
        this.emptyImageViewFragment = emptyImageView;
        this.emptyTextViewFragment = emptyTextView;
        this.aB = aB;
        
        this.positionClicked = -1;
        //After this.listFragment = listView;
        this.mOffList = insertPlaceHolderNode(_mOffList);
    }
	
	public void setNodes(ArrayList<MegaOffline> mOffList){
		log("setNodes");
		String pathNav = fragment.getPathNavigation();
		if(pathNav!=null){		
			if (pathNav.equals("/")){
				if (mOffList != null){
					if(!mOffList.isEmpty()) {
						log("List not empty");
						MegaOffline lastItem = mOffList.get(mOffList.size()-1);
						if(!(lastItem.getHandle().equals("0"))){
							String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
							log("Export in: "+path);
							File file= new File(path);
							if(file.exists()){
								MegaOffline masterKeyFile = new MegaOffline("0", path, "MEGARecoveryKey.txt", 0, "0", 0, "0");
								mOffList.add(masterKeyFile);
							}
						}	
					}
					else{
						String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
						log("Export in: "+path);
						File file= new File(path);
						if(file.exists()){
							MegaOffline masterKeyFile = new MegaOffline("0", path, "MEGARecoveryKey.txt", 0, "0", 0, "0");
							mOffList.add(masterKeyFile);
						}
					}
				}						
			}
		}		
		
		this.mOffList = insertPlaceHolderNode(mOffList);
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

			if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				holder.textViewFileName.setMaxWidth(Util.scaleWidthPx(280, outMetrics));
			} else{
				holder.textViewFileName.setMaxWidth(Util.scaleWidthPx(240, outMetrics));
			}
			holder.textViewFileSize = (TextView) v.findViewById(R.id.offline_list_filesize);
			holder.threeDotsLayout = (RelativeLayout) v.findViewById(R.id.offline_list_three_dots_layout);

			holder.itemLayout.setOnClickListener(this);
			holder.itemLayout.setOnLongClickListener(this);
			holder.itemLayout.setTag(holder);
			
			v.setTag(holder);
			
			return holder;
		}else if (viewType == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID){
			ViewHolderOfflineGrid holder = null;
			
			View v = inflater.inflate(R.layout.item_offline_grid, parent, false);
			
			holder = new ViewHolderOfflineGrid(v);
			holder.folderLayout = v.findViewById(R.id.item_file_grid_folder);
			holder.fileLayout = v.findViewById(R.id.item_file_grid_file);
			holder.itemLayout = (RelativeLayout) v.findViewById(R.id.offline_grid_item_layout);
			holder.thumbLayout = (RelativeLayout) v.findViewById(R.id.file_grid_thumbnail_layout);
			holder.imageView = (ImageView) v.findViewById(R.id.file_grid_thumbnail);
			holder.iconView = (ImageView) v.findViewById(R.id.file_grid_icon_for_file);
			holder.textViewFileName = (TextView) v.findViewById(R.id.offline_grid_filename);
			holder.textViewFileName.setMaxWidth(Util.scaleWidthPx(240, outMetrics));
			holder.textViewFileSize = (TextView) v.findViewById(R.id.offline_grid_filesize);
			holder.imageButtonThreeDots = (ImageButton) v.findViewById(R.id.offline_grid_three_dots);
			holder.separator = (View) v.findViewById(R.id.offline_grid_separator);
			
			holder.imageViewIcon = (ImageView)v.findViewById(R.id.offline_grid_icon);
			holder.fileGridIconForFile = (ImageView)v.findViewById(R.id.file_grid_icon_for_file);
			holder.thumbLayoutForFile = (RelativeLayout)v.findViewById(R.id.file_grid_thumbnail_layout_for_file);
			holder.imageButtonThreeDotsForFile = (ImageButton)v.findViewById(R.id.file_grid_three_dots_for_file);
			holder.textViewFileNameForFile = (TextView)v.findViewById(R.id.file_grid_filename_for_file);
			holder.fileGridSelected = (ImageView)v.findViewById(R.id.file_grid_selected);
			
			holder.itemLayout.setOnClickListener(this);
			holder.itemLayout.setOnLongClickListener(this);
			holder.itemLayout.setTag(holder);
            
            holder.imageButtonThreeDots.setTag(holder);
            holder.imageButtonThreeDots.setOnClickListener(this);
            holder.imageButtonThreeDotsForFile.setTag(holder);
            holder.imageButtonThreeDotsForFile.setOnClickListener(this);
			v.setTag(holder);
			
			return holder;
		}
		else{
			return null;
		}
	}
	
//	private void dlog(Object o) {
//		String s = (o == null) ? "NULL" : o.toString();
//		Log.e("@#$",s);
//	}
	
	/**
	 * In grid view.
	 * For folder count is odd. Insert null element as placeholder.
	 *
	 * @param nodes Origin nodes to show.
	 * @return Nodes list with placeholder.
	 */
	private ArrayList<MegaOffline> insertPlaceHolderNode(ArrayList<MegaOffline> nodes) {
	    //need re-calculate
	    folderCount = 0;
        CopyOnWriteArrayList<MegaOffline> safeList = new CopyOnWriteArrayList(nodes);
        for (MegaOffline node : safeList) {
            if (node == null) {
                safeList.remove(node);
            } else if (node.isFolder()) {
                folderCount++;
            }
        }
		boolean isGrid = !(((ManagerActivityLollipop)context).isList);
        int spanCount = 2;
        if (listFragment instanceof NewGridRecyclerView) {
            spanCount = ((NewGridRecyclerView)listFragment).getSpanCount();
        }
        placeholderCount =  (folderCount % spanCount) == 0 ? 0 : spanCount - (folderCount % spanCount);
        
        if (folderCount > 0 && placeholderCount != 0 && isGrid) {
            //Add placeholder at folders' end.
            for (int i = 0;i < placeholderCount;i++) {
                safeList.add(folderCount + i,null);
            }
		}
		return new ArrayList<>(safeList);
	}

	@Override
	public void onBindViewHolder(ViewHolderOffline holder, int position) {
		log("onBindViewHolder");
		if (getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_LIST){
			ViewHolderOfflineList holderList = (ViewHolderOfflineList) holder;
			onBindViewHolderList(holderList, position);
		}
		else if (getAdapterType() == MegaOfflineLollipopAdapter.ITEM_VIEW_TYPE_GRID){
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

		MegaOffline currentNode = (MegaOffline) getItem(position);
		//Placeholder for folder when folder count is odd.
		if (currentNode == null) {
			holder.folderLayout.setVisibility(View.INVISIBLE);
			holder.fileLayout.setVisibility(View.GONE);
			holder.itemLayout.setVisibility(View.INVISIBLE);
			return;
		}
		if(currentNode.getHandle().equals("0")){
			//The node is the MasterKey File
			holder.textViewFileName.setText(currentNode.getName());
			
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
			File file= new File(path);
			long nodeSize;
			if(file.exists()){
				nodeSize = file.length();
				holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
			}
			holder.iconView.setImageResource(MimeTypeThumbnail.typeForName(currentNode.getName()).getIconResourceId());
			holder.iconView.setVisibility(View.VISIBLE);
			holder.imageView.setVisibility(View.GONE);
			holder.imageButtonThreeDots.setTag(holder);
			holder.imageButtonThreeDots.setOnClickListener(this);

			holder.itemLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.background_item_grid));
			holder.separator.setBackgroundColor(ContextCompat.getColor(context, R.color.new_background_fragment));

//			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			return;
		}

		String path=null;
		
		if(currentNode.getOrigin()==MegaOffline.INCOMING){
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + currentNode.getHandleIncoming() + "/";
		}
		else if(currentNode.getOrigin()==MegaOffline.INBOX){
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in/";
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

		holder.textViewFileName.setText(currentNode.getName());
		
		int folders=0;
		int files=0;
		if (currentFile.isDirectory()){
			
			File[] fList = currentFile.listFiles();

			if(fList != null){
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

			}else{
				holder.textViewFileSize.setText(" ");
			}
		}
		else{
			long nodeSize = currentFile.length();

			holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
		}

		holder.iconView.setImageResource(MimeTypeThumbnail.typeForName(currentNode.getName()).getIconResourceId());
		holder.imageView.setVisibility(View.GONE);
		holder.iconView.setVisibility(View.VISIBLE);

		if (currentFile.isFile()){
			holder.itemLayout.setVisibility(View.VISIBLE);
			holder.folderLayout.setVisibility(View.GONE);
			holder.fileLayout.setVisibility(View.VISIBLE);
			
			holder.itemLayout.setVisibility(View.VISIBLE);
			holder.folderLayout.setVisibility(View.GONE);
//			holder.imageViewThumb.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
//			holder.imageViewThumb.setVisibility(View.GONE);
			holder.fileLayout.setVisibility(View.VISIBLE);
			holder.textViewFileName.setVisibility(View.VISIBLE);
			holder.textViewFileSize.setVisibility(View.GONE);
			
			holder.textViewFileNameForFile.setText(currentNode.getName());
//			long nodeSize = currentNode.getSize();
//			holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
			
			holder.fileGridIconForFile.setVisibility(View.VISIBLE);
			holder.fileGridIconForFile.setImageResource(MimeTypeThumbnail.typeForName(currentNode.getName()).getIconResourceId());
			holder.thumbLayoutForFile.setBackgroundColor(Color.TRANSPARENT);
			
            if (multipleSelect && isItemChecked(position)) {
                holder.itemLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.background_item_grid_selected));
                holder.fileGridSelected.setVisibility(View.VISIBLE);
            } else {
                holder.itemLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.background_item_grid));
                holder.fileGridSelected.setVisibility(View.GONE);
            }
			if (MimeTypeThumbnail.typeForName(currentNode.getName()).isImage()){
				Bitmap thumb = null;
								
				if (currentFile.exists()){
					thumb = ThumbnailUtils.getThumbnailFromCache(Long.parseLong(currentNode.getHandle()));
					if (thumb != null){
						thumb = ThumbnailUtilsLollipop.getRoundedRectBitmap(context, thumb, 3);
						holder.imageView.setImageBitmap(thumb);
						holder.imageView.setVisibility(View.VISIBLE);
						holder.iconView.setVisibility(View.GONE);

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
		}else{
			holder.itemLayout.setVisibility(View.VISIBLE);
			holder.folderLayout.setVisibility(View.VISIBLE);
			holder.fileLayout.setVisibility(View.GONE);
			holder.textViewFileSize.setVisibility(View.VISIBLE);
			holder.imageView.setVisibility(View.GONE);
			holder.iconView.setVisibility(View.VISIBLE);
			setFolderSelected(holder,position,R.drawable.ic_folder_list);
		}
		
		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);
	}
	
	private void setFolderSelected(ViewHolderOfflineGrid holder,int position,int folderDrawableResId) {
		if (multipleSelect && isItemChecked(position)) {
			RelativeLayout.LayoutParams paramsMultiselect = (RelativeLayout.LayoutParams)holder.imageViewIcon.getLayoutParams();
			paramsMultiselect.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,24,context.getResources().getDisplayMetrics());
			paramsMultiselect.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,24,context.getResources().getDisplayMetrics());
			holder.imageViewIcon.setLayoutParams(paramsMultiselect);
			holder.itemLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.background_item_grid_selected));
			holder.imageViewIcon.setImageResource(R.drawable.ic_select_folder);
		} else {
			holder.itemLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.background_item_grid));
			holder.imageViewIcon.setImageResource(folderDrawableResId);
		}
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
				
		MegaOffline currentNode = (MegaOffline) getItem(position);
		
		if(currentNode.getHandle().equals("0")){
			//The node is the MasterKey File
			holder.textViewFileName.setText(currentNode.getName());
			
			String path = Environment.getExternalStorageDirectory().getAbsolutePath()+Util.rKFile;
			File file= new File(path);
			long nodeSize;
			if(file.exists()){
				nodeSize = file.length();
				holder.textViewFileSize.setText(Util.getSizeString(nodeSize));
			}			
			holder.imageView.setImageResource(MimeTypeList.typeForName(currentNode.getName()).getIconResourceId());
			holder.threeDotsLayout.setVisibility(View.VISIBLE);
			holder.threeDotsLayout.setTag(holder);
			holder.threeDotsLayout.setOnClickListener(this);
			holder.itemLayout.setBackgroundColor(Color.WHITE);
			return;
		}
		
		String path=null;
		
		if(currentNode.getOrigin()==MegaOffline.INCOMING){
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + currentNode.getHandleIncoming() + "/";
		}
		else if(currentNode.getOrigin()==MegaOffline.INBOX){
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/in/";
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

		holder.textViewFileName.setText(currentNode.getName());
		
		int folders=0;
		int files=0;
		if (currentFile.isDirectory()){

			log("Directory offline");
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
			params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params.setMargins(0, 0, 0, 0);

			holder.imageView.setLayoutParams(params);
			
			File[] fList = currentFile.listFiles();
			if(fList != null){
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
			}else{
				holder.textViewFileSize.setText(" ");

			}

			if (!multipleSelect) {
				holder.itemLayout.setBackgroundColor(Color.WHITE);
				holder.imageView.setImageResource(R.drawable.ic_folder_list);
			}
			else {
				if(this.isItemChecked(position)){
					//MegaOffline nodeTemp = (MegaOffline) getItem(position);
//					if(isRecoveryKey(nodeTemp)){
//						holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
//					}else{
//						holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_multiselect_color));
//					}
					holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_multiselect_color));
					RelativeLayout.LayoutParams paramsMultiselect = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
					paramsMultiselect.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
					paramsMultiselect.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
					params.setMargins(0, 0, 0, 0);

					holder.imageView.setLayoutParams(paramsMultiselect);
					holder.imageView.setImageResource(R.drawable.ic_select_folder);
				}
				else{
					holder.itemLayout.setBackgroundColor(Color.WHITE);
					holder.imageView.setImageResource(R.drawable.ic_folder_list);

				}
			}

		}
		else{
			log("File offline");
			long nodeSize = currentFile.length();
			holder.textViewFileSize.setText(Util.getSizeString(nodeSize));

			if (!multipleSelect) {
				log("Not multiselect");

				holder.itemLayout.setBackgroundColor(Color.WHITE);
				holder.imageView.setImageResource(MimeTypeList.typeForName(currentNode.getName()).getIconResourceId());
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
				params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
				params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
				params.setMargins(0, 0, 0, 0);

				holder.imageView.setLayoutParams(params);

				log("Check the thumb");
				if (MimeTypeList.typeForName(currentNode.getName()).isImage()){
					Bitmap thumb = null;

					if (currentFile.exists()){
						thumb = ThumbnailUtils.getThumbnailFromCache(Long.parseLong(currentNode.getHandle()));
						if (thumb != null){
							RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
							params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
							params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
							int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
							params1.setMargins(left, 0, 0, 0);

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
				log("multiselect ON");
				if(this.isItemChecked(position)){
					holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.new_multiselect_color));
					RelativeLayout.LayoutParams paramsMultiselect = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
					paramsMultiselect.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
					paramsMultiselect.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
					paramsMultiselect.setMargins(0, 0, 0, 0);

					holder.imageView.setLayoutParams(paramsMultiselect);
					holder.imageView.setImageResource(R.drawable.ic_select_folder);
				}
				else{
					holder.itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

					holder.imageView.setImageResource(MimeTypeList.typeForName(currentNode.getName()).getIconResourceId());
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
					params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
					params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
					params.setMargins(0, 0, 0, 0);

					holder.imageView.setLayoutParams(params);

					log("Check the thumb");
					if (MimeTypeList.typeForName(currentNode.getName()).isImage()){
						Bitmap thumb = null;

						if (currentFile.exists()){
							thumb = ThumbnailUtils.getThumbnailFromCache(Long.parseLong(currentNode.getHandle()));
							if (thumb != null){
								RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
								params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
								params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
								int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
								params1.setMargins(left, 0, 0, 0);

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
			}
		}

		holder.threeDotsLayout.setTag(holder);
		holder.threeDotsLayout.setOnClickListener(this);
	}

	
	@Override
	public int getItemCount() {
		log("getItemCount");
		return mOffList.size();
	}

	public int getItemCountWithoutRK(){
		log("getItemCountWithoutRK");

		ArrayList<MegaOffline> mOffListWithoutRK = new ArrayList<>();
		for(MegaOffline item: mOffList){
			if(item == null) {
				continue;
			}
			if(item.getHandle().equals("0")){
				//isRecoveryKey

			}else{
				//isnotRecoveryKey
				mOffListWithoutRK.add(item);
			}
		}
		return mOffListWithoutRK.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		log("getItemViewType");

		return getAdapterType();
	}
 
	public Object getItem(int position) {
		log("getItem");

		return mOffList.get(position);
	}

	public MegaOffline getItemOff(int position) {
		log("getItemOff");

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
    
//    public void setAdapterType(int getAdapterType()){
//    	this.getAdapterType() = getAdapterType();
//    }
//
//    public int getAdapterType(){
//    	return getAdapterType();
//	}

	@Override
	public void onClick(View v) {
		log("onClick");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		ViewHolderOffline holder = (ViewHolderOffline) v.getTag();
		
		int currentPosition = holder.getAdapterPosition();
		MegaOffline mOff = (MegaOffline) getItem(currentPosition);
		
		switch (v.getId()){
			case R.id.offline_list_item_layout:
			case R.id.offline_grid_item_layout:{
				int[] screenPosition = new int[2];
				ImageView imageView;
				if (getAdapterType() == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST) {
					imageView = (ImageView) v.findViewById(R.id.offline_list_thumbnail);
				}
				else {
					imageView = (ImageView) v.findViewById(R.id.offline_grid_thumbnail);
				}
				imageView.getLocationOnScreen(screenPosition);
				int[] dimens = new int[4];
				dimens[0] = screenPosition[0];
				dimens[1] = screenPosition[1];
				dimens[2] = imageView.getWidth();
				dimens[3] = imageView.getHeight();
				fragment.itemClick(currentPosition, dimens, imageView);
				break;
			}			
			case R.id.offline_list_three_dots_layout:
            case R.id.file_grid_three_dots_for_file:
			case R.id.offline_grid_three_dots:{

				if(context instanceof ManagerActivityLollipop){
					log("Connection! - ManagerActivity instance!");
					((ManagerActivityLollipop) context).showOptionsPanel(mOff);
				}
				break;
			}
		}		
	}

	@Override
	public boolean onLongClick(View view) {
		log("OnLongCLick");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		ViewHolderOffline holder = (ViewHolderOffline) view.getTag();
		int currentPosition = holder.getAdapterPosition();

		MegaOffline item = mOffList.get(currentPosition);
		if(!(item.getHandle().equals("0"))){
			fragment.activateActionMode();
			fragment.itemClick(currentPosition, null, null);
		}

		return true;
	}
	
	/*
	 * Get document at specified position
	 */
	public MegaOffline getNodeAt(int position) {
		log("getNodeAt");
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
		log("setMultipleSelect: " + multipleSelect);
		if (this.multipleSelect != multipleSelect) {
			this.multipleSelect = multipleSelect;
		}
		if (this.multipleSelect) {
			selectedItems = new SparseBooleanArray();
		}
	}

	private static void log(String log) {
		Util.log("MegaOfflineLollipopAdapter", log);
	}
}
