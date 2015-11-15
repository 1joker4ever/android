package mega.privacy.android.app.lollipop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.lollipop.MegaBrowserLollipopAdapter.ViewHolderBrowserGrid;
import mega.privacy.android.app.lollipop.MegaBrowserLollipopAdapter.ViewHolderBrowserList;
import mega.privacy.android.app.utils.Util;
import mega.privacy.android.app.R;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MegaContactsLollipopAdapter extends RecyclerView.Adapter<MegaContactsLollipopAdapter.ViewHolderContacts> implements OnClickListener {
	
	public static final int ITEM_VIEW_TYPE_LIST = 0;
	public static final int ITEM_VIEW_TYPE_GRID = 1;
	
	Context context;
	int positionClicked;
	ArrayList<MegaUser> contacts;
	ImageView emptyImageViewFragment;
	TextView emptyTextViewFragment;
	RecyclerView listFragment;
	MegaApiAndroid megaApi;
	boolean multipleSelect;
	private SparseBooleanArray selectedItems;
	ContactsFragmentLollipop fragment;
	int adapterType;
	
	private class UserAvatarListenerList implements MegaRequestListenerInterface{

		Context context;
		ViewHolderContacts holder;
		MegaContactsLollipopAdapter adapter;
		
		public UserAvatarListenerList(Context context, ViewHolderContacts holder, MegaContactsLollipopAdapter adapter) {
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
			log("onRequestFinish()");
			if (e.getErrorCode() == MegaError.API_OK){
				boolean avatarExists = false;
				
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
								holder.imageView.setImageBitmap(bitmap);
								holder.contactInitialLetter.setVisibility(View.GONE);
							}
						}
					}
					
					
					if(request.getParamType()==1){
						log("(1)request.getText(): "+request.getText());
						holder.nameText=request.getText();
						holder.name=true;
					}
					else if(request.getParamType()==2){
						log("(2)request.getText(): "+request.getText());
						holder.firstNameText = request.getText();
						holder.firstName = true;
					}
					if(holder.name&&holder.firstName){
						String fullName = holder.nameText + " " + holder.firstNameText;
						if (fullName.trim().length() > 0){
							holder.textViewContactName.setText(holder.nameText+" "+holder.firstNameText);
							holder.name= false;
							holder.firstName = false;
						}
					}
					
//					if (!avatarExists){
//						createDefaultAvatar();
//					}
				}
			}
