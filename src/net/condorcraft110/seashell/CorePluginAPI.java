package net.condorcraft110.seashell;

import java.util.*;

public class CorePluginAPI
{
	private static final HashMap<String, Class<? extends CorePlugin>> registry = new HashMap<>();
	
	public static void registerAPI(Class<? extends CorePlugin> clazz)
	{
		registry.put(clazz.getName(), clazz);
	}
	
	public static CorePlugin getAPIInstance(String className)
	{
		CorePlugin cp = null;
		try
		{
			Class<? extends CorePlugin> clazz = registry.get(className);
			cp = (CorePlugin)clazz.getDeclaredMethod("getInstance").invoke(null);
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
		return cp;
	}
	
	public static Class<? extends CorePlugin> getAPIClass(String className)
	{
		return registry.get(className);
	}
}
