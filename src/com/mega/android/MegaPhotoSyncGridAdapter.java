package com.mega.android;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mega.android.utils.ThumbnailUtils;
import com.mega.android.utils.Util;
import com.mega.sdk.MegaApiAndroid;
import com.mega.sdk.MegaApiJava;
import com.mega.sdk.MegaNode;

public class MegaPhotoSyncGridAdapter extends BaseAdapter {
	
	Context context;
	
	ArrayList<MegaNode> nodes;
	ArrayList<MegaMonthPic> monthPics;
	int positionClicked;
	
	MegaApiAndroid megaApi;
	
	long photoSyncHandle = -1;
	
	ListView listFragment;
	ImageView emptyImageViewFragment;
	TextView emptyTextViewFragment;
	ActionBar aB;
	
	int numberOfCells;
	
	int orderGetChildren = MegaApiJava.ORDER_MODIFICATION_DESC;
	
	public MegaPhotoSyncGridAdapter(Context _context, ArrayList<MegaMonthPic> _monthPics, long _photosyncHandle, ListView listView, ImageView emptyImageView, TextView emptyTextView, ActionBar aB, ArrayList<MegaNode> _nodes, int numberOfCells) {
		this.context = _context;
		this.monthPics = _monthPics;
		this.photoSyncHandle = _photosyncHandle;
		this.nodes = _nodes;
		
		this.listFragment = listView;
		this.emptyImageViewFragment = emptyImageView;
		this.emptyTextViewFragment = emptyTextView;
		this.aB = aB;
		
		this.numberOfCells = numberOfCells;
		
		this.positionClicked = -1;
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
	}
	
	public void setNodes(ArrayList<MegaMonthPic> monthPics, ArrayList<MegaNode> nodes){
		this.monthPics = monthPics;
		this.nodes = nodes;
		positionClicked = -1;	
		notifyDataSetChanged();
//		listFragment.clearFocus();
//		if (listFragment != null){
//			listFragment.post(new Runnable() {
//                @Override
//                public void run() {                	
//                    listFragment.setSelection(0);
//                }
//            });
//		}
//		list.smoothScrollToPosition(0);
	}
	
	public void setPhotoSyncHandle(long photoSyncHandle){
		this.photoSyncHandle = photoSyncHandle;
		notifyDataSetChanged();
	}
	
	/*private view holder class*/
    public class ViewHolderPhotoSyncGrid {
    	
    	public LinearLayout cellLayout;
    	public ArrayList<RelativeLayout> relativeLayoutsComplete;
    	public ArrayList<RelativeLayout> relativeLayoutsEmpty;
    	public ArrayList<ImageView> imageViews;
    	public TextView textView;
    	public RelativeLayout textRelativeLayout;
    	
    	public ArrayList<Long> documents;
    }
    
    ViewHolderPhotoSyncGrid holder = null;
	int positionG;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
			holder = new ViewHolderPhotoSyncGrid();
			holder.relativeLayoutsEmpty = new ArrayList<RelativeLayout>();
			holder.relativeLayoutsComplete = new ArrayList<RelativeLayout>();
			holder.imageViews = new ArrayList<ImageView>();
			
			holder.documents = new ArrayList<Long>();

			convertView = inflater.inflate(R.layout.item_photo_sync_grid, parent, false);

			holder.cellLayout = (LinearLayout) convertView.findViewById(R.id.cell_photosync_layout);
			
			for (int i=0;i<numberOfCells;i++){
				View rLView = inflater.inflate(R.layout.cell_photosync_grid_fill, holder.cellLayout, false);
				
				RelativeLayout rL = (RelativeLayout) rLView.findViewById(R.id.cell_photosync_grid_item_complete_layout);
				rL.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
				holder.cellLayout.addView(rL);
				holder.relativeLayoutsComplete.add(rL);
				
				RelativeLayout rLE = (RelativeLayout) rLView.findViewById(R.id.cell_photosync_item_layout_empty);
				holder.relativeLayoutsEmpty.add(rLE);
				
				ImageView iV = (ImageView) rLView.findViewById(R.id.cell_photosync_grid_thumbnail);
				holder.imageViews.add(iV);
			}
			
