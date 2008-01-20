// $Id: SyflexCacheCopyAction.java,v 1.2 2008/01/20 06:42:17 jim Exp $

package us.temerity.pipeline.plugin.SyflexCacheCopyAction.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S Y F L E X   C A C H E   C O P Y   A C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

public 
class SyflexCacheCopyAction 
extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public SyflexCacheCopyAction()
  {
    super("SyflexCacheCopy", new VersionID("2.0.9"), "Temerity", "Copies a syflex cache");

    {
      ActionParam param = 
        new StringActionParam
        (aClothObject,
         "The name of the piece of cloth to be simulated.", 
         null);
      addSingleParam(param);
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
    /* sanity checks */ 
    {
      String sname = null;
      {
        TreeSet<String> sources =  new TreeSet<String>(agenda.getSourceNames());

        if(sources.size() != 1)
          throw new PipelineException
            ("Only one source is allowed.");
        sname = sources.first();
      }
      /* create a temporary script file */ 
      File script = createTemp(agenda, 0755, "bash");
      try {      
        FileWriter out = new FileWriter(script);

        out.write("#!/bin/bash\n\n");

        String cpOpts = "--remove-destination";
        if(PackageInfo.sOsType == OsType.MacOS)
          cpOpts = "-f";

        /* the primary file sequences */ 
        {
          FileSeq target = agenda.getPrimaryTarget();
          NodeID snodeID = new NodeID(agenda.getNodeID(), sname);

          Path spath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
          FileSeq source = agenda.getPrimarySource(sname);
          int wk;
          for(wk=0; wk<target.numFrames(); wk++) {
            out.write("cp " + cpOpts + " " +
                      spath + "/" + source.getFile(wk) + " " +
                      target.getFile(wk) + "\n");
          }
        }
        out.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary script file (" + script + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }
      /* create the process to run the action */ 
      try {
        return new SubProcessHeavy
          (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
           script.getPath(), new ArrayList<String>(), 
           agenda.getEnvironment(), agenda.getWorkingDir(), 
           outFile, errFile);
      }
      catch(Exception ex) {
        throw new PipelineException
          ("Unable to generate the SubProcess to perform this Action!\n" +
           ex.getMessage());
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -18961943105506416L;

  private static final String aClothObject = "ClothObject";

}
