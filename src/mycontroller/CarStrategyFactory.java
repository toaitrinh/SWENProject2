package mycontroller;

public class CarStrategyFactory {
	private static CarStrategyFactory instance = null;
	
	public static CarStrategyFactory getInstance() {
		if (instance == null) {
			instance = new CarStrategyFactory();
		}
		return instance;
	}
	
	public CarStrategy getStrategy(MyAutoController controller) {
		switch(controller.getMode()) {
		case FUEL:
			return new FuelStrategy();
		default:
			return new HealthStrategy();
		}
	}
}
