package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

/*------------------------------------------------------------------------------------------*/
/*   P A R A M   M A N I F E S T   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Different stages for data entry.
 */
public 
enum ParamManifestStage
{
  StringReplacement,
  AoEModes,
  OptionalBranches,
  Contexts,
  FrameRanges,
  Offsets,
  Externals;
  
  
  public static ParamManifestStage
  getNextStage
  (
    ParamManifestStage currentStage   
  )
  {
    int ordinal = currentStage.ordinal();
    ordinal++;
    ParamManifestStage array[] = values();
    if (ordinal > (array.length - 1))
      return null;
    else
      return array[ordinal];
  }
  
  @Override
  public String 
  toString() 
  {
    switch(this) {
    case StringReplacement:
      return "String Replacements";
    case FrameRanges:
      return "Frame Ranges";
    case Offsets:
      return "Offsets";
    case AoEModes:
      return "AoE Modes";
    case OptionalBranches:
      return "Optional Branches";
    case Contexts:
      return "Contexts";
    case Externals:
      return "Externals";
    default:
      return null;
    }
  }
  
  public static ParamManifestStage
  getPreviousStage
  (
    ParamManifestStage currentStage   
  )
  {
    int ordinal = currentStage.ordinal();
    ordinal--;
    ParamManifestStage array[] = values();
    if (ordinal >= 0 )
      return array[ordinal];
    else
      return null;
  }
}
