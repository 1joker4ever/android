package mega.privacy.android.app.lollipop.managerSections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.ChatDividerItemDecoration;
import mega.privacy.android.app.components.scrollBar.FastScroller;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaNotificationsAdapter;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.lollipop.megachat.ArchivedChatsActivity;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaListChatLollipopAdapter;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaUserAlert;

import static mega.privacy.android.app.utils.Util.adjustForLargeFont;

public class NotificationsFragmentLollipop extends Fragment implements View.OnClickListener {

    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";

    MegaApiAndroid megaApi;

    DatabaseHandler dbH;

    Context context;
    ActionBar aB;
    MegaNotificationsAdapter adapterList;
    RelativeLayout mainRelativeLayout;

    RecyclerView listView;
    LinearLayoutManager mLayoutManager;
    FastScroller fastScroller;

    ArrayList<MegaUserAlert> notifications;

    int lastFirstVisiblePosition;

    int numberOfClicks = 0;

    //Empty screen
    TextView emptyTextView;
    LinearLayout emptyLayout;
    TextView emptyTextViewInvite;
    ImageView emptyImageView;

//    boolean chatEnabled = true;
    float density;
    DisplayMetrics outMetrics;
    Display display;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        dbH = DatabaseHandler.getDbHandler(getActivity());
    }

    public void checkScroll() {
        if (listView != null) {
            if (listView.canScrollVertically(-1)) {
                ((ManagerActivityLollipop) context).changeActionBarElevation(true);
            }
            else {
                ((ManagerActivityLollipop) context).changeActionBarElevation(false);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView");

        display = ((Activity) context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = getResources().getDisplayMetrics().density;

        View v = inflater.inflate(R.layout.notifications_fragment, container, false);

        listView = (RecyclerView) v.findViewById(R.id.notifications_list_view);
        fastScroller = (FastScroller) v.findViewById(R.id.fastscroll_notifications);

        listView.setClipToPadding(false);
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);
        listView.setLayoutManager(mLayoutManager);
        listView.setHasFixedSize(true);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (context instanceof ManagerActivityLollipop) {
                    checkScroll();
                }
            }
        });
//        listView.setClipToPadding(false);

        emptyLayout = (LinearLayout) v.findViewById(R.id.linear_empty_layout_notifications);
        emptyTextViewInvite = (TextView) v.findViewById(R.id.empty_text_notifications_invite);
        emptyTextViewInvite.setWidth(Util.scaleWidthPx(236, outMetrics));
        emptyTextView = (TextView) v.findViewById(R.id.empty_text_notifications);

        LinearLayout.LayoutParams emptyTextViewParams1 = (LinearLayout.LayoutParams)emptyTextViewInvite.getLayoutParams();
        emptyTextViewParams1.setMargins(0, Util.scaleHeightPx(50, outMetrics), 0, Util.scaleHeightPx(24, outMetrics));
        emptyTextViewInvite.setLayoutParams(emptyTextViewParams1);

        LinearLayout.LayoutParams emptyTextViewParams2 = (LinearLayout.LayoutParams)emptyTextView.getLayoutParams();
        emptyTextViewParams2.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics));
        emptyTextView.setLayoutParams(emptyTextViewParams2);

        emptyImageView = (ImageView) v.findViewById(R.id.empty_image_view_recent);
        emptyImageView.setOnClickListener(this);

        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            emptyImageView.setImageResource(R.drawable.chat_empty_landscape);
        }else{
            emptyImageView.setImageResource(R.drawable.ic_empty_chat_list);
        }

        mainRelativeLayout = (RelativeLayout) v.findViewById(R.id.main_relative_layout_notifications);

        notifications = megaApi.getUserAlerts();

        if (adapterList == null){
            log("adapterList is NULL");
            adapterList = new MegaNotificationsAdapter(context, this, notifications, listView);
        }
        else{
            adapterList.setNotifications(notifications);
        }

        listView.setAdapter(adapterList);

        setNotifications();

        return v;
    }

    public static NotificationsFragmentLollipop newInstance() {
        log("newInstance");
        NotificationsFragmentLollipop fragment = new NotificationsFragmentLollipop();
        return fragment;
    }

    public void setNotifications(){
        log("setNotifications");

        if(isAdded()) {
            notifications = megaApi.getUserAlerts();

            if (adapterList == null){
                log("adapterList is NULL");
                adapterList = new MegaNotificationsAdapter(context, this, notifications, listView);
            }
            else{
                adapterList.setNotifications(notifications);
            }

            if (notifications == null || notifications.isEmpty()) {
                String textToShow = String.format(context.getString(R.string.context_empty_chat_recent));

                try{
                    textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                    textToShow = textToShow.replace("[/A]", "</font>");
                    textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                    textToShow = textToShow.replace("[/B]", "</font>");
                }
                catch (Exception e){}
                Spanned result = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }

                emptyTextViewInvite.setText(result);
                emptyTextViewInvite.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);

            } else {
                log("number of notifications: "+notifications.size());
                listView.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        log("onClick");

        switch (v.getId()) {
            case R.id.empty_image_view_chat:{
                numberOfClicks++;
                log("Number of clicks: "+numberOfClicks);
                if (numberOfClicks >= 5){
                    numberOfClicks = 0;

                }

                break;
            }
        }
    }

    public void itemClick(int position) {
        log("itemClick: "+position);


    }

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

    public void updateNotifications() {
        log("updateNotifications: ");

        if(!isAdded()){
            log("return!");
            return;
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if(listView.getLayoutManager()!=null){
            outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, listView.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public void onPause() {
        log("onPause");
        lastFirstVisiblePosition = ((LinearLayoutManager)listView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

        super.onPause();
    }

    @Override
    public void onResume() {
        log("onResume: lastFirstVisiblePosition " +lastFirstVisiblePosition);
        if(lastFirstVisiblePosition>0){
            (listView.getLayoutManager()).scrollToPosition(lastFirstVisiblePosition);
        }else{
            (listView.getLayoutManager()).scrollToPosition(0);
        }
        lastFirstVisiblePosition=0;

        super.onResume();
    }

    public int getItemCount(){
        if(adapterList != null){
            return adapterList.getItemCount();
        }
        return 0;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        log("onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            listView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    private static void log(String log) {
        Util.log("NotificationsFragmentLollipop", log);
    }
}