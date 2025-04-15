package io.github.francescodonnini.data;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataLoaderImpl implements DataLoader {
    private final Logger logger = Logger.getLogger(DataLoaderImpl.class.getName());
    // repositoryPath è il percorso delle repository dove leggere i file da cui creare le entry per il dataset.
    private final String projectPath;
    // releases è la lista delle release da cui selezionare i file per le entry.
    private final List<Release> releases;
    private final List<JavaClass> classes = new ArrayList<>();
    private final List<JavaMethod> methods = new ArrayList<>();
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final JavaMethodExtractor extractor = new JavaMethodExtractor();

    public DataLoaderImpl(String projectPath, List<Release> releases) {
        this.projectPath = projectPath;
        this.releases = releases;
    }


    @Override
    public List<JavaClass> getClasses() {
        lazyLoading();
        return classes;
    }

    @Override
    public List<JavaMethod> getMethods() {
        lazyLoading();
        return methods;
    }

    private void lazyLoading() {
        if (classes.isEmpty()) {
            try {
                createEntries();
            } catch (IOException | GitAPIException e) {
                logger.log(Level.SEVERE, "error creating entries from file", e);
            }
        }
    }

    // createEntries crea una lista di entry partendo da repositoryPath e da una lista di release selezionate da Jira.
    // Ci si aspetta che il naming delle release scelte da Jira sia consistente con il naming dei tag su github.
    private void createEntries() throws IOException, GitAPIException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(projectPath, ".git"))
                .build();
        var git = new Git(repository);
        var current = repository.getBranch();
        var tags = git.tagList().call().stream().map(Ref::getName).toList();
        for (var tag : tags) {
            var o = releases.stream().filter(r -> tag.endsWith(r.name())).findFirst();
            // Non è stata trovata la release indicata nei tag di github tra le release selezionate da Jira quindi
            // si scarta il tag.
            if (o.isEmpty()) {
                continue;
            }
            var release = o.get();
            git.checkout().setName(tag).call();
            listAllFiles(Path.of(projectPath)).stream()
                    .filter(this::isValidPath).forEach(f -> {
                        createEntry(f, release);
                    });
        }
        // Si reimposta la repository locale di git a quella iniziale.
        git.checkout().setName(current).call();
    }

    // Si vogliono selezionare solamente i file .java che non sono file di test.
    private boolean isValidPath(Path path) {
        return isJavaNonTestFile(path.toString());
    }

    private static List<Path> listAllFiles(Path basePath) throws IOException {
        var paths = new ArrayList<Path>();
        paths.add(basePath);
        var files = new ArrayList<Path>();
        while (!paths.isEmpty()) {
            var path = paths.removeLast();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
            {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        if (!Files.isHidden(entry)) {
                            paths.add(entry);
                        }
                    } else if (isJavaNonTestFile(entry.toString())) {
                        files.add(basePath.relativize(entry));
                    }
                }
            }
        }
        return files;
    }

    private static boolean isJavaNonTestFile(String path) {
        return isJavaFile(path) && isNonTestFile(path);
    }

    private static boolean isJavaFile(String path) {
        return path.endsWith(".java") && !path.endsWith("package-info.java");
    }

    private static boolean isNonTestFile(String path) {
        return path.contains("src" + File.separator + "main");
    }

    // createEntry legge un file in path afferente a release (si assume che quando invocato il metodo è stato fatto
    // checkout allo snapshot della repository indicato da release).
    // createEntry inizializza i campi path, buggy, content e release della classe relativa a path
    // e non a un file. In Java sebbene sia la norma che un file coincide con una classe, può capitare che ci siano
    // classi nidificate.
    private void createEntry(Path path, Release release) {
        var realPath = Path.of(projectPath, path.toString());
        try {
            // Non sempre ci si sposta alla fine del file invocando skip con il valore dell'intero massimo, potrebbe
            // essere necessario effettuare più invocazioni.
            // buggy inizialmente viene impostato come false, perché l'etichettatura viene fatta in un secondo momento.
            var clazz = new JavaClass(projectPath, path.toString(), release, Files.readString(realPath));
            classes.add(clazz);
            extractor.setClass(clazz);
            parseMethods(clazz);
        } catch (IOException e) {
            logger.log(Level.INFO, "Release = %s, file not found: %s".formatted(release.name(), path));
        }
    }

    private void parseMethods(JavaClass clazz) throws IOException {
        var units = compiler
                .getStandardFileManager(null, null, null)
                .getJavaFileObjects(clazz.getRealPath());
        var task = (JavacTask) compiler.getTask(null, null, null, null, null, units);
        extractor.setSourcePositions(Trees.instance(task).getSourcePositions());
        for (var cu : task.parse()) {
            extractor.setCompilationUnit(cu);
            cu.accept(extractor, null);
            methods.addAll(extractor.getMethods());
            extractor.reset();
        }
    }
}
