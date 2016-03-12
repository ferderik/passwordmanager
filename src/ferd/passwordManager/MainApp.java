package ferd.passwordManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lyndir.masterpassword.MPSiteType;

import ferd.passwordManager.control.AddController;
import ferd.passwordManager.control.LoginManagerController;
import ferd.passwordManager.control.PasswordManagerController;
import ferd.passwordManager.model.MasterKeyAdapter;
import ferd.passwordManager.model.SavedPassword;
import ferd.passwordManager.util.Util;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {
    private static final String MASTER_PASSWORD = "Master Password";
    private Stage primaryStage;
    private Stage secondaryStage;
    private BorderPane rootLayout;
    private LoginManagerController loginManagerController;
    private PasswordManagerController pwManagerController;
    private Timeline timeOut;

    public static void main(final String[] args) {
        Application.launch(args);
    }

    private boolean checkCreateNew() {
        final Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("No password file found!");
        alert.setHeaderText("A file for that name and password couldn't be found.");
        alert.setContentText("Do you want to create one?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        final Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(null) == ButtonType.YES;
    }

    public void doAddWebsite(final SavedPassword pw) {
        if (this.pwManagerController == null) {
            return;
        }
        this.pwManagerController.addSavedPassword(pw);
        this.secondaryStage.hide();
    }

    public void doRefreshWebsites() {
        if (this.pwManagerController == null) {
            return;
        }
        this.pwManagerController.refreshPasswords();
        this.secondaryStage.hide();
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    public boolean getTimeoutRunning() {
        return this.timeOut.getStatus() == Animation.Status.RUNNING;
    }

    public void initRootLayout() {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            this.rootLayout = (BorderPane) loader.load();
            final Scene scene = new Scene(this.rootLayout);
            this.primaryStage.setScene(scene);
            this.primaryStage.show();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        this.timeOut.stop();
        this.savePasswords();
        this.showLogin();
        this.pwManagerController = null;
    }

    private List<SavedPassword> readSavedPasswords(final MasterKeyAdapter mkAdapter) {
        List<SavedPassword> pws = null;
        final Gson gson = new Gson();
        final Type listType = new TypeToken<List<SavedPassword>>() {}.getType();

        File workingDir = new File(System.getProperty("user.dir"));
        try {
            workingDir = Util.getWorkingDirectory();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        String nameGen, passGen;
        nameGen = mkAdapter.encode("passwordManager.files", MPSiteType.GeneratedName, 1);
        passGen = mkAdapter.encode("passwordManager.files", MPSiteType.GeneratedMaximum, 1);

        final File file = new File(workingDir, nameGen);
        if (!file.exists() || file.length() == 0) {
            final boolean createNew = this.checkCreateNew();
            if (!createNew) {
                return null;
            }
            pws = new ArrayList<>();
            try {
                Util.encryptStringToFile(gson.toJson(pws), file, passGen);
            } catch (final GeneralSecurityException | IOException e) {
                new Alert(AlertType.ERROR,
                        "Something, somewhere went terribly wrong.\n I couldn't encrypt to that file.\n Please choose a different location.",
                        ButtonType.OK).showAndWait();
                e.printStackTrace();
            }
        } else {
            try {
                final String data = Util.decryptFileToString(file, passGen);
                pws = gson.fromJson(data, listType);
            } catch (final GeneralSecurityException | IOException e) {
                e.printStackTrace();
                this.manageFileDecodeFail(file);
                return null;
            } catch (final JsonSyntaxException e) {
                this.manageFileDeserializeFail(file);
                e.printStackTrace();
                return null;
            }
        }
        pws.forEach(pw -> pw.getPasswordType());
        return pws;
    }

    private void manageFileDeserializeFail(final File file) {
        final Alert alert = new Alert(AlertType.ERROR, "The meta data file couldn't be parsed correctly.\n" + "Do you want me to delete it?",
                ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(null) == ButtonType.YES) {
            try {
                Files.delete(file.toPath());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void manageFileDecodeFail(final File file) {
        final Alert alert = new Alert(AlertType.WARNING,
                "The meta data file couldn't be decoded. \n" + "This might be due to the file being generated by an older version, sorry :(\n"
                        + "If you recently switched to a newer version, please delete the file and import from an exported list.\n"
                        + "Otherwise it might be because of file corruption or even a name collision.\n"
                        + "If this keeps happening, please choose a different name or password.\n\n" + "Do you want me to delete the meta data file?",
                ButtonType.YES, ButtonType.NO);
        final ButtonType result = alert.showAndWait().orElse(null);
        if (result == ButtonType.YES) {
            try {
                Files.delete(file.toPath());
            } catch (final IOException e1) {
                final Alert alert2 = new Alert(AlertType.ERROR, "That file doesn't even exist! What did you do? :(", ButtonType.OK);
                alert2.showAndWait();
                e1.printStackTrace();
            }
        }
    }

    public void resetTimeout() {
        if (this.timeOut.getStatus() == Animation.Status.RUNNING) {
            this.timeOut.playFromStart();
        }
    }

    public void saveAndQuit() {
        this.savePasswords();
    }

    public void savePasswords() {
        if (this.pwManagerController == null) {
            return;
        }
        final List<SavedPassword> pws = this.pwManagerController.getSavedPasswords();
        final MasterKeyAdapter mkAdapter = this.pwManagerController.getMasterKeyAdapter();
        final Gson gson = new GsonBuilder().create();
        File workingDir = new File(System.getProperty("user.dir"));
        try {
            workingDir = Util.getWorkingDirectory();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final String nameGen = mkAdapter.encode("passwordManager.files", MPSiteType.GeneratedName, 1);
        final String passGen = mkAdapter.encode("passwordManager.files", MPSiteType.GeneratedMaximum, 1);
        final File file = new File(workingDir, nameGen);

        try {
            Util.encryptStringToFile(gson.toJson(pws), file, passGen);
        } catch (IOException | GeneralSecurityException e) {
            new Alert(AlertType.ERROR, "Couldn't encrypt the meta data to a file.\n" + "Please export them unencrypted, so you don't lose them.",
                    ButtonType.OK).showAndWait();
            e.printStackTrace();
        }
    }

    public void showAddPasswordDialog(final SavedPassword pw) {
        this.secondaryStage = new Stage();
        this.secondaryStage.initOwner(this.primaryStage);
        this.secondaryStage.initModality(Modality.WINDOW_MODAL);
        String title;
        if (pw == null) {
            title = "Add a new website";
        } else {
            title = "Edit website";
        }
        this.secondaryStage.setTitle(title);

        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AddLayout.fxml"));
            final Scene scene = new Scene(loader.load());
            this.secondaryStage.setScene(scene);
            this.secondaryStage.show();
            final AddController addController = loader.getController();
            addController.setMainApp(this);
            addController.setSavedPassword(pw);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void showLogin() {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/LoginManager.fxml"));
            final BorderPane loginManager = (BorderPane) loader.load();

            this.primaryStage.setTitle(MainApp.MASTER_PASSWORD);
            this.rootLayout.setCenter(loginManager);

            this.loginManagerController = loader.getController();
            this.loginManagerController.setMainApp(this);
            this.primaryStage.getScene().setOnKeyPressed(event -> {
            });
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void showPasswordManager(final MasterKeyAdapter mkAdapter, final String name) {
        try {
            final List<SavedPassword> pws = this.readSavedPasswords(mkAdapter);
            if (pws == null) {
                this.loginManagerController.showGenerating(false);
                return;
            }
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PasswordManager.fxml"));
            final AnchorPane passwordManager = (AnchorPane) loader.load();

            this.rootLayout.setCenter(passwordManager);

            this.primaryStage.setTitle(MainApp.MASTER_PASSWORD + " - " + name);

            this.pwManagerController = loader.getController();
            this.pwManagerController.setMainApp(this);
            this.pwManagerController.setMasterKeyAdapter(mkAdapter);
            this.primaryStage.setOnCloseRequest(event -> this.saveAndQuit());
            this.pwManagerController.addSavedPasswords(pws);
            this.pwManagerController.selectSavedPassword(0);

            this.primaryStage.getScene().setOnKeyPressed(event -> this.pwManagerController.handleKeyPressed(event));
            this.timeOut.play();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(final Stage primaryStage) {
        this.primaryStage = primaryStage;
        final Image img = new Image("file:res/icon.png");
        primaryStage.getIcons().add(img);
        this.timeOut = new Timeline(new KeyFrame(Duration.seconds(30), event -> this.logout()));
        this.timeOut.setCycleCount(Animation.INDEFINITE);

        this.initRootLayout();

        this.showLogin();
    }

    public void startTimeout() {
        this.timeOut.playFromStart();
    }

    public void stopTimeout() {
        this.timeOut.stop();
    }
}
