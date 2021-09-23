package mma.annotations;

import arc.files.Fi;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.OS;
import arc.util.Strings;
import arc.util.Time;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import mindustry.annotations.BaseProcessor;
import mindustry.annotations.util.Selement;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class ModBaseProcessor extends BaseProcessor {
    static final String parentName = "mindustry.gen";
    public static String rootPackageName = null;
    {
        enableTimer=true;
    }
    public static void print(String obj, Object... args) {
        String message = Strings.format(obj.toString(), args);
        System.out.println(message);
    }

    public static void write(TypeSpec.Builder builder, String packageName) throws Exception {
        write(builder, packageName, (Seq<String>) null);
    }

    protected static Fi getFilesFi(StandardLocation location) throws IOException {
        return getFilesFi(location, "no", "no").parent().parent();
    }

    protected static Fi getFilesFi(StandardLocation location, String packageName, String className) throws IOException {
        return Fi.get(filer.getResource(location, packageName, className)
                .toUri().toURL().toString().substring(OS.isWindows ? 6 : "file:".length()));
    }

    public String classPrefix() {
        String classNamePrefix = "Mod";
        if (!rootPackageName.equals("mma")) {
            classNamePrefix = Strings.capitalize(rootPackageName);
            Fi child = rootDirectory.child("annotations/classPrefix.txt");
            if (child.exists()) {
                classNamePrefix = child.readString();
            }
        }
        return classNamePrefix;
    }

    @Override
    protected String getPackageName() {
        packageName=(rootPackageName = rootDirectory.child("core/src").list()[0].name()) + ".gen";
        return packageName;
    }

    public void delete(String packageName, String name) throws IOException {
//        print("delete name: @",name);
        FileObject resource;
        resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, name);
//        boolean delete = resource.delete();
//        print("delete: @ ,named: @, filer: @",delete,resource.getName(),resource.getClass().getName());
        Files.delete(Paths.get(resource.getName() + ".java"));
    }

    public void delete(String name) throws IOException {
        delete(packageName, name);
    }
}
