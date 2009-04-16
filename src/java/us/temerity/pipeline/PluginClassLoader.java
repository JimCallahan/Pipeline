// $Id: PluginClassLoader.java,v 1.7 2009/04/16 20:13:17 jesse Exp $

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
   * 
   * @param parentLoader
   *   The parent class loader. If this is <code>null</code> then the default System
   *   Class Loader will be used.
   */
  public 
  PluginClassLoader
  (
    TreeMap<String,byte[]> contents, 
    ClassLoader parentLoader
  ) 
  {
    super(parentLoader == null ? getSystemClassLoader() : parentLoader);

    pContents  = contents; 
    pResources = new TreeMap<String,Long>();
  }

  /**
   * Construct a new plugin loader.
   * 
   * @param contents
   *   The raw plugin class bytes indexed by class name.
   * 
   * @param parentLoader
   *   The parent class loader. If this is <code>null</code> then the default System
   *   Class Loader will be used.
   */
  public
  PluginClassLoader
  (
   TreeMap<String,byte[]> contents, 
   TreeMap<String,Long> resources, 
   PluginID pid, 
   PluginType ptype, 
   ClassLoader parentLoader
  )
  {
    super(parentLoader == null ? getSystemClassLoader() : parentLoader);

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
   * Construct a new plugin loader.
   * 
   * @param childLoader
   *   The childLoader that is going be used to extend the parent loader.
   * 
   * @param parentLoader
   *   The parent class loader. If this is <code>null</code> then the default System
   *   Class Loader will be used.
   */
  public 
  PluginClassLoader
  (
    PluginClassLoader childLoader, 
    ClassLoader parentLoader
  ) 
  {
    this(new TreeMap<String, byte[]>(childLoader.pContents), parentLoader);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C L A S S   L O A D E R   O V E R R I D E S                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Finds the specified class. 
   */
  @Override
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
  @Override
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
    
    long toReturn = -1L;
    
    ClassLoader parent = getParent();
    if (parent != null && parent instanceof PluginClassLoader) {
      PluginClassLoader pparent = (PluginClassLoader) parent;
      toReturn = pparent.getResourceSize(path);
    }
    
    if (toReturn == -1L) {
      if(pResources != null) {
        if(pResources.containsKey(path))
          toReturn = pResources.get(path);
      }
    }

    return toReturn;
  }

  /**
   * Returns all resources available to the plugin.
   */
  public SortedMap<String,Long>
  getResources()
  {
    TreeMap<String, Long> toReturn = new TreeMap<String, Long>();
    
    getResourcesHelper(toReturn);
    
    if(!toReturn.isEmpty()) {
      return Collections.unmodifiableSortedMap(toReturn);
    }

    return null;
  }

  private void
  getResourcesHelper
  (
    TreeMap<String, Long> resources
  )
  {
    ClassLoader parent = getParent();
    if (parent != null && parent instanceof PluginClassLoader) {
      PluginClassLoader pparent = (PluginClassLoader) parent;
      pparent.getResourcesHelper(resources);
    }
    
    if (pResources != null) {
      for (String res : pResources.keySet()) {
        if (!resources.containsKey(res)) {
          resources.put(res, pResources.get(res));
        }   
      }
    }
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
  
}

