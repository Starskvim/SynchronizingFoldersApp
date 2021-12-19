import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

@NoArgsConstructor
public class MainApp {

    private static HashSet<File> folder1list = new HashSet<>();
    private static HashSet<File> folder2list = new HashSet<>();
    private static HashSet<String> currentFileABSpath = new HashSet<>();


    public static void main(String[] args) {

        String folder1 = "F:\\test\\folder1";
        String folder2 = "F:\\test\\folder2";

//        String folder1 = "F:\\[3D PRINT]\\Модели\\[Patreon]\\[Figure]";
//        String folder2 = "D:\\[Patreon]\\[Figure]";

        //syncStartMethodA(folder1, folder2);
        syncStartMethodB(folder1, folder2);
        //syncStartMethodC(folder1, folder2);
        //System.out.println(" done ");

//        try {
//            Collection<File> test = startScan(folder1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        //String fileForMD5 = "F:\\test\\folder1\\11\\1121\\112131\\okas ore glow.zip";
        //System.out.println(getMD5file(fileForMD5));
    }

    public static void syncStartMethodA(String folder1, String folder2) {


        folder1list.addAll(startScan(folder1));

        for (File file : folder1list) {
            try {

                String absPathCurrentFile = file.getAbsolutePath();
                String pathCurrentFile = file.getParent();

                String pathNewFile = pathCurrentFile.replace(folder1, folder2);
                String absPathNewFile = absPathCurrentFile.replace(folder1, folder2);

                if (Files.exists(Paths.get(pathNewFile))) {
                    System.out.println("yes");

                    if (!Files.isRegularFile(Paths.get(absPathNewFile))) {
                        copyFileUsingStream(file, new File(absPathNewFile));
                    } else {
                        System.out.println("skipped " + absPathNewFile);
                    }

                } else {
                    System.out.println("no");

                    try {
                        Files.createDirectories(Paths.get(pathNewFile));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (!Files.isRegularFile(Paths.get(absPathNewFile))) {
                        copyFileUsingStream(file, new File(absPathNewFile));
                    } else {
                        System.out.println("skipped " + absPathNewFile);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void syncStartMethodB(String folder1, String folder2) {

        syncStartMethodA(folder1, folder2);

        folder2list.addAll(startScan(folder2));
        if (!folder2list.isEmpty()) {
            syncRenamingFiles(folder1list, folder2list);
            folder2list.clear();
            folder2list.addAll(startScan(folder2));
        }

        for (File file : folder1list) {
            currentFileABSpath.add(file.getAbsolutePath());
        }

        for (File file : folder2list) {

            if (!currentFileABSpath.contains(file.getAbsolutePath().replace(folder2, folder1))) {

                File fileFolder = new File(file.getParent());
                if (file.delete()) {
                    System.out.println(file.getName() + " - удален");
                } else {
                    System.out.println(file.getName() + " - не удален");
                }
                if (fileFolder.delete()) {
                    System.out.println(file.getParent() + " - папка удалена");
                } else {
                    System.out.println(file.getParent() + " - папка не пуста");
                }

            }
        }


    }

    public static void syncStartMethodC(String folder1, String folder2) {
        syncStartMethodA(folder1, folder2);
        folder1list.clear();
        folder2list.clear();
        syncStartMethodA(folder2, folder1);
    }

    public static void syncRenamingFiles(HashSet<File> folder1listToMD5, HashSet<File> folder2listToMD5) {


        HashMap<String, File> folder1FilesMap = new HashMap<>();
        for (File file : folder1listToMD5) {
            folder1FilesMap.put(getMD5file(file.getAbsolutePath()), file);
        }
        for (File targetFile : folder2listToMD5) {

            String md5TargetFile = getMD5file(targetFile.getAbsolutePath());

            if (folder1FilesMap.containsKey(md5TargetFile)) {
                File currentFile = folder1FilesMap.get(md5TargetFile);
                if (!currentFile.getName().equals(targetFile.getName())) {

                    System.out.println(targetFile.getParent() + "\\" + currentFile.getName());

                    Path targetPath = targetFile.toPath();
                    Path targetPathRename = Paths.get(targetFile.getParent() + "\\" + currentFile.getName());

                    try {
                        Files.move(targetPath, targetPathRename, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("rename ok");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String getMD5file(String path) {
        String md5 = "";
        try {
            try (InputStream is = Files.newInputStream(Paths.get(path))) {

                md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
                System.out.println(md5);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return md5;
    }

    public static Collection<File> startScan(String adress) {

        long start = System.currentTimeMillis();

        File adres = new File(adress);


        Collection<File> files = null;
        try {
            files = FileUtils.streamFiles(adres, true, null).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (File file : files) {

            System.out.println(file.getPath() + "--------" + file.getParent() + "--------" + file.getParentFile() + "--------" + FilenameUtils.getExtension(file.getName()));

        }

        long fin = System.currentTimeMillis();
        System.out.println("scan time " + (fin - start));

        return files;
    }

    private static void copyFileUsingChannel(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private static void copyFileUsingJava7Files(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }

}
