// $Id: TarballArchiver.java,v 1.2 2005/02/07 14:52:39 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T A R B A L L   A R C H I V E R                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Archives files as a gzip(1) compressed tar(1) archive. <P> 
 * 
 * This archiver defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Capacity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The maximum uncompressed size (in bytes) to make an archive tarball. <BR>
 *   </DIV> <BR>
 * 
 *   Archive Directory <BR>
 *   <DIV style="margin-left: 40px;">
 *     The location where archive tarballs are stored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class TarballArchiver
  extends BaseArchiver
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TarballArchiver()
  {
    super("Tarball", new VersionID("1.0.0"),
	  "Archives files as a gzip(1) compressed tar(1) archive.");

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
	 "The location where archive tarballs are stored.",
	 PackageInfo.sTempDir);
      addParam(param);
    }
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
   * Archives the given set of files.  <P> 
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
    File tarball = new File(adir, name + ".tgz");

    Map<String,String> env = System.getenv();

    ArrayList<String> args = new ArrayList<String>();
    args.add("--create");
    args.add("--gzip");
    args.add("--file=" + tarball);
    args.add(tarball.getPath());

    for(File file : files) 
      args.add(file.getPath());

    SubProcessLight proc = 
      new SubProcessLight("TarballArchive", "tar", args, env, dir);
    try {
      proc.start();
      proc.join();
      if(!proc.wasSuccessful()) 
	throw new PipelineException
	  ("Unable to create the archive (" + tarball  + "):\n\n" + 
	   "  " + proc.getStdErr());
    }
    catch(InterruptedException ex) {
      throw new PipelineException
	("Interrupted while creating the archive (" + tarball  + ")!");
    }
  }

  /** 
   * Restores the given set of files. <P> 
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
    File tarball = new File(adir, name + ".tgz");

    Map<String,String> env = System.getenv();

    ArrayList<String> args = new ArrayList<String>();
    args.add("--extract");
    args.add("--ungzip");
    args.add("--file=" + tarball);

    for(File file : files) 
      args.add(file.getPath());

    SubProcessLight proc = 
      new SubProcessLight("TarballRestore", "tar", args, env, dir);
    try {
      proc.start();
      proc.join();
      if(!proc.wasSuccessful()) 
	throw new PipelineException
	  ("Unable to restore the archive (" + tarball  + "):\n\n" + 
	   "  " + proc.getStdErr());
    }
    catch(InterruptedException ex) {
      throw new PipelineException
	("Interrupted while restoring the archive (" + tarball  + ")!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6725655154336418175L;

}


