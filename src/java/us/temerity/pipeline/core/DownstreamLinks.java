// $Id: DownstreamLinks.java,v 1.3 2004/05/08 23:31:39 jim Exp $

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
 * this class on disk. <P> 
 * 
 * Users should never insantiate this class directly!
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
    pWorkingLinks   = new TreeMap<NodeID,TreeSet<String>>();
    pCheckedInLinks = new TreeMap<VersionID,TreeMap<String,VersionID>>();
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

    pWorkingLinks   = new TreeMap<NodeID,TreeSet<String>>();
    pCheckedInLinks = new TreeMap<VersionID,TreeMap<String,VersionID>>();
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
   * Gets the names of the nodes connected by a downstream link to the given working 
   * version. <P> 
   * 
   * A return value of <CODE>null</CODE> indicates that no working version with the 
   * given node ID exists.  If the working version does exist and has no downstream
   * links, an empty <CODE>TreeSet</CODE> will be returned.
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

    if(pWorkingLinks.containsKey(id)) {
      TreeSet<String> links = pWorkingLinks.get(id);
      if(links != null)
	return new TreeSet<String>(links);
      else 
	return new TreeSet<String>();
    }

    return null;
  }
  
  /**
   * If no downstream links already exist for the given working version, create an 
   * empty set of downstream links for the version.
   * 
   * @param id
   *   The unique working version identifier.
   */ 
  public void 
  createWorking
  (
   NodeID id
  ) 
  {
    TreeSet<String> links = pWorkingLinks.get(id);
    if(links == null) 
      pWorkingLinks.put(id, null);
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
  }

  /**
   * Delete the given working version from the downstream links table. <P> 
   * 
   * Used when releasing a working version so that subsequent calls to the 
   * {@link #getWorking getWorking} method will return <CODE>null</CODE>.
   * 
   * @param id
   *   The unique working version identifier.
   */ 
  public void 
  releaseWorking
  (
   NodeID id
  ) 
  {
    pWorkingLinks.remove(id);
  }


  /*----------------------------------------------------------------------------------------*/

  /**  
   * Get the revision numbers indexed by node name of the checked-in nodes connected by a 
   * downstream link to the checked-in version with the given revision number. <P> 
   * 
   * A return value of <CODE>null</CODE> indicates that no checked-in version with the given 
   * revision number exists.  If the checked-in version does exist and has no downstream
   * links, an empty <CODE>TreeMap</CODE> will be returned.
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
    
    if(pCheckedInLinks.containsKey(vid)) {
      TreeMap<String,VersionID> links = pCheckedInLinks.get(vid);
      if(links != null)
	return new TreeMap<String,VersionID>(links);
      else 
	return new TreeMap<String,VersionID>();
    }

    return null;
  }
  
  /**  
   * Get the revision numbers indexed by node name of the checked-in nodes connected by a 
   * downstream link to the checked-in version with the given revision number.
   * 
   * A return value of <CODE>null</CODE> indicates that no checked-in versions exist.
   * If the latest checked-in version does exist and has no downstream links, an 
   * empty <CODE>TreeMap</CODE> will be returned.
   * 
   * @return
   *   The table of revision numbers indexed by the names of the downstream nodes.
   */
  public TreeMap<String,VersionID>
  getLatestCheckedIn() 
  {
    if(pCheckedInLinks.isEmpty()) 
      return null;

    VersionID vid = pCheckedInLinks.lastKey();
    return getCheckedIn(vid);
  }
  
  /**
   * If no downstream links already exist for the given checked-in version, create an 
   * empty set of downstream links for the version.
   * 
   * @param vid 
   *   The revision number of the checked-in node version.
   */ 
  public void 
  createCheckedIn
  (
   VersionID vid
  ) 
  { 
    TreeMap<String,VersionID> links = pCheckedInLinks.get(vid);
    if(links == null) 
      pCheckedInLinks.put(vid, null);
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

    TreeMap<NodeID,TreeSet<String>> working = 
      (TreeMap<NodeID,TreeSet<String>>) decoder.decode("WorkingLinks");
    if(working != null) 
      pWorkingLinks = working;

    TreeMap<VersionID,TreeMap<String,VersionID>> checkedIn = 
      (TreeMap<VersionID,TreeMap<String,VersionID>>) decoder.decode("CheckedInLinks");
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
  private TreeMap<NodeID,TreeSet<String>>  pWorkingLinks;

  /** 
   * The revision numbers of the checked-in nodes downstream indexed by revision number and
   * downstream node name.
   */
  private TreeMap<VersionID,TreeMap<String,VersionID>>  pCheckedInLinks;
  
}

