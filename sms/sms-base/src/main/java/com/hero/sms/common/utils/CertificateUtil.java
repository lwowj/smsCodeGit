package com.hero.sms.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.impl.dv.util.Base64;

/**
 * 秘钥证书工具类
 * 
 * @author Administrator
 *
 */
@Slf4j
public class CertificateUtil {

	//public static int blockSize = 128;  还能在坑一点吗？？？？？？  blockSize应该是根据数据计算而来，这里声明成静态变量，当多线程并发时，每天线程都在修改它，导致blockSize出现混乱

	// 非对称密钥算法
	public static final String KEY_ALGORITHM = "RSA";

	public static final int KEY_SIZE = 1024;

	public static final String PUBLIC_KEY = "publicKey";

	public static final String PRIVATE_KEY = "privateKey";

	private CertificateUtil() {
	}

	/**
	 * 初始化生成公钥私钥的类
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPairGenerator initKeyPairGenerator() throws NoSuchAlgorithmException {
		// 实例化密钥生成器
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		// 初始化密钥生成器
		keyPairGenerator.initialize(KEY_SIZE);
		return keyPairGenerator;
	}

	/**
	 * 生成秘钥对
	 * 
	 * @return Map
	 */
	public static Map<String, Object> createKey() {
		// 将生成的公钥私钥放到map中
		Map<String, Object> keyMap = new HashMap<String, Object>();
		try {
			KeyPairGenerator keyPairGenerator = initKeyPairGenerator();
			// 生成密钥对
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			// 公钥
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			// 私钥
			RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

			keyMap.put(PUBLIC_KEY, publicKey);
			keyMap.put(PRIVATE_KEY, privateKey);

		} catch (NoSuchAlgorithmException e) {
			log.error("获取加密实例出错，没有找到对应的加密方式" + e.getMessage(), e);
		}
		return keyMap;

	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static String encryptByPublicKey(String data, String key) {
		try {
			return Base64Utils.encode(encryptByPublicKey(data.getBytes("UTF-8"), Base64Utils.decode(key)));
		} catch (UnsupportedEncodingException e) {
			log.error("公钥加密异常", e);
			return null;
		}
	}

	/**
	 * 公钥加密
	 * 
	 * @param data 待加密数据
	 * @param key
	 *            密钥
	 * @return byte[] 加密数据
	 */
	public static byte[] encryptByPublicKey(byte[] data, byte[] key) {

		// 实例化密钥工厂
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			// 密钥材料转换
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
			// 产生公钥
			PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
			// 数据加密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			int blockSize = cipher.getOutputSize(data.length) - 11;
			return doFinal(data, cipher,blockSize);

		} catch (NoSuchAlgorithmException e) {
			log.error("公钥加密出错：NoSuchAlgorithmException", e);
		} catch (InvalidKeySpecException e) {
			log.error("公钥加密异常：InvalidKeySpecException", e);
		} catch (NoSuchPaddingException e) {
			log.error("公钥加密异常：NoSuchPaddingException", e);
		} catch (InvalidKeyException e) {
			log.error("公钥加密异常：InvalidKeyException", e);
		} catch (IllegalBlockSizeException e) {
			log.error("公钥加密异常：IllegalBlockSizeException", e);
		} catch (BadPaddingException e) {
			log.error("公钥加密异常：BadPaddingException", e);
		} catch (IOException e) {
			log.error("公钥加密异常：IOException", e);
		}
		return null;
	}
	
	/**
	 * 公钥解密
	 * @param data
	 * @param key
	 * @return
	 */
	public static String decryptByPublicKey(String data,String key){
		
		try {
			return new String(decryptByPublicKey(data.getBytes("UTF-8"), Base64Utils.decode(key)),"UTF-8");
		} catch (Exception e) {
			log.error("公钥解密异常",e);
		}
		return null;
	}
	
