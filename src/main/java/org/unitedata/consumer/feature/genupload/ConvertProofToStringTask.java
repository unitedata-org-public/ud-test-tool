package org.unitedata.consumer.feature.genupload;

import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.feature.entity.ProofData;
import org.unitedata.consumer.util.ProofFormatParser;

public class ConvertProofToStringTask extends AbstractToolTask {

    ProofFormatParser parser = new ProofFormatParser();

    public ConvertProofToStringTask(PipelineNode node) {
        super(node);
    }

    @Override
    protected String process(Object obj) throws TaskToolException {
        ProofData proofData = (ProofData)obj;
        if(proofData == null){
            return null;
        }

        return parser.toString(proofData);
    }


}
