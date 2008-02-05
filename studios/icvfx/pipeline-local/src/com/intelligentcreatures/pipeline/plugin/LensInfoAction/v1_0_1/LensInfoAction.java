
package com.intelligentcreatures.pipeline.plugin.LensInfoAction.v1_0_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L E NS  I N F O  A C T I O N                                                           */
/*------------------------------------------------------------------------------------------*/

/** 
 * 
 */
public
class LensInfoAction
extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  LensInfoAction() 
  {
    super("LensInfo", new VersionID("1.0.1"), "ICVFX",
	  "Create lens information"); 

    {
      ActionParam param = 
	new Tuple2dActionParam
	(aLens,
	 "Lens Start/End", 
	 null);
      addSingleParam(param);
    } 
  
    {
      ActionParam param =
        new Tuple2dActionParam
        (aFocus,
         "Focus Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aHeight,
         "Height Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aTilt,
         "Tilt Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aStop,
         "Stop Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aFilter,
         "Filter Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aPanAngle,
         "Pan Angle Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aDistToSub,
         "DistToSub Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aRollAngle,
         "RollAngle Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aTrack,
         "Track Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aEastWest,
         "EastWest Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aSwing,
         "Swing Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aLiftBoom,
         "LiftBoom Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aZoom,
         "Zoom Start/End",
         null);
      addSingleParam(param);
    }

    {
      ActionParam param =
        new Tuple2dActionParam
        (aRamp, 
         "Ramp Start/End",
         null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aLens);   
      layout.addEntry(aFocus);
      layout.addEntry(aHeight);
      layout.addEntry(aTilt);
      layout.addEntry(aStop);
      layout.addEntry(aFilter);
      layout.addEntry(aPanAngle);
      layout.addEntry(aDistToSub);
      layout.addEntry(aRollAngle);
      layout.addEntry(aTrack);
      layout.addEntry(aEastWest);
      layout.addEntry(aSwing);
      layout.addEntry(aLiftBoom);
      layout.addEntry(aZoom);
      layout.addEntry(aRamp);

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   *
   *
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
    /* create the process to run the action */
    if(PackageInfo.sOsType == OsType.Windows) {
      File script = createTemp(agenda, ".bat");
      try {
        FileWriter out = new FileWriter(script);

        Path wpath = agenda.getTargetPath();

        for(Path target : agenda.getPrimaryTarget().getPaths()) {
          Path path = new Path(wpath, target);
          out.write("@echo off > " + path.toOsString() + "\n");
        }

        for(FileSeq fseq : agenda.getSecondaryTargets()) {
          for(Path target : fseq.getPaths()) {
            Path path = new Path(wpath, target);
            out.write("@echo off > " + path.toOsString() + "\n");
          }
        }

        out.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary BAT file (" + script + ") for Job " +
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }

      return createScriptSubProcess(agenda, script, outFile, errFile);
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : agenda.getPrimaryTarget().getFiles())
        args.add(file.toString());

      for(FileSeq fseq : agenda.getSecondaryTargets()) {
        for(File file : fseq.getFiles())
          args.add(file.toString());
      }

      return createSubProcess(agenda, "touch", args, outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public static final long serialVersionUID = -8536967306812150859L;

  public static final String aLens 	 = "Lens";
  public static final String aFocus 	 = "Focus";
  public static final String aHeight 	 = "Height";
  public static final String aTilt 	 = "Tilt";
  public static final String aStop 	 = "Stop";
  public static final String aFilter 	 = "Filter";
  public static final String aPanAngle 	 = "PanAngle";
  public static final String aDistToSub  = "DistToSub";
  public static final String aRollAngle  = "RollAngle";
  public static final String aTrack 	 = "Track";
  public static final String aEastWest 	 = "EastWest";
  public static final String aSwing 	 = "Swing";
  public static final String aLiftBoom 	 = "LiftBoom";
  public static final String aZoom 	 = "Zoom";
  public static final String aRamp  	 = "Ramp";

}

