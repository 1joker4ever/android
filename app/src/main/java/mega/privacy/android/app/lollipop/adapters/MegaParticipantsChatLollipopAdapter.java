package mega.privacy.android.app.lollipop.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContact;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.lollipop.AddContactActivityLollipop;
import mega.privacy.android.app.lollipop.ContactsFragmentLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.listeners.ChatParticipantAvatarListener;
import mega.privacy.android.app.lollipop.megachat.GroupChatInfoActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.MegaChatParticipant;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaUser;


public class MegaParticipantsChatLollipopAdapter extends RecyclerView.Adapter<MegaParticipantsChatLollipopAdapter.ViewHolderParticipants> implements OnClickListener {

	public static final int ITEM_VIEW_TYPE_LIST = 0;
	public static final int ITEM_VIEW_TYPE_GRID = 1;
	public static final int ITEM_VIEW_TYPE_LIST_ADD_CONTACT = 2;

	Context context;
	int positionClicked;
	ArrayList<MegaChatParticipant> participants;
	RecyclerView listFragment;
	MegaApiAndroid megaApi;
	boolean multipleSelect;
	DatabaseHandler dbH = null;
	private SparseBooleanArray selectedItems;

	public MegaParticipantsChatLollipopAdapter(Context _context, ArrayList<MegaChatParticipant> _participants, RecyclerView _listView) {
		this.context = _context;
		this.participants = _participants;
		this.positionClicked = -1;
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		listFragment = _listView;
	}
	
	/*private view holder class*/
    public class ViewHolderParticipants extends RecyclerView.ViewHolder{
    	public ViewHolderParticipants(View v) {
			super(v);
		}   	
		
    	TextView contactInitialLetter;
//        ImageView imageView;
        TextView textViewContactName;
        TextView textViewContent;
        ImageButton imageButtonThreeDots;
        RelativeLayout itemLayout;
		ImageView permissionsIcon;
        int currentPosition;
        String contactMail;
		String userHandle;
		String fullName="";

		public String getContactMail (){
			return contactMail;
		}
    }
    
    public class ViewHolderParticipantsList extends ViewHolderParticipants{
    	public ViewHolderParticipantsList(View v) {
			super(v);
		}
    	RoundedImageView imageView;

		public void setImageView(Bitmap bitmap){
			imageView.setImageBitmap(bitmap);
			contactInitialLetter.setVisibility(View.GONE);
		}
    }

	ViewHolderParticipantsList holderList = null;

	@Override
	public ViewHolderParticipants onCreateViewHolder(ViewGroup parent, int viewType) {
		log("onCreateViewHolder");
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;
    
	    dbH = DatabaseHandler.getDbHandler(context);

	   
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant_chat_list, parent, false);

		holderList = new ViewHolderParticipantsList(v);
		holderList.itemLayout = (RelativeLayout) v.findViewById(R.id.participant_list_item_layout);
		holderList.imageView = (RoundedImageView) v.findViewById(R.id.participant_list_thumbnail);
		holderList.contactInitialLetter = (TextView) v.findViewById(R.id.participant_list_initial_letter);
		holderList.textViewContactName = (TextView) v.findViewById(R.id.participant_list_name);
		holderList.textViewContent = (TextView) v.findViewById(R.id.participant_list_content);
		holderList.imageButtonThreeDots = (ImageButton) v.findViewById(R.id.participant_list_three_dots);
		holderList.permissionsIcon = (ImageView) v.findViewById(R.id.participant_list_permissions);

		//Right margin
		RelativeLayout.LayoutParams actionButtonParams = (RelativeLayout.LayoutParams)holderList.imageButtonThreeDots.getLayoutParams();
		actionButtonParams.setMargins(0, 0, Util.scaleWidthPx(10, outMetrics), 0);
		holderList.imageButtonThreeDots.setLayoutParams(actionButtonParams);

		holderList.itemLayout.setTag(holderList);
		holderList.itemLayout.setOnClickListener(this);

		v.setTag(holderList);

