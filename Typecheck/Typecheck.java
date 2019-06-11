import syntaxtree.*;
import typechecker.*;
import visitor.GJNoArguDepthFirst;
import java.util.*;
import java.lang.*;

public class Typecheck{
  public static void main(String[] args) {
    try {
      Node goal = null;
      goal = new MiniJavaParser(System.in).Goal();

      SymbolTable mySymbolTable = new SymbolTable();
      if (goal.accept(mySymbolTable) == null) {
        System.out.println("Type error at ST");
        System.exit(1);
      }

      Typechecker typecheck = new Typechecker(mySymbolTable);
      if (goal.accept(typecheck) == null) {
        System.out.println("Type error");
        System.exit(1);
      }

      System.out.println("Program type checked successfully");
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println(e.getMessage());
      System.exit(-1);
    }

  }
}
