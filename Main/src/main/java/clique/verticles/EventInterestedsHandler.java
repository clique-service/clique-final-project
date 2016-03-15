package clique.verticles;

public class EventInterestedsHandler extends EventHandler {

	@Override
	public String getHandlerName() {
		return "eventInteresteds";
	}

	@Override
	public String getField() {
		return "interested";
	}
}
