# DroidCook

DroidCook is a bytecode manipulation library for injecting android boilerplate code. How it works:

* Annotate classes, methods, and fields.
* During post-compile of a build (before conversion to dex format), annotations will be read from compiled java classes and be used to inject boilerplate into the class file.

## Getting Started

Currently ANT only. Jars are not yet being provided.

### Quick

```bash
$ git clone git@github.com:thesmythgroup/DroidCook.git
$ cd DroidCook
$ ant -Dp=/home/user/workspace/projectroot install
```

Import the `cook_rules.xml` now located in your project root.

Test the project build:

```bash
$ cd $PROJECT
$ ant debug
```

Inspect the `-post-compile` output which should show class transformations that have occured.

### Not So Quick

```bash
$ git clone git@github.com:thesmythgroup/DroidCook.git
$ cd DroidCook
$ ant asm api
```

This builds two jars, `droidcook.jar` and `droidcook-api.jar`. The first is the bytecode transformer that should run during `-post-compile`. The second provides the annotations api and should be included in an android project like any other library.

Copy the assets to android project:

```bash
$ mkdir $PROJECT/compile-libs
$ cp bin/DroidCook.jar $PROJECT/compile-libs/
$ cp bin/DroidCook-api.jar $PROJECT/libs/
$ cp cook_rules.xml $PROJECT
```

Finally, update the project's `custom_rules.xml` to include `cook_rules.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="imported" default="help">
    <import file="cook_rules.xml" />
</project>
```

## Available Annotations

For now, have a look at:

https://github.com/thesmythgroup/DroidCook/blob/master/src/org/tsg/android/api/Annotations.java

## Differences from ...

Quick list of differences for similar projects.

### Roboguice

* Requires extending the library's classes
* Performs work during runtime via reflection

### AndroidAnnotations

* Generates raw source code before compiling that extends an app's current activities
* Requires one to reference the generated classes with an underscore suffix throughout the app and manifest