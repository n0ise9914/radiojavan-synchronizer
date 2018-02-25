package music;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends Application {
    private Label progressBarInfo;
    private Label status;
    private ProgressBar progressBar;
    private Service service = new Service("mp3/topmonth", this::console, this::progress);
    private TextField hostField, portField, usernameField, passwordField;
    private Gson gson = new Gson();
    private ProxyInfo proxyInfo = new ProxyInfo();
    private static boolean silent = false;
    private Stage primaryStage;
    private boolean initialized = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Platform.setImplicitExit(false);

        VBox vbox = new VBox();
        vbox.setSpacing(3);
        vbox.setPadding(new Insets(25, 25, 25, 25));

        status = new Label();
        vbox.getChildren().add(status);
        status.setText("-");

        progressBar = new ProgressBar();
        progressBar.setMinWidth(300);
        vbox.getChildren().add(progressBar);

        progressBarInfo = new Label();
        vbox.getChildren().add(progressBarInfo);
        progressBarInfo.setText("-");

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.BASELINE_RIGHT);

        Button gotoBackground = new Button();
        gotoBackground.setText("gotoBackground");
        gotoBackground.setOnAction(event -> {
            runInBackground();
            primaryStage.hide();
        });
        hBox.getChildren().add(gotoBackground);

        Button sync = new Button();
        sync.setText("sync");
        sync.setOnAction(event -> {
            sync.setDisable(true);
            service.start(primaryStage::show);
            Platform.runLater(vbox::requestFocus);
        });
        hBox.getChildren().add(sync);

        Button exit = new Button();
        exit.setText("exit");
        exit.setOnAction(event -> {
            Runtime.getRuntime().exit(0);
        });
        hBox.getChildren().add(exit);

        vbox.getChildren().add(hBox);

        vbox.getChildren().add(new Label("Proxy"));

        GridPane proxyPane = new GridPane();
        proxyPane.setPadding(new Insets(10, 10, 10, 10));
        proxyPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        proxyPane.setHgap(10);
        proxyPane.setVgap(10);

        int width = 100;
        hostField = new TextField();
        hostField.setMaxWidth(width);
        proxyPane.add(new Label("host"), 0, 0);
        proxyPane.add(hostField, 1, 0);
        hostField.textProperty().addListener((observable, oldValue, newValue) -> updateProxy(true));

        portField = new TextField();
        portField.setMaxWidth(width);
        proxyPane.add(new Label("port"), 2, 0);
        proxyPane.add(portField, 3, 0);
        portField.textProperty().addListener((observable, oldValue, newValue) -> updateProxy(true));

        usernameField = new TextField();
        usernameField.setMaxWidth(width);
        proxyPane.add(new Label("user"), 0, 1);
        proxyPane.add(usernameField, 1, 1);
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> updateProxy(true));

        passwordField = new TextField();
        passwordField.setMaxWidth(width);
        proxyPane.add(new Label("pass"), 2, 1);
        proxyPane.add(passwordField, 3, 1);
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> updateProxy(true));

        vbox.getChildren().add(proxyPane);
        Scene scene = new Scene(vbox, 350, 230);
        primaryStage.setTitle("Radiojavan synchronizer");
        primaryStage.setScene(scene);

        if (!silent) {
            primaryStage.show();
        } else {
            runInBackground();
        }

        primaryStage.setResizable(false);

        try {
            if (new File(System.getProperty("user.dir") + "/proxy.json").exists()) {
                String data = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/proxy.json")));
                ProxyInfo proxyInfo = gson.fromJson(data, ProxyInfo.class);
                hostField.setText(proxyInfo.host);
                portField.setText(proxyInfo.port);
                usernameField.setText(proxyInfo.user);
                passwordField.setText(proxyInfo.pass);
                updateProxy(false);
                this.proxyInfo = proxyInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initialized = true;
    }

    private boolean runningInBackground = false;
    private void runInBackground() {
        if(runningInBackground)
            return;
        runningInBackground = true;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                service.start(() -> {
                    Platform.runLater(() -> {
                        primaryStage.show();
                    });
                });
            }
        }, 1000 * 60 * 10, 1000 * 60 * 60 * 2);
    }

    private void console(String message) {
        System.out.println(message);
        Platform.runLater(() -> status.setText(message));
    }

    private void progress(ProgressInfo info) {
        Platform.runLater(() -> progressBar.setProgress((double) info.currentProgressValue / 100));
        Platform.runLater(() -> progressBarInfo.setText(info.totalFilesCount + "/" + info.downloadedFilesCount + "          "
                + getSize(info.completeFileSize) + "/" + getSize(info.downloadedFileSize)));
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.contains("silent")) {
                silent = true;
            }
        }
        launch(args);
    }

    private String getSize(long bytes) {
        long kilobytes = (bytes / 1024);
        long megabytes = (kilobytes / 1024);
        return megabytes + "." + kilobytes;
    }

    private void updateProxy(boolean save) {
        if (!initialized) return;

        proxyInfo.host = this.hostField.getText();
        proxyInfo.port = this.portField.getText();
        proxyInfo.user = this.usernameField.getText();
        proxyInfo.pass = this.passwordField.getText();
        if (save) ProxyHelper.save(proxyInfo);
    }
}