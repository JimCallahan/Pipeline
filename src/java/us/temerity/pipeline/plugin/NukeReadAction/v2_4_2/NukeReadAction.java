// $Id: NukeReadAction.java,v 1.1 2008/02/14 18:46:22 jim Exp $

package us.temerity.pipeline.plugin.NukeReadAction.v2_4_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   N U K E   R E A D   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a Nuke script fragment containing a single Read node for the set of source 
 * images.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the source node containing the images to read in by the create Nuke Read
 *     node. 
 *   </DIV> <BR>
 *   <P>  
 * 
 *   Missing Frames <BR>
 *   <DIV style="margin-left: 40px;">
 *     What to do when there is an error reading the file (by Nuke).
 *   </DIV> <BR>
 * 
 *   Colorspace <BR>
 *   <DIV style="margin-left: 40px;">
 *     Lookup table (LUT) used by Nuke to convert file pixel data to the internal data 
 *     representation used by Nuke.
 *   </DIV> <BR>
 * 
 *   Premultiplied <BR>
 *   <DIV style="margin-left: 40px;">
 *     If there is in alpha channel, divide color data by the alpha before converting from
 *     the colorspace, and then multiply by the alpha afterwards.  This will correct the
 *     color of the partially-trasparent pixels produced by most renderers.
 *   </DIV> <BR>
 * 
 *   Raw <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do no convert the data.  For most file formats this is the same as Linear colorspace, 
 *     but for some it may disable other processing, such as conversion from YUV.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public 
class NukeReadAction
  extends NukeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeReadAction() 
  {
    super("NukeRead", new VersionID("2.4.2"), "Temerity", 
	  "Provides control over generating and/or running a Nuke script containing a " + 
          "Reformat node which can rescale the resolution and orientation of a set of " + 
          "source images."); 
  
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "Specifies the source node containing the images to read in by the created " + 
         "Nuke Read node.", 
	 null);
      addSingleParam(param);
    } 


    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aError); 
      choices.add(aBlack);
      choices.add(aCheckboard);
      choices.add(aNearestFrame);

      ActionParam param = 
	new EnumActionParam
	(aMissingFrames,
	 "What to do when there is an error reading the file (by Nuke).",
         aError, choices);
      addSingleParam(param);
    } 
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aDefault); 
      choices.add(aSRGB); 
      choices.add(aRec709); 
      choices.add(aLinear); 
      choices.add(aCineon); 

      ActionParam param = 
	new EnumActionParam
	(aColorspace,
	 "Lookup table (LUT) used by Nuke to convert file pixel data to the internal " + 
         "data representation used by Nuke.", 
         aDefault, choices);
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aPremultiplied,
	 "If there is in alpha channel, divide color data by the alpha before converting " + 
         "from the colorspace, and then multiply by the alpha afterwards.  This will " + 
         "correct the color of the partially-trasparent pixels produced by most renderers.", 
         false); 
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aRaw,
	 "Do no convert the data.  For most file formats this is the same as Linear " + 
         "colorspace, but for some it may disable other processing, such as conversion " + 
         "from YUV.", 
         false); 
      addSingleParam(param);
    } 
    
    /* parameter layout */ 
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);  
      layout.addEntry(aMissingFrames); 
      layout.addSeparator(); 
      layout.addEntry(aColorspace);
      layout.addEntry(aPremultiplied);
      layout.addEntry(aRaw);

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment(); 
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
  @Override
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    /* target Nuke script */ 
    Path nukePath = getPrimaryTargetPath(agenda, getNukeExtensions(), "Nuke Script");

    /* source images */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    {
      String sname = getSingleStringParamValue(aImageSource); 
      if(sname == null) 
        throw new PipelineException
          ("The " + aImageSource + " was not set!");
      
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq == null) 
        throw new PipelineException
          ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
           "source nodes!");
      
      sourceSeq = fseq;
      
      Path spath = new Path(sname);
      sourcePath = new Path("WORKING" + spath.getParent());
    }

    /* create a temporary Nuke script */ 
    Path script = new Path(createTemp(agenda, "nk"));
    try {    
      FileWriter out = new FileWriter(script.toFile()); 

      FilePattern fpat = sourceSeq.getFilePattern(); 
      out.write
        ("Read {\n" + 
         " inputs 0\n" + 
         " file " + sourcePath + "/" + NukeActionUtils.toNukeFilePattern(fpat) + "\n"); 

      FrameRange range = sourceSeq.getFrameRange(); 
      if(range != null) {
        out.write(" first " + range.getStart() + "\n" + 
                  " last " + range.getEnd() + "\n"); 
      }

      String missing = getSingleStringParamValue(aMissingFrames); 
      out.write(" on_error "); 
      if(missing.equals(aNearestFrame)) 
        out.write("\"" + missing.toLowerCase() + "\"");
      else
        out.write(missing.toLowerCase());
      out.write("\n");

      String cspace = getSingleStringParamValue(aColorspace);
      if(!cspace.equals(aDefault)) 
        out.write(" colorspace " + getSingleStringParamValue(aColorspace) + "\n"); 

      out.write(" premultiplied " + getSingleBooleanParamValue(aPremultiplied) + "\n" + 
                " raw " + getSingleBooleanParamValue(aRaw) + "\n" + 
                "}\n"); 
                
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary Nuke script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
    }
    
    /* just copy the temporary Nuke script to the target location */ 
    return createTempCopySubProcess(agenda, script.toFile(), nukePath, outFile, errFile);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6549732981179384042L;

  public static final String aImageSource = "ImageSource";

  public static final String aMissingFrames = "MissingFrames";
  public static final String aError         = "Error";
  public static final String aBlack         = "Black";
  public static final String aCheckboard    = "Checkboard";
  public static final String aNearestFrame  = "Nearest Frame";

  public static final String aColorspace = "Colorspace"; 
  public static final String aDefault    = "default"; 
  public static final String aSRGB       = "sRGB"; 
  public static final String aRec709     = "rec709"; 
  public static final String aLinear     = "linear"; 
  public static final String aCineon     = "Cineon"; 
      
  public static final String aPremultiplied = "Premultiplied";     
  public static final String aRaw           = "Raw"; 

}
