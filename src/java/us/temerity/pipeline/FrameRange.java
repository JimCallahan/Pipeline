// $Id: FrameRange.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F R A M E   R A N G E                                                                  */
/*                                                                                          */
/*    Information about the range of frame numbers which make up a file sequence.           */
/*------------------------------------------------------------------------------------------*/

public
class FrameRange
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /* Default constructor. */ 
  public 
  FrameRange()
  {
    pStart = 0;
    pEnd   = 0;
    pBy    = 1;
  }

  /* Construct for a single frame. */ 
  public 
  FrameRange
  (
   int single  /* IN: frame number */ 
  ) 
  {
    pStart = single;
    pEnd   = single;
    pBy    = 1;
  }

  /* Construct for a sequence of frames. */ 
  public 
  FrameRange
  (
   int start,   /* IN: start frame */ 
   int end,     /* IN: start frame */ 
   int by       /* IN: frame increment */ 
  ) 
  {
    pStart = start;
    pEnd   = end;
    pBy    = by;
  }

  /* Copy constructor. */ 
  public 
  FrameRange
  (
   FrameRange range   /* IN: frame range to copy */ 
  ) 
  {
    assert(range != null);
    pStart = range.getStart();
    pEnd   = range.getEnd();
    pBy    = range.getBy();
  }
  

   
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /* is this a single frame? (as opposed to a sequence) */ 
  public boolean
  isSingle() 
  {
    return (pStart == pEnd);
  }

  /* is the given frame number valid? (within the range) */ 
  public boolean
  isValid
  (
   int frame   /* IN: frame number */ 
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

  /* start frame */
  public int
  getStart() 
  {
    return pStart;
  }

  /* end frame */
  public int
  getEnd() 
  {
    return pEnd;
  }

  /* frame increment */ 
  public int
  getBy()
  {
    return pBy;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   F R A M E   N U M B E R I N G                                                        */
  /*----------------------------------------------------------------------------------------*/

  /* total number of frames in range */ 
  public int
  getNumFrames()
  {
    return (int) Math.floor(((double) (pEnd - pStart)) / ((double) pBy)) + 1;
  }

  /* convert an index into a frame number */ 
  public int 
  indexToFrame
  (
   int idx    /* IN: frame index */ 
  )
  {
    assert (isValid(pStart + pBy*idx)) : ("Index: " + idx + "  Frame: " + (pStart + pBy*idx));
    return (pStart + pBy*idx);
  }
  
  /* Convert a frame number into an index. */ 
  public int 
  frameToIndex
  (
   int frame  /* IN: frame number */ 
  ) 
    throws IllegalArgumentException
  {
    if(!isValid(frame))
      throw new IllegalArgumentException(
	"The given frame (" + frame + ") was not valid for the range: " + range + "."); 
    
    return ((frame - pStart) / pBy);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /* are the frame ranges equivalent? */ 
  public boolean
  equals
  (
   Object obj
  )
  {
    FrameRange range = (FrameRange) obj;
    
    return ((range != null) && 
	    (pStart == range.pStart) && 
	    (pEnd == range.pEnd) && 
	    (pBy == range.pBy));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /* convert to a string representation */ 
  public String
  toString() 
  {
    if(isSingle())
      return String.valueOf(pStart);
    else
      return (String.valueOf(pStart) + "-" + 
	      String.valueOf(pEnd) + "x" + 
	      String.valueOf(pBy));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  writeGlue
  ( 
   GlueEncoder ge   /* IN: the current GLUE encoder */ 
  ) 
    throws GlueError
  {
    ge.writeEntity("Start", new Integer(pStart));
    ge.writeEntity("End",   new Integer(pEnd));
    ge.writeEntity("By",    new Integer(pBy));
  }
    
  public void 
  readGlue
  (
   GlueDecoder gd  /* IN: the current GLUE decoder */ 
  ) 
    throws GlueError
  {
    pStart = ((Integer) gd.readEntity("Start")).intValue();
    pEnd   = ((Integer) gd.readEntity("End")).intValue();
    pBy    = ((Integer) gd.readEntity("By")).intValue();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  protected int  pStart;   /* The start frame of the sequence. */ 
  protected int  pEnd;     /* The end frame of the sequence. */ 
  protected int  pBy;      /* The frame increment. */ 

}



