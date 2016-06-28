package mega.privacy.android.app.lollipop;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaAccountDetails;
import nz.mega.sdk.MegaAccountSession;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;

public class MyAccountInfo implements MegaRequestListenerInterface {

    int usedPerc = 0;
    int accountType = -1;
    MegaAccountDetails accountInfo = null;
    BitSet paymentBitSet = null;
    long numberOfSubscriptions = -1;
    long usedGbStorage = -1;
    String usedFormatted = "";
    String totalFormatted = "";
    int levelInventory = -1;
    int levelAccountDetails = -1;

    boolean inventoryFinished = false;
    boolean accountDetailsFinished = false;
    boolean getPaymentMethodsBoolean = false;

    String lastSessionFormattedDate;

    DatabaseHandler dbH;
    Context context;

    public MyAccountInfo(Context context){
        log("AccountController created");

        this.context = context;
        if (dbH == null){
            dbH = DatabaseHandler.getDbHandler(context);
        }
    }

    public void setAccountDetails(){
        long totalStorage = accountInfo.getStorageMax();
        long usedStorage = accountInfo.getStorageUsed();;
        boolean totalGb = false;

        usedPerc = 0;
        if (totalStorage != 0){
            usedPerc = (int)((100 * usedStorage) / totalStorage);
        }

        totalStorage = ((totalStorage / 1024) / 1024) / 1024;
        totalFormatted="";

        if (totalStorage >= 1024){
            totalStorage = totalStorage / 1024;
            totalFormatted = totalFormatted + totalStorage + " TB";
        }
        else{
            totalFormatted = totalFormatted + totalStorage + " GB";
            totalGb = true;
        }

        usedStorage = ((usedStorage / 1024) / 1024) / 1024;
        usedFormatted="";

        if(totalGb){
            usedGbStorage = usedStorage;
            usedFormatted = usedFormatted + usedStorage + " GB";
        }
        else{
            if (usedStorage >= 1024){
                usedGbStorage = usedStorage;
                usedStorage = usedStorage / 1024;

                usedFormatted = usedFormatted + usedStorage + " TB";
            }
            else{
                usedGbStorage = usedStorage;
                usedFormatted = usedFormatted + usedStorage + " GB";
            }
        }

        accountDetailsFinished = true;

        accountType = accountInfo.getProLevel();

        switch (accountType){
            case 0:{
                levelAccountDetails = -1;
                break;
            }
            case 1:{
                levelAccountDetails = 1;
                break;
            }
            case 2:{
                levelAccountDetails = 2;
                break;
            }
            case 3:{
                levelAccountDetails = 3;
                break;
            }
            case 4:{
                levelAccountDetails = 0;
                break;
            }
        }

        log("LEVELACCOUNTDETAILS: " + levelAccountDetails + "; LEVELINVENTORY: " + levelInventory + "; INVENTORYFINISHED: " + inventoryFinished);

    }

