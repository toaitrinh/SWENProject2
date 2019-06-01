package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import tiles.MapTile;
import utilities.Coordinate;
import world.World;

public class HealthStrategy implements CarStrategy{

	@Override
	public void update(MyAutoController car) {
		// parents is a map of parent nodes to child nodes
		HashMap<Coordinate, Coordinate> parents = new HashMap<Coordinate, Coordinate>();
		Coordinate exit = car.getExit().get(0);
		// array-list of traveled coordinates
		ArrayList<Coordinate> travelled = car.getTravelled();
		// parcels is used for 
		LinkedList<Coordinate> parcels = new LinkedList<Coordinate>();
		// add parcels into parcels list to iterate through
		if (car.getParcels().size() > 0) {
			parcels.add(car.getParcels().get(0));
			for (int k = 1; k < car.getParcels().size(); k++) {
				if (!travelled.contains(car.getParcels().get(k))) {
					parcels.add(car.getParcels().get(k));
				}
			}
		}
		// calculate distance from parcels to exit
		LinkedList<Coordinate> path = new LinkedList<Coordinate>();
		LinkedList<Coordinate> waterHealth = car.getWaterHealthTiles();

		Coordinate carPos = new Coordinate(car.getPosition());
		Coordinate destination = null;
		// determine if we want to go to the exit or to the next parcel
		if  (car.numParcelsFound() >= car.numParcels()) {
			destination = new Coordinate(exit.toString());
		} else {
			destination = new Coordinate(parcels.get(0).toString());
		}
		
		path = new LinkedList<Coordinate>();
		// lavaCount used for checking how much health would be lost along the path
		int lavaCount;
		// at first, we try to go to the destination without going through any health usage
		try {
			lavaCount = bfs(car, carPos, destination, parents, path, false) * 5;
		// if it fails, it will throw a nullpointerexception, which is when we do the same
		// but going through the health usage
		} catch (NullPointerException e) {
			path = new LinkedList<Coordinate>();
			lavaCount = bfs(car, carPos, destination, parents, path, true) * 5;
		}
		// if the car remains alive throughout this path, simply go along the path
		if ((car.getHealth() - lavaCount) > 0) {
			go(car, destination, path);
		// if the car dies along this path, we determine how much health is needed and pick it up
		} else {
			int healthNeeded = (int) (lavaCount - car.getHealth() + 5)/5;
			for (int i = 0; i < healthNeeded; i++) {
				// iterate through the water and health tiles to find the path to a tile with the
				// least amount of lava on its path and go to it
				Coordinate destHealth = null;
				int minLava = 500;
				for (Coordinate htile: waterHealth) {
					path = new LinkedList<Coordinate>();
					if (bfs(car, carPos, htile, parents, path, true) < minLava) {
						minLava = bfs(car, carPos, htile, parents, path, true);
						destHealth = new Coordinate(htile.toString());
					}
				}
				// add lava on the path to health needed
				healthNeeded += minLava;
				go(car, destHealth, path);
			}
			// after getting the required health, proceed along the path to the destination
			path = new LinkedList<Coordinate>();
			try {
				bfs(car, carPos, destination, parents, path, false);
			} catch (NullPointerException e) {
				path = new LinkedList<Coordinate>();
				bfs(car, carPos, destination, parents, path, true);
			}
			go(car, destination, path);
		}
	}
	
