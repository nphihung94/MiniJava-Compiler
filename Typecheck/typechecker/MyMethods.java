package typechecker;

import java.util.*;

public class MyMethods {

  MyType returnType;
  String name;
  public HashMap<String, MyType> fields;
  public LinkedHashMap<String, MyType> parameters;

  public MyMethods(String name,MyType rett) {
    this.name = name;
    this.returnType = rett;
    this.fields = new HashMap<String, MyType>();
    this.parameters = new LinkedHashMap<String, MyType>();
  }

  public MyType getFields_Type (String field_name) {
    return fields.get(field_name);
  }

  public MyType getPara_Type (String parameters_name){
    return parameters.get(parameters_name);
  }

  public Collection<MyType> getPara_TypeList () {
    return parameters.values();
  }
}
