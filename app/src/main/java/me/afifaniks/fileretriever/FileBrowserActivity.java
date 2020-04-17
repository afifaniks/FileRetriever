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
    private static ListAdapter listAdapter;
    private static ArrayList<FileHandler> fileList;
    private static DecimalFormat df = new DecimalFormat("0.00");
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        path = getIntent().getStringExtra("pathToExplore");

        listView = findViewById(R.id.listFile);

        Browse browse = new Browse(this);

        try {
            fileList = browse.execute(path).get();

            listAdapter = new ListAdapter(FileBrowserActivity.this, fileList);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final FileHandler clickedItem = (FileHandler) parent.getItemAtPosition(position);

                    String type = clickedItem.getType();

                    if (type.equals("file")) {
                        new AlertDialog.Builder(FileBrowserActivity.this)
                                .setTitle("Confirm Download")
                                .setMessage("Do you want to download file of size " +
                                        df.format(clickedItem.getSize() * 0.000001) +
                                        " MB?")
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
                        String pathToBrowse = clickedItem.getPath();
                        try {
                            ArrayList<FileHandler> newFileList = new Browse(FileBrowserActivity.this).execute(pathToBrowse).get();
                            // Set Files
                            changeListItem(newFileList);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            listView.setAdapter(listAdapter);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void changeListItem(ArrayList<FileHandler> list) {
        fileList.clear();
        fileList.addAll(list);
        listAdapter.notifyDataSetChanged();
    }

}
