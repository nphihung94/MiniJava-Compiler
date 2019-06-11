package src;
import syntaxtree.*;
import java.util.*;
import visitor.DepthFirstVisitor;


public class VirtualTable extends DepthFirstVisitor{
  public HashMap<String,VirtualClass> classList = new HashMap<String,VirtualClass>();
  public VirtualClass currentClassScope;
  public VirtualMethod currentMethodScope;
  public boolean addVar = false;

  public String getType(Type nodeType) {
    switch (nodeType.f0.which)  {
      case 0: return "int[]";
      case 1: return "bool";
      case 2: return "int";
      case 3: return ((Identifier) nodeType.f0.choice).f0.toString();
      default:
        return null;
    }
  }


   //
   // User-generated visitor methods below
   //

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    public void visit(Goal n) {
      n.f0.accept(this);
      for (Node typeDeclare: n.f1.nodes){
          typeDeclare.accept(this);
      }
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
   public void visit(MainClass n) {

      VirtualClass main = new VirtualClass(n.f1.f0.toString());
      VirtualMethod main_method = new VirtualMethod("main");
      main.methodList.add(main_method);
      currentClassScope = main;
      currentMethodScope = main_method;
      classList.put(n.f1.f0.toString(),main);
      addVar = true;
      for (Node varDeclare: n.f14.nodes){
        varDeclare.accept(this);
      }
    }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public void visit(TypeDeclaration n) {
      n.f0.accept(this);
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public void visit(ClassDeclaration n) {
     String className = n.f1.f0.toString();
     VirtualClass newClass;
     if (classList.get(className) == null) {
       newClass = new VirtualClass(className);
       classList.put(className,newClass);
     }
     else {
       newClass = classList.get(className);
     }
     currentClassScope = newClass;

     addVar = true;
     for (Node varDeclare: n.f3.nodes) {
       varDeclare.accept(this);
     }

     addVar = false;
     for (Node methods: n.f4.nodes){
       methods.accept(this);
     }
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
   public void visit(ClassExtendsDeclaration n) {
      String className = n.f1.f0.toString();
      String parentName = n.f3.f0.toString();
      VirtualClass newClass;
      if (classList.get(className) == null) {
        newClass = new VirtualClass(className);
        classList.put(className,newClass);
      }
      else {
        newClass = classList.get(className);
      }
      currentClassScope = newClass;

      if (classList.get(parentName) != null) {
        newClass.parentClass = classList.get(parentName);
      }
      else {
        VirtualClass parentClass = new VirtualClass(parentName);
        classList.put(parentName,parentClass);
        newClass.parentClass = parentClass;
      }

      addVar = true;
      for (Node varDeclare: n.f5.nodes) {
        varDeclare.accept(this);
      }

      addVar = false;
      for (Node methods: n.f6.nodes){
        methods.accept(this);
      }
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public void visit(VarDeclaration n) {
      String varName = n.f1.f0.toString();
      if (addVar) {
        currentClassScope.classMembers.add(varName);
        currentClassScope.classMemberType.put(varName,getType(n.f0));
      }
      else {
        currentMethodScope.localName.add(varName);
        currentMethodScope.localTypeList.put(varName,getType(n.f0));
      }
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
   public void visit(MethodDeclaration n) {
      String methodName = n.f2.f0.toString();
      VirtualMethod newMethod = new VirtualMethod(methodName);
      currentClassScope.methodList.add(newMethod);
      currentMethodScope = newMethod;
      newMethod.retType = getType(n.f1);
      n.f4.accept(this);

      n.f7.accept(this);
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> ( FormalParameterRest() )*
    */
   public void visit(FormalParameterList n) {
      n.f0.accept(this);
      for (Node para: n.f1.nodes) {
        para.accept(this);
      }
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public void visit(FormalParameter n) {
      String parameter = n.f1.f0.toString();
      currentMethodScope.paraName.add(parameter);
      currentMethodScope.paraTypeList.put(parameter,getType(n.f0));
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public void visit(FormalParameterRest n) {
      n.f1.accept(this);
   }
 }
