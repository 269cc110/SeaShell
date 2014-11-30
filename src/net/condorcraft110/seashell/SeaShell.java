package net.condorcraft110.seashell;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.nio.charset.*;
import net.condorcraft110.seashell.csc.*;
import net.condorcraft110.seashell.lang.*;
//import net.condorcraft110.seashell.server.SSServer;
import net.condorcraft110.seashell.util.config.*;
import net.condorcraft110.seashell.SSCommandExecutors.*;

public class SeaShell
{
	static Scanner scanner;
	private static boolean initialised = false;
	static TreeMap<String, CommandExecutor> commands;
	static TreeMap<String, CommandExecutor> aliases;
	static HashMap<String, String[]> aliasStrings;
	static TreeMap<String, CommandExecutor> unlocalisedCommands;
	static TreeMap<String, CommandExecutor> unlocalisedAliases;
	static TreeMap<String, String[]> unlocalisedAliasStrings;
	private static HashMap<String, CommandExecutor> easterEggs;
	public static File currentDir = new File(".");
	public static final String SS_VERSION = "0.4.4";
	public static final String CSC_VERSION = "0.1.4";
	public static final String API_VERSION = "1";
	static boolean updatesAvailable = false; //unused
	//public static final Properties cfg = SSUtil.readProperties("config.txt");
	public static final Config config = new Config("config.txt");
	static Localiser localiser;
	public static final String LANG = config.get("lang", "en_GB");
	static ArrayList<String> errorMessages;
	public static boolean debug = config.getBoolean("debug", false);
	public static boolean logCommands = config.getBoolean("log-commands", true);
	public static final PrintStream STDOUT = System.out;
	static ArrayList<File> protectedFiles;
	public static final String AUTHOR = "condorcraft110";
	public static Charset charset = SSUtil.getCharsetSilently(config.get("charset", "UTF-8"), true);
	public static int verbosityLevel = config.getInt("verbosity", 0);
	
	public static void main(String[] args) throws Exception
	{
		int argsLoc = 0;
		if(System.console() == null)
		{
			if(args.length < 1)
			{
				consoleError();
			}
			else if(!args[0].equals("-t"))
			{
				consoleError();
			}
			else
			{
				argsLoc++;
			}
		}
		try
		{
			init();
			if(args.length > argsLoc)
			{
				if(args[argsLoc].equals("-h") || args[argsLoc].equals("-help"))
				{
					System.out.println("SeaShell " + SS_VERSION + " help:");
					System.out.println("    -h, -help: displays this help message");
					System.out.println("    -s, -script: runs the specified CSC script");
					System.out.println("    -c, -command: executes the specified command");
				}
				else if(args[argsLoc].equals("-s") || args[argsLoc].equals("-script"))
				{
					CSC.runScript(new File(args[0]), true, SSUtil.copyFrom(args, 1));
				}
				else if(args[argsLoc].equals("-c") || args[argsLoc].equals("-command"))
				{
					String[] cmd = SSUtil.copyFrom(args, argsLoc + 1);
					cmd[0] = "\"" + cmd[0];
					cmd[cmd.length - 1] += "\"";
					execCommand(SSUtil.concatenate(cmd, "\" \""));
				}
				else
				{
					System.out.println("SeaShell " + SS_VERSION + " help:");
					System.out.println("    -h, -help: displays this help message");
					System.out.println("    -s, -script: runs the specified CSC script");
					System.out.println("    -c, -command: executes the specified command");
				}
				execCommand("exit");
			}
			start();
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			if(debug) e.printStackTrace();
			else System.out.println(e);
		}
	}
	
	private static void init() throws Exception
	{
		if(initialised) return;
		//convertToXML("config.txt");
		System.setErr(System.out); // fixes problems with plugins printing to stderr
		registerLanguages();
		localiser = new Localiser(LANG);
		//localiser.setDefault();
		SSLogger.start();
		SSLogger.log(localiser.localise("message.init").replace("$SSVERSION", SS_VERSION).replace("$APIVERSION", API_VERSION).replace("$CSCVERSION", CSC_VERSION));
		commands = new TreeMap<String, CommandExecutor>();
		aliases = new TreeMap<String, CommandExecutor>();
		aliasStrings = new HashMap<String, String[]>();
		unlocalisedCommands = new TreeMap<String, CommandExecutor>();
		unlocalisedAliases = new TreeMap<String, CommandExecutor>();
		unlocalisedAliasStrings = new TreeMap<String, String[]>();
		errorMessages = new ArrayList<String>();
		easterEggs = new HashMap<>();
		scanner = new Scanner(System.in);
		protectedFiles = new ArrayList<File>();
		registerCommands();
		registerEasterEggs();
		registerProtectedFiles();
		Constants.init();
		Variables.init();
		if(!config.getBoolean("disable-plugins", false)) loadPlugins();
		SSLogger.log(localiser.localise("message.init.script"));
		Sys.runStartupScript();
		initialised = true;
		SSLogger.log(localiser.localise("message.init.done"));
	}
	
