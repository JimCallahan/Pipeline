// $Id: CheckSumCache.java,v 1.6 2010/01/24 01:58:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K   S U M   C A C H E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A cache of checksums for the files associated with a working version of a node.
 */
public 
class CheckSumCache
  implements Cloneable, Glueable, Serializable
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
  CheckSumCache() 
  {
    pCheckSums = new TreeMap<String,TransientCheckSum>(); 
  }

  /** 
   * Construct an empty checksum cache for the given working version of a node.
   */
  public  
  CheckSumCache
  (
   NodeID nodeID
  ) 
  {
    pNodeID = nodeID;    
    pCheckSums = new TreeMap<String,TransientCheckSum>();
  }

  /** 
   * Construct a checksum cache by copying the checksums from a checked-in version of a 
   * node.<P>
   * 
   * The newly created checksums will have an updated-on timestamp of 0L.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param vsn
   *   The node version containing the checksums.
   */
  public  
  CheckSumCache
  (
   NodeID nodeID, 
   NodeVersion vsn
  ) 
  {
    pNodeID = nodeID;

    pCheckSums = new TreeMap<String,TransientCheckSum>();
    for(Map.Entry<String,CheckSum> entry : vsn.getCheckSums().entrySet()) {
      String fname = entry.getKey(); 
      CheckSum sum = entry.getValue(); 
      pCheckSums.put(fname, new TransientCheckSum(sum, 0L)); 
    }
  }

  /** 
   * Construct a checksum cache by copying the checksums from another cache while renaming
   * the files. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param nfnames
   *   The filenames of the new checksums copies.
   * 
   * @param ofnames
   *   The filenames of the old checksums to copy.
   * 
   * @param ocache
   *   The checksum cache which contains the checksums to copy.
   */
  public  
  CheckSumCache
  (
   NodeID nodeID, 
   ArrayList<String> nfnames,
   ArrayList<String> ofnames,
   CheckSumCache ocache
  ) 
  {
    pNodeID    = nodeID;
    pCheckSums = new TreeMap<String,TransientCheckSum>();    

    int wk;
    for(wk=0; wk<ofnames.size() && wk<nfnames.size(); wk++) {
      TransientCheckSum osum = ocache.pCheckSums.get(ofnames.get(wk)); 
      if(osum != null) 
        pCheckSums.put(nfnames.get(wk), new TransientCheckSum(osum));
    }
  }

  /** 
   * Copy constructor. 
   */
  public  
  CheckSumCache
  (
   CheckSumCache sums
  ) 
  {
    pNodeID = sums.getNodeID(); 
    pCheckSums = new TreeMap<String,TransientCheckSum>(sums.pCheckSums);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the checksum cached for the working version of a file matches the given 
   * checksum for the corresponding checked-in version of the file.
   * 
   * @param fname
   *   The short filename without any directory components.
   * 
   * @param checksum
   *   The precomputed checksum for the corresponding checked-in file.
   */ 
  public boolean
  isIdentical
  (
   String fname, 
   CheckSum checksum
  ) 
    throws PipelineException 
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Fine,
       "Comparing working and checked-in checksums for: " + pNodeID + "/" + fname); 
    
    TransientCheckSum found = pCheckSums.get(fname); 
    if(found == null) 
      throw new PipelineException
        ("Unable to find any checksum in the cache for: " + pNodeID + "/" + fname); 

    return found.equals(checksum); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique identifier for the working node who's checksums are being cached.
   */ 
  public NodeID
  getNodeID() 
  {
    return pNodeID; 
  }

   
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this cache contains no checksums. 
   */ 
  public boolean
  isEmpty() 
  {
    return pCheckSums.isEmpty(); 
  }
   

  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset whether the cache has been modified. 
   */ 
  public void 
  resetModified() 
  {
    pWasModified = false;
  }


  /**
   * Whether the cache has been modified with newer checksums since the last reset.
   */ 
  public boolean 
  wasModified() 
  {
    return pWasModified;
  }

  /**
   * Recompute the checksum for the given file unless has already been generated after the
   * given last modification timestamp for the file, warning when checksums are recomputed.
   * 
   * @param prodDir
   *   The root production directory. 
   * 
   * @param fname
   *   The short filename without any directory components.
   * 
   * @param stat
   *   The file status information for the file.
   * 
   * @param critical
   *   The last legitimate change time (ctime) of the file.
   * 
   * @param warning
   *   If not (null), log warning messages when the checksum is recomputed prefixed
   *   by this string. 
   * 
   * @throws IOException
   *   If the source file does not exist or are otherwise unable to compute its checksum.
   */
  public void 
  update
  (
   Path prodDir, 
   String fname, 
   NativeFileStat stat, 
   long critical, 
   String warning
  ) 
    throws IOException 
  {
    TransientCheckSum found = pCheckSums.get(fname); 
    long stamp = stat.lastCriticalChange(critical); 
    if((found == null) || !found.isValidAfter(stamp)) {
      Path wpath = new Path(prodDir, pNodeID.getWorkingParent());
      Path path = new Path(wpath, fname);

      if((warning != null) && 
         LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Warning)) {
        if(found == null) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Sum, LogMgr.Level.Warning, 
             warning + path + "\n" + 
             "  No checksum had been computed yet.");
        }
        else {
          long updatedOn = found.getUpdatedOn();
          long mtime = stat.lastModification(); 
          long ctime = stat.lastChange();
          LogMgr.getInstance().log
            (LogMgr.Kind.Sum, LogMgr.Level.Warning, 
             warning + path + "\n" + 
             "    CheckSum: " + TimeStamps.format(updatedOn) + " (" + updatedOn + ")\n" + 
             "  File Stamp: " + TimeStamps.format(stamp) + " (" + stamp + ")\n" + 
             "  File MTime: " + TimeStamps.format(mtime) + " (" + mtime + ")\n" + 
             "  File CTime: " + TimeStamps.format(ctime) + " (" + ctime + ")\n" + 
             "    Critical: " + TimeStamps.format(critical) + " (" + critical + ")"); 
        }
      }

      pCheckSums.put(fname, new TransientCheckSum(path, stamp+1L));
      pWasModified = true;

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finest)) {
        long updatedOn = stamp+1;
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Sum, LogMgr.Level.Finest, 
           "Computed New CheckSum for: " + path + 
           "  Updated On: " + TimeStamps.format(updatedOn) + " (" + updatedOn + ")"); 
      }
    }
    else {
      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finest)) {
        Path wpath = new Path(prodDir, pNodeID.getWorkingParent());
        Path path = new Path(wpath, fname);
        long updatedOn = found.getUpdatedOn();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Sum, LogMgr.Level.Finest, 
           "Kept Existing CheckSum for: " + path + "\n" + 
           "  Last Updated: " + TimeStamps.format(updatedOn) + " (" + updatedOn + ")"); 
      }
    }
  }
  
  /**
   * The timestamps of each currently cached checksum indexed by primary/secondary file.
   */
  public TreeMap<String,Long>
  getLatestUpdates()
  {
    TreeMap<String,Long> results = new TreeMap<String,Long>();
    for(Map.Entry<String,TransientCheckSum> entry : pCheckSums.entrySet()) 
      results.put(entry.getKey(), entry.getValue().getUpdatedOn());
    return results;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Recompute the checksum for the given file regarless of whether a checksum already 
   * exists in the cache for the file.<P> 
   * 
   * The newly created checksums will have an updated-on timestamp of 0L.
   * 
   * @param prodDir
   *   The root production directory. 
   * 
   * @param fname
   *   The short filename without any directory components.
   * 
   * @throws IOException
   *   If the source file does not exist or are otherwise unable to compute its checksum.
   */
  public void 
  recompute
  (
   Path prodDir, 
   String fname
  ) 
    throws IOException 
  {
    Path wpath = new Path(prodDir, pNodeID.getWorkingParent());
    pCheckSums.put(fname, new TransientCheckSum(new Path(wpath, fname), 0L));
    pWasModified = true;
  }
  
  /**
   * Replace the updated on timestamp associated with a checksum already being cached. <P> 
   * 
   * This is used internally by various server processes to update checksum timestamps 
   * without recomputing them.  Often used in combination with {@link #recompute} which 
   * initially sets a 0L timestamp which this method then replaces with a more useful 
   * timestamp.
   * 
   * @param fname
   *   The short filename without any directory components.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the file 
   *   was last modified.
   */ 
  public void 
  replaceUpdatedOn
  (
   String fname, 
   long stamp   
  ) 
  {
    TransientCheckSum found = pCheckSums.get(fname); 
    if(found != null) {
      pCheckSums.put(fname, new TransientCheckSum(found, stamp+1L));
      pWasModified = true;

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finer)) {
        Path wpath = new Path(PackageInfo.sProdPath, 
                              new Path(pNodeID.getWorkingParent(), fname));
        LogMgr.getInstance().log
          (LogMgr.Kind.Sum, LogMgr.Level.Finer,
           "Replacing timestamp of checksum for: " + wpath);
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a checksum for the given short file name to the cache, but only if was generated 
   * after any checksum already stored for the file.
   * 
   * @param fname
   *   The short filename without any directory components.
   * 
   * @param checksum
   *   The checksum of the given file.
   */
  public void 
  add
  (
   String fname, 
   TransientCheckSum checksum
  ) 
  {
    TransientCheckSum found = pCheckSums.get(fname); 
    if((found == null) || checksum.isNewerThan(found)) {
      pCheckSums.put(fname, checksum); 
      pWasModified = true;

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finest)) {
        Path wpath = new Path(PackageInfo.sProdPath, pNodeID.getWorkingParent());
        Path path = new Path(wpath, fname);
        long updatedOn = checksum.getUpdatedOn();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Sum, LogMgr.Level.Finest, 
           "Added " + ((found != null) ? "Newer" : "Missing") + " CheckSum to Cache for: " + 
           path + "\n" + 
           "  Updated On: " + TimeStamps.format(updatedOn) + " (" + updatedOn + ")"); 
      }
    }
  }
  
  /**
   * Add all of the checksums stored in another cache to this one, but only the checksums
   * that were generated after any corresponding checksum already stored for the file.
   */ 
  public void 
  addAll
  (
   CheckSumCache sums
  ) 
  {
    for(Map.Entry<String,TransientCheckSum> entry : sums.pCheckSums.entrySet()) 
      add(entry.getKey(), entry.getValue());
  }

  /**
   * Add all of the checksums stored in another cache to this one, but only the checksums
   * that were generated after the given dates for a specific set of files. <P> 
   * 
   * This is useful if you have the latestUpdates for another CheckSumCache and want to 
   * collate all of the checksums that are newer than the other CheckSumCache into this 
   * cache which can then be transmitted over the network and applied to the other cache
   * using {@link #addAll addAll()}.
   * 
   * @param latestUpdates
   *   The timestamps of each currently cached checksum indexed by primary/secondary file.
   * 
   * @param ocache
   *   The checksum cache which contains the checksums to copy.
   */ 
  public void 
  addDelta
  (
   TreeMap<String,Long> latestUpdates,
   CheckSumCache ocache
  ) 
  {
    for(Map.Entry<String,TransientCheckSum> entry : ocache.pCheckSums.entrySet()) {
      String fname = entry.getKey();
      TransientCheckSum osum = entry.getValue(); 
      Long stamp = latestUpdates.get(fname); 
      if((osum != null) && ((stamp == null) || osum.isValidAfter(stamp))) {
        pCheckSums.put(fname, new TransientCheckSum(osum));
        pWasModified = true;

        if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finest)) {
          Path wpath = new Path(PackageInfo.sProdPath, pNodeID.getWorkingParent());
          Path path = new Path(wpath, fname);
          long updatedOn = osum.getUpdatedOn();
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Sum, LogMgr.Level.Finest, 
             "Adding " + ((stamp == null) ? "Missing" : "Newer") + " CheckSum to " + 
             "Delta-Cache for: " + path + "\n" + 
             "  Updated On: " + TimeStamps.format(updatedOn) + " (" + updatedOn + ")" +  
             ((stamp == null) ? "" : 
              "\n    External: " + TimeStamps.format(stamp) + " (" + stamp + ")"));   
        }
      }
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Remove checksums for the given file.
   */ 
  public void 
  remove
  (
   String fname
  )
  {
    if(pCheckSums.remove(fname) != null) {
      pWasModified = true;

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finest)) {
        Path wpath = new Path(PackageInfo.sProdPath, pNodeID.getWorkingParent());
        Path path = new Path(wpath, fname); 
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Sum, LogMgr.Level.Finest, 
           "Removed Cached CheckSum for: " + path); 
      }
    }
  }
  
  /**
   * Remove checksums for the files which are members of the given file sequences.
   */ 
  public void 
  remove
  (
   SortedSet<FileSeq> fseqs
  )
  {
    for(FileSeq fseq : fseqs) {
      for(Path path : fseq.getPaths()) 
        remove(path.toString());
    }
  }
  
  /**
   * Remove checksums for any files not members of the given file sequences.
   */ 
  public void 
  removeAllExcept
  (
   SortedSet<FileSeq> fseqs
  )
  {
    TreeMap<String,TransientCheckSum> current = pCheckSums; 
    pCheckSums = new TreeMap<String,TransientCheckSum>(); 
    for(FileSeq fseq : fseqs) {
      for(Path path : fseq.getPaths()) {
        String fname = path.toString();
        TransientCheckSum sum = current.get(fname);
        if(sum != null)
          pCheckSums.put(fname, sum); 
      }
    }      
    
    if(!current.keySet().equals(pCheckSums.keySet())) {
      pWasModified = true;

      if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finest)) {
        Path wpath = new Path(PackageInfo.sProdPath, pNodeID.getWorkingParent());
        for(String fname : current.keySet()) {
          if(pCheckSums.get(fname) == null) {
            Path path = new Path(wpath, fname); 
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Sum, LogMgr.Level.Finest, 
               "Removed not Excepted Cached CheckSum for: " + path); 
          }
        }
      }
    }
  }
  
  /**
   * Remove all checksums. 
   */ 
  public void
  removeAll()
  {
    if(!pCheckSums.isEmpty()) {
      pCheckSums.clear(); 
      pWasModified = true;

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Sum, LogMgr.Level.Finest, 
         "Removed All Checksums from Cache for Node: " + pNodeID); 
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a copy of the cached checksums suitable for use in creating a NodeVersion.
   */ 
  public TreeMap<String,CheckSum>
  getVersionCheckSums()
  {
    TreeMap<String,CheckSum> checksums = new TreeMap<String,CheckSum>();
    for(String fname : pCheckSums.keySet()) 
      checksums.put(fname, new CheckSum(pCheckSums.get(fname))); 
    return checksums; 
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
    return new CheckSumCache(this); 
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
    encoder.encode("NodeID", pNodeID);
    if(!pCheckSums.isEmpty()) 
      encoder.encode("CheckSums", pCheckSums);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    NodeID id = (NodeID) decoder.decode("NodeID");
    if(id == null) 
      throw new GlueException("The \"NodeID\" is missing!");
    pNodeID = id; 

    TreeMap<String,TransientCheckSum> checksums = 
      (TreeMap<String,TransientCheckSum>) decoder.decode("CheckSums");
    if(checksums != null)  
      pCheckSums = checksums; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -735588650136072427L;
        
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique identifier for the working node which owns the files who's checksums 
   * are being stored.
   */ 
  private NodeID pNodeID; 

  /**
   * The checksums for each file associated with a working version of a node indexed by short
   * file name. 
   */
  private TreeMap<String,TransientCheckSum>  pCheckSums; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the cache has been modified since the last reset.
   */
  private boolean  pWasModified; 

}
