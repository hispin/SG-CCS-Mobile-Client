# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# keep the line numbers for the stack traces of the crashes
-keepattributes SourceFile,LineNumberTable

# the classes that are stored as json (gson uses the names of the fields)
-keep class com.sensoguard.ccsmobileclient.classes.Alarm { *; }
-keep class com.sensoguard.ccsmobileclient.classes.SystemSort { *; }

# javax mail (sending the alarms by email) is loaded by reflection and by its mailcap files
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-keep class com.sun.mail.** { *; }
-keep class myjava.awt.datatransfer.** { *; }
-keep class org.apache.harmony.awt.** { *; }
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn javax.security.**

# keep the edge to edge setup of androidx (do not inline it into the activities of the app),
# it calls the deprecated colors apis only for backward compatibility
-keep class androidx.core.view.WindowCompat { public static void enableEdgeToEdge(android.view.Window); }
