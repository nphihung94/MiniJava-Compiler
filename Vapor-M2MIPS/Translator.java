import cs132.vapor.ast.*;

import java.util.*;

public class Translator extends VInstr.Visitor<Throwable> {
  public int indentCounter = 0;
  public boolean errorPrint = false;
  public boolean heapPrint = false;
  public boolean printIntPrint = false;
  public boolean nullErr = false;
  public boolean oobErr = false;
  public HashMap<String,String> MipFunction = new HashMap<String,String>();

  public void printM(String outString, Object... args) {
    String indent = "";
    for (int i=0; i<indentCounter; i++) {
      indent = indent + "\t";
    }
    System.out.println(indent + String.format(outString,args));
  }

  public void createFunctionMap() {
    MipFunction.put("HeapAllocZ","_heapAlloc");
    MipFunction.put("Add","addu");
    MipFunction.put("Sub","subu");
    MipFunction.put("MulS","mul");
    MipFunction.put("Lt","sltu");
    MipFunction.put("LtS","slti");
    MipFunction.put("LtSreg","slt");
    MipFunction.put("Eq","subu");
  }

  public void printInstruct(String inst, String outReg, String inReg) {
    printM("%s %s %s",inst,outReg,inReg);
  }

  public void printBuiltin(String funct, String outReg, String arg1, String arg2) {
    printM("%s %s %s %s",funct,outReg,arg1,arg2);
  }

  public void printJump(String jumpType, String label) {
    printM("%s %s",jumpType,label);
  }

  public void startMain(String funcName) {
    indentCounter ++;
    printM("jal %s",funcName);
    printInstruct("li","$v0","10");
    printM("syscall ");
    indentCounter --;
  }

  public void printEndProg() {
    printM(".data");
    printM(".align 0");
    if (printIntPrint) {
      printM("_newline: .asciiz \"\\n\"");
    }
    int i = 0;
    if (nullErr) {
      printM("_str%d: .asciiz \"null pointer\\n\"",i);
      i++;
    }
   if (oobErr) {
      printM("_str%d: .asciiz \"array index out of bounds\\n\"",i);
   }
  }

  public void printInt() {
    printM("_print:");
    indentCounter++;
    printInstruct("li","$v0","1");
    printM("syscall");
    printInstruct("la","$a0","_newline");
    printInstruct("li","$v0","4");
    printM("syscall");
    printJump("jr","$ra");
    indentCounter--;
  }

  public void printError() {
    printM("_error:");
    indentCounter++;
    printInstruct("li","$v0","4");
    printM("syscall");
    printInstruct("li","$v0","10");
    printM("syscall");
    indentCounter --;
  }

  public void printHeap() {
    printM("_heapAlloc:");
    indentCounter ++;
    printInstruct("li","$v0","9");
    printM("syscall");
    printJump("jr","$ra");
    indentCounter --;
  }


  public Translator(VaporProgram inputProgram) throws Throwable  {

    createFunctionMap();

    printM(".data");
    printM("");

    for (VDataSegment data: inputProgram.dataSegments) {
      printM("%s:",data.ident);
      indentCounter ++;
      for (VOperand val: data.values) {
        printM("%s",val.toString().substring(1));
      }
      printM("");
      indentCounter --;
    }

    printM(".text");
    printM("");
    startMain(inputProgram.functions[0].ident);

    for (VFunction curFunc: inputProgram.functions) {
      printM("%s:",curFunc.ident);
      indentCounter ++;
      int offset = 8 + 4 * (curFunc.stack.local + curFunc.stack.out);
      printInstruct("sw","$fp","-8($sp)");
      printInstruct("move","$fp","$sp");
      printBuiltin("subu","$sp","$sp",String.valueOf(offset));
      printInstruct("sw","$ra","-4($fp)");

      LinkedList<VCodeLabel> labelList = new LinkedList<VCodeLabel>(Arrays.asList(curFunc.labels));
      for (VInstr instruction: curFunc.body) {
        while (!labelList.isEmpty() && labelList.peek().sourcePos.line < instruction.sourcePos.line) {
          String label = labelList.pop().ident + ":";
          indentCounter --;
          printM(label);
          indentCounter ++;
        }
        instruction.accept(this);
      }


      printInstruct("lw","$ra","-4($fp)");
      printInstruct("lw","$fp","-8($fp)");
      printBuiltin("addu","$sp","$sp",String.valueOf(offset));
      printJump("jr","$ra");
      printM("");
      indentCounter --;
    }
  if (printIntPrint)  {
    printInt();
  }

  if (errorPrint) {
    printError();
  }

  if (heapPrint) {
    printHeap();
  }

  printEndProg();
  }


  public boolean regCheck(String name) {
    return name.contains("$");
  }

