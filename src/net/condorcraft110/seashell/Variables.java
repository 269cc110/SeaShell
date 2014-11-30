package net.condorcraft110.seashell;

import java.io.*;
import java.util.*;

public class Variables
{
	private static final HashMap<String, Variable> variables = new HashMap<>();
	private static final HashMap<String, Variable> aliases = new HashMap<>();
	
	public static void setVar(String name, String value, boolean persistent)
	{
		Variable var = new Variable();
		var.value = value;
		var.persistent = persistent;
		variables.put(name, var);
	}
	
	public static void delVar(String name)
	{
		variables.remove(name);
	}
	
	public static String format(String s)
	{
		String result = s;
		for(String var : variables.keySet())
		{
			result = result.replace("%" + var, variables.get(var).value);
		}
		return result;
	}
	
	static void setAlias(String value, String name, boolean persistent)
	{
		Variable var = new Variable();
		var.value = value;
		var.persistent = persistent;
		aliases.put(name, var);
	}
	
	static String fromAlias(String command)
	{
		return aliases.containsKey(command) ? aliases.get(command).value : "";
	}
	
	static void init()
	{
		try
		{
			if(SeaShell.isInitialised()) return;
			
			File file = new File("persistence.dat");
			if(!file.exists())
			{
				file.createNewFile();
				return;
			}
			
			DataInputStream fileStream = SSUtil.getIS(file);
			ArrayList<Byte> variablesAL = new ArrayList<Byte>();
			ArrayList<Byte> aliasesAL = new ArrayList<Byte>();
			byte[] variablesBA;
			byte[] aliasesBA;
			int i = fileStream.read();
			while(i != -1 && (byte)i != Byte.MAX_VALUE) //
			{
				variablesAL.add((byte)i);
				i = fileStream.read();
			}
			i = fileStream.read();
			while(i != -1)
			{
				aliasesAL.add((byte)i);
				i = fileStream.read();
			}
			fileStream.close();
			variablesBA = SSUtil.toPrimitiveByteArray(variablesAL.toArray(new Byte[variablesAL.size() - 1]));
			aliasesBA = SSUtil.toPrimitiveByteArray(aliasesAL.toArray(new Byte[variablesAL.size() - 1]));
			ByteArrayInputStream variablesBAIS = new ByteArrayInputStream(variablesBA);
			ByteArrayInputStream aliasesBAIS = new ByteArrayInputStream(aliasesBA);

			Properties persistentVariables = new Properties();
			persistentVariables.load(variablesBAIS);
			for(String s : persistentVariables.stringPropertyNames())
			{
				Variable var = new Variable();
				var.value = persistentVariables.getProperty(s);
				var.persistent = true;
				variables.put(s, var);
			}

			Properties persistentAliases = new Properties();
			persistentAliases.load(aliasesBAIS);
			for(String s : persistentAliases.stringPropertyNames())
			{
				Variable var = new Variable();
				var.value = persistentAliases.getProperty(s);
				var.persistent = true;
				aliases.put(s, var);
			}
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println("Error reading persistents: " + e.toString());
			return;
		}
	}
	
	static void exit()
	{
		if(!SeaShell.isInitialised()) return;
		try
		{
			File file = new File("persistence.dat");
			if(!file.exists())
			{
				file.createNewFile();
			}
			
			DataOutputStream fileStream = SSUtil.getOS(file);
			
			Properties persistentVariables = new Properties();
			for(String s : variables.keySet())
			{
				Variable var = variables.get(s);
				if(var.persistent) persistentVariables.setProperty(s, var.value);
			}
			persistentVariables.store(fileStream, "Persistent variables for SeaShell");
			
			fileStream.write((int)Byte.MAX_VALUE);
			fileStream.write((int)'\n');
			
			Properties persistentAliases = new Properties();
			for(String s : aliases.keySet())
			{
				Variable var = aliases.get(s);
				if(var.persistent) persistentAliases.setProperty(s, var.value);
			}
			persistentAliases.store(fileStream, "Persistent aliases for SeaShell");
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println("Error saving persistents: " + e.toString());
			return;
		}
	}
}
