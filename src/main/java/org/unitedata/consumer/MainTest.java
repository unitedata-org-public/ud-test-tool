package org.unitedata.consumer;

import org.bitcoinj.core.Sha256Hash;
import org.junit.Test;
import org.unitedata.eos.crypto.EosSignatureProcessor;
import org.unitedata.eos.crypto.KeyFormatTransformer;
import org.unitedata.eos.utils.SignatureUtil;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    public  void shit() throws Exception{

        String plainText = "{\"anonymousFactor\":1553143484074,\"proofs\":[{\"staticProof\":\"6492E44B0BE977345E464B3CDADA215F\",\"staticFactor\":3484042418913769472,\"dynamicProof\":\"DC3D558D26CD6009BD0C06AFFD8C7E7F\",\"token\":\"7179565eb847970fdf8916c0c595012849ae79c41c9eccf6302c5b9cc7cd010e44b9ada805c69693df5dd417aa813cca711821f4963126a7f8add4c72d3f8ff2244329ef464bb1d20fc783f618a667829c064a63c798317977a48848e8fba2d0\",\"uploadedTimestamp\":1553139307619}],\"returningTimestamp\":1553143484074,\"reportSummary\":\"A44796F1E45DFB1437A7B824F53A736B\",\"requestedProof\":\"67E2438C3480396B12878496F3489398\"}";
        String eoskey = "EOS88aYmZo7YEdjCw1JJru3r5NWsaUq9rbAqJqUKTRHnraFiQ6Xj2";
        String sig = "SIG_K1_K7k9C8L8gW1n4ZR6r2yYhvzicc21acFXRfuDBsQ9GDFPL2NHisCSNdJWeN4QpH8DBUhH45MRC9aCfCeB86aFij9a3WQBhw";
        SignatureUtil.verify(eoskey, plainText, sig);

/*
        String wif = "5KckjQE7nAbS8RoMuKh6h2vUQFH6zGyH713zM7SGfTbmShFXjE5";
        String eoskey = new KeyFormatTransformer().fromWifToEosKey(wif);
        System.out.println(eoskey);

        String plainText = "{\"anonymousFactor\":1553143484074,\"proofs\":[{\"staticProof\":\"6492E44B0BE977345E464B3CDADA215F\",\"staticFactor\":3484042418913769472,\"dynamicProof\":\"DC3D558D26CD6009BD0C06AFFD8C7E7F\",\"token\":\"7179565eb847970fdf8916c0c595012849ae79c41c9eccf6302c5b9cc7cd010e44b9ada805c69693df5dd417aa813cca711821f4963126a7f8add4c72d3f8ff2244329ef464bb1d20fc783f618a667829c064a63c798317977a48848e8fba2d0\",\"uploadedTimestamp\":1553139307619}],\"returningTimestamp\":1553143484074,\"reportSummary\":\"A44796F1E45DFB1437A7B824F53A736B\",\"requestedProof\":\"67E2438C3480396B12878496F3489398\"}";
        String sig = new EosSignatureProcessor().signHash(Sha256Hash.create(plainText.getBytes()), new KeyFormatTransformer().fromWifToPrivateKey(wif));
        System.out.println(sig);

        boolean varified = new EosSignatureProcessor().verifyHash(Sha256Hash.create(plainText.getBytes()).getBytes(), sig, eoskey);
        System.out.println(varified);
*/
    }

}