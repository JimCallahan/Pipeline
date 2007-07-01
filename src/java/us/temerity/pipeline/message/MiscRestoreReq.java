// $Id: MiscRestoreReq.java,v 1.5 2007/07/01 23:54:23 jim Exp $

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
  extends PrivilegedReq
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
   *   or <CODE>null</CODE> to use the original archiver.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed
   *   or <CODE>null</CODE> to use the original toolset. 
   * 
   * @param dryrun
   *   Whether to show what files would have been restored without actually performing
   *   the restore operation. 
   */
  public
  MiscRestoreReq
  (
   String name,
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset, 
   boolean dryrun
  )
  {
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The archive name cannot be (null)!");
    pName = name;

    if(versions == null) 
      throw new IllegalArgumentException
	("The checked-in versions cannot be (null)!");
    pVersions = versions;

    pArchiver = archiver;
    pToolset  = toolset; 
    pDryRun   = dryrun; 
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
   * or <CODE>null</CODE> to use the original archiver.
   */ 
  public BaseArchiver
  getArchiver()
  {
    return pArchiver;
  }

  /**
   * Get the name of the toolset environment under which the archiver is executed
   * or <CODE>null</CODE> to use the original toolset.
   */ 
  public String
  getToolset()
  {
    return pToolset;
  }

  /**
   * Whether to show what files would have been restored without actually performing
   * the restore operation. 
   */ 
  public boolean
  isDryRun() 
  {
    return pDryRun;
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
    out.writeObject(pName);
    out.writeObject(pVersions);
    if(pArchiver != null)
      out.writeObject(new BaseArchiver(pArchiver));
    else 
      out.writeObject((BaseArchiver) null);
    out.writeObject(pToolset);
    out.writeObject(pDryRun);
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
    pName = (String) in.readObject();
    pVersions = (TreeMap<String,TreeSet<VersionID>>) in.readObject();
    
    BaseArchiver arch = (BaseArchiver) in.readObject();
    if(arch != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pArchiver = client.newArchiver(arch.getName(), arch.getVersionID(), arch.getVendor());
	pArchiver.setParamValues(arch);
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
    else {
      pArchiver = null;
    }

    pToolset = (String) in.readObject();
    pDryRun  = (Boolean) in.readObject();
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
   * or <CODE>null</CODE> to use the original archiver.
   */ 
  private BaseArchiver  pArchiver;

  /**
   * The name of the toolset environment under which the archiver is executed
   * or <CODE>null</CODE> to use the original toolset.
   */
  private String  pToolset; 

  /**
   * Whether to show what files would have been restored without actually performing
   * the restore operation. 
   */ 
  private boolean  pDryRun; 

}
  
