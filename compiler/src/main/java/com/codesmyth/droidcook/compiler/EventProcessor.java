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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
  }

  public TypeSpec makeType(TypeElement el) {
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

      TypeName paramType = ex.getReturnType().getKind().isPrimitive()
          ? ClassName.get(mTypeUtils.getPrimitiveType(ex.getReturnType().getKind()))
          : ClassName.bestGuess(ex.getReturnType().toString());

      ClassName boxedType = ex.getReturnType().getKind().isPrimitive()
          ? ClassName.bestGuess(mTypeUtils.boxedClass(mTypeUtils.getPrimitiveType(ex.getReturnType().getKind())).toString())
          : ClassName.bestGuess(ex.getReturnType().toString());

      MethodSpec get = MethodSpec.methodBuilder(key)
          .addModifiers(Modifier.PUBLIC)
          .addStatement("return ($T) mExtras.get($S)", boxedType, key)
          .returns(paramType)
          .build();

      event.addMethod(get);
    }

    return event.build();
  }

  public TypeSpec makeTypeBuilder(TypeElement el) {
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

      TypeName paramType = ex.getReturnType().getKind().isPrimitive()
          ? ClassName.get(mTypeUtils.getPrimitiveType(ex.getReturnType().getKind()))
          : ClassName.bestGuess(ex.getReturnType().toString());

      ClassName boxedType = ex.getReturnType().getKind().isPrimitive()
          ? ClassName.bestGuess(mTypeUtils.boxedClass(mTypeUtils.getPrimitiveType(ex.getReturnType().getKind())).toString())
          : ClassName.bestGuess(ex.getReturnType().toString());

      MethodSpec set = MethodSpec.methodBuilder(key)
          .addModifiers(Modifier.PUBLIC)
          .returns(ClassName.bestGuess(className))
          .addParameter(paramType, "x")
          .addStatement("mExtras.$L($S, x)", mBundlePutMap.get(boxedType.toString()), key)
          .addStatement("return this")
          .build();

      event.addMethod(set);
    }

    return event.build();
  }

  private static Map<String, String> mBundlePutMap = new HashMap<>();

  static {
    mBundlePutMap.put("java.lang.String", "putString");
    mBundlePutMap.put("java.lang.Boolean", "putBoolean");
    mBundlePutMap.put("java.lang.Byte", "putByte");
    mBundlePutMap.put("java.lang.Character", "putChar");
    mBundlePutMap.put("java.lang.Integer", "putInt");
    mBundlePutMap.put("java.lang.Short", "putShort");
    mBundlePutMap.put("java.lang.Float", "putFloat");
    mBundlePutMap.put("java.lang.Double", "putDouble");
    mBundlePutMap.put("java.lang.Long", "putLong");
  }
}
