package uk.ac.ed.inf.powergrab.drones.controller;

import java.util.List;
import java.util.Random;

import uk.ac.ed.inf.powergrab.ChargingStation;
import uk.ac.ed.inf.powergrab.Direction;
import uk.ac.ed.inf.powergrab.Map;
import uk.ac.ed.inf.powergrab.Position;

public class DroneController {

	protected Random rnd;
	protected Map map;

	public DroneController(Random rnd, Map map) {
		this.rnd = rnd;
		this.map = map;
	}
	
	protected Direction getRandomDirection(List<Direction> directions) {
		return directions.get(rnd.nextInt(directions.size()));
	}
	
	//find the nearest ChargingStation
	protected ChargingStation getNearestStation(List<ChargingStation> stations,Position pos) {
		
		double distance;
		double minDistance = Double.POSITIVE_INFINITY;
		
		ChargingStation nearestStation = null;
		//loop through ChargingStations, till shortest distance between pos and station is found
		for(ChargingStation cs : stations) {
			
			distance = pos.getDistance(cs.getPosition());
			if(distance<minDistance) {
				minDistance = distance;
				nearestStation = cs;
				}
			
		}
		
		return nearestStation;
	}
	
	//check if distance is smaller than connection radius
	protected boolean inConnectRadius(double distance) {
		return distance <= 0.00025;
	}
	
}
