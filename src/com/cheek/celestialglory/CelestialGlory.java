package com.cheek.celestialglory;

import java.util.Random;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.os.Handler;

import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.provider.Settings.System;

import com.cheek.iap.BillingService;
import com.cheek.iap.PurchaseObserver;
import com.cheek.iap.ResponseHandler;
import com.cheek.iap.BillingService.RequestPurchase;
import com.cheek.iap.BillingService.RestoreTransactions;
import com.cheek.iap.Consts.PurchaseState;
import com.cheek.iap.Consts.ResponseCode;

public class CelestialGlory extends Activity {

    private static final int MENU_HOME = Menu.FIRST;
    private static final int MENU_LESSONS = Menu.FIRST + 1;
    private static final int MENU_CHALLENGES = Menu.FIRST + 2;
    private static final int MENU_AIDES = Menu.FIRST + 3;
    private static final int MENU_ACTIONS = Menu.FIRST + 4;
    private static final int MENU_FORUM = Menu.FIRST + 5;
    
    private WebView engine;
    private String androidID, usableWidth = "320",
    	billingSupportString = "; GoogleIAP", authKey = "",
    	authKeySupportString = "";
    public static String TAG = "celestialglory";
    
    private static final Random RNG = new Random();
    
    private PackageInfo pInfo = null;
    
    private MyPurchaseObserver mMyPurchaseObserver;
    private Handler mHandler;
    private BillingService mBillingService;
    
    
	@SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        engine = (WebView) findViewById(R.id.browser);
        engine.clearCache(true); // start by clearing the cache
        // splashScreen = (FrameLayout) findViewById(R.id.splash_frame);
//        handler = new Handler();
        
        androidID = System.getString(this.getContentResolver(), System.ANDROID_ID);
        Log.i(TAG, "androidID is " + androidID);
        
    	if (android.os.Build.MODEL.equalsIgnoreCase("NookColor")) {
        	
        	androidID = "nkc+" + androidID;
        	Log.i(TAG, "because this is a nook color and I can't change " +
        		"the UserAgent, androidID is now " + androidID);
            
    	}
            
        try { // sometimes androidID can't be compared, so wrap in try/catch
            	
        	if (androidID.equals("null") || androidID.equals("9774d56d682e549c")) {
            
        		Log.i(TAG, "invalid androidID!  Trying IMEI...");
        		androidID = getIMEI(); // hack for sdk and broken phones
        		Log.i(TAG, "because my androidID is not valid, I am now using " +
        			"an IMEI of " + androidID);
        	
        }
        
        } catch (Exception e) {
        	
        	androidID = getIMEI(); // lame java workaround for hack for sdk and broken phones
        	Log.i(TAG, "because my androidID is not valid, I am now using " +
        			"an IMEI of " + androidID);
        
        }
        
        authKey = getAuthKey();
        authKeySupportString = "; authKey=" + authKey;
        
// set up IAPs

        mHandler = new Handler();
        mMyPurchaseObserver = new MyPurchaseObserver(this, mHandler);
        mBillingService = new BillingService();
        mBillingService.setContext(this);

        // Check if billing is supported.
        ResponseHandler.register(mMyPurchaseObserver);
        
        if (!mBillingService.checkBillingSupported()) {
        	
        	billingSupportString = "";
        	Log.i(TAG, "Uhoh!  Billing isn't supported!");
        	
        } else {
        	
        	Log.i(TAG, "Billing supported.");
        	
        }
        
        
// set up the Web Engine

        
        engine.getSettings().setJavaScriptEnabled(true);
        
        // get usable width -- normal is 320
        
