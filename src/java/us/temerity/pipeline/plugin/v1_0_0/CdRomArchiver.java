// $Id: CdRomArchiver.java,v 1.1 2005/03/15 19:14:37 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T A R B A L L   A R C H I V E R                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Archive to removable CD-ROM media. <P> 
 * 
 * This archiver defines the following parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   SCSI Device <BR>
 *   <DIV style="margin-left: 40px;">
 *     The SCSI device use to burn the CD-ROM.
 *   </DIV> <BR>
 * 
 *   Restore Mount <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The root mount directory of the CD-ROM during restore operations.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * In order for this plugin to work properly the cdrecord(1) binary must have the set user
 * ID bit set so that non-root users can burn CD-ROMs.  The plugin is always run as the 
 * "pipeline" user and cannot be run as root.
 */
public
class CdRomArchiver
  extends BaseArchiver
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CdRomArchiver()
  {
    super("CD-ROM", new VersionID("1.0.0"),
	  "Archive to removable CD-ROM media.");

    {
      ArchiverParam param = 
	new StringArchiverParam
	("SCSIDevice", 
	 "The SCSI device use to burn the CD-ROM.", 
	 "1,1,0");
      addParam(param);
    }

    {
      ArchiverParam param = 
	new StringArchiverParam
	("RestoreMount", 
	 "The root mount directory of the CD-ROM during restore operations.", 
	 "/mnt/cdrom1");
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add("SCSIDevice");
      layout.add("RestoreMount");

      setLayout(layout);      
    }

    underDevelopment();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the archiver requires manual confirmation before initiating an archive or 
   * restore operation. 
   */ 
  public boolean
  isManual()
  {
    return true;
  }

  /**
   * Get the capacity of the media (in bytes). <P> 
   * 
   * Hardcoded to 703M for standard CD-ROMs.
   */ 
  public long 
  getCapacity()
  { 
    return 737148928L;
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
    /* write the cdrecord(1) file list */ 
    File filelist = createArchiveTemp(name, 0644, "txt");
    try {
      FileWriter out = new FileWriter(filelist);
      
      int dlen = dir.getPath().length();
      for(File file : files) {
	File path = new File(dir, file.getPath());
	try {
	  String real = NativeFileSys.realpath(path).getPath();
	  out.write(file.getParent() + "/=" + real.substring(dlen+1) + "\n");
	}
	catch(IOException ex) {
	  throw new PipelineException(ex);
	}
      }
      
      out.close();      
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to create the temporary file list (" + filelist + ")!\n" + 
	 ex.getMessage());
    }

    File script = createArchiveTemp(name, 0755, "bash");
    File iso = createArchiveTemp(name, 0644, "raw");
    try {
      FileWriter out = new FileWriter(script);

      out.write
	("#!/bin/bash\n" +
	 "\n" +
	 "mkisofs -R -o " + iso + " -graft-points -path-list " + filelist +
	 " -V " + name + "\n" + 
	 "cdrecord -v speed=2 dev=1,1,0 " + iso + "\n");

      out.close();      
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to create the temporary shell script (" + script + ")!\n" + 
	 ex.getMessage());
    }
    
    /* create the process to run the action */ 
    try {
      return new SubProcessHeavy
 	(getName(), script.getPath(), new ArrayList<String>(), System.getenv(), dir, 
 	 outFile, errFile);
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
    throw new PipelineException("Not implemented yet...");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -36786988013830430L;

}


