/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import com.google.gson.Gson;
import org.bson.Document;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.Double.parseDouble;

class Server extends Observable {
  mongodb database;
  Gson gson = new Gson();
  String announcement = "";
  ArrayList<String> historyBids = new ArrayList<String>();
  double currentbidPrice = 0.0;
  public static void main(String[] args) {
    new Server().runServer();
  }

  private void runServer() {
    try {
      database = new mongodb();
      readItemList("input.txt");
      setUpNetworking();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  private void testing() throws IOException {
    Item item = new Item("Basketball", "a ball that you shoot", "Desheng Liu", 100.00, 20.00, "");
    Item item2 = new Item("Soccerball", "ball you kick", "plop", 200, 4, "");
    Socket socket = new Socket();
    Message message = new Message("ADDITEM", item);
    Message message2 = new Message("ADDITEM", item2);
    String input = new Gson().toJson(message);
    String input2 = new Gson().toJson(message2);
    processRequest(input, socket); // 1 adding
    processRequest(input2, socket); // testing adding

    message = new Message("REMOVEITEM", item);
    input = new Gson().toJson(message);
    processRequest(input, socket); // testing remove
  }
  private void readItemList(String FileName) throws FileNotFoundException {
    try {
      File file = new File(FileName);
      Scanner scanner = new Scanner(file);
      String addIntoDB = "ADDITEM";

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] fileArr = line.split(" : ");
        for (String s : fileArr) {
          s = s.trim();
        }
        String name = fileArr[0];
        String description = fileArr[1];
        String winner = "";
        double finalPrice = parseDouble(fileArr[3]);
        double startingPrice = parseDouble(fileArr[4]);
        String imagefile = fileArr[5];

        Item item = new Item(name, description, winner, finalPrice, startingPrice, imagefile);
        if(database.getItem(name) == null){ // prevent duplicates in here
          database.addItem(item);
          System.out.println(item.getName()+" was added into the database.");
        }
      }
      scanner.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error while trying to read input file.");
    }
  }
  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    ServerSocket serverSock = new ServerSocket(1234);
    while (true) {
      Socket clientSocket = serverSock.accept();
      System.out.println("Connecting to... " + clientSocket);

      ClientHandler handler = new ClientHandler(this, clientSocket);
      this.addObserver(handler);

      Thread t = new Thread(handler);
      t.start();
      System.out.println("Server -- got a connection to a client.");
    }
  }

  public void sendMessageToClient(PrintWriter sender, Message message){
    sender.println(gson.toJson(message));
    sender.flush();
  }
  protected synchronized void processRequest(String input, Socket client) throws IOException {
    PrintWriter messageSender = new PrintWriter(client.getOutputStream());
    Message message = gson.fromJson(input, Message.class);
    DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    try {
      switch (message.getHTTPlike()) {
        case "ADDITEM":
          System.out.println("we added an item!");
          database.addItem(message.getItem());
          break;
        case "REMOVEITEM": //remove an item from database
          System.out.println("removed an item");
          database.removeItem(message.getItem());
           break;
        case "NEWACC":
          System.out.println(client.toString()+" made a request to make a new account.");
          User newUser = message.getUser();
          if(database.getUser(newUser) == null){ // check if the username already exists in the database.
            Message userAdd_success = new Message("USERSUCCESS", newUser);
            database.addUser(newUser); // store into account collection database
            sendMessageToClient(messageSender, userAdd_success); // registration good message to client
            database.addToNonVolHistory(new Message(newUser.getUsername()+" has made an account at "+LocalDateTime.now().format(format)));
          }
          else{
            Message userAdd_unsuccess = new Message("USERFAIL", newUser);
            sendMessageToClient(messageSender, userAdd_unsuccess); // registration not good message to client
          }
          break;
        case "LOGIN":
          System.out.println(client.toString() + " made a request to log in.");
          User loginUser = message.getUser();
          User databaseUser = database.getUser(loginUser);
          if(databaseUser == null){
            Message logIn_failed = new Message("USERFAIL", loginUser);
            sendMessageToClient(messageSender, logIn_failed); // login failed message to client
          }
          else {
            if (databaseUser.checkPassword(loginUser.getPassword())) {
              Message logIn_success = new Message("USERSUCCESS", loginUser);
              sendMessageToClient(messageSender, logIn_success);
              database.addToNonVolHistory(new Message(loginUser.getUsername()+"has logged into their account at "+LocalDateTime.now().format(format)));
            }
            else{
              Message logIn_failed = new Message("USERFAIL", loginUser);
              sendMessageToClient(messageSender, logIn_failed);
            }
          }
          break;
        case "BID": // this request assumes that the bidItem is a valid item (could be sold or not though)
          System.out.print("someone made a bid request.");
          Double client_BidPrice = message.getBidPrice();
          String bidUsername = message.getUser().getUsername();
          Item bidItem = database.getItem(message.getItemname());
          if(bidItem == null){
            Message messageItemSold = new Message("ITEMSOLD");
            sendMessageToClient(messageSender, messageItemSold);
            break;
          }
          if(client_BidPrice > bidItem.getCurrentPrice()){ // checks for valid bid
            database.updateItem(bidItem.getName(), message.getUser().getUsername(), client_BidPrice); //  if so, update the bid price and change the highest bidders name
            if(client_BidPrice >= bidItem.getFinalPrice()){ // if client bid is higher or equal to set price
              database.transferSoldDB(bidItem); // auction item is sold
              announcement = bidItem.getName()+" was sold to "+message.getUser().getUsername()+" at a bid offer of $"+client_BidPrice+"\n";
              database.addToNonVolHistory(new Message(bidItem.getName()+" was sold to "+message.getUser().getUsername()+" at a bid offer of $"+client_BidPrice
              +" at "+LocalDateTime.now().format(format)));
            } else{
              announcement = message.getUser().getUsername()+" has made a bid for the "+bidItem.getName()+" at a offer of $"+client_BidPrice+"\n";
              database.addToNonVolHistory(new Message(message.getUser().getUsername()+" has made a bid for the "+bidItem.getName()+" at a offer of $"+client_BidPrice
              +" at "+LocalDateTime.now().format(format)));
            }
            historyBids.add(announcement);
            currentbidPrice = client_BidPrice;
          } else{
            Message messageBidLow = new Message("BIDLOW");
            sendMessageToClient(messageSender, messageBidLow);
          }
          break;
        case "END":
          System.out.println("Connection to "+ client.toString() + "ended.");
          client.close();
          break;
        case "LOG":
          database.addToNonVolHistory(message);
        case "UPDATELIST":
          System.out.println("HI!");
          break;
      }
      this.setChanged();

      // each HTTP-like message we make sure to update the list of items/sold items
      // so the display is updated each time for any type of update
      ArrayList<Item> itemList = new ArrayList<Item>();
      for(Document d : mongodb.getItem_collection().find()){
        Item item = gson.fromJson(d.toJson(), Item.class);
        itemList.add(item);
      }

      ArrayList<Item> soldList = new ArrayList<Item>();
      for(Document d : mongodb.getSold_collection().find()){
        Item item = gson.fromJson(d.toJson(), Item.class);
        soldList.add(item);
      }
      Message sentToClients = new Message("UPDATE", itemList, soldList, historyBids, announcement, currentbidPrice);
      this.notifyObservers(gson.toJson(sentToClients));
      announcement = "";
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}