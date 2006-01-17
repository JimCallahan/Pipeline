// $Id: MRayRenderAction.java,v 1.1 2006/01/17 20:41:47 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * The Mental Ray renderer. <P> 
 * 
 * All of the MI file (.mi) dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed.  The frame range rendered will be limited by frame 
 * numbers of the target images.  In most cases, an Execution Method of (Parallel) and a 
 * Batch Size of (1) should be used with this action so that each image frame is rendered by
 * a seperate invocation of render(1) which is only passed the MIs required for the frame 
 * being rendered. It is also possible to render multi-frame MIs or even multiple single 
 * frame MIs at one time by using a larger Batch Size.  <P> 
 * 
 * See the Mental Ray documentation for details about <B>mentalrayrender</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Verbosity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of render progress, warning and error messages. <BR>
 *   </DIV> <BR>
 * 
 *   Include Path<BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of directories which overrides the path used to 
 *     resolve $include statements in the MI scene file.
 *   </DIV> <BR>
 * 
 *   Library Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of directories that mental ray searches for shader 
 *     libraries containing shader code before the default library paths. 
 *   </DIV> <BR>
 * 
 *   Texture Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of directories that mental ray searches for texture files.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV><P> 
 *     
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain MI files. This 
 *     parameter determines the order in which the input MI files are processed. If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MRayRenderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayRenderAction() 
  {
    super("MRayRender", new VersionID("2.0.0"), "Temerity",
	  "The Mental Ray renderer.");

    {
      ArrayList<String> verbose = new ArrayList<String>();
      verbose.add("No Messages");
      verbose.add("Fatal Messages Only");
      verbose.add("Error Messages");
      verbose.add("Warning Messages");
      verbose.add("Info Messages");
      verbose.add("Progress Messages");
      verbose.add("Details Messages");

      ActionParam param = 
	new EnumActionParam
	("RenderVerbosity",
	 "The verbosity of render progress, warning and error messages.", 
	 "Warning Messages", verbose);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("IncludePath",
	 "A colon seperated list of directories which overrides the path used to " +
	 "resolve $include statements in the MI scene file.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("LibraryPath",
	 "A colon seperated list of directories that mental ray searches for shader " + 
	 "libraries containing shader code before the default library paths.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("TexturePath",
	 "A colon seperated list of directories that mental ray searches for texture " + 
	 "files.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("ExtraOptions",
	 "Additional command-line arguments.", 
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("RenderVerbosity");
      layout.addSeparator();  
      layout.addEntry("IncludePath");
      layout.addEntry("LibraryPath");
      layout.addEntry("TexturePath");
      layout.addSeparator();  
      layout.addEntry("ExtraOptions");   

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
	 "Process the MI file in this order.",
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
    FrameRange range = null;
    LinkedList<File> sources = new LinkedList<File>(); 
    boolean single = false;
    {
      TreeMap<Integer,LinkedList<File>> sourceMIs = new TreeMap<Integer,LinkedList<File>>();
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourceMIs(nodeID, sname, fseq, order, sourceMIs);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourceMIs(nodeID, sname, fseq, order, sourceMIs);
	  }
	}
      }

      for(LinkedList<File> mis : sourceMIs.values())
	sources.addAll(mis); 

      if(sources.isEmpty()) 
	throw new PipelineException
	  ("No source MI files where specified using the per-source Order parameter!");
      else if (sources.size() == 1)
	single = true;

      {
	FileSeq fseq = agenda.getPrimaryTarget();
	range = fseq.getFrameRange();
      }
    }

    /* combine MI files into a single file */ 
    File scene = null; 
    if(single) {
      scene = sources.getFirst();
    }
    else {
      /* create a temporary MI file */ 
      scene = createTemp(agenda, 0644, "mi");
      try {      
	FileWriter out = new FileWriter(scene);
	
	out.write("# " + getName() + " (" + getVersionID() + ")\n\n");
	
	for(File file : sources)
	  out.write("$include \"" + file + "\"\n");
	
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the temporary MI scene file (" + scene + ") for Job " + 
	   "(" + agenda.getJobID() + ")!\n" +
	   ex.getMessage());
      }
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>(); 
      
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("RenderVerbosity");
	if(param != null) {
	  int level = param.getIndex();
	  if(level == -1) 
	    throw new PipelineException
	      ("The Render Verbosity was illegal!"); 
	  
	  args.add("-verbose");
	  args.add(String.valueOf(level));
	}
      }

      {
	String path = (String) getSingleParamValue("IncludePath"); 
	if((path != null) && (path.length() == 0))
	   path = null;

	String miroot = agenda.getEnvironment().get("MI_ROOT");

	String ipath = null;
	if(single) {
	  if(path != null) 
	    ipath = path;
	}
	else {
	  if(path != null) {
	    if(miroot != null) 
	      ipath = (scene.getParent() + ":" + miroot + "/include:" + path);
	    else 
	      ipath = (scene.getParent() + ":" + path);
	  }
	  else {
	    if(miroot != null) 
	      ipath = (scene.getParent() + ":" + miroot + "/include");
	  }
	}
	
	if(ipath != null) {
	  args.add("-I");
	  args.add(ipath);
	}
      }

      {
	String path = (String) getSingleParamValue("LibraryPath"); 
	if((path != null) && (path.length() > 0)) {
	  args.add("-L");
	  args.add(path);
	}
      }

      {
	String path = (String) getSingleParamValue("TexturePath"); 
	if((path != null) && (path.length() > 0)) {
	  args.add("-T");
	  args.add(path);
	}
      }
      
      if(!agenda.getPrimaryTarget().isSingle()) {
	args.add("-render");
	args.add(String.valueOf(range.getStart()));
	args.add(String.valueOf(range.getEnd()));
	args.add(String.valueOf(range.getBy()));
      }

      args.add(scene.toString());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "mentalrayrender", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }

  /**
   * A helper method for generating source MI filenames.
   */ 
  private void 
  addSourceMIs
  (
   NodeID nodeID, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   TreeMap<Integer,LinkedList<File>> sourceMIs
  )
    throws PipelineException 
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if((suffix == null) || !suffix.equals("mi"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have contain MI files!");
    
    NodeID snodeID = new NodeID(nodeID, sname);
    for(File file : fseq.getFiles()) {
      File source = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + file);
    
      LinkedList<File> mis = sourceMIs.get(order);
      if(mis == null) {
	mis = new LinkedList<File>();
	sourceMIs.put(order, mis);
      }
      
      mis.add(source);
    }      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7064904024883001175L;

}

