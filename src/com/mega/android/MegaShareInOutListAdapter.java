package com.mega.android;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mega.android.utils.ThumbnailUtils;
import com.mega.android.utils.Util;
import com.mega.components.RoundedImageView;
import com.mega.sdk.MegaApiAndroid;
import com.mega.sdk.MegaApiJava;
import com.mega.sdk.MegaNode;
import com.mega.sdk.MegaShare;
import com.mega.sdk.MegaTransfer;
import com.mega.sdk.MegaUser;
import com.mega.android.utils.ThumbnailUtils;
import com.mega.android.utils.Util;
import com.mega.components.RoundedImageView;
import com.mega.sdk.MegaApiAndroid;
import com.mega.sdk.MegaApiJava;
import com.mega.sdk.MegaError;
import com.mega.sdk.MegaNode;
import com.mega.sdk.MegaRequest;
import com.mega.sdk.MegaRequestListenerInterface;
import com.mega.sdk.MegaTransfer;
import com.mega.sdk.MegaUser;

public class MegaShareInOutListAdapter extends BaseAdapter implements OnClickListener {

	Context context;
	MegaApiAndroid megaApi;

	int positionClicked;
	//ArrayList<MegaNode> nodes;

	long parentHandle = -1;

	ArrayList<MegaShareAndroidElement> megaShareInList;
	
	ListView listFragment;
	ImageView emptyImageViewFragment;
	TextView emptyTextViewFragment;
	ActionBar aB;
	HashMap<Long, MegaTransfer> mTHash = null;
	
	MegaTransfer currentTransfer = null;
	
	public static int MODE_IN = 0;
	public static int MODE_OUT = 1;

	//boolean multipleSelect;
	int type = ManagerActivity.MODE_IN;

	int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;
	
//	public static ArrayList<String> pendingAvatars = new ArrayList<String>();
	
	private class UserAvatarListenerList implements MegaRequestListenerInterface{

		Context context;
		ViewHolderInOutShareList holder;
		MegaShareInOutListAdapter adapter;
		
		public UserAvatarListenerList(Context context, ViewHolderInOutShareList holder, MegaShareInOutListAdapter adapter) {
			this.context = context;
			this.holder = holder;
			this.adapter = adapter;
		}
		
		@Override
		public void onRequestStart(MegaApiJava api, MegaRequest request) {
			log("onRequestStart()");
		}

		@Override
		public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
			
			log("onRequestFinish() "+request.getEmail());
			if (e.getErrorCode() == MegaError.API_OK){
				boolean avatarExists = false;
				
//				pendingAvatars.remove(request.getEmail());
				
				if (holder.contactMail.compareTo(request.getEmail()) == 0){
					File avatar = null;
					if (context.getExternalCacheDir() != null){
						avatar = new File(context.getExternalCacheDir().getAbsolutePath(), holder.contactMail + ".jpg");
					}
					else{
						avatar = new File(context.getCacheDir().getAbsolutePath(), holder.contactMail + ".jpg");
					}
					Bitmap bitmap = null;
					if (avatar.exists()){
						if (avatar.length() > 0){
							BitmapFactory.Options bOpts = new BitmapFactory.Options();
							bOpts.inPurgeable = true;
							bOpts.inInputShareable = true;
							bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
							if (bitmap == null) {
								avatar.delete();
							}
							else{
								avatarExists = true;
								holder.roundedImageView.setImageBitmap(bitmap);
							}
						}
					}
					
					if (!avatarExists){
						createDefaultAvatar();
					}
				}
			}
			else{
//				pendingAvatars.remove(request.getEmail());
				
				if (holder.contactMail.compareTo(request.getEmail()) == 0){
					createDefaultAvatar();
				}
			}
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
			holder.roundedImageView.setImageBitmap(defaultAvatar);
			
			
			Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics ();
		    display.getMetrics(outMetrics);
		    float density  = context.getResources().getDisplayMetrics().density;
		    
