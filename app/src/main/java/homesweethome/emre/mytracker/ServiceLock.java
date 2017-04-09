package homesweethome.emre.mytracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class ServiceLock extends Service {


    private static final String TAG =  "ServiceLock";
    private int milliseconde = 15000;
    private int distance = 10;

    // SMS
    private SmsReceiver mySmsReceiver;
    private BroadcastReceiver msgCom;

    // GPS
    private LocationManager locationManager = null;
    private String provider;
    private MyLocationListener myLocationListener = null;
    private HashMap<String,MyLocationListener> hashmap;

    // DATA
    //String phoneNumber = "0758113909";


    // SHAREDPREFERENCES
    private SharedPreferences sharedPreferences ;
    private String traceurPreferences= "TRACEUR_PREFERENCES";



    public ServiceLock() {
        hashmap = new HashMap<>();
    }

    @Override
    public void onCreate(){
        initSMS();
        initBroadcast();
        Toast.makeText(this,"Service started",Toast.LENGTH_SHORT).show();
        Log.i(TAG,"Service on");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(msgCom);
        unregisterReceiver(mySmsReceiver);
        Log.i(TAG,"Service done");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    private void initBroadcast(){
        msgCom = new MsgCom();
        LocalBroadcastManager.getInstance(this).registerReceiver(msgCom,new IntentFilter("lockMyPhone"));
    }

    private void initSMS(){
        mySmsReceiver = new SmsReceiver();
        this.registerReceiver(mySmsReceiver,new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    private void startGPS(String phoneNumber){

        if (hashmap.size()<3) {
            // le gestionnaire de position
            if (locationManager == null) {
                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                // definition du critère pour sélectionner le fourniseur de position le plus précis
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);

                // renvoie le fournisseur disponible

                provider = locationManager.getBestProvider(criteria, true);

                Log.d(TAG, "provider = " + provider);
            }



            if (provider != null) {
                Location location = null;
                // Derniere position connu par le provider
                try {
                    location = locationManager.getLastKnownLocation(provider);
                } catch (SecurityException s) {
                    Log.e(TAG, "Security exception: ", s);
                }

                Sym symKey = getSymKey(phoneNumber);


                myLocationListener = new MyLocationListener(this, phoneNumber,symKey);
                //hashmap.put(phoneNumber, new MyLocationListener(this,phoneNumber));
                hashmap.put(phoneNumber,myLocationListener);
                myLocationListener = null ;

                if (location != null) {
                    //myLocationListener.onLocationChanged(location);
                    hashmap.get(phoneNumber).onLocationChanged(location);
                }else{
                    location  = new Location(provider);
                    if(Locale.getDefault().getLanguage().equals("fr")){
                        location.setLatitude(48.858053);
                        location.setLongitude(2.294289);
                    }
                    else {
                        location.setLatitude(39.925018);
                        location.setLongitude(32.836956);
                    }
                    hashmap.get(phoneNumber).onLocationChanged(location);
                }




                // condition de mise à jour de la position au moins 10 metres et/ou 15 000 millisecondes
                try {
                    //locationManager.requestLocationUpdates(provider, milliseconde, distance, myLocationListener);
                    locationManager.requestLocationUpdates(provider,milliseconde,distance,hashmap.get(phoneNumber));
                } catch (SecurityException s) {
                    Log.e(TAG, "Security exception: ", s);
                }
            }
        }
        else{
            Log.i(TAG,"Nombre de tracker ");
            Sms sms = new Sms();
            sms.sendSms(phoneNumber,"Nombre de tracker > 3 Impossible de suivre ",getApplicationContext());
        }
    }


    private void stopGPS(String phoneNumber){
        Log.d(TAG,""+phoneNumber);
        if (phoneNumber.equals("all")){
            Set<String> phoneNum = hashmap.keySet();
            for (String sPhoneNum:phoneNum){
                MyLocationListener myLocationListener1 = hashmap.get(sPhoneNum);
                if (locationManager != null && myLocationListener1 != null) {
                    try {
                        locationManager.removeUpdates(myLocationListener1);
                    } catch (SecurityException s) {
                        Log.e(TAG, "Security exception: ", s);
                    }
                }
                hashmap.remove(sPhoneNum);
            }
            locationManager = null;
            hashmap.clear();
        }else {
            MyLocationListener myLocationListener1 = hashmap.get(phoneNumber);
            if (locationManager != null && myLocationListener1 != null) {
                try {
                    locationManager.removeUpdates(myLocationListener1);
                } catch (SecurityException s) {
                    Log.e(TAG, "Security exception: ", s);
                }
            }
            if(hashmap.size() == 1){
                hashmap.remove(phoneNumber);
                locationManager = null ;
            }
            else {
                hashmap.remove(phoneNumber);
            }
        }
    }


    private class MsgCom extends BroadcastReceiver{

        @Override
        public void onReceive(Context context,Intent intent) {
            if (TextUtils.equals(intent.getAction(),"lockMyPhone")){
                String message = intent.getStringExtra("message");
                String phoneNumber = intent.getStringExtra("phoneNumber");
                //Log.d(TAG," Got Message: "+message);


                if (message.equals("stopService")) {
                    stopGPS("all");
                    ServiceLock.this.stopSelf();
                    sharedPreferences = getSharedPreferences(traceurPreferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("StartOrStop", "Stop");
                    editor.commit();
                    Log.i(TAG, " Service stopped");
                }
                else {
                    if (checkTracking(phoneNumber)) {
                        if (message.equals("startGPS")) {
                            startGPS(phoneNumber);
                            Log.i(TAG, "GPS tracking started by: " + phoneNumber);
                        }
                        if (message.equals("stopGPS")) {
                            stopGPS(phoneNumber);
                            Log.i(TAG, "GPS tracking stopped by: " + phoneNumber);
                        }
                        if (message.startsWith("REQUEST:")) {
                            request(phoneNumber, message);
                        }
                        if (message.startsWith("RESPONSE:")) {
                            response(phoneNumber, message);
                        }
                    }
                }
            }
        }
    }


    public void request(String phoneNumber,String message){

        String publicKey = message.substring(8);
        Asym asym = new Asym(publicKey, null);
        Sym sym = new Sym();
        String symKey = sym.getSymKey();

        String encryptedSymKey = asym.encryptKeyAsim(symKey);

        Sms sms = new Sms();
        String response = "RESPONSE:"+encryptedSymKey;
        sms.sendSms(phoneNumber,response,this);

        //writeKey(phoneNumber,symKey);
        writeSymKey(phoneNumber,symKey);
    }

    public void response(String phoneNumber,String message){
        sharedPreferences = getSharedPreferences(traceurPreferences,Context.MODE_PRIVATE);
        String privatecKey = sharedPreferences.getString("PrivateKey","");
        String response = message.substring(9);
        Asym asym = new Asym(null, privatecKey);
        String symKey = asym.decryptKeyAsim(response);

        writeSymKey(phoneNumber,symKey);
    }



    public Sym getSymKey(String phoneNumber) {

        Context myContext = getApplicationContext();

        String databaseName = "FeedReader.db";

        File dbFile = myContext.getDatabasePath(databaseName);

        Sym sym = null;



        if (dbFile.exists()) {

            String selectKey = "SELECT " + FeedReaderContract.FeedEntry.COLUMN_NAME_KEY +","+ FeedReaderContract.FeedEntry.COLUMN_NAME_TEL + " FROM "
                    + FeedReaderContract.FeedEntry.TABLE_NAME ;
                    //" WHERE " + FeedReaderContract.FeedEntry.COLUMN_NAME_TEL + '=' + '"' + phoneNumber + '"';

            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.toString(), null, 0);
            Cursor resultKey = db.rawQuery(selectKey, null);


            if (resultKey != null) {
                Log.d(TAG,"das le if resultkey");
                while(resultKey.moveToNext()) {

                    String symKey = resultKey.getString(0);
                    String phoneTel = resultKey.getString(1);
                    if(comparePhone(phoneNumber,phoneTel)){
                        if (!symKey.equals("")) {
                            sym = new Sym(symKey);
                        } else {
                            Log.d(TAG, "SYMKEY VIDE");
                        }
                        break;
                    }
                }
            }
            else{
                Toast.makeText(this, "Erreur Lecture DB ",Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erreur Lecture DB");
            }
            resultKey.close();
            db.close();
        }
        else {
            Log.e(TAG, "DBFILE  does not exist");
        }
        return sym;
    }



    public void writeSymKey(String phoneNumber,String symkey){

        String []isInDB = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_NAME,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TEL,
                FeedReaderContract.FeedEntry.COLUMN_NAME_KEY};

        //String clauseWhere = FeedReaderContract.FeedEntry.COLUMN_NAME_TEL + " = ?";
        //String[] whereArgs = {phoneNumber};

        FeedReaderDBHelper mDbHelper = new FeedReaderDBHelper(getApplicationContext());

        SQLiteDatabase db = mDbHelper.getWritableDatabase();


        //Cursor cursor = db.query(FeedReaderContract.FeedEntry.TABLE_NAME,isInDB,clauseWhere,whereArgs,null,null,null);
        Cursor cursor = db.query(FeedReaderContract.FeedEntry.TABLE_NAME,isInDB,null,null,null,null,null);

        int count = cursor.getCount();



        if (count!=0){
            do {
                cursor.moveToFirst();
                String id = cursor.getString(0);

                String name = cursor.getString(1);

                String number = cursor.getString(2);

                if(comparePhone(phoneNumber,number)){

                    ContentValues values =  new ContentValues();
                    values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME,name);
                    values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TEL,number);
                    values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_KEY,symkey);
                    db.update(FeedReaderContract.FeedEntry.TABLE_NAME,
                            values,null,null);

                }



            }
            while(cursor.moveToNext());


        }
        else{
            Log.e(TAG, "Erreur  COUNT == 0");
        }
        cursor.close();
        db.close();

    }



    public boolean comparePhone(String phone1,String phone2){
        String subPhone1 = phone1.substring(phone1.length()-9,phone1.length());
        String subPhone2 = phone2.substring(phone2.length()-9,phone2.length());
        Log.d(TAG,subPhone1);
        Log.d(TAG,subPhone2);
        return subPhone1.equals(subPhone2);
    }

    public boolean checkTracking(String phoneNumber) {
        boolean trackingOK = false;

        Context myContext = getApplicationContext();

        String databaseName = "FeedReader.db";

        File dbFile = myContext.getDatabasePath(databaseName);

        if (dbFile.exists()) {

            String selectNumber = "SELECT " + FeedReaderContract.FeedEntry.COLUMN_NAME_TEL + " FROM " + FeedReaderContract.FeedEntry.TABLE_NAME;
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.toString(), null, 0);
            Cursor resultNumber = db.rawQuery(selectNumber, null);
            if (resultNumber != null) {
                while (resultNumber.moveToNext()) {
                    String number = resultNumber.getString(0);
                    if(comparePhone(phoneNumber,number)){
                        trackingOK = true;
                        break;
                    }
                }
            }
            resultNumber.close();
            db.close();
        } else {
            Log.d(TAG, "DBFILE does not exist");
        }
        return trackingOK;
    }

}
