package homesweethome.emre.mytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MyPassword extends AppCompatActivity {

    private EditText myPassword1 ;
    private EditText myPassword2 ;
    private Button buttonEnregistrer;
    private SharedPreferences sharedPreferences;
    private String traceurPreferences= "TRACEUR_PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_password);

        myPassword1 = (EditText) findViewById(R.id.myPassword1);

        myPassword2 = (EditText)findViewById(R.id.myPassword2);

        buttonEnregistrer = (Button)findViewById(R.id.buttonPassword);

        View.OnClickListener handlerButtonEnregistrer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ed1 = myPassword1.getText().toString();
                String ed2 = myPassword2.getText().toString();
                myPassword1.getText().clear();
                myPassword2.getText().clear();
                if(ed1.equals(ed2)){
                    sharedPreferences = getSharedPreferences(traceurPreferences, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("PASSWORD",ed1);
                    editor.commit();
                    Toast.makeText(getApplicationContext(),"Clé enregistré",Toast.LENGTH_SHORT).show();
                    finish();
                }
               else{
                    Toast.makeText(getApplicationContext(),"Clé non enregistré",Toast.LENGTH_SHORT).show();
                }
            }
        };

        buttonEnregistrer.setOnClickListener(handlerButtonEnregistrer);


    }
}
