// $Id: TestJarReaderApp.java,v 1.1 2006/08/20 05:46:51 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*; 
import java.util.*; 
import java.util.jar.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   J A R   R E A D E R                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class TestJarReaderApp
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
    FileCleaner.init();

    try {
      TestJarReaderApp app = new TestJarReaderApp();
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
  { 
    TreeMap<String,byte[]> contents = new TreeMap<String,byte[]>();

    try {
      File jarFile = new File("../all.jar"); 

      JarInputStream in = new JarInputStream(new FileInputStream(jarFile));
      
      byte buf[] = new byte[4096];
      while(true) {
	JarEntry entry = in.getNextJarEntry();
	if(entry == null) 
	  break;

	if(!entry.isDirectory()) {
	  String path = entry.getName(); 
	  if(path.endsWith("class")) {
	    String cname = path.substring(0, path.length()-6).replace('/', '.'); 

	    ByteArrayOutputStream out = new ByteArrayOutputStream();

	    while(true) {
	      int len = in.read(buf, 0, buf.length); 
	      if(len == -1) 
		break;
	      out.write(buf, 0, len);
	    }
	    
	    contents.put(cname, out.toByteArray());
	  }
	}
      }

      in.close();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 

    for(String name : contents.keySet()) {
      byte[] data = contents.get(name);
      System.out.print(name + "\n" + 
		       "  DataSize = " + data.length + "\n\n");
    }
  }
}
