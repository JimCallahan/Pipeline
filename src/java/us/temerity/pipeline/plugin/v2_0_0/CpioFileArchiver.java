// $Id: CpioFileArchiver.java,v 1.3 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C P I O   F I L E   A R C H I V E R                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Archive to cpio(1) file. <P> 
 * 
 * This archiver defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Archive Directory <BR>
 *   <DIV style="margin-left: 40px;">
 *     The location where archive files are stored.
 *   </DIV> <BR>
 * 
 *   Capacity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The maximum size (in bytes) to make an archive file. <BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class CpioFileArchiver
  extends BaseArchiver
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CpioFileArchiver()
  {
    super("CpioFile", new VersionID("2.0.0"), "Temerity",
	  "Archive to cpio(1) file.");

    {
      ArchiverParam param = 
	new DirectoryArchiverParam
	("ArchiveDirectory", 
	 "The location where archive files are stored.",
	 PackageInfo.sTempDir.getPath());
      addParam(param);
    }

    {
      ArchiverParam param = 
	new ByteSizeArchiverParam
	("Capacity", 
	 "The maximum size (in bytes) to make an archive file.", 
	 1073741824L);
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add("ArchiveDirectory");
      layout.add("Capacity");

      setLayout(layout);      
    }
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
    File afile = null;
    {
      String path = (String) getParamValue("ArchiveDirectory");
      if(path == null) 
	throw new PipelineException
	  ("The archive directory cannot be (null)");
      afile = new File(path, name + ".cpio");
    }

    File script = createArchiveTemp(name, 0755, "bash");
    try {
      FileWriter out = new FileWriter(script);

      out.write
	("#!/bin/bash\n" +
	 "\n" +
	 "cpio --create --format=newc --dereference --file=" + afile + " <<EOF\n");

      for(File file : files) 
	out.write(file.getPath().substring(1) + "\n");
      
      out.write("EOF\n");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to create the temporary shell script (" + script + ")!\n" + 
	 ex.getMessage());
    }

    try {
      return new SubProcessHeavy
	(getName(), script.getPath(), new ArrayList<String>(), System.getenv(), 
	 dir, outFile, errFile);
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
   * @param stamp
   *   The timestamp of the start of the restore operation.
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
   Date stamp, 
   Collection<File> files, 
   File dir,
   File outFile, 
   File errFile  
  ) 
    throws PipelineException
  {
    File afile = null;
    {
      String path = (String) getParamValue("ArchiveDirectory");
      if(path == null) 
	throw new PipelineException
	  ("The archive directory cannot be (null)");
      afile = new File(path, name + ".cpio");
    }
    
    Map<String,String> env = System.getenv();

    ArrayList<String> args = new ArrayList<String>();   
    args.add("--extract");
    args.add("--file=" + afile);

    for(File file : files) 
      args.add(file.getPath().substring(1));

    try {
      return new SubProcessHeavy
	(getName(), "cpio", args, env, dir, outFile, errFile);
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

  private static final long serialVersionUID = 2683358205766375314L;

}


