package clique.verticles;

import clique.base.EventHandler;

public class EventAttendeesHandler extends EventHandler {

	@Override
	public String getHandlerName() {
		return "eventAttendees";
	}

	@Override
	public String getField() {
		return "attending";
	}
}
