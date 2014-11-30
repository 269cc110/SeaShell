package net.condorcraft110.seashell;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.nio.charset.*;
import org.apache.commons.exec.*;
import net.condorcraft110.seashell.csc.*;
import org.apache.commons.exec.environment.*;

@SuppressWarnings("unused")
public final class SSCommandExecutors
{
	/**
	 * Exits SeaShell
	 */
	public static class CommandExecutorExit implements CommandExecutor
	{
		public void exec(String[] args)
		{
			SeaShell.shutdown(true);
			Variables.exit();
			SSLogger.exit();
			System.exit(0);
		}
		
		public String getHelp()
		{
			return "Exits SeaShell";
		}
		
		public String getUsage()
		{
			return "exit";
		}
	}
	
	/**
	 * Attempts to execute a command on the system shell
	 * 
	 * @param command the command to execute
	 */
	public static class CommandExecutorShell implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}

			try
			{
				CommandLine line = CommandLine.parse(SSUtil.formatShellCommand(args));
				DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
				DefaultExecutor exec = new DefaultExecutor();
				exec.setWorkingDirectory(SeaShell.currentDir);
				exec.setStreamHandler(new PumpStreamHandler(System.out, System.err, System.in));
				exec.setExitValues(null);
				exec.execute(line, EnvironmentUtils.getProcEnvironment(), handler);
				handler.waitFor();
				int i = handler.getExitValue();
				if(i != 0) System.out.println("Process exited with non-zero exit code " + i);
				ExecuteException e = handler.getException();
				if(e != null) throw e;
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
		
		public String getHelp()
		{
			return "Attempts to execute a command on the system shell";
		}
		
		public String getUsage()
		{
			return "shell <shell command>";
		}
	}
	
	/**
	 * SeaShell help
	 * 
	 * @param command the command to print the help message of
	 */
	public static class CommandExecutorHelp implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				printCommands();
				return;
			}
			printHelp(args[0]);
		}
		
		private void printCommands()
		{
			boolean vhelp = SeaShell.config.getBoolean("vertical-help", true);
			Set<String> commandsSet = SeaShell.commands.keySet();
			System.out.print("Available commands:");
			if(vhelp)
			{
				System.out.println();
				for(String s : commandsSet)
				{
					System.out.print("    " + SeaShell.localiser.localise(s) + " - ");
					printHelp(SeaShell.localiser.localise(s));
				}
			}
			else
			{
				System.out.println(" " + SSUtil.concatenate(SSUtil.localiseAll(commandsSet.toArray(new String[commandsSet.size()])), ", "));
			}
		}
		
		private void printHelp(String command)
		{
			if(SeaShell.commands.containsKey(SeaShell.localiser.localise(command))) System.out.println(SeaShell.commands.get(SeaShell.localiser.localise(command)).getHelp());
			else System.out.println("Unknown command: " + command);
		}
		
		public String getHelp()
		{
			return "SeaShell help";
		}
		
		public String getUsage()
		{
			return "help [command]";
		}
	}
	
	
	
	public static class CommandExecutorChDir implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			File file = new File(args[0]);
			if(!file.isAbsolute()) file = new File(SeaShell.currentDir, args[0]);
			File canonFile = SSUtil.attemptCanonical(file);
			String canonPath = SSUtil.getPath(canonFile);
			if(!canonFile.exists())
			{
				System.out.println("Not found: " + canonPath);
				return;
			}
			else if(!file.isDirectory())
			{
				System.out.println("Not a directory: " + canonPath);
				return;
			}
			SeaShell.currentDir = canonFile;
			Constants.addConst("CDIR", canonPath);
			System.out.println("Changed working directory to " + canonPath);
		}
		
		public String getHelp()
		{
			return "Changes the current working directory to the specified string";
		}
		
		public String getUsage()
		{
			return "chdir <directory>";
		}
	}
	
	public static class CommandExecutorCSC implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			File file = new File(args[0]);
			if(!file.isAbsolute()) file = new File(SeaShell.currentDir, args[0]);
			if(!file.exists())
			{
				System.out.println("Not found: " + SSUtil.getPath(file));
				return;
			}
			try
			{
				CSC.runScript(file, true, SSUtil.copyFrom(args, 1));
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			} 
		}
		
		public String getHelp()
		{
			return "Reads and executes a CSC script";
		}
		
		public String getUsage()
		{
			return "csc <CSC script> [arguments...]";
		}
	}
	
	public static class CommandExecutorEcho implements CommandExecutor
	{
		public void exec(String[] args)
		{
			System.out.println(SSUtil.concatenate(args, " "));
		}
		
		public String getHelp()
		{
			return "Prints the specified string";
		}
		
		public String getUsage()
		{
			return "echo [string]";
		}
	}
	
	public static class CommandExecutorCopy implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			try
			{
				File srcFile = SSUtil.resolveFile(args[0], true, false);
				if(!srcFile.exists())
				{
					System.out.println("File not found: " + SSUtil.getPath(srcFile));
					return;
				}
				DataInputStream in = SSUtil.getIS(srcFile);
				DataOutputStream out = SSUtil.getOS(SSUtil.resolveFile(args[1], true, true));
				SSUtil.copyFully(in, out);
				in.close();
				out.close();
			}
			catch(Exception e)
			{
				System.out.println("Unable to copy " + args[0] + " to " + args[1] + ", an exception occurred: " + (!SeaShell.debug ? e.toString() : ""));
				if(SeaShell.debug) e.printStackTrace();
			}
		}
		
		public String getHelp()
		{
			return "Copies a file (directories are not supported) from one directory to another";
		}
		
		public String getUsage()
		{
			return "copy <source file> <destination directory>";
		}
	}
	
	public static class CommandExecutorDelete implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			File file = new File(args[0]);
			if(!file.exists()) file = new File(SeaShell.currentDir, args[0]);
			if(!file.exists())
			{
				System.out.println("File not found: " + args[0]);
				return;
			}
			System.out.print("Are you sure you want to delete " + args[0] + "? This action cannot be undone (Y|N) ");
			String response = SeaShell.scanner.nextLine();
			if(response.trim().equalsIgnoreCase("y") || response.trim().equalsIgnoreCase("yes") || response.trim().equalsIgnoreCase("true") || response.trim().equalsIgnoreCase("yeah"))
			{
				delete(file);
			}
		}
		
		private void delete(File file)
		{
			if(file.isDirectory())
			{
				for(File f : file.listFiles())
				{
					delete(f);
				}
			}
			if(!file.delete()) System.out.println("Unable to delete file " + file.getAbsolutePath());
		}
		
		public String getHelp()
		{
			return "Permanently deletes a file or directory. Use with caution, files cannot be recovered from within SeaShell";
		}
		
		public String getUsage()
		{
			return "delete <file>";
		}
	}
	
	public static class CommandExecutorMkDir implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: mkdir " + getUsage());
				return;
			}
			if(args[0].equals("love")) System.out.println("not war?");
			File file = new File(args[0]);
			if(file.exists()) return;
			file.mkdirs();
		}
		
		public String getHelp()
		{
			return "Creates a directory";
		}
		
		public String getUsage()
		{
			return "mkdir <directory>";
		}
	}
	
	public static class CommandExecutorVersion implements CommandExecutor
	{
		public void exec(String[] args)
		{
			System.out.println("SeaShell " + SeaShell.SS_VERSION + " by " + SeaShell.AUTHOR);
		}
		
		public String getHelp()
		{
			return "Prints the SeaShell version";
		}
		
		public String getUsage()
		{
			return "version";
		}
	}
	
	public static class CommandExecutorDownload implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			try
			{
				URL url = new URL(args[0]);
				URLConnection connection = url.openConnection();
				connection.connect();
				InputStream in = connection.getInputStream();
				File dnldFolder = new File("downloads");
				if(!dnldFolder.exists()) dnldFolder.mkdirs();
				File file = new File(dnldFolder, new File(url.getFile()).getName());
				if(args.length > 1)
				{
					file = new File(args[1]);
				}
				if(!file.exists()) file.createNewFile();
				DataOutputStream out = SSUtil.getOS(file);
				SSUtil.copyFully(in, out);
				in.close();
				out.flush();
				out.close();
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
		
		public String getHelp()
		{
			return "Downloads a file from a server, similar to wget on Unix and Linux operating systems";
		}
		
		public String getUsage()
		{
			return "download <URL>";
		}
	}
	
	public static class CommandExecutorDirectory implements CommandExecutor
	{
		public void exec(String[] args)
		{
			File f = SeaShell.currentDir;
			boolean showhidden = false;
			if(args.length > 0)
			{
				for(int i = 0; i < args.length; i++)
				{
					if(args[i].equalsIgnoreCase("-hidden") || args[i].equalsIgnoreCase("-h"))
					{
						showhidden = true;
						break;
					}
				}
				
				File file = SSUtil.resolveFile(args[0], true, false);
				if(file.exists()) f = file;
			}
			for(File file : f.listFiles())
			{
				if(showhidden) System.out.println((file.isHidden() ? "[HID] " : "[VIS] ") + (file.isFile() ? "[FILE] " : "[DIR]  ") + file.getName());
				else if(!file.isHidden()) System.out.println((file.isFile() ? "[FILE] " : "[DIR]  ") + file.getName());
			}
		}
		
		public String getHelp()
		{
			return "Prints the contents of a directory. Hidden files are not displayed unless the switch -hidden (or -h) is specified";
		}
		
		public String getUsage()
		{
			return "directory [directory] [-hidden|-h]";
		}
	}
	
	public static class CommandExecutorRandomFile implements CommandExecutor
	{
		private Random random = new Random();
		private HashMap<Long, Random> seededRandomCache = new HashMap<>();
		
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			try
			{
				File file = new File(args[0]);
				if(file.exists())
				{
					System.out.println("File " + args[0] + " already exists");
					return;
				}
				if(!file.isAbsolute()) file = new File(SeaShell.currentDir, args[0]);
				file.createNewFile();
				Random random = findRandom(args);
				int chunkSize = SeaShell.config.getInt("io-chunk-size", 4096);
				int sizeRemaining = Integer.parseInt(args[1]);
				DataOutputStream stream = SSUtil.getOS(file);
				while(sizeRemaining > 0)
				{
					int nextChunkSize = sizeRemaining >= chunkSize ? chunkSize : sizeRemaining;
					sizeRemaining -= nextChunkSize;
					byte[] bytes = new byte[nextChunkSize];
					random.nextBytes(bytes);
					stream.write(bytes);
				}
				stream.close();
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
				return;
			}
		}
		
		public String getHelp()
		{
			return "Creates a file of specified size and fills it with randomly generated bytes";
		}
		
		public String getUsage()
		{
			return "randomfile <filename> <filesize in bytes> [seed]";
		}
		
		private Random findRandom(String[] args)
		{
			if(args.length > 2)
			{
				Long seed = Long.valueOf(args[2]);
				
				if(seededRandomCache.containsKey(seed)) return seededRandomCache.get(seed);
				
				return seededRandomCache.put(seed, new Random(seed.longValue()));
			}
			
			return random;
		}
	}
	
	public static class CommandExecutorAlias implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			if(SeaShell.localisedCommandExists(args[0], 0) && SeaShell.hasAliases(args[0]))
			{
				System.out.println("Aliases for " + args[0] + ": " + SSUtil.concatenate(SSUtil.localiseAll(SeaShell.aliasStrings.get(SeaShell.localiser.localise(args[0]))), ", "));
			}
			else if(!SeaShell.localisedCommandExists(args[0], 0))
			{
				System.out.println("Unknown command: " + args[0]);
			}
			else if(!SeaShell.hasAliases(args[0]))
			{
				System.out.println("No aliases found for command " + args[0]);
			}
		}
		
		public String getHelp()
		{
			return "Prints the aliases for the specified command";
		}
		
		public String getUsage()
		{
			return "alias <command>";
		}
	}
	
	public static class CommandExecutorPrint implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			try
			{
				File file = new File(args[0]);
				if(!file.isAbsolute()) file = new File(SeaShell.currentDir, args[0]);
				if(!file.exists())
				{
					System.out.println("File not found: " + args[0]);
					return;
				}
				DataInputStream stream = SSUtil.getIS(file);
				byte[] bytes = new byte[(int)file.length()];
				stream.readFully(bytes);
				stream.close();
				System.out.println(new String(bytes));
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
		
		public String getHelp()
		{
			return "Prints the contents of the specified file";
		}
		
		public String getUsage()
		{
			return "print <file>";
		}
	}
	
	public static class CommandExecutorSeaShell implements CommandExecutor
	{
		public void exec(String[] args)
		{
			new CommandExecutorVersion().exec(null);
			System.out.println("System information: ");
			System.out.println("    OS:\t\t\t\t" + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
			System.out.println("    Java:\t\t\t" + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
			System.out.println("    Data model:\t\t\t" + System.getProperty("sun.arch.data.model"));
			System.out.println("    Available processors/cores:\t" + Runtime.getRuntime().availableProcessors());
		}
		
		public String getHelp()
		{
			return "Prints information about SeaShell and the platform it is currently running on";
		}
		
		public String getUsage()
		{
			return "seashell";
		}
	}
	
	public static class CommandExecutorUpdate implements CommandExecutor
	{
		public void exec(String[] args)
		{
			/*if(!SeaShell.updatesAvailable)
			{
				System.out.println("No updates are currently available");
				return;
			}
			try
			{
				ProcessBuilder builder = new ProcessBuilder(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java" + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : ""), "-jar", "updater.jar", SeaShell.SS_VERSION);
				builder.inheritIO();
				SSLogger.exit();
				builder.start();
				System.exit(0);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}*/
		}
		
		public String getHelp()
		{
			return "[NYI] If updates are available, updates SeaShell";
		}
		
		public String getUsage()
		{
			return "update";
		}
	}
	
	public static class CommandExecutorErrors implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(SeaShell.errorMessages.size() > 0)
				for(String s : SeaShell.errorMessages) System.out.println(s);
			else
				System.out.println("No errors encountered during startup");
		}

		public String getHelp()
		{
			return "Prints any error messages encountered during startup";
		}
		
		public String getUsage()
		{
			return "errors";
		}
	}
	
	public static class CommandExecutorClear implements CommandExecutor
	{
		public void exec(String[] args)
		{
			/*try
		    {
		        String os = System.getProperty("os.name");

		        if(os.contains("Windows"))
		        {
		            Runtime.getRuntime().exec("cls");
		        }
		        else
		        {
		            Runtime.getRuntime().exec("clear");
		        }
		        System.out.println(SeaShell.localiser.localise("message.seashell").replace("$SSVERSION", SeaShell.SS_VERSION).replace("$CMDCOUNT", "" + (SeaShell.commands.size() + SeaShell.unlocalisedCommands.size())).replace("$ERRCOUNT", "" + CommandLoader.errors));
		    }
		    catch(Exception e)
		    {
		        System.out.println(e);
		    }*/
			
			for(int i = 0; i < 128; i++)
			{
				System.out.println();
			}
		}
		
		public String getHelp()
		{
			return "Attempts to clear the screen";
		}
		
		public String getUsage()
		{
			return "clear";
		}
	}
	
	public static class CommandExecutorSet implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			Variables.setVar(args[0], SSUtil.concatenate(SSUtil.copyFrom(args, 1), " "), false);
		}
		
		public String getHelp()
		{
			return "Sets a variable";
		}
		
		public String getUsage()
		{
			return "set <name> <value>";
		}
	}
	
	public static class CommandExecutorSetp implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
		
			Variables.setVar(args[0], args[1], true);
		}
		
		public String getHelp()
		{
			return "Sets a variable that will remain after SeaShell exits";
		}
		
		public String getUsage()
		{
			return "setp <name> <value>";
		}
	}
	
	public static class CommandExecutorAdd implements CommandExecutor
	{
		public void exec(String[] args)
		{
			double[] doubles = new double[args.length];
			
			for(int i = 0; i < args.length; i++)
			{
				try
				{
					doubles[i] = Double.parseDouble(args[i]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Invalid number: " + args[i]);
					return;
				}
			}
			
			double dresult = 0;
			
			for(double d : doubles)
			{
				dresult += d;
			}
			
			String result = "" + dresult;
			if(result.endsWith(".0")) result = result.substring(0, result.length() - 2);
			
			System.out.println(result);
		}
		
		public String getHelp()
		{
			return "Adds a series of numbers together and prints the result";
		}
		
		public String getUsage()
		{
			return "add [array of doubles]";
		}
	}
	
	public static class CommandExecutorSubtract implements CommandExecutor
	{
		public void exec(String[] args)
		{
			double[] doubles = new double[args.length];
			
			for(int i = 0; i < args.length; i++)
			{
				try
				{
					doubles[i] = Double.parseDouble(args[i]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Invalid number: " + args[i]);
					return;
				}
			}
			
			double dresult = doubles[0];
			
			for(int i = 1; i < doubles.length; i++)
			{
				dresult -= doubles[i];
			}
			
			String result = "" + dresult;
			if(result.endsWith(".0")) result = result.substring(0, result.length() - 2);
			
			System.out.println(result);
		}
		
		public String getHelp()
		{
			return "Subtracts a series of numbers from each other and prints the result";
		}
		
		public String getUsage()
		{
			return "subtract [array of doubles]";
		}
	}
	
	public static class CommandExecutorMultiply implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			double[] doubles = new double[args.length];
			
			for(int i = 0; i < args.length; i++)
			{
				try
				{
					doubles[i] = Double.parseDouble(args[i]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Invalid number: " + args[i]);
					return;
				}
			}
			
			double dresult = doubles[0];
			
			for(int i = 1; i < doubles.length; i++)
			{
				dresult *= doubles[i];
			}
			
			String result = "" + dresult;
			if(result.endsWith(".0")) result = result.substring(0, result.length() - 2);
			
			System.out.println(result);
		}
		
		public String getHelp()
		{
			return "Multiplies a series of numbers together and prints the result";
		}
		
		public String getUsage()
		{
			return "multiply [array of doubles]";
		}
	}
	
	public static class CommandExecutorDivide implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			double[] doubles = new double[args.length];
			
			for(int i = 0; i < args.length; i++)
			{
				try
				{
					doubles[i] = Double.parseDouble(args[i]);
				}
				catch(NumberFormatException e)
				{
					System.out.println("Invalid number: " + args[i]);
					return;
				}
			}
			
			double dresult = doubles[0];
			
			for(int i = 1; i < doubles.length; i++)
			{
				dresult /= doubles[i];
			}
			
			String result = "" + dresult;
			if(result.endsWith(".0")) result = result.substring(0, result.length() - 2);
			
			System.out.println(result);
		}
		
		public String getHelp()
		{
			return "Divides a series of numbers together and prints the result";
		}
		
		public String getUsage()
		{
			return "divide [array of doubles]";
		}
	}
	
	public static class CommandExecutorIf implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 3)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			String[] commands = SSUtil.copyFrom(args, 3);
			
			if(args[1].equalsIgnoreCase("equals"))
			{
				if(args[0].equalsIgnoreCase(args[2])) execCommands(commands);
			}
			else if(args[1].equalsIgnoreCase("equalsc"))
			{
				if(args[0].equals(args[2])) execCommands(commands);
			}
			else if(args[1].equalsIgnoreCase("equalsn"))
			{
				if(Double.parseDouble(args[0]) == Double.parseDouble(args[2])) execCommands(commands);
			}
			else if(args[1].equalsIgnoreCase("greater"))
			{
				if(Double.parseDouble(args[0]) > Double.parseDouble(args[2])) execCommands(commands);
			}
			else if(args[1].equalsIgnoreCase("less"))
			{
				if(Double.parseDouble(args[0]) < Double.parseDouble(args[2])) execCommands(commands);
			}
			else if(args[1].equalsIgnoreCase("contains"))
			{
				if(args[0].contains(args[2])) execCommands(commands);
			}
			else if(args[1].equalsIgnoreCase("starts"))
			{
				if(args[2].startsWith(args[0])) execCommands(commands);
			}
			else if(args[1].equalsIgnoreCase("ends"))
			{
				if(args[2].endsWith(args[0])) execCommands(commands);
			}
			else
			{
				System.out.println("Invalid operation: " + args[1]);
				return;
			}
		}
		
		private void execCommands(String[] cmds)
		{
			for(String cmd : cmds)
			{
				SeaShell.execCommand(cmd);
			}
		}
		
		public String getHelp()
		{
			return "Executes the specified commands if the condition is met";
		}
		
		public String getUsage()
		{
			return "if <value a> <equals[c|n]|greater|less|contains|starts|ends> <value b> <commands...>";
		}
	}
	
	public static class CommandExecutorSeta implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			Variables.setAlias(args[0], args[1], false);
		}
		
		public String getHelp()
		{
			return "Sets the specified value as an alias for the specified command, useful with 'if'";
		}
		
		public String getUsage()
		{
			return "seta <command> <alias>";
		}
	}
	
	public static class CommandExecutorSetap implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			Variables.setAlias(args[0], args[1], true);
		}
		
		public String getHelp()
		{
			return "Sets the specified value as a persistent alias for the specified command, useful with 'if'";
		}
		
		public String getUsage()
		{
			return "setap <command> <alias>";
		}
	}
	
	public static class CommandExecutorInput implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length > 0)
			{
				SeaShell.STDOUT.print(SSUtil.concatenate(args, " "));
			}
			
			System.out.println(SSAPI.getStdinScanner().nextLine());
		}
		
		public String getHelp()
		{
			return "Gets a line of input from stdin and prints it";
		}
		
		public String getUsage()
		{
			return "input [prompt]";
		}
	}
	
	public static class CommandExecutorSleep implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			try
			{
				Thread.sleep(Long.parseLong(args[0]));
			}
			catch(NumberFormatException e)
			{
				System.out.println("Invalid number: " + args[0]);
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
		
		public String getHelp()
		{
			return "Waits for the specified number of milliseconds (seconds / 1000)";
		}
		
		public String getUsage()
		{
			return "sleep <milliseconds>";
		}
	}
	
	public static class CommandExecutorDel implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			Variables.delVar(args[0]);
		}
		
		public String getHelp()
		{
			return "Deletes a variable";
		}
		
		public String getUsage()
		{
			return "del <variable name>";
		}
	}
	
	private static class CommandExecutorRandom implements CommandExecutor // WIP
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
			}
			
			int times = 0;
			try
			{
				times = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e)
			{
				System.out.println("Invalid number: " + args[0]);
				return;
			}
			
			Random random = new Random();
			
			for(int i = 0; i < times; i++)
			{
				System.out.println(random.nextInt());
			}
			
			System.out.println();
		}
		
		public String getHelp()
		{
			return "Prints a string of random numbers of the specified length";
		}
		
		public String getUsage()
		{
			return "random <number of bytes>";
		}
	}
	
	public static class CommandExecutorPwd implements CommandExecutor
	{
		public void exec(String[] args)
		{
			try
			{
				System.out.println(SeaShell.currentDir.getCanonicalPath());
			}
			catch(IOException e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else e.printStackTrace();
			}
		}
		
		public String getHelp()
		{
			return "Prints the current working directory";
		}
		
		public String getUsage()
		{
			return "pwd";
		}
	}
	
	public static class CommandExecutorDigest implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			try
			{
				byte[] digest = SSUtil.digestFile(args[0], args[1]);
				if(digest != null) System.out.println(new String(digest, "UTF-8"));
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
		
		public String getHelp()
		{
			return "Prints the specified digest of the specified file using the charset specified in config.txt";
		}
		
		public String getUsage()
		{
			return "digest <digest> <file>";
		}
	}
	
	public static class CommandExecutorWrite implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			try
			{
				int argsLoc = 0;
				boolean append = (args[0].equals("-a") || args[0].equals("-append"));
				if(append) argsLoc++;
				File file = new File(args[argsLoc]);
				if(!file.isAbsolute()) file = new File(SeaShell.currentDir, args[argsLoc]);
				if(SeaShell.protectedFiles.contains(file))
				{
					System.out.println("Protected file: " + file.getName());
					return;
				}
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file, append));
				stream.write(args[1 + argsLoc].getBytes(SeaShell.charset));
				stream.close();
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
		
		public String getHelp()
		{
			return "Writes the specified string to the specified file, creating it if it does not already exist and appending to the end of the file if the -append (-a) switch is specified";
		}
		
		public String getUsage()
		{
			return "write [-append|-a] <file> <string>";
		}
	}
	
	public static class CommandExecutorBeep implements CommandExecutor
	{
		public void exec(String[] args)
		{
			java.awt.Toolkit.getDefaultToolkit().beep();
		}
		
		public String getHelp()
		{
			return "Emits a platform-dependent audio beep";
		}
		
		public String getUsage()
		{
			return "beep";
		}
	}
	
	public static class CommandExecutorDateTime implements CommandExecutor
	{
		public void exec(String[] args)
		{
			String formatString = "HH:mm:ss dd/MM/yyyy";
			
			if(args.length > 0)
			{
				formatString = SSUtil.concatenate(args, " ");
			}
			
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat(formatString);
			System.out.println(format.format(date));
		}
		
		public String getHelp()
		{
			return "Prints the current system date and time in the specified format, defaulting to HH:mm:ss dd/MM/yyyy";
		}
		
		public String getUsage()
		{
			return "datetime [format]";
		}
	}
	
	public static class CommandExecutorMilliTime implements CommandExecutor
	{
		public void exec(String[] args)
		{
			System.out.println(System.currentTimeMillis());
		}
		
		public String getHelp()
		{
			return "Prints the current system time in milliseconds";
		}
		
		public String getUsage()
		{
			return "millitime";
		}
	}
	
	public static class CommandExecutorNanoTime implements CommandExecutor
	{
		public void exec(String[] args)
		{
			System.out.println(System.nanoTime());
		}
		
		public String getHelp()
		{
			return "Prints the current system time in nanoseconds";
		}
		
		public String getUsage()
		{
			return "nanotime";
		}
	}
	
	public static class CommandExecutorUsage implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			//String command = SeaShell.localiser.localise(args[0]);
			if(SeaShell.commands.containsKey(SeaShell.localiser.localise(args[0]))) System.out.println(SeaShell.commands.get(SeaShell.localiser.localise(args[0])).getUsage());
			else System.out.println("Unknown command: " + args[0]);
		}
		
		public String getHelp()
		{
			return "Prints the usage of the specified command";
		}
		
		public String getUsage()
		{
			return "usage <command>";
		}
	}
	
	public static class CommandExecutorFor implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			String[] commands = SSUtil.copyFrom(args, 1);
			
			try
			{
				for(int i = 0; i < Integer.parseInt(args[0]); i++)
				{
					for(String s : commands)
					{
						SeaShell.execCommand(s);
					}
				}
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
		}
		
		public String getHelp()
		{
			return "Executes the specified commands the specified number of times";
		}
		
		public String getUsage()
		{
			return "for <times> <commands...>";
		}
	}
	
	public static class CommandExecutorPlugins implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			switch(args[0])
			{
				case "view":
					if(args.length < 2)
					{
						System.out.println("Usage: plugins view <ID>");
						return;
					}
					if(CommandLoader.plugins.containsKey(args[1]))
					{
						PluginDescriptor desc = CommandLoader.plugins.get(args[1]);
						System.out.println(desc.pluginID + ":");
						System.out.println("    Plugin author:\t" + desc.pluginAuthor);
						System.out.println("    Plugin version:\t" + desc.pluginVersion);
						System.out.println("    Filename:\t" + desc.filename);
					}
					else
					{
						System.out.println("No such plugin loaded");
					}
					return;
				case "list":
					if(!CommandLoader.plugins.isEmpty())
					{
						for(PluginDescriptor desc : CommandLoader.plugins.values())
						{
							System.out.println(desc.pluginID + ":");
							System.out.println("    Plugin author:\t" + desc.pluginAuthor);
							System.out.println("    Plugin version:\t" + desc.pluginVersion);
							System.out.println("    Filename:\t" + desc.filename);
						}
					}
					else
					{
						System.out.println("No plugins loaded");
					}
					return;
				default:
					System.out.println("Usage: " + getUsage());
					return;
					
			}
		}
		
		public String getHelp()
		{
			return "Displays information about plugins";
		}
		
		public String getUsage()
		{
			return "plugins <view|list|blacklist|extload> &[ID] &[name]";
		}
	}
	
	public static class CommandExecutorCopyAll implements CommandExecutor
	{
		private boolean useRegex;
		private Pattern pattern;
		
		public void exec(String[] args)
		{
			if(args.length < 2)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			if(args.length > 2)
			{
				useRegex = true;
				pattern = Pattern.compile(args[2]);
			}
			
			try
			{
				walkFolder(args[0], args[1]);
			}
			catch(Exception e)
			{
				if(SeaShell.debug) e.printStackTrace();
				else System.out.println(e);
			}
			
			useRegex = false;
			pattern = null;
		}
		
		private void walkFolder(String srcFolder, String destFolder) throws Exception
		{
			File file = new File(srcFolder);
			if(SeaShell.verbosityLevel > 1) System.out.println("Searching " + file.getCanonicalPath());
			File[] files = file.listFiles();
			if(files == null)
			{
				if(SeaShell.debug) System.out.println("listFiles() returning null for " + file.getCanonicalPath());
				return;
			}
			for(File f : files)
			{
				if(f.isDirectory()) walkFolder(f.getCanonicalPath(), destFolder);
				else if(!useRegex || pattern.matcher(f.getName()).matches()) copy(f, destFolder);
			}
		}
		
		private void copy(File file, String folder) throws Exception
		{
			if(SeaShell.verbosityLevel > 0) System.out.println("Copying " + file.getCanonicalPath() + " to " + folder);
			File destFile = new File(folder, file.getName());
			destFile.getParentFile().mkdirs();
			destFile.createNewFile();
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(folder, file.getName())));
			SSUtil.copyFully(in, out);
			in.close();
			out.close();
		}
		
		public String getHelp()
		{
			return "Copies every file in a directory tree (matching an optional regular expression) to another directory";
		}
		
		public String getUsage()
		{
			return "copyall <source> <destination> [regex]";
		}
	}
	
	public static class CommandExecutorVerbosity implements CommandExecutor
	{
		public void exec(String[] args)
		{
			if(args.length < 1)
			{
				System.out.println("Usage: " + getUsage());
				return;
			}
			
			SeaShell.verbosityLevel = SSUtil.clampInt(Integer.parseInt(args[0]), 0, 3);
			
			System.out.println("Verbosity level set to " + SeaShell.verbosityLevel);
		}
		
		public String getHelp()
		{
			return "Sets the verbosity level";
		}
		
		public String getUsage()
		{
			return "verbosity <level>";
		}
	}
	
	public static class CommandExecutorDebug implements CommandExecutor
	{
		public void exec(String[] args)
		{
			SeaShell.debug = !SeaShell.debug;
			
			System.out.println("Debug mode " + (SeaShell.debug ? "on" : "off"));
		}
		
		public String getHelp()
		{
			return "Toggles debug mode";
		}
		
		public String getUsage()
		{
			return "debug";
		}
	}
	
	public static class CommandExecutorCharset implements CommandExecutor
	{
		public void exec(String[] args)
		{
			Charset charset = SSUtil.getCharsetSilently(args[0], false);
			
			if(charset != null)
			{
				SeaShell.charset = charset;
				
				System.out.println("Changed charset to " + charset.displayName());
			}
			else
			{
				System.out.println("Charset not found");
			}
		}
		
		public String getHelp()
		{
			return "Changes the default charset to use in encoding strings";
		}
		
		public String getUsage()
		{
			return "charset <charset>";
		}
	}
}
