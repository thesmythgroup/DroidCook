package com.codesmyth.droidcook.compiler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.codesmyth.droidcook.api.Receive;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
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
public class ReceiveProcessor extends AbstractProcessor {
  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager msg;

  private TypeSpec.Builder receiverFactory;

  private HashMap<TypeElement, Set<ExecutableElement>> execMap;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    msg = processingEnv.getMessager();

    receiverFactory = TypeSpec.classBuilder("ReceiverFactory").addModifiers(Modifier.PUBLIC);
    execMap = new HashMap<>();
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

    for (Element el : els) {
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

    for (TypeElement el : execMap.keySet()) {
      try {
        receiverFactory.addMethod(addFactoryMethod(el));
      } catch (ProcessorException e) {
        e.printStackTrace();
        msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
      }
    }

    try {
      JavaFile.builder("com.codesmyth.droidcook", makePackedReceiver())
          .build()
          .writeTo(filer);
    } catch (IOException e) {
      e.printStackTrace();
      msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
    }
    try {
      JavaFile.builder("com.codesmyth.droidcook", receiverFactory.build())
          .build()
          .writeTo(filer);
    } catch (IOException e) {
      e.printStackTrace();
      msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
    }
    return true;
  }

  private MethodSpec addFactoryMethod(TypeElement el) throws ProcessorException {
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

  private CodeBlock switchFor(TypeElement el) throws ProcessorException {
    CodeBlock.Builder code = CodeBlock.builder();
    code.beginControlFlow("switch (intent.getAction())");
    for (ExecutableElement method : execMap.get(el)) {

      List<? extends VariableElement> params = method.getParameters();
      if (params.size() != 1) {
        msg.printMessage(Kind.WARNING, "@Receive method should only have 1 param", method);
        continue;
      }

      TypeMirror param = params.get(0).asType();
      Element paramElem = typeUtils.asElement(param);
      String paramName = paramElem.getSimpleName().toString();
      PackageElement pkg = elementUtils.getPackageOf(paramElem);
      String pkgName = pkg.getQualifiedName().toString();
      if ("".equals(pkgName)) {
        msg.printMessage(Kind.WARNING, "Unable to resolve package name for param: " + paramName,
            paramElem);
        continue;
      }

      code.add("case $S:\n", param.toString());
      code.indent();
      code.addStatement("x.$L(new $T(intent.getExtras()))",
          method.getSimpleName(),
          ClassName.get(pkgName, "Event" + paramName)
      );
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
        msg.printMessage(Diagnostic.Kind.ERROR, "@Receive method should only have 1 param", method);
        throw new ProcessorException("@Receive method should only have 1 param");
      }
      code.addStatement("filter.addAction($S)", params.get(0).asType().toString());
    }

    code.addStatement("return filter");
    return code.build();
  }

  private TypeSpec makePackedReceiver() {
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
            .addStatement("$T.getInstance(context).registerReceiver(this, filter())",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build())
        .addMethod(MethodSpec.methodBuilder("unregister")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addParameter(Context.class, "context")
            .addStatement("$T.getInstance(context).unregisterReceiver(this)",
                ClassName.get("android.support.v4.content", "LocalBroadcastManager"))
            .build())
        .build();
  }
}
