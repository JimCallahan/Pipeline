// $Id: ExternalSeqUtilityParam.java,v 1.1 2009/05/26 10:56:09 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   E X T E R N A L   S E Q   P A R A M                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A Utility parameter with a {@link FileSeqUtilityParam} value and a 
 * {@link IntegerUtilityParam} value. 
 */
public 
class ExternalSeqUtilityParam
  extends ComplexUtilityParam
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
  ExternalSeqUtilityParam() 
  {
    super();
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * <p>
   * This constructor does not allow the creation of a new working area.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param pathValue
   *   The path value (which can include a frame range).
   *   
   * @param frameStart
   *   The frame start for the sequence.
   */ 
  public
  ExternalSeqUtilityParam
  (
    String name,  
    String desc, 
    Path pathValue,
    Integer frameStart
  ) 
    throws PipelineException 
  {
    super(name, desc);

    {
      pExternalSeqParam = 
        new FileSeqUtilityParam(aExternalSeq, "The path of the external sequence", pathValue);
      addParam(pExternalSeqParam);
    }
    {
      pFrameStartParam = 
        new IntegerUtilityParam(aFrameStart, "The frame the external sequence will be read from", frameStart);
      addParam(pFrameStartParam);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aExternalSeq);
      layout.add(aFrameStart);
      setLayout(layout);
    }
  }

  public final Path
  getExternalSeq()
  {
    return pExternalSeqParam.getPathValue();
  }
  
  public final Integer
  getFrameStart()
  {
    return pFrameStartParam.getIntegerValue();
  }

  @Override
  protected boolean 
  needsUpdating()
  {
    return false;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6822143170355129287L;
  
  public static final String aExternalSeq = "ExternalSeq";
  public static final String aFrameStart  = "FrameStart";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private FileSeqUtilityParam pExternalSeqParam;
  private IntegerUtilityParam pFrameStartParam;
}
