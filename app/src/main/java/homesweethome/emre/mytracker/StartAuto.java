package homesweethome.emre.mytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartAuto extends BroadcastReceiver {

    private final static String TAG = "StartAuto";

    public StartAuto() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        Intent i = new Intent(context,ServiceLock.class);
        context.startService(i);
        Log.i(TAG,"Demarrage de ServiceLock");

    }
}
