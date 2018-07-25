package mega.privacy.android.app.lollipop.megachat.chatAdapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.lollipop.adapters.FileStorageLollipopAdapter;
import mega.privacy.android.app.lollipop.listeners.GroupCallListener;
import mega.privacy.android.app.lollipop.listeners.UserAvatarListener;
import mega.privacy.android.app.lollipop.megachat.calls.ChatCallActivity;
import mega.privacy.android.app.lollipop.megachat.calls.InfoPeerGroupCall;
import mega.privacy.android.app.lollipop.megachat.calls.LocalCameraCallFullScreenFragment;
import mega.privacy.android.app.lollipop.megachat.calls.MegaSurfaceRenderer;
import mega.privacy.android.app.lollipop.megachat.calls.RemoteCameraCallFullScreenFragment;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatVideoListenerInterface;
import nz.mega.sdk.MegaNode;

import static android.view.View.GONE;
import static mega.privacy.android.app.utils.Util.context;


public class GroupCallAdapter extends RecyclerView.Adapter<GroupCallAdapter.ViewHolderGroupCall> {

    public static final int ITEM_VIEW_TYPE_GRID = 1;

    Context context;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi = null;
    Display display;
    DisplayMetrics outMetrics;
    float density;
    float scaleW;
    float scaleH;
    float widthScreenPX, heightScreenPX;

    RecyclerView recyclerViewFragment;
    ArrayList<InfoPeerGroupCall> peers;
    long chatId;

    int width = 0;
    int height = 0;
    Bitmap bitmap;

    private int numberOfCells;
    private int gridWidth = 50;
    boolean avatarRequested = false;
    private int adapterType;


    public GroupCallAdapter(Context context, RecyclerView recyclerView, ArrayList<InfoPeerGroupCall> peers, long chatId, int adapterType) {
        log("GroupCallAdapter()");

        this.context = context;
        this.recyclerViewFragment = recyclerView;
        this.peers = peers;
        this.chatId = chatId;
        this.adapterType = adapterType;

        MegaApplication app = (MegaApplication) ((Activity) context).getApplication();
        if (megaApi == null) {
            megaApi = app.getMegaApi();
        }

        log("retryPendingConnections()");
        if (megaApi != null) {
            log("---------retryPendingConnections");
            megaApi.retryPendingConnections();
        }

        if (megaChatApi == null) {
            megaChatApi = app.getMegaChatApi();
        }
    }

    /*private view holder class*/
    public class ViewHolderGroupCall extends RecyclerView.ViewHolder{

        public RelativeLayout rLayout;
        public RelativeLayout avatarLayout;
        public RoundedImageView avatarImage;
        public TextView avatarInitialLetter;
        public SurfaceView localFullScreenSurfaceView;
        public MegaSurfaceRenderer localRenderer;

        public ViewHolderGroupCall(View itemView) {
            super(itemView);
        }

    }

    public class ViewHolderGroupCallGrid extends ViewHolderGroupCall{
        public ViewHolderGroupCallGrid(View v) {
            super(v);
        }

    }

    ViewHolderGroupCallGrid holderGrid = null;

    @Override public GroupCallAdapter.ViewHolderGroupCall onCreateViewHolder(ViewGroup parent, int viewType) {
        log("onCreateViewHolder()");

        display = ((Activity) context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        widthScreenPX = outMetrics.widthPixels;
        heightScreenPX = outMetrics.heightPixels;

        density = ((Activity) context).getResources().getDisplayMetrics().density;

        scaleW = Util.getScaleW(outMetrics, density);
        scaleH = Util.getScaleH(outMetrics, density);



        if (viewType == GroupCallAdapter.ITEM_VIEW_TYPE_GRID){
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera_group_call, parent, false);
//            GroupCallAdapter.ViewHolderGroupCall holder = new GroupCallAdapter.ViewHolderGroupCall(v);
//
//            holder.rLayout = (RelativeLayout) v.findViewById(R.id.rl);
//            holder.rLayout.setVisibility(View.VISIBLE);
//            holder.avatarLayout = (RelativeLayout) v.findViewById(R.id.avatar_rl);
//            holder.avatarImage = (RoundedImageView) v.findViewById(R.id.avatar_image);
//            holder.avatarInitialLetter = (TextView) v.findViewById(R.id.avatar_initial_letter);
//
//            holder.localFullScreenSurfaceView = (SurfaceView)v.findViewById(R.id.surface_local_video);
//            holder.localFullScreenSurfaceView.setZOrderMediaOverlay(true);
//            SurfaceHolder localSurfaceHolder =  holder.localFullScreenSurfaceView.getHolder();
//            localSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
//            holder.localRenderer = new MegaSurfaceRenderer( holder.localFullScreenSurfaceView);
//
//            v.setTag(holder);
//            return holder;

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera_group_call, parent, false);

            holderGrid = new ViewHolderGroupCallGrid(v);
            holderGrid.rLayout = (RelativeLayout) v.findViewById(R.id.rl);
            holderGrid.avatarLayout = (RelativeLayout) v.findViewById(R.id.avatar_rl);
            holderGrid.avatarImage = (RoundedImageView) v.findViewById(R.id.avatar_image);
            holderGrid.avatarInitialLetter = (TextView) v.findViewById(R.id.avatar_initial_letter);
            holderGrid.localFullScreenSurfaceView = (SurfaceView)v.findViewById(R.id.surface_local_video);
            holderGrid.localFullScreenSurfaceView.setZOrderMediaOverlay(true);
            SurfaceHolder localSurfaceHolder =  holderGrid.localFullScreenSurfaceView.getHolder();
            localSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            holderGrid.localRenderer = new MegaSurfaceRenderer( holderGrid.localFullScreenSurfaceView);

            v.setTag(holderGrid);
            return holderGrid;

        }else{
            return null;

        }
    }

    @Override
    public int getItemViewType(int position) {
        return adapterType;
    }

    @Override
    public void onBindViewHolder(ViewHolderGroupCall holder, int position) {
        log("onBindViewHolder");

        if (adapterType == GroupCallAdapter.ITEM_VIEW_TYPE_GRID){
            ViewHolderGroupCallGrid holderGrid = (ViewHolderGroupCallGrid) holder;
            onBindViewHolderGrid(holderGrid, position);
        }

    }

