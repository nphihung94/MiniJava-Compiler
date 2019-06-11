package V2VM;

import cs132.vapor.ast.*;

import java.util.*;

public class LinearScan {
  public LinkedList<String> callerRegs = new LinkedList<String>(Arrays.asList(
                  "$t0","$t1","$t2","$t3","$t4","$t5","$t6","$t7","$t8"));
  public LinkedList<String> calleeRegs = new LinkedList<String>(Arrays.asList(
                  "$s0","$s1","$s2","$s3","$s4","$s5","$s6","$s7"));

  public LinkedList<VMVariable> active;
  public LinkedList<String> freeRegs;

  public HashMap<VMVariable,String> stackMap;
  public HashMap<VMVariable,String> regMap;

  public Liveliness liveAnalyze;

  public VFunction curFunction;
  public int localNum;
  public int outNum;

  public HashMap<String,String> finalMap;

  public LinearScan(VFunction inputFunc) throws Throwable {
    this.curFunction = inputFunc;
    this.localNum = 0;


    this.freeRegs = new LinkedList<String>();
    this.stackMap = new HashMap<VMVariable,String>();
    this.regMap = new HashMap<VMVariable,String>();
    this.finalMap = new HashMap<String,String> ();
  }

  public void printFinalMap() {
    System.out.println("Reg Map: ");
    for (Map.Entry<VMVariable,String> entry: regMap.entrySet()) {
      System.out.println("variable: " + entry.getKey().name + " match with reg: " + entry.getValue());
    }
    System.out.println("Final Map:");
    for (Map.Entry<String,String> entry: finalMap.entrySet()) {
      System.out.println("variable: " + entry.getKey() + " match with reg: " + entry.getValue());
    }
  }

  public void scan() throws Throwable{
    liveAnalyze = new Liveliness(curFunction);
    //this.liveAnalyze.printVarList();
    outNum = liveAnalyze.outNum;
    LinearScanAllocation();
    for (Iterator iter = regMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry<VMVariable,String> regPair = (Map.Entry<VMVariable,String>) iter.next();
      finalMap.put(regPair.getKey().name,regPair.getValue());
      iter.remove();
    }
    for (Iterator iter = stackMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry<VMVariable,String> regPair = (Map.Entry<VMVariable,String>) iter.next();
      finalMap.put(regPair.getKey().name,regPair.getValue());
      iter.remove();
    }
  }

  public void LinearScanAllocation() {
    active = new LinkedList<VMVariable>();
    LinkedList<VMVariable> allVar = new LinkedList<VMVariable>(liveAnalyze.varList.values());
    Collections.sort(allVar,new VMVariable.StartPointComparator());
    for (VMVariable variable: allVar) {
      ExpireOldInterval(variable);
      // 17 is total available register both caller save and callee save
      if ((active.size() == 17) || ((variable.isafterCall == true) && calleeRegs.isEmpty())) {
        SpillAtInterval(variable);
      }
      else {
        regMap.put(variable,firstFreeReg(variable.isafterCall));
        active.add(variable);
        Collections.sort(active,new VMVariable.EndPointComparator());
      }
    }
  }

  public void ExpireOldInterval(VMVariable variable) {
    Collections.sort(active,new VMVariable.EndPointComparator());
    for (ListIterator<VMVariable> iterator = active.listIterator(); iterator.hasNext();) {
      VMVariable varJ = iterator.next();
      if (varJ.endLine >= variable.startLine){
        return;
      }
      iterator.remove();
      freeRegs.add(regMap.get(varJ));
    }
  }

  public void SpillAtInterval(VMVariable variable) {
    if (active.isEmpty())
      System.out.println("active emtpy while trying to spill at function " + curFunction.ident);
    VMVariable spill = active.getLast();
    if (spill.endLine > variable.endLine) {
      regMap.put(variable,regMap.get(spill));
      String newLocal = "local["+ localNum++ +"]";
      stackMap.put(spill,newLocal);
      active.remove(spill);
      active.add(variable);
      Collections.sort(active,new VMVariable.EndPointComparator());
    }
    else {
      String newLocal = "local["+ localNum++ +"]";
      stackMap.put(variable,newLocal);
    }
  }

  public String firstFreeReg(boolean isafterCall) {
    if (!isafterCall) {
        if (!freeRegs.isEmpty()) {
          return freeRegs.removeFirst();
        }
        if (callerRegs.isEmpty()) {
          localNum++;
          return calleeRegs.removeFirst();
        }
        return callerRegs.removeFirst();
    }
    for (ListIterator<String> iterator = freeRegs.listIterator(); iterator.hasNext();) {
      String freeReg = iterator.next();
      if (freeReg.contains("s")) {
        iterator.remove();
        return freeReg;
      }
    }
    localNum ++;
    return calleeRegs.removeFirst();
  }

}
