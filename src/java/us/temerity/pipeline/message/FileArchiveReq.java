// $Id: FileArchiveReq.java,v 1.1 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   A R C H I V E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to archive the given checked-in versions. <P>
 */
public
class FileArchiveReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name 
   *   The name of the archive volume to create.
   * 
   * @param fseqs
   *   The file sequences to archive indexed by fully resolved node name and checked-in 
   *   revision number.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   */
  public
  FileArchiveReq
  (
   String name, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The volume name cannot be (null)!");
    pName = name; 

    if(fseqs == null) 
      throw new IllegalArgumentException
	("The checked-in file sequences cannot be (null)!");
    pFileSeqs = fseqs;

    if(archiver == null) 
      throw new IllegalArgumentException
	("The archiver cannot be (null)!");
    pArchiver = archiver;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the archive volume to create.
   */ 
  public String
  getName()
  {
    return pName; 
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
   * Get the archiver plugin instance used to perform the archive operation.
   */ 
  public BaseArchiver
  getArchiver()
  {
    return pArchiver;
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
    out.writeObject(pName);
    out.writeObject(pFileSeqs);
    out.writeObject(new BaseArchiver(pArchiver));
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
    pName = (String) in.readObject();
    pFileSeqs = (TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>) in.readObject();
    
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 21428909044013021L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive volume to create.
   */ 
  private String pName; 

  /**
   * The file sequences to archive indexed by fully resolved node name and checked-in 
   * revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>  pFileSeqs; 

  /**
   * The archiver plugin instance used to perform the archive operation.
   */ 
  private BaseArchiver  pArchiver;

}
  
