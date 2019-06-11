package src;
import syntaxtree.*;
import visitor.*;
import java.util.*;

public class Translator extends GJNoArguDepthFirst<LinkedList<String>> {

  private VirtualTable VTable;

  // Value to generate label
  private int varCounter = 0;
  private int indentNum = 0;
  private int nullNum = 1;
  private int outofboundNum = 0;
  private int ifNum = 1;
  private int whileNum = 1;
  private int elseNum = 1;

  private boolean memoryAlloc = false;
  private Stack<String> classVisit = new Stack<String>();
  private String currentMethod;
  private LinkedList<String> VaporTranslation = new LinkedList<String>();
  private LinkedList<String> args = new LinkedList<String>();

  public Translator(VirtualTable table) {
    this.VTable = table;
  }

  private String lastVarUsed(LinkedList<String> expr) {
    String var = expr.getLast().split("=")[0];
    var = var.trim();
    var = var.replaceAll("[\\[\\]]","");
    return var;
  }

  private boolean singleCheck(LinkedList<String> expr) {
    boolean result = false;
    if (expr.size() == 1) {
      if (expr.getLast().length() == 1)
        result = true;
      if ((expr.getLast().split("=").length == 1) && !(expr.getLast().contains("while")) && !(expr.getLast().contains("print")))
        result = true;
      }
    return result;
  }

  private String getIDName(LinkedList<String> expr) {
    if (!singleCheck(expr)) {
      return expr.get(0);
    }
    else {
      return expr.getLast();
    }
  }

  private void removeIDName(LinkedList<String> expr) {
    if (expr.size() > 0) {
      if ((expr.getFirst().split(" ").length == 1) && !(expr.getLast().contains("while")) && !(expr.getLast().contains("print")))
        expr.removeFirst();
    }
  }

  private boolean classTypeCheck(String type) {
    if (type == null)
      return false;
    return !type.equals("int[]") && !type.equals("int") && !type.equals("bool");
  }

  private String getVar(LinkedList<String> getFrom, LinkedList<String> addTo) {
    if (singleCheck(getFrom)) {
      return getFrom.getLast();
    }


    removeIDName(getFrom);
    addTo.addAll(getFrom);
    return lastVarUsed(addTo);
  }

  private String addIdent() {
    String ident = "";
    for (int i = 0; i < indentNum; i++)
      ident = ident + "\t";
    return ident;
  }

  private String newVar() {
    return "t." + varCounter++;
  }

  private void resetVarCount() {
    varCounter = 0;
  }

  private String errorMessage(boolean NullPointer) {
    if (NullPointer)
      return addIdent() + "Error(\"null pointer\")";
    return addIdent() + "Error(\"array index out of bounds\")";
  }

  private LinkedList<String> outofBoundCheck(String varName) {
    LinkedList<String> outofBound = new LinkedList<String>();
    int currentOutofBoundLabel = outofboundNum++;
    outofBound.add(addIdent() + "if " + varName +" goto :outBound_" + currentOutofBoundLabel );
    indentNum ++;
    outofBound.add(errorMessage(false));
    indentNum --;
    outofBound.add(addIdent() + "outBound_" + currentOutofBoundLabel + ":");
    return outofBound;
  }

  private LinkedList<String> nullPointerCheck(String varName) {
    LinkedList<String> NullPtr = new  LinkedList<String>();
    int currentNullLabel = nullNum++;
    NullPtr.add(addIdent() + "if " + varName +" goto :null" + currentNullLabel);
    indentNum ++;
    NullPtr.add(errorMessage(true));
    indentNum --;
    NullPtr.add(addIdent() + "null" + currentNullLabel + ":");
    return NullPtr;
  }

  private boolean localVarCheck(String varName) {
    String currentClass = classVisit.get(0);
    VirtualMethod currentMet = VTable.classList.get(currentClass).getMethod(currentMethod);
    return (currentMet.paraName.indexOf(varName) != -1) || (currentMet.localName.indexOf(varName) != -1);
  }

  private LinkedList<String> arrayExpr(String arrName, String assignTo) {
    LinkedList<String> arrayDefer = new LinkedList<String>();
    VirtualClass currentClass = VTable.classList.get(classVisit.get(0));
    if (!localVarCheck(arrName)) {
      if (currentClass.getMemberList().indexOf(arrName) == -1) {
        arrayDefer.add(addIdent() + assignTo + " = "  + arrName);
      }
      else {
        int Index = currentClass.getMemberList().indexOf(arrName);
        int offset = 4 + Index*4;
        arrayDefer.add(addIdent() + assignTo + " = [this + " + offset + "]" );
      }
    }
    else {
        arrayDefer.add(addIdent() + assignTo + " = "  + arrName);
    }
    return arrayDefer;
  }

