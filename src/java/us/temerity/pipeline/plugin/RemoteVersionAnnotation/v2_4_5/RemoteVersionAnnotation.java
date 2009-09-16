// $Id: RemoteVersionAnnotation.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.RemoteVersionAnnotation.v2_4_5;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   R E M O T E   V E R S I O N   A N N O T A T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A annotation which records information about the original node version which was 
 * extracted from a remote site.
 */
public 
class RemoteVersionAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  RemoteVersionAnnotation()
  {
    super("RemoteVersion", new VersionID("2.4.5"), "Temerity", 
          "A annotation which records information about the original node version which " + 
          "was extracted from a remote site.");
    
    {
      AnnotationParam param =
        new PathAnnotationParam
        (aOrigName, 
         "The fully qualified original name of the node.",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aVersionID, 
         "The revision number of the node version.",
         null);
      addParam(param);
    }

    {
      AnnotationParam param =
        new StringAnnotationParam
        (aExtractedAt, 
         "The name of the local site from which the node was extracted.",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aExtractedOn, 
         "The time and date when the node was extracted.",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aExtractedBy, 
         "The name of the user which extracted the node.",
         null);
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        (aJarName, 
         "The name of the JAR archive containnig the extracted node.", 
         null);
      addParam(param);
    }
    
    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add(aOrigName);
      layout.add(aVersionID);
      layout.add(null);
      layout.add(aExtractedAt);
      layout.add(aExtractedOn);
      layout.add(aExtractedBy);
      layout.add(null);
      layout.add(aJarName);

      setLayout(layout);
    }
 
    addContext(AnnotationContext.PerVersion);
    removeContext(AnnotationContext.PerNode);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3108832280158282428L;

  public static final String aOrigName    = "OrigName";
  public static final String aVersionID   = "VersionID";
  public static final String aExtractedAt = "ExtractedAt";
  public static final String aExtractedOn = "ExtractedOn";
  public static final String aExtractedBy = "ExtractedBy";
  public static final String aJarName     = "JarName";
}
