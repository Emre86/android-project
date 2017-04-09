package homesweethome.emre.mytracker;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.TreeSet;

public class DisplayContact extends AppCompatActivity implements ConstanteContacts {

    private ListView mListView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;
    private ArrayList<String> contactList;
    private TreeSet<String> contactTree;
    private Cursor cursor;
    private int counter;

    private static final String TAG = "DisplayContact" ;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contact);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Readings Contact");
        pDialog.setCancelable(false);
        pDialog.show();
        mListView = (ListView) findViewById(R.id.list);
        updateBarHandler = new Handler();
        // Creation d'un Thread pour l'execution en arriere plan
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContacts();
            }
        }).start();


        // Action lors d'un clic sur l'item en question
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String item = adapterView.getItemAtPosition(i).toString();

                Intent intentDisplayAllNumberContact = new Intent(getApplicationContext(), DisplayAllNumberForAContact.class);
                intentDisplayAllNumberContact.putExtra("nameContact", item);
                startActivity(intentDisplayAllNumberContact);
            }
        });
    }


    public void getContacts() {
        contactTree = new TreeSet<String>();
        StringBuffer output;
        Log.d(TAG,"PASSAGE DANS LE GETCONTACT");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        else {
            ContentResolver contentResolver = getContentResolver();
            cursor = contentResolver.query(CONTENT_URI, null, null, null, null);
            Log.d(TAG, "Nombre de count " + cursor.getCount());
            if (cursor.getCount() > 0) {
                counter = 0;
                while (cursor.moveToNext()) {
                    Log.d(TAG, "je susi dans le while");
                    updateBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
                        }
                    });

                    String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                    if (hasPhoneNumber > 0) {
                        output = new StringBuffer();
                        output.append("" + name);
                        contactTree.add(output.toString());

                    }
                }
                Log.d(TAG, "je suis apres le while");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactList = new ArrayList<String>(contactTree);
                        Locale locale = new Locale("fr");
                        Collections.sort(contactList, Collator.getInstance(locale));
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.contact_listview, R.id.text1, contactList);
                        mListView.setAdapter(adapter);
                    }
                });

                // Rejet du progressBar apres 500 millisecondes
                updateBarHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.cancel();
                    }
                }, 500);
            }
            else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactList = new ArrayList<String>();
                        contactList.add("Aucun contact");
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.contact_listview,R.id.text1,contactList);
                        mListView.setAdapter(adapter);
                    }
                });
                updateBarHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.cancel();
                    }
                },500);

            }
        }
        Log.d(TAG,"apres le if");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                getContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
