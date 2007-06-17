// $Id: HfsGenerateAction.java,v 1.1 2007/06/17 15:34:41 jim Exp $

package us.temerity.pipeline.plugin.HfsGenerateAction.v1_1_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*    H F S   G E N E R A T E   A C T I O N                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a sequences of renderer input files from a Houdini scene. <P> 
 * 
 * This action provides a convienent method for evaluating a renderer output operator 
 * contained in the source Houdini scene using hscript(1).  The target primary file
 * sequence should contain the input files to be generated. The frame range (trange f1 f2 f3),
 * script file (script) and generate script (tscript) parameters of this operator will 
 * be overridden by the Action to correspond to the input files regenerated by the job. <P> 
 * 
 * The output picture (picture) parameter of this operator should be set to generate 
 * the image files associated with the rendering node downstream of the node using this
 * action.  The output picture path should begin with $WORKING or be relative to the current
 * working directory to insure proper behavior when used by different artists or in different 
 * working area views of the same artist.  It is recommended that the filename prefix used 
 * for the output picture match that of this node.  For example: <P> 
 * <DIV style="margin-left: 40px;">
 *    Generated File Sequence - myrender.#.ifd, 1-100x1 <BR>
 *    Output Picture Parameter - "$WORKING/shot123/images/myrender.$F4.pic"
 * </DIV> <P> 
 * 
 * The following Houdini output operators are supported by this action:<BR>
 * <DIV style="margin-left: 40px;">
 *   Mantra - The Houdini renderer. <BR>
 *   Wren - The Houdini line renderer. <BR>
 *   RenderMan - RenderMan compliant renderers. <BR>
 *   MentalRay - The MentalRay raytracer. <BR>
 * </DIV> <P> 
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on the
 * usage and behavior of renderer output operators and hscript(1).<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Output Operator <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the render output operator. <BR>
 *   </DIV> <BR>
 *
 *   Camera Override <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the render camera (if set). <BR> 
 *   </DIV> <BR>
 * 
 *   Houdini Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Houdini scene file used to generate the input
 *     files. <BR> 
 *   </DIV> <BR>
 * 
 *   Pre Generate Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate before file generation 
 *     begins.  <BR>
 *   </DIV> <BR>
 * 
 *   Post Generate Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate after file generation 
 *     ends. <BR>
 *   </DIV> <BR>
 * 
 *   Pre Frame Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate before generating each 
 *     frame. <BR>
 *   </DIV> <BR>
 * 
 *   Post Frame Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate after generating each 
 *     frame. <BR>
 *   </DIV> <BR>
 * 
 *   Use Graphical License<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to use an interactive graphical Houdini license when running hscript(1).  
 *     Normally, hscript(1) is run using a non-graphical license (-R option).  A graphical 
 *     license may be required if the site has not obtained any non-graphical licenses.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class HfsGenerateAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsGenerateAction() 
  {
    super("HfsGenerate", new VersionID("1.1.0"), "Temerity", 
	  "Generates a sequences of renderer input files from a Houdini scene.");

    {
      ActionParam param = 
	new StringActionParam
	("OutputOperator",
	 "The name of the output operator generating the input files.", 
	 "mantra1");
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new StringActionParam
	("CameraOverride",
	 "Overrides the render camera (if set).", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("HoudiniScene",
	 "The source Houdini scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PreGenerateScript",
	 "The pre-generate command script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PostGenerateScript",
	 "The post-generate command script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PreFrameScript",
	 "The pre-frame command script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PostFrameScript",
	 "The post-frame command script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("UseGraphicalLicense",
	 "Whether to use an interactive graphical Houdini license when running hscript(1).",
	 false);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("OutputOperator");
      layout.addEntry("CameraOverride");
      layout.addSeparator();
      layout.addEntry("HoudiniScene");
      layout.addSeparator();
      layout.addEntry("UseGraphicalLicense");

      {
	LayoutGroup scripts = new LayoutGroup
	  ("Command Scripts", 
	   "Houdini command scripts run at various stages of the input file generation " + 
	   "process.", 
	   true);
	scripts.addEntry("PreGenerateScript"); 
	scripts.addEntry("PostGenerateScript");
	scripts.addSeparator();
	scripts.addEntry("PreFrameScript"); 
	scripts.addEntry("PostFrameScript"); 

	layout.addSubGroup(scripts);
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
    String opname = null;
    File source = null;
    File preGenerate = null;
    File postGenerate = null;
    File preFrame = null;
    File postFrame = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	if(!fseq.hasFrameNumbers())
	  throw new PipelineException
	    ("The HfsGenerate Action requires that the generated renderer input files " + 
	     "have frame numbers.");

	String suffix = fseq.getFilePattern().getSuffix();
	if(!(suffix.equals("rib") || suffix.equals("ifd") || suffix.equals("mi"))) 
	  throw new PipelineException
	    ("The HfsGenerate Action requires that the generated renderer input files " + 
	     "be either Mantra (.ifd), RenderMan (.rib) or MentalRay (.mi) format files.");
      }
      
      /* generate the filename of the Houdini scene to load */
      {
	String sname = (String) getSingleParamValue("HoudiniScene"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Houdini Scene node (" + sname + ") was not one of the source " + 
	       "nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !suffix.equals("hip")) 
	    throw new PipelineException
	      ("The HfsGenerate Action requires that the source node specified by the " + 
	       "Houdini Scene parameter (" + sname + ") must have a single Houdini scene " +
	       "file (.hip) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  source = new File(PackageInfo.sProdDir,
			    snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The HfsGenerate Action requires the Houdini Scene parameter to be set!");
	}
      }
	
      /* the full name of the geometry output operator */ 
      {
	String name = (String) getSingleParamValue("OutputOperator"); 
	if((name == null) || (name.length() == 0))
	  throw new PipelineException
	    ("The HfsGenerate Action requires a valid Output Operator name!");

	opname = ("/out/" + name);
      }

      /* command script files */
      {
	String sname = (String) getSingleParamValue("PreGenerateScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Generate Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The HfsGenerate Action requires that the source node specified by the Pre " +
	       "Generate Script parameter (" + sname + ") must have a single command " +
	       "script (.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preGenerate = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
      
      {
	String sname = (String) getSingleParamValue("PostGenerateScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Generate Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("cmd"))) 
	    throw new PipelineException
	      ("The HfsGenerate Action requires that the source node specified by the Post " +
	       "Generate Script parameter (" + sname + ") must have a single command " +
	       "script (.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postGenerate = new File(PackageInfo.sProdDir,
				snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
      
      {
	String sname = (String) getSingleParamValue("PreFrameScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Frame Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("cmd"))) 
	    throw new PipelineException
	      ("The HfsGenerate Action requires that the source node specified by the Pre " +
	       "Frame Script parameter (" + sname + ") must have a single command script " + 
	       "(.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preFrame = new File(PackageInfo.sProdDir,
			      snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      {
	String sname = (String) getSingleParamValue("PostFrameScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Frame Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("cmd"))) 
	    throw new PipelineException
	      ("The HfsGenerate Action requires that the source node specified by the Post " +
	       "Frame Script parameter (" + sname + ") must have a single command script " + 
	       "(.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postFrame = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
    }
      
    /* create the temporary Houdini command script */ 
    File hscript = createTemp(agenda, 0644, "cmd");
    try {      
      FileWriter out = new FileWriter(hscript);
    
      String camera = (String) getSingleParamValue("CameraOverride");
      if((camera != null) && (camera.length() > 0)) 
	out.write("opparm " + opname + " camera '" + camera + "'\n");

      FileSeq fseq = new FileSeq(PackageInfo.sProdDir.getPath() + nodeID.getWorkingParent(), 
				 agenda.getPrimaryTarget());
      if(fseq.hasFrameNumbers()) {
	FilePattern fpat = fseq.getFilePattern();
	FrameRange frange = fseq.getFrameRange();
	out.write("opparm " + opname + " trange on\n" +
		  "opparm " + opname + " f1 " + frange.getStart() + "\n" +
		  "opparm " + opname + " f2 " + frange.getEnd() + "\n" +
		  "opparm " + opname + " f3 " + frange.getBy() + "\n" +
		  "opparm " + opname + " tscript on\n" + 
		  "opparm " + opname + " script '" + fpat.getPrefix() + ".$F");

	if(fpat.getPadding() > 1) 
	  out.write(String.valueOf(fpat.getPadding()));
	
	out.write("." + fpat.getSuffix() + "'\n");
      }
      else {
	out.write("opparm " + opname + " trange off\n" +
		  "opparm " + opname + " " + fseq + "\n");
      }

      if(preGenerate != null) 
	out.write("opparm " + opname + " prerender '" + preGenerate + "'\n");

      if(postGenerate != null) 
	out.write("opparm " + opname + " postrender '" + postGenerate + "'\n");
	
      if(preFrame != null) 
	out.write("opparm " + opname + " preframe '" + preFrame + "'\n");
	
      if(postFrame != null) 
	out.write("opparm " + opname + " postframe '" + postFrame + "'\n");

      out.write("opparm -c " + opname + " execute\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + hscript + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* license type */ 
    String licopt = " -R";
    {
      Boolean tf = (Boolean) getSingleParamValue("UseGraphicalLicense"); 
      if((tf != null) && tf)
	licopt = "";
    }

    /* create the wrapper shell script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n" +
		"cat " + hscript + " | hscript" + licopt + " -v " + source); 
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(), new ArrayList<String>(), 
	 agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -9055558547083589380L;

}