//			else{
//				if (holder.contactMail.compareTo(request.getEmail()) == 0){
//					createDefaultAvatar();
//				}
//			}
		}

		@Override
		public void onRequestTemporaryError(MegaApiJava api,
				MegaRequest request, MegaError e) {
			log("onRequestTemporaryError");
		}

		@Override
		public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
			// TODO Auto-generated method stub			
		}
		
	}
	
	public MegaContactsLollipopAdapter(Context _context, ContactsFragmentLollipop _fragment, ArrayList<MegaUser> _contacts, ImageView _emptyImageView,TextView _emptyTextView, RecyclerView _listView, int adapterType) {
		this.context = _context;
		this.contacts = _contacts;
		this.fragment = _fragment;
		this.positionClicked = -1;
		this.adapterType = adapterType;
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		
		emptyImageViewFragment = _emptyImageView;
		emptyTextViewFragment = _emptyTextView;
		listFragment = _listView;
	}
	
	/*private view holder class*/
    public class ViewHolderContacts extends RecyclerView.ViewHolder{
    	public ViewHolderContacts(View v) {
			super(v);
		}
    	
		RoundedImageView imageView;
    	TextView contactInitialLetter;
//        ImageView imageView;
        TextView textViewContactName;
        TextView textViewContent;
        ImageButton imageButtonThreeDots;
        RelativeLayout itemLayout;
        int currentPosition;
        String contactMail;
    	boolean name = false;
    	boolean firstName = false;
    	String nameText;
    	String firstNameText;
    }
    
    public class ViewHolderContactsList extends ViewHolderContacts{
    	public ViewHolderContactsList(View v) {
			super(v);
		}
    }
    
    public class ViewHolderContactsGrid extends ViewHolderContacts{
    	public ViewHolderContactsGrid(View v) {
			super(v);
		}
    	
    	public View separator;
    }
    
	ViewHolderContactsList holderList = null;
	ViewHolderContactsGrid holderGrid = null;

	@Override
	public ViewHolderContacts onCreateViewHolder(ViewGroup parent, int viewType) {
		log("onCreateViewHolder");
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
	    
	    if (viewType == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST){
	   
		    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);	
	
		    holderList = new ViewHolderContactsList(v);
		    holderList.itemLayout = (RelativeLayout) v.findViewById(R.id.contact_list_item_layout);
		    holderList.imageView = (RoundedImageView) v.findViewById(R.id.contact_list_thumbnail);	
		    holderList.contactInitialLetter = (TextView) v.findViewById(R.id.contact_list_initial_letter);
		    holderList.textViewContactName = (TextView) v.findViewById(R.id.contact_list_name);
		    holderList.textViewContent = (TextView) v.findViewById(R.id.contact_list_content);
		    holderList.imageButtonThreeDots = (ImageButton) v.findViewById(R.id.contact_list_three_dots);
			
		    holderList.itemLayout.setTag(holderList);
		    holderList.itemLayout.setOnClickListener(this);
		    
			v.setTag(holderList);
	
			return holderList;
	    }
	    else if (viewType == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID){
	    	
	    	View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_grid, parent, false);
	    	
	    	holderGrid = new ViewHolderContactsGrid(v);
	    	holderGrid.itemLayout = (RelativeLayout) v.findViewById(R.id.contact_grid_item_layout);
		    holderGrid.imageView = (RoundedImageView) v.findViewById(R.id.contact_grid_thumbnail);	
		    holderGrid.contactInitialLetter = (TextView) v.findViewById(R.id.contact_grid_initial_letter);
		    holderGrid.textViewContactName = (TextView) v.findViewById(R.id.contact_grid_name);
		    holderGrid.textViewContent = (TextView) v.findViewById(R.id.contact_grid_content);
		    holderGrid.imageButtonThreeDots = (ImageButton) v.findViewById(R.id.contact_grid_three_dots);
		    
		    holderGrid.separator = (View) v.findViewById(R.id.contact_grid_separator);

		    holderGrid.itemLayout.setTag(holderGrid);
		    holderGrid.itemLayout.setOnClickListener(this);
		    
		    v.setTag(holderGrid);
		    
	    	return holderGrid;	    	
	    }
	    else{
	    	return null;
	    }
	}
	
	@Override
	public void onBindViewHolder(ViewHolderContacts holder, int position) {
		log("onBindViewHolder");
		
		if (adapterType == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_LIST){
			ViewHolderContactsList holderList = (ViewHolderContactsList) holder;
			onBindViewHolderList(holderList, position);
		}
		else if (adapterType == MegaBrowserLollipopAdapter.ITEM_VIEW_TYPE_GRID){
			ViewHolderContactsGrid holderGrid = (ViewHolderContactsGrid) holder;
			onBindViewHolderGrid(holderGrid, position);
		}
	}
	
	public void onBindViewHolderGrid (ViewHolderContactsGrid holder, int position){
		holder.currentPosition = position;
		holder.imageView.setImageBitmap(null);
		holder.contactInitialLetter.setText("");
		
		MegaUser contact = (MegaUser) getItem(position);
		holder.contactMail = contact.getEmail();
		
		if (!multipleSelect) {
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			
			if (positionClicked != -1) {
				if (positionClicked == position) {
					holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid));
					holder.separator.setBackgroundColor(context.getResources().getColor(R.color.grid_item_separator));
					holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
					listFragment.smoothScrollToPosition(positionClicked);
				}
				else {
					holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid));
					holder.separator.setBackgroundColor(context.getResources().getColor(R.color.grid_item_separator));
					holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
				}
			} 
			else {
				holder.itemLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_item_grid));
				holder.separator.setBackgroundColor(context.getResources().getColor(R.color.grid_item_separator));
				holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
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
				holder.separator.setBackgroundColor(context.getResources().getColor(R.color.grid_item_separator));
			}
		}
		
		createDefaultAvatar(holder);
		
		UserAvatarListenerList listener = new UserAvatarListenerList(context, holder, this);
	
		holder.textViewContactName.setText(contact.getEmail());
		holder.name=false;
		holder.firstName=false;
		megaApi.getUserAttribute(contact, 1, listener);
		megaApi.getUserAttribute(contact, 2, listener);
		
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
					if (context.getExternalCacheDir() != null){
						megaApi.getUserAvatar(contact, context.getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
					}
					else{
						megaApi.getUserAvatar(contact, context.getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
					}
				}
				else{
					holder.contactInitialLetter.setVisibility(View.GONE);
					holder.imageView.setImageBitmap(bitmap);
				}
			}
			else{
				if (context.getExternalCacheDir() != null){
					megaApi.getUserAvatar(contact, context.getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);	
				}
				else{
					megaApi.getUserAvatar(contact, context.getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);	
				}			
			}
		}	
		else{
			if (context.getExternalCacheDir() != null){
				megaApi.getUserAvatar(contact, context.getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
			}
			else{
				megaApi.getUserAvatar(contact, context.getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
			}
		}
		
		ArrayList<MegaNode> sharedNodes = megaApi.getInShares(contact);
		
		String sharedNodesDescription = getDescription(sharedNodes);
		
		holder.textViewContent.setText(sharedNodesDescription);
		
		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);	
	}
	
	public void onBindViewHolderList(ViewHolderContactsList holder, int position){
		holder.currentPosition = position;
		holder.imageView.setImageBitmap(null);
		holder.contactInitialLetter.setText("");
		
		MegaUser contact = (MegaUser) getItem(position);
		holder.contactMail = contact.getEmail();
		
	
		if (!multipleSelect) {
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			
			if (positionClicked != -1) {
				if (positionClicked == position) {
					//				holder.arrowSelection.setVisibility(View.VISIBLE);
//					holder.optionsLayout.setVisibility(View.GONE);
					holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
					holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
					listFragment.smoothScrollToPosition(positionClicked);
				}
				else {
					//				holder.arrowSelection.setVisibility(View.GONE);
					holder.itemLayout.setBackgroundColor(Color.WHITE);
					holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
				}
			} 
			else {
				//			holder.arrowSelection.setVisibility(View.GONE);
				holder.itemLayout.setBackgroundColor(Color.WHITE);
				holder.imageButtonThreeDots.setImageResource(R.drawable.action_selector_ic);
			}
		} else {
			holder.imageButtonThreeDots.setVisibility(View.GONE);		

			if(this.isItemChecked(position)){
				holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
			}
			else{
				holder.itemLayout.setBackgroundColor(Color.WHITE);
			}
		}

		createDefaultAvatar(holder);
		
		UserAvatarListenerList listener = new UserAvatarListenerList(context, holder, this);
	
		holder.textViewContactName.setText(contact.getEmail());
		holder.name=false;
		holder.firstName=false;
		megaApi.getUserAttribute(contact, 1, listener);
		megaApi.getUserAttribute(contact, 2, listener);
		
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
					if (context.getExternalCacheDir() != null){
						megaApi.getUserAvatar(contact, context.getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
					}
					else{
						megaApi.getUserAvatar(contact, context.getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
					}
				}
				else{
					holder.contactInitialLetter.setVisibility(View.GONE);
					holder.imageView.setImageBitmap(bitmap);
				}
			}
			else{
				if (context.getExternalCacheDir() != null){
					megaApi.getUserAvatar(contact, context.getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);	
				}
				else{
					megaApi.getUserAvatar(contact, context.getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);	
				}			
			}
		}	
		else{
			if (context.getExternalCacheDir() != null){
				megaApi.getUserAvatar(contact, context.getExternalCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
			}
			else{
				megaApi.getUserAvatar(contact, context.getCacheDir().getAbsolutePath() + "/" + contact.getEmail() + ".jpg", listener);
			}
		}
		
		ArrayList<MegaNode> sharedNodes = megaApi.getInShares(contact);
		
		String sharedNodesDescription = getDescription(sharedNodes);
		
		holder.textViewContent.setText(sharedNodesDescription);
		
		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);	
	}
	
	public void createDefaultAvatar(ViewHolderContacts holder){
		log("createDefaultAvatar()");
		
		Bitmap defaultAvatar = Bitmap.createBitmap(ManagerActivityLollipop.DEFAULT_AVATAR_WIDTH_HEIGHT,ManagerActivityLollipop.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
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
		holder.imageView.setImageBitmap(defaultAvatar);
		
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = context.getResources().getDisplayMetrics().density;
	    
	    int avatarTextSize = getAvatarTextSize(density);
	    log("DENSITY: " + density + ":::: " + avatarTextSize);
	    if (holder.contactMail != null){
		    if (holder.contactMail.length() > 0){
		    	String firstLetter = holder.contactMail.charAt(0) + "";
		    	firstLetter = firstLetter.toUpperCase(Locale.getDefault());
		    	holder.contactInitialLetter.setVisibility(View.VISIBLE);
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
    public int getItemCount() {
        return contacts.size();
    }
	
	@Override
	public int getItemViewType(int position) {
		return adapterType;
	}
	
	public Object getItem(int position) {
		log("getItem");
		return contacts.get(position);
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
		notifyDataSetChanged();
	}
	
	public void setAdapterType(int adapterType){
		this.adapterType = adapterType;
	}
 
	public boolean isMultipleSelect() {
		return multipleSelect;
	}
	
	public void setMultipleSelect(boolean multipleSelect) {
		if (this.multipleSelect != multipleSelect) {
			this.multipleSelect = multipleSelect;
			notifyDataSetChanged();
		}
		if(this.multipleSelect)
		{
			selectedItems = new SparseBooleanArray();
		}
	}
	
	public void toggleSelection(int pos) {
		log("toggleSelection");
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
		for (int i= 0; i<this.getItemCount();i++){
			if(!isItemChecked(i)){
				toggleSelection(i);
			}
		}
	}

	public void clearSelections() {
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
	 * Get list of all selected contacts
	 */
	public List<MegaUser> getSelectedUsers() {
		ArrayList<MegaUser> users = new ArrayList<MegaUser>();
		
		for (int i = 0; i < selectedItems.size(); i++) {
			if (selectedItems.valueAt(i) == true) {
				MegaUser u = getContactAt(selectedItems.keyAt(i));
				if (u != null){
					users.add(u);
				}
			}
		}
		return users;
	}	
	
	/*
	 * Get contact at specified position
	 */
	public MegaUser getContactAt(int position) {
		try {
			if (contacts != null) {
				return contacts.get(position);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}
    
	@Override
	public void onClick(View v) {
		if (adapterType == ITEM_VIEW_TYPE_LIST){
			ViewHolderContactsList holder = (ViewHolderContactsList) v.getTag();
			int currentPosition = holder.currentPosition;
			MegaUser c = (MegaUser) getItem(currentPosition);
			
			switch (v.getId()){			
				case R.id.contact_list_three_dots:{
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
					((ManagerActivityLollipop) context).showOptionsPanel(c);
					break;
				}			
				case R.id.contact_list_item_layout:{
					((ContactsFragmentLollipop) fragment).itemClick(currentPosition);	
					break;
				}
			}
		}
		else if (adapterType == ITEM_VIEW_TYPE_GRID){
			ViewHolderContactsGrid holder = (ViewHolderContactsGrid) v.getTag();
			int currentPosition = holder.currentPosition;
			MegaUser c = (MegaUser) getItem(currentPosition);
			
			switch (v.getId()){			
				case R.id.contact_grid_three_dots:{
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
					((ManagerActivityLollipop) context).showOptionsPanel(c);
					break;
				}			
				case R.id.contact_grid_item_layout:{
					((ContactsFragmentLollipop) fragment).itemClick(currentPosition);	
					break;
				}
			}
		}
	}
	
	public void setContacts (ArrayList<MegaUser> contacts){
		this.contacts = contacts;
		positionClicked = -1;
		notifyDataSetChanged();
	}
	
	public String getDescription(ArrayList<MegaNode> nodes){
		int numFolders = 0;
		int numFiles = 0;
		
		for (int i=0;i<nodes.size();i++){
			MegaNode c = nodes.get(i);
			if (c.isFolder()){
				numFolders++;
			}
			else{
				numFiles++;
			}
		}
		
		String info = "";
		if (numFolders > 0){
			info = numFolders +  " " + context.getResources().getQuantityString(R.plurals.general_num_folders, numFolders);
			if (numFiles > 0){
				info = info + ", " + numFiles + " " + context.getResources().getQuantityString(R.plurals.general_num_files, numFiles);
			}
		}
		else {
			if (numFiles == 0){
				info = numFiles +  " " + context.getResources().getQuantityString(R.plurals.general_num_folders, numFolders);
			}
			else{
				info = numFiles +  " " + context.getResources().getQuantityString(R.plurals.general_num_files, numFiles);
			}
		}
		
		return info;
	}
	
	private static void log(String log) {
		Util.log("MegaContactsLollipopAdapter", log);
	}

	public RecyclerView getListFragment() {
		return listFragment;
	}

	public void setListFragment(RecyclerView listFragment) {
		this.listFragment = listFragment;
	}
}
