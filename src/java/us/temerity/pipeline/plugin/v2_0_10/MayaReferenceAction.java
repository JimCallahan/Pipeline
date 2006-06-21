// $Id: MayaReferenceAction.java,v 1.1 2006/06/21 05:25:10 jim Exp $

package us.temerity.pipeline.plugin.v2_0_10;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E F E R E N C E   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Maya scene from component scenes. <P> 
 * 
 * A new empty scene is first created.  The component scenes are imported as Maya references 
 * from each source node who's primary file sequence is a Maya scene file ("ma" or "mb").  
 * 
 * At each stage in the process, an optional MEL script may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the Intial MEL, Model MEL and Final MEL single valued
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
 *      but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after saving the generated 
 *      Maya scene. <BR> 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Prefix Name <BR>
 *   <DIV style="margin-left: 40px;">
 *      The namespace prefix for the referenced scene in Maya instead of the filename.
 *   </DIV> <BR> 
 * </DIV> <P> 
 */
public
class MayaReferenceAction
  extends BaseAction
{  

/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaReferenceAction() 
  {
    super("MayaReference", new VersionID("2.0.10"), "Temerity",
	  "Builds a Maya scene from component scenes.");
    
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
	 "The MEL script to evaluate after importing models but before saving the scene.",
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
      layout.addEntry("InitialMEL");
      layout.addEntry("ModelMEL");
      layout.addEntry("FinalMEL");

      setSingleLayout(layout);
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
	new StringActionParam
	("PrefixName",
	 "The namespace prefix for the referenced scene in Maya instead of the filename.",
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
    Path initialMel = null;
    Path modelMel = null;
    Path finalMel = null;
    TreeMap<String,Path> modelPaths = new TreeMap<String,Path>();
    Path saveScene = null;
    boolean isAscii = false;
    {
      /* MEL script filenames */ 
      initialMel = getMelPath("InitialMEL", "Initial MEL", agenda);
      modelMel   = getMelPath("ModelMEL", "Model MEL", agenda);
      finalMel   = getMelPath("FinalMEL", "Final MEL", agenda);

      /* model filenames */ 
      for(String sname : agenda.getSourceNames()) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	String suffix = fseq.getFilePattern().getSuffix();
	if(fseq.isSingle() && (suffix != null)) {
	  if(suffix.equals("ma") || suffix.equals("mb")) {
	    Path npath = new Path(sname);
	    modelPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
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
	    ("The MayaReference Action requires that the primary target file sequence " + 
	     "must be a single Maya scene file."); 
	
	isAscii = suffix.equals("ma");
	saveScene = new Path(PackageInfo.sProdPath,
			     agenda.getNodeID().getWorkingParent() + "/" + fseq.getPath(0));
      }
    }

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0755, "mel");
    try {      
      FileWriter out = new FileWriter(script);

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
      for(String sname : modelPaths.keySet()) {
	Path mpath = modelPaths.get(sname);

	String nspace = null;
	if(getSourceParam(sname, "PrefixName") != null)
	  nspace = (String) getSourceParamValue(sname, "PrefixName");

	if(nspace == null) {
	  Path npath = new Path(sname);
	  nspace = npath.getName();
	}
	
	String format = "";
	{
	  String fname = mpath.getName();
	  if(fname.endsWith("ma")) 
	    format = "  -type \"mayaAscii\"\n";
	  else if(fname.endsWith("mb")) 
	    format = "  -type \"mayaBinary\"\n";
	}

	out.write
	  ("// MODEL: " + sname + "\n" + 
	   "print \"Importing Reference Model: " + mpath + "\\n\";\n" + 
	   "file\n" +
	   "  -reference\n" +
	   format +
	   "  -namespace \"" + nspace + "\"\n" + 
	   "  -options \"v=0\"\n" + 
	   "  \"$WORKING" + mpath + "\";\n" +
	   "\n\n");
      }
      
      /* the model MEL script */ 
      if(modelMel != null) {
	out.write("// MODEL MEL\n" + 
		  "source \"" + modelMel + "\";\n\n");
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
      
      String program = "maya";
      if(PackageInfo.sOsType == OsType.Windows) 
	program = (program + ".exe");

      /* added custom Mental Ray shader path to the environment */ 
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
	  ("The MayaCollate Action requires that the source node specified by the " + 
	   title + " parameter (" + mname + ") must have a single MEL file as its " + 
	   "primary file sequence!");
      
      NodeID mnodeID = new NodeID(agenda.getNodeID(), mname);
      script = new Path(PackageInfo.sProdPath,
			mnodeID.getWorkingParent() + "/" + fseq.getPath(0)); 
    }

    return script;	      
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1932790132693664097L;

}

