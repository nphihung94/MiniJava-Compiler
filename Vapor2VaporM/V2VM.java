import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;
import V2VM.*;


import java.io.InputStreamReader;
import java.util.*;

public class V2VM {
  public static void main (String[] args) throws Throwable {
    Op[] ops = {Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS, Op.PrintIntS, Op.HeapAllocZ, Op.Error};
    VaporProgram inputProgram = null;
    boolean allowLocals = true;
    String[] registers = null;
    boolean allowStack = false;
    try {
      inputProgram = VaporParser.run(new InputStreamReader(System.in),1,1,
                      Arrays.asList(ops),allowLocals,registers,allowStack);
    }
      catch (ProblemException error) {
        System.out.println(error.getMessage());
        System.exit(1);
      }
    //System.out.println("Finish parsing vapor");
    Translator translateV2VM = new Translator(inputProgram);
  }
}
