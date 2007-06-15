// $Id: UtilContextUtilityParam.java,v 1.1 2007/06/15 22:29:47 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   U T I L   C O N T E X T   U T I L I T Y   P A R A M                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A Utility parameter with an {@link UtilContext} value. <P> 
 */
public 
class UtilContextUtilityParam
  extends ComplexUtilityParam
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
  UtilContextUtilityParam() 
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
  UtilContextUtilityParam
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
  UtilContextUtilityParam
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
	new EnumUtilityParam
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
	new EnumUtilityParam
	(aView, 
	 "The user's working area that is being operated in", 
	 view, 
	 views);
      addParam(pViewParam);
    }
    else {
      pNewViewParam = 
	new StringUtilityParam
	(aView, 
	 "The user's working area that is being operated in", 
	 view );
      addParam(pNewViewParam);
    }
    
    {
      String toolset = value.getToolset();
      pToolsetParam = 
	new EnumUtilityParam
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
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected boolean
  needsUpdating()
  {
    return !pAllowsNewView;
  }
  
  /**
   * Called when setting a param value.  Makes sure that the view values are correct.
   */
  @Override
  protected boolean
  valueUpdated
  (
    List<String> paramName
  )
  {
    String name = paramName.get(0);
    if (name.equals(aAuthor)) {
      String author = pAuthorParam.getStringValue();
      String view = pViewParam.getStringValue();
      TreeSet<String> views = pWorkingAreas.get(author);
  
      String viewValue = null;
      
      if(views.contains(view))
        viewValue = view;
      else if (views.contains("default"))
        viewValue = "default"; 
      else
        viewValue = views.first();
  
      
      pViewParam = 
        new EnumUtilityParam
        (aView, 
         "The user's working area that is being operated in", 
         viewValue, 
         new ArrayList<String>(views));
      
      replaceParam(pViewParam);
      return true;
    }
    return false;
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
  
  private EnumUtilityParam pAuthorParam;
  private EnumUtilityParam pViewParam;
  private StringUtilityParam pNewViewParam;
  private EnumUtilityParam pToolsetParam;
  
  private boolean pAllowsNewView;
}
