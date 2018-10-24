import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) 当前交易的每一笔输入所指向的输入应该都未被使用
     * (2) 当前交易的每一笔输入都为合法输入，其数字签名验证均为合法
     * (3) 同一个未被交易的输出不可被使用两次
     * (4) 所有交易的输出不可以为负数
     * (5) 所有交易的输入总和 应 大于等于 输出总和
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     */
    public boolean isValidTx(Transaction tx) {
        double sumOfOut = 0;
        double sumOfIn = 0;
        Set<UTXO> used = new HashSet<>();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            //此时有效的输入应等于有效的为被交易的输出
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            //如果不存在这笔未被交易的输出，那么此交易输入不存在
            if (!utxoPool.contains(utxo)) return false;
            //获取此输入的上一输出
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            //验证币的所有者的有效签署，public key、message、signature
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) return false;
            //验证此utxo是否已经被使用，避免双花问题
            if (!used.contains(utxo)) return false;
            used.add(utxo);
            //交易输入累加
            sumOfIn += output.value;
        }
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) return false;
            //交易输出累加
            sumOfOut += output.value;
        }
        //交易的输入应大于或等于输出
        //输入 = 输出 + 交易费 （交易费 >= 0）
        return !(sumOfIn < sumOfOut);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        HashSet<Transaction> txVis = new HashSet<>();
        while (true) {
            boolean updated = false;
            for (Transaction tx : possibleTxs) {
                if (txVis.contains(tx)) continue;
                if (isValidTx(tx)) {
                    txVis.add(tx);
                    updated = true;
                    //将此交易产生的未被使用的交易输出放入池中
                    for (int i = 0; i < tx.numOutputs(); ++i) {
                        UTXO utxo = new UTXO(tx.getHash(), i);
                        utxoPool.addUTXO(utxo, tx.getOutput(i));
                    }
                    //将此交易消耗掉的未被使用的交易输出从池中移除
                    for (int i = 0; i < tx.numInputs(); ++i) {
                        Transaction.Input input = tx.getInput(i);
                        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                        utxoPool.removeUTXO(utxo);
                    }
                }
            }
            //如果当前没有其他交易待处理，或是存在不合法交易则退出
            if(!updated) break;
        }
        Transaction[] ret = new Transaction[txVis.size()];
        int idx =0;
        for(Transaction tx : txVis)
            ret[idx++] = tx;
        return ret;
    }


}
