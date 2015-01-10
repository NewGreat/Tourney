package usspg31.tourney.controller.dialogs;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import usspg31.tourney.model.Player;

public class AttendenceDialogController extends VBox implements DialogContent<List<Player>, List<Player>> {

	private final static Logger log = Logger.getLogger(AttendenceDialogController.class.getName());

	@FXML private TableView<String> tableRegisteredPlayers;
	@FXML private TableView<String> tableAttendingPlayers;
	@FXML private Button buttonAddAttendee;
	@FXML private Button buttonRemoveAttendee;
	@FXML private Button buttonStartTournament;

	public AttendenceDialogController() {
		try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/ui/fxml/dialogs/attendence-dialog.fxml"));
			log.info(loader.getLocation().toString());
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void setDialogRoot(ModalDialog<List<Player>, List<Player>> dialogRoot) {
		this.buttonStartTournament.setOnAction(event -> {
			// TODO: ask the user if he really intends to start the tournament
			dialogRoot.exitWith(DialogResult.OK);
		});
	}

	@Override
	public void setProperties(List<Player> properties) {
		// TODO: add the given list to the registered players table
	}

	@Override
	public List<Player> getReturnValue() {
		// TODO: return the content of the attending players table
		return null;
	}

	@Override
	public Node getRoot() {
		return this;
	}

}
