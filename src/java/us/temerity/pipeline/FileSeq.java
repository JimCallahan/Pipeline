// $Id: FileSeq.java,v 1.21 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
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
    if(prefix == null)
      throw new IllegalStateException(); 

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
    if(pattern == null)
      throw new IllegalStateException(); 
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
   * Construct a <CODE>FileSeq</CODE> which contains a subset of the frames from the 
   * given file sequence.
   * 
   * @param fseq  
   *   The original file sequence.
   * 
   * @param startIdx   
   *   The frame index of the first file.
   * 
   * @param endIdx
   *   The frame index of the last file.
   */ 
  public
  FileSeq
  (
   FileSeq fseq, 
   int startIdx, 
   int endIdx
  ) 
  {
    pFilePattern = fseq.getFilePattern();

    if(endIdx < startIdx) 
      throw new IllegalArgumentException
	("The end index (" + endIdx + ") cannot be less than the start index " + 
	 "(" + startIdx + ")!");

    FrameRange range = fseq.getFrameRange();
    if((range == null) || (range.isSingle())) {
      if((startIdx != 0) || (endIdx != 0)) 
	throw new IllegalArgumentException
	  ("Illegal frame indices [" + startIdx + "," + endIdx + "] specified when " +
	   "the file sequence contains only a single frame!");

      pFrameRange = range; 
    }
    else {
      if(startIdx < 0) 
	throw new IllegalArgumentException
	  ("The start frame index (" + startIdx + ") cannot be negative!");
      
      if(endIdx >= range.numFrames()) 
	throw new IllegalArgumentException
	  ("The end frame index (" + endIdx + ") was greater than the largest frame " + 
	   "index (" + (range.numFrames()-1) + ") of the file sequence!"); 

      pFrameRange = new FrameRange(range.indexToFrame(startIdx), 
				   range.indexToFrame(endIdx), 
				   range.getBy());
    }
    
    buildCache();
  }

  /** 
   * Construct a <CODE>FileSeq</CODE> by prepend a path to an existing file sequence.
   * 
   * @param path
   *   The path to prepend.
   * 
   * @param fseq  
   *   The file sequence to prepend.
   */ 
  public
  FileSeq
  (
   String path, 
   FileSeq fseq
  ) 
  {
    pFilePattern = new FilePattern(path, fseq.getFilePattern());
    pFrameRange  = fseq.getFrameRange();

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
    if(pFilePattern == null)
      throw new IllegalStateException(); 
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
    if(pFilePattern == null)
      throw new IllegalStateException(); 
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

  /**
   * Generated an abstract pathname for the given frame number. <P>
   * 
   * @param idx  
   *   The frame index of the file.  Valid frame indices are in the range: 
   *   [0,{@link #numFrames() numFrames()}).
   */ 
  public Path 
  getPath
  (
   int idx  
  )  
  {
    if(pFilePattern == null)
      throw new IllegalStateException(); 
    if(pFrameRange != null) 
      return pFilePattern.getPath(pFrameRange.indexToFrame(idx));
    else 
      return pFilePattern.getPath();
  }

  /**
   * Generate a list of abstract pathnames of all of the files in the file sequence. 
   */ 
  public ArrayList<Path> 
  getPaths() 
  {
    ArrayList<Path> files = new ArrayList<Path>();
    if(pFrameRange != null) {
      int frames[] = pFrameRange.getFrameNumbers();
      int wk;
      for(wk=0; wk<frames.length; wk++)
	files.add(pFilePattern.getPath(frames[wk]));
    }
    else {
      files.add(pFilePattern.getPath());
    }

    return files;
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
    if(pStringRep == null)
      throw new IllegalStateException(); 
    return pHashCode;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(pStringRep == null)
      throw new IllegalStateException(); 
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


  /**
   * Whether the given file sequence have an identical prefix and suffix as this file 
   * sequence. <P> 
   * 
   * The similarity of file sequences is independent of whether they have any frame numbers 
   * in common.  In other words, there is no requirement that they share any literal files.
   */ 
  public boolean 
  similarTo
  (
   FileSeq fseq
  )
  {
    return (pFilePattern.getPrefix().equals(fseq.pFilePattern.getPrefix()) && 
	    (((pFilePattern.getSuffix() == null) && 
	      (fseq.pFilePattern.getSuffix() == null)) ||
	     ((pFilePattern.getSuffix() != null) && 
	      pFilePattern.getSuffix().equals(fseq.pFilePattern.getSuffix()))));
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
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs the minimum set of file sequences which include all of the given files.<P> 
   * 
   * WARNING: The prefix of the returned file sequences will be seperated with an 
   * operating system dependent character.
   * 
   * @param files
   *   The files which define the file sequences created.
   * 
   * @param ignoreInvalid
   *   Whether to ignore files which do not conform to the file naming conventions.
   * 
   * @return 
   *   The generated file sequences.
   * 
   * @throws PipelineException 
   *   If invalid or conflicting files are given.
   */ 
  public static TreeSet<FileSeq>
  collate
  (
   Set<File> files, 
   boolean ignoreInvalid
  ) 
    throws PipelineException
  {
    TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>();

    /* sort the files in to file sequence fragments */ 
    TreeMap<String,Fragment> ufrags = new TreeMap<String,Fragment>();
    TreeMap<String,Fragment> pfrags = new TreeMap<String,Fragment>();
    for(File file : files) {
      try {
	String prefix = null;
	Integer frame = null;
	String suffix = null;

	String comps[] = file.getName().split("\\.", -1);
	switch(comps.length) {
	case 1:
	  prefix = comps[0];
	  break;

	case 2: 
	  prefix = comps[0];
	  try {
	    frame = new Integer(comps[1]);
	  }
	  catch(NumberFormatException ex) {
	    suffix = comps[1];
	  }
	  break;	    
	  
	case 3: 
	  prefix = comps[0];
	  try {
	    frame = new Integer(comps[1]);
	  }
	  catch(NumberFormatException ex) {
	    throw new PipelineException 
	      ("The frame number component (" + comps[1] + ") of file (" + file + ") " + 
	       "was not valid!");
	  }
	  suffix = comps[2];
	  break;

	default:
	  throw new PipelineException 
	    ("The file (" + file + ") did not conform to the (prefix[.frame][.suffix]) " + 
	     "file naming convention required for file sequences!");
	}
	
	if(frame != null) {
	  boolean isPadded = (comps[1].startsWith("0") && (comps[1].length() > 1));
	  int digits = comps[1].length();
	  String key = (prefix + "|" + suffix + "|" + digits);
	  
	  Fragment frag = null;
	  if(isPadded) {
	    frag = pfrags.get(key);
	    if(frag == null) {
	      frag = new Fragment(prefix, suffix, digits);
	      pfrags.put(key, frag);
	    }
	  }
	  else {
	    frag = ufrags.get(key);
	    if(frag == null) {
	      frag = new Fragment(prefix, suffix, digits);
	      ufrags.put(key, frag);
	    }		    
	  }
	  
	  frag.uFrames.add(frame);
	}
	else {
	  FileSeq fseq = new FileSeq(prefix, suffix);
	  fseqs.add(fseq);
	}
      }
      catch(PipelineException ex) {
	if(!ignoreInvalid) 
	  throw ex;	  
      }
    }

    /* merge any unpadded fragments with padded fragments which have the same 
       number of digits */ 
    for(String key : pfrags.keySet()) {
      Fragment pfrag = pfrags.get(key);
      Fragment ufrag = ufrags.get(key);
      
      if(ufrag != null) {
	pfrag.uFrames.addAll(ufrag.uFrames);
	ufrags.remove(key);
      }
    }

    /* merge unpadded fragments which have the same prefix|suffix */ 
    TreeMap<String,Fragment> mfrags = new TreeMap<String,Fragment>();
    for(Fragment ufrag : ufrags.values()) {
      String key = (ufrag.uPrefix + "|" + ufrag.uSuffix);
      Fragment mfrag = mfrags.get(key);
      if(mfrag == null) {
	mfrag = new Fragment(ufrag.uPrefix, ufrag.uSuffix, 0);
	mfrags.put(key, mfrag);
      }
      mfrag.uFrames.addAll(ufrag.uFrames);
    }

    /* build file sequences from the fragments */     
    for(Fragment frag : pfrags.values())  
      fseqs.add(frag.toFileSeq());

    for(Fragment frag : mfrags.values()) 
      fseqs.add(frag.toFileSeq());

    return fseqs;
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
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static
  class Fragment
  {
    public 
    Fragment
    (
     String prefix, 
     String suffix, 
     int digits
    ) 
    {
      uPrefix = prefix; 
      uSuffix = suffix;
      uDigits = digits; 

      uFrames = new TreeSet<Integer>();
    }

    public FileSeq
    toFileSeq() 
    {
      FilePattern fpat = new FilePattern(uPrefix, uDigits, uSuffix);

      FrameRange frange = null;
      {
	int startFrame = uFrames.first();
	int endFrame   = uFrames.last();
	if(endFrame == startFrame) {
	  frange = new FrameRange(startFrame);
	}
	else {
	  int minInc = endFrame - startFrame;
	  {
	    int prev = startFrame;
	    for(Integer frame : uFrames) {
	      if(frame != startFrame) 
		minInc = Math.min(minInc, frame - prev);
	      prev = frame;
	    }
	  }

	  int byFrame;
	  for(byFrame=minInc; byFrame>1; byFrame--) {
	    boolean done = true;
	    for(Integer frame : uFrames) {
	      if(frame != startFrame) {
		if(((frame - startFrame) % byFrame) != 0) {
		  done = false;
		  break;
		}
	      }
	    }
	    
	    if(done) 
	      break;
	  }

	  frange = new FrameRange(startFrame, endFrame, byFrame);
	}
      }
      
      return new FileSeq(fpat, frange);
    }


    public String  uPrefix; 
    public String  uSuffix; 
    public int     uDigits; 

    public TreeSet<Integer>  uFrames;
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


