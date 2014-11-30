package net.condorcraft110.seashell.util.config;

import java.io.*;
import java.util.*;

import net.condorcraft110.seashell.SSUtil;
import net.condorcraft110.seashell.SeaShell;

/**
 * Re-implementation of {@link java.util.Properties Properties} backed by a {@link java.util.TreeMap TreeMap}
 * @author condorcraft110
 */
public class Config
{
	protected Map<String, String> configMap;
	protected boolean loaded;
	protected boolean lazy;
	protected File file;
	protected boolean hasChanged;
	
	public Config(String filename)
	{
		this(filename, false);
	}
	
	public Config(File file)
	{
		this(file, false);
	}
	
	public Config(String filename, boolean lazy)
	{
		this(SSUtil.attemptCanonical(new File(filename)), lazy);
	}
	
	public Config(File file, boolean lazy)
	{
		if(file == null) throw new IllegalArgumentException("Config file must not be null");
		if(file.isDirectory()) throw new IllegalArgumentException("Config file must not be a directory");
		if(!lazy) load(file);
		this.file = file;
	}
	
	protected Config()
	{
		// for subclasses not based on files
	}
	
	protected void load(File file)
	{
		if(loaded || file == null) return;
		
		TreeMap<String, String> map = new TreeMap<>();
		
		try(Scanner scanner = new Scanner(new FileInputStream(file)))
		{
			while(scanner.hasNextLine())
			{
				String line = scanner.nextLine();
				if(line.trim().startsWith("#") || line.trim().equals("")) continue;
				String[] lineSplit = line.split("=");
				String key = lineSplit[0];
				String value = SSUtil.concatenate(SSUtil.copyFrom(lineSplit, 1), "=");
				map.put(key, value);
			}
		}
		catch(FileNotFoundException e)
		{
			// nom nom nom
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
		
		//SSUtil.printMap(map);
		
		configMap = map;
		
		loaded = true;
	}
	
	public String get(String key)
	{
		return get(key, "");
	}
	
	public void set(String key, String value)
	{
		set(key, value, true);
	}
	
	public void set(String key, String value, boolean overwrite)
	{
		if(overwrite || !configMap.containsKey(key))
		{
			configMap.put(key, value);
			
			hasChanged = true;
		}
	}
	
	public String get(String key, String default_)
	{
		if(lazy)
		{
			load(file);
		}
		
		return configMap.containsKey(key) ? configMap.get(key) : default_;
	}
	
	public boolean containsKey(String key)
	{
		return configMap.containsKey(key);
	}
	
	public boolean containsValue(String value)
	{
		return configMap.containsValue(value);
	}
	
	public void save(boolean onlyIfChanged) // TODO doesn't preserve comments
	{
		if(!onlyIfChanged || hasChanged)
		{
			try(PrintStream stream = new PrintStream(new FileOutputStream(file)))
			{
				SSUtil.printMap(stream, configMap);
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
	}
	
	public int getInt(String key, int default_, boolean setDefault)
	{
		if(!configMap.containsKey(key))
		{
			if(setDefault) configMap.put(key, "" + default_);
			return default_;
		}
		
		try
		{
			return Integer.parseInt(configMap.get(key));
		}
		catch(NumberFormatException e)
		{
			return default_;
		}
	}
	
	public double getDouble(String key, double default_, boolean setDefault)
	{
		if(!configMap.containsKey(key))
		{
			if(setDefault) configMap.put(key, "" + default_);
			return default_;
		}
		
		try
		{
			return Double.parseDouble(configMap.get(key));
		}
		catch(NumberFormatException e)
		{
			return default_;
		}
	}
	
	public boolean getBoolean(String key, boolean default_, boolean setDefault)
	{
		if(!configMap.containsKey(key))
		{
			if(setDefault) configMap.put(key, "" + default_);
			return default_;
		}
		
		return Boolean.parseBoolean(configMap.get(key));
	}
	
	public float getFloat(String key, float default_, boolean setDefault)
	{
		if(!configMap.containsKey(key))
		{
			if(setDefault) configMap.put(key, "" + default_);
			return default_;
		}
		
		try
		{
			return Float.parseFloat(configMap.get(key));
		}
		catch(NumberFormatException e)
		{
			return default_;
		}
	}
	
	public long getLong(String key, long default_, boolean setDefault)
	{
		if(!configMap.containsKey(key))
		{
			if(setDefault) configMap.put(key, "" + default_);
			return default_;
		}
		
		try
		{
			return Long.parseLong(configMap.get(key));
		}
		catch(NumberFormatException e)
		{
			return default_;
		}
	}
	
	public short getShort(String key, short default_, boolean setDefault)
	{
		if(!configMap.containsKey(key))
		{
			if(setDefault) configMap.put(key, "" + default_);
			return default_;
		}
		
		try
		{
			return Short.parseShort(configMap.get(key));
		}
		catch(NumberFormatException e)
		{
			return default_;
		}
	}
	
	public byte getByte(String key, byte default_, boolean setDefault)
	{
		if(!configMap.containsKey(key))
		{
			if(setDefault) configMap.put(key, "" + default_);
			return default_;
		}
		
		try
		{
			return Byte.parseByte(configMap.get(key));
		}
		catch(NumberFormatException e)
		{
			return default_;
		}
	}
	
	public int getInt(String key, int default_)
	{
		return getInt(key, default_, true);
	}
	
	public double getDouble(String key, double default_)
	{
		return getDouble(key, default_, true);
	}
	
	public boolean getBoolean(String key, boolean default_)
	{
		return getBoolean(key, default_, true);
	}
	
	public float getFloat(String key, float default_)
	{
		return getFloat(key, default_, true);
	}
	
	public long getLong(String key, long default_)
	{
		return getLong(key, default_, true);
	}
	
	public short getShort(String key, short default_)
	{
		return getShort(key, default_, true);
	}
	
	public byte getByte(String key, byte default_)
	{
		return getByte(key, default_, true);
	}
}
