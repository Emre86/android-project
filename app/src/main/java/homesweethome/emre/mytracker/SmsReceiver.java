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


public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG =  "SMSReceiver";

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

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
                    String messageOrigin = "";
                    String phoneNumber = "";
                    for (SmsMessage smsMessage :messages) {
                        messageOrigin = messageOrigin + smsMessage.getMessageBody();
                        Log.i(TAG, "Message received: " + smsMessage.getMessageBody());
                        phoneNumber = smsMessage.getDisplayOriginatingAddress();
                    }
                    checkSMS(messageOrigin, phoneNumber, context);

                    //Log.i(TAG, "Message received: " + messages[0].getMessageBody());
                    //String message = messages[0].getMessageBody();
                    //String phoneNumber = messages[0].getDisplayOriginatingAddress();
                    //checkSMS(message, phoneNumber, context);
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


        if (SMS.equals("startGPS")){
            broadcastMessage(context,SMS,phoneNumber);
        }
        if (SMS.equals("stopGPS")){
            broadcastMessage(context,SMS,phoneNumber);
        }
        if (SMS.startsWith("REQUEST:")){
            broadcastMessage(context,SMS,phoneNumber);
        }
        if (SMS.startsWith("RESPONSE:")){
            broadcastMessage(context,SMS,phoneNumber);
        }
        if (SMS.startsWith("GEO:")){
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
