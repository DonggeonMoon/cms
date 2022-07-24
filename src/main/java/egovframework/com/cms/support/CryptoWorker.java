package egovframework.com.cms.support;

public interface CryptoWorker {
    String encrypt(String string);

    String decrypt(String string);
}
