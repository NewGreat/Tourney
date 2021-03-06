package usspg31.tourney.controller.dialogs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import usspg31.tourney.controller.PreferencesManager;
import usspg31.tourney.controller.controls.MaterialTextField;
import usspg31.tourney.controller.controls.MaterialTextField.ValidationResult;
import usspg31.tourney.controller.dialogs.modal.DialogButtons;
import usspg31.tourney.controller.dialogs.modal.DialogResult;
import usspg31.tourney.controller.dialogs.modal.IModalDialogProvider;
import usspg31.tourney.controller.dialogs.modal.ModalDialog;
import usspg31.tourney.controller.dialogs.modal.SimpleDialog;
import usspg31.tourney.model.Event;
import usspg31.tourney.model.IdentificationManager;
import usspg31.tourney.model.Player;
import usspg31.tourney.model.Tournament;
import usspg31.tourney.model.Tournament.ExecutionState;

public class PlayerEditorDialog extends VBox implements
        IModalDialogProvider<Object, Player> {

    private static final Logger log = Logger.getLogger(PlayerEditorDialog.class
            .getName());

    @FXML private MaterialTextField textFieldFirstName;
    @FXML private MaterialTextField textFieldLastName;
    @FXML private MaterialTextField textFieldEmail;
    @FXML private MaterialTextField textFieldNickname;
    @FXML private TableView<Tournament> tableTournaments;
    @FXML private Button buttonAddTournament;
    @FXML private Button buttonRemoveTournament;
    @FXML private CheckBox checkBoxPayed;

    private TableColumn<Tournament, String> tableColumnTournamentName;
    private ObservableList<Tournament> registeredTournaments;

    private ModalDialog<ObservableList<Tournament>, Tournament> tournamentSelectionDialog;

    private Player loadedPlayer;
    private Event loadedEvent;

    public PlayerEditorDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(
                    "/ui/fxml/dialogs/player-pre-registration-dialog.fxml"),
                    PreferencesManager.getInstance().getSelectedLanguage()
                            .getLanguageBundle());
            loader.setController(this);
            loader.setRoot(this);
            loader.load();

            Pattern emailPattern = Pattern.compile(
                    "^[0-9a-z._%+-]+@[0-9a-z.-]+\\.[a-z]{2,}$",
                    Pattern.CASE_INSENSITIVE);

            this.textFieldEmail
                    .setInputValidationCallback(input -> {
                        if (input.isEmpty()) {
                            return ValidationResult.ok();
                        } else if (!input.isEmpty()
                                && emailPattern.matcher(input).matches()) {
                            return ValidationResult.success();
                        } else {
                            return ValidationResult
                                    .error(PreferencesManager
                                            .getInstance()
                                            .localizeString(
                                                    "dialogs.personediting.errors.invalidmail"));
                        }
                    });
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @FXML
    private void initialize() {
        this.tournamentSelectionDialog = new TournamentSelectionDialog()
                .modalDialog();

        this.registeredTournaments = FXCollections.observableArrayList();
        this.initTournamentTable();
    }

    private void initTournamentTable() {
        this.tableColumnTournamentName = new TableColumn<>(PreferencesManager
                .getInstance().localizeString(
                        "dialogs.playerpreregistration.tournamentname"));
        this.tableColumnTournamentName.setCellValueFactory(cellData -> cellData
                .getValue().nameProperty());
        this.tableTournaments.getColumns().add(this.tableColumnTournamentName);

        this.tableColumnTournamentName.prefWidthProperty().bind(
                this.tableTournaments.widthProperty());

        this.tableTournaments
                .setPlaceholder(new Text(PreferencesManager.getInstance()
                        .localizeString("tableplaceholder.notournaments")));
    }

    private void updateTournamentList() {
        this.registeredTournaments.clear();
        for (Tournament tournament : this.loadedEvent.getTournaments()) {
            for (Player player : tournament.getRegisteredPlayers()) {
                if (player.getId().equals(this.loadedPlayer.getId())) {
                    this.registeredTournaments.add(tournament);
                }
            }
        }
    }

    @Override
    public void setProperties(Object properties) {
        if (properties instanceof Player) {
            this.loadPlayer((Player) ((Player) properties).clone());
        } else if (properties instanceof Event) {
            this.loadedEvent = (Event) properties;
        }

        if (this.loadedEvent != null && this.loadedPlayer != null) {
            this.updateTournamentList();

            /* Create bindings for the tournament table */
            this.tableTournaments.setItems(this.registeredTournaments);
        }
    }

    private void loadPlayer(Player player) {
        if (this.loadedPlayer != null) {
            this.unloadPlayer();
        }

        this.loadedPlayer = player;

        /* Bind the values of the text fields */
        this.textFieldFirstName.textProperty().bindBidirectional(
                this.loadedPlayer.firstNameProperty());
        this.textFieldLastName.textProperty().bindBidirectional(
                this.loadedPlayer.lastNameProperty());
        this.textFieldEmail.textProperty().bindBidirectional(
                this.loadedPlayer.mailAdressProperty());
        this.textFieldNickname.textProperty().bindBidirectional(
                this.loadedPlayer.nickNameProperty());
        this.checkBoxPayed.selectedProperty().bindBidirectional(
                this.loadedPlayer.payedProperty());

        /* Bind the remove button's availability to the selected item */
        this.buttonRemoveTournament.disableProperty().bind(
                this.tableTournaments.getSelectionModel()
                        .selectedItemProperty().isNull());
    }

    private void unloadPlayer() {
        /* Unbind the values of the text fields */
        this.textFieldFirstName.textProperty().unbindBidirectional(
                this.loadedPlayer.firstNameProperty());
        this.textFieldLastName.textProperty().unbindBidirectional(
                this.loadedPlayer.lastNameProperty());
        this.textFieldEmail.textProperty().unbindBidirectional(
                this.loadedPlayer.mailAdressProperty());
        this.textFieldNickname.textProperty().unbindBidirectional(
                this.loadedPlayer.nickNameProperty());
        this.checkBoxPayed.selectedProperty().unbindBidirectional(
                this.loadedPlayer.payedProperty());

        /* Unbind the remove button's availability to the selected item */
        this.buttonRemoveTournament.disableProperty().unbind();

        this.loadedPlayer = null;
    }

    @Override
    public Player getReturnValue() {
        return this.loadedPlayer;
    }

    @Override
    public void initModalDialog(ModalDialog<Object, Player> modalDialog) {
        modalDialog.title("dialogs.playerpreregistration").dialogButtons(
                DialogButtons.OK_CANCEL);
    }

    @Override
    public String getInputErrorString() {
        /* Check if there is a player with the same key data */
        int duplicatePlayers = 0;
        for (Player existentPlayer : this.loadedEvent.getRegisteredPlayers()) {
            if (existentPlayer.getId().equals(
                    String.valueOf(IdentificationManager
                            .generateId(this.loadedPlayer)))) {
                duplicatePlayers++;
            }
        }

        if (this.loadedPlayer.getFirstName().equals("")
                && this.loadedPlayer.getLastName().equals("")
                && this.loadedPlayer.getNickName().equals("")) {
            return PreferencesManager.getInstance().localizeString(
                    "dialogs.playerpreregistration.errors.emptydata");
        }
        if (duplicatePlayers > 1) {
            /* More than this player itself exists with the same data */
            return PreferencesManager.getInstance().localizeString(
                    "dialogs.playerpreregistration.errors.duplicate");
        }
        if (!this.loadedPlayer.hasValidMailAddress()
                && this.textFieldEmail.getText().length() > 0) {
            /* The entered mail address does not withstand simple regex matching */
            return PreferencesManager.getInstance().localizeString(
                    "dialogs.personediting.errors.invalidmail");
        }

        return null;
    };

    @FXML
    private void onButtonAddTournamentClicked(ActionEvent event) {
        ObservableList<Tournament> unregisteredTournaments = FXCollections
                .observableArrayList();
        for (Tournament tournament : this.loadedEvent.getTournaments()) {
            boolean playerRegistered = false;
            for (Player player : tournament.getRegisteredPlayers()) {
                if (player.getId().equals(this.loadedPlayer.getId())) {
                    playerRegistered = true;
                }
            }
            if (!playerRegistered) {
                unregisteredTournaments.add(tournament);
            }
        }

        this.tournamentSelectionDialog
                .properties(unregisteredTournaments)
                .onResult(
                        (result, returnValue) -> {
                            if (result == DialogResult.OK
                                    && returnValue != null) {
                                returnValue.getRegisteredPlayers().add(
                                        this.loadedPlayer);
                                this.updateTournamentList();
                            }
                        }).show();
    }

    @FXML
    private void onButtonRemoveTournamentClicked(ActionEvent event) {
        Tournament selectedTournament = this.tableTournaments
                .getSelectionModel().getSelectedItem();

        if (selectedTournament.getExecutionState() == ExecutionState.CURRENTLY_EXECUTED) {
            new SimpleDialog<>(
                    PreferencesManager
                            .getInstance()
                            .localizeString(
                                    "registrationphase.dialogs.removetournament.errors.tournamentcurrentlyexecuted"))
                    .modalDialog().title("dialogs.titles.error")
                    .dialogButtons(DialogButtons.OK).show();
        } else if (selectedTournament.getExecutionState() == ExecutionState.CURRENTLY_EXECUTED) {
            new SimpleDialog<>(
                    PreferencesManager
                            .getInstance()
                            .localizeString(
                                    "registrationphase.dialogs.removetournament.errors.tournamentexecuted"))
                    .modalDialog().title("dialogs.titles.error")
                    .dialogButtons(DialogButtons.OK).show();
        } else {
            new SimpleDialog<>(
                    PreferencesManager
                            .getInstance()
                            .localizeString(
                                    "registrationphase.dialogs.removetournament.message.before")
                            + " \""
                            + this.loadedPlayer.getFirstName()
                            + " "
                            + this.loadedPlayer.getLastName()
                            + "\" "
                            + PreferencesManager
                                    .getInstance()
                                    .localizeString(
                                            "registrationphase.dialogs.removetournament.message.middle")
                            + " \""
                            + selectedTournament.getName()
                            + "\" "
                            + PreferencesManager
                                    .getInstance()
                                    .localizeString(
                                            "registrationphase.dialogs.removetournament.message.after"))
                    .modalDialog()
                    .title("registrationphase.dialogs.removetournament.title")
                    .dialogButtons(DialogButtons.YES_NO)
                    .onResult(
                            (result, returnValue) -> {
                                if (result == DialogResult.YES) {
                                    Player playerToRemove = null;
                                    for (Player player : selectedTournament
                                            .getRegisteredPlayers()) {
                                        if (player.getId().equals(
                                                this.loadedPlayer.getId())) {
                                            playerToRemove = player;
                                            break;
                                        }
                                    }

                                    selectedTournament.getRegisteredPlayers()
                                            .remove(playerToRemove);
                                    this.updateTournamentList();
                                }
                            }).show();

            this.updateTournamentList();
        }
    }
}
