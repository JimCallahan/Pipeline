// $Id: ArchiveVolume.java,v 1.1 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E   V O L U M E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about an archive volume containing files associated with checked-in versions.
 */
public
class ArchiveVolume
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ArchiveVolume() 
  {}

  /**
   * Construct a new instance.
   * 
   * @param name
   *   The unique name of the archive.
   * 
   * @param stamp
   *   The creation timestamp. 
   * 
   * @param fseqs 
   *   The file sequences of the checked-in versions contained in the archive indexed by
   *   fully resolved node name and revision number.
   * 
   * @param sizes
   *   The total size of the files associated with each checked-in version indexed by
   *   fully resolved node name and revision number.
   * 
   * @param archiver 
   *   The archiver plugin instance used to create the archive.
   */
  public 
  ArchiveVolume
  (
   String name, 
   Date stamp, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs,
   TreeMap<String,TreeMap<VersionID,Long>> sizes,
   BaseArchiver archiver
  ) 
  {
    super(name);
    
    pTimeStamp = stamp;

    if(fseqs == null) 
      throw new IllegalArgumentException
	("The file sequences cannot be (null)!");    
    if(fseqs.isEmpty()) 
      throw new IllegalArgumentException
	("The file sequences cannot be empty!");    
    pFileSeqs = fseqs;

    if(sizes == null) 
      throw new IllegalArgumentException
	("The sizes cannot be (null)!");    
    if(sizes.isEmpty()) 
      throw new IllegalArgumentException
	("The sizes cannot be empty!");    
    pSizes = sizes;

    if(!fseqs.keySet().equals(sizes.keySet())) 
      throw new IllegalArgumentException
	("The checked-in version indices of the file sequences and sizes " +
	 "tables inconsistent!");

    for(String vname : fseqs.keySet()) {
      if(!fseqs.get(vname).keySet().equals(sizes.get(vname).keySet()))
	throw new IllegalArgumentException
	  ("The checked-in version indices of the file sequences and sizes " +
	   "tables inconsistent!");
    }

    if(archiver == null) 
      throw new IllegalArgumentException
	("The archiver plugin instance cannot be (null)!");
    pArchiver = archiver;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the timestamp of when the archive was created.
   */
  public Date
  getTimeStamp()
  {
    return pTimeStamp;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved names of the nodes contained in the archive.
   */ 
  public Set<String>
  getNames()
  {
    return Collections.unmodifiableSet(pFileSeqs.keySet());
  }

  /**
   * Get the revision numbers of the checked-in versions of the given node contained in the 
   * archive.
   * 
   * @param name
   *   The fully resolved node name.    
   */ 
  public Set<VersionID>
  getVersionIDs
  (
   String name
  ) 
  {
    TreeMap<VersionID,TreeSet<FileSeq>> versions = pFileSeqs.get(name);
    if(versions != null) 
      return Collections.unmodifiableSet(versions.keySet());
    return new TreeSet<VersionID>();
  }

  /**
   * Get the file sequences associated with the given checked-in version contained in the 
   * archive.
   * 
   * @param name
   *   The fully resolved node name.    
   * 
   * @param vid
   *   The revision number of the checked-in version.
   */
  public Set<FileSeq>
  getFileSequences
  (
   String name, 
   VersionID vid
  )
  {
    TreeMap<VersionID,TreeSet<FileSeq>> versions = pFileSeqs.get(name);
    if(versions != null) {
      TreeSet<FileSeq> fseqs = versions.get(vid);
      if(fseqs != null)
	return Collections.unmodifiableSet(fseqs);
    }
    return new TreeSet<FileSeq>();
  }

  /**
   * Get the names of the files associated with the given checked-in version contained in 
   * the archive relative to the base production directory.
   * 
   * @param name
   *   The fully resolved node name.    
   * 
   * @param vid
   *   The revision number of the checked-in version.
   */
  public ArrayList<File>
  getFiles
  (
   String name, 
   VersionID vid
  )
  {
    TreeMap<VersionID,TreeSet<FileSeq>> versions = pFileSeqs.get(name);
    if(versions != null) {
      TreeSet<FileSeq> fseqs = versions.get(vid);
      if(fseqs != null) {
	int cnt = 0;
	for(FileSeq fseq : fseqs) 
	  cnt += fseq.numFrames();
	
	ArrayList<File> files = new ArrayList<File>(cnt);
	for(FileSeq fseq : fseqs) 
	  files.addAll(fseq.getFiles());

	return files;
      }
    }
    return new ArrayList<File>();
  }
  
  /**
   * Get the names of the files associated with ALL checked-in versions contained in 
   * the archive relative to the base production directory.
   */
  public ArrayList<File>
  getFiles() 
  {
    int cnt = 0;
    for(String name : pFileSeqs.keySet()) {
      TreeMap<VersionID,TreeSet<FileSeq>> versions = pFileSeqs.get(name);
      for(VersionID vid : versions.keySet()) {
	for(FileSeq fseq : versions.get(vid)) 
	  cnt += fseq.numFrames();
      }
    }
	
    ArrayList<File> files = new ArrayList<File>(cnt);
    for(String name : pFileSeqs.keySet()) {
      TreeMap<VersionID,TreeSet<FileSeq>> versions = pFileSeqs.get(name);
      for(VersionID vid : versions.keySet()) {
	for(FileSeq fseq : versions.get(vid)) 
	 files.addAll(fseq.getFiles());
      }
    }
    
    return files;
  }
  
  /**
   * Get the total size of the files associated with the given checked-in version contained
   * in the archive.
   * 
   * @param name
   *   The fully resolved node name.    
   * 
   * @param vid
   *   The revision number of the checked-in version.
   */
  public long
  getSize
  (
   String name, 
   VersionID vid
  )
  {
    TreeMap<VersionID,Long> sizes = pSizes.get(name);
    if((sizes != null) && sizes.containsKey(vid))
      return sizes.get(vid);
    return 0L;
  }

  /**
   * Get the total size of all files contained in the archive.
   */
  public Long
  getTotalSize() 
  {
    long total = 0L;
    for(String name : pSizes.keySet()) {
      for(Long size : pSizes.get(name).values()) 
	total += size;
    }
    
    return total;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the archiver plugin instance used to create the archive.
   */ 
  public BaseArchiver
  getArchiver() 
  {
    if(pArchiver != null)
      return (BaseArchiver) pArchiver.clone();
    return null;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded archiver plugin instance into a 
   * generic staticly loaded BaseArchiver instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    out.writeObject(pTimeStamp);
    out.writeObject(pFileSeqs);
    out.writeObject(new BaseArchiver(pArchiver));
  }

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an archiver plugin instance and copy
   * its parameters from the generic staticly loaded BaseArchiver instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    pTimeStamp = (Date) in.readObject();
    pFileSeqs = (TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>) in.readObject();

    {
      BaseArchiver archiver = (BaseArchiver) in.readObject();
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pArchiver = client.newArchiver(archiver.getName(), archiver.getVersionID());
	pArchiver.setParamValues(archiver);
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
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

    encoder.encode("TimeStamp", pTimeStamp.getTime());
    encoder.encode("FileSeqs", pFileSeqs);
    encoder.encode("Sizes", pSizes);
    encoder.encode("Archiver", new BaseArchiver(pArchiver));
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    {
      Long stamp = (Long) decoder.decode("TimeStamp");
      if(stamp == null) 
 	throw new GlueException("The \"TimeStamp\" was missing!");
      pTimeStamp = new Date(stamp);
    }

    {
      TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> files = 
	(TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>) decoder.decode("FileSeqs");
      if(files == null) 
 	throw new GlueException("The \"FileSeqs\" was missing!");
      pFileSeqs = files;      
    }
    
    {
      TreeMap<String,TreeMap<VersionID,Long>> sizes = 
	(TreeMap<String,TreeMap<VersionID,Long>>) decoder.decode("Sizes");
      if(sizes == null) 
 	throw new GlueException("The \"Sizes\" was missing!");
      pSizes = sizes; 
    }
    
    {
      BaseArchiver archiver = (BaseArchiver) decoder.decode("Archiver");
      if(archiver == null) 
 	throw new GlueException("The \"Archiver\" was missing!");

      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pArchiver = client.newArchiver(archiver.getName(), archiver.getVersionID());
	pArchiver.setParamValues(archiver);
      }
      catch(PipelineException ex) {
	throw new GlueException(ex.getMessage());
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation archive.
   */ 
  public String
  toString() 
  {
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5195117839403338226L;
                                                


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The timestamp of when the archive was created.
   */
  private Date  pTimeStamp;

  /** 
   * The file sequences of the checked-in versions contained in the archive indexed by
   * fully resolved node name and revision number.
   */
  private TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>  pFileSeqs;

  /**
   * The total size of the files associated with each checked-in version indexed by
   * fully resolved node name and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Long>>  pSizes; 

  /** 
   * The archiver plugin instance used to create the archive.
   */
  private BaseArchiver  pArchiver;

}

