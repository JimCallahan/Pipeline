import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

import java.util.*;
import java.io.*;

public 
class CollapseTest {
  public static void 
  main(String args[]) 
  {
    try {
      Set<String> orig = null;    
      ListPathSet<String> pset = new ListPathSet<String>();
      Set<String> extr = new TreeSet<String>();
      {
        File infile = new File("/base/tmp/collapsed-nodes"); 
      
        orig = (Set<String>) GlueDecoderImpl.decodeFile("CollapsedNodePaths", infile);
        for(String str : orig) {
          String parts[] = str.split(":"); 
          
          LinkedList<String> path = new LinkedList<String>();
          for(String p : parts) { 
            if(p.length() > 0) 
              path.add(p);
          }

          pset.add(path); 
        }

        File outfile = new File("/base/tmp/collapsed-nodes2"); 
        GlueEncoderImpl.encodeFile("CollapsedNodePaths", pset, outfile); 

        LinkedList<LinkedList<String>> nested = pset.toNestedList();
        for(LinkedList<String> ls : nested) {
          StringBuilder buf = new StringBuilder();
          for(String s : ls) 
            buf.append(":" + s);
          buf.append(":"); 
          
          extr.add(buf.toString());
        }

        for(LinkedList<String> ls : nested) {
          if(!pset.contains(ls)) {
            System.out.print("Missing Path:\n"); 
            for(String s : ls)
              System.out.print("  " + s + "\n");
          }
        }
      }     

      System.out.print("Orig Size = " + orig.size() + "\n" +
                       "Extr Size = " + extr.size() + "\n\n"); 
   
      {
        FileWriter out = new FileWriter(new File("/base/tmp/orig.raw"));
        for(String s : orig) 
          out.write(s + "\n");
        out.close();
      }
     
      {
        FileWriter out = new FileWriter(new File("/base/tmp/extr.raw"));
        for(String s : extr) 
          out.write(s + "\n");
        out.close();
      }

      {
        FileWriter out = new FileWriter(new File("/base/tmp/first.raw"));
        for(String s : pset.getFirstElements()) 
          out.write(s + "\n");
        out.close();
      }

      {
        Random rand = new Random(System.currentTimeMillis());

        String[] ary = orig.toArray(new String[0]);
        int wk;
        for(wk=0; wk<ary.length*0.75; wk++) {
          int idx = rand.nextInt(ary.length); 
          String str = ary[idx];

          orig.remove(str); 
          
          {
            String parts[] = str.split(":"); 
          
            LinkedList<String> path = new LinkedList<String>();
            for(String p : parts) { 
              if(p.length() > 0) 
                path.add(p);
            }
            
            pset.remove(path); 
          }
        }

        {
          extr = new TreeSet<String>();

          LinkedList<LinkedList<String>> nested = pset.toNestedList();
          for(LinkedList<String> ls : nested) {
            StringBuilder buf = new StringBuilder();
            for(String s : ls) 
              buf.append(":" + s);
            buf.append(":"); 
            
            extr.add(buf.toString());
          }
        }
      }

      System.out.print("Orig2 Size = " + orig.size() + "\n" +
                       "Extr2 Size = " + extr.size() + "\n\n"); 
   
      {
        FileWriter out = new FileWriter(new File("/base/tmp/orig2.raw"));
        for(String s : orig) 
          out.write(s + "\n");
        out.close();
      }
     
      {
        FileWriter out = new FileWriter(new File("/base/tmp/extr2.raw"));
        for(String s : extr) 
          out.write(s + "\n");
        out.close();
      }      

      {
        FileWriter out = new FileWriter(new File("/base/tmp/first2.raw"));
        for(String s : pset.getFirstElements()) 
          out.write(s + "\n");
        out.close();
      }
    }
    catch(Exception ex) {
      System.out.print(Exceptions.getFullMessage(ex)); 
    }
  }
}
