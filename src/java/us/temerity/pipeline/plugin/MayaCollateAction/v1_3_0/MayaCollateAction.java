// $Id: MayaCollateAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MayaCollateAction.v1_3_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C O L L A T E   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Maya scene from model scenes and imported animation data. <P> 
 * 
 * A new empty scene is first created.  The model scenes are imported as Maya references 
 * from each source node who's primary file sequence is a Maya scene file (.ma or .mb).  
 * Each of these model scenes should contain a DAG node which has a name that matches the
 * Root DAG Node single parameter (usually called ROOT) under which all visible geometry is 
 * grouped. These scenes should also contain a Maya set which has a name that matches the 
 * value of the Import Set single parameter (usually called SELECT).  When a model scene is 
 * imported, it is placed in a Maya namespace which matches the prefix of the model scene 
 * file name. <P> 
 * 
 * Then the animation data is imported from each animation file (.anim) sequence which sets
 * the per-source parameters below.  This animation data is then applied to the imported 
 * reference objects in the generated scene. <P> 
 * 
 * The animation data is composed of a series of shots.  Each shot contains one or more 
 * animations of equal frame length which share the same Shot Order.  Shots are processed 
 * in lowest to higest Shot Order.  The animations which make up the first shot (lowest Shot 
 * Order) are applied to the generated Maya scene starting at Begin Frame. Subsequent shots 
 * are concatented after each previous shot.  All animation file sequences which share the
 * same Order must be of the same length.  Animstion length is determined by looking at
 * the single parameters of the MayaExportAnim action which generated the animation files.
 * This means that all animations used by MayaCollate must have been generated by a 
 * MayaExportAnim action. <P> 
 * 
 * For each shot, the prefixes of the animation files (which set Order) must correspond
 * to the prefix of one of the imported reference model scenes.  If there is no model to
 * for an animation, an error will be generated.  Conversely, any models which have no 
 * corresponding animation for a shot will have the visibility of their "ROOT" DAG node
 * set to (false) for the duration of the shot.  This allows objects which only should 
 * appear in a subset of the shots to remain properly hidden when not animated. <P> 
 * 
 * At each stage in the process, optional MEL scripts may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the Intial MEL, Model MEL, Anim MEL and Final MEL single valued 
 * parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Root DAG Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     The DAG node in each the model scene under which all visible geometry is grouped.
 *   </DIV> <BR>
 * 
 *   Import Set
 *   <DIV style="margin-left: 40px;">
 *     The name of the Maya Set in each model scene used to identify the DAG nodes
 *     to which the imported animation will be applied.
 *   </DIV>
 *
 *   Begin Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The frame number of the first animation keyframe in the generated Maya scene. 
 *   </DIV> <BR>
 * 
 * 
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate just after scene creation
 *     and before importing any models.
 *   </DIV> <BR>
 * 
 *   Model MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate after importing all models,
 *     but before loading and applying any animation data.
 *   </DIV> <BR>
 * 
 *   Anim MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate after loading and applying all
 *     animation data, but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate after saving the generated 
 *     Maya scene.
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *      Determines the order in which the animations are concatenated.  Animations are 
 *      processed from least to greatest Order.  All animations with the same Order
 *      are applied to the same frame range in the generate Maya scene.
 *   </DIV> <BR> 
 * </DIV> <P> 
 */
