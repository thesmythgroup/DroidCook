package com.codesmyth.droidcook.compiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.codesmyth.droidcook.api.Receive;
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
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class ReceiveProcessor extends AbstractProcessor {
  private Types    mTypeUtils;
  private Elements mElementUtils;
  private Filer    mFiler;
  private Messager mMessager;

  private TypeSpec.Builder mFactory;

  private HashMap<TypeElement, Set<ExecutableElement>> mMap;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    mTypeUtils = processingEnv.getTypeUtils();
    mElementUtils = processingEnv.getElementUtils();
    mFiler = processingEnv.getFiler();
    mMessager = processingEnv.getMessager();

    mFactory = TypeSpec.classBuilder("ReceiverFactory").addModifiers(Modifier.PUBLIC);
    mMap = new HashMap<>();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>();
    set.add(Receive.class.getCanonicalName());
    return set;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

    Set<? extends Element> els = roundEnvironment.getElementsAnnotatedWith(Receive.class);
    if (els.size() == 0) {
      return true;
    }

    try {

      for (Element el : els) {
        TypeElement parent = (TypeElement) el.getEnclosingElement();

        Set<ExecutableElement> methods;
        if (mMap.containsKey(parent)) {
          methods = mMap.get(parent);
        } else {
          methods = new HashSet<>();
          mMap.put(parent, methods);
        }

        methods.add((ExecutableElement) el);
      }

      for (TypeElement el : mMap.keySet()) {
        mFactory.addMethod(addFactoryMethod(el));
      }

      try {
        JavaFile.builder("com.codesmyth.droidcook", makePackedReceiver())
            .build()
            .writeTo(mFiler);
        JavaFile.builder("com.codesmyth.droidcook", mFactory.build())
            .build()
            .writeTo(mFiler);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return true;
    } catch (ProcessorException e) {
      return true;
    }
  }

  TypeSpec makePackedReceiver() {
    return TypeSpec.classBuilder("PackedReceiver")
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .superclass(BroadcastReceiver.class)
        .addMethod(MethodSpec.methodBuilder("filter")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(IntentFilter.class)
            .build())
        .addMethod(MethodSpec.methodBuilder("register")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("$T.getInstance(context).registerReceiver(this, filter())", ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build())
        .addMethod(MethodSpec.methodBuilder("unregister")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("$T.getInstance(context).unregisterReceiver(this)", ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build())
        .build();
  }

  MethodSpec addFactoryMethod(TypeElement el) throws ProcessorException {
    return MethodSpec.methodBuilder("makeFor")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(ClassName.get("com.codesmyth.droidcook", "PackedReceiver"))
        .addParameter(ParameterSpec.builder(ClassName.bestGuess(el.toString()), "x")
            .addModifiers(Modifier.FINAL)
            .build())
        .addStatement("return $L", TypeSpec.anonymousClassBuilder("")
            .superclass(ClassName.get("com.codesmyth.droidcook", "PackedReceiver"))
            .addMethod(MethodSpec.methodBuilder("onReceive")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(Context.class, "context")
                .addParameter(Intent.class, "intent")
                .addCode(switchFor(el))
                .build())
            .addMethod(MethodSpec.methodBuilder("filter")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(IntentFilter.class)
                .addCode(filterFor(el))
                .build())
            .build())
        .build();
  }

  CodeBlock switchFor(TypeElement el) throws ProcessorException {
    CodeBlock.Builder code = CodeBlock.builder();
    code.beginControlFlow("switch (intent.getAction())");
    for (ExecutableElement method : mMap.get(el)) {
      List<? extends VariableElement> params = method.getParameters();
      if (params.size() != 1) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, "@Receive method should only have 1 param", method);
        throw new ProcessorException("@Receive method should only have 1 param");
      }
      Element param = mTypeUtils.asElement(params.get(0).asType());
      code.add("case $S:\n", param.toString());
      code.indent();
      code.addStatement("x.$L(new $T(intent.getExtras()))", method.getSimpleName().toString(), ClassName.get(mElementUtils.getPackageOf(param).toString(), "Event" + param.getSimpleName().toString()));
      code.addStatement("break");
      code.unindent();
    }
    code.endControlFlow();
    return code.build();
  }

  CodeBlock filterFor(TypeElement el) throws ProcessorException {
    CodeBlock.Builder code = CodeBlock.builder()
        .addStatement("$T filter = new $T()", IntentFilter.class, IntentFilter.class);

    for (ExecutableElement method : mMap.get(el)) {
      List<? extends VariableElement> params = method.getParameters();
      if (params.size() != 1) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, "@Receive method should only have 1 param", method);
        throw new ProcessorException("@Receive method should only have 1 param");
      }
      code.addStatement("filter.addAction($S)", params.get(0).asType().toString());
    }

    code.addStatement("return filter");
    return code.build();
  }
}
