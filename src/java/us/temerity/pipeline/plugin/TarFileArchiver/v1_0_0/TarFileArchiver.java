// $Id: TarFileArchiver.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.TarFileArchiver.v1_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T A R   F I L E   A R C H I V E R                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Archive to tar(1) file. <P> 
 * 
 * This archiver defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Archive Directory <BR>
 *   <DIV style="margin-left: 40px;">
 *     The location where archive tarballs are stored.
 *   </DIV> <BR>
 * 
 *   Capacity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The maximum uncompressed size (in bytes) to make an archive tarball. <BR>
 *   </DIV> <BR>
 * 
 *   Compress <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether the TAR archive should be compressed with gzip(1).
 *   </DIV> 
 * </DIV> <P> 
 */
public
class TarFileArchiver
  extends BaseArchiver
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TarFileArchiver()
  {
    super("TarFile", new VersionID("1.0.0"), "Temerity", 
	  "Archive to tar(1) file.");

    {
      ArchiverParam param = 
	new DirectoryArchiverParam
	("ArchiveDirectory", 
	 "The location where archive tarballs are stored.",
	 PackageInfo.sTempDir.getPath());
      addParam(param);
    }

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
	new BooleanArchiverParam
	("Compress", 
	 "Whether the TAR archive should be compressed with gzip(1).",
	 true);
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add("ArchiveDirectory");
      layout.add("Capacity");
      layout.add("Compress");

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
   * @param env
   *   The cooked toolset environment.
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
   Map<String,String> env, 
   File dir, 
   File outFile, 
   File errFile 
  ) 
    throws PipelineException
  {
    File adir = null;
    {
      String path = (String) getParamValue("ArchiveDirectory");
      if(path == null) 
	throw new PipelineException
	  ("The archive directory cannot be (null)");
      adir = new File(path);
    }

    Boolean compress = (Boolean) getParamValue("Compress");
    if(compress == null) 
      throw new PipelineException
	("The compression flag cannot be (null)");

    File tarball = new File(adir, name + (compress ? ".tgz" : ".tar"));

    ArrayList<String> args = new ArrayList<String>();
    args.add("--verbose");
    args.add("--create");
    if(compress) 
      args.add("--gzip");
    args.add("--dereference");
    args.add("--file=" + tarball);

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
   * @param stamp
   *   The timestamp of the start of the restore operation.
   * 
   * @param files
   *   The names of the files to restore relative to the base production directory.
   * 
   * @param env
   *   The cooked toolset environment.
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
   long stamp, 
   Collection<File> files, 
   Map<String,String> env, 
   File dir,
   File outFile, 
   File errFile  
  ) 
    throws PipelineException
  {
    File adir = null;
    {
      String path = (String) getParamValue("ArchiveDirectory");
      if(path == null) 
	throw new PipelineException
	  ("The archive directory cannot be (null)");
      adir = new File(path);
    }

    Boolean compress = (Boolean) getParamValue("Compress");
    if(compress == null) 
      throw new PipelineException
	("The compression flag cannot be (null)");

    File tarball = new File(adir, name + (compress ? ".tgz" : ".tar"));

    ArrayList<String> args = new ArrayList<String>();   
    args.add("--verbose");
    args.add("--extract");
    if(compress) 
      args.add("--ungzip");
    args.add("--file=" + tarball);

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

  private static final long serialVersionUID = -6725655154336418174L;

}


