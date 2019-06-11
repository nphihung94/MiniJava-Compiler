package src;
import java.util.*;

public class VirtualClass {

  public String name;
  public List<String> classMembers;
  public HashMap<String,String> classMemberType;
  public List<VirtualMethod> methodList;
  public VirtualClass parentClass;

  public VirtualClass(String className) {
    this.name = className;
    this.classMembers = new ArrayList<String>();
    this.classMemberType = new HashMap<String,String>();
    this.methodList = new ArrayList<VirtualMethod>();
    this.parentClass = null;
  }

  // Return a LinkedList of type T contains all the members from current className
  // and Parent Class.
  public <T> List<T> getList(Class<T> Type, List<T> Object){
    // If there is no parent class, return the object in form of list of class<T>
    if (this.parentClass == null)
      return new ArrayList<T>(Object);

    List<T> parentMember;
    if (Type.equals(String.class)) {
      parentMember = (List<T>) parentClass.getMemberList();
    }
    else {
      parentMember = (List<T>) parentClass.getMethodList();
    }

    // remove duplicates member between parents and current class
    List<T> notduplicate = new ArrayList<T>(Object);
    Set<T> curObjectMember = new HashSet<T>(Object);
    for (int i = 0; i < parentMember.size(); i++) {
      T check = parentMember.get(i);
      if (curObjectMember.contains(check))
        notduplicate.remove(check);
    }

    parentMember.addAll(notduplicate);
    return parentMember;
  }

  // Size of Class in byte: 4 bytes for the class methods pointer and 4 byte
  // for each fields
  public int size() {
    return 4 + 4*getMemberList().size();
  }

  public List<String> getMemberList() {
    return getList(String.class,classMembers);
  }

  public List<VirtualMethod> getMethodList() {
    return getList(VirtualMethod.class,methodList);
  }

  public VirtualMethod getMethod(String methodName) {
    List<VirtualMethod> allMethods = this.getMethodList();
    for (VirtualMethod iter: allMethods) {
      if (iter.name.equals(methodName))
        return iter;
    }
    return null;
  }

  public String getClass(String methodName) {
    if (methodList.indexOf(methodName) == -1){
      if (parentClass == null)
        return null;
      return parentClass.getClass(methodName);
    }
    return name;
  }

}
