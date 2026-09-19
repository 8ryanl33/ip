package goat.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

/**
 * One speech bubble: a picture of the speaker beside what they said.
 *
 * The user's bubble and Goat's are the same control with their contents
 * reversed, which is why {@link #flip()} exists rather than a second class: the
 * two differ only in which side the picture sits on and which way the text is
 * aligned.
 *
 * The constructor is private and the two factory methods are named for the
 * speaker, so a caller cannot build a bubble without saying whose it is.
 */
public class DialogBox extends HBox {

    @FXML
    private Label dialog;

    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image image) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("Could not load a dialog box: " + e.getMessage());
        }
        dialog.setText(text);
        displayPicture.setImage(image);
        // A circular clip turns a square picture into a round avatar without
        // needing the image file itself to have transparent corners.
        displayPicture.setClip(new Circle(20.0, 20.0, 20.0));
    }

    /**
     * Returns a bubble for something the user said, with the picture on the right.
     *
     * @param text  what the user typed
     * @param image the user's picture
     * @return the bubble, ready to be added to the conversation
     */
    public static DialogBox getUserDialog(String text, Image image) {
        return new DialogBox(text, image);
    }

    /**
     * Returns a bubble for something Goat said, with the picture on the left.
     *
     * @param text  Goat's reply
     * @param image Goat's picture
     * @return the bubble, ready to be added to the conversation
     */
    public static DialogBox getGoatDialog(String text, Image image) {
        DialogBox box = new DialogBox(text, image);
        box.flip();
        return box;
    }

    /** Puts the picture on the left and the text against it, mirroring the bubble. */
    private void flip() {
        ObservableList<Node> children = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(children);
        getChildren().setAll(children);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }
}
