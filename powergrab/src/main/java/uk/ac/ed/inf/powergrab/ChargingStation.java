package uk.ac.ed.inf.powergrab;

public class ChargingStation {
		
	private String id; 
	private Position position;
	private double coins;
	private double power;
	
	public ChargingStation(String id, Position position, double coins, double power) {
		this.id = id; this.position = position; this.coins = coins; this.power = power;
	}
	
	//getters 
	public String getID() {
		return id;
	}
	public Position getPosition() {
		return position;
	}
	public double getCoins() {
		return coins;
	}
	public double getPower() {
		return power;
	}
	
	//get the coin value, then set station's coins to 0
	//and return coin value
	public double useCoins() {
		double retCoins = this.coins;
		this.coins = 0;
		return retCoins;
	}
	
	//get the power value, then set station's power to 0
	//and return power value
	public double usePower() {
		double retPower = this.power;
		this.power = 0;
		return retPower;
	}
	
	
	
}
