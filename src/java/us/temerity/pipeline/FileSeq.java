// $Id: FileSeq.java,v 1.4 2004/02/25 02:59:28 jim Exp $

package us.temerity.pipeline;

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
  implements Glueable
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
   * @param prefix  [<B>in</B>]
   *   The required filename prefix component. This argument cannot be <CODE>null</CODE>.
   * 
   * @param suffix  [<B>in</B>]
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
      pPattern = new FilePattern(prefix, -1, suffix);
    else 
      pPattern = new FilePattern(prefix);

    pFrameRange = null;
  }

  /**
   * Construct a general <CODE>FileSeq</CODE> from <CODE>FilePattern</CODE> and 
   * <CODE>FrameRange</CODE> components. 
   * 
   * @param pattern [<B>in</B>]
   *   The filename pattern.  This argument cannot be <CODE>null</CODE>.
   * 
   * @param range [<B>in</B>]
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
    pPattern = new FilePattern(pattern);

    if(range != null) {
      if(!pPattern.hasFrameNumbers())
	throw new IllegalArgumentException
	  ("The FilePattern has NO frame number component, but FrameRange was NOT (null)!");

      pFrameRange = new FrameRange(range);
    }      
    else if(pPattern.hasFrameNumbers()) 
      throw new IllegalArgumentException
	("The FilePattern HAS a frame number component, but FrameRange was (null)!");
  }


  /** 
   * Construct a <CODE>FileSeq</CODE> which contains only a single file extracted from 
   * the given file sequence. 
   * 
   * @param fseq  [<B>in</B>]
   *   The file sequence which contains the file to be extracted.
   * 
   * @param idx   [<B>in</B>]
   *   The frame index of the file to be extracted.
   */ 
  public
  FileSeq
  (
   FileSeq fseq, 
   int idx       
  ) 
  {
    pPattern = fseq.getFilePattern();
    if(fseq.getFrameRange() != null) 
      pFrameRange = new FrameRange(fseq.getFrameRange().indexToFrame(idx));
    else 
      pFrameRange = null;
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
    pPattern    = fseq.getFilePattern();
    pFrameRange = fseq.getFrameRange();
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
    assert(pPattern != null);
    return new FilePattern(pPattern);
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
   * @param idx  [<B>in</B>]
   *   The frame index of the file.  Valid frame indices are in the range: 
   *   [0,{@link #numFrames() numFrames()}).
   */ 
  public File 
  getFile
  (
   int idx  
  )  
  {
    assert(pPattern != null);
    if(pFrameRange != null) 
      return pPattern.getFile(pFrameRange.indexToFrame(idx));
    else 
      return pPattern.getFile();
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
	files.add(pPattern.getFile(frames[wk]));
    }
    else {
      files.add(pPattern.getFile());
    }

    return files;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I L E S Y T E M                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Delete any existing files from this sequence under the given directory. 
   * 
   * @param dir  [<B>in</B>]
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
   * @param dir  [<B>in</B>]
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

      if(!pPattern.equals(fseq.getFilePattern()))
	return false;

      if(((pFrameRange == null) && (fseq.getFrameRange() == null)) || 
	 ((pFrameRange != null) && (fseq.getFrameRange() != null) &&
	  pFrameRange.equals(fseq.getFrameRange())))
	return true;
    }
    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    return toString().hashCode();
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(pFrameRange != null) {
      if(pFrameRange.isSingle())
	return pPattern.getFile(pFrameRange.getStart()).getPath();
      else 
	return (pPattern.toString() + ", " + pFrameRange.toString());
    }
    else {
      return pPattern.toString();
    }
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
    encoder.encode("FilePattern", pPattern);
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
    pPattern    = (FilePattern) decoder.decode("FilePattern");
    pFrameRange = (FrameRange)  decoder.decode("FrameRange");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The filename pattern. 
   */
  private FilePattern pPattern;    

  /**
   * The file sequence frame range.  If <CODE>null</CODE>, then this is a single frame 
   * file sequence.
   */ 
  private FrameRange pFrameRange;  

}


