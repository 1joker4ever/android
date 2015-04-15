package nz.mega.android;

import nz.mega.android.utils.Util;

public class MegaPreferences{
	
	String firstTime = "";
	String camSyncWifi = "";
	String camSyncCharging = "";
	String camSyncEnabled = "";
	String camSyncHandle = "";
	String camSyncLocalPath = "";
	String camSyncFileUpload = "";
	String camSyncTimeStamp = "";
	String pinLockEnabled = "";
	String pinLockCode = "";
	String storageAskAlways = "";
	String storageDownloadLocation = "";
	String lastFolderUpload = "";
	String lastFolderCloud = "";
	String secondaryMediaFolderEnabled = "";
	String localPathSecondaryFolder = "";
	String megaHandleSecondaryFolder = "";
	String secSyncTimeStamp = "";
		
	public final static int ONLY_PHOTOS = 1001;
	public final static int ONLY_VIDEOS = 1002;
	public final static int PHOTOS_AND_VIDEOS = 1003;
	
	MegaPreferences(String firstTime, String camSyncWifi, String camSyncEnabled, String camSyncHandle, String camSyncLocalPath, String camSyncFileUpload, String camSyncTimeStamp, String pinLockEnabled, String pinLockCode, String storageAskAlways, 
			String storageDownloadLocation, String camSyncCharging, String lastFolderUpload, String lastFolderCloud, String secondaryMediaFolderEnabled, String localPathSecondaryFolder, String megaHandleSecondaryFolder){
		this.firstTime = firstTime;
		this.camSyncWifi = camSyncWifi;
		this.camSyncEnabled = camSyncEnabled;
		this.camSyncHandle = camSyncHandle;
		this.camSyncLocalPath = camSyncLocalPath;
		this.camSyncFileUpload = camSyncFileUpload;
		this.camSyncTimeStamp = camSyncTimeStamp;
		this.pinLockEnabled = pinLockEnabled;
		this.pinLockCode = pinLockCode;
		this.storageAskAlways = storageAskAlways;
		this.storageDownloadLocation = storageDownloadLocation;
		this.camSyncCharging = camSyncCharging;
		this.lastFolderUpload = lastFolderUpload;
		this.lastFolderCloud = lastFolderCloud;
		this.secondaryMediaFolderEnabled = secondaryMediaFolderEnabled;
		this.localPathSecondaryFolder = localPathSecondaryFolder;
		this.megaHandleSecondaryFolder = megaHandleSecondaryFolder;
	}
	
	public String getFirstTime (){
		return firstTime;
	}
	
	public void setFirstTime(String firstTime){
		this.firstTime = firstTime;
	}
	
	public String getCamSyncEnabled(){
		return camSyncEnabled;
	}
	
	public void setCamSyncEnabled(String camSyncEnabled){
		this.camSyncEnabled = camSyncEnabled;
	}
	
	public String getCamSyncHandle(){
		return camSyncHandle;
	}
	
	public void setCamSyncHandle(String camSyncHandle){
		this.camSyncHandle = camSyncHandle;
	}
	
	public String getCamSyncLocalPath(){
		return camSyncLocalPath;
	}
	
	public void setCamSyncLocalPath(String camSyncLocalPath){
		this.camSyncLocalPath = camSyncLocalPath;
	}
	
	public String getCamSyncWifi (){
		return camSyncWifi;
	}
	
	public void setCamSyncWifi(String camSyncWifi){
		this.camSyncWifi = camSyncWifi;
	}
	
	public String getCamSyncCharging (){
		return camSyncCharging;
	}
	
	public void setCamSyncCharging(String camSyncCharging){
		this.camSyncCharging = camSyncCharging;
	}
	
	public String getCamSyncFileUpload(){
		return camSyncFileUpload;
	}
	
	public void setCamSyncFileUpload(String camSyncFileUpload){
		this.camSyncFileUpload = camSyncFileUpload;
	}
	
	public String getCamSyncTimeStamp(){
		return camSyncTimeStamp;
	}
	
	public void setCamSyncTimeStamp(String camSyncTimeStamp){
		this.camSyncTimeStamp = camSyncTimeStamp;
	}
	
	public String getPinLockEnabled(){
		return pinLockEnabled;
	}
	
	public void setPinLockEnabled(String pinLockEnabled){
		this.pinLockEnabled = pinLockEnabled;
	}
	
	public String getPinLockCode(){
		return pinLockCode;
	}
	
	public void setPinLockCode(String pinLockCode){
		this.pinLockCode = pinLockCode;
	}
	
	public String getStorageAskAlways(){
		return storageAskAlways;
	}
	
	public void setStorageAskAlways(String storageAskAlways){
		this.storageAskAlways = storageAskAlways;
	}
	
	public String getStorageDownloadLocation(){
		return storageDownloadLocation;
	}
	
	public void setStorageDownloadLocation(String storageDownloadLocation){
		this.storageDownloadLocation = storageDownloadLocation;
	}

	public String getLastFolderUpload() {
		if(lastFolderUpload == null || lastFolderUpload.length() == 0)
			return null;
		return lastFolderUpload;
	}

	public void setLastFolderUpload(String lastFolderUpload) {
		this.lastFolderUpload = lastFolderUpload;
	}

