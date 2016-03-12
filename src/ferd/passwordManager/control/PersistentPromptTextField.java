package ferd.passwordManager.control;

import javafx.scene.control.TextField;

public class PersistentPromptTextField extends TextField {

    public PersistentPromptTextField() {
        this("", "");
    }

    public PersistentPromptTextField(final String text, final String prompt) {
        super(text);
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
