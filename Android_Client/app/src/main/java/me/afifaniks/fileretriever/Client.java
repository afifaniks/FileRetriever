package me.afifaniks.fileretriever;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends AsyncTask <String, Void, Socket> {
    Socket clientSocket;
    ProgressDialog progressDialog;
    ProgressDialog progressDialogFailure;
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
            System.out.println("Trying: " + host + ":" + port);
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(host, Integer.valueOf(port)), 5000);
            System.out.println("Socket Connected");

            return clientSocket;

        } catch (IOException e) {
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
        progressDialog.setMessage("Please wait while we try to establish a connection with the server...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Socket socket) {
        if (socket == null) {
            progressDialog.dismiss();
            new AlertDialog.Builder(context)
                    .setTitle("Connection Failure")
                    .setMessage("Make sure your PC Server Agent is connected to the same network and recheck ip and port.")
                    .setIcon(R.drawable.warning)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }}).show();
        } else {
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
}
