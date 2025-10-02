package com.example.NoteKeep;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetectText {

    private static final Logger logger = LoggerFactory.getLogger(DetectText.class);

    public static String detectTextFromBytes(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) return "";

        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath == null || credentialsPath.isBlank()) {
            logger.warn("GOOGLE_APPLICATION_CREDENTIALS is not configured; skipping OCR");
            return "";
        }

        List<AnnotateImageRequest> requests = new ArrayList<>();
        ByteString imgBytes = ByteString.copyFrom(bytes);
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        StringBuilder out = new StringBuilder();
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    throw new IOException("Vision API error: " + res.getError().getMessage());
                }
                List<EntityAnnotation> annotations = res.getTextAnnotationsList();
                if (annotations != null && !annotations.isEmpty()) {
                    out.append(annotations.get(0).getDescription());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to call Google Vision API", e);
            throw e;
        }
        return out.toString();
    }
}
