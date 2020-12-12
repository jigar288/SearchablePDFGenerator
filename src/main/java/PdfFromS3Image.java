import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.amazon.textract.pdf.ImageType;
import com.amazon.textract.pdf.PDFDocument;
import com.amazon.textract.pdf.TextLine;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.BoundingBox;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;

// FIXME: JUST MAKE S3 CLIENT & CREDS PART OF THE CLASS CONSTRUCTOR

public class PdfFromS3Image {

    private List<TextLine> extractText(String bucketName, String documentName, AWSCredentials credentials){
        
        AmazonTextract client = AmazonTextractClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_2).build();   

        DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                .withDocument(new Document()
                        .withS3Object(new S3Object()
                                .withName(documentName)
                                .withBucket(bucketName)));

        DetectDocumentTextResult result = client.detectDocumentText(request);

        List<TextLine> lines = new ArrayList<TextLine>();
        List<Block> blocks = result.getBlocks();
        BoundingBox boundingBox = null;
        for (Block block : blocks) {
            if ((block.getBlockType()).equals("LINE")) {
                boundingBox = block.getGeometry().getBoundingBox();
                lines.add(new TextLine(boundingBox.getLeft(),
                        boundingBox.getTop(),
                        boundingBox.getWidth(),
                        boundingBox.getHeight(),
                        block.getText()));
            }
        }

        System.out.println("extractText ran successfully");

        return lines;
    }

    private BufferedImage getImageFromS3(String bucketName, String documentName, AmazonS3 s3client) throws IOException {

        com.amazonaws.services.s3.model.S3Object fullObject = s3client.getObject(new GetObjectRequest(bucketName, documentName));
        BufferedImage image = ImageIO.read(fullObject.getObjectContent());
        return image;
    }

    private void UploadToS3(String bucketName, String objectName, String contentType, byte[] bytes, AmazonS3 s3client) throws IOException {        
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectName, baInputStream, metadata);
        s3client.putObject(putRequest);
    }

    public void run(AWSInformation awsInformation) throws IOException {

        System.out.println("Generating searchable pdf from: " + awsInformation.getInputBucket() + "/" + awsInformation.getDocumentName());

        ImageType imageType = ImageType.JPEG;
        if(awsInformation.getDocumentName().toLowerCase().endsWith(".png"))
            imageType = ImageType.PNG;
   

        //Extract text
        List<TextLine> lines = extractText(awsInformation.getInputBucket(), awsInformation.getDocumentName(), awsInformation.getCredentials());

        //Get image from S3
        BufferedImage image = getImageFromS3(awsInformation.getInputBucket(), awsInformation.getDocumentName(), awsInformation.getS3client());

        //Create PDF document
        PDFDocument pdfDocument = new PDFDocument();

        //Add page with text layer and image in the pdf document
        pdfDocument.addPage(image, imageType, lines);

        //Save PDF to stream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        pdfDocument.save(os);
        pdfDocument.close();

        
        //Upload PDF to S3
        UploadToS3(awsInformation.getOutputBucket(), awsInformation.getOutputDocumentName(), "application/pdf", os.toByteArray(), awsInformation.getS3client());

        System.out.println("Generated searchable pdf: " + awsInformation.getOutputBucket() + "/" + awsInformation.getOutputDocumentName());
    }
}
