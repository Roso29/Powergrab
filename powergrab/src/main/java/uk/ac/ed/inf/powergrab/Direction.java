package uk.ac.ed.inf.powergrab;

public enum Direction {

		N(0),
		NNE(1),
		NE(2),
		ENE(3),
		E(4),
		ESE(5),
		SE(6),
		SSE(7),
		S(8),
		SSW(9),
		SW(10),
		WSW(11),
		W(12),
		WNW(13),
		NW(14),
		NNW(15);
		
		public final double angleInRadians;
		
		Direction(int directionNumber) {
			this.angleInRadians = directionNumber*Math.PI/8.0;
		}
}
	
