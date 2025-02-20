package mmc.annotations.internal;

import arc.util.io.*;
import lombok.*;
import lombok.experimental.Delegate;

import javax.annotation.processing.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

@SupportedAnnotationTypes("*")
public class DelegateProcessor implements Processor{
    public static final String SUFFIX = "Delegate";
    @Delegate()
    final AbstractProcessor created;
final DelegateClassLoader loader;
    @SneakyThrows
    public DelegateProcessor(){
        Class<? extends DelegateProcessor> clzz = getClass();
        String canonicalName = clzz.getCanonicalName();
        if(!canonicalName.endsWith(SUFFIX))throw new RuntimeException("Class name must ends with Delegate");
        loader = new DelegateClassLoader(clzz.getClassLoader());
        Class<?> found = loader.loadClass(canonicalName.substring(0, canonicalName.length() - SUFFIX.length()));
        Constructor<?> constructor = found.getConstructor();
        constructor.setAccessible(true);

        this.created= (AbstractProcessor)constructor.newInstance();
    }

    @AllArgsConstructor
    protected static class DelegateClassLoader extends ClassLoader{
        static ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ClassLoader parent;

        public static byte[] readBytes(InputStream inputStream) throws IOException{
            buffer.reset();
            int nRead;
            byte[] data = new byte[16384]; // Размер буфера (можно настроить)

            while((nRead = inputStream.read(data, 0, data.length)) != -1){
                buffer.write(data, 0, nRead);
            }

            buffer.flush(); // Очищаем буфер

            return buffer.toByteArray();
        }

        @SneakyThrows
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException{

            URL resource = parent.getResource(name.replace('.','/') + ".class.hidden");
            if(resource == null) return parent.loadClass(name);
            byte[] bytes;
            try(val stream = new BufferedInputStream(resource.openStream())){
                bytes=readBytes(stream);
            }

            return defineClass(name,bytes,0,bytes.length);
        }

    }

}
