package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import controller.CarController;
import exceptions.UnsupportedModeException;
import swen30006.driving.Simulation;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

public class MyAutoController extends CarController {

	private enum MOVEMENT {
		FORWARD, REVERSE, STATIONARY
	};

	private Coordinate internalPosition;
	private WorldSpatial.Direction internalOrientation;
	private MOVEMENT internalMovement;
	private ArrayList<String> movesToNextGoal;
	private ArrayList<String> bestRoute;
	private boolean movingToGoal = false;

	public MyAutoController(Car car) {
		super(car);
		internalPosition = new Coordinate(getPosition());
		internalOrientation = getOrientation();
		internalMovement = MOVEMENT.STATIONARY;
		setNonWallTiles();
	}

	// Coordinate initialGuess;
	// boolean notSouth = true;
	int count = 0;
	@Override
	public void update() {
		//System.out.format("%s     %s\n",getPosition(),getOrientation());
		//System.out.format("%s     %s      %s\n",internalPosition,internalOrientation, internalMovement);
		//System.out.println(getPossibleMoves());
		if (movingToGoal == false) {
			veryStupidMCTS();
			movingToGoal = true;
			System.out.println(bestRoute.size());
		}
		if (movingToGoal==true) {
			makeGeneratedMove(bestRoute.get(count));
			if (count == bestRoute.size()-1) {
				movingToGoal = false;
				count = 0;
			}
		}
		// Lets get all of this logic under control
		/*
		if (count==0) {
			applyReverseAcceleration();
			makeMoveInternally("reverse");
			System.out.println("reverse");
		}
		if (count==1) {
			turnRight();
			makeMoveInternally("right");
			System.out.println("right");
		}
		if (count==2) {
			turnRight();
			makeMoveInternally("right");
			System.out.println("right");
		}
		if (count==3) {
			makeMoveInternally("nothing");
			System.out.println("nothing");
		}
		if (count==4) {
			makeMoveInternally("nothing");
			System.out.println("nothing");
		}
		if (count==5) {
			makeMoveInternally("nothing");
			System.out.println("nothing");
		}
		if (count==6) {
			makeMoveInternally("nothing");
			System.out.println("nothing");
		}
		if (count==7) {
			turnRight();
			makeMoveInternally("right");
			System.out.println("right");
		}*/
			
			
			/*
		}
		if (noWallTileAt(0,1)) {
			System.out.println("no wall up");
			for (Map.Entry<Coordinate, MapTile> tile : getMap().entrySet()) {
				if (tile.getKey().x==internalPosition.x&& tile.getKey().y==internalPosition.y+1) {
					System.out.println(tile.getValue().getType());
				}
			}
		}
		if (noWallTileAt(0,-1)) {
			System.out.println("no wall down");
			for (Map.Entry<Coordinate, MapTile> tile : getMap().entrySet()) {
				if (tile.getKey().x==internalPosition.x&& tile.getKey().y==internalPosition.y-1) {
					System.out.println(tile.getKey());
					System.out.println(tile.getValue().getType());
				}
			}
		}
		if (noWallTileAt(1,0)) {
			System.out.println("no wall right");
			for (Map.Entry<Coordinate, MapTile> tile : getMap().entrySet()) {
				if (tile.getKey().x==internalPosition.x+1 && tile.getKey().y==internalPosition.y) {
					System.out.println(tile.getKey());
					System.out.println(tile.getValue().getType());
				}
			}
		}
		if (noWallTileAt(-1,0)) {
			System.out.println("no wall left");
			for (Map.Entry<Coordinate, MapTile> tile : getMap().entrySet()) {
				if (tile.getKey().x==internalPosition.x-1 && tile.getKey().y==internalPosition.y) {
					System.out.println(tile.getKey());
					System.out.println(tile.getValue().getType());
				}
			}
		}*/
		
		count++;
		/*
		if (movingToGoal == false) {
			veryStupidMCTS();
			movingToGoal = true;
			System.out.println(bestRoute.size());
		}
		if (movingToGoal == true) {
			String nextMove = bestRoute.get(0);
			if (nextMove.equals("forward")) {
				applyForwardAcceleration();
			} else if (nextMove.equals("reverse")) {
				applyReverseAcceleration();
			} else if (nextMove.equals("left")) {
				turnLeft();
			} else if (nextMove.equals("right")) {
				turnRight();
			}
			bestRoute.remove(0);
			System.out.println(nextMove);
		}
		if (bestRoute.size() == 0) {
			movingToGoal = false;
		}*/
	}

