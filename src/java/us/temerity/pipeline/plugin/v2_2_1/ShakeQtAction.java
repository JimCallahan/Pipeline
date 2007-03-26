// $Id: ShakeQtAction.java,v 1.1 2007/03/26 23:23:55 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   Q T   A C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a QuickTime movie from a sequence of images using Shake. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images used to create the movie. 
 *   </DIV> <BR>
 * 
 *   FPS <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of image frames per second.
 *   </DIV>
 * </DIV> <P> 
 */
public
class ShakeQtAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShakeQtAction() 
  {
    super("ShakeQt", new VersionID("2.2.1"), "Temerity",
	  "Creates a QuickTime movie from a sequence of images using Shake."); 

    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source which contains the images used to create the movie.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aFPS,
	 "The number of image frames per second.",
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);   
      layout.addEntry(aFPS); 

      setSingleLayout(layout);  
    }

    removeSupport(OsType.Unix);
    addSupport(OsType.MacOS);
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
    /* target quicktime movie */ 
    Path target = getPrimaryTargetPath(agenda, "mov", "QuickTime movie file (.mov)");

    /* source images */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
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
    
    /* create temporary Python script to run Shake and copy the movie 
         (for some reason the Qt output has to be on the local machine!) */ 
    File script = createTemp(agenda, "py");
    try {      
      FileWriter out = new FileWriter(script);

      /* import modules */ 
      out.write("import shutil\n");

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* run shake */ 
      Path movie = new Path(createTemp(agenda, "mov"));
      {
        out.write("launch('shake', ['-v'"); 
        
        Double fps = (Double) getSingleParamValue(aFPS);
        if(fps != null)
          out.write(", '-fps', '" + fps + "'");
        
        out.write(", '-fi'"); 
        if(sourceSeq.isSingle()) {
          Path path = new Path(sourcePath, sourceSeq.getPath(0));
          out.write(", '" + path + "'"); 
        }
        else {
          out.write(", '" + sourcePath.toOsString() + "/" + sourceSeq.getFilePattern() + "'" +
                    ", '-t', '" + sourceSeq.getFrameRange() + "'");
        }
        
        out.write(", '-fo', '" + movie + "'])\n");
      }

      /* copy the temporary movie to its target location */ 
      out.write("shutil.copy('" + movie + "', '" + target + "')\n");
      
      out.write("\n" + 
                "print 'ALL DONE.'\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary Python script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8177844794925006156L;

  public static final String aImageSource = "ImageSource";
  public static final String aFPS         = "FPS";

}

