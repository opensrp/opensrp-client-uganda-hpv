package org.smartregister.ug.hpv.runner;

/**
 * Created by ndegwamartin on 17/05/2018.
 */


import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

/**
 * More dynamic path resolution.
 * <p>
 * This workaround is only for Mac Users necessary and only if they don't use the $MODULE_DIR$
 * workaround mentioned at http://robolectric.org/getting-started/.
 * <p>
 * Follow this issue at https://code.google.com/p/android/issues/detail?id=158015
 */
public class CustomRobolectricRunner extends RobolectricTestRunner {

    public CustomRobolectricRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    protected AndroidManifest getAppManifest(Config config) {
        AndroidManifest appManifest = super.getAppManifest(config);
        FsFile androidManifestFile = appManifest.getAndroidManifestFile();

        if (androidManifestFile.exists()) {
            return appManifest;
        } else {
         /*   String moduleRoot = getModuleRootPath(config);
            androidManifestFile = FileFsFile.from(moduleRoot, appManifest.getAndroidManifestFile().getPath().replace("bundles", "manifests/full"));
            FsFile resDirectory = FileFsFile.from(moduleRoot, appManifest.getResDirectory().getPath().replace("/res", "").replace("bundles", "res"));
            FsFile assetsDirectory = FileFsFile.from(moduleRoot, appManifest.getAssetsDirectory().getPath().replace("/assets", "").replace("bundles", "assets"));
            return new AndroidManifest(androidManifestFile, resDirectory, assetsDirectory);
            */
         return null;
        }
    }
}