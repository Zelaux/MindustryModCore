package mmc;

import lombok.*;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.io.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;

public class AnnotationJarRenamer{
    public static final String JAVAX_ANNOTATION_PROCESSING_PROCESSOR = "javax.annotation.processing.Processor";
    public static final String META_INF_SERVICES_JAVAX_ANNOTATION_PROCESSING_PROCESSOR = "META-INF/services/" + JAVAX_ANNOTATION_PROCESSING_PROCESSOR;

    public static void run(File file){
        renameClassesInJar(file);
    }

    @SneakyThrows
    static void renameClassesInJar(File jarFile){
        Field nameField = ZipArchiveEntry.class.getDeclaredField("name");
        nameField.setAccessible(true);

        System.out.println("Unpacking");
        ZipFile zipFile = new ZipFile(jarFile);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ZipArchiveEntry[] archiveEntry = {null};
        String processorsList;
        try(ZipArchiveOutputStream stream = new ZipArchiveOutputStream(out1)){
            zipFile.copyRawEntries(stream, new ZipArchiveEntryPredicate(){
                @Override
                @SneakyThrows
                public boolean test(ZipArchiveEntry zipArchiveEntry){

                    String name = zipArchiveEntry.getName();

                    String suffix = ".class";
                    if(!name.endsWith(suffix)){
                        if(!name.equals(META_INF_SERVICES_JAVAX_ANNOTATION_PROCESSING_PROCESSOR)) return true;
                        archiveEntry[0] = zipArchiveEntry;
                        return false;
                    }
                    String className = name.substring(0, name.length() - suffix.length())
                                           .replace('/', '.');

                    if(className.startsWith("mmc.annotations.ModAnnotations")) return true;
                    if(className.startsWith("mmc") && (className.endsWith("Delegate") || className.endsWith(".internal."))) return true;
                    if(className.startsWith("mindustry.annotations.Annotations")) return true;
                    nameField.set(zipArchiveEntry, name + ".hidden");

                    return true;
                }
            });
            processorsList = IOUtils.toString(zipFile.getInputStream(archiveEntry[0]), StandardCharsets.UTF_8);


            {
                File processorListFile = new File(jarFile.getParentFile(), JAVAX_ANNOTATION_PROCESSING_PROCESSOR);
                String[] lines = processorsList.split("\n");
                for(int i = 0; i < lines.length; i++){
                    lines[i] += "Delegate";
                }
                FileUtils.write(processorListFile, String.join(",", lines), StandardCharsets.UTF_8);
                ArchiveEntry archiveEntry1 = stream.createArchiveEntry(processorListFile, META_INF_SERVICES_JAVAX_ANNOTATION_PROCESSING_PROCESSOR);

                stream.putArchiveEntry(archiveEntry1);
                FileUtils.copyFile(processorListFile, stream);
                ;
                stream.closeArchiveEntry();

            }
        }
        zipFile.close();
        System.out.println("Packing");

        jarFile.delete();


        FileUtils.writeByteArrayToFile(jarFile, out1.toByteArray());
        out1.close();


        System.out.println(jarFile.getCanonicalPath());

    }

}
