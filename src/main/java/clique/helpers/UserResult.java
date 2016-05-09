package clique.helpers;

import java.util.Map;

public class UserResult implements Comparable<UserResult> {

	private String id;
	private String name;
	private Long rate;
	private Long events;
	private Long likes;
	private Long categories;
	private Long places;

	public UserResult(String id, String name, Long rate, Long events, Long likes, Long categories, Long places) {
		super();
		this.id = id;
		this.name = name;
		this.rate = rate;
		this.events = events;
		this.likes = likes;
		this.categories = categories;
		this.places = places;
	}

	public String getId() {
		return id;
	}

	public UserResult setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public UserResult setName(String name) {
		this.name = name;
		return this;
	}

	public Long getRate() {
		return rate;
	}

	public UserResult setRate(Long rate) {
		this.rate = rate;
		return this;
	}

	public Long getEvents() {
		return events;
	}

	public UserResult setEvents(Long events) {
		this.events = events;
		return this;
	}

	public Long getLikes() {
		return likes;
	}

	public UserResult setLikes(Long likes) {
		this.likes = likes;
		return this;
	}

	public Long getCategories() {
		return categories;
	}

	public UserResult setCategories(Long categories) {
		this.categories = categories;
		return this;
	}

	public Long getPlaces() {
		return places;
	}

	public UserResult setPlaces(Long places) {
		this.places = places;
		return this;
	}

	public static UserResult parse(Map<String, Object> user) {
		return new UserResult(user.get("id").toString(), user.get("name").toString(),
				Long.valueOf(user.get("rating").toString()), Long.valueOf(user.get("events").toString()),
				Long.valueOf(user.get("likes").toString()), Long.valueOf(user.get("categories").toString()),
				Long.valueOf(user.get("places").toString()));
	}

	@Override
	public int compareTo(UserResult o) {
		return Long.compare(this.rate, o.rate);
	}
}
