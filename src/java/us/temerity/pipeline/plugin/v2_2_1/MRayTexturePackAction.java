// $Id: MRayTexturePackAction.java,v 1.4 2007/04/04 07:33:30 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   T E X T U R E   P A C K   A C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates optimized Mental Ray memory mappable pyramid textures from a series of 
 * pre-filtered level source images. <P> 
 * 
 * Converts the sequence of pre-filtered level images which make up the primary file 
 * sequence of one of the source nodes into a single texture map which is the primary file 
 * sequence of this node.  The first source image should be full resolution and each
 * successively image half as large.  At most 20 images may be given.  For example, a 
 * source texture with 8 level named "source.#.tif" would consist of: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   source.0000.tif (256x256)
 *   source.0001.tif (128x128)
 *   source.0002.tif (64x64)
 *   source.0003.tif (32x32)
 *   source.0004.tif (16x16)
 *   source.0005.tif (8x8)
 *   source.0006.tif (4x4)
 *   source.0007.tif (2x2)
 * </DIV> <BR>
 * 
 * The advantage of using this action over {@link MRayTexutreAction MRayTexture} is that 
 * you can have more control over how the smaller resolution levels are generated from the 
 * original fully resolution source texture.  MRayTexture uses a simple box filter to 
 * produce these images.<P> 
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
 *     The source node which contains the pre-filtered pyramid level images files to 
 *     convert. <BR> 
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
 * </DIV>
 */
public
class MRayTexturePackAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayTexturePackAction() 
  {
    super("MRayTexturePack", new VersionID("2.2.1"), "Temerity", 
	  "Generates optimized Mental Ray memory mappable pyramid textures from " + 
	  "pre-filtered level source images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source node which contains the pre-filtered pyramid level images files " +
         "to convert.",
	 null);
      addSingleParam(param);
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
	   "How to organize output texel data.",
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
	   "The byte ordering of texel data in the output texture MAP file.", 
	   "Little-Endian", choices);
	addSingleParam(param);
      }
    }   

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);
      layout.addSeparator();    
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
    /* source pre-filtered level image paths */ 
    ArrayList<Path> sourcePaths =
      getPrimarySourcePaths(aImageSource, agenda, "pre-filtered image levels"); 
    if(sourcePaths == null) 
      throw new PipelineException("The ImageSource node was not specified!");
    if(sourcePaths.size() > 20) 
      throw new PipelineException
        ("The " + aImageSource + " file sequence contained more than the maximum (20) " + 
         "pre-filtered image levels!");

    /* target texture path */ 
    Path targetPath = 
      getPrimaryTargetPath(agenda, "map", "Mental Ray memory mappable pyramid texture");

    /* build the texture */   
    {
      String program = "imf_copy";
      if(PackageInfo.sOsType == OsType.Windows) 
        program = "imf_copy.exe";

      ArrayList<String> args = new ArrayList<String>();  

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
   
      args.add("-c");

      for(Path spath : sourcePaths) 
        args.add(spath.toOsString());

      args.add(targetPath.toOsString());
      
      /* create the process to run the action */ 
      return createSubProcess(agenda, program, args, outFile, errFile);
    } 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5085305062156511493L;

  public static final String aImageSource = "ImageSource";
  public static final String aGamma       = "Gamma";
  public static final String aFilter      = "Filter";
  public static final String aTexelLayout = "TexelLayout";
  public static final String aByteOrder   = "ByteOrder";

}

