package homesweethome.emre.mytracker;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsManager;

import java.util.ArrayList;

/**
 * Created by emre on 23/08/16.
 */
public class Sms {


    private final static String TAG =  "Sms";
    private final static int PERMISSIONS_REQUEST_SEND_SMS = 101;

    public void sendSms(String phoneNumber,String message, final Context myContext){


        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(myContext,0,new Intent(SENT),0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(myContext,0,new Intent(DELIVERED),0);

        myContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Context cont = myContext.getApplicationContext();
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                       // Toast.makeText(cont,"SMS Sent",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(cont,"Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(cont,"No service",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(cont,"Null PDU",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(cont,"Radio Off",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        },new IntentFilter(SENT));

        myContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Context cont = myContext.getApplicationContext();
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        //Toast.makeText(cont,"SMS delivered",Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(cont,"SMS not delivered",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();


        ArrayList<String> messages = sms.divideMessage(message);

        ArrayList<PendingIntent> sentPIs  = new ArrayList<>();
        ArrayList<PendingIntent> deliveredPIs = new ArrayList<>();

        //sms.sendTextMessage(phoneNumber,null,message,sentPI,deliveredPI);
        for (int i = 0; i < messages.size();i++ ){
            sentPIs.add(sentPI);
            deliveredPIs.add(deliveredPI);
        }

        sms.sendMultipartTextMessage(phoneNumber,null,messages,sentPIs,deliveredPIs);

        Log.d(TAG,"seres");
    }

}
