// $Id: NodeVersion.java,v 1.7 2004/03/22 03:11:08 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.core.LogMessage;

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
  {
    pSources = new TreeMap<String,LinkVersion>();
  }

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

    pSources = new TreeMap<String,LinkVersion>();
  }

  /** 
   * Copy constructor. 
   * 
   * @param vsn [<B>in</B>]
   *   The <CODE>NodeVersion</CODE> to copy.
   */ 
  public 
  NodeVersion
  (
   NodeVersion vsn
  ) 
  {
    super(vsn);

    pVersionID = vsn.getVersionID();

    pMessage = new LogMessage(vsn.pMessage);

    pSources = new TreeMap<String,LinkVersion>();
    for(LinkVersion link : vsn.getSources()) 
      pSources.put(link.getName(), new LinkVersion(link));
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
   * Get when the version was checked-in.
   */ 
  public Date
  getTimeStamp() 
  {
    return pMessage.getTimeStamp();
  }

  /**
   * Get the name of the user who checked-in the version.
   */ 
  public String
  getAuthor() 
  {
    return pMessage.getAuthor();
  }

  /**
   * Get the check-in log message text. 
   */ 
  public String
  getMessage() 
  {
    return pMessage.getMessage();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the fully resolved names of the upstream nodes.
   */
  public Set<String>
  getSourceNames() 
  {
    return Collections.unmodifiableSet(pSources.keySet());
  }

  /** 
   * Get the link relationship information for the given upstream node.
   * 
   * @param name [<B>in</B>] 
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The link relationship information or <CODE>null</CODE> if no upstream node
   *   exits with the given name.
   */
  public LinkVersion
  getSource
  (
   String name
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    return new LinkVersion(pSources.get(name));
  }

  /** 
   * Get the link relationship information for all of the upstream nodes.
   */
  public ArrayList<LinkVersion>
  getSources() 
  {
    ArrayList<LinkVersion> links = new ArrayList<LinkVersion>();
    for(LinkVersion link : pSources.values()) 
      links.add(new LinkVersion(link));
    return links;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if(obj != null) {
      if(obj instanceof NodeVersion) {
	NodeVersion vsn = (NodeVersion) obj;
	return (super.equals(obj) && 
		(((pVersionID == null) && (vsn.pVersionID == null)) ||  
 		 pVersionID.equals(vsn.pVersionID)) &&        
		pMessage.equals(vsn.pMessage) && 
		pSources.equals(vsn.pSources));
      }
      else if(obj instanceof NodeMod) {
	NodeMod mod = (NodeMod) obj;
	return (super.equals(obj) &&       
		getSources().equals(mod.getSources()));
      }
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
    throws CloneNotSupportedException
  {
    return new NodeVersion(this);
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
    super.toGlue(encoder);

    if(pVersionID != null) 
      encoder.encode("VersionID", pVersionID);
    
    encoder.encode("Message", pMessage);

    if(!pSources.isEmpty())
      encoder.encode("Sources", pSources);
  }


  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    VersionID vid = (VersionID) decoder.decode("VersionID");
    if(vid == null) 
      throw new GlueException("The \"VersionID\" was missing!");
    pVersionID = vid;

    LogMessage msg = (LogMessage) decoder.decode("Message");
    if(msg == null) 
      throw new GlueException("The \"Message\" was missing!");
    pMessage = msg;
    
    TreeMap<String,LinkVersion> sources = 
      (TreeMap<String,LinkVersion>) decoder.decode("Sources"); 
    if(sources != null) 
      pSources = sources;
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
   * A table of link relationship information associated with all nodes upstream of this 
   * node indexed by the fully resolved names of the upstream nodes.
   */ 
  private TreeMap<String,LinkVersion>  pSources;
 
}

