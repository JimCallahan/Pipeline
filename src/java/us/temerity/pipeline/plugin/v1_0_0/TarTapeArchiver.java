// $Id: TarTapeArchiver.java,v 1.1 2005/03/18 16:35:20 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T A R   T A P E   A R C H I V E R                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Archive to magnetic tape using the tar(1) utility. <P> 
 * 
 * This archiver defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   TapeDevice <BR>
 *   <DIV style="margin-left: 40px;">
 *     The device of the tape drive to use.  The value of this parameter is passed directly
 *     to the --file option of tar(1).  See the tar(1) manpag for details.
 *   </DIV> <BR>
 * 
 *   Capacity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The maximum size (in bytes) of files written to one tape. <BR>
 *   </DIV> 
 * </DIV> <P> 
 */
public
class TarTapeArchiver
  extends BaseArchiver
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TarTapeArchiver()
  {
    super("TarTape", new VersionID("1.0.0"),
	  "Archive to magnetic tape using the tar(1) utility.");

    {
      ArchiverParam param = 
	new DirectoryArchiverParam
	("TapeDevice", 
	 "The device of the tape drive to use.",
	 "/dev/rmt0");
      addParam(param);
    }

    {
      ArchiverParam param = 
	new ByteSizeArchiverParam
	("Capacity", 
	 "The maximum size (in bytes) of files written to one tape.",
	 1073741824L);
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add("TapeDevice");
      layout.add("Capacity");

      setLayout(layout);      
    }

    underDevelopment();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

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
   * Creates a new archive volume containing the given set of files.  <P> 
   *  
   * @param name 
   *   The name of the backup.
   * 
   * @param files
   *   The names of the files to archive relative to the base production directory.
   * 
   * @param dir
   *   The base repository directory.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will create the archive volume containing the given files.
   * 
   * @throws PipelineException
   *   If unable to prepare a SubProcess due to illegal archiver pararameters.
   */  
  public SubProcessHeavy
  archive
  (
   String name, 
   Collection<File> files, 
   File dir, 
   File outFile, 
   File errFile 
  ) 
    throws PipelineException
  {
    File device = null; 
    {
      String path = (String) getParamValue("TapeDevice");
      if(path == null) 
	throw new PipelineException
	  ("The tape device cannot be (null)");
      device = new File(path);
    }

    Map<String,String> env = System.getenv();

    ArrayList<String> args = new ArrayList<String>();
    args.add("--verbose");
    args.add("--create");
    args.add("--dereference");
    args.add("--file=" + device);

    for(File file : files) 
      args.add(file.getPath().substring(1));

    try {
      return new SubProcessHeavy
	(getName(), "tar", args, env, dir, outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform the archive operation!\n" +
	 ex.getMessage());
    }     
  }

  /** 
   * Restores the given set of files from an archive volume. <P> 
   * 
   * @param name 
   *   The name of the backup.
   * 
   * @param files
   *   The names of the files to restore relative to the base production directory.
   * 
   * @param dir
   *   The base repository directory.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will restore the given file from the archive volume.
   * 
   * @throws PipelineException
   *   If unable to prepare a SubProcess due to illegal archiver pararameters.
   */  
  public SubProcessHeavy 
  restore
  (
   String name, 
   Collection<File> files, 
   File dir,
   File outFile, 
   File errFile  
  ) 
    throws PipelineException
  {
    File device = null; 
    {
      String path = (String) getParamValue("TapeDevice");
      if(path == null) 
	throw new PipelineException
	  ("The tape device cannot be (null)");
      device = new File(path);
    }

    Map<String,String> env = System.getenv();

    ArrayList<String> args = new ArrayList<String>();   
    args.add("--verbose");
    args.add("--extract");
    args.add("--file=" + device);

    for(File file : files) 
      args.add(file.getPath().substring(1));

    try {
      return new SubProcessHeavy
	(getName(), "tar", args, env, dir, outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform the restore operation!\n" +
	 ex.getMessage());
    }     
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3451740157729490251L;

}


