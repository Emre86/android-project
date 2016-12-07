package homesweethome.emre.mytracker;
import homesweethome.emre.mytracker.FeedReaderContract.FeedEntry;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;


public class Traceur extends AppCompatActivity {

    private ArrayList<String> values;
    private FeedReaderDBHelper mDbHelper = null;
    private SQLiteDatabase db = null ;
    private ArrayAdapter adapter ;
    private ListView listView ;
    private SharedPreferences sharedPreferences ;
    private String traceurPreferences= "TRACEUR_PREFERENCES";
    private final static String TAG = "Traceur";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traceur);

        /********************** ListView *******/

        values = getValues();

        adapter = new ArrayAdapter<String>(this, R.layout.mycontact_listview, values);

        listView = (ListView) findViewById(R.id.listv);
        listView.setAdapter(adapter);

        registerForContextMenu(listView);


        Log.i(TAG,"Démarrage MyTracker");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    @Override
    protected void onResume(){
        values = getValues();

        adapter = new ArrayAdapter<String>(this, R.layout.mycontact_listview, values);

        listView = (ListView) findViewById(R.id.listv);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        super.onResume();

    }


    public ArrayList<String>  getValues(){
        mDbHelper = new FeedReaderDBHelper(getApplicationContext());
        db = mDbHelper.getReadableDatabase();
        String[] projection = {
                FeedEntry._ID,
                FeedEntry.COLUMN_NAME_NAME,
                FeedEntry.COLUMN_NAME_TEL
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedEntry._ID + " DESC";

        Cursor c = db.query(
                FeedEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<String> values = new ArrayList<String>();

        String val1 = "Vide";
        String val2 ;


        while (c.moveToNext()) {
            val1 = c.getString(c.getColumnIndex(FeedEntry.COLUMN_NAME_NAME));
            val2 = c.getString(c.getColumnIndex(FeedEntry.COLUMN_NAME_TEL));
            if (val1.equals("")) {
                val1 = val2;
            }
            else {
                val1 = val1 + ": " + val2;
            }
            values.add(val1);
        }

        if(values.isEmpty()){
            values.add(val1);
        }


        c.close();
        db.close();

        return values;

    }

    public void afterDelete(){
        db = mDbHelper.getWritableDatabase();
        db.delete(FeedEntry.TABLE_NAME,null,null);
        db.close();

        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    public void deleteOne(String number){
        db = mDbHelper.getWritableDatabase();

        String[] numb = number.split(" ");
        String clauseWhere = FeedEntry.COLUMN_NAME_TEL + " = ?";
        String[] whereArgs = {numb[numb.length-1]};
        db.delete(FeedEntry.TABLE_NAME,clauseWhere,whereArgs);
        db.close();
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }



    public void onCreateContextMenu(ContextMenu menu,View v,ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);
        menu.setHeaderTitle("Action:");
        menu.add(0,v.getId(),0,"Localiser");
        menu.add(0,v.getId(),0,"Supprimer");
    }

    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String number = (String) listView.getItemAtPosition(info.position);


        if (item.getTitle().equals("Localiser")) {
            sharedPreferences = getSharedPreferences(traceurPreferences,Context.MODE_PRIVATE);
            String valueLock = sharedPreferences.getString("Lock","");
            if(valueLock.equals("Lock")){
                String valuePassword = sharedPreferences.getString("PASSWORD","");
                mySendSms(valuePassword,number,true);
            }
            else{
                mySendSms("",number,false);
            }

        }
        else{
            deleteOne(number);
            Toast.makeText(getApplicationContext(),"Supprimé",Toast.LENGTH_SHORT).show();
        }
        return super.onContextItemSelected(item);
    }



    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.optionmenu,menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        sharedPreferences = getSharedPreferences(traceurPreferences, Context.MODE_PRIVATE);
        String valueStart = sharedPreferences.getString("StartOrStop","");

        if (!(valueStart.equals(""))){
            MenuItem menuItem = menu.getItem(4);
            if (valueStart.equals("Start")) {
                valueStart = "Stop";
            }
            else{
                valueStart = "Start";
            }
            menuItem.setTitle(valueStart);
        }

        String valueUnlock = sharedPreferences.getString("Lock","");

        if(!(valueUnlock.equals(""))){

            MenuItem menuItem = menu.getItem(5);
            if(valueUnlock.equals("Unlock")){
                valueUnlock = "Lock";
            }
            else{
                valueUnlock = "Unlock";
            }
            menuItem.setTitle(valueUnlock);
        }
        /*else{
            MenuItem menuItem = menu.getItem(5);
            menuItem.setTitle("Lock");
        }*/
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.ajoutContact:
                Intent displayContact = new Intent(getApplicationContext(), DisplayContact.class);
                startActivity(displayContact);
                return true;
            case R.id.ajoutNumero:
                getBoiteDeDialogueNumero();
                return true;
            case R.id.supprimeTout:
                afterDelete();
                return true;
            case R.id.creerVerrou:
                Intent creerVerrou = new Intent(getApplicationContext(),MyPassword.class);
                startActivity(creerVerrou);
                return true;
            case R.id.StartOrStop:
                sharedPreferences = getSharedPreferences(traceurPreferences, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (item.getTitle().equals("Start")) {
                    editor.putString("StartOrStop","Start");
                    editor.commit();
                    Intent i = new Intent(this,ServiceLock.class);
                    this.startService(i);
                    Log.i(TAG,"Service started");
                    //Toast.makeText(getApplicationContext(),"Service started",Toast.LENGTH_SHORT).show();
                }
                else{
                    editor.putString("StartOrStop","Stop");
                    editor.commit();
                    Intent intent = new Intent("lockMyPhone");
                    intent.putExtra("message","stopService");
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    //Toast.makeText(getApplicationContext(),"Service stoped",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.lock:
                sharedPreferences = getSharedPreferences(traceurPreferences,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor2 = sharedPreferences.edit();
                if(item.getTitle().equals("Lock")){
                    editor2.putString("Lock","Lock");
                }
                else{
                    editor2.putString("Lock","Unlock");
                }
                editor2.commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void mySendSms(final String cle,final String number,boolean isLock){
        if(isLock){
            if (!cle.equals("")) {

                AlertDialog.Builder alertSms = new AlertDialog.Builder(this);
                final EditText editText = new EditText(this);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alertSms.setTitle("Demande de Localisation");
                alertSms.setMessage("Entrez la clé");
                alertSms.setView(editText);
                alertSms.setPositiveButton("Envoi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String ed = editText.getText().toString();
                        if(ed.equals(cle)){
                            //Toast.makeText(getApplicationContext(),"Envoi effectué",Toast.LENGTH_SHORT).show();
                            Sms mySms = new Sms();
                            mySms.sendSms(number,"startGPS",getApplicationContext());
                            Intent mapsActvity = new Intent(getApplicationContext(),MapsActivity.class);
                            mapsActvity.putExtra("number",number);
                            startActivity(mapsActvity);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Clé Invalide",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertSms.show();
            }
            else{
                Intent creerVerrou = new Intent(getApplicationContext(),MyPassword.class);
                startActivity(creerVerrou);
            }
        }
        else{
            Sms mySms = new Sms();
            mySms.sendSms(number,"startGPS",getApplicationContext());
            Intent mapsActvity = new Intent(getApplicationContext(),MapsActivity.class);
            mapsActvity.putExtra("number",number);
            startActivity(mapsActvity);
            Log.i(TAG,"MapsActivity Started");
        }
    }



    public void getBoiteDeDialogueNumero(){
        AlertDialog.Builder alertNumero = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        //alertNumero.setTitle("");
        alertNumero.setMessage("Entrez un numéro");
        alertNumero.setCancelable(true);
        alertNumero.setView(editText);
        alertNumero.setPositiveButton("Enregistrer",new DialogInterface.OnClickListener(){
           @Override
            public void onClick(DialogInterface dialogInterface, int i){
               Editable editable = editText.getText();
               Toast.makeText(getApplicationContext(),""+editable,Toast.LENGTH_SHORT).show();

               FeedReaderDBHelper mDbHelper = new FeedReaderDBHelper(getApplicationContext());

               SQLiteDatabase db = mDbHelper.getWritableDatabase();

               ContentValues values = new ContentValues();
               values.put(FeedEntry.COLUMN_NAME_NAME, "");
               values.put(FeedEntry.COLUMN_NAME_TEL, editable.toString());

               // Insert the new row, returning the primary key value of the new row
               long newRowId;
               newRowId = db.insert(
                       FeedEntry.TABLE_NAME,
                       null,
                       values);

               if (newRowId == -1) {
                   Log.e(TAG, "Echec Insertion");
                   Toast.makeText(getApplicationContext(),"Echec Insertion",Toast.LENGTH_SHORT).show();
               }
               db.close();
               finish();
               overridePendingTransition(0, 0);
               startActivity(getIntent());
               overridePendingTransition(0, 0);
           }
        });
        alertNumero.show();
    }
}
