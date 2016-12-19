## react-native-video [![Build Status](https://travis-ci.org/drivetribe/react-native-video.svg?branch=drivetribe)](https://travis-ci.org/drivetribe/react-native-video)

A `<Video>` component for react-native, based on [react-native-video](https://github.com/react-native-community/react-native-video).

Differences:
- Uses [ExoPlayer v2](https://github.com/google/ExoPlayer) on Android
- Removed unnecessary features
- Focused video player, additional features should be configured or plugged in

Requires react-native >= 0.38.0

### Add it to your project

Run `npm i --save @drivetribe/react-native-video`

#### iOS

Run `react-native link` to link the react-native-video library.

If you would like to allow other apps to play music over your video component, add:

**AppDelegate.m**

```objective-c
#import <AVFoundation/AVFoundation.h>  // import

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  ...
  [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryAmbient error:nil];  // allow
  ...
}
```

#### Android

Run `react-native link` to link the react-native-video library.

Or if you have trouble, make the following additions to the given files manually:

**android/settings.gradle**

```
include ':react-native-video'
project(':react-native-video').projectDir = new File(rootProject.projectDir, '../node_modules/@drivetribe/react-native-video/android-exoplayer')
```

**android/app/build.gradle**

```
dependencies {
   ...
   compile project(':react-native-video')
}
```

**MainApplication.java** (react-native >= 0.29.0)

On top, where imports are:

```java
import com.brentvatne.react.ReactVideoPackage;
```

Under `.addPackage(new MainReactPackage())`:

```java
.addPackage(new ReactVideoPackage())
```

## Usage

```javascript
// Within your render function, assuming you have a file called
// "background.mp4" in your project. You can include multiple videos
// on a single screen if you like.

<Video
  source={{uri: "background"}}   // Can be a URL or a local file.
  ref={(ref) => {this.player = ref}} // Store reference
  rate={1.0}                     // 0 is paused, 1 is normal.
  volume={1.0}                   // 0 is muted, 1 is normal.
  muted={false}                  // Mutes the audio entirely.
  paused={false}                 // Pauses playback entirely.
  resizeMode="cover"             // Fill the whole screen at aspect ratio.
  repeat={true}                  // Repeat forever.
  playInBackground={false}       // Audio continues to play when app entering background.
  playWhenInactive={false}       // [iOS] Video continues to play when control or notification center are shown.
  progressUpdateInterval={250.0} // [iOS] Interval to fire onProgress (default to ~250ms)
  onLoadStart={this.loadStart}   // Callback when video starts to load
  onLoad={this.setDuration}      // Callback when video loads
  onProgress={this.setTime}      // Callback every ~250ms with currentTime
  onEnd={this.onEnd}             // Callback when playback finishes
  onError={this.videoError}      // Callback when video cannot be loaded
  style={styles.backgroundVideo}
/>

// Later to trigger fullscreen
this.player.presentFullscreenPlayer()

// To set video position in seconds (seek)
this.player.seek(0)

// Later on in your styles..
const styles = StyleSheet.create({
  backgroundVideo: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
  },
});
```


## Android ExoPlayer Usage

```javascript
<Video
   source={{ uri: "background" }}
   rate={1.0}                   // 0 is paused, 1 is normal.
   volume={1.0}                 // 0 is muted, 1 is normal.
   muted={false}                // Mutes the audio entirely.
   paused={false}               // Pauses playback entirely.
   resizeMode="cover"           // Fill the whole screen at aspect ratio.
   repeat={true}                // Repeat forever.
   onLoadStart={this.loadStart} // Callback when video starts to load
   onLoad={this.setDuration}    // Callback when video loads
   onProgress={this.setTime}    // Callback every ~250ms with currentTime
   onEnd={this.onEnd}           // Callback when playback finishes
   onError={this.videoError}    // Callback when video cannot be loaded
   onAudio={this.videoError}    // Callback when video cannot be loaded
   disableFocus={true}          // disables audio focus and wake lock (default false)
   onAudioBecomingNoisy={this.onAudioBecomingNoisy} // Callback when audio is becoming noisy - should pause video
   onAudioFocusChanged={this.onAudioFocusChanged} // Callback when audio focus has been lost - another app stole focus pause if lost
   style={styles.backgroundVideo}
/>

// Later on in your styles..
const styles = Stylesheet.create({
  backgroundVideo: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
  },
});
```

### Play in background on iOS

To enable audio to play in background on iOS the audio session needs to be set to `AVAudioSessionCategoryPlayback`. See [Apple documentation][1] for additional details. (NOTE: there is now a ticket to [expose this as a prop]( https://github.com/react-native-community/react-native-video/issues/310) )

## Static Methods

`seek(seconds)`

Seeks the video to the specified time (in seconds). Access using a ref to the component

`presentFullscreenPlayer()`

Toggles a fullscreen player. Access using a ref to the component.

## Examples

- Try the included VideoPlayer example yourself:

   ```sh
   git clone git@github.com:drivetribe/react-native-video.git
   cd react-native-video/Examples/VideoPlayer
   npm install
   react-native run-android
   react-native run-ios

   ```

   Then `Cmd+R` to start the React Packager, build and run the project in the simulator.

## TODOS
- [ ] Callback to get buffering progress for remote videos
- [ ] Add support for captions
- [ ] Add support for playing multiple videos in a sequence (will interfere with current `repeat` implementation)

[1]: https://developer.apple.com/library/ios/qa/qa1668/_index.html

---

**MIT Licensed**
