// $Id: DisneyBuild.java,v 1.2 2009/06/11 05:38:37 jesse Exp $

package us.temerity.pipeline.jesse;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_3.*;


public class DisneyBuild
{

  /**
   * @param args
   * @throws PipelineException 
   */
  public static void 
  main
  (
    String[] args
  ) 
  throws PipelineException
  {
    PluginMgrClient.init();
    
    TreeSet<String> nodeList = new TreeSet<String>();
    {
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/anim/edit/SEQ_SHOT_anim");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/anim/prepare/cache/ASSET_TYPE_cache");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/anim/prepare/combine/ASSET_TYPE_build");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/anim/prepare/export/ASSET_TYPE");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/comp/edit/SEQ_SHOT_comp");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/comp/render/SEQ_SHOT_final");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/lgt/edit/SEQ_SHOT_light");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/lgt/edit/SEQ_SHOT_preLight");
      nodeList.add("/templates/PROJECT/prod/SUB/SEQ/SHOT/lgt/render/pass/SEQ_SHOT_PASS");
    }

    
    Integer seqNum = 0;
    if (args.length > 0)
      seqNum = Integer.valueOf(args[0]);
    ArrayList<Thread> threads = new ArrayList<Thread>();
    
    ArrayList<String[]> assets = new ArrayList<String[]>();
    {
      String[] array = {"bob", "char"};
      assets.add(array);
    }
    {
      String[] array = {"tom", "char"};
      assets.add(array);
    }
    {
      String[] array = {"harry", "char"};
      assets.add(array);
    }
    {
      String[] array = {"sheila", "char"};
      assets.add(array);
    }
    {
      String[] array = {"louise", "char"};
      assets.add(array);
    }
    {
      String[] array = {"bucket", "prop"};
      assets.add(array);
    }
    {
      String[] array = {"bottle", "prop"};
      assets.add(array);
    }
    {
      String[] array = {"house", "set"};
      assets.add(array);
    }
    {
      String[] array = {"livingRoom", "set"};
      assets.add(array);
    }
    {
      String[] array = {"car", "vehicle"};
      assets.add(array);
    }
    {
      String[] array = {"boat", "vehicle"};
      assets.add(array);
    }
    {
      String[] array = {"bus", "vehicle"};
      assets.add(array);
    }
    
    ArrayList<String> passes = 
      new ArrayList<String>();
    {
      Collections.addAll(passes, "ambOCC", "diff", "spec", "matte", "rgb", "light" );
    }
    
    TripleMap<String, Integer, Integer, Boolean> shots = 
     new TripleMap<String, Integer, Integer, Boolean>();
    
    ArrayList<String> projects = new ArrayList<String>();
    projects.add("projA");
    projects.add("projB");
    
    int assetEnd = assets.size() - 1;
    int passEnd = passes.size() - 1;
    
    LogMgr.getInstance().setLevel(Kind.Ops, Level.Fine);
    
    int iters = 20;
    for (int i = 0; i < iters; i++) {
      
      TreeMap<String, String> replacements = new TreeMap<String, String>();
      replacements.put("templates", "projects");
      replacements.put("PROJECT_TEST", "disney");
      
      
      {
        String project = projects.get(random(0, 1));
        int seq = random(seqNum, seqNum + 10);
        int shot = random(0, 500);
        while (shots.get(project, seq, shot) != null) {
          shot = random(0, 500);
        }
        shots.put(project, seq, shot, true);
        
        replacements.put("SUB", project);
        replacements.put("SEQ", "s" + pad(seq));
        replacements.put("SHOT", "s" + pad(shot));
      }

      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts = 
        new TreeMap<String, ArrayList<TreeMap<String,String>>>();
      {
        TreeSet<Integer> assetIdx = new TreeSet<Integer>();
        {
          int numAssets = random(3, assetEnd + 1);

          while (assetIdx.size() < numAssets) {
            assetIdx.add(random(0, assetEnd));
          }
        }
        ArrayList<TreeMap<String, String>> assetContext = 
          new ArrayList<TreeMap<String,String>>();
        for (int idx : assetIdx) {
          String[] array = assets.get(idx);
          TreeMap<String, String> each = 
            new TreeMap<String, String>();
          each.put("ASSET", array[0]);
          each.put("TYPE", array[1]);
          assetContext.add(each);
        }
        contexts.put("asset", assetContext);
      }
      
      {
        TreeSet<Integer> passIdx = new TreeSet<Integer>();
        {
          int numPasses = random(1, passEnd + 1);

          while (passIdx.size() < numPasses) {
            passIdx.add(random(0, passEnd));
          }
        }
        ArrayList<TreeMap<String, String>> passContext = 
          new ArrayList<TreeMap<String,String>>();
        for (int idx : passIdx) {
          String name= passes.get(idx);
          TreeMap<String, String> each = 
            new TreeMap<String, String>();
          each.put("PASS", name);
          passContext.add(each);
        }
        {
          TreeMap<String, String> each = 
            new TreeMap<String, String>();
          each.put("PASS", "beau");
          passContext.add(each);
        }
        contexts.put("pass", passContext);
      }
      
      TreeMap<String, FrameRange> ranges =
        new TreeMap<String, FrameRange>();
      FrameRange range = new FrameRange(1, random(400, 800), 1);
      ranges.put("range", range);
      
      TemplateBuildInfo info = new TemplateBuildInfo();
      info.setNodesToBuild(nodeList);

      RunBuilder run = 
        new RunBuilder(info, replacements, contexts, ranges, new TreeMap<String, ActionOnExistence>());
      threads.add(run);
      run.start();
    }
    for (Thread t : threads) {
      try {
        t.join();
      }
      catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  public static
  class RunBuilder
    extends Thread
  {
    public
    RunBuilder
    (
      TemplateBuildInfo templateInfo,
      TreeMap<String, String> stringReplacements,
      TreeMap<String, ArrayList<TreeMap<String, String>>> contexts,
      TreeMap<String, FrameRange> frameRanges,
      TreeMap<String, ActionOnExistence> aoeModes
    ) 
    {
      pTemplateInfo = templateInfo;
      pStringReplacements = stringReplacements;
      pContexts = contexts;
      pFrameRanges = frameRanges;
      pAoeModes = aoeModes;
    }
    
    @Override
    public void 
    run()
    {
      MasterMgrClient mclient = new MasterMgrClient();
      QueueMgrClient qclient = new QueueMgrClient();
      BuilderInformation information = 
        new BuilderInformation(false, false, true, true, new MultiMap<String, String>());
      
      try {
        TemplateBuilder builder = 
          new TemplateBuilder(mclient, qclient, information, pTemplateInfo, pStringReplacements, pContexts, pFrameRanges, pAoeModes, new TreeMap<String, TemplateExternalData>(), null);
        builder.run();
      }
      catch (PipelineException ex) {
        ex.printStackTrace();
      }
      finally {
        mclient.disconnect();
        qclient.disconnect();
      }
    }
    
    private TemplateBuildInfo pTemplateInfo;
    private TreeMap<String, String> pStringReplacements;
    private TreeMap<String, ArrayList<TreeMap<String, String>>> pContexts;
    private TreeMap<String, FrameRange> pFrameRanges;
    private TreeMap<String, ActionOnExistence> pAoeModes;
  }
  
  private static String 
  pad
  (
    int i
  )
  {
    String pad = String.valueOf(i);
    while(pad.length() < 3)
      pad = "0" + pad;
    return pad;
  }
  
  private static int
  random
  (
    double start,
    double end
  )
  {
    
    int n = (int)((end - start + 1) * Math.random() + start);
    return n;
  }
}
