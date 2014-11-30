package net.condorcraft110.seashell;

import java.util.*;

public class SSAPI
{
	@Deprecated
	@SafeVarargs
	public static void registerUnlocalisedCommandExecutor(CommandExecutor exec, String absoluteCommand, String... absoluteAliases)
	{
		absoluteCommand = absoluteCommand.toLowerCase();
		SeaShell.unlocalisedCommands.put(absoluteCommand, exec);
		if(absoluteAliases.length > 0)
		{
			for(String s : absoluteAliases)
			{
				SeaShell.unlocalisedAliases.put(s, exec);
			}
			SeaShell.unlocalisedAliasStrings.put(absoluteCommand, absoluteAliases);
		}
		EventBus.fire(EventType.COMMAND_REGISTERED, exec, absoluteCommand, (Object)absoluteAliases);
	}
	
	@SafeVarargs
	public static void registerCommandExecutor(CommandExecutor exec, String command, String... aliases)
	{
		SeaShell.commands.put("command.root." + command.toLowerCase().trim(), exec);
		if(aliases.length > 0)
		{
			String[] ulAliases = SSUtil.addToBeginningOfAll("command.alias." + command + ".", aliases);
			for(String s : ulAliases)
			{
				SeaShell.aliases.put(s.toLowerCase().trim(), exec);
			}
			SeaShell.aliasStrings.put("command.root." + command, ulAliases);
		}
		EventBus.fire(EventType.COMMAND_REGISTERED, exec, command, (Object)aliases);
	}
	
	public static Scanner getStdinScanner()
	{
		return SeaShell.scanner;
	}
	
	public static String localise(String s)
	{
		return SeaShell.localiser.localise(s);
	}
	
	public static void addConstant(String name, String value)
	{
		if(!Constants.constantExists(name))
		{
			Constants.addConst(name, value);
		}
	}
	
	public static void removeConstant(String name)
	{
		if(!Constants.isSystemConstant(name))
		{
			Constants.removeConst(name);
		}
	}
}
