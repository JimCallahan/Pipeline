// $Id: CheckSum.java,v 1.16 2009/04/01 21:17:58 jim Exp $

package us.temerity.pipeline.core;
 
import us.temerity.pipeline.*;

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K   S U M                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Computes the checksum of the files associated with working and checked-in versions of 
 * nodes and uses these checksums to optimize file comparisons. <P> 
 * 
 * @see MessageDigest
 * @see MasterMgr
 */ 
public
class CheckSum
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct using the given digest algorithm. <P> 
   * 
   * @param dir 
   *   The root production directory.
   */ 
  public
  CheckSum
  (
   File dir
  ) 
  {
    init(dir);
  }


  /*-- CONTRUCTION HELPERS -----------------------------------------------------------------*/

  private void 
  init
  (
   File dir
  ) 
  {
    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");

    if(dir == null) 
      throw new IllegalArgumentException("The root production directory cannot be (null)!");
    if(!dir.isDirectory()) 
      throw new IllegalArgumentException 
	("The root production directory (" + dir + ") was not valid!");
    pProdDir = dir;

    pCheckSumDir = new File(dir, "checksum");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the absolute canonical file system path to the checksum file based on the 
   * given node file path. <P> 
   * 
   * If the given file is associated with a working version the <CODE>path</CODE> 
   * argument should have the following form: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   /working/<I>author</I>/<I>view</I>/<I>fully-resolved-node-path</I>/<I>file-name</I> <P>
   *   
   *   Where the (<I>author</I>) is the name of the user owning the working version of 
   *   the node.  The (<I>view</I>) is the name of the particular working area view of 
   *   which the working version is a member. The (<I>fully-resolved-node-path</I>) is all 
   *   but the last component of the fully resolved name of the node.  Finally, 
   *   (<I>file-name)</I> is the simple name of the file used to generate the checksum. <P> 
   * </DIV>
   * 
   * Alternatively, if the given file is associated with a checked-in version the 
   * <CODE>path</CODE> argument should have the following form: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   /repository/<I>fully-resolved-node-path</I>/<I>revision-number</I>/<I>file-name</I> <P>
   *       
   *   Where the (<I>revision-number</I>) is the revision number of the checked-in version 
   *   owning the file.  The other components of the name have the same meaining as 
   *   described above for working versions. <P> 
   * </DIV>
   * 
   * @param path 
   *   The fully resolved node file path.
   * 
   * @return 
   *   The absolute canonical file system path of the checksum file.
   * 
   * @throws PipelineException
   *   If unable to determine the path of the checksum file.
   */ 
  public File
  checkSumFile
  (
   File path  
  ) 
    throws PipelineException
  {
    return new File(pCheckSumDir, path.getPath());
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate an up-to-date checksum file for the given node file path. <P> 
   *
   * No action will be taken if the source file is missing, if the source file is not a 
   * regular file or if there already exists a checksum file which is newer than 
   * the given source file. <P> 
   * 
   * @param path 
   *   The fully resolved node file path.
   * 
   * @throws PipelineException
   *   If unable to generate the checksum file.
   */ 
  public void
  refresh
  (
   File path
  ) 
    throws PipelineException
  {
    if(path == null) 
      throw new IllegalArgumentException("The source node file path cannot be (null)!");

    /* abort early if the source file is missing or is not a regular file */ 
    File file = new File(pProdDir, path.getPath());
    if(!file.isFile()) 
      return;

    /* abort early if the checksum file is up-to-date */ 
    File sfile = checkSumFile(path);    
    if(sfile.isFile()) {
      try {
        long sstamp = NativeFileSys.lastModOrChange(sfile);
        long stamp  = NativeFileSys.lastModOrChange(file);
        if(sstamp >= stamp)
          return; 
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to determine the last modification/change timestamps for a production " + 
           "file (" + file + ") and/or its checksum (" + sfile + ") even though both " + 
           "files appear to exist!  This is likely a symptom of a serious file server " + 
           "and/or file system configuration problem and you should notify your Systems " + 
           "Administrator of this error immediately. More specifically:\n\n" + 
           "  " + ex.getMessage());
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Finer,
       "Rebuilding checksum for: " + file);

    /* verify (and possibly create) checksum directory */ 
    {
      File dir = sfile.getParentFile();

      /* try multiple times, in case another program creates the directory in between the 
	   check for existance of the directory and the attempt to create it */ 
      int tries = 10;
      int wk;
      for(wk=0; wk<tries; wk++) {
	if(dir.exists()) {
	  if(!dir.isDirectory()) 
	    throw new PipelineException
	      ("The checksum directory (" + dir + ") exists but is not a directory!");
	  break;
	}
	else {
	  try {
	    if(dir.mkdirs())
	      break;
	  }
	  catch (SecurityException ex) {
	    throw new PipelineException
	      ("Unable to create the checksum directory (" + dir + ")!");
	  }
	}
      }
      
      if(wk == tries) 
	throw new PipelineException
	  ("Unable to create the checksum directory (" + dir + ") after (" + tries + 
	   ") attempts!");
    }

    TaskTimer timer = null;
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finer))
      timer = new TaskTimer();

    /* generate the checksum */ 
    byte checksum[] = null;
    try {
      checksum = NativeFileSys.md5sum(new Path(file.getPath())); 
    }
    catch(IOException ex) {
      throw new PipelineException(ex);
    }

    /* write the checksum to file */ 
    try {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sum, LogMgr.Level.Finest,
	 "Writing the checksum file: " + sfile);

      FileOutputStream out = new FileOutputStream(sfile);	
      try {
	out.write(checksum);
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the checksum file (" + sfile + ")!");
      }
      finally {
	out.flush();
	out.close();
      }
    }  
    catch(FileNotFoundException ex) {
      throw new PipelineException
	("Could not open the checksum file (" + sfile + ")!");
    }
    catch(SecurityException ex) {
      throw new PipelineException
	("No permission to write the checksum file (" + sfile + ")!");
    } 
    catch (IOException ex) { 
      throw new IllegalStateException();
    }

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Sum, LogMgr.Level.Finer)) {
      timer.suspend(); 
      LogMgr.getInstance().log
        (LogMgr.Kind.Sum, LogMgr.Level.Finer,
         "\n  " + timer);
    }

    LogMgr.getInstance().flush();
  }

  /**
   * Generate an up-to-date checksum file for the given node file path. <P> 
   *
   * No action will be taken if the source file is missing, if the source file is not a 
   * regular file or if there already exists a checksum file which is newer than 
   * the given source file. <P> 
   * 
   * @param path 
   *   The fully resolved node file path.
   * 
   * @throws PipelineException
   *   If unable to generate the checksum file.
   */ 
  public void
  refresh
  (
   Path path
  ) 
    throws PipelineException
  {
    refresh(path.toFile());
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Indirectly compare the two files specified by the given node file paths by comparing 
   * their associated checksum files. <P> 
   * 
   * The checksum files associated with the given node file paths are assumed to up-to-date 
   * by a previous call to {@link #refresh refresh}. 
   * 
   * @param fileA 
   *   The first fully resolved node file path.
   * 
   * @param fileB 
   *   The second fully resolved node file path.
   * 
   * @return 
   *   Whether the given files are identical.
   * 
   * @throws PipelineException
   *   If unable to compare the associated checksum files.
   */
  public boolean
  compare
  (
   File fileA,   
   File fileB   
  ) 
    throws PipelineException
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Fine,
       "Comparing (checksums of): " + fileA + " " + fileB);

    /* make sure the files are distinct */ 
    if(fileA.compareTo(fileB) == 0) 
      throw new PipelineException
	("Attempted to compare the node path (" + fileA + ") with itself!");

    /* checksums */ 
    byte[] sumA = readCheckSum(checkSumFile(fileA));
    byte[] sumB = readCheckSum(checkSumFile(fileB));
  
    /* compare checksums */ 
    return Arrays.equals(sumA, sumB);
  } 

  /**
   * Indirectly compare the two files specified by the given node file paths by comparing 
   * their associated checksum files. <P> 
   * 
   * The checksum files associated with the given node file paths are assumed to up-to-date 
   * by a previous call to {@link #refresh refresh}. 
   * 
   * @param pathA 
   *   The first fully resolved node file path.
   * 
   * @param pathB 
   *   The second fully resolved node file path.
   * 
   * @return 
   *   Whether the given files are identical.
   * 
   * @throws PipelineException
   *   If unable to compare the associated checksum files.
   */
  public boolean
  compare
  (
   Path pathA,   
   Path pathB   
  ) 
    throws PipelineException
  { 
    return compare(pathA.toFile(), pathB.toFile());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Validate a restored file associated with a checked-in node using the previously 
   * generated repository checksum and the checksum of the restored file.
   * 
   * @parm restoreDir
   *    The temporary restore root directory.
   * 
   * @param path
   *    The fully resolved node file path relative to the repository/restore root directory.
   * 
   * @throws PipelineException
   *   If unable to compare the associated checksums.
   */
  public boolean
  validateRestore
  (
   File restoreDir, 
   File path
  ) 
    throws PipelineException
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Sum, LogMgr.Level.Fine,
       "Validating Restored File: " + path);

    /* read the original checksum */ 
    byte[] sumA = readCheckSum(new File(pCheckSumDir, "repository" + path));

    /* generate the checksum */ 
    byte sumB[] = null;
    try {
      File file = new File(restoreDir, path.getPath());
      sumB = NativeFileSys.md5sum(new Path(file.getPath())); 
    }
    catch(IOException ex) {
      throw new PipelineException(ex);
    }

    /* compare checksums */ 
    return Arrays.equals(sumA, sumB);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Read in the bytes from a checksum on disk.
   */ 
  private byte[] 
  readCheckSum
  (
   File file
  )
    throws PipelineException 
  {
    byte[] sum = new byte[sByteSize];

    try {
      FileInputStream in = new FileInputStream(file);	
      try {
        in.read(sum);
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to read the checksum file (" + file + ")!");
      }
      finally {
        in.close();
      }
    } 
    catch(FileNotFoundException ex) {
      throw new PipelineException
        ("The checksum file (" + file + ") did not exist!");
    }
    catch(SecurityException ex) {
      throw new PipelineException
	("No permission to read the checksum file (" + file + ")!");
    }   
    catch (IOException ex) {     
      throw new IllegalStateException(); 
    } 

    return sum;    
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Size (in bytes) of checksum data.
   */ 
  private final int sByteSize = 16; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The root production directory.
   */
  private File  pProdDir; 

  /**
   * The root checksum directory.
   */
  private File  pCheckSumDir; 
}



