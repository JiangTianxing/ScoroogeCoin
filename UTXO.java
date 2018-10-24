import java.util.Arrays;

/**
 * Unspent Transaction Output
 * 未被使用的交易输出
 */
public class UTXO implements Comparable<UTXO> {

    // 此UTXO源自的事务的哈希值
    private byte[] txHash;

    // 所述交易相应输出的索引
    private int index;

    /**
     * 创建 在Hash值为txHash的交易中，索引为index的相应输出的 UTXO对象
     * @param txHash hash值
     * @param index  索引
     */
    public UTXO(byte[] txHash, int index) {
        this.txHash = Arrays.copyOf(txHash, txHash.length);
        this.index = index;
    }

    /**
     * 返回当前UTXO对象所在的交易的hash值
     * @return byte[]
     */
    public byte[] getTxHash() {
        return txHash;
    }

    /**
     * 返回当前UTXO对象在交易相应输出中对应的索引
     * @return int
     */
    public int getIndex() {
        return index;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        UTXO utxo = (UTXO) other;
        byte[] hash = utxo.txHash;
        int in = utxo.index;
        if (hash.length != txHash.length || index != in)
            return false;
        for (int i = 0; i < hash.length; i++) {
            if (hash[i] != txHash[i])
                return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + index;
        hash = hash * 31 + Arrays.hashCode(txHash);
        return hash;
    }

    public int compareTo(UTXO utxo) {
        byte[] hash = utxo.txHash;
        int in = utxo.index;
        if (in > index)
            return -1;
        else if (in < index)
            return 1;
        else {
            int len1 = txHash.length;
            int len2 = hash.length;
            if (len2 > len1)
                return -1;
            else if (len2 < len1)
                return 1;
            else {
                for (int i = 0; i < len1; i++) {
                    if (hash[i] > txHash[i])
                        return -1;
                    else if (hash[i] < txHash[i])
                        return 1;
                }
                return 0;
            }
        }
    }
}