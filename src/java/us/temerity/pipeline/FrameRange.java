// $Id: FrameRange.java,v 1.12 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;
 
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F R A M E   R A N G E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of frame numbers for the sequence of files associated with a Pipeline node. <P>
 * 
 * The <CODE>FrameRange</CODE> class can represent both single and multiple file 
 * sequences. All frame numbers must be non-negative. <P> 
 * 
 * This class is used by the {@link FileSeq FileSeq} class in combination with the 
 * {@link FilePattern FilePattern} class to represent the file sequences associated with 
 * nodes.
 * 
 * @see FilePattern
 * @see FileSeq
 */
public
class FrameRange
  implements Cloneable, Glueable, Serializable
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
  FrameRange()
  {
    pStart = 0;
    pEnd   = 0;
    pBy    = 1;

    pNumFrames = 1;
  }

  /**
   * Construct for a single frame. 
   * 
   * @param single  
   *   The sole frame number of the single frame file sequence. 
   * 
   * @throws IllegalArgumentException
   *   If the frame number argument <CODE>single</CODE> is negative.
   */ 
  public 
  FrameRange
  (
   int single
  ) 
  {
    if(single < 0)
      throw new IllegalArgumentException
	("The frame number (" + single + ") cannot be negative!");

    pStart = single;
    pEnd   = single;
    pBy    = 1;

    pNumFrames = 1;
  }

  /**
   * Construct for a sequence of frames. 
   * 
   * @param start  
   *   The first frame number in the file sequence.
   * 
   * @param end  
   *   The last frame number in the file sequence.
   * 
   * @param by  
   *   The frame step increment. 
   * 
   * @throws IllegalArgumentException
   *   If the <CODE>start</CODE> or <CODE>end</CODE> frame are negative.  If the frame 
   *   increment <CODE>by</CODE> is not positive.  If the <CODE>start</CODE> frame is 
   *   not greater-than the <CODE>end</CODE> frame.
   */ 
  public 
  FrameRange
  (
   int start,  
   int end,    
   int by      
  ) 
  {
    if(start < 0)
      throw new IllegalArgumentException
	("The start frame (" + start + ") cannot be negative!");

    if(start > end) 
      throw new IllegalArgumentException
	("The start frame (" + start + ") cannot be greater-than " + 
	 "the end frame (" + end + ")!");

    if(by <= 0) 
      throw new IllegalArgumentException
	("The frame increment (" + by + ") must be positive!");

    if(((end - start) % by) != 0) 
      throw new IllegalArgumentException
	("The frame range (" + (end-start) + ") was not evenly divisible by the frame " +
	 "increment (" + by + ")!");      

    pStart = start;
    pEnd   = end;
    pBy    = by;

    pNumFrames = ((end - start) / by) + 1;
  }


  /**
   * Construct the smallest possible frame range which includes all of the given frames. <P>
   * 
   * The constructed frame range will have a frame step increment which is the greatest
   * common multiple of the frame steps between all given frames.  This may cause the 
   * frame range to include frames additional frames in order to meet this requirement. <P> 
   * 
   * The frame range will always be bounded by the lowest and highest of the given frames.
   * 
   * @param frames
   *   A set of frame numbers.
   */ 
  public 
  FrameRange
  ( 
   TreeSet<Integer> frames
  ) 
  {
    if(frames.isEmpty()) 
      throw new IllegalArgumentException
	("At least one frame must be supplied!");
    
    pStart = frames.first();
    pEnd   = frames.last();
    pBy    = 1;

    switch(frames.size()) {
    case 1:
      break;

    case 2:
      pBy = frames.last() - frames.first();
      break;

    default:
      {
	TreeSet<Integer> incs = new TreeSet<Integer>();
	Integer prev = null;
	for(Integer frame : frames) {
	  if(prev != null) 
	    incs.add(frame - prev);
	  prev = frame;
	}
      
	boolean done = false;
	int wk = 1; 
	while(!done) {
	  boolean multipleAll = true;
	  for(Integer inc : incs) {
	    if(wk > inc) {
	      done = true;
	      multipleAll = false;
	      break;		
	    }
	    
	    if((inc % wk) != 0)
	      multipleAll = false;
	  }
	  
	  if(multipleAll)
	    pBy = wk;
	  
	  wk++;
	}
      }
    }
      
    pNumFrames = ((pStart - pEnd) / pBy) + 1;
  }
  
  /**
   * Copy constructor. 
   *
   * @param range  
   *    The <CODE>FrameRange</CODE> to copy. 
   */ 
  public 
  FrameRange
  (
   FrameRange range
  ) 
  {
    pStart = range.getStart();
    pEnd   = range.getEnd();
    pBy    = range.getBy();

    pNumFrames = range.numFrames();
  }
  

   
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does this <CODE>FraneRange</CODE> contain only a single frame? 
   */
  public boolean
  isSingle() 
  {
    return (pStart == pEnd);
  }

  /**
   * Is the given frame number valid? In other words, is the given frame number a member 
   * of the set of frames for this <CODE>FrameRange</CODE>.
   * 
   * @param frame 
   *   The frame number to test for validity.
   */
  public boolean
  isValid
  (
   int frame  
  ) 
  {
    if(isSingle())
      return (frame == pStart);

    if((frame < pStart) || (frame > pEnd))
      return false;

    if(((frame - pStart) % pBy) != 0) 
      return false;

    return true;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the first frame number in the file sequence.
   */
  public int
  getStart() 
  {
    return pStart;
  }

  /**
   * Get the last frame number in the file sequence.
   */
  public int
  getEnd() 
  {
    return pEnd;
  }

  /**
   * Get the frame step increment. 
   */
  public int
  getBy()
  {
    return pBy;
  }

  /**
   * The total number of frames in the file sequence.
   */ 
  public int
  numFrames()
  {
    return pNumFrames;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   F R A M E   N U M B E R I N G                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the an array of valid frame numbers.
   */
  public int[]
  getFrameNumbers() 
  {
    int frames[] = new int[pNumFrames];
    int frame, wk;
    for(wk=0, frame=pStart; wk<pNumFrames; wk++, frame+=pBy) 
      frames[wk] = frame;

    return frames;
  }

  /**
   * Convert a frame index into a frame number.
   * 
   * @param idx 
   *   The frame index to convert.  Valid frame indices are in the range: 
   *   [0,{@link #numFrames() numFrames()}).
   * 
   * @return 
   *   The frame number corresponding to the given frame index.
   * 
   * @throws IllegalArgumentException
   *   If the given frame index is invalid.
   */ 
  public int 
  indexToFrame
  (
   int idx  
  )
  {
    if((idx < 0) || (idx >= pNumFrames)) 
      throw new IllegalArgumentException
	("The given frame index (" + idx + ") was not valid for the range: " + 
	 "[0," + pNumFrames + ")."); 
      
    return (pStart + pBy*idx);
  }
  
  /**
   * Convert a frame number into a frame index.
   * 
   * @param frame 
   *   The frame number to convert.  
   * 
   * @return 
   *   The frame index corresponding to the given frame number.
   *
   * @throws IllegalArgumentException
   *   If the given frame number is invalid.
   */ 
  public int 
  frameToIndex
  (
   int frame  /* IN: frame number */ 
  ) 
  {
    if(!isValid(frame))
      throw new IllegalArgumentException
	("The given frame (" + frame + ") was not valid for the range: " + this + "."); 
    
    return ((frame - pStart) / pBy);
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
    if((obj != null) && (obj instanceof FrameRange)) {
      FrameRange range = (FrameRange) obj;
    
      return ((pStart == range.pStart) && 
	      (pEnd == range.pEnd) && 
	      (pBy == range.pBy));
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
    if(isSingle())
      return String.valueOf(pStart);
    else
      return (pStart + "-" + pEnd + "x" + pBy);
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
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException ex) {
      throw new IllegalStateException();     
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
    encoder.encode("Start", pStart);
    
    if(pStart != pEnd) {
      encoder.encode("End", pEnd);
      encoder.encode("By",pBy);
    }
  }
    
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Integer start = (Integer) decoder.decode("Start"); 
    if(start == null) 
      throw new GlueException("The \"Start\" frame was missing!");
    if(start < 0)
      throw new GlueException("The \"Start\" frame (" + start + ") cannot be negative!");
    pStart = start;

    Integer end = (Integer) decoder.decode("End");   
    if(end != null) {
      Integer by  = (Integer) decoder.decode("By");    
      if(by == null) 
	throw new GlueException("Found an \"End\" frame without a \"By\" frame increment!");

      if(start > end) 
	throw new GlueException
	  ("The \"Start\" frame (" + start + ") cannot be greater-than " + 
	   "the \"End\" frame (" + end + ")!");

      if(by <= 0) 
        throw new GlueException
	  ("The \"By\" frame increment (" + by + ") must be positive!");

      pEnd = end;
      pBy  = by;

      pNumFrames = (int) Math.floor(((double) (pEnd - pStart)) / ((double) pBy)) + 1;
    }
    else {
      pEnd = pStart;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7113054792066134471L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The first frame number in the file sequence.  If this is a single frame file sequence,
   * then this is the single frame number.
   */
  private int pStart; 

  /** 
   * The last frame number in the file sequence. 
   */  
  private int pEnd;  

  /** 
   * The frame step increment. 
   */   
  private int pBy;      

  /** 
   * The number of frames in the file sequence.
   */ 
  private int pNumFrames;  

}



