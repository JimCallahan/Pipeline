// $Id: TestAnnotation.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TestAnnotation.v2_4_5;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   A N N O T A T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A test annotation with at lease one of each type of parameter.
 */
public 
class TestAnnotation
  extends BaseAnnotation
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TestAnnotation()
  {
    super("Test", new VersionID("2.4.5"), "Temerity", 
          "A test annotation with at lease one of each type of parameter.");
    
    {
      AnnotationParam param = 
	new BooleanAnnotationParam
        ("SomeBoolean", 
         "An boolean parameter.",
         false);
      addParam(param);
    }

    {
      AnnotationParam param = 
	new DoubleAnnotationParam
        ("SomeDouble", 
         "An double parameter.",
         123.456);
      addParam(param);
    }

    {
      ArrayList<String> colors = new ArrayList<String>();
      colors.add("Red");
      colors.add("Yellow");
      colors.add("Green");
      colors.add("Cyan");
      colors.add("Blue");
      colors.add("Purple");

      AnnotationParam param = 
	new EnumAnnotationParam
        ("SomeEnum",
         "An enum parameter.",
         "Purple", colors);
      addParam(param);
    }

    {
      AnnotationParam param = 
	new IntegerAnnotationParam
        ("SomeInteger", 
         "An integer parameter.",
         666);
      addParam(param);
    }

    {
      AnnotationParam param = 
	new TextAreaAnnotationParam
	("SomeTextArea", 
	 "A text area parameter.", 
	 "Some text\nwhich lasts more than\none line...",
	 5); 
      addParam(param);
    }
    
    {
      AnnotationParam param =
        new StringAnnotationParam
        ("SomeString",
         "A string parameter.",
         "Hi There");
      addParam(param);
    }
    
    {
      AnnotationParam param = 
	new PathAnnotationParam
	("SomePath", 
         "A file system path parameter.", 
         new Path("/some/path"));
      addParam(param);
    }
      
    {
      AnnotationParam param =
        new ToolsetAnnotationParam
        ("SomeToolset",
         "A toolst name parameter.", 
         null);
      addParam(param);
    }

    {
      AnnotationParam param = 
	new WorkGroupAnnotationParam
	("SomeWorkGroup", 
	 "A work group parameter.", 
	 true, true, null); 
      addParam(param);
    }
    
    {
      AnnotationParam param = 
        new BuilderIDAnnotationParam
        ("SomeBuilderID", 
         "A builder plugin ID parameter.", 
         null);
      addParam(param);
    }
    
    addContext(AnnotationContext.PerVersion);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7890707824297025907L;

}
