// $Id: MaxScriptAction.java,v 1.4 2007/05/17 16:54:45 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X   S C R I P T   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Loads a 3dsmax scene, evaluates a set of MAXScripts and optionally saves the modified 
 * scene as the primary target file sequence of the node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Max Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the 3dsmax scene file to load.  If this parameter is 
 *     not set, then no scene will be loaded.  This may be useful in the case where the 
 *     MAXScripts create the scene from scratch. <BR>
 *   </DIV> <BR>
 * 
 *   Use File Units <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to use the Max Scene's unit scale when loading the scene, ignoring 
 *     and replacing the System's unit scale.
 *   </DIV><BR>
 * 
 *   Save Result <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to save the 3dsmax scene as the node's primary target after all MAXScrips have 
 *     been evaluated.  It may be desirable to not save the Maya scene when the MAXScripts
 *     generate output during their evaluation. <BR>
 *   </DIV><BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain a MAXScript. 
 *     This parameter determines the order in which the MAXScripts evaluated.  If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MaxScriptAction
  extends MaxActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MaxScriptAction() 
  {
    super("MaxScript", new VersionID("2.2.1"), "Temerity",
	  "Opens a 3d Studio Max scene, runs a set of MAXScript(s) and optionally saves " + 
          "the resulting scene.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aMaxScene,
	 "The source 3dsmax scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aUseFileUnits,
	 "Whether to use the Max Scene's unit scale when loading the scene, ignoring " + 
         "and replacing the System's unit scale.", 
	 true);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new BooleanActionParam
	(aSaveResult,
	 "Whether to save the post-MAXScript 3dsmax scene.",
	 true);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMaxScene);
      layout.addEntry(aUseFileUnits);
      layout.addSeparator();
      layout.addEntry(aSaveResult);
      
      setSingleLayout(layout);
    }

    addSupport(OsType.Windows);  
    removeSupport(OsType.Unix);
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
	 "Each source node sequence which sets this parameter should contain a MAXScript. " +
         "This parameter determines the order in which the MAXScripts evaluated.  If " +
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
    /* the source 3dsmax scene */ 
    Path sourceScene = getMaxSceneSourcePath(aMaxScene, agenda);

    /* the target 3dsmax scene */
    Path targetScene = null;
    if(getSingleBooleanParamValue(aSaveResult))
      targetScene = getMaxSceneTargetPath(agenda);
    
    /* the MAXScripts to evaluate */ 
    MappedLinkedList<Integer,Path> sourceMsPaths = new MappedLinkedList<Integer,Path>();
    {
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, aOrder);
	  addMsPaths(agenda, sname, fseq, order, sourceMsPaths);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
            addMsPaths(agenda, sname, fseq, order, sourceMsPaths);
	  }
	}
      }

      if(sourceMsPaths.isEmpty()) 
	throw new PipelineException
	  ("No MAXScripts where specified using the per-source Order parameter!"); 
    }

    /* create a temporary MAXScript */ 
    File script = createTemp(agenda, "ms");
    try {
      FileWriter out = new FileWriter(script); 
      
      if(sourceScene != null) 
        out.write("loadMaxFile \"" + escPath(sourceScene) + "\" " +
                  "useFileUnits:" + getSingleBooleanParamValue(aUseFileUnits) + "\n"); 
      
      /* evaluate the MAXScripts */ 
      if(!sourceMsPaths.isEmpty()) {
	for(LinkedList<Path> paths : sourceMsPaths.values()) {
	  for(Path spath : paths) 
	    out.write("include \"" + escPath(spath) + "\"\n");
	}
	out.write("\n");
      }
      
      /* save the resulting file */ 
      if(targetScene != null) 
	out.write("saveMAXFile \"" + escPath(targetScene) + "\"\n"); 

      /* all done */
      out.write("quitMax #noPrompt");

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary MAXScript file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */
    return createMaxSubProcess(script, agenda, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating MAXScript filenames.
   */ 
  private void 
  addMsPaths
  (
   ActionAgenda agenda, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   MappedLinkedList<Integer,Path> sourceMsPaths
  )
    throws PipelineException 
  {
    if(order == null) 
      return;

    String suffix = fseq.getFilePattern().getSuffix();
    if(!fseq.isSingle() || (suffix == null) || !suffix.equals("ms"))
      throw new PipelineException
        ("The " + getName() + " Action requires that the file sequence (" + fseq + ") of " + 
         "the source node (" + sname + ") selected for evaluation must be a single " + 
         "MAXScript!"); 
      
    sourceMsPaths.put(order, getWorkingNodeFilePath(agenda, sname, fseq));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7740936650142327601L;

  public static final String aMaxScene     = "MaxScene"; 
  public static final String aUseFileUnits = "UseFileUnits"; 
  public static final String aSaveResult   = "SaveResult"; 
  public static final String aOrder        = "Order"; 

}

