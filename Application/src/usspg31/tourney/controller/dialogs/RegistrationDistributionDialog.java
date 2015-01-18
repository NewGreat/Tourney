package usspg31.tourney.controller.dialogs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import usspg31.tourney.controller.controls.NumberTextField;
import usspg31.tourney.controller.dialogs.modal.DialogButtons;
import usspg31.tourney.controller.dialogs.modal.IModalDialogProvider;
import usspg31.tourney.controller.dialogs.modal.ModalDialog;
import usspg31.tourney.controller.dialogs.modal.SimpleDialog;

public class RegistrationDistributionDialog extends VBox implements
	IModalDialogProvider<String, Integer> {

    private static final Logger log = Logger
	    .getLogger(RegistrationDistributionDialog.class.getName());

    @FXML
    private NumberTextField textFieldNumberOfRegistrators;

    public RegistrationDistributionDialog() {
	try {
	    FXMLLoader loader = new FXMLLoader(this.getClass().getResource(
		    "/ui/fxml/dialogs/registration-distribution-dialog.fxml"));
	    loader.setController(this);
	    loader.setRoot(this);
	    loader.load();
	} catch (IOException e) {
	    log.log(Level.SEVERE, e.getMessage(), e);
	}
    }

    @FXML
    private void initialize() {

    }

    @Override
    public void setProperties(String properties) {

    }

    @Override
    public Integer getReturnValue() {
	if (this.textFieldNumberOfRegistrators.getText().length() == 0) {
	    return null;
	} else {
	    return Integer
		    .valueOf(this.textFieldNumberOfRegistrators.getText());
	}
    }

    @Override
    public boolean hasNoInput() {
	return this.textFieldNumberOfRegistrators.getText().length() == 0
		|| this.getReturnValue() <= 1;
    }

    @FXML
    private void onButtonDistributionHelpClicked(ActionEvent event) {
	new SimpleDialog<>(
		"Diese Funktion ermöglicht es Ihnen, die Anwesenheitskontrolle von Spielern "
			+ "an mehreren Arbeitsplätzen vorzunehmen.\n"
			+ "Gehen Sie dazu folgendermaßen vor:\n\n"
			+ " \u2022 Tragen Sie die Anzahl der Arbeitsplätze inklusive des "
			+ "Administrationsrechners ein,\n    an denen Anmeldungen vorgenommen werden "
			+ "sollen. Dies müssen also mindestens zwei sein.\n\n"
			+ " \u2022 Bestätigen Sie Ihre Eingabe und speichern Sie das Event ab. Hierzu ist "
			+ "ein portables Speichermedium\n    wie ein USB-Stick zu empfehlen, um "
			+ "die Daten transportieren zu können.\n\n"
			+ " \u2022 Öffnen Sie das gespeicherte Event an den anderen Anmeldungsarbeitsplätzen. "
			+ "Wählen Sie eine einzigartige\n    Nummer für jeden Arbeitsplatz aus. "
			+ "Die Software wechselt automatisch in die Ansicht der angemeldeten Spieler.\n    "
			+ "Hier können Anmeldungen wie gehabt vorgenommen werden.\n\n"
			+ " \u2022 Die Anmeldungsarbeitsplätze speichern die Anmeldungen über das "
			+ "Speichern-Symbol wieder auf\n    dem Speichermedium ab. Der Administrator "
			+ "betätigt die Import-Schaltfläche in der Spieleranmeldung\n    und wählt "
			+ "die Eventdatei  vom Anmeldungsarbeitsplatz aus.\n    Alle Daten werden "
			+ "importiert und zusammengefügt.").modalDialog()
		.dialogButtons(DialogButtons.OK)
		.title("Informationen zur Anmeldungsverteilung").show();
    }

    @Override
    public void initModalDialog(ModalDialog<String, Integer> modalDialog) {
	modalDialog.title("Anmeldung verteilen").dialogButtons(
		DialogButtons.OK_CANCEL);
    }
}