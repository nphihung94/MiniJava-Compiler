package src;
import java.util.*;

public class VirtualMethod {
  public String name;
  public List<String> paraName;
  public List<String> localName;
  public HashMap<String,String> paraTypeList;
  public HashMap<String,String> localTypeList;
  public String retType;

  public VirtualMethod(String nameMethod) {
    this.name = nameMethod;
    this.paraName = new ArrayList<String>();
    this.localName = new ArrayList<String>();
    this.paraTypeList = new HashMap<String,String>();
    this.localTypeList = new HashMap<String,String>();
  }

  public boolean equals(Object object) {
    if (object == this)
      return true;
    if (object instanceof VirtualMethod)
      {
        VirtualMethod object_class = (VirtualMethod) object;
        return this.name.equals(object_class.name);
      }
    return false;
  }

}
