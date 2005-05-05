// $Id: ActionInfo.java,v 1.1 2005/05/05 22:46:06 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A C T I O N   I N F O                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Action parameter information for nodes which are direct dependencies of the target
 * node. 
 */
public
class ActionInfo
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  ActionInfo()
  {
    pSingleParams = new TreeMap<String,ActionParam>();
    pSourceParams = new TreeMap<String,TreeMap<String,ActionParam>>();
  }

  /**
   * Construct a new instance.
   * 
   * @param action
   *   The action plugin from which to gather parameters. 
   * 
   * @param isEnabled
   *   Whether the action plugin is currently enabled.
   * 
   */ 
  public
  ActionInfo
  (
   BaseAction action, 
   boolean isEnabled
  ) 
  {
    super(action.getName());

    if(action == null)
      throw new IllegalArgumentException("The action cannot be (null)!");

    pIsEnabled = isEnabled;
    pVersionID = action.getVersionID();
    
    pSingleParams = new TreeMap<String,ActionParam>();
    if(action.hasSingleParams()) {
      for(ActionParam aparam : action.getSingleParams()) 
	pSingleParams.put(aparam.getName(), aparam);
    }

    pSourceParams = new TreeMap<String,TreeMap<String,ActionParam>>();    
    if(action.supportsSourceParams()) {
      pSupportsSourceParams = true;
      for(String sname : action.getSourceNames()) {
	TreeMap<String,ActionParam> aparams = new TreeMap<String,ActionParam>();
	pSourceParams.put(sname, aparams);
	for(ActionParam aparam : action.getSourceParams(sname))
	  aparams.put(aparam.getName(), aparam);
      }	  
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the revision number of the action plugin. 
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
  }
  
  /**
   * Whether the parent node has enabled the action plugin.
   */ 
  public boolean 
  isEnabled() 
  {
    return pIsEnabled;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S I N G L E   V A L U E D   P A R A M E T E R S                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does the action have any single valued parameters?
   */ 
  public boolean 
  hasSingleParams()
  {
    return (!pSingleParams.isEmpty());
  }

  /** 
   * Get the value of the single valued parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public Comparable
  getSingleParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    ActionParam param = getSingleParam(name); 
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter!");
    return param.getValue();
  }

  /** 
   * Get the single valued parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter or <CODE>null</CODE> if no parameter with the given name exists.
   */ 
  public ActionParam
  getSingleParam
  (
   String name   
  ) 
  {
    if(name == null)
      throw new IllegalArgumentException("The parameter name cannot be (null)!");
    return pSingleParams.get(name);
  }

  /** 
   * Get all of the single valued parameters. <P> 
   * 
   * The returned ArrayList may be empty if the action does not have any single 
   * valued parameters.
   * 
   * @return 
   *   The set of single valued parameters for this action.  
   */ 
  public Collection<ActionParam>
  getSingleParams()
  {
    return Collections.unmodifiableCollection(pSingleParams.values());
  }  


  /*----------------------------------------------------------------------------------------*/
  /*   P E R - S O U R C E   P A R A M E T E R S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  <P> 
   * 
   * Subclasses MUST override this method to return <CODE>true</CODE> if per-source 
   * paramters are allowed to be added to the action. 
   */ 
  public boolean 
  supportsSourceParams()
  {
    return pSupportsSourceParams;
  }

  /** 
   * Does this action have per-source parameters for the given node? 
   * 
   * @param source  
   *   The fully resolved dependency node name.
   */ 
  public boolean 
  hasSourceParams
  (
   String source      
  ) 
  {
    return pSourceParams.containsKey(source);
  }

  /** 
   * Get node names of the upstream nodes with per-source parameters.
   */ 
  public Set<String>
  getSourceNames()
  {
    return Collections.unmodifiableSet(pSourceParams.keySet());
  }

  /**
   * Get the value of the named parameter for the given upstream node. 
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no parameter with the given name exists for the given upstream node.
   */ 
  public Comparable
  getSourceParamValue
  (
   String source,
   String name  
  ) 
    throws PipelineException	
  {
    ActionParam param = getSourceParam(source, name);
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter for the upstream " + 
	 "node (" + source + ")!");

    return param.getValue();
  }

  /**
   * Get the named parameter for the given upstream node.
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter or <CODE>null</CODE> if no parameter with the given name exists
   *   for the given source.
   */ 
  public ActionParam
  getSourceParam
  (
   String source,
   String name  
  )
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<String,ActionParam> table = pSourceParams.get(source);
    if(table == null) 
      return null;

    return table.get(name);
  }

  /**
   * Get all of the per-source parameters for the given upstream node.  <P> 
   * 
   * The returned ArrayList may be empty if the given upstream node does not have any
   * parameters.
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The set of parameters for the given upstream node.  
   */ 
  public Collection<ActionParam>
  getSourceParams
  (
   String source  
  ) 
  {    
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<String,ActionParam> table = pSourceParams.get(source);
    if(table != null) 
      return Collections.unmodifiableCollection(table.values());
    else 
      return new ArrayList<ActionParam>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
    
    encoder.encode("VersionID", pVersionID);    
    encoder.encode("IsEnabled", pIsEnabled);

    if(!pSingleParams.isEmpty()) 
      encoder.encode("SingleParams", pSingleParams);
      
    encoder.encode("SupportsSourceParams", pSupportsSourceParams);
    if(!pSourceParams.isEmpty()) 
      encoder.encode("SourceParams", pSourceParams);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    VersionID vid = (VersionID) decoder.decode("VersionID");
    if(vid == null) 
      throw new GlueException("The \"VersionID\" was missing!");
    pVersionID = vid; 

    Boolean enabled = (Boolean) decoder.decode("IsEnabled");
    if((enabled != null) && enabled)
      pIsEnabled = true;

    TreeMap<String,ActionParam> single = 
      (TreeMap<String,ActionParam>) decoder.decode("SingleParams");   
    if(single != null) 
      pSingleParams.putAll(single);

    Boolean supports = (Boolean) decoder.decode("SupportsSourceParams");
    if((supports != null) && supports) {
      pSupportsSourceParams = true;

      TreeMap<String,TreeMap<String,ActionParam>> source = 
	(TreeMap<String,TreeMap<String,ActionParam>>) decoder.decode("SourceParams");   
      if(source != null) 
	pSourceParams.putAll(source);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4083238514328839966L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the action plugin. 
   */ 
  private VersionID  pVersionID;

  /**
   * Whether the action plugin is currently enabled.
   */
  private boolean pIsEnabled; 

  /** 
   * The table of single valued action parameters.
   */
  private TreeMap<String,ActionParam>  pSingleParams;    

  /**
   * Whether the action supports per-source parameters.
   */  
  private boolean  pSupportsSourceParams; 

  /** 
   * The table of action parameters associated with each linked upstream node.
   */
  private TreeMap<String,TreeMap<String,ActionParam>>  pSourceParams;    

}
