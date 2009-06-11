// $Id: FileSeqUtilityParam.java,v 1.2 2009/06/11 05:14:06 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S E Q   U T I L I T Y    P A R A M                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A Utility parameter with a Path value that can include a file sequence component.
 */
public class FileSeqUtilityParam
  extends SimpleParam
  implements UtilityParam
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
  FileSeqUtilityParam() 
  {
    super();
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  FileSeqUtilityParam
  (
   String name,  
   String desc, 
   FileSeq value
  ) 
  {
    super(name, desc, value);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the {@link FileSeq} value of the parameter. 
   */ 
  public FileSeq
  getFileSeqValue() 
  {
    return ((FileSeq) getValue());
  }
  
  @Override
  public void 
  fromString
  (
    String value
  )
  {
    if (value == null)
      throw new IllegalArgumentException("Cannot set a Parameter value from a null string");
    FileSeq fseq = FileSeq.fromString(value);
    setValue(fseq);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value      
  )
    throws IllegalArgumentException 
  {
    if((value != null) && !(value instanceof FileSeq))
      throw new IllegalArgumentException
        ("The parameter (" + pName + ") only accepts (FileSeq) values!");
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8630949846667299441L;
}
