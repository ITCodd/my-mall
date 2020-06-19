package com.github.utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
 
public class FileCompressUtil {
     
    public static int BUFFER_SIZE = 2048;
     
    private static List<String> unTar(InputStream inputStream, String destDir) throws Exception {
        List<String> fileNames = new ArrayList<String>();
        TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream, BUFFER_SIZE);
        TarArchiveEntry entry = null;
        try {
            while ((entry = tarIn.getNextTarEntry()) != null) {
                fileNames.add(entry.getName());
                if (entry.isDirectory()) {//是目录  
                    createDirectory(destDir, entry.getName());//创建空目录  
                } else {//是文件  
                    File tmpFile = new File(destDir + File.separator + entry.getName());
                    createDirectory(tmpFile.getParent() + File.separator, null);//创建输出目录  
                    OutputStream out = null;
                    try { 
                        out = new FileOutputStream(tmpFile);
                        int length = 0;
                        byte[] b = new byte[2048];
                        while ((length = tarIn.read(b)) != -1) {
                            out.write(b, 0, length);
                        }
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            IOUtils.closeQuietly(tarIn);
        }
         
        return fileNames;
    }
     
    public static List<String> unTar(String tarFile, String destDir) throws Exception {
        File file = new File(tarFile);
        return unTar(file, destDir);
    }
     
    public static List<String> unTar(File tarFile, String destDir) throws Exception {
        if(StringUtils.isBlank(destDir)) {
            destDir = tarFile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        return unTar(new FileInputStream(tarFile), destDir);
    }
     
    public static List<String> unTarBZip2(File tarFile,String destDir) throws Exception{ 
        if(StringUtils.isBlank(destDir)) {
            destDir = tarFile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        return unTar(new BZip2CompressorInputStream(new FileInputStream(tarFile)), destDir);
    }  
     
    public static List<String> unTarBZip2(String file,String destDir) throws Exception{ 
        File tarFile = new File(file);
        return unTarBZip2(tarFile, destDir);
    }  
     
    public static List<String> unBZip2(String bzip2File, String destDir) throws IOException {
        File file = new File(bzip2File);
        return unBZip2(file, destDir);
    }
     
    public static List<String> unBZip2(File srcFile, String destDir) throws IOException {
        if(StringUtils.isBlank(destDir)) {
            destDir = srcFile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        List<String> fileNames = new ArrayList<String>();
        InputStream is = null;
        OutputStream os = null;
        try {
            File destFile = new File(destDir, FilenameUtils.getBaseName(srcFile.toString()));
            fileNames.add(FilenameUtils.getBaseName(srcFile.toString()));
            is = new BZip2CompressorInputStream(new BufferedInputStream(new FileInputStream(srcFile), BUFFER_SIZE));
            os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
        return fileNames;
    }
     
    public static List<String> unGZ(String gzFile, String destDir) throws IOException {
        File file = new File(gzFile);
        return unGZ(file, destDir);
    }
     
    public static List<String> unGZ(File srcFile, String destDir) throws IOException {
        if(StringUtils.isBlank(destDir)) {
            destDir = srcFile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        List<String> fileNames = new ArrayList<String>();
        InputStream is = null;
        OutputStream os = null;
        try {
            File destFile = new File(destDir, FilenameUtils.getBaseName(srcFile.toString()));
            fileNames.add(FilenameUtils.getBaseName(srcFile.toString()));
            is = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(srcFile), BUFFER_SIZE),true);
            os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
        return fileNames;
    }
     
    public static List<String> unTarGZ(File tarFile,String destDir) throws Exception{ 
        if(StringUtils.isBlank(destDir)) {
            destDir = tarFile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        return unTar(new GzipCompressorInputStream(new FileInputStream(tarFile)), destDir);
    }  
     
    public static List<String> unTarGZ(String file,String destDir) throws Exception{ 
        File tarFile = new File(file);
        return unTarGZ(tarFile, destDir);
    }  
     
    public static void createDirectory(String outputDir,String subDir){  
        File file = new File(outputDir);  
        if(!(subDir == null || subDir.trim().equals(""))){//子目录不为空  
            file = new File(outputDir + File.separator + subDir);  
        }  
        if(!file.exists()){  
            file.mkdirs();  
        }  
    }  
     
    public static List<String> unZip(File zipfile, String destDir) throws Exception {
        if(StringUtils.isBlank(destDir)) {
            destDir = zipfile.getParent();
        }
        destDir = destDir.endsWith(File.separator) ? destDir : destDir + File.separator;
        ZipArchiveInputStream is = null;
        List<String> fileNames = new ArrayList<String>();
 
        try {
            is = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipfile), BUFFER_SIZE));
            ZipArchiveEntry entry = null;
            while ((entry = is.getNextZipEntry()) != null) {
                fileNames.add(entry.getName());
                if (entry.isDirectory()) {
                    File directory = new File(destDir, entry.getName());
                    directory.mkdirs();
                } else {
                    OutputStream os = null;
                    try {
                        os = new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName())), BUFFER_SIZE);
                        IOUtils.copy(is, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            IOUtils.closeQuietly(is);
        }
         
        return fileNames;
    }
     
    public static List<String> unZip(String zipfile, String destDir) throws Exception {
        File zipFile = new File(zipfile);
        return unZip(zipFile, destDir);
    }
     
    public static List<String> unCompress(String compressFile, String destDir) throws Exception {
        String upperName= compressFile.toUpperCase();
        List<String> ret = null;
        if(upperName.endsWith(".ZIP")) {
            ret = unZip(compressFile, destDir);
        } else if(upperName.endsWith(".TAR")) {
            ret = unTar(compressFile, destDir);
        } else if(upperName.endsWith(".TAR.BZ2")) {
            ret = unTarBZip2(compressFile, destDir);
        } else if(upperName.endsWith(".BZ2")) {
            ret = unBZip2(compressFile, destDir);
        } else if(upperName.endsWith(".TAR.GZ")) {
            ret = unTarGZ(compressFile, destDir);
        } else if(upperName.endsWith(".GZ")) {
            ret = unGZ(compressFile, destDir);
        }
        return ret;
    }

    private static List<File> getFiles(File file) throws IOException {
        List<File> files=new ArrayList<>();
        Queue<File> queue=new LinkedBlockingQueue<File>();
        queue.add(file);
        while (!queue.isEmpty()){
            File f = queue.poll();
            if(f!=file){
                files.add(f);
            }
            if(!f.isFile()){
                File[] listFiles = f.listFiles();
                if(listFiles!=null&&listFiles.length!=0){
                    for (File listFile : listFiles) {
                        queue.offer(listFile);
                    }
                }
            }
        }
        return files;
    }

    public static void compressGzip(File dir,File outFile) throws IOException{
        OutputStream myOutputStream=new FileOutputStream(outFile);
        GzipParameters parameters=new GzipParameters();
        parameters.setFilename(FilenameUtils.getName(outFile.getName()));
        OutputStream compressorOutputStream = new GzipCompressorOutputStream(myOutputStream,parameters);
        compress(dir, compressorOutputStream);
        myOutputStream.close();
    }

    public static void doCompress(File dir,File outFile,String type) throws IOException, CompressorException {
        if(StringUtils.equals(type,"gz")){
            compressGzip(dir,outFile);
        }else{
            OutputStream myOutputStream=new FileOutputStream(outFile);
            CompressorOutputStream compressorOutputStream = new CompressorStreamFactory()
                    .createCompressorOutputStream(type, myOutputStream);
            compress(dir, compressorOutputStream);
            myOutputStream.close();
        }

    }

    private static void compress(File dir, OutputStream compressorOutputStream) throws IOException {
        List<File> files = getFiles(dir);
        String dirPath = dir.getAbsolutePath();
        try (ArchiveOutputStream o = new TarArchiveOutputStream(compressorOutputStream)) {
            for (File f : files) {
                String filePath=f.getAbsolutePath();
                ArchiveEntry entry = o.createArchiveEntry(f, filePath.substring(dirPath.length() + 1));
                o.putArchiveEntry(entry);
                if (f.isFile()) {
                    try (InputStream i = Files.newInputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
                o.closeArchiveEntry();
            }
            o.finish();
        }
        compressorOutputStream.close();
    }

    public static void main(String[] args) throws Exception {
        File dir = new File("C:\\Users\\DELL\\Desktop\\笔记");
        List<File> files = getFiles(dir);
        for (File file : files) {
            System.out.println("file = " + file.getAbsolutePath());
        }

//        File outFile=new File("E:\\var\\2.bzip2");
//        doCompress(dir,outFile,"bzip2");
//        System.out.println(unZip("F:\\data_detail\\MRO_ZTE_20170816150000.zip", null));
//        System.out.println(unTar("F:\\fileupload\\中文test.tar", "F:\\fileupload\\"));
         
        //System.out.println(unBZip2("F:\\fileupload\\中文test.xml.bz2", "F:\\fileupload\\"));
        System.out.println(unTarBZip2("E:\\var\\2.bzip2", "E:\\var\\out"));
         
//        System.out.println(unTarGZ("F:\\data_detail\\zookeeper-3.4.10.tar.gz", null));
        //System.out.println(unTarGZ("F:\\fileupload\\all.tar.gz", "F:\\fileupload\\"));
//    	List<String> list = unZip("D:\\server\\server\\apache-tomcat-7.0.86-windows-x64.zip", "F:\\360Downloads\\unzip");
//    	for (String f : list) {
//			System.out.println(f);
//		}
    }
}