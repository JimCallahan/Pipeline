// $Id: TemplateSecContextAnnotation.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateSecContextAnnotation.v2_4_10;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   S E C   C O N T E X T   A N N O T A T I O N                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation specifying the name of a context that is going to be applied to the secondary
 * sequences of a node.
 * <p>
 * The node that this is on will have a secondary sequence added from the template once for 
 * each list in the context.
 * <p>
 * The SeqName param can be used to specify a particular secondary sequence to apply the 
 * context to, rather than applying it to all the secondary sequences.  The value of the 
 * SeqName parameter should be results of calling toString() on the FilePattern of the 
 * secondary sequence or <code>null</code> if the context should be applied to all the 
 * sequences. 
 */
public 
class TemplateSecContextAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateSecContextAnnotation()
  {
    super("TemplateSecContext", new VersionID("2.4.10"), "Temerity", 
          "Annotation specifying the name of a context that is going to be applied to the " +
          "secondary sequences of a node.");
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aContextName,
         "The name of the context.",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aSeqName,
         "The name of the secondary sequence to apply this context to or null to apply it to " +
         "all secondary sequences.",
         null);
      addParam(param);
    }
    
    underDevelopment();
    
    addContext(AnnotationContext.PerVersion);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6726444554537430253L;

  public static final String aContextName = "ContextName";
  public static final String aSeqName     = "SeqName";
}
