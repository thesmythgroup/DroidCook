package com.codesmyth.droidcook.compiler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.codesmyth.droidcook.api.Event;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class EventProcessor extends AbstractProcessor {

  private Types    mTypeUtils;
  private Elements mElementUtils;
  private Filer    mFiler;
  private Messager mMessager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    mTypeUtils = processingEnv.getTypeUtils();
    mElementUtils = processingEnv.getElementUtils();
    mFiler = processingEnv.getFiler();
    mMessager = processingEnv.getMessager();
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
        if (el.getKind() != ElementKind.INTERFACE && !el.getModifiers().contains(Modifier.ABSTRACT)) {
          mMessager.printMessage(Diagnostic.Kind.ERROR, "Only interfaces or abstract classes accepted for @Event", el);
        }
        try {
          JavaFile.builder(mElementUtils.getPackageOf(el).toString(), makeType((TypeElement) el))
              .build()
              .writeTo(mFiler);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      return true;
    } catch (ProcessorException | ClassNotFoundException e) {
      mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
      return true;
    }
  }

  public TypeSpec makeType(TypeElement el) throws ProcessorException, ClassNotFoundException {
    String className = "Event_" + el.getSimpleName();

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
      if (el.getKind() != ElementKind.INTERFACE && !ch.getModifiers().contains(Modifier.ABSTRACT)) {
        continue;
      }

      String key = ch.getSimpleName().toString();
      ExecutableElement ex = (ExecutableElement) ch;

      // TODO(d) handle unboxing in future
      TypeName paramType = mirrorToType(ex.getReturnType());
      TypeName boxedType = mirrorToType(ex.getReturnType());

      MethodSpec get = MethodSpec.methodBuilder(key)
          .addModifiers(Modifier.PUBLIC)
          .addStatement("return mExtras.$L($S)", findGetMethod(boxedType), key)
          .returns(paramType)
          .build();

      event.addMethod(get);
    }

    return event.build();
  }

  public TypeSpec makeTypeBuilder(TypeElement el) throws ProcessorException, ClassNotFoundException {
    FieldSpec extras = FieldSpec.builder(Bundle.class, "mExtras")
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .initializer("new $T()", Bundle.class)
        .build();

    String className = "Builder";

    TypeSpec.Builder event = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .addField(extras)
        .addMethod(MethodSpec.methodBuilder("broadcast")
            .addModifiers(Modifier.PUBLIC)
            .returns(boolean.class)
            .addParameter(Context.class, "context")
            .addStatement("return $T.getInstance(context).sendBroadcast(new $T(ACTION).putExtras(mExtras))",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"), Intent.class)
            .build())
        .addMethod(MethodSpec.methodBuilder("broadcastSync")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("$T.getInstance(context).sendBroadcastSync(new $T(ACTION).putExtras(mExtras))",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"), Intent.class)
            .build());

    for (Element ch : el.getEnclosedElements()) {
      if (ch.getKind().isClass() || ch.getKind().isInterface()) {
        continue;
      }

      if (el.getKind() != ElementKind.INTERFACE && !ch.getModifiers().contains(Modifier.ABSTRACT)) {
        continue;
      }

      String key = ch.getSimpleName().toString();
      ExecutableElement ex = (ExecutableElement) ch;

      // TODO(d) handle unboxing in future
      TypeName paramType = mirrorToType(ex.getReturnType());
      TypeName boxedType = mirrorToType(ex.getReturnType());

      MethodSpec set = MethodSpec.methodBuilder(key)
          .addModifiers(Modifier.PUBLIC)
          .returns(ClassName.bestGuess(className))
          .addParameter(paramType, "x")
          .addStatement("mExtras.$L($S, x)", findPutMethod(boxedType), key)
          .addStatement("return this")
          .build();

      event.addMethod(set);
    }

    return event.build();
  }

  TypeName mirrorToType(TypeMirror type) throws ProcessorException {
    TypeName result;
    boolean isArray = type instanceof ArrayType;

    if (isArray) {
      result = ArrayTypeName.get((ArrayType) type);
    } else if (type.getKind().isPrimitive()) {
      result = TypeName.get(type);
    } else {
      result = TypeName.get(type);
    }
    return result;
  }

  private String findGetMethod(TypeName typeName) throws ClassNotFoundException, ProcessorException {
    for (Method m : Bundle.class.getMethods()) {
      if (!m.getName().startsWith("get")) {
        continue;
      }
      Type[] params = m.getGenericParameterTypes();
      if (params.length != 0 && params[0].equals(String.class)) {
        TypeName genReturnType = TypeName.get(m.getGenericReturnType());
        TypeName returnType = TypeName.get(m.getReturnType());
        if (genReturnType.equals(typeName) || returnType.equals(typeName)) {
          return m.getName();
        }
      }
    }
    throw new ProcessorException("Failed to find get method for " + typeName);
  }

  private String findPutMethod(TypeName typeName) throws ClassNotFoundException, ProcessorException {
    for (Method m : Bundle.class.getMethods()) {
      if (!m.getName().startsWith("put")) {
        continue;
      }
      Type[] params = m.getGenericParameterTypes();
      if (params.length == 2 && params[0].equals(String.class)) {
        TypeName param = TypeName.get(params[1]);
        if (param.equals(typeName)) {
          return m.getName();
        }
      }
    }
    throw new ProcessorException("Failed to find put method for " + typeName);
  }
}
