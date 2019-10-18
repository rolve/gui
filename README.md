# GUI Library for "Intro to Java" Courses

Classes for creating simple GUIs in introduction-level programming courses. The classes in this library use Swing classes ([JFrame](https://docs.oracle.com/javase/8/docs/api/javax/swing/JFrame.html?is-external=true), [JPanel](https://docs.oracle.com/javase/8/docs/api/javax/swing/JPanel.html?is-external=true), etc.) but do not expose any Swing type or concepts directly. Instead, all functionality is exposed using a simple, single-threaded, and mostly primitive-type-based interface.

The base interface uses a single object of type [Window](https://rolve.github.io/gui/apidocs/gui/Window.html) and focuses on drawing operations and global user inputs. An extended interface allows adding "[Component](https://rolve.github.io/gui/apidocs/gui/component/Component.html)s", which are objects that draw themselves on the window and can react to local user inputs using simple callbacks.

More Info in the [API Documentation](https://rolve.github.io/gui/apidocs/overview-summary.html).
