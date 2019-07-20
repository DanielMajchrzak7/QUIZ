package com.example.quiz_application;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extraScore";
    public static final long COUNTDOUWN_IN_MS = 30000;
    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCountDown;
    private RadioGroup rbGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private Button buttonConfirmNext;

    //zmienna ktroa bedzieodpowiedzialna za zmiane koloru buttona w zaleznosci od dobrej badz zlej odpowiedzi
    private ColorStateList textColorDefoultRb;

    private CountDownTimer countDownTimer;
    private long timeLeftInMs;

    //int pokazujacy ile pytan ostalo juz pokazane
    private int questionCounter;
    //liczba wszystkich pytan jakie mamy w array list
    private int questionCountTotal;
    //aktualne pytanie
    private Question currentQuestion;

    private int score;
    //bedzie trzymal pytanie mimo ze nie odpowiedzielismy a albo pokze nastepne pytanie jesli odpowiedzialem na pytanie
    public boolean answered;

    //lista wszystkich pytan
    private List<Question> questionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        rbGroup = findViewById(R.id.radio_group);
        rb1 = findViewById(R.id.radio_button1);
        rb2 = findViewById(R.id.radio_button2);
        rb3 = findViewById(R.id.radio_button3);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefoultRb = rb1.getTextColors();

        QuizDbHelper dbHelper = new QuizDbHelper(this);
        questionList = dbHelper.getAllQuestions();
        questionCountTotal = questionList.size();
        Collections.shuffle(questionList);

        showNextQuestion();

        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!answered){
                    if(rb1.isChecked() || rb2.isChecked() || rb3.isChecked()){
                        checkAnswer();
                    }
                    else {
                        Toast.makeText(QuizActivity.this, "Zaznacz odpowiedź!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    showNextQuestion();
                }
            }
        });
    }

    private void showNextQuestion(){

        rb1.setTextColor(textColorDefoultRb);
        rb2.setTextColor(textColorDefoultRb);
        rb3.setTextColor(textColorDefoultRb);
        rbGroup.clearCheck();

        if(questionCounter < questionCountTotal){
            currentQuestion = questionList.get(questionCounter);

            textViewQuestion.setText(currentQuestion.getQuestion());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());

            questionCounter++;

            textViewQuestionCount.setText("Pytanie: " + questionCounter + " z " + questionCountTotal);


            answered = false;
            buttonConfirmNext.setText("Potwierdź");

            timeLeftInMs = COUNTDOUWN_IN_MS;
            startCountdown();
        } else{
            finishQuiz();
        }
    }

    private void startCountdown(){
    countDownTimer = new CountDownTimer(timeLeftInMs, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            timeLeftInMs = millisUntilFinished;
            updateCountDownText();
        }
        @Override
        public void onFinish() {
            timeLeftInMs = 0;
            updateCountDownText();
            checkAnswer();
        }
    }.start();
    }

    private void updateCountDownText (){
        int minutes = (int) (timeLeftInMs / 1000) / 60;
        int seconds = (int) (timeLeftInMs / 1000) % 60;


        String timeFormatted =  String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);

        textViewCountDown.setText(timeFormatted);
    }


    private void checkAnswer(){
        answered = true;

         countDownTimer.cancel();

        RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
        int answerNr = rbGroup.indexOfChild(rbSelected) + 1;

        if(answerNr==currentQuestion.getAnswerNr()){
            score++;
            textViewScore.setText("Wynik: " + score);
        }
        showSolution();
    }

    private void showSolution(){
        rb1.setTextColor(Color.RED);
        rb2.setTextColor(Color.RED);
        rb3.setTextColor(Color.RED);

        switch(currentQuestion.getAnswerNr()){
            case 1:
                rb1.setTextColor(Color.GREEN);
                textViewQuestion.setText("Odpowiedź A jest odpowiedzią prawidłową!");
                break;
            case 2:
                rb2.setTextColor(Color.GREEN);
                textViewQuestion.setText("Odpowiedź B jest odpowiedzią prawidłową!");
                break;
            case 3:
                rb3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Odpowiedź C jest odpowiedzią prawidłową!");
                break;
        }

        if(questionCounter<questionCountTotal){
            buttonConfirmNext.setText("Następne pytanie");
        }
        else{
            buttonConfirmNext.setText("Zakończ");
        }
    }

    private void finishQuiz(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SCORE, score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy(){
    super.onDestroy();
    if (countDownTimer != null){
        countDownTimer.cancel();
    }
    }
}
