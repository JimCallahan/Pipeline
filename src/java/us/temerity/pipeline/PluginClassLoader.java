// $Id: PluginClassLoader.java,v 1.3 2009/03/26 06:48:37 jlee Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;
import java.net.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   C L A S S   L O A D E R                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A class loader which loads plugin classes from an array of bytes.
 */
public
class PluginClassLoader
  extends ClassLoader
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new plugin loader.
   * 
   * @param contents
   *   The raw plugin class bytes indexed by class name.
   */
  public 
  PluginClassLoader
  (
   TreeMap<String,byte[]> contents
  ) 
  {
    super();

    pContents = contents; 
  }

  /**
   *
   */
  public
  PluginClassLoader
  (
   TreeMap<String,byte[]> contents, 
   String cname, 
   TreeMap<String,Long> resources 
  )
  {
    super();

    if(contents == null)
      throw new IllegalArgumentException();

    if(resources == null)
      throw new IllegalArgumentException();
    
    if(cname == null)
      throw new IllegalArgumentException();

    pContents  = new TreeMap<String,byte[]>(contents);
    pResources = new TreeMap<String,Long>(resources);

    Path cpath = new Path(cname.replace('.', '/'));

    Path pluginPath = new Path
      (new Path(PackageInfo.sInstPath, "plugins"), 
       cpath.getParentPath());

    pResourceRootPath = new Path(pluginPath, "resources");

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "Resource path (" + pResourceRootPath + ") for plugin (" + cname + ")");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L A S S   L O A D E R   O V E R R I D E S                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Finds the specified class. 
   */
  protected Class 
  findClass
  (
   String cname
  ) 
    throws ClassNotFoundException
  {
    byte bs[] = pContents.get(cname);
    if(bs == null) 
      throw new ClassNotFoundException
	("Unable to find class (" + cname + ")!");

    return defineClass(cname, bs, 0, bs.length);
  }

  /**
   * Finds the specified resource.
   */
  protected URL
  findResource
  (
   String rname
  )
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
       "Resource name (" + rname + ")");

    if(pResources != null) {
      if(pResources.containsKey(rname)) {
	Path resourcePath = new Path(pResourceRootPath, rname);

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	   "Resource path (" + resourcePath + ")");

	File resourceFile = resourcePath.toFile();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	   "Resource file (" + resourceFile + ") exists (" + resourceFile.exists() + ")");

	if(!resourceFile.exists())
	  return null;

	URI resourceURI = resourceFile.toURI();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	   "Resource URI (" + resourceURI + ")");

	try {
	  URL resourceURL = resourceURI.toURL();

	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	     "Resource URL (" + resourceURL + ")");

	  return resourceURL;
	}
	catch(MalformedURLException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
	     "Error constructing URL from (" + rname + ")!");
	}
      }
    }

    return null;
  }

  /**
   * Returns the file size for valid resource.
   */
  public long
  getResourceSize
  (
   String path
  )
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
       "Path (" + path + ")");

    if(pResources != null) {
      if(pResources.containsKey(path))
	return pResources.get(path);
      else
	return -1L;
    }

    return -1L;
  }

  /**
   * Returns all resources available to the plugin.
   */
  public SortedMap<String,Long>
  getResources()
  {
    if(pResources != null) {
      return Collections.unmodifiableSortedMap(pResources);
    }

    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The raw plugin class bytes indexed by class name.
   */ 
  private TreeMap<String,byte[]>  pContents; 

  /**
   * The resource file sizes indexed by class name.
   */
  private TreeMap<String,Long>  pResources;

  /**
   *
   */
  private Path  pResourceRootPath;

}

