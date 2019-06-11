package typechecker;
import syntaxtree.*;
import visitor.GJNoArguDepthFirst;
import java.util.*;

public class Typechecker extends GJNoArguDepthFirst<MyType> {
  public SymbolTable mySymbolTable ;
  public boolean typeDefineScope;

  public Typechecker(SymbolTable symbols) {
    mySymbolTable = symbols;
    typeDefineScope = false;
  }

  //
  // Auto class visitors--probably don't need to be overridden.
  //
  public MyType visit(NodeList n) {
     for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
        if (e.nextElement().accept(this) == null)
          return null;
     }
     return MyType.OTHER;
  }

  public MyType visit(NodeListOptional n) {
     if ( n.present() ) {
        for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
           if (e.nextElement().accept(this) == null)
            return null;

        }
        return MyType.OTHER;
     }
     else
        return MyType.OTHER;
  }

  public MyType visit(NodeOptional n) {
     if ( n.present() )
      {
        if (n.node.accept(this) == null)
          return null;
        return MyType.OTHER;
      }

     else
        return MyType.OTHER;
  }

  public MyType visit(NodeSequence n) {
     for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
        if (e.nextElement().accept(this) == null)
          return null;
     }
     return MyType.OTHER;
  }

  //
  // User-generated visitor methods below
  //

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public MyType visit(Goal n) {
    if (n.f0.accept(this) == null)
      return null;
    System.out.println("MainClass accepted");
    return n.f1.accept(this);
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> "public"
   * f4 -> "static"
   * f5 -> "void"
   * f6 -> "main"
   * f7 -> "("
   * f8 -> "String"
   * f9 -> "["
   * f10 -> "]"
   * f11 -> Identifier()
   * f12 -> ")"
   * f13 -> "{"
   * f14 -> ( VarDeclaration() )*
   * f15 -> ( Statement() )*
   * f16 -> "}"
   * f17 -> "}"
   */
  public MyType visit(MainClass n) {
    mySymbolTable.currentClass = mySymbolTable.classes.get(n.f1.f0.toString());
    mySymbolTable.currentMethods = mySymbolTable.getMethod(mySymbolTable.currentClass.name,"mainMethod");
    System.out.println("Start checking statement: " + n.f15.toString());
    MyType ret = n.f15.accept(this);
    //if (ret == null)
    //  System.out.println("Wrong in accept statement");
    mySymbolTable.currentClass = null;
    mySymbolTable.currentMethods = null;
    return ret;
  }

  /**
   * f0 -> ClassDeclaration()
   *       | ClassExtendsDeclaration()
   */
  public MyType visit(TypeDeclaration n) {
    return n.f0.accept(this);
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */
  public MyType visit(ClassDeclaration n) {
    mySymbolTable.currentClass = mySymbolTable.classes.get(n.f1.f0.toString());
    MyType ret = n.f4.accept(this);
    mySymbolTable.currentClass = null;
    return ret;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "extends"
   * f3 -> Identifier()
   * f4 -> "{"
   * f5 -> ( VarDeclaration() )*
   * f6 -> ( MethodDeclaration() )*
   * f7 -> "}"
   */
  public MyType visit(ClassExtendsDeclaration n) {
     mySymbolTable.currentClass = mySymbolTable.classes.get(n.f1.f0.toString());
     MyType ret = n.f6.accept(this);
     mySymbolTable.currentClass = null;
     return ret;
  }

  /**
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */
  public MyType visit(MethodDeclaration n) {
    mySymbolTable.currentMethods = mySymbolTable.getMethod(mySymbolTable.currentClass.name,n.f2.f0.toString());
    if (n.f8.accept(this) == null)
      return null;

    MyType expressionType = n.f10.accept(this);
    if (expressionType == null)
      return null;
    MyType ret = null;
    if (mySymbolTable.isSubType(expressionType,mySymbolTable.currentMethods.returnType))
      {
        ret = MyType.OTHER;
      }
    mySymbolTable.currentMethods = null;
    return ret;
  }

  /**
   * f0 -> ArrayType()
   *       | BooleanType()
   *       | IntegerType()
   *       | Identifier()
   */
  public MyType visit(Type n) {
     typeDefineScope = true;
     MyType ret = n.f0.accept(this);
     typeDefineScope = false;
     return ret;
  }

  /**
   * f0 -> "boolean"
   */
  public MyType visit(BooleanType n) {
     return MyType.BOOLEAN;
  }

  /**
   * f0 -> "int"
   */
  public MyType visit(IntegerType n) {
    return MyType.INTEGER;
  }
  /**
   * f0 -> Block()
   *       | AssignmentStatement()
   *       | ArrayAssignmentStatement()
   *       | IfStatement()
   *       | WhileStatement()
   *       | PrintStatement()
   */
  public MyType visit(Statement n) {
    System.out.println("Got to statement visitor for "+n.f0.choice.toString());
    MyType ret = n.f0.accept(this);
    if (ret != null)
      System.out.println("after checking statement return: " + ret.type);
    return ret;
  }

  /**
   * f0 -> "{"
   * f1 -> ( Statement() )*
   * f2 -> "}"
   */
  public MyType visit(Block n) {
     return n.f1.accept(this);
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   * f3 -> ";"
   */
  public MyType visit(AssignmentStatement n) {
    System.out.println("Get to AssignmentStatement");
    MyType idType = n.f0.accept(this);
    if (idType == null)
      return null;
    System.out.println("Finish checking Identifier");
    MyType expressionType = n.f2.accept(this);
    if (expressionType == null)
      return null;
    if (! mySymbolTable.isSubType(expressionType,idType))
      return null;
    return MyType.OTHER;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> Expression()
   * f6 -> ";"
   */
  public MyType visit(ArrayAssignmentStatement n) {
    MyType idType = n.f0.accept(this);
    if (idType != MyType.ARRAY)
      return null;
    MyType indexType = n.f2.accept(this);
    if (indexType != MyType.INTEGER)
      return null;
    MyType expressionType = n.f5.accept(this);
    if (expressionType != MyType.INTEGER)
      return null;
    return MyType.OTHER;

  }

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   * f5 -> "else"
   * f6 -> Statement()
   */
  public MyType visit(IfStatement n) {
    MyType expressionType = n.f2.accept(this);
    if (expressionType != MyType.BOOLEAN)
      return null;
    MyType statement1Type = n.f4.accept(this);
    MyType statement2Type = n.f6.accept(this);
    if ((statement1Type == null) || (statement1Type == null))
      return null;
    return MyType.OTHER;
  }

  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public MyType visit(WhileStatement n) {
    MyType expressionType = n.f2.accept(this);
    if (expressionType != MyType.BOOLEAN)
      return null;
    MyType statementType = n.f4.accept(this);
    if (statementType == null)
      return null;
    return MyType.OTHER;
  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> ";"
   */
  public MyType visit(PrintStatement n) {
    System.out.println("PrintStatement for "+ n.f2.f0.toString());
    MyType expressionType = n.f2.accept(this);
    System.out.println("Finish checking expression");
    if (expressionType != MyType.INTEGER){
      System.out.println("Wrong at Expression");
      return null;
    }

    return MyType.OTHER;
  }

  /**
   * f0 -> AndExpression()
   *       | CompareExpression()
   *       | PlusExpression()
   *       | MinusExpression()
   *       | TimesExpression()
   *       | ArrayLookup()
   *       | ArrayLength()
   *       | MessageSend()
   *       | PrimaryExpression()
   */
  public MyType visit(Expression n) {
     return n.f0.accept(this);
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "&&"
   * f2 -> PrimaryExpression()
   */
  public MyType visit(AndExpression n) {
    MyType pri1Type = n.f0.accept(this);
    if (pri1Type != MyType.BOOLEAN)
      return null;
    MyType pri2Type = n.f2.accept(this);
    if (pri2Type != MyType.BOOLEAN)
      return null;
    return MyType.BOOLEAN;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public MyType visit(CompareExpression n) {
    MyType pri1Type = n.f0.accept(this);
    if (pri1Type != MyType.INTEGER)
      return null;
    MyType pri2Type = n.f2.accept(this);
    if (pri2Type != MyType.INTEGER)
      return null;
    return MyType.BOOLEAN;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public MyType visit(PlusExpression n) {
    MyType pri1Type = n.f0.accept(this);
    if (pri1Type != MyType.INTEGER)
      return null;
    MyType pri2Type = n.f2.accept(this);
    if (pri2Type != MyType.INTEGER)
      return null;
    return MyType.INTEGER;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public MyType visit(MinusExpression n) {
    MyType pri1Type = n.f0.accept(this);
    if (pri1Type != MyType.INTEGER)
      return null;
    MyType pri2Type = n.f2.accept(this);
    if (pri2Type != MyType.INTEGER)
      return null;
    return MyType.INTEGER;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public MyType visit(TimesExpression n) {
    MyType pri1Type = n.f0.accept(this);
    if (pri1Type != MyType.INTEGER)
      return null;
    MyType pri2Type = n.f2.accept(this);
    if (pri2Type != MyType.INTEGER)
      return null;
    return MyType.INTEGER;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public MyType visit(ArrayLookup n) {
    MyType pri1Type = n.f0.accept(this);
    if (pri1Type != MyType.ARRAY)
      return null;
    MyType pri2Type = n.f2.accept(this);
    if (pri2Type != MyType.INTEGER)
      return null;
    return MyType.INTEGER;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> "length"
   */
  public MyType visit(ArrayLength n) {
    MyType pri1Type = n.f0.accept(this);
    if (pri1Type != MyType.ARRAY)
      return null;
    return MyType.INTEGER;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( ExpressionList() )?
   * f5 -> ")"
   */
  public MyType visit(MessageSend n) {
    System.out.println("Got to MessageSend");
    MyType priType = n.f0.accept(this);
    if (priType.type != MyType.Type.ID)
    {
      System.out.println("PrimaryExpression type is " + priType.type);
      return null;
    }

    System.out.println("finish get PrimaryExpression");
    MyMethods method = mySymbolTable.getMethod(priType.name,n.f2.f0.toString());
    if (method == null)
      return null;
    System.out.println("finish get method");
    if (!n.f4.present())
      {
        System.out.println("Number of paramater for " + method.name + " is " + method.getPara_TypeList().size());
        if (method.getPara_TypeList().isEmpty())
          return method.returnType;
        System.out.println("Mismatch number of parameters");
        return null;
      }
    System.out.println("Number of paramater for " + method.name + " is " + method.getPara_TypeList().size());
    System.out.println("Type of parametr is ");
    for (MyType parametertest: method.getPara_TypeList()) { System.out.println(parametertest.type);}
    System.out.println("Start compare parameter list");
    Queue<MyType> paraQueue = new LinkedList<MyType>(method.getPara_TypeList());
    ExpressionList callParaList = (ExpressionList) n.f4.node;

    MyType firstExp = callParaList.f0.accept(this);
    System.out.println("First expresion in MessageSend: "+firstExp.name+" type:" +firstExp.type);
    if (firstExp == null)
      return null;
    System.out.println("First element in paraQueue is: " + paraQueue.peek().name);
    System.out.println("paraQueue is " + paraQueue);

    if (!mySymbolTable.isSubType(firstExp,paraQueue.peek())){
      System.out.println(paraQueue.peek().name + " and " +firstExp.name +" is not sub type");
      return null;
    }
    paraQueue.remove();


    for(Node nextExp: callParaList.f1.nodes) {
      MyType nextExpType = nextExp.accept(this);
      if (nextExpType == null)
        return null;
      if (!mySymbolTable.isSubType(nextExpType,paraQueue.remove()))
        return null;
    }

    if (!paraQueue.isEmpty())
      return null;
    return method.returnType;


  }

  /**
   * f0 -> Expression()
   * f1 -> ( ExpressionRest() )*
   */
  public MyType visit(ExpressionList n) {
     MyType expressionType = n.f0.accept(this);
     if (expressionType == null)
      return null;
     return n.f1.accept(this);
  }

  /**
   * f0 -> ","
   * f1 -> Expression()
   */
  public MyType visit(ExpressionRest n) {
    MyType expressionType = n.f1.accept(this);
    if (expressionType == null)
     return null;
    return expressionType;
  }

  /**
   * f0 -> IntegerLiteral()
   *       | TrueLiteral()
   *       | FalseLiteral()
   *       | Identifier()
   *       | ThisExpression()
   *       | ArrayAllocationExpression()
   *       | AllocationExpression()
   *       | NotExpression()
   *       | BracketExpression()
   */
  public MyType visit(PrimaryExpression n) {
    MyType ret = n.f0.accept(this);
    if (ret == null)
      {
        System.out.println("Wrong at PrimaryExpression");
        return null;
      }
     return ret;
  }

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public MyType visit(IntegerLiteral n) {
    return MyType.INTEGER;
  }

  /**
   * f0 -> "true"
   */
  public MyType visit(TrueLiteral n) {
    return MyType.BOOLEAN;
  }

  /**
   * f0 -> "false"
   */
  public MyType visit(FalseLiteral n) {
    return MyType.BOOLEAN;
  }

  /**
   * f0 -> <IDENTIFIER>
   */
  public MyType visit(Identifier n) {
    String idName = n.f0.toString();
    System.out.println("Start checking Identifier");
    if (typeDefineScope) {
      System.out.println("Start checking Identifier in type difine");
      MyClass classType = mySymbolTable.classes.get(idName);
      if (classType == null)
        return null;
      MyType ret = new MyType(idName,MyType.Type.ID);
      return ret;
    }
      else {
        System.out.println("Start checking Identifier not in type define");
        MyType ret = mySymbolTable.currentMethods.fields.get(idName);
        if (ret != null)
          return ret;
        System.out.println("Not found in "+mySymbolTable.currentMethods.name + " fields");

        ret = mySymbolTable.currentMethods.parameters.get(idName);
        if (ret != null)
          return ret;

        System.out.println("Not found in "+mySymbolTable.currentMethods.name + " paramater");
        ret = mySymbolTable.currentClass.fields.get(idName);
        if (ret != null)
          return ret;

        System.out.println("Not found in "+mySymbolTable.currentClass.name + " fields");

        MyClass parentCheck = mySymbolTable.currentClass.parent;
        while (parentCheck != null)
        {
          ret = parentCheck.fields.get(idName);
          if (ret != null)
            return ret;
          System.out.println("Not found in "+parentCheck.name + " fields");
          parentCheck = parentCheck.parent;
        }
        return null;
      }
  }

  /**
   * f0 -> "this"
   */
  public MyType visit(ThisExpression n) {
     MyType ret = new MyType(mySymbolTable.currentClass.name,MyType.Type.ID);
     return ret;
  }

  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public MyType visit(ArrayAllocationExpression n) {
    MyType expressionType = n.f3.accept(this);
    if (expressionType != MyType.INTEGER)
      return null;
    return MyType.ARRAY;
  }

  /**
   * f0 -> "new"
   * f1 -> Identifier()
   * f2 -> "("
   * f3 -> ")"
   */
  public MyType visit(AllocationExpression n) {
    typeDefineScope = true;
    System.out.println("AllocationExpression for "+ n.f1.f0.toString());
    MyType ret = n.f1.accept(this);
    typeDefineScope = false;
    if (ret.type != MyType.Type.ID)
    {
      return null;
    }

    return ret;
  }

  /**
   * f0 -> "!"
   * f1 -> Expression()
   */
  public MyType visit(NotExpression n) {
    MyType expressionType = n.f1.accept(this);
    if (expressionType != MyType.BOOLEAN)
      return null;
    return MyType.BOOLEAN;
  }

  /**
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   */
  public MyType visit(BracketExpression n) {
    return n.f1.accept(this);
  }
}
