package net.condorcraft110.seashell;

import java.io.*;

public class SSLogger
{
	private static PrintStream stream;
	
	static void start()
	{
		try
		{
			File file = new File("logs", "seashell_" + System.currentTimeMillis() + ".log");
			file.getParentFile().mkdirs();
			file.createNewFile();
			stream = new PrintStream(file);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void log(String s)
	{
		stream.println("[" + System.currentTimeMillis() + "] [INFO]  " + s);
		EventBus.fire(EventType.TEXT_LOGGED, 0, s);
	}
	
	public static void warningLog(String s)
	{
		stream.println("[" + System.currentTimeMillis() + "] [WARN]  " + s);
		EventBus.fire(EventType.TEXT_LOGGED, 1, s);
	}

	static void errorLog(String s)
	{
		stream.println("[" + System.currentTimeMillis() + "] [ERROR] " + s);
		EventBus.fire(EventType.TEXT_LOGGED, 2, s);
		SeaShell.errorMessages.add(s);
	}
	
	static void exit()
	{
		stream.flush();
		stream.close();
	}
}