  private LinkedList<String> arrayAccess(String arrName, String elementIndex) {
    LinkedList<String> arrayAcc = new LinkedList<String>();
    String var1 = newVar();
    arrayAcc.addAll(arrayExpr(arrName,var1));
    arrayAcc.addAll(nullPointerCheck(var1));
    String var2 = newVar();
    arrayAcc.add(addIdent() + var2 + " = [" + var1 + "]");
    arrayAcc.add(addIdent() + var2 + " = Lt(" + elementIndex + " "+ var2 +")");
    arrayAcc.addAll(outofBoundCheck(var2));
    arrayAcc.add(addIdent() + var2 + " = MulS(" + elementIndex + " 4)");
    arrayAcc.add(addIdent() + var2 + " = Add(" + var2 + " " + var1 + ")");
    return arrayAcc;
  }

  private LinkedList<String> printArrayAlloc() {
    LinkedList<String> arrayAlloc = new LinkedList<String>();
    arrayAlloc.add("func ArrayAlloc(size)");
    indentNum ++;
    arrayAlloc.add(addIdent() + "bytes = Add(size 1)");
    arrayAlloc.add(addIdent() + "bytes = MulS(bytes 4)");
    arrayAlloc.add(addIdent() + "address = HeapAllocZ(bytes)");
    arrayAlloc.add(addIdent() + "if address goto :arraynull");
    indentNum ++;
    arrayAlloc.add(errorMessage(true));
    indentNum --;
    arrayAlloc.add(addIdent() + "arraynull:");
    arrayAlloc.add(addIdent() + "[address] = size");
    arrayAlloc.add(addIdent() + "ret address");
    indentNum --;
    return arrayAlloc;
  }



