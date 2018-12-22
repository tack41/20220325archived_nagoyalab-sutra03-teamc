package xuzhongwei.gunsecury;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    BluetoothDevice mBluetoothDevice;
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            Log.d("FIGHTING","1111111");
        }else{
            Log.d("FIGHTING",savedInstanceState.toString());
        }

        setContentView(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.layout.welcome);
        Button welcomeButton = (Button) findViewById(com.slack.nagoyalab_sutra03.teamc.mimamorukun.R.id.welcomeButton);
        welcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScanActivity();
            }
        });
        Intent intent = getIntent();
        mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        mActivity = this;
    }

    private void goToScanActivity(){
        Intent intent = new Intent(this,DeviceScanActivity.class);
        startActivity(intent);
    }





}

