/**
 * 
 */
package us.temerity.pipeline.plugin.ShaveCacheAction.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A V E   C A C H E   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a Shave and a Haircut cache from a Maya scene for a specific hair object,
 * defined by the name of the node. <P> 
 * 
 * Shave and a Haircut uses the name of the hair object as art of name of the cache.  In order
 * to have it cache succesfully, the name of a node with this Action needs to be in the format
 * shaveStatFile_<i>nameOfHairShape</i>.  The name of the hair shape cannot have any 
 * underscores (_) in it.  The Action will interpret an underscore as a namespace with a 
 * colon.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source Maya scene node that has to contain the necessary Shave nodes.
 *   </DIV> <BR>
 *   
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate just after the scene is opened
 *      and before the cache is generated. <BR>
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after the cache has 
 *      been created. <BR> 
 *   </DIV> 
 * </DIV> <P> 
 */
public class ShaveCacheAction
  extends BaseAction
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public ShaveCacheAction()
  {
    super("ShaveCache", new VersionID("2.0.9"), 
      "Temerity", "Creates a Shave and a Haircut cache from a maya scene");

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
	new LinkActionParam
	(aInitialMEL,
	 "The MEL script to evaluate after scene creation and before importing models.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aFinalMEL,
	 "The MEL script to evaluate after saving the scene.", 
	 null);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aFinalMEL);

      setSingleLayout(layout);
    }
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
    Path sourceNodePath = null;
    NodeID nodeID = agenda.getNodeID();
    String cachePath;
    int cacheStart;
    int cacheEnd;
    Path initialMel = null;
    Path finalMel = null;
    
    {
      /* MEL script filenames */
      initialMel = getMelPath(aInitialMEL, "Initial MEL", agenda);
      finalMel = getMelPath(aFinalMEL, "Final MEL", agenda);
    }

    {
      String name = (String) getSingleParamValue(aMayaScene);
      NodeID sNodeID = new NodeID(nodeID, name);
      FileSeq fseq = agenda.getPrimarySource(name);
      String suffix = fseq.getFilePattern().getSuffix();
      if (!fseq.isSingle() || (suffix == null)
	|| !(suffix.equals("ma") || suffix.equals("mb")))
	throw new PipelineException(
	  "The ShaveCache Action requires that the Source Node"
	  + "must be a single Maya scene file.");

      sourceNodePath = new Path(PackageInfo.sProdPath, sNodeID
	.getWorkingParent()
	+ "/" + fseq.getPath(0));
    }
    
    String shaveName = "";
    
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      if (!fseq.hasFrameNumbers())
	throw new PipelineException(
	"Cache files must have frame numbers");
      FilePattern pat = fseq.getFilePattern();
      String suffix = pat.getSuffix();
      if (!suffix.equals("stat"))
	throw new PipelineException("Cache files must have the (stat) suffix");
      int padding = fseq.getFilePattern().getPadding();
      if (padding != 4)
	throw new PipelineException
	  ("Cache files must have a padding of 4");
      Path p = new Path(nodeID.getName());
      cachePath = ("$WORKING" + p.getParent());
      cacheStart = fseq.getFrameRange().getStart();
      cacheEnd = fseq.getFrameRange().getEnd();
      
      String tempShaveName = pat.getPrefix();
      if (!tempShaveName.startsWith("shaveStatFile_"))
	throw new PipelineException
	  ("The Cache files do not have the proper start to their name.  " +
	   "All cache files must start with (shaveStatFile_)");
      tempShaveName = tempShaveName.replaceFirst("shaveStatFile_", "");
      String buffer[] = tempShaveName.split("_");
      int size = buffer.length;
      for(int i = 0; i < size; i++) {
	String part = buffer[i];
	shaveName = part;
	if(i != (size - 1))
	  shaveName += ":";
      }
    }
    
    File script = createTemp(agenda, 0755, "mel");
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      out.println("if (!`pluginInfo -q -l EnvPathConvert`)");
      out.write("     loadPlugin \"EnvPathConvert\";\n\n");
      
      /* the initial MEL script */ 
      if(initialMel != null) {
	out.write("// INTITIAL MEL\n" + 
	  "source \"" + initialMel + "\";\n\n");
      }
      
      out.write("playbackOptions -e -min " + cacheStart + ";\n");
      out.write("playbackOptions -e -ast " + cacheStart + ";\n");
      out.write("playbackOptions -e -max " + cacheEnd + ";\n");
      out.write("playbackOptions -e -aet " + cacheEnd + ";\n");
      
      out.println("string $envNode = `createNode envPathConvert`;");
      out.println("setAttr -type \"string\" ($envNode + \".envPath\") \""
	+ cachePath + "\";");
      out.println("connectAttr -f ($envNode + \".absPath\") shaveGlobals.tmpDir ;");
      out.println("select -r " + shaveName + ";");
      out.println("shaveClearDynamicsCurrent;");
      out.println("shaveRunDynamicsCurrent;");
      
      /* the final MEL script */ 
      if(finalMel != null) {
	out.write("// FINAL MEL\n" + 
	  "source \"" + finalMel + "\";\n\n");
      }
      
      out.close();
      
    } catch (IOException ex) {
      throw new PipelineException(
	"Unable to write temporary MEL script file (" + script
	+ ") for Job " + "(" + agenda.getJobID() + ")!\n"
	+ ex.getMessage());
    }
    
    /* create the process to run the action */
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(sourceNodePath.toOsString());

      String program = "maya";
      if (PackageInfo.sOsType == OsType.Windows)
	program = (program + ".exe");

      /* added custom Mental Ray shader path to the environment */
      Map<String, String> env = agenda.getEnvironment();
      Map<String, String> nenv = env;
      String midefs = env.get("PIPELINE_MI_SHADER_PATH");
      if (midefs != null) {
	nenv = new TreeMap<String, String>(env);
	Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
	nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
      }

      return new SubProcessHeavy(agenda.getNodeID().getAuthor(),
	getName() + "-" + agenda.getJobID(), program, args, nenv,
	agenda.getWorkingDir(), outFile, errFile);
    } catch (Exception ex) {
      throw new PipelineException(
	"Unable to generate the SubProcess to perform this Action!\n"
	+ ex.getMessage());
    }
  }
  
  
  /**
   * Get the abstract path to the MEL file specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued MEL parameter.
   * 
   * @param title
   *   The title of the parameter in exception messages.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The MEL file or <CODE>null</CODE> if none was specified.
   */ 
  private Path
  getMelPath
  (
    String pname, 
    String title, 
    ActionAgenda agenda
  ) 
  throws PipelineException 
  {
    Path script = null; 
    String mname = (String) getSingleParamValue(pname); 
    if(mname != null) {
      FileSeq fseq = agenda.getPrimarySource(mname);
      if(fseq == null) 
	throw new PipelineException
	("Somehow the " + title + " node (" + mname + ") was not one of the " + 
	"source nodes!");

      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mel")) 
	throw new PipelineException
	("The SyflexApplyCache Action requires that the source node specified by the " + 
	  title + " parameter (" + mname + ") must have a single MEL file as its " + 
	"primary file sequence!");

      NodeID mnodeID = new NodeID(agenda.getNodeID(), mname);
      script = new Path(PackageInfo.sProdPath, 
	mnodeID.getWorkingParent() + "/" + fseq.getFile(0)); 
    }

    return script;		       
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String aMayaScene = "MayaScene";
  private static final String aInitialMEL = "InitialMEL";
  private static final String aFinalMEL = "FinalMEL";
  private static final long serialVersionUID = 6394929640555596222L;
}
