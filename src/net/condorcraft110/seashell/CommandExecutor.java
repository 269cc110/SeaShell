package net.condorcraft110.seashell;

public interface CommandExecutor
{
	void exec(String[] args);
	String getHelp();
	String getUsage();
}
