package homesweethome.emre.mytracker;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by emre on 04/09/16.
 */
public class MyLocationListener implements LocationListener {

    private final static String APP = "MYTRACKER:" ;
    private static final String TAG = APP + "MyLocListener";
    ServiceLock main;

    String phoneNumber;

    Sym symKey ;

    MyLocationListener(ServiceLock main,String myPhoneNumber,Sym symKey ){
        this.main = main ;
        phoneNumber = myPhoneNumber ;
        this.symKey = symKey;
    }

    public void onLocationChanged(Location location){
        Log.i(TAG,"LAT:"+location.getLatitude());
        Log.i(TAG,"LON:"+location.getLongitude());

        Sms mySms = new Sms();
        String localisation = "Lat:"+location.getLatitude()+ " "+ "Long:"+location.getLongitude();
        if(symKey == null){
            Log.e(TAG,"SymKey is Null");
        }
        String geoposition = "GEOPOSITION:" + symKey.encrypt(localisation);
        mySms.sendSms(phoneNumber,geoposition,main.getApplicationContext());
    }


    public void onStatusChanged(String provider, int status, Bundle extras){
        //Toast.makeText(main,provider+"'Status changed to "+status+"!",Toast.LENGTH_SHORT).show();
    }

    public void onProviderEnabled(String provider){
        //Toast.makeText(main,"Provider "+provider+" enabled!",Toast.LENGTH_SHORT).show();

    }

    public void onProviderDisabled(String provider){
        //Toast.makeText(main,"Provider "+provider+" disabled!",Toast.LENGTH_SHORT).show();
    }

}
