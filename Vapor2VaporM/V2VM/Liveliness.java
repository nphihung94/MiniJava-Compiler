package V2VM;

import cs132.vapor.ast.*;
import java.util.*;

public class Liveliness extends VInstr.Visitor<Throwable> {
  public LinkedHashMap<String,VMVariable> varList = new LinkedHashMap<>();
  public int outNum = 0;

  public void printVarList() {
    System.out.println("Var List: ");
    for (Map.Entry<String,VMVariable> entry: varList.entrySet()) {
      System.out.println("variable name: " + entry.getKey() + " match with Variable: " + entry.getValue());
    }
  }

  public Liveliness(VFunction currentFunction) throws Throwable{
    //System.out.println("Start liveAnalyze at function " + currentFunction.ident);
    for (VVarRef parameter: currentFunction.params) {
      varList.put(parameter.toString(), new VMVariable(parameter.toString(),parameter.sourcePos.line));
    }

    LinkedList<VCodeLabel> labels = new LinkedList<VCodeLabel>(Arrays.asList(currentFunction.labels));

    for (VInstr instruction: currentFunction.body) {
      while (!labels.isEmpty() && labels.peek().sourcePos.line < instruction.sourcePos.line) {
        String labelName = labels.pop().ident;
        for (VMVariable var : varList.values())
          var.beforeLabel.add(labelName);
      }
      instruction.accept(this);
    }
  }

  public boolean varCheck(Node n) {
    if (n == null)
     return false;
    if (n instanceof VMemRef)
    {
      return n instanceof VMemRef.Global;
    }
    else if (n instanceof VOperand)
      {
        return n instanceof VVarRef.Local;
      }
    return false;
  }

  public void writeVar(String varName, int line) {
    VMVariable variable = varList.get(varName);
    if (variable == null)
      {
        varList.put(varName, new VMVariable(varName,line));
      }
    else {
        variable.writeVariable(line);
    }
  }

  public void readVar(String varName, int line) {
    VMVariable variable = varList.get(varName);
    if (variable != null)
      variable.readVariable(line);
  }

  public void labelAnalyze(String labelName, int line) {
    for (VMVariable variable: varList.values()){
      if (variable.afterLabel.contains(labelName)) {
        variable.endLine = line;
        variable.isafterCall = variable.isbeforeCall;
      }
    }
  }

  public String getLabel(VAddr n) {
    return n.toString().replaceFirst(":","");
  }

  public String getLabel(VBranch n) {
    return n.target.getTarget().ident.replaceFirst(":","");
  }

  @Override
  public void visit(VAssign a) throws Throwable {
    int line = a.sourcePos.line;
    VOperand lhs = a.dest;
    VOperand rhs = a.source;
    if (varCheck(rhs)) {
      readVar(rhs.toString(),line);
    }
    writeVar(lhs.toString(),line);
  }

  @Override
  public void visit(VBranch b) throws Throwable{
    int line = b.sourcePos.line;
    String label = getLabel(b);
    labelAnalyze(label,line);
    VOperand variable = b.value;
    readVar(variable.toString(),line);
  }

  @Override
  public void visit(VBuiltIn c) throws Throwable{
    int line = c.sourcePos.line;
    for (VOperand arg: c.args) {
      if (varCheck(arg)) {
        readVar(arg.toString(),line);
      }
    }
    VOperand lhs = c.dest;
    if (varCheck(lhs)) {
      writeVar(lhs.toString(),line);
    }

  }

  @Override
  public void visit(VCall c) throws Throwable{
    int line = c.sourcePos.line;
    for (VOperand arg: c.args) {
      if (varCheck(arg)) {
          readVar(arg.toString(),line);
      }
    }

    VAddr<VFunction> rhs = c.addr;
    readVar(rhs.toString(),line);
    // Only a0...a3 reserved for argument passing
    if (c.args.length > 4) {
      outNum = c.args.length - 4;
    }

    for (VMVariable variable: varList.values())
      variable.isbeforeCall = true;

    VOperand lhs = c.dest;
    if (varCheck(lhs)) {
        writeVar(lhs.toString(),line);
    }
  }

  @Override
  public void visit(VGoto g) throws Throwable{
    int line = g.sourcePos.line;
    String label = getLabel(g.target);
    labelAnalyze(label,line);
  }

  @Override
  public void visit(VMemRead r) throws Throwable{
    int line =  r.sourcePos.line;
    VVarRef lhs = r.dest;
    VMemRef rhs =  r.source;
    if (varCheck(rhs)) {
      readVar(((VMemRef.Global)rhs).base.toString(),line);
    }

    if (varCheck(lhs)) {
      writeVar(lhs.toString(),line);
    }
  }

  @Override
  public void visit(VMemWrite w) throws Throwable{
    int line = w.sourcePos.line;
    VMemRef lhs =  w.dest;
    if (varCheck(lhs)) {
      readVar(((VMemRef.Global)lhs).base.toString(),line);
    }

    VOperand rhs = w.source;
    if (varCheck(rhs)){
      readVar(rhs.toString(),line);
    }
  }

  @Override
  public void visit(VReturn r) throws Throwable{
    int line = r.sourcePos.line;
    if (varCheck(r.value))
      writeVar(r.value.toString(),line);
  }
}
