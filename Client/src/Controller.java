/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Controller {
    @FXML
    private Button logout_button;
    @FXML
    private Button quit_button;
    @FXML
    private Button client_login_button;
    @FXML
    private Button newAccount_button;
    @FXML
    private TextField password_field;
    @FXML
    private Button signin_guest_button;
    @FXML
    private TextField username_field;
    @FXML
    private Button create_account_button;
    @FXML
    private TextField password_create_field;
    @FXML
    private TextField username_create_field;

    @FXML
    private TextField bidonitem_field;

    @FXML
    private TextField bidprice_textfield;
    @FXML
    private Button makebid_button;
    @FXML
    private Label welcome_label;
    @FXML
    private Button client_quit_button;

    public static String name;
    public static User auctionUser;
    public static boolean logged_in = false;

    @FXML
    void loginAction(MouseEvent event) {
        String username = username_field.getText();
        String password = password_field.getText();
        User user = new User(username, password); // make user
        System.out.print(username + " " + password + " logging in.");
        Message message = new Message("LOGIN", user);
        Client.sendToServer(message);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (logged_in) { // if true, open up the new pane of the auction site
            try {
                updateStatics(username, user);
                Client.sendToServer(new Message(""));
                Client.switchScenes("auctionPage.fxml"); // change to auction site
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            ControllerAuction.windowErrorSound();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setContentText("Invalid login credentials. Please try again.");
            Optional<ButtonType> result = alert.showAndWait();
        }
    }
    @FXML
    void signInAction(MouseEvent event) {
        String username = "guest"+Integer.toString(User.getId_Count());         // make a guest account, username of the user_id
        String password = "guest"+Integer.toString(User.getId_Count());
        User user = new User(username, password, "makeaccountmethod");
        System.out.print(username + " " + password + "account creation.");
        Message message = new Message("NEWACC", user);
        Client.sendToServer(message);
        try {Thread.sleep(500);}
        catch (InterruptedException e) {throw new RuntimeException(e);}
        if(logged_in){
            try {
                updateStatics(username, user);
                Client.sendToServer(new Message(""));
                Client.switchScenes("auctionPage.fxml"); // change to auction site
            }
            catch (IOException e) {throw new RuntimeException(e);}
        }
        else{
            ControllerAuction.windowErrorSound();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setContentText("Guest Creation not working at the moment. Please try later.");
            Optional<ButtonType> result = alert.showAndWait();
        }
    }
    @FXML
    void createAccount(MouseEvent event) {
        String username = username_create_field.getText();
        String password = password_create_field.getText();
        User user = new User(username, password, "makeaccountmethod");
        System.out.print(username + " " + password + "account creation.");
        Message message = new Message("NEWACC", user);
        Client.sendToServer(message);
        try {Thread.sleep(500);}
        catch (InterruptedException e) {throw new RuntimeException(e);}
        if(logged_in && username.length()>0 && password.length()>0){
            try {
                updateStatics(username, user);
                Client.sendToServer(new Message(""));
                Client.switchScenes("auctionPage.fxml"); // change to auction site
            }
            catch (IOException e) {throw new RuntimeException(e);}
        }
        else{
            ControllerAuction.windowErrorSound();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setContentText("Username is already taken or invalid input. Please try again.");
            Optional<ButtonType> result = alert.showAndWait();
        }
    }
    @FXML
    void registerAction(MouseEvent event) throws IOException {
        Client.switchScenes("accountCreationPage.fxml"); // display a new pane for a registration page
    }
    @FXML
    void logoutAuction(MouseEvent event) {
        Client.logOut();
    }
    @FXML
    void loginScreenAction(MouseEvent event) throws IOException {
        Client.switchScenes("startUpPage.fxml");
    }
    public void updateStatics(String username, User user){
        logged_in = false;
        name = username;
        auctionUser = user;
    }
}
