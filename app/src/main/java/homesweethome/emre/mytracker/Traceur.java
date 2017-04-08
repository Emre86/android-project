package homesweethome.emre.mytracker;
import homesweethome.emre.mytracker.FeedReaderContract.FeedEntry;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;



public class Traceur extends AppCompatActivity {

    private ArrayList<String> values;
    private FeedReaderDBHelper mDbHelper = null;
    private SQLiteDatabase db = null ;
    private ArrayAdapter adapter ;
    private ListView listView ;
    private SharedPreferences sharedPreferences ;
    private String traceurPreferences= "TRACEUR_PREFERENCES";

    private final static String TAG =  "Traceur";
    private final static int PERMISSIONS_ALL_NEEDED_APPLICATIONS = 101;

    private boolean haveAllNeededPermissions = false;
    private String[] allNeededPermissions = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traceur);

        SharedPreferences prefs = getSharedPreferences(traceurPreferences, Context.MODE_PRIVATE);
        if (!prefs.getBoolean("firstTime",false)) {
            Asym asym = new Asym();
            String publicKey = asym.getPublicKeyAsym();
            String privateKey = asym.getPrivateKeyAsym();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.putString("PublicKey",publicKey);
            editor.putString("PrivateKey",privateKey);
            editor.commit();
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            for (String perm: allNeededPermissions) {
                if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(allNeededPermissions, PERMISSIONS_ALL_NEEDED_APPLICATIONS);
                    break;
                }
            }
            haveAllNeededPermissions = true;
        }
        else{
            haveAllNeededPermissions = true;
        }


        /********************** ListView *******/
        if (haveAllNeededPermissions) {
            values = getValues();

            //checkTracking();

            adapter = new ArrayAdapter<String>(this, R.layout.mycontact_listview, values);

            listView = (ListView) findViewById(R.id.listv);
            listView.setAdapter(adapter);

            registerForContextMenu(listView);

            Log.i(TAG, "Démarrage MyTracker");
        }
        else{
            Toast.makeText(getApplicationContext()," Veuillez Accepter toutes les permisions nécessaires aux fonctionnements de l'application",Toast.LENGTH_SHORT).show();
            //finish();
        }
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
        menu.add(0,v.getId(),0,"Echange de Clé");
    }

    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String number = (String) listView.getItemAtPosition(info.position);

        sharedPreferences = getSharedPreferences(traceurPreferences,Context.MODE_PRIVATE);
        String valueLock = sharedPreferences.getString("Lock","");


        if (item.getTitle().equals("Localiser")) {
           // sharedPreferences = getSharedPreferences(traceurPreferences,Context.MODE_PRIVATE);
           // String valueLock = sharedPreferences.getString("Lock","");

            if (checkKey(number)) {
                if (valueLock.equals("Lock")) {
                    String valuePassword = sharedPreferences.getString("PASSWORD", "");
                    mySendSms(valuePassword, number, true);
                } else {
                    mySendSms("", number, false);
                }
            }
            else{
                Toast.makeText(this,"Veuillez procéder à l'échange de clé",Toast.LENGTH_SHORT).show();
            }

        }

        if (item.getTitle().equals("Supprimer")){
            deleteOne(number);
            Toast.makeText(getApplicationContext(),"Supprimé",Toast.LENGTH_SHORT).show();
        }

        if (item.getTitle().equals("Echange de Clé")){
            Log.d(TAG, "APPUIE SUR ECHANGE DE CLE ");
            if (valueLock.equals("Lock")) {
                String valuePassword = sharedPreferences.getString("PASSWORD", "");
                mySendSmsRequest(valuePassword, number, true);
            } else {
                mySendSmsRequest("", number, false);
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_ALL_NEEDED_APPLICATIONS) {
            haveAllNeededPermissions = true;
            for (int grant :grantResults){
                if (grant != PackageManager.PERMISSION_GRANTED){
                    haveAllNeededPermissions = false;
                    break;
                }
            }
            if (haveAllNeededPermissions) {
                values = getValues();

                adapter = new ArrayAdapter<String>(this, R.layout.mycontact_listview, values);

                listView = (ListView) findViewById(R.id.listv);
                listView.setAdapter(adapter);

                registerForContextMenu(listView);


                Log.i(TAG, "Démarrage MyTracker");
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void mySendSms(final String cle,final String number,boolean isLock){
        if (isLock) {
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
                        if (ed.equals(cle)) {
                            //Toast.makeText(getApplicationContext(),"Envoi effectué",Toast.LENGTH_SHORT).show();
                            Sms mySms = new Sms();
                            mySms.sendSms(number, "startGPS", getApplicationContext());
                            Intent mapsActvity = new Intent(getApplicationContext(), MapsActivity.class);
                            mapsActvity.putExtra("number", number);
                            startActivity(mapsActvity);
                        } else {
                            Toast.makeText(getApplicationContext(), "Clé Invalide", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertSms.show();
            } else {
                Intent creerVerrou = new Intent(getApplicationContext(), MyPassword.class);
                startActivity(creerVerrou);
            }
        } else {
            Sms mySms = new Sms();
            mySms.sendSms(number, "startGPS", getApplicationContext());
            Intent mapsActvity = new Intent(getApplicationContext(), MapsActivity.class);
            mapsActvity.putExtra("number", number);
            startActivity(mapsActvity);
            Log.i(TAG, "MapsActivity Started");
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
               if(editable.toString().length()<9){
                   Toast.makeText(getApplicationContext(),"Numéro non valide",Toast.LENGTH_SHORT ).show();
               }
               else {
                   //Toast.makeText(getApplicationContext(),""+editable,Toast.LENGTH_SHORT).show();

                   FeedReaderDBHelper mDbHelper = new FeedReaderDBHelper(getApplicationContext());

                   SQLiteDatabase db = mDbHelper.getWritableDatabase();

                   ContentValues values = new ContentValues();
                   values.put(FeedEntry.COLUMN_NAME_NAME, "");
                   values.put(FeedEntry.COLUMN_NAME_TEL, editable.toString());
                   values.put(FeedEntry.COLUMN_NAME_KEY, "");

                   // Insert the new row, returning the primary key value of the new row
                   long newRowId;
                   newRowId = db.insert(
                           FeedEntry.TABLE_NAME,
                           null,
                           values);

                   if (newRowId == -1) {
                       Log.e(TAG, "Echec Insertion");
                       Toast.makeText(getApplicationContext(), "Echec Insertion", Toast.LENGTH_SHORT).show();
                   }
                   db.close();
                   finish();
                   overridePendingTransition(0, 0);
                   startActivity(getIntent());
                   overridePendingTransition(0, 0);
               }
           }
        });
        alertNumero.show();
    }



    public boolean checkKey(String phoneNumber) {
        boolean keyOK = false;
        Context myContext = getApplicationContext();

        String databaseName = "FeedReader.db";

        File dbFile = myContext.getDatabasePath(databaseName);


        if (dbFile.exists()) {

            String selectKey = "SELECT " + FeedReaderContract.FeedEntry.COLUMN_NAME_KEY + ", " +  FeedEntry.COLUMN_NAME_TEL + " FROM "
                    + FeedReaderContract.FeedEntry.TABLE_NAME ;

            //String selectNumber = "SELECT " + FeedReaderContract.FeedEntry.COLUMN_NAME_TEL + " FROM " + FeedReaderContract.FeedEntry.TABLE_NAME;

            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.toString(), null, 0);
            Cursor resultKey = db.rawQuery(selectKey, null);

            if (resultKey != null) {
                while (resultKey.moveToNext()) {
                    String symKey = resultKey.getString(0);
                    String phoneNumberKey = resultKey.getString(1);

                    if (comparePhone(phoneNumberKey,phoneNumber)) {
                        Log.d(TAG, "COMPARE OK ");
                        if (!symKey.equals("")) {
                            keyOK = true;
                        }
                        break;
                    }
                }
            }
            else{
                Toast.makeText(this, "Contact non disponible dans la base de donnée",Toast.LENGTH_SHORT).show();
            }
            resultKey.close();
            db.close();
        } else {
            Log.d(TAG, "DBFILE does not exist");
        }
        return keyOK;
    }


    public void mySendSmsRequest(final String cle,final String number,boolean isLock){

        sharedPreferences = getSharedPreferences(traceurPreferences,Context.MODE_PRIVATE);
        String publicKey = sharedPreferences.getString("PublicKey","");

        final String message = "REQUEST:" + publicKey ;
        //final String message = "Request:";
        Log.d(TAG,message);
        if (isLock) {
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
                        if (ed.equals(cle)) {
                            //Toast.makeText(getApplicationContext(),"Envoi effectué",Toast.LENGTH_SHORT).show();
                            Sms mySms = new Sms();
                            mySms.sendSms(number, message , getApplicationContext());

                        } else {
                            Toast.makeText(getApplicationContext(), "Clé Invalide", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertSms.show();
            } else {
                Intent creerVerrou = new Intent(getApplicationContext(), MyPassword.class);
                startActivity(creerVerrou);
            }
        } else {
            Sms mySms = new Sms();
            mySms.sendSms(number, message, getApplicationContext());
        }

    }

    public boolean comparePhone(String phone1,String phone2){
        String subPhone1 = phone1.substring(phone1.length()-9,phone1.length());
        String subPhone2 = phone2.substring(phone2.length()-9,phone2.length());
        Log.d(TAG,subPhone1);
        Log.d(TAG,subPhone2);
        return subPhone1.equals(subPhone2);
    }



}
