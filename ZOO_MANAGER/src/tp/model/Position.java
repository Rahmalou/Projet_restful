package tp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Position {

	private static final double MAX_NEAR_DISTANCE = 100; //kilomètre
	public final static double AVERAGE_RADIUS_OF_EARTH = 6371;

	private double latitude,longitude;
	public Position(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Position() {	}

	public boolean equals(Object o){
		boolean result = false;
		if (o instanceof Position){
			Position otherPosition = (Position)o; 
			result = otherPosition.latitude == this.latitude && 
					 otherPosition.longitude == this.longitude;
		}
		return result;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public String toString(){
		final StringBuffer buffer = new StringBuffer();
		buffer.append("(").append(latitude).append(", ").append(longitude).append(")");
		return buffer.toString();
	}

	public boolean isNear(Position position) {
		// TODO Auto-generated method stub
		if (this.equals(position)) {
			return true;
		}
		else {
			long dist = calculateDistance(position);
		
			return dist <= MAX_NEAR_DISTANCE;
		}
	}
	
	private long calculateDistance(Position p2) {

	    double latDistance = Math.toRadians(latitude - p2.latitude);
	    double lngDistance = Math.toRadians(longitude - p2.longitude);

	    double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)) +
	                    (Math.cos(Math.toRadians(latitude))) *
	                    (Math.cos(Math.toRadians(latitude))) *
	                    (Math.sin(lngDistance / 2)) *
	                    (Math.sin(lngDistance / 2));

	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

	    return Math.round(AVERAGE_RADIUS_OF_EARTH * c);

	}
	
}

