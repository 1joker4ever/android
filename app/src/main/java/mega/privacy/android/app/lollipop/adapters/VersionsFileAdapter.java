package mega.privacy.android.app.lollipop.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.scrollBar.SectionTitleProvider;
import mega.privacy.android.app.lollipop.VersionsFileActivity;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaNode;

public class VersionsFileAdapter extends RecyclerView.Adapter<VersionsFileAdapter.ViewHolderVersion> implements OnClickListener, View.OnLongClickListener, SectionTitleProvider {

	public static final int ITEM_VIEW_TYPE_LIST = 0;
	public static final int ITEM_VIEW_TYPE_GRID = 1;

	Context context;
	MegaApiAndroid megaApi;

//	int positionClicked;
	ArrayList<MegaNode> nodes;

	long parentHandle = -1;
	DisplayMetrics outMetrics;

	private SparseBooleanArray selectedItems;

	RecyclerView listFragment;

	boolean multipleSelect;

	/* public static view holder class */
	public static class ViewHolderVersion extends ViewHolder {

		public ViewHolderVersion(View v) {
			super(v);
		}

		public TextView textViewFileName;
		public TextView textViewFileSize;
		public long document;
		public ImageView imageView;
		public RelativeLayout itemLayout;
		public RelativeLayout threeDotsLayout;
		public RelativeLayout headerLayout;
		public TextView titleHeader;
		public TextView sizeHeader;
	}

	public void toggleAllSelection(int pos) {
		log("toggleAllSelection: "+pos);
		final int positionToflip = pos;

		if (selectedItems.get(pos, false)) {
			log("delete pos: "+pos);
			selectedItems.delete(pos);
		}
		else {
			log("PUT pos: "+pos);
			selectedItems.put(pos, true);
		}

		VersionsFileAdapter.ViewHolderVersion view = (VersionsFileAdapter.ViewHolderVersion) listFragment.findViewHolderForLayoutPosition(pos);
		if(view!=null){
			log("Start animation: "+pos+" multiselection state: "+isMultipleSelect());
			Animation flipAnimation = AnimationUtils.loadAnimation(context, R.anim.multiselect_flip);
			flipAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					log("onAnimationEnd");
					if (selectedItems.size() <= 0){
						log("toggleAllSelection: hideMultipleSelect");
						((VersionsFileActivity) context).hideMultipleSelect();
					}
					log("toggleAllSelection: notified item changed");
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

	public void toggleSelection(int pos) {
		log("toggleSelection: "+pos);

		if (selectedItems.get(pos, false)) {
			log("delete pos: "+pos);
			selectedItems.delete(pos);
		}
		else {
			log("PUT pos: "+pos);
			selectedItems.put(pos, true);
		}
		notifyItemChanged(pos);

		VersionsFileAdapter.ViewHolderVersion view = (VersionsFileAdapter.ViewHolderVersion) listFragment.findViewHolderForLayoutPosition(pos);
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
						((VersionsFileActivity) context).hideMultipleSelect();
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});

			view.imageView.startAnimation(flipAnimation);

		}
		else{
			log("view is null - not animation");
		}
	}

	public void selectAll(){
		for (int i= 0; i<this.getItemCount();i++){
			if(!isItemChecked(i)){
				toggleAllSelection(i);
			}
		}
	}

	public void clearSelections() {
		log("clearSelections");
		for (int i= 0; i<this.getItemCount();i++){
			if(isItemChecked(i)){
				toggleAllSelection(i);
			}
		}
	}

