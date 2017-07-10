package info.android.rowing.activity;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import info.android.rowing.R;

//import static info.android.rowing.R.id.player;

public class Login extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //addListenerOnButton();//player or coxswain.
    }




   public void OK(View v) {
//new maincoach (new Intent(MainActivity.this,maincoach.class));
       // Intent ch = new Intent(this, NewSessionActivity.class);
       Intent ch = new Intent(this, NewSessionActivity.class);

// login function to send all data

       //startActivity (intent1);
       Bundle extras = new Bundle(); // pass multiple things between two activity


       EditText logdata = (EditText) findViewById(R.id.log);
      // RadioGroup rg2 = (RadioGroup) findViewById(R.id.radio1);

       RadioGroup rg = (RadioGroup) findViewById(R.id.radiogroup);
       String selectedRadioValue = ((RadioButton)findViewById(rg.getCheckedRadioButtonId() )).getText().toString();
       extras.putString("radioGroupSelected", selectedRadioValue);
       extras.putString("namexx", logdata.getText().toString());

      // String selectedRadioValue2 = ((RadioButton)findViewById(rg2.getCheckedRadioButtonId() )).getText().toString();
      // extras.putString("radioGroupSelected2", selectedRadioValue2);




       ch.putExtras(extras);



       startActivity(ch);

   }
}
