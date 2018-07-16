package mega.privacy.android.app.lollipop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactAdapter;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.components.scrollBar.FastScroller;
import mega.privacy.android.app.lollipop.adapters.AddContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaAddContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.PhoneContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.ShareContactsAdapter;
import mega.privacy.android.app.lollipop.adapters.ShareContactsHeaderAdapter;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaUser;


public class AddContactActivityLollipop extends PinActivityLollipop implements View.OnClickListener, RecyclerView.OnItemTouchListener{

    DisplayMetrics outMetrics;
    MegaApplication app;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;
    DatabaseHandler dbH = null;
    int contactType = 0;
    int multipleSelectIntent;
    long nodeHandle = -1;
    long[] nodeHandles;
    Handler handler;
    long chatId = -1;

    AddContactActivityLollipop addContactActivityLollipop;

    Toolbar tB;
    ActionBar aB;

    RelativeLayout containerContacts;
    RecyclerView recyclerViewList;
    LinearLayoutManager linearLayoutManager;
    ImageView emptyImageView;
    TextView emptyTextView;
    ProgressBar progressBar;
    private RelativeLayout contactErrorLayout;
    private RelativeLayout notPermitedAddContacts;
    RecyclerView addedContactsRecyclerView;
    RelativeLayout containerAddedContactsRecyclerView;
    LinearLayout separator;
    LinearLayoutManager mLayoutManager;
    String inputString  = "";
    String savedInputString = "";

//    Adapter list MEGA contacts
    MegaContactsLollipopAdapter adapterMEGA;
//    Adapter list chips MEGA contacts
    MegaAddContactsLollipopAdapter adapterMEGAContacts;

    ArrayList<MegaUser> contactsMEGA;
    ArrayList<MegaContactAdapter> visibleContactsMEGA = new ArrayList<>();
    ArrayList<MegaContactAdapter> filteredContactMEGA = new ArrayList<>();
    ArrayList<MegaContactAdapter> addedContactsMEGA = new ArrayList<>();
    ArrayList<MegaContactAdapter> queryContactMEGA = new ArrayList<>();


//    Adapter list Phone contacts
    PhoneContactsLollipopAdapter adapterPhone;
//    Adapter list chips Phone contacts
    AddContactsLollipopAdapter adapterContacts;

    ArrayList<PhoneContactInfo> phoneContacts = new ArrayList<>();
    ArrayList<PhoneContactInfo> addedContactsPhone = new ArrayList<>();
    ArrayList<PhoneContactInfo> filteredContactsPhone = new ArrayList<>();
    ArrayList<PhoneContactInfo> queryContactsPhone = new ArrayList<>();

//    Adapter list Share contacts
    ShareContactsHeaderAdapter adapterShareHeader;
//    Adapter list chips MEGA/Phone contacts
    ShareContactsAdapter adapterShare;

    ArrayList<ShareContactInfo> addedContactsShare = new ArrayList<>();
    ArrayList<ShareContactInfo> shareContacts = new ArrayList<>();
    ArrayList<ShareContactInfo> filteredContactsShare = new ArrayList<>();
    ArrayList<ShareContactInfo> queryContactsShare = new ArrayList<>();

    RelativeLayout relativeLayout;

    ArrayList<String> savedaddedContacts = new ArrayList<>();

    public static String EXTRA_MEGA_CONTACTS = "mega_contacts";
    public static String EXTRA_CONTACTS = "extra_contacts";
    public static String EXTRA_NODE_HANDLE = "node_handle";

    private MenuItem sendInvitationMenuItem;

    private boolean comesFromChat;

    private RelativeLayout headerContacts;
    private TextView textHeader;

    private boolean fromAchievements = false;
    private ArrayList<String> mailsFromAchievements;

    private MenuItem searchMenuItem;
    private SearchView.SearchAutoComplete searchAutoComplete;
    private boolean searchExpand = false;

    private FilterContactsTask filterContactsTask;
    private GetContactsTask getContactsTask;
    private RecoverContactsTask recoverContactsTask;

    private FastScroller fastScroller;

    private FloatingActionButton fabImageGroup;
    private EditText nameGroup;
    private boolean onNewGroup = false;

