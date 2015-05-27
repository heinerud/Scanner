/**
	Streck.java
	Strecksystem 1.2
	Joel Heinerud - April 2015
	joel.heinerud@gmail.com
**/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.CodeSource;
import java.util.Timer;

public class Streck {
	private String password = "asd";
	private String lastCode;
	private HashMap<String, Product> products;
	private HashMap<String, Customer> customers;
	private HashMap<Product, Integer> productBasket;
	private LinkedList<String[]> totalHistory;
	private File productFile;
	private File customerFile;
	private File historyFile;
	private File summaryFile;
	private DecimalFormat df = new DecimalFormat("#.##");
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private Scanner userInput = new Scanner(System.in);
	private Timer timer = new Timer();
	
	public Streck() {
		init();
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("===========================");
		System.out.println("=    Strecksystem 1.2     =");
		System.out.println("=   Joel Heinerud 2015    =");
		System.out.println("===========================");
		System.out.println("");
		for (int i = 0; i < 9; ++i) System.out.println();
		String input = query("Scan...");
		while(true) {
			for (int i = 0; i < 50; ++i) System.out.println();
			if(input.equals("exit")) {
				break;
			}
			if(isValidInput(input)) {
				scan(input);	
			}
			System.out.println("");
			input = query("Scan...");
		}
		userInput.close();
	}
	
	public void init() {    		
		String rootLocation = "";
		try {
			CodeSource codeSource = Streck.class.getProtectionDomain().getCodeSource();
			File jarFile = new File(codeSource.getLocation().toURI().getPath());
			rootLocation = jarFile.getParentFile().getPath();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not find root");
		}
		productFile = new File(rootLocation + "/products.txt");
		customerFile = new File(rootLocation + "/customers.txt");
		historyFile = new File(rootLocation + "/history.txt");
		summaryFile = new File(rootLocation + "/summary.txt");
		
		products = new HashMap<String, Product>();
		customers = new HashMap<String, Customer>();
		totalHistory = new LinkedList<String[]>();
		productBasket = new HashMap<Product, Integer>();

		initProducts();
		initCustomers();
		initHistory();
		saveProducts();
		saveCustomers();
		saveHistory();
		saveSummary();
	}
	
