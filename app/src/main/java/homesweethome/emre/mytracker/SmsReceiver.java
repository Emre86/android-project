package homesweethome.emre.mytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    public static final String TAG = "SMSReceiver";

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.i(TAG,"Intent Received: "+intent.getAction());

        Log.i(TAG,"VALEUR DE TELEPHON: " + Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

        if (intent.getAction() == Telephony.Sms.Intents.SMS_RECEIVED_ACTION){
        // if (intent.getAction() == android.provider.Telephony.SMS_RECEIVED)
            Bundle bundle = intent.getExtras();
            //String info = intent.getStringExtra("format");

            if (bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                        //messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], info);
                    messages[i] = getIncomingMessage(pdus[i],bundle);
                }


                if (messages.length > -1){
                    Log.i(TAG,"Message received: "+messages[0].getMessageBody());
                    //Toast.makeText(context,messages[0].getMessageBody(),Toast.LENGTH_SHORT).show();
                    String message = messages[0].getMessageBody();
                    String phoneNumber = messages[0].getDisplayOriginatingAddress();
                    checkSMS(message,phoneNumber,context);
                }
            }
        }
        //throw new UnsupportedOperationException("Not yet implemented");

    }



    public SmsMessage getIncomingMessage(Object pdu,Bundle bundle){
        SmsMessage currentSMS ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[])pdu,format);
        }
        else{
            currentSMS = SmsMessage.createFromPdu((byte[])pdu);
        }
        return currentSMS;
    }



    public void checkSMS(String SMS,String phoneNumber,Context context){
        /*if(SMS.equals("stopService")){
            broadcastMessage(context,SMS,phoneNumber);
        }*/
        if (SMS.equals("startGPS")){
            broadcastMessage(context,SMS,phoneNumber);
        }
        if (SMS.equals("stopGPS")){
            broadcastMessage(context,SMS,phoneNumber);
        }
    }

    public void broadcastMessage(Context context,String msg,String phoneNumber){
        Log.d(TAG,"Broadcasting message");
        Intent intent = new Intent("lockMyPhone");
        intent.putExtra("message",msg);
        intent.putExtra("phoneNumber",phoneNumber);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }


}
