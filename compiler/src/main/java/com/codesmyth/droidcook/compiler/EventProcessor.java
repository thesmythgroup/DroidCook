package com.codesmyth.droidcook.compiler;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import com.codesmyth.droidcook.api.Event;
import com.codesmyth.droidcook.api.Warning;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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
public class EventProcessor extends AbstractProcessor {

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager msg;

  private TypeMirror parcelableType;
  // TODO serializable, sparsearray, non-primitive list types; specify preferred/weighted order.

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    msg = processingEnv.getMessager();

    parcelableType = elementUtils.getTypeElement("android.os.Parcelable").asType();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>();
    set.add(Event.class.getCanonicalName());
    return set;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    try {
      for (Element el : roundEnvironment.getElementsAnnotatedWith(Event.class)) {
        if (!el.getKind().isInterface() && !el.getModifiers().contains(Modifier.ABSTRACT)) {
          msg.printMessage(Diagnostic.Kind.WARNING,
              "Only interfaces or abstract classes accepted for @Event", el);
        }
        try {
          JavaFile.builder(elementUtils.getPackageOf(el).toString(), makeType((TypeElement) el))
              .build()
              .writeTo(filer);
        } catch (IOException e) {
          e.printStackTrace();
          msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        }
      }
      return true;
    } catch (ProcessorException | ClassNotFoundException e) {
      msg.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
      return true;
    }
  }

  public TypeSpec makeType(TypeElement el) throws ProcessorException, ClassNotFoundException {
    String className = "Event" + el.getSimpleName();

    TypeSpec.Builder event = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("mExtras = new $T()", Bundle.class)
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Bundle.class, "extras")
            .addStatement("mExtras = extras")
            .build())
        .addField(FieldSpec.builder(String.class, "ACTION")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", el.toString())
            .build())
        .addField(FieldSpec.builder(Bundle.class, "mExtras")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .build())
        .addType(makeTypeBuilder(el))
        .addMethod(MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.bestGuess("Builder"))
            .addStatement("return new Builder()")
            .build());

    if (el.getKind().isInterface()) {
      event.addSuperinterface(ClassName.bestGuess(el.toString()));
    } else {
      event.superclass(ClassName.bestGuess(el.toString()));
    }

    for (Element ch : el.getEnclosedElements()) {
      if (!(el.getKind().isInterface() || ch.getModifiers().contains(Modifier.ABSTRACT))) {
        continue;
      }

      String key = ch.getSimpleName().toString();
      ExecutableElement ex = (ExecutableElement) ch;

      // TODO(d) handle unboxing in future
      TypeName paramType = mirrorToName(ex.getReturnType());
      TypeMirror boxedType = ex.getReturnType();

      MethodSpec.Builder getter = MethodSpec.methodBuilder(key)
          .addModifiers(Modifier.PUBLIC)
          .returns(paramType);

      try {
        getter = getter.addStatement("return mExtras.$L($S)", findGetter(boxedType), key);
      } catch (ProcessorException e) {
        msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        AnnotationSpec ann = AnnotationSpec.builder(Warning.class)
            .addMember("value", "$S", e.getMessage()).build();
        getter = getter.addAnnotation(ann)
            .addStatement("throw new $T($S)", RuntimeException.class, e.getMessage());
      } finally {
        event.addMethod(getter.build());
      }
    }

    return event.build();
  }

  public TypeSpec makeTypeBuilder(TypeElement el)
      throws ProcessorException, ClassNotFoundException {
    FieldSpec extras = FieldSpec.builder(Bundle.class, "mExtras")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .initializer("new $T()", Bundle.class)
        .build();

    String className = "Builder";

    TypeSpec.Builder event = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addField(extras)
        .addMethod(MethodSpec.methodBuilder("intent")
            .addModifiers(Modifier.PUBLIC)
            .returns(Intent.class)
            .addStatement("return new $T(ACTION).putExtras(mExtras)", Intent.class)
            .build())
        .addMethod(MethodSpec.methodBuilder("pendingIntent")
            .addModifiers(Modifier.PUBLIC)
            .returns(PendingIntent.class)
            .addParameter(Context.class, "context")
            .addParameter(int.class, "requestCode")
            .addParameter(int.class, "flags")
            .addStatement("return $T.getBroadcast(context, requestCode, intent(), flags)",
                PendingIntent.class)
            .build())
        .addMethod(MethodSpec.methodBuilder("broadcast")
            .addModifiers(Modifier.PUBLIC)
            .returns(boolean.class)
            .addParameter(Context.class, "context")
            .addStatement(
                "return $T.getInstance(context).sendBroadcast(intent())",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build())
        .addMethod(MethodSpec.methodBuilder("broadcastSync")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement(
                "$T.getInstance(context).sendBroadcastSync(intent())",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build());

    for (Element ch : el.getEnclosedElements()) {
      if (ch.getKind().isClass() || ch.getKind().isInterface()) {
        continue;
      }

      if (!(el.getKind().isInterface() || ch.getModifiers().contains(Modifier.ABSTRACT))) {
        continue;
      }

      String key = ch.getSimpleName().toString();
      ExecutableElement ex = (ExecutableElement) ch;

      // TODO(d) handle unboxing in future
      TypeName paramType = mirrorToName(ex.getReturnType());
      TypeMirror boxedType = ex.getReturnType();

      MethodSpec.Builder setter = MethodSpec.methodBuilder(key)
          .addModifiers(Modifier.PUBLIC)
          .returns(ClassName.bestGuess(className))
          .addParameter(paramType, "x");

      try {
        setter = setter.addStatement("mExtras.$L($S, x)", findSetter(boxedType), key)
            .addStatement("return this");
      } catch (ProcessorException e) {
        msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        AnnotationSpec ann = AnnotationSpec.builder(Warning.class)
            .addMember("value", "$S", e.getMessage()).build();
        setter = setter.addAnnotation(ann)
            .addStatement("throw new $T($S)", RuntimeException.class, e.getMessage());
      } finally {
        event.addMethod(setter.build());
      }
    }

    return event.build();
  }

  TypeName mirrorToName(TypeMirror t) throws ProcessorException {
    if (t.getKind() == TypeKind.ARRAY) {
      return ArrayTypeName.get((ArrayType) t);
    }
    return TypeName.get(t);
  }

  private String findGetter(TypeMirror t) throws ClassNotFoundException, ProcessorException {
    TypeName name = mirrorToName(t);

    // see comment in findSetter
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
        } else if ((returnType == Parcelable.class || genericReturnType == Parcelable.class)
            && typeUtils.isAssignable(t, parcelableType)) {
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

    // Exact string matches are preferred to avoid generating code that accidentally
    // type casts, but also need to facilitate Parcelable and Serializable,
    // which will be checked explicitly before return; but here, lies are last hope.
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
}


