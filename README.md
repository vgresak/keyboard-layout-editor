# Keyboard layout editor
Keyboard layout editor is a JavaFX application made to help create custom keyboard layouts. 
The application works with the <code>X&nbsp;Keyboard&nbsp;Extension&nbsp;(XKB)</code> (often found in the Linux, OpenSolaris, BSD). 

## Requirements
As this is a JavaFX application, you need both Java 8 and JavaFX to build and run the editor. If you are using APT, you can use <code>sudo apt-get install openjdk-8-jdk openjfx</code>.

Optionally you can install [xkblayout-state](https://github.com/nonpop/xkblayout-state) on your system. If you won't, the editor will run just fine but the currently selected group will not be automatically displayed by the editor (you can still choose desired group via the menu in the editor).

## Creating a runnable JAR file
To build the application and to create an executable JAR file from sources run <code>/bin/bash ./gradlew fatJar</code> in the project directory (you can also run <code>gradle fatJar</code> if you have [gradle](https://gradle.org/) installed).
Gradle task <code>fatJar</code> will create single JAR file that contains keyboard layout editor and all third-party libraries.
Output file is located at <code>build/libs/keyboard-layout-editor-all-1.0-SNAPSHOT.jar</code>.

## Running
You can run the application using the <code>java -jar keyboard-layout-editor-all-1.0-SNAPSHOT.jar</code> command.

## Features
* Current keyboard layout is automatically imported from the X Server.
* Symbol file can be exported and used by XKB to change the keyboard mapping.
* There is a support for up to 4 characters (levels) per key.
* It is possible to set key type (xkb_type).
* Generated characters are defined using Unicode (UXXXX and 0x100XXXX formats are supported), character map or using [keysym](https://www.cl.cam.ac.uk/~mgk25/ucs/keysymdef.h).
* There are a lot of characters in Unicode and this application aims to be able to display them all correctly. Choose which font you want to be used in the virtual keyboard. When font does not support displayed character, application tries to detect it and select an alternative.
* You can choose how the model of the keyboard is displayed. Select desired appearance of the keyboard or create and use your own.
* Editor is not dependent on any specific GUI environment. It does not matter whether you use KDE, GNOME, Xfce or anything else.
* Application can be used on any platform that supports Java 8 and JavaFX. If you are not using XKB, you can still work with the application and generate a symbol file (although you will not be able to use the file itself to change your keyboard mapping).
