package nz.mega.android;

import java.text.DecimalFormat;
import java.util.ArrayList;

import nz.mega.android.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaGlobalListenerInterface;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaPricing;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class FortumoFragment extends Fragment implements MegaRequestListenerInterface, MegaGlobalListenerInterface {
	
	WebView myWebView;
	
	MegaApiAndroid megaApi;
	Context context;
	private ActionBar aB;
	
	
	@Override
	public void onDestroy(){
		if(megaApi != null)
		{	
			megaApi.removeGlobalListener(this);
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState){
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		
		megaApi.addGlobalListener(this);
		
		super.onCreate(savedInstanceState);
		log("onCreate");
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		DecimalFormat df = new DecimalFormat("#.##");  

		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		if (aB == null){
			aB = ((ActionBarActivity)context).getSupportActionBar();
		}

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = ((Activity) context).getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);

		View v = null;
		v = inflater.inflate(R.layout.activity_fortumo_payment, container, false);
		
	        
        myWebView = (WebView) v.findViewById(R.id.webview);
//        WebSettings webSettings = myWebView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
	        
        megaApi.getPricing(this);
        
		return v;
	}	
	
	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,MegaError e) {
		
		log("REQUEST: " + request.getName() + "__" + request.getRequestString());
		if (request.getType() == MegaRequest.TYPE_GET_PRICING){
			MegaPricing p = request.getPricing();
			for (int i=0;i<p.getNumProducts();i++){
				Product account = new Product (p.getHandle(i), p.getProLevel(i), p.getMonths(i), p.getGBStorage(i), p.getAmount(i), p.getGBTransfer(i));
				if ((account.getLevel()==4) && (account.getMonths()==1)){
					long planHandle = account.handle;
					megaApi.getPaymentId(planHandle, this);
					log("megaApi.getPaymentId(" + planHandle + ")");
				}
			}
		}
		else if (request.getType() == MegaRequest.TYPE_GET_PAYMENT_ID){
			log("PAYMENT ID: " + request.getLink());
//			Toast.makeText(context, "PAYMENTID: " + request.getLink(), Toast.LENGTH_LONG).show();
			
			String urlFortumo = "http://fortumo.com/mobile_payments/f250460ec5d97fd27e361afaa366db0f?cuid=" + request.getLink();
			myWebView.loadUrl(urlFortumo);
//			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlFortumo));
//			startActivity(browserIntent);
//			((ManagerActivity)context).onBackPressed();
		}
	}
	
	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		// TODO Auto-generated method stub
		
	}
	
//	public int onBackPressed(){
////		((ManagerActivity)context).showpF(parameterType, accounts);
//		((ManagerActivity)context).showpF(parameterType, accounts, true);
//		return 3;
//	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		aB = ((ActionBarActivity)activity).getSupportActionBar();
	}
	
	public static void log(String message) {
		Util.log("FortumoFragment", message);
	}

	@Override
	public void onUsersUpdate(MegaApiJava api, ArrayList<MegaUser> users) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNodesUpdate(MegaApiJava api, ArrayList<MegaNode> nodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReloadNeeded(MegaApiJava api) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccountUpdate(MegaApiJava api) {
		Toast.makeText(context, "ON ACCOUNT UPDATE!!!!", Toast.LENGTH_LONG).show();
		((ManagerActivity)context).onBackPressed();
	}
}
