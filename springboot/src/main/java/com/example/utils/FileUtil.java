package com.example.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    
    /**
     * 读取文件字节数组
     * @param filePath 文件路径
     * @return 文件字节数组
     * @throws IOException
     */
    public static byte[] readBytes(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }
}