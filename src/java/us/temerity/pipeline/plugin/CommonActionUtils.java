// $Id: CommonActionUtils.java,v 1.10 2008/02/04 04:00:10 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

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
 * common operations performed inside the {@link BaseAction#prep prep} method of most Actions.
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
        ("There is no Action parameter named (" + name + ")!");
      
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
   * Get the value of the single valued non-null Tuple2i parameter with the given name.
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
  public final Tuple2i
  getSingleTuple2iParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleTuple2iParamValue(name, null, false);
  }

  /** 
   * Get the value of the single valued Tuple2i parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE> and allowsNull is <CODE>false</CODE>.
   */ 
  public final Tuple2i
  getSingleTuple2iParamValue
  (
   String name, 
   boolean allowsNull 
  ) 
    throws PipelineException
  {
    return getSingleTuple2iParamValue(name, null, allowsNull);
  }

  /** 
   * Get the bounds checked value of the single valued Tuple2i parameter with 
   * the given name. <P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final Tuple2i
  getSingleTuple2iParamValue
  (
   String name, 
   Range<Tuple2i> range, 
   boolean allowsNull
  ) 
    throws PipelineException
  {
    Tuple2i value = (Tuple2i) getSingleParamValue(name); 
    if(value == null) {
      if(!allowsNull) 
        throw new PipelineException
          ("The required parameter (" + name + ") was not set!"); 
      return null;
    }

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Tuple3i parameter with the given name.
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
  public final Tuple3i
  getSingleTuple3iParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleTuple3iParamValue(name, null, false);
  }

  /** 
   * Get the value of the single valued Tuple3i parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE> and allowsNull is <CODE>false</CODE>.
   */ 
  public final Tuple3i
  getSingleTuple3iParamValue
  (
   String name, 
   boolean allowsNull   
  ) 
    throws PipelineException
  {
    return getSingleTuple3iParamValue(name, null, allowsNull);
  }

  /** 
   * Get the bounds checked value of the single valued Tuple3i parameter with 
   * the given name. <P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final Tuple3i
  getSingleTuple3iParamValue
  (
   String name, 
   Range<Tuple3i> range, 
   boolean allowsNull
  ) 
    throws PipelineException
  {
    Tuple3i value = (Tuple3i) getSingleParamValue(name); 
    if(value == null) {
      if(!allowsNull) 
        throw new PipelineException
          ("The required parameter (" + name + ") was not set!"); 
      return null;
    }

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
   * Get the value of the single valued non-null Tuple2d parameter with the given name.
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
  public final Tuple2d
  getSingleTuple2dParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleTuple2dParamValue(name, null, false);
  }

  /** 
   * Get the value of the single valued Tuple2d parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE> and allowsNull is <CODE>false</CODE>.
   */ 
  public final Tuple2d
  getSingleTuple2dParamValue
  (
   String name, 
   boolean allowsNull 
  ) 
    throws PipelineException
  {
    return getSingleTuple2dParamValue(name, null, allowsNull);
  }

  /** 
   * Get the bounds checked value of the single valued Tuple2d parameter with 
   * the given name. <P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final Tuple2d
  getSingleTuple2dParamValue
  (
   String name, 
   Range<Tuple2d> range, 
   boolean allowsNull
  ) 
    throws PipelineException
  {
    Tuple2d value = (Tuple2d) getSingleParamValue(name); 
    if(value == null) {
      if(!allowsNull) 
        throw new PipelineException
          ("The required parameter (" + name + ") was not set!"); 
      return null;
    }

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }
 

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Tuple3d parameter with the given name.
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
  public final Tuple3d
  getSingleTuple3dParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleTuple3dParamValue(name, null, false);
  }

  /** 
   * Get the value of the single valued Tuple3d parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE> and allowsNull is <CODE>false</CODE>.
   */ 
  public final Tuple3d
  getSingleTuple3dParamValue
  (
   String name, 
   boolean allowsNull 
  ) 
    throws PipelineException
  {
    return getSingleTuple3dParamValue(name, null, allowsNull);
  }

  /** 
   * Get the bounds checked value of the single valued Tuple3d parameter with 
   * the given name. <P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final Tuple3d
  getSingleTuple3dParamValue
  (
   String name, 
   Range<Tuple3d> range, 
   boolean allowsNull
  ) 
    throws PipelineException
  {
    Tuple3d value = (Tuple3d) getSingleParamValue(name); 
    if(value == null) {
      if(!allowsNull) 
        throw new PipelineException
          ("The required parameter (" + name + ") was not set!"); 
      return null;
    }

    if((range != null) && !range.isInside(value))
      throw new PipelineException
        ("The value (" + value + ") of parameter (" + name + ") was outside the valid " + 
         "range of values: " + range + "!"); 

    return value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null Tuple4d parameter with the given name.
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
  public final Tuple4d
  getSingleTuple4dParamValue
  (
   String name   
  ) 
    throws PipelineException
  {
    return getSingleTuple4dParamValue(name, null, false);
  }

  /** 
   * Get the value of the single valued Tuple4d parameter with the given name.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or 
   *   the value is <CODE>null</CODE> and allowsNull is <CODE>false</CODE>.
   */ 
  public final Tuple4d
  getSingleTuple4dParamValue
  (
   String name, 
   boolean allowsNull 
  ) 
    throws PipelineException
  {
    return getSingleTuple4dParamValue(name, null, allowsNull);
  }

  /** 
   * Get the bounds checked value of the single valued Tuple4d parameter with 
   * the given name. <P> 
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param range
   *   The valid range values for the parameter. 
   * 
   * @param allowsNull
   *   Whether this parameter can have a null value.
   * 
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final Tuple4d
  getSingleTuple4dParamValue
  (
   String name, 
   Range<Tuple4d> range, 
   boolean allowsNull
  ) 
    throws PipelineException
  {
    Tuple4d value = (Tuple4d) getSingleParamValue(name); 
    if(value == null) {
      if(!allowsNull) 
        throw new PipelineException
          ("The required parameter (" + name + ") was not set!"); 
      return null;
    }

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
   * @param allowsNull
   *   Whether this parameter can have a null or empty value.
   *   
   * @return 
   *   The action parameter value or (optionally)
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued parameter with the given name exists or (optionally)
   *   if the value is null or empty.
   */ 
  public final String
  getSingleStringParamValue
  (
   String name,
   boolean allowsNull
  ) 
    throws PipelineException
  { 
    String value = (String) getSingleParamValue(name); 
    if((value == null) || (value.length() == 0)) {
      if (!allowsNull)
        throw new PipelineException
          ("Cannot have an empty String value for parameter (" + name + ")");
      value = null;
    }
    return value;    
  }
  
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
   *   If no single valued parameter with the given name exists
   */ 
  public final String
  getSingleStringParamValue
  (
   String name
  ) 
    throws PipelineException
  {
    return getSingleStringParamValue(name, true);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S O U R C E   P A R A M E T E R   L O O K U P                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the selected index of the single valued source Enum parameter with the given name.<P>
   * 
   * @param source
   *   The name of the source.
   *   
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists.
   */ 
  public final int
  getSourceEnumParamIndex
  (
    String source,
    String name   
  ) 
    throws PipelineException
  {
    EnumActionParam param = (EnumActionParam) getSourceParam(source, name);
    if(param == null) 
      throw new PipelineException
        ("There is no Action parameter named (" + name + ")!");
      
    return param.getIndex();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null source Boolean parameter with the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists.
   */ 
  public final boolean
  getSourceBooleanParamValue
  (
    String source,
    String name     
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSourceParamValue(source, name);  
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    return value;
  }  

  /** 
   * Get the value of the single valued source Boolean parameter with the given name.<P> 
   * 
   * If <CODE>null</CODE> value is treated as <CODE>false</CODE>.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists.
   */ 
  public final boolean
  getSourceOptionalBooleanParamValue
  (
    String source,
    String name     
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSourceParamValue(source, name);
    return ((value != null) && value);
  }  


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null source Long parameter with the given name.<P>
   * 
   * This method can be used to retrieve ByteSizeActionParam values.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final long
  getSourceLongParamValue
  (
    String source,
    String name     
  ) 
    throws PipelineException
  {
    return getSourceLongParamValue(source, name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null source Long parameter with 
   * the given name. <P> 
   * 
   * This method can also be used to retrieve ByteSizeActionParam values.
   * 
   * @param source
   *   The name of the source.
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
   *   If no single valued source parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final long
  getSourceLongParamValue
  (
    String source,
    String name,   
    Range<Long> range
  ) 
    throws PipelineException
  {
    Long value = (Long) getSourceParamValue(source, name);
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
   * Get the value of the single valued non-null source Integer parameter with the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final int
  getSourceIntegerParamValue
  (
    String source,
    String name     
  ) 
    throws PipelineException
  {
    return getSourceIntegerParamValue(source, name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null source Integer parameter with 
   * the given name. <P> 
   * 
   * @param source
   *   The name of the source.
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
   *   If no single valued source parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final int
  getSourceIntegerParamValue
  (
    String source,
    String name, 
    Range<Integer> range
  ) 
    throws PipelineException
  {
    Integer value = (Integer) getSourceParamValue(source, name);
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
   * Get the value of the single valued non-null source Double parameter with the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final double
  getSourceDoubleParamValue
  (
    String source,
    String name     
  ) 
    throws PipelineException
  {
    return getSourceDoubleParamValue(source, name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null source Double parameter with 
   * the given name. <P> 
   * 
   * @param source
   *   The name of the source.
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
   *   If no single valued source parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final double
  getSourceDoubleParamValue
  (
    String source,
    String name,   
    Range<Double> range
  ) 
    throws PipelineException
  {
    Double value = (Double) getSourceParamValue(source, name);
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
   * Get the value of the single valued source String parameter with the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param allowsNull
   *   Whether this parameter can have a null or empty value.
   *   
   * @return 
   *   The action parameter value or (optionally)
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists or (optionally)
   *   if the value is null or empty.
   */ 
  public final String
  getSourceStringParamValue
  (
    String source,
    String name,
    boolean allowsNull
  ) 
    throws PipelineException
  { 
    String value = (String) getSourceParamValue(source, name);
    if((value == null) || (value.length() == 0)) {
      if (!allowsNull)
        throw new PipelineException
          ("Cannot have an empty String value for parameter (" + name + ")");
      value = null;
    }
    return value;
  }
  
  /** 
   * Get the value of the single valued source String parameter with the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value or 
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued source parameter with the given name exists 
   */ 
  public final String
  getSourceStringParamValue
  (
    String source,
    String name
  ) 
    throws PipelineException
  {
    return getSourceStringParamValue(source, name, true);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D A R Y   S O U R C E   P A R A M E T E R   L O O K U P                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the selected index of the single valued secondary source Enum parameter with the 
   * given name.<P> 
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence. 
   *  
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists.
   */ 
  public final int
  getSecondarySourceEnumParamIndex
  (
    String source,
    FilePattern fpat,
    String name   
  ) 
    throws PipelineException
  {
    EnumActionParam param = (EnumActionParam) getSecondarySourceParam(source, fpat, name);
    if(param == null) 
      throw new PipelineException
        ("There is no Action parameter named (" + name + ")!");
      
    return param.getIndex();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null secondary  source Boolean parameter with 
   * the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists.
   */ 
  public final boolean
  getSecondarySourceBooleanParamValue
  (
    String source,
    FilePattern fpat,
    String name     
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSecondarySourceParamValue(source, fpat, name);  
    if(value == null) 
      throw new PipelineException
        ("The required parameter (" + name + ") was not set!"); 

    return value;
  }  

  /** 
   * Get the value of the single valued secondary source Boolean parameter with 
   * the given name.<P> 
   * 
   * If <CODE>null</CODE> value is treated as <CODE>false</CODE>.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists.
   */ 
  public final boolean
  getSecondarySourceOptionalBooleanParamValue
  (
    String source,
    FilePattern fpat,
    String name     
  ) 
    throws PipelineException
  {
    Boolean value = (Boolean) getSecondarySourceParamValue(source, fpat, name);;
    return ((value != null) && value);
  }  


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the single valued non-null secondary source Long parameter with the 
   * given name.<P> 
   * 
   * This method can be used to retrieve ByteSizeActionParam values.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final long
  getSecondarySourceLongParamValue
  (
    String source,
    FilePattern fpat,
    String name     
  ) 
    throws PipelineException
  {
    return getSecondarySourceLongParamValue(source, fpat, name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null secondary source Long 
   * parameter with the given name. <P> 
   * 
   * This method can also be used to retrieve ByteSizeActionParam values.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
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
   *   If no single valued secondary source parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final long
  getSecondarySourceLongParamValue
  (
    String source,
    FilePattern fpat,
    String name,   
    Range<Long> range
  ) 
    throws PipelineException
  {
    Long value = (Long) getSecondarySourceParamValue(source, fpat, name);
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
   * Get the value of the single valued non-null secondary source Integer parameter with 
   * the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final int
  getSecondarySourceIntegerParamValue
  (
    String source,
    FilePattern fpat,
    String name     
  ) 
    throws PipelineException
  {
    return getSecondarySourceIntegerParamValue(source, fpat, name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null secondary source Integer 
   * parameter with the given name. <P> 
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
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
   *   If no single valued secondary source parameter with the given name exists,
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final int
  getSecondarySourceIntegerParamValue
  (
    String source,
    FilePattern fpat,
    String name, 
    Range<Integer> range
  ) 
    throws PipelineException
  {
    Integer value = (Integer) getSecondarySourceParamValue(source, fpat, name);;
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
   * Get the value of the single valued non-null secondary source Double parameter with 
   * the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value.
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists or 
   *   the value is <CODE>null</CODE>.
   */ 
  public final double
  getSecondarySourceDoubleParamValue
  (
    String source,
    FilePattern fpat,
    String name     
  ) 
    throws PipelineException
  {
    return getSecondarySourceDoubleParamValue(source, fpat, name, null);
  }

  /** 
   * Get the bounds checked value of the single valued non-null secondary source Double 
   * parameter with the given name. <P> 
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
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
   *   If no single valued secondary source parameter with the given name exists, 
   *   the value is <CODE>null</CODE> or is out-of-bounds.
   */ 
  public final double
  getSecondarySourceDoubleParamValue
  (
    String source,
    FilePattern fpat,
    String name,   
    Range<Double> range
  ) 
    throws PipelineException
  {
    Double value = (Double) getSecondarySourceParamValue(source, fpat, name);
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
   * Get the value of the single valued secondary source String parameter with the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @param allowsNull
   *   Whether this parameter can have a null or empty value.
   *   
   * @return 
   *   The action parameter value or (optionally)
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists or 
   *   (optionally) if the value is null or empty.
   */ 
  public final String
  getSecondarySourceStringParamValue
  (
    String source,
    FilePattern fpat,
    String name,
    boolean allowsNull
  ) 
    throws PipelineException
  { 
    String value = (String) getSecondarySourceParamValue(source, fpat, name);
    if((value == null) || (value.length() == 0)) {
      if (!allowsNull)
        throw new PipelineException
          ("Cannot have an empty String value for parameter (" + name + ")");
      value = null;
    }
    return value; 
  }
  
  /** 
   * Get the value of the single valued secondary source String parameter with the given name.
   * 
   * @param source
   *   The name of the source.
   * 
   * @param fpat
   *   The FilePattern of the Secondary Sequence.
   * 
   * @param name  
   *   The name of the parameter. 
   *
   * @return 
   *   The action parameter value or 
   *   <CODE>null</CODE> if the value is null or the empty string. 
   * 
   * @throws PipelineException 
   *   If no single valued secondary source parameter with the given name exists.
   */ 
  public final String
  getSecondarySourceStringParamValue
  (
    String source,
    FilePattern fpat,
    String name
  ) 
    throws PipelineException
  {
    return getSecondarySourceStringParamValue(source, fpat, name, true);
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A T H   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Escape any backslashes in the given file system path string.
   */ 
  public static final String 
  escPath
  (
   String str
  ) 
  {
    return str.replaceAll("\\\\", "\\\\\\\\");
  }

  /**
   * Convert an abstract file system path into an OS specific path string with any
   * backslashes escaped.
   */ 
  public static final String 
  escPath
  (
   Path path 
  ) 
  {
    return escPath(path.toOsString());
  }


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
    if(param == null) 
      throw new PipelineException
        ("There is no Action parameter named (" + pname + ")!");

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
    if(param == null) 
      throw new PipelineException
        ("There is no Action parameter named (" + pname + ")!");

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



