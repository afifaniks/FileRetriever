package me.afifaniks.fileretriever;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.Socket;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    EditText ipField;
    EditText portField;
    Button connectBtn;
    private static Socket clientSocket = null;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
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

//        System.out.println(Environment.getExternalStorageDirectory() + "PATH");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);
//        }

        verifyStoragePermissions(this);

        ipField = findViewById(R.id.txtIP);
        portField = findViewById(R.id.txtPort);
        connectBtn = findViewById(R.id.btnConnect);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipField.getText().toString();
                String port = portField.getText().toString();

                Client client = new Client();

                try {
                    clientSocket = client.execute(ip, port).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println(clientSocket.isConnected());

                FileBrowserActivity.clientSocket = clientSocket;

                Intent fileBrowser = new Intent(MainActivity.this, FileBrowserActivity.class);
                fileBrowser.putExtra("pathToExplore", "root");
                startActivity(fileBrowser);
            }
        });

    }
}
