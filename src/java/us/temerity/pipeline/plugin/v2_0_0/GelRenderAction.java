// $Id: GelRenderAction.java,v 1.1 2006/01/22 21:00:58 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G E L   R E N D E R   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * The Gelato hardware accelerated renderer. <P> 
 * 
 * All of the Pyg file (.pyg) dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed.  The frame range rendered will be limited by frame 
 * numbers of the target images.  In most cases, an Execution Method of (Parallel) and a 
 * Batch Size of (1) should be used with this action so that each image frame is rendered by
 * a seperate invocation of render(1) which is only passed the Pygs required for the frame 
 * being rendered. It is also possible to render multi-frame Pygs or even multiple single 
 * frame Pygs at one time by using a larger Batch Size.  Depending on the Pygs processed, 
 * one or more images, depthmaps or deep shadow maps may be generated in one rendering 
 * pass. <P> 
 * 
 * If the <A href="http://www.renderman.org/RMR/Utils/gelato/index.html#ribelato">Ribelato</A>
 * input generator plugin is installed, you may also supply RIB input files (.rib) in addition
 * to Pyg files for the renderer. <P> 
 * 
 * See the Gelato documentation for details about <B>gelato</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shading Quality: <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the "shadingquaility" options specified in the input scenes. <BR> 
 *   </DIV> <BR>
 * 
 *   Shading System: <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the shading system used to render the input scenes. 
 *     <UL>
 *       <LI> Shading Language - Use Gelato Shading Language shaders.
 *       <LI> Default Surface - Replace all shaders with the "defaultsurface" shader.
 *       <LI> Key Fill Rim - Render with three automatically-placed, unshadowed lights.
 *     </UL>
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
 *     Each source node sequence which sets this parameter should contain Pyg files. This 
 *     parameter determines the order in which the input Pyg files are processed. If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class GelRenderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GelRenderAction() 
  {
    super("GelRender", new VersionID("2.0.0"), "Temerity",
	  "The Gelato hardware accelerated renderer.");

    {
      ActionParam param = 
	new DoubleActionParam
	("ShadingQuality", 
	 "Overrides the \"shadingquaility\" options specified in the input scenes.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Shading Language");
      choices.add("Default Surface");
      choices.add("Key Fill Rim");

      ActionParam param = 
	new EnumActionParam
	("ShadingSystem", 
	 "Specifies the shading system used to render the input scenes.",
	 "Shading Language", choices);
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
      layout.addEntry("ShadingQuality");
      layout.addEntry("ShadingSystem");
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
	 "Process the Pyg input scene file in this order.",
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
    TreeMap<Integer,LinkedList<File>> sourceFiles = new TreeMap<Integer,LinkedList<File>>();
    {
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, "Order");
	  addSourceFiles(nodeID, sname, fseq, order, sourceFiles);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
	    addSourceFiles(nodeID, sname, fseq, order, sourceFiles);
	  }
	}
      }

      if(sourceFiles.isEmpty()) 
	throw new PipelineException
	  ("No source input files where specified using the per-source Order parameter!");

      {
	FileSeq fseq = agenda.getPrimaryTarget();
	range = fseq.getFrameRange();
      }
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>(); 

      {
	Double quality = (Double) getSingleParamValue("ShadingQuality");
	if(quality != null) {
	  args.add("-preview");
	  args.add(quality.toString());
	}
      }

      {
	args.add("-shade");
	EnumActionParam param = (EnumActionParam) getSingleParam("ShadingSystem");
	switch(param.getIndex()) {
	case 0:
	  args.add("gsl");
	  break;

	case 1:
	  args.add("defaultsurface");
	  break;

	case 2:
	  args.add("keyfillrim");
	  break;

	default:
	  throw new PipelineException
	    ("Illegal Shading System value!");	
	}
      }

      args.add("-statistics");

      addExtraOptions(args);

      for(LinkedList<File> files : sourceFiles.values()) 
	for(File file : files) 
	  args.add(file.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "gelato", args,agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }

  /**
   * A helper method for generating source File filenames.
   */ 
  private void 
  addSourceFiles
  (
   NodeID nodeID, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   TreeMap<Integer,LinkedList<File>> sourceFiles
  )
    throws PipelineException 
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if((suffix == null) || (!suffix.equals("pyg") && !suffix.equals("rib")))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
	 "must have contain either Pyg (.pyg), or if Ribelato is installed, RIB (.rib) " + 
	 "files!");
    
    NodeID snodeID = new NodeID(nodeID, sname);
    for(File file : fseq.getFiles()) {
      File source = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + file);
    
      LinkedList<File> files = sourceFiles.get(order);
      if(files == null) {
	files = new LinkedList<File>();
	sourceFiles.put(order, files);
      }
      
      files.add(source);
    }      
  }

  /**
   * Append any additional command-line arguments.
   */ 
  private void 
  addExtraOptions
  (
   ArrayList<String> args
  ) 
    throws PipelineException
  {
    String extra = (String) getSingleParamValue("ExtraOptions");
    if(extra == null) 
      return;

    String parts[] = extra.split("\\p{Space}");
    int wk;
    for(wk=0; wk<parts.length; wk++) {
      if(parts[wk].length() > 0) 
	args.add(parts[wk]);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7822707304956303521L;

}

