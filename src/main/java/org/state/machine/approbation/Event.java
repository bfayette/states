package org.state.machine.approbation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Event<T> implements Serializable {
	
	private static final long serialVersionUID = 3136734275963488125L;

	private String eventId;
	
	private T entity;
	
	private Map<String, Object> args = new HashMap<>();
	
	private Event() {
	}

	public Event(String eventId, T entity) {
		this();
		this.eventId = eventId;
		this.entity = entity;
	}

	@SuppressWarnings("unchecked")
	public <E> E  getArgument(String key) {
		return (E)args.get(key); 
		//to be implemented if needed. borrow from an internal lib
	}
	
	public void setArgument(String mkey, Object mvalue) {
		this.args.put(mkey, mvalue);
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}
	
	public T getEntity() {
		return entity;
	}
}