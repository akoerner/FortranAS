
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader {
    public static List<String> getClasses() {
        List<String> classNames = new ArrayList<>();
        try {
            String jarFilePath = JarClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                    classNames.add(className);
                }
            }
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classNames;
    }

    public static void printAllJarClasses() {
        List<String> classes = getClasses();
        System.out.println("Classes available in the current JAR:");
        for (String className : classes) {
            System.out.println(className);
        }
    }
}

