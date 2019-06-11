package typechecker;
import java.util.*;

public class MyClass {

  public HashMap<String, MyType> fields;
  public HashMap<String, MyMethods> methods;

  public String name;
  public MyClass parent;
  public boolean init;

  public MyClass (String name) {
    this.name = name;
    this.parent = null;
    this.init = true;
    fields = new HashMap<String, MyType>();
    methods = new HashMap<String, MyMethods>();
  }

  // If found field in the current class then return field Type, otherwise return null
  public MyType getFields_Type(String field_name) {
    if (parent != null)
      {
        MyType parent_fieldType = parent.getFields_Type(field_name);
        if (parent_fieldType != null)
          return parent_fieldType;
      }
    return fields.get(field_name);
  }

  // If found methods in class or parent class then return it.
  public MyMethods getMethods(String method_name){
    MyMethods ret_methods = methods.get(method_name);
    if (ret_methods != null)
      return ret_methods;

    if (parent != null) {
      MyMethods parent_methods = parent.getMethods(method_name);
      if (parent_methods != null)
        return parent_methods;
    }

    return null;
  }

  public boolean equals(Object object) {
    if (object == this)
      return true;
    if (object instanceof MyClass)
      {
        MyClass object_class = (MyClass) object;
        return this.name.equals(object_class.name);
      }
    return false;
  }
}
