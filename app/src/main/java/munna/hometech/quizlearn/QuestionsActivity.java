package munna.hometech.quizlearn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionsActivity extends AppCompatActivity {

    public static final String FILE_NAME = "QUIZLEARN";
    public static final String KEY_NAME = "QUESTIONS";

    private TextView tvQuestion, tvNoIndicator;
    private LinearLayout optionsContainer;
    private Button btnShare, btnNext;
    private FloatingActionButton btnBookmarks;

    private int count = 0;
    private int position = 0;
    private int score = 0;
    private List<QuestionModel> questionModelList;

    private String setId;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private Dialog loadingDialog;
    private List<QuestionModel> bookmarksList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    private int matchedQuestionPosition;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.rounded_corners));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        loadAds();

        tvQuestion = findViewById(R.id.tv_question);
        tvNoIndicator = findViewById(R.id.tv_no_indicator);
        optionsContainer = findViewById(R.id.options_container);
        btnShare = findViewById(R.id.btn_share);
        btnBookmarks = findViewById(R.id.btn_bookmarks);
        btnNext = findViewById(R.id.btn_next);

        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();

        getBookmarks();

        btnBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modelMatch()) {
                    bookmarksList.remove(matchedQuestionPosition);
                    btnBookmarks.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                } else {
                    bookmarksList.add(questionModelList.get(position));
                    btnBookmarks.setImageDrawable(getDrawable(R.drawable.bookmark));
                }
            }
        });

       setId = getIntent().getStringExtra("setId");

        for (int i = 0;i < 4;i++) {
            optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
        }

        questionModelList = new ArrayList<>();
        loadingDialog.show();
        myRef.child("Sets").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String id = dataSnapshot.getKey();
                            String question = dataSnapshot.child("question").getValue().toString();
                            String optionA = dataSnapshot.child("optionA").getValue().toString();
                            String optionB = dataSnapshot.child("optionB").getValue().toString();
                            String optionC = dataSnapshot.child("optionC").getValue().toString();
                            String optionD = dataSnapshot.child("optionD").getValue().toString();
                            String correctANS = dataSnapshot.child("correctANS").getValue().toString();

                            questionModelList.add(new QuestionModel(id,question,optionA,optionB,optionC,optionD,correctANS,setId));
                        }
                        if (questionModelList.size() > 0) {
                            for (int i = 0;i < 4;i++) {
                                optionsContainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        checkAnswer((Button) v);
                                    }
                                });
                            }
                            playAnim(tvQuestion, 0, questionModelList.get(position).getQuestion());

                            btnNext.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    btnNext.setEnabled(false);
                                    btnNext.setAlpha(0.7f);
                                    enableOption(true);
                                    position++;
                                    if (position == questionModelList.size()) {
                                        if (mInterstitialAd.isLoaded()) {
                                            mInterstitialAd.show();
                                            return;
                                        }
                                        Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                                        scoreIntent.putExtra("score", score);
                                        scoreIntent.putExtra("total", questionModelList.size());
                                        startActivity(scoreIntent);
                                        finish();
                                        return;
                                    }
                                    count = 0;
                                    playAnim(tvQuestion, 0, questionModelList.get(position).getQuestion());
                                }
                            });

                            btnShare.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String body = questionModelList.get(position).getQuestion() + "\n" +
                                                  questionModelList.get(position).getOptionA() + "\n" +
                                                  questionModelList.get(position).getOptionB() + "\n" +
                                                  questionModelList.get(position).getOptionC() + "\n" +
                                                  questionModelList.get(position).getOptionD();
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quiz Learn Challenge");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, body);
                                    startActivity(Intent.createChooser(shareIntent,"Share Via"));
                                }
                            });
                        } else {
                            finish();
                            Toast.makeText(QuestionsActivity.this, "Question is Empty!", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QuestionsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        finish();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeBookmarks();
    }

    private void playAnim(View view, int value, String data) {
        for (int i = 0;i < 4;i++) {
            optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
        }
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100).setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (value == 0 && count < 4) {
                            String option = "";
                            if (count == 0) {
                                option = questionModelList.get(position).getOptionA();
                            } else if (count == 1) {
                                option = questionModelList.get(position).getOptionB();
                            } else if (count == 2) {
                                option = questionModelList.get(position).getOptionC();
                            } else if (count == 3) {
                                option = questionModelList.get(position).getOptionD();
                            }
                            playAnim(optionsContainer.getChildAt(count), 0, option);
                            count++;
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (value == 0) {
                            try {
                                ((TextView)view).setText(data);
                                tvNoIndicator.setText(position+1+"/"+questionModelList.size());
                                if (modelMatch()) {
                                    btnBookmarks.setImageDrawable(getDrawable(R.drawable.bookmark));
                                } else {
                                    btnBookmarks.setImageDrawable(getDrawable(R.drawable.bookmark_border));
                                }
                            } catch (ClassCastException ex) {
                                ((Button)view).setText(data);
                            }
                            view.setTag(data);
                            playAnim(view, 1, data);
                        } else {
                            enableOption(true);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    private void checkAnswer(Button selectedOption) {
        enableOption(false);
        btnNext.setEnabled(true);
        btnNext.setAlpha(1);
        btnNext.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGrey)));
        if (selectedOption.getText().toString().equals(questionModelList.get(position).getCorrectANS())) {
            score++;
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
        } else {
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.error)));
            Button correctOption = optionsContainer.findViewWithTag(questionModelList.get(position).getCorrectANS());
            correctOption.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.successGreen)));
        }
    }

    private void enableOption(boolean enable) {
        for (int i = 0;i < 4;i++) {
            optionsContainer.getChildAt(i).setEnabled(enable);
            if (enable) {
                optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
            }
        }
    }

    private void getBookmarks() {
        String json = preferences.getString(KEY_NAME, "");
        Type type = new TypeToken<List<QuestionModel>>(){}.getType();
        bookmarksList = gson.fromJson(json, type);
        if (bookmarksList == null) {
            bookmarksList = new ArrayList<>();
        }
    }

    private boolean modelMatch() {
        boolean matched = false;
        int i = 0;
        for (QuestionModel model : bookmarksList) {
            if (model.getQuestion().equals(questionModelList.get(position).getQuestion())
                    && model.getCorrectANS().equals(questionModelList.get(position).getCorrectANS())
                    && model.getSetNo().equals(questionModelList.get(position).getSetNo())) {
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return matched;
    }

    private void storeBookmarks() {
        String json = gson.toJson(bookmarksList);
        editor.putString(KEY_NAME, json);
        editor.commit();
    }

    private void loadAds() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitialAd));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                Intent scoreIntent = new Intent(QuestionsActivity.this, ScoreActivity.class);
                scoreIntent.putExtra("score", score);
                scoreIntent.putExtra("total", questionModelList.size());
                startActivity(scoreIntent);
                finish();
                return;
            }
        });
    }
}