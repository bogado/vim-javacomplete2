package kg.ash.javavi.searchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClasspathCollector {

    public static final Logger logger = LogManager.getLogger();

    private ByExtensionVisitor finder 
        = new ByExtensionVisitor(
                Arrays.asList(
                    "*.jar", "*.JAR", "*.zip", "*.ZIP", "*.class"));
    
    private String pSep = File.pathSeparator;
    
    public List<String> collectClassPath() {
        List<String> result = new ArrayList<>();

        String extdirs = System.getProperty("java.ext.dirs");
        Stream.of(extdirs.split(pSep))
            .map(path -> addPathFromDir(path + File.separator))
            .forEach(result::addAll);

        result.addAll(addPathFromDir(System.getProperty("java.home")));

        String classPath = System.getProperty("java.class.path");
        Stream.of(classPath.split(pSep))
            .filter(p -> p.length() >= 4)
            .forEach(path -> {
                String ext = path.substring(path.length() - 4)
                    .toLowerCase();
                if (ext.endsWith(".jar") || ext.endsWith(".zip")) {
                    result.add(path);
                } else {
                    result.addAll(addPathFromDir(path));
                }
            });

        return result;
    }

    private List<String> addPathFromDir(String dirpath) {
        List<String> result = new ArrayList<>();
        File dir = new File(dirpath);
        if (dir.isDirectory()) {
            try {
                Files.walkFileTree(Paths.get(dir.getPath()), finder);
                result.addAll(finder.getResultList());
            } catch (IOException e) {
                logger.error(e, e);
            }
        }

        return result;
    }
}
