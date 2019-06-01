package mycontroller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;

import tiles.MapTile;
import utilities.Coordinate;
import world.World;

public class FuelStrategy implements CarStrategy{

	@Override
	public void update(MyAutoController car) {
		// TODO Auto-generated method stub
		// parents is a map of parent nodes to child nodes
		HashMap<Coordinate, Coordinate> parents = new HashMap<Coordinate, Coordinate>();

		Coordinate exit = car.getExit().get(0);
		
		ArrayList<Coordinate> travelled = car.getTravelled();
		// order is used for coordinate order we have to traverse in order to be fuel efficient
		LinkedList<Coordinate> order = new LinkedList<Coordinate>();
		// distance is just a guide for order
		ArrayList<Integer> distance = new ArrayList<Integer>();
		// calculate distance from parcels to exit
		LinkedList<Coordinate> path = new LinkedList<Coordinate>();
		int[] d = new int[4];
		for (int i = 0; i < car.getParcels().size(); i++) {
			d[i] = bfs(car.getParcels().get(i), exit, parents, path);
		}
		// sort the parcels in order
		if (car.getParcels().size() > 0) {
			order.add(car.getParcels().get(0));
			distance.add(d[0]);
			for (int k = 1; k < car.getParcels().size(); k++) {
				int m = 0;
				while (d[k] > d[m]) {
					m++;
				}
				if (!travelled.contains(car.getParcels().get(k))) {
					order.add(m, car.getParcels().get(k));
					distance.add(m, d[k]);
				}
			}
		}
		
		
		// This is just a trial run to get from one place to another
		// Not yet implemented the remaining parts of going through all the parcels then the exit
		// bfs through car to first parcel so that we can get a node of parents along that path
		Coordinate carPos = new Coordinate(car.getPosition());
		if (car.numParcelsFound() >= car.numParcels()) {
			path = new LinkedList<Coordinate>();
			bfs(carPos, exit, parents, path);
			go(car, exit, path);
		} else {
			path = new LinkedList<Coordinate>();
			bfs(carPos, order.get(0), parents, path);
			// execute movement along that path
			go(car, order.get(0), path);
		}
	}
	
	public void go(MyAutoController car, Coordinate dest, LinkedList<Coordinate> path) {
		Coordinate currentPos = new Coordinate(car.getPosition());
		//Coordinate nextPos = parents.get(currentPos);
		Coordinate nextPos = path.getLast();
		System.out.printf("nextpos is %s currentPos is %s\n", nextPos.toString(), currentPos.toString());
		System.out.printf("Orientation is %s", car.getOrientation());
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
	
	
	public int bfs(Coordinate start, Coordinate end, HashMap<Coordinate, Coordinate> parents, LinkedList<Coordinate> path) {
		HashMap<Coordinate, Integer> cost = new HashMap<Coordinate, Integer>();
		cost.put(start, 0);
		HashMap<Coordinate, MapTile> map = World.getMap();
		LinkedList<Coordinate> visited = new LinkedList<Coordinate>();
		LinkedList<Coordinate> queue = new LinkedList<Coordinate>();
		queue.add(start);
		visited.add(start);
		while (!queue.isEmpty()) {
			Coordinate temp = queue.poll();
			int tempCost = cost.get(temp);
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
					parents.put(next, temp);
					queue.add(next);
					visited.add(next);
					cost.put(next, tempCost+1);
					if (next.x == end.x && next.y == end.y) {
						break;
					}
				}
			}
		}
		int tileDistance;
		if (cost.containsKey(end)) {
			tileDistance = cost.get(end);
		} else {
			tileDistance = 0;
		}
		
		Coordinate main = new Coordinate(end.toString());
		path.add(end);
		while (!(main.x == start.x && main.y == start.y)) {
			Coordinate temp2 = parents.get(main);
			if (temp2 != null) {
				if (!(temp2.x == start.x && temp2.y == start.y)) {
					path.add(temp2);
				}
				main.x = temp2.x;
				main.y = temp2.y;
			}
		}
		
		
		return tileDistance;
	}


}
