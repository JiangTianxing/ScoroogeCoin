import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Crypto {
    /**
     * 检验数字签名
     * @param pubKey    公钥
     * @param message   信息
     * @param signature 签名
     * @return
     */
    public static boolean verifySignature(PublicKey pubKey, byte[] message, byte[] signature) {
        Signature sig = null;
        try {
            sig = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.initVerify(pubKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            sig.update(message);
            return sig.verify(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }
}
