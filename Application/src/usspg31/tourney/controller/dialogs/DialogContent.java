package usspg31.tourney.controller.dialogs;

import javafx.scene.Node;

public interface DialogContent<P, R> {

	default void setDialogRoot(ModalDialog<P, R> dialogRoot) { }
	default Node getRoot() { return (Node) this; }
	default void setProperties(P properties) { }
	default R getReturnValue() { return null; }

}