	private static void start() throws Exception
	{
		checkUpdate();
		System.out.println(localiser.localise("message.seashell").replace("$SSVERSION", SS_VERSION).replace("$CMDCOUNT", "" + (commands.size() + unlocalisedCommands.size())).replace("$ERRCOUNT", "" + CommandLoader.errors));
		loop();
	}
	
	private static void loadPlugins() throws LanguageNotSupportedException
	{
		System.setSecurityManager(new PluginSecurityManager());
		File file = new File("plugins");
		if(file.exists() && file.isDirectory())
		{
			File coreFile = new File(file, "core");
			if(coreFile.exists() && coreFile.isDirectory())
			{
				File[] listedFiles = coreFile.listFiles();
				if(listedFiles.length > 0)
				{
					for(File f : listedFiles)
					{
						if(f.getName().toLowerCase().endsWith(".jar") || f.getName().toLowerCase().endsWith(".zip"))
						{
							CommandLoader.loadJarPlugin(f);
						}
					}
				}
			}
			else if(!file.exists()) file.mkdirs();
			else if(file.exists() && !file.isDirectory()) System.out.println(localiser.localise("message.plugin.nodir"));
			
			File[] listedFiles = file.listFiles();
			int i = listedFiles.length;
			if(i > 0)
			{
				for(File f : listedFiles)
				{
					if(f.getName().toLowerCase().endsWith(".jar") || f.getName().toLowerCase().endsWith(".zip"))
					{
						CommandLoader.loadJarPlugin(f);
					}
					/*else if(f.getName().toLowerCase().endsWith(".class"))
					{
						CommandLoader.loadClassPlugin(f);
					}*/ //TODO Figure out for event system
				}
			}
			else
			{
				SSLogger.log(localiser.localise("message.plugin.none"));
			}
		}
		else if(!file.exists()) file.mkdirs();
		else if(file.exists() && !file.isDirectory()) System.out.println(localiser.localise("message.plugin.nodir"));
		//SSServer.INSTANCE.init(new PluginInitEvent(""));
	}

	private static void registerCommands()
	{
		if(initialised) return;
		SSAPI.registerCommandExecutor(new CommandExecutorExit(), "exit", "quit");
		SSAPI.registerCommandExecutor(new CommandExecutorShell(), "shell");
		SSAPI.registerCommandExecutor(new CommandExecutorHelp(), "help");
		SSAPI.registerCommandExecutor(new CommandExecutorChDir(), "chdir", "cd");
		SSAPI.registerCommandExecutor(new CommandExecutorCSC(), "csc", "script");
		SSAPI.registerCommandExecutor(new CommandExecutorEcho(), "echo");
		SSAPI.registerCommandExecutor(new CommandExecutorCopy(), "copy", "cp");
		SSAPI.registerCommandExecutor(new CommandExecutorDelete(), "delete", "rm", "remove");
		SSAPI.registerCommandExecutor(new CommandExecutorMkDir(), "mkdir", "md", "make");
		SSAPI.registerCommandExecutor(new CommandExecutorVersion(), "version", "ver");
		SSAPI.registerCommandExecutor(new CommandExecutorDownload(), "download", "dl", "wget");
		SSAPI.registerCommandExecutor(new CommandExecutorDirectory(), "directory", "dir");
		SSAPI.registerCommandExecutor(new CommandExecutorRandomFile(), "randomfile", "randfile", "rfile", "randomf", "randf", "rf");
		SSAPI.registerCommandExecutor(new CommandExecutorAlias(), "alias");
		SSAPI.registerCommandExecutor(new CommandExecutorPrint(), "print");
		SSAPI.registerCommandExecutor(new CommandExecutorSeaShell(), "seashell");
		//SSAPI.registerCommandExecutor(new CommandExecutorUpdate(), "update"); // obsolete
		SSAPI.registerCommandExecutor(new CommandExecutorErrors(), "errors", "errs", "err");
		SSAPI.registerCommandExecutor(new CommandExecutorClear(), "clear", "clr", "cls");
		SSAPI.registerCommandExecutor(new CommandExecutorSet(), "set");
		SSAPI.registerCommandExecutor(new CommandExecutorSetp(), "setp");
		SSAPI.registerCommandExecutor(new CommandExecutorAdd(), "add", "plus");
		SSAPI.registerCommandExecutor(new CommandExecutorSubtract(), "subtract", "subtr", "minus", "takeaway");
		SSAPI.registerCommandExecutor(new CommandExecutorMultiply(), "multiply", "mltply", "times");
		SSAPI.registerCommandExecutor(new CommandExecutorDivide(), "divide", "div");
		SSAPI.registerCommandExecutor(new CommandExecutorIf(), "if");
		//SSAPI.registerCommandExecutor(new CommandExecutorSeta(), "seta"); //FIXME
		//SSAPI.registerCommandExecutor(new CommandExecutorSetap(), "setap"); //FIXME
		SSAPI.registerCommandExecutor(new CommandExecutorInput(), "input", "in");
		SSAPI.registerCommandExecutor(new CommandExecutorSleep(), "sleep", "wait");
		SSAPI.registerCommandExecutor(new CommandExecutorDel(), "del");
		SSAPI.registerCommandExecutor(new CommandExecutorPwd(), "pwd");
		SSAPI.registerCommandExecutor(new CommandExecutorDigest(), "digest");
		SSAPI.registerCommandExecutor(new CommandExecutorWrite(), "write");
		SSAPI.registerCommandExecutor(new CommandExecutorBeep(), "beep");
		SSAPI.registerCommandExecutor(new CommandExecutorDateTime(), "datetime", "date", "time", "dt");
		SSAPI.registerCommandExecutor(new CommandExecutorMilliTime(), "millitime", "milli", "mt");
		SSAPI.registerCommandExecutor(new CommandExecutorNanoTime(), "nanotime", "nano", "nt");
		SSAPI.registerCommandExecutor(new CommandExecutorUsage(), "usage");
		SSAPI.registerCommandExecutor(new CommandExecutorFor(), "for");
		SSAPI.registerCommandExecutor(new CommandExecutorPlugins(), "plugins");
		SSAPI.registerCommandExecutor(new CommandExecutorCopyAll(), "copyall", "copya", "ca");
		SSAPI.registerCommandExecutor(new CommandExecutorVerbosity(), "verbosity", "verbose");
		SSAPI.registerCommandExecutor(new CommandExecutorDebug(), "debug");
	}
	
