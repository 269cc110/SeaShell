package net.condorcraft110.seashell;

import java.io.*;

import net.condorcraft110.seashell.csc.*;

class Sys
{
	private static int state = 0;
	
	static void runStartupScript()
	{
		if(state != 0) return;
		File file = new File("sys/startup.csc");
		try
		{
			CSC.runScript(file, false, null);
		}
		catch(FileNotFoundException e)
		{
			SSUtil.createCSC("sys/startup.csc", "*", "*", SSUtil.createMap("echocmd", "false"), null, true);
		}
		state = 1;
	}
	
	static void runShutdownScript()
	{
		if(state != 1) return;
		File file = new File("sys/shutdown.csc");
		try
		{
			CSC.runScript(file, false, null);
		}
		catch(FileNotFoundException e)
		{
			SSUtil.createCSC("sys/shutdown.csc", "*", "*", SSUtil.createMap("echocmd", "false"), null, true);
		}
		state = 2;
	}
}
