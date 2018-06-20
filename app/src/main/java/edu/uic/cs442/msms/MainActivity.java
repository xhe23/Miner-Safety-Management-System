package edu.uic.cs442.msms;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ERROR_REQUEST = 9001;
    private Button LoginButton;
    private EditText Username;
    private EditText Password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoginButton = (Button)findViewById(R.id.button2);
        Username = (EditText)findViewById(R.id.editText);
        Password = (EditText)findViewById(R.id.editText2);
        LoginButton.setOnClickListener(buttonListener);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(Username.getText().toString().equals(getString(R.string.username)) &&
                    Password.getText().toString().equals(getString(R.string.password))) {
                // Google Map Setting
                //if(isServicesOK()){   // This is commented for emulator
                init();
                //}
            } else {
                Toast.makeText(getBaseContext(), "Invalid user name or password.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private void init(){

        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);

        finish();
    }


    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            // fine! and user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");

            return true;
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            // an error but we can fix it
            Log.d(TAG, "isServicesOK: Error occurred but we can fix this!");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_REQUEST);

            dialog.show();
        } else {
            Toast.makeText(this, "We can't make map requests..", Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}
