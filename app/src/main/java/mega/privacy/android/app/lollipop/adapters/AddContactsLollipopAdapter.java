package mega.privacy.android.app.lollipop.adapters;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.AddContactActivityLollipop;
import mega.privacy.android.app.lollipop.PhoneContactInfo;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;

/**
 * Created by mega on 22/11/17.
 */

public class AddContactsLollipopAdapter extends RecyclerView.Adapter<AddContactsLollipopAdapter.ViewHolderChips> implements View.OnClickListener{

    private int positionClicked;
    ArrayList<PhoneContactInfo> contacts;
    //private RecyclerView listFragment;
    private MegaApiAndroid megaApi;
    private AddContactActivityLollipop activity;

    public AddContactsLollipopAdapter (AddContactActivityLollipop _activity, ArrayList<PhoneContactInfo> _contacts){
        this.contacts = _contacts;
        this.activity = _activity;
        this.positionClicked = -1;

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)activity).getApplication()).getMegaApi();
        }
    }


    public static class ViewHolderChips extends RecyclerView.ViewHolder{
        public ViewHolderChips(View itemView) {
            super(itemView);
        }

        TextView textViewName;
        ImageView deleteIcon;
        RelativeLayout itemLayout;

    }

    ViewHolderChips holderList = null;

    @Override
    public AddContactsLollipopAdapter.ViewHolderChips onCreateViewHolder(ViewGroup parent, int viewType) {
        log("onCreateViewHolder");

        Display display = ((Activity)activity).getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chip, parent, false);

        holderList = new ViewHolderChips(v);
        holderList.itemLayout = (RelativeLayout) v.findViewById(R.id.item_layout_chip);

        holderList.textViewName = (TextView) v.findViewById(R.id.name_chip);
        if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            log("Landscape");
            holderList.textViewName.setMaxWidth(Util.scaleWidthPx(260, outMetrics));
        }else{
            log("Portrait");
            holderList.textViewName.setMaxWidth(Util.scaleWidthPx(230, outMetrics));
        }

        holderList.deleteIcon = (ImageView) v.findViewById(R.id.delete_icon_chip);
        holderList.deleteIcon.setOnClickListener(this);

        holderList.deleteIcon.setTag(holderList);

        v.setTag(holderList);

        return holderList;
    }

    @Override
    public void onBindViewHolder(AddContactsLollipopAdapter.ViewHolderChips holder, int position) {
        log("onBindViewHolderList");

        PhoneContactInfo contact = (PhoneContactInfo) getItem(position);
        if (contact.getName() != null){
            holder.textViewName.setText(contact.getName());
        }
        else {
            holder.textViewName.setText(contact.getEmail());
        }
    }

    @Override
    public int getItemCount() {
        return  contacts.size();
    }

    @Override
    public void onClick(View view) {
        log("onClick");

        AddContactsLollipopAdapter.ViewHolderChips holder = (AddContactsLollipopAdapter.ViewHolderChips) view.getTag();
        if(holder!=null){
            int currentPosition = holder.getLayoutPosition();
            log("onClick -> Current position: "+currentPosition);

            if(currentPosition<0){
                log("Current position error - not valid value");
                return;
            }
            switch (view.getId()) {
                case R.id.delete_icon_chip: {
                    activity.deleteContact(currentPosition);
                    break;
                }
            }
        }
        else{
            log("Error. Holder is Null");
        }
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

    public void setContacts (ArrayList<PhoneContactInfo> contacts){
        log("setContacts");
        this.contacts = contacts;

        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        log("getItem");
        return contacts.get(position);
    }

    private static void log(String log) {
        Util.log("AddContactsLollipopAdapter", log);
    }

}
