// $Id: Archive.java,v 1.2 2005/01/15 02:56:32 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I V E                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the contents of an archive of files associated with checked-in node 
 * versions. 
 */
public
class Archive
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  Archive() 
  {}

  /**
   * Construct a new instance.
   * 
   * @param name
   *   The unique name of the archive.
   * 
   * @param files 
   *   The file sequences of the checked-in versions contained in the archive indexed by
   *   fully resolved node name and revision number.
   * 
   * @param archiver 
   *   The archiver plugin instance used to create the archive.
   * 
   * @param output
   *   The output produced by OS level subprocesses executed by the 
   *   {@link BaseArchiver#archive archive} method during creation of the archive or 
   *   <CODE>null</CODE> if no output was produced.
   */
  public 
  Archive
  (
   String name, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> files, 
   BaseArchiver archiver, 
   String output
  ) 
  {
    super(name);
    
    pTimeStamp = new Date();

    if(files == null) 
      throw new IllegalArgumentException
	("The files cannot be (null)!");    
    if(files.isEmpty()) 
      throw new IllegalArgumentException
	("The files cannot be empty!");    
    pFiles = files;

    if(archiver == null) 
      throw new IllegalArgumentException
	("The archiver plugin instance cannot be (null)!");
    pArchiver = archiver;

    pOutput = output;
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
    return Collections.unmodifiableSet(pFiles.keySet());
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
    TreeMap<VersionID,TreeSet<FileSeq>> versions = pFiles.get(name);
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
    TreeMap<VersionID,TreeSet<FileSeq>> versions = pFiles.get(name);
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
    TreeMap<VersionID,TreeSet<FileSeq>> versions = pFiles.get(name);
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
    for(String name : pFiles.keySet()) {
      TreeMap<VersionID,TreeSet<FileSeq>> versions = pFiles.get(name);
      for(VersionID vid : versions.keySet()) {
	for(FileSeq fseq : versions.get(vid)) 
	  cnt += fseq.numFrames();
      }
    }
	
    ArrayList<File> files = new ArrayList<File>(cnt);
    for(String name : pFiles.keySet()) {
      TreeMap<VersionID,TreeSet<FileSeq>> versions = pFiles.get(name);
      for(VersionID vid : versions.keySet()) {
	for(FileSeq fseq : versions.get(vid)) 
	 files.addAll(fseq.getFiles());
      }
    }
    
    return files;
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
  
  /**
   * Get the output produced by OS level subprocesses executed by the 
   * {@link BaseArchiver#archive archive} method during creation of the archive or 
   * <CODE>null</CODE> if no output was produced.
   */
  public String
  getOutput() 
  {
    return pOutput;
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
    out.writeObject(pFiles);
    out.writeObject(new BaseArchiver(pArchiver));
    out.writeObject(pOutput);
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
    pFiles = (TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>) in.readObject();

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

    pOutput = (String) in.readObject();
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
    encoder.encode("Files", pFiles);
    encoder.encode("Archiver", pArchiver);

    if(pOutput != null)
      encoder.encode("Output", pOutput);
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
	(TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>) decoder.decode("Files");
      if(files == null) 
 	throw new GlueException("The \"Files\" was missing!");
      pFiles = files;      
    }
    
    {
      BaseArchiver archiver = (BaseArchiver) decoder.decode("Archiver");
      if(archiver == null) 
 	throw new GlueException("The \"Archiver\" was missing!");
      pArchiver = archiver;   
    }
      
    pOutput = (String) decoder.decode("Output");
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
  private TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>  pFiles;

  /** 
   * The archiver plugin instance used to create the archive.
   */
  private BaseArchiver  pArchiver;

  /** 
   * The output produced by OS level subprocesses executed by the 
   * {@link BaseArchiver#archive archive} method during creation of the archive or 
   * <CODE>null</CODE> if no output was produced.
   */
  private String  pOutput;

}

