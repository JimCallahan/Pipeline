// $Id: NodeVersion.java,v 1.26 2009/03/25 22:02:23 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public 
  NodeVersion()
  {
    pSources = new TreeMap<String,LinkVersion>();
  }

  /**
   * Construct a new checked-in version based on a working version of the node.
   *
   * @param mod 
   *   The working version of the node.
   * 
   * @param vid 
   *   The revision number of the new checked-in node version.
   * 
   * @param lvids
   *   The revision numbers of the checked-in upstream node versions.
   * 
   * @param locked
   *   Whether the upstream working versions are locked.
   * 
   * @param isNovel
   *   Whether each file associated with the version contains new data not present in the
   *   previous checked-in version.
   * 
   * @param author
   *   The name of the user creating the version.
   * 
   * @param msg 
   *   The check-in log message.
   * 
   * @param rootName
   *   The fully resolved name of the root node of the check-in operation.
   * 
   * @param rootVersionID
   *   The revision number of the new version of the root node created by the check-in 
   *   operation.
   */
  public 
  NodeVersion
  (
   NodeMod mod, 
   VersionID vid, 
   TreeMap<String,VersionID> lvids,
   TreeMap<String,Boolean> locked,
   TreeMap<FileSeq,boolean[]> isNovel,
   String author, 
   String msg, 
   String rootName, 
   VersionID rootVersionID 
  ) 
  {
    super(mod);

    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;

    if(msg == null) 
      throw new IllegalArgumentException("The check-in message cannot be (null)!");
    pMessage = new LogMessage(author, msg, rootName, rootVersionID);

    pSources = new TreeMap<String,LinkVersion>();
    for(LinkMod link : mod.getSources()) 
      pSources.put(link.getName(), 
		   new LinkVersion(link, lvids.get(link.getName()), 
                                   locked.get(link.getName())));

    if(isNovel == null) 
      throw new IllegalArgumentException("The file novelty table cannot be (null)!");      
    pIsNovel = new TreeMap<FileSeq,boolean[]>();
    for(FileSeq fseq : isNovel.keySet()) 
      pIsNovel.put(fseq, isNovel.get(fseq).clone());

    if(pAction != null) {
      TreeSet<String> dead = new TreeSet<String>();

      for(String sname : pAction.getSourceNames()) {
	if(!pSources.containsKey(sname)) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
	     "While creating checked-in node (" + pName + ") version (" + pVersionID + "), " +
	     "per-source action parameters associated with the primary file sequencs of " + 
	     "source (" + sname + ") where found for the action associated with the " + 
	     "working version being checked-in, but there were NO upstream links to this " + 
	     "source node!  These extra per-source parameters were ignored."); 
	  dead.add(sname);
	}
      }

      for(String sname : pAction.getSecondarySourceNames()) {
	if(!pSources.containsKey(sname)) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
	     "While creating checked-in node (" + pName + ") version (" + pVersionID + "), " +
	     "per-source action parameters associated with a secondary file sequencs of " + 
	     "source (" + sname + ") where found for the action associated with the " + 
	     "working version being checked-in, but there were NO upstream links to this " + 
	     "source node!  These extra per-source parameters were ignored."); 
	  dead.add(sname);
	}
      }

      for(String sname : dead) {
	pAction.removeSourceParams(sname); 
	pAction.removeSecondarySourceParams(sname);
      }
    }
  }

  /** 
   * Copy constructor. 
   * 
   * @param vsn 
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

    pIsNovel = new TreeMap<FileSeq,boolean[]>();
    for(FileSeq fseq : vsn.getSequences()) 
      pIsNovel.put(fseq, vsn.isNovel(fseq).clone());
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
    return pVersionID;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get when the version was checked-in.
   */ 
  public long
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

  /**
   * Get the check-in log message.
   */ 
  public LogMessage
  getLogMessage() 
  {
    return pMessage;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Does this version have links to upstream nodes.
   */
  public boolean 
  hasSources() 
  {
    return (!pSources.isEmpty());
  }

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
   * @param name  
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

    LinkVersion link = pSources.get(name);
    if(link != null) 
      return new LinkVersion(link);

    return null;
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
  
  /**
   * Get whether each file associated with the given file sequence contains new data not 
   * present in the previous checked-in version. <P> 
   * 
   * @param fseq
   *   The file sequence to lookup.
   * 
   * @return
   *   The per-file novelty flags for the given file sequence.
   */ 
  public boolean[] 
  isNovel
  (
   FileSeq fseq
  ) 
  {
    return pIsNovel.get(fseq);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the node links of this version and the given working version identical?
   */ 
  public boolean 
  identicalLinks
  ( 
   NodeMod mod
  ) 
  {
    return getSources().equals(mod.getSources());
  }

  /**
   * Are the node links of this version and the given checked-in version identical?
   */ 
  public boolean 
  identicalLinks
  ( 
   NodeVersion vsn
  ) 
  {
    return getSources().equals(vsn.getSources());
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
	return (identicalProperties(vsn) && 
		identicalLinks(vsn) &&
		(((pVersionID == null) && (vsn.pVersionID == null)) ||  
 		 pVersionID.equals(vsn.pVersionID)) &&        
		pMessage.equals(vsn.pMessage));
      }
      else if(obj instanceof NodeMod) {
	NodeMod mod = (NodeMod) obj;
	return (identicalProperties(mod) && 
		identicalLinks(mod));
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

    encoder.encode("VersionID", pVersionID);
    encoder.encode("Message", pMessage);

    if(!pSources.isEmpty())
      encoder.encode("Sources", pSources);

    encoder.encode("IsNovel", pIsNovel);
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

    TreeMap<FileSeq,boolean[]> isNovel = 
      (TreeMap<FileSeq,boolean[]>) decoder.decode("IsNovel");
    if(isNovel == null) 
      throw new GlueException("The \"IsNovel\" was missing!");
    pIsNovel = isNovel;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S I T E   V E R S I O N S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Modify the node version to make it suitable for transfer to a remote site.<P> 
   * 
   * This method should only be used internallly to implement the 
   * {@link MssterMgrClient#extractSiteVersion} method and never in user code!<P> 
   * 
   * This method will go away when true multi-site support is added to Pipeline.<P> 
   * 
   * @param referenceNames
   *   The fully resolved names of the source nodes to include as Reference links or
   *   <CODE>null</CODE> if no links should be included.
   * 
   * @param localSiteName
   *   Name for the local site which will be used to modify extracted node names.
   * 
   * @param stamp
   *   The timestamp of when this node version was extracted.
   * 
   * @param creator
   *   The name of the user extracting this node version. 
   * 
   * @param jarName
   *   The name of the JAR archive which will contain the extracted node.
   */ 
  public void
  makeSiteLocal
  (
   TreeSet<String> referenceNames, 
   String localSiteName, 
   long stamp, 
   String creator, 
   String jarName
  )
    throws PipelineException
  {
    /* record info about original node in a version annotation */ 
    {
      PluginMgrClient client = PluginMgrClient.getInstance();
      BaseAnnotation annot = 
        client.newAnnotation("RemoteVersion", new VersionID("2.4.5"), "Temerity");

      annot.setParamValue("OrigName", new Path(pName));
      annot.setParamValue("VersionID", pVersionID.toString());
      annot.setParamValue("ExtractedAt", localSiteName);
      annot.setParamValue("ExtractedOn", TimeStamps.format(stamp));
      annot.setParamValue("ExtractedBy", creator);
      annot.setParamValue("JarName", jarName);
      
      pAnnotations.put("RemoteVersion", annot);
    }    

    /* remove any action and action related information */ 
    pAction = null;
    pIsActionEnabled = false;
    pJobReqs   = null;
    pOverflow  = null;
    pExecution = null;
    pBatchSize = null;
    
    /* localize node name */ 
    String origName = pName;
    pName = siteLocalName(origName, localSiteName);
    
    /* fix sources */ 
    {
      TreeMap<String,LinkVersion> sources = new TreeMap<String,LinkVersion>();
      if(referenceNames != null) {
        for(String sname : referenceNames) {
          LinkVersion vsn = pSources.get(sname);
          if(vsn == null) 
            throw new PipelineException
              ("There is no existing link to a source node named (" + sname + ") for the " + 
               "node (" + origName + "), so it cannot be included as a one of the links " + 
               "to be converted to a Reference link for this node!");
          
          String nname = siteLocalName(sname, localSiteName);
          LinkMod mod = new LinkMod(nname, LinkPolicy.Reference, 
                                    vsn.getRelationship(), vsn.getFrameOffset());
          sources.put(nname, new LinkVersion(mod, vsn.getVersionID(), false));
        }
      }
      pSources = sources;
    }

    /* make all files novel */ 
    for(FileSeq fseq : pIsNovel.keySet()) {
      boolean novel[] = pIsNovel.get(fseq);

      int wk;
      for(wk=0; wk<novel.length; wk++) 
        novel[wk] = true;
    }
  }

  /** 
   * Generate a localized name for extracted nodes.
   */ 
  private String
  siteLocalName
  (
   String name, 
   String localSiteName
  ) 
  {
    Path orig = new Path(name); 
    Path fixed = new Path(new Path(orig.getParentPath(), localSiteName), orig.getName());
    return fixed.toString();
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


  /**
   * Whether each file associated with this version contains new data not present in the
   * previous checked-in version. <P> 
   * 
   * All files of the initial version will be novel.  In subsequent versions, only those 
   * files which have been added or modified since the previous version will be marked as 
   * novel.
   */
  private TreeMap<FileSeq,boolean[]>  pIsNovel;
}

