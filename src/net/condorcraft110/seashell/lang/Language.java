package net.condorcraft110.seashell.lang;

import java.io.*;
import java.util.*;
import net.condorcraft110.seashell.*;

public class Language extends HashMap<String, String>
{
	private static final long serialVersionUID = 7110313910292418050L;
	
	private String ref;
	private boolean initialised = false;
	
	public Language(String ref)
	{
		this.ref = ref;
	}
	
	public void load() throws FileNotFoundException, IOException
	{
		Scanner scanner = new Scanner(SSUtil.getIS(ref));
		while(scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			if(!line.trim().equals(""))
			{
				String[] lineSplit = line.split("=");
				add(lineSplit[0], lineSplit[1]);
			}
		}
		scanner.close();
		initialised = true;
	}
	
	public boolean isInitialised()
	{
		return initialised;
	}
	
	public void add(String a, String b)
	{
		put(a, b);
		put(b, a);
	}
}
