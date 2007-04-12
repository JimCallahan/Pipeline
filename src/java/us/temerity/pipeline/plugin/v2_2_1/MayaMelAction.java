// $Id: MayaMelAction.java,v 1.3 2007/04/12 12:31:27 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

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
 *     Each source node sequence which sets this parameter should contain a MEL script. 
 *     This parameter determines the order in which the MEL scripts evaluated.  If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaMelAction
  extends MayaActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaMelAction() 
  {
    super("MayaMEL", new VersionID("2.2.1"), "Temerity",
	  "Opens a Maya scene, runs the MEL script(s) and optionally saves the scene.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene,
	 "The source Maya scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aSaveResult,
	 "Whether to save the post-MEL Maya scene.",
	 true);
      addSingleParam(param);
    }

    addUnitsParams();

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aSaveResult);
      layout.addSeparator();
      addUnitsParamsToLayout(layout); 
      
      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);  
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
	(aOrder, 
	 "Each source node sequence which sets this parameter should contain a MEL script. " +
         "This parameter determines the order in which the MEL scripts evaluated.  If " +
         "this parameter is not set for a source node file sequence, it will be ignored.", 
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
    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);

    /* the target Maya scene (optional) */
    Path targetScene = null; 
    String sceneType = null;
    if(getSingleBooleanParamValue(aSaveResult)) {
      targetScene = getMayaSceneTargetPath(agenda);
      sceneType = getMayaSceneType(agenda);
    }
    
    /* the MEL scripts to evaluate */ 
    MappedLinkedList<Integer,Path> sourceMelPaths = new MappedLinkedList<Integer,Path>();
    {
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, aOrder);
	  addMelPaths(agenda, sname, fseq, order, sourceMelPaths);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
            addMelPaths(agenda, sname, fseq, order, sourceMelPaths);
	  }
	}
      }

      if(sourceMelPaths.isEmpty()) 
	throw new PipelineException
	  ("No MEL scripts where specified using the per-source Order parameter!"); 
    }

    /* create a temporary MEL script */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script); 
      
      /* a workaround needed in "maya -batch" mode (at least in Maya-7.0) */ 
      out.write("// WORK AROUNDS:\n" + 
		"lightlink -q;\n\n");

      /* rename the current scene as the output scene */ 
      if(targetScene != null) {
	out.write("// SCENE SETUP\n" + 
		  "file -rename \"" + targetScene + "\";\n" + 
		  "file -type \"" + sceneType + "\";\n\n");
      }
      
      /* evaluate the MEL scripts */ 
      if(!sourceMelPaths.isEmpty()) {
	out.write("// MEL SCRIPTS \n");
	for(LinkedList<Path> paths : sourceMelPaths.values()) {
	  for(Path spath : paths) 
	    out.write("source \"" + spath + "\";\n");
	}
	out.write("\n");
      }
      
      /* save the file */ 
      if(targetScene != null) 
	out.write("// SAVE\n" + 
		  "file -save;\n");

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */
    return createMayaSubProcess(sourceScene, script, true, agenda, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating MEL script filenames.
   */ 
  private void 
  addMelPaths
  (
   ActionAgenda agenda, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   MappedLinkedList<Integer,Path> sourceMelPaths
  )
    throws PipelineException 
  {
    if(order == null) 
      return;

    String suffix = fseq.getFilePattern().getSuffix();
    if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mel"))
      throw new PipelineException
        ("The " + getName() + " Action requires that the file sequence (" + fseq + ") of " + 
         "the source node (" + sname + ") selected for evaluation must be a single MEL " + 
         "script!"); 
      
    sourceMelPaths.put(order, getWorkingNodeFilePath(agenda, sname, fseq));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8661691577988538631L;

  public static final String aMayaScene  = "MayaScene";
  public static final String aSaveResult = "SaveResult"; 
  public static final String aOrder      = "Order"; 

}

