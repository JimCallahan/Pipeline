package us.temerity.pipeline.plugin.TemplateManifestAnnotation.v2_4_27;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   M A N I F E S T   A N N O T A T I O N                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Annotation used to indicate a node in a template that will have a template manifest written 
 * into it.
 */
public 
class TemplateManifestAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateManifestAnnotation()
  {
    super("TemplateManifest", new VersionID("2.4.27"), "Temerity", 
          "Annotation used to indicate a node in a template that will have a template " +
          "manifest written into it.");
    
    {
      ArrayList<String> values = new ArrayList<String>(2); 
      Collections.addAll(values, aParam, aDesc);
      
      AnnotationParam param =
        new EnumAnnotationParam
        (aManifestType,
         "The type of manifest to write into this node.",
         aParam,
         values);
      addParam(param);
    }
    
    underDevelopment();
    
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5896603611965929831L;
  
  public static final String aManifestType = "ManifestType";
  public static final String aParam        = "Param";
  public static final String aDesc         = "Desc";
}
