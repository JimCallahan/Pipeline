// $Id: MayaMelAction.java,v 1.1 2006/07/02 03:56:43 jim Exp $

package us.temerity.pipeline.plugin.v2_0_11;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M E L   A C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Loads a Maya scene, evaluates a set of MEL scripts and optionally saves the modified 
 * scene as the primary target file sequence of the node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file to load.  If this parameter is 
 *     not set, then no scene will be loaded.  This may be useful in the case where the 
 *     MEL scripts create the scene from scratch. <BR>
 *   </DIV> <BR>
 * 
 *   Save Result <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to save the Maya scene as the node's primary target after all MEL scrips have 
 *     been evaluated.  It may be desirable to not save the Maya scene when the MEL scripts
 *     generate output during their evaluation. <BR>
 *   </DIV><BR>
 *   
 *   Linear Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The linear unit that the generated scene will use. 
 *   </DIV> <BR>
 * 
 *   Angular Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The angular unit that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Time Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The unit of time and frame rate that the generated scene will use. 
 *   </DIV>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node which sets this parameter should have a MEL script as its primary
 *     file sequence which will be evaluated after the Maya scene file is loaded.  This 
 *     parameter determines the order of evaluation of the MEL scripts. If this parameter 
 *     is not set for a source node, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaMelAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaMelAction() 
  {
    super("MayaMEL", new VersionID("2.0.11"), "Temerity",
	  "Opens a Maya scene, runs the MEL script(s) and optionally saves the scene.");
    
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
	("SaveResult",
	 "Whether to save the post-MEL Maya scene.",
	 true);
      addSingleParam(param);
    }

    addSupport(OsType.MacOS);
    //addSupport(OsType.Windows);   // SHOULD WORK, BUT UNTESTED
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
	 "Evaluates the MEL script in this order.",
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */ 
    Path loadScene = null;
    Path saveScene = null; 
    boolean isAscii = false;
    TreeMap<Integer,LinkedList<Path>> mel = new TreeMap<Integer,LinkedList<Path>>();
    {
      /* generate the filename of the Maya scene to load */ 
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
	      ("The MayaMEL Action requires that the source node specified by the Maya " +
	       "Scene parameter (" + sname + ") must have a single Maya scene file as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  loadScene = new Path(PackageInfo.sProdPath,
			       snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
      }
	
      /* generate the name of the Maya scene to save */ 
      {
	Boolean save = (Boolean) getSingleParamValue("SaveResult"); 
	if((save != null) && save) {
	  FileSeq fseq = agenda.getPrimaryTarget();
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || 
	     (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
	    throw new PipelineException
	      ("The MayaMEL Action requires that the primary target file sequence must " + 
	       "be a single Maya scene file if the Save Result parameter is set!"); 
	  
	  isAscii = suffix.equals("ma");
	  saveScene = new Path(PackageInfo.sProdPath,
			       nodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
      }

      /* generate the table of MEL script files to evaluate */ 
      for(String sname : getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sname, "Order");
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow an per-source Order parameter exists for a node (" + sname + ") " + 
	     "which was not one of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !suffix.equals("mel"))
	  throw new PipelineException
	    ("The MayaMEL Action requires that the source node (" + sname + ") with " + 
	     "per-source Order parameter must have a single MEL script file as its " + 
	     "primary file sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	Path script = new Path(PackageInfo.sProdPath,
			       snodeID.getWorkingParent() + "/" + fseq.getPath(0));

	LinkedList<Path> scripts = mel.get(order);
	if(scripts == null) {
	  scripts = new LinkedList<Path>();
	  mel.put(order, scripts);
	}
	
	scripts.add(script);
      }
    }

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0755, "mel");
    try {      
      FileWriter out = new FileWriter(script);
    
      /* a workaround needed in "maya -batch" mode */ 
      out.write("// WORK AROUNDS:\n" + 
		"lightlink -q;\n\n");
      
      /* rename the current scene as the output scene */ 
      if(saveScene != null) {
	out.write("// SCENE SETUP\n" + 
		  "file -rename \"" + saveScene + "\";\n" + 
		  "file -type \"" + (isAscii ? "mayaAscii" : "mayaBinary") + "\";\n\n");
      }

      /* evaluate the MEL scripts */ 
      if(!mel.isEmpty()) {
	out.write("// MEL SCRIPTS \n");
	for(LinkedList<Path> scripts : mel.values()) {
	  for(Path spath : scripts) 
	    out.write("source \"" + spath + "\";\n");
	}
	out.write("\n");
      }

      /* save the file */ 
      if(saveScene != null) 
	out.write("// SAVE\n" + 
		  "file -save;\n");
      
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
      
      if(loadScene != null) {
	args.add("-file");
	args.add(loadScene.toOsString());
      }

      String program = "maya";
      if(PackageInfo.sOsType == OsType.Windows) 
	program = (program + ".exe");
      
      /* add the custom Mental Ray shader path to the environment */
      Map<String, String> env = agenda.getEnvironment();
      Map<String, String> nenv = env;
      String midefs = env.get("PIPELINE_MI_SHADER_PATH");
      if(midefs != null) {
	nenv = new TreeMap<String, String>(env);
	Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
	nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
      }

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 program, args, nenv, agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -442907818081700317L;

}

