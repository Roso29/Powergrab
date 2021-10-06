package uk.ac.ed.inf.powergrab.drones.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import uk.ac.ed.inf.powergrab.ChargingStation;
import uk.ac.ed.inf.powergrab.Direction;
import uk.ac.ed.inf.powergrab.Map;
import uk.ac.ed.inf.powergrab.Position;
import uk.ac.ed.inf.powergrab.drones.Drone;

public class StatelessController extends DroneController{

	private List<Direction> allowableMoves = new ArrayList<Direction>();
	private Drone drone;
	
	
	public StatelessController(Drone drone, Map map,Random rnd) {
		super(rnd,map);
		this.drone = drone;
	}
	
	//return array of ChargingStations that can be connected to after one move
	private ChargingStation[] getRankDirection(List<Direction> allowableMoves){

		double distance = 0;
		double minDistance;
		
		Position newPosition;
		ChargingStation nearestStation;
		
		ChargingStation[] nearestStations = new ChargingStation[allowableMoves.size()];
		
		int dirIndex = 0;
		for(Direction direction : allowableMoves) {
			
			newPosition = drone.dronePosition.nextPosition(direction);
			nearestStation = getNearestStation(Arrays.asList(map.getMap()),newPosition);
			
			if(!inConnectRadius(newPosition.getDistance(nearestStation.getPosition()))) {
				nearestStation = null;
			}
			
			if(allowableMoves.contains(direction))
				nearestStations[dirIndex] = nearestStation;
			dirIndex++;
		}
		return nearestStations;
	}
	
	private Direction chooseDirection(List<Direction> allowableMoves) {
		
		ChargingStation[] rankDirection = getRankDirection(allowableMoves);
		
		double coinMaxPos = 0;double coinMaxNeg = Double.NEGATIVE_INFINITY;
		
		Direction bestDirection = null;
		Direction bestDirectionNeg = null;
		ChargingStation nearestStation;
		
		List<Direction> nullDirections = new ArrayList<Direction>();

		//go through each possible direction
		for(int i = 0; i < rankDirection.length; i++) {	
			//if the direction does not lead to a station connection
			//add the direction to list of null direction
			if(rankDirection[i]==null) {
				nullDirections.add(allowableMoves.get(i));
			}
			
			//if the direction leads to connecting to a visited station
			//add the direction to list of null direction
			else if(rankDirection[i].getCoins()==0) {
				nullDirections.add(allowableMoves.get(i));
			}
			//if the direction leads to positive station and the best number of coins yet,
			//prioritise this direction
			else if(rankDirection[i].getCoins()>0 && rankDirection[i].getCoins()>coinMaxPos){
				coinMaxPos = rankDirection[i].getCoins();
				bestDirection = allowableMoves.get(i);
				nearestStation = rankDirection[i];
			}
			//if we can find a best direction, choose least bad negative station
			else if(bestDirection == null && rankDirection[i].getCoins()<0 && rankDirection[i].getCoins()>coinMaxNeg) {
				bestDirectionNeg = allowableMoves.get(i); 
				nearestStation = rankDirection[i];
			}
		}
		//if there is no positive charging station possible
		if(bestDirection == null) {
			
			if(nullDirections.size()==0) {
				//choose the best negative station if there is no positive/neutral direction
				bestDirection = bestDirectionNeg;
				
			}else {
				//choose one of the safe/neutral directions
			bestDirection = getRandomDirection(nullDirections);
			nearestStation = null;
			}
		}
		return bestDirection;
	}
	

	//get list of directions that keep the the drone within the play area
	private List<Direction> scanAllowableMoves() {
		List<Direction> moves = new ArrayList<Direction>();
	
		//loop through every direction, and add to list if in play area
		for(Direction direction : Direction.values()) {	
			if (drone.dronePosition.nextPosition(direction).inPlayArea())
				moves.add(direction);
		}
		return moves;	
		
	}
	
	public void iterate() {
		//find valid direction
		allowableMoves = scanAllowableMoves();
		//choose best direction from the allowable moves
		Direction direction = chooseDirection(allowableMoves);
		
		drone.move(direction);
		
	}
	
}
