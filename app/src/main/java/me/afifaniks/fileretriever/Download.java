package me.afifaniks.fileretriever;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download extends AsyncTask<String, Integer, Void> {
    final static String DOWNLOAD_REQUEST = "::1";
    final static int BUFFER_SIZE = 4096;
    private ProgressDialog progressDialog;
    Context context;
    String fileName;
    String filePath;
    String pathToSave;
    Integer fileSize;

    public Download(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        fileName = strings[0];
        filePath = strings[1];
        fileSize = Integer.valueOf(strings[2]);

        Socket s = FileBrowserActivity.clientSocket;
        DataInputStream dInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            dInputStream = new DataInputStream(s.getInputStream());
            dataOutputStream = new DataOutputStream(s.getOutputStream());


            dataOutputStream.writeUTF(DOWNLOAD_REQUEST + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String downloadPath = Environment.getExternalStorageDirectory().toString() + File.separator + "FileRetriever";

        File folder = new File(downloadPath);

        System.out.println(folder.listFiles());

        if (!folder.exists()) {
            System.out.println("Folder Created: " + folder.mkdirs());
        }

        pathToSave = downloadPath + File.separator + fileName;

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(pathToSave);
            byte[] buffer = new byte[BUFFER_SIZE];
            int read = 0;
            int totalRead = 0;
            int remaining = fileSize;

            publishProgress(0);

            while ((read = dInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
//                System.out.println("read " + totalRead + " bytes.");

                publishProgress((totalRead * 100)/fileSize);

                fos.write(buffer, 0, read);
            }

            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressDialog.setProgress(values[0]);
        progressDialog.setMessage("Downloading file: " + filePath);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Downloading file: " + filePath);
        progressDialog.setTitle("Download Started");
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Download.this.cancel(true);
                        File file = new File(pathToSave);
                        if (file.delete()) {
                            System.out.println("Deleted Incomplete Download");
                        } else {
                            System.out.println("Couldn't Delete Incomplete Download");
                        }
                        dialog.dismiss();
                    }
                });
        progressDialog.show();

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File Download Complete", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }
}
