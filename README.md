# GUI Library for "Intro to Java" Courses

Classes for creating simple GUIs in introduction-level programming courses.
The classes in this library use Swing classes ([JFrame][1], [JPanel][2], etc.) 
but do not expose any Swing type or concepts directly. Instead, all 
functionality is exposed using a simple, single-threaded, and mostly 
primitive-type-based interface.

The base interface uses a single object of type [Window][3] and focuses on 
drawing operations and global user inputs. An extended interface allows 
adding "[Component][4]s", which are objects that draw themselves on the window 
and can react to local user inputs using simple callbacks.

More Info in the [API Documentation][5].


## Download

The library is hosted in a [public Maven repository on GitLab.com][6], where 
you can check for the latest version and download the JAR file.

For Maven projects, add the following to your pom.xml file:

```xml
<dependencies>
    <dependency>
        <groupId>ch.trick17.gui</groupId>
        <artifactId>gui</artifactId>
        <version>1.4.1-SNAPSHOT</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>gitlab-maven</id>
        <url>https://gitlab.com/api/v4/projects/30441174/packages/maven</url>
    </repository>
</repositories>
```

For Gradle projects:

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://gitlab.com/api/v4/projects/30441174/packages/maven"
    }
}
dependencies {
    implementation 'ch.trick17.gui:gui:1.4.1-SNAPSHOT'
}
```


[1]: https://docs.oracle.com/javase/8/docs/api/javax/swing/JFrame.html?is-external=true
[2]: https://docs.oracle.com/javase/8/docs/api/javax/swing/JPanel.html?is-external=true
[3]: https://rolve.github.io/gui/apidocs/gui/Window.html
[4]: https://rolve.github.io/gui/apidocs/gui/component/Component.html
[5]: https://rolve.github.io/gui/apidocs/overview-summary.html
[6]: https://gitlab.com/rolve/gui/-/packages