    public MegaAccountDetails getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(MegaAccountDetails accountInfo) {
        this.accountInfo = accountInfo;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public long getNumberOfSubscriptions() {
        return numberOfSubscriptions;
    }

    public void setNumberOfSubscriptions(long numberOfSubscriptions) {
        this.numberOfSubscriptions = numberOfSubscriptions;
    }

    public BitSet getPaymentBitSet() {
        return paymentBitSet;
    }

    public void setPaymentBitSet(BitSet paymentBitSet) {
        this.paymentBitSet = paymentBitSet;
    }

    public long getUsedGbStorage() {
        return usedGbStorage;
    }

    public void setUsedGbStorage(long usedGbStorage) {
        this.usedGbStorage = usedGbStorage;
    }

    public int getUsedPerc() {
        return usedPerc;
    }

    public void setUsedPerc(int usedPerc) {
        this.usedPerc = usedPerc;
    }

    public String getTotalFormatted() {
        return totalFormatted;
    }

    public void setTotalFormatted(String totalFormatted) {
        this.totalFormatted = totalFormatted;
    }

    public String getUsedFormatted() {
        return usedFormatted;
    }

    public void setUsedFormatted(String usedFormatted) {
        this.usedFormatted = usedFormatted;
    }

    public int getLevelInventory() {
        return levelInventory;
    }

    public void setLevelInventory(int levelInventory) {
        this.levelInventory = levelInventory;
    }

    public int getLevelAccountDetails() {
        return levelAccountDetails;
    }

    public void setLevelAccountDetails(int levelAccountDetails) {
        this.levelAccountDetails = levelAccountDetails;
    }


    public boolean isAccountDetailsFinished() {
        return accountDetailsFinished;
    }

    public void setAccountDetailsFinished(boolean accountDetailsFinished) {
        this.accountDetailsFinished = accountDetailsFinished;
    }

    public boolean isInventoryFinished() {
        return inventoryFinished;
    }

    public void setInventoryFinished(boolean inventoryFinished) {
        this.inventoryFinished = inventoryFinished;
    }

    public String getLastSessionFormattedDate() {
        return lastSessionFormattedDate;
    }

    public void setLastSessionFormattedDate(String lastSessionFormattedDate) {
        this.lastSessionFormattedDate = lastSessionFormattedDate;
    }

    public static void log(String message) {
        Util.log("MyAccountInfo", message);
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestFinish: " + request.getRequestString());

        if (request.getType() == MegaRequest.TYPE_ACCOUNT_DETAILS){
            log ("account_details request");
            if (e.getErrorCode() == MegaError.API_OK){

                dbH.setAccountDetailsTimeStamp();

                setAccountInfo(request.getMegaAccountDetails());

                setAccountDetails();

                ((ManagerActivityLollipop)context).updateAccountDetailsVisibleInfo();

                if(request.getMegaAccountDetails()!=null){
                    log("getMegaAccountDetails not Null");

                    MegaAccountSession megaAccountSession = request.getMegaAccountDetails().getSession(0);

                    if(megaAccountSession!=null){
                        log("getMegaAccountSESSION not Null");
                        long mostRecentSession = megaAccountSession.getMostRecentUsage();
                        log("The last session: "+mostRecentSession);
                        java.text.DateFormat df = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.MEDIUM, Locale.getDefault());
                        Date date = new Date(mostRecentSession * 1000);
                        Calendar cal = Calendar.getInstance();
                        TimeZone tz = cal.getTimeZone();
                        df.setTimeZone(tz);
                        lastSessionFormattedDate = df.format(date);
                        log("Formatted date: "+lastSessionFormattedDate);
                    }
                }

                //Check if myAccount section is visible
                MyAccountFragmentLollipop mAF = ((ManagerActivityLollipop) context).getMyAccountFragment();
                ManagerActivityLollipop.DrawerItem drawerItem = ((ManagerActivityLollipop) context).getDrawerItem();
                if((drawerItem== ManagerActivityLollipop.DrawerItem.ACCOUNT)&&(((ManagerActivityLollipop) context).getAccountFragment()== Constants.MY_ACCOUNT_FRAGMENT)){
                    if(mAF!=null){
                        mAF.setAccountDetails();
                    }
                }
                log("onRequest TYPE_ACCOUNT_DETAILS: "+getUsedPerc());
            }
        }
        else if (request.getType() == MegaRequest.TYPE_GET_PAYMENT_METHODS){
            log ("payment methods request");
            getPaymentMethodsBoolean=true;
            if (e.getErrorCode() == MegaError.API_OK){
                dbH.setPaymentMethodsTimeStamp();
                setPaymentBitSet(Util.convertToBitSet(request.getNumber()));
            }
        }
        else if(request.getType() == MegaRequest.TYPE_CREDIT_CARD_QUERY_SUBSCRIPTIONS){
            if (e.getErrorCode() == MegaError.API_OK){
                dbH.setCreditCardTimestamp();
                setNumberOfSubscriptions(request.getNumber());
                log("NUMBER OF SUBS: " + getNumberOfSubscriptions());
                ((ManagerActivityLollipop) context).updateCancelSubscriptions();
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

    }
}
