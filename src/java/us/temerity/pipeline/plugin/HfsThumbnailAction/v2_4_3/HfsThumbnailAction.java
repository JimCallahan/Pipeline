package us.temerity.pipeline.plugin.HfsThumbnailAction.v2_4_3;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   N U K E   T H U M B N A I L   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts and resizes a single image frame from a source sequence for use as a thumbnail
 * image on web pages. <P> 
 * 
 * This action uses the Houdini and a dynamically generate COP network to perform the 
 * image conversion and resize operations. <P> 
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on the
 * usage and behavior of the composite output operator and hscript(1).<P>  
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Number <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies which image from the source image sequence should be processed.
 *   </DIV> 
 * 
 *   Thubnail Size <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The maximum width/height of the generated thumbnail image.  The image will be scaled 
 *     proportionally so that it fits within a square region of this size.
 *   </DIV> 
 * 
 *   Pixel Gain<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Multiplier of input pixel value intensity to produce thumbnail pixel values. 
 *     Useful for rescaling High Dynamic Range (HDR) images into a visually useful range. 
 *   </DIV> 
 * 
 *   Add Alpha<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to add an solid alpha channel to the input image before resizing and/or 
 *     compositing over the optional background layer.
 *   </DIV> 
 * 
 *   Over Background<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to composite the thumbnail images over a constant colored background layer.
 *   </DIV> 
 * 
 *   Background Color<BR>
 *   <DIV style="margin-left: 40px;">
 *     The background color to be used to composite the thumbnail image over.
 *   </DIV> 
 * 
 *   Use Graphical License<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to use an interactive graphical Houdini license when running hscript(1).  
 *     Normally, hscript(1) is run using a non-graphical license (-R option).  A graphical 
 *     license may be required if the site has not obtained any non-graphical licenses.
 *   </DIV> 
 * </DIV> <P> 
 */