	public HashMap<Coordinate, MapTile> getAdjacentTiles() {
		HashMap<Coordinate, MapTile> adjacentTiles = new HashMap<Coordinate, MapTile>();
		for (Map.Entry<Coordinate, MapTile> tile : getMap().entrySet()) {
			if ((internalPosition.x == tile.getKey().x && Math.abs(internalPosition.y - tile.getKey().y) == 1)
					|| (internalPosition.y == tile.getKey().y && Math.abs(internalPosition.x - tile.getKey().x) == 1)) {
				adjacentTiles.put(tile.getKey(), tile.getValue());
			}
		}
		return adjacentTiles;
	}

	HashMap<Coordinate, MapTile> nonWallTiles;

	public void setNonWallTiles() {
		nonWallTiles = new HashMap<Coordinate, MapTile>();
		for (Map.Entry<Coordinate, MapTile> tile : getMap().entrySet()) {
			if (!tile.getValue().isType(MapTile.Type.WALL)) {
				nonWallTiles.put(tile.getKey(), tile.getValue());
			}
		}
	}

	public boolean noWallTileAt(int x, int y) {
		for (Coordinate coord : nonWallTiles.keySet()) {
			if ((coord.x == internalPosition.x + x) && (coord.y == internalPosition.y + y)) {
				return true;
			}
		}
		return false;
	}

	// returns possible moves based on tileType, orientation, direction of movement
	public ArrayList<String> getPossibleMoves() {
		ArrayList<String> possibleMoves = new ArrayList<String>();

		if (internalMovement == MOVEMENT.STATIONARY) {
			if (internalOrientation == WorldSpatial.Direction.NORTH) {
				if (noWallTileAt(0, 1)) {possibleMoves.add("forward");}
				if (noWallTileAt(0, -1)) {possibleMoves.add("reverse");}
			} else if (internalOrientation == WorldSpatial.Direction.SOUTH) {
				if (noWallTileAt(0, -1)) {possibleMoves.add("forward");}
				if (noWallTileAt(0, 1)) {possibleMoves.add("reverse");}
			} else if (internalOrientation == WorldSpatial.Direction.EAST) {
				if (noWallTileAt(1, 0)) {possibleMoves.add("forward");}
				if (noWallTileAt(-1, 0)) {possibleMoves.add("reverse");}
			} else if (internalOrientation == WorldSpatial.Direction.WEST) {
				if (noWallTileAt(-1, 0)) {possibleMoves.add("forward");}
				if (noWallTileAt(1, 0)) {possibleMoves.add("reverse");}
			}
		} else if (internalMovement == MOVEMENT.FORWARD) {
			possibleMoves.add("reverse");
			if (internalOrientation == WorldSpatial.Direction.NORTH) {
				if (noWallTileAt(0, 1) && noWallTileAt(0,2)) {possibleMoves.add("nothing");}
				if (noWallTileAt(-1, 0)) {possibleMoves.add("left");}
				if (noWallTileAt(1, 0)) {possibleMoves.add("right");}
			} else if (internalOrientation == WorldSpatial.Direction.SOUTH) {
				if (noWallTileAt(0, -1) && noWallTileAt(0,-2)) {possibleMoves.add("nothing");}
				if (noWallTileAt(1, 0)) {possibleMoves.add("left");}
				if (noWallTileAt(-1, 0)) {possibleMoves.add("right");}
			} else if (internalOrientation == WorldSpatial.Direction.EAST) {
				if (noWallTileAt(1, 0) && noWallTileAt(2,0)) {possibleMoves.add("nothing");}
				if (noWallTileAt(0, 1)) {possibleMoves.add("left");}
				if (noWallTileAt(0, -1)) {possibleMoves.add("right");}
			} else if (internalOrientation == WorldSpatial.Direction.WEST) {
				if (noWallTileAt(-1, 0) && noWallTileAt(-2,0)) {possibleMoves.add("nothing");}
				if (noWallTileAt(0, -1)) {possibleMoves.add("left");}
				if (noWallTileAt(0, 1)) {possibleMoves.add("right");}
			}
		} else if (internalMovement == MOVEMENT.REVERSE) {
			possibleMoves.add("forward");
			if (internalOrientation == WorldSpatial.Direction.NORTH) {
				if (noWallTileAt(0, -1) && noWallTileAt(0, -2)) {possibleMoves.add("nothing");}
				if (noWallTileAt(-1, 0)) {possibleMoves.add("left");}
				if (noWallTileAt(1, 0)) {possibleMoves.add("right");}
			} else if (internalOrientation == WorldSpatial.Direction.SOUTH) {
				if (noWallTileAt(0, 1) && noWallTileAt(0, 2)) {possibleMoves.add("nothing");}
				if (noWallTileAt(1, 0)) {possibleMoves.add("left");}
				if (noWallTileAt(-1, 0)) {possibleMoves.add("right");}
			} else if (internalOrientation == WorldSpatial.Direction.EAST) {
				if (noWallTileAt(-1, 0) && noWallTileAt(-2,0)) {possibleMoves.add("nothing");}
				if (noWallTileAt(0, 1)) {possibleMoves.add("left");}
				if (noWallTileAt(0, -1)) {possibleMoves.add("right");}
			} else if (internalOrientation == WorldSpatial.Direction.WEST) {
				if (noWallTileAt(1, 0) && noWallTileAt(2,0)) {possibleMoves.add("nothing");}
				if (noWallTileAt(0, -1)) {possibleMoves.add("left");}
				if (noWallTileAt(0, 1)) {possibleMoves.add("right");}
			}
		}
		return possibleMoves;
	}

