package me.afifaniks.fileretriever;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

public class Client extends AsyncTask <String, Void, Socket> {
    Socket clientSocket;
    ProgressDialog progressDialog;
    Context context;
    String host;
    String port;

    public Client(Context context) {
        this.context = context;
    }

    @Override
    protected Socket doInBackground(String... strings) {
        host = strings[0];
        port = strings[1];

        try {
            clientSocket = new Socket(host, Integer.valueOf(port));
            System.out.println("Socket Connected");

            return clientSocket;

        } catch (IOException e) {
            Toast.makeText(context, "Couldn't establish connection. Please check IP & port again", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Checking Connection");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait while we try to establish a connection with server...");
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Client.this.cancel(true);
                        dialog.dismiss();
                    }
                });
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Socket socket) {
        Intent fileBrowser = new Intent(context, FileBrowserActivity.class);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileBrowser.putExtra("pathToExplore", "root");
        fileBrowser.putExtra("ip", host);
        fileBrowser.putExtra("port", port);
        context.startActivity(fileBrowser);
    }
}
