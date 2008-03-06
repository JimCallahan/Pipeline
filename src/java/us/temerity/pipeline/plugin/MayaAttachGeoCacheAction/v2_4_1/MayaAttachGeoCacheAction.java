// $Id: MayaAttachGeoCacheAction.java,v 1.1 2008/03/06 13:03:34 jim Exp $

package us.temerity.pipeline.plugin.MayaAttachGeoCacheAction.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.ArrayList;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M A K E   G E O   C A C H E   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a MEL script which when executed will attach a Maya geometry cache to a given 
 * piece of geometry.<P> 
 * 
 * See the Maya documentation for the MEL command (cacheFile) for details.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Cache File <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya geometry cache file. 
 *   </DIV> <BR>
 * 
 *   Geometry Name
 *   <DIV style="margin-left: 40px;">
 *     The name of the Maya shape node whose points will be attached to the cache.
 *   </DIV> <BR>
 *   
 *   Pre Cache MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate before attaching the geometry cache. 
 *   </DIV> <BR>
 *   
 *   Post Cache MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate after attaching the geometry cache. 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaAttachGeoCacheAction
  extends MayaBuildUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaAttachGeoCacheAction() 
  {
    super("MayaAttachGeoCache", new VersionID("2.4.1"), "Temerity",
          "Creates a MEL script which when executed will attach a Maya geometry cache " + 
          "to a given piece of geometry."); 

    {
      ActionParam param = 
	new LinkActionParam
	(aCacheFile, 
	 "The source node which contains the Maya geometry cache file.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aGeometryName,
         "The name of the Maya shape node whose points will be attached to the cache.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aPreCacheMEL,
         "The MEL script to evaluate before attaching the geometry cache.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aPostCacheMEL,
         "The MEL script to evaluate after attaching the geometry cache.",
         null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aCacheFile);
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
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mel", "MEL script");

    /* MEL script paths */ 
    Path preCacheMEL  = getMelScriptSourcePath(aPreCacheMEL, agenda);
    Path postCacheMEL = getMelScriptSourcePath(aPostCacheMEL, agenda);

    /* the source cache file */ 
    String cachePrefix = null; 
    String cacheDir = null; 
    {
      ActionParam param = getSingleParam(aCacheFile);
      if(param == null) 
        throw new PipelineException
          ("There is no Action parameter named (" + aCacheFile + ")!");

      String title = param.getNameUI();

      String mname = (String) param.getValue();
      if(mname != null) {
        FileSeq fseq = agenda.getPrimarySource(mname);
        if(fseq == null) 
          throw new PipelineException
            ("Somehow the " + title + " node (" + mname + ") was not one of the " + 
             "source nodes!");
      
        String suffix = fseq.getFilePattern().getSuffix();
        if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mc")) {
          throw new PipelineException
            ("The " + getName() + " Action requires that the source node specified by the " + 
             title + " parameter (" + mname + ") must have a single Maya Geometry Cache " + 
             "file as its primary file sequence!");
        }
        
        cachePrefix = fseq.getFilePattern().getPrefix();

        Path spath = new Path(mname);
        cacheDir = spath.getParent();
      }
    }
    
    /* name of the geometry */ 
    String shapeNode = getSingleStringParamValue(aGeometryName); 

    /* create a temporary MEL script to create the geometry cache */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script); 

      out.write("// PLUGIN LOADING\n" +
                "if (!`pluginInfo -q -l \"plEnvEvalNode.py\"`)\n" +
                "  loadPlugin \"plEvalEnvNode.py\";\n\n");

      if(preCacheMEL != null) 
	out.write("// PRE-CACHE SCRIPT\n" + 
                  "print \"Pre-Cache Script: " + preCacheMEL + "\\n\";\n" +
                  "source \"" + preCacheMEL + "\";\n\n");

      /* attach the cache */ 
      out.write
        ("// ATTACH THE GEOMETRY CACHE\n" + 
         "{\n" + 
         "  $working = `getenv(\"WORKING\")`;\n" + 
         "  string $switch = createHistorySwitch(\"" + shapeNode + "\",false);\n" + 
         "  $cacheNode = `cacheFile -attachFile -fileName \"" + cachePrefix + "\" " + 
                                   "-directory ($working + \"" + cacheDir + "\") " + 
                                   "-channelName  \"" + shapeNode + "\" " + 
                                   "-inAttr ($switch + \".inp[0]\")`;\n" + 
         "  string $plenv = `createNode -name \"cacheDirectory\" plEvalEnvNode`;\n" + 
         "  connectAttr -f ($plenv + \".output\") ($cacheNode + \".cachePath\");\n" + 
         "  setAttr -type \"string\" ($plenv + \".input\") (\"$WORKING" + cacheDir + "\");\n"+
         "  setAttr ($switch + \".playFromCache\") true;\n" + 
         "  setAttr ($cacheNode + \".enable\") 1;\n" + 
         "  string $cacheScript = (\"getAttr \\\"\" + $cacheNode + \".cachePath\\\"\");\n" + 
         "  scriptNode -scriptType 1 -bs $cacheScript -n \"cacheDirectoryUpdater\";\n" +
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
    return createTempCopySubProcess(agenda, script, target, outFile, errFile);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1578369171261385411L;
  
  public static final String aCacheFile    = "CacheFile";
  public static final String aGeometryName = "GeometryName";
  public static final String aPostCacheMEL = "PostCacheMEL";
  public static final String aPreCacheMEL  = "PreCacheMEL";

}
