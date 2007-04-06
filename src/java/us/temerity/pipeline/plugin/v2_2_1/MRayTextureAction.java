// $Id: MRayTextureAction.java,v 1.8 2007/04/06 21:16:28 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   T E X T U R E   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates optimized Mental Ray memory mappable textures from source images. <P> 
 * 
 * Converts the images which make up the primary file sequence of one of the source
 * nodes into the texture maps which make up the primary file sequence of this node. <P> 
 * 
 * Note that the MAP files generated by this Action are hardware specific so care needs
 * to be taken to insure that the hosts which generate the MAP files are of the same 
 * hardware type as the hosts which will use these files.  The best way of insuring this
 * is through the use of hardware specific Selection Keys. <P>
 * 
 * See the Mental Ray documentation for for details about <B>imf_copy</B>(1) and memory
 * mapped texture files. <P> 
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
 *   Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of texture data stored in the MAP file. 
 *     <UL>
 *       <LI> Pyramid (mip-mapped texture)
 *       <LI> Flat (single texture)
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Gamma <BR>
 *   <DIV style="margin-left: 40px;">
 *     Gama correction exponent.
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Filter <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the default filter value. 
 *   </DIV> <BR>
 *   <BR>  
 *   
 *   Texel Layout <BR>
 *   <DIV style="margin-left: 40px;">
 *     How to organize output texel data.
 *     <UL>
 *       <LI> Scanlines
 *       <LI> Tiles
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 *   
 *   Byte Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     The byte ordering of texel data in the output texture MAP file.  This must match the
 *     native byte order on the rendering 
 *     <UL>
 *       <LI> Little-Endian (x86)
 *       <LI> Big-Endian (others)
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV><P> 
 * 
 * By default, the "python" program is used by this action to run the "imf_copy" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class MRayTextureAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayTextureAction() 
  {
    super("MRayTexture", new VersionID("2.2.1"), "Temerity", 
	  "Generates optimized Mental Ray memory mappable textures from source images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source node which contains the image files to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Pyramid");
      choices.add("Flat");

      {
	ActionParam param = 
	  new EnumActionParam
	  (aFormat, 
	   "The format of texture data stored in the MAP file.", 
	   "Pyramid", choices);
	addSingleParam(param);
      }
    }   

    {
      ActionParam param = 
	new DoubleActionParam
	(aGamma, 
	 "Gamma correction exponent.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aFilter, 
	 "Overrides the default filter value.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Scanlines");
      choices.add("Tiles");

      {
	ActionParam param = 
	  new EnumActionParam
	  (aTexelLayout, 
	   "How to organize output texel data in the generated texture MAP file.",
	   "Scanlines", choices);
	addSingleParam(param);
      }
    }   

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Little-Endian");
      choices.add("Big-Endian");

      {
	ActionParam param = 
	  new EnumActionParam
	  (aByteOrder, 
	   "The byte ordering of texel data in the generated texture MAP file.", 
	   "Little-Endian", choices);
	addSingleParam(param);
      }
    }   

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);
      layout.addSeparator();   
      layout.addEntry(aFormat);   
      layout.addEntry(aGamma);
      layout.addEntry(aFilter);
      layout.addSeparator();   
      layout.addEntry(aTexelLayout);
      layout.addEntry(aByteOrder);
      layout.addSeparator();      
      addExtraOptionsParamToLayout(layout);

      setSingleLayout(layout);   
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
    /* file sequence checks */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    FileSeq targetSeq = null;
    {
      {    
	String sname = getSingleStringParamValue(aImageSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
	
	sourceSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("map"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain Mental Ray" + 
	     "memory mappable pyramid textures (.map)!");
	
	targetSeq = fseq;
      }

      if(sourceSeq.numFrames() != targetSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + sourceSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + targetSeq + ")!");
    }

    /* build common command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();    
    {
      switch(getSingleEnumParamIndex(aFormat)) {
      case 0:
        args.add("-p");
        break;

      case 1:
        break;
        
      default:
        throw new PipelineException
          ("Illegal Format value!");	
      }

      {
        Double filter = (Double) getSingleParamValue(aFilter); 
        if(filter != null) {
          if(filter < 0.0) 
            throw new PipelineException
              ("The value (" + filter + ") of parameter (" + aFilter + ") cannot be " + 
               "negative!");               

          args.add("-f");
          args.add(filter.toString());
        }
      }
      
      { 
        Double gamma = (Double) getSingleParamValue(aGamma); 
        if(gamma != null) {
          if(gamma < 0.0) 
            throw new PipelineException
              ("The value (" + gamma + ") of parameter (" + aGamma + ") cannot be " + 
               "negative!");     

          args.add("-g");
          args.add(gamma.toString());
        }
      }
      
      switch(getSingleEnumParamIndex(aTexelLayout)) {
      case 0:
        break;
        
      case 1:
        args.add("-r");
        break;
        
      default:
        throw new PipelineException
          ("Illegal Texel Layout value!");	
      }
      
      switch(getSingleEnumParamIndex(aTexelLayout)) {
      case 0:
        args.add("-L");
        break;
        
      case 1:
        args.add("-B");
        break;
        
      default:
        throw new PipelineException
          ("Illegal Byte Order value!");	
      }

      args.addAll(getExtraOptionsArgs());
    }
      
    /* texture conversion program */ 
    String program = "imf_copy";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "imf_copy.exe";
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, program, args, sourcePath, sourceSeq, targetSeq, 
                                  null, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7117525167366555106L;

  public static final String aImageSource = "ImageSource";
  public static final String aGamma       = "Gamma";
  public static final String aFilter      = "Filter";
  public static final String aFormat      = "Format";
  public static final String aTexelLayout = "TexelLayout";
  public static final String aByteOrder   = "ByteOrder";

}

