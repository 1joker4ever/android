package mega.privacy.android.app.lollipop.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.AndroidRuntimeException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import mega.privacy.android.app.CameraSyncService;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.DownloadService;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.R;
import mega.privacy.android.app.UploadService;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.MyAccountFragmentLollipop;
import mega.privacy.android.app.lollipop.TourActivityLollipop;
import mega.privacy.android.app.utils.PreviewUtils;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;

public class AccountController {

    Context context;
    MegaApiAndroid megaApi;
    DatabaseHandler dbH;
    MegaPreferences prefs = null;

    static int count = 0;

    public AccountController(Context context){
        log("AccountController created");
        this.context = context;
        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if (dbH == null){
            dbH = DatabaseHandler.getDbHandler(context);
        }
    }

    public void deleteAccount(MyAccountFragmentLollipop mAF){
        log("deleteAccount");
        megaApi.cancelAccount(mAF);
    }

    public void confirmDeleteAccount(String link, String pass, MyAccountFragmentLollipop mAF){
        log("confirmDeleteAccount");
        megaApi.confirmCancelAccount(link, pass, mAF);
    }

    public void confirmChangeMail(String link, String pass, MyAccountFragmentLollipop mAF){
        log("confirmChangeMail");
        megaApi.confirmChangeEmail(link, pass, mAF);
    }

