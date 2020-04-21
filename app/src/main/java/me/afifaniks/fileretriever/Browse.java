package me.afifaniks.fileretriever;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Browse extends AsyncTask<String, Void, ArrayList<FileHandler>> {
    private ProgressDialog progressDialog = null;
    private Context context;
    final static String BROWSE_REQUEST = "::2";
    String ip;
    Integer port;

    public Browse(Context context, String ip, Integer port) {
        this.context = context;
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected ArrayList<FileHandler> doInBackground(String... strings) {
        String dir = strings[0]; // Directory of root to be browsed

        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(ip, Integer.valueOf(port)), 5000);

            DataOutputStream dOutputStream = null;
            DataInputStream dataInputStream = null;

            JSONArray fileList;
            ArrayList<FileHandler> files = new ArrayList<>();
            dOutputStream = new DataOutputStream(s.getOutputStream());
            dataInputStream = new DataInputStream(s.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

            System.out.println("Requesting Files...");
            dOutputStream.writeUTF(BROWSE_REQUEST + dir);
            System.out.println("File List...");

            fileList = new JSONArray(bufferedReader.readLine());
            int numberOfItems = fileList.length();

            for (int i = 0; i < numberOfItems; i++) {
                files.add(new FileHandler(
                        fileList.getJSONObject(i).get("name").toString(),
                        fileList.getJSONObject(i).get("type").toString(),
                        fileList.getJSONObject(i).get("path").toString(),
                        Long.valueOf(fileList.getJSONObject(i).get("size").toString())
                ));
            }
            s.close();
            return files;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading Data");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait while we finish loading...");
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Browse.this.cancel(true);
                        dialog.dismiss();
                    }
                });
        progressDialog.show();

    }

    @Override
    protected void onPostExecute(ArrayList<FileHandler> list) {
        progressDialog.dismiss();

        if (list == null) {
            new AlertDialog.Builder(context)
                    .setTitle("Connection Failure")
                    .setMessage("Make sure your PC Server Agent is connected to the same network and recheck ip and port.")
                    .setIcon(R.drawable.warning)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }}).show();
        } else {
            FileBrowserActivity f = (FileBrowserActivity)context;
            f.changeListItem(list);
        }
    }

}
