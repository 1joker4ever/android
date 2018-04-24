package mega.privacy.android.app.lollipop.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaOffline;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaNode;

/**
 * Created by mega on 24/04/18.
 */

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolderBrowser> implements View.OnClickListener {

    MegaApiAndroid megaApi;

    Context context;
    ArrayList<Long> handles;
    ArrayList<MegaNode> nodes;
    ArrayList<MegaOffline> offNodes;
    long parentHandle;
    String parentPath;
    RecyclerView recyclerView;


    public static class ViewHolderBrowser extends RecyclerView.ViewHolder {

        public ViewHolderBrowser(View v) {
            super(v);
        }

        public ImageView imageView;
        public TextView textViewFileName;
        public TextView textViewFileSize;
        public TextView textViewState;
        public long document;
        public RelativeLayout itemLayout;
    }

    public PlayListAdapter(Context _context, ArrayList<Long> _handles, long _parentHandle, RecyclerView _recyclerView) {

        this.context = _context;
        this.handles = _handles;
        this.parentHandle = _parentHandle;
        this.recyclerView = _recyclerView;

        if (megaApi == null) {
            megaApi = ((MegaApplication) ((Activity) context).getApplication()).getMegaApi();
        }
    }

    public PlayListAdapter(Context _context, ArrayList<MegaOffline> _handles, String _parentPath, RecyclerView _recyclerView) {

        this.context = _context;
        this.offNodes = _handles;
        this.parentPath = _parentPath;
        this.recyclerView = _recyclerView;

        if (megaApi == null) {
            megaApi = ((MegaApplication) ((Activity) context).getApplication()).getMegaApi();
        }
    }

    public void setOffNodes(ArrayList<MegaOffline> nodes) {
        log("setOffNodes");
        this.offNodes = nodes;
        notifyDataSetChanged();
    }

    public void setNodes(ArrayList<MegaNode> nodes) {
        log("setNodes");
        this.nodes = nodes;
        notifyDataSetChanged();
    }



    @Override
    public PlayListAdapter.ViewHolderBrowser onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_playlist, parent, false);
        PlayListAdapter.ViewHolderBrowser holderList = new PlayListAdapter.ViewHolderBrowser(v);

        holderList.itemLayout = (RelativeLayout) v.findViewById(R.id.file_list_item_layout);
        holderList.imageView = (ImageView) v.findViewById(R.id.file_list_thumbnail);
        holderList.textViewFileName = (TextView) v.findViewById(R.id.file_list_filename);
        holderList.textViewFileSize = (TextView) v.findViewById(R.id.file_list_filesize);
        holderList.textViewState = (TextView) v.findViewById(R.id.file_list_filestate);

        return holderList;
    }

    @Override
    public void onBindViewHolder(PlayListAdapter.ViewHolderBrowser holder, int position) {

        MegaNode node = (MegaNode) getItem(position);
        holder.document = node.getHandle();
        Bitmap thumb = null;

        holder.textViewFileName.setText(node.getName());
        holder.textViewFileSize.setText(Util.getSizeString(node.getSize()));

        holder.itemLayout.setBackgroundColor(Color.WHITE);
        holder.imageView.setImageResource(MimeTypeList.typeForName(node.getName()).getIconResourceId());

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
        params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, context.getResources().getDisplayMetrics());
        params.setMargins(0, 0, 0, 0);
        holder.imageView.setLayoutParams(params);

        log("Check the thumb");

        if (node.hasThumbnail()) {
            log("Node has thumbnail");
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
            params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
            params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
            int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
            params1.setMargins(left, 0, 0, 0);

            holder.imageView.setLayoutParams(params1);

            thumb = ThumbnailUtils.getThumbnailFromCache(node);
            if (thumb != null) {

                holder.imageView.setImageBitmap(thumb);

            } else {
                thumb = ThumbnailUtils
                        .getThumbnailFromFolder(node, context);
                if (thumb != null) {
                    holder.imageView.setImageBitmap(thumb);

                } else {
                    try {
                        thumb = ThumbnailUtilsLollipop.getThumbnailFromMegaList(node, context, holder, megaApi, this);
                    } catch (Exception e) {
                    } // Too many AsyncTasks

                    if (thumb != null) {
                        holder.imageView.setImageBitmap(thumb);
                    }
                }
            }
        } else {
            log("Node NOT thumbnail");
            thumb = ThumbnailUtils.getThumbnailFromCache(node);
            if (thumb != null) {
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
                params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
                params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
                int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
                params1.setMargins(left, 0, 0, 0);

                holder.imageView.setLayoutParams(params1);
                holder.imageView.setImageBitmap(thumb);


            } else {
                thumb = ThumbnailUtils.getThumbnailFromFolder(node, context);
                if (thumb != null) {
                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.imageView.getLayoutParams();
                    params1.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
                    params1.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());
                    int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics());
                    params1.setMargins(left, 0, 0, 0);

                    holder.imageView.setLayoutParams(params1);
                    holder.imageView.setImageBitmap(thumb);

                } else {
                    try {
                        ThumbnailUtilsLollipop.createThumbnailList(context, node,holder, megaApi, this);
                    } catch (Exception e) {
                    } // Too many AsyncTasks
                }
            }
        }
    }

    public Object getItem(int position) {
        if (nodes != null){
            return nodes.get(position);
        }

        return null;
    }

    @Override
    public int getItemCount() {
        if (nodes != null){
            return nodes.size();
        }else{
            return 0;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

        }
    }

    private static void log(String log) {
        Util.log("PlayListAdapter", log);
    }
}
