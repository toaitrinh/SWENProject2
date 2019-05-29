package mycontroller;

import controller.CarController;
import swen30006.driving.Simulation;
import world.Car;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.badlogic.gdx.Input;
import tiles.GrassTrap;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MudTrap;
import tiles.ParcelTrap;
import tiles.WaterTrap;
import utilities.Coordinate;

public class MyAutoController extends CarController{
	
	// Keeps track of the map's internal representation
	private HashMap<Coordinate, MapTile> internalMap;
	// is true if we are following a wall
	private boolean isFollowingWall = false;
	// is true if we found a parcel that is obtainable
	private boolean foundUnobstructedParcel = false;
	Map.Entry<Coordinate,MapTile> unobstructedParcel;
	// is true if we picked up the parcel
	private boolean parcelObtained;
	// remember current coordinates before getting the parcel
	private int xBeforeGettingParcel;
	private int yBeforeGettingParcel;
	// Current position
	private int xCurrentPosition;
	private int yCurrentPosition;
	// Keeps track of each move made when moving from wall to picking up parcel, so it is reversible
	private ArrayList<String> movesToParcel;
	private boolean backTracking;
	
	
	// Constructor
	public MyAutoController(Car car) {
		super(car);
		internalMap = new HashMap<Coordinate, MapTile>();
		movesToParcel = new ArrayList<String>();
	}
	
	// Called every few milliseconds
	public void update() {
		// Adds newly discovered tiles to our internal map representation
		updateInternalMap(getView());
		
		
		
		if (foundUnobstructedParcel) {
			//System.out.println("yo");
			handleGettingParcel();
			updateCurrentPosition();
			
			if (parcelObtained) {
				foundUnobstructedParcel = false;
				parcelObtained = false;
				backTracking = true;
			}
			//System.out.format("currentx=%d, currenty=%d\n", xCurrentPosition, yCurrentPosition);
			//System.out.format("parcelx=%d, parcely=%d\n", unobstructedParcel.getKey().x, unobstructedParcel.getKey().y);
			return;
		}
		if (backTracking == true) {
			backTrack();
		}
		checkIfUnobstructedParcelAvailable();
		
		
		// For testing purposes, allows user input
		updateWithUserInput();
		
		// handles wall following
		handleWallFollowing();
		// Updates our current position
		updateCurrentPosition();
	}
	
