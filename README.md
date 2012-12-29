# CookAndroid

CookAndroid is a bytecode manipulation library for injecting android boilerplate code. How it works:

* Annotate classes, methods, and fields.
* During post-compile of a build (before conversion to dex format), annotations will be read from compiled java classes and be used to inject boilerplate into the class file.

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