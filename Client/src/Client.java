/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.net.Socket;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;


public class Client extends Application {
  private static String host = "10.165.69.126";
  private static String awsHostNonPresenting = "18.188.111.58";
  private static String awsHostPresent = "18.116.203.152";
  private static String utHost = "10.146.208.128";
  private BufferedReader fromServer;
  private static PrintWriter toServer;
  private Scanner consoleInput = new Scanner(System.in);
  static Thread readerThread;
  static Thread writerThread;
  static Gson gson = new Gson();
  public static ArrayList<Item> itemList;
  public static ArrayList<Item> solditemList;
  private static Stage stage;
  public static String announcement = "";
  public static ArrayList<String> historyBids = new ArrayList<>();
  public static double currentprice;
  public static void main(String[] args) {
      launch(args);
  }
  String wiisportsmusic = "wiisportsmusic.mp3";
  Media sound = new Media(new File(wiisportsmusic).toURI().toString());
  MediaPlayer mediaPlayer = new MediaPlayer(sound);

  @Override
  public void start(Stage primaryStage) throws IOException {
    try {
      new Client().setUpNetworking();
      stage = primaryStage;
      mediaPlayer.setVolume(.6);
      mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      mediaPlayer.play();
      FXMLLoader loader = new FXMLLoader();
      URL xmlUrl = getClass().getResource("/startUpPage.fxml");
      loader.setLocation(xmlUrl);
      Parent root = loader.load();
      primaryStage.setTitle("Client Login");
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println("There's an error.");
    }
  }
  public static void switchScenes(String fxml) throws IOException {
    Parent pane = FXMLLoader.load(Client.class.getResource(fxml));
    stage.setScene(new Scene(pane));
  }
  public static void logOut(){
    Message message = new Message("END");
    sendToServer(message);
    readerThread.stop(); // issue with making the static possibly?
    System.exit(0);
  }
  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    Socket socket = new Socket(awsHostPresent, 1234);
    System.out.println("Connecting to... " + socket);
    fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    toServer = new PrintWriter(socket.getOutputStream());
    readerThread = new Thread(new Runnable() {
      @Override
      public void run() {
        String recievedMessage; // recieving a json string, need to covert to gson
        try {
          while ((recievedMessage = fromServer.readLine()) != null) {
            Message message = gson.fromJson(recievedMessage, Message.class);
            System.out.println("From server: " + recievedMessage);
            processRequest(message);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    readerThread.start();
  }
  protected void processRequest(Message incomingMessage) { // checks the HTTP-like response, gives response to frontend.
    switch (incomingMessage.getHTTPlike()){
      case "UPDATE":
        System.out.println("itemList recieved");
        solditemList = incomingMessage.getSoldItemList();
        itemList = incomingMessage.getItemList();
        historyBids = incomingMessage.getHistoryBids();
        currentprice = incomingMessage.getCurrBidPrice();
        announcement = incomingMessage.getAnnouncement();
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            if(incomingMessage.getItemList().size() > 0){ControllerAuction.items.setAll(incomingMessage.getItemList());}
            ControllerAuction.solditems.setAll(incomingMessage.getSoldItemList());
            ControllerAuction.currentprice_gui.set(incomingMessage.getCurrBidPrice());
            ControllerAuction.announcement_gui.set(incomingMessage.getAnnouncement());
          }
        });
        break;
      case "USERSUCCESS":
        Controller.logged_in = true;
        break;
      case "USERFAIL":
        System.out.println("Account login/registration failed. Please try again.");
        break;
      case "BIDLOW":
        ControllerAuction.bidtoolow = true;
        break;
      case "ITEMSOLD":
        ControllerAuction.soldout = true;
    }
  }
  static void sendToServer(Message message) { // server expects a json format in String type
    System.out.println("Sending to server: " + message.toString());
    toServer.println(gson.toJson(message));
    toServer.flush();
  }

}