public 
class HfsThumbnailAction
  extends HfsActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsThumbnailAction() 
  {
    super("HfsThumbnail", new VersionID("2.4.3"), "Temerity", 
	  "Generates a smaller version of a single image from a sequence of images.");
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aImageNumber, 
	 "Specifies the frame number of image from the source sequence to process.", 
	 1);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aThumbnailSize, 
	 "The maximum width/height of the generated thumbnail image.  The image will be " + 
         "scaled proportionally so that it fits within a square region of this size.", 
	 100);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aPixelGain, 
	 "Multiplier of input pixel value intensity to produce thumbnail pixel values. " + 
         "Useful for rescaling High Dynamic Range (HDR) images into a visually useful " + 
         "range", 
	 1.0); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aAddAlpha, 
	 "Whether to add an solid alpha channel to the input image before resizing and/or " + 
         "compositing over the optional background layer.", 
         false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aOverBackground, 
	 "Whether to composite the thumbnail images over a constant colored " + 
         "background layer.", 
         true);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Color3dActionParam
	(aBackgroundColor, 
	 "The thumbnail is composited over a background layer of this constant color.", 
	 new Color3d(0.5, 0.5, 0.5));
      addSingleParam(param);
    }

    addUseGraphicalLicenseParam();

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageNumber);   
      layout.addEntry(aThumbnailSize);  
      layout.addSeparator();
      layout.addEntry(aPixelGain); 
      layout.addEntry(aAddAlpha);   
      layout.addEntry(aOverBackground);   
      layout.addEntry(aBackgroundColor);   
      layout.addSeparator();
      layout.addEntry(aUseGraphicalLicense);

      setSingleLayout(layout);  
    }

    //addSupport(OsType.Windows); 
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
    /* the source image path */ 
    Path sourcePath = null;
    {
      String sname = agenda.getSourceName(); 
      if(sname == null) 
	throw new PipelineException
          ("The HfsThumbnail Action only accepts a single source node.");

      Path fpath = null;
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq.hasFrameNumbers()) {
        int frame = getSingleIntegerParamValue(aImageNumber, new Range<Integer>(0, null));
        FrameRange range = fseq.getFrameRange();
        if(!range.isValid(frame))
          throw new PipelineException
            ("The specified Image Number (" + frame + ") does not exist in the source " + 
             "file sequence (" + fseq + ")!"); 

        fpath = fseq.getPath(range.frameToIndex(frame));
      }
      else {
        fpath = fseq.getPath(0);
      }

      NodeID snodeID = new NodeID(agenda.getNodeID(), sname); 
      sourcePath = new Path(PackageInfo.sProdPath,
                            new Path(snodeID.getWorkingParent(), fpath)); 
    }

    /* the target thumbnail image path */
    Path targetPath = null;
    {
      FileSeq targetSeq = agenda.getPrimaryTarget();
      if(!targetSeq.isSingle()) 
	throw new PipelineException
	  ("The HfsThumbnail Action requires a single target image file!"); 

      targetPath = getPrimaryTargetPath(agenda, "thumbnail image");
    }

    /* create a temporary Houdini command script */ 
    Path hscript = new Path(createTemp(agenda, "cmd"));
    try {    
      boolean overBg = getSingleBooleanParamValue(aOverBackground);
      int size = getSingleIntegerParamValue(aThumbnailSize, new Range<Integer>(1, null));
      double pixelGain = 
        getSingleDoubleParamValue(aPixelGain, new Range<Double>(0.0, 10.0));

      Color3d bg = (Color3d) getSingleParamValue(aBackgroundColor); 
      if(bg == null) 
        throw new PipelineException
          ("The BackgroundColor was not specified!");
      
      FileWriter out = new FileWriter(hscript.toFile()); 

      /* create the comp network */       
      out.write("opcf /img\n\n" +
                "opadd -n img comp1\n" + 
                "opparm comp1\n\n"); 

      /* read the file in */ 
      out.write("opcf /img/comp1\n\n" + 
                "opadd -n file IN\n" + 
                "opparm IN filename ( '" + sourcePath + "' ) overridesize ( natural ) \n\n");

      /* resize it */
      out.write("opadd -n scale SIZE\n" + 
                "opparm SIZE scaletype ( res ) imageres ( " + size + " " + size + " ) " + 
                "downfilter ( sinc )\n\n");  
      
      /* adjust gain and optionally add an alpha channel */ 
      out.write("opadd -n bright GAIN\n" + 
                "opparm GAIN usecomp ( on ) " + 
                "red ( " + pixelGain + " 0 ) " + 
                "green ( " + pixelGain + " 0 ) " + 
                "blue ( " + pixelGain + " 0 ) ");
      if(getSingleBooleanParamValue(aAddAlpha)) 
        out.write("alpha ( 0 1 )\n\n");
      else 
        out.write("alpha ( 1 0 )\n\n"); 

      /* wire up the nodes */ 
      out.write("opwire -n IN -0 SIZE\n" + 
                "opwire -n SIZE -0 GAIN\n\n"); 

      /* comp resized image over the background? */ 
      if(overBg) 
        out.write("opadd -n color BG\n" + 
                  "opparm BG color ( " + bg.r() + " " + bg.g() + " " + bg.b() + " 1 ) " + 
                  "size ( " + size + " " + size + ")\n\n" +
                  "opadd -n over OVER\n\n" + 
                  "opwire -n BG -1 OVER\n" + 
                  "opwire -n GAIN -0 OVER\n\n"); 

      /* write the thumbnail out */
      out.write("opcf /out\n\n" + 
                "opadd -n comp comp1\n" + 
                "opparm comp1 trange ( on ) f ( 1 1 1 ) " + 
                "coppath ( /img/comp1/" + (overBg ? "OVER" : "GAIN") + " ) " + 
                "res ( " + size + " " + size + " ) copoutput ( '" + targetPath + "' )\n\n"); 

      /* execute the output operator and quit */ 
      out.write("opparm -c /out/comp1 execute\n" + 
                "quit\n");

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary Houdini command script file (" + hscript + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* houdini version */ 
    VersionID hvid = getHoudiniVersion(agenda); 

    /* create the process to run the action */ 
    {
      String program = "hscript";
      if((hvid != null) && (hvid.compareTo(new VersionID("8.1.0")) >= 0))
        program = "hbatch";

      ArrayList<String> args = new ArrayList<String>(); 
      if(!getSingleBooleanParamValue(aUseGraphicalLicense)) 
        args.add("-R"); 
      args.add("-i"); 
      args.add("-v"); 
      args.add(hscript.toOsString());

      return createSubProcess(agenda, program, args, outFile, errFile);
    }
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aImageNumber     = "ImageNumber";
  public static final String aThumbnailSize   = "ThumbnailSize"; 
  public static final String aPixelGain       = "PixelGain"; 
  public static final String aAddAlpha        = "AddAlpha"; 
  public static final String aOverBackground  = "OverBackground"; 
  public static final String aBackgroundColor = "BackgroundColor"; 
  
  private static final long serialVersionUID = -6230406522769412274L;

}
