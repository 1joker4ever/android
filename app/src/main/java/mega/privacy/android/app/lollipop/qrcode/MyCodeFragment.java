package mega.privacy.android.app.lollipop.qrcode;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaContactDB;
import mega.privacy.android.app.R;
import mega.privacy.android.app.UserCredentials;
import mega.privacy.android.app.components.RoundedImageView;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;

import static android.graphics.Color.WHITE;

/**
 * Created by mega on 22/01/18.
 */

public class MyCodeFragment extends Fragment implements View.OnClickListener, MegaRequestListenerInterface {

    public static int DEFAULT_AVATAR_WIDTH_HEIGHT = 150;
    public static int WIDTH = 500;

    MegaUser myUser;
    String myEmail;
    MegaApiAndroid megaApi;
    DatabaseHandler dbH = null;
    Handler handler;

    long handle;
    String contactLink = null;

    private ActionBar aB;

    private RelativeLayout relativeContainerQRCode;
    private RelativeLayout relativeQRCode;
    private ImageView avatarImage;
    private ImageView qrcode;
    private TextView qrcode_link;
    private Button qrcode_copy_link;
    private View v;

    private Context context;

    private Bitmap qrCodeBitmap;
    private File qrFile = null;

    public static ProgressDialog processingDialog;

    public static MyCodeFragment newInstance() {
        log("newInstance");
        MyCodeFragment fragment = new MyCodeFragment();
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        log("onCreate");

        super.onCreate(savedInstanceState);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        myEmail = megaApi.getMyUser().getEmail();
        myUser = megaApi.getMyUser();
        dbH = DatabaseHandler.getDbHandler(context);
        handler = new Handler();

        if (savedInstanceState != null){
            handle = savedInstanceState.getLong("handle");
            contactLink = savedInstanceState.getString("contactLink");
        }

    }

