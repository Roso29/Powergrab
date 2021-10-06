package uk.ac.ed.inf.powergrab;

import java.io.IOException;

public class Map {

	private ChargingStation[] mapArray;
	
	public Map(int Size) {
		mapArray = new ChargingStation[Size];
	}

	//return the mapArray
	public ChargingStation[] getMap() {
		return mapArray;
	}

	
}
