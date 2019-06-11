package typechecker;

public class MyType {
  public enum Type {
    Array, Boolean, Integer, ID, Other;
  }

  public static MyType ARRAY = new MyType(Type.Array);
  public static MyType BOOLEAN = new MyType(Type.Boolean);
  public static MyType INTEGER = new MyType(Type.Integer);
  public static MyType ID = new MyType(Type.ID);
  public static MyType OTHER = new MyType(Type.Other);

  public String name;
  public Type type;

  /* Constructor for a Type */
  public MyType(Type t){
    this.name = "";
    this.type = t;
  }

  public MyType(String name, Type t){
    this.name = name;
    this.type = t;
  }



  public boolean equals(Object object){
    /* If exactly one object return true */
    if (object == this)
      return true;
    /* If object is a child of MyType then cast and compare Type and Name */
    if (object instanceof MyType){
      MyType cast_object = (MyType) object;
      if (cast_object.type == this.type) {
        if (this.type == Type.ID)
          return this.name.equals(cast_object.name);
        return true;
      }
    }

    return false;
  }
}
