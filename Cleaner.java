/**
	Cleaner.java
	Strecksystem 1.2
	Joel Heinerud - April 2015
	joel.heinerud@gmail.com
**/

import java.util.TimerTask;

public class Cleaner extends TimerTask {
	private Streck s;
	
	public Cleaner(Streck s) {
		this.s = s;
	}
	
	@Override
	public void run() {
		s.clearProducts();
	}

}
