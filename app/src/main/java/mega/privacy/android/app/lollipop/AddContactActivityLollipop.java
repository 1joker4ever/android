package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactAdapter;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.components.tokenautocomplete.ContactInfo;
import mega.privacy.android.app.components.tokenautocomplete.ContactsCompletionView;
import mega.privacy.android.app.components.tokenautocomplete.TokenCompleteTextView;
import mega.privacy.android.app.lollipop.adapters.MegaContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.PhoneContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaUser;


public class AddContactActivityLollipop extends PinActivityLollipop implements TokenCompleteTextView.TokenListener<ContactInfo>, View.OnClickListener, RecyclerView.OnItemTouchListener {

    public static String ACTION_PICK_CONTACT_SHARE_FOLDER = "ACTION_PICK_CONTACT_SHARE_FOLDER";
    public static String ACTION_PICK_CONTACT_SEND_FILE = "ACTION_PICK_CONTACT_SEND_FILE";

    DisplayMetrics outMetrics;
    private android.support.v7.app.AlertDialog shareFolderDialog;
    MegaApplication app;
    MegaApiAndroid megaApi;
    DatabaseHandler dbH = null;
    int contactType = 0;
    int multipleSelectIntent;
    int sendToInbox;
    long nodeHandle = -1;
    long[] nodeHandles;
    Handler handler;

    AddContactActivityLollipop addContactActivityLollipop;

    Toolbar tB;
    ActionBar aB;

    ImageView sendButton;
    ImageView backButton;
    ContactsCompletionView completionView;
    RecyclerView recyclerView;
    ImageView emptyImageView;
    TextView emptyTextView;
    ProgressBar progressBar;
    EditText addContactEditText;

    ContactInfo[] people;
    MegaContactsLollipopAdapter adapterMEGA;
    GestureDetectorCompat detector;

    PhoneContactsLollipopAdapter adapterPhone;

    ArrayList<PhoneContactInfo> phoneContacts;
//    ArrayList<PhoneContactInfo> selectedContactsPhone = new ArrayList<PhoneContactInfo>();
    SparseBooleanArray selectedContactsPhone = new SparseBooleanArray();
    SparseBooleanArray selectedContactsMEGA = new SparseBooleanArray();
    ArrayList<PhoneContactInfo> filteredContactsPhone = new ArrayList<PhoneContactInfo>();

    ArrayList<MegaUser> contactsMEGA;
    ArrayList<MegaContactAdapter> visibleContactsMEGA = new ArrayList<MegaContactAdapter>();

    boolean itemClickPressed = false;

    public static String EXTRA_MEGA_CONTACTS = "mega_contacts";
    public static String EXTRA_CONTACTS = "extra_contacts";
    public static String EXTRA_NODE_HANDLE = "node_handle";

    private MenuItem sendInvitationMenuItem;
    private MenuItem writeMailMenuItem;

