package homesweethome.emre.mytracker;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SmsMaps smsMaps ;
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // AJOUT A CETTE LIGNE
        smsMaps = new SmsMaps(MapsActivity.this);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            number= null;
        } else {
            number= extras.getString("number");
        }

        // Recepteur local pour les intents

        this.registerReceiver(smsMaps,new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        double latitude = 48.858053;
        double longitude = 2.294289 ;

        if(Locale.getDefault().getLanguage().equals("tr")){
            latitude = 39.925018;
            longitude = 32.836956;
        }
        if (Locale.getDefault().getLanguage().equals("en")){
            latitude = 37.8078124;
            longitude = -122.47516439999998;
        }
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        mMap.setBuildingsEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng monument = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(monument).title("Monument"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(toureiffel));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(monument,15));
    }


    public  void update(String latitude,String longitude){
        LatLng myPos = new LatLng(Float.valueOf(latitude),Float.valueOf(longitude));
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(myPos).title("My phone"));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myPos,15);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    protected void onDestroy(){
        // on enleve le broadcast receiver
        this.unregisterReceiver(smsMaps);
        Sms mySms = new Sms();
        mySms.sendSms(number,"stopGPS",getApplicationContext());
        super.onDestroy();
    }


}
