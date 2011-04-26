// $Id: TarFileArchiver.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.TarFileArchiver.v2_6_3;

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
 *   Compression Program <BR>
 *   <DIV style="margin-left: 40px;">
 *     The program to use to compress the contents of the TAR archive:<BR>
 *     <DIV style="margin-left: 40px;">
 *       None - No compression.
 *       GZip - Compress using the gzip(1) program.
 *       BZip2 - Compress using the bzip2(1) program.
 *     </DIV> 
 *   </DIV> 
 * 
 *   Compression Level <BR>
 *   <DIV style="margin-left: 40px;">
 *     If the archive is compressed, the level of compression [1-9]: 1=fast ... 9=best
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
    super("TarFile", new VersionID("2.6.3"), "Temerity", 
	  "Archive to tar(1) file.");

    pArchiveLine = 0;
    pRestoreLine = 0;

    {
      ArchiverParam param = 
	new DirectoryArchiverParam
	("ArchiveDirectory", 
	 "The location where archive tarballs are stored.",
	 PackageInfo.getTempPath(OsType.Unix).toOsString(OsType.Unix));
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
      ArrayList<String> progs = new ArrayList<String>();
      progs.add("None");
      progs.add("GZip");
      progs.add("BZip2");

      ArchiverParam param = 
	new EnumArchiverParam
	("CompressionProgram", 
	 "The program to use to compress the contents of the TAR archive.", 
	 "None", progs);
      addParam(param);
    }

    {
      ArrayList<String> levels = new ArrayList<String>();
      levels.add("1 (fast)");
      levels.add("2");
      levels.add("3");
      levels.add("4");
      levels.add("5");
      levels.add("6 (default)");
      levels.add("7");
      levels.add("8");
      levels.add("9 (best)");

      ArchiverParam param = 
	new EnumArchiverParam
	("CompressionLevel", 
	 "If the archive is compressed, the level of compression.", 
	 "6 (default)", levels);
      addParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add("ArchiveDirectory");
      layout.add("Capacity");
      layout.add(null);
      layout.add("CompressionProgram");
      layout.add("CompressionLevel");

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
    File filelist = createArchiveTemp(name, 0644, "txt");
    try {
      FileWriter out = new FileWriter(filelist);
      
      for(File file : files) 
	out.write(file.getPath().substring(1) + "\n");

      out.close();      
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to create the temporary file list (" + filelist + ")!\n" + 
	 ex.getMessage());
    }

    File adir = null;
    {
      String path = (String) getParamValue("ArchiveDirectory");
      if(path == null) 
	throw new PipelineException
	  ("The archive directory cannot be (null)");
      adir = new File(path);
    }

    String compress = null;
    {
      int level = 6;
      {
	String clevel = (String) getParamValue("CompressionLevel");
	if(clevel != null) {
	  try {
	    int n = Integer.parseInt(clevel.substring(0, 1));
	    if((level >= 1) && (level <= 9)) 
	      level = n;
	  }
	  catch(NumberFormatException ex) {
	  }
	}
      }

      File tarball = new File(adir, name); 

      String prog  = (String) getParamValue("CompressionProgram");
      if(prog.equals("GZip")) 
	compress = (" | gzip -c -" + level + " > " + tarball + ".tgz");
      else if(prog.equals("BZip2")) 
	compress = (" | bzip2 -c -" + level + " > " + tarball + ".tbz");
      else 
	compress = (" --file=" + tarball + ".tar");
    }

    File script = createArchiveTemp(name, 0755, "bash");
    try {
      FileWriter out = new FileWriter(script);
      
      out.write
	("#!/bin/bash\n" +
	 "\n" +
	 "echo \"----------------------------------------------------------------------\"\n" +
	 "echo \"  tar --verbose --create --dereference --files-from=" + filelist + 
	 compress + "\"\n" +
	 "echo \"----------------------------------------------------------------------\"\n" +
	 "(tar --verbose --create --dereference --files-from=" + filelist + 
	 compress + ") 2>&1\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to create the temporary shell script (" + script + ")!\n" + 
	 ex.getMessage());
    }
      
    try {
      return new SubProcessHeavy
	(getName(), script.getPath(), new ArrayList<String>(), env, dir, outFile, errFile);
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
   *   The names of the files to restore relative to the base repository directory.
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
    File filelist = createRestoreTemp(name, stamp, 0644, "txt");
    try {
      FileWriter out = new FileWriter(filelist);
      
      for(File file : files) 
	out.write(file.getPath().substring(1) + "\n");

      out.close();      
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to create the temporary file list (" + filelist + ")!\n" + 
	 ex.getMessage());
    }

    File adir = null;
    {
      String path = (String) getParamValue("ArchiveDirectory");
      if(path == null) 
	throw new PipelineException
	  ("The archive directory cannot be (null)");
      adir = new File(path);
    }

    String compress1 = "";
    String compress2 = "";
    {
      File tarball = new File(adir, name); 

      String prog  = (String) getParamValue("CompressionProgram");
      if(prog.equals("GZip")) {
	compress1 = ("gzip -cd " + tarball + ".tgz | ");
	compress2 = " --file=-";
      }
      else if(prog.equals("BZip2")) {
	compress1 = ("bzip2 -cd " + tarball + ".tbz | ");
	compress2 = " --file=-";
      }
      else {
	compress2 = (" --file=" + tarball + ".tar");
      }
    }

    File script = createRestoreTemp(name, stamp, 0755, "bash");
    try {
      FileWriter out = new FileWriter(script);
      
      out.write
	("#!/bin/bash\n" +
	 "\n" +
	 "echo \"----------------------------------------------------------------------\"\n" +
	 "echo \"  " + compress1 + "tar --verbose --extract --files-from=" + filelist + 
	 compress2 + "\"\n" +
	 "echo \"----------------------------------------------------------------------\"\n" +
	 "(" + compress1 + "tar --verbose --extract --files-from=" + filelist + 
	 compress2 + ") 2>&1\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to create the temporary shell script (" + script + ")!\n" + 
	 ex.getMessage());
    }
      
    try {
      return new SubProcessHeavy
	(getName(), script.getPath(), new ArrayList<String>(), env, dir, outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform the archive operation!\n" +
	 ex.getMessage());
    }     
  }



  /*----------------------------------------------------------------------------------------*/
  /*   M O N I T O R I N G                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Monitor the progress of the the archive operation by parsing the STDOUT/STDERR files
   * generated and reporting back to the MasterMgrClient any progress messages and/or 
   * reports of completion percentage.<P> 
   *
   * @param output 
   *   The STDOUT file monitor.
   * 
   * @param error
   *   The STDERR file monitor.
   * 
   * @param timer
   *   The archive operation timer supplied by the MasterMgr server.
   * 
   * @param opn
   *   The operation progress notifier.  
   * 
   * @throws PipelineException
   *   If the archive opertion should be aborted early in response to STDOUT/STDERR output.
   */ 
  public void 
  archiveMonitor
  (
   FileMonitor output, 
   FileMonitor error, 
   TaskTimer timer, 
   OpNotifiable opn
  )
    throws PipelineException
  {
    try {
      int totalOut = output.getNumLines();
      if(totalOut > pArchiveLine) {
        for(; pArchiveLine<totalOut; pArchiveLine++) {
          if(pArchiveLine < 3)
            continue;
          
          String line = output.getLines(pArchiveLine, 1);
          if(line.length() > 0) 
            opn.step(timer, "Archived: /" + line);
        }
      }
    }
    catch(IOException ex) {
    }
  }

  /**
   * Monitor the progress of the the restore operation by parsing the STDOUT/STDERR files
   * generated and reporting back to the MasterMgrClient any progress messages and/or 
   * reports of completion percentage.<P> 
   *
   * @param output 
   *   The STDOUT file monitor.
   * 
   * @param error
   *   The STDERR file monitor.
   * 
   * @param timer
   *   The restore operation timer supplied by the MasterMgr server.
   * 
   * @param opn
   *   The operation progress notifier.  
   * 
   * @throws PipelineException
   *   If the restore opertion should be aborted early in response to STDOUT/STDERR output.
   */ 
  public void 
  restoreMonitor
  (
   FileMonitor output, 
   FileMonitor error, 
   TaskTimer timer, 
   OpNotifiable opn
  )
    throws PipelineException
  {
    try {
      int totalOut = output.getNumLines();
      if(totalOut > pRestoreLine) {
        for(; pRestoreLine<totalOut; pRestoreLine++) {
          if(pRestoreLine < 3)
            continue;
          
          String line = output.getLines(pRestoreLine, 1);
          if(line.length() > 0) 
            opn.step(timer, "Extracted: /" + line);
        }
      }
    }
    catch(IOException ex) {
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7886950621662155986L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The line number of the last STDOUT archive/restore operation output file line processed.
   */ 
  private int pArchiveLine; 
  private int pRestoreLine; 

}