    private class GetContactsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            if (contactType == Constants.CONTACT_TYPE_MEGA) {
                getVisibleMEGAContacts();
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE) {
                phoneContacts.clear();
                filteredContactsPhone.clear();
                phoneContacts = getPhoneContacts();
                addedContactsPhone.clear();
                for (int i = 0; i < phoneContacts.size(); i++) {
                    filteredContactsPhone.add(phoneContacts.get(i));
                }
                getVisibleMEGAContacts();

                boolean found;
                PhoneContactInfo contactPhone;
                MegaContactAdapter contactMEGA;

                if (filteredContactsPhone != null && !filteredContactsPhone.isEmpty()) {
                    for (int i=0; i<filteredContactsPhone.size(); i++) {
                        found = false;
                        contactPhone = filteredContactsPhone.get(i);
                        for (int j=0; j<visibleContactsMEGA.size(); j++) {
                            contactMEGA = visibleContactsMEGA.get(j);
                            if (contactPhone.getEmail().equals(getMegaContactMail(contactMEGA))){
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            filteredContactsPhone.remove(contactPhone);
                            i--;
                        }
                    }
                }
            }
            else {
                phoneContacts.clear();
                filteredContactsPhone.clear();
                addedContactsPhone.clear();
                phoneContacts = getPhoneContacts();
                for (int i = 0; i < phoneContacts.size(); i++) {
                    filteredContactsPhone.add(phoneContacts.get(i));
                }
                getVisibleMEGAContacts();

                MegaContactAdapter contactMEGA;
                PhoneContactInfo contactPhone;
                ShareContactInfo contact;
                boolean found;
                shareContacts.clear();
                if (filteredContactMEGA != null && !filteredContactMEGA.isEmpty()) {
                    shareContacts.add(new ShareContactInfo(true, true, false));
                    for (int i = 0; i<filteredContactMEGA.size(); i++) {
                        contactMEGA = filteredContactMEGA.get(i);
                        contact = new ShareContactInfo(null, contactMEGA, null);
                        shareContacts.add(contact);
                    }
                    shareContacts.get(shareContacts.size()-1).setLastItem(true);
                }
                if (filteredContactsPhone != null && !filteredContactsPhone.isEmpty()) {
                    shareContacts.add(new ShareContactInfo(true, false, true));
                    for (int i=0; i<filteredContactsPhone.size(); i++) {
                        found = false;
                        contactPhone = filteredContactsPhone.get(i);
                        for (int j=0; j<filteredContactMEGA.size(); j++) {
                            contactMEGA = filteredContactMEGA.get(j);
                            if (contactPhone.getEmail().equals(getMegaContactMail(contactMEGA))){
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            shareContacts.add(new ShareContactInfo(contactPhone, null, null));
                        }
                        else {
                            filteredContactsPhone.remove(contactPhone);
                            i--;
                        }
                    }
                }
                filteredContactsShare.clear();
                for (int i=0; i<shareContacts.size(); i++) {
                    filteredContactsShare.add(shareContacts.get(i));
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            progressBar.setVisibility(View.GONE);

            if (contactType == Constants.CONTACT_TYPE_MEGA) {
                setMegaAdapterContacts(filteredContactMEGA, MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE){
                setPhoneAdapterContacts(filteredContactsPhone);
            }
            else {
                setShareAdapterContacts(filteredContactsShare);
            }
            setTitleAB();
            setRecyclersVisibility();
            setSeparatorVisibility();
            setSendInvitationVisibility();
            visibilityFastScroller();
        }
    }

    private class FilterContactsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            inputString = searchAutoComplete.getText().toString();
            if (inputString != null && !inputString.equals("")){
                MegaContactAdapter contactMega;
                PhoneContactInfo contactPhone;
                ShareContactInfo contactShare;

                if (contactType == Constants.CONTACT_TYPE_MEGA) {
                    queryContactMEGA.clear();
                    for (int i=0; i<filteredContactMEGA.size(); i++){
                        contactMega = filteredContactMEGA.get(i);
                        if (getMegaContactMail(contactMega).toLowerCase().contains(inputString.toLowerCase())
                                || contactMega.getFullName().toLowerCase().contains(inputString.toLowerCase())) {
                            queryContactMEGA.add(contactMega);
                        }
                    }
                }
                else if (contactType == Constants.CONTACT_TYPE_DEVICE) {
                    queryContactsPhone.clear();
                    for (int i = 0; i<filteredContactsPhone.size(); i++) {
                        contactPhone = filteredContactsPhone.get(i);
                        if (contactPhone.getEmail().toLowerCase().contains(inputString.toLowerCase())
                                || contactPhone.getName().toLowerCase().contains(inputString.toLowerCase())) {
                            queryContactsPhone.add(contactPhone);
                        }
                    }
                }
                else {
                    queryContactsShare.clear();
                    int numMega = 0;
                    int numPhone = 0;
                    for (int i = 0; i<filteredContactsShare.size(); i++) {
                        contactShare = filteredContactsShare.get(i);
                        if (contactShare.isHeader()){
                            queryContactsShare.add(contactShare);
                        }
                        else {
                            if (contactShare.isMegaContact()){
                                if (getMegaContactMail(contactShare.getMegaContactAdapter()).toLowerCase().contains(inputString.toLowerCase())
                                        || contactShare.getMegaContactAdapter().getFullName().toLowerCase().contains(inputString.toLowerCase())) {
                                    queryContactsShare.add(contactShare);
                                    numMega++;
                                }
                            }
                            else {
                                if (contactShare.getPhoneContactInfo().getEmail().toLowerCase().contains(inputString.toLowerCase())
                                        || contactShare.getPhoneContactInfo().getName().toLowerCase().contains(inputString.toLowerCase())) {
                                    queryContactsShare.add(contactShare);
                                    numPhone++;
                                }
                            }
                        }
                    }
                    if (numMega == 0) {
                        queryContactsShare.remove(0);
                    }
                    else {
                        queryContactsShare.get(numMega).setLastItem(true);
                    }
                    if (numPhone == 0) {
                        queryContactsShare.remove(queryContactsShare.size()-1);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            if (contactType == Constants.CONTACT_TYPE_MEGA) {
                if (inputString != null && !inputString.equals("")) {
                    setMegaAdapterContacts(queryContactMEGA, MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
                }
                else {
                    setMegaAdapterContacts(filteredContactMEGA, MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
                }
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE) {
                if (inputString != null && !inputString.equals("")) {
                    setPhoneAdapterContacts(queryContactsPhone);
                }
                else {
                    setPhoneAdapterContacts(filteredContactsPhone);
                }
            }
            else {
                if (inputString != null && !inputString.equals("")) {
                    setShareAdapterContacts(queryContactsShare);
                }
                else {
                    setShareAdapterContacts(filteredContactsShare);
                }
            }
            visibilityFastScroller();
        }
    }

    private class RecoverContactsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (contactType == Constants.CONTACT_TYPE_MEGA) {
                getVisibleMEGAContacts();
                String contactToAddMail = null;
                MegaContactAdapter contactToAdd, contact;
                for (int i=0; i<savedaddedContacts.size(); i++){
                    String mail = savedaddedContacts.get(i);
                    for (int j=0;j<filteredContactMEGA.size(); j++){
                        contact = filteredContactMEGA.get(j);
                        contactToAddMail = getMegaContactMail(contact);
                        if (contactToAddMail != null && contactToAddMail.equals(mail)){
                            if (!addedContactsMEGA.contains(contact)) {
                                addedContactsMEGA.add(contact);
                                filteredContactMEGA.remove(contact);
                            }
                            break;
                        }
                    }
                    if (contactToAddMail != null && !contactToAddMail.equals(mail)){
                        contactToAdd = new MegaContactAdapter(null, null, mail);
                        if (!addedContactsMEGA.contains(contactToAdd)) {
                            addedContactsMEGA.add(contactToAdd);
                        }
                    }
                }
            }
            else {
                phoneContacts.clear();
                filteredContactsPhone.clear();
                phoneContacts = getPhoneContacts();
                for (int i = 0; i < phoneContacts.size(); i++) {
                    filteredContactsPhone.add(phoneContacts.get(i));
                }
                getVisibleMEGAContacts();

                MegaContactAdapter contactMEGA;
                PhoneContactInfo contactPhone;
                ShareContactInfo contact = null;
                boolean found;
                shareContacts.clear();

                if (filteredContactMEGA != null && !filteredContactMEGA.isEmpty()) {
                    shareContacts.add(new ShareContactInfo(true, true, false));
                    for (int i = 0; i<filteredContactMEGA.size(); i++) {
                        contactMEGA = filteredContactMEGA.get(i);
                        contact = new ShareContactInfo(null, contactMEGA, null);
                        shareContacts.add(contact);
                    }
                    shareContacts.get(shareContacts.size()-1).setLastItem(true);
                }
                if (filteredContactsPhone != null && !filteredContactsPhone.isEmpty()) {
                    shareContacts.add(new ShareContactInfo(true, false, true));
                    for (int i=0; i<filteredContactsPhone.size(); i++) {
                        found = false;
                        contactPhone = filteredContactsPhone.get(i);
                        for (int j=0; j<filteredContactMEGA.size(); j++) {
                            contactMEGA = filteredContactMEGA.get(j);
                            if (contactPhone.getEmail().equals(getMegaContactMail(contactMEGA))){
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            shareContacts.add(new ShareContactInfo(contactPhone, null, null));
                        }
                        else {
                            filteredContactsPhone.remove(contactPhone);
                            i--;
                        }
                    }
                }
                filteredContactsShare.clear();
                for (int i=0; i<shareContacts.size(); i++) {
                    filteredContactsShare.add(shareContacts.get(i));
                }
                addedContactsShare.clear();
                String contactToAddMail = null;

                for (int i=0; i<savedaddedContacts.size(); i++){
                    String mail = savedaddedContacts.get(i);
                    log("mail["+i+"]: "+mail);
                    for (int j = 0; j< filteredContactsShare.size(); j++){
                        contact = filteredContactsShare.get(j);
                        if (contact.isMegaContact() && !contact.isHeader()){
                            contactToAddMail = getMegaContactMail(contact.getMegaContactAdapter());
                        }
                        else if (!contact.isHeader()){
                            contactToAddMail = contact.getPhoneContactInfo().getEmail();
                        }
                        else {
                            contactToAddMail = null;
                        }
                        if (contactToAddMail != null && contactToAddMail.equals(mail)){
                            if (!addedContactsShare.contains(contact)) {
                                addedContactsShare.add(contact);
                                filteredContactsShare.remove(contact);
                                if (contact.isMegaContact()){
                                    filteredContactMEGA.remove(contact.getMegaContactAdapter());
                                }
                                else {
                                    filteredContactsPhone.remove(contact.getPhoneContactInfo());
                                }
                            }
                            break;
                        }
                    }
                    if (contactToAddMail != null && !contactToAddMail.equals(mail)){
                        contact = new ShareContactInfo(null, null, mail);
                        if (!addedContactsShare.contains(contact)) {
                            addedContactsShare.add(contact);
                        }
                    }
                }

                if (filteredContactMEGA.size() == 0) {
                    filteredContactsShare.remove(0);
                }
                else {
                    filteredContactsShare.get(filteredContactMEGA.size()).setLastItem(true);
                }
                if (filteredContactsPhone.size() == 0) {
                    filteredContactsShare.remove(filteredContactsShare.size()-1);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setAddedAdapterContacts();
            if (contactType == Constants.CONTACT_TYPE_MEGA) {
                if (onNewGroup) {
                    newGroup();
                }
                else {
                    setMegaAdapterContacts(filteredContactMEGA, MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
                }
            }
            else {
                setShareAdapterContacts(filteredContactsShare);
            }

            setTitleAB();
            setRecyclersVisibility();
            setSeparatorVisibility();
            visibilityFastScroller();

            if (searchExpand) {
                if (filterContactsTask != null && filterContactsTask.getStatus() == AsyncTask.Status.RUNNING) {
                    filterContactsTask.cancel(true);
                }
                filterContactsTask = new FilterContactsTask();
                filterContactsTask.execute();
            }
        }
    }

    void setAddedAdapterContacts () {
        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            if (adapterMEGAContacts == null){
                adapterMEGAContacts = new MegaAddContactsLollipopAdapter(addContactActivityLollipop, addedContactsMEGA);
            }
            else {
                adapterMEGAContacts.setContacts(addedContactsMEGA);
            }

            if (addedContactsMEGA.size() == 0){
                containerAddedContactsRecyclerView.setVisibility(View.GONE);
            }
            else {
                containerAddedContactsRecyclerView.setVisibility(View.VISIBLE);
            }

            addedContactsRecyclerView.setAdapter(adapterMEGAContacts);
        }
        else if (contactType == Constants.CONTACT_TYPE_DEVICE) {
            if (adapterContacts == null){
                adapterContacts = new AddContactsLollipopAdapter(this, addedContactsPhone);
            }
            else {
                adapterContacts.setContacts(addedContactsPhone);
            }

            if (addedContactsPhone.size() == 0){
                containerAddedContactsRecyclerView.setVisibility(View.GONE);
            }
            else {
                containerAddedContactsRecyclerView.setVisibility(View.VISIBLE);
            }

            addedContactsRecyclerView.setAdapter(adapterContacts);
        }
        else {
            if (adapterShare == null) {
                adapterShare = new ShareContactsAdapter(addContactActivityLollipop, addedContactsShare);
            }
            else {
                adapterShare.setContacts(addedContactsShare);
            }

            if (addedContactsShare.size() == 0){
                containerAddedContactsRecyclerView.setVisibility(View.GONE);
            }
            else {
                containerAddedContactsRecyclerView.setVisibility(View.VISIBLE);
            }

            addedContactsRecyclerView.setAdapter(adapterShare);
        }

        setSendInvitationVisibility();
    }

    void setPhoneAdapterContacts (ArrayList<PhoneContactInfo> contacts) {
        if(filteredContactsPhone!=null){
            if (filteredContactsPhone.size() == 0){
                headerContacts.setVisibility(View.GONE);
                String textToShow = String.format(getString(R.string.context_empty_contacts), getString(R.string.section_contacts));
                try{
                    textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                    textToShow = textToShow.replace("[/A]", "</font>");
                    textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                    textToShow = textToShow.replace("[/B]", "</font>");
                }
                catch (Exception e){}
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }
                emptyTextView.setText(result);
            }
            else {
                emptyTextView.setText(R.string.contacts_list_empty_text_loading);
            }
        }
        else{
            log("PhoneContactsTask: Phone contacts null");
            boolean hasReadContactsPermission = (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
            if (!hasReadContactsPermission) {
                log("PhoneContactsTask: No read contacts permission");
            }
        }

        if (adapterPhone == null){
            adapterPhone = new PhoneContactsLollipopAdapter(addContactActivityLollipop, contacts);

            recyclerViewList.setAdapter(adapterPhone);

            adapterPhone.SetOnItemClickListener(new PhoneContactsLollipopAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    itemClick(view, position);
                }
            });
        }
        else{
            adapterPhone.setContacts(contacts);
        }

        if(adapterPhone!=null){
            if (adapterPhone.getItemCount() == 0){
                headerContacts.setVisibility(View.GONE);
                recyclerViewList.setVisibility(View.GONE);
                if (contactType == Constants.CONTACT_TYPE_BOTH) {
                    if (adapterMEGA != null) {
                        if (adapterMEGA.getItemCount() == 0) {
                            emptyImageView.setVisibility(View.VISIBLE);
                            emptyTextView.setVisibility(View.VISIBLE);
                        }
                        else {
                            emptyImageView.setVisibility(View.GONE);
                            emptyTextView.setVisibility(View.GONE);
                        }
                    }
                }
                else {
                    emptyImageView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            }
            else{
                headerContacts.setVisibility(View.VISIBLE);
                recyclerViewList.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
            }
        }
    }

    void setMegaAdapterContacts (ArrayList<MegaContactAdapter> contacts, int adapter) {
        if (adapterMEGA == null) {
            adapterMEGA = new MegaContactsLollipopAdapter(addContactActivityLollipop, null, contacts, recyclerViewList, adapter);
        } else {
            adapterMEGA.setAdapterType(adapter);
            adapterMEGA.setContacts(contacts);
        }

        adapterMEGA.setPositionClicked(-1);
        recyclerViewList.setAdapter(adapterMEGA);

        if (adapterMEGA.getItemCount() == 0) {

            emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
            String textToShow = String.format(getString(R.string.context_empty_contacts), getString(R.string.section_contacts));
            try {
                textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                textToShow = textToShow.replace("[/B]", "</font>");
            } catch (Exception e) {
            }
            Spanned result = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(textToShow, Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(textToShow);
            }
            emptyTextView.setText(result);
            headerContacts.setVisibility(View.GONE);
            recyclerViewList.setVisibility(View.GONE);
            emptyImageView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            headerContacts.setVisibility(View.VISIBLE);
            recyclerViewList.setVisibility(View.VISIBLE);
            emptyImageView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    void setShareAdapterContacts (ArrayList<ShareContactInfo> contacts) {
        if (adapterShareHeader == null){
            adapterShareHeader = new ShareContactsHeaderAdapter(addContactActivityLollipop, contacts);
            recyclerViewList.setAdapter(adapterShareHeader);
            adapterShareHeader.SetOnItemClickListener(new ShareContactsHeaderAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    itemClick(view, position);
                }
            });
        }
        else{
            adapterShareHeader.setContacts(contacts);
        }

        if (adapterShareHeader.getItemCount() == 0){
            emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
            String textToShow = String.format(getString(R.string.context_empty_contacts), getString(R.string.section_contacts));
            try{
                textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                textToShow = textToShow.replace("[/B]", "</font>");
            }
            catch (Exception e){}
            Spanned result = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(textToShow);
            }
            emptyTextView.setText(result);
        }
        else {
            emptyImageView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            log("isValid");
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public void setSendInvitationVisibility() {
        if (sendInvitationMenuItem != null) {
            if (contactType == Constants.CONTACT_TYPE_MEGA && adapterMEGAContacts != null) {
                if (adapterMEGAContacts.getItemCount() > 0 && !searchExpand){
                    sendInvitationMenuItem.setVisible(true);
                }
                else {
                    sendInvitationMenuItem.setVisible(false);
                }
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE && adapterContacts != null) {
                if (adapterContacts.getItemCount() > 0  && !searchExpand){
                    sendInvitationMenuItem.setVisible(true);
                }
                else {
                    sendInvitationMenuItem.setVisible(false);
                }
            }
            else if (adapterShare != null){
                if (adapterShare.getItemCount() > 0  && !searchExpand){
                    sendInvitationMenuItem.setVisible(true);
                }
                else {
                    sendInvitationMenuItem.setVisible(false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_contact, menu);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setIconifiedByDefault(true);

        searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextColor(ContextCompat.getColor(this, R.color.mail_my_account));
        searchAutoComplete.setHintTextColor(ContextCompat.getColor(this, R.color.mail_my_account));
        searchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        ImageView closeIcon = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        closeIcon.setImageDrawable(Util.mutateIcon(this, R.drawable.ic_close_white, R.color.add_contact_icons));

        searchAutoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                refreshKeyboard();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String s = v.getText().toString();
                    log("s: "+s);
                    if (s.isEmpty() || s.equals("null") || s.equals("")) {
                        hideKeyboard();
                    }
                    else {
                        if (contactType == Constants.CONTACT_TYPE_MEGA) {
                            if (!comesFromChat) {
                                boolean isValid = isValidEmail(s.trim());
                                if (isValid) {
                                    MegaContactAdapter contact = new MegaContactAdapter(null, null, s.trim());
                                    addContactMEGA(contact);
                                    searchAutoComplete.getText().clear();
                                }
                                else {
                                    setError();
                                }
                            }
                            else {
                                setError();
                            }
                        }
                        else if (contactType == Constants.CONTACT_TYPE_DEVICE) {
                            boolean isValid = isValidEmail(s.trim());
                            if (isValid) {
                                PhoneContactInfo contact = new PhoneContactInfo(0, null, s.trim(), null);
                                addContact(contact);
                                searchAutoComplete.getText().clear();
                            }
                            else {
                                setError();
                            }
                        }
                        else {
                            boolean isValid = isValidEmail(s.trim());
                            if (isValid) {
                                ShareContactInfo contact = new ShareContactInfo(null, null, s.trim());
                                addShareContact(contact);
                                searchAutoComplete.getText().clear();
                            }
                            else {
                                setError();
                            }
                        }
                        if (filterContactsTask != null && filterContactsTask.getStatus() == AsyncTask.Status.RUNNING){
                            filterContactsTask.cancel(true);
                        }
                        filterContactsTask = new FilterContactsTask();
                        filterContactsTask.execute();
                    }
                    return true;
                }
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEND)) {
                    if (contactType == Constants.CONTACT_TYPE_DEVICE){
                        if (addedContactsPhone.isEmpty() || addedContactsPhone == null) {
                            hideKeyboard();
                        }
                        else {
                            inviteContacts(addedContactsPhone);
                        }
                    }
                    else if (contactType == Constants.CONTACT_TYPE_MEGA){
                        if (addedContactsMEGA.isEmpty() || addedContactsMEGA == null) {
                            hideKeyboard();
                        }
                        else {
                            setResultContacts(addedContactsMEGA, true);
                        }
                    }
                    else {
                        if (addedContactsShare.isEmpty() || addedContactsShare == null) {
                            hideKeyboard();
                        }
                        else {
                            shareWith(addedContactsShare);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                quitError();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                log("onTextChanged: " + s.toString() + "_ " + start + "__" + before + "__" + count);
                if (contactType == Constants.CONTACT_TYPE_MEGA){
                    if (s != null){
                        if (s.length() > 0) {
                            String temp = s.toString();
                            char last = s.charAt(s.length()-1);
                            if(last == ' '){
                                if (!comesFromChat){
                                    boolean isValid = isValidEmail(temp.trim());
                                    if(isValid){
                                        MegaContactAdapter contact = new MegaContactAdapter(null, null, temp.trim());
                                        addContactMEGA(contact);
                                        searchAutoComplete.getText().clear();
                                    }
                                    else{
                                        setError();
                                    }
                                }
                                else {
                                    setError();
                                }
                            }
                            else{
                                log("Last character is: "+last);
                            }
                        }
                    }
                }
                else if (contactType == Constants.CONTACT_TYPE_DEVICE){
                    if (s != null) {
                        if (s.length() > 0) {
                            String temp = s.toString();
                            char last = s.charAt(s.length()-1);
                            if(last == ' '){
                                boolean isValid = isValidEmail(temp.trim());
                                if(isValid){
                                    PhoneContactInfo contact = new PhoneContactInfo(0, null, temp.trim(), null);
                                    addContact(contact);
                                    searchAutoComplete.getText().clear();
                                }
                                else{
                                    setError();
                                }
                            }
                            else{
                                log("Last character is: "+last);
                            }
                        }
                    }
                }
                else {
                    if (s != null) {
                        if (s.length() > 0) {
                            String temp = s.toString();
                            char last = s.charAt(s.length()-1);
                            if (last == ' '){
                                boolean isValid = isValidEmail(temp.trim());
                                if (isValid){
                                    ShareContactInfo contact = new ShareContactInfo(null, null, temp.trim());
                                    addShareContact(contact);
                                    searchAutoComplete.getText().clear();
                                }
                                else {
                                    setError();
                                }
                            }
                            else{
                                log("Last character is: "+last);
                            }
                        }
                    }
                }
                if (filterContactsTask != null && filterContactsTask.getStatus() == AsyncTask.Status.RUNNING){
                    filterContactsTask.cancel(true);
                }
                filterContactsTask = new FilterContactsTask();
                filterContactsTask.execute();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                refreshKeyboard();
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                log("onMenuItemActionExpand");
                searchExpand = true;
                setSendInvitationVisibility();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                log("onMenuItemActionCollapse");
                searchExpand = false;
                setSendInvitationVisibility();
                setTitleAB();
                if (filterContactsTask != null && filterContactsTask.getStatus() == AsyncTask.Status.RUNNING){
                    filterContactsTask.cancel(true);
                }
                return true;
            }
        });
        searchView.setIconifiedByDefault(true);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

        });

        sendInvitationMenuItem = menu.findItem(R.id.action_send_invitation);
        sendInvitationMenuItem.setIcon(Util.mutateIcon(this, R.drawable.ic_send_white, R.color.accentColor));
        setSendInvitationVisibility();


        if (searchExpand && searchMenuItem != null) {
            searchMenuItem.expandActionView();
            if (searchView != null) {
                log("searchView != null inputString: "+savedInputString);
                searchView.setQuery(savedInputString, false);
                if (recoverContactsTask != null && recoverContactsTask.getStatus() == AsyncTask.Status.FINISHED) {
                    filterContactsTask = new FilterContactsTask();
                    filterContactsTask.execute();
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        log("onPrepareOptionsMenu");

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
                if (contactType == Constants.CONTACT_TYPE_DEVICE){
                    inviteContacts(addedContactsPhone);
                }
                else if (contactType == Constants.CONTACT_TYPE_MEGA){
                    setResultContacts(addedContactsMEGA, true);
                }
                else {
                    shareWith(addedContactsShare);
                }
                hideKeyboard();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideKeyboard () {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void refreshKeyboard() {

        String s = inputString;
        if (s != null) {
            if (s.length() == 0 && (!addedContactsMEGA.isEmpty() || !addedContactsPhone.isEmpty() || !addedContactsShare.isEmpty())){
                searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_SEND);
            }
            else {
                searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }
        }
        else if (!addedContactsMEGA.isEmpty() || !addedContactsPhone.isEmpty() || !addedContactsShare.isEmpty()) {
            searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_SEND);
        }
        else {
            searchAutoComplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            inputMethodManager.restartInput(view);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("fromAchievements", fromAchievements);
        outState.putStringArrayList("mailsFromAchievements", mailsFromAchievements);
        outState.putBoolean("searchExpand", searchExpand);
        outState.putString("inputString", searchAutoComplete.getText().toString());
        outState.putBoolean("onNewGroup", onNewGroup);

        saveContactsAdded(outState);
    }

    void saveContactsAdded (Bundle outState) {

        boolean finished = true;

        if (getContactsTask != null && getContactsTask.getStatus() == AsyncTask.Status.RUNNING){
            getContactsTask.cancel(true);
            finished = false;
            if (contactType == Constants.CONTACT_TYPE_DEVICE) {
                outState.putParcelableArrayList("addedContactsPhone", null);
                outState.putParcelableArrayList("filteredContactsPhone", null);
                outState.putParcelableArrayList("phoneContacts", null);
            }
            else {
                outState.putStringArrayList("savedaddedContacts", null);
            }
        }
        else if (filterContactsTask != null && filterContactsTask.getStatus() == AsyncTask.Status.RUNNING){
            filterContactsTask.cancel(true);
            finished = true;
        }
        else if (recoverContactsTask != null && recoverContactsTask.getStatus() == AsyncTask.Status.RUNNING) {
            recoverContactsTask.cancel(true);
            finished = false;
            if (contactType == Constants.CONTACT_TYPE_DEVICE) {
                outState.putParcelableArrayList("addedContactsPhone", addedContactsPhone);
                outState.putParcelableArrayList("filteredContactsPhone", filteredContactsPhone);
                outState.putParcelableArrayList("phoneContacts", phoneContacts);
            }
            else {
                outState.putStringArrayList("savedaddedContacts", savedaddedContacts);
            }
        }

        if (finished){
            savedaddedContacts.clear();
            if (contactType == Constants.CONTACT_TYPE_MEGA) {
                for (int i=0; i<addedContactsMEGA.size(); i++){
                    if (getMegaContactMail(addedContactsMEGA.get(i)) != null) {
                        savedaddedContacts.add(getMegaContactMail(addedContactsMEGA.get(i)));
                    }
                    else {
                        savedaddedContacts.add(addedContactsMEGA.get(i).getFullName());
                    }
                }
                outState.putStringArrayList("savedaddedContacts", savedaddedContacts);
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE) {
                outState.putParcelableArrayList("addedContactsPhone", addedContactsPhone);
                outState.putParcelableArrayList("filteredContactsPhone", filteredContactsPhone);
                outState.putParcelableArrayList("phoneContacts", phoneContacts);
            }
            else {
                for (int i=0; i<addedContactsShare.size(); i++){
                    if (addedContactsShare.get(i).isMegaContact()) {
                        savedaddedContacts.add(getMegaContactMail(addedContactsShare.get(i).getMegaContactAdapter()));
                    }
                    else if (addedContactsShare.get(i).isPhoneContact()) {
                        savedaddedContacts.add(addedContactsShare.get(i).getPhoneContactInfo().getEmail());
                    }
                    else {
                        savedaddedContacts.add(addedContactsShare.get(i).getMail());
                    }
                }
                outState.putStringArrayList("savedaddedContacts", savedaddedContacts);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate");

        super.onCreate(savedInstanceState);

        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        if (getIntent() != null){
            contactType = getIntent().getIntExtra("contactType", Constants.CONTACT_TYPE_MEGA);
            chatId = getIntent().getLongExtra("chatId", -1);
            fromAchievements = getIntent().getBooleanExtra("fromAchievements", false);
            if (fromAchievements){
                mailsFromAchievements = getIntent().getStringArrayListExtra(EXTRA_CONTACTS);
            }
            comesFromChat = getIntent().getBooleanExtra("chat", false);
            if (contactType == Constants.CONTACT_TYPE_MEGA || contactType == Constants.CONTACT_TYPE_BOTH){
                multipleSelectIntent = getIntent().getIntExtra("MULTISELECT", -1);
                if(multipleSelectIntent==0){
                    nodeHandle =  getIntent().getLongExtra(EXTRA_NODE_HANDLE, -1);
                }
                else if(multipleSelectIntent==1){
                    log("onCreate multiselect YES!");
                    nodeHandles=getIntent().getLongArrayExtra(EXTRA_NODE_HANDLE);
                }
            }
        }

        Display display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        app = (MegaApplication)getApplication();
        megaApi = app.getMegaApi();
        if(megaApi==null||megaApi.getRootNode()==null){
            log("Refresh session - sdk");
            Intent intent = new Intent(this, LoginActivityLollipop.class);
            intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }
        if(Util.isChatEnabled()){
            if (megaChatApi == null){
                megaChatApi = ((MegaApplication) getApplication()).getMegaChatApi();
            }

            if(megaChatApi==null||megaChatApi.getInitState()== MegaChatApi.INIT_ERROR){
                log("Refresh session - karere");
                Intent intent = new Intent(this, LoginActivityLollipop.class);
                intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        }

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
        aB.setHomeButtonEnabled(true);
        aB.setDisplayHomeAsUpEnabled(true);

        relativeLayout = (RelativeLayout) findViewById(R.id.relative_container_add_contact);

        contactErrorLayout = (RelativeLayout) findViewById(R.id.add_contact_email_error);
        contactErrorLayout.setVisibility(View.GONE);
        notPermitedAddContacts = (RelativeLayout) findViewById(R.id.not_permited_add_contact_error);
        notPermitedAddContacts.setVisibility(View.GONE);
        addedContactsRecyclerView = (RecyclerView) findViewById(R.id.contact_adds_recycler_view);
        containerAddedContactsRecyclerView = (RelativeLayout) findViewById(R.id.contacts_adds_container);
        separator = (LinearLayout) findViewById(R.id.separator);
        containerAddedContactsRecyclerView.setVisibility(View.GONE);
        fabImageGroup = (FloatingActionButton) findViewById(R.id.image_group_floating_button);
        fabImageGroup.setVisibility(View.GONE);
        nameGroup = (EditText) findViewById(R.id.name_group_edittext);
        nameGroup.setVisibility(View.GONE);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        addedContactsRecyclerView.setLayoutManager(mLayoutManager);
        addedContactsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        headerContacts = (RelativeLayout) findViewById(R.id.header_list);
        textHeader = (TextView) findViewById(R.id.text_header_list);

        fastScroller = (FastScroller) findViewById(R.id.fastscroll);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewList = (RecyclerView) findViewById(R.id.add_contact_list);
        recyclerViewList.setClipToPadding(false);
        recyclerViewList.setHasFixedSize(true);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        recyclerViewList.addOnItemTouchListener(this);
        recyclerViewList.setItemAnimator(new DefaultItemAnimator());
        fastScroller.setRecyclerView(recyclerViewList);

        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            headerContacts.setVisibility(View.VISIBLE);
            textHeader.setText(getString(R.string.contacts_mega));
            recyclerViewList.addItemDecoration(new SimpleDividerItemDecoration(this, outMetrics));
        }
        else if(contactType == Constants.CONTACT_TYPE_DEVICE) {
            headerContacts.setVisibility(View.VISIBLE);
            textHeader.setText(getString(R.string.contacts_phone));
            recyclerViewList.addItemDecoration(new SimpleDividerItemDecoration(this, outMetrics));
        }
        else {
            headerContacts.setVisibility(View.GONE);
        }

        containerContacts = (RelativeLayout) findViewById(R.id.container_list_contacts);

        emptyImageView = (ImageView) findViewById(R.id.add_contact_list_empty_image);
        emptyTextView = (TextView) findViewById(R.id.add_contact_list_empty_text);
        emptyImageView.setImageResource(R.drawable.ic_empty_contacts);
        emptyTextView.setText(R.string.contacts_list_empty_text_loading_share);

        progressBar = (ProgressBar) findViewById(R.id.add_contact_progress_bar);

        //Get MEGA contacts and phone contacts: first name, last name and email
        if (savedInstanceState != null) {
            onNewGroup = savedInstanceState.getBoolean("onNewGroup", onNewGroup);
            searchExpand = savedInstanceState.getBoolean("searchExpand", false);
            savedInputString = savedInstanceState.getString("inputString");

            if (contactType == Constants.CONTACT_TYPE_MEGA || contactType == Constants.CONTACT_TYPE_BOTH) {
                savedaddedContacts = savedInstanceState.getStringArrayList("savedaddedContacts");

                if (savedaddedContacts == null && contactType == Constants.CONTACT_TYPE_MEGA) {
                    setAddedAdapterContacts();
                    getContactsTask = new GetContactsTask();
                    getContactsTask.execute();
                }
                else if (savedaddedContacts == null && contactType == Constants.CONTACT_TYPE_BOTH) {
                    setAddedAdapterContacts();
                    queryIfHasReadContactsPermissions();
                }
                else {
                    recoverContactsTask = new RecoverContactsTask();
                    recoverContactsTask.execute();
                }
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE){
                addedContactsPhone = savedInstanceState.getParcelableArrayList("addedContactsPhone");
                filteredContactsPhone = savedInstanceState.getParcelableArrayList("filteredContactsPhone");
                phoneContacts = savedInstanceState.getParcelableArrayList("phoneContacts");

                setAddedAdapterContacts();

                if (filteredContactsPhone == null && phoneContacts == null) {
                    queryIfHasReadContactsPermissions();
                }
                else {
                    if (addedContactsPhone.size() == 0) {
                        containerAddedContactsRecyclerView.setVisibility(View.GONE);
                    }
                    else {
                        containerAddedContactsRecyclerView.setVisibility(View.VISIBLE);
                    }

                    addedContactsRecyclerView.setAdapter(adapterContacts);

                    if (phoneContacts != null && !phoneContacts.isEmpty()) {
                        if (filteredContactsPhone == null || filteredContactsPhone.isEmpty()) {
                            for (int i = 0; i < phoneContacts.size(); i++) {
                                filteredContactsPhone.add(phoneContacts.get(i));
                            }
                        }
                        setPhoneAdapterContacts(filteredContactsPhone);
                    }
                    else {
                        emptyImageView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.VISIBLE);

                        progressBar.setVisibility(View.VISIBLE);
                        getContactsTask = new GetContactsTask();
                        getContactsTask.execute();
                    }
                }
                setTitleAB();
                setRecyclersVisibility();
                setSeparatorVisibility();
            }
        }
        else {
            if (contactType == Constants.CONTACT_TYPE_MEGA) {
                setAddedAdapterContacts();

                progressBar.setVisibility(View.VISIBLE);
                getContactsTask = new GetContactsTask();
                getContactsTask.execute();
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE){
                setAddedAdapterContacts();
                queryIfHasReadContactsPermissions();
            }
            else {
                setAddedAdapterContacts();
                queryIfHasReadContactsPermissions();
            }
        }
    }

    void queryIfHasReadContactsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasReadContactsPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
            if (!hasReadContactsPermission) {
                log("No read contacts permission");
                ActivityCompat.requestPermissions((AddContactActivityLollipop) this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        Constants.REQUEST_READ_CONTACTS);
            }
            else {
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.VISIBLE);
                getContactsTask = new GetContactsTask();
                getContactsTask.execute();
            }
        }
        else{
            emptyImageView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.VISIBLE);
            getContactsTask = new GetContactsTask();
            getContactsTask.execute();
        }
    }

    private void setTitleAB() {
        log("setTitleAB");
        if (aB != null) {
            if (contactType == Constants.CONTACT_TYPE_MEGA){
                if (!onNewGroup) {
                    aB.setTitle(getString(R.string.group_chat_start_conversation_label));
                    if (addedContactsMEGA.size() > 0) {
                        aB.setSubtitle(addedContactsMEGA.size() + " " + getResources().getQuantityString(R.plurals.general_num_contacts, addedContactsMEGA.size()));
                    }
                    else {
                        aB.setSubtitle(null);
                    }
                }
            }
            else if (contactType == Constants.CONTACT_TYPE_DEVICE){
                aB.setTitle(getString(R.string.invite_contacts));
                if (addedContactsPhone.size() > 0){
                    aB.setSubtitle(addedContactsPhone.size() + " " + getResources().getQuantityString(R.plurals.general_num_contacts, addedContactsPhone.size()));
                }
                else {
                    aB.setSubtitle(null);
                }
            }
            else {
                aB.setTitle(getString(R.string.share_with));
                if (addedContactsShare.size() > 0){
                    aB.setSubtitle(addedContactsShare.size() + " " + getResources().getQuantityString(R.plurals.general_num_contacts, addedContactsShare.size()));
                }
                else {
                    aB.setSubtitle(null);
                }
            }
        }
    }

    private void setError(){
        log("setError");
        if (comesFromChat){
            notPermitedAddContacts.setVisibility(View.VISIBLE);
        }
        else {
            contactErrorLayout.setVisibility(View.VISIBLE);
        }
    }

    private void quitError(){
        if(contactErrorLayout.getVisibility() != View.GONE){
            log("quitError");
            contactErrorLayout.setVisibility(View.GONE);
        }
        if(notPermitedAddContacts.getVisibility() != View.GONE){
            log("quitError");
            notPermitedAddContacts.setVisibility(View.GONE);
        }
    }

    public void addShareContact (ShareContactInfo contact) {
        log("addShareContact");

        addedContactsShare.add(contact);
        adapterShare.setContacts(addedContactsShare);
        mLayoutManager.scrollToPosition(adapterShare.getItemCount()-1);
        setSendInvitationVisibility();
        containerAddedContactsRecyclerView.setVisibility(View.VISIBLE);
        setTitleAB();

        if (adapterShareHeader != null){
            if (adapterShareHeader.getItemCount() == 0){
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);

                String textToShow = String.format(getString(R.string.context_empty_contacts), getString(R.string.section_contacts));
                try{
                    textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                    textToShow = textToShow.replace("[/A]", "</font>");
                    textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                    textToShow = textToShow.replace("[/B]", "</font>");
                }
                catch (Exception e){}
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }
                emptyTextView.setText(result);
            }
            else {
                emptyImageView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.GONE);
            }
        }
        setRecyclersVisibility();
        setSeparatorVisibility();
        refreshKeyboard();
    }

    public void addContactMEGA (MegaContactAdapter contact) {
        log("addContactMEGA: " + contact.getFullName());

        addedContactsMEGA.add(contact);
        adapterMEGAContacts.setContacts(addedContactsMEGA);
        mLayoutManager.scrollToPosition(adapterMEGAContacts.getItemCount()-1);
        setSendInvitationVisibility();
        containerAddedContactsRecyclerView.setVisibility(View.VISIBLE);
        setTitleAB();
        if (adapterMEGA != null){
            if (adapterMEGA.getItemCount() == 1){
                headerContacts.setVisibility(View.GONE);
                recyclerViewList.setVisibility(View.GONE);
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);

                String textToShow = String.format(getString(R.string.context_empty_contacts), getString(R.string.section_contacts));
                try{
                    textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                    textToShow = textToShow.replace("[/A]", "</font>");
                    textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                    textToShow = textToShow.replace("[/B]", "</font>");
                }
                catch (Exception e){}
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }
                emptyTextView.setText(result);
            }
        }
        setRecyclersVisibility();
        setSeparatorVisibility();
        refreshKeyboard();
    }

    public void addContact (PhoneContactInfo contact){
        log("addContact: " + contact.getName()+" mail: " + contact.getEmail());

        addedContactsPhone.add(contact);
        adapterContacts.setContacts(addedContactsPhone);
        mLayoutManager.scrollToPosition(adapterContacts.getItemCount()-1);
        setSendInvitationVisibility();
        containerAddedContactsRecyclerView.setVisibility(View.VISIBLE);
        setTitleAB();

        if(adapterPhone!=null){
            if (adapterPhone.getItemCount() == 0){
                headerContacts.setVisibility(View.GONE);
                recyclerViewList.setVisibility(View.GONE);
                emptyImageView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);

                String textToShow = String.format(getString(R.string.context_empty_contacts), getString(R.string.section_contacts));
                try{
                    textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                    textToShow = textToShow.replace("[/A]", "</font>");
                    textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                    textToShow = textToShow.replace("[/B]", "</font>");
                }
                catch (Exception e){}
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }
                emptyTextView.setText(result);
            }
        }
        setRecyclersVisibility();
        setSeparatorVisibility();
        refreshKeyboard();
    }

    public void deleteContact (int position){
        log("deleteContact: " +position);

        if (contactType == Constants.CONTACT_TYPE_MEGA){
            MegaContactAdapter deleteContact = addedContactsMEGA.get(position);
            if (deleteContact.getMegaUser() != null || deleteContact.getMegaContactDB() != null) {
                addMEGAFilteredContact(deleteContact);
            }
            addedContactsMEGA.remove(deleteContact);
            setSendInvitationVisibility();
            adapterMEGAContacts.setContacts(addedContactsMEGA);
            if (addedContactsMEGA.size() == 0){
                containerAddedContactsRecyclerView.setVisibility(View.GONE);
            }
        }
        else if (contactType == Constants.CONTACT_TYPE_DEVICE){
            PhoneContactInfo deleteContact = addedContactsPhone.get(position);
            if (deleteContact.getName() != null) {
                addFilteredContact(deleteContact);
            }
            addedContactsPhone.remove(deleteContact);
            setSendInvitationVisibility();
            adapterContacts.setContacts(addedContactsPhone);
            if (addedContactsPhone.size() == 0){
                containerAddedContactsRecyclerView.setVisibility(View.GONE);
            }
        }
        else {
            ShareContactInfo deleteContact = addedContactsShare.get(position);

            if (deleteContact.isPhoneContact()) {
                addFilteredContact(deleteContact.getPhoneContactInfo());
            }
            else if (deleteContact.isMegaContact()) {
                addMEGAFilteredContact(deleteContact.getMegaContactAdapter());
            }

            addedContactsShare.remove(deleteContact);
            setSendInvitationVisibility();
            adapterShare.setContacts(addedContactsShare);
            if (addedContactsShare.size() == 0){
                containerAddedContactsRecyclerView.setVisibility(View.GONE);
            }
        }
        setTitleAB();
        setRecyclersVisibility();
        setSeparatorVisibility();
        refreshKeyboard();
    }

    public void showSnackbar(String message) {
        hideKeyboard();
        Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);
        snackbar.show();
    }

    private void addMEGAFilteredContact (MegaContactAdapter contact) {

        filteredContactMEGA.add(contact);
        Collections.sort(filteredContactMEGA, new Comparator<MegaContactAdapter>(){

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

        int index = filteredContactMEGA.indexOf(contact);

        if (contactType == Constants.CONTACT_TYPE_BOTH) {
            int i = filteredContactMEGA.indexOf(contact);
            ShareContactInfo contactToAdd = new ShareContactInfo(null, contact, null);
            if (filteredContactMEGA.size() == 1) {
                contactToAdd.setLastItem(true);
                filteredContactsShare.add(0, new ShareContactInfo(true, true, false));
                filteredContactsShare.add(1, contactToAdd);
            }
            else if (i == filteredContactMEGA.size() -1){
                contactToAdd.setLastItem(true);
                filteredContactsShare.get(i).setLastItem(false);
                filteredContactsShare.add(i+1, contactToAdd);
            }
            else {
                filteredContactsShare.add(i+1, contactToAdd);
            }
            adapterShareHeader.setContacts(filteredContactsShare);
            linearLayoutManager.scrollToPosition(index + 1);
        }
        else {
            if (!onNewGroup) {
                adapterMEGA.setContacts(filteredContactMEGA);
                linearLayoutManager.scrollToPosition(index);
                if (adapterMEGA.getItemCount() != 0) {
                    headerContacts.setVisibility(View.VISIBLE);
                    recyclerViewList.setVisibility(View.VISIBLE);
                    emptyImageView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void addFilteredContact(PhoneContactInfo contact) {
        log("addFilteredContact");
        filteredContactsPhone.add(contact);
        Collections.sort(filteredContactsPhone);
        int index = filteredContactsPhone.indexOf(contact);
        int position;

        log("Size filteredContactsPhone: " +filteredContactsPhone.size());

        if (contactType == Constants.CONTACT_TYPE_BOTH) {
            if (filteredContactsPhone.size() == 1){
                filteredContactsShare.add(filteredContactsShare.size(), new ShareContactInfo(true, false, true));
                filteredContactsShare.add(filteredContactsShare.size(), new ShareContactInfo(contact, null, null));
                position = filteredContactsShare.size();
            }
            else {
                position = (adapterShareHeader.getItemCount()-filteredContactsPhone.size())+index+1;
                filteredContactsShare.add(position, new ShareContactInfo(contact, null, null));
            }
            adapterShareHeader.setContacts(filteredContactsShare);
            linearLayoutManager.scrollToPosition(position);
        }
        else {
            adapterPhone.setContacts(filteredContactsPhone);
            linearLayoutManager.scrollToPosition(index);
            if(adapterPhone!=null){
                if (adapterPhone.getItemCount() != 0){
                    headerContacts.setVisibility(View.VISIBLE);
                    recyclerViewList.setVisibility(View.VISIBLE);
                    emptyImageView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void getVisibleMEGAContacts () {
        contactsMEGA = megaApi.getContacts();
        visibleContactsMEGA.clear();
        filteredContactMEGA.clear();
        addedContactsMEGA.clear();

        if(chatId!=-1){
            log("Add participant to chat");
            if(megaChatApi!=null){
                MegaChatRoom chat = megaChatApi.getChatRoom(chatId);
                if(chat!=null){
                    long participantsCount = chat.getPeerCount();

                    for (int i=0;i<contactsMEGA.size();i++){
                        if (contactsMEGA.get(i).getVisibility() == MegaUser.VISIBILITY_VISIBLE){

                            boolean found = false;

                            for(int j=0;j<participantsCount;j++) {

                                long peerHandle = chat.getPeerHandle(j);

                                if(peerHandle == contactsMEGA.get(i).getHandle()){
                                    found = true;
                                    break;
                                }
                            }

                            if(!found){
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

                                log("Added to list: " + contactsMEGA.get(i).getEmail() + "_" + contactsMEGA.get(i).getVisibility());
                                MegaContactAdapter megaContactAdapter = new MegaContactAdapter(contactDB, contactsMEGA.get(i), fullName);
                                visibleContactsMEGA.add(megaContactAdapter);
                            }
                            else{
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

                                log("Removed from list - already included on chat: "+fullName);
                            }
                        }
                    }
                }
                else{
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
                }
            }
        }
        else{
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

        for (int i= 0; i<visibleContactsMEGA.size(); i++){
            filteredContactMEGA.add(visibleContactsMEGA.get(i));
        }
    }

    @SuppressLint("InlinedApi")
    //Get the contacts explicitly added
    private ArrayList<PhoneContactInfo> getPhoneContacts() {
        log("getPhoneContacts");
        ArrayList<PhoneContactInfo> contactList = new ArrayList<>();
        log("inputString empty");
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''  AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1";

        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            String SORT_ORDER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.SORT_KEY_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

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
                    if (inputString == ""){
                        log("VALID Contact: "+ name + " ---> "+ emailAddress);
                        PhoneContactInfo contactPhone = new PhoneContactInfo(id, name, emailAddress, null);
                        contactList.add(contactPhone);
                    }
                    else if (!inputString.isEmpty() && (name.toUpperCase().contains(inputString.toUpperCase()) || emailAddress.toUpperCase().contains(inputString.toUpperCase()))){
                        log("VALID Contact: "+ name + " ---> "+ emailAddress + " inputString: " + inputString);
                        PhoneContactInfo contactPhone = new PhoneContactInfo(id, name, emailAddress, null);
                        contactList.add(contactPhone);
                    }
                }
            }
            c.close();

            log("contactList.size() = " + contactList.size());

            return contactList;

        } catch (Exception e) { log ("Exception: " + e.getMessage()); }

        return null;
    }

    public void onTextChanged(CharSequence s, int start, int before, int count){

    }

    public void itemClick(String email, int adapter){

        log("itemClick");
        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            if (adapter == MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT) {
                for (int i = 0; i < filteredContactMEGA.size(); i++) {
                    if (getMegaContactMail(filteredContactMEGA.get(i)).equals(email)) {
                        addContactMEGA(filteredContactMEGA.get(i));
                        filteredContactMEGA.remove(i);
                        adapterMEGA.setContacts(filteredContactMEGA);
                        break;
                    }
                }

                if (addedContactsMEGA.size() == 0) {
                    setSendInvitationVisibility();
                }
                if (searchExpand) {
                    filterContactsTask = new FilterContactsTask();
                    filterContactsTask.execute();
                }
            }
            else {
                for (int i=0; i<addedContactsMEGA.size(); i++) {
                    if (getMegaContactMail(addedContactsMEGA.get(i)).equals(email)) {
                        addMEGAFilteredContact(addedContactsMEGA.get(i));
                        addedContactsMEGA.remove(i);
                        adapterMEGA.setContacts(addedContactsMEGA);
                        adapterMEGAContacts.setContacts(addedContactsMEGA);
                    }
                }
                if (addedContactsMEGA.size() == 0) {
                    setSendInvitationVisibility();
                }
                textHeader.setText(addedContactsMEGA.size()+" "+getString(R.string.participants_chat_label));
            }
        }
    }

    public void itemClick(View view, int position) {
        log("on item click");

        if (contactType == Constants.CONTACT_TYPE_DEVICE){

            if(adapterPhone==null){
                return;
            }

            final PhoneContactInfo contact = adapterPhone.getItem(position);
            if(contact == null) {
                return;
            }

            for (int i = 0; i < filteredContactsPhone.size(); i++) {
                if (filteredContactsPhone.get(i).getEmail().compareTo(contact.getEmail()) == 0) {
                    filteredContactsPhone.remove(i);
                    adapterPhone.setContacts(filteredContactsPhone);
                    break;
                }
            }
            addContact(contact);
        }
        else if (contactType == Constants.CONTACT_TYPE_BOTH){
            if(adapterShareHeader==null){
                return;
            }

            final ShareContactInfo contact = adapterShareHeader.getItem(position);
            if(contact == null || contact.isHeader()) {
                return;
            }
            if (contact.isMegaContact()) {
                if (filteredContactMEGA.size() == 1){
                    filteredContactsShare.remove(0);
                }
                else if (position == filteredContactMEGA.size()) {
                    filteredContactsShare.get(position-1).setLastItem(true);
                }
                filteredContactMEGA.remove(contact.getMegaContactAdapter());
            }
            else if (contact.isPhoneContact()) {
                filteredContactsPhone.remove(contact.getPhoneContactInfo());
                if (filteredContactsPhone.size() == 0) {
                    filteredContactsShare.remove(filteredContactsShare.size()-2);
                }
            }
            filteredContactsShare.remove(contact);
            adapterShareHeader.setContacts(filteredContactsShare);

            addShareContact(contact);
        }
        if (searchExpand) {
            filterContactsTask = new FilterContactsTask();
            filterContactsTask.execute();
        }
    }

    public String getShareContactMail(ShareContactInfo contact) {
        String mail = null;

        if (contact.isMegaContact() && !contact.isHeader()) {
            if (contact.getMegaContactAdapter().getMegaUser() != null && contact.getMegaContactAdapter().getMegaUser().getEmail() != null) {
                mail = contact.getMegaContactAdapter().getMegaUser().getEmail();
            } else if (contact.getMegaContactAdapter().getMegaContactDB() != null && contact.getMegaContactAdapter().getMegaContactDB().getMail() != null) {
                mail = contact.getMegaContactAdapter().getMegaContactDB().getMail();
            }
        }
        else if (contact.isPhoneContact() && !contact.isHeader()){
            mail = contact.getPhoneContactInfo().getEmail();
        }
        else{
            mail = contact.getMail();
        }

        return mail;
    }

    public String getMegaContactMail (MegaContactAdapter contact) {
        String mail = null;
        if (contact != null) {
            if (contact.getMegaUser() != null && contact.getMegaUser().getEmail() != null) {
                mail = contact.getMegaUser().getEmail();
            }
            else if (contact.getMegaContactDB() != null && contact.getMegaContactDB().getMail() != null) {
                mail = contact.getMegaContactDB().getMail();
            }
        }
        return mail;
    }

    @Override
    public void onClick(View v) {
        ((MegaApplication) getApplication()).sendSignalPresenceActivity();

    }

    @Override
    public void onBackPressed() {
        if (onNewGroup) {
            returnToAddContacts();
        }
        else {
            super.onBackPressed();
        }
    }

    private void setResultContacts(ArrayList<MegaContactAdapter> addedContacts, boolean megaContacts){
        log("setResultContacts");
        ArrayList<String> contactsSelected = new ArrayList<String>();
        String contactEmail;

        if (addedContacts != null) {
            for (int i = 0; i < addedContacts.size(); i++) {
                if (addedContacts.get(i).getMegaUser() != null && addedContacts.get(i).getMegaContactDB() != null) {
                    contactEmail = addedContacts.get(i).getMegaUser().getEmail();
                } else {
                    contactEmail = addedContacts.get(i).getFullName();
                }
                if (contactEmail != null) {
                    contactsSelected.add(contactEmail);
                }
            }
        }
        log("contacts selected: "+contactsSelected.size());
        if (contactsSelected.size() == 1)  {
            startConversation(contactsSelected, megaContacts);
        }
        else {
            if (onNewGroup) {
                startConversation(contactsSelected, megaContacts);
            }
            else {
                newGroup();
            }
        }
    }

    private void returnToAddContacts() {
        onNewGroup = false;
        setTitleAB();
        textHeader.setText(getString(R.string.contacts_mega));
        searchMenuItem.setVisible(true);
        addedContactsRecyclerView.setVisibility(View.VISIBLE);
        setRecyclersVisibility();
        fabImageGroup.setVisibility(View.GONE);
        nameGroup.setVisibility(View.GONE);
        if (addedContactsMEGA.size() == 0) {
            containerAddedContactsRecyclerView.setVisibility(View.GONE);
        }
        setMegaAdapterContacts(filteredContactMEGA, MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_ADD_CONTACT);
        visibilityFastScroller();
    }

    private void newGroup () {
        onNewGroup = true;
        if (aB != null) {
            aB.setTitle(getString(R.string.title_new_group));
            aB.setSubtitle(getString(R.string.subtitle_new_group));
        }
        textHeader.setText(addedContactsMEGA.size()+" "+getString(R.string.participants_chat_label));
        searchMenuItem.setVisible(false);
        addedContactsRecyclerView.setVisibility(View.GONE);
        fabImageGroup.setVisibility(View.VISIBLE);
        nameGroup.setVisibility(View.VISIBLE);
        setSendInvitationVisibility();
        setMegaAdapterContacts(addedContactsMEGA, MegaContactsLollipopAdapter.ITEM_VIEW_TYPE_LIST_GROUP_CHAT);
        visibilityFastScroller();
    }

    private void startConversation (ArrayList<String> contacts, boolean megaContacts) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_CONTACTS, contacts);

        if(multipleSelectIntent==0){
            intent.putExtra(EXTRA_NODE_HANDLE, nodeHandle);
            intent.putExtra("MULTISELECT", 0);
        }
        else if(multipleSelectIntent==1){
            intent.putExtra(EXTRA_NODE_HANDLE, nodeHandles);
            intent.putExtra("MULTISELECT", 1);
        }

        intent.putExtra(EXTRA_MEGA_CONTACTS, megaContacts);
        setResult(RESULT_OK, intent);
        hideKeyboard();
        finish();
    }

    private void shareWith (ArrayList<ShareContactInfo> addedContacts){
        log("shareWith");

        ArrayList<String> contactsSelected = new ArrayList<>();
        if (addedContacts != null){
            for (int i=0; i<addedContacts.size(); i++){
                contactsSelected.add(getShareContactMail(addedContacts.get(i)));
            }
        }

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

        intent.putExtra(EXTRA_MEGA_CONTACTS, false);
        setResult(RESULT_OK, intent);
        hideKeyboard();
        finish();
    }

    private void inviteContacts(ArrayList<PhoneContactInfo> addedContacts){
        log("inviteContacts");

        String contactEmail = null;
        ArrayList<String> contactsSelected = new ArrayList<>();
        if (addedContacts != null) {
            for (int i=0;i<addedContacts.size();i++) {
                contactEmail = addedContacts.get(i).getEmail();
                if (fromAchievements){
                    if (contactEmail != null && !mailsFromAchievements.contains(contactEmail)) {
                        contactsSelected.add(contactEmail);
                    }
                }
                else {
                    if (contactEmail != null) {
                        contactsSelected.add(contactEmail);
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
        hideKeyboard();
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
                    if (hasReadContactsPermissions && contactType == Constants.CONTACT_TYPE_DEVICE) {
                        filteredContactsPhone.clear();
                        emptyImageView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.VISIBLE);

                        progressBar.setVisibility(View.VISIBLE);
                        new GetContactsTask().execute();
                    }
                    else if (hasReadContactsPermissions && contactType == Constants.CONTACT_TYPE_BOTH) {
                        progressBar.setVisibility(View.VISIBLE);
                        emptyTextView.setText(R.string.contacts_list_empty_text_loading_share);

                        getContactsTask = new GetContactsTask();
                        getContactsTask.execute();
                    }
                }
                else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean hasReadContactsPermissions = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED);
                    if (hasReadContactsPermissions && contactType == Constants.CONTACT_TYPE_DEVICE) {
                        log("Permission denied");
                        filteredContactsPhone.clear();
                        emptyImageView.setVisibility(View.VISIBLE);
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText(R.string.no_contacts_permissions);

                        progressBar.setVisibility(View.GONE);
                    }
                }
                break;
            }
        }
    }

    public void setSeparatorVisibility (){
        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            if (addedContactsMEGA.size() > 0){
                separator.setVisibility(View.VISIBLE);
            }
            else {
                separator.setVisibility(View.GONE);
            }
        }
        else if (contactType ==  Constants.CONTACT_TYPE_DEVICE) {
            if (addedContactsPhone.size() > 0){
                separator.setVisibility(View.VISIBLE);
            }
            else {
                separator.setVisibility(View.GONE);
            }
        }
        else {
            if (addedContactsShare.size() > 0){
                separator.setVisibility(View.VISIBLE);
            }
            else {
                separator.setVisibility(View.GONE);
            }
        }
    }

    public void setRecyclersVisibility () {
        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            if (filteredContactMEGA.size() > 0){
                containerContacts.setVisibility(View.VISIBLE);
            }
            else {
                containerContacts.setVisibility(View.GONE);
            }
        }
        else if (contactType ==  Constants.CONTACT_TYPE_DEVICE) {
            if (filteredContactsPhone.size() > 0){
                containerContacts.setVisibility(View.VISIBLE);
            }
            else {
                containerContacts.setVisibility(View.GONE);
            }
        }
        else {
            if (filteredContactsShare.size() >= 2){
                containerContacts.setVisibility(View.VISIBLE);
            }
            else {
                containerContacts.setVisibility(View.GONE);
            }
        }
    }

    public void visibilityFastScroller(){
        fastScroller.setRecyclerView(recyclerViewList);
        if (contactType == Constants.CONTACT_TYPE_MEGA) {
            if(adapterMEGA == null){
                fastScroller.setVisibility(View.GONE);
            }
            else{
                if(adapterMEGA.getItemCount() < 20){
                    fastScroller.setVisibility(View.GONE);
                }
                else{
                    fastScroller.setVisibility(View.VISIBLE);
                }
            }
        }
        else if (contactType == Constants.CONTACT_TYPE_DEVICE) {
            if(adapterPhone == null){
                fastScroller.setVisibility(View.GONE);
            }
            else{
                if(adapterPhone.getItemCount() < 20){
                    fastScroller.setVisibility(View.GONE);
                }
                else{
                    fastScroller.setVisibility(View.VISIBLE);
                }
            }
        }
        else {
            if(adapterShareHeader == null){
                fastScroller.setVisibility(View.GONE);
            }
            else{
                if(adapterShareHeader.getItemCount() < 20){
                    fastScroller.setVisibility(View.GONE);
                }
                else{
                    fastScroller.setVisibility(View.VISIBLE);
                }
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
