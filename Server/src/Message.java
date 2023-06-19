/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import java.util.ArrayList;

class Message {
  private String HTTPlike;
  private Item item;
  private String itemname;
  private ArrayList<Item> itemList;
  private ArrayList<Item> soldList;
  private ArrayList<String> historyBids;
  private Double bidPrice;
  private User user;
  private String announcement;
  private double currBidPrice;

  public Message(String HTTPlike, Item item){ // adding and removing item from database constuctor send to server
    this.HTTPlike = HTTPlike;
    this.item = item;
  }

  public Message(String HTTPlike){ // basic template message
    this.HTTPlike = HTTPlike;
  } // base template

  public Message(String HTTPlike, String announcment){ // anouncement to make to client
    this.HTTPlike = HTTPlike;
    this.announcement = announcment;
  }
  public Message(String HTTPlike, ArrayList<Item> itemList, ArrayList<Item> soldList, ArrayList<String> historyBids, String announcement, double currBidPrice){ // updated Itemlist constructor "UPDATE" send to client
    this.HTTPlike = HTTPlike;
    this.itemList = itemList;
    this.soldList = soldList;
    this.historyBids = historyBids;
    this.announcement = announcement;
    this.currBidPrice = currBidPrice;
  }

  public Message(String HTTPlike, String itemname, Double bidPrice, User user){ // bid message for "BID" send to server
    this.HTTPlike = HTTPlike;
    this.itemname = itemname;
    this.bidPrice = bidPrice;
    this.user = user;
  }

  public Message(String HTTPlike, User user){ // login and registration message sent to Server
    this.HTTPlike = HTTPlike;
    this.user = user;
  }

  public String getHTTPlike(){
    return HTTPlike;
  }

  public Item getItem(){
    return item;
  }

  public ArrayList<Item> getItemList(){return itemList;}
  public Double getBidPrice(){
    return bidPrice;
  }
  public User getUser() {
    return this.user;
  }
  public String getItemname(){
    return this.itemname;
  }
  public String getAnnouncement(){
    return this.announcement;
  }
  public ArrayList<String> getHistoryBids(){
    return this.historyBids;
  }
  public ArrayList<Item> getSoldItemList(){
    return this.soldList;
  }
  public double getCurrBidPrice(){
    return currBidPrice;
  }
}