package uk.ac.ed.inf.powergrab;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.gson.GeometryGeoJson;


public class MapHandler {

	private String mapSource;
	
	private static FeatureCollection fc;
	
	
	public MapHandler() {
		this.mapSource = "";
	}
	
	//function to download map json file from url
	public String MapDownload(String date) throws IOException {
		
		String urlString = "http://homepages.inf.ed.ac.uk/stg/powergrab/"+date+"/powergrabmap.geojson";
		
		URL mapUrl = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) mapUrl.openConnection();
		
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect();
		
		InputStream mapInput = conn.getInputStream();
		 
		int mapByte;
		char mapChar;

		while((mapByte = mapInput.read()) != -1) {
			mapChar = (char)mapByte;
			mapSource += mapChar;
		}
		return mapSource;	
	}
	
	public FeatureCollection getFeatureCollection() {
		return fc;
	}
	
	
	//function to get the features from the json file
	public Map parseMap(String mapSource) {
		
		fc = FeatureCollection.fromJson(mapSource);
		
		int index = 0;
		Point p;
		String id;
		double latitude;
		double longitude;
		Position position;
		double coins;
		double power;
		
		Map map = new Map(fc.features().size());
		
		//loop through the features of the json file
		for(Feature f : fc.features()) {
			
			
			p = (Point) f.geometry();	
			id = f.getProperty("id").toString();
			latitude = p.coordinates().get(1);
			longitude = p.coordinates().get(0);
			position = new Position(latitude, longitude);
			coins = f.getProperty("coins").getAsDouble();
			power = f.getProperty("power").getAsDouble();
		
			//create ChargingStation object from the feature values
			ChargingStation cs = new ChargingStation(id,position,coins,power);	
			//add ChargingStation to map array
			map.getMap()[index] = cs;
			
			index++;
		}
		return map;
	}		
}