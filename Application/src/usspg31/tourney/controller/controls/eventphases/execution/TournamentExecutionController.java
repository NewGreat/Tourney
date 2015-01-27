package usspg31.tourney.controller.controls.eventphases.execution;

import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import usspg31.tourney.controller.RoundTimer;
import usspg31.tourney.controller.controls.PairingView;
import usspg31.tourney.controller.controls.TournamentUser;
import usspg31.tourney.controller.dialogs.PairingScoreDialog;
import usspg31.tourney.controller.dialogs.PairingScoreDialog.PairingEntry;
import usspg31.tourney.controller.dialogs.modal.DialogResult;
import usspg31.tourney.controller.dialogs.modal.ModalDialog;
import usspg31.tourney.model.GamePhase;
import usspg31.tourney.model.Pairing;
import usspg31.tourney.model.PlayerScore;
import usspg31.tourney.model.RoundGeneratorFactory;
import usspg31.tourney.model.Tournament;

public class TournamentExecutionController implements TournamentUser {

    private static final Logger log = Logger
            .getLogger(TournamentExecutionController.class.getName());

    @FXML private PairingView pairingView;
    @FXML private Button buttonStartRound;
    @FXML private Button buttonEnterResult;

    @FXML private Label labelTime;
    @FXML private Button buttonAddTime;
    @FXML private Button buttonPauseTime;
    @FXML private Button buttonResumeTime;
    @FXML private Button buttonResetTime;
    @FXML private Button buttonSubtractTime;

    private RoundTimer roundTimer;

    private Tournament loadedTournament;
    private RoundGeneratorFactory roundGenerator = new RoundGeneratorFactory();

    private final ModalDialog<PairingEntry, Pairing> pairingScoreDialog;

    public TournamentExecutionController() {
        this.pairingScoreDialog = new PairingScoreDialog().modalDialog();
    }

    @FXML
    public void initialize() {
    }

    @Override
    public void loadTournament(Tournament tournament) {
        log.info("Loading Tournament");
        this.loadedTournament = tournament;
        this.loadedTournament.getRemainingPlayers().addAll(this.loadedTournament.getAttendingPlayers());

        this.pairingView.SelectedRoundProperty().addListener((ov, o, n) -> {
            if (n.intValue() > o.intValue()) {
                //this.updateRoundTimer();
            }
        });

        this.pairingView.loadTournament(tournament);

        this.buttonEnterResult.disableProperty().bind(
                this.pairingView.selectedPairingProperty().isNull());

        if (tournament.getRounds().size() == 0) {
            this.generateRound();
        }

        //this.updateRoundTimer();
    }

    private void updateRoundTimer() {
        int roundDuration = -1;
        int round = 0;
        for (GamePhase phase : this.loadedTournament.getRuleSet().getPhaseList()) {
            if ((round += phase.getRoundCount()) > this.loadedTournament.getRounds().size()) {
                roundDuration = (int) phase.getRoundDuration().getSeconds();
            }
        }
        if (roundDuration == -1) {
            roundDuration = 5 * 60; // 5 seconds standard time
        }
        this.roundTimer = new RoundTimer(roundDuration);

        this.labelTime.textProperty().unbind();
        this.labelTime.textProperty().bind(
                this.roundTimer.getTimerDuration().asString().concat("s"));
        this.buttonAddTime.setOnAction(event -> {
            this.roundTimer.addTime();
        });
        this.buttonSubtractTime.setOnAction(event -> {
            this.roundTimer.subtractTime();
        });
        this.buttonPauseTime.setOnAction(event -> {
            this.roundTimer.pause();
        });
        this.buttonResumeTime.setOnAction(event -> {
            this.roundTimer.resume();
        });
        this.buttonResetTime.setOnAction(event -> {
            this.roundTimer.reset();
        });
    }

    @Override
    public void unloadTournament() {
        log.info("Unloading Tournament");
        this.pairingView.unloadTournament();
    }

    @FXML
    private void onButtonStartRoundClicked(ActionEvent event) {
        log.finer("Start Round Button was clicked");
        this.generateRound();
    }

    private void generateRound() {
        log.info("Generating next round");
        this.loadedTournament.getRounds().add(
                this.roundGenerator.generateRound(this.loadedTournament));
        this.buttonStartRound.setDisable(true);
    }

    @FXML
    private void onButtonEnterResultClicked(ActionEvent event) {
        log.info("Enter Result Button was clicked");
        // FIXME: if the user selects 0 as a score for a player, the value doesn't get set
        this.pairingScoreDialog
        .properties(new PairingEntry(this.loadedTournament, this.pairingView.getSelectedPairing()))
        .onResult((result, value) -> {
            if (result == DialogResult.OK) {
                for (int i = 0; i < value.getScoreTable().size(); i++) {
                    PlayerScore score = value.getScoreTable().get(i);
                    PlayerScore selectedScore = this.pairingView.getSelectedPairing().getScoreTable().get(i);
                    for (int j = 0; j < score.getScore().size(); j++) {
                        Integer newScore = score.getScore().get(j);
                        selectedScore.getScore().clear();
                        selectedScore.getScore().add(newScore);
                    }
                    //this.loadedTournament.getScoreTable().
                }
                this.pairingView.updatePairings();
            }
        })
        .show();

        // check, if all pairings have a score
        boolean roundFinished = true;
        roundFinishCheck:
        for (Pairing pairing : this.loadedTournament.getRounds().get(
                this.pairingView.getSelectedRound()).getPairings()) {
            for (PlayerScore playerScore : pairing.getScoreTable()) {
                for (Integer score : playerScore.getScore()) {
                    if (score == null) {
                        roundFinished = false;
                        break roundFinishCheck;
                    }
                }
            }
        }
        this.buttonStartRound.setDisable(!roundFinished);
    }

    @FXML
    private void onButtonSwapPlayersClicked(ActionEvent e) {
        log.info("Swap Players Button clicked");

    }
}