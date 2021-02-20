package com.example.canyoucount;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    final int OPERATION_BARRIER = 3;
    final int PROGRESS_FOR_LEVEL = 5;
    final Difficult DEF_DIFFICULT = Difficult.HARD;
    final int DEF_MILLIS = 30100;

    TextView levelView;
    TextView scoreView;
    TextView errorsView;
    TextView timeView;
    LinearLayout startButton;
    LinearLayout taskLayout;
    TextView numberOneView;
    TextView numberTwoView;
    TextView operationView;
    GridLayout answersGridLayout;
    TextView timeOutText;
    Difficult difficult;
    int mFirstNumRange;
    int mSecondNumRange;
    int mAnswersMax;
    int numberOne;
    int numberTwo;
    int answerCellNumber;
    int score;
    int errors;
    int level;
    int progress;
    int correctAnswer;
    int[] colors;
    Operation operation;
    CountDownTimer timer;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        handler = new Handler(getMainLooper());
        numberOneView = findViewById(R.id.number1);
        numberTwoView = findViewById(R.id.number2);
        operationView = findViewById(R.id.operation);
        answersGridLayout = findViewById(R.id.gridView);
        timeOutText = findViewById(R.id.timeOutText);
        levelView = findViewById(R.id.levelNumber);
        scoreView = findViewById(R.id.scoreNumber);
        errorsView = findViewById(R.id.errorsNumber);
        timeView = findViewById(R.id.timeNumber);
        startButton = findViewById(R.id.startButtonLayout);
        taskLayout = findViewById(R.id.taskLayout);
        level = 1;
        progress = 0;
        levelView.setText(String.valueOf(level));
        operation = Operation.ADDITION;
        difficult = DEF_DIFFICULT;
        colors = new int[]{
                R.color.beige, R.color.pink, R.color.yellow,
                R.color.light_purple, R.color.very_light_grey, R.color.light_blue,
                R.color.light_red, R.color.yellow_green, R.color.light_green
        };
        setAnswersGridLayout();
        answersGridLayout.setAlpha((float) 0.5);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskLayout.setVisibility(View.VISIBLE);
                answersGridLayout.setAlpha((float) 1.0);
                startButton.setVisibility(View.INVISIBLE);
                timeOutText.clearAnimation();
                timeOutText.setVisibility(View.INVISIBLE);
