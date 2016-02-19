package clique.helpers;

public class FinishChecker {
	private boolean likes = false;
	private boolean events = false;
	private boolean taggedPlaces = false;

	public boolean isLikes() {
		return likes;
	}

	public void setLikes(boolean likes) {
		this.likes = likes;
	}

	public boolean isEvents() {
		return events;
	}
	
	public void setEvents(boolean events) {
		this.events = events;
	}

	public boolean isTaggedPlaces() {
		return taggedPlaces;
	}

	public void setTaggedPlaces(boolean taggedPlaces) {
		this.taggedPlaces = taggedPlaces;
	}

	public boolean isAllDone() {
		return isLikes() && isEvents() && isTaggedPlaces();
	}
}
