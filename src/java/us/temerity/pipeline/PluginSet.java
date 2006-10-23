// $Id: PluginSet.java,v 1.1 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   S E T                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A common datastructure for exchanging the vendors, names and revision numbers of plugins 
 * associated with a toolset or toolset package.
 */
public
class PluginSet
 extends DoubleMap<String,String,TreeSet<VersionID>> 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an empty set.
   */ 
  public 
  PluginSet() 
  {
    super();
  }  

  /**
   * Deep copy constructor. 
   */ 
  public 
  PluginSet
  (
   PluginSet pset
  )
  {
    super();
    addAll(pset);
  }  

  /**
   * Deep copy constructor. 
   */ 
  public 
  PluginSet
  (
   DoubleMap<String,String,TreeSet<VersionID>> table
  )
  {
    super();

    for(String vendor : table.keySet()) {
      for(String name : table.get(vendor).keySet()) 
	addPlugins(vendor, name, table.get(vendor, name));
    }
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all plugin vendors in the set.
   */ 
  public Set<String>
  getVendors() 
  {
    return keySet();
  }

  /**
   * Get the names of all plugin names for the given vendor in the set.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   */ 
  public Set<String>
  getNames
  (
   String vendor
  ) 
  {
    return keySet(vendor);
  }
  
  /**
   * Get the revision numbers of all plugins with the given vendor and name in the set.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param name
   *   The name of the plugin.
   */ 
  public Set<VersionID> 
  getVersions
  (
   String vendor, 
   String name
  ) 
  {
    return get(vendor, name);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a plugin to the set.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param name
   *   The name of the plugin.
   * 
   * @param vid
   *   The revision number of the plugin.
   */ 
  public void
  addPlugin
  (
   String vendor, 
   String name, 
   VersionID vid
  ) 
  {
    TreeSet<VersionID> evids = get(vendor, name);
    if(evids == null) {
      evids = new TreeSet<VersionID>();
      put(vendor, name, evids);
    }

    evids.add(vid);	
  }

  /**
   * Add several versions of a plugin to the set.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param name
   *   The name of the plugin.
   * 
   * @param vids
   *   The revision numbers of the plugins.
   */ 
  public void
  addPlugins
  (
   String vendor, 
   String name, 
   TreeSet<VersionID> vids
  ) 
  {
    TreeSet<VersionID> evids = get(vendor, name);
    if(evids == null) {
      evids = new TreeSet<VersionID>();
      put(vendor, name, evids);
    }

    evids.addAll(vids);	
  }
  
  /**
   * Add all plugins from another plugin set to this one.
   * 
   * @param pset
   *   The plugin set to add. 
   */ 
  public void
  addAll
  ( 
   PluginSet pset
  ) 
  {
    for(String vendor : pset.keySet()) {
      for(String name : pset.get(vendor).keySet()) 
	addPlugins(vendor, name, pset.get(vendor, name));
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8260605213854536274L;
  
}
