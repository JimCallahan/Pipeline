// $Id: MiscRestoreReq.java,v 1.2 2005/03/21 07:04:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   R E S T O R E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to restore the given checked-in versions from the given archive. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscRestoreReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The unique name of the archive containing the checked-in versions to restore.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to restore.
   * 
   * @param archiver
   *   The alternative archiver plugin instance used to perform the restore operation
   *   or <CODE>null</CODE> to use the default archiver.
   */
  public
  MiscRestoreReq
  (
    String name,
    TreeMap<String,TreeSet<VersionID>> versions, 
    BaseArchiver archiver
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The archive name cannot be (null)!");
    pName = name;

    if(versions == null) 
      throw new IllegalArgumentException
	("The checked-in versions cannot be (null)!");
    pVersions = versions;

    pArchiver = archiver;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique name of the archive containing the checked-in versions to restore.
   */ 
  public String
  getName() 
  {
    return pName; 
  }

  /**
   * Get the fully resolved names and revision numbers of the checked-in versions to restore.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions()
  {
    return pVersions; 
  }


  /**
   * Get the alternative archiver plugin instance used to perform the restore operation
   * or <CODE>null</CODE> to use the default archiver.
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
    out.writeObject(pVersions);
    if(pArchiver != null)
      out.writeObject(new BaseArchiver(pArchiver));
    else 
      out.writeObject((BaseArchiver) null);
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
    pVersions = (TreeMap<String,TreeSet<VersionID>>) in.readObject();
    
    BaseArchiver archiver = (BaseArchiver) in.readObject();
    if(archiver != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pArchiver = client.newArchiver(archiver.getName(), archiver.getVersionID());
	pArchiver.setParamValues(archiver);
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
    else {
      pArchiver = null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8597316222224313321L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique name of the archive containing the checked-in versions to restore.
   */ 
  private String  pName; 

  /**
   * The fully resolved names and revision numbers of the checked-in versions to restore.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions; 

  /**
   * The alternative archiver plugin instance used to perform the restore operation
   * or <CODE>null</CODE> to use the default archiver.
   */ 
  private BaseArchiver  pArchiver;

}
  
