package mma.annotations;
/**all values here have default values*/
public enum AnnotationSetting{
    /**Revisions path(needs if  are using entity generation)
     * @default "annotations/src/main/resources/revisions"*/
    revisionsPath,
    /**Prefix for generated classes
     * @default capitalized root package name*/
    classPrefix,
    /**root package name
     * @default the name of the first folder in "core/src"*/
    rootPackage
    ;
}
