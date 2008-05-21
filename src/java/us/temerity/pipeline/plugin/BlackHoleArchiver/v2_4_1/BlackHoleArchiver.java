// $Id: BlackHoleArchiver.java,v 1.1 2008/05/21 02:00:27 jim Exp $

package us.temerity.pipeline.plugin.BlackHoleArchiver.v2_4_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B L A C K   H O L E   A R C H I V E R                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * A fake archiver which does nothing! <P> 
 * 
 * This archiver can be used to mark node versions which should be permanently deleted when
 * offlined.  Because this archiver does not stored the node versions anywhere, offlining
 * is equivalent to deletion.  For this reason, great care should be take when using this
 * archiver.  Once a node version has been offlined and the only archives made of the node
 * version where created with this archiver, it NEVER be restored! <P> 
 * 
 * The reason this archiver exists is to facilitate automated permanent deletion of node 
 * versions for nodes used soley to communicate changed to external systems which should
 * not be retained and will never need to be restored. 
 */
public
class BlackHoleArchiver
  extends BaseArchiver
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  BlackHoleArchiver()
  {
    super("BlackHole", new VersionID("2.4.1"), "Temerity", 
	  "A fake archiver which does nothing!");

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
    return Long.MAX_VALUE; 
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
    File script = createArchiveTemp(name, 0755, "bash");
    try {
      FileWriter out = new FileWriter(script);
      
      out.write
	("#!/bin/bash\n" +
	 "\n" +
	 "echo \"----------------------------------------------------------------------\"\n" +
	 "echo \"  NODE VERSION FILES ARCHIVED TO NOWHERE:\"\n" +
	 "echo \"----------------------------------------------------------------------\"\n");
      
      for(File file : files) 
	out.write("echo \"" + file.getPath() + "\"\n");

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
    throw new PipelineException
      ("There is no way to restore files using the BlackHole archiver!"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7095582951324473661L;

}


