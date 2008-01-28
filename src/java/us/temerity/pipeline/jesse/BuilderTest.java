package us.temerity.pipeline.jesse;

import java.util.ArrayList;

import us.temerity.pipeline.core.BuilderApp;


public class BuilderTest
{

  /**
   * @param args
   */
//  public static void main(String[] args)
//  {
//    ArrayList<String> argv = new ArrayList<String>();
//    argv.add("us.temerity.pipeline.builder.maya2mr.v2_3_1.NewAssetBuilder");
//    argv.add("--log=ops:finest,arg:finest,bld:finest");
//    argv.add("--abort");
//    argv.add("--gui");
//    argv.add("--builder=NewAssetBuilder");
//    argv.add("--UtilContext-Author=jesse");
//    argv.add("--UtilContext-View=default");
//    argv.add("--UtilContext-Toolset=070215-2.2.1a");
//    argv.add("--MayaContext-AngularUnits=degrees");
//    argv.add("--MayaContext-LinearUnits=centimeter");
//    argv.add("--MayaContext-TimeUnits=NTSC (30 fps)");
//    argv.add("--ReleaseOnError=true");
//    argv.add("--BuildLowRez=true");
//    argv.add("--CheckinWhenDone=false");
//    argv.add("--BuildTextureNode=true");
//    argv.add("--BuildSeparateHead=false");
//    argv.add("--ActionOnExistance=CheckOut");
//    argv.add("--SelectionKeys-Unix=true");
//    argv.add("--ProjectName=clientB");
//    argv.add("--builder=NewAssetBuilder-DefaultAssetNames");
//    argv.add("--AssetName=boStaff");
//    argv.add("--AssetType=prop");
//
//    String list[] = argv.toArray(new String[0]);
//    BuilderApp app = new BuilderApp();
//    app.run(list);
//  }

//  public static void main(String[] args)
//  {
//    ArrayList<String> argv = new ArrayList<String>();
//    argv.add("us.temerity.pipeline.builder.tests.TopBuilder");
//    argv.add("--log=ops:finest,arg:finest,bld:finest");
//    //argv.add("--abort");
//    argv.add("--gui");
//    argv.add("--builder=TopBuilder");
//    argv.add("--NumberOfChildren=3");
//
//    String list[] = argv.toArray(new String[0]);
//    BuilderApp app = new BuilderApp();
//    app.run(list);
//  }
  
  public static void main(String[] args)
  {
    ArrayList<String> argv = new ArrayList<String>();
    argv.add("Maya2MR");
    argv.add("2.3.2");
    argv.add("Temerity");
    argv.add("Maya2MR");
    argv.add("--log=ops:finest,arg:finest,bld:finest");
    //argv.add("--log=bld:warning,ops:finest");
    argv.add("--ProjectName=testProject");
    argv.add("--UtilContext-Author=jim");
    argv.add("--UtilContext-View=seaside");
    //argv.add("--abort");
    //argv.add("--batch");

    String list[] = argv.toArray(new String[0]);
    BuilderApp app = new BuilderApp();
    app.run(list);
  }

}
