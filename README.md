# GUI Library for "Intro to Java" Courses

A library for creating simple GUIs in introduction-level programming courses.
The default backend uses Swing to display the GUI in a window, but the
library does not expose any Swing types or concepts directly; instead, all
functionality is exposed using a simple, single-threaded, and mostly
primitive-type-based API. In addition, there is an experimental backend that
exposes the GUI as a web page using an HTML5 canvas.

The base API uses a single object of type [Gui][3] and focuses on
drawing operations and global user inputs. An extended interface allows
adding "[Component][4]s", which are objects that draw
themselves on the GUI and can react to local user inputs using simple
callbacks.

More Info in the [API Documentation][5].


## Download

The library is hosted in a [public Maven repository on gitlab.fhnw.ch][6],
where you can check for the latest version and download the JAR file.

For Maven projects, add the following to your pom.xml file:

```xml
<dependencies>
    <dependency>
        <groupId>ch.trick17.gui</groupId>
        <artifactId>gui</artifactId>
        <version>2.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>https://gitlab.fhnw.ch/api/v4/projects/13172/packages/maven</url>
    </repository>
</repositories>
```

For Gradle projects:

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://gitlab.fhnw.ch/api/v4/projects/13172/packages/maven"
    }
}
dependencies {
    implementation 'ch.trick17.gui:gui:2.0.0-SNAPSHOT'
}
```


[1]: https://docs.oracle.com/javase/8/docs/api/javax/swing/JFrame.html?is-external=true
[2]: https://docs.oracle.com/javase/8/docs/api/javax/swing/JPanel.html?is-external=true
[3]: https://rolve.github.io/gui/apidocs/gui/Gui.html
[4]: https://rolve.github.io/gui/apidocs/gui/component/Component.html
[5]: https://rolve.github.io/gui/apidocs/overview-summary.html
[6]: https://gitlab.fhnw.ch/michael.faes/gui/-/packages
