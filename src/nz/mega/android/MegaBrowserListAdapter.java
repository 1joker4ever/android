package nz.mega.android;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nz.mega.android.utils.PreviewUtils;
import nz.mega.android.utils.ThumbnailUtils;
import nz.mega.android.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MegaBrowserListAdapter extends BaseAdapter implements OnClickListener {

	private class Media {
		public String filePath;
		public long timestamp;
	}
	
	HashMap<Long, String> initDBHM(){
		HashMap<Long, String> hm = new HashMap<Long, String>();
		
		String projection[] = {	MediaColumns.DATA, 
				//MediaColumns.MIME_TYPE, 
				//MediaColumns.DATE_MODIFIED,
				MediaColumns.DATE_MODIFIED};
//		String selection = "(abs(" + MediaColumns.DATE_MODIFIED + "-" + n.getModificationTime() + ") < 3) OR ("+ MediaColumns.DATA + " LIKE '%" + n.getName() + "%')";
		String selection = "";
		log("SELECTION: " + selection);
		ArrayList<Uri> uris = new ArrayList<Uri>();
		uris.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		uris.add(MediaStore.Images.Media.INTERNAL_CONTENT_URI);	
		String order = MediaColumns.DATE_MODIFIED + " ASC";
		String[] selectionArgs = null;
		
		for(int i=0; i<uris.size(); i++){
			if (app == null){
				app = ((MegaApplication) ((Activity) context).getApplication());
			}
			Cursor cursor = app.getContentResolver().query(uris.get(i), projection, selection, selectionArgs, order);
			if (cursor != null){
				int dataColumn = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				int timestampColumn = cursor.getColumnIndexOrThrow(MediaColumns.DATE_MODIFIED);
				while(cursor.moveToNext()){
					Media media = new Media();
			        media.filePath = cursor.getString(dataColumn);
			        media.timestamp = cursor.getLong(timestampColumn);
			        
			        hm.put(media.timestamp, media.filePath);
				}
			}
		}
		
		return hm;	
	}
	
	HashMap<Long, String> hm;
	
	private class MediaDBTask extends AsyncTask<MegaNode, Void, String> {
		
		Context context;
		MegaApplication app;
		ViewHolderBrowserList holder;
		MegaApiAndroid megaApi;
		MegaBrowserListAdapter adapter;
		MegaNode node;
		Bitmap thumb = null;
		
		public MediaDBTask(Context context, ViewHolderBrowserList holder, MegaApiAndroid megaApi, MegaBrowserListAdapter adapter) {
			this.context = context;
			this.app = (MegaApplication)(((Activity)(this.context)).getApplication());
			this.holder = holder;
			this.megaApi = megaApi;
			this.adapter = adapter;
		}
		@Override
		protected String doInBackground(MegaNode... params) {
			this.node = params[0];
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (this.node == null){
				return null;
			}
			
			if (app == null){
				return null;
			}
			
			if (hm == null){
				return null;
			}
			
			boolean thumbCreated = false;
			boolean previewCreated = false;
			
			File previewDir = PreviewUtils.getPreviewFolder(context);
			File thumbDir = ThumbnailUtils.getThumbFolder(context);
			File previewFile = new File(previewDir, MegaApiAndroid.handleToBase64(node.getHandle())+".jpg");
			File thumbFile = new File(thumbDir, MegaApiAndroid.handleToBase64(node.getHandle())+".jpg");
							
			if (!thumbFile.exists()){
		
				log("n.getName(): " + node.getName() + "____" + node.getModificationTime());
				String filePath = hm.get(node.getModificationTime());
				if (filePath != null){
					File f = new File(filePath);
					if (f != null){
						if (f.length() == node.getSize()){
							log("IDEM: " + filePath + "____" + node.getName());
							thumbCreated = MegaUtils.createThumbnail(f, thumbFile);
							if (!node.hasThumbnail()){
								log("Upload thumbnail -> " + node.getName() + "___" + thumbFile.getAbsolutePath());
								megaApi.setThumbnail(node, thumbFile.getAbsolutePath());
							}
							else{
								log("Thumbnail OK: " + node.getName() + "___" + thumbFile.getAbsolutePath());
							}
							
							if (!previewFile.exists()){
								previewCreated = MegaUtils.createPreview(f, previewFile);
								if (!node.hasPreview()){
									log("Upload preview -> " + node.getName() + "___" + previewFile.getAbsolutePath());
									megaApi.setPreview(node, previewFile.getAbsolutePath());
								}
							}
							else{
								if (!node.hasPreview()){
									log("Upload preview -> " + node.getName() + "___" + previewFile.getAbsolutePath());
									megaApi.setPreview(node, previewFile.getAbsolutePath());
								}
							}
						}
					}
				}
				else{
					List<String> paths = new ArrayList<String>(hm.values());
					for (int i=0;i<paths.size();i++){
						if (paths.get(i).contains(node.getName())){
							filePath = paths.get(i);
							File f = new File(filePath);
							if (f != null){
								if (f.length() == node.getSize()){
									log("IDEM(por nombre): " + filePath + "____" + node.getName());
									thumbCreated = MegaUtils.createThumbnail(f, thumbFile);
									if (!node.hasThumbnail()){
										log("Upload thumbnail -> " + node.getName() + "___" + thumbFile.getAbsolutePath());
										megaApi.setThumbnail(node, thumbFile.getAbsolutePath());
									}
									else{
										log("Thumbnail OK: " + node.getName() + "___" + thumbFile.getAbsolutePath());
									}
									
									if (!previewFile.exists()){
										previewCreated = MegaUtils.createPreview(f, previewFile);
										if (!node.hasPreview()){
											log("Upload preview -> " + node.getName() + "___" + previewFile.getAbsolutePath());
											megaApi.setPreview(node, previewFile.getAbsolutePath());
										}
									}
									else{
										if (!node.hasPreview()){
											log("Upload preview -> " + node.getName() + "___" + previewFile.getAbsolutePath());
											megaApi.setPreview(node, previewFile.getAbsolutePath());
										}
									}
								}
							}
						}
					}
				}
			}
			else{
				thumbCreated = true;
				if (!node.hasThumbnail()){
					log("Upload thumbnail -> " + node.getName() + "___" + thumbFile.getAbsolutePath());
					megaApi.setThumbnail(node, thumbFile.getAbsolutePath());
				}
				else{
					log("Thumbnail OK: " + node.getName() + "___" + thumbFile.getAbsolutePath());
				}
			}
			
			if (!previewFile.exists()){
				log("n.getName(): " + node.getName() + "____" + node.getModificationTime());
				String filePath = hm.get(node.getModificationTime());
				if (filePath != null){
					File f = new File(filePath);
					if (f != null){
						if (f.length() == node.getSize()){
							log("IDEM: " + filePath + "____" + node.getName());
							previewCreated = MegaUtils.createPreview(f, previewFile);
							if (!node.hasPreview()){
								log("Upload preview -> " + node.getName() + "___" + previewFile.getAbsolutePath());
								megaApi.setPreview(node, previewFile.getAbsolutePath());
							}
							if (!thumbFile.exists()){
								thumbCreated = MegaUtils.createThumbnail(f, thumbFile);
								if (!node.hasThumbnail()){
									log("Upload thumbnail -> " + node.getName() + "___" + thumbFile.getAbsolutePath());
									megaApi.setThumbnail(node, thumbFile.getAbsolutePath());
								}
							}
							else{
								if (!node.hasThumbnail()){
									log("Upload thumbnail -> " + node.getName() + "___" + thumbFile.getAbsolutePath());
									megaApi.setThumbnail(node, thumbFile.getAbsolutePath());
								}
							}
						}
					}
				}
			}
			else{
				if (!node.hasPreview()){
					log("Upload preview -> " + node.getName() + "___" + previewFile.getAbsolutePath());
					megaApi.setPreview(node, previewFile.getAbsolutePath());
				}
			}
			
			if (thumbCreated){
				if (thumbFile != null){
					return thumbFile.getAbsolutePath();
				}
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String res) {
			if (res == null){
				log("megaApi.getThumbnail");
				if (this.node != null){
					try {
						thumb = ThumbnailUtils.getThumbnailFromMegaList(node, context, holder, megaApi, adapter);
					} 
					catch (Exception e) {
					} // Too many AsyncTasks
	
					if (thumb != null) {
						if(!multipleSelect){
							if ((holder.document == node.getHandle())){
								holder.imageView.setImageBitmap(thumb);
							}
						}
						else{
							if ((holder.document == node.getHandle())){
								holder.imageView.setImageBitmap(thumb);
							}
						}
					}
				}
			}
			else{
				log("From folder");
				if (this.node != null){
					thumb = ThumbnailUtils.getThumbnailFromCache(node);
					if (thumb != null) {
						if(!multipleSelect){
							if ((holder.document == node.getHandle())){
								holder.imageView.setImageBitmap(thumb);
							}
						}
						else{
							if ((holder.document == node.getHandle())){
								holder.imageView.setImageBitmap(thumb);
							}
						}
					} else {
						thumb = ThumbnailUtils
								.getThumbnailFromFolder(node, context);
						if (thumb != null) {
							if(!multipleSelect){
								if ((holder.document == node.getHandle())){
									holder.imageView.setImageBitmap(thumb);
								}
							}
							else{
								if ((holder.document == node.getHandle())){
									holder.imageView.setImageBitmap(thumb);
								}
							}
						}
					}
				}
				//HE ENCONTRADO LA IMAGEN Y LA PUEDO LEER
			}
//			onKeysGenerated(key[0], key[1]);
		}		
	}
	
	static int FROM_FILE_BROWSER = 13;
	static int FROM_INCOMING_SHARES= 14;
	static int FROM_OFFLINE= 15;

	Context context;
	MegaApplication app;
	MegaApiAndroid megaApi;

	int positionClicked;
	ArrayList<MegaNode> nodes;

	long parentHandle = -1;

	ListView listFragment;
//	ImageView emptyImageViewFragment;
//	TextView emptyTextViewFragment;
	ActionBar aB;
	HashMap<Long, MegaTransfer> mTHash = null;
	MegaTransfer currentTransfer = null;
	boolean incoming = false;
	DatabaseHandler dbH = null;
	boolean multipleSelect;
//	boolean overflowMenu = false;
	int type = ManagerActivity.FILE_BROWSER_ADAPTER;

	int orderGetChildren = MegaApiJava.ORDER_DEFAULT_ASC;

	/* public static view holder class */
	public class ViewHolderBrowserList {
		public CheckBox checkbox;
		public ImageView imageView;
		public TextView textViewFileName;
		public TextView textViewFileSize;
		public ImageView savedOffline;
//		public ImageView savedOfflineMultiselect;
		public ImageButton imageButtonThreeDots;
		public ImageView publicLinkImageMultiselect;
		public ImageView publicLinkImage;
		public RelativeLayout itemLayout;
		//public ImageView arrowSelection;
		public LinearLayout optionsLayout;
		public RelativeLayout optionDownload;
		public RelativeLayout optionProperties;
		public RelativeLayout optionMore;		
		public ProgressBar transferProgressBar;
		public RelativeLayout optionRename;
		public RelativeLayout optionPublicLink;
		public RelativeLayout optionShare;
		public RelativeLayout optionPermissions;
		public RelativeLayout optionDelete;
		public RelativeLayout optionRemoveTotal;
		public RelativeLayout optionClearShares;
		public RelativeLayout optionLeaveShare;
		public RelativeLayout optionMoveTo;
		public TextView propertiesText;
		public int currentPosition;
		public long document;
	}

	public MegaBrowserListAdapter(Context _context, ArrayList<MegaNode> _nodes,long _parentHandle, ListView listView, ActionBar aB, int type) {
		this.context = _context;
		this.nodes = _nodes;
		this.parentHandle = _parentHandle;
		this.type = type;
		
		switch (type) {
		case ManagerActivity.FILE_BROWSER_ADAPTER: {
			((ManagerActivity) context).setParentHandleBrowser(parentHandle);
			break;
		}
		case ManagerActivity.CONTACT_FILE_ADAPTER: {
			((ContactPropertiesMainActivity) context).setParentHandle(parentHandle);
			break;
		}
		case ManagerActivity.RUBBISH_BIN_ADAPTER: {
			((ManagerActivity) context).setParentHandleRubbish(parentHandle);
			break;
		}		
		case ManagerActivity.FOLDER_LINK_ADAPTER: {
			megaApi = ((MegaApplication) ((Activity) context).getApplication())
					.getMegaApiFolder();
			break;
		}
		case ManagerActivity.SEARCH_ADAPTER: {
			((ManagerActivity) context).setParentHandleSearch(parentHandle);
			break;
		}
		case ManagerActivity.OUTGOING_SHARES_ADAPTER: {
			((ManagerActivity) context).setParentHandleOutgoing(-1);
			break;
		}
		case ManagerActivity.INCOMING_SHARES_ADAPTER: {
			incoming=true;
			dbH = DatabaseHandler.getDbHandler(context);
			((ManagerActivity) context).setParentHandleIncoming(-1);
			break;
		}
		case ManagerActivity.INBOX_ADAPTER: {
			//Empty
			break;
		}
		default: {
			((ManagerActivity) context).setParentHandleBrowser(parentHandle);
			break;
		}
		}

		this.listFragment = listView;
//		this.emptyImageViewFragment = emptyImageView;
//		this.emptyTextViewFragment = emptyTextView;
		this.aB = aB;
		this.positionClicked = -1;
		this.type = type;		

		if (megaApi == null) {
			megaApi = ((MegaApplication) ((Activity) context).getApplication())
					.getMegaApi();
		}
		
		this.app = ((MegaApplication) ((Activity) context).getApplication());
		this.hm = initDBHM();
	}

	public void setNodes(ArrayList<MegaNode> nodes) {
		this.nodes = nodes;
//		contentTextFragment.setText(getInfoFolder(node));
		positionClicked = -1;
		notifyDataSetChanged();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		log("getView");

		listFragment = (ListView) parent;
		final int _position = position;	
		
		ViewHolderBrowserList holder = null;

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = ((Activity) context).getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_file_list, parent,false);
			holder = new ViewHolderBrowserList();
			holder.itemLayout = (RelativeLayout) convertView.findViewById(R.id.file_list_item_layout);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.file_list_checkbox);
			holder.checkbox.setClickable(false);
			holder.imageView = (ImageView) convertView.findViewById(R.id.file_list_thumbnail);
			holder.savedOffline = (ImageView) convertView.findViewById(R.id.file_list_saved_offline);
			
			holder.publicLinkImageMultiselect = (ImageView) convertView.findViewById(R.id.file_list_public_link_multiselect);
			holder.publicLinkImage = (ImageView) convertView.findViewById(R.id.file_list_public_link);
			
			holder.textViewFileName = (TextView) convertView.findViewById(R.id.file_list_filename);			
			holder.textViewFileName.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			holder.textViewFileName.getLayoutParams().width = Util.px2dp((225 * scaleW), outMetrics);
			holder.textViewFileSize = (TextView) convertView.findViewById(R.id.file_list_filesize);
			holder.transferProgressBar = (ProgressBar) convertView.findViewById(R.id.transfers_list__browser_bar);
			
			holder.imageButtonThreeDots = (ImageButton) convertView.findViewById(R.id.file_list_three_dots);
			holder.optionsLayout = (LinearLayout) convertView.findViewById(R.id.file_list_options);
			holder.optionRename = (RelativeLayout) convertView.findViewById(R.id.file_list_option_rename_layout);
			holder.optionRename.setVisibility(View.GONE);
			holder.optionLeaveShare = (RelativeLayout) convertView.findViewById(R.id.file_list_option_leave_share_layout);
			holder.optionLeaveShare.setVisibility(View.GONE);
			
			holder.optionDownload = (RelativeLayout) convertView.findViewById(R.id.file_list_option_download_layout);
			holder.optionProperties = (RelativeLayout) convertView.findViewById(R.id.file_list_option_properties_layout);
			holder.propertiesText = (TextView) convertView.findViewById(R.id.file_list_option_properties_text);			

			holder.optionPublicLink = (RelativeLayout) convertView.findViewById(R.id.file_list_option_public_link_layout);
//			holder.optionPublicLink.getLayoutParams().width = Util.px2dp((60), outMetrics);
//			((LinearLayout.LayoutParams) holder.optionPublicLink.getLayoutParams()).setMargins(Util.px2dp((17 * scaleW), outMetrics),Util.px2dp((4 * scaleH), outMetrics), 0, 0);

			holder.optionShare = (RelativeLayout) convertView.findViewById(R.id.file_list_option_share_layout);
			holder.optionPermissions = (RelativeLayout) convertView.findViewById(R.id.file_list_option_permissions_layout);
			
			holder.optionDelete = (RelativeLayout) convertView.findViewById(R.id.file_list_option_delete_layout);			
			holder.optionRemoveTotal = (RelativeLayout) convertView.findViewById(R.id.file_list_option_remove_layout);

//			holder.optionDelete.getLayoutParams().width = Util.px2dp((60 * scaleW), outMetrics);
//			((LinearLayout.LayoutParams) holder.optionDelete.getLayoutParams()).setMargins(Util.px2dp((1 * scaleW), outMetrics),Util.px2dp((5 * scaleH), outMetrics), 0, 0);

			holder.optionClearShares = (RelativeLayout) convertView.findViewById(R.id.file_list_option_clear_share_layout);	
			holder.optionMoveTo = (RelativeLayout) convertView.findViewById(R.id.file_list_option_move_layout);		
			
			holder.optionMore = (RelativeLayout) convertView.findViewById(R.id.file_list_option_overflow_layout);
//			holder.optionMore.getLayoutParams().width = Util.px2dp((60 * scaleW), outMetrics);
//			((LinearLayout.LayoutParams) holder.optionMore.getLayoutParams()).setMargins(Util.px2dp((1 * scaleW), outMetrics),Util.px2dp((5 * scaleH), outMetrics), 0, 0);
						
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderBrowserList) convertView.getTag();
		}
	
		holder.optionShare.setVisibility(View.GONE);
		holder.optionPermissions.setVisibility(View.GONE);
		holder.savedOffline.setVisibility(View.INVISIBLE);
