/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import javafx.scene.image.Image;
import org.bson.types.ObjectId;

public class Item {
	private ObjectId id;
	private String name, description, highestBidder, imagefile;
	private double finalPrice;
	private double currentPrice;
	public Item(String name, String description, String highestBidder, double finalPrice, double currentPrice, String imagefile) {
		this.name = name;
		this.description = description;
		this.highestBidder = highestBidder;
		this.finalPrice = finalPrice;
		this.currentPrice = currentPrice;
		this.imagefile = imagefile;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHighestBidder() {
		return highestBidder;
	}
	public double getFinalPrice() {
		return finalPrice;
	}

	public double getCurrentPrice(){
		return currentPrice;
	}

	public Image getImagefile(){
		return new Image(imagefile);
	}

	public void setFinalPrice(double finalPrice) {
		this.finalPrice = finalPrice;
	}

	@Override
	public String toString(){
		return name;
	}

	public String printDebug() {
		return "Item [name=" + name + ", description=" + description + ", winner=" + highestBidder + ", finalPrice="
				+ finalPrice + "]";
	}
}
