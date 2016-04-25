package mega.privacy.android.app.lollipop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.util.ArrayList;

import mega.privacy.android.app.ContactsExplorerAdapter.OnItemCheckClickListener;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;


public class PhoneContactsActivityLollipop extends PinActivityLollipop implements OnClickListener, OnItemCheckClickListener, MegaRequestListenerInterface {

	public static String EXTRA_MEGA_CONTACTS = "mega_contacts";
	public static String EXTRA_CONTACTS = "extra_contacts";

	Handler handler;

	ActionBar aB;
	Toolbar tB;
	MegaApiAndroid megaApi;
	float scaleH, scaleW;
	float density;
	DisplayMetrics outMetrics;
	Display display;

	ImageView emptyImageView;
	TextView emptyTextView;
	private RecyclerView listView;
	private RecyclerView.LayoutManager mLayoutManager;

	PhoneContactsLollipopAdapter adapter;

	ArrayList<PhoneContacts> phoneContacts;

	public class PhoneContacts{
		String id;
		String name;
		String email;
		String phoneNumber;

		public PhoneContacts(String id, String name, String email, String phoneNumber) {
			this.id = id;
			this.name = name;
			this.email = email;
			this.phoneNumber = phoneNumber;
		}

		public String getId(){
			return id;
		}

		public String getName(){
			return name;
		}

		public String getEmail(){
			return email;
		}

		public String getPhoneNumber(){
			return phoneNumber;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        // do nothing
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

//	@SuppressLint("InlinedApi")
//	private ArrayList<PhoneContacts> getPhoneContacts() {
//       ArrayList<PhoneContacts> contactList = new ArrayList<PhoneContacts>();
//
//       try {
//
//            /**************************************************/
//
//            ContentResolver cr = getBaseContext().getContentResolver();
//
//            @SuppressLint("InlinedApi")
//            String SORT_ORDER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;
//
//            Cursor c = cr.query(
//                    Data.CONTENT_URI,
//                    null,
//                    "(" + Data.MIMETYPE + "= ? OR " + Data.MIMETYPE + "= ?) AND " +
//                    Data.CONTACT_ID + " IN (SELECT " + Contacts._ID + " FROM contacts WHERE " + Contacts.HAS_PHONE_NUMBER + "!=0) AND " + Contacts.IN_VISIBLE_GROUP + "=1",
//                    new String[]{Email.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE}, SORT_ORDER);
//
//            while (c.moveToNext()){
//            	long id = c.getLong(c.getColumnIndex(Data.CONTACT_ID));
//                String name = c.getString(c.getColumnIndex(Data.DISPLAY_NAME));
//                String data1 = c.getString(c.getColumnIndex(Data.DATA1));
//                String mimetype = c.getString(c.getColumnIndex(Data.MIMETYPE));
//                if (mimetype.compareTo(Email.CONTENT_ITEM_TYPE) == 0){
//                	log("ID: " + id + "___ NAME: " + name + "____ EMAIL: " + data1);
//                	PhoneContacts pc = new PhoneContacts(id, name, data1, null);
//                	contactList.add(pc);
//                }
//                else if (mimetype.compareTo(Phone.CONTENT_ITEM_TYPE) == 0){
//                	PhoneContacts pc = new PhoneContacts(id, name, null, data1);
//                	contactList.add(pc);
//                	log("ID: " + id + "___ NAME: " + name + "____ PHONE: " + data1);
//                }
//            }
//
//            c.close();
//
//            return contactList;
//
//        } catch (Exception e) {}
//
//        return null;
//    }

	public ArrayList<PhoneContacts> getContactsFromPhone(){

		String name;
		ArrayList<PhoneContacts> contactList = new ArrayList<PhoneContacts>();
		ContentResolver cr = getContentResolver();

		@SuppressLint("InlinedApi")
        String SORT_ORDER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,null, null, SORT_ORDER);

		if (cur.getCount() > 0) {
//			ArrayList<String> emailNameList = new ArrayList<String>();
//			ArrayList<String> emailPhoneList = new ArrayList<String>();

			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
						ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
				while (emails.moveToNext()) {
					// This would allow you get several email addresses
					String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
					log("Contact: "+ name + " ==> "+ emailAddress);
					if ((!emailAddress.equalsIgnoreCase("")) && (emailAddress.contains("@"))) {
						PhoneContacts contactPhone = new PhoneContacts(id, name, emailAddress, null);
						contactList.add(contactPhone);
					}
				}
				emails.close();
			}
		}
		return contactList;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		if (megaApi == null){
			MegaApplication app = (MegaApplication)getApplication();
			megaApi = app.getMegaApi();
		}

