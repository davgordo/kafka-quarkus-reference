package com.redhat.consulting;

import org.eclipse.microprofile.reactive.messaging.Channel;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/test")
public class TestResource {
    private static final Logger LOGGER = Logger.getLogger(TestResource.class);

    final TestResults testResults = new TestResults();

    @Channel("movies")
    Emitter<Movie> emitter;

    @POST
    public Response runTest(Test test) {
        LOGGER.infof("Sending test id %s with %s messages to Kafka", test.getId(), test.getMessages());

        int i = 0;

        for(i=0; i < test.getMessages(); i++) {
            try {
                emitter
                        .send(new Movie("Office Space", 1999))
                        .whenComplete((success, failure) -> {
                            if (failure != null) {
                                testResults.incrementFailedMessages();
                            } else {
                                testResults.incrementProcessedMessages();
                            }
                        });
            }
            catch(Exception e) {
                LOGGER.infof("Failed to send message: %s", e.getMessage());
            }
        }

        return Response.accepted().build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getCurrentCounters() {
        String response = String.format("Processed: %d, Failed: %d, Total Processed: %d, Total Failed %d\n",
                testResults.getProcessedMessages(), testResults.getFailedMessages(), testResults.getTotalProcessed(), testResults.getTotalFailed());
        testResults.reset();

        return response;
    }


    public static class TestResults {
        private int processedMessages = 0;
        private int failedMessages = 0;

        private int totalProcessed = 0;
        private int totalFailed = 0;

        public int getProcessedMessages() {
            return processedMessages;
        }

        public void setProcessedMessages(int processedMessages) {
            this.processedMessages = processedMessages;
        }

        public int getFailedMessages() {
            return failedMessages;
        }

        public void setFailedMessages(int failedMessages) {
            this.failedMessages = failedMessages;
        }

        public int getTotalProcessed() {
            return totalProcessed;
        }

        public void setTotalProcessed(int totalProcessed) {
            this.totalProcessed = totalProcessed;
        }

        public int getTotalFailed() {
            return totalFailed;
        }

        public void setTotalFailed(int totalFailed) {
            this.totalFailed = totalFailed;
        }

        public void incrementProcessedMessages() {
            this.processedMessages++;
            this.totalProcessed++;
        }

        public void incrementFailedMessages() {
            this.failedMessages++;
            this.totalFailed++;
        }

        public void reset() {
            this.processedMessages = 0;
            this.failedMessages = 0;
        }
    }
}
