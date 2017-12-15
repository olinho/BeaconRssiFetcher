package fm.lem.aleksandr.beaconfirst;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.google.gson.Gson;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.model.Manager;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = "Olek";
    private ProximityManager kontaktManager;
    private static String API_KEY = "jbfgGaXWMsCCuFoUJSIifXzyGjIhXHcg";

    private File path;
    private File file;
    FileOutputStream stream = null;
    private Button savingBtn;
    private boolean doesSaveData = false;
    private boolean doesSaveData2 = false;
    private boolean doesSaveData3 = false;
    private boolean doesSaveData4 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        oneTimeConfiguration();
        savingBtn = (Button) findViewById(R.id.savingDataBtnId);

        path = getApplicationContext().getExternalFilesDir(null);
        file = new File(path, "pomiary.txt");
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file, true);
            stream.write("liwo na praw 2o".getBytes());
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public void onClickSaveBtn(View view) {
        Log.e(TAG, "onClickSaveBtn: saveData");

//        saveMeasuredData("14.12.");
    }

    private void saveMeasuredData(String data) {
        if (stream != null) {
            try {
                stream.write(data.getBytes());
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                stream = new FileOutputStream(file, true);
                stream.write("marimba".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveMeasurement(com.couchbase.lite.Manager manager, Database db, String docId) {
        Map<String, Object> docContent = new HashMap<String, Object>();
        docContent.put("msg", "first msg");
        docContent.put("measurement", new Measurement("kara", 0.23));
        Log.i(TAG, "saveMeasurement: " + "docContent=" + String.valueOf(docContent));

        Document doc = new Document(db, docId);
        try {
            doc.putProperties(docContent);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private void retrieveMovie(Manager manager, Database couchDb, String docId) {
        Document retrievedDocument = couchDb.getDocument(docId); // Retrieve the document by id
        Object measurementObj = retrievedDocument.getProperties().get("measurement");
        Gson gson = new Gson();
        String jsonString = gson.toJson(measurementObj, Map.class); //Convert the object to json string using Gson
        Measurement measurement = gson.fromJson(jsonString, Measurement.class); //convert the json string to Movie object
        Log.i("json", jsonString);
        Log.i("beaconId: ", measurement.getBeaconId());
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissionAndStart();
    }

    private void oneTimeConfiguration() {
        initializeKontaktSDK();
        kontaktManager = ProximityManagerFactory.create(this);
        kontaktManager.configuration()
                .activityCheckConfiguration(ActivityCheckConfiguration.create(5000,1000))
                .deviceUpdateCallbackInterval(500)
                .scanMode(ScanMode.BALANCED)
                .scanPeriod(ScanPeriod.RANGING)
                ;
        Log.d(TAG, "oneTimeConfiguration: ");
        kontaktManager.setIBeaconListener(customIBeaconListener());
    }

    private IBeaconListener customIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.e(TAG, "onIBeaconDiscovered: " + ibeacon.getUniqueId() + " " + ibeacon.getRssi());
                Log.e(TAG, "onIBeaconDiscovered: " + ibeacon.getUniqueId() + "|" + ibeacon.getRssi()
                        + "|" + ibeacon.getMajor() + "|" + ibeacon.getTxPower()
                );
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion region) {
                for (IBeaconDevice ibeacon : ibeacons) {
                    Log.i(TAG, String.valueOf(ibeacon.getUniqueId()) + "|"
                            + String.valueOf(ibeacon.getRssi()) + "|"
                            + String.valueOf(ibeacon.getTxPower()) + "|"
                            + String.valueOf(ibeacon.getProximityUUID()) + "|"
                                    + String.valueOf(ibeacon.getBatteryPower()) + "|"

                            );

                }
            }
        };
    }




    private void initializeKontaktSDK() {
        KontaktSDK.initialize(API_KEY);
        if (KontaktSDK.isInitialized()){
            Log.d(TAG, "SDK Initialized");
        }
    }

    @Override
    protected void onStop() {
        kontaktManager.stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kontaktManager.disconnect();
        kontaktManager = null;
    }

    private void checkPermissionAndStart() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Arrays.toString(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION}));
        if (PackageManager.PERMISSION_GRANTED == checkSelfPermissionResult) {
            //already granted
            Log.d(TAG,"Permission already granted");
            startScanning();
        }
        else {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            Log.d(TAG,"Permission request called");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (100 == requestCode) {
                Log.d(TAG,"Permission granted");
                startScanning();
            }
        } else
        {
            Log.d(TAG,"Permission not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }

    private void startScanning() {
        kontaktManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                kontaktManager.startScanning();
                Log.d(TAG, "Scanning started");
            }
        });
    }
}