	public String getLastFolderCloud() {
		if(lastFolderCloud == null || lastFolderCloud.length() == 0)
			return null;
		
		return lastFolderCloud;
	}

	public void setLastFolderCloud(String lastFolderCloud) {
		this.lastFolderCloud = lastFolderCloud;
	}

	public String getSecondaryMediaFolderEnabled() {
		return secondaryMediaFolderEnabled;
	}

	public void setSecondaryMediaFolderEnabled(String secondaryMediaFolderEnabled) {
		this.secondaryMediaFolderEnabled = secondaryMediaFolderEnabled;
	}

	public String getLocalPathSecondaryFolder() {
		return localPathSecondaryFolder;
	}

	public void setLocalPathSecondaryFolder(String localPathSecondaryFolder) {
		this.localPathSecondaryFolder = localPathSecondaryFolder;
	}

	public String getMegaHandleSecondaryFolder() {
		log("getMegaHandleSecondaryFolder "+megaHandleSecondaryFolder);
		return megaHandleSecondaryFolder;
	}

	public void setMegaHandleSecondaryFolder(String megaHandleSecondaryFolder) {
		this.megaHandleSecondaryFolder = megaHandleSecondaryFolder;
	}
	private static void log(String log) {
		Util.log("Preferences", log);
	}

	public String getSecSyncTimeStamp() {
		return secSyncTimeStamp;
	}

	public void setSecSyncTimeStamp(String secSyncTimeStamp) {
		this.secSyncTimeStamp = secSyncTimeStamp;
	}
}

//import java.util.Arrays;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.os.Bundle;
//import android.preference.PreferenceActivity;
//import android.provider.Settings;
//import android.util.Base64;
//
//public class Preferences extends PreferenceActivity{
//	
//	public static String FILE = "prefs_main.xml";
//	// Preferences keys
//	public static String KEY_EMAIL = "email";
//	public static String KEY_PASSWORD2 = "password";
//	public static String KEY_PUBLIC_KEY = "public_key";
//	public static String KEY_PRIVATE_KEY = "private_key";
//	
//	@Override
//	public void onCreate(Bundle savedInstanceState){
//		super.onCreate(savedInstanceState);
//		
//	}
//	
//	/*
//	 * Get static preference object
//	 */
//	public static SharedPreferences getPreferences(Context context) {
//		SharedPreferences prefs = context.getSharedPreferences(FILE, 0);
//		return prefs;
//	}
//	
//	/*
//	 * Get user credentials or null if not available
//	 */
//	synchronized public static UserCredentials getCredentials(Context context){
//		if (context == null){
//			return null;
//		}
//		
//		SharedPreferences prefs = getPreferences(context);
//		
//		String email = decrypt(prefs.getString(KEY_EMAIL, null));
//		if(email == null) return null;
//		
//		String publicKey = decrypt(prefs.getString(KEY_PUBLIC_KEY, null));
//		if(publicKey == null) return null;
//		
//		String privateKey = decrypt(prefs.getString(KEY_PRIVATE_KEY, null));
//		if (privateKey == null) return null;
//		
//		return new UserCredentials(email, privateKey, publicKey);
//	}
//	
//	public static String encrypt(String original) {
//		if (original == null) {
//			return null;
//		}
//		try {
//			byte[] encrypted = Util.aes_encrypt(getAesKey(),original.getBytes());
//			return Base64.encodeToString(encrypted, Base64.DEFAULT);
//		} catch (Exception e) {
//			log("ee");
//			e.printStackTrace();
//			return null;
//		}
//	}
//	
//	public static String decrypt(String encodedString) {
//		if (encodedString == null) {
//			return null;
//		}
//		try {
//			byte[] encoded = Base64.decode(encodedString, Base64.DEFAULT);
//			byte[] original = Util.aes_decrypt(getAesKey(), encoded);
//			return new String(original);
//		} catch (Exception e) {
//			log("de");
//			return null;
//		}
//	}
//	
//	/*
//	 * Save user credentials
//	 */
//	public static void saveCredentials(Context context,
//			UserCredentials credentials) {
//		if(context == null) return;
//		SharedPreferences prefs = getPreferences(context);
//		Editor editor = prefs.edit();
//		editor.putString(Preferences.KEY_EMAIL, encrypt(credentials.getEmail()));
//		editor.putString(Preferences.KEY_PUBLIC_KEY,
//				encrypt(credentials.getPublicKey()));
//		editor.putString(Preferences.KEY_PRIVATE_KEY,
//				encrypt(credentials.getPrivateKey()));
//		editor.commit();
//	}
//	
//	/*
//	 * Remove user credentials
//	 */
//	public static void clearCredentials(Context context) {
//		SharedPreferences prefs = getPreferences(context);
//		Editor editor = prefs.edit();
//		editor.clear();
//		editor.commit();
//	}
//	
//	private static byte[] getAesKey() {
//		String key = Settings.Secure.ANDROID_ID
//				+ "fkvn8 w4y*(NC$G*(G($*GR*(#)*huio4h389$G";
//		return Arrays.copyOfRange(key.getBytes(), 0, 32);
//	}
//	

//
//}
