package ferd.passwordManager.control;

import javafx.scene.control.PasswordField;

public class PersistentPromptPasswordField extends PasswordField {

    public PersistentPromptPasswordField() {
        this("");
    }

    public PersistentPromptPasswordField(final String prompt) {
        this.setPromptText(prompt);
        this.getStyleClass().add("persistent-prompt");
        this.refreshPromptVisibility();

        this.textProperty().addListener(observable -> this.refreshPromptVisibility());
    }

    private void refreshPromptVisibility() {
        final String text = this.getText();
        if (text == null || text.isEmpty()) {
            this.getStyleClass().remove("no-prompt");
        } else {
            if (!this.getStyleClass().contains("no-prompt")) {
                this.getStyleClass().add("no-prompt");
            }
        }
    }
}
