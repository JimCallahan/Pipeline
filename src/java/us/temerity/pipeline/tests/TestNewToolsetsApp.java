// $Id: TestNewToolsetsApp.java,v 1.6 2005/01/22 06:10:10 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.toolset.*;

import java.io.*; 
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   T O O L S E T S                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestNewToolsetsApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    
    //Logs.sub.setLevel(Level.FINEST);

    try {
      TestNewToolsetsApp app = new TestNewToolsetsApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
    throws PipelineException, GlueException
  {
    File dir = new File("data/toolsets");

    TreeSet<File> pnames = new TreeSet<File>();
    {
      String files[] = dir.list();
      int wk;
      for(wk=0; wk<files.length; wk++) {
	File file = new File(dir, files[wk]);
	if(file.isFile()) 
	  pnames.add(file);
      }
    }

    LinkedList<PackageCommon> packages = new LinkedList<PackageCommon>();
    for(File file : pnames) {
      PackageMod mod = new PackageMod(file.getName());
      mod.loadShellScript(file);
      printPackage(mod);
      
      packages.add(mod);
    }

    Toolset tset = new Toolset("Testing", packages);
    printToolset(tset);
  }

  



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
  printPackage
  (
   PackageCommon com
  ) 
    throws GlueException
  { 
    GlueEncoder ge = new GlueEncoderImpl("Package", com);
    System.out.print(ge.getText() + "\n");
  }


  private void 
  printToolset
  (
   Toolset tset
  ) 
    throws GlueException
  { 
    if(tset.hasModifiablePackages() || tset.hasConflicts()) {
      System.out.print("Toolset: " + tset.getName() + "\n" + 
		       "  Packages:\n");
      {
	int idx;
	for(idx=0; idx<tset.getNumPackages(); idx++) {
	  String name = tset.getPackageName(idx);
	  System.out.print("    " + name + " [" + tset.getPackageVersionID(idx) + "]\n");
	}
      }
      
      if(tset.hasConflicts()) {
	System.out.print("  Conflicts:\n");
	for(String name : tset.getConflictedEnvNames()) {
	  System.out.print("    " + name + ": ");
	  
	  int idx;
	  for(idx=0; idx<tset.getNumPackages(); idx++) {
	    if(tset.isPackageEnvConflicted(idx, name)) 
	      System.out.print(tset.getPackageName(idx) + " ");
	  }
	  System.out.print("\n");
	}
      }

      System.out.print("  Environment:\n");
      TreeMap<String,String> env = tset.getEnvironment();
      for(String name : env.keySet()) 
	System.out.print("    " + name + " = " + env.get(name) + "\n");
      System.out.print("\n");
    }
    else {
      GlueEncoder ge = new GlueEncoderImpl("Toolset", tset);
      System.out.print(ge.getText() + "\n");
    }
  }
 
}
