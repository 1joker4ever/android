package mega.privacy.android.app.lollipop.megachat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.MegaLinearLayoutManager;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaChatLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaListChatLollipopAdapter;
import mega.privacy.android.app.lollipop.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatListenerInterface;
import nz.mega.sdk.MegaChatRoom;

public class RecentChatsFragmentLollipop extends Fragment implements MegaChatListenerInterface, RecyclerView.OnItemTouchListener, GestureDetector.OnGestureListener, View.OnClickListener {

    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;

    Context context;
    ActionBar aB;
    RecyclerView listView;
    MegaListChatLollipopAdapter adapterList;
    GestureDetectorCompat detector;

    RecyclerView.LayoutManager mLayoutManager;

    ArrayList<MegaChatListItem> chats;

    //Empty screen
    TextView emptyTextView;
    LinearLayout emptyLayout;
    TextView emptyTextViewInvite;
    ImageView emptyImageView;
    Button inviteButton;

    float scaleH, scaleW;
    float density;
    DisplayMetrics outMetrics;
    Display display;

    private ActionMode actionMode;

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        public void onLongPress(MotionEvent e) {
            log("onLongPress");
            View view = listView.findChildViewUnder(e.getX(), e.getY());
            int position = listView.getChildLayoutPosition(view);

            // handle long press
            if (!adapterList.isMultipleSelect()){
                adapterList.setMultipleSelect(true);

                actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());

                itemClick(position);
            }

