package mega.privacy.android.app.lollipop.megachat.chatAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.EmojiconTextView;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.lollipop.listeners.ChatUserAvatarListener;
import mega.privacy.android.app.lollipop.megachat.ChatExplorerActivity;
import mega.privacy.android.app.lollipop.megachat.ChatExplorerFragment;
import mega.privacy.android.app.lollipop.megachat.ChatItemPreferences;
import mega.privacy.android.app.lollipop.megachat.RecentChatsFragmentLollipop;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.TimeChatUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaNode;


public class MegaListChatLollipopAdapter extends RecyclerView.Adapter<MegaListChatLollipopAdapter.ViewHolderChatList> implements OnClickListener, View.OnLongClickListener {

	static public int ADAPTER_RECENT_CHATS = 0;
	static public int ADAPTER_ARCHIVED_CHATS = ADAPTER_RECENT_CHATS+1;

	Context context;
	int positionClicked;
	ArrayList<MegaChatListItem> chats;
	RecyclerView listFragment;
	MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;
	boolean multipleSelect;
	private SparseBooleanArray selectedItems;
	Object fragment;

	DisplayMetrics outMetrics;
	ChatController cC;

	DatabaseHandler dbH = null;
	ChatItemPreferences chatPrefs = null;

	int adapterType;

	public MegaListChatLollipopAdapter(Context _context, Object _fragment, ArrayList<MegaChatListItem> _chats, RecyclerView _listView, int type) {
		log("new adapter");
		this.context = _context;
		this.chats = _chats;
		this.positionClicked = -1;
		this.fragment = _fragment;
		this.adapterType = type;
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		if (megaChatApi == null){
			megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
		}

		listFragment = _listView;

		cC = new ChatController(context);
		
		if(chats!=null)
    	{
    		log("Number of chats: "+chats.size());
    	}
    	else{
    		log("Number of chats: NULL");
    	}

		if(!(context instanceof ManagerActivityLollipop)){
			selectedItems = new SparseBooleanArray();
			multipleSelect = true;
		}
	}
	
	/*public view holder class*/
    public static class ViewHolderChatList extends ViewHolder{
    	public ViewHolderChatList(View arg0) {
			super(arg0);
		}
    	RoundedImageView imageView;
    	TextView contactInitialLetter;
        TextView textViewContactName;
        EmojiconTextView textViewContent;
		TextView textViewDate;
        ImageButton imageButtonThreeDots;
        RelativeLayout itemLayout;
		RelativeLayout circlePendingMessages;

		TextView numberPendingMessages;
		RelativeLayout layoutPendingMessages;
        ImageView muteIcon;
		ImageView contactStateIcon;
        String contactMail;
		String fullName = "";

		public int currentPosition;
		public long userHandle;
		public boolean nameRequestedAction = false;

		public String getContactMail (){
			return contactMail;
		}

		public void setImageView(Bitmap bitmap){
			imageView.setImageBitmap(bitmap);
			contactInitialLetter.setVisibility(View.GONE);
		}
    }
    ViewHolderChatList holder;

	@Override
	public void onBindViewHolder(ViewHolderChatList holder, int position) {
		log("---------------onBindViewHolder---------------- position:"+position);

		holder.imageView.setImageBitmap(null);
		holder.contactInitialLetter.setText("");

		MegaChatListItem chat = (MegaChatListItem) getItem(position);
		//log("ChatRoom handle: "+chat.getChatId() +"****"+ MegaApiAndroid.userHandleToBase64(chat.getChatId())+ " title "+chat.getTitle());

		setTitle(position, holder);

		holder.userHandle = -1;

		if(!chat.isGroup()){
			log("Chat one to one");
			long contactHandle = chat.getPeerHandle();
			String userHandleEncoded = MegaApiAndroid.userHandleToBase64(contactHandle);

			holder.contactMail = megaChatApi.getContactEmail(contactHandle);
			if (!multipleSelect) {
				//Multiselect OFF
				holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
				holder.itemLayout.setBackgroundColor(Color.WHITE);

				setUserAvatar(holder, userHandleEncoded);
			} else {
				log("Multiselect ON");

				if(this.isItemChecked(position)){
//					holder.imageButtonThreeDots.setVisibility(View.GONE);
					holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
					holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.new_multiselect_color));
					holder.imageView.setImageResource(R.drawable.ic_select_avatar);
					holder.contactInitialLetter.setVisibility(View.GONE);
				}
				else{
					log("NOT selected");
					holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
					holder.itemLayout.setBackgroundColor(Color.WHITE);

					setUserAvatar(holder, userHandleEncoded);
				}
			}
			holder.contactStateIcon.setVisibility(View.VISIBLE);

			holder.contactStateIcon.setMaxWidth(Util.scaleWidthPx(6,outMetrics));
			holder.contactStateIcon.setMaxHeight(Util.scaleHeightPx(6,outMetrics));

