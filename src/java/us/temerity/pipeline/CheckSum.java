// $Id: CheckSum.java,v 1.4 2004/03/11 11:00:04 jim Exp $

package us.temerity.pipeline;

import java.lang.*;
import java.security.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K   S U M                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Computes the checksum of the files associated with working and checked-in versions of 
 * nodes and uses these checksums to optimize file comparisons. <P> 
 * 
 * @see MessageDigest
 * @see NodeMgr
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
   * The <CODE>algorithm</CODE> argument may be one of the following: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   "MD2" <BR>
   *   <DIV style="margin-left: 20px;">
   *     The MD2 message digest algorithm as defined in RFC 1319. <P>
   *   </DIV>
   * </DIV>
   * 
   * <DIV style="margin-left: 40px;">
   *   "MD5" <BR>
   *   <DIV style="margin-left: 20px;">
   *     The MD5 message digest algorithm as defined in RFC 1321. <P>
   *   </DIV>
   * </DIV>
   * 
   * <DIV style="margin-left: 40px;">
   *   "SHA-1" <BR>
   *   <DIV style="margin-left: 20px;">
   *     The Secure Hash Algorithm, as defined in Secure Hash Standard, NIST FIPS 180-1. <P>
   *   </DIV>
   * </DIV>
   * 
   * <DIV style="margin-left: 40px;">
   *   "SHA-256", "SHA-384", "SHA-512" <BR>
   *   <DIV style="margin-left: 20px;">
   *     New hash algorithms defined by the draft Federal Information Processing 
   *     Standard 180-2, Secure Hash Standard (SHS). <P>
   *   </DIV>
   * </DIV>
   * 
   * @param algorithm [<B>in</B>]
   *   The digest algorithm.
   * 
   * @param dir [<B>in</B>]
   *   The root production directory.
   */ 
  public
  CheckSum
  (
   String algorithm, 
   File dir
  ) 
  {
    init(algorithm, dir);
  }

  /** 
   * Construct using the default "MD5" digest algorithm and the default production and 
   * checksum directories. 
   */
  public
  CheckSum() 
  {
    init("MD5", PackageInfo.sProdDir);
  }
  

  /*-- CONTRUCTION HELPERS -----------------------------------------------------------------*/

  private void 
  init
  (
   String algorithm,
   File dir
  ) 
  {
    try {
      pDigest = MessageDigest.getInstance(algorithm);
    }
    catch (NoSuchAlgorithmException ex) {
      throw new IllegalArgumentException
	("Unknown digest algorithm (" + algorithm + ")!");
    }

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
   * @param path [<B>in</B>]
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
  

  /**
   * Generate an up-to-date checksum file for the given node file path. <P> 
   *
   * No action will be taken if the source file is missing, if the source file is not a 
   * regular file or if there already exists a checksum file which is newer than 
   * the given source file. <P> 
   * 
   * If the source file is larger than <CODE>size</CODE> bytes, it is mapped directly 
   * into a region of memory to increase I/O performance. Memory-mapping seems to be 
   * vastly more efficient (more than 100 times faster) for large (>1 MB) files, but 
   * slightly less efficient (2-3 times slower) for small (<512 bytes) files.  A good 
   * number for the <CODE>size</CODE> argument is system dependent, but less than (1 Kb) 
   * seems reasonable.
   *
   * @param path [<B>in</B>]
   *   The fully resolved node file path.
   * 
   * @param size [<B>in</B>]
   *   The maximum non-memory-mapped source file size.
   * 
   * @throws PipelineException
   *   If unable to generate the checksum file.
   */ 
  public void
  refresh
  (
   File path, 
   long size
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
    if(sfile.isFile() && (sfile.lastModified() > file.lastModified())) 
      return;

    Logs.sum.finer("Rebuilding checksum for: " + file);

    /* verify (and possibly create) checksum directory */ 
    File dir = sfile.getParentFile();
    if(dir.exists()) {
      if(!dir.isDirectory()) 
	throw new PipelineException
	  ("The checksum directory (" + dir + ") exists but is not a directory!");
    }
    else {
      try {
	if(!dir.mkdirs()) {
	  throw new PipelineException
	    ("Unable to create the checksum directory (" + dir + ")!");
	}
      }
      catch (SecurityException ex) {
	throw new PipelineException
	  ("Unable to create the checksum directory (" + dir + ")!");
      }
    }

    /* generate the checksum */ 
    try {
      FileInputStream in = new FileInputStream(file);

      /* memory-map the source file */  
      if(file.length() > size) {
	Logs.sum.finest("Memory-mapping the checksum file.");

	FileChannel chan = in.getChannel();
	
	try {
	  MappedByteBuffer buf = chan.map(FileChannel.MapMode.READ_ONLY, 0, chan.size());
	  pDigest.reset();
	  pDigest.update(buf);
	}
	catch(Exception ex) {
	  throw new PipelineException
	    ("Unable to memory-map the source file (" + file + ")!");
	}
	finally {
	  chan.close();
	}
      }

      /* read the source file byte-by-byte */ 
      else {
	Logs.sum.finest("Reading the checksum file.");

	DigestInputStream din = new DigestInputStream(in, pDigest);
	try {
	  while(din.read() != -1);
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the source file (" + file + ")!");
	}
	finally {
	  din.close();
	}
      }
    }
    catch(FileNotFoundException ex) {
      throw new PipelineException
	("The source file (" + file + ") did not exist!");
    }
    catch(SecurityException ex) {
      throw new PipelineException
	("No permission to read the source file (" + file + ")!");
    }   
    catch (IOException ex) {   
      assert(false);
    } 

    /* write the checksum to file */ 
    try {
      Logs.sum.finest("Writing the checksum file: " + sfile);

      FileOutputStream out = new FileOutputStream(sfile);	
      try {
	out.write(pDigest.digest());
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
      assert(false);
    }

    Logs.flush();
  }

  /**
   * Generate an up-to-date checksum file for the given node file path. <P> 
   * 
   * Always memory-maps the source file. 
   * 
   * @param path [<B>in</B>]
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
    refresh(path, 0);
  }

  /**
   * Indirectly compare the two files specified by the given node file paths by comparing 
   * their associated checksum files. <P> 
   * 
   * The checksum files associated with the given node file paths are assumed to up-to-date 
   * by a previous call to {@link #refresh refresh}. 
   * 
   * @param pathA [<B>in</B>]
   *   The first fully resolved node file path.
   * 
   * @param pathB [<B>in</B>]
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
   File pathA,   
   File pathB   
  ) 
    throws PipelineException
  {
    Logs.sum.fine("Comparing (checksums of): " + pathA + " " + pathB);

    /* make sure they are actually distint */ 
    if(pathA.compareTo(pathB) == 0) 
      throw new PipelineException
	("Attempted to compare the node path (" + pathA + ") with itself!");

    /* checksum file paths */ 
    File sfileA = checkSumFile(pathA);  
    File sfileB = checkSumFile(pathB);  

    /* checksums */ 
    int size = pDigest.getDigestLength();
    byte[] sumA = new byte[size];
    byte[] sumB = new byte[size];
    {
      /* read the first checksum file */ 
      try {
	FileInputStream in = new FileInputStream(sfileA);	
	try {
	  in.read(sumA);
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the checksum file (" + sfileA + ")!");
	}
	finally {
	  in.close();
	}
      } 
      catch(FileNotFoundException ex) {
	throw new PipelineException
	  ("The checksum file (" + sfileA + ") did not exist!");
      }
      catch(SecurityException ex) {
	throw new PipelineException
	("No permission to read the checksum file (" + sfileA + ")!");
      }   
      catch (IOException ex) {   
	assert(false);
      } 

      /* read the second checksum file */ 
      try {
	FileInputStream in = new FileInputStream(sfileB);	
	try {
	  in.read(sumB);
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the checksum file (" + sfileB + ")!");
	}
	finally {
	  in.close();
	}
      } 
      catch(FileNotFoundException ex) {
	throw new PipelineException
	  ("The checksum file (" + sfileB + ") did not exist!");
      }
      catch(SecurityException ex) {
	throw new PipelineException
	("No permission to read the checksum file (" + sfileB + ")!");
      }   
      catch (IOException ex) {   
	assert(false);
      }       
    }
      
    /* compare checksums */ 
    return Arrays.equals(sumA, sumB);
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The message digest algorithm. 
   */ 
  private MessageDigest pDigest;   

  /**
   * The root production directory.
   */
  private File  pProdDir; 

  /**
   * The root checksum directory.
   */
  private File  pCheckSumDir; 
}



