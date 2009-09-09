// $Id: CheckSumCache.java,v 1.1 2009/08/28 02:10:46 jim Exp $

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
   * Construct a checksum cache by copying the checksums from a checked-in version of a node.
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
    long stamp = System.currentTimeMillis(); 
    for(Map.Entry<String,CheckSum> entry : vsn.getCheckSums().entrySet()) {
      String fname = entry.getKey(); 
      CheckSum sum = entry.getValue(); 
      pCheckSums.put(fname, new TransientCheckSum(sum, stamp)); 
    }
  }

  /** 
   * Construct a checksum cache by copying the checksums from another cache while renaming
   * the files. 
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
    pNodeID = nodeID;

    pCheckSums = new TreeMap<String,TransientCheckSum>();
    long stamp = System.currentTimeMillis(); 
    
    int wk;
    for(wk=0; wk<ofnames.size() && wk<nfnames.size(); wk++) {
      TransientCheckSum osum = ocache.pCheckSums.get(ofnames.get(wk)); 
      if(osum != null) 
        pCheckSums.put(nfnames.get(wk), new TransientCheckSum(osum, stamp));
    }
  }

  /** 
   * Construct a new cache by copying the checksums from another cache which are newer
   * than the given dates for a specific set of files. 
   * 
   * @param latestUpdates
   *   The timestamps of each currently cached checksum indexed by primary/secondary file.
   * 
   * @param ocache
   *   The checksum cache which contains the checksums to copy.
   */
  public  
  CheckSumCache
  (
   TreeMap<String,Long> latestUpdates,
   CheckSumCache ocache
  )
  {
    pNodeID = ocache.getNodeID(); 

    pCheckSums = new TreeMap<String,TransientCheckSum>();
    for(Map.Entry<String,TransientCheckSum> entry : ocache.pCheckSums.entrySet()) {
      String fname = entry.getKey();
      TransientCheckSum osum = entry.getValue(); 
      Long stamp = latestUpdates.get(fname); 
      if((osum != null) && ((stamp == null) || osum.isValidAfter(stamp)))
        pCheckSums.put(fname, new TransientCheckSum(osum));
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
   * given last modification timestamp for the file.
   * 
   * @param prodDir
   *   The root production directory. 
   * 
   * @param fname
   *   The short filename without any directory components.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the file 
   *   was last modified.
   * 
   * @throws IOException
   *   If the source file does not exist or are otherwise unable to compute its checksum.
   */
  public void 
  update
  (
   Path prodDir, 
   String fname, 
   long stamp
  ) 
    throws IOException 
  {
    TransientCheckSum found = pCheckSums.get(fname); 
    if((found == null) || !found.isValidAfter(stamp)) {
      Path wpath = new Path(prodDir, pNodeID.getWorkingParent());
      pCheckSums.put(fname, new TransientCheckSum(new Path(wpath, fname), stamp+1L));
      pWasModified = true;
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
    }
  }
  
  /**
   * Add all of the checksums stored in another cache to this one, but only the checksums
   * that was generated after any corresponding checksum already stored for the file.
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

  
  /*----------------------------------------------------------------------------------------*/
  
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
      for(Path path : fseq.getPaths()) {
        if(pCheckSums.remove(path.toString()) != null) 
          pWasModified = true;
      }
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
    
    if(!current.keySet().equals(pCheckSums.keySet()))
      pWasModified = true;
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