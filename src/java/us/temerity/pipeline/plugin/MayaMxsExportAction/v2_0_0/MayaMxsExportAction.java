// $Id: MayaMxsExportAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaMxsExportAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M X S   E X P O R T   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports a MXS input file for the Maxwell renderer from a Maya scene. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file to render. <BR> 
 *   </DIV> <BR>
 * 
 *   Pre Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate before exporting begins. <BR>
 *     Commonly set to use the MEL script generated by node with the 
 *     {@link us.temerity.pipeline.plugin.v2_0_0.MaxwellGlobalsAction MaxwellGlobals} action.
 *   </DIV> <BR>
 * 
 *   Post Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate after exporting ends. <BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MayaMxsExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaMxsExportAction() 
  {
    super("MayaMxsExport", new VersionID("2.0.0"), "Temerity",
	  "Exports a MXS input file for the Maxwell renderer from a Maya scene.");
    
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

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("MayaScene");

      {
	LayoutGroup mel = new LayoutGroup
	  ("MEL Scripts", 
	   "MEL scripts run at various stages of the exporting process.", 
	   true);
	mel.addEntry("PreExportMEL"); 
	mel.addEntry("PostExportMEL");

	layout.addSubGroup(mel);
      }

      setSingleLayout(layout);
    }

    underDevelopment();
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
    File scene = null;
    File projectDir = null;
    File preExport = null;
    File postExport = null;
    {
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
	      ("The MayaMxsExport Action requires that the source node specified by the Maya " +
	       "Scene parameter (" + sname + ") must have a single Maya scene file as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new File(PackageInfo.sProdDir,
			   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The MayaMxsExport Action requires the Maya Scene parameter to be set!");
	}
      }
      
      {
	File sceneDir = scene.getParentFile();
	if((sceneDir != null) && sceneDir.getName().equals("scenes")) 
	  projectDir = sceneDir.getParentFile();
	  
	File renderScenesDir = new File(projectDir, "renderScenes");
	if(!renderScenesDir.isDirectory()) 
	  projectDir = null;

	if(projectDir == null) 
	  throw new PipelineException
	    ("The MayaMxsExport Action requires that the Maya Scene resides in a standard " + 
	     "Maya project directory structure which contains both \"scenes\" and " + 
	     "\"renderScenes\" subdirectories!");
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
	      ("The MayaMxsExport Action requires that the source node specified by the Pre " +
	       "Export MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

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
	      ("The MayaMxsExport Action requires that the source node specified by the Post " +
	       "Export MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postExport = new File(PackageInfo.sProdDir,
				snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
    }

    /* create a temporary MEL script */ 
    File script = createTemp(agenda, 0644, "mel");
    try {      
      FileWriter out = new FileWriter(script);
      
      if(preExport != null) 
	out.write("source \"" + preExport.getPath() + "\";\n");

      out.write("setProject \"" + projectDir + "\";\n" + 
		"mayall -export;\n");
	
      if(postExport != null) 
	out.write("source \"" + postExport.getPath() + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(scene.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "maya", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 441870473392698747L;

}