			holder.textRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.cell_photosync_grid_month_layout);
			
			holder.textView = (TextView) convertView.findViewById(R.id.cell_photosync_grid_month_name);
			
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolderPhotoSyncGrid) convertView.getTag();
		}
		
		MegaMonthPic monthPic = (MegaMonthPic) getItem(position);
		
		if (monthPic.monthYearString != null){
			if (monthPic.monthYearString.compareTo("") != 0){
				holder.textRelativeLayout.setVisibility(View.VISIBLE);
				holder.textView.setText(monthPic.monthYearString);
				for (int i=0;i<numberOfCells;i++){
					holder.relativeLayoutsComplete.get(i).setVisibility(View.GONE);
					holder.relativeLayoutsEmpty.get(i).setVisibility(View.GONE);
				}
			}
			else{
				holder.textRelativeLayout.setVisibility(View.GONE);
				for (int i=0;i<numberOfCells;i++){
					if (monthPic.nodeHandles.size() > i){
						MegaNode n = megaApi.getNodeByHandle(monthPic.nodeHandles.get(i));
						
						holder.relativeLayoutsComplete.get(i).setVisibility(View.VISIBLE);
						holder.imageViews.get(i).setVisibility(View.VISIBLE);
						holder.relativeLayoutsEmpty.get(i).setVisibility(View.GONE);
						if (holder.documents.size() > i){
							holder.documents.set(i, n.getHandle());
						}
						else{
							holder.documents.add(i, n.getHandle());
						}
						
						Bitmap thumb = null;						
						holder.imageViews.get(i).setImageResource(MimeType.typeForName(n.getName()).getIconResourceId());	
						if (n.hasThumbnail()){
							thumb = ThumbnailUtils.getThumbnailFromCache(n);
							if (thumb != null){
								holder.imageViews.get(i).setImageBitmap(thumb);
							}
							else{
								thumb = ThumbnailUtils.getThumbnailFromFolder(n, context);
								if (thumb != null){
									holder.imageViews.get(i).setImageBitmap(thumb);
								}
								else{ 
									try{
										thumb = ThumbnailUtils.getThumbnailFromMegaPhotoSyncGrid(n, context, holder, megaApi, this, i);
									}
									catch(Exception e){} //Too many AsyncTasks
									
									if (thumb != null){
										holder.imageViews.get(i).setImageBitmap(thumb);
									}
									else{
										holder.imageViews.get(i).setImageResource(MimeType.typeForName(n.getName()).getIconResourceId());
									}
								}
							}
						}
					}
					else{
						holder.relativeLayoutsComplete.get(i).setVisibility(View.VISIBLE);
						holder.imageViews.get(i).setVisibility(View.GONE);
						holder.relativeLayoutsEmpty.get(i).setVisibility(View.VISIBLE);
						if (holder.documents.size() > i){
							holder.documents.set(i,  -1l);
						}
						else{
							holder.documents.add(i, -1l);
						}
					}
				}
			}
				
		}		
		else{
			holder.textRelativeLayout.setVisibility(View.GONE);
			for (int i=0;i<numberOfCells;i++){
				
				if (monthPic.nodeHandles.size() > i){
					MegaNode n = megaApi.getNodeByHandle(monthPic.nodeHandles.get(i));
					
					holder.relativeLayoutsComplete.get(i).setVisibility(View.VISIBLE);
					holder.imageViews.get(i).setVisibility(View.VISIBLE);
					holder.relativeLayoutsEmpty.get(i).setVisibility(View.GONE);
					if (holder.documents.size() > i){
						holder.documents.set(i, n.getHandle());
					}
					else{
						holder.documents.add(i, n.getHandle());
					}
					
					Bitmap thumb = null;					
					holder.imageViews.get(i).setImageResource(MimeType.typeForName(n.getName()).getIconResourceId());
					if (n.hasThumbnail()){
						thumb = ThumbnailUtils.getThumbnailFromCache(n);
						if (thumb != null){
							holder.imageViews.get(i).setImageBitmap(thumb);
						}
						else{
							thumb = ThumbnailUtils.getThumbnailFromFolder(n, context);
							if (thumb != null){
								holder.imageViews.get(i).setImageBitmap(thumb);
							}
							else{ 
								try{
									thumb = ThumbnailUtils.getThumbnailFromMegaPhotoSyncGrid(n, context, holder, megaApi, this, i);
								}
								catch(Exception e){} //Too many AsyncTasks
								
								if (thumb != null){
									holder.imageViews.get(i).setImageBitmap(thumb);
								}
								else{
									holder.imageViews.get(i).setImageResource(MimeType.typeForName(n.getName()).getIconResourceId());
								}
							}
						}
					}
				}
				else{
					holder.relativeLayoutsComplete.get(i).setVisibility(View.VISIBLE);
					holder.imageViews.get(i).setVisibility(View.GONE);
					holder.relativeLayoutsEmpty.get(i).setVisibility(View.VISIBLE);
					if (holder.documents.size() > i){
						holder.documents.set(i,  -1l);
					}
					else{
						holder.documents.add(i, -1l);
					}
				}				
			}
		}
		
		
		for (int i=0; i< holder.imageViews.size(); i++){
			final int index = i;
			ImageView iV = holder.imageViews.get(i);
			iV.setTag(holder);
			iV.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewHolderPhotoSyncGrid holder= (ViewHolderPhotoSyncGrid) v.getTag();
					
					long handle = holder.documents.get(index);
//					MegaNode n = megaApi.getNodeByHandle(handle);
					nodeClicked(handle);
				}
			} );
		}
		
		
		return convertView;
	}	
	
	public void nodeClicked(long handle){
		
		MegaNode n = megaApi.getNodeByHandle(handle);
		if (n != null){
			if (!n.isFolder()){
				if (MimeType.typeForName(n.getName()).isImage()){
					int positionInNodes = 0;
					for (int i=0;i<nodes.size();i++){
						if(nodes.get(i).getHandle() == n.getHandle()){
							positionInNodes = i;
							break;
						}
					}
					
					Intent intent = new Intent(context, FullScreenImageViewer.class);
					intent.putExtra("position", positionInNodes);
					intent.putExtra("parentNodeHandle", megaApi.getParentNode(n).getHandle());
					intent.putExtra("adapterType", ManagerActivity.PHOTO_SYNC_ADAPTER);
					intent.putExtra("orderGetChildren", orderGetChildren);
					context.startActivity(intent);
				}
				else if (MimeType.typeForName(n.getName()).isVideo() || MimeType.typeForName(n.getName()).isAudio() ){
					MegaNode file = n;
					Intent service = new Intent(context, MegaStreamingService.class);
			  		context.startService(service);
			  		String fileName = file.getName();
					try {
						fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
					} 
					catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
			  		String url = "http://127.0.0.1:4443/" + file.getBase64Handle() + "/" + fileName;
			  		String mimeType = MimeType.typeForName(file.getName()).getType();
			  		System.out.println("FILENAME: " + fileName);
			  		
			  		Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
			  		mediaIntent.setDataAndType(Uri.parse(url), mimeType);
			  		if (ManagerActivity.isIntentAvailable(context, mediaIntent)){
			  			context.startActivity(mediaIntent);
			  		}
			  		else{
			  			Toast.makeText(context, context.getResources().getString(R.string.intent_not_available), Toast.LENGTH_LONG).show();
			  			ArrayList<Long> handleList = new ArrayList<Long>();
						handleList.add(n.getHandle());
						((ManagerActivity) context).onFileClick(handleList);
			  		}						
				}
				else{
					ArrayList<Long> handleList = new ArrayList<Long>();
					handleList.add(n.getHandle());
					((ManagerActivity) context).onFileClick(handleList);
				}	
				positionClicked = -1;
				notifyDataSetChanged();
			}
		}
		
	}

	@Override
    public int getCount() {
		return monthPics.size();
    }
 
    @Override
    public Object getItem(int position) {
        return monthPics.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }    
    
    public int getPositionClicked (){
    	return positionClicked;
    }
    
    public void setPositionClicked(int p){
    	positionClicked = p;
    }
	
	public long getPhotoSyncHandle(){
		return photoSyncHandle;
	}
	
	public void setOrder(int orderGetChildren){
		this.orderGetChildren = orderGetChildren;
	}
	
	private static void log(String log) {
		Util.log("MegaPhotoSyncGridAdapter", log);
	}
}
