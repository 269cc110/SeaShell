package net.condorcraft110.seashell;

import java.net.*;

public class PluginClassLoader extends URLClassLoader
{
	public PluginClassLoader(URL dest)
	{
		super(new URL[]{dest});
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		return loadClass(name, true);
	}
}