//	public void clearSelections() {
//		if(selectedItems!=null){
//			selectedItems.clear();
//			for (int i= 0; i<this.getItemCount();i++) {
//				if (isItemChecked(i)) {
//					toggleAllSelection(i);
//				}
//			}
//		}
//		notifyDataSetChanged();
//	}
//
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
	public List<MegaNode> getSelectedNodes() {
		ArrayList<MegaNode> nodes = new ArrayList<MegaNode>();

		for (int i = 0; i < selectedItems.size(); i++) {
			if (selectedItems.valueAt(i) == true) {
				MegaNode document = getNodeAt(selectedItems.keyAt(i));
				if (document != null){
					nodes.add(document);
				}
			}
		}
		return nodes;
	}

	public VersionsFileAdapter(Context _context, ArrayList<MegaNode> _nodes, RecyclerView recyclerView) {
		this.context = _context;
		this.nodes = _nodes;

		this.listFragment = recyclerView;

		if (megaApi == null) {
			megaApi = ((MegaApplication) ((Activity) context).getApplication())
					.getMegaApi();
		}
	}

	public void setNodes(ArrayList<MegaNode> nodes) {
		log("setNodes");
		this.nodes = nodes;
//		contentTextFragment.setText(getInfoFolder(node));
		notifyDataSetChanged();
	}

	@Override
	public ViewHolderVersion onCreateViewHolder(ViewGroup parent, int viewType) {
		log("onCreateViewHolder");
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_version_file, parent, false);

		ViewHolderVersion holderList = new ViewHolderVersion(v);
		holderList.itemLayout = (RelativeLayout) v.findViewById(R.id.version_file_item_layout);
		holderList.imageView = (ImageView) v.findViewById(R.id.version_file_thumbnail);

		holderList.textViewFileName = (TextView) v.findViewById(R.id.version_file_filename);

		holderList.textViewFileSize = (TextView) v.findViewById(R.id.version_file_filesize);

		holderList.threeDotsLayout = (RelativeLayout) v.findViewById(R.id.version_file_three_dots_layout);

		holderList.headerLayout = (RelativeLayout) v.findViewById(R.id.version_file_header_layout);
		holderList.titleHeader = (TextView) v.findViewById(R.id.version_file_header_title);
		holderList.sizeHeader = (TextView) v.findViewById(R.id.version_file_header_size);

		holderList.itemLayout.setTag(holderList);
		holderList.itemLayout.setOnClickListener(this);
		holderList.itemLayout.setOnLongClickListener(this);

		holderList.threeDotsLayout.setTag(holderList);
		holderList.threeDotsLayout.setOnClickListener(this);

		v.setTag(holderList);

		return holderList;
	}
	
	@Override
	public void onBindViewHolder(ViewHolderVersion holder, int position) {
		log("onBindViewHolder");

		MegaNode node = (MegaNode) getItem(position);
		holder.document = node.getHandle();
		Bitmap thumb = null;

		if(position==0){
			holder.titleHeader.setText(context.getString(R.string.header_current_section_item));
			holder.sizeHeader.setVisibility(View.GONE);
			holder.headerLayout.setVisibility(View.VISIBLE);
		}
		else if(position==1){
			holder.titleHeader.setText(context.getResources().getQuantityString(R.plurals.header_previous_section_item, megaApi.getNumVersions(node)));

			if(((VersionsFileActivity)context).versionsSize!=null){
				holder.sizeHeader.setText(((VersionsFileActivity)context).versionsSize);
				holder.sizeHeader.setVisibility(View.VISIBLE);
			}
			else{
				holder.sizeHeader.setVisibility(View.GONE);
			}

			holder.headerLayout.setVisibility(View.VISIBLE);
		}
		else{
			holder.headerLayout.setVisibility(View.GONE);
		}
		
		holder.textViewFileName.setText(node.getName());
		holder.textViewFileSize.setText("");

		long nodeSize = node.getSize();
		holder.textViewFileSize.setText(Util.getSizeString(nodeSize));

		if (!multipleSelect) {
			log("Not multiselect");
			holder.itemLayout.setBackgroundColor(Color.WHITE);
			holder.imageView.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
			params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
			params.setMargins(0, 0, 0, 0);
			holder.imageView.setLayoutParams(params);

			log("Check the thumb");

			if (node.hasThumbnail()) {
				log("Node has thumbnail");
				RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
				params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
				params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
				int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
				params1.setMargins(left, 0, 0, 0);

				holder.imageView.setLayoutParams(params1);

				thumb = ThumbnailUtils.getThumbnailFromCache(node);
				if (thumb != null) {

					holder.imageView.setImageBitmap(thumb);

				} else {
					thumb = ThumbnailUtils
							.getThumbnailFromFolder(node, context);
					if (thumb != null) {
						holder.imageView.setImageBitmap(thumb);

					} else {
						try {
							thumb = ThumbnailUtilsLollipop.getThumbnailFromMegaList(node, context, holder, megaApi, this);
						} catch (Exception e) {
						} // Too many AsyncTasks

						if (thumb != null) {
							holder.imageView.setImageBitmap(thumb);
						}
					}
				}
			} else {
				log("Node NOT thumbnail");
				thumb = ThumbnailUtils.getThumbnailFromCache(node);
				if (thumb != null) {
					RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
					params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
					params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
					int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
					params1.setMargins(left, 0, 0, 0);

					holder.imageView.setLayoutParams(params1);
					holder.imageView.setImageBitmap(thumb);


				} else {
					thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
					if (thumb != null) {
						RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
						params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
						params1.setMargins(left, 0, 0, 0);

						holder.imageView.setLayoutParams(params1);
						holder.imageView.setImageBitmap(thumb);

					} else {
						holder.imageView.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());
						RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
						params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
						params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
						int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics());
						params.setMargins(left,0, 0, 0);
						holder.imageView.setLayoutParams(params);

						try {

							ThumbnailUtilsLollipop.createThumbnailList(context, node,holder, megaApi, this);
						} catch (Exception e) {
						} // Too many AsyncTasks
					}
				}
			}
		}
		else {
			log("Multiselection ON");
			if(this.isItemChecked(position)){
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.new_multiselect_color));
				RelativeLayout.LayoutParams paramsMultiselect = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
				paramsMultiselect.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
				paramsMultiselect.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
				int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics());
				paramsMultiselect.setMargins(left, 0, 0, 0);
				holder.imageView.setLayoutParams(paramsMultiselect);
				holder.imageView.setImageResource(R.drawable.ic_select_folder);
			}
			else{
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.white));

				log("Check the thumb");

				if (node.hasThumbnail()) {
					log("Node has thumbnail");
					RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
					params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
					params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
					int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
					params1.setMargins(left, 0, 0, 0);

					holder.imageView.setLayoutParams(params1);

					thumb = ThumbnailUtils.getThumbnailFromCache(node);
					if (thumb != null) {

						holder.imageView.setImageBitmap(thumb);

					} else {
						thumb = ThumbnailUtils
								.getThumbnailFromFolder(node, context);
						if (thumb != null) {
							holder.imageView.setImageBitmap(thumb);

						} else {
							try {
								thumb = ThumbnailUtilsLollipop.getThumbnailFromMegaList(node, context, holder, megaApi, this);
							} catch (Exception e) {
							} // Too many AsyncTasks

							if (thumb != null) {
								holder.imageView.setImageBitmap(thumb);
							}
						}
					}
				} else {
					log("Node NOT thumbnail");

					thumb = ThumbnailUtils.getThumbnailFromCache(node);
					if (thumb != null) {
						RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
						params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
						int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
						params1.setMargins(left, 0, 0, 0);

						holder.imageView.setLayoutParams(params1);
						holder.imageView.setImageBitmap(thumb);


					} else {
						thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
						if (thumb != null) {
							RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
							params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
							params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
							int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
							params1.setMargins(left, 0, 0, 0);

							holder.imageView.setLayoutParams(params1);
							holder.imageView.setImageBitmap(thumb);

						} else {
							log("NOT thumbnail");
							holder.imageView.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());
							RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
							params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
							params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
							int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics());
							params1.setMargins(left, 0, 0, 0);

							if (MimeTypeList.typeForName(node.getName()).isImage()) {
								try {
									ThumbnailUtilsLollipop.createThumbnailList(context, node, holder, megaApi, this);
								} catch (Exception e) {
								}
							}
						}
					}
				}
			}
		}
	}

	private String getItemNode(int position) {
		return nodes.get(position).getName();
	}

	@Override
	public int getItemCount() {
		if (nodes != null){
			return nodes.size();
		}else{
			return 0;
		}
	}

	public Object getItem(int position) {
		if (nodes != null){
			return nodes.get(position);
		}
		
		return null;
	}

	@Override
	public String getSectionTitle(int position) {
		return getItemNode(position).substring(0, 1);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void onClick(View v) {
		log("onClick");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		ViewHolderVersion holder = (ViewHolderVersion) v.getTag();
		int currentPosition = holder.getAdapterPosition();
		log("onClick -> Current position: "+currentPosition);

		if(currentPosition<0){
			log("Current position error - not valid value");
			return;
		}

		final MegaNode n = (MegaNode) getItem(currentPosition);

		switch (v.getId()) {
			case R.id.version_file_three_dots_layout:{

				log("onClick: version_file_three_dots: "+currentPosition);
				if(!Util.isOnline(context)){
					((VersionsFileActivity) context).showSnackbar(context.getString(R.string.error_server_connection_problem));
					return;
				}

				if(multipleSelect){
					((VersionsFileActivity) context).itemClick(currentPosition);
				}
				else{
					((VersionsFileActivity) context).showOptionsPanel(n, currentPosition);

				}

				break;
			}
			case R.id.version_file_item_layout:{

				((VersionsFileActivity) context).itemClick(currentPosition);

				break;
			}
		}
	}

	@Override
	public boolean onLongClick(View view) {
		log("OnLongCLick");
//		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();
//
//		ViewHolderVersion holder = (ViewHolderVersion) view.getTag();
//		int currentPosition = holder.getAdapterPosition();
//
//		((VersionsFileActivity) context).itemClick(currentPosition);

		return true;
	}

	/*
	 * Get document at specified position
	 */
	public MegaNode getNodeAt(int position) {
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
	}

	public boolean isMultipleSelect() {
		return multipleSelect;
	}

	public void setMultipleSelect(boolean multipleSelect) {
		log("setMultipleSelect: "+multipleSelect);
		if (this.multipleSelect != multipleSelect) {
			this.multipleSelect = multipleSelect;
		}
		if(this.multipleSelect){
			selectedItems = new SparseBooleanArray();
		}
	}

	private static void log(String log) {
		Util.log("VersionsFileAdapter", log);
	}
}