	public void createFile(File f) {
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void initProducts() {
		createFile(productFile);
		try(Scanner sc = new Scanner(new FileInputStream(productFile))){
			Pattern productPattern = Pattern.compile("<name>(.*)<code>(.*)<cost>(.*)");
			while(sc.hasNextLine()) {
				String nextLine = sc.nextLine();
				Matcher pMatcher = productPattern.matcher(nextLine);
				if(pMatcher.matches()) {
					String name = pMatcher.group(1);
					String code = pMatcher.group(2);
					double cost = Double.parseDouble(pMatcher.group(3));
					Product p = new Product(name, code, cost);
					products.put(code, p);
				}				
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Could not initialize products");
			return;
		}		
	}
	
	public void initCustomers() {
		createFile(customerFile);
		try(Scanner sc = new Scanner(new FileInputStream(customerFile))){
			Pattern customerPattern = Pattern.compile("<name>(.*)<email>(.*)<code>(.*)");
			Pattern historyPattern = Pattern.compile(".*<time>(.*)<product>(.*)");
			Customer currentCustomer = null;
			while(sc.hasNextLine()) {
				String nextLine = sc.nextLine();
				Matcher cMatcher = customerPattern.matcher(nextLine);
				Matcher hMatcher = historyPattern.matcher(nextLine);
				if(cMatcher.matches()) {
					String name = cMatcher.group(1);
					String email = cMatcher.group(2);
					String code = cMatcher.group(3);
					currentCustomer = new Customer(name, email, code);
					customers.put(code, currentCustomer);
				} else if (hMatcher.matches()) {
					String timestamp = hMatcher.group(1);
					String productCode = hMatcher.group(2);
					Product p = products.get(productCode);
					currentCustomer.addToHistory(p, timestamp);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Could not initialize customers!");
			return;
		}
	}

	public void initHistory() {
		createFile(historyFile);
		try(Scanner sc = new Scanner(new FileInputStream(historyFile))){
			Pattern historyPattern = Pattern.compile("(.*)<customer>(.*)<product>(.*)");
			while(sc.hasNextLine()) {
				String nextLine = sc.nextLine();
				Matcher hMatcher = historyPattern.matcher(nextLine);
				if(hMatcher.matches()) {
					String timestamp = hMatcher.group(1);
					String customer = hMatcher.group(2);
					String product = hMatcher.group(3);
					totalHistory.addLast(new String[]{timestamp, customer, product});
				}				
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Could not initialize history");
			return;
		}		
	}
	
	public void add(String code) {
		if(isCorrectPassword()) {
			String answer = query("What are you adding? (c/p)");
			if(answer.equals("c")) {
				String name = query("What is your name?");
				if(!isValidInput(name)) {
					return;
				}					
				String mail  = query("Mail address:");
				if(!isValidInput(mail)) {
					return;
				}
				mail = mail.replaceAll("\\s+", "");			
				Customer c = new Customer(name, mail, code);
				customers.put(code, c);
				saveCustomers();
			} else if(answer.equals("p")) {
				String name = query("Name of the product?");
				if(!isValidInput(name)) {
					return;
				}
				String cost = query("Cost:");
				if(!isValidInput(cost)) {
					return;
				}
				cost = cost.replaceAll("\\s+", "");			
				Double costDouble = 0.0;
				try {
					costDouble = Double.parseDouble(cost);
				} catch(Exception e) {
					System.out.println("Illegal character in cost!");
					return;
				}			
				Product p = new Product(name, code, costDouble);
				products.put(code, p);
				saveProducts();
			}
		} else {
			System.out.println("Wrong password!");
		}
	}
	
	public void addToHistory(HashMap<Product, Integer> productBasket, Customer c) {
		for(Product p : productBasket.keySet()) {
			int bought = productBasket.get(p);
			for(int i=0; i<bought; i++) {
				totalHistory.addLast(new String[]{dateFormat.format(Calendar.getInstance().getTime()), c.getCode(), p.getCode()});
			}
		}
		saveHistory();
	}

	public void saveProducts() {
		TreeMap<String, Product> sortedProducts = new TreeMap<String, Product>();
		for(Product p : products.values()) {
			sortedProducts.put(p.getName(), p);			
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(productFile)));
			for(Product p : sortedProducts.values()) {
				out.println("<name>" + p.getName() + "<code>" + p.getCode() + "<cost>" + p.getCost());
			}			
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Could not save products!");
			return;
		}	
	}
	
	public void saveCustomers() {
		TreeMap<String, Customer> sortedCustomers = sortCustomers();		
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(customerFile)));
			for(Customer c : sortedCustomers.values()) {
				out.println("<name>" + c.getName() + "<email>" + c.getMail() + "<code>" + c.getCode());
				for(String[] s : c.getHistory()) {
					out.println("\t<time>" + s[0] + "<product>" + s[1]);
				}
			}
			out.println("");
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Could not save customers!");
			return;
		}
	}
		
