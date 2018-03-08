package com.hawker.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static fj.data.List.list;

/**
 * @author mingjiang.ji on 2017/11/22
 */
public class FileUtil {

    public static List<String> readLines(String filePath) throws IOException {
        try (InputStream is = new FileInputStream(filePath)) {
            return IOUtils.readLines(is, "utf8");
        }
    }

    public static List<String> readLines(File filePath) throws IOException {
        try (InputStream is = new FileInputStream(filePath)) {
            return IOUtils.readLines(is, "utf8");
        }
    }

    public static String readLinesStr(String filePath) throws IOException {
        return StringUtils.join(readLines(filePath),"");
    }

    /**
     * 输入文件全称, 输出文件名（不带后缀）
     * 例如： abcde.txt -> abcde Or abc.cde$fgh.mp4 -> abc.cde$fgh
     */
    public static String getFileName(String fileName) {
        if (fileName.contains(".")) {
            return StringUtils.join(list(fileName.split("\\.")).init().toJavaList().toArray(), ".");
        } else {
            return fileName;
        }
    }


    /**
     * 输入文件全称, 输出文件后缀
     * 例如： abcde.txt -> txt Or abc.cde$fgh.mp4 -> mp4
     */
    public static String getFileSuffix(String fileName) {
        if (fileName.contains(".")) {
            return list(fileName.split("\\.")).last();
        } else {
            return fileName;
        }
    }

    public static void main(String[] args) throws IOException {
//        readLinesStr("src/main/avro/user.avsc");
        Path resourceDirectory = Paths.get("src/main/avro/user.avsc");
        System.out.println(resourceDirectory.getFileName());
        System.out.println(resourceDirectory.toFile());

        List<String>  a = readLines(resourceDirectory.toFile());

    }
}
