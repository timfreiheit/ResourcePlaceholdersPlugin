ResourcesPlaceholders
======

Gradle plugin which adds support for ${placeholder} in Android resource files   

Installation
------------

Add the following to your `build.gradle`:

```gradle

buildscript {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.timfreiheit:ResourcesPlaceholdersPlugin:X.X.X'
    }
}

apply plugin: 'com.android.application'

// Make sure to apply this plugin *after* the Android plugin
apply plugin: 'de.timfreiheit.resourceplaceholders.plugin'

```

Usage
------------

```gradle

resourcePlaceholders {
    files = ['xml/shortcuts.xml']
}

```

Every file in which the placeholders should be supported must be listed.
This improved incremental builds and unnecessary work.