    public File queryIfQRExists (){
        log("queryIfQRExists");

        if (context.getExternalCacheDir() != null){
            qrFile = new File(context.getExternalCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
        }
        else{
            qrFile = new File(context.getCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
        }

        if (qrFile.exists()){
            return qrFile;
        }

        return null;
    }

    public void setImageQR (){
        log("setImageQR");

        if (qrFile.exists()) {
            if (qrFile.length() > 0) {
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                qrCodeBitmap = BitmapFactory.decodeFile(qrFile.getAbsolutePath(), bOpts);
                qrcode.setImageBitmap(qrCodeBitmap);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("handle", handle);
        outState.putString("contactLink", contactLink);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView");

        v = inflater.inflate(R.layout.fragment_mycode, container, false);

        if (aB == null){
            aB = ((AppCompatActivity)context).getSupportActionBar();
        }

        if(aB!=null){
            aB.setTitle(getString(R.string.section_qr_code));
            aB.setHomeButtonEnabled(true);
            aB.setDisplayHomeAsUpEnabled(true);
        }

        relativeContainerQRCode = (RelativeLayout) v.findViewById(R.id.qr_code_relative_container);
        relativeQRCode = (RelativeLayout) v.findViewById(R.id.qr_code_relative_layout_avatar);
        avatarImage = (ImageView) v.findViewById(R.id.qr_code_avatar);
        qrcode = (ImageView) v.findViewById(R.id.qr_code_image);
        qrcode_link = (TextView) v.findViewById(R.id.qr_code_link);
        qrcode_copy_link = (Button) v.findViewById(R.id.qr_code_button_copy_link);
        qrcode_copy_link.setEnabled(false);
        qrcode_copy_link.setOnClickListener(this);

        if (contactLink != null){
            qrcode_link.setText(contactLink);
            qrcode_copy_link.setEnabled(true);
        }

        Configuration configuration = getResources().getConfiguration();
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
        int top, bottom;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
        params.gravity = Gravity.CENTER;
        if(configuration.orientation==Configuration.ORIENTATION_LANDSCAPE){
            top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            params.setMargins(0, top, 0, bottom);
            relativeContainerQRCode.setLayoutParams(params);
            relativeContainerQRCode.setPadding(0,-80,0,0);

        }else{
            top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55, getResources().getDisplayMetrics());
            bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58, getResources().getDisplayMetrics());
            params.setMargins(0, top, 0, bottom);
            relativeContainerQRCode.setLayoutParams(params);
        }
        if (megaApi == null) {
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }
        qrFile = queryIfQRExists();
        if (qrFile != null && qrFile.exists()) {
            setImageQR();
            megaApi.contactLinkCreate(this);
        }
        else {
            megaApi.contactLinkCreate(this);
            ProgressDialog temp = null;
            try{
                temp = new ProgressDialog(context);
                temp.setMessage(getString(R.string.generatin_qr));
                temp.show();
            }
            catch(Exception e){
            }
            processingDialog = temp;
        }

        return v;
    }

    public Bitmap createQRCode (Bitmap qr, Bitmap avatar){
        log("createQRCode");

        Bitmap qrCode = Bitmap.createBitmap(WIDTH,WIDTH, Bitmap.Config.ARGB_8888);
        int width = 180;
        Canvas c = new Canvas(qrCode);
        int pos = (c.getWidth()/2) - (width/2);

        avatar = Bitmap.createScaledBitmap(avatar, width, width, false);
        c.drawBitmap(qr, 0f, 0f, null);
        c.drawBitmap(avatar, pos, pos, null);

        return qrCode;
    }

    public Bitmap queryQR () {
        log("queryQR");

        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BitMatrix bitMatrix = null;

        try {
            bitMatrix = new MultiFormatWriter().encode(contactLink, BarcodeFormat.QR_CODE, 40, 40, hints);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        int[] pixels = new int[w * h];
        int color = getResources().getColor(R.color.lollipop_primary_color);
        float resize = 12.2f;

        Bitmap bitmap = Bitmap.createBitmap(WIDTH, WIDTH, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(WHITE);
        c.drawRect(0, 0, WIDTH, WIDTH, paint);
        paint.setColor(color);

        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? color : WHITE;
                if (pixels[offset + x] == color){
                    c.drawCircle(x*resize, y*resize, 5, paint);
                }
                log("pixels[offset + x]: "+Integer.toString(pixels[offset + x])+ " offset+x: "+(offset+x));
            }
        }
        paint.setColor(WHITE);
        c.drawRect(3*resize, 3*resize, 11.5f*resize, 11.5f*resize, paint);
        c.drawRect(28.5f*resize, 3*resize, 37*resize, 11.5f*resize, paint);
        c.drawRect(3*resize, 28.5f*resize, 11.5f*resize, 37*resize, paint);

        paint.setColor(color);
        c.drawRoundRect(3.75f*resize, 3.75f*resize, 10.75f*resize, 10.75f*resize, 30, 30, paint);
        c.drawRoundRect(29.25f*resize, 3.75f*resize, 36.25f*resize, 10.75f*resize, 30, 30, paint);
        c.drawRoundRect(3.75f*resize, 29.25f*resize, 10.75f*resize, 36.25f*resize, 30, 30, paint);

        paint.setColor(WHITE);
        c.drawRoundRect(4.75f*resize, 4.75f*resize, 9.75f*resize, 9.75f*resize, 25, 25, paint);
        c.drawRoundRect(30.25f*resize, 4.75f*resize, 35.25f*resize, 9.75f*resize, 25, 25, paint);
        c.drawRoundRect(4.75f*resize, 30.25f*resize, 9.75f*resize, 35.25f*resize, 25, 25, paint);

        paint.setColor(color);
        c.drawCircle(7.25f*resize, 7.25f*resize, 17.5f, paint);
        c.drawCircle(32.75f*resize, 7.25f*resize, 17.5f, paint);
        c.drawCircle(7.25f*resize, 32.75f*resize, 17.5f, paint);

//        bitmap.setPixels(pixels, 0, w, 0, 0, w,  h);

        return bitmap;
    }

    public Bitmap setUserAvatar(){
        log("setUserAvatar");

        File avatar = null;
        if (context.getExternalCacheDir() != null){
            avatar = new File(context.getExternalCacheDir().getAbsolutePath(), myEmail + ".jpg");
        }
        else{
            avatar = new File(context.getCacheDir().getAbsolutePath(), myEmail + ".jpg");
        }
        Bitmap bitmap = null;
        if (avatar.exists()){
            if (avatar.length() > 0){
                BitmapFactory.Options bOpts = new BitmapFactory.Options();
                bOpts.inPurgeable = true;
                bOpts.inInputShareable = true;
                bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                if (bitmap == null) {
                    return createDefaultAvatar();
                }
                else{
                    return getCircleBitmap(bitmap);
                }
            }
            else{
                return createDefaultAvatar();
            }
        }
        else{
            return createDefaultAvatar();
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        log("getCircleBitmap");

        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public Bitmap createDefaultAvatar(){
        log("createDefaultAvatar()");

        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint paintText = new Paint();
        Paint paintCircle = new Paint();

        paintText.setColor(Color.WHITE);
        paintText.setTextSize(150);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);
        Typeface face = Typeface.SANS_SERIF;
        paintText.setTypeface(face);
        paintText.setAntiAlias(true);
        paintText.setSubpixelText(true);
        paintText.setStyle(Paint.Style.FILL);


        String color = megaApi.getUserAvatarColor(myUser);
        if(color!=null){
            log("The color to set the avatar is "+color);
            paintCircle.setColor(Color.parseColor(color));
            paintCircle.setAntiAlias(true);
        }
        else{
            log("Default color to the avatar");
            paintCircle.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
            paintCircle.setAntiAlias(true);
        }


        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius,paintCircle);

        UserCredentials credentials = dbH.getCredentials();
        String fullName = null;
        if(credentials!=null){
            fullName = credentials.getFirstName();
            if (fullName == null) {
                fullName = credentials.getLastName();
                if (fullName == null) {
                    fullName = myEmail;
                }
            }
        }
        else{
            //No name, ask for it and later refresh!!
            fullName = myEmail;
        }
        String firstLetter = fullName.charAt(0) + "";

        log("Draw letter: "+firstLetter);
        Rect bounds = new Rect();

        paintText.getTextBounds(firstLetter,0,firstLetter.length(),bounds);
        int xPos = (c.getWidth()/2);
        int yPos = (int)((c.getHeight()/2)-((paintText.descent()+paintText.ascent()/2))+20);
        c.drawText(firstLetter.toUpperCase(Locale.getDefault()), xPos, yPos, paintText);

        return defaultAvatar;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        log("onConfigurationChanged");
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            log("onConfigurationChanged: changed to LANDSCAPE");

        }else{
            log("onConfigurationChanged: changed to PORTRAIT");

        }
    }

    @Override
    public void onAttach(Activity activity) {
        log("onAttach");
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }

    @Override
    public void onAttach(Context context) {
        log("onAttach context");
        super.onAttach(context);
        this.context = context;
        aB = ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    private static void log(String log) {
        Util.log("MyCodeFragment", log);
    }

    @Override
    public void onClick(View v) {
        log("onClick");
        switch (v.getId()) {
            case R.id.qr_code_button_copy_link: {
                copyLink();
                break;
            }
        }
    }

    public void copyLink () {
        log("copyLink");

        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", contactLink);
        clipboardManager.setPrimaryClip(clip);
        showSnackbar(getString(R.string.qrcode_link_copied));
    }

    public void showSnackbar(String s){
        log("showSnackbar");
        Snackbar snackbar = Snackbar.make(v, s, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setMaxLines(5);
        snackbar.show();
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {

        if (request.getType() == MegaRequest.TYPE_CONTACT_LINK_CREATE) {
            if (e.getErrorCode() == MegaError.API_OK) {
                log("Contact link create LONG: " + request.getNodeHandle());
                log("Contact link create BASE64: " + "https://mega.nz/C!" + MegaApiAndroid.handleToBase64(request.getNodeHandle()));

                handle = request.getNodeHandle();
                contactLink = "https://mega.nz/C!" + MegaApiAndroid.handleToBase64(request.getNodeHandle());
                qrcode_link.setText(contactLink);
                qrCodeBitmap = createQRCode(queryQR(), setUserAvatar());
                File qrCodeFile = null;
                if (context.getExternalCacheDir() != null){
                    qrCodeFile = new File(context.getExternalCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
                }
                else{
                    qrCodeFile = new File(context.getCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
                }
                if (qrCodeFile != null && !qrCodeFile.exists()) {
                    try {
                        FileOutputStream out = new FileOutputStream(qrCodeFile);
                        qrCodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
                qrcode.setImageBitmap(qrCodeBitmap);
                qrcode_copy_link.setEnabled(true);
                if (processingDialog != null) {
                    processingDialog.dismiss();
                }
            }
        }

//        megaApi.contactLinkQuery(request.getNodeHandle(), this);
        if (request.getType() == MegaRequest.TYPE_CONTACT_LINK_QUERY){
            if (e.getErrorCode() == MegaError.API_OK){
                log("Contact link query " + request.getNodeHandle() + "_" + MegaApiAndroid.handleToBase64(request.getNodeHandle()) + "_" + request.getEmail() + "_" + request.getName() + "_" + request.getText());
            }
        }

//        megaApi.contactLinkDelete(request.getNodeHandle(), this);
        if (request.getType() == MegaRequest.TYPE_CONTACT_LINK_DELETE){
            if (e.getErrorCode() == MegaError.API_OK){
                log("Contact link delete:" + e.getErrorCode() + "_" + request.getNodeHandle() + "_"  + MegaApiAndroid.handleToBase64(request.getNodeHandle()));
                ((QRCodeActivity) context).resetSuccessfully(true);
                File qrCodeFile = null;
                if (context.getExternalCacheDir() != null){
                    qrCodeFile = new File(context.getExternalCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
                }
                else{
                    qrCodeFile = new File(context.getCacheDir().getAbsolutePath(), myEmail + "QRcode.jpg");
                }
                if (qrCodeFile != null && qrCodeFile.exists()){
                    qrCodeFile.delete();
                }
            }
            else {
                ((QRCodeActivity) context).resetSuccessfully(false);
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

    }

    public void resetQRCode () {
        log("resetQRCode");

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }
        megaApi.contactLinkDelete(handle, this);
        megaApi.contactLinkCreate(this);
    }
}
