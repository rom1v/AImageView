# AImageView

An
[ImageView](http://developer.android.com/reference/android/widget/ImageView.html)
using an alternative to the
[ScaleType](http://developer.android.com/reference/android/widget/ImageView.ScaleType.html)
mechanism.


### Why?

The `ScaleType` enum constants do not cover all common cases, and the choice of
the right constant (if any) is not always intuitive.


### Principle

`AImageView` provides 4 parameters:

 * `xWeight` and `yWeight` (`float`s in [0;1]) indicate where to bind the image
   to the component;
 * `fit` indicates whether the image must fit `inside` the component (by adding
   margins), `outside` the component (by cropping), always `horizontal`ly or
   always `vertical`ly;
 * `scale` indicates whether the image can be `upscale`d and/or `downscale`d.

Currently, it always preserves [aspect
ratio](https://en.wikipedia.org/wiki/Aspect_ratio_%28image%29).


### Example

~~~xml
    <com.rom1v.aimageview.AImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="@drawable/myimage"
        app:xWeight="0.5"
        app:yWeight="0.5"
        app:fit="inside"
        app:scale="downscale|upscale" />
~~~

Here, the image will fit inside the component (margins will be added if
necessary), exactly (downscaling and upscaling are accepted) and will be
centered (0.5, 0.5).


### `ScaleType` constants equivalences

Actually, the `ScaleType` enum constants correspond to specific parameters
values. As you can notice, they do not cover all the combinations, and are not
always explicit…

#### `ScaleType.CENTER`

~~~
  app:xWeight="0.5"
  app:yWeight="0.5"
  app:scale="disabled"
  // app:fit has no meaning when scale is disabled
~~~

#### `ScaleType.CENTER_CROP`

~~~
  app:xWeight="0.5"
  app:yWeight="0.5"
  app:fit="outside"
  app:scale="downscale|upscale"
~~~

#### `ScaleType.CENTER_INSIDE`

~~~
  app:xWeight="0.5"
  app:yWeight="0.5"
  app:fit="inside"
  app:scale="downscale"
~~~

#### `ScaleType.FIT_CENTER`

~~~
  app:xWeight="0.5"
  app:yWeight="0.5"
  app:fit="inside"
  app:scale="downscale|upscale"
~~~

#### `ScaleType.FIT_END`

~~~
  app:xWeight="1"
  app:yWeight="1"
  app:fit="inside"
  app:scale="downscale|upscale"
~~~

#### `ScaleType.FIT_START`

~~~
  app:xWeight="0"
  app:yWeight="0"
  app:fit="inside"
  app:scale="downscale|upscale"
~~~

#### `ScaleType.FIT_XY`

This configuration cannot be reproduced using `AImageView` parameters, since
this component always preserve aspect ratio.

#### `ScaleType.MATRIX`

`AImageView` extends `ImageView` and force the `scaleType` to `ScaleType.MATRIX`
(to scale and move the image content). Therefore, there is no "equivalent",
`AImageView` is built upon it.


### Build

The library is built using [Gradle](https://en.wikipedia.org/wiki/Gradle).

You can build the `aar` project library using:

    ./gradlew assembleDebug

It will be generated to `library/build/outputs/aar/aimageview.aar`.

You can use then use it in your projects.

First, copy `aimageview.aar` to `libs/` of your app project.

Then add a `flatDir` declaration in your repositories list of your root
`build.gradle`:

~~~
allprojects {
    repositories {
        mavenCentral()
        flatDir {
            dirs 'libs'
        }
    }
}
~~~

Finally, declare it in your app dependencies:

~~~
dependencies {
    compile(name: 'aimageview', ext: 'aar')
}
~~~


### Sample application

A sample application using it is (as a submodule) is also available:

    git clone --recursive http://git.rom1v.com/AImageViewSample.git
    cd AImageViewSample
    ./gradlew assembleDebug  # for example

([github](https://github.com/rom1v/AImageViewSample.git) mirror)


### License

This library is published under the terms of the
[GNU/LGPLv3](https://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License).
