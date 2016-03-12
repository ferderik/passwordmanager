package ferd.passwordManager.control;

import ferd.passwordManager.MainApp;
import ferd.passwordManager.model.SavedPassword;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AddController {
    @FXML
    private TextField websiteField;
    @FXML
    private TextField notesField;
    @FXML
    private Button okButton;
    private MainApp mainApp;
    private SavedPassword pw;

    @FXML
    private void doAddWebsite() {
        final String website = this.websiteField.getText();
        final String notes = this.notesField.getText();
        if (website.length() <= 0) {
            return;
        }
        if (this.pw == null) {
            final SavedPassword pw = new SavedPassword(website, 1, notes);
            this.mainApp.doAddWebsite(pw);
        } else {
            this.pw.setWebsite(website);
            this.pw.setNotes(notes);
            this.mainApp.doRefreshWebsites();
        }
    }

    public void setMainApp(final MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setSavedPassword(final SavedPassword pw) {
        this.pw = pw;
        if (pw == null) {
            return;
        }
        this.websiteField.setText(pw.getWebsite());
        this.notesField.setText(pw.getNotes());
    }
}
