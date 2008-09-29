// $Id: DownstreamLinks.java,v 1.12 2008/09/29 19:02:17 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
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
 * The {@link MasterMgr MasterMgr} class is responsible for keeping the linkage information  
 * contained in instances of this class up to date with the <CODE>NodeMod</CODE> and 
 * <CODE>NodeVersion</CODE> instances.  The <CODE>MasterMgr</CODE> class is also responsible 
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
    pWorkingLinks   = new DoubleMap<String,String,TreeSet<String>>();
    pCheckedInLinks = new TreeMap<VersionID,MappedSet<String,VersionID>>();
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

    pWorkingLinks   = new DoubleMap<String,String,TreeSet<String>>();
    pCheckedInLinks = new TreeMap<VersionID,MappedSet<String,VersionID>>();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether there area any working versions downstream of this node.
   */ 
  public boolean 
  hasWorking() 
  {
    return (!pWorkingLinks.isEmpty());
  }

  /**
   * Whether there are any working versions downstream of this node in the given working area.
   * 
   * @param author 
   *   The name of the user which owns the upstream working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public boolean 
  hasWorking
  (
   String author, 
   String view
  ) 
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    return pWorkingLinks.containsKey(author, view);
  }

  /** 
   * Whether there are any working versions downstream of this node in the working area 
   * containing the given working version.
   * 
   * @param nodeID
   *   The unique working version identifier of the upstream node.
   */
  public boolean
  hasWorking
  (
   NodeID nodeID
  ) 
  {
    if(nodeID == null) 
      throw new IllegalArgumentException
        ("The upstream working version node ID cannot be (null)!");

    if(!nodeID.getName().equals(pName))
      throw new IllegalStateException(); 

    return hasWorking(nodeID.getAuthor(), nodeID.getView());
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Are there any checked-in versions downstream of this node.
   */ 
  public boolean 
  hasCheckedIn() 
  {
    return (!pCheckedInLinks.isEmpty()); 
  }

  /**
   * Are there any checked-in versions downstream of the given checked-in version of 
   * this node.
   * 
   * @param vid 
   *   The revision number of the checked-in upstream node.
   */ 
  public boolean 
  hasCheckedIn
  (
   VersionID vid 
  ) 
  {
    return (pCheckedInLinks.containsKey(vid));
  }

   
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether there are any working or checked-in versions downstream of this node.
   */ 
  public boolean 
  hasAny() 
  {
    return (hasWorking() || hasCheckedIn());
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
    if(pName == null)
      throw new IllegalStateException(); 
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G   V E R S I O N S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the names of the working versions of nodes downstream of the given working version
   * within a specific working area. <P> 
   * 
   * @param author 
   *   The name of the user which owns the upstream working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @return
   *   The names of the downstream nodes or <CODE>null</CODE> if none exist.
   */
  public TreeSet<String>
  getWorking
  (
   String author, 
   String view
  ) 
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    TreeSet<String> links = pWorkingLinks.get(author, view);
    if(links != null) 
      return new TreeSet<String>(links);

    return null;
  }
  
  /** 
   * Gets the names of the working versions of nodes downstream of the given working version
   * within a specific working area. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier of the upstream node.
   * 
   * @return
   *   The names of the downstream nodes or <CODE>null</CODE> if none exist.
   */
  public TreeSet<String>
  getWorking
  (
   NodeID nodeID
  ) 
  {
    if(nodeID == null) 
      throw new IllegalArgumentException
        ("The upstream working version node ID cannot be (null)!");

    if(!nodeID.getName().equals(pName))
      throw new IllegalStateException(); 

    return getWorking(nodeID.getAuthor(), nodeID.getView());
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Add the name of a working version of a node downstream of the given working version
   * within a specific working area. <P> 
   * 
   * @param author 
   *   The name of the user which owns the upstream working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param dname 
   *   The fully resolved name of the downstream node.
   */
  public void 
  addWorking
  (
   String author, 
   String view,
   String dname
  ) 
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    if(dname == null) 
      throw new IllegalArgumentException("The downstream node name cannot be (null)!");

    TreeSet<String> links = pWorkingLinks.get(author, view);
    if(links == null) {
      links = new TreeSet<String>();
      pWorkingLinks.put(author, view, links);
    }

    links.add(dname); 
  }
 
  /** 
   * Add the name of a working version of a node downstream of the given working version
   * within a specific working area. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier of the upstream node.
   * 
   * @param name 
   *   The fully resolved name of the downstream node.
   */
  public void 
  addWorking
  (
   NodeID nodeID,
   String name
  ) 
  {
    if(nodeID == null) 
      throw new IllegalArgumentException
	("The upstream working version node ID cannot be (null)!");

    if(!nodeID.getName().equals(pName))
      throw new IllegalStateException(); 
    
    addWorking(nodeID.getAuthor(), nodeID.getView(), name);
  }
 

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove the name of a working version of a node downstream of the given working version
   * within a specific working area. <P> 
   * 
   * This is used by the Release node operation to clean up downstream links to the 
   * node being released. 
   * 
   * @param author 
   *   The name of the user which owns the upstream working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param dname 
   *   The fully resolved name of the downstream node being released.
   */
  public void 
  removeWorking
  (
   String author, 
   String view,
   String dname
  ) 
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    TreeSet<String> links = pWorkingLinks.get(author, view);
    if(links != null) {
      links.remove(dname);
      if(links.isEmpty())
        pWorkingLinks.remove(author, view);
    }
  }
 
  /** 
   * Remove the name of a working version of a node downstream of the given working version
   * within a specific working area. <P> 
   * 
   * This is used by the Release node operation to clean up downstream links to the 
   * node being released. 
   * 
   * @param nodeID
   *   The unique working version identifier of the upstream node.
   * 
   * @param dname 
   *   The fully resolved name of the downstream node being released.
   */
  public void 
  removeWorking
  (
   NodeID nodeID,
   String dname
  ) 
  {
    if(nodeID == null) 
      throw new IllegalArgumentException
	("The upstream working version node ID cannot be (null)!");

    if(!nodeID.getName().equals(pName))
      throw new IllegalStateException(); 

    if(dname == null) 
      throw new IllegalArgumentException
	("The downstream node name cannot be (null)!");

    removeWorking(nodeID.getAuthor(), nodeID.getView(), dname);
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Remove the names of all working versions downstream of the given working version
   * within a specific working area. <P> 
   * 
   * This is used by the Release node operation to clean up all downstream links of the 
   * node being released.
   * 
   * @param author 
   *   The name of the user which owns the upstream working version being released.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public void 
  removeAllWorking
  (
   String author, 
   String view
  ) 
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    pWorkingLinks.remove(author, view);
  }

  /**
   * Remove the names of all working versions downstream of the given working version
   * within a specific working area. <P> 
   * 
   * This is used by the Release node operation to clean up all downstream links of the 
   * node being released.
   * 
   * @param nodeID
   *   The unique working version identifier of the upstream node being released.
   */ 
  public void 
  removeAllWorking
  (
   NodeID nodeID
  ) 
  {
    if(nodeID == null) 
      throw new IllegalArgumentException
	("The upstream working version node ID cannot be (null)!");

    if(!nodeID.getName().equals(pName))
      throw new IllegalStateException(); 

    removeAllWorking(nodeID.getAuthor(), nodeID.getView());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K E D - I N   V E R S I O N S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**  
   * Get the names and revision numbers of the checked-in versions of nodes downstream 
   * of each of the checked-in versions of a node.<P> 
   * 
   * @return
   *   The revision numbers of all checked-in nodes downstream of a specific version of this
   *   node indexed by upstream node revision number and downstream node name. 
   */
  public TreeMap<VersionID,MappedSet<String,VersionID>> 
  getAllCheckedIn() 
  {
    TreeMap<VersionID,MappedSet<String,VersionID>> versions = 
      new TreeMap<VersionID,MappedSet<String,VersionID>>();

    for(VersionID vid : pCheckedInLinks.keySet()) 
      versions.put(vid, new MappedSet<String,VersionID>(pCheckedInLinks.get(vid)));

    return versions;
  }

  /**  
   * Get the names and revision numbers of the checked-in versions of nodes downstream 
   * of the given checked-in version of a node.<P> 
   * 
   * @param vid 
   *   The revision number of the checked-in upstream node.
   * 
   * @return
   *   The revision numbers indexed by the names of the downstream nodes or 
   *   <CODE>null</CODE> if none exist.
   */
  public MappedSet<String,VersionID>
  getCheckedIn
  (
   VersionID vid 
  ) 
  {
    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");
    
    MappedSet<String,VersionID> links = pCheckedInLinks.get(vid);
    if(links != null) 
      return new MappedSet<String,VersionID>(links);

    return null;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Add the name and revision of a checked-in version of a node downstream of the given 
   * checked-in version of a node.<P> 
   * 
   * @param vid 
   *   The revision number of the checked-in upstream node.
   * 
   * @param dname 
   *   The fully resolved name of the downstream node.
   * 
   * @param dvid
   *   The revision number of the downstream checked-in version of the node.
   */
  public void 
  addCheckedIn
  (
   VersionID vid,
   String dname,
   VersionID dvid 
  ) 
  {
    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number of the upstream cannot be (null)!");

    if(dname == null) 
      throw new IllegalArgumentException
	("The downstream node name cannot be (null)!");
    
    if(dvid == null) 
      throw new IllegalArgumentException
	("The downstream revision number cannot be (null)!");

    MappedSet<String,VersionID> links = pCheckedInLinks.get(vid);
    if(links == null) {
      links = new MappedSet<String,VersionID>();
      pCheckedInLinks.put(vid, links);
    }

    links.put(dname, dvid);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove the name of all checked-in versions of a node downstream of this node.
   * 
   * This is used by the Delete node operation to clean up downstream links to the 
   * node being deleted. 
   * 
   * @param dname 
   *   The fully resolved name of the downstream node being deleted.
   */
  public void 
  removeAllCheckedIn
  (
   String dname
  ) 
  {
    TreeSet<VersionID> empty = new TreeSet<VersionID>();

    for(VersionID vid : pCheckedInLinks.keySet()) {
      MappedSet<String,VersionID> links = pCheckedInLinks.get(vid);
      if(links != null) {
        links.remove(dname);
        if(links.isEmpty()) 
          empty.add(vid);
      }
    }
    
    for(VersionID vid : empty)
      pCheckedInLinks.remove(vid);      
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

    DoubleMap<String,String,TreeSet<String>> working = 
      (DoubleMap<String,String,TreeSet<String>>) decoder.decode("WorkingLinks");
    if(working != null) 
      pWorkingLinks = working;

    TreeMap<VersionID,MappedSet<String,VersionID>> checkedIn = 
      (TreeMap<VersionID,MappedSet<String,VersionID>>) decoder.decode("CheckedInLinks");
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
   * The names of the working nodes downstream of this node indexed by the working area 
   * author and view containing the working nodes. 
   */
  private DoubleMap<String,String,TreeSet<String>>  pWorkingLinks;

  /** 
   * The revision numbers of all checked-in nodes downstream of a specific version of this
   * node indexed by upstream node revision number and downstream node name.
   */
  private TreeMap<VersionID,MappedSet<String,VersionID>>  pCheckedInLinks;
}

