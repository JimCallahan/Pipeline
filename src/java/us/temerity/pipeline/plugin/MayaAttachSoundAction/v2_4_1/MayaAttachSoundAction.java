// $Id: MayaAttachSoundAction.java,v 1.1 2008/03/06 13:02:57 jim Exp $

package us.temerity.pipeline.plugin.MayaAttachSoundAction.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.ArrayList;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M A K E   G E O   C A C H E   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a MEL script which when executed will attach an sound file to the Maya scene 
 * and setup the time slider to play the audio track when scrubbing.<P> 
 * 
 * See the Maya documentation for the MEL command (sound) for details.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Sound File <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the sound file to attach. 
 *   </DIV> <BR>
 *   
 *   Pre Attach MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate before attaching the sound file. 
 *   </DIV> <BR>
 *   
 *   Post Attach MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate after attaching the sound file. 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaAttachSoundAction
  extends MayaBuildUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaAttachSoundAction() 
  {
    super("MayaAttachSound", new VersionID("2.4.1"), "Temerity",
          "Creates a MEL script which when executed will attach an sound file to the " + 
          "Maya scene and setup the time slider to play the audio track when scrubbing.");  

    {
      ActionParam param = 
	new LinkActionParam
	(aSoundFile, 
	 "The source node which contains the sound file to attach.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new LinkActionParam
        (aPreAttachMEL,
         "The MEL script to evaluate before attaching the geometry cache.", 
         null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new LinkActionParam
        (aPostAttachMEL,
         "The MEL script to evaluate after attaching the geometry cache.",
         null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aSoundFile);
      layout.addSeparator(); 
      layout.addEntry(aPreAttachMEL);
      layout.addEntry(aPostAttachMEL);

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
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mel", "MEL script");

    /* MEL script paths */ 
    Path preAttachMEL  = getMelScriptSourcePath(aPreAttachMEL, agenda);
    Path postAttachMEL = getMelScriptSourcePath(aPostAttachMEL, agenda);

    /* the source sound file */ 
    String soundName = null;
    Path soundPath = null; 
    {
      ActionParam param = getSingleParam(aSoundFile);
      if(param == null) 
        throw new PipelineException
          ("There is no Action parameter named (" + aSoundFile + ")!");

      String title = param.getNameUI();

      String mname = (String) param.getValue();
      if(mname != null) {
        FileSeq fseq = agenda.getPrimarySource(mname);
        if(fseq == null) 
          throw new PipelineException
            ("Somehow the " + title + " node (" + mname + ") was not one of the " + 
             "source nodes!");
      
        String suffix = fseq.getFilePattern().getSuffix();
        if(!fseq.isSingle() || (suffix == null)) {
          throw new PipelineException
            ("The " + getName() + " Action requires that the source node specified by the " + 
             title + " parameter (" + mname + ") must have a single Sound File " + 
             "file as its primary file sequence!");
        }
        
        soundName = fseq.getFilePattern().getPrefix();

        Path spath = new Path(mname);
        soundPath = new Path(spath.getParentPath(), fseq.getPath(0));
      }
    }
    
    /* create a temporary MEL script to create the geometry sound */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script); 

      if(preAttachMEL != null) 
	out.write("// PRE-ATTACH SCRIPT\n" + 
                  "print \"Pre-Attach Script: " + preAttachMEL + "\\n\";\n" +
                  "source \"" + preAttachMEL + "\";\n\n");

      out.write
        ("// ATTACH THE SOUND FILE\n" + 
         "{\n" + 
         "  sound -file \"$WORKING" + soundPath + "\" -name \"" + soundName + "\";\n" +
         "  global string $gPlayBackSlider;\n" + 
         "  timeControl -e -sound \"" + soundName + "\" " + 
                       "-displaySound true $gPlayBackSlider;\n" + 
         "  timeControl -e -pressCommand \"timeControl -e -beginScrub $gPlayBackSlider\" " + 
                       "-e -releaseCommand \"timeControl -e -endScrub $gPlayBackSlider\" " + 
                       "$gPlayBackSlider;\n" + 
         "}\n\n"); 

      if(postAttachMEL != null)
	out.write("// POST-ATTACH SCRIPT\n" + 
                  "print \"Post-Attach Script: " + postAttachMEL + "\\n\";\n" +
                  "source \"" + postAttachMEL + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, script, target, outFile, errFile);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5870482903480320293L;
  
  public static final String aSoundFile     = "SoundFile";
  public static final String aPostAttachMEL = "PostAttachMEL";
  public static final String aPreAttachMEL  = "PreAttachMEL";

}
