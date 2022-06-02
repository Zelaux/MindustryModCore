package mma.annotations;
/**all values here have default values*/
public enum AnnotationSetting{
    /**Assets path(needs if are using assets classes generation)
     * @default "core/assets"*/
    assetsPath,
    /**path to mod.(h)json
     * @default rootProject*/
    modInfoPath,
    /**Assets raw path(needs if are using Tex.java generation)
     * a null value means that it is equal to assetsPath
     * @default "core/assets-raw"*/
    assetsRawPath,
    /**Revisions path(needs if  are using entity generation)
     * @default "annotations/src/main/resources/revisions"*/
    revisionsPath,
    /**Prefix for generated classes
     * @default capitalized root package name*/
    classPrefix,
    /**root package name
     * @default the name of the first folder in "core/src"*/
    rootPackage

}
