package me.afifaniks.fileretriever;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Browse extends AsyncTask<String, Void, ArrayList<FileHandler>> {
    private ProgressDialog progressDialog = null;
    private Context context;
    final static String BROWSE_REQUEST = "::2";

    public Browse(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<FileHandler> doInBackground(String... strings) {
        String dir = strings[0]; // Directory of root to be browsed

        Socket s = FileBrowserActivity.clientSocket;

        System.out.println(s.isConnected() + "SOCKET CHECK");

        DataOutputStream dOutputStream = null;
        DataInputStream dataInputStream = null;
        ObjectInputStream objectInputStream = null;

        JSONArray fileList;
        ArrayList<FileHandler> files = new ArrayList<>();

        try {
            dOutputStream = new DataOutputStream(s.getOutputStream());
            dataInputStream = new DataInputStream(s.getInputStream());
//            objectInputStream = new ObjectInputStream(s.getInputStream());

            System.out.println("Requesting Files...");
            dOutputStream.writeUTF(BROWSE_REQUEST + dir);
            System.out.println("File List...");

            String response = dataInputStream.readUTF();

            fileList = new JSONArray(response);
            int numberOfItems = fileList.length();

            for (int i = 0; i < numberOfItems; i++) {
                files.add(new FileHandler(
                        fileList.getJSONObject(i).get("name").toString(),
                        fileList.getJSONObject(i).get("type").toString(),
                        fileList.getJSONObject(i).get("path").toString(),
                        Long.valueOf(fileList.getJSONObject(i).get("size").toString())
                ));
            }
            return files;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading Contents");

        progressDialog.show();
    }

    @Override
    protected void onPostExecute(ArrayList<FileHandler> list) {
        progressDialog.dismiss();
    }
}