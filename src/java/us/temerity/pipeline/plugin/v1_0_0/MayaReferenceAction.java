// $Id: MayaReferenceAction.java,v 1.3 2004/10/28 15:55:24 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

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
    super("MayaReference", new VersionID("1.0.0"), 
	  "Builds a Maya scene from component scenes.");
    
    {
      BaseActionParam param = 
	new LinkActionParam
	("InitialMEL",
	 "The MEL script to evaluate after scene creation and before importing models.",
	 null);
      addSingleParam(param);
    }
    
    {
      BaseActionParam param = 
	new LinkActionParam
	("ModelMEL",
	 "The MEL script to evaluate after importing models but before saving the scene.",
	 null);
      addSingleParam(param);
    }
    
    {
      BaseActionParam param = 
	new LinkActionParam
	("FinalMEL",
	 "The MEL script to evaluate after saving the scene.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> layout = new ArrayList<String>();
      layout.add("InitialMEL");
      layout.add("ModelMEL");
      layout.add("FinalMEL");

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
    makeTargetDir(agenda);

    /* sanity checks */ 
    File initialMel = null;
    File modelMel = null;
    File finalMel = null;
    TreeMap<String,File> modelFiles = new TreeMap<String,File>();
    File saveScene = null;
    boolean isAscii = false;
    {
      /* MEL script filenames */ 
      initialMel = getMelFile("InitialMEL", "Initial MEL", agenda);
      modelMel   = getMelFile("ModelMEL", "Model MEL", agenda);
      finalMel   = getMelFile("FinalMEL", "Final MEL", agenda);

      /* model filenames */ 
      for(String sname : agenda.getSourceNames()) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	String suffix = fseq.getFilePattern().getSuffix();
	if(fseq.isSingle() && (suffix != null)) {
	  if(suffix.equals("ma") || suffix.equals("mb")) {
	    File path = new File(sname);
	    File file = new File(path.getParent() + "/" + fseq.getFile(0));
	    modelFiles.put(sname, file);
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
	saveScene = new File(PackageInfo.sProdDir,
			     agenda.getNodeID().getWorkingParent() + "/" + fseq.getFile(0));
	
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
      for(String mname : modelFiles.keySet()) {
	File file = modelFiles.get(mname);

	File path = new File(mname);
	String nspace = path.getName();

	out.write
	  ("// MODEL: " + mname + "\n" + 
	   "print \"Importing Reference Model: " + file + "\\n\";\n" + 
	   "file\n" +
	   "  -reference\n" +
	   "  -type \"mayaAscii\"\n" +
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

  private static final long serialVersionUID = -4751775228919961267L;

}

