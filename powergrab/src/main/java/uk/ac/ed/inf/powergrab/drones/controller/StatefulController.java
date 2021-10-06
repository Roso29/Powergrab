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

public class StatefulController extends DroneController{

	private Drone drone;
	private List<ChargingStation> stationOrder = new ArrayList<ChargingStation>();
	private List<Direction> lastDirectionList = new ArrayList<Direction>();
	private Direction lastDirection;
	private int movesFromStation;
	
	public StatefulController(Drone drone, Map map, Random rnd) {
		super(rnd,map);
		this.drone = drone;
		//Create order of stations to travel
		stationOrder = findBestPath();
	}
	
	private int getNumberOfPositiveStations() {
		int numberOfPositive = 0;
		
		for(ChargingStation cs : map.getMap()) {
			if(cs.getCoins()>=0) {
				numberOfPositive++;
			}
		}
		return numberOfPositive;
		
	}
	
	//Compare distances of each general route calculated
	private List<ChargingStation> findBestPath() {
		List<ChargingStation> bestOrder = getStationOrder();

		//Calculate distance of route from the tactic of repeatedly 
		//travelling to the next nearest station
		
		List<Position> posoList = new ArrayList<Position>();
		//Iterate through positions from this tactic and add them to a list
		for(ChargingStation cs : bestOrder) {
			posoList.add(cs.getPosition());
		}
		
		double totalDist=0;
		
		//Roughly calculate the distance required for this route
		for(int posIndex = 1; posIndex<posoList.size();posIndex++) {
			totalDist += posoList.get(posIndex).getDistance(posoList.get(posIndex-1));
		}
		double minDist = totalDist;
		//Repeat this distance check for each of the k clustered routes
		//Set the route order as the one with the lowest estimated distance
		
		List<ChargingStation> clusterStationOrder = new ArrayList<ChargingStation>();
		int totalPositiveStations = getNumberOfPositiveStations();
		
		for(int i=2;i<=totalPositiveStations;i++) {
			totalDist=0;
			clusterStationOrder = KCluster(i);
			posoList.clear();
			for(ChargingStation cs : clusterStationOrder) {
				posoList.add(cs.getPosition());
			}
			
			for(int posIndex = 1; posIndex<posoList.size();posIndex++) {
				totalDist += posoList.get(posIndex).getDistance(posoList.get(posIndex-1));
			}
			
			if(totalDist<minDist) {
				bestOrder = clusterStationOrder;
				
				minDist = totalDist;
			}
		}
		return bestOrder;
	}
	
	//Function for creating route order by clustering the stations into
	//k clusters. 
	
	private List<ChargingStation> KCluster(int k) {
		
		List<ChargingStation> posStations = new ArrayList<ChargingStation>();
		Position[] clusters = new Position[k];
		List<ChargingStation>[] clusterAssign =  new ArrayList[k];
		
		
		int clusterIndex;
		int nearestClusterIndex = 0;
		
		//Create list of positive stations on map
		for(ChargingStation cs : map.getMap()) {
			if(cs.getCoins()>=0) 
				posStations.add(cs);
		}
		
		//Create a list of stations for each cluster and store each list in an array
		for(int index = 0; index<k; index++) {
			clusterAssign[index] = new ArrayList<ChargingStation>();
		}
	
		//Set initial clusters to first k positions in list
		for(int i=0;i<k;i++) {
			clusters[i] = posStations.get(i).getPosition();
		}
		
		//Repeat clustering technique 5 iterations
		for(int iter=0;iter<5;iter++) {
			//Clear each cluster assignment list
			for(List<ChargingStation> clusterStations : clusterAssign) {
				clusterStations.clear();
			}
	
			//Loop through each positive station
			for(ChargingStation cs : posStations) {
				double nearestStationDist = Double.POSITIVE_INFINITY;
				clusterIndex=0;
				
				//Calculate which cluster the station is nearest to
				for(Position cluster : clusters) {
					if(cluster.getDistance(cs.getPosition())<nearestStationDist) {
						nearestStationDist = cluster.getDistance(cs.getPosition());
						nearestClusterIndex = clusterIndex;
					}
					clusterIndex++;
				}
				//Assign the station to the nearest cluster
				clusterAssign[nearestClusterIndex].add(cs);
			}
			
			//Recalculate the cluster centres by averaging the positions assigned to each cluster
			for(clusterIndex=0;clusterIndex<k;clusterIndex++) {
				double longitude =0;
				double latitude  =0;
				
				for(ChargingStation cs : clusterAssign[clusterIndex]) {
					longitude += cs.getPosition().longitude;
					latitude  += cs.getPosition().latitude;
				}
				
				longitude = longitude/clusterAssign[clusterIndex].size();
				latitude = latitude/clusterAssign[clusterIndex].size();
		
				clusters[clusterIndex] = new Position(latitude, longitude);	
			}
			
		}
		
		List<ChargingStation> routeOrder = new ArrayList<ChargingStation>();
		
		Position firstStationOfCluster = drone.dronePosition;
		
		//Order the clusters by starting at nearest cluster then 
		//moving to the next nearest cluster and repeating
		for(int cluster : orderClusters(Arrays.asList(clusters))){
			//Order current cluster and add cluster route to overall route 
			routeOrder.addAll(getClusterOrder(clusterAssign[cluster],firstStationOfCluster));
			firstStationOfCluster = routeOrder.get(routeOrder.size()-1).getPosition();
		}
		
		return routeOrder;
	}
	
