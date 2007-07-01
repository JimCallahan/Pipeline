// $Id: MiscArchiveReq.java,v 1.6 2007/07/01 23:54:23 jim Exp $

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
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param prefix
   *   A prefix to prepend to the created archive volume name.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to archive.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the archive operation.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed or
   *   <CODE>null</CODE> to use the default toolset.
   * 
   * @param dryrun
   *   Whether to show what files would have been archived without actually performing
   *   the archive operation. 
   */
  public
  MiscArchiveReq
  (
   String prefix, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset, 
   boolean dryrun
  )
  {
    super();

    if(prefix == null) 
      throw new IllegalArgumentException
	("The volume prefix cannot be (null)!");
    pPrefix = prefix; 

    if(versions == null) 
      throw new IllegalArgumentException
	("The checked-in versions cannot be (null)!");
    pVersions = versions;

    if(archiver == null) 
      throw new IllegalArgumentException
	("The archiver cannot be (null)!");
    pArchiver = archiver;

    pToolset = toolset;
    pDryRun  = dryrun; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the prefix to prepend to the created archive volume name.
   */ 
  public String
  getPrefix()
  {
    return pPrefix; 
  }

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

  /**
   * Get the name of the toolset environment under which the archiver is executed or
   *   <CODE>null</CODE> to use the default toolset.
   */ 
  public String
  getToolset()
  {
    return pToolset;
  }

  /**
   * Whether to show what files would have been archived without actually performing
   * the archive operation. 
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
    out.writeObject(pPrefix);
    out.writeObject(pVersions);
    out.writeObject(new BaseArchiver(pArchiver));
    out.writeObject(pToolset);
    out.writeObject(pDryRun);
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
    pPrefix = (String) in.readObject();
    pVersions = (TreeMap<String,TreeSet<VersionID>>) in.readObject();
    
    BaseArchiver arch = (BaseArchiver) in.readObject();
    try {
      PluginMgrClient client = PluginMgrClient.getInstance();
      pArchiver = client.newArchiver(arch.getName(), arch.getVersionID(), arch.getVendor());
      pArchiver.setParamValues(arch);
    }
    catch(PipelineException ex) {
      throw new IOException(ex.getMessage());
    }

    pToolset = (String) in.readObject();
    pDryRun  = (Boolean) in.readObject();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2750373201787269766L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The prefix to prepend to the created archive volume name.
   */ 
  private String pPrefix; 

  /**
   * The fully resolved names and revision numbers of the checked-in versions to archive.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions; 

  /**
   * The archiver plugin instance used to perform the archive operation.
   */ 
  private BaseArchiver  pArchiver;

  /**
   * The name of the toolset environment under which the archiver is executed or
   * <CODE>null</CODE> to use the default toolset.
   */
  private String  pToolset; 

  /**
   * Whether to show what files would have been archived without actually performing
   * the archive operation. 
   */ 
  private boolean  pDryRun; 

}
  
