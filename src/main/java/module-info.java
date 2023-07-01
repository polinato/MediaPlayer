module com.tpm.mediaplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.tpm.mediaplayer to javafx.fxml;
    exports com.tpm.mediaplayer;
}