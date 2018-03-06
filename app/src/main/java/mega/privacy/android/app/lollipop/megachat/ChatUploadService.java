package mega.privacy.android.app.lollipop.megachat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.VideoDownsampling;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.DBUtil;
import mega.privacy.android.app.utils.PreviewUtils;
import mega.privacy.android.app.utils.ThumbnailUtils;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaTransferListenerInterface;

public class ChatUploadService extends Service implements MegaTransferListenerInterface, MegaRequestListenerInterface, MegaChatRequestListenerInterface {

	public static String ACTION_CANCEL = "CANCEL_UPLOAD";
	public static String EXTRA_FILEPATH = "MEGA_FILE_PATH";
	public static String EXTRA_SIZE = "MEGA_SIZE";
	public static String EXTRA_CHAT_ID = "CHAT_ID";
	public static String EXTRA_ID_PEND_MSG = "ID_PEND_MSG";

	private boolean isForeground = false;
	private boolean canceled;

	boolean sendOriginalAttachments=false;

	ArrayList<PendingMessage> pendingMessages;
	HashMap<String, Integer> mapVideoDownsampling;

	MegaApplication app;
	MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;
	int requestSent = 0;

	WifiLock lock;
	WakeLock wl;
	DatabaseHandler dbH = null;

	int transfersCount = 0;
	int numberVideosPending = 0;
	int totalVideos = 0;
//	int currentPercentageDownsampling = 0;

	MegaNode parentNode;

	VideoDownsampling videoDownsampling;

	private Notification.Builder mBuilder;
	private NotificationCompat.Builder mBuilderCompat;
	private NotificationManager mNotificationManager;

	Object syncObject = new Object();

	MegaRequestListenerInterface megaRequestListener;
	MegaTransferListenerInterface megaTransferListener;

	private int notificationId = Constants.NOTIFICATION_UPLOAD;

	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		super.onCreate();
		log("onCreate");

		app = (MegaApplication)getApplication();

		megaApi = app.getMegaApi();
		megaChatApi = app.getMegaChatApi();
		megaApi.addTransferListener(this);
		pendingMessages = new ArrayList<>();

		dbH = DatabaseHandler.getDbHandler(getApplicationContext());

		isForeground = false;
		canceled = false;

		mapVideoDownsampling = new HashMap();

		int wifiLockMode = WifiManager.WIFI_MODE_FULL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            wifiLockMode = WifiManager.WIFI_MODE_FULL_HIGH_PERF;
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		lock = wifiManager.createWifiLock(wifiLockMode, "MegaUploadServiceWifiLock");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MegaUploadServicePowerLock");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			mBuilder = new Notification.Builder(ChatUploadService.this);
		mBuilderCompat = new NotificationCompat.Builder(ChatUploadService.this);

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onDestroy(){
		log("onDestroy");
		if((lock != null) && (lock.isHeld()))
			try{ lock.release(); } catch(Exception ex) {}
		if((wl != null) && (wl.isHeld()))
			try{ wl.release(); } catch(Exception ex) {}

		if(megaApi != null)
		{
			megaApi.removeRequestListener(this);
			megaApi.removeTransferListener(this);
		}

        if (megaChatApi != null){
            megaChatApi.saveCurrentState();
        }

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log("onStartCommand");

		canceled = false;

		if(intent == null){
			return START_NOT_STICKY;
		}

		if ((intent.getAction() != null)){
			if (intent.getAction().equals(ACTION_CANCEL)) {
				log("Cancel intent");
				canceled = true;
				megaApi.cancelTransfers(MegaTransfer.TYPE_UPLOAD, this);
				return START_NOT_STICKY;
			}
		}

		onHandleIntent(intent);

		return START_NOT_STICKY;
	}

