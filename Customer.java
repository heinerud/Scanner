/**
	Customer.java
	Strecksystem 1.2
	Joel Heinerud - April 2015
	joel.heinerud@gmail.com
**/

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Customer {
	private String name;
	private String mail;
	private String code;
	private double debt;
	private LinkedList<String[]> history;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public Customer(String name, String mail, String code) {
		this.name = name;
		this.mail = mail;
		this.code = code;
		this.debt = 0.0;
		this.history = new LinkedList<String[]>();
	}		
	
	public String getName() {
		return name;
	}
	
	public String getMail() {
		return mail;
	}
	
	public String getCode() {
		return code;
	}
	
	public double getDebt() {
		return debt;
	}

	public LinkedList<String[]> getHistory() {
		return history;
	}
	
	public void addToHistory(HashMap<Product, Integer> productBasket) {
		for(Product p : productBasket.keySet()) {
			int bought = productBasket.get(p);
			for(int i=0; i<bought; i++) {
				history.addLast(new String[]{dateFormat.format(Calendar.getInstance().getTime()), p.getCode()});
				debt += p.getCost();
			}
		}
	}
	
	public void addToHistory(Product p, String timestamp) {
		history.addLast(new String[]{timestamp, p.getCode()});
		debt += p.getCost();
	}
}