            super.onLongPress(e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if (megaChatApi == null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        megaChatApi.addChatListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView");

        detector = new GestureDetectorCompat(getActivity(), new RecyclerViewOnGestureListener());

        display = ((Activity) context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = getResources().getDisplayMetrics().density;

        View v = inflater.inflate(R.layout.chat_recent_tab, container, false);

        listView = (RecyclerView) v.findViewById(R.id.chat_recent_list_view);
        listView.setPadding(0, 0, 0, Util.scaleHeightPx(85, outMetrics));
        listView.setClipToPadding(false);

        listView.addItemDecoration(new SimpleDividerItemDecoration(context, outMetrics));
        mLayoutManager = new MegaLinearLayoutManager(context);
        listView.setLayoutManager(mLayoutManager);
        //Just onClick implemented
        listView.addOnItemTouchListener(this);
        listView.setItemAnimator(new DefaultItemAnimator());

        emptyLayout = (LinearLayout) v.findViewById(R.id.linear_empty_layout_chat_recent);
        emptyTextViewInvite = (TextView) v.findViewById(R.id.empty_text_chat_recent_invite);
        emptyTextViewInvite.setWidth(Util.scaleWidthPx(236, outMetrics));
        emptyTextView = (TextView) v.findViewById(R.id.empty_text_chat_recent);

        LinearLayout.LayoutParams emptyTextViewParams1 = (LinearLayout.LayoutParams)emptyTextViewInvite.getLayoutParams();
        emptyTextViewParams1.setMargins(0, Util.scaleHeightPx(50, outMetrics), 0, Util.scaleHeightPx(24, outMetrics));
        emptyTextViewInvite.setLayoutParams(emptyTextViewParams1);

        LinearLayout.LayoutParams emptyTextViewParams2 = (LinearLayout.LayoutParams)emptyTextView.getLayoutParams();
        emptyTextViewParams2.setMargins(0, Util.scaleHeightPx(20, outMetrics), 0, Util.scaleHeightPx(20, outMetrics));
        emptyTextView.setLayoutParams(emptyTextViewParams2);

        emptyImageView = (ImageView) v.findViewById(R.id.empty_image_view_chat);

        inviteButton = (Button) v.findViewById(R.id.invite_button);
        inviteButton.setOnClickListener(this);

        this.setChats();

        return v;
    }

    public static RecentChatsFragmentLollipop newInstance() {
        log("newInstance");
        RecentChatsFragmentLollipop fragment = new RecentChatsFragmentLollipop();
        return fragment;
    }

    public void setChats(){
//        ArrayList<MegaChatRoom> temporalChats = megaChatApi.getChatRooms();
//        chats.clear();
//
//        for(int i=0;i<chats.size();i++){
//            MegaChatRoom chat = chats.get(i);
//            int privilege = chat.getOwnPrivilege();
//            if(privilege==MegaChatRoom.PRIV_RM){
//                log("Borradooooo");
//            }
//            else if(privilege==MegaChatRoom.PRIV_UNKNOWN){
//                log("Unknow");
//            }
//            else{
//                log("otro privilegio");
//                chats.add(chat);
//            }
//        }

        chats = megaChatApi.getChatListItems();
        megaChatApi.getChatListItems();

        if (adapterList == null){
            adapterList = new MegaListChatLollipopAdapter(context, this, chats, listView, MegaListChatLollipopAdapter.ADAPTER_RECENT_CHATS);
        }
        else{
            adapterList.setChats(chats);
        }

        adapterList.setPositionClicked(-1);
        listView.setAdapter(adapterList);

        if (adapterList.getItemCount() == 0){
            log("adapterList.getItemCount() == 0");
            listView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        }
        else{
            log("adapterList.getItemCount() NOT = 0");
            listView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        detector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public void onClick(View v) {
        log("onClick");
        switch (v.getId()) {
            case R.id.invite_button:{
                ((ManagerActivityLollipop)context).chooseAddContactDialog(false);
                break;
            }
        }
    }

    /////Multiselect/////
    private class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            List<MegaChatListItem> chats = adapterList.getSelectedChats();

            switch(item.getItemId()){
                case R.id.cab_menu_select_all:{
                    selectAll();
                    actionMode.invalidate();
                    break;
                }
                case R.id.cab_menu_unselect_all:{
                    clearSelections();
                    hideMultipleSelect();
                    actionMode.invalidate();
                    break;
                }
                case R.id.cab_menu_mute:{
                    clearSelections();
                    hideMultipleSelect();
                    //Mute
                    Toast.makeText(context, "Mute: "+chats.size()+" chats",Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.cab_menu_archive:{
                    clearSelections();
                    hideMultipleSelect();
                    //Archive
                    Toast.makeText(context, "Archive: "+chats.size()+" chats",Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.cab_menu_delete:{
                    clearSelections();
                    hideMultipleSelect();
                    //Delete
                    Toast.makeText(context, "Delete: "+chats.size()+" chats",Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.recent_chat_action, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            adapterList.setMultipleSelect(false);
            clearSelections();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            List<MegaChatListItem> selected = adapterList.getSelectedChats();

            if (selected.size() != 0) {
                menu.findItem(R.id.cab_menu_mute).setVisible(true);
                menu.findItem(R.id.cab_menu_archive).setVisible(true);
                menu.findItem(R.id.cab_menu_delete).setVisible(true);

                MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);
                if(selected.size()==adapterList.getItemCount()){
                    menu.findItem(R.id.cab_menu_select_all).setVisible(false);
                    unselect.setTitle(getString(R.string.action_unselect_all));
                    unselect.setVisible(true);
                }
                else if(selected.size()==1){
                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                    unselect.setTitle(getString(R.string.action_unselect_one));
                    unselect.setVisible(true);
                }
                else{
                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                    unselect.setTitle(getString(R.string.action_unselect_all));
                    unselect.setVisible(true);
                }

            }
            else{
                menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
            }

            return false;
        }

    }

    public boolean showSelectMenuItem(){
        if (adapterList != null){
            return adapterList.isMultipleSelect();
        }

        return false;
    }

    /*
     * Clear all selected items
     */
    private void clearSelections() {
        if(adapterList.isMultipleSelect()){
            adapterList.clearSelections();
        }
        updateActionModeTitle();
    }

    private void updateActionModeTitle() {
        if (actionMode == null || getActivity() == null) {
            return;
        }
        List<MegaChatListItem> chats = adapterList.getSelectedChats();

        actionMode.setTitle(context.getString(R.string.selected_items, chats.size()));

        try {
            actionMode.invalidate();
        } catch (NullPointerException e) {
            e.printStackTrace();
            log("oninvalidate error");
        }
    }

    /*
     * Disable selection
     */
    void hideMultipleSelect() {
        log("hideMultipleSelect");
        adapterList.setMultipleSelect(false);
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public void selectAll() {
        if (adapterList != null) {
            if (adapterList.isMultipleSelect()) {
                adapterList.selectAll();
            } else {
                adapterList.setMultipleSelect(true);
                adapterList.selectAll();

                actionMode = ((AppCompatActivity) context).startSupportActionMode(new ActionBarCallBack());
            }

            updateActionModeTitle();
        }
    }

    public void itemClick(int position) {
        log("itemClick");
        if (adapterList.isMultipleSelect()){
            adapterList.toggleSelection(position);
            List<MegaChatListItem> chats = adapterList.getSelectedChats();
            if (chats.size() > 0){
                updateActionModeTitle();
//                adapterList.notifyDataSetChanged();
            }
            else{
                hideMultipleSelect();
            }
        }
        else{
            log("open chat");
            Intent intent = new Intent(context, ChatActivityLollipop.class);
            intent.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
            intent.putExtra("CHAT_ID", chats.get(position).getChatId());
            this.startActivity(intent);
        }
    }
    /////END Multiselect/////

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        aB = ((AppCompatActivity)context).getSupportActionBar();
    }

    public void resetAdapter(){
        log("resetAdapter");
        if(adapterList!=null){
            adapterList.setPositionClicked(-1);
        }
    }

    @Override
    public void onChatListItemUpdate(MegaChatApiJava api, MegaChatListItem item) {
        log("onChatListItemUpdate");

        if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_STATUS)){
            log("Change status");

            if(item!=null) {

                long chatHandleToUpdate = item.getChatId();
                int indexToReplace = -1;
                ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                while (itrReplace.hasNext()) {
                    MegaChatListItem chat = itrReplace.next();
                    if (chat != null) {
                        if (chat.getChatId() == chatHandleToUpdate) {
                            indexToReplace = itrReplace.nextIndex() - 1;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (indexToReplace != -1) {
                    log("Index to replace: " + indexToReplace);

                    log("Item status: "+item.getOnlineStatus());
                    chats.set(indexToReplace, item);

                    adapterList.modifyChat(chats, indexToReplace);
                }
            }
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_PARTICIPANTS)){
            log("Change participants");
            MegaChatRoom chatToCheck = megaChatApi.getChatRoom(item.getChatId());
            updateCacheForNonContacts(chatToCheck);
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_VISIBILITY)){
            log("Change visibility");

        }
        else if((item.hasChanged(MegaChatListItem.CHANGE_TYPE_TITLE))||(item.hasChanged(MegaChatListItem.CHANGE_TYPE_UNREAD_COUNT))||(item.hasChanged(MegaChatListItem.CHANGE_TYPE_LAST_MSG))){
            log("Change title or unread count or last message: "+item.getChanges());

            if(item!=null){

                if (adapterList == null || adapterList.getItemCount()==0){
                    setChats();
                }
                else{
                    long chatHandleToUpdate = item.getChatId();
                    int indexToReplace = -1;
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if(chat!=null){
                            if(chat.getChatId()==chatHandleToUpdate){
                                indexToReplace = itrReplace.nextIndex()-1;
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    if(indexToReplace!=-1){
                        log("Index to replace: "+indexToReplace);
                        int checkUnread = item.getUnreadCount();
                        log("Unread count: "+item.getUnreadCount());


                        findViewHolder(indexToReplace);


//                        chats.set(indexToReplace, item);

//                        adapterList.modifyChat(chats, indexToReplace);
//                        adapterList.setPositionClicked(-1);
//
//                        if (adapterList.getItemCount() == 0){
//                            log("adapterList.getItemCount() == 0");
//                            listView.setVisibility(View.GONE);
//                            emptyLayout.setVisibility(View.VISIBLE);
//                        }
//                        else{
//                            log("adapterList.getItemCount() NOT = 0");
//                            listView.setVisibility(View.VISIBLE);
//                            emptyLayout.setVisibility(View.GONE);
//                        }
//
//                        findViewHolder(indexToReplace);
                    }
                }
            }

        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_CLOSED)){
            log("Change closed");

            if(item!=null){

                if (adapterList.getItemCount()!=0){

                    long chatHandleToRemove = item.getChatId();
                    int indexToRemove = -1;
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if(chat!=null){
                            if(chat.getChatId()==chatHandleToRemove){
                                indexToRemove = itrReplace.nextIndex()-1;
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    if(indexToRemove!=-1){
                        log("Index to replace: "+indexToRemove);
                        chats.remove(indexToRemove);

                        adapterList.removeChat(chats, indexToRemove);
                        adapterList.setPositionClicked(-1);

                        if (adapterList.getItemCount() == 0){
                            log("adapterList.getItemCount() == 0");
                            listView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                        else{
                            log("adapterList.getItemCount() NOT = 0");
                            listView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        else{
            log("Other change: "+item.getChanges());
            if(item!=null){
                log("New chat: "+item.getTitle());
                setChats();
                MegaChatRoom chatToCheck = megaChatApi.getChatRoom(item.getChatId());
                updateCacheForNonContacts(chatToCheck);
            }
            else{
                log("The chat is NULL");
            }
        }
    }

    public void findViewHolder(int position){
        View holder = mLayoutManager.findViewByPosition(position);

        TextView unreadCount = (TextView) holder.findViewById(R.id.recent_chat_list_unread_number);
        log("getTextUnread:  ------------------------------------------------"+unreadCount.getText());
        unreadCount.setText("16");
        log("getTextUnread:  ------------------------------------------------"+unreadCount.getText());
        unreadCount.invalidate();

//        myRecyclerView.findViewHolderForAdapterPosition(pos);
    }

    public String getParticipantFullName(MegaChatRoom chat, long i){
        String participantFirstName = chat.getPeerFirstname(i);
        String participantLastName = chat.getPeerLastname(i);

        if(participantFirstName==null){
            participantFirstName="";
        }
        if(participantLastName == null){
            participantLastName="";
        }

        if (participantFirstName.trim().length() <= 0){
            log("Participant1: "+participantFirstName);
            return participantLastName;
        }
        else{
            log("Participant2: "+participantLastName);
            return participantFirstName + " " + participantLastName;
        }
    }

    public void updateCacheForNonContacts(MegaChatRoom chatToCheck){
        if(chatToCheck!=null){
            long peers = chatToCheck.getPeerCount();
            for(int i=0; i<peers; i++){
//                    long peerHandle = chatToCheck.getPeerHandle(i);
                String fullName = getParticipantFullName(chatToCheck, i);
                if(fullName!=null){
                    if(fullName.trim().length()<=0){
                        log("Ask for name!");
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context);
                        megaChatApi.getUserFirstname(chatToCheck.getPeerHandle(i), listener);
                        megaChatApi.getUserLastname(chatToCheck.getPeerHandle(i), listener);
                    }
                    else{
                        log("Exists name!");
                    }
                }
                else{
                    log("Ask for name!");
                    ChatNonContactNameListener listener = new ChatNonContactNameListener(context);
                    megaChatApi.getUserFirstname(chatToCheck.getPeerHandle(i), listener);
                    megaChatApi.getUserLastname(chatToCheck.getPeerHandle(i), listener);
                }
            }
        }
    }

    @Override
    public void onChatRoomUpdate(MegaChatApiJava api, MegaChatRoom item) {
        log("onChatRoomUpdate");

//        MegaChatRoom chatUpdated = megaChatApi.getChatRoom(chat.getChatId());
//        if(chatUpdated!=null){
//            log("chat updated: "+chat.getTitle());
//            log("unread count: "+chat.getUnreadCount());
//            log("change type: "+chat.getChanges());
//        }

        if(item.hasChanged(MegaChatRoom.CHANGE_TYPE_UNREAD_COUNT)){
            log("CHANGE_TYPE_UNREAD_COUNT for the chat: "+item.getChatId());
            if(item!=null){

                if (adapterList == null || adapterList.getItemCount()==0){
                    setChats();
                }
                else{
                    long chatHandleToUpdate = item.getChatId();
                    int indexToReplace = -1;
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if(chat!=null){
                            if(chat.getChatId()==chatHandleToUpdate){
                                indexToReplace = itrReplace.nextIndex()-1;
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    if(indexToReplace!=-1){
                        log("Index to replace: "+indexToReplace);
                        MegaChatListItem chatToReplace = megaChatApi.getChatListItem(item.getChatId());
                        log("Unread count: "+chatToReplace.getUnreadCount());
                        chats.set(indexToReplace, chatToReplace);

                        adapterList.modifyChat(chats, indexToReplace);
                        adapterList.setPositionClicked(-1);

                        if (adapterList.getItemCount() == 0){
                            log("adapterList.getItemCount() == 0");
                            listView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                        else{
                            log("adapterList.getItemCount() NOT = 0");
                            listView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    private static void log(String log) {
        Util.log("RecentChatsFragmentLollipop", log);
    }
}