    public void exportMK(){
        log("exportMK");
        if (!Util.isOnline(context)){
            ((ManagerActivityLollipop) context).showSnackbar(context.getString(R.string.error_server_connection_problem));
            return;
        }

        String key = megaApi.exportMasterKey();

        BufferedWriter out;
        try {

            final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MEGA/MEGAMasterKey.txt";
            final File f = new File(path);
            log("Export in: "+path);
            FileWriter fileWriter= new FileWriter(path);
            out = new BufferedWriter(fileWriter);
            out.write(key);
            out.close();
            String message = context.getString(R.string.toast_master_key, path);
            ((ManagerActivityLollipop) context).invalidateOptionsMenu();
            MyAccountFragmentLollipop mAF = ((ManagerActivityLollipop) context).getMyAccountFragment();
            if(mAF!=null){
                mAF.updateMKButton();
            }
            Util.showAlert(((ManagerActivityLollipop) context), message, "MasterKey exported!");

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyMK(){
        log("copyMK");
        String key = megaApi.exportMasterKey();
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", key);
        clipboard.setPrimaryClip(clip);
        Util.showAlert(((ManagerActivityLollipop) context), context.getString(R.string.copy_MK_confirmation), null);
    }

    public void removeMK() {
        log("removeMK");
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MEGA/MEGAMasterKey.txt";
        final File f = new File(path);
        f.delete();
        String message = context.getString(R.string.toast_master_key_removed);
        ((ManagerActivityLollipop) context).invalidateOptionsMenu();
        MyAccountFragmentLollipop mAF = ((ManagerActivityLollipop) context).getMyAccountFragment();
        if(mAF!=null){
            mAF.updateMKButton();
        }
        Util.showAlert(((ManagerActivityLollipop) context), message, null);
    }

    public void killAllSessions(Context context){
        log("killAllSessions");
        megaApi.killSession(-1, (ManagerActivityLollipop) context);
    }

    static public void logout(Context context, MegaApiAndroid megaApi, boolean confirmAccount) {
        log("logout");
        logout(context, megaApi, confirmAccount, false);
    }


    static public void logout(Context context, MegaApiAndroid megaApi, boolean confirmAccount, boolean logoutBadSession) {
        log("logout");

        File offlineDirectory = null;
        if (Environment.getExternalStorageDirectory() != null){
            offlineDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.offlineDIR);
        }
        else{
            offlineDirectory = context.getFilesDir();
        }

        try {
            Util.deleteFolderAndSubfolders(context, offlineDirectory);
        } catch (IOException e) {}

        File thumbDir = ThumbnailUtils.getThumbFolder(context);
        File previewDir = PreviewUtils.getPreviewFolder(context);

        try {
            Util.deleteFolderAndSubfolders(context, thumbDir);
        } catch (IOException e) {}

        try {
            Util.deleteFolderAndSubfolders(context, previewDir);
        } catch (IOException e) {}

        File externalCacheDir = context.getExternalCacheDir();
        File cacheDir = context.getCacheDir();
        try {
            Util.deleteFolderAndSubfolders(context, externalCacheDir);
        } catch (IOException e) {}

        try {
            Util.deleteFolderAndSubfolders(context, cacheDir);
        } catch (IOException e) {}

        final String pathMK = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MEGA/MEGAMasterKey.txt";
        final File fMK = new File(pathMK);
        if (fMK.exists()){
            fMK.delete();
        }

        PackageManager m = context.getPackageManager();
        String s = context.getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            log("Error Package name not found " + e);
        }

        File appDir = new File(s);

        for (File c : appDir.listFiles()){
            if (c.isFile()){
                c.delete();
            }
        }

        Intent cancelTransfersIntent = new Intent(context, DownloadService.class);
        cancelTransfersIntent.setAction(DownloadService.ACTION_CANCEL);
        context.startService(cancelTransfersIntent);
        cancelTransfersIntent = new Intent(context, UploadService.class);
        cancelTransfersIntent.setAction(UploadService.ACTION_CANCEL);
        context.startService(cancelTransfersIntent);

        DatabaseHandler dbH = DatabaseHandler.getDbHandler(context);
        dbH.clearCredentials();

        if (dbH.getPreferences() != null){
            dbH.clearPreferences();
            dbH.setFirstTime(false);
            Intent stopIntent = null;
            stopIntent = new Intent(context, CameraSyncService.class);
            stopIntent.setAction(CameraSyncService.ACTION_LOGOUT);
            context.startService(stopIntent);
        }
        dbH.clearOffline();

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if (!logoutBadSession){
            megaApi.logout();
        }

        if (!confirmAccount){
            if(context != null)	{
                Intent intent = new Intent(((ManagerActivityLollipop)context), TourActivityLollipop.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ((ManagerActivityLollipop)context).startActivity(intent);
                ((ManagerActivityLollipop)context).finish();
                context = null;
            }
            else{
                Intent intent = new Intent (context, TourActivityLollipop.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                try{
                    context.startActivity(intent);
                }
                catch (AndroidRuntimeException e){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                if (context instanceof Activity){
                    ((Activity)context).finish();
                }
                context = null;
            }
        }
        else{
            if (context != null){
                ((ManagerActivityLollipop)context).finish();
            }
            else{
                ((Activity)context).finish();
            }
        }
    }

    public int updateUserAttributes(String oldFirstName, String newFirstName, String oldLastName, String newLastName, String oldMail, String newMail){
        log("updateUserAttributes");
        MyAccountFragmentLollipop myAccountFragmentLollipop = ((ManagerActivityLollipop)context).getMyAccountFragment();
        if(!oldFirstName.equals(newFirstName)){
            log("Changes in first name");
            if(myAccountFragmentLollipop!=null){
                count++;
                megaApi.setUserAttribute(MegaApiJava.USER_ATTR_FIRSTNAME, newFirstName, myAccountFragmentLollipop);
            }
        }
        if(!oldLastName.equals(newLastName)){
            log("Changes in last name");
            if(myAccountFragmentLollipop!=null){
                count++;
                megaApi.setUserAttribute(MegaApiJava.USER_ATTR_LASTNAME, newLastName, myAccountFragmentLollipop);
            }
        }
        if(!oldMail.equals(newMail)){
            log("Changes in mail, new mail: "+newMail);
            megaApi.changeEmail(newMail, myAccountFragmentLollipop);
        }
        log("The number of attributes to change is: "+count);
        return count;
    }

    public int getCount() {
        return count;
    }

    static public void setCount(int countUa) {
        count = countUa;
    }

    public static void log(String message) {
        Util.log("AccountController", message);
    }
}
