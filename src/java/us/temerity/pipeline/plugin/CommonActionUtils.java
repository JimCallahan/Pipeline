// $Id: CommonActionUtils.java,v 1.2 2007/04/05 08:38:02 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M M O N   A C T I O N   U T I L S                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of most Action plugins which augments BaseAction with many commonly
 * useful helper methods.<P> 
 * 
 * This class provides convenience methods for accessing and validating Action parameters, 
 * constructing node target/source related file system paths, creating subprocesses and other 
 * common operations performed inside the {@link BaseAcrion#prep prep} method of most Actions.
 */
public 
class CommonActionUtils
  extends BaseAction 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the action.  
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  CommonActionUtils
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   M I S C   F I L E   U T I L S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract working area file system paths in the given node file sequence. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param name
   *   The unique node name.
   * 
   * @param fseq
   *   The file sequence associated with the node. 
   */
  public static final ArrayList<Path> 
  getWorkingNodeFilePaths
  (
   ActionAgenda agenda, 
   String name, 
   FileSeq fseq
  ) 
  {
    NodeID nodeID = new NodeID(agenda.getNodeID(), name);
    Path wpath = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent());

    ArrayList<Path> paths = new ArrayList<Path>();
    for(Path fpath : fseq.getPaths()) 
      paths.add(new Path(wpath, fpath));
        
    return paths;
  }

  /**
   * Get the abstract working area file system path to the first file in the given 
   * node file sequence. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param name
   *   The unique node name.
   * 
   * @param fseq
   *   The file sequence associated with the node. 
   */
  public static final Path
  getWorkingNodeFilePath
  (
   ActionAgenda agenda, 
   String name, 
   FileSeq fseq
  ) 
  {
    return getWorkingNodeFilePath(agenda, name, fseq.getPath(0)); 
  }

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param name
   *   The unique node name.
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public static final Path
  getWorkingNodeFilePath
  (
   ActionAgenda agenda, 
   String name, 
   Path file
  ) 
  {
    return getWorkingNodeFilePath(new NodeID(agenda.getNodeID(), name), file); 
  }

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public static final Path
  getWorkingNodeFilePath
  (
   NodeID nodeID,
   Path file
  ) 
  {
    return getWorkingNodeFilePath(nodeID, file.toString());
  }

  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  public static final Path
  getWorkingNodeFilePath
  (
   NodeID nodeID,
   String file
  ) 
  {
    return new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + file);     
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a unique temporary script file appropriate for the current operating 
   * system type.<P> 
   * 
   * On Unix/MacOS this will be an bash(1) script, while on Windows the script will
   * be a BAT file suitable for evaluation by "Cmd".  The returned script file is suitable
   * for use as with the {@link #createScriptSubProcess createScriptSubProcess} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   */ 
  public final File
  createTempScript
  (
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    if(PackageInfo.sOsType == OsType.Windows)
      return createTemp(agenda, "bat");
    else 
      return createTemp(agenda, 0644, "bash");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M M O N   P A R A M E T E R S                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Adds an additional command-line options parameter to the action.<P> 
   * 
   * The following single valued parameters is added: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   Extra Options <BR>
   *   <DIV style="margin-left: 40px;">
   *     Additional command-line arguments. <BR> 
   *   </DIV> <BR>
   * </DIV> <P> 
   * 
   * This method should be called in the subclass constructor before specifying parameter
   * layouts.
   */
  protected final void 
  addExtraOptionsParam() 
  {
    ActionParam param = 
      new StringActionParam
      (aExtraOptions,
       "Additional command-line arguments.", 
       null);
    addSingleParam(param);
  }

  /**
   * Add the parameter created by the {@link #addExtraOptionsParam addExtraOptionsParam} 
   * method to the given parameter layout group.
   */ 
  protected final void 
  addExtraOptionsParamToLayout
  (
   LayoutGroup layout
  ) 
  {
    layout.addEntry(aExtraOptions);
  }

  /**
   * Get any additional command-line arguments specified using the action parameter created 
   * by the {@link #addExtraOptionsParam addExtraOptionsParam} method.
   */ 
  public final ArrayList<String>
  getExtraOptionsArgs() 
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();

    String extra = (String) getSingleParamValue(aExtraOptions);
    if(extra != null) {
      String parts[] = extra.split("\\p{Space}");
      int wk;
      for(wk=0; wk<parts.length; wk++) {
        if(parts[wk].length() > 0) 
          args.add(parts[wk]);
      }
    }

    return args;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R   L O O K U P                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the selected index of the single valued Enum parameter with the given name.<P> 
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
  public final int
  getSingleEnumParamIndex
  (
   String name   
  ) 
    throws PipelineException
  {
    EnumActionParam param = (EnumActionParam) getSingleParam(name);
    if(param == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not illegal!"); 
      
    return param.getIndex();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Boolean parameter with the given name.
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
  public final boolean
  getSingleBooleanParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSingleParamValue(name);  
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    return value;
  }  

  /** 
   * Get the value of the single valued Boolean parameter with the given name.<P> 
   * 
   * If <CODE>null</CODE> value is treated as <CODE>false</CODE>.
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
  public final boolean
  getSingleOptionalBooleanParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSingleParamValue(name); 
    return ((value != null) && value);
  }  


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Long parameter with the given name.<P> 
   * 
   * This method can be used to retrieve ByteSizeActionParam values.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final long
  getSingleLongParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleLongParamValue(name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Long parameter with 
   * the given name. <P> 
   * 
   * This method can also be used to retrieve ByteSizeActionParam values.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final long
  getSingleLongParamValue
  (
   String name, 
   Range<Long> range
  ) 
    throws PipelineException
  {
    Long value = (Long) getSingleParamValue(name); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Integer parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final int
  getSingleIntegerParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleIntegerParamValue(name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Integer parameter with 
   * the given name. <P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final int
  getSingleIntegerParamValue
  (
   String name, 
   Range<Integer> range
  ) 
    throws PipelineException
  {
    Integer value = (Integer) getSingleParamValue(name); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Double parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final double
  getSingleDoubleParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleDoubleParamValue(name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null Double parameter with 
   * the given name. <P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final double
  getSingleDoubleParamValue
  (
   String name, 
   Range<Double> range
  ) 
    throws PipelineException
  {
    Double value = (Double) getSingleParamValue(name); 
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued String parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value or 
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists.
   */ 
  public final String
  getSingleStringParamValue
  (
   String name   
  ) 
    throws PipelineException
  { 
    String value = (String) getSingleParamValue(name); 
    if((value != null) && (value.length() > 0))
      return value;

    return null;    
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P A T H   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract paths to the primary files associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param desc
   *   A brief description of the type of files expected (used to generate error messages).
   * 
   * @return 
   *   The paths to the target files. 
   */ 
  public final ArrayList<Path>
  getPrimaryTargetPaths
  (
   ActionAgenda agenda, 
   String desc
  ) 
    throws PipelineException 
  {
    return getPrimaryTargetPaths(agenda, new ArrayList<String>(), desc);
  }

  /**
   * Get the abstract paths to the primary files associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffix
   *   The allowable filename suffix.
   * 
   * @param desc
   *   A brief description of the type of files expected (used to generate error messages).
   * 
   * @return 
   *   The paths to the target files. 
   */ 
  public final ArrayList<Path>
  getPrimaryTargetPaths
  (
   ActionAgenda agenda, 
   String suffix, 
   String desc
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add(suffix);
    
    return getPrimaryTargetPaths(agenda, suffixes, desc);
  }

  /**
   *  Get the abstract paths to the primary files associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffixes
   *   The allowable filename suffixes.
   * 
   * @param desc
   *   A brief description of the type of files expected (used to generate error messages).
   * 
   * @return 
   *   The paths to the target files. 
   */ 
  public final ArrayList<Path>
  getPrimaryTargetPaths
  (
   ActionAgenda agenda, 
   Collection<String> suffixes, 
   String desc
  ) 
    throws PipelineException 
  {
    FileSeq fseq = agenda.getPrimaryTarget();
    String suffix = fseq.getFilePattern().getSuffix();
    if(!suffixes.isEmpty() && ((suffix == null) || !suffixes.contains(suffix))) {
      throw new PipelineException
        ("The " + getName() + " Action requires that the primary target file sequence " + 
         "(" + fseq + ") must be " + desc + "!");
    }

    ArrayList<Path> tpaths = new ArrayList<Path>();
    for(Path path : fseq.getPaths()) 
      tpaths.add(new Path(agenda.getTargetPath(), path)); 
    return tpaths;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary file associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the target file. 
   */ 
  public final Path
  getPrimaryTargetPath
  (
   ActionAgenda agenda, 
   String desc
  ) 
    throws PipelineException 
  {
    return getPrimaryTargetPath(agenda, new ArrayList<String>(), desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffix
   *   The allowable filename suffix.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the target file. 
   */ 
  public final Path
  getPrimaryTargetPath
  (
   ActionAgenda agenda, 
   String suffix, 
   String desc
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add(suffix);
    
    return getPrimaryTargetPath(agenda, suffixes, desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a target node. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffixes
   *   The allowable filename suffixes.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the target file. 
   */ 
  public final Path
  getPrimaryTargetPath
  (
   ActionAgenda agenda, 
   Collection<String> suffixes, 
   String desc
  ) 
    throws PipelineException 
  {
    FileSeq fseq = agenda.getPrimaryTarget();
    String suffix = fseq.getFilePattern().getSuffix();
    if(!fseq.isSingle() || 
       (!suffixes.isEmpty() && ((suffix == null) || !suffixes.contains(suffix)))) {
      throw new PipelineException
        ("The " + getName() + " Action requires that the primary target file sequence " + 
         "(" + fseq + ") must be a single " + desc + "!");
    }

    return new Path(agenda.getTargetPath(), fseq.getPath(0));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract paths to the primary files associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param desc
   *   A brief description of the type of files expected (used to generate error messages).
   * 
   * @return 
   *   The paths to the primary files of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public final ArrayList<Path>
  getPrimarySourcePaths
  (
   String pname, 
   ActionAgenda agenda, 
   String desc
  ) 
    throws PipelineException 
  {
    return getPrimarySourcePaths(pname, agenda, new ArrayList<String>(), desc);
  }

  /**
   * Get the abstract paths to the primary files associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffix
   *   The allowable filename suffix.
   * 
   * @param desc
   *   A brief description of the type of files expected (used to generate error messages).
   * 
   * @return 
   *   The paths to the primary files of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public final ArrayList<Path>
  getPrimarySourcePaths
  (
   String pname, 
   ActionAgenda agenda, 
   String suffix, 
   String desc
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add(suffix);

    return getPrimarySourcePaths(pname, agenda, suffixes, desc);
  }

  /**
   * Get the abstract paths to the primary files associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffixes
   *   The allowable filename suffixes.
   * 
   * @param desc
   *   A brief description of the type of files expected (used to generate error messages).
   * 
   * @return 
   *   The paths to the primary files of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public final ArrayList<Path>
  getPrimarySourcePaths
  (
   String pname, 
   ActionAgenda agenda, 
   Collection<String> suffixes, 
   String desc
  ) 
    throws PipelineException 
  {
    ArrayList<Path> paths = null; 

    ActionParam param = getSingleParam(pname);
    String title = param.getNameUI();

    String mname = (String) param.getValue();
    if(mname != null) {
      FileSeq fseq = agenda.getPrimarySource(mname);
      if(fseq == null) 
	throw new PipelineException
	  ("Somehow the " + title + " node (" + mname + ") was not one of the " + 
	   "source nodes!");
      
      String suffix = fseq.getFilePattern().getSuffix();
      if(!suffixes.isEmpty() && ((suffix == null) || !suffixes.contains(suffix))) {
	throw new PipelineException
	  ("The " + getName() + " Action requires that the source node specified by the " + 
	   title + " parameter (" + mname + ") must have " + desc + " as its primary file " + 
           "sequence!");
      }
      
      paths = getWorkingNodeFilePaths(agenda, mname, fseq); 
    }

    return paths;	      
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract path to the single primary file associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the primary file of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public final Path
  getPrimarySourcePath
  (
   String pname, 
   ActionAgenda agenda, 
   String desc
  ) 
    throws PipelineException 
  {
    return getPrimarySourcePath(pname, agenda, new ArrayList<String>(), desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffix
   *   The allowable filename suffix.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the primary file of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public final Path
  getPrimarySourcePath
  (
   String pname, 
   ActionAgenda agenda, 
   String suffix, 
   String desc
  ) 
    throws PipelineException 
  {
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add(suffix);

    return getPrimarySourcePath(pname, agenda, suffixes, desc);
  }

  /**
   * Get the abstract path to the single primary file associated with a source node 
   * specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued parameter which names the source node.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param suffixes
   *   The allowable filename suffixes.
   * 
   * @param desc
   *   A brief description of the type of file expected (used to generate error messages).
   * 
   * @return 
   *   The path to the primary file of the source node or 
   *   <CODE>null</CODE> if none was specified.
   */ 
  public final Path
  getPrimarySourcePath
  (
   String pname, 
   ActionAgenda agenda, 
   Collection<String> suffixes, 
   String desc
  ) 
    throws PipelineException 
  {
    Path path = null; 

    ActionParam param = getSingleParam(pname);
    String title = param.getNameUI();

    String mname = (String) param.getValue();
    if(mname != null) {
      FileSeq fseq = agenda.getPrimarySource(mname);
      if(fseq == null) 
	throw new PipelineException
	  ("Somehow the " + title + " node (" + mname + ") was not one of the " + 
	   "source nodes!");
      
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || 
         (!suffixes.isEmpty() && ((suffix == null) || !suffixes.contains(suffix)))) {
	throw new PipelineException
	  ("The " + getName() + " Action requires that the source node specified by the " + 
	   title + " parameter (" + mname + ") must have a single " + desc + " as " + 
           "its primary file sequence!");
      }
      
      path = getWorkingNodeFilePath(agenda, mname, fseq); 
    }

    return path;	      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S U B P R O C E S S   C R E A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/
   
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method in a mostly OS-independent manner.<P> 
   * 
   * The caller is reponsible for handling any differences in program name and arguments 
   * between the different operating system types, but this method will handle specifying
   * the process owner, title, environment and working directory. <P> 
   * 
   * When run on a Unix or MacOS system, the working directory is the working area directory 
   * containing the target node.  On Windows, the working directory is always the local 
   * temporary directory since many Windows programs fail if the working directory in on a 
   * network share.  The caller is responsible for making any target file paths relative
   * to the working directory on Unix/MacOS and absolute on Windows.  The correct path for 
   * target files can be obtained using the {@link ActionAgenda#getTargetPath 
   * ActionAgenda.getTargetPath} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public final SubProcessHeavy
  createSubProcess
  (
   ActionAgenda agenda,
   String program, 
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    return createSubProcess(agenda, program, null, null, outFile, errFile);
  }

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method in a mostly OS-independent manner.<P> 
   * 
   * The caller is reponsible for handling any differences in program name and arguments 
   * between the different operating system types, but this method will handle specifying
   * the process owner, title, environment and working directory. <P> 
   * 
   * When run on a Unix or MacOS system, the working directory is the working area directory 
   * containing the target node.  On Windows, the working directory is always the local 
   * temporary directory since many Windows programs fail if the working directory in on a 
   * network share.  The caller is responsible for making any target file paths relative
   * to the working directory on Unix/MacOS and absolute on Windows.  The correct path for 
   * target files can be obtained using the {@link ActionAgenda#getTargetPath 
   * ActionAgenda.getTargetPath} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param args  
   *   The command line arguments of the program to execute or 
   *   <CODE>null</CODE> for an empty argument list.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public final SubProcessHeavy
  createSubProcess
  (
   ActionAgenda agenda,
   String program, 
   ArrayList<String> args,
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    return createSubProcess(agenda, program, args, null, outFile, errFile);
  }

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method in a mostly OS-independent manner.<P> 
   * 
   * The caller is reponsible for handling any differences in program name and arguments 
   * between the different operating system types, but this method will handle specifying
   * the process owner, title, environment and working directory.  The default Toolset 
   * generated environment can be overridden if specified using a non-null <CODE>env</CODE> 
   * parameter.<P> 
   * 
   * When run on a Unix or MacOS system, the working directory is the working area directory 
   * containing the target node.  On Windows, the working directory is always the local 
   * temporary directory since many Windows programs fail if the working directory in on a 
   * network share.  The caller is responsible for making any target file paths relative
   * to the working directory on Unix/MacOS and absolute on Windows.  The correct path for 
   * target files can be obtained using the {@link ActionAgenda#getTargetPath 
   * ActionAgenda.getTargetPath} method.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param args  
   *   The command line arguments of the program to execute or 
   *   <CODE>null</CODE> for an empty argument list.
   * 
   * @param env  
   *   The environment under which the OS level process is run or 
   *   <CODE>null</CODE> to use the environment defined by the ActionAgenda.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public final SubProcessHeavy
  createSubProcess
  (
   ActionAgenda agenda,
   String program, 
   ArrayList<String> args,
   Map<String,String> env,  
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    Path dir = null; 
    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      dir = agenda.getTargetPath();
      break;

    case Windows:
      dir = PackageInfo.sTempPath;
    }

    return createSubProcess(agenda, program, args, env, dir.toFile(), outFile, errFile);
  }
  
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method in a mostly OS-independent manner.<P> 
   * 
   * The caller is reponsible for handling any differences in program name and arguments 
   * between the different operating system types, but this method will handle specifying
   * the process owner, title, environment.  The default Toolset generated environment can 
   * be overridden if specified using a non-null <CODE>env</CODE> parameter.<P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute as an OS level subprocess.  
   * 
   * @param args  
   *   The command line arguments of the program to execute or 
   *   <CODE>null</CODE> for an empty argument list.
   * 
   * @param env  
   *   The environment under which the OS level process is run or 
   *   <CODE>null</CODE> to use the environment defined by the ActionAgenda.
   * 
   * @param dir 
   *   The working directory where the subprocess is run.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public final SubProcessHeavy
  createSubProcess
  (
   ActionAgenda agenda,
   String program, 
   ArrayList<String> args,
   Map<String,String> env,  
   File dir, 
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    try {
      String owner = agenda.getSubProcessOwner();
      String title = getName() + "-" + agenda.getJobID(); 

      ArrayList<String> nargs = args;
      if(nargs == null) 
        nargs = new ArrayList<String>();

      Map<String,String> nenv = env;
      if(nenv == null) 
        nenv = agenda.getEnvironment();

      return new SubProcessHeavy(owner, title, program, nargs, nenv, dir, outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method when generating a temporary script to execute some 
   * OS-specific commands. <P>
   * 
   * If running on Unix or MacOS, the supplied script must be a bash(1) shell script while on
   * Windows the script must be an executable BAT/CMD file. The Action is reposible for 
   * making sure that the contents of these scripts are portable.  This method simply takes 
   * care of the common wrapper code needed to instantiate a {@link SubProcessHeavy} to run 
   * the script.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param script
   *   The temporary script file to execute.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */ 
  public final SubProcessHeavy
  createScriptSubProcess
  (
   ActionAgenda agenda,
   File script, 
   File outFile, 
   File errFile    
  ) 
    throws PipelineException
  {
    String program = null;
    ArrayList<String> args = new ArrayList<String>();

    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      program = "bash";
      args.add(script.getPath());
      break;

    case Windows:
      program = script.getPath(); 
    }

    return createSubProcess(agenda, program, args, outFile, errFile);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method when simply copying a single temporary file created in 
   * the <CODE>prep</CODE> method to the target location.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param temp
   *   The temporary file to copy.
   * 
   * @param target
   *   The abtract path to the location of the target file.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */ 
  public final SubProcessHeavy
  createTempCopySubProcess
  (
   ActionAgenda agenda,
   File temp, 
   Path target, 
   File outFile, 
   File errFile    
  ) 
    throws PipelineException
  {
    String program = null;
    ArrayList<String> args = new ArrayList<String>();

    if(PackageInfo.sOsType == OsType.Windows) {
      program = "cmd.exe";
      
      args.add("/c");
      args.add("\"copy /y " + temp.getPath() + " " + target.toOsString() + "\"");
    }
    else {
      program = "cp";
      
      args.add(temp.getPath());
      args.add(target.toOsString());
    } 
    
    return createSubProcess(agenda, program, args, outFile, errFile);
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -826177190846836889L;
  
  public static final String aExtraOptions = "ExtraOptions"; 

}