	private static void registerLanguages() throws Exception
	{
		if(initialised) return;
		File file = new File("lang");
		if(file.exists() && file.isDirectory())
		{
			File[] listedFiles = file.listFiles();
			int i = listedFiles.length;
			if(i > 0)
			{
				for(File f : listedFiles)
				{
					if(f.getName().toLowerCase().endsWith(".lang"))
					{
						String name = f.getName().substring(0, f.getName().length() - 5);
						Localiser.registerLanguage(name, new Language("lang" + File.separator + f.getName()));
						Localiser.registerLocalisation("command.root.sss", "ssssss", name);
						Localiser.registerLocalisation("command.root.e", "e", name);
					}
				}
			}
			else
			{
				SSLogger.log("Error: no languages found");
				System.out.println("No languages found");
				shutdown(false);
				SSLogger.exit();
				System.exit(1);
			}
		}
		else
		{
			SSLogger.log("Error: no languages found");
			System.out.println("No languages found");
			shutdown(false);
			SSLogger.exit();
			System.exit(1);
		}
	}
	
	private static void registerEasterEggs()
	{
		if(initialised) return;
		easterEggs.put("command.root.sss", new EEsss());
		easterEggs.put("command.root.e", new EEE());
	}
	
	private static void registerProtectedFiles()
	{
		if(initialised) return;
		protectedFiles.add(new File("persistence.dat"));
	}
	
	private static void loop() throws Exception
	{
		while(true)
		{
			System.out.print(localiser.localise("message.prompt"));
			String cmd = scanner.nextLine();
			execCommand(cmd);
		}
	}
	
