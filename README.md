
# react-native-ilive

## Getting started

`$ npm install react-native-ilive --save`

### Mostly automatic installation

`$ react-native link react-native-ilive`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-ilive` and add `RNIlive.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNIlive.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNIlivePackage;` to the imports at the top of the file
  - Add `new RNIlivePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-ilive'
  	project(':react-native-ilive').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-ilive/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-ilive')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNIlive.sln` in `node_modules/react-native-ilive/windows/RNIlive.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Ilive.RNIlive;` to the usings at the top of the file
  - Add `new RNIlivePackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNIlive from 'react-native-ilive';

// TODO: What to do with the module?
RNIlive;
```
  