public
class MayaCollateAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaCollateAction() 
  {
    super("MayaCollate", new VersionID("1.3.0"), "Temerity", 
	  "Builds a Maya scene from model scenes and animation files.");
    
    {
      ActionParam param = 
	new StringActionParam
	("RootDAGNode",
	 "The DAG node in each the model scene under which all visible geometry is grouped.", 
	 "ROOT");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("ImportSet", 
	 "The name of the Maya Set in each model scene used to identify the DAG nodes " +
	 "to which the imported animation will be applied.", 
	 "SELECT");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("BeginFrame",
	 "The start frame of animation in the generated Maya scene.", 
	 0);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new LinkActionParam
	("InitialMEL",
	 "The MEL script to evaluate after scene creation and before importing models.",
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	("ModelMEL",
	 "The MEL script to evaluate after importing models but before animation.",
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	("AnimMEL",
	 "The MEL script to evaluate after applying animation but before saving the scene.",
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	("FinalMEL",
	 "The MEL script to evaluate after saving the scene.", 
	 null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("RootDAGNode");
      layout.addEntry("ImportSet");
      layout.addEntry("BeginFrame");
      layout.addSeparator();
      layout.addEntry("InitialMEL");
      layout.addEntry("ModelMEL");
      layout.addEntry("AnimMEL");
      layout.addEntry("FinalMEL");

      setSingleLayout(layout);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

    {
      ActionParam param = 
	new IntegerActionParam
	("Order", 
	 "The order in which to apply the imported animation.",
	 100);
      params.put(param.getName(), param);
    }
    
    return params;
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
    /* sanity checks */ 
    String rootDAG = null;
    String importSet = null;
    File initialMel = null;
    File modelMel = null;
    File animMel = null;
    File finalMel = null;
    TreeMap<String,File> modelFiles = new TreeMap<String,File>();
    TreeMap<Integer,TreeMap<String,File>> anims = new TreeMap<Integer,TreeMap<String,File>>();
    TreeMap<Integer,Integer> shotLengths = new TreeMap<Integer,Integer>();
    File saveScene = null;
    boolean isAscii = false;
    {
      {
	rootDAG = (String) getSingleParamValue("RootDAGNode"); 
	if((rootDAG == null) || (rootDAG.length() == 0))
	  throw new PipelineException 
	    ("The MayaCollate Action requires a valid Root DAG Node!");
      }
      
      {
	importSet = (String) getSingleParamValue("ImportSet"); 
	if((importSet == null) || (importSet.length() == 0))
	  throw new PipelineException 
	    ("The MayaCollate Action requires a valid Import Set!");
      }

      /* MEL script filenames */ 
      initialMel = getMelFile("InitialMEL", "Initial MEL", agenda);
      modelMel   = getMelFile("ModelMEL", "Model MEL", agenda);
      animMel    = getMelFile("AnimMEL", "Anim MEL", agenda);
      finalMel   = getMelFile("FinalMEL", "Final MEL", agenda);

      /* model and animation filenames */ 
      for(String sname : agenda.getSourceNames()) {
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);

	{
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  FilePattern fpat = fseq.getFilePattern();
	  String suffix = fpat.getSuffix();
	  if(fseq.isSingle() && (suffix != null)) {
	    if(suffix.equals("ma") || suffix.equals("mb")) {
	      String prefix = fpat.getPrefix();
	      if(modelFiles.containsKey(prefix)) 		
		throw new PipelineException
		  ("The MayaCollate Action requires that all model files have a unique " + 
		   "filename prefix!");

	      File path = new File(sname);
	      modelFiles.put(prefix, new File(path.getParent() + "/" + fseq.getFile(0)));
	    }
	  }

	  if(hasSourceParams(sname)) {
	    if(fseq.isSingle() && (suffix != null) && suffix.equals("anim")) {
	      Integer order = (Integer) getSourceParamValue(sname, "Order");
	      addAnimSeq(order, snodeID, agenda.getSourceActionInfo(sname), fseq, 
			 anims, shotLengths);
	    }
	    else {
	      throw new PipelineException
		("The MayaCollate Action requires that only single animation file " + 
		 "sequences set per-source parameters!");
	    }
	  }
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    String suffix = fseq.getFilePattern().getSuffix();
	    if(fseq.isSingle() && (suffix != null) && suffix.equals("anim")) {
	      Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	      addAnimSeq(order, snodeID, agenda.getSourceActionInfo(sname), fseq,
			 anims, shotLengths);
	    }
	    else {
	      throw new PipelineException
		("The MayaCollate Action requires that only single animation file " + 
		 "sequences set per-source parameters!");
	    }
	  }
	}
      }
      
      /* the generated Maya scene filename */ 
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
	  throw new PipelineException
	    ("The MayaCollate Action requires that the primary target file sequence must " + 
	     "be a single Maya scene file."); 
	
	isAscii = suffix.equals("ma");
	saveScene = new File(PackageInfo.sProdDir,
			     agenda.getNodeID().getWorkingParent() + "/" + fseq.getFile(0));
	
      }
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

      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
		"file -rename \"" + saveScene + "\";\n" + 
		"file -type \"" + (isAscii ? "mayaAscii" : "mayaBinary") + "\";\n\n");

      /* the initial MEL script */ 
      if(initialMel != null) {
	out.write("// INTITIAL MEL\n" + 
		  "source \"" + initialMel + "\";\n\n");
      }
      
      /* the model file reference imports */ 
      for(String prefix : modelFiles.keySet()) {
	File file = modelFiles.get(prefix);

	String format = "";
	if(file.toString().endsWith("ma")) 
	  format = "  -type \"mayaAscii\"\n";
	else if(file.toString().endsWith("mb")) 
	  format = "  -type \"mayaBinary\"\n";

	out.write
	  ("// MODEL: " + prefix + "\n" + 
	   "print \"Importing Reference Model: " + file + "\\n\";\n" + 
	   "file\n" +
	   "  -reference\n" + 
	   format +
	   "  -namespace \"LOADANIM" + prefix + "\"\n" + 
	   "  -options \"v=0\"\n" + 
	   "  \"$WORKING" + file + "\";\n" + 
	   "file\n" +
	   "  -reference\n" +
	   format +
	   "  -namespace \"" + prefix + "\"\n" + 
	   "  -options \"v=0\"\n" + 
	   "  \"$WORKING" + file + "\";\n" +
	   "\n\n");
      }
      
      /* the model MEL script */ 
      if(modelMel != null) {
	out.write("// MODEL MEL\n" + 
		  "source \"" + modelMel + "\";\n\n");
      }
      
      /* real number formatter */ 
      DecimalFormat fmt = new DecimalFormat("######0.0000");

      /* import the animation */ 
      double minFrame = -1.0;
      double maxFrame = -1.0;
      TreeMap<Integer,Double> shotStarts = new TreeMap<Integer,Double>();
      {
	/* the current start frame of the shot in the generated scene */ 
	double shotStart = 0.0; 
	{
	  Integer begin = (Integer) getSingleParamValue("BeginFrame"); 
	  if(begin != null) 
	    shotStart = (double) begin;

	  minFrame = shotStart; 
	}

	/* process the shots */ 
	for(Integer order : anims.keySet()) {
	  TreeMap<String,File> animFiles = anims.get(order);
	  double animLength = (double) shotLengths.get(order);

	  out.write("// SHOT (Order): " + order + "\n\n");

	  /* apply the animations for each element in the shot */ 
	  for(String prefix : animFiles.keySet()) {
	    String loadSet  = ("LOADANIM" + prefix + ":" + importSet);
	    String applySet = (prefix + ":" + importSet);

 	    out.write("// ELEMENT: " + prefix + "\n");

	    /* import the entire animation onto the WORKING model */ 
	    {
	      File file = animFiles.get(prefix);
	      out.write
		("print \"Importing Animation: " + file + "\\n\";\n" +
		 "select -r \"" + loadSet + "\";\n" +
		 "file\n" + 
		 "  -import\n" + 
		 "  -type \"animImport\"\n" + 
		 "  -options \";targetTime=1;time=0;copies=1;" + 
		 "option=replaceCompletely;pictures=0;connect=0;\"\n" + 
		 "  \"" + file + "\";\n" +
		 "select -r \"" + loadSet + "\";\n" +
		 "setInfinity -postInfinite linear;\n");
	    }

	    /* copy the animation from the WORKING model to FINAL model */ 
	    {
	      /* cut the animation for the specified range from the WORKING model */ 
	      out.write
		("select -r \"" + loadSet + "\";\n" +
		 "copyKey\n" +
		 "  -clipboard anim\n" +
		 "  -option curve\n" +
		 "  -hierarchy below\n" +
		 "  -animation objects\n" + 
		 "  -time \"0.0:" + fmt.format(animLength - 0.01) + "\"\n" +
		 "  -includeUpperBound on\n" +
		 "  -forceIndependentEulerAngles on\n" + 
		 "  -controlPoints off\n" +
		 "  -shape on;\n");       
	      
	      /* paste the cut animation onto the real model node */ 
	      out.write
		("select -r \"" + applySet + "\";\n" +
		 "pasteKey\n" + 
		 "  -clipboard anim\n" + 
		 "  -option insert\n" +
		 "  -time " + fmt.format(shotStart) + "\n" +
		 //		 "  -includeUpperBound on\n" + 
		 "  -copies 1;\n\n");  
	    }
	  }
	    
	  /* advance to the next shot in the sequence */ 
	  shotStarts.put(order, shotStart);
	  shotStart += animLength;
	}
	
	maxFrame = shotStart;
      }
      
      {
	out.write("// CLEAN UP\n"); 

	/* clean up the working model references */ 
	for(File mfile : modelFiles.values()) 
	  out.write("file -removeReference \"$WORKING" + mfile + "\";\n");
	
	/* general scene sanitation */ 
	out.write("source cleanUpScene;\n" + 
		  "deleteUnusedCommon(\"animCurve\", 0, \"Maya6 needs this...\");\n\n");
      }

      /* set visibility keys on the models */       
      {
	out.write("// VISIBILITY KEYS\n");
	for(Integer order : anims.keySet()) {
	  TreeSet<String> visible = new TreeSet<String>(anims.get(order).keySet());
	  for(String prefix : modelFiles.keySet()) {
	    out.write("setKeyframe -t " + fmt.format(shotStarts.get(order)) + 
		      " -v " + (visible.contains(prefix) ? "1" : "0") + 
		      " -at visibility \"" + prefix + ":" + rootDAG + "\";\n");
	  }
	}
	out.write("\n");
      }

      /* change range slider */ 
      if((minFrame < Integer.MAX_VALUE) && (maxFrame > -1))
	out.write("// FRAME RANGE\n" + 
		  "playbackOptions\n" + 
		  "  -minTime " + minFrame + "\n" +
		  "  -maxTime " + maxFrame + "\n" +
		  "  -animationStartTime " + minFrame + "\n" +
		  "  -animationEndTime " + maxFrame + ";\n\n");

      /* the anim MEL script */ 
      if(animMel != null) {
	out.write("// ANIM MEL\n" + 
		  "source \"" + animMel + "\";\n\n");
      }

      /* save the file */ 
      out.write("// SAVE\n" + 
		"print \"Saving Scene: " + saveScene + "\\n\";\n" + 
		"file -save;\n");

      /* the final MEL script */ 
      if(finalMel != null) {
	out.write("// FINAL MEL\n" + 
		  "source \"" + finalMel + "\";\n\n");
      }

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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the MEL file specified by the given parameter.
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
  private File 
  getMelFile
  (
   String pname, 
   String title, 
   ActionAgenda agenda
  ) 
    throws PipelineException 
  {
    File script = null; 
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
	  ("The MayaCollate Action requires that the source node specified by the " + 
	   title + " parameter (" + mname + ") must have a single MEL file as its " + 
	   "primary file sequence!");
      
      NodeID mnodeID = new NodeID(agenda.getNodeID(), mname);
      script = new File(PackageInfo.sProdDir,
			mnodeID.getWorkingParent() + "/" + fseq.getFile(0)); 
    }

    return script;	      
  }

  /**
   * Add the given source animation file sequence.
   */ 
  private void 
  addAnimSeq
  (
   Integer order,
   NodeID nodeID,
   ActionInfo info, 
   FileSeq fseq,
   TreeMap<Integer,TreeMap<String,File>> anims,
   TreeMap<Integer,Integer> shotLengths
  ) 
    throws PipelineException
  {
    if(order != null) {
      if((info == null) || !info.getName().equals("MayaAnimExport")) 
	throw new PipelineException
	  ("The MayaCollate Action requires that all animation file sequences where " + 
	   "generated by a MayaAnimExport action!");
      
      Integer firstFrame = (Integer) info.getSingleParamValue("FirstFrame");
      if(firstFrame == null)
	throw new PipelineException
	  ("Unable to determine the value of the First Frame single parameter of the " +
	   "MayaAnimExport action of the source node (" + nodeID + ")!");

      Integer lastFrame = (Integer) info.getSingleParamValue("LastFrame");  
      if(lastFrame == null)
	throw new PipelineException
	  ("Unable to determine the value of the Last Frame single parameter of the " +
	   "MayaAnimExport action of the source node (" + nodeID + ")!");

      int length = lastFrame - firstFrame + 1;
      Integer shotLength = shotLengths.get(order);
      if(shotLength == null) 
	shotLengths.put(order, length);
      else if(length != shotLength)
	throw new PipelineException
	  ("The MayaCollate Action requires that the length of all animations be identical " +
	   "for all file sequences with the same Order per-source parameter!");

      TreeMap<String,File> animFiles = anims.get(order);
      if(animFiles == null) {
	animFiles = new TreeMap<String,File>();
	anims.put(order, animFiles);
      }

      String prefix = fseq.getFilePattern().getPrefix();
      if(animFiles.containsKey(prefix)) 
	throw new PipelineException
	  ("All animation file sequences having the same Order must have unique filename " +
	   "prefixes!");
      
      File file = new File(PackageInfo.sProdDir,
			   nodeID.getWorkingParent() + "/" + fseq.getFile(0));
      animFiles.put(prefix, file);
    }
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1692142244129458356L;

}

