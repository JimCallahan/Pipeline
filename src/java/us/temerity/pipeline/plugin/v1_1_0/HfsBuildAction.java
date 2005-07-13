// $Id: HfsBuildAction.java,v 1.1 2005/07/13 13:52:07 jim Exp $

package us.temerity.pipeline.plugin.v1_1_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   B U I L D   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Builds a new Houdini scene from a set of component Houdini Scenes. <P> 
 * 
 * This action provides a convienent way of building a Houdini scene from components parts
 * procedurally using hscript(1).  The per-source parameters control the order these 
 * scenes are loaded and how common sections are merged.  Houdini command scripts may also be 
 * executed at various stages in the process.<P> 
 * 
 * See the <A href="http://www.sidefx.com"><B>Houdini</B></A> documentation for details about
 * hscript(1) and the "mread" and "mwrite" commands used by this action. <P> 
 *  
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Pre Build Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate before loading any
 *     source scenes. <BR>
 *   </DIV> <BR>
 * 
 *   Post Build Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate after loading all
 *     source scenes. <BR>
 *   </DIV> <BR>
 * 
 *   Pre Scene Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate before loading each 
 *     source scene. <BR>
 *   </DIV> <BR>
 * 
 *   Post Scene Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate after loading each 
 *     source scene. <BR>
 *   </DIV> <BR> 
 * 
 *   Use Graphical License<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to use an interactive graphical Houdini license when running hscript(1).  
 *     Normally, hscript(1) is run using a non-graphical license (-R option).  A graphical 
 *     license may be required if the site has not obtained any non-graphical licenses.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node which sets this parameter should have a Houdini scene file as the
 *     sole member of the selected file sequence.  This parameter specifies the order in 
 *     which will this scene will be loaded. If this parameter is not set for a source node, 
 *     it will be ignored.
 *   </DIV> 
 * 
 *   Merge Pattern <BR>
 *   <DIV style="margin-left: 40px;">
 *     This parameter species which portions of the Houdini scene should be merged into
 *     the newly generated scene. The first scene loaded, as determined by Order, will always
 *     replace the entire contents of the current scene ignoring this parameter.  A pattern 
 *     of "*" indicates that the entire contents should be loaded.  Existing components in 
 *     the generated scene will be overridden by components matching this pattern in the 
 *     loaded scene.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class HfsBuildAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsBuildAction() 
  {
    super("HfsBuild", new VersionID("1.1.0"), 
	  "Builds a new Houdini scene from a set of component Houdini Scenes.");

    {
      ActionParam param = 
	new LinkActionParam
	("PreBuildScript",
	 "The pre-build command script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PostBuildScript",
	 "The post-build command script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PreSceneScript",
	 "The pre-scene command script.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	("PostSceneScript",
	 "The post-scene command script.", 
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
      layout.addEntry("UseGraphicalLicense");

      {
	LayoutGroup scripts = new LayoutGroup
	  ("Command Scripts", 
	   "Houdini command scripts run at various stages of the scene building process.", 
	   true);
	scripts.addEntry("PreBuildScript"); 
	scripts.addEntry("PostBuildScript");
	scripts.addSeparator();
	scripts.addEntry("PreSceneScript"); 
	scripts.addEntry("PostSceneScript"); 

	layout.addSubGroup(scripts);
      }

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
	 "Loads the Houdini scene in this order.", 
	 100);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("MergePattern", 
	 "Specifies the pattern used to select the components to be merged into the " +
	 "current scene.", 
	 "*");
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
    File targetScene = null; 
    ArrayList<File> sourceScenes = new ArrayList<File>();
    ArrayList<String> mergePatterns = new ArrayList<String>();
    File preBuild = null;
    File postBuild = null;
    File preScene = null;
    File postScene = null;
    {
      DoubleMap<Integer,String,TreeSet<FileSeq>> sources = 
	new DoubleMap<Integer,String,TreeSet<FileSeq>>();

      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourceSeq(order, sname, fseq, sources);
	}
	
	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = 
	      (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourceSeq(order, sname, fseq, sources);
	  }
	}
      }

      for(Integer order : sources.keySet()) {
	for(String sname : sources.get(order).keySet()) {
	  for(FileSeq fseq : sources.get(order).get(sname)) {
	    String pattern = null;
	    if(fseq.equals(agenda.getPrimarySource(sname))) 
	      pattern = (String) getSourceParamValue(sname, "MergePattern");
	    else {
	      FilePattern fpat = fseq.getFilePattern();
	      pattern = (String) getSecondarySourceParamValue(sname, fpat, "MergePattern");
	    }

	    if((pattern == null) || (pattern.length() == 0)) 
	      throw new PipelineException
		("The Merge Pattern for file sequence (" + fseq + ") of source node " + 
		 "(" + sname + ") was not specified!");
	    
	    NodeID snodeID = new NodeID(nodeID, sname);
	    File sfile = new File(PackageInfo.sProdDir,
				  snodeID.getWorkingParent() + "/" + fseq.getFile(0));

	    sourceScenes.add(sfile);
	    mergePatterns.add(pattern);
	  }
	}
      } 

      /* generate the name of the Houdini scene to save */ 
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !suffix.equals("hip")) 
	  throw new PipelineException
	    ("The HfsBuild Action requires that the primary target file sequence must " + 
	     "be a single Houdini scene file!");
	  
	targetScene = new File(PackageInfo.sProdDir,
			       nodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }

      /* command script files */
      {
	String sname = (String) getSingleParamValue("PreBuildScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Build Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The HfsBuild Action requires that the source node specified by the Pre " +
	       "Build Script parameter (" + sname + ") must have a single command " +
	       "script (.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preBuild = new File(PackageInfo.sProdDir,
			      snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
      
      {
	String sname = (String) getSingleParamValue("PostBuildScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Build Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("cmd"))) 
	    throw new PipelineException
	      ("The HfsBuild Action requires that the source node specified by the Post " +
	       "Build Script parameter (" + sname + ") must have a single command " +
	       "script (.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postBuild = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
      
      {
	String sname = (String) getSingleParamValue("PreSceneScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Scene Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("cmd"))) 
	    throw new PipelineException
	      ("The HfsBuild Action requires that the source node specified by the Pre " +
	       "Scene Script parameter (" + sname + ") must have a single command script " + 
	       "(.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preScene = new File(PackageInfo.sProdDir,
			      snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      {
	String sname = (String) getSingleParamValue("PostSceneScript"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Scene Script node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("cmd"))) 
	    throw new PipelineException
	      ("The HfsBuild Action requires that the source node specified by the Post " +
	       "Scene Script parameter (" + sname + ") must have a single command script " + 
	       "(.cmd) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postScene = new File(PackageInfo.sProdDir,
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }
    }

    /* create the temporary Houdini command script */ 
    File hscript = createTemp(agenda, 0644, "cmd");
    try {      
      FileWriter out = new FileWriter(hscript);

      if(preBuild != null) 
	out.write("source " + preBuild + "\n");

      /* load the source scenes */ 
      int wk;
      for(wk=0; wk<sourceScenes.size(); wk++) {
	File sourceScene = sourceScenes.get(wk);
	String pattern = mergePatterns.get(wk);

	if(preScene != null) 
	  out.write("source " + preScene + "\n");

	out.write("mread");
	if(wk > 0) 
	  out.write(" -o -m " + pattern);
	out.write(" " + sourceScene + "\n");

	if(postScene != null) 
	  out.write("source " + postScene + "\n");
      }

      if(postBuild != null) 
	out.write("source " + postBuild + "\n");

      /* save the file */ 
      out.write("mwrite " + targetScene + "\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary command file (" + hscript + ") for Job " + 
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
		"cat " + hscript + " | hscript" + licopt);
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given source file sequence to the to be loaded set.
   */ 
  private void 
  addSourceSeq
  (
   Integer order, 
   String sname, 
   FileSeq fseq, 
   DoubleMap<Integer,String,TreeSet<FileSeq>> sources
  ) 
    throws PipelineException
  {
    if(order != null) {
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null) || !suffix.equals("hip"))
	throw new PipelineException
	  ("The file sequence (" + fseq + ") of source node (" + sname + ") " + 
	   "does not contain a single Houdini scene (.hip) file!");
      
      TreeSet<FileSeq> fseqs = sources.get(order, sname);
      if(fseqs == null) {
	fseqs = new TreeSet<FileSeq>();
	sources.put(order, sname, fseqs);
      }
      
      fseqs.add(fseq);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -896579156106321150L;

}