  @Override
public void visit(VAssign a) throws Throwable {
  String rhs = a.source.toString();
  String lhs = a.dest.toString();
  if (regCheck(rhs)) {
    printInstruct("move",lhs,rhs);
  }
  else {
    String func = "";
    if (rhs.contains(":")) {
      func = "la";
    }
    else {
      func = "li";
    }
    printInstruct(func,lhs,rhs.replaceFirst(":",""));
  }
}

@Override
public void visit(VBranch b) throws Throwable{
  String target = b.target.toString().substring(1);
  String condition = b.value.toString();
  if (b.positive) {
    printInstruct("bnez",condition,target);
  }
  else {
    printInstruct("beqz",condition,target);
  }
}

@Override
public void visit(VBuiltIn c) throws Throwable{
  VVarRef dest = c.dest;
  String opName = c.op.name;
  if (dest != null) {
    String arg1 = c.args[0].toString();
    if (opName.contains("HeapAlloc")) {
      heapPrint = true;
      String func = "";
      if (regCheck(arg1)) {
        func = "move";
      }
      else {
        func = "li";
      }
      printInstruct(func,"$a0",arg1);
      printJump("jal",MipFunction.get(opName));
      printInstruct("move",dest.toString(),"$v0");
    }
    else {
      String arg2 = c.args[1].toString();
      if (!regCheck(arg1) && !regCheck(arg2)) {
        int val;
        switch(opName) {
          case "Add":
            val = Integer.parseInt(arg1) + Integer.parseInt(arg2);
            break;
          case "Sub":
            val = Integer.parseInt(arg1) - Integer.parseInt(arg2);
            break;
          case "Muls":
            val = Integer.parseInt(arg1) * Integer.parseInt(arg2);
            break;
          default: val = 0;
        }
        printInstruct("li",dest.toString(),String.valueOf(val));
      }
      else if (!regCheck(arg1)) {
        printInstruct("li","$t9",arg1);
        printBuiltin(MipFunction.get(opName),dest.toString(),"$t9",arg2);
      }
      else {
        if (opName.contains("Lts") && regCheck(arg2)) {
          opName = "LtSreg";
        }
        printBuiltin(MipFunction.get(opName),dest.toString(),arg1,arg2);
      }
    }

  }
  else {
    String argu = c.args[0].toString();
    if (opName.contains("PrintIntS")) {
      printIntPrint = true;
      String func = "";
      if (regCheck(argu)) {
        func = "move";
      }
      else {
        func = "li";
      }
      printInstruct(func,"$a0",argu);
      printJump("jal","_print");
    }
    else {
      nullErr = nullErr || argu.contains("null");
      oobErr = oobErr || argu.contains("out of bounds");
      errorPrint = true;
      int type = 0;
      if (nullErr && argu.contains("out of bounds")) {
        type = 1;
      }
      printInstruct("la","$a0","_str" + type);
      printJump("j","_error");
    }
  }
}

@Override
public void visit(VCall c) throws Throwable{
  String address = c.addr.toString();
  String jumpType = "";
  if (regCheck(address)) {
    jumpType = "jalr";
  }
  else {
    jumpType = "jal";
  }
  printJump(jumpType,address.replaceFirst(":",""));
}

@Override
public void visit(VGoto g) throws Throwable{
  printJump("j",g.target.toString().substring(1));
}

@Override
public void visit(VMemRead r) throws Throwable{
  VVarRef dest = r.dest;
  VMemRef source = r.source;
  if (source instanceof VMemRef.Stack) {
    VMemRef.Stack stack = (VMemRef.Stack) source;
    int index = 4 * stack.index;
    String sourceReg = "($sp)";
    if (stack.region.toString().contains("In")) {
      sourceReg = "($fp)";
    }
    sourceReg = String.valueOf(index) + sourceReg;
    printInstruct("lw",dest.toString(),sourceReg);
  }
  else {
    VMemRef.Global globalVal = (VMemRef.Global) source;
    int byteOffset = globalVal.byteOffset;
    String reg = globalVal.base.toString();
    String accessVal = byteOffset + "("+ reg + ")";
    printInstruct("lw",dest.toString(),accessVal);
  }
}

@Override
public void visit(VMemWrite w) throws Throwable{
  VMemRef dest = w.dest;
  VOperand source = w.source;
  if (dest instanceof VMemRef.Stack) {
    VMemRef.Stack stack = (VMemRef.Stack) dest;
    int index = 4 * stack.index;
    String accessStack = index + "($sp)";
    if (regCheck(source.toString())) {
      printInstruct("sw",source.toString(),accessStack);
    }
    else {
      printInstruct("li","$t9",source.toString());
      printInstruct("sw","$t9",accessStack);
    }
  }
  else {
    VMemRef.Global globalVal = (VMemRef.Global) dest;
    int byteOffset = globalVal.byteOffset;
    String reg = globalVal.base.toString();
    String accessVal = byteOffset +"(" + reg + ")";
    if (!source.toString().contains(":")) {
      String sourceString = source.toString();
      if (!regCheck(source.toString())) {
        printInstruct("li","$t9",sourceString);
        sourceString = "$t9";
      }
      printInstruct("sw",sourceString,accessVal);
    }
    else {
      printInstruct("la","$t9",source.toString());
      printInstruct("sw","$t9",accessVal);
    }
  }
}
@Override
public void visit(VReturn r) throws Throwable{
  printM("");
}

}
