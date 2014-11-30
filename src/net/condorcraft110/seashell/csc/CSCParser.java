package net.condorcraft110.seashell.csc;

import net.condorcraft110.seashell.*;

public class CSCParser
{
	public static CSCAttributes parseCSCInfo(String infoLine, String filename, boolean warnIncorrectVersion)
	{
		boolean echocmd = SeaShell.config.getBoolean("default-echocmd", true);
		try
		{
			//Check we're actually trying to parse a CSC script
			if(!infoLine.startsWith("[CSC]"))
			{
				System.out.println("Invalid CSC header: " + filename);
				return null;
			}
			String[] infoLineSplit = infoLine.substring(5).split("-");
			//Parse versions
			String versions = infoLineSplit[0];
			String[] versionsSplit = versions.split("\\|");
			if(warnIncorrectVersion && !versionsSplit[0].equals(SeaShell.SS_VERSION) && !versionsSplit[0].equals("*"))
			{
				SSLogger.warningLog("Warning: " + filename + " was written for a different version of SeaShell, some commands may not work properly!");
			}
			if(warnIncorrectVersion && !versionsSplit[1].equals(SeaShell.CSC_VERSION) && !versionsSplit[0].equals("*"))
			{
				System.out.println("Warning: " + filename + " was written for a different version of CSC, this may cause parsing errors!");
			}
			//Parse declarations
			String[] info = infoLineSplit[1].split("\\|");
			for(String nextDeclaration : info)
			{
				if(nextDeclaration.equals("!")) break;
				String[] dec = nextDeclaration.split("=");
				if(dec[0].equals("echocmd"))
				{
					echocmd = Boolean.parseBoolean(dec[1]);
				}
				if(dec.length != 2)
				{
					System.out.println("Invalid CSC header: " + filename);
					return null;
				}
			}
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
			return null;
		}
		return new CSCAttributes(echocmd);
	}
}