		    int avatarTextSize = getAvatarTextSize(density);
		    log("DENSITY: " + density + ":::: " + avatarTextSize);
		    if (holder.contactMail != null){
			    if (holder.contactMail.length() > 0){
			    	log("TEXT: " + holder.contactMail);
			    	log("TEXT AT 0: " + holder.contactMail.charAt(0));
			    	String firstLetter = holder.contactMail.charAt(0) + "";
			    	firstLetter = firstLetter.toUpperCase(Locale.getDefault());
			    	holder.contactInitialLetter.setText(firstLetter);
			    	holder.contactInitialLetter.setTextSize(32);
			    	holder.contactInitialLetter.setTextColor(Color.WHITE);
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

		@Override
		public void onRequestTemporaryError(MegaApiJava api,MegaRequest request, MegaError e) {
			log("onRequestTemporaryError");
		}

		@Override
		public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
			// TODO Auto-generated method stub
			
		}
		
	}	

	/* public static view holder class */
	public class ViewHolderInOutShareList {
		public ImageView imageView;
		public RoundedImageView roundedImageView;
		public TextView contactInitialLetter;
		public TextView textViewFileName;
		public TextView textViewFileSize;
		public RelativeLayout itemLayoutFile;
		public RelativeLayout itemLayoutContact;
		//public ImageView arrowSelection;
		public RelativeLayout optionsLayout;
		public ImageView optionDownload;
		public ImageView optionProperties;
		public ProgressBar transferProgressBar;
		public int currentPosition;
		public RoundedImageView contactThumbnail;
		public TextView contactName;
//		public TextView contactContent;		
		public long document;
		String contactMail;
		//public TextView textViewOwner;
	}

	public MegaShareInOutListAdapter(Context _context, ArrayList<MegaShareAndroidElement> _megaShareInList,long _parentHandle, ListView listView, ImageView emptyImageView,TextView emptyTextView, ActionBar aB, int type) {
		this.context = _context;
		this.megaShareInList = _megaShareInList;		
		this.parentHandle = _parentHandle;

		this.listFragment = listView;
		this.emptyImageViewFragment = emptyImageView;
		this.emptyTextViewFragment = emptyTextView;
		this.aB = aB;

		this.positionClicked = -1;

		this.type = type;
		
		log("MegaShareInOutAdapter: "+type);

		if (megaApi == null) {
			megaApi = ((MegaApplication) ((Activity) context).getApplication()).getMegaApi();
		}
	}
	
	
	public void setNodes(ArrayList<MegaShareAndroidElement> _megaShareInList) {
		this.megaShareInList = _megaShareInList;
		positionClicked = -1;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		listFragment = (ListView) parent;
		final int _position = position;

		ViewHolderInOutShareList holder = null;

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = ((Activity) context).getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.shared_item_list, parent,false);
			holder = new ViewHolderInOutShareList();
			
			holder.itemLayoutFile = (RelativeLayout) convertView.findViewById(R.id.file_share_item_layout);
			holder.imageView = (ImageView) convertView.findViewById(R.id.file_list_thumbnail_share);
			holder.textViewFileName = (TextView) convertView.findViewById(R.id.file_list_filename_share);			
			holder.textViewFileName.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			holder.textViewFileName.getLayoutParams().width = Util.px2dp((225 * scaleW), outMetrics);
			holder.textViewFileSize = (TextView) convertView.findViewById(R.id.file_list_filesize_share);
			holder.transferProgressBar = (ProgressBar) convertView.findViewById(R.id.transfers_list_bar_share);		
					
			holder.itemLayoutContact = (RelativeLayout) convertView.findViewById(R.id.contact_share_item_layout);
			holder.contactName = (TextView) convertView.findViewById(R.id.shared_contact_name);
//			holder.contactContent = (TextView) convertView.findViewById(R.id.shared_contact_content);
			holder.roundedImageView = (RoundedImageView) convertView.findViewById(R.id.contact_list_thumbnail_share);
			holder.contactInitialLetter = (TextView) convertView.findViewById(R.id.contact_list_initial_letter_share); 
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderInOutShareList) convertView.getTag();
		}

