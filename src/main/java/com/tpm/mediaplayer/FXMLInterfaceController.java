package com.tpm.mediaplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class FXMLInterfaceController implements Initializable {

    @FXML
    private Label ltime;

    @FXML
    private Button bplay;

    @FXML
    private Slider timeslider, volumeslider;

    @FXML
    private MediaView mv;

    @FXML
    private BorderPane bp;

    MediaPlayer mp;
    private Duration duration;

    @FXML
    private void createMedia(String path){

        Media media = new Media(path);
        mp = new MediaPlayer(media);
        mv.setMediaPlayer(mp);
    }

    private static String formatTime(Duration elapsed, Duration duration) {

        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }

        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {

            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);

            if (durationHours > 0)
                intDuration -= durationHours * 60 * 60;

            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds, durationHours, durationMinutes, durationSeconds);
            }
            else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes, durationSeconds);
            }
        }
        else {
            if (elapsedHours > 0)
                return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
            else
                return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
        }
    }

    private void doVolumeListener(){

        volumeslider.setMin(0.0);
        volumeslider.setValue(1.0);
        volumeslider.setMax(1.0);
        volumeslider.valueChangingProperty()
                .addListener((observable, oldValue, newValue) -> mp.volumeProperty().setValue(volumeslider.getValue()));
    }

    private void doUpdate(){

        Platform.runLater(() -> {

            Duration dr = mp.getCurrentTime();
            timeslider.setDisable(dr.isUnknown());
            ltime.setText(formatTime(dr, duration));

            if(!timeslider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeslider.isValueChanging()){
                timeslider.setValue(dr.divide(duration.toMillis()).toSeconds() * 100.0);
            }
        });
    }

    private void doTimeListener(MediaPlayer player){

        if(player != null){

            mp.currentTimeProperty().addListener(observable -> doUpdate());

            timeslider.valueProperty().addListener((observable, oldValue, newValue) -> {

                if (timeslider.isValueChanging()) {
                    double currentTimeMillis = duration.toMillis() * (newValue.doubleValue() / 100.0);
                    mp.seek(Duration.millis(currentTimeMillis));
                }
            });
        }
    }

    private void doOnReadyPlayer(){

        mp.setOnReady(() -> {
            duration = mp.getMedia().getDuration();
            doUpdate();
        });
    }

    private void doDragNDrop(){

        bp.setOnDragOver(event -> {

            Dragboard db = event.getDragboard();

            if(db.hasFiles() || db.hasUrl()) {
                event.acceptTransferModes(TransferMode.LINK);
            }
            else {
                event.consume();
            }
        });

        bp.setOnDragDropped(event -> {

            Dragboard db = event.getDragboard();
            boolean success;
            String path;

            if(db.hasFiles()){

                success = true;
                if(db.getFiles().size() > 0) {

                    try {
                        path = db.getFiles().get(0).toURI().toURL().toString();
                        createMedia(path);
                        doVolumeListener();
                        doTimeListener(mp);
                        doOnReadyPlayer();
                    }
                    catch(MalformedURLException mex) {
                        mex.printStackTrace();
                    }
                }
            }

            else {
                path = db.getUrl();
                createMedia(path);
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

    }

    @FXML
    private void handleMenuActionOpen() {

        FileChooser fc = new FileChooser();
        File path = fc.showOpenDialog(null);

        createMedia(path.toURI().toString());

        doVolumeListener();
        doTimeListener(mp);
        doOnReadyPlayer();
    }

    @FXML
    private void handleButtonActionPlay() {

        if(mp != null){

            Status status = mp.getStatus();
            if(status == Status.STOPPED || status == Status.READY || status == Status.PAUSED) {
                mp.play();
                bplay.setText("||");
            }
            else {
                mp.pause();
                bplay.setText(">");
            }
        }
    }

    @FXML
    private void handleButtonActionStop() {

        if(mp != null){
            Status status = mp.getStatus();

            if(status == Status.PLAYING || status == Status.HALTED || status == Status.PAUSED) {
                mp.stop();
            }
        }
    }

    @FXML
    private void handleButtonActionClose() {

        mp.stop();
        mv.setMediaPlayer(null);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        doDragNDrop();
    }

}
