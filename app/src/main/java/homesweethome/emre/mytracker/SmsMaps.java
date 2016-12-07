package homesweethome.emre.mytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsMaps extends BroadcastReceiver {

    private MapsActivity myMaps;

    private final static String SmsReceiveAction = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

    public SmsMaps(){
    }

    public SmsMaps(MapsActivity myMaps) {
        super();
        this.myMaps = myMaps;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        if (intent.getAction() == SmsReceiveAction){
            Bundle bundle = intent.getExtras();

            if (bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = getIncomingMessage(pdus[i],bundle);
                }


                if (messages.length > -1){
                    //Toast.makeText(context,messages[0].getMessageBody(),Toast.LENGTH_LONG).show();
                    boolean isGPSlatlong = isGPSLatLon(messages[0].getMessageBody());
                    if (isGPSlatlong) {
                        String[] separated = messages[0].getMessageBody().split(" ");
                        String[] separated_info_1 = separated[0].split(":");
                        String[] separated_info_2 = separated[1].split(":");

                        myMaps.update(separated_info_1[1], separated_info_2[1]);
                    }
                }
            }
        }





    }

    public SmsMessage getIncomingMessage(Object pdu, Bundle bundle){
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

    public boolean isGPSLatLon(String message){
        return message.startsWith("Lat:");
    }


}
