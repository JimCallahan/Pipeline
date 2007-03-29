// $Id: UtilContextBuilderParam.java,v 1.3 2007/03/29 19:27:48 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.SimpleParamAccess;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   U T I L   C O N T E X T   B U I L D E R   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder parameter with an {@link UtilContext} value. <P> 
 */
public 
class UtilContextBuilderParam
  extends ComplexBuilderParam
  implements SimpleParamAccess
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
  UtilContextBuilderParam() 
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
   * @param value 
   *   The value for this parameter.  If this is <code>null</code> then the default
   *   value is gotten from {@link UtilContext}.
   */ 
  public
  UtilContextBuilderParam
  (
   String name,  
   String desc, 
   UtilContext value
  ) 
    throws PipelineException 
  {
    this(name, desc, value, false);
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
   *   The value for this parameter.  If this is <code>null</code> then the default
   *   value is gotten from {@link UtilContext}.
   *   
   * @param allowsNewView
   *   Does this UtilContext allow you to create a new working area, or do you need
   *   to choose from a list of existing ones.  	
   */ 
  public
  UtilContextBuilderParam
  (
   String name,  
   String desc, 
   UtilContext value,
   boolean allowsNewView
  ) 
    throws PipelineException 
  {
    super(name, desc);
    
    pAllowsNewView = allowsNewView;
    pWorkingAreas = BaseUtil.getWorkingAreas();
    pToolsets = BaseUtil.getActiveToolsets();
    
    if (value == null)
      value = BaseUtil.getDefaultUtilContext();
    
    String author = value.getAuthor();
    {
      pAuthorParam = 
	new EnumBuilderParam
	(aAuthor, 
	 "The user whose working area is being operated in.", 
	 author, 
	 new ArrayList<String>(pWorkingAreas.keySet()));
      addParam(pAuthorParam);
    }

    String view = value.getView();
    if (!pAllowsNewView) {
      ArrayList<String> views = new ArrayList<String> (pWorkingAreas.get(author));
      pViewParam = 
	new EnumBuilderParam
	(aView, 
	 "The user's working area that is being operated in", 
	 view, 
	 views);
      addParam(pViewParam);
    }
    else {
      pNewViewParam = 
	new StringBuilderParam
	(aView, 
	 "The user's working area that is being operated in", 
	 view );
      addParam(pNewViewParam);
    }
    
    {
      String toolset = value.getToolset();
      pToolsetParam = 
	new EnumBuilderParam
	(aToolset, 
	 "The default toolset for all created nodes.", 
	 toolset, 
	 pToolsets);
      addParam(pToolsetParam);
    }
    
    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add(aAuthor);
      layout.add(aView);
      layout.add(aToolset);
      setLayout(layout);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public UtilContext
  getUtilContextValue() 
  {
    String author = pAuthorParam.getStringValue();
    String view = null;
    if (!pAllowsNewView)  
      view = pViewParam.getStringValue();
    else
      view = pNewViewParam.getStringValue();
    String toolset = pToolsetParam.getStringValue();
    
    return new UtilContext(author, view, toolset);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S I M P L E   P A R A M E T E R   A C C E S S                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Implemented to allow this Complex Parameter to be used a Simple Parameter.  Returns
   * a {@link UtilContext};
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getValue()
  {
    return getUtilContextValue();
  }
  
  /**
   * Sets the value of the parameter from a UtilContext.
   */
  @SuppressWarnings("unchecked")
  public void
  setValue
  (
    Comparable value
  )
  {
    if ( ( value != null ) && !( value instanceof UtilContext ) )
      throw new IllegalArgumentException
        ("The parameter (" + pName + ") only accepts UtilContext values!");
    UtilContext context = (UtilContext) value;
    
    setValue(aAuthor, context.getAuthor());
    setValue(aView, context.getView());
    setValue(aToolset, context.getToolset());
  }
  
  /**
   * Sets the value from a single String.  Used for command line argument parsing.
   */
  public void 
  fromString
  (
    String key 
  )
  {
    String buffer[] = key.split(",");
    if (buffer.length != 3) {
      throw new IllegalArgumentException
        ("The string that was passed in is not valid.  To set the UtilContext value, " +
         "it needs three comma-separated string values in the form author,view,toolset");
    }
    setValue(aAuthor, buffer[0]);
    setValue(aView, buffer[1]);
    setValue(aToolset, buffer[2]);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Called when setting a param value.  Makes sure that the view values are correct.
   */
  protected void
  valueUpdated
  (
    @SuppressWarnings("unused")
    String paramName
  )
  {
    if (paramName.equals(aAuthor) && !pAllowsNewView)
    {
      String author = pAuthorParam.getStringValue();
      String view = pViewParam.getStringValue();
      TreeSet<String> views = pWorkingAreas.get(author);
  
      String viewValue = null;
      
      if(views.contains(view))
        viewValue = view;
      else if (views.contains("default"))
        viewValue = view; 
      else
        viewValue = views.first();
  
      
      pViewParam = 
        new EnumBuilderParam
        (aView, 
         "The user's working area that is being operated in", 
         viewValue, 
         new ArrayList<String>(views));
      
      replaceParam(pViewParam);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1468438453624432149L;
  
  public static final String aAuthor = "Author";
  public static final String aView = "View";
  public static final String aToolset = "Toolset";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private TreeMap<String, TreeSet<String>> pWorkingAreas;
  private ArrayList<String> pToolsets;
  
  private EnumBuilderParam pAuthorParam;
  private EnumBuilderParam pViewParam;
  private StringBuilderParam pNewViewParam;
  private EnumBuilderParam pToolsetParam;
  
  private boolean pAllowsNewView;
}
