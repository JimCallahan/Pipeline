// $Id: MRayInstGroupAction.java,v 1.1 2007/06/28 19:52:05 jesse Exp $

package us.temerity.pipeline.plugin.MRayInstGroupAction.v2_3_2;

import java.io.*;
import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.PythonActionUtils;


/*------------------------------------------------------------------------------------------*/
/*   M R A Y   I N S T   G R O U P   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds MentalRay "instgroups" statements for instances in source MI files. <P>
 * 
 * This action scans each source MI files for "instance" statements and then generates 
 * a target MI file which includes the source MI file and contains an "instgroups" statement 
 * for all found instances.  <P> 
 * 
 * By default, the "python" program is used by this action to parse the MI files.
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Instance List<BR>
 *   <DIV style="margin-left: 40px;">
 *     A List of instances to either include or exclude based on the File Type parameter.
 *   </DIV> <BR>
 *   
 *   List Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether the list of instances should be the only instances Included or the instances
 *     that are Excluded from the render instance.
 *   </DIV> <BR>
 * 
 *   Visible<BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to override the (visible) attribute for these instances.  Select None
 *     for no override or the value you want the override to have.
 *   </DIV> <BR>
 * 
 *   Shadow <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to override the (shadow) attribute for these instances.  Select None
 *     for no override or the value you want the override to have. 
 *   </DIV> <BR>
 *   
 *   Shadow Map <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to override the (shadowmap) attribute for these instances.  Select None
 *     for no override or the value you want the override to have. 
 *   </DIV> <BR>
 *   
 *   Shadow Mode <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to override the (shadowmode) attribute for these instances.  Select None
 *     for no override or the value you want the override to have. 
 *   </DIV> <BR>
 *   
 *   Trace <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to override the (trace) attribute for these instances.  Select None
 *     for no override or the value you want the override to have. 
 *   </DIV> <BR>
 *   
 *   Face <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to override the (face) attribute for these instances.  Select None
 *     for no override or the value you want the override to have. 
 *   </DIV> <BR>
 *   
 *   Visible In Reflect <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects visible or invisible in reflections. 
 *   </DIV> <BR>
 *   
 *   Has Reflections <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects have or not have reflections. 
 *   </DIV> <BR>
 *   
 *   Visible In Refract <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects visible or invisible in refractions. 
 *   </DIV> <BR>
 *   
 *   Has Refractions <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects have or not have refractions. 
 *   </DIV> <BR>
 *   
 *   Visible In Transp <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects visible or invisible in transparency. 
 *   </DIV> <BR>
 *   
 *   Has Transparency <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects have or not have transparency. 
 *   </DIV> <BR>
 *   
 *   Visible In FinalG <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects visible or invisible to Final Gather rays. 
 *   </DIV> <BR>
 *   
 *   Has Final Gather <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do you want to make these objects use or not use Final Gather. 
 *   </DIV> <BR>
 *   
 *   Cast Caustics <BR>
 *   <DIV style="margin-left: 40px;">
 *     Note that casting here means "cast a reflection" (that is, become visible in 
 *     reflecting objects). This is the opposite sense of casting a ray or photon. 
 *   </DIV> <BR>
 *   
 *   Receives Caustics <BR>
 *   <DIV style="margin-left: 40px;">
 *     A material shader attached to an object can send out caustic photons only if the 
 *     object has the caustic receive flag set (or inherits it). 
 *   </DIV> <BR>
 *   
 *   Caustics Interact <BR>
 *   <DIV style="margin-left: 40px;">
 *     Controls "invisibility to photons", that is, if visibility is disabled photons 
 *     do not intersect this object and fly right through. This also affects the 
 *     portion of the scene where photons have an effect and will be traced by mental ray. 
 *   </DIV> <BR>
 *   
 *   Cast GI <BR>
 *   <DIV style="margin-left: 40px;">
 *     Note that casting here means "cast a reflection" (that is, become visible in 
 *     reflecting objects). This is the opposite sense of casting a ray or photon. 
 *   </DIV> <BR>
 *   
 *   Receives GI <BR>
 *   <DIV style="margin-left: 40px;">
 *     A material shader attached to an object can send out GI photons only if the 
 *     object has the caustic receive flag set (or inherits it). 
 *   </DIV> <BR>
 *   
 *   GI Interact <BR>
 *   <DIV style="margin-left: 40px;">
 *     Controls "invisibility to photons", that is, if visibility is disabled photons 
 *     do not intersect this object and fly right through. This also affects the 
 *     portion of the scene where photons have an effect and will be traced by mental ray. 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public class 
MRayInstGroupAction
  extends PythonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MRayInstGroupAction()
  {
    super("MRayInstGroup", new VersionID("2.3.2"), "Temerity",
	  "Builds MentalRay \"instgroups\" statements for instances in source MI files.");

    {
      ActionParam param = 
	new LinkActionParam
	(aInstanceList,
	 "A List of instances to either include or exclude based on the " +
	 "File Type parameter.",
	 null);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Include");
      choices.add("Exclude");
      
      ActionParam param = 
	new EnumActionParam
	(aListType,
	 "Whether the list of instances should be the only instances Included or the instances that are Excluded from the render instance.",
	 "Include",
	 choices);
      addSingleParam(param);
    }

    
    ArrayList<String> threeChoices = new ArrayList<String>();
    threeChoices.add("None");
    threeChoices.add("On");
    threeChoices.add("Off");
    
    {
      ActionParam param = 
	new EnumActionParam
	(aVisible,
	 "Do you want to override the (visible) attribute for these instances.  Select None" +
	 "for no override or the value you want the override to have.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aShadowMap,
	 "Do you want to override the (shadowmap) attribute for these instances.  Select None" +
	 "for no override or the value you want the override to have.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aTrace,
	 "Do you want to override the (trace) attribute for these instances.  Select None" +
	 "for no override or the value you want the override to have.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aShadow,
	 "Do you want to override the (shadow) attribute for these instances.  Select None" +
	 "for no override or the value you want the override to have.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("Front");
      choices.add("Back");
      choices.add("All");
      
      ActionParam param = 
	new EnumActionParam
	(aFace,
	 "Do you want to override the (face) attribute for these instances.  Select None" +
	 "for no override or the value you want the override to have.",
	 "None",
	 choices);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("Cast");
      choices.add("Receieve");
      choices.add("Both");
      
      ActionParam param = 
	new EnumActionParam
	(aShadowMode,
	 "Do you want to override the (shadow) attribute for these instances.  Select None" +
	 "for no override or the value you want the override to have.",
	 "None",
	 choices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aVisibleInReflect,
	 "Do you want to make these objects visible or invisible in reflections.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aHasReflections,
	 "Do you want to make these objects have or not have reflections.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aVisibleInRefract,
	 "Do you want to make these objects visible or invisible in refractions.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aHasRefractions,
	 "Do you want to make these objects have or not have refractions.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aVisibleInTransp,
	 "Do you want to make these objects visible or invisible through transparency.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aHasTransparency,
	 "Do you want to make these objects have or not have transparency.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aVisibleInFinalG,
	 "Do you want to make these objects contribute or not contribute to final gathering.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aHasFinalGather,
	 "Do you want to make these objects have or not have final gathering.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aCastCaustics,
	 "Note that casting here means \"cast a reflection\" " +
	 "(that is, become visible in reflecting objects). " +
	 "This is the opposite sense of \"casting a ray\".",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aReceiveCaustics,
	 "A material shader attached to an object can send out caustic rays only " +
	 "if the object has the caustic receive flag set (or inherits it).",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aCausticInteract,
	 "Controls \"invisibility to photons\", that is, if visibility is " +
	 "disabled photons do not intersect this object and fly right through. " +
	 "This also affects the portion of the scene where photons have an effect " +
	 "and will be traced by mental ray.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aCastGI,
	 "Note that casting here means \"cast a reflection\" " +
	 "(that is, become visible in reflecting objects). " +
	 "This is the opposite sense of \"casting a ray\".",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aReceiveGI,
	 "A material shader attached to an object can send out gi photons only " +
	 "if the object has the gi receive flag set (or inherits it).",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new EnumActionParam
	(aGIInteract,
	 "Controls \"invisibility to photons\", that is, if visibility is " +
	 "disabled photons do not intersect this object and fly right through. " +
	 "This also affects the portion of the scene where photons have an effect " +
	 "and will be traced by mental ray.",
	 "None",
	 threeChoices);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aInstanceList);
      layout.addEntry(aListType);
      layout.addSeparator();
      layout.addEntry(aVisible);
      layout.addEntry(aShadow);
      layout.addEntry(aShadowMode);
      layout.addEntry(aShadowMap);
      layout.addEntry(aTrace);
      layout.addEntry(aFace);
      {
	LayoutGroup sub = 
	  new LayoutGroup("Reflect/Refract", "Settings for Reflection and Refraction", true);
	sub.addEntry(aVisibleInReflect);
	sub.addEntry(aHasReflections);
	sub.addSeparator();
	sub.addEntry(aVisibleInRefract);
	sub.addEntry(aHasRefractions);
	layout.addSubGroup(sub);
      }
      {
	LayoutGroup sub = 
	  new LayoutGroup("Transparancy", "Settings for Transparancy", true);
	sub.addEntry(aVisibleInTransp);
	sub.addEntry(aHasTransparency);
	layout.addSubGroup(sub);
      }
      {
	LayoutGroup sub = 
	  new LayoutGroup("Final Gather", "Settings for Final Gathering", true);
	sub.addEntry(aVisibleInFinalG);
	sub.addEntry(aHasFinalGather);
	layout.addSubGroup(sub);
      }
      {
	LayoutGroup sub = 
	  new LayoutGroup("Caustics and GI", "Settings for Caustics and Global Illumination", true);
	sub.addEntry(aCastCaustics);
	sub.addEntry(aReceiveCaustics);
	sub.addEntry(aCausticInteract);
	sub.addSeparator();
	sub.addEntry(aCastGI);
	sub.addEntry(aReceiveGI);
	sub.addEntry(aGIInteract);
	layout.addSubGroup(sub);
      }
      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    /* the MIs to export */ 
    ArrayList<Path> targetPaths = 
      getPrimaryTargetPaths(agenda, "mi", "Mental Ray Input (.mi) files");
    
    Path instancePath = 
	getPrimarySourcePath(aInstanceList, agenda, "txt", 
	  "A text file containing a list of instances");
    int listType = getSingleEnumParamIndex(aListType);

    /* the MIs to parse */ 
    ArrayList<ArrayList<Path>> sourcePaths = new ArrayList<ArrayList<Path>>();
    for(String sname : agenda.getSourceNames()) {
      if (sname.equals(getSingleStringParamValue(aInstanceList)))
	continue;
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq.numFrames() != targetPaths.size()) 
        throw new PipelineException
          ("The file sequence (" + fseq + ") of source node (" + sname + ") did not have " + 
           "the same number of frames as the target MI file seqence!"); 

      String suffix = fseq.getFilePattern().getSuffix();
      if((suffix == null) || !suffix.equals("mi"))
        throw new PipelineException
          ("The " + getName() + " Action requires that the source node (" + sname + ") " + 
           "must have Mental Ray Input (.mi) files as its primary file sequence!");
      
      ArrayList<Path> spaths = new ArrayList<Path>();
      Path parent = (new Path(sname)).getParentPath();
      for(Path path : fseq.getPaths()) 
        spaths.add(new Path(parent, path));
      sourcePaths.add(spaths);
    }
    
    boolean hasOverride = false;
    String visibleValue = getValue(aVisible);
    String shadowMapValue = getValue(aShadowMap);
    String traceValue = getValue(aTrace);
    String faceValue = getFaceValue();
    String shadowValue = getValue(aShadow);
    String shadowModeValue = getShadowValue();
    
    if (visibleValue != null || shadowMapValue   != null || 
        traceValue   != null || faceValue        != null || 
        shadowValue  != null || shadowModeValue  != null)
      hasOverride = true;
    
    String reflectValue = getMask(aVisibleInReflect, aHasReflections);
    String refractValue = getMask(aVisibleInRefract, aHasRefractions);
    String fgValue = getMask(aVisibleInFinalG, aHasFinalGather);
    String transpValue = getMask(aVisibleInTransp, aHasTransparency);
    
    String causticValue = getLongMask(aCastCaustics, aReceiveCaustics, aCausticInteract);
    String giValue = getLongMask(aCastGI, aReceiveGI, aGIInteract);
    if (hasOverride == false) 
      if (reflectValue != null || refractValue != null || fgValue != null || 
	  transpValue  != null || causticValue != null || giValue != null )
	hasOverride = true;

    /* create a temporary Python script file to process the MI files */ 
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);

      NodeID nodeID = agenda.getNodeID(); 
      String instGroupName = nodeID.getName().replaceAll("/", "_").substring(1);
      Path wpath = new Path(PackageInfo.sProdPath, 
                            "working/" + nodeID.getAuthor() + "/" + nodeID.getView());
      
      /* process the MI files */ 
      int idx = 0;
      for(Path tpath : targetPaths) {
        out.write("target = open('" + tpath + "', 'w')\n" +
        	  "objects = list()\n" +
                  "try:\n"); 
        
        if (instancePath != null)
          out.write("  ipath = open('" + instancePath + "', 'rU')\n" +
          	    "  iList = list()\n" +
          	    "  try:\n" +
          	    "    for line in ipath:" +
          	    "      iList.append(line.split()[0].lstrip())\n" +
          	    "  finally:\n" +
          	    "    ipath.close()\n");
            

        for(ArrayList<Path> spaths : sourcePaths) 
          out.write("  target.write('$include \"$WORKING" + spaths.get(idx) + "\"\\n')\n");

        for(ArrayList<Path> spaths : sourcePaths) {
          Path path = new Path(wpath, spaths.get(idx));
          out.write("  source = open('" + path + "', 'rU')\n" + 
                    "  try:\n" + 
                    "    for line in source:\n" + 
                    "      if line.startswith('instance'):\n");
          if (instancePath == null)
            out.write("        objects.append(line.split()[1])\n");
          else if (listType == 0) //Include
            out.write("        if iList.count(line.split()[1]) > 0:\n" +
                      "          objects.append(line.split()[1])\n");
          else
            out.write("        if iList.count(line.split()[1]) == 0:\n" +
                      "          objects.append(line.split()[1])\n");
          out.write("  finally:\n" + 
                    "    source.close()\n");
        }
        if (hasOverride) {
          out.write("  for instance in objects:\n" + 
          	    "    target.write(\'incremental instance \' + instance + \'\\n\')\n");
          if (visibleValue != null)
            out.write("    target.write(\'    visible " + visibleValue + "\\n\')\n");
          if (shadowValue != null)
            out.write("    target.write(\'    shadow " + shadowValue + "\\n\')\n");
          if (shadowModeValue != null)
            out.write("    target.write(\'    shadow " + shadowModeValue + "\\n\')\n");
          if (shadowMapValue != null)
            out.write("    target.write(\'    shadowmap " + shadowMapValue + "\\n\')\n");
          if (traceValue != null)
            out.write("    target.write(\'    trace " + traceValue + "\\n\')\n");
          if (faceValue != null)
            out.write("    target.write(\'    face " + faceValue + "\\n\')\n");
          if (reflectValue != null)
            out.write("    target.write(\'    reflection " + reflectValue + "\\n\')\n");
          if (refractValue != null)
            out.write("    target.write(\'    refraction " + refractValue + "\\n\')\n");
          if (transpValue != null)
            out.write("    target.write(\'    transparency " + transpValue + "\\n\')\n");
          if (fgValue!= null)
            out.write("    target.write(\'    finalgather " + fgValue + "\\n\')\n");
          if (causticValue != null)
            out.write("    target.write(\'    caustic " + causticValue + "\\n\')\n");
          if (giValue != null)
            out.write("    target.write(\'    globillum " + giValue + "\\n\')\n");
          out.write("    target.write(\'end instance\\n\\n\')\n");
        }
        
        out.write("  target.write('instgroup \"" + instGroupName + "\"\\n')\n");
        out.write("  for instance in objects:\n" + 
        	  "    target.write(\'  \' + instance + \'\\n\')\n");
        out.write("  target.write('end instgroup\\n')\n" + 
                  "finally:\n" + 
                  "  target.close()\n");
        idx++;
      }
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }     

  /**
   * The bitmask formula is as follows.
   * <ul>
   * <li>[1] to enable casting
   * <li>[2] to enable receiving
   * <li>[4] to disable casting
   * <li>[8] to disable receiving
   * <li>[16] to enable interaction 
   * <li> [32] to disable interaction
   * </ul>
   * <p>
   * The Enum param is as follows 
   * <ul>
   * <li> 0 is no override
   * <li> 1 is Enabled
   * <li> 2 is Disabled
   */
  private String 
  getLongMask
  (
    String casts,
    String receives,
    String interact
  )
    throws PipelineException
  {
    String toReturn = null;
    int castsIndex = getSingleEnumParamIndex(casts);
    int receivesIndex = getSingleEnumParamIndex(receives);
    int interIndex = getSingleEnumParamIndex(interact);
    if (castsIndex == 0 && receivesIndex == 0 && interIndex == 0)
      return toReturn;
    
    if (castsIndex == 1)
      toReturn = "1";
    else
      toReturn = "0";
    
    if (receivesIndex == 1)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;
    
    if (castsIndex == 2)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;

    if (receivesIndex == 2)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;
    
    if (interIndex == 1)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;
    
    if (interIndex == 2)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;

    return toReturn;
  }


  /**
   * The bitmask formula is as follows.
   * <ul>
   * <li>[1] to enable casting
   * <li>[2] to enable receiving
   * <li>[4] to disable casting
   * <li>[8] to disable receiving
   * </ul>
   * <p>
   * The Enum param is as follows 
   * <ul>
   * <li> 0 is no override
   * <li> 1 is Enabled
   * <li> 2 is Disabled
   */
  private String 
  getMask
  (
    String visibleIn, //equals casts
    String has        //equals receives
  )
    throws PipelineException
  {
    String toReturn = null;
    int visIndex = getSingleEnumParamIndex(visibleIn);
    int hasIndex = getSingleEnumParamIndex(has);
    if (visIndex == 0 && hasIndex == 0)
      return toReturn;
    
    if (visIndex == 1)
      toReturn = "1";
    else
      toReturn = "0";
    
    if (hasIndex == 1)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;
    
    if (visIndex == 2)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;

    if (hasIndex == 2)
      toReturn = "1" + toReturn;
    else
      toReturn = "0" + toReturn;
    
    return toReturn;
  }


  private String 
  getShadowValue()
    throws PipelineException
  {
    String toReturn = null;
    int temp = getSingleEnumParamIndex(aShadowMode);
    switch(temp) {
    case 1:
    case 2:
    case 3:
      toReturn = String.valueOf(temp);
      break;
    }
    return toReturn;
  }


  private String 
  getFaceValue()
    throws PipelineException
  {
    String toReturn = null;
    int temp = getSingleEnumParamIndex(aFace);
    switch(temp) {
    case 1:
      toReturn = "front";
      break;
    case 2:
      toReturn = "back";
      break;
    case 3:
      toReturn = "both";
      break;
    }
    return toReturn;
  }


  private String 
  getValue
  (
    String paramName
  ) 
    throws PipelineException
  {
    String toReturn = null;
    int temp = getSingleEnumParamIndex(paramName);
    switch(temp) {
    case 1:
      toReturn = "on";
      break;
    case 2:
      toReturn = "off";
      break;
    }
    return toReturn;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3185853602446551715L;

  public final String aInstanceList = "InstanceList";
  public final String aListType = "ListType";
  
  public final String aVisible = "Visible";
  public final String aShadow = "Shadow";
  public final String aShadowMode = "ShadowMode";
  public final String aShadowMap = "ShadowMap";
  public final String aTrace= "Trace";
  public final String aFace= "Face";
  
  public final String aVisibleInReflect = "VisibleInReflect";
  public final String aHasReflections   = "HasReflections";

  public final String aVisibleInRefract = "VisibleInRefract";
  public final String aHasRefractions   = "HasRefractions";
  
  public final String aVisibleInTransp  = "VisibleInTransp";
  public final String aHasTransparency  = "HasTransparency";
  
  public final String aVisibleInFinalG  = "VisibleInFinalG";
  public final String aHasFinalGather   = "HasFinalGather";
  
  public final String aCastCaustics     = "CastCaustics";
  public final String aReceiveCaustics  = "ReceiveCaustics";
  public final String aCausticInteract  = "CausticInteract";
  
  public final String aCastGI           = "CastGI";
  public final String aReceiveGI        = "ReceiveGI";
  public final String aGIInteract       = "GIInteract";
}

