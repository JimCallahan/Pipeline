// $Id: MayaCollateAction.java,v 1.1 2004/12/05 01:30:28 jim Exp $

package us.temerity.pipeline.plugin.v1_2_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C O L L A T E   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Maya scene from component scenes and imported animation data. <P> 
 * 
 * A new empty scene is first created.  The component scenes are imported as Maya references 
 * from each source node who's primary file sequence is a Maya scene file ("ma" or "mb").  
 * Then the animation data is imported from each source node who's primary file sequence is 
 * a Maya animation file ("anim") and which sets the per-source parameters below.  This 
 * animation data is then applied objects in the to the generated scene. <P> 
 * 
 * The animation data is composed of a series of shots.  Each shot contains one or more 
 * animations of equal frame length (End Frame - Start Frame) and which share the same 
 * Shot Order.  Shots are processed in lowest to higest Shot Order.  The animations which 
 * make up the first shot (lowest Shot Order) are applied to the generated Maya scene
 * starting at Begin Frame. Subsequent shots are concatented after each previous shot. <P> 
 * 
 * At each stage in the process, an optional MEL script may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the Intial MEL, Model MEL, Anim MEL and Final MEL single valued 
 * parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate just after scene creation
 *      and before importing any models.
 *   </DIV> <BR>
 * 
 *   Model MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after importing all models,
 *      but before loading and applying any animation data.
 *   </DIV> <BR>
 * 
 *   Anim MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after loading and applying all
 *      animation data, but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after saving the generated 
 *      Maya scene.
 *   </DIV> <BR>
 * 
 *   Begin Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *      The frame number of the first animation keyframe in the generated Maya scene. 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shot Order <BR>
 *   <DIV style="margin-left: 40px;">
 *      Determines the order in which the animations are concatenated.  Animations are 
 *      processed from least to greatest Shot Order.  All animations with the same Shot Order
 *      are applied to the same frame range in the generate Maya scene.  Therefore, the 
 *      Anim Offset and Anim Length parameters must be identical for all animations which
 *      share the same Order.
 *   </DIV> <BR> 
 * 
 *   Offset <BR>
 *   <DIV style="margin-left: 40px;">
 *      The number of frames of animation to ignore from the start of the imported animation.
 *      Note that the frame number of the start of the animation is not considered, as the
 *      Offset is measured relative to the first keyframe of animation.  For example, if the 
 *      first keyframe in the animation file was at frame (15) and the Offset was (2), then 
 *      the first imported keyframe would be from frame (17) in the animation file.
 *   </DIV> <BR> 
 * 
 *   Length <BR>
 *   <DIV style="margin-left: 40px;">
 *      The number of frames of animation to import starting at the frame determined by the 
 *      Offset in the animation file.  For example, if the first keyframe in the animation 
 *      file was at frame (10), the Offset was (2) and the Length was (3) then the keyframes
 *      in the range [12, 14.99] would be imported from the animation file.
 *   </DIV> <BR> 
 * 
 *   Root DAG Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     The root Maya DAG node to which the imported animation will be applied.  Each imported
 *     model will be placed in its own Maya namespace consisting of the prefix of the last 
 *     component of the Pipeline source node for the model.  When specifying Root DAG Node, 
 *     this namespace must be used in the name of the Maya DAG node.
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
    super("MayaCollate", new VersionID("1.2.0"), 
	  "Builds a Maya scene from component scenes and animation files.");
    
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
      ActionParam param = 
	new IntegerActionParam
	("BeginFrame",
	 "The start frame of animation in the generated Maya scene.", 
	 0);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("InitialMEL");
      layout.addEntry("ModelMEL");
      layout.addEntry("AnimMEL");
      layout.addEntry("FinalMEL");
      layout.addSeparator();
      layout.addEntry("BeginFrame");

      setSingleLayout(layout);
    }
    
    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add("ShotOrder");
      layout.add("Offset");
      layout.add("Length");
      layout.add("RootDAGNode");

      setSourceLayout(layout);
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
	("ShotOrder", 
	 "The order in which to apply the imported animation.",
	 100);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	("Offset", 
	 "The number of frames of animation to ignore from the start of the " + 
	 "animation file.",
	 0);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("Length", 
	 "The number of frames of animation to import starting at the frame determined " + 
	 "by the Offset in the animation file.",
	 30);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("RootDAGNode",
	 "The root Maya DAG node to which animation will be applied.", 
	 null);
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
    File initialMel = null;
    File modelMel = null;
    File animMel = null;
    File finalMel = null;
    TreeMap<String,File> modelFiles = new TreeMap<String,File>();
    TreeMap<Integer,TreeMap<String,File>> anims = 
      new TreeMap<Integer,TreeMap<String,File>>();
    File saveScene = null;
    boolean isAscii = false;
    {
      /* MEL script filenames */ 
      initialMel = getMelFile("InitialMEL", "Initial MEL", agenda);
      modelMel   = getMelFile("ModelMEL", "Model MEL", agenda);
      animMel    = getMelFile("AnimMEL", "Anim MEL", agenda);
      finalMel   = getMelFile("FinalMEL", "Final MEL", agenda);

      /* model and animation filenames */ 
      for(String sname : agenda.getSourceNames()) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	String suffix = fseq.getFilePattern().getSuffix();
	if(fseq.isSingle() && (suffix != null)) {
	  NodeID snodeID = new NodeID(agenda.getNodeID(), sname);

	  if(suffix.equals("ma") || suffix.equals("mb")) {
	    File path = new File(sname);
	    File file = new File(path.getParent() + "/" + fseq.getFile(0));
	    modelFiles.put(sname, file);
	  }
	  else if(suffix.equals("anim") && hasSourceParams(sname)) {
	    Integer order = (Integer) getSourceParamValue(sname, "ShotOrder");
	    if(order != null) {
	      TreeMap<String,File> animFiles = anims.get(order);
	      if(animFiles == null) {
		animFiles = new TreeMap<String,File>();
		anims.put(order, animFiles);
	      }

	      File file = new File(PackageInfo.sProdDir,
				   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	      animFiles.put(sname, file);
	    }
	    else if(agenda.getSourceNames().contains(sname)) {
	      throw new PipelineException
		("The animation source node (" + sname + ") did not set the Order " + 
		 "per-source parameter!");
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
    File script = createTemp(agenda, 0755, "mel");
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
      for(String mname : modelFiles.keySet()) {
	File file = modelFiles.get(mname);

	File path = new File(mname);
	String nspace = path.getName();

	String format = "";
	if(file.toString().endsWith("ma")) 
	  format = "  -type \"mayaAscii\"\n";
	else if(file.toString().endsWith("mb")) 
	  format = "  -type \"mayaBinary\"\n";

	out.write
	  ("// MODEL: " + mname + "\n" + 
	   "print \"Importing Reference Model: " + file + "\\n\";\n" + 
	   "file\n" +
	   "  -reference\n" + 
	   format +
	   "  -namespace \"LOADANIM" + nspace + "\"\n" + 
	   "  -options \"v=0\"\n" + 
	   "  \"$WORKING" + file + "\";\n" + 
	   "file\n" +
	   "  -reference\n" +
	   format +
	   "  -namespace \"" + nspace + "\"\n" + 
	   "  -options \"v=0\"\n" + 
	   "  \"$WORKING" + file + "\";\n" +
	   "\n\n");
      }
      
      /* the model MEL script */ 
      if(modelMel != null) {
	out.write("// MODEL MEL\n" + 
		  "source \"" + modelMel + "\";\n\n");
      }
      
      /* hidden data node attributes */ 
      ArrayList<String>  nodeNames    = new ArrayList<String>();
      ArrayList<String>  rootDAGNodes = new ArrayList<String>();
      ArrayList<Integer> shotIndices  = new ArrayList<Integer>();
      ArrayList<Double>  shotStarts   = new ArrayList<Double>();
      ArrayList<Double>  shotEnds     = new ArrayList<Double>();
      ArrayList<Integer> shotOrders   = new ArrayList<Integer>();
      
      /* import the animation */ 
      double minFrame = -1.0;
      double maxFrame = -1.0;
      {
	/* the current start frame of the shot in the generated scene */ 
	double shotStart = 0.0; 
	double shotEnd   = 0.0;
	{
	  Integer begin = (Integer) getSingleParamValue("BeginFrame"); 
	  if(begin != null) 
	    shotStart = (double) begin;

	  minFrame = shotStart; 
	}

	/* real number formatter */ 
	DecimalFormat fmt = new DecimalFormat("######0.0000");

	/* process the shots */ 
	int shotIdx = 0;
	for(Integer order : anims.keySet()) {
	  TreeMap<String,File> animFiles = anims.get(order);

	  boolean firstElement = true;
	  Integer animLength = null;
	  
	  out.write("// SHOT (Order): " + order + "\n\n");
	  
	  /* apply the animations for each element in the shot */ 
	  for(String sname : animFiles.keySet()) {
	    
	    /* get per-source parameter values */ 
	    Integer offset = null;
	    Integer length = null;
	    String rootDagNode = null;
	    {
	      offset = (Integer) getSourceParamValue(sname, "Offset");
	      if(offset == null) 
		throw new PipelineException
		  ("The Offset per-source parameter for source node " + 
		   "(" + sname + ") was not set!");
	      if(offset < 0) 
		throw new PipelineException
		  ("The Offset per-source parameter for source node " + 
		   "(" + sname + ") cannot be negative!");
	      
	      length = (Integer) getSourceParamValue(sname, "Length");
	      if(length == null) 
		throw new PipelineException
		  ("The Length per-source parameter for source node " + 
		   "(" + sname + ") was not set!");
	      if(length < 1) 
		throw new PipelineException
		  ("The Length per-source parameter for source node " + 
		   "(" + sname + ") must be positive!");
	      
	      if(animLength == null) {
		animLength = length;
		shotEnd = shotStart + ((double) animLength);
	      }
	      else if(!animLength.equals(length)) {
		throw new PipelineException
		  ("All animations with the same Shot Order must have the same Length. " +
		   "The animation (" + sname + ") in shot (" + order + ") had a Length of " + 
		   "(" + length + ") frames, but previous animations " + 
		   "for this shot had a Length of (" + animLength + ") frames!");
	      }

	      rootDagNode = (String) getSourceParamValue(sname, "RootDAGNode");
	      if((rootDagNode == null) || (rootDagNode.length() == 0)) 
		throw new PipelineException
		  ("The Root DAG Node per-source parameter for source node " + 
		   "(" + sname + ") was not set!");
	    }
		  
	    /* set the hidden data node attributes */ 
	    {
	      nodeNames.add(sname);
	      rootDAGNodes.add(rootDagNode);
	      shotIndices.add(shotIdx);

	      if(firstElement) {
		shotStarts.add(shotStart);
		shotEnds.add(shotEnd);
		shotOrders.add(order);
	      }

	      firstElement = false;
	    }
	      
	    out.write("// ELEMENT: " + sname + "\n");

	    /* import the entire animation onto the WORKING model */ 
	    {
	      File file = animFiles.get(sname);

	      out.write
		("print \"Importing Animation: " + file + "\\n\";\n" +
		 "select -r LOADANIM" + rootDagNode + ";\n" +
		 "file\n" + 
		 "  -import\n" + 
		 "  -type \"animImport\"\n" + 
		 "  -options \";targetTime=1;time=0;copies=1;" + 
		 "option=replaceCompletely;pictures=0;connect=0;\"\n" + 
		 "  \"" + file + "\";\n");	       
	    }
	    
	    /* set the post-infinity type to "linear" */ 
	    out.write
	      ("setInfinity -hierarchy \"below\" -postInfinite linear " + 
	       "LOADANIM" + rootDagNode + ";\n");

	    /* copy the animation from the WORKING model to FINAL model */ 
	    {
	      /* the frame range to cut */ 
	      double a = (double) offset;
	      double b = a + ((double) animLength) - 0.01;

	      /* cut the animation for the specified range from the WORKING model */ 
	      out.write
		("select -r LOADANIM" + rootDagNode + ";\n" +
		 "copyKey\n" +
		 "  -clipboard anim\n" +
		 "  -option curve\n" +
		 "  -hierarchy below\n" +
		 "  -animation objects\n" + 
		 "  -time \"" + fmt.format(a) + ":" + fmt.format(b) + "\"\n" +
		 "  -includeUpperBound on\n" +
		 "  -forceIndependentEulerAngles on\n" + 
		 "  -controlPoints off\n" +
		 "  -shape on;\n");       
	      
	      /* paste the cut animation onto the real model node */ 
	      out.write
		("select -r " + rootDagNode + ";\n" +
		 "pasteKey\n" + 
		 "  -clipboard anim\n" + 
		 "  -option insert\n" +
		 "  -time " + fmt.format(shotStart) + "\n" +
		 "  -includeUpperBound on\n" + 
		 "  -copies 1;\n\n");  
	    }
	  }
	    
	  /* advance to the next shot in the sequence */ 
	  shotStart = shotEnd;
	  maxFrame = Math.max(maxFrame, shotEnd);
	  shotIdx++;
	}
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

	DecimalFormat fmt = new DecimalFormat("######0.0000");
	TreeSet<String> allDAGs = new TreeSet(rootDAGNodes);

	int shotIdx = 0;
	for(Integer order : anims.keySet()) {
	  TreeMap<String,File> animFiles = anims.get(order);
	  
	  TreeSet<String> visibleDAGs = new TreeSet<String>();
	  for(String sname : animFiles.keySet()) 
	    visibleDAGs.add((String) getSourceParamValue(sname, "RootDAGNode"));
	  
	  for(String dag : allDAGs) {
	    boolean visible = visibleDAGs.contains(dag);
	    out.write("setKeyframe -t " + fmt.format(shotStarts.get(shotIdx)) + 
		      " -v " + (visible ? "1" : "0") + " -at visibility " + dag + ";\n");
	  }

	  shotIdx++;
	}
	out.write("\n");
      }

      /* the hidden data node */ 
      {
	out.write
	  ("// HIDDEN DATA NODE\n" + 
	   "print \"Setting PipelineMayaCollate..\\n\";\n" +
	   "if(size(`ls PipelineMayaCollate`) > 0) {\n" +
	   "  lockNode -lock off PipelineMayaCollate;\n" + 
	   "  delete PipelineMayaCollate;\n" + 
	   "}\n" + 
	   "createNode \"unknown\" -name \"PipelineMayaCollate\";\n" +
	   "addAttr -sn \"sceneNodeName\" -ln \"sceneNodeName\" -dt \"string\";\n" +
	   "addAttr -sn \"numberOfElements\" -ln \"numberOfElements\";\n" +
	   "addAttr -m -sn \"nodeName\" -ln \"nodeName\" -dt \"string\";\n" + 
	   "addAttr -m -sn \"rootDAGNode\" -ln \"rootDAGNode\" -dt \"string\";\n" + 
	   "addAttr -m -sn \"shotIndex\" -ln \"shotIndex\";\n" + 
	   "addAttr -sn \"numberOfShots\" -ln \"numberOfShots\";\n" +
	   "addAttr -m -sn \"shotStart\" -ln \"shotStart\";\n" + 
	   "addAttr -m -sn \"shotLength\" -ln \"shotEnd\";\n" + 
	   "addAttr -m -sn \"shotOrder\" -ln \"shotOrder\";\n" +

	   "setAttr \"PipelineMayaCollate.sceneNodeName\" -type \"string\" \"" + 
	   agenda.getNodeID().getName() + "\";\n" +
	
	   "setAttr \"PipelineMayaCollate.numberOfElements\" " + 
	   nodeNames.size() + ";\n");

	if(nodeNames.size() > 0) {
	  out.write
	    ("setAttr -size " + nodeNames.size() + " " + 
	     "\"PipelineMayaCollate.nodeName[0:" + (nodeNames.size()-1) + "]\" " +
	     "-type \"string\"");

	  for(String name : nodeNames) 
	    out.write("\n  \"" + name + "\"");
	  out.write(";\n");
	}
	  
	if(rootDAGNodes.size() > 0) {
	  out.write
	    ("setAttr -size " + rootDAGNodes.size() + " " + 
	     "\"PipelineMayaCollate.rootDAGNode[0:" + (rootDAGNodes.size()-1) + "]\" " +
	     "-type \"string\"");

	  for(String dag : rootDAGNodes) 
	    out.write("\n  \"" + dag + "\"");
	  out.write(";\n");
	}

	if(shotIndices.size() > 0) {
	  out.write
	    ("setAttr -size " + shotIndices.size() + " " + 
	     "\"PipelineMayaCollate.shotIndex[0:" + (shotIndices.size()-1) + "]\"");

	  for(Integer shotIdx : shotIndices) 
	    out.write("\n  " + shotIdx);
	  out.write(";\n");
	}

	out.write
	  ("setAttr \"PipelineMayaCollate.numberOfShots\" " + shotStarts.size() + ";\n");

	{
	  DecimalFormat fmt = new DecimalFormat("######0.0000");

	  if(shotStarts.size() > 0) {	  
	    out.write
	      ("setAttr -size " + shotStarts.size() + " " +
	       "\"PipelineMayaCollate.shotStart[0:" + (shotStarts.size()-1) + "]\"");

	    for(Double shotStart : shotStarts) 
	      out.write("\n  " + fmt.format(shotStart)); 
	    out.write(";\n");
	  }

	  if(shotEnds.size() > 0) {
	    out.write
	      ("setAttr -size " + shotEnds.size() + " " +
	       "\"PipelineMayaCollate.shotEnd[0:" + (shotEnds.size()-1) + "]\"");

	    for(Double shotEnd : shotEnds) 
	      out.write("\n  " + fmt.format(shotEnd)); 
	    out.write(";\n");
	  }
	}

	if(shotOrders.size() > 0) {
	  out.write
	    ("setAttr -size " + shotOrders.size() + " " + 
	     "\"PipelineMayaCollate.shotOrder[0:" + (shotOrders.size()-1) + "]\"");
	  
	  for(Integer order : shotOrders) 
	    out.write("\n  " + order);
	  out.write(";\n");
	}

	out.write("setAttr -lock on PipelineMayaCollate.sceneNodeName;\n" + 
		  "setAttr -lock on PipelineMayaCollate.numberOfElements;\n" + 
		  "setAttr -lock on PipelineMayaCollate.nodeName;\n" + 
		  "setAttr -lock on PipelineMayaCollate.rootDAGNode;\n" + 
		  "setAttr -lock on PipelineMayaCollate.shotIndex;\n" + 
		  "setAttr -lock on PipelineMayaCollate.numberOfShots;\n" + 
		  "setAttr -lock on PipelineMayaCollate.shotStart;\n" + 
		  "setAttr -lock on PipelineMayaCollate.shotEnd;\n" + 
		  "setAttr -lock on PipelineMayaCollate.shotOrder;\n" + 
		  "lockNode -lock on PipelineMayaCollate;\n\n");
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


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 677210692602296168L;

}

