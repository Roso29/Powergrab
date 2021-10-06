package uk.ac.ed.inf.powergrab;

public class Position {
	
		
		public double latitude;
		public double longitude;
		public static final double distance = 0.0003;
		
		
		public Position(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		
		//calculate new position object based on travelling in direction from this position
		public Position nextPosition(Direction direction) {
			double angleInRadians = direction.angleInRadians;
			
			//calculate new latitude and longitude
			double newLatitude = latitude + Math.cos(angleInRadians)*distance;
			double newLongitude = longitude + Math.sin(angleInRadians)*distance;
						
			//create new Position object using the latitude and longitude
			return new Position (newLatitude, newLongitude);
		
		}
		
		//calculate distance from Position pos
		public double getDistance(Position pos) {
			double distance = (Math.sqrt(Math.pow(this.latitude - pos.latitude,2)+
					Math.pow(this.longitude-pos.longitude,2)));
			return distance;
		}
		
		
		//decide whether position is in play area or not
		public boolean inPlayArea() {
			
			boolean PlayAreaLat = this.latitude > 55.942617 && this.latitude < 55.946233; 
			boolean PlayAreaLong = this.longitude < -3.184319 && this.longitude > -3.192473; 
			
			return PlayAreaLat && PlayAreaLong;
		}
		
	
	
}
