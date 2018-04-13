package mobi.pdf417.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.microblink.MicroblinkSDK;
import com.microblink.entities.recognizers.RecognizerBundle;
import com.microblink.entities.recognizers.blinkbarcode.barcode.BarcodeRecognizer;
import com.microblink.uisettings.ActivityRunner;
import com.microblink.uisettings.BarcodeUISettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pdf417MobiDemoActivity extends Activity {

    private static final int MY_REQUEST_CODE = 1337;

    /**
     * Barcode recognizer that will perform recognition of images
     */
    private BarcodeRecognizer mBarcodeRecognizer;

    /**
     * Recognizer bundle that will wrap the barcode recognizer in order for recognition to be performed
     */
    private RecognizerBundle mRecognizerBundle;
    private TextView messageBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setupVersionTextView();
        messageBox = findViewById(R.id.message);

        // You have to enable recognizers and barcode types you want to support
        // Don't enable what you don't need, it will significantly decrease scanning performance
        mBarcodeRecognizer = new BarcodeRecognizer();
        mBarcodeRecognizer.setScanPDF417(true);
        //mBarcodeRecognizer.setScanQRCode(true);

        mRecognizerBundle = new RecognizerBundle(mBarcodeRecognizer);
    }

    private String getSsid() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    private Boolean isValidSsid() {
        // Should be like Pin.tv something
        Pattern p = Pattern.compile("/^[P-p][I-i][N-n].[T-t][V-v].*$/g");
        Matcher m = p.matcher(getSsid());
        return m.matches();
    }

    public void onScanButtonClick(View v) throws MalformedURLException, JSONException {
        Log.d("WIFI", getSsid());

        if (isValidSsid() == true) {
        //if (isValidSsid() == false) {
            // start default barcode scanning activity
            messageBox.setText("Red correcta.");
            BarcodeUISettings uiSettings = new BarcodeUISettings(mRecognizerBundle);
            uiSettings.setBeepSoundResourceID(R.raw.beep);
            ActivityRunner.startActivityForResult(this, MY_REQUEST_CODE, uiSettings);

        } else {
            messageBox.setText("Debe conectarse a una red Pin.tv para poder utilizar el escaner.");
            Log.d("Invalido", getSsid());

            //api.doInBackground("http://192.168.1.117:5000/ping","{'probando':'acava'}");
            //api.doInBackground("http://google.com","ahiva");

        }
    }

    private String makeJSON(String strelement) throws JSONException
    {
        String message;
        JSONObject json = new JSONObject();
        json.put("JSON", strelement);
        message = json.toString();
        return message;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //handleScanResultIntent(data);
            CallAPI api = new CallAPI();
            try {
                mRecognizerBundle.loadFromIntent(data);
                // after calling mRecognizerBundle.loadFromIntent, results are stored in mBarcodeRecognizer
                BarcodeRecognizer.Result result = mBarcodeRecognizer.getResult();
                api.execute("http://192.168.1.28:5000/info", makeJSON(result.getStringData()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
/*
    private void handleScanResultIntent(Intent data) {
        // updates bundled recognizers with results that have arrived
        mRecognizerBundle.loadFromIntent(data);
        // after calling mRecognizerBundle.loadFromIntent, results are stored in mBarcodeRecognizer
        BarcodeRecognizer.Result result = mBarcodeRecognizer.getResult();
        Log.d("titulo","Llego aca asi: ");
        Log.d("titulo",result.getStringData());
        //do what you want with the result
        if (URLUtil.isValidUrl(result.getStringData()))
        {
            CallAPI api = new CallAPI();
            try {
                api.execute("http://192.168.1.117:5000/info", makeJSON(result.getStringData()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //openScanResultInBrowser(result);
        }

        } else {
            shareScanResult(result);
        }

    }
*/
/*
    private void openScanResultInBrowser(BarcodeRecognizer.Result result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(result.getStringData()));
        startActivity(Intent.createChooser(intent, getString(R.string.UseWith)));
    }
*/
    /*
    private void shareScanResult(BarcodeRecognizer.Result result) {
        StringBuilder sb = new StringBuilder(result.getBarcodeFormat().name());
        sb.append("\n\n");

        if (result.isUncertain()) {
            sb.append("\nThis scan data is uncertain!\n\nString data:\n");
        }
        sb.append(result.getStringData());

        sb.append("\nRaw data:\n");
        sb.append(Arrays.toString(result.getRawData()));
        sb.append("\n\n\n");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(intent, getString(R.string.UseWith)));
    }
    */

    /**
     * Builds string which contains information about application version and library version.
     */
    /*
    private void setupVersionTextView() {
        String versionString;

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String appVersion = packageInfo.versionName;
            int appVersionCode = packageInfo.versionCode;

            versionString = "Application version: " +
                    appVersion +
                    ", build " +
                    appVersionCode +
                    "\nLibrary version: " +
                    MicroblinkSDK.getNativeLibraryVersionString();
        } catch (NameNotFoundException e) {
            versionString = "";
        }
    }
    */

}