	protected void onHandleIntent(final Intent intent) {
		log("onHandleIntent");

		String filePath = intent.getStringExtra(EXTRA_FILEPATH);

		long chatId = intent.getLongExtra(EXTRA_CHAT_ID, -1);

		long idPendMsg = intent.getLongExtra(EXTRA_ID_PEND_MSG, -1);

        sendOriginalAttachments = DBUtil.isSendOriginalAttachments(this);
        log("sendOriginalAttachments is "+sendOriginalAttachments);

		if(chatId!=-1){

			log("The chat id is: "+chatId);

			PendingMessage newMessage = new PendingMessage(idPendMsg, chatId, filePath, PendingMessage.STATE_SENDING);

			parentNode = megaApi.getNodeByPath("/"+Constants.CHAT_FOLDER);
			if(parentNode != null){
				log("The destination "+Constants.CHAT_FOLDER+ " already exists");

				if(!wl.isHeld()){
					wl.acquire();
				}

				if(!lock.isHeld()){
					lock.acquire();
				}
				log("Chat file uploading: "+filePath);

				File file = new File(filePath);

				if(MimeTypeList.typeForName(file.getName()).isMp4Video()&&(!sendOriginalAttachments)){
					try {
						totalVideos++;
						numberVideosPending++;
						File defaultDownloadLocation = null;
						if (Environment.getExternalStorageDirectory() != null){
							defaultDownloadLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.chatTempDIR + "/");
						}
						else{
							defaultDownloadLocation = getFilesDir();
						}

						defaultDownloadLocation.mkdirs();

						File outFile = new File(defaultDownloadLocation.getAbsolutePath(), file.getName());
						int index = 0;
						if(outFile!=null){
							while(outFile.exists()){
								if(index>0){
									outFile = new File(defaultDownloadLocation.getAbsolutePath(), file.getName());
								}

								index++;
								String outFilePath = outFile.getAbsolutePath();
								String[] splitByDot = outFilePath.split("\\.");
								String ext="";
								if(splitByDot!=null && splitByDot.length>1)
									ext = splitByDot[splitByDot.length-1];
								String fileName = outFilePath.substring(outFilePath.lastIndexOf(File.separator)+1, outFilePath.length());
								if(ext.length()>0)
									fileName=fileName.replace("."+ext, "_"+index+".mp4");
								else
									fileName=fileName.concat("_"+index+".mp4");

								outFile = new File(defaultDownloadLocation.getAbsolutePath(), fileName);
							}
						}

						outFile.createNewFile();

						if(outFile==null){
							numberVideosPending--;
							totalVideos--;
							pendingMessages.add(newMessage);
							megaApi.startUpload(filePath, parentNode);
						}
						else{
							newMessage.setVideoDownSampled(outFile.getAbsolutePath());
							pendingMessages.add(newMessage);
							mapVideoDownsampling.put(outFile.getAbsolutePath(), 0);
							if(videoDownsampling==null){
								videoDownsampling = new VideoDownsampling(this);
							}
							videoDownsampling.changeResolution(file, outFile.getAbsolutePath());
						}

					} catch (Throwable throwable) {
						pendingMessages.add(newMessage);
						megaApi.startUpload(filePath, parentNode);
						log("EXCEPTION: Video cannot be downsampled");
					}
				}
				else{
					pendingMessages.add(newMessage);
					megaApi.startUpload(filePath, parentNode);
				}

			}
			else{
				log("Chat folder NOT exists --> STOP service");
				isForeground = false;
				stopForeground(true);
				mNotificationManager.cancel(notificationId);
				stopSelf();
				log("after stopSelf");
			}
		}
		else{
			log("Error the chatId is not correct: "+chatId);
		}
	}

	/*
	 * Stop uploading service
	 */
	private void cancel() {
		log("cancel");
		canceled = true;
		isForeground = false;
		stopForeground(true);
		mNotificationManager.cancel(notificationId);
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/*
	 * No more intents in the queue
	 */
	private void onQueueComplete() {
		log("onQueueComplete");
		//Review when is called

		if((lock != null) && (lock.isHeld()))
			try{ lock.release(); } catch(Exception ex) {}
		if((wl != null) && (wl.isHeld()))
			try{ wl.release(); } catch(Exception ex) {}

		int total = megaApi.getNumPendingUploads() + megaApi.getNumPendingDownloads();
		if(total <= 0 && numberVideosPending<=0){
			log("onQueueComplete: reset total uploads/downloads");
			megaApi.resetTotalUploads();
			megaApi.resetTotalDownloads();
			numberVideosPending=0;
			totalVideos=0;
		}

		log("stopping service!!!!!!!!!!:::::::::::::::!!!!!!!!!!!!");
		isForeground = false;
		stopForeground(true);
		mNotificationManager.cancel(notificationId);
		stopSelf();
		log("after stopSelf");

		try{
			String pathSelfie = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.temporalPicDIR;
			File f = new File(pathSelfie);
			//Delete recursively all files and folder
			if (f.exists()) {
				if (f.isDirectory()) {
					if(f.list().length<=0){
						f.delete();
					}
				}
			}
		}
		catch (Exception e){
			log("EXCEPTION: pathSelfie not deleted");
		}

		try{
			String pathVideoDownsampling = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ Util.chatTempDIR;
			File fVideo = new File(pathVideoDownsampling);
			//Delete recursively all files and folder
			if (fVideo.exists()) {
				if (fVideo.isDirectory()) {
					if(fVideo.list().length<=0){
						fVideo.delete();
					}
				}

			}
		}
		catch (Exception e){
			log("EXCEPTION: pathVideoDownsampling not deleted");
		}
	}

	public void updateProgressDownsampling(int percentage, String key){

		mapVideoDownsampling.put(key, percentage);
		updateProgressNotification();
	}

	public void finishDownsampling(String returnedFile, boolean success){
		log("finishDownsampling");
		numberVideosPending--;

		File downFile = null;

		if(success){
			mapVideoDownsampling.put(returnedFile, 100);
			downFile = new File(returnedFile);
		}
		else{
			mapVideoDownsampling.remove(returnedFile);

			for(int i=0; i<pendingMessages.size();i++){
				PendingMessage pendMsg = pendingMessages.get(i);

				if(pendMsg.getVideoDownSampled()!=null){
					if(pendMsg.getVideoDownSampled().equals(returnedFile)){
						pendMsg.setVideoDownSampled(null);

						PendingNodeAttachment nodeAttachment = pendMsg.getNodeAttachment();
						downFile = new File(nodeAttachment.getFilePath());
						log("Found the downFile");
					}
				}
				else{
					log("Error message could not been downsampled");
				}
			}
			if(downFile!=null){
				mapVideoDownsampling.put(downFile.getAbsolutePath(), 100);
			}
		}

		if(downFile!=null){
			megaApi.startUpload(downFile.getPath(), parentNode);
		}
	}

	@SuppressLint("NewApi")
	private void updateProgressNotification() {

		int totalTransfers = megaApi.getTotalUploads()+numberVideosPending;

		long totalSizePendingTransfer = megaApi.getTotalUploadBytes();
		long totalSizeTransferred = megaApi.getTotalUploadedBytes();

		int progressPercent = 0;

		if(totalVideos>0){

			progressPercent = (int) Math.round((double) totalSizeTransferred / totalSizePendingTransfer * 50);

			int downsamplingPercent  = 0;

			Collection<Integer> values= mapVideoDownsampling.values();
			int simplePercentage = 50/totalVideos;
			for (Iterator iterator = values.iterator(); iterator.hasNext();) {
				Integer value = (Integer) iterator.next();
				int simpleValue = simplePercentage*value/100;
				downsamplingPercent = downsamplingPercent +simpleValue;
			}

			progressPercent = progressPercent + downsamplingPercent;
		}
		else{
			progressPercent =  (int) Math.round((double) totalSizeTransferred / totalSizePendingTransfer * 100);
		}

		log("updateProgressNotification: "+progressPercent);

		String message = "";

		int inProgress = progressPercent*totalTransfers/100;
		if(inProgress==0){
			inProgress=1;
		}

		message = getResources().getQuantityString(R.plurals.upload_service_notification, totalTransfers, inProgress, totalTransfers);

		Intent intent;
		intent = new Intent(ChatUploadService.this, ManagerActivityLollipop.class);
		intent.setAction(Constants.ACTION_SHOW_TRANSFERS);

		PendingIntent pendingIntent = PendingIntent.getActivity(ChatUploadService.this, 0, intent, 0);
		Notification notification = null;
		int currentapiVersion = Build.VERSION.SDK_INT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			mBuilder
					.setSmallIcon(R.drawable.ic_stat_notify_upload)
					.setProgress(100, progressPercent, false)
					.setContentIntent(pendingIntent)
					.setOngoing(true).setContentTitle(message)
					.setContentText(getString(R.string.chat_upload_title_notification))
					.setOnlyAlertOnce(true);
			notification = mBuilder.build();
		}
		else if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)	{

			mBuilder
			.setSmallIcon(R.drawable.ic_stat_notify_upload)
			.setProgress(100, progressPercent, false)
			.setContentIntent(pendingIntent)
			.setOngoing(true).setContentTitle(message)
			.setContentText(getString(R.string.chat_upload_title_notification))
			.setOnlyAlertOnce(true);
			notification = mBuilder.getNotification();

		}
		else
		{
			notification = new Notification(R.drawable.ic_stat_notify_upload, null, 1);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_progress);
			notification.contentIntent = pendingIntent;
			notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_stat_notify_upload);
			notification.contentView.setTextViewText(R.id.status_text, message);
			notification.contentView.setProgressBar(R.id.status_progress, 100, progressPercent, false);
		}

			
		if (!isForeground) {
			log("starting foreground");
			try {
				startForeground(notificationId, notification);
				isForeground = true;
			}
			catch (Exception e){
				log("startforeground exception: " + e.getMessage());
				isForeground = false;
			}
		} else {
			mNotificationManager.notify(notificationId, notification);
		}
	}
	
	public static void log(String log) {
		Util.log("ChatUploadService", log);
	}

	@Override
	public void onTransferStart(MegaApiJava api, MegaTransfer transfer) {
		log("Upload start: " + transfer.getFileName() + "_" + megaApi.getTotalUploads());

		if(transfer.isStreamingTransfer()){
			return;
		}

        transfersCount++;
		if (!transfer.isFolderTransfer()){
			updateProgressNotification();
		}
	}

	@Override
	public void onTransferFinish(MegaApiJava api, MegaTransfer transfer,MegaError error) {
		log("onTransferFinish: " + transfer.getFileName() + " size " + transfer.getTransferredBytes());
		log("transfer.getPath:" + transfer.getPath());

		if(transfer.isStreamingTransfer()){
			return;
		}

        transfersCount--;

        if (canceled) {
            log("Upload cancelled: " + transfer.getFileName());

            if((lock != null) && (lock.isHeld()))
                try{ lock.release(); } catch(Exception ex) {}
            if((wl != null) && (wl.isHeld()))
                try{ wl.release(); } catch(Exception ex) {}

            ChatUploadService.this.cancel();
            log("after cancel");
			String pathSelfie = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.temporalPicDIR;
			File f = new File(pathSelfie);
			//Delete recursively all files and folder
			if (f.isDirectory()) {
				if (f.isDirectory()) {
					if(f.list().length<=0){
						f.delete();
					}
				}
			}
			f.delete();
        }
        else{
            if (error.getErrorCode() == MegaError.API_OK) {
                log("Upload OK: " + transfer.getFileName());

                if(Util.isVideoFile(transfer.getPath())){
                    log("Is video!!!");

					File previewDir = PreviewUtils.getPreviewFolder(this);
					File preview = new File(previewDir, MegaApiAndroid.handleToBase64(transfer.getNodeHandle()) + ".jpg");
					File thumbDir = ThumbnailUtils.getThumbFolder(this);
					File thumb = new File(thumbDir, MegaApiAndroid.handleToBase64(transfer.getNodeHandle()) + ".jpg");
					megaApi.createThumbnail(transfer.getPath(), thumb.getAbsolutePath());
					megaApi.createPreview(transfer.getPath(), preview.getAbsolutePath());

                    MegaNode node = megaApi.getNodeByHandle(transfer.getNodeHandle());
                    if(node!=null){

                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(transfer.getPath());

						String location = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
						if(location!=null){
							log("Location: "+location);

							boolean secondTry = false;
							try{
								final int mid = location.length() / 2; //get the middle of the String
								String[] parts = {location.substring(0, mid),location.substring(mid)};

								Double lat = Double.parseDouble(parts[0]);
								Double lon = Double.parseDouble(parts[1]);
								log("Lat: "+lat); //first part
								log("Long: "+lon); //second part

								megaApi.setNodeCoordinates(node, lat, lon, null);
							}
							catch (Exception e){
								secondTry = true;
								log("Exception, second try to set GPS coordinates");
							}

							if(secondTry){
								try{
									String latString = location.substring(0,7);
									String lonString = location.substring(8,17);

									Double lat = Double.parseDouble(latString);
									Double lon = Double.parseDouble(lonString);
									log("Lat2: "+lat); //first part
									log("Long2: "+lon); //second part

									megaApi.setNodeCoordinates(node, lat, lon, null);
								}
								catch (Exception e){
									log("Exception again, no chance to set coordinates of video");
								}
							}
						}
						else{
							log("No location info");
						}
                    }
                }
				else if (MimeTypeList.typeForName(transfer.getPath()).isImage()){
					log("Is image!!!");

					MegaNode node = megaApi.getNodeByHandle(transfer.getNodeHandle());
					if(node!=null){

						if(sendOriginalAttachments){
							File previewDir = PreviewUtils.getPreviewFolder(this);
							File preview = new File(previewDir, MegaApiAndroid.handleToBase64(transfer.getNodeHandle()) + ".jpg");
							File thumbDir = ThumbnailUtils.getThumbFolder(this);
							File thumb = new File(thumbDir, MegaApiAndroid.handleToBase64(transfer.getNodeHandle()) + ".jpg");
							megaApi.createThumbnail(transfer.getPath(), thumb.getAbsolutePath());
							megaApi.createPreview(transfer.getPath(), preview.getAbsolutePath());
						}
						else{
							File previewDir = PreviewUtils.getPreviewFolder(this);

							try{
								File previewOldPreview = new File(transfer.getPath());
								String newName = MegaApiAndroid.handleToBase64(transfer.getNodeHandle()) + ".jpg";
								File preview = new File(previewDir, newName);

								previewOldPreview.renameTo(preview);
							}
							catch (Exception e){
								log("Cannot rename file preview");
							}

							File thumbDir = ThumbnailUtils.getThumbFolder(this);
							File thumb = new File(thumbDir, MegaApiAndroid.handleToBase64(transfer.getNodeHandle()) + ".jpg");
							megaApi.createThumbnail(transfer.getPath(), thumb.getAbsolutePath());
							megaApi.setPreview(node, transfer.getPath());
						}

						try {
							final ExifInterface exifInterface = new ExifInterface(transfer.getPath());
							float[] latLong = new float[2];
							if (exifInterface.getLatLong(latLong)) {
								log("Latitude: "+latLong[0]+" Longitude: " +latLong[1]);
								megaApi.setNodeCoordinates(node, latLong[0], latLong[1], null);
							}

						} catch (Exception exception) {
							log("Couldn't read exif info: " + transfer.getPath());
						}
					}
				}
				else if (MimeTypeList.typeForName(transfer.getPath()).isPdf()) {
					log("Is pdf!!!");

					try{
						ThumbnailUtilsLollipop.createThumbnailPdf(this, transfer.getPath(), megaApi, transfer.getNodeHandle());
					}
					catch(Exception e){
						log("Pdf thumbnail could not be created");
					}

					int pageNumber = 0;
					FileOutputStream out = null;

					try {

						PdfiumCore pdfiumCore = new PdfiumCore(this);
						MegaNode pdfNode = megaApi.getNodeByHandle(transfer.getNodeHandle());

						if (pdfNode == null){
							log("pdf is NULL");
							return;
						}

						File previewDir = PreviewUtils.getPreviewFolder(this);
						File preview = new File(previewDir, MegaApiAndroid.handleToBase64(transfer.getNodeHandle()) + ".jpg");
						File file = new File(transfer.getPath());

						PdfDocument pdfDocument = pdfiumCore.newDocument(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
						pdfiumCore.openPage(pdfDocument, pageNumber);
						int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
						int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
						Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
						pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
						Bitmap resizedBitmap = PreviewUtils.resizeBitmapUpload(bmp, width, height);
						out = new FileOutputStream(preview);
						boolean result = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
						if(result){
							log("Compress OK!");
							File oldPreview = new File(previewDir, transfer.getFileName()+".jpg");
							if (oldPreview.exists()){
								oldPreview.delete();
							}
							megaApi.setPreview(pdfNode, preview.getAbsolutePath(), this);
						}
						else{
							log("Not Compress");
						}
						pdfiumCore.closeDocument(pdfDocument);
					} catch(Exception e) {
						log("Pdf preview could not be created");
					} finally {
						try {
							if (out != null)
								out.close();
						} catch (Exception e) {
							//todo with exception
						}
					}
				}
                else{
                    log("NOT video, image or pdf!");
                }

                attachNodes(transfer);
			}
            else{
                log("Upload Error: " + transfer.getFileName() + "_" + error.getErrorCode() + "___" + error.getErrorString());

                if(error.getErrorCode() == MegaError.API_EEXIST){
                	log("Transfer API_EEXIST: "+transfer.getNodeHandle());
				}
				else{
					if(error.getErrorCode()==MegaError.API_EOVERQUOTA){
						log("OVERQUOTA ERROR: "+error.getErrorCode());
						Intent intent;
						intent = new Intent(this, ManagerActivityLollipop.class);
						intent.setAction(Constants.ACTION_OVERQUOTA_STORAGE);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

						startActivity(intent);

						Intent tempIntent = null;
						tempIntent = new Intent(this, ChatUploadService.class);
						tempIntent.setAction(ChatUploadService.ACTION_CANCEL);
						startService(tempIntent);
					}

					//Find the pending message
					for(int i=0; i<pendingMessages.size();i++){
						PendingMessage pendMsg = pendingMessages.get(i);

						PendingNodeAttachment nodeAttachment = pendMsg.getNodeAttachment();

						if(nodeAttachment.getFilePath().equals(transfer.getPath())){
							if(nodeAttachment.getFilePath().equals(transfer.getPath())){

								dbH.updatePendingMessage(pendMsg.getId(), -1+"", PendingMessage.STATE_ERROR);
								launchErrorToChat(pendMsg);

								if (megaApi.getNumPendingUploads() == 0 && transfersCount==0 && numberVideosPending<=0 && requestSent<=0){
									onQueueComplete();
								}
								return;
							}
						}
					}
				}
			}

            log("IN Finish: "+transfer.getFileName()+" path: "+transfer.getPath());
			String pathSelfie = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.temporalPicDIR;
			if (transfer.getPath() != null) {
				if (transfer.getPath().startsWith(pathSelfie)) {
					File f = new File(transfer.getPath());
					f.delete();
				}
			} else {
				log("transfer.getPath() is NULL");
			}
        }

		if (megaApi.getNumPendingUploads() == 0 && transfersCount==0 && numberVideosPending<=0 && requestSent<=0){
			onQueueComplete();
		}
	}

	public void attachNodes(MegaTransfer transfer){
		log("attachNodes");
		//Find the pending message
		for(int i=0; i<pendingMessages.size();i++){
			PendingMessage pendMsg = pendingMessages.get(i);
			PendingNodeAttachment nodeAttachment = pendMsg.getNodeAttachment();

			boolean videofound = false;
			if(Util.isVideoFile(transfer.getPath())){
				log("Attach video file");
				if(pendMsg.getVideoDownSampled()!=null){
					String path = pendMsg.getVideoDownSampled();
					if(path.equals(transfer.getPath())){
						log("NodeHANDLE of the nodeAttachment: "+nodeAttachment.getNodeHandle());
						if(nodeAttachment.getNodeHandle()==-1){
							nodeAttachment.setNodeHandle(transfer.getNodeHandle());
							videofound = true;
							if(megaChatApi!=null){
								log("Send node: "+transfer.getNodeHandle()+ " to chat: "+pendMsg.getChatId());
								requestSent++;
								megaChatApi.attachNode(pendMsg.getChatId(), transfer.getNodeHandle(), this);
								try{
									//Delete the local temp video file
									File f = new File(transfer.getPath());

									if (f.exists()) {
										boolean deleted = f.delete();
										if(!deleted){
											log("ERROR: Local file not deleted!");
										}
									}
								}
								catch(Exception e){
									log("Local file not deleted!");
								}

							}
						}
						else{
							log("Already attached");
						}
					}
				}
			}

			if(!videofound){
				log("Not video found");
				if(nodeAttachment.getFilePath().equals(transfer.getPath())){
					log("NodeHANDLE of the nodeAttachment: "+nodeAttachment.getNodeHandle());
					if(nodeAttachment.getNodeHandle()==-1){
						nodeAttachment.setNodeHandle(transfer.getNodeHandle());
						if(megaChatApi!=null){
							log("Send node: "+transfer.getNodeHandle()+ " to chat: "+pendMsg.getChatId());
							requestSent++;
							megaChatApi.attachNode(pendMsg.getChatId(), transfer.getNodeHandle(), this);
						}
					}
					else{
						log("Already attached");
					}
				}
			}
		}
	}

	@Override
	public void onTransferUpdate(MegaApiJava api, MegaTransfer transfer) {

		if(transfer.isStreamingTransfer()){
			return;
		}

		if (!transfer.isFolderTransfer()){
			if (canceled) {
				log("Transfer cancel: " + transfer.getFileName());
	
				if((lock != null) && (lock.isHeld()))
					try{ lock.release(); } catch(Exception ex) {}
				if((wl != null) && (wl.isHeld()))
					try{ wl.release(); } catch(Exception ex) {}
				
				megaApi.cancelTransfer(transfer);
				ChatUploadService.this.cancel();
				log("after cancel");
				return;
			}
			
			if (transfer.getPath() != null){
				File f = new File(transfer.getPath());
				if (f.isDirectory()){
					transfer.getTotalBytes();				
				}
			}

			updateProgressNotification();
		}
	}

	@Override
	public void onTransferTemporaryError(MegaApiJava api,
			MegaTransfer transfer, MegaError e) {
		log(transfer.getPath() + "\nUpload Temporary Error: " + e.getErrorString() + "__" + e.getErrorCode());

		if(e.getErrorCode() == MegaError.API_EOVERQUOTA) {
			log("API_EOVERQUOTA error!!");

			Intent intent = null;
			intent = new Intent(this, ManagerActivityLollipop.class);
			intent.setAction(Constants.ACTION_OVERQUOTA_TRANSFER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getName());
		if (request.getType() == MegaRequest.TYPE_COPY){
			updateProgressNotification();
		}
		else if (request.getType() == MegaRequest.TYPE_SET_ATTR_FILE) {
			log("TYPE_SET_ATTR_FILE");
		}
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("UPLOAD: onRequestFinish "+request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_SET_ATTR_FILE) {
			if (e.getErrorCode()==MegaError.API_OK){
				log("preview upload OK");
			}
			else {
				log("preview upload NOT OK");
			}
		}

	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError: " + request.getName());
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		log("onRequestUpdate: " + request.getName());
	}

	@Override
	public boolean onTransferData(MegaApiJava api, MegaTransfer transfer, byte[] buffer)
	{
		return true;
	}

	@Override
	public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

	}

	@Override
	public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

	}

	@Override
	public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
		if(request.getType() == MegaChatRequest.TYPE_ATTACH_NODE_MESSAGE){
            requestSent--;
			if(e.getErrorCode()==MegaChatError.ERROR_OK){
				log("Attachment sent correctly");
				MegaNodeList nodeList = request.getMegaNodeList();

				//Find the pending message
				for(int i=0; i<pendingMessages.size();i++){
					PendingMessage pendMsg = pendingMessages.get(i);

					//Check node handles - if match add to DB the karere temp id of the message

					long nodeHandle = pendMsg.getNodeHandle();
					MegaNode node = nodeList.get(0);
					if(node.getHandle()==nodeHandle){
						log("The message MATCH!!");
						long tempId = request.getMegaChatMessage().getTempId();
						log("The tempId of the message is: "+tempId);
						dbH.updatePendingMessage(pendMsg.getId(), tempId+"", PendingMessage.STATE_SENT);
						pendingMessages.remove(i);
						break;
					}
				}
			}
			else{
				log("Attachment not correctly sent: "+e.getErrorCode()+" "+ e.getErrorString());
				MegaNodeList nodeList = request.getMegaNodeList();

				//Find the pending message
				for(int i=0; i<pendingMessages.size();i++){
					PendingMessage pendMsg = pendingMessages.get(i);
					//Check node handles - if match add to DB the karere temp id of the message
					long nodeHandle = pendMsg.getNodeHandle();
					MegaNode node = nodeList.get(0);
					if(node.getHandle()==nodeHandle){
						log("The message MATCH!!");
						dbH.updatePendingMessage(pendMsg.getId(), -1+"", PendingMessage.STATE_ERROR);

						launchErrorToChat(pendMsg);
						break;
					}
				}
			}
		}

		if (megaApi.getNumPendingUploads() == 0 && transfersCount==0 && numberVideosPending<=0 && requestSent<=0){
			onQueueComplete();
		}
	}

	public void launchErrorToChat(PendingMessage pendMsg){
		log("launchErrorToChat");

		long openChatId = MegaApplication.getOpenChatId();
		if(pendMsg.getChatId()==openChatId){
			log("Error update activity");
			Intent intent;
			intent = new Intent(this, ChatActivityLollipop.class);
			intent.setAction(Constants.ACTION_UPDATE_ATTACHMENT);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("ID_MSG", pendMsg.getId());
			startActivity(intent);
		}
	}

	@Override
	public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

	}
}
