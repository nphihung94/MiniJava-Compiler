package typechecker;

import syntaxtree.*;
import visitor.GJNoArguDepthFirst;
import java.util.*;

public class SymbolTable extends GJNoArguDepthFirst<Boolean> {
  public HashMap<String, MyClass> classes = new HashMap<String, MyClass>();
  public MyClass currentClass = null;
  public MyMethods currentMethods = null;

  public boolean isSubClass(MyClass childClass, MyClass parentClass) {
    System.out.println("Compare "+childClass.name + " and "+parentClass.name);
    if (childClass.equals(parentClass))
      return true;

    while (! childClass.equals(parentClass)) {
      if (childClass.parent == null)
        return false;
      System.out.println("Compare "+childClass.name + " and "+parentClass.name);
      childClass = childClass.parent;
    }
    return true;
  }

  public boolean isSubType(MyType first, MyType second) {
    System.out.println("Compare "+first.type +" and "+second.type);
    if ((first.type == MyType.Type.ID) && (second.type == MyType.Type.ID))
      {
        System.out.println("Compare type of " + first.name + " and "+second.name);
        return (isSubClass(classes.get(first.name), classes.get(second.name)) || isSubClass(classes.get(second.name),classes.get(first.name)));
      }
    System.out.println("result is " + first.equals(second));
    return first.equals(second);
  }

  public MyMethods getMethod(String className, String methodName){
    MyClass methodClass = classes.get(className);
    if (methodClass == null)
      return null;
    MyMethods retMethod = methodClass.methods.get(methodName);
    // If there is method in class, return it
    if (retMethod != null)
      return retMethod;
    // Otherwise check parent class.
    if (methodClass.parent == null)
      return null;
    return getMethod(methodClass.parent.name,methodName);
  }

  public MyClass addClass(String className){
    MyClass retClass = classes.get(className);
    if (retClass == null)
      {
        MyClass newClass = new MyClass(className);
        classes.put(className,newClass);
        System.out.println("Add class "+ className +" into classes");
        retClass = newClass;
        System.out.println("Classes now is "+classes);
      }
    else if (retClass.init == false)
      {
        retClass.init = true;
      }
      else
        return null;
    return retClass;
  }



  public MyClass addParent(String child, String parent){
    MyClass childClass = classes.get(child);
    if (childClass == null)
      return null;
    MyClass parentClass = classes.get(parent);
    if (parentClass == null)
      {
        parentClass = addClass(parent);
        parentClass.init = false;
      }
    childClass.parent = parentClass;
    return childClass;
  }

  public String getTypeName(Type t) {
    if (t.f0.which != 3)
      return null;
    Identifier castType = (Identifier) t.f0.choice;
    return castType.f0.toString();
  }

  public MyType getType(Type t) {
      switch(t.f0.which) {
        case 0: return MyType.ARRAY;
        case 1: return MyType.BOOLEAN;
        case 2: return MyType.INTEGER;
        case 3:
        String className = getTypeName(t);
        MyType newType = new MyType(className,MyType.Type.ID);
        if (classes.get(className) == null) {
          MyClass newClass = new MyClass(className);
          newClass.init = false;
          classes.put(className,newClass);
        }

        return newType;
        default: return null;
    }
  }

  public MyMethods addMethod(String methodName, MyType mtype) {
    MyMethods checkExist = currentClass.methods.get(methodName);
    if (checkExist != null)
      return null;
    MyMethods newMethod = new MyMethods(methodName, mtype);
    currentClass.methods.put(methodName, newMethod);
    return newMethod;
  }

  public MyType addField(String fieldName, MyType ftype) {
    if (currentMethods == null) {
      MyType checkExistfield = currentClass.fields.get(fieldName);
      if (checkExistfield != null)
        return null;
      MyType newfield = new MyType(fieldName, ftype.type);
      currentClass.fields.put(fieldName,ftype);
      System.out.println("Add "+fieldName +" into class "+currentClass.name);
      return newfield;
    }

    MyType checkExistField = currentMethods.fields.get(fieldName);
    if (checkExistField != null)
      return null;
    MyType checkExistPara = currentMethods.parameters.get(fieldName);
    if (checkExistPara != null)
      return null;
    MyType newfield = new MyType(fieldName, ftype.type);
    currentMethods.fields.put(fieldName,ftype);
    return newfield;
  }

