/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.com.cuatronetworks.sensoresbpdp;

import java.io.IOException;
import java.io.InputStream;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
 
import javazoom.jl.decoder.JavaLayerException;
import mx.com.cuatronetworks.sensoresbpdp.model.AudioPlayer;
import software.amazon.awssdk.services.polly.model.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

/**
 *
 * @author david
 */
public class TextToSpeech {
    
    private final PollyClient polly;

    AwsCredentialsProvider awsCredentialsProvider;
    
    
    public TextToSpeech(Region region){
        // create an Amazon Polly client in a specific region
        // Use your access key id and access secret key
        // Obtain it from AWS console
        //
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create("AKIAURYO3RK56DA2WC3I","X2moynxt6jDnJW/t9fkcRGZtso37BHh83dNqIR0J");
        awsCredentialsProvider = StaticCredentialsProvider.create(awsCredentials);
                                //new AWSBasicCredentials("AKIAURYO3RK56DA2WC3I","X2moynxt6jDnJW/t9fkcRGZtso37BHh83dNqIR0J");
        //
        // Create an Amazon Polly client in a specific region
        //
        this.polly = PollyClient.builder().region(Region.US_EAST_1).credentialsProvider(awsCredentialsProvider).build();
        //this.polly = AmazonPollyClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).withRegion(region.getName()).build();
   

    }
    public void play(String text) throws IOException, JavaLayerException {
        //
        // Get the audio stream created using the text
        //

        InputStream speechStream = this.synthesize(text, this.getVoice() ,OutputFormat.MP3);
        //
        // Play the audio
        //
        AudioPlayer.play(speechStream);
    }
 
    @SuppressWarnings("empty-statement")
    public InputStream synthesize(String text, Voice voice,OutputFormat format) throws IOException {
        //
        // Get the default voice
        //
        //Voice voice = this.getVoice();
        //voice.setId("Lupe");
       /* SynthesizeSpeechRequest synthReq = 
		new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId()).withOutputFormat(format).withEngine("neural");
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);*/
        //
        // Create speech synthesis request comprising of information such as following:
        // Text
        // Voice
        // The detail will be used to create the speech
        //
        SynthesizeSpeechRequest synthesizeSpeechRequest = SynthesizeSpeechRequest.builder().text(text).voiceId(voice.id()).outputFormat(format).build();
        //SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId()).withOutputFormat(format);
        //
        // Create the speech
        //
        ResponseInputStream<SynthesizeSpeechResponse> synthRes = this.polly.synthesizeSpeech(synthesizeSpeechRequest);
        return synthRes;
        //SynthesizeSpeechResult synthRes = this.polly.synthesizeSpeech(synthReq);
        //
        // Returns the audio stream
        //
        //return synthRes.getAudioStream();
    }
 
    public Voice getVoice() {
        //
        // Create describe voices request.
        //
        DescribeVoicesRequest voicesRequest = DescribeVoicesRequest.builder().languageCode("es-MX").build();
        DescribeVoicesResponse esMxVoicesResult = this.polly.describeVoices(voicesRequest);
        return esMxVoicesResult.voices().get(0);
        /*
        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
        // Synchronously ask Amazon Polly to describe available TTS voices.
        DescribeVoicesResult describeVoicesResult = this.polly.describeVoices(describeVoicesRequest);
        return describeVoicesResult.getVoices().get(0);*/
    }
}
