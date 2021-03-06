package kg.ash.javavi.searchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kg.ash.javavi.readers.ClassReader;
import kg.ash.javavi.readers.Parser;
import kg.ash.javavi.readers.Reflection;

public class ClassSearcher {

    public static final Logger logger = 
        LogManager.getLogger();

    private boolean isReflected = false;
    private boolean isInSource = false;
    private String sources;
    private String sourceFile = null;

    public boolean find(String targetClass, String sources) {
        logger.debug("executing search of \"{}\"", targetClass);

        this.sources = sources;
        if (Reflection.exist(targetClass) || Reflection.exist("java.lang." + targetClass)) {
            isReflected = true;
            return true;
        } else {
            String[] sourcesArray = sources.split(File.pathSeparator);
            for (String sourceDir : sourcesArray) {
                if (targetClass.contains("$")) {
                    targetClass = targetClass.split("\\$")[0];
                }
                targetClass = targetClass.replaceAll("[\\[\\]]", "");
                SourceFileVisitor visitor = new SourceFileVisitor(targetClass);
                try {
                    Files.walkFileTree(Paths.get(sourceDir), visitor);

                    if (visitor.getTargetFile() != null) {
                        sourceFile = visitor.getTargetFile().replace('\\', '/');
                        isInSource = true;
                        return true;
                    }
                } catch (IOException e) {
                    logger.error(e, e);
                }
            }
        }

        return false;
    }

    public ClassReader getReader() {
        if (isReflected()) {
            return new Reflection(sources);
        } else {
            return new Parser(sources, getSourceFile());
        }
    }

    public boolean isReflected() {
        return isReflected;
    }

    public boolean isInSource() {
        return isInSource;
    }

    public String getSourceFile() {
        return sourceFile;
    }
    
}
