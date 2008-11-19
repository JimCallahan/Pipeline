// $Id: FrameRangeParam.java,v 1.1 2008/11/19 04:34:47 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   F R A M E   R A N G E   P A R A M                                                      */
/*------------------------------------------------------------------------------------------*/

public abstract 
class FrameRangeParam<E>
  extends ComplexParam<E>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class when
   * encountered during the reading of GLUE format files and should not be called from user
   * code.
   */
  public 
  FrameRangeParam() 
  {
    super();
  }
  
  
  /**
   * Construct a parameter with the given name and description
   *
   * @param name
   *   The short name of the parameter.
   * 
   * @param desc
   *   A short description used in tooltips.
   */
  public
  FrameRangeParam
  (
   String name, 
   String desc
  ) 
  {
    this(name, desc, new FrameRange(1,2,1));
  }
  
  /**
   * Construct a parameter with the given name, description, and default value
   *
   * @param name
   *   The short name of the parameter.
   * 
   * @param desc
   *   A short description used in tooltips.
   *   
   * @param value
   *   The default value or <code>null</code> to have the parameter set its value to
   *   1-2x1
   */
  public
  FrameRangeParam
  (
   String name, 
   String desc,
   FrameRange value
  ) 
  {
    super(name, desc);
    
    if (value == null)
      value = new FrameRange(1,2,1);

    ArrayList<String> layout = new ArrayList<String>();
    {
      E param = createIntegerParam(aStart, "The start frame of the range", value.getStart());
      addParam(param);
      layout.add(aStart);
    }
    {
      E param = createIntegerParam(aEnd, "The end frame of the range", value.getEnd());
      addParam(param);
      layout.add(aEnd);
    }
    {
      E param = createIntegerParam(aBy, "The step of the range", value.getBy());
      addParam(param);
      layout.add(aBy);
    }
    setLayout(layout);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected boolean 
  needsUpdating()
  {
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the frame range value.
   */
  public FrameRange
  getFrameRangeValue()
  {
    Integer start = (Integer) getValue(aStart);
    Integer end = (Integer) getValue(aEnd);
    Integer by = (Integer) getValue(aBy);
    
    if (start == null || end == null || by == null)
      throw new IllegalArgumentException
        ("(" + start + "-" + end + "x" + by + ") is not a valid Frame Range");
    
    FrameRange range = new FrameRange(start, end, by);
    return range;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected abstract E
  createIntegerParam
  (
    String name, 
    String desc, 
    Integer value 
  );
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8567425539078503314L;

  public static final String aStart = "Start";
  public static final String aEnd = "End";
  public static final String aBy = "By";
  

}
