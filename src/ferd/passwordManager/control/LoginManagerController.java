package ferd.passwordManager.control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import ferd.passwordManager.MainApp;
import ferd.passwordManager.model.MasterKeyAdapter;
import ferd.passwordManager.util.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginManagerController {
    private static final String TEXT_HELLO = "Hello.";
    private static final String TEXT_GENERATING = "Generating…";
    @FXML
    private TextField nameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private CheckBox saveBox;
    @FXML
    private Label helloLabel;
    private MainApp mainApp;

    // TODO use properties for save folder etc

    @FXML
    private void initialize() {
        try {
            final File f = new File(Util.getWorkingDirectory(), "login");
            if (f.exists() && !f.isDirectory() && f.length() > 0) {
                final BufferedReader br = new BufferedReader(new FileReader(f));
                final String s = br.readLine();
                br.close();
                this.nameField.setText(s);
                this.saveBox.setSelected(true);
                Platform.runLater(() -> this.passwordField.requestFocus());
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        this.saveBox.setFocusTraversable(false);
    }

    @FXML
    private void login() {
        this.showGenerating(true);

        final String name = this.nameField.getText();
        if (name.length() <= 0) {
            this.nameField.requestFocus();
            return;
        }
        if (this.passwordField.getText().length() <= 0) {
            this.passwordField.requestFocus();
            return;
        }
        try {
            final File f = new File(Util.getWorkingDirectory(), "login");
            if (this.saveBox.isSelected()) {
                final BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(name);
                bw.close();
            } else {
                Files.delete(f.toPath());
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final Consumer<MasterKeyAdapter> cons = pw -> Platform.runLater(() -> this.mainApp.showPasswordManager(pw, name));
        final Thread pwct = new Thread(() -> cons.accept(new MasterKeyAdapter(name, LoginManagerController.this.passwordField.getText())));
        pwct.start();
    }

    public void setMainApp(final MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void showGenerating(final boolean generating) {
        this.helloLabel.setText(generating ? TEXT_GENERATING : TEXT_HELLO);
        this.loginButton.setDisable(generating);
        this.nameField.setDisable(generating);
        this.passwordField.setDisable(generating);
        this.saveBox.setDisable(generating);
        if (!generating) {
            this.passwordField.requestFocus();
        }
    }
}
