// $Id: NodeVersion.java,v 1.1 2004/02/28 20:05:47 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   V E R S I O N                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A read-only checked-in version of a node. <P>
 *
 */
public
class NodeVersion 
  extends NodeCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  NodeVersion()
  {}

  /**
   * Construct a new checked-in version based on a working version of the node.
   *
   * @param mod [<B>in</B>]
   *   The working version of the node.
   * 
   * @param vid [<B>in</B>]
   *   The revision number of the new checked-in node version.
   * 
   * @param msg [<B>in</B>]
   *   The check-in log message.
   */
  public 
  NodeVersion
  (
   NodeMod mod, 
   VersionID vid, 
   String msg
  ) 
  {
    super(mod);

    pVersionID = vid;
    pMessage   = new LogMessage(msg);
    pComments  = new TreeMap<Date,LogMessage>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of this version of the node.
   */ 
  public VersionID
  getVersionID()
  {
    return new VersionID(pVersionID);
  }

  /** 
   * Get the 



  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the fully resolved names of the upstream nodes.
   */
  public ArrayList<String>
  getSourceNames() 
  {
    return new ArrayList<String>(pSourceVersionDepends.keySet());
  }

  /** 
   * Get the dependency relationship information for the given upstream node.
   * 
   * @param name [<B>in</B>] 
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The dependency relationship information or <CODE>null</CODE> if no upstream node
   *   exits with the given name.
   */
  public DependVersion
  getSource
  (
   String name
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    return new DependVersion(pSourceVersionDepends.get(name));
  }

  /** 
   * Get the dependency relationship information for all of the upstream nodes.
   */
  public ArrayList<DependVersion>
  getSources() 
  {
    ArrayList<DependVersion> deps = new ArrayList<DependVersion>();
    for(DependVersion dep : pSourceVersionDepends.values()) 
      deps.add(new DependVersion(dep));
    return deps;
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the fully resolved names of the downstream nodes.
   */
  public ArrayList<String>
  getTargetNames() 
  {
    return new ArrayList<String>(pTargetVersionIDs.keySet());
  }

  /** 
   * Get the revision number of the given downstream node.
   * 
   * @param name [<B>in</B>] 
   *   The fully resolved node name of the downstream node.
   */
  public VersionID
  getTarget
  (
   String name
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The downstream node name cannot be (null)!");

    if(!pTargetVersionIDs.containsKey(name)) 
      throw new IllegalArgumentException("No downstream node named (" + name + ") exists!");

    return new VersionID(pTargetVersionIDs.get(name));
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7664817792128584958L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of this version. 
   */ 
  private VersionID  pVersionID;       


  /**
   * The descriptive message given at the time this version of the node was created
   * by a check-in operation. The timestamp and author of the message are also the 
   * timestamp and author of the node version. <P> 
   */
  private LogMessage  pMessage;        

  /** 
   * The change comments associated with this version of the node indexed 
   * by when the comment was made.
   */
  private TreeMap<Date,LogMessage>  pComments;           


  /**
   * A table of dependency information associated with all nodes upstream of this 
   * node indexed by the fully resolved names of the upstream nodes.
   */ 
  private TreeMap<String,DependVersion>  pSourceVersionDepends;
 
  /**
   * The revision numbers of all downstream node versions which depend upon this node
   * indexed by the fully resolved names of the downstream nodes.
   */ 
  private TreeMap<String,VersionID>  pTargetVersionIDs;
 
}

