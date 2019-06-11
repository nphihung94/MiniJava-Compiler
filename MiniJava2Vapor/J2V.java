import syntaxtree.*;
import src.*;
import visitor.GJNoArguDepthFirst;
import java.util.*;
import java.lang.*;

public class J2V{
  public static void main(String[] args) {
    try {
      Node goal = null;
      goal = new MiniJavaParser(System.in).Goal();

      VirtualTable mySymbolTable = new VirtualTable();
      goal.accept(mySymbolTable);

      Translator Translate = new Translator(mySymbolTable);
      LinkedList<String> vaporProg = goal.accept(Translate);
      for (String line: vaporProg) {
        System.out.println(line);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
      System.exit(-1);
    }

  }
}
