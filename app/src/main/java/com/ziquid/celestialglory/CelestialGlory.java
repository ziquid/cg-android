package com.ziquid.celestialglory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.ziquid.util.Purchase;
import com.ziquid.util.IabHelper;
import com.ziquid.util.IabHelper.IabAsyncInProgressException;
import com.ziquid.util.IabResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

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
  public String urlHome;

  private static final Random RNG = new Random();

  private PackageInfo pInfo = null;
// MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuzoxjOWZTfffcUacQYKLpVWGmNw2XLbdZUOhbcrBTFTuQaP90sGpduBQZt7QYNqQv8A1O4x1mcwRR0tCwUBvD+qZoJypyAVmF5COQzurPH34TEJFD+Xi8blpcU6SIg9UHBlPCBvlx3fU52NPA9+SDMfA1Mm+BwopzQ8Jeo6GWamSITz8XJUhZmtKTGy111xyR0MSdPV+IHisIXcC7C4SR2nGaJ6wyR3mJcMHAXO5XsxMx5lHIL4XJjdw13vhmhXpnE7r/IcN9wsdMICGI5Y+4vqvjoC697b0+6GHyhwRRViKTFrz0TiKkOypAf9lHMFXXKUCfY/XQgtx8UTOmo4xJwIDAQAB
  private String IAPPublicKey0 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA";
  private String IAPPublicKey1 = "uzoxjOWZTfffcUacQYKLpVWGmNw2XLbdZUOhbcrBTFTu";

  private String IAPPublicKey8 = "kOypAf9lHMFXXKUCfY/XQgtx8UTOmo4xJwIDAQAB";
  private String IAPPublicKey9 = "MeI4gowIDAQAB";

  private String IAPPublicKey3 = "oJypyAVmF5COQzurPH34TEJFD+Xi8blpcU6SIg9UHBlP";
  private String IAPPublicKey4 = "CBvlx3fU52NPA9+SDMfA1Mm+BwopzQ8Jeo6GWamSITz8";

  private String IAPPublicKey2 = "QaP90sGpduBQZt7QYNqQv8A1O4x1mcwRR0tCwUBvD+qZ";
  private String IAPPublicKey7 = "9wsdMICGI5Y+4vqvjoC697b0+6GHyhwRRViKTFrz0TiK";

  private String IAPPublicKey5 = "XJUhZmtKTGy111xyR0MSdPV+IHisIXcC7C4SR2nGaJ6w";
  private String IAPPublicKey6 = "yR3mJcMHAXO5XsxMx5lHIL4XJjdw13vhmhXpnE7r/IcN";

  private IabHelper mHelper;
//    private MyPurchaseObserver mMyPurchaseObserver;
//    private Handler mHandler;
//    private BillingService mBillingService;


  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (BuildConfig.DEBUG) {
      urlHome = getString(R.string.url_debug_home);
    }
    else {
      urlHome = getString(R.string.url_home);
    }
    setContentView(R.layout.main);

    engine = (WebView) findViewById(R.id.browser);
    engine.clearCache(true); // start by clearing the cache
    // splashScreen = (FrameLayout) findViewById(R.id.splash_frame);
//        handler = new Handler();

    androidID = "" + android.provider.Settings.Secure.getString(getContentResolver(),
      android.provider.Settings.Secure.ANDROID_ID);
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

    // set up IAPs v3
    mHelper = new IabHelper(this, IAPPublicKey0 + IAPPublicKey1 +
      IAPPublicKey2 + IAPPublicKey3 + IAPPublicKey4 + IAPPublicKey5 +
      IAPPublicKey6 + IAPPublicKey7 + IAPPublicKey8);

    // enable debug logging (for a production application, you should set this to false).
    if (BuildConfig.DEBUG) {
      mHelper.enableDebugLogging(true);
    }

    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          Log.i(TAG, "Problem setting up In-app Billing: " + result);
        } else {
          Log.i(TAG, "Billing supported.");
        }
      }
    });

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

      pInfo = getPackageManager().getPackageInfo("com.ziquid.celestialglory",
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
          " (com.ziquid.celestialglory/Unknown/Unknown" +
          billingSupportString + ')');

    }

    engine.setWebViewClient(new MyWebViewClient());