			setStatus(position, holder);
		}
		else{
			log("Group chat");
			holder.contactStateIcon.setVisibility(View.GONE);

			if (!multipleSelect) {
				//Multiselect OFF
				holder.itemLayout.setBackgroundColor(Color.WHITE);

				if (chat.getTitle().length() > 0){
					String chatTitle = chat.getTitle().trim();
					String firstLetter = chatTitle.charAt(0) + "";
					firstLetter = firstLetter.toUpperCase(Locale.getDefault());
					holder.contactInitialLetter.setText(firstLetter);
				}

				createGroupChatAvatar(holder);
			} else {
				log("Multiselect ON");

				if(this.isItemChecked(position)){
//					holder.imageButtonThreeDots.setVisibility(View.GONE);
					holder.itemLayout.setBackgroundColor(context.getResources().getColor(R.color.new_multiselect_color));
					holder.imageView.setImageResource(R.drawable.ic_select_avatar);

					holder.contactInitialLetter.setVisibility(View.GONE);
				}
				else{
					log("NOT selected");
					holder.itemLayout.setBackgroundColor(Color.WHITE);

					if (chat.getTitle().length() > 0){
						String chatTitle = chat.getTitle().trim();
						String firstLetter = chatTitle.charAt(0) + "";
						firstLetter = firstLetter.toUpperCase(Locale.getDefault());
						holder.contactInitialLetter.setText(firstLetter);
					}

					createGroupChatAvatar(holder);
				}
			}
		}

		setPendingMessages(position, holder);

		setTs(position, holder);

		setLastMessage(position, holder);

		chatPrefs = dbH.findChatPreferencesByHandle(String.valueOf(chat.getChatId()));
		if(chatPrefs!=null) {
			log("Chat prefs exists!!!");
			boolean notificationsEnabled = true;
			if (chatPrefs.getNotificationsEnabled() != null) {
				notificationsEnabled = Boolean.parseBoolean(chatPrefs.getNotificationsEnabled());
			}

			if (!notificationsEnabled) {
				log("Chat is MUTE");
				holder.muteIcon.setVisibility(View.VISIBLE);
			}
			else{
				log("Chat with notifications enabled!!");
				holder.muteIcon.setVisibility(View.GONE);
			}
		}
		else{
			log("Chat prefs is NULL");
			holder.muteIcon.setVisibility(View.GONE);
		}

		if(context instanceof ChatExplorerActivity || context instanceof FileExplorerActivityLollipop){

			holder.imageButtonThreeDots.setVisibility(View.GONE);
			if(chat.getOwnPrivilege()==MegaChatRoom.PRIV_RM||chat.getOwnPrivilege()==MegaChatRoom.PRIV_RO){
				holder.imageView.setAlpha(.4f);

				holder.itemLayout.setOnClickListener(null);
				holder.itemLayout.setOnLongClickListener(null);

				holder.layoutPendingMessages.setAlpha(.4f);

				holder.textViewContent.setTextColor(context.getResources().getColor(R.color.text_secondary));
				holder.textViewDate.setTextColor(context.getResources().getColor(R.color.text_secondary));
				holder.textViewContactName.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
			}
			else{
				holder.imageView.setAlpha(1.0f);

				holder.imageButtonThreeDots.setTag(holder);

				holder.itemLayout.setOnClickListener(this);
				holder.itemLayout.setOnLongClickListener(null);

				holder.layoutPendingMessages.setAlpha(1.0f);

				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
				holder.textViewDate.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
				holder.textViewContactName.setTextColor(ContextCompat.getColor(context, R.color.file_list_first_row));
			}
		}
		else{
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);

			holder.imageButtonThreeDots.setTag(holder);

			holder.itemLayout.setOnClickListener(this);
			holder.itemLayout.setOnLongClickListener(this);
		}
	}

	public void setUserAvatar(ViewHolderChatList holder, String userHandle){
		log("setUserAvatar");

		createDefaultAvatar(holder, userHandle);

		ChatUserAvatarListener listener = new ChatUserAvatarListener(context, holder, this);

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

					if(megaApi==null){
						log("setUserAvatar: megaApi is Null in Offline mode");
						return;
					}

					if (context.getExternalCacheDir() != null){
						megaApi.getUserAvatar(holder.contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + holder.contactMail + ".jpg", listener);
					}
					else{
						megaApi.getUserAvatar(holder.contactMail, context.getCacheDir().getAbsolutePath() + "/" + holder.contactMail + ".jpg", listener);
					}
				}
				else{
					holder.contactInitialLetter.setVisibility(View.GONE);
					holder.imageView.setImageBitmap(bitmap);
				}
			}
			else{
				if(megaApi==null){
					log("setUserAvatar: megaApi is Null in Offline mode");
					return;
				}

				if (context.getExternalCacheDir() != null){
					megaApi.getUserAvatar(holder.contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + holder.contactMail + ".jpg", listener);
				}
				else{
					megaApi.getUserAvatar(holder.contactMail, context.getCacheDir().getAbsolutePath() + "/" + holder.contactMail + ".jpg", listener);
				}
			}
		}
		else{
			if(megaApi==null){
				log("setUserAvatar: megaApi is Null in Offline mode");
				return;
			}

			if (context.getExternalCacheDir() != null){
				megaApi.getUserAvatar(holder.contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + holder.contactMail + ".jpg", listener);
			}
			else{
				megaApi.getUserAvatar(holder.contactMail, context.getCacheDir().getAbsolutePath() + "/" + holder.contactMail + ".jpg", listener);
			}
		}
	}

	public String formatStringDuration(int duration) {

		if (duration > 0) {
			int hours = duration / 3600;
			int minutes = (duration % 3600) / 60;
			int seconds = duration % 60;

			String timeString;
			if (hours > 0) {
				timeString = " %d " + context.getResources().getString(R.string.initial_hour) + " %d " + context.getResources().getString(R.string.initial_minute);
				timeString = String.format(timeString, hours, minutes);
			} else if(minutes>0){
				timeString = " %d " + context.getResources().getString(R.string.initial_minute) + " %02d " + context.getResources().getString(R.string.initial_second);
				timeString = String.format(timeString, minutes, seconds);
			}
			else{
				timeString = " %02d " + context.getResources().getString(R.string.initial_second);
				timeString = String.format(timeString, seconds);
			}

			log("The duration is: " + hours + " " + minutes + " " + seconds);

			return timeString;
		}
		return "0";
	}

	@Override
	public ViewHolderChatList onCreateViewHolder(ViewGroup parent, int viewType) {
		log("onCreateViewHolder");

		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

		dbH = DatabaseHandler.getDbHandler(context);

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_chat_list, parent, false);
		holder = new ViewHolderChatList(v);
		holder.itemLayout = (RelativeLayout) v.findViewById(R.id.recent_chat_list_item_layout);
		holder.muteIcon = (ImageView) v.findViewById(R.id.recent_chat_list_mute_icon);

		holder.imageView = (RoundedImageView) v.findViewById(R.id.recent_chat_list_thumbnail);
		holder.contactInitialLetter = (TextView) v.findViewById(R.id.recent_chat_list_initial_letter);
		holder.textViewContactName = (TextView) v.findViewById(R.id.recent_chat_list_name);

		if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			log("Landscape");
			holder.textViewContactName.setMaxWidth(Util.scaleWidthPx(260, outMetrics));
		}else{
			log("Portrait");
			holder.textViewContactName.setMaxWidth(Util.scaleWidthPx(190, outMetrics));
		}

		holder.textViewContent = (EmojiconTextView) v.findViewById(R.id.recent_chat_list_content);
		if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			log("Landscape");
			holder.textViewContent.setMaxWidth(Util.scaleWidthPx(260, outMetrics));
		}else{
			log("Portrait");
			holder.textViewContent.setMaxWidth(Util.scaleWidthPx(190, outMetrics));
		}
		holder.textViewDate = (TextView) v.findViewById(R.id.recent_chat_list_date);
		holder.imageButtonThreeDots = (ImageButton) v.findViewById(R.id.recent_chat_list_three_dots);

		if(context instanceof ManagerActivityLollipop){
			holder.imageButtonThreeDots.setVisibility(View.VISIBLE);
			holder.imageButtonThreeDots.setOnClickListener(this);
		}
		else{
			holder.imageButtonThreeDots.setVisibility(View.GONE);
			holder.imageButtonThreeDots.setOnClickListener(null);
		}

		holder.layoutPendingMessages = (RelativeLayout) v.findViewById(R.id.recent_chat_list_unread_layout);
		holder.circlePendingMessages = (RelativeLayout) v.findViewById(R.id.recent_chat_list_unread_circle);
		holder.numberPendingMessages = (TextView) v.findViewById(R.id.recent_chat_list_unread_number);

		holder.contactStateIcon = (ImageView) v.findViewById(R.id.recent_chat_list_contact_state);

		v.setTag(holder);

		return holder;
	}

	public void setUnreadCount(int unreadMessages, ViewHolderChatList holder){
		log("setPendingMessages: "+unreadMessages);

		Bitmap image=null;
		String numberString = "";

		int heightPendingMessageIcon = (int) context.getResources().getDimension(R.dimen.width_image_pending_message_one_digit);

		if(unreadMessages<0){
			unreadMessages = Math.abs(unreadMessages);
			log("unread number: "+unreadMessages);
			numberString = "+"+unreadMessages;
		}
		else{
			numberString = unreadMessages+"";
		}

//		numberString="20";
		int size = numberString.length();

		switch(size){
			case 0:{
				log("0 digits - error!");
				holder.layoutPendingMessages.setVisibility(View.GONE);
				break;
			}
			case 1:{
				log("drawing circle for one digit");
				holder.circlePendingMessages.setBackground(context.getResources().getDrawable(R.drawable.ic_unread_1));
				holder.layoutPendingMessages.setVisibility(View.VISIBLE);

				break;
			}
			case 2:{
				log("drawing oval for two digits");
				holder.circlePendingMessages.setBackground(context.getResources().getDrawable(R.drawable.ic_unread_2));
				holder.layoutPendingMessages.setVisibility(View.VISIBLE);

				break;
			}
			case 3:{
				log("drawing oval for three digits");
				holder.circlePendingMessages.setBackground(context.getResources().getDrawable(R.drawable.ic_unread_3));
				holder.layoutPendingMessages.setVisibility(View.VISIBLE);

				break;
			}
			default:{
				log("drawing oval for DEFAULT");
				holder.circlePendingMessages.setBackground(context.getResources().getDrawable(R.drawable.ic_unread_4));
				holder.layoutPendingMessages.setVisibility(View.VISIBLE);

				break;
			}
		}

		holder.numberPendingMessages.setText(numberString);

	}

	public void createGroupChatAvatar(ViewHolderChatList holder){
		log("createGroupChatAvatar()");

		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(ContextCompat.getColor(context,R.color.divider_upgrade_account));

		int radius;
		if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
			radius = defaultAvatar.getWidth()/2;
		else
			radius = defaultAvatar.getHeight()/2;

		c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
		holder.imageView.setImageBitmap(defaultAvatar);

		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
		display.getMetrics(outMetrics);
		float density  = context.getResources().getDisplayMetrics().density;

		String firstLetter = holder.contactInitialLetter.getText().toString();

		if(firstLetter.trim().isEmpty()){
			holder.contactInitialLetter.setVisibility(View.INVISIBLE);
		}
		else{
			log("Group chat initial letter is: "+firstLetter);
			if(firstLetter.equals("(")){
				holder.contactInitialLetter.setVisibility(View.INVISIBLE);
			}
			else{
				holder.contactInitialLetter.setText(firstLetter);
				holder.contactInitialLetter.setTextColor(Color.WHITE);
				holder.contactInitialLetter.setVisibility(View.VISIBLE);
				holder.contactInitialLetter.setTextSize(24);
			}
		}
	}

	public void createDefaultAvatar(ViewHolderChatList holder, String userHandle){
		log("createDefaultAvatar()");

		Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);

		String color = megaApi.getUserAvatarColor(userHandle);
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
		holder.imageView.setImageBitmap(defaultAvatar);

		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

		boolean setInitialByMail = false;

		if (holder.fullName != null){
			if (holder.fullName.trim().length() > 0){
				String firstLetter = holder.fullName.charAt(0) + "";
				firstLetter = firstLetter.toUpperCase(Locale.getDefault());
				holder.contactInitialLetter.setText(firstLetter);
				holder.contactInitialLetter.setTextColor(Color.WHITE);
				holder.contactInitialLetter.setVisibility(View.VISIBLE);
			}else{
				setInitialByMail=true;
			}
		}
		else{
			setInitialByMail=true;
		}
		if(setInitialByMail){
			if (holder.contactMail != null){
				if (holder.contactMail.length() > 0){
					log("email TEXT: " + holder.contactMail);
					log("email TEXT AT 0: " + holder.contactMail.charAt(0));
					String firstLetter = holder.contactMail.charAt(0) + "";
					firstLetter = firstLetter.toUpperCase(Locale.getDefault());
					holder.contactInitialLetter.setText(firstLetter);
					holder.contactInitialLetter.setTextColor(Color.WHITE);
					holder.contactInitialLetter.setVisibility(View.VISIBLE);
				}
			}
		}
		holder.contactInitialLetter.setTextSize(24);
	}

	@Override
    public int getItemCount() {
        return chats.size();
    }

	public boolean isMultipleSelect() {
		log("isMultipleSelect");
		return multipleSelect;
	}

	public void setMultipleSelect(boolean multipleSelect) {
		log("setMultipleSelect");
		if (this.multipleSelect != multipleSelect) {
			this.multipleSelect = multipleSelect;
		}
		if(this.multipleSelect)
		{
			selectedItems = new SparseBooleanArray();
		}
	}

	public void toggleAllSelection(int pos) {
		log("toggleSelection");
		final int positionToflip = pos;

		if (selectedItems.get(pos, false)) {
			log("delete pos: "+pos);
			selectedItems.delete(pos);
		}
		else {
			log("PUT pos: "+pos);
			selectedItems.put(pos, true);
		}

		ViewHolderChatList view = (ViewHolderChatList) listFragment.findViewHolderForLayoutPosition(pos);
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
						if(context instanceof ManagerActivityLollipop){
							((RecentChatsFragmentLollipop) fragment).hideMultipleSelect();
						}
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

		ViewHolderChatList view = (ViewHolderChatList) listFragment.findViewHolderForLayoutPosition(pos);
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
						if(context instanceof ManagerActivityLollipop){
							((RecentChatsFragmentLollipop) fragment).hideMultipleSelect();
						}
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			view.imageView.startAnimation(flipAnimation);
		}
	}

	public void selectAll(){
		for (int i= 0; i<this.getItemCount();i++){
			if(!isItemChecked(i)){
				toggleSelection(i);
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
	 * Get request at specified position
	 */
	public MegaChatListItem getChatAt(int position) {
		try {
			if (chats != null) {
				return chats.get(position);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return null;
	}

	/*
	 * Get list of all selected chats
	 */
	public ArrayList<MegaChatListItem> getSelectedChats() {
		ArrayList<MegaChatListItem> chats = new ArrayList<MegaChatListItem>();

		for (int i = 0; i < selectedItems.size(); i++) {
			if (selectedItems.valueAt(i) == true) {
				MegaChatListItem r = getChatAt(selectedItems.keyAt(i));
				if (r != null){
					chats.add(r);
				}
			}
		}
		return chats;
	}

    public Object getItem(int position) {
        return chats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getPositionClicked (){
    	return positionClicked;
    }

    public void setPositionClicked(int p){
		log("setPositionClicked: "+p);
    	positionClicked = p;
		notifyDataSetChanged();
    }

	@Override
	public void onClick(View v) {
		ViewHolderChatList holder = (ViewHolderChatList) v.getTag();
		int currentPosition = holder.getAdapterPosition();
		log("onClick -> Current position: "+currentPosition);
		MegaChatListItem c = (MegaChatListItem) getItem(currentPosition);

		switch (v.getId()){
			case R.id.recent_chat_list_three_dots:{
				log("click three dots!: "+c.getTitle());
				if(context instanceof ManagerActivityLollipop) {

					if (multipleSelect) {
						((RecentChatsFragmentLollipop) fragment).itemClick(currentPosition);
					} else {
						((ManagerActivityLollipop) context).showChatPanel(c);
					}
				}

				break;
			}
			case R.id.recent_chat_list_item_layout:{
				log("click layout!");
//				if(multipleSelect){
//					toggleSelection(holder);
//				}

				if(context instanceof ManagerActivityLollipop){
					((RecentChatsFragmentLollipop) fragment).itemClick(currentPosition);
				}
				else{
					((ChatExplorerFragment) fragment).itemClick(currentPosition);

				}

				break;
			}
		}
	}

	@Override
	public boolean onLongClick(View view) {
		log("OnLongCLick");
		ViewHolderChatList holder = (ViewHolderChatList) view.getTag();
		int currentPosition = holder.getAdapterPosition();

		if(context instanceof ManagerActivityLollipop) {
			((RecentChatsFragmentLollipop) fragment).activateActionMode();
			((RecentChatsFragmentLollipop) fragment).itemClick(currentPosition);
		}

		return true;
	}


	public void updateNonContactName(int pos, long userHandle){
		log("updateNonContactName: "+pos+"_"+userHandle);
		ViewHolderChatList view = (ViewHolderChatList) listFragment.findViewHolderForLayoutPosition(pos);

		if(view!=null){
			if(view.userHandle == userHandle){
				notifyItemChanged(pos);
			}
		}
	}

	public void setStatus(int position, ViewHolderChatList holder){
		log("setStatus: "+position);

		if(holder!=null){
			MegaChatListItem chat = chats.get(position);
			long userHandle = chat.getPeerHandle();
			int state = megaChatApi.getUserOnlineStatus(userHandle);

			if(chat!=null){

				if(state == MegaChatApi.STATUS_ONLINE){
					log("This user is connected");
					holder.contactStateIcon.setVisibility(View.VISIBLE);
					holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_online));
				}
				else if(state == MegaChatApi.STATUS_AWAY){
					log("This user is away");
					holder.contactStateIcon.setVisibility(View.VISIBLE);
					holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_away));
				}
				else if(state == MegaChatApi.STATUS_BUSY){
					log("This user is busy");
					holder.contactStateIcon.setVisibility(View.VISIBLE);
					holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_busy));
				}
				else if(state == MegaChatApi.STATUS_OFFLINE){
					log("This user is offline");
					holder.contactStateIcon.setVisibility(View.VISIBLE);
					holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_offline));
				}
				else if(state == MegaChatApi.STATUS_INVALID){
					log("INVALID status: "+state);
					holder.contactStateIcon.setVisibility(View.GONE);
				}
				else{
					log("This user status is: "+state);
					holder.contactStateIcon.setVisibility(View.GONE);
				}
			}
			else{
				log("Chat is NULL");
			}
		}
		else{
			log("Holder is NULL: "+position);
			notifyItemChanged(position);
		}
	}


	public void updateContactStatus(int position, long userHandle, int state){
		log("updateContactStatus: "+position);

		holder = (ViewHolderChatList) listFragment.findViewHolderForAdapterPosition(position);

		if(holder!=null){

			if(state == MegaChatApi.STATUS_ONLINE){
				log("This user is connected");
				holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_online));
				holder.contactStateIcon.setVisibility(View.VISIBLE);
			}
			else if(state == MegaChatApi.STATUS_AWAY){
				log("This user is away");
				holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_away));
				holder.contactStateIcon.setVisibility(View.VISIBLE);
			}
			else if(state == MegaChatApi.STATUS_BUSY){
				log("This user is busy");
				holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_busy));
				holder.contactStateIcon.setVisibility(View.VISIBLE);
			}
			else if(state == MegaChatApi.STATUS_OFFLINE){
				log("This user is offline");
				holder.contactStateIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_status_contact_offline));
				holder.contactStateIcon.setVisibility(View.VISIBLE);
			}
			else{
				log("This user status is: "+state);
				holder.contactStateIcon.setVisibility(View.GONE);
			}
		}
		else{
			log("Holder is NULL");
			notifyItemChanged(position);
		}
	}

	public void setTitle(int position, ViewHolderChatList holder) {
		log("setTitle");
		if (holder == null) {
			holder = (ViewHolderChatList) listFragment.findViewHolderForAdapterPosition(position);
		}

		if(holder!=null){

			MegaChatListItem chat = chats.get(position);
			String title = chat.getTitle();

			if(title!=null){
				log("ChatRoom title: "+title);
				log("chat timestamp: "+chat.getLastTimestamp());
				String date = TimeChatUtils.formatDateAndTime(chat.getLastTimestamp(), TimeChatUtils.DATE_LONG_FORMAT);
				log("date timestamp: "+date);
				holder.textViewContactName.setText(title);

				if(!chat.isGroup()){
					holder.fullName = title;
				}
				else{
					if (title.length() > 0){
						String chatTitle = title.trim();
						String firstLetter = chatTitle.charAt(0) + "";
						firstLetter = firstLetter.toUpperCase(Locale.getDefault());
						holder.contactInitialLetter.setText(firstLetter);
					}

					createGroupChatAvatar(holder);
				}
			}

		}
		else{
			log("Holder is NULL: "+position);
			notifyItemChanged(position);
		}
	}

	public void setTs(int position, ViewHolderChatList holder) {
		log("setTs");

		if (holder == null) {
			holder = (ViewHolderChatList) listFragment.findViewHolderForAdapterPosition(position);
		}

		if(holder!=null){
			MegaChatListItem chat = chats.get(position);

			int messageType = chat.getLastMessageType();

			if(messageType==MegaChatMessage.TYPE_INVALID) {
				holder.textViewDate.setVisibility(View.GONE);
			}
			else{
				log("ChatRoom title: "+chat.getTitle());
				log("chat timestamp: "+chat.getLastTimestamp());
				String date = TimeChatUtils.formatDateAndTime(chat.getLastTimestamp(), TimeChatUtils.DATE_LONG_FORMAT);
				log("date timestamp: "+date);
				holder.textViewDate.setText(date);
				holder.textViewDate.setVisibility(View.VISIBLE);
			}
		}
		else{
			log("Holder is NULL: "+position);
			notifyItemChanged(position);
		}
	}

	public void setPendingMessages(int position, ViewHolderChatList holder){
		log("setPendingMessages");
		if(holder == null){
			holder = (ViewHolderChatList) listFragment.findViewHolderForAdapterPosition(position);
		}

		if(holder!=null){
			MegaChatListItem chat = chats.get(position);
			int unreadMessages = chat.getUnreadCount();
			log("Unread messages: "+unreadMessages);
			if(chat.getUnreadCount()!=0){
				setUnreadCount(unreadMessages, holder);
			}
			else{
				holder.layoutPendingMessages.setVisibility(View.INVISIBLE);
			}
		}
		else{
			log("Holder is NULL: "+position);
			notifyItemChanged(position);
		}
	}

	public void showMuteIcon(int position){
		log("showMuteIcon");
		if(holder == null){
			holder = (ViewHolderChatList) listFragment.findViewHolderForAdapterPosition(position);
		}

		if(holder!=null){
			MegaChatListItem chatToShow = chats.get(position);

			chatPrefs = dbH.findChatPreferencesByHandle(String.valueOf(chatToShow.getChatId()));
			if(chatPrefs!=null) {
				log("Chat prefs exists!!!");
				boolean notificationsEnabled = true;
				if (chatPrefs.getNotificationsEnabled() != null) {
					notificationsEnabled = Boolean.parseBoolean(chatPrefs.getNotificationsEnabled());
				}

				if (!notificationsEnabled) {
					log("Chat is MUTE");
					holder.muteIcon.setVisibility(View.VISIBLE);
				}
				else{
					log("Chat with notifications enabled!!");
					holder.muteIcon.setVisibility(View.GONE);
				}
			}
			else{
				log("Chat prefs is NULL");
				holder.muteIcon.setVisibility(View.GONE);
			}
			notifyItemChanged(position);
		}
		else{
			log("Holder is NULL: "+position);
			notifyItemChanged(position);
		}
	}

	public void setLastMessage(int position, ViewHolderChatList holder){
		log("setLastMessage");
		if(holder == null){
			holder = (ViewHolderChatList) listFragment.findViewHolderForAdapterPosition(position);
		}

		if(holder!=null){
			MegaChatListItem chat = chats.get(position);

			int messageType = chat.getLastMessageType();
			log("MessageType: "+messageType);
			String lastMessageString = chat.getLastMessage();

			if(messageType==MegaChatMessage.TYPE_INVALID){
				log("Message Type -> INVALID");
				holder.textViewContent.setText(context.getString(R.string.no_conversation_history));
				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
				holder.textViewDate.setVisibility(View.GONE);
			}
			else if(messageType==255){
				log("Message Type -> LOADING");
				holder.textViewContent.setText(context.getString(R.string.general_loading));
				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
				holder.textViewDate.setVisibility(View.GONE);
			}
			else if(messageType==MegaChatMessage.TYPE_ALTER_PARTICIPANTS){
				log("Message Type -> TYPE_ALTER_PARTICIPANTS");
				int privilege = chat.getLastMessagePriv();
				log("Privilege: "+privilege);
				String textToShow = "";

				if(chat.getLastMessageHandle()==megaChatApi.getMyUserHandle()){
					log("I have changed the permissions");

					MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());

					String fullNameAction = chatRoom.getPeerFullnameByHandle(chat.getLastMessageSender());
					if(fullNameAction==null){
						fullNameAction = "";
					}

					if(fullNameAction.trim().length()<=0){
						fullNameAction = cC.getFullName(chat.getLastMessageSender(), chat.getChatId());
					}

					if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
						if(fullNameAction.isEmpty()){
							if(!(holder.nameRequestedAction)){
								log("3-Call for nonContactName: "+ chat.getLastMessageSender());
								fullNameAction = "Unknown name";
								holder.nameRequestedAction=true;
								holder.userHandle = chat.getLastMessageSender();
								ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageSender());
								megaChatApi.getUserFirstname(chat.getLastMessageSender(), listener);
								megaChatApi.getUserLastname(chat.getLastMessageSender(), listener);
								megaChatApi.getUserEmail(chat.getLastMessageSender(), listener);
							}
							else{
								log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
							}
						}
					}

					if(privilege!=MegaChatRoom.PRIV_RM){
						log("I was added");
						String myFullName = megaChatApi.getMyFullname();
						if(myFullName==null){
							myFullName = "";
						}
						if(myFullName.trim().length()<=0){
							myFullName = megaChatApi.getMyEmail();
						}

						textToShow = String.format(context.getString(R.string.message_add_participant), myFullName, fullNameAction);
						try{
							textToShow = textToShow.replace("[A]", "");
							textToShow = textToShow.replace("[/A]", "");
							textToShow = textToShow.replace("[B]", "");
							textToShow = textToShow.replace("[/B]", "");
							textToShow = textToShow.replace("[C]", "");
							textToShow = textToShow.replace("[/C]", "");
						}
						catch (Exception e){}
					}
					else{
						log("I was removed or left");
						if(chat.getLastMessageSender()==chat.getLastMessageHandle()){
							log("I left the chat");
							String myFullName = megaChatApi.getMyFullname();
							if(myFullName==null){
								myFullName = "";
							}
							if(myFullName.trim().length()<=0){
								myFullName = megaChatApi.getMyEmail();
							}
							textToShow = String.format(context.getString(R.string.message_participant_left_group_chat), myFullName);
							try{
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
							}
							catch (Exception e){}
						}
						else{
							String myFullName = megaChatApi.getMyFullname();
							if(myFullName==null){
								myFullName = "";
							}
							if(myFullName.trim().length()<=0){
								myFullName = megaChatApi.getMyEmail();
							}
							textToShow = String.format(context.getString(R.string.message_remove_participant), myFullName, fullNameAction);
							try{
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
								textToShow = textToShow.replace("[C]", "");
								textToShow = textToShow.replace("[/C]", "");
							}
							catch (Exception e){}
						}
					}

					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}

					holder.textViewContent.setText(result);
				}
				else{

					MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());
					String fullNameTitle = chatRoom.getPeerFullnameByHandle(chat.getLastMessageHandle());
					if(fullNameTitle==null){
						fullNameTitle = "";
					}

					if(fullNameTitle.trim().length()<=0){
						fullNameTitle = cC.getFullName(chat.getLastMessageHandle(), chat.getChatId());
					}

					if(fullNameTitle.trim().length()<=0){
						if(!(holder.nameRequestedAction)){
							log("3-Call for nonContactName: "+ chat.getLastMessageHandle());
							fullNameTitle = "Unknown name";
							holder.nameRequestedAction=true;
							holder.userHandle = chat.getLastMessageHandle();
							ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageHandle());
							megaChatApi.getUserFirstname(chat.getLastMessageHandle(), listener);
							megaChatApi.getUserLastname(chat.getLastMessageHandle(), listener);
							megaChatApi.getUserEmail(chat.getLastMessageHandle(), listener);
						}
						else{
							log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
						}
					}

					if(privilege!=MegaChatRoom.PRIV_RM){
						log("Participant was added");
						if(chat.getLastMessageSender()==megaChatApi.getMyUserHandle()){
							log("By me");
							String myFullName = megaChatApi.getMyFullname();
							if(myFullName==null){
								myFullName = "";
							}
							if(myFullName.trim().length()<=0){
								myFullName = megaChatApi.getMyEmail();
							}
							textToShow = String.format(context.getString(R.string.message_add_participant), fullNameTitle, myFullName);
							try{
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
								textToShow = textToShow.replace("[C]", "");
								textToShow = textToShow.replace("[/C]", "");
							}
							catch (Exception e){}
						}
						else{
//                        textToShow = String.format(context.getString(R.string.message_add_participant), message.getHandleOfAction()+"");
							log("By other");

							String fullNameAction = chatRoom.getPeerFullnameByHandle(chat.getLastMessageSender());
							if(fullNameAction==null){
								fullNameAction = "";
							}

							if(fullNameAction.trim().length()<=0){
								fullNameAction = cC.getFullName(chat.getLastMessageSender(), chat.getChatId());
							}

							if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
								if(fullNameAction.isEmpty()){
									if(!(holder.nameRequestedAction)){
										log("3-Call for nonContactName: "+ chat.getLastMessageSender());
										fullNameAction = "Unknown name";
										holder.nameRequestedAction=true;
										holder.userHandle = chat.getLastMessageSender();
										ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageSender());
										megaChatApi.getUserFirstname(chat.getLastMessageSender(), listener);
										megaChatApi.getUserLastname(chat.getLastMessageSender(), listener);
										megaChatApi.getUserEmail(chat.getLastMessageSender(), listener);
									}
									else{
										log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
									}
								}
							}

							textToShow = String.format(context.getString(R.string.message_add_participant), fullNameTitle, fullNameAction);
							try{
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
								textToShow = textToShow.replace("[C]", "");
								textToShow = textToShow.replace("[/C]", "");
							}
							catch (Exception e){}

						}
					}//END participant was added
					else{
						log("Participant was removed or left");
						if(chat.getLastMessageSender()==megaChatApi.getMyUserHandle()){
							String myFullName = megaChatApi.getMyFullname();
							if(myFullName==null){
								myFullName = "";
							}
							if(myFullName.trim().length()<=0){
								myFullName = megaChatApi.getMyEmail();
							}
							textToShow = String.format(context.getString(R.string.message_remove_participant), fullNameTitle, myFullName);
							try{
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
								textToShow = textToShow.replace("[C]", "");
								textToShow = textToShow.replace("[/C]", "");
							}
							catch (Exception e){}
						}
						else{

							if(chat.getLastMessageSender()==chat.getLastMessageHandle()){
								log("The participant left the chat");

								textToShow = String.format(context.getString(R.string.message_participant_left_group_chat), fullNameTitle);
								try{
									textToShow = textToShow.replace("[A]", "");
									textToShow = textToShow.replace("[/A]", "");
									textToShow = textToShow.replace("[B]", "");
									textToShow = textToShow.replace("[/B]", "");
								}
								catch (Exception e){}

							}
							else{
								log("The participant was removed");
								String fullNameAction = chatRoom.getPeerFullnameByHandle(chat.getLastMessageSender());
								if(fullNameAction==null){
									fullNameAction = "";
								}

								if(fullNameAction.trim().length()<=0){
									fullNameAction = cC.getFullName(chat.getLastMessageSender(), chat.getChatId());
								}

								if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
									if(fullNameAction.isEmpty()){
										if(!(holder.nameRequestedAction)){
											log("3-Call for nonContactName: "+ chat.getLastMessageSender());
											fullNameAction = "Unknown name";
											holder.nameRequestedAction=true;
											holder.userHandle = chat.getLastMessageSender();
											ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageSender());
											megaChatApi.getUserFirstname(chat.getLastMessageSender(), listener);
											megaChatApi.getUserLastname(chat.getLastMessageSender(), listener);
											megaChatApi.getUserEmail(chat.getLastMessageSender(), listener);
										}
										else{
											log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
										}
									}
								}

								textToShow = String.format(context.getString(R.string.message_remove_participant), fullNameTitle, fullNameAction);
								try{
									textToShow = textToShow.replace("[A]", "");
									textToShow = textToShow.replace("[/A]", "");
									textToShow = textToShow.replace("[B]", "");
									textToShow = textToShow.replace("[/B]", "");
									textToShow = textToShow.replace("[C]", "");
									textToShow = textToShow.replace("[/C]", "");
								}
								catch (Exception e){}
							}