//		if (!multipleSelect) {
//			holder.checkbox.setVisibility(View.GONE);
//			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
//		} else {
//			holder.checkbox.setVisibility(View.VISIBLE);
////			holder.arrowSelection.setVisibility(View.GONE);
//			holder.imageButtonThreeDots.setVisibility(View.GONE);
//
//			SparseBooleanArray checkedItems = listFragment.getCheckedItemPositions();
//			if (checkedItems.get(position, false) == true) {
//				holder.checkbox.setChecked(true);
//			} else {
//				holder.checkbox.setChecked(false);
//			}
//		}
		
		holder.roundedImageView.setImageBitmap(null);
		holder.contactInitialLetter.setText("");
		holder.currentPosition = position;

//		HashMap<MegaUser,ArrayList<MegaNode>> _inSHash	
		
		if(type==MODE_IN){
			log("type=MODE_IN");
			MegaShareAndroidElement mUserShare = (MegaShareAndroidElement) getItem(position);
			MegaNode node = mUserShare.getNode();
			MegaUser megaUser = mUserShare.getUser();
	
			Bitmap thumb = null;
			
			if(node==null){
				//It is a contact
				
				getAvatar(megaUser, holder);
				
				//End thumb			
				
	//			holder.optionsLayout.setVisibility(View.GONE);
				
				holder.itemLayoutFile.setVisibility(View.GONE);
				holder.imageView.setVisibility(View.GONE);
				holder.textViewFileName.setVisibility(View.GONE);			
				holder.textViewFileSize.setVisibility(View.GONE);
				
				holder.transferProgressBar.setVisibility(View.GONE);				
				
				holder.roundedImageView.setVisibility(View.VISIBLE);				
				holder.itemLayoutContact.setVisibility(View.VISIBLE);
				holder.contactName.setVisibility(View.VISIBLE);
//				holder.contactContent.setVisibility(View.VISIBLE);	
				
			}
			else{
				//It is node

				holder.textViewFileName.setText(node.getName());
					
				if (node.isFolder()) {
					holder.textViewFileSize.setText(getInfoFolder(node));			
					
					holder.imageView.setImageResource(R.drawable.mime_folder_shared);
						
				} else {
					long nodeSize = node.getSize();
					holder.textViewFileSize.setText(Util.getSizeString(nodeSize));	
					
					if(mTHash!=null){
						
						log("NODE: " + mTHash.get(node.getHandle()));
						MegaTransfer tempT = mTHash.get(node.getHandle());
						
						if (tempT!=null){
							holder.transferProgressBar.setVisibility(View.VISIBLE);		
							holder.textViewFileSize.setVisibility(View.GONE);	
							
							double progressValue = 100.0 * tempT.getTransferredBytes() / tempT.getTotalBytes();
							holder.transferProgressBar.setProgress((int)progressValue);
						}
						
						if (currentTransfer != null){
							if (node.getHandle() == currentTransfer.getNodeHandle()){
								holder.transferProgressBar.setVisibility(View.VISIBLE);		
								holder.textViewFileSize.setVisibility(View.GONE);	
								double progressValue = 100.0 * currentTransfer.getTransferredBytes() / currentTransfer.getTotalBytes();
								holder.transferProgressBar.setProgress((int)progressValue);
							}
						}
						
						if(mTHash.size() == 0){
							holder.transferProgressBar.setVisibility(View.GONE);		
							holder.textViewFileSize.setVisibility(View.VISIBLE);	
						}
					}			
					
					holder.imageView.setImageResource(MimeType.typeForName(node.getName()).getIconResourceId());
	
					if (node.hasThumbnail()) {
						thumb = ThumbnailUtils.getThumbnailFromCache(node);
						if (thumb != null) {
							holder.imageView.setImageBitmap(thumb);
						} else {
							thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
							if (thumb != null) {
								holder.imageView.setImageBitmap(thumb);
							} else {
								try {
									thumb = ThumbnailUtils.getThumbnailFromMegaListShare(node,context, holder, megaApi, this);
									
								} catch (Exception e) {
								} // Too many AsyncTasks
	
								if (thumb != null) {
									holder.imageView.setImageBitmap(thumb);
								}
							}
						}
					} else {
						thumb = ThumbnailUtils.getThumbnailFromCache(node);
						if (thumb != null) {
							holder.imageView.setImageBitmap(thumb);
						} else {
							thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
							if (thumb != null) {
								holder.imageView.setImageBitmap(thumb);
							} else {
								try {
									ThumbnailUtils.createThumbnailListShare(context, node,holder, megaApi, this);
								} catch (Exception e) {
								} // Too many AsyncTasks
							}
						}
					}
				}
							
	//			holder.optionsLayout.setVisibility(View.GONE);
				
				holder.itemLayoutFile.setVisibility(View.VISIBLE);
				holder.imageView.setVisibility(View.VISIBLE);
				holder.textViewFileName.setVisibility(View.VISIBLE);			
				holder.textViewFileSize.setVisibility(View.VISIBLE);
				
				holder.transferProgressBar.setVisibility(View.GONE);	
				
				holder.itemLayoutContact.setVisibility(View.GONE);
				holder.contactName.setVisibility(View.GONE);
//				holder.contactContent.setVisibility(View.GONE);
				holder.roundedImageView.setVisibility(View.GONE);
			}
		}
		else{
			//type=MODE_OUT
			log("type=MODE_OUT");
			
			MegaShareAndroidElement mUserShare = (MegaShareAndroidElement) getItem(position);
			MegaNode node = mUserShare.getNode();
			MegaUser megaUser = mUserShare.getUser();
	
			Bitmap thumb = null;
			
			if (node.isFolder()) {
			
				if(mUserShare.isRepeat()){

					if(megaUser!=null){
						//Contact
						
						holder.contactName.setText(megaUser.getEmail());
						holder.contactMail=megaUser.getEmail();
						
						getAvatar(megaUser, holder);
						
						holder.itemLayoutFile.setVisibility(View.GONE);
						holder.imageView.setVisibility(View.GONE);
						holder.textViewFileName.setVisibility(View.GONE);			
						holder.textViewFileSize.setVisibility(View.GONE);
						
						holder.transferProgressBar.setVisibility(View.GONE);	
						
						holder.roundedImageView.setVisibility(View.VISIBLE);
						holder.itemLayoutContact.setVisibility(View.VISIBLE);
						holder.contactName.setVisibility(View.VISIBLE);
//						holder.contactContent.setVisibility(View.VISIBLE);
						holder.roundedImageView.setVisibility(View.VISIBLE);
					}
					else{
						//pongo el public link
						
						holder.contactName.setText(R.string.file_properties_shared_folder_public_link);
						holder.contactMail=null;
						
						holder.itemLayoutFile.setVisibility(View.GONE);
						holder.imageView.setVisibility(View.GONE);
						holder.textViewFileName.setVisibility(View.GONE);			
						holder.textViewFileSize.setVisibility(View.GONE);
						
						holder.transferProgressBar.setVisibility(View.GONE);
						
						holder.roundedImageView.setVisibility(View.GONE);
						holder.itemLayoutContact.setVisibility(View.VISIBLE);
						holder.contactName.setVisibility(View.VISIBLE);
//						holder.contactContent.setVisibility(View.VISIBLE);
						holder.roundedImageView.setVisibility(View.VISIBLE);
					}	
					
				}
				else{
					
					if(megaUser!=null){
						//Contact
						getAvatar(megaUser, holder);						
						//End thumb								
						
						holder.contactName.setText(megaUser.getEmail());
						holder.contactMail=megaUser.getEmail();

					}
					else{
						//Public link
						
						holder.contactName.setText(R.string.file_properties_shared_folder_public_link);
						holder.contactMail=null;
					}		
					
					holder.textViewFileName.setText(node.getName());
					
					holder.textViewFileSize.setText(getInfoFolder(node));			
					
					holder.imageView.setImageResource(R.drawable.mime_folder_shared);
					
					holder.itemLayoutFile.setVisibility(View.VISIBLE);
					holder.imageView.setVisibility(View.VISIBLE);
					holder.textViewFileName.setVisibility(View.VISIBLE);			
					holder.textViewFileSize.setVisibility(View.VISIBLE);
					
					holder.transferProgressBar.setVisibility(View.GONE);	
					
					holder.itemLayoutContact.setVisibility(View.VISIBLE);
					holder.contactName.setVisibility(View.VISIBLE);
//					holder.contactContent.setVisibility(View.VISIBLE);
					holder.roundedImageView.setVisibility(View.VISIBLE);
				}				
					
			} else {
				long nodeSize = node.getSize();
				holder.textViewFileSize.setText(Util.getSizeString(nodeSize));	
				
				if(mTHash!=null){
					
					log("NODE: " + mTHash.get(node.getHandle()));
					MegaTransfer tempT = mTHash.get(node.getHandle());
					
					if (tempT!=null){
						holder.transferProgressBar.setVisibility(View.VISIBLE);		
						holder.textViewFileSize.setVisibility(View.GONE);	
						
						double progressValue = 100.0 * tempT.getTransferredBytes() / tempT.getTotalBytes();
						holder.transferProgressBar.setProgress((int)progressValue);
					}
					
					if (currentTransfer != null){
						if (node.getHandle() == currentTransfer.getNodeHandle()){
							holder.transferProgressBar.setVisibility(View.VISIBLE);		
							holder.textViewFileSize.setVisibility(View.GONE);	
							double progressValue = 100.0 * currentTransfer.getTransferredBytes() / currentTransfer.getTotalBytes();
							holder.transferProgressBar.setProgress((int)progressValue);
						}
					}
					
					if(mTHash.size() == 0){
						holder.transferProgressBar.setVisibility(View.GONE);		
						holder.textViewFileSize.setVisibility(View.VISIBLE);	
					}
				}			
				
				holder.imageView.setImageResource(MimeType.typeForName(node.getName()).getIconResourceId());

				if (node.hasThumbnail()) {
					thumb = ThumbnailUtils.getThumbnailFromCache(node);
					if (thumb != null) {
						holder.imageView.setImageBitmap(thumb);
					} else {
						thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
						if (thumb != null) {
							holder.imageView.setImageBitmap(thumb);
						} else {
							try {
								thumb = ThumbnailUtils.getThumbnailFromMegaListShare(node,context, holder, megaApi, this);
								
							} catch (Exception e) {
							} // Too many AsyncTasks

							if (thumb != null) {
								holder.imageView.setImageBitmap(thumb);
							}
						}
					}
				} else {
					thumb = ThumbnailUtils.getThumbnailFromCache(node);
					if (thumb != null) {
						holder.imageView.setImageBitmap(thumb);
					} else {
						thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
						if (thumb != null) {
							holder.imageView.setImageBitmap(thumb);
						} else {
							try {
								ThumbnailUtils.createThumbnailListShare(context, node,holder, megaApi, this);
							} catch (Exception e) {
							} // Too many AsyncTasks
						}
					}
				}			
				
				holder.itemLayoutFile.setVisibility(View.VISIBLE);
				holder.imageView.setVisibility(View.VISIBLE);
				holder.textViewFileName.setVisibility(View.VISIBLE);			
				holder.textViewFileSize.setVisibility(View.VISIBLE);
				
				holder.transferProgressBar.setVisibility(View.GONE);	
				
				holder.itemLayoutContact.setVisibility(View.GONE);
				holder.contactName.setVisibility(View.GONE);
//				holder.contactContent.setVisibility(View.GONE);
				holder.roundedImageView.setVisibility(View.GONE);
			}

		}

