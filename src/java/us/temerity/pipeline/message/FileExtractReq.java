// $Id: FileExtractReq.java,v 1.2 2005/09/07 21:11:16 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   E X T R A C T   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to extract the files associated with the given checked-in versions from 
 * the given archive volume and place them into a temporary directory.
 */ 
public
class FileExtractReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param archiveName 
   *   The name of the archive volume to create.
   * 
   * @param stamp
   *   The timestamp of the start of the restore operation.
   * 
   * @param fseqs
   *   The file sequences to extract indexed by fully resolved node name and checked-in 
   *   revision number.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   * 
   * @param env
   *   The cooked toolset environment.
   * 
   * @param size
   *   The required temporary disk space needed for the restore operation.
   */
  public
  FileExtractReq
  (
   String archiveName, 
   Date stamp, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver, 
   Map<String,String> env, 
   Long size
  )
  {
    if(archiveName == null) 
      throw new IllegalArgumentException
	("The volume name cannot be (null)!");
    pArchiveName = archiveName; 

    if(stamp == null) 
      throw new IllegalArgumentException
	("The timestamp cannot be (null)!");
    pTimeStamp = stamp;

    if(fseqs == null) 
      throw new IllegalArgumentException
	("The checked-in file sequences cannot be (null)!");
    pFileSeqs = fseqs;

    if(archiver == null) 
      throw new IllegalArgumentException
	("The archiver cannot be (null)!");
    pArchiver = archiver;

    if(env == null) 
      throw new IllegalArgumentException
	("The toolset environment cannot be (null)!");
    pEnvironment = env;

    pSize = size;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the archive volume to restore.
   */ 
  public String
  getArchiveName()
  {
    return pArchiveName; 
  }

  /**
   * Get the timestamp of the start of the restore operation.
   */ 
  public Date
  getTimeStamp() 
  {
    return pTimeStamp; 
  }

  /**
   * Get the file sequences to archive indexed by fully resolved node name and checked-in 
   * revision number.
   */ 
  public TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>
  getSequences()
  {
    return pFileSeqs; 
  }

  /**
   * Get the archiver plugin instance used to perform the restore operation.
   */ 
  public BaseArchiver
  getArchiver()
  {
    return pArchiver;
  }

  /**
   * Get the environment under which the action is executed.
   */ 
  public Map<String,String>
  getEnvironment()
  {
    return pEnvironment;
  }

  /**
   * Get the archiver plugin instance used to perform the restore operation.
   */ 
  public Long
  getSize()
  {
    return pSize; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded action plugin instance into a 
   * generic staticly loaded BaseAction instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    out.writeObject(pArchiveName);
    out.writeObject(pTimeStamp);
    out.writeObject(pFileSeqs);
    out.writeObject(new BaseArchiver(pArchiver));
    out.writeObject(pSize);
    out.writeObject(pEnvironment);
  }  

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    pArchiveName = (String) in.readObject();
    pTimeStamp = (Date) in.readObject();
    pFileSeqs = (TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>) in.readObject();
    
    BaseArchiver arch = (BaseArchiver) in.readObject();
    try {
      PluginMgrClient client = PluginMgrClient.getInstance();
      pArchiver = client.newArchiver(arch.getName(), arch.getVersionID(), arch.getVendor());
      pArchiver.setParamValues(arch);
    }
    catch(PipelineException ex) {
      throw new IOException(ex.getMessage());
    }

    pEnvironment = (Map<String,String>) in.readObject();
    pSize = (Long) in.readObject();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9182816569367732240L;
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive volume.
   */ 
  private String pArchiveName; 

  /**
   * The timestamp of the start of the restore operation.
   */ 
  private Date  pTimeStamp; 

  /**
   * The file sequences to extract indexed by fully resolved node name and checked-in 
   * revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>  pFileSeqs; 

  /**
   * The archiver plugin instance used to perform the archive operation.
   */ 
  private BaseArchiver  pArchiver;

  /**
   * The environment under which the action is executed.
   */
  private Map<String,String> pEnvironment;
 
  /**
   * The archiver plugin instance used to perform the restore operation.
   */ 
  private Long  pSize; 

}
  
