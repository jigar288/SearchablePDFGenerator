import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;

public class AWSInformation {
    
    private String inputBucket;
    private String outputBucket;
    private String documentName;
    private String outputDocumentName;
    private AWSCredentials credentials;
    private AmazonS3 s3client;


    public String getInputBucket() {
        return inputBucket;
    }

    public void setInputBucket(String inputBucket) {
        this.inputBucket = inputBucket;
    }

    public String getOutputBucket() {
        return outputBucket;
    }

    public void setOutputBucket(String outputBucket) {
        this.outputBucket = outputBucket;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public AWSCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    public AmazonS3 getS3client() {
        return s3client;
    }

    public void setS3client(AmazonS3 s3client) {
        this.s3client = s3client;
    }



    public String getOutputDocumentName() {
        return outputDocumentName;
    }

    public void setOutputDocumentName(String outputDocumentName) {
        this.outputDocumentName = outputDocumentName;
    }

    public AWSInformation(String inputBucket, String outputBucket, String documentName, String outputDocumentName,
            AWSCredentials credentials, AmazonS3 s3client) {
        this.inputBucket = inputBucket;
        this.outputBucket = outputBucket;
        this.documentName = documentName;
        this.outputDocumentName = outputDocumentName;
        this.credentials = credentials;
        this.s3client = s3client;
    }

    

}
