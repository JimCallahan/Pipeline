// $Id: ExtractNodeExt.java,v 1.2 2009/09/16 15:56:45 jesse Exp $

package us.temerity.pipeline.plugin.ExtractNodeExt.v2_4_5;

import us.temerity.pipeline.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*  E X T R A C T   N O D E   E X T                                                         */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a JAR archive containing a node version suitable for insertion into a remote
 * site's node database after check-in of a node with the ExtractNode annotation.
 */
public 
class ExtractNodeExt 
  extends BaseMasterExt
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N S T R U C T O R                                                                 */
  /*----------------------------------------------------------------------------------------*/

  public 
  ExtractNodeExt()
  {
    super("ExtractNode", new VersionID("2.4.5"), "Temerity",
          "Restricts access to node operations based on the SubmitTask, ApproveTask, " +
          "Synchtask, and CommonTask annotations."); 

    {
      ExtensionParam param = 
        new StringExtensionParam
        (aLocalSite, 
         "The name to give the site where this extension is being run.", 
         null);
      addParam(param);
    }
    
    {
      ExtensionParam param = 
        new PathExtensionParam
        (aOutputDir, 
         "The temporary directory where all JAR archives created will be written.  This " + 
         "directory needs to be writable by all users.", 
         new Path("/usr/tmp"));
      addParam(param);
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to test before checking-in an individual node.
   */  
  public boolean
  hasPreCheckInTest() 
  {
    return true;
  }

  /**
   * Test to perform before checking-in an individual node.
   * 
   * @param rname
   *   The fully resolved node name of the root node of the check-in operation.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version of the node.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @param msg 
   *   The check-in message text.
   * 
   * @throws PipelineException
   *   To abort the operation.
   */  
  public void
  preCheckInTest
  (
   String rname, 
   NodeID nodeID, 
   NodeMod mod,
   VersionID.Level level, 
   String msg
  ) 
    throws PipelineException
  {
    TreeMap<String, BaseAnnotation> annots = lookupAnnots(nodeID.getName(), null); 
    if(annots == null) 
      return; 

    for(String aname : annots.keySet()) {
      BaseAnnotation annot = annots.get(aname);
      if(annot.getName().equals("RemoteNode")) 
        throw new PipelineException
          ("You cannot check-in changes to the node (" + nodeID.getName() + ") because " + 
           "it is being supplied by a remote site and is therefore illegal to edit locally!");
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to run a tesk after checking-in an individual node.
   */  
  public boolean
  hasPostCheckInTask() 
  {
    return true;
  }
  
  /**
   * The task to perform after checking-in an individual node.
   * 
   * @param vsn
   *   The newly created checked-in node version. 
   */  
  public void
  postCheckInTask
  (
   NodeVersion vsn
  ) 
  {
    String abortMsg = 
      ("Extraction of the version (" + vsn.getVersionID() + ") of node " + 
       "(" + vsn.getName() + ") was aborted.");
               
    String localSite = null; 
    try {
      localSite = (String) getParamValue(aLocalSite);
      if((localSite == null) || (localSite.length() == 0)) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
           "The " + aLocalSite + " parameter for the ExtractNode extension was missing or " + 
           "invalid!\n" + abortMsg);
        return;
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
         "Unable to lookup the value for the " + aLocalSite + " parameter for the " + 
         "ExtractNode extension!\n" + abortMsg);
        return;
    }

    Path outputDir = null; 
    try {
      outputDir = (Path) getParamValue(aOutputDir);
      if(outputDir == null) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
         "The " + aOutputDir + " parameter for the ExtractNode extension was not " + 
           "specified!\n" + abortMsg);
        return;
      }
      if(!outputDir.toFile().isDirectory()) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
           "The " + aOutputDir + " parameter for the ExtractNode extension is node a " + 
           "valid directory!\n" + abortMsg);
        return;
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
         "Unable to lookup the value for the " + aOutputDir + " parameter for the " + 
         "ExtractNode extension!\n" + abortMsg);
        return;
    }

    TreeMap<String, BaseAnnotation> annots = lookupAnnots(vsn.getName(), abortMsg); 
    if(annots == null) 
      return; 
    
    for(String aname : annots.keySet()) {
      BaseAnnotation annot = annots.get(aname);
      if(annot.getName().equals("ExtractNode")) {         
        TreeSet<String> referenceNames = new TreeSet<String>();
        for(String sname : vsn.getSourceNames()) {
          TreeMap<String,BaseAnnotation> sannots = lookupAnnots(sname, abortMsg); 
          if(sannots == null) 
              return; 
          
          for(String saname : sannots.keySet()) {
            BaseAnnotation sannot = sannots.get(saname);
            if(sannot.getName().equals("ExtractNode")) 
              referenceNames.add(sname);
          }              
        }
        
        try { 
          Path jarPath = 
            getMasterMgrClient().extractSiteVersion
              (vsn.getName(), vsn.getVersionID(), referenceNames, localSite, 
               vsn.getSequences(), null, outputDir);
          
          LogMgr.getInstance().log
            (LogMgr.Kind.Ext, LogMgr.Level.Info, 
             "Extracted version (" + vsn.getVersionID() + ") of node " + 
             "(" + vsn.getName() + ") to a JAR archive (" + jarPath + ").");
        }
        catch(PipelineException ex) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
             "Unable to extract site version due to:\n" + 
             ex.getMessage() + "\n" + abortMsg);
          return;
        }
      }
    }
  }

  private TreeMap<String,BaseAnnotation> 
  lookupAnnots
  (
   String name, 
   String abortMsg 
  ) 
  { 
    TreeMap<String, BaseAnnotation> annots = null; 
    try {
      annots = getMasterMgrClient().getAnnotations(name); 
    }  
    catch(PipelineException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ext, LogMgr.Level.Severe, 
         "Unable to lookup the annotations for node (" + name + ")!" + 
         ((abortMsg != null) ? ("\n" + abortMsg) : "")); 
    } 
     
    return annots;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2408008163343658054L;
  
  public static final String aLocalSite = "LocalSite";
  public static final String aOutputDir = "OutputDir";
      
}
