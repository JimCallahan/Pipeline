// $Id: MayaBuildUtils.java,v 1.2 2008/03/06 13:01:35 jim Exp $

package us.temerity.pipeline.plugin;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   B U I L D   U T I L S                                                        */
/*------------------------------------------------------------------------------------------*/


public class MayaBuildUtils
  extends MayaActionUtils
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
  MayaBuildUtils
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
  /*   C O M M O N   P A R A M E T E R S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds a Start Frame Integer param to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Start Frame <BR>
   *   <DIV style="margin-left: 40px;">
   *     The start frame of the generated Maya scene.
   *   </DIV> <BR>
   */
  protected void
  addStartFrameParam()
  {
    ActionParam param = 
      new IntegerActionParam
      (aStartFrame,
       "The start frame of the generated Maya scene.  Accessible as $plStartFrame in " + 
       "optional MEL scripts.", 
       1);
    addSingleParam(param);
  }
  
  /**
   * Adds an End Frame Integer param to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   End Frame <BR>
   *   <DIV style="margin-left: 40px;">
   *     The end frame of the generated Maya scene.
   *   </DIV> <BR>
   */
  protected void
  addEndFrameParam()
  {
    ActionParam param = 
      new IntegerActionParam
      (aEndFrame,
       "The end frame of the generated Maya scene.  Accessible as $plEndFrame in " + 
       "optional MEL scripts.", 
       1);
    addSingleParam(param);
  }

  /**
   * Provide StartFrame and EndFrame parameter values as MEL variables 
   * $plStartFrame and $plEndFrame.
   */ 
  protected String
  genFrameRangeVarsMEL()
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder(); 
    
    buf.append("// ACTION PARAMETERS: StartFrame, EndFrame\n"); 

    Integer startFrame = (Integer) getSingleParamValue(aStartFrame);
    if(startFrame != null) 
      buf.append("float $plStartFrame = " + startFrame + ";\n");
    
    Integer endFrame = (Integer) getSingleParamValue(aEndFrame);
    if(endFrame != null) 
      buf.append("float $plEndFrame = " + endFrame + ";\n"); 

    return buf.toString(); 
  }
        
  /**
   * Set the scene time range based on the StartFrame and EndFrame parameters.
   */
  protected String
  genPlaybackOptionsMEL() 
    throws PipelineException
  {
    StringBuilder buf = new StringBuilder(); 

    buf.append("// TIME RANGE\n");
    
    Integer start = (Integer) getSingleParamValue(aStartFrame);
    if(start != null) {
      buf.append("playbackOptions -e -min " + start + ";\n");
      buf.append("playbackOptions -e -ast " + start + ";\n");
    }
    
    Integer end = (Integer) getSingleParamValue(aEndFrame);
    if(end != null) {
      buf.append("playbackOptions -e -max " + end + ";\n");
      buf.append("playbackOptions -e -aet " + end + ";\n");
    }
    
    buf.append("\n"); 

    return buf.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Adds an Enum Source Parameter called BuildType to the Action.
   * <p>
   * It has the following options:
   * <ul>
   * <li>Import
   * <li>Reference
   * <li>Proxy
   * </ul>
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Build Type<BR>
   *   <DIV style="margin-left: 40px;">
   *     The method Maya should use to merge the data from the source scene into the
   *     generated scene.
   *   </DIV> <BR>
   */
  protected void
  addBuildTypeSourceParam
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ArrayList<String> choices = new ArrayList<String>();
    for (BuildType btype : BuildType.values())
      choices.add(btype.toString());

    ActionParam param = 
      new EnumActionParam
      (aBuildType,
       "The method Maya should use to merge the data from the source scene into the " + 
       "generated scene.", 
       BuildType.Reference.toString(), choices);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds an Enum Source Parameter called BuildType to the Action.
   * <p>
   * It has the following options:
   * <ul>
   * <li>Import
   * <li>Reference
   * </ul>
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Build Type<BR>
   *   <DIV style="margin-left: 40px;">
   *     The method Maya should use to merge the data from the source scene into the
   *     generated scene.
   *   </DIV> <BR>
   */
  protected void
  addOldBuildTypeSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ArrayList<String> choices = new ArrayList<String>();
    choices.add(aImport);
    choices.add(aReference);

    ActionParam param = 
      new EnumActionParam
      (aBuildType,
       "The method Maya should use to merge the data from the source scene into the " + 
       "generated scene.", 
       aReference, choices);
    params.put(param.getName(), param);
  }
  

  /**
   * Adds an Enum Source Parameter called SceneType to the Action.
   * <p>
   * It has the following options:
   * <ul>
   * <li>Model
   * <li>Animation
   * </ul>
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Scene Type<BR>
   *   <DIV style="margin-left: 40px;">
   *     Is this a model scene or an animation scene.
   *   </DIV> <BR>
   */
  protected void
  addSceneTypeSourceParam
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ArrayList<String> choices = new ArrayList<String>();
    for (SceneType stype : SceneType.values())
      choices.add(stype.toString());

    ActionParam param = 
      new EnumActionParam
      (aSceneType,
       "Is this an animation or a model scene?", 
       SceneType.Model.toString(), choices);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds a Boolean Source Parameter called NameSpace to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *  Name Space<BR>
   *  <DIV style="margin-left: 40px;">
   *    Whether Maya should create a namespace for the imported/referenced scene.
   *    This option is highly recommended to avoid name clashes.
   * </DIV> <BR>
   */
  protected void
  addNamespaceSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new BooleanActionParam
      (aNameSpace,
       "Whether Maya should create a namespace for the imported/referenced scene.  " + 
       "This option is highly recommended to avoid name clashes.",
       true);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds a String Source Parameter called Prefix Name to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Prefix Name <BR>
   *   <DIV style="margin-left: 40px;">
   *     The namespace prefix to use for the imported/referenced Maya scene inside the 
   *     generated Maya scene.  If unset, the namespace is based on the filename.
   *   </DIV> <BR>
   */
  protected void
  addPrefixNameSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new StringActionParam
      (aPrefixName,
       "The namespace prefix to use for the imported/referenced Maya scene inside the " +
       "generated Maya scene.  If unset, the namespace is based on the filename.",
       null);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds a String Source Parameter called Proxy Name to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Proxy Name <BR>
   *   <DIV style="margin-left: 40px;">
   *     The proxy name to be used for the referenced Maya scene.  If Build Type is set
   *     to Reference, this will be the proxy tag for the reference.  If Build Type is set
   *     to Import than this field will be ignored.
   *   </DIV> <BR>
   */
  protected void
  addProxyNameSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new StringActionParam
      (aProxyName,
       "This parameter will set the Maya Proxy name for this proxy level.",
       null);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds an Integer Source Parameter called Num Instances to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Num Instances<BR>
   *   <DIV style="margin-left: 40px;">
   *     The number of instances of a model that should be created.  If this is set
   *     to zero, then one instance will still be created, but it will not have
   *     any numbering in the namespace.  For example, if the Prefix Name is 'rock',
   *     setting Num Instances to '0' will result in a namespace of 'rock'.  Setting
   *     it to '1' will result in rock-0001
   *   </DIV> <BR>
   */
  protected void
  addNumInstancesSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new IntegerActionParam
      (aNumInstances,
       "The number of instances of a model that should be created.  If this is set " + 
       "to zero, then one instance will still be created, but it will not have " + 
       "any numbering in the namespace.", 
       0);
    params.put(param.getName(), param);
  }
  
  /**
   * Adds an Integer Source Parameter called Instance Start to the Action.
   * <p>
   * <DIV style="margin-left: 40px;">
   *   Instance Start<BR>
   *   <DIV style="margin-left: 40px;">
   *     The number for the first instance of a reference.  This allows numbered 
   *     instances to start somewhere besides zero.
   *   </DIV> <BR>
   */
  protected void
  addInstanceStartSourceParam  
  (
    TreeMap<String,ActionParam> params  
  )
  {
    ActionParam param = 
      new IntegerActionParam
      (aInstanceStart,
       "The number for the first instance of a reference.  This allows numbered " + 
       "instances to start somewhere besides zero.", 
       0);
    params.put(param.getName(), param);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M M O N   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Scan all the sources that contain source parameters and populate the shared data 
   * structures.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   *   
   * @throws PipelineException
   *   If some of the settings on the action are incorrect.
   */
  protected void
  scanAllSources
  (
    ActionAgenda agenda  
  )
    throws PipelineException
  {
    pAnimDataByActualPrefix = new TreeMap<String, MayaBuildData>();
    pModelDataByActualPrefix = new TreeMap<String, MayaBuildData>();
    pProxyDataByActualPrefix = new MappedLinkedList<String, MayaBuildData>();
    
    for (String sname : agenda.getSourceNames()) {
      if (hasSourceParams(sname)) {
        for (MayaBuildData data : scanSource(agenda, sname, null)) {
          String actualPrefix = data.getActualPrefix();
          SceneType sceneType = data.getSceneType();
          BuildType buildType = data.getBuildType();

          if (sceneType == SceneType.Model) {
            if (buildType != BuildType.Proxy) {
              if (pModelDataByActualPrefix.containsKey(actualPrefix))
                throw new PipelineException
                 ("More than one model source has an actual prefix of " +
                  "(" + actualPrefix + ")");
              pModelDataByActualPrefix.put(actualPrefix, data);
            }
            else
              pProxyDataByActualPrefix.put(actualPrefix, data);
          }
          else if (sceneType == SceneType.Animation) 
            pAnimDataByActualPrefix.put(actualPrefix, data);
        }
      }
      
      Set<FilePattern> secPats = getSecondarySequences(sname);
      for (FilePattern pat : secPats) {
        if (hasSecondarySourceParams(sname, pat)) {
          for (MayaBuildData data : scanSource(agenda, sname, pat)) {
            String actualPrefix = data.getActualPrefix();
            SceneType sceneType = data.getSceneType();
            BuildType buildType = data.getBuildType();

            if (sceneType == SceneType.Model) {
              if (buildType != BuildType.Proxy) {
                if (pModelDataByActualPrefix.containsKey(actualPrefix))
                  throw new PipelineException
                   ("More than one model source has an actual prefix of " +
                    "(" + actualPrefix + ")");
                pModelDataByActualPrefix.put(actualPrefix, data);
              }
              else
                pProxyDataByActualPrefix.put(actualPrefix, data);
            }
            else if (sceneType == SceneType.Animation) 
              pAnimDataByActualPrefix.put(actualPrefix, data);
          }
        }
      }
    }
    
    for (String prefix : pProxyDataByActualPrefix.keySet()) {
      MayaBuildData modelData = pModelDataByActualPrefix.get(prefix); 
      if (modelData == null)
        throw new PipelineException
          ("There is a proxy specified for prefix (" + prefix +") but there is no model " +
           "with the same prefix");
      String proxyName = modelData.getProxyName();
      if (proxyName == null)
        throw new PipelineException
          ("The model with the prefix (" + prefix + ") has proxies, but does not define " +
           "a proxy name.");
      TreeSet<String> proxyNames = new TreeSet<String>();
      proxyNames.add(proxyName);
      for (MayaBuildData proxyData : pProxyDataByActualPrefix.get(prefix)) {
        String proxName = proxyData.getProxyName();
        if (proxName == null)
        throw new PipelineException
          ("One of the proxies for (" + prefix + ") does not define a proxy name.");
        if (proxyNames.contains(proxName))
          throw new PipelineException
            ("More than one proxy for prefix (" + prefix + ") has a proxy name of " +
             "(" + proxName + ")");
        proxyNames.add(proxName);
      }
    }
    
    for (String prefix : pAnimDataByActualPrefix.keySet()) {
      String trimmedPrefix = prefix.substring(0, prefix.length() - 2); 
      if (pModelDataByActualPrefix.get(trimmedPrefix) == null) {
        throw new PipelineException
          ("Animation data exists for prefix (" + trimmedPrefix + ") but there is no " +
           "corresponding model file");
      }
    }
  }

  
  /**
   * Scan a single source (either the primary or a secondary sequence) and return the list of 
   * {@link MayaBuildData MayaBuildDatas} containing all the information about that 
   * source. <p>
   * 
   * In most cases, the List will only contain a single entry.  In cases where the 
   * numInstances of the sources is greater than one, there will be an entry in the List for 
   * each instance.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param sname
   *   The name of the source node.
   *   
   * @param fpat
   *   The file pattern if this is a secondary sequence of a source or <code>null</code> if 
   *   this is the primary source.
   *   
   * @return
   *   The list of data.
   *   
   * @throws PipelineException
   *   If the source specified is not a valid maya file or if the source does not have source
   *   parameters.
   */
  protected List<MayaBuildData>
  scanSource
  (
    ActionAgenda agenda,
    String sname,
    FilePattern fpat
  )
    throws PipelineException
  {
    List<String> sourceParamNames = getSourceLayout();
    
    LinkedList<MayaBuildData> toReturn = new LinkedList<MayaBuildData>();
    
    FilePattern pat = fpat;
    
    FileSeq primarySeq = agenda.getPrimarySource(sname); 
    if (pat == null)
      pat = primarySeq.getFilePattern();
    String suffix = pat.getSuffix();
    if (primarySeq.isSingle() && (suffix != null)) {
      if (suffix.equals("ma") || suffix.equals("mb")) {
        
        if (fpat == null) {
          if (!hasSourceParams(sname))
            throw new PipelineException
              ("The source (" + sname + ") does not have any source parameters");
        }
        else
          if (!hasSecondarySourceParams(sname, fpat))
            throw new PipelineException
              ("The secondary sequence (" + fpat + ") of source (" + sname +") does not have " +
               "any source parameters");
        
        Path mpath = new Path(sname).getParentPath();
        if (fpat == null)
          mpath = new Path(mpath, primarySeq.getPath(0));
        else
          mpath = new Path(mpath, fpat.getPath());

        String prefixName;
        if (fpat == null)
          prefixName = getSourceStringParamValue(sname, aPrefixName);
        else
          prefixName = getSecondarySourceStringParamValue(sname, fpat, aPrefixName);
        
        String proxyName = null;
        if (sourceParamNames.contains(aProxyName)) {
          if (fpat == null)
            proxyName = getSourceStringParamValue(sname, aProxyName);
          else
            proxyName = getSecondarySourceStringParamValue(sname, fpat, aProxyName);
        }
        
        int idx;
        
        BuildType buildType;
        if (fpat == null)
          idx = getSourceEnumParamIndex(sname, aBuildType);
        else
          idx = getSecondarySourceEnumParamIndex(sname, fpat, aBuildType);
        buildType = BuildType.values()[idx];
        
        SceneType sceneType = SceneType.Model;
        if (sourceParamNames.contains(aSceneType)) {
          if (fpat == null)
            idx = getSourceEnumParamIndex(sname, aSceneType);
          else
            idx = getSecondarySourceEnumParamIndex(sname, fpat, aSceneType);
          sceneType = SceneType.values()[idx];
        }
        
        boolean nameSpace;
        if (fpat == null)
          nameSpace = getSourceBooleanParamValue(sname, aNameSpace);
        else
          nameSpace = getSecondarySourceBooleanParamValue(sname, fpat, aNameSpace);
        
        int instanceStart = -1;
        int numInstances = -1;
        if (sourceParamNames.contains(aInstanceStart)) {
          int iStart;
          if (fpat == null)
            iStart = 
              getSourceIntegerParamValue(sname, aInstanceStart, new Range<Integer>(0, null));
          else
            iStart = 
              getSecondarySourceIntegerParamValue(sname, fpat, aInstanceStart, 
                                                  new Range<Integer>(0, null));
          int nInstance;
          if (fpat == null)
            nInstance =
              getSourceIntegerParamValue(sname, aNumInstances, new Range<Integer>(0, null));
          else
            nInstance =
              getSecondarySourceIntegerParamValue(sname, fpat, aNumInstances, 
                                                  new Range<Integer>(0, null));
          
          if (nInstance != 0) {
            instanceStart = iStart;
            numInstances = nInstance;
          }
        }
        
        if (numInstances == -1) {
          MayaBuildData data = 
            new MayaBuildData
              (sname, mpath, prefixName, proxyName, nameSpace, sceneType, buildType, -1);
          toReturn.add(data);
        }
        else {
          for (int each = 0; each < numInstances; each++) {
            int current = instanceStart + each;
            MayaBuildData data =
              new MayaBuildData
                (sname, mpath, prefixName, proxyName, nameSpace, sceneType, 
                 buildType, current);
            toReturn.add(data);
          }
        }
      }
      else
        throw new PipelineException
          ("The source (" + sname + "), secondary sequence (" + fpat +"), is not a valid " +
           "maya scene.");
    }
    else
      throw new PipelineException
        ("The source (" + sname + "), secondary sequence (" + fpat +"), is not a valid " +
         "maya scene.");
  
    return toReturn;
  }
  
  /**
   * Write the shared functions that the animation linking scripts use.
   * 
   * @param out
   *   The file writer for the MEL script.
   * 
   * @throws IOException
   *   If there is a problem writing the MEL script. 
   */
  protected void
  writeAnimLinkingHeader
  (
    FileWriter out  
  ) 
    throws IOException
  {
    out.write(
      "global proc string removePrefix(string $name)\n" + 
      "{\n" + 
      "  string $toReturn;\n" + 
      "  string $buffer[];\n" + 
      "  tokenize($name, \"|\", $buffer);\n" + 
      "  string $part;\n" + 
      "  for ($part in $buffer) {\n" + 
      "    string $buffer2[];\n" + 
      "    tokenize($part, \":\", $buffer2);\n" + 
      "    if ($toReturn == \"\")\n" + 
      "      $toReturn += $buffer2[(size($buffer2) -1)];\n" + 
      "    else\n" + 
      "      $toReturn += \"|\" + $buffer2[(size($buffer2) -1)];\n" + 
      "  }\n" + 
      "  return $toReturn;    \n" + 
      "}\n");

    out.write(
      "global proc string addPrefix(string $name, string $prefix)\n" + 
      "{\n" + 
      "  string $toReturn;\n" + 
      "  string $buffer[];\n" + 
      "  tokenize($name, \"|\", $buffer);\n" + 
      "  string $part;\n" + 
      "  for ($part in $buffer) {\n" + 
      "    if ($toReturn == \"\")\n" + 
      "      $toReturn += $prefix + $buffer[(size($buffer) -1)];\n" + 
      "    else\n" + 
      "      $toReturn += \"|\" + $prefix + $buffer[(size($buffer) -1)];\n" + 
      "  }\n" + 
      "  return $toReturn;\n" + 
      "}\n");
  }

  /**
   * Write the MEL script that will link the animation curves from one data file to the 
   * controls from another data file. <p>
   * 
   * This code is correctly bracketed to prevent any namespace collision with other code.
   * 
   * @param out
   *   The file writer for the MEL script.
   *   
   * @param ignoreMissing
   *   Should curves that do not have a corresponding channel be skipped?  If this is set
   *   to false, a curve that is missing a channel will result in an error being thrown.
   *   
   * @param animData
   *   The data file containing the animation curves.
   * 
   * @param modelData
   *   The data file containing the controls.
   *   
   * @throws IOException
   *   If there is a problem writing to the MEL script
   *   
   * @throws PipelineException
   *   If there are any problems figuring out what should go into the MEL script.
   */
  public void
  writeAnimLinkingMEL
  (
    FileWriter out,
    boolean ignoreMissing,
    MayaBuildData animData,
    MayaBuildData modelData
  )
    throws PipelineException, IOException
  {
    String curvePrefix = animData.getAnimationPrefix();
    String modelPrefix = modelData.getAnimationPrefix();
    
    out.write
    ("{\n" +
     "  int $ignoreMissing = "  + (ignoreMissing ? 1 : 0) + ";\n" + 
     "  string $crvPrefix = \"" + curvePrefix + "\";\n" + 
     "  string $objPrefix = \"" + modelPrefix + "\";\n" +
     "  string $obj = $crvPrefix + \"curveInfo\";\n" + 
     "  if (`objExists $obj`) {\n" + 
     "    int $size = `getAttr -s ($obj + \".cn\")`;\n" + 
     "    int $i = 0;\n" + 
     "    for ($i = 0; $i < $size; $i++) {\n" + 
     "      string $curve = `getAttr  ($obj + \".cn[\" + $i + \"]\")`;\n" + 
     "      string $attr = `getAttr  ($obj + \".an[\" + $i + \"]\")`;\n" + 
     "      $curve =  addPrefixToName($crvPrefix, $curve);\n" + 
     "      $attr  =  addPrefixToName($objPrefix, $attr);\n" + 
     "      string $pieces[];\n" + 
     "      tokenize($attr, \".\", $pieces);\n" + 
     "      if (!attributeExists($pieces[1], $pieces[0])) {\n" + 
     "        if ($ignoreMissing)\n" + 
     "          print(\"Ignoring curve (\" + $curve + \") because attr " +
     "(\" + $attr + \") does not exist.\\n\");\n" + 
     "        else { \n" + 
     "          error(\"Error with curve (\" + $curve + \") because attr " +
     "(\" + $attr + \") does not exist.\");\n" +
     "          quit -f -ec 35;\n" +
     "             }\n" + 
     "      }" +
     "      else {\n" +
     "        print(\"connectAttr -f \" + $curve + \" \" + $attr + \"\\n\");\n" + 
     "        connectAttr -f $curve $attr;   \n" +
     "      }\n" + 
     "    }\n" + 
     "  }\n" + 
     "  else {\n" + 
     "    string $curves[] = `ls -type animCurve ($crvPrefix + \"*\")`;\n" + 
     "    string $curve;\n" + 
     "    for ($curve in $curves) {\n" + 
     "      string $shortName = removePrefix($curve);\n" + 
     "      string $buffer[];\n" + 
     "      tokenize($shortName, \"_\", $buffer);\n" + 
     "      string $attr = $buffer[(size($buffer) -1)];\n" + 
     "      string $name = \"\";\n" + 
     "      int $length = (size($buffer) -1);\n" + 
     "      int $i;\n" + 
     "      for ($i = 0; $i < $length; $i++) {\n" + 
     "        $name += $buffer[$i];\n" + 
     "        if ($i != ($length - 1) )\n" + 
     "          $name += \"_\";\n" + 
     "      }\n" + 
     "      string $channel = addPrefix($name, $objPrefix) + \".\" + $attr;\n" + 
     "      print ($curve + \"\\t\" + $channel + \"\\n\");\n" + 
     "      if (!`getAttr -l $channel`)\n" + 
     "        connectAttr -f ($curve + \".output\") $channel;\n" + 
     "    }\n" +
     "  }\n" +
     "}\n");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the animation data indexed by the resolved prefix name. 
   */
  public TreeMap<String, MayaBuildData> 
  getAnimDataByActualPrefix()
  {
    return pAnimDataByActualPrefix;
  }
  
  /**
   * Get the model data indexed by the resolved prefix name.
   */
  public TreeMap<String,MayaBuildData> 
  getModelDataByActualPrefix()
  {
    return pModelDataByActualPrefix;
  }

  
  /**
   * Get the proxy data indexed by the resolved prefix name.
   */
  public MappedLinkedList<String, MayaBuildData>
  getProxyDataByActualPrefix()
  {
    return pProxyDataByActualPrefix;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a four digit padding string for a reference number.
   */ 
  private String 
  pad
  (
    int num
  )
    throws PipelineException
  {
    String parse = String.valueOf(num);
    int length = parse.length();
    if (length > 4)
      throw new PipelineException("Cannot create instance numbers over 9999");
    for (int i = length; i < 4; i++) 
      parse = "0" + parse;
    return parse;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S                                                          */
  /*----------------------------------------------------------------------------------------*/

  protected
  class MayaBuildData
  {
    public
    MayaBuildData
    (
      String nodeName,
      Path scenePath,
      String prefixName,
      String proxyName,
      boolean nameSpace,
      SceneType sceneType,
      BuildType buildType,
      int instanceNumber
    )
      throws PipelineException
    {
      if (nodeName == null)
        throw new PipelineException("The node name cannot be null.");
      if (scenePath == null)
        throw new PipelineException("The scene path cannot be null.");
      if (sceneType == null)
        throw new PipelineException("The scene type cannot be null.");
      if (buildType == null)
        throw new PipelineException("The build type cannot be null");
      if (instanceNumber < -1)
        throw new PipelineException
          ("The number of instances must be -1 or greater.");
      if (instanceNumber > 9999)
        throw new PipelineException("The number of instances cannot be greater than 9999");
      
      if (buildType == BuildType.Proxy && proxyName == null)
        throw new PipelineException("Must specify a ProxyName when the BuildType is Proxy.");
      if (!(buildType == BuildType.Proxy || buildType == BuildType.Reference) && proxyName != null)
        throw new PipelineException
          ("Cannot specify a ProxyName when the BuildType is not Proxy or Reference.");
      
      pNodeName   = nodeName;
      pScenePath  = scenePath;
      pPrefixName = prefixName;
      pProxyName  = proxyName;
      pNamespace  = nameSpace;
      pSceneType  = sceneType;
      pBuildType  = buildType;
       
      pInstanceNumber = instanceNumber;
      
      /* Do some validation */
      if (!pNamespace && pBuildType == BuildType.Reference && pSceneType != SceneType.Model )
        throw new PipelineException
          ("The default namespace can only be used for models, not animation");
      
      if (pBuildType == BuildType.Proxy && pSceneType != SceneType.Model)
        throw new PipelineException
          ("The BuildType of Proxy is not valid with a SceneType that is not Model");
    }

    /**
     * Write the MEL script that will build this particular data. <p>
     * 
     * Care must be taken when adding proxies that they are added immediately after the node 
     * that they are proxying has been referenced.  Failure to do things in the correct order
     * will result in the proxies not corresponding with the correct nodes. <p>
     * 
     * Also note that this code does not wrap curly braces around the code, meaning that two
     * back-to-back calls to this method will result in MEL script errors.  This is to allow
     * for proxies to correctly find their parents, but the following coding conventions 
     * should be observed.<br>
     * <code>
     * out.write("{");
     * pReferenceData.writeBuildMEL(out, false);
     * pProxy1Data.writeBuildMEL(out, false);
     * pProxy2Data.writeBuildMEL(out, false);
     * out.write("}");
     * out.write("{");
     * pReference2Data.writeBuildMEL(out, false);
     * out.write("}");
     * </code><br>
     *
     * @param out
     *   Where to write the MEL script.
     *   
     * @param deferReferences
     *   Whether to defer the references.  If this is used with animation being imported or 
     *   proxies, the action may well fail.
     *   
     * @throws IOException
     *   If there is a problem writing to the MEL script
     *   
     * @throws PipelineException
     *   If there are any problems figuring out what should go into the MEL script.
     */
    public void
    writeBuildMEL
    (
      FileWriter out,
      boolean deferReferences
    ) 
      throws IOException, PipelineException
    {
      String prefix = getActualPrefix();
      
      switch (pBuildType) {
      case Import:
      case Reference:
        out.write
          ("// BUILDING: " + pNodeName+ "\n" + 
           "print \"" + pBuildType + " " + pSceneType + ":" + pScenePath + "\\n\";\n" +
           "  string $actualFile = `file\n");
        if (deferReferences)
          out.write
            ("    -loadReferenceDepth \"none\"\n");
        
        switch (pBuildType) {
        case Import:
          out.write("    -import\n");
          if(pNamespace) 
            out.write("    -namespace \"" + prefix + "\"\n");
          break;
          
        case Reference:
          out.write("    -reference\n");
          if(pNamespace)
            out.write("    -namespace \"" + prefix + "\"\n");
          else
            out.write("    -defaultNamespace\n");        
          break;
        default:
          throw new PipelineException("Unknown BuildType (" + pBuildType + ")");
        }
        
        {
          String fname = pScenePath.getName();
          if(fname.endsWith("ma")) 
            out.write("    -type \"mayaAscii\"\n");
          else if(fname.endsWith("mb")) 
            out.write("    -type \"mayaBinary\"\n");
          else 
            throw new PipelineException
              ("Unknown Maya scene format for source file (" + pScenePath+ ")!");
        }
  
        out.write
        ("    -options \"v=0\"\n" + 
         "    \"$WORKING" + pScenePath + "\"`;\n");
        

        if (pProxyName != null) {
          out.write
            ("  string $refNode = `file -q -rfn $actualFile`;\n");
          out.write
            ("  if( isValidReference( $refNode ) )\n" + 
             "    setAttr ($refNode + \".proxyTag\") -type \"string\" " + 
             "\"" + pProxyName + "\";\n");
        }
        break;
      
      case Proxy:
        String finalPath = "$WORKING" + pScenePath;
        out.write("  optionVar -intValue proxyOptionsSharedEdits true;\n");
        out.write("  proxyAdd $refNode \"" + finalPath + "\" \"" + pProxyName+"\";\n");
        
        break;
      }
    }
    
    /**
     * Get the prefix name that was set on the source. <p>
     * 
     * This is not the same as the final prefix name, which can be gotten with the 
     * {@link #getActualPrefix()} method.
     */
    public final String
    getPrefixName()
    {
      return pPrefixName;
    }

    /**
     * Get the Build Type.
     */
    public final BuildType
    getBuildType()
    {
      return pBuildType;
    }

    /**
     * Get the proxy name.
     */
    public String 
    getProxyName()
    {
      return pProxyName;
    }

    /**
     * Get the Scene Type.
     */
    public SceneType 
    getSceneType()
    {
      return pSceneType;
    }
    
    public final String
    getActualPrefix()
      throws PipelineException
    {
      /* Get the default namespace out of the way. */
      if (!pNamespace && pBuildType == BuildType.Reference)
        return null;
      StringBuffer toReturn = new StringBuffer();
      if (pPrefixName == null)
        toReturn.append(new Path(pNodeName).getName());
      else
        toReturn.append(pPrefixName);
      if (pInstanceNumber != -1)
        toReturn.append(pad(pInstanceNumber));
      if (pSceneType == SceneType.Animation)
        toReturn.append("_a");
      return toReturn.toString();
    }
    
    private String
    getAnimationPrefix()
      throws PipelineException
    {
      String toReturn = "";
      
      String actualPrefix = getActualPrefix();
      if (pNamespace) {
        toReturn = actualPrefix + ":";
      }
      else {
        if (pBuildType.equals("Reference"))
          toReturn = actualPrefix + "_";
      }
      
      return toReturn;
    }
    
    
    private final String    pNodeName;
    private final Path      pScenePath;
    private final String    pPrefixName;
    private final String    pProxyName;
    private final SceneType pSceneType;
    private final BuildType pBuildType;
    private final boolean   pNamespace;
    private final int       pInstanceNumber;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The type of scene that is being built.
   */
  protected
  enum SceneType
  {
    Model, Animation
  }

  /**
   * How the scene is being included in the newly built scene.
   */
  protected
  enum BuildType
  {
    Reference, Import, Proxy
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 3431517306706257L;
  
  public static final String aBuildType       = "BuildType";
  public static final String aSceneType       = "SceneType";
  public static final String aNameSpace       = "NameSpace";
  public static final String aPrefixName      = "PrefixName";
  public static final String aProxyName       = "ProxyName";
  public static final String aStartFrame      = "StartFrame";
  public static final String aEndFrame        = "EndFrame";
  public static final String aDeferReferences = "DeferReferences";
  public static final String aNumInstances    = "NumInstances";
  public static final String aInstanceStart   = "InstanceStart";
  
  public static final String aProxy           = "Proxy";
  public static final String aImport          = "Import";
  public static final String aReference       = "Reference";
  
  public static final String aModel           = "Model";
  public static final String aAnimation       = "Animation";
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  protected TreeMap<String, MayaBuildData> pModelDataByActualPrefix;
  protected TreeMap<String, MayaBuildData> pAnimDataByActualPrefix;
  protected MappedLinkedList<String, MayaBuildData> pProxyDataByActualPrefix;
}
