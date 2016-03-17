package clique.verticles;

import clique.base.EventHandler;

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
