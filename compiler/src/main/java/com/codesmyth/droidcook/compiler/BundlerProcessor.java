package com.codesmyth.droidcook.compiler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.codesmyth.droidcook.api.Bundler;
import com.codesmyth.droidcook.api.Warning;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BundlerProcessor extends AbstractProcessor {
  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager log;

  private TypeMirror parcelableType;
  // TODO serializable, sparsearray, non-primitive list types; specify preferred/weighted order.

  private ClassName parcelableCreatorName = ClassName.get("android.os", "Parcelable.Creator");
  private ClassName localBroadcastManagerName = ClassName.get("android.support.v4.content", "LocalBroadcastManager");
  private ClassName fragmentName = ClassName.get("android.support.v4.app", "Fragment");

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    log = processingEnv.getMessager();

    parcelableType = elementUtils.getTypeElement("android.os.Parcelable").asType();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>();
    set.add(Bundler.class.getCanonicalName());
    return set;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    try {
      for (Element el : roundEnvironment.getElementsAnnotatedWith(Bundler.class)) {
        if (!el.getKind().isInterface() && !el.getModifiers().contains(Modifier.ABSTRACT)) {
          printMessage(Diagnostic.Kind.WARNING, "Only interfaces and abstract classes are supported by @Bundler", el);
          continue;
        }
        try {
          String pkg = elementUtils.getPackageOf(el).toString();
          TypeSpec spec = bundlerSpec((TypeElement) el);
          JavaFile.builder(pkg, spec).build().writeTo(filer);
        } catch (IOException e) {
          e.printStackTrace();
          printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        }
      }
    } catch (ProcessorException|ClassNotFoundException e) {
      printMessage(Diagnostic.Kind.ERROR, e.getMessage());
    }
    return true;
  }

  public TypeSpec bundlerSpec(TypeElement el) throws ProcessorException, ClassNotFoundException {
    ClassName parentName = ClassName.bestGuess(el.toString());
    ClassName className = ClassName.bestGuess(el.getSimpleName() + "Bundler");

    TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addSuperinterface(mirrorToName(parcelableType));
    if (el.getKind().isInterface()) {
      builder.addSuperinterface(parentName);
    } else {
      builder.superclass(parentName);
    }

    // fields
    builder
        .addField(FieldSpec.builder(String.class, "ACTION")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", el)
            .build())
        .addField(FieldSpec.builder(Bundle.class, "data")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            //.initializer("new $T()", Bundle.class)
            .build());

    // constructors
    builder
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("data = new $T()", Bundle.class)
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addJavadoc(JAVADOC_CONSTRUCTOR_BUNDLE)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Bundle.class, "bundle")
            .addStatement("this()")
            .addStatement("readFrom(bundle)")
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addJavadoc(JAVADOC_CONSTRUCTOR_INTENT)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Intent.class, "intent")
            .addStatement("this()")
            .addStatement("readFrom(intent)")
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(parentName, "impl")
            .addStatement("this()")
            .addStatement("readFrom(impl)")
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(fragmentName, "fragment")
            .addStatement("this()")
            .addStatement("readFrom(fragment)")
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(Parcel.class, "in")
            .addStatement("data = Bundle.CREATOR.createFromParcel(in)")
            .build());

    // readFrom(android.os.Bundle) helper explicitly checks if argument contains
    // each managed key; if so, copy value to data.
    //
    // Must be aware of getter and setter Bundle methods used for a key;
    // so, append statements during iteration of enclosing elements;
    // finally, add code block to readFrom method builder.
    CodeBlock.Builder readFromBundle = CodeBlock.builder()
        .beginControlFlow("if (null == bundle || bundle.isEmpty())")
        .addStatement("return")
        .endControlFlow();
    CodeBlock.Builder readFromImpl = CodeBlock.builder()
        .beginControlFlow("if (null == impl)")
        .addStatement("return")
        .endControlFlow();

    // getter and setter methods are created for enclosing elements meeting requirements;
    // must be a method;
    // must be abstract if not an interface method;
    // must take no arguments;
    // must return a value.
    for (Element en : el.getEnclosedElements()) {
      if (en.getKind() != ElementKind.METHOD) {
        continue;
      }
      if (el.getKind().isClass() && !en.getModifiers().contains(Modifier.ABSTRACT)) {
        continue;
      }

      ExecutableElement ex = (ExecutableElement) en;
      if (ex.getParameters().size() != 0) {
        continue;
      }

      TypeMirror dataType = ex.getReturnType();
      if (dataType.getKind() == TypeKind.VOID) {
        continue;
      }

      TypeName dataTypeName = mirrorToName(dataType);
      String name = en.getSimpleName().toString();
      String key = el + "." + name;

      // for use by readFromCode later
      String getterName = "";
      String setterName = "";

      // build getter
      MethodSpec.Builder getter = MethodSpec.methodBuilder(name)
          .addModifiers(Modifier.PUBLIC)
          .returns(dataTypeName);
      try {
        getterName = findGetter(dataType);
        getter.addStatement("return data.$L(\"$L\")", getterName, key);
      } catch (ProcessorException e) {
        printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        AnnotationSpec ann = AnnotationSpec.builder(Warning.class).addMember("value", "$S", e.getMessage()).build();
        getter.addAnnotation(ann).addStatement("throw new $T($S)", RuntimeException.class, e.getMessage());
      } finally {
        builder.addMethod(getter.build());
      }

      // build setter
      MethodSpec.Builder setter = MethodSpec.methodBuilder("set" + title(name))
          .addModifiers(Modifier.PUBLIC)
          .returns(className)
          .addParameter(dataTypeName, "x");
      try {
        setterName = findSetter(dataType);
        setter.addStatement("data.$L(\"$L\", x)", setterName, key).addStatement("return this");
      } catch (ProcessorException e) {
        printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        AnnotationSpec ann = AnnotationSpec.builder(Warning.class).addMember("value", "$S", e.getMessage()).build();
        setter.addAnnotation(ann).addStatement("throw new $T($S)", RuntimeException.class, e.getMessage());
      } finally {
        builder.addMethod(setter.build());
      }

      if (!"".equals(getterName) && !"".equals(setterName)) {
        readFromBundle.beginControlFlow("if (bundle.containsKey($S))", key);
        readFromBundle.addStatement("data.$L($S, bundle.$L($S))", setterName, key, getterName, key);
        readFromBundle.endControlFlow();

        readFromImpl.addStatement("data.$L($S, impl.$L())", setterName, key, name);
      }
    }

    // helper methods
    builder
        .addMethod(MethodSpec.methodBuilder("isEmpty")
            .addModifiers(Modifier.PUBLIC)
            .returns(boolean.class)
            .addStatement("return data.isEmpty()")
            .build())
        .addMethod(MethodSpec.methodBuilder("intent")
            .addJavadoc(JAVADOC_INTENT, className.simpleName())
            .addModifiers(Modifier.PUBLIC)
            .returns(Intent.class)
            .addStatement("return new $T(ACTION).putExtras(data)", Intent.class)
            .build())
        .addMethod(MethodSpec.methodBuilder("localcast")
            .addJavadoc(JAVADOC_LOCALCAST)
            .addModifiers(Modifier.PUBLIC)
            .returns(boolean.class)
            .addParameter(Context.class, "context")
            .addStatement("return $T.getInstance(context).sendBroadcast(intent())", localBroadcastManagerName)
            .build())
        .addMethod(MethodSpec.methodBuilder("localcastSync")
            .addJavadoc(JAVADOC_LOCALCAST_SYNC)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Context.class, "context")
            .addStatement("$T.getInstance(context).sendBroadcastSync(intent())", localBroadcastManagerName)
            .build())
        .addMethod(MethodSpec.methodBuilder("broadcast")
            .addJavadoc(JAVADOC_BROADCAST)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Context.class, "context")
            .addStatement("context.sendBroadcast(intent())")
            .build())
        .addMethod(MethodSpec.methodBuilder("broadcast")
            .addJavadoc(JAVADOC_BROADCAST_PERMISSION)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Context.class, "context")
            .addParameter(String.class, "receiverPermission")
            .addStatement("context.sendBroadcast(intent(), receiverPermission)")
            .build())
        // TODO consider; broadcastDirect(Context, ComponentName) { Context.broadcast(intent().setComponent(Component) }
        .addMethod(MethodSpec.methodBuilder("readFrom")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(parentName, "impl")
            .addCode(readFromImpl.build())
            .build())
        .addMethod(MethodSpec.methodBuilder("readFrom")
            .addJavadoc(JAVADOC_READ_FROM_BUNDLE, className.simpleName())
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Bundle.class, "bundle")
            .addCode(readFromBundle.build())
            .build())
        .addMethod(MethodSpec.methodBuilder("readFrom")
            .addJavadoc(JAVADOC_READ_FROM_INTENT, className.simpleName())
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Intent.class, "intent")
            .beginControlFlow("if (null != intent)")
            .addStatement("readFrom(intent.getExtras())")
            .endControlFlow()
            .build())
        .addMethod(MethodSpec.methodBuilder("readFrom")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(fragmentName, "fragment")
            .beginControlFlow("if (null != fragment)")
            .addStatement("readFrom(fragment.getArguments())")
            .endControlFlow()
            .build())
        .addMethod(MethodSpec.methodBuilder("writeTo")
            .addJavadoc(JAVADOC_WRITE_TO_BUNDLE, className.simpleName())
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Bundle.class, "bundle")
            .beginControlFlow("if (null != bundle)")
            .addStatement("bundle.putAll(data)")
            .endControlFlow()
            .build())
        .addMethod(MethodSpec.methodBuilder("writeTo")
            .addJavadoc(JAVADOC_WRITE_TO_INTENT, className.simpleName())
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Intent.class, "intent")
            .beginControlFlow("if (null != intent)")
            .addStatement("intent.putExtras(data)")
            .endControlFlow()
            .build())
        .addMethod(MethodSpec.methodBuilder("writeTo")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(fragmentName, "fragment")
            .beginControlFlow("if (null != fragment)")
            .addStatement("Bundle bundle = fragment.getArguments()")
            .beginControlFlow("if (null == bundle)")
            .addStatement("bundle = new Bundle()")
            .endControlFlow()
            .addStatement("bundle.putAll(data)")
            .addStatement("fragment.setArguments(bundle)")
            .endControlFlow()
            .build());

    // implement parcelable
    TypeName creatorName = ParameterizedTypeName.get(parcelableCreatorName, className);
    TypeSpec creatorSpec = TypeSpec.anonymousClassBuilder("")
        .addSuperinterface(creatorName)
        .addMethod(MethodSpec.methodBuilder("createFromParcel")
            .addModifiers(Modifier.PUBLIC)
            .returns(className)
            .addParameter(Parcel.class, "in")
            .addStatement("return new $T(in)", className)
            .build())
        .addMethod(MethodSpec.methodBuilder("newArray")
            .addModifiers(Modifier.PUBLIC)
            .returns(ArrayTypeName.of(className))
            .addParameter(int.class, "size")
            .addStatement("return new $T[size]", className)
            .build())
        .build();
    builder
        .addMethod(MethodSpec.methodBuilder("describeContents")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(int.class)
            .addStatement("return data.describeContents()")
            .build())
        .addMethod(MethodSpec.methodBuilder("writeToParcel")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Parcel.class, "out")
            .addParameter(int.class, "flags")
            .addStatement("data.writeToParcel(out, flags)")
            .build())
        .addField(FieldSpec.builder(creatorName, "CREATOR")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$L", creatorSpec)
            .build());

    return builder.build();
  }

  /**
   * Convert first character of String to uppercase.
   * When String is null or empty, return unchanged.
   */
  String title(String x) {
    if (null == x || "".equals(x)) {
      return x;
    }
    return x.substring(0, 1).toUpperCase() + x.substring(1);
  }

  TypeName mirrorToName(TypeMirror t) throws ProcessorException {
    if (t.getKind() == TypeKind.ARRAY) {
      return ArrayTypeName.get((ArrayType) t);
    }
    return TypeName.get(t);
  }

  private String findGetter(TypeMirror t) throws ClassNotFoundException, ProcessorException {
    TypeName name = mirrorToName(t);

    // Exact string matches are preferred to avoid generating code that accidentally
    // type casts, but also need to facilitate Parcelable and Serializable
    // which will be checked explicitly before return; but here, lies are last hope.
    String assignable = "";

    for (Method m : Bundle.class.getMethods()) {
      if (!m.getName().startsWith("get")) {
        continue;
      }
      Type[] params = m.getGenericParameterTypes();
      if (params.length == 1 && params[0].equals(String.class)) {

        Type genericReturnType = m.getGenericReturnType();
        TypeName genericReturnName = TypeName.get(genericReturnType);

        Type returnType = m.getReturnType();
        TypeName returnName = TypeName.get(returnType);

        if (name.equals(genericReturnName) || name.equals(returnName)) {
          return m.getName();
        } else if ((returnType == Parcelable.class || genericReturnType == Parcelable.class) &&
            typeUtils.isAssignable(t, parcelableType)) {
          assignable = m.getName();
        } // TODO other uncommon types
      }
    }
    if ("".equals(assignable)) {
      throw new ProcessorException("Failed to find getter for " + name);
    }
    return assignable;
  }

  private String findSetter(TypeMirror t) throws ClassNotFoundException, ProcessorException {
    TypeName name = mirrorToName(t);

    // see comment in findGetter
    String assignable = "";

    for (Method m : Bundle.class.getMethods()) {
      if (!m.getName().startsWith("put")) {
        continue;
      }
      Type[] params = m.getGenericParameterTypes();
      if (params.length == 2 && params[0].equals(String.class)) {
        Type param = params[1];
        if (TypeName.get(param).equals(name)) {
          return m.getName();
        } else if (param == Parcelable.class && typeUtils.isAssignable(t, parcelableType)) {
          assignable = m.getName();
        } // TODO other uncommon types
      }
    }

    if ("".equals(assignable)) {
      throw new ProcessorException("Failed to find setter for " + name);
    }
    return assignable;
  }

  void printMessage(Diagnostic.Kind kind, String message) {
    log.printMessage(kind, "DroidCook BundlerProcessor: " + message);
  }

  void printMessage(Diagnostic.Kind kind, String message, Element el) {
    log.printMessage(kind, "DroidCook BundlerProcessor: " + message, el);
  }

  private static final String JAVADOC_CONSTRUCTOR_BUNDLE = "Calls readFrom on argument";
  private static final String JAVADOC_CONSTRUCTOR_INTENT = "Calls readFrom on argument";
  private static final String JAVADOC_INTENT =
      "Returns new action intent with shallow copy of $L data.";
  private static final String JAVADOC_LOCALCAST =
      "Broadcast new action intent to all local BroadcastReceivers within your process.\n" +
          "This call is asynchronous; it returns immediately.";
  private static final String JAVADOC_LOCALCAST_SYNC =
      "Like localcast; but if there are any receivers for the Intent, this function will block\n" +
          "and immediately dispatch them before returning.";
  private static final String JAVADOC_BROADCAST =
      "Broadcast new action intent to all interested BroadcastReceivers.\n" +
          "This call is asynchronous; it returns immediately.";
  private static final String JAVADOC_BROADCAST_PERMISSION =
      "Broadcast new action intent to all interested BroadcastReceivers; enforce optional required permission.\n" +
          "This call is asynchronous; it returns immediately.";
  private static final String JAVADOC_READ_FROM_BUNDLE =
      "Write shallow copy of Bundle data for matching keys to $L data; does nothing if Bundle is null.";
  private static final String JAVADOC_READ_FROM_INTENT =
      "Write shallow copy of Intent extras for matching keys to $L data; does nothing if Intent or extras is null.";
  private static final String JAVADOC_WRITE_TO_BUNDLE =
      "Write shallow copy of $L data to Bundle; does nothing if Bundle is null.";
  private static final String JAVADOC_WRITE_TO_INTENT =
      "Write shallow copy of $L data to Intent extras; does nothing if Intent is null.";
}