  public MyType addParameter(String paraName, MyType paraType) {
    if (currentMethods == null)
      return null;
    MyType checkExistPara = currentMethods.parameters.get(paraName);
    if (checkExistPara != null)
      return null;
    currentMethods.parameters.put(paraName,paraType);
    return paraType;
  }
  //
  // Auto class visitors--probably don't need to be overridden.
  //
  public Boolean visit(NodeList n) {
     for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
        if (e.nextElement().accept(this) == null)
          return null;
     }
     return true;
  }

  public Boolean visit(NodeListOptional n) {
     if ( n.present() ) {
        for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
           if (e.nextElement().accept(this) == null)
            return null;
        }
        return true;
     }
     else
        return true;
  }

  public Boolean visit(NodeOptional n) {
     if ( n.present() )
        return n.node.accept(this);
     else
        return true;
  }

  //
  // User-generated visitor methods below
  //

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public Boolean visit(Goal n) {
     if (n.f0.accept(this) == null)
      return null;
     if (n.f1.accept(this) == null)
      return null;
     System.out.println("Classes in SymbolTable");
     for (MyClass checkClassInit: classes.values()) {
       System.out.println(checkClassInit.name);
       if (checkClassInit.init == false)
       {
        System.out.println("Has uninit Class: " + checkClassInit.name);
        System.out.println("Hashmap classes is :" + classes);
        return null;
       }

     }
     return true;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> "public"
   * f4 -> "static"
   * f5 -> "void"
   * f6 -> "main"
   * f7 -> "("
   * f8 -> "String"
   * f9 -> "["
   * f10 -> "]"
   * f11 -> Identifier()
   * f12 -> ")"
   * f13 -> "{"
   * f14 -> ( VarDeclaration() )*
   * f15 -> ( Statement() )*
   * f16 -> "}"
   * f17 -> "}"
   */
  public Boolean visit(MainClass n) {
    MyClass mainclass = addClass(n.f1.f0.toString());
    if (mainclass == null)
      return null;
    currentClass = mainclass;

    MyMethods mainMethod = addMethod("mainMethod",MyType.OTHER);
    if (mainMethod == null)
      return null;
    currentMethods = mainMethod;
    MyType mainPara = addParameter(n.f11.f0.toString(),MyType.OTHER);
    Boolean ret = n.f14.accept(this);
    currentClass = null;
    currentMethods = null;
    return ret;
  }

  /**
   * f0 -> ClassDeclaration()
   *       | ClassExtendsDeclaration()
   */
  public Boolean visit(TypeDeclaration n) {
     return n.f0.accept(this);
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */
  public Boolean visit(ClassDeclaration n) {
    MyClass newClass = addClass(n.f1.f0.toString());
    if (newClass == null)
      return null;
    currentClass = newClass;
    if (n.f3.accept(this) == null)
      return null;
    Boolean ret = n.f4.accept(this);
    currentClass = null;
    return ret;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "extends"
   * f3 -> Identifier()
   * f4 -> "{"
   * f5 -> ( VarDeclaration() )*
   * f6 -> ( MethodDeclaration() )*
   * f7 -> "}"
   */
  public Boolean visit(ClassExtendsDeclaration n) {
    MyClass child = addClass(n.f1.f0.toString());
    if (child == null)
      return null;
    MyClass curClass = addParent(n.f1.f0.toString(),n.f3.f0.toString());
    if (curClass == null)
      return null;
    currentClass = curClass;
    if (n.f5.accept(this) == null)
      return null;
    Boolean ret = n.f6.accept(this);
    currentClass = null;
    return ret;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */
  public Boolean visit(VarDeclaration n) {
    System.out.println("Type name of VarDeclaration is: " + n.f0.toString());
    if (addField(n.f1.f0.toString(),getType(n.f0)) == null)
      return null;
    //System.out.println("Class field after add para");
    //for (MyType printField: currentClass.fields.values()) {
    //    System.out.println(printField.name + " type: " + printField.type);
    //}
    return true;
  }

  /**
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */
  public Boolean visit(MethodDeclaration n) {
    MyMethods newMethod = addMethod(n.f2.f0.toString(),getType(n.f1));
    if (newMethod == null)
    {
      System.out.println("Wrong at MethodDeclaration");
      return null;
    }
    currentMethods = newMethod;
    if (n.f4.accept(this) == null)
    {
      System.out.println("Wrong at MethodDeclaration");
      return null;
    }
    Boolean ret = n.f7.accept(this);


    //System.out.println("Method list: ");
    //for (MyMethods printField: currentClass.methods.values()) {
    //    System.out.println(printField.name);
    //}
    currentMethods = null;
    return ret;
  }

  /**
   * f0 -> FormalParameter()
   * f1 -> ( FormalParameterRest() )*
   */
  public Boolean visit(FormalParameterList n) {
    if (n.f0.accept(this) == null)
    {
      System.out.println("Wrong at FormalParameterList");
      return null;
    }

    return n.f1.accept(this);
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   */
  public Boolean visit(FormalParameter n) {
    if (addParameter(n.f1.f0.toString(),getType(n.f0)) == null)
    {
      System.out.println("Wrong at FormalParameter");
      return null;
    }

    return true;
  }

  /**
   * f0 -> ","
   * f1 -> FormalParameter()
   */
  public Boolean visit(FormalParameterRest n) {
     return n.f1.accept(this);
  }
}
