package munna.hometech.quizlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ScoreActivity extends AppCompatActivity {

    private TextView tvScored,tvTotal;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        loadAds();

        tvScored = findViewById(R.id.tv_scored);
        tvTotal = findViewById(R.id.tv_total);
        btnDone = findViewById(R.id.btn_done);

        tvScored.setText(String.valueOf(getIntent().getIntExtra("score", 0)));
        tvTotal.setText("OUT OF"+ " "+ String.valueOf(getIntent().getIntExtra("total", 0)));

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadAds() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }
}