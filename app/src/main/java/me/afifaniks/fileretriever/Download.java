package me.afifaniks.fileretriever;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class Download extends AsyncTask<String, Integer, Void> {
    final static String DOWNLOAD_REQUEST = "::1";
    final static String FTP_DOWNLOAD_REQUEST = "::3";
    final static int BUFFER_SIZE = 1024 * 20;
    final static int BUFFER_SIZE_FTP = 1024000;
    final static int TCP_SIZE_LIMIT = 8000000;

    private ProgressDialog progressDialog;
    FTPClient ftp;
    Context context;
    String fileName;
    String filePath;
    String downloadPath;
    String pathToSave;
    String ip;
    Integer port;
    Integer fileSize;
    double timeTaken;
    Socket s = null;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public Download(Context context, String ip, Integer port) {
        this.context = context;
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected Void doInBackground(String... strings) {
        fileName = strings[0];
        filePath = strings[1];
        fileSize = Integer.valueOf(strings[2]);

        DataInputStream dInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            s = new Socket(ip, port);

            dInputStream = new DataInputStream(s.getInputStream());
            dataOutputStream = new DataOutputStream(s.getOutputStream());

            downloadPath = Environment.getExternalStorageDirectory().toString() + File.separator + "FileRetriever";

            File folder = new File(downloadPath);

            System.out.println(folder.listFiles());

            if (!folder.exists()) {
                System.out.println("Folder Created: " + folder.mkdirs());
            }

            pathToSave = downloadPath + File.separator + fileName;

            boolean ftpDownloadStatus = false;

            System.out.println("File Size: " + fileSize + " TCP: " + TCP_SIZE_LIMIT);

            if (fileSize > TCP_SIZE_LIMIT) {
                dataOutputStream.writeUTF(FTP_DOWNLOAD_REQUEST + filePath);
                String ftpPort = dInputStream.readUTF(); // Should return port number of server; -1 is failure

                if (!ftpPort.equals("-1")) {
                    progressDialog.setMessage("Downloading using FTP connection");

                    long startTime = System.currentTimeMillis();

                    try {
                        FTPDownloader(ip, Integer.valueOf(ftpPort), "admin", "admin");
                        downloadFileFTP(fileName, pathToSave);

                        disconnect();
                        dataOutputStream.writeUTF("true"); // Download Success
                        ftpDownloadStatus = true;

                        long estimatedTime = System.currentTimeMillis() - startTime;

                        timeTaken = estimatedTime/1000.0;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            if (!ftpDownloadStatus) {
                try {
                    progressDialog.setMessage("Downloading using TCP connecttion");
                    dataOutputStream.writeUTF(DOWNLOAD_REQUEST + filePath);
                    FileOutputStream fos = null;
                    fos = new FileOutputStream(pathToSave);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read = 0;
                    int totalRead = 0;
                    int remaining = fileSize;

                    publishProgress(0);

                    long startTime = System.currentTimeMillis();

                    while ((read = dInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                        totalRead += read;
                        remaining -= read;

                        publishProgress((totalRead * 100)/fileSize);

                        fos.write(buffer, 0, read);
                    }

                    long estimatedTime = System.currentTimeMillis() - startTime;

                    timeTaken = estimatedTime/1000.0;

                    fos.close();


                    s.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
//        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Download.this.cancel(true);
                            System.out.println("OK DELETING");
                            if (ftp != null) {
                                disconnect(); // To close ftp if exists
                            }

                            dialog.dismiss();

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            File file = new File(pathToSave);
                            if (file.delete()) {
                                System.out.println("Deleted Incomplete Download");
                            } else {
                                System.out.println("Couldn't Delete Incomplete Download");
                            }
                        }
                    }
                });

        progressDialog.show();

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File Download Complete", Toast.LENGTH_SHORT).show();
        new AlertDialog.Builder(context)
                .setTitle("Download Completed")
                .setMessage("File Downloaded Successfully" + "\n" +
                        "Time Taken: " + df.format(timeTaken) + "s\n" +
                        "Avg. Transfer Rate: " + df.format((fileSize*0.000001)/timeTaken) +"MBps")
                .setIcon(R.drawable.warning)
                .setPositiveButton("Show Files", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        Uri uri = Uri.parse(downloadPath); // a directory
                        intent.setDataAndType(uri, "*/*");
                        context.startActivity(Intent.createChooser(intent, "Open Folder"));
                    }}).setNegativeButton("CLOSE", null).show();
        progressDialog.dismiss();
    }

    private void FTPDownloader(String host, int port, String user, String pwd) throws Exception {
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(host, port);
        ftp.setBufferSize(1024*1024);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(user, pwd);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalActiveMode();
    }

    public void downloadFileFTP (String remoteFilePath, String localFilePath) {
        // APPROACH #2: using InputStream retrieveFileStream(String)
        try {

            String remoteFile2 = remoteFilePath;
            File downloadFile2 = new File(localFilePath);
            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
            InputStream inputStream = ftp.retrieveFileStream(remoteFile2);
            byte[] bytesArray = new byte[BUFFER_SIZE_FTP];
            int total = 0;

            int bytesRead = -1;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream2.write(bytesArray, 0, bytesRead);
                total += bytesRead;
                publishProgress((total*100)/fileSize);
//                System.out.println("Downloaded FTP " + total + " % " + (total*100)/fileSize);
            }

            boolean success = ftp.completePendingCommand();
            if (success) {
                System.out.println("File has been downloaded successfully.");
            }
            outputStream2.close();
            inputStream.close();

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftp.isConnected()) {
                    ftp.logout();
                    ftp.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void disconnect() {
        if (ftp.isConnected()) {
            try {
//                this.ftp.logout();
                ftp.disconnect();
                ftp = null;
            } catch (IOException f) {
                // do nothing as file is already downloaded from FTP server
            }
        }
    }

}
