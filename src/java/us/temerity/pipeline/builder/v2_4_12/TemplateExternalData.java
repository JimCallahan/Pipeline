package us.temerity.pipeline.builder.v2_4_12;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   E X T E R N A L   D A T A                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Data representing an external file sequence used by a template. 
 */
public 
class TemplateExternalData
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * No-arg constructor for use with the {@link GlueDecoder}.
   * <p>
   * This constructor should not be used by user code.
   */
  public 
  TemplateExternalData()
  {}
  
  /**
   * Constructor
   * 
   * @param fileSeq
   *   The file sequence of the external sequence
   *
   * @param startFrame
   *   The frame number that the template will start using the external sequence.
   */
  public 
  TemplateExternalData
  (
    FileSeq fileSeq,
    Integer startFrame
  )
  {
    pFileSeq = fileSeq;
    pStartFrame = startFrame;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/


  /**
   * Get the FileSeq for the external sequence.
   */
  public final FileSeq 
  getFileSeq()
  {
    return pFileSeq;
  }

  /**
   * Get the start frame for the external sequence.
   */
  public final Integer 
  getStartFrame()
  {
    return pStartFrame;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  G L U A B L E                                                                         */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  fromGlue
  (
    GlueDecoder decoder
  )
    throws GlueException
  {
    pFileSeq = (FileSeq) decoder.decode(aFileSeq);
    pStartFrame = (Integer) decoder.decode(aStartFrame);
  }

  @Override
  public void 
  toGlue
  (
    GlueEncoder encoder
  )
    throws GlueException
  {
    encoder.encode(aFileSeq, pFileSeq);
    encoder.encode(aStartFrame, pStartFrame);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String aFileSeq    = "FileSeq";
  private static final String aStartFrame = "StartFrame";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private FileSeq pFileSeq;
  private Integer pStartFrame;
}
