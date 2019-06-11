import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;


import java.io.InputStreamReader;
import java.util.*;

public class VM2M {
  public static void main (String[] args) throws Throwable {
    Op[] ops = {Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS, Op.PrintIntS, Op.HeapAllocZ, Op.Error};
    VaporProgram inputProgram = null;
    boolean allowLocals = false;
    String[] registers = {"v0","v1","a0","a1","a2","a3","t0","t1","t2","t3","t4","t5","t6","t7","s0","s1","s2","s3","s4","s5","s6","s7","t8"};
    boolean allowStack = true;
    try {
      inputProgram = VaporParser.run(new InputStreamReader(System.in),1,1,
                      Arrays.asList(ops),allowLocals,registers,allowStack);
    }
      catch (ProblemException error) {
        System.out.println(error.getMessage());
        System.exit(1);
      }
    //System.out.println("Finish parsing vapor");
    Translator translateVM2M = new Translator(inputProgram);
  }
}
