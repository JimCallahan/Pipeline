// $Id: MayaPygExportAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaPygExportAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   P Y G     A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports Pyg input files for the Gelato renderer from a Maya scene. <P> 
 * 
 * Uses the mangoBatchExport MEL command to generate either a single or per-frame Pyg files.
 * In addition, the exported files are renamed to comply with Pipeline file naming
 * conventions.  For each frame, there will be two Pyg files generated.  The top level Pyg
 * files which should be rendered with Gelato will be named to match the primary file sequence
 * of this node.  These top level Pyg files contain render settings, camera, outputs and 
 * include the main Pyg containing the geometry and lights which make up the scene.  These
 * main Pyg files will have "_main" appended to the prefix portion of the file sequence 
 * pattern.  They should be added as a secondary sequence to this node. <P> 
 * 
 * Due to limitations in the mangoBatchExport MEL command, all frames must be exported by
 * a single invocation of this action.  Therefore only the Serial execution method should be
 * used with this node.  The mangoBatchExport MEL command is currently only capable of 
 * exporting the entire Maya scene. <P> 
 * 
 * Note the the Render Camera parameter is required by this action and is crucial to the 
 * proper renaming of the generated Pyg files. <P> 
 * 
 * See the Mango documentation for details about the <B>mangoBatchExport</B>(1) MEL 
 * command.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file. <BR> 
 *   </DIV> <BR>
 *   
 *   Render Camera <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the shape node in the Maya scene for the camera being rendered.
 *   </DIV> <BR>
 * 
 *   <I>MEL Scripts</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Pre Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The source node which contains the MEL script to evaluate before exporting begins. 
 *     </DIV> <BR>
 *   
 *     Post Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The source node which contains the MEL script to evaluate after exporting ends. <BR>
 *     </DIV> 
 *    </DIV> 
 * </DIV> <P> 
 */
