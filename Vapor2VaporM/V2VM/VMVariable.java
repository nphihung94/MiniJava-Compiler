package V2VM;
import java.util.*;

public class VMVariable{
  public String name;
  public int startLine;
  public int endLine;
  // Labels that variable is under
  public Set<String> beforeLabel = new HashSet<>();
  // Labels that variable used after that label
  public Set<String> afterLabel = new HashSet<>();
  public boolean isbeforeCall = false;
  public boolean isafterCall = false;

  public VMVariable(String varName, int startLine) {
    this.name = varName;
    this.startLine = startLine;
  }

  public void readVariable(int line) {
    this.endLine = line;
    if (!beforeLabel.isEmpty())
      afterLabel.addAll(beforeLabel);
    beforeLabel.clear();
    isafterCall = isbeforeCall;
  }

  public void writeVariable(int line) {
    this.endLine = line;
    beforeLabel.clear();
  }


// Override Comparator sort varibale by endPoint/startpoint
  public static class StartPointComparator implements Comparator<VMVariable> {
    @Override
    public int compare(VMVariable variable1, VMVariable variable2) {
      return variable1.startLine - variable2.startLine;
    }
  }

  public static class EndPointComparator implements Comparator<VMVariable> {
    @Override
    public int compare(VMVariable variable1, VMVariable variable2) {
      return variable1.endLine - variable2.endLine;
    }
  }


}