//		holder.imageButtonThreeDots.setTag(holder);
//		holder.imageButtonThreeDots.setOnClickListener(this);

		/*
		if (positionClicked != -1) {
			if (positionClicked == position) {
				//				holder.arrowSelection.setVisibility(View.VISIBLE);
				LayoutParams params = holder.optionsLayout.getLayoutParams();
				params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
				//				holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
				listFragment.smoothScrollToPosition(_position);

				// Choose the buttons to show depending on the type of
				// folder

				MegaNode n = (MegaNode) getItem(positionClicked);
				MegaNode folder = null;

				if (n.isFile())
					folder = megaApi.getParentNode(n);
				else
					folder = n;

				int accessLevel = megaApi.getAccess(folder);

				switch (accessLevel) {
					case MegaShare.ACCESS_FULL: {
	
						//						holder.optionDownload.setVisibility(View.VISIBLE);
						//						holder.optionProperties.setVisibility(View.VISIBLE);
						//						holder.optionPublicLink.setVisibility(View.GONE);
						//						holder.optionRename.setVisibility(View.VISIBLE);
						//						holder.optionDelete.setVisibility(View.VISIBLE);
						//
						//						holder.optionDownload.getLayoutParams().width = Util
						//								.px2dp((44 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionDownload
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((9 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
						//						holder.optionProperties.getLayoutParams().width = Util
						//								.px2dp((44 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionProperties
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((17 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);						
						//						holder.optionRename.getLayoutParams().width = Util
						//								.px2dp((44 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionRename
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((17 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
						//						holder.optionDelete.getLayoutParams().width = Util
						//								.px2dp((44 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionDelete
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((17 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
						// holder.textViewPermissions.setText(context.getString(R.string.file_properties_shared_folder_full_access));
						break;
					}
					case MegaShare.ACCESS_READ: {
						log("read");
						//						holder.optionDownload.setVisibility(View.VISIBLE);
						//						holder.optionProperties.setVisibility(View.VISIBLE);						
						//						holder.optionPublicLink.setVisibility(View.GONE);
						//						holder.optionRename.setVisibility(View.GONE);
						//						holder.optionDelete.setVisibility(View.GONE);
						//
						//						holder.optionDownload.getLayoutParams().width = Util
						//								.px2dp((100 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionDownload
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((9 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
						//						holder.optionProperties.getLayoutParams().width = Util
						//								.px2dp((100 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionProperties
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((17 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
						break;
					}
					case MegaShare.ACCESS_READWRITE: {
						log("readwrite");
						//						holder.optionDownload.setVisibility(View.VISIBLE);
						//						holder.optionProperties.setVisibility(View.VISIBLE);
						//						holder.optionPublicLink.setVisibility(View.GONE);
						//						holder.optionRename.setVisibility(View.VISIBLE);
						//						holder.optionDelete.setVisibility(View.GONE);
						//
						//						holder.optionDownload.getLayoutParams().width = Util
						//								.px2dp((70 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionDownload
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((9 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
						//						holder.optionProperties.getLayoutParams().width = Util
						//								.px2dp((70 * scaleW), outMetrics);
						//						((TableRow.LayoutParams) holder.optionProperties
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((17 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
						//						((TableRow.LayoutParams) holder.optionRename
						//								.getLayoutParams()).setMargins(
						//								Util.px2dp((17 * scaleW), outMetrics),
						//								Util.px2dp((4 * scaleH), outMetrics), 0, 0);
	
	
					}
				}
			}
		}*/
			
		return convertView;
	}
	
	private void getAvatar(MegaUser megaUser, ViewHolderInOutShareList holder){
		
		UserAvatarListenerList listener = new UserAvatarListenerList(context,holder,this);
		holder.contactName.setText(megaUser.getEmail());
		holder.contactMail=megaUser.getEmail();
		File avatar = null;
		if (context.getExternalCacheDir() != null){
			avatar = new File(context.getExternalCacheDir().getAbsolutePath(), megaUser.getEmail() + ".jpg");
		}
		else{
			avatar = new File(context.getCacheDir().getAbsolutePath(), megaUser.getEmail() + ".jpg");
		}
		Bitmap bitmap = null;
		if (avatar.exists()){
			if (avatar.length() > 0){
				BitmapFactory.Options bOpts = new BitmapFactory.Options();
				bOpts.inPurgeable = true;
				bOpts.inInputShareable = true;
				bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
				if (bitmap == null) {
					avatar.delete();
					if (context.getExternalCacheDir() != null){
						megaApi.getUserAvatar(megaUser, context.getExternalCacheDir().getAbsolutePath() + "/" + megaUser.getEmail() + ".jpg", listener);
					}
					else{
						megaApi.getUserAvatar(megaUser, context.getCacheDir().getAbsolutePath() + "/" + megaUser.getEmail() + ".jpg", listener);
					}
				}
				else{
					holder.roundedImageView.setImageBitmap(bitmap);
				}
			}
			else{
				if (context.getExternalCacheDir() != null){
					megaApi.getUserAvatar(megaUser, context.getExternalCacheDir().getAbsolutePath() + "/" + megaUser.getEmail() + ".jpg", listener);	
				}
				else{
					megaApi.getUserAvatar(megaUser, context.getCacheDir().getAbsolutePath() + "/" + megaUser.getEmail() + ".jpg", listener);	
				}			
			}
		}	
		else{
//			if (!pendingAvatars.contains(megaUser.getEmail())){
//				pendingAvatars.add(megaUser.getEmail());
				if (context.getExternalCacheDir() != null){
					megaApi.getUserAvatar(megaUser, context.getExternalCacheDir().getAbsolutePath() + "/" + megaUser.getEmail() + ".jpg", listener);
				}
				else{
					megaApi.getUserAvatar(megaUser, context.getCacheDir().getAbsolutePath() + "/" + megaUser.getEmail() + ".jpg", listener);
				}
//			}
		}
	}
				
	private String getInfoFolder(MegaNode n) {
		ArrayList<MegaNode> nL = megaApi.getChildren(n);

		int numFolders = 0;
		int numFiles = 0;

		for (int i = 0; i < nL.size(); i++) {
			MegaNode c = nL.get(i);
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

		return info;
	}

	@Override
	public boolean isEnabled(int position) {
		// if (position == 0){
		// return false;
		// }
		// else{
		// return true;
		// }
		return super.isEnabled(position);
	}

	@Override
	public int getCount() {
		return megaShareInList.size();
	}

	@Override
	public Object getItem(int position) {
		return megaShareInList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getPositionClicked() {
		return positionClicked;
	}

	public void setPositionClicked(int p) {
		positionClicked = p;
	}

	@Override
	public void onClick(View v) {
		ViewHolderInOutShareList holder = (ViewHolderInOutShareList) v.getTag();
		int currentPosition = holder.currentPosition;
		MegaNode n = (MegaNode) getItem(currentPosition);

		switch (v.getId()) {
		case R.id.file_list_option_download: {
			positionClicked = -1;
			notifyDataSetChanged();
			ArrayList<Long> handleList = new ArrayList<Long>();
			handleList.add(n.getHandle());
			if (type == ManagerActivity.CONTACT_FILE_ADAPTER) {
				((ContactPropertiesMainActivity) context).onFileClick(handleList);
			} else if (type == ManagerActivity.FOLDER_LINK_ADAPTER) {
				((FolderLinkActivity) context).onFileClick(handleList);
			} else {
				((ManagerActivity) context).onFileClick(handleList);
			}
			break;
		}
		case R.id.file_list_option_properties: {
			Intent i = new Intent(context, FilePropertiesActivity.class);
			i.putExtra("handle", n.getHandle());

			if (n.isFolder()) {
				ArrayList<MegaShare> sl = megaApi.getOutShares(n);

				if (sl != null) {

					if (sl.size() > 0) {

						i.putExtra("imageId", R.drawable.mime_folder_shared);
					} else {
						i.putExtra("imageId", R.drawable.mime_folder);
					}
				} else {
					i.putExtra("imageId", R.drawable.mime_folder);
				}

			} else {
				i.putExtra("imageId", MimeType.typeForName(n.getName())
						.getIconResourceId());
			}
			i.putExtra("name", n.getName());
			context.startActivity(i);
			positionClicked = -1;
			notifyDataSetChanged();
			break;
		}
		case R.id.file_list_option_delete: {
			ArrayList<Long> handleList = new ArrayList<Long>();
			handleList.add(n.getHandle());
			setPositionClicked(-1);
			notifyDataSetChanged();
			if (type != ManagerActivity.CONTACT_FILE_ADAPTER) {
				((ManagerActivity) context).moveToTrash(handleList);
			} else {
				((ContactPropertiesMainActivity) context).moveToTrash(handleList);
			}
			break;
		}
		case R.id.file_list_option_public_link: {
			setPositionClicked(-1);
			notifyDataSetChanged();
			if ((type == ManagerActivity.FILE_BROWSER_ADAPTER)
					|| (type == ManagerActivity.SEARCH_ADAPTER)) {
				((ManagerActivity) context).getPublicLinkAndShareIt(n);
			}
			break;
		}
		case R.id.file_list_option_rename: {
			setPositionClicked(-1);
			notifyDataSetChanged();
			if (type != ManagerActivity.CONTACT_FILE_ADAPTER) {
				((ManagerActivity) context).showRenameDialog(n, n.getName());
			}
			if (type == ManagerActivity.CONTACT_FILE_ADAPTER) {
				((ContactPropertiesMainActivity) context).showRenameDialog(n,	n.getName());
			}

			break;
		}/*
		case R.id.file_list_option_move: {
			setPositionClicked(-1);
			notifyDataSetChanged();
			ArrayList<Long> handleList = new ArrayList<Long>();
			handleList.add(n.getHandle());
			if (type != ManagerActivity.CONTACT_FILE_ADAPTER) {
				((ManagerActivity) context).showMove(handleList);
			} else {
				((ContactPropertiesMainActivity) context).showMove(handleList);
			}
			break;
		}
		case R.id.file_list_option_copy: {
			positionClicked = -1;
			notifyDataSetChanged();
			ArrayList<Long> handleList = new ArrayList<Long>();
			handleList.add(n.getHandle());
			if (type != ManagerActivity.CONTACT_FILE_ADAPTER) {
				((ManagerActivity) context).showCopy(handleList);
			} else {
				((ContactPropertiesMainActivity) context).showCopy(handleList);
			}
			break;
		}*/
		case R.id.file_list_three_dots: {
			if (positionClicked == -1) {
				positionClicked = currentPosition;
				notifyDataSetChanged();
			} else {
				if (positionClicked == currentPosition) {
					positionClicked = -1;
					notifyDataSetChanged();
				} else {
					positionClicked = currentPosition;
					notifyDataSetChanged();
				}
			}
			break;
		}
		}
	}

	/*
	 * Get document at specified position
	 */
	public MegaNode getNodeAt(int position) {
		try {
			if (megaShareInList != null) {
				return megaShareInList.get(position).getNode();
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	public long getParentHandle() {
		return parentHandle;
	}

	public void setParentHandle(long parentHandle) {
		this.parentHandle = parentHandle;
		((ManagerActivity) context).setParentHandleSharedWithMe(parentHandle);
		
	}
	
	public boolean isMultipleSelect() {
//		return multipleSelect;
		return false;
	}

	public void setMultipleSelect(boolean multipleSelect) {
//		if (this.multipleSelect != multipleSelect) {
//			this.multipleSelect = multipleSelect;
//			notifyDataSetChanged();
//		}
	}

	public void setOrder(int orderGetChildren) {
		this.orderGetChildren = orderGetChildren;
	}
	
    public void setTransfers(HashMap<Long, MegaTransfer> _mTHash)
    {
    	this.mTHash = _mTHash;
    	notifyDataSetChanged();
    }
    
	public void setType(int _type) {
		this.type = _type;
	}
	
    public int getType()
    {
    	return this.type;
    }
    
    public void setCurrentTransfer(MegaTransfer mT)
    {
    	this.currentTransfer = mT;
    	MegaNode nodeT = megaApi.getNodeByHandle(mT.getNodeHandle());
    	if(megaApi.getParentNode(nodeT).getHandle()==parentHandle){    		
    		notifyDataSetChanged();    		
    	}
    	
    	if (type == ManagerActivity.SHARED_WITH_ME_ADAPTER){
    		notifyDataSetChanged();
    	}
    }   
    
	private static void log(String log) {
		Util.log("MegaShareInOutAdapter", log);
	}
}
