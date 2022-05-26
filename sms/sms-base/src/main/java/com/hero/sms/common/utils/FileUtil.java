package com.hero.sms.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Preconditions;
import com.hero.sms.common.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Administrator
 */
@Slf4j
public class FileUtil {

    private static final int BUFFER = 1024 * 8;

    /**
     * 压缩文件或目录
     *
     * @param fromPath 待压缩文件或路径
     * @param toPath   压缩文件，如 xx.zip
     */
    public static void compress(String fromPath, String toPath) throws IOException {
        File fromFile = new File(fromPath);
        File toFile = new File(toPath);
        if (!fromFile.exists()) {
            throw new FileNotFoundException(fromPath + "不存在！");
        }
        try (
                FileOutputStream outputStream = new FileOutputStream(toFile);
                CheckedOutputStream checkedOutputStream = new CheckedOutputStream(outputStream, new CRC32());
                ZipOutputStream zipOutputStream = new ZipOutputStream(checkedOutputStream)
        ) {
            String baseDir = "";
            compress(fromFile, zipOutputStream, baseDir);
        }
    }

    /**
     * 文件下载
     *
     * @param filePath 待下载文件路径
     * @param fileName 下载文件名称
     * @param limitFileTypes 限制的下载文件类型
     * @param delete   下载后是否删除源文件
     * @param response HttpServletResponse
     * @throws Exception Exception
     */
    public static void download(String filePath, String fileName,String[] limitFileTypes, Boolean delete, HttpServletResponse response) throws Exception {
        File file = new File(filePath);
        if (!file.exists())
            throw new Exception("文件未找到");

        String fileType = getFileType(file);
        if (!fileTypeIsValid(limitFileTypes,fileType)) {
            throw new Exception("暂不支持该类型文件下载");
        }
        response.setHeader("Content-Disposition", "attachment;fileName=" + java.net.URLEncoder.encode(fileName, "utf-8"));
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        try (InputStream inputStream = new FileInputStream(file); OutputStream os = response.getOutputStream()) {
            byte[] b = new byte[2048];
            int length;
            while ((length = inputStream.read(b)) > 0) {
                os.write(b, 0, length);
            }
        } finally {
            if (delete)
                delete(filePath);
        }
    }

