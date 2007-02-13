// $Id: ConnectRefAnimAction.java,v 1.1 2007/02/13 05:27:17 jesse Exp $

package com.sony.scea.pipeline.plugins.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   I M P O R T   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Maya scene from component scenes. <P> 
 * 
 * A new empty scene is first created.  The component scenes are imported from each 
 * source node who's primary file sequence is a Maya scene file ("ma" or "mb").  
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
class ConnectRefAnimAction
  extends BaseAction
{  

/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

public
  ConnectRefAnimAction() 
  {
    super("ConnectReferencedAnim", new VersionID("1.0.0"), "SCEA",
	  "Builds a Maya scene with imported FBX anim.");
    
    underDevelopment();
    
    {
    	ActionParam param = 
    	new LinkActionParam
    	(aMayaSkelFile,
    	 "The Maya skeleton on which the animation will be applied.",
    	 null);
          addSingleParam(param);
    }
    
    {
    	ActionParam param = 
    	new LinkActionParam
    	(aMayaAnimFile,
    	 "The FBX animation to apply to the skeleton.",
    	 null);
          addSingleParam(param);
    }
    
    {
    	ActionParam param = 
    	new LinkActionParam
    	(aInitMEL,
    	 "The MEL script to evaluate after scene creation and before importing models.",
    	 null);
          addSingleParam(param);
    }
        
	{
		ActionParam param = 
    	new LinkActionParam
    	(aModelMEL,
    	 "The MEL script to evaluate after importing models but before saving the scene.",
    	 null);
          addSingleParam(param);
	}
        
	{
		ActionParam param = 
    	new LinkActionParam
    	(aFinalMEL,
    	 "The MEL script to evaluate after saving the scene.", 
    	 null);
          addSingleParam(param);
	}
        
	{
		LayoutGroup layout = new LayoutGroup(true);
		layout.addEntry(aMayaSkelFile);
      	layout.addEntry(aMayaAnimFile);
		layout.addEntry(aInitMEL);
      	layout.addEntry(aModelMEL);
      	layout.addEntry(aFinalMEL);

      	setSingleLayout(layout);
	}
        
    addSupport(OsType.MacOS);
    //addSupport(OsType.Windows);
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

    //{
    //  ActionParam param = 
	//new StringActionParam
	//("PrefixName",
	// "The namespace prefix for the referenced scene in Maya instead of the filename.",
	//null);
    // params.put(param.getName(), param);
    //}
  
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
  public SubProcessHeavy prep(ActionAgenda agenda, File outFile, File errFile) throws PipelineException
  {
    /* sanity checks */ 
    Path initialMel = null;
    Path modelMel = null;
    Path finalMel = null;
    //TreeMap<String,Path> modelPaths = new TreeMap<String,Path>();
    Path saveScene = null;
    boolean isAscii = false;
    String skelMayaFile = null;
    String mayaAnimFile = null;
    
    //if(agenda.getSourceNames().size() != 2) 
    //	throw new PipelineException ("number of source nodes must equal to two (2) !");
    	
    {
      /* MEL script filenames */ 
      initialMel = getMelPath(aInitMEL, "Initial MEL", agenda);
      modelMel   = getMelPath(aModelMEL, "Model MEL", agenda);
      finalMel   = getMelPath(aFinalMEL, "Final MEL", agenda);

      /* model filenames */
    	/*
      for(String sname : agenda.getSourceNames())
      {
    	  FileSeq fseq = agenda.getPrimarySource(sname);
    	  String suffix = fseq.getFilePattern().getSuffix();
    	  if(fseq.isSingle() && (suffix != null))
    	  {
    		  if(suffix.equals("ma") || suffix.equals("mb"))
    		  {
    			  //Path npath = new Path(sname);
    			  //modelPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
    			  skelMayaFile = sname;
    		  }
    		  if(suffix.equals("fbx"))
    		  {
    			  //Path npath = new Path(sname);
    			  //modelPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
    			  mayaAnimFile = sname;
    		  }
    	  }
      }
      */
      
      //grab maya skeleton path and make sure it's a valid one...
      {
    	  String skeletonName = (String) getSingleParamValue(aMayaSkelFile);
    	  if (skeletonName == null)
    		  throw new PipelineException("Valid Maya Skeleton file is required!");
    	  
    	  FileSeq fseq = agenda.getPrimarySource(skeletonName);
    	  String suffix = fseq.getFilePattern().getSuffix();
    	  if(fseq.isSingle() && (suffix != null))
    	  {
    		  if(suffix.equals("ma") || suffix.equals("mb"))
    		  {
    			  //get full path...
    			  NodeID sNodeID = new NodeID(agenda.getNodeID(), skeletonName);
    			  skelMayaFile = new Path(PackageInfo.sProdPath, sNodeID.getWorkingParent() + "/" + fseq.getPath(0)).toString();
    			  
    			  //skelMayaFile = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent() + "/" + fseq.getPath(0)).toString();
    		  }
    	  }
    	  if(skelMayaFile == null) 
    		  throw new PipelineException ("skeleton is not a valid Maya file!");
      }
      
//    grab fbx anim and make sure it's a valid one...
      {
    	  String mayaAnimName = (String) getSingleParamValue(aMayaAnimFile);
    	  if (mayaAnimName == null)
    		  throw new PipelineException("Valid Maya anim file is required!");
    	  
    	  FileSeq fseq = agenda.getPrimarySource(mayaAnimName);
    	  String suffix = fseq.getFilePattern().getSuffix();  
    	  if(fseq.isSingle() && (suffix != null))
    	  {
    		  if(suffix.equals("ma") || suffix.equals("mb"))
    		  {
    			  //get full path...
    			  NodeID sNodeID = new NodeID(agenda.getNodeID(), mayaAnimName);
    			  mayaAnimFile = new Path(PackageInfo.sProdPath, sNodeID.getWorkingParent() + "/" + fseq.getPath(0)).toString();
    			  
    			  //mayaAnimFile = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent() + "/" + fseq.getPath(0)).toString();
    		  }
    	  
    	  }
    	  if(mayaAnimFile == null) 
    		  throw new PipelineException ("animation is not a valid Maya file!");
      }
      /* the generated Maya scene filename */ 
      {
    	  FileSeq fseq = agenda.getPrimaryTarget();
    	  String suffix = fseq.getFilePattern().getSuffix();
	
    	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb")))  new PipelineException
    	  ("The MayaImport Action requires that the primary target file sequence " + "must be a single Maya scene file."); 
	
    	  isAscii = suffix.equals("ma");
    	  saveScene = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent() + "/" + fseq.getPath(0));
      }
    }

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0755, "mel");
    try {      
      FileWriter out = new FileWriter(script);

      /* a workaround needed in "maya -batch" mode */ 
      out.write("// WORK AROUNDS:\n" + 
		"lightlink -q;\n\n");
      
      /* load the fbx plugin */ 
      out.write("loadPlugin \"fbxmaya.so\";\n\n");
      
      /* load the animImportExport plugin */ 
      //out.write("loadPlugin \"animImportExport.so\";\n\n");

      /*reference in skeleton file with specific namespace*/
      String skelNamespace = "test";
      String animNamespace = "testAnim";
      
      /*set FBX options and disable prompt*/
      //out.write("eval FBXImportShowUI -v false;\n");
      //out.write("eval FBXImportMode -v exmerge;\n");
      
      /*import in fbx animation...*/
      //out.write("eval \"FBXImport -f \\\"" + mayaAnimFile + "\\\" -caller \\\"FBXMayaTranslator\\\"\";\n\n");
      
      /* rename the current scene as the output scene */ 
      isAscii = false; //force binary for now...
      	out.write("// SCENE SETUP\n" + 
		"file -rename \"" + saveScene + "\";\n" + 
		"file -type \"" + (isAscii ? "mayaAscii" : "mayaBinary") + "\";\n\n");
      
      	out.write("file -r -type \"mayaBinary\" -gl -namespace \"" + skelNamespace + "\"-options \"v=0\" -pr \"" + skelMayaFile + "\";\n\n");
      	out.write("file -r -type \"mayaAscii\" -gl -namespace \"" + animNamespace + "\"-options \"v=0\" -pr \"" + mayaAnimFile + "\";\n\n");

//WCT - temp hardcoding
      	out.write("string $selectedReference = \"testRN\";\n");
      	out.write("int $version = 7;\n");

      //Remove 'RN'
      	out.write("int $end = `size($selectedReference)` - 2;\n");
      	out.write("string $character = `substring $selectedReference 1 $end`;\n");
      //find characterAnimRN node
      	out.write("string $animRN = ($character + \"AnimRN\");\n");
      	out.write("if (`objExists($animRN)`){\n");
      	out.write("string $animCurves[];\n");
      //use correct command for Maya version
      	out.write("if ($version < 7){\n");
      	out.write("	string $fileName = `reference -rfn $animRN -q -f -ln`;\n");
      	out.write("	string $animCurvesFromRNNode[] = `reference -f $fileName -q -n`;\n");
      	out.write("	$animCurves = AWAppendStringsToStringArray($animCurves, $animCurvesFromRNNode);\n");
      	out.write("}else{\n");
      	out.write("	string $animCurvesFromRNNode[] = `referenceQuery -n $animRN`;\n");
		out.write("	$animCurves = AWAppendStringsToStringArray($animCurves, $animCurvesFromRNNode);\n");
		out.write("}\n");
		out.write("for ($aCurve in $animCurves){\n");
		out.write("	string $bigToken[];\n");
		out.write("	int $numTokens1 = `tokenize $aCurve \":\" $bigToken`;\n");
		out.write("	string $animNodeToken[];\n");
		out.write("	int $numTokens2 = `tokenize $bigToken[2] \"_\" $animNodeToken`;\n");
			
		out.write("	string $creature = $bigToken[1];\n");
		out.write("	string $joint = $animNodeToken[$numTokens2-2];\n");
		out.write("	string $axis = $animNodeToken[$numTokens2-1];\n");
		out.write("	if (!`objExists($creature + \":\" + $joint)`){\n");
				
		out.write("		$joint = ($animNodeToken[$numTokens2-3] + \"_\" + $animNodeToken[$numTokens2-2]);\n");
		out.write("		if (!`objExists($creature + \":\" + $joint)`){\n");
					
		out.write("			$joint = ($animNodeToken[$numTokens2-4] + \"_\" + $animNodeToken[$numTokens2-3] + \"_\" + $animNodeToken[$numTokens2-2]);\n");
					//check again
		out.write("			if (!`objExists($creature + \":\" + $joint)`){\n");
		out.write("				error (\"Had trouble parsing animCurve: '\" + $aCurve + \"'.\");\n");	
		out.write("			}\n");
		out.write("		}\n");
		out.write("	}\n");
			//make sure the attributes are unlocked
		out.write("	int $skip = 0;\n");
		out.write("	string $lockedAttrs[] = `listAttr -l ($creature + \":\" + $joint)`;\n");
		out.write("	if (`size($lockedAttrs)`){\n");
				//check for rotation or translation lock
		out.write("		for ($lockedAttr in $lockedAttrs){\n");
		out.write("			if (($lockedAttr == \"rotateX\") || ($lockedAttr == \"rotateY\") || ($lockedAttr == \"rotateZ\")){\n");
						//cannot unlock because creature is reference, give warning
		out.write("				$skip = 1;\n");
		out.write("			}\n");
		out.write("		}\n");
				//ok to connect?
		out.write("		if ($skip != 1){\n");
		out.write("			connectAttr ($aCurve+\".output\") ($creature+\":\"+$joint+\".\"+$axis);\n");

		out.write("		}\n");
		out.write("	}else{\n");
				//ok to connect
		out.write("		connectAttr ($aCurve+\".output\") ($creature+\":\"+$joint+\".\"+$axis);\n");
		out.write("	}\n");
	out.write("	}\n");
out.write("	}\n");      
      /* the initial MEL script */ 
      //if(initialMel != null) {
	//out.write("// INTITIAL MEL\n" + 
	//	  "source \"" + initialMel + "\";\n\n");
     // }
      
      /* the model file reference imports */ 
      
/*
	out.write
	  ("// MODEL: " + sname + "\n" + 
	   "print \"Importing Model: " + mpath + "\\n\";\n" + 
	   "file\n" +
	   "  -import\n" +
	   format +
	   "  -namespace \"" + nspace + "\"\n" + 
	   "  -options \"v=0\"\n" + 
	   "  \"$WORKING" + mpath + "\";\n" +
	   "\n\n");
      }
*/      
      /* the model MEL script */ 
      if(modelMel != null) {
	out.write("// MODEL MEL\n" + 
		  "source \"" + modelMel + "\";\n\n");
      }

      /* save the file */ 
      //String saveSceneName = saveScene.toOsString().replace("", "");
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
       new PipelineException
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

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 program, args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -2657360063696888249L;
  private static final String aMayaSkelFile = "Skeleton";
  private static final String aMayaAnimFile = "Anim";
  private static final String aInitMEL = "InitialMEL";
  private static final String aModelMEL = "ModelMEL";
  private static final String aFinalMEL = "FinalMEL";
}
