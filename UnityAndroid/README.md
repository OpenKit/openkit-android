Building the Unity Plugin
=======

```
Grabbed classes.jar from Unity version 4.0.1f2, at:
/Applications/Unity/Unity.app/Contents/PlaybackEngines/AndroidPlayer/bin/classes.jar

Loose notes on building the jar for Unity usage:
    Clear workspace
    File -> Import -> Generic project
    Select openkit-android/UnityAndroid
    Within Package Explorer, right click on UnityAndroid and select Properties > Java Build Path > Libraries
        Remove any libraries with absolute paths
        Add External Jars:
          openkitsdk.jar (from openkit-unity/UnityPlugin/Assets/Plugins/Android/)
        Make sure to check openkitsdk.jar in "Order and Export" tab

    Right click again in UnityAndroid > Android Tools > Fix project properties

    Right click again in Unity Android > Export > Java > Jar File.  Next.
    In the next screen, uncheck everything except for src.  Also uncheck all files in the right hand side.
    Export as unityandroid.jar
    Hit Next. Uncheck "Export class files with compile errors".
    Hit finish.

    Replace the Unity file at Assets/Plugins/Android/unityandroid.jar with the newly generated file.
```

