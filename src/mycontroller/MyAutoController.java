package mycontroller;

import controller.CarController;
import swen30006.driving.Simulation;
import world.Car;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;

import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;


public class MyAutoController extends CarController{		
		
		private HashMap<Coordinate, MapTile> map = new HashMap<Coordinate, MapTile>();
	
		// How many minimum units the wall is away from the player.
		private int wallSensitivity = 1;
		
		private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.
		
		// Car Speed to move at
		private final int CAR_MAX_SPEED = 1;
		
		private int parcelsFound = 0;
		private ArrayList<Coordinate> parcels = new ArrayList<Coordinate>();
		private boolean exitFound = false;
		private ArrayList<Coordinate> exit = new ArrayList<Coordinate>();
		private String phase = "explore";
		private ArrayList<Coordinate> lava = new ArrayList<Coordinate>();
		private ArrayList<Coordinate> health = new ArrayList<Coordinate>();
		//private ArrayList<Coordinate> ice = new ArrayList<Coordinate>();
		
		private ArrayList<Coordinate> travelled = new ArrayList<Coordinate>();
		
		private CarStrategy strategy; 
		
		private static Simulation.StrategyMode mode;
				
		public MyAutoController(Car car) {
			super(car);
			mode = Simulation.toConserve();
			strategy = CarStrategyFactory.getInstance().getStrategy(this);
			HashMap<Coordinate, MapTile> x = getMap();
		}
				
		public Simulation.StrategyMode getMode() {
			return mode;
		}
		
		public ArrayList<Coordinate> getParcels() {
			return parcels;
		}
		
		public ArrayList<Coordinate> getExit() {
			return exit;
		}
		
		public ArrayList<Coordinate> getTravelled() {
			return travelled;
		}
		
		// Updates internal map representation as new tiles are discovered
		public void updateMap(HashMap<Coordinate, MapTile> currentView) {
			for (Coordinate key : currentView.keySet()) {
				if (!map.containsKey(key)) {
					MapTile value = currentView.get(key);
					map.put(key, value);
					if (value.getType() == MapTile.Type.TRAP) {
						TrapTile value2 = (TrapTile) value;
						if (value2.getTrap().equals("parcel")) {
							parcelsFound++;
							parcels.add(key);
							if (parcelsFound == numParcels() && exitFound == true) {
								phase = "search";
							}
						} else if (value2.getTrap().equals("lava")) {
							lava.add(key);
						} else if (value2.getTrap().equals("health") || value2.getTrap().equals("water")) {
							health.add(key);
						}
					} else if (value.getType() == MapTile.Type.FINISH) {
						exitFound = true;
						exit.add(key);
						if (parcelsFound == numParcels() && exitFound == true) {
							phase = "search";
						}
					} 
				}
			}
		}
		
		// Coordinate initialGuess;
		// boolean notSouth = true;
		@Override
		public void update() {
			if (phase.equals("explore")) {
				updateMap(getView());
				// checkStateChange();
				if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
					applyForwardAcceleration();   // Tough luck if there's a wall in the way
				}
				if (isFollowingWall) {
					// If wall no longer on left, turn left
					if (travelled.contains(new Coordinate(getPosition()))) {
						isFollowingWall = false;
					} else {
						if(!checkFollowingWall(getOrientation(), map)) {
							turnLeft();
						} else {
							// If wall on left and wall straight ahead, turn right
							if(checkWallAhead(getOrientation(), map)) {
								if (!checkWallRight(getOrientation(), map))	{
									turnRight();
									isFollowingWall = true;
								} else if (!checkWallLeft(getOrientation(), map)){
									turnLeft();
								} else {
									applyReverseAcceleration();
								}
							}
						}
						Coordinate tempcoord = new Coordinate(getPosition());
						travelled.add(tempcoord);
						if (parcels.contains(tempcoord)) {
							parcels.remove(tempcoord);
						}
					}
				} else {
					// Start wall-following (with wall on left) as soon as we see a wall straight ahead
					if(checkWallAhead(getOrientation(), map)) {
						if (!checkWallRight(getOrientation(), map))	{
							turnRight();
							isFollowingWall = true;
						} else if (!checkWallLeft(getOrientation(), map)){
							turnLeft();
						} else {
							applyReverseAcceleration();
						}
					}
				}
			} else {
				strategy.update(this);
				Coordinate tempcoord = new Coordinate(getPosition());
				travelled.add(tempcoord);
				if (parcels.contains(tempcoord)) {
					parcels.remove(tempcoord);
				}
			}
		}

		/**
		 * Check if you have a wall in front of you!
		 * @param orientation the orientation we are in based on WorldSpatial
		 * @param currentView what the car can currently see
		 * @return
		 */
		private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
			switch(orientation){
			case EAST:
				return checkEast(currentView);
			case NORTH:
				return checkNorth(currentView);
			case SOUTH:
				return checkSouth(currentView);
			case WEST:
				return checkWest(currentView);
			default:
				return false;
			}
		}
		
		private boolean checkWallRight(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
			switch(orientation){
			case EAST:
				return checkSouth(currentView);
			case NORTH:
				return checkEast(currentView);
			case SOUTH:
				return checkWest(currentView);
			case WEST:
				return checkNorth(currentView);
			default:
				return false;
			}
		}
		
		private boolean checkWallLeft(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
			switch(orientation){
			case EAST:
				return checkNorth(currentView);
			case NORTH:
				return checkWest(currentView);
			case SOUTH:
				return checkEast(currentView);
			case WEST:
				return checkSouth(currentView);
			default:
				return false;
			}
		}
		/**
		 * Check if the wall is on your left hand side given your orientation
		 * @param orientation
		 * @param currentView
		 * @return
		 */
		private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
			
			switch(orientation){
			case EAST:
				return checkNorth(currentView);
			case NORTH:
				return checkWest(currentView);
			case SOUTH:
				return checkEast(currentView);
			case WEST:
				return checkSouth(currentView);
			default:
				return false;
			}	
		}
		
		/**
		 * Method below just iterates through the list and check in the correct coordinates.
		 * i.e. Given your current position is 10,10
		 * checkEast will check up to wallSensitivity amount of tiles to the right.
		 * checkWest will check up to wallSensitivity amount of tiles to the left.
		 * checkNorth will check up to wallSensitivity amount of tiles to the top.
		 * checkSouth will check up to wallSensitivity amount of tiles below.
		 */
		public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
			// Check tiles to my right
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}
		
		public boolean isLava(MapTile tile) {
			if (tile.isType(MapTile.Type.TRAP)) {
				TrapTile tile2 = (TrapTile) tile;
				if (tile2.getTrap().equals("lava")) {
					return true;
				}
			}
			return false;
		}
		
		public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
			// Check tiles to my left
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}
		
		public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
			// Check tiles to towards the top
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}
		
		public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
			// Check tiles towards the bottom
			Coordinate currentPosition = new Coordinate(getPosition());
			for(int i = 0; i <= wallSensitivity; i++){
				MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
				if(tile.isType(MapTile.Type.WALL)){
					return true;
				}
			}
			return false;
		}		
	}
