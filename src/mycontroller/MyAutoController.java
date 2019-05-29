package mycontroller;

import controller.CarController;
import swen30006.driving.Simulation;
import world.Car;
import world.WorldSpatial;

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
		private boolean exitFound = false;
		private String phase = "explore";
		
		private CarStrategy strategy; 
		
		private static Simulation.StrategyMode mode;
				
		public MyAutoController(Car car) {
			super(car);
			mode = Simulation.toConserve();
			strategy = CarStrategyFactory.getInstance().getStrategy(this);
		}
				
		public Simulation.StrategyMode getMode() {
			return mode;
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
							if (parcelsFound == numParcels() && exitFound == true) {
								phase = "search";
							}
						}
					} else if (value.getType() == MapTile.Type.FINISH) {
						exitFound = true;
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
					if(!checkFollowingWall(getOrientation(), map)) {
						turnLeft();
					} else {
						// If wall on left and wall straight ahead, turn right
						if(checkWallAhead(getOrientation(), map)) {
							turnRight();
						}
					}
				} else {
					// Start wall-following (with wall on left) as soon as we see a wall straight ahead
					if(checkWallAhead(getOrientation(), map)) {
						turnRight();
						isFollowingWall = true;
					}
				}
			} else {
				strategy.update();
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
		 
		
		
//		@Override
//		public void update() {
//			// Gets what the car can see
//			HashMap<Coordinate, MapTile> currentView = getView();
//			
//			strategy.update();
//			
//		}

		
		
	}