//                startButton.setOnClickListener(null);
                startTimer();
                setNewBoard();
            }
        });
    }

    private void setNewBoard() {
        setBoardColors();
        if (progress == PROGRESS_FOR_LEVEL) {
            if (score > errors) {
                this.level++;
                levelView.setText(String.valueOf(this.level));
                bounceView(levelView);
            }
            startTimer();
            resetScore();
        }
        if (this.level >= OPERATION_BARRIER) {
            int random = getRandomNumber(0, 2);
            operation = random == 0 ? Operation.ADDITION : Operation.SUBTRACTION;
        }

        if (operation == Operation.ADDITION) {
            mFirstNumRange = level + 3;
            mSecondNumRange = level + 3;
            mAnswersMax = 9 + level;
            numberOne = getRandomNumber(level - 1, mFirstNumRange);
            if (this.level < 5) {
                numberTwo = getRandomNumber(0, mSecondNumRange);
            } else {
                numberTwo = getRandomNumber(level - 5, mSecondNumRange);
            }
            operationView.setText("+");
            correctAnswer = numberOne + numberTwo;
        } else {
            mFirstNumRange = level + 3;
            mSecondNumRange = mFirstNumRange;
            mAnswersMax = mFirstNumRange + level;
            numberOne = getRandomNumber(level - 1, mFirstNumRange);
            if (this.level < 5) {
                numberTwo = getRandomNumber(0, numberOne);
            } else {
                numberTwo = getRandomNumber(level - 5, numberOne);
            }
            operationView.setText("-");
            correctAnswer = numberOne - numberTwo;
        }

        numberOneView.setText(String.valueOf(numberOne));
        numberTwoView.setText(String.valueOf(numberTwo));

        int[] numbersSet = new int[answersGridLayout.getChildCount()];
        answerCellNumber = new Random().nextInt(answersGridLayout.getChildCount());
        numbersSet[answerCellNumber] = correctAnswer;
        FrameLayout correctFrame = (FrameLayout) answersGridLayout.getChildAt(answerCellNumber);
        TextView numberView = (TextView) correctFrame.getChildAt(0);
        numberView.setText(String.valueOf(correctAnswer));

        correctFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable = getResources().getDrawable(R.drawable.button_round);
                drawable.setTint(getResources().getColor(R.color.green));
                correctFrame.setBackground(drawable);
                bounceView(correctFrame);
                correctAnswer();
            }
        });

        for (int i = 0; i < answersGridLayout.getChildCount(); i++) {
            if (i != answerCellNumber) {
                int randomNumber = new Random().nextInt(mAnswersMax);
                boolean duplicate = true;
                while (duplicate) {
                    boolean found = false;
                    if (i == 0 && randomNumber == correctAnswer) {
                        randomNumber = new Random().nextInt(mAnswersMax);
                        found = true;
                    } else {
                        for (int j = 0; j < i; j++) {
                            if (randomNumber == correctAnswer || numbersSet[j] == randomNumber) {
                                randomNumber = new Random().nextInt(mAnswersMax);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        duplicate = false;
                    }
                }
                numbersSet[i] = randomNumber;
                FrameLayout frame = (FrameLayout) answersGridLayout.getChildAt(i);
                TextView number = (TextView) frame.getChildAt(0);
                number.setText(String.valueOf(randomNumber));
                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Drawable drawable = getResources().getDrawable(R.drawable.button_round);
                        drawable.setTint(getResources().getColor(R.color.red));
                        frame.setBackground(drawable);
                        frame.setOnClickListener(null);
                        wrongAnswer();
                    }
                });
            }
        }
    }

    private void correctAnswer() {
        progress++;
        score++;
        if (progress == PROGRESS_FOR_LEVEL) {
            stopTimer();
        }
        scoreView.setTextColor(getResources().getColor(R.color.dark_green));
        scoreView.setText(String.valueOf(score));
        bounceView(scoreView);
        disableGridListeners();
        runnable = new Runnable() {
            @Override
            public void run() {
                setNewBoard();
            }
        };
        handler.postDelayed(runnable, 1500);
    }

    private void wrongAnswer() {
        errors++;
        errorsView.setTextColor(getResources().getColor(R.color.red));
        errorsView.setText(String.valueOf(errors));
    }

    private void setAnswersGridLayout() {
        int viewsCount;
        if (difficult == Difficult.HARD) {
            answersGridLayout.setColumnCount(3);
            answersGridLayout.setRowCount(3);
            viewsCount = 9;
        } else if (difficult == Difficult.MEDIUM) {
            answersGridLayout.setColumnCount(3);
            answersGridLayout.setRowCount(2);
            viewsCount = 6;
        } else {
            answersGridLayout.setColumnCount(2);
            answersGridLayout.setRowCount(2);
            viewsCount = 4;
        }
        for (int i = 0; i < viewsCount; i++) {
            FrameLayout frame = (FrameLayout) getLayoutInflater().inflate(R.layout.answer_frame, null);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            );
            params.height = 0;
            params.width = 0;
            params.topMargin = 25;
            params.bottomMargin = 25;
            params.leftMargin = 25;
            params.rightMargin = 25;
            frame.setLayoutParams(params);
            if (difficult == Difficult.HARD) {
                ((TextView) frame.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            }
            answersGridLayout.addView(frame);
        }
        setBoardColors();
    }

    private void bounceView(View view) {
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        anim.setInterpolator(interpolator);
        view.startAnimation(anim);
    }

    public int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private void setBoardColors() {
        for (int i = 0; i < answersGridLayout.getChildCount(); i++) {
            Drawable drawable = getResources().getDrawable(R.drawable.button_round);
            drawable.setTint(getResources().getColor(colors[i]));
            answersGridLayout.getChildAt(i).setBackground(drawable);
        }
    }

    private void resetScore() {
        progress = 0;
        score = 0;
        errors = 0;
        scoreView.setTextColor(getResources().getColor(R.color.grey));
        scoreView.setText(String.valueOf(score));
        bounceView(scoreView);
        errorsView.setTextColor(getResources().getColor(R.color.grey));
        errorsView.setText(String.valueOf(errors));
        bounceView(errorsView);
    }

    private void startTimer() {
        timeView.setTextColor(getResources().getColor(R.color.grey));

        timer =  new CountDownTimer(DEF_MILLIS, 1000){

            long remainingTime = DEF_MILLIS;

            boolean firstTick = true;

            @Override
            public void onTick(long millisUntilFinished) {
                if (firstTick) {
                    firstTick = false;
                } else {
                    remainingTime -= 1000;
                }
                displayRemainSecs(remainingTime);
            }

            @Override
            public void onFinish() {
                remainingTime -= 1000;
                displayRemainSecs(remainingTime);
                resetScore();
                disableGridListeners();
                taskLayout.setVisibility(View.INVISIBLE);
                answersGridLayout.setAlpha((float) 0.5);
                startButton.setVisibility(View.VISIBLE);
                timeOutText.setVisibility(View.VISIBLE);
                bounceView(timeOutText);
                handler.removeCallbacks(runnable);
            }
        }.start();
    }

    private void displayRemainSecs(long remainingTime) {
        if (remainingTime < 4000) {
            timeView.setText(String.valueOf(remainingTime / 1000));
            timeView.clearAnimation();
            timeView.setTextColor(getResources().getColor(R.color.red));
            bounceView(timeView);
        } else {
            timeView.setText(String.valueOf(remainingTime / 1000));
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void disableGridListeners() {
        for (int i = 0; i < answersGridLayout.getChildCount(); i++) {
            answersGridLayout.getChildAt(i).setOnClickListener(null);
        }
    }

}