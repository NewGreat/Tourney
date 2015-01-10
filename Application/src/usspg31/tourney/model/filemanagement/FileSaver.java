package usspg31.tourney.model.filemanagement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.collections.ObservableList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import usspg31.tourney.model.Event;
import usspg31.tourney.model.Player;
import usspg31.tourney.model.Tournament;
import usspg31.tourney.model.TournamentModule;

/**
 * Contains static methods that save rule modules and events including
 * tournaments and players using XML files
 * 
 * @author Jan Tagscherer
 */
public class FileSaver {
	private static final Logger log = Logger.getLogger(FileSaver.class
			.getName());

	private static DocumentBuilderFactory documentFactory;
	private static DocumentBuilder documentBuilder;
	private static Transformer transformer;
	private static boolean initialized = false;

	/**
	 * Initialize the file manager. Has to be called before all other methods.
	 */
	public static void initialize() {
		FileSaver.documentFactory = DocumentBuilderFactory.newInstance();
		try {
			FileSaver.documentBuilder = FileSaver.documentFactory
					.newDocumentBuilder();

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			FileSaver.transformer = transformerFactory.newTransformer();
			FileSaver.transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			FileSaver.transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");

			FileSaver.initialized = true;
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE,
					"Error while creating the document builder in file management initialization.");
			log.log(Level.SEVERE, e.getMessage(), e);
		} catch (TransformerConfigurationException e) {
			log.log(Level.SEVERE,
					"Error while creating the transformer in file management initialization.");
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Save an event including tournaments, players and the utilized tournament
	 * modules as a .tef file
	 * 
	 * @param event
	 *            Event that should be saved
	 * @param path
	 *            Path where the event should be saved
	 */
	public static void saveEventToFile(Event event, String path) {
		if (!FileSaver.initialized) {
			FileSaver.initialize();
		}

		File zipFile = new File(path);
		try {
			if (zipFile.getParentFile() != null) {
				zipFile.getParentFile().mkdirs();
			}
			zipFile.createNewFile();
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}

		FileOutputStream fileOutputStream;
		ZipOutputStream zipOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(path);
			zipOutputStream = new ZipOutputStream(fileOutputStream);
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}

		FileSaver.saveEvent(event, "Event.xml", zipOutputStream);
		FileSaver.savePlayersToFile(event.getRegisteredPlayers(),
				"Players.xml", zipOutputStream);
		for (Tournament tournament : event.getTournaments()) {
			FileSaver.saveTournament(tournament,
					"Tournament_" + tournament.getId() + ".xml",
					zipOutputStream);
		}

		try {
			zipOutputStream.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Save a tournament rule template to an individual file
	 * 
	 * @param module
	 *            Tournament module to be saved
	 * @param path
	 *            Path where the tournament module should be saved
	 */
	public static void saveTournamentModuleToFile(TournamentModule module,
			String path) {
		if (!FileSaver.initialized) {
			FileSaver.initialize();
		}

		if (!path.endsWith(".ttm")) {
			path += ".ttm";
		}

		FileSaver.saveTournamentModule(module, path);
	}

	/**
	 * Save an event to a file
	 * 
	 * @param event
	 *            Event to be saved
	 * @param path
	 *            Path were the event should be saved
	 */
	private static void saveEvent(Event event, String fileName,
			ZipOutputStream zipOutputStream) {

		EventDocument document = new EventDocument(
				FileSaver.documentBuilder.newDocument());

		document.appendMetaData(event);
		document.appendTournamentList(event.getTournaments());

		FileSaver.saveDocumentToZip(document.getDocument(), fileName,
				zipOutputStream);
	}

	/**
	 * Save a tournament to a file
	 * 
	 * @param tournament
	 *            Tournament to be saved
	 * @param path
	 *            Path where the tournament should be saved
	 */
	private static void saveTournament(Tournament tournament, String fileName,
			ZipOutputStream zipOutputStream) {

		TournamentDocument document = new TournamentDocument(
				FileSaver.documentBuilder.newDocument());

		document.appendMetaData(tournament);
		document.appendAdministratorList(tournament.getAdministrators());

		document.appendPlayerList(tournament.getRegisteredPlayers(),
				TournamentDocument.REGISTERED_PLAYERS);
		document.appendPlayerList(tournament.getAttendingPlayers(),
				TournamentDocument.ATTENDANT_PLAYERS);
		document.appendPlayerList(tournament.getRemainingPlayers(),
				TournamentDocument.REMAINING_PLAYERS);

		document.appendTournamentRounds(tournament.getRounds());

		if (tournament.getRuleSet() != null) {
			document.appendTournamentRules(tournament.getRuleSet());
		}

		FileSaver.saveDocumentToZip(document.getDocument(), fileName,
				zipOutputStream);
	}

	/**
	 * Save a list of players to a file
	 * 
	 * @param players
	 *            The list of players to be saved
	 * @param path
	 *            The path where the players should be saved
	 */
	private static void savePlayersToFile(ObservableList<Player> players,
			String fileName, ZipOutputStream zipOutputStream) {

		PlayerDocument document = new PlayerDocument(
				FileSaver.documentBuilder.newDocument());

		document.appendPlayerList(players);

		FileSaver.saveDocumentToZip(document.getDocument(), fileName,
				zipOutputStream);
	}

	/**
	 * Save a tournament module to a file
	 * 
	 * @param module
	 *            Tournament module to be saved
	 * @param path
	 *            Path where the tournament module should be saved
	 */
	private static void saveTournamentModule(TournamentModule module,
			String path) {

		TournamentModuleDocument document = new TournamentModuleDocument(
				FileSaver.documentBuilder.newDocument());

		document.appendMetaData(module);
		document.appendPossibleScores(module.getPossibleScores());
		document.appendTournamentPhases(module.getPhaseList());

		FileSaver.saveDocumentToFile(document.getDocument(), path);
	}

	/**
	 * Save an existing XML document to a file
	 * 
	 * @param document
	 *            The DOM document to be saved
	 * @param path
	 *            The path where the document will be saved
	 */
	private static void saveDocumentToFile(Document document, String path) {
		try {
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(path));

			FileSaver.transformer.transform(source, result);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not save the XML file to \"" + path
					+ "\".");
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Save an existing XML document directly to a zip file
	 * 
	 * @param document
	 *            The DOM document to be saved
	 * @param fileName
	 *            The file name of this document in the zip file
	 * @param zipOutputStream
	 *            The zip output stream the document should be saved to
	 */
	private static void saveDocumentToZip(Document document, String fileName,
			ZipOutputStream zipOutputStream) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DOMSource source = new DOMSource(document);
			StreamResult outputTarget = new StreamResult(outputStream);
			FileSaver.transformer.transform(source, outputTarget);
			InputStream inputStream = new ByteArrayInputStream(
					outputStream.toByteArray());

			ZipEntry zipEntry = new ZipEntry(fileName);
			zipOutputStream.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = inputStream.read(bytes)) >= 0) {
				zipOutputStream.write(bytes, 0, length);
			}

			zipOutputStream.closeEntry();
			inputStream.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not output the XML file.");
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}