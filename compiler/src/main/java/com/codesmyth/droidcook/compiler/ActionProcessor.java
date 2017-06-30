package com.codesmyth.droidcook.compiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.codesmyth.droidcook.api.Action;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

@AutoService(Processor.class)
public class ActionProcessor extends AbstractProcessor {
  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager log;

  private HashMap<TypeElement, Set<ExecutableElement>> execMap;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    log = processingEnv.getMessager();

    execMap = new HashMap<>();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> set = new HashSet<>();
    set.add(Action.class.getCanonicalName());
    return set;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    Set<? extends Element> els = roundEnvironment.getElementsAnnotatedWith(Action.class);
    if (els.size() == 0) {
      return true;
    }

    for (Element el : els) {
      if (el.getKind() != ElementKind.METHOD) {
        // TODO
        printMessage(Diagnostic.Kind.WARNING, "Only methods are currently supported by @Action", el);
        continue;
      }

      TypeElement parent = (TypeElement) el.getEnclosingElement();

      Set<ExecutableElement> methods;
      if (execMap.containsKey(parent)) {
        methods = execMap.get(parent);
      } else {
        methods = new HashSet<>();
        execMap.put(parent, methods);
      }

      methods.add((ExecutableElement) el);
    }

    try {
      JavaFile.builder("com.codesmyth.droidcook", packedReceiverSpec()).build().writeTo(filer);
    } catch (IOException e) {
      e.printStackTrace();
      printMessage(Diagnostic.Kind.WARNING, e.getMessage());
    }

    for (TypeElement el : execMap.keySet()) {
      try {
        String pkg = elementUtils.getPackageOf(el).toString();
        TypeSpec spec = receiverSpec(el);
        JavaFile.builder(pkg, spec).build().writeTo(filer);
      } catch (ProcessorException|IOException e) {
        e.printStackTrace();
        printMessage(Diagnostic.Kind.WARNING, e.getMessage());
      }
    }

    return true;
  }

  private TypeSpec receiverSpec(TypeElement el) throws ProcessorException {
    ClassName parentName = ClassName.bestGuess(el.toString());
    ClassName className = ClassName.bestGuess(el.getSimpleName() + "Receiver");

    return TypeSpec.classBuilder(className.simpleName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .superclass(ClassName.get("com.codesmyth.droidcook", "PackedReceiver"))
        .addField(parentName, "x", Modifier.PRIVATE, Modifier.FINAL)
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(parentName, "x")
            .addStatement("this.x = x")
            .build())
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
        .build();
  }

  private CodeBlock switchFor(TypeElement el) throws ProcessorException {
    CodeBlock.Builder code = CodeBlock.builder();
    code.beginControlFlow("switch (intent.getAction())");

    for (ExecutableElement method : execMap.get(el)) {
      List<? extends VariableElement> params = method.getParameters();
      if (params.size() != 1) {
        printMessage(Kind.WARNING, "@Action method should only have 1 param", method);
        continue;
      }

      TypeMirror param = params.get(0).asType();
      Element paramElem = typeUtils.asElement(param);
      String paramName = paramElem.getSimpleName().toString();
      PackageElement pkg = elementUtils.getPackageOf(paramElem);

      String pkgName = pkg.getQualifiedName().toString();
      if ("".equals(pkgName)) {
        printMessage(Kind.WARNING, "Unable to resolve package name for param: " + paramName, paramElem);
        continue;
      }

      code.add("case $S:\n", param.toString());
      code.indent();
      code.addStatement("x.$L(new $T(intent.getExtras()))", method.getSimpleName(), ClassName.get(pkgName, paramName + "Bundler"));
      code.addStatement("break");
      code.unindent();
    }

    code.endControlFlow();
    return code.build();
  }

  private CodeBlock filterFor(TypeElement el) throws ProcessorException {
    CodeBlock.Builder code = CodeBlock.builder()
        .addStatement("$T filter = new $T()", IntentFilter.class, IntentFilter.class);

    for (ExecutableElement method : execMap.get(el)) {
      List<? extends VariableElement> params = method.getParameters();
      if (params.size() != 1) {
        printMessage(Diagnostic.Kind.ERROR, "@Action method should only have 1 param", method);
        throw new ProcessorException("@Action method should only have 1 param");
      }
      code.addStatement("filter.addAction($S)", params.get(0).asType().toString());
    }

    code.addStatement("return filter");
    return code.build();
  }

  private TypeSpec packedReceiverSpec() {
    return TypeSpec.classBuilder("PackedReceiver")
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .superclass(BroadcastReceiver.class)
        .addMethod(MethodSpec.methodBuilder("filter")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(IntentFilter.class)
            .build())
        .addMethod(MethodSpec.methodBuilder("registerLocal")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("$T.getInstance(context).registerReceiver(this, filter())",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build())
        .addMethod(MethodSpec.methodBuilder("unregisterLocal")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("$T.getInstance(context).unregisterReceiver(this)",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build())
        .addMethod(MethodSpec.methodBuilder("registerBroad")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("context.registerReceiver(this, filter())")
            .build())
        .addMethod(MethodSpec.methodBuilder("registerBroad")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addParameter(String.class, "broadcastPermission")
            .addParameter(Handler.class, "handler")
            .addStatement("context.registerReceiver(this, filter(), broadcastPermission, handler)")
            .build())
        .addMethod(MethodSpec.methodBuilder("unregisterBroad")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("context.unregisterReceiver(this)")
            .build())
        .build();
  }

  void printMessage(Diagnostic.Kind kind, String message) {
    log.printMessage(kind, "DroidCook ActionProcessor: " + message);
  }

  void printMessage(Diagnostic.Kind kind, String message, Element el) {
    log.printMessage(kind, "DroidCook ActionProcessor: " + message, el);
  }
}
