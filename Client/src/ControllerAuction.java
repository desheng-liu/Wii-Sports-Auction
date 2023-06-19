/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import com.sun.deploy.services.PlatformType;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import sun.java2d.pipe.SpanShapeRenderer;

import javax.naming.CompositeName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class ControllerAuction {

    @FXML
    private TextField bidonitem_field;

    @FXML
    private TextField bidprice_textfield;
    @FXML
    private Label itemname_label;
    @FXML
    private Label currentBidPrice_label;
    @FXML
    private Label currentbidpriceTITLE_label;
    @FXML
    private Label sellingbidpriceTITLE_label;
    @FXML
    private Button logout_button;
    @FXML
    private Button makebid_button;
    @FXML
    private Button quit_button;

    @FXML
    private Label welcome_label;
    @FXML
    private Label announcements_label;
    @FXML
    private Label solditemmessage_label;
    @FXML
    private Button auctionHistory_button;
    @FXML
    private TextArea auctionHistory_textArea;
    @FXML
    private Button closeAuctionHistory_button;
    @FXML
    private ComboBox<Item> itemList_combobox;
    @FXML
    private ComboBox<Item> soldlist_combobox;
    @FXML
    private Label sellingbidprice_label;
    @FXML
    private Label highestbiddermessage_label;
    @FXML
    private Label iteminformation_label;
    @FXML
    private Label solditeminformation_label;
    @FXML
    private Label itemnameTITLE_label;
    @FXML
    private Label descriptionTITLE_label;
    @FXML
    private Label description_label;
    @FXML
    private ImageView imageView;

    public static boolean soldout = false;
    public static boolean bidtoolow = false;
    ArrayList<String> pastBidsfromLogin = new ArrayList<>();

    static SimpleStringProperty announcement_gui = new SimpleStringProperty(Client.announcement);
    static SimpleDoubleProperty currentprice_gui = new SimpleDoubleProperty(Client.currentprice);
    static ObservableList<Item> items = FXCollections.observableList(Client.itemList);
    static ObservableList<Item> solditems = FXCollections.observableList(Client.solditemList);
    String makebidsound = "makebidsound.mp3";
    static String windows = "windowserror.mp3";
    Media bidsound = new Media(new File(makebidsound).toURI().toString());
    static Media windowserrorsound = new Media(new File(windows).toURI().toString());
    MediaPlayer bidnoise = new MediaPlayer(bidsound);
    static MediaPlayer errorsound = new MediaPlayer(windowserrorsound);


    static public void windowErrorSound(){
        errorsound.seek(Duration.ZERO);
        errorsound.setVolume(.5);
        errorsound.play();
    }
    @FXML
    public void initialize(){
        welcome_label.setText("Welcome to the Annual International Wii Sports Auction, "+Controller.name+"!");
        announcements_label.setText("");
        currentBidPrice_label.setText("");
        auctionHistory_textArea.setVisible(false);
        closeAuctionHistory_button.setVisible(false);
        itemsoldDisplayOFF();
        itemnotsolddisplayOFF();
        initListners();
    }
    @FXML
    void makeBidOnAction(MouseEvent event) {
        try{
            Double user_bidprice = Double.parseDouble(bidprice_textfield.getText());
            User user = Controller.auctionUser;
            String itemname = bidonitem_field.getText();
            Message message = new Message("BID", itemname, user_bidprice, user);
            Client.sendToServer(message);
            try {Thread.sleep(500);}
            catch (InterruptedException e) {throw new RuntimeException(e);}
            if(soldout){
                windowErrorSound();
                soldout = false;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setContentText("The auction for this item has closed. Sorry!");
                Optional<ButtonType> result = alert.showAndWait();
            } else if (bidtoolow){
                windowErrorSound();
                bidtoolow = false;
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error!");
                alert.setContentText("Bid that you made was too low. Try again.");
                Optional<ButtonType> result = alert.showAndWait();
            }
            else{
                bidnoise.seek(Duration.ZERO);
                bidnoise.setVolume(.4);
                bidnoise.play();
            }
        } catch (NumberFormatException e){
            windowErrorSound();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setContentText("Not a valid bid. Try again.");
            Optional<ButtonType> result = alert.showAndWait();
        }
    }

    @FXML
    void soldlistsetOnAction(ActionEvent event) {
        Item item = soldlist_combobox.getSelectionModel().getSelectedItem();
        double highestCurrentBid = 0.0;
        String highestBidder = "";
        for(Item i: Client.solditemList){ // network based
            if (i.getName().equals(item.getName())) {
                highestCurrentBid = i.getCurrentPrice();
                highestBidder = i.getHighestBidder();
            }
        }
        solditeminformation_label.setVisible(true);
        bidonitem_field.setText("");
        solditemmessage_label.setText("The auction for " + item.getName() + " is closed."); // display that the item cannot be bid on anymore!
        highestbiddermessage_label.setText(highestBidder + " has won the auction for " + item.getName() + " with a bid price of " + highestCurrentBid + "!"); // display the highest bidder's username and the final selling price.
        itemsoldDisplayON();
    }

    @FXML
    void comboSetOnAction(ActionEvent event) { // need to update combobox each time something happens
        Item item = itemList_combobox.getSelectionModel().getSelectedItem();
        double highestCurrentBid = 0.0;
        String highestBidder = "";
        for (Item i : Client.itemList) {  // network based
            if (i.getName().equals(item.getName())) {
                highestCurrentBid = i.getCurrentPrice();
            }
        }
        try {
            imageView.setImage(item.getImagefile());
            bidonitem_field.setText(item.getName());
            sellingbidprice_label.setText(String.valueOf(item.getFinalPrice()));
            currentBidPrice_label.setText(String.valueOf(highestCurrentBid));
            itemname_label.setText(item.getName());
            description_label.setText(item.getDescription());
            itemNOTsolddisplayON();
        }
        catch(Exception e){
            bidonitem_field.setText(item.getName());
            sellingbidprice_label.setText(String.valueOf(item.getFinalPrice()));
            currentBidPrice_label.setText(String.valueOf(highestCurrentBid));
            itemname_label.setText(item.getName());
            description_label.setText(item.getDescription());
            itemNOTsolddisplayON();
            imageView.setVisible(false);
        };
    }
    public void initListners(){
        announcement_gui.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        announcements_label.setText(Client.announcement);
                        pastBidsfromLogin.add(Client.announcement); // history array
                        auctionHistory_textArea.clear(); // reset because we dont wanna print two copies of the arraylist
                        for(String s : pastBidsfromLogin){
                            auctionHistory_textArea.appendText(s+"\n");
                        }
                    }
                });
            }
        });
        currentprice_gui.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        currentBidPrice_label.setText(String.valueOf(Client.currentprice));
                    }
                });
            }
        });

        items.addListener(new ListChangeListener<Item>() {
            @Override
            public void onChanged(Change<? extends Item> change) {
                itemList_combobox.setItems(items); // update the items whenever a change is detected
            }
        });

        solditems.addListener(new ListChangeListener<Item>() {
            @Override
            public void onChanged(Change<? extends Item> change) {
                soldlist_combobox.setItems(solditems); // update the items whenever a change is detected
            }
        });

    }
    @FXML
    void seeHistoryOnClicked(MouseEvent event) { // needs to update right when a bid is made?
        auctionHistory_textArea.setVisible(true);
        closeAuctionHistory_button.setVisible(true);
    }
    @FXML
    void closeHistoryOnClicked(MouseEvent event) {
        auctionHistory_textArea.setVisible(false);
        closeAuctionHistory_button.setVisible(false);
    }
    void itemnotsolddisplayOFF(){
        sellingbidpriceTITLE_label.setVisible(false);
        currentbidpriceTITLE_label.setVisible(false);
        sellingbidprice_label.setVisible(false);
        currentBidPrice_label.setVisible(false);
        descriptionTITLE_label.setVisible(false);
        description_label.setVisible(false);
        itemname_label.setVisible(false);
        itemnameTITLE_label.setVisible(false);
        iteminformation_label.setVisible(false);
        imageView.setVisible(false);
    }
    void itemNOTsolddisplayON(){
        imageView.setVisible(true);
        sellingbidpriceTITLE_label.setVisible(true);
        currentbidpriceTITLE_label.setVisible(true);
        sellingbidprice_label.setVisible(true);
        currentBidPrice_label.setVisible(true);
        itemnameTITLE_label.setVisible(true);
        iteminformation_label.setVisible(true);
        itemname_label.setVisible(true);
        description_label.setVisible(true);
        descriptionTITLE_label.setVisible(true);

    }
    void itemsoldDisplayON(){
        solditemmessage_label.setVisible(true);
        highestbiddermessage_label.setVisible(true);
    }
    void itemsoldDisplayOFF(){
        solditeminformation_label.setVisible(false);
        solditemmessage_label.setVisible(false);
        highestbiddermessage_label.setVisible(false);
    }

    @FXML
    void logOutAction(MouseEvent event) throws IOException {
        Client.switchScenes("startUpPage.fxml");
    }
    @FXML
    void quitAction(MouseEvent event) {
        Client.logOut();
    }

}
