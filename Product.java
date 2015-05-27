/**
	Product.java
	Strecksystem 1.2
	Joel Heinerud - April 2015
	joel.heinerud@gmail.com
**/

public class Product {
	private String name;
	private String code;
	private double cost;
	
	public Product(String name, String code, double cost){
		this.name = name;
		this.code = code;
		this.cost = cost;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	public double getCost() {
		return cost;
	}
}
