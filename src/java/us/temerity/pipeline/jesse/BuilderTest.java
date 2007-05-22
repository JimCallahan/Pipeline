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
//    argv.add("us.temerity.pipeline.builder.builders.NewAssetBuilder");
//    argv.add("--log=ops:finest,arg:finest,bld:finest");
//    //argv.add("--abort");
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
//    argv.add("--builder=NewAssetBuilder-BuildsAssetNames");
//    argv.add("--AssetName=boStaff");
//    argv.add("--AssetType=prop");
//
//    String list[] = argv.toArray(new String[0]);
//    BuilderApp app = new BuilderApp();
//    app.run(list);
//  }

  public static void main(String[] args)
  {
    ArrayList<String> argv = new ArrayList<String>();
    argv.add("us.temerity.pipeline.builder.tests.TopBuilder");
    argv.add("--log=ops:finest,arg:finest,bld:finest");
    //argv.add("--abort");
    argv.add("--gui");
    argv.add("--builder=TopBuilder");
    argv.add("--NumberOfChildren=3");

    String list[] = argv.toArray(new String[0]);
    BuilderApp app = new BuilderApp();
    app.run(list);
  }

}
