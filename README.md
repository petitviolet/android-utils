# Android Util Library

This repo includes these following utilitiy classes.

- Logger
    - Log with more detail class, method and line number
- KeyboardUtil
    - show/hide keyboard 
- ReflectionUtil
    - getter/setter through reflection 
- ToastUtil
    - short cut to show Toast
- AddOnScrollListenerUtil
    - not `set` but `add` AbsListView.OnScrollListener
- MainThreadCallback
    - for OkHttp Callback executed on MainThread

# How to Use

You can use this library by just add following settings at your application build.gradle file.

```groovy
repositories {
    maven { url "https://petitviolet.github.io/maven/repository" }
}
compile 'net.petitviolet.library:android-utils:0.1.1'
```

# License

[MIT License](http://petitviolet.mit-license.org/)