	private int[] orderClusters(List<Position> clusters){
		/*Orders the list of cluster centres by starting at the initial
		  drone position then finding the nearest cluster to that, then
		  repeating till all the clusters have been ordered.
		*/
		
		List<Position> orderedClusters = new ArrayList<Position>();
		int[] clusterOrder = new int[clusters.size()];
		Position currPos = drone.dronePosition;
		Position nextPos = null;
		int nextIndex=0;
		double dist = 0;
	
		//Loop through each cluster
		for(int i = 0; i<clusters.size();i++) {
			
			double minDist = Double.POSITIVE_INFINITY;
			

			for(Position cluster : clusters) {
				
				dist = cluster.getDistance(currPos);
				//if this distance is the shortest yet and this cluster has not
				//been visited yet, set next cluster to this cluster
				if(dist<minDist && !orderedClusters.contains(cluster)) {
					minDist = dist;
					
					nextPos = cluster;
					nextIndex = clusters.indexOf(cluster);
				}
			}
			//add the next nearest cluster to orderClusters list
			orderedClusters.add(nextPos);
			
			//update current position and append the next nearest cluster to the ordered cluster array
			currPos = nextPos;
			clusterOrder[i] = nextIndex;
		}
	
		return clusterOrder;
		
	}

	
	//calculate chosen route within a cluster
	private List<ChargingStation> getClusterOrder(List<ChargingStation> cluster,Position pos){
		
		//create list to store the ordered stations
		List<ChargingStation> orderedStations = new ArrayList<ChargingStation>();
		
		//if cluster contains 1 or 0 stations, return the station
		if(cluster.size()==1 || cluster.size()==0) {
			return cluster;
		}
		
		//initialised variables
		double minDist = Double.POSITIVE_INFINITY;
		double dist;
		ChargingStation closestStation = null;
		
		/*//loop through each cluster
		for(ChargingStation cs : cluster) {
			//find nearest station to initPos by getting the distance of each 
			dist = getDistance(pos, cs.getPosition());
			if(dist<minDist) {
				closestStation = cs;
				minDist = dist;
			}
		}*/
		
		//get closest station to current position
		closestStation = getNearestStation(cluster,pos);
		//remove closestStation from cluster list and add it to the orderedStations list
		cluster.remove(closestStation);
		orderedStations.add(closestStation);
		
		//recursively call this function with new cluster list without closestStation
		//and updating the position to the position of the closestStation
		orderedStations.addAll((getClusterOrder(cluster, closestStation.getPosition())));
		return orderedStations;
	}
	
	//function to get station order with no clusters
	private List<ChargingStation> getStationOrder(){
		//create list of visited stations, 
		//get nearest station to drones starting position,
		
		List<ChargingStation> visited = new ArrayList<ChargingStation>();
		ChargingStation nearestStation = getNearestPosStation(drone.dronePosition,visited);
		ChargingStation currentStation;
		
		//keep getting the nearest positive station to the current station the drone will be at,
		//until all positive stations have been visited
		while(nearestStation != null) {
			visited.add(nearestStation);
			currentStation = nearestStation;	
			nearestStation = getNearestPosStation(currentStation.getPosition(),visited);	
		}
		return visited;
	}
	
	//get the nearest positive station to some position
	private ChargingStation getNearestPosStation(Position pos,List<ChargingStation> visited) {
		
		ChargingStation nearestStation = null;
		double nearestDist=Double.POSITIVE_INFINITY;
		
		//loop through each station
		for(ChargingStation csV : map.getMap()) {
			//if that station has negative/no coins or has already been visited, ignore it
			if(csV.getCoins()<=0 || visited.contains(csV))
				continue;
			
			//get distance from station to position
			double distanceUV = pos.getDistance(csV.getPosition());
			
			//if its closer than current closest station,
			//update variables
			if(distanceUV<nearestDist) {
				nearestStation = csV;
				nearestDist = distanceUV; 
			}
		}
		return nearestStation;
	}
	
	
	//function to calculate the mean of some list of positions
	private Position getMeanOfPositions(List<Position> posList) {
		//create array to store/calculate the mean positions
		double[] muPos = new double[2];
		//calculate the total longitude and latitude
		for(Position pos : posList) {
			muPos[0] = muPos[0]+pos.longitude;
			muPos[1] = muPos[1]+pos.latitude;
		}
		//divide each latitude and longitude by size of posList
		muPos[0] = muPos[0]/posList.size();
		muPos[1] = muPos[1]/posList.size();
		//create position variable from these means
		Position meanPosition = new Position(muPos[1],muPos[0]);
		return meanPosition;
	}
	
