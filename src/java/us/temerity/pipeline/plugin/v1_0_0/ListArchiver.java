// $Id: ListArchiver.java,v 1.2 2005/02/20 20:52:03 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   A R C H I V E R                                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates listings of the files to be manually archived. <P> 
 * 
 * The user is resposible for performing the archive/restore opertion on the files listed 
 * by this plugin before confirming the operation.  Failure to do so can result in permanent
 * data loss and/or database corruption. <P> 
 * 
 * This plugin should be used as a last resort when it is impossible to directly perform
 * file backup in an Archiver plugin.  This may be the case when the backup device resides on
 * an unreachable machine or cannot be controlled automatically. <P> 
 * 
 * This archiver defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Capacity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The maximum uncompressed size (in bytes) to make an archive volume. <BR>
 *   </DIV> <BR>
 * 
 *   Archive Directory <BR>
 *   <DIV style="margin-left: 40px;">
 *     The location where archive listings are stored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class ListArchiver
  extends BaseArchiver
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ListArchiver()
  {
    super("List", new VersionID("1.0.0"),
	  "Generates listings of the files to be manually archived.");

    {
      ArchiverParam param = 
	new ByteSizeArchiverParam
	("Capacity", 
	 "The maximum size (in bytes) to make an archive.", 
	 1073741824L);
      addParam(param);
    }

    {
      ArchiverParam param = 
	new DirectoryArchiverParam
	("ArchiveDirectory", 
	 "The location where archive/restore listing files are stored.",
	 PackageInfo.sTempDir);
      addParam(param);
    }

    underDevelopment();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the archiver requires manual confirmation before initiating an archive or 
   * restore operation. <P> 
   */ 
  public boolean
  isManual()
  {
    return false;
  }

  /**
   * Whether the archiver requires manual confirmation to signal that the archive operation 
   * has been completed. 
   */ 
  public boolean
  manualArchiveComplete()
  {
    return true;
  }

  /**
   * Whether the archiver requires manual confirmation to signal that the restore operation 
   * has been completed. 
   */ 
  public boolean
  manualRestoreComplete()
  {
    return true;
  }

  /**
   * Get the capacity of the media (in bytes).
   */ 
  public long 
  getCapacity()
  {
    try {
      Long bytes = (Long) getParamValue("Capacity");
      if(bytes != null)    
	return bytes;
    }
    catch(PipelineException ex) {
    }
    
    return 0L;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Creates a listing of the set of files to be archived. <P> 
   *  
   * @param name 
   *   The name of the backup.
   * 
   * @param files
   *   The names of the files to archive relative to the base production directory.
   * 
   * @param dir
   *   The base production directory.
   * 
   * @throws PipelineException
   *   If unable to successfully archive all of the given files.
   */  
  public void 
  archive
  (
   String name, 
   Collection<File> files, 
   File dir
  ) 
    throws PipelineException
  {
    File adir = (File) getParamValue("ArchiveDirectory");
    if((adir == null) || !adir.isDirectory()) 
      throw new PipelineException
	("The archive directory (" + adir + ") was not valid!");

    File listing = new File(adir, name + ".archive");
    try {
      StringBuffer buf = new StringBuffer();

      for(File file : files) 
	buf.append(dir + file.toString() + "\n");

      FileWriter out = new FileWriter(listing);
      out.write(buf.toString());	
      out.close();
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to create the archive listing file (" + listing + ")!");
    }
  }

  /** 
   * Creates a listing of the set of files to be restored. <P> 
   * 
   * @param name 
   *   The name of the backup.
   * 
   * @param files
   *   The names of the files to restore relative to the base production directory.
   * 
   * @param dir
   *   The base production directory.
   * 
   * @throws PipelineException
   *   If unable to successfully restore all of the given files.
   */  
  public void 
  restore
  (
   String name, 
   Collection<File> files, 
   File dir   
  ) 
    throws PipelineException
  {
    File adir = (File) getParamValue("ArchiveDirectory");
    if((adir == null) || !adir.isDirectory()) 
      throw new PipelineException
	("The archive directory (" + adir + ") was not valid!");

    File listing = new File(adir, name + ".restore");
    try {
      StringBuffer buf = new StringBuffer();

      for(File file : files) 
	buf.append(dir + file.toString() + "\n");

      FileWriter out = new FileWriter(listing);
      out.write(buf.toString());	
      out.close();
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to create the restore listing file (" + listing + ")!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5644507467020746013L;

}


