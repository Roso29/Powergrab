package uk.ac.ed.inf.powergrab;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import uk.ac.ed.inf.powergrab.FileHandler;
import uk.ac.ed.inf.powergrab.drones.Drone;
import uk.ac.ed.inf.powergrab.drones.controller.StatefulController;
import uk.ac.ed.inf.powergrab.drones.controller.StatelessController;


public class App 
{
	private Map map;
	private static Drone drone;
	
    public static void main( String[] args )
    {
    		//Parse command line arguments
    		System.out.println(System.getProperty("user.dir"));
    		
    		if(args.length!=7) {
    			System.out.println("Not enough command line arguments!");
    			return;
    		}
    		
   		String date = String.join("/", args[2],args[1],args[0]);
   		Position initPos = new Position(Double.parseDouble(args[3]),Double.parseDouble(args[4]));
   		int randomSeed = Integer.parseInt(args[5]);
   		
   		String mapSource=null;	
   		MapHandler mh = new MapHandler();
   		FileHandler fh = null;
   		
   		
   		
   		//get JSON object as string from the url given the date and store 
   		//as string in mapSource
   		try {
			mapSource = mh.MapDownload(date);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
   		//Create map object by parsing the JSON object
   		Map map = mh.parseMap(mapSource);
   		
   		//Create File Handler for output files
		try {
			fh = new FileHandler(String.join("-", args[6],args[0],args[1],args[2]),mh.getFeatureCollection());
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
   
   		//Create Drone Object and Random object
   		drone = new Drone(initPos, map, fh);
   		
   		Random rnd = new Random(randomSeed);
   		
   		//Create stateful drone
   		if(args[6].equals("stateful")) { 
   			StatefulController controller = new StatefulController(drone, map, rnd);
   			//Iterate the drone until it runs out of power
   			while(drone.isAlive) {
   				controller.iterate();
   			}
   		}
   		//Create stateless drone
   		else if(args[6].equals("stateless")) {
   			StatelessController controller = new StatelessController(drone, map, rnd);
   			//Iterate the drone until it runs out of power
   			while(drone.isAlive) {
   				controller.iterate();
   			}
   		}
   		else {
   			System.out.println("Drone must either be stateless or stateful!");
   			System.out.println("Cannot accept "+args[6]+"!");
   			return;
   		}
   		
   		//Close the text file
		try {
			fh.closeTextFile();
		} catch (IOException e) {
			System.out.println("Could not close the text file.");
			e.printStackTrace();
		}
		
		//Create GEOJSON file
		try {
			
			fh.writeGEOJSON(drone.posList);
		} catch (IOException e) {
			System.out.println("Could not write the geojson file.");
			e.printStackTrace();
		}

		
	}
		
}

