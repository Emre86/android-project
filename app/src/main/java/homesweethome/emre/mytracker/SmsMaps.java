package homesweethome.emre.mytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class SmsMaps extends BroadcastReceiver {

    private MapsActivity myMaps;

    private final static String SmsReceiveAction = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;
    private final static String TAG = "SmsMaps";

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

                        String phoneNumber = messages[0].getDisplayOriginatingAddress();

                        Sym symKey = getSymKey(phoneNumber,context);

                        String decrypt = symKey.decrypt(messages[0].getMessageBody().substring(12));

                        String[] separated = decrypt.split(" ");
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
        return message.startsWith("GEOPOSITION:");
    }

    public Sym getSymKey(String phoneNumber, Context context) {



        String databaseName = "FeedReader.db";

        File dbFile = context.getDatabasePath(databaseName);

        Sym sym = null;

        if (dbFile.exists()) {

            String selectKey = "SELECT " + FeedReaderContract.FeedEntry.COLUMN_NAME_KEY + ","
                    + FeedReaderContract.FeedEntry.COLUMN_NAME_TEL + " FROM "
                    + FeedReaderContract.FeedEntry.TABLE_NAME ;
            //+ " WHERE " + FeedReaderContract.FeedEntry.COLUMN_NAME_TEL + '=' + '"' + phoneNumber + '"';

            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.toString(), null, 0);
            Cursor resultKey = db.rawQuery(selectKey, null);

            if(resultKey != null) {
                while (resultKey.moveToNext()) {
                    String symKey = resultKey.getString(0);
                    String phoneTel = resultKey.getString(1);
                    if(comparePhone(phoneNumber,phoneTel)) {
                        if (!symKey.equals("")) {
                            sym = new Sym(symKey);
                        }
                        break;
                    }
                }
            }
            else{
                Log.d(TAG, "pas de result key");
            }

            resultKey.close();
            db.close();
        } else {
            Log.d(TAG, "DBFILE does not exist");
        }
        return sym;
    }

    public boolean comparePhone(String phone1,String phone2){
        String subPhone1 = phone1.substring(phone1.length()-9,phone1.length());
        String subPhone2 = phone2.substring(phone2.length()-9,phone2.length());
        Log.d(TAG,subPhone1);
        Log.d(TAG,subPhone2);
        return subPhone1.equals(subPhone2);
    }


}
