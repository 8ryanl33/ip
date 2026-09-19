package goat.gui;

import java.io.IOException;

import goat.Goat;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The JavaFX application: builds the window and hands it a chatbot to talk to.
 *
 * Nothing about the chatbot lives here. This class loads the layout, creates
 * one {@link Goat}, gives it to the controller, and shows the window.
 */
public class Main extends Application {

    /** Where the tasks are kept, the same file the terminal version uses. */
    private static final String SAVE_FILE_PATH = "data/goat.txt";

    private final Goat goat = Goat.forGui(SAVE_FILE_PATH);

    /**
     * {@inheritDoc}
     *
     * <p>Loads the window from FXML, hands the chatbot to its controller, and
     * shows it.
     *
     * @param stage the window JavaFX has created for us
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Goat");
            stage.setMinHeight(400.0);
            stage.setMinWidth(450.0);
            fxmlLoader.<MainWindow>getController().setGoat(goat);
            stage.show();
        } catch (IOException e) {
            // Only reachable if the FXML is missing from the JAR, which is a
            // packaging fault rather than something a user can act on.
            System.err.println("Could not load the window layout: " + e.getMessage());
        }
    }
}
