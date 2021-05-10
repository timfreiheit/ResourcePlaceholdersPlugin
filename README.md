ResourcesPlaceholders
======

Gradle plugin which adds support for ${placeholder} [manifestPlaceholders](https://developer.android.com/studio/build/manifest-build-variables.html) in Android resource files   

Installation
------------

Add the following to your `build.gradle`:

Build script snippet for plugins DSL:
```gradle
plugins {
  id "de.timfreiheit.resourceplaceholders" version "X.X.X"
}
```

Build script snippet for use where dynamic configuration is required:
```gradle

buildscript {
    repositories {
        // ...
        maven {
          url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "de.timfreiheit.resourceplaceholders:placeholders:X.X.X"
    }
}

apply plugin: 'com.android.application'
apply plugin: "de.timfreiheit.resourceplaceholders"

```

Usage
------------

A common use case is using the ``` ${applicationId} ``` when defining [App Shortcuts](https://developer.android.com/preview/shortcuts.html).
The **android:targetPackage** must be set statically and can not easily be used with different build variants or types.  
Using placeholders the ``` shortcuts.xml ``` file could look something like:

```xml

<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <shortcut ...>
        <intent
            android:action="android.intent.action.VIEW"
            android:targetClass="com.test.MainActivity"
            android:targetPackage="${applicationId}"/>
    </shortcut>
</shortcuts>

```

Register the file in your apps ``` build.gradle ``` to the plugin:

```gradle

resourcePlaceholders {
    files = ['xml/shortcuts.xml']
}

```

Every file in which the placeholders should be supported must be listed.
This improves incremental builds and avoid unnecessary work.
