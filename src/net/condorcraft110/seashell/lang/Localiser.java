package net.condorcraft110.seashell.lang;

import java.io.*;
import java.util.*;

public class Localiser
{
	private static HashMap<String, Language> languageRegistry = new HashMap<>();
	
	private final Language currentLang;
	
	public Localiser(String language) throws LanguageNotSupportedException, IOException
	{
		if(languageRegistry.containsKey(language))
		{
			currentLang = languageRegistry.get(language);
			if(!currentLang.isInitialised()) currentLang.load();
		}
		else
		{
			throw new LanguageNotSupportedException(language);
		}
	}
	
	public String localise(String s)
	{
		String loc = s;
		if(currentLang.containsKey(s)) loc = currentLang.get(s);
		return loc;
	}
	
	public static void registerLanguage(String id, Language data)
	{
		languageRegistry.put(id, data);
	}
	
	public static void registerLocalisation(String a, String b, String lang) throws LanguageNotSupportedException
	{
		if(languageRegistry.containsKey(lang))
		{
			languageRegistry.get(lang).add(a, b);
		}
		else
		{
			throw new LanguageNotSupportedException(lang);
		}

	}
	
	/*public void setDefault()
	{
		Locale.setDefault(new Locale.Builder().setLanguage(currentLangSplit[0]).setRegion(currentLangSplit[1]).build());
	}*/
}