//		holder.savedOfflineMultiselect.setVisibility(View.GONE);
		holder.publicLinkImage.setVisibility(View.GONE);
		holder.publicLinkImageMultiselect.setVisibility(View.GONE);
		
		holder.transferProgressBar.setVisibility(View.GONE);
		holder.textViewFileSize.setVisibility(View.VISIBLE);

		holder.currentPosition = position;

		MegaNode node = (MegaNode) getItem(position);
		holder.document = node.getHandle();
		Bitmap thumb = null;

		holder.textViewFileName.setText(node.getName());
		
		if (!multipleSelect) {
			holder.checkbox.setVisibility(View.GONE);
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
	
		} else {
			holder.checkbox.setVisibility(View.VISIBLE);
			holder.imageButtonThreeDots.setVisibility(View.GONE);
			
			SparseBooleanArray checkedItems = listFragment.getCheckedItemPositions();
			if (checkedItems.get(position, false) == true) {
				holder.checkbox.setChecked(true);					
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
				

			} else {
				holder.checkbox.setChecked(false);
			}
		}
	
		holder.textViewFileSize.setText("");
		if (node.isFolder()) {
			holder.propertiesText.setText(R.string.general_folder_info);
			holder.textViewFileSize.setText(getInfoFolder(node));

			ArrayList<MegaShare> sl = megaApi.getOutShares(node);
			if (sl != null) {
				if (sl.size() > 0) {
					if(sl.size() == 1){
						if(sl.get(0).getUser()==null){
							//IT is just public link, not shared folder
							holder.imageView.setImageResource(R.drawable.ic_folder_list);
						}
						else{
							holder.imageView.setImageResource(R.drawable.ic_folder_shared_list);
						}
					}
					else{
						holder.imageView.setImageResource(R.drawable.ic_folder_shared_list);
					}
				} else {
					holder.imageView.setImageResource(R.drawable.ic_folder_list);
				}
			} else {
				holder.imageView.setImageResource(R.drawable.ic_folder_list);
			}
		} 
		else {
			holder.propertiesText.setText(R.string.general_file_info);
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

			holder.imageView.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());

			if (node.hasThumbnail()) {
				thumb = ThumbnailUtils.getThumbnailFromCache(node);
				if (thumb != null) {
					if(!multipleSelect){
						holder.imageView.setImageBitmap(thumb);
					}
					else{
						holder.imageView.setImageBitmap(thumb);
					}
				} else {
					thumb = ThumbnailUtils
							.getThumbnailFromFolder(node, context);
					if (thumb != null) {
						if(!multipleSelect){
							holder.imageView.setImageBitmap(thumb);
						}
						else{
							holder.imageView.setImageBitmap(thumb);
						}
					} else {
//						new MediaDBTask(context, holder, megaApi, this).execute(node);
						try {
							log("megaApi.getThumbnail");
							thumb = ThumbnailUtils.getThumbnailFromMegaList(
									node, context, holder, megaApi, this);
						} catch (Exception e) {
						} // Too many AsyncTasks
		
						if (thumb != null) {
							if(!multipleSelect){
								holder.imageView.setImageBitmap(thumb);
							}
							else{
								holder.imageView.setImageBitmap(thumb);
							}
						}
					}
				}
			} else {
				thumb = ThumbnailUtils.getThumbnailFromCache(node);
				if (thumb != null) {
					if(!multipleSelect){
						holder.imageView.setImageBitmap(thumb);
					}
					else{
						holder.imageView.setImageBitmap(thumb);
					}
				} else {
					thumb = ThumbnailUtils
							.getThumbnailFromFolder(node, context);
					if (thumb != null) {
						if(!multipleSelect){
							holder.imageView.setImageBitmap(thumb);
						}
						else{
							holder.imageView.setImageBitmap(thumb);
						}
					} else {
						try {
							ThumbnailUtils.createThumbnailList(context, node,holder, megaApi, this);
						} catch (Exception e) {
						} // Too many AsyncTasks
					}
				}
			}
		}
		
		File offlineDirectory = null;
		
		if(incoming){
			log("Incoming tab: MegaBrowserListAdapter: "+node.getHandle());
			//Find in the database
			MegaOffline offlineNode = dbH.findByHandle(node.getHandle());
			if(offlineNode!=null){
				//Find in the filesystem
				if (Environment.getExternalStorageDirectory() != null){
					offlineDirectory = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" + offlineNode.getHandleIncoming());
					log("offline Directory: "+offlineDirectory.getAbsolutePath());
				}
				else{
					offlineDirectory = context.getFilesDir();
				}
			}
		}
		else{
			//Find in the filesystem
			if (Environment.getExternalStorageDirectory() != null){
				offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR + "/" +megaApi.getNodePath(node));
			}
			else{
				offlineDirectory = context.getFilesDir();
			}
		}		
		
		if (offlineDirectory!=null){
			if (offlineDirectory.exists()){
				if(multipleSelect){
					holder.savedOffline.setVisibility(View.VISIBLE);
				}
				else{
					holder.savedOffline.setVisibility(View.VISIBLE);
				}
			}
		}
		
		ArrayList<MegaShare> sl = megaApi.getOutShares(node);		

		if (sl != null && sl.size() != 0){
			
			for(int i=0; i<sl.size();i++){

				//Check if one of the ShareNodes is the public link

				if(sl.get(i).getUser()==null){

					if(multipleSelect){
						holder.publicLinkImageMultiselect.setVisibility(View.VISIBLE);
						holder.publicLinkImage.setVisibility(View.GONE);
					}
					else
					{
						holder.publicLinkImageMultiselect.setVisibility(View.GONE);
						holder.publicLinkImage.setVisibility(View.VISIBLE);
					}
					//
					break;

				}
			}
		}

		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);
		
		holder.optionClearShares.setTag(holder);
		holder.optionClearShares.setOnClickListener(this);

		holder.optionPermissions.setTag(holder);
		holder.optionPermissions.setOnClickListener(this);
		
		holder.optionLeaveShare.setTag(holder);
		holder.optionLeaveShare.setOnClickListener(this);
				
		if (positionClicked != -1) {
			if (positionClicked == position) {
				//				holder.arrowSelection.setVisibility(View.VISIBLE);
//				holder.optionsLayout.setVisibility(View.GONE);
				LayoutParams params = holder.optionsLayout.getLayoutParams();
				params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
				holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
				listFragment.smoothScrollToPosition(_position);
				
				if (type == ManagerActivity.FILE_BROWSER_ADAPTER) {
					//Visible
					log("ManagerActivity.FILE_BROWSER_ADAPTER");
					holder.optionDownload.setVisibility(View.VISIBLE);
					holder.optionProperties.setVisibility(View.VISIBLE);				
					holder.optionDelete.setVisibility(View.VISIBLE);
					holder.optionPublicLink.setVisibility(View.VISIBLE);
					holder.optionMore.setVisibility(View.VISIBLE);
					//Hide
					holder.optionClearShares.setVisibility(View.GONE);
					holder.optionRemoveTotal.setVisibility(View.GONE);
					holder.optionShare.setVisibility(View.GONE);
					holder.optionPermissions.setVisibility(View.GONE);
					holder.optionMoveTo.setVisibility(View.GONE);
					
//					holder.optionDownload.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionDownload.getLayoutParams()).setMargins(Util.px2dp((20 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);
//					holder.optionProperties.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);			log("clickado:BLANCO "+ holder.currentPosition);
//					((LinearLayout.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);						
//					holder.optionPublicLink.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionPublicLink.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);
//					holder.optionDelete.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionDelete.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);					
//					holder.optionMore.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionMore.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);
					
				}	
				else if (type == ManagerActivity.INBOX_ADAPTER) {
					//Visible
					log("ManagerActivity.INBOX_ADAPTER");
					holder.optionDownload.setVisibility(View.VISIBLE);
					holder.optionProperties.setVisibility(View.VISIBLE);				
					holder.optionDelete.setVisibility(View.VISIBLE);					
					holder.optionMore.setVisibility(View.VISIBLE);
					//Hide
					holder.optionClearShares.setVisibility(View.GONE);
					holder.optionPublicLink.setVisibility(View.GONE);
					holder.optionRemoveTotal.setVisibility(View.GONE);
					holder.optionShare.setVisibility(View.GONE);
					holder.optionPermissions.setVisibility(View.GONE);
					holder.optionMoveTo.setVisibility(View.GONE);
				}
				else if (type == ManagerActivity.SEARCH_ADAPTER){
					holder.optionDownload.setVisibility(View.VISIBLE);
					holder.optionProperties.setVisibility(View.VISIBLE);	
					holder.optionPublicLink.setVisibility(View.VISIBLE);
					holder.optionRename.setVisibility(View.GONE);
					holder.optionDelete.setVisibility(View.GONE);
					holder.optionRemoveTotal.setVisibility(View.GONE);
					holder.optionClearShares.setVisibility(View.GONE);
					holder.optionMore.setVisibility(View.GONE);
					holder.optionMoveTo.setVisibility(View.VISIBLE);
					holder.optionLeaveShare.setVisibility(View.GONE);
					holder.optionPermissions.setVisibility(View.GONE);
						
				}
				else if ((type == ManagerActivity.CONTACT_FILE_ADAPTER) || (type == ManagerActivity.INCOMING_SHARES_ADAPTER)) {

					// Choose the buttons to show depending on the type of
					// folder
					
					MegaNode n = (MegaNode) getItem(positionClicked);
					MegaNode folder = null;

					if (n.isFile())
						folder = megaApi.getParentNode(n);
					else
						folder = n;

					int accessLevel = megaApi.getAccess(folder);
					log("Node: "+folder.getName());
					log("ManagerActivity.CONTACT_FILE_ADAPTER: "+accessLevel);					
					
					switch (accessLevel) {
					case MegaShare.ACCESS_FULL: {

						holder.optionDownload.setVisibility(View.VISIBLE);
						holder.optionProperties.setVisibility(View.VISIBLE);
						
						if(node.isFile()){
							holder.optionLeaveShare.setVisibility(View.GONE);
						}
						else {
							holder.optionLeaveShare.setVisibility(View.VISIBLE);
						}
						holder.optionPublicLink.setVisibility(View.GONE);
						holder.optionRemoveTotal.setVisibility(View.GONE);
						holder.optionClearShares.setVisibility(View.GONE);
						holder.optionRename.setVisibility(View.VISIBLE);
						holder.optionDelete.setVisibility(View.GONE);
						holder.optionMoveTo.setVisibility(View.GONE);

						break;
					}
					case MegaShare.ACCESS_READ: {
						log("read");
						holder.optionDownload.setVisibility(View.VISIBLE);
						holder.optionProperties.setVisibility(View.VISIBLE);	
						holder.optionPublicLink.setVisibility(View.GONE);
						holder.optionRename.setVisibility(View.GONE);
						holder.optionDelete.setVisibility(View.GONE);
						holder.optionRemoveTotal.setVisibility(View.GONE);
						holder.optionClearShares.setVisibility(View.GONE);
						holder.optionMore.setVisibility(View.GONE);
						holder.optionMoveTo.setVisibility(View.GONE);
						
						if(node.isFile()){
							holder.optionLeaveShare.setVisibility(View.GONE);
						}
						else {
							holder.optionLeaveShare.setVisibility(View.VISIBLE);
						}						
						break;
					}
					case MegaShare.ACCESS_READWRITE: {
						log("readwrite");
						holder.optionDownload.setVisibility(View.VISIBLE);
						holder.optionProperties.setVisibility(View.VISIBLE);
						//						holder.shareDisabled.setVisibility(View.VISIBLE);
						holder.optionPublicLink.setVisibility(View.GONE);
						holder.optionRename.setVisibility(View.VISIBLE);
						holder.optionDelete.setVisibility(View.GONE);
						holder.optionRemoveTotal.setVisibility(View.GONE);
						holder.optionClearShares.setVisibility(View.GONE);
						holder.optionMoveTo.setVisibility(View.GONE);
						if(node.isFile()){
							holder.optionLeaveShare.setVisibility(View.GONE);
						}
						else {
							holder.optionLeaveShare.setVisibility(View.VISIBLE);
						}
						
						holder.optionMore.setVisibility(View.VISIBLE);

						break;
					}
					}
				} 
				else if (type == ManagerActivity.OUTGOING_SHARES_ADAPTER) {
					
					holder.imageView.setImageResource(R.drawable.folder_shared_mime);
					
					//TODO Tengo que comprobar el parentHandle
					//Visible
					holder.optionDownload.setVisibility(View.VISIBLE);
					holder.optionProperties.setVisibility(View.VISIBLE);
					//holder.shareDisabled.setVisibility(View.VISIBLE);					
					holder.optionPermissions.setVisibility(View.VISIBLE);
					holder.optionClearShares.setVisibility(View.VISIBLE);
					holder.optionMore.setVisibility(View.VISIBLE);
					
					//HIDE
					holder.optionShare.setVisibility(View.GONE);
					holder.optionPublicLink.setVisibility(View.GONE);					
					holder.optionDelete.setVisibility(View.GONE);		
					holder.optionRemoveTotal.setVisibility(View.GONE);
					holder.optionRename.setVisibility(View.GONE);
					holder.optionMoveTo.setVisibility(View.GONE);

//					holder.optionDownload.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionDownload.getLayoutParams()).setMargins(Util.px2dp((20 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);
//					holder.optionProperties.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);						
//					holder.optionPermissions.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionPermissions.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);
//					holder.optionClearShares.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionClearShares.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);					
//					holder.optionMore.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionMore.getLayoutParams()).setMargins(Util.px2dp((25 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);					
					
				}
				else if (type == ManagerActivity.RUBBISH_BIN_ADAPTER) {

					holder.optionDownload.setVisibility(View.GONE);
					holder.optionProperties.setVisibility(View.VISIBLE);
					holder.optionPublicLink.setVisibility(View.GONE);
					holder.optionClearShares.setVisibility(View.GONE);
					holder.optionRename.setVisibility(View.GONE);
					holder.optionMore.setVisibility(View.GONE);
					holder.optionRename.setVisibility(View.GONE);
					holder.optionDelete.setVisibility(View.GONE);
					holder.optionRemoveTotal.setVisibility(View.VISIBLE);
					holder.optionMoveTo.setVisibility(View.VISIBLE);

//					holder.optionDownload.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionDownload.getLayoutParams()).setMargins(Util.px2dp((9 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);
//					holder.optionProperties.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionProperties.getLayoutParams()).setMargins(Util.px2dp((17 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);
//					holder.optionRename.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionRename
//							.getLayoutParams()).setMargins(	Util.px2dp((17 * scaleW), outMetrics),
//									Util.px2dp((4 * scaleH), outMetrics), 0, 0);
//					holder.optionDelete.getLayoutParams().width = Util.px2dp((44 * scaleW), outMetrics);
//					((LinearLayout.LayoutParams) holder.optionDelete.getLayoutParams()).setMargins(Util.px2dp((17 * scaleW), outMetrics),
//							Util.px2dp((4 * scaleH), outMetrics), 0, 0);

				} 
				else if (type == ManagerActivity.FOLDER_LINK_ADAPTER) {

					holder.optionDownload.setVisibility(View.VISIBLE);
					holder.optionProperties.setVisibility(View.GONE);
					holder.optionPublicLink.setVisibility(View.GONE);
					holder.optionRename.setVisibility(View.GONE);
					holder.optionDelete.setVisibility(View.GONE);
					holder.optionRemoveTotal.setVisibility(View.GONE);
					holder.optionClearShares.setVisibility(View.GONE);
					holder.optionMoveTo.setVisibility(View.GONE);

//					holder.optionDownload.getLayoutParams().width = Util.px2dp(
//							(335 * scaleW), outMetrics);
//					((RelativeLayout.LayoutParams) holder.optionDownload
//							.getLayoutParams()).setMargins(
//									Util.px2dp((9 * scaleW), outMetrics),
//									Util.px2dp((4 * scaleH), outMetrics), 0, 0);
				}
			} else {
				//				holder.arrowSelection.setVisibility(View.GONE);
				LayoutParams params = holder.optionsLayout.getLayoutParams();
				params.height = 0;
				holder.itemLayout.setBackgroundColor(Color.WHITE);
				holder.imageButtonThreeDots
				.setImageResource(R.drawable.action_selector_ic);
			}
		} else {
			//			holder.arrowSelection.setVisibility(View.GONE);
			LayoutParams params = holder.optionsLayout.getLayoutParams();
			params.height = 0;
			holder.itemLayout.setBackgroundColor(Color.WHITE);
			holder.imageButtonThreeDots
			.setImageResource(R.drawable.action_selector_ic);
		}

		holder.optionDownload.setTag(holder);
		holder.optionDownload.setOnClickListener(this);
		
		holder.optionShare.setTag(holder);
		holder.optionShare.setOnClickListener(this);

		holder.optionProperties.setTag(holder);
		holder.optionProperties.setOnClickListener(this);

		holder.optionRename.setTag(holder);
		holder.optionRename.setOnClickListener(this);

		holder.optionDelete.setTag(holder);
		holder.optionDelete.setOnClickListener(this);
		
		holder.optionRemoveTotal.setTag(holder);
		holder.optionRemoveTotal.setOnClickListener(this);

		holder.optionPublicLink.setTag(holder);
		holder.optionPublicLink.setOnClickListener(this);
		
		holder.optionMore.setTag(holder);
		holder.optionMore.setOnClickListener(this);
		
		holder.optionMoveTo.setTag(holder);
		holder.optionMoveTo.setOnClickListener(this);
		
		return convertView;
	}
	
	private String getInfoFolder(MegaNode n) {
		int numFolders = megaApi.getNumChildFolders(n);
		int numFiles = megaApi.getNumChildFiles(n);

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
		return nodes.size();
	}

	@Override
	public Object getItem(int position) {
		return nodes.get(position);
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
		ViewHolderBrowserList holder = (ViewHolderBrowserList) v.getTag();
		int currentPosition = holder.currentPosition;
		final MegaNode n = (MegaNode) getItem(currentPosition);

		switch (v.getId()) {
		case R.id.file_list_option_download_layout: {
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
		case R.id.file_list_option_leave_share_layout: {
			positionClicked = -1;	
			notifyDataSetChanged();
			if (type == ManagerActivity.CONTACT_FILE_ADAPTER) {
				((ContactPropertiesMainActivity) context).leaveIncomingShare(n);
			}
			else
			{
				((ManagerActivity) context).leaveIncomingShare(n);
			}			
			//Toast.makeText(context, context.getString(R.string.general_not_yet_implemented), Toast.LENGTH_LONG).show();
			break;
		}
		case R.id.file_list_option_move_layout:{
			if (type == ManagerActivity.RUBBISH_BIN_ADAPTER) {
				positionClicked = -1;
				notifyDataSetChanged();
				ArrayList<Long> handleList = new ArrayList<Long>();
				handleList.add(n.getHandle());
				((ManagerActivity) context).showMove(handleList);
			} 
			break;
		}
		case R.id.file_list_option_properties_layout: {
			Intent i = new Intent(context, FilePropertiesActivity.class);
			i.putExtra("handle", n.getHandle());
			
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
			if (type == ManagerActivity.INCOMING_SHARES_ADAPTER){
				i.putExtra("from", FROM_INCOMING_SHARES);
			}
			else{
				i.putExtra("from", FROM_FILE_BROWSER);
			}
			context.startActivity(i);
			positionClicked = -1;
			notifyDataSetChanged();
			break;
		}
		case R.id.file_list_option_clear_share_layout: {
			if (type == ManagerActivity.OUTGOING_SHARES_ADAPTER){
				ArrayList<MegaShare> shareList = megaApi.getOutShares(n);				
				((ManagerActivity) context).removeAllSharingContacts(shareList, n);
				//break;
			}
		}
		case R.id.file_list_option_remove_layout: {
			ArrayList<Long> handleList = new ArrayList<Long>();
			handleList.add(n.getHandle());
			setPositionClicked(-1);
			notifyDataSetChanged();
			if (type == ManagerActivity.OUTGOING_SHARES_ADAPTER){
				ArrayList<MegaShare> shareList = megaApi.getOutShares(n);				
				((ManagerActivity) context).removeAllSharingContacts(shareList, n);
				//break;
			}
			else if (type != ManagerActivity.CONTACT_FILE_ADAPTER ) {
				((ManagerActivity) context).moveToTrash(handleList);
				//break;
			} 
			else {
				((ContactPropertiesMainActivity) context).moveToTrash(handleList);
				//break;
			}
			break;
		}
		case R.id.file_list_option_delete_layout: {
			ArrayList<Long> handleList = new ArrayList<Long>();
			handleList.add(n.getHandle());
			setPositionClicked(-1);
			notifyDataSetChanged();
			if (type == ManagerActivity.OUTGOING_SHARES_ADAPTER){
				ArrayList<MegaShare> shareList = megaApi.getOutShares(n);				
				((ManagerActivity) context).removeAllSharingContacts(shareList, n);
				//break;
			}
			else if (type != ManagerActivity.CONTACT_FILE_ADAPTER ) {
				((ManagerActivity) context).moveToTrash(handleList);
				//break;
			} 
			else {
				((ContactPropertiesMainActivity) context).moveToTrash(handleList);
				//break;
			}
			break;
		}
		case R.id.file_list_option_public_link_layout: {
			setPositionClicked(-1);
			notifyDataSetChanged();
			if ((type == ManagerActivity.FILE_BROWSER_ADAPTER)
					|| (type == ManagerActivity.SEARCH_ADAPTER)) {
				((ManagerActivity) context).getPublicLinkAndShareIt(n);
			}
			break;
		}
		case R.id.file_list_option_rename_layout: {
			if (type == ManagerActivity.CONTACT_FILE_ADAPTER){
				((ContactPropertiesMainActivity) context).showRenameDialog(n, n.getName());
			}
			else if (type == ManagerActivity.INCOMING_SHARES_ADAPTER){
				((ManagerActivity) context).showRenameDialog(n, n.getName());
			}
			break;
		}		
		case R.id.file_list_option_overflow_layout: {

			if ((type == ManagerActivity.FILE_BROWSER_ADAPTER)	|| (type == ManagerActivity.SEARCH_ADAPTER) || (type == ManagerActivity.OUTGOING_SHARES_ADAPTER)) {
//				((ManagerActivity) context).showOverflowMenu(n);
				AlertDialog moreOptionsDialog;
				
				String [] optionsString = null;
				if (n.isFolder()){
					optionsString = new String[] {context.getString(R.string.context_share_folder), context.getString(R.string.context_rename), context.getString(R.string.context_move), context.getString(R.string.context_copy)}; 
				}
				else{
					optionsString = new String[] {context.getString(R.string.context_rename), context.getString(R.string.context_move), context.getString(R.string.context_copy), context.getString(R.string.context_send_file_inbox)};
				}
				
				final ListAdapter adapter = new ArrayAdapter<String>(context, R.layout.select_dialog_text, android.R.id.text1, optionsString);
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.more_options_overflow);
				builder.setSingleChoiceItems(adapter,  0,  new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (n.isFile()){
							which = which + 1;
						}
						switch (which){
							case 0:{
								setPositionClicked(-1);
								notifyDataSetChanged();									
								((ManagerActivity) context).shareFolder(n);
								break;
							}
							case 1:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								((ManagerActivity) context).showRenameDialog(n, n.getName());
								break;
							}
							case 2:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ManagerActivity) context).showMove(handleList);
								break;
							}
							case 3:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ManagerActivity) context).showCopy(handleList);
								break;
							}
							case 4:{
								log("Selected send file");
								setPositionClicked(-1);
								notifyDataSetChanged();
								((ManagerActivity) context).sentToInbox(n);
								break;
							}
						}

						dialog.dismiss();
					}
				});
				
				builder.setPositiveButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				moreOptionsDialog = builder.create();
				moreOptionsDialog.show();
				Util.brandAlertDialog(moreOptionsDialog);
			}
			
			else if (type == ManagerActivity.INCOMING_SHARES_ADAPTER) {
//				((ManagerActivity) context).showOverflowMenu(n);
				AlertDialog moreOptionsDialog;
				
				final ListAdapter adapter = new ArrayAdapter<String>(context, R.layout.select_dialog_text, android.R.id.text1, new String[] {context.getString(R.string.context_move), context.getString(R.string.context_copy)});
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.more_options_overflow);
				builder.setSingleChoiceItems(adapter,  0,  new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which){
							case 0:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ManagerActivity) context).showMove(handleList);
								break;
							}
							case 1:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ManagerActivity) context).showCopy(handleList);
								break;
							}
						}

						dialog.dismiss();
					}
				});
				
				builder.setPositiveButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				moreOptionsDialog = builder.create();
				moreOptionsDialog.show();
				Util.brandAlertDialog(moreOptionsDialog);
			}
			
			else if (type == ManagerActivity.CONTACT_FILE_ADAPTER) {
//				((ManagerActivity) context).showOverflowMenu(n);
				AlertDialog moreOptionsDialog;
				
				final ListAdapter adapter = new ArrayAdapter<String>(context, R.layout.select_dialog_text, android.R.id.text1, new String[] {context.getString(R.string.context_move), context.getString(R.string.context_copy)});
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.more_options_overflow);
				builder.setSingleChoiceItems(adapter,  0,  new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which){
							case 0:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ContactPropertiesMainActivity) context).showMove(handleList);
								break;
							}
							case 1:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ContactPropertiesMainActivity) context).showCopy(handleList);
								break;
							}
						}

						dialog.dismiss();
					}
				});
				
				builder.setPositiveButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				moreOptionsDialog = builder.create();
				moreOptionsDialog.show();
				Util.brandAlertDialog(moreOptionsDialog);
			}
			else if ((type == ManagerActivity.INBOX_ADAPTER)) {
//				((ManagerActivity) context).showOverflowMenu(n);
				AlertDialog moreOptionsDialog;
				
				String [] optionsString = null;

				optionsString = new String[] {context.getString(R.string.context_rename), context.getString(R.string.context_move), context.getString(R.string.context_copy)};				
				
				final ListAdapter adapter = new ArrayAdapter<String>(context, R.layout.select_dialog_text, android.R.id.text1, optionsString);
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.more_options_overflow);
				builder.setSingleChoiceItems(adapter,  0,  new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						switch (which){
							
							case 0:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								((ManagerActivity) context).showRenameDialog(n, n.getName());
								break;
							}
							case 1:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ManagerActivity) context).showMove(handleList);
								break;
							}
							case 2:{
								setPositionClicked(-1);
								notifyDataSetChanged();
								ArrayList<Long> handleList = new ArrayList<Long>();
								handleList.add(n.getHandle());									
								((ManagerActivity) context).showCopy(handleList);
								break;
							}
						}

						dialog.dismiss();
					}
				});
				
				builder.setPositiveButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				moreOptionsDialog = builder.create();
				moreOptionsDialog.show();
				Util.brandAlertDialog(moreOptionsDialog);
			}
			break;
		}	
				
		case R.id.file_list_option_permissions_layout: {
			Intent i = new Intent(context, FileContactListActivity.class);
			i.putExtra("name", n.getHandle());
			context.startActivity(i);			
			break;
		}	
		
		case R.id.file_list_option_share_layout: {			
			
			if (type == ManagerActivity.OUTGOING_SHARES_ADAPTER){
				if(n.isFolder()){
					((ManagerActivity) context).shareFolder(n);
				}
			}			
			break;
		}	
		
		/*
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
	public MegaNode getDocumentAt(int position) {
		try {
			if (nodes != null) {
				return nodes.get(position);
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
		switch (type) {
		case ManagerActivity.FILE_BROWSER_ADAPTER: {
			log("setParentHandleBrowser -FILE_BROWSER_ADAPTER");
			((ManagerActivity) context).setParentHandleBrowser(parentHandle);
			break;
		}
		case ManagerActivity.CONTACT_FILE_ADAPTER: {
			((ContactPropertiesMainActivity) context).setParentHandle(parentHandle);
			break;
		}
		case ManagerActivity.RUBBISH_BIN_ADAPTER: {
			((ManagerActivity) context).setParentHandleRubbish(parentHandle);
			break;
		}
		case ManagerActivity.FOLDER_LINK_ADAPTER: {
			break;
		}
		case ManagerActivity.SEARCH_ADAPTER: {
			((ManagerActivity) context).setParentHandleSearch(parentHandle);
			break;
		}
		case ManagerActivity.INCOMING_SHARES_ADAPTER: {
			//TODO necesito algo?
			((ManagerActivity) context).setParentHandleIncoming(parentHandle);
			break;
		}
		case ManagerActivity.OUTGOING_SHARES_ADAPTER: {
			log("setParentHandleBrowser -ManagerActivity.OUTGOING_SHARES_ADAPTER");
			//TODO necesito algo?
			((ManagerActivity) context).setParentHandleOutgoing(parentHandle);
			break;
		}
		default: {
			log("setParentHandleBrowser -default");
			((ManagerActivity) context).setParentHandleBrowser(parentHandle);
			break;
		}
		}
	}

	public boolean isMultipleSelect() {
		return multipleSelect;
	}

	public void setMultipleSelect(boolean multipleSelect) {
		if (this.multipleSelect != multipleSelect) {
			this.multipleSelect = multipleSelect;
			notifyDataSetChanged();
		}
	}

	public void setOrder(int orderGetChildren) {
		this.orderGetChildren = orderGetChildren;
	}

	public void setTransfers(HashMap<Long, MegaTransfer> _mTHash)
	{
		this.mTHash = _mTHash;
		notifyDataSetChanged();
	}

	public void setCurrentTransfer(MegaTransfer mT)
	{
		this.currentTransfer = mT;
		MegaNode nodeT = megaApi.getNodeByHandle(mT.getNodeHandle());
		if (megaApi.getParentNode(nodeT) != null){
			if(megaApi.getParentNode(nodeT).getHandle()==parentHandle){    		
				notifyDataSetChanged();    		
			}
		}

		if (type == ManagerActivity.SHARED_WITH_ME_ADAPTER){
			notifyDataSetChanged();
		}
	}   

	private static void log(String log) {
		Util.log("MegaBrowserListAdapter", log);
	}
}

