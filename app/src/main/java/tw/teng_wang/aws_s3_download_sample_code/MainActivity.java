package tw.teng_wang.aws_s3_download_sample_code;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String awsAccessKey = "Your-awsAccessKey";
        String awsSecretKey = "Your-awsSecretKey";
        String sessionToken = "Your-sessionToken";

        AWSSessionCredentials awsCred = new BasicSessionCredentials(awsAccessKey
                , awsSecretKey, sessionToken);

        String fileKey = "Your-fileKey";
        downloadWithTransferUtility(fileKey, awsCred);
    }

    private void downloadWithTransferUtility(String fileKey, AWSSessionCredentials awsCred) {
        TransferNetworkLossHandler.getInstance(getApplicationContext());
        AmazonS3Client s3Client = new AmazonS3Client(awsCred, Region.getRegion(Regions.AP_NORTHEAST_1), new ClientConfiguration());

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(s3Client)
                        .defaultBucket("Your-BucketName")
                        .build();

        String fileName = getDownloadFileName(fileKey);
        TransferObserver downloadObserver =
                transferUtility.download(fileKey,
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + fileName));

        // Attach a listener to the observer to get state update and progress notifications
        downloadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Toast.makeText(MainActivity.this, "TransferState.COMPLETED", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
                Log.d("Your Activity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getDownloadFileName(String key) {
        String[] path_split = key.split("/");
        return path_split[path_split.length - 1];
    }
}
