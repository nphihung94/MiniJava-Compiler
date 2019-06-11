package V2VM;

import cs132.vapor.ast.*;

import java.util.*;

public class Translator extends VInstr.Visitor<Throwable> {
  public int indentCounter = 0;
  public LinearScan scanner;

  public void printVaporM(String outString, Object... args) {
    String indent = "";
    for (int i=0; i<indentCounter; i++) {
      indent = indent + "\t";
    }
    System.out.println(indent + String.format(outString,args));
  }

  public String getRegister(String variable) {
    return scanner.finalMap.get(variable);
  }

  public Translator(VaporProgram inputProgram) throws Throwable {
    // Print data Segment
    for (VDataSegment data: inputProgram.dataSegments) {
      if (data.mutable) {
        printVaporM("%s %s","var",data.ident);
      }
      else {
        printVaporM("%s %s","const",data.ident);
      }

      indentCounter++;
      for (VOperand.Static val: data.values) {
        printVaporM(val.toString());
      }
      indentCounter--;
    }

    for (VFunction curFunction: inputProgram.functions) {
      scanner = new LinearScan(curFunction);
      scanner.scan();
      //scanner.printFinalMap();

      int inNum = 0;
      if (curFunction.params.length > 4){
        inNum = curFunction.params.length - 4;
      }

      printVaporM("func %s [in %d,out %d,local %d]",curFunction.ident,inNum,scanner.outNum,scanner.localNum);
      indentCounter++;
      for(int i = 0; i < scanner.localNum && i < 8; i++) {
        printVaporM("local[%d] = $s%d",i,i);
      }

      for (int i = 0; i < curFunction.params.length; i++) {
        String register = getRegister(curFunction.params[i].toString());
        if (i < 4) {
          printVaporM("%s = $a%d",register,i);
        }
        else {
          printVaporM("%s = in[%d]",register,i-4);
        }
      }

      LinkedList<VCodeLabel> labelList = new LinkedList<VCodeLabel>(Arrays.asList(curFunction.labels));
      for (VInstr currentInst: curFunction.body) {
        while (!labelList.isEmpty() && (labelList.peek().sourcePos.line < currentInst.sourcePos.line)) {
          String label = labelList.pop().ident;
          indentCounter--;
          printVaporM("%s:",label);
          indentCounter++;
        }
        currentInst.accept(this);
      }
      printVaporM("");
      indentCounter --;
    }
  }

  @Override
  public void visit(VAssign a) throws Throwable{
    String lhs = a.dest.toString();
    String rhs = a.source.toString();
    String rhsVal;
    if (getRegister(rhs) == null) {
      rhsVal = rhs;
    }
    else {
      rhsVal = getRegister(rhs);
    }
    printVaporM("%s = %s",getRegister(lhs),rhsVal);
  }

  @Override
  public void visit(VBranch b) throws Throwable{
    String condition = getRegister(b.value.toString());
    String ifStatement;
    if (b.positive) {
      ifStatement = "if";
    }
    else {
      ifStatement = "if0";
    }
    printVaporM("%s %s goto %s",ifStatement,condition,b.target.toString());
  }

  @Override
  public void visit(VBuiltIn c) throws Throwable{
    String argList = "";
    int argListSize = c.args.length;
    for (int i = 0; i< argListSize; i++) {
      String curArg = c.args[i].toString();
      String arg = "";
      if (getRegister(curArg) == null) {
        arg = curArg;
      }
      else {
        arg  = getRegister(curArg);
      }
      if (i == (argListSize - 1))
        {
          argList = argList + arg;
        }
        else {
          argList = argList + arg + " ";
        }
    }
    VVarRef lhs = c.dest;
    String lhsVal = "";
    if (lhs != null) {
      lhsVal = getRegister(lhs.toString()) + " = ";
    }
    printVaporM("%s%s(%s)",lhsVal,c.op.name,argList);
  }

  @Override
  public void visit(VCall c) throws Throwable{
    for (int i = 0 ; i < c.args.length; i++) {
      String arg = c.args[i].toString();
      String rhs = "";
      if (getRegister(arg) == null) {
        rhs = arg;
      }
      else {
        rhs = getRegister(arg);
      }

      if (i<4) {
        printVaporM("$a%d = %s",i,rhs);
      }
      else {
        printVaporM("out[%d] = %s",i-4,rhs);
      }
    }
    String lhs = getRegister(c.dest.toString());
    String address = c.addr.toString();
    String funcName = "";
    if (getRegister(address) == null) {
      funcName = address;
    }
    else {
      funcName = getRegister(address);
    }
    printVaporM("call %s",funcName);
    printVaporM("%s = $v0",lhs);
  }

  @Override
  public void visit(VGoto g) throws Throwable{
    printVaporM("goto %s",g.target.toString());
  }

  @Override
  public void visit(VMemRead r) {
    String lhs = getRegister(r.dest.toString());
    VMemRef.Global source = (VMemRef.Global) r.source;
    String sourceString = source.base.toString();
    String rhs = "";
    if (getRegister(sourceString) == null) {
      rhs = sourceString;
    }
    else {
      rhs = getRegister(sourceString);
    }
    String offset = "";
    if (source.byteOffset != 0) {
      offset = "+" + String.valueOf(source.byteOffset);
    }
    printVaporM("%s = [%s%s]",lhs,rhs,offset);

  }

  @Override
  public void visit(VMemWrite w) throws Throwable{
    VMemRef.Global dest = (VMemRef.Global) w.dest;
    String lhs = getRegister(dest.base.toString());
    String sourceString = w.source.toString();
    String rhs = "";
    if (getRegister(sourceString) == null) {
      rhs = sourceString;
    }
    else {
      rhs = getRegister(sourceString);
    }
    String offset = "";
    if (dest.byteOffset != 0) {
      offset = "+" +  String.valueOf(dest.byteOffset);
    }
    printVaporM("[%s%s] = %s",lhs,offset,rhs);

  }

  @Override
  public void visit(VReturn r) throws Throwable{
    if (r.value != null) {
      String returnVal = r.value.toString();
      String Val = "";
      if (getRegister(returnVal) == null)  {
        Val = returnVal;
      }
      else {
        Val = getRegister(returnVal);
      }
      printVaporM("$v0 = %s",Val);
    }

    for (int i = 0; i < scanner.localNum && i < 8; i ++) {
      printVaporM("$s%d = local[%d]",i,i);
    }

    printVaporM("ret");
  }
}
