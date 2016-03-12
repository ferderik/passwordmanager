package ferd.passwordManager.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class IntField extends TextField {
    private IntegerProperty value;
    private int minValue;
    private int maxValue;
    private boolean init = false;

    public IntField() {}

    public int getValue() {
        return this.value.getValue();
    }

    public void initValues(final int minValue, final int maxValue, final int initialValue) {
        if (this.init) {
            return;
        }
        this.init = true;
        if (minValue > maxValue) {
            throw new IllegalArgumentException("IntField min value " + this.minValue + " greater than max value " + this.maxValue);
        }
        if (maxValue < minValue) {
            throw new IllegalArgumentException("IntField max value " + this.minValue + " less than min value " + this.maxValue);
        }
        if (!(minValue <= initialValue && initialValue <= maxValue)) {
            throw new IllegalArgumentException("IntField initialValue " + initialValue + " not between " + this.minValue + " and " + this.maxValue);
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = new SimpleIntegerProperty(initialValue);
        this.setText(Integer.toString(initialValue));
        this.value.addListener((v, o, n) -> this.valueChanged(v, o, n));
        this.addEventFilter(KeyEvent.KEY_TYPED, event -> this.keyEvent(event));
        this.textProperty().addListener((v, o, n) -> this.textChanged(v, o, n));
    }

    private void keyEvent(final KeyEvent event) {
        if (!"0123456789+-".contains(event.getCharacter())) {
            event.consume();
        }
    }

    public void setValue(final int newValue) {
        this.value.setValue(newValue);
    }

    private void textChanged(final ObservableValue<? extends String> v, final String o, final String n) {
        if (n == null || "".equals(n)) {
            this.value.setValue(0);
            return;
        }

        String next = o;
        try {
            final int intValue = Integer.parseInt(n);
            if (this.minValue <= intValue && intValue <= this.maxValue) {
                next = n;
            }
        } catch (final NumberFormatException e) {
        }
        this.textProperty().setValue(next);

        this.value.set(Integer.parseInt(this.textProperty().get()));
    }

    private void valueChanged(final ObservableValue<? extends Number> v, final Number o, final Number n) {
        if (n == null) {
            this.setText("");
        } else {
            if (n.intValue() < this.minValue) {
                this.value.setValue(this.minValue);
                return;
            }

            if (n.intValue() > this.maxValue) {
                this.value.setValue(this.maxValue);
                return;
            }

            if (!(n.intValue() == 0 && (this.textProperty().get() == null || "".equals(this.textProperty().get())))) {
                this.setText(n.toString());
            }
        }
    }

    public IntegerProperty valueProperty() {
        return this.value;
    }
}
