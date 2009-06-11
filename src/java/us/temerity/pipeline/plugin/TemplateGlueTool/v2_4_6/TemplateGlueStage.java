// $Id: TemplateGlueStage.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

public 
enum TemplateGlueStage
{
  ScanNetwork,
  InitialSummary,
  StringReplacement,
  FrameRanges,
  AoEModes,
  OptionalBranches,
  Contexts,
  ContextDefaults,
  Externals,
  FinalSummary;

  @Override
  public String 
  toString() 
  {
    switch(this) {
    case ScanNetwork:
      return "Scan Network";
    case StringReplacement:
      return "String Replacements";
    case FrameRanges:
      return "Frame Ranges";
    case AoEModes:
      return "AoE Modes";
    case OptionalBranches:
      return "Optional Branches";
    case Contexts:
      return "Contexts";
    case ContextDefaults:
      return "Context Defaults";
    case Externals:
      return "Externals";
    case InitialSummary:
      return "Initial Summary";
    case FinalSummary:
      return "Final Summary";
    default:
      return null;
    }
  }
  
  public static TemplateGlueStage
  getNextStage
  (
    TemplateGlueStage currentStage   
  )
  {
    int ordinal = currentStage.ordinal();
    ordinal++;
    TemplateGlueStage array[] = values();
    if (ordinal > (array.length - 1))
      return null;
    else
      return array[ordinal];
  }
  
  public static TemplateGlueStage
  getPreviousStage
  (
    TemplateGlueStage currentStage   
  )
  {
    int ordinal = currentStage.ordinal();
    ordinal--;
    TemplateGlueStage array[] = values();
    if (ordinal > 0 )
      return array[ordinal];
    else
      return null;
  }
}
