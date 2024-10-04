package com.example.messenger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.concurrent.Task;

public class Controller implements Initializable {

    @FXML
    private Button button_send;

    @FXML
    private TextField tf_message;

    @FXML
    private VBox vBox_messages;

    @FXML
    private ScrollPane sp_main;

    private Server server;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Ensure that vBox_messages is initialized and height listener is set
        vBox_messages.heightProperty().addListener((observable, oldValue, newValue) -> {
            sp_main.setVvalue((Double) newValue);
        });

        // Run server initialization and message reception in a background thread
        Task<Void> serverTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Initialize the server
                    server = new Server(new ServerSocket(1235));

                    // Start receiving messages in the background
                    server.receiveMessageFromClient(vBox_messages);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error creating or running the server");
                }
                return null;
            }
        };

        // Start the server task in a new thread
        new Thread(serverTask).start();

        // Set up the send button action
        button_send.setOnAction(actionEvent -> {
            String messageToSend = tf_message.getText();
            if (!messageToSend.isEmpty()) {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 5, 10));

                Text text = new Text(messageToSend);
                TextFlow textFlow = new TextFlow(text);

                textFlow.setStyle("-fx-color: rgb(239,242,255);" +
                        " -fx-background-color: rgb(15,125,142);" +
                        " -fx-background-radius: 20px");

                textFlow.setPadding(new Insets(5, 10, 5, 10));
                text.setFill(Color.color(0.934, 0.945, 0.996));

                hBox.getChildren().add(textFlow);
                vBox_messages.getChildren().add(hBox);

                // Send the message to the client
                server.sendMessageToClient(messageToSend);
                tf_message.clear();
            }
        });
    }

    public static void addLabel(String messageFromClient, VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromClient);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle(" -fx-background-color: rgb(233,233,235);" +
                " -fx-background-radius: 20px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        hBox.getChildren().add(textFlow);

        // Ensure that the UI update is run on the JavaFX Application Thread
        Platform.runLater(() -> vBox.getChildren().add(hBox));
    }
}
