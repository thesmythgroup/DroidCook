# CookAndroid

CookAndroid is a bytecode manipulation library for injecting android boilerplate code. How it works:

* Annotate classes, methods, and fields.
* During post-compile of a build (before conversion to dex format), annotations will be read from compiled java classes and be used to inject boilerplate into the class file.

## Getting Started

Currently ANT only. Jars are not yet being provided.

### Quick

```bash
$ git clone git@github.com:thesmythgroup/CookAndroid.git
$ cd CookAndroid
$ ant -Dproject=/home/user/workspace/projectroot install
```

Update android project's `custom_rules.xml` to include `cook_rules.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="imported" default="help">
    <import file="cook_rules.xml" />
</project>
```

Test the project build:

```bash
$ cd $PROJECT
$ ant debug
```

Inspect the `-post-compile` output which should show class transformations that have occured.

### Not So Quick

```bash
$ git clone git@github.com:thesmythgroup/CookAndroid.git
$ cd CookAndroid
$ ant asm api
```

This builds two jars, `cookandroid.jar` and `cookandroid-api.jar`. The first is the bytecode transformer that should run during post-compile. The second provides the annotations api and should be included in an android project like any other library.

Copy the assets to android project:

```bash
$ mkdir $PROJECT/compile-libs
$ cp bin/cookandroid.jar $PROJECT/compile-libs/
$ cp bin/cookandroid-api.jar $PROJECT/libs/
$ cp cook_rules.xml $PROJECT
```

Finally, update the project's `custom_rules.xml` to include `cook_rules.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project name="imported" default="help">
    <import file="cook_rules.xml" />
</project>
```

## API

For now, have a look at:

https://github.com/thesmythgroup/CookAndroid/blob/master/src/org/tsg/android/api/Annotations.java

## Differences from ...

Quick list of differences (and gripes) for similar projects.

### Roboguice

* Requires extending the library's classes to do anything useful, painful
* Performs work during runtime via reflection
* Obscure errors and stack traces due to the previous two statements

### AndroidAnnotations

* Generates raw source code before compiling that extends an app's current activities
* Requires one to reference these new "invisible" generated classes throughout the app and manifest
* Setup is tedious (but getting better at the time of this writing)