	  /** 
     * 用公钥解密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] decryptByPublicKey(byte[] data, byte[] key)  
            throws Exception {  
        // 取得公钥  
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key publicKey = keyFactory.generatePublic(x509KeySpec);  
  
        // 对数据解密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.DECRYPT_MODE, publicKey);  
     	int blockSize = cipher.getOutputSize(data.length) - 11;
  
        return doFinal(data,cipher,blockSize);  
    }

	/**
	 * 私钥解密
	 * 
	 * @param data
	 *            待解密数据
	 * @param key
	 *            密钥
	 * @return byte[] 解密数据
	 */
	public static byte[] decryptByPrivateKey(byte[] data, byte[] key) {
		try {
			// 取得私钥
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
			// 生成私钥
			PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
			// 数据解密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			int blockSize = cipher.getOutputSize(data.length);
			return doFinal(data, cipher,blockSize);
		} catch (NoSuchAlgorithmException e) {
			log.error("私钥解密异常：NoSuchAlgorithmException", e);
		} catch (InvalidKeySpecException e) {
			log.error("私钥解密异常：InvalidKeySpecException", e);
		} catch (NoSuchPaddingException e) {
			log.error("私钥解密异常：NoSuchPaddingException", e);
		} catch (InvalidKeyException e) {
			log.error("私钥解密异常：InvalidKeyException", e);
		} catch (IllegalBlockSizeException e) {
			log.error("私钥解密异常：IllegalBlockSizeException", e);
		} catch (BadPaddingException e) {
			log.error("私钥解密异常：BadPaddingException", e);
		} catch (IOException e) {
			log.error("私钥解密异常：IOException", e);
		}
		return null;
	}

	/**
	 * 私钥解密
	 * 
	 * @param data
	 *            待解密数据
	 * @param key
	 *            密钥
	 * @return String 解密数据
	 */
	public static String decryptByPrivateKey(String data, String key) {
		try {
			byte[] decryptStr = decryptByPrivateKey(Base64Utils.decode(data), Base64Utils.decode(key));
			return new String(decryptStr, "UTF-8");
		} catch (IOException e) {
			log.error("转换出错", e);
		}
		return null;
	}
	
	/**
	 * 私钥加密
	 * @param data
	 * @param key
	 * @return
	 */
	public static String encryptByPrivateKey(String data,String key){
		try {
			byte[] decryptStr = encryptByPrivateKey(data.getBytes(), Base64Utils.decode(key));
			return Base64Utils.encode(decryptStr);
		} catch (Exception e) {
			log.error("私钥加密异常", e);
		}
		return null;
	}
	
