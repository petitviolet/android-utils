# Android Util Library

![jitpack](https://img.shields.io/github/tag/petitviolet/android-utils.svg?label=JitPack)

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
- LruCache
    - Memory and Disk LruCache implementation

# How to Use

You can use this library by just add following settings at your application build.gradle file.

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
compile 'com.github.petitviolet:android-utils:0.1.3'
```

# License

[MIT License](http://petitviolet.mit-license.org/)
