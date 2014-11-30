package net.condorcraft110.seashell;

import java.util.*;

public class EventBus
{
	private static ArrayList<EventHandler> handlers = new ArrayList<>();
	
	static void fire(EventType type, Object... args)
	{
		for(EventHandler handler : handlers)
		{
			handler.handleEvent(type, args);
		}
	}
	
	public static void subscribe(EventHandler handler)
	{
		handlers.add(handler);
	}
}
