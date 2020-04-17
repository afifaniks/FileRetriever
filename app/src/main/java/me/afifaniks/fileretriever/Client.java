package me.afifaniks.fileretriever;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.Socket;

public class Client extends AsyncTask <String, Void, Socket> {
    Socket clientSocket;

    @Override
    protected Socket doInBackground(String... strings) {
        String host = strings[0];
        Integer port = Integer.valueOf(strings[1]);

        try {
            clientSocket = new Socket(host, port);
            System.out.println("Socket Connected");
//            MainActivity.clientSocket = clientSocket;

            return clientSocket;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
