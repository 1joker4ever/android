package mega.privacy.android.app.lollipop.listeners;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.BigGroupCallAdapter;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.GroupCallAdapter;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatVideoListenerInterface;

public class BigGroupCallListener implements MegaChatVideoListenerInterface {

    Context context;
    BigGroupCallAdapter.ViewHolderGroupCall holder;

    int width;
    int height;
    Bitmap bitmap;

    public BigGroupCallListener(Context context, BigGroupCallAdapter.ViewHolderGroupCall holder) {
        log("BigGroupCallListener");
        this.context = context;
        this.holder = holder;
        this.width = 0;
        this.height = 0;
    }

    @Override
    public void onChatVideoData(MegaChatApiJava api, long chatid, int width, int height, byte[] byteBuffer) {
        if((width == 0) || (height == 0)){
            return;
        }

        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;


            SurfaceHolder Sholder = holder.surfaceView.getHolder();
            if (Sholder != null) {
                int viewWidth = holder.surfaceView.getWidth();
                int viewHeight = holder.surfaceView.getHeight();

                if ((viewWidth != 0) && (viewHeight != 0)) {
                    int holderWidth = viewWidth < width ? viewWidth : width;
                    int holderHeight = holderWidth * viewHeight / viewWidth;
                    if (holderHeight > viewHeight) {

                        holderHeight = viewHeight;
                        holderWidth = holderHeight * viewWidth / viewHeight;
                    }
                    this.bitmap = holder.localRenderer.CreateBitmap(width, height);
                    Sholder.setFixedSize(holderWidth, holderHeight);
                }else{
                    this.width = -1;
                    this.height = -1;
                }
            }
        }

        if (bitmap != null) {
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(byteBuffer));

            // Instead of using this WebRTC renderer, we should probably draw the image by ourselves.
            // The renderer has been modified a bit and an update of WebRTC could break our app
            holder.localRenderer.DrawBitmap(false);
        }
    }


    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private static void log(String log) {
        Util.log("GroupCallListener", log);
    }

}

