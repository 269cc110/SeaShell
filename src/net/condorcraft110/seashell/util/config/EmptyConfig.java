package net.condorcraft110.seashell.util.config;

import java.io.*;
import java.util.*;

/**
 * {@link Config} that does not load its contents from a file.
 * @see Config
 * @author condorcraft110
 */
public class EmptyConfig extends Config
{
	public EmptyConfig()
	{
		super();
	}
	
	protected void load(File file)
	{
		configMap = new TreeMap<>();
	}
}