    public class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        public void onLongPress(MotionEvent e) {
//            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
//            int position = recyclerView.getChildPosition(view);
//
//            // handle long press
//            if (!adapter.isMultipleSelect()){
//                adapter.setMultipleSelect(true);
//
//                actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
//
//                itemClick(position);
//            }
            super.onLongPress(e);
        }
    }

    private class PhoneContactsTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            phoneContacts = getPhoneContacts();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean bool) {

            progressBar.setVisibility(View.GONE);

            List<ContactInfo> tokens = completionView.getObjects();
            if (tokens != null) {
                log("tokens.size() = " + tokens.size());

                if (tokens.size() == 0) {
                    for (int i = 0; i < phoneContacts.size(); i++) {
                        log("filteredContacts.add(visibleContacts.get(" + i + ") = " + phoneContacts.get(i).getEmail());
                        filteredContactsPhone.add(phoneContacts.get(i));
                    }
                } else {
                    for (int i = 0; i < phoneContacts.size(); i++) {
                        boolean found = false;
                        for (int j = 0; j < tokens.size(); j++) {
                            log("tokens.get(" + j + ").getEmail() = " + tokens.get(j).getEmail());
                            log("visibleContacts.get(" + i + ").getEmail() = " + phoneContacts.get(i).getEmail());
                            if (tokens.get(j).getEmail().compareTo(phoneContacts.get(i).getEmail()) == 0) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            log("!found -> filteredContacts.add(visibleContacts.get(" + i + ") = " + phoneContacts.get(i).getEmail());
                            filteredContactsPhone.add(phoneContacts.get(i));
                        }
                    }
                }
            }

            if (adapterPhone == null){
                adapterPhone = new PhoneContactsLollipopAdapter(addContactActivityLollipop, phoneContacts);

                recyclerView.setAdapter(adapterPhone);

                adapterPhone.SetOnItemClickListener(new PhoneContactsLollipopAdapter.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        itemClick(view, position);
                    }
                });
            }
            else{
                adapterPhone.setContacts(phoneContacts);
            }

            if (adapterPhone.getItemCount() == 0){
                recyclerView.setVisibility(View.GONE);
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);
            }
            else{
                recyclerView.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
            }

            String text = completionView.getText().toString();
            text.replace(",", "");
            text.replace(" ", "");
            if (text.compareTo("") != 0){
                onTextChanged(text, -1, -1, -1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_contact, menu);

        sendInvitationMenuItem = menu.findItem(R.id.action_send_invitation);
        writeMailMenuItem = menu.findItem(R.id.action_write_mail);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        log("onPrepareOptionsMenu");
        if(sendToInbox==0){
            if (writeMailMenuItem != null){
                writeMailMenuItem.setVisible(true);
            }
        } else {
            if (writeMailMenuItem != null){
                writeMailMenuItem.setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected");
        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        int id = item.getItemId();
        switch(id) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            case R.id.action_send_invitation:{
                List<ContactInfo> tokens = completionView.getObjects();
                if (contactType == Constants.CONTACT_TYPE_DEVICE){
                    inviteContacts(selectedContactsPhone);
                }
                else if (contactType == Constants.CONTACT_TYPE_MEGA){
                    writeMailMenuItem.setVisible(false);
                    setResultContacts(selectedContactsMEGA, true);
                }
                break;
            }
            case R.id.action_write_mail:{
                showNewContactDialog();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate");

        super.onCreate(savedInstanceState);

        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        if (getIntent() != null){
            contactType = getIntent().getIntExtra("contactType", Constants.CONTACT_TYPE_MEGA);

            if (contactType == Constants.CONTACT_TYPE_MEGA){
                multipleSelectIntent = getIntent().getIntExtra("MULTISELECT", -1);
                if(multipleSelectIntent==0){
                    nodeHandle =  getIntent().getLongExtra(EXTRA_NODE_HANDLE, -1);
                }
                else if(multipleSelectIntent==1){
                    log("onCreate multiselect YES!");
                    nodeHandles=getIntent().getLongArrayExtra(EXTRA_NODE_HANDLE);
                }
                sendToInbox= getIntent().getIntExtra("SEND_FILE", -1);
            }
        }

        Display display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;

        app = (MegaApplication)getApplication();
        megaApi = app.getMegaApi();

        addContactActivityLollipop = this;


        log("retryPendingConnections()");
        if (megaApi != null){
            log("---------retryPendingConnections");
            megaApi.retryPendingConnections();
        }

        dbH = DatabaseHandler.getDbHandler(this);
        handler = new Handler();
        setContentView(R.layout.activity_add_contact);

        tB = (Toolbar) findViewById(R.id.add_contact_toolbar);
        if(tB==null){
            log("Tb is Null");
            return;
        }

        tB.setVisibility(View.VISIBLE);
        setSupportActionBar(tB);
        aB = getSupportActionBar();
        log("aB.setHomeAsUpIndicator_1");
        aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);


//        people = new ContactInfo[]{
//                new ContactInfo("Marshall Weir", "marshall@example.com"),
//                new ContactInfo("Margaret Smith", "margaret@example.com"),
//                new ContactInfo("Max Jordan", "max@example.com"),
//                new ContactInfo("Meg Peterson", "meg@example.com"),
//                new ContactInfo("Amanda Johnson", "amanda@example.com"),
//                new ContactInfo("Terry Anderson", "terry@example.com"),
//                new ContactInfo("Siniša Damianos Pilirani Karoline Slootmaekers",
//                        "siniša_damianos_pilirani_karoline_slootmaekers@example.com")
//        };

        completionView = (ContactsCompletionView)findViewById(R.id.add_contact_chips);
//        adapter = new FilteredArrayAdapter<Person>(this, R.layout.person_layout, people) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                if (convertView == null) {
//
//                    LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//                    convertView = l.inflate(R.layout.person_layout, parent, false);
//                }
//
//                Person p = getItem(position);
//                ((TextView)convertView.findViewById(R.id.name)).setText(p.getName());
//                ((TextView)convertView.findViewById(R.id.email)).setText(p.getEmail());
//
//                return convertView;
//            }
//
//            @Override
//            protected boolean keepObject(Person person, String mask) {
//                mask = mask.toLowerCase();
//                return person.getName().toLowerCase().startsWith(mask) || person.getEmail().toLowerCase().startsWith(mask);
//            }
//        };
//        completionView.setAdapter(adapter);

        completionView.setTokenListener(this);
        completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        completionView.setOnTextChangeListener(this);
//
//
//        completionView.addTextChangedListener(new TextWatcher() {
//
//            String text = "";
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                log("onTextChanged: " + s.toString() + "_ " + start + "__" + before + "__" + count);
//
//                String [] spl = s.toString().split(",,");
//                for (int i=0;i<spl.length;i++){
//                    log("SS: _" + spl[i] + "_");
//                }
//
//                log("INDEXOF: " + s.toString().indexOf(",, "));
//                text = "" + s.toString();
//                int ind = text.indexOf(",, ");
//                while(ind >= 0){
//                    log("IND: " + ind);
//                    if (ind >= 0){
//                        text = text.substring(ind+3, text.length());
//                    }
//                    ind = text.indexOf(",, ");
//                }
//
//                log("text: _" + text + "_");
//
//                ArrayList<MegaUser> filteredContactsAfterText = new ArrayList<MegaUser>();
//                if (filteredContacts != null){
//                    for (int i=0;i<filteredContacts.size();i++){
//                        if (filteredContacts.get(i).getEmail().startsWith(text.trim()) == true){
//                            filteredContactsAfterText.add(filteredContacts.get(i));
//                        }
//                        else{
//                            MegaContact contactDB = dbH.findContactByHandle(String.valueOf(filteredContacts.get(i).getHandle()));
//                            if(contactDB!=null){
//                                String name = contactDB.getName();
//                                String lastName = contactDB.getLastName();
//                                if (name.startsWith(text.trim()) == true){
//                                    filteredContactsAfterText.add(filteredContacts.get(i));
//                                }
//                                else if (lastName.startsWith(text.trim()) == true){
//                                    filteredContactsAfterText.add(filteredContacts.get(i));
//                                }
//                            }
//                        }
//                    }
//
//                    if (adapter != null){
//                        adapter.setContacts(filteredContactsAfterText);
//                        adapter.setAdapterType(MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
//
//                        if (adapter.getItemCount() == 0){
//
//                            emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
//                            emptyTextView.setText(R.string.contacts_list_empty_text);
//                            recyclerView.setVisibility(View.GONE);
//                            emptyImageView.setVisibility(View.VISIBLE);
//                            emptyTextView.setVisibility(View.VISIBLE);
//                        }
//                        else{
//                            recyclerView.setVisibility(View.VISIBLE);
//                            emptyImageView.setVisibility(View.GONE);
//                            emptyTextView.setVisibility(View.GONE);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable textEd) {
//                log("afterTextChanged: " + textEd);
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                log("beforeTextChanged: " + s.toString() + "_" + start + "__" + after + "__" + count);
//            }
//        });

        addContactEditText = (EditText) findViewById(R.id.add_contact_edittext);
        addContactEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                log("onTextChanged: " + s.toString() + "_ " + start + "__" + before + "__" + count);

                String text = "";
                for (int i=0;i<s.toString().length();i++){
                    if (s.toString().charAt(i) != ','){
                        text = text + s.toString().charAt(i);
                    }
                }

                log("text: _" + text + "_");

                if (contactType == Constants.CONTACT_TYPE_MEGA){
                    ArrayList<MegaContactAdapter> filteredContactsAfterText = new ArrayList<MegaContactAdapter>();
                    if (visibleContactsMEGA != null) {
                        for (int i = 0; i < visibleContactsMEGA.size(); i++) {
                            try {
                                String email = visibleContactsMEGA.get(i).getMegaUser().getEmail();
                                String emailPart = "";

                                if (email != null){
                                    if (email.length() >= text.trim().length()){
                                        emailPart = email.substring(0, text.trim().length());
                                    }
                                }

                                Collator collator = Collator.getInstance(Locale.getDefault());
                                collator.setStrength(Collator.PRIMARY);

                                if (collator.compare(text.trim(), emailPart) == 0){
                                    filteredContactsAfterText.add(visibleContactsMEGA.get(i));
                                }
                                else {
                                    MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(visibleContactsMEGA.get(i).getMegaUser().getHandle()));
                                    if (contactDB != null) {
                                        String name = contactDB.getName();
                                        String lastName = contactDB.getLastName();
                                        String namePart = "";
                                        String lastNamePart = "";
                                        String fullNamePart = "";

                                        if (name != null){
                                            if (name.length() >= text.trim().length()){
                                                namePart = name.substring(0, text.trim().length());
                                            }
                                        }

                                        if (lastName != null){
                                            if (lastName.length() >= text.trim().length()){
                                                lastNamePart = lastName.substring(0, text.trim().length());
                                            }
                                        }

                                        if ((name != null) && (lastName != null)) {
                                            String fullName = name + " " + lastName;
                                            if (fullName != null) {
                                                if (fullName.trim().length() >= text.trim().length()){
                                                    fullNamePart = fullName.substring(0, text.trim().length());
                                                }
                                            }
                                        }

                                        if (collator.compare(text.trim(), namePart) == 0){
                                            filteredContactsAfterText.add(visibleContactsMEGA.get(i));
                                        }
                                        else if (collator.compare(text.trim(), lastNamePart) == 0){
                                            filteredContactsAfterText.add(visibleContactsMEGA.get(i));
                                        }
                                        else if (collator.compare(text.trim(), fullNamePart) == 0){
                                            filteredContactsAfterText.add(visibleContactsMEGA.get(i));
                                        }
                                    }
                                }
                            }
                            catch (Exception e) { log ("Exception: " + e.getMessage()); }
                        }

                        if (adapterMEGA != null) {
                            adapterMEGA.setContacts(filteredContactsAfterText);
                            adapterMEGA.setAdapterType(MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);

                            if (adapterMEGA.getItemCount() == 0) {

                                emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
                                emptyTextView.setText(R.string.contacts_list_empty_text);
                                recyclerView.setVisibility(View.GONE);
                                emptyImageView.setVisibility(View.VISIBLE);
                                emptyTextView.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyImageView.setVisibility(View.GONE);
                                emptyTextView.setVisibility(View.GONE);
                            }
                        }
                    }
                }
                else if (contactType == Constants.CONTACT_TYPE_DEVICE){
                    ArrayList<PhoneContactInfo> filteredContactsAfterText = new ArrayList<PhoneContactInfo>();
                    if (filteredContactsPhone != null) {
                        for (int i = 0; i < filteredContactsPhone.size(); i++) {
                            try {
                                String email = filteredContactsPhone.get(i).getEmail();
                                String name = filteredContactsPhone.get(i).getName();
                                String phoneNumber = filteredContactsPhone.get(i).getPhoneNumber();
                                String emailPart = "";
                                String namePart = "";
                                String phoneNumberPart = "";

                                if (email != null){
                                    if (email.length() >= text.trim().length()){
                                        emailPart = email.substring(0, text.trim().length());
                                    }
                                }

                                if (name != null){
                                    if (name.trim().length() >= text.trim().length()){
                                        namePart = name.substring(0, text.trim().length());
                                    }
                                }

                                if (phoneNumber != null){
                                    if (phoneNumber.length() >= text.trim().length()){
                                        phoneNumberPart = phoneNumber.substring(0, text.trim().length());
                                    }
                                }

                                Collator collator = Collator.getInstance(Locale.getDefault());
                                collator.setStrength(Collator.PRIMARY);

                                if (collator.compare(text.trim(), emailPart) == 0){
                                    filteredContactsAfterText.add(filteredContactsPhone.get(i));
                                }
                                else if (collator.compare(text.trim(), namePart) == 0){
                                    filteredContactsAfterText.add(filteredContactsPhone.get(i));
                                }
                                else if (collator.compare(text.trim(), phoneNumberPart) == 0){
                                    filteredContactsAfterText.add(filteredContactsPhone.get(i));
                                }
//                        if (filteredContactsPhone.get(i).getEmail().startsWith(text.trim()) == true) {
//                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
//                        } else if (filteredContactsPhone.get(i).getName().startsWith(text.trim()) == true) {
//                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
//                        } else if (filteredContactsPhone.get(i).getPhoneNumber().startsWith(text.trim()) == true) {
//                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
//                        }
                            }
                            catch (Exception e) { log ("Exception: " + e.getMessage()); }
                        }

                        if (adapterPhone != null) {
                            adapterPhone.setContacts(filteredContactsAfterText);

                            if (adapterPhone.getItemCount() == 0) {

                                emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
                                emptyTextView.setText(R.string.contacts_list_empty_text);
                                recyclerView.setVisibility(View.GONE);
                                emptyImageView.setVisibility(View.VISIBLE);
                                emptyTextView.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyImageView.setVisibility(View.GONE);
                                emptyTextView.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton = (ImageView) findViewById(R.id.add_contact_send);
        if (completionView.getObjects().size() > 0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sendButton.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_BOTTOM, completionView.getId());
            params.setMargins(0, Util.scaleWidthPx(24, outMetrics), 0, 0);
        }
        sendButton.setOnClickListener(this);

        backButton = (ImageView) findViewById(R.id.add_contact_back);
        backButton.setOnClickListener(this);

        detector = new GestureDetectorCompat(this, new RecyclerViewOnGestureListener());
        recyclerView = (RecyclerView) findViewById(R.id.add_contact_list);
        recyclerView.setPadding(0, 0, 0, Util.scaleHeightPx(85, outMetrics));
        recyclerView.setClipToPadding(false);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, outMetrics));
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnItemTouchListener(this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        emptyImageView = (ImageView) findViewById(R.id.add_contact_list_empty_image);
        emptyTextView = (TextView) findViewById(R.id.add_contact_list_empty_text);

        emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
        emptyTextView.setText(R.string.contacts_list_empty_text_loading);

        progressBar = (ProgressBar) findViewById(R.id.add_contact_progress_bar);

        //Get MEGA contacts and phone contacts: first name, last name and email
        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            aB.setTitle(getString(R.string.menu_choose_contact));

            if (sendInvitationMenuItem != null){
                sendInvitationMenuItem.setVisible(false);
            }

            if(sendToInbox==0){
                if (writeMailMenuItem != null){
                    writeMailMenuItem.setVisible(true);
                }
            } else {
                if (writeMailMenuItem != null){
                    writeMailMenuItem.setVisible(false);
                }
            }



            contactsMEGA = megaApi.getContacts();
            visibleContactsMEGA.clear();

            for (int i=0;i<contactsMEGA.size();i++){
                log("contact: " + contactsMEGA.get(i).getEmail() + "_" + contactsMEGA.get(i).getVisibility());
                if (contactsMEGA.get(i).getVisibility() == MegaUser.VISIBILITY_VISIBLE){

                    MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(contactsMEGA.get(i).getHandle()+""));
                    String fullName = "";
                    if(contactDB!=null){
                        ContactController cC = new ContactController(this);
                        fullName = cC.getFullName(contactDB.getName(), contactDB.getLastName(), contactsMEGA.get(i).getEmail());
                    }
                    else{
                        //No name, ask for it and later refresh!!
                        fullName = contactsMEGA.get(i).getEmail();
                    }

                    MegaContactAdapter megaContactAdapter = new MegaContactAdapter(contactDB, contactsMEGA.get(i), fullName);
                    visibleContactsMEGA.add(megaContactAdapter);
                }
            }

            List<ContactInfo> tokens = completionView.getObjects();
            if (tokens != null) {
                log("tokens.size() = " + tokens.size());

//                if (tokens.size() == 0) {
//                    for (int i = 0; i < visibleContactsMEGA.size(); i++) {
//                        log("filteredContacts.add(visibleContacts.get(" + i + ") = " + visibleContactsMEGA.get(i).getEmail());
//                        filteredContactsMEGA.add(visibleContactsMEGA.get(i));
//                    }
//                } else {
//                    for (int i = 0; i < visibleContactsMEGA.size(); i++) {
//                        boolean found = false;
//                        for (int j = 0; j < tokens.size(); j++) {
//                            log("tokens.get(" + j + ").getEmail() = " + tokens.get(j).getEmail());
//                            log("visibleContacts.get(" + i + ").getEmail() = " + visibleContactsMEGA.get(i).getEmail());
//                            if (tokens.get(j).getEmail().compareTo(visibleContactsMEGA.get(i).getEmail()) == 0) {
//                                found = true;
//                                break;
//                            }
//                        }
//
//                        if (!found) {
//                            log("!found -> filteredContacts.add(visibleContacts.get(" + i + ") = " + visibleContactsMEGA.get(i).getEmail());
//                            filteredContactsMEGA.add(visibleContactsMEGA.get(i));
//                        }
//                    }
//                }
            }

            Collections.sort(visibleContactsMEGA, new Comparator<MegaContactAdapter>(){

                public int compare(MegaContactAdapter c1, MegaContactAdapter c2) {
                    String name1 = c1.getFullName();
                    String name2 = c2.getFullName();
                    int res = String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
                    if (res == 0) {
                        res = name1.compareTo(name2);
                    }
                    return res;
                }
            });

            if (adapterMEGA == null) {
                adapterMEGA = new MegaContactsLollipopAdapter(this, null, visibleContactsMEGA, emptyImageView, emptyTextView, recyclerView, MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
            } else {
                adapterMEGA.setContacts(visibleContactsMEGA);
                adapterMEGA.setAdapterType(MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
            }

            adapterMEGA.setPositionClicked(-1);
            recyclerView.setAdapter(adapterMEGA);

            if (adapterMEGA.getItemCount() == 0) {

                emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
                emptyTextView.setText(R.string.contacts_list_empty_text);
                recyclerView.setVisibility(View.GONE);
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
            }
        }
        else {
            aB.setTitle(getString(R.string.menu_add_contact));

            if (sendInvitationMenuItem != null){
                sendInvitationMenuItem.setVisible(false);
            }
            if(sendToInbox==0){
                if (writeMailMenuItem != null){
                    writeMailMenuItem.setVisible(true);
                }
            }else if(sendToInbox==1){
                if (writeMailMenuItem != null){
                    writeMailMenuItem.setVisible(false);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean hasReadContactsPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
                if (!hasReadContactsPermission) {
                    log("No read contacts permission");
                    ActivityCompat.requestPermissions((AddContactActivityLollipop) this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            Constants.REQUEST_READ_CONTACTS);
                } else {
                    filteredContactsPhone.clear();

                    emptyImageView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.VISIBLE);

                    progressBar.setVisibility(View.VISIBLE);
                    new PhoneContactsTask().execute();

                }
            }
            else{
                filteredContactsPhone.clear();

                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.VISIBLE);
                new PhoneContactsTask().execute();
            }
        }
    }

    @SuppressLint("InlinedApi")
    //Get the contacts explicitly added
    private ArrayList<PhoneContactInfo> getPhoneContacts() {
        log("getPhoneContacts");
        ArrayList<PhoneContactInfo> contactList = new ArrayList<PhoneContactInfo>();

        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            String SORT_ORDER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.SORT_KEY_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;
            String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''  AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1";
            Cursor c = cr.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,filter,
                    null, SORT_ORDER);

            while (c.moveToNext()){
                long id = c.getLong(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                String name = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                String emailAddress = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                log("ID: " + id + "___ NAME: " + name + "____ EMAIL: " + emailAddress);

                if ((!emailAddress.equalsIgnoreCase("")) && (emailAddress.contains("@")) && (!emailAddress.contains("s.whatsapp.net"))) {
                    log("VALID Contact: "+ name + " ---> "+ emailAddress);
                    PhoneContactInfo contactPhone = new PhoneContactInfo(id, name, emailAddress, null);
                    contactList.add(contactPhone);
                }
            }

            c.close();

            log("contactList.size() = " + contactList.size());

            return contactList;

        } catch (Exception e) { log ("Exception: " + e.getMessage()); }

        return null;
    }

    public void onTextChanged(CharSequence s, int start, int before, int count){
//        log("onTextChanged: " + s.toString() + "_ " + start + "__" + before + "__" + count);
//
////        String [] spl = s.toString().split(",,");
////        for (int i=0;i<spl.length;i++){
////            log("SS: _" + spl[i] + "_");
////        }
////
////        log("INDEXOF: " + s.toString().indexOf(",, "));
////        String text = "" + s.toString();
////        int ind = text.indexOf(",, ");
////        while(ind >= 0){
////            log("IND: " + ind);
////            if (ind >= 0){
////                text = text.substring(ind+3, text.length());
////            }
////            ind = text.indexOf(",, ");
////        }
////
////        log("text: _" + text + "_");
//
//        String text = "";
//        for (int i=0;i<s.toString().length();i++){
//            if (s.toString().charAt(i) != ','){
//                text = text + s.toString().charAt(i);
//            }
//        }
//
//        log("text: _" + text + "_");
//
//        if (contactType == Constants.CONTACT_TYPE_MEGA){
//            ArrayList<MegaUser> filteredContactsAfterText = new ArrayList<MegaUser>();
//            if (filteredContactsMEGA != null) {
//                for (int i = 0; i < filteredContactsMEGA.size(); i++) {
//                    try {
//                        String email = filteredContactsMEGA.get(i).getEmail();
//                        String emailPart = "";
//
//                        if (email != null){
//                            if (email.length() >= text.trim().length()){
//                                emailPart = email.substring(0, text.trim().length());
//                            }
//                        }
//
//                        Collator collator = Collator.getInstance(Locale.getDefault());
//                        collator.setStrength(Collator.PRIMARY);
//
//                        if (collator.compare(text.trim(), emailPart) == 0){
//                            filteredContactsAfterText.add(filteredContactsMEGA.get(i));
//                        }
//                        else {
//                            MegaContact contactDB = dbH.findContactByHandle(String.valueOf(filteredContactsMEGA.get(i).getHandle()));
//                            if (contactDB != null) {
//                                String name = contactDB.getName();
//                                String lastName = contactDB.getLastName();
//                                String namePart = "";
//                                String lastNamePart = "";
//                                String fullNamePart = "";
//
//                                if (name != null){
//                                    if (name.length() >= text.trim().length()){
//                                        namePart = name.substring(0, text.trim().length());
//                                    }
//                                }
//
//                                if (lastName != null){
//                                    if (lastName.length() >= text.trim().length()){
//                                        lastNamePart = lastName.substring(0, text.trim().length());
//                                    }
//                                }
//
//                                if ((name != null) && (lastName != null)) {
//                                    String fullName = name + " " + lastName;
//                                    if (fullName != null) {
//                                        if (fullName.trim().length() >= text.trim().length()){
//                                            fullNamePart = fullName.substring(0, text.trim().length());
//                                        }
//                                    }
//                                }
//
//                                if (collator.compare(text.trim(), namePart) == 0){
//                                    filteredContactsAfterText.add(filteredContactsMEGA.get(i));
//                                }
//                                else if (collator.compare(text.trim(), lastNamePart) == 0){
//                                    filteredContactsAfterText.add(filteredContactsMEGA.get(i));
//                                }
//                                else if (collator.compare(text.trim(), fullNamePart) == 0){
//                                    filteredContactsAfterText.add(filteredContactsMEGA.get(i));
//                                }
//                            }
//                        }
//                    }
//                    catch (Exception e) { log ("Exception: " + e.getMessage()); }
//                }
//
//                if (adapterMEGA != null) {
//                    adapterMEGA.setContacts(filteredContactsAfterText);
//                    adapterMEGA.setAdapterType(MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
//
//                    if (adapterMEGA.getItemCount() == 0) {
//
//                        emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
//                        emptyTextView.setText(R.string.contacts_list_empty_text);
//                        recyclerView.setVisibility(View.GONE);
//                        emptyImageView.setVisibility(View.VISIBLE);
//                        emptyTextView.setVisibility(View.VISIBLE);
//                    } else {
//                        recyclerView.setVisibility(View.VISIBLE);
//                        emptyImageView.setVisibility(View.GONE);
//                        emptyTextView.setVisibility(View.GONE);
//                    }
//                }
//            }
//        }
//        else if (contactType == Constants.CONTACT_TYPE_DEVICE){
//            ArrayList<PhoneContactInfo> filteredContactsAfterText = new ArrayList<PhoneContactInfo>();
//            if (filteredContactsPhone != null) {
//                for (int i = 0; i < filteredContactsPhone.size(); i++) {
//                    try {
//                        String email = filteredContactsPhone.get(i).getEmail();
//                        String name = filteredContactsPhone.get(i).getName();
//                        String phoneNumber = filteredContactsPhone.get(i).getPhoneNumber();
//                        String emailPart = "";
//                        String namePart = "";
//                        String phoneNumberPart = "";
//
//                        if (email != null){
//                            if (email.length() >= text.trim().length()){
//                                emailPart = email.substring(0, text.trim().length());
//                            }
//                        }
//
//                        if (name != null){
//                            if (name.trim().length() >= text.trim().length()){
//                                namePart = name.substring(0, text.trim().length());
//                            }
//                        }
//
//                        if (phoneNumber != null){
//                            if (phoneNumber.length() >= text.trim().length()){
//                                phoneNumberPart = phoneNumber.substring(0, text.trim().length());
//                            }
//                        }
//
//                        Collator collator = Collator.getInstance(Locale.getDefault());
//                        collator.setStrength(Collator.PRIMARY);
//
//                        if (collator.compare(text.trim(), emailPart) == 0){
//                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
//                        }
//                        else if (collator.compare(text.trim(), namePart) == 0){
//                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
//                        }
//                        else if (collator.compare(text.trim(), phoneNumberPart) == 0){
//                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
//                        }
////                        if (filteredContactsPhone.get(i).getEmail().startsWith(text.trim()) == true) {
////                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
////                        } else if (filteredContactsPhone.get(i).getName().startsWith(text.trim()) == true) {
////                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
////                        } else if (filteredContactsPhone.get(i).getPhoneNumber().startsWith(text.trim()) == true) {
////                            filteredContactsAfterText.add(filteredContactsPhone.get(i));
////                        }
//                    }
//                    catch (Exception e) { log ("Exception: " + e.getMessage()); }
//                }
//
//                if (adapterPhone != null) {
//                    adapterPhone.setContacts(filteredContactsAfterText);
//
//                    if (adapterPhone.getItemCount() == 0) {
//
//                        emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
//                        emptyTextView.setText(R.string.contacts_list_empty_text);
//                        recyclerView.setVisibility(View.GONE);
//                        emptyImageView.setVisibility(View.VISIBLE);
//                        emptyTextView.setVisibility(View.VISIBLE);
//                    } else {
//                        recyclerView.setVisibility(View.VISIBLE);
//                        emptyImageView.setVisibility(View.GONE);
//                        emptyTextView.setVisibility(View.GONE);
//                    }
//                }
//            }
//        }
    }


    public void showNewContactDialog(){
        log("showNewContactDialog");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

        final EditText input = new EditText(this);
        layout.addView(input, params);

//		input.setId(EDIT_TEXT_ID);
        input.setSingleLine();
        input.setHint(getString(R.string.email_text));
        input.setTextColor(getResources().getColor(R.color.text_secondary));
//		input.setSelectAllOnFocus(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String value = input.getText().toString().trim();
                    String emailError = Util.getEmailError(value, addContactActivityLollipop);
                    if (emailError != null) {
                        input.setError(emailError);
                        input.requestFocus();
                    } else {
                        ContactController cC = new ContactController(addContactActivityLollipop);
                        cC.inviteContact(value);
                        shareFolderDialog.dismiss();
                    }
                }
                else{
                    log("other IME" + actionId);
                }
                return false;
            }
        });
        input.setImeActionLabel(getString(R.string.general_share),EditorInfo.IME_ACTION_DONE);
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboardDelayed(v);
                }
            }
        });

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.title_write_user_email));
        builder.setPositiveButton(getString(R.string.general_share),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.setView(layout);
        shareFolderDialog = builder.create();
        shareFolderDialog.show();
        shareFolderDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = input.getText().toString().trim();
                String emailError = Util.getEmailError(value, addContactActivityLollipop);
                if (emailError != null) {
                    input.setError(emailError);
                } else {
                    setResultContact(value);
                    shareFolderDialog.dismiss();
                }
            }
        });
    }

    /*
	 * Display keyboard
	 */
    private void showKeyboardDelayed(final View view) {
        log("showKeyboardDelayed");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 50);
    }
    public void itemClick(String email, int position){

        log("itemClick");
        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            ContactInfo c = new ContactInfo();

            for (int i = 0; i < visibleContactsMEGA.size(); i++) {
                if (visibleContactsMEGA.get(i).getMegaUser().getEmail().compareTo(email) == 0) {
                    c.setEmail(visibleContactsMEGA.get(i).getMegaUser().getEmail());
                    MegaContactDB contactDB = dbH.findContactByHandle(String.valueOf(visibleContactsMEGA.get(i).getMegaUser().getHandle()));
                    if (contactDB != null) {
                        String name = contactDB.getName();
                        String lastName = contactDB.getLastName();
                        c.setName(name + " " + lastName);
                    }
                    break;
                }
            }

            if (c.getEmail().compareTo("") != 0) {
                log("completionView.getText() =  " + completionView.getText());
                itemClickPressed = true;
                completionView.addObject(c);
            }

            if (selectedContactsMEGA.get(position) == false){
                selectedContactsMEGA.put(position, true);
            }
            else{
                selectedContactsMEGA.put(position, false);
            }

            if (adapterMEGA != null){
                adapterMEGA.setSelectedContacts(selectedContactsMEGA);
            }

            if (selectedContactsMEGA.size() == 0){
                aB.setTitle(getString(R.string.menu_choose_contact));
                if (sendInvitationMenuItem != null){
                    sendInvitationMenuItem.setVisible(false);
                }
                if(sendToInbox==0){
                    if (writeMailMenuItem != null){
                        writeMailMenuItem.setVisible(true);
                    }
                } else {
                    if (writeMailMenuItem != null){
                        writeMailMenuItem.setVisible(false);
                    }
                }

            }
            else{
                int counter = 0;
                for (int i=0;i<selectedContactsMEGA.size();i++){
                    if (selectedContactsMEGA.valueAt(i) == true){
                        counter++;
                    }
                }
                if (counter == 0){
                    aB.setTitle(getString(R.string.menu_choose_contact));
                    if (sendInvitationMenuItem != null){
                        sendInvitationMenuItem.setVisible(false);
                    }

                    if(sendToInbox==0){
                        if (writeMailMenuItem != null){
                            writeMailMenuItem.setVisible(true);
                        }
                    } else {
                        if (writeMailMenuItem != null){
                            writeMailMenuItem.setVisible(false);
                        }
                    }
                }
                else{
                    aB.setTitle(getResources().getQuantityString(R.plurals.general_selection_num_contacts, counter, counter));
                    if (sendInvitationMenuItem != null){
                        sendInvitationMenuItem.setVisible(true);
                    }
                    if (writeMailMenuItem != null){
                        writeMailMenuItem.setVisible(false);
                    }
                }
            }
        }
    }

    public void itemClick(View view, int position) {
        log("on item click");

        if (contactType == Constants.CONTACT_TYPE_DEVICE){

            final PhoneContactInfo contact = adapterPhone.getDocumentAt(position);
            if(contact == null) {
                return;
            }

            ContactInfo c = new ContactInfo();

            for (int i = 0; i < filteredContactsPhone.size(); i++) {
                if (filteredContactsPhone.get(i).getEmail().compareTo(contact.getEmail()) == 0) {
                    c.setEmail(filteredContactsPhone.get(i).getEmail());
                    c.setName(contact.getName());

                    filteredContactsPhone.remove(i);
                    break;
                }
            }

            if (c.getEmail().compareTo("") != 0) {
                log("completionView.getText() =  " + completionView.getText());
                itemClickPressed = true;
                completionView.addObject(c);
            }

            if (selectedContactsPhone.get(position) == false){
                selectedContactsPhone.put(position, true);
            }
            else{
                selectedContactsPhone.put(position, false);
            }

            if (adapterPhone != null){
                adapterPhone.setSelectedContacts(selectedContactsPhone);
            }

            if (selectedContactsPhone.size() == 0){
                aB.setTitle(getString(R.string.menu_add_contact));
                if (sendInvitationMenuItem != null){
                    sendInvitationMenuItem.setVisible(false);
                }

                if(sendToInbox==0){
                    if (writeMailMenuItem != null){
                        writeMailMenuItem.setVisible(true);
                    }
                }else if(sendToInbox==1){
                    if (writeMailMenuItem != null){
                        writeMailMenuItem.setVisible(false);
                    }
                }
            }
            else{
                int counter = 0;
                for (int i=0;i<selectedContactsPhone.size();i++){
                    if (selectedContactsPhone.valueAt(i) == true){
                        counter++;
                    }
                }
                if (counter == 0){
                    aB.setTitle(getString(R.string.menu_add_contact));
                    if (sendInvitationMenuItem != null){
                        sendInvitationMenuItem.setVisible(false);
                    }
                    if(sendToInbox==0){
                        if (writeMailMenuItem != null){
                            writeMailMenuItem.setVisible(true);
                        }
                    }else if(sendToInbox==1){
                        if (writeMailMenuItem != null){
                            writeMailMenuItem.setVisible(false);
                        }
                    }

                }
                else{
                    aB.setTitle(getResources().getQuantityString(R.plurals.general_selection_num_contacts, counter, counter));
                    if (sendInvitationMenuItem != null){
                        sendInvitationMenuItem.setVisible(true);
                    }

                    if (writeMailMenuItem != null){
                        writeMailMenuItem.setVisible(false);
                    }


                }
            }
        }

//        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which){
//                    case DialogInterface.BUTTON_POSITIVE:
//                        inviteContact(contact.getEmail());
//                        break;
//
//                    case DialogInterface.BUTTON_NEGATIVE:
//                        //No button clicked
//                        break;
//                }
//            }
//        };
//
//        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
//        String message= getResources().getString(R.string.confirmation_add_contact,contact.getEmail());
//        builder.setMessage(message).setPositiveButton(R.string.contact_invite, dialogClickListener)
//                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    private void updateTokenConfirmation() {
        log("Current tokens: \n");
        for (Object token: completionView.getObjects()){
            log(token.toString() + "\n");
        }

        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            List<ContactInfo> tokens = completionView.getObjects();
//            if ((tokens != null) && (visibleContactsMEGA != null) && (filteredContactsMEGA != null)) {
            if ((tokens != null) && (visibleContactsMEGA != null)){
                log("tokens.size() = " + tokens.size());

                if (adapterMEGA != null) {
                    adapterMEGA.setContacts(visibleContactsMEGA);
                    adapterMEGA.setAdapterType(MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
                }
            }

            if (itemClickPressed) {
                for (int i = 0; i < completionView.getText().length(); i++) {
                    if ((completionView.getText().charAt(i) != ',') && (completionView.getText().charAt(i) != ' ')) {
                        log("Delete char: " + i + "__ " + completionView.getText().charAt(i));
                        completionView.getText().delete(i, i + 1);
                        i--;
                    }
                }
                itemClickPressed = false;
            }
        }
        else if (contactType == Constants.CONTACT_TYPE_DEVICE){
            List<ContactInfo> tokens = completionView.getObjects();
            if ((tokens != null) && (phoneContacts != null) && (filteredContactsPhone != null)) {
                filteredContactsPhone.clear();
                log("tokens.size() = " + tokens.size());

                if (tokens.size() == 0) {
                    for (int i = 0; i < phoneContacts.size(); i++) {
                        log("filteredContacts.add(visibleContacts.get(" + i + ") = " + phoneContacts.get(i).getEmail());
                        filteredContactsPhone.add(phoneContacts.get(i));
                    }
                } else {
                    for (int i = 0; i < phoneContacts.size(); i++) {
                        boolean found = false;
                        for (int j = 0; j < tokens.size(); j++) {
                            log("tokens.get(" + j + ").getEmail() = " + tokens.get(j).getEmail());
                            log("visibleContacts.get(" + i + ").getEmail() = " + phoneContacts.get(i).getEmail());
                            if (tokens.get(j).getEmail().compareTo(phoneContacts.get(i).getEmail()) == 0) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            log("!found -> filteredContacts.add(visibleContacts.get(" + i + ") = " + phoneContacts.get(i).getEmail());
                            filteredContactsPhone.add(phoneContacts.get(i));
                        }
                    }
                }

                if (adapterPhone != null) {
                    adapterPhone.setContacts(phoneContacts);
                }
            }

            if (itemClickPressed) {
                for (int i = 0; i < completionView.getText().length(); i++) {
                    if ((completionView.getText().charAt(i) != ',') && (completionView.getText().charAt(i) != ' ')) {
                        log("Delete char: " + i + "__ " + completionView.getText().charAt(i));
                        completionView.getText().delete(i, i + 1);
                        i--;
                    }
                }
                itemClickPressed = false;
            }
        }
    }


    @Override
    public void onTokenAdded(ContactInfo token) {
        log("Added: " + token);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sendButton.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_BOTTOM, completionView.getId());
        params.setMargins(0, Util.scaleWidthPx(24, outMetrics), 0, 0);
        updateTokenConfirmation();
    }

    @Override
    public void onTokenRemoved(ContactInfo token) {
        log("Removed: " + token);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sendButton.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        updateTokenConfirmation();
    }

    @Override
    public void onClick(View v) {
        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        switch (v.getId()){
            case R.id.add_contact_back:{
                onBackPressed();
                break;
            }
            case R.id.add_contact_send:{
//                int l = completionView.getText().length();
//                completionView.getText().delete(l-1, l);

                List<ContactInfo> tokens = completionView.getObjects();
                if (contactType == Constants.CONTACT_TYPE_DEVICE){
                    inviteContacts(selectedContactsPhone);
                }
                else if (contactType == Constants.CONTACT_TYPE_MEGA){
                    setResultContacts(selectedContactsMEGA, true);
                }
                break;
            }
        }
    }

    private void setResultContacts(SparseBooleanArray selectedContacts, boolean megaContacts){
        log("setResultContacts");
        ArrayList<String> contactsSelected = new ArrayList<String>();

        if (selectedContacts != null){
            for (int i=0;i<selectedContacts.size();i++) {
                if (selectedContacts.valueAt(i) == true) {
                    int key = selectedContacts.keyAt(i);
                    if (adapterMEGA != null){
                        MegaUser contact = adapterMEGA.getDocumentAt(key);
                        String contactEmail = contact.getEmail();
                        if (contactEmail != null){
                            contactsSelected.add(contactEmail);
                        }
                    }
                }
            }
        }

//        if (tokens != null) {
//            for (int i = 0; i < tokens.size(); i++) {
//                String contactEmail = tokens.get(i).getEmail();
//                if (contactEmail != null){
//                    contactsSelected.add(contactEmail);
//                }
//            }
//        }

        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_CONTACTS, contactsSelected);

        if(multipleSelectIntent==0){
            intent.putExtra(EXTRA_NODE_HANDLE, nodeHandle);
            intent.putExtra("MULTISELECT", 0);
        }
        else if(multipleSelectIntent==1){
            intent.putExtra(EXTRA_NODE_HANDLE, nodeHandles);
            intent.putExtra("MULTISELECT", 1);
        }

        if(sendToInbox==0){
            intent.putExtra("SEND_FILE",0);
        } else {
            intent.putExtra("SEND_FILE",1);
        }
        intent.putExtra(EXTRA_MEGA_CONTACTS, megaContacts);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setResultContact(String email){
        log("setResultContact");
        ArrayList<String> contactsSelected = new ArrayList<String>();

        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_CONTACTS, contactsSelected);

        contactsSelected.add(email);
        log("user email: "+contactsSelected.get(0));

        if(multipleSelectIntent==0){
            log("multiselectIntent == 0");
            intent.putExtra(EXTRA_NODE_HANDLE, nodeHandle);
            intent.putExtra("MULTISELECT", 0);
        }
        else if(multipleSelectIntent==1){
            log("multiselectIntent == 1");
            if(nodeHandles!=null){
                log("number of items selected: "+nodeHandles.length);
            }
            intent.putExtra(EXTRA_NODE_HANDLE, nodeHandles);
            intent.putExtra("MULTISELECT", 1);
        }

        if(sendToInbox==0){
            intent.putExtra("SEND_FILE",0);
            if (writeMailMenuItem != null){
                writeMailMenuItem.setVisible(true);
            }
        }
        else
        {
            intent.putExtra("SEND_FILE",1);
            if (writeMailMenuItem != null){
                writeMailMenuItem.setVisible(false);
            }
        }
        intent.putExtra(EXTRA_MEGA_CONTACTS, false);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void inviteContacts(SparseBooleanArray selectedContacts){
        ArrayList<String> contactsSelected = new ArrayList<String>();

        if (selectedContacts != null) {
            for (int i=0;i<selectedContacts.size();i++) {
                if (selectedContacts.valueAt(i) == true) {
                    int key = selectedContacts.keyAt(i);
                    if (adapterPhone != null){
                        PhoneContactInfo contact = adapterPhone.getDocumentAt(key);
                        String contactEmail = contact.getEmail();
                        if (contactEmail != null){
                            contactsSelected.add(contactEmail);
                        }
                    }
                }
            }
        }

        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_CONTACTS, contactsSelected);
        for(int i=0; i<contactsSelected.size();i++){
            log("setResultContacts: "+contactsSelected.get(i));
        }

        intent.putExtra(EXTRA_MEGA_CONTACTS, false);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        log("onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_READ_CONTACTS: {
                log("REQUEST_READ_CONTACTS");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean hasReadContactsPermissions = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
                    if (hasReadContactsPermissions) {
                        filteredContactsPhone.clear();
                        emptyImageView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.VISIBLE);

                        progressBar.setVisibility(View.VISIBLE);
                        new PhoneContactsTask().execute();
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public static void log(String message) {
        Util.log("AddContactActivityLollipop", message);
    }
}
