package mmat.tests.mma;

import mma.annotations.ModAnnotations.*;
import mmat.tests.*;

@MainClass(TestMod.class)
@DependenciesAnnotation()
@AnnotationSettings(
rootPackage = "mmat.tests",
modInfoPath = "tests/assets/mod.hjson",
classPrefix = "Tm"
)
class AnnotationProcessorSettings {
}
