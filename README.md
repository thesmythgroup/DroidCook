# gradle

```groovy
repositories {
    maven { url "http://dl.bintray.com/dskinner/maven" }
}

dependencies {
    compile 'com.codesmyth.droidcook:common:0.4.0'
    compile 'com.codesmyth.droidcook:api:0.4.0'
    annotationProcessor 'com.codesmyth.droidcook:compiler:0.4.0'
}
```

# build

```bash
# for use with mavenLocal()
./gradlew clean assemble publishToMavenLocal

# jcenter publish, requires activating upload
# afterwards at https://bintray.com
./gradlew clean assemble bintrayUpload
```