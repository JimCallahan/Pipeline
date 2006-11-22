// $Id: MayaAnimExportAction.java,v 1.5 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   A N I M   E X P O R T   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Exports animation channels from a Maya scene. <P> 
 * 
 * For each target file sequence which contains a single Maya animation (.anim) file, there
 * should exist a namespace in the animated Maya scene which matches the animation file 
 " prefix exactly.  <P> 
 * 
 * Each of these namespaces should contain a Maya set used to select the DAG nodes owning the
 * animation channels to be baked and/or exported in that namespace.  This selection set is 
 * specified by the Export Set single parameter.  The namespace should also contain a DAG
 * node under which all visible geometry is grouped (usually called ROOT).  This root DAG 
 * node should have its visibility channel set to keyable so that the MayaCollate action can 
 * properly control when objects should be visible in multiple shot animation sequences. <P> 
 * 
 * If the primary file sequences is not an animation file, it will simply be touched by
 * this action.  This can be useful when exporting several animation files at once so that
 * the name of the Pipeline node need not correspond to one of these sequences, but instead
 * can be given a more appropriate name. <P> 
 * 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Method <BR>
 *   <DIV style="margin-left: 40px;">
 *     The animation export method: <BR>
 *     <UL>
 *       <LI>Export   - Exports the animation channels of the members of the Export Set. 
 *       <LI>Bake     - First bakes per-frame animation (simulation OFF) for the channels of 
 *                      the members of the Export Set and then exports these baked channels.
 *       <LI>Simulate - First bakes per-frame animation (simulation ON) for the channels of 
 *                      the members of the Export Set and then exports these baked channels.
 *       <LI>Script   - The Export MEL script is responsible for performing all animation
 *                      baking and/or export operations. 
 *     </UL>
 *   </DIV> <BR>
 *   
 *   Export Set
 *   <DIV style="margin-left: 40px;">
 *     The name of the Maya Set (under each namespace) used to identify the DAG nodes who's 
 *     animation channels should be baked and/or exported.
 *   </DIV>
 *   
 * 
 *   First Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *      The first exported frame of animation.
 *   </DIV> <BR>
 * 
 *   Last Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *      The last export frame of animation. <P> 
 * 
 *      Note that a frame of animation is defined as continuing up to the start of the frame 
 *      after this frame.  Exported animation curves should contain valid data over the entire
 *      last frame to insure valid motion blur at shot transitions.  If no key exists at the
 *      time (Last Frame + 1.0), a key will be inserted at that time and included in the 
 *      exported animation.
 *   </DIV> <BR>
 * 
 *   
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source who's primay file sequence is the Maya scene which contains the animation
 *     being exported.  
 *   </DIV> <BR>
 * 
 * 
 *   Prep MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     If set, this parameter specifies the node containing the MEL script to evalute after
 *     loading the Maya Scene but before baking and/or exporting animation.  This script can 
 *     be useful for performing any dynamic setup needed before animation export begins.
 *   </DIV> 
 * 
 *   Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     When the Method parameter is set to Script, this parameter specifies the node 
 *     containing the MEL script resposible for exporting all animation files.  Any baking
 *     of channels required before export is the responsibility of this script.
 *   </DIV> <BR> 
 * </DIV> <P> 
 * 
 * This action adds several environmental variables to the environment under which Maya is 
 * run in order to communicate action parameters to the Prep and Export MEL scripts:<BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   ANIM_EXPORT_ANIMS <BR>
 *   <DIV style="margin-left: 40px;">
 *     A comma seperated list of the prefixes of the animation files being exported.
 *   </DIV> 
 * 
 *   ANIM_EXPORT_SET <BR>
 *   <DIV style="margin-left: 40px;">
 *     The value of the Export Set single parameter.
 *   </DIV> 
 * 
 *   ANIM_EXPORT_FIRST_FRAME <BR>
 *   <DIV style="margin-left: 40px;">
 *     The value of the First Frame single parameter.
 *   </DIV> 
 * 
 *   ANIM_EXPORT_LAST_FRAME <BR>
 *   <DIV style="margin-left: 40px;">
 *     The value of the Last Frame single parameter.
 *   </DIV> 
 * </DIV> 
 */
