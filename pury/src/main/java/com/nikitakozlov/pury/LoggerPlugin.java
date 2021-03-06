package com.nikitakozlov.pury;

import com.nikitakozlov.pury.profile.ProfilerId;
import com.nikitakozlov.pury.result.ResultVisitor;
import com.nikitakozlov.pury.result.model.AverageProfileResult;
import com.nikitakozlov.pury.result.model.AverageTime;
import com.nikitakozlov.pury.result.model.ProfileResult;
import com.nikitakozlov.pury.result.model.RootAverageProfileResult;
import com.nikitakozlov.pury.result.model.RootSingleProfileResult;
import com.nikitakozlov.pury.result.model.SingleProfileResult;

import java.util.Locale;

import static com.nikitakozlov.pury.Pury.LOG_TAG;

public class LoggerPlugin implements Plugin {

    private static final String RIGHT_ARROW = " --> ";
    private static final String LEFT_ARROW = " <-- ";
    private static final String MS = "ms";
    private static final String EXEC_TIME = "execution = ";
    private static final int MS_TO_NS = 1000000;
    private static final String DEPTH_PREFIX = "  ";

    private static final String AVERAGE = "avg = ";
    private static final String MIN = "min = ";
    private static final String MAX = "max = ";
    private static final String FOR = "for ";
    private static final String RUNS = " runs";

    @Override
    public void handleResult(ProfileResult result, ProfilerId profilerId) {
        Visitor visitor = new Visitor();
        result.accept(visitor);

        StringBuilder logMessage = new StringBuilder("Profiling results");
        String methodId = profilerId.getProfilerName();
        if (!methodId.isEmpty()) {
            logMessage.append(" for ")
                    .append(methodId);
        }
        logMessage.append(":\n")
                .append(visitor.getLogMessage());
        Pury.getLogger().result(LOG_TAG, logMessage.toString());
    }


    private static class Visitor implements ResultVisitor {

        private final StringBuilder logMessageBuilder = new StringBuilder();

        @Override
        public void visit(AverageProfileResult averageProfileResult) {

            appendFullDepthPrefix(logMessageBuilder, averageProfileResult.getDepth());
            logMessageBuilder.append(averageProfileResult.getStageName())
                    .append(RIGHT_ARROW);
            averageTimeToString(averageProfileResult.getStartTime(), logMessageBuilder);
            logMessageBuilder.append("\n");
            for (ProfileResult result : averageProfileResult.getNestedResults()) {
                result.accept(this);
                logMessageBuilder.append("\n");
            }
            appendFullDepthPrefix(logMessageBuilder, averageProfileResult.getDepth());
            logMessageBuilder.append(averageProfileResult.getStageName())
                    .append(LEFT_ARROW);
            averageTimeToString(averageProfileResult.getExecTime(), logMessageBuilder);
        }

        @Override
        public void visit(RootAverageProfileResult rootAverageProfileResult) {
            logMessageBuilder.append(rootAverageProfileResult.getStageName())
                    .append(" --> 0ms\n");
            for (ProfileResult result : rootAverageProfileResult.getNestedResults()) {
                result.accept(this);
                logMessageBuilder.append("\n");
            }
            logMessageBuilder.append(rootAverageProfileResult.getStageName())
                    .append(LEFT_ARROW);
            averageTimeToString(rootAverageProfileResult.getExecTime(), logMessageBuilder);
        }

        @Override
        public void visit(RootSingleProfileResult rootSingleProfileResult) {

            logMessageBuilder.append(rootSingleProfileResult.getStageName())
                    .append(" --> 0ms\n");
            for (ProfileResult result : rootSingleProfileResult.getNestedResults()) {
                result.accept(this);
                logMessageBuilder.append("\n");
            }
            logMessageBuilder.append(rootSingleProfileResult.getStageName())
                    .append(LEFT_ARROW)
                    .append(rootSingleProfileResult.getExecTime() / MS_TO_NS)
                    .append(MS);
        }

        @Override
        public void visit(SingleProfileResult singleProfileResult) {

            appendFullDepthPrefix(logMessageBuilder, singleProfileResult.getDepth());
            logMessageBuilder.append(singleProfileResult.getStageName())
                    .append(RIGHT_ARROW)
                    .append(singleProfileResult.getStartTime() / MS_TO_NS)
                    .append("ms\n");
            for (ProfileResult result : singleProfileResult.getNestedResults()) {
                result.accept(this);
                logMessageBuilder.append("\n");
            }
            appendFullDepthPrefix(logMessageBuilder, singleProfileResult.getDepth());
            logMessageBuilder.append(singleProfileResult.getStageName())
                    .append(LEFT_ARROW)
                    .append((singleProfileResult.getStartTime() + singleProfileResult.getExecTime()) / MS_TO_NS)
                    .append("ms, ")
                    .append(EXEC_TIME)
                    .append(singleProfileResult.getExecTime() / MS_TO_NS)
                    .append(MS);
        }

        String getLogMessage() {
            return logMessageBuilder.toString();
        }

        private void appendFullDepthPrefix(StringBuilder sb, int depth) {
            for (int i = 0; i < depth; i++) {
                sb.append(DEPTH_PREFIX);
            }
        }

        private void averageTimeToString(AverageTime time, StringBuilder stringBuilder) {
            stringBuilder.append(AVERAGE)
                    .append(String.format(Locale.US, "%.2f", time.getAverageValue() / MS_TO_NS))
                    .append(MS)
                    .append(", ")

                    .append(MIN)
                    .append(time.getMinValue() / MS_TO_NS)
                    .append(MS)
                    .append(", ")

                    .append(MAX)
                    .append(time.getMaxValue() / MS_TO_NS)
                    .append(MS)
                    .append(", ")

                    .append(FOR)
                    .append(time.getMeasurementCounter())
                    .append(RUNS);
        }
    }
}
