// $Id: DownstreamLinks.java,v 1.1 2004/03/29 08:18:22 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D O W N S T R E A M   L I N K S                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The downstream links associated with all working and checked-in versions of a node. <P> 
 * 
 * This class is used to cache the downstream linkage information for a node in order to 
 * avoid doing deep searches of all nodes.  All of this information is redundant as it can 
 * be regenerated (with a considerable amount of effort) from the {@link NodeMod NodeMod} 
 * and {@link NodeVersion NodeVersion} instances. <P> 
 * 
 * The {@link NodeMgr NodeMgr} class is responsible for keeping the linkage information  
 * contained in instances of this class up to date with the <CODE>NodeMod</CODE> and 
 * <CODE>NodeVersion</CODE> instances.  The <CODE>NodeMgr</CODE> class is also responsible 
 * for reading and writing the Glue format text files which store persistant instance of 
 * this class on disk.
 * 
 * @see NodeVersion
 * @see NodeMod
 * @see NodeMgr
 */
public
class DownstreamLinks
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  DownstreamLinks() 
  {
    pWorkingLinks   = new HashMap<NodeID,TreeSet<String>>();
    pCheckedInLinks = new HashMap<VersionID,TreeMap<String,VersionID>>();
  }

  /**
   * Construct an empty set of downstream links.
   * 
   * @param name 
   *   The fully resolved name of the node.
   */ 
  public 
  DownstreamLinks
  (
   String name 
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    pWorkingLinks   = new HashMap<NodeID,TreeSet<String>>();
    pCheckedInLinks = new HashMap<VersionID,TreeMap<String,VersionID>>();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the fully resolved name of the node.
   */ 
  public String
  getName() 
  {
    assert(pName != null);
    return pName;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the names of the nodes connected by a downstream link to the given working version.
   * 
   * @param id
   *   The unique working version identifier.
   * 
   * @return
   *   The names of the downstream nodes.
   */
  public TreeSet<String>
  getWorking
  (
   NodeID id
  ) 
  {
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    assert(id.getName().equals(pName)); 

    TreeSet<String> links = pWorkingLinks.get(id);
    if(links != null)
      return new TreeSet<String>(links);
    return new TreeSet<String>();
  }
  
  /** 
   * Add the name of a node connected by a downstream link to the given working version.
   * 
   * @param id
   *   The unique working version identifier.
   * 
   * @param name 
   *   The fully resolved name of the downstream node.
   */
  public void 
  addWorking
  (
   NodeID id,
   String name
  ) 
  {
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    assert(id.getName().equals(pName)); 
    
    if(name == null) 
      throw new IllegalArgumentException
	("The downstream node name cannot be (null)!");

    TreeSet<String> links = pWorkingLinks.get(id);
    if(links == null) {
      links = new TreeSet<String>();
      pWorkingLinks.put(id, links);
    }
    links.add(name);
  }

  /** 
   * Remove the name of a node connected by a downstream link to the given working version.
   * 
   * @param id
   *   The unique working version identifier.
   * 
   * @param name 
   *   The fully resolved name of the downstream node.
   */
  public void 
  removeWorking
  (
   NodeID id,
   String name
  ) 
  {
    if(id == null) 
      throw new IllegalArgumentException
	("The working version ID cannot be (null)!");
    assert(id.getName().equals(pName));  

    if(name == null) 
      throw new IllegalArgumentException
	("The downstream node name cannot be (null)!");
  
    TreeSet<String> links = pWorkingLinks.get(id);
    if(links != null) 
      links.remove(name);

    if(links.isEmpty()) 
      pWorkingLinks.remove(id);
  }


  /*----------------------------------------------------------------------------------------*/

  /**  
   * Get the revision numbers indexed by node name of the checked-in nodes connected by a 
   * downstream link to the checked-in version with the given revision number.
   * 
   * @param vid 
   *   The revision number of the checked-in node version.
   * 
   * @return
   *   The table of revision numbers indexed by the names of the downstream nodes.
   */
  public TreeMap<String,VersionID>
  getCheckedIn
  (
   VersionID vid 
  ) 
  {
    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");
    
    TreeMap<String,VersionID> links = pCheckedInLinks.get(vid);
    if(links != null)
      return new TreeMap<String,VersionID>(links);
    return new TreeMap<String,VersionID>();
  }
  
  /** 
   * Add the name and revision of a checked-in version of a node connected by a downstream 
   * link to the checked-in version with the given revision number.
   * 
   * @param vid 
   *   The revision number of the checked-in node version.
   * 
   * @param name 
   *   The fully resolved name of the downstream node.
   * 
   * @param dvid
   *   The revision number of the downstream checked-in node version.
   */
  public void 
  addCheckedIn
  (
   VersionID vid,
   String name,
   VersionID dvid 
  ) 
  {
    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");

    if(name == null) 
      throw new IllegalArgumentException
	("The downstream node name cannot be (null)!");
    
    if(dvid == null) 
      throw new IllegalArgumentException
	("The downstream revision number cannot be (null)!");

    TreeMap<String,VersionID> links = pCheckedInLinks.get(vid);
    if(links == null) {
      links = new TreeMap<String,VersionID>();
      pCheckedInLinks.put(vid, links);
    }
    links.put(name, dvid);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("Name", pName);

    if(!pWorkingLinks.isEmpty()) 
      encoder.encode("WorkingLinks", pWorkingLinks);

    if(!pCheckedInLinks.isEmpty())
      encoder.encode("CheckedInLinks", pCheckedInLinks);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    String name = (String) decoder.decode("Name"); 
    if(name == null) 
      throw new GlueException("The \"Name\" was missing!");
    pName = name;

    HashMap<NodeID,TreeSet<String>> working = 
      (HashMap<NodeID,TreeSet<String>>) decoder.decode("WorkingLinks");
    if(working != null) 
      pWorkingLinks = working;

    HashMap<VersionID,TreeMap<String,VersionID>> checkedIn = 
      (HashMap<VersionID,TreeMap<String,VersionID>>) decoder.decode("CheckedInLinks");
    if(checkedIn != null) 
      pCheckedInLinks = checkedIn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node.
   */
  private String  pName;        

  /** 
   * The names of the working nodes downstream indexed by working version id.
   */
  private HashMap<NodeID,TreeSet<String>>  pWorkingLinks;

  /** 
   * The revision numbers of the checked-in nodes downstream indexed by revision number and
   * downstream node name.
   */
  private HashMap<VersionID,TreeMap<String,VersionID>>  pCheckedInLinks;
  
}

