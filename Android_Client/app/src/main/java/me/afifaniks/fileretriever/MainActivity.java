package me.afifaniks.fileretriever;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    EditText ipField;
    EditText portField;
    Button connectBtn;
    private static String ip;
    private static String port;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String SHARED_PREF = "fileRetrieverPref";
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

        ipField = findViewById(R.id.txtIP);
        portField = findViewById(R.id.txtPort);
        connectBtn = findViewById(R.id.btnConnect);

        // Setting data from shared preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String prefIP = sharedPreferences.getString("IP", "");
        String prefPort = sharedPreferences.getString("PORT", "");

        System.out.println("Test" + prefIP);

        // Setting preffered values
        ipField.setText(prefIP);
        portField.setText(prefPort);

        // ipField only takes valid input like XXX.XXX.XXX.XXX
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       android.text.Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }

        };

        ipField.setFilters(filters);

        boolean newConnectionRequest = getIntent().getBooleanExtra("reqNewConnection", false);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = ipField.getText().toString();
                port = portField.getText().toString();

                if (!(ip.equals("") || port.equals(""))) {
                    // Saving data automatically
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("IP", ip);
                    editor.putString("PORT", port);
                    editor.commit();

                    Client client = new Client(MainActivity.this);
                    client.execute(ip, port);
                }

            }
        });

        if (!newConnectionRequest) { // Only will be initiated if MainActivity is not called in runtime
            // Trying initially to connect
            ip = ipField.getText().toString();
            port = portField.getText().toString();

            if (!(ip.equals("") || port.equals(""))) {
                new Client(this).execute(ip, port);
            }
        }

    }
}