	public static void execCommand(String command, String failMessage, boolean echocmd)
	{
		String[] cmd = SSUtil.formatCommand(command);
		EventBus.fire(EventType.COMMAND_STARTED, command, (Object)cmd, failMessage, echocmd);
		if(cmd.length < 1)
		{
			return;
		}
		if(logCommands) SSLogger.log(localiser.localise("message.command.executed") + command);
		if(cmd[0].startsWith("!"))
		{
			echocmd = !echocmd;
			cmd[0] = cmd[0].substring(1);
		}
		String theCommand = localiser.localise(cmd[0].trim().toLowerCase());
		if(theCommand.equals(""))
		{
			System.out.println(failMessage.replace("$CMD", cmd[0]));
		}
		else if(easterEggs.containsKey(theCommand))
		{
			if(echocmd) System.out.println(command + ":");
			easterEggs.get(theCommand).exec(SSUtil.copyFrom(cmd, 1));
		}
		else if(commands.containsKey(theCommand))
		{
			if(echocmd) System.out.println(command + ":");
			commands.get(theCommand).exec(SSUtil.copyFrom(cmd, 1));
		}
		else if(aliases.containsKey(theCommand))
		{
			if(echocmd) System.out.println(command + ":");
			aliases.get(theCommand).exec(SSUtil.copyFrom(cmd, 1));
		}
		else if(commands.containsKey(Variables.fromAlias(theCommand)))
		{
			if(echocmd) System.out.println(command + ":");
			commands.get(Variables.fromAlias(theCommand)).exec(SSUtil.copyFrom(cmd, 1));
		}
		else if(aliases.containsKey(Variables.fromAlias(theCommand)))
		{
			if(echocmd) System.out.println(command + ":");
			aliases.get(Variables.fromAlias(theCommand)).exec(SSUtil.copyFrom(cmd, 1));
		}
		else
		{
			System.out.println(failMessage);
			EventBus.fire(EventType.COMMAND_FAILED, command, theCommand, (Object)cmd, failMessage, echocmd);
			return;
		}
		EventBus.fire(EventType.COMMAND_EXECUTED, command, theCommand, (Object)cmd, failMessage, echocmd);
	}
	
	public static String execCommandToString(String command, String failMessage, boolean echocmd)
	{
		StringOutputStream out = new StringOutputStream();
		System.setOut(new PrintStream(out));
		execCommand(command, failMessage, echocmd);
		System.setOut(STDOUT);
		return out.getOutput();
	}
	
	public static boolean isInitialised()
	{
		return initialised;
	}
	
	public static void execCommand(String command)
	{
		execCommand(command, localiser.localise("message.command.unknown"), false);
	}
	
	public static String execCommandToString(String command)
	{
		return execCommandToString(command, localiser.localise("message.command.unknown.nested"), false);
	}
	
	public static boolean localisedCommandExists(String command, int searchMode)
	{
		String theCommand = localiser.localise(command);
		
		switch(searchMode)
		{
			case 0:
				return commands.containsKey(theCommand);
			case 1:
				return aliases.containsKey(theCommand);
			case 2:
				return commands.containsKey(theCommand) || aliases.containsKey(theCommand);
			default:
				return false;
		}
	}
	
	public static boolean hasAliases(String command)
	{
		String theCommand = localiser.localise(command);
		return aliasStrings.containsKey(theCommand);
	}
	
	static void shutdown(boolean runScript)
	{
		SSLogger.log(localiser.localise("message.shutdown"));
		
		try
		{
			EventBus.fire(EventType.SEASHELL_SHUTDOWN, runScript);
			
			for(PluginDescriptor plugin : CommandLoader.plugins.values())
			{
				plugin.loader.close();
			}
		}
		catch(Exception e)
		{
			SSLogger.log(localiser.localise("message.shutdown.error").replace("$ERRMSG", e.toString()));
		}
		
		if(runScript)
		{
			SSLogger.log(localiser.localise("message.shutdown.script"));
			Sys.runShutdownScript();
		}
		
		//config.save(true);
	}
	
	private static void consoleError() throws Exception
	{
		//UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JOptionPane.showMessageDialog(null, "SeaShell must be launched from a console", "Error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
	
	private static void checkUpdate() throws Exception
	{
		try
		{
			URLConnection conn = new URL("http://files.condorcraft110.net/seashell/version.dat").openConnection();
			conn.connect();
			Scanner scan = new Scanner(conn.getInputStream());
			String latestVersion = scan.nextLine();
			if(!latestVersion.equals(SS_VERSION))
			{
				System.out.print("Updates available. Download ZIP (Y|N)? ");
				String response = scanner.nextLine();
				if(response.trim().equalsIgnoreCase("y") || response.trim().equalsIgnoreCase("yes") || response.trim().equalsIgnoreCase("true") || response.trim().equalsIgnoreCase("yeah"))
				{
					System.out.println("Downloading");
					SSUtil.download("http://files.condorcraft110.net/seashell/releases/seashell_" + scan.nextLine() + ".zip", System.getProperty("user.dir"));
				}
				else
				{
					System.out.println("Aborting");
				}
				// FIXME Corrupts ZIP
			}
			scan.close();
		}
		catch(Exception e)
		{
			SSLogger.log(e.toString());
		}
	}
	
	private static final class EEsss implements CommandExecutor
	{
		public void exec(String[] args)
		{
			System.out.println("BOOM");
		}
		
		public String getHelp()
		{
			return null;
		}
		
		public String getUsage()
		{
			return null;
		}
	}
	
	private static final class EEE implements CommandExecutor
	{
		public void exec(String[] args)
		{
			System.out.println("E!");
		}
		
		public String getHelp()
		{
			return null;
		}
		
		public String getUsage()
		{
			return null;
		}
	}
}
