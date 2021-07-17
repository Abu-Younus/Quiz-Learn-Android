package munna.hometech.quizlearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class HomeActivity extends AppCompatActivity {

    private Button btnStartQuiz, btnBookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnStartQuiz = findViewById(R.id.btn_start_quiz);
        btnBookmarks = findViewById(R.id.btn_bookmarks);

        MobileAds.initialize(this);

        loadAds();

        btnBookmarks.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkGrey)));

        btnStartQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent categoriesIntent = new Intent(HomeActivity.this, CategoriesActivity.class);
                startActivity(categoriesIntent);
            }
        });

        btnBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bookmarksIntent = new Intent(HomeActivity.this, BookmarkActivity.class);
                startActivity(bookmarksIntent);
            }
        });
    }

    private void loadAds() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}