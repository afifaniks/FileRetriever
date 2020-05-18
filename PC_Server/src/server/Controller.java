package server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private Text fxIpAddress;

    @FXML
    private Text fxPort;

    @FXML
    private Text fxServerStatus;

    @FXML
    private Text fxMobileConnected;

    @FXML
    private TextArea fxLog;

    @FXML
    private Button fxStartButton;

    private ServerSocket serverSocket = null;
    private int port;
    private DataInputStream dInputStream;
    private DataOutputStream dOutputStream;
    private OutputStreamWriter outputStreamWriter;
    private ObjectOutputStream objectOutputStream;
    private PrintWriter printWriter;
    private final Integer FTP_PORT = 7270;
    String FILE_REQUEST = "::1";
    String BROWSE_REQUEST = "::2";
    String FILE_REQUEST_FTP = "::3";
    final static int BUFFER_SIZE = 1024 * 20;
    final static int BUFFER_SIZE_FTP = 8 * 1024;
    private static boolean STOP_REQUEST = false;

    private void browseFiles(String rootPath) {
        File[] files;

        if (rootPath.equals("root")) {
            files = File.listRoots();
        } else {
            File f = new File(rootPath);
            files = f.listFiles();
        }

        // ArrayList<JSONObject> fileList = new ArrayList<>();
        JSONArray fileList = new JSONArray();

        String fileType;
        long size;

        for (File f : files) {
            if (f.isDirectory()) {
                if (rootPath.equals("root")) {
                    fileType = "drive";
                } else
                    fileType = "dir";
                size = 0;
            } else {
                fileType = "file";
                size = f.length();
            }

            JSONObject newFile = new JSONObject();

            try {
                if (rootPath.equals("root")) {
                    newFile.put("name", f.getPath());
                }
                else {
                    newFile.put("name", f.getName());
                }
                newFile.put("type", fileType);
                newFile.put("path", f.getPath());
                newFile.put("size", size);

                fileList.put(newFile);


            } catch (JSONException e) {
                fxLog.appendText("Problem in packing JSON");
                e.printStackTrace();
            }
        }

        try {
            printWriter = new PrintWriter(dOutputStream);
            printWriter.print(fileList.toString()+"\n");
            printWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        fxLog.appendText("File List Sent.\n");
    }

    private void sendFile(String filePath) throws IOException {
        File file = new File(filePath);

        fxLog.appendText("Starting Transmission...\n");
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[BUFFER_SIZE];

        while (fis.read(buffer) > 0) {
            dOutputStream.write(buffer);
        }

        fxLog.appendText("Transmission Successful\n");
    }

    @FXML
    void startApp(ActionEvent event) {
        if (serverSocket == null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    STOP_REQUEST = false;
                    startServer();
                }
            };

            Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.start();

            fxStartButton.setText("Stop Server");
        } else {
            STOP_REQUEST = true;

            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            serverSocket.close();
                            serverSocket = null;

                            Platform.runLater(() -> {
                                fxServerStatus.setText("Stopped");
                            });

                            fxLog.setText("");
                            fxIpAddress.setText("");
                            fxPort.setText("");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                Platform.runLater(() -> {
                    fxStartButton.setText("Start Server");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startServer() {
        port = 19051;

        Platform.runLater(() -> {
            fxServerStatus.setText("Initializing....");
        });

        try {
            serverSocket = new ServerSocket(port);
            Platform.runLater(() -> {
                fxServerStatus.setText("Running");
            });

        } catch (IOException e) {
            e.printStackTrace();
        }



        while (serverSocket != null && !STOP_REQUEST) {
            try {

                Platform.runLater(() -> {
                    try {
                        fxIpAddress.setText(InetAddress.getLocalHost() + "\n");
                        fxLog.appendText("Waiting for a new request... " + "\n");
                        fxPort.setText(port + "");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                });

                Socket socket = serverSocket.accept();

                Platform.runLater(() -> {
                    fxLog.appendText("Connected to client >> " + socket.getInetAddress() + "\n");
                    fxMobileConnected.setText("Connected");;
                });

                dOutputStream = new DataOutputStream(socket.getOutputStream());

                dInputStream = new DataInputStream(socket.getInputStream());

                String msg = "";

                    try {
                        msg = dInputStream.readUTF();

                        String action = msg.substring(0, 3);
                        String filePath = msg.substring(3); // 0:3 --> Action Codes

                        if (action.equals(FILE_REQUEST)) {
                            sendFile(filePath);
                        } else if (action.equals(BROWSE_REQUEST)) {
                            browseFiles(filePath);
                        } else if(action.equals(FILE_REQUEST_FTP)) {

                            int lastSlash = filePath.lastIndexOf("\\"); //CAUTION

                            String dirToFTP = filePath.substring(0, lastSlash);

                            FTPServerHandler ftpServerHandler = new FTPServerHandler(FTP_PORT,
                                    "admin",
                                    "admin".toCharArray(),
                                    dirToFTP);

                            boolean serverStatus = ftpServerHandler.startServer();

                            if (serverStatus) {
                                // Writing status On Ouput Stream
                                dOutputStream.writeUTF(String.valueOf(FTP_PORT));

                                String downloadResponse = dInputStream.readUTF();

                                Platform.runLater(() -> {
                                    fxLog.appendText("FTP Sending Completed: " + downloadResponse + "\n");
                                });
                            }

                            ftpServerHandler.stopServer();

                            Platform.runLater(() -> {
                                fxLog.appendText("FTP Server closed\n");
                                fxMobileConnected.setText("Disonnected\n");
                                fxLog.appendText("Socket connection closed\n");
                            });

                        }
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            fxMobileConnected.setText("Disonnected\n");
                            fxLog.appendText("Socket connection closed\n");
                        });
                        msg = "-exit";
                        e.printStackTrace();
                }

                socket.close();

                    Platform.runLater(() -> {
                        fxLog.appendText("Connection Closed\n");
                        fxMobileConnected.setText("Disconnected");
                    });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fxLog.setEditable(false);
        fxLog.setWrapText(true);

    }
}
