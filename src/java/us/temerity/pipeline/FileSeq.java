// $Id: FileSeq.java,v 1.11 2004/05/21 18:07:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S E Q                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A sequence of one or more files associated with a Pipeline node. <P> 
 * 
 * The file sequence is composed of {@link FilePattern FilePattern} and 
 * {@link FrameRange FrameRange} components which together define a unique set of files.
 * 
 * @see FilePattern
 * @see FrameRange
 */
public
class FileSeq
  implements Comparable, Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  FileSeq() 
  {}

  
  /**
   * Construct a <CODE>FileSeq</CODE> which contains a single frame without a frame number.
   * 
   * @param prefix  
   *   The required filename prefix component. This argument cannot be <CODE>null</CODE>.
   * 
   * @param suffix  
   *   The filename extension (or suffix) component.  If <CODE>null</CODE> is passed for this 
   *   argument, then the filename will have no suffix component.
   */
  public 
  FileSeq
  (
   String prefix,       
   String suffix    
  ) 
  {
    assert(prefix != null);
    if((suffix != null) && (suffix.length() >= 0))
      pFilePattern = new FilePattern(prefix, -1, suffix);
    else 
      pFilePattern = new FilePattern(prefix);

    pFrameRange = null;

    buildCache();
  }

  /**
   * Construct a general <CODE>FileSeq</CODE> from <CODE>FilePattern</CODE> and 
   * <CODE>FrameRange</CODE> components. 
   * 
   * @param pattern 
   *   The filename pattern.  This argument cannot be <CODE>null</CODE>.
   * 
   * @param range 
   *   The frame range of the file sequence.  If this is a single frame file sequence 
   *   without frame numbers, <CODE>null</CODE> should be passed for this argument.
   * 
   * @throws IllegalArgumentException
   *   If the <CODE>pattern</CODE> has a frame number component and the <CODE>range</CODE>
   *   is <CODE>null</CODE>.  If the <CODE>pattern</CODE> has NO frame number component and 
   *   the <CODE>range</CODE> is NOT <CODE>null</CODE>.
   */ 
  public 
  FileSeq
  (
   FilePattern pattern,  
   FrameRange range      
  ) 
  {
    assert(pattern != null);      
    pFilePattern = new FilePattern(pattern);

    if(range != null) {
      if(!pFilePattern.hasFrameNumbers())
	throw new IllegalArgumentException
	  ("The FilePattern has NO frame number component, but FrameRange was NOT (null)!");

      pFrameRange = new FrameRange(range);
    }      
    else if(pFilePattern.hasFrameNumbers()) 
      throw new IllegalArgumentException
	("The FilePattern HAS a frame number component, but FrameRange was (null)!");

    buildCache();
  }


  /** 
   * Construct a <CODE>FileSeq</CODE> which contains only a single file extracted from 
   * the given file sequence. 
   * 
   * @param fseq  
   *   The file sequence which contains the file to be extracted.
   * 
   * @param idx   
   *   The frame index of the file to be extracted.
   */ 
  public
  FileSeq
  (
   FileSeq fseq, 
   int idx       
  ) 
  {
    pFilePattern = fseq.getFilePattern();
    if(fseq.getFrameRange() != null) 
      pFrameRange = new FrameRange(fseq.getFrameRange().indexToFrame(idx));
    else 
      pFrameRange = null;

    buildCache();
  }

  
  /**
   * Copy constructor. 
   */ 
  public
  FileSeq
  (
   FileSeq fseq   
  ) 
  {
    pFilePattern = fseq.getFilePattern();
    pFrameRange  = fseq.getFrameRange();

    pStringRep = fseq.toString();
    pHashCode  = fseq.hashCode();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does this <CODE>FileSeq</CODE> have a frame number component? 
   */ 
  public boolean 
  hasFrameNumbers() 
  {
    return (pFrameRange != null);
  }
  
  /**
   * Is this a single frame sequence? 
   */ 
  public boolean 
  isSingle()
  {
    return ((pFrameRange == null) || (pFrameRange.isSingle()));
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the filename pattern. 
   */ 
  public FilePattern
  getFilePattern()
  {
    assert(pFilePattern != null);
    return new FilePattern(pFilePattern);
  }

  /** 
   * Get the file sequence frame range. 
   */ 
  public FrameRange
  getFrameRange() 
  {
    if(pFrameRange != null) 
      return new FrameRange(pFrameRange);
    return null;
  }

  /** 
   * The total number of frames in the file sequence.
   */ 
  public int
  numFrames() 
  {
    return (pFrameRange == null) ? 1 : pFrameRange.numFrames();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I L E N A M E   G E N E R A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generated a literal filename for the given frame number. <P>
   * 
   * @param idx  
   *   The frame index of the file.  Valid frame indices are in the range: 
   *   [0,{@link #numFrames() numFrames()}).
   */ 
  public File 
  getFile
  (
   int idx  
  )  
  {
    assert(pFilePattern != null);
    if(pFrameRange != null) 
      return pFilePattern.getFile(pFrameRange.indexToFrame(idx));
    else 
      return pFilePattern.getFile();
  }
 

  /**
   * Generate a list of all of the files in the file sequence. 
   */ 
  public ArrayList<File> 
  getFiles() 
  {
    ArrayList<File> files = new ArrayList<File>();
    if(pFrameRange != null) {
      int frames[] = pFrameRange.getFrameNumbers();
      int wk;
      for(wk=0; wk<frames.length; wk++)
	files.add(pFilePattern.getFile(frames[wk]));
    }
    else {
      files.add(pFilePattern.getFile());
    }

    return files;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I L E S Y T E M                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Delete any existing files from this sequence under the given directory. 
   * 
   * @param dir  
   *   The parent directory of the file sequence.
   * 
   * @throws PipelineException
   *   If unable to delete the files.
   */ 
  public void 
  delete
  (
   File dir   
  )
    throws PipelineException
  {
    File full = null;
    try {
      for(File file : getFiles()) {
	full = new File(dir, file.getPath());
	if(full.isFile()) {
	  Logs.sub.finest("Deleting file: " + full);
	  full.delete();
	}
      }
    }
    catch (SecurityException ex) {
      throw new PipelineException
	("Unable to delete file (" + full + ") from file sequence " + this + 
	 " due to the Java Security Manager!");
    }
  }


  /** 
   * Change the permissions to read-only for any existing files from the sequence 
   * in the given directory. 
   * 
   * @param dir  
   *   The parent directory of the file sequence.
   * 
   * @throws PipelineException
   *   If unable to change the permission of the files.
   */ 
  public void 
  setReadOnly
  (
   File dir   /* IN: parent directory */ 
  )
    throws PipelineException
  {
    File full = null;
    try {
      for(File file : getFiles()) {
	full = new File(dir, file.getPath());
	if(full.isFile()) {
	  Logs.sub.finest("Making file read-only: " + full);
	  full.setReadOnly();
	}
      }
    }
    catch (SecurityException ex) {
      throw new PipelineException
	("Unable to change the permissions to read-only for file (" + full + ") from " + 
	 "file sequence " + this + " due to the Java Security Manager!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof FileSeq)) {
      FileSeq fseq = (FileSeq) obj;
      return ((pHashCode == fseq.pHashCode) && 
	      pStringRep.equals(fseq.pStringRep));
    }
    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    assert(pStringRep != null);
    return pHashCode;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    assert(pStringRep != null);
    return pStringRep;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof FileSeq))
      throw new IllegalArgumentException("The object to compare was NOT a FileSeq!");

    return compareTo((FileSeq) obj);
  }


  /**
   * Compares this <CODE>FileSeq</CODE> with the given <CODE>FileSeq</CODE> for order.
   * 
   * @param fseq 
   *   The <CODE>FileSeq</CODE> to be compared.
   */
  public int
  compareTo
  (
   FileSeq fseq
  )
  {
    return pStringRep.compareTo(fseq.pStringRep);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return new FileSeq(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("FilePattern", pFilePattern);
    if(pFrameRange != null) 
      encoder.encode("FrameRange",  pFrameRange);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    FilePattern pat = (FilePattern) decoder.decode("FilePattern");
    if(pat == null) 
      throw new GlueException("The \"FilePattern\" was missing!");
    pFilePattern = pat;

    pFrameRange = (FrameRange) decoder.decode("FrameRange");

    buildCache();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the cached string representation and hash code for the file sequence.
   */
  private void
  buildCache() 
  {
    if(pFrameRange != null) {
      if(pFrameRange.isSingle())
	pStringRep = pFilePattern.getFile(pFrameRange.getStart()).getPath();
      else 
	pStringRep = (pFilePattern.toString() + ", " + pFrameRange.toString());
    }
    else {
      pStringRep = pFilePattern.toString();
    }

    pHashCode = pStringRep.hashCode();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3252483522938496643L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The filename pattern. 
   */
  private FilePattern pFilePattern;    

  /**
   * The file sequence frame range.  If <CODE>null</CODE>, then this is a single frame 
   * file sequence.
   */ 
  private FrameRange pFrameRange;  


  /** 
   * The cached string representation.
   */
  private String  pStringRep;
 
  /** 
   * The cached hash code.
   */
  private int  pHashCode;
} 


