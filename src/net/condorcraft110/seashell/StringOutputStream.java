package net.condorcraft110.seashell;

import java.io.*;

public class StringOutputStream extends ByteArrayOutputStream
{
	public void write(String s) throws UnsupportedEncodingException, IOException
	{
		write(s.getBytes(SeaShell.charset));
	}
	
	public String getOutput()
	{
		return new String(toByteArray(), SeaShell.charset);
	}
}
