package clique.verticles;

import clique.base.EventHandler;

public class EventMaybesHandler extends EventHandler {
	@Override
	public String getHandlerName() {
		return "eventMaybes";
	}

	@Override
	public String getField() {
		return "maybe";
	}
}
