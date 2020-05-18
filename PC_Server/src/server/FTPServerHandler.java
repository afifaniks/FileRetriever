package server;

import org.erc.pftps.services.FTPServer;

public class FTPServerHandler {
    int port;
    String username;
    char[] password;
    String path;
    FTPServer ftpServer = null;

    public FTPServerHandler(int port, String username, char[] password, String path) {
        this.port = port;
        this.username = username;
        this.password = password;
        this.path = path;

        ftpServer = new FTPServer();
        ftpServer.setPort(port);
        ftpServer.setUser(username, password, path);
    }

    public boolean startServer() {
        return ftpServer.start();
    }

    public void stopServer() {
        ftpServer.stop();
    }
}
