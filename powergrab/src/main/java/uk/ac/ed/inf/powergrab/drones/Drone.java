package uk.ac.ed.inf.powergrab.drones;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ed.inf.powergrab.FileHandler;

import uk.ac.ed.inf.powergrab.ChargingStation;
import uk.ac.ed.inf.powergrab.Direction;
import uk.ac.ed.inf.powergrab.Map;
import uk.ac.ed.inf.powergrab.Position;

public class Drone {

	public Position dronePosition;
	public int moves = 0;
	private double coins;
	private double power;
	
	public boolean isAlive; 
	public int doneMoves;

	public List<Position> posList = new ArrayList<Position>();
	private Map map;
	private FileHandler fh;
	
	public Drone(Position dronePosition, Map map, FileHandler fh) {
		this.dronePosition = dronePosition;
		this.fh = fh;
		this.map = map;
		this.coins = 0.0;
		this.power = 250.0;
		this.isAlive = true;
		posList.add(dronePosition);
	
	}

	//Move the drone in given direction
	public void move(Direction direction) {
		
		//Update position of drone
		dronePosition = dronePosition.nextPosition(direction);
		//Find the nearest station to new position
		ChargingStation nearestStation = getNearestStation(this.dronePosition);
		
		//If connected to a station, update drone and station accordingly
		if(nearestStation!=null) {
			if(inConnectRadius((this.dronePosition.getDistance(nearestStation.getPosition())))) {
				this.coins = (Math.max(this.coins + nearestStation.useCoins(), 0));
				this.power = (Math.max(this.power + nearestStation.usePower(), 0));
				
			}
		}
		
		//Append state of drone to text file
		try {
			fh.writePosTextFile(dronePosition, direction, dronePosition.nextPosition(direction), this.coins, this.power);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Add position to list of positions
		posList.add(dronePosition);
		
		//Update power values and check if the drone is still alive
		this.power -= 1.25;
		moves++;
		if(moves >= 250 || this.power <= 0 ) {
			isAlive = false;
		}
	}
	
	//Check if a distance is short enough to connect to a station
	private boolean inConnectRadius(double distance) {
		return distance <= 0.00025;
	}

	//Return the nearest Station to a position
	private ChargingStation getNearestStation(Position pos) {

		double distance;
		double minDistance = Double.POSITIVE_INFINITY;
		
		ChargingStation nearestStation = null;
		
		//Loop through every station until the nearest station is found by updating 
		//the nearest station on each iteration
		for(ChargingStation cs : map.getMap()) {
			distance = pos.getDistance(cs.getPosition());
			if(distance<minDistance) {
				minDistance = distance;
				nearestStation = cs;
				}
			
		}
		return nearestStation;
	}
	
	
	
	
	//Return power of drone
	public double getPower() {
		return power;
	}
	//Return coins drone has
	public double getCoins() {
		return coins;
	}

}
