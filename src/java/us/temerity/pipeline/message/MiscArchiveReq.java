// $Id: MiscArchiveReq.java,v 1.2 2005/02/07 14:51:49 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A R C H I V E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to archive the given checked-in versions. <P>
 * 
 * @see MasterMgr
 */
public
class MiscArchiveReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to archive.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   */
  public
  MiscArchiveReq
  (
    TreeMap<String,TreeSet<VersionID>> versions, 
    BaseArchiver archiver
  )
  {
    if(versions == null) 
      throw new IllegalArgumentException
	("The checked-in versions cannot be (null)!");
    pVersions = versions;

    if(archiver == null) 
      throw new IllegalArgumentException
	("The archiver cannot be (null)!");
    pArchiver = archiver;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved names and revision numbers of the checked-in versions to archive.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions()
  {
    return pVersions; 
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
    out.writeObject(pVersions);
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
    pVersions = (TreeMap<String,TreeSet<VersionID>>) in.readObject();
    
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

  private static final long serialVersionUID = 2750373201787269766L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names and revision numbers of the checked-in versions to archive.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions; 

  /**
   * The archiver plugin instance used to perform the archive operation.
   */ 
  private BaseArchiver  pArchiver;

}
  
