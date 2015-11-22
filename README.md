# dex-method-counts

Forked from [this repo](https://github.com/mihaip/dex-method-counts).
 
Simple tool to output per-package method counts in an Android DEX executable grouped by package, to aid in getting under the 65,536 referenced method limit.
More details are [in this blog post](http://blog.persistent.info/2014/05/per-package-method-counts-for-androids.html).
The DEX file parsing is based on the `dexdeps` tool from [the Android source tree](https://android.googlesource.com/platform/dalvik.git/+/master/tools/dexdeps/).

You can just [download executable jar](https://github.com/Airfreshener/dex-method-counts-UI/raw/master/content/DexMethodCounts.jar).

Support drag and drop for more usability.

![alt Drag&Drop](https://github.com/Airfreshener/dex-method-counts-UI/raw/master/content/screenshot1.png)

You can open multiple APK files at same time:

![alt Multiple files](https://github.com/Airfreshener/dex-method-counts-UI/raw/master/content/screenshot2.png)

Also you can save dex tree in current tab to text file as in original tool.