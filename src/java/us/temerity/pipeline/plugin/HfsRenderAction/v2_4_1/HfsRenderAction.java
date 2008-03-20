// $Id: HfsRenderAction.java,v 1.2 2008/03/20 21:50:02 jim Exp $

package us.temerity.pipeline.plugin.HfsRenderAction.v2_4_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.PythonActionUtils; 
import us.temerity.pipeline.plugin.HfsActionUtils; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*    H F S   R E N D E R   A C T I O N                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a sequences of images directly from a Houdini scene. <P> 
 * 
 * This action provides a convienent method for evaluating a renderer output operator 
 * contained in the source Houdini scene using hscript(1).  The target primary file
 * sequence should contain the images to be rendered.  The frame range (trange f1 f2 f3) and
 * output picture (picture) parameters of this operator will be overridden by the Action 
 * to correspond to the images regenerated by the job. <P> 
 * 
 * The following Houdini output operators are supported by this action:<BR>
 * <DIV style="margin-left: 40px;">
 *   Mantra - The Houdini renderer. <BR>
 *   OpenGL - The Houdini hardware renderer. <BR>
 *   Wren - The Houdini line renderer. <BR>
 *   RenderMan - RenderMan compliant renderers. <BR>
 *   MentalRay - The MentalRay raytracer. <BR>
 * </DIV> <P> 
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on the
 * usage and behavior of renderer output operators and hbatch(1).<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Output Operator <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the render output operator. <BR>
 *   </DIV> <BR>
 *
 *   Camera Override <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the render camera (if set). <BR> 
 *   </DIV> <BR>
 * 
 *   Houdini Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Houdini scene file to render. <BR> 
 *   </DIV> <BR>
 * 
 *   Pre Render Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate before rendering 
 *     begins.  <BR>
 *   </DIV> <BR>
 * 
 *   Post Render Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate after rendering 
 *     ends. <BR>
 *   </DIV> <BR>
 * 
 *   Pre Frame Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate before rendering each 
 *     frame. <BR>
 *   </DIV> <BR>
 * 
 *   Post Frame Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the command script to evaluate after rendering each 
 *     frame. <BR>
 *   </DIV> <BR>
 * 
 *   Use Graphical License<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to use an interactive graphical Houdini license when running hbatch(1).  
 *     Normally, hbatch(1) is run using a non-graphical license (-R option).  A graphical 
 *     license may be required if the site has not obtained any non-graphical licenses.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class HfsRenderAction
  extends HfsActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsRenderAction() 
  {
    super("HfsRender", new VersionID("2.4.1"), "Temerity",
	  "Renders a sequences of images directly from a Houdini scene.");

    addHoudiniSceneParam();
    addOutputOperatorParam("mantra1"); 
    addCameraOverrideParam(); 
    addUseGraphicalLicenseParam();

    addPreRenderScriptParam();
    addPostRenderScriptParam();
    addPreFrameScriptParam();
    addPostFrameScriptParam();

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aHoudiniScene);
      layout.addEntry(aOutputOperator);
      layout.addEntry(aCameraOverride);
      layout.addSeparator();
      layout.addEntry(aUseGraphicalLicense);

      addScriptParamsToLayout(layout); 
      
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
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    /* target files */ 
    FileSeq targetSeq = 
      new FileSeq(agenda.getTargetPath().toString(), agenda.getPrimaryTarget()); 

    /* houdini scene */ 
    Path source = getHoudiniSceneSourcePath(aHoudiniScene, agenda);

    /* create the temporary Houdini command script */ 
    Path hscript = new Path(createTemp(agenda, "cmd"));
    try {      
      FileWriter out = new FileWriter(hscript.toFile());
    
      String opname = ("/out/" + getSingleStringParamValue(aOutputOperator, false));

      writeCameraOverrideOpparm(opname, agenda, out);

      String picture = "picture";
      VersionID hvid = getHoudiniVersion(agenda); 
      if((hvid != null) && (hvid.compareTo(new VersionID("9.0.0")) >= 0))
        picture = "vm_picture";

      if(targetSeq.hasFrameNumbers()) {
	FilePattern fpat = targetSeq.getFilePattern();
	FrameRange frange = targetSeq.getFrameRange();
	out.write("opparm " + opname + " trange on\n" +
		  "opparm " + opname + " f1 " + frange.getStart() + "\n" +
		  "opparm " + opname + " f2 " + frange.getEnd() + "\n" +
		  "opparm " + opname + " f3 " + frange.getBy() + "\n" +
		  "opparm " + opname + " " + picture + " '" + fpat.getPrefix() + ".$F");

	if(fpat.getPadding() > 1) 
	  out.write(String.valueOf(fpat.getPadding()));
	
	out.write("." + fpat.getSuffix() + "'\n");
      }
      else {
	out.write("opparm " + opname + " trange off\n" +
		  "opparm " + opname + " " + picture + " '" + targetSeq + "'\n");
      }

      writePreRenderScriptOpparm(opname, agenda, out); 
      writePostRenderScriptOpparm(opname, agenda, out); 
      writePreFrameScriptOpparm(opname, agenda, out); 
      writePostFrameScriptOpparm(opname, agenda, out); 

      out.write("opparm -c " + opname + " execute\n" + 
                "quit\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + hscript + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>(); 
      args.add("-v"); 

      if(getSingleBooleanParamValue(aUseGraphicalLicense)) 
        args.add("-R"); 

      args.add(source.toOsString());
      args.add(hscript.toOsString());

      return createSubProcess(agenda, "hbatch", args, outFile, errFile);
    }
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5581918117123812875L;

}