	public void veryStupidMCTS() {
		
		Random random = new Random();
		String nextMove;
		ArrayList<String> possibleMoves;
		movesToNextGoal = new ArrayList<String>();
		int shortestPath = 999999;
		int count = 0;
		int minDamageTaken = 999999;
		MOVEMENT oldMovement = internalMovement;

		while (count < 10000) {
			internalPosition = new Coordinate(getPosition());
			internalOrientation = getOrientation();
			internalMovement = oldMovement;
			movesToNextGoal = new ArrayList<String>();
			int damageTaken = 0;
			p1NotFound = true;
			p2NotFound = true;
			p3NotFound = true;
			p4NotFound = true;
			while (!packageFound()) { // if a path gets really long just abandon it
				possibleMoves = getPossibleMoves();
				if (possibleMoves.contains("nothing")) {
					for (int i=0; i<4; i++) {
						possibleMoves.add("nothing");
					}
				}
				nextMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
				makeMoveInternally(nextMove);
				movesToNextGoal.add(nextMove);
			}
			if (damageTaken < minDamageTaken && movesToNextGoal.size() < shortestPath) {
				shortestPath = movesToNextGoal.size();
				minDamageTaken = damageTaken;
				bestRoute = movesToNextGoal;
			}
			count++;
		}
		internalPosition = new Coordinate(getPosition());
		internalOrientation = getOrientation();
		internalMovement = oldMovement;
		movingToGoal = true;

	}
	
	public void updateInternals(int x, int y) {
		internalPosition.x += x;
		internalPosition.y += y;
	}
	
	public void updateInternals(int x, int y, WorldSpatial.Direction direction) {
		internalPosition.x += x;
		internalPosition.y += y;
		internalOrientation = direction;
	}
	public void updateInternals(MOVEMENT movement) {
		internalMovement = movement;
	}
	boolean p1NotFound = true;
	boolean p2NotFound = true;
	boolean p3NotFound = true;
	boolean p4NotFound = true;
	private boolean packageFound() {
		if (internalPosition.x==19 && internalPosition.y==2 && p1NotFound) {
			p1NotFound = false;
			return true;
		}
		if (internalPosition.x==5 && internalPosition.y==15 && p2NotFound) {
			p2NotFound = false;
			return true;
		}
		if (internalPosition.x==16 && internalPosition.y==13 && p3NotFound) {
			p3NotFound = false;
			return true;
		}
		if (internalPosition.x==23 && internalPosition.y==15 && p4NotFound) {
			p4NotFound = false;
			return true;
		}
		
		return false;
	}
	