		return holderList;
	}
	
	@Override
	public void onBindViewHolder(ViewHolderParticipants holder, int position) {
		log("onBindViewHolder");
		

		onBindViewHolderList(holderList, position);

	}

	public void onBindViewHolderList(ViewHolderParticipantsList holder, int position){
		log("onBindViewHolderList");
		holder.currentPosition = position;
		holder.imageView.setImageBitmap(null);
		holder.contactInitialLetter.setText("");
		
		MegaChatParticipant participant = (MegaChatParticipant) getItem(position);
		holder.contactMail = participant.getEmail();
		String userHandleEncoded = MegaApiAndroid.userHandleToBase64(participant.getHandle());
		holder.userHandle = userHandleEncoded;
		log("participant: "+participant.getEmail()+" handle: "+participant.getHandle());
		holder.fullName = participant.getFullName();
	
		if (!multipleSelect) {
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			
			if (positionClicked != -1) {
				if (positionClicked == position) {
					//				holder.arrowSelection.setVisibility(View.VISIBLE);
//					holder.optionsLayout.setVisibility(View.GONE);
					holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.file_list_selected_row));
					listFragment.smoothScrollToPosition(positionClicked);
				}
				else {
					//				holder.arrowSelection.setVisibility(View.GONE);
					holder.itemLayout.setBackgroundColor(Color.WHITE);
				}
			} 
			else {
				//			holder.arrowSelection.setVisibility(View.GONE);
				holder.itemLayout.setBackgroundColor(Color.WHITE);
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

		holder.textViewContactName.setText(holder.fullName);

		createDefaultAvatar(holder);

		if(holder.contactMail!=null){
			log("The participant is contact!!");

			MegaUser contact = megaApi.getContact(holder.contactMail);


			ChatParticipantAvatarListener listener = new ChatParticipantAvatarListener(context, holder, this);

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
		}
		else
		{
			log("Participant is NOT contact");
		}

		holder.textViewContent.setText("A rellenar!");

		int permission = participant.getPrivilege();

		if(permission== MegaChatRoom.PRIV_STANDARD) {
			holder.permissionsIcon.setImageResource(R.drawable.ic_permissions_read_write);
		}
		else if(permission==MegaChatRoom.PRIV_MODERATOR){
			holder.permissionsIcon.setImageResource(R.drawable.ic_permissions_full_access);
		}
		else{
			holder.permissionsIcon.setImageResource(R.drawable.ic_permissions_read_only);
		}
		
		holder.imageButtonThreeDots.setTag(holder);
		holder.imageButtonThreeDots.setOnClickListener(this);	
	}
	
	public void createDefaultAvatar(ViewHolderParticipants holder){
		log("createDefaultAvatar()");
		
		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);
		String color = megaApi.getUserAvatarColor(holder.userHandle);
		if(color!=null){
			log("The color to set the avatar is "+color);
			p.setColor(Color.parseColor(color));
		}
		else{
			log("Default color to the avatar");
			p.setColor(context.getResources().getColor(R.color.lollipop_primary_color));
		}

		int radius;
		if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
			radius = defaultAvatar.getWidth()/2;
		else
			radius = defaultAvatar.getHeight()/2;

		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
		((ViewHolderParticipantsList)holder).imageView.setImageBitmap(defaultAvatar);

		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
		display.getMetrics(outMetrics);
		float density  = context.getResources().getDisplayMetrics().density;

//		String fullName;
		String firstLetter = holder.fullName.charAt(0) + "";
		firstLetter = firstLetter.toUpperCase(Locale.getDefault());
		holder.contactInitialLetter.setText(firstLetter);
		holder.contactInitialLetter.setTextColor(Color.WHITE);
		holder.contactInitialLetter.setVisibility(View.VISIBLE);

		holder.contactInitialLetter.setTextSize(24);
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
        return participants.size();
    }

	public Object getItem(int position) {
		log("getItem");
		return participants.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getPositionClicked() {
		return positionClicked;
	}

	public void setPositionClicked(int p) {
		log("setPositionClicked: "+p);
		positionClicked = p;
		notifyDataSetChanged();
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
	 * Get list of all selected participants
	 */
	public ArrayList<MegaChatParticipant> getParticipant() {
		ArrayList<MegaChatParticipant> participants = new ArrayList<MegaChatParticipant>();
		
		for (int i = 0; i < selectedItems.size(); i++) {
			if (selectedItems.valueAt(i) == true) {
				MegaChatParticipant u = getParticipantAt(selectedItems.keyAt(i));
				if (u != null){
					participants.add(u);
				}
			}
		}
		return participants;
	}	
	
	/*
	 * Get participant at specified position
	 */
	public MegaChatParticipant getParticipantAt(int position) {
		try {
			if (participants != null) {
				return participants.get(position);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}
    
	@Override
	public void onClick(View v) {

		ViewHolderParticipantsList holder = (ViewHolderParticipantsList) v.getTag();
		int currentPosition = holder.currentPosition;
		MegaChatParticipant p = (MegaChatParticipant) getItem(currentPosition);

		switch (v.getId()){
			case R.id.participant_list_three_dots:{
				log("contact_list_three_dots");
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
				((GroupChatInfoActivityLollipop) context).showParticipantsOptionsPanel(p);
				break;
			}
			case R.id.participant_list_item_layout:{
				log("contact_list_item_layout");
//				if (fragment != null){
//					((ContactsFragmentLollipop) fragment).itemClick(currentPosition);
//				}
				break;
			}
		}
	}
	
	public void setParticipants (ArrayList<MegaChatParticipant> participants){
		log("setParticipants");
		this.participants = participants;
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
		Util.log("MegaParticipantsChatLollipopAdapter", log);
	}

	public RecyclerView getListFragment() {
		return listFragment;
	}

	public void setListFragment(RecyclerView listFragment) {
		this.listFragment = listFragment;
	}
}