	// function that executes movement of car when given path to drive along
	public void go(MyAutoController car, Coordinate dest, LinkedList<Coordinate> path) {
		Coordinate currentPos = new Coordinate(car.getPosition());
		Coordinate nextPos = path.getLast();
		// Go one tile right
		if (nextPos.x > currentPos.x) {
			switch(car.getOrientation()) {
			case EAST:
				car.applyForwardAcceleration();
				break;
			case WEST:
				car.applyReverseAcceleration();
				break;
			case SOUTH:
				car.turnLeft();
				car.applyForwardAcceleration();
				break;
			case NORTH:
				car.turnRight();
				car.applyForwardAcceleration();
				break;
			}
			currentPos.x = nextPos.x;
			// Go one tile Left
		} else if (nextPos.x < currentPos.x) {
			switch(car.getOrientation()) {
			case WEST:
				car.applyForwardAcceleration();
				break;
			case EAST:
				car.applyReverseAcceleration();
				break;
			case NORTH:
				car.turnLeft();
				car.applyForwardAcceleration();
				break;
			case SOUTH:
				car.turnRight();
				car.applyForwardAcceleration();
				break;
			}
			currentPos.x = nextPos.x;
		// Go one tile up
		} else if (nextPos.y > currentPos.y) {
			switch(car.getOrientation()) {
			case NORTH:
				car.applyForwardAcceleration();
				break;
			case SOUTH:
				car.applyReverseAcceleration();
				break;
			case EAST:
				car.turnLeft();
				car.applyForwardAcceleration();
				break;
			case WEST:
				car.turnRight();
				car.applyForwardAcceleration();
				break;
			}
			currentPos.y = nextPos.y;
		// Go one tile down
		} else if (nextPos.y < currentPos.y) {
			switch(car.getOrientation()) {
			case SOUTH:
				car.applyForwardAcceleration();
				break;
			case NORTH:
				car.applyReverseAcceleration();
				break;
			case WEST:
				car.turnLeft();
				car.applyForwardAcceleration();
				break;
			case EAST:
				car.turnRight();
				car.applyForwardAcceleration();
				break;
			}
			currentPos.y = nextPos.y;
		}	
	}	
	
	// breadth first search function
	public int bfs(MyAutoController car, Coordinate start, Coordinate end, HashMap<Coordinate, Coordinate> parents, LinkedList<Coordinate> path, boolean forHealth) {
		// initialise variables
		HashMap<Coordinate, MapTile> map = World.getMap();
		ArrayList<Coordinate> lava = car.getLava();
		LinkedList<Coordinate> waterHealth = car.getWaterHealthTiles();
		LinkedList<Coordinate> visited = new LinkedList<Coordinate>();
		LinkedList<Coordinate> queue = new LinkedList<Coordinate>();
		// lavaCount to be returned
		int lavaCount = 0;
		// add the starting location to lists
		queue.add(start);
		visited.add(start);
		// simple bfs search where we add more possible coordinates based on the last coordinate
		// until all possible coordinates are considered
		while (!queue.isEmpty()) {
			// take the next coordinate in the queue
			Coordinate root = queue.poll();
			// iterate through going up, left, right, down
			for (int i = 0; i < 4; i++) {
				Coordinate branch = new Coordinate(root.toString());
				switch(i) {
				case 0:
					branch.y--;
					break;
				case 1:
					branch.x++;
					break;
				case 2:
					branch.y++;
					break;
				case 3:
					branch.x--;
					break;
				}
				// check if either the next coordinate we are moving to is a wall or its been visited
				if (!map.get(branch).isType(MapTile.Type.WALL) && !visited.contains(branch)) {
					// check if the next coordinate is a health or water tile
					boolean isHealth = false;
					for (Coordinate htile: waterHealth) {
						if (htile.equals(branch)) {
							isHealth = true;
						}
					}
					// check to see if we are allowed to go through health/water tiles based on function input
					if (!forHealth) {
						// if we aren't allowed and the next tile isn't health/water...
						if (!isHealth) {
							// record the coordinate and where it came from, which connects coordinates along a path
							parents.put(branch, root);
							// add to the queue and visited
							queue.add(branch);
							visited.add(branch);
							// if we have reached our destination, break out of the loop
							if (end.equals(branch)) {
								break;
							}
						}
					// in the case that we are allowed to go through health/water tiles,
					// do the same as above (aren't allowed but not a health/water tile)
					} else {
						parents.put(branch, root);
						queue.add(branch);
						visited.add(branch);
						if (end.equals(branch)) {
							break;
						}
					}
				}
			}
		}
		
		// From the finish point, we traverse back along the path using the parents hashmap
		// in order to create a single path
		Coordinate node = new Coordinate(end.toString());
		path.add(end);
		// until we reach the start/carPosition...
		while (!(node.equals(start))) {
			// retrieve the coordinate's parent
			Coordinate source = parents.get(node);
			if (!(source.equals(start))) {
				path.add(source);
				// for any lava tiles along the path, we add to the lava count
				for (Coordinate ltile: lava) {
					if (ltile.equals(source)) {
						lavaCount++;
					}
				}
			}
			// next coordinate becomes the main coordinate
			node = new Coordinate(source.toString());
		}
		
		return lavaCount;
	}
}