        Display display = ((WindowManager)
    		getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
        int screenWidth = display.getWidth();
        String width = Integer.toString(screenWidth);
        Log.d(TAG, "screen width is " + width);
        
        // Android tries to auto-adjust images for a specific screen density.
        // We don't want that to happen, so we give it a width we know it will
        // turn into the correct width we want.
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        float screenDensity = metrics.density;
        
        String density = Float.toString(screenDensity);
        Log.d(TAG, "screen density is " + density);
        
        usableWidth = Integer.toString((int) (screenWidth / screenDensity));
        
        try {

        	pInfo = getPackageManager().getPackageInfo("com.cheek.celestialglory",
    			PackageManager.GET_META_DATA);
            engine.getSettings().setUserAgentString(
        		engine.getSettings().getUserAgentString() + " (" +
        		pInfo.packageName + "; Android/" +
        		pInfo.versionName + '/' + pInfo.versionCode + "; width=" +
        		usableWidth + billingSupportString + authKeySupportString +
        		')');

        } catch (Exception e) {
        	
        	engine.getSettings().setUserAgentString(
        		engine.getSettings().getUserAgentString() +
        		" (com.cheek.celestialglory/Unknown/Unknown" +
        		billingSupportString + ')');
        	
        }

        engine.setWebViewClient(new MyWebViewClient());

/*        removeSplash = new Runnable() {
        	
            public void run() {
            	engine.loadUrl(getString(R.string.url_home) + androidID);
                splashScreen.setVisibility(View.GONE);                       
            }
            
        };

        handler.postDelayed(removeSplash, 4000);
*/

        // FIXME: vertical centering of the splash doesn't work
        
        String data = "<html>" +
        	"<head><meta http-equiv=\"refresh\"" +
        	" content=\"1;url=" + getString(R.string.url_home) +
        	androidID + "\"/></head> " +
            "<body style=\"margin: 0px; padding: 0px; background-color: black; " +
        	"color: white;\"><span style=\"display: table-cell; vertical-align: middle;\">" +
        	"<img width=\"" + usableWidth + "\" style=\"display: inline-block;\" " +
        	"src=\"file:///android_asset/cg_splash.jpg\"></span></body>" +
            "</html>";
        
        // splashScreen.setVisibility(View.GONE);

        engine.loadDataWithBaseURL("Fake://url.com/", data, "text/html",
        	"UTF-8", "");
        
//        engine.loadUrl(getString(R.string.url_home) + androidID);
          
    }
    
    
    @Override
    protected void onStart() {
    	super.onStart();
    	ResponseHandler.register(mMyPurchaseObserver);
	}


	@Override
	protected void onStop() {
	    super.onStop();
	    ResponseHandler.unregister(mMyPurchaseObserver);
	}
	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    mBillingService.unbind();
	}

	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);

    	menu.add(0, MENU_HOME, 0,
        	R.string.menu_home)
        	.setIcon(R.drawable.cg_home);
       	
    	menu.add(0, MENU_LESSONS, 1,
        	R.string.menu_lessons)
        	.setIcon(R.drawable.cg_quests);

    	menu.add(0, MENU_CHALLENGES, 2,
            	R.string.menu_challenges)
            	.setIcon(R.drawable.cg_trials);

    	menu.add(0, MENU_AIDES, 3,
        	R.string.menu_aides)
        	.setIcon(R.drawable.cg_aides);

    	menu.add(0, MENU_ACTIONS, 4,
        	R.string.menu_actions)
        	.setIcon(R.drawable.cg_actions);

    	menu.add(0, MENU_FORUM, 5,
        	R.string.menu_forum)
        	.setIcon(R.drawable.cg_forum);

        return result;
    } // onCreateOptionsMenu
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	
    	case MENU_HOME:
//    		engine.clearCache(true); // clear the cache whenever we go home
    		engine.loadUrl(getString(R.string.url_home) + androidID);
            return true;

    	case MENU_LESSONS:
    		engine.loadUrl(getString(R.string.url_lessons) + androidID);
            return true;

    	case MENU_CHALLENGES:
    		engine.loadUrl(getString(R.string.url_challenges) + androidID);
            return true;

    	case MENU_AIDES:
    		engine.loadUrl(getString(R.string.url_aides) + androidID);
            return true;

    	case MENU_ACTIONS:
    		engine.loadUrl(getString(R.string.url_actions) + androidID);
            return true;

    	case MENU_FORUM:
    		engine.loadUrl(getString(R.string.url_forum));
