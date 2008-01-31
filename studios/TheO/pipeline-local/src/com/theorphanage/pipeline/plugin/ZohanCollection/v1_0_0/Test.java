package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;

public class Test {

  /**
   * @param args
   * @throws PipelineException 
   */
  public static void main(String[] args) throws PipelineException {
    
    MasterMgrClient client = new MasterMgrClient();
    QueueMgrClient queue = new QueueMgrClient();
    UtilContext util = new UtilContext("jesse_clemens", "default", "test3");
    StudioDefinitions defs = new StudioDefinitions(client, queue, util);
    DoubleMap<String, String, ArrayList<String>> toReturn = defs.getAllProjectsAllNamesForParam();
    for (String p : toReturn.keySet()) {
      System.out.println("Project: " +  p);
      for (String s : toReturn.keySet(p)) {
        System.out.println("\tSequence: " +  s);
        ArrayList<String> shots = toReturn.get(p, s);
        for (String shot : shots)
          System.out.println("\t\tShot: " +  shot);
      }
    }

  }

}
