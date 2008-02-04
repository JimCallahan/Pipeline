package com.intelligentcreatures.pipeline.plugin.PFTrackEditor.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S C I T E D   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * PFTrack match mover. 
 */
public
class PFTrackEditor 
extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  PFTrackEditor()
  {
    super("PFTrack", new VersionID("1.0.0"), "ICVFX", 
          "PFTrack match mover", 
          "pftrack");

    underDevelopment();

  }



  public SubProcessLight
  prep
  (
   String author,
   FileSeq fseq,
   Map<String, String> env,
   File dir
  )
    throws PipelineException
  {
    if(!fseq.isSingle())
      throw new PipelineException
        (" ");

    String sceneFile = fseq.getFile(0).toString();

    File pscFile = createTemp("psc");
    // first create IMS FILE
    // #IMS 
    // number of frames
    // framerate
    // /path/to each frame
    // 1.cin
    // 2.cin

    // write PFTrackScript file
    // #PFTrackScript v1.0
    // <importMovie> "/path/to/created/ims file"
    // <exit>

    // execute pftrack with psc file 
    // 
    try {
      FileWriter out = new FileWriter(pscFile);

      out.write("#PFTrackScript v1.0 \n " + 
                " <loadShot> \""+ sceneFile + "\"\n" + "<exit>\n");
      out.close();


    }
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary PSC script file (" + pscFile + ") " +
         ex.getMessage());
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add(pscFile.getPath());




    return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public static final long serialVersionUID = 2947241235055623518L;

}