public
class MayaPygExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaPygExportAction() 
  {
    super("MayaPygExport", new VersionID("2.0.0"), "Temerity",
	  "Exports Pyg input files for the Gelato renderer from a Maya scene.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("MayaScene",
	 "The source Maya scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("RenderCamera", 
	 "The name of the shape node in the Maya scene for the camera being rendered.",
	 "perspShape");
      addSingleParam(param);
    }
    
    /* MEL scripts */ 
    {
      {
	ActionParam param = 
	  new LinkActionParam
	  ("PreExportMEL",
	   "The pre-export MEL script.", 
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new LinkActionParam
	  ("PostExportMEL",
	   "The post-export MEL script.", 
	   null);
	addSingleParam(param);
      }
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("MayaScene");
      layout.addEntry("RenderCamera");

      {
	LayoutGroup group = new LayoutGroup
	  ("MEL Scripts", 
	   "MEL scripts run at various stages of the exporting process.", 
	   true);
	group.addEntry("PreExportMEL"); 
	group.addEntry("PostExportMEL");

	layout.addSubGroup(group);
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */ 
    FileSeq target = null;
    String camera = null;
    File scene = null;
    File preExport = null;
    File postExport = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	if(!fseq.isSingle() && (fpat.getPadding() > 1))
	  throw new PipelineException
	    ("The primary file sequence must have unpadded frame numbers!");

	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("pyg"))	   
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") must contain one or more Gelato " + 
	     "input files (.pyg)!");

	target = fseq;
      }

      {
	String sname = (String) getSingleParamValue("MayaScene"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Maya Scene node (" + sname + ") was not one of the source " + 
	       "nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || 
	     (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
	    throw new PipelineException
	      ("The MayaPygExport Action requires that the source node specified by the " +
	       "Maya Scene parameter (" + sname + ") must have a single Maya scene file " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new File(PackageInfo.sProdDir,
			   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The MayaPygExport Action requires the Maya Scene parameter to be set!");
	}
      }      

      {
	camera = (String) getSingleParamValue("RenderCamera"); 
	if((camera == null) || (camera.length() == 0)) 
	  throw new PipelineException
	    ("The MayaPygExport Action requires that the Render Camera must be specified!");
      }

      {
	String sname = (String) getSingleParamValue("PreExportMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaPygExport Action requires that the source node specified by the " +
	       "Pre Export MEL parameter (" + sname + ") must have a single MEL script " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preExport = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      {
	String sname = (String) getSingleParamValue("PostExportMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaPygExport Action requires that the source node specified by the " +
	       "Post Export MEL parameter (" + sname + ") must have a single MEL script " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postExport = new File(PackageInfo.sProdDir,
				snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
    }

    /* create a temporary MEL script */ 
    File mel = createTemp(agenda, 0644, "mel");
    try {      
      FileWriter out = new FileWriter(mel);
      
      if(preExport != null) 
	out.write("source \"" + preExport.getPath() + "\";\n\n");
      
      FilePattern fpat = target.getFilePattern();
      FrameRange range = target.getFrameRange();
      if(target.hasFrameNumbers())
	out.write("setAttr \"defaultRenderGlobals.an\" 1;\n" + 
		  "setAttr \"defaultRenderGlobals.ofc\" 2;\n" + 
		  "setAttr \"defaultRenderGlobals.startFrame\" " + range.getStart() + ";\n" + 
		  "setAttr \"defaultRenderGlobals.endFrame\" " + range.getEnd() + ";\n" + 
		  "setAttr \"defaultRenderGlobals.byFrameStep\" " + range.getBy() + ";\n\n");
      else
	out.write("setAttr \"defaultRenderGlobals.an\" 0;\n" + 
		  "setAttr \"defaultRenderGlobals.ofc\" 0;\n");

      out.write("string $cameras[] = `ls -cameras`;\n" + 
		"for ($i = 0; $i < size($cameras); $i++)\n" + 
		"  setAttr ($cameras[$i] + \".renderable\") no;\n" + 
		"setAttr (\"" + camera + ".renderable\") yes;\n" + 
		"\n" +
		"if(! `pluginInfo -q -l Mango`)\n" + 
		"  loadPlugin -qt (getenv(\"MANGOHOME\") + \"/plug-ins/Mango.so\");\n" +
		"setAttr -type \"string\" defaultRenderGlobals.imageFilePrefix " +
		"\"./" + fpat.getPrefix() + "\";\n" + 
		"gelatoBatchExport(\"" + fpat.getPrefix() + ".pyg\");\n\n");

      if(postExport != null) 
	out.write("source \"" + postExport.getPath() + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + mel + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create a temporary script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n" + 
		"echo Exporting...\n" +
		"maya -batch -script " + mel + " -file " + scene + "\n" + 
		"\n" + 
		"echo Renaming...\n");

      String prefix = target.getFilePattern().getPrefix();
      FrameRange range = target.getFrameRange();
      if(target.hasFrameNumbers()) {
	int frames[] = range.getFrameNumbers();
	int wk;
	for(wk=0; wk<frames.length; wk++) {
	  int fr = frames[wk];
	  out.write("rm -f " + prefix + "." + fr + ".pyg\n" +
		    "cat " + prefix + "." + fr + "_" + camera + ".pyg " + 
		    "| sed -e 's|Input (\"./" + prefix + "." + fr + "_main.pyg\")" + 
		    "|Input (\"" + prefix + "_main." + fr + ".pyg\")|g' " +
		    "> " + prefix + "." + fr + ".pyg\n" + 
		    "rm -f " + prefix + "." + fr + "_" + camera + ".pyg\n" +
		    "mv " + prefix + "." + fr + "_main.pyg " + 
		    prefix + "_main." + fr + ".pyg\n\n");
	}
	
	out.write("rm -f " + prefix + ".pyg\n");
      }
      else {
	out.write("rm -f " + prefix + ".pyg\n" +
		  "cat " + prefix + "_" + camera + ".pyg " + 
		  "| sed -e 's|Input (\"./" + prefix + "_main.pyg\")" + 
		  "|Input (\"" + prefix + "_main.pyg\")|g' " +
		  "> " + prefix + ".pyg\n");
      }

      out.write("echo DONE.\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4993201585801212198L;

}

