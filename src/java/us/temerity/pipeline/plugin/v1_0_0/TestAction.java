// $Id: TestAction.java,v 1.7 2004/11/19 06:45:56 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   A C T I O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A test action with at lease one of each type of parameter.
 */
public
class TestAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TestAction() 
  {
    super("Test",  new VersionID("1.0.0"),
	  "A test action with at lease one of each type of parameter.");

    {
      ActionParam param = 
	new IntegerActionParam("SomeInteger", 
			       "An integer parameter.",
			       123);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam("SomeDouble", 
			      "An double parameter.",
			      123.456);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam("SomeString",
			      "A short string parameter.",
			      "test");
      addSingleParam(param);
    }

    {
      ArrayList<String> colors = new ArrayList<String>();
      colors.add("Red");
      colors.add("Yellow");
      colors.add("Green");
      colors.add("Cyan");
      colors.add("Blue");
      colors.add("Purple");

      ActionParam param = 
	new EnumActionParam("SomeEnum",
			    "An enum parameter.",
			    "Purple", colors);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam("SomeLink",
			    "An upstream link parameter.",
			    null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup("ActionParameters", true);
      layout.addEntry("SomeString");

      {
	LayoutGroup nlayout = new LayoutGroup("Numbers", false);
	nlayout.addEntry("SomeDouble");
	nlayout.addEntry("SomeInteger");

	layout.addSubGroup(nlayout);
      }

      {
	LayoutGroup mlayout = new LayoutGroup("Miscellaneous", true);
	mlayout.addEntry("SomeLink");
	mlayout.addSeparator();
	mlayout.addSeparator();
	mlayout.addEntry("SomeEnum");

	layout.addSubGroup(mlayout);
      }

      setSingleLayout(layout);
    }

    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add("AnotherDouble");
      layout.add("AnotherInteger");
      layout.add("AnotherLink");
      layout.add("AnotherString");
      layout.add("AnotherEnum");

      setSourceLayout(layout);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
    
    {
      ActionParam param = 
	new IntegerActionParam("AnotherInteger", 
			       "An integer parameter.",
			       123);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new DoubleActionParam("AnotherDouble", 
			      "An double parameter.",
			      123.456);
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new StringActionParam("AnotherString",
			      "A short string parameter.",
			      "test");
      params.put(param.getName(), param);
    }

    {
      ArrayList<String> colors = new ArrayList<String>();
      colors.add("Red");
      colors.add("Yello");
      colors.add("Green");
      colors.add("Cyan");
      colors.add("Blue");
      colors.add("Purple");

      ActionParam param = 
	new EnumActionParam("AnotherEnum",
			    "An enum parameter.",
			    "Cyan", colors);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam("AnotherLink",
			    "An upstream link parameter.",
			    null);
      params.put(param.getName(), param);
    }

    return params;
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
    throw new PipelineException("Not implemented yet...");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1201400081088512588L;

}



