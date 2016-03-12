package ferd.passwordManager.control;

import ferd.passwordManager.model.SavedPassword;
import ferd.passwordManager.util.Util;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SavedPasswordListViewCell extends DragDropCell<SavedPassword> {
    private static final Font WEBSITE_FONT = new Font(14);
    private static final Font NOTES_FONT = new Font(10);
    private final HBox hbox = new HBox();
    private final VBox vbox = new VBox();
    private final Label website = new Label();
    private final Label notes = new Label();
    private final Button delete = new Button();
    private final PasswordManagerController pwManagerController;
    private final VBox emptyBox = new VBox();
    private final Tooltip tt = new Tooltip("Double click to edit");

    public SavedPasswordListViewCell(final PasswordManagerController pwManagerController) {
        this.pwManagerController = pwManagerController;

        this.vbox.prefWidthProperty().bind(this.hbox.widthProperty().subtract(this.delete.widthProperty()));
        this.hbox.getChildren().addAll(this.vbox, this.delete);
        this.hbox.setAlignment(Pos.CENTER);
        this.vbox.getChildren().addAll(this.website, this.notes);
        this.website.setFont(WEBSITE_FONT);
        this.notes.setFont(NOTES_FONT);
        this.notes.getStyleClass().add("notes");
        this.delete.getStyleClass().add("deleteButton");
        this.delete.setMinSize(25, 25);

        this.emptyBox.getChildren().addAll(new Label(), new Label());

        this.setOnMouseClicked(event -> this.editPassword(event));
        Util.hackTooltipTiming(this.tt, 125);

        this.delete.setVisible(false);
        this.setOnMouseEntered(event -> this.delete.setVisible(true));
        this.setOnMouseExited(event -> this.delete.setVisible(false));
        this.delete.setOnAction(event -> this.deletePasswordFromButton((Button) event.getSource()));
    }

    private void deletePasswordFromButton(final Button source) {
        final Alert alert = new Alert(AlertType.CONFIRMATION, "Do you really want to delete this password?", ButtonType.YES, ButtonType.NO);
        final ButtonType res = alert.showAndWait().orElse(null);
        if (res == ButtonType.YES) {
            final SavedPassword pw = (SavedPassword) source.getUserData();
            this.pwManagerController.removePassword(pw);
        }
    }

    private void editPassword(final MouseEvent event) {
        if (event.getClickCount() >= 2) {
            if (this.delete != null && this.delete.getUserData() != null) {
                this.pwManagerController.editSavedPassword((SavedPassword) this.delete.getUserData());
            }
        }
    }

    @Override
    public void updateItem(final SavedPassword pw, final boolean empty) {
        super.updateItem(pw, empty);
        if (pw == null || pw.getWebsite() == null) {
            this.setGraphic(this.emptyBox);
            this.setTooltip(null);
            return;
        }
        this.website.setText(pw.getWebsite());
        if (pw.getNotes() != null) {
            this.notes.setText(pw.getNotes());
        }
        this.delete.setUserData(pw);
        this.setTooltip(this.tt);
        this.setGraphic(this.hbox);
    }
}