	//function that guides the drone through the chosen route 
	
	private Direction chooseDirection() {
		
		//movesFromStation++;
		//get the next station to visit
		//initialise variables
		ChargingStation nextStation = stationOrder.get(0);
		ChargingStation nearestStation;
		Direction chosenDirection = null;

		//if there have been more than 20 moves since the most recent station visit,
		//and the drone is within 5 moves of the mean of the last 20 positions
		if(movesFromStation > 20 && drone.dronePosition.getDistance(getMeanOfPositions(drone.posList.subList(drone.posList.size()-21, drone.posList.size()-1)))<5*Position.distance) {
			
			//if there are more than 3 stations left
			if(stationOrder.size()>=3) {
				//swap the next station to visit with the station 2 visits away
				ChargingStation cs1 = stationOrder.get(2);
				stationOrder.set(2, stationOrder.get(0));
				stationOrder.set(0,cs1);
			//movesFromStation=0;
			}
			else {
				movesFromStation=0;
				return getSafeDirection(angleToDirection(getAngle(nextStation.getPosition(),drone.dronePosition)));
				
			}
			//reset moves from last visited station
			movesFromStation=0;
			return null;
		}
		
		//increment moves from the last visited station 
		movesFromStation++;
		
		//approximate the direction required to move towards the next station
		Direction direction = (angleToDirection(getAngle(nextStation.getPosition(),drone.dronePosition)));
		
		//get nearest station to drones current position
		nearestStation = getNearestStation(Arrays.asList(map.getMap()),drone.dronePosition.nextPosition(direction));
		
		//if drone is close enough to connect to the nearest station
		if(inConnectRadius(drone.dronePosition.nextPosition(direction).getDistance(nearestStation.getPosition()))) {
			
			//if the nearest station is positive
			if(nearestStation.getCoins()>0) {
				
				//check if the drone is stuck in a loop or the next move will force the drone outside the play area,
				//find a new safe direction
				if(isStuck(drone.dronePosition.nextPosition(direction),direction)||!drone.dronePosition.nextPosition(direction).inPlayArea()) {
					direction = getSafeDirection(direction);
				}
				
				//set chosenDirection to safe direction or approximated direction
				chosenDirection = direction;
				
				//if new direction causes the drone to connect to a station
				if(inConnectRadius(drone.dronePosition.nextPosition(direction).getDistance(nearestStation.getPosition()))&&nearestStation == getNearestStation(Arrays.asList(map.getMap()),drone.dronePosition.nextPosition(direction))) {
					//remove station from station order list
					stationOrder.remove(nearestStation);
					movesFromStation=0;
				}
				
			}
			
			//if station has no coins
			else if(nearestStation.getCoins()==0) {
				//check if drone is either stuck or will become out of play area 
				if(isStuck(drone.dronePosition.nextPosition(direction),direction)||!drone.dronePosition.nextPosition(direction).inPlayArea()) {
					direction = getSafeDirection(direction);
				}
				//update chosenDirection
				chosenDirection = direction;
				
				//if nearestStation is the next station we want to visit remove it from the stationOrder, 
				//and reset moves from last station
				if(nearestStation==nextStation) {
					stationOrder.remove(0);
					movesFromStation = 0;
				}
			}
			
			//if the nearest station is a negative station
			else {
				
				//get the direction from either side of the current direction
				Direction[] directions=(alternativeDir(direction,direction));
				
				while(true){
					boolean goodDirection = false;
					//get the nearest station from the first alternate direction
					for(int dir = 0; dir<=1;dir++) {
					ChargingStation nearestStation1 = getNearestStation(Arrays.asList(map.getMap()),drone.dronePosition.nextPosition(directions[dir]));
					
					//check if the alternative direction keeps the drone in the play area,
					//avoids negative directions,
					//or does not connect to a negative station if the nearest station is one
					goodDirection = drone.dronePosition.nextPosition(directions[dir]).inPlayArea() &&
							(nearestStation1.getCoins()>=0 || 
							!inConnectRadius(drone.dronePosition.nextPosition(directions[dir]).getDistance(nearestStation1.getPosition())));
					
					//if the direction meets the conditions above
					if(goodDirection) {
						//check if the alternative direction makes the drone stuck
						if(isStuck(drone.dronePosition.nextPosition(directions[dir]),directions[dir])) {
							//get some safe direction
							directions[dir] = getSafeDirection(directions[dir]);
						}
						
						//set chosen direction to this direction
						chosenDirection=directions[dir];
						break;
					}}
					if(!goodDirection) {
						directions = (alternativeDir(directions[0],directions[1]));
					}
					else {
						break;
					}
					
					//repeat these steps 
					
				}
			}
		}
		//if drones direction doesn't cause a connection
		else {
			//if direction keeps the drone in the play area, set chosen direction to direction
			if(drone.dronePosition.nextPosition(direction).inPlayArea()) {
				chosenDirection = direction;
			}
			//if not, choose some safe direction
			else {
				chosenDirection = getSafeDirection(direction);
			}
		}
		
		//add the chosen direction to the list of directions and update the last direction variable
		lastDirectionList.add(chosenDirection);
		lastDirection = chosenDirection;
		return chosenDirection;
		
	}
	
