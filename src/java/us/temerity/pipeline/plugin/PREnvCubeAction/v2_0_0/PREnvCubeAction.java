// $Id: PREnvCubeAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.PREnvCubeAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P R   E N V   C U B E   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generate optimized PhotoRealistic RenderMan cube faced environment maps from six 
 * directional source images. <P> 
 * 
 * Converts the six images [+x, -x, +y, -y, +z, -z] which make up the primary file 
 * sequence of one of the source nodes into the single cubic environment map which is the 
 * single member of the primary file sequence of this node. <P> 
 * 
 * See the <A href="https://renderman.pixar.com/products/tools/rps.html">RenderMan 
 * ProServer</A>
 * documentation for details about <B>txmake</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images files to convert. <BR> 
 *   </DIV> <BR>
 *   <BR>
 * 
 *
 *   Image Resize <BR>
 *   <DIV style="margin-left: 40px;">
 *     The method used to resize the input image to an exact power of two.
 *     <UL>
 *       <LI> Up - Resized up to the next higer power of two.
 *       <LI> Down - Resized down to the next lower power of two.
 *       <LI> Round - Resized to the nearest power of two.
 *       <LI> None - Image is not resized.
 *     </UL>
 *   </DIV> <BR>
 *
 *   Texture Coords <BR>
 *   <DIV style="margin-left: 40px;">
 *     How texture coordinates are adjusted by the resize of the input images.
 *     <UL>
 *       <LI> Proportional - 0 to 1 across longest dimension, 0 to aspect ratio across 
 *                           shortest dimensions. 
 *       <LI> Square - 0 to 1 across both dimensions.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Field Of View <BR>
 *   <DIV style="margin-left: 40px;">
 *     The field-of-view (in degrees) of each environment cube face.
 *   </DIV> <BR>
 *   <BR>
 * 
 * 
 *   Filter <BR>
 *   <DIV style="margin-left: 40px;">
 *     The filter used to downsample the source images to generate the mipmap levels.
 *     <UL>
 *       <LI> Box 
 *       <LI> Triangle 
 *       <LI> Gaussian  
 *       <LI> Catmul-Rom 
 *       <LI> Bessel 
 *       <LI> Sinc 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   S Filter Width <BR>
 *   T Filter Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the width (diameter) of the downsizing filter in the S/T directions.
 *   </DIV> <BR>
 *   <BR>
 * 
 *   
 *   S Mode <BR>
 *   T Mode <BR>
 *   <DIV style="margin-left: 40px;">
 *     The method used to lookup texels outside the [0,1] S/T coordinate range: 
 *     <UL>
 *       <LI>Black - Use black for all values outside [0,1] range.
 *       <LI>Clamp - Use border texel color for values outside [0,1] range.
 *       <LI>Peridoc - Tiles texture outside [0,1] range.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Filter Pattern <BR>
 *   <DIV style="margin-left: 40px;">
 *     Controls the set of filtered texture resolutions which are generated and stored in the 
 *     texture file.
 *     <UL>
 *       <LI> Single
 *       <LI> Diagonal
 *       <LI> All
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Bit Depth <BR>
 *   <DIV style="margin-left: 40px;">
 *     The storage size of texels in the generated texture map.
 *     <UL>
 *       <LI> Byte - 8-bit integer.
 *       <LI> Short - 16-bit integer.
 *       <LI> Float - 32-bit floating point.
 *       <LI> Lossy - compressed floating point.
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Texture Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The file format of the generated texture map.
 *     <UL>
 *       <LI> Pixar
 *       <LI> TIFF
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 * 
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV>
 */
