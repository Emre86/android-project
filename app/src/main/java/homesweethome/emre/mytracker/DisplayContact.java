package homesweethome.emre.mytracker;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


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


        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(CONTENT_URI,null,null,null,null);
        if (cursor.getCount() > 0){
            counter = 0;
            while(cursor.moveToNext()){
                updateBarHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.setMessage("Reading contacts : " + counter++ +"/"+ cursor.getCount());
                    }
                });

                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0){
                    output = new StringBuffer();
                    output.append(""+name);
                    contactTree.add(output.toString());

                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    contactList = new ArrayList<String>(contactTree);
                    Locale locale = new Locale("fr");
                    Collections.sort(contactList, Collator.getInstance(locale));
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.contact_listview,R.id.text1,contactList);
                    mListView.setAdapter(adapter);
                }
            });

            // Rejet du progressBar apres 500 millisecondes
            updateBarHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.cancel();
                }
            },500);
        }
    }

}
