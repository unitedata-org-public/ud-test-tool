package org.unitedata.consumer.feature.genupload;

import org.unitedata.consumer.*;
import org.unitedata.consumer.util.ProofFormatParser;

/**
 * @author: hushi
 * @author: sanbanfu
 * @create: 2019/03/14
 */
public class BuildProofDataToolTask extends AbstractToolTask{

    private ProofFormatParser proofParserParser = new ProofFormatParser();


    public BuildProofDataToolTask(PipelineNode node) {
        super(node);
    }

    @Override
    protected Object process(Object in) throws TaskToolException {
        if(in == null){
            return null;
        }
        String s = (String)in;
        return proofParserParser.fromPlainText(s);
    }

}