public
class PREnvCubeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PREnvCubeAction() 
  {
    super("PREnvCube", new VersionID("2.0.0"), "Temerity", 
	  "Generate optimized PhotoRealistic RenderMan cube faced environment maps from " + 
	  "six directional source images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node which contains the six image files [+x, -x, +y, -y, +z, -z] " + 
	 "to convert.",
	 null);
      addSingleParam(param);
    }

    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Up");
      choices.add("Down");
      choices.add("Round");
      choices.add("None");

      ActionParam param = 
	new EnumActionParam
	("ImageResize", 
	 "The method used to resize the input image to an exact power of two.",
	 "Up", choices);
      addSingleParam(param);
    }       

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Proportional");
      choices.add("Square");

      ActionParam param = 
	new EnumActionParam
	("TextureCoords", 
	 "How texture coordinates are adjusted by the resize of the input images.", 
	 "Proportional", choices);
      addSingleParam(param);
    }       

    {
      ActionParam param = 
	new DoubleActionParam
	("FieldOfView", 
	 "The field-of-view (in degrees) of each environment cube face.", 
	 90.0);
      addSingleParam(param);
    }


    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Box");
      choices.add("Triangle");
      choices.add("Gaussian");
      choices.add("Catmul-Rom");
      choices.add("Bessel");
      choices.add("Sinc");

      ActionParam param = 
	new EnumActionParam
	("Filter", 
	 "The filter used to downsample the source images to generate the mipmap levels.",
	 "Sinc", choices);
      addSingleParam(param);
    }   

    {
      ActionParam param = 
	new DoubleActionParam
	("SFilterWidth", 
	 "Overrides the width (diameter) of the downsizing filter in the S direction.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("TFilterWidth", 
	 "Overrides the width (diameter) of the downsizing filter in the T direction.", 
	 null);
      addSingleParam(param);
    }
    

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Black");
      choices.add("Clamp");
      choices.add("Periodic");

      {
	ActionParam param = 
	  new EnumActionParam
	  ("SMode", 
	   "The method used to lookup texels outside the [0,1] S-coordinate range.",
	   "Black", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new EnumActionParam
	  ("TMode", 
	   "The method used to lookup texels outside the [0,1] T-coordinate range.",
	   "Black", choices);
	addSingleParam(param);
      }
	
      addPreset("Mode", choices);
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "Black");
	values.put("TMode", "Black");
	
	addPresetValues("Mode", "Black", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "Clamp");
	values.put("TMode", "Clamp");
	
	addPresetValues("Mode", "Clamp", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "Periodic");
	values.put("TMode", "Periodic");
	
	addPresetValues("Mode", "Periodic", values);
      }
    }   
    

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Single");
      choices.add("Diagonal");
      choices.add("All");

      ActionParam param = 
	new EnumActionParam
	("FilterPattern", 
	 "Controls the set of filtered texture resolutions which are generated and stored " + 
	 "in the texture file.",
	 "Diagonal", choices);
      addSingleParam(param);
    }   

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Byte");
      choices.add("Short");
      choices.add("Float");
      choices.add("Lossy");

      ActionParam param = 
	new EnumActionParam
	("BitDepth", 
	 "The storage size of texels in the generated texture map.",
	 "Byte", choices);
      addSingleParam(param);
    }   
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Pixar");
      choices.add("TIFF");

      ActionParam param = 
	new EnumActionParam
	("TextureFormat", 
	 "The file format of the generated texture map.",
	 "TIFF", choices);
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
      layout.addEntry("ImageSource");
      layout.addSeparator();      
      layout.addEntry("ExtraOptions");

      {
	LayoutGroup resize = new LayoutGroup
	  ("Resize", "Input image resizing.", true);
	resize.addEntry("ImageResize");
	resize.addEntry("TextureCoords");
	resize.addEntry("FieldOfView");

	layout.addSubGroup(resize);
      }

      {
	LayoutGroup filter = new LayoutGroup
	  ("Filtering", "Mipmap level generation filtering controls.", true);
	filter.addEntry("Filter");
	filter.addSeparator();      
	filter.addEntry("SFilterWidth");
	filter.addEntry("TFilterWidth");

	layout.addSubGroup(filter);
      }

      {
	LayoutGroup output = new LayoutGroup
	  ("Output", "Texture output and interpretation controls.", true);
	output.addEntry("Mode");
	output.addEntry("SMode");
	output.addEntry("TMode");
	output.addSeparator();
	output.addEntry("FilterPattern");
	output.addSeparator();
	output.addEntry("BitDepth");
	output.addEntry("TextureFormat");
	
	layout.addSubGroup(output);	
      }

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
    NodeID nodeID = agenda.getNodeID();

    /* file sequence checks */ 
    File fromPath = null;
    FileSeq fromSeq = null;
    File target = null;
    {
      {    
	String sname = (String) getSingleParamValue("ImageSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source (" + sname + ") was not one of the source nodes!");
	
	if(fseq.numFrames() != 6)
	  throw new PipelineException
	    ("The source file sequence (" + fseq + ") must contain exactly six " + 
	     "[+x, -x, +y, -y, +z, -z] images!");

	fromSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromPath = new File(PackageInfo.sProdDir, 
			    snodeID.getWorkingParent().toFile().getPath());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("tex"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a " + 
	     "PhotoRealistic RenderMan textures (.tex)!");
	
	if(fseq.numFrames() != 1)
	  throw new PipelineException
	    ("The target file sequence (" + fseq + ") must be a single environment map!");
	
	target = fseq.getFile(0);
      }
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add("-envcube");

    {
      Double fov = (Double) getSingleParamValue("FieldOfView");
      if(fov != null) {
	if(fov < 0.0) 
	  throw new PipelineException
	    ("The Field Of View (" + fov + ") cannot be negative!");

	args.add("-fov");
	args.add(fov.toString());
      }
    }

    {
      args.add("-resize");
      EnumActionParam param1 = (EnumActionParam) getSingleParam("ImageResize");
      EnumActionParam param2 = (EnumActionParam) getSingleParam("TextureCoords");
      switch(param1.getIndex()) {
      case 0:
	switch(param2.getIndex()) {
	case 0:
	  args.add("up");
	  break;
	  
	case 1:
	  args.add("up-");
	}
	break;
	
      case 1:
	switch(param2.getIndex()) {
	case 0:
	  args.add("down");
	  break;
	  
	case 1:
	  args.add("down-");
	}
	break;
	
      case 2:
	switch(param2.getIndex()) {
	case 0:
	  args.add("round");
	  break;
	  
	case 1:
	  args.add("round-");
	}
	break;
	
      case 3:
	args.add("none");
      }
    }
    
    {
      args.add("-filter");
      EnumActionParam param = (EnumActionParam) getSingleParam("Filter");
      switch(param.getIndex()) {
      case 0:
	args.add("box");
	break;

      case 1:
	args.add("triangle");
	break;

      case 2:
	args.add("gaussian");
	break;
	
      case 3:
	args.add("catmull-rom");
	break;

      case 4:
	args.add("bessel");
	break;
	
      case 5:
	args.add("sinc");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Filter value!");	
      }
    }

    Double swidth = (Double) getSingleParamValue("SFilterWidth");
    if(swidth != null) {
      if(swidth < 0.0) 
	throw new PipelineException
	  ("The S Filter Width (" + swidth + ") cannot be negative!");

      args.add("-swidth");
      args.add(swidth.toString());
    }

    Double twidth = (Double) getSingleParamValue("TFilterWidth");
    if(twidth != null) {
      if(twidth < 0.0) 
	throw new PipelineException
	  ("The T Filter Width (" + twidth + ") cannot be negative!");

      args.add("-twidth");
      args.add(twidth.toString());
    }

    {
      args.add("-smode");
      EnumActionParam param = (EnumActionParam) getSingleParam("SMode");
      switch(param.getIndex()) {
      case 0:
	args.add("black");
	break;

      case 1:
	args.add("clamp");
	break;

      case 2:
	args.add("periodic");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal S Mode value!");
      }
    }
    
    {
      args.add("-tmode");
      EnumActionParam param = (EnumActionParam) getSingleParam("TMode");
      switch(param.getIndex()) {
      case 0:
	args.add("black");
	break;

      case 1:
	args.add("clamp");
	break;

      case 2:
	args.add("periodic");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal T Mode value!");
      }
    }

    {
      args.add("-pattern");
      EnumActionParam param = (EnumActionParam) getSingleParam("FilterPattern");
      switch(param.getIndex()) {
      case 0:
	args.add("single");
	break;

      case 1:
	args.add("diagonal");
	break;

      case 2:
	args.add("all");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Filter Pattern value!");	
      }
    }
    
    {
      EnumActionParam param = (EnumActionParam) getSingleParam("BitDepth");
      switch(param.getIndex()) {
      case 0:
	break;

      case 1:
	args.add("-short");
	break;

      case 2:
	args.add("-float");
	break;
	
      case 3:
	args.add("-float");
	args.add("-lossy");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Filter Pattern value!");	
      }
    }

    {
      args.add("-format");
      EnumActionParam param = (EnumActionParam) getSingleParam("TextureFormat");
      switch(param.getIndex()) {
      case 0:
	args.add("pixar");
	break;

      case 1:
	args.add("tiff");
	break;

      default:
	throw new PipelineException
	  ("Illegal Texture Format value!");	
      }
    }

    addExtraOptions(args);

    try {
      for(File source : fromSeq.getFiles()) 
	args.add(fromPath + "/" + source); 
      args.add(target.toString());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "txmake", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
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

  private static final long serialVersionUID = -3000078982837861129L;

}

