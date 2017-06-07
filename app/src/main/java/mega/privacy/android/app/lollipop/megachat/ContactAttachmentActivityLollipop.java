package mega.privacy.android.app.lollipop.megachat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactAdapter;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.R;
import mega.privacy.android.app.ShareInfo;
import mega.privacy.android.app.UploadService;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.lollipop.AddContactActivityLollipop;
import mega.privacy.android.app.lollipop.ContactInfoActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PinActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaSharedFolderLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.lollipop.listeners.FileContactMultipleRequestListener;
import mega.privacy.android.app.modalbottomsheet.FileContactsListBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.ContactAttachmentBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.NodeAttachmentBottomSheetDialogFragment;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatHandleList;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatPeerList;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaShare;
import nz.mega.sdk.MegaUser;

public class ContactAttachmentActivityLollipop extends PinActivityLollipop implements MegaRequestListenerInterface, MegaChatRequestListenerInterface, OnClickListener {

	MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;
	ActionBar aB;
	Toolbar tB;
	ContactAttachmentActivityLollipop contactAttachmentActivityLollipop = this;
	public MegaUser selectedUser;

	RelativeLayout container;
	RecyclerView listView;
	View separator;
	Button inviteButton;
	LinearLayout optionsBar;
	LinearLayoutManager mLayoutManager;

	ChatController cC;

	AndroidMegaChatMessage message = null;
	public long chatId;
	public long messageId;

	ArrayList<MegaContactAdapter> contacts;

	MegaContactsLollipopAdapter adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate");
		super.onCreate(savedInstanceState);
		
		if (megaApi == null){
			megaApi = ((MegaApplication) getApplication()).getMegaApi();
		}

