package homesweethome.emre.mytracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class ServiceLock extends Service {

    private static final String TAG = "ServiceLock";
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
        //Log.d(TAG,"Service started");
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
        Log.d(TAG,"Service done");
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

                myLocationListener = new MyLocationListener(this, phoneNumber);
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

                if(message.equals("stopService")){
                    stopGPS("all");
                    ServiceLock.this.stopSelf();
                    sharedPreferences = getSharedPreferences(traceurPreferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("StartOrStop","Stop");
                    editor.commit();
                    Log.i(TAG," Service stopped");
                }
                else if (message.equals("startGPS")){
                    startGPS(phoneNumber);
                    Log.i(TAG, "GPS tracking started by: "+phoneNumber);
                }
                else if(message.equals("stopGPS")){
                    stopGPS(phoneNumber);
                    Log.i(TAG, "GPS tracking stopped by: "+phoneNumber);
                }
            }
        }
    }

}
