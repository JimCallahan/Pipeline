// $Id: CommonActionUtils.java,v 1.10 2008/02/04 04:00:10 jim Exp $

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   W O R K I N G   D E F R A G                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Runs the StorNext file defragger (snfsdegrag) safely on the files in one or more 
 * Pipeline working directories safely.
 */
public 
class WorkingDefrag
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
      boolean dryRun = false;
      boolean recursive = false;
      LinkedList<File> dirs = new LinkedList<File>();
      for(String arg : args) {
        if(arg.equals("--help"))
          usage();
        else if(arg.equals("--dry-run"))
          dryRun = true;
        else if(arg.equals("--recursive"))
          recursive = true;
        else 
          dirs.add(new File(arg));
      }
			
      //LogMgr.getInstance().setLevel(LogMgr.Kind.Sub, LogMgr.Level.Finer); 

      PluginMgrClient.getInstance().init();
      MasterMgrClient mclient = new MasterMgrClient();
      try {
        WorkingDefrag app = new WorkingDefrag(mclient, dryRun, recursive);
        app.processRoots(dirs);
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
       "usage: WorkingDefrag --help\n" + 
       "       WorkingDefrag [--dry-run] [--recursive] dir1 [dir2 ... dirN]\n" + 
       "\n" +
       "Runs the StorNext file defragger (snfsdegrag) safely on the files in one\n" +
       "or more Pipeline working directories safely.  Defragging changes the last\n" + 
       "change (ctime) timestamp of the files which normally would make Pipeline\n" +
       "think that the files may have been altered even though the defrag process\n" + 
       "doesn't change their contents. In a mixed OS environment, some operations\n" + 
       "a Windows client which should cause a last modification (mtime) timstamp\n" + 
       "changes actually alter ctime instead. Pipeline is able to differentiate\n" + 
       "legitimate ctime changes from non-legitimate ones by keeping an timestamp\n" + 
       "internally (called LastCTimeUpdate) of when Pipeline itself makes ctime\n" + 
       "related changes. If the ctime for a file is newer-than LastCTimeUpdate\n" + 
       "then Pipeline treats ctime as an indication of the file contents being\n" + 
       "modified and not just its file meta-data. The update of ctime by the\n" + 
       "StorNext defrag process is therefore wrongly seen as a file content change.\n" + 
       "\n" + 
       "In order to avoid this situation, this tool will scan the files of a working\n" + 
       "directory and group them by the Pipeline node which manages each file and\n" + 
       "determine which files are not managed by any Pipeline node. Unmanaged files\n" + 
       "are then defragged without any additional actions. Any files associated with\n" + 
       "a node which does not have an enabled action must be skipped, since there\n" + 
       "would be no way to differentiate a StorNext update and an edit of the file.\n" + 
       "However, files associated with nodes with enabled actions can be defragged\n" + 
       "as long as the LastCTimeUpdate property of the node is then made to be newer\n" + 
       "than any ctime of its associated files after the defrag is complete.\n" + 
       "\n" + 
       "  --dry-run\n" + 
       "\n" + 
       "    Perform all of the tests on files for the given directories and log what\n" + 
       "    defrag operations would have been performed, but do not take any action\n" + 
       "    which changes either files or Pipeline node properties.\n" + 
       "\n" + 
       "  --recursive\n" + 
       "\n" + 
       "    Process not only the given directories but all subdirectories as well.\n\n");
		
    System.exit(1);       
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Initialize the instance.
   */ 
  public
  WorkingDefrag
  (
   MasterMgrClient mclient,
   boolean dryRun, 
   boolean recursive
  ) 
    throws PipelineException
  {
    pMasterMgrClient = mclient; 
    pDryRun = dryRun;
    pRecursive = recursive;  
  }
	
  

  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Process the root working directories passesd as command-line arguments.
   */
  public void 
  processRoots
  (
   LinkedList<File> roots
  )
    throws PipelineException
  {
    if(pDryRun) 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "-- DRY RUN MODE --\n" + 
         "Scanning Directories..."); 

    pCounter = 0L;
    TreeSet<File> found = new TreeSet<File>();
    for(File f : roots) {
      if(!f.isDirectory()) 
        throw new PipelineException 
          ("The given path (" + f + ") is not a valid directory!");
    
      if(pRecursive) 
        scanDirs(f, found);
      else 
        found.add(f);
    }

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Scanned " + ByteSize.longToFloatString(pCounter) + " Dirs");

    for(File f : found) 
      processDir(f); 

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "ALL DONE."); 
  }
	
  /**
   * Recursively search beneath the given directory for the names of subdirectories.
   */ 
  private void 
  scanDirs
  (
   File root, 
   TreeSet<File> found
  ) 
  {
    if(root.isDirectory()) {
      pCounter++;
      if(pCounter%512 == 0)
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "Scanned " + ByteSize.longToFloatString(pCounter) + " Dirs");

      found.add(root); 

      File ls[] = root.listFiles();
      if(ls != null) {
        for(File f : root.listFiles())
          scanDirs(f, found); 			
      }
    }
  }
	
  /**
   * Determine which files in a directory should be defragged and do so.
   */ 
  private void 
  processDir
  (
   File root
  ) 
  throws PipelineException
  {
    File files[] = root.listFiles();
    if(files == null) 
      return;
		
    String dname = null;
    String author = null;
    String view = null;
    {
      Path fullPath = new Path(root);
      String fullStr = fullPath.toOsString();

      if(!fullStr.startsWith(PackageInfo.sWorkPath.toOsString()))
        throw new PipelineException
          ("The given directory (" + root + ") is not part of a Pipeline working area!");
			
      String workStr = fullStr.substring(PackageInfo.sWorkPath.toOsString().length());
      Path workPath = new Path(workStr);
      ArrayList<String> comps = workPath.getComponents();
      if(comps.size() >= 3) {
        author = comps.get(0);
        view = comps.get(1);
        dname = workStr.substring(author.length() + view.length() + 2);
      }
    }
			
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "Processing Directory: " + root); 
		
    TreeMap<File,NodeID> fileToNode = new TreeMap<File,NodeID>();
    TreeMap<NodeID,NodeMod> nodes = new TreeMap<NodeID,NodeMod>();
    if((author != null) || (view != null) || (dname != null)) {
      String pattern = (dname + "/[^/]*");
      for(String name : pMasterMgrClient.getWorkingNames(author, view, pattern)) {
        NodeID nodeID = new NodeID(author, view, name);
        NodeMod mod = pMasterMgrClient.getWorkingVersion(nodeID);
        nodes.put(nodeID, mod); 
        Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());
        for(FileSeq fseq : mod.getSequences()) {
          for(Path path : fseq.getPaths()) {
            Path p = new Path(wpath, path);
            fileToNode.put(p.toFile(), nodeID); 
          }
        }
      }
    }

    TreeSet<File> unmanaged = new TreeSet<File>();
    MappedSet<NodeID,File> managed = new MappedSet<NodeID,File>();
    TreeMap<NodeID,File> skipped = new TreeMap<NodeID,File>();
    for(File f : files) {
      if(f.isFile()) {	
        try {
          if(!NativeFileSys.isSymlink(f)) {
            if((author == null) || (view == null) || (dname == null)) {
              unmanaged.add(f);
            }
            else {
              NodeID nodeID = fileToNode.get(f);
              if(nodeID != null) 
                managed.put(nodeID, f);
              else
                unmanaged.add(f);
            }
          }
        }
        catch(IOException ex) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Warning,
             "Unable to get File Status: " + f + "\n" + ex.getMessage());
        }

      }	
    }

    for(File f : unmanaged) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "  Defrag Unmanaged: " + f); 
      if(!pDryRun)
        defrag(f);
    }

    for(NodeID nodeID : managed.keySet()) {
      NodeMod mod = nodes.get(nodeID);
      if(!mod.isActionEnabled()) {
        LogMgr.getInstance().logAndFlush			

          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "  Skipping Node: " + nodeID.getName() + 
           " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")"); 

        for(File f : managed.get(nodeID)) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "    Skipped: " + f);
        }
      } 
      else {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "  Processing Node: " + nodeID.getName() + 
           " (" + nodeID.getAuthor() + "|" + nodeID.getView() + ")"); 

        long latestCTime = -1L;
        for(File f : managed.get(nodeID)) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "    Defrag Managed: " + f); 
          if(!pDryRun)
            defrag(f);

          try {
            NativeFileStat stat = new NativeFileStat(new Path(f));
            long ctime = stat.lastChange(); 
            if(ctime > latestCTime) 
              latestCTime = ctime;
          }
          catch(IOException ex) {
            throw new PipelineException
              ("Somehow the defrag killed (" + f + ")!");
          }
        }
					
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "    LastCTimeUpdate Set."); 
        if(!pDryRun && (latestCTime > -1L))
          pMasterMgrClient.setLastCTimeUpdate(nodeID, latestCTime+1);
      }
    }
  }
	
  /**
   * Run "snfdefrag" on an individual file.
   */ 	
  private void 
  defrag
  (
   File file
  ) 
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();
    args.add(file.getPath());

    try {
      SubProcessLight proc = new 
        SubProcessLight("Defrag", new File("/usr/cvfs/bin/snfsdefrag"), args);
      proc.start();
      proc.join();
      if(!proc.wasSuccessful()) 
        throw new PipelineException
          ("Defrag of (" + file + ") failed!\n\n" + 
           proc.getStdErr() + "\n\n" + 
           proc.getStdOut());
    }
    catch(InterruptedException ex) {
      throw new PipelineException("Interrupted while running (snfsdefrag)!)");
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean  pDryRun;
  private boolean  pRecursive; 
  private long     pCounter;

  private MasterMgrClient  pMasterMgrClient; 

}
	


