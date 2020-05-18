package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/server/view.fxml"));
        primaryStage.setTitle("File Retriever Server");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 700, 450));
        primaryStage.getIcons().add(new Image("server/icon.png"));
        primaryStage.requestFocus();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
