// $Id: MayaSdExportAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaSdExportAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   S D   A C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports RealFlow geometry and other scene data from a Maya scene. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file. <BR> 
 *   </DIV> <BR>
 * 
 *   Export Selected <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to only export the selected objects in the Maya scene.  <P> 
 *     If true, the selection should be set by MEL script specified by the Pre Export MEL script 
 *     parameter.  If false, the entire scene will be exported regardless of the selection.
 *   </DIV> <BR>
 * 
 *   Export Deformation <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to export per-vertex deformations. <BR> 
 *   </DIV> <BR>
 * 
 *   Pre Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate before exporting begins. <BR>
 *   </DIV> <BR>
 * 
 *   Post Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the MEL script to evaluate after exporting ends. <BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MayaSdExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaSdExportAction() 
  {
    super("MayaSdExport", new VersionID("2.0.0"), "Temerity",
	  "Exports RealFlow geometry and other scene data from a Maya scene.");
    
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
	new BooleanActionParam
	("ExportSelected",
	 "Whether to only export the selected objects in the Maya scene.", 
	 false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("ExportDeformation",
	 "Whether to export per-vertex deformations.", 
	 false);
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
      layout.addEntry("ExportSelected");
      layout.addEntry("ExportDeformation");

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
    boolean exportSelected = false;
    File preExport = null;
    File postExport = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !suffix.equals("sd"))	   
	  throw new PipelineException
	    ("The MayaSdExport Action requires a single output SD file.");
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
	      ("The MayaSdExport Action requires that the source node specified by the Maya " +
	       "Scene parameter (" + sname + ") must have a single Maya scene file as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new File(PackageInfo.sProdDir,
			   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The MayaSdExport Action requires the Maya Scene parameter to be set!");
	}
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
	      ("The MayaSdExport Action requires that the source node specified by the Pre " +
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
	      ("The MayaSdExport Action requires that the source node specified by the Post " +
	       "Export MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postExport = new File(PackageInfo.sProdDir,
				snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      {
	Boolean selected = (Boolean) getSingleParamValue("ExportSelected"); 
	if((selected != null) && selected) {
	  exportSelected = true;

	  if(preExport == null) 
	    throw new PipelineException
	      ("When exporting only the selected objects from the Maya scene, a Pre Export " +
	       "MEL script must be specified which performs the selection of the objects to " + 
	       "export!");
	}
      }
    }

    /* create a temporary MEL script */ 
    File script = createTemp(agenda, 0644, "mel");
    try {      
      FileWriter out = new FileWriter(script);
      
      if(preExport != null) 
	out.write("source \"" + preExport.getPath() + "\";\n");

      String deformFlag = "0";
      {
	Boolean deform = (Boolean) getSingleParamValue("ExportDeformation"); 
	if((deform != null) && deform) 
	  deformFlag = "1";
      }
      
      FileSeq fseq = new FileSeq(PackageInfo.sProdDir.getPath() + nodeID.getWorkingParent(), 
				 agenda.getPrimaryTarget());
      
      out.write("if(!`pluginInfo -q -loaded \"sdTranslator\"`)\n" + 
		"  loadPlugin \"sdTranslator\";\n" + 
		"file -op \"exportDeformation=" + deformFlag + "\" -typ \"sd\" " + 
		(exportSelected ? "-es" : "-ea") + " \"" + fseq.getFile(0) + "\";\n");
	
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

  private static final long serialVersionUID = 8633293605394510170L;

}

