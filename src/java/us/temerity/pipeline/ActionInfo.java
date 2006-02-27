// $Id: ActionInfo.java,v 1.3 2006/02/27 17:58:25 jim Exp $

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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  ActionInfo()
  {
    pSingleParams    = new TreeMap<String,ActionParam>();
    pSourceParams    = new TreeMap<String,TreeMap<String,ActionParam>>();
    pSecondaryParams = new TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>();
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
    pSecondaryParams = new TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>();
    if(action.supportsSourceParams()) {
      pSupportsSourceParams = true;
      for(String sname : action.getSourceNames()) {
	TreeMap<String,ActionParam> aparams = new TreeMap<String,ActionParam>();
	pSourceParams.put(sname, aparams);
	for(ActionParam aparam : action.getSourceParams(sname))
	  aparams.put(aparam.getName(), aparam);
      }	

      for(String sname : action.getSecondarySourceNames()) {
	TreeMap<FilePattern,TreeMap<String,ActionParam>> sparams = 
	  new TreeMap<FilePattern,TreeMap<String,ActionParam>>();
	pSecondaryParams.put(sname, sparams);

	for(FilePattern fpat : action.getSecondarySequences(sname)) {
	  TreeMap<String,ActionParam> aparams = new TreeMap<String,ActionParam>();
	  sparams.put(fpat, aparams);
	  for(ActionParam aparam : action.getSecondarySourceParams(sname, fpat))
	    aparams.put(aparam.getName(), aparam);
	}	
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
   * Does this action support per-source parameters?
   */ 
  public boolean 
  supportsSourceParams()
  {
    return pSupportsSourceParams;
  }

  /** 
   * Whether this action has per-source parameters for the primary sequence of the 
   * given node.
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
   * Whether this action has per-source parameters for a secondary sequence of the 
   * given node.
   * 
   * @param source  
   *   The fully resolved dependency node name.
   * 
   * @param fpat
   *   The file pattern of the secondary sequence of the upstream node.
   */ 
  public boolean 
  hasSecondarySourceParams
  (
   String source, 
   FilePattern fpat
  ) 
  {
    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    return ((ftable != null) && ftable.containsKey(fpat));
  }

  /** 
   * Get node names of the upstream nodes with primary file sequence per-source parameters.
   */ 
  public Set<String>
  getSourceNames()
  {
    return Collections.unmodifiableSet(pSourceParams.keySet());
  }

  /** 
   * Get node names of the upstream nodes with secondary file sequence per-source parameters.
   */ 
  public Set<String>
  getSecondarySourceNames()
  {
    return Collections.unmodifiableSet(pSecondaryParams.keySet());
  }

  /** 
   * Get the file patterns of all secondary sequences with per-source parameters for the 
   * given node. 
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   */ 
  public Set<FilePattern>
  getSecondarySequences
  (
   String source
  )
  {
    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable != null) 
      return Collections.unmodifiableSet(ftable.keySet());
    return new TreeSet<FilePattern>();
  }

  /**
   * Get the value of the named parameter for the primary file sequence of an upstream node.
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
   * Get the named parameter for the primary file sequence of an upstream node.
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
   * Get all of the per-source parameters associated with the primary file sequence of
   * an upstream node.  <P> 
   * 
   * The returned <CODE>Collection</CODE> may be empty if the upstream node does 
   * not have any parameters associated with its primary file sequence.
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

  /**
   * Get the value of the named parameter for a secondary file sequence of an upstream node. 
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The secondary sequence file pattern.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no parameter with the given name exists for the given secondary file sequence of 
   *   the upstream node.
   */ 
  public Comparable
  getSecondarySourceParamValue
  (
   String source,
   FilePattern fpat, 
   String name  
  ) 
    throws PipelineException	
  {
    ActionParam param = getSecondarySourceParam(source, fpat, name);
    if(param == null)
      throw new PipelineException
	("Unable to determine the value of the (" + name + ") parameter for the secondary " + 
	 "file sequence (" + fpat + ") of the upstream node (" + source + ")!");

    return param.getValue();
  }

  /**
   * Get the named parameter for a secondary file sequence of an upstream node.
   *
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The secondary sequence file pattern.
   * 
   * @param name  
   *   The name of the parameter. 
   * 
   * @return 
   *   The action parameter or <CODE>null</CODE> if no parameter with the given name exists
   *   for given secondary file sequence of the upstream node.
   */ 
  public ActionParam
  getSecondarySourceParam
  (
   String source,
   FilePattern fpat, 
   String name  
  )
  {
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable == null) 
      return null;

    TreeMap<String,ActionParam> table = ftable.get(fpat);
    if(table == null) 
      return null;

    return table.get(name);
  }

  /**
   * Get all of the per-source parameters associated with the given secondary file sequence
   * of an upstream node.  <P> 
   * 
   * The returned <CODE>Collection</CODE> may be empty if the upstream node does 
   * not have any parameters associated with the given secondary file sequence.
   * 
   * @param source  
   *   The fully resolved node name of the upstream node.
   * 
   * @param fpat
   *   The secondary sequence file pattern.
   * 
   * @return 
   *   The set of parameters for the given upstream node.  
   */ 
  public Collection<ActionParam>
  getSecondarySourceParams
  (
   String source,
   FilePattern fpat
  ) 
  {    
    if(source == null)
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(fpat == null) 
      throw new IllegalArgumentException
	("The secondary sequence file pattern cannot be (null)!");    

    TreeMap<FilePattern,TreeMap<String,ActionParam>> ftable = pSecondaryParams.get(source);
    if(ftable == null) 
      return new ArrayList<ActionParam>();

    TreeMap<String,ActionParam> table = ftable.get(fpat);
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
    
    if(!pSecondaryParams.isEmpty()) 
      encoder.encode("SecondarySourceParams", pSecondaryParams);
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

      TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>> secondary = 
	(TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>) 
        decoder.decode("SecondarySourceParams");   
      if(secondary != null) 
	pSecondaryParams.putAll(secondary);
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
   * The table of action parameters associated with the primary file sequence of an upstream 
   * node.  Indexed by fully resolved node name and action parameter name.
   */
  private TreeMap<String,TreeMap<String,ActionParam>>  pSourceParams;    

  /**
   * The table of action parameters associated with a secondary file sequence of an upstream
   * node.  Indexed by fully resolved node name, seconday sequence file pattern and action 
   * parameter name.
   */ 
  private TreeMap<String,TreeMap<FilePattern,TreeMap<String,ActionParam>>>  pSecondaryParams;

}
