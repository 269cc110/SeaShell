package net.condorcraft110.seashell;

import java.io.*;

public class PluginDescriptor
{
	public final File file;
	public final String filename;
	public final String pluginID;
	public final String pluginVersion;
	public final String pluginAuthor;
	public final PluginClassLoader loader;
	
	public PluginDescriptor(File file, String filename, String pluginID, String pluginVersion, String pluginAuthor, PluginClassLoader loader)
	{
		this.file = file;
		this.filename = filename;
		this.pluginID = pluginID;
		this.pluginVersion = pluginVersion;
		this.pluginAuthor = pluginAuthor;
		this.loader = loader;
	}
}
