package net.condorcraft110.seashell;

import java.io.*;
import java.util.*;
import java.util.regex.*;

// TODO WIP
public class GroupedFileParser implements Closeable
{
	private final InputStream in;
	private final Scanner scanner;
	private final HashMap<String, ArrayList<String>> groups = new HashMap<>();
	
	public GroupedFileParser(String ref)
	{
		this(new File(ref));
	}
	
	public GroupedFileParser(File f)
	{
		in = SSUtil.getIS(f);
		scanner = new Scanner(in);
		in.mark(Integer.MAX_VALUE);
	}
	
	public ArrayList<String> getGroup(String groupName)
	{
		ArrayList<String> group = new ArrayList<>();
		try
		{
			StringBuilder builder = new StringBuilder();
			if(scanner.hasNextLine())
			{
				String line = scanner.nextLine();
				while(scanner.hasNextLine() && line != "")
				{
					builder.append(line);
					line = scanner.nextLine();
				}
			}
			String fullText = builder.toString().replaceAll("(\r|\n)", "");
			Pattern pattern = Pattern.compile("group \\'.+?\\'");
			Matcher matcher0 = pattern.matcher(fullText);
			while(matcher0.find())
			{
				String s = matcher0.group(1);
			}
			
			in.reset();
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
		return group;
	}
	
	public void close()
	{
		scanner.close();
	}
}