//                        textToShow = String.format(context.getString(R.string.message_remove_participant), message.getHandleOfAction()+"");
						}
					} //END participant removed

					Spanned result = null;
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
						result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
					} else {
						result = Html.fromHtml(textToShow);
					}

					holder.textViewContent.setText(result);
				}
				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
			}
			else if(messageType==MegaChatMessage.TYPE_PRIV_CHANGE){
				log("PRIVILEGE CHANGE message");

				int privilege = chat.getLastMessagePriv();
				log("Privilege of the user: "+privilege);

				String privilegeString = "";
				if(privilege==MegaChatRoom.PRIV_MODERATOR){
					privilegeString = context.getString(R.string.administrator_permission_label_participants_panel);
				}
				else if(privilege==MegaChatRoom.PRIV_STANDARD){
					privilegeString = context.getString(R.string.standard_permission_label_participants_panel);
				}
				else if(privilege==MegaChatRoom.PRIV_RO){
					privilegeString = context.getString(R.string.observer_permission_label_participants_panel);
				}
				else {
					log("Change to other");
					privilegeString = "Unknow";
				}

				String textToShow = "";

				if(chat.getLastMessageHandle()==megaChatApi.getMyUserHandle()){
					log("a moderator change my privilege");

					if(chat.getLastMessageSender()==megaChatApi.getMyUserHandle()){
						log("I changed my Own permission");
						String myFullName = megaChatApi.getMyFullname();
						if(myFullName==null){
							myFullName = "";
						}
						if(myFullName.trim().length()<=0){
							myFullName = megaChatApi.getMyEmail();
						}
						textToShow = String.format(context.getString(R.string.message_permissions_changed), myFullName, privilegeString, myFullName);
						try{
							textToShow = textToShow.replace("[A]", "");
							textToShow = textToShow.replace("[/A]", "");
							textToShow = textToShow.replace("[B]", "");
							textToShow = textToShow.replace("[/B]", "");
							textToShow = textToShow.replace("[C]", "");
							textToShow = textToShow.replace("[/C]", "");
							textToShow = textToShow.replace("[D]", "");
							textToShow = textToShow.replace("[/D]", "");
							textToShow = textToShow.replace("[E]", "");
							textToShow = textToShow.replace("[/E]", "");
						}
						catch (Exception e){}
					}
					else{
						log("I was change by someone");
						MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());

						String fullNameAction = chatRoom.getPeerFullnameByHandle(chat.getLastMessageSender());
						if(fullNameAction==null){
							fullNameAction = "";
						}

						if(fullNameAction.trim().length()<=0){
							fullNameAction = cC.getFullName(chat.getLastMessageSender(), chat.getChatId());
						}

						if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
							if(fullNameAction.isEmpty()){
								if(!(holder.nameRequestedAction)){
									log("3-Call for nonContactName: "+ chat.getLastMessageSender());
									fullNameAction = "Unknown name";
									holder.nameRequestedAction=true;
									holder.userHandle = chat.getLastMessageSender();
									ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageSender());
									megaChatApi.getUserFirstname(chat.getLastMessageSender(), listener);
									megaChatApi.getUserLastname(chat.getLastMessageSender(), listener);
									megaChatApi.getUserEmail(chat.getLastMessageSender(), listener);
								}
								else{
									log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
								}
							}
						}
						String myFullName = megaChatApi.getMyFullname();
						if(myFullName==null){
							myFullName = "";
						}
						if(myFullName.trim().length()<=0){
							myFullName = megaChatApi.getMyEmail();
						}

						textToShow = String.format(context.getString(R.string.message_permissions_changed), myFullName, privilegeString, fullNameAction);
						try{
							textToShow = textToShow.replace("[A]", "");
							textToShow = textToShow.replace("[/A]", "");
							textToShow = textToShow.replace("[B]", "");
							textToShow = textToShow.replace("[/B]", "");
							textToShow = textToShow.replace("[C]", "");
							textToShow = textToShow.replace("[/C]", "");
							textToShow = textToShow.replace("[D]", "");
							textToShow = textToShow.replace("[/D]", "");
							textToShow = textToShow.replace("[E]", "");
							textToShow = textToShow.replace("[/E]", "");
						}
						catch (Exception e){}
					}
				}
				else{
					log("Participant privilege change!");

					MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());
					String fullNameTitle = chatRoom.getPeerFullnameByHandle(chat.getLastMessageHandle());
					if(fullNameTitle==null){
						fullNameTitle = "";
					}

					if(fullNameTitle.trim().length()<=0){
						fullNameTitle = cC.getFullName(chat.getLastMessageHandle(), chat.getChatId());
					}

					if(fullNameTitle.trim().length()<=0){
						if(!(holder.nameRequestedAction)){
							log("3-Call for nonContactName: "+ chat.getLastMessageHandle());
							fullNameTitle = "Unknown name";
							holder.nameRequestedAction=true;
							holder.userHandle = chat.getLastMessageHandle();
							ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageHandle());
							megaChatApi.getUserFirstname(chat.getLastMessageHandle(), listener);
							megaChatApi.getUserLastname(chat.getLastMessageHandle(), listener);
							megaChatApi.getUserEmail(chat.getLastMessageHandle(), listener);
						}
						else{
							log("4-Name already asked and no name received: "+ chat.getLastMessageHandle());
						}
					}

					if(chat.getLastMessageSender()==megaChatApi.getMyUserHandle()){
						log("The privilege was change by me");
						String myFullName = megaChatApi.getMyFullname();
						if(myFullName==null){
							myFullName = "";
						}
						if(myFullName.trim().length()<=0){
							myFullName = megaChatApi.getMyEmail();
						}
						textToShow = String.format(context.getString(R.string.message_permissions_changed), fullNameTitle, privilegeString, myFullName);
						try{
							textToShow = textToShow.replace("[A]", "");
							textToShow = textToShow.replace("[/A]", "");
							textToShow = textToShow.replace("[B]", "");
							textToShow = textToShow.replace("[/B]", "");
							textToShow = textToShow.replace("[C]", "");
							textToShow = textToShow.replace("[/C]", "");
							textToShow = textToShow.replace("[D]", "");
							textToShow = textToShow.replace("[/D]", "");
							textToShow = textToShow.replace("[E]", "");
							textToShow = textToShow.replace("[/E]", "");
						}
						catch (Exception e){}

					}
					else{
						log("By other");
						String fullNameAction = chatRoom.getPeerFullnameByHandle(chat.getLastMessageSender());
						if(fullNameAction==null){
							fullNameAction = "";
						}

						if(fullNameAction.trim().length()<=0){
							fullNameAction = cC.getFullName(chat.getLastMessageSender(), chat.getChatId());
						}

						if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
							if(fullNameAction.isEmpty()){
								if(!(holder.nameRequestedAction)){
									log("3-Call for nonContactName: "+ chat.getLastMessageSender());
									fullNameAction = "Unknown name";
									holder.nameRequestedAction=true;
									holder.userHandle = chat.getLastMessageSender();
									ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageSender());
									megaChatApi.getUserFirstname(chat.getLastMessageSender(), listener);
									megaChatApi.getUserLastname(chat.getLastMessageSender(), listener);
									megaChatApi.getUserEmail(chat.getLastMessageSender(), listener);
								}
								else{
									log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
								}
							}
						}

						textToShow = String.format(context.getString(R.string.message_permissions_changed), fullNameTitle, privilegeString, fullNameAction);
						try{
							textToShow = textToShow.replace("[A]", "");
							textToShow = textToShow.replace("[/A]", "");
							textToShow = textToShow.replace("[B]", "");
							textToShow = textToShow.replace("[/B]", "");
							textToShow = textToShow.replace("[C]", "");
							textToShow = textToShow.replace("[/C]", "");
							textToShow = textToShow.replace("[D]", "");
							textToShow = textToShow.replace("[/D]", "");
							textToShow = textToShow.replace("[E]", "");
							textToShow = textToShow.replace("[/E]", "");
						}
						catch (Exception e){}
					}
				}

				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}

				holder.textViewContent.setText(result);

				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
			}
			else if(messageType==MegaChatMessage.TYPE_TRUNCATE){
				log("Message type TRUNCATE");

				String textToShow = null;
				if(chat.getLastMessageSender() == megaChatApi.getMyUserHandle()){
					String myFullName = megaChatApi.getMyFullname();
					if(myFullName==null){
						myFullName = "";
					}
					if(myFullName.trim().length()<=0){
						myFullName = megaChatApi.getMyEmail();
					}
					textToShow = String.format(context.getString(R.string.history_cleared_by), myFullName);
				}
				else{
					MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());

					String fullNameAction = chatRoom.getPeerFullnameByHandle(chat.getLastMessageSender());
					if(fullNameAction==null){
						fullNameAction = "";
					}

					if(fullNameAction.trim().length()<=0){
						fullNameAction = cC.getFullName(chat.getLastMessageSender(), chat.getChatId());
					}

					if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
						if(fullNameAction.isEmpty()){
							if(!(holder.nameRequestedAction)){
								log("3-Call for nonContactName: "+ chat.getLastMessageSender());
								fullNameAction = "Unknown name";
								holder.nameRequestedAction=true;
								holder.userHandle = chat.getLastMessageSender();
								ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageSender());
								megaChatApi.getUserFirstname(chat.getLastMessageSender(), listener);
								megaChatApi.getUserLastname(chat.getLastMessageSender(), listener);
								megaChatApi.getUserEmail(chat.getLastMessageSender(), listener);
							}
							else{
								log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
							}
						}
					}

					textToShow = String.format(context.getString(R.string.history_cleared_by), fullNameAction);
				}

				try{
					textToShow = textToShow.replace("[A]", "");
					textToShow = textToShow.replace("[/A]", "");
					textToShow = textToShow.replace("[B]", "");
					textToShow = textToShow.replace("[/B]", "");
				}
				catch (Exception e){}

				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}

				holder.textViewContent.setText(result);

				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));

			}
			else if(messageType==MegaChatMessage.TYPE_CHAT_TITLE) {

				String messageContent = chat.getLastMessage();

				String textToShow = null;
				if(chat.getLastMessageSender() == megaChatApi.getMyUserHandle()){
					String myFullName = megaChatApi.getMyFullname();
					if(myFullName==null){
						myFullName = "";
					}
					if(myFullName.trim().length()<=0){
						myFullName = megaChatApi.getMyEmail();
					}
					textToShow = String.format(context.getString(R.string.change_title_messages), myFullName, messageContent);
				}
				else{

					MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());

					String fullNameAction = chatRoom.getPeerFullnameByHandle(chat.getLastMessageSender());
					if(fullNameAction==null){
						fullNameAction = "";
					}

					if(fullNameAction.trim().length()<=0){
						fullNameAction = cC.getFullName(chat.getLastMessageSender(), chat.getChatId());
					}
					if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
						if(fullNameAction.isEmpty()){
							if(!(holder.nameRequestedAction)){
								log("3-Call for nonContactName: "+ chat.getLastMessageSender());
								fullNameAction = "Unknown name";
								holder.nameRequestedAction=true;
								holder.userHandle = chat.getLastMessageSender();
								ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, chat.getLastMessageSender());
								megaChatApi.getUserFirstname(chat.getLastMessageSender(), listener);
								megaChatApi.getUserLastname(chat.getLastMessageSender(), listener);
								megaChatApi.getUserEmail(chat.getLastMessageSender(), listener);
							}
							else{
								log("4-Name already asked and no name received: "+ chat.getLastMessageSender());
							}
						}
					}

					textToShow = String.format(context.getString(R.string.change_title_messages), fullNameAction, messageContent);
				}

				try {
					textToShow = textToShow.replace("[A]", "");
					textToShow = textToShow.replace("[/A]", "");
					textToShow = textToShow.replace("[B]", "");
					textToShow = textToShow.replace("[/B]", "");
					textToShow = textToShow.replace("[C]", "");
					textToShow = textToShow.replace("[/C]", "");
				} catch (Exception e) {
				}

				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}

				holder.textViewContent.setText(result);

				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
			}
			else if(messageType==MegaChatMessage.TYPE_CALL_ENDED){
				String messageContent = chat.getLastMessage();

				char separator = 0x01;
				String separatorString = separator + "";

				String [] sp = messageContent.split(separatorString);

				String textToShow = "";

				if(sp.length>=2){

					String durationString = sp[0];
					String termCodeString = sp[1];

					int duration = Integer.parseInt(durationString);
					int termCode = Integer.parseInt(termCodeString);


					switch(termCode){
						case MegaChatMessage.END_CALL_REASON_ENDED:{

							int minutes = (duration % 3600) / 60;
							int seconds = duration % 60;

							if(minutes == 0){
								textToShow = context.getResources().getQuantityString(R.plurals.plural_call_ended_messages_just_seconds, seconds, seconds);
							}
							else{
								if(seconds == 0){
									textToShow = context.getResources().getQuantityString(R.plurals.plural_call_ended_messages_with_minutes, minutes, minutes);
								}
								else if (seconds == 1){
									textToShow = context.getResources().getQuantityString(R.plurals.plural_call_ended_messages_with_one_second, minutes, minutes, seconds);
								}
								else{
									textToShow = context.getResources().getQuantityString(R.plurals.plural_call_ended_messages_with_more_seconds, minutes, minutes, seconds);
								}
							}

							try {
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
								textToShow = textToShow.replace("[C]", "");
								textToShow = textToShow.replace("[/C]", "");
							} catch (Exception e) {
							}

							break;
						}
						case MegaChatMessage.END_CALL_REASON_REJECTED:{

							textToShow = String.format(context.getString(R.string.call_rejected_messages));
							try {
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
							} catch (Exception e) {
							}

							break;
						}
						case MegaChatMessage.END_CALL_REASON_NO_ANSWER:{

							long lastMsgSender = chat.getLastMessageSender();
							if(lastMsgSender==megaChatApi.getMyUserHandle()){
								textToShow = String.format(context.getString(R.string.call_not_answered_messages));
							}
							else{
								textToShow = String.format(context.getString(R.string.call_missed_messages));
							}

							try {
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
							} catch (Exception e) {
							}

							break;
						}
						case MegaChatMessage.END_CALL_REASON_FAILED:{

							textToShow = String.format(context.getString(R.string.call_failed_messages));
							try {
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
							} catch (Exception e) {
							}

							break;
						}
						case MegaChatMessage.END_CALL_REASON_CANCELLED:{

							long lastMsgSender = chat.getLastMessageSender();
							if(lastMsgSender==megaChatApi.getMyUserHandle()){
								textToShow = String.format(context.getString(R.string.call_cancelled_messages));
							}
							else{
								textToShow = String.format(context.getString(R.string.call_missed_messages));
							}

							try {
								textToShow = textToShow.replace("[A]", "");
								textToShow = textToShow.replace("[/A]", "");
								textToShow = textToShow.replace("[B]", "");
								textToShow = textToShow.replace("[/B]", "");
							} catch (Exception e) {
							}

							break;
						}
					}
				}

				Spanned result = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);
				} else {
					result = Html.fromHtml(textToShow);
				}

				holder.textViewContent.setText(result);

				holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
			}
			else{
				//OTHER TYPE OF MESSAGE
				if(lastMessageString==null){
                    log("Message Type-> "+messageType+" last content is NULL ");
					lastMessageString = context.getString(R.string.error_message_unrecognizable);
				}
				else{
                    log("Message Type-> "+messageType+" last content: "+lastMessageString + "length: "+lastMessageString.length());
                }

                long lastMsgSender = chat.getLastMessageSender();
				if(lastMsgSender==megaChatApi.getMyUserHandle()){

					log("getLastMessageSender: the last message is mine: "+lastMsgSender);
					Spannable me = new SpannableString("Me: ");
					me.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.file_list_first_row)), 0, me.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					if(lastMessageString!=null) {
						Spannable myMessage = new SpannableString(lastMessageString);
						myMessage.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.file_list_second_row)), 0, myMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						CharSequence indexedText = TextUtils.concat(me, myMessage);
						holder.textViewContent.setText(indexedText);
					}
				}
				else{
					log("getLastMessageSender: The last message NOT mine"+lastMsgSender);

					MegaChatRoom chatRoom = megaChatApi.getChatRoom(chat.getChatId());

					if(chat.isGroup()){

						holder.currentPosition = position;
						holder.userHandle = lastMsgSender;

						String fullNameAction = chatRoom.getPeerFirstnameByHandle(lastMsgSender);
						if(fullNameAction==null){
							fullNameAction = "";
						}

						if(fullNameAction.trim().length()<=0){
							fullNameAction = cC.getFirstName(lastMsgSender, chatRoom);
						}

						if(fullNameAction.trim().length()<=0){

//					megaChatApi.getUserFirstname();
							if(fullNameAction.isEmpty()){
								if(!(holder.nameRequestedAction)){
									log("3-Call for nonContactName: "+ lastMsgSender);
									fullNameAction = "Unknown name";
									holder.nameRequestedAction=true;
									holder.userHandle = lastMsgSender;
									ChatNonContactNameListener listener = new ChatNonContactNameListener(context, holder, this, lastMsgSender);
									megaChatApi.getUserFirstname(lastMsgSender, listener);
									megaChatApi.getUserLastname(lastMsgSender, listener);
									megaChatApi.getUserEmail(lastMsgSender, listener);
								}
								else{
									log("4-Name already asked and no name received: "+ lastMsgSender);
								}
							}
						}

						Spannable name = new SpannableString(fullNameAction+": ");
						name.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						if(chat.getUnreadCount()==0){
							log("Message READ");

							Spannable myMessage = new SpannableString(lastMessageString);
							myMessage.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.file_list_second_row)), 0, myMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							CharSequence indexedText = TextUtils.concat(name, myMessage);
							holder.textViewContent.setText(indexedText);
						}
						else{
							log("Message NOt read");
							Spannable myMessage = new SpannableString(lastMessageString);
							myMessage.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accentColor)), 0, myMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							CharSequence indexedText = TextUtils.concat(name, myMessage);
							holder.textViewContent.setText(indexedText);
						}
					}
					else{
						if(chat.getUnreadCount()==0){
							log("Message READ");
							holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.file_list_second_row));
						}
						else{
							log("Message NOt read");
							holder.textViewContent.setTextColor(ContextCompat.getColor(context, R.color.accentColor));
						}

						holder.textViewContent.setText(lastMessageString);
					}

				}
			}
		}
		else{
			log("Holder is NULL: "+position);
			notifyItemChanged(position);
		}
	}
	
	public void setChats (ArrayList<MegaChatListItem> chats){
		log("SETCONTACTS!!!!");
		this.chats = chats;
		if(chats!=null)
		{
			log("num requests: "+chats.size());
		}

		positionClicked = -1;
//		listFragment.invalidate();
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

	public void updateMultiselectionPosition(int oldPosition){
		log("updateMultiselectionPosition");

		List<Integer> selected = getSelectedItems();
		boolean movedSelected = false;

		if(isItemChecked(oldPosition)){
			movedSelected=true;
		}

		selectedItems.clear();

		if(movedSelected){
			selectedItems.put(0, true);
		}

		for(int i=0;i<selected.size();i++){
			int pos = selected.get(i);
			if(pos!=oldPosition){
				if(pos<oldPosition){
					selectedItems.put(pos+1, true);
				}
				else{
					selectedItems.put(pos, true);
				}
			}

//			notifyItemChanged(pos);
//			notifyItemChanged(pos+1);
		}
	}

	public void modifyChat(ArrayList<MegaChatListItem> chats, int position){
		this.chats = chats;
		notifyItemChanged(position);
	}

	public void removeChat(ArrayList<MegaChatListItem> chats, int position){
		this.chats = chats;
		notifyItemRemoved(position);
	}
	
	private static void log(String log) {
		Util.log("MegaListChatLollipopAdapter", log);
	}
}