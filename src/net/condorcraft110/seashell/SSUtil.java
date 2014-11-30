package net.condorcraft110.seashell;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.*;
import java.security.*;
import java.util.regex.*;
import java.nio.charset.*;
import java.nio.channels.*;
import net.condorcraft110.seashell.csc.*;
import net.condorcraft110.seashell.lang.*;

@SuppressWarnings("unused")
public class SSUtil
{
	public static final Pattern EMBEDDED_PATTERN = Pattern.compile("\\{.*?\\}");
	public static final Pattern SPLITTER_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	
	/**
	 * 
	 * @param src the command to parse
	 * @return the formatted command as a {@code String} array
	 */
	public static String[] formatCommand(String src)
	{
		src = src.replace("^", "^E").replace("\\\\", "^BS").replace("\\%", "^P").replace("\\$", "^D").replace("\\&", "^A");
		src = Variables.format(Constants.format(CSC.format(src
				.replace("\\\"", "^Q")
				.replace("\\{", "^OB")
				.replace("\\}", "^CB")
				.replace("\\~", "^T"))));
		
		Matcher matcher0 = EMBEDDED_PATTERN.matcher(src);
		while(matcher0.find())
		{
			String found0 = matcher0.group(0).trim();
			String found = found0.substring(1, found0.length() - 1);
			src = replaceFirstLiteral(found0, SeaShell.execCommandToString(found), src);
		}
		
		src = src.replace("^BS", "\\")
				.replace("^OB", "{")
				.replace("^CB", "}")
				.replace("^A", "&")
				.replace("^D", "$")
				.replace("^P", "%")
				.replace("~", System.getProperty("user.home"))
				.replace("^T", "~")
				.replace("^E", "^");
		
		ArrayList<String> list = new ArrayList<>();
		Matcher matcher1 = SPLITTER_PATTERN.matcher(src);
		while(matcher1.find())
		{
			list.add(matcher1.group(1).replace("\"", "").replace("^Q", "\""));
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * Returns a copy of an array 
	 * @param src
	 * @param off
	 * @return
	 */
	public static String[] copyFrom(String[] src, int off)
	{
		String[] result = new String[src.length - off];
		/*for(int i = off; i < src.length; i++)
		{
			result[i - off] = src[i];
		}*/
		System.arraycopy(src, off, result, 0, src.length - off);
		return result;
	}
	
	public static String[] addToBeginning(String toAdd, String[] addTo)
	{
		String[] result = new String[addTo.length + 1];
		result[0] = toAdd;
		System.arraycopy(addTo, 0, result, 1, addTo.length);
		return result;
	}
	
	public static String concatenate(String[] src, String separator)
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(String s : src)
		{
			if(!first) builder.append(separator);
			builder.append(s);
			first = false;
		}
		return builder.toString();
	}
	
	public static String formatShellCommand(String[] args)
	{
		String plaf = System.getProperty("os.name");
		String result = toCommandString(args);
		if(plaf.toLowerCase().contains("win")) result = "cmd /c " + result;
		return result;
	}
	
	public static String[] copyTo(String[] src, int to)
	{
		String[] result = new String[src.length - to];
		/*for(int i = 0; i < result.length; i++)
		{
			result[i] = src[i];
		}*/
		System.arraycopy(src, 0, result, 0, src.length - to);
		return result;
	}
	
	public static String[] addToBeginningOfAll(String toAdd, String[] addTo)
	{
		String[] result = new String[addTo.length];
		for(int i = 0; i < addTo.length; i++)
		{
			result[i] = toAdd + addTo[i];
		}
		return result;
	}
	
	public static Properties readProperties(String filename)
	{
		Properties props = new Properties();
		
		try
		{
			DataInputStream stream = getIS(filename);
			props.load(stream);
			stream.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		return props;
	}
	
	public static String[] localiseAll(String[] src)
	{
		String[] result = new String[src.length];
		
		for(int i = 0; i < src.length; i++)
		{
			result[i] = SeaShell.localiser.localise(src[i]);
		}
		
		return result;//src.replaceFirst(found0.replace("{", "\\{").replace("}", "\\}"), SeaShell.execCommandToString(found));
	}
	
	public static String replaceFirstLiteral(String find, String replace, String in)
	{
		return Pattern.compile(find, Pattern.LITERAL).matcher(in).replaceFirst(replace);
	}
	
	public static byte[] digestFile(String digestName, String filename)
	{
		byte[] result = null;
		
		try
		{
			File file = new File(filename);
			MessageDigest digest = MessageDigest.getInstance(digestName);
			InputStream stream = getIS(file);
			byte[] bytes = new byte[(int)file.length()];
			stream.close();
			result =  digest.digest(bytes);
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
		
		return result;
	}
	
	public static void download(String urlString, String destFolder) // wip
	{
		try
		{
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.connect();
			ReadableByteChannel channel = Channels.newChannel(conn.getInputStream());
			File file = new File(destFolder, new File(url.getFile()).getName());
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
			outStream.close();
			channel.close();
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
		
		/*try
		{
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream in = conn.getInputStream();
			File file = new File(destFolder, new File(url.getFile()).getName());
			DataOutputStream out = getOS(file);
			copyFully(in, out);
			in.close();
			out.close();
		}
		catch(Exception e)
		{
			if(SeaShell.DEBUG) e.printStackTrace();
			else System.out.println(e);
		}*/
	}
	
	public static String getPath(File f)
	{
		try
		{
			return f.getCanonicalPath();
		}
		catch(Exception e)
		{
			return f.getAbsolutePath();
		}
	}
	
	public static String toCommandString(String[] src)
	{
		return "\"" + concatenate(src, "\" \"") + "\"";
	}
	
	public static byte[] toPrimitiveByteArray(Byte[] in)
	{
		byte[] result = new byte[in.length];
		for(int i = 0; i < in.length; i++)
		{
			byte b = in[i].byteValue();
			result[i] = b;
		}
		return result;
	}
	
	public static DataInputStream getIS(File file)
	{
		DataInputStream stream = null;
		try
		{
			if(!file.exists())
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
		return stream;
	}
	
	public static DataInputStream getIS(String ref)
	{
		return getIS(new File(ref));
	}
	
	public static DataOutputStream getOS(File file)
	{
		DataOutputStream stream = null;
		try
		{
			if(!file.exists())
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
		return stream;
	}
	
	public static DataOutputStream getOS(String ref)
	{
		return getOS(new File(ref));
	}
	
	public static File attemptCanonical(File f)
	{
		try
		{
			return f.getCanonicalFile();
		}
		catch(Exception e)
		{
			return f;
		}
	}
	
	public static <K, V> void printMap(Map<K, V> map)
	{
		printMap(System.out, map);
	}
	
	public static <K, V> void printMap(PrintStream stream, Map<K, V> map)
	{
		for(Entry<K, V> e : map.entrySet())
		{
			stream.println(e.getKey().toString() + "=" + e.getValue().toString());
		}
	}
	
	public static void copyFully(InputStream in, OutputStream out)
	{
		try
		{
			byte[] b = new byte[SeaShell.config.getInt("io-chunk-size", 4096)];
			int i = in.read(b);
			while(i > -1)
			{
				out.write(b, 0, i);
				i = in.read(b);
			}
		}
		catch(Exception e)
		{
			if(SeaShell.debug) e.printStackTrace();
			else System.out.println(e);
		}
	}
	
	public static File resolveFile(String path, boolean canonicalise, boolean createIfNonExistent)
	{
		File result = new File(path);
		if(!result.isAbsolute()) result = new File(SeaShell.currentDir, path);
		if(createIfNonExistent && !result.exists()) create(result, false);
		return canonicalise ? attemptCanonical(result) : result;
	}
	
	public static void create(File f, boolean silent)	
	{
		try
		{
			if(!f.exists())
			{
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
		}
		catch(Exception e)
		{
			if(!silent)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
	}
	
	public static String[] mapToArray(Map<String, String> map, String kvSeparator)
	{
		if(map == null || kvSeparator == null) return new String[0];
		
		ArrayList<String> result = new ArrayList<>();
		
		for(Entry<String, String> e : map.entrySet())
		{
			result.add(e.getKey().toString() + kvSeparator + e.getValue().toString());
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	public static void createCSC(String name, String ssVersion, String cscVersion, Map<String, String> args, ArrayList<String> commands, boolean silent)
	{
		try
		{
			File file = resolveFile(name, true, true);
			OutputStreamWriter writer = new OutputStreamWriter(getOS(file));
			writer.write("[CSC]" + ssVersion + "|" + cscVersion + "-" + concatenate(mapToArray(args, "="), "|") + "|!\n");
			
			if(commands != null)
			{
				for(String string : commands)
				{
					writer.write(string + "\n");
				}
			}
		}
		catch(Exception e)
		{
			if(!silent)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
	}
	
	@SafeVarargs
	public static Map<String, String> createMap(String... args)
	{
		HashMap<String, String> result = new HashMap<>();
		
		for(int i = 0; i < args.length; i++)
		{
			result.put(args[i], args[++i]);
		}
		
		return result;
	}
	
	public static int clampInt(int i, int lower, int upper)
	{
		return i > upper ? upper : (i < lower ? lower : i);
	}
	
	public static Charset getCharsetSilently(String name, boolean returnUTF8)
	{
		try
		{
			return Charset.forName(name);
		}
		catch(Exception e)
		{
			return returnUTF8 ? getCharsetSilently("UTF-8", false) : null;
		}
	}
}
