# Add any ProGuard configurations specific to this
# extension here.

-keep public class com.sumit.deeplink.DeepLink {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'com/sumit/deeplink/repack'
-flattenpackagehierarchy
-dontpreverify

-keep class com.sumit.deeplink.DeepLink$DeepLinkActivity