	/** 
     * 用私钥加密 
     *  
     * @param data 
     * @param key 
     * @return 
     * @throws Exception 
     */  
    public static byte[] encryptByPrivateKey(byte[] data, byte[] key)  
            throws Exception {  
        // 取得私钥  
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);  
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);  
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);  
  
        // 对数据加密  
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());  
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
  
        return cipher.doFinal(data);  
    }  

	/**
	 * 取得私钥
	 * 
	 * @param keyMap
	 *            密钥map
	 * @return byte[] 私钥
	 */
	protected static byte[] getPrivateKey(Map<String, Object> keyMap) {
		Key key = (Key) keyMap.get(PRIVATE_KEY);
		return key.getEncoded();
	}

	/**
	 * 取得私钥 String
	 * 
	 * @param keyMap
	 * @return
	 */
	public static String getPrivateKeyStr(Map<String, Object> keyMap) {
		return Base64.encode(getPrivateKey(keyMap));
	}

	/**
	 * 取得私钥 String
	 * 
	 * @param key
	 * @return
	 */
	@Deprecated
	public static String getPrivateKey(byte[] key) {
		return Base64.encode(key);
	}

	/**
	 * 取得公钥
	 * 
	 * @param keyMap
	 *            密钥map
	 * @return byte[] 公钥
	 */
	protected static byte[] getPublicKey(Map<String, Object> keyMap) {
		Key key = (Key) keyMap.get(PUBLIC_KEY);
		return key.getEncoded();
	}

	/**
	 * 获得公钥
	 * 
	 * @param keyMap
	 * @return
	 */
	public static String getPublicKeyStr(Map<String, Object> keyMap) {
		return Base64.encode(getPublicKey(keyMap));
	}

	/**
	 * 获得公钥
	 * 
	 * @param key
	 * @return
	 */
	@Deprecated
	public static String getPublicKey(byte[] key) {

		return Base64.encode(key);
	}

	/**
	 * 公钥加密方法 分段加密 返回加密后的字符串
	 * 
	 * @param encryptData
	 *            待加密的字符串
	 * @param cerPath
	 *            证书路径
	 * @return
	 */
	public static byte[] encryption(String encryptData, String cerPath) {
		try {
			if (StringUtils.isEmpty(encryptData)) {
				log.error("待解密数据为空");
				return null;
			}
			if (StringUtils.isEmpty(cerPath)) {
				log.error("证书路径为空");
				return null;
			}
			return encryption(encryptData.getBytes(), cerPath);
		} catch (UnrecoverableKeyException e) {
			log.error("获取公钥失败：" + e.getMessage(), e);
		} catch (InvalidKeyException e) {
			log.error("公钥无效：" + e.getMessage(), e);
		} catch (KeyStoreException e) {
			log.error("秘钥库异常:" + e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error("NoSuchAlgorithmException:" + e.getMessage(), e);
		} catch (CertificateException e) {
			log.error("证书异常：" + e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			log.error("补位异常：" + e.getMessage(), e);
		} catch (IOException e) {
			log.error("IO流异常：" + e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			log.error("BlockSize异常：" + e.getMessage(), e);
		} catch (BadPaddingException e) {
			log.error("补位异常：" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 解密方法 返回解密后的字符串
	 * 
	 * @param decryptData
	 *            带解密字符串
	 * @param cerPath
	 *            证书路径
	 * @param keystorePassword
	 *            证书库密码
	 * @param cerPassword
	 *            证书密码
	 * @return
	 */
	public static String cerDecryption(byte[] decryptData, String cerPath, String keystorePassword, String cerPassword,
			String storeKey) {
		try {
			if (StringUtils.isEmpty(cerPath)) {
				log.error("证书路径为空");
				return null;
			}
			if (StringUtils.isEmpty(keystorePassword)) {
				log.error("私钥密码为空");
				return null;
			}
			if (StringUtils.isEmpty(cerPassword)) {
				log.error("公钥密码为空");
				return null;
			}
			if (StringUtils.isEmpty(storeKey)) {
				log.error("秘钥库别名位空");
				return null;
			}
			byte[] res = decryption(decryptData, cerPath, keystorePassword, cerPassword, storeKey);
			return new String(res, "UTF8");
		} catch (UnrecoverableKeyException e) {
			log.error("获取私钥失败：" + e.getMessage(), e);
		} catch (InvalidKeyException e) {
			log.error("私钥无效：" + e.getMessage(), e);
		} catch (KeyStoreException e) {
			log.error("秘钥库异常:" + e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error("NoSuchAlgorithmException:" + e.getMessage(), e);
		} catch (CertificateException e) {
			log.error("证书异常：" + e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			log.error("补位异常：" + e.getMessage(), e);
		} catch (IOException e) {
			log.error("IO流异常：" + e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			log.error("BlockSize异常：" + e.getMessage(), e);
		} catch (BadPaddingException e) {
			log.error("补位异常：" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 解密方法 返回字节数组
	 * 
	 * @param decryptData
	 * @param cerPath
	 * @param keystorePassword
	 * @param cerPassword
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static byte[] decryption(byte[] decryptData, String cerPath, String keystorePassword, String cerPassword,
			String storeKey) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		// 用证书的私钥解密 - 该私钥存在生成该证书的密钥库中
		FileInputStream fis = new FileInputStream(cerPath);
		KeyStore ks = KeyStore.getInstance("JKS"); // 加载证书库
		char[] kspwd = keystorePassword.toCharArray(); // 证书库密码
		char[] keypwd = cerPassword.toCharArray(); // 证书密码
		ks.load(fis, kspwd); // 加载证书
		PrivateKey pk = (PrivateKey) ks.getKey(storeKey, keypwd); // 获取证书私钥
		fis.close();
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, pk);
		int blockSize = cipher.getOutputSize(decryptData.length);
		return doFinal(decryptData, cipher,blockSize);
	}

	/**
	 * 证书加密方法
	 * 
	 * @param encryptData
	 *            待加密字节
	 * @param cerPath
	 *            证书路径
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static byte[] encryption(byte[] encryptData, String cerPath) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// 用证书的公钥加密
		CertificateFactory cff = CertificateFactory.getInstance("X.509");
		FileInputStream fis = new FileInputStream(cerPath); // 证书文件
		Certificate cf = cff.generateCertificate(fis);
		PublicKey pk = cf.getPublicKey(); // 得到证书文件携带的公钥
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // 定义算法：RSA
		cipher.init(Cipher.ENCRYPT_MODE, pk);
		int blockSize = cipher.getOutputSize(encryptData.length) - 11;
		return doFinal(encryptData, cipher,blockSize);
	}

	/**
	 * 加密解密共用核心代码，分段加密解密
	 * 
	 * @param decryptData
	 * @param cipher
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws IOException
	 */
	public static byte[] doFinal(byte[] decryptData, Cipher cipher,int blockSize)
			throws IllegalBlockSizeException, BadPaddingException, IOException {
		int offSet = 0;
		byte[] cache = null;
		int i = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (decryptData.length - offSet > 0) {
			if (decryptData.length - offSet > blockSize) {
				cache = cipher.doFinal(decryptData, offSet, blockSize);
			} else {
				cache = cipher.doFinal(decryptData, offSet, decryptData.length - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * blockSize;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

}
