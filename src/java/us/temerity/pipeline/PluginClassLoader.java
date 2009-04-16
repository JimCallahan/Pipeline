// $Id: PluginClassLoader.java,v 1.6 2009/04/16 19:14:53 jlee Exp $

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

    pContents  = contents; 
    pResources = new TreeMap<String,Long>();
  }

  /**
   * Construct a new plugin loader.
   */
  public
  PluginClassLoader
  (
   TreeMap<String,byte[]> contents, 
   TreeMap<String,Long> resources, 
   PluginID pid, 
   PluginType ptype
  )
  {
    super();

    if(contents == null)
      throw new IllegalArgumentException
	("The class bytes table cannot be (null)!");

    if(resources == null)
      throw new IllegalArgumentException
	("The resources table cannot be (null)!");
    
    if(pid == null)
      throw new IllegalArgumentException
	("The PluginType cannot be (null)!");

    if(ptype == null)
      throw new IllegalArgumentException
	("The PluginID cannt be (null)!");

    pContents  = new TreeMap<String,byte[]>(contents);
    pResources = new TreeMap<String,Long>(resources);

    String vendor = pid.getVendor();
    String name   = pid.getName();
    VersionID vid = pid.getVersionID();

    Path rootPluginPath = new Path(PackageInfo.sInstPath, "plugins");

    Path pluginPath = 
      new Path(rootPluginPath, vendor + "/" + ptype + "/" + name + "/" + vid);

    pResourceRootPath = new Path(pluginPath, ".resources");

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "Resource path (" + pResourceRootPath + ") " + 
       "for Plugin (" + vendor + "/" + ptype + "/" + name + "/" + vid + ")");
  }

  /**
   * EXPERIMENTAL - for Jesse's Builders calling Builders outside of it's ClassLoader.
   */
  public
  PluginClassLoader
  (
   PluginClassLoader parentLoader, 
   PluginClassLoader childLoader
  )
  {
    if(parentLoader == null) {
      throw new IllegalArgumentException
	("The parent class loader cannot be (null)!");
    }

    if(childLoader == null) {
      throw new IllegalArgumentException
	("The child class loader cannot be (null)!");
    }

    pParentLoader = parentLoader;
    pChildLoader  = childLoader;
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
    /**
     * EXPERIMENTAL - for Jesse's Builders calling Builders outside of it's ClassLoader.
     */
    if(pParentLoader != null) {
      try {
	return pParentLoader.loadClass(cname);
      }
      catch(ClassNotFoundException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	   "Class (" + cname + ") not found in the parent ClassLoader.");
      }

      try {
	return pChildLoader.loadClass(cname);
      }
      catch(ClassNotFoundException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	   "Class (" + cname + ") not found in the child ClassLoader.");
      }

      throw new ClassNotFoundException
	("Unable to find class (" + cname + ")!");
    }

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
    /**
     * EXPERIMENTAL - for Jesse's Builders calling Builders outside of it's ClassLoader.
     */
    if(pParentLoader != null) {
      {
	URL parentResource = pParentLoader.getResource(rname);

	if(parentResource != null)
	  return parentResource;
      }
      {
	URL childResource = pChildLoader.getResource(rname);

	if(childResource != null)
	  return childResource;
      }

      return null;
    }

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
    /**
     * EXPERIMENTAL - for Jesse's Builders calling Builders outside of it's ClassLoader.
     */
    if(pParentLoader != null) {
      {
	long parentResourceSize = pParentLoader.getResourceSize(path);

	if(parentResourceSize != -1L)
	  return parentResourceSize;
      }
      {
	long childResourceSize = pChildLoader.getResourceSize(path);

	if(childResourceSize != -1L)
	  return childResourceSize;
      }

      return -1L;
    }

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
    // TODO if the experiment code for Jesse works, then figure out what goes here.

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
   * The resource directory for the Plugin.
   */
  private Path  pResourceRootPath;

  /**
   * EXPERIMENTAL - for Jesse's Builders calling Builders outside of it's ClassLoader.
   */
  private PluginClassLoader  pParentLoader;
  private PluginClassLoader  pChildLoader;

}

