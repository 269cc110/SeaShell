package net.condorcraft110.seashell;

import java.security.*;
import java.io.*;

public class PluginSecurityManager extends SecurityManager
{
	@SuppressWarnings("deprecation")
	public void checkPermission(Permission perm)
	{
		if(currentClassLoader() instanceof PluginClassLoader)
		{
			if(perm instanceof FilePermission)
			{
				if(!SSUtil.getPath(new File(perm.getName())).startsWith(SSUtil.getPath(new File("plugins"))))
					throw new SecurityException("Only I/O in the 'plugins' directory is permitted for untrusted plugins");
			}
			else if(perm instanceof RuntimePermission)
			{
				if(perm.getName().toLowerCase().contains("classloader")
						|| perm.getName().toLowerCase().contains("securitymanager")
						|| perm.getName().equalsIgnoreCase("setio")
						|| perm.getName().equalsIgnoreCase("accessdeclaredmembers")
						|| perm.getName().equalsIgnoreCase("getstacktrace")
						|| perm.getName().equalsIgnoreCase("usepolicy"))
				{
					throw new SecurityException("Permission denied: " + perm.toString());
				}
			}
		}
		
		//super.checkPermission(perm);
	}
}