	//function that checks if the drone is caught in a back and forth loop
	private boolean isStuck(Position nextPos,Direction direction) {
		
		//if the drone is on its first move
		
		
		//if the drone has done more than two moves
		if(drone.posList.size()>2) {
			//if the drones position is the same as its position 2 moves ago, return true
			if(nextPos.latitude==drone.posList.get(drone.posList.size()-2).latitude&&
					nextPos.longitude==drone.posList.get(drone.posList.size()-2).longitude) {
					return true;
			}
			
		}
		//return false if neither condition met
		return false;
	}
	
	//function that gets some random and safe direction
	private Direction getSafeDirection(Direction direction) {
		
		//initialise variables, and get the opposite of last direction 
		ChargingStation nearestStation = null;
		Direction oppositeLastDir = Direction.values()[(int) ((direction.angleInRadians/(Math.PI/8.0)+8)%16)];
		boolean safe;
		List<Direction> safeDirections = new ArrayList<Direction>();
		
		//loop through each possible direction
		for(Direction potentialDir : Direction.values()) {
			
			if(!(potentialDir==oppositeLastDir)) {
				//check if direction will keep drone in play area and avoid negative stations
				//if so, add it to list of safe directions
				nearestStation = getNearestStation(Arrays.asList(map.getMap()),drone.dronePosition.nextPosition(potentialDir));
				safe = drone.dronePosition.nextPosition(potentialDir).inPlayArea()&& (nearestStation.getCoins()>=0 || !inConnectRadius(drone.dronePosition.nextPosition(potentialDir).getDistance(nearestStation.getPosition())));
				if(safe)
					safeDirections.add(potentialDir);
			}
		}
		//return random, safe direction
		return getRandomDirection(safeDirections);
	}
	
	
	//get directions clockwise and anticlockwise of dir1 and dir2 on the compass respectively
	private Direction[] alternativeDir(Direction dir1, Direction dir2) {
		
		Direction[] altDir = new Direction[2];
		int dir1Int = (int) (dir1.angleInRadians/(Math.PI/8.0)+15)%16;
		int dir2Int = (int) (dir2.angleInRadians/(Math.PI/8.0)+17)%16;

		altDir[0] = Direction.values()[dir1Int];
		altDir[1] = Direction.values()[dir2Int];
		return altDir;
	}
	
	//convert an angle in degrees to a direction
	private Direction angleToDirection(double angle) {
		int directionIndex = (int) (Math.round(angle/22.5))%16;
		Direction direction = Direction.values()[directionIndex];
		return direction;
	}
	
	//computer the bearing angle from north required to move from pos1 to pos2
	private double getAngle(Position pos1, Position pos2) {
		double deltaLat = pos1.latitude-pos2.latitude;
		double deltaLong = pos1.longitude-pos2.longitude;
		double theta = Math.toDegrees(Math.acos(
				deltaLat/Math.sqrt(Math.pow(deltaLat,2)+Math.pow(deltaLong,2))));
		if(deltaLong>=0) 
			return theta;
		else
			return 360-theta;
	}
	
	//repeatedly go back and forth till all the moves are used
	private Direction finishMoves() {
		Direction nextDirection = Direction.values()[(int) ((Math.round(lastDirection.angleInRadians/(Math.PI/8.0))+8)%16)];
		lastDirection = nextDirection;
		return nextDirection;
	}

	
	public void iterate() {
		Direction direction;
		
		//if there are stations left to visit
		if(stationOrder.size()!=0) {
			//get the best direction to go to that station
			direction = chooseDirection();
		}
		
		//if no stations left
		else{
			//move drone back and forth
			direction = finishMoves();
		}
		
		if(direction != null) {
			drone.move(direction);
		}
		
	}
	
}
