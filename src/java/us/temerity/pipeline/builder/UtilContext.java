package us.temerity.pipeline.builder;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   U T I L   C O N T E X T                                                                */
/*------------------------------------------------------------------------------------------*/

public class UtilContext
  implements Comparable<UtilContext>, Glueable, Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class when
   * encountered during the reading of GLUE format files and should not be called from
   * user code.
   */
  public 
  UtilContext()
  {
    pAuthor = null;
    pView = null;
    pToolset = null;
  }

  /**
   * @param author
   * @param view
   * @param toolset
   */
  public 
  UtilContext
  (
    String author, 
    String view, 
    String toolset
  )
  {
    pAuthor = author;
    pView = view;
    pToolset = toolset;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * @return the toolset
   */
  public String 
  getToolset()
  {
    return pToolset;
  }

  /**
   * @return the author
   */
  public String 
  getAuthor()
  {
    return pAuthor;
  }

  /**
   * @return the view
   */
  public String 
  getView()
  {
    return pView;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns a default {@link UtilContext}
   */
  public static UtilContext 
  getDefaultUtilContext
  (
    MasterMgrClient client  
  )
    throws PipelineException
  {
    String toolset = client.getDefaultToolsetName();
    if (toolset == null)
      throw new PipelineException
        ("Unable to determine which toolset to use for the default UtilContext since " + 
         "no default toolset has yet been specified!"); 
    return new UtilContext(PackageInfo.sUser, "default", toolset);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public int 
  compareTo
  (
    UtilContext that
  )
  {
    int value = this.pAuthor.compareTo(that.getAuthor());
    if(value == 0) {
      value = this.pView.compareTo(that.getView());
    }
    if(value == 0) {
      value = this.pToolset.compareTo(that.getToolset());
    }
    return value;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  fromGlue
  (
    GlueDecoder decoder
  ) 
  {
    pAuthor = (String) decoder.decode("Author");
    pView = (String) decoder.decode("View");
    pToolset = (String) decoder.decode("Toolset");
  }

  public void 
  toGlue
  (
    GlueEncoder encoder
  ) 
  throws GlueException
  {
    encoder.encode("Author", pAuthor);
    encoder.encode("View", pView);
    encoder.encode("Toolset", pToolset);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6913346629352411368L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private String pAuthor;

  private String pView;

  private String pToolset;
}