		if (megaChatApi == null){
			megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
		}

		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);

		cC = new ChatController(this);

		Intent intent = getIntent();
		if (intent != null) {
			chatId = intent.getLongExtra("chatId", -1);
			messageId = intent.getLongExtra("messageId", -1);
			log("Id Chat and Message id: "+chatId+ "___"+messageId);
			MegaChatMessage messageMega = megaChatApi.getMessage(chatId, messageId);
			if(messageMega!=null){
				message = new AndroidMegaChatMessage(messageMega);
			}
		}

		if(message!=null){
			contacts = new ArrayList<>();

			long userCount  = message.getMessage().getUsersCount();
			for(int i=0; i<userCount;i++) {
				String name = "";
				name = message.getMessage().getUserName(i);
				if (name.trim().isEmpty()) {
					name = message.getMessage().getUserEmail(i);
				}
				String email = message.getMessage().getUserEmail(i);
				log("Contact Name: " + name);
				MegaUser contact = megaApi.getContact(email);
				if (contact != null) {
					MegaContactAdapter contactDB = new MegaContactAdapter(null, contact, name);
					contacts.add(contactDB);
				} else {
					log("Error - this contact is NULL");
				}
			}
		}
		else{
			finish();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_dark_primary_color));
		}

		setContentView(R.layout.activity_contact_attachment_chat);

		//Set toolbar
		tB = (Toolbar) findViewById(R.id.toolbar_contact_attachment_chat);
		setSupportActionBar(tB);
		aB = getSupportActionBar();
		aB.setDisplayHomeAsUpEnabled(true);
		aB.setDisplayShowHomeEnabled(true);
		aB.setTitle(getString(R.string.activity_title_contacts_attached));

		if(message.getMessage().getUserHandle()==megaChatApi.getMyUserHandle()) {
			aB.setSubtitle(megaChatApi.getMyFullname());
		}
		else{
			String fullNameAction = cC.getFullName(message.getMessage().getUserHandle(), chatId);

			if(fullNameAction==null){
				fullNameAction = "";
			}
			aB.setSubtitle(fullNameAction);
		}

		container = (RelativeLayout) findViewById(R.id.contact_attachment_chat);

		optionsBar = (LinearLayout) findViewById(R.id.options_contact_attachment_chat_layout);
		separator = (View) findViewById(R.id.contact_attachment_chat_separator_3);

		inviteButton = (Button) findViewById(R.id.contact_attachment_chat_invite_button);
		inviteButton.setOnClickListener(this);

		listView = (RecyclerView) findViewById(R.id.contact_attachment_chat_view_browser);
		listView.setClipToPadding(false);
		listView.addItemDecoration(new SimpleDividerItemDecoration(this, outMetrics));
		mLayoutManager = new LinearLayoutManager(this);
		listView.setLayoutManager(mLayoutManager);
		listView.setItemAnimator(new DefaultItemAnimator());

		if (adapter == null){
			adapter = new MegaContactsLollipopAdapter(this, contacts, listView);
		}

		adapter.setPositionClicked(-1);
		adapter.setMultipleSelect(false);

		listView.setAdapter(adapter);

		((MegaApplication) getApplication()).sendSignalPresenceActivity();

	}

	@Override
    protected void onDestroy(){
    	super.onDestroy();
    	
    	if(megaApi != null)
    	{
    		megaApi.removeRequestListener(this);
    	}
    }

	public void showOptionsPanel(MegaUser sUser){
		log("showNodeOptionsPanel-Offline");
		if(sUser!=null){
			this.selectedUser = sUser;
			ContactAttachmentBottomSheetDialogFragment bottomSheetDialogFragment = new ContactAttachmentBottomSheetDialogFragment();
			bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
		}
	}
	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		if (request.getType() == MegaRequest.TYPE_SHARE) {
			log("onRequestStart - Share");
		}
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,MegaError e) {
		log("onRequestFinish: " + request.getType() + "__" + request.getRequestString());

		if (request.getType() == MegaRequest.TYPE_INVITE_CONTACT){
			log("MegaRequest.TYPE_INVITE_CONTACT finished: "+request.getNumber());

			if(request.getNumber()== MegaContactRequest.INVITE_ACTION_REMIND){
				showSnackbar(getString(R.string.context_contact_invitation_resent));
			}
			else{
				if (e.getErrorCode() == MegaError.API_OK){
					log("OK INVITE CONTACT: "+request.getEmail());
					if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD)
					{
						showSnackbar(getString(R.string.context_contact_request_sent, request.getEmail()));
					}
				}
				else{
					log("Code: "+e.getErrorString());
					if(e.getErrorCode()==MegaError.API_EEXIST)
					{
						showSnackbar(getString(R.string.context_contact_already_invited, request.getEmail()));
					}
					else{
						showSnackbar(getString(R.string.general_error));
					}
					log("ERROR: " + e.getErrorCode() + "___" + e.getErrorString());
				}
			}
		}
	}


	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError");
	}
	
	public static void log(String log) {
		Util.log("ContactAttachmentActivityLollipop", log);
	}

	public void itemClick(int position) {
		log("itemClick");
		((MegaApplication) getApplication()).sendSignalPresenceActivity();
		MegaContactAdapter c = contacts.get(position);
//            showMsgNotSentPanel(m);
		if(c!=null){
			if(c.getMegaUser()!=null){
				Intent i = new Intent(this, ContactInfoActivityLollipop.class);
				i.putExtra("name", c.getMegaUser().getEmail());
				this.startActivity(i);
			}
			else{
				log("The contact is null");
			}
		}
	}

	@Override
	public void onClick(View v) {
		((MegaApplication) getApplication()).sendSignalPresenceActivity();
		switch (v.getId()){		
			case R.id.contact_attachment_chat_invite_button:{
				log("Click on Invite button");

				ArrayList<String> contactEmails = new ArrayList<>();
				ContactController contactControllerC = new ContactController(this);
				for(int i=0;i<contacts.size();i++){
					MegaContactAdapter contact = contacts.get(i);

					if(contact.getMegaUser().getVisibility()!=MegaUser.VISIBILITY_VISIBLE){
						String userMail = contact.getMegaUser().getEmail();
						contactEmails.add(userMail);
					}

				}
				if(contactEmails!=null){
					if(!contactEmails.isEmpty()){
						contactControllerC.inviteMultipleContacts(contactEmails);
					}
				}
				break;
			}
		}
	}

	public void setPositionClicked(int positionClicked){
		if (adapter != null){
			adapter.setPositionClicked(positionClicked);
		}	
	}
	
	public void notifyDataSetChanged(){		
		if (adapter != null){
			adapter.notifyDataSetChanged();
		}		
	}

	public void showSnackbar(String s){
		log("showSnackbar");
		Snackbar snackbar = Snackbar.make(container, s, Snackbar.LENGTH_LONG);
		TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		snackbarTextView.setMaxLines(5);
		snackbar.show();
	}

	public void startConversation(long handle){
		log("startConversation");
		MegaChatRoom chat = megaChatApi.getChatRoomByUser(handle);
		MegaChatPeerList peers = MegaChatPeerList.createInstance();
		if(chat==null){
			log("No chat, create it!");
			peers.addPeer(handle, MegaChatPeerList.PRIV_STANDARD);
			megaChatApi.createChat(false, peers, this);
		}
		else{
			log("There is already a chat, open it!");
			Intent intentOpenChat = new Intent(this, ChatActivityLollipop.class);
			intentOpenChat.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
			intentOpenChat.putExtra("CHAT_ID", chat.getChatId());
			finish();
			intentOpenChat.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intentOpenChat);
		}
	}

	@Override
	public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {
		log("onRequestStart: "+request.getRequestString());
	}

	@Override
	public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

	}

	@Override
	public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
		log("onRequestFinish: "+request.getRequestString());

		if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM){
			log("Create chat request finish!!!");
			if(e.getErrorCode()==MegaChatError.ERROR_OK){

				log("open new chat");
				Intent intent = new Intent(this, ChatActivityLollipop.class);
				intent.setAction(Constants.ACTION_CHAT_NEW);
				intent.putExtra("CHAT_ID", request.getChatHandle());
				finish();
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				this.startActivity(intent);

			}
			else{
				log("EEEERRRRROR WHEN CREATING CHAT " + e.getErrorString());
				showSnackbar(getString(R.string.create_chat_error));
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

	}
}