  //
  // User-generated visitor methods below
  //

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public LinkedList<String> visit(Goal n) {
     VaporTranslation.addAll(n.f0.accept(this));
     for (Node type: n.f1.nodes) {
       VaporTranslation.addAll(type.accept(this));
     }
     if (memoryAlloc)
      VaporTranslation.addAll(printArrayAlloc());
    return VaporTranslation;
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
  public LinkedList<String> visit(MainClass n) {
    LinkedList<String> mainclass = new LinkedList<String>();
    for (VirtualClass clas: VTable.classList.values()) {
      if (!clas.name.equals(n.f1.f0.toString())) {
        mainclass.add(addIdent() + "const vmt_" + clas.name);
        indentNum ++;
        for (VirtualMethod meth: clas.getMethodList()) {
          String metName = meth.name;
          mainclass.add(addIdent() + ":" + clas.name + "." + metName);
        }
        indentNum --;
      }
      else {
        mainclass.add(addIdent() + "const vmt_" + clas.name);
      }
    }
    classVisit.push(getIDName(n.f1.accept(this)));
    currentMethod = "main";
    mainclass.add("func Main()");
    indentNum ++;
    for (Node statement: n.f15.nodes) {
      mainclass.addAll(statement.accept(this));
    }
    mainclass.add(addIdent() + "ret");
    indentNum --;

    return mainclass;

  }

  /**
   * f0 -> ClassDeclaration()
   *       | ClassExtendsDeclaration()
   */
  public LinkedList<String> visit(TypeDeclaration n) {
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
  public LinkedList<String> visit(ClassDeclaration n) {
     LinkedList<String> classDeclare = new LinkedList<String>();
     classVisit.clear();
     classVisit.push(getIDName(n.f1.accept(this)));
     for (Node method: n.f4.nodes) {
       classDeclare.addAll(method.accept(this));
     }
     return classDeclare;
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
  public LinkedList<String> visit(ClassExtendsDeclaration n) {
    LinkedList<String> classExtend = new LinkedList<String>();
    String className = getIDName(n.f1.accept(this));
    classVisit.clear();
    classVisit.push(className);
    for (Node method: n.f6.nodes) {
      classExtend.addAll(method.accept(this));
    }
    VTable.currentClassScope = null;
    return classExtend;
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
  public LinkedList<String> visit(MethodDeclaration n) {
     resetVarCount();
     LinkedList<String> methodDeclare = new LinkedList<String>();
     currentMethod = getIDName(n.f2.accept(this));
     String methodName = currentMethod;
     String className = classVisit.get(0);
     String funcDeclare = "func " + className + "." + methodName + "(this";
     VirtualMethod currentMethod = VTable.classList.get(className).getMethod(methodName);
     int paraNum = currentMethod.paraName.size();
     for (int i = 0 ; i < paraNum ; i++)
      funcDeclare = funcDeclare + " " + currentMethod.paraName.get(i);
     funcDeclare = funcDeclare + ")";
     methodDeclare.add(funcDeclare);
     indentNum ++ ;
     for (Node statement: n.f8.nodes) {
        methodDeclare.addAll(statement.accept(this));
     }
     LinkedList<String> retExpr = n.f10.accept(this);
     String retval = getVar(retExpr,methodDeclare);
     methodDeclare.add(addIdent() + "ret " + retval);
     indentNum --;
     return methodDeclare;

  }

  /**
   * f0 -> Block()
   *       | AssignmentStatement()
   *       | ArrayAssignmentStatement()
   *       | IfStatement()
   *       | WhileStatement()
   *       | PrintStatement()
   */
  public LinkedList<String> visit(Statement n) {
     //System.out.println("Get in to statement");
     LinkedList<String> statement = new LinkedList<String>();
     statement = n.f0.accept(this);
     //System.out.println("Finish statement visit");
     //System.out.println(statement == null);
     return statement;
  }

  /**
   * f0 -> "{"
   * f1 -> ( Statement() )*
   * f2 -> "}"
   */
  public LinkedList<String> visit(Block n) {
    LinkedList<String> block = new LinkedList<String>();
    indentNum ++;
    for (Node statement: n.f1.nodes) {
      block.addAll(statement.accept(this));
    }
    indentNum --;
    return block;

  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   * f3 -> ";"
   */
  public LinkedList<String> visit(AssignmentStatement n) {
    LinkedList<String> Assignment = new LinkedList<String>();
    String ID = getIDName(n.f0.accept(this));

    LinkedList<String> expr = n.f2.accept(this);
    String var = getVar(expr,Assignment);
    if (!localVarCheck(ID)) {
      //System.out.println("get in to offset of assignment ");
      VirtualClass currentClass = VTable.classList.get(classVisit.get(0));

      //System.out.println("ID is " + ID);
      int index = currentClass.getMemberList().indexOf(ID);
      int offset = 4 + 4 * index;
      ID = "[this+" + offset + "]";
    }
    Assignment.add(addIdent() + ID + " = " + var);
    return Assignment;

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
  public LinkedList<String> visit(ArrayAssignmentStatement n) {
    LinkedList<String> arrayAssignment = new LinkedList<String>();
    String ID = getIDName(n.f0.accept(this));
    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f2.accept(this);

    String arrayIndex = getVar(expr1,arrayAssignment);
    arrayAssignment.addAll(arrayAccess(ID,arrayIndex));
    int returnVar = varCounter;
    LinkedList<String> expr2 = new LinkedList<String>();
    expr2 = n.f5.accept(this);
    String expr2Val = getVar(expr2,arrayAssignment);
    arrayAssignment.add(addIdent() + "[t." + (returnVar - 1) + " + 4] = " + expr2Val);
    return arrayAssignment;
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
  public LinkedList<String> visit(IfStatement n) {
    LinkedList<String> ifthenelse = new LinkedList<String>();
    int currentIf = ifNum ++;

    LinkedList<String> expr = new LinkedList<String>();
    expr = n.f2.accept(this);
    String condition = getVar(expr,ifthenelse);

    ifthenelse.add(addIdent() + "if0 " + condition + " goto" + " :if" + currentIf +"_else");
    LinkedList<String> ifStatement = new LinkedList<String>();
    ifStatement = n.f4.accept(this);
    indentNum ++;

    if (!singleCheck(ifStatement)) {
      removeIDName(ifStatement);
      ifthenelse.addAll(ifStatement);
    }

    ifthenelse.add(addIdent() + "goto :if" + currentIf +"_end");
    indentNum --;
    ifthenelse.add(addIdent() + "if" + currentIf + "_else:");
    LinkedList<String> elseStatement = new LinkedList<String>();
    elseStatement = n.f6.accept(this);
    indentNum ++;
    if (!singleCheck(elseStatement)) {
      removeIDName(elseStatement);
      ifthenelse.addAll(elseStatement);
    }
    indentNum --;
    ifthenelse.add(addIdent() + "if" + currentIf + "_end:");
    return ifthenelse;
  }

  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public LinkedList<String> visit(WhileStatement n) {
     int currentWhile = whileNum ++;
     LinkedList<String> whiledo = new LinkedList<String>();

     whiledo.add(addIdent() + "while" + currentWhile + ":");


     LinkedList<String> expr = new LinkedList<String>();

     expr = n.f2.accept(this);
     indentNum ++;
     String condition = getVar(expr,whiledo);
     indentNum --;

     whiledo.add(addIdent() + "if0 " + condition + " goto :while" + currentWhile+"_end");
     LinkedList<String> whilestatement = new LinkedList<String>();
     whilestatement = n.f4.accept(this);
     if (!singleCheck(whilestatement)) {
       removeIDName(whilestatement);
       whiledo.addAll(whilestatement);
     }
     indentNum ++;
     whiledo.add(addIdent() + "goto :while" + currentWhile);
     indentNum --;
     whiledo.add(addIdent() + "while" + currentWhile +"_end:");
     return whiledo;


  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> ";"
   */
  public LinkedList<String> visit(PrintStatement n) {
     LinkedList<String> printstate = new LinkedList<String>();
     LinkedList<String> expr = new LinkedList<String>();
     expr = n.f2.accept(this);
     String printVal = getVar(expr,printstate);
     printstate.add(addIdent() + "PrintIntS(" + printVal + ")");
     //System.out.println(printstate != null);
     //System.out.println("Finish with System.out.println");

     return printstate;
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
  public LinkedList<String> visit(Expression n) {
     return n.f0.accept(this);
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "&&"
   * f2 -> PrimaryExpression()
   */
  public LinkedList<String> visit(AndExpression n) {
    LinkedList<String> andExpr = new LinkedList<String>();
    int currentElse = elseNum ++;

    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f0.accept(this);
    String expr1Val = getVar(expr1,andExpr);
    andExpr.add(addIdent() + "if0 " + expr1Val +" goto :else" + currentElse);
    indentNum ++;

    LinkedList<String> expr2 = new LinkedList<String>();
    expr2 = n.f2.accept(this);
    String expr2Val = getVar(expr2,andExpr);


    int currentVarCount = varCounter;
    andExpr.add(addIdent() + newVar() + " = MulS(" + expr1Val + " " + expr2Val + ")");
    andExpr.add(addIdent() + "goto :else" + currentElse + "_end");
    indentNum --;
    andExpr.add(addIdent() + "else" + currentElse + ":");
    indentNum ++;
    andExpr.add(addIdent() + "t." + currentVarCount + " =0");
    indentNum --;
    andExpr.add(addIdent() + "else" + currentElse + "_end:");
    andExpr.add(addIdent() + newVar() + " = Eq(1 t." + currentVarCount + ")");
    return andExpr;

  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public LinkedList<String> visit(CompareExpression n) {
    LinkedList<String> compareExpr = new LinkedList<String>();
    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f0.accept(this);
    String expr1Val = getVar(expr1,compareExpr);
    LinkedList<String> expr2 = new LinkedList<String>();
    expr2 = n.f2.accept(this);
    String expr2Val = getVar(expr2,compareExpr);
    compareExpr.add(addIdent() + newVar() + " = " + "LtS(" + expr1Val + " " + expr2Val + ")");
    return compareExpr;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public LinkedList<String> visit(PlusExpression n) {
    LinkedList<String> plusExpr = new LinkedList<String>();
    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f0.accept(this);
    String expr1Val = getVar(expr1,plusExpr);
    LinkedList<String> expr2 = new LinkedList<String>();
    expr2 = n.f2.accept(this);
    String expr2Val = getVar(expr2,plusExpr);
    plusExpr.add(addIdent() + newVar() + " = " + "Add(" + expr1Val + " " + expr2Val + ")");
    return plusExpr;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public LinkedList<String> visit(MinusExpression n) {
    LinkedList<String> minusExpr = new LinkedList<String>();
    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f0.accept(this);
    String expr1Val = getVar(expr1,minusExpr);
    LinkedList<String> expr2 = new LinkedList<String>();
    expr2 = n.f2.accept(this);
    String expr2Val = getVar(expr2,minusExpr);
    minusExpr.add(addIdent() + newVar() + " = " + "Sub(" + expr1Val + " " + expr2Val + ")");
    return minusExpr;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public LinkedList<String> visit(TimesExpression n) {
    LinkedList<String> multExpr = new LinkedList<String>();
    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f0.accept(this);
    String expr1Val = getVar(expr1,multExpr);
    LinkedList<String> expr2 = new LinkedList<String>();
    expr2 = n.f2.accept(this);
    String expr2Val = getVar(expr2,multExpr);
    multExpr.add(addIdent() + newVar() + " = " + "MulS(" + expr1Val + " " + expr2Val + ")");
    return multExpr;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public LinkedList<String> visit(ArrayLookup n) {
    LinkedList<String> arrayLookup = new LinkedList<String>();
    LinkedList<String> expr2 = new LinkedList<String>();
    expr2 = n.f2.accept(this);
    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f0.accept(this);
    String arrayIndex = getVar(expr2,arrayLookup);
    String ID;
    if (!singleCheck(expr1)) {
      if (expr1.getFirst().split(" ").length == 1) {
        varCounter--;
        expr1.removeFirst();
      }
      arrayLookup.addAll(expr1);
      ID = lastVarUsed(expr1);
    }
    else {
      ID = expr1.getLast();
    }
    arrayLookup.addAll(arrayAccess(ID,arrayIndex));
    int currentVarCount = varCounter;
    arrayLookup.add(addIdent() + newVar() + " = [t." + (currentVarCount - 1) + " + 4]");
    return arrayLookup;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> "length"
   */
  public LinkedList<String> visit(ArrayLength n) {
     LinkedList<String> arrayLength = new LinkedList<String>();
     LinkedList<String> expr = new LinkedList<String>();
     expr = n.f0.accept(this);
     String arrayName = getVar(expr,arrayLength);
     String var = newVar();
     arrayLength.addAll(arrayExpr(arrayName,var));
     arrayLength.addAll(nullPointerCheck(arrayName));
     int currentVarCount = varCounter;
     arrayLength.add(addIdent() + newVar() + " = [t." + (currentVarCount - 1) + "]");
     return arrayLength;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( ExpressionList() )?
   * f5 -> ")"
   */
  public LinkedList<String> visit(MessageSend n) {
    LinkedList<String> messageSend  = new LinkedList<String>();
    LinkedList<String> expr1 = new LinkedList<String>();
    expr1 = n.f0.accept(this);
    //System.out.println("Get in to MessageSend");
    //System.out.println(expr1 == null);
    String classPtr = lastVarUsed(expr1);
    if (!singleCheck(expr1)) {
      messageSend.addAll(expr1);
    }

    if (!classPtr.equals("this")) {
      messageSend.addAll(nullPointerCheck(classPtr));
    }
    String methodName = getIDName(n.f2.accept(this));
    String className = classVisit.peek();
    VirtualMethod curMethod = VTable.classList.get(className).getMethod(methodName);
    if (classTypeCheck(curMethod.retType)) {
      classVisit.push(curMethod.retType);
    }

    int methodIndex =  4 * VTable.classList.get(className).getMethodList().indexOf(curMethod);
    int currentVarCount = varCounter;

    messageSend.add(addIdent() + newVar() + " = [" + classPtr + "]");
    messageSend.add(addIdent() + newVar() + " = [t." + currentVarCount + "+" + methodIndex + "]");
    String methodPtr = "t." + ++currentVarCount;
    String methodCall = "call " + methodPtr + "(" + classPtr;
    args.clear();
    LinkedList<String> exprList = new LinkedList<String>();
    exprList = n.f4.accept(this);
    if (exprList != null)
      messageSend.addAll(exprList);

    int argsSize = args.size();
    for (int i = 0; i < argsSize; i++) {
      methodCall = methodCall + " " + args.get(i);
    }

    methodCall = methodCall + ")";
    messageSend.add(addIdent() + newVar() + " = " + methodCall);
    return messageSend;
  }

  /**
   * f0 -> Expression()
   * f1 -> ( ExpressionRest() )*
   */
  public LinkedList<String> visit(ExpressionList n) {
    LinkedList<String> expr = new LinkedList<String>();
    expr = n.f0.accept(this);
    LinkedList<String> exprList = new LinkedList<String>();
    LinkedList<String> newArg = new LinkedList<String>(args);
    newArg.add(getVar(expr,exprList));
    for (Node nextExpr: n.f1.nodes) {
      LinkedList<String> newExpr = new LinkedList<String>();
      newExpr = nextExpr.accept(this);
      newArg.add(getVar(newExpr,exprList));
    }
    args = newArg;
    return exprList;
  }

  /**
   * f0 -> ","
   * f1 -> Expression()
   */
  public LinkedList<String> visit(ExpressionRest n) {
     return n.f1.accept(this);
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
  public LinkedList<String> visit(PrimaryExpression n) {
     //System.out.println("Get into PrimaryExpression");
     LinkedList<String> priExpr = new LinkedList<String>();
     priExpr = n.f0.accept(this);
     //System.out.println(priExpr == null);
     return priExpr;
  }

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public LinkedList<String> visit(IntegerLiteral n) {
     LinkedList<String> integerLiteral =  new LinkedList<String>(Arrays.asList(n.f0.toString()));
     //System.out.println("Get in to ingerger literal");
     //System.out.println(integerLiteral == null);
     return integerLiteral;
  }

  /**
   * f0 -> "true"
   */
  public LinkedList<String> visit(TrueLiteral n) {
     return new LinkedList<String>(Arrays.asList("1"));
  }

  /**
   * f0 -> "false"
   */
  public LinkedList<String> visit(FalseLiteral n) {
     return new LinkedList<String>(Arrays.asList("0"));
  }

  /**
   * f0 -> <IDENTIFIER>
   */
  public LinkedList<String> visit(Identifier n) {
    LinkedList<String> id = new LinkedList<String>();
    String name = n.f0.toString();
    id.add(name);
    if (!classVisit.isEmpty()) {
      String currentClass = classVisit.get(0);
      VirtualMethod curMethod = VTable.classList.get(currentClass).getMethod(currentMethod);
      if (curMethod != null) {
        if (curMethod.paraName.indexOf(name) != -1) {
          if (classTypeCheck(curMethod.paraTypeList.get(name)))
            classVisit.push(curMethod.paraTypeList.get(name));
        }
        else
        if (curMethod.localName.indexOf(name) != -1) {
          if (classTypeCheck(curMethod.localTypeList.get(name)))
            classVisit.push(curMethod.localTypeList.get(name));
        }
        else {
          if (classTypeCheck(VTable.classList.get(currentClass).classMemberType.get(name))) {
            classVisit.push(VTable.classList.get(currentClass).classMemberType.get(name));
          }
          if (VTable.classList.get(currentClass).getMemberList().indexOf(name) != -1) {
            int index = VTable.classList.get(currentClass).getMemberList().indexOf(name);
            int offset = index * 4 + 4;
            id.add(addIdent() + newVar() + " = [this+" + offset + "]");
          }
        }


      }
    }
    return id;
  }

  /**
   * f0 -> "this"
   */
  public LinkedList<String> visit(ThisExpression n) {
     classVisit.push(classVisit.get(0));
     return new LinkedList<String>(Arrays.asList("this"));
  }

  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public LinkedList<String> visit(ArrayAllocationExpression n) {
     memoryAlloc = true;
     LinkedList<String> arrayAllocExpr  = new LinkedList<String>();
     LinkedList<String> expr = new LinkedList<String>();
     expr = n.f3.accept(this);
     String allocVar = getVar(expr,arrayAllocExpr);
     arrayAllocExpr.add(addIdent() + newVar() + " = call :ArrayAlloc(" + allocVar + ")");
     return arrayAllocExpr;
  }

  /**
   * f0 -> "new"
   * f1 -> Identifier()
   * f2 -> "("
   * f3 -> ")"
   */
  public LinkedList<String> visit(AllocationExpression n) {
     String className = n.f1.accept(this).getLast();
     classVisit.push(className);
     VirtualClass currClass = VTable.classList.get(className);
     LinkedList<String> newObject = new LinkedList<String>();
     String createObject = newVar();
     newObject.add(addIdent() + createObject + " = HeapAllocZ(" + currClass.size() +")");
     newObject.add(addIdent() + "[" + createObject + "] = :vmt_" + className);
     return newObject;
  }

  /**
   * f0 -> "!"
   * f1 -> Expression()
   */
  public LinkedList<String> visit(NotExpression n) {
     LinkedList<String> notExpr = new LinkedList<String>();
     LinkedList<String> expr = new LinkedList<String>();
     expr = n.f1.accept(this);
     String exprVar = getVar(expr,notExpr);
     notExpr.add(addIdent() + newVar() + " = Sub(1 " + exprVar +")");
     return notExpr;
  }

  /**
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   */
  public LinkedList<String> visit(BracketExpression n) {
     return n.f1.accept(this);
  }

}
