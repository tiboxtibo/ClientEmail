package app;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.regex.Pattern;


/** Login Controller */
public class LoginController {

    @FXML
    private TextField userTextField;
    @FXML
    private PasswordField pwdTextField;
    @FXML
    private Label errLabel;




    /** Cambia la visualizzazione con la mailView */
    @FXML
    public static void switchToMailView() throws IOException {
        ClientGUI.setRoot("mailview");
    }

    /**
     * Even handler del bottone del login
     * Controlla le stringhe vuote e la correttezza dell'indirizzo mail
     * Se il login è valido cambia scena alla mailView
     * */
    public void loginClick(ActionEvent actionEvent) throws IOException{
        if(userTextField.getText() == null || userTextField.getText().trim().isEmpty() || pwdTextField.getText() == null || pwdTextField.getText().trim().isEmpty() ) {
            errLabel.setText("I campi non devono essere vuoti");
            errLabel.setVisible(true);
        }else if(emailCheck())//la mail ha un formato corretto
            ClientMethods.login(userTextField.getText(), pwdTextField.getText(), errLabel);
    }


    /**
     * Il pattern dei caratteri accetta ogni combinazione c di lettere e numeri
     * es: c@c.c  ->  matteo@gmail.com
     * */
    public static Pattern validateEmail = Pattern.compile("[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$", Pattern.CASE_INSENSITIVE);

    /** Controlla che la email rispetti il seguente pattern, altrimenti ritorna un errore */
    public boolean emailCheck() {
        String email = userTextField.getText();
        if (!email.matches(String.valueOf(validateEmail))) {
            errLabel.setText("Indirizzo email NON valido");
            errLabel.setVisible(true);
            return false;
        }
        errLabel.setVisible(false);
        errLabel.setText("Utente NON trovato");
        return true;
    }
}