//    		buyLuck("luck_10"); // for testing
            return true;

    	} // menu item selected
    	
        return super.onOptionsItemSelected(item);
        
    } // onOptionsItemsSelected
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
//    	Log.d(TAG, "keycode " + Integer.toString(keyCode) + 
//    		"; engine.canGoBack() is " + Boolean.toString(engine.canGoBack()));
    	
//    	historyList = engine.copyBackForwardList();
//    	Log.d(TAG, "history list size is " + historyList.getSize() + 
//    		", current index is " + historyList.getCurrentIndex());
    	
        if ((keyCode == KeyEvent.KEYCODE_BACK) && engine.canGoBack() &&
    		!engine.getUrl().equals(getString(R.string.url_home) + androidID)) {
        	
        	engine.clearHistory();
        	engine.loadUrl(getString(R.string.url_home) + androidID);
//        	engine.clearHistory();
        	
            return true;
        }

        return super.onKeyDown(keyCode, event);
    
    } // onKeyDown
    
    
    public String getIMEI() {
    	
    	String imei = "000000000000000";
    	String fakeIMEIPrefix = "sdk+";

    	SharedPreferences settings;
        SharedPreferences.Editor settings_editor;
    	        	
        try {
        	
        	TelephonyManager manager = (TelephonyManager)
        		this.getSystemService(Context.TELEPHONY_SERVICE);
        	imei = manager.getDeviceId();
        	
        } catch (Exception e) {
        		
        	Log.i(TAG, "uhoh!  This tablet/device doesn't have an IMEI " +
        		"and LAMELY deleted all references to TelephonyManager " +
        		"in the ABI!");
        	fakeIMEIPrefix = "tab+";
        			
        }
        	        
    	if (imei == null) { // some generic tablets return null for IMEI
		
    		Log.i(TAG, "uhoh!  This tablet/device doesn't have an IMEI " +
    			"and returns null!");
    		fakeIMEIPrefix = "tab+";
    			
    	}
        
        if ((imei == null) || imei.equals("000000000000000")) { 
// no imei?  generate fake one
        
        	settings = getSharedPreferences(TAG, MODE_PRIVATE);
            imei = settings.getString("fake-imei", "000000000000000");
            
            if (imei.equals("000000000000000")) {
            
            	Log.d(TAG, "No saved fake IMEI, so I'm going " +
            		"to have to generate a fake one ...");
            	
            	imei = fakeIMEIPrefix + String.valueOf(RNG.nextLong());
            	settings_editor = settings.edit();
            	settings_editor.putString("fake-imei", imei);
            	settings_editor.commit();
            	Log.i(TAG, "created fake imei of " + imei);
           
            } else {
            
            	Log.i(TAG, "found existing fake imei of " + imei);
            
            }
        	
        }
        
        Log.i(TAG, "imei is " + imei);
        return imei;

    }
    
    
   public String getAuthKey() {
    	
    	String authKey = "000000000000000";

    	SharedPreferences settings;
        SharedPreferences.Editor settings_editor;
    	        	
       	settings = getSharedPreferences(TAG, MODE_PRIVATE);
       	authKey = settings.getString("auth-key", "000000000000000");
            
        if (authKey.equals("000000000000000")) {
            
           	Log.d(TAG, "No saved authKey; generating one ...");
            	
           	authKey = UUID.randomUUID().toString();
           	settings_editor = settings.edit();
           	settings_editor.putString("auth-key", authKey);
           	settings_editor.commit();
           	Log.i(TAG, "created authKey of " + authKey);
           
        } else {
            
           	Log.i(TAG, "found existing authKey of " + authKey);
            
        }
        
        Log.i(TAG, "authKey is " + authKey);
        return authKey;

    }
    
    
    private DialogInterface.OnClickListener okListener(DialogInterface dialog,
    		int which) {
        	return null;
    }

        
    @SuppressWarnings("unused")
    private void showPopUp(String Text) {
        
    	new AlertDialog.Builder(this)
			.setMessage(Text)
			.setPositiveButton(getString(R.string.msg_ok), okListener(null, 0))
			.show();
    	
    }
    
    
    private void showPopUp(Integer Text) {
        
    	new AlertDialog.Builder(this)
			.setMessage(Text)
			.setPositiveButton(getString(R.string.msg_ok), okListener(null, 0))
			.show();
    	
    }
    
    
    private void buyLuck(String sku) {
    	
        if (mBillingService.requestPurchase(sku, androidID)) {
//        	showPopUp("you just bought luck!");
//        	String response = downloadString(getString(R.string.url_purchase) +
//    			androidID +	'/' + sku);
        	engine.loadUrl(getString(R.string.url_home) + androidID);
        } else {
        	showPopUp(R.string.billing_not_supported_message);
        }
        
    }
    
    
