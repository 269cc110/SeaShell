package net.condorcraft110.seashell;

import java.io.*;

public class PluginInitEvent
{
	private String pluginName;
	
	PluginInitEvent(String pluginName)
	{
		this.pluginName = pluginName;
	}
	
	public File getPluginHomeDir()
	{
		File dir = new File("plugins", pluginName);
		if(!dir.exists()) dir.mkdirs();
		else if(dir.exists() && !dir.isDirectory()) throw new RuntimeException("Plugin home directory already exists, but is not a directory");
		return dir;
	}
}
