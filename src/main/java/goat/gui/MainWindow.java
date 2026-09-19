package goat.gui;

import goat.Goat;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Drives the main window: reads what is typed, asks Goat, shows both.
 *
 * The controller holds no chatbot logic of its own. Every line the user types
 * goes straight to {@link Goat#getResponse(String)}, and whatever comes back is
 * put into a bubble. That is the whole of the connection between the GUI and
 * the rest of the program.
 */
public class MainWindow extends AnchorPane {

    /** How long Goat's farewell stays on screen before the window closes. */
    private static final Duration FAREWELL_PAUSE = Duration.seconds(1.5);

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    private Goat goat;

    private final Image userImage = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private final Image goatImage = new Image(this.getClass().getResourceAsStream("/images/DaGoat.png"));

    /** Keeps the newest message in view as the conversation grows. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Hands this window the chatbot it should talk to, and shows its greeting.
     *
     * The greeting is shown here rather than in {@link #initialize()} because
     * there is no chatbot to greet with until this is called.
     *
     * @param goat the chatbot driving the conversation
     */
    public void setGoat(Goat goat) {
        this.goat = goat;
        dialogContainer.getChildren().add(
                DialogBox.getGoatDialog(goat.getWelcomeMessage(), goatImage));
    }

    /**
     * Sends whatever is in the text field to Goat and shows both sides of the
     * exchange.
     *
     * Wired to both the Send button and the Enter key by MainWindow.fxml.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText().trim();
        if (input.isEmpty()) {
            // Enter on an empty field should do nothing, rather than making
            // Goat complain about a command it was never given.
            return;
        }
        String response = goat.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getGoatDialog(response, goatImage));
        userInput.clear();

        if (goat.isExitRequested()) {
            closeAfterFarewell();
        }
    }

    /**
     * Closes the window, but not before the farewell has been read.
     *
     * Exiting the instant "bye" is typed would hide the reply Goat just gave,
     * so the window waits first. Input is disabled during the pause, so nothing
     * can be typed into a chatbot that is on its way out.
     */
    private void closeAfterFarewell() {
        userInput.setDisable(true);
        sendButton.setDisable(true);
        PauseTransition pause = new PauseTransition(FAREWELL_PAUSE);
        pause.setOnFinished(event -> Platform.exit());
        pause.play();
    }
}