//    private String downloadString(String fileUrl) {
//    	
//        URL myFileUrl = null;
//        String newString = "";
//        
//        try {
//             myFileUrl= new URL(fileUrl);
//        } catch (MalformedURLException e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//        }
//        
//        try {
//        	HttpURLConnection conn =
//        		(HttpURLConnection) myFileUrl.openConnection();
//        	conn.setDoInput(true);
//            conn.setRequestProperty("User-Agent",
//             	"com.cheek.celestialglory luckloader");
//            conn.connect();
////             int length = conn.getContentLength();
//            InputStream is = conn.getInputStream();
//          
//            newString = new Scanner(is).useDelimiter("\\A").next(); 
//            // convert to String
//             
//        } catch (IOException e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//        }
//        
//        return newString;
//        
//    }
    
    
    private class MyWebViewClient extends WebViewClient {
    	
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

        	if (url.startsWith("iap://")) {
        		
//        		showPopUp("in app purchase!");
        		buyLuck(url.substring(6));
        		return true;
        		
        	} else {
        		
        		view.loadUrl(url);
        		return true;
        		
        	}
            
        }
        
    }
    
}


/**
* A {@link PurchaseObserver} is used to get callbacks when Android Market sends
* messages to this application so that we can update the UI.
*/

class MyPurchaseObserver extends PurchaseObserver {
	
	String TAG = "MyPurchaseObserver";
	Activity myActivity;
	
	public MyPurchaseObserver(Activity activity, Handler handler) {
		
		super(activity, handler);
		myActivity = activity;
		Log.i(TAG, "MyPurchaseObserver init");
		
	}

	@Override
	public void onBillingSupported(boolean supported) {
		
		Log.i(TAG, "supported: " + supported);
		
	}

	@Override
	public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
		int quantity, long purchaseTime, String developerPayload) {
	     
		Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId +
			 " " + purchaseState);
	    
	}

	@Override
	public void onRequestPurchaseResponse(RequestPurchase request,
		ResponseCode responseCode) {
	    
	    Log.d(TAG, request.mProductId + ": " + responseCode);
	
	    if (responseCode == ResponseCode.RESULT_OK) {
	
	    	Log.i(TAG, "purchase was successfully sent to server");
	        
	    } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
	    	
	        Log.i(TAG, "user canceled purchase");
	        
	    } else {
	    
	    	Log.i(TAG, "purchase failed");
	        
	    }
	    
	}

	
	@Override
	public void onRestoreTransactionsResponse(RestoreTransactions request,
		ResponseCode responseCode) {
	    
		if (responseCode == ResponseCode.RESULT_OK) {
	    
			Log.d(TAG, "completed RestoreTransactions request");
	        
		} else {
	    
			Log.d(TAG, "RestoreTransactions error: " + responseCode);
	        
		}
	
	}
	
}