	// Updates internal map representation as new tiles are discovered
	public void updateInternalMap(HashMap<Coordinate, MapTile> currentView) {
		for (Map.Entry<Coordinate,MapTile> entry : currentView.entrySet()) {
			if (!internalMap.containsKey(entry.getKey())) {
				internalMap.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	
	// For testing purposes, accepts user input
	public void updateWithUserInput() {
		Set<Integer> parcels = Simulation.getParcels();
		Simulation.resetParcels();
        for (int k : parcels){
		     switch (k){
		        case Input.Keys.B:
		        	applyBrake();
		            break;
		        case Input.Keys.UP:
		        	applyForwardAcceleration();
		            break;
		        case Input.Keys.DOWN:
		        	applyReverseAcceleration();
		        	break;
		        case Input.Keys.LEFT:
		        	turnLeft();
		        	break;
		        case Input.Keys.RIGHT:
		        	turnRight();
		        	break;
		        default:
		      }
		  }
	}
	
	// Returns an arraylist containing tiles of the specified type
	public HashMap<Coordinate,MapTile> getTilesByType(String type) {
		HashMap<Coordinate,MapTile> tiles = new HashMap<Coordinate,MapTile>();
		
		for (Map.Entry<Coordinate,MapTile> tile : internalMap.entrySet()) {
			if (type.equals("grass") && tile.getClass().equals(GrassTrap.class)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("health") && tile.getClass().equals(HealthTrap.class)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("lava") && tile.getClass().equals(LavaTrap.class)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("mud") && tile.getClass().equals(MudTrap.class)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("parcel") && tile.getValue().getClass().equals(ParcelTrap.class)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("water") && tile.getValue().getClass().equals(WaterTrap.class)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("wall") && tile.getValue().getType().equals(MapTile.Type.WALL)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("empty") && tile.getValue().getType().equals(MapTile.Type.EMPTY)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("start") && tile.getValue().getType().equals(MapTile.Type.START)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("road") && tile.getValue().getType().equals(MapTile.Type.ROAD)) {
				tiles.put(tile.getKey(), tile.getValue());
			} else if (type.equals("finish") && tile.getValue().getType().equals(MapTile.Type.FINISH)) {
				tiles.put(tile.getKey(), tile.getValue());
			}
		}
		
		return tiles;
	}
	
	
	private boolean checkWallAhead(){
		switch(getOrientation()){
		case EAST:
			return checkDirection("east");
		case NORTH:
			return checkDirection("north");
		case SOUTH:
			return checkDirection("south");
		case WEST:
			return checkDirection("west");
		default:
			return false;
		}
	}
	

	private boolean checkFollowingWall() {
		
		switch(getOrientation()){
		case EAST:
			return checkDirection("north");
		case NORTH:
			return checkDirection("west");
		case SOUTH:
			return checkDirection("east");
		case WEST:
			return checkDirection("south");
		default:
			return false;
		}	
	}
	
	
	public boolean checkDirection(String direction) {
		Coordinate currentPosition = new Coordinate(getPosition());
		
		MapTile tile;
		if (direction.equals("east")) {
			tile = internalMap.get(new Coordinate(currentPosition.x+1, currentPosition.y));
		} else if (direction.equals("west")) {
			tile = internalMap.get(new Coordinate(currentPosition.x-1, currentPosition.y));
		} else if (direction.equals("north")) {
			tile = internalMap.get(new Coordinate(currentPosition.x, currentPosition.y+1));
		} else {
			tile = internalMap.get(new Coordinate(currentPosition.x, currentPosition.y-1));
		}
		
		
		if(tile.isType(MapTile.Type.WALL) || tile.getClass().equals(LavaTrap.class)){
			return true;
		}
		
		return false;
	}
	
	
	public void handleWallFollowing() {
		if(getSpeed() < 1){       // Need speed to turn and progress toward the exit
			applyForwardAcceleration();   // Tough luck if there's a wall in the way
		}
		if (isFollowingWall) {
			// If wall no longer on left, turn left
			if(!checkFollowingWall()) {
				turnLeft();
			} else {
				// If wall on left and wall straight ahead, turn right
				if(checkWallAhead()) {
					turnRight();
				}
			}
		} else {
			// Start wall-following (with wall on left) as soon as we see a wall straight ahead
			if(checkWallAhead()) {
				turnRight();
				isFollowingWall = true;
			}
		}
	}
	
	// Updates our current position
	public void updateCurrentPosition() {
		Coordinate currentPosition = new Coordinate(getPosition());
		xCurrentPosition = currentPosition.x;
		yCurrentPosition = currentPosition.y;
	}
	
	//
	public void checkIfUnobstructedParcelAvailable() {
		// If we havent even found a parcel yet, no point in proceeding
		HashMap<Coordinate,MapTile> parcels = getTilesByType("parcel");
		
		if (parcels.entrySet().size() == 0) {
			foundUnobstructedParcel = false;
			return;
		}
		
		// updates in case we need to start finding the parcel, and then return to this position later
		Coordinate currentPosition = new Coordinate(getPosition());
		int xCurrentPosition = currentPosition.x;
		int yCurrentPosition = currentPosition.y;
		xBeforeGettingParcel = xCurrentPosition;
		yBeforeGettingParcel = yCurrentPosition;
		
		HashMap<Coordinate,MapTile> badTiles = getTilesByType("lava");
		badTiles.putAll(getTilesByType("wall"));
		
		// The difference between current position and parcel position
		for (Map.Entry<Coordinate,MapTile> parcel : parcels.entrySet()) {
			// Difference between my current position and the parcel's position
			int xDiff = parcel.getKey().x - xCurrentPosition;
			int yDiff = parcel.getKey().y - yCurrentPosition;
			System.out.format("xDiff=%d, yDiff=%d\n", xDiff, yDiff);
			// if a wall or lava tile comes between us, return False
			for (Map.Entry<Coordinate,MapTile> badTile : badTiles.entrySet()) {
				if (xCurrentPosition==badTile.getKey().x && Math.abs(yCurrentPosition-badTile.getKey().y) <= yDiff ||
						yCurrentPosition==badTile.getKey().y && Math.abs(xCurrentPosition-badTile.getKey().x) <= xDiff) {
					foundUnobstructedParcel = false;
					return;
				}
			}
			unobstructedParcel = parcel;
			break;
		}
		//System.out.format("parcelx=%d, parcely=%d", unobstructedParcel.getKey().x, unobstructedParcel.getKey().y);
		foundUnobstructedParcel = true;
		
	}
	
	// 
	public void handleGettingParcel() {
		
		Coordinate currentPosition = new Coordinate(getPosition());
		int xCurrentPosition = currentPosition.x;
		int yCurrentPosition = currentPosition.y;
		
		// difference between current position and parcel position tells us where to go
		int xDiff = unobstructedParcel.getKey().x - xCurrentPosition;
		int yDiff = unobstructedParcel.getKey().y - yCurrentPosition;
		System.out.format("xDiff=%d, yDiff=%d\n", xDiff, yDiff);
		//System.out.format("xcurrentPosition=%d, yCurrentPosition=%d\n", xCurrentPosition, yCurrentPosition);
		//System.out.println(getOrientation().toString());
		
		// Make sure I'm oriented towards the parcel
		WorldSpatial.Direction requiredOrientation = null;
		if (xDiff==0 && yDiff>0) {
			requiredOrientation = WorldSpatial.Direction.NORTH;
		} else if (xDiff==0 && yDiff<0) {
			requiredOrientation = WorldSpatial.Direction.SOUTH;
		} else if (yDiff==0 && xDiff>0) {
			requiredOrientation = WorldSpatial.Direction.EAST;
		} else if (yDiff==0 && xDiff<0) {
			requiredOrientation = WorldSpatial.Direction.WEST;
		}
		
		// If we are not at the required orientation, make a turn
		System.out.format("orientation= %s,   requiredOrientation= %s\n", getOrientation(), requiredOrientation);
		if (!getOrientation().equals(requiredOrientation)) {
			turnRight();
			movesToParcel.add("right");
		}
		
		if (xDiff==0 && Math.abs(yDiff)>0) {
			applyForwardAcceleration();
			movesToParcel.add("forward");
		}/* else if (xDiff==0 && yDiff<0) {
			applyReverseAcceleration();
		}*/ else if (yDiff==0 && Math.abs(xDiff)>0) {
			applyForwardAcceleration();
			movesToParcel.add("forward");
		}/* else if (yDiff==0 && xDiff<0) {
			applyReverseAcceleration();
		} */else if (xDiff==0 && yDiff==0) {
			parcelObtained = true;
			internalMap.remove(unobstructedParcel);
			unobstructedParcel = null;
		}
	}
	
	public void backTrack() {
		if (movesToParcel.size() == 0) {
			backTracking = false;
			return;
		}
		String move = movesToParcel.get(movesToParcel.size()-1);
		if (move.equals("forward")) {
			applyReverseAcceleration();
		} else if (move.equals("right")) {
			turnLeft();
		}
	}	
}
