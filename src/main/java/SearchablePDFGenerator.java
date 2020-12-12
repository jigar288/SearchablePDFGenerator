import java.io.*;
import java.util.Scanner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ObjectListing;

public class SearchablePDFGenerator {

    private AWSCredentials credentials;
    private AmazonS3 s3client;
    private ObjectListing s3objectListing;

    public SearchablePDFGenerator() {
        this.credentials = new BasicAWSCredentials(AWSAccountInfo.accessKey, AWSAccountInfo.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_2).build();
    }

    private void retrieveS3Objects() {
        this.s3objectListing = this.s3client.listObjects(AWSAccountInfo.sourceBucketName);
    }

    private void convertObjectsToPDF() throws IOException, InterruptedException {
        int i = 0;
        for (S3ObjectSummary os : this.s3objectListing.getObjectSummaries()) {

            String longFilePath = os.getKey();
            int lastSlash = longFilePath.lastIndexOf("/");
            String fileName = longFilePath.substring(lastSlash + 1, longFilePath.length());
            System.out.println("Working on file: " + fileName);

            String outputFileName = Integer.toString(i) + "SampleOutput.pdf";

            // Generate searchable PDF from image in Amazon S3 bucket

            PdfFromS3Image s3Image = new PdfFromS3Image();
            AWSInformation awsInformation = new AWSInformation(AWSAccountInfo.sourceBucketName, AWSAccountInfo.destinationBucket, fileName, outputFileName, this.credentials, this.s3client);        
            s3Image.run(awsInformation);
                        
            Thread.sleep(1000); 
            i++;
        }
    }

    

    private void convertS3ImagesToSearchablePDFs(){
        try {            
            retrieveS3Objects();
            convertObjectsToPDF();        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String args[]) {

        // TODO: create a readme 
        /**
         * 
         * - with project description
         * - sample input & output
         * - how to run
         * - security warning - aws creds will be safe
         * - aws tech used
         * - explain how it works w/ aws = s3 & textract
         * - explain to use it
         * 
         */

        System.out.println("Welcome to Searchable PDF Generator. This project takes either images and PDFs and turns them into keyword searchable PDF documents. Please enter your aws account info.");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your AWS access key: ");
        AWSAccountInfo.accessKey = scanner.nextLine();

        System.out.println("Enter your AWS secret key: ");
        AWSAccountInfo.secretKey = scanner.nextLine();

        System.out.println("Enter your s3 source bucket name: ");
        AWSAccountInfo.sourceBucketName = scanner.nextLine();
        
        System.out.println("Enter your s3 destination bucket name: ");
        AWSAccountInfo.destinationBucket = scanner.nextLine();

        scanner.close(); //closing scanner to avoid any resource leaks

        SearchablePDFGenerator generator = new SearchablePDFGenerator();
        generator.convertS3ImagesToSearchablePDFs();

    }
    
    public static class AWSAccountInfo {        
        public static String accessKey = "";
        public static String secretKey = "";
        public static String sourceBucketName = "";
        public static String destinationBucket = "";           
    }
        
}
