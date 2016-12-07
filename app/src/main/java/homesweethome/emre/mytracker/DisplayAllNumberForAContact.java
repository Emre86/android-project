package homesweethome.emre.mytracker;


import homesweethome.emre.mytracker.FeedReaderContract.FeedEntry;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DisplayAllNumberForAContact extends AppCompatActivity implements ConstanteContacts {


    private String name = null;
    private final static String TAG="DisplayNumberForContact";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_all_number_for_acontact);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        if(b!=null)
        {
            name =(String) b.get("nameContact");

            ArrayList<String> values = new ArrayList<String>();
            values.add(name);

            Cursor idCursor = getContentResolver().query(CONTENT_URI,null,DISPLAY_NAME+" = '"+name+"'", null, null);
            if (idCursor.moveToFirst()){

                String na = idCursor.getString(idCursor.getColumnIndex(DISPLAY_NAME));
                String num = idCursor.getString(idCursor.getColumnIndex(HAS_PHONE_NUMBER));
                String contact_id = idCursor.getString(idCursor.getColumnIndex(_ID));
                Cursor phoneCursor = getContentResolver().query(Phone_CONTENT_URI,null,Phone_CONTACT_ID + " = ?",new String[]{contact_id}, null);

                while (phoneCursor.moveToNext()){
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(Phone_NUMBER));
                    values.add(phoneNumber);
                }
                phoneCursor.close();
            }
            idCursor.close();

            ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.mycontact_listview, values);

            ListView listView = (ListView) findViewById(R.id.listnumber);
            listView.setAdapter(adapter);

            // Action lors d'un clic sur l'item en question
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    String tel = adapterView.getItemAtPosition(i).toString();
                    tel = tel.replace(" ","");
                    tel = tel.replace("+","00");
                    tel = tel.replace("(","");
                    tel = tel.replace(")","");

                    FeedReaderDBHelper mDbHelper = new FeedReaderDBHelper(getApplicationContext());

                    SQLiteDatabase db = mDbHelper.getWritableDatabase();


                    String []isInDB = {
                            FeedEntry._ID,
                            FeedEntry.COLUMN_NAME_NAME,
                            FeedEntry.COLUMN_NAME_TEL};

                    String clauseWhere = FeedEntry.COLUMN_NAME_NAME + " = ?";
                    String[] whereArgs = {name};

                    Cursor cursor = db.query(FeedEntry.TABLE_NAME,isInDB,clauseWhere,whereArgs,null,null,null);


                    int count = cursor.getCount();



                    if (count!=0){

                        ContentValues values = new ContentValues();
                        values.put(FeedEntry.COLUMN_NAME_NAME,name);
                        values.put(FeedEntry.COLUMN_NAME_TEL,tel);
                        db.update(
                                FeedEntry.TABLE_NAME,
                                values,
                                clauseWhere,
                                whereArgs);
                        Log.i(TAG,"Update Name: "+name);
                    }
                    else {

                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(FeedEntry.COLUMN_NAME_NAME, name);
                        values.put(FeedEntry.COLUMN_NAME_TEL, tel);

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId;
                        newRowId = db.insert(
                                FeedEntry.TABLE_NAME,
                                null,
                                values);

                        if (newRowId == -1) {
                            Log.e(TAG, "Echec Insertion Name: "+name);
                        }else {
                            Log.i(TAG, "Insertion Name:" + name + " ID: "+newRowId);
                        }

                        Toast.makeText(getApplicationContext(), "Ecriture des données dans la base ", Toast.LENGTH_SHORT).show();
                    }
                    db.close();
                    cursor.close();
                }
            });
        }
    }
}
