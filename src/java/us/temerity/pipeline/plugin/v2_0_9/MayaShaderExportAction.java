// $Id: MayaShaderExportAction.java,v 1.1 2006/06/26 21:04:38 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   S H A D E R  E X P O R T   A C T I O N                                       */
/*------------------------------------------------------------------------------------------*/

/** 
 * Exports all shaders with a given prefix.
 */ 
public
class MayaShaderExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  MayaShaderExportAction()
  {
    super("MayaShaderExport", new VersionID("2.0.9"), "Temerity",
	  "Exports all shaders with a given prefix.");

    
    {
      ActionParam param = 
	new LinkActionParam
	(PARAM_scene, 
	 "The source Maya scene node.", 
	 null); 
      addSingleParam(param);
    }
    
    
    {
      ActionParam param = 
	new StringActionParam
	(PARAM_prefix, 
	 "The prefix that will be used to select the shaders to export. " + 
	 "If it is a namespace, you need to include the \":\"",  // ??? WHY ":"
	 "name"); 
      addSingleParam(param);
    }
    

    /* MEL scripts */
    {
      ActionParam param = 
	new LinkActionParam
	(PARAM_preExport,
	 "The pre-export MEL script.", 
	 null); 
      addSingleParam(param);
    }
      
      
    {
      ActionParam param = 
	new LinkActionParam
	(PARAM_postExport,
	 "The post-export MEL script.", 
	 null); 
      addSingleParam(param);
    }


    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(PARAM_scene);
      layout.addEntry(PARAM_prefix);

      {
	LayoutGroup sub = new LayoutGroup(false);
	sub.addEntry(PARAM_preExport);
	sub.addEntry(PARAM_postExport);
	layout.addSubGroup(sub);
      }

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
    boolean isAscii = false;
    Path scene;
    Path sourceScene;
    Path preExport = null;
    Path postExport = null;
    NodeID nodeID = agenda.getNodeID();

    /* the generated Maya scene filename */
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();
      if (!fseq.isSingle() || (suffix == null) || 
	  !(suffix.equals("ma") || suffix.equals("mb")))
	throw new PipelineException
	  ("The MayaShaderExport Action requires that the primary target file sequence " +
	   "must be a single Maya scene file.");

      isAscii = suffix.equals("ma");
      scene = new Path(PackageInfo.sProdPath, 
		       nodeID.getWorkingParent() + "/" + fseq.getPath(0));
    }

    {
      String sname = (String) getSingleParamValue("MayaScene");
      if (sname != null) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if (fseq == null)
	  throw new PipelineException
	    ("Somehow the Maya Scene node (" + sname + ") was not one of the source " + 
	     "nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if (!fseq.isSingle() || (suffix == null)
	    || !(suffix.equals("ma") || suffix.equals("mb")))
	  throw new PipelineException
	    ("The MayaMiShader Action requires that the source node specified by the " + 
	     "Maya Scene parameter (" + sname + ") must have a single Maya scene file " + 
	     "as its primary file sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	sourceScene = new Path(PackageInfo.sProdPath, 
			       snodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
      else {
	throw new PipelineException
	  ("The MayaMiShader Action requires the Maya Scene parameter to be set!");
      }
    }

    {
      String sname = (String) getSingleParamValue(PARAM_preExport);
      if (sname != null) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if (fseq == null)
	  throw new PipelineException
	    ("Somehow the Pre Export MEL node (" + sname + ") was not one of the " + 
	     "source nodes!");
	
	  String suffix = fseq.getFilePattern().getSuffix();
	  if (!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel")))
	    throw new PipelineException
	      ("The MayaMiShader Action requires that the source node specified "
	       + "by the Pre Export MEL parameter (" + sname + ") must "
	       + "have a single MEL script as its primary file sequence!");
	  
	  NodeID snodeID = new NodeID(nodeID, sname);
	  preExport = new Path(PackageInfo.sProdPath, 
			       snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
    }

    {
      String sname = (String) getSingleParamValue(PARAM_postExport);
      if (sname != null) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if (fseq == null)
	  throw new PipelineException
	    ("Somehow the Post Export MEL node (" + sname + ") was not one of the " + 
	     "source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if (!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel")))
	  throw new PipelineException
	    ("The MayaMiShader Action requires that the source node specified by the "
	     + "Post Export MEL parameter (" + sname + ") must have a single MEL script "
	     + "as its primary file sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	postExport = new Path(PackageInfo.sProdPath, 
			      snodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
    }

    String prefixName = (String) getSingleParamValue(PARAM_prefix);

    File script = createTemp(agenda, 0755, "mel");
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));

      if (preExport != null) {
	out.println("source \"" + preExport + "\";");
	out.println();
      }

      out.println("select -r `lsThroughFilter -na DefaultAllShadingNodesFilter "
		  + "-sort byName -reverse false`;");
      if (prefixName != null)
	out.println("string $mats[] = `ls -sl \"" + prefixName + "*\"`;");
      else
	out.println("string $mats[] = `ls -sl`;");
      out.println("select -r $mats;");
      String output = "file -op \"v=0\"";
      if (isAscii)
	output += " -typ \"mayaAscii\"";
      else
	output += " -typ \"mayaBinary\"";
      output += " -es \"" + scene.toOsString() + "\";";
      out.println(output);
      
      if (postExport != null)
	out.println("source \"" + postExport + "\";");
      
      out.close();
      
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n"
	 + ex.getMessage());
    }

    /* create the process to run the action */
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(sourceScene.toOsString());
      
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
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 program, args, nenv, agenda.getWorkingDir(), outFile, errFile);
    } 
    catch (Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n"
	 + ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String PARAM_scene = "MayaScene";
  private static final String PARAM_postExport = "PostExportMEL";
  private static final String PARAM_preExport = "PreExportMEL";
  private static final String PARAM_prefix = "SelectionPrefix";


  private static final long serialVersionUID = -1670239630790895450L;

}
