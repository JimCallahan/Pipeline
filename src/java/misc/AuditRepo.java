// $Id: CommonActionUtils.java,v 1.10 2008/02/04 04:00:10 jim Exp $

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A U D I T   R E P O                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Compares the checksums for all online repository files against the checksums stored in 
 * the Pipeline database for those files.
 */
public 
class AuditRepo
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  public static void 
  main
  (
   String args[]
  )
  {
    boolean success = false;
    try {
      String regex = null;
      switch(args.length) {
      case 0:
        break;
        
      case 1:
        if(args[0].equals("--help"))
          usage();
        else 
          regex = args[0];
        break;

      default:
        usage(); 
      }
			
      PluginMgrClient.getInstance().init();
      MasterMgrClient mclient = new MasterMgrClient();
      try {
        AuditRepo app = new AuditRepo(mclient);
        app.audit(regex);
      }
      finally {
        mclient.disconnect();
        PluginMgrClient.getInstance().disconnect();
      }
			
      success = true;
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }
		
    System.exit(success ? 0 : 1); 
  }
	

  /**
   * Print usage information and exit.
   */
  private static void 
  usage() 
  {
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Severe,
       "Illegal command-line arguments!\n" + 
       "\n" + 
       "usage: AuditRepo --help\n" + 
       "       AuditRepo [node-regex]\n" + 
       "\n" +
       "For all checked-in nodes which match the given regular expression (node-regex)\n" + 
       "regenerate and compare the checksums for any online files associated with the\n" + 
       "node against the checksums stored in the node's database entry for the file.\n" + 
       "If no regular expression is given, then all nodes will be compared.\n\n"); 
		
    System.exit(1);       
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Initialize the instance.
   */ 
  public
  AuditRepo
  (
   MasterMgrClient mclient
  ) 
  {
    pMasterMgrClient = mclient; 
  }
	
  

  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Run the audit... 
   */
  public void 
  audit
  (
   String regex
  )
    throws PipelineException
  {
    for(String name : pMasterMgrClient.getCheckedInNames(regex)) {
      TreeSet<VersionID> offline = pMasterMgrClient.getOfflineVersionIDs(name); 
      for(NodeVersion vsn : pMasterMgrClient.getAllCheckedInVersions(name).values()) {
        VersionID vid = vsn.getVersionID();
        boolean isOffline = offline.contains(vid); 
        
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "Processing Node: " + name + " (v" + vid + ")" + (isOffline ? "  OFFLINE" : ""));
                                             
        if(!isOffline) {
          Path rpath = new Path(PackageInfo.sRepoPath, name); 
          Path vpath = new Path(rpath, vid.toString());

          for(Map.Entry<String,CheckSum> entry : vsn.getCheckSums().entrySet()) {
            String fname  = entry.getKey();
            CheckSum dsum = entry.getValue();

            Path path = new Path(vpath, fname);
            CheckSum rsum = null; 
            try {
              rsum = new CheckSum(path);

              if(rsum.equals(dsum)) {
                LogMgr.getInstance().logAndFlush
                  (LogMgr.Kind.Ops, LogMgr.Level.Info,
                   "  " + fname + " [ok]"); 
              }
              else {
                LogMgr.getInstance().logAndFlush
                  (LogMgr.Kind.Ops, LogMgr.Level.Info,
                   "  " + fname + " [BAD]  DB=" + dsum + "  REPO=" + rsum); 
              }
            }
            catch(IOException ex) {
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Ops, LogMgr.Level.Info,
                 "  " + fname + " [FAILED]: " + ex.getMessage()); 
            }       
          }
        }
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private MasterMgrClient  pMasterMgrClient; 

}
	