	public void makeMoveInternally(String move) {
		WorldSpatial.Direction NORTH = WorldSpatial.Direction.NORTH;
		WorldSpatial.Direction SOUTH = WorldSpatial.Direction.SOUTH;
		WorldSpatial.Direction EAST = WorldSpatial.Direction.EAST;
		WorldSpatial.Direction WEST = WorldSpatial.Direction.WEST;
		MOVEMENT FORWARD = MOVEMENT.FORWARD;
		MOVEMENT REVERSE = MOVEMENT.REVERSE;
		MOVEMENT STATIONARY = MOVEMENT.STATIONARY;
		
		if (internalMovement == STATIONARY) {
			if (internalOrientation == NORTH) {
				if 		(move.equals("forward")) {updateInternals(0,1); updateInternals(FORWARD);} 
				else if (move.equals("reverse")) {updateInternals(0,-1);updateInternals(REVERSE);} 
			} else if (internalOrientation == SOUTH) {
				if 		(move.equals("forward")) {updateInternals(0,-1);updateInternals(FORWARD);} 
				else if (move.equals("reverse")) {updateInternals(0,1);updateInternals(REVERSE);} 
			} else if (internalOrientation == EAST) {
				if 		(move.equals("forward")) {updateInternals(1,0);updateInternals(FORWARD);} 
				else if (move.equals("reverse")) {updateInternals(-1,0);updateInternals(REVERSE);} 
			} else if (internalOrientation == WEST) {
				if 		(move.equals("forward")) {updateInternals(-1,0);updateInternals(FORWARD);} 
				else if (move.equals("reverse")) {updateInternals(1,0);updateInternals(REVERSE);} 
			}
		} else if (internalMovement == FORWARD) {
			if (move.equals("reverse")) {
				updateInternals(STATIONARY);
			} else if (internalOrientation == NORTH) {
				if 		(move.equals("nothing")) {updateInternals(0,1,NORTH);} 
				else if (move.equals("left")) {updateInternals(-1,0,WEST);} 
				else if (move.equals("right")) {updateInternals(1,0,EAST);}
			} else if (internalOrientation == SOUTH) {
				if 		(move.equals("nothing")) {updateInternals(0,-1,SOUTH);} 
				else if (move.equals("left")) {updateInternals(1,0,EAST);} 
				else if (move.equals("right")) {updateInternals(-1,0,WEST);}
			} else if (internalOrientation == EAST) {
				if 		(move.equals("nothing")) {updateInternals(1,0,EAST);} 
				else if (move.equals("left")) {updateInternals(0,1,NORTH);} 
				else if (move.equals("right")) {updateInternals(0,-1,SOUTH);}
			} else if (internalOrientation == WEST) {
				if 		(move.equals("nothing")) {updateInternals(-1,0,WEST);} 
				else if (move.equals("left")) {updateInternals(0,-1,SOUTH);} 
				else if (move.equals("right")) {updateInternals(0,1,NORTH);}
			}
		} else if (internalMovement == REVERSE) {
			if (move.equals("forward")) {
				updateInternals(STATIONARY);
			} else if (internalOrientation == NORTH) {
				if		(move.equals("nothing")) {updateInternals(0,-1,NORTH);} 
				else if (move.equals("left")) {updateInternals(-1,0,EAST);} 
				else if (move.equals("right")) {updateInternals(1,0,WEST);}
			} else if (internalOrientation == SOUTH) {
				if 		(move.equals("nothing")) {updateInternals(0,1,SOUTH);} 
				else if (move.equals("left")) {updateInternals(1,0,WEST);} 
				else if (move.equals("right")) {updateInternals(-1,0,EAST);}
			} else if (internalOrientation == EAST) {
				if 		(move.equals("nothing")) {updateInternals(-1,0,EAST);} 
				else if (move.equals("left")) {updateInternals(0,1,SOUTH);} 
				else if (move.equals("right")) {updateInternals(0,-1,NORTH);}
			} else if (internalOrientation == WEST) {
				if 		(move.equals("nothing")) {updateInternals(1,0,WEST);} 
				else if (move.equals("left")) {updateInternals(0,-1,NORTH);} 
				else if (move.equals("right")) {updateInternals(0,1,SOUTH);}
			}
		}
	}
	
	public void makeGeneratedMove(String move) {
		if (move.equals("forward")) {
			applyForwardAcceleration();
		} else if (move.equals("reverse")) {
			applyReverseAcceleration();
		} else if (move.equals("left")) {
			turnLeft();
		} else if (move.equals("right")) {
			turnRight();
		}
		
		makeMoveInternally(move);
		System.out.println(move);
	}
}