/*        removeSplash = new Runnable() {

            public void run() {
            	engine.loadUrl(urlHome) + androidID);
                splashScreen.setVisibility(View.GONE);                       
            }
            
        };

        handler.postDelayed(removeSplash, 4000);
*/

    // FIXME: vertical centering of the splash doesn't work

    String data = "<html>" +
      "<head><meta http-equiv=\"refresh\"" +
      " content=\"1;url=" + urlHome +
      androidID + "\"/></head> " +
      "<body style=\"margin: 0px; padding: 0px; background-color: black; " +
      "color: white;\"><span style=\"display: table-cell; vertical-align: middle;\">" +
      "<img width=\"" + usableWidth + "\" style=\"display: inline-block;\" " +
      "src=\"file:///android_asset/cg_splash.jpg\"></span></body>" +
      "</html>";

    // splashScreen.setVisibility(View.GONE);

    engine.loadDataWithBaseURL("Fake://url.com/", data, "text/html",
      "UTF-8", "");

//        engine.loadUrl(urlHome + androidID);

  }


  @Override
  protected void onStart() {
    super.onStart();
  }


  @Override
  protected void onStop() {
    super.onStop();
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mHelper != null) try {
      mHelper.dispose();
    } catch (IabAsyncInProgressException e) {
      e.printStackTrace();
    }
    mHelper = null;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

// Pass on the activity result to the helper for handling
    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
      super.onActivityResult(requestCode, resultCode, data);
    }
    else {
      Log.i(TAG, "onActivityResult handled by IABUtil.");
    }
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

//    menu.add(0, MENU_FORUM, 5,
//      R.string.menu_forum)
//      .setIcon(R.drawable.cg_forum);

    return result;
  } // onCreateOptionsMenu


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case MENU_HOME:
//    		engine.clearCache(true); // clear the cache whenever we go home
        engine.loadUrl(urlHome + androidID);
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

//      case MENU_FORUM:
//    		engine.loadUrl(getString(R.string.url_forum));
//        buyLuck("luck__10"); // for testing
//        return true;

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
      !engine.getUrl().equals(urlHome + androidID)) {

      engine.clearHistory();
      engine.loadUrl(urlHome + androidID);
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

    try {
      mHelper.launchPurchaseFlow(this, sku, 10001, mPurchaseFinishedListener,
        androidID);
    } catch (IabAsyncInProgressException e) {
      e.printStackTrace();
    }

    engine.loadUrl(urlHome + androidID);

  }


  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
    = new IabHelper.OnIabPurchaseFinishedListener() {

    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
      if (result.isFailure()) {
        Log.d(TAG, "Error purchasing: " + result);
        showPopUp("Error purchasing: " + result);
        return;
      }
      else if (!purchase.getDeveloperPayload().equals(androidID)) {
        Log.d(TAG, "Error purchasing for ID " + purchase.getDeveloperPayload());
        showPopUp("Error purchasing for ID " + purchase.getDeveloperPayload());
        return;
      }

      try {
        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
      } catch (IabAsyncInProgressException e) {
        e.printStackTrace();
      }

    }
  };

  IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
    new IabHelper.OnConsumeFinishedListener() {
      public void onConsumeFinished(Purchase purchase, IabResult result) {
        if (result.isSuccess()) {

          new submitLuckTask().execute(getString(R.string.url_purchase) +
            androidID +	'/' + purchase.getSku());

        }
        else {
          Log.d(TAG, result.toString());
        }
      }
    };


  private String downloadString(String fileUrl) {

    URL myFileUrl = null;
    String newString = "";

    try {
      myFileUrl= new URL(fileUrl);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      HttpURLConnection conn =
        (HttpURLConnection) myFileUrl.openConnection();
      conn.setDoInput(true);
      conn.setRequestProperty("User-Agent",
        "com.ziquid.celestialglory luckloader");
      conn.connect();
//             int length = conn.getContentLength();
      InputStream is = conn.getInputStream();

      newString = new Scanner(is).useDelimiter("\\A").next();
      // convert to String

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return newString;
  }


  private class submitLuckTask extends AsyncTask<String, Integer, String> {

    protected String doInBackground(String... fileUrl) {
      return downloadString(fileUrl[0]);
    }

    protected void onPostExecute(String result) {

      if (BuildConfig.DEBUG) {
        Log.d(TAG, "submitLuckTask result: " + result);
      }

//      showPopUp("submitLuckTask result: " + result);
    }
  }


  private class MyWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

      if (url.startsWith("iap://")) {

//        showPopUp("in app purchase!");
        buyLuck(url.substring(6));
        return true;

      } else {
        view.loadUrl(url);
        return true;
      }

    }

  }

}
