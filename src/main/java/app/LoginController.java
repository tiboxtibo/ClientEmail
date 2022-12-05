package app;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * The type Login controller.
 */
public class LoginController {



    @FXML
    private TextField userTextField;
    @FXML
    private PasswordField pwdTextField;
    @FXML
    private Label errLabel;



    /**
     * Switch to mail view.
     *
     * @throws IOException the io exception
     */
    @FXML
    public static void switchToMailView() throws IOException {
        ClientGUI.setRoot("mailview");
    }



    /**
     * Event handler for the login button.
     * Checks for null or empty strings.
     * If login is valid, scene switches to mailview
     *
     * @param actionEvent the action event
     * @throws IOException the io exception
     */
    public void loginClick(ActionEvent actionEvent) throws IOException{
        if(userTextField.getText() == null || userTextField.getText().trim().isEmpty() || pwdTextField.getText() == null || pwdTextField.getText().trim().isEmpty() ) {
            errLabel.setText("Fields must not be empty");
            errLabel.setVisible(true);
        }else if(emailCheck())
            ClientMethods.login(userTextField.getText(), pwdTextField.getText(), errLabel);
    }

    /**
     * The pattern of characters for the mail check.
     * Accepts any combination of a-Z characters and 0-9 numbers.
     * Must be of type combination@combination.combination
     * Example can be lupoandrea98@gmail.com
     */
    public static Pattern validateEmail = Pattern.compile("[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$", Pattern.CASE_INSENSITIVE);

    /**
     * Check if mail is valid using the pattern above.
     * If the mail does not match the pattern, we show an error on the UI with errLabel.
     *
     * @return if mail exist or not
     */
    public boolean emailCheck() {
        String email = userTextField.getText();
        if (!email.matches(String.valueOf(validateEmail))) {
            errLabel.setText("Invalid email address");
            errLabel.setVisible(true);
            return false;
        }
        errLabel.setVisible(false);
        errLabel.setText("User not found.");
        return true;
    }
}
