package ferd.passwordManager.control;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lyndir.masterpassword.MPSiteType;
import com.lyndir.masterpassword.MPSiteTypeClass;

import ferd.passwordManager.MainApp;
import ferd.passwordManager.model.MPSiteTypeConverter;
import ferd.passwordManager.model.MasterKeyAdapter;
import ferd.passwordManager.model.SavedPassword;
import ferd.passwordManager.util.Util;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class PasswordManagerController {
    private static final Font DEFAULT_FONT = new Font("System", 25);
    private static final String AUTO_LOGOUT_ACTIVE = "After 30s without action you will be logged out";
    private static final String AUTO_LOGOUT_INACTIVE = "You will stay logged in";
    private static final String TITLE_EXPORT = "Select file to export passwords to";
    private static final String TITLE_IMPORT = "Select file to import passwords from";
    @FXML
    private SplitPane splitPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ListView<SavedPassword> passwordListView;
    @FXML
    private CheckBox autoLogout;
    @FXML
    private Label websiteName;
    @FXML
    private Button decrementCounter;
    @FXML
    private IntField counter;
    @FXML
    private Button incrementCounter;
    @FXML
    private ChoiceBox<MPSiteType> passwordTypes;
    @FXML
    private CheckBox hidePassword;
    @FXML
    private Label passwordLabel;
    private MainApp mainApp;
    private MasterKeyAdapter masterKeyAdapter;
    private SavedPassword currentPW;
    private final Timeline HIDE_PASSWORD_TIMELINE = new Timeline(new KeyFrame(Duration.seconds(10), event -> this.hidePassword()));
    private final Timeline UPDATE_PASSWORD = new Timeline(new KeyFrame(Duration.seconds(2), event -> this.updatePassword()));

    public void addSavedPassword(final SavedPassword pw) {
        this.passwordListView.getItems().add(pw);
        this.passwordListView.scrollTo(pw);
        this.passwordListView.getSelectionModel().select(pw);
    }

    public void addSavedPasswords(final List<SavedPassword> pws) {
        final List<SavedPassword> pLVItems = this.passwordListView.getItems();
        for (final SavedPassword pw : pws) {
            if (!pLVItems.contains(pw)) {
                pLVItems.add(pw);
            }
        }
        if (this.passwordListView.getItems().size() > 0 && this.passwordListView.getSelectionModel().getSelectedIndex() == -1) {
            this.passwordListView.scrollTo(0);
            this.passwordListView.getSelectionModel().select(0);
        }
    }

    private void adjustFontSize() {
        Font old = DEFAULT_FONT;
        final String text = this.passwordLabel.getText();
        while (Util.computeTextWidth(old, text, 0) > this.passwordLabel.getWidth() && old.getSize() >= 0) {
            old = new Font(old.getName(), old.getSize() - 1);
        }
        this.passwordLabel.setFont(old);
    }

    private void changeType(final MPSiteType newValue) {
        if (newValue == null || this.currentPW == null) {
            return;
        }
        this.currentPW.setPasswordType(newValue);
        this.updatePassword();
    }

    private void copyPassword() {
        this.mainApp.resetTimeout();
        this.passwordLabel.setText("copied for 10 seconds");
        this.UPDATE_PASSWORD.playFromStart();
        StringSelection pwsel = new StringSelection(
                this.masterKeyAdapter.encode(this.currentPW.getWebsite(), this.currentPW.getPasswordType(), this.currentPW.getCounter()));
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(pwsel, null);
        this.HIDE_PASSWORD_TIMELINE.playFromStart();
        pwsel = null;
    }

    @FXML
    public void decCounter() {
        int c = Integer.parseInt(this.counter.getText()) - 1;
        c = c < 1 ? 1 : c;
        this.counter.setText(Integer.toString(c));
        this.currentPW.setCounter(c);
        this.updatePassword();
    }

    public void editSavedPassword(final SavedPassword pw) {
        this.mainApp.showAddPasswordDialog(pw);
    }

    @FXML
    private void exportSavedPasswords() {
        this.mainApp.resetTimeout();
        this.portSavedPasswords(false);
    }


    private File getFileForExportImport(final boolean im) {
        final FileChooser fc = new FileChooser();
        File workingDir = null;
        try {
            workingDir = Util.getWorkingDirectory();
            fc.setInitialDirectory(workingDir);
            fc.setTitle(im ? TITLE_IMPORT : TITLE_EXPORT);
            if (im) {
                return fc.showOpenDialog(this.mainApp.getPrimaryStage());
            } else {
                return fc.showSaveDialog(this.mainApp.getPrimaryStage());
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public MasterKeyAdapter getMasterKeyAdapter() {
        return this.masterKeyAdapter;
    }

    public List<SavedPassword> getSavedPasswords() {
        return this.passwordListView.getItems();
    }

    public void handleKeyPressed(final KeyEvent event) {
        final KeyCode code = event.getCode();
        if (KeyCode.PLUS.equals(code) || KeyCode.ADD.equals(code)) {
            this.incCounter();
            event.consume();
        } else if (KeyCode.MINUS.equals(code) || KeyCode.SUBTRACT.equals(code)) {
            this.decCounter();
            event.consume();
        }
    }

    private void hidePassword() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
    }

    @FXML
    private void importSavedPasswords() {
        this.mainApp.resetTimeout();
        this.portSavedPasswords(true);
    }

    @FXML
    private void incCounter() {
        int c = Integer.parseInt(this.counter.getText()) + 1;
        c = c < 0 ? Integer.MAX_VALUE : c;
        this.counter.setText(Integer.toString(c));
        this.currentPW.setCounter(c);
        this.updatePassword();
    }

    @FXML
    private void initialize() {
        this.passwordListView.setCellFactory(lv -> new SavedPasswordListViewCell(this));
        this.passwordTypes.getItems().setAll(
                Arrays.stream(MPSiteType.values()).filter(type -> type.getTypeClass() == MPSiteTypeClass.Generated).collect(Collectors.toList()));
        this.passwordTypes.setConverter(new MPSiteTypeConverter());
        this.counter.initValues(1, Integer.MAX_VALUE, 1);
        this.counter.textProperty().addListener((v, o, n) -> this.updatePassword());
        this.counter.setTooltip(Util.hackTooltipTiming(new Tooltip("Incase you need a new password"), 125));
        final MenuItem importMenuItem = new MenuItem("Import unencrypted");
        importMenuItem.setOnAction(event -> this.importSavedPasswords());
        final MenuItem exportMenuItem = new MenuItem("Export unencrypted");
        exportMenuItem.setOnAction(event -> this.exportSavedPasswords());
        this.autoLogout.setTooltip(Util.hackTooltipTiming(new Tooltip(AUTO_LOGOUT_ACTIVE), 125));

        this.passwordListView.setContextMenu(new ContextMenu(importMenuItem, exportMenuItem));

        this.passwordListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> this.showSavedPasswordDetails(newValue));
        this.hidePassword.setOnAction(event -> this.updatePassword());
        this.passwordTypes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.changeType(newValue));
        this.passwordLabel.setOnMouseClicked(event -> this.copyPassword());
        this.passwordLabel.setTooltip(Util.hackTooltipTiming(new Tooltip("click to copy"), 125));

        this.splitPane.widthProperty().addListener((o, old, n) -> this.splitPane.setDividerPosition(0, 0.33d));
        this.passwordLabel.widthProperty().addListener((o, old, n) -> this.adjustFontSize());
        this.passwordLabel.textProperty().addListener((o, old, n) -> this.adjustFontSize());
        this.borderPane.widthProperty().addListener((o, old, n) -> this.adjustFontSize());
        this.updateButtonState();
    }

    @FXML
    private void logout() {
        this.hidePassword();
        this.mainApp.logout();
        this.masterKeyAdapter = null;
    }

    private void portSavedPasswords(final boolean im) {
        final File target = this.getFileForExportImport(im);
        if (target == null) {
            return;
        }
        final Gson gson = new Gson();
        final Type listType = new TypeToken<List<SavedPassword>>() {}.getType();
        if (im) {
            try (final BufferedReader br = new BufferedReader(new FileReader(target))) {
                final List<SavedPassword> pws = gson.fromJson(br, listType);
                pws.forEach(pw -> pw.getPasswordType());
                for (final SavedPassword pw : pws) {
                    if (pw.getPasswordType() == null) {
                        throw new Exception();
                    }
                }
                this.addSavedPasswords(pws);
            } catch (final IOException e) {
                e.printStackTrace();
                new Alert(AlertType.ERROR, "The selected file couldn't be read.\n Please choose a different file.", ButtonType.OK).showAndWait();
            } catch (final JsonSyntaxException e) {
                e.printStackTrace();
                new Alert(AlertType.ERROR, "The selected file couldn't be parsed as a list of passwords.", ButtonType.OK).showAndWait();
            } catch (final Exception e) {
                e.printStackTrace();
                new Alert(AlertType.ERROR, "The file couldn't be parsed correctly.\n"
                        + "This is probably due to it being generated by an older version.\n" + "Please use a converter tool.", ButtonType.OK)
                                .showAndWait();
            }
        } else {
            try (final BufferedWriter bw = new BufferedWriter(new FileWriter(target))) {
                final String json = gson.toJson(this.getSavedPasswords());
                bw.write(json);
            } catch (final IOException e) {
                e.printStackTrace();
                new Alert(AlertType.ERROR, "Couldn't write to that file.\n Please choose a different file.", ButtonType.OK).showAndWait();
            }
        }

    }

    public void refreshPasswords() {
        this.websiteName.setText(this.currentPW.getWebsite());
        this.updatePassword();
        final int i = this.passwordListView.getSelectionModel().getSelectedIndex();
        final ObservableList<SavedPassword> l = this.passwordListView.getItems();
        this.passwordListView.setItems(null);
        this.passwordListView.setItems(l);
        this.passwordListView.getSelectionModel().select(i);
    }

    public void removePassword(final SavedPassword pw) {
        this.passwordListView.getItems().remove(pw);
    }

    public void selectSavedPassword(final int i) {
        this.passwordListView.getSelectionModel().select(i);
        this.passwordListView.scrollTo(i);
    }

    public void setMainApp(final MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setMasterKeyAdapter(final MasterKeyAdapter mkAdapter) {
        this.masterKeyAdapter = mkAdapter;
    }

    @FXML
    private void showAddWebsiteDialog() {
        this.mainApp.showAddPasswordDialog(null);
    }

    private void showSavedPasswordDetails(final SavedPassword newValue) {
        this.mainApp.resetTimeout();
        if (this.currentPW == newValue) {
            return;
        }
        this.currentPW = newValue;
        if (newValue != null && newValue.getWebsite() != null) {
            this.websiteName.setText(newValue.getWebsite());
            this.counter.setText(Integer.toString(newValue.getCounter()));
            this.passwordTypes.getSelectionModel().select(newValue.getPasswordType());
            this.hidePassword.setSelected(true);
            this.updatePassword();
        } else {
            this.websiteName.setText("");
            this.counter.setText("1");
            this.passwordTypes.getSelectionModel().select(MPSiteType.GeneratedMaximum);
            this.passwordLabel.setText("");
        }
        this.updateButtonState();
    }

    @FXML
    private void toggleAutoLogout() {
        if (this.mainApp.getTimeoutRunning()) {
            this.autoLogout.getTooltip().setText(AUTO_LOGOUT_INACTIVE);
            this.mainApp.stopTimeout();
        } else {
            this.autoLogout.getTooltip().setText(AUTO_LOGOUT_ACTIVE);
            this.mainApp.startTimeout();
        }
    }

    private void updateButtonState() {
        final boolean flag = this.currentPW == null;
        this.incrementCounter.setDisable(flag);
        this.decrementCounter.setDisable(flag);
        this.hidePassword.setDisable(flag);
        this.passwordTypes.setDisable(flag);
        this.counter.setDisable(flag);
    }

    private void updatePassword() {
        this.mainApp.resetTimeout();
        if (this.currentPW == null) {
            return;
        }

        this.currentPW.setCounter(Integer.parseInt(this.counter.getText()));
        String pw = this.masterKeyAdapter.encode(this.currentPW.getWebsite(), this.currentPW.getPasswordType(), this.currentPW.getCounter());
        if (this.hidePassword.isSelected()) {
            pw = String.format("%" + Integer.toString(pw.length()) + "s", "").replaceAll(" ", "•");
        }
        this.passwordLabel.setText(pw);
        pw = null;
    }
}
