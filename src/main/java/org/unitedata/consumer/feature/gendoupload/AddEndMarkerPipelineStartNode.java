package org.unitedata.consumer.feature.gendoupload;

import org.unitedata.consumer.Main;
import org.unitedata.consumer.PipelineStartNode;

import java.util.function.Predicate;

/**
 * @author: hushi
 * @create: 2019/03/15
 */
public class AddEndMarkerPipelineStartNode extends PipelineStartNode{
    public AddEndMarkerPipelineStartNode(Predicate<String> predicate, Main mainParam) {
        super(predicate, mainParam);
    }

    @Override
    protected void postRead() {
        try {
            Main.INPUT_FILE_LINES.put(Main.INPUT_QUEUE_END_MARKER);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