//    @Override
//    public void onBindViewHolder(ViewHolderGroupCall holder, int position) {
public void onBindViewHolderGrid (ViewHolderGroupCallGrid holder, int position){

    log("onBindViewHolderGrid()");

        InfoPeerGroupCall peer = getNodeAt(position);
        if (peer == null){
            return;
        }
        log("Peer in position: "+position+", handle("+peer.getHandle()+"), name("+peer.getName()+"), videoOn("+peer.isVideoOn()+"), audioOn("+peer.isAudioOn()+")");
        if(peer.isVideoOn()){
            log("video on");
            this.width=0;
            this.height=0;
            GroupCallListener listenerPeer = new GroupCallListener(context,holder);
            peer.setListener(listenerPeer);
            megaChatApi.addChatLocalVideoListener(chatId, listenerPeer);
            holder.avatarLayout.setVisibility(GONE);
        }else{
            log("video off");

            holder.avatarLayout.setVisibility(View.VISIBLE);
            if(peer.getHandle().equals(megaChatApi.getMyUserHandle())) {
                setProfileMyAvatar(holder);
            }else{
                setProfileContactAvatar(peer.getHandle(), peer.getName(), holder);
            }
        }

    }

    public void setNodes(ArrayList<InfoPeerGroupCall> peers) {
        log("setNodes() -> peers: "+peers.size());
        this.peers = peers;
        for(int i=0; i<peers.size();i++){
log("handle "+peers.get(i).getHandle());
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        if (peers != null){
            log("getItemCount() -> "+peers.size());

            return peers.size();
        }else{
            log("getItemCount() -> 0");

            return 0;
        }

    }

    public Object getItem(int position) {
        log("getItem()");

        if (peers != null){
            return peers.get(position);
        }

        return null;
    }


    public InfoPeerGroupCall getNodeAt(int position) {
        log("getNodeAt() position-> "+position);

        try {
            if (peers != null) {
                return peers.get(position);
            }
        } catch (IndexOutOfBoundsException e) {
        }
        return null;
    }


   //My AVATAR
   public void setProfileMyAvatar(ViewHolderGroupCall holder) {
       log("setProfileMyAvatar()");

       Bitmap myBitmap = null;
       File avatar = null;
       if (context != null) {
           log("context is not null");
           if (context.getExternalCacheDir() != null) {
               avatar = new File(context.getExternalCacheDir().getAbsolutePath(), megaChatApi.getMyEmail() + ".jpg");
           } else {
               avatar = new File(context.getCacheDir().getAbsolutePath(), megaChatApi.getMyEmail() + ".jpg");
           }
       }
       if (avatar.exists()) {
           if (avatar.length() > 0) {
               BitmapFactory.Options bOpts = new BitmapFactory.Options();
               bOpts.inPurgeable = true;
               bOpts.inInputShareable = true;
               myBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
               myBitmap = ThumbnailUtilsLollipop.getRoundedRectBitmap(context, myBitmap, 3);
               if (myBitmap != null) {
                       holder.avatarImage.setImageBitmap(myBitmap);
                       holder.avatarInitialLetter.setVisibility(GONE);
               }
               else{
                   createMyDefaultAvatar(holder);
               }
           }
           else {
               createMyDefaultAvatar(holder);
           }
       } else {
           createMyDefaultAvatar(holder);
       }
   }

    public void createMyDefaultAvatar(ViewHolderGroupCall holder) {
        log("createMyDefaultAvatar()");

        String myFullName = megaChatApi.getMyFullname();
        String myFirstLetter=myFullName.charAt(0) + "";
        myFirstLetter = myFirstLetter.toUpperCase(Locale.getDefault());
        long userHandle = megaChatApi.getMyUserHandle();

        Bitmap defaultAvatar = Bitmap.createBitmap(outMetrics.widthPixels,outMetrics.widthPixels, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.TRANSPARENT);

        String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(userHandle));

        if(color!=null){
            log("The color to set the avatar is "+color);
            p.setColor(Color.parseColor(color));
        }
        else{
            log("Default color to the avatar");
            p.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
        }

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        holder.avatarImage.setImageBitmap(defaultAvatar);
        holder.avatarInitialLetter.setText(myFirstLetter);
        holder.avatarInitialLetter.setTextSize(40);
        holder.avatarInitialLetter.setTextColor(Color.WHITE);
        holder.avatarInitialLetter.setVisibility(View.VISIBLE);

    }


    public void setProfileContactAvatar(long userHandle,  String fullName, ViewHolderGroupCall holder){
        log("setProfileContactAvatar");
        Bitmap bitmap = null;
        File avatar = null;
        String contactMail = megaChatApi.getContactEmail(userHandle);
        if (context.getExternalCacheDir() != null) {
            avatar = new File(context.getExternalCacheDir().getAbsolutePath(), contactMail + ".jpg");
        } else {
            avatar = new File(context.getCacheDir().getAbsolutePath(), contactMail + ".jpg");
        }

        if (avatar.exists()) {
            if (avatar.length() > 0) {
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                bitmap = ThumbnailUtilsLollipop.getRoundedRectBitmap(context, bitmap, 3);
                if (bitmap != null) {
                    holder.avatarImage.setVisibility(View.VISIBLE);
                    holder.avatarImage.setImageBitmap(bitmap);
                    holder.avatarInitialLetter.setVisibility(GONE);
                }else{
                    UserAvatarListener listener = new UserAvatarListener(context);
                    avatar.delete();
                    if(!avatarRequested){
                        avatarRequested = true;
                        if (context.getExternalCacheDir() != null){
                            megaApi.getUserAvatar(contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                        }
                        else{
                            megaApi.getUserAvatar(contactMail, context.getCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                        }
                    }

                    createDefaultAvatar(userHandle, fullName, holder);
                }
            }
            else{
                UserAvatarListener listener = new UserAvatarListener(context);

                if(!avatarRequested){
                    avatarRequested = true;
                    if (context.getExternalCacheDir() != null){
                        megaApi.getUserAvatar(contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                    }
                    else{
                        megaApi.getUserAvatar(contactMail, context.getCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                    }
                }

                createDefaultAvatar(userHandle, fullName, holder);
            }
        }
        else{
            UserAvatarListener listener = new UserAvatarListener(context);

            if(!avatarRequested){
                avatarRequested = true;
                if (context.getExternalCacheDir() != null){
                    megaApi.getUserAvatar(contactMail, context.getExternalCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                }
                else{
                    megaApi.getUserAvatar(contactMail, context.getCacheDir().getAbsolutePath() + "/" + contactMail + ".jpg", listener);
                }
            }

            createDefaultAvatar(userHandle, fullName, holder);
        }
    }

    public void createDefaultAvatar(long userHandle,  String fullName, ViewHolderGroupCall holder) {
        log("createDefaultAvatar");

        Bitmap defaultAvatar = Bitmap.createBitmap(outMetrics.widthPixels, outMetrics.widthPixels, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.TRANSPARENT);

        String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(userHandle));
        if (color != null) {
            log("The color to set the avatar is " + color);
            p.setColor(Color.parseColor(color));
        } else {
            log("Default color to the avatar");
            p.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
        }

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight()) {
            radius = defaultAvatar.getWidth() / 2;
        }else {
            radius = defaultAvatar.getHeight() / 2;
        }
        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
        holder.avatarImage.setVisibility(View.VISIBLE);
        holder.avatarImage.setImageBitmap(defaultAvatar);
        String contactFirstLetter = fullName.charAt(0) + "";
        contactFirstLetter = contactFirstLetter.toUpperCase(Locale.getDefault());
        holder.avatarInitialLetter.setText(contactFirstLetter);
        holder.avatarInitialLetter.setTextSize(60);
        holder.avatarInitialLetter.setTextColor(Color.WHITE);
        holder.avatarInitialLetter.setVisibility(View.VISIBLE);

    }

    public void setAdapterType(int adapterType){
        this.adapterType = adapterType;
    }
    private static void log(String log) {
        Util.log("GroupCallAdapter", log);
    }

}
