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

    private static final String TAG = "MyLocationListener";
    ServiceLock main;

    String phoneNumber;

    MyLocationListener(ServiceLock main,String myPhoneNumber){
        this.main = main ;
        phoneNumber = myPhoneNumber ;
    }

    public void onLocationChanged(Location location){
        Log.i(TAG,"LATITUDE LOC: "+location.getLatitude());
        Log.i(TAG,"LONGITUDE LOC: "+location.getLongitude());

        Sms mySms = new Sms();
        mySms.sendSms(phoneNumber,"Lat:"+location.getLatitude()+ " "+ "Long:"+location.getLongitude(),main.getApplicationContext());
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
