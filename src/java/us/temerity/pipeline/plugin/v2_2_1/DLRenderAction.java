// $Id: DLRenderAction.java,v 1.2 2007/04/12 15:21:55 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   R E N D E R   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * The 3Delight RenderMan compliant renderer. <P> 
 * 
 * All of the RIB file (.rib) dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed.  This action makes no assumptions about the contents
 * of the RIB files specified except the order in which they are processed.<P> 
 * 
 * The frame range rendered will be limited by frame numbers of the target images.  In most 
 * cases, an Execution Method of (Parallel) and a Batch Size of (1) should be used with this 
 * action so that each image frame is rendered by a seperate invocation of renderdl(1) which 
 * is passed the specific RIBs required for the frame being rendered.  It is also possible 
 * to render multi-frame RIBs or even multiple single frame RIBs at one time by using a 
 * larger Batch Size.<P> 
 * 
 * Any source file sequences which set the Order per-source parameter should contain either
 * a single RIB file or the same number of RIB files as there number of target images to be
 * rendered.  You should use a Link Relationship of All for single frame RIBs which to be used
 * in all rendered frames.  Per frame RIBs should use a Link Relationshop of 1:1 so that only 
 * the correct RIBs will be used to render each frame.<P> 
 * 
 * Depending on the RIBs processed and RiDisplay requests encountered, one or more images, 
 * depthmaps or deep shadow maps may be generated in one rendering pass.  No parsing or 
 * checking of the contents of the RIB files will be performed to insure that they actually
 * do generate the primary/secondary target files of this Action.<P> 
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for
 * <A href="http://www.3delight.com/ZDoc/3delight_10.html"><B>renderdl</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 *   Render Verbosity<BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of render progress, warning and error messages.
 *   </DIV> <BR>
 *   
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain RIB files. This 
 *     parameter determines the order in which the input RIB files are processed. If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class DLRenderAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLRenderAction() 
  {
    super("DLRender", new VersionID("2.2.1"), "Temerity",
	  "The 3Delight RenderMan compliant renderer.");
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Low");
      choices.add("Medium");
      choices.add("High");

      ActionParam param = 
	new EnumActionParam
	(aRenderVerbosity,
	 "The verbosity of rendering statistics.",
	 "High", choices);
      addSingleParam(param);
    }

    addExtraOptionsParam(); 

    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aRenderVerbosity);
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

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
	 "Each source node sequence which sets this parameter should contain RIB files. " + 
         "This parameter determines the order in which the input RIB files are processed. " + 
         "If this parameter is not set for a source node file sequence, it will be ignored.", 
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
    /* the target frame range and order of RIB files to the processed */ 
    FrameRange targetRange = null;
    MappedLinkedList<Integer,Path> sourcePaths = new MappedLinkedList<Integer,Path>();
    {
      FileSeq targetSeq = agenda.getPrimaryTarget();
      targetRange = targetSeq.getFrameRange();
      int numFrames = targetSeq.numFrames();

      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, aOrder);
	  addSourcePaths(agenda, numFrames, sname, fseq, order, sourcePaths);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
            addSourcePaths(agenda, numFrames, sname, fseq, order, sourcePaths);
	  }
	}
      }

      if(sourcePaths.isEmpty()) 
	throw new PipelineException
	  ("No source RIB files where specified using the per-source Order parameter!");
    }

    /* generate the command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>(); 
    {
      args.add("-noinit");
      args.add("-stats" + (getSingleEnumParamIndex(aRenderVerbosity) + 1));
      
      if(targetRange != null) {
	args.add("-frames");
	args.add(String.valueOf(targetRange.getStart()));
	args.add(String.valueOf(targetRange.getEnd()));
      }

      args.addAll(getExtraOptionsArgs());

      for(LinkedList<Path> ribs : sourcePaths.values()) 
	for(Path path : ribs) 
	  args.add(path.toOsString()); 
    }

    /* render program */ 
    String program = "renderdl";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "renderdl.exe";

    /* create the process to run the action */ 
    return createSubProcess(agenda, program, args, null, 
                            agenda.getTargetPath().toFile(), outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating source RIB filenames.
   */ 
  private void 
  addSourcePaths
  (
   ActionAgenda agenda, 
   int numFrames, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   MappedLinkedList<Integer,Path> sourcePaths
  )
    throws PipelineException 
  {
    if(order == null) 
      return;
    
    if(sourcePaths.containsKey(order)) 
      throw new PipelineException
        ("The Order per-source parameter for file sequence (" + fseq + ") of source node " + 
         "(" + sname + ") was not unique!");

    String suffix = fseq.getFilePattern().getSuffix();
    if((suffix == null) || !suffix.equals("rib"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have contain RIB files!");
    
    if((fseq.numFrames() != 1) && (fseq.numFrames() != numFrames))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " +  
	 "must have the contain the same number of files as the target sequence or " + 
	 "exactly one file.");

    for(Path path : getWorkingNodeFilePaths(agenda, sname, fseq))
      sourcePaths.put(order, path);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3407283696477409595L;

  public static final String aOrder           = "Order"; 
  public static final String aRenderVerbosity = "RenderVerbosity";

}

