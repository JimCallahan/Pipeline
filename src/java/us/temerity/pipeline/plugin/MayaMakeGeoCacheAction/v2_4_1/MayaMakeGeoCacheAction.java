// $Id: MayaMakeGeoCacheAction.java,v 1.1 2008/03/20 21:08:39 jim Exp $

package us.temerity.pipeline.plugin.MayaMakeGeoCacheAction.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.ArrayList;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M A K E   G E O   C A C H E   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a single Maya geometry cache file which stores attribute data for the points of 
 * a given piece of geometry over the current playback time range.<P> 
 * 
 * See the Maya documentation for the MEL command (cacheFile) for details.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file. 
 *   </DIV> <BR>
 * 
 *   Geometry Name
 *   <DIV style="margin-left: 40px;">
 *     The name of the Maya shape node whose points will be cached.
 *   </DIV><BR>
 *   
 *   Pre Cache MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate before creating the geometry cache. 
 *   </DIV> <BR>
 *   
 *   Post Cache MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate after creating the geometry cache. 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaMakeGeoCacheAction
  extends MayaActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaMakeGeoCacheAction() 
  {
    super("MayaMakeGeoCache", new VersionID("2.4.1"), "Temerity",
          "Creates a single Maya geometry cache file which stores attribute data for " + 
          "the points of a given piece of geometry over the current playback time range."); 

    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene,
	 "The source Maya scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aGeometryName,
         "The name of the Maya shape node whose points will be cached.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aPreCacheMEL,
         "The MEL script to evaluate before creating the geometry cache.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aPostCacheMEL,
         "The MEL script to evaluate after creating the geometry cache.",
         null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aGeometryName);
      layout.addSeparator(); 
      layout.addEntry(aPreCacheMEL);
      layout.addEntry(aPostCacheMEL);

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
    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    
    /* MEL script paths */ 
    Path preCacheMEL  = getMelScriptSourcePath(aPreCacheMEL, agenda);
    Path postCacheMEL = getMelScriptSourcePath(aPostCacheMEL, agenda);

    /* name of the geometry */ 
    String shapeNode = getSingleStringParamValue(aGeometryName); 

    /* the geometry cache file to create */ 
    String cachePrefix = null; 
    Path cacheDir = null; 
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mc")) 
        throw new PipelineException
          ("The " + getName() + " Action requires that the primary target file sequence " + 
           "(" + fseq + ") must be a single Maya Geometry Cache!");

      cachePrefix = fseq.getFilePattern().getPrefix();
      
      Path path = new Path(agenda.getTargetPath(), fseq.getPath(0));
      cacheDir = path.getParentPath();
    }

    /* create a temporary MEL script to create the geometry cache */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script); 

      if(preCacheMEL != null) 
	out.write("// PRE-CACHE SCRIPT\n" + 
                  "print \"Pre-Cache Script: " + preCacheMEL + "\\n\";\n" +
                  "source \"" + preCacheMEL + "\";\n\n");

      out.write
        ("// CREATE THE GEOMETRY CACHE\n" + 
         "{\n" + 
         "  print \"Creating: " + cacheDir + "/" + cachePrefix + ".mc\\n\";\n" +
         "  float $st = `playbackOptions -query -animationStartTime`;\n" + 
         "  float $et = `playbackOptions -query -animationEndTime`;\n" + 
         "  cacheFile -startTime $st -endTime $et -points \"" + shapeNode + "\" " + 
         "-format \"OneFile\" -fileName \"" + cachePrefix + "\" " + 
         "-directory \"" + cacheDir + "\";\n" + 
         "}\n\n"); 

      if(postCacheMEL != null)
	out.write("// POST-CACHE SCRIPT\n" + 
                  "print \"Post-Cache Script: " + postCacheMEL + "\\n\";\n" +
                  "source \"" + postCacheMEL + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */
    return createMayaSubProcess(sourceScene, script, true, agenda, outFile, errFile);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2386274830116068147L;
  
  public static final String aMayaScene    = "MayaScene";
  public static final String aGeometryName = "GeometryName";
  public static final String aPostCacheMEL = "PostCacheMEL";
  public static final String aPreCacheMEL  = "PreCacheMEL";

}
