package net.condorcraft110.seashell;

import java.util.*;

public class Constants
{
	private static HashMap<String, Variable> constants = new HashMap<>();
	private static ArrayList<String> systemConstants = new ArrayList<String>();
	
	static void init()
	{
		addConst("CDIR", SSUtil.getPath(SeaShell.currentDir));
		systemConstants.add("CDIR");
		addConst("SSVER", SeaShell.SS_VERSION);
		systemConstants.add("SSVER");
	}
	
	static void addConst(String name, String value)
	{
		Variable var = new Variable();
		var.value = value;
		var.persistent = false;
		constants.put(name, var);
	}
	
	static void removeConst(String name)
	{
		constants.remove(name);
	}
	
	public static boolean isSystemConstant(String name)
	{
		return systemConstants.contains(name);
	}
	
	public static boolean constantExists(String name)
	{
		return constants.containsKey(name);
	}
	
	static void clear()
	{
		constants.clear();
	}
	
	public static String format(String s)
	{
		String result = s;
		for(String var : constants.keySet())
		{
			result = result.replace("$" + var, constants.get(var).value);
		}
		return result;
	}
}
