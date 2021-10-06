package uk.ac.ed.inf.powergrab;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import uk.ac.ed.inf.powergrab.Direction;
import uk.ac.ed.inf.powergrab.Position;

public class FileHandler {

	private BufferedWriter bufWriter;
	private static FeatureCollection fc;
	private String path;
	
	
	public FileHandler(String path, FeatureCollection fc) throws FileNotFoundException {
		this.path = path;
		this.fc = fc;
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path+".txt"));
		this.bufWriter = new BufferedWriter(writer);
		
	}
	
	//function to convert position list to a line path for a json file
	private String createLineStringJson(List<Position> posList) {
		String json = "{\n "+'"'+ "type" +'"'+':'+'"'+"Feature"+'"'+",\n"
					+ '"'+ "properties" +'"'+':'+" {}"+",\n"
					+ '"'+ "geometry" +'"'+':'+'{'+"\n"
					+'"'+ "type" +'"'+':'+'"'+"LineString"+'"'+",\n"
					+'"'+"coordinates"+'"' + ": [";
		
		//loop through each position
		for(Position pos: posList) {
			//add line path element to json file
			json += '['+Double.toString(pos.longitude)+','+Double.toString(pos.latitude)+"],\n";
			
		}
		json = json.substring(0,json.length()-2) + "\n]\n}}";
		
		return json;
	}
	
	//function to create geojson file from the positions in position list
	public void writeGEOJSON(List<Position> posList) throws IOException {
		System.out.println("Writing!");
		fc.features().add(Feature.fromJson(createLineStringJson(posList)));
		FileWriter fw = new FileWriter(path+".geojson");
		fw.write(fc.toJson());
		fw.flush();
		fw.close();
		System.out.println("Done!");	
	}
	
	//function to create the text file from the drone information
	public void writePosTextFile(Position prevPos, Direction dir, Position curPos, double coinsAfter, double powerAfter) throws IOException {
		bufWriter.write(Double.toString(prevPos.latitude)+',');
		bufWriter.write(Double.toString(prevPos.longitude)+',');
		bufWriter.write(dir.toString()+',');
		bufWriter.write(Double.toString(curPos.latitude)+',');
		bufWriter.write(Double.toString(curPos.longitude)+',');
		bufWriter.write(Double.toString(coinsAfter)+',');
		bufWriter.write(Double.toString(powerAfter));
		bufWriter.newLine();
	}
	
	//close the text file
	public void closeTextFile() throws IOException {
		bufWriter.close();
	}
	
}
