package net.condorcraft110.seashell.csc;

import java.io.*;
import java.util.*;
import net.condorcraft110.seashell.*;

public class CSC
{
	private static ArrayDeque<CSC> currentCSCs = new ArrayDeque<>();
	
	private static boolean inScript = false;
	
	private ArrayList<String> argsList = new ArrayList<>();
	
	private CSC() {}
	
	public static void runScript(File file) throws FileNotFoundException
	{
		runScript(file, true, null);
	}
	
	public static void runScript(File file, boolean warnIncorrectVersion, String[] args) throws FileNotFoundException
	{
		inScript = true;
		
		CSC csc = new CSC();
		
		if(args != null)
		{
			for(String arg : args)
			{
				csc.argsList.add(arg);
			}
		}
		
		currentCSCs.push(csc);
		
		int lineNumber = 2;
		Scanner scanner = new Scanner(file);
		String info = scanner.nextLine();
		
		while(info.trim().equals("") || info.trim().startsWith("#"))
		{
			info = scanner.nextLine();
			lineNumber++;
		}
		
		CSCAttributes attrib = CSCParser.parseCSCInfo(info, file.getName(), warnIncorrectVersion);
		
		if(attrib == null)
		{
			scanner.close();
			return;
		}
		
		while(scanner.hasNextLine())
		{
			String line = scanner.nextLine().trim();
			line.replaceAll("#.*#", "");
			if(!line.startsWith("#") && !line.equals("")) SeaShell.execCommand(line, "Unknown command: $CMD in " + file.getName() + " at line " + lineNumber, attrib.echocmd);
			lineNumber++;
		}
		
		scanner.close();
		
		currentCSCs.pop();
		
		inScript = !currentCSCs.isEmpty();
	}
	
	public static String format(String line)
	{
		if(!inScript) return line;
		
		String result = line;
		
		CSC csc = currentCSCs.peek();
		for(int i = 0; i < csc.argsList.size(); i++)
		{
			result = result.replace("&" + i, csc.argsList.get(i));
		}
		
		return result;
	}
}