		setContentView(R.layout.activity_contactsexplorer);

		//Set toolbar
		tB = (Toolbar) findViewById(R.id.toolbar_contacts_explorer);
		setSupportActionBar(tB);
		aB = getSupportActionBar();
//		aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
		aB.setDisplayHomeAsUpEnabled(true);
		aB.setDisplayShowHomeEnabled(true);

		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;
		handler = new Handler();

		aB.setTitle(getResources().getString(R.string.section_contacts));

		listView = (RecyclerView) findViewById(R.id.contacts_explorer_list_view);
		listView.addItemDecoration(new SimpleDividerItemDecoration(this));
		mLayoutManager = new LinearLayoutManager(this);
		listView.setLayoutManager(mLayoutManager);

		emptyImageView = (ImageView) findViewById(R.id.contact_explorer_list_empty_image);
		emptyTextView = (TextView) findViewById(R.id.contact_explorer_list_empty_text);

		phoneContacts = getContactsFromPhone();

		if (adapter == null){
			adapter = new PhoneContactsLollipopAdapter(this, phoneContacts);

			listView.setAdapter(adapter);

			adapter.SetOnItemClickListener(new PhoneContactsLollipopAdapter.OnItemClickListener() {

				@Override
				public void onItemClick(View view, int position) {
					itemClick(view, position);
				}
			});
		}
		else{
			adapter.setContacts(phoneContacts);
		}

		if (adapter.getItemCount() == 0){
			listView.setVisibility(View.GONE);
			emptyImageView.setVisibility(View.VISIBLE);
			emptyTextView.setVisibility(View.VISIBLE);
		}
		else{
			listView.setVisibility(View.VISIBLE);
			emptyImageView.setVisibility(View.GONE);
			emptyTextView.setVisibility(View.GONE);
		}
	}

//	@Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//		log("onCreateOptionsMenuLollipop");
//
//		// Inflate the menu items for use in the action bar
//	    MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.menu.contacts_explorer_action, menu);
//
//	    addContactMenuItem = menu.findItem(R.id.cab_menu_add_contact);
//
//		if(sendToInbox==0){
//			addContactMenuItem.setVisible(true);
//		}
//		else{
//			addContactMenuItem.setVisible(false);
//		}
//
//	    return super.onCreateOptionsMenu(menu);
//	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
			case android.R.id.home: {
				finish();
				break;
			}
//			case R.id.cab_menu_add_contact:{
//				showNewContactDialog();
//        		break;
//			}
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Validate email
	 */
	private String getEmailError(String value) {
		log("getEmailError");
		if (value.length() == 0) {
			return getString(R.string.error_enter_email);
		}
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
			return getString(R.string.error_invalid_email);
		}
		return null;
	}

	@Override
	public void onClick(View v) {
//		switch (v.getId()) {
//			case R.id.home:{
//				finish();
//			}
//		}		
	}

	@Override
	public void onItemCheckClick(int position) {

	}

	public void itemClick(View view, int position) {
		log("on item click");		

		final PhoneContacts contact = (PhoneContacts) adapter.getDocumentAt(position);
		if(contact == null)
		{
			return;
		}

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						inviteContact(contact.getEmail());
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						//No button clicked
						break;
				}
			}
		};

		android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
		String message= getResources().getString(R.string.confirmation_remove_contact,contact.getEmail());
		builder.setMessage(message).setPositiveButton(R.string.general_remove, dialogClickListener)
				.setNegativeButton(R.string.general_cancel, dialogClickListener).show();

	}

	public void inviteContact(String email){
		megaApi.inviteContact(email, null, MegaContactRequest.INVITE_ACTION_ADD, this);
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {

	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
		log("onRequest finished");
//		if (e.getErrorCode() == MegaError.API_OK) {
//			Snackbar.make(fragmentContainer, getString(R.string.context_contact_invitation_sent), Snackbar.LENGTH_LONG).show();
//		}
//		else{
//			if(e.getErrorCode()==MegaError.API_EEXIST)
//			{
//				Snackbar.make(fragmentContainer, getString(R.string.context_contact_already_exists, request.getEmail()), Snackbar.LENGTH_LONG).show();
//			}
//			else{
//				Snackbar.make(fragmentContainer, getString(R.string.general_error), Snackbar.LENGTH_LONG).show();
//			}
//			log("ERROR: " + e.getErrorCode() + "___" + e.getErrorString());
//		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

	}


	public static void log(String message) {
		Util.log("PhoneContactsActivityLollipop", message);
	}
}