public
class MayaAnimExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaAnimExportAction() 
  {
    super("MayaAnimExport", new VersionID("2.0.0"), "Temerity", 
	  "Exports animation channels from a Maya scene.");

    {
      ArrayList<String> options = new ArrayList<String>(); 
      options.add("Export");
      options.add("Bake");
      options.add("Simulate");
      options.add("Script");

      ActionParam param = 
	new EnumActionParam
	("Method",
	 "The animation export method.",
	 "Export", options); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("ExportSet", 
	 "The name of the Maya Set (under each namespace) used to identify the DAG nodes " +
	 "who's animation channels should be baked and/or exported.", 
	 "SELECT");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("FirstFrame",
	 "The first exported frame of animation.", 
	 0);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("LastFrame",
	 "The last exported frame of animation.", 
	 30);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new LinkActionParam
	("MayaScene",
	 "The source Maya scene node containing the animation being exported.", 
	 null);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new LinkActionParam
	("PrepMEL",
	 "The MEL script to evaluate after loading the animation scene but before baking " +
	 "or exporting animation.", 
	 null);
      addSingleParam(param);
    }
	  
    {
      ActionParam param = 
	new LinkActionParam
	("ExportMEL",
	 "The MEL script to evaluate in order to bake and/or export animation.",
	 null);
      addSingleParam(param);
    }
	  
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("Method");
      layout.addEntry("ExportSet");
      layout.addSeparator(); 
      layout.addEntry("FirstFrame");
      layout.addEntry("LastFrame");
      layout.addSeparator(); 
      layout.addEntry("MayaScene");
      layout.addSeparator(); 
      layout.addEntry("PrepMEL"); 
      layout.addEntry("ExportMEL");

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
    File touch = null;
    TreeMap<String,File> targetAnims = new TreeMap<String,File>();
    String method = null;
    String exportSet = null;
    Integer firstFrame = null;
    Integer lastFrame = null;
    File scene = null;
    File prepMel = null;
    File exportMel = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	if(fseq.hasFrameNumbers()) 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") must be a single file!");

	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if(suffix == null) 
	  touch = fseq.getFile(0);
	else if(suffix.equals("anim")) 
	  targetAnims.put(fpat.getPrefix(), fseq.getFile(0));
	else 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") must be either a Maya animation " + 
	     "(.anim) file or a single dummy file without a suffix!");
      }

      for(FileSeq fseq : agenda.getSecondaryTargets()) {
	if(fseq.hasFrameNumbers()) 
	  throw new PipelineException
	    ("The secondary file sequence (" + fseq + ") must be a single file!");
	
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("anim")) 
	  throw new PipelineException
	    ("The secondary file sequence (" + fseq + ") must be a Maya animation " + 
	     "(.anim) file!");	
	
	if(targetAnims.containsKey(fpat.getPrefix())) 
	  throw new PipelineException
	    ("Each target animation file sequence must have a unique filename prefix!");

	targetAnims.put(fpat.getPrefix(), fseq.getFile(0));
      }

      {
	method = (String) getSingleParamValue("Method"); 
	if((method == null) || (method.length() == 0))
	  throw new PipelineException 
	    ("Somehow the export Method was undefined!");

	ArrayList<String> options = new ArrayList<String>();
	options.add("Export");
	options.add("Bake");
	options.add("Simulate");
	options.add("Script");
	if(!options.contains(method)) 
	  throw new PipelineException
	    ("The export Method (" + method + ") was not one of the legal values!");
      }

      {
	exportSet = (String) getSingleParamValue("ExportSet"); 
	if((exportSet == null) || (exportSet.length() == 0))
	  throw new PipelineException 
	    ("The MayaAnimExport Action requires a valid Export Set!");
      }

      {
	firstFrame = (Integer) getSingleParamValue("FirstFrame"); 
	if(firstFrame == null)
	  throw new PipelineException 
	    ("The MayaAnimExport Action requires a valid First Frame!");
	  
	lastFrame = (Integer) getSingleParamValue("LastFrame"); 
	if(lastFrame == null)
	  throw new PipelineException 
	    ("The MayaAnimExport Action requires a valid Last Frame!");

	if(firstFrame > lastFrame) 
	  throw new PipelineException
	    ("The First Frame cannot be greater-than the Last Frame!");
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
	      ("The MayaAnimExport Action requires that the source node specified by the " +
	       "Maya Scene parameter (" + sname + ") must have a single Maya scene file " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new File(PackageInfo.sProdDir,
			   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The MayaAnimExport Action requires a valid the Maya Scene!");
	}
      }
      
      {
	String sname = (String) getSingleParamValue("PrepMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Prep MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaAnimExport Action requires that the source node specified by the " +
	       "Prep MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  prepMel = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      {
	String sname = (String) getSingleParamValue("ExportMEL"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaAnimExport Action requires that the source node specified by the " +
	       "Export MEL parameter (" + sname + ") must have a single MEL script as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  exportMel = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else if(method.equals("Script")) {
	  throw new PipelineException
	    ("The MayaAnimExport Action requires an Export MEL script when the export " + 
	     "Method is (Script)!");
	}
      }
    }
      
    /* add the extra environmental variables */ 
    TreeMap<String,String> senv = new TreeMap<String,String>(agenda.getEnvironment());
    {
      {
	StringBuilder buf = new StringBuilder();
	for(String prefix : targetAnims.keySet()) 
	  buf.append(prefix + ":");
	String value = buf.toString();
	senv.put("ANIM_EXPORT_ANIMS", value.substring(0, value.length()-1));
      }

      senv.put("ANIM_EXPORT_SET", exportSet);
      senv.put("ANIM_EXPORT_FIRST_FRAME", firstFrame.toString());
      senv.put("ANIM_EXPORT_LAST_FRAME", lastFrame.toString());
    }

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0644, "mel");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("print (\"$WORKING = \" + getenv(\"WORKING\") + \"\\n\");\n"); 

      /* a workaround needed in "maya -batch" mode */ 
      out.write("// WORK AROUNDS:\n" + 
		"lightlink -q;\n\n");
      
      /* load the animImportExport plugin */ 
      out.write("loadPlugin \"animImportExport.so\";\n\n");

      /* the Prep MEL script */ 
      if(prepMel != null) {
	out.write("// PREP MEL\n" + 
		  "source \"" + prepMel + "\";\n\n");
      }

      /* export one extra frame */ 
      lastFrame++;

      if(method.equals("Bake") || method.equals("Simulate")) {
	out.write("// BAKE KEYS\n");

	String sim = "";
	if(method.equals("Simulate")) 
	  sim = "-simulation true ";

	out.write("select -r ");
	for(String prefix : targetAnims.keySet()) 
	  out.write("\"" + prefix + ":" + exportSet + "\" ");
	out.write(";\n");	

	out.write("bakeResults -t \"" + firstFrame + ":" + lastFrame + "\" " + sim +
		  "`ls -sl`;\n\n");
      }
      
      if(method.equals("Script")) {
	out.write("// EXPORT MEL\n" + 
		  "print \"Evaluating Export MEL script: " + exportMel + "\\n\";\n" +
		  "source \"" + exportMel + "\";\n\n");
      }
      else {
	for(String prefix : targetAnims.keySet()) {
	  File target = targetAnims.get(prefix);
	  out.write
	    ("// EXPORT: " + prefix + "\n" +
	     "print \"Exporting: " + target + "\\n\";\n" +
	     "select -r \"" + prefix + ":" + exportSet + "\";\n" +
	     "setInfinity -postInfinite linear;\n" + 
	     "file\n" + 
	     "  -type \"animExport\"\n" + 
	     "  -exportSelected\n" + 
	     "  -force\n" + 
	     "  -options \"precision=17;intValue=17;nodeNames=1;verboseUnits=0;" + 
	     "whichRange=2;range=" + firstFrame + ":" + lastFrame + ";" + 
	     "options=curve;hierarchy=selected;controlPoints=0;shapes=1;" + 
	     "helpPictures=0;useChannelBox=0;" + 
	     "copyKeyCmd=-animation objects -time >" + firstFrame + ":" + lastFrame + 
	     "> -float >" + firstFrame + ":" + lastFrame + "> -option curve " + 
	     "-hierarchy selected -controlPoints 0 -shape 1\"\n" + 
	     "  \"" + target + "\";\n\n");
	}
      }

      if(touch != null) 
	out.write("system \"touch " + touch + "\";\n\n");

      out.write("print \"ALL DONE.\\n\";\n");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
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
	 "maya", args, senv, agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -8907325751539886288L;

}

