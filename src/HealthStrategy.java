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
		// TODO Auto-generated method stub
		// parents is a map of parent nodes to child nodes
		HashMap<Coordinate, Coordinate> parents = new HashMap<Coordinate, Coordinate>();
		// hardcoded parcels in
		//Coordinate[] parcels = new Coordinate[4];
		//parcels[0] = new Coordinate("5,15");
		//parcels[1] = new Coordinate("19,2");
	    //parcels[2] = new Coordinate("16,13");
		//parcels[3] = new Coordinate("23,15");
		//Coordinate exit = new Coordinate("23,16");
		Coordinate exit = car.getExit().get(0);
		
		ArrayList<Coordinate> travelled = car.getTravelled();
		// order is used for coordinate order we have to traverse in order to be fuel efficient
		LinkedList<Coordinate> order = new LinkedList<Coordinate>();
		if (car.getParcels().size() > 0) {
			order.add(car.getParcels().get(0));
			for (int k = 1; k < car.getParcels().size(); k++) {
				if (!travelled.contains(car.getParcels().get(k))) {
					order.add(car.getParcels().get(k));
				}
			}
		}
		// calculate distance from parcels to exit
		LinkedList<Coordinate> path = new LinkedList<Coordinate>();
		LinkedList<Coordinate> health = car.getHealthTiles();
		LinkedList<Coordinate> water = car.getWaterTiles();
		LinkedList<Coordinate> waterHealth = health;
		for (Coordinate wtiles: water) {
			waterHealth.add(wtiles);
		}
		// This is just a trial run to get from one place to another
		// Not yet implemented the remaining parts of going through all the parcels then the exit
		// bfs through car to first parcel so that we can get a node of parents along that path
		Coordinate carPos = new Coordinate(car.getPosition());
		if (car.numParcelsFound() >= car.numParcels()) {
			path = new LinkedList<Coordinate>();
			int lavaCount;
			try {
				lavaCount = bfs(car, carPos, exit, parents, path, false) * 5;
			} catch (Exception e) {
				System.out.println("CAUGHT");
				path = new LinkedList<Coordinate>();
				lavaCount = bfs(car, carPos, exit, parents, path, true) * 5;
				System.out.println("CAUGHT2");
			}
			System.out.printf("lavaCount is %d\n", lavaCount);
			if ((car.getHealth() - lavaCount) > 0) {
				go(car, exit, parents, path);
			} else {
				int healthNeeded = (int) (lavaCount - car.getHealth() + 5)/5;
				for (int i = 0; i < healthNeeded; i++) {
					Coordinate destHealth = null;
					int minLava = 500;
					for (Coordinate htile: waterHealth) {
						path = new LinkedList<Coordinate>();
						if (bfs(car, carPos, htile, parents, path, true) < minLava) {
							minLava = bfs(car, carPos, htile, parents, path, true);
							destHealth = new Coordinate(htile.toString());
						}
					}
					healthNeeded += minLava;
					go(car, destHealth, parents, path);
				}
				path = new LinkedList<Coordinate>();
				try {
					bfs(car, carPos, exit, parents, path, false);
				} catch (Exception e) {
					System.out.println("CAUGHT");
					path = new LinkedList<Coordinate>();
					bfs(car, carPos, exit, parents, path, true);
					System.out.println("CAUGHT2");
				}
				go(car, exit, parents, path);
			}
		} else {
			path = new LinkedList<Coordinate>();
			int lavaCount;
			try {
				lavaCount = bfs(car, carPos, order.get(0), parents, path, false) * 5;
			} catch (Exception e) {
				System.out.println("CAUGHT");
				path = new LinkedList<Coordinate>();
				lavaCount = bfs(car, carPos, order.get(0), parents, path, true) * 5;
				System.out.println("CAUGHT2");
			}
			System.out.printf("lavaCount is %d\n", lavaCount);
			if ((car.getHealth() - lavaCount) > 0) {
				go(car, order.get(0), parents, path);
			} else {
				int healthNeeded = (int) (lavaCount - car.getHealth() + 5)/5;
				for (int i = 0; i < healthNeeded; i++) {
					Coordinate destHealth = null;
					int minLava = 500;
					for (Coordinate htile: waterHealth) {
						path = new LinkedList<Coordinate>();
						if (bfs(car, carPos, htile, parents, path, true) < minLava) {
							minLava = bfs(car, carPos, htile, parents, path, true);
							destHealth = new Coordinate(htile.toString());
						}
					}
					healthNeeded += minLava;
					go(car, destHealth, parents, path);
				}
				path = new LinkedList<Coordinate>();
				try {
					bfs(car, carPos, order.get(0), parents, path, false);
				} catch (Exception e) {
					System.out.println("CAUGHT");
					path = new LinkedList<Coordinate>();
					bfs(car, carPos, order.get(0), parents, path, true);
					System.out.println("CAUGHT2");
				}
				go(car, order.get(0), parents, path);
			}
			// execute movement along that path
			
		}
	}
	
	public void go(MyAutoController car, Coordinate dest, HashMap<Coordinate, Coordinate> parents, LinkedList<Coordinate> path) {
		Coordinate currentPos = new Coordinate(car.getPosition());
		//Coordinate nextPos = parents.get(currentPos);
		Coordinate nextPos = path.getLast();
		System.out.printf("nextpos is %s currentPos is %s\n", nextPos.toString(), currentPos.toString());
		System.out.printf("Orientation is %s\n", car.getOrientation());
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
	
	
	public int bfs(MyAutoController car, Coordinate start, Coordinate end, HashMap<Coordinate, Coordinate> parents, LinkedList<Coordinate> path, boolean forHealth) {
		//HashMap<Coordinate, Integer> cost = new HashMap<Coordinate, Integer>();
		//cost.put(start, 0);
		HashMap<Coordinate, MapTile> map = World.getMap();
		ArrayList<Coordinate> lava = car.getLava();
		LinkedList<Coordinate> health = car.getHealthTiles();
		LinkedList<Coordinate> water = car.getWaterTiles();
		LinkedList<Coordinate> waterHealth = health;
		for (Coordinate wtiles: water) {
			waterHealth.add(wtiles);
		}
		LinkedList<Coordinate> visited = new LinkedList<Coordinate>();
		LinkedList<Coordinate> queue = new LinkedList<Coordinate>();
		int lavaCount = 0;
		queue.add(start);
		visited.add(start);
		while (!queue.isEmpty()) {
			Coordinate temp = queue.poll();
			//int tempCost = cost.get(temp);
			for (int i = 0; i < 4; i++) {
				Coordinate next = new Coordinate(temp.toString());
				switch(i) {
				case 0:
					next.y--;
					break;
				case 1:
					next.x++;
					break;
				case 2:
					next.y++;
					break;
				case 3:
					next.x--;
					break;
				}
				if (!map.get(next).isType(MapTile.Type.WALL) && !visited.contains(next)) {
					boolean isHealth = false;
					for (Coordinate htile: waterHealth) {
						if (htile.equals(next)) {
							isHealth = true;
						}
					}
					if (!forHealth) {
						if (!isHealth) {
							parents.put(next, temp);
							queue.add(next);
							visited.add(next);
							//cost.put(next, tempCost+1);
							if (end.equals(next)) {
								break;
							}
						}
					} else {
						parents.put(next, temp);
						queue.add(next);
						visited.add(next);
						//cost.put(next, tempCost+1);
						if (end.equals(next)) {
							break;
						}
					}
				}
			}
		}
		//int tileDistance;
		//if (cost.containsKey(end)) {
		//	tileDistance = cost.get(end);
		//} else {
		//	tileDistance = 0;
		//}
		
		Coordinate main = new Coordinate(end.toString());
		path.add(end);
		while (!(main.equals(start))) {
			Coordinate temp2 = parents.get(main);
			if (!(temp2.equals(start))) {
				path.add(temp2);
				for (Coordinate ltile: lava) {
					if (ltile.equals(temp2)) {
						lavaCount++;
					}
				}
			}
			main.x = temp2.x;
			main.y = temp2.y;
		}
		
		System.out.printf("lavaCount is %d\n", lavaCount);
		//return tileDistance;
		return lavaCount;
	}
}

