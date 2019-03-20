package org.unitedata.consumer.feature.genupload;

import lombok.Data;
import org.unitedata.consumer.*;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.util.ProofParserParser;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @author: sanbanfu
 * @create: 2019/03/14
 */
public class BuildProofDataToolTask extends AbstractToolTask{

    private ProofParserParser proofParserParser = ProofParserParser.INSTANCE;


    public BuildProofDataToolTask(PipelineNode node) {
        super(node);
    }

    @Override
    protected Object process(Object in) throws TaskToolException {
        if(in == null){
            return null;
        }
        String s = (String)in;
        return proofParserParser.toProofData(s);
    }

}
