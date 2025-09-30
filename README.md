# GUI Library for "Intro to Java" Courses

A library for creating simple GUIs in introduction-level programming courses.
The default backend uses Swing to display the GUI in a window, but the
library does not expose any Swing types or concepts directly; instead, all
functionality is exposed using a simple, single-threaded, and mostly
primitive-type-based API. In addition, there is an experimental backend that
exposes the GUI as a web page using an HTML5 canvas.

The base API uses a single object of type [Gui][1] and focuses on
drawing operations and global user inputs. An extended interface allows
adding "[Component][2]s", which are objects that draw
themselves on the GUI and can react to local user inputs using simple
callbacks.

More Info in the [API Documentation][3].


## Download

The library is hosted in a [public Maven repository on gitlab.fhnw.ch][4],
where you can check for the latest version and download the JAR file.

For Maven projects, add the following to your pom.xml file:

```xml
<dependencies>
    <dependency>
        <groupId>ch.trick17.gui</groupId>
        <artifactId>gui</artifactId>
        <version>2.6.0-SNAPSHOT</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>https://gitlab.fhnw.ch/api/v4/projects/17730/packages/maven</url>
    </repository>
</repositories>
```

For Gradle projects:

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://gitlab.fhnw.ch/api/v4/projects/17730/packages/maven"
    }
}
dependencies {
    implementation 'ch.trick17.gui:gui:2.6.0-SNAPSHOT'
}
```

[1]: https://gui.pages.fhnw.ch/gui/apidocs/ch/trick17/gui/Gui.html
[2]: https://gui.pages.fhnw.ch/gui/apidocs/ch/trick17/gui/component/Component.html
[3]: https://gui.pages.fhnw.ch/gui/apidocs/
[4]: https://gitlab.fhnw.ch/gui/gui/-/packages
