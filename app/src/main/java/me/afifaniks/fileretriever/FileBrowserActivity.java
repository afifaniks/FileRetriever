package me.afifaniks.fileretriever;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FileBrowserActivity extends AppCompatActivity {

    public static Socket clientSocket;
    private ListView listView;
    private TextView currentLocation;
    private TextView totalFiles;
    private static ListAdapter listAdapter;
    private static ArrayList<FileHandler> fileList = new ArrayList<>();
    private static DecimalFormat df = new DecimalFormat("0.00");
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        path = getIntent().getStringExtra("pathToExplore");

        listView = findViewById(R.id.listFile);
        currentLocation = findViewById(R.id.txtLocation);
        totalFiles = findViewById(R.id.txtTotalFiles);

        currentLocation.setText(path);

        Browse browse = new Browse(this);

        browse.execute(path);

        fileList.add(new FileHandler("", "", "", 0)); // Adding dummy list


        listAdapter = new ListAdapter(FileBrowserActivity.this, fileList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FileHandler clickedItem = (FileHandler) parent.getItemAtPosition(position);

                String type = clickedItem.getType();

                if (type.equals("file")) {
                    new AlertDialog.Builder(FileBrowserActivity.this)
                            .setTitle("Confirm Download")
                            .setMessage("File Path: " + clickedItem.path + "\n\n" +
                                    "File Size: " + df.format(clickedItem.getSize() * 0.000001) + " MB")
                            .setIcon(R.drawable.download)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new Download(FileBrowserActivity.this).execute(
                                            clickedItem.getName(),
                                            clickedItem.getPath(),
                                            String.valueOf(clickedItem.getSize()));
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                } else {
                    path = clickedItem.getPath();
                    currentLocation.setText(path);
                    new Browse(FileBrowserActivity.this).execute(path);
                }
            }
        });

        listView.setAdapter(listAdapter);
    }

    public void changeListItem(ArrayList<FileHandler> list) {
        fileList.clear();
        fileList.addAll(list);
        totalFiles.setText("Total Files: " + list.size());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (!path.equals("root")) {
            if (path.length() == 3)
                path = "root";
            else {
                path = path.substring(0, path.lastIndexOf("\\"));
                if (path.length() == 2)
                    path += "\\";
            }

            currentLocation.setText(path);
            new Browse(FileBrowserActivity.this).execute(path);

        } else {
            super.onBackPressed();
        }
    }
}
