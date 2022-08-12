package mmat.tests.mma;

import mma.annotations.ModAnnotations.*;
import mmat.tests.*;

@MainClass(TestMod.class)
@DependenciesAnnotation()
@ModAssetsAnnotation
@AnnotationSettings(
rootPackage = "mmat.tests",
assetsPath = "tests/assets",
assetsRawPath = "tests/assets",
modInfoPath = "tests/assets/mod.hjson",
classPrefix = "Tm"
)
class AnnotationProcessorSettings {
}