    /**
     * 递归删除文件或目录
     *
     * @param filePath 文件或目录
     */
    public static void delete(String filePath) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) Arrays.stream(files).forEach(f -> delete(f.getPath()));
        }
        file.delete();
    }

    /**
     * 获取文件类型
     *
     * @param file 文件
     * @return 文件类型
     * @throws Exception Exception
     */
    private static String getFileType(File file) throws Exception {
        Preconditions.checkNotNull(file);
        if (file.isDirectory()) {
            throw new Exception("file不是文件");
        }
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    /**
     * 校验文件类型是否是允许下载的类型
     * （出于安全考虑：https://github.com/wuyouzhuguli/FEBS-Shiro/issues/40）
     *
     * @param fileType fileType
     * @return Boolean
     */
    private static Boolean fileTypeIsValid(String[] limitFileTypes,String fileType) {
        Preconditions.checkNotNull(fileType);
        fileType = StringUtils.lowerCase(fileType);
        return ArrayUtils.contains(limitFileTypes, fileType);
    }

    private static void compress(File file, ZipOutputStream zipOut, String baseDir) throws IOException {
        if (file.isDirectory()) {
            compressDirectory(file, zipOut, baseDir);
        } else {
            compressFile(file, zipOut, baseDir);
        }
    }

    private static void compressDirectory(File dir, ZipOutputStream zipOut, String baseDir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null && ArrayUtils.isNotEmpty(files)) {
            for (File file : files) {
                compress(file, zipOut, baseDir + dir.getName() + "/");
            }
        }
    }

    private static void compressFile(File file, ZipOutputStream zipOut, String baseDir) throws IOException {
        if (!file.exists()) {
            return;
        }
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            ZipEntry entry = new ZipEntry(baseDir + file.getName());
            zipOut.putNextEntry(entry);
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                zipOut.write(data, 0, count);
            }
        }
    }

    /**
     * 判断是否图片
     * @param head
     * @return
     */
    public static boolean isPpictureFile(byte[] head){
        /*                 JEPG       PNG        GIF         TIFF     Windows Bitmap      CAD     Adobe Photoshop */
        String[] pics = { "FFD8FF", "89504E47", "47494638", "49492A00", "424D",        "41433130","38425053" };
        String fileHead = bytesToHexString(head);
        if (fileHead == null || fileHead.length() == 0) {
            return false;
        }
        fileHead = fileHead.toUpperCase();
        for (String fileType : pics) {
            if (fileHead.startsWith(fileType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将文件头转换成16进制字符串
     *
     * @param src 原生byte
     * @return 16进制字符串
     */
    private static String bytesToHexString(byte[] src) {

        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 保存图片
     *
     * @param img
     * @param filiName
     * @return
     */
    public static boolean savePpictureFile(MultipartFile img, String filiName, Long fileSize) throws Exception {
        try {
            File saveFile = new File(filiName);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            FileInputStream fis = (FileInputStream) img.getInputStream();
            FileOutputStream fos = new FileOutputStream(saveFile);
            Long kb = 1L;
            if(fileSize != 0) {
                kb = fileSize;
            }
            Long size = kb * 1024L;
            if(img.getSize() > size) {
                fos.flush();
                fos.close();
                fis.close();
                throw new ServiceException("图片大于"+ kb +"KB");
            }
            byte[] head = new byte[28];
            fis.read(head, 0, 28);
            if(!isPpictureFile(head)) {
                fos.flush();
                fos.close();
                fis.close();
                throw new ServiceException("图片类型不对");
            }
            fos.write(head, 0, 28);
            int len;
            byte buffer[] = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            fis.close();
            return true;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 根据流（.txt）获取内容
     *
     * @param file
     * @return
     */
    public static String getContentFromInputStream(MultipartFile file) {
        StringBuffer result = new StringBuffer();
        InputStreamReader inputStream = null;
        try {
            String code = getFilecharset(file.getInputStream());
            inputStream = new InputStreamReader(file.getInputStream(),code);
            BufferedReader reader = new BufferedReader(inputStream);
            String line = null;
            boolean firstFlag = true;
            while ((line = reader.readLine()) != null) {
                if (!firstFlag) {
                    result.append(",");
                } else {
                    firstFlag = false;
                }
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    private static String getFilecharset(InputStream inputStream) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                return charset; //文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF
                    && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; //文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; //文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; //文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
     * 说明：根据指定URL将文件下载到指定目标位置
     * 
     * @param urlPath
     *            下载路径
     * @param downloadDir
     *            文件存放目录
     * @return 返回下载文件
     */
    public static File downloadFile(String urlPath, String downloadDir,String fileFullName) {
        File file = null;
        try {
            // 统一资源
            URL url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            //设置超时
            httpURLConnection.setConnectTimeout(1000*5);
            //设置请求方式，默认是GET
            httpURLConnection.setRequestMethod("POST");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();
            // 文件大小
            int fileLength = httpURLConnection.getContentLength();
 
            // 控制台打印文件大小
            System.out.println("您要下载的文件大小为:" + fileLength / (1024 * 1024) + "MB");
 
            // 建立链接从请求中获取数据
            URLConnection con = url.openConnection();
            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
            // 指定文件名称(有需求可以自定义)
            // 指定存放位置(有需求可以自定义)
            String path = downloadDir + File.separatorChar + fileFullName;
            file = new File(path);
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
 
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[2048];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
                // 控制台打印文件下载的百分比情况
                System.out.println("下载了-------> " + len * 100 / fileLength + "%\n");
            }
            // 关闭资源
            bin.close();
            out.close();
            System.out.println("文件下载成功！");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("文件下载失败！");
        } finally {
            return file;
        }
    }
    
    /**
     * 判断文件是否存在
     * @param filePath
     * @return
     */
    public static boolean fileIsExists(String filePath) 
    {
    	boolean fileIsExists = false;
    	try 
    	{
    		 File file = new File(filePath);
    	     if (file.exists())
    	     {
    	    	 fileIsExists = true;
    	     }
		} catch (Exception e) {
		}
        return fileIsExists;
    }
 
    public static int downloadNet(String urlPath, String downloadDir,String fileFullName) throws MalformedURLException {
    	int returnInt = -1;
        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;

        URL url = new URL(urlPath);
        try 
        {
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
         // 指定存放位置(有需求可以自定义)
            String path = downloadDir + File.separatorChar + fileFullName;
            File file = new File(path);
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fs = new FileOutputStream(path);
            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            returnInt = 1;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            returnInt = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnInt;
    }
}
