package usspg31.tourney.controller.controls;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class NumberTextField extends TextField {

	private IntegerProperty numberValue;

	public NumberTextField() {
		this.addEventFilter(KeyEvent.KEY_TYPED, event -> {
			if (!event.getCharacter().matches("[0-9]")) {
				event.consume();
			}
		});
		this.textProperty().addListener((ov, o, n) -> {
			try {
				this.setNumberValue(Integer.parseInt(this.getText()));
			} catch (Exception e) {
				this.setText(Integer.toString(this.getNumberValue()));
			}
		});
		this.numberValueProperty().addListener((ov, o, n) -> {
			if (!n.equals(Integer.parseInt(this.getText()))) {
				this.setText(n.toString());
			}
		});
	}

	/**
	 * @return the numberValue property
	 */
	public IntegerProperty numberValueProperty() {
		if (this.numberValue == null) {
			this.numberValue = new SimpleIntegerProperty();
		}
		return this.numberValue;
	}

	/**
	 * @return the value of the numberValue property
	 */
	public int getNumberValue() {
		return this.numberValueProperty().get();
	}

	/**
	 * @param value sets the new value for the numberValue property
	 */
	public void setNumberValue(int value) {
		this.numberValueProperty().set(value);
	}
}