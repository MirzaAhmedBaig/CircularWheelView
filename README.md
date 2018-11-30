# CircularWheelView
Circular Wheel type list that can be used as item picker of string items.
 
 [ ![Download](https://api.bintray.com/packages/mirzaahmedbaig/CircularWheelView/circularwheelview/images/download.svg) ](https://bintray.com/mirzaahmedbaig/CircularWheelView/circularwheelview/_latestVersion)
 [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-CircularWheelView-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/7174)
 
![Alt Text](https://github.com/MirzaAhmedBaig/CircularWheelView/blob/master/demo1.gif) 
![Alt Text](https://github.com/MirzaAhmedBaig/CircularWheelView/blob/master/demo2.gif)

### Requirements
Kotlin, Android version >= 17

## Adding to Project


#### Add dependencies in gradle

```groovy
 repositories {
  jcenter()
 }
 
 //dependency
 implementation 'org.mab.wheelview:wheelview:1.1.0'
 
```

#### Simple usage

```xml
<?xml version="1.0" encoding="utf-8"?>
<org.mab.wheelpicker.CircularWheelView
        android:id="@+id/circularWheelPicker_one"
        android:layout_width="300dp"
        android:layout_height="400dp"
        app:wheel_background_color="#00ffff"
        app:wheel_item_selected_text_color="#0000ff"
        app:wheel_item_text_color="#ff00ff" />
```