	public void saveHistory() {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(historyFile)));
			for(String[] s : totalHistory) {
				out.println(s[0] + "<customer>" + s[1] + "<product>" + s[2]);
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Could not save history!");
			return;
		}
	}
	
	public void saveSummary() {
		createFile(summaryFile);
		try {
			TreeMap<String, Customer> sortedCustomers = sortCustomers();
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(summaryFile)));
			String mailAdresses = "";
			for(Customer c : sortedCustomers.values()) {
				mailAdresses += c.getMail() + ", ";
			}
			if(!mailAdresses.equals("")) {
				mailAdresses = mailAdresses.substring(0, mailAdresses.length()-2);
				out.println(mailAdresses);
				out.println("");
				for(Customer c : sortedCustomers.values()) {
					out.println(c.getName() + " - " + df.format(c.getDebt()) + " kr");
				}
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Could not create summary!");
			return;
		}
		System.out.println("Summary complete!");
	}

	
	public boolean isValidInput(String s) {
		if(s == null || (s != null && ("".equals(s)))) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isCorrectPassword() {
		char[] answer = System.console().readPassword("Enter the password: ");
		String pw = new String(answer);
		if(pw.equals(password)) {
			return true;
		} else {
			return false;
		}
	}

	public String query(String question) {
		System.out.print(question + " ");
		String answer = userInput.next();
		return answer;		
	}

	public void clearProducts() {
		productBasket.clear();
		lastCode = "";
	}
	
	public TreeMap<String, Customer> sortCustomers() {
		TreeMap<String, Customer> tm = new TreeMap<String, Customer>();
		for(Customer c : customers.values()) {
			tm.put(c.getName(), c);
		}
		return tm;
	}
	
	public void printNews() {
		if(totalHistory.size() > 0) {
			System.out.println("Latest purchases:");
			if(totalHistory.size() > 20) {
				System.out.println("...");
				for(int i=totalHistory.size()-20; i<totalHistory.size(); i++) {
					String[] s = totalHistory.get(i);
					System.out.println(s[0] + " - " + customers.get(s[1]).getName() + " - " + products.get(s[2]).getName());
				}
			} else {
				for(String[] s : totalHistory) {
					System.out.println(s[0] + " - " + customers.get(s[1]).getName() + " - " + products.get(s[2]).getName());
				}
			}
		} else {
			System.out.println("No purchases made yet");
		}
	}
	
	public void printHelp() {
		System.out.println("Make a purchase");
		System.out.println("1. Scan the products you wish to buy");
		System.out.println("2. Scan your code");
		System.out.println("");
		System.out.println("Add something");
		System.out.println("1. Scan the object you are adding three times");
		System.out.println("2. Follow the instructions");
		System.out.println("");
		System.out.println("Clear the varukorg");
		System.out.println("1. Scan \"clear\"");
		System.out.println("");
		System.out.println("Print a summary");
		System.out.println("1. Scan \"summary\"");
		System.out.println("");
		System.out.println("View the news");
		System.out.println("1. Scan \"news\"");
	}
	
	public void scan(String code) {	
		timer.cancel();
		timer = new Timer();
		timer.schedule(new Cleaner(this), 10000);
		switch (code) {
			case "summary":
				saveSummary();
				break;
			case "help":
				printHelp();
				break;
			case "news":
				printNews();
				break;
			case "clear":
				clearProducts();
				break;
			default:
				if(products.containsKey(code)) {
					Product activeProduct = products.get(code);
					if(productBasket.containsKey(activeProduct)) {
						productBasket.put(activeProduct, productBasket.get(activeProduct) + 1);
					} else {
						productBasket.put(activeProduct, 1);
					}
					double totalCost = 0;
					for(Product p : productBasket.keySet()) {
						int counter = productBasket.get(p);
						System.out.println("(" + counter + ") " + p.getName() + " - " + df.format(p.getCost()) + " kr");
						totalCost += p.getCost()*counter;
					}
					System.out.println("Total cost: " + df.format(totalCost) + " kr");
				} else if(customers.containsKey(code)) {
					Customer activeCustomer = customers.get(code);
					if(productBasket.size() != 0) {					
						activeCustomer.addToHistory(productBasket);
						addToHistory(productBasket, activeCustomer);
						saveProducts();
						saveCustomers();
						for(Product p : productBasket.keySet()) {
							int counter = productBasket.get(p);
							System.out.println("(" + counter + ") " + p.getName()  + " bought by " + activeCustomer.getName());
						}
						System.out.println("Total debt: " + df.format(activeCustomer.getDebt()) + " kr");
						productBasket.clear();
					} else {
						System.out.println(activeCustomer.getName() + " - Debt: " + df.format(activeCustomer.getDebt()) + " kr");
						LinkedList<String[]> history = activeCustomer.getHistory();
						if(history.size() > 0) {
							System.out.println("Latest purchases:");
							if(history.size() > 15) {
								System.out.println("...");
								for(int i=history.size()-15; i<history.size(); i++) {
									String[] s = history.get(i);
									System.out.println(s[0] + " - " + products.get(s[1]).getName());
								}
							} else {
								for(String[] s : history) {
									System.out.println(s[0] + " - " + products.get(s[1]).getName());
								}
							}
						} else {
							System.out.println("No purchases made yet");
						}
					}
				} else {
					if(code.equals(lastCode)) {
						add(code);
						lastCode = "";
					}
				}
				lastCode = code;
		}
	}	
		
	public static void main(String[] arg) {
		new Streck();
	}
}
