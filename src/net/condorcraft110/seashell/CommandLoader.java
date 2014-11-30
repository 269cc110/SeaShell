package net.condorcraft110.seashell;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.annotation.*;
import java.lang.reflect.*;

public class CommandLoader
{
	public static int loadedPlugins = 0;
	public static int errors = 0;
	
	protected static HashMap<String, PluginDescriptor> plugins = new HashMap<>();
	
	static void loadJarPlugin(File pluginFile)
	{
		int currentErrors = 0;
		
		Properties props = new Properties();
		PluginClassLoader loader = null;
		
		try
		{
			SSLogger.log("[CommandLoader] Attempting to load candidate plugin " + pluginFile.getName());
			EventBus.fire(EventType.ATTEMPT_PLUGIN_LOAD, 0, pluginFile);
			try
			{
				loader = new PluginClassLoader(new URL("jar:file:" + pluginFile.getAbsolutePath() + "!/"));
			}
			catch(MalformedURLException e)
			{
				SSLogger.errorLog("[CommandLoader] Error creating URL for class loader, ignoring plugin");
				currentErrors++;
				return;
			}
			InputStream stream = loader.getResourceAsStream("plugin.cfg");
			if(stream == null)
			{
				SSLogger.errorLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + " does not contain a plugin.cfg file, ignoring");
				try
				{
					loader.close();
				}
				catch(IOException e)
				{
					SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
					currentErrors++;
				}
				currentErrors++;
				return;
			}
			try
			{
				props.load(stream);
			}
			catch(IOException e)
			{
				SSLogger.errorLog("[CommandLoader] Error loading configuration for " + pluginFile.getName() + ", ignoring");
				try
				{
					loader.close();
				}
				catch(IOException e2)
				{
					SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
					currentErrors++;
				}
				currentErrors++;
				return;
			}
			if(props.containsKey("target-ss-version") && !props.getProperty("target-ss-version").equalsIgnoreCase(SeaShell.SS_VERSION)) SSLogger.warningLog("Candidate plugin " + pluginFile.getName() + " targets a different version of SeaShell, this may cause errors!");
			if(props.containsKey("target-api-version") && !props.getProperty("target-api-version").equalsIgnoreCase(SeaShell.API_VERSION)) SSLogger.warningLog("Candidate plugin " + pluginFile.getName() + " targets a different version of SeaShell's API, this may cause errors!");
			if(props.containsKey("plugin-class"))
			{
				String className = props.getProperty("plugin-class");
				Class<?> clazz = null;
				try
				{
					clazz = loader.loadClass(className);
				}
				catch(ClassNotFoundException e)
				{
					SSLogger.errorLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s plugin-class could not be found, ignoring");
					try
					{
						loader.close();
					}
					catch(IOException e2)
					{
						SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
						currentErrors++;
					}
					currentErrors++;
					return;
				}
				Field f = null;
				for(Field field : clazz.getDeclaredFields())
				{
					if(field.getAnnotation(PluginInstance.class) != null)
					{
						f = field;
						break;
					}
				}
				Object obj = null;
				if(f != null)
				{
					try
					{
						f.setAccessible(true);
						obj = f.get(null);
						if(obj == null)
						{
							SSLogger.warningLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @InitCommandInstance field is null, this may cause errors in loading");
						}
					}
					catch(IllegalAccessException e)
					{
						SSLogger.warningLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @InitCommandInstance field cannot be accessed, this may cause errors in loading");
					}
					catch(NullPointerException e)
					{
						SSLogger.warningLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @InitCommandInstance field is non-static so cannot be accessed, this may cause errors in loading");
					}
				}
				boolean flag = true;
				for(Method m : clazz.getDeclaredMethods())
				{
					Annotation annotation = m.getAnnotation(InitPlugin.class);
					if(annotation != null)
					{
						flag = false;
						m.setAccessible(true);
						try
						{
							m.invoke(obj, new PluginInitEvent(pluginFile.getName().substring(0, pluginFile.getName().length() - 4)));
						}
						catch(IllegalArgumentException e)
						{
							SSLogger.errorLog("[CommandLoader] " + pluginFile.getName() + "'s @InitCommand method does not accept a PluginInitEvent argument, attempting to call without arguments");
							try
							{
								m.invoke(obj);
							}
							catch(IllegalArgumentException e2)
							{
								SSLogger.errorLog("[CommandLoader] Candidate jar " + pluginFile.getName() + "'s @InitCommand method requires one or more arguments. Unable to invoke the method");
								try
								{
									loader.close();
								}
								catch(IOException e3)
								{
									SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
									currentErrors++;
								}
								currentErrors++;
								return;
							}
							catch(IllegalAccessException e2) //should never happen
							{
								SSLogger.errorLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @InitCommand method cannot be accessed. Unable to invoke the method");
								try
								{
									loader.close();
								}
								catch(IOException e3)
								{
									SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
									currentErrors++;
								}
								currentErrors++;
								return;
							}
							catch(InvocationTargetException e2)
							{
								SSLogger.errorLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @InitCommand method threw an exception");
								currentErrors++;
							}
						}
						catch(NullPointerException e)
						{
							SSLogger.errorLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @CommandInstance field is null or not declared, and its @InitCommand method is non-static. Unable to invoke the method");
							try
							{
								loader.close();
							}
							catch(IOException e2)
							{
								SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
								currentErrors++;
							}
							currentErrors++;
							return;
						}
						catch(IllegalAccessException e)
						{
							SSLogger.errorLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @InitCommand method cannot be accessed. Unable to invoke the method");
							try
							{
								loader.close();
							}
							catch(IOException e2)
							{
								SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
								currentErrors++;
							}
							currentErrors++;
							return;
						}
						catch(InvocationTargetException e)
						{
							SSLogger.errorLog("[CommandLoader] Candidate plugin " + pluginFile.getName() + "'s @plugin method threw an exception: " + e.getCause().toString());
							currentErrors++;
						}
						break;
					}
				}
				if(flag)
				{
					SSLogger.errorLog("[CommandLoader] Candidate jar " + pluginFile.getName() + "'s main class does not contain a method annotated with @InitCommand, ignoring");
					try
					{
						loader.close();
					}
					catch(IOException e2)
					{
						SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
						currentErrors++;
					}
					currentErrors++;
					return;
				}
			}
			else
			{
				SSLogger.errorLog("[CommandLoader] Candidate jar " + pluginFile.getName() + "'s config.cfg does not contain a plugin-class property, ignoring");
				EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 0, props);
				try
				{
					loader.close();
				}
				catch(IOException e)
				{
					SSLogger.errorLog("[CommandLoader] Error closing resources, this may cause resource leaks!");
					currentErrors++;
				}
				currentErrors++;
				return;
			}
		}
		finally
		{
			if(currentErrors < 1)
			{
				SSLogger.log("[CommandLoader] Loaded plugin " + pluginFile.getName() + " successfully");
				PluginDescriptor desc = new PluginDescriptor(pluginFile, SSUtil.getPath(pluginFile), props.getProperty("plugin-id", "null" + plugins.size()), props.getProperty("plugin-version", "null"), props.getProperty("plugin-author", "null"), loader);
				plugins.put(desc.pluginID, desc);
				EventBus.fire(EventType.PLUGIN_LOADED, 0, desc);
			}
			else errors += currentErrors;
		}
	}
	
	private static PluginClassLoader classLoader = null;
	
	static void loadClassPlugin(File pluginFile)
	{
		EventBus.fire(EventType.ATTEMPT_PLUGIN_LOAD, 1, pluginFile);
		if(classLoader == null)
		{
			try
			{
				classLoader = new PluginClassLoader(new File("plugins").toURI().toURL());
			}
			catch(Exception e)
			{
				SSLogger.errorLog("[CommandLoader] Error creating URL for class loader, ignoring plugin");
				EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
				errors++;
			}
			EventBus.subscribe(new EventHandler()
			{
				public void handleEvent(EventType type, Object... args)
				{
					if(type == EventType.SEASHELL_SHUTDOWN)
					{
						try
						{
							classLoader.close();
						}
						catch(Exception e)
						{
							if(SeaShell.debug) e.printStackTrace();
							else System.out.println(e);
						}
					}
				}
			});
		}
		SSLogger.log("Attempting to load plugin " + pluginFile.getName());
		String className = pluginFile.getName().substring(0, pluginFile.getName().length() - 6);
		EventBus.fire(EventType.ATTEMPT_PLUGIN_LOAD, 1, pluginFile);
		Class<?> clazz = null;
		try
		{
			clazz = classLoader.loadClass(className);
		}
		catch(ClassNotFoundException e)
		{
			SSLogger.errorLog("Class not found: " + className);
			EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
			return;
		}
		boolean flag = true;
		Object obj = null;
		for(Field f : clazz.getDeclaredFields())
		{
			f.setAccessible(true);
			if(f.getAnnotation(PluginInstance.class) != null)
			{
				try
				{
					obj = f.get(null);
				}
				catch (IllegalArgumentException e)
				{
					EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
					return;
				}
				catch(IllegalAccessException e)
				{
					EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
					return;
				}
				flag = false;
				break;
			}
		}
		if(flag) SSLogger.warningLog("No field annotated with @CommandInstance was found in " + className + ", this may cause loading errors");
		boolean flag2 = true;
		for(Method m : clazz.getDeclaredMethods())
		{
			m.setAccessible(true);
			if(m.getAnnotation(InitPlugin.class) != null)
			{
				try
				{
					m.invoke(obj, new PluginInitEvent(className));
				}
				catch (IllegalArgumentException e)
				{
					try
					{
						m.invoke(obj);
					}
					catch (IllegalArgumentException e2)
					{
						EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
						return;
					}
					catch(IllegalAccessException e2)
					{
						EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
						return;
					}
					catch(InvocationTargetException e2)
					{
						SSLogger.errorLog(className + "'s @InitCommand method threw an exception: " + e.getCause().toString());
						EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
					}
				}
				catch(IllegalAccessException e)
				{
					EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
					return;
				}
				catch(InvocationTargetException e)
				{
					SSLogger.errorLog(className + "'s @InitCommand method threw an exception: " + e.getCause().toString());
					EventBus.fire(EventType.PLUGIN_LOAD_FAILED, 1, e);
				}
				flag2 = false;
				break;
			}
		}
		if(flag2)
		{
			SSLogger.errorLog("No method annotated with @InitCommand was found in " + className + ", ignoring plugin");
			errors++;
			return;
		}
		
		SSLogger.log("Loaded plugin " + pluginFile.getName() + " successfully");
		EventBus.fire(EventType.PLUGIN_LOADED, 1, null);